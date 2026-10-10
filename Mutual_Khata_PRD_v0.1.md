---
title: Mutual Khata
subtitle: Product Requirements Document | Native Android MVP
author: Product & Engineering Working Draft
date: 9 October 2026
subject: Research-backed product specification v0.1
---

**Version:** 0.1  
**Status:** Draft for product discussion  
**Research snapshot:** 9 October 2026  
**Platform:** Native Android app; no PWA or mobile website product  
**Name status:** Working name only; trademark clearance pending

> **Product thesis:** Small retailers and distributors should be able to keep one shared, understandable account of goods supplied, payments acknowledged, and corrections requested—without either side silently rewriting a confirmed record.

---

## Document at a glance

| Area | Decision in this draft |
|---|---|
| Initial audience | Small retailers, local distributors, and distributor collection agents in India; no business registration required to try the pilot |
| Core problem | Separate registers, missing receipts, disputed amounts, delayed reconciliation, and low trust in unilateral records |
| Main product promise | A shared, server-backed pair ledger where confirmed transactions are not edited or deleted in place; changes require a visible correction request and counterparty approval |
| Platform | Native Android app, built with Kotlin and Jetpack Compose |
| Trust model | Explicit counterparty acknowledgement, role permissions, append-only business events, and visible history |
| Offline behaviour | Cached history and local drafts are available offline; no offline action is shown as shared/confirmed until the server accepts it |
| MVP deliberately excludes | Payments/UPI processing, lending, credit scoring, GST invoicing, inventory, AI/OCR/voice, iOS, and a web/PWA product client |
| Launch approach | Founder-led pilot with 5–10 retailer–distributor pairs before wider release |
| Key success signal | Active pairs use the ledger repeatedly and most proposed transactions are acknowledged promptly without confusing pending entries with confirmed balances |

## 1. Executive summary

Mutual Khata is a native Android application for the informal and micro-business trade ecosystem in India. It is designed for a retailer who buys goods on credit from several distributors and a distributor whose owner or field agent visits multiple retailers to deliver goods or collect payments.

Today, each side may maintain a separate paper register or a separate digital record. A missing receipt, a mistaken amount, a delayed entry, or a disputed payment can become a trust issue. The parties may not agree on the opening balance, whether goods were received, whether a payment was collected, or whether a correction was legitimate.

Mutual Khata will begin with one narrow job: **make the shared account between one retailer and one distributor clearer and harder to silently alter.** Each retailer–distributor relationship has its own ledger. A retailer can have many separate distributor ledgers; a distributor can have many retailer ledgers. The pair does not need to settle every other business relationship to use the product.

A transaction is proposed by one side and reviewed by the other. Only confirmed records affect the confirmed balance. A confirmed transaction is never edited in place: a correction is a new request linked to the original, with the original value, proposed value, reason, author, reviewer, and timestamps visible in history. A dispute stays visible and does not silently change the confirmed balance.

The application itself is native Android, but the shared ledger needs a secure backend and database. A local-only app cannot safely coordinate approval across two separate phones. Offline use will therefore be conservative: users can read cached data and prepare drafts without a connection, but the interface must clearly show that a draft is not shared and is not part of the confirmed balance until server acknowledgement.

This PRD defines the initial scope, behaviour, data model, success metrics, security constraints, pilot, and unresolved decisions. It is a working product specification, not a claim that market demand, legal compliance, or name clearance has already been proven.

## 2. Problem and context

### 2.1 The real-world workflow

A typical relationship looks like this:

1. A distributor supplies goods worth ₹20,000 to a retailer on credit.
2. The retailer pays ₹5,000 two days later, then another ₹5,000 during a later visit.
3. The retailer may deal with several distributors and may use a different notebook or record for each.
4. A distributor owner may not personally visit every shop; a field agent may deliver stock or collect cash.
5. At reconciliation, the two records may disagree. A paper slip may have been lost, a payment may have been entered late, or one side may dispute the value.

The software cannot independently prove that goods physically arrived or that cash changed hands. It can, however, make each party's claim explicit, capture acknowledgement, preserve the history of changes, and make disagreements visible before they are mistaken for agreed balances.

### 2.2 User problems to validate

- **Split records:** the two parties rely on their own registers or phone records.
- **Silent edits:** a person may overwrite a number without leaving an understandable trail.
- **Lost proof:** paper receipts and delivery slips can be misplaced or damaged.
- **Delayed entry:** a field agent can collect money while the office record is updated later.
- **Opening-balance ambiguity:** parties may begin with different ideas of what is already due.
- **Complexity and low digital confidence:** accounting terminology, setup screens, GST requirements, and computer-first workflows can be barriers for a very small business.
- **Connectivity constraints:** a shop or market may have unreliable mobile internet, so the app must not lose drafts or pretend that an unsynced event is confirmed.

These are hypotheses based on the founder's description. The pilot must confirm frequency, severity, current workarounds, and willingness to adopt a shared workflow.

## 3. Market and competitive research

Research is based on publicly accessible product listings and platform/legal guidance checked on 9 October 2026. Product listings describe advertised features; they do not independently verify how every feature behaves in real use.

