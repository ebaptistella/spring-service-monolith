package dev.ebaptistella.monolith.shared;

public final class EventRoutes {

    public static final String CUSTOMER_CREATED_EXCHANGE = "monolith.customer-created";
    public static final String CUSTOMER_CREATED_QUEUE = "monolith.customer-created.queue";
    public static final String CUSTOMER_CREATED_ROUTING_KEY = CUSTOMER_CREATED_EXCHANGE;
    public static final String CUSTOMER_CREATED_EXTERNALIZED =
            CUSTOMER_CREATED_EXCHANGE + "::" + CUSTOMER_CREATED_ROUTING_KEY;
    public static final String CUSTOMER_CREATED_DLX = "monolith.customer-created.dlx";
    public static final String CUSTOMER_CREATED_DLQ = "monolith.customer-created.dlq";
    public static final String CUSTOMER_CREATED_DLQ_ROUTING_KEY = "monolith.customer-created.dead";

    public static final String EMAIL_DISPATCH_EXCHANGE = "monolith.email-dispatch-requested";
    public static final String EMAIL_DISPATCH_QUEUE = "monolith.email-dispatch-requested.queue";
    public static final String EMAIL_DISPATCH_ROUTING_KEY = EMAIL_DISPATCH_EXCHANGE;
    public static final String EMAIL_DISPATCH_EXTERNALIZED =
            EMAIL_DISPATCH_EXCHANGE + "::" + EMAIL_DISPATCH_ROUTING_KEY;
    public static final String EMAIL_DISPATCH_DLX = "monolith.email-dispatch-requested.dlx";
    public static final String EMAIL_DISPATCH_DLQ = "monolith.email-dispatch-requested.dlq";
    public static final String EMAIL_DISPATCH_DLQ_ROUTING_KEY = "monolith.email-dispatch-requested.dead";

    public static final String ORDER_PLACED_EXCHANGE = "monolith.order-placed";
    public static final String ORDER_PLACED_QUEUE = "monolith.order-placed.queue";
    public static final String ORDER_PLACED_ROUTING_KEY = ORDER_PLACED_EXCHANGE;
    public static final String ORDER_PLACED_EXTERNALIZED = ORDER_PLACED_EXCHANGE + "::" + ORDER_PLACED_ROUTING_KEY;
    public static final String ORDER_PLACED_DLX = "monolith.order-placed.dlx";
    public static final String ORDER_PLACED_DLQ = "monolith.order-placed.dlq";
    public static final String ORDER_PLACED_DLQ_ROUTING_KEY = "monolith.order-placed.dead";

    public static final String STOCK_RESERVED_EXCHANGE = "monolith.stock-reserved";
    public static final String STOCK_RESERVED_ORDER_QUEUE = "monolith.stock-reserved.order.queue";
    public static final String STOCK_RESERVED_FINANCE_QUEUE = "monolith.stock-reserved.finance.queue";
    public static final String STOCK_RESERVED_ROUTING_KEY = STOCK_RESERVED_EXCHANGE;
    public static final String STOCK_RESERVED_EXTERNALIZED =
            STOCK_RESERVED_EXCHANGE + "::" + STOCK_RESERVED_ROUTING_KEY;
    public static final String STOCK_RESERVED_DLX = "monolith.stock-reserved.dlx";
    public static final String STOCK_RESERVED_ORDER_DLQ = "monolith.stock-reserved.order.dlq";
    public static final String STOCK_RESERVED_FINANCE_DLQ = "monolith.stock-reserved.finance.dlq";
    public static final String STOCK_RESERVED_DLQ_ROUTING_KEY = "monolith.stock-reserved.dead";

    public static final String STOCK_RESERVATION_FAILED_EXCHANGE = "monolith.stock-reservation-failed";
    public static final String STOCK_RESERVATION_FAILED_QUEUE = "monolith.stock-reservation-failed.queue";
    public static final String STOCK_RESERVATION_FAILED_ROUTING_KEY = STOCK_RESERVATION_FAILED_EXCHANGE;
    public static final String STOCK_RESERVATION_FAILED_EXTERNALIZED =
            STOCK_RESERVATION_FAILED_EXCHANGE + "::" + STOCK_RESERVATION_FAILED_ROUTING_KEY;
    public static final String STOCK_RESERVATION_FAILED_DLX = "monolith.stock-reservation-failed.dlx";
    public static final String STOCK_RESERVATION_FAILED_DLQ = "monolith.stock-reservation-failed.dlq";
    public static final String STOCK_RESERVATION_FAILED_DLQ_ROUTING_KEY =
            "monolith.stock-reservation-failed.dead";

    public static final String PAYMENT_CAPTURED_EXCHANGE = "monolith.payment-captured";
    public static final String PAYMENT_CAPTURED_QUEUE = "monolith.payment-captured.queue";
    public static final String PAYMENT_CAPTURED_ROUTING_KEY = PAYMENT_CAPTURED_EXCHANGE;
    public static final String PAYMENT_CAPTURED_EXTERNALIZED =
            PAYMENT_CAPTURED_EXCHANGE + "::" + PAYMENT_CAPTURED_ROUTING_KEY;
    public static final String PAYMENT_CAPTURED_DLX = "monolith.payment-captured.dlx";
    public static final String PAYMENT_CAPTURED_DLQ = "monolith.payment-captured.dlq";
    public static final String PAYMENT_CAPTURED_DLQ_ROUTING_KEY = "monolith.payment-captured.dead";

