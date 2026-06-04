# Module: notification

## 1. Module responsibility (Modulith boundary)

Notification orchestration without owning email transport: reacts to domain events, optionally pushes welcome/order-confirmed to an external notification platform, builds email content in pure logic, and publishes `EmailDispatchRequestedEvent` for the `email` module.

No HTTP API; no JPA. `allowedDependencies = {"shared"}`.

## 2. models

| Type | Role |
|------|------|
| `CustomerCreatedNotification` | Welcome flow input (from `CustomerCreatedEvent` or `LocalAccountRegisteredEvent`) |
| `WelcomeNotificationContent` | Rendered welcome email fields |
| `OrderConfirmedNotification` | Order confirmation input |
| `OrderConfirmationEmailContent` | Rendered order confirmation email fields |
| `OrderCancelledNotification` | Order cancellation input (from enriched `OrderCancelledEvent`) |
| `OrderCancellationEmailContent` | Rendered cancellation email fields |
| `AccountStatusChangedNotification` | Account status change input |
| `AccountStatusEmailContent` | Rendered account status email fields |

## 3. logic

| Type | Role |
|------|------|
| `WelcomeNotificationRules` | Build welcome `WelcomeNotificationContent` |
| `OrderConfirmationRules` | Build order confirmation `OrderConfirmationEmailContent` |
| `OrderCancellationRules` | Build cancellation copy by `reason` (stock vs payment vs generic) |
| `AccountStatusRules` | Build account status update email |

## 4. controllers (logic sandwich)

| Controller | Flow |
|------------|------|
| `WelcomeNotificationController` | Platform welcome → rules → `NotificationEventProducer` (welcome email) |
| `OrderConfirmationNotificationController` | Platform order confirmed → rules → producer |
| `OrderCancellationNotificationController` | Rules → `EmailDispatchRequestedEvent` → producer |
| `AccountStatusNotificationController` | Rules → producer |

## 5. adapters

| Adapter | Role |
|---------|------|
| `CustomerCreatedEventAdapter` | `CustomerCreatedEvent` → `CustomerCreatedNotification` |
| `LocalAccountRegisteredEventAdapter` | `LocalAccountRegisteredEvent` → welcome notification |
| `OrderConfirmedEventAdapter` | `OrderConfirmedEvent` → `OrderConfirmedNotification`; content → `EmailDispatchRequestedEvent` |
| `OrderCancelledEventAdapter` | `OrderCancelledEvent` → `OrderCancelledNotification` |
| `AccountStatusChangedEventAdapter` | `AccountStatusChangedEvent` → `AccountStatusChangedNotification` |
| `EmailDispatchAdapter` | Content records → `EmailDispatchRequestedEvent` |

## 6. wire/in and wire/out

No module `wire/in` or `wire/out`. Uses `shared.wire.in.events` for Rabbit payloads.

## 7. diplomat

| Subfolder | Classes |
|-----------|---------|
| **http_server** | — |
| **jpa** | — |
| **producer** | `NotificationEventProducer` |
| **consumer** | `CustomerCreatedConsumer`, `LocalAccountRegisteredConsumer`, `OrderConfirmedConsumer`, `OrderCancelledConsumer`, `AccountStatusChangedConsumer` |
| **inbound** | — |
| **outbound** | — |
| **Other** | `NotificationPlatform`, `LoggingNotificationPlatform`, `OAuth2NotificationPlatformClient`; `config/NotificationPlatformConfiguration`, `NotificationPlatformProperties` |

## 8. Sync integration and async

**Sync**

None (no `shared.contracts` implementation in this module).

**Async** (`EventRoutes`)

| Direction | Event | Queue |
|-----------|-------|-------|
| **In** | `CustomerCreatedEvent` | `CUSTOMER_CREATED_QUEUE` |
| **In** | `LocalAccountRegisteredEvent` | `LOCAL_ACCOUNT_REGISTERED_QUEUE` |
| **In** | `OrderConfirmedEvent` | `ORDER_CONFIRMED_NOTIFICATION_QUEUE` |
| **In** | `OrderCancelledEvent` | `ORDER_CANCELLED_NOTIFICATION_QUEUE` |
| **In** | `AccountStatusChangedEvent` | `ACCOUNT_STATUS_CHANGED_QUEUE` |
| **Out** | `EmailDispatchRequestedEvent` | `EMAIL_DISPATCH_EXCHANGE` → `EMAIL_DISPATCH_QUEUE` |

Same exchange/routing key for fan-out events uses **separate subscriber queues** (e.g. `ORDER_CONFIRMED_INVENTORY_QUEUE` vs `ORDER_CONFIRMED_NOTIFICATION_QUEUE`; `ORDER_CANCELLED_*` modules vs `ORDER_CANCELLED_NOTIFICATION_QUEUE`).

**Design note:** saga failures converge on a single `OrderCancelledEvent` consumer in notification — do **not** consume `StockReservationFailed` or `PaymentFailed` separately (avoids duplicate emails).

## Idempotency

- Event adapters propagate `idempotencyKey` from upstream domain events into `EmailDispatchRequestedEvent`.
- `EmailDispatchAdapter` builds deterministic dispatch ids so duplicate deliveries send at most one email.

## 9. Existing tests

| Test | Scope |
|------|--------|
| `CustomerCreatedEventAdapterTest` | Welcome path wire → model |
| `LocalAccountRegisteredEventAdapterTest` | Local auth welcome path |
| `OrderConfirmedEventAdapterTest` | Order confirmation → email dispatch wire |
| `OrderCancelledEventAdapterTest` | Cancellation wire → model |
| `OrderCancellationRulesTest` | Reason → email copy |
| `AccountStatusRulesTest` | Status email content |
| `ArchitectureTest` | Logic isolation; consumer → adapter |
| `CustomerRegistrationE2ETest`, `LocalAuthE2ETest`, `OrderFlowE2ETest`, `OrderPaymentFailureE2ETest` | Indirect E2E through customer/order/identity flows |

No dedicated controller unit tests; behavior covered via adapters, rules, and E2E.
