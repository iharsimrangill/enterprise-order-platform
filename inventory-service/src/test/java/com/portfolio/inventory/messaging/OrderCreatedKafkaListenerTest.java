package com.portfolio.inventory.messaging;

import com.portfolio.inventory.application.ReservationProcessingResult;
import com.portfolio.inventory.application.ReserveInventoryService;
import com.portfolio.inventory.application.port.InventoryEventPublisher;
import com.portfolio.inventory.domain.InventoryReservation;
import com.portfolio.inventory.domain.ReservationStatus;
import com.portfolio.inventory.messaging.event.OrderCreatedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

class OrderCreatedKafkaListenerTest {
    @Test
    void publishesOutcomeAfterReservationProcessing() {
        var service = mock(ReserveInventoryService.class);
        var publisher = mock(InventoryEventPublisher.class);
        var listener = new OrderCreatedKafkaListener(service, publisher);
        var event = event();
        var reservation = reservation(event, ReservationStatus.RESERVED, null);
        when(service.handle(event)).thenReturn(ReservationProcessingResult.processed(reservation));

        listener.consume(event);

        var inOrder = inOrder(service, publisher);
        inOrder.verify(service).handle(event);
        inOrder.verify(publisher).publish(reservation);
    }

    @Test
    void republishesPersistedOutcomeForDuplicateInputEvent() {
        var service = mock(ReserveInventoryService.class);
        var publisher = mock(InventoryEventPublisher.class);
        var listener = new OrderCreatedKafkaListener(service, publisher);
        var event = event();
        var reservation = reservation(event, ReservationStatus.REJECTED, "Insufficient stock");
        when(service.handle(event)).thenReturn(ReservationProcessingResult.duplicate(reservation));

        listener.consume(event);

        verify(publisher).publish(reservation);
    }

    private static OrderCreatedEvent event() {
        return new OrderCreatedEvent(
                UUID.randomUUID(), "order.created", 1, Instant.parse("2026-08-29T16:00:00Z"),
                UUID.randomUUID(), UUID.randomUUID(), "PENDING", BigDecimal.TEN,
                List.of(new OrderCreatedEvent.Line("SKU-1", 1, BigDecimal.TEN, BigDecimal.TEN)));
    }

    private static InventoryReservation reservation(
            OrderCreatedEvent event,
            ReservationStatus status,
            String reason) {
        return new InventoryReservation(
                event.eventId(), event.orderId(), status, reason,
                Instant.parse("2026-08-29T16:00:01Z"),
                List.of(new InventoryReservation.Line("SKU-1", 1)));
    }
}
