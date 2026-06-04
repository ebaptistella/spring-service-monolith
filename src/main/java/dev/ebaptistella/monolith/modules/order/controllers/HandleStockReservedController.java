package dev.ebaptistella.monolith.modules.order.controllers;

import dev.ebaptistella.monolith.modules.order.diplomat.jpa.OrderPersistence;
import dev.ebaptistella.monolith.modules.order.logic.OrderRules;
import dev.ebaptistella.monolith.modules.order.models.Order;
import dev.ebaptistella.monolith.modules.order.models.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HandleStockReservedController {

    private final OrderPersistence persistence;

    @Transactional
    public void handle(UUID orderId) {
        Order order = persistence.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));

        if (OrderRules.shouldSkipMarkAwaitingPayment(order.status())) {
            return;
        }

        Instant now = Instant.now();
        Order updated = new Order(
                order.id(),
                order.customerId(),
                OrderStatus.AWAITING_PAYMENT,
                order.totalAmount(),
                order.currency(),
                order.lines(),
                order.createdAt(),
                now,
                order.idempotencyKey(),
                order.rootIdempotencyKey(),
                order.requestFingerprint());
        persistence.save(updated);
    }
}
