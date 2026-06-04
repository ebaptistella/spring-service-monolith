package dev.ebaptistella.monolith.modules.inventory.diplomat.consumer;

import dev.ebaptistella.monolith.modules.inventory.adapters.StockEventAdapter;
import dev.ebaptistella.monolith.modules.inventory.controllers.ReleaseStockController;
import dev.ebaptistella.monolith.shared.EventRoutes;
import dev.ebaptistella.monolith.shared.wire.in.events.OrderCancelledEvent;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component("inventoryOrderCancelledConsumer")
@RequiredArgsConstructor
public class OrderCancelledConsumer {

    private final ReleaseStockController releaseStockController;

    @Observed(name = "rabbit.receive", contextualName = "order-cancelled inventory release")
    @RabbitListener(queues = EventRoutes.ORDER_CANCELLED_INVENTORY_QUEUE)
    public void onOrderCancelled(OrderCancelledEvent event) {
        releaseStockController.release(event.idempotencyKey(), StockEventAdapter.orderIdFrom(event));
    }
}
