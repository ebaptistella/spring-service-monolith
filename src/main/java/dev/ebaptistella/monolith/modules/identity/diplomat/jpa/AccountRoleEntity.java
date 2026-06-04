package dev.ebaptistella.monolith.modules.identity.diplomat.jpa;

import dev.ebaptistella.monolith.shared.models.auth.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "account_roles")
@IdClass(AccountRoleId.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class AccountRoleEntity {

    @Id
    @Column(name = "account_id")
    private UUID accountId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private AccountEntity account;

    AccountRoleEntity(AccountEntity account, Role role) {
        this.account = account;
        this.accountId = account.getId();
        this.role = role;
    }
}
