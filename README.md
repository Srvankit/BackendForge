# BackendForge

BackendForge is a production-hardened Spring Boot starter kit for building secure, maintainable REST APIs. It is built on Java 21, Maven, PostgreSQL, Spring Data JPA, Spring Security, Bean Validation, Flyway database migrations, and OpenAPI v3 documentation with Swagger UI.

This foundation includes PostgreSQL persistence, forward-only schema migrations, user registration with BCrypt password hashing, stateless JWT authentication with timing equalization, configurable rate limiting, safe request correlation IDs, non-root Docker containerization, and comprehensive health and readiness probes.

---

## Architecture & Project Structure

Application code is rooted at `com.thecreatorco.backendforge`:

* **`common.correlation`**: [CorrelationIdFilter](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/common/correlation/CorrelationIdFilter.java) captures or generates safe correlation IDs (`X-Correlation-ID`), propagates them to SLF4J MDC, attaches them to responses, and sanitizes CRLF/oversize inputs.
* **`common.error`**: Centralized exception handling via [GlobalExceptionHandler](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/common/error/GlobalExceptionHandler.java) returning standardized [ApiError](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/common/error/ApiError.java) responses while excluding sensitive details from logs and client responses.
* **`config`**: [ProductionConfiguration](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/config/ProductionConfiguration.java) and [ProductionConfigurationProperties](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/config/ProductionConfigurationProperties.java) providing fail-fast validation of required production database and JWT settings, along with OpenAPI specification setup in [OpenApiConfig](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/config/OpenApiConfig.java).
* **`ratelimit`**: In-memory sliding-window limiter ([ClientRateLimiter](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/ratelimit/ClientRateLimiter.java)), interceptor ([RateLimitInterceptor](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/ratelimit/RateLimitInterceptor.java)), and validated configuration properties ([RateLimitProperties](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/ratelimit/RateLimitProperties.java)) keyed by direct remote address.
* **`security`**: Stateless security filter chain ([SecurityConfig](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/security/SecurityConfig.java)), HMAC-SHA256 signing and claims validation ([JwtService](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/security/JwtService.java)), and request authentication ([JwtAuthenticationFilter](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/security/JwtAuthenticationFilter.java)).
* **`user`**: User JPA entity ([UserEntity](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/user/UserEntity.java)), Flyway migrations, repository, controller ([UserController](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/user/UserController.java)), password UTF-8 byte boundary validator ([PasswordByteLengthValidator](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/user/PasswordByteLengthValidator.java)), and registration/login service ([UserService](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/user/UserService.java)) with constant-time failure equalization.

---

## Prerequisites & Installation

### 1. Required Toolchain

* **Java**: OpenJDK 21 LTS or newer.
* **Build Tool**: Apache Maven 3.9+ is bundled via the included Maven Wrapper ([mvnw](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/mvnw) / [mvnw.cmd](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/mvnw.cmd)). A globally installed Maven distribution is **not** required.
* **Database**: PostgreSQL 15 or 16 (local native service or Docker container).
* **Container Runtime**: Docker Desktop or Docker Engine (required for Docker Compose and Testcontainers integration tests).

### 2. Verify Installed Toolchain

#### Linux / macOS:
```bash
java -version
./mvnw -version
docker --version
docker compose version
```

#### Windows PowerShell:
```powershell
java -version
.\mvnw.cmd -version
docker --version
docker compose version
```

> **Verifying Java 21**: The output of `java -version` must report version `21` (e.g., `openjdk version "21.0.x"`). If multiple Java versions are installed, ensure your `JAVA_HOME` environment variable points to your JDK 21 installation (`echo $JAVA_HOME` on Linux/macOS or `$env:JAVA_HOME` on Windows PowerShell).

---

## Configuration & Environment Variables

BackendForge provides safe development defaults when running with the default profile. In production (`SPRING_PROFILES_ACTIVE=prod`), all database and JWT parameters must be explicitly provided.

> **Security Rule**: `JWT_SECRET` has **no hardcoded fallback** in any profile. It must be provided via an environment variable before the application will start.

### Configuration Reference

| Variable | Default (Local / Dev) | Production (`prod` profile) | Description |
| :--- | :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `default` | `prod` | Active Spring profile (`prod` activates strict startup validation) |
| `DB_URL` | `jdbc:postgresql://localhost:5432/backendforge` | **Required** | JDBC connection URL for PostgreSQL |
| `DB_USERNAME` | `backendforge` | **Required** | Database user |
| `DB_PASSWORD` | `backendforge` | **Required** | Database password |
| `SERVER_PORT` | `8080` | `8080` (or target port) | HTTP application port |
| `JWT_SECRET` | *(Required via env)* | **Required** | Base64-encoded secret decoding to $\ge 32$ bytes (256 bits) |
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
| `POSTGRES_PASSWORD` | *(Must be set in `.env`)* | *(Compose only)* | PostgreSQL password used by Docker Compose |

