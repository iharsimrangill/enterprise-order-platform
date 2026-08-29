package com.portfolio.inventory.messaging;

import com.portfolio.inventory.application.ReservationProcessingResult;
import com.portfolio.inventory.application.ReserveInventoryService;
import com.portfolio.inventory.application.port.InventoryEventPublisher;
import com.portfolio.inventory.messaging.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedKafkaListener {
    private static final Logger log = LoggerFactory.getLogger(OrderCreatedKafkaListener.class);

    private final ReserveInventoryService reserveInventoryService;
    private final InventoryEventPublisher inventoryEventPublisher;

    public OrderCreatedKafkaListener(
            ReserveInventoryService reserveInventoryService,
            InventoryEventPublisher inventoryEventPublisher) {
        this.reserveInventoryService = reserveInventoryService;
        this.inventoryEventPublisher = inventoryEventPublisher;
    }

    @KafkaListener(topics = "${app.kafka.topics.order-created}")
    public void consume(OrderCreatedEvent event) {
        var result = reserveInventoryService.handle(event);

        // Publish after the reservation transaction has completed. On a retried input event,
        // the persisted reservation is reloaded and the same deterministic outcome event is emitted.
        inventoryEventPublisher.publish(result.reservation());

        log.atInfo()
                .addKeyValue("eventId", event.eventId())
                .addKeyValue("orderId", event.orderId())
                .addKeyValue("result", result.status())
                .addKeyValue("outcome", result.reservation().status())
                .addKeyValue("republished", result.status() == ReservationProcessingResult.Status.DUPLICATE)
                .log("Processed order.created event and published inventory outcome");
    }
}
