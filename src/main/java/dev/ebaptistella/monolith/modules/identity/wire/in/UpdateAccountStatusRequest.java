package dev.ebaptistella.monolith.modules.identity.wire.in;

import dev.ebaptistella.monolith.modules.identity.models.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAccountStatusRequest(@NotNull AccountStatus status) {
}
