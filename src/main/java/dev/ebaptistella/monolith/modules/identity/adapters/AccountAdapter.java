package dev.ebaptistella.monolith.modules.identity.adapters;

import dev.ebaptistella.monolith.modules.identity.models.Account;
import dev.ebaptistella.monolith.modules.identity.models.LocalLoginInput;
import dev.ebaptistella.monolith.modules.identity.models.LocalRegistrationInput;
import dev.ebaptistella.monolith.modules.identity.wire.in.LoginRequest;
import dev.ebaptistella.monolith.modules.identity.wire.in.RegisterRequest;
import dev.ebaptistella.monolith.modules.identity.wire.out.AccountResponse;
import dev.ebaptistella.monolith.modules.identity.wire.out.LoginResponse;
import dev.ebaptistella.monolith.modules.identity.wire.out.RegisterResponse;

public final class AccountAdapter {

    private AccountAdapter() {
    }

    public static LocalRegistrationInput wireInToRegistrationInput(RegisterRequest request) {
        return new LocalRegistrationInput(request.email(), request.password());
    }

    public static LocalLoginInput wireInToLoginInput(LoginRequest request) {
        return new LocalLoginInput(request.email(), request.password());
    }

    public static RegisterResponse modelToRegisterResponse(Account account) {
        return new RegisterResponse(account.id(), account.email());
    }

    public static LoginResponse toLoginResponse(String accessToken, Account account) {
        return new LoginResponse(accessToken, account.id(), account.email());
    }

    public static AccountResponse modelToWireOut(Account account) {
        return AccountResponse.from(account);
    }
}
