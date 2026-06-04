package dev.ebaptistella.monolith.modules.finance.controllers;

import dev.ebaptistella.monolith.modules.finance.diplomat.jpa.FinancePersistence;
import dev.ebaptistella.monolith.modules.finance.models.PaymentIntent;
import dev.ebaptistella.monolith.modules.finance.models.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CancelPaymentController {

    private final FinancePersistence persistence;

    @Transactional
    public void cancel(UUID orderId) {
        persistence.findIntentByOrderId(orderId).ifPresent(this::cancelIfPending);
    }

    private void cancelIfPending(PaymentIntent intent) {
        if (intent.status() != PaymentStatus.PENDING) {
            return;
        }

        persistence.saveIntent(new PaymentIntent(
                intent.id(),
                intent.orderId(),
                intent.amount(),
                intent.currency(),
                PaymentStatus.CANCELLED,
                intent.createdAt(),
                Instant.now(),
                intent.idempotencyKey(),
                intent.rootIdempotencyKey()));
    }
}
