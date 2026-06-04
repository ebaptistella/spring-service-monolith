package dev.ebaptistella.monolith.modules.inventory.diplomat.jpa;

import dev.ebaptistella.monolith.modules.inventory.models.MovementType;
import dev.ebaptistella.monolith.modules.inventory.models.StockMovement;
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
@Table(name = "stock_movements")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class StockMovementEntity {

    @Id
    private UUID id;

    @Column(name = "sku_id", nullable = false)
    private UUID skuId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MovementType type;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private UUID idempotencyKey;

    static StockMovementEntity fromModel(StockMovement movement) {
        StockMovementEntity entity = new StockMovementEntity();
        entity.id = movement.id();
        entity.skuId = movement.skuId();
        entity.orderId = movement.orderId();
        entity.quantity = movement.quantity();
        entity.type = movement.type();
        entity.createdAt = movement.createdAt();
        entity.idempotencyKey = movement.idempotencyKey();
        return entity;
    }

    StockMovement toModel() {
        return new StockMovement(id, skuId, orderId, quantity, type, createdAt, idempotencyKey);
    }
}
