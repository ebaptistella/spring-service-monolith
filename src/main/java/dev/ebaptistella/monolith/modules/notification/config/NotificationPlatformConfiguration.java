package dev.ebaptistella.monolith.modules.notification.config;

import dev.ebaptistella.monolith.modules.notification.diplomat.NotificationPlatformApi;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@EnableConfigurationProperties(NotificationPlatformProperties.class)
public class NotificationPlatformConfiguration {

    @Bean
    NotificationPlatformApi notificationPlatformApi(
            NotificationPlatformProperties properties, RestClient.Builder restClientBuilder) {
        RestClient restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        return HttpServiceProxyFactory.builderFor(adapter).build().createClient(NotificationPlatformApi.class);
    }
}
