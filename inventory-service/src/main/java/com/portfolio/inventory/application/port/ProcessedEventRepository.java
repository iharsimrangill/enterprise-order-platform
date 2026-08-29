package com.portfolio.inventory.application.port;

import java.util.UUID;

public interface ProcessedEventRepository {
    boolean exists(UUID eventId);
    void markProcessed(UUID eventId);
}
