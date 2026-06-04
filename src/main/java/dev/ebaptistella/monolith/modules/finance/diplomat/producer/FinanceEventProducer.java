package dev.ebaptistella.monolith.modules.finance.diplomat.producer;

import dev.ebaptistella.monolith.modules.finance.adapters.PaymentEventAdapter;
import dev.ebaptistella.monolith.modules.finance.models.PaymentIntent;
import dev.ebaptistella.monolith.shared.wire.in.events.PaymentCapturedEvent;
import dev.ebaptistella.monolith.shared.wire.in.events.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FinanceEventProducer {

    private final ApplicationEventPublisher events;

    public void publishPaymentCaptured(PaymentIntent intent) {
        PaymentCapturedEvent wire = PaymentEventAdapter.toPaymentCaptured(intent);
        events.publishEvent(wire);
    }

    public void publishPaymentFailed(PaymentIntent intent, String reason) {
        PaymentFailedEvent wire = PaymentEventAdapter.toPaymentFailed(intent, reason);
        events.publishEvent(wire);
    }
}
