package dev.ebaptistella.monolith.modules.notification.diplomat;

import java.util.UUID;

public record NotificationWelcomeRequest(UUID customerId, String email, String fullName) {}
