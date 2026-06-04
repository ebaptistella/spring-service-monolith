package dev.ebaptistella.monolith.config.security;

import dev.ebaptistella.monolith.support.AuthE2eJwtSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;

@Configuration
@Profile("auth-e2e")
public class AuthE2eJwtDecoderConfiguration {

    @Bean
    @Primary
    JwtDecoder jwtDecoder() throws Exception {
        return NimbusJwtDecoder.withPublicKey(
                        (java.security.interfaces.RSAPublicKey)
                                AuthE2eJwtSupport.jwkSet()
                                        .getKeys()
                                        .getFirst()
                                        .toRSAKey()
                                        .toPublicKey())
                .build();
    }

    @Bean
    @Primary
    JwtIssuerAuthenticationManagerResolver authE2eJwtIssuerAuthenticationManagerResolver(
            JwtDecoder jwtDecoder,
            AuthenticatedUserAuthenticationConverter authenticatedUserAuthenticationConverter) {
        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(jwtDecoder);
        provider.setJwtAuthenticationConverter(authenticatedUserAuthenticationConverter);
        AuthenticationManager authenticationManager = provider::authenticate;

        return new JwtIssuerAuthenticationManagerResolver(issuer -> {
            if (!AuthE2eJwtSupport.ISSUER.equals(issuer)) {
                throw new JwtException("Untrusted issuer: " + issuer);
            }
            return authenticationManager;
        });
    }
}
