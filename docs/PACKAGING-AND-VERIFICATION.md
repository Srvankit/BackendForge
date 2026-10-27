# BackendForge: Safe Source Packaging & Verification Guide

> **Document Status**: Operational Guide & Release Verification Standard
> **Target Audience**: Repository Owner, Maintainers, and Distribution Operators
> **Important Legal Notice**: This guide provides engineering and operational procedures for packaging BackendForge as a commercial source-code starter kit. It does not constitute legal advice. Formal software licenses and third-party notices must be reviewed and approved by licensed legal counsel.

---

## 1. Executive Summary & Distribution Strategy

BackendForge is designed to be distributed commercially as a **developer source-code starter kit**. Under this distribution model:

1. **Source-Only Distribution**: Customers receive original source files, architectural scaffolding, migration scripts, container configuration, and documentation in a clean ZIP archive.
2. **Decoupled Dependencies**: Third-party runtime binaries (Spring Boot, Hibernate, Jackson, etc.) are **not bundled** in the release ZIP. Customers download dependencies directly from Maven Central during their first local build execution (`./mvnw clean package` or `.\mvnw.cmd clean package`).
3. **Distribution Cleanliness**: Because no compiled third-party JARs or fat-JAR executables are redistributed, the starter kit avoids complex binary redistribution obligations (such as LGPL-2.1 library relinking mandates).
4. **Zero-Leak Guarantee**: Under no circumstances may local development artifacts (`.env`, database credentials, `.git` history, IDE configurations, temporary files, logs, or crash dumps) be included in the release archive.

---

## 2. Release Reference Distinction: Baseline v1.0.0 vs Future v1.0.1

A critical requirement of controlled release engineering is strict separation between historical Git references and ongoing uncommitted development work:

| Aspect | Baseline `v1.0.0` (`f7670ae`) | Current Candidate on `main` | Future Release `v1.0.1` |
| :--- | :--- | :--- | :--- |
| **Git Status** | Committed and tagged at baseline commit `f7670ae8824a5a277ec9c126c87ede53a3050ea5` | Committed on `main` branch; all 98 tests passing | Formally tagged with signed Git tag `v1.0.1` only after owner/legal signoff |
| **`pom.xml` Version** | `0.0.1-SNAPSHOT` | `1.0.1` (aligned) | `1.0.1` (release) |
| **`LICENSE.md`** | **Missing** from baseline Git tree | Preliminary commercial draft on `main` | Finalized commercial license approved by counsel |
| **`THIRD-PARTY-NOTICES.md`** | **Missing** from baseline Git tree | Preliminary attribution report on `main` | Audited third-party notices committed |
| **Maven Wrapper** | **Missing** from baseline tree (`mvnw` untracked) | Fully tracked and committed on `main` | Fully tracked and committed |
| **Commercial Docs** | Missing launch preparation and blockers | Complete blockers, Gumroad draft, and launch checklist | Fully resolved commercial blockers |
| **Suitability for Sale** | **UNSUITABLE**: Missing legal notices, wrapper, POM version | **UNSUITABLE**: Awaiting Gumroad setup & legal review of license | **TARGET STATE**: Packaged and uploaded to Gumroad after legal signoff |


> [!CAUTION]
> **Never Package v1.0.0 as Though it Contained Uncommitted Work**: The baseline tag `v1.0.0` points to commit `f7670ae`, which lacks legal documents, the Maven wrapper, and recent fixes. Packaging `v1.0.0` via `git archive` will accurately reflect only what was committed at that tag. Uncommitted working changes must never be mixed into a release archive without a reviewed, formal commit.

---

## 3. Safe Source Packaging Automation

To prevent human error and guarantee reproducible release packages, BackendForge provides automated packaging scripts:
- **Windows PowerShell**: [`scripts/package-source.ps1`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/scripts/package-source.ps1)
- **POSIX / Bash**: [`scripts/package-source.sh`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/scripts/package-source.sh)

