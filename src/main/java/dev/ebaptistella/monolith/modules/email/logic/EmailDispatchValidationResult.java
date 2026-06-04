package dev.ebaptistella.monolith.modules.email.logic;

import dev.ebaptistella.monolith.modules.email.models.EmailDispatch;

import java.util.Optional;

public record EmailDispatchValidationResult(
        Optional<EmailDispatch> dispatch,
        Optional<String> error
) {

    public static EmailDispatchValidationResult valid(EmailDispatch dispatch) {
        return new EmailDispatchValidationResult(Optional.of(dispatch), Optional.empty());
    }

    public static EmailDispatchValidationResult invalid(String error) {
        return new EmailDispatchValidationResult(Optional.empty(), Optional.of(error));
    }

    public boolean rejected() {
        return error.isPresent();
    }
}
