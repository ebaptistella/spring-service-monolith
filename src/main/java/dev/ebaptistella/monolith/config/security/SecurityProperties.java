package dev.ebaptistella.monolith.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        List<String> permitAllPatterns,
        Boolean oauth2Enabled,
        SecurityStrategies strategies
) {
    public record SecurityStrategies(
            Boolean jwtExternal,
            Boolean local,
            Boolean social
    ) {
    }

    public SecurityProperties {
        if (permitAllPatterns == null) {
            permitAllPatterns = List.of();
        }
        if (oauth2Enabled == null) {
            oauth2Enabled = false;
        }
        if (strategies == null) {
            strategies = new SecurityStrategies(false, false, false);
        }
    }

    public boolean isJwtExternalEnabled() {
        return oauth2Enabled || Boolean.TRUE.equals(strategies.jwtExternal());
    }

    public SecurityStrategies strategies() {
        return new SecurityStrategies(
                Boolean.TRUE.equals(strategies.jwtExternal()),
                Boolean.TRUE.equals(strategies.local()),
                Boolean.TRUE.equals(strategies.social()));
    }
}
