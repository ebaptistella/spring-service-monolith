package dev.ebaptistella.monolith.modules.notification.models;

public record OrderCancellationEmailContent(String to, String subject, String body) {
}
