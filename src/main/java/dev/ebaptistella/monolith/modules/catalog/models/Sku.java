package dev.ebaptistella.monolith.modules.catalog.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Sku(
        UUID id,
        UUID productId,
        String code,
        BigDecimal listPrice,
        boolean active,
        Instant createdAt,
        UUID idempotencyKey,
        UUID rootIdempotencyKey,
        String requestFingerprint
) {
}
