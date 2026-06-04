package dev.ebaptistella.monolith.config;

import dev.ebaptistella.monolith.shared.EventRoutes;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class RabbitTopologyConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("spring.modulith.events.externalization.enabled=true")
            .withUserConfiguration(RabbitTopologyConfiguration.class);

    @Test
    void mainQueuesDeclarePerExchangeDeadLetterRouting() {
        contextRunner.run(context -> {
            Declarables declarables = context.getBean(Declarables.class);
            assertDeadLetterBinding(
                    declarables,
                    EventRoutes.CUSTOMER_CREATED_QUEUE,
                    EventRoutes.CUSTOMER_CREATED_DLX,
                    EventRoutes.CUSTOMER_CREATED_DLQ_ROUTING_KEY);
            assertDeadLetterBinding(
                    declarables,
                    EventRoutes.EMAIL_DISPATCH_QUEUE,
                    EventRoutes.EMAIL_DISPATCH_DLX,
                    EventRoutes.EMAIL_DISPATCH_DLQ_ROUTING_KEY);
        });
    }

    @Test
    void commerceSubscriberQueuesDeclarePerExchangeDeadLetterRouting() {
        contextRunner.run(context -> {
            Declarables declarables = context.getBean(Declarables.class);
            assertDeadLetterBinding(
                    declarables,
                    EventRoutes.STOCK_RESERVED_ORDER_QUEUE,
                    EventRoutes.STOCK_RESERVED_DLX,
                    EventRoutes.STOCK_RESERVED_DLQ_ROUTING_KEY);
            assertDeadLetterBinding(
                    declarables,
                    EventRoutes.STOCK_RESERVED_FINANCE_QUEUE,
                    EventRoutes.STOCK_RESERVED_DLX,
                    EventRoutes.STOCK_RESERVED_DLQ_ROUTING_KEY);
            assertDeadLetterBinding(
                    declarables,
                    EventRoutes.ORDER_CONFIRMED_INVENTORY_QUEUE,
                    EventRoutes.ORDER_CONFIRMED_DLX,
                    EventRoutes.ORDER_CONFIRMED_DLQ_ROUTING_KEY);
            assertDeadLetterBinding(
                    declarables,
                    EventRoutes.ORDER_CONFIRMED_NOTIFICATION_QUEUE,
                    EventRoutes.ORDER_CONFIRMED_DLX,
                    EventRoutes.ORDER_CONFIRMED_DLQ_ROUTING_KEY);
            assertDeadLetterBinding(
                    declarables,
                    EventRoutes.ORDER_CANCELLED_INVENTORY_QUEUE,
                    EventRoutes.ORDER_CANCELLED_DLX,
                    EventRoutes.ORDER_CANCELLED_DLQ_ROUTING_KEY);
            assertDeadLetterBinding(
                    declarables,
                    EventRoutes.ORDER_CANCELLED_FINANCE_QUEUE,
                    EventRoutes.ORDER_CANCELLED_DLX,
                    EventRoutes.ORDER_CANCELLED_DLQ_ROUTING_KEY);
            assertDeadLetterBinding(
                    declarables,
                    EventRoutes.ORDER_CANCELLED_NOTIFICATION_QUEUE,
                    EventRoutes.ORDER_CANCELLED_DLX,
                    EventRoutes.ORDER_CANCELLED_DLQ_ROUTING_KEY);
            assertDeadLetterBinding(
                    declarables,
                    EventRoutes.LOCAL_ACCOUNT_REGISTERED_QUEUE,
                    EventRoutes.LOCAL_ACCOUNT_REGISTERED_DLX,
                    EventRoutes.LOCAL_ACCOUNT_REGISTERED_DLQ_ROUTING_KEY);
            assertDeadLetterBinding(
                    declarables,
                    EventRoutes.ACCOUNT_STATUS_CHANGED_QUEUE,
                    EventRoutes.ACCOUNT_STATUS_CHANGED_DLX,
                    EventRoutes.ACCOUNT_STATUS_CHANGED_DLQ_ROUTING_KEY);
        });
    }

    @Test
    void eachExchangeHasDedicatedDeadLetterQueue() {
        contextRunner.run(context -> {
            Declarables declarables = context.getBean(Declarables.class);
            assertThat(queueNames(declarables))
                    .contains(
                            EventRoutes.CUSTOMER_CREATED_DLQ,
                            EventRoutes.EMAIL_DISPATCH_DLQ);
        });
    }

    private static void assertDeadLetterBinding(
            Declarables declarables, String queueName, String deadLetterExchange, String deadLetterRoutingKey) {
        Queue queue = declarables.getDeclarables().stream()
                .filter(Queue.class::isInstance)
                .map(Queue.class::cast)
                .filter(candidate -> queueName.equals(candidate.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Queue not declared: " + queueName));

        Map<String, Object> arguments = queue.getArguments();
        assertThat(arguments)
                .containsEntry("x-dead-letter-exchange", deadLetterExchange)
                .containsEntry("x-dead-letter-routing-key", deadLetterRoutingKey);
    }

    private static Stream<String> queueNames(Declarables declarables) {
        return declarables.getDeclarables().stream()
                .filter(Queue.class::isInstance)
                .map(Queue.class::cast)
                .map(Queue::getName);
    }
}
