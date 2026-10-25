# WiseKoin Terms of Service

**Publication status:** Draft—do not publish until all bracketed items are resolved  
**Effective date:** [EFFECTIVE DATE]  
**Version:** [VERSION]

These Terms of Service (the **“Terms”**) are an agreement between you and **[OPERATOR LEGAL NAME, ENTITY TYPE, AND JURISDICTION]**, located at **[POSTAL ADDRESS]** (**“WiseKoin,” “we,” “us,”** or **“our”**). They govern your access to and use of the WiseKoin Android application, related application programming interfaces, and services we make available under the WiseKoin name (collectively, the **“Service”**).

Please read these Terms carefully. By creating an account, selecting “I agree,” or otherwise accessing or using the Service after being shown these Terms, you agree to them. If you do not agree, do not create an account or use the Service.

Our Privacy Policy, available at **[PRIVACY POLICY URL]**, explains how we collect, use, disclose, retain, and protect personal data. It is separate from these Terms.

## 1. Eligibility

You may use the Service only if:

- you are at least 18 years old;
- you have legal capacity to enter into these Terms;
- you are not prohibited from using the Service under applicable law; and
- you use the Service only in a country where it and its relevant third-party services are available.

The 18-year minimum applies while the Service uses Google Gemini features under provider terms that prohibit making an API client available to people under 18. If you use the Service for an organization, you represent that you have authority to bind that organization, and “you” includes the organization.

## 2. What WiseKoin provides

WiseKoin is a personal recordkeeping and planning tool. Depending on the features available in your version, the Service may let you:

- maintain internal wallet balances and records of income, expenses, and transfers;
- organize categories, savings goals, debts, and repayment or funding records;
- view summaries and reports;
- schedule recurring expenses that automatically create entries in your WiseKoin ledger;
- ask automated tools to extract proposed transaction details from a receipt image or voice recording;
- optionally identify possible transaction records from supported finance-app notifications on your Android device; and
- send support questions, bug reports, or ideas through the Help Center.

WiseKoin records do not represent money held by us. The Service does not open or operate bank accounts, hold funds, initiate transfers at a financial institution, process payments, lend money, collect debts, trade securities, or verify your records against a bank statement. References to a “wallet,” “transfer,” “payment,” “debt,” or similar term describe your records inside WiseKoin unless we clearly say otherwise.

## 3. Accounts and security

You must provide accurate, current account information and keep it updated. You are responsible for:

- keeping your password, email account, device, verification codes, and session credentials secure;
- all activity performed through your account or device, except to the extent caused by our breach of these Terms or duties imposed by law;
- promptly changing your password and contacting us at [SUPPORT/SECURITY EMAIL] if you suspect unauthorized access; and
- maintaining access to the email address used for security verification and password recovery.

Do not share a verification code or allow another person to use your account. We may require authentication, current-password verification, or an emailed one-time code before sensitive actions. Sessions and verification codes expire, and the Service may require you to sign in again.

## 4. Your financial records and automated entries

You control the financial information you enter and the actions you confirm. You must review your records for accuracy and correct errors promptly.

If you create a recurring expense, the Service may automatically create an internal expense entry on the schedule you choose and adjust the selected internal wallet balance. This does not make a real-world payment. You remain responsible for disabling or changing schedules you no longer want and for meeting any actual payment obligation outside WiseKoin.

WiseKoin may prevent an entry when an internal wallet would have an insufficient balance or when a record conflicts with another record. These controls are bookkeeping safeguards only; they do not establish your actual financial position.

## 5. Your content

**“User Content”** means information or material you submit to or create through the Service, including profile information, wallet or category names, transaction and recurring-entry details, notes, goal and debt information, receipt images, voice recordings, notification-derived transaction facts, and Help Center feedback.

### Ownership

As between you and us, you retain ownership of your User Content. These Terms do not transfer ownership of it to us.

