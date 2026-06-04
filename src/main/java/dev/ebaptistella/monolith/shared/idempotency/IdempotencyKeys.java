package dev.ebaptistella.monolith.shared.idempotency;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class IdempotencyKeys {

    private static final UUID NAMESPACE = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");

    private IdempotencyKeys() {
    }

    public static UUID derive(UUID root, String... parts) {
        if (root == null) {
            throw new IllegalArgumentException("root idempotency key is required");
        }
        String material = Stream.concat(Stream.of(root.toString()), Stream.of(parts))
                .collect(Collectors.joining(":"));
        return UUID.nameUUIDFromBytes(material.getBytes(StandardCharsets.UTF_8));
    }

    public static UUID namespace() {
        return NAMESPACE;
    }
}
