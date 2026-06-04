package dev.ebaptistella.monolith.modules.finance.models;

import java.math.BigDecimal;
import java.util.UUID;

public record CapturePaymentInput(UUID eventIdempotencyKey, UUID orderId, BigDecimal amount) {
}
