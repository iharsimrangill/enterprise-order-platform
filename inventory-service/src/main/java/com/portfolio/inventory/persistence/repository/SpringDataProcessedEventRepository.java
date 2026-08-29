package com.portfolio.inventory.persistence.repository;

import com.portfolio.inventory.persistence.entity.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataProcessedEventRepository extends JpaRepository<ProcessedEventEntity, UUID> {
}
