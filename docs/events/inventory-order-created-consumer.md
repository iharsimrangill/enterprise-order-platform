# Inventory consumer: `orders.created.v1`

The inventory service consumes the order service's `order.created` integration event from `orders.created.v1`.

## Consumer identity

- Consumer group: `inventory-service-v1` by default.
- Message key: order UUID (set by the producer).
- Offset commits: record-level, with Kafka auto-commit disabled.

## Processing flow

1. Deserialize the versioned event into an inventory-owned contract type.
2. Check the event id against the processed-event port.
3. Validate availability for every requested SKU before mutating stock.
4. Reserve all lines when available, otherwise create a rejected reservation.
5. Save the reservation result.
6. Mark the event id as processed.

## Idempotency

Duplicate event ids are ignored before stock is changed. This protects inventory from Kafka redelivery at the application boundary.

The current adapters are intentionally in-memory to keep this PR focused on the consumer/use-case boundary. A follow-up persistence PR will move stock, reservations, and processed-event ids into PostgreSQL with transactional locking and unique constraints.

## Failure semantics

Exceptions are allowed to escape the listener so the Kafka container does not treat a failed handler invocation as successful. Retry/dead-letter policy is intentionally deferred to a dedicated resilience PR.
