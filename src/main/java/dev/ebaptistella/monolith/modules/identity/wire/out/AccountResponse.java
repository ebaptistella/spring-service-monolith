package dev.ebaptistella.monolith.modules.identity.wire.out;

import dev.ebaptistella.monolith.modules.identity.models.Account;
import dev.ebaptistella.monolith.modules.identity.models.AccountStatus;
import dev.ebaptistella.monolith.shared.models.auth.Role;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AccountResponse(
        UUID id, String email, AccountStatus status, Set<Role> roles, Instant createdAt) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.id(), account.email(), account.status(), account.roles(), account.createdAt());
    }
}
