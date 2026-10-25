# WiseKoin Privacy Policy

**Publication status:** Draft—do not publish until every bracketed item is resolved and the publication gates in the reviewer notes are completed  
**Effective date:** [EFFECTIVE DATE]  
**Version:** [VERSION]  
**Last updated:** [LAST UPDATED DATE]

This Privacy Policy explains how **[OPERATOR LEGAL NAME, ENTITY TYPE, AND FORMATION JURISDICTION]**, located at **[POSTAL ADDRESS]** (**“WiseKoin,” “we,” “us,”** or **“our”**), collects, uses, discloses, and retains personal data when you use the WiseKoin Android application, application programming interfaces, and related services (collectively, the **“Service”**).

WiseKoin is a personal financial recordkeeping and planning tool. It does not connect to your bank account, collect your online-banking credentials, hold funds, or execute payments.

Contact us about this policy or a privacy request at **[PRIVACY EMAIL]** or **[PRIVACY POSTAL ADDRESS]**. **[INSERT DATA PROTECTION OFFICER OR EEA/UK REPRESENTATIVE DETAILS IF REQUIRED.]**

## 1. Scope and responsibility

This policy applies to personal data processed through the Service. It does not govern the independent privacy practices of your device manufacturer, Android or an app store, your bank or wallet provider, your mobile or internet provider, or a third-party service you use independently.

For personal data covered by this policy, **[OPERATOR LEGAL NAME]** is the **[controller/business/other locally applicable role]**, except where this policy says a provider acts independently under its own terms.

The Service is intended only for people who are at least 18 years old. See Section 13.

## 2. Personal data we process

### Account and profile data

We process:

- username;
- email address;
- optional phone number;
- password in hashed form; and
- your selected base currency.

### Authentication and security data

We process session credentials, hashed refresh tokens, one-time verification-code records, verification purpose and status, send and attempt counts, timestamps, and security-event information needed for login, password recovery, password changes, account deletion, abuse prevention, and rate limiting. For unauthenticated requests, rate limiting temporarily uses the requesting IP address; for authenticated requests, it uses your internal user ID.

We send verification codes and security messages to your account email address through our email provider.

### Financial and planning data

We process the records you create in WiseKoin, which may include:

- wallet names, types, and balances;
- transaction amount, currency, type, date, wallet, category, and notes;
- transfer source and destination wallets;
- recurring-entry amount, note, category, wallet, schedule, and status;
- category names, types, and icons;
- savings-goal names, amounts, deadlines, status, and funding history; and
- debt counterparty names, borrowed or lent status, amounts, due dates, repayment history, and notes.

These records can reveal sensitive details about your finances and other people. Avoid entering unnecessary bank account numbers, card numbers, passwords, government identifiers, or information about another person that you are not authorized to provide.

### Receipt images and voice recordings

If you choose receipt extraction, we process the image you capture or select. If you choose voice extraction, we process the audio you record. We also process the file type, size, a one-way media fingerprint for duplicate detection, device locale, time zone, and a request identifier.

The Android app keeps an app-captured receipt or voice file temporarily in its private cache and is designed to delete it after processing, failure, reset, or leaving the relevant workflow. A gallery image selected through Android's photo picker is streamed from the selected content URI. The WiseKoin backend validates and holds the uploaded media in request memory long enough to send it for extraction; it does not save the original image or recording as a database record or server file.

The original media is nevertheless sent to Google Gemini, whose processing and retention are described in Sections 5, 7, and 8.

### AI-derived and learning data

For receipt or voice extraction, we process the resulting proposed transaction fields, which can include merchant or counterparty, amount, currency, date/time, transaction type, category, wallet, transcript-derived note, ambiguity and confidence information, duplicate candidates, and model/provider metadata.

We retain structured extraction records and your later confirmation, correction, clarification, or rejection. If you choose to remember a category correction, we create an account-specific merchant/category rule for future suggestions. WiseKoin does not use this feature to make credit, lending, insurance, employment, or other decisions that produce legal or similarly significant effects.

### Optional finance-app notification data

If you grant Android notification-listener access **and** enable WiseKoin's tracking switch, Android makes notification content available to the local listener. The current WiseKoin code examines only allowlisted packages identified as VCB Digibank, MB Bank, Techcombank Mobile, and MoMo and ignores other packages.

