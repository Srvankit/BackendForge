# Gumroad Launch Checklist: BackendForge

> **Document Status**: Operational Launch Runbook & Merchant Verification Checklist
> **Application Version**: Candidate 1.0.1
> **Product Brand**: The Creator Co.
> **Platform**: Gumroad (Seller Location: India)
> **License Status**: NOT FINALIZED — PRELIMINARY DRAFT (Requires Legal Signoff)

---

## Strict Security Warning & Repository Hygiene

> [!CAUTION]
> **NEVER COMMIT SENSITIVE PERSONAL OR FINANCIAL DATA TO GIT**:
> Under no circumstances may the repository owner commit government identity documents, passport scans, Aadhaar numbers, PAN cards, GSTIN certificates, bank account numbers, IFSC codes, Gumroad API tokens, or tax returns to this Git repository.
> All financial, identity, and tax data must be configured directly within the secure portals of Gumroad, Stripe, and the Indian GST portal.

---

## Pre-Launch Readiness Matrix

```
[ ] Phase 1: Gumroad Account & Genuine Legal Identity Verification
[ ] Phase 2: Indian Payout Rail & Currency Configuration
[ ] Phase 3: Indian Tax, GST & Cross-Border Invoicing Compliance
[ ] Phase 4: Legal Review & Commercial License Finalization
[ ] Phase 5: Technical Packaging & Customer Archive Verification
[ ] Phase 6: Gumroad Product Listing Creation & Asset Upload
[ ] Phase 7: Test Purchase & Buyer Download Flow Verification
[ ] Phase 8: Publishing & Live Operations
```

---

## Phase 1: Gumroad Account & Seller Identity Verification

