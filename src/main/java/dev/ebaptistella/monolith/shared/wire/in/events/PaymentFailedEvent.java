package dev.ebaptistella.monolith.shared.wire.in.events;

import dev.ebaptistella.monolith.shared.EventRoutes;
import org.springframework.modulith.events.Externalized;

import java.util.UUID;

@Externalized(EventRoutes.PAYMENT_FAILED_EXTERNALIZED)
public record PaymentFailedEvent(UUID idempotencyKey, UUID orderId, String reason) {
}
