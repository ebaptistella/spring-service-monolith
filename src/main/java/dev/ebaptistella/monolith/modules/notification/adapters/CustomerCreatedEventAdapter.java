package dev.ebaptistella.monolith.modules.notification.adapters;

import dev.ebaptistella.monolith.modules.notification.models.CustomerCreatedNotification;
import dev.ebaptistella.monolith.shared.wire.in.events.CustomerCreatedEvent;

public final class CustomerCreatedEventAdapter {

    private CustomerCreatedEventAdapter() {
    }

    public static CustomerCreatedNotification wireToModel(CustomerCreatedEvent wire) {
        return new CustomerCreatedNotification(
                wire.idempotencyKey(), wire.customerId(), wire.email(), wire.fullName());
    }
}
