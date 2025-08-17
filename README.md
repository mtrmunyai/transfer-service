# Transfer Service

A Spring Boot microservice that handles money transfers between accounts, supporting single and batch transfers with
resiliency patterns and asynchronous processing.

---

## Features

- Create single and batch transfers
- Query transfer status by ID
- Resilience using Resilience4j Circuit Breaker
- Async batch processing with thread pool executor
- Scheduled cleanup of old transfers
- In-memory H2 database for persistence
- OpenAPI (Swagger) API documentation
- Dockerized for containerized deployments
- Lombok for boilerplate reduction
- API validation and exception handling

---

## Technologies Used

- Java 17
- Spring Boot 3
- Spring Data JPA & JDBC
- Resilience4j
- Lombok
- H2 Database
- Liquibase
- Springdoc OpenAPI UI
- Docker
- JUnit 5 & Mockito (Testing)

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker (optional, for containerized run)

### Build the project

```bash
mvn clean package
```

### Run locally

java -jar target/transfer-service-1.0.0.jar

The service will start on port 8080 by default.

### Run with Docker

Make sure you have built the jar (```mvn clean package```).

```docker-compose up --build```

### API Endpoints

| Method | Endpoint           | Description                 |
| ------ | ------------------ | --------------------------- |
| POST   | `/transfers`       | Create a single transfer    |
| POST   | `/transfers/batch` | Create a batch of transfers |
| GET    | `/transfers/{id}`  | Get status by transfer ID   |

### Configuration

Externalize properties in application.yml or environment variables:

````
spring:
    datasource:
        url: jdbc:h2:mem:ledgerdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
        username: sa
        password:
    jpa:
        hibernate:
            ddl-auto: update

ledger:
    service:
        endpoint:
            base-url: http://ledger-service
            path: /api/ledger/transfer
````

### Improvements & Next Steps

- API Versioning: Add explicit API versioning to support backward compatibility (e.g., /api/v1/transfers).

- Authentication & Authorization: Integrate Spring Security with proper auth mechanisms.

- Error Handling: Enhance error response structures and logging.

- Persistence: Use production-grade DB instead of in-memory H2.

- Metrics & Monitoring: Integrate with Prometheus/Grafana.

- CI/CD: Setup pipelines for automated builds and deployment.

- Documentation: Publish API docs to a developer portal or Confluence.

- Performance: Optimize batch processing and executor tuning.

### Demo
https://youtu.be/NApcotfsERA
