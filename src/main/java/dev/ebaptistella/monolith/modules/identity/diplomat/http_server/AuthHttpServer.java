package dev.ebaptistella.monolith.modules.identity.diplomat.http_server;

import dev.ebaptistella.monolith.modules.identity.adapters.AccountAdapter;
import dev.ebaptistella.monolith.modules.identity.adapters.AuthAdapter;
import dev.ebaptistella.monolith.modules.identity.controllers.AuthEntrypointController;
import dev.ebaptistella.monolith.modules.identity.logic.LocalRegistrationResult;
import dev.ebaptistella.monolith.shared.exception.AuthOperationException;
import dev.ebaptistella.monolith.modules.identity.models.TokenExchangeResult;
import dev.ebaptistella.monolith.modules.identity.wire.in.LoginRequest;
import dev.ebaptistella.monolith.modules.identity.wire.in.RegisterRequest;
import dev.ebaptistella.monolith.modules.identity.wire.out.AuthConfigResponse;
import dev.ebaptistella.monolith.modules.identity.wire.out.LoginResponse;
import dev.ebaptistella.monolith.modules.identity.wire.out.RegisterResponse;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Unified authentication entrypoint (local or external proxy)")
@RequiredArgsConstructor
@ConditionalOnExpression(
        "${app.security.strategies.jwt-external:false} == true || "
                + "${app.security.strategies.local:false} == true || "
                + "${app.security.strategies.social:false} == true || "
                + "${app.security.oauth2-enabled:false} == true")
public class AuthHttpServer {

    private final AuthEntrypointController authEntrypointController;

    @GetMapping("/config")
    @Observed(name = "http.server", contextualName = "auth config")
    @Operation(
            summary = "Auth capabilities",
            description = "Returns enabled strategies and stable entrypoint paths for clients")
    public AuthConfigResponse config() {
        return AuthAdapter.modelToWireOut(authEntrypointController.config());
    }

    @PostMapping("/register")
    @Observed(name = "http.server", contextualName = "auth register")
    @Operation(summary = "Register account", description = "Available when local authentication is enabled")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        LocalRegistrationResult result = authEntrypointController.register(
                AccountAdapter.wireInToRegistrationInput(request));
        if (result.rejected()) {
            throw new IllegalArgumentException(result.error().orElse("Registration rejected"));
        }
        RegisterResponse body = AccountAdapter.modelToRegisterResponse(result.account().orElseThrow());
        if (result.replay()) {
            return ResponseEntity.ok(body);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/login")
    @Observed(name = "http.server", contextualName = "auth login")
    @Operation(
            summary = "Login",
            description = "Authenticates locally or proxies to the configured external identity provider")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return AuthAdapter.modelToLoginResponse(
                authEntrypointController.login(AuthAdapter.wireToLoginCommand(request)));
    }

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Observed(name = "http.server", contextualName = "auth token")
    @Operation(
            summary = "OAuth2 token",
            description = "Proxies OAuth2 token requests to the external provider or issues local tokens")
    public ResponseEntity<String> token(@RequestParam MultiValueMap<String, String> form) {
        TokenExchangeResult result =
                authEntrypointController.exchangeToken(AuthAdapter.wireToTokenExchangeCommand(form));
        return ResponseEntity.status(result.statusCode())
                .contentType(MediaType.parseMediaType(result.contentType()))
                .body(result.body());
    }

    @RestControllerAdvice(assignableTypes = AuthHttpServer.class)
    static class AuthHttpExceptionHandler {

        @ExceptionHandler(AuthOperationException.class)
        ResponseEntity<String> handleAuthOperation(AuthOperationException ex) {
            return ResponseEntity.status(ex.statusCode()).body(ex.getMessage());
        }
    }
}
