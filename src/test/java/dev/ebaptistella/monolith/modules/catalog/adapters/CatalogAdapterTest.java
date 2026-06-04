package dev.ebaptistella.monolith.modules.catalog.adapters;

import dev.ebaptistella.monolith.modules.catalog.models.CreateSkuInput;
import dev.ebaptistella.monolith.modules.catalog.models.Sku;
import dev.ebaptistella.monolith.modules.catalog.wire.in.CreateSkuRequest;
import dev.ebaptistella.monolith.modules.catalog.wire.out.SkuResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class CatalogAdapterTest {

    @Test
    void wireInToCreateSkuInput_mapsFields() {
        CreateSkuRequest request = new CreateSkuRequest(
                "Widget",
                "A useful widget",
                "SKU-001",
                new BigDecimal("19.99"));

        CreateSkuInput input = CatalogAdapter.wireInToCreateSkuInput(request);

        assertThat(input.productName()).isEqualTo("Widget");
        assertThat(input.productDescription()).isEqualTo("A useful widget");
        assertThat(input.code()).isEqualTo("SKU-001");
        assertThat(input.listPrice()).isEqualByComparingTo("19.99");
    }

    @Test
    void modelToWireOut_mapsFields() {
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Sku sku = new Sku(id, productId, "SKU-001", new BigDecimal("19.99"), true, createdAt, null, null, null);

        SkuResponse response = CatalogAdapter.modelToWireOut(sku);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.productId()).isEqualTo(productId);
        assertThat(response.code()).isEqualTo("SKU-001");
        assertThat(response.listPrice()).isEqualByComparingTo("19.99");
        assertThat(response.active()).isTrue();
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }
}
