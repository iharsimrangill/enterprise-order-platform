package com.portfolio.inventory.application.port;

import com.portfolio.inventory.domain.InventoryReservation;
import java.util.Optional;
import java.util.UUID;

public interface InventoryReservationRepository {
    InventoryReservation save(InventoryReservation reservation);
    Optional<InventoryReservation> findByOrderId(UUID orderId);
}
