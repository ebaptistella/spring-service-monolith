package dev.ebaptistella.monolith.shared.idempotency;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public final class IdempotencyReplay {

    private IdempotencyReplay() {
    }

    public static <T, R> R resolve(
            Optional<T> existing,
            String fingerprint,
            Function<T, String> storedFingerprint,
            Function<T, R> onReplay,
            Supplier<R> onContinue) {
        return existing
                .map(entity -> {
                    IdempotencyAssertions.assertMatchingFingerprint(storedFingerprint.apply(entity), fingerprint);
                    return onReplay.apply(entity);
                })
                .orElseGet(onContinue);
    }
}
