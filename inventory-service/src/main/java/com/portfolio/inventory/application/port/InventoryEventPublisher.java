package com.portfolio.inventory.application.port;

import com.portfolio.inventory.domain.InventoryReservation;

public interface InventoryEventPublisher {
    void publish(InventoryReservation reservation);
}
