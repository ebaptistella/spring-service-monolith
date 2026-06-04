package dev.ebaptistella.monolith.modules.email.models;

import java.util.UUID;

public record EmailDispatch(
        UUID dispatchId,
        String to,
        String subject,
        String body
) {
}
