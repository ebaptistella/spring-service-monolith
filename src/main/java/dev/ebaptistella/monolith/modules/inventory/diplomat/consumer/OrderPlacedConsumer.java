package dev.ebaptistella.monolith.modules.inventory.diplomat.consumer;

import dev.ebaptistella.monolith.modules.inventory.adapters.StockEventAdapter;
import dev.ebaptistella.monolith.modules.inventory.controllers.HandleOrderPlacedController;
import dev.ebaptistella.monolith.shared.EventRoutes;
import dev.ebaptistella.monolith.shared.wire.in.events.OrderPlacedEvent;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderPlacedConsumer {

    private final HandleOrderPlacedController handleOrderPlacedController;

    @Observed(name = "rabbit.receive", contextualName = "order-placed consume")
    @RabbitListener(queues = EventRoutes.ORDER_PLACED_QUEUE)
    public void onOrderPlaced(OrderPlacedEvent event) {
        handleOrderPlacedController.handle(StockEventAdapter.wireToModel(event));
    }
}
