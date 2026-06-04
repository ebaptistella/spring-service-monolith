package dev.ebaptistella.monolith.shared.contracts.inventory;

import java.util.List;
import java.util.UUID;

public interface StockAvailabilityQuery {

    int available(UUID skuId);

    boolean hasAvailabilityForLines(List<StockLineRequest> lines);
}
