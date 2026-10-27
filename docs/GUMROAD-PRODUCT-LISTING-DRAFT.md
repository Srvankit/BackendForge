# Gumroad Product Listing Draft: BackendForge

> **Document Classification**: Pre-Launch Commercial Listing Copy
> **Status**: DRAFT FOR OWNER REVIEW — DO NOT PUBLISH UNTIL LEGAL AND ACCOUNT APPROVAL
> **Product Brand**: The Creator Co.
> **Product Version**: 1.0.1
> **Distribution Format**: Digital Download (Source Code ZIP Archive: `BackendForge-v1.0.1.zip`)
> **Sales Platform**: Gumroad
> **License Draft Status**: Preliminary Draft (Legal Counsel Review Pending)

---

## 1. Product Listing Header

* **Product Title**: BackendForge — Production-Ready Spring Boot REST API Starter Kit
* **Brand / Seller**: The Creator Co.
* **Suggested Category**: Software & Development / Developer Tools / Starter Kits & Templates
* **Version**: `1.0.1`
* **Pricing Placeholder**: `[PRICE PLACEHOLDER: e.g., $79 USD / $99 USD — To be set by owner in Gumroad dashboard]`
* **Cover Image / Thumbnail**: `[ASSET PLACEHOLDER: 1280x720 PNG / WebP displaying BackendForge architecture, Java 21, Spring Boot 3.5.6, PostgreSQL, Docker, Swagger UI]`
* **Summary / Tagline (140 characters max)**:
  > Production-hardened Java 21 & Spring Boot 3.5.6 REST API starter kit with JWT auth, PostgreSQL, Flyway, rate limiting, Docker, and Swagger.

---

## 2. Short Product Description (Elevator Pitch)

Stop wasting 40+ hours writing repetitive boilerplate for every new backend.

**BackendForge** is a production-engineered Java 21 and Spring Boot 3.5.6 REST API starter kit designed for solopreneurs, SaaS founders, agency engineers, and freelance backend developers who demand clean architecture, rigorous security defaults, and automated database migrations from day one.

Unlike basic tutorials, BackendForge gives you a battle-tested foundation: stateless HMAC-SHA256 JWT authentication with constant-time failure equalization, in-memory sliding-window rate limiting, forward-only PostgreSQL Flyway migrations, structured correlation IDs (`X-Correlation-ID`) for distributed tracing, non-root multi-stage Docker containerization, OpenAPI 3.0 / Swagger UI documentation, and a comprehensive automated test suite (98 tests, including real PostgreSQL integration tests via Testcontainers).

Download the clean source code, run `./mvnw clean test`, and start shipping your real business logic in minutes.

---

## 3. Important Customer Notice: Software Starter Kit, NOT a Hosted Service

> [!IMPORTANT]
> **DELIVERABLE NOTICE**:
> BackendForge is a **downloadable developer source-code starter kit and architectural foundation**, delivered as a clean ZIP archive (`BackendForge-v1.0.1.zip`).
> - **Self-Hosted**: You host, run, and manage your applications on your own servers or cloud infrastructure (e.g., AWS, GCP, Azure, Railway, Render, DigitalOcean, or Docker).
> - **Not a Hosted SaaS**: This purchase does **not** include managed cloud hosting, database provisioning, domain names, or recurring SaaS infrastructure.
> - **Technical Expertise Required**: You will need standard software development skills (Java 21, Maven, PostgreSQL, Docker) to customize, build, and deploy the application.

---

## 4. Target Audience: Who Is BackendForge For?

BackendForge was built specifically for developers who value time, code quality, and security:

* **SaaS Founders & Solo Developers**: Launch your backend with confidence, skipping weeks of repetitive scaffolding and security wiring.
* **Freelancers & Digital Agencies**: Build client backends rapidly on an unlimited-project license, delivering clean, professional, and well-documented codebases.
* **Java & Spring Boot Engineers**: Use a standardized, clean-architecture reference template that adheres strictly to modern Spring Boot 3 conventions and Java 21 idioms.
* **Engineering Teams**: Kickstart microservices or greenfield projects with established conventions for errors, logging, migrations, and health probes.

---

## 5. What's Included: Features & Technical Architecture

### 5.1 Core Backend & Persistence
* **Spring Boot 3.5.6 & Java 21**: Modern LTS foundation utilizing records, sealed constructs, and Spring Boot 3 Jakarta EE standards.
* **PostgreSQL & Spring Data JPA**: Fully configured persistence layer with HikariCP connection pooling, schema validation, and optimized repositories.
* **Flyway Schema Migrations**: Forward-only, repeatable database migrations (`src/main/resources/db/migration/`). Never execute ad-hoc schema modifications in production.
* **Automated Fail-Fast Production Validation**: Dedicated `ProductionConfiguration` that verifies database credentials and cryptographic JWT secrets at boot before accepting traffic.

