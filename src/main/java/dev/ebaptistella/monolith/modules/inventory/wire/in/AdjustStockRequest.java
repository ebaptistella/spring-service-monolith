package dev.ebaptistella.monolith.modules.inventory.wire.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdjustStockRequest(@NotNull UUID skuId, int quantityDelta) {
}