### JWT Secret Generation & Validation Rules

BackendForge signs and verifies JWT access tokens using HMAC-SHA256 (`HS256`). The implementation in [JwtService](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/security/JwtService.java) enforces strict validation rules at startup:

1. **Format**: The string must be valid Base64.
2. **Key Length**: Decoded bytes must be at least 32 bytes (256 bits).
3. **Startup Exceptions**: If `JWT_SECRET` is blank, missing, invalid Base64, or shorter than 32 decoded bytes, startup terminates immediately with an `IllegalStateException`:
   * `"JWT_SECRET must be configured with a Base64-encoded secret"`
   * `"JWT_SECRET must be valid Base64"`
   * `"JWT_SECRET must decode to at least 32 bytes"`

#### Generate a Compliant Base64 Secret:

* **Linux / macOS (OpenSSL)**:
  ```bash
  openssl rand -base64 32
  ```

* **Windows PowerShell**:
  ```powershell
  [Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
  ```

* **Safe Example for Local Development Only**:
  ```
  VGhpcyBpcyBhIHNhZmUgZGV2ZWxvcG1lbnQgc2VjcmV0IGZvciBCYWNrZW5kRm9yZ2U=
  ```
  *(Never use example keys in production environments).*

### Development vs. Production Configuration

* **Development (`default` profile)**:
  Allows quick local execution with fallback database credentials (`backendforge:backendforge` at `localhost:5432/backendforge`) and default JWT issuer/audience claims.
* **Production (`SPRING_PROFILES_ACTIVE=prod`)**:
  [ProductionConfiguration](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/config/ProductionConfiguration.java) activates strict fail-fast validation. If `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_ISSUER`, or `JWT_AUDIENCE` are missing or empty, application startup halts immediately.

> **Note on `.env` files**: Spring Boot does not automatically load `.env` files when running locally on the host. The `.env` file is used by Docker Compose. When running on the host, export environment variables in your shell or IDE run configuration.

---

## PostgreSQL Setup & Connection Guide

### 1. Local Native PostgreSQL

If PostgreSQL is installed directly on your machine (listening on the default port `5432`):

1. **Create the Database and User**:
   Open a terminal and connect as the PostgreSQL administrator:
   ```bash
   psql -U postgres
   ```
   Execute the following SQL statements:
   ```sql
   CREATE DATABASE backendforge;
   CREATE USER backendforge WITH ENCRYPTED PASSWORD 'your_secure_password';
   GRANT ALL PRIVILEGES ON DATABASE backendforge TO backendforge;
   \c backendforge
   GRANT ALL ON SCHEMA public TO backendforge;
   ```

2. **Configure Connection Properties**:
   Set `DB_URL=jdbc:postgresql://localhost:5432/backendforge`, `DB_USERNAME=backendforge`, and `DB_PASSWORD=your_secure_password`.

### 2. Docker Compose PostgreSQL

The provided [compose.yaml](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/compose.yaml) runs an isolated PostgreSQL 16.4 container:

* **Compose Host Port**: `5434` (mapped to container port `5432`).
* **Why Port 5434?** Compose maps PostgreSQL to host port `5434` to prevent port collisions with any local PostgreSQL instance already running on default port `5432` or secondary port `5433`.
* **Connecting from Host to Compose Database**:
  If you run the Spring Boot application on your host while using Compose for PostgreSQL, use:
  ```
  DB_URL=jdbc:postgresql://localhost:5434/backendforge
  ```
* **Connecting Inside Compose Network**:
  When running both backend and database inside Docker Compose, the backend container connects internally using:
  ```
  DB_URL=jdbc:postgresql://postgres:5432/backendforge
  ```

### 3. Role of Flyway Database Migrations

* Database migrations are located in `src/main/resources/db/migration/` and execute automatically at application startup.
* **Important Prerequisite**: Flyway automatically creates application tables (such as `users`) and metadata tables (`flyway_schema_history`), but **Flyway cannot create the PostgreSQL database catalog itself**. The target database (`backendforge`) must exist and be reachable before starting the application.
* Hibernate is configured with `ddl-auto: validate`. It verifies that JPA entity mappings match the database schema created by Flyway without executing automatic schema mutations.

---

## Running the Application

### 1. Host Execution with Maven Wrapper

Ensure your local PostgreSQL database is running and the `backendforge` database exists.

#### Linux / macOS:
```bash
export DB_URL="jdbc:postgresql://localhost:5432/backendforge"
export DB_USERNAME="backendforge"
export DB_PASSWORD="your-local-password"
export JWT_SECRET="VGhpcyBpcyBhIHNhZmUgZGV2ZWxvcG1lbnQgc2VjcmV0IGZvciBCYWNrZW5kRm9yZ2U="

./mvnw spring-boot:run
```

