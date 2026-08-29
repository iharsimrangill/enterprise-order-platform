package com.portfolio.orders.application;

import com.portfolio.orders.application.port.OrderRepository;
import com.portfolio.orders.application.port.ProcessedInventoryEventRepository;
import com.portfolio.orders.domain.Order;
import com.portfolio.orders.domain.OrderStatus;
import com.portfolio.orders.messaging.event.InventoryOutcomeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@Service
public class HandleInventoryOutcomeService {

    static final String INVENTORY_RESERVED = "inventory.reserved";
    static final String INVENTORY_REJECTED = "inventory.rejected";
    private static final int SUPPORTED_EVENT_VERSION = 1;

    private final OrderRepository orderRepository;
    private final ProcessedInventoryEventRepository processedEventRepository;
    private final Clock clock;

    @Autowired
    public HandleInventoryOutcomeService(
            OrderRepository orderRepository,
            ProcessedInventoryEventRepository processedEventRepository) {
        this(orderRepository, processedEventRepository, Clock.systemUTC());
    }

    HandleInventoryOutcomeService(
            OrderRepository orderRepository,
            ProcessedInventoryEventRepository processedEventRepository,
            Clock clock) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository must not be null");
        this.processedEventRepository = Objects.requireNonNull(
                processedEventRepository,
                "processedEventRepository must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Transactional
    public InventoryOutcomeHandlingResult handle(InventoryOutcomeEvent event) {
        validate(event);

        if (processedEventRepository.existsByEventId(event.eventId())) {
            return InventoryOutcomeHandlingResult.DUPLICATE;
        }

        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new OrderNotFoundException(event.orderId()));

        applyOutcome(order, event);
        orderRepository.save(order);
        processedEventRepository.markProcessed(
                event.eventId(),
                event.orderId(),
                event.eventType(),
                Instant.now(clock));

        return InventoryOutcomeHandlingResult.APPLIED;
    }

    private static void applyOutcome(Order order, InventoryOutcomeEvent event) {
        switch (event.eventType()) {
            case INVENTORY_RESERVED -> applyReserved(order, event);
            case INVENTORY_REJECTED -> applyRejected(order, event);
            default -> throw new IllegalArgumentException("Unsupported inventory event type: " + event.eventType());
        }
    }

    private static void applyReserved(Order order, InventoryOutcomeEvent event) {
        if (order.status() == OrderStatus.CONFIRMED) {
            return;
        }
        if (order.status() != OrderStatus.PENDING) {
            throw new InventoryOutcomeConflictException(order.id(), order.status(), event.eventType());
        }
        order.confirm(event.occurredAt());
    }

    private static void applyRejected(Order order, InventoryOutcomeEvent event) {
        if (order.status() == OrderStatus.REJECTED) {
            return;
        }
        if (order.status() != OrderStatus.PENDING) {
            throw new InventoryOutcomeConflictException(order.id(), order.status(), event.eventType());
        }
        if (event.reason() == null || event.reason().isBlank()) {
            throw new IllegalArgumentException("inventory.rejected event must include a reason");
        }
        order.reject(event.reason(), event.occurredAt());
    }

    private static void validate(InventoryOutcomeEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        Objects.requireNonNull(event.eventId(), "eventId must not be null");
        Objects.requireNonNull(event.orderId(), "orderId must not be null");
        Objects.requireNonNull(event.occurredAt(), "occurredAt must not be null");
        if (event.eventType() == null || event.eventType().isBlank()) {
            throw new IllegalArgumentException("eventType must not be blank");
        }
        if (event.eventVersion() != SUPPORTED_EVENT_VERSION) {
            throw new IllegalArgumentException("Unsupported inventory event version: " + event.eventVersion());
        }
    }
}
