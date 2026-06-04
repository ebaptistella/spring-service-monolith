package dev.ebaptistella.monolith.modules.finance.adapters;

import dev.ebaptistella.monolith.modules.finance.models.CapturePaymentInput;
import dev.ebaptistella.monolith.modules.finance.models.PaymentIntent;
import dev.ebaptistella.monolith.shared.wire.in.events.OrderCancelledEvent;
import dev.ebaptistella.monolith.shared.wire.in.events.PaymentCapturedEvent;
import dev.ebaptistella.monolith.shared.wire.in.events.PaymentFailedEvent;
import dev.ebaptistella.monolith.shared.wire.in.events.StockReservedEvent;

import java.math.BigDecimal;
import java.util.UUID;

public final class PaymentEventAdapter {

    private PaymentEventAdapter() {
    }

    public static CapturePaymentInput stockReservedToCaptureInput(StockReservedEvent event) {
        return new CapturePaymentInput(event.idempotencyKey(), event.orderId(), event.totalAmount());
    }

    public static PaymentCapturedEvent toPaymentCaptured(PaymentIntent intent) {
        return new PaymentCapturedEvent(
                intent.rootIdempotencyKey(), intent.orderId(), intent.id(), intent.amount());
    }

    public static PaymentFailedEvent toPaymentFailed(PaymentIntent intent, String reason) {
        return new PaymentFailedEvent(intent.rootIdempotencyKey(), intent.orderId(), reason);
    }

    public static PaymentFailedEvent toPaymentFailed(UUID rootIdempotencyKey, UUID orderId, String reason) {
        return new PaymentFailedEvent(rootIdempotencyKey, orderId, reason);
    }

    public static UUID orderIdFrom(OrderCancelledEvent event) {
        return event.orderId();
    }
}
