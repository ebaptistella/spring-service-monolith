package dev.ebaptistella.monolith.modules.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.integrations.notification-platform")
public record NotificationPlatformProperties(
        boolean enabled,
        String baseUrl,
        String registrationId
) {
    public NotificationPlatformProperties {
        if (registrationId == null || registrationId.isBlank()) {
            registrationId = "notification-platform";
        }
    }
}
