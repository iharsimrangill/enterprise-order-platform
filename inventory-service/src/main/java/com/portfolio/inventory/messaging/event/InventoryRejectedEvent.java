package com.portfolio.inventory.messaging.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InventoryRejectedEvent(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        UUID orderId,
        UUID sourceOrderEventId,
        String reason,
        List<Line> lines) {

    public InventoryRejectedEvent {
        lines = List.copyOf(lines);
    }

    public record Line(String sku, int quantity) {
    }
}
