package dev.ebaptistella.monolith.modules.order.diplomat.outbound;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface CatalogGateway {

    boolean isSkuActive(UUID skuId);

    Optional<BigDecimal> getListPrice(UUID skuId);
}
