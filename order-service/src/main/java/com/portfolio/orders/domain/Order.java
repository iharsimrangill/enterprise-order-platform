package com.portfolio.orders.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Order aggregate root.
 *
 * <p>The aggregate owns its lifecycle rules and protects invariants such as a
 * non-empty line-item collection and legal state transitions.</p>
 */
public final class Order {

    private final UUID id;
    private final UUID customerId;
    private final List<OrderLine> lines;
    private final Instant createdAt;
    private OrderStatus status;
    private Instant updatedAt;
    private String rejectionReason;

    private Order(
            UUID id,
            UUID customerId,
            List<OrderLine> lines,
            OrderStatus status,
            Instant createdAt,
            Instant updatedAt,
            String rejectionReason) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.customerId = Objects.requireNonNull(customerId, "customerId must not be null");
        this.lines = validateAndCopyLines(lines);
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        this.rejectionReason = rejectionReason;
    }

    public static Order place(UUID id, UUID customerId, List<OrderLine> lines, Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        return new Order(id, customerId, lines, OrderStatus.PENDING, now, now, null);
    }

    public void confirm(Instant now) {
        transitionTo(OrderStatus.CONFIRMED, now);
    }

    public void reject(String reason, Instant now) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("rejection reason must not be blank");
        }
        transitionTo(OrderStatus.REJECTED, now);
        this.rejectionReason = reason.trim();
    }

    public void cancel(Instant now) {
        transitionTo(OrderStatus.CANCELLED, now);
    }

    public void fulfill(Instant now) {
        transitionTo(OrderStatus.FULFILLED, now);
    }

    private void transitionTo(OrderStatus target, Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        if (now.isBefore(updatedAt)) {
            throw new IllegalArgumentException("transition time cannot be before the last update");
        }
        if (!status.canTransitionTo(target)) {
            throw new InvalidOrderStateTransitionException(status, target);
        }
        status = target;
        updatedAt = now;
    }

    private static List<OrderLine> validateAndCopyLines(List<OrderLine> lines) {
        Objects.requireNonNull(lines, "lines must not be null");
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("order must contain at least one line");
        }
        if (lines.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("order lines must not contain null values");
        }
        return List.copyOf(lines);
    }

    public BigDecimal totalAmount() {
        return lines.stream()
                .map(OrderLine::subtotal)
                .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), BigDecimal::add);
    }

    public UUID id() {
        return id;
    }

    public UUID customerId() {
        return customerId;
    }

    public List<OrderLine> lines() {
        return lines;
    }

    public OrderStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public String rejectionReason() {
        return rejectionReason;
    }
}
