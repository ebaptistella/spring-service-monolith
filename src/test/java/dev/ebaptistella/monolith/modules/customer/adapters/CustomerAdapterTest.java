package dev.ebaptistella.monolith.modules.customer.adapters;

import dev.ebaptistella.monolith.modules.customer.models.CustomerRegistrationInput;
import dev.ebaptistella.monolith.modules.customer.models.Customer;
import dev.ebaptistella.monolith.modules.customer.wire.in.CreateCustomerRequest;
import dev.ebaptistella.monolith.modules.customer.wire.out.CustomerResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class CustomerAdapterTest {

    @Test
    void wireInToRegistrationInput_mapsFields() {
        CreateCustomerRequest request = new CreateCustomerRequest("ana@example.com", "Ana Baptista");

        CustomerRegistrationInput input = CustomerAdapter.wireInToRegistrationInput(request);

        assertThat(input.email()).isEqualTo("ana@example.com");
        assertThat(input.fullName()).isEqualTo("Ana Baptista");
    }

    @Test
    void modelToWireOut_mapsFields() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Customer customer = new Customer(id, "ana@example.com", "Ana Baptista", createdAt, null, null, null);

        CustomerResponse response = CustomerAdapter.modelToWireOut(customer);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.email()).isEqualTo("ana@example.com");
        assertThat(response.fullName()).isEqualTo("Ana Baptista");
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }
}
