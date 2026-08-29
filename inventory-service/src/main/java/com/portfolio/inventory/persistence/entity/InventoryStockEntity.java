package com.portfolio.inventory.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "inventory_stock")
public class InventoryStockEntity {
    @Id
    @Column(name = "sku", nullable = false, length = 120)
    private String sku;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected InventoryStockEntity() {
    }

    public InventoryStockEntity(String sku, int availableQuantity) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("sku must not be blank");
        }
        if (availableQuantity < 0) {
            throw new IllegalArgumentException("availableQuantity must not be negative");
        }
        this.sku = sku;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = 0;
        this.updatedAt = Instant.now();
    }

    public boolean canReserve(int quantity) {
        return quantity > 0 && availableQuantity >= quantity;
    }

    public void reserve(int quantity) {
        if (!canReserve(quantity)) {
            throw new IllegalStateException("Insufficient stock for SKU " + sku);
        }
        availableQuantity -= quantity;
        reservedQuantity += quantity;
        updatedAt = Instant.now();
    }

    public String getSku() {
        return sku;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }
}
