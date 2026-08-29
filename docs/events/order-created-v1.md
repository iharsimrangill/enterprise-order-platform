# `order.created` event — v1

The order service publishes this integration event after a newly placed order has been persisted.

## Topic

`orders.created.v1`

The topic name can be overridden with `ORDER_CREATED_TOPIC`.

## Kafka key

The order UUID is used as the Kafka message key so events for the same order are routed consistently by Kafka partitioning.

## Example payload

```json
{
  "eventId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
  "eventType": "order.created",
  "eventVersion": 1,
  "occurredAt": "2026-08-29T16:00:00Z",
  "orderId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "customerId": "cccccccc-cccc-cccc-cccc-cccccccccccc",
  "status": "PENDING",
  "totalAmount": 25.00,
  "lines": [
    {
      "sku": "SKU-100",
      "quantity": 2,
      "unitPrice": 12.50,
      "lineTotal": 25.00
    }
  ]
}
```

## Delivery semantics

This PR intentionally introduces direct Kafka publication first so the event contract and producer boundary are explicit. A later PR will replace direct publication with a transactional outbox to remove the database/Kafka dual-write failure window.
