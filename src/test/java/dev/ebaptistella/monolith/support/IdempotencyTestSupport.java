package dev.ebaptistella.monolith.support;

import dev.ebaptistella.monolith.shared.idempotency.IdempotencyContext;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyHeaders;
import io.restassured.specification.RequestSpecification;

import java.util.UUID;
import java.util.function.Supplier;

public final class IdempotencyTestSupport {

    private IdempotencyTestSupport() {
    }

    public static String newKey() {
        return UUID.randomUUID().toString();
    }

    public static RequestSpecification withIdempotencyKey(RequestSpecification spec) {
        return spec.header(IdempotencyHeaders.X_IDEMPOTENCY_KEY, newKey());
    }

    public static void runWithRootKey(Runnable test) {
        IdempotencyContext.runWithRootKey(UUID.randomUUID(), test);
    }

    public static <T> T runWithRootKey(Supplier<T> test) {
        return IdempotencyContext.runWithRootKey(UUID.randomUUID(), test);
    }
}