#### Windows PowerShell:
```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/backendforge"
$env:DB_USERNAME = "backendforge"
$env:DB_PASSWORD = "your-local-password"
$env:JWT_SECRET = "VGhpcyBpcyBhIHNhZmUgZGV2ZWxvcG1lbnQgc2VjcmV0IGZvciBCYWNrZW5kRm9yZ2U="

.\mvnw.cmd spring-boot:run
```

The application will start on `http://localhost:8080`.

### 2. Isolated Docker Compose Environment

Docker Compose launches PostgreSQL 16.4 and builds the containerized backend using the production profile:

1. Copy the example configuration file:
   * Linux / macOS: `cp .env.example .env`
   * Windows PowerShell: `Copy-Item .env.example .env`
2. Update `.env` with a secure password for `POSTGRES_PASSWORD` and generate a fresh Base64 secret for `JWT_SECRET`.
3. Start the environment:
   ```bash
   docker compose up --build
   ```
4. Access endpoints:
   * API endpoints: `http://localhost:8080`
   * Swagger UI: `http://localhost:8080/swagger-ui.html`
   * Health Probe: `http://localhost:8080/actuator/health`
5. Stop Compose without removing database data:
   ```bash
   docker compose down
   ```
   *(Data persists in named volume `backendforge-compose-postgres-data`)*.

### 3. Standalone Packaging & Execution

Build an executable production JAR:

#### Linux / macOS:
```bash
./mvnw clean package -DskipTests
java -jar target/backendforge-1.0.1.jar
```

#### Windows PowerShell:
```powershell
.\mvnw.cmd clean package -DskipTests
java -jar target/backendforge-1.0.1.jar
```

### 4. Standalone Container Execution

Build and run the non-root Docker container image:

```bash
docker build -t backendforge:latest .

docker run -d \
  --name backendforge-api \
  --restart unless-stopped \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL="jdbc:postgresql://host.docker.internal:5432/backendforge" \
  -e DB_USERNAME="backendforge" \
  -e DB_PASSWORD="your-local-password" \
  -e JWT_SECRET="<your-base64-secret>" \
  -e JWT_ISSUER="backendforge" \
  -e JWT_AUDIENCE="backendforge-api" \
  backendforge:latest
```

---

## Build and Test

BackendForge includes unit tests and containerized integration tests:

### 1. Fast Unit Tests (No Docker Required)

Execute unit tests (e.g., [JwtServiceTest](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/test/java/com/thecreatorco/backendforge/security/JwtServiceTest.java), [UserServiceTest](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/test/java/com/thecreatorco/backendforge/user/UserServiceTest.java), [UserControllerTest](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/test/java/com/thecreatorco/backendforge/user/UserControllerTest.java), [OpenApiConfigTest](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/test/java/com/thecreatorco/backendforge/config/OpenApiConfigTest.java)) without needing a Docker daemon:

* **Linux / macOS**:
  ```bash
  ./mvnw test "-Dtest=*Test,!*IntegrationTest"
  ```

* **Windows PowerShell**:
  ```powershell
  .\mvnw.cmd test "-Dtest=*Test,!*IntegrationTest"
  ```

### 2. Full Test Suite (Testcontainers / Docker Required)

Execute all tests, including integration tests:

* **Linux / macOS**:
  ```bash
  ./mvnw clean test
  ```

* **Windows PowerShell**:
  ```powershell
  .\mvnw.cmd clean test
  ```

> **Testcontainers Requirement**: Integration tests (`DatabaseMigrationIntegrationTest`, `UserRepositoryIntegrationTest`, `UserRegistrationIntegrationTest`, `UserAuthenticationIntegrationTest`) automatically spin up temporary PostgreSQL containers using Testcontainers. Docker Desktop or Docker Engine must be running. Testcontainers creates and destroys isolated temporary containers; existing databases or volumes are never modified.

### 3. Whitespace & Patch Integrity Verification

Verify formatting, CRLF hygiene, and whitespace rules:

```bash
git diff --check
```

---

## API Reference & Authentication Workflow

All API endpoints exchange JSON payloads unless noted. Protected endpoints require an `Authorization: Bearer <access-token>` header.

### Endpoints Summary

| Method | Endpoint | Access | Rate-Limited | Description |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Public | Yes (30/min) | Register a new user account |
| `POST` | `/api/v1/auth/login` | Public | Yes (30/min) | Authenticate credentials and receive JWT |
| `GET` | `/api/v1/users/me` | Authenticated | No | Retrieve authenticated user profile |
| `GET` | `/v3/api-docs` | Public | No | OpenAPI v3 specification (JSON) |
| `GET` | `/v3/api-docs.yaml` | Public | No | OpenAPI v3 specification (YAML) |
| `GET` | `/swagger-ui.html` | Public | No | Interactive Swagger UI API documentation |
| `GET` | `/actuator/health` | Public | No | Application health status (`show-details: never`) |
| `GET` | `/actuator/health/readiness`| Public | No | Kubernetes / container readiness probe |
| `GET` | `/actuator/health/liveness` | Public | No | Kubernetes / container liveness probe |

