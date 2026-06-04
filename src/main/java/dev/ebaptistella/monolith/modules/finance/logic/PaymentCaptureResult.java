package dev.ebaptistella.monolith.modules.finance.logic;

import dev.ebaptistella.monolith.modules.finance.models.PaymentIntent;

import java.util.Optional;

public record PaymentCaptureResult(
        CaptureOutcome outcome, Optional<PaymentIntent> intent, Optional<String> failureReason) {

    public enum CaptureOutcome {
        SUCCESS,
        IDEMPOTENT,
        FAILED
    }

    public boolean failed() {
        return outcome == CaptureOutcome.FAILED;
    }

    public boolean idempotent() {
        return outcome == CaptureOutcome.IDEMPOTENT;
    }

    public static PaymentCaptureResult success(PaymentIntent intent) {
        return new PaymentCaptureResult(CaptureOutcome.SUCCESS, Optional.of(intent), Optional.empty());
    }

    public static PaymentCaptureResult idempotentSuccess(PaymentIntent intent) {
        return new PaymentCaptureResult(CaptureOutcome.IDEMPOTENT, Optional.of(intent), Optional.empty());
    }

    public static PaymentCaptureResult failed(String reason) {
        return new PaymentCaptureResult(CaptureOutcome.FAILED, Optional.empty(), Optional.of(reason));
    }
}
