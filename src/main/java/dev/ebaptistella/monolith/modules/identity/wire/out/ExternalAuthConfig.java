package dev.ebaptistella.monolith.modules.identity.wire.out;

public record ExternalAuthConfig(
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
