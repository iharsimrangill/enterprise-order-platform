package com.portfolio.orders.application;

import com.portfolio.orders.domain.OrderStatus;

import java.util.UUID;

public class InventoryOutcomeConflictException extends RuntimeException {

    public InventoryOutcomeConflictException(UUID orderId, OrderStatus currentStatus, String eventType) {
        super("Inventory outcome " + eventType + " cannot be applied to order "
                + orderId + " in status " + currentStatus);
    }
}
