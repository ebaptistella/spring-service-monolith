package dev.ebaptistella.monolith.modules.inventory.models;

import java.time.Instant;
import java.util.UUID;

public record StockLevel(UUID skuId, int onHand, int reserved, Instant updatedAt) {
}
