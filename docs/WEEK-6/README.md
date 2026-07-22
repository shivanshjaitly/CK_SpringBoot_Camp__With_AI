# Week 6

| Class | When | File | Code |
|-------|------|------|------|
| **Class 1** | Saturday | [Class-1.md](Class-1.md) | `week-06-booking-service` |
| **Class 2** | Sunday | [Class-2.md](Class-2.md) | `week-06-booking-service` |

**Project #3 spans Week 6 + Week 7** (4 classes — same rhythm as Expense).

| Week | Classes | Covers |
|------|---------|--------|
| **Week 6** | Sat + Sun | Domain + production REST + JWT · Idempotency · AOP · Testing · N+1 |
| **Week 7** | Sat + Sun | Microservices + Kafka + Saga · Spring AI RAG · **Project #3 complete** |

**Starting checkpoint:** Week 5 done — Expense Approval (Project #2) complete.

Extra: [Groq setup](../groq-setup.md) · **Next → [Week 7](../WEEK-7/README.md)**

[← Back to Week 5](../WEEK-5/README.md) · [← START-HERE](../../START-HERE.md)

---

## HLD — What We Are Building (Week 6 + Week 7)

### The Big Idea

We are building a **Booking Platform** — meeting rooms, doctor appointments, consultant slots, interview scheduling:

> User searches slots (keyword **or** AI semantic search) → books with **idempotency** → system confirms → **Kafka** notifies other services → cancel triggers **saga compensation**.

This is **Project #3** — the bootcamp flagship. Higher weightage than Expense: design patterns, distributed flows, RAG.

---

### Roles & Who Can Do What

```
CUSTOMER  → search slots · book · view / cancel own bookings
PROVIDER  → create / manage slots for their resource
ADMIN     → view all bookings · Actuator ops
```

---

### Domain Model

```
AppUser
├── id, email (unique), password (BCrypt), fullName
└── role: CUSTOMER | PROVIDER | ADMIN

Slot
├── id, title, description
├── resourceType: MEETING_ROOM | DOCTOR | CONSULTANT | INTERVIEW
├── startTime, endTime, location
├── capacity, bookedCount
├── status: OPEN | FULL | CANCELLED
├── provider → AppUser
└── createdAt

Booking
├── id
├── slot → Slot
├── customer → AppUser
├── status: PENDING | CONFIRMED | CANCELLED
├── idempotencyKey (unique, nullable until Class 2)
├── notes
└── createdAt, updatedAt
```

---

### API Surface (Week 6 core)

| Method | Path | Who | What |
|--------|------|-----|------|
| `POST` | `/api/auth/login` | anyone | JWT |
| `POST` | `/api/auth/register` | anyone | register CUSTOMER |
| `POST` | `/api/slots` | PROVIDER | create slot |
| `GET` | `/api/slots` | authenticated | list / filter open slots |
| `GET` | `/api/slots/{id}` | authenticated | slot detail |
| `POST` | `/api/bookings` | CUSTOMER | book (Idempotency-Key header — Class 2) |
| `GET` | `/api/bookings/mine` | CUSTOMER | my bookings |
| `PATCH` | `/api/bookings/{id}/cancel` | CUSTOMER | cancel own booking |
| `GET` | `/api/bookings` | ADMIN | all bookings |
| `GET` | `/actuator/health` | anyone | health |

**Week 7 adds:** Kafka events, notification service, `POST /api/ai/search` (semantic), `POST /api/ai/ask` (RAG).

---

### Microservices Target (Week 7)

```
┌─────────────┐     ┌──────────────────┐     ┌─────────────────────┐
│   Client    │────►│  booking-service │────►│ notification-service│
│  (Postman)  │     │     :8081        │     │       :8082         │
└─────────────┘     └────────┬─────────┘     └──────────▲──────────┘
                             │                          │
                             │  Kafka: booking.events   │
                             └──────────────────────────┘
                             │
                    ┌────────▼─────────┐
                    │ ai-search-service│
                    │      :8083       │
                    │ embeddings + RAG │
                    └──────────────────┘
```

**Week 6:** one Spring Boot app (booking-service).  
**Week 7 Sat:** extract notification + wire Kafka + choreography saga.  
**Week 7 Sun:** AI search service + RAG demo.

---

### Design Patterns (teach explicitly)

| Pattern | Where |
|---------|-------|
| **State** | Booking lifecycle PENDING → CONFIRMED → CANCELLED |
| **Strategy** | Slot validation by `resourceType` |
| **Repository** | Spring Data JPA |
| **Outbox / Event** | Publish `BookingCreated` / `BookingCancelled` (Week 7) |
| **Idempotent Consumer** | Notification service ignores duplicate event ids |

---

### Idempotency

```
Client → POST /api/bookings
         Header: Idempotency-Key: <uuid>
Server → if key seen → return stored response (same booking)
         else → create booking, store key → response
```

Safe retries. No double-booking from network blips.

---

### Saga (choreography — Week 7)

```
1. booking-service: create Booking PENDING → reserve slot capacity
2. booking-service: CONFIRM → publish BookingConfirmed
3. notification-service: send email/log on BookingConfirmed
4. On cancel: BookingCancelled → release capacity + notify (compensation)
```

No orchestrator class — **events** drive the flow. Interview gold.

---

### Kafka (Week 7 — keep thin)

| Topic | Events |
|-------|--------|
| `booking.events` | `BookingConfirmed`, `BookingCancelled` |

One producer (booking), one–two consumers (notification, optional AI index refresh).

---

### AI + RAG (Week 7 Sunday)

```
User query  →  EmbeddingModel  →  vector search (SimpleVectorStore)
            →  top-K slot descriptions  →  ChatClient (Groq)  →  answer
```

Semantic search: *"quiet room for 1:1 near downtown"* finds meaning, not keywords.

---

### Tech Stack

| Layer | Technology |
|-------|------------|
| Framework | Spring Boot 3.x |
| Security | JWT + `@PreAuthorize` |
| DB | H2 (dev) · Postgres (Docker / prod profile) |
| ORM | JPA · `@EntityGraph` / JOIN FETCH |
| AOP | `@Aspect` audit logging |
| Messaging | Kafka (Week 7) |
| AI | Spring AI · EmbeddingModel · SimpleVectorStore · Groq chat |
| Ops | Actuator · OpenAPI · Docker Compose |
| Tests | Mockito · MockMvc · `@DataJpaTest` · `@SpringBootTest` |

---

### Week-by-Week Build Plan

```
WEEK 6 — Saturday (Class 1)
  └── Scaffold · enums · entities · repos · DTOs · JWT
  └── Slot + Booking services · REST · validation · GlobalExceptionHandler · OpenAPI

WEEK 6 — Sunday (Class 2)
  └── Idempotency-Key · State transitions · AOP audit
  └── N+1 + EntityGraph · Unit / MockMvc / DataJpaTest
  └── Actuator · Docker Compose (app + Postgres)

WEEK 7 — Saturday (Class 1)
  └── Split notification-service · Kafka · saga choreography
  └── Compensation on cancel · correlation / event id

WEEK 7 — Sunday (Class 2) — PROJECT COMPLETE
  └── Embeddings · SimpleVectorStore · semantic search · RAG ask
  └── End-to-end Docker demo · portfolio wrap
```

---

### Flow Diagram (text)

```
Customer                      booking-service                    Kafka / others
   │                                │                                 │
   ├── POST /login ────────────────►│                                 │
   │◄── JWT ────────────────────────┤                                 │
   │                                │                                 │
   ├── GET /slots ─────────────────►│                                 │
   │◄── open slots ─────────────────┤                                 │
   │                                │                                 │
   ├── POST /bookings ─────────────►│  Idempotency-Key check          │
   │   Idempotency-Key: uuid        │  PENDING → CONFIRMED            │
   │◄── 201 BookingResponse ────────┤── publish BookingConfirmed ────►│
   │                                │                                 │
   │                                │◄── notification consumes ───────┤
   │                                │                                 │
   ├── PATCH .../cancel ───────────►│  release capacity               │
   │◄── CANCELLED ──────────────────┤── publish BookingCancelled ────►│
```

---

*CodeKerdos.in · Spring Boot + AI Bootcamp · Project #3*
