package com.portfolio.orders.messaging;

import com.portfolio.orders.application.event.OrderCreatedEvent;
import com.portfolio.orders.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KafkaOrderEventPublisherTest {

    @Test
    void publishesEventUsingOrderIdAsKafkaMessageKey() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
        KafkaOrderEventPublisher publisher = new KafkaOrderEventPublisher(
                kafkaTemplate,
                "orders.created.v1");
        UUID orderId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                "order.created",
                1,
                Instant.parse("2026-08-29T16:00:00Z"),
                orderId,
                UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
                OrderStatus.PENDING,
                new BigDecimal("25.00"),
                List.of(new OrderCreatedEvent.Line(
                        "SKU-100",
                        2,
                        new BigDecimal("12.50"),
                        new BigDecimal("25.00"))));

        publisher.publish(event);

        verify(kafkaTemplate).send("orders.created.v1", orderId.toString(), event);
    }
}
