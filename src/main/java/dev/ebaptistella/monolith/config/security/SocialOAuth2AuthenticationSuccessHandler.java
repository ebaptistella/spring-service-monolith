package dev.ebaptistella.monolith.config.security;

import dev.ebaptistella.monolith.shared.models.auth.AuthProvider;
import dev.ebaptistella.monolith.shared.models.auth.AuthenticatedUser;
import dev.ebaptistella.monolith.shared.contracts.IdentityResolver;
import dev.ebaptistella.monolith.shared.models.auth.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.security.strategies.social", havingValue = "true")
public class SocialOAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final IdentityResolver identityResolution;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.Authentication authentication)
            throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OidcUser oidcUser = (OidcUser) oauthToken.getPrincipal();
        AuthProvider provider = mapProvider(oauthToken.getAuthorizedClientRegistrationId());

        AuthenticatedUser user = identityResolution.resolve(
                provider,
                oidcUser.getSubject(),
                oidcUser.getEmail(),
                Set.of(Role.USER));

        var authorities = user.roles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        var enriched = new UsernamePasswordAuthenticationToken(user, oauthToken.getCredentials(), authorities);
        SecurityContextHolder.getContext().setAuthentication(enriched);
        response.sendRedirect("/swagger-ui.html");
    }

    private AuthProvider mapProvider(String registrationId) {
        if ("google".equalsIgnoreCase(registrationId)) {
            return AuthProvider.GOOGLE;
        }
        return AuthProvider.GOOGLE;
    }
}
