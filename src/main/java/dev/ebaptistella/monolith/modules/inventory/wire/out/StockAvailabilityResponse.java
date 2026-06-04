package dev.ebaptistella.monolith.modules.inventory.wire.out;

import java.util.UUID;

public record StockAvailabilityResponse(UUID skuId, int onHand, int reserved, int available) {
}
