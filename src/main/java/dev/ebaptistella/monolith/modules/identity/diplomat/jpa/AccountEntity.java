package dev.ebaptistella.monolith.modules.identity.diplomat.jpa;

import dev.ebaptistella.monolith.modules.identity.models.Account;
import dev.ebaptistella.monolith.modules.identity.models.AccountStatus;
import dev.ebaptistella.monolith.shared.models.auth.Role;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class AccountEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AccountStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private UUID idempotencyKey;

    @Column(name = "root_idempotency_key", nullable = false)
    private UUID rootIdempotencyKey;

    @Column(name = "request_fingerprint", nullable = false, length = 64)
    private String requestFingerprint;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 16)
    private Set<AccountRoleEntity> roles = new HashSet<>();

    static AccountEntity fromModel(Account account) {
        AccountEntity entity = new AccountEntity();
        entity.id = account.id();
        entity.email = account.email();
        entity.status = account.status();
        entity.createdAt = account.createdAt();
        entity.idempotencyKey = account.idempotencyKey();
        entity.rootIdempotencyKey = account.rootIdempotencyKey();
        entity.requestFingerprint = account.requestFingerprint();
        entity.roles.clear();
        for (Role role : account.roles()) {
            entity.roles.add(new AccountRoleEntity(entity, role));
        }
        return entity;
    }

    Account toModel() {
        Set<Role> roleSet = roles.stream().map(AccountRoleEntity::getRole).collect(java.util.stream.Collectors.toUnmodifiableSet());
        return new Account(
                id,
                email,
                status,
                roleSet,
                createdAt,
                idempotencyKey,
                rootIdempotencyKey,
                requestFingerprint);
    }
}
