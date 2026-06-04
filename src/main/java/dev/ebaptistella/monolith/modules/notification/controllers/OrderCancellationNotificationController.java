package dev.ebaptistella.monolith.modules.notification.controllers;

import dev.ebaptistella.monolith.modules.notification.diplomat.producer.NotificationEventProducer;
import dev.ebaptistella.monolith.modules.notification.logic.OrderCancellationRules;
import dev.ebaptistella.monolith.modules.notification.models.OrderCancelledNotification;
import dev.ebaptistella.monolith.modules.notification.models.OrderCancellationEmailContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderCancellationNotificationController {

    private final NotificationEventProducer producer;

    @Transactional
    public void notifyOrderCancelled(OrderCancelledNotification notification) {
        OrderCancellationEmailContent content = OrderCancellationRules.buildEmailContent(notification);
        producer.publishEmailDispatchRequested(notification.idempotencyKey(), content);
    }
}
