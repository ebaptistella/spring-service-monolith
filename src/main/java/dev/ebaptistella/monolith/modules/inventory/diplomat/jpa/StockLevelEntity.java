package dev.ebaptistella.monolith.modules.inventory.diplomat.jpa;

import dev.ebaptistella.monolith.modules.inventory.models.StockLevel;
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
@Table(name = "stock_levels")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class StockLevelEntity {

    @Id
    @Column(name = "sku_id")
    private UUID skuId;

    @Column(name = "on_hand", nullable = false)
    private int onHand;

    @Column(nullable = false)
    private int reserved;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    static StockLevelEntity fromModel(StockLevel level) {
        StockLevelEntity entity = new StockLevelEntity();
        entity.skuId = level.skuId();
        entity.onHand = level.onHand();
        entity.reserved = level.reserved();
        entity.updatedAt = level.updatedAt();
        return entity;
    }

    StockLevel toModel() {
        return new StockLevel(skuId, onHand, reserved, updatedAt);
    }
}
