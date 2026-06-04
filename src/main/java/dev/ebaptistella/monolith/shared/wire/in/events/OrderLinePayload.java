package dev.ebaptistella.monolith.shared.wire.in.events;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderLinePayload(UUID skuId, int quantity, BigDecimal unitPrice) {
}
