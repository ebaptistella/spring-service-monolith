package dev.ebaptistella.monolith.modules.catalog.logic;

import dev.ebaptistella.monolith.modules.catalog.models.CreateSkuInput;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class CatalogRulesTest {

    @Test
    void validateCreateSku_rejectsBlankCode() {
        assertThat(CatalogRules.validateCreateSku("  ", new BigDecimal("10.00")))
                .hasValueSatisfying(error -> assertThat(error).contains("blank"));
    }

    @Test
    void validateCreateSku_rejectsNonPositivePrice() {
        assertThat(CatalogRules.validateCreateSku("SKU-001", BigDecimal.ZERO))
                .hasValueSatisfying(error -> assertThat(error).contains("positive"));
        assertThat(CatalogRules.validateCreateSku("SKU-001", new BigDecimal("-1.00")))
                .hasValueSatisfying(error -> assertThat(error).contains("positive"));
    }

    @Test
    void createSku_createsProductAndSkuWhenValid() {
        CreateSkuInput input = new CreateSkuInput(
                "Widget",
                "A useful widget",
                "SKU-001",
                new BigDecimal("19.99"));

        CreateSkuResult result = CatalogRules.createSku(input);

        assertThat(result.rejected()).isFalse();
        CreatedSku created = result.created().orElseThrow();
        assertThat(created.product().name()).isEqualTo("Widget");
        assertThat(created.product().description()).isEqualTo("A useful widget");
        assertThat(created.product().active()).isTrue();
        assertThat(created.sku().code()).isEqualTo("SKU-001");
        assertThat(created.sku().listPrice()).isEqualByComparingTo("19.99");
        assertThat(created.sku().productId()).isEqualTo(created.product().id());
        assertThat(created.sku().active()).isTrue();
        assertThat(created.sku().id()).isNotNull();
        assertThat(created.sku().createdAt()).isNotNull();
    }

    @Test
    void createSku_rejectsInvalidInput() {
        CreateSkuInput input = new CreateSkuInput("Widget", null, "", new BigDecimal("19.99"));

        CreateSkuResult result = CatalogRules.createSku(input);

        assertThat(result.rejected()).isTrue();
        assertThat(result.created()).isEmpty();
    }
}
