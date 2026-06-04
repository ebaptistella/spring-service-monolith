package dev.ebaptistella.monolith.modules.inventory.models;

import java.util.UUID;

public record StockReserveLine(UUID skuId, int quantity) {
}
