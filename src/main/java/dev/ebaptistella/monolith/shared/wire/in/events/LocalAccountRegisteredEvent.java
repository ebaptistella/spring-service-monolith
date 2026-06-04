package dev.ebaptistella.monolith.shared.wire.in.events;

import dev.ebaptistella.monolith.shared.EventRoutes;
import org.springframework.modulith.events.Externalized;

import java.util.UUID;

@Externalized(EventRoutes.LOCAL_ACCOUNT_REGISTERED_EXTERNALIZED)
public record LocalAccountRegisteredEvent(UUID idempotencyKey, UUID accountId, String email) {
}
