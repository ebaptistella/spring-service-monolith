package dev.ebaptistella.monolith.modules.notification.models;

public record WelcomeNotificationContent(
        String to,
        String subject,
        String body
) {
}
