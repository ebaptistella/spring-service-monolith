package dev.ebaptistella.monolith.modules.notification.logic;

import dev.ebaptistella.monolith.modules.notification.models.OrderConfirmationEmailContent;
import dev.ebaptistella.monolith.modules.notification.models.OrderConfirmedNotification;

public final class OrderConfirmationRules {

    private OrderConfirmationRules() {
    }

    public static OrderConfirmationEmailContent buildEmailContent(OrderConfirmedNotification notification) {
        String body = """
                Hello,

                Your order %s has been confirmed.
                Total amount: %s BRL.

                Thank you for your purchase!
                """.formatted(notification.orderId(), notification.totalAmount());

        return new OrderConfirmationEmailContent(
                notification.customerEmail(), "Your order was confirmed", body);
    }
}
