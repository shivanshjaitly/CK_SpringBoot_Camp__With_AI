# Runbook — Booking Platform (Week 6 + Week 7, Project #3)

Everything needed to get `week-06-booking-service` + `week-07-notification-service` running on a **fresh machine**, plus how to test every surface (REST API, Thymeleaf UI, RAG search, Kafka saga). Written so you can paste this into a new laptop with zero prior context.

---

## 1. Prerequisites (install once per machine)

| Tool | Check | Install (Mac) |
|---|---|---|
| JDK 17+ | `java -version` | `brew install openjdk@21` |
| Docker Desktop | `docker info` | https://www.docker.com/products/docker-desktop |
| Postman | app opens | https://www.postman.com/downloads/ |

Maven is **not** required globally — both services ship `./mvnw` (the Maven Wrapper), which downloads the correct Maven version automatically on first run.

> **Corporate proxy / Netskope users:** if you see `PKIX path building failed` errors, your JDK's own trust store (or the JRE **inside a Docker container**, which is separate again) is missing your company's intercepting-proxy root CA — see [§8 Troubleshooting](#8-troubleshooting).

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

This builds and starts **four containers** (all defined in `week-06-booking-service/docker-compose.yml`, even though `notification-service`'s *source code* lives in the sibling `week-07-notification-service/` folder — the compose file just points `build:` at that folder):

| Container | What | Port |
|---|---|---|
| `week-06-booking-service-postgres-1` | Database for booking-service | 5432 |
| `booking-kafka` | Single-node Kafka broker (KRaft, no Zookeeper) | 29092 (host) / 9092 (in-network) |
| `booking-service` | REST API + Thymeleaf UI | **8081** |
| `notification-service` | Kafka consumer + notifications API | **8082** |

Wait for all four to show `healthy`:

```bash
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

> **Gotcha — starting only one service by name will NOT start everything.**
> `docker compose up -d booking-service` only starts `booking-service` **and its declared dependencies** (`postgres`, `kafka`) — it will **not** start `notification-service`, because nothing depends on it. If you only see 3 of the 4 healthy, run `docker compose up -d` (no service name) to bring up everything that's defined but not yet running.

First run downloads a local ONNX embedding model on `booking-service` startup (~90MB, one-time, cached in the Docker image layer after that) — expect it to take longer the very first time. If your network intercepts HTTPS (VPN/corporate proxy), this download step can fail with `PKIX path building failed` and the container will show `Exited (1)` — see [§8](#8-troubleshooting). **This has been observed to be transient on some networks** — a plain retry sometimes succeeds:

```bash
docker compose up -d booking-service
```

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

## 5. Seeded demo data

Seeded automatically on first startup by `DataLoader` (`week-06-booking-service/src/main/java/in/codekerdos/booking/config/DataLoader.java`) — works the same whether you're on Postgres (Docker) or H2 (standalone).

### Users

| Email | Password | Role |
|---|---|---|
| `customer@codekerdos.in` | `cust123` | CUSTOMER |
| `provider@codekerdos.in` | `prov123` | PROVIDER |
| `admin@codekerdos.in` | `adm123` | ADMIN |

### Slots (both OPEN, owned by `provider@codekerdos.in`)

| id | title | resourceType | capacity |
|---|---|---|---|
| 1 | Quiet downtown meeting room | `MEETING_ROOM` | 4 |
| 2 | Dr. Mehta — follow-up consultation | `DOCTOR` | 1 |

`ResourceType` enum values: `MEETING_ROOM`, `DOCTOR`, `CONSULTANT`, `INTERVIEW`.

These IDs only apply on a fresh database. If you've been experimenting, run `GET /api/slots` first to get real current IDs before hardcoding `slotId` in a booking request.

---

## 6. Full endpoint reference

### booking-service (port 8081)

| Method | Path | Auth / Role | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | none | Register a new **CUSTOMER** account |
| POST | `/api/auth/login` | none | Login, get JWT |
| GET | `/api/slots` | any logged-in user | List OPEN slots (optional `?resourceType=` filter) |
| GET | `/api/slots/{id}` | any logged-in user | Get one slot |
| POST | `/api/slots` | PROVIDER | Create a slot |
| POST | `/api/bookings` | CUSTOMER | Book a slot (optional `Idempotency-Key` header) |
| GET | `/api/bookings/mine` | CUSTOMER | My bookings |
| GET | `/api/bookings` | ADMIN | All bookings |
| PATCH | `/api/bookings/{id}/cancel` | CUSTOMER | Cancel my booking |
| GET | `/api/bookings/stats/providers` | ADMIN | Bookings-per-provider aggregate |
| GET | `/api/ai/search?query=` | any logged-in user | Semantic slot search (no `GROQ_API_KEY` needed) |
| POST | `/api/ai/ask` | any logged-in user | RAG answer over live slot data (needs `GROQ_API_KEY`) |
| GET | `/swagger-ui.html` | — | Interactive API docs |
| GET | `/ui/login`, `/ui/dashboard`, `/ui/bookings/mine` | session cookie | Thymeleaf browser UI |

### notification-service (port 8082)

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/api/notifications` | none (internal service) | 50 most recent notifications, newest first |
| GET | `/actuator/health` | none | Health check |

### 6.1 Endpoint → Postman request map

Every row above maps 1:1 to a request in `docs/postman-week-6-7-booking.json` (import it — see §7). Run the whole thing top-to-bottom in this order to touch every endpoint in one pass:

| Endpoint | Postman folder → request |
|---|---|
| `POST /api/auth/register` | 1. Auth → Register (new CUSTOMER) |
| `POST /api/auth/login` | 1. Auth → Login as Customer / Login as Provider / Login as Admin |
| `GET /api/slots` | 2. Slots → List Open Slots |
| `GET /api/slots?resourceType=` | 2. Slots → List Open Slots (filter by resourceType) |
| `GET /api/slots/{id}` | 2. Slots → Get Slot by Id |
| `POST /api/slots` | 2. Slots → Create Slot (PROVIDER only) |
| `POST /api/bookings` | 3. Bookings → Book a Slot (CUSTOMER) |
| `GET /api/bookings/mine` | 3. Bookings → My Bookings (CUSTOMER) |
| `GET /api/bookings` | 3. Bookings → All Bookings (ADMIN) |
| `PATCH /api/bookings/{id}/cancel` | 3. Bookings → Cancel Booking (CUSTOMER) |
| `GET /api/bookings/stats/providers` | 3. Bookings → Provider Stats (ADMIN) |
| `GET /api/ai/search?query=` | 4. AI Search (RAG) → Semantic Search |
| `POST /api/ai/ask` | 4. AI Search (RAG) → RAG Ask |
| `GET /api/notifications` | 5. Notification Service → Recent Notifications |
| `GET /actuator/health` (notification-service) | 5. Notification Service → Health Check |

Full click-by-click walkthrough with expected results for each of these is in §7.

---

### 6.2 Where AI actually comes into the picture

Two completely different AI mechanisms are involved, and it's worth knowing which is which:

**1. Embeddings (local, free, no API key) — powers `GET /api/ai/search`**

- On every `booking-service` startup, `AiSlotIndexService` reads every `Slot` row and turns `title + description + location + resourceType` into a vector using a small **local ONNX model** (`all-MiniLM-L6-v2`, downloaded once from Hugging Face — this is the ~90MB download from §4). No network call, no Groq key needed — it runs entirely inside the JVM.
- Each vector is stored in an in-memory `VectorStore`, keyed by the slot's database id.
- When you call `GET /api/ai/search?query=...`, `AiSearchService.semanticSearch()` embeds your query the same way and asks the vector store for the top-5 closest matches **by meaning** (cosine similarity), not keyword overlap — that's why "peaceful place for a one-on-one talk" matches "Quiet downtown meeting room" despite sharing zero words. It then re-fetches those slots live from Postgres/H2 (so a stale embedding can never report a slot as bookable if it's actually full/closed) and returns them.

**2. RAG / LLM (needs `GROQ_API_KEY`) — powers `POST /api/ai/ask`**

- `ask()` does the *same* semantic search as above first, to find the top-5 relevant slots.
- It then stuffs those slots as plain-text context into a prompt ("Answer using ONLY the slots listed below...") and sends it to **Groq's hosted `llama-3.3-70b-versatile` model** via Spring AI's `ChatClient`. This step is the only one that leaves your machine and needs an API key/network access.
- The LLM's job here is purely to phrase a natural-language answer from the slots it was handed — it never invents slots that aren't in the context, by instruction.

So: **search = local embeddings only (offline-friendly), ask = local embeddings + a live call to Groq's LLM.** If `GROQ_API_KEY` is missing/invalid, only `ask` breaks; `search` is unaffected because it never touches Groq.

---

## 7. Testing walkthrough (Postman)

Import **`docs/postman-week-6-7-booking.json`** into Postman first: **Import → select the file**. It contains 5 folders covering every step below — **Auth**, **Slots**, **Bookings**, **AI Search (RAG)**, **Notification Service** — pre-wired with the right URLs, headers, and bodies so you never type a JWT by hand.

### 7.0 Live demo — one RUN block, start to finish

For teaching / screen-share: log in, book a slot, then prove the Kafka saga fired by checking the *other* service.

```http
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{ "email": "customer@codekerdos.in", "password": "cust123" }
```

Copy `token` from the response body, then:

```http
POST http://localhost:8081/api/bookings
Authorization: Bearer <token>
Content-Type: application/json
Idempotency-Key: demo-key-1

{ "slotId": 1, "notes": "Need projector" }
```

**201 Created**, status `CONFIRMED`. Now check notification-service — different port, no auth, it's an internal service:

```http
GET http://localhost:8082/api/notifications
```

Within ~2 seconds you should see a new `BookingConfirmed` entry at the top — `booking-service`'s outbox relay published to Kafka, and `notification-service`'s `@KafkaListener` consumed it. **This one round-trip is the entire Week 6 + Week 7 saga in a single screenshot.**

No token on the booking call → **401**. Empty `[]` on the notifications call → `booking-service` or `notification-service` isn't actually healthy; check `docker ps` before blaming Kafka.

### 7.0.1 Every other endpoint, same RUN-block style

**Register a new customer:**

```http
POST http://localhost:8081/api/auth/register
Content-Type: application/json

{ "email": "newuser@codekerdos.in", "password": "pass123", "fullName": "New User" }
```

**201 Created** with the new user's JWT already in the response — no separate login needed. Registering with an email that already exists → **409 Conflict**.

**Log in as Provider and Admin** (needed for the requests below):

```http
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{ "email": "provider@codekerdos.in", "password": "prov123" }
```

```http
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{ "email": "admin@codekerdos.in", "password": "adm123" }
```

Copy the two tokens as `<providerToken>` / `<adminToken>`.

**List open slots:**

```http
GET http://localhost:8081/api/slots
Authorization: Bearer <token>
```

**Filter slots by resource type:**

```http
GET http://localhost:8081/api/slots?resourceType=MEETING_ROOM
Authorization: Bearer <token>
```

Unknown enum value (e.g. `?resourceType=ROOM`) → **400 Bad Request**.

**Get one slot by id:**

```http
GET http://localhost:8081/api/slots/1
Authorization: Bearer <token>
```

Non-existent id → **404 Not Found**.

**Create a slot (PROVIDER only):**

```http
POST http://localhost:8081/api/slots
Authorization: Bearer <providerToken>
Content-Type: application/json

{
  "title": "Rooftop interview room",
  "description": "Bright, quiet space good for technical interviews with a whiteboard.",
  "resourceType": "INTERVIEW",
  "startTime": "2026-08-10T14:00:00",
  "endTime": "2026-08-10T15:00:00",
  "location": "Tower B, Floor 9",
  "capacity": 2
}
```

**201 Created.** Same request with `<token>` (a CUSTOMER, not PROVIDER) instead → **403 Forbidden**. Note the returned `id` — that's the `slotId` for the next booking.

**My bookings:**

```http
GET http://localhost:8081/api/bookings/mine
Authorization: Bearer <token>
```

**Cancel a booking** (use a real id from a booking you made — see §7.0):

```http
PATCH http://localhost:8081/api/bookings/1/cancel
Authorization: Bearer <token>
```

Cancelling someone else's booking, or one that's already cancelled → **403** / **409**.

**All bookings (ADMIN only):**

```http
GET http://localhost:8081/api/bookings
Authorization: Bearer <adminToken>
```

**Bookings-per-provider stats (ADMIN only):**

```http
GET http://localhost:8081/api/bookings/stats/providers
Authorization: Bearer <adminToken>
```

**Semantic search — no GROQ_API_KEY needed:**

```http
GET http://localhost:8081/api/ai/search?query=peaceful place for a one-on-one talk
Authorization: Bearer <token>
```

"Quiet downtown meeting room" should rank first even though it shares almost no words with the query — that's meaning-based search, not keyword search.

**RAG ask — needs a real `GROQ_API_KEY` set on booking-service (see §3):**

```http
POST http://localhost:8081/api/ai/ask
Authorization: Bearer <token>
Content-Type: application/json

{ "question": "is there a quiet room available this week?" }
```

No/invalid `GROQ_API_KEY` → **500** (`OpenAI API key must be set` / `Invalid API Key`). That's expected, not a bug — semantic search above still works without it.

**notification-service health check:**

```http
GET http://localhost:8082/actuator/health
```

`{"status":"UP"}` = healthy.

### 7.1 Swagger UI (fastest way to explore without Postman)

Open **http://localhost:8081/swagger-ui.html** — click **Authorize**, paste a JWT from the login call below, try any endpoint.

### 7.2 Auth

Folder: **1. Auth**

1. **Register (new CUSTOMER)** — optional, seeded users already exist (see §5). Click **Send**.
2. **Login as Customer** — click **Send**. This request has a little script attached that automatically copies the JWT out of the response and saves it into the collection variable `token` — every other request already sends `Authorization: Bearer {{token}}`, so you don't need to copy anything by hand.
3. **Login as Provider** / **Login as Admin** — same idea, save into `providerToken` / `adminToken`, needed for the PROVIDER/ADMIN-only requests below.

`/api/auth/register` always creates a `CUSTOMER` — there's no self-service way to register as PROVIDER/ADMIN; use the seeded `provider@codekerdos.in` / `admin@codekerdos.in` accounts for those roles.

### 7.3 Slots — read and write

Folder: **2. Slots** (run **Login as Customer** first, from §7.2)

1. **List Open Slots** — Send.
2. **List Open Slots (filter by resourceType)** — Send. Change the `resourceType` query param value in the URL bar if you want a different filter.
3. **Get Slot by Id** — uses the collection variable `slotId` (defaults to `1`); edit it in the collection's **Variables** tab if you want a different one.

**Adding new data — create a slot** (run **Login as Provider** from §7.2 first):

4. **Create Slot (PROVIDER only)** — already sends `Authorization: Bearer {{providerToken}}` and a full example body. Send.

Note the `id` in the response — that's the `slotId` you'll use to book it in the next step.

### 7.4 Bookings

Folder: **3. Bookings**

1. **Book a Slot (CUSTOMER)** — sends `Idempotency-Key: demo-key-1` (retrying the same key returns the same booking, doesn't double-book) and body `{"slotId": {{slotId}}, ...}`. Send. Its script auto-saves the returned booking `id` into the collection variable `bookingId`.
2. **My Bookings (CUSTOMER)** — Send.
3. **Cancel Booking (CUSTOMER)** — uses `{{bookingId}}` from step 1. Send.

**Admin-only views** (run **Login as Admin** from §7.2 first):

4. **All Bookings (ADMIN)** — every booking in the system.
5. **Provider Stats (ADMIN)** — bookings count per provider.

### 7.5 Semantic search + RAG (Week 7 Sunday feature)

Folder: **4. AI Search (RAG)**

1. **Semantic Search (no GROQ key needed)** — Send. Expect the "Quiet downtown meeting room" slot to rank first — it's semantically closest to "peaceful place for a one-on-one talk" even though they share almost no exact words.
2. **RAG Ask (needs GROQ_API_KEY on booking-service)** — Send. Requires a real `GROQ_API_KEY` set on `booking-service` (see §3).

If `GROQ_API_KEY` isn't set, **RAG Ask** returns a 500 (`OpenAI API key must be set`) — that's expected, not a bug; **Semantic Search** still works fine without it.

### 7.6 Kafka saga: booking → notification-service

Folder: **5. Notification Service**

With the full Docker stack running, book or cancel a slot (see §7.4), then within ~2 seconds run **Recent Notifications** (no auth needed, plain `GET`).

You should see a `BookingConfirmed` (or `BookingCancelled`) entry — proof the transactional-outbox → Kafka → consumer pipeline actually fired. **An empty `[]` here is expected if you haven't successfully booked/cancelled anything yet** — it's not itself a sign of a broken connection.

**Watching Kafka directly (bypassing both services' REST APIs)** — the most convincing proof, since you see the raw message on the wire instead of trusting `notification-service` to tell you the truth:

```bash
# Terminal A — tail the topic live, before you book anything
docker exec -it booking-kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 --topic booking.events --from-beginning

# Terminal B (or Postman) — now book/cancel a slot (§7.4)
```

Within ~2 seconds of booking, Terminal A prints a raw JSON message like `{"eventId":"...","eventType":"BookingConfirmed","bookingId":1,"slotId":1,"customerEmail":"customer@codekerdos.in",...}` — that's `booking-service`'s `OutboxRelay` publishing straight to the `booking.events` topic, with zero involvement from `notification-service`. Press `Ctrl+C` to stop tailing.

Other useful raw-Kafka commands, all run the same way (`docker exec -it booking-kafka /opt/kafka/bin/<script> ...`):

| What | Command |
|---|---|
| List all topics | `kafka-topics.sh --bootstrap-server localhost:9092 --list` |
| Describe `booking.events` (partitions, replicas) | `kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic booking.events` |
| Check `notification-service`'s consumer group lag (0 = fully caught up) | `kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group notification-service` |

### 7.7 Thymeleaf UI (browser)

Open **http://localhost:8081/ui/login** in a browser:

1. Log in as `customer@codekerdos.in` / `cust123`
2. **Slots** page — book any OPEN slot
3. **My Bookings** — see status, cancel a booking, see the live notification feed (pulled from `notification-service`'s REST API)

This is a separate, session/cookie-based login from the JWT API — logging into one does not log you into the other.

### 7.8 Automated tests

```bash
cd week-06-booking-service && ./mvnw test
cd week-07-notification-service && ./mvnw test
```

---

## 8. Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `docker compose up` fails pulling `eclipse-temurin:*-alpine` on Apple Silicon | Alpine JRE image has no arm64 build | Already fixed in both Dockerfiles (`-jammy` base) — pull latest code |
| App takes ~45s to start standalone (no Docker) | `KafkaAdmin` tries to reach `localhost:29092` and times out before giving up | Cosmetic only — start Kafka via `docker compose up -d kafka` first, or just wait it out |
| `booking-service` container shows `Exited (1)`, logs show `PKIX path building failed` / `SSLHandshakeException` while fetching `huggingface.co/sentence-transformers/...` | The JRE **inside the container** (its own trust store, separate from your host machine's) doesn't trust a certificate presented by your network's intercepting proxy/VPN while downloading the embedding model on first boot | Try a plain retry first — it has been observed to succeed on the second attempt: `docker compose up -d booking-service`. If it keeps failing, you need your org's CA bundle baked into the Docker image (ask to have the Dockerfile updated for this) |
| `PKIX path building failed` when running **standalone** (`./mvnw spring-boot:run`, no Docker) | Same root cause as above, but this time it's your **host JDK's** trust store, not a container's | Get your org's CA bundle (`.pem`), then: `keytool -importcert -alias corp-ca -keystore "$(dirname $(dirname $(readlink -f $(which java))))/lib/security/cacerts" -storepass changeit -file /path/to/ca-bundle.pem -noprompt` — re-run afterward |
| Only 3 of 4 containers show up after `docker compose up -d <name>` | You started one specific service by name — Compose only starts that service plus what it *depends on*, not services downstream of it (e.g. starting `booking-service` won't start `notification-service`) | Run `docker compose up -d` with no service name to start everything defined in the file |
| Postman request to `localhost:8082` says `Could not get response` / `ECONNREFUSED` even though `docker ps` shows it healthy | Almost always means Postman (or whichever terminal you last checked `docker ps` in) is not actually talking to the same Docker daemon/host you think — e.g. a different terminal session, container, VM, or remote environment | Open a terminal on the **exact machine Postman is running on** and run `docker ps` there right before retrying the request, plus `lsof -nP -iTCP:8082 -sTCP:LISTEN` to confirm something is really listening from that machine's point of view. Retry once — Docker Desktop's port-forwarding occasionally has a few-second hiccup |
| `OpenAI API key must be set` | `GROQ_API_KEY` not set | Add it to `week-06-booking-service/.env` (see §3) |
| `/api/ai/ask` returns 500 `Invalid API Key` | `GROQ_API_KEY` is set but wrong/expired | Get a fresh key from https://console.groq.com |
| `GET /api/notifications` returns `[]` forever, even after booking | `booking-service` isn't actually up/healthy (check `docker ps`), or `notification-service` isn't running (see the "only 3 of 4 containers" row above) | Confirm all 4 containers are healthy, then redo the booking → check-notifications sequence in §7.6 |

---

*CodeKerdos.in · Booking Platform Runbook*
