package dev.ebaptistella.monolith.modules.finance.diplomat.consumer;

import dev.ebaptistella.monolith.modules.finance.adapters.PaymentEventAdapter;
import dev.ebaptistella.monolith.modules.finance.controllers.CapturePaymentController;
import dev.ebaptistella.monolith.modules.finance.models.CapturePaymentInput;
import dev.ebaptistella.monolith.shared.EventRoutes;
import dev.ebaptistella.monolith.shared.wire.in.events.StockReservedEvent;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component("financeStockReservedConsumer")
@RequiredArgsConstructor
public class StockReservedConsumer {

    private final CapturePaymentController capturePaymentController;

    @Observed(name = "rabbit.receive", contextualName = "stock-reserved consume")
    @RabbitListener(queues = EventRoutes.STOCK_RESERVED_FINANCE_QUEUE)
    public void onStockReserved(StockReservedEvent event) {
        CapturePaymentInput input = PaymentEventAdapter.stockReservedToCaptureInput(event);
        capturePaymentController.capture(input);
    }
}
