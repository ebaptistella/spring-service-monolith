package dev.ebaptistella.monolith.modules.identity.diplomat.jpa;

import dev.ebaptistella.monolith.modules.identity.models.LocalCredential;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "local_credentials")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class LocalCredentialEntity {

    @Id
    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    static LocalCredentialEntity fromModel(LocalCredential credential) {
        LocalCredentialEntity entity = new LocalCredentialEntity();
        entity.accountId = credential.accountId();
        entity.passwordHash = credential.passwordHash();
        return entity;
    }

    LocalCredential toModel() {
        return new LocalCredential(accountId, passwordHash);
    }
}