For an allowlisted notification, the app transiently reads text fields on your device to identify a possible transaction. It does not retain or upload the original notification text through this feature. It retains a structured candidate containing the supported source, amount, currency, income/expense direction, event and capture time, parser version, a one-way reference hash, and processing status. It also collects aggregate counts for observed, parsed, failed, duplicate, confirmed, and rejected suggestions.

The structured candidate and related metadata can still be sensitive financial data. A candidate does not become a WiseKoin transaction unless you review and confirm it.

### Help Center feedback

If you send feedback, we process the category you choose, your message, and any name or email you choose to include. The form may prefill your profile name and email, but those contact fields are optional and can be removed before sending. Do not put passwords, verification codes, full bank or card numbers, receipt images, or unnecessary sensitive financial information in a feedback message.

### Technical and diagnostics data

We use Sentry to receive application errors and sampled performance traces. Depending on the event, SDK, device, and project configuration, this data may include:

- crash or exception type, message, and stack trace;
- operation names, timing, and performance spans;
- app version, release, and environment;
- device model, operating-system/runtime version, and app state;
- network status and request destination or route information; and
- timestamp, event identifiers, and network metadata such as IP address.

WiseKoin configures its SDKs not to automatically send Sentry user profiles, cookies, HTTP request or response headers, request bodies, URL query strings, GraphQL documents/variables, screenshots, Android view hierarchy, Logcat, or file paths. These settings reduce data collection but do not make diagnostics anonymous. A crash message or stack context can still contain unexpected personal data.

### Data from remote content hosts

**[REMOVE BEFORE PUBLICATION OR RETAIN THIS DISCLOSURE:]** The audited app contains a default profile image hosted by Google and a receipt placeholder hosted by Unsplash. Loading either image can provide the host with ordinary request data such as IP address, time, request headers, and device/network information. We recommend replacing both with packaged app assets.

## 3. How we obtain data

We obtain personal data:

- directly from you when you register, create records, select or record media, confirm or correct a suggestion, change settings, or send feedback;
- from your device, such as locale, time zone, app/device diagnostics, IP/network information, and content you permit the app to access;
- from supported finance-app notifications when you grant notification access and enable tracking;
- by generating or inferring structured fields, confidence/ambiguity information, duplicate signals, and merchant-category rules from your use of extraction features; and
- from service providers that return extraction results, delivery status, or technical diagnostics.

## 4. Why we process personal data

We use personal data only for the following implemented purposes:

1. **Provide the Service:** create and maintain your account; authenticate sessions; store, calculate, display, and synchronize your financial records; run recurring entries; and provide reports.
2. **Provide features you request:** process a selected receipt or recording, generate a proposed transaction, and process optional notification-derived candidates.
3. **Personalize your account:** apply your base currency, categories, wallets, and remembered merchant-category rules to your own suggestions.
4. **Protect accounts and the Service:** verify sensitive actions, recover accounts, rotate or revoke sessions, validate ownership and uploads, enforce rate and duplicate limits, and prevent misuse.
5. **Support and communicate:** deliver security codes and respond to feedback or support requests when contact details are available.
6. **Operate and improve reliability:** diagnose errors, monitor sampled performance, measure notification-parser quality through aggregate counters, and improve the clarity and reliability of WiseKoin features.
7. **Comply with law and protect rights:** respond to valid legal requirements, enforce applicable terms, investigate security incidents, and establish or defend legal claims.

The audited Service contains no advertising SDK, cross-app behavioral tracking SDK, or marketing-email feature. **[CONFIRM THAT NO OFF-CODE PRACTICE SELLS PERSONAL DATA, SHARES IT FOR CROSS-CONTEXT BEHAVIORAL ADVERTISING, OR USES IT FOR TARGETED ADVERTISING.]**

## 5. AI-assisted extraction

### Data sent to Google Gemini

When you request receipt or voice extraction, we send Google:

- the selected receipt image or voice recording;
- an extraction prompt and response schema;
- your locale, time zone, and base currency;
- relevant category names and identifiers;
- for voice extraction, relevant wallet names and identifiers;
- account-specific merchant/category rules, if any; and
- the generated output required to return a result.