    public static final String PAYMENT_FAILED_EXCHANGE = "monolith.payment-failed";
    public static final String PAYMENT_FAILED_QUEUE = "monolith.payment-failed.queue";
    public static final String PAYMENT_FAILED_ROUTING_KEY = PAYMENT_FAILED_EXCHANGE;
    public static final String PAYMENT_FAILED_EXTERNALIZED =
            PAYMENT_FAILED_EXCHANGE + "::" + PAYMENT_FAILED_ROUTING_KEY;
    public static final String PAYMENT_FAILED_DLX = "monolith.payment-failed.dlx";
    public static final String PAYMENT_FAILED_DLQ = "monolith.payment-failed.dlq";
    public static final String PAYMENT_FAILED_DLQ_ROUTING_KEY = "monolith.payment-failed.dead";

    public static final String ORDER_CONFIRMED_EXCHANGE = "monolith.order-confirmed";
    public static final String ORDER_CONFIRMED_INVENTORY_QUEUE = "monolith.order-confirmed.inventory.queue";
    public static final String ORDER_CONFIRMED_NOTIFICATION_QUEUE = "monolith.order-confirmed.notification.queue";
    public static final String ORDER_CONFIRMED_ROUTING_KEY = ORDER_CONFIRMED_EXCHANGE;
    public static final String ORDER_CONFIRMED_EXTERNALIZED =
            ORDER_CONFIRMED_EXCHANGE + "::" + ORDER_CONFIRMED_ROUTING_KEY;
    public static final String ORDER_CONFIRMED_DLX = "monolith.order-confirmed.dlx";
    public static final String ORDER_CONFIRMED_INVENTORY_DLQ = "monolith.order-confirmed.inventory.dlq";
    public static final String ORDER_CONFIRMED_NOTIFICATION_DLQ = "monolith.order-confirmed.notification.dlq";
    public static final String ORDER_CONFIRMED_DLQ_ROUTING_KEY = "monolith.order-confirmed.dead";

    public static final String ORDER_CANCELLED_EXCHANGE = "monolith.order-cancelled";
    public static final String ORDER_CANCELLED_INVENTORY_QUEUE = "monolith.order-cancelled.inventory.queue";
    public static final String ORDER_CANCELLED_FINANCE_QUEUE = "monolith.order-cancelled.finance.queue";
    public static final String ORDER_CANCELLED_NOTIFICATION_QUEUE = "monolith.order-cancelled.notification.queue";
    public static final String ORDER_CANCELLED_ROUTING_KEY = ORDER_CANCELLED_EXCHANGE;
    public static final String ORDER_CANCELLED_EXTERNALIZED =
            ORDER_CANCELLED_EXCHANGE + "::" + ORDER_CANCELLED_ROUTING_KEY;
    public static final String ORDER_CANCELLED_DLX = "monolith.order-cancelled.dlx";
    public static final String ORDER_CANCELLED_INVENTORY_DLQ = "monolith.order-cancelled.inventory.dlq";
    public static final String ORDER_CANCELLED_FINANCE_DLQ = "monolith.order-cancelled.finance.dlq";
    public static final String ORDER_CANCELLED_NOTIFICATION_DLQ = "monolith.order-cancelled.notification.dlq";
    public static final String ORDER_CANCELLED_DLQ_ROUTING_KEY = "monolith.order-cancelled.dead";

    public static final String LOCAL_ACCOUNT_REGISTERED_EXCHANGE = "monolith.local-account-registered";
    public static final String LOCAL_ACCOUNT_REGISTERED_QUEUE = "monolith.local-account-registered.queue";
    public static final String LOCAL_ACCOUNT_REGISTERED_ROUTING_KEY = LOCAL_ACCOUNT_REGISTERED_EXCHANGE;
    public static final String LOCAL_ACCOUNT_REGISTERED_EXTERNALIZED =
            LOCAL_ACCOUNT_REGISTERED_EXCHANGE + "::" + LOCAL_ACCOUNT_REGISTERED_ROUTING_KEY;
    public static final String LOCAL_ACCOUNT_REGISTERED_DLX = "monolith.local-account-registered.dlx";
    public static final String LOCAL_ACCOUNT_REGISTERED_DLQ = "monolith.local-account-registered.dlq";
    public static final String LOCAL_ACCOUNT_REGISTERED_DLQ_ROUTING_KEY =
            "monolith.local-account-registered.dead";

    public static final String ACCOUNT_STATUS_CHANGED_EXCHANGE = "monolith.account-status-changed";
    public static final String ACCOUNT_STATUS_CHANGED_QUEUE = "monolith.account-status-changed.queue";
    public static final String ACCOUNT_STATUS_CHANGED_ROUTING_KEY = ACCOUNT_STATUS_CHANGED_EXCHANGE;
    public static final String ACCOUNT_STATUS_CHANGED_EXTERNALIZED =
            ACCOUNT_STATUS_CHANGED_EXCHANGE + "::" + ACCOUNT_STATUS_CHANGED_ROUTING_KEY;
    public static final String ACCOUNT_STATUS_CHANGED_DLX = "monolith.account-status-changed.dlx";
    public static final String ACCOUNT_STATUS_CHANGED_DLQ = "monolith.account-status-changed.dlq";
    public static final String ACCOUNT_STATUS_CHANGED_DLQ_ROUTING_KEY = "monolith.account-status-changed.dead";

    private EventRoutes() {
    }

    public static String deadLetterExchange(String mainExchange) {
        return mainExchange + ".dlx";
    }

    public static String deadLetterQueue(String mainExchange) {
        return mainExchange + ".dlq";
    }

    public static String deadLetterRoutingKey(String mainExchange) {
        return mainExchange + ".dead";
    }
}
