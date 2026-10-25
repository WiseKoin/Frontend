# WiseKoin product and Terms of Service audit

**Audit date:** 2026-10-26  
**Repositories reviewed:** Spring Boot backend (`D:\Projects\WiseKoin`) and Android client (`D:\AndroidStudioProjects\WiseKoin`)  
**Purpose:** identify facts that should control WiseKoin's Terms of Service and distinguish them from operator choices or README-only aspirations.

## Citation convention and method

`Backend:` links point into this repository. `Android:` links point into the separate client repository. Line references describe the audited revision and may move as the code changes. Findings are based primarily on executable code, configuration, migrations, and dependency manifests. Repository-wide searches also checked for billing, subscriptions, purchases, advertising, age gates, administrator roles, suspension states, data export, and legal-document acceptance.

Absence findings are inherently revision-specific. They should be rechecked before release.

## Executive conclusions

- WiseKoin is an authenticated personal-finance recordkeeping and planning service. It records internal wallet balances, income, expenses, transfers, categories, goals, debts, recurring expenses, and reports; it does not execute a real-world payment or move funds at a bank.
- Receipt and voice features send user-selected media, plus account context such as category or wallet names, to Google Gemini for structured extraction. AI results may be ambiguous or wrong and are presented as suggestions; a user confirmation creates the internal transaction.
- Optional Android notification access parses notifications locally from an allowlist of VCB Digibank, MB Bank, Techcombank Mobile, and MoMo. The client stores structured candidates locally and uploads structured facts and a hash, not the original notification text. A candidate is not a transaction until the user confirms it.
- Sentry receives error/performance telemetry and receives Help Center feedback directly, including the message and any optional name/email.
- No product billing, subscriptions, in-app purchases, advertising, credits, pricing, or refund mechanism was found.
- Users can permanently delete their account after password and email-OTP verification. Core account records are explicitly deleted and AI/notification records are database-cascaded. Sentry feedback/telemetry and provider-side copies are outside that deletion path.
- The code has request and media limits but no administrator role, account-disabled field, or implemented suspension workflow. Every authenticated account is reported as enabled and non-locked.
- There is no Terms/Privacy acceptance control or age gate in the Android registration flow. There is also no user-data export feature.
- Current Gemini terms create a material release dependency: Gemini API clients must not be directed to or likely accessed by people under 18; unpaid-service terms warn not to submit sensitive, confidential, or personal information. WiseKoin should use an appropriately contracted paid service, impose and enforce an 18+ rule while Gemini is enabled, and have counsel confirm that its consumer-facing use case is permitted.

## 1. Service scope and who can use it

### Implemented service

The backend exposes authenticated CRUD and planning functionality for:

