package com.portfolio.orders.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class OrderLineId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    protected OrderLineId() {
    }

    public OrderLineId(UUID orderId, int lineNumber) {
        this.orderId = Objects.requireNonNull(orderId, "orderId must not be null");
        if (lineNumber <= 0) {
            throw new IllegalArgumentException("lineNumber must be greater than zero");
        }
        this.lineNumber = lineNumber;
    }

    public UUID orderId() {
        return orderId;
    }

    public int lineNumber() {
        return lineNumber;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof OrderLineId that)) {
            return false;
        }
        return Objects.equals(orderId, that.orderId) && Objects.equals(lineNumber, that.lineNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, lineNumber);
    }
}
