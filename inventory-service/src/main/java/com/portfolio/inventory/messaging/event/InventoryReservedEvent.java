package com.portfolio.inventory.messaging.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InventoryReservedEvent(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        UUID orderId,
        UUID sourceOrderEventId,
        List<Line> lines) {

    public InventoryReservedEvent {
        lines = List.copyOf(lines);
    }

    public record Line(String sku, int quantity) {
    }
}
