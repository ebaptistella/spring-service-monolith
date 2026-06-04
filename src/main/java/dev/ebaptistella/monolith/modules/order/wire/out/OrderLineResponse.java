package dev.ebaptistella.monolith.modules.order.wire.out;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderLineResponse(
        UUID id,
        UUID skuId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal) {
}
