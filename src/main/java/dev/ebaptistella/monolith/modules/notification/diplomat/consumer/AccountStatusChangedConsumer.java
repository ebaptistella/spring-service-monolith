package dev.ebaptistella.monolith.modules.notification.diplomat.consumer;

import dev.ebaptistella.monolith.modules.notification.adapters.AccountStatusChangedEventAdapter;
import dev.ebaptistella.monolith.modules.notification.controllers.AccountStatusNotificationController;
import dev.ebaptistella.monolith.shared.EventRoutes;
import dev.ebaptistella.monolith.shared.wire.in.events.AccountStatusChangedEvent;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountStatusChangedConsumer {

    private final AccountStatusNotificationController accountStatusNotificationController;

    @Observed(name = "rabbit.receive", contextualName = "account-status-changed consume")
    @RabbitListener(queues = EventRoutes.ACCOUNT_STATUS_CHANGED_QUEUE)
    public void onAccountStatusChanged(AccountStatusChangedEvent event) {
        accountStatusNotificationController.notifyStatusChanged(
                AccountStatusChangedEventAdapter.wireToModel(event));
    }
}
