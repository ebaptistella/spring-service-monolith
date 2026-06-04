package dev.ebaptistella.monolith.modules.identity.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ebaptistella.monolith.modules.identity.diplomat.jpa.AccountPersistence;
import dev.ebaptistella.monolith.modules.identity.diplomat.producer.IdentityEventProducer;
import dev.ebaptistella.monolith.modules.identity.logic.AccountRules;
import dev.ebaptistella.monolith.modules.identity.logic.LocalRegistrationResult;
import dev.ebaptistella.monolith.modules.identity.models.Account;
import dev.ebaptistella.monolith.modules.identity.models.LocalCredential;
import dev.ebaptistella.monolith.modules.identity.models.LocalRegistrationInput;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyContext;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyKeys;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyPayloadFingerprint;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyReplay;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterLocalAccountController {

    private static final String SCOPE = "account.register";

    private final AccountPersistence jpa;
    private final PasswordEncoder passwordEncoder;
    private final IdentityEventProducer identityEventProducer;
    private final ObjectMapper objectMapper;

    @Transactional
    public LocalRegistrationResult register(LocalRegistrationInput input) {
        UUID rootKey = IdempotencyContext.requireRootKey();
        UUID entityKey = IdempotencyKeys.derive(rootKey, SCOPE);
        String fingerprint = IdempotencyPayloadFingerprint.sha256(input, objectMapper);

        return IdempotencyReplay.resolve(
                jpa.findByIdempotencyKey(entityKey),
                fingerprint,
                Account::requestFingerprint,
                LocalRegistrationResult::replay,
                () -> registerNew(input, entityKey, rootKey, fingerprint));
    }

    private LocalRegistrationResult registerNew(
            LocalRegistrationInput input, UUID entityKey, UUID rootKey, String fingerprint) {
        boolean emailExists = jpa.findByEmail(input.email()).isPresent();
        LocalRegistrationResult result = AccountRules.register(input, emailExists);
        if (result.rejected()) {
            return result;
        }

        Account draft = result.account().orElseThrow();
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
        jpa.saveIdentityLink(AccountRules.localIdentityLink(account));
        jpa.saveLocalCredential(new LocalCredential(account.id(), passwordEncoder.encode(input.password())));
        identityEventProducer.publishLocalAccountRegistered(account);
        return LocalRegistrationResult.success(account);
    }
}
