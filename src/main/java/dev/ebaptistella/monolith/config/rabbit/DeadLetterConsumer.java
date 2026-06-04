package dev.ebaptistella.monolith.config.rabbit;

import dev.ebaptistella.monolith.shared.EventRoutes;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.rabbit.dead-letter", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DeadLetterConsumer {

    private final DeadLetterReporter reporter;

    @Observed(name = "rabbit.dead-letter.consume", contextualName = "dead-letter consume")
    @RabbitListener(
            queues = {
                EventRoutes.CUSTOMER_CREATED_DLQ,
                EventRoutes.EMAIL_DISPATCH_DLQ,
                EventRoutes.ORDER_PLACED_DLQ,
                EventRoutes.STOCK_RESERVED_ORDER_DLQ,
                EventRoutes.STOCK_RESERVED_FINANCE_DLQ,
                EventRoutes.STOCK_RESERVATION_FAILED_DLQ,
                EventRoutes.PAYMENT_CAPTURED_DLQ,
                EventRoutes.PAYMENT_FAILED_DLQ,
                EventRoutes.ORDER_CONFIRMED_INVENTORY_DLQ,
                EventRoutes.ORDER_CONFIRMED_NOTIFICATION_DLQ,
                EventRoutes.ORDER_CANCELLED_INVENTORY_DLQ,
                EventRoutes.ORDER_CANCELLED_FINANCE_DLQ
            },
            containerFactory = "deadLetterListenerContainerFactory")
    public void onDeadLetter(Message message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue) {
        log.warn("Dead-letter message received queue={}", queue);
        reporter.report(queue, message);
    }
}
