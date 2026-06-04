package dev.ebaptistella.monolith.modules.order.models;

import java.util.UUID;

public record PlaceOrderLineInput(UUID skuId, int quantity) {
}
