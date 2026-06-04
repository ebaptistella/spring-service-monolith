package dev.ebaptistella.monolith.shared.contracts.catalog;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface CatalogQuery {

    boolean isSkuActive(UUID skuId);

    Optional<BigDecimal> getListPrice(UUID skuId);
}
