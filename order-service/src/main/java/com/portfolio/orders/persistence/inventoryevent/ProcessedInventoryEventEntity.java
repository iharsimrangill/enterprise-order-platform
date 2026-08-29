package com.portfolio.orders.persistence.inventoryevent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "processed_inventory_event")
public class ProcessedInventoryEventEntity {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Column(name = "event_type", nullable = false, updatable = false, length = 64)
    private String eventType;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private Instant processedAt;

    protected ProcessedInventoryEventEntity() {
    }

    public ProcessedInventoryEventEntity(UUID eventId, UUID orderId, String eventType, Instant processedAt) {
        this.eventId = Objects.requireNonNull(eventId, "eventId must not be null");
        this.orderId = Objects.requireNonNull(orderId, "orderId must not be null");
        this.eventType = Objects.requireNonNull(eventType, "eventType must not be null");
        this.processedAt = Objects.requireNonNull(processedAt, "processedAt must not be null");
    }

    public UUID eventId() {
        return eventId;
    }
}
