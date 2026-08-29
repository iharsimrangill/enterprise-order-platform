package com.portfolio.inventory.application;

import com.portfolio.inventory.application.port.InventoryAvailabilityPort;
import com.portfolio.inventory.application.port.InventoryReservationRepository;
import com.portfolio.inventory.application.port.ProcessedEventRepository;
import com.portfolio.inventory.domain.InventoryReservation;
import com.portfolio.inventory.domain.ReservationStatus;
import com.portfolio.inventory.messaging.event.OrderCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Objects;

@Service
public class ReserveInventoryService {
    private final InventoryAvailabilityPort availabilityPort;
    private final InventoryReservationRepository reservationRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final Clock clock;

    @Autowired
    public ReserveInventoryService(
            InventoryAvailabilityPort availabilityPort,
            InventoryReservationRepository reservationRepository,
            ProcessedEventRepository processedEventRepository) {
        this(availabilityPort, reservationRepository, processedEventRepository, Clock.systemUTC());
    }

    ReserveInventoryService(
            InventoryAvailabilityPort availabilityPort,
            InventoryReservationRepository reservationRepository,
            ProcessedEventRepository processedEventRepository,
            Clock clock) {
        this.availabilityPort = Objects.requireNonNull(availabilityPort);
        this.reservationRepository = Objects.requireNonNull(reservationRepository);
        this.processedEventRepository = Objects.requireNonNull(processedEventRepository);
        this.clock = Objects.requireNonNull(clock);
    }

    public synchronized ReservationProcessingResult handle(OrderCreatedEvent event) {
        Objects.requireNonNull(event, "event must not be null");

        if (processedEventRepository.exists(event.eventId())) {
            return ReservationProcessingResult.duplicate();
        }

        var lines = event.lines().stream()
                .map(line -> new InventoryReservation.Line(line.sku(), line.quantity()))
                .toList();

        var unavailableLine = lines.stream()
                .filter(line -> !availabilityPort.isAvailable(line.sku(), line.quantity()))
                .findFirst();

        InventoryReservation reservation;
        if (unavailableLine.isPresent()) {
            var line = unavailableLine.get();
            reservation = new InventoryReservation(
                    event.eventId(),
                    event.orderId(),
                    ReservationStatus.REJECTED,
                    "Insufficient stock for SKU " + line.sku(),
                    clock.instant(),
                    lines);
        } else {
            lines.forEach(line -> availabilityPort.reserve(line.sku(), line.quantity()));
            reservation = new InventoryReservation(
                    event.eventId(),
                    event.orderId(),
                    ReservationStatus.RESERVED,
                    null,
                    clock.instant(),
                    lines);
        }

        var saved = reservationRepository.save(reservation);
        processedEventRepository.markProcessed(event.eventId());
        return ReservationProcessingResult.processed(saved);
    }
}
