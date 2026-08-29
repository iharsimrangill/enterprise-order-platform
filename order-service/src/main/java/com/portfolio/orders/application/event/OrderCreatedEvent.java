package com.portfolio.orders.application.event;

import com.portfolio.orders.domain.Order;
import com.portfolio.orders.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Versioned integration event emitted after an order is persisted.
 */
public record OrderCreatedEvent(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        UUID orderId,
        UUID customerId,
        OrderStatus status,
        BigDecimal totalAmount,
        List<Line> lines) {

    public OrderCreatedEvent {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(totalAmount, "totalAmount must not be null");
        Objects.requireNonNull(lines, "lines must not be null");
        if (eventVersion < 1) {
            throw new IllegalArgumentException("eventVersion must be positive");
        }
        lines = List.copyOf(lines);
    }

    public static OrderCreatedEvent from(Order order) {
        Objects.requireNonNull(order, "order must not be null");
        return new OrderCreatedEvent(
                UUID.randomUUID(),
                "order.created",
                1,
                order.createdAt(),
                order.id(),
                order.customerId(),
                order.status(),
                order.totalAmount(),
                order.lines().stream()
                        .map(line -> new Line(
                                line.sku(),
                                line.quantity(),
                                line.unitPrice(),
                                line.subtotal()))
                        .toList());
    }

    public record Line(
            String sku,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal) {
    }
}
