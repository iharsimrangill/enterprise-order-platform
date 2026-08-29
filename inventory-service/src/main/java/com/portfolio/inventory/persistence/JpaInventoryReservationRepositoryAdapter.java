package com.portfolio.inventory.persistence;

import com.portfolio.inventory.application.port.InventoryReservationRepository;
import com.portfolio.inventory.domain.InventoryReservation;
import com.portfolio.inventory.persistence.entity.InventoryReservationEntity;
import com.portfolio.inventory.persistence.entity.ReservationLineEmbeddable;
import com.portfolio.inventory.persistence.repository.SpringDataInventoryReservationRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@Profile("!memory")
public class JpaInventoryReservationRepositoryAdapter implements InventoryReservationRepository {
    private final SpringDataInventoryReservationRepository repository;

    public JpaInventoryReservationRepositoryAdapter(SpringDataInventoryReservationRepository repository) {
        this.repository = repository;
    }

    @Override
    public InventoryReservation save(InventoryReservation reservation) {
        var saved = repository.save(toEntity(reservation));
        return toDomain(saved);
    }

    @Override
    public Optional<InventoryReservation> findByOrderId(UUID orderId) {
        return repository.findByOrderId(orderId).map(this::toDomain);
    }

    private InventoryReservationEntity toEntity(InventoryReservation reservation) {
        var lines = reservation.lines().stream()
                .map(line -> new ReservationLineEmbeddable(line.sku(), line.quantity()))
                .toList();
        return new InventoryReservationEntity(
                reservation.eventId(),
                reservation.orderId(),
                reservation.status(),
                reservation.reason(),
                reservation.createdAt(),
                lines);
    }

    private InventoryReservation toDomain(InventoryReservationEntity entity) {
        var lines = entity.getLines().stream()
                .map(line -> new InventoryReservation.Line(line.getSku(), line.getQuantity()))
                .toList();
        return new InventoryReservation(
                entity.getEventId(),
                entity.getOrderId(),
                entity.getStatus(),
                entity.getReason(),
                entity.getCreatedAt(),
                lines);
    }
}