---

### 1. User Registration

* **Endpoint**: `POST /api/v1/auth/register`
* **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "correct-horse-battery"
  }
  ```
* **Validation Constraints**:
  * Email must be valid and $\le 320$ characters. It is automatically trimmed and normalized to lowercase.
  * Password must be between 12 and 128 characters and $\le 72$ UTF-8 bytes (enforced by [PasswordByteLengthValidator](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/user/PasswordByteLengthValidator.java) due to BCrypt truncation limits).
* **Response (201 Created)**:
  * Headers: `Location: /api/v1/auth/register/{id}`, `X-Correlation-ID: <id>`
  * Body:
    ```json
    {
      "id": "c1f7a85e-049d-4762-b918-977464a4b277",
      "email": "user@example.com",
      "enabled": true,
      "createdAt": "2026-10-27T04:45:00.000Z"
    }
    ```
* **Conflict (409 Conflict)**: Returns code `EMAIL_ALREADY_REGISTERED` if the email address is already registered.

#### Example Command:

* **Linux / macOS cURL**:
  ```bash
  curl -X POST http://localhost:8080/api/v1/auth/register \
    -H "Content-Type: application/json" \
    -d '{"email":"user@example.com","password":"correct-horse-battery"}'
  ```

* **Windows PowerShell**:
  ```powershell
  $body = @{ email = "user@example.com"; password = "correct-horse-battery" } | ConvertTo-Json
  Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/v1/auth/register" -ContentType "application/json" -Body $body
  ```

---

### 2. User Login

* **Endpoint**: `POST /api/v1/auth/login`
* **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "correct-horse-battery"
  }
  ```
* **Response (200 OK)**:
  ```json
  {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJiYWNrZW5kZm9yZ2UiLCJzdWIiOiJjMWY3YTg1ZS0wNDlkLTQ3NjItYjkxOC05Nzc0NjRhNGIyNzciLCJhdWQiOiJiYWNrZW5kZm9yZ2UtYXBpIiwiZXhwIjoxNzYxNTU0NDAwLCJpYXQiOjE3NjE1NTM1MDAsImVtYWlsIjoidXNlckBleGFtcGxlLmNvbSIsInRva2VuX3VzZSI6ImFjY2VzcyJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900
  }
  ```
* **Authentication Failure (401 Unauthorized)**:
  Returns code `INVALID_CREDENTIALS` for unknown email, incorrect password, or disabled account.
  > **Timing Equalization**: To prevent account enumeration through response time variance, login verification against nonexistent emails executes against a precomputed dummy hash.

#### Example Command:

* **Linux / macOS cURL**:
  ```bash
  curl -X POST http://localhost:8080/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"user@example.com","password":"correct-horse-battery"}'
  ```

* **Windows PowerShell**:
  ```powershell
  $body = @{ email = "user@example.com"; password = "correct-horse-battery" } | ConvertTo-Json
  $response = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/v1/auth/login" -ContentType "application/json" -Body $body
  $token = $response.accessToken
  ```

---

### 3. Get Authenticated User Profile

* **Endpoint**: `GET /api/v1/users/me`
* **Headers**: `Authorization: Bearer <access-token>`
* **Response (200 OK)**:
  ```json
  {
    "id": "c1f7a85e-049d-4762-b918-977464a4b277",
    "email": "user@example.com",
    "enabled": true,
    "createdAt": "2026-10-27T04:45:00.000Z"
  }
  ```
* **Failure (401 Unauthorized)**: Returns code `UNAUTHORIZED` if token is missing, expired, or invalid.

#### Example Command:

* **Linux / macOS cURL**:
  ```bash
  curl -X GET http://localhost:8080/api/v1/users/me \
    -H "Authorization: Bearer <your-access-token>"
  ```

* **Windows PowerShell**:
  ```powershell
  Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/v1/users/me" -Headers @{ Authorization = "Bearer $token" }
  ```

---

### 4. Interactive Swagger UI Authentication Workflow

Swagger UI is available at `http://localhost:8080/swagger-ui.html`. To test protected endpoints directly in your browser:

1. Open `http://localhost:8080/swagger-ui.html` in your web browser.
2. Locate and expand `POST /api/v1/auth/login`. Click **Try it out**, enter your credentials, and click **Execute**.
3. Copy the string value of `accessToken` from the response body.
4. Scroll to the top of the Swagger page and click the green **Authorize** button (displaying an open padlock icon).
5. In the `bearerAuth` dialog, enter your token (or `Bearer <your-token>`) and click **Authorize**, then click **Close**.
6. The padlock icon will show as locked. You can now execute `GET /api/v1/users/me` directly within the browser interface.

---

### 5. Standard Error Contract (`ApiError`)

All error responses share a standard JSON structure defined by [ApiError](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/common/error/ApiError.java):

