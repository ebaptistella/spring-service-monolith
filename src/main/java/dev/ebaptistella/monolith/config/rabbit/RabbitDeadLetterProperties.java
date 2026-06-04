package dev.ebaptistella.monolith.config.rabbit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbit.dead-letter")
public record RabbitDeadLetterProperties(
        boolean enabled,
        int maxBodyLength
) {
    public RabbitDeadLetterProperties {
        if (maxBodyLength <= 0) {
            maxBodyLength = 4096;
        }
    }
}
