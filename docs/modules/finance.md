# Module: finance

Payment intent lifecycle: capture after stock reservation, cancel on order cancellation.

## 1. Module responsibility (Modulith boundary)

- Owns `PaymentIntent` and `Transaction` for an order
- Captures payment when inventory publishes `StockReserved` (fan-out queue)
- Cancels pending intents when order module publishes `OrderCancelled`
- Publishes `PaymentCaptured` / `PaymentFailed` for order module to confirm or cancel the order
- No public HTTP API in this POC — entry is event-driven only

Does not own orders, stock, or gateway provider configuration beyond `FakePaymentGateway`.

## 2. models

| Type | Role |
|------|------|
| `PaymentIntent` | Per-order payment state (amount, currency, status) |
| `Transaction` | Audit row per gateway attempt outcome |
| `PaymentStatus` | `PENDING`, `CAPTURED`, `FAILED`, `CANCELLED` |
| `CapturePaymentInput` | `orderId`, `amount` from `StockReservedEvent` |

## 3. logic

`PaymentRules` (pure):

- `isValidCaptureAmount` — amount must be positive

`PaymentCaptureResult` — success, failed, or idempotent success (no side effects).

Capture idempotency and terminal-state handling live in `CapturePaymentController` (not duplicated in rules): if intent already `CAPTURED`, returns `PaymentCaptureResult.idempotentSuccess` without calling the gateway or republishing events.

## 4. controllers (logic sandwich)

| Controller | Trigger | Behavior |
|------------|---------|----------|
| `CapturePaymentController` | `StockReserved` consumer | Find-or-create `PENDING` intent, gateway capture, persist, publish captured/failed |
| `CancelPaymentController` | `OrderCancelled` consumer | Set `PENDING` → `CANCELLED`; no-op if no intent or not pending |

## 5. adapters

- `PaymentEventAdapter` — `StockReservedEvent` → `CapturePaymentInput`; `PaymentIntent` ↔ `PaymentCapturedEvent` / `PaymentFailedEvent`; order id extraction from `OrderCancelledEvent`

## 6. wire/in and wire/out

No module-local HTTP wire types. Event payloads live in `shared.wire.in.events` (`StockReservedEvent`, `PaymentCapturedEvent`, `PaymentFailedEvent`, `OrderCancelledEvent`).

## 7. diplomat

| Area | Types |
|------|--------|
| **jpa** | `PaymentIntentEntity`, `TransactionEntity`, repositories, `FinancePersistence` |
| **payment** | `PaymentGateway`, `FakePaymentGateway`, `FakePaymentGatewayProperties` |
| **producer** | `FinanceEventProducer` |
| **consumer** | `StockReservedConsumer` (`STOCK_RESERVED_FINANCE_QUEUE`), `OrderCancelledConsumer` (`ORDER_CANCELLED_FINANCE_QUEUE`) — `@Observed`, no `@Slf4j` / `log.info` |

No `http_server` or `outbound` in this module.

## 8. Sync integration and async

**Sync:** None exposed; payment I/O is `PaymentGateway` inside diplomat.

**Async (`EventRoutes`):**

| Direction | Event | Queue |
|-----------|-------|-------|
| Consumes | `StockReservedEvent` | `STOCK_RESERVED_FINANCE_QUEUE` |
| Consumes | `OrderCancelledEvent` | `ORDER_CANCELLED_FINANCE_QUEUE` |
| Produces | `PaymentCapturedEvent` | `PAYMENT_CAPTURED_*` → order |
| Produces | `PaymentFailedEvent` | `PAYMENT_FAILED_*` → order |

### Reprocessing `StockReserved` (idempotency)

The same `StockReservedEvent` may be delivered more than once (at-least-once messaging, consumer retry, or duplicate publish). `financeStockReservedConsumer` always calls `CapturePaymentController.capture`.

Safe reprocessing behavior:

1. **First delivery:** No intent for `orderId` → create `PENDING` intent → gateway capture → `CAPTURED` or `FAILED` + corresponding event.
2. **Duplicate after success:** Intent already `CAPTURED` → `captureExisting` returns `idempotentSuccess`; gateway and `publishPaymentCaptured` are not invoked again.
3. **Duplicate while still `PENDING`:** Retries gateway path (POC accepts this; production would use idempotency keys on the gateway).
4. **Terminal failure/cancel:** `FAILED` or `CANCELLED` intents return failed capture without republishing success.

Order module separately idempotents `StockReserved` via `OrderRules.shouldSkipMarkAwaitingPayment` when status is already `AWAITING_PAYMENT`.

## Idempotency

- `payment_intents` and capture transactions store derived `idempotency_key` from the order root key (`finance.capture`, order id).
- `CapturePaymentController` returns idempotent success when intent is already terminal; gateway not called again.

## 9. Existing tests

| Test | Tag | Coverage |
|------|-----|----------|
| `PaymentRulesTest` | unit | Capture amount validation |
| `CapturePaymentControllerTest` | unit | Idempotent capture when already `CAPTURED`, positive amount rule, successful capture path |
