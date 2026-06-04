package dev.ebaptistella.monolith.modules.order.logic;

import dev.ebaptistella.monolith.modules.order.models.Order;
import dev.ebaptistella.monolith.modules.order.models.OrderLine;
import dev.ebaptistella.monolith.modules.order.models.OrderStatus;
import dev.ebaptistella.monolith.modules.order.models.PlaceOrderInput;
import dev.ebaptistella.monolith.modules.order.models.PlaceOrderLineInput;
import dev.ebaptistella.monolith.modules.order.models.PlaceOrderResolvedLine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class OrderRules {

    public static final BigDecimal SPEND_LIMIT = new BigDecimal("1000.00");
    public static final String DEFAULT_CURRENCY = "BRL";

    private OrderRules() {
    }

    public static PlaceOrderResult validatePlaceOrder(
            PlaceOrderInput input,
            BigDecimal confirmedTotal,
            boolean customerExists,
            List<PlaceOrderResolvedLine> resolvedLines,
            boolean stockAvailable) {
        if (!customerExists) {
            return PlaceOrderResult.rejected("Customer not found: " + input.customerId());
        }

        if (input.lines() == null || input.lines().isEmpty()) {
            return PlaceOrderResult.rejected("Order must contain at least one line");
        }

        if (resolvedLines == null || resolvedLines.size() != input.lines().size()) {
            return PlaceOrderResult.rejected("Order lines could not be resolved");
        }

        List<OrderLine> lines = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (int i = 0; i < input.lines().size(); i++) {
            PlaceOrderLineInput lineInput = input.lines().get(i);
            PlaceOrderResolvedLine resolved = resolvedLines.get(i);

            if (lineInput.quantity() <= 0) {
                return PlaceOrderResult.rejected("Quantity must be positive for SKU " + lineInput.skuId());
            }

            if (!lineInput.skuId().equals(resolved.skuId())) {
                return PlaceOrderResult.rejected("Resolved line mismatch for SKU " + lineInput.skuId());
            }

            if (lineInput.quantity() != resolved.quantity()) {
                return PlaceOrderResult.rejected("Resolved quantity mismatch for SKU " + lineInput.skuId());
            }

            if (resolved.unitPrice() == null) {
                return PlaceOrderResult.rejected("SKU has no list price: " + lineInput.skuId());
            }

            BigDecimal lineTotal = lineTotal(resolved.unitPrice(), lineInput.quantity());
            totalAmount = totalAmount.add(lineTotal);
        }

        if (!stockAvailable) {
            return PlaceOrderResult.rejected("Insufficient stock for one or more SKUs");
        }

        if (exceedsSpendLimit(confirmedTotal, totalAmount)) {
            return PlaceOrderResult.rejected(
                    "Order total would exceed customer spend limit of " + SPEND_LIMIT + " BRL");
        }

        Instant now = Instant.now();
        UUID orderId = UUID.randomUUID();

        for (PlaceOrderResolvedLine resolved : resolvedLines) {
            lines.add(new OrderLine(
                    UUID.randomUUID(),
                    orderId,
                    resolved.skuId(),
                    resolved.quantity(),
                    resolved.unitPrice(),
                    lineTotal(resolved.unitPrice(), resolved.quantity())));
        }

        Order order = new Order(
                orderId,
                input.customerId(),
                OrderStatus.PLACED,
                totalAmount.setScale(2, RoundingMode.HALF_UP),
                DEFAULT_CURRENCY,
                List.copyOf(lines),
                now,
                now,
                null,
                null,
                null);

        return PlaceOrderResult.success(order);
    }

    public static boolean exceedsSpendLimit(BigDecimal confirmedTotal, BigDecimal newTotal) {
        BigDecimal safeConfirmed = confirmedTotal == null ? BigDecimal.ZERO : confirmedTotal;
        BigDecimal safeNew = newTotal == null ? BigDecimal.ZERO : newTotal;
        return safeConfirmed.add(safeNew).compareTo(SPEND_LIMIT) > 0;
    }

    public static BigDecimal lineTotal(BigDecimal unitPrice, int quantity) {
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    public static boolean canMarkAwaitingPayment(OrderStatus status) {
        return status == OrderStatus.PLACED;
    }

    public static boolean canConfirm(OrderStatus status) {
        return status == OrderStatus.AWAITING_PAYMENT;
    }

    public static boolean canCancel(OrderStatus status) {
        return status == OrderStatus.PLACED || status == OrderStatus.AWAITING_PAYMENT;
    }

    /** Idempotent or invalid: skip StockReserved → AWAITING_PAYMENT transition. */
    public static boolean shouldSkipMarkAwaitingPayment(OrderStatus status) {
        return status == OrderStatus.AWAITING_PAYMENT || !canMarkAwaitingPayment(status);
    }

    /** Idempotent or invalid: skip cancel transitions from event handlers. */
    public static boolean shouldSkipCancelTransition(OrderStatus status) {
        return status == OrderStatus.CANCELLED || !canCancel(status);
    }

    /** Idempotent or invalid: skip PaymentCaptured → CONFIRMED transition. */
    public static boolean shouldSkipConfirmTransition(OrderStatus status) {
        return status == OrderStatus.CONFIRMED || !canConfirm(status);
    }

    public static Order toCancelled(Order order) {
        Instant now = Instant.now();
        return new Order(
                order.id(),
                order.customerId(),
                OrderStatus.CANCELLED,
                order.totalAmount(),
                order.currency(),
                order.lines(),
                order.createdAt(),
                now,
                order.idempotencyKey(),
                order.rootIdempotencyKey(),
                order.requestFingerprint());
    }
}
