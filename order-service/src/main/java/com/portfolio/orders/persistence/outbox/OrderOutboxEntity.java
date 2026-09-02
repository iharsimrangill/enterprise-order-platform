package com.portfolio.orders.persistence.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_outbox")
public class OrderOutboxEntity {

    private static final Duration BASE_RETRY_DELAY = Duration.ofSeconds(5);
    private static final Duration MAX_RETRY_DELAY = Duration.ofMinutes(5);

    @Id
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    protected OrderOutboxEntity() {
    }

    public OrderOutboxEntity(
            UUID eventId,
            UUID aggregateId,
            String eventType,
            String payload,
            Instant createdAt) {
        this.eventId = eventId;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = createdAt;
        this.attempts = 0;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public String getLastError() {
        return lastError;
    }

    public void markPublished(Instant publishedAt) {
        this.publishedAt = publishedAt;
        this.nextAttemptAt = null;
        this.lastError = null;
    }

    public void recordAttempt() {
        this.attempts++;
    }

    public void scheduleRetry(Instant now, Exception exception) {
        long multiplier = 1L << Math.min(Math.max(attempts - 1, 0), 10);

        Duration delay = BASE_RETRY_DELAY.multipliedBy(multiplier);
        if (delay.compareTo(MAX_RETRY_DELAY) > 0) {
            delay = MAX_RETRY_DELAY;
        }

        this.nextAttemptAt = now.plus(delay);
        this.lastError = truncateError(exception);
    }

    public boolean isRetryEligible(Instant now, int maxAttempts) {
        if (publishedAt != null || attempts >= maxAttempts) {
            return false;
        }

        return nextAttemptAt == null || !nextAttemptAt.isAfter(now);
    }

    private String truncateError(Exception exception) {
        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            message = exception.getClass().getSimpleName();
        }

        return message.length() <= 1000
                ? message
                : message.substring(0, 1000);
    }
}
