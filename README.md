# BackendForge

BackendForge is a reusable Spring Boot foundation for building secure, maintainable REST APIs. It uses Java 21, Maven, PostgreSQL, Spring Data JPA, Spring Security, Bean Validation, Flyway, and OpenAPI documentation.

This first foundation intentionally contains no authentication flows, user management, business endpoints, database migrations, Docker configuration, or deployment configuration.

## Prerequisites

- Java 21
- Maven 3.9 or newer
- PostgreSQL 15 or newer for running the application

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

Copy `.env.example` to `.env` for local reference, then export the variables through your shell or IDE. Do not commit `.env` or real credentials.

The database schema is set to `validate`, and Flyway is enabled for future versioned migrations. No migrations are included in this foundation.

## Build and test

Run the full Maven verification:

```bash
mvn clean test
```

## Run the application

After PostgreSQL is available and the environment variables are configured:

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
