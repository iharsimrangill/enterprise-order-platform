package com.portfolio.inventory.messaging;

import com.portfolio.inventory.application.ReserveInventoryService;
import com.portfolio.inventory.messaging.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedKafkaListener {
    private static final Logger log = LoggerFactory.getLogger(OrderCreatedKafkaListener.class);
    private final ReserveInventoryService reserveInventoryService;

    public OrderCreatedKafkaListener(ReserveInventoryService reserveInventoryService) {
        this.reserveInventoryService = reserveInventoryService;
    }

    @KafkaListener(topics = "${app.kafka.topics.order-created}")
    public void consume(OrderCreatedEvent event) {
        var result = reserveInventoryService.handle(event);
        log.atInfo()
                .addKeyValue("eventId", event.eventId())
                .addKeyValue("orderId", event.orderId())
                .addKeyValue("result", result.status())
                .log("Processed order.created event");
    }
}
