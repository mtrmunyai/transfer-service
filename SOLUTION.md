# Transfer Service Solution Documentation

## Project Overview

This project implements a **Transfer Service** in Java using Spring Boot. It provides APIs to create single and batch
transfers, manage transfer statuses, and clean up old transfers.

---

## Technologies Used

- Java 17
- Spring Boot 3.x
- Spring Data JPA
- Spring Security (disabled for open access)
- Spring Validation
- Resilience4J for Circuit Breaker
- H2 In-Memory Database for development/testing
- Liquibase for database migrations
- Lombok for boilerplate code reduction
- Springdoc OpenAPI for API documentation
- Docker for containerization

---

## Key Components

### 1. Transfer Entity

- Uses Lombok annotations (`@Data`, `@Builder`, etc.) for clean code.
- Fields include `id`, `version`, `fromAccountId`, `toAccountId`, `amount`, `status`, and `created`.
- `update()` method to change transfer status.

### 2. TransferService

- Handles business logic for creating transfers and batches.
- Uses `RestTemplate` to call external ledger service.
- Applies Resilience4J Circuit Breaker for fault tolerance with fallback methods.
- Batch processing is done asynchronously using a fixed thread pool executor.
- Validates batch size and input data.

### 3. TransferController

- REST endpoints for:
    - Creating single transfer (`POST /transfers`)
    - Creating batch transfers (`POST /transfers/batch`)
    - Getting transfer status (`GET /transfers/{id}`)
- Uses OpenAPI annotations for auto-generated documentation.

### 4. TransferCleanupService

- Scheduled cleanup of old transfers older than 24 hours.
- Runs hourly using Spring’s `@Scheduled` annotation.

### 5. Configuration Classes

- **AsyncConfig**: Configures thread pool executor.
- **SecurityConfig**: Disables security for all endpoints.
- **LedgerServiceProperties**: Externalizes ledger service endpoint configuration.

---

## Testing

- Unit tests use JUnit 5 and Mockito.
- `TransferServiceTest` covers all main methods including fallback scenarios.
- `TransferCleanupServiceTest` verifies scheduled cleanup logic.
- Tests include assertions using `assertAll` for better failure reporting.

---

## Build and Run

### Build

```bash
mvn clean package

java -jar target/transfer-service-1.0.0.jar

version: '3.1'

services:
  transfer-service:
    image: openjdk:17
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:h2:mem:ledgerdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
      - SPRING_DATASOURCE_USERNAME=sa
      - SPRING_DATASOURCE_PASSWORD=
      - SPRING_JPA_HIBERNATE_DDL_AUTO=update
    volumes:
      - ./target/transfer-service-1.0.0.jar:/app.jar
    entrypoint: java -jar /app.jar --spring.profiles.active=DOCKER
```

### Run with:

docker-compose up

### API Documentation

Accessible via Swagger UI at:

http://localhost:8080/swagger-ui.html

## Code Coverage

JaCoCo configured with minimum 50% instruction coverage.

Excludes application main class and test classes.

Reports generated during Maven test phase.

## Notes

The ledger service URL is configurable via application.properties or environment variables.

Batch transfers limited to max 20 requests per call.

Uses H2 in-memory DB for ease of testing, can be switched to a persistent DB by configuration.

Circuit breaker fallbacks provide resilience in case of downstream failures.

## What to Improve

- Error Handling: Enhance error responses with more detailed error bodies and use consistent HTTP status codes for
  client and server errors.

- Security: Implement proper authentication and authorization instead of disabling security completely.

- Input Validation: Add more robust validation rules and custom validators for transfer request fields.

- Logging: Include more contextual information in logs for better traceability in distributed systems.

- API Versioning: Implement versioning for APIs to support backward compatibility.

- Monitoring & Metrics: Integrate monitoring tools (e.g., Micrometer, Prometheus) to track service health and
  performance.

- Performance: Improve batch processing efficiency and consider reactive programming for better scalability.

- Database: Move from in-memory H2 to a production-grade database such as PostgreSQL or MySQL.

- Testing: Increase test coverage, add integration and end-to-end tests, especially for the REST endpoints and database
  interactions.

- Configuration Management: Support externalized configuration via Spring Cloud Config or Kubernetes ConfigMaps for
  easier deployments.

- Documentation: Expand API documentation with usage examples, error codes, and detailed schemas.

- Resilience: Add retries with backoff and rate limiting to complement the circuit breaker pattern.

- Asynchronous Communication: Consider messaging queues (e.g., RabbitMQ, Kafka) for decoupled and resilient batch
  processing.