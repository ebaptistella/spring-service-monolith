package dev.ebaptistella.monolith.modules.notification.adapters;

import dev.ebaptistella.monolith.modules.notification.models.CustomerCreatedNotification;
import dev.ebaptistella.monolith.shared.wire.in.events.CustomerCreatedEvent;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class CustomerCreatedEventAdapterTest {

    @Test
    void wireToModel_mapsAllFields() {
        UUID idempotencyKey = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        CustomerCreatedEvent wire = new CustomerCreatedEvent(idempotencyKey, customerId, "ana@example.com", "Ana Baptista");

        CustomerCreatedNotification model = CustomerCreatedEventAdapter.wireToModel(wire);

        assertThat(model.idempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(model.customerId()).isEqualTo(customerId);
        assertThat(model.email()).isEqualTo("ana@example.com");
        assertThat(model.fullName()).isEqualTo("Ana Baptista");
    }
}
