package dev.ebaptistella.monolith.modules.inventory.controllers;

import dev.ebaptistella.monolith.modules.inventory.diplomat.jpa.InventoryPersistence;
import dev.ebaptistella.monolith.modules.inventory.models.MovementType;
import dev.ebaptistella.monolith.modules.inventory.models.ReservationStatus;
import dev.ebaptistella.monolith.modules.inventory.models.StockLevel;
import dev.ebaptistella.monolith.modules.inventory.models.StockMovement;
import dev.ebaptistella.monolith.modules.inventory.models.StockReservation;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReleaseStockController {

    private final InventoryPersistence persistence;

    @Transactional
    public void release(UUID eventIdempotencyKey, UUID orderId) {
        Instant now = Instant.now();
        for (StockReservation reservation : persistence.findActiveReservationsByOrderId(orderId)) {
            UUID movementKey = releaseMovementKey(eventIdempotencyKey, orderId, reservation.skuId());
            if (persistence.findMovementByIdempotencyKey(movementKey).isPresent()) {
                continue;
            }

            StockLevel level = persistence.getOrCreateLevel(reservation.skuId());
            StockLevel updated = new StockLevel(
                    level.skuId(),
                    level.onHand(),
                    level.reserved() - reservation.quantity(),
                    now);
            persistence.saveLevel(updated);

            persistence.saveReservation(new StockReservation(
                    reservation.id(),
                    reservation.orderId(),
                    reservation.skuId(),
                    reservation.quantity(),
                    ReservationStatus.RELEASED,
                    reservation.createdAt(),
                    reservation.idempotencyKey()));

            persistence.saveMovement(new StockMovement(
                    UUID.randomUUID(),
                    reservation.skuId(),
                    orderId,
                    reservation.quantity(),
                    MovementType.RELEASE,
                    now,
                    movementKey));
        }
    }

    private static UUID releaseMovementKey(UUID root, UUID orderId, UUID skuId) {
        return IdempotencyKeys.derive(
                root, "inventory", "movement", "release", orderId.toString(), skuId.toString());
    }
}
