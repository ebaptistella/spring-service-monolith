package dev.ebaptistella.monolith.modules.order.diplomat.consumer;

import dev.ebaptistella.monolith.modules.order.adapters.OrderEventAdapter;
import dev.ebaptistella.monolith.modules.order.controllers.HandleStockReservationFailedController;
import dev.ebaptistella.monolith.shared.EventRoutes;
import dev.ebaptistella.monolith.shared.wire.in.events.StockReservationFailedEvent;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockReservationFailedConsumer {

    private final HandleStockReservationFailedController controller;

    @Observed(name = "rabbit.receive", contextualName = "stock-reservation-failed consume")
    @RabbitListener(queues = EventRoutes.STOCK_RESERVATION_FAILED_QUEUE)
    public void onStockReservationFailed(StockReservationFailedEvent event) {
        controller.handle(OrderEventAdapter.orderIdFrom(event), OrderEventAdapter.reasonFrom(event));
    }
}
