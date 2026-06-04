package dev.ebaptistella.monolith.modules.notification.logic;

import dev.ebaptistella.monolith.modules.notification.models.AccountStatusChangedNotification;
import dev.ebaptistella.monolith.modules.notification.models.AccountStatusEmailContent;

public final class AccountStatusRules {

    private AccountStatusRules() {
    }

    public static AccountStatusEmailContent buildEmailContent(AccountStatusChangedNotification notification) {
        String body = """
                Hello,

                Your account status has been updated to: %s.

                If you did not expect this change, please contact support immediately.
                """.formatted(notification.status());

        return new AccountStatusEmailContent(
                notification.email(), "Your account status was updated", body);
    }
}
