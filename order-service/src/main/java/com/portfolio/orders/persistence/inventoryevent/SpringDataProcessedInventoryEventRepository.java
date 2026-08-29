package com.portfolio.orders.persistence.inventoryevent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataProcessedInventoryEventRepository
        extends JpaRepository<ProcessedInventoryEventEntity, UUID> {
}
