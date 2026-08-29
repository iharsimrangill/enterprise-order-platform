package com.portfolio.inventory.application;

import com.portfolio.inventory.domain.InventoryReservation;

public record ReservationProcessingResult(Status status, InventoryReservation reservation) {
    public enum Status { PROCESSED, DUPLICATE }

    public static ReservationProcessingResult processed(InventoryReservation reservation) {
        return new ReservationProcessingResult(Status.PROCESSED, reservation);
    }

    public static ReservationProcessingResult duplicate() {
        return new ReservationProcessingResult(Status.DUPLICATE, null);
    }
}
