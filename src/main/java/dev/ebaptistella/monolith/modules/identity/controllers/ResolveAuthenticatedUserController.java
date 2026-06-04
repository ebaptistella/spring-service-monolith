package dev.ebaptistella.monolith.modules.identity.controllers;

import dev.ebaptistella.monolith.shared.contracts.IdentityResolver;
import dev.ebaptistella.monolith.modules.identity.config.IdentityProperties;
import dev.ebaptistella.monolith.modules.identity.diplomat.jpa.AccountPersistence;
import dev.ebaptistella.monolith.modules.identity.logic.AccountRules;
import dev.ebaptistella.monolith.modules.identity.models.Account;
import dev.ebaptistella.monolith.modules.identity.models.AccountStatus;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyKeys;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyPayloadFingerprint;
import dev.ebaptistella.monolith.shared.models.auth.AuthProvider;
import dev.ebaptistella.monolith.shared.models.auth.AuthenticatedUser;
import dev.ebaptistella.monolith.shared.models.auth.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service("resolveAuthenticatedUserController")
@RequiredArgsConstructor
public class ResolveAuthenticatedUserController implements IdentityResolver {

    private final AccountPersistence jpa;
    private final IdentityProperties identityProperties;

    @Transactional
    public AuthenticatedUser resolve(
            AuthProvider provider, String externalSubject, String emailHint, Set<Role> rolesHint) {
        Account account = jpa.findByProviderAndSubject(provider, externalSubject)
                .orElseGet(() -> provisionAccount(provider, externalSubject, emailHint, rolesHint));

        if (account.status() == AccountStatus.DISABLED) {
            throw new IllegalStateException("Account is disabled: " + account.id());
        }

        return new AuthenticatedUser(account.id(), account.email(), account.roles(), provider);
    }

    private Account provisionAccount(
            AuthProvider provider, String externalSubject, String emailHint, Set<Role> rolesHint) {
        if (!identityProperties.jitProvisioning()) {
            throw new IllegalStateException("Account not found for provider " + provider);
        }
        if (emailHint == null || emailHint.isBlank()) {
            throw new IllegalStateException("Email is required for JIT provisioning");
        }

        Account draft = AccountRules.provisionExternalAccount(emailHint, rolesHint, provider, externalSubject);
        UUID rootKey = IdempotencyKeys.derive(
                IdempotencyKeys.namespace(), "account.jit", provider.name(), externalSubject);
        UUID entityKey = IdempotencyKeys.derive(rootKey, "account.jit");
        String fingerprint = IdempotencyPayloadFingerprint.sha256(
                emailHint + "|" + provider.name() + "|" + externalSubject);
        Account account = new Account(
                draft.id(),
                draft.email(),
                draft.status(),
                draft.roles(),
                draft.createdAt(),
                entityKey,
                rootKey,
                fingerprint);
        jpa.save(account);
        jpa.saveIdentityLink(AccountRules.externalIdentityLink(account, provider, externalSubject));
        return account;
    }
}