```json
{
  "timestamp": "2026-10-27T04:45:00.000Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "fieldErrors": {
    "password": "Password must be between 12 and 128 characters"
  }
}
```

#### Standard Error Codes:

| HTTP Status | Error Code (`code`) | Trigger Condition |
| :--- | :--- | :--- |
| `400 Bad Request` | `VALIDATION_ERROR` | Validation failure on request body fields |
| `400 Bad Request` | `MALFORMED_JSON` | Syntax error in incoming JSON payload |
| `400 Bad Request` | `UNKNOWN_JSON_PROPERTY` | Request contains undeclared JSON property |
| `400 Bad Request` | `BAD_REQUEST` | Disallowed request format or Spring firewall rejection |
| `401 Unauthorized` | `UNAUTHORIZED` | Missing, invalid, or expired Bearer token on protected route |
| `401 Unauthorized` | `INVALID_CREDENTIALS` | Invalid email/password during login |
| `409 Conflict` | `EMAIL_ALREADY_REGISTERED` | Attempted registration with an existing email |
| `415 Unsupported` | `UNSUPPORTED_MEDIA_TYPE` | Request `Content-Type` is not `application/json` |
| `429 Too Many` | `RATE_LIMIT_EXCEEDED` | Exceeded 30 requests/min; response contains `Retry-After` header |
| `500 Server Error` | `INTERNAL_SERVER_ERROR` | Unhandled exception (sanitized in client response) |

---

## CORS & Frontend Integration

### Why Browsers Block Cross-Origin Requests

When connecting a single-page application (SPA) running on a different origin (such as a Next.js, React, or Vue application running on `http://localhost:3000` or `https://app.yourdomain.com`) to BackendForge running on `http://localhost:8080`, web browsers enforce the **Same-Origin Policy (SOP)**. Browsers issue an HTTP `OPTIONS` preflight request. If the backend does not return valid CORS response headers (`Access-Control-Allow-Origin`, `Access-Control-Allow-Methods`, `Access-Control-Allow-Headers`), the browser blocks the response.

### Current Implementation Status

> **Important**: CORS is **not configured out of the box** in BackendForge. Inspection of [SecurityConfig](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/security/SecurityConfig.java) confirms that no `cors()` filter or `CorsConfigurationSource` bean is registered.

### Safe Guidance for Frontend Integration

When integrating a frontend application, configure explicit allowed origins in your security configuration.

#### Recommended Implementation Pattern:

To enable CORS safely in your derived application:

1. Add a `CorsConfigurationSource` bean in [SecurityConfig](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/security/SecurityConfig.java):
   ```java
   @Bean
   public CorsConfigurationSource corsConfigurationSource(
           @Value("${app.cors.allowed-origins:http://localhost:3000}") List<String> allowedOrigins) {
       CorsConfiguration configuration = new CorsConfiguration();
       configuration.setAllowedOrigins(allowedOrigins);
       configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
       configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Correlation-ID"));
       configuration.setExposedHeaders(List.of("X-Correlation-ID", "Location", "Retry-After"));
       configuration.setMaxAge(3600L);

       UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
       source.registerCorsConfiguration("/**", configuration);
       return source;
   }
   ```
2. Enable CORS in the security filter chain:
   ```java
   http.cors(cors -> cors.configurationSource(corsConfigurationSource(allowedOrigins)))
   ```

> **Security Warning**:
> * **Never** configure wildcard origins (`"*"`) in conjunction with credentials (`allowCredentials(true)`). Doing so exposes authenticated sessions to cross-site credential exploitation and is prohibited by modern browsers.
> * Maintain allowed origins as an environment-configurable property so development origins (`http://localhost:3000`) and production origins (`https://app.yourdomain.com`) are kept strictly segregated.

---

## Reverse Proxy, Networking & Rate Limiting

### Current Rate-Limiting Design

BackendForge includes an in-memory sliding-window rate limiter implemented in [ClientRateLimiter](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/ratelimit/ClientRateLimiter.java) and [RateLimitInterceptor](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/ratelimit/RateLimitInterceptor.java):

* **Protected Routes**: Rate limiting applies to `POST /api/v1/auth/login` and `POST /api/v1/auth/register` (default: 30 requests per 60-second window).
* **Table Capacity**: Automatically tracks up to 10,000 active client windows in memory (`RATE_LIMIT_MAX_ENTRIES`). Expired entries are evicted automatically.

### Architecture Limitations

1. **Instance-Local**: Counters are stored entirely in local JVM memory. Counters are **not shared** across horizontally scaled application replicas and reset when the application restarts.
2. **Direct Remote Address**: [RateLimitInterceptor](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/ratelimit/RateLimitInterceptor.java) calls `request.getRemoteAddr()` directly to identify clients.

### Reverse Proxy & Forwarded Header Considerations

When deploying BackendForge behind a reverse proxy, load balancer, or CDN (such as NGINX, HAProxy, AWS ALB, or Cloudflare):

