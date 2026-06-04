package dev.ebaptistella.monolith.shared.idempotency;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public final class IdempotencyContext {

    public static final ScopedValue<UUID> ROOT_KEY = ScopedValue.newInstance();

    private IdempotencyContext() {
    }

    public static Optional<UUID> rootKey() {
        return ROOT_KEY.isBound() ? Optional.of(ROOT_KEY.get()) : Optional.empty();
    }

    public static UUID requireRootKey() {
        return rootKey().orElseThrow(() -> new IllegalStateException("Missing X-Idempotency-Key in request context"));
    }

    public static void runWithRootKey(UUID rootKey, Runnable action) {
        ScopedValue.where(ROOT_KEY, rootKey).run(action);
    }

    public static <T> T runWithRootKey(UUID rootKey, Supplier<T> supplier) {
        try {
            return ScopedValue.where(ROOT_KEY, rootKey).call(supplier::get);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Idempotency scope failed", ex);
        }
    }

    public static <T> T callWithRootKey(UUID rootKey, Callable<T> callable) throws Exception {
        return ScopedValue.where(ROOT_KEY, rootKey).call(callable);
    }
}
