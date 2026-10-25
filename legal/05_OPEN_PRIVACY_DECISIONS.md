# WiseKoin open privacy decisions

The repositories establish product behavior, but they cannot establish the operator's identity, legal obligations, contracts, production configuration, or off-code practices. Resolve every **publication blocker** below before publishing `06_PRIVACY_POLICY_DRAFT.md` or collecting production user data.

## Answers requested from the operator

| Priority | Decision | Current draft treatment | Information required |
|---|---|---|---|
| Publication blocker | Data controller/operator | Placeholder | Exact legal name, entity type, registration number if disclosed, formation country/state, and postal address |
| Publication blocker | Privacy contact | Placeholder | Monitored privacy/support email, postal contact, and DPO/representative details if legally required |
| Publication blocker | Effective date and version | Placeholder | Publication date, policy version, and stable public URL |
| Publication blocker | Launch territories and governing privacy laws | Placeholder | Every country/region where the app will be offered; primary establishment and supervisory authority; whether EEA/UK/Swiss, California/other US-state, Vietnam, or other local supplements are needed |
| Publication blocker | Audience and age | Interim assumption: 18+ only | Confirm the app is not directed to or likely accessed by anyone under 18; implement an age gate while Gemini is enabled |
| Publication blocker | Gemini contract and data use | Conditional disclosure | Confirm the exact Cloud project, active paid-service status, DPA acceptance, model/endpoint, processing locations, supported launch regions, abuse-monitoring retention, and deletion/request process |
| Publication blocker | Sentry configuration | US region verified; other details open | Confirm event/trace/feedback retention for both projects, DPA, subprocessors, role access, IP-discard setting, default/custom scrubbing, Seer/AI setting, and data-subject deletion/export process |
| Publication blocker | Hosting and database | Neon is only the repository's production example | Identify actual application host, database vendor/plan, cloud/region, subprocessors, encryption, access model, log retention, and contractual terms |
| Publication blocker | Backups and disaster recovery | No promise made | Identify what is backed up, provider/region, encryption, schedule, retention, restore access, and how deleted-user data ages out |
| Publication blocker | Email delivery | Vendor unknown | Identify SMTP/email vendor, sending domain, region, logs/message-content retention, DPA, and deletion/request process |
| Publication blocker | Retention schedule | Only code-enforced periods are stated | Choose periods for active account/profile/finance records, structured AI history/rules/actions, server notification metrics, OTP/security records, API/server logs, support feedback, inactive accounts, backups, and legal holds |
| Publication blocker | Privacy rights workflow | No implemented workflow | State how users submit access, correction, deletion, objection/restriction, consent withdrawal, and portability requests; set identity-verification and response procedures |
| Publication blocker | Sale, targeted advertising, and disclosure practices | Code contains no ads or sale mechanism | Confirm no off-code sale, sharing for cross-context behavioral advertising, data brokerage, audience enrichment, or marketing disclosure; identify any exception |
| Publication blocker | Legal bases | Conditional section | For each launch jurisdiction, confirm contract/legitimate-interest/consent/legal-obligation bases and complete any legitimate-interest or consent assessment |
| Publication blocker | International transfers | Placeholder | Identify data origin/destination countries and applicable transfer mechanisms, such as SCCs/UK addendum or other recognized safeguards |
| Publication blocker | Automated decisions | Draft says suggestions do not make legal/significant decisions | Confirm WiseKoin is not used for credit, eligibility, employment, insurance, lending, or other legally significant automated decisions |
| Important | Neon production status | Not asserted as fact | Confirm whether Neon Cloud is actually used; if yes, provide region, retention, branch/back-up behavior, and DPA status |
| Important | Infrastructure access | Not determinable | List personnel/contractors with production and Sentry access, role controls, review cadence, and incident-response contacts |
| Important | Account deletion SLA | Active database deletion described; processors/backups excluded | Define completion time, backup aging, legal/security exceptions, processor deletion steps, and confirmation sent to the user |
| Important | Data export | No automated export exists | Decide whether to implement export or fulfill verified access/portability requests manually; specify format and scope |
| Important | Sentry telemetry choice | No in-app opt-out | Determine lawful basis by territory and whether consent/opt-out is required; if offered, implement it before promising it |
| Important | Help Center feedback | Direct to Sentry | Define retention, support access, response practice, and how feedback is located/deleted when the user did not provide email |
| Important | Remote image hosts | Audit recommends removal | Replace Google/Unsplash placeholders with local assets; if retained, approve disclosure and review their terms/privacy behavior |
| Important | Android/Google Play distribution | Not confirmed | Identify distribution channels and complete the matching Google Play Data safety/permissions declarations |
| Important | Security incident notice | No off-code process known | Define incident assessment, escalation, regulator/user notification, and contact process for applicable jurisdictions |
| Important | Law-enforcement/legal disclosures | Generic lawful-disclosure text only | Confirm process, reviewer authority, logging, challenge policy, and any transparency commitments |
| Important | Policy-change notice | Placeholder | Choose email/in-app method and advance-notice period for material changes; decide when renewed consent is required |

