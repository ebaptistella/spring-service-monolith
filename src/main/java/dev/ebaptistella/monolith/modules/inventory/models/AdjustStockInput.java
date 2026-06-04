package dev.ebaptistella.monolith.modules.inventory.models;

import java.util.UUID;

public record AdjustStockInput(UUID skuId, int quantityDelta) {
}
