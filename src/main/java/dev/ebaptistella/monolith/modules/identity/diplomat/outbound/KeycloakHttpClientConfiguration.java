package dev.ebaptistella.monolith.modules.identity.diplomat.outbound;

import dev.ebaptistella.monolith.modules.identity.config.IdentityAuthProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@ConditionalOnProperty(name = "app.security.strategies.jwt-external", havingValue = "true")
public class KeycloakHttpClientConfiguration {

    @Bean
    KeycloakTokenApi keycloakTokenApi(IdentityAuthProperties authProperties, RestClient.Builder restClientBuilder) {
        RestClient restClient = restClientBuilder.baseUrl(resolveTokenUri(authProperties)).build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        return HttpServiceProxyFactory.builderFor(adapter).build().createClient(KeycloakTokenApi.class);
    }

    private static String resolveTokenUri(IdentityAuthProperties authProperties) {
        String configured = authProperties.external().tokenUri();
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        return authProperties.jwt().keycloakIssuer() + "/protocol/openid-connect/token";
    }
}
