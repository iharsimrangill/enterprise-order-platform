package com.portfolio.inventory.messaging;

import com.portfolio.inventory.application.port.InventoryEventPublisher;
import com.portfolio.inventory.domain.InventoryReservation;
import com.portfolio.inventory.domain.ReservationStatus;
import com.portfolio.inventory.messaging.event.InventoryRejectedEvent;
import com.portfolio.inventory.messaging.event.InventoryReservedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class KafkaInventoryEventPublisher implements InventoryEventPublisher {
    private static final int EVENT_VERSION = 1;
    private static final long SEND_TIMEOUT_SECONDS = 10;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String inventoryReservedTopic;
    private final String inventoryRejectedTopic;

    public KafkaInventoryEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.inventory-reserved}") String inventoryReservedTopic,
            @Value("${app.kafka.topics.inventory-rejected}") String inventoryRejectedTopic) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate, "kafkaTemplate must not be null");
        this.inventoryReservedTopic = requireTopic(inventoryReservedTopic, "inventoryReservedTopic");
        this.inventoryRejectedTopic = requireTopic(inventoryRejectedTopic, "inventoryRejectedTopic");
    }

    @Override
    public void publish(InventoryReservation reservation) {
        Objects.requireNonNull(reservation, "reservation must not be null");

        switch (reservation.status()) {
            case RESERVED -> publishReserved(reservation);
            case REJECTED -> publishRejected(reservation);
        }
    }

    private void publishReserved(InventoryReservation reservation) {
        var event = new InventoryReservedEvent(
                deterministicEventId(reservation),
                "inventory.reserved",
                EVENT_VERSION,
                reservation.createdAt(),
                reservation.orderId(),
                reservation.eventId(),
                reservation.lines().stream()
                        .map(line -> new InventoryReservedEvent.Line(line.sku(), line.quantity()))
                        .toList());

        send(inventoryReservedTopic, reservation.orderId().toString(), event);
    }

    private void publishRejected(InventoryReservation reservation) {
        var event = new InventoryRejectedEvent(
                deterministicEventId(reservation),
                "inventory.rejected",
                EVENT_VERSION,
                reservation.createdAt(),
                reservation.orderId(),
                reservation.eventId(),
                reservation.reason(),
                reservation.lines().stream()
                        .map(line -> new InventoryRejectedEvent.Line(line.sku(), line.quantity()))
                        .toList());

        send(inventoryRejectedTopic, reservation.orderId().toString(), event);
    }

    private void send(String topic, String key, Object event) {
        try {
            kafkaTemplate.send(topic, key, event).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new InventoryEventPublicationException("Interrupted while publishing inventory event", exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new InventoryEventPublicationException("Failed to publish inventory event", exception);
        }
    }

    static UUID deterministicEventId(InventoryReservation reservation) {
        var seed = "inventory."
                + reservation.status().name().toLowerCase(Locale.ROOT)
                + ":"
                + reservation.eventId();
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
    }

    private static String requireTopic(String topic, String name) {
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return topic;
    }
}
