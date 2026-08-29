package com.portfolio.inventory.persistence.repository;

import com.portfolio.inventory.persistence.entity.InventoryReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataInventoryReservationRepository
        extends JpaRepository<InventoryReservationEntity, UUID> {
    Optional<InventoryReservationEntity> findByOrderId(UUID orderId);
}
