package com.portfolio.inventory.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record InventoryReservation(
        UUID eventId,
        UUID orderId,
        ReservationStatus status,
        String reason,
        Instant createdAt,
        List<Line> lines) {

    public InventoryReservation {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(lines, "lines must not be null");
        lines = List.copyOf(lines);
    }

    public record Line(String sku, int quantity) {
        public Line {
            if (sku == null || sku.isBlank()) {
                throw new IllegalArgumentException("sku must not be blank");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be positive");
            }
        }
    }
}
