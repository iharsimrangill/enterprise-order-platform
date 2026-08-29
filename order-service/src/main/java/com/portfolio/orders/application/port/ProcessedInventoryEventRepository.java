package com.portfolio.orders.application.port;

import java.time.Instant;
import java.util.UUID;

/**
 * Durable idempotency boundary for inventory outcome events consumed by the
 * Order Service.
 */
public interface ProcessedInventoryEventRepository {

    boolean existsByEventId(UUID eventId);

    void markProcessed(UUID eventId, UUID orderId, String eventType, Instant processedAt);
}
