package dev.ebaptistella.monolith.shared.contracts;

import dev.ebaptistella.monolith.shared.models.auth.AuthenticatedUser;

import java.util.Optional;

public interface CurrentUserProvider {

    Optional<AuthenticatedUser> current();
}
