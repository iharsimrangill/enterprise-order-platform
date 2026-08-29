package com.portfolio.orders.messaging.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Local representation of inventory outcome integration events.
 *
 * <p>Both inventory.reserved.v1 and inventory.rejected.v1 deserialize into this
 * contract. The rejected event supplies {@code reason}; the reserved event does
 * not.</p>
 */
public record InventoryOutcomeEvent(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        UUID orderId,
        UUID sourceOrderEventId,
        String reason,
        List<Line> lines) {

    public InventoryOutcomeEvent {
        lines = lines == null ? List.of() : List.copyOf(lines);
    }

    public record Line(String sku, int quantity) {
    }
}
