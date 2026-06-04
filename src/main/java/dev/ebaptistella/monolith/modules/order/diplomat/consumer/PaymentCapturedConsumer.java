package dev.ebaptistella.monolith.modules.order.diplomat.consumer;

import dev.ebaptistella.monolith.modules.order.adapters.OrderEventAdapter;
import dev.ebaptistella.monolith.modules.order.controllers.HandlePaymentCapturedController;
import dev.ebaptistella.monolith.shared.EventRoutes;
import dev.ebaptistella.monolith.shared.wire.in.events.PaymentCapturedEvent;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentCapturedConsumer {

    private final HandlePaymentCapturedController controller;

    @Observed(name = "rabbit.receive", contextualName = "payment-captured consume")
    @RabbitListener(queues = EventRoutes.PAYMENT_CAPTURED_QUEUE)
    public void onPaymentCaptured(PaymentCapturedEvent event) {
        controller.handle(OrderEventAdapter.orderIdFrom(event));
    }
}
