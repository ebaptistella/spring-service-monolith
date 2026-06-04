package dev.ebaptistella.monolith.modules.identity.models;

import dev.ebaptistella.monolith.shared.models.auth.Role;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record Account(
        UUID id,
        String email,
        AccountStatus status,
        Set<Role> roles,
        Instant createdAt,
        UUID idempotencyKey,
        UUID rootIdempotencyKey,
        String requestFingerprint
) {
}
