# Convenções do monólito

Guia **resumido** de padrões para reduzir boilerplate mantendo **Diplomat Architecture + Spring Modulith**.

> **Implementação detalhada:** receitas passo a passo, anti-patterns, nomenclatura e checklist de PR em **[GUIDELINES.md](GUIDELINES.md)**.

## Ferramentas

| Ferramenta | Uso | Evitar |
|------------|-----|--------|
| **Lombok** | `@RequiredArgsConstructor`, `@Slf4j`, getters/setters em JPA | `@Data` em `@Entity` |
| **Spring Cache** | `@Cacheable` / `@CachePut` no diplomat JPA/cache | Camada de cache separada em CRUD simples |
| **Records** | Models, wire DTOs, eventos | Entidades JPA (use classes + Lombok) |
| **Resilience4j** | — | **Removido**; usar spring-retry abaixo |
| **spring-retry** | `@Retryable` / `@Recover` em SMTP e HTTP OAuth2 externo | Em CRUD local |
| **ProblemDetailSupport** | Erros RFC 7807 no filter e no `GlobalExceptionHandler` | JSON manual ad hoc |
| **Sentry** | Erros não tratados e fallbacks de resiliência | Fluxo feliz |
| **Modulith Events API** | `@Externalized` + `ModulithEventConfiguration` | Eventos internos sem contrato |

## Estrutura de pacotes

```text
dev.ebaptistella.monolith/
├── MonolithApplication.java
├── config/                    # cross-cutting runtime (security, openapi, rabbit, observability, retry)
│   └── retry/RetryConfiguration.java   # @EnableRetry
├── shared/                    # módulo Modulith OPEN (wire de integração, models/contratos compartilhados)
│   ├── wire/in/events/       # payloads de mensageria (@Externalized)
│   ├── models/auth/          # AuthenticatedUser, Role, AuthProvider (modelo interno pós-auth)
│   ├── contracts/            # IdentityResolver, CurrentUserProvider (SPI config ↔ identity)
│   ├── idempotency/          # IdempotencyContext, IdempotencyKeys, filter helpers
│   ├── resilience/           # NotificationPlatformConcurrencyLimiter
│   ├── web/                  # GlobalExceptionHandler, ProblemDetailSupport
│   └── EventRoutes.java      # exchanges, filas, DLQ, EXTERNALIZED keys
└── modules/<nome>/
    ├── models/                # entidades de domínio
    ├── logic/                 # regras puras (sem Spring, sem I/O)
    ├── controllers/           # casos de uso (@Service) — logic sandwich
    ├── adapters/              # wire ↔ model
    ├── wire/in, wire/out      # DTOs HTTP
    └── diplomat/              # I/O externo (jpa, http_server, consumer, producer, smtp, cache)
```

O diretório **`diplomat/`** dentro de cada módulo é a camada de comunicação externa (equivalente ao pacote `diplomat` na arquitetura Clojure). Não use prefixo `Diplomat` em nomes de classes.

Fonte da estrutura Diplomat: [clojure-guidelines.mdc](../../../cursor-rules/clojure/clojure-guidelines.mdc) (`models`, `logic`, `controllers`, `adapters`, `wire`, `diplomat`).

## Fronteiras Diplomat

Mapa de camadas (Java ↔ Clojure):

| Camada | Pacote | Regra MUST |
|--------|--------|------------|
| **models** | `modules/<m>/models/` | Domínio strict; sem Spring |
| **logic** | `modules/<m>/logic/` | Puro; sem I/O, wire ou Spring |
| **controllers** | `modules/<m>/controllers/` | Logic sandwich; sem HTTP, sem wire |
| **adapters** | `modules/<m>/adapters/` | `wireToModel` / `modelToWireOut`; sem logic/controllers |
| **wire/in** | `wire/in`, `shared/wire/in/events` | Contratos externos loose |
| **wire/out** | `wire/out` | Respostas HTTP strict |
| **diplomat** | `diplomat/*` | I/O fino; delega a controllers |