We do not intentionally send your account password, refresh token, email address, or phone number in the extraction prompt. The media or user-created category, wallet, and note content may itself contain identifying or sensitive information.

### Provider configuration required before launch

**[CONFIRM BEFORE PUBLICATION:]** Production must use a Gemini **Paid Service** under an active Cloud Billing project and applicable data-processing terms. Under Google's current paid-service terms, Google states that it does not use prompts, associated files, or responses to improve its products, but it logs prompts and responses for limited safety, abuse-prevention, and legal purposes. Google's current abuse-monitoring notice states a 55-day period for prompts, contextual information, and output and permits authorized human review of flagged content. Confirm the exact terms, model, project, region, and retention immediately before publication.

Do not deploy these financial-media features using Gemini unpaid service outside jurisdictions where paid-service data terms automatically apply. Google's current unpaid-service terms may permit use of submitted content and responses to improve Google products and human review and warn against submitting personal, confidential, or sensitive information.

See the [Gemini API Additional Terms](https://ai.google.dev/gemini-api/terms), [Gemini API abuse-monitoring notice](https://ai.google.dev/gemini-api/docs/usage-policies), and [Google Privacy Policy](https://policies.google.com/privacy).

### Human review and effect of output

WiseKoin's extraction is automated. Google personnel may review provider content only as described by Google's applicable terms and safety process. WiseKoin personnel may access structured results or diagnostics only under **[DESCRIBE PRODUCTION SUPPORT/ACCESS POLICY]**.

AI results are suggestions. You review and confirm them before they become a WiseKoin transaction. They do not make a legal or similarly significant decision about you.

## 6. Legal bases where required

Where applicable law requires us to identify a legal basis, we rely on the bases below, subject to **[JURISDICTION-SPECIFIC COUNSEL REVIEW]**:

| Processing | Proposed basis |
|---|---|
| Account, authentication, ledger, user-requested receipt/voice extraction, requested notification suggestions, and support | Necessary to perform our agreement with you or take steps at your request |
| Account security, rate limiting, duplicate prevention, service integrity, limited diagnostics, and feature-reliability measurement | Our legitimate interests in providing a secure and reliable Service, after balancing those interests against your rights |
| Optional device permissions and any processing for which local law requires consent | Consent; you may withdraw device permission or disable the feature, without affecting earlier lawful processing |
| Record retention, lawful requests, and legal claims | Compliance with legal obligations and/or our legitimate interests in establishing, exercising, or defending legal claims |

Granting an Android permission is a device control and is not necessarily the only consent required by privacy law. **[CONFIRM TERRITORY-SPECIFIC CONSENT REQUIREMENTS FOR SENTRY, NOTIFICATION ACCESS, AI MEDIA, AND ANY OPTIONAL PROCESSING.]**

## 7. How we disclose personal data

We disclose personal data only as described below and subject to the final contracts and configurations confirmed before publication.

### Service providers

- **Google Gemini:** processes receipt/voice media, prompts, context, and generated results for extraction.
- **Sentry:** processes Android/backend diagnostic telemetry and Help Center feedback. The current WiseKoin organization is hosted in Sentry's US region. Feedback text and optional contact details go directly from the app to Sentry. Sentry Service Data is governed by our customer agreement and data-processing terms, not solely by Sentry's public website privacy policy. See [Sentry's Privacy Policy](https://sentry.io/privacy/) for general information.
- **[ACTUAL DATABASE PROVIDER; CONFIRM WHETHER NEON]:** stores the PostgreSQL records described in this policy.
- **[APPLICATION HOST/INFRASTRUCTURE PROVIDER]:** hosts the WiseKoin backend and may process API traffic, IP addresses, logs, and application data.
- **[SMTP/EMAIL PROVIDER]:** receives email address and verification-message content to deliver security codes.
- **[BACKUP, NETWORK, SECURITY, AND OTHER PROCESSORS]:** [LIST PROVIDERS, PURPOSES, AND LOCATIONS].

### At your direction

We process and disclose data when you direct us to use a feature—for example, when you submit a receipt or recording for Google extraction or send a Help Center message to Sentry.

### Legal and safety disclosures

We may disclose personal data if reasonably necessary to comply with applicable law or valid legal process, protect a person's rights or safety, investigate fraud or a security incident, or establish, exercise, or defend legal claims. **[CONFIRM THE OPERATOR'S REQUEST-REVIEW AND CHALLENGE PROCEDURE.]**

### Business reorganization

**[COUNSEL/OPERATOR TO CONFIRM:]** If WiseKoin is involved in a merger, acquisition, financing, reorganization, bankruptcy, or sale of assets, personal data may be reviewed or transferred as part of that transaction, subject to applicable law and appropriate confidentiality protections. We will provide notice if required.

### No advertising sale or sharing

The audited product has no advertising or behavioral-marketing SDK and no mechanism for selling personal data. **[CONFIRM BEFORE PUBLICATION: We do not sell personal data, share personal data for cross-context behavioral advertising, or use personal data for targeted advertising as those terms are defined by applicable law.]**

We do not send notification content or WiseKoin financial records back to the supported banks or wallet apps through the audited notification feature.

## 8. Retention

We retain personal data only for the periods described below or **[FINAL GENERAL RETENTION STANDARD, INCLUDING LEGAL-HOLD RULES]**. Configuration values can change; the final published policy must state the actual production periods.

| Data | Retention in the audited implementation |
|---|---|
| Account/profile and core financial records | While the account exists, unless you delete a supported record earlier; [ADD INACTIVE-ACCOUNT AND LEGAL-RETENTION PERIODS] |
| Android session tokens | Until logout, session failure, account deletion flow, app-data removal, or uninstall; excluded from Android cloud backup/device transfer |
| Server refresh-token hashes | Default token validity is 30 days; rotated or revoked on refresh/logout and all revoked on password change, reset, or account deletion |
| OTP challenge/security-verification records | Codes and verification tokens are valid for 10 minutes by default; successful consumption and account deletion remove records; [ADD EXPIRED-ROW CLEANUP PERIOD AFTER IMPLEMENTATION] |
| App-created receipt and voice cache | Intended to be removed after processing/failure/reset; process interruption may leave cache until later app/OS cleanup [ADD STARTUP CLEANUP] |
| Original media on the WiseKoin backend | Not persisted as a server file or database record; processed in request memory |
| AI ingestion fingerprint/status metadata | 30 days by default; [CONFIRM PRODUCTION OVERRIDE] |
| Structured AI results, corrections, rules, and actions | While the account exists, unless [ADD SHORTER RETENTION OR USER DELETE CONTROL] |
| Local notification candidates | Up to 14 days, subject to a 100-item cap; earlier if dismissed, cleared, account-switched, logged out, or app data is removed |
| Local notification aggregate metrics | Up to 30 days and removed after acknowledged upload |
| Server notification suggestions and metrics | While the account exists; [ADD TIME-BASED RETENTION] |
| Sentry errors, traces, and Help Center feedback | [SENTRY EVENT/TRACE RETENTION] and [SENTRY FEEDBACK RETENTION]; confirm deletion and backup aging |
| Gemini prompts, contextual data, media, and output | Under the applicable Google terms; current abuse-monitoring documentation states 55 days [VERIFY AT PUBLICATION] |
| Email-provider records | [SMTP PROVIDER RETENTION] |
| API/hosting/security logs | [LOG CATEGORIES AND RETENTION] |
| Backups | [BACKUP SCHEDULE, RETENTION, AND DELETION/ISOLATION PERIOD] |

When you complete in-app account deletion, WiseKoin verifies your password and emailed code, revokes server refresh tokens, and removes the active account, core financial/planning records, and user-linked AI/notification database records. The app then clears its signed-in state and account-bound local notification data.

Account deletion does not by itself prove immediate deletion from Sentry, Google, the email provider, infrastructure logs, or backups. **[DESCRIBE THE OPERATOR'S PROCESS AND SLA FOR THOSE SYSTEMS, INCLUDING HOW A USER'S SENTRY FEEDBACK IS LOCATED.]** We may retain limited data where required by law, to resolve a security incident or dispute, or to establish or defend legal claims, but only for the applicable purpose and period.

## 9. Security

WiseKoin implements safeguards designed to reduce risk, including:

- HTTPS-only release API and Sentry endpoints and disabled production cleartext traffic;
- authenticated access to non-authentication API routes and ownership checks for user records;
- password hashing and one-way hashing of refresh tokens, verification tokens, idempotency keys, notification references, and media fingerprints as appropriate;
- AES-GCM encryption for Android session tokens and local notification state using Android Keystore keys;
- exclusion of token and notification preferences from Android cloud backup and device transfer;
- automatic rotation/revocation of sessions for relevant security actions;
- media, rate, duplicate, type, dimension, and duration validation;
- minimized Sentry request/user/content collection; and
- disabled release HTTP logging of API requests.

**[ADD VERIFIED PRODUCTION CONTROLS ONLY: database and backup encryption, hosting/network controls, staff access restrictions, audit logging, monitoring, security testing, incident response, and vendor reviews.]**

No system is completely secure. Keep your device, email account, password, and verification codes secure, and contact **[SECURITY EMAIL]** if you believe your account or data has been compromised.

## 10. Your choices and controls

### Account and records

You can update supported profile details in the app and create, edit, or delete supported financial records. Some records cannot be deleted while referenced by another record; you may need to remove the dependent record first.

Logging out removes the active local session and revokes the submitted server refresh token, but does not delete your account or records. Use the in-app account-deletion flow to delete the account.

### Camera, microphone, photos, and notifications

You can decline camera or microphone permission and avoid the associated feature. The Android photo picker lets you choose a particular image without granting broad media-library access.

Notification-derived suggestions require both Android notification-listener access and the WiseKoin tracking switch. You can turn tracking off in WiseKoin, clear pending local candidates, or revoke notification access in Android settings. Revoking a permission stops future access but does not automatically delete data already sent to the backend; use account deletion or contact us for an applicable privacy request.

Android's notification-listener permission is broad at the operating-system level even though WiseKoin's current code filters for allowlisted finance-app packages. Review the permission carefully before enabling it.

### Feedback

You can omit your name and email from Help Center feedback. If you send feedback without contact information, we may be unable to reply or reliably associate that feedback with your account for a later request.

### Diagnostics

The audited app does not provide an in-app Sentry diagnostics opt-out. **[CONFIRM LAWFUL BASIS AND WHETHER A TERRITORY-SPECIFIC CONSENT OR OPT-OUT MUST BE IMPLEMENTED.]**

### Export

The audited app does not provide automated data export. To request a copy in a jurisdiction that grants that right, contact **[PRIVACY EMAIL]**. **[IMPLEMENT AND DOCUMENT THE MANUAL OR AUTOMATED EXPORT PROCEDURE, FORMAT, SCOPE, AND VERIFICATION.]**

## 11. Your privacy rights

Depending on where you live and subject to applicable exceptions, you may have rights to:

- know whether and how we process your personal data;
- access or obtain a copy of personal data;
- correct inaccurate personal data;
- request deletion;
- object to or restrict certain processing;
- withdraw consent for future processing where consent is the basis;
- receive certain data in a portable format;
- opt out of sale, targeted advertising, or qualifying profiling, if any occurs; and
- complain to a competent privacy or data-protection authority.

To exercise a right, contact **[PRIVACY EMAIL OR REQUEST URL]** and describe the request. We may ask for information reasonably necessary to verify your identity and authority. Authorized agents may submit a request where local law permits, subject to verification. We will respond within **[APPLICABLE RESPONSE PERIOD OR “THE PERIOD REQUIRED BY APPLICABLE LAW”]** and explain any lawful refusal or extension.

The in-app deletion mechanism is available for active accounts, but processor records or a request for access/portability may require this contact process. Do not submit a privacy request directly to Sentry or Google for WiseKoin-controlled Service Data unless we instruct you to; contact us so we can coordinate with our processors.

**[INSERT REGION-SPECIFIC SUPPLEMENTS, APPEAL RIGHTS, NON-DISCRIMINATION LANGUAGE, SUPERVISORY AUTHORITY, AND FINANCIAL-INCENTIVE DISCLOSURE IF APPLICABLE.]**

## 12. International processing

WiseKoin's Sentry organization is in the United States. Google and other providers may process data in countries where they or their subprocessors operate. Your data may therefore be processed outside your country and may be subject to different laws.

For transfers that require a legal safeguard, we use **[IDENTIFY APPLICABLE ADEQUACY DECISIONS, STANDARD CONTRACTUAL CLAUSES, UK ADDENDUM/IDTA, DATA PRIVACY FRAMEWORK RELIANCE, OR OTHER MECHANISM]**. Contact **[PRIVACY EMAIL]** for information about applicable safeguards. **[COMPLETE TRANSFER IMPACT/LOCALIZATION REVIEW FOR EVERY LAUNCH TERRITORY.]**

## 13. Age and children's privacy

WiseKoin is intended only for users aged 18 or older. Current Gemini terms prohibit making a Gemini API client available to people under 18, and the Service's financial and notification features are not designed for children.

The audited registration flow does not yet enforce an age gate. **An enforceable 18+ representation must be implemented before publication or production launch while Gemini remains enabled.** If you believe a person under 18 has provided personal data, contact **[PRIVACY EMAIL]** so we can investigate and take action required by applicable law.

If WiseKoin is later offered to minors, we will first change the product and this policy, establish a valid age/parental-consent process where required, and obtain specialist legal review.

## 14. Changes to this policy

We may update this policy to reflect changes to the Service, providers, security practices, or law. We will post the revised policy at **[PRIVACY POLICY URL]** and update its effective date and version.

For a material change, we will provide notice through **[EMAIL/IN-APP METHOD]** at least **[NOTICE PERIOD]** before it takes effect unless law, security, or an urgent provider change requires a different period. Where applicable law requires consent, we will request it before the relevant new processing begins.

## 15. Contact

**Controller/operator:** [OPERATOR LEGAL NAME]  
**Postal address:** [POSTAL ADDRESS]  
**Privacy email:** [PRIVACY EMAIL]  
**Support/security email:** [SUPPORT/SECURITY EMAIL]  
**Data Protection Officer:** [DPO NAME/CONTACT OR “NOT APPOINTED/NOT REQUIRED,” AFTER COUNSEL REVIEW]  
**EEA/UK representative:** [REPRESENTATIVE DETAILS IF REQUIRED]  
**Supervisory authority:** [IDENTIFY IF A LOCAL DISCLOSURE IS REQUIRED]

---

## Reviewer notes — remove before publication

1. This draft is based on the 2026-10-26 code audit in `04_PRIVACY_DATA_FLOW_AUDIT.md`. Re-run the audit against the release candidate.
2. Resolve every bracketed item and every question in `05_OPEN_PRIVACY_DECISIONS.md`. The draft is not publication-ready while placeholders remain.
3. Confirm actual production infrastructure and vendor contracts. Repository examples do not prove that Neon, a particular SMTP provider, or any stated region is in use.
4. Confirm paid Gemini status and its DPA. WiseKoin sends financial images/audio and account context; do not use unpaid processing where Google's terms permit product improvement or human review of personal/sensitive content.
5. Confirm both Sentry projects' US-region choice, retention, IP-discard setting, server-side scrubbing, DPA, user access, feedback retention, Seer setting, and deletion/export workflow.
6. Replace the Google-hosted avatar and Unsplash receipt placeholder with local assets, then remove Section 2's remote-content paragraph.
7. Add an age gate and legal-document acceptance record before launch. Current code has neither.
8. Implement and document privacy-request intake, verified export/access, processor deletion, backup aging, and response tracking.
9. Add scheduled OTP cleanup, defined structured-AI/notification-metric retention, and abandoned media-cache cleanup.
10. Ensure Google Play Data safety declarations and runtime permission explanations match the final policy exactly.
11. Do not add unverified claims such as “we encrypt all data at rest,” “we never retain deleted data,” “we are GDPR compliant,” or a specific incident-notification deadline without operational and legal support.
12. Have qualified privacy counsel review the final policy, legal bases, transfer mechanisms, children/age position, consumer disclosures, and rights supplements for every launch jurisdiction before publication. This draft is a technical/legal drafting aid, not legal advice.

