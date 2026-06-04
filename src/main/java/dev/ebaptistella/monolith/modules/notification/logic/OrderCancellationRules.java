package dev.ebaptistella.monolith.modules.notification.logic;

import dev.ebaptistella.monolith.modules.notification.models.OrderCancellationEmailContent;
import dev.ebaptistella.monolith.modules.notification.models.OrderCancelledNotification;

public final class OrderCancellationRules {

    private OrderCancellationRules() {
    }

    enum CancellationReasonCategory {
        STOCK,
        PAYMENT,
        GENERIC
    }

    public static OrderCancellationEmailContent buildEmailContent(OrderCancelledNotification notification) {
        String explanation = explainCancellation(notification.reason());
        String body = """
                Hello,

                Your order %s could not be completed and has been cancelled.
                %s
                Order total: %s BRL.

                If you have questions, please contact support.
                """.formatted(notification.orderId(), explanation, notification.totalAmount());

        return new OrderCancellationEmailContent(
                notification.customerEmail(), "Your order was cancelled", body);
    }

    private static String explainCancellation(String reason) {
        return switch (categorize(reason)) {
            case STOCK -> "Reason: one or more items were out of stock.";
            case PAYMENT -> "Reason: payment could not be processed.";
            case GENERIC -> reason == null || reason.isBlank()
                    ? "Reason: unavailable."
                    : "Reason: %s".formatted(reason);
        };
    }

    private static CancellationReasonCategory categorize(String reason) {
        if (reason == null || reason.isBlank()) {
            return CancellationReasonCategory.GENERIC;
        }
        String lower = reason.toLowerCase();
        if (lower.contains("stock") || lower.contains("insufficient")) {
            return CancellationReasonCategory.STOCK;
        }
        if (lower.contains("payment")
                || lower.contains("gateway")
                || lower.contains("declined")
                || lower.contains("capture")) {
            return CancellationReasonCategory.PAYMENT;
        }
        return CancellationReasonCategory.GENERIC;
    }
}
