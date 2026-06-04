package dev.ebaptistella.monolith.shared.idempotency;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class IdempotencyKeysTest {

    @Test
    void derive_isDeterministic() {
        UUID root = UUID.randomUUID();

        UUID first = IdempotencyKeys.derive(root, "order", "place");
        UUID second = IdempotencyKeys.derive(root, "order", "place");

        assertThat(first).isEqualTo(second);
    }

    @Test
    void derive_changesWhenScopeChanges() {
        UUID root = UUID.randomUUID();

        UUID orderKey = IdempotencyKeys.derive(root, "order", "place");
        UUID customerKey = IdempotencyKeys.derive(root, "customer", "register");

        assertThat(orderKey).isNotEqualTo(customerKey);
    }
}
