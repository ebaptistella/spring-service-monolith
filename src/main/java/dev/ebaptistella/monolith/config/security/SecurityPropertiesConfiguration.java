package dev.ebaptistella.monolith.config.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({SecurityProperties.class, JwtIssuerProperties.class})
public class SecurityPropertiesConfiguration {
}
