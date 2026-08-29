package com.portfolio.orders.persistence.inventoryevent;

import com.portfolio.orders.application.port.ProcessedInventoryEventRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public class JpaProcessedInventoryEventRepositoryAdapter implements ProcessedInventoryEventRepository {

    private final SpringDataProcessedInventoryEventRepository repository;

    public JpaProcessedInventoryEventRepositoryAdapter(SpringDataProcessedInventoryEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByEventId(UUID eventId) {
        return repository.existsById(eventId);
    }

    @Override
    public void markProcessed(UUID eventId, UUID orderId, String eventType, Instant processedAt) {
        repository.save(new ProcessedInventoryEventEntity(eventId, orderId, eventType, processedAt));
    }
}
