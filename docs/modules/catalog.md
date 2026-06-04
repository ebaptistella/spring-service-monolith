# Module: catalog

## 1. Module responsibility (Modulith boundary)

Product catalog: products, SKUs, list prices, and active flags. Exposes HTTP CRUD-style SKU APIs and a **sync read contract** (`CatalogQuery`) for other modules (e.g. `order` validates SKU and price at place-order time).

`package-info`: `allowedDependencies = {"shared"}` only — no direct imports from other modules.

## 2. models

| Type | Role |
|------|------|
| `Product` | Catalog product aggregate id, name, description, active, timestamps |
| `Sku` | SKU under a product: code, list price, active |
| `CreateSkuInput` | Domain input for SKU creation |

## 3. logic

| Type | Role |
|------|------|
| `CatalogRules` | Validates create-SKU input; builds `Product` + `Sku` ids and defaults |
| `CreateSkuResult` | Success / rejected with error message |
| `CreatedSku` | Pair of product + sku produced by rules |

## 4. controllers (logic sandwich)

| Controller | Flow |
|------------|------|
| `CreateSkuController` | Rules → persist product+sku via `CatalogPersistence` |
| `QueryCatalogController` | Load SKU by id (`findById` / `getById`) |

## 5. adapters

| Adapter | Role |
|---------|------|
| `CatalogAdapter` | `CreateSkuRequest` ↔ `CreateSkuInput`; `Sku` → `SkuResponse` |
| `CatalogQueryAdapter` | `Sku` → contract fields (`isSkuActive`, `getListPrice`) |

## 6. wire/in and wire/out

| Direction | Type |
|-----------|------|
| **wire/in** | `CreateSkuRequest` |
| **wire/out** | `SkuResponse` |

No module-local event wire; integration events live in `shared.wire.in.events`.

## 7. diplomat

| Subfolder | Classes |
|-----------|---------|
| **http_server** | `CatalogHttpServer` — `POST/GET /api/v1/catalog/skus` |
| **jpa** | `CatalogPersistence`, `ProductEntity`, `SkuEntity`, `ProductJpaRepository`, `SkuJpaRepository` |
| **inbound** | `LocalCatalogQuery` implements `shared.contracts.catalog.CatalogQuery` (reads JPA only) |
| **producer** | — |
| **consumer** | — |
| **outbound** | — |

## 8. Sync integration and async

**Sync**

| Contract | Implementation | Consumers (other modules) |
|----------|----------------|---------------------------|
| `CatalogQuery` (`isSkuActive`, `getListPrice`) | `LocalCatalogQuery` | `order` via `LocalCatalogGateway` |

**Async**

None in this module (no `EventRoutes` usage).

## Idempotency

- `POST /api/v1/catalog/skus` requires `X-Idempotency-Key`; SKU row stores derived `idempotency_key` (`catalog.sku.create` scope).

## 9. Existing tests

| Test | Scope |
|------|--------|
| `CatalogRulesTest` | Pure rules |
| `CatalogAdapterTest`, `CatalogQueryAdapterTest` | Adapters |
| `CreateSkuControllerTest` | Controller + mocked JPA |
| `ArchitectureTest` | Layering for `catalog` |
