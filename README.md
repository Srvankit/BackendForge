# BackendForge

BackendForge is a production-hardened Spring Boot foundation for building secure, maintainable REST APIs. It is built on Java 21, Maven, PostgreSQL, Spring Data JPA, Spring Security, Bean Validation, Flyway, and OpenAPI documentation.

This foundation includes PostgreSQL persistence, versioned migrations, user registration with BCrypt password hashing, stateless JWT authentication with timing equalization, configurable rate limiting, safe request correlation IDs, non-root Docker containerization, and comprehensive health/readiness probes.

---

## Architecture & Project Structure

Application code is rooted at `com.thecreatorco.backendforge`:

* **`common.correlation`**: [CorrelationIdFilter](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/common/correlation/CorrelationIdFilter.java) captures or generates safe correlation IDs (`X-Correlation-ID`), propagates them to SLF4J MDC, attaches them to responses, and sanitizes CRLF/oversize inputs.
* **`common.error`**: Centralized exception handling via [GlobalExceptionHandler](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/common/error/GlobalExceptionHandler.java) returning standardized [ApiError](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/common/error/ApiError.java) responses while excluding sensitive details from logs and client responses.
* **`config`**: [ProductionConfiguration](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/config/ProductionConfiguration.java) and [ProductionConfigurationProperties](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/config/ProductionConfigurationProperties.java) providing fail-fast validation of required production database and JWT settings.
* **`ratelimit`**: In-memory sliding-window limiter ([ClientRateLimiter](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/ratelimit/ClientRateLimiter.java)), interceptor, and validated configuration properties ([RateLimitProperties](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/ratelimit/RateLimitProperties.java)) keyed by direct remote address.
* **`security`**: Stateless security filter chain ([SecurityConfig](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/security/SecurityConfig.java)), HMAC-SHA256 signing and claims validation ([JwtService](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/security/JwtService.java)), and request authentication ([JwtAuthenticationFilter](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/security/JwtAuthenticationFilter.java)).
* **`user`**: User JPA entity, Flyway migrations, repository, controller, password UTF-8 byte boundary validator ([PasswordByteLengthValidator](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/user/PasswordByteLengthValidator.java)), and registration/login service with constant-time failure equalization.

---

## Prerequisites

* **Java**: OpenJDK 21 LTS or newer
* **Build Tool**: Apache Maven 3.9 or newer
* **Database**: PostgreSQL 15 or 16
* **Container Runtime**: Docker Desktop or Docker Engine (required for Testcontainers and container builds)

Verify your local toolchain:

```bash
java -version
mvn -version
docker --version
```

---

## Configuration & Environment Variables

BackendForge provides safe development defaults when running with the default profile. In production (`SPRING_PROFILES_ACTIVE=prod`), all database and JWT parameters must be explicitly provided.

### Configuration Reference

| Variable | Default (Local) | Production (`prod` profile) | Description |
| :--- | :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `default` | `prod` | Active Spring profile (`prod` activates strict startup validation) |
| `DB_URL` | `jdbc:postgresql://localhost:5432/backendforge` | **Required** | JDBC connection URL for PostgreSQL |
| `DB_USERNAME` | `backendforge` | **Required** | Database user |
| `DB_PASSWORD` | `backendforge` | **Required** | Database password |
| `SERVER_PORT` | `8080` | `8080` (or target port) | HTTP application port |
| `JWT_SECRET` | *(blank in dev)* | **Required** | Base64-encoded secret decoding to $\ge 32$ bytes (256 bits) |
| `JWT_EXPIRATION_SECONDS` | `900` | `900` | Access-token lifetime in seconds (15 minutes) |
| `JWT_ISSUER` | `backendforge` | **Required** | Required `iss` claim for tokens |
| `JWT_AUDIENCE` | `backendforge-api` | **Required** | Required `aud` claim for tokens |
| `RATE_LIMIT_LOGIN_MAX_REQUESTS` | `30` | `30` | Login attempts allowed per remote address per window |
| `RATE_LIMIT_LOGIN_WINDOW` | `60s` | `60s` | Sliding window duration for login limiter |
| `RATE_LIMIT_REGISTER_MAX_REQUESTS`| `30` | `30` | Registration attempts allowed per remote address per window |
| `RATE_LIMIT_REGISTER_WINDOW` | `60s` | `60s` | Sliding window duration for registration limiter |
| `RATE_LIMIT_MAX_ENTRIES` | `10000` | `10000` | Maximum active client rate-limit windows kept in memory |
| `POSTGRES_DB` | `backendforge` | *(Compose only)* | PostgreSQL database name used by Docker Compose |
| `POSTGRES_USER` | `backendforge` | *(Compose only)* | PostgreSQL username used by Docker Compose |
| `POSTGRES_PASSWORD` | `change-me` | *(Compose only)* | PostgreSQL password used by Docker Compose |

