package dev.ebaptistella.monolith.modules.order.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Order(
        UUID id,
        UUID customerId,
        OrderStatus status,
        BigDecimal totalAmount,
        String currency,
        List<OrderLine> lines,
        Instant createdAt,
        Instant updatedAt,
        UUID idempotencyKey,
        UUID rootIdempotencyKey,
        String requestFingerprint) {
}
