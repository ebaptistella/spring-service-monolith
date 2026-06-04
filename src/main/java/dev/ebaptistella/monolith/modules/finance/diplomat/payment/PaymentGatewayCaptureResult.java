package dev.ebaptistella.monolith.modules.finance.diplomat.payment;

import java.util.Optional;

public record PaymentGatewayCaptureResult(
        boolean success, Optional<String> gatewayReference, Optional<String> failureReason) {

    public static PaymentGatewayCaptureResult success(String gatewayReference) {
        return new PaymentGatewayCaptureResult(true, Optional.of(gatewayReference), Optional.empty());
    }

    public static PaymentGatewayCaptureResult failure(String reason) {
        return new PaymentGatewayCaptureResult(false, Optional.empty(), Optional.of(reason));
    }
}
