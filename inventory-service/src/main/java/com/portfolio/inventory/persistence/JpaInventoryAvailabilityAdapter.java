package com.portfolio.inventory.persistence;

import com.portfolio.inventory.application.port.InventoryAvailabilityPort;
import com.portfolio.inventory.persistence.repository.SpringDataInventoryStockRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!memory")
public class JpaInventoryAvailabilityAdapter implements InventoryAvailabilityPort {
    private final SpringDataInventoryStockRepository stockRepository;

    public JpaInventoryAvailabilityAdapter(SpringDataInventoryStockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public boolean isAvailable(String sku, int quantity) {
        return stockRepository.findBySkuForUpdate(sku)
                .map(stock -> stock.canReserve(quantity))
                .orElse(false);
    }

    @Override
    public void reserve(String sku, int quantity) {
        var stock = stockRepository.findBySkuForUpdate(sku)
                .orElseThrow(() -> new IllegalStateException("Unknown SKU " + sku));
        stock.reserve(quantity);
        stockRepository.save(stock);
    }
}
