package dev.ebaptistella.monolith.modules.inventory.adapters;

import dev.ebaptistella.monolith.modules.inventory.models.OrderPlacedInventoryInput;
import dev.ebaptistella.monolith.modules.inventory.models.StockReserveInput;
import dev.ebaptistella.monolith.modules.inventory.models.StockReserveLine;
import dev.ebaptistella.monolith.shared.wire.in.events.OrderCancelledEvent;
import dev.ebaptistella.monolith.shared.wire.in.events.OrderConfirmedEvent;
import dev.ebaptistella.monolith.shared.wire.in.events.OrderLinePayload;
import dev.ebaptistella.monolith.shared.wire.in.events.OrderPlacedEvent;
import dev.ebaptistella.monolith.shared.wire.in.events.StockReservationFailedEvent;
import dev.ebaptistella.monolith.shared.wire.in.events.StockReservedEvent;

import java.math.BigDecimal;
import java.util.UUID;

public final class StockEventAdapter {

    private StockEventAdapter() {
    }

    public static OrderPlacedInventoryInput wireToModel(OrderPlacedEvent wire) {
        return new OrderPlacedInventoryInput(
                wire.idempotencyKey(),
                wire.orderId(),
                wire.lines().stream()
                        .map(StockEventAdapter::lineToReserveLine)
                        .toList(),
                wire.totalAmount());
    }

    public static StockReserveInput orderPlacedToReserveInput(OrderPlacedEvent wire) {
        return wireToModel(wire).toReserveInput();
    }

    private static StockReserveLine lineToReserveLine(OrderLinePayload line) {
        return new StockReserveLine(line.skuId(), line.quantity());
    }

    public static StockReservedEvent toStockReserved(UUID idempotencyKey, UUID orderId, BigDecimal totalAmount) {
        return new StockReservedEvent(idempotencyKey, orderId, totalAmount);
    }

    public static StockReservationFailedEvent toStockReservationFailed(
            UUID idempotencyKey, UUID orderId, String reason) {
        return new StockReservationFailedEvent(idempotencyKey, orderId, reason);
    }

    public static UUID orderIdFrom(OrderConfirmedEvent event) {
        return event.orderId();
    }

    public static UUID orderIdFrom(OrderCancelledEvent event) {
        return event.orderId();
    }
}
