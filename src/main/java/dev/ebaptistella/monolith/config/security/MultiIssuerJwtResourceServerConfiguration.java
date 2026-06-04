package dev.ebaptistella.monolith.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Configuration
@Profile("!test")
@RequiredArgsConstructor
@ConditionalOnExpression(
        "${app.security.strategies.jwt-external:false} || ${app.security.strategies.local:false} || ${app.security.oauth2-enabled:false}")
public class MultiIssuerJwtResourceServerConfiguration {

    private final SecurityProperties securityProperties;
    private final JwtIssuerProperties jwtIssuerProperties;
    private final Environment environment;
    private final AuthenticatedUserAuthenticationConverter authenticatedUserAuthenticationConverter;

    @Bean
    JwtIssuerAuthenticationManagerResolver jwtIssuerAuthenticationManagerResolver() {
        List<String> issuers = TrustedIssuerResolver.resolve(securityProperties, jwtIssuerProperties, environment);
        Set<String> trustedIssuers = Set.copyOf(issuers);
        ConcurrentMap<String, AuthenticationManager> managers = new ConcurrentHashMap<>();

        return new JwtIssuerAuthenticationManagerResolver(issuer -> {
            if (!trustedIssuers.contains(issuer)) {
                throw new JwtException("Untrusted issuer: " + issuer);
            }
            return managers.computeIfAbsent(issuer, this::buildAuthenticationManager);
        });
    }

    private AuthenticationManager buildAuthenticationManager(String issuer) {
        JwtAuthenticationProvider provider =
                new JwtAuthenticationProvider(JwtDecoders.fromOidcIssuerLocation(issuer));
        provider.setJwtAuthenticationConverter(authenticatedUserAuthenticationConverter);
        return provider::authenticate;
    }
}
