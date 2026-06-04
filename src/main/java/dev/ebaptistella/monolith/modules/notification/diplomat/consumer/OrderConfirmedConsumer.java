package dev.ebaptistella.monolith.modules.notification.diplomat.consumer;

import dev.ebaptistella.monolith.modules.notification.adapters.OrderConfirmedEventAdapter;
import dev.ebaptistella.monolith.modules.notification.controllers.OrderConfirmationNotificationController;
import dev.ebaptistella.monolith.shared.EventRoutes;
import dev.ebaptistella.monolith.shared.wire.in.events.OrderConfirmedEvent;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component("notificationOrderConfirmedConsumer")
@RequiredArgsConstructor
public class OrderConfirmedConsumer {

    private final OrderConfirmationNotificationController orderConfirmationNotificationController;

    @Observed(name = "rabbit.receive", contextualName = "order-confirmed notification")
    @RabbitListener(queues = EventRoutes.ORDER_CONFIRMED_NOTIFICATION_QUEUE)
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        orderConfirmationNotificationController.notifyOrderConfirmed(
                OrderConfirmedEventAdapter.wireToModel(event));
    }
}
