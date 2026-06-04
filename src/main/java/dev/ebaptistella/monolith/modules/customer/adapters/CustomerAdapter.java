package dev.ebaptistella.monolith.modules.customer.adapters;

import dev.ebaptistella.monolith.modules.customer.models.CustomerRegistrationInput;
import dev.ebaptistella.monolith.modules.customer.models.Customer;
import dev.ebaptistella.monolith.modules.customer.wire.in.CreateCustomerRequest;
import dev.ebaptistella.monolith.modules.customer.wire.out.CustomerResponse;

public final class CustomerAdapter {

    private CustomerAdapter() {
    }

    public static CustomerRegistrationInput wireInToRegistrationInput(CreateCustomerRequest request) {
        return new CustomerRegistrationInput(request.email(), request.fullName());
    }

    public static CustomerResponse modelToWireOut(Customer customer) {
        return CustomerResponse.from(customer);
    }
}
