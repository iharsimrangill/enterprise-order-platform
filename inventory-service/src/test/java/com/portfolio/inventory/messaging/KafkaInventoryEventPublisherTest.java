package com.portfolio.inventory.messaging;

import com.portfolio.inventory.domain.InventoryReservation;
import com.portfolio.inventory.domain.ReservationStatus;
import com.portfolio.inventory.messaging.event.InventoryRejectedEvent;
import com.portfolio.inventory.messaging.event.InventoryReservedEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class KafkaInventoryEventPublisherTest {

    @Test
    void publishesReservedEventUsingOrderIdAsKey() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.<SendResult<String, Object>>completedFuture(null));
        var publisher = new KafkaInventoryEventPublisher(
                kafkaTemplate, "inventory.reserved.v1", "inventory.rejected.v1");
        var reservation = reservation(ReservationStatus.RESERVED, null);

        publisher.publish(reservation);

        var payload = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(
                eq("inventory.reserved.v1"),
                eq(reservation.orderId().toString()),
                payload.capture());
        assertThat(payload.getValue()).isInstanceOf(InventoryReservedEvent.class);
        var event = (InventoryReservedEvent) payload.getValue();
        assertThat(event.eventType()).isEqualTo("inventory.reserved");
        assertThat(event.eventVersion()).isEqualTo(1);
        assertThat(event.eventId()).isEqualTo(KafkaInventoryEventPublisher.deterministicEventId(reservation));
        assertThat(event.sourceOrderEventId()).isEqualTo(reservation.eventId());
    }

    @Test
    void publishesRejectedEventWithReason() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.<SendResult<String, Object>>completedFuture(null));
        var publisher = new KafkaInventoryEventPublisher(
                kafkaTemplate, "inventory.reserved.v1", "inventory.rejected.v1");
        var reservation = reservation(ReservationStatus.REJECTED, "Insufficient stock for SKU SKU-1");

        publisher.publish(reservation);

        var payload = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(
                eq("inventory.rejected.v1"),
                eq(reservation.orderId().toString()),
                payload.capture());
        assertThat(payload.getValue()).isInstanceOf(InventoryRejectedEvent.class);
        var event = (InventoryRejectedEvent) payload.getValue();
        assertThat(event.reason()).isEqualTo("Insufficient stock for SKU SKU-1");
        assertThat(event.eventId()).isEqualTo(KafkaInventoryEventPublisher.deterministicEventId(reservation));
    }

    @Test
    void generatesSameOutcomeEventIdWhenReservationIsRepublished() {
        var reservation = reservation(ReservationStatus.RESERVED, null);

        var first = KafkaInventoryEventPublisher.deterministicEventId(reservation);
        var second = KafkaInventoryEventPublisher.deterministicEventId(reservation);

        assertThat(first).isEqualTo(second);
    }

    private static InventoryReservation reservation(ReservationStatus status, String reason) {
        return new InventoryReservation(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                status,
                reason,
                Instant.parse("2026-08-29T16:00:00Z"),
                List.of(
                        new InventoryReservation.Line("SKU-1", 2),
                        new InventoryReservation.Line("SKU-2", 1)));
    }
}
