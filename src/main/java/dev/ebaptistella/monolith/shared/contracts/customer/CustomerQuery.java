package dev.ebaptistella.monolith.shared.contracts.customer;

import java.util.Optional;
import java.util.UUID;

public interface CustomerQuery {

    boolean exists(UUID customerId);

    Optional<String> findEmail(UUID customerId);
}
