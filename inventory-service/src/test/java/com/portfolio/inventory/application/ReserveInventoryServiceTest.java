package com.portfolio.inventory.application;

import com.portfolio.inventory.application.port.InventoryAvailabilityPort;
import com.portfolio.inventory.application.port.InventoryReservationRepository;
import com.portfolio.inventory.application.port.ProcessedEventRepository;
import com.portfolio.inventory.domain.ReservationStatus;
import com.portfolio.inventory.messaging.event.OrderCreatedEvent;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReserveInventoryServiceTest {
    private final InventoryAvailabilityPort availability = mock(InventoryAvailabilityPort.class);
    private final InventoryReservationRepository reservations = mock(InventoryReservationRepository.class);
    private final ProcessedEventRepository processedEvents = mock(ProcessedEventRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-29T16:00:00Z"), ZoneOffset.UTC);
    private final ReserveInventoryService service =
            new ReserveInventoryService(availability, reservations, processedEvents, clock);

    @Test
    void reservesEveryLineWhenStockIsAvailable() {
        var event = event("SKU-1", 2, "SKU-2", 1);
        when(availability.isAvailable(anyString(), anyInt())).thenReturn(true);
        when(reservations.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.handle(event);

        assertThat(result.status()).isEqualTo(ReservationProcessingResult.Status.PROCESSED);
        assertThat(result.reservation().status()).isEqualTo(ReservationStatus.RESERVED);
        verify(availability).reserve("SKU-1", 2);
        verify(availability).reserve("SKU-2", 1);
        verify(processedEvents).markProcessed(event.eventId());
    }

    @Test
    void rejectsReservationWithoutMutatingStockWhenAnyLineIsUnavailable() {
        var event = event("SKU-1", 2, "SKU-2", 5);
        when(availability.isAvailable("SKU-1", 2)).thenReturn(true);
        when(availability.isAvailable("SKU-2", 5)).thenReturn(false);
        when(reservations.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.handle(event);

        assertThat(result.reservation().status()).isEqualTo(ReservationStatus.REJECTED);
        assertThat(result.reservation().reason()).contains("SKU-2");
        verify(availability, never()).reserve(anyString(), anyInt());
        verify(processedEvents).markProcessed(event.eventId());
    }

    @Test
    void ignoresDuplicateEvents() {
        var event = event("SKU-1", 1, "SKU-2", 1);
        when(processedEvents.exists(event.eventId())).thenReturn(true);

        var result = service.handle(event);

        assertThat(result.status()).isEqualTo(ReservationProcessingResult.Status.DUPLICATE);
        assertThat(result.reservation()).isNull();
        verifyNoInteractions(availability, reservations);
    }

    private static OrderCreatedEvent event(String sku1, int qty1, String sku2, int qty2) {
        return new OrderCreatedEvent(
                UUID.randomUUID(), "order.created", 1, Instant.parse("2026-08-29T15:59:00Z"),
                UUID.randomUUID(), UUID.randomUUID(), "PENDING", BigDecimal.valueOf(50),
                List.of(
                        new OrderCreatedEvent.Line(sku1, qty1, BigDecimal.TEN, BigDecimal.valueOf(20)),
                        new OrderCreatedEvent.Line(sku2, qty2, BigDecimal.valueOf(30), BigDecimal.valueOf(30))));
    }
}
