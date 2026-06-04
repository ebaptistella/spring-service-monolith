package dev.ebaptistella.monolith.modules.inventory.diplomat.consumer;

import dev.ebaptistella.monolith.modules.inventory.adapters.StockEventAdapter;
import dev.ebaptistella.monolith.modules.inventory.controllers.CommitStockController;
import dev.ebaptistella.monolith.shared.EventRoutes;
import dev.ebaptistella.monolith.shared.wire.in.events.OrderConfirmedEvent;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component("inventoryOrderConfirmedConsumer")
@RequiredArgsConstructor
public class OrderConfirmedConsumer {

    private final CommitStockController commitStockController;

    @Observed(name = "rabbit.receive", contextualName = "order-confirmed inventory commit")
    @RabbitListener(queues = EventRoutes.ORDER_CONFIRMED_INVENTORY_QUEUE)
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        commitStockController.commit(event.idempotencyKey(), StockEventAdapter.orderIdFrom(event));
    }
}
