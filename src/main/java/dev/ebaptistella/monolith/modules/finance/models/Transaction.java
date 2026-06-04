package dev.ebaptistella.monolith.modules.finance.models;

import java.time.Instant;
import java.util.UUID;

public record Transaction(
        UUID id,
        UUID paymentIntentId,
        PaymentStatus status,
        String gatewayReference,
        Instant createdAt,
        UUID idempotencyKey) {
}
