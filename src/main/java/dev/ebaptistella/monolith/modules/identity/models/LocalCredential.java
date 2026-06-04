package dev.ebaptistella.monolith.modules.identity.models;

import java.util.UUID;

public record LocalCredential(UUID accountId, String passwordHash) {
}
