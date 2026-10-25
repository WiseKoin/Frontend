# WiseKoin open Terms decisions

Code cannot answer the questions below. The draft Terms use bracketed placeholders or the expressly labeled interim assumptions in this file. Resolve every **publication blocker** before release.

## Answers requested from the operator

| Priority | Decision | Current draft treatment | What to provide |
|---|---|---|---|
| Publication blocker | Contracting party | Placeholder; no entity is inferable from code | Exact legal name, entity type, registration number if customarily disclosed, country/state of formation, and postal address |
| Publication blocker | Effective date | Placeholder | Intended publication/effective date and version number |
| Publication blocker | Contact | Placeholders | Support/legal email and notices address; identify a privacy contact if different |
| Publication blocker | Privacy Policy | Placeholder | Public Privacy Policy URL and the date/version that will accompany these Terms |
| Publication blocker | Eligibility | Draft requires users to be at least 18 while Gemini is enabled | Confirm 18+ only. If minors must be supported, disable/replace Gemini and obtain specialist child-privacy advice before changing this provision |
| Publication blocker | Gemini use case | Flagged, not assumed compliant | Confirm with Google/provider counsel that a consumer-facing personal-finance client is permitted under the then-current Gemini terms |
| Publication blocker | Gemini plan and region | Assumes an appropriately contracted paid Gemini service will be used for production | Confirm the Cloud project has active billing, the applicable DPA is in place, and every launch territory is supported; specify any excluded countries |
| Publication blocker | Sentry processing | Described functionally; retention/region not promised | Confirm Sentry organization region, event/trace retention, feedback retention, DPA status, and account-deletion workflow for linked feedback |
| Publication blocker | Governing law and forum | Placeholders | Chosen country/state law and exclusive courts, with consumer-law carve-outs where required |
| Publication blocker | Dispute model | No arbitration assumed | Choose ordinary courts, arbitration, or another process; provide rules, seat, language, fee allocation, opt-out, and class-action position if arbitration is chosen |
| Publication blocker | Warranty posture | Conservative “as available/as is” language with mandatory-law savings | Confirm the operator wants the broadest lawful disclaimer and identify any promised SLA, support response, availability, backup, or accuracy commitment |
| Publication blocker | Liability cap | Placeholder | Choose a cap and currency. For a free consumer service, consider a fixed floor rather than a zero-fee cap; local counsel should test enforceability |
| Publication blocker | Operator termination | Placeholder because no suspension mechanism exists | State grounds, notice, cure/appeal, effect on data, and emergency/security exceptions; implement the promised workflow |
| Important | Pricing/refunds | Draft says the audited version is free and contains no purchase mechanism | Confirm launch is entirely free. If not, provide prices, currency, billing party, taxes, renewal/cancellation mechanics, free-trial terms, refund policy, and store rules; implement checkout before adding paid clauses |
| Important | IP owner and app license | Operator ownership is a placeholder; narrow user license proposed | Identify the owner of code, UI, brand, documentation, and operator content; confirm contributor assignments and intended proprietary/open-source distribution |
| Important | Feedback rights | Users retain ownership; operator receives a limited right to review/respond/use feedback to improve WiseKoin without compensation | Confirm this commercial treatment or specify another one |
| Important | Indemnity | Draft limits indemnity to users acting for a business; no consumer indemnity is assumed | Confirm whether business use is allowed and whether counsel wants a business-only indemnity |
| Important | Account deletion/retention | Active database deletion is described; backups, legal holds, Sentry, Google, and SMTP retention are deferred to Privacy Policy | Provide backup schedule, deletion SLAs, legal-retention exceptions, and processor deletion procedures |
| Important | Data export | No export right is promised because none is implemented | Decide whether to build/export data or handle access/portability requests manually; reflect the result in Privacy Policy |
| Important | Change notice | Placeholder for material-change notice period/method | Choose email/in-app notice method and advance-notice period, subject to governing law |
| Important | Launch territories/languages | No territory list or controlling-language clause assumed | List launch countries and authoritative Terms language; arrange local-law review and translations |
| Important | SMTP/database/hosting vendors | General processor wording only | Identify the actual production email, database, hosting, and backup vendors for the Privacy Policy/vendor review |
| Product decision | Financial positioning | Draft says ledger/planning only, not bank/payment/lending/advice | Confirm WiseKoin will not hold funds, initiate external transfers, link bank credentials, underwrite credit, or give personalized financial advice at launch |
| Product decision | Recurring expenses | Disclosed as internal automatic entries “where available” | Confirm whether the backend API is public/productized before Android supports its management UI |
| Product decision | Bank-notification brands | Draft disclaims affiliation/endorsement | Confirm use of brand names has been reviewed and whether feature availability should be described by country/device |

## Interim assumptions used in the draft

These are drafting assumptions, not facts established by code:

1. `[OPERATOR LEGAL NAME]` operates WiseKoin and has all rights necessary to license the app and brand.
2. The production service is free to end users. No payment information is collected for WiseKoin fees.
3. Users must be at least 18 while Google Gemini powers receipt/voice extraction.
4. Production receipt/voice extraction will use a paid Gemini service and suitable data-processing terms, not unpaid quota.
5. WiseKoin is for personal bookkeeping and planning, not regulated financial services or professional advice.
6. Users own their content. The operator receives only a service-purpose license plus a narrow right to use feedback to improve the product.
7. No service-level agreement, guaranteed support response, guaranteed backup, or guaranteed availability is offered.
8. Ordinary courts—not arbitration—remain the default unless the operator instructs otherwise. Governing law and forum remain blank.
9. No broad consumer indemnity is imposed. A business-only indemnity is bracketed for counsel review.
10. Mandatory consumer rights override conflicting disclaimers, liability limitations, venue terms, and change mechanisms.

## Required implementation work before publication

The following is not solved by drafting text:

- Add links to the Terms and Privacy Policy on registration and an unchecked acceptance control. Store user ID, document versions, timestamp, and acceptance source.
- Add an 18+ representation or equivalent age gate while Gemini is enabled.
- Ensure receipt/voice AI calls cannot use an unpaid Gemini project in production; verify supported regions and provider contract/DPA.
- Publish a Privacy Policy aligned with actual Sentry, Gemini, database, hosting, SMTP, Android notification, media, backup, and deletion behavior.
- Decide and implement operator-side suspension/termination if the final Terms reserve that power.
- Establish a procedure for access, correction, deletion, and portability requests, including Sentry feedback and processor-side data.
- Add visible Terms/Privacy access from the profile/help area and a material-update notice mechanism.

## Fastest path to a publishable version

Reply with these eight items first:

1. legal operator name and address;
2. support/legal email;
3. governing law and chosen courts;
4. confirmation of “18+ only”;
5. confirmation that the app is free;
6. liability cap and currency;
7. material-change notice period; and
8. confirmation of paid Gemini, Sentry region/retention, and Privacy Policy URL.

Qualified counsel should review the answers and final document for every launch jurisdiction.
