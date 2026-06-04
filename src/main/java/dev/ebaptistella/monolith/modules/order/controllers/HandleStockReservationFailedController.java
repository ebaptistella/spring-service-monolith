package dev.ebaptistella.monolith.modules.order.controllers;

import dev.ebaptistella.monolith.modules.order.diplomat.jpa.OrderPersistence;
import dev.ebaptistella.monolith.modules.order.diplomat.outbound.CustomerGateway;
import dev.ebaptistella.monolith.modules.order.diplomat.producer.OrderEventProducer;
import dev.ebaptistella.monolith.modules.order.logic.OrderRules;
import dev.ebaptistella.monolith.modules.order.models.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HandleStockReservationFailedController {

    private final OrderPersistence persistence;
    private final CustomerGateway customerGateway;
    private final OrderEventProducer producer;

    @Transactional
    public void handle(UUID orderId, String reason) {
        Order order = persistence.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));

        if (OrderRules.shouldSkipCancelTransition(order.status())) {
            return;
        }

        String customerEmail = customerGateway.findEmail(order.customerId())
                .orElseThrow(() -> new IllegalStateException(
                        "Customer email not found for order: " + orderId));

        Order cancelled = OrderRules.toCancelled(order);
        persistence.save(cancelled);
        producer.publishOrderCancelled(cancelled, customerEmail, reason);
    }
}
