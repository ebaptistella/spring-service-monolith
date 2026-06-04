package dev.ebaptistella.monolith.shared.wire.in.events;

import dev.ebaptistella.monolith.shared.EventRoutes;
import org.springframework.modulith.events.Externalized;

import java.util.UUID;

@Externalized(EventRoutes.CUSTOMER_CREATED_EXTERNALIZED)
public record CustomerCreatedEvent(
        UUID idempotencyKey,
        UUID customerId,
        String email,
        String fullName
) {
}
