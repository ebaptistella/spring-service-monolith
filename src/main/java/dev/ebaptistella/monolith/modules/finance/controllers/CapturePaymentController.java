package dev.ebaptistella.monolith.modules.finance.controllers;

import dev.ebaptistella.monolith.modules.finance.diplomat.jpa.FinancePersistence;
import dev.ebaptistella.monolith.modules.finance.diplomat.payment.PaymentGateway;
import dev.ebaptistella.monolith.modules.finance.diplomat.payment.PaymentGatewayCaptureResult;
import dev.ebaptistella.monolith.modules.finance.diplomat.producer.FinanceEventProducer;
import dev.ebaptistella.monolith.modules.finance.logic.PaymentCaptureResult;
import dev.ebaptistella.monolith.modules.finance.logic.PaymentRules;
import dev.ebaptistella.monolith.modules.finance.models.CapturePaymentInput;
import dev.ebaptistella.monolith.modules.finance.models.PaymentIntent;
import dev.ebaptistella.monolith.modules.finance.models.PaymentStatus;
import dev.ebaptistella.monolith.modules.finance.models.Transaction;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CapturePaymentController {

    private static final String DEFAULT_CURRENCY = "BRL";

    private final FinancePersistence persistence;
    private final PaymentGateway paymentGateway;
    private final FinanceEventProducer producer;

    @Transactional
    public PaymentCaptureResult capture(CapturePaymentInput input) {
        if (!PaymentRules.isValidCaptureAmount(input.amount())) {
            return PaymentCaptureResult.failed("Capture amount must be positive");
        }

        UUID intentKey = intentKey(input.eventIdempotencyKey(), input.orderId());
        UUID transactionKey = transactionKey(input.eventIdempotencyKey(), input.orderId());

        PaymentIntent intent = persistence.findIntentByIdempotencyKey(intentKey)
                .or(() -> persistence.findIntentByOrderId(input.orderId()))
                .orElseGet(() -> createPendingIntent(input, intentKey));

        return captureExisting(intent, transactionKey);
    }

    private PaymentIntent createPendingIntent(CapturePaymentInput input, UUID intentKey) {
        Instant now = Instant.now();
        PaymentIntent intent = new PaymentIntent(
                UUID.randomUUID(),
                input.orderId(),
                input.amount(),
                DEFAULT_CURRENCY,
                PaymentStatus.PENDING,
                now,
                now,
                intentKey,
                input.eventIdempotencyKey());
        persistence.saveIntent(intent);
        return intent;
    }

    private PaymentCaptureResult captureExisting(PaymentIntent intent, UUID transactionKey) {
        return switch (intent.status()) {
            case CAPTURED -> PaymentCaptureResult.idempotentSuccess(intent);
            case CANCELLED -> PaymentCaptureResult.failed("Payment cancelled");
            case FAILED -> handleFailed(intent, transactionKey);
            case PENDING -> captureViaGateway(intent, transactionKey);
        };
    }

    private PaymentCaptureResult handleFailed(PaymentIntent intent, UUID transactionKey) {
        if (persistence.findTransactionByIdempotencyKey(transactionKey).isPresent()) {
            return PaymentCaptureResult.idempotentSuccess(intent);
        }
        return PaymentCaptureResult.failed("Payment already failed");
    }

    private PaymentCaptureResult captureViaGateway(PaymentIntent intent, UUID transactionKey) {
        PaymentGatewayCaptureResult gatewayResult =
                paymentGateway.capture(intent.orderId(), intent.amount());
        Instant now = Instant.now();

        if (gatewayResult.success()) {
            if (persistence.findTransactionByIdempotencyKey(transactionKey).isPresent()) {
                return PaymentCaptureResult.idempotentSuccess(intent);
            }

            PaymentIntent captured = new PaymentIntent(
                    intent.id(),
                    intent.orderId(),
                    intent.amount(),
                    intent.currency(),
                    PaymentStatus.CAPTURED,
                    intent.createdAt(),
                    now,
                    intent.idempotencyKey(),
                    intent.rootIdempotencyKey());
            persistence.saveIntent(captured);
            persistence.saveTransaction(new Transaction(
                    UUID.randomUUID(),
                    intent.id(),
                    PaymentStatus.CAPTURED,
                    gatewayResult.gatewayReference().orElse(null),
                    now,
                    transactionKey));
            producer.publishPaymentCaptured(captured);
            return PaymentCaptureResult.success(captured);
        }

        String reason = gatewayResult.failureReason().orElse("Payment gateway failure");
        if (persistence.findTransactionByIdempotencyKey(transactionKey).isPresent()) {
            return PaymentCaptureResult.failed(reason);
        }

        PaymentIntent failed = new PaymentIntent(
                intent.id(),
                intent.orderId(),
                intent.amount(),
                intent.currency(),
                PaymentStatus.FAILED,
                intent.createdAt(),
                now,
                intent.idempotencyKey(),
                intent.rootIdempotencyKey());
        persistence.saveIntent(failed);
        persistence.saveTransaction(new Transaction(
                UUID.randomUUID(),
                intent.id(),
                PaymentStatus.FAILED,
                null,
                now,
                transactionKey));
        producer.publishPaymentFailed(failed, reason);
        return PaymentCaptureResult.failed(reason);
    }

    private static UUID intentKey(UUID root, UUID orderId) {
        return IdempotencyKeys.derive(root, "finance", "intent", orderId.toString());
    }

    private static UUID transactionKey(UUID root, UUID orderId) {
        return IdempotencyKeys.derive(root, "finance", "transaction", orderId.toString());
    }
}
