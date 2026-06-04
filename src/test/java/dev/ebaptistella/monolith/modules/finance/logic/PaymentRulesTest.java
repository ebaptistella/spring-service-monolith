package dev.ebaptistella.monolith.modules.finance.logic;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class PaymentRulesTest {

    @Test
    void isValidCaptureAmount_acceptsPositiveValues() {
        assertThat(PaymentRules.isValidCaptureAmount(new BigDecimal("10.50"))).isTrue();
        assertThat(PaymentRules.isValidCaptureAmount(new BigDecimal("0.01"))).isTrue();
    }

    @Test
    void isValidCaptureAmount_rejectsZeroNegativeOrNull() {
        assertThat(PaymentRules.isValidCaptureAmount(BigDecimal.ZERO)).isFalse();
        assertThat(PaymentRules.isValidCaptureAmount(new BigDecimal("-1"))).isFalse();
        assertThat(PaymentRules.isValidCaptureAmount(null)).isFalse();
    }
}
