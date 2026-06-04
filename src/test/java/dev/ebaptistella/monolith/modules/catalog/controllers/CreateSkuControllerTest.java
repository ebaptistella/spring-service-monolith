package dev.ebaptistella.monolith.modules.catalog.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ebaptistella.monolith.modules.catalog.diplomat.jpa.CatalogPersistence;
import dev.ebaptistella.monolith.modules.catalog.logic.CreateSkuResult;
import dev.ebaptistella.monolith.modules.catalog.models.CreateSkuInput;
import dev.ebaptistella.monolith.modules.catalog.models.Product;
import dev.ebaptistella.monolith.modules.catalog.models.Sku;
import dev.ebaptistella.monolith.support.IdempotencyTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CreateSkuControllerTest {

    @Mock
    private CatalogPersistence jpa;

    private CreateSkuController controller;

    @BeforeEach
    void setUp() {
        controller = new CreateSkuController(jpa, new ObjectMapper());
    }

    @Test
    void create_persistsProductAndSku() {
        IdempotencyTestSupport.runWithRootKey(() -> {
            CreateSkuInput input = new CreateSkuInput(
                    "Widget",
                    "A useful widget",
                    "SKU-001",
                    new BigDecimal("19.99"));
            when(jpa.findSkuByIdempotencyKey(any())).thenReturn(Optional.empty());
            when(jpa.saveProductAndSku(any(Product.class), any(Sku.class)))
                    .thenAnswer(invocation -> invocation.getArgument(1));

            CreateSkuResult result = controller.create(input);

            assertThat(result.rejected()).isFalse();
            assertThat(result.replay()).isFalse();
            assertThat(result.created()).map(created -> created.sku().code()).contains("SKU-001");
            verify(jpa).saveProductAndSku(any(Product.class), any(Sku.class));
        });
    }

    @Test
    void create_rejectsInvalidInput() {
        IdempotencyTestSupport.runWithRootKey(() -> {
            CreateSkuInput input = new CreateSkuInput("Widget", null, "", new BigDecimal("19.99"));
            when(jpa.findSkuByIdempotencyKey(any())).thenReturn(Optional.empty());

            CreateSkuResult result = controller.create(input);

            assertThat(result.rejected()).isTrue();
            verify(jpa, never()).saveProductAndSku(any(), any());
        });
    }
}
