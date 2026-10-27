# BackendForge Commercial Release Blockers

> **Document Status**: Commercial Decision Tracker & Launch Readiness Audit
> **Classification**: Commercial, Legal, and Operational Pre-Launch Blockers
> **Application Version**: Candidate 1.0.1
> **Product Brand**: The Creator Co. (public-facing brand; unconfirmed legal entity registration)
> **Intended Sales Platform**: Gumroad (Seller Country: India)
> **License Draft Status**: NOT FINALIZED — PRELIMINARY DRAFT FOR LEGAL AND BUSINESS REVIEW ONLY

---

## Executive Summary

BackendForge is **technically verified** (clean builds, 100% passing test suite across all 98 unit and Testcontainers integration tests, hardened security configurations, verified packaging workflows, and clean artifact generation).

The repository owner has confirmed key foundational business decisions:
1. **Product Brand**: The public-facing product brand is **The Creator Co.** (Note: This is an informal commercial brand name; it is not currently claimed or verified as a registered legal entity).
2. **Seller Identity & Country**: The seller operates from **India** using their genuine legal identity. (Genuine legal name, address, and tax identifiers are intentionally omitted from Git to protect sensitive personal records and must be configured directly within the merchant platform).
3. **Licensing Model**: **Unlimited-project licensing**. Purchasers may use BackendForge to build unlimited personal, commercial, SaaS, and bespoke client projects, but are strictly prohibited from redistributing, reselling, or open-sourcing the starter kit itself as a competing standalone product.
4. **Update Entitlement**: Purchasers receive **perpetual permission to use the specific version purchased** (Candidate Version 1.0.1). Future product updates, feature upgrades, and subsequent versions are **sold separately**. There is no entitlement to free lifetime updates or ongoing maintenance.
5. **Sales Platform**: **Gumroad** is the selected sales platform. (Note: Gumroad account verification, payout integration, and payment enablement have not yet been completed by the owner).
6. **Distribution Format**: **Source-code starter kit only** (`BackendForge-v1.0.1.zip`). Decoupled third-party dependencies are fetched by the customer directly from Maven Central.

### Critical Legal Notice
While the repository owner has confirmed their business preferences, **a business decision is not legally enforceable merely because it has been selected**. The commercial software license agreement ([`LICENSE.md`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/LICENSE.md)) remains in **`STATUS: NOT FINALIZED — PRELIMINARY DRAFT FOR LEGAL AND BUSINESS REVIEW ONLY`** until formal review and approval by qualified legal counsel under applicable law, and until account verification on Gumroad is complete. BackendForge **CANNOT be commercially sold** until the open items below are resolved.

---

## Summary of Commercial Blockers

| Blocker ID | Domain | Decision & Status | Resolution Details | Remaining Open Action |
| :--- | :--- | :--- | :--- | :--- |
| **BLK-01** | Legal Identity | **OPEN (PARTIALLY RESOLVED)** | Brand confirmed as **The Creator Co.**; seller operating from **India**. Genuine legal details withheld from repository. | Owner must complete legal identity and address verification in Gumroad dashboard. |
| **BLK-02** | Licensing Model | **RESOLVED IN POLICY** *(Legal review pending)* | **Unlimited-project model** confirmed. Unlimited personal & client projects allowed; reselling the starter kit prohibited. | Legal counsel must review the revised grant and restriction language in [`LICENSE.md`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/LICENSE.md). |
| **BLK-03** | Entitlements | **RESOLVED IN POLICY** *(Legal review pending)* | **Perpetual use of purchased version** (`1.0.1`). Future updates sold separately. No free lifetime updates. | Legal counsel must review the version-entitlement clause in [`LICENSE.md`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/LICENSE.md). |
| **BLK-04** | Sales Platform & Refunds | **PARTIALLY RESOLVED** | Intended platform confirmed as **Gumroad** (India). Payout account and refund wording remain open. | Owner must connect payout account on Gumroad and establish the final digital goods refund policy. |
| **BLK-05** | Legal Jurisdiction | **OPEN** | Seller country confirmed as India. Specific governing state law and dispute court venue not selected. | Legal counsel must determine appropriate Indian governing law and dispute court venue clauses. |
| **BLK-06** | Risk & Liability | **OPEN** | AS IS disclaimer drafted; liability monetary cap and consequential damage disclaimers remain bracketed. | Legal counsel must review and approve monetary liability ceiling and disclaimer enforceability. |
| **BLK-07** | Distribution Form | **RESOLVED** | Confirmed: Source-code starter kit only (`BackendForge-v1.0.1.zip`). Third-party binaries fetched via Maven. | None. Packaging scripts and verification standards enforce source-only packaging. |
| **BLK-08** | Tax & Invoicing | **OPEN** | Seller based in India. GST registration, Letter of Undertaking (LUT) for service exports, and invoicing rules pending. | Owner must consult a qualified Indian tax professional (CA) regarding GST, LUT filing, and Gumroad invoicing. |