### Permission to operate the Service

You grant us a non-exclusive, worldwide, royalty-free license to host, reproduce, transmit, format, analyze, display, and otherwise process User Content only as reasonably necessary to:

- provide the features you request;
- maintain, secure, troubleshoot, and support the Service;
- identify duplicates, measure feature reliability, and improve WiseKoin's extraction or categorization features; and
- allow our contracted service providers to perform those activities for us.

This license is sublicensable only to service providers and other recipients described in our Privacy Policy and only for those purposes. It lasts while the content is retained for those purposes, except that processor rights or retention may continue where their applicable terms or law require. Account deletion and retention are addressed in Section 13 and the Privacy Policy.

### Your responsibilities for content

You represent that you have the rights and permissions needed to submit User Content and allow the processing described in these Terms and the Privacy Policy. Do not upload or record content unlawfully, violate another person's privacy or intellectual-property rights, or submit material you are not entitled to use.

Do not include passwords, security codes, full bank-account or card numbers, government identifiers, or unnecessary sensitive information in transaction notes, audio, receipt images, or feedback. Use the receipt and voice features only with material you are authorized to process.

### Feedback

If you send a bug report, idea, question, or other feedback, you authorize us to review it, respond to it, and use it to maintain and improve WiseKoin without payment or an obligation to implement it. You retain any ownership you otherwise have in the text you submit. A feedback submission does not guarantee a response.

## 6. AI-assisted receipt and voice extraction

Receipt and voice extraction uses automated systems, currently including Google Gemini, to propose transaction details. To provide a result, the Service may send the selected image or recording, an extraction prompt, locale and time-zone information, base currency, and relevant category, wallet, or learned categorization context to the AI provider. See our Privacy Policy for the data-processing details.

AI output is probabilistic. It may be incomplete, inaccurate, duplicated, outdated, or unsuitable, even when accompanied by a confidence label. Confidence labels may be uncalibrated and are not guarantees. You must review the amount, currency, date, type, wallet, category, merchant, and every other field before confirming a suggestion.

AI output is provided for bookkeeping convenience only. It is not financial, investment, tax, accounting, legal, credit, or other professional advice. Do not rely on it to make a payment, meet a deadline, prepare an official filing, or make a consequential financial decision without checking authoritative records and, where appropriate, a qualified professional.

An AI result does not create a WiseKoin transaction unless you confirm it. If you ask WiseKoin to remember a category correction, the Service may use that confirmation to create a rule for future suggestions in your account.

You must not use the AI features to violate law, another person's rights, these Terms, the [Google APIs Terms of Service](https://developers.google.com/terms), the [Gemini API Additional Terms](https://ai.google.dev/gemini-api/terms), or applicable Google prohibited-use policies. Provider availability, safety controls, quotas, and output restrictions may limit or block an extraction.

## 7. Optional finance-app notification access

On supported Android devices, you may choose to grant WiseKoin notification-listener access and enable transaction tracking. This permission can expose notification content from applications on your device to WiseKoin's local listener. WiseKoin is designed to examine only supported, allowlisted finance-app packages for this feature.

The current parser supports notifications identified as coming from VCB Digibank, MB Bank, Techcombank Mobile, and MoMo. Parsing occurs on your device. The feature is designed to retain a structured candidate—such as amount, currency, direction, source app, time, parser version, and a one-way reference hash—rather than the original notification text. Structured candidate data and aggregate parser metrics may be sent to our backend as described in the Privacy Policy.

Notification parsing may miss an event, misread an amount or direction, duplicate an event, or interpret an unrelated notification as financial activity. A notification candidate is only a suggestion and does not become a WiseKoin transaction until you review and confirm it. Always compare it with the original provider record.

You may disable tracking or revoke notification access through WiseKoin or Android settings. Local candidates expire and are limited in number under the product's current retention rules. Availability may vary by app version, finance-app notification format, device, operating system, locale, and region.

