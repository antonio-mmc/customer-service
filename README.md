# PetClinic Customer Service

## Description

**Bounded Context:** Customer / Ownership Domain

The Customer Service is the authoritative source of truth for **Owners**, **Pets**, and **Pet Types**. It handles the full lifecycle of these entities, exposes a synchronous internal validation API consumed by the Visit Service, and publishes domain events over RabbitMQ whenever pet state changes.

## Tech Stack

| Concern | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2.x |
| Persistence | Spring Data JPA + H2 (in-memory) |
| Messaging | Spring Cloud Stream — RabbitMQ binder |
| Observability | Micrometer + OpenTelemetry (OTLP) |
| API Docs | SpringDoc OpenAPI 2.3.0 (Swagger UI) |

## Architecture

### Communication strategy
- **Synchronous REST** — used for immediate data retrieval and cross-service pet validation (e.g. Visit Service calls `/api/internal/pets/{petId}` before creating a visit).
- **Asynchronous events (RabbitMQ)** — used for state propagation. When a pet is deactivated, a `PetDeactivatedEvent` is published so downstream consumers (e.g. Visit Service) can react without tight coupling.

### Error handling
All errors follow **RFC 7807** (`ProblemDetail`):
- `404 Not Found` — owner or pet does not exist.
- `409 Conflict` — business invariant violation (e.g. operating on an `INACTIVE` pet).
- `400 Bad Request` — validation failure on request body.

## API Endpoints

| Method | Endpoint | Description |
|:---|:---|:---|
| `GET` | `/api/owners` | List all owners (optional `?lastName=` filter). |
| `GET` | `/api/owners/{ownerId}` | Get an owner by ID, including their pets. |
| `POST` | `/api/owners` | Create a new owner. |
| `PUT` | `/api/owners/{ownerId}` | Update an existing owner. |
| `DELETE` | `/api/owners/{ownerId}` | Delete an owner by ID. |
| `POST` | `/api/owners/{ownerId}/pets` | Add a new pet to an owner. |
| `GET` | `/api/pets/{petId}` | Get a pet by ID. |
| `PATCH` | `/api/pets/{petId}/deactivate` | Deactivate a pet (sets status to `INACTIVE`). |
| `GET` | `/api/petTypes` | List all available pet types. |
| `GET` | `/api/internal/pets/{petId}` | **Internal:** Validate a pet for cross-service use. |

## How to Run

### Prerequisites
- Java 21
- Maven 3.9+
- Docker (for RabbitMQ)

### 1. Start RabbitMQ
```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

### 2. Run locally
```bash
./mvnw spring-boot:run
```
The service starts on **port 8080** and connects to the local RabbitMQ instance and an in-memory H2 database.

> In the full stack, all external traffic must go through the API Gateway on port **8000**.

### 3. API Documentation
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`

## Full-Stack Deployment

Use Docker Compose from the `API-Gateway` directory:

```bash
cd ~/ASID/API-Gateway
docker compose up -d
```

See the API Gateway README for the complete build and deploy walkthrough.
