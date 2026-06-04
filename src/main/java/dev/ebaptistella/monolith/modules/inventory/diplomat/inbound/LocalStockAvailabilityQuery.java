package dev.ebaptistella.monolith.modules.inventory.diplomat.inbound;

import dev.ebaptistella.monolith.modules.inventory.diplomat.jpa.InventoryPersistence;
import dev.ebaptistella.monolith.modules.inventory.logic.StockRules;
import dev.ebaptistella.monolith.modules.inventory.models.StockLevel;
import dev.ebaptistella.monolith.shared.contracts.inventory.StockAvailabilityQuery;
import dev.ebaptistella.monolith.shared.contracts.inventory.StockLineRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LocalStockAvailabilityQuery implements StockAvailabilityQuery {

    private final InventoryPersistence persistence;

    @Override
    public int available(UUID skuId) {
        StockLevel level = persistence.getOrCreateLevel(skuId);
        return StockRules.available(level.onHand(), level.reserved());
    }

    @Override
    public boolean hasAvailabilityForLines(List<StockLineRequest> lines) {
        return lines.stream()
                .allMatch(line -> available(line.skuId()) >= line.quantity());
    }
}
