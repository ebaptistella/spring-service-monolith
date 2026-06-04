package dev.ebaptistella.monolith.modules.identity.logic;

import dev.ebaptistella.monolith.modules.identity.models.Account;

import java.util.Optional;

public record LocalRegistrationResult(Optional<Account> account, Optional<String> error, boolean replay) {

    public static LocalRegistrationResult success(Account account) {
        return new LocalRegistrationResult(Optional.of(account), Optional.empty(), false);
    }

    public static LocalRegistrationResult replay(Account account) {
        return new LocalRegistrationResult(Optional.of(account), Optional.empty(), true);
    }

    public static LocalRegistrationResult duplicateEmail(String email) {
        return new LocalRegistrationResult(
                Optional.empty(), Optional.of("Email already registered: " + email), false);
    }

    public boolean rejected() {
        return error.isPresent();
    }
}
