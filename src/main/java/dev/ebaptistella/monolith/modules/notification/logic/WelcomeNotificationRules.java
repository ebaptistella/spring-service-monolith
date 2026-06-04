package dev.ebaptistella.monolith.modules.notification.logic;

import dev.ebaptistella.monolith.modules.notification.models.CustomerCreatedNotification;
import dev.ebaptistella.monolith.modules.notification.models.WelcomeNotificationContent;

public final class WelcomeNotificationRules {

    private WelcomeNotificationRules() {
    }

    public static WelcomeNotificationContent buildEmailContent(CustomerCreatedNotification notification) {
        return new WelcomeNotificationContent(
                notification.email(),
                "Your account was created",
                """
                        Hello %s,

                        Your account was successfully created. You can now sign in using this email address.

                        Welcome!
                        """.formatted(notification.fullName()).strip());
    }
}
