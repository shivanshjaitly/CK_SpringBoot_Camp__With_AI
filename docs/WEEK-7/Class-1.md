# Week 7 · Class 1 — Saturday · Microservices + Kafka + Saga

> **[← Week 7 Index](README.md)** · **Next → [Class 2 — RAG](Class-2.md)**  
> **Coding folder:** `week-06-booking-service`  
> **Previous ← [Week 6 Class 2](../WEEK-6/Class-2.md)**

---

## CLASS 1 — TOPICS

| # | Topic | Code? |
|---|-------|-------|
| 1 | Week 6 recap — why split services? | Talk |
| 2 | Multi-module layout + notification-service | Code |
| 3 | Docker Compose — Kafka (+ Zookeeper or KRaft) | Code |
| 4 | Domain events + producer in booking-service | Code |
| 5 | Consumer in notification-service | Code |
| 6 | Saga choreography + cancel compensation | Code |
| 7 | Idempotent consumer (`eventId`) | Code |
| 8 | End-to-end demo + wrap | Run |

**Session goal:** Booking publishes events; notification reacts; cancel compensates. RAG = Sunday.

---

**Time split:** ~20 / 40 / 40 / 20 min for intro · split+Kafka · saga · demo.

---

# TOPIC 1 — Why Microservices Here?

### SAY

> "Not because microservices are trendy. Because **notification** and **AI search** change for different reasons and fail differently.
> Booking must stay up if email is down — Kafka buffers."

### DRAW

```
Before:  one JVM does booking + email + AI
After:   booking | notification | ai-search   +  Kafka bus
```

### END THOUGHT

> "Split for independence — not for resume padding."

---

# TOPIC 2 — Multi-module Layout

### YOU DO

Evolve folder to:

```
week-06-booking-service/
├── pom.xml                          ← parent packaging pom
├── docker-compose.yml
├── booking-service/                 ← existing app moved here (8081)
│   └── pom.xml
└── notification-service/            ← NEW (8082)
    └── pom.xml
```

Parent modules: `booking-service`, `notification-service`.  
(ai-search-service added Sunday.)

**notification-service** starters: `web`, `actuator`, `kafka`. No JPA required for v1 (log + optional in-memory sent set).

### END THOUGHT

> "Two boot jars. Topic 3 — Kafka in Compose."

---

# TOPIC 3 — Kafka in Docker Compose

### YOU DO

Add to `docker-compose.yml`:

```yaml
  kafka:
    image: bitnami/kafka:3.7
    ports:
      - "9092:9092"
    environment:
      KAFKA_CFG_NODE_ID: 0
      KAFKA_CFG_PROCESS_ROLES: controller,broker
      KAFKA_CFG_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
      KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
      KAFKA_CFG_CONTROLLER_QUORUM_VOTERS: 0@kafka:9093
      KAFKA_CFG_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_CFG_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE: "true"
```

(Adjust image/env to whatever is stable in class — Bitnami KRaft or Confluent cp-kafka. Pick **one** and stick to it in the script.)

Booking + notification:

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP:localhost:9092}
    consumer:
      group-id: notification-service
      auto-offset-reset: earliest
```

### RUN

`docker compose up -d kafka postgres` → both healthy.

### END THOUGHT

> "Broker up. Topic 4 — producer."

---

# TOPIC 4 — Event Producer

### YOU DO

**`BookingEvent` record** — `eventId`, `type`, `occurredAt`, `bookingId`, `slotId`, `customerEmail`.

**`BookingEventPublisher`:**

```java
@Service
public class BookingEventPublisher {
    public static final String TOPIC = "booking.events";
    private final KafkaTemplate<String, BookingEvent> kafka;

    public void publish(BookingEvent event) {
        kafka.send(TOPIC, event.bookingId().toString(), event);
    }
}
```

After successful confirm / cancel in `BookingService`, publish `BookingConfirmed` / `BookingCancelled` with `UUID.randomUUID()` as `eventId`.

### SAY

> "Key by `bookingId` so events for one booking stay ordered on a partition."

### END THOUGHT

> "Events leave the monolith. Topic 5 — consumer."

---

# TOPIC 5 — Notification Consumer

### YOU DO

```java
@Service
public class BookingEventListener {

    @KafkaListener(topics = "booking.events", groupId = "notification-service")
    public void onMessage(BookingEvent event) {
        switch (event.type()) {
            case "BookingConfirmed" -> log.info("NOTIFY confirm booking={}", event.bookingId());
            case "BookingCancelled" -> log.info("NOTIFY cancel booking={}", event.bookingId());
            default -> log.warn("Unknown type {}", event.type());
        }
    }
}
```

v1 = log. Optional: write to a `notifications` table later.

### RUN

Create booking → notification logs CONFIRM. Cancel → CANCEL log.

### END THOUGHT

> "Choreography is just events + listeners. Topic 6 — saga wording."

---

# TOPIC 6 — Saga + Compensation

### SAY

> "Orchestration = one conductor. **Choreography** = each service reacts.
> Cancel is **compensation**: undo capacity + notify. No 2PC."

### DRAW

```
Book:   reserve → confirm → BookingConfirmed → notify
Cancel: release → cancel  → BookingCancelled → notify (compensate notify)
```

If notification is down, Kafka retains — booking still succeeded. That's the point.

### END THOUGHT

> "Say 'choreography saga' in interviews with this diagram. Topic 7 — consumer idempotency."

---

# TOPIC 7 — Idempotent Consumer

### YOU DO

In-memory `Set<String> processedEventIds` (or DB table):

```java
if (!processed.add(event.eventId())) {
    log.info("Duplicate event {} — skip", event.eventId());
    return;
}
```

### SAY

> "At-least-once delivery → handlers must be idempotent. Same idea as HTTP Idempotency-Key."

### END THOUGHT

> "Topic 8 — demo."

---

# TOPIC 8 — Demo + Wrap

### Demo script

1. Compose: Postgres + Kafka  
2. Start booking-service + notification-service  
3. Book → see Kafka-driven log in notification  
4. Cancel → compensation notify  
5. Replay / duplicate eventId → skipped  

### SAY — Sunday preview

> "Tomorrow: **ai-search-service** — embed slot descriptions, semantic search, RAG ask. Project #3 complete."

---

## Interview Quick Reference

| Question | Answer |
|----------|--------|
| Why Kafka? | Buffer + decouple; booking survives notification outage |
| Saga type? | Choreography via domain events |
| Compensation? | Cancel releases seat + emits BookingCancelled |
| Consumer idempotency? | Dedupe on eventId |

---

*CodeKerdos.in · Week 7 Class 1*
