package com.portfolio.inventory.infrastructure.memory;

import com.portfolio.inventory.application.port.ProcessedEventRepository;
import org.springframework.stereotype.Repository;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryProcessedEventRepository implements ProcessedEventRepository {
    private final Set<UUID> processedEvents = ConcurrentHashMap.newKeySet();

    @Override
    public boolean exists(UUID eventId) {
        return processedEvents.contains(eventId);
    }

    @Override
    public void markProcessed(UUID eventId) {
        processedEvents.add(eventId);
    }
}
