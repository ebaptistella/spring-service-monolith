package dev.ebaptistella.monolith.modules.customer.logic;

import dev.ebaptistella.monolith.modules.customer.models.Customer;
import dev.ebaptistella.monolith.modules.customer.models.CustomerRegistrationInput;

import java.time.Instant;
import java.util.UUID;

public final class CustomerRules {

    private CustomerRules() {
    }

    public static CustomerRegistrationResult register(CustomerRegistrationInput input, boolean emailExists) {
        if (emailExists) {
            return CustomerRegistrationResult.duplicateEmail(input.email());
        }

        Customer customer = new Customer(
                UUID.randomUUID(),
                input.email(),
                input.fullName(),
                Instant.now(),
                null,
                null,
                null);

        return CustomerRegistrationResult.success(customer);
    }
}
