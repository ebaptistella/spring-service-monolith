# Module: email

## 1. Module responsibility (Modulith boundary)

Email dispatch execution: validates dispatch requests, deduplicates by `dispatchId`, sends via pluggable `EmailSender` (in-memory for tests, SMTP in production), with spring-retry on the sender delegate and Sentry on failure.

Event-driven only (no HTTP). `allowedDependencies = {"shared"}`.

## 2. models

| Type | Role |
|------|------|
| `EmailDispatch` | dispatchId, to, subject, body |

## 3. logic

| Type | Role |
|------|------|
| `EmailDispatchRules` | Required to/subject validation |
| `EmailDispatchValidationResult` | Valid / rejected |
| `SentEmailRecorder` | Test hook implemented by `InMemoryEmailSender` |

## 4. controllers (logic sandwich)

| Controller | Flow |
|------------|------|
| `SendEmailController` | Validate → dedup by `dispatchId` → `ResilientEmailSender.send` |
| `ResilientEmailSender` | `@Retryable` + `@Recover` → `EmailSender`; fallback throws `EmailDispatchException` + Sentry |

## 5. adapters

| Adapter | Role |
|---------|------|
| `EmailDispatchEventAdapter` | `EmailDispatchRequestedEvent` → `EmailDispatch` |

## 6. wire/in and wire/out

No module HTTP wire. Inbound async: `shared.wire.in.events.EmailDispatchRequestedEvent`.

## 7. diplomat

| Subfolder | Classes |
|-----------|---------|
| **http_server** | — |
| **jpa** | — |
| **producer** | — |
| **consumer** | `EmailDispatchConsumer` |
| **inbound** | — |
| **outbound** | — |
| **smtp** | `SmtpEmailSender` |
| **inmemory** | `InMemoryEmailSender` (also `SentEmailRecorder`) |
| **Root** | `EmailSender` interface, `ResilientEmailSender` |

## 8. Sync integration and async

**Sync**

None.

**Async** (`EventRoutes`)

| Direction | Event | Queue |
|-----------|-------|-------|
| **In** | `EmailDispatchRequestedEvent` | `EMAIL_DISPATCH_QUEUE` |

`EMAIL_DISPATCH_EXTERNALIZED` = `monolith.email-dispatch-requested::monolith.email-dispatch-requested`.

Publisher: `notification` (`NotificationEventProducer`).

## Idempotency

- `SendEmailController` deduplicates by deterministic `dispatchId` from `EmailDispatchAdapter` (`derive` on event root key + template scope).
- No HTTP header; idempotency is event-step only.

## 9. Existing tests

| Test | Scope |
|------|--------|
| `EmailDispatchEventAdapterTest` | Wire → model |
| `ArchitectureTest` | Consumer → adapter; logic isolation |
| `CustomerRegistrationE2ETest`, `ZCustomerJwtAuthE2ETest`, `OrderFlowE2ETest`, `LocalAuthE2ETest` | Assert sent mail via `SentEmailRecorder` / in-memory sender |
| `RabbitTopologyConfigurationTest`, `EventRoutesTest` | Email queue topology |

No dedicated `SendEmailController` unit test.
