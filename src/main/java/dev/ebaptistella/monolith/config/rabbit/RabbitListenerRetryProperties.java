package dev.ebaptistella.monolith.config.rabbit;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.rabbit.listener.retry")
public record RabbitListenerRetryProperties(
        boolean enabled,
        int maxAttempts,
        Duration initialInterval,
        double multiplier,
        Duration maxInterval
) {
    public RabbitListenerRetryProperties {
        if (maxAttempts <= 0) {
            maxAttempts = 3;
        }
        if (initialInterval == null) {
            initialInterval = Duration.ofSeconds(1);
        }
        if (multiplier <= 0) {
            multiplier = 2.0;
        }
        if (maxInterval == null) {
            maxInterval = Duration.ofSeconds(10);
        }
    }
}
