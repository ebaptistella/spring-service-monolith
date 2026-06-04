package dev.ebaptistella.monolith.shared.wire.in.events;

import dev.ebaptistella.monolith.shared.EventRoutes;
import org.springframework.modulith.events.Externalized;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Externalized(EventRoutes.ORDER_PLACED_EXTERNALIZED)
public record OrderPlacedEvent(
        UUID idempotencyKey,
        UUID orderId,
        UUID customerId,
        BigDecimal totalAmount,
        List<OrderLinePayload> lines) {
}
