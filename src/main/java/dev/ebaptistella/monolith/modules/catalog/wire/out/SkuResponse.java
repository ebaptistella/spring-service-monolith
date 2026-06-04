package dev.ebaptistella.monolith.modules.catalog.wire.out;

import dev.ebaptistella.monolith.modules.catalog.models.Sku;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SkuResponse(
        UUID id,
        UUID productId,
        String code,
        BigDecimal listPrice,
        boolean active,
        Instant createdAt
) {

    public static SkuResponse from(Sku sku) {
        return new SkuResponse(
                sku.id(),
                sku.productId(),
                sku.code(),
                sku.listPrice(),
                sku.active(),
                sku.createdAt());
    }
}
