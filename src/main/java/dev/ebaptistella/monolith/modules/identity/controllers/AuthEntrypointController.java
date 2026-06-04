package dev.ebaptistella.monolith.modules.identity.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ebaptistella.monolith.modules.identity.adapters.AuthAdapter;
import dev.ebaptistella.monolith.modules.identity.config.IdentityAuthProperties;
import dev.ebaptistella.monolith.modules.identity.diplomat.outbound.ExternalTokenException;
import dev.ebaptistella.monolith.modules.identity.diplomat.outbound.KeycloakTokenClient;
import dev.ebaptistella.monolith.modules.identity.logic.LocalRegistrationResult;
import dev.ebaptistella.monolith.modules.identity.models.AuthConfig;
import dev.ebaptistella.monolith.modules.identity.models.AuthProviderChoice;
import dev.ebaptistella.monolith.modules.identity.models.ExternalAuthInfo;
import dev.ebaptistella.monolith.modules.identity.models.LocalLoginInput;
import dev.ebaptistella.monolith.modules.identity.models.LocalLoginResult;
import dev.ebaptistella.monolith.modules.identity.models.LocalRegistrationInput;
import dev.ebaptistella.monolith.modules.identity.models.LoginCommand;
import dev.ebaptistella.monolith.modules.identity.models.TokenExchangeCommand;
import dev.ebaptistella.monolith.modules.identity.models.TokenExchangeResult;
import dev.ebaptistella.monolith.shared.exception.AuthOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@ConditionalOnExpression(
        "${app.security.strategies.jwt-external:false} == true || "
                + "${app.security.strategies.local:false} == true || "
                + "${app.security.strategies.social:false} == true || "
                + "${app.security.oauth2-enabled:false} == true")
public class AuthEntrypointController {

    private final IdentityAuthProperties authProperties;
    private final RegisterLocalAccountController registerLocalAccountController;
    private final ObjectProvider<AuthenticateLocalAccountController> authenticateLocalAccountController;
    private final ObjectProvider<KeycloakTokenClient> keycloakTokenClient;
    private final ObjectMapper objectMapper;

    public AuthConfig config() {
        List<String> strategies = new ArrayList<>();
        if (authProperties.strategies().local()) {
            strategies.add("local");
        }
        if (authProperties.isJwtExternalEnabled()) {
            strategies.add("jwt-external");
        }
        if (authProperties.strategies().social()) {
            strategies.add("social");
        }

        boolean localRegistration = authProperties.strategies().local();
        IdentityAuthProperties.ExternalProvider externalSettings = authProperties.external();
        boolean externalRegistration = authProperties.isJwtExternalEnabled()
                && Boolean.TRUE.equals(externalSettings.registrationAvailable());
        String registrationMode = switch (registrationMode(localRegistration, externalRegistration)) {
            case LOCAL -> "local";
            case EXTERNAL -> "external";
            case NONE -> "none";
        };
        String defaultLoginProvider = authProperties.strategies().local() ? "local" : "external";

        return new AuthConfig(
                localRegistration,
                registrationMode,
                localRegistration ? "/api/v1/auth/register" : null,
                registrationHint(localRegistration, externalRegistration, externalSettings),
                !strategies.isEmpty(),
                List.copyOf(strategies),
                defaultLoginProvider,
                "/api/v1/auth/login",
                "/api/v1/auth/token",
                authProperties.isJwtExternalEnabled() ? externalAuthInfo(externalSettings) : null,
                authProperties.strategies().social() ? "/oauth2/authorization/google" : null);
    }

    private enum RegistrationMode {
        LOCAL,
        EXTERNAL,
        NONE
    }

    private static RegistrationMode registrationMode(boolean localRegistration, boolean externalRegistration) {
        if (localRegistration) {
            return RegistrationMode.LOCAL;
        }
        if (externalRegistration) {
            return RegistrationMode.EXTERNAL;
        }
        return RegistrationMode.NONE;
    }

    private ExternalAuthInfo externalAuthInfo(IdentityAuthProperties.ExternalProvider external) {
        IdentityAuthProperties.JwtSettings jwt = authProperties.jwt();
        return AuthAdapter.externalAuthInfo(
                external.provider(),
                jwt.keycloakIssuer(),
                jwt.keycloakIssuer(),
                "/api/v1/auth/token",
                "/api/v1/auth/login",
                Boolean.TRUE.equals(external.registrationAvailable()),
                external.registrationUrl(),
                external.adminConsoleUrl(),
                externalRegistrationHint(external));
    }

    private static String externalRegistrationHint(IdentityAuthProperties.ExternalProvider external) {
        if (Boolean.TRUE.equals(external.registrationAvailable())
                && external.registrationUrl() != null
                && !external.registrationUrl().isBlank()) {
            return """
                    Self-registration is available at %s
                    """.formatted(external.registrationUrl()).trim();
        }
        return """
                Use demo-user/demo-password for the POC, create users in Keycloak admin (%s), \
                or enable LOCAL_AUTH_ENABLED for POST /api/v1/auth/register
                """.formatted(external.adminConsoleUrl()).trim();
    }

    private static String registrationHint(
            boolean localRegistration,
            boolean externalRegistration,
            IdentityAuthProperties.ExternalProvider external) {
        return switch (registrationMode(localRegistration, externalRegistration)) {
            case LOCAL -> "POST /api/v1/auth/register with email and password (min 8 chars).";
            case EXTERNAL -> "Register via external provider at %s".formatted(external.registrationUrl());
            case NONE -> externalRegistrationHint(external);
        };
    }

