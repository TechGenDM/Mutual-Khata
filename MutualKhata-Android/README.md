# Mutual Khata — Android-first starter

**Product promise:** Dono ka hisaab. Dono ki tasdeeq.

This is a native Android Studio project (Kotlin + Jetpack Compose), not a website or PWA. It contains a clickable UI prototype for a low-friction retailer/distributor khata flow.

## Current status: prototype, not production MVP yet

The current app uses sample data held in memory. It demonstrates navigation, sample ledgers, entry submission, pending-request states, and simulated approval/rejection on one device. Those demo buttons do **not** contact another phone or alter a real shared ledger. No phone number, bank account, payment rail, or live backend is connected.

The real mutual-khata MVP is complete only when two separately authenticated devices use the same server-backed relationship, and the server—not the app UI—enforces approval, permissions, audit history, and immutable confirmed entries.

## Open in Android Studio

1. Unzip this folder.
2. Open the `MutualKhata-Android` folder in Android Studio.
3. Use JDK 17 and install Android SDK Platform 35 if prompted.
4. Let Gradle sync, then run the `app` configuration on an emulator or Android phone.

The included `gradlew` bootstrap script downloads Gradle 8.9 on first use (internet required). Android Studio may also download the configured Gradle version during sync. Use JDK 17.

## MVP scope (first field pilot)

- Two parties: retailer and distributor; a distributor agent can be added as an authorized team member in the next iteration.
- Link a retailer and distributor by invite/QR, with the other party explicitly accepting.
- Record goods received, payments, and returns.
- Every newly created entry starts as **Pending**; it must not affect the confirmed balance until the counterparty approves it.
- After confirmation, don't edit or delete the original row. A correction is a separate request with a reason and proposed change; after approval, append a correction/adjustment and preserve the original history.
- Show confirmed, pending, and disputed items separately.
- Keep an offline draft, then sync to the server when online. An offline-only draft is never represented as mutually confirmed.
- Weekly statement and export after the core workflow is proven.

## Proposed production architecture

- **Android:** Kotlin + Jetpack Compose, Room for local drafts/cache, Android Keystore-backed token storage.
- **Backend:** PostgreSQL with authenticated API (Supabase is a candidate). Enforce authorization and append-only audit behaviour on the server using row-level security and server-side functions.
- **Notifications:** in-app first; SMS/WhatsApp only after consent, cost review, and reliability testing.
- **Security:** phone verification, per-business roles, least-privilege agent access, explicit privacy notice, server-side audit logs, encrypted transport, backup/recovery plan, and account deletion/export paths.

`backend/schema.sql` is a **design starting point** for review, not a production-ready security implementation. Partner-invite acceptance, full RLS policy review, rate limiting, migrations, monitoring, and security testing must be finished before real merchant data is used.

## Important product rules

1. A correct OTP proves control of a device/session; it does not prove that an invoice or payment amount is true.
2. Approval is tied to the exact transaction and proposed change—not a reusable generic code.
3. Both parties must be able to dispute a transaction and keep the original history.
4. The app should use simple local language, large touch targets, low-data screens, and avoid requiring a desktop.
5. Don't call this a legally certified ledger or guarantee that both entries are truthful. It records who entered and approved what, and when.

## Name status

“Mutual Khata” is a working project name only. An existing Google Play app lists a feature called “Shared Mutual Khata”, so do a full Indian trademark clearance (including similar spellings/phonetics and relevant classes) before launch or paying for branding. This repository does not provide legal clearance.
