package dev.ebaptistella.monolith.modules.order.models;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderLine(
        UUID id,
        UUID orderId,
        UUID skuId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal) {
}
