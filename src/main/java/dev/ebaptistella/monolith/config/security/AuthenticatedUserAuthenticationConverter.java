package dev.ebaptistella.monolith.config.security;

import dev.ebaptistella.monolith.shared.models.auth.AuthProvider;
import dev.ebaptistella.monolith.shared.models.auth.AuthenticatedUser;
import dev.ebaptistella.monolith.shared.contracts.IdentityResolver;
import dev.ebaptistella.monolith.shared.models.auth.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class AuthenticatedUserAuthenticationConverter
        implements org.springframework.core.convert.converter.Converter<
                Jwt, org.springframework.security.authentication.AbstractAuthenticationToken> {

    private final IdentityResolver identityResolution;
    private final JwtIssuerProperties jwtIssuerProperties;

    @Override
    public org.springframework.security.authentication.AbstractAuthenticationToken convert(Jwt jwt) {
        AuthenticatedUser user = resolveUser(jwt);
        var authorities = user.roles().stream()
                .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        "ROLE_" + role.name()))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                user, jwt, authorities);
    }

    private AuthenticatedUser resolveUser(Jwt jwt) {
        AuthProvider provider = resolveProvider(jwt);
        String email = JwtClaimSupport.extractEmail(jwt);
        Set<Role> roles = JwtClaimSupport.extractRoles(jwt);
        String externalSubject = provider == AuthProvider.LOCAL && jwt.hasClaim("account_id")
                ? jwt.getClaimAsString("account_id")
                : jwt.getSubject();
        return identityResolution.resolve(provider, externalSubject, email, roles);
    }

    private AuthProvider resolveProvider(Jwt jwt) {
        String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "";
        if (issuer.equals(jwtIssuerProperties.localIssuer())) {
            return AuthProvider.LOCAL;
        }
        if (issuer.equals(jwtIssuerProperties.keycloakIssuer()) || issuer.contains("/realms/")) {
            return AuthProvider.KEYCLOAK;
        }
        return AuthProvider.KEYCLOAK;
    }
}