Copy `.env.example` to `.env` to supply variables for Docker Compose. Spring Boot does not load `.env` files automatically on the host; export them in your shell or pass them to your process.

---

## Local Development

### 1. Host Execution against Local PostgreSQL

1. Start a local PostgreSQL instance (e.g., listening on `localhost:5432`).
2. Generate a local development secret:
   ```bash
   openssl rand -base64 32
   ```
3. Set your environment variables and launch the application:
   ```bash
   export DB_URL="jdbc:postgresql://localhost:5432/backendforge"
   export DB_USERNAME="backendforge"
   export DB_PASSWORD="your-local-password"
   export JWT_SECRET="<your-generated-base64-secret>"
   mvn spring-boot:run
   ```
   *(On Windows PowerShell, use `$env:VARIABLE_NAME = "value"`)*

4. The API will listen on `http://localhost:8080`.

### 2. Isolated Docker Compose Environment

Docker Compose runs an isolated PostgreSQL 16.4 container and builds the backend container using the production profile:

1. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```
2. Generate a secure secret and replace `JWT_SECRET` in `.env`:
   ```bash
   openssl rand -base64 32
   ```
3. Update `POSTGRES_PASSWORD` in `.env` to a secure local password.
4. Launch the Compose environment:
   ```bash
   docker compose up --build
   ```
5. Inside Compose:
   * Backend is accessible on host port `8080` (`http://localhost:8080`).
   * Compose PostgreSQL is mapped to host port `5434` to avoid collisions with local port `5432`.
   * Compose waits for PostgreSQL health checks before launching the backend container.
6. Stop Compose without deleting the volume:
   ```bash
   docker compose down
   ```
   *(Data persists in the named volume `backendforge-compose-postgres-data`)*

---

## Build and Test

Execute the full test suite including Testcontainers integration tests:

```bash
mvn clean test
```

> **Note**: A running Docker daemon is required for Testcontainers tests (`DatabaseMigrationIntegrationTest`, `UserRepositoryIntegrationTest`, `UserRegistrationIntegrationTest`, `UserAuthenticationIntegrationTest`). Testcontainers automatically creates and destroys isolated temporary PostgreSQL containers without touching existing databases or volumes.

Verify code formatting and whitespace integrity:

```bash
git diff --check
```

Build the production JAR executable without running tests:

```bash
mvn clean package -DskipTests
```

---

## API Reference & Authentication

All API endpoints exchange JSON payloads unless noted. Protected endpoints require an `Authorization: Bearer <access-token>` header.

### Endpoints Summary

| Method | Endpoint | Access | Rate-Limited | Description |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Public | Yes | Register a new user account |
| `POST` | `/api/v1/auth/login` | Public | Yes | Authenticate and obtain JWT access token |
| `GET` | `/v3/api-docs` | Public | No | OpenAPI v3 JSON specification |
| `GET` | `/v3/api-docs.yaml` | Public | No | OpenAPI v3 YAML specification |
| `GET` | `/swagger-ui.html` | Public | No | Swagger UI API documentation |
| `GET` | `/actuator/health` | Public | No | Application health probe (`show-details: never`) |
| `GET` | `/actuator/health/readiness` | Public | No | Kubernetes / container readiness probe |
| `GET` | `/actuator/health/liveness` | Public | No | Kubernetes / container liveness probe |

### 1. User Registration

