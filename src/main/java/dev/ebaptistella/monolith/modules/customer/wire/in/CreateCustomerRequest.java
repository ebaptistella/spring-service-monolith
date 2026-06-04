package dev.ebaptistella.monolith.modules.customer.wire.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateCustomerRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(max = 200) String fullName
) {
}
