package dev.ebaptistella.monolith.modules.inventory.controllers;

import dev.ebaptistella.monolith.modules.inventory.diplomat.jpa.InventoryPersistence;
import dev.ebaptistella.monolith.modules.inventory.logic.StockRules;
import dev.ebaptistella.monolith.modules.inventory.models.AdjustStockInput;
import dev.ebaptistella.monolith.modules.inventory.models.MovementType;
import dev.ebaptistella.monolith.modules.inventory.models.StockLevel;
import dev.ebaptistella.monolith.modules.inventory.models.StockMovement;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyContext;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdjustStockController {

    private final InventoryPersistence persistence;

    @Transactional
    public StockLevel adjust(AdjustStockInput input) {
        UUID root = IdempotencyContext.requireRootKey();
        UUID movementKey = IdempotencyKeys.derive(root, "inventory", "adjustment", input.skuId().toString());

        if (persistence.findMovementByIdempotencyKey(movementKey).isPresent()) {
            return persistence.getOrCreateLevel(input.skuId());
        }

        StockLevel current = persistence.getOrCreateLevel(input.skuId());
        int resultingOnHand = current.onHand() + input.quantityDelta();

        if (!StockRules.isValidAdjustmentResult(resultingOnHand)) {
            throw new IllegalArgumentException("Adjustment would result in negative on-hand stock");
        }

        StockLevel updated = new StockLevel(
                current.skuId(), resultingOnHand, current.reserved(), Instant.now());
        persistence.saveLevel(updated);
        persistence.saveMovement(new StockMovement(
                UUID.randomUUID(),
                input.skuId(),
                null,
                input.quantityDelta(),
                MovementType.ADJUSTMENT,
                Instant.now(),
                movementKey));

        return updated;
    }
}
