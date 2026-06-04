package dev.ebaptistella.monolith.modules.catalog.adapters;

import dev.ebaptistella.monolith.modules.catalog.models.Sku;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class CatalogQueryAdapterTest {

    @Test
    void isSkuActive_reflectsSkuState() {
        Sku activeSku = new Sku(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "SKU-001",
                new BigDecimal("10.00"),
                true,
                Instant.now(),
                null,
                null,
                null);
        Sku inactiveSku = new Sku(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "SKU-002",
                new BigDecimal("10.00"),
                false,
                Instant.now(),
                null,
                null,
                null);

        assertThat(CatalogQueryAdapter.isSkuActive(activeSku)).isTrue();
        assertThat(CatalogQueryAdapter.isSkuActive(inactiveSku)).isFalse();
    }

    @Test
    void getListPrice_returnsSkuPrice() {
        BigDecimal listPrice = new BigDecimal("29.99");
        Sku sku = new Sku(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "SKU-001",
                listPrice,
                true,
                Instant.now(),
                null,
                null,
                null);

        assertThat(CatalogQueryAdapter.getListPrice(sku)).contains(listPrice);
    }
}
