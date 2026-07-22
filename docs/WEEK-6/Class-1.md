# Week 6 · Class 1 — Saturday · Booking Domain + Production REST

> **[← Week 6 Index](README.md)** · **Next → [Class 2 — Idempotency + AOP + Tests](Class-2.md)**  
> **Coding folder:** `week-06-booking-service` (NEW — Project #3 starts here)

---

## HOW TO USE THIS FILE (read once)

You teach **Topic 1 → 10 in order**.  
For each topic: **SAY** → **DRAW** (optional) → **YOU DO** → **CODE** → **RUN** → **STUCK?**

**Students follow you in THEIR IDE.** Build live — do not open a private solutions repo.

---

## CLASS 1 — TOPICS

| # | Topic | Files? |
|---|-------|--------|
| 1 | Project #2 recap + Project #3 intro | Talk |
| 2 | Why Booking beats Expense on resume weight | Talk / board |
| 3 | Scaffold multi-module-ready project | ✅ |
| 4 | Enums — Role, ResourceType, SlotStatus, BookingStatus | ✅ |
| 5 | Entities — AppUser, Slot, Booking | ✅ |
| 6 | Repositories | ✅ |
| 7 | DTOs + Bean Validation | ✅ |
| 8 | JWT security + Auth (login / register) | ✅ |
| 9 | SlotService + BookingService + Controllers | ✅ |
| 10 | GlobalExceptionHandler + OpenAPI + wrap | ✅ |

**Sunday (Class 2):** Idempotency · AOP audit · N+1 · Testing · Docker  
**Week 7:** Microservices · Kafka · Saga · RAG

---

## FILES YOU WILL CREATE (Class 1 end state)

```
week-06-booking-service/
├── pom.xml
├── .env.example
├── docker-compose.yml          ← stub; used heavily Sunday / Week 7
└── src/main/
    ├── resources/application.yml
    └── java/in/codekerdos/booking/
        ├── BookingServiceApplication.java
        ├── enums/
        ├── entity/
        ├── repository/
        ├── dto/
        ├── security/           JwtService, JwtAuthFilter
        ├── config/             SecurityConfig, OpenApiConfig, DataLoader
        ├── service/
        ├── controller/
        ├── exception/          GlobalExceptionHandler, ApiError, ...
        └── aspect/             (Sunday)
```

---

**Time split:**

| Block | Duration | Topics |
|-------|----------|--------|
| Recap + why this project | 15 min | 1–2 |
| Scaffold + domain | 35 min | 3–6 |
| Security + APIs | 55 min | 7–9 |
| Errors + Swagger + wrap | 15 min | 10 |

---

## HOW TO RUN — VS Code / Cursor (before class)

| Step | Action |
|------|--------|
| 1 | Open repo root |
| 2 | `cp week-06-booking-service/.env.example week-06-booking-service/.env` |
| 3 | Set `GROQ_API_KEY` + `JWT_SECRET` (same habit as Expense) |
| 4 | **F5** → **Week 6 Booking — Run (H2)** |
| 5 | Wait: `Tomcat started on port 8081` |
| 6 | Swagger: http://localhost:8081/swagger-ui.html |

### Demo users (seeded)

| Email | Password | Role |
|-------|----------|------|
| `customer@codekerdos.in` | `cust123` | CUSTOMER |
| `provider@codekerdos.in` | `prov123` | PROVIDER |
| `admin@codekerdos.in` | `adm123` | ADMIN |

---

# TOPIC 1 — Project #2 Recap + Project #3 Intro

### SAY

> "Project #2 = **Expense** — one app, JWT, roles, `@Async` AI.
> Project #3 = **Booking** — same Spring skills, then we add **idempotency, AOP, microservices, Kafka, saga, RAG**.
> This is the **flagship** for LinkedIn and interviews."

### Quick fire

| Question | Answer |
|----------|--------|
| Expense security? | JWT + `@PreAuthorize` |
| Expense AI? | Categorize + fraud + manager summary |
| Today's focus? | Domain + JWT + Slot/Booking REST |
| Port? | **8081** (Expense stays on 8080) |

### DRAW

```
week-04-expense-approval     →  Project #2 ✅
week-06-booking-service      →  Project #3 (today → Week 7)
```

### END THOUGHT

> "Next: why Booking is higher weightage than Expense."

---

# TOPIC 2 — Resume Weightage (Board — 8 min)

### SAY

> "Expense proves you can ship a product API.
> Booking proves you understand **systems**: retries, events, compensation, semantic search."

### Contents

| Feature | Expense | Booking |
|---------|---------|---------|
| JWT + roles | ✅ | ✅ |
| Idempotency | ❌ | ✅ Sunday |
| AOP audit | ❌ | ✅ Sunday |
| Microservices + Kafka | ❌ | ✅ Week 7 |
| Saga | ❌ | ✅ Week 7 |
| RAG / embeddings | ❌ | ✅ Week 7 |

### END THOUGHT

> "We earn that weightage by finishing demos — not by buzzword slides. Topic 3 — scaffold."

---

# TOPIC 3 — Scaffold Project

### SAY

> "New folder. Port **8081**. Same Boot 3.4 + JWT stack as Expense.
> Week 6 = **one deployable**. Week 7 we **split** notification + AI into services — packages today map to services later."

### YOU DO

Create `week-06-booking-service/` with `pom.xml`, `application.yml`, `.env.example`, main class.

Key `pom.xml` extras vs Expense:

- `spring-boot-starter-aop` (Sunday)
- `springdoc-openapi-starter-webmvc-ui` (Swagger today)
- `spring-boot-starter-actuator`
- Kafka / AI starters land Week 7 — don't add yet unless scaffolding ahead

### RUN

App starts on **8081** with H2.

### END THOUGHT

> "Scaffold up. Topic 4 — enums."

---

# TOPIC 4 — Enums

### YOU DO

**`enums/Role.java`**

```java
package in.codekerdos.booking.enums;

public enum Role {
    CUSTOMER,
    PROVIDER,
    ADMIN
}
```

**`enums/ResourceType.java`**

```java
package in.codekerdos.booking.enums;

public enum ResourceType {
    MEETING_ROOM,
    DOCTOR,
    CONSULTANT,
    INTERVIEW
}
```

**`enums/SlotStatus.java`**

```java
package in.codekerdos.booking.enums;

public enum SlotStatus {
    OPEN,
    FULL,
    CANCELLED
}
```

**`enums/BookingStatus.java`**

```java
package in.codekerdos.booking.enums;

public enum BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED
}
```

### SAY

> "BookingStatus is our **State pattern** vocabulary. Sunday we enforce legal transitions.
> Week 7 saga listens for CONFIRMED / CANCELLED events."

### END THOUGHT

> "Enums done. Topic 5 — entities."

---

# TOPIC 5 — Entities

### YOU DO

**`entity/AppUser.java`** — same idea as Expense: id, email unique, BCrypt password, fullName, Role.

**`entity/Slot.java`**

```java
package in.codekerdos.booking.entity;

import in.codekerdos.booking.enums.ResourceType;
import in.codekerdos.booking.enums.SlotStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "slots")
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceType resourceType;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    private String location;

    @Column(nullable = false)
    private int capacity = 1;

    @Column(nullable = false)
    private int bookedCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status = SlotStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private AppUser provider;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // getters / setters
}
```

**`entity/Booking.java`**

```java
package in.codekerdos.booking.entity;

import in.codekerdos.booking.enums.BookingStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_idempotency_key", columnList = "idempotencyKey", unique = true)
})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private Slot slot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private AppUser customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    /** Populated Sunday — unique per successful create */
    @Column(unique = true, length = 64)
    private String idempotencyKey;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    // getters / setters
}
```

### DRAW

```
PROVIDER 1──* Slot 1──* Booking *──1 CUSTOMER
```

### END THOUGHT

> "LAZY on associations — Sunday we fix N+1 with EntityGraph. Topic 6 — repos."

---

# TOPIC 6 — Repositories

### YOU DO

```java
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
}

public interface SlotRepository extends JpaRepository<Slot, Long> {
    List<Slot> findByStatus(SlotStatus status);
    List<Slot> findByResourceTypeAndStatus(ResourceType type, SlotStatus status);
}

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomerEmail(String email);
    Optional<Booking> findByIdempotencyKey(String key);
}
```

> Note: `findByCustomerEmail` needs `customer.email` path — Spring Data: `findByCustomer_Email`.

### END THOUGHT

> "Repos ready. Topic 7 — DTOs."

---

# TOPIC 7 — DTOs + Bean Validation

### SAY

> "Same rule as Expense: **never** expose entities. `@Valid` + `@NotNull` / `@Size` / `@Email`."

### YOU DO (minimum set)

| DTO | Purpose |
|-----|---------|
| `LoginRequest` / `RegisterRequest` / `AuthResponse` | Auth |
| `CreateSlotRequest` / `SlotResponse` | Provider slots |
| `CreateBookingRequest` / `BookingResponse` | Customer bookings |

**`CreateBookingRequest`**

```java
public record CreateBookingRequest(
        @NotNull Long slotId,
        @Size(max = 500) String notes
) {}
```

**`CreateSlotRequest`** — `@NotBlank title`, `@NotNull resourceType`, start/end, `@Min(1) capacity`.

### END THOUGHT

> "Contracts locked. Topic 8 — JWT."

---

# TOPIC 8 — JWT Security + Auth

### SAY

> "Copy the **pattern** from Expense — not the package names.
> `JwtService` · `JwtAuthFilter` · `SecurityConfig` · `AuthController` · BCrypt · `DataLoader`."

### YOU DO

**Security rules (Class 1):**

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**", "/actuator/health",
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
    .requestMatchers("/api/slots").authenticated()
    .anyRequest().authenticated()
)
```

Method security:

- `@PreAuthorize("hasRole('PROVIDER')")` on create slot
- `@PreAuthorize("hasRole('CUSTOMER')")` on book / mine / cancel
- `@PreAuthorize("hasRole('ADMIN')")` on list all bookings

**AuthController:** `POST /api/auth/login`, `POST /api/auth/register` (CUSTOMER only via public register).

### RUN

```http
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{ "email": "customer@codekerdos.in", "password": "cust123" }
```

Expected: `{ "token": "eyJ...", "role": "CUSTOMER", ... }`

### END THOUGHT

> "Auth works. Topic 9 — booking APIs."

---

# TOPIC 9 — SlotService + BookingService + Controllers

### SAY

> "Booking create today: load slot → check OPEN + capacity → save Booking CONFIRMED → bump `bookedCount` → if full set FULL.
> Use `@Transactional`. Sunday we add idempotency + stricter state machine."

### YOU DO — BookingService.create (core logic)

```java
@Transactional
public BookingResponse create(CreateBookingRequest request, String customerEmail) {
    AppUser customer = userRepository.findByEmail(customerEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    Slot slot = slotRepository.findById(request.slotId())
            .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

    if (slot.getStatus() != SlotStatus.OPEN) {
        throw new BusinessException("Slot is not open for booking");
    }
    if (slot.getBookedCount() >= slot.getCapacity()) {
        throw new BusinessException("Slot is full");
    }

    Booking booking = new Booking();
    booking.setSlot(slot);
    booking.setCustomer(customer);
    booking.setStatus(BookingStatus.CONFIRMED);
    booking.setNotes(request.notes());
    bookingRepository.save(booking);

    slot.setBookedCount(slot.getBookedCount() + 1);
    if (slot.getBookedCount() >= slot.getCapacity()) {
        slot.setStatus(SlotStatus.FULL);
    }
    return BookingResponse.from(booking);
}
```

**Cancel:** only owner · status → CANCELLED · decrement bookedCount · reopen slot if was FULL.

**Controllers:** `SlotController`, `BookingController` with `@Operation` / `@Tag` for Swagger.

### RUN

```http
POST http://localhost:8081/api/bookings
Authorization: Bearer <customer_token>
Content-Type: application/json

{ "slotId": 1, "notes": "Need projector" }
```

Expected: `201` + CONFIRMED booking.

### END THOUGHT

> "Happy path works. Topic 10 — errors + OpenAPI."

---

# TOPIC 10 — GlobalExceptionHandler + OpenAPI + Wrap

### SAY

> "Production APIs return a **stable error body**: timestamp, status, message, path.
> Swagger documents the contract for Postman and interviews."

### YOU DO

**`exception/ApiError.java`**

```java
public record ApiError(
        Instant timestamp,
        int status,
        String message,
        String path
) {}
```

**`@ControllerAdvice`** handlers for:

| Exception | Status |
|-----------|--------|
| `MethodArgumentNotValidException` | 400 |
| `ResourceNotFoundException` | 404 |
| `BusinessException` | 409 (or 400) |
| `AccessDeniedException` | 403 |

**`OpenApiConfig`** — title *CodeKerdos Booking Service*, Bearer JWT scheme.

### RUN

Open http://localhost:8081/swagger-ui.html — Authorize with Bearer token — try Create Booking.

---

## CLASS 1 — Deliverables

| # | Done when |
|---|-----------|
| 1 | Project runs on 8081 |
| 2 | Login + register work |
| 3 | Provider can create slots |
| 4 | Customer can book + list mine + cancel |
| 5 | Validation + ApiError shape |
| 6 | Swagger UI live |

### SAY — Sunday preview

> "Tomorrow: **Idempotency-Key**, AOP audit log every service call, N+1 + EntityGraph, Mockito / MockMvc / DataJpaTest, Docker Compose + Postgres."

### Homework

1. Book the same slot twice until FULL — prove capacity logic  
2. Cancel and re-book — prove capacity releases  
3. Screenshot Swagger Authorize flow  

---

## Interview Quick Reference (Class 1)

| Question | Answer |
|----------|--------|
| Why DTOs? | Stable API; hide JPA / Lazy |
| Why CONFIRMED immediately today? | Happy path; Week 7 can insert PENDING + async confirm |
| Unique idempotency column? | Prepared for Sunday retries |
| Why port 8081? | Run Expense + Booking side by side |

---

*CodeKerdos.in · Week 6 Class 1 · Project #3 start*
