# Repository Instructions

## Project overview

This repository contains a Spring Boot web application written in Java 21 and built with Maven. The base package is `com.appsdeveloperblog.todoapp`.

## Architecture

- Keep application code under `src/main/java` and tests under `src/test/java`.
- Organize features into clear layers: web/controllers, services, and persistence/repositories as the application grows.
- Keep controllers focused on HTTP concerns, services responsible for business logic, and repositories responsible for data access.
- Place configuration in `src/main/resources`, using Spring configuration and environment variables for deployment-specific values.

## Coding and naming conventions

- Follow standard Java conventions and the existing tab-based formatting.
- Use small, focused classes and methods; prefer clear, readable code over clever abstractions.
- Use `UpperCamelCase` for classes, records, and interfaces; `lowerCamelCase` for methods, fields, and parameters; and `UPPER_SNAKE_CASE` for constants.
- Name classes by their role, such as `TodoController`, `TodoService`, and `TodoRepository`.
- Use descriptive names and avoid unexplained abbreviations.
- Keep public APIs and method parameters typed explicitly; use immutable data where practical.

## Dependency injection

- Use Spring-managed beans and constructor injection.
- Prefer a single explicit constructor; use `final` fields for injected dependencies.
- Avoid field injection and avoid creating Spring-managed dependencies manually with `new`.
- Depend on interfaces where this improves substitutability, but do not add abstractions without a clear need.

## Testing expectations

- Add or update tests for every behavioral change.
- Use JUnit and Spring Boot's test support already provided by the Maven build.
- Keep unit tests focused and fast; use Spring context or web-layer tests when framework integration is part of the behavior.
- Test successful paths, validation, and relevant error cases. Tests should be deterministic and independent.

## Maven commands

Use the Maven wrapper from the repository root:

```bash
./mvnw test
./mvnw verify
./mvnw spring-boot:run
```

Use `./mvnw.cmd` on Windows. Run `./mvnw test` after code changes and `./mvnw verify` before submitting substantial changes.

## Error handling

- Validate external input at the web boundary and return appropriate HTTP status codes.
- Use domain-specific exceptions for expected business failures and map them consistently with a centralized exception handler.
- Do not expose stack traces, secrets, or internal implementation details in API responses.
- Log actionable diagnostic information at the appropriate level without logging credentials or sensitive user data.

## General instructions

- Keep changes scoped to the requested behavior and preserve existing public behavior unless a change is intentional.
- Prefer existing Spring Boot and JDK capabilities before adding dependencies.
- Keep configuration environment-specific and never commit secrets.
- Update relevant tests and documentation when behavior or public interfaces change.
