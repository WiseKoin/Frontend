# WiseKoin privacy and data-flow audit

**Audit date:** 2026-10-26  
**Repositories reviewed:** Android client (`D:\AndroidStudioProjects\WiseKoin`) and Spring Boot backend (`D:\Projects\WiseKoin`)  
**Scope:** executable source, manifests, build/dependency files, database entities and migrations, deployment examples, and user-facing controls. Current third-party terms were checked separately where code could not establish provider behavior.

## Citation convention and limits

Android links are relative to this legal folder. Backend links cross into the separate backend repository. Line numbers describe the audited revision and may move as code changes. Negative findings are revision-specific: repository-wide searches checked permissions, dependencies, models, API routes, and UI for advertising, analytics, location, contacts, device identifiers, age/date-of-birth, consent records, privacy requests, and export/download features.

This is an engineering inventory, not a legal conclusion. Production infrastructure, contracts, staff practices, backups, support procedures, and disclosures outside the repositories must be confirmed by the operator.

## Executive findings

- WiseKoin processes account identifiers and contact details, authentication/security records, detailed user-entered financial records, receipt images, voice recordings, structured finance-app notification data, AI-derived results and learning signals, Help Center feedback, and technical diagnostics.
- The backend stores account and product records in PostgreSQL. The production example points to Neon Cloud, but code cannot prove which database or hosting vendor is actually used in production.
- Receipt images and voice recordings are temporarily held in Android cache or streamed from a selected content URI. The backend reads them into memory and does not persist the source media, but it transmits the media and contextual account data to Google Gemini. Structured extraction results remain in PostgreSQL.
- Optional notification tracking transiently reads notification text from four allowlisted finance apps on-device. It persists and uploads structured transaction facts and a one-way reference hash, not the original text. The local queue is encrypted, account-scoped, capped at 100, and pruned after 14 days.
- Sentry is initialized for both the Android app and backend for errors and performance traces. The Help Center sends feedback text and optional name/email directly to Sentry. Automatic request/user/body/header/query collection, screenshots, view hierarchy, and Logcat capture are disabled in code, but Sentry can still receive crash stacks, performance spans, app/device/runtime metadata, and network metadata such as IP address.
- No advertising, behavioral-marketing, general product-analytics, location, contacts, phone-state, advertising-ID, Firebase, payment, or social SDK was found.
- Users can update profile data, delete many individual records, disable notification tracking, clear local notification candidates, revoke Android permissions, and delete an account after password and email-OTP verification. There is no automated data export, privacy-request intake, Sentry opt-out, age gate, or Terms/Privacy acceptance record.
- The most serious publication dependencies are unconfirmed processor terms and retention, a missing age gate while Gemini requires API clients not to be likely accessed by under-18s, incomplete provider/back-up deletion procedures, and remote placeholder images hosted by Google and Unsplash.

## 1. Data inventory

### A. Account and profile data

