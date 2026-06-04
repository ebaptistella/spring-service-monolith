package dev.ebaptistella.monolith.modules.customer.diplomat.inbound;

import dev.ebaptistella.monolith.modules.customer.diplomat.jpa.CustomerPersistence;
import dev.ebaptistella.monolith.shared.contracts.customer.CustomerQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LocalCustomerQuery implements CustomerQuery {

    private final CustomerPersistence persistence;

    @Override
    public boolean exists(UUID customerId) {
        return persistence.existsById(customerId);
    }

    @Override
    public Optional<String> findEmail(UUID customerId) {
        return persistence.findById(customerId).map(customer -> customer.email());
    }
}
