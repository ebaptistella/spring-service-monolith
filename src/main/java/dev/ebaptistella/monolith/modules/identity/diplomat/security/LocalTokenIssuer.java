package dev.ebaptistella.monolith.modules.identity.diplomat.security;

import dev.ebaptistella.monolith.modules.identity.config.AuthorizationServerProperties;
import dev.ebaptistella.monolith.modules.identity.models.Account;
import dev.ebaptistella.monolith.shared.models.auth.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.security.strategies.local", havingValue = "true")
public class LocalTokenIssuer {

    private final JwtEncoder jwtEncoder;
    private final AuthorizationServerProperties properties;
    private final AuthorizationServerSettings authorizationServerSettings;

    public String issueAccessToken(Account account) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(authorizationServerSettings.getIssuer())
                .subject(account.id().toString())
                .audience(java.util.List.of(properties.clientId()))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .claim("account_id", account.id().toString())
                .claim("email", account.email())
                .claim(
                        "roles",
                        account.roles().stream().map(Role::name).toList())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
