package dev.ebaptistella.monolith.modules.order.wire.out;

import dev.ebaptistella.monolith.modules.order.models.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID customerId,
        OrderStatus status,
        BigDecimal totalAmount,
        String currency,
        List<OrderLineResponse> lines,
        Instant createdAt,
        Instant updatedAt) {
}
