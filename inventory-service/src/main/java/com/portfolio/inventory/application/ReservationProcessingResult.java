package com.portfolio.inventory.application;

import com.portfolio.inventory.domain.InventoryReservation;

import java.util.Objects;

public record ReservationProcessingResult(Status status, InventoryReservation reservation) {
    public enum Status { PROCESSED, DUPLICATE }

    public ReservationProcessingResult {
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(reservation, "reservation must not be null");
    }

    public static ReservationProcessingResult processed(InventoryReservation reservation) {
        return new ReservationProcessingResult(Status.PROCESSED, reservation);
    }

    public static ReservationProcessingResult duplicate(InventoryReservation reservation) {
        return new ReservationProcessingResult(Status.DUPLICATE, reservation);
    }
}
