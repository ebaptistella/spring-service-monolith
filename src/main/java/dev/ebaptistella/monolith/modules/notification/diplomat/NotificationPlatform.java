package dev.ebaptistella.monolith.modules.notification.diplomat;

import dev.ebaptistella.monolith.modules.notification.models.CustomerCreatedNotification;
import dev.ebaptistella.monolith.modules.notification.models.OrderConfirmedNotification;

public interface NotificationPlatform {

    void publishWelcome(CustomerCreatedNotification notification);

    void publishOrderConfirmed(OrderConfirmedNotification notification);
}
