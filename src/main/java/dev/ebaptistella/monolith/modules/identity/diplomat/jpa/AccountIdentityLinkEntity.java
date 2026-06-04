package dev.ebaptistella.monolith.modules.identity.diplomat.jpa;

import dev.ebaptistella.monolith.modules.identity.models.AccountIdentityLink;
import dev.ebaptistella.monolith.shared.models.auth.AuthProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account_identity_links")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class AccountIdentityLinkEntity {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AuthProvider provider;

    @Column(name = "external_subject", nullable = false, length = 512)
    private String externalSubject;

    @Column(name = "linked_at", nullable = false)
    private Instant linkedAt;

    static AccountIdentityLinkEntity fromModel(AccountIdentityLink link) {
        AccountIdentityLinkEntity entity = new AccountIdentityLinkEntity();
        entity.id = link.id();
        entity.accountId = link.accountId();
        entity.provider = link.provider();
        entity.externalSubject = link.externalSubject();
        entity.linkedAt = link.linkedAt();
        return entity;
    }

    AccountIdentityLink toModel() {
        return new AccountIdentityLink(id, accountId, provider, externalSubject, linkedAt);
    }
}
