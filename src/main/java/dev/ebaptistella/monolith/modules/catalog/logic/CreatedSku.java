package dev.ebaptistella.monolith.modules.catalog.logic;

import dev.ebaptistella.monolith.modules.catalog.models.Product;
import dev.ebaptistella.monolith.modules.catalog.models.Sku;

public record CreatedSku(Product product, Sku sku) {
}
