# Kafka retry and dead-letter policy

Both record-based Kafka consumers use a shared recovery policy implemented with Spring Kafka's `DefaultErrorHandler` and `DeadLetterPublishingRecoverer`.

## Delivery policy

- The listener receives the record once normally.
- A failed listener invocation is retried **2 additional times** by default.
- Retries wait **1 second** between attempts by default.
- After retries are exhausted, the record is published to a dead-letter topic.
- The DLT keeps the original partition so per-partition failure investigation remains straightforward.

Defaults can be overridden with:

- `KAFKA_RETRY_BACKOFF_MS`
- `KAFKA_MAX_RETRIES`
- `KAFKA_DLT_SUFFIX`

With the default `.dlt` suffix, examples are:

| Consumer | Source topic | Dead-letter topic |
| --- | --- | --- |
| Inventory Service | `orders.created.v1` | `orders.created.v1.dlt` |
| Order Service | `inventory.reserved.v1` | `inventory.reserved.v1.dlt` |
| Order Service | `inventory.rejected.v1` | `inventory.rejected.v1.dlt` |

## Why bounded retries

Unlimited retries can pin a consumer partition behind one poison record. This policy gives transient failures a small retry window, then isolates persistent failures so healthy records can continue to flow.

## Operational recovery

DLT records preserve Spring Kafka dead-letter headers describing the original topic/partition/offset and failure. Operators can inspect the failure, correct the underlying problem, and replay the record intentionally rather than relying on infinite automatic retries.

## Scope

This policy covers exceptions raised during listener processing. Handling malformed payloads that fail during deserialization is a separate concern and should use Spring Kafka's `ErrorHandlingDeserializer` so those records can also enter the dead-letter flow.
