package dev.ebaptistella.monolith.config.rabbit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;

@Tag("unit")
class SentryDeadLetterReporterTest {

    @Test
    void reportDoesNotThrowWhenXDeathIsPresent() {
        SentryDeadLetterReporter reporter = new SentryDeadLetterReporter(new RabbitDeadLetterProperties(true, 100));

        MessageProperties properties = new MessageProperties();
        properties.setHeader(
                "x-death",
                List.of(Map.of(
                        "exchange", "monolith.customer-created",
                        "queue", "monolith.customer-created.queue",
                        "reason", "rejected")));
        properties.setReceivedRoutingKey("monolith.customer-created");
        properties.setMessageId("msg-1");
        properties.setContentType("application/json");

        Message message = new Message("{\"id\":\"1\"}".getBytes(StandardCharsets.UTF_8), properties);

        assertThatCode(() -> reporter.report("monolith.customer-created.dlq", message))
                .doesNotThrowAnyException();
    }

    @Test
    void reportDoesNotThrowWhenXDeathIsMissing() {
        SentryDeadLetterReporter reporter = new SentryDeadLetterReporter(new RabbitDeadLetterProperties(true, 50));

        Message message = new Message("payload".getBytes(StandardCharsets.UTF_8), new MessageProperties());

        assertThatCode(() -> reporter.report("monolith.email-dispatch-requested.dlq", message))
                .doesNotThrowAnyException();
    }
}