| Product / reference | Publicly described overlap | Implication for Mutual Khata |
|---|---|---|
| OkCredit | Digital ledger, customer/supplier balances, transaction sharing, billing and payment features [2] | A basic ledger and reminders are not a sufficient differentiator. |
| Khatabook | Digital khata, balance tracking, reminders and records [3] | The experience must be simpler and more trustworthy than a one-sided personal ledger, not just another ledger screen. |
| DeLe: Khata Book & Udhar Hisab | Its current public Play listing advertises a “Shared Mutual Khata” so two people can view the same khata [1] | Shared visibility alone is already publicly advertised by at least one product. Mutual Khata must differentiate through specific transaction-level acknowledgement, visible correction history, and field-agent controls. |
| Vyapar | Billing, inventory, accounting, and offline/online workflows [4] | A broad accounting/POS product is not the MVP target. Do not compete on inventory or GST features initially. |
| Android offline-first guidance | Recommends local data sources, server synchronization, and explicit conflict handling for offline-capable apps [5] | Local drafts must be separated from server-confirmed shared records. “Last write wins” is unsafe for financial records. |
| Google Play policy and Android guidance | Account-deletion path requirements, target API policy, accessibility and security guidance [6–9] | Privacy, account deletion, accessibility, security, and API targeting are release requirements—not afterthoughts. |

### Competitive conclusion

The product opportunity is **not “a khata that both users can see.”** A more defensible initial proposition is:

> **A shared transaction record with clear states: proposed, awaiting response, confirmed, disputed, rejected, or corrected—with no silent overwrite of a confirmed entry.**

Before claiming that this workflow is unique, the team should install and test relevant apps directly, document their transaction and correction flows, and conduct user interviews. Public listings alone do not establish that no other product offers comparable semantics.

## 4. Vision, positioning, and principles

### Vision

Make everyday credit transactions between India's small retailers and distributors easier to remember, verify, and reconcile.

### Positioning statement

For small retailers and local distributors who rely on informal credit, Mutual Khata is a simple Android shared-ledger tool that records what each side proposed and acknowledged. Unlike a private notebook or an editable one-sided ledger, it keeps a visible history of confirmation and correction events.

### Product principles

1. **Trust through transparency, not a magic code.** A one-time code can help link accounts or authenticate an action, but it cannot prove that an invoice or payment is factually correct. Show the exact amount, type, date, proposer, and consequence before approval.
2. **Confirmed means acknowledged, not objectively proven.** The app records each party's acknowledgement; it is not a court, auditor, or independent witness to physical delivery or cash movement.
3. **No silent overwrites.** Correct with a new event linked to the original; preserve the original claim and decision history.
4. **Pending is never confused with confirmed.** Pending or disputed items are visible but excluded from the confirmed balance.
5. **Simple enough for a busy counter.** Everyday actions should require little typing, use familiar language, and remain usable on budget Android phones.
6. **Many relationships, each separate.** One retailer can connect to many distributors. Each pair has a separate ledger and opening-balance confirmation.
7. **Offline honestly.** Local drafts may be saved offline, but the app must not suggest that the other party has seen or approved them until the server confirms it.
8. **No unnecessary data collection.** Do not require GSTIN, Aadhaar, bank details, email, or formal business registration for the pilot.

## 5. Target users and jobs to be done

### Persona A — Retailer / shopkeeper

- Operates a small kirana, general store, mobile-accessory shop, or similar business.
- Buys from multiple distributors, sometimes with weekly or rolling credit.
- Wants to know what is confirmed, what is pending, and what has already been paid.
- May prefer Hindi or another familiar language and may not use accounting software.

**Job to be done:** “When a distributor says I owe a certain amount, help me compare it with the records we both acknowledged, so I can identify any difference before paying or settling.”

### Persona B — Distributor owner / supervisor

- Supplies goods to many retailers and needs a reliable outstanding ledger.
- May delegate shop visits and collection to field agents.
- Needs to see which entries await a retailer response and which are disputed.

**Job to be done:** “When I need to collect money or reconcile an account, show me the same confirmed balance my retailer sees and preserve proof of the changes we both accepted.”

### Persona C — Distributor field / collection agent

- Visits shops to deliver goods or collect payments on behalf of the distributor.
- Needs quick access to assigned retailers and a way to record what happened.
- Must not be able to change confirmed balances or act outside permissions granted by the distributor.

**Job to be done:** “During a shop visit, let me record a delivery or collection quickly and leave both parties with a clear record of what was entered and acknowledged.”

### Initial target market

Start with a small, local pilot where the founder can observe usage directly. Do not gate access on formal registration. Focus on retailers and distributors who already transact on credit and are willing to test a mutual ledger. The first release should be language- and workflow-specific to the pilot region rather than attempting every Indian trade category at once.

## 6. Goals, non-goals, and MVP boundary

### Goals for the MVP

- Establish a retailer–distributor pair without requiring a registered company.
- Allow each party to propose goods/credit entries, payments, and corrections.
- Require counterparty acknowledgement before a record becomes confirmed.
- Present one canonical confirmed balance per pair, with clear direction: “you need to pay” or “they need to pay.”
- Preserve who proposed, who approved/rejected/disputed, and when.
- Support distributor owners and explicitly authorized field agents.
- Prevent accidental duplicate submissions and unsafe offline merging.
- Validate that real users will use the workflow repeatedly.

### Non-goals for the first MVP

- UPI, bank transfer, wallet, payment-gateway, or cash-handling functionality. The app records acknowledgements about payments; it does not move money.
- Loans, credit scoring, collections automation that threatens or shames debtors, or lending referrals.
- GST invoicing, tax filing, accounting compliance, inventory management, barcode/POS, payroll, or full business ERP.
- AI, voice transcription, OCR of bills, blockchain, smart contracts, or automatic claims that a record is true.
- iOS, web/PWA, desktop, or an admin website as a user-facing product.
- Multi-level distributor chains, retailer-to-retailer ledgers, group settlements, or cross-ledger netting.
- Automatic resolution of disputed transactions. The product should expose the dispute and provide a clear next step, not make a judgement on who is right.

