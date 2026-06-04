package dev.ebaptistella.monolith.modules.order.wire.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlaceOrderRequest(
        @NotNull UUID customerId,
        @NotEmpty @Valid List<PlaceOrderLineRequest> lines) {
}
