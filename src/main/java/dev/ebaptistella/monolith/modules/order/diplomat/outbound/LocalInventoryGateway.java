package dev.ebaptistella.monolith.modules.order.diplomat.outbound;

import dev.ebaptistella.monolith.shared.contracts.inventory.StockAvailabilityQuery;
import dev.ebaptistella.monolith.shared.contracts.inventory.StockLineRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LocalInventoryGateway implements InventoryGateway {

    private final StockAvailabilityQuery stockAvailabilityQuery;

    @Override
    public int available(UUID skuId) {
        return stockAvailabilityQuery.available(skuId);
    }

    @Override
    public boolean hasAvailabilityForLines(List<StockLineRequest> lines) {
        return stockAvailabilityQuery.hasAvailabilityForLines(lines);
    }
}
