package dev.ebaptistella.monolith.modules.notification.adapters;

import dev.ebaptistella.monolith.modules.notification.models.AccountStatusChangedNotification;
import dev.ebaptistella.monolith.shared.wire.in.events.AccountStatusChangedEvent;

public final class AccountStatusChangedEventAdapter {

    private AccountStatusChangedEventAdapter() {
    }

    public static AccountStatusChangedNotification wireToModel(AccountStatusChangedEvent wire) {
        return new AccountStatusChangedNotification(
                wire.idempotencyKey(), wire.accountId(), wire.email(), wire.status());
    }
}
