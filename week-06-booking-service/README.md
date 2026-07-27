# Week 6–7 · Project #3 — Booking Platform

> Full teaching scripts: [`docs/WEEK-6`](../docs/WEEK-6/README.md) · [`docs/WEEK-7`](../docs/WEEK-7/README.md)  
> Ops / troubleshooting: [`RUNBOOK.md`](../RUNBOOK.md)

Two independently deployable Spring Boot services:

| Service | Port | Role |
|---------|------|------|
| **`booking-service`** (this folder) | 8081 | Auth, slots, bookings, outbox, AI search/RAG |
| **[`notification-service`](../week-07-notification-service)** | 8082 | Consumes Kafka events, stores notifications |

---

## What we are building

A **booking platform** — meeting rooms, doctor visits, consultant slots, interview scheduling.

```
Customer searches slots → books (with idempotency) → booking confirmed
                       → Kafka event → notification-service notifies
                       → cancel → compensation (release capacity + cancel notification)
```

**Roles:** `CUSTOMER` (book/cancel) · `PROVIDER` (create slots) · `ADMIN` (all bookings + stats)

---

## Architecture

```
Client (Postman / Swagger)
        │
        ▼  sync HTTP (JWT)
booking-service :8081
        │
        ├── PostgreSQL / H2  (bookings, slots, outbox_events)
        │
        ├── async Kafka topic "booking.events"
        │         │
        │         ▼
        │   notification-service :8082
        │
        ├── in-process AI (embeddings + RAG on /api/ai/*)
        │
        └── optional Thymeleaf UI at /ui/** (session login, separate from JWT API)
```

### Inter-service communication

| Path | Type | When |
|------|------|------|
| Client → `booking-service` | **Sync REST** | Login, list slots, book, cancel |
| `booking-service` → Kafka → `notification-service` | **Async** | Booking confirmed / cancelled (main flow) |
| Thymeleaf UI → `notification-service` | **Sync REST** | Dashboard reads notification feed only |
| `UiController` → `BookingService` | **In-process** | Same JVM, no HTTP between UI and API |

**Important:** Kafka is **asynchronous** — the booking API does not wait for the notification to be sent. The consumer reacts later.

---

## Quick start (single service, H2, no Docker)

```bash
cp .env.example .env
# set JWT_SECRET (and GROQ_API_KEY if you want /api/ai/ask to actually answer)

./mvnw spring-boot:run
```

- API: http://localhost:8081
- Swagger: http://localhost:8081/swagger-ui.html
- Health: http://localhost:8081/actuator/health
- Thymeleaf UI (optional): http://localhost:8081/ui/login

Booking create/cancel still writes to the transactional outbox and the relay still runs, but with no broker reachable it just logs a warning and retries — the REST API works exactly the same either way.

### Seeded users

| Email | Password | Role |
|-------|----------|------|
| customer@codekerdos.in | cust123 | CUSTOMER |
| provider@codekerdos.in | prov123 | PROVIDER |
| admin@codekerdos.in | adm123 | ADMIN |

---

## Full distributed demo (Postgres + Kafka + both services)

```bash
docker compose up --build
```

Runs four containers: Postgres, Kafka (KRaft), `booking-service`, `notification-service`.

```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"customer@codekerdos.in","password":"cust123"}' | jq -r .token)

# 2. Book — same DB transaction writes Booking + outbox row
curl -s -X POST http://localhost:8081/api/bookings \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -H "Idempotency-Key: demo-1" -d '{"slotId":1,"notes":"Need projector"}'

# 3. Within ~2s outbox relay publishes BookingConfirmed to Kafka.
#    notification-service consumes and records a notification:
curl -s http://localhost:8082/api/notifications | jq
```

Cancel (`PATCH /api/bookings/1/cancel`) publishes `BookingCancelled` — the saga compensation path.

Postgres only (no full stack):

```bash
docker compose up -d postgres
# SPRING_PROFILES_ACTIVE=postgres ./mvnw spring-boot:run
```

---

