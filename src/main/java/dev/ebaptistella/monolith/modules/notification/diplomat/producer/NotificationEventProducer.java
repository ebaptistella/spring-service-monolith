package dev.ebaptistella.monolith.modules.notification.diplomat.producer;

import dev.ebaptistella.monolith.modules.notification.adapters.EmailDispatchAdapter;
import dev.ebaptistella.monolith.modules.notification.models.AccountStatusEmailContent;
import dev.ebaptistella.monolith.modules.notification.models.OrderCancellationEmailContent;
import dev.ebaptistella.monolith.modules.notification.models.OrderConfirmationEmailContent;
import dev.ebaptistella.monolith.modules.notification.models.WelcomeNotificationContent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventProducer {

    private final ApplicationEventPublisher events;

    public void publishEmailDispatchRequested(UUID rootKey, WelcomeNotificationContent content) {
        events.publishEvent(EmailDispatchAdapter.contentToWire(rootKey, content));
    }

    public void publishEmailDispatchRequested(UUID rootKey, OrderConfirmationEmailContent content) {
        events.publishEvent(EmailDispatchAdapter.contentToWire(rootKey, content));
    }

    public void publishEmailDispatchRequested(UUID rootKey, OrderCancellationEmailContent content) {
        events.publishEvent(EmailDispatchAdapter.contentToWire(rootKey, content));
    }

    public void publishEmailDispatchRequested(UUID rootKey, AccountStatusEmailContent content) {
        events.publishEvent(EmailDispatchAdapter.contentToWire(rootKey, content));
    }
}
