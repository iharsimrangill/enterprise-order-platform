package com.portfolio.orders.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Lifecycle states for an order aggregate.
 *
 * <p>Transitions are intentionally defined in the domain layer so callers cannot
 * move an order into an invalid state without going through the aggregate.</p>
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    REJECTED,
    CANCELLED,
    FULFILLED;

    private static final Set<OrderStatus> PENDING_TRANSITIONS =
            EnumSet.of(CONFIRMED, REJECTED, CANCELLED);
    private static final Set<OrderStatus> CONFIRMED_TRANSITIONS =
            EnumSet.of(FULFILLED, CANCELLED);

    public boolean canTransitionTo(OrderStatus target) {
        if (target == null) {
            return false;
        }

        return switch (this) {
            case PENDING -> PENDING_TRANSITIONS.contains(target);
            case CONFIRMED -> CONFIRMED_TRANSITIONS.contains(target);
            case REJECTED, CANCELLED, FULFILLED -> false;
        };
    }
}
