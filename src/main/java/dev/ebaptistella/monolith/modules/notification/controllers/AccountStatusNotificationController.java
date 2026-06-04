package dev.ebaptistella.monolith.modules.notification.controllers;

import dev.ebaptistella.monolith.modules.notification.diplomat.producer.NotificationEventProducer;
import dev.ebaptistella.monolith.modules.notification.logic.AccountStatusRules;
import dev.ebaptistella.monolith.modules.notification.models.AccountStatusChangedNotification;
import dev.ebaptistella.monolith.modules.notification.models.AccountStatusEmailContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountStatusNotificationController {

    private final NotificationEventProducer producer;

    @Transactional
    public void notifyStatusChanged(AccountStatusChangedNotification notification) {
        AccountStatusEmailContent content = AccountStatusRules.buildEmailContent(notification);
        producer.publishEmailDispatchRequested(notification.idempotencyKey(), content);
    }
}
