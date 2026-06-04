package dev.ebaptistella.monolith.modules.catalog.logic;

import dev.ebaptistella.monolith.modules.catalog.models.CreateSkuInput;
import dev.ebaptistella.monolith.modules.catalog.models.Product;
import dev.ebaptistella.monolith.modules.catalog.models.Sku;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class CatalogRules {

    private CatalogRules() {
    }

    public static Optional<String> validateCreateSku(String code, BigDecimal listPrice) {
        if (code == null || code.isBlank()) {
            return Optional.of("SKU code must not be blank");
        }
        if (listPrice == null || listPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.of("List price must be positive");
        }
        return Optional.empty();
    }

    public static CreateSkuResult createSku(CreateSkuInput input) {
        Optional<String> validationError = validateCreateSku(input.code(), input.listPrice());
        if (validationError.isPresent()) {
            return CreateSkuResult.rejected(validationError.get());
        }

        Instant now = Instant.now();
        Product product = new Product(
                UUID.randomUUID(),
                input.productName(),
                input.productDescription(),
                true,
                now,
                null,
                null,
                null);
        Sku sku = new Sku(
                UUID.randomUUID(),
                product.id(),
                input.code().trim(),
                input.listPrice(),
                true,
                now,
                null,
                null,
                null);

        return CreateSkuResult.success(new CreatedSku(product, sku));
    }
}