---

## Detailed Blocker Specifications

### BLK-01: Seller Legal Entity and Account Verification
- **Status**: OPEN (Partially Resolved in Business Policy)
- **Confirmed Decisions**: Brand name is **The Creator Co.** (public-facing, unconfirmed registration). Seller operates from **India** under their genuine legal identity.
- **Why It Matters**: A software license without an identifiable licensor cannot legally transfer rights, enforce restrictions, or protect intellectual property. Furthermore, Gumroad requires real legal identity verification before enabling payouts.
- **Security Rule**: Personal legal names, residential addresses, bank account numbers, IFSC codes, PAN, and GSTIN numbers must **NEVER** be committed to this Git repository.
- **Required Resolution**: The owner must supply real legal identity, residential/business address, and government tax identifiers directly inside the Gumroad merchant verification dashboard. In [`LICENSE.md`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/LICENSE.md), explicit placeholders (`[Licensor Legal Entity / Individual Name]`, `[Contact Email / Website]`) are retained until formally executed.

### BLK-02: Scope of License Grant (Project Allowance)
- **Status**: RESOLVED IN BUSINESS POLICY (Formal Legal Review Pending)
- **Confirmed Decision**: **Unlimited-Project Model**. Purchasers are granted the right to use BackendForge for an unlimited number of personal, commercial, SaaS, and bespoke client projects.
- **Clear Restrictions**: Purchasers are strictly prohibited from reselling, sublicensing, redistributing, or publicly hosting the original starter kit or its core scaffolding as a competing standalone template, boilerplate, or code generator.
- **Required Resolution**: The draft license ([`LICENSE.md`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/LICENSE.md) Section 2) has been updated to reflect the unlimited-project grant. Legal counsel must review the phrasing to confirm that client project handover rights and starter kit resale prohibitions are mutually clear and enforceable.

### BLK-03: Update Entitlement and Version Support
- **Status**: RESOLVED IN BUSINESS POLICY (Formal Legal Review Pending)
- **Confirmed Decision**: **Perpetual Version License; Future Updates Sold Separately**.
  - The purchaser receives perpetual permission to use Candidate Version 1.0.1.
  - Future feature updates, major version releases, or architectural overhauls are **sold separately**.
  - No free lifetime updates, ongoing maintenance, or bespoke technical support are promised or implied.
- **Required Resolution**: The draft license ([`LICENSE.md`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/LICENSE.md) Section 5) has been updated to explicitly distinguish perpetual use of the purchased release from separate commercial entitlement to future releases. Legal counsel must confirm the wording.

### BLK-04: Digital Marketplace and Refund Terms
- **Status**: PARTIALLY RESOLVED (Platform Confirmed; Policy & Verification Pending)
- **Confirmed Decision**: **Gumroad** is the designated sales platform; seller country is **India**.
- **Open Items**:
  - Gumroad account creation and identity verification are not yet complete.
  - Indian payout account (Stripe Express / bank payout) is not yet connected.
  - Payment processing has not yet been enabled.
  - Digital goods refund policy wording remains open.
- **Why It Matters**: Because software source code is downloadable and irrevocable, software vendors typically apply a "no refund once downloaded" policy, subject to statutory consumer protection laws. Gumroad platform guidelines and merchant terms must be adhered to.
- **Required Resolution**: The owner must configure the Gumroad account, connect the payout account, and decide on the explicit refund policy (e.g., all sales final on downloadable source code, except where prohibited by mandatory local consumer protection law).

