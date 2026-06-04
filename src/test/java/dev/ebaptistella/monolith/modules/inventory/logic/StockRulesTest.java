package dev.ebaptistella.monolith.modules.inventory.logic;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class StockRulesTest {

    @Test
    void available_isOnHandMinusReserved() {
        assertThat(StockRules.available(100, 30)).isEqualTo(70);
    }

    @Test
    void canReserve_whenQuantityPositiveAndAvailableSufficient() {
        assertThat(StockRules.canReserve(10, 5)).isTrue();
        assertThat(StockRules.canReserve(10, 10)).isTrue();
    }

    @Test
    void canReserve_rejectsZeroOrNegativeQuantityOrInsufficientStock() {
        assertThat(StockRules.canReserve(10, 0)).isFalse();
        assertThat(StockRules.canReserve(10, -1)).isFalse();
        assertThat(StockRules.canReserve(5, 6)).isFalse();
    }

    @Test
    void isValidAdjustmentResult_requiresNonNegativeOnHand() {
        assertThat(StockRules.isValidAdjustmentResult(0)).isTrue();
        assertThat(StockRules.isValidAdjustmentResult(5)).isTrue();
        assertThat(StockRules.isValidAdjustmentResult(-1)).isFalse();
    }
}
