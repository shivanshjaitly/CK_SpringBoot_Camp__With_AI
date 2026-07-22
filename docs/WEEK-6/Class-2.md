# Week 6 · Class 2 — Sunday · Idempotency + AOP + Testing + Docker

> **[← Week 6 Index](README.md)** · **Previous → [Class 1](Class-1.md)** · **Next → [Week 7](../WEEK-7/README.md)**  
> **Coding folder:** `week-06-booking-service`

---

## CLASS 2 — TOPICS

| # | Topic | Code? |
|---|-------|-------|
| 1 | Class 1 recap + demo | Run |
| 2 | Idempotency-Key on POST /bookings | Code |
| 3 | State machine — legal booking transitions | Code |
| 4 | Spring AOP audit — `@Around` service calls | Code |
| 5 | N+1 — spot with SQL log · fix with `@EntityGraph` | Code |
| 6 | Unit tests — Mockito `@Mock` / `@InjectMocks` | Code |
| 7 | MockMvc + `@DataJpaTest` + `@SpringBootTest` | Code |
| 8 | Actuator + Docker Compose (Postgres) | Code |
| 9 | Wrap + Week 7 preview | Talk |

**Session goal:** Production hardening complete. Microservices / Kafka / RAG = **Week 7**.

---

**Time split:**

| Block | Duration | Topics |
|-------|----------|--------|
| Recap + idempotency + state | 40 min | 1–3 |
| AOP + N+1 | 25 min | 4–5 |
| Testing | 35 min | 6–7 |
| Docker + wrap | 20 min | 8–9 |

---

# TOPIC 1 — Recap

### RUN

Login as customer → `GET /api/slots` → `POST /api/bookings` → `GET /api/bookings/mine`.

### END THOUGHT

> "Happy path works. Today we make retries and ops safe."

---

# TOPIC 2 — Idempotency

### SAY

> "Networks retry. Without idempotency, two identical POSTs create **two bookings**.
> Client sends `Idempotency-Key`. We store key → response. Same key = same booking."

### DRAW

```
POST /bookings + Idempotency-Key: abc
  ├─ key unknown → create booking, save key, return 201
  └─ key known   → return stored booking 200 (no second seat)
```

### YOU DO

1. Require header on create (or accept optional in demo — prefer **required** for CUSTOMER book).
2. In `BookingService.create`:

```java
@Transactional
public BookingResponse create(CreateBookingRequest request, String email, String idempotencyKey) {
    if (idempotencyKey == null || idempotencyKey.isBlank()) {
        throw new BusinessException("Idempotency-Key header is required");
    }
    return bookingRepository.findByIdempotencyKey(idempotencyKey)
            .map(BookingResponse::from)
            .orElseGet(() -> createNew(request, email, idempotencyKey));
}
```

3. Controller:

```java
@PostMapping
public ResponseEntity<BookingResponse> create(
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @Valid @RequestBody CreateBookingRequest request,
        Authentication auth) {
    BookingResponse body = bookingService.create(request, auth.getName(), idempotencyKey);
    // 200 if replay, 201 if new — optional: track via service flag
    return ResponseEntity.status(HttpStatus.CREATED).body(body);
}
```

### RUN

Same body + same `Idempotency-Key` twice → one row in DB.  
Different key → second booking (until capacity).

### Interview line

> "Idempotency key is stored uniquely; retries are safe."

### END THOUGHT

> "Retries fixed. Topic 3 — state transitions."

---

# TOPIC 3 — State Machine

### SAY

> "Not every status jump is legal. CANCELLED cannot become CONFIRMED."

### YOU DO

```java
public final class BookingStateMachine {
    private BookingStateMachine() {}

    public static void assertTransition(BookingStatus from, BookingStatus to) {
        boolean ok = (from == BookingStatus.PENDING && to == BookingStatus.CONFIRMED)
                || (from == BookingStatus.PENDING && to == BookingStatus.CANCELLED)
                || (from == BookingStatus.CONFIRMED && to == BookingStatus.CANCELLED);
        if (!ok) {
            throw new BusinessException("Illegal transition: " + from + " → " + to);
        }
    }
}
```

Call before status changes. **Strategy** for slot-type rules can plug in here later (doctor vs room).

### END THOUGHT

> "State pattern in 15 lines. Topic 4 — AOP."

---

# TOPIC 4 — Spring AOP Audit

### SAY

> "Cross-cutting = don't sprinkle `log.info` in every method.
> `@Aspect` + `@Around` logs every public service call — interview classic."

### YOU DO

