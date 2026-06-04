package dev.ebaptistella.monolith.modules.customer.diplomat.producer;

import dev.ebaptistella.monolith.modules.customer.adapters.CustomerEventAdapter;
import dev.ebaptistella.monolith.modules.customer.models.Customer;
import dev.ebaptistella.monolith.shared.wire.in.events.CustomerCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerEventProducer {

    private final ApplicationEventPublisher events;

    public void publishCustomerCreated(Customer customer) {
        CustomerCreatedEvent wire = CustomerEventAdapter.modelToWire(customer);
        events.publishEvent(wire);
    }
}
