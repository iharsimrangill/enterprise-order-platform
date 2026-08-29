# Order Service — Inventory Outcome Consumer

The Order Service closes the first orchestration loop in the platform by consuming the Inventory Service outcomes:

- `inventory.reserved.v1` → transition a `PENDING` order to `CONFIRMED`.
- `inventory.rejected.v1` → transition a `PENDING` order to `REJECTED` and persist the inventory rejection reason.

## Processing flow

1. Kafka delivers an inventory outcome keyed by `orderId`.
2. The Order Service checks `processed_inventory_event` by `eventId`.
3. If the event has already been processed, the handler returns without changing the order.
4. The order aggregate is loaded and the lifecycle rule is applied.
5. The updated aggregate and the processed-event marker are persisted in the same Spring transaction.
6. The Kafka record is acknowledged only after the listener returns successfully.

## Idempotency

Kafka delivery is at-least-once. The `processed_inventory_event.event_id` primary key is a durable inbox marker, so service restarts do not erase duplicate protection.

The handler also treats a semantically repeated outcome as safe when the order is already in the matching state. A contradictory outcome against a terminal order raises an explicit conflict instead of silently overwriting business state.

## Contract isolation

The Order Service owns a local `InventoryOutcomeEvent` representation. It does not import Inventory Service Java classes. Both `inventory.reserved.v1` and `inventory.rejected.v1` are deserialized into the local record and differentiated by `eventType`.

## Saga state

After this change, the core flow is:

`POST /orders` → `PENDING` → `orders.created.v1` → Inventory reservation → `inventory.reserved.v1` / `inventory.rejected.v1` → `CONFIRMED` / `REJECTED`

A later PR can add dead-letter/retry policy and then move Kafka publication to transactional outboxes for stronger delivery guarantees.
