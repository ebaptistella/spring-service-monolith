package dev.ebaptistella.monolith.shared.contracts.inventory;

import java.util.UUID;

public record StockLineRequest(UUID skuId, int quantity) {
}