Subpastas `diplomat/`:

| Subpasta | Uso |
|----------|-----|
| `http_server` | HTTP: wire → adapter → controller → adapter → wire |
| `jpa` | Persistência |
| `producer` | Publicação de eventos |
| `consumer` | wire → `adapter.wireToModel` → controller |
| `inbound` | Implementação de `shared.contracts` (sync) — **MUST** ler via `jpa`, não via controllers |
| `outbound` | Gateways que consomem contratos de outros módulos |

### Quando usar sync vs async

| Necessidade | Padrão | Onde |
|-------------|--------|------|
| Leitura/validação imediata na API | `shared.contracts.*` | `diplomat/inbound/Local*Query` → `diplomat/jpa` |
| Orquestrador chamando outro módulo | Contrato + gateway | `diplomat/outbound/*Gateway` → contrato |
| Coreografia entre módulos | Eventos | `shared.wire.in.events` + `producer` / `consumer` |

Referência de inbound SPI: `customer/diplomat/inbound/LocalCustomerQuery` → `CustomerPersistence`.

## Integração entre módulos

- **Wire** (`shared.wire.in.events`): payloads que cruzam fronteira de mensageria; `@Externalized("exchange::routing-key")`.
- **Models** (`shared.models.auth`): vocabulário interno compartilhado (não é wire — resultado pós-adapter).
- **Contracts** (`shared.contracts`): interfaces SPI entre `config` e módulos (ex.: `IdentityResolver`).
- Constantes de roteamento em `shared.EventRoutes`.
- Consumo via `@RabbitListener` em `modules/<nome>/diplomat/consumer` + adapter wire → model.
- Módulos **não** importam `diplomat` de outros módulos; apenas `shared` + eventos.

### Commerce (catalog, inventory, order, finance)

| Integração | Padrão |
|------------|--------|
| Sync (validação imediata na API) | `shared.contracts.*` implementado em `diplomat/inbound/Local*Query` |
| Sync (orquestrador consumindo outros módulos) | `diplomat/outbound/*Gateway` + `Local*Gateway` delegando aos contratos |
| Async (coreografia) | `@Externalized` em `shared.wire.in.events` + `diplomat/producer` / `diplomat/consumer` |

Filas com **vários consumidores** no mesmo exchange usam **filas por assinante** (mesmo routing key), por exemplo `STOCK_RESERVED_ORDER_QUEUE` e `STOCK_RESERVED_FINANCE_QUEUE`. Nunca compartilhe uma fila entre módulos — cada `@RabbitListener` precisa de fila própria.

### Consumers Rabbit (`diplomat/consumer`)

Padrão obrigatório:

```java
@RabbitListener(queues = EventRoutes.SOME_QUEUE)
public void onEvent(SomeWireEvent wire) {
    someController.handle(SomeEventAdapter.wireToModel(wire));
}
```

- **Recebe** `shared.wire.in.events.*` (wire).
- **Traduz** via `adapters.*EventAdapter.wireToModel`.
- **Delega** ao controller com **model** do módulo.
- Consumer não contém regra de negócio nem logging de rejeição de domínio.

## Controllers (logic sandwich)

1. **Gather** — buscar dados via `diplomat` (JPA, etc.)
2. **Logic** — executar regras puras em `logic/`
3. **Effects** — persistir, publicar eventos, chamar outros diplomats

HTTP fica em `diplomat/http_server` (`*HttpServer`).

## Configuração global (`config/`)

