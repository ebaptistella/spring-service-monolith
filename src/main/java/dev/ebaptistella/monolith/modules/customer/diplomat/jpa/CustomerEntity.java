package dev.ebaptistella.monolith.modules.customer.diplomat.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class CustomerEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private UUID idempotencyKey;

    @Column(name = "root_idempotency_key", nullable = false)
    private UUID rootIdempotencyKey;

    @Column(name = "request_fingerprint", nullable = false, length = 64)
    private String requestFingerprint;

    static CustomerEntity fromModel(dev.ebaptistella.monolith.modules.customer.models.Customer customer) {
        CustomerEntity entity = new CustomerEntity();
        entity.id = customer.id();
        entity.email = customer.email();
        entity.fullName = customer.fullName();
        entity.createdAt = customer.createdAt();
        entity.idempotencyKey = customer.idempotencyKey();
        entity.rootIdempotencyKey = customer.rootIdempotencyKey();
        entity.requestFingerprint = customer.requestFingerprint();
        return entity;
    }

    dev.ebaptistella.monolith.modules.customer.models.Customer toModel() {
        return new dev.ebaptistella.monolith.modules.customer.models.Customer(
                id,
                email,
                fullName,
                createdAt,
                idempotencyKey,
                rootIdempotencyKey,
                requestFingerprint);
    }
}
