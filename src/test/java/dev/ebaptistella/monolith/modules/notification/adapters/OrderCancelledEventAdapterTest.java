package dev.ebaptistella.monolith.modules.notification.adapters;

import dev.ebaptistella.monolith.modules.notification.models.OrderCancelledNotification;
import dev.ebaptistella.monolith.shared.wire.in.events.OrderCancelledEvent;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class OrderCancelledEventAdapterTest {

    @Test
    void wireToModel_mapsAllFields() {
        UUID idempotencyKey = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        OrderCancelledEvent wire = new OrderCancelledEvent(
                idempotencyKey, orderId, customerId, "buyer@example.com", new BigDecimal("150.00"), "Payment failed");

        OrderCancelledNotification model = OrderCancelledEventAdapter.wireToModel(wire);

        assertThat(model.idempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(model.orderId()).isEqualTo(orderId);
        assertThat(model.customerId()).isEqualTo(customerId);
        assertThat(model.customerEmail()).isEqualTo("buyer@example.com");
        assertThat(model.totalAmount()).isEqualByComparingTo("150.00");
        assertThat(model.reason()).isEqualTo("Payment failed");
    }
}
