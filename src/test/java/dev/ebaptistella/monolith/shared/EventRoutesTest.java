package dev.ebaptistella.monolith.shared;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class EventRoutesTest {

    @Test
    void customerCreatedDeadLetterNamesFollowConvention() {
        assertThat(EventRoutes.CUSTOMER_CREATED_DLX).isEqualTo("monolith.customer-created.dlx");
        assertThat(EventRoutes.CUSTOMER_CREATED_DLQ).isEqualTo("monolith.customer-created.dlq");
        assertThat(EventRoutes.CUSTOMER_CREATED_DLQ_ROUTING_KEY).isEqualTo("monolith.customer-created.dead");
    }

    @Test
    void emailDispatchDeadLetterNamesFollowConvention() {
        assertThat(EventRoutes.EMAIL_DISPATCH_DLX).isEqualTo("monolith.email-dispatch-requested.dlx");
        assertThat(EventRoutes.EMAIL_DISPATCH_DLQ).isEqualTo("monolith.email-dispatch-requested.dlq");
        assertThat(EventRoutes.EMAIL_DISPATCH_DLQ_ROUTING_KEY)
                .isEqualTo("monolith.email-dispatch-requested.dead");
    }

    @Test
    void commerceSubscriberQueuesFollowConvention() {
        assertThat(EventRoutes.STOCK_RESERVED_ORDER_QUEUE).isEqualTo("monolith.stock-reserved.order.queue");
        assertThat(EventRoutes.STOCK_RESERVED_FINANCE_QUEUE).isEqualTo("monolith.stock-reserved.finance.queue");
        assertThat(EventRoutes.ORDER_CONFIRMED_INVENTORY_QUEUE)
                .isEqualTo("monolith.order-confirmed.inventory.queue");
        assertThat(EventRoutes.ORDER_CONFIRMED_NOTIFICATION_QUEUE)
                .isEqualTo("monolith.order-confirmed.notification.queue");
        assertThat(EventRoutes.ORDER_CANCELLED_INVENTORY_QUEUE)
                .isEqualTo("monolith.order-cancelled.inventory.queue");
        assertThat(EventRoutes.ORDER_CANCELLED_FINANCE_QUEUE)
                .isEqualTo("monolith.order-cancelled.finance.queue");
        assertThat(EventRoutes.ORDER_CANCELLED_NOTIFICATION_QUEUE)
                .isEqualTo("monolith.order-cancelled.notification.queue");
        assertThat(EventRoutes.LOCAL_ACCOUNT_REGISTERED_QUEUE)
                .isEqualTo("monolith.local-account-registered.queue");
        assertThat(EventRoutes.ACCOUNT_STATUS_CHANGED_QUEUE)
                .isEqualTo("monolith.account-status-changed.queue");
    }

    @Test
    void orderPlacedDeadLetterNamesFollowConvention() {
        assertThat(EventRoutes.ORDER_PLACED_DLX).isEqualTo("monolith.order-placed.dlx");
        assertThat(EventRoutes.ORDER_PLACED_DLQ).isEqualTo("monolith.order-placed.dlq");
        assertThat(EventRoutes.ORDER_PLACED_DLQ_ROUTING_KEY).isEqualTo("monolith.order-placed.dead");
    }

    @Test
    void deadLetterHelpersAreConsistent() {
        String exchange = "monolith.example";
        assertThat(EventRoutes.deadLetterExchange(exchange)).isEqualTo(exchange + ".dlx");
        assertThat(EventRoutes.deadLetterQueue(exchange)).isEqualTo(exchange + ".dlq");
        assertThat(EventRoutes.deadLetterRoutingKey(exchange)).isEqualTo(exchange + ".dead");
    }
}
