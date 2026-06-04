package dev.ebaptistella.monolith.modules.catalog.diplomat.inbound;

import dev.ebaptistella.monolith.modules.catalog.adapters.CatalogQueryAdapter;
import dev.ebaptistella.monolith.modules.catalog.diplomat.jpa.CatalogPersistence;
import dev.ebaptistella.monolith.shared.contracts.catalog.CatalogQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LocalCatalogQuery implements CatalogQuery {

    private final CatalogPersistence persistence;

    @Override
    public boolean isSkuActive(UUID skuId) {
        return persistence.findSkuById(skuId)
                .map(CatalogQueryAdapter::isSkuActive)
                .orElse(false);
    }

    @Override
    public Optional<BigDecimal> getListPrice(UUID skuId) {
        return persistence.findSkuById(skuId)
                .flatMap(CatalogQueryAdapter::getListPrice);
    }
}
