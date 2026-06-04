package dev.ebaptistella.monolith.modules.inventory.models;

import java.time.Instant;
import java.util.UUID;

public record StockMovement(
        UUID id,
        UUID skuId,
        UUID orderId,
        int quantity,
        MovementType type,
        Instant createdAt,
        UUID idempotencyKey) {
}
