package dev.ebaptistella.monolith.modules.identity.controllers;

import dev.ebaptistella.monolith.modules.identity.diplomat.jpa.AccountPersistence;
import dev.ebaptistella.monolith.modules.identity.diplomat.security.LocalTokenIssuer;
import dev.ebaptistella.monolith.modules.identity.models.Account;
import dev.ebaptistella.monolith.modules.identity.models.LocalCredential;
import dev.ebaptistella.monolith.modules.identity.models.LocalLoginInput;
import dev.ebaptistella.monolith.modules.identity.models.LocalLoginResult;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.security.strategies.local", havingValue = "true")
public class AuthenticateLocalAccountController {

    private final AccountPersistence jpa;
    private final PasswordEncoder passwordEncoder;
    private final LocalTokenIssuer localTokenIssuer;

    public LocalLoginResult login(LocalLoginInput input) {
        Account account = jpa.findByEmail(input.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        LocalCredential credential = jpa.findLocalCredentialByEmail(input.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(input.password(), credential.passwordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        String accessToken = localTokenIssuer.issueAccessToken(account);
        return new LocalLoginResult(accessToken, account.id(), account.email());
    }
}
