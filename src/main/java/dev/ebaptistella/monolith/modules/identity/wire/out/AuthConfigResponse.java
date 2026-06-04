package dev.ebaptistella.monolith.modules.identity.wire.out;

import java.util.List;

public record AuthConfigResponse(
        boolean registrationEnabled,
        String registrationMode,
        String registerEndpoint,
        String registrationHint,
        boolean loginEnabled,
        List<String> strategies,
        String defaultLoginProvider,
        String loginEndpoint,
        String tokenEndpoint,
        ExternalAuthConfig external,
        String socialLoginUrl) {
}
