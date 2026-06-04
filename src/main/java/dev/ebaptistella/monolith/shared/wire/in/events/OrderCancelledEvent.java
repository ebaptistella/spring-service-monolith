package dev.ebaptistella.monolith.shared.wire.in.events;

import dev.ebaptistella.monolith.shared.EventRoutes;
import org.springframework.modulith.events.Externalized;

import java.math.BigDecimal;
import java.util.UUID;

@Externalized(EventRoutes.ORDER_CANCELLED_EXTERNALIZED)
public record OrderCancelledEvent(
        UUID idempotencyKey,
        UUID orderId,
        UUID customerId,
        String customerEmail,
        BigDecimal totalAmount,
        String reason) {
}
