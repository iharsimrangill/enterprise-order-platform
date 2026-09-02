package com.portfolio.orders.persistence.outbox;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderOutboxEntityRetryTest {

    @Test
    void schedulesExponentialRetryAfterFailure() {
        Instant now = Instant.parse("2026-09-02T12:00:00Z");

        OrderOutboxEntity entity = new OrderOutboxEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "OrderCreated",
                "{}",
                now);

        entity.recordAttempt();
        entity.scheduleRetry(now, new IllegalStateException("kafka unavailable"));

        assertEquals(1, entity.getAttempts());
        assertEquals(now.plusSeconds(5), entity.getNextAttemptAt());
        assertEquals("kafka unavailable", entity.getLastError());

        assertFalse(entity.isRetryEligible(now.plusSeconds(4), 8));
        assertTrue(entity.isRetryEligible(now.plusSeconds(5), 8));
    }

    @Test
    void doublesRetryDelayAcrossAttempts() {
        Instant now = Instant.parse("2026-09-02T12:00:00Z");

        OrderOutboxEntity entity = new OrderOutboxEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "OrderCreated",
                "{}",
                now);

        entity.recordAttempt();
        entity.scheduleRetry(now, new RuntimeException("first"));

        entity.recordAttempt();
        entity.scheduleRetry(now, new RuntimeException("second"));

        assertEquals(2, entity.getAttempts());
        assertEquals(now.plusSeconds(10), entity.getNextAttemptAt());
    }

    @Test
    void stopsRetryingAfterMaximumAttempts() {
        Instant now = Instant.parse("2026-09-02T12:00:00Z");

        OrderOutboxEntity entity = new OrderOutboxEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "OrderCreated",
                "{}",
                now);

        for (int i = 0; i < 8; i++) {
            entity.recordAttempt();
        }

        assertFalse(entity.isRetryEligible(now, 8));
    }

    @Test
    void publishingClearsRetryMetadata() {
        Instant now = Instant.parse("2026-09-02T12:00:00Z");

        OrderOutboxEntity entity = new OrderOutboxEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "OrderCreated",
                "{}",
                now);

        entity.recordAttempt();
        entity.scheduleRetry(now, new RuntimeException("temporary failure"));
        entity.markPublished(now.plusSeconds(20));

        assertEquals(now.plusSeconds(20), entity.getPublishedAt());
        assertNull(entity.getNextAttemptAt());
        assertNull(entity.getLastError());
        assertFalse(entity.isRetryEligible(now.plusSeconds(30), 8));
    }

    @Test
    void capsRetryDelayAtFiveMinutes() {
        Instant now = Instant.parse("2026-09-02T12:00:00Z");

        OrderOutboxEntity entity = new OrderOutboxEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "OrderCreated",
                "{}",
                now);

        for (int i = 0; i < 10; i++) {
            entity.recordAttempt();
        }

        entity.scheduleRetry(now, new RuntimeException("still unavailable"));

        assertEquals(now.plusSeconds(300), entity.getNextAttemptAt());
    }
}
