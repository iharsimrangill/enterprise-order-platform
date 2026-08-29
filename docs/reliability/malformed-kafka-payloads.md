# Malformed Kafka payload handling

## Problem

JSON deserialization happens before a normal `@KafkaListener` method receives a record. Without an error-aware deserializer, a malformed record can repeatedly fail at the consumer boundary and prevent the normal listener error path from handling it cleanly.

## Strategy

Both Order Service and Inventory Service wrap their Jackson JSON delegate with Spring Kafka's `ErrorHandlingDeserializer`.

When the delegate cannot deserialize a value:

1. the listener is not invoked;
2. Spring Kafka records the deserialization failure in Kafka headers;
3. the existing `DefaultErrorHandler` handles the failed record;
4. after the configured retry policy is exhausted, `DeadLetterPublishingRecoverer` publishes the record to `<source-topic>.dlt`.

## Raw payload preservation

A deserialization failure exposes the original value as `byte[]`. The platform therefore uses a `DelegatingByTypeSerializer` for Kafka producers:

- `byte[]` -> `ByteArraySerializer`
- all normal integration-event objects -> `JacksonJsonSerializer`

This preserves the exact malformed bytes in the DLT rather than converting them to JSON/Base64. Operators can inspect or replay the original payload later.

## Topics

- `orders.created.v1.dlt`
- `inventory.reserved.v1.dlt`
- `inventory.rejected.v1.dlt`

## Operational principle

Malformed payloads are isolated as data-quality failures. They do not enter business logic and they do not block a Kafka partition indefinitely.
