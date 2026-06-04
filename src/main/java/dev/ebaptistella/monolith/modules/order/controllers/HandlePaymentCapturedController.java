package dev.ebaptistella.monolith.modules.order.controllers;

import dev.ebaptistella.monolith.modules.order.diplomat.jpa.OrderPersistence;
import dev.ebaptistella.monolith.modules.order.diplomat.outbound.CustomerGateway;
import dev.ebaptistella.monolith.modules.order.diplomat.producer.OrderEventProducer;
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
public class HandlePaymentCapturedController {

    private final OrderPersistence persistence;
    private final CustomerGateway customerGateway;
    private final OrderEventProducer producer;

    @Transactional
    public void handle(UUID orderId) {
        Order order = persistence.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));

        if (OrderRules.shouldSkipConfirmTransition(order.status())) {
            return;
        }

        String customerEmail = customerGateway.findEmail(order.customerId())
                .orElseThrow(() -> new IllegalStateException(
                        "Customer email not found for order: " + orderId));

        Instant now = Instant.now();
        Order confirmed = new Order(
                order.id(),
                order.customerId(),
                OrderStatus.CONFIRMED,
                order.totalAmount(),
                order.currency(),
                order.lines(),
                order.createdAt(),
                now,
                order.idempotencyKey(),
                order.rootIdempotencyKey(),
                order.requestFingerprint());
        Order saved = persistence.save(confirmed);
        producer.publishOrderConfirmed(saved, customerEmail);
    }
}
