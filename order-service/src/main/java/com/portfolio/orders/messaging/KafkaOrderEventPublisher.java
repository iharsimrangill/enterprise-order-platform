package com.portfolio.orders.messaging;

import com.portfolio.orders.application.event.OrderCreatedEvent;
import com.portfolio.orders.application.port.OrderEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class KafkaOrderEventPublisher implements OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String orderCreatedTopic;

    public KafkaOrderEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.order-created}") String orderCreatedTopic) {
        this.kafkaTemplate =
                Objects.requireNonNull(kafkaTemplate, "kafkaTemplate must not be null");

        if (orderCreatedTopic == null || orderCreatedTopic.isBlank()) {
            throw new IllegalArgumentException("orderCreatedTopic must not be blank");
        }

        this.orderCreatedTopic = orderCreatedTopic;
    }

    @Override
    public void publish(OrderCreatedEvent event) {
        Objects.requireNonNull(event, "event must not be null");

        try {
            kafkaTemplate
                    .send(orderCreatedTopic, event.orderId().toString(), event)
                    .get(10, TimeUnit.SECONDS);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to publish order-created event " + event.eventId(),
                    exception);
        }
    }
}
