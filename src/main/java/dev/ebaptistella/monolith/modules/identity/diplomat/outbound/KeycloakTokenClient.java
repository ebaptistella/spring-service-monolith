package dev.ebaptistella.monolith.modules.identity.diplomat.outbound;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ebaptistella.monolith.modules.identity.config.IdentityAuthProperties;
import dev.ebaptistella.monolith.modules.identity.models.LocalLoginResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Base64;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.security.strategies.jwt-external", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
public class KeycloakTokenClient {

    private final KeycloakTokenApi keycloakTokenApi;
    private final IdentityAuthProperties authProperties;
    private final ObjectMapper objectMapper;

    public LocalLoginResult loginWithPassword(String email, String password) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("username", email);
        form.add("password", password);
        ResponseEntity<String> response = exchangeToken(form);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ExternalTokenException(response.getStatusCode().value(), response.getBody());
        }
        return toLoginResult(response.getBody());
    }

    public ResponseEntity<String> exchangeToken(MultiValueMap<String, String> form) {
        MultiValueMap<String, String> enriched = enrichClientCredentials(new LinkedMultiValueMap<>(form));
        return keycloakTokenApi.exchangeToken(enriched);
    }

    private MultiValueMap<String, String> enrichClientCredentials(MultiValueMap<String, String> form) {
        IdentityAuthProperties.ExternalProvider external = authProperties.external();
        if (!form.containsKey("client_id")) {
            form.add("client_id", external.clientId());
        }
        if (!form.containsKey("client_secret")) {
            form.add("client_secret", external.clientSecret());
        }
        return form;
    }

    private LocalLoginResult toLoginResult(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            String accessToken = root.path("access_token").asText(null);
            if (accessToken == null || accessToken.isBlank()) {
                throw new IllegalStateException("External token response missing access_token");
            }
            UUID accountId = extractAccountId(accessToken);
            String email = extractEmail(accessToken);
            return new LocalLoginResult(accessToken, accountId, email);
        } catch (ExternalTokenException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to parse external token response", ex);
        }
    }

    private static UUID extractAccountId(String accessToken) {
        try {
            return UUID.fromString(readClaim(accessToken, "sub"));
        } catch (Exception ex) {
            return null;
        }
    }

    private static String extractEmail(String accessToken) {
        try {
            String email = readClaim(accessToken, "email");
            if (email != null && !email.isBlank()) {
                return email;
            }
            return readClaim(accessToken, "preferred_username");
        } catch (Exception ex) {
            return null;
        }
    }

    private static String readClaim(String accessToken, String claim) throws Exception {
        String[] parts = accessToken.split("\\.");
        if (parts.length < 2) {
            return null;
        }
        byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
        JsonNode root = new ObjectMapper().readTree(payload);
        JsonNode value = root.get(claim);
        return value != null && !value.isNull() ? value.asText() : null;
    }
}
