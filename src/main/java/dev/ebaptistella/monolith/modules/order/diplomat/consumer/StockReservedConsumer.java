package dev.ebaptistella.monolith.modules.order.diplomat.consumer;

import dev.ebaptistella.monolith.modules.order.adapters.OrderEventAdapter;
import dev.ebaptistella.monolith.modules.order.controllers.HandleStockReservedController;
import dev.ebaptistella.monolith.shared.EventRoutes;
import dev.ebaptistella.monolith.shared.wire.in.events.StockReservedEvent;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component("orderStockReservedConsumer")
@RequiredArgsConstructor
public class StockReservedConsumer {

    private final HandleStockReservedController controller;

    @Observed(name = "rabbit.receive", contextualName = "stock-reserved consume")
    @RabbitListener(queues = EventRoutes.STOCK_RESERVED_ORDER_QUEUE)
    public void onStockReserved(StockReservedEvent event) {
        controller.handle(OrderEventAdapter.orderIdFrom(event));
    }
}