    public LocalRegistrationResult register(LocalRegistrationInput input) {
        if (!authProperties.strategies().local()) {
            throw new AuthOperationException(
                    HttpStatus.NOT_IMPLEMENTED.value(),
                    "Registration is managed by the external identity provider");
        }
        return registerLocalAccountController.register(input);
    }

    public LocalLoginResult login(LoginCommand command) {
        AuthProviderChoice provider = resolveProvider(command.provider());
        return switch (provider) {
            case LOCAL -> authenticateLocally(command.email(), command.password());
            case EXTERNAL -> authenticateExternally(command.email(), command.password());
        };
    }

    public TokenExchangeResult exchangeToken(TokenExchangeCommand command) {
        return switch (tokenExchangeMode()) {
            case EXTERNAL -> exchangeExternalToken(command);
            case LOCAL -> new TokenExchangeResult(
                    HttpStatus.OK.value(),
                    issueLocalToken(command),
                    MediaType.APPLICATION_JSON_VALUE);
            case NONE -> throw new AuthOperationException(
                    HttpStatus.NOT_FOUND.value(), "Token endpoint is not configured");
        };
    }

    private enum TokenExchangeMode {
        EXTERNAL,
        LOCAL,
        NONE
    }

    private TokenExchangeMode tokenExchangeMode() {
        if (authProperties.isJwtExternalEnabled()) {
            return TokenExchangeMode.EXTERNAL;
        }
        if (authProperties.strategies().local()) {
            return TokenExchangeMode.LOCAL;
        }
        return TokenExchangeMode.NONE;
    }

    private TokenExchangeResult exchangeExternalToken(TokenExchangeCommand command) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        if (command.grantType() != null) {
            form.add("grant_type", command.grantType());
        }
        if (command.username() != null) {
            form.add("username", command.username());
        }
        if (command.password() != null) {
            form.add("password", command.password());
        }
        ResponseEntity<String> response = keycloakTokenClient.getObject().exchangeToken(form);
        MediaType contentType = response.getHeaders().getContentType();
        return new TokenExchangeResult(
                response.getStatusCode().value(),
                response.getBody(),
                contentType != null ? contentType.toString() : MediaType.APPLICATION_JSON_VALUE);
    }

    private LocalLoginResult authenticateLocally(String email, String password) {
        AuthenticateLocalAccountController controller = authenticateLocalAccountController.getIfAvailable();
        if (controller == null) {
            throw new AuthOperationException(
                    HttpStatus.BAD_REQUEST.value(), "Local authentication is not enabled");
        }
        return controller.login(new LocalLoginInput(email, password));
    }

    private LocalLoginResult authenticateExternally(String email, String password) {
        KeycloakTokenClient client = keycloakTokenClient.getIfAvailable();
        if (client == null) {
            throw new AuthOperationException(
                    HttpStatus.BAD_REQUEST.value(), "External authentication is not enabled");
        }
        try {
            return client.loginWithPassword(email, password);
        } catch (ExternalTokenException ex) {
            throw new AuthOperationException(
                    ex.getStatusCode().value(), ex.getReason() != null ? ex.getReason() : ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            throw new AuthOperationException(
                    HttpStatus.BAD_GATEWAY.value(), "External identity provider is unavailable", ex);
        }
    }

    private String issueLocalToken(TokenExchangeCommand command) {
        if (!"password".equals(command.grantType())) {
            throw new AuthOperationException(
                    HttpStatus.BAD_REQUEST.value(), "Unsupported grant_type: %s".formatted(command.grantType()));
        }
        if (!StringUtils.hasText(command.username()) || !StringUtils.hasText(command.password())) {
            throw new AuthOperationException(
                    HttpStatus.BAD_REQUEST.value(), "username and password are required");
        }

        AuthenticateLocalAccountController controller = authenticateLocalAccountController.getIfAvailable();
        if (controller == null) {
            throw new AuthOperationException(
                    HttpStatus.BAD_REQUEST.value(), "Local authentication is not enabled");
        }

        try {
            LocalLoginResult login = controller.login(new LocalLoginInput(command.username(), command.password()));
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("access_token", login.accessToken());
            payload.put("token_type", "Bearer");
            payload.put("expires_in", 3600);
            return objectMapper.writeValueAsString(payload);
        } catch (BadCredentialsException ex) {
            throw new AuthOperationException(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        } catch (AuthOperationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AuthOperationException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unable to issue local token", ex);
        }
    }

    private AuthProviderChoice resolveProvider(String provider) {
        if (StringUtils.hasText(provider)) {
            return switch (provider.trim().toLowerCase()) {
                case "local" -> AuthProviderChoice.LOCAL;
                case "external", "jwt-external", "keycloak" -> AuthProviderChoice.EXTERNAL;
                default -> throw new AuthOperationException(
                        HttpStatus.BAD_REQUEST.value(), "Unknown auth provider: %s".formatted(provider));
            };
        }
        return Stream.of(
                        authProperties.strategies().local() ? AuthProviderChoice.LOCAL : null,
                        authProperties.isJwtExternalEnabled() ? AuthProviderChoice.EXTERNAL : null)
                .filter(choice -> choice != null)
                .findFirst()
                .orElseThrow(() -> new AuthOperationException(
                        HttpStatus.NOT_FOUND.value(), "Authentication is not configured"));
    }
}
