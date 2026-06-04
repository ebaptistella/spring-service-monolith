package dev.ebaptistella.monolith.modules.finance.diplomat.payment;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentGateway {

    PaymentGatewayCaptureResult capture(UUID orderId, BigDecimal amount);
}
