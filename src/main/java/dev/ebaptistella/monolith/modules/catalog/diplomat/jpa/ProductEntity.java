package dev.ebaptistella.monolith.modules.catalog.diplomat.jpa;

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
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class ProductEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private UUID idempotencyKey;

    @Column(name = "root_idempotency_key", nullable = false)
    private UUID rootIdempotencyKey;

    @Column(name = "request_fingerprint", nullable = false, length = 64)
    private String requestFingerprint;

    static ProductEntity fromModel(dev.ebaptistella.monolith.modules.catalog.models.Product product) {
        ProductEntity entity = new ProductEntity();
        entity.id = product.id();
        entity.name = product.name();
        entity.description = product.description();
        entity.active = product.active();
        entity.createdAt = product.createdAt();
        entity.idempotencyKey = product.idempotencyKey();
        entity.rootIdempotencyKey = product.rootIdempotencyKey();
        entity.requestFingerprint = product.requestFingerprint();
        return entity;
    }

    dev.ebaptistella.monolith.modules.catalog.models.Product toModel() {
        return new dev.ebaptistella.monolith.modules.catalog.models.Product(
                id,
                name,
                description,
                active,
                createdAt,
                idempotencyKey,
                rootIdempotencyKey,
                requestFingerprint);
    }
}
