package dev.ebaptistella.monolith.modules.identity.wire.out;

import java.util.UUID;

public record RegisterResponse(UUID accountId, String email) {
}
