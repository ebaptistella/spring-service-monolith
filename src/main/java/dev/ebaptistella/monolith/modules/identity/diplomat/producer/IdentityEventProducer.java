package dev.ebaptistella.monolith.modules.identity.diplomat.producer;

import dev.ebaptistella.monolith.modules.identity.models.Account;
import dev.ebaptistella.monolith.shared.wire.in.events.AccountStatusChangedEvent;
import dev.ebaptistella.monolith.shared.wire.in.events.LocalAccountRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdentityEventProducer {

    private final ApplicationEventPublisher events;

    public void publishLocalAccountRegistered(Account account) {
        events.publishEvent(new LocalAccountRegisteredEvent(
                account.rootIdempotencyKey(), account.id(), account.email()));
    }

    public void publishAccountStatusChanged(Account account) {
        events.publishEvent(new AccountStatusChangedEvent(
                account.rootIdempotencyKey(), account.id(), account.email(), account.status().name()));
    }
}
