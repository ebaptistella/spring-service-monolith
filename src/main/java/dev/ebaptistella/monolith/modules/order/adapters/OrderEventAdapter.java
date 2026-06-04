package dev.ebaptistella.monolith.modules.order.adapters;

import dev.ebaptistella.monolith.modules.order.models.Order;
import dev.ebaptistella.monolith.shared.wire.in.events.OrderCancelledEvent;
import dev.ebaptistella.monolith.shared.wire.in.events.OrderConfirmedEvent;
import dev.ebaptistella.monolith.shared.wire.in.events.OrderLinePayload;
import dev.ebaptistella.monolith.shared.wire.in.events.OrderPlacedEvent;
import dev.ebaptistella.monolith.shared.wire.in.events.PaymentCapturedEvent;
import dev.ebaptistella.monolith.shared.wire.in.events.PaymentFailedEvent;
import dev.ebaptistella.monolith.shared.wire.in.events.StockReservationFailedEvent;
import dev.ebaptistella.monolith.shared.wire.in.events.StockReservedEvent;

import java.math.BigDecimal;
import java.util.UUID;

public final class OrderEventAdapter {

    private OrderEventAdapter() {
    }

    public static OrderPlacedEvent toOrderPlaced(Order order) {
        return new OrderPlacedEvent(
                order.rootIdempotencyKey(),
                order.id(),
                order.customerId(),
                order.totalAmount(),
                order.lines().stream()
                        .map(OrderEventAdapter::lineToPayload)
                        .toList());
    }

    public static OrderConfirmedEvent toOrderConfirmed(Order order, String customerEmail) {
        return new OrderConfirmedEvent(
                order.rootIdempotencyKey(),
                order.id(),
                order.customerId(),
                customerEmail,
                order.totalAmount());
    }

    public static OrderCancelledEvent toOrderCancelled(Order order, String customerEmail, String reason) {
        return new OrderCancelledEvent(
                order.rootIdempotencyKey(),
                order.id(),
                order.customerId(),
                customerEmail,
                order.totalAmount(),
                reason);
    }

    private static OrderLinePayload lineToPayload(dev.ebaptistella.monolith.modules.order.models.OrderLine line) {
        return new OrderLinePayload(line.skuId(), line.quantity(), line.unitPrice());
    }

    public static UUID orderIdFrom(StockReservedEvent event) {
        return event.orderId();
    }

    public static UUID orderIdFrom(PaymentCapturedEvent event) {
        return event.orderId();
    }

    public static UUID orderIdFrom(PaymentFailedEvent event) {
        return event.orderId();
    }

    public static UUID orderIdFrom(StockReservationFailedEvent event) {
        return event.orderId();
    }

    public static String reasonFrom(StockReservationFailedEvent event) {
        return event.reason();
    }

    public static String reasonFrom(PaymentFailedEvent event) {
        return event.reason();
    }
}
