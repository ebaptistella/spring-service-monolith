package dev.ebaptistella.monolith.modules.identity.wire.out;

import java.util.UUID;

public record LoginResponse(String accessToken, UUID accountId, String email) {
}
