package dev.ebaptistella.monolith.modules.finance.diplomat.consumer;

import dev.ebaptistella.monolith.modules.finance.adapters.PaymentEventAdapter;
import dev.ebaptistella.monolith.modules.finance.controllers.CancelPaymentController;
import dev.ebaptistella.monolith.shared.EventRoutes;
import dev.ebaptistella.monolith.shared.wire.in.events.OrderCancelledEvent;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component("financeOrderCancelledConsumer")
@RequiredArgsConstructor
public class OrderCancelledConsumer {

    private final CancelPaymentController cancelPaymentController;

    @Observed(name = "rabbit.receive", contextualName = "order-cancelled consume")
    @RabbitListener(queues = EventRoutes.ORDER_CANCELLED_FINANCE_QUEUE)
    public void onOrderCancelled(OrderCancelledEvent event) {
        cancelPaymentController.cancel(PaymentEventAdapter.orderIdFrom(event));
    }
}
