# Module: identity

Account management, multi-provider identity resolution, local authentication, and unified auth entrypoint.

## Responsabilidade (Modulith)

- Persist `Account`, identity provider links, roles, and local credentials
- JIT provisioning when external JWT/social login arrives
- Unified auth facade at `/api/v1/auth/*` (local or external proxy)
- Admin account API at `/api/v1/accounts/*`
- Implements `shared.contracts.IdentityResolver` and `CurrentUserProvider`

## models

- `Account`, `AccountStatus`, `AccountIdentityLink`, `LocalCredential`
- `LocalRegistrationInput`, `LocalLoginInput`, `LocalLoginResult`
- `AuthConfig`, `ExternalAuthInfo`, `LoginCommand`, `TokenExchangeCommand`, `TokenExchangeResult`
- `AuthOperationException` (em `shared.exception`), `AuthProviderChoice`

## logic

- `AccountRules` — registration and status invariants

## controllers (logic sandwich)

- `RegisterLocalAccountController`, `AuthenticateLocalAccountController` — local auth
- `AuthEntrypointController` — config, register, login, token exchange (models only; no HTTP/wire)
- `ResolveAuthenticatedUserController`, `GetAccountController`, `UpdateAccountStatusController`

## adapters

- `AccountAdapter` — account wire ↔ model
- `AuthAdapter` — auth config and login wire ↔ model

## wire/in e wire/out

- **in:** `RegisterRequest`, `LoginRequest`, `UpdateAccountStatusRequest`
- **out:** `AuthConfigResponse`, `ExternalAuthConfig`, `RegisterResponse`, `LoginResponse`, `AccountResponse`

## diplomat

- `http_server/AuthHttpServer`, `AccountHttpServer` — HTTP, status codes, wire translation
- `jpa/AccountPersistence` — persistence
- `outbound/KeycloakTokenClient` — OAuth2 token proxy
- `security/LocalTokenIssuer` — local JWT issuance

## Integração

| Tipo | Detalhe |
|------|---------|
| Sync SPI | `IdentityResolver`, `CurrentUserProvider` → `config/security` |
| Async | Nenhum |

## Testes

- `AuthEntrypointControllerTest`, `ExternalAuthProxyE2ETest`, `LocalAuthE2ETest`

## Auth strategies

| Strategy | Description |
|----------|-------------|
| `jwt-external` | Keycloak JWT; login/token via transparent proxy at `/api/v1/auth/login` and `/api/v1/auth/token` |
| `local` | Local register/login/token at `/api/v1/auth/*` |
| `social` | OAuth2 Login (Google) |

Clients call **`GET /api/v1/auth/config`** for capabilities. All strategies converge on `AuthenticatedUser` in `shared.models.auth`.

## Idempotency

- `POST /api/v1/auth/register` (local) requires `X-Idempotency-Key`; account stores `idempotency_key` + fingerprint.
- `/auth/login` and `/auth/token` are exempt from the header filter.

## Testes