1. **The Shared Proxy IP Problem**:
   Because `request.getRemoteAddr()` returns the immediate TCP socket peer, it will see the IP address of the reverse proxy, not the originating client. If left unconfigured, **all incoming users will share a single rate-limiting bucket**, leading to false-positive rate limit rejections (`HTTP 429`).
2. **Do Not Trust Arbitrary Forwarded Headers**:
   Reading client-supplied `X-Forwarded-For` headers directly without a trusted proxy is unsafe. Attackers can forge arbitrary `X-Forwarded-For` header values to bypass rate limits or pollute in-memory tables.
3. **Production Reverse Proxy Configuration**:
   BackendForge does **not** enable `server.forward-headers-strategy` by default. When deploying behind a trusted reverse proxy:
   * Configure Spring Boot to process proxy headers safely:
     ```yaml
     server:
       forward-headers-strategy: framework
     ```
   * Or configure Tomcat's trusted internal proxies:
     ```yaml
     server:
       tomcat:
         remoteip:
           internal-proxies: "10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|172\\.(1[6-9]|2[0-9]|3[0-1])\\.\\d{1,3}\\.\\d{1,3}|192\\.168\\.\\d{1,3}\\.\\d{1,3}"
     ```
   * **Network Isolation**: Ensure your network firewall prevents public traffic from reaching the backend port directly, guaranteeing that only the trusted reverse proxy can forward requests.

---

## Database Migration Guide

BackendForge uses Flyway Community for forward-only, immutable database migrations.

### Adding a New Migration

To add a new database table or alter an existing schema:

1. **Determine the Next Version Number**:
   Inspect existing migrations in `src/main/resources/db/migration/`. The baseline migration is [V1__create_users_table.sql](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/resources/db/migration/V1__create_users_table.sql). The next migration must be named `V2__<description>.sql`.
2. **Create the Migration Script**:
   Create a new file: `src/main/resources/db/migration/V2__create_projects_table.sql`:
   ```sql
   CREATE TABLE projects (
       id UUID PRIMARY KEY,
       user_id UUID NOT NULL,
       name VARCHAR(100) NOT NULL,
       description TEXT,
       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
       CONSTRAINT fk_projects_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
       CONSTRAINT ck_projects_name_not_blank CHECK (btrim(name) <> '')
   );

   CREATE INDEX idx_projects_user_id ON projects(user_id);
   ```

3. **Hibernate Schema Validation Synchronization**:
   BackendForge configures `spring.jpa.hibernate.ddl-auto: validate`.
   * **Execution Order**: Flyway executes new migrations during application startup *before* Hibernate initializes.
   * **Entity Validation**: Hibernate compares your Java `@Entity` classes against the database schema. If an `@Entity` has a column or table that does not exist in PostgreSQL, or if column types/lengths mismatch, startup terminates with a `SchemaManagementException`.
   * **Rule**: Always create and verify the Flyway SQL migration script *before* or *simultaneously* with updating JPA entity classes.

4. **Backward-Compatible Schema Evolution (Expand-Contract)**:
   * **Immutable History**: Never modify, delete, or reorder a migration file that has already run in any environment. Flyway validates migration checksums against `flyway_schema_history`; altering a deployed script halts application startup.
   * **Expand**: Add new columns as `NULL` or with explicit database defaults so existing application instances continue functioning during rolling deployments.
   * **Contract**: Author a subsequent migration (e.g., `V3__...sql`) to drop unused columns once older application versions are fully decommissioned.

---

## Troubleshooting Guide

### 1. PostgreSQL Connection Refused or Database Does Not Exist

* **Symptom**:
  ```
  org.postgresql.util.PSQLException: Connection to localhost:5432 refused
  ```
  or:
  ```
  org.postgresql.util.PSQLException: FATAL: database "backendforge" does not exist
  ```
* **Causes**:
  * PostgreSQL service is stopped or not listening on the configured port.
  * Application is pointing to `localhost:5432` while Compose PostgreSQL is running on host port `5434`.
  * The database `backendforge` has not been created.
* **Resolution**:
  1. For local PostgreSQL, verify service status and create the database:
     ```bash
     psql -U postgres -c "CREATE DATABASE backendforge;"
     ```
  2. If using Docker Compose PostgreSQL, set `DB_URL=jdbc:postgresql://localhost:5434/backendforge`.
  3. Ensure network firewalls permit TCP connections on the target port.

---

### 2. Port Conflicts (5432, 5433, 5434, 8080)

* **Symptom**:
  ```
  Web server failed to start. Port 8080 was already in use.
  ```
  or Docker error:
  ```
  Bind for 0.0.0.0:5434 failed: port is already allocated
  ```
* **Why Port 5434?**
  BackendForge's [compose.yaml](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/compose.yaml) maps PostgreSQL to host port `5434` intentionally to avoid collisions with standard PostgreSQL (`5432`) and common secondary instances (`5433`).
