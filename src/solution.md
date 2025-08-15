**TransferService Solution Documentation
Overview**

````
The TransferService is a Spring Boot application providing REST APIs to handle financial transfers between accounts. 
It integrates with a Ledger service to perform actual transfers and manage account details.
````

**Architecture & Components**
1. TransferServiceApplication

The main Spring Boot application entry point.

Boots up the application and configures context.

2. **TransferController**

```
REST Controller exposing endpoints:

POST /transfers — create a single transfer.

POST /transfers/batch — create multiple transfers in one request.

GET /transfers/{id} — fetch status of a specific transfer. 
```

Annotated with Swagger/OpenAPI annotations for API documentation.

Uses TransferService to handle business logic.

3. **TransferService**

Core business logic layer.

Methods:
````
createTransfer(TransferRequest) — sends a transfer request to Ledger service.

createBatch(List<TransferRequest>) — processes batch transfer requests with validation on batch size.

getStatusByTransferId(String) — retrieves transfer status from Ledger service.
````

Uses Resilience4j Circuit Breaker to handle failures with fallback methods:
````
fallbackCreateTransfer

fallbackGetStatus
````
Uses RestTemplate to communicate with Ledger service REST API.

4. DTOs (Data Transfer Objects)
````
TransferRequest — request payload for transfers; includes fields like transferId, fromAccountId, toAccountId, and amount.

TransferDTO — internal DTO sent to Ledger service; matches the contract expected by Ledger API.

Both DTOs include validation constraints and Swagger schema annotations.
````
5. Configuration
````
LedgerServiceProperties — binds configuration properties from application.yml (e.g., Ledger base URL and path).

SecurityConfig — disables Spring Security for simplicity, permitting all requests.
````
6. Testing
````
TransferControllerRestTemplateTest — integration tests using TestRestTemplate and @MockBean to mock TransferService.

Tests create transfer, batch transfer, and get transfer status endpoints.

ActuatorEndpointTest — tests Spring Boot actuator endpoints for health checks and exposure.

Unit tests for TransferService (not shown here) ensure business logic correctness including fallback methods.
````
7. Application Configuration
````
application.yml configures:

Server port (8080)
````
Management actuator endpoints exposure (/actuator/health)

Ledger service endpoint URLs.

Logging levels and file.

Disables Liquibase for DB migration (optional).
````
8. OpenAPI Specification
````
Defines the Ledger service API contract:

/ledger/transfer for transfers

/accounts for account creation

/accounts/{id} for fetching account details
````

Includes JSON schemas for TransferDTO and AccountDTO.

Supports Swagger UI generation for API exploration.

How It Works
````
````
Client calls TransferController endpoints with validated requests.

TransferController forwards requests to TransferService.

TransferService builds TransferDTO, calls Ledger service REST API via RestTemplate.

CircuitBreaker handles failures gracefully using fallback methods.

Responses returned to clients with operation status.
````

**Future Improvements**
````
Add detailed error handling and standardized error response schemas.

Enable authentication and authorization.

Implement asynchronous processing for batch transfers.

Add database persistence for transfers.

Enhance Swagger docs with examples and better response descriptions.

Integrate with real Ledger backend for live operations.
````

**Summary**
````
This project provides a robust, scalable microservice for handling financial transfers using Spring Boot, 
Resilience4j for fault tolerance, and OpenAPI for documentation. 
It is well-tested and easily extendable for production use.
````