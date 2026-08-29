package com.portfolio.inventory.messaging;

import com.portfolio.inventory.application.ReservationProcessingResult;
import com.portfolio.inventory.application.ReserveInventoryService;
import com.portfolio.inventory.messaging.event.OrderCreatedEvent;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;

class OrderCreatedKafkaListenerTest {
    @Test
    void delegatesConsumedEventToReservationUseCase() {
        var service = mock(ReserveInventoryService.class);
        var listener = new OrderCreatedKafkaListener(service);
        var event = new OrderCreatedEvent(
                UUID.randomUUID(), "order.created", 1, Instant.now(),
                UUID.randomUUID(), UUID.randomUUID(), "PENDING", BigDecimal.TEN,
                List.of(new OrderCreatedEvent.Line("SKU-1", 1, BigDecimal.TEN, BigDecimal.TEN)));
        when(service.handle(event)).thenReturn(ReservationProcessingResult.duplicate());

        listener.consume(event);

        verify(service).handle(event);
    }
}
