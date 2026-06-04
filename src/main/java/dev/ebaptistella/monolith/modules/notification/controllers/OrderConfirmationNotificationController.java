package dev.ebaptistella.monolith.modules.notification.controllers;

import dev.ebaptistella.monolith.modules.notification.diplomat.NotificationPlatform;
import dev.ebaptistella.monolith.modules.notification.diplomat.producer.NotificationEventProducer;
import dev.ebaptistella.monolith.modules.notification.logic.OrderConfirmationRules;
import dev.ebaptistella.monolith.modules.notification.models.OrderConfirmationEmailContent;
import dev.ebaptistella.monolith.modules.notification.models.OrderConfirmedNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderConfirmationNotificationController {

    private final NotificationPlatform notificationPlatform;
    private final NotificationEventProducer producer;

    @Transactional
    public void notifyOrderConfirmed(OrderConfirmedNotification notification) {
        notificationPlatform.publishOrderConfirmed(notification);

        OrderConfirmationEmailContent content = OrderConfirmationRules.buildEmailContent(notification);
        producer.publishEmailDispatchRequested(notification.idempotencyKey(), content);
    }
}
