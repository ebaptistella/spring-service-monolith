package dev.ebaptistella.monolith.modules.notification.diplomat.consumer;

import dev.ebaptistella.monolith.modules.notification.adapters.OrderCancelledEventAdapter;
import dev.ebaptistella.monolith.modules.notification.controllers.OrderCancellationNotificationController;
import dev.ebaptistella.monolith.shared.EventRoutes;
import dev.ebaptistella.monolith.shared.wire.in.events.OrderCancelledEvent;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component("notificationOrderCancelledConsumer")
@RequiredArgsConstructor
public class OrderCancelledConsumer {

    private final OrderCancellationNotificationController orderCancellationNotificationController;

    @Observed(name = "rabbit.receive", contextualName = "order-cancelled notification")
    @RabbitListener(queues = EventRoutes.ORDER_CANCELLED_NOTIFICATION_QUEUE)
    public void onOrderCancelled(OrderCancelledEvent event) {
        orderCancellationNotificationController.notifyOrderCancelled(
                OrderCancelledEventAdapter.wireToModel(event));
    }
}
