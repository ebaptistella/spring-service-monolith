package dev.ebaptistella.monolith.modules.identity.models;

import java.util.List;

public record AuthConfig(
        boolean registrationEnabled,
        String registrationMode,
        String registerEndpoint,
        String registrationHint,
        boolean loginEnabled,
        List<String> strategies,
        String defaultLoginProvider,
        String loginEndpoint,
        String tokenEndpoint,
        ExternalAuthInfo external,
        String socialLoginUrl) {
}
