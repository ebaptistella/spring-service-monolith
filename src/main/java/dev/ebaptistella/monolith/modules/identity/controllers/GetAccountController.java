package dev.ebaptistella.monolith.modules.identity.controllers;

import dev.ebaptistella.monolith.modules.identity.diplomat.jpa.AccountPersistence;
import dev.ebaptistella.monolith.modules.identity.models.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetAccountController {

    private final AccountPersistence jpa;

    public Account getById(UUID id) {
        return jpa.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
    }
}
