\# BackendForge — GitHub Copilot Instructions



\## Project Overview



BackendForge is a reusable backend starter kit developed by The Creator Co. It helps developers build REST APIs faster using a secure, maintainable, tested, and documented Spring Boot foundation.



\## Technology Stack



\* Java 21

\* Spring Boot

\* Maven

\* Spring Security

\* Spring Data JPA

\* PostgreSQL

\* Flyway

\* JWT authentication

\* Bean Validation

\* OpenAPI/Swagger

\* JUnit 5

\* Testcontainers

\* Docker and Docker Compose

\* GitHub Actions



Verify compatibility before selecting framework and dependency versions.



\## Architecture



Use a modular monolith architecture.



Organize code into clear packages for configuration, security, authentication, user management, common components, and exception handling.



Use:



\* Controllers for HTTP request handling.

\* Services for business logic.

\* Repositories for database access.

\* DTOs for API requests and responses.



Prefer constructor injection and avoid unnecessary abstractions.



Do not expose database entities directly through public API responses.



\## Coding Standards



\* Follow standard Java conventions.

\* Use descriptive names.

\* Keep methods focused and maintainable.

\* Validate external input.

\* Avoid duplicated logic.

\* Handle errors consistently.

\* Document important design decisions.

\* Do not create unnecessary placeholder classes.



\## Security



\* Hash passwords securely.

\* Never store plaintext passwords.

\* Never hardcode credentials, JWT secrets, or private keys.

\* Load secrets from environment variables or appropriate external configuration.

\* Validate JWT signatures and expiration.

\* Enforce authorization on protected endpoints.

\* Never log passwords, tokens, or sensitive authentication data.

\* Do not disable security protections to make tests pass.

\* Test invalid credentials and unauthorized access.



\## Database



\* Use PostgreSQL.

\* Use Flyway for versioned schema migrations.

\* Avoid automatic schema updates in production.

\* Use transactions where appropriate.

\* Never commit real database credentials.



\## API Design



\* Follow REST conventions.

\* Validate request data.

\* Use appropriate HTTP status codes.

\* Return consistent error responses.

\* Document endpoints with OpenAPI.

\* Include reusable pagination and filtering where appropriate.

\* Do not expose internal implementation details in API errors.



\## Testing



\* Write unit and integration tests for meaningful behavior.

\* Test authentication and authorization boundaries.

\* Test validation and exception handling.

\* Use Testcontainers for database integration tests where appropriate.

\* Never remove tests merely to obtain a passing build.

\* Never claim tests passed unless they were actually executed successfully.



\## Development Workflow



Before implementing a task:



1\. Inspect the existing repository.

2\. Explain the proposed changes.

3\. Make only the requested changes.

4\. Add or update relevant tests.

5\. Run appropriate verification commands.

6\. Report actual results and remaining issues.



Preserve existing work and avoid unnecessary changes.



Do not implement unrelated features without approval.



\## Commercial Product Quality



BackendForge is intended to become a commercial developer toolkit.



Prioritize reliable builds, secure defaults, reproducible setup, reusable code, automated testing, and clear documentation.



Do not claim the product is production-ready until its security, testing, documentation, and deployment requirements have been verified.



