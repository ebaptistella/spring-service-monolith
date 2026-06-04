package dev.ebaptistella.monolith.modules.notification.diplomat;

import dev.ebaptistella.monolith.shared.resilience.NotificationPlatformConcurrencyLimiter;
import dev.ebaptistella.monolith.modules.notification.config.NotificationPlatformProperties;
import dev.ebaptistella.monolith.modules.notification.models.CustomerCreatedNotification;
import dev.ebaptistella.monolith.modules.notification.models.OrderConfirmedNotification;
import io.sentry.Sentry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.integrations.notification-platform.enabled", havingValue = "true")
public class OAuth2NotificationPlatformClient implements NotificationPlatform {

    private static final String SERVICE_PRINCIPAL = "spring-service-monolith";

    private final NotificationPlatformProperties properties;
    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final NotificationPlatformApi notificationPlatformApi;
    private final NotificationPlatformConcurrencyLimiter concurrencyLimiter;

    @Override
    @Retryable(retryFor = Exception.class, maxAttempts = 2, backoff = @Backoff(delay = 300))
    public void publishWelcome(CustomerCreatedNotification notification) {
        concurrencyLimiter.run(() -> publishWelcomeInternal(notification));
    }

    @Override
    @Retryable(retryFor = Exception.class, maxAttempts = 2, backoff = @Backoff(delay = 300))
    public void publishOrderConfirmed(OrderConfirmedNotification notification) {
        concurrencyLimiter.run(() -> publishOrderConfirmedInternal(notification));
    }

    private void publishWelcomeInternal(CustomerCreatedNotification notification) {
        if (clientRegistrationMissing()) {
            return;
        }
        String token = obtainAccessToken();
        notificationPlatformApi.publishWelcome(
                "Bearer " + token,
                new NotificationWelcomeRequest(
                        notification.customerId(), notification.email(), notification.fullName()));
        log.info("Welcome notification published to platform for customerId={}", notification.customerId());
    }

    private void publishOrderConfirmedInternal(OrderConfirmedNotification notification) {
        if (clientRegistrationMissing()) {
            return;
        }
        String token = obtainAccessToken();
        notificationPlatformApi.publishOrderConfirmed(
                "Bearer " + token,
                new NotificationOrderConfirmedRequest(
                        notification.orderId(),
                        notification.customerId(),
                        notification.customerEmail(),
                        notification.totalAmount().toPlainString()));
        log.info("Order confirmation notification published to platform for orderId={}", notification.orderId());
    }

    private String obtainAccessToken() {
        ClientRegistration registration =
                clientRegistrationRepository.findByRegistrationId(properties.registrationId());
        if (registration == null) {
            throw new IllegalStateException(
                    "OAuth2 client registration '" + properties.registrationId() + "' not found");
        }

        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(properties.registrationId())
                .principal(SERVICE_PRINCIPAL)
                .build();
        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            throw new IllegalStateException(
                    "Unable to obtain access token for registration " + properties.registrationId());
        }
        return authorizedClient.getAccessToken().getTokenValue();
    }

    private boolean clientRegistrationMissing() {
        ClientRegistration registration =
                clientRegistrationRepository.findByRegistrationId(properties.registrationId());
        if (registration == null) {
            log.warn(
                    "OAuth2 client registration '{}' not found; skipping notification platform call",
                    properties.registrationId());
            return true;
        }
        return false;
    }

    @Recover
    public void recoverWelcome(CustomerCreatedNotification notification, Exception ex) {
        log.warn("Notification platform call failed for customerId={}", notification.customerId(), ex);
        Sentry.captureException(ex);
    }

    @Recover
    public void recoverOrderConfirmed(OrderConfirmedNotification notification, Exception ex) {
        log.warn("Notification platform call failed for orderId={}", notification.orderId(), ex);
        Sentry.captureException(ex);
    }
}