### BLK-05: Governing Law and Forum Selection
- **Status**: OPEN (Awaiting Legal Counsel Review)
- **Current State**: Placeholders in [`LICENSE.md`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/LICENSE.md) Section 8 (`[Governing Law: Laws of India / State of [State], India]`, `[Exclusive Venue: Courts of [City/State], India]`).
- **Why It Matters**: Because the seller operates from India, disputes should ideally be governed by Indian commercial law and adjudicated in an Indian court with appropriate jurisdiction. Without an explicit choice of law and venue clause, litigation could occur in unpredictable international jurisdictions.
- **Required Resolution**: Licensed legal counsel must designate the appropriate Indian state jurisdiction and court venue.

### BLK-06: Limitation of Liability and Damages Cap
- **Status**: OPEN (Awaiting Legal Counsel Review)
- **Current State**: Placeholders in [`LICENSE.md`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/LICENSE.md) Section 7 (`[MONETARY CAP, e.g., total amount paid by Licensee for the Software, or $100 USD]`).
- **Why It Matters**: Without an enforceable liability cap, software authors risk unbounded liability if downstream client systems suffer data breaches or outages.
- **Required Resolution**: Legal counsel must review the "AS IS" warranty disclaimer and aggregate liability cap to ensure maximum enforceability under Indian law (e.g., Indian Contract Act, 1872) and cross-border commercial norms.

### BLK-07: Confirmation of Distribution Format (Source Kit Only)
- **Status**: RESOLVED (Verified & Enforced)
- **Confirmed Decision**: BackendForge is distributed strictly as a **source-code starter kit ZIP** (`BackendForge-v1.0.1.zip`).
- **Open-Source Compliance Rationale**: Distributing source code means all third-party dependencies (including LGPL-2.1 Hibernate ORM and EPL Logback) are downloaded directly from Maven Central by the customer during local build execution. This avoids complex binary redistribution obligations (such as LGPL object relinking mandates).
- **Enforcement**: Automated packaging scripts ([`scripts/package-source.ps1`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/scripts/package-source.ps1) and [`scripts/package-source.sh`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/scripts/package-source.sh)) and verification scripts enforce clean source packaging from explicit Git references.

### BLK-08: Tax, GST, LUT, and Cross-Border Invoicing
- **Status**: OPEN (Requires Professional Tax Review)
- **Context**: The seller is located in India and selling digital products globally via Gumroad.
- **Why It Matters**:
  - Export of software services from India qualifies for zero-rated GST if a Letter of Undertaking (LUT) is filed on the GST portal before export, or IGST is paid and refunded.
  - Domestic Indian sales may attract 18% GST if the seller crosses the statutory threshold (or registers voluntarily).
  - Gumroad acts as Merchant of Record (MoR) for certain jurisdictions, collecting sales tax/VAT internationally, but the Indian seller must understand their domestic tax reporting obligations.
- **Required Resolution**: The owner must consult a qualified Indian Chartered Accountant (CA) or tax professional prior to enabling commercial sales.

---

## Action Plan for Commercial Launch

1. **Owner Execution**:
   - Create and verify the Gumroad seller account using genuine Indian legal identity and address.
   - Connect an eligible Indian bank/payout account on Gumroad.
   - Consult an Indian tax advisor regarding GST registration and filing Form GST RFD-11 (LUT) for zero-rated exports.
2. **Legal Review**:
   - Engage qualified legal counsel to review [`LICENSE.md`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/LICENSE.md) (governing law, dispute venue, liability cap, refund terms).
   - Sign off on final commercial terms.
3. **License Finalization**:
   - Update [`LICENSE.md`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/LICENSE.md) from `STATUS: NOT FINALIZED` to active commercial license upon legal approval.
4. **Product Listing & Packaging**:
   - Finalize the listing copy using [`docs/GUMROAD-PRODUCT-LISTING-DRAFT.md`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/docs/GUMROAD-PRODUCT-LISTING-DRAFT.md).
   - Follow the launch steps in [`docs/GUMROAD-LAUNCH-CHECKLIST.md`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/docs/GUMROAD-LAUNCH-CHECKLIST.md).
   - Package and verify `BackendForge-v1.0.1.zip` using the repository verification scripts.
   - Upload the verified ZIP to Gumroad and perform a test purchase.
