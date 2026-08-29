package com.portfolio.inventory.messaging;

public class InventoryEventPublicationException extends RuntimeException {
    public InventoryEventPublicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
