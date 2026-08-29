package com.portfolio.orders.domain;

public class InvalidOrderStateTransitionException extends IllegalStateException {

    public InvalidOrderStateTransitionException(OrderStatus from, OrderStatus to) {
        super("Order cannot transition from " + from + " to " + to);
    }
}
