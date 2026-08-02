# Week 7

| Class | When | File | Code |
|-------|------|------|------|
| **Class 1** | Saturday | [Class-1.md](Class-1.md) | `week-06-booking-service` (+ notification module) |
| **Class 2** | Sunday | [Class-2.md](Class-2.md) | `week-06-booking-service` (+ ai-search module) |

**Project #3 completes this week.**

**Deep dive:** [Docker.md](Docker.md) — Dockerfile, Compose, listeners, healthchecks, explained line by line.

| Week | Classes | Covers |
|------|---------|--------|
| Week 6 | Sat + Sun | Monolith booking API hardened |
| **Week 7** | Sat + Sun | Microservices · Kafka · Saga · RAG · **complete** |

**Starting checkpoint:** Week 6 done — idempotency, AOP, tests, Docker Postgres.

[← Week 6](../WEEK-6/README.md) · [← START-HERE](../../START-HERE.md)

---

## HLD — Distributed Booking (this week)

### Target topology

```
booking-service:8081  ──produces──►  Kafka[booking.events]  ──consumes──►  notification-service:8082
        │
        └── REST (optional) ──►  ai-search-service:8083  (embeddings + RAG)
```

### Saga (choreography)

| Step | Service | Action |
|------|---------|--------|
| 1 | booking | Confirm booking + reserve capacity |
| 2 | booking | Publish `BookingConfirmed` |
| 3 | notification | Send / log notification |
| 4 | booking (cancel) | Release capacity + publish `BookingCancelled` |
| 5 | notification | Send cancel notice (**compensation** path) |

### Kafka events (JSON)

```json
{
  "eventId": "uuid",
  "type": "BookingConfirmed",
  "occurredAt": "2026-07-12T10:00:00Z",
  "bookingId": 42,
  "slotId": 7,
  "customerEmail": "customer@codekerdos.in"
}
```

Consumers store `eventId` — **idempotent consumers**.

### RAG flow (Sunday)

```
query → embed → SimpleVectorStore similarity → top slots → ChatClient prompt → answer
```

---

*CodeKerdos.in · Week 7 · Project #3 finale*
