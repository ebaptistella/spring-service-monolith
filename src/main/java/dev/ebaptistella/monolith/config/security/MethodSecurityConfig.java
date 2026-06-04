package dev.ebaptistella.monolith.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@Profile("!test")
@EnableMethodSecurity
public class MethodSecurityConfig {
}
