package dev.ebaptistella.monolith.modules.email.logic;

import dev.ebaptistella.monolith.modules.email.models.EmailDispatch;

public final class EmailDispatchRules {

    private EmailDispatchRules() {
    }

    public static EmailDispatchValidationResult validate(EmailDispatch dispatch) {
        if (dispatch.to() == null || dispatch.to().isBlank()) {
            return EmailDispatchValidationResult.invalid("Recipient is required");
        }
        if (dispatch.subject() == null || dispatch.subject().isBlank()) {
            return EmailDispatchValidationResult.invalid("Subject is required");
        }
        return EmailDispatchValidationResult.valid(dispatch);
    }
}
