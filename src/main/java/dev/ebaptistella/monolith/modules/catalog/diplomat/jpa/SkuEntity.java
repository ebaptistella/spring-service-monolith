package dev.ebaptistella.monolith.modules.catalog.diplomat.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "skus")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class SkuEntity {

    @Id
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(name = "list_price", nullable = false)
    private BigDecimal listPrice;

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

    static SkuEntity fromModel(dev.ebaptistella.monolith.modules.catalog.models.Sku sku) {
        SkuEntity entity = new SkuEntity();
        entity.id = sku.id();
        entity.productId = sku.productId();
        entity.code = sku.code();
        entity.listPrice = sku.listPrice();
        entity.active = sku.active();
        entity.createdAt = sku.createdAt();
        entity.idempotencyKey = sku.idempotencyKey();
        entity.rootIdempotencyKey = sku.rootIdempotencyKey();
        entity.requestFingerprint = sku.requestFingerprint();
        return entity;
    }

    dev.ebaptistella.monolith.modules.catalog.models.Sku toModel() {
        return new dev.ebaptistella.monolith.modules.catalog.models.Sku(
                id,
                productId,
                code,
                listPrice,
                active,
                createdAt,
                idempotencyKey,
                rootIdempotencyKey,
                requestFingerprint);
    }
}