### Scope decision

Build a native Android product, but do not confuse “no website product” with “no backend.” A production shared ledger needs an API/service, persistent cloud database, authentication, access controls, notifications, backups, and operational monitoring. Google Play also requires an external account-deletion resource when in-app account creation is offered [6]; this can be a minimal privacy/support page or deletion endpoint, not a PWA or web product interface.

## 7. MVP features and priorities

Priority definitions: **P0 = needed for the first meaningful pilot; P1 = add after the core trust flow works; P2 = explicitly later.**

| Feature | Priority | Requirement |
|---|---|---|
| Phone-based sign-in | P0 | Verify the phone number using OTP. Do not treat login OTP as approval of a financial entry. |
| Lightweight profile | P0 | Display name, role, business display name, and preferred language. Registration number, GSTIN, Aadhaar, email, and bank details are not required. |
| Retailer–distributor linking | P0 | Invite using a QR code or short-lived pairing code. Show the other party's name and phone suffix before either side accepts. Both parties must accept the link. |
| Multiple relationships | P0 | A retailer can have multiple independent distributor ledgers; a distributor can have multiple retailer ledgers. |
| Opening balance | P0 | One party proposes who owes whom and how much. The other party must confirm or dispute it. Until confirmed, it is shown as pending, not as confirmed debt. |
| Credit/goods entry | P0 | Record date, amount, short description/reference, direction, proposer, and optional note. Keep the flow lightweight; no mandatory SKU catalog. |
| Payment/collection entry | P0 | Record amount, date/time, method label (cash/other, descriptive only), and who entered it. Both sides acknowledge the entry before it affects confirmed balance. |
| Return/adjustment | P0 | Record a goods return or adjustment with amount and reason. Require counterparty acknowledgement. Do not allow an unreasoned balance-only change. |
| Confirm / dispute / reject | P0 | Counterparty sees the exact proposed details and chooses an explicit action. A rejection or dispute never silently changes the confirmed balance. |
| Correction request | P0 | Correct a confirmed item only by proposing a linked correction/reversal. Original details remain visible. The correction affects the balance only after approval. |
| Shared balance and history | P0 | Show confirmed balance, direction, confirmed transactions, pending requests, disputes, and corrections as separate sections/states. |
| Field-agent role | P0 | Distributor owner invites agents and assigns retailer access. Every agent action shows the agent's identity and is logged. Delegated authority must be explicit. |
| Notifications | P0 | In-app request inbox and push notifications for new requests, decisions, and reminders. The inbox remains the source of truth if push is missed. |
| Local cache and offline drafts | P0 | Read previously synced data offline and save unfinished drafts locally. Clearly label unsynced drafts as “only on this phone—not shared.” No offline approval is considered final. |
| Basic privacy and security | P0 | Authorization checks on the server, TLS, least-privilege access, secure session handling, protected secrets, audit logs, and deletion workflow planned before public release. |
| Settings / support | P0 | Change language, see app version, get support, log out, and request account deletion. |
| Shareable account statement | P1 | Generate a simple date-filtered PDF/text statement and share through the Android share sheet. It must distinguish confirmed from disputed/pending entries. |
| Bill/receipt image attachments | P1 | Attach optional proof to a transaction, with consent and access controls. Do not block a transaction if no image exists. |
| CSV export | P1 | Provide a user-requested export after privacy and access controls are tested. |
| Reminder cadence | P1 | Let users send neutral reminders; avoid harassment, public disclosure, or automatic threats. |
| Hindi + English UI | P0 | Use simple Hindi and simple English, tested with target users. Add more languages after pilot evidence; language design must not be hard-coded in layouts. |
| Odia and other regional languages | P1 | Add based on pilot user demand and reviewed translations. |

## 8. Core user flows

### Flow A — First-time setup and pairing

1. User installs the Android app and selects Hindi or English.
2. User verifies their phone number via OTP.
3. User enters a display name, chooses Retailer or Distributor Owner, and enters a business display name. No formal registration proof is required for pilot access.
4. Distributor owner may invite a field agent and define what that agent is allowed to do.
5. One side creates a pairing code/QR and shares it in person. The other side sees the name and phone suffix and accepts or declines.
6. Both parties see a new pair ledger in `Opening balance pending` state.
7. Either side proposes the opening balance, including which party owes the money. The other side confirms or disputes it.
8. Only after acceptance does the opening balance become the confirmed starting point.

**Acceptance criteria:** Pairing requires two distinct authenticated accounts; a pairing code is single-use and expires; neither side can be silently added to a relationship; an unconfirmed opening balance is not included in the confirmed balance.

### Flow B — Distributor records goods supplied

1. Distributor or authorized agent opens the correct retailer ledger.
2. Selects `Goods supplied / udhaar`, enters amount, date, and a short description or bill reference.
3. App shows a review screen with the retailer, amount, and expected effect on the balance.
4. On submit, status becomes `Awaiting retailer response`; it does not affect confirmed balance yet.
5. Retailer receives a push notification where available and sees the item in the request inbox.
6. Retailer reviews the exact details and chooses `Confirm`, `Dispute`, or `Reject`.
7. On confirmation, server records the decision and the item becomes part of the shared confirmed balance on both phones.
8. On dispute or rejection, the item stays visible in its own state and does not affect confirmed balance. The proposer can respond with a corrected proposal.

