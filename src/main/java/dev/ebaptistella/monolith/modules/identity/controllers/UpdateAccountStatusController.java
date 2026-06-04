package dev.ebaptistella.monolith.modules.identity.controllers;

import dev.ebaptistella.monolith.modules.identity.diplomat.jpa.AccountPersistence;
import dev.ebaptistella.monolith.modules.identity.diplomat.producer.IdentityEventProducer;
import dev.ebaptistella.monolith.modules.identity.logic.AccountRules;
import dev.ebaptistella.monolith.modules.identity.models.Account;
import dev.ebaptistella.monolith.modules.identity.models.AccountStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateAccountStatusController {

    private final AccountPersistence jpa;
    private final IdentityEventProducer identityEventProducer;

    @Transactional
    public Account updateStatus(UUID id, AccountStatus status) {
        Account account = jpa.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
        Account updated = jpa.save(AccountRules.withStatus(account, status));
        identityEventProducer.publishAccountStatusChanged(updated);
        return updated;
    }
}
