package com.portfolio.inventory.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ReservationLineEmbeddable {
    @Column(name = "sku", nullable = false, length = 120)
    private String sku;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    protected ReservationLineEmbeddable() {
    }

    public ReservationLineEmbeddable(String sku, int quantity) {
        this.sku = sku;
        this.quantity = quantity;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }
}
