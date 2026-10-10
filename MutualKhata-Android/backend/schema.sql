-- Mutual Khata: initial PostgreSQL design sketch.
-- Review/finish RLS, invite flow, grants, functions and migrations before any real data.

create extension if not exists pgcrypto;

do $$ begin
  create type public.mk_entry_type as enum (
    'goods_received', 'payment_made', 'goods_returned', 'opening_balance', 'adjustment'
  );
exception when duplicate_object then null; end $$;

do $$ begin
  create type public.mk_entry_status as enum ('pending', 'confirmed', 'rejected', 'disputed');
exception when duplicate_object then null; end $$;

do $$ begin
  create type public.mk_relationship_status as enum ('pending_invite', 'active', 'paused', 'closed');
exception when duplicate_object then null; end $$;

do $$ begin
  create type public.mk_correction_status as enum ('pending', 'approved', 'rejected');
exception when duplicate_object then null; end $$;

create table if not exists public.mk_profiles (
  user_id uuid primary key references auth.users(id) on delete cascade,
  display_name text not null,
  business_name text,
  preferred_language text not null default 'hi',
  created_at timestamptz not null default now()
);

create table if not exists public.mk_relationships (
  id uuid primary key default gen_random_uuid(),
  retailer_id uuid not null references public.mk_profiles(user_id),
  distributor_id uuid not null references public.mk_profiles(user_id),
  status public.mk_relationship_status not null default 'pending_invite',
  created_by uuid not null references public.mk_profiles(user_id),
  created_at timestamptz not null default now(),
  accepted_at timestamptz,
  constraint mk_relationship_distinct_parties check (retailer_id <> distributor_id),
  constraint mk_relationship_unique_pair unique (retailer_id, distributor_id)
);

-- balance_delta is from the retailer's point of view:
-- positive => retailer owes distributor; negative => distributor owes retailer.
-- In production, calculate/validate this server-side from entry_type and amount.
create table if not exists public.mk_ledger_entries (
  id uuid primary key default gen_random_uuid(),
  relationship_id uuid not null references public.mk_relationships(id),
  created_by uuid not null references public.mk_profiles(user_id),
  entry_type public.mk_entry_type not null,
  amount numeric(12,2) not null check (amount >= 0),
  balance_delta numeric(12,2) not null,
  description text not null default '',
  invoice_ref text,
  occurred_at date not null default current_date,
  status public.mk_entry_status not null default 'pending',
  confirmed_by uuid references public.mk_profiles(user_id),
  confirmed_at timestamptz,
  corrects_entry_id uuid references public.mk_ledger_entries(id),
  created_at timestamptz not null default now(),
  constraint mk_adjustment_needs_target check (entry_type <> 'adjustment' or corrects_entry_id is not null),
  constraint mk_confirmed_has_actor check (status <> 'confirmed' or (confirmed_by is not null and confirmed_at is not null))
);

create table if not exists public.mk_correction_requests (
  id uuid primary key default gen_random_uuid(),
  entry_id uuid not null references public.mk_ledger_entries(id),
  requested_by uuid not null references public.mk_profiles(user_id),
  proposed_amount numeric(12,2) not null check (proposed_amount >= 0),
  proposed_description text,
  reason text not null,
  status public.mk_correction_status not null default 'pending',
  resolved_by uuid references public.mk_profiles(user_id),
  requested_at timestamptz not null default now(),
  resolved_at timestamptz
);

create unique index if not exists mk_one_pending_correction_per_entry
  on public.mk_correction_requests(entry_id) where status = 'pending';

create table if not exists public.mk_audit_events (
  id uuid primary key default gen_random_uuid(),
  relationship_id uuid not null references public.mk_relationships(id),
  entry_id uuid references public.mk_ledger_entries(id),
  actor_id uuid references public.mk_profiles(user_id),
  event_type text not null,
  details jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now()
);

create index if not exists mk_ledger_relationship_time_idx
  on public.mk_ledger_entries(relationship_id, created_at desc);
create index if not exists mk_audit_relationship_time_idx
  on public.mk_audit_events(relationship_id, created_at desc);

