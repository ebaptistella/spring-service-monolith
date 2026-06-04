package dev.ebaptistella.monolith.modules.order.diplomat.outbound;

import dev.ebaptistella.monolith.shared.contracts.customer.CustomerQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LocalCustomerGateway implements CustomerGateway {

    private final CustomerQuery customerQuery;

    @Override
    public boolean exists(UUID customerId) {
        return customerQuery.exists(customerId);
    }

    @Override
    public Optional<String> findEmail(UUID customerId) {
        return customerQuery.findEmail(customerId);
    }
}