## Key concepts (Week 6 Sunday)

### 1. Idempotency-Key

**Problem:** Network retries can send the same `POST /api/bookings` twice → risk of double booking.

**Solution:** Client sends a unique header:

```
Idempotency-Key: <uuid>
```

**Flow** (`BookingService.create`):

1. If key already exists in DB → return the **same** booking (no new row).
2. If key is new → create booking and store the key.

```java
bookingRepository.findByIdempotencyKey(idempotencyKey)
    .map(BookingResponse::from)
    .orElseGet(() -> createNew(...));
```

**DB support:** `bookings.idempotency_key` has a **unique index** — duplicate keys cannot be inserted.

**What it protects:** Retries from the **same client** with the **same key**.  
**What it does not protect:** Two different users racing for the last seat (no row locking — see honest gaps below).

**Files:** `BookingController` (header) · `BookingService` · `Booking.idempotencyKey` · `BookingRepository.findByIdempotencyKey`

---

### 2. Spring AOP — audit logging

**Problem:** You want every service method logged (start, success, failure, duration) without copy-pasting `log.info` into every method.

**Solution:** `ServiceAuditAspect` — cross-cutting concern via AOP.

```java
@Aspect
@Component
public class ServiceAuditAspect {
    @Pointcut("within(in.codekerdos.booking.service..*)")
    public void serviceLayer() {}

    @Around("serviceLayer()")
    public Object audit(ProceedingJoinPoint pjp) throws Throwable { ... }
}
```

**What happens:**
- Before any method in `in.codekerdos.booking.service..*` → log `AUDIT start`
- After success → log `AUDIT ok` + elapsed ms
- On exception → log `AUDIT fail` + rethrow

**Why AOP:** Business logic stays clean; auditing is applied in **one place** to all services (`BookingService`, `SlotService`, `AuthService`, etc.).

**Dependency:** `spring-boot-starter-aop` in `pom.xml`.

---

### 3. N+1 problem and `@EntityGraph`

**Problem (N+1 queries):**

`Booking` and `Slot` use **lazy** `@ManyToOne` relations:

```java
@ManyToOne(fetch = FetchType.LAZY)
private Slot slot;

@ManyToOne(fetch = FetchType.LAZY)
private AppUser customer;
```

Loading 10 bookings and then calling `booking.getSlot().getTitle()` and `booking.getCustomer().getEmail()` for each can trigger:

- 1 query for bookings
- 10 queries for slots
- 10 queries for customers  
→ **21 queries** (classic N+1)

Same for slots + provider when building `SlotResponse` with `slot.getProvider().getEmail()`.

**Solution:** `@EntityGraph` — fetch associations in **one JOIN query** for that repository call.

```java
// BookingRepository
@EntityGraph(attributePaths = {"slot", "customer"})
List<Booking> findByCustomer_Email(String email);

@EntityGraph(attributePaths = {"slot", "customer"})
List<Booking> findAllWithDetails();

// SlotRepository
@EntityGraph(attributePaths = {"provider"})
List<Slot> findByStatus(SlotStatus status);
```

**When to use:** Any time you know you will read child entities in the same transaction (e.g. mapping to `BookingResponse` / `SlotResponse`).

**Alternative (not used here):** `JOIN FETCH` in JPQL — same idea, different syntax.

---

### 4. Booking state machine

Legal transitions only (`BookingStateMachine`):

```
PENDING   → CONFIRMED
PENDING   → CANCELLED
CONFIRMED → CANCELLED
```

Illegal transitions throw `BusinessException`.

---

## Key concepts (Week 7)

### Transactional outbox + Kafka

**Problem:** You must update the DB **and** send a Kafka message. Doing them separately risks: DB committed but message lost (or the reverse).

**Solution:**

1. In the **same `@Transactional`** as the booking write → insert row into `outbox_events`.
2. `OutboxRelay` (scheduled every 2s) polls unpublished rows → publishes to `booking.events`.
3. `notification-service` consumes → saves notification.

