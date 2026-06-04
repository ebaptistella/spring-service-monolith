package dev.ebaptistella.monolith.shared.wire.in.events;

import dev.ebaptistella.monolith.shared.EventRoutes;
import org.springframework.modulith.events.Externalized;

import java.util.UUID;

@Externalized(EventRoutes.EMAIL_DISPATCH_EXTERNALIZED)
public record EmailDispatchRequestedEvent(
        UUID idempotencyKey,
        UUID dispatchId,
        String to,
        String subject,
        String body
) {
}
