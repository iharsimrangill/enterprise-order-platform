package com.portfolio.orders.persistence.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.orders.application.event.OrderCreatedEvent;
import com.portfolio.orders.application.port.OrderOutboxWriter;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class JpaOrderOutboxWriter implements OrderOutboxWriter {

    private final SpringDataOrderOutboxRepository repository;
    private final ObjectMapper objectMapper;

    public JpaOrderOutboxWriter(
            SpringDataOrderOutboxRepository repository,
            ObjectMapper objectMapper) {
        this.repository = Objects.requireNonNull(repository);
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    @Override
    public void save(OrderCreatedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            repository.save(new OrderOutboxEntity(
                    event.eventId(),
                    event.orderId(),
                    event.eventType(),
                    payload,
                    event.occurredAt()));

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Unable to serialize order event for outbox",
                    exception);
        }
    }
}
