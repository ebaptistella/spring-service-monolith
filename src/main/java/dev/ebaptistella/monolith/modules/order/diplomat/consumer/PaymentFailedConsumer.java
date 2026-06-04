package dev.ebaptistella.monolith.modules.order.diplomat.consumer;

import dev.ebaptistella.monolith.modules.order.adapters.OrderEventAdapter;
import dev.ebaptistella.monolith.modules.order.controllers.HandlePaymentFailedController;
import dev.ebaptistella.monolith.shared.EventRoutes;
import dev.ebaptistella.monolith.shared.wire.in.events.PaymentFailedEvent;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentFailedConsumer {

    private final HandlePaymentFailedController controller;

    @Observed(name = "rabbit.receive", contextualName = "payment-failed consume")
    @RabbitListener(queues = EventRoutes.PAYMENT_FAILED_QUEUE)
    public void onPaymentFailed(PaymentFailedEvent event) {
        controller.handle(OrderEventAdapter.orderIdFrom(event), OrderEventAdapter.reasonFrom(event));
    }
}
