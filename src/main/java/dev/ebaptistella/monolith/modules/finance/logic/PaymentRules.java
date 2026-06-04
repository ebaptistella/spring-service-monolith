package dev.ebaptistella.monolith.modules.finance.logic;

import java.math.BigDecimal;

public final class PaymentRules {

    private PaymentRules() {
    }

    public static boolean isValidCaptureAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }
}
