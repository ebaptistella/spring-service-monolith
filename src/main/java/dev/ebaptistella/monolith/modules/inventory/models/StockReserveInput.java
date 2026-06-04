package dev.ebaptistella.monolith.modules.inventory.models;

import java.util.List;
import java.util.UUID;

public record StockReserveInput(UUID eventIdempotencyKey, UUID orderId, List<StockReserveLine> lines) {
}