### Flow C — Retailer records a payment

1. Retailer opens the distributor's ledger and selects `Payment made`.
2. Enters amount, date/time, and method label (e.g. cash; this is a record, not a payment rail).
3. App shows the current confirmed balance and the proposed effect of this payment.
4. The request is sent to the distributor or an explicitly authorized collection agent.
5. When acknowledged, the transaction affects the confirmed balance and creates a receipt/reference within the app.
6. If the distributor/agent disagrees that payment was received, they can dispute or reject it with a reason. The app must not falsely label it as paid/confirmed before acknowledgement.

The reciprocal flow also exists: an authorized agent can record a collection and the retailer acknowledges the record. Each record must make clear who entered it and on whose behalf.

### Flow D — Correct a confirmed entry

1. User opens a confirmed transaction and taps `Request correction`.
2. App shows the original amount/details and allows a proposed replacement or reversal plus a reason.
3. Server stores the correction as a new request linked to the original. The original is never overwritten or deleted.
4. The counterparty sees original and proposed details side by side and chooses `Approve`, `Dispute`, or `Reject`.
5. If approved, the server adds the correction event to the ledger and recalculates the confirmed balance from the event history.
6. If disputed/rejected, the original confirmed event remains effective until a correction is approved or the parties otherwise resolve the matter. The disagreement stays visible.

### Flow E — Agent visit and collection

1. Distributor owner adds a field-agent account and assigns only the retailers that agent visits.
2. Agent opens an assigned ledger; unrelated retailer ledgers are not visible.
3. Agent records a delivery or collection and identifies themself as the actor.
4. If the owner has granted the required authority, the agent can acknowledge a payment on behalf of the distributor. This is shown explicitly as `Acknowledged by [agent] for [distributor]`.
5. The retailer still receives the entry to review and acknowledge. The agent cannot approve a retailer's action or edit a confirmed entry silently.
6. Distributor owner can review an activity log for their agents.

## 9. Transaction state model and business rules

### States

| State | Meaning | Included in confirmed balance? |
|---|---|---|
| Local draft | Saved only on the current device and not yet submitted to the server | No |
| Awaiting response | Submitted to the server and waiting for the counterparty | No |
| Confirmed | The required authorized counterparty acknowledged the exact transaction version | Yes |
| Disputed | A party explicitly contests the proposal or correction | No additional effect from the disputed proposal; prior confirmed records remain effective |
| Rejected | Counterparty declined the proposal | No |
| Correction pending | A correction request is linked to a confirmed record and awaiting a decision | Original remains effective; proposed correction has no effect |
| Corrected | A correction has been accepted and is included as a new event | The effective balance reflects the original plus approved correction events |
| Cancelled/withdrawn | Proposer withdraws an unanswered proposal, with an audit entry | No |

### Non-negotiable business rules

1. The backend is authoritative for shared status and confirmed balance.
2. Confirmed entries cannot be updated or deleted through ordinary app functions.
3. Corrections are append-only events that reference the original transaction.
4. A decision applies to the exact version shown to the reviewer. If details change, the previous request is invalidated and a new review is required.
5. A transaction does not become confirmed because an OTP was entered, a notification was delivered, or the creator says it is confirmed. It requires an authenticated, authorized acknowledgement recorded by the server.
6. Pending, rejected, and disputed proposals are not mixed into the confirmed balance. The UI must show them separately, without hiding them.
7. Opening balance is a proposal and needs mutual acknowledgement; migration from a paper register must not silently declare an amount accepted.
8. Each pair has one canonical balance with an unambiguous direction. A retailer's other distributor balances are not netted against this pair.
9. All money is represented in integer paise or an exact decimal type—never binary floating-point. Display uses Indian rupee formatting.
10. Every write request has a unique idempotency key so retries cannot accidentally duplicate transactions.
11. Server time, actor identity, role, decision, and original/proposed values are recorded for important events. Client-supplied timestamps may be retained as contextual data but cannot be the sole audit timestamp.
12. No automated logic can resolve a factual dispute. The app exposes the state and supports a transparent correction workflow.
13. App support staff cannot casually rewrite ledger events. Any exceptional administrative intervention requires restricted permissions and a separate audit log.
14. Pairing and agent permissions can be revoked. Revocation blocks future access but must not silently erase existing shared history.
15. Notification failure does not change transaction state; users can always open the request inbox to see pending items.

### Balance calculation

Use a canonical sign convention for each pair. For example, a confirmed goods-supplied entry increases the retailer's payable to the distributor; a confirmed payment received decreases it; a confirmed return or credit adjustment decreases it according to the agreed reason. The app should explain the effect in plain language before submission. Balance calculation must run on the server from accepted events, not from mutable values supplied by the client.

## 10. Permissions and roles

| Capability | Retailer | Distributor owner | Assigned agent |
|---|---:|---:|---:|
| View own pair ledger | Yes | Yes, for their retailers | Only assigned retailer ledgers |
| Propose a goods/credit entry | Yes, if the relationship allows it | Yes | Yes, for assigned retailers if granted |
| Propose a payment/collection record | Yes | Yes | Yes, for assigned retailers if granted |
| Confirm own proposal | No | No | No |
| Acknowledge on behalf of counterparty | No | Yes, for distributor role | Only with explicit owner-granted authority; event shows agent identity |
| Approve an entry on behalf of retailer | No | No | No |
| Edit/delete confirmed event | No | No | No |
| Propose correction | Yes | Yes | If granted for assigned relationships |
| Manage distributor agents | No | Yes | No |
| View audit trail for own side | Yes | Yes | Limited to own actions and assigned relationships |