### Core Packaging Principles Enforced by the Scripts:

1. **Strict Git Archive Source**: The scripts use `git archive` targeting an explicitly supplied commit SHA or tag. `git archive` extracts files exclusively from Git's object database for that reference. It is physically impossible for untracked workspace files (like `.env`, `target/`, `.idea/`, or crash dumps) to be included in the archive.
2. **Mandatory Explicit Reference**: An explicit reference (e.g. `-ReleaseRef <ref>` or `--ref <ref>`) is required. The script will never guess or default to `HEAD`.
3. **Dirty Workspace Protection**: The scripts check `git status --porcelain=v1`. If uncommitted changes exist, the script halts immediately to prevent confusion between working state and release state. (The `--dry-run` or `-DryRun` switch can be used to audit without creating an archive).
4. **Mandatory File Manifest Validation**: Before archiving, the script inspects the target commit's file tree (`git ls-tree`) to verify the presence of:
   - `LICENSE.md`
   - `THIRD-PARTY-NOTICES.md`
   - `README.md`
   - `pom.xml`
   - `.env.example`
   - `compose.yaml`
   - `Dockerfile`
   - `mvnw` & `mvnw.cmd` & `.mvn/wrapper/maven-wrapper.properties`
   - `src/main/resources/application.yml`
   If any required file is missing (as in `v1.0.0`), the script halts with an explicit error.
5. **Forbidden Pattern Detection**: Scans the target commit tree for accidentally committed secrets (`.env`, `*.key`, `*.pem`), build directories (`target/`), IDE clutter (`.idea/`, `.vscode/`), and crash dumps.
6. **Isolated Output Directory**: Archives are saved into `target/dist/` (which is covered by `.gitignore`), preventing accidental commit of generated ZIP files.
7. **Dry-Run Inspection Mode**: With `-DryRun` / `--dry-run`, operators can verify the complete list of files that would be packaged without creating any file on disk.

### Packaging Commands:

#### Windows PowerShell:
```powershell
# 1. Audit / Dry-Run (creates NO archive):
.\scripts\package-source.ps1 -ReleaseRef "v1.0.0" -DryRun -AllowDirty

# 2. Package a future reviewed commit/tag (clean tree):
.\scripts\package-source.ps1 -ReleaseRef "<commit-sha-or-v1.0.1>"
```

#### Linux / macOS:
```bash
# 1. Audit / Dry-Run (creates NO archive):
chmod +x ./scripts/*.sh
./scripts/package-source.sh --ref "v1.0.0" --dry-run --allow-dirty

# 2. Package a future reviewed commit/tag (clean tree):
./scripts/package-source.sh --ref "<commit-sha-or-v1.0.1>"
```

---

## 4. Pre-Sale Quality Assurance & Verification Checklist

Before uploading any release ZIP to digital marketplaces (Gumroad, LemonSqueezy, GitHub Releases, or private customer portals), operators must execute the pre-sale verification procedure:

### Automated Pre-Sale Audit:
BackendForge provides standalone verification scripts:
- **Windows PowerShell**: [`scripts/verify-package.ps1`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/scripts/verify-package.ps1)
- **POSIX / Bash**: [`scripts/verify-package.sh`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/scripts/verify-package.sh)

```powershell
.\scripts\verify-package.ps1 -ZipPath "target/dist/BackendForge-v1.0.1.zip"
```

### Manual Verification Checklist:

- [ ] **1. Secret and Credential Exclusion**:
  - Open archive entry listing (`tar -tf` or `unzip -l`).
  - Confirm **no** `.env`, `.env.local`, `.env.prod`, or live credential file exists.
  - Confirm `.env.example` is present and contains only placeholder values (`changeme`, `your-secret-here`).
  - *Never print or view sensitive `.env` files during checks.*

- [ ] **2. Git History and Repository Metadata Exclusion**:
  - Confirm **no** `.git/` directory or `.gitattributes` internal files are inside the ZIP. Customers should receive clean source code, allowing them to run `git init` for their own projects.

