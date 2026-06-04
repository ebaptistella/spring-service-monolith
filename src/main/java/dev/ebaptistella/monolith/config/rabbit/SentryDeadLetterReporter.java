package dev.ebaptistella.monolith.config.rabbit;

import dev.ebaptistella.monolith.shared.observability.TraceContextSupport;
import io.micrometer.observation.annotation.Observed;
import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryLevel;
import io.sentry.protocol.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SentryDeadLetterReporter implements DeadLetterReporter {

    private final RabbitDeadLetterProperties properties;

    @Override
    @Observed(name = "rabbit.dead-letter.report", contextualName = "dead-letter sentry report")
    public void report(String deadLetterQueue, org.springframework.amqp.core.Message amqpMessage) {
        MessageProperties messageProperties = amqpMessage.getMessageProperties();
        String body = extractBody(amqpMessage);
        Map<String, Object> headers = messageProperties != null ? messageProperties.getHeaders() : Map.of();
        Map<String, Object> death = firstDeath(headers);

        String sourceExchange = stringValue(death.get("exchange"), "unknown");
        String sourceQueue = stringValue(death.get("queue"), "unknown");
        String reason = stringValue(death.get("reason"), "unknown");

        log.error(
                "RabbitMQ message dead-lettered queue={} sourceExchange={} sourceQueue={} reason={} body={}",
                deadLetterQueue,
                sourceExchange,
                sourceQueue,
                reason,
                truncate(body));

        SentryEvent event = new SentryEvent();
        Message sentryMessage = new Message();
        sentryMessage.setMessage("RabbitMQ message dead-lettered on queue " + deadLetterQueue);
        event.setMessage(sentryMessage);
        event.setLevel(SentryLevel.ERROR);
        event.setTag("messaging.system", "rabbitmq");
        event.setTag("rabbit.dead_letter_queue", deadLetterQueue);
        event.setTag("rabbit.source_exchange", sourceExchange);
        event.setExtra("rabbit.dead_letter_queue", deadLetterQueue);
        event.setExtra("rabbit.source_exchange", sourceExchange);
        event.setExtra("rabbit.source_queue", sourceQueue);
        event.setExtra("rabbit.death_reason", reason);
        event.setExtra("rabbit.routing_key", routingKey(messageProperties));
        event.setExtra("rabbit.message_id", messageId(messageProperties));
        event.setExtra("rabbit.content_type", contentType(messageProperties));
        event.setExtra("rabbit.body", truncate(body));
        event.setExtra("rabbit.headers", headers);
        TraceContextSupport.enrich(event, headers);

        Sentry.captureEvent(event);
    }

    private String extractBody(org.springframework.amqp.core.Message message) {
        if (message.getBody() == null) {
            return "";
        }
        return new String(message.getBody(), StandardCharsets.UTF_8);
    }

    private String truncate(String value) {
        if (value.length() <= properties.maxBodyLength()) {
            return value;
        }
        return value.substring(0, properties.maxBodyLength()) + "...";
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> firstDeath(Map<String, Object> headers) {
        Object deaths = headers.get("x-death");
        if (!(deaths instanceof List<?> deathList) || deathList.isEmpty()) {
            return Map.of();
        }
        Object first = deathList.getFirst();
        if (first instanceof Map<?, ?> deathMap) {
            return (Map<String, Object>) deathMap;
        }
        return Map.of();
    }

    private static String stringValue(Object value, String fallback) {
        return value != null ? value.toString() : fallback;
    }

    private static String routingKey(MessageProperties properties) {
        return properties != null && properties.getReceivedRoutingKey() != null
                ? properties.getReceivedRoutingKey()
                : "unknown";
    }

    private static String messageId(MessageProperties properties) {
        return properties != null && properties.getMessageId() != null ? properties.getMessageId() : "unknown";
    }

    private static String contentType(MessageProperties properties) {
        return properties != null && properties.getContentType() != null
                ? properties.getContentType()
                : "unknown";
    }
}
