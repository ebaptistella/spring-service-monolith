package dev.ebaptistella.monolith.config.security;

import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class TrustedIssuerResolver {

    private TrustedIssuerResolver() {
    }

    static List<String> resolve(
            SecurityProperties securityProperties, JwtIssuerProperties jwtIssuerProperties, Environment environment) {
        Set<String> issuers = new LinkedHashSet<>();
        if (securityProperties.strategies().local() && StringUtils.hasText(jwtIssuerProperties.localIssuer())) {
            issuers.add(jwtIssuerProperties.localIssuer());
        }
        if (securityProperties.isJwtExternalEnabled()) {
            String resourceServerIssuer =
                    environment.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri");
            if (StringUtils.hasText(resourceServerIssuer)) {
                issuers.add(resourceServerIssuer);
            }
            if (StringUtils.hasText(jwtIssuerProperties.keycloakIssuer())) {
                issuers.add(jwtIssuerProperties.keycloakIssuer());
            }
        }
        return List.copyOf(issuers);
    }
}