* **Identify the Conflicting Process**:
  * **Windows PowerShell**:
    ```powershell
    Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
    netstat -ano | findstr :8080
    ```
  * **Linux / macOS**:
    ```bash
    lsof -i :8080
    # or:
    ss -tulpn | grep 8080
    ```
* **Resolution**:
  * Stop the conflicting application or container.
  * Or reconfigure `SERVER_PORT` (e.g., `export SERVER_PORT=8081` or `$env:SERVER_PORT = "8081"`).

---

### 3. Invalid Base64 JWT Secret or Insufficient Key Length

* **Symptom**:
  ```
  java.lang.IllegalStateException: JWT_SECRET must be configured with a Base64-encoded secret
  ```
  or:
  ```
  java.lang.IllegalStateException: JWT_SECRET must decode to at least 32 bytes
  ```
* **Causes**:
  * `JWT_SECRET` is unset or blank.
  * Secret contains non-Base64 characters.
  * Decoded byte array is less than 32 bytes (256 bits).
* **Resolution**:
  Generate a compliant 32-byte Base64 key:
  * Linux/macOS: `openssl rand -base64 32`
  * Windows PowerShell: `[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))`

---

### 4. Missing Environment Variables in Production

* **Symptom**:
  ```
  java.lang.IllegalStateException: Application failed to start with active profile 'prod': DB_URL must be configured
  ```
* **Causes**:
  `SPRING_PROFILES_ACTIVE=prod` activates [ProductionConfiguration](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/config/ProductionConfiguration.java), which forbids development fallbacks.
