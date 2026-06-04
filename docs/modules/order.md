# Module: order

Order placement, lifecycle, and orchestration of the checkout saga (stock reservation → payment → confirmation).

## 1. Module responsibility (Modulith boundary)

- Owns `Order` aggregate and `OrderStatus` lifecycle: `PLACED` → `AWAITING_PAYMENT` → `CONFIRMED` or `CANCELLED`
- HTTP API: place and query orders (`/api/v1/orders`)
- Publishes `OrderPlaced`, `OrderConfirmed`, `OrderCancelled` for downstream modules
- Reacts to inventory and finance events to advance or cancel orders
- Enforces business rules: spend limit (1000 BRL per customer confirmed total), catalog pricing, stock availability at place time

Does not own stock reservation, payment capture, or notifications — coordinates via events and sync gateways.

## 2. models

| Type | Role |
|------|------|
| `Order`, `OrderLine` | Aggregate root and line items |
| `OrderStatus` | `PLACED`, `AWAITING_PAYMENT`, `CONFIRMED`, `CANCELLED` |
| `PlaceOrderInput`, `PlaceOrderLineInput` | Command input for placement |
| `PlaceOrderResolvedLine` | Catalog-resolved line (SKU, qty, unit price) passed into rules |

## 3. logic

`OrderRules` (pure, no Spring):

- `validatePlaceOrder` — customer, lines, stock, spend limit; builds `PLACED` order
- `canMarkAwaitingPayment`, `canConfirm`, `canCancel` — allowed transitions
- `shouldSkipMarkAwaitingPayment`, `shouldSkipConfirmTransition`, `shouldSkipCancelTransition` — idempotent/no-op guards for event handlers (already in target state or invalid source state)
- `toCancelled` — immutable cancel transformation
- `exceedsSpendLimit`, `lineTotal`, constants `SPEND_LIMIT`, `DEFAULT_CURRENCY`

`PlaceOrderResult` — success/rejected wrapper for placement.

## 4. controllers (logic sandwich)

| Controller | Trigger | Behavior |
|------------|---------|----------|
| `PlaceOrderController` | HTTP POST | Resolves catalog/inventory via gateways, runs `OrderRules`, saves, publishes `OrderPlaced` |
| `GetOrderController` | HTTP GET | Load by id |
| `CancelOrderController` | (internal/API if wired) | Cancel with `OrderRules.toCancelled`; throws if not cancellable |
| `HandleStockReservedController` | `StockReserved` consumer | `PLACED` → `AWAITING_PAYMENT`; skips via `shouldSkipMarkAwaitingPayment` |
| `HandleStockReservationFailedController` | `StockReservationFailed` | Cancel + `OrderCancelled` |
| `HandlePaymentCapturedController` | `PaymentCaptured` | `AWAITING_PAYMENT` → `CONFIRMED`; loads customer email; publishes `OrderConfirmed` |
| `HandlePaymentFailedController` | `PaymentFailed` | Cancel + `OrderCancelled` |

Controllers depend on `diplomat` (persistence, gateways, producers) and `logic`; never on `wire`.

## 5. adapters

- `OrderAdapter` — `PlaceOrderRequest` ↔ `PlaceOrderInput`; `Order` → `OrderResponse`
- `OrderEventAdapter` — `Order` ↔ shared event payloads; extracts ids/reasons from inbound events

## 6. wire/in and wire/out

- **in:** `PlaceOrderRequest`, `PlaceOrderLineRequest`
- **out:** `OrderResponse`, `OrderLineResponse`

Mapped only at `diplomat/http_server` boundary via adapters.

## 7. diplomat

| Area | Types |
|------|--------|
| **http_server** | `OrderHttpServer` — `/api/v1/orders` |
| **jpa** | `OrderEntity`, `OrderLineEntity`, `OrderJpaRepository`, `OrderPersistence` |
| **producer** | `OrderEventProducer` — Spring `ApplicationEventPublisher` + `@Externalized` routing |
| **consumer** | `StockReservedConsumer`, `StockReservationFailedConsumer`, `PaymentCapturedConsumer`, `PaymentFailedConsumer` (`@Observed`, no application logging) |
| **outbound** | `CatalogGateway`, `InventoryGateway`, `CustomerGateway` (+ `Local*` implementations) |

No `inbound` SPI in this module; sync reads use outbound gateways to other modules.

## 8. Sync integration and async

**Sync (contracts / gateways):**

- `CatalogGateway` — SKU active, list price
- `InventoryGateway` — `StockLineRequest` availability check at place time
- `CustomerGateway` — existence at place; email at confirm

**Async (events + `EventRoutes`):**

| Direction | Event | Queue / exchange |
|-----------|-------|------------------|
| Produces | `OrderPlacedEvent` | `ORDER_PLACED_*` → inventory |
| Produces | `OrderConfirmedEvent` | `ORDER_CONFIRMED_*` → inventory, notification |
| Produces | `OrderCancelledEvent` | `ORDER_CANCELLED_*` → inventory, finance |
| Consumes | `StockReservedEvent` | `STOCK_RESERVED_ORDER_QUEUE` |
| Consumes | `StockReservationFailedEvent` | `STOCK_RESERVATION_FAILED_QUEUE` |
| Consumes | `PaymentCapturedEvent` | `PAYMENT_CAPTURED_QUEUE` |
| Consumes | `PaymentFailedEvent` | `PAYMENT_FAILED_QUEUE` |

## Idempotency

- `POST /api/v1/orders` derives `idempotency_key` from `R` (`order.place` scope); replay returns `200` without republishing `OrderPlacedEvent`.
- Saga consumers remain at-least-once; downstream modules idempotize their own steps via derived keys on `OrderPlacedEvent.idempotencyKey`.

## 9. Existing tests

| Test | Tag | Coverage |
|------|-----|----------|
| `OrderRulesTest` | unit | Placement rules, spend limit, status guards (`shouldSkip*`) |
| `PlaceOrderControllerTest` | unit | Placement orchestration (mocked diplomat) |
| `OrderFlowE2ETest` | e2e | Full order flow via HTTP + messaging |
