# BackendForge

BackendForge is a reusable Spring Boot foundation for building secure, maintainable REST APIs. It uses Java 21, Maven, PostgreSQL, Spring Data JPA, Spring Security, Bean Validation, Flyway, and OpenAPI documentation.

This foundation includes PostgreSQL persistence, user registration, and JWT-based authentication. Business endpoints, Docker configuration, and deployment configuration remain outside the current scope.

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

The application reads database settings from environment variables and provides safe local-development defaults:

| Variable | Default | Description |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/backendforge` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `backendforge` | Database username |
| `DB_PASSWORD` | `backendforge` | Database password |
| `SERVER_PORT` | `8080` | HTTP port |
| `JWT_SECRET` | required | Base64-encoded JWT signing secret decoding to at least 32 bytes |
| `JWT_EXPIRATION_SECONDS` | `900` | Access-token lifetime in seconds |
| `JWT_ISSUER` | `backendforge` | Required issuer claim for access tokens |
| `JWT_AUDIENCE` | `backendforge-api` | Required audience claim for access tokens |

Copy `.env.example` to `.env` for local reference, then export the variables through your shell or IDE. Do not commit `.env` or real credentials.

The database schema is set to `validate`, and Flyway applies versioned migrations from `src/main/resources/db/migration`. The first migration creates the `users` table with a UUID primary key, unique email address, password hash storage, enabled status, and creation/update timestamps. Application startup does not load `.env` automatically; export the variables in your shell or configure them in your IDE.

User registration is available at `POST /api/v1/auth/register`. The endpoint trims and lowercases email addresses, hashes passwords with BCrypt, and returns only the user ID, normalized email, enabled status, and creation timestamp. Passwords must be between 12 and 128 characters; no arbitrary composition rules are imposed.

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

Send the token to protected endpoints using an `Authorization: Bearer <access-token>` header. Access tokens include and require the configured issuer, audience, and `token_use=access` claim. Tokens issued before these required claims were introduced will be rejected after deployment. Sessions are stateless; registration and login are public, while other endpoints require a valid token. Generate a local secret with `openssl rand -base64 32` and provide it through `JWT_SECRET`. Use a secret-management system rather than committing secrets in deployment environments.

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

The application listens on `http://localhost:8080` by default. OpenAPI JSON is exposed at `/v3/api-docs`, and the Swagger UI is available at `/swagger-ui.html`.

## Project structure

Application code is rooted at `com.thecreatorco.backendforge`. Feature packages such as `auth`, `user`, `security`, `config`, `common`, and `exception` will be added as concrete functionality is implemented; empty placeholder classes are intentionally not created.

## Dependency versions

- Spring Boot `3.5.6`, which supports Java 21 and provides the dependency-management baseline for the Spring ecosystem.
- Java `21`, the current long-term-support Java release targeted by the project.
- springdoc OpenAPI `2.8.13`, compatible with Spring Boot 3 and Spring Framework 6.
- PostgreSQL and Flyway versions are managed by the Spring Boot parent to keep their transitive dependencies aligned.
