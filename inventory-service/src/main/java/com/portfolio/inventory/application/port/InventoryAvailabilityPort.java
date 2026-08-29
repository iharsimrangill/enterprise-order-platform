package com.portfolio.inventory.application.port;

public interface InventoryAvailabilityPort {
    boolean isAvailable(String sku, int quantity);
    void reserve(String sku, int quantity);
}
