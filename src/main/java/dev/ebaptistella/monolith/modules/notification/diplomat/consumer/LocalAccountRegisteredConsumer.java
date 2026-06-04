package dev.ebaptistella.monolith.modules.notification.diplomat.consumer;

import dev.ebaptistella.monolith.modules.notification.adapters.LocalAccountRegisteredEventAdapter;
import dev.ebaptistella.monolith.modules.notification.controllers.WelcomeNotificationController;
import dev.ebaptistella.monolith.shared.EventRoutes;
import dev.ebaptistella.monolith.shared.wire.in.events.LocalAccountRegisteredEvent;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocalAccountRegisteredConsumer {

    private final WelcomeNotificationController welcomeNotificationController;

    @Observed(name = "rabbit.receive", contextualName = "local-account-registered consume")
    @RabbitListener(queues = EventRoutes.LOCAL_ACCOUNT_REGISTERED_QUEUE)
    public void onLocalAccountRegistered(LocalAccountRegisteredEvent event) {
        welcomeNotificationController.notifyAccountCreated(
                LocalAccountRegisteredEventAdapter.wireToWelcomeNotification(event));
    }
}
