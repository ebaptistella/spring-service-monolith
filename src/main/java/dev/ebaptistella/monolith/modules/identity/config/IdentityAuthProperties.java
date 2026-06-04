package dev.ebaptistella.monolith.modules.identity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record IdentityAuthProperties(
        StrategyFlags strategies, Boolean oauth2Enabled, JwtSettings jwt, ExternalProvider external) {

    public record StrategyFlags(Boolean jwtExternal, Boolean local, Boolean social) {
    }

    public record JwtSettings(String localIssuer, String keycloakIssuer) {
    }

    public record ExternalProvider(
            String provider,
            String tokenUri,
            String clientId,
            String clientSecret,
            Boolean registrationAvailable,
            String registrationUrl,
            String adminConsoleUrl) {
        public ExternalProvider {
            if (provider == null || provider.isBlank()) {
                provider = "keycloak";
            }
            if (clientId == null || clientId.isBlank()) {
                clientId = "monolith-api";
            }
            if (clientSecret == null || clientSecret.isBlank()) {
                clientSecret = "monolith-api-secret";
            }
            if (registrationAvailable == null) {
                registrationAvailable = false;
            }
            if (adminConsoleUrl == null || adminConsoleUrl.isBlank()) {
                adminConsoleUrl = "http://localhost:8091";
            }
        }
    }

    public IdentityAuthProperties {
        if (strategies == null) {
            strategies = new StrategyFlags(false, false, false);
        }
        if (oauth2Enabled == null) {
            oauth2Enabled = false;
        }
        if (jwt == null) {
            jwt = new JwtSettings("http://localhost:8080", "http://localhost:8091/realms/monolith");
        }
        if (external == null) {
            external = new ExternalProvider(null, null, null, null, false, null, null);
        }
    }

    public boolean isJwtExternalEnabled() {
        return oauth2Enabled || Boolean.TRUE.equals(strategies.jwtExternal());
    }

    public StrategyFlags strategies() {
        return new StrategyFlags(
                Boolean.TRUE.equals(strategies.jwtExternal()),
                Boolean.TRUE.equals(strategies.local()),
                Boolean.TRUE.equals(strategies.social()));
    }
}