* **Resolution**:
  Ensure all mandatory production variables are exported in your environment: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_ISSUER`, and `JWT_AUDIENCE`.

---

### 5. Expired or Invalid JWTs

* **Symptom**:
  HTTP 401 Unauthorized:
  ```json
  {
    "status": 401,
    "code": "UNAUTHORIZED",
    "message": "Authentication is required"
  }
  ```
* **Causes**:
  * Token lifetime expired (default: 900 seconds / 15 minutes).
  * Secret mismatch (token signed with a different `JWT_SECRET` than currently configured).
  * Claim mismatch: Token `iss` or `aud` does not match server `JWT_ISSUER` or `JWT_AUDIENCE`.
  * Missing `Bearer ` prefix in the `Authorization` header.
* **Resolution**:
  Re-authenticate via `POST /api/v1/auth/login` and provide the fresh token.

---

### 6. Docker Daemon Unavailable

* **Symptom**:
  ```
  java.lang.IllegalStateException: Could not find a valid Docker environment (see https://www.testcontainers.org/supported_docker_environment/)
  ```
* **Causes**:
  Docker Desktop or Docker Engine is not running when executing integration tests.
* **Resolution**:
  * Start Docker Desktop / Docker Engine.
  * Or run unit tests only (which do not require Docker):
    * Linux/macOS: `./mvnw test "-Dtest=*Test,!*IntegrationTest"`
    * Windows PowerShell: `.\mvnw.cmd test "-Dtest=*Test,!*IntegrationTest"`

---

### 7. Maven Wrapper Execution or Script Issues

* **Symptom**:
  * Linux/macOS: `bash: ./mvnw: Permission denied`
  * Windows: `.\mvnw.cmd : File ... cannot be loaded because running scripts is disabled on this system.`
* **Resolution**:
  * **Linux / macOS**: Grant executable permissions:
    ```bash
    chmod +x mvnw
    ```
  * **Windows PowerShell**: Set process execution policy:
    ```powershell
    Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
    ```
    Or run `.\mvnw.cmd` directly from `cmd.exe` or PowerShell.
  * **Network / Proxy Issues**: If the wrapper fails downloading Maven, verify your proxy settings or use an installed Maven distribution (`mvn`).

---

### 8. HTTP 401, 403, and 429 Responses

* **HTTP 401 (Unauthorized)**:
  * Route requires authentication and token is missing, expired, or invalid.
  * Or login failed with `INVALID_CREDENTIALS`.
* **HTTP 403 (Forbidden)**:
  * BackendForge enforces stateless authentication (`anyRequest().authenticated()`). An unauthenticated request triggers 401. If custom role authorization rules are added and an authenticated user lacks permissions, Spring Security returns 403 Forbidden.
* **HTTP 429 (Too Many Requests)**:
  * Returns code `RATE_LIMIT_EXCEEDED`.
  * Triggered when a client exceeds 30 login or registration requests per 60 seconds from the same remote IP.
  * Inspect the `Retry-After: <seconds>` response header before repeating requests.

---

## Observability & Production Diagnostics

1. **Request Correlation (`X-Correlation-ID`)**:
   * Every request is assigned a correlation ID by [CorrelationIdFilter](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/common/correlation/CorrelationIdFilter.java).
   * Clients can pass an `X-Correlation-ID` (or `X-Request-ID`) header. Valid IDs (alphanumeric, hyphens, underscores, dots, $\le 64$ chars) are retained; multiline or invalid IDs are replaced with a fresh UUID.
   * `X-Correlation-ID` is returned on all HTTP responses and logged via SLF4J MDC (`correlationId`).
2. **Health Probes**:
   * `/actuator/health/readiness` and `/actuator/health/liveness` provide container readiness/liveness checks.
   * `management.endpoint.health.show-details: never` prevents leakage of database details or connection strings.
   * All other Actuator management endpoints are unexposed.
3. **Log Sanitization & Hygiene**:
   * Exception handlers log only HTTP method, URI, remote IP, and exception class names.
   * Passwords, authorization tokens, JWT claims, request bodies, and connection credentials are never logged.

---

## Production Deployment Checklist

Before deploying BackendForge to production:

- [ ] **1. Active Profile**: Set `SPRING_PROFILES_ACTIVE=prod` to enforce fail-fast production validation.
- [ ] **2. Database Credentials & Connectivity**:
  - Supply production `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
  - Ensure the database user has permissions for Flyway migration history (`flyway_schema_history`).
  - Keep `spring.jpa.hibernate.ddl-auto: validate` enabled.
- [ ] **3. JWT Signing Key & Claims**:
  - Generate a secure, unique Base64 secret ($\ge 32$ bytes) using a cryptographically secure random source.
  - Set `JWT_ISSUER` and `JWT_AUDIENCE` to your organization's domain.
- [ ] **4. Trusted Reverse Proxy & Network Security**:
  - Terminate TLS at your ingress, load balancer, or reverse proxy.
  - Configure `server.forward-headers-strategy=framework` or trusted remote IP properties when running behind an internal reverse proxy.
  - Restrict backend network access so public traffic cannot bypass the proxy.
- [ ] **5. Ingress Rate Limiting**:
  - Because [ClientRateLimiter](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/src/main/java/com/thecreatorco/backendforge/ratelimit/ClientRateLimiter.java) is instance-local, deploy edge rate limiting (Cloudflare, AWS WAF, or NGINX) for multi-replica production environments.
- [ ] **6. Container Operations**:
  - Run the container as non-root `appuser` (UID `10001`).
  - Configure container health checks against `/actuator/health/readiness`.
  - Allow at least 25–30 seconds for graceful shutdown (`timeout-per-shutdown-phase: 20s`).

---

## Customer & Production Clarity

* **Starter Kit Nature**: BackendForge is a production-hardened backend starter kit and architectural foundation authored by **The Creator Co.** It is intended to be customized, extended, and integrated into your organization's deployment environment. It requires environment-specific security reviews, secrets management, and infrastructure configuration before production use. It is sold as a source-code starter kit, not a hosted SaaS or cloud service.
* **Commercial Licensing Model**: BackendForge is licensed under an **unlimited-project model** (allowing unlimited personal, SaaS, and client project deployments, with strict prohibitions on reselling or redistributing the starter kit itself). The included [LICENSE.md](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/LICENSE.md) file is a preliminary commercial license draft provided for legal review and customization. It does not constitute legal advice and must be finalized by licensed legal counsel before commercial release. See [COMMERCIAL-RELEASE-BLOCKERS.md](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/docs/COMMERCIAL-RELEASE-BLOCKERS.md) for the active checklist of legal, business, and verification decisions.
* **Update Policy**: Licenses grant perpetual permission to use the purchased version (`1.0.1`). Future major versions and feature updates are sold separately; no free lifetime updates or ongoing maintenance obligations are included.
* **Commercial Launch Preparation**: Pre-launch product listing copy is maintained in [GUMROAD-PRODUCT-LISTING-DRAFT.md](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/docs/GUMROAD-PRODUCT-LISTING-DRAFT.md), and operational merchant setup steps are tracked in [GUMROAD-LAUNCH-CHECKLIST.md](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/docs/GUMROAD-LAUNCH-CHECKLIST.md).
* **Third-Party Dependency Notices**: The included [THIRD-PARTY-NOTICES.md](file:///c:/Users/AnkitAccess/Downloads/BackendForge-main/THIRD-PARTY-NOTICES.md) file provides comprehensive open-source licensing attributions. Third-party dependencies are fetched directly from Maven Central and are not bundled in binary form in the release archive. If distributing precompiled binaries or container images, distributors must perform an audit to ensure complete compliance with upstream licenses (such as LGPL-2.1 and EPL).
* **Support and Warranties**: BackendForge is provided on an "AS IS" basis without express or implied warranties or technical support guarantees unless separately contracted.


---

## Dependency Versions

* **Spring Boot**: `3.5.6`
* **Java**: `21` (LTS)
* **Springdoc OpenAPI**: `2.8.13`
* **PostgreSQL**: `16.4` (Compose) / `15` (Testcontainers)
* **JJWT (Java JWT)**: `0.12.6`
* **Flyway**: `11.7.2`
* **Testcontainers**: `2.0.5`
