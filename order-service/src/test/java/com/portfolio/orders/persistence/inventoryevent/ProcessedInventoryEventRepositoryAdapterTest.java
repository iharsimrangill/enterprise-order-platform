package com.portfolio.orders.persistence.inventoryevent;

import com.portfolio.orders.application.port.ProcessedInventoryEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(JpaProcessedInventoryEventRepositoryAdapter.class)
class ProcessedInventoryEventRepositoryAdapterTest {

    @Autowired
    private ProcessedInventoryEventRepository repository;

    @Test
    void storesDurableIdempotencyMarker() {
        UUID eventId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        assertThat(repository.existsByEventId(eventId)).isFalse();

        repository.markProcessed(
                eventId,
                UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                "inventory.reserved",
                Instant.parse("2026-08-29T16:30:00Z"));

        assertThat(repository.existsByEventId(eventId)).isTrue();
    }
}