## Interim assumptions used only to complete the draft

These are conspicuously bracketed in the policy and are **not** code-established facts:

1. `[OPERATOR LEGAL NAME]` is the controller/business responsible for WiseKoin.
2. WiseKoin is offered only to people aged 18 or older while Gemini powers receipt and voice extraction.
3. Production Gemini calls use a paid service under an active Cloud Billing account and an applicable data-processing addendum.
4. WiseKoin does not sell personal data, share it for cross-context behavioral advertising, or use it for targeted advertising; the code supports this, but the operator must confirm off-code practices.
5. WiseKoin sends no marketing email. The code implements security/transactional email only.
6. Product data is used only to deliver, secure, support, diagnose, and improve the requested WiseKoin features, not for credit scoring or other legally significant decisions.
7. Sentry is a processor for telemetry and Help Center feedback in its verified US region; exact retention and IP handling remain blank.
8. The operator will provide a manual privacy-request channel until an automated export/request feature exists.
9. Core product records are retained for the account's life unless the user deletes an individual record or the account; all shorter/longer operational and legal periods remain to be defined.
10. No statement about encryption at rest, backup deletion, certifications, or breach notification timing is made until operational evidence exists.

## Code-backed retention facts that can be stated now

- Android notification candidates: up to 14 days and 100 items.
- Android notification parser metrics: up to 30 days locally, and removed after acknowledged upload.
- AI ingestion fingerprints/status metadata: 30 days by default, configurable in production.
- Access-token default validity: 15 minutes.
- Refresh-token default validity: 30 days; tokens rotate and can be revoked.
- OTP and post-verification token default validity: 10 minutes each.
- Android app-created receipt/audio cache: intended to be deleted after processing/reset/failure, but process death can leave a file until OS/app cleanup.
- Source receipt/audio: not persisted by the WiseKoin backend, but sent to Gemini.
- Google currently documents 55-day Gemini API abuse-monitoring retention for prompts/context/output; verify at launch.
- Structured AI results, learned rules, server notification metrics, account data, and financial records: no time-based cleanup; account deletion is the primary implemented endpoint.

## Required product and operational work

Drafting cannot substitute for these controls:

1. Add Privacy Policy and Terms links plus an unchecked acceptance control to registration; record version, user, time, and source.
2. Add and enforce an 18+ representation while Gemini is enabled.
3. Use a properly contracted paid Gemini project and document the DPA/region/retention configuration.
4. Configure and document Sentry IP discarding, default/custom data scrubbing, retention, DPA, least-privilege access, and feedback deletion.
5. Replace remote Google/Unsplash placeholder images with local packaged assets.
6. Add scheduled deletion for expired OTP challenges.
7. Adopt and implement retention for structured AI history and notification pilot metrics.
8. Add startup/periodic deletion of abandoned receipt and voice cache files.
9. Establish a verified privacy-rights process and either build export or document a manual export procedure.
10. Extend account deletion to a documented processor/back-up workflow with a deletion SLA and legal-hold rules.
11. Publish the policy in-app and at a stable URL before launch.
12. Map the final policy to app-store disclosures, records of processing, vendor DPAs, transfer assessments, incident response, and staff access procedures.

## Minimum reply needed to remove the main placeholders

Please provide:

1. operator legal name, entity type, address, and formation jurisdiction;
2. privacy/support email and any DPO/representative;
3. launch countries and intended effective date;
4. confirmation of “18+ only”;
5. actual hosting, database, backup, and SMTP vendors/regions;
6. actual retention periods for product data, logs, backups, Sentry, and feedback;
7. Gemini paid-project/DPA confirmation and Sentry DPA/IP-scrubbing confirmation;
8. whether the operator ever sells data, uses targeted advertising, or sends marketing;
9. privacy-request/export procedure and account-deletion SLA; and
10. material policy-change notice method and period.

Qualified privacy counsel should validate the answers and adapt the final policy for every launch jurisdiction.

