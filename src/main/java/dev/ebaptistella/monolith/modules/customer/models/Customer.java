package dev.ebaptistella.monolith.modules.customer.models;

import java.time.Instant;
import java.util.UUID;

public record Customer(
        UUID id,
        String email,
        String fullName,
        Instant createdAt,
        UUID idempotencyKey,
        UUID rootIdempotencyKey,
        String requestFingerprint
) {
}