No role can approve its own proposal. The system must enforce permissions on the server, not merely hide buttons in the UI.

## 11. UX and content requirements

### Interface principles

- Native Android app built with Kotlin and Jetpack Compose.
- Primary navigation should be small: `Khate`, `Requests`, `Activity`, `Profile`.
- Home screen should put the most useful action first: pending approvals, recently used ledgers, and a clear add/record action.
- A ledger screen should prioritise: counterparty name, clear `You will pay` / `You will receive` amount, pending amount/count, and recent history.
- Avoid accounting jargon where a common phrase is possible. Use verbs like `Maal diya`, `Payment darj karein`, `Confirm karein`, `Galat hai / Dispute`, and `Correction maangein` after careful native-speaker review.
- Show status with both label and icon; never rely on colour alone.
- Keep forms short and use numeric keypad for amounts. Ask for a reason only when it affects the record, such as a correction or dispute.
- Use a plain confirmation screen before critical actions. Do not put `Approve` beside `Reject` in a way that makes accidental taps likely.
- Minimum touch targets should meet Android accessibility guidance (at least 48 dp for interactive elements); test text scaling, contrast, TalkBack, and low-end screens [8].
- Hindi and English translations must be stored as localised resources; do not hard-code UI text inside screen logic [9].
- No advertising on financial-record screens in the MVP. Ads would reduce perceived trust and distract from the core task.

### Key UI status copy

- **Draft:** `Abhi sirf is phone par saved hai. Doosri party ko nahi bheja gaya.`
- **Awaiting response:** `Request bhej di gayi hai. Abhi confirmed hisaab mein shamil nahi hai.`
- **Confirmed:** `Dono taraf se confirm.`
- **Disputed:** `Is entry par disagreement hai. Confirmed balance nahi badla.`
- **Correction pending:** `Purana record abhi valid hai; correction approval ka wait kar rahi hai.`

Exact Hindi wording is provisional and should be tested with actual pilot users from the chosen region.

## 12. Technical direction (recommendation, not an implementation lock)

### Android client

- **Language/UI:** Kotlin + Jetpack Compose.
- **Architecture:** UI layer, ViewModels, domain/business rules, repositories, and data sources separated so money-related rules can be unit tested independently of screens. This aligns with Android's recommended data-layer separation [10].
- **Local persistence:** Room for cached confirmed events, local drafts, and a queue of unsynced operations.
- **Networking:** typed API client over HTTPS; retry transient failures with backoff and idempotency keys.
- **Background work:** WorkManager for safe retry/sync of eligible uploads. Never silently auto-approve a transaction during a background retry.
- **Notifications:** Firebase Cloud Messaging or equivalent push transport, backed by a server-side inbox so missed notifications are recoverable.
- **Secure local handling:** keep auth/session secrets out of plain preferences; use Android Keystore-backed mechanisms where suitable. Follow Android's security guidance and never embed server secrets in the app [7].

### Backend

Use a server-authoritative API and a relational database such as PostgreSQL. Final provider choice should consider operational cost, India-region availability, data handling, backup, and maintainability. Important business rules must run server-side inside reliable transactions—not only in Kotlin code or client-side database policies.

Suggested logical components:

1. Authentication and user/device sessions.
2. Business, membership, and role/permission service.
3. Pairing/invitation service.
4. Ledger relationship and transaction proposal service.
5. Server-side decision/approval service.
6. Balance projector that derives the confirmed balance from accepted events.
7. Notification and activity-inbox service.
8. Audit logging, admin access review, monitoring, backup, and restore procedures.

### Suggested core data entities

| Entity | Purpose / minimum fields |
|---|---|
| `users` | User ID, verified phone reference, display name, preferred locale, status, created/updated timestamps |
| `businesses` | Business ID, display name, business type optional, creator/owner, status; no registration ID required |
| `business_memberships` | User, business, role, assigned scope, permissions, status |
| `ledger_relationships` | Pair ID, retailer business/user, distributor business, status, paired-at timestamp |
| `ledger_events` | Event ID, pair ID, type, amount in paise, description/reference, actor, actor role, created server time, source entry, effective state |
| `entry_proposals` | Proposal ID, pair ID, proposed event payload/version, proposer, current state, idempotency key, created server time |
| `decisions` | Proposal/version, decision, reviewer, reviewer role, reason, server timestamp |
| `correction_links` | Original event ID, correction proposal ID, reason, status |
| `audit_events` | Actor, action, target, before/after summary or payload hash, server timestamp, request ID |
| `device_sync_queue` | Local-only unsynced draft IDs and retry metadata; never treated as confirmed server state |
| `attachments` (P1) | Object reference, linked event/proposal, uploader, consent/access metadata, content type and size |

This is a conceptual model. The actual schema, deletion/retention rules, and access controls need review before production.

### API behaviour required by the PRD

- `POST /pairings` creates a short-lived invitation.
- `POST /pairings/{id}/accept` validates the other party and activates the relationship.
- `POST /ledgers/{id}/proposals` creates a pending entry with idempotency protection.
- `POST /proposals/{id}/decisions` accepts, disputes, or rejects a specific immutable proposal version.
- `POST /events/{id}/corrections` creates a correction request; it never mutates the source event.
- `GET /ledgers/{id}` returns confirmed balance, confirmed events, pending proposals, disputes, and sync metadata separately.
- `GET /activity` returns a permission-filtered audit/activity feed.