-- Enable RLS now so accidental client reads are denied until reviewed policies are added.
-- No client UPDATE/DELETE policy is intentionally defined for ledger entries.
alter table public.mk_profiles enable row level security;
alter table public.mk_relationships enable row level security;
alter table public.mk_ledger_entries enable row level security;
alter table public.mk_correction_requests enable row level security;
alter table public.mk_audit_events enable row level security;

-- 1. Add least-privilege SELECT/INSERT policies for participants of active relationships.
create policy "Users can view their own profile" on public.mk_profiles for select using (auth.uid() = user_id);
create policy "Users can update their own profile" on public.mk_profiles for update using (auth.uid() = user_id);
create policy "Users can insert their own profile" on public.mk_profiles for insert with check (auth.uid() = user_id);
create policy "Users can view partner profiles" on public.mk_profiles for select using (
  exists (
    select 1 from public.mk_relationships
    where (retailer_id = auth.uid() and distributor_id = mk_profiles.user_id)
       or (distributor_id = auth.uid() and retailer_id = mk_profiles.user_id)
  )
);

create policy "Users can view their relationships" on public.mk_relationships for select using (
  retailer_id = auth.uid() or distributor_id = auth.uid()
);

create policy "Users can view ledger entries of their relationships" on public.mk_ledger_entries for select using (
  exists (
    select 1 from public.mk_relationships r
    where r.id = relationship_id and (r.retailer_id = auth.uid() or r.distributor_id = auth.uid())
  )
);

create policy "Users can view correction requests for their relationships" on public.mk_correction_requests for select using (
  exists (
    select 1 from public.mk_ledger_entries e
    join public.mk_relationships r on e.relationship_id = r.id
    where e.id = entry_id and (r.retailer_id = auth.uid() or r.distributor_id = auth.uid())
  )
);

create policy "Users can view audit events of their relationships" on public.mk_audit_events for select using (
  exists (
    select 1 from public.mk_relationships r
    where r.id = relationship_id and (r.retailer_id = auth.uid() or r.distributor_id = auth.uid())
  )
);

-- 2. Add server-side invite/acceptance functions; never let a client impersonate the other party.
create or replace function public.mk_invite_partner(partner_id uuid, is_retailer boolean) returns uuid as $$
declare
  new_rel_id uuid;
begin
  if partner_id = auth.uid() then
    raise exception 'Cannot invite yourself';
  end if;

  insert into public.mk_relationships (retailer_id, distributor_id, status, created_by)
  values (
    case when is_retailer then auth.uid() else partner_id end,
    case when is_retailer then partner_id else auth.uid() end,
    'pending_invite',
    auth.uid()
  ) returning id into new_rel_id;

  return new_rel_id;
end;
$$ language plpgsql security definer;

create or replace function public.mk_accept_invite(rel_id uuid) returns void as $$
begin
  update public.mk_relationships
  set status = 'active', accepted_at = now()
  where id = rel_id
    and status = 'pending_invite'
    and (
      (retailer_id = auth.uid() and created_by != auth.uid()) or
      (distributor_id = auth.uid() and created_by != auth.uid())
    );

  if not found then
    raise exception 'Invite not found or you are not authorized to accept it';
  end if;
end;
$$ language plpgsql security definer;

-- 3. Add RPC functions to create entries, approve/reject entries, dispute entries, request/approve corrections.
-- 4. In approve RPC, verify the caller is the *other* participant and the entry is still pending.
create or replace function public.mk_create_entry(
  rel_id uuid,
  e_type public.mk_entry_type,
  e_amount numeric(12,2),
  e_desc text default '',
  e_invoice text default null
) returns uuid as $$
declare
  new_entry_id uuid;
  rel record;
  calc_delta numeric(12,2);
