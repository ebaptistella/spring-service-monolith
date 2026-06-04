package dev.ebaptistella.monolith.modules.inventory.controllers;

import dev.ebaptistella.monolith.modules.inventory.diplomat.jpa.InventoryPersistence;
import dev.ebaptistella.monolith.modules.inventory.logic.StockRules;
import dev.ebaptistella.monolith.modules.inventory.models.StockLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueryStockController {

    private final InventoryPersistence persistence;

    public int available(UUID skuId) {
        StockLevel level = persistence.getOrCreateLevel(skuId);
        return StockRules.available(level.onHand(), level.reserved());
    }

    public StockLevel getLevel(UUID skuId) {
        return persistence.getOrCreateLevel(skuId);
    }
}
