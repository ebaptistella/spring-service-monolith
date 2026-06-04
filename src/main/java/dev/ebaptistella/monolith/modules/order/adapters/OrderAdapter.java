package dev.ebaptistella.monolith.modules.order.adapters;

import dev.ebaptistella.monolith.modules.order.models.Order;
import dev.ebaptistella.monolith.modules.order.models.OrderLine;
import dev.ebaptistella.monolith.modules.order.models.PlaceOrderInput;
import dev.ebaptistella.monolith.modules.order.models.PlaceOrderLineInput;
import dev.ebaptistella.monolith.modules.order.wire.in.PlaceOrderLineRequest;
import dev.ebaptistella.monolith.modules.order.wire.in.PlaceOrderRequest;
import dev.ebaptistella.monolith.modules.order.wire.out.OrderLineResponse;
import dev.ebaptistella.monolith.modules.order.wire.out.OrderResponse;

public final class OrderAdapter {

    private OrderAdapter() {
    }

    public static PlaceOrderInput wireInToPlaceOrderInput(PlaceOrderRequest request) {
        return new PlaceOrderInput(
                request.customerId(),
                request.lines().stream()
                        .map(OrderAdapter::wireInToPlaceOrderLineInput)
                        .toList());
    }

    private static PlaceOrderLineInput wireInToPlaceOrderLineInput(PlaceOrderLineRequest line) {
        return new PlaceOrderLineInput(line.skuId(), line.quantity());
    }

    public static OrderResponse modelToWireOut(Order order) {
        return new OrderResponse(
                order.id(),
                order.customerId(),
                order.status(),
                order.totalAmount(),
                order.currency(),
                order.lines().stream()
                        .map(OrderAdapter::lineToWireOut)
                        .toList(),
                order.createdAt(),
                order.updatedAt());
    }

    private static OrderLineResponse lineToWireOut(OrderLine line) {
        return new OrderLineResponse(
                line.id(),
                line.skuId(),
                line.quantity(),
                line.unitPrice(),
                line.lineTotal());
    }
}
