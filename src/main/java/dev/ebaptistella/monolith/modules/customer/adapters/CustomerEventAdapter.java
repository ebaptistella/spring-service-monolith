package dev.ebaptistella.monolith.modules.customer.adapters;

import dev.ebaptistella.monolith.modules.customer.models.Customer;
import dev.ebaptistella.monolith.shared.wire.in.events.CustomerCreatedEvent;

public final class CustomerEventAdapter {

    private CustomerEventAdapter() {
    }

    public static CustomerCreatedEvent modelToWire(Customer customer) {
        return new CustomerCreatedEvent(
                customer.rootIdempotencyKey(),
                customer.id(),
                customer.email(),
                customer.fullName());
    }
}