* **Request**: `POST /api/v1/auth/register`
* **Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "correct-horse-battery"
  }
  ```
* **Validation Rules**:
  * Email must be valid and $\le 320$ characters. It is automatically trimmed and converted to lowercase.
  * Password must be between 12 and 128 characters and no more than 72 UTF-8 bytes (BCrypt input boundary).
* **Response (201 Created)**:
  * Headers: `Location: /api/v1/auth/register/{id}`, `X-Correlation-ID: <id>`
  * Body:
    ```json
    {
      "id": "c1f7a85e-049d-4762-b918-977464a4b277",
      "email": "user@example.com",
      "enabled": true,
      "createdAt": "2026-10-26T20:00:00.000Z"
    }
    ```
* **Conflict (409 Conflict)**: Returns `EMAIL_ALREADY_REGISTERED` if the email already exists.

### 2. User Login

* **Request**: `POST /api/v1/auth/login`
* **Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "correct-horse-battery"
  }
  ```
* **Response (200 OK)**:
  ```json
  {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900
  }
  ```
* **Failure (401 Unauthorized)**: Returns code `INVALID_CREDENTIALS` for unknown email, wrong password, or disabled accounts. Password verification runs against a precomputed dummy hash for unknown emails to equalize timing.

### 3. Error Contract (`ApiError`)

All error responses share a standard JSON structure:

```json
{
  "timestamp": "2026-10-26T20:00:00.000Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "fieldErrors": {
    "password": "Password must be between 12 and 128 characters"
  }
}
```

Standard Error Codes:
* `VALIDATION_ERROR` (400)
* `MALFORMED_JSON` (400)
* `UNKNOWN_JSON_PROPERTY` (400)
* `BAD_REQUEST` (400)
* `UNAUTHORIZED` (401)
* `INVALID_CREDENTIALS` (401)
* `EMAIL_ALREADY_REGISTERED` (409)
* `UNSUPPORTED_MEDIA_TYPE` (415)
* `RATE_LIMIT_EXCEEDED` (429, includes `Retry-After` header)
* `INTERNAL_SERVER_ERROR` (500)

---

## Observability & Production Diagnostics

1. **Request Correlation (`X-Correlation-ID`)**:
   * Every request is assigned a correlation ID.
   * Clients can pass an `X-Correlation-ID` (or `X-Request-ID`) header. If valid (whitelisted alphanumeric, hyphens, underscores, dots, $\le 64$ chars), it is preserved. If missing, multiline, or invalid, a fresh UUID is generated.
   * `X-Correlation-ID` is returned on all HTTP responses and logged via SLF4J MDC (`correlationId`).
2. **Health Probes**:
   * `/actuator/health/readiness` and `/actuator/health/liveness` provide Kubernetes and container readiness/liveness checks.
   * `management.endpoint.health.show-details: never` prevents leakage of database details, connection strings, or system metrics.
   * All other Actuator endpoints are unexposed or secured behind authentication.
3. **Sensitive Data Protection in Logs**:
   * Exception handlers log only HTTP method, URI, remote client IP, and exception class names.
   * Raw exception messages, passwords, authorization headers, JWT tokens, request bodies, and database connection secrets are never logged.

---

## Production Deployment Checklist

Before deploying BackendForge to staging or production, verify each item:

- [ ] **1. Active Profile**: Set `SPRING_PROFILES_ACTIVE=prod`. The production profile activates strict startup validation and removes fallback credentials.
- [ ] **2. Database Connectivity & Credentials**:
  - Provide `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
  - Database user has permissions to create/modify tables and manage the `flyway_schema_history` table.
  - Hibernate operates with `ddl-auto: validate` to guarantee schema compliance with Flyway versioning.
- [ ] **3. JWT Signing Key & Claims**:
  - `JWT_SECRET` must be a high-entropy secret generated with `openssl rand -base64 32` or pulled from a secret manager (AWS Secrets Manager, Vault, etc.). Never commit secrets.
  - `JWT_ISSUER` and `JWT_AUDIENCE` must be explicitly configured to match your organization's API domain.
  - `JWT_EXPIRATION_SECONDS` configured appropriately (default: `900`).
- [ ] **4. Container Security**:
  - Image runs as non-root `appuser` (UID `10001`).
  - Container health checks verify `/actuator/health/readiness` via `curl`.
  - Container runtime must allocate sufficient grace period for shutdown (at least 25–30 seconds to allow the 20s Spring graceful shutdown phase to drain inflight requests).
- [ ] **5. Ingress & Rate Limiting**:
  - Configure edge rate limiting / WAF (Cloudflare, AWS WAF, NGINX) to protect against distributed credential stuffing, as instance rate limiters are local to each JVM process.
  - Configure TLS termination at the ingress or load balancer.

### Production Startup Command

Run the packaged JAR with environment variables:

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL="jdbc:postgresql://postgres-prod.internal:5432/backendforge"
export DB_USERNAME="prod_user"
export DB_PASSWORD="prod_password"
export JWT_SECRET="<base64-secret-at-least-32-bytes>"
export JWT_ISSUER="https://api.yourdomain.com"
export JWT_AUDIENCE="backendforge-api"

java -jar target/backendforge-0.0.1-SNAPSHOT.jar
```

