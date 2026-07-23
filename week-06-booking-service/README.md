# Week 6–7 · Project #3 — Booking Platform

> Full teaching scripts: [`docs/WEEK-6`](../docs/WEEK-6/README.md) · [`docs/WEEK-7`](../docs/WEEK-7/README.md)

Two independently deployable Spring Boot services: **`booking-service`** (this folder, port 8081) and **[`notification-service`](../week-07-notification-service)** (port 8082), talking to each other only through Kafka — no shared code, no direct HTTP calls between them.

## Quick start (single service, H2, no Docker)

```bash
cp .env.example .env
# set JWT_SECRET

./mvnw spring-boot:run
# or F5 → "Week 6 Booking — Run (H2)"
```

- API: http://localhost:8081
- Swagger: http://localhost:8081/swagger-ui.html
- Health: http://localhost:8081/actuator/health

Booking create/cancel still writes to the transactional outbox and the relay still runs, but with no broker reachable it just logs a warning and retries — the REST API works exactly the same either way.

### Seeded users

| Email | Password | Role |
|-------|----------|------|
| customer@codekerdos.in | cust123 | CUSTOMER |
| provider@codekerdos.in | prov123 | PROVIDER |
| admin@codekerdos.in | adm123 | ADMIN |

## Full distributed demo (Postgres + Kafka + both services, one command)

```bash
docker compose up --build
```

This builds and runs **four** containers: Postgres, a single-node KRaft Kafka broker (no Zookeeper), `booking-service`, and `notification-service`. Once healthy:

```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"customer@codekerdos.in","password":"cust123"}' | jq -r .token)

# 2. Book a slot — writes Booking + an outbox row in the same DB transaction
curl -s -X POST http://localhost:8081/api/bookings \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -H "Idempotency-Key: demo-1" -d '{"slotId":1,"notes":"Need projector"}'

# 3. Within ~2s the outbox relay publishes BookingConfirmed to Kafka.
#    notification-service consumes it and records a notification:
curl -s http://localhost:8082/api/notifications | jq
```

Cancel the same booking (`PATCH /api/bookings/1/cancel`) and a second, independent notification shows up — the compensating step of the choreography saga, no orchestrator involved.

Run only Postgres for local `./mvnw` dev without full Docker:

```bash
docker compose up -d postgres
# run with SPRING_PROFILES_ACTIVE=postgres
```

## What's actually implemented (vs. taught-but-not-yet-built)

| Concept | Status |
|---|---|
| JWT + role-based `@PreAuthorize` | ✅ |
| Idempotency-Key on booking create | ✅ |
| State machine (booking lifecycle) | ✅ |
| Spring AOP `@Around` audit logging | ✅ |
| N+1 fix via `@EntityGraph` | ✅ |
| Native SQL reporting query (`GET /api/bookings/stats/providers`) | ✅ |
| Transactional outbox (DB write + "send an event" commit atomically) | ✅ |
| Kafka producer/consumer across 2 services | ✅ — see [`notification-service`](../week-07-notification-service) |
| Choreography saga with cancel compensation | ✅ |
| Idempotent consumer (dedupe by eventId) | ✅ |
| Docker Compose: Postgres + Kafka + both services, one command | ✅ |
| `ai-search-service` — embeddings / RAG (Week 7 Sunday) | ❌ not built yet |

## Roadmap

| Week | Focus |
|------|--------|
| 6 Sat | Domain + JWT + REST + OpenAPI |
| 6 Sun | Idempotency · AOP · N+1 · tests · Docker |
| 7 Sat | notification-service · Kafka · saga |
| 7 Sun | ai-search-service · embeddings · RAG |
