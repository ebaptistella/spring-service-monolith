package dev.ebaptistella.monolith.modules.notification.adapters;

import dev.ebaptistella.monolith.modules.notification.models.CustomerCreatedNotification;
import dev.ebaptistella.monolith.shared.wire.in.events.LocalAccountRegisteredEvent;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class LocalAccountRegisteredEventAdapterTest {

    @Test
    void wireToWelcomeNotification_derivesDisplayNameFromEmail() {
        UUID idempotencyKey = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        LocalAccountRegisteredEvent wire = new LocalAccountRegisteredEvent(idempotencyKey, accountId, "local.user@example.com");

        CustomerCreatedNotification model = LocalAccountRegisteredEventAdapter.wireToWelcomeNotification(wire);

        assertThat(model.idempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(model.customerId()).isEqualTo(accountId);
        assertThat(model.email()).isEqualTo("local.user@example.com");
        assertThat(model.fullName()).isEqualTo("local.user");
    }
}
