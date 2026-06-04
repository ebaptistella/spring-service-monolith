package dev.ebaptistella.monolith.modules.inventory.adapters;

import dev.ebaptistella.monolith.modules.inventory.models.AdjustStockInput;
import dev.ebaptistella.monolith.modules.inventory.models.StockLevel;
import dev.ebaptistella.monolith.modules.inventory.wire.in.AdjustStockRequest;
import dev.ebaptistella.monolith.modules.inventory.wire.out.StockAvailabilityResponse;

public final class InventoryAdapter {

    private InventoryAdapter() {
    }

    public static AdjustStockInput wireInToAdjustInput(AdjustStockRequest request) {
        return new AdjustStockInput(request.skuId(), request.quantityDelta());
    }

    public static StockAvailabilityResponse modelToWireOut(StockLevel level) {
        int available = Math.max(0, level.onHand() - level.reserved());
        return new StockAvailabilityResponse(
                level.skuId(), level.onHand(), level.reserved(), available);
    }
}