```text
config/
├── security/       # SecurityFilterChain, JWT, Authorization Server
├── openapi/        # springdoc
├── observability/  # Redis/Liquibase tracing
├── modulith/       # métricas e observação Modulith
├── retry/          # RetryConfiguration (@EnableRetry)
├── idempotency/    # IdempotencyWebFilter, IdempotencyAmqpConfiguration
├── ModulithEventConfiguration.java
├── RabbitTopologyConfiguration.java   # exchange + DLX/DLQ por canal
├── RabbitListenerConfiguration.java   # retry + JSON converter
└── rabbit/
    ├── DeadLetterConsumer.java        # consome DLQ e externaliza
    └── SentryDeadLetterReporter.java  # Sentry ao receber na DLQ
```

### RabbitMQ: retry e dead-letter

Cada exchange de evento tem **DLX + DLQ próprios** (`*.dlx`, `*.dlq`, routing `*.dead`), declarados em `RabbitTopologyConfiguration` a partir de `EventRoutes`.

| Etapa | Comportamento |
|-------|----------------|
| Falha no listener | Retry com backoff (`app.rabbit.listener.retry.*`) |
| Esgotadas tentativas | `RejectAndDontRequeueRecoverer` → mensagem vai para DLX/DLQ |
| Mensagem na DLQ | `DeadLetterConsumer` → `SentryDeadLetterReporter` (Sentry + log) |

Filas existentes **sem** argumentos `x-dead-letter-*` precisam ser recriadas (`docker compose down -v` ou delete manual no RabbitMQ).

### Observabilidade em fluxos assíncronos

- **RabbitMQ** (baseline): `spring.rabbitmq.template/listener.simple.observation-enabled=true` injeta/extrai `traceparent` (W3C) nos headers AMQP.
- **Logs**: formato ECS (`logging.structured.format.console=ecs`); MDC inclui `traceId`, `spanId` e `idempotencyKey` (`logging.structured.json.context.include`).
- **Consumers** (`@Observed` + listener observation): spans filhos continuam o trace da mensagem; logs no consumer carregam o mesmo `traceId`.
- **Cadeia notification → email**: publicação do segundo evento ocorre dentro do consumer ativo → trace propagado automaticamente.
- **DLQ / Sentry**: `TraceContextSupport` anexa `traceId`/`spanId` (MDC) e `traceparent` (header da mensagem) ao evento Sentry.
- **Export OTLP/Jaeger**: perfil `observability` (opcional); propagação funciona mesmo com export desligado.

### Identidade e autorização

- **Wire externo:** claims JWT / tokens OAuth2 (parse em `config.security`).
- **Model:** `shared.models.auth.AuthenticatedUser` (principal interno pós-adapter).
- **Contracts:** `shared.contracts.IdentityResolver`, `CurrentUserProvider`.
- **Implementação:** `modules/identity`; infra de filters/JWT em `config/security`.

### Idempotência (`X-Idempotency-Key`)

- **Header obrigatório** em `POST`/`PUT`/`PATCH` sob `/api/**` (exceto `/api/v1/auth/login` e `/api/v1/auth/token`).
- **`R`** (root key do cliente) propagada em wire events como `idempotencyKey` + header AMQP.
- **Entidades raiz** (`orders`, `customers`, `accounts`, …): colunas `idempotency_key` (UNIQUE), `root_idempotency_key`, `request_fingerprint`.
- **Steps saga**: `idempotency_key = IdempotencyKeys.derive(R, escopo, …)` por linha/agregado (`stock_reservations`, `payment_intents`, …).
- **Semântica at-least-once**: producers **sempre publicam** quando invocados; idempotência no **controller step** via `findByIdempotencyKey` — sem dedup no producer.
- **Replay HTTP**: mesma chave + mesmo payload → `200` + body existente; fingerprint diferente → `409`.
- **Utilitários**: `shared/idempotency/*`, filter em `config/idempotency/IdempotencyWebFilter`.
- Após reescrever migrations locais: `docker compose down -v`.

### Java 21 (preferências do projeto)