WiseKoin is independent of the named banks, wallets, and finance applications. Their names and marks belong to their respective owners. They do not sponsor, endorse, or operate WiseKoin unless expressly stated by them.

## 8. Acceptable use

You may not use, or help another person use, the Service to:

- violate applicable law, regulation, court order, or another person's rights;
- submit content you do not have the right or permission to process;
- access another person's account, notifications, device, or data without authorization;
- introduce malware or other harmful code;
- probe, scan, or test vulnerabilities without our prior written authorization;
- interfere with, disrupt, overload, or degrade the Service or its providers;
- bypass or evade authentication, rate limits, upload limits, idempotency controls, provider safety controls, or other technical safeguards;
- use automated means to extract data or send requests in a manner that exceeds documented interfaces or materially burdens the Service;
- reverse engineer the Service or its provider models, except to the limited extent applicable law prohibits this restriction; or
- use AI output as professional advice or in a high-risk activity where an error could reasonably cause personal injury, material financial harm, or violation of law.

The Service enforces technical limits, including request-rate, file-size, file-type, media-duration, duplicate-processing, queue-size, and retention limits. Current defaults may change to protect reliability, security, provider capacity, or lawful operation. We will not use a limit change to impose a fee without first disclosing and obtaining any consent required by law.

## 9. Fees

The version of WiseKoin governed by these Terms is currently offered without a subscription fee or in-app purchase. These Terms do not authorize us to charge you.

If we introduce a paid feature, we will disclose the price, currency, billing party, renewal and cancellation mechanics, applicable taxes, and refund terms before you purchase it and will request any consent required by law. New paid terms will apply only after that process; they will not retroactively create a charge.

## 10. Third-party services and software

The Service depends on third-party services and software, including:

- Google Gemini for receipt and voice extraction;
- Sentry for application monitoring, performance diagnostics, and Help Center feedback;
- Android and related Google platform services; and
- hosting, database, email-delivery, and network providers identified or described in our Privacy Policy.

These providers may process data for us under our agreements with them. When you have a direct relationship with a third party—for example, your Android platform, app store, device manufacturer, bank, wallet provider, or network provider—that relationship is governed by the third party's terms and privacy practices. We are not responsible for a third party's independent service.

Third-party availability or terms may require us to modify, limit, suspend, or discontinue a dependent feature. Open-source components included in the application remain subject to their applicable licenses.

## 11. WiseKoin intellectual property and license to you

The Service, including its software, design, documentation, and WiseKoin branding, is owned by **[IP OWNER]** or its licensors, except for User Content and third-party components.

Subject to these Terms, we grant you a limited, personal, non-exclusive, non-transferable, non-sublicensable, revocable right to install and use the object-code version of the application and access the Service for its intended recordkeeping purposes. This right does not permit you to sell, rent, distribute, publicly host, or create a competing service from WiseKoin, except where an applicable open-source license or mandatory law expressly permits the activity.

No other rights are granted. “WiseKoin” and associated branding may not be used in a way that suggests sponsorship, affiliation, or endorsement without the owner's written permission.

## 12. Privacy and communications

Our Privacy Policy describes personal-data processing, including financial records, media, notification-derived data, device/technical data, security email, AI providers, Sentry telemetry and feedback, international transfers, retention, deletion, and your privacy rights.

We may send transactional or security communications necessary to operate your account, such as password-recovery and verification emails. These are not marketing subscriptions. [DESCRIBE ANY MARKETING COMMUNICATIONS OR STATE THAT NONE ARE SENT.]

## 13. Account deletion, request restriction, and termination

### Your choice to stop using WiseKoin

You may stop using the Service at any time. Logging out removes the active session from the device but does not delete the account. To delete the account, use the in-app deletion flow and complete the required password and email-verification steps.

