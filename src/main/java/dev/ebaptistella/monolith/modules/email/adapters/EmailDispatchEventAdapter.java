package dev.ebaptistella.monolith.modules.email.adapters;

import dev.ebaptistella.monolith.modules.email.models.EmailDispatch;
import dev.ebaptistella.monolith.shared.wire.in.events.EmailDispatchRequestedEvent;

public final class EmailDispatchEventAdapter {

    private EmailDispatchEventAdapter() {
    }

    public static EmailDispatch wireToModel(EmailDispatchRequestedEvent wire) {
        return new EmailDispatch(wire.dispatchId(), wire.to(), wire.subject(), wire.body());
    }
}
