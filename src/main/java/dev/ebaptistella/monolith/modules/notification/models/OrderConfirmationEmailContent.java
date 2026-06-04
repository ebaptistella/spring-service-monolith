package dev.ebaptistella.monolith.modules.notification.models;

public record OrderConfirmationEmailContent(String to, String subject, String body) {
}
