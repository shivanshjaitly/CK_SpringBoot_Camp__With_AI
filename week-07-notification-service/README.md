# Week 7 · Project #3 — Notification Service

The second microservice in the Booking Platform. It has **no knowledge of `booking-service`'s code** — it owns its own copy of the `BookingEvent` contract and reacts only to what arrives on the `booking.events` Kafka topic. That's the point: two independently deployable services, coupled only by a JSON shape.

See [`../week-06-booking-service`](../week-06-booking-service) for the producer side and the full distributed demo (`docker compose up --build`).

## Run standalone (needs a broker reachable at `localhost:29092`)

```bash
cd ../week-06-booking-service && docker compose up -d kafka
cd ../week-07-notification-service && ./mvnw spring-boot:run
```

- API: http://localhost:8082
- Health: http://localhost:8082/actuator/health
- `GET /api/notifications` — the 50 most recent simulated notifications (H2 in-memory)

## Why `localhost:29092` and not `9092`

The Kafka broker in `../week-06-booking-service/docker-compose.yml` advertises **two listeners**: `PLAINTEXT://kafka:9092` for other containers on the compose network, and `PLAINTEXT_HOST://localhost:29092` for anything running on the host machine (this service when run via `./mvnw spring-boot:run`, IDE runs, CLI tools). Without the second listener, the broker would tell a host-side client to reconnect to `kafka:9092`, which doesn't resolve outside Docker. When this service runs *inside* Docker Compose instead (see the root `docker-compose.yml`), it's given `KAFKA_BOOTSTRAP_SERVERS=kafka:9092` to use the container-network listener.

The broker also has a `healthcheck` (`kafka-broker-api-versions.sh`) with a 20s `start_period`, so `docker compose up -d kafka` can report the container as "started" before the broker is actually ready to accept connections — if this service fails to connect on first try, wait a few seconds and retry, or use `docker compose up -d --wait kafka` to block until healthy.

## What it does

1. `@KafkaListener` on `booking.events`, consumer group `notification-service`.
2. Idempotent consumer: checks a `processed_events` table by `eventId` before acting — Kafka is at-least-once, so the same event can be redelivered (producer retry, consumer rebalance) and must be a safe no-op.
3. Builds a human-readable message per event type (`BookingConfirmed` / `BookingCancelled`) and persists it to a `notifications` table — this is the compensating step of the choreography saga on cancel, with no orchestrator telling either service what to do.

## Why a separate module instead of a shared library

`booking-service` and `notification-service` each define their **own** `BookingEvent` record with the same fields. Sharing one Java class across services (e.g. via a common JAR) would mean redeploying both together on every contract change — the opposite of what "microservice" is supposed to buy you. The producer sends plain JSON with `spring.json.add.type.headers: false`; the consumer deserializes into its own local type via `spring.json.value.default.type`. Either side's Java model can change shape internally as long as the JSON on the wire stays compatible.
