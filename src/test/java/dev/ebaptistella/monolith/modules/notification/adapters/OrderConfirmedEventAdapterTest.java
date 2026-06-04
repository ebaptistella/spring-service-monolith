package dev.ebaptistella.monolith.modules.notification.adapters;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class OrderConfirmedEventAdapterTest {

    @Test
    void wireToModelMapsAllFields() {
        UUID idempotencyKey = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        var notification = OrderConfirmedEventAdapter.wireToModel(new dev.ebaptistella.monolith.shared.wire.in.events
                .OrderConfirmedEvent(
                        idempotencyKey, orderId, customerId, "buyer@example.com", new BigDecimal("150.00")));

        assertThat(notification.idempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(notification.orderId()).isEqualTo(orderId);
        assertThat(notification.customerEmail()).isEqualTo("buyer@example.com");
        assertThat(notification.totalAmount()).isEqualByComparingTo("150.00");
    }
}
