package dev.ebaptistella.monolith.modules.order.models;

import java.math.BigDecimal;
import java.util.UUID;

public record PlaceOrderResolvedLine(UUID skuId, int quantity, BigDecimal unitPrice) {
}
