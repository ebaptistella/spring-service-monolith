package dev.ebaptistella.monolith.modules.finance.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentIntent(
        UUID id,
        UUID orderId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        Instant createdAt,
        Instant updatedAt,
        UUID idempotencyKey,
        UUID rootIdempotencyKey) {
}
