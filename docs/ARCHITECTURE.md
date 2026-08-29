# Architecture

## Goals

The platform demonstrates production-oriented patterns used in enterprise systems:

- independently deployable services
- event-driven communication
- transactional consistency boundaries
- idempotent consumers
- observability and operational health
- security and role-based authorization
- automated testing and CI/CD
- containerized local development and Kubernetes deployment

## Initial service boundaries

### Order Service
Owns order lifecycle, line items, totals, state transitions, and order-facing APIs.

### Inventory Service
Owns stock availability, reservations, releases, and inventory adjustments.

### Notification Service
Consumes domain events and delivers customer-facing notifications without coupling notification logic to transactional services.

## Planned event flow

```text
Client
  |
  v
Order Service ---- OrderCreated ----> Kafka ----> Inventory Service
     ^                                      |             |
     |                                      |             +-- reserve stock
     |                                      v
     +---- InventoryReserved <--------- Kafka
                                             |
                                             +--------> Notification Service
```

## Consistency model

The project will evolve toward a saga-style workflow. Local database transactions remain inside each service boundary. Cross-service workflows use durable domain events, idempotency, retries, and compensating actions rather than distributed database transactions.

## ADRs

Major architecture decisions should be captured in `docs/adr/` as the project evolves.
