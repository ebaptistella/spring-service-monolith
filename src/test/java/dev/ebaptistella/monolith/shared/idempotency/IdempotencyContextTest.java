package dev.ebaptistella.monolith.shared.idempotency;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("unit")
class IdempotencyContextTest {

    @Test
    void requireRootKey_throwsWhenNotBound() {
        assertThatThrownBy(IdempotencyContext::requireRootKey)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Missing X-Idempotency-Key");
    }

    @Test
    void runWithRootKey_bindsRootKeyInsideScope() {
        UUID rootKey = UUID.randomUUID();

        IdempotencyContext.runWithRootKey(rootKey, () ->
                assertThat(IdempotencyContext.requireRootKey()).isEqualTo(rootKey));

        assertThat(IdempotencyContext.rootKey()).isEmpty();
    }

    @Test
    void runWithRootKeySupplier_returnsValue() {
        UUID rootKey = UUID.randomUUID();

        String value = IdempotencyContext.runWithRootKey(rootKey, () -> {
            assertThat(IdempotencyContext.requireRootKey()).isEqualTo(rootKey);
            return "ok";
        });

        assertThat(value).isEqualTo("ok");
        assertThat(IdempotencyContext.rootKey()).isEmpty();
    }
}
