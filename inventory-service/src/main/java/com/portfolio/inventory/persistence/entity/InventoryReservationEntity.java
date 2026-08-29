package com.portfolio.inventory.persistence.entity;

import com.portfolio.inventory.domain.ReservationStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "inventory_reservation")
public class InventoryReservationEntity {
    @Id
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ReservationStatus status;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "inventory_reservation_line",
            joinColumns = @JoinColumn(name = "event_id"))
    @OrderColumn(name = "line_no")
    private List<ReservationLineEmbeddable> lines = new ArrayList<>();

    protected InventoryReservationEntity() {
    }

    public InventoryReservationEntity(
            UUID eventId,
            UUID orderId,
            ReservationStatus status,
            String reason,
            Instant createdAt,
            List<ReservationLineEmbeddable> lines) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
        this.lines = new ArrayList<>(lines);
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<ReservationLineEmbeddable> getLines() {
        return List.copyOf(lines);
    }
}