### 5.2 Authentication & Security Hardening
* **Stateless JWT Security Filter Chain**: HMAC-SHA256 (`HS256`) access token signing and verification using `io.jsonwebtoken:jjwt` (0.12.6).
* **Strict Secret Validation**: Enforces Base64-encoded signing keys of at least 32 bytes (256 bits) with mandatory startup termination if weak or missing.
* **BCrypt Password Hashing**: Adaptive cost factor (`BCryptPasswordEncoder(12)`) for high-entropy credential storage.
* **Timing Equalization**: Registration and login endpoints execute synthetic password verifications on non-existent accounts to eliminate user enumeration through response-time variance.
* **UTF-8 Byte Boundary Validation**: Enforces BCrypt's 72-byte truncation boundary prior to hashing, rejecting silently truncated passwords.
* **Standardized Error Handling**: Centralized `GlobalExceptionHandler` and RFC-compliant `ApiError` payloads that completely prevent stack traces or database internals from leaking to API clients.

### 5.3 Reliability, Diagnostics & Documentation
* **Sliding-Window Rate Limiting**: In-memory, lock-free sliding-window limiter on sensitive endpoints (`/api/v1/users/register`, `/api/v1/users/login`) with automatic stale-entry cleanup.
* **Correlation IDs & Distributed Tracing**: `CorrelationIdFilter` captures or generates safe, sanitized UUID correlation IDs (`X-Correlation-ID`), propagates them to SLF4J MDC, and echoes them in HTTP responses.
* **OpenAPI 3.0 & Swagger UI**: Interactive API documentation automatically exposed at `/swagger-ui.html` and OpenAPI JSON at `/v3/api-docs`.
* **Spring Boot Actuator Probes**: Liveness (`/actuator/health/liveness`) and readiness (`/actuator/health/readiness`) health probes ready for Kubernetes or Docker container orchestration.

### 5.4 Containerization & Local Development
* **Multi-Stage Non-Root Dockerfile**: Production-ready container image running on Eclipse Temurin 21 JRE, dropping privileges to non-root user `appuser` (UID `10001`).
* **Docker Compose Orchestration**: Single-command local environment (`docker compose up -d`) spinning up PostgreSQL 16.4 and initializing test credentials.
* **Zero Host-Maven Requirement**: Fully bundled Maven Wrapper (`./mvnw` and `.\mvnw.cmd`) ensures reproducible builds across Linux, macOS, and Windows.

### 5.5 Comprehensive Automated Test Suite
* **98 Automated Tests**: 57 unit tests and 41 integration tests passing with zero failures.
* **Testcontainers Integration**: Real integration tests (`UserAuthenticationIntegrationTest`, `DatabaseMigrationIntegrationTest`, `RateLimitIntegrationTest`, `SecurityConfigIntegrationTest`) executing against ephemeral PostgreSQL 15 containers.

---

## 6. Included Files Manifest

When you purchase and unpack `BackendForge-v1.0.1.zip`, you receive the complete uncompiled source tree:

```text
BackendForge-v1.0.1/
├── src/
│   ├── main/
│   │   ├── java/com/thecreatorco/backendforge/
│   │   │   ├── common/           # Correlation ID filters, MDC logging, ApiError handling
│   │   │   ├── config/           # Production validator properties, OpenAPI configuration
│   │   │   ├── ratelimit/        # Sliding-window rate limiter, interceptors, properties
│   │   │   ├── security/         # SecurityConfig, JwtService, JwtAuthenticationFilter
│   │   │   └── user/             # UserEntity, UserRepository, UserService, UserController
│   │   └── resources/
│   │       ├── db/migration/     # Flyway SQL schema scripts (V1__create_users_table.sql)
│   │       ├── application.yml   # Default local development configuration
│   │       └── application-prod.yml # Strict production profile overrides
│   └── test/                     # 98 unit and Testcontainers integration tests
├── scripts/                      # Verified packaging and release audit scripts
├── docs/                         # Architecture guides, deployment runbooks, checklists
├── .mvn/wrapper/                 # Bundled Maven wrapper binaries and configuration
├── mvnw & mvnw.cmd               # Cross-platform Maven build scripts
├── pom.xml                       # Clean Maven project configuration (Java 21, Spring Boot 3.5.6)
├── Dockerfile                    # Production multi-stage non-root container configuration
├── compose.yaml                  # Local development PostgreSQL 16 orchestration
├── .env.example                  # Documented environment variable template
├── LICENSE.md                    # Commercial software license agreement
├── THIRD-PARTY-NOTICES.md        # Comprehensive open-source attribution report
└── README.md                     # Comprehensive setup, API reference, and deployment guide
```

---

## 7. System Requirements & Prerequisites

Before running BackendForge, ensure your local workstation or server meets the following:

