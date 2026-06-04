package dev.ebaptistella.monolith.modules.notification.logic;

import dev.ebaptistella.monolith.modules.notification.models.OrderCancellationEmailContent;
import dev.ebaptistella.monolith.modules.notification.models.OrderCancelledNotification;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class OrderCancellationRulesTest {

    @Test
    void buildEmailContent_mapsStockReasonToFriendlyCopy() {
        UUID orderId = UUID.randomUUID();
        OrderCancelledNotification notification = new OrderCancelledNotification(
                UUID.randomUUID(),
                orderId,
                UUID.randomUUID(),
                "buyer@example.com",
                new BigDecimal("99.90"),
                "Insufficient stock for SKU WIDGET-1");

        OrderCancellationEmailContent content = OrderCancellationRules.buildEmailContent(notification);

        assertThat(content.to()).isEqualTo("buyer@example.com");
        assertThat(content.subject()).isEqualTo("Your order was cancelled");
        assertThat(content.body()).contains(orderId.toString());
        assertThat(content.body()).contains("out of stock");
        assertThat(content.body()).contains("99.90 BRL");
    }

    @Test
    void buildEmailContent_mapsPaymentReasonToFriendlyCopy() {
        OrderCancelledNotification notification = new OrderCancelledNotification(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "buyer@example.com",
                new BigDecimal("50.00"),
                "Payment gateway declined capture");

        OrderCancellationEmailContent content = OrderCancellationRules.buildEmailContent(notification);

        assertThat(content.body()).contains("payment could not be processed");
    }

    @Test
    void buildEmailContent_usesGenericReasonWhenUnknown() {
        OrderCancelledNotification notification = new OrderCancelledNotification(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "buyer@example.com",
                new BigDecimal("10.00"),
                "Manual cancellation by operator");

        OrderCancellationEmailContent content = OrderCancellationRules.buildEmailContent(notification);

        assertThat(content.body()).contains("Reason: Manual cancellation by operator");
    }
}
