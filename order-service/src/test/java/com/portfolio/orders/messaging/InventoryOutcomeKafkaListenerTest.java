package com.portfolio.orders.messaging;

import com.portfolio.orders.application.HandleInventoryOutcomeService;
import com.portfolio.orders.messaging.event.InventoryOutcomeEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class InventoryOutcomeKafkaListenerTest {

    @Test
    void delegatesConsumedOutcomeToApplicationService() {
        HandleInventoryOutcomeService service = mock(HandleInventoryOutcomeService.class);
        InventoryOutcomeKafkaListener listener = new InventoryOutcomeKafkaListener(service);
        InventoryOutcomeEvent event = new InventoryOutcomeEvent(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "inventory.reserved",
                1,
                Instant.parse("2026-08-29T16:30:00Z"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                null,
                List.of(new InventoryOutcomeEvent.Line("SKU-100", 2)));

        listener.onInventoryOutcome(event);

        verify(service).handle(event);
    }
}
