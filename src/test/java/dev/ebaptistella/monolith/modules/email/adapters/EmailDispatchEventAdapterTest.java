package dev.ebaptistella.monolith.modules.email.adapters;

import dev.ebaptistella.monolith.modules.email.models.EmailDispatch;
import dev.ebaptistella.monolith.shared.wire.in.events.EmailDispatchRequestedEvent;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class EmailDispatchEventAdapterTest {

    @Test
    void wireToModel_mapsAllFields() {
        UUID idempotencyKey = UUID.randomUUID();
        UUID dispatchId = UUID.randomUUID();
        EmailDispatchRequestedEvent wire =
                new EmailDispatchRequestedEvent(idempotencyKey, dispatchId, "ana@example.com", "Welcome", "Hello Ana");

        EmailDispatch model = EmailDispatchEventAdapter.wireToModel(wire);

        assertThat(model.dispatchId()).isEqualTo(dispatchId);
        assertThat(model.to()).isEqualTo("ana@example.com");
        assertThat(model.subject()).isEqualTo("Welcome");
        assertThat(model.body()).isEqualTo("Hello Ana");
    }
}
