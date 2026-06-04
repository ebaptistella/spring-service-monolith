package dev.ebaptistella.monolith.modules.inventory.models;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderPlacedInventoryInput(
        UUID eventIdempotencyKey, UUID orderId, List<StockReserveLine> lines, BigDecimal totalAmount) {

    public StockReserveInput toReserveInput() {
        return new StockReserveInput(eventIdempotencyKey, orderId, lines);
    }
}
