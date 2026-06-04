package dev.ebaptistella.monolith.modules.catalog.models;

import java.time.Instant;
import java.util.UUID;

public record Product(
        UUID id,
        String name,
        String description,
        boolean active,
        Instant createdAt,
        UUID idempotencyKey,
        UUID rootIdempotencyKey,
        String requestFingerprint
) {
}
