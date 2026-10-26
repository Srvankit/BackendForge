# BackendForge

BackendForge is a reusable Spring Boot foundation for building secure, maintainable REST APIs. It uses Java 21, Maven, PostgreSQL, Spring Data JPA, Spring Security, Bean Validation, Flyway, and OpenAPI documentation.

This foundation includes PostgreSQL persistence, user registration, JWT-based authentication, and reproducible local Docker development.

## Prerequisites

- Java 21
- Maven 3.9 or newer
- PostgreSQL 15 or newer for running the application
- Docker Desktop or another Docker-compatible runtime for the Testcontainers integration tests

Verify Java and Maven:

```bash
java -version
mvn -version
```

## Configuration

The default profile is intended for local development and provides safe local-development defaults:

| Variable | Default | Description |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/backendforge` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `backendforge` | Database username |
| `DB_PASSWORD` | `backendforge` | Database password |
| `POSTGRES_DB` | `backendforge` | Compose PostgreSQL database name |
| `POSTGRES_USER` | `backendforge` | Compose PostgreSQL username |
| `POSTGRES_PASSWORD` | required for Compose | Compose PostgreSQL password |
| `SERVER_PORT` | `8080` | HTTP port |
| `JWT_SECRET` | required | Base64-encoded JWT signing secret decoding to at least 32 bytes |
| `JWT_EXPIRATION_SECONDS` | `900` | Access-token lifetime in seconds |
| `JWT_ISSUER` | `backendforge` | Required issuer claim for access tokens |
| `JWT_AUDIENCE` | `backendforge-api` | Required audience claim for access tokens |
| `RATE_LIMIT_LOGIN_MAX_REQUESTS` | `30` | Login requests allowed per client window |
| `RATE_LIMIT_LOGIN_WINDOW` | `60s` | Login limiter window |
| `RATE_LIMIT_REGISTER_MAX_REQUESTS` | `30` | Registration requests allowed per client window |
| `RATE_LIMIT_REGISTER_WINDOW` | `60s` | Registration limiter window |
| `RATE_LIMIT_MAX_ENTRIES` | `10000` | Maximum active client windows retained in memory |

Copy `.env.example` to `.env` for Docker Compose. Compose reads `.env` for interpolation and explicitly passes the required application variables to the backend container; Spring Boot does not load `.env` files automatically. Compose derives the backend database URL, username, and password from `POSTGRES_DB`, `POSTGRES_USER`, and `POSTGRES_PASSWORD`, so customized PostgreSQL credentials remain aligned. For host execution against the existing PostgreSQL container, use `jdbc:postgresql://localhost:5433/backendforge` instead.

For production, activate the explicit `prod` profile with `SPRING_PROFILES_ACTIVE=prod` and provide `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_SECONDS`, `JWT_ISSUER`, and `JWT_AUDIENCE`. The production profile has no database or JWT defaults and fails during startup when any required setting is missing or invalid. Generate a secret with `openssl rand -base64 32`; never reuse the example values or commit production credentials.

The database schema is set to `validate`, and Flyway applies versioned migrations from `src/main/resources/db/migration`. The first migration creates the `users` table with a UUID primary key, unique email address, password hash storage, enabled status, and creation/update timestamps. Application startup does not load `.env` automatically; export the variables in your shell or configure them in your IDE.

User registration is available at `POST /api/v1/auth/register`. The endpoint trims and lowercases email addresses, hashes passwords with BCrypt, and returns only the user ID, normalized email, enabled status, and creation timestamp. New passwords must be between 12 and 128 characters and no more than 72 UTF-8 bytes, which matches BCrypt's effective input limit. Multibyte passwords are measured by encoded UTF-8 byte length; new registrations over the limit are rejected rather than silently truncated. The existing login character limit is retained so previously stored password hashes remain usable.

Login is available at `POST /api/v1/auth/login`:

```json
{
  "email": "user@example.com",
  "password": "correct-horse-battery"
}
```

Successful login returns an access token:

