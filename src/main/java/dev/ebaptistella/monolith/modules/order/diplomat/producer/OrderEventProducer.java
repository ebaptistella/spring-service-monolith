package dev.ebaptistella.monolith.modules.order.diplomat.producer;

import dev.ebaptistella.monolith.modules.order.adapters.OrderEventAdapter;
import dev.ebaptistella.monolith.modules.order.models.Order;
import dev.ebaptistella.monolith.shared.wire.in.events.OrderCancelledEvent;
import dev.ebaptistella.monolith.shared.wire.in.events.OrderConfirmedEvent;
import dev.ebaptistella.monolith.shared.wire.in.events.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final ApplicationEventPublisher events;

    public void publishOrderPlaced(Order order) {
        OrderPlacedEvent wire = OrderEventAdapter.toOrderPlaced(order);
        events.publishEvent(wire);
    }

    public void publishOrderConfirmed(Order order, String customerEmail) {
        OrderConfirmedEvent wire = OrderEventAdapter.toOrderConfirmed(order, customerEmail);
        events.publishEvent(wire);
    }

    public void publishOrderCancelled(Order order, String customerEmail, String reason) {
        OrderCancelledEvent wire = OrderEventAdapter.toOrderCancelled(order, customerEmail, reason);
        events.publishEvent(wire);
    }
}
