package dev.ebaptistella.monolith.modules.order.logic;

import dev.ebaptistella.monolith.modules.order.models.Order;

import java.util.Optional;

public record PlaceOrderResult(Optional<Order> order, Optional<String> error, boolean replay) {

    public static PlaceOrderResult success(Order order) {
        return new PlaceOrderResult(Optional.of(order), Optional.empty(), false);
    }

    public static PlaceOrderResult replay(Order order) {
        return new PlaceOrderResult(Optional.of(order), Optional.empty(), true);
    }

    public static PlaceOrderResult rejected(String error) {
        return new PlaceOrderResult(Optional.empty(), Optional.of(error), false);
    }

    public boolean rejected() {
        return error.isPresent();
    }
}
