package dev.ebaptistella.monolith.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("!test")
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityProperties securityProperties;
    private final AuthenticatedUserAuthenticationConverter authenticatedUserAuthenticationConverter;
    private final JwtIssuerAuthenticationManagerResolver jwtIssuerAuthenticationManagerResolver;

    @Bean
    @Order(2)
    @ConditionalOnProperty(name = "app.security.strategies.social", havingValue = "true")
    SecurityFilterChain socialLoginFilterChain(
            HttpSecurity http, SocialOAuth2AuthenticationSuccessHandler successHandler) throws Exception {
        return http.securityMatcher("/login/**", "/oauth2/authorization/**", "/oauth2/code/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2.successHandler(successHandler))
                .build();
    }

    @Bean
    @Order(1)
    @ConditionalOnExpression(
            "${app.security.strategies.jwt-external:false} || ${app.security.strategies.local:false} || ${app.security.oauth2-enabled:false}")
    SecurityFilterChain jwtResourceServerFilterChain(HttpSecurity http) throws Exception {
        return resourceServerChain(http);
    }

    @Bean
    @Order(99)
    SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
        String[] permitAll = securityProperties.permitAllPatterns().toArray(String[]::new);
        boolean authRequired = securityProperties.isJwtExternalEnabled()
                || securityProperties.strategies().local();

        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(permitAll).permitAll();
                    if (authRequired) {
                        auth.anyRequest().authenticated();
                    } else {
                        auth.anyRequest().permitAll();
                    }
                })
                .build();
    }

    private SecurityFilterChain resourceServerChain(HttpSecurity http) throws Exception {
        String[] permitAll = securityProperties.permitAllPatterns().toArray(String[]::new);

        return http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(permitAll).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.authenticationManagerResolver(
                        jwtIssuerAuthenticationManagerResolver))
                .build();
    }
}