1. **Java**: OpenJDK 21 LTS or newer (`java -version`).
2. **Container Engine**: Docker Desktop or Docker Engine (required for Docker Compose and Testcontainers integration tests).
3. **Build Tool**: Not required—the bundled Maven Wrapper (`./mvnw` / `.\mvnw.cmd`) downloads Apache Maven automatically.
4. **Database (Optional)**: A local PostgreSQL service if you choose not to use Docker Compose.
5. **Operating System**: Fully compatible with macOS (Intel & Apple Silicon), Linux (x86_64 & aarch64), and Windows 10/11 (PowerShell & WSL2).

---

## 8. Quick Setup (Under 5 Minutes)

```bash
# 1. Unpack the downloaded archive
unzip BackendForge-v1.0.1.zip
cd BackendForge-v1.0.1

# 2. Run the test suite to verify your environment (requires Docker)
./mvnw clean test

# 3. Create your local environment configuration
cp .env.example .env

# 4. Generate a secure Base64 JWT signing key
# Linux/macOS:
openssl rand -base64 32
# (Paste the generated key into JWT_SECRET inside your .env file)

# 5. Start the PostgreSQL database via Docker Compose
docker compose up -d

# 6. Launch the application locally
./mvnw spring-boot:run

# 7. Explore the interactive API documentation
open http://localhost:8080/swagger-ui.html
```

---

## 9. Commercial License Summary

BackendForge is licensed under an **Unlimited-Project Commercial Model**:

* **What You CAN Do**:
  * Create, build, and deploy an **unlimited number of personal, commercial, and SaaS applications** for your own business.
  * Build and deliver bespoke commercial applications to **unlimited consulting clients** as client deliverables.
  * Host, run, and scale your derived applications anywhere without paying royalties or per-seat fees.
  * Access, modify, refactor, and customize all provided source code.
* **What You CANNOT Do**:
  * You **cannot** resell, sublicense, rent, or redistribute BackendForge source code, scaffolding, or templates as a standalone developer starter kit, boilerplate, template, or code generator.
  * You **cannot** publish the source code to public repositories (such as public GitHub, GitLab, or Bitbucket repositories). All source repositories must remain private.
  * You **cannot** create a competing commercial starter kit or boilerplate product derived from BackendForge.
* **License Status**: The formal agreement is provided in [`LICENSE.md`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/LICENSE.md). The license is currently in preliminary draft status pending final legal counsel review.

---

## 10. Update Policy

* **Perpetual Version Use**: Your purchase grants you perpetual permission to use the purchased version (Candidate Version 1.0.1) according to the license terms.
* **Future Updates Sold Separately**: Future major releases, new architectural features, and subsequent editions are sold separately and are not included in this purchase.
* **No Maintenance Guarantee**: This purchase is for a self-hosted software starter kit and does not include guaranteed lifetime updates, ongoing maintenance, or software subscription tiers.

---

## 11. Technical Support Policy

* `[SUPPORT COMMITMENT PLACEHOLDER — To be determined and published by the seller prior to launch. E.g.: Community issue tracking / support email / no dedicated 1-on-1 support included. BackendForge is provided as an engineering foundation for professional developers; bespoke consulting, architecture reviews, and custom feature implementations are not included.]`

---

## 12. Digital Refund Policy

* `[REFUND POLICY PLACEHOLDER — To be finalized by the seller in accordance with Gumroad platform terms and applicable consumer protection statutes. Because BackendForge is an irrevocable digital source-code download, all sales are typically final once the file has been downloaded, except where refund rights are mandated by applicable local law.]`

---

## 13. Third-Party Open-Source Attribution

BackendForge incorporates and links against industry-standard open-source libraries, including Spring Boot, PostgreSQL JDBC Driver, Hibernate ORM, Flyway, JJWT, and Jackson.

* These components remain the property of their respective copyright holders and are licensed under their respective open-source licenses (including Apache-2.0, BSD-2-Clause, BSD-3-Clause, MIT, EPL-2.0, and LGPL-2.1).
* BackendForge does not bundle third-party compiled JAR binaries in the download archive; all dependencies are retrieved directly from Maven Central during build.
* Complete attributions and upstream notices are cataloged in [`THIRD-PARTY-NOTICES.md`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/THIRD-PARTY-NOTICES.md).

---

## 14. Pre-Launch Disclaimer

> **DRAFT LISTING NOTICE**:
> This product listing is a preliminary commercial draft prepared for The Creator Co. It has not been published to Gumroad.
> Commercial launch is subject to the resolution of remaining legal, tax, and account verification blockers cataloged in [`docs/COMMERCIAL-RELEASE-BLOCKERS.md`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/docs/COMMERCIAL-RELEASE-BLOCKERS.md) and execution of the launch steps in [`docs/GUMROAD-LAUNCH-CHECKLIST.md`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/docs/GUMROAD-LAUNCH-CHECKLIST.md).
