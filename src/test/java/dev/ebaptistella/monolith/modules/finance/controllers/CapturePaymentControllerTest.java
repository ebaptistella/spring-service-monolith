package dev.ebaptistella.monolith.modules.finance.controllers;

import dev.ebaptistella.monolith.modules.finance.diplomat.jpa.FinancePersistence;
import dev.ebaptistella.monolith.modules.finance.diplomat.payment.PaymentGateway;
import dev.ebaptistella.monolith.modules.finance.diplomat.payment.PaymentGatewayCaptureResult;
import dev.ebaptistella.monolith.modules.finance.diplomat.producer.FinanceEventProducer;
import dev.ebaptistella.monolith.modules.finance.logic.PaymentCaptureResult;
import dev.ebaptistella.monolith.modules.finance.models.CapturePaymentInput;
import dev.ebaptistella.monolith.modules.finance.models.PaymentIntent;
import dev.ebaptistella.monolith.modules.finance.models.PaymentStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CapturePaymentControllerTest {

    @Mock
    private FinancePersistence persistence;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private FinanceEventProducer producer;

    @InjectMocks
    private CapturePaymentController controller;

    @Test
    void capture_isIdempotentWhenPaymentAlreadyCaptured() {
        UUID root = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PaymentIntent captured = intent(root, orderId, PaymentStatus.CAPTURED);
        when(persistence.findIntentByIdempotencyKey(any())).thenReturn(Optional.of(captured));

        PaymentCaptureResult result = controller.capture(
                new CapturePaymentInput(root, orderId, new BigDecimal("99.90")));

        assertThat(result.idempotent()).isTrue();
        assertThat(result.failed()).isFalse();
        verify(paymentGateway, never()).capture(any(), any());
        verify(producer, never()).publishPaymentCaptured(any());
    }

    @Test
    void capture_failsWhenAmountIsNotPositive() {
        UUID root = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        PaymentCaptureResult result = controller.capture(new CapturePaymentInput(root, orderId, BigDecimal.ZERO));

        assertThat(result.failed()).isTrue();
        assertThat(result.failureReason()).hasValue("Capture amount must be positive");
        verify(persistence, never()).saveIntent(any());
        verify(paymentGateway, never()).capture(any(), any());
    }

    @Test
    void capture_createsIntentAndPublishesEventWhenGatewaySucceeds() {
        UUID root = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("150.00");
        when(persistence.findIntentByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(persistence.findIntentByOrderId(orderId)).thenReturn(Optional.empty());
        when(persistence.findTransactionByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(persistence.saveIntent(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGateway.capture(orderId, amount))
                .thenReturn(PaymentGatewayCaptureResult.success("gw-ref-1"));

        PaymentCaptureResult result = controller.capture(new CapturePaymentInput(root, orderId, amount));

        assertThat(result.failed()).isFalse();
        assertThat(result.idempotent()).isFalse();
        verify(persistence).saveTransaction(any());
        verify(producer).publishPaymentCaptured(any());
    }

    @Test
    void capture_publishesFailureWhenGatewayFails() {
        UUID root = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("75.00");
        when(persistence.findIntentByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(persistence.findIntentByOrderId(orderId)).thenReturn(Optional.empty());
        when(persistence.findTransactionByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(persistence.saveIntent(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGateway.capture(orderId, amount))
                .thenReturn(PaymentGatewayCaptureResult.failure("Declined"));

        PaymentCaptureResult result = controller.capture(new CapturePaymentInput(root, orderId, amount));

        assertThat(result.failed()).isTrue();
        assertThat(result.failureReason()).hasValue("Declined");
        verify(producer).publishPaymentFailed(any(PaymentIntent.class), org.mockito.ArgumentMatchers.eq("Declined"));
    }

    private static PaymentIntent intent(UUID root, UUID orderId, PaymentStatus status) {
        Instant now = Instant.now();
        return new PaymentIntent(
                UUID.randomUUID(),
                orderId,
                new BigDecimal("99.90"),
                "BRL",
                status,
                now,
                now,
                UUID.randomUUID(),
                root);
    }
}
