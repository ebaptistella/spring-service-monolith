package dev.ebaptistella.monolith.modules.order.logic;

import dev.ebaptistella.monolith.modules.order.models.OrderStatus;
import dev.ebaptistella.monolith.modules.order.models.PlaceOrderInput;
import dev.ebaptistella.monolith.modules.order.models.PlaceOrderLineInput;
import dev.ebaptistella.monolith.modules.order.models.PlaceOrderResolvedLine;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class OrderRulesTest {

    @Test
    void exceedsSpendLimit_whenCombinedTotalAboveLimit() {
        assertThat(OrderRules.exceedsSpendLimit(new BigDecimal("900.00"), new BigDecimal("100.01")))
                .isTrue();
    }

    @Test
    void exceedsSpendLimit_whenCombinedTotalAtOrBelowLimit() {
        assertThat(OrderRules.exceedsSpendLimit(new BigDecimal("900.00"), new BigDecimal("100.00")))
                .isFalse();
        assertThat(OrderRules.exceedsSpendLimit(new BigDecimal("900.00"), new BigDecimal("50.00")))
                .isFalse();
    }

    @Test
    void validatePlaceOrder_rejectsWhenStockUnavailable() {
        UUID customerId = UUID.randomUUID();
        UUID skuId = UUID.randomUUID();
        PlaceOrderInput input = new PlaceOrderInput(
                customerId, List.of(new PlaceOrderLineInput(skuId, 2)));
        List<PlaceOrderResolvedLine> resolvedLines = List.of(
                new PlaceOrderResolvedLine(skuId, 2, new BigDecimal("10.00")));

        PlaceOrderResult result = OrderRules.validatePlaceOrder(
                input, BigDecimal.ZERO, true, resolvedLines, false);

        assertThat(result.rejected()).isTrue();
        assertThat(result.error()).hasValueSatisfying(error -> assertThat(error).contains("Insufficient stock"));
    }

    @Test
    void validatePlaceOrder_rejectsWhenSpendLimitExceeded() {
        UUID customerId = UUID.randomUUID();
        UUID skuId = UUID.randomUUID();
        PlaceOrderInput input = new PlaceOrderInput(
                customerId, List.of(new PlaceOrderLineInput(skuId, 1)));
        List<PlaceOrderResolvedLine> resolvedLines = List.of(
                new PlaceOrderResolvedLine(skuId, 1, new BigDecimal("50.00")));

        PlaceOrderResult result = OrderRules.validatePlaceOrder(
                input, new BigDecimal("960.00"), true, resolvedLines, true);

        assertThat(result.rejected()).isTrue();
        assertThat(result.error()).hasValueSatisfying(error -> assertThat(error).contains("spend limit"));
    }

    @Test
    void validatePlaceOrder_succeedsWhenRulesPass() {
        UUID customerId = UUID.randomUUID();
        UUID skuId = UUID.randomUUID();
        PlaceOrderInput input = new PlaceOrderInput(
                customerId, List.of(new PlaceOrderLineInput(skuId, 2)));
        List<PlaceOrderResolvedLine> resolvedLines = List.of(
                new PlaceOrderResolvedLine(skuId, 2, new BigDecimal("10.00")));

        PlaceOrderResult result = OrderRules.validatePlaceOrder(
                input, BigDecimal.ZERO, true, resolvedLines, true);

        assertThat(result.rejected()).isFalse();
        assertThat(result.order()).isPresent();
        assertThat(result.order().orElseThrow().status()).isEqualTo(OrderStatus.PLACED);
        assertThat(result.order().orElseThrow().totalAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    void statusTransitions_followExpectedLifecycle() {
        assertThat(OrderRules.canMarkAwaitingPayment(OrderStatus.PLACED)).isTrue();
        assertThat(OrderRules.canMarkAwaitingPayment(OrderStatus.AWAITING_PAYMENT)).isFalse();

        assertThat(OrderRules.canConfirm(OrderStatus.AWAITING_PAYMENT)).isTrue();
        assertThat(OrderRules.canConfirm(OrderStatus.PLACED)).isFalse();

        assertThat(OrderRules.canCancel(OrderStatus.PLACED)).isTrue();
        assertThat(OrderRules.canCancel(OrderStatus.AWAITING_PAYMENT)).isTrue();
        assertThat(OrderRules.canCancel(OrderStatus.CONFIRMED)).isFalse();
        assertThat(OrderRules.canCancel(OrderStatus.CANCELLED)).isFalse();
    }

    @Test
    void shouldSkipMarkAwaitingPayment_whenAlreadyAwaitingOrInvalid() {
        assertThat(OrderRules.shouldSkipMarkAwaitingPayment(OrderStatus.AWAITING_PAYMENT)).isTrue();
        assertThat(OrderRules.shouldSkipMarkAwaitingPayment(OrderStatus.CONFIRMED)).isTrue();
        assertThat(OrderRules.shouldSkipMarkAwaitingPayment(OrderStatus.PLACED)).isFalse();
    }

    @Test
    void shouldSkipCancelTransition_whenAlreadyCancelledOrInvalid() {
        assertThat(OrderRules.shouldSkipCancelTransition(OrderStatus.CANCELLED)).isTrue();
        assertThat(OrderRules.shouldSkipCancelTransition(OrderStatus.CONFIRMED)).isTrue();
        assertThat(OrderRules.shouldSkipCancelTransition(OrderStatus.PLACED)).isFalse();
    }

    @Test
    void shouldSkipConfirmTransition_whenAlreadyConfirmedOrInvalid() {
        assertThat(OrderRules.shouldSkipConfirmTransition(OrderStatus.CONFIRMED)).isTrue();
        assertThat(OrderRules.shouldSkipConfirmTransition(OrderStatus.PLACED)).isTrue();
        assertThat(OrderRules.shouldSkipConfirmTransition(OrderStatus.AWAITING_PAYMENT)).isFalse();
    }
}