- wallets and aggregate internal balances ([Backend: `WalletController.java`, lines 34-89](../src/main/java/com/wisekoin/backend/module/wallet/WalletController.java#L34));
- income, expense, and transfer records, filtering, pagination, and yearly monthly statistics ([Backend: `TransactionController.java`, lines 40-101](../src/main/java/com/wisekoin/backend/module/transaction/TransactionController.java#L40), [transaction types, lines 78-82](../src/main/java/com/wisekoin/backend/module/transaction/Transaction.java#L78));
- user-defined categories ([Backend: `CategoryController.java`, lines 33-84](../src/main/java/com/wisekoin/backend/module/category/CategoryController.java#L33));
- savings goals and internal funding records ([Backend: `GoalController.java`, lines 34-82](../src/main/java/com/wisekoin/backend/module/goal/GoalController.java#L34));
- debts (borrowed or lent), repayment records, and status tracking ([Backend: `DebtController.java`, lines 34-82](../src/main/java/com/wisekoin/backend/module/debt/DebtController.java#L34), [debt types/statuses, lines 57-66](../src/main/java/com/wisekoin/backend/module/debt/Debt.java#L57)); and
- recurring expenses, which the server converts into internal expense transactions on schedule ([Backend: `RecurringTransactionController.java`, lines 29-79](../src/main/java/com/wisekoin/backend/module/recurring/RecurringTransactionController.java#L29), [scheduler, lines 18-29](../src/main/java/com/wisekoin/backend/module/recurring/RecurringTransactionScheduler.java#L18)).

The Android app provides account, overview, expense, savings-goal, debt, transaction-entry, receipt, voice, profile/security, Help Center, and bank-notification screens ([Android: navigation routes, lines 50-81](../../../AndroidStudioProjects/WiseKoin/app/src/main/kotlin/com/wisekoin/navigation/WiseKoinNavigation.kt#L50)). Some backend capabilities, including recurring-expense management, are not currently exposed in the Android UI; public terms should therefore describe them as available only where the Service exposes them.

### What the service does not do

Transactions reference WiseKoin-owned wallet records and adjust database balances; a transfer requires two different owned wallets ([Backend: `TransactionService.java`, lines 331-336 and 367-429](../src/main/java/com/wisekoin/backend/module/transaction/TransactionService.java#L331)). There is no bank-account credential flow, payment-rail SDK, open-banking SDK, card processor, or payout code. The app is a ledger and planning tool, not a bank, wallet custodian, money transmitter, payment processor, lender, broker, or financial adviser.

The Android notification feature observes on-device notifications; it does not log in to or transact with the named financial applications ([Android: listener, lines 9-48](../../../AndroidStudioProjects/WiseKoin/app/src/main/kotlin/com/wisekoin/notification/BankNotificationListenerService.kt#L9)).

### Technical and eligibility boundaries

- Android minimum SDK is 24 ([Android: `app/build.gradle.kts`, lines 110-115](../../../AndroidStudioProjects/WiseKoin/app/build.gradle.kts#L110)).
- Code contains no age field, date-of-birth field, parental-consent flow, country gate, sanctions screen, business-only restriction, or Terms checkbox.
- Current Gemini Additional Terms state that API clients may not be directed to or likely accessed by individuals under 18. That external requirement means the released Service must be 18+ while these Gemini features remain enabled, even though the application does not presently enforce it.

## 2. Accounts, authentication, and account responsibilities

- Registration collects username, email, optional phone, and password; it creates a default wallet and default categories ([Backend: `RegisterRequest.java`, lines 10-20](../src/main/java/com/wisekoin/backend/module/auth/dto/RegisterRequest.java#L10), [Backend: `AuthService.java`, lines 35-51](../src/main/java/com/wisekoin/backend/module/auth/AuthService.java#L35)).
- Email must be unique. Passwords are BCrypt-encoded before storage ([Backend: `AuthService.java`, lines 37-46](../src/main/java/com/wisekoin/backend/module/auth/AuthService.java#L37), [Backend: `PasswordConfig.java`](../src/main/java/com/wisekoin/backend/config/PasswordConfig.java)).
- Authentication is stateless. All non-authentication API routes require a valid session; auth endpoints and API documentation are public ([Backend: `SecurityConfig.java`, lines 35-54](../src/main/java/com/wisekoin/backend/config/SecurityConfig.java#L35)).
- Default access-token lifetime is 15 minutes and refresh-token lifetime is 30 days. Refresh tokens rotate and are stored server-side only as SHA-256 hashes ([Backend: `application.properties`, lines 29-31](../src/main/resources/application.properties#L29), [Backend: `RefreshTokenService.java`, lines 28-73](../src/main/java/com/wisekoin/backend/module/auth/token/RefreshTokenService.java#L28)).
- Logout revokes the presented refresh token. Password change/reset revokes all refresh tokens for the account ([Backend: `AuthService.java`, lines 68-77](../src/main/java/com/wisekoin/backend/module/auth/AuthService.java#L68), [Backend: `UserService.java`, lines 73-105](../src/main/java/com/wisekoin/backend/module/user/UserService.java#L73)).
- The Android client stores session tokens using AES-GCM keys held by Android Keystore and excludes token preferences from cloud backup/device transfer ([Android: `TokenManager.kt`, lines 38-116](../../../AndroidStudioProjects/WiseKoin/app/src/main/kotlin/com/wisekoin/data/local/TokenManager.kt#L38), [Android: `data_extraction_rules.xml`, lines 6-14](../../../AndroidStudioProjects/WiseKoin/app/src/main/res/xml/data_extraction_rules.xml#L6)).
- Password recovery, password change, and deletion use purpose-bound email OTPs. Defaults are 10-minute OTP and verification-token lifetimes, 60-second resend cooldown, three sends per hour, and five verification attempts ([Backend: `application.properties`, lines 90-106](../src/main/resources/application.properties#L90), [Backend: `OtpService.java`, lines 65-103 and 131-176](../src/main/java/com/wisekoin/backend/module/user/otp/OtpService.java#L65)).

Terms implication: users should be responsible for accurate registration information, credential security, activity under their account, keeping email access current, and notifying the operator of suspected compromise. The Terms should not promise multifactor login; OTP protects selected security actions, not every login.

## 3. Financial-record behavior and automation

- Wallet balances are application records. Expense processing can fail if an internal wallet would become negative ([Backend: `WalletService.java`, lines 100-112](../src/main/java/com/wisekoin/backend/module/wallet/WalletService.java#L100)).
- Funding a goal and repaying a debt create internal transactions; deleting a funded goal refunds its funding entries to the originating internal wallets ([Backend: `GoalService.java`, lines 62-78 and 128-136](../src/main/java/com/wisekoin/backend/module/goal/GoalService.java#L62), [Backend: `DebtService.java`, lines 75-102](../src/main/java/com/wisekoin/backend/module/debt/DebtService.java#L75)).
- Recurring schedules automatically create an internal expense transaction when due. Processing catches up missed months, capped at 120 occurrences per processing run ([Backend: `RecurringTransactionProcessor.java`, lines 14 and 26-57](../src/main/java/com/wisekoin/backend/module/recurring/RecurringTransactionProcessor.java#L14)).
- The app does not verify WiseKoin records against a bank ledger. Users must review records and remain responsible for real-world balances, debts, tax records, payment deadlines, and financial decisions.

Terms implication: expressly distinguish internal records from real funds and disclose recurring automation. Include a financial-information, no-advice, and accuracy disclaimer.

## 4. User content and content handling

User-provided or user-derived content includes:

- profile data (username, email, phone, base currency) ([Backend: `User.java`, lines 23-31](../src/main/java/com/wisekoin/backend/module/user/User.java#L23));
- wallet/category names, transaction and recurring notes, goal names, debt counterpart names/notes, amounts, currencies, and dates ([Backend: `Transaction.java`, lines 35-76](../src/main/java/com/wisekoin/backend/module/transaction/Transaction.java#L35), [Backend: `RecurringTransaction.java`, lines 38-66](../src/main/java/com/wisekoin/backend/module/recurring/RecurringTransaction.java#L38));
- receipt images and voice recordings selected or created for AI extraction ([Backend: `ReceiptController.java`, lines 24-39](../src/main/java/com/wisekoin/backend/module/receipt/ReceiptController.java#L24), [Backend: `VoiceController.java`, lines 24-39](../src/main/java/com/wisekoin/backend/module/voice/VoiceController.java#L24));
- structured facts extracted from finance-app notifications ([Android: candidate model, lines 10-20](../../../AndroidStudioProjects/WiseKoin/app/src/main/kotlin/com/wisekoin/data/model/BankNotificationCandidate.kt#L10)); and
- Help Center feedback, category, and optional name/email ([Android: `FeedbackSubmission.kt`, lines 3-14](../../../AndroidStudioProjects/WiseKoin/app/src/main/kotlin/com/wisekoin/data/model/FeedbackSubmission.kt#L3)).

No repository document assigns ownership of user content to the operator. The Terms should leave ownership with users and obtain only the license necessary to receive, store, reproduce, transmit, format, analyze, secure, and display content to operate and improve the applicable features and to instruct processors. Users should promise that they have the rights and permissions needed for content they submit, especially images, recordings, and notification data involving other people.

Feedback is explicitly presented as material the team will use to improve WiseKoin ([Android: feedback strings, lines 314-336](../../../AndroidStudioProjects/WiseKoin/app/src/main/res/values/strings.xml#L314)). A narrow feedback-use clause is supported; a broad transfer of all feedback IP is not evidenced or necessary.

## 5. AI and automated features

### Receipt extraction

Receipt images are passed to an extraction client with a prompt containing current date/time, locale, time zone, base currency, the user's expense categories, and learned merchant-category rules ([Backend: `ReceiptService.java`, lines 78-130](../src/main/java/com/wisekoin/backend/module/receipt/ReceiptService.java#L78)). Output can contain store, category candidates, amount, currency, occurrence time, subtotal, tax, discount, tip, payment method, ambiguities, confidence, and possible-duplicate information ([Backend: `ReceiptService.java`, lines 145-234 and 267-345](../src/main/java/com/wisekoin/backend/module/receipt/ReceiptService.java#L145)).

### Voice extraction

Voice recordings are passed with category and wallet context to extract up to ten proposed transactions from one recording ([Backend: `VoiceService.java`, lines 52-55 and 80-136](../src/main/java/com/wisekoin/backend/module/voice/VoiceService.java#L52)). The Android client deletes its temporary audio file after processing or when the ViewModel is cleared ([Android: `VoiceViewModel.kt`, lines 133-143, 199-249 and 435-446](../../../AndroidStudioProjects/WiseKoin/app/src/main/kotlin/com/wisekoin/viewmodel/VoiceViewModel.kt#L133)).

### Provider, lifecycle, and human confirmation

- The extraction client sends prompt and base64 media to Google Gemini and returns structured JSON ([Backend: `GeminiAiExtractionClient.java`, lines 101-134](../src/main/java/com/wisekoin/backend/module/ai/provider/GeminiAiExtractionClient.java#L101)).
- The backend stores structured extraction metadata and results, including merchant label/key, proposed category, amount, currency, occurrence time, ambiguities, model/provider, confidence, locale/time zone, status, and later transaction/rejection data ([Backend: `AiExtraction.java`, lines 26-132](../src/main/java/com/wisekoin/backend/module/ai/adaptive/AiExtraction.java#L26)).
- Suggestions start `PENDING`; users may clarify, confirm, or reject them. Confirmation creates an internal transaction; rejection records a reason ([Backend: `AiSuggestionController.java`, lines 43-112](../src/main/java/com/wisekoin/backend/module/ai/suggestion/AiSuggestionController.java#L43), [Backend: `AiSuggestionService.java`, lines 57-115 and 190-200](../src/main/java/com/wisekoin/backend/module/ai/suggestion/AiSuggestionService.java#L57)).
- If the user opts to remember a corrected category, the service can create/update a per-user merchant categorization rule ([Backend: `AiSuggestionConfirmationRequest.java`, lines 33-35](../src/main/java/com/wisekoin/backend/module/ai/suggestion/dto/AiSuggestionConfirmationRequest.java#L33), [Backend: `AiAdaptationService.java`, lines 181-209](../src/main/java/com/wisekoin/backend/module/ai/adaptive/AiAdaptationService.java#L181)).
- The confidence profile is configured as uncalibrated by default. This is strong evidence that confidence labels are aids, not guarantees ([Backend: `application.properties`, lines 60-70](../src/main/resources/application.properties#L60)).

Terms implication: users must verify every extracted field before confirmation; outputs are probabilistic and not professional advice. The operator should not promise error-free extraction, calibrated probabilities, or detection of all duplicates.

### Media persistence and metadata

The backend validates and reads the upload into an in-memory/base64 payload for provider transmission. No entity or filesystem write stores the source media. A SHA-256 media fingerprint and ingestion status are stored for deduplication ([Backend: `AiMediaIngestionService.java`, lines 272-295](../src/main/java/com/wisekoin/backend/module/ai/media/AiMediaIngestionService.java#L272), [Backend: `AiIngestionRequest.java`, lines 25-56](../src/main/java/com/wisekoin/backend/module/ai/ingestion/AiIngestionRequest.java#L25)). Ingestion metadata is deleted after a configurable period, 30 days by default ([Backend: `AiIngestionRequestService.java`, lines 30-47 and 110-114](../src/main/java/com/wisekoin/backend/module/ai/ingestion/AiIngestionRequestService.java#L30)). Structured AI extraction/feedback records have no general time-based deletion job; they remain until account deletion or another operational deletion.

Terms and Privacy Policy must distinguish the app backend's source-media non-persistence from Google's independent processing and retention.

## 6. Bank-notification feature

- Notification access is a privileged Android service permission the user must enable. Tracking is off by default and cannot be enabled without an active account binding ([Android: manifest, lines 83-97](../../../AndroidStudioProjects/WiseKoin/app/src/main/AndroidManifest.xml#L83), [Android: store, lines 65-89](../../../AndroidStudioProjects/WiseKoin/app/src/main/kotlin/com/wisekoin/data/local/BankNotificationStore.kt#L65)).
- Only package names for VCB Digibank, MB Bank, Techcombank Mobile, and MoMo are allowlisted ([Android: `BankNotificationParserRegistry.kt`, lines 30-81](../../../AndroidStudioProjects/WiseKoin/app/src/main/kotlin/com/wisekoin/notification/BankNotificationParserRegistry.kt#L30)).
- The listener reads title/text/big text/subtext/text lines transiently, passes them to a local parser, and does not place the original text in the persisted candidate model ([Android: listener, lines 19-45](../../../AndroidStudioProjects/WiseKoin/app/src/main/kotlin/com/wisekoin/notification/BankNotificationListenerService.kt#L19), [Android: candidate model, lines 10-20](../../../AndroidStudioProjects/WiseKoin/app/src/main/kotlin/com/wisekoin/data/model/BankNotificationCandidate.kt#L10)).
- Candidate IDs are SHA-256 hashes over notification key plus extracted transaction facts. The persisted candidate contains amount, currency, direction, source app, timestamps, parser identifiers, and optional server suggestion ID ([Android: parser, lines 78-103 and 154-156](../../../AndroidStudioProjects/WiseKoin/app/src/main/kotlin/com/wisekoin/notification/BankNotificationParser.kt#L78)).
- The local queue is AES-GCM encrypted, account-scoped, capped at 100 candidates, and pruned after 14 days. Local aggregate parser metrics are retained for 30 days ([Android: retention, lines 6-30](../../../AndroidStudioProjects/WiseKoin/app/src/main/kotlin/com/wisekoin/data/local/BankNotificationRetention.kt#L6), [Android: store encryption/metrics, lines 190-269](../../../AndroidStudioProjects/WiseKoin/app/src/main/kotlin/com/wisekoin/data/local/BankNotificationStore.kt#L190)).
- The client uploads only source ID, hash, parser version, amount, currency, direction, occurrence time, locale, and time zone, plus aggregate counts ([Android: syncer, lines 17-60](../../../AndroidStudioProjects/WiseKoin/app/src/main/kotlin/com/wisekoin/notification/BankNotificationSyncer.kt#L17)). The server accepts only the same allowlisted sources and requires the notification to be no more than 14 days old ([Backend: request DTO, lines 14-24](../src/main/java/com/wisekoin/backend/module/ai/notification/dto/BankNotificationSuggestionRequest.java#L14), [Backend: validation, lines 114-133](../src/main/java/com/wisekoin/backend/module/ai/notification/BankNotificationSuggestionService.java#L114)).
- Server-created notification suggestions deliberately require category review and are not AI-provider calls ([Backend: `BankNotificationSuggestionService.java`, lines 65-111](../src/main/java/com/wisekoin/backend/module/ai/notification/BankNotificationSuggestionService.java#L65)).

Terms implication: call this an optional convenience parser, not bank synchronization. Disclaim completeness and accuracy, require review, and state no affiliation with the named financial institutions. Detailed permission/data disclosures belong in the Privacy Policy and Android permission education UI.

## 7. Feedback, observability, and third parties

### Sentry

- Backend Sentry collects error monitoring and request traces when configured. Automatic user info, cookies, headers, bodies, query parameters, GraphQL values, and file paths are disabled ([Backend: `application.properties`, lines 3-18](../src/main/resources/application.properties#L3)).
- Android Sentry instruments file I/O, OkHttp, and Compose; Logcat, screenshots, view hierarchy, additional context, automatic user info, HTTP bodies/headers/query parameters, and cookies are disabled ([Android: `app/build.gradle.kts`, lines 163-189](../../../AndroidStudioProjects/WiseKoin/app/build.gradle.kts#L163), [Android: manifest, lines 22-71](../../../AndroidStudioProjects/WiseKoin/app/src/main/AndroidManifest.xml#L22)).
- Help Center feedback is sent straight to Sentry as category-prefixed message text, optional name, and optional contact email ([Android: `FeedbackRepository.kt`, lines 13-30](../../../AndroidStudioProjects/WiseKoin/app/src/main/kotlin/com/wisekoin/data/repository/FeedbackRepository.kt#L13)). Messages must be 12-2,000 characters; optional name is capped at 100 and email at 254 ([Android: `FeedbackViewModel.kt`, lines 155-170](../../../AndroidStudioProjects/WiseKoin/app/src/main/kotlin/com/wisekoin/viewmodel/FeedbackViewModel.kt#L155)). The UI warns users not to include credentials, account numbers, receipt images, or sensitive financial information ([Android: `strings.xml`, lines 335-336](../../../AndroidStudioProjects/WiseKoin/app/src/main/res/values/strings.xml#L335)).

Sentry's public Privacy Policy says customer-submitted Service Data is controlled by the Sentry customer and governed by the customer agreement, rather than Sentry's website privacy policy. WiseKoin therefore needs its own Privacy Policy and should document its Sentry region and retention settings.

### Google Gemini

Google receives receipt/voice media, constructed prompts, and some account context for extraction. Current official terms are the [Gemini API Additional Terms](https://ai.google.dev/gemini-api/terms) and [Google APIs Terms](https://developers.google.com/terms). Material flow-down items include lawful use, rights in submitted content, no circumvention of limits or safeguards, and appropriate caution with generated output.

The Gemini terms distinguish paid and unpaid processing. They state that unpaid-service inputs/outputs may be used to improve Google products and may be human reviewed, and warn not to submit sensitive, confidential, or personal information. Paid-service prompts/responses are not used for product improvement and are handled under Google's processor terms, subject to safety/security logging. The repository cannot establish which plan or cloud project is used.

### Other infrastructure

- PostgreSQL is the persistence technology; the README identifies Neon as the intended host, but runtime database configuration is environment-provided and could be changed ([Backend: `application.properties`, lines 20-27](../src/main/resources/application.properties#L20)).
- Security OTPs are sent through an environment-configured SMTP service; the provider is not identifiable from code ([Backend: `application.properties`, lines 90-105](../src/main/resources/application.properties#L90)).
- Android uses Google/Android platform components and open-source libraries including Retrofit, OkHttp, Gson, CameraX, Coil, and Sentry ([Android: `app/build.gradle.kts`, lines 192-234](../../../AndroidStudioProjects/WiseKoin/app/build.gradle.kts#L192)). Their licenses apply to their components; they should not be represented as owning the WiseKoin service.

Terms implication: identify Google Gemini and Sentry by name; use a general service-provider clause for hosting/email providers whose production identities are not fixed. The Privacy Policy—not the Terms alone—must describe processing, retention, transfers, and user rights.

## 8. Enforced limits and acceptable-use boundaries

Code-enforced defaults, all configurable at deployment unless noted:

| Limit | Implemented behavior | Evidence |
|---|---|---|
| API rate limits | Fixed 60-second window: 10 receipt/voice requests, 20 auth requests, 120 other API requests; keyed by user when authenticated and IP otherwise; HTTP 429 with retry delay | [Backend config, lines 83-88](../src/main/resources/application.properties#L83); [filter, lines 59-110](../src/main/java/com/wisekoin/backend/config/ratelimit/RateLimitFilter.java#L59) |
| Receipt media | Default 5 MB; JPEG, PNG, or WebP; maximum width/height 8,192 and 20 million pixels | [Backend config, lines 49-55](../src/main/resources/application.properties#L49); [media validation, lines 60-65 and 93-112](../src/main/java/com/wisekoin/backend/module/ai/media/AiMediaIngestionService.java#L60) |
| Voice media | Default 5 MB, MP4/M4A container, maximum 60 seconds | [Backend config, lines 54-55](../src/main/resources/application.properties#L54); [media validation, lines 68-81](../src/main/java/com/wisekoin/backend/module/ai/media/AiMediaIngestionService.java#L68) |
| Repeat AI extraction | Same user's same media/source is rejected when already completed on the same day; failed/stale processing attempts may restart | [Backend ingestion service, lines 49-88 and 116-132](../src/main/java/com/wisekoin/backend/module/ai/ingestion/AiIngestionRequestService.java#L49) |
| Voice batch | Maximum ten proposed transactions per recording | [Backend voice service, lines 52-55](../src/main/java/com/wisekoin/backend/module/voice/VoiceService.java#L52) |
| Notification queue | Maximum 100 local candidates, 14-day retention | [Android retention, lines 6-30](../../../AndroidStudioProjects/WiseKoin/app/src/main/kotlin/com/wisekoin/data/local/BankNotificationRetention.kt#L6) |
| Suggestion listing | Page size clamped to 1-100 | [Backend suggestion controller, lines 43-53](../src/main/java/com/wisekoin/backend/module/ai/suggestion/AiSuggestionController.java#L43) |
| Feedback | 12-2,000 message characters; optional contact field limits | [Android feedback ViewModel, lines 155-170](../../../AndroidStudioProjects/WiseKoin/app/src/main/kotlin/com/wisekoin/viewmodel/FeedbackViewModel.kt#L155) |

Content-type validation, ownership checks, authentication, provider safety controls, idempotency, and rate limiting support prohibitions against unlawful content, malware, unauthorized access, interference, scraping/automation that burdens the service, bypassing safeguards/limits, and submitting content without rights. There is no evidence for restrictions unrelated to security, law, provider terms, or this financial-record purpose.

## 9. Paid plans, advertising, and commercial terms

Repository-wide searches found no Google Play Billing dependency, subscription/product model, purchase endpoint, checkout, payment processor, ad SDK, premium entitlement, credit balance, pricing, trial, refund, or cancellation flow. The word “payment” in product code describes a user's financial record, not payment to WiseKoin.

The Terms should state that the audited version is free and does not authorize charges. Do not add subscription, auto-renewal, tax, price-change, or refund clauses unless a payment system and checkout disclosures are actually implemented.

## 10. Suspension, termination, deletion, and portability

### Implemented

- A user can delete the account only after current-password verification and a purpose-bound email OTP ([Backend: `UserController.java`, lines 94-100](../src/main/java/com/wisekoin/backend/module/user/UserController.java#L94), [Backend: `UserService.java`, lines 121-136](../src/main/java/com/wisekoin/backend/module/user/UserService.java#L121)).
- Deletion revokes refresh tokens and deletes OTPs, recurring transactions, transactions, debts, goals, categories, wallets, and the user. Database foreign keys cascade deletion to AI extractions, AI feedback, categorization rules, AI actions, ingestion metadata, and notification pilot metrics ([Backend: `V9__add_adaptive_ai_feedback.sql`, lines 1-48](../src/main/resources/db/migration/V9__add_adaptive_ai_feedback.sql#L1), [Backend: `V10__add_ai_ingestion_requests.sql`, lines 1-9](../src/main/resources/db/migration/V10__add_ai_ingestion_requests.sql#L1), [Backend: `V12__add_notification_pilot_and_transaction_idempotency.sql`, lines 21-38](../src/main/resources/db/migration/V12__add_notification_pilot_and_transaction_idempotency.sql#L21)).
- Android clears tokens on logout even if the network logout fails; changing to guest/account binding resets the encrypted notification state ([Android: `AuthViewModel.kt`, lines 214-241](../../../AndroidStudioProjects/WiseKoin/app/src/main/kotlin/com/wisekoin/viewmodel/AuthViewModel.kt#L214), [Android: navigation, lines 113-120](../../../AndroidStudioProjects/WiseKoin/app/src/main/kotlin/com/wisekoin/navigation/WiseKoinNavigation.kt#L113), [Android: store, lines 65-81](../../../AndroidStudioProjects/WiseKoin/app/src/main/kotlin/com/wisekoin/data/local/BankNotificationStore.kt#L65)).

### Not implemented or not determinable

- No administrator role, account-suspension status, disabled flag, ban mechanism, notice process, or appeal flow exists. `CustomUserDetails` reports every loaded account as non-expired, non-locked, credentials-non-expired, and enabled ([Backend: `CustomUserDetails.java`, lines 35-52](../src/main/java/com/wisekoin/backend/module/auth/security/CustomUserDetails.java#L35)).
- Authentication/validation/rate-limit controls can reject requests, but that is not an account-termination system.
- No data export/download function was found.
- The account deletion code cannot establish backup purge schedules, legal holds, Google retention, Sentry retention, or deletion of independently submitted Sentry feedback. Those promises require operational confirmation and a Privacy Policy.

Terms implication: accurately describe user deletion and temporary request restriction. Do not claim a detailed operator suspension/appeal system unless one is adopted. Any reserved operator termination right needs a confirmed policy and an operational way to execute it.

## 11. Intellectual property and licensing status

No top-level `LICENSE` file was found in either repository. Backend Maven license metadata is empty ([Backend: `pom.xml`, lines 17-21](../pom.xml#L17)). Code cannot establish the legal owner of the WiseKoin name, code, design, or content, trademark status, contributor assignments, or the operator's authority to grant an end-user license.

The Terms may grant a narrow, revocable, non-transferable right to use the app only after the owner/operator and distribution model are confirmed. Open-source components remain under their own licenses. VCB, MB, Techcombank, MoMo, Google, Android, and Sentry names/marks belong to their respective owners; the code does not evidence sponsorship or affiliation.

## 12. Launch-critical mismatches and controls

1. **Gemini eligibility and use case:** Current Gemini terms require 18+ API clients and describe the service as for professional/business developer use, not consumer use. WiseKoin appears consumer-facing. Obtain written/provider-counsel confirmation that this deployment is permitted, or replace/disable the feature.
2. **Gemini plan/data handling:** Receipt and voice inputs can contain personal financial information. Confirm a paid Cloud project and appropriate DPA/region. Do not deploy these features on an unpaid tier that warns against such inputs.
3. **No age gate:** Add an 18+ eligibility representation to registration if Gemini remains enabled. A clause hidden in Terms is weaker than affirmative assent.
4. **No Terms/Privacy assent:** Add conspicuous links and an unchecked acceptance control at registration; record Terms/Privacy versions and acceptance timestamp server-side. Provide notice/acceptance handling for material changes.
5. **No Privacy Policy:** The application processes financial records, media, notification-derived data, telemetry, and feedback through multiple providers. Publish a separate, consistent Privacy Policy before release.
6. **No data export:** Decide whether law or product commitments require export. Do not promise portability until implemented.
7. **Deletion scope:** Define backup retention and processor-deletion procedures. Account deletion does not itself remove Sentry feedback.
8. **Operator enforcement:** Decide whether and how accounts can be suspended or terminated for abuse; implement any process the Terms promise.

## 13. External primary sources checked

- [Gemini API Additional Terms of Service](https://ai.google.dev/gemini-api/terms), effective 2026-03-23 (checked 2026-10-26).
- [Google APIs Terms of Service](https://developers.google.com/terms), last modified 2021-11-09 (checked 2026-10-26).
- [Sentry Privacy Policy](https://sentry.io/privacy/), version 3.3.2 dated 2025-10-29 (checked 2026-10-26).
- [Sentry API documentation for user feedback](https://docs.sentry.io/api/projects/list-a-projects-user-feedback/) (checked 2026-10-26).

These provider documents can change independently. Re-check them and the actual production plan/region at launch and on material provider changes.
