package dev.ebaptistella.monolith.support;

import java.time.Duration;

public final class AsyncTestSupport {

    public static final Duration TIMEOUT = Duration.ofSeconds(120);

    private AsyncTestSupport() {
    }
}
