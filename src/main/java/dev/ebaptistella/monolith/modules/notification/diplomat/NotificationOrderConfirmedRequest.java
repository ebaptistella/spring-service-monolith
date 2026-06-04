package dev.ebaptistella.monolith.modules.notification.diplomat;

import java.util.UUID;

public record NotificationOrderConfirmedRequest(
        UUID orderId, UUID customerId, String email, String totalAmount) {}
