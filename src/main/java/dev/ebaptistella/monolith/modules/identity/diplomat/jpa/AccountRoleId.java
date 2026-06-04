package dev.ebaptistella.monolith.modules.identity.diplomat.jpa;

import dev.ebaptistella.monolith.shared.models.auth.Role;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.UUID;

@EqualsAndHashCode
class AccountRoleId implements Serializable {

    private UUID accountId;
    private Role role;
}
