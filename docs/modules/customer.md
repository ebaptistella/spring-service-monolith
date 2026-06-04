# Module: customer

## 1. Module responsibility (Modulith boundary)

Customer registration and lookup. Persists customers, caches reads in Redis, publishes `CustomerCreatedEvent` for welcome/notification flow, and exposes **sync** `CustomerQuery` for order validation and email resolution.

`allowedDependencies = {"shared"}`.

## 2. models

| Type | Role |
|------|------|
| `Customer` | id, email, fullName, createdAt |
| `CustomerRegistrationInput` | Registration command (email, fullName) |

## 3. logic

| Type | Role |
|------|------|
| `CustomerRules` | Reject duplicate email; build new `Customer` |
| `CustomerRegistrationResult` | Success / rejected (duplicate email) |

## 4. controllers (logic sandwich)

| Controller | Flow |
|------------|------|
| `RegisterCustomerController` | Check email exists → rules → save → `CustomerEventProducer` |
| `GetCustomerController` | Load by id (cache-backed read path via persistence) |

## 5. adapters

| Adapter | Role |
|---------|------|
| `CustomerAdapter` | `CreateCustomerRequest` ↔ `CustomerRegistrationInput`; `Customer` → `CustomerResponse` |
| `CustomerEventAdapter` | `Customer` → `CustomerCreatedEvent` |

## 6. wire/in and wire/out

| Direction | Type |
|-----------|------|
| **wire/in** | `CreateCustomerRequest` |
| **wire/out** | `CustomerResponse` |

Published event wire: `shared.wire.in.events.CustomerCreatedEvent`.

## 7. diplomat

| Subfolder | Classes |
|-----------|---------|
| **http_server** | `CustomerHttpServer` — `POST/GET /api/v1/customers`; `@Observed` por endpoint |
| **jpa** | `CustomerPersistence`, `CustomerEntity`, `CustomerJpaRepository` |
| **cache** | `CustomerCacheConfiguration`, `CustomerCacheNames`, `CustomerCacheReader` |
| **inbound** | `LocalCustomerQuery` → `CustomerQuery` |
| **producer** | `CustomerEventProducer` |
| **consumer** | — |
| **outbound** | — |

## 8. Sync integration and async

**Sync**

| Contract | Implementation | Consumers |
|----------|----------------|-----------|
| `CustomerQuery` (`exists`, `findEmail`) | `LocalCustomerQuery` | `order` via `LocalCustomerGateway` |

**Async** (`EventRoutes`)

| Direction | Event | Queue / exchange |
|-----------|-------|------------------|
| **Out** | `CustomerCreatedEvent` | `CUSTOMER_CREATED_EXCHANGE` → `CUSTOMER_CREATED_QUEUE` |

`CUSTOMER_CREATED_EXTERNALIZED` = `monolith.customer-created::monolith.customer-created`.

Subscriber: `notification` (`CustomerCreatedConsumer`).

## Idempotency

- `POST /api/v1/customers` requires `X-Idempotency-Key` (`R`).
- Entity key: `IdempotencyKeys.derive(R, "customer.register")`; stored as `idempotency_key` with `root_idempotency_key` and `request_fingerprint`.
- HTTP replay (same key + body) → `200 OK`; fingerprint mismatch → `409`; replay skips `CustomerEventProducer`.

## 9. Existing tests

| Test | Scope |
|------|--------|
| `CustomerRulesTest` | Registration rules |
| `CustomerAdapterTest` | Wire ↔ model |
| `RegisterCustomerControllerTest` | Register + event publish |
| `CustomerRegistrationE2ETest`, `ZCustomerJwtAuthE2ETest` | HTTP + welcome email via `SentEmailRecorder` |
| `CustomerModuleIT` | Modulith scenario — publica `CustomerCreatedEvent` |
| `ArchitectureTest` | Layering |
| `EventRoutesTest`, `RabbitTopologyConfigurationTest` | Topology includes customer queue |
