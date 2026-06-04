package dev.ebaptistella.monolith.modules.identity.models;

import java.util.UUID;

public record LocalLoginResult(String accessToken, UUID accountId, String email) {
}