Endpoints are examples, not a locked API contract.

### Offline and synchronization contract

- Read from local cache immediately where available; indicate its last sync time.
- Drafts may be created offline and must persist across app restarts.
- On reconnect, upload the draft with a unique idempotency key and show `Sending` until the server returns a real proposal ID.
- A server-accepted proposal becomes `Awaiting response`; it is still not confirmed.
- A decision is final in the UI only after a server acknowledgement.
- Never use last-write-wins for money, approvals, or correction events.
- If a device has stale data, refresh and show the new version before the user approves a sensitive action.
- No silent conflict resolution. If the server detects a version mismatch, ask the user to review the current record and submit a new proposal.

## 13. Security, privacy, and compliance requirements

Mutual Khata deals with personal identifiers and potentially sensitive business/financial records. “Trustworthy” must mean precise claims about what the app does and well-designed controls—not a promise that data can never be breached or disputed.

### Security requirements

- Enforce object-level access checks on every backend request: a user can access only relationships and business records they are authorised to view.
- Use TLS/HTTPS for all network requests; avoid hardcoded service secrets in the Android client; protect sensitive local keys using Android platform facilities where appropriate [7].
- Store the smallest amount of personal information needed for pairing and support.
- Use rate limits and abuse detection for OTP, invitations, and repeated approval attempts.
- Bind each decision to the exact proposal ID/version and authenticated actor; prevent replay and self-approval.
- Log security-relevant administrative actions and restrict production database access.
- Create tested backups and restore procedures. A backup is useful only if it can actually be restored.
- Protect receipt attachments with private storage and authorisation checks if/when launched.
- Avoid exposing balances or transaction details in lock-screen notification previews by default; allow a user to control preview detail.
- Create a security incident and account-recovery process before public launch.

### Privacy and legal readiness

- Provide a plain-language privacy notice describing what data is collected, why, who can see it, how long it is kept, how to request deletion, and how to contact support.
- Do not collect GSTIN, Aadhaar, bank credentials, contacts, precise location, or phone-call/SMS history by default.
- Request only permissions tied to a feature. Camera/storage permissions should not be required for core ledger usage.
- Provide account deletion request paths inside the app and through an external resource. Google Play's current policy requires both an in-app deletion path and an external web resource for apps that enable account creation [6]. This is a narrow compliance/support surface, not a user-facing website/PWA.
- Define a legally reviewed retention/deletion policy for shared records, audit events, attachments, and account identifiers. Where any records must be retained or anonymised rather than erased, explain the reason and scope transparently; do not invent a retention rule without legal review.
- The Digital Personal Data Protection Rules, 2025 specify phased commencement dates [11]. Because the rules' timeline is staged, obtain India-specific legal review before public launch and verify current operative provisions then. Design for clear notice, consent, user access, correction, and deletion from the start.
- This app is a record-keeping product, not a bank, payment service, lender, tax advisor, or source of legally conclusive evidence. Product copy must not claim otherwise.

### Google Play release check

As of the research snapshot, Google Play's published policy says new apps and updates must target Android 16 / API level 36 or higher starting 31 August 2026, subject to listed platform exceptions and possible extension [12]. Re-check current policy when preparing the release build; set target API and SDK versions to the then-current requirement.

## 14. Non-functional requirements and quality bar

| Area | MVP requirement / proposed target |
|---|---|
| Correctness | No unacknowledged or disputed proposal changes the confirmed balance. Corrections preserve the source event. |
| Reliability | Requests can retry safely without duplicate entries; the user sees a clear state if sync fails. |
| Performance | On the agreed pilot device, cached ledger screen should feel immediate; target p95 under 2 seconds for a cached ledger render and under 3 seconds for a typical online proposal response on a healthy connection. Measure rather than assume. |
| Resilience | App restarts do not lose a saved draft. Sync retries survive app closure. |
| Low data use | Fetch changes incrementally; do not download all history on each screen visit. |
| Accessibility | At least 48 dp touch targets, sufficient contrast, support larger text, accessible control labels, and tests with TalkBack [8]. |
| Language | Hindi and English in P0, with an architecture that can add regional languages without code rewrites [9]. |
| Compatibility | Agree a minimum Android version after checking pilot-device distribution; test at least one low-memory, lower-cost phone and a recent Android phone. |
| Observability | Track API errors, sync failures, duplicate prevention, pending age, decision latency, and crash rate without logging full sensitive transaction contents unnecessarily. |
| Security | Automated server-side authorization tests, dependency updates, secret scanning, and audit log tests before external pilot. |
| Maintainability | Versioned API/schema migrations, environment separation, repeatable builds, and documented backup/restore process. |

## 15. Analytics and definition of success

Downloads are not the success metric. The meaningful unit is an active retailer–distributor pair, because the product only delivers its main value when both sides participate.

### North-star metric

**Weekly active mutual ledgers:** number of unique retailer–distributor pairs with at least one server-confirmed transaction in the previous seven days.

### Pilot metrics

