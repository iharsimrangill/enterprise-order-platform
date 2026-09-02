package com.portfolio.orders.persistence.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.orders.application.event.OrderCreatedEvent;
import com.portfolio.orders.application.port.OrderEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private final int maxAttempts;

    @Autowired
    public OrderOutboxRelay(
            SpringDataOrderOutboxRepository repository,
            OrderEventPublisher eventPublisher,
            ObjectMapper objectMapper,
            @Value("${app.outbox.max-attempts:8}") int maxAttempts) {
        this(
                repository,
                eventPublisher,
                objectMapper,
                Clock.systemUTC(),
                maxAttempts);
    }

    OrderOutboxRelay(
            SpringDataOrderOutboxRepository repository,
            OrderEventPublisher eventPublisher,
            ObjectMapper objectMapper,
            Clock clock,
            int maxAttempts) {
        this.repository = Objects.requireNonNull(repository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.clock = Objects.requireNonNull(clock);

        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }

        this.maxAttempts = maxAttempts;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:1000}")
    @Transactional
    public void publishPendingEvents() {
        Instant now = Instant.now(clock);

        List<OrderOutboxEntity> events =
                repository.findEligibleForRetry(now, maxAttempts);

        for (OrderOutboxEntity entity : events) {
            publish(entity);
        }
    }

    private void publish(OrderOutboxEntity entity) {
        Instant now = Instant.now(clock);

        entity.recordAttempt();

        try {
            OrderCreatedEvent event =
                    objectMapper.readValue(
                            entity.getPayload(),
                            OrderCreatedEvent.class);

            eventPublisher.publish(event);
            entity.markPublished(now);

        } catch (Exception exception) {
            entity.scheduleRetry(now, exception);
        }

        repository.save(entity);
    }
}
