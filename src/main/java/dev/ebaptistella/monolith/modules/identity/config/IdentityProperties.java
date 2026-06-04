package dev.ebaptistella.monolith.modules.identity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.identity")
public record IdentityProperties(boolean jitProvisioning) {
}
