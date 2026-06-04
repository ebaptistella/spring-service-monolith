package dev.ebaptistella.monolith.shared.idempotency;

import dev.ebaptistella.monolith.shared.exception.IdempotencyConflictException;

public final class IdempotencyAssertions {

    private IdempotencyAssertions() {
    }

    public static void assertMatchingFingerprint(String stored, String computed) {
        if (stored == null || !stored.equals(computed)) {
            throw new IdempotencyConflictException();
        }
    }
}
