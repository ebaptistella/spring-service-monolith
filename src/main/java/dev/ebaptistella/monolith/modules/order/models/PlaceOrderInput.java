package dev.ebaptistella.monolith.modules.order.models;

import java.util.List;
import java.util.UUID;

public record PlaceOrderInput(UUID customerId, List<PlaceOrderLineInput> lines) {
}
