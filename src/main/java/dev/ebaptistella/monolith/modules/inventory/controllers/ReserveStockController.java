package dev.ebaptistella.monolith.modules.inventory.controllers;

import dev.ebaptistella.monolith.modules.inventory.diplomat.jpa.InventoryPersistence;
import dev.ebaptistella.monolith.modules.inventory.logic.StockReservationResult;
import dev.ebaptistella.monolith.modules.inventory.logic.StockRules;
import dev.ebaptistella.monolith.modules.inventory.models.MovementType;
import dev.ebaptistella.monolith.modules.inventory.models.ReservationStatus;
import dev.ebaptistella.monolith.modules.inventory.models.StockLevel;
import dev.ebaptistella.monolith.modules.inventory.models.StockMovement;
import dev.ebaptistella.monolith.modules.inventory.models.StockReservation;
import dev.ebaptistella.monolith.modules.inventory.models.StockReserveInput;
import dev.ebaptistella.monolith.modules.inventory.models.StockReserveLine;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReserveStockController {

    private final InventoryPersistence persistence;

    @Transactional
    public StockReservationResult reserve(StockReserveInput input) {
        UUID root = input.eventIdempotencyKey();
        List<StockReserveLine> linesToReserve = new ArrayList<>();

        for (StockReserveLine line : input.lines()) {
            UUID reservationKey = reservationKey(root, input.orderId(), line.skuId());
            if (persistence.findReservationByIdempotencyKey(reservationKey).isPresent()) {
                continue;
            }
            linesToReserve.add(line);
        }

        if (linesToReserve.isEmpty()) {
            return StockReservationResult.idempotentSuccess();
        }

        Map<UUID, StockLevel> levels = new HashMap<>();
        for (StockReserveLine line : linesToReserve) {
            StockLevel level = persistence.getOrCreateLevel(line.skuId());
            int available = StockRules.available(level.onHand(), level.reserved());
            if (!StockRules.canReserve(available, line.quantity())) {
                return StockReservationResult.failed(
                        "Insufficient stock for SKU " + line.skuId());
            }
            levels.put(line.skuId(), level);
        }

        Instant now = Instant.now();
        for (StockReserveLine line : linesToReserve) {
            UUID reservationKey = reservationKey(root, input.orderId(), line.skuId());
            UUID movementKey = reserveMovementKey(root, input.orderId(), line.skuId());

            StockLevel level = levels.get(line.skuId());
            StockLevel updated = new StockLevel(
                    level.skuId(), level.onHand(), level.reserved() + line.quantity(), now);
            persistence.saveLevel(updated);
            levels.put(line.skuId(), updated);

            persistence.saveReservation(new StockReservation(
                    UUID.randomUUID(),
                    input.orderId(),
                    line.skuId(),
                    line.quantity(),
                    ReservationStatus.ACTIVE,
                    now,
                    reservationKey));

            persistence.saveMovement(new StockMovement(
                    UUID.randomUUID(),
                    line.skuId(),
                    input.orderId(),
                    line.quantity(),
                    MovementType.RESERVE,
                    now,
                    movementKey));
        }

        return StockReservationResult.success();
    }

    private static UUID reservationKey(UUID root, UUID orderId, UUID skuId) {
        return IdempotencyKeys.derive(root, "inventory", "reserve", orderId.toString(), skuId.toString());
    }

    private static UUID reserveMovementKey(UUID root, UUID orderId, UUID skuId) {
        return IdempotencyKeys.derive(
                root, "inventory", "movement", "reserve", orderId.toString(), skuId.toString());
    }
}
