package dev.ebaptistella.monolith.config.rabbit;

import dev.ebaptistella.monolith.shared.EventRoutes;
import dev.ebaptistella.monolith.support.TestProfileIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@Tag("integration")
class RabbitDeadLetterRoutingIT extends TestProfileIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private DeadLetterReporter deadLetterReporter;

    @Test
    void unprocessableMessageIsRoutedToDeadLetterQueueAndReported() {
        rabbitTemplate.convertAndSend(
                EventRoutes.CUSTOMER_CREATED_EXCHANGE,
                EventRoutes.CUSTOMER_CREATED_ROUTING_KEY,
                "not-valid-json",
                message -> {
                    message.getMessageProperties().setContentType("application/json");
                    return message;
                });

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> verify(deadLetterReporter)
                .report(eq(EventRoutes.CUSTOMER_CREATED_DLQ), any(Message.class)));
    }
}