Registration requires username, email, and password and accepts an optional phone number ([backend registration DTO, lines 10-20](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/auth/dto/RegisterRequest.java#L10)). The user table stores email, a password hash, username, phone, and base currency ([backend user entity, lines 18-31](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/user/User.java#L18)). Registration also creates a default wallet and default categories ([backend auth service, lines 35-51](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/auth/AuthService.java#L35)).

The app does not collect date of birth, government ID, bank-login credentials, card credentials, precise location, contacts, or address-book data. No corresponding fields, permissions, or SDKs were found.

### B. Authentication and security data

- Access and refresh tokens are issued for stateless JWT authentication. Default lifetimes are 15 minutes and 30 days respectively ([backend configuration, lines 29-31](../../../Projects/WiseKoin/src/main/resources/application.properties#L29)).
- The server stores only a SHA-256 hash of each refresh token plus user link and creation/expiry timestamps; rotation deletes the prior token ([refresh-token entity, lines 29-45](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/auth/token/RefreshToken.java#L29), [refresh-token service, lines 28-73](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/auth/token/RefreshTokenService.java#L28)).
- Passwords are encoded before the user is created ([auth service, lines 41-46](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/auth/AuthService.java#L41)).
- Password recovery, password change, and account deletion use emailed one-time codes. The server stores a password-encoded code, purpose, expiry, resend/attempt counters, verification-token hash, and timestamps ([OTP entity, lines 28-69](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/user/otp/OtpChallenge.java#L28)). Defaults are a 10-minute code, a 10-minute verification token, a 60-second resend delay, three sends per hour, and five attempts ([backend configuration, lines 90-106](../../../Projects/WiseKoin/src/main/resources/application.properties#L90)).
- OTP emails disclose the recipient address, security-code purpose/content, and expiry to the configured SMTP service ([email delivery service, lines 23-45](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/user/otp/EmailOtpDeliveryService.java#L23)).
- Anonymous API rate limiting keys requests by client IP; authenticated requests are keyed by internal user ID ([rate-limit filter, lines 59-110](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/config/ratelimit/RateLimitFilter.java#L59)). Counters are held only in the server process and stale windows are periodically removed ([rate-limit service, lines 9-49](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/config/ratelimit/RateLimitService.java#L9)).

On Android, raw access and refresh tokens are encrypted with AES-GCM using an Android Keystore key and stored in private SharedPreferences; corrupt/undecryptable data causes the tokens to be cleared ([Android token manager, lines 22-116](../app/src/main/kotlin/com/wisekoin/data/local/TokenManager.kt#L22)). Token preferences are excluded from cloud backup and device transfer ([Android extraction rules, lines 6-14](../app/src/main/res/xml/data_extraction_rules.xml#L6)).

### C. Financial and planning records

The backend stores detailed personal-finance records, including:

- wallet name, type, and balance ([wallet entity, lines 28-51](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/wallet/Wallet.java#L28));
- category name, type, and icon ([category entity, lines 28-51](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/category/Category.java#L28));
- transaction amount, type, note, date, source/destination wallets, category, goal/debt links, and idempotency hashes ([transaction entity, lines 30-82](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/transaction/Transaction.java#L30));
- recurring amount, note, day/schedule, wallet/category, active state, and dates ([recurring-transaction entity, lines 33-66](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/recurring/RecurringTransaction.java#L33));
- goal name, target/current amount, deadline, and status ([goal entity, lines 26-53](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/goal/Goal.java#L26)); and
- debt counterparty name, borrowed/lent type, total/remaining amount, due date, status, and note ([debt entity, lines 26-66](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/debt/Debt.java#L26)).

These are user-entered ledger records. No bank account linking or bank-credential collection was found.

### D. Receipt images and voice recordings

The Android app requests camera access only when opening receipt scanning and offers the Android photo picker as an alternative ([receipt screen, lines 55-104](../app/src/main/kotlin/com/wisekoin/ui/screens/ScanReceiptScreen.kt#L55)). Camera images are written to the app cache with a `Receipt_` filename ([receipt capture, lines 396-417](../app/src/main/kotlin/com/wisekoin/ui/screens/ScanReceiptScreen.kt#L396)). Gallery files are streamed from the selected content URI; the app does not copy the selected gallery file into its own persistent storage ([file utilities, lines 14-38 and 50-64](../app/src/main/kotlin/com/wisekoin/util/FileUtils.kt#L14)). App-created cached receipt files are deleted after the upload callback or a capture failure ([receipt screen, lines 94-103](../app/src/main/kotlin/com/wisekoin/ui/screens/ScanReceiptScreen.kt#L94), [file deletion, lines 41-47](../app/src/main/kotlin/com/wisekoin/util/FileUtils.kt#L41)).

Voice input requires microphone permission. Recordings are written to a temporary `.m4a` file in app cache, limited to 55 seconds client-side, uploaded as audio, and deleted after processing, reset, or ViewModel destruction ([voice ViewModel, lines 125-145, 198-249 and 428-446](../app/src/main/kotlin/com/wisekoin/viewmodel/VoiceViewModel.kt#L125)).

The backend validates type, size, dimensions, and duration, reads the media into an in-memory base64 payload, and derives a SHA-256 fingerprint ([media ingestion, lines 60-81 and 272-295](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/ai/media/AiMediaIngestionService.java#L60)). No backend entity or filesystem path stores the original media. Configuration explicitly caps receipt and audio uploads at 5 MiB by default and voice duration at 60 seconds ([backend configuration, lines 49-58](../../../Projects/WiseKoin/src/main/resources/application.properties#L49)).

### E. AI prompts, outputs, and learning data

For receipts, WiseKoin sends Google Gemini the source image plus a prompt containing current date/time, locale, time zone, base currency, category names/IDs, and learned merchant categorization rules ([receipt service, lines 78-130](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/receipt/ReceiptService.java#L78)). For voice, it sends the recording plus those details and wallet names/IDs ([voice service, lines 80-136](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/voice/VoiceService.java#L80)). The Gemini client sends the prompt and base64 media to the configured Google endpoint using an API key ([Gemini client, lines 101-133](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/ai/provider/GeminiAiExtractionClient.java#L101)).

The backend stores structured AI data linked to the user: source, merchant key/label, proposed category, amount, currency, date, wallet IDs, ambiguity and confidence information, locale/time zone, provider/model/prompt versions, suggestion status, rejection/confirmation timestamps, parser/reference hashes, and resulting transaction ID ([AI extraction entity, lines 26-132](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/ai/adaptive/AiExtraction.java#L26)). It also stores confirmation/rejection actions and remembered merchant categorization rules ([AI feedback, lines 25-56](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/ai/adaptive/AiFeedback.java#L25), [categorization rule, lines 31-57](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/ai/adaptive/CategorizationRule.java#L31)).

Separately, WiseKoin stores a media fingerprint, hashed idempotency key, source, processing status, and trace IDs for deduplication ([ingestion request entity, lines 25-56](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/ai/ingestion/AiIngestionRequest.java#L25)). That minimal ingestion metadata is removed after 30 days by default ([ingestion cleanup, lines 30-46 and 110-114](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/ai/ingestion/AiIngestionRequestService.java#L30)). Structured extractions, feedback, and learned rules have no time-based cleanup in code and remain until account deletion or an operator-side process.

### F. Finance-app notifications

Notification access is optional and must be granted through Android settings. A separate in-app switch controls parsing; the app automatically disables tracking if OS access is removed ([notification screen, lines 90-113 and 156-180](../app/src/main/kotlin/com/wisekoin/ui/screens/BankNotificationsScreen.kt#L90)). The listener service is not exported to other apps and is protected by Android's notification-listener binding permission ([Android manifest, lines 83-97](../app/src/main/AndroidManifest.xml#L83)).

The current parser accepts package names for VCB Digibank, MB Bank, Techcombank Mobile, and MoMo only ([parser registry, lines 30-81](../app/src/main/kotlin/com/wisekoin/notification/BankNotificationParserRegistry.kt#L30)). For an allowlisted notification, the listener transiently reads title, text, big text, subtext, and text lines, then passes them to a local parser ([listener, lines 9-47](../app/src/main/kotlin/com/wisekoin/notification/BankNotificationListenerService.kt#L9)). The original text is not a field in the persisted candidate. Persisted candidates contain source/parser IDs, amount, currency, direction, source package, occurred/captured times, a hashed reference, and optional suggestion ID ([candidate model, lines 10-30](../app/src/main/kotlin/com/wisekoin/data/model/BankNotificationCandidate.kt#L10)).

The local candidate state is AES-GCM encrypted with an Android Keystore key, scoped to a hash of the signed-in account, and cleared/disabled when the bound account changes ([notification store, lines 65-101 and 190-269](../app/src/main/kotlin/com/wisekoin/data/local/BankNotificationStore.kt#L65)). Candidate retention is 14 days with a 100-item maximum ([retention rules, lines 6-30](../app/src/main/kotlin/com/wisekoin/data/local/BankNotificationRetention.kt#L6)). The encrypted preference is excluded from Android cloud backup and device transfer ([backup rules, lines 8-10](../app/src/main/res/xml/backup_rules.xml#L8)).

The backend receives structured facts—source, reference hash, parser version, amount, currency, direction, event time, locale, and time zone—and aggregate parser counts; it does not receive original notification text through this path ([Android syncer, lines 17-60](../app/src/main/kotlin/com/wisekoin/notification/BankNotificationSyncer.kt#L17)). Server-side notification metrics are linked to the user and include daily observed, parsed, failure, duplicate, upload, confirmation, and rejection counts ([metric entity, lines 26-76](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/ai/notification/NotificationPilotMetric.java#L26)). No time-based deletion job exists for server metrics; the 30-day limit in code restricts report ingestion/query range, not database retention ([metric service, lines 23-54 and 102-117](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/ai/notification/NotificationPilotMetricService.java#L23)).

### G. Help Center feedback

Feedback consists of a bug/idea/question category, message, and optional name and email ([feedback model, lines 3-14](../app/src/main/kotlin/com/wisekoin/data/model/FeedbackSubmission.kt#L3)). The app sends those fields directly to Sentry's feedback API, not through the WiseKoin backend ([feedback repository, lines 13-30](../app/src/main/kotlin/com/wisekoin/data/repository/FeedbackRepository.kt#L13)). The form pre-fills name/email from the signed-in profile but permits the user to remove them, and validates the submitted fields. The UI warns users not to include passwords, bank account numbers, receipt images, or sensitive financial information ([feedback strings, lines 327-336](../app/src/main/res/values/strings.xml#L327)).

Because Sentry feedback is not keyed to WiseKoin's database account-deletion transaction, deleting a WiseKoin account does not automatically prove deletion of prior Sentry feedback.

### H. Error, performance, device, and network telemetry

Android Sentry is mandatory for a release build and samples performance traces at 10% in release and 100% in debug ([Android build configuration, lines 57-67 and 132-149](../app/build.gradle.kts#L57)). Automatic instrumentation covers file I/O, OkHttp, and Compose; Logcat capture and Sentry Gradle-plugin telemetry are disabled ([Android Sentry build settings, lines 163-189](../app/build.gradle.kts#L163)). Trace headers are propagated only to WiseKoin's configured API base URL ([Android application, lines 6-11](../app/src/main/kotlin/com/wisekoin/WiseKoinApplication.kt#L6)).

The manifest disables automatic Sentry collection of user info, cookies, request/response headers, HTTP bodies, URL query parameters, GraphQL documents/variables, file paths, additional context, screenshots, and view hierarchy ([Android manifest, lines 22-71](../app/src/main/AndroidManifest.xml#L22)). The backend applies substantially the same request/user-data restrictions and samples traces at 10% by default ([backend configuration, lines 3-18](../../../Projects/WiseKoin/src/main/resources/application.properties#L3)). Generic backend exceptions and media-processing errors are explicitly sent to Sentry; application logs record only exception class names for those paths ([global exception handler, lines 58-66 and 130-135](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/common/exception/GlobalExceptionHandler.java#L58)).

These controls minimize content but do not make telemetry anonymous. Depending on the SDK/platform and Sentry project settings, events can still include timestamps, app release/environment, stack traces, operation names, timing, device model, OS/runtime state, network status, and source IP. Sentry documentation notes that IP storage is a separate project-level control. The live Sentry organization was verified in the US region on the audit date, with separate `wisekoin-android` and `wisekoin-backend` projects; event/trace/feedback retention and IP-scrubbing settings remain unconfirmed.

### I. Remote images and incidental requests

The default profile avatar is loaded from `lh3.googleusercontent.com` ([Auth UI state, lines 20-27](../app/src/main/kotlin/com/wisekoin/viewmodel/AuthViewModel.kt#L20), [profile image load, lines 111-119](../app/src/main/kotlin/com/wisekoin/ui/screens/ProfileScreen.kt#L111)). A receipt placeholder can load from `images.unsplash.com` ([receipt image component, lines 69-86](../app/src/main/kotlin/com/wisekoin/ui/screens/ConfirmReceiptComponents.kt#L69)). Those requests can expose ordinary connection data such as IP address, time, app/network headers, and device characteristics to Google or Unsplash. These remote placeholders are unnecessary privacy dependencies and should be replaced with packaged local assets before release.

A user-selected profile image is stored only as a URI in current in-memory UI state and displayed through Coil; no backend avatar upload or persistent local avatar setting was found ([profile picker, lines 73-77](../app/src/main/kotlin/com/wisekoin/ui/screens/ProfileScreen.kt#L73), [avatar update, lines 246-248](../app/src/main/kotlin/com/wisekoin/viewmodel/AuthViewModel.kt#L246)).

## 2. Storage locations and recipients

| Location/recipient | Data involved | Evidence and status |
|---|---|---|
| Android private storage | Encrypted access/refresh tokens; encrypted notification candidates/settings/metrics; temporary receipt/audio cache | Keystore-backed encryption and backup exclusions are implemented; cache cleanup is implemented for app-created receipt/audio files. |
| WiseKoin PostgreSQL | Account/profile, hashed credentials/tokens, OTP state, ledger/planning records, AI structured results/rules/actions, ingestion fingerprints, notification metrics | Data model is code-confirmed. Production example uses Neon Cloud, but actual production deployment and region are not established ([production Compose, lines 10-24](../../../Projects/WiseKoin/docker-compose.prod.yml#L10), [README, lines 280-290](../../../Projects/WiseKoin/README.md#L280)). |
| Google Gemini | Receipt/voice media, extraction prompt, locale/time zone, base currency, relevant category/wallet context and learned rules; generated output | Transmission is code-confirmed. Billing tier, Cloud project, region, DPA, and launch-territory eligibility require operator confirmation. |
| Sentry (US region) | Android/backend error and trace telemetry; Help Center message and optional name/email | Projects and region verified through the connected Sentry account; retention, IP scrubbing, DPA, access roles, and deletion workflow require confirmation. |
| SMTP/email provider | Recipient email and security verification message/code | Vendor is environment-configured and unknown from code. STARTTLS is required by default ([mail configuration, lines 90-101](../../../Projects/WiseKoin/src/main/resources/application.properties#L90)). |
| Google-hosted image and Unsplash | Incidental HTTP request metadata when remote placeholders load | Code-confirmed; should be removed or disclosed. |
| Hosting/network/backup providers | API traffic, source IP, application/database records, logs and backups as applicable | Provider, locations, backup schedule, access controls, and retention are not determinable from code. |

The named banks and wallet apps are data sources on the user's device. The audited code does not send data back to them or authenticate against them.

## 3. Purposes supported by code

The implemented processing supports:

1. creating and authenticating accounts and maintaining sessions;
2. providing the personal ledger, wallet, transaction, category, goal, debt, recurring-entry, and reporting features;
3. sending requested security and password-recovery emails;
4. preventing abuse, duplicate requests, unauthorized access, and invalid uploads;
5. extracting proposed financial fields from a user-selected receipt or recording;
6. learning account-specific merchant/category rules when the user chooses to remember a correction;
7. identifying and measuring optional notification-derived suggestions;
8. receiving user-requested Help Center feedback; and
9. diagnosing crashes, errors, and performance problems.

No code supports targeted advertising, sale of data, marketing email, cross-app behavioral profiling, credit scoring, lending decisions, or training WiseKoin's own general-purpose AI model. The operator must still confirm that no off-code business practice introduces one of those purposes.

## 4. Security controls and limitations

### Implemented controls

- Release API URLs and Sentry DSNs must use HTTPS; production Android cleartext traffic is disabled ([Android build URL validation, lines 13-34](../app/build.gradle.kts#L13), [Android manifest, lines 11-16](../app/src/main/AndroidManifest.xml#L11)). Debug builds deliberately permit cleartext traffic to the local emulator backend ([debug manifest, lines 5-8](../app/src/debug/AndroidManifest.xml#L5)); debug builds must not be used for production data.
- All non-authentication backend routes require authentication, and server sessions are stateless ([security configuration, lines 35-54](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/config/SecurityConfig.java#L35)).
- Android tokens and notification state use AES-GCM keys stored in Android Keystore and are excluded from backup.
- Passwords, refresh tokens, OTPs, verification tokens, idempotency keys, notification references, and media duplicate fingerprints are stored as password hashes or one-way SHA-256 values as appropriate.
- Sensitive Sentry request/user/body/header/query/screenshot/view/log sources are disabled in both applications.
- Release network logging is disabled and debug logging is BASIC; authorization and cookie headers are redacted ([Android network client, lines 30-40 and 136-137](../app/src/main/kotlin/com/wisekoin/data/remote/RetrofitClient.kt#L30)).
- Media type, size, image dimensions, duration, request rate, and ownership are validated by the backend.

### Limitations and items not established

- The repositories do not prove production database encryption at rest, backup encryption, backup retention, administrator access controls, audit logging, incident-response procedures, vulnerability-management cadence, or deletion from backups.
- Sentry project-level IP scrubbing and retention are not proven by SDK flags.
- The backend CORS configuration accepts all origins for bearer-token requests, although it does not allow credentialed browser cookies ([security configuration, lines 59-72](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/config/SecurityConfig.java#L59)). This is not an Android data flow but should be threat-modeled if a web client is added.
- Expired OTP rows have a repository deletion query but no scheduled cleanup invocation was found. They are replaced per user/purpose, consumed after successful verification, or removed on account deletion. A defined retention/cleanup job should be added.
- Server-side AI extraction history and notification metrics have no time-based retention job.
- Remote placeholder images add third-party network disclosures with no product need.

## 5. Retention and deletion matrix

| Data | Code-backed retention/deletion | Gap |
|---|---|---|
| Android access/refresh tokens | Retained until logout/session failure/account deletion flow clears local tokens; excluded from backup | OS/app-uninstall behavior applies; no operator retention issue for local copy |
| Server refresh-token hashes | Default 30-day expiry; rotated/revoked on refresh/logout; all revoked on password change/reset/account deletion | Expired-row cleanup occurs when a new token is issued, not on a documented schedule |
| OTP challenge data | Codes and verification tokens expire after 10 minutes by default; consumed verification is deleted; account deletion removes all | No active scheduled cleanup for expired, unused rows |
| Core account and financial data | Retained while account exists; specific records can be edited/deleted subject to referential rules; account deletion removes active database records | Backup/legal/security retention and deletion SLA unknown |
| App-created receipt cache | Deleted after processing/failure via callback when the URI is an app-cache `Receipt_` file | Process death before cleanup could leave cache until OS/app cleanup; no startup sweep found |
| Voice cache | Deleted after processing, reset, or ViewModel clear | Process death before cleanup could leave cache until OS/app cleanup; no startup sweep found |
| Backend source receipt/audio | Not persisted as a file/entity; held in request memory for validation/provider call | Provider-side retention is separate |
| AI ingestion fingerprint metadata | 30 days by default, scheduled cleanup | Operator can override duration by environment and must disclose actual setting |
| Structured AI results, feedback, rules, actions | No time-based cleanup; user-linked rows cascade on account deletion | Define account-life or shorter retention and verify deletion behavior |
| Local notification candidates | 14 days maximum; 100-item cap; user can dismiss/clear; switching/logging out clears account-bound state | Precise cleanup timing depends on app/store activity |
| Local notification metrics | 30 days; acknowledged uploads are removed | None material beyond app lifecycle |
| Server notification suggestions/metrics | User-linked and deleted by database cascade on account deletion | No time-based cleanup; 30-day report window is not retention |
| Sentry telemetry and feedback | Governed by Sentry organization/project settings | Confirm exact retention, region, IP scrubbing, deletion/export workflow, and backup behavior |
| Gemini prompts/media/output | Governed by selected Gemini service terms | Confirm paid status and DPA. Current Google abuse-monitoring documentation states 55 days for prompts, context, and output; unpaid-service terms permit broader improvement/human review outside EEA/CH/UK |
| SMTP delivery records | Controlled by actual email provider and operator mailbox/log settings | Vendor and retention unknown |
| Infrastructure logs/backups | Not represented in repository | Providers, data, regions, schedule, retention, restoration/deletion behavior unknown |

### Account deletion path

The in-app deletion flow requires current password and a purpose-bound email verification token. The backend revokes refresh tokens, removes OTPs, recurring entries, transactions, debts, goals, categories, wallets, and the user in a transaction ([user service, lines 121-136](../../../Projects/WiseKoin/src/main/java/com/wisekoin/backend/module/user/UserService.java#L121)). AI, ingestion, suggestion-action, and notification-metric foreign keys use `ON DELETE CASCADE` ([AI migration, lines 1-57](../../../Projects/WiseKoin/src/main/resources/db/migration/V9__add_adaptive_ai_feedback.sql#L1), [ingestion migration, lines 1-19](../../../Projects/WiseKoin/src/main/resources/db/migration/V10__add_ai_ingestion_requests.sql#L1), [notification migration, lines 21-43](../../../Projects/WiseKoin/src/main/resources/db/migration/V12__add_notification_pilot_and_transaction_idempotency.sql#L21)). The Android flow then logs out, which clears local session tokens; account rebinding clears local notification state ([navigation, lines 113-120 and 370-380](../app/src/main/kotlin/com/wisekoin/navigation/WiseKoinNavigation.kt#L113)).

This path does **not** itself delete Sentry feedback/telemetry, Google's retained API records, SMTP-provider records, infrastructure logs, or backups. The final policy must describe the actual operator process for those systems.

## 6. User-facing controls actually implemented

- edit username, email, optional phone, and base currency;
- change/reset password and revoke sessions;
- log out;
- create, edit, and delete supported financial records, subject to referential/business rules;
- choose whether to provide a receipt image or voice recording and grant/revoke camera or microphone permission;
- grant/revoke Android notification-listener access, independently enable/disable tracking, dismiss individual candidates, and clear the local queue;
- choose whether to include a name/email with Help Center feedback; and
- delete the account after current-password and email-OTP verification.

Not implemented:

- downloadable or machine-readable account export;
- a privacy-rights request form or tracked request workflow;
- an age gate or parental-consent flow;
- consent records for Terms/Privacy acceptance, AI processing, or Sentry telemetry;
- an in-app Sentry diagnostics opt-out;
- a processor-wide delete/export workflow for Sentry, Gemini, email, hosting, or backups;
- marketing preferences (no marketing feature was found); or
- an in-app published Privacy Policy link.

## 7. Third-party terms affecting disclosure

As of the audit date:

- Google's [Gemini API Additional Terms](https://ai.google.dev/gemini-api/terms) distinguish paid from unpaid processing. For paid service, Google says prompts/files/responses are not used to improve its products and are processed under its data-processing addendum, while limited safety logging remains. For unpaid service outside the EEA, Switzerland, and UK, submitted content and outputs may be used to improve Google products and may be reviewed by humans; Google warns not to submit personal, confidential, or sensitive data. WiseKoin necessarily sends potentially sensitive financial media, so paid-service status and a suitable DPA are release blockers.
- Google's [Gemini abuse-monitoring notice](https://ai.google.dev/gemini-api/docs/usage-policies) says prompts, contextual information, and output are retained for 55 days for safety/policy enforcement and may be reviewed by authorized personnel if flagged.
- Sentry's [Privacy Policy](https://sentry.io/privacy/) states that customer-submitted Service Data is governed by the customer agreement rather than that public policy. Sentry also documents project/organization controls for data scrubbing and discarding IP addresses. WiseKoin must confirm its Customer Agreement/DPA, retention, region, IP settings, and deletion process rather than copying a generic Sentry promise.

Provider terms and configurations change. Re-verify them immediately before publication and on material vendor changes.

## 8. Required remediation before publication

1. Resolve every item in `05_OPEN_PRIVACY_DECISIONS.md` and replace all policy placeholders.
2. Add an unchecked Terms/Privacy acceptance control to registration and record document versions, user, timestamp, and acceptance source.
3. Enforce an 18+ eligibility representation while Gemini is used, or replace/disable Gemini and obtain specialist child-privacy advice.
4. Confirm production Gemini is a paid service under the correct Cloud project, execute/accept the applicable DPA, and verify supported launch regions.
5. Confirm Sentry retention, US-region choice, DPA, access roles, default and custom scrubbing, IP-discard settings, and feedback/data-subject deletion procedure for both projects.
6. Replace Google and Unsplash remote placeholder images with packaged assets.
7. Identify actual hosting, Neon/database, SMTP, network, and backup vendors; document locations, subprocessors, contracts, access, retention, and deletion.
8. Implement a privacy request workflow and decide whether to build export or fulfill access/portability manually.
9. Add scheduled cleanup for expired OTP records and defined retention for structured AI history and server notification metrics.
10. Add startup/periodic cleanup for abandoned Android receipt/audio cache files.
11. Publish the policy in-app and at a stable URL before collecting production user data.
12. Reconcile the final disclosures with Google Play's Data safety form and permission declarations.

