# Docker — Detailed Explanation + How It's Used in Week 7

> **[← Week 7 Index](README.md)**

---

## 1. What Docker Actually Is

Docker packages an application **plus its entire runtime environment** (JVM, OS libraries, config) into a single, portable unit called an **image**. When you run that image, you get a **container** — an isolated process with its own filesystem, network, and process tree, but sharing the host's kernel (unlike a VM, which virtualizes the whole OS).

| Concept | What it means |
|---|---|
| **Image** | Read-only template built from a `Dockerfile` — layers of filesystem diffs stacked on top of each other |
| **Container** | A running (or stopped) instance of an image |
| **Dockerfile** | Recipe describing how to build the image, step by step |
| **Volume** | Persistent storage that survives container restarts/recreation |
| **Network** | Virtual network so containers can talk to each other by service name |
| **Docker Compose** | A YAML file (`docker-compose.yml`) that defines and wires up *multiple* containers together as one stack |

**Why it matters here:** in a microservices setup like Week 7's (booking + notification + Kafka + Postgres), you have 4 independent processes that all need to find each other, start in the right order, and behave the same on your laptop as in production. Compose solves exactly that.

---

## 2. How This Repo Uses Docker

### a) The `Dockerfile` — building the booking-service image

```1:14:week-06-booking-service/Dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=5 \
  CMD bash -c 'echo > /dev/tcp/localhost/8081' || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

This is a **multi-stage build**:
1. **Build stage** (`maven:3.9-eclipse-temurin-17`) — has the full Maven + JDK toolchain, compiles the app into a jar. `dependency:go-offline` is run *before* copying `src/` so Docker caches the downloaded dependencies layer and doesn't re-download them every time you change a Java file.
2. **Runtime stage** (`eclipse-temurin:17-jre-jammy`) — a slim image with just the JRE (no compiler, no Maven). Only the built jar is copied over (`COPY --from=build`). This keeps the final image small and avoids shipping build tools into "production."
3. `HEALTHCHECK` lets Docker (and Compose's `depends_on: condition: service_healthy`) know when the app is actually ready to accept traffic — not just "process started."

### b) `docker-compose.yml` — wiring the whole stack together

```1:73:week-06-booking-service/docker-compose.yml
services:
  postgres:
    image: postgres:16-alpine
    ...
  kafka:
    image: apache/kafka:3.7.1
    ...
  booking-service:
    build: .
    ...
    depends_on:
      postgres:
        condition: service_healthy
      kafka:
        condition: service_healthy

  notification-service:
    build: ../week-07-notification-service
    ...
    depends_on:
      kafka:
        condition: service_healthy

volumes:
  booking_pg:
```

Four services, each is its own container:

| Service | Role | Notes |
|---|---|---|
| `postgres` | Database | `booking_pg` named **volume** persists data across `docker compose down/up` — without it, restarting the container wipes your DB |
| `kafka` | Event broker | Single-node **KRaft** mode (no Zookeeper) — `broker,controller` roles combined |
| `booking-service` | Your Spring Boot app | `build: .` → uses the Dockerfile above; connects to Postgres/Kafka using **service names as hostnames** (`postgres:5432`, `kafka:9092`) since Compose puts all services on one internal network |
| `notification-service` | Kafka consumer app | Built from a sibling folder; only needs Kafka, no DB |

Key mechanics worth calling out:

- **`depends_on: condition: service_healthy`** — this is what makes the doc's Topic 3 ("Kafka in Docker Compose") actually reliable. Without it, `booking-service` could start before Kafka/Postgres are truly ready and crash-loop on startup even though the containers technically exist.
- **Dual Kafka listeners** (`PLAINTEXT` vs `PLAINTEXT_HOST`) — containers talk to Kafka via `kafka:9092` (internal Docker network), while your IDE/`mvnw` running *outside* Docker talks via `localhost:29092`. This is a very common real-world gotcha: "inside-network hostname" vs "host-machine port" are different addresses to the same broker.
- **Env var interpolation**: `${JWT_SECRET:-codekerdos-demo-secret...}` and `${GROQ_API_KEY:-}` pull from your shell/`.env` file at compose time, falling back to a default — this is how secrets stay out of the YAML (see `.env.example` in the same folder).

### c) How this maps to the Week 7 docs

`docs/WEEK-7/Class-1.md` Topic 3 teaches the *concept* (a simplified Bitnami Kafka snippet with Zookeeper-less KRaft config), and the actual code folder implements a production-shaped version of it with the `apache/kafka` image, healthchecks, and dependency ordering. The README's target topology —

```
booking-service:8081 ──produces──► Kafka[booking.events] ──consumes──► notification-service:8082
```

— is realized concretely as containers on the same Compose network, each addressable by service name.

---

## 3. Running It

```bash
cd week-06-booking-service
docker compose up -d --build   # build booking-service image + start all 4 containers
docker compose ps              # check health status
docker compose logs -f notification-service   # watch it consume Kafka events
docker compose down            # stop everything (data survives, in the named volume)
docker compose down -v         # stop + wipe the Postgres volume too
```

This is exactly the "Topic 8 — Demo" flow in `Class-1.md`: bring up the stack, book a slot, watch the Kafka-driven notification log appear, cancel to trigger the compensation event.

---

## Interview Quick Reference

| Question | Answer |
|---|---|
| Why multi-stage Dockerfile? | Keep final image small — build tools (Maven/JDK) don't ship to runtime |
| Why Compose over separate `docker run`s? | One file defines networking, ordering, env vars, volumes for all services |
| How do containers find each other? | By service name on Compose's internal DNS network (`kafka:9092`, `postgres:5432`) |
| Why `depends_on` + healthcheck instead of just `depends_on`? | Plain `depends_on` only waits for container *start*, not readiness — healthcheck waits for the app/broker to actually accept connections |
| Why a named volume for Postgres? | Containers are ephemeral; volumes persist data independent of container lifecycle |

---

*CodeKerdos.in · Week 7 · Docker deep dive*
