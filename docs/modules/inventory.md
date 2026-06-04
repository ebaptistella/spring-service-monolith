# Module: inventory

## 1. Module responsibility (Modulith boundary)

Stock levels, reservations, movements, and availability. Reacts to order lifecycle events (reserve on placed, commit on confirmed, release on cancelled). Publishes stock outcome events for `order` and `finance`. Exposes HTTP stock adjustment/query and **sync** `StockAvailabilityQuery` for place-order validation.

`allowedDependencies = {"shared"}`.

## 2. models

| Type | Role |
|------|------|
| `StockLevel` | Per-SKU on-hand and reserved quantities |
| `StockReservation` | Reservation tied to `orderId`, SKU, qty, `ReservationStatus` |
| `StockMovement` | Audit trail (`MovementType`: RESERVE, COMMIT, RELEASE, ADJUST, …) |
| `StockReserveInput`, `StockReserveLine` | Reserve command for an order |
| `AdjustStockInput` | Manual on-hand delta |
| `OrderPlacedInventoryInput` | Model after `OrderPlacedEvent` adapter |
| `MovementType`, `ReservationStatus` | Enums |

## 3. logic

| Type | Role |
|------|------|
| `StockRules` | `available`, `canReserve`, valid adjustment result |
| `StockReservationResult` | Success / failed / idempotent success |

## 4. controllers (logic sandwich)

| Controller | Flow |
|------------|------|
| `ReserveStockController` | Idempotent reserve per order; rules + JPA levels/reservations/movements |
| `CommitStockController` | On order confirmed: commit reservations (reduce on-hand) |
| `ReleaseStockController` | On order cancelled: release reservations |
| `AdjustStockController` | Apply stock adjustment |
| `QueryStockController` | Read `StockLevel` for a SKU |
| `HandleOrderPlacedController` | Reserve → publish `StockReserved` or `StockReservationFailed` |

## 5. adapters

| Adapter | Role |
|---------|------|
| `InventoryAdapter` | HTTP wire ↔ adjust/query models |
| `StockEventAdapter` | `OrderPlacedEvent` / confirmed / cancelled wire ↔ models; build outbound stock events |

## 6. wire/in and wire/out

| Direction | Type |
|-----------|------|
| **wire/in** | `AdjustStockRequest` |
| **wire/out** | `StockAvailabilityResponse` |

Inbound async payloads: `shared.wire.in.events.OrderPlacedEvent`, `OrderConfirmedEvent`, `OrderCancelledEvent`.

## 7. diplomat

| Subfolder | Classes |
|-----------|---------|
| **http_server** | `InventoryHttpServer` — `POST /api/v1/inventory/adjustments`, `GET .../skus/{skuId}/availability` |
| **jpa** | `InventoryPersistence`, `StockLevelEntity`, `StockReservationEntity`, `StockMovementEntity`, repositories |
| **inbound** | `LocalStockAvailabilityQuery` → `StockAvailabilityQuery` |
| **producer** | `InventoryEventProducer` — `StockReservedEvent`, `StockReservationFailedEvent` |
| **consumer** | `OrderPlacedConsumer`, `OrderConfirmedConsumer`, `OrderCancelledConsumer` |
| **outbound** | — (callers use contract from `order`) |

## 8. Sync integration and async

**Sync**

| Contract | Implementation |
|----------|----------------|
| `StockAvailabilityQuery` (`available`, `hasAvailabilityForLines`) | `LocalStockAvailabilityQuery` |

**Async** (`EventRoutes`)

| Direction | Event (wire) | Queue |
|-----------|--------------|-------|
| **In** | `OrderPlacedEvent` | `ORDER_PLACED_QUEUE` |
| **In** | `OrderConfirmedEvent` | `ORDER_CONFIRMED_INVENTORY_QUEUE` |
| **In** | `OrderCancelledEvent` | `ORDER_CANCELLED_INVENTORY_QUEUE` |
| **Out** | `StockReservedEvent` | `STOCK_RESERVED_EXCHANGE` (fan-out: order + finance queues) |
| **Out** | `StockReservationFailedEvent` | `STOCK_RESERVATION_FAILED_QUEUE` |

Externalized keys: `STOCK_RESERVED_EXTERNALIZED`, `STOCK_RESERVATION_FAILED_EXTERNALIZED`, plus order event keys on consumed payloads.

## Idempotency

- Each reservation row: `idempotency_key = derive(orderRootKey, "inventory", "reserve", orderId, skuId)` from `OrderPlacedEvent.idempotencyKey`.
- `ReserveStockController` short-circuits when key exists (at-least-once safe); producers always publish on success path.

## 9. Existing tests

| Test | Scope |
|------|--------|
| `StockRulesTest` | Pure rules |
| `ReserveStockControllerTest` | Reserve + idempotency |
| `ArchitectureTest` | Logic isolation; consumer → adapter rule |
| `OrderFlowE2ETest` | End-to-end commerce (includes inventory path) |
| `EventRoutesTest` | Queue naming conventions |
