package dev.ebaptistella.monolith.modules.notification.diplomat;

import dev.ebaptistella.monolith.modules.notification.models.CustomerCreatedNotification;
import dev.ebaptistella.monolith.modules.notification.models.OrderConfirmedNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(
        name = "app.integrations.notification-platform.enabled",
        havingValue = "false",
        matchIfMissing = true)
public class LoggingNotificationPlatform implements NotificationPlatform {

    @Override
    public void publishWelcome(CustomerCreatedNotification notification) {
        log.debug(
                "Notification platform integration disabled; skipping welcome push for customerId={}",
                notification.customerId());
    }

    @Override
    public void publishOrderConfirmed(OrderConfirmedNotification notification) {
        log.debug(
                "Notification platform integration disabled; skipping order confirmation push for orderId={}",
                notification.orderId());
    }
}
