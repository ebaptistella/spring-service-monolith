package dev.ebaptistella.monolith.modules.catalog.adapters;

import dev.ebaptistella.monolith.modules.catalog.models.Sku;

import java.math.BigDecimal;
import java.util.Optional;

public final class CatalogQueryAdapter {

    private CatalogQueryAdapter() {
    }

    public static boolean isSkuActive(Sku sku) {
        return sku.active();
    }

    public static Optional<BigDecimal> getListPrice(Sku sku) {
        return Optional.of(sku.listPrice());
    }
}
