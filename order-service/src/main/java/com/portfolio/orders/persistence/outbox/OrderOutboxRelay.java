package com.portfolio.orders.persistence.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.orders.application.event.OrderCreatedEvent;
import com.portfolio.orders.application.port.OrderEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Component
public class OrderOutboxRelay {

    private final SpringDataOrderOutboxRepository repository;
    private final OrderEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Autowired
    public OrderOutboxRelay(
            SpringDataOrderOutboxRepository repository,
            OrderEventPublisher eventPublisher,
            ObjectMapper objectMapper) {
        this(repository, eventPublisher, objectMapper, Clock.systemUTC());
    }

    OrderOutboxRelay(
            SpringDataOrderOutboxRepository repository,
            OrderEventPublisher eventPublisher,
            ObjectMapper objectMapper,
            Clock clock) {
        this.repository = Objects.requireNonNull(repository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.clock = Objects.requireNonNull(clock);
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:1000}")
    @Transactional
    public void publishPendingEvents() {
        List<OrderOutboxEntity> events =
                repository.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc();

        for (OrderOutboxEntity entity : events) {
            publish(entity);
        }
    }

    private void publish(OrderOutboxEntity entity) {
        entity.recordAttempt();

        try {
            OrderCreatedEvent event =
                    objectMapper.readValue(
                            entity.getPayload(),
                            OrderCreatedEvent.class);

            eventPublisher.publish(event);
            entity.markPublished(Instant.now(clock));

        } catch (Exception exception) {
            // Intentionally leave publishedAt null.
            // The next scheduled poll retries this event.
        }

        repository.save(entity);
    }
}
