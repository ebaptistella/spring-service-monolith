package dev.ebaptistella.monolith.modules.notification.adapters;

import dev.ebaptistella.monolith.modules.notification.models.OrderCancelledNotification;
import dev.ebaptistella.monolith.shared.wire.in.events.OrderCancelledEvent;

public final class OrderCancelledEventAdapter {

    private OrderCancelledEventAdapter() {
    }

    public static OrderCancelledNotification wireToModel(OrderCancelledEvent wire) {
        return new OrderCancelledNotification(
                wire.idempotencyKey(),
                wire.orderId(),
                wire.customerId(),
                wire.customerEmail(),
                wire.totalAmount(),
                wire.reason());
    }
}
