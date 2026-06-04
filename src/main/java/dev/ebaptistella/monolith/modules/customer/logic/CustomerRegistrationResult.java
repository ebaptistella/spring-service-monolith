package dev.ebaptistella.monolith.modules.customer.logic;

import dev.ebaptistella.monolith.modules.customer.models.Customer;

import java.util.Optional;

public record CustomerRegistrationResult(
        Optional<Customer> customer,
        Optional<String> error,
        boolean replay
) {

    public static CustomerRegistrationResult success(Customer customer) {
        return new CustomerRegistrationResult(Optional.of(customer), Optional.empty(), false);
    }

    public static CustomerRegistrationResult replay(Customer customer) {
        return new CustomerRegistrationResult(Optional.of(customer), Optional.empty(), true);
    }

    public static CustomerRegistrationResult duplicateEmail(String email) {
        return new CustomerRegistrationResult(
                Optional.empty(),
                Optional.of("Email already registered: " + email),
                false);
    }

    public boolean rejected() {
        return error.isPresent();
    }
}
