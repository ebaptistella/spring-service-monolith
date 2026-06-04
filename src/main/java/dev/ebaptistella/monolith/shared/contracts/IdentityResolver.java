package dev.ebaptistella.monolith.shared.contracts;

import dev.ebaptistella.monolith.shared.models.auth.AuthProvider;
import dev.ebaptistella.monolith.shared.models.auth.AuthenticatedUser;
import dev.ebaptistella.monolith.shared.models.auth.Role;

import java.util.Set;

public interface IdentityResolver {

    AuthenticatedUser resolve(
            AuthProvider provider,
            String externalSubject,
            String emailHint,
            Set<Role> rolesHint);
}