- [ ] **1.1. Account Registration & Security**:
  - Sign up or log in to the official [Gumroad Dashboard](https://app.gumroad.com/).
  - Enable Two-Factor Authentication (2FA) on the account using an authenticator app (TOTP) or SMS.
- [ ] **1.2. Public Brand Configuration**:
  - Set the public-facing profile name to **The Creator Co.**.
  - Configure public support email or help URL (e.g., `support@thecreatorco.example`).
  - *Note*: Ensure the profile clearly reflects that "The Creator Co." is the product brand, without falsely claiming registered corporate status unless legal incorporation is officially complete.
- [ ] **1.3. Genuine Legal Seller Identity**:
  - In Gumroad Settings -> Payouts, supply your genuine individual legal name (or registered partnership/LLP/private limited company name).
  - Enter your verifiable residential or registered business address in India.
  - Complete the mandatory identity verification flow (government photo ID upload) when prompted by Gumroad/Stripe.

---

## Phase 2: Indian Payout Rail & Currency Configuration

- [ ] **2.1. Payout Method Setup**:
  - Connect an eligible Indian payout rail supported by Gumroad (e.g., Stripe Express connect for Indian creators or direct bank transfer).
  - Provide an eligible Indian bank account (Account Number and IFSC code) capable of receiving foreign inward remittances.
- [ ] **2.2. Currency & Pricing Settings**:
  - Set the customer listing price in **USD** (recommended for global developer tools; e.g., $79 USD or $99 USD).
  - Confirm payout conversion settings: Gumroad collects in USD and remits payments in INR to your connected Indian bank account via its payout processor.
  - Note applicable foreign exchange (FX) conversion fees charged by payment processors or your beneficiary bank.
- [ ] **2.3. Platform Fee & Payout Schedule Review**:
  - Review Gumroad's standard fee structure: **10% flat platform fee** per sale plus credit card / payment processing fees (~2.9% + $0.30).
  - Confirm the payout schedule (Gumroad typically processes payouts weekly on Fridays for balances exceeding the $10 USD threshold).
  - Review holding periods for initial accounts or anti-fraud reserves.

---

## Phase 3: Indian Tax, GST & Cross-Border Invoicing Compliance

- [ ] **3.1. Professional Tax Consultation**:
  - Consult a qualified Indian Chartered Accountant (CA) or tax professional before commencing commercial sales.
- [ ] **3.2. GST Registration Determination**:
  - Determine whether GST registration is mandatory for your volume. In India, export of services is treated as an inter-state supply; while service providers generally have a ₹20 Lakh turnover threshold, many financial/export compliance procedures require GST registration.
- [ ] **3.3. Zero-Rated Export via Letter of Undertaking (LUT)**:
  - If GST registered, file **Form GST RFD-11 (Letter of Undertaking - LUT)** on the GST portal (`gst.gov.in`) for the active financial year prior to exporting digital goods/services.
  - Filing an LUT allows you to export digital software to overseas buyers at **0% IGST without payment of integrated tax**.
  - Ensure you receive and retain Foreign Inward Remittance Certificates (FIRC) or electronic Foreign Inward Remittance Statements (e-FIRS) from your bank for all export remittances.
- [ ] **3.4. Domestic Indian Sales Compliance**:
  - If selling to purchasers residing in India, domestic GST (18%) must be charged and remitted if you are GST-registered.
- [ ] **3.5. Merchant of Record (MoR) Review**:
  - Review how Gumroad handles international sales tax, EU VAT, and UK VAT as Merchant of Record. Verify how Gumroad invoices international customers versus your domestic tax reporting obligations in India.

---

## Phase 4: Legal Review & Commercial License Finalization

- [ ] **4.1. Legal Counsel Review of `LICENSE.md`**:
  - Submit the draft [`LICENSE.md`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/LICENSE.md) to a licensed commercial/IP attorney.
  - Confirm that the **unlimited-project license grant** (Section 2) accurately protects your intellectual property while giving customers the right to build unlimited personal and client projects.
  - Ensure the **starter-kit resale and redistribution prohibition** (Section 3) is legally enforceable.
  - Finalize the **governing law and dispute venue** clause in Section 8 (designating the appropriate Indian court jurisdiction).
  - Finalize the **warranty disclaimer and monetary liability cap** in Section 7.
- [ ] **4.2. Finalize Digital Goods Refund Policy**:
  - Define the explicit refund policy for digital source code (e.g., all sales final once the archive is downloaded, with exceptions only where legally mandated by local consumer protection statutes).
  - Incorporate the approved policy into Gumroad product settings and [`LICENSE.md`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/LICENSE.md) Section 5.
- [ ] **4.3. Finalize Technical Support Scope**:
  - Define the exact support commitment (e.g., email bug reports, community GitHub discussions, or no personalized 1-on-1 technical consulting).
- [ ] **4.4. Transition License from Draft to Active**:
  - Once reviewed and approved by legal counsel, remove the `STATUS: NOT FINALIZED` draft banner from [`LICENSE.md`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/LICENSE.md) and replace bracketed placeholders with official details.

---

## Phase 5: Technical Packaging & Customer Archive Verification

- [ ] **5.1. Verify Test Suite & Build Cleanliness**:
  - On a clean working tree with Docker running, execute:
    ```powershell
    .\mvnw.cmd clean test
    ```
    Confirm that all 98 unit and Testcontainers integration tests pass with 0 errors and 0 failures.
- [ ] **5.2. Confirm Product Version and Reference**:
  - Confirm target version: `1.0.1`.
  - Confirm archive filename: `BackendForge-v1.0.1.zip`.
- [ ] **5.3. Package Customer Source Archive**:
  - Run the automated, leak-proof packaging script targeting the reviewed candidate commit SHA:
    ```powershell
    .\scripts\package-source.ps1 -ReleaseRef "<commit-sha>" -ArchiveName "BackendForge-v1.0.1.zip" -ExpectedVersion "1.0.1"
    ```
  - Verify that the output archive is created at `target/dist/BackendForge-v1.0.1.zip`.
- [ ] **5.4. Run Package Audit Verification**:
  - Audit the generated archive using the verification script:
    ```powershell
    .\scripts\verify-package.ps1 -ZipPath "target/dist/BackendForge-v1.0.1.zip"
    ```
  - Confirm that the audit reports:
    - `CRITICAL FILES CHECK: PASSED` (`LICENSE.md`, `THIRD-PARTY-NOTICES.md`, `README.md`, `pom.xml`, wrappers present).
    - `FORBIDDEN FILES CHECK: PASSED` (0 secrets, 0 `.env` files, 0 build artifacts, 0 Git databases, 0 IDE files).
    - `AUDIT RESULT: VERIFIED`.
- [ ] **5.5. Record Cryptographic Hash**:
  - Calculate and record the SHA-256 checksum of the customer ZIP:
    ```powershell
    Get-FileHash -Algorithm SHA256 "target/dist/BackendForge-v1.0.1.zip"
    ```
  - Retain this checksum to display on the customer post-purchase download page.

---

## Phase 6: Gumroad Product Listing Creation & Asset Upload

- [ ] **6.1. Create Product in Gumroad**:
  - Navigate to Gumroad -> Products -> New Product.
  - Type: **Digital product / Software**.
  - Name: **BackendForge — Production-Ready Spring Boot REST API Starter Kit**.
  - Price: Set agreed price in USD (e.g., $79 USD).
- [ ] **6.2. Populate Listing Description**:
  - Copy the structured, professional listing copy directly from [`docs/GUMROAD-PRODUCT-LISTING-DRAFT.md`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/docs/GUMROAD-PRODUCT-LISTING-DRAFT.md).
  - Verify that the listing explicitly states:
    - Software starter kit deliverable, NOT a hosted service.
    - Java 21 and Docker prerequisites.
    - Unlimited-project commercial license terms.
    - Perpetual use of version 1.0.1, with future updates sold separately.
    - Digital download refund policy.
- [ ] **6.3. Upload Visual Assets**:
  - Upload high-resolution cover image and product icon showcasing architecture, tech stack (Spring Boot 3.5.6, Java 21, PostgreSQL, Docker, Swagger UI), and clean code.
- [ ] **6.4. Upload Customer Deliverable**:
  - Under the "Content" tab, manually upload the verified `BackendForge-v1.0.1.zip` archive.
  - In the customer receipt/download instructions, provide the SHA-256 checksum, link to the setup guide, and instructions for running `./mvnw clean test`.

---

## Phase 7: Pre-Launch Testing & Purchase Flow Verification

- [ ] **7.1. Test Purchase Execution**:
  - Gumroad provides a "Test" mode, or you can create a 100% off discount coupon code restricted to 1 use for testing.
  - Complete the checkout process as an external buyer would.
- [ ] **7.2. Inspect Customer Receipt & Delivery**:
  - Verify that the customer email receipt arrives promptly with correct branding (The Creator Co.).
  - Verify that the receipt contains the download button and customer order details.
- [ ] **7.3. Verify Downloaded Package**:
  - Download the ZIP from the receipt link.
  - Verify SHA-256 hash matches the pre-upload record.
  - Unpack the archive into a fresh temporary directory.
  - Confirm that `LICENSE.md`, `THIRD-PARTY-NOTICES.md`, `README.md`, and `.env.example` are present.
  - Confirm that no `.git`, `.env`, or build directories exist in the extracted folder.
- [ ] **7.4. Verify Fresh Build from Downloaded Package**:
  - In the extracted folder, run:
    ```bash
    ./mvnw clean test
    ```
  - Verify that the test suite runs and passes cleanly on the downloaded package.

---

## Phase 8: Publishing & Live Operations

- [ ] **8.1. Final Sanity Check**:
  - Verify all legal blockers in [`docs/COMMERCIAL-RELEASE-BLOCKERS.md`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/docs/COMMERCIAL-RELEASE-BLOCKERS.md) have been signed off.
  - Verify Gumroad payout status shows "Active" or "Ready for Payouts".
- [ ] **8.2. Publish Product**:
  - In the Gumroad product editor, click **Publish and continue**.
  - Obtain the live shareable URL (e.g., `https://thecreatorco.gumroad.com/l/backendforge`).
- [ ] **8.3. Post-Launch Monitoring**:
  - Monitor Gumroad analytics and conversion rates.
  - Monitor incoming support/inquiry emails.
  - Maintain tax records and FIRC statements for every remittance received.
