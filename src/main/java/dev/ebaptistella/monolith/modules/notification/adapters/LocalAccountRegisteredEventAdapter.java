package dev.ebaptistella.monolith.modules.notification.adapters;

import dev.ebaptistella.monolith.modules.notification.models.CustomerCreatedNotification;
import dev.ebaptistella.monolith.shared.wire.in.events.LocalAccountRegisteredEvent;

public final class LocalAccountRegisteredEventAdapter {

    private LocalAccountRegisteredEventAdapter() {
    }

    public static CustomerCreatedNotification wireToWelcomeNotification(LocalAccountRegisteredEvent wire) {
        return new CustomerCreatedNotification(
                wire.idempotencyKey(), wire.accountId(), wire.email(), displayName(wire.email()));
    }

    private static String displayName(String email) {
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }
}
