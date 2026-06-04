package dev.ebaptistella.monolith.modules.catalog.models;

import java.math.BigDecimal;

public record CreateSkuInput(
        String productName,
        String productDescription,
        String code,
        BigDecimal listPrice
) {
}
