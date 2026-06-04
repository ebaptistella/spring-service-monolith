package dev.ebaptistella.monolith.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtIssuerProperties(
        String localIssuer,
        String keycloakIssuer
) {
}
