package dev.ebaptistella.monolith.modules.email.diplomat.consumer;

import dev.ebaptistella.monolith.modules.email.adapters.EmailDispatchEventAdapter;
import dev.ebaptistella.monolith.modules.email.controllers.SendEmailController;
import dev.ebaptistella.monolith.shared.EventRoutes;
import dev.ebaptistella.monolith.shared.wire.in.events.EmailDispatchRequestedEvent;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailDispatchConsumer {

    private final SendEmailController sendEmailController;

    @Observed(name = "rabbit.receive", contextualName = "email-dispatch consume")
    @RabbitListener(queues = EventRoutes.EMAIL_DISPATCH_QUEUE)
    public void onEmailDispatchRequested(EmailDispatchRequestedEvent event) {
        sendEmailController.dispatch(EmailDispatchEventAdapter.wireToModel(event));
    }
}
