package dev.ebaptistella.monolith.modules.inventory.diplomat.jpa;

import dev.ebaptistella.monolith.modules.inventory.models.ReservationStatus;
import dev.ebaptistella.monolith.modules.inventory.models.StockReservation;
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
@Table(name = "stock_reservations")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class StockReservationEntity {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "sku_id", nullable = false)
    private UUID skuId;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private UUID idempotencyKey;

    static StockReservationEntity fromModel(StockReservation reservation) {
        StockReservationEntity entity = new StockReservationEntity();
        entity.id = reservation.id();
        entity.orderId = reservation.orderId();
        entity.skuId = reservation.skuId();
        entity.quantity = reservation.quantity();
        entity.status = reservation.status();
        entity.createdAt = reservation.createdAt();
        entity.idempotencyKey = reservation.idempotencyKey();
        return entity;
    }

    StockReservation toModel() {
        return new StockReservation(id, orderId, skuId, quantity, status, createdAt, idempotencyKey);
    }
}
