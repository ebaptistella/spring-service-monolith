package dev.ebaptistella.monolith.modules.notification.adapters;

import dev.ebaptistella.monolith.modules.notification.models.OrderConfirmedNotification;
import dev.ebaptistella.monolith.shared.wire.in.events.OrderConfirmedEvent;

public final class OrderConfirmedEventAdapter {

    private OrderConfirmedEventAdapter() {
    }

    public static OrderConfirmedNotification wireToModel(OrderConfirmedEvent wire) {
        return new OrderConfirmedNotification(
                wire.idempotencyKey(),
                wire.orderId(),
                wire.customerId(),
                wire.customerEmail(),
                wire.totalAmount());
    }
}
