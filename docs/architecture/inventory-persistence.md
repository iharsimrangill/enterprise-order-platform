# Inventory persistence

The Inventory Service owns a dedicated PostgreSQL database in local development (`inventory-postgres`, port `5433`). The Order Service continues to use the existing database on port `5432`, preserving service-level data ownership.

## Tables

- `inventory_stock` stores available and reserved quantities per SKU.
- `inventory_reservation` stores one reservation outcome per order-created event.
- `inventory_reservation_line` stores the SKU/quantity snapshot associated with the reservation.
- `processed_event` provides durable idempotency for Kafka event processing.

## Transaction boundary

`ReserveInventoryService.handle(...)` is transactional. Availability checks acquire pessimistic write locks on stock rows, so the check and decrement happen inside one database transaction. Reservation persistence, stock mutation, and processed-event recording commit or roll back together.

Database uniqueness constraints on `event_id` and `order_id` provide a second line of defense against duplicate delivery in addition to the application-level processed-event check.

## Local development

Start infrastructure with `docker compose up -d inventory-postgres kafka`. The Inventory Service defaults to:

- JDBC: `jdbc:postgresql://localhost:5433/inventory_platform`
- user: `platform`
- password: `platform`

All values can be overridden through `INVENTORY_DB_URL`, `INVENTORY_DB_USER`, and `INVENTORY_DB_PASSWORD`.
