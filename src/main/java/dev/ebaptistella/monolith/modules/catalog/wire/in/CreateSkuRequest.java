package dev.ebaptistella.monolith.modules.catalog.wire.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateSkuRequest(
        @NotBlank @Size(max = 200) String productName,
        String productDescription,
        @NotBlank @Size(max = 64) String code,
        @NotNull @Positive BigDecimal listPrice
) {
}