**Delivery:** at-least-once — consumer dedupes by `eventId` (`processed_events` table).

**Files:** `OutboxEventRecorder` · `OutboxRelay` · `BookingEventListener` (notification-service)

### Choreography saga (cancel = compensation)

| Step | Service | Action |
|------|---------|--------|
| 1 | booking | Confirm booking, reserve slot capacity |
| 2 | booking | Outbox → `BookingConfirmed` |
| 3 | notification | Send confirmation |
| 4 | booking (cancel) | Release capacity, outbox → `BookingCancelled` |
| 5 | notification | Send cancel notice |

No central orchestrator — **events** drive the flow.

---

## API surface

| Method | Path | Who |
|--------|------|-----|
| POST | `/api/auth/login` | anyone |
| POST | `/api/auth/register` | anyone |
| POST | `/api/slots` | PROVIDER |
| GET | `/api/slots` | authenticated |
| POST | `/api/bookings` | CUSTOMER (+ `Idempotency-Key`) |
| GET | `/api/bookings/mine` | CUSTOMER |
| PATCH | `/api/bookings/{id}/cancel` | CUSTOMER |
| GET | `/api/bookings` | ADMIN |
| GET | `/api/bookings/stats/providers` | ADMIN |
| GET | `/api/ai/search?query=...` | authenticated |
| POST | `/api/ai/ask` | authenticated |
| GET | `/actuator/health` | anyone |

Thymeleaf (session-based, not JWT): `/ui/login` · `/ui/dashboard` · `/ui/bookings/mine`

---

## What's implemented

| Concept | Status |
|---------|--------|
| JWT + `@PreAuthorize` | ✅ |
| Idempotency-Key on booking create | ✅ |
| State machine (booking lifecycle) | ✅ |
| Spring AOP `@Around` audit logging | ✅ |
| N+1 fix via `@EntityGraph` | ✅ |
| Native SQL reporting (`GET /api/bookings/stats/providers`) | ✅ |
| Transactional outbox | ✅ |
| Kafka producer/consumer (2 services) | ✅ |
| Choreography saga + cancel compensation | ✅ |
| Idempotent consumer (dedupe by `eventId`) | ✅ |
| Docker Compose (Postgres + Kafka + both services) | ✅ |
| Thymeleaf UI (`/ui/**`) | ✅ |
| Semantic search (`GET /api/ai/search`) | ✅ |
| RAG (`POST /api/ai/ask`) | ✅ |

### Honest gaps (good interview talking points)

- **No optimistic/pessimistic locking** on slot capacity — idempotency handles retries, not concurrent last-seat races.
- **AI runs in-process** in `booking-service` (not a separate `ai-search-service` module).
- **Thymeleaf is optional** — class focus is REST + Postman; `/ui` is for browser demo.

---

## AI search / RAG (Week 7 Sunday)

```
User query → EmbeddingModel (local ONNX, all-MiniLM-L6-v2) → SimpleVectorStore
           → top-K slots re-fetched from DB → ChatClient (Groq) → answer
```

- `GET /api/ai/search?query=...` — semantic search (no `GROQ_API_KEY` needed; embeddings are local).
- `POST /api/ai/ask` — RAG over live slot catalog (needs `GROQ_API_KEY`).
- New slots indexed on create; all slots re-indexed at startup.

Embeddings: `spring-ai-transformers` + local PyTorch runtime. Chat: Groq OpenAI-compatible API (Groq has no embeddings API).

---

## 4-class roadmap

| Class | Topic |
|-------|--------|
| Week 6 Sat | Domain + JWT + REST + OpenAPI |
| Week 6 Sun | Idempotency · AOP · N+1 · tests · Docker |
| Week 7 Sat | notification-service · Kafka · saga |
| Week 7 Sun | Embeddings · SimpleVectorStore · RAG · project complete |

---

## Portfolio one-liner

> Built a booking platform with JWT, idempotent booking APIs, AOP audit, transactional outbox + Kafka saga across microservices, and Spring AI RAG for semantic slot search.
