package dev.ebaptistella.monolith.shared.wire.in.events;

import dev.ebaptistella.monolith.shared.EventRoutes;
import org.springframework.modulith.events.Externalized;

import java.util.UUID;

@Externalized(EventRoutes.STOCK_RESERVATION_FAILED_EXTERNALIZED)
public record StockReservationFailedEvent(UUID idempotencyKey, UUID orderId, String reason) {
}
