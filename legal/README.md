# WiseKoin legal drafting package

Prepared from the backend repository at `D:\Projects\WiseKoin` and the Android repository at `D:\AndroidStudioProjects\WiseKoin` on 2026-10-26.

## Terms of Service package

1. [Product and terms audit](01_PRODUCT_TERMS_AUDIT.md) — code-backed findings, limits, third parties, and launch risks.
2. [Open terms decisions](02_OPEN_TERMS_DECISIONS.md) — facts that code cannot establish and questions for the operator.
3. [Terms of Service draft](03_TERMS_OF_SERVICE_DRAFT.md) — a publication-form draft with bracketed placeholders for unresolved business and legal choices.

## Privacy package

Read these files in order:

1. [Privacy and data-flow audit](04_PRIVACY_DATA_FLOW_AUDIT.md) — evidence-backed data inventory, storage, recipients, telemetry, safeguards, retention, deletion, and user controls across both repositories.
2. [Open privacy decisions](05_OPEN_PRIVACY_DECISIONS.md) — production, vendor, jurisdiction, retention, rights, and contact facts that code cannot establish.
3. [Privacy Policy draft](06_PRIVACY_POLICY_DRAFT.md) — a publication-form draft limited to implemented processing, with bracketed placeholders and reviewer notes for unresolved matters.

The drafts are intentionally limited to the product that is implemented. They do not add subscriptions, in-app purchases, advertising, social posting, bank transfers, investment services, or other features not found in the repositories.

## Publication gate

Do not publish either draft until every bracketed placeholder is resolved, the two documents are reconciled, and the registration flow presents the Terms and Privacy Policy through an enforceable acceptance mechanism. The current Android registration flow contains no legal-document acceptance control or age gate.

The privacy package additionally requires confirmation of actual production hosting/database/email/backup vendors, Gemini paid-service and DPA status, Sentry retention and privacy settings, deletion/export procedures, launch territories, and the operator's off-code practices. Code-backed behavior cannot answer those questions.

This package is a drafting aid, not legal advice. Qualified counsel in every launch jurisdiction should review the final documents and the Gemini/Sentry data-processing configuration before release.
