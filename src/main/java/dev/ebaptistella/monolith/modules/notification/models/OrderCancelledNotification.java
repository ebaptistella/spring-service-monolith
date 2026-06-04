package dev.ebaptistella.monolith.modules.notification.models;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCancelledNotification(
        UUID idempotencyKey,
        UUID orderId,
        UUID customerId,
        String customerEmail,
        BigDecimal totalAmount,
        String reason) {
}
