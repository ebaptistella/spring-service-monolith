package dev.ebaptistella.monolith.modules.notification.diplomat.consumer;

import dev.ebaptistella.monolith.modules.notification.adapters.CustomerCreatedEventAdapter;
import dev.ebaptistella.monolith.modules.notification.controllers.WelcomeNotificationController;
import dev.ebaptistella.monolith.shared.EventRoutes;
import dev.ebaptistella.monolith.shared.wire.in.events.CustomerCreatedEvent;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerCreatedConsumer {

    private final WelcomeNotificationController welcomeNotificationController;

    @Observed(name = "rabbit.receive", contextualName = "customer-created consume")
    @RabbitListener(queues = EventRoutes.CUSTOMER_CREATED_QUEUE)
    public void onCustomerCreated(CustomerCreatedEvent event) {
        welcomeNotificationController.notifyAccountCreated(CustomerCreatedEventAdapter.wireToModel(event));
    }
}
