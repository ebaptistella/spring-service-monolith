package dev.ebaptistella.monolith.shared.wire.in.events;

import dev.ebaptistella.monolith.shared.EventRoutes;
import org.springframework.modulith.events.Externalized;

import java.util.UUID;

@Externalized(EventRoutes.ACCOUNT_STATUS_CHANGED_EXTERNALIZED)
public record AccountStatusChangedEvent(UUID idempotencyKey, UUID accountId, String email, String status) {
}
