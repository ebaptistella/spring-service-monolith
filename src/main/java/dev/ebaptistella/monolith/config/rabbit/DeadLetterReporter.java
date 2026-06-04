package dev.ebaptistella.monolith.config.rabbit;

import org.springframework.amqp.core.Message;

public interface DeadLetterReporter {

    void report(String deadLetterQueue, Message message);
}
