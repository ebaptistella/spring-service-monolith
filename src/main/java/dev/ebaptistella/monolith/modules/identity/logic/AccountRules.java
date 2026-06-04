package dev.ebaptistella.monolith.modules.identity.logic;

import dev.ebaptistella.monolith.modules.identity.models.Account;
import dev.ebaptistella.monolith.modules.identity.models.AccountIdentityLink;
import dev.ebaptistella.monolith.modules.identity.models.AccountStatus;
import dev.ebaptistella.monolith.modules.identity.models.LocalRegistrationInput;
import dev.ebaptistella.monolith.shared.models.auth.AuthProvider;
import dev.ebaptistella.monolith.shared.models.auth.Role;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class AccountRules {

    private static final Set<Role> DEFAULT_ROLES = Set.of(Role.USER);

    private AccountRules() {
    }

    public static LocalRegistrationResult register(LocalRegistrationInput input, boolean emailExists) {
        if (emailExists) {
            return LocalRegistrationResult.duplicateEmail(input.email());
        }

        UUID accountId = UUID.randomUUID();
        Instant now = Instant.now();
        Account account = new Account(accountId, input.email(), AccountStatus.ACTIVE, DEFAULT_ROLES, now, null, null, null);
        return LocalRegistrationResult.success(account);
    }

    public static AccountIdentityLink localIdentityLink(Account account) {
        return new AccountIdentityLink(
                UUID.randomUUID(),
                account.id(),
                AuthProvider.LOCAL,
                account.id().toString(),
                account.createdAt());
    }

    public static Account withStatus(Account account, AccountStatus status) {
        return new Account(
                account.id(),
                account.email(),
                status,
                account.roles(),
                account.createdAt(),
                account.idempotencyKey(),
                account.rootIdempotencyKey(),
                account.requestFingerprint());
    }

    public static Account provisionExternalAccount(
            String emailHint, Set<Role> rolesHint, AuthProvider provider, String externalSubject) {
        Set<Role> roles = rolesHint == null || rolesHint.isEmpty() ? DEFAULT_ROLES : Set.copyOf(rolesHint);
        UUID accountId = UUID.randomUUID();
        Instant now = Instant.now();
        return new Account(accountId, emailHint, AccountStatus.ACTIVE, roles, now, null, null, null);
    }

    public static AccountIdentityLink externalIdentityLink(
            Account account, AuthProvider provider, String externalSubject) {
        return new AccountIdentityLink(
                UUID.randomUUID(), account.id(), provider, externalSubject, account.createdAt());
    }
}
