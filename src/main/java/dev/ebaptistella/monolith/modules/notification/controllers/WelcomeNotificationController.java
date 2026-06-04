package dev.ebaptistella.monolith.modules.notification.controllers;

import dev.ebaptistella.monolith.modules.notification.diplomat.NotificationPlatform;
import dev.ebaptistella.monolith.modules.notification.diplomat.producer.NotificationEventProducer;
import dev.ebaptistella.monolith.modules.notification.logic.WelcomeNotificationRules;
import dev.ebaptistella.monolith.modules.notification.models.CustomerCreatedNotification;
import dev.ebaptistella.monolith.modules.notification.models.WelcomeNotificationContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WelcomeNotificationController {

    private final NotificationPlatform notificationPlatform;
    private final NotificationEventProducer producer;

    @Transactional
    public void notifyAccountCreated(CustomerCreatedNotification notification) {
        notificationPlatform.publishWelcome(notification);

        WelcomeNotificationContent content = WelcomeNotificationRules.buildEmailContent(notification);
        log.info("Scheduling welcome email for customerId={}", notification.customerId());
        producer.publishEmailDispatchRequested(notification.idempotencyKey(), content);
    }
}
