package dev.ebaptistella.monolith.modules.inventory.models;

import java.time.Instant;
import java.util.UUID;

public record StockReservation(
        UUID id,
        UUID orderId,
        UUID skuId,
        int quantity,
        ReservationStatus status,
        Instant createdAt,
        UUID idempotencyKey) {
}
