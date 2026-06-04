package dev.ebaptistella.monolith.modules.identity.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({IdentityProperties.class, AuthorizationServerProperties.class, IdentityAuthProperties.class})
public class IdentityConfiguration {
}