```json
{
  "accessToken": "<signed-jwt>",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

Failed login (unknown email, wrong password, or disabled account) returns the same HTTP 401 `INVALID_CREDENTIALS` body. Password verification still runs when no matching enabled account exists so response timing does not trivially disclose account existence. Exception handlers log only exception type names for unexpected failures; passwords, tokens, and request bodies are not written to application logs.

Duplicate registration for an existing email still returns HTTP 409 `EMAIL_ALREADY_REGISTERED`. That conflict response is intentional for client UX and can reveal whether an email is registered; registration rate limiting reduces bulk probing. Changing that status would break the existing API contract.

Send the token to protected endpoints using an `Authorization: Bearer <access-token>` header. Access tokens include and require the configured issuer, audience, and `token_use=access` claim. Tokens issued before these required claims were introduced will be rejected after deployment. Sessions are stateless; registration and login are public, while other endpoints require a valid token. Generate a local secret with `openssl rand -base64 32` and provide it through `JWT_SECRET`. Use a secret-management system rather than committing secrets in deployment environments.

Login and registration have separate configurable in-memory request limits. Limits are keyed by the request's direct remote address; forwarded headers are not trusted. Requests over a limit receive HTTP 429 with the standard `ApiError` response and a `Retry-After` header. The limiter runs before controller validation and service/database/BCrypt work, and does not apply to health checks or unrelated authenticated routes. Invalid rate-limit settings (non-positive limits or zero/negative windows) fail application startup.

The limiter is per application instance, is reset when the application restarts, and uses bounded expiring state. When the in-memory client-window map is full, additional distinct clients receive HTTP 429 rather than evicting active quotas. It is not shared across replicas, does not survive process restarts, and is not a substitute for a trusted edge/WAF or distributed rate limiter. It also does not fully stop credential stuffing or distributed attacks that rotate source addresses. Tune thresholds with load testing; multi-instance production deployments should enforce limits at a trusted gateway or shared store in addition to these per-instance defaults.

## Build and test

Run the full Maven verification. The integration test starts an isolated PostgreSQL 15 container, applies Flyway migrations, verifies the expected schema, and starts the Spring application context with Hibernate validation enabled:

```bash
mvn clean test
```

Docker must be running before executing the integration test. The test does not use or modify a personal PostgreSQL database.

## Run the application

After PostgreSQL is available and the environment variables are configured, start the application:

```bash
mvn spring-boot:run
```

The application listens on `http://localhost:8080` by default. OpenAPI JSON is exposed at `/v3/api-docs`, and the Swagger UI is available at `/swagger-ui.html`. Readiness is available at `/actuator/health/readiness`; only health endpoints are exposed by the management API, and health details are intentionally hidden.

## Docker local development

### Prerequisites

- Docker Desktop or another Docker-compatible runtime with Compose support
- A local `.env` file created from `.env.example`
- A Base64-encoded JWT secret that decodes to at least 32 bytes

Generate a suitable development secret with:

```bash
openssl rand -base64 32
```

Set the generated value as `JWT_SECRET` in `.env`. Compose activates the `prod` profile and passes all required settings explicitly. Use different credentials and secrets for shared or production environments.

### Start the isolated Compose environment

The Compose environment uses its own PostgreSQL service and named volume. It does not use the existing `backendforge-postgres` container or the `backendforge-postgres-data` volume.

```bash
docker compose up --build
```

The backend is available at `http://localhost:8080`, OpenAPI JSON at `http://localhost:8080/v3/api-docs`, and Swagger UI at `http://localhost:8080/swagger-ui.html`. Compose PostgreSQL is available from the host at `localhost:5434`; inside Compose, the backend connects to `postgres:5432`. Compose waits for PostgreSQL health and reports backend readiness through `/actuator/health/readiness`.

The existing host PostgreSQL container remains separate at `localhost:5433`. Always check `DB_URL` before running host-based commands so that migrations and application requests target the intended database. Do not use the existing host URL in the Compose backend configuration.

Stop the Compose services without deleting their data with:

```bash
docker compose down
```

The Compose database persists in the `backendforge-compose-postgres-data` volume. Do not use volume-prune commands. The existing `backendforge-postgres-data` volume is unrelated and must not be removed or reused.

Common startup problems:

- If port `8080` is already in use, stop the conflicting local process or set a different `SERVER_PORT` and matching host mapping.
- If port `5434` is already in use, change only the host side of the PostgreSQL mapping in `compose.yaml`.
- If the backend cannot connect to PostgreSQL, verify that `POSTGRES_PASSWORD` is set in `.env` and inspect `docker compose logs postgres` without printing credentials.
- If the backend remains unhealthy, check `docker compose ps` and inspect backend logs for startup or migration errors. The health check uses the image's installed `curl` utility and does not expose health details.
- If JWT startup validation fails, replace `JWT_SECRET` with a Base64 value that decodes to at least 32 bytes and verify the issuer, audience, and expiration settings.

## Project structure

Application code is rooted at `com.thecreatorco.backendforge`. Feature packages such as `auth`, `user`, `security`, `config`, `common`, and `exception` will be added as concrete functionality is implemented; empty placeholder classes are intentionally not created.

## Dependency versions

- Spring Boot `3.5.6`, which supports Java 21 and provides the dependency-management baseline for the Spring ecosystem.
- Java `21`, the current long-term-support Java release targeted by the project.
- springdoc OpenAPI `2.8.13`, compatible with Spring Boot 3 and Spring Framework 6.
- PostgreSQL and Flyway versions are managed by the Spring Boot parent to keep their transitive dependencies aligned.
