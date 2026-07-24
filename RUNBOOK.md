# Runbook — Booking Platform (Week 6 + Week 7, Project #3)

Everything needed to get `week-06-booking-service` + `week-07-notification-service` running on a **fresh machine**, plus how to test every surface (REST API, Thymeleaf UI, RAG search, Kafka saga). Written so you can paste this into a new laptop with zero prior context.

---

## 1. Prerequisites (install once per machine)

| Tool | Check | Install (Mac) |
|---|---|---|
| JDK 17+ | `java -version` | `brew install openjdk@21` |
| Docker Desktop | `docker info` | https://www.docker.com/products/docker-desktop |
| curl, python3 | usually preinstalled | — |

Maven is **not** required globally — both services ship `./mvnw` (the Maven Wrapper), which downloads the correct Maven version automatically on first run.

> **Corporate proxy / Netskope users:** if you see `PKIX path building failed` errors when the app starts (embedding model download), your JDK's own trust store is missing your company's intercepting-proxy root CA — see [§6 Troubleshooting](#6-troubleshooting).

---

## 2. Get the code

```bash
git clone https://github.com/shivanshjaitly/CK_SpringBoot_Camp__With_AI.git
cd CK_SpringBoot_Camp__With_AI
```

---

## 3. Environment variables

Both services read secrets from a local `.env` file (never committed — see `.gitignore`).

```bash
cp week-06-booking-service/.env.example week-06-booking-service/.env
```

Edit `week-06-booking-service/.env`:

```
GROQ_API_KEY=gsk_your_key_here          # free at https://console.groq.com — used for RAG chat answers
JWT_SECRET=any-string-32-chars-minimum  # any string ≥32 chars, dev-only
```

No `.env` is needed for `week-07-notification-service` — it only needs `KAFKA_BOOTSTRAP_SERVERS`, which defaults to `localhost:29092` and is set automatically inside Docker Compose.

Without `GROQ_API_KEY` the whole app still starts and every feature works **except** `POST /api/ai/ask` (semantic search `GET /api/ai/search` does **not** need it — embeddings are local, only the RAG answer step calls Groq).

---

## 4. Start everything — one command

```bash
cd week-06-booking-service
docker compose up --build
```

This builds and starts **four containers**:

| Container | What |
|---|---|
| `postgres` | Database for booking-service |
| `booking-kafka` | Single-node Kafka broker (KRaft, no Zookeeper) |
| `booking-service` | Port **8081** — REST API + Thymeleaf UI |
| `notification-service` | Port **8082** — Kafka consumer + notifications API |

Wait for all four to show `healthy`:

```bash
docker ps --format "table {{.Names}}\t{{.Status}}"
```

First run downloads a local ONNX embedding model on `booking-service` startup (~90MB, one-time, cached in the Docker image layer after that) — expect it to take longer the very first time.

Stop everything:

```bash
docker compose down
```

Add `-v` to also wipe the Postgres volume (`docker compose down -v`) if you want a totally clean slate next time.

### Running booking-service standalone (no Docker, H2 in-memory DB)

Useful for quick local dev without Postgres/Kafka:

```bash
cd week-06-booking-service
export JWT_SECRET=dev-secret-min-32-characters-long
export GROQ_API_KEY=gsk_your_key_here   # optional, only needed for /api/ai/ask
./mvnw spring-boot:run
```

App comes up on **http://localhost:8081**. Kafka publishing will log warnings and retry harmlessly if no broker is reachable — the REST API and UI work identically either way.

---

## 5. Seeded demo users

| Email | Password | Role |
|---|---|---|
| `customer@codekerdos.in` | `cust123` | CUSTOMER |
| `provider@codekerdos.in` | `prov123` | PROVIDER |
| `admin@codekerdos.in` | `adm123` | ADMIN |

---

## 6. Testing the APIs

### 6.1 Swagger UI (fastest way to explore)

Open **http://localhost:8081/swagger-ui.html** — click **Authorize**, paste a JWT from the login call below, try any endpoint.

### 6.2 REST API via curl

```bash
# Login
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"customer@codekerdos.in","password":"cust123"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['token'])")

# List open slots
curl -s http://localhost:8081/api/slots -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

# Book a slot (Idempotency-Key header — retrying the same key returns the same booking)
curl -s -X POST http://localhost:8081/api/bookings \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -H "Idempotency-Key: demo-key-1" \
  -d '{"slotId":1,"notes":"Need projector"}' | python3 -m json.tool

# My bookings
curl -s http://localhost:8081/api/bookings/mine -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

# Cancel booking 1
curl -s -X PATCH http://localhost:8081/api/bookings/1/cancel -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

### 6.3 Semantic search + RAG (Week 7 Sunday feature)

```bash
# Semantic search — matches by MEANING, not keywords (works without GROQ_API_KEY)
curl -s -G "http://localhost:8081/api/ai/search" \
  --data-urlencode "query=peaceful place for a one-on-one talk" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

# RAG ask — needs a real GROQ_API_KEY set on booking-service
curl -s -X POST http://localhost:8081/api/ai/ask \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"question":"is there a quiet room available this week?"}' | python3 -m json.tool
```

### 6.4 Kafka saga: booking → notification-service

With the full Docker stack running, book or cancel a slot (see 6.2), then within ~2 seconds check:

```bash
curl -s http://localhost:8082/api/notifications | python3 -m json.tool
```

You should see a `BookingConfirmed` (or `BookingCancelled`) entry — proof the transactional-outbox → Kafka → consumer pipeline actually fired.

### 6.5 Thymeleaf UI (browser)

Open **http://localhost:8081/ui/login** in a browser:

1. Log in as `customer@codekerdos.in` / `cust123`
2. **Slots** page — book any OPEN slot
3. **My Bookings** — see status, cancel a booking, see the live notification feed (pulled from `notification-service`'s REST API)

This is a separate, session/cookie-based login from the JWT API — logging into one does not log you into the other.

### 6.6 Automated tests

```bash
cd week-06-booking-service && ./mvnw test
cd week-07-notification-service && ./mvnw test
```

---

## 7. Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `docker compose up` fails pulling `eclipse-temurin:*-alpine` on Apple Silicon | Alpine JRE image has no arm64 build | Already fixed in both Dockerfiles (`-jammy` base) — pull latest code |
| App takes ~45s to start standalone (no Docker) | `KafkaAdmin` tries to reach `localhost:29092` and times out before giving up | Cosmetic only — start Kafka via `docker compose up -d kafka` first, or just wait it out |
| `PKIX path building failed` / `SSLHandshakeException` on first booking-service startup | Your JDK's own trust store (separate from the OS/browser one) doesn't trust your network's intercepting proxy (common on corporate/campus Wi-Fi, e.g. Netskope) | Get your org's CA bundle (`.pem`), then: `keytool -importcert -alias corp-ca -keystore "$(dirname $(dirname $(readlink -f $(which java))))/lib/security/cacerts" -storepass changeit -file /path/to/ca-bundle.pem -noprompt` — re-run afterward |
| `OpenAI API key must be set` | `GROQ_API_KEY` not set | Add it to `week-06-booking-service/.env` (see §3) |
| `/api/ai/ask` returns 500 `Invalid API Key` | `GROQ_API_KEY` is set but wrong/expired | Get a fresh key from https://console.groq.com |
| `GET /api/notifications` empty after booking | `notification-service` not running, or you're not using the full `docker compose up` stack | Check `docker ps` — both `booking-service` and `notification-service` must be up |

---

*CodeKerdos.in · Booking Platform Runbook*
