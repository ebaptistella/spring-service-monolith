package dev.ebaptistella.monolith.shared.wire.in.events;

import dev.ebaptistella.monolith.shared.EventRoutes;
import org.springframework.modulith.events.Externalized;

import java.math.BigDecimal;
import java.util.UUID;

@Externalized(EventRoutes.ORDER_CONFIRMED_EXTERNALIZED)
public record OrderConfirmedEvent(
        UUID idempotencyKey,
        UUID orderId,
        UUID customerId,
        String customerEmail,
        BigDecimal totalAmount) {
}