```java
@Aspect
@Component
@Slf4j
public class ServiceAuditAspect {

    @Pointcut("within(in.codekerdos.booking.service..*)")
    public void serviceLayer() {}

    @Around("serviceLayer()")
    public Object audit(ProceedingJoinPoint pjp) throws Throwable {
        String name = pjp.getSignature().toShortString();
        long start = System.currentTimeMillis();
        log.info("AUDIT start {}", name);
        try {
            Object result = pjp.proceed();
            log.info("AUDIT ok {} ({} ms)", name, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable t) {
            log.warn("AUDIT fail {} ({} ms): {}", name, System.currentTimeMillis() - start, t.getMessage());
            throw t;
        }
    }
}
```

Ensure `spring-boot-starter-aop` on classpath. Mention `@Before` / `@After` / `@AfterReturning` / `@AfterThrowing` as siblings.

### RUN

Hit create booking → console shows AUDIT lines.

### END THOUGHT

> "Audit without polluting business code. Topic 5 — N+1."

---

# TOPIC 5 — N+1 + EntityGraph

### SAY

> "List bookings → for each booking lazy-load slot → **N+1 queries**.
> Turn on `show-sql`. Fix with `@EntityGraph` or `JOIN FETCH`."

### YOU DO

```java
@EntityGraph(attributePaths = {"slot", "customer"})
@Query("select b from Booking b where b.customer.email = :email")
List<Booking> findMineWithDetails(@Param("email") String email);
```

### RUN

Before: 1 + N SQL. After: one (or few) joins.

### END THOUGHT

> "You can *see* the bug in logs — that's the teaching win. Topic 6 — unit tests."

---

# TOPIC 6 — Mockito Unit Tests

### YOU DO

```java
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock BookingRepository bookingRepository;
    @Mock SlotRepository slotRepository;
    @Mock AppUserRepository userRepository;
    @InjectMocks BookingService bookingService;

    @Test
    void create_whenSlotFull_throws() {
        // stub slot FULL / capacity → assert BusinessException
    }

    @Test
    void create_whenIdempotencyExists_returnsExisting() {
        // stub findByIdempotencyKey → present → never save new
    }
}
```

### SAY

> "`@Mock` = fake collaborator. `@InjectMocks` = real class under test."

### END THOUGHT

> "Fast tests, no Spring context. Topic 7 — slice + full tests."

---

# TOPIC 7 — MockMvc · DataJpaTest · SpringBootTest

### YOU DO (one of each live)

| Annotation | Tests what |
|------------|------------|
| `@WebMvcTest(BookingController.class)` + MockMvc | HTTP + validation + security wiring |
| `@DataJpaTest` | Repository queries + constraints |
| `@SpringBootTest` + `@AutoConfigureMockMvc` | Full stack smoke |

Demo: MockMvc POST booking without token → 401.  
With customer token + valid body → 201.

### END THOUGHT

> "Pyramid: many unit, some slice, few full. Topic 8 — Docker."

---

# TOPIC 8 — Actuator + Docker Compose

### SAY

> "Actuator = ops endpoints. Docker Compose = Postgres (+ Kafka next week)."

### YOU DO

`application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

`docker-compose.yml` (Week 6):

```yaml
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: booking_db
      POSTGRES_USER: booking
      POSTGRES_PASSWORD: booking
    ports:
      - "5432:5432"
  # kafka added Week 7
```

Profile `postgres` datasource → run with Compose up.

### RUN

`GET /actuator/health` → UP.

### END THOUGHT

> "Local prod-shaped runtime. Topic 9 — wrap."

---

# TOPIC 9 — Wrap + Week 7 Preview

### WEEK 6 — Deliverables

| # | Deliverable |
|---|-------------|
| 1 | Slot + Booking REST + JWT |
| 2 | Idempotency-Key |
| 3 | State transitions |
| 4 | AOP audit |
| 5 | N+1 fixed |
| 6 | Unit + MockMvc + DataJpaTest |
| 7 | Actuator + Postgres Compose |

### SAY — Week 7 preview

> "Next Saturday: **microservices** — notification-service, **Kafka** topic `booking.events`, **saga** choreography + cancel compensation.
> Sunday: **embeddings + SimpleVectorStore + RAG** — search slots by meaning. Project #3 complete."

### Homework

1. Double-submit same Idempotency-Key — prove one booking  
2. Show SQL log before/after EntityGraph  
3. Run `./mvnw test` green  

---

## Interview Quick Reference (Class 2)

| Question | Answer |
|----------|--------|
| Idempotency? | Client key; server stores; retries return same result |
| `@Around` vs `@Before`? | Around wraps proceed(); before only pre |
| N+1? | Lazy load in loop; fix JOIN FETCH / EntityGraph |
| `@DataJpaTest`? | Slice — JPA only, fast |
| Why Actuator? | Health for orchestrators / k8s |

---

*CodeKerdos.in · Week 6 Class 2 · Hardening complete*
