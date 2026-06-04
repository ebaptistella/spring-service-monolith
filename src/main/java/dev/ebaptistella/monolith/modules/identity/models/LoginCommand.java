package dev.ebaptistella.monolith.modules.identity.models;

public record LoginCommand(String email, String password, String provider) {
}
