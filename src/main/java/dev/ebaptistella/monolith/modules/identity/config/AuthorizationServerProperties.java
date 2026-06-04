package dev.ebaptistella.monolith.modules.identity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.authorization-server")
public record AuthorizationServerProperties(
        String clientId,
        String clientSecret,
        String issuer
) {
    public AuthorizationServerProperties {
        if (clientId == null || clientId.isBlank()) {
            clientId = "monolith-local";
        }
        if (clientSecret == null || clientSecret.isBlank()) {
            clientSecret = "local-secret";
        }
        if (issuer == null || issuer.isBlank()) {
            issuer = "http://localhost:8080";
        }
    }
}
