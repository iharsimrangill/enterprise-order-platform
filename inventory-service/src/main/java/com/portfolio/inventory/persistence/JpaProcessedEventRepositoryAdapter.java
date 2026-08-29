package com.portfolio.inventory.persistence;

import com.portfolio.inventory.application.port.ProcessedEventRepository;
import com.portfolio.inventory.persistence.entity.ProcessedEventEntity;
import com.portfolio.inventory.persistence.repository.SpringDataProcessedEventRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
@Profile("!memory")
public class JpaProcessedEventRepositoryAdapter implements ProcessedEventRepository {
    private final SpringDataProcessedEventRepository repository;

    public JpaProcessedEventRepositoryAdapter(SpringDataProcessedEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean exists(UUID eventId) {
        return repository.existsById(eventId);
    }

    @Override
    public void markProcessed(UUID eventId) {
        repository.save(new ProcessedEventEntity(eventId, Instant.now()));
    }
}
