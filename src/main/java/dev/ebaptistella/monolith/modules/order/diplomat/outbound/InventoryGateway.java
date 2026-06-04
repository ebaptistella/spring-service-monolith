package dev.ebaptistella.monolith.modules.order.diplomat.outbound;

import dev.ebaptistella.monolith.shared.contracts.inventory.StockLineRequest;

import java.util.List;
import java.util.UUID;

public interface InventoryGateway {

    int available(UUID skuId);

    boolean hasAvailabilityForLines(List<StockLineRequest> lines);
}
