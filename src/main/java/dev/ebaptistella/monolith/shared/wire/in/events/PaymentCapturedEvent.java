package dev.ebaptistella.monolith.shared.wire.in.events;

import dev.ebaptistella.monolith.shared.EventRoutes;
import org.springframework.modulith.events.Externalized;

import java.math.BigDecimal;
import java.util.UUID;

@Externalized(EventRoutes.PAYMENT_CAPTURED_EXTERNALIZED)
public record PaymentCapturedEvent(UUID idempotencyKey, UUID orderId, UUID paymentIntentId, BigDecimal amount) {
}
