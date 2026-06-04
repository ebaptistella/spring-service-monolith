package dev.ebaptistella.monolith.modules.identity.adapters;

import dev.ebaptistella.monolith.modules.identity.models.AuthConfig;
import dev.ebaptistella.monolith.modules.identity.models.ExternalAuthInfo;
import dev.ebaptistella.monolith.modules.identity.models.LocalLoginResult;
import dev.ebaptistella.monolith.modules.identity.models.LoginCommand;
import dev.ebaptistella.monolith.modules.identity.models.TokenExchangeCommand;
import dev.ebaptistella.monolith.modules.identity.wire.in.LoginRequest;
import dev.ebaptistella.monolith.modules.identity.wire.out.AuthConfigResponse;
import dev.ebaptistella.monolith.modules.identity.wire.out.ExternalAuthConfig;
import dev.ebaptistella.monolith.modules.identity.wire.out.LoginResponse;
import org.springframework.util.MultiValueMap;

import java.util.List;

public final class AuthAdapter {

    private AuthAdapter() {
    }

    public static LoginCommand wireToLoginCommand(LoginRequest request) {
        return new LoginCommand(request.email(), request.password(), request.provider());
    }

    public static LoginResponse modelToLoginResponse(LocalLoginResult result) {
        return new LoginResponse(result.accessToken(), result.accountId(), result.email());
    }

    public static AuthConfigResponse modelToWireOut(AuthConfig config) {
        return new AuthConfigResponse(
                config.registrationEnabled(),
                config.registrationMode(),
                config.registerEndpoint(),
                config.registrationHint(),
                config.loginEnabled(),
                List.copyOf(config.strategies()),
                config.defaultLoginProvider(),
                config.loginEndpoint(),
                config.tokenEndpoint(),
                config.external() == null ? null : modelToWireOut(config.external()),
                config.socialLoginUrl());
    }

    private static ExternalAuthConfig modelToWireOut(ExternalAuthInfo external) {
        return new ExternalAuthConfig(
                external.provider(),
                external.issuer(),
                external.publicIssuer(),
                external.tokenProxyEndpoint(),
                external.loginProxyEndpoint(),
                external.registrationAvailable(),
                external.registrationUrl(),
                external.adminConsoleUrl(),
                external.hint());
    }

    public static TokenExchangeCommand wireToTokenExchangeCommand(MultiValueMap<String, String> form) {
        return new TokenExchangeCommand(first(form, "grant_type"), first(form, "username"), first(form, "password"));
    }

    private static String first(MultiValueMap<String, String> form, String key) {
        List<String> values = form.get(key);
        return values == null || values.isEmpty() ? null : values.getFirst();
    }

    public static LocalLoginResult loginResponseToModel(LoginResponse response) {
        return new LocalLoginResult(response.accessToken(), response.accountId(), response.email());
    }

    public static ExternalAuthInfo externalAuthInfo(
            String provider,
            String issuer,
            String publicIssuer,
            String tokenProxyEndpoint,
            String loginProxyEndpoint,
            boolean registrationAvailable,
            String registrationUrl,
            String adminConsoleUrl,
            String hint) {
        return new ExternalAuthInfo(
                provider,
                issuer,
                publicIssuer,
                tokenProxyEndpoint,
                loginProxyEndpoint,
                registrationAvailable,
                registrationUrl,
                adminConsoleUrl,
                hint);
    }
}
