package com.portfolio.orders.messaging;

import com.portfolio.orders.application.HandleInventoryOutcomeService;
import com.portfolio.orders.messaging.event.InventoryOutcomeEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryOutcomeKafkaListener {

    private final HandleInventoryOutcomeService service;

    public InventoryOutcomeKafkaListener(HandleInventoryOutcomeService service) {
        this.service = service;
    }

    @KafkaListener(topics = {
            "${app.kafka.topics.inventory-reserved}",
            "${app.kafka.topics.inventory-rejected}"
    })
    public void onInventoryOutcome(InventoryOutcomeEvent event) {
        service.handle(event);
    }
}
