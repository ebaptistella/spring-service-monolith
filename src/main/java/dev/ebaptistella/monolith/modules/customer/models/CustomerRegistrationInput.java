package dev.ebaptistella.monolith.modules.customer.models;

public record CustomerRegistrationInput(
        String email,
        String fullName
) {
}