begin
  select * into rel from public.mk_relationships where id = rel_id and status = 'active';
  if not found then
    raise exception 'Active relationship not found';
  end if;
  if auth.uid() not in (rel.retailer_id, rel.distributor_id) then
    raise exception 'Not authorized';
  end if;

  if e_type = 'goods_received' then
    calc_delta := e_amount;
  elsif e_type = 'payment_made' then
    calc_delta := -e_amount;
  elsif e_type = 'goods_returned' then
    calc_delta := -e_amount;
  elsif e_type = 'opening_balance' then
    calc_delta := e_amount;
  else
    calc_delta := 0;
  end if;

  insert into public.mk_ledger_entries (
    relationship_id, created_by, entry_type, amount, balance_delta, description, invoice_ref
  ) values (
    rel_id, auth.uid(), e_type, e_amount, calc_delta, e_desc, e_invoice
  ) returning id into new_entry_id;

  insert into public.mk_audit_events (relationship_id, entry_id, actor_id, event_type, details)
  values (rel_id, new_entry_id, auth.uid(), 'entry_created', jsonb_build_object('type', e_type, 'amount', e_amount));

  return new_entry_id;
end;
$$ language plpgsql security definer;

create or replace function public.mk_approve_entry(entry_id uuid) returns void as $$
declare
  rec record;
begin
  select e.*, r.retailer_id, r.distributor_id into rec
  from public.mk_ledger_entries e
  join public.mk_relationships r on e.relationship_id = r.id
  where e.id = entry_id;

  if not found then raise exception 'Entry not found'; end if;
  if rec.status != 'pending' then raise exception 'Entry not pending'; end if;
  if auth.uid() not in (rec.retailer_id, rec.distributor_id) then raise exception 'Not authorized'; end if;
  if rec.created_by = auth.uid() then raise exception 'Cannot approve your own entry'; end if;

  update public.mk_ledger_entries
  set status = 'confirmed', confirmed_by = auth.uid(), confirmed_at = now()
  where id = entry_id;

  insert into public.mk_audit_events (relationship_id, entry_id, actor_id, event_type)
  values (rec.relationship_id, entry_id, auth.uid(), 'entry_approved');
end;
$$ language plpgsql security definer;

create or replace function public.mk_reject_entry(entry_id uuid) returns void as $$
declare
  rec record;
begin
  select e.*, r.retailer_id, r.distributor_id into rec
  from public.mk_ledger_entries e
  join public.mk_relationships r on e.relationship_id = r.id
  where e.id = entry_id;

  if not found then raise exception 'Entry not found'; end if;
  if rec.status != 'pending' then raise exception 'Entry not pending'; end if;
  if auth.uid() not in (rec.retailer_id, rec.distributor_id) then raise exception 'Not authorized'; end if;
  if rec.created_by = auth.uid() then raise exception 'Cannot reject your own entry'; end if;

  update public.mk_ledger_entries
  set status = 'rejected'
  where id = entry_id;

  insert into public.mk_audit_events (relationship_id, entry_id, actor_id, event_type)
  values (rec.relationship_id, entry_id, auth.uid(), 'entry_rejected');
end;
$$ language plpgsql security definer;

-- 5. Implement approved correction as an append-only adjustment row; never overwrite the source amount.
create or replace function public.mk_request_correction(
  e_id uuid,
  prop_amount numeric(12,2),
  prop_desc text,
  req_reason text
) returns uuid as $$
declare
  rec record;
  new_req_id uuid;
begin
  select e.*, r.retailer_id, r.distributor_id into rec
  from public.mk_ledger_entries e
  join public.mk_relationships r on e.relationship_id = r.id
  where e.id = e_id;

  if not found then raise exception 'Entry not found'; end if;
  if auth.uid() not in (rec.retailer_id, rec.distributor_id) then raise exception 'Not authorized'; end if;

  insert into public.mk_correction_requests (
    entry_id, requested_by, proposed_amount, proposed_description, reason
  ) values (
    e_id, auth.uid(), prop_amount, prop_desc, req_reason
  ) returning id into new_req_id;

  insert into public.mk_audit_events (relationship_id, entry_id, actor_id, event_type, details)
  values (rec.relationship_id, e_id, auth.uid(), 'correction_requested', jsonb_build_object('proposed_amount', prop_amount, 'reason', req_reason));

  return new_req_id;
