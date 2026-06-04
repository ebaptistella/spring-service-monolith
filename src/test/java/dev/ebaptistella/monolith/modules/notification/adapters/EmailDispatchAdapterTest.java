package dev.ebaptistella.monolith.modules.notification.adapters;

import dev.ebaptistella.monolith.modules.notification.models.WelcomeNotificationContent;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyKeys;
import dev.ebaptistella.monolith.shared.wire.in.events.EmailDispatchRequestedEvent;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class EmailDispatchAdapterTest {

    @Test
    void contentToWire_derivesDeterministicDispatchIdFromRootKeyAndScope() {
        UUID rootKey = UUID.randomUUID();
        WelcomeNotificationContent content =
                new WelcomeNotificationContent("user@example.com", "Welcome", "Hello");

        EmailDispatchRequestedEvent wire = EmailDispatchAdapter.contentToWire(rootKey, content);

        UUID expectedDispatchId = IdempotencyKeys.derive(rootKey, EmailDispatchAdapter.SCOPE_WELCOME);
        assertThat(wire.idempotencyKey()).isEqualTo(expectedDispatchId);
        assertThat(wire.dispatchId()).isEqualTo(expectedDispatchId);
        assertThat(wire.to()).isEqualTo("user@example.com");
        assertThat(wire.subject()).isEqualTo("Welcome");
        assertThat(wire.body()).isEqualTo("Hello");
    }
}