| Metric | Definition | Initial hypothesis/guardrail |
|---|---|---|
| Pair activation | A linked pair where both users join and acknowledge an opening balance or first transaction | At least 70% of pilot pairs complete setup; investigate every major drop-off. |
| Confirmation speed | Median and p90 time from server proposal creation to counterparty decision | Target median under 24 hours for straightforward entries. |
| Confirmation rate | Share of proposals confirmed within 24 hours | Initial target 80%; interpret alongside disputes and abandonment, not alone. |
| Dispute rate | Share of proposals marked disputed or rejected | No arbitrary “lower is always better” target; a healthy app must allow legitimate disagreements to surface. Monitor themes and resolution. |
| Repeat usage | Share of activated pairs with confirmed activity in each subsequent week | Initial target at least 60% in weeks 2–4 of the pilot. |
| Task completion | Time and success rate for recording a simple payment in a moderated test | Target: most participants complete without help in under 60 seconds. Validate with real users. |
| Sync integrity | Duplicates, lost drafts, mismatched balances, decisions without server acknowledgement | Zero known balance-integrity failures in pilot; any such failure blocks expansion. |
| Trust comprehension | Can a user explain the difference between pending and confirmed? | At least 8 of 10 test users can explain after a short onboarding. |

These targets are hypotheses for a small learning pilot, not forecasts or commitments. Analytics should be privacy-minimised; use pseudonymous pair IDs and avoid storing full ledger details in analytics events.

## 16. Pilot and validation plan

### Stage 0 — Problem interviews (before polishing UI)

Interview 5–8 retailers and 3–5 distributor owners/agents. Ask them to walk through the most recent disputed transaction, how each side recorded it, what proof existed, who entered the number, how long reconciliation took, and what would prevent them from using a shared app. Observe the workflow where possible; do not lead with the solution.

### Stage 1 — Prototype/usability test

Test the primary flows with 5–8 target users: pair accounts, confirm opening balance, record goods supplied, record payment, dispute an incorrect value, and correct a previously confirmed entry. Include at least one user with low confidence using apps. Watch for incorrect mental models around status and balance.

### Stage 2 — Closed pilot

- Start with 5–10 retailer–distributor pairs in one area.
- Include at least one distributor with a field agent.
- Keep support direct and manual; do not add large-scale growth mechanisms yet.
- Record baseline reconciliation time and common dispute causes before launch where possible.
- Review usage weekly and interview both sides, including pairs that stop using the app.
- Do not use live records as the only recordkeeping system until backup, export, recovery, and support procedures have been tested. Encourage users to retain normal business records during the pilot.

### Stage 3 — Expansion gate

Proceed beyond the pilot only when: (a) no critical balance-integrity defects remain; (b) users understand pending vs confirmed; (c) most active pairs complete a real transaction cycle without founder intervention; (d) security/privacy basics and data deletion are reviewed; and (e) there is evidence that users will keep using the product after the novelty period.

## 17. Risks and mitigations

| Risk | Why it matters | Mitigation |
|---|---|---|
| Counterparty does not install/respond | One-sided usage offers little mutual trust | Make pairing extremely simple; show pending requests; interview non-responders; let both parties start the link. |
| Confirmation friction is too high | Every entry may feel slower than a notebook | Test a short confirmation screen; consider batch confirmation only after validating risks, never as a silent default. |
| A party falsely confirms an event | Software cannot verify the physical world | Show exact details before approval, preserve acknowledgements and evidence, and avoid claiming that confirmed means objectively true. |
| Agent misuse or account sharing | Agent may enter or acknowledge incorrect collections | Individual agent accounts, explicit assigned scope, delegated permissions, visible agent identity, revocation, and owner audit view. |
| Incorrect opening balance | Trust fails before regular usage begins | Require mutual confirmation and clearly mark unresolved opening balances. |
| Poor connectivity | Users may assume their entry reached the other phone | Durable local drafts, explicit sync labels, idempotency, no offline final approval, and a persistent request inbox. |
| Accidental duplicates | Retry taps or network failure can distort balances | Idempotency keys, server uniqueness, clear pending state, and duplicate tests. |
| One party refuses correction | Dispute remains unresolved | Keep current confirmed record effective; show dispute and correction history; support human conversation without pretending to adjudicate. |
| App or device loss | Business records may become inaccessible | Server backup, account recovery, restore testing, and later statement export. |
| Overbuilding before validation | Complexity delays learning | Restrict MVP to paired ledger, acknowledgement, corrections, roles, and safe sync. Defer inventory, AI, OCR, and payments. |
| Name is descriptive or conflicts with existing marks | Rebranding/legal risk after adoption | Treat “Mutual Khata” as a working name; search exact and similar marks and get professional clearance before launch [13]. |
| Legal or Play policy changes | Release may be blocked or require product changes | Re-check DPDP, deletion, privacy, and Play requirements immediately before external launch [6, 11, 12]. |

## 18. Roadmap

### Milestone 1 — Product proof (first meaningful build)

- Auth/profile and Hindi/English language selection.
- Pairing and mutual opening-balance confirmation.
- Shared ledger and simple credit/payment records.
- Transaction proposal, counterparty decision, correction request, and audit feed.
- Distributor owner/agent roles and permissions.
- Server-authoritative balance, local cache, draft queue, in-app request inbox.
- Internal QA for permissions, retries, duplicate submission, and balances.

### Milestone 2 — Pilot hardening

- Basic statement share/export.
- Optional receipt/bill image attachments if user interviews show real value.
- Better dispute reason capture and activity filters.
- Account recovery, deletion workflow, privacy-support resources, monitoring and restore test.
- Accessibility and low-end Android device testing.

### Milestone 3 — Evidence-driven improvements

Only after pilot evidence: local language expansion (including Odia if demanded), optional voice input, OCR-assisted receipt capture, batch review for trusted contexts, and wider distributor-agent workflows. These must remain assistive; no model should automatically confirm an amount or resolve a financial dispute.

