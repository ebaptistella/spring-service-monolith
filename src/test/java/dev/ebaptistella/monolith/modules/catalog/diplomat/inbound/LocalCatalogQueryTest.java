package dev.ebaptistella.monolith.modules.catalog.diplomat.inbound;

import dev.ebaptistella.monolith.modules.catalog.diplomat.jpa.CatalogPersistence;
import dev.ebaptistella.monolith.modules.catalog.models.Sku;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class LocalCatalogQueryTest {

    @Mock
    private CatalogPersistence persistence;

    @InjectMocks
    private LocalCatalogQuery query;

    @Test
    void isSkuActive_whenSkuMissing_returnsFalse() {
        UUID skuId = UUID.randomUUID();
        when(persistence.findSkuById(skuId)).thenReturn(Optional.empty());

        assertThat(query.isSkuActive(skuId)).isFalse();
    }

    @Test
    void isSkuActive_whenSkuActive_returnsTrue() {
        UUID skuId = UUID.randomUUID();
        when(persistence.findSkuById(skuId))
                .thenReturn(Optional.of(new Sku(
                        skuId,
                        UUID.randomUUID(),
                        "SKU-1",
                        new BigDecimal("10.00"),
                        true,
                        java.time.Instant.now(),
                        null,
                        null,
                        null)));

        assertThat(query.isSkuActive(skuId)).isTrue();
    }

    @Test
    void getListPrice_whenSkuPresent_returnsPrice() {
        UUID skuId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("49.90");
        when(persistence.findSkuById(skuId))
                .thenReturn(Optional.of(new Sku(
                        skuId,
                        UUID.randomUUID(),
                        "SKU-1",
                        price,
                        true,
                        java.time.Instant.now(),
                        null,
                        null,
                        null)));

        assertThat(query.getListPrice(skuId)).contains(price);
    }
}
