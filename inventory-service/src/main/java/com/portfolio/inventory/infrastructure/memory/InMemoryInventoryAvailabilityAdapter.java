package com.portfolio.inventory.infrastructure.memory;

import com.portfolio.inventory.application.port.InventoryAvailabilityPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("memory")
public class InMemoryInventoryAvailabilityAdapter implements InventoryAvailabilityPort {
    private final Map<String, Integer> stock = new ConcurrentHashMap<>();

    @Override
    public boolean isAvailable(String sku, int quantity) {
        return stock.getOrDefault(sku, 0) >= quantity;
    }

    @Override
    public void reserve(String sku, int quantity) {
        stock.compute(sku, (key, current) -> {
            int available = current == null ? 0 : current;
            if (available < quantity) {
                throw new IllegalStateException("Insufficient stock for SKU " + sku);
            }
            return available - quantity;
        });
    }

    public void putStock(String sku, int quantity) {
        stock.put(sku, quantity);
    }
}