end;
$$ language plpgsql security definer;

create or replace function public.mk_approve_correction(req_id uuid) returns void as $$
declare
  req record;
  e record;
  adj_id uuid;
  adj_amount numeric(12,2);
  adj_delta numeric(12,2);
begin
  select * into req from public.mk_correction_requests where id = req_id and status = 'pending';
  if not found then raise exception 'Request not found or not pending'; end if;

  select le.*, r.retailer_id, r.distributor_id into e
  from public.mk_ledger_entries le
  join public.mk_relationships r on le.relationship_id = r.id
  where le.id = req.entry_id;

  if auth.uid() not in (e.retailer_id, e.distributor_id) then raise exception 'Not authorized'; end if;
  if req.requested_by = auth.uid() then raise exception 'Cannot approve your own correction request'; end if;

  adj_amount := abs(e.amount - req.proposed_amount);
  adj_delta := req.proposed_amount - e.amount;
  if e.balance_delta < 0 then
    adj_delta := -adj_delta;
  end if;

  update public.mk_correction_requests
  set status = 'approved', resolved_by = auth.uid(), resolved_at = now()
  where id = req_id;

  insert into public.mk_ledger_entries (
    relationship_id, created_by, entry_type, amount, balance_delta, description, status, confirmed_by, confirmed_at, corrects_entry_id
  ) values (
    e.relationship_id, auth.uid(), 'adjustment', adj_amount, adj_delta, 'Correction: ' || coalesce(req.proposed_description, e.description), 'confirmed', auth.uid(), now(), e.id
  ) returning id into adj_id;

  insert into public.mk_audit_events (relationship_id, entry_id, actor_id, event_type, details)
  values (e.relationship_id, e.id, auth.uid(), 'correction_approved', jsonb_build_object('adjustment_entry_id', adj_id));
end;
$$ language plpgsql security definer;

create or replace function public.mk_reject_correction(req_id uuid) returns void as $$
declare
  req record;
  e record;
begin
  select * into req from public.mk_correction_requests where id = req_id and status = 'pending';
  if not found then raise exception 'Request not found or not pending'; end if;

  select le.*, r.retailer_id, r.distributor_id into e
  from public.mk_ledger_entries le
  join public.mk_relationships r on le.relationship_id = r.id
  where le.id = req.entry_id;

  if auth.uid() not in (e.retailer_id, e.distributor_id) then raise exception 'Not authorized'; end if;
  if req.requested_by = auth.uid() then raise exception 'Cannot reject your own correction request'; end if;

  update public.mk_correction_requests
  set status = 'rejected', resolved_by = auth.uid(), resolved_at = now()
  where id = req_id;

  insert into public.mk_audit_events (relationship_id, entry_id, actor_id, event_type)
  values (e.relationship_id, e.id, auth.uid(), 'correction_rejected');
end;
$$ language plpgsql security definer;

-- 6. Add a trigger preventing direct edits/deletes of confirmed entries and append audit events for all state changes.
create or replace function public.mk_prevent_confirmed_edits() returns trigger as $$
begin
  if TG_OP = 'DELETE' then
    if OLD.status = 'confirmed' then
      raise exception 'Cannot delete a confirmed entry';
    end if;
    return OLD;
  end if;

  if TG_OP = 'UPDATE' then
    if OLD.status = 'confirmed' and NEW.status = 'confirmed' then
      if OLD.amount != NEW.amount or OLD.balance_delta != NEW.balance_delta or OLD.entry_type != NEW.entry_type then
        raise exception 'Cannot modify financial amounts of a confirmed entry';
      end if;
    end if;
    return NEW;
  end if;
  return NEW;
end;
$$ language plpgsql;

create trigger mk_ledger_entries_confirmed_trigger
before update or delete on public.mk_ledger_entries
for each row execute function public.mk_prevent_confirmed_edits();

-- 7. Add tests for cross-account access, replay, duplicate approvals, race conditions and authorization bypasses.
-- (Tests should be implemented in a dedicated pgTAP test suite)
