package dev.ebaptistella.monolith.modules.identity.controllers;

import dev.ebaptistella.monolith.modules.identity.config.IdentityAuthProperties;
import dev.ebaptistella.monolith.modules.identity.diplomat.outbound.KeycloakTokenClient;
import dev.ebaptistella.monolith.shared.exception.AuthOperationException;
import dev.ebaptistella.monolith.modules.identity.models.LocalLoginResult;
import dev.ebaptistella.monolith.modules.identity.models.LocalRegistrationInput;
import dev.ebaptistella.monolith.modules.identity.models.LoginCommand;
import dev.ebaptistella.monolith.modules.identity.models.TokenExchangeCommand;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AuthEntrypointControllerTest {

    private static final IdentityAuthProperties.JwtSettings JWT =
            new IdentityAuthProperties.JwtSettings("http://localhost:8080", "http://localhost:8091/realms/monolith");

    private static final IdentityAuthProperties.ExternalProvider EXTERNAL =
            new IdentityAuthProperties.ExternalProvider(
                    "keycloak", null, "monolith-api", "monolith-api-secret", false, null, "http://localhost:8091");

    @Mock
    private RegisterLocalAccountController registerLocalAccountController;

    @Mock
    private ObjectProvider<AuthenticateLocalAccountController> authenticateLocalAccountController;

    @Mock
    private ObjectProvider<KeycloakTokenClient> keycloakTokenClient;

    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Test
    void register_whenLocalDisabled_returnsNotImplemented() {
        AuthEntrypointController controller = controllerFor(
                new IdentityAuthProperties.StrategyFlags(true, false, false), true);

        assertThatThrownBy(() -> controller.register(new LocalRegistrationInput("a@b.com", "password123")))
                .isInstanceOf(AuthOperationException.class)
                .extracting(ex -> ((AuthOperationException) ex).statusCode())
                .isEqualTo(HttpStatus.NOT_IMPLEMENTED.value());
    }

    @Test
    void login_whenExternalEnabled_proxiesToKeycloakClient() {
        AuthEntrypointController controller = controllerFor(
                new IdentityAuthProperties.StrategyFlags(true, false, false), true);
        KeycloakTokenClient client = mock(KeycloakTokenClient.class);
        when(keycloakTokenClient.getIfAvailable()).thenReturn(client);
        when(client.loginWithPassword("demo-user@example.com", "demo-password"))
                .thenReturn(new LocalLoginResult("token", UUID.randomUUID(), "demo-user@example.com"));

        var response = controller.login(new LoginCommand("demo-user@example.com", "demo-password", null));

        assertThat(response.accessToken()).isEqualTo("token");
        verify(client).loginWithPassword("demo-user@example.com", "demo-password");
    }

    @Test
    void token_whenExternalEnabled_proxiesFormBody() {
        AuthEntrypointController controller = controllerFor(
                new IdentityAuthProperties.StrategyFlags(true, false, false), true);
        KeycloakTokenClient client = mock(KeycloakTokenClient.class);
        when(keycloakTokenClient.getObject()).thenReturn(client);
        when(client.exchangeToken(any())).thenReturn(ResponseEntity.ok("{\"access_token\":\"abc\"}"));

        var response = controller.exchangeToken(new TokenExchangeCommand(null, null, null));

        assertThat(response.body()).contains("access_token");
        verify(client).exchangeToken(any());
    }

    @Test
    void config_exposesEnabledStrategies() {
        AuthEntrypointController controller = controllerFor(
                new IdentityAuthProperties.StrategyFlags(true, true, false), true);

        var config = controller.config();

        assertThat(config.registrationEnabled()).isTrue();
        assertThat(config.registrationMode()).isEqualTo("local");
        assertThat(config.registerEndpoint()).isEqualTo("/api/v1/auth/register");
        assertThat(config.strategies()).containsExactlyInAnyOrder("local", "jwt-external");
        assertThat(config.defaultLoginProvider()).isEqualTo("local");
        assertThat(config.external()).isNotNull();
        assertThat(config.external().provider()).isEqualTo("keycloak");
        assertThat(config.tokenEndpoint()).isEqualTo("/api/v1/auth/token");
    }

    private AuthEntrypointController controllerFor(
            IdentityAuthProperties.StrategyFlags strategies, boolean oauth2Enabled) {
        IdentityAuthProperties properties =
                new IdentityAuthProperties(strategies, oauth2Enabled, JWT, EXTERNAL);
        return new AuthEntrypointController(
                properties,
                registerLocalAccountController,
                authenticateLocalAccountController,
                keycloakTokenClient,
                objectMapper);
    }
}
