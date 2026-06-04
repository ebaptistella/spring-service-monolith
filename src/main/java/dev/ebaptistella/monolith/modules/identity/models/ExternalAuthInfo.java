package dev.ebaptistella.monolith.modules.identity.models;

public record ExternalAuthInfo(
        String provider,
        String issuer,
        String publicIssuer,
        String tokenProxyEndpoint,
        String loginProxyEndpoint,
        boolean registrationAvailable,
        String registrationUrl,
        String adminConsoleUrl,
        String hint) {
}