### Later, not promised

iOS, broader accounting, inventory, payments integration, retailer analytics, integrations, and expansion across regions. Each requires a separate product and legal review.

## 19. Open decisions for product discussion

These questions are deliberately surfaced rather than quietly assumed. The defaults below keep the first release safe and small.

1. **Must every transaction require counterparty acknowledgement?** Default: yes for the MVP. Do not add silent “trusted auto-confirm” mode before pilot evidence.
2. **Can a field agent acknowledge money received on behalf of the distributor?** Default: only if the owner explicitly grants that permission; the receipt clearly identifies the agent and delegation.
3. **Should goods and payment entries be confirmed independently?** Default: yes. A payment does not automatically confirm a disputed invoice, and an invoice does not imply payment.
4. **What happens when one party does not respond?** Default: leave it pending, show reminders and age; never auto-confirm or silently expire it into the balance.
5. **What is the minimum Android version?** Default: decide after sampling target devices; optimise and test low-cost hardware rather than choosing solely by developer convenience.
6. **What backend/provider should be used?** Default: choose a relational, server-authoritative system with strong transactional writes, access controls, backups, and acceptable operational cost. Evaluate region/data-handling before selecting a provider.
7. **How should historical records be handled after account deletion?** Default: do not launch publicly until retention, counterparty visibility, anonymisation/deletion and legal requirements are reviewed and clearly described.
8. **Is “Mutual Khata” a safe brand to launch?** Default: working name only until exact/phonetic trademark search, app-store/name/domain search, and qualified legal clearance are complete.

## 20. Definition of Done for the MVP

The MVP is not “done” when the screens look complete. The core release is ready for a closed pilot when all items below are true:

- Two separate Android accounts can pair and see the same relationship.
- A proposed opening balance remains pending until the other side acts.
- Both parties can create and acknowledge goods/payment/return entries.
- Pending, confirmed, disputed and rejected records are visually and numerically distinct.
- A confirmed record cannot be edited or deleted in the normal application flow.
- A correction creates a linked event and requires counterparty approval.
- Server-side tests prove users cannot access unrelated ledgers or approve their own proposals.
- Repeated network retries do not create duplicate entries.
- An offline draft survives app restart and is clearly labelled unsynced until accepted by the server.
- Field-agent permissions restrict access and show the actual actor in history.
- Balance calculation passes deterministic tests for supply, payment, returns, corrections, rejection, and dispute scenarios.
- Hindi/English strings, touch targets, contrast, large text, TalkBack basics, and a lower-cost Android phone have been tested.
- Privacy notice, account-deletion path, support contact, backup/restore, incident response and Play release requirements have owners and have been reviewed.
- Pilot users can explain what “confirmed” and “pending” mean and complete the main task without the developer operating the phone for them.

## 21. References and research notes

**[1]** Google Play listing: DeLe: Khata Book & Udhar Hisab. Public listing describes a “Shared Mutual Khata.” Checked 9 October 2026. https://play.google.com/store/apps/details?id=com.deleapp.khata

**[2]** Google Play listing: OkCredit: Ledger, UPI, Billing. Public listing describes ledgers, customer/supplier balances and related business features. Checked 9 October 2026. https://play.google.com/store/apps/details?id=in.okcredit.merchant

**[3]** Google Play listing: Khatabook (public listing). Checked 9 October 2026. https://play.google.com/store/apps/details?id=com.vaibhavkalpe.android.khatabook

**[4]** Google Play listing: Vyapar Invoice GST Billing App. Public listing describes invoicing, inventory, accounting and related workflows. Checked 9 October 2026. https://play.google.com/store/apps/details?id=in.android.vyapar

**[5]** Android Developers, “Build an offline-first app.” Guidance on local data sources, synchronisation and conflict handling. https://developer.android.com/topic/architecture/data-layer/offline-first

**[6]** Google Play Console Help, “User Data” / account deletion requirement. https://support.google.com/googleplay/android-developer/answer/10144311

**[7]** Android Developers, “Security checklist.” Guidance includes HTTPS, safe API-key handling, and key management. https://developer.android.com/privacy-and-security/security-tips

**[8]** Android Developers, “Make apps more accessible.” Guidance includes at least 48 dp touch targets and control descriptions. https://developer.android.com/guide/topics/ui/accessibility/apps

**[9]** Android Developers, “Localize your app.” https://developer.android.com/guide/topics/resources/localization

**[10]** Android Developers, “Data layer.” Guidance on separating UI and data/business rules. https://developer.android.com/topic/architecture/data-layer

**[11]** Ministry of Electronics and Information Technology, Government of India, Digital Personal Data Protection Rules, 2025, notified on 14 November 2025; verify applicable commencement dates and obligations with qualified counsel before launch. https://www.meity.gov.in/static/uploads/2025/11/53450e6e5dc0bfa85ebd78686cadad39.pdf

**[12]** Google Play Console Help, target API level requirements. Policy checked 9 October 2026; verify again before release. https://support.google.com/googleplay/android-developer/answer/11926878

**[13]** IP India, Basics of Trademarks and official public search. These sources explain distinctiveness/conflict considerations; this PRD does not claim that a clearance search or legal opinion has been completed. https://ipindia.gov.in/basics-of-trademarks and https://tmrsearch.ipindia.gov.in/ESEARCH

---

**End of PRD v0.1.** This document is the working product specification for discussion and pilot planning. Product claims, legal requirements, competitor status, API policies, and provider choices must be re-verified at the point of public release.
