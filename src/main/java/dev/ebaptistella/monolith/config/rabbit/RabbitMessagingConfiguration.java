package dev.ebaptistella.monolith.config.rabbit;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({RabbitDeadLetterProperties.class, RabbitListenerRetryProperties.class})
public class RabbitMessagingConfiguration {
}
