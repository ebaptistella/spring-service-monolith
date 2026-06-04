package dev.ebaptistella.monolith.modules.identity.models;

import dev.ebaptistella.monolith.shared.models.auth.AuthProvider;

import java.time.Instant;
import java.util.UUID;

public record AccountIdentityLink(
        UUID id,
        UUID accountId,
        AuthProvider provider,
        String externalSubject,
        Instant linkedAt
) {
}