- [ ] **3. Build Outputs, Binaries, and Dumps Exclusion**:
  - Confirm **no** `target/` directory, `.class` files, `.jar` files, or `.war` files exist.
  - Confirm **no** `hs_err_pid*`, `replay_pid*`, or `*.log` files exist.

- [ ] **4. IDE and OS Clutter Exclusion**:
  - Confirm **no** `.idea/`, `.vscode/`, `*.iml`, `*.code-workspace`, `.DS_Store`, or `Thumbs.db` files exist.

- [ ] **5. Required Starter-Kit Manifest Completeness**:
  - [ ] `LICENSE.md` present and non-empty.
  - [ ] `THIRD-PARTY-NOTICES.md` present, audited, and non-empty.
  - [ ] `README.md` present with setup, architecture, and configuration documentation.
  - [ ] `pom.xml` present with version matching the release tag.
  - [ ] `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties` present and executable.
  - [ ] `compose.yaml` and `Dockerfile` present.
  - [ ] Complete `src/main/` and `src/test/` source trees present.

- [ ] **6. Clean Build Verification**:
  - Extract the candidate ZIP to a temporary clean folder.
  - Execute `./mvnw clean test` (or `.\mvnw.cmd clean test`) to confirm that a fresh customer checkout builds and passes tests out of the box with zero external dependencies.

- [ ] **7. Cryptographic Checksum Generation**:
  - Compute SHA-256 hash:
    ```powershell
    Get-FileHash -Path "BackendForge-v1.0.1.zip" -Algorithm SHA256
    ```
  - Record the SHA-256 hash in the release notes and distribution manifest for tamper verification.

---

## 5. Dependency-License Reporting & Audit Findings

### Safe License Plugin Execution (Without Modifying `pom.xml`)
The dependency-license report was generated using the trusted, version-pinned `org.codehaus.mojo:license-maven-plugin:2.5.0` via Maven Wrapper directly from the command line:

```powershell
# Production Runtime Dependencies:
.\mvnw.cmd org.codehaus.mojo:license-maven-plugin:2.5.0:add-third-party `
  "-Dlicense.includedScopes=compile,runtime" `
  "-Dlicense.outputDirectory=target/generated-sources/license" `
  "-Dlicense.thirdPartyFilename=THIRD-PARTY-PRODUCTION.txt" `
  "-Dlicense.missingFile=target/generated-sources/license/THIRD-PARTY-PRODUCTION.properties"

# Test Dependencies:
.\mvnw.cmd org.codehaus.mojo:license-maven-plugin:2.5.0:add-third-party `
  "-Dlicense.includedScopes=test" `
  "-Dlicense.outputDirectory=target/generated-sources/license" `
  "-Dlicense.thirdPartyFilename=THIRD-PARTY-TEST.txt" `
  "-Dlicense.missingFile=target/generated-sources/license/THIRD-PARTY-TEST.properties"
```

- **Exit Status**: 0 (BUILD SUCCESS)
- **Output Location**: `target/generated-sources/license/` (automatically ignored by Git).
- **Unresolved Licenses**: **0** (all 95 production and 41 test dependencies were fully resolved to declared upstream licenses).

### Production vs Test Dependency Breakdown:
- **Production Runtime Dependencies**: **95** components (transitive resolution of Spring Boot, Tomcat, PostgreSQL JDBC, Flyway, Hibernate ORM, JJWT, SpringDoc OpenAPI, Micrometer, Jackson, and Logback).
- **Test-Only Dependencies**: **41** components (JUnit Jupiter, Mockito, Testcontainers, AssertJ, Awaitility, JSONassert). None are redistributed in customer production builds.