Or run via Docker:

```bash
docker run -d \
  --name backendforge-api \
  --restart unless-stopped \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL="jdbc:postgresql://db:5432/backendforge" \
  -e DB_USERNAME="prod_user" \
  -e DB_PASSWORD="prod_password" \
  -e JWT_SECRET="<base64-secret-at-least-32-bytes>" \
  -e JWT_ISSUER="https://api.yourdomain.com" \
  -e JWT_AUDIENCE="backendforge-api" \
  backendforge:latest
```

---

## Safe Upgrades, Rollbacks & Migration Guidelines

### 1. Flyway Versioning & Forward-Only Migrations

* Database migrations are located in `src/main/resources/db/migration/` and follow the naming standard `V<version>__<description>.sql`.
* Flyway executes versioned migrations on startup and stores migration history and checksums in `flyway_schema_history`.
* **Immutable Migrations**: Never modify or reorder an existing migration script that has been deployed to any environment. Changing historical files breaks Flyway checksum validation and aborts application startup.

### 2. Zero-Downtime Schema Evolution (Expand-Contract)

When releasing schema changes in rolling zero-downtime environments:
1. **Expand**: Add new tables or columns. New columns must be `NULL` or have database defaults so running older application versions can continue inserting rows without failure.
2. **Transition**: Deploy application code that reads from/writes to the new schema while maintaining backward compatibility.
3. **Contract**: Once all instances run the updated code, apply a subsequent migration to remove obsolete columns or enforce strict `NOT NULL` constraints.

### 3. Rollback Procedures

* **Application Rollback**: If an application version fails post-deployment, roll back the container or JAR to the previous tag. Because Hibernate uses `validate`, the previous version will run successfully provided database changes followed the non-breaking expand pattern.
* **Database Rollback**: Flyway Community does not support automated down migrations. Do not manually delete entries from `flyway_schema_history`. To undo a schema change, author a new forward migration (e.g. `V2__revert_...sql`) that applies the compensating DDL changes.
* **Token Invalidation on Rollback**: Rolling back does not invalidate existing tokens unless `JWT_SECRET`, `JWT_ISSUER`, or `JWT_AUDIENCE` is altered.

---

## Known Limitations & Architecture Decisions

1. **Instance-Local Rate Limiting**:
   * Rate limiting is enforced in JVM memory via [ClientRateLimiter](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/ratelimit/ClientRateLimiter.java).
   * Counters are not shared across multiple replica instances and reset when a container restarts.
   * Multi-instance production deployments should enforce distributed limits at the API Gateway, Ingress, or Redis layer in addition to these application-level guards.
2. **Registration Conflict Disclosure**:
   * `POST /api/v1/auth/register` returns HTTP 409 `EMAIL_ALREADY_REGISTERED` for existing emails.
   * While login response timing is equalized with dummy hashing to prevent account enumeration during authentication, registration conflict responses inherently indicate whether an email address is registered.
   * This behavior is standard for self-service API registration contracts and is protected against automated harvesting by registration rate limiting.
3. **Synchronous Thread-Bound MDC Context**:
   * [CorrelationIdFilter](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/common/correlation/CorrelationIdFilter.java) binds correlation IDs to the thread-local SLF4J MDC context for the duration of the request.
   * If background `@Async` worker tasks or standalone thread pools are introduced in the future, an `AsyncConfigurer` or `TaskDecorator` must be configured to pass MDC attributes across thread boundaries.

---

## Dependency Versions

* **Spring Boot**: `3.5.6`
* **Java**: `21` (LTS)
* **Springdoc OpenAPI**: `2.8.13`
* **PostgreSQL**: `16.4` (Compose) / `15` (Testcontainers)
* **JJWT**: `0.12.6`
* **Testcontainers**: `2.0.5`
