package dev.ebaptistella.monolith.modules.order.diplomat.outbound;

import dev.ebaptistella.monolith.shared.contracts.catalog.CatalogQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LocalCatalogGateway implements CatalogGateway {

    private final CatalogQuery catalogQuery;

    @Override
    public boolean isSkuActive(UUID skuId) {
        return catalogQuery.isSkuActive(skuId);
    }

    @Override
    public Optional<BigDecimal> getListPrice(UUID skuId) {
        return catalogQuery.getListPrice(skuId);
    }
}
