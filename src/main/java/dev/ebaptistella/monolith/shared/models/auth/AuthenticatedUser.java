package dev.ebaptistella.monolith.shared.models.auth;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedUser(
        UUID accountId,
        String email,
        Set<Role> roles,
        AuthProvider authProvider
) {
}
