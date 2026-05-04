# PetClinic Customer/Ownership Service

## Project Title & Description
**Service Name:** Customer Service  
**Bounded Context:** Customer/Ownership Domain  

The Customer Service is a core microservice within the Spring PetClinic ecosystem. It is responsible for managing the Customer and Ownership bounded context, acting as the authoritative source of truth for Owners, Pets, and Pet Types. This service handles the lifecycle of these entities, providing crucial validation endpoints for downstream services (such as the Visit Service) and broadcasting domain events to ensure consistency across the distributed architecture.

## Tech Stack
* **Java 21**: Leveraging virtual threads, records, and pattern matching for modern, high-performance execution.
* **Spring Boot 3.2.x**: Core framework for rapid microservice development, dependency injection, and auto-configuration.
* **Spring Data JPA & H2**: For robust ORM data persistence and an embedded database for lightweight local development and testing.
* **Spring Cloud Netflix Eureka**: For dynamic service discovery and registration within the microservice ecosystem.
* **Spring Cloud Stream & RabbitMQ**: For asynchronous, message-driven event broadcasting and loose coupling.
* **Springdoc OpenAPI (Swagger)**: For automated, interactive API documentation and contract definition.
* **Micrometer & OpenTelemetry**: For distributed tracing and system observability.

## Architectural Decisions (Crucial Section)

### Synchronous vs. Asynchronous
We employ a hybrid communication strategy tailored to the specific needs of the operation:
* **Synchronous REST**: Used for immediate data retrieval and inter-service validation (e.g., the `Internal` API used by the Visit Service to immediately verify if a Pet is active and belongs to a specific Owner). Synchronous HTTP ensures strict, real-time consistency where immediate business validation is mandatory.
* **Asynchronous Messaging (RabbitMQ)**: Used for state propagation and side-effects. When an entity's state changes (e.g., a pet is deactivated), the service publishes domain events via RabbitMQ. This embraces **Eventual Consistency**, completely decoupling the Customer Service from downstream consumers, improving overall system availability, and reducing latency during write operations.

### Resilience
*(Note: While downstream consumers actively implement Resilience4j to call this service, resilience is a systemic architectural concern).*
Our architectural guidelines dictate that synchronous calls must be protected. Downstream services calling the Customer Service must implement **Resilience4j (Circuit Breaker and Time Limiter)**. This prevents cascading failures, thread pool exhaustion, and resource locking across the cluster if the Customer Service experiences high latency, temporary unavailability, or network partitions. 

### Error Handling
The service strictly adheres to **RFC 7807 (Problem Details for HTTP APIs)** for all error responses, providing a standardized, machine-readable format for clients. 
* **404 Not Found**: Explicitly returned when an entity (like a Pet or Owner) does not exist in the database, allowing clients to cleanly distinguish missing data from other failures.
* **409 Conflict / 400 Bad Request**: Returned when a business invariant is breached (e.g., `PetStatusException` when attempting to use an `INACTIVE` pet for a new operation). This clearly distinguishes domain-specific business errors from generic 500 internal infrastructure failures.

## API Endpoints

| HTTP Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/owners/{ownerId}` | Retrieve an owner by ID, including their associated pets. |
| `POST` | `/api/owners/{ownerId}/pets` | Add and register a new pet to an existing owner. |
| `PATCH` | `/api/pets/{petId}/deactivate` | Deactivate a pet by ID, updating its state to `INACTIVE`. |
| `GET` | `/api/internal/pets/{petId}` | **Internal:** Validate a pet for inter-service use (returns owner ID and status). |

## How to Run

### 1. Start Infrastructure Dependencies
Before starting the service, ensure the required infrastructure (like RabbitMQ and optionally a Eureka Server) is running. You can typically start the message broker using Docker:
```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

### 2. Run the Application
Use the Maven Wrapper to build and start the Spring Boot application locally:
```bash
./mvnw spring-boot:run
```
The service will start and automatically connect to the local RabbitMQ instance and the H2 in-memory database.

### 3. Access API Documentation
Once running, you can explore, test the endpoints, and view the OpenAPI contract via the Swagger UI:
* **Swagger UI:** `http://localhost:8080/swagger-ui.html`
* **OpenAPI Specs:** `http://localhost:8080/v3/api-docs`
