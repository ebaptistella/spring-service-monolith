package dev.ebaptistella.monolith.shared.wire.in.events;

import dev.ebaptistella.monolith.shared.EventRoutes;
import org.springframework.modulith.events.Externalized;

import java.math.BigDecimal;
import java.util.UUID;

@Externalized(EventRoutes.STOCK_RESERVED_EXTERNALIZED)
public record StockReservedEvent(UUID idempotencyKey, UUID orderId, BigDecimal totalAmount) {
}
