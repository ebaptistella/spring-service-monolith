package dev.ebaptistella.monolith.modules.notification.models;

import java.util.UUID;

public record AccountStatusChangedNotification(
        UUID idempotencyKey, UUID accountId, String email, String status) {
}
