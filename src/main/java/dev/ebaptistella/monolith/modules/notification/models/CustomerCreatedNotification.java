package dev.ebaptistella.monolith.modules.notification.models;

import java.util.UUID;

public record CustomerCreatedNotification(
        UUID idempotencyKey,
        UUID customerId,
        String email,
        String fullName
) {
}
