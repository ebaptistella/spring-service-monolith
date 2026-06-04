package dev.ebaptistella.monolith.config;

import dev.ebaptistella.monolith.shared.EventRoutes;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConditionalOnProperty(
        prefix = "spring.modulith.events.externalization",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class RabbitTopologyConfiguration {

    private record EventChannel(
            String exchange,
            String queue,
            String routingKey,
            String deadLetterExchange,
            String deadLetterQueue,
            String deadLetterRoutingKey) {
    }

    private static EventChannel channel(
            String exchange, String queue, String routingKey, String dlx, String dlq, String dlqRoutingKey) {
        return new EventChannel(exchange, queue, routingKey, dlx, dlq, dlqRoutingKey);
    }

    private static final List<EventChannel> CHANNELS = List.of(
            channel(
                    EventRoutes.CUSTOMER_CREATED_EXCHANGE,
                    EventRoutes.CUSTOMER_CREATED_QUEUE,
                    EventRoutes.CUSTOMER_CREATED_ROUTING_KEY,
                    EventRoutes.CUSTOMER_CREATED_DLX,
                    EventRoutes.CUSTOMER_CREATED_DLQ,
                    EventRoutes.CUSTOMER_CREATED_DLQ_ROUTING_KEY),
            channel(
                    EventRoutes.EMAIL_DISPATCH_EXCHANGE,
                    EventRoutes.EMAIL_DISPATCH_QUEUE,
                    EventRoutes.EMAIL_DISPATCH_ROUTING_KEY,
                    EventRoutes.EMAIL_DISPATCH_DLX,
                    EventRoutes.EMAIL_DISPATCH_DLQ,
                    EventRoutes.EMAIL_DISPATCH_DLQ_ROUTING_KEY),
            channel(
                    EventRoutes.ORDER_PLACED_EXCHANGE,
                    EventRoutes.ORDER_PLACED_QUEUE,
                    EventRoutes.ORDER_PLACED_ROUTING_KEY,
                    EventRoutes.ORDER_PLACED_DLX,
                    EventRoutes.ORDER_PLACED_DLQ,
                    EventRoutes.ORDER_PLACED_DLQ_ROUTING_KEY),
            channel(
                    EventRoutes.STOCK_RESERVED_EXCHANGE,
                    EventRoutes.STOCK_RESERVED_ORDER_QUEUE,
                    EventRoutes.STOCK_RESERVED_ROUTING_KEY,
                    EventRoutes.STOCK_RESERVED_DLX,
                    EventRoutes.STOCK_RESERVED_ORDER_DLQ,
                    EventRoutes.STOCK_RESERVED_DLQ_ROUTING_KEY),
            channel(
                    EventRoutes.STOCK_RESERVED_EXCHANGE,
                    EventRoutes.STOCK_RESERVED_FINANCE_QUEUE,
                    EventRoutes.STOCK_RESERVED_ROUTING_KEY,
                    EventRoutes.STOCK_RESERVED_DLX,
                    EventRoutes.STOCK_RESERVED_FINANCE_DLQ,
                    EventRoutes.STOCK_RESERVED_DLQ_ROUTING_KEY),
            channel(
                    EventRoutes.STOCK_RESERVATION_FAILED_EXCHANGE,
                    EventRoutes.STOCK_RESERVATION_FAILED_QUEUE,
                    EventRoutes.STOCK_RESERVATION_FAILED_ROUTING_KEY,
                    EventRoutes.STOCK_RESERVATION_FAILED_DLX,
                    EventRoutes.STOCK_RESERVATION_FAILED_DLQ,
                    EventRoutes.STOCK_RESERVATION_FAILED_DLQ_ROUTING_KEY),
            channel(
                    EventRoutes.PAYMENT_CAPTURED_EXCHANGE,
                    EventRoutes.PAYMENT_CAPTURED_QUEUE,
                    EventRoutes.PAYMENT_CAPTURED_ROUTING_KEY,
                    EventRoutes.PAYMENT_CAPTURED_DLX,
                    EventRoutes.PAYMENT_CAPTURED_DLQ,
                    EventRoutes.PAYMENT_CAPTURED_DLQ_ROUTING_KEY),
            channel(
                    EventRoutes.PAYMENT_FAILED_EXCHANGE,
                    EventRoutes.PAYMENT_FAILED_QUEUE,
                    EventRoutes.PAYMENT_FAILED_ROUTING_KEY,
                    EventRoutes.PAYMENT_FAILED_DLX,
                    EventRoutes.PAYMENT_FAILED_DLQ,
                    EventRoutes.PAYMENT_FAILED_DLQ_ROUTING_KEY),
            channel(
                    EventRoutes.ORDER_CONFIRMED_EXCHANGE,
                    EventRoutes.ORDER_CONFIRMED_INVENTORY_QUEUE,
                    EventRoutes.ORDER_CONFIRMED_ROUTING_KEY,
                    EventRoutes.ORDER_CONFIRMED_DLX,
                    EventRoutes.ORDER_CONFIRMED_INVENTORY_DLQ,
                    EventRoutes.ORDER_CONFIRMED_DLQ_ROUTING_KEY),
            channel(
                    EventRoutes.ORDER_CONFIRMED_EXCHANGE,
                    EventRoutes.ORDER_CONFIRMED_NOTIFICATION_QUEUE,
                    EventRoutes.ORDER_CONFIRMED_ROUTING_KEY,
                    EventRoutes.ORDER_CONFIRMED_DLX,
                    EventRoutes.ORDER_CONFIRMED_NOTIFICATION_DLQ,
                    EventRoutes.ORDER_CONFIRMED_DLQ_ROUTING_KEY),
            channel(
                    EventRoutes.ORDER_CANCELLED_EXCHANGE,
                    EventRoutes.ORDER_CANCELLED_INVENTORY_QUEUE,
                    EventRoutes.ORDER_CANCELLED_ROUTING_KEY,
                    EventRoutes.ORDER_CANCELLED_DLX,
                    EventRoutes.ORDER_CANCELLED_INVENTORY_DLQ,
                    EventRoutes.ORDER_CANCELLED_DLQ_ROUTING_KEY),
            channel(
                    EventRoutes.ORDER_CANCELLED_EXCHANGE,
                    EventRoutes.ORDER_CANCELLED_FINANCE_QUEUE,
                    EventRoutes.ORDER_CANCELLED_ROUTING_KEY,
                    EventRoutes.ORDER_CANCELLED_DLX,
                    EventRoutes.ORDER_CANCELLED_FINANCE_DLQ,
                    EventRoutes.ORDER_CANCELLED_DLQ_ROUTING_KEY),
            channel(
                    EventRoutes.ORDER_CANCELLED_EXCHANGE,
                    EventRoutes.ORDER_CANCELLED_NOTIFICATION_QUEUE,
                    EventRoutes.ORDER_CANCELLED_ROUTING_KEY,
                    EventRoutes.ORDER_CANCELLED_DLX,
                    EventRoutes.ORDER_CANCELLED_NOTIFICATION_DLQ,
                    EventRoutes.ORDER_CANCELLED_DLQ_ROUTING_KEY),
            channel(
                    EventRoutes.LOCAL_ACCOUNT_REGISTERED_EXCHANGE,
                    EventRoutes.LOCAL_ACCOUNT_REGISTERED_QUEUE,
                    EventRoutes.LOCAL_ACCOUNT_REGISTERED_ROUTING_KEY,
                    EventRoutes.LOCAL_ACCOUNT_REGISTERED_DLX,
                    EventRoutes.LOCAL_ACCOUNT_REGISTERED_DLQ,
                    EventRoutes.LOCAL_ACCOUNT_REGISTERED_DLQ_ROUTING_KEY),
            channel(
                    EventRoutes.ACCOUNT_STATUS_CHANGED_EXCHANGE,
                    EventRoutes.ACCOUNT_STATUS_CHANGED_QUEUE,
                    EventRoutes.ACCOUNT_STATUS_CHANGED_ROUTING_KEY,
                    EventRoutes.ACCOUNT_STATUS_CHANGED_DLX,
                    EventRoutes.ACCOUNT_STATUS_CHANGED_DLQ,
                    EventRoutes.ACCOUNT_STATUS_CHANGED_DLQ_ROUTING_KEY));

    @Bean
    Declarables diplomatEventTopology() {
        List<Declarable> declarables = new ArrayList<>();
        for (EventChannel eventChannel : CHANNELS) {
            TopicExchange exchange = new TopicExchange(eventChannel.exchange(), true, false);
            TopicExchange deadLetterExchange = new TopicExchange(eventChannel.deadLetterExchange(), true, false);

            Queue queue = QueueBuilder.durable(eventChannel.queue())
                    .deadLetterExchange(eventChannel.deadLetterExchange())
                    .deadLetterRoutingKey(eventChannel.deadLetterRoutingKey())
                    .build();

            Queue deadLetterQueue = QueueBuilder.durable(eventChannel.deadLetterQueue()).build();

            Binding mainBinding = BindingBuilder.bind(queue).to(exchange).with(eventChannel.routingKey());
            Binding deadLetterBinding = BindingBuilder.bind(deadLetterQueue)
                    .to(deadLetterExchange)
                    .with(eventChannel.deadLetterRoutingKey());

            declarables.add(exchange);
            declarables.add(deadLetterExchange);
            declarables.add(queue);
            declarables.add(deadLetterQueue);
            declarables.add(mainBinding);
            declarables.add(deadLetterBinding);
        }
        return new Declarables(declarables);
    }
}
