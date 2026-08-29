package com.portfolio.inventory.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Inventory-owned representation of the order.created.v1 integration contract.
 * Keeping this type local avoids compile-time coupling to the order-service module.
 */
public record OrderCreatedEvent(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        UUID orderId,
        UUID customerId,
        String status,
        BigDecimal totalAmount,
        List<Line> lines) {

    public record Line(
            String sku,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal) {
    }
}
