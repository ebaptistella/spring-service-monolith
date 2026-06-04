package dev.ebaptistella.monolith.modules.order.diplomat.outbound;

import java.util.Optional;
import java.util.UUID;

public interface CustomerGateway {

    boolean exists(UUID customerId);

    Optional<String> findEmail(UUID customerId);
}
