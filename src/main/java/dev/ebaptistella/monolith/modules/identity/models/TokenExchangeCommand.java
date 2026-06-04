package dev.ebaptistella.monolith.modules.identity.models;

public record TokenExchangeCommand(String grantType, String username, String password) {
}
