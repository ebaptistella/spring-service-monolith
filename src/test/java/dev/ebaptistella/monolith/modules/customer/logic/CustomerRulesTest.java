package dev.ebaptistella.monolith.modules.customer.logic;

import dev.ebaptistella.monolith.modules.customer.models.Customer;
import dev.ebaptistella.monolith.modules.customer.models.CustomerRegistrationInput;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class CustomerRulesTest {

    @Test
    void register_createsCustomerWhenEmailIsAvailable() {
        CustomerRegistrationInput input = new CustomerRegistrationInput("ana@example.com", "Ana Baptista");

        CustomerRegistrationResult result = CustomerRules.register(input, false);

        assertThat(result.rejected()).isFalse();
        Customer customer = result.customer().orElseThrow();
        assertThat(customer.email()).isEqualTo("ana@example.com");
        assertThat(customer.fullName()).isEqualTo("Ana Baptista");
        assertThat(customer.id()).isNotNull();
        assertThat(customer.createdAt()).isNotNull();
    }

    @Test
    void register_rejectsDuplicateEmail() {
        CustomerRegistrationInput input = new CustomerRegistrationInput("ana@example.com", "Ana Baptista");

        CustomerRegistrationResult result = CustomerRules.register(input, true);

        assertThat(result.rejected()).isTrue();
        assertThat(result.error()).hasValueSatisfying(error -> assertThat(error).contains("Email already registered"));
        assertThat(result.customer()).isEmpty();
    }
}