Account deletion is intended to permanently remove the account and its active WiseKoin database records, including wallets, transactions, goals, debts, categories, recurring entries, and account-linked AI records. It cannot undo actions already completed outside WiseKoin because the Service does not control those systems. Limited copies may remain temporarily in backups, security records, provider systems, or where retention is required by law, as described in the Privacy Policy. Help Center feedback sent to Sentry may require a separate privacy request if it cannot be reliably linked to the deleted account.

### Technical restriction

We may automatically reject or temporarily limit requests that fail authentication or validation, exceed technical limits, appear duplicated, or threaten the security or reliability of the Service or its providers.

### Operator suspension or termination

**[CONFIRM AND IMPLEMENT BEFORE PUBLICATION: We may suspend or terminate an account for a material or repeated breach of these Terms, unlawful use, security risk, or where required by law or a provider. Except where immediate action is reasonably necessary to prevent harm, comply with law, or protect the Service, we will provide [NOTICE PERIOD] notice and a reasonable opportunity to cure or appeal by contacting [APPEAL EMAIL]. Specify what happens to data and whether export is offered.]**

Sections that by their nature should survive termination—including ownership, accrued responsibilities, disclaimers, liability limits, dispute terms, and necessary content-processing rights—will survive to the extent permitted by law.

## 14. Service changes and availability

We may update the Service to add, change, or remove features, address security or legal requirements, or respond to third-party changes. We do not promise that every feature will always be available, that the Service will be uninterrupted or error-free, or that any data will always be recoverable.

We will not materially reduce a paid commitment without applying the paid terms presented at purchase. [CONFIRM WHETHER ANY AVAILABILITY, SUPPORT, OR BACKUP COMMITMENT EXISTS; NONE IS ASSUMED IN THIS DRAFT.]

## 15. Disclaimers

Nothing in these Terms excludes a warranty, guarantee, remedy, or consumer right that cannot lawfully be excluded.

To the maximum extent permitted by law, the Service and all suggestions, reports, and outputs are provided **“as is”** and **“as available.”** We disclaim implied warranties of merchantability, satisfactory quality, fitness for a particular purpose, non-infringement, and any warranty arising from course of dealing or usage of trade.

We do not warrant that:

- financial records, balances, reports, reminders, extracted fields, notification suggestions, confidence scores, or duplicate warnings are complete or accurate;
- the Service will meet your accounting, tax, legal, regulatory, or professional requirements;
- the Service will be uninterrupted, secure, or free from harmful components; or
- User Content will never be lost.

You are responsible for maintaining any independent records and backups you need and for checking WiseKoin information against authoritative sources. WiseKoin is not a substitute for a bank statement or advice from a qualified professional.

## 16. Limitation of liability

Nothing in these Terms limits or excludes liability that cannot lawfully be limited or excluded, including **[INSERT JURISDICTION-SPECIFIC NON-EXCLUDABLE LIABILITIES]**.

To the maximum extent permitted by law, neither WiseKoin nor its directors, officers, employees, contractors, or affiliates will be liable for indirect, incidental, special, consequential, exemplary, or punitive damages, or for loss of profits, revenue, business opportunity, goodwill, or data, arising out of or related to the Service, even if advised that such loss may occur.

To the maximum extent permitted by law, the aggregate liability of WiseKoin and the other parties listed above for all claims arising out of or related to the Service or these Terms will not exceed **[LIABILITY CAP AND CURRENCY—LOCAL COUNSEL TO CONFIRM A NON-ZERO FLOOR APPROPRIATE FOR A FREE CONSUMER SERVICE]**.

These limitations apply only to the extent they are fair and enforceable under the law that applies to you. [INSERT ANY REQUIRED CONSUMER, DATA-PROTECTION, GROSS-NEGLIGENCE, WILLFUL-MISCONDUCT, OR PERSONAL-INJURY CARVE-OUTS.]

## 17. Business-user indemnity

