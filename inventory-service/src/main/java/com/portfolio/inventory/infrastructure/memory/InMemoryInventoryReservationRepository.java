package com.portfolio.inventory.infrastructure.memory;

import com.portfolio.inventory.application.port.InventoryReservationRepository;
import com.portfolio.inventory.domain.InventoryReservation;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("memory")
public class InMemoryInventoryReservationRepository implements InventoryReservationRepository {
    private final Map<UUID, InventoryReservation> reservationsByOrder = new ConcurrentHashMap<>();

    @Override
    public InventoryReservation save(InventoryReservation reservation) {
        reservationsByOrder.put(reservation.orderId(), reservation);
        return reservation;
    }

    @Override
    public Optional<InventoryReservation> findByOrderId(UUID orderId) {
        return Optional.ofNullable(reservationsByOrder.get(orderId));
    }
}
