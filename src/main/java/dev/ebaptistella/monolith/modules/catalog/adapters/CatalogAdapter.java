package dev.ebaptistella.monolith.modules.catalog.adapters;

import dev.ebaptistella.monolith.modules.catalog.models.CreateSkuInput;
import dev.ebaptistella.monolith.modules.catalog.models.Sku;
import dev.ebaptistella.monolith.modules.catalog.wire.in.CreateSkuRequest;
import dev.ebaptistella.monolith.modules.catalog.wire.out.SkuResponse;

public final class CatalogAdapter {

    private CatalogAdapter() {
    }

    public static CreateSkuInput wireInToCreateSkuInput(CreateSkuRequest request) {
        return new CreateSkuInput(
                request.productName(),
                request.productDescription(),
                request.code(),
                request.listPrice());
    }

    public static SkuResponse modelToWireOut(Sku sku) {
        return SkuResponse.from(sku);
    }
}