- **Records** para models, wire, results e `@ConfigurationProperties`.
- **Switch expressions** em enums e estratégias (`PaymentStatus`, `AuthProviderChoice`, etc.).
- **`IdempotencyContext`** via **`ScopedValue`** (`--enable-preview` no toolchain Corretto 21 desta POC)
- **`IdempotencyReplay.resolve`** para replay HTTP idempotente nos controllers.
- **Erros HTTP** via hierarquia **`sealed`** `DomainHttpException` + handler com pattern matching.
- **Coleções:** `.toList()`, `Collectors.toUnmodifiableSet()` em vez de `Collectors.toList/toSet`.
- **Virtual threads:** `spring.threads.virtual.enabled: true` (baseline em `application.yml` e perfil `test`).
- **Text blocks** + `.formatted(...)` para mensagens multi-linha.

### Boot 3.5 (stack e operação)

- **Logging:** ECS nativo (`logging.structured.format.console=ecs`); sem `logstash-logback-encoder`.
- **HTTP outbound:** `@HttpExchange` + `RestClient`/`HttpServiceProxyFactory`; timeouts em `spring.http.client.*`.
- **Resiliência:** `@Retryable`/`@Recover` (sem Resilience4j); limitador de concorrência em `shared/resilience`.
- **Observabilidade HTTP:** `@Observed` nos `*HttpServer`; MDC `idempotencyKey` incluído em `logging.structured.json.context.include`.
- **JPA:** preferir `LAZY` + `@EntityGraph` nos repositórios que carregam agregados.
- **Testes infra:** `@Container` reutilizável + `@DynamicPropertySource` em `IntegrationTestContainers`; `@DirtiesContext(AFTER_CLASS)` evita listeners Rabbit concorrentes entre contextos Spring distintos; testes Modulith com `@EnableScenarios` (`*ModuleIT.java`).

## Novos módulos

```bash
./scripts/create-module.sh billing
```

Depois:

1. Liquibase: `db/changelog/changes/NNN-<modulo>-....sql` + include no master.
2. models → logic → controllers → adapters → diplomat.
3. Eventos cross-module em `shared.wire.in.events`.

## Testes

| Tipo | Convenção |
|------|-----------|
| Unitário | `@Tag("unit")`, mock de persistence/producers |
| Integração | `@Tag("integration")`, `*IT.java`, Testcontainers |
| E2E | `@Tag("e2e")`, `*E2ETest.java`, Rest Assured |
| Modulith | `@EnableScenarios` + `Scenario` em `*ModuleIT.java`; `ModulithActuatorIT` para `/actuator/modulith` |

**Suporte compartilhado:** `IntegrationTestContainers` (infra) → `TestProfileIntegrationTest` (perfil `test`, reset de e-mail entre testes). E2E com auth local/JWT usam `@SpringBootTest` próprio + `@DirtiesContext(AFTER_CLASS)` herdado da base.

## Checklist de revisão

Referência Diplomat: [clojure-guidelines.mdc](../../../cursor-rules/clojure/clojure-guidelines.mdc).

- [ ] Logic sem dependência de diplomat/wire/controllers
- [ ] Adapters sem dependência de logic/controllers
- [ ] `diplomat/inbound` delega a `diplomat/jpa`, não a controllers
- [ ] Controllers sem dependência de wire (HTTP em `diplomat/http_server`)
- [ ] Consumers: wire → `adapter.wireToModel` → controller; sem `log.*` de domínio
- [ ] Entity JPA com `@Getter` / `@Setter` / `@NoArgsConstructor(PROTECTED)`
- [ ] Eventos com `@Externalized` e routing key em `EventRoutes`
- [ ] POST mutáveis documentam `X-Idempotency-Key`; steps persistem `idempotency_key` derivada
- [ ] `@Observed` em I/O relevante (SMTP, consumers, HttpServers)
- [ ] POST mutáveis usam `ProblemDetailSupport` em erros de filter quando aplicável
- [ ] Testes de integração estendem `IntegrationTestContainers`; E2E com perfil `test` preferem `TestProfileIntegrationTest`
