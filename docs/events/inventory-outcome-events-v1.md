# Inventory outcome events v1

The Inventory Service emits one outcome event after processing an `orders.created.v1` message.

## Topics

- `inventory.reserved.v1` — emitted when every requested SKU can be reserved.
- `inventory.rejected.v1` — emitted when at least one requested SKU lacks sufficient stock.

Both topics use the order UUID as the Kafka message key so events for the same order are consistently partitioned.

## Delivery model

The reservation transaction commits before the outcome is published. Publication is awaited by the Kafka listener.
If publication fails, the listener throws and the input record can be retried.

The Inventory Service stores the incoming order event ID as a processed marker. On retry it reloads the previously persisted reservation rather than mutating stock again, then republishes the outcome.

Outcome event IDs are deterministic from the source order event ID and reservation status. Republishing therefore emits the same event ID, allowing downstream consumers to deduplicate safely.

This is an at-least-once design. A later transactional-outbox change can remove the remaining synchronous Kafka publication from the listener path.

## `inventory.reserved.v1`

```json
{
  "eventId": "...",
  "eventType": "inventory.reserved",
  "eventVersion": 1,
  "occurredAt": "2026-08-29T16:00:00Z",
  "orderId": "...",
  "sourceOrderEventId": "...",
  "lines": [
    { "sku": "SKU-1", "quantity": 2 }
  ]
}
```

## `inventory.rejected.v1`

```json
{
  "eventId": "...",
  "eventType": "inventory.rejected",
  "eventVersion": 1,
  "occurredAt": "2026-08-29T16:00:00Z",
  "orderId": "...",
  "sourceOrderEventId": "...",
  "reason": "Insufficient stock for SKU SKU-1",
  "lines": [
    { "sku": "SKU-1", "quantity": 2 }
  ]
}
```
