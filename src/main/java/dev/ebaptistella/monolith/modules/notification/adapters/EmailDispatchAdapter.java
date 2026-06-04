package dev.ebaptistella.monolith.modules.notification.adapters;

import dev.ebaptistella.monolith.modules.notification.models.AccountStatusEmailContent;
import dev.ebaptistella.monolith.modules.notification.models.OrderCancellationEmailContent;
import dev.ebaptistella.monolith.modules.notification.models.OrderConfirmationEmailContent;
import dev.ebaptistella.monolith.modules.notification.models.WelcomeNotificationContent;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyKeys;
import dev.ebaptistella.monolith.shared.wire.in.events.EmailDispatchRequestedEvent;

import java.util.UUID;

public final class EmailDispatchAdapter {

    public static final String SCOPE_WELCOME = "email.welcome";
    public static final String SCOPE_ORDER_CONFIRMED = "email.order-confirmed";
    public static final String SCOPE_ORDER_CANCELLED = "email.order-cancelled";
    public static final String SCOPE_ACCOUNT_STATUS = "email.account-status";

    private EmailDispatchAdapter() {
    }

    public static EmailDispatchRequestedEvent contentToWire(UUID rootKey, WelcomeNotificationContent content) {
        return toWire(rootKey, SCOPE_WELCOME, content.to(), content.subject(), content.body());
    }

    public static EmailDispatchRequestedEvent contentToWire(UUID rootKey, OrderConfirmationEmailContent content) {
        return toWire(rootKey, SCOPE_ORDER_CONFIRMED, content.to(), content.subject(), content.body());
    }

    public static EmailDispatchRequestedEvent contentToWire(UUID rootKey, OrderCancellationEmailContent content) {
        return toWire(rootKey, SCOPE_ORDER_CANCELLED, content.to(), content.subject(), content.body());
    }

    public static EmailDispatchRequestedEvent contentToWire(UUID rootKey, AccountStatusEmailContent content) {
        return toWire(rootKey, SCOPE_ACCOUNT_STATUS, content.to(), content.subject(), content.body());
    }

    private static EmailDispatchRequestedEvent toWire(
            UUID rootKey, String scope, String to, String subject, String body) {
        UUID dispatchId = IdempotencyKeys.derive(rootKey, scope);
        return new EmailDispatchRequestedEvent(dispatchId, dispatchId, to, subject, body);
    }
}
