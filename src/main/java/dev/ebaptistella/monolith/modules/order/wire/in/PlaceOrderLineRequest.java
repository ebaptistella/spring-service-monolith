package dev.ebaptistella.monolith.modules.order.wire.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlaceOrderLineRequest(@NotNull UUID skuId, @Positive int quantity) {
}