**[CONFIRM WHETHER BUSINESS USE AND THIS CLAUSE ARE DESIRED. NO CONSUMER INDEMNITY IS ASSUMED.]** If you use the Service on behalf of a business, that business will defend and indemnify WiseKoin and its directors, officers, employees, contractors, and affiliates against third-party claims, damages, and reasonable legal costs to the extent arising from the business's unlawful User Content, infringement of third-party rights, or material breach of Sections 5 or 8. This obligation does not apply to the extent a claim was caused by WiseKoin's breach, negligence, or willful misconduct.

## 18. Changes to these Terms

We may update these Terms to reflect changes to the Service, law, security requirements, or third-party dependencies. We will identify the effective date and make the revised Terms available in the Service or at [TERMS URL].

For a material change, we will provide notice by **[EMAIL/IN-APP METHOD]** at least **[NOTICE PERIOD]** before it takes effect, unless a shorter period is reasonably required by law or an urgent security/provider change. Where law requires consent, the change will not apply until you consent. If you do not agree to revised Terms, you must stop using the Service and may delete your account before they take effect.

## 19. Governing law and disputes

These Terms are governed by the laws of **[GOVERNING JURISDICTION]**, without regard to conflict-of-law rules. This choice does not deprive you of mandatory protections provided by the law of your habitual residence.

Before filing a claim, you and WiseKoin agree to try in good faith for **[30]** days to resolve it informally by sending a written description and requested resolution to the contact details in Section 21. This does not prevent either party from seeking urgent injunctive relief or using a small-claims or consumer process where available.

Subject to mandatory consumer law, the courts located in **[EXCLUSIVE COURT/VENUE]** will have exclusive jurisdiction. **[IF ARBITRATION IS CHOSEN, REPLACE THIS COURTS CLAUSE WITH COUNSEL-DRAFTED ARBITRATION TERMS, INCLUDING RULES, SEAT, LANGUAGE, COSTS, OPT-OUT, AND ANY CLASS-ACTION PROVISION.]**

## 20. General terms

These Terms and the documents expressly incorporated into them are the entire agreement about the Service, except for separate paid or enterprise terms you affirmatively accept. If a provision is unenforceable, it will be enforced to the maximum lawful extent and the remainder will continue in effect.

Our delay in enforcing a provision is not a waiver. You may not assign these Terms without our written consent. We may assign them as part of a merger, reorganization, sale of the Service, or operation of law, subject to applicable notice and consumer rights.

Neither party is liable for delay or failure caused by events beyond its reasonable control, except for obligations that cannot lawfully be excused. These Terms do not create an agency, partnership, employment, or joint-venture relationship, and they do not give enforcement rights to a person who is not a party, except where applicable law provides otherwise.

## 21. Contact

**[OPERATOR LEGAL NAME]**  
**Email:** [SUPPORT/LEGAL EMAIL]  
**Security reports:** [SECURITY EMAIL OR URL]  
**Privacy requests:** [PRIVACY EMAIL OR URL]  
**Postal address:** [POSTAL ADDRESS]

## Reviewer notes — remove before publication

- This draft assumes a free, 18+ personal-finance ledger and planning product. It does not include paid subscriptions, advertising, real-world payments, bank credential access, lending, investing, or professional advice because the audited code does not provide them.
- Confirm a paid, appropriately contracted Gemini deployment and permitted consumer-facing use. Terms language cannot cure a provider-contract mismatch.
- Prepare and link a Privacy Policy covering Gemini, Sentry, notification access, media, financial data, hosting/database/email vendors, retention, international transfers, and rights requests.
- Implement affirmative Terms/Privacy assent, age eligibility, versioned consent records, and any operator suspension/appeal process promised here.
- Resolve every bracketed placeholder, particularly legal entity, IP owner, liability cap, governing law, venue, change notice, contacts, and retention.
- Have qualified counsel review the final Terms for all launch jurisdictions before publication. This draft is not a substitute for legal advice.
