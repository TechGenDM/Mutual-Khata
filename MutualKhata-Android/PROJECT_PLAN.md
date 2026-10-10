# Build plan

## Milestone 0 — Android UI foundation (current)
- [x] Native Android project scaffold
- [x] Home, Khata, Requests, Profile screens
- [x] Entry request demo flow and visible demo boundaries
- [x] Initial data model / backend design notes
- [ ] Run on emulator and verify screen sizes

## Milestone 1 — Real shared ledger (next)
- [ ] Set up Supabase project and environment configuration
- [ ] Complete auth and phone verification strategy
- [ ] Invite/accept retailer-distributor relationship
- [ ] Persist local drafts/cache with Room
- [ ] Server-side entry create/approve/reject/dispute RPCs
- [ ] Audit-log and correction request/approval tests
- [ ] Test on two separate Android devices

## Milestone 2 — Field pilot
- [ ] Gujarati/Hindi/Odia/local wording review with actual users
- [ ] Run with 5–10 existing retailer-distributor pairs
- [ ] Measure entry confirmation time, mismatch rate, abandonment, sync failures
- [ ] Add agent permission model and weekly statement only after core flow works

## Non-negotiable acceptance test
Create an invoice on Phone A. Phone B must see the exact pending invoice. Until Phone B approves, neither phone may show it as confirmed or include it in the confirmed balance. After approval, both phones show the same amount and audit history. An amount change must create a new correction request and preserve the original entry.