### Key Third-Party License Categories (Production):
1. **Apache License 2.0**: ~80% of production stack (Spring Boot framework, Apache Tomcat Embed, Flyway, Jackson, JJWT, SpringDoc OpenAPI, Micrometer, Byte Buddy, Hibernate Validator, SnakeYAML). Permissive commercial license; requires attribution and preservation of upstream copyright/notices.
2. **BSD-2-Clause / BSD-3-Clause**: PostgreSQL JDBC Driver (`42.7.7`), ANTLR 4 Runtime (`4.13.0`), HdrHistogram (`2.2.2`). Permissive; requires notice reproduction.
3. **MIT License**: SLF4J API (`2.0.17`), Checker Qual (`3.49.3`), WebJars Locator Lite (`1.1.0`). Permissive; requires copyright notice preservation.
4. **Eclipse Distribution License (EDL 1.0 / BSD equivalent)**: JAXB Runtime, Jakarta Activation API, Jakarta XML Binding API, Angus Activation. Permissive.
5. **Eclipse Public License (EPL 1.0 & EPL 2.0)**:
   - Logback Classic & Core (`1.5.18`): Dual-licensed under EPL-1.0 or LGPL-2.1.
   - AspectJ Weaver (`1.9.24`): EPL-2.0.
   - Jakarta Persistence API (`3.1.0`): Dual-licensed EPL-2.0 or EDL-1.0.
   - Weak copyleft: Does not contaminate independent application code, but requires source code availability notices if redistributing modified library binaries.
6. **GNU Lesser General Public License (LGPL 2.1)**:
   - Hibernate ORM Core (`6.6.29.Final`): LGPL-2.1-or-later.
   - In source distribution: clean, because binaries are not redistributed.
   - In binary / Docker distribution: Requires notice, source availability or written offer for the Hibernate library, and relinking capability.

---

## 6. Review of Preliminary `THIRD-PARTY-NOTICES.md`

A forensic line-by-line comparison between the preliminary draft in [`THIRD-PARTY-NOTICES.md`](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/THIRD-PARTY-NOTICES.md) and the actual Maven resolution revealed several gaps:

1. **Extraneous Dependency (Lombok)**:
   - `THIRD-PARTY-NOTICES.md` (line 28) includes `Project Lombok (org.projectlombok:lombok:1.18.36 - MIT License)`.
   - **Finding**: Lombok is **not** present in `pom.xml` or anywhere in the dependency tree. It must be removed from `THIRD-PARTY-NOTICES.md`.
2. **Omitted Direct Dependency**:
   - `org.flywaydb:flyway-database-postgresql:11.7.2` (Apache-2.0) is a direct dependency in `pom.xml` but was omitted from the summary table.
3. **Omitted Critical Transitive Dependencies**:
   - The preliminary notice lists 15 high-level components. The actual runtime stack contains 95 JARs. Key runtime libraries that should be acknowledged include:
     - `net.bytebuddy:byte-buddy:1.17.7` (Apache-2.0, required by Hibernate)
     - `org.antlr:antlr4-runtime:4.13.0` (BSD-3-Clause, query parser in Hibernate)
     - `org.hibernate.validator:hibernate-validator:8.0.3.Final` (Apache-2.0, validation engine)
     - `org.yaml:snakeyaml:2.4` (Apache-2.0, YAML parser)
     - `org.webjars:swagger-ui:5.28.1` (Apache-2.0, Swagger UI documentation frontend)
     - `org.aspectj:aspectjweaver:1.9.24` (EPL-2.0, Spring AOP)
     - `io.micrometer:micrometer-core:1.15.4` (Apache-2.0, Actuator metrics)
4. **Missing Upstream Notice Extracts**:
   - While `THIRD-PARTY-NOTICES.md` includes notices for Spring Boot, Tomcat, PostgreSQL, Flyway, Jackson, Hibernate, Logback, JJWT, SpringDoc, HikariCP, and SLF4J, notices for ANTLR 4, Swagger UI, and Micrometer should be added prior to precompiled binary distribution.
5. **No Legal Guarantee**:
   - Neither the automated Maven report nor `THIRD-PARTY-NOTICES.md` guarantees full legal compliance. Licensed legal counsel must review the final document.
