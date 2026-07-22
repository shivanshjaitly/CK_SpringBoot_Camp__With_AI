# CodeKerdos Spring Boot Bootcamp — Weeks 1–5 Study Guide

**Projects completed:** Employee Management System (EMS) · Expense Approval System (EAS)  
**Code folders:** `week-01-spring-core-demo` · `week-01-employee-management` · `week-02-employee-management` · `week-04-expense-approval`

---

## Table of Contents

1. [Week-by-Week Summary](#week-by-week-summary)
2. [Topic-Wise Deep Dive](#topic-wise-deep-dive)
3. [JWT, SecurityConfig & JwtAuthFilter — Interview Deep Dive](#jwt-securityconfig--jwtauthfilter--interview-deep-dive)
4. [@Transactional — Interview Deep Dive](#transactional--interview-deep-dive)
5. [CSRF — Interview Deep Dive](#csrf--interview-deep-dive)
6. [Master Interview Question Bank](#master-interview-question-bank)

---

## Week-by-Week Summary

### Week 1 — Spring Core + First Spring Boot App

| Class | Topics | Project |
|-------|--------|---------|
| **Sat** | IoC, DI, `@Autowired`, `@Qualifier`, `@Primary`, `@Configuration`, `@Bean`, Bean Scopes (Singleton vs Prototype) | `week-01-spring-core-demo` |
| **Sun** | Spring Boot scaffold, `@SpringBootApplication`, JPA entities, repositories, Groq + Spring AI (`ChatClient`, `PromptTemplate`), H2 database | `week-01-employee-management` |

**What you built:** Plain Spring Core demo → first Boot app with Employee/Department entities and AI greeting.

---

### Week 2 — Project #1: Employee Management System (Complete)

| Class | Topics | Project |
|-------|--------|---------|
| **Sat** | Layered architecture (Controller → Service → Repository), DTOs, `@Valid`, `@Transactional`, Spring Security (HTTP Basic), GlobalExceptionHandler, first AI services | `week-02-employee-management` |
| **Sun** | MySQL profiles, pagination (`Pageable`), JPA Specifications, **Natural Language HR Search** (Groq → JSON → Specification → DB), Thymeleaf login page, form login | Same folder |

**What you built:** Full REST API + browser login + AI features. **Project #1 complete.**

**Demo credentials:** `hr@codekerdos.in` / `hr123`

---

### Week 3 — Security Lecture + Q&A

| Class | Topics | Project |
|-------|--------|---------|
| **Sat** | Spring Security filter chain, **CSRF**, API vs browser auth, security headers (`frameOptions`), passwords (`{noop}` vs BCrypt), secrets in `.env` | Theory on EMS |
| **Sun** | Open Q&A, interview lightning round, Week 4 preview | No new features |

**What you learned:** Why security exists, how filters work, when CSRF matters, 401 vs 403.

---

### Week 4 — Project #2: Expense Approval (Part 1)

| Class | Topics | Project |
|-------|--------|---------|
| **Sat** | Domain model (`AppUser`, `Expense`), enums (`Role`, `ExpenseStatus`, `ExpenseCategory`), repositories, DTOs, `ExpenseService` (submit + list mine), **`@Transactional` deep dive** | `week-04-expense-approval` |
| **Sun** | **JWT** (`JwtService`, `JwtAuthFilter`), `SecurityConfig`, BCrypt, `AuthController`, employee endpoints, `GlobalExceptionHandler` | Same folder |

**Demo users:**

| Email | Password | Role |
|-------|----------|------|
| `employee@codekerdos.in` | `emp123` | EMPLOYEE |
| `manager@codekerdos.in` | `mgr123` | MANAGER |
| `admin@codekerdos.in` | `adm123` | ADMIN |

---

### Week 5 — Project #2: Expense Approval (Complete)

| Class | Topics | Project |
|-------|--------|---------|
| **Sat** | `@EnableMethodSecurity`, `@PreAuthorize`, manager approve/reject, business rules (cannot approve own expense), admin list all, date-range summary | Same folder |
| **Sun** | AI auto-categorization, fraud flags, `@Async` background processing, manager AI summary, end-to-end demo | **Project #2 complete** |

---

## Topic-Wise Deep Dive

### 1. Spring Core (Week 1)

| Concept | One-liner |
|---------|-----------|
| **IoC** | Inversion of Control — Spring creates and manages object lifecycle |
| **DI** | Dependencies injected by container, not `new` in every class |
| **Constructor injection** | Preferred — mandatory, testable, immutable fields |
| **`@Configuration` + `@Bean`** | Java-based config; you define how beans are created |
| **`@Autowired` / `@Qualifier` / `@Primary`** | Wire dependencies; resolve multiple implementations |
| **Bean Scope** | Singleton (default, one instance) vs Prototype (new instance per request) |

---

### 2. Spring Boot + Layered Architecture (Week 1–2)

```
HTTP Request
     ↓
SecurityFilterChain  (who are you?)
     ↓
@RestController      (thin — JSON in/out)
     ↓
@Service             (business rules, @Transactional)
     ↓
Repository (JPA)     (no handwritten SQL for CRUD)
     ↓
Database (H2 or MySQL)
```

| Layer | Responsibility |
|-------|----------------|
| **Entity** | DB table mapping (`@Entity`, `@OneToMany`, `@ManyToOne`) |
| **DTO** | API contract — hide entity internals from clients |
| **Repository** | `JpaRepository` — free CRUD + custom query methods |
| **Service** | Business logic, validation, transactions |
| **Controller** | HTTP mapping, `@Valid` on request bodies |

---

### 3. JPA & Database (Week 1–2, Week 4)

| Concept | Explanation |
|---------|-------------|
| **`JpaRepository<T, ID>`** | Spring Data generates SQL from interface methods |
| **`@Enumerated(STRING)`** | Store enum name in DB (readable, not ordinal) |
| **`BigDecimal` for money** | Avoid floating-point rounding errors |
| **`FetchType.LAZY`** | Load related entity only when accessed |
| **H2 vs MySQL** | H2 = in-memory dev; MySQL = persistent production-shaped DB |
| **`Pageable`** | Page number + size + sort for large lists |
| **`JpaSpecificationExecutor`** | Dynamic query builder for flexible filters |
| **N+1 problem** | Loading parent + N children with N extra queries — fix with `JOIN FETCH` or `@EntityGraph` |

---

### 4. Spring AI + Groq (Week 1–2, Week 5)

| Concept | Explanation |
|---------|-------------|
| **Groq** | Hosted LLM API (LLaMA 3.3), OpenAI-compatible, fast free tier |
| **`ChatClient`** | Spring AI bean to call any LLM provider |
| **`PromptTemplate`** | Reusable prompt with `{variable}` placeholders |
| **NL Search pattern** | User text → Groq → JSON criteria → JPA Specification → safe DB query |
| **Why not AI → SQL directly?** | Security + testability — Java controls the query |
| **Structured JSON from LLM** | Parseable output — Java validates before saving |
| **`@Async`** | Run AI analysis in background thread — submit API returns instantly |

**Rule:** AI suggests; Java decides and executes.

---

### 5. Spring Security — Evolution Across Weeks

| Week | Auth mechanism | CSRF |
|------|----------------|------|
| **Week 2 EMS** | HTTP Basic (Postman) + Form login (browser) | Enabled; `/api/**` exempt |
| **Week 4–5 EAS** | JWT Bearer token (stateless) | Disabled entirely |

---

### 6. Expense Approval Domain (Week 4–5)

**Roles:**

| Role | Permissions |
|------|-------------|
| EMPLOYEE | Submit expenses, view own submissions |
| MANAGER | View pending queue, approve, reject, AI summary |
| ADMIN | View all expenses, date-range summary |

**Expense lifecycle:**

```
PENDING → APPROVED
        → REJECTED (with reason)
```

**Key APIs:**

| Method | Path | Role |
|--------|------|------|
| POST | `/api/auth/login` | Public |
| POST | `/api/expenses` | EMPLOYEE |
| GET | `/api/expenses/mine` | EMPLOYEE |
| GET | `/api/expenses/pending` | MANAGER |
| PATCH | `/api/expenses/{id}/approve` | MANAGER |
| PATCH | `/api/expenses/{id}/reject` | MANAGER |
| GET | `/api/expenses` | ADMIN |
| GET | `/api/expenses/summary?from=&to=` | MANAGER / ADMIN |
| GET | `/api/ai/manager-summary` | MANAGER |

---

## JWT, SecurityConfig & JwtAuthFilter — Interview Deep Dive

### The Big Picture — How JWT Auth Works

```
1. LOGIN (once)
   Client → POST /api/auth/login { email, password }
          → AuthService validates password (BCrypt)
          → JwtService.generateToken(email)
          ← { token: "eyJhbG...", expiresIn: 86400000 }

2. EVERY SUBSEQUENT REQUEST
   Client → GET /api/expenses/mine
            Header: Authorization: Bearer eyJhbG...
          → JwtAuthFilter (runs BEFORE controller)
          → Extract email from token
          → Load UserDetails from DB
          → Validate signature + expiry
          → Set SecurityContext (authenticated user + roles)
          → Controller runs
          → @PreAuthorize checks role if present
```

**Stateless:** No `JSESSIONID` cookie, no server-side session table. Token carries identity; server verifies signature.

---

### JwtService — Creates and Validates Tokens

**Location:** `week-04-expense-approval/.../security/JwtService.java`

**Responsibilities:**

| Method | What it does |
|--------|--------------|
| `generateToken(email, extraClaims)` | Build signed JWT with subject=email, issuedAt, expiration, HMAC signature |
| `extractEmail(token)` | Read `subject` claim — who is this token for? |
| `isTokenValid(token, userDetails)` | Email matches + not expired |
| `getSigningKey()` | HMAC-SHA key from `app.jwt.secret` in config |

**Key config (`application.yml`):**

```yaml
app:
  jwt:
    secret: codekerdos-demo-secret-change-in-prod-min-32-chars
    expiration-ms: 86400000   # 24 hours
```

**Interview answers:**

| Question | Answer |
|----------|--------|
| What algorithm signs the JWT? | HMAC-SHA (symmetric key from secret) |
| What is inside a JWT? | Header (alg) + Payload (sub, iat, exp, claims) + Signature |
| Where is the password checked? | **Not in JwtService** — login uses `AuthenticationManager` + BCrypt |
| What if token is tampered? | Signature verification fails → token rejected |
| What if token expired? | `isTokenExpired()` returns true → filter skips auth → 401 |

---

### JwtAuthFilter — Validates Token on Every Request

**Location:** `week-04-expense-approval/.../security/JwtAuthFilter.java`

**Extends:** `OncePerRequestFilter` — runs exactly once per HTTP request.

**Step-by-step flow:**

```
1. Read Authorization header
2. If missing or not "Bearer " → pass through (anonymous request)
3. Extract JWT string (substring after "Bearer ")
4. jwtService.extractEmail(jwt) → userEmail
5. If email found AND SecurityContext empty:
   a. userDetailsService.loadUserByUsername(userEmail)
   b. jwtService.isTokenValid(jwt, userDetails)
   c. Create UsernamePasswordAuthenticationToken
   d. SecurityContextHolder.getContext().setAuthentication(authToken)
6. filterChain.doFilter(request, response) → continue chain
```

**Why check `SecurityContext.getAuthentication() == null`?**  
Avoid re-authenticating if already set (e.g., by another filter).

**Why no exception thrown on bad token?**  
Filter silently skips setting auth → Spring Security sees unauthenticated user → returns 401 on protected routes.

**Interview answers:**

| Question | Answer |
|----------|--------|
| Where is token validated on every request? | `JwtAuthFilter.doFilterInternal()` |
| What does the filter set? | `SecurityContextHolder` with user + authorities (roles) |
| Why `OncePerRequestFilter`? | Prevents double execution on forwarded requests |
| Filter order? | **Before** `UsernamePasswordAuthenticationFilter` |
| Bearer vs Basic? | Bearer = JWT token after login; Basic = base64(user:pass) every call |

---

### SecurityConfig — Wires the Security Chain

**Location:** `week-04-expense-approval/.../config/SecurityConfig.java`

**Annotations:**

| Annotation | Purpose |
|------------|---------|
| `@EnableWebSecurity` | Activate Spring Security |
| `@EnableMethodSecurity` | Enable `@PreAuthorize` on service/controller methods |

**Beans defined:**

| Bean | Purpose |
|------|---------|
| `securityFilterChain` | HTTP rules, CSRF, session policy, filter order |
| `userDetailsService` | Load user from DB by email → Spring `User` with roles |
| `authenticationProvider` | `DaoAuthenticationProvider` + BCrypt password check |
| `authenticationManager` | Used by login endpoint to authenticate credentials |
| `passwordEncoder` | `BCryptPasswordEncoder` — one-way salted hash |

**SecurityFilterChain breakdown:**

```java
http
    .csrf(AbstractHttpConfigurer::disable)           // Stateless JWT API — no browser cookies
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/auth/**", "/h2-console/**").permitAll()
        .anyRequest().authenticated()
    )
    .sessionManagement(session -> session
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // No JSESSIONID
    )
    .authenticationProvider(authenticationProvider())
    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
    .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
```

| Line | Why |
|------|-----|
| `csrf().disable()` | JWT in header, not cookie — CSRF not applicable |
| `permitAll()` on `/api/auth/**` | Login must work without existing token |
| `STATELESS` | No server session — scales horizontally |
| `addFilterBefore(jwtAuthFilter, ...)` | JWT parsed before default auth filter |
| `frameOptions().disable()` | H2 console needs iframe (dev only) |

**Role mapping:**

```java
.roles(user.getRole().name())   // EMPLOYEE → ROLE_EMPLOYEE
```

Spring adds `ROLE_` prefix automatically. `@PreAuthorize("hasRole('MANAGER')")` checks this.

---

### JWT vs Session vs Basic — Comparison Table

| | HTTP Basic (Week 2) | Session Cookie | JWT (Week 4–5) |
|--|---------------------|----------------|----------------|
| Auth per request | Send password (base64) | Cookie auto-sent | Bearer token in header |
| Server state | None | Session store / memory | None (stateless) |
| Scales across servers | Yes | Needs sticky session or shared store | Yes |
| CSRF risk | Low (no cookie) | **High** (cookie auto-sent) | Low (header, not cookie) |
| Token expiry | N/A | Session timeout | `exp` claim in JWT |
| Best for | Dev/Postman | Browser apps | Mobile, SPA, microservices |

---

### @PreAuthorize (Week 5)

```java
@PreAuthorize("hasRole('MANAGER')")
public List<ExpenseResponse> findPending() { ... }

@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
public ExpenseSummaryResponse getSummary(...) { ... }
```

**Two layers of security:**

1. **URL/method level** — `@PreAuthorize` (role gate)
2. **Service level** — business rules (e.g., cannot approve own expense)

Both matter in interviews.

---

## @Transactional — Interview Deep Dive

### What It Does

`@Transactional` tells Spring: **wrap this method in a database transaction.**

```
@Transactional
submit() {
    ┌─── BEGIN TRANSACTION ──────────────────────────┐
    │  1. findByEmail → SELECT                        │
    │  2. new Expense()                               │
    │  3. expenseRepository.save → INSERT             │
    │                                                 │
    │  ✅ Success → COMMIT                            │
    │  ❌ RuntimeException → ROLLBACK                 │
    └─────────────────────────────────────────────────┘
}
```

---

### Four Rules (Memorize These)

**Rule 1 — Writes get `@Transactional`**

```java
@Transactional
public ExpenseResponse submit(...) { ... }   // INSERT / UPDATE / DELETE
```

**Rule 2 — Reads get `@Transactional(readOnly = true)`**

```java
@Transactional(readOnly = true)
public List<ExpenseResponse> findMine(...) { ... }   // SELECT only
```

| `readOnly = true` benefit | Explanation |
|---------------------------|-------------|
| DB optimization | MySQL skips unnecessary writes |
| Hibernate optimization | Skips dirty-check / flush |
| Read replica routing | Connection pools can route to replica |

**Rule 3 — Rollback on unchecked exceptions by default**

```java
@Transactional
public ExpenseResponse submit(...) {
    AppUser user = appUserRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("..."));
    // RuntimeException → automatic ROLLBACK
}
```

Checked exceptions do **not** rollback unless you specify `rollbackFor`.

**Rule 4 — Self-invocation bypasses the proxy**

```java
// WRONG — transaction NOT started
public void doSomething() {
    this.submit(request, email);
}

// CORRECT — call through injected bean
expenseService.submit(request, email);
```

Spring wraps `@Service` in a **proxy**. Internal `this.method()` calls skip the proxy.

---

### Where @Transactional Lives

| Layer | Use @Transactional? |
|-------|---------------------|
| Controller | ❌ No — HTTP concerns only |
| **Service** | ✅ Yes — business operations |
| Repository | ❌ Usually no — Spring Data methods are already transactional |

---

### @Transactional Interview Q&A

| Question | Answer |
|----------|--------|
| What does `@Transactional` do? | Wraps method in DB transaction — commit on success, rollback on exception |
| When is rollback triggered? | Unchecked exceptions (`RuntimeException`) by default |
| Why `readOnly = true`? | SELECT-only — DB/Hibernate optimizations |
| What is self-invocation problem? | Same-class call bypasses Spring proxy — no transaction |
| `@Transactional` on class vs method? | Class-level applies to all public methods; method-level overrides |
| Does it work on private methods? | **No** — proxy only intercepts public methods |
| Propagation `REQUIRED` (default)? | Join existing transaction or create new one |
| Can you use `@Transactional` with `@Async`? | Separate threads = separate transactions — design carefully |

---

## CSRF — Interview Deep Dive

### What Is CSRF?

**Cross-Site Request Forgery** — an attack where a logged-in user's browser is tricked into making unwanted requests to a site they are authenticated with.

**Classic story:**

```
1. HR manager logged into EMS at localhost:8080 (session cookie in browser)
2. Manager opens evil-blog.com in another tab
3. evil-blog.com has hidden form: POST /api/employees/delete/1 → localhost:8080
4. Browser automatically sends session cookie with the request
5. EMS performs the action — manager never clicked our app
```

**Key insight:** CSRF exploits **automatic cookie sending** by the browser.

---

### CSRF Protection Mechanism

| Mechanism | How it works |
|-----------|--------------|
| **CSRF token** | Server embeds secret token in real forms; attacker can't guess it |
| **SameSite cookie** | Browser limits cross-site cookie sends |
| **Stateless API (JWT in header)** | No session cookie → different threat model |

---

### CSRF in Our Projects

**Week 2 EMS (session + browser login):**

```java
.csrf(csrf -> csrf
    .ignoringRequestMatchers("/api/**", "/h2-console/**")
)
```

| Setting | Meaning |
|---------|---------|
| CSRF enabled by default | Protects browser form POSTs |
| `/api/**` ignored | Postman sends Basic Auth header — no auto cookie |
| Browser `/login` form | Still CSRF-protected when enabled for those routes |

**Week 4–5 EAS (JWT stateless API):**

```java
.csrf(AbstractHttpConfigurer::disable)
```

| Why disabled? | Explanation |
|---------------|-------------|
| Auth via Bearer header | Client explicitly sends token — not auto-sent by browser |
| No session cookie | Nothing for attacker to hijack via cross-site form |
| Common pattern | Standard for pure REST + JWT APIs |

**Interview line:** *"CSRF targets browser sessions with cookies. Stateless JWT APIs authenticate via Authorization header — CSRF is typically disabled. For cookie-based browser apps, keep CSRF enabled on forms."*

---

### When CSRF Matters vs Doesn't

| Client | Auth mechanism | CSRF relevant? |
|--------|----------------|----------------|
| Postman `/api/**` | `Authorization: Basic` or `Bearer` | **No** |
| Browser form login | Session cookie (`JSESSIONID`) | **Yes** |
| Mobile app + JWT | Bearer token header | **No** |
| React SPA + JWT | Bearer in axios/fetch | **No** |
| React SPA + cookie | HttpOnly cookie | **Yes** — use CSRF token or SameSite |

---

### CSRF Interview Q&A

| Question | Answer |
|----------|--------|
| Explain CSRF in one sentence | Malicious site tricks your logged-in browser into sending authenticated requests using your cookies |
| Can you disable CSRF completely? | Yes for pure JWT/stateless APIs; risky for cookie-based browser apps |
| 401 vs 403? | 401 = not authenticated; 403 = authenticated but not authorized |
| Why ignore `/api/**` in EMS? | REST clients send auth header manually — no automatic cookie |
| Is ignoring CSRF bad? | Match protection to how clients authenticate |
| XSS vs CSRF? | XSS steals credentials/scripts; CSRF abuses existing session |

---

## Master Interview Question Bank

### Week 1 — Spring Core & Boot

| # | Question | Answer |
|---|----------|--------|
| 1 | What is IoC? | Inversion of Control — Spring manages object lifecycle |
| 2 | What is DI? | Dependencies provided by container, not `new` |
| 3 | Constructor vs field injection? | Constructor — mandatory, testable, immutable |
| 4 | What is `@SpringBootApplication`? | `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan` |
| 5 | What is JPA? | Java Persistence API — ORM mapping objects to DB tables |
| 6 | What is `JpaRepository`? | Spring Data interface — free CRUD + query methods |
| 7 | What is Groq? | Free LLM API provider, OpenAI-compatible, ultra-fast |
| 8 | What is Spring AI ChatClient? | Unified interface to call any LLM from Spring |
| 9 | What is PromptTemplate? | Reusable prompt with `{variable}` placeholders |
| 10 | Why env vars for API keys? | Security — never commit secrets to Git |

---

### Week 2 — EMS & REST

| # | Question | Answer |
|---|----------|--------|
| 11 | Why DTOs? | Decouple API contract from database schema |
| 12 | Why `@Transactional`? | Atomic DB operations — all succeed or all roll back |
| 13 | H2 vs MySQL? | H2 = in-memory dev; MySQL = persistent production DB |
| 14 | What is Pageable? | Spring abstraction for page/size/sort |
| 15 | What is JpaSpecificationExecutor? | Dynamic query builder for flexible filters |
| 16 | How does NL search work? | LLM → JSON criteria → JPA Specification → DB |
| 17 | Why not let AI query DB directly? | Security + testability — Java controls the query |
| 18 | HTTP Basic vs form login? | Basic = API/Postman; form = browser session |
| 19 | What is Thymeleaf? | Server-side HTML template engine for Spring |
| 20 | `@RestController` vs `@Controller`? | REST returns JSON; Controller returns view name |

---

### Week 3 — Security

| # | Question | Answer |
|---|----------|--------|
| 21 | Explain CSRF in one sentence | Evil site uses your browser cookie to perform actions as you |
| 22 | JWT vs session — one difference | JWT is stateless signed token; session stored server-side |
| 23 | What is the Spring Security filter chain? | Series of filters (CSRF, auth, authorization) before controller |
| 24 | What file configures security? | `SecurityConfig.java` → `SecurityFilterChain` bean |
| 25 | Is HTTP Basic safe? | OK for dev over HTTPS; production prefers JWT/OAuth2 |
| 26 | Why `{noop}` password? | Demo only — plain text encoding for teaching |
| 27 | What is BCrypt? | One-way salted password hash — use in production |
| 28 | SQL injection — are we safe? | JPA parameterized queries help; never concat SQL |
| 29 | Should H2 console be public? | Never in production — dev convenience only |
| 30 | What is `@PreAuthorize`? | Method-level role/permission check before method runs |

---

### Week 4 — Expense Domain + JWT

| # | Question | Answer |
|---|----------|--------|
| 31 | What is `@Enumerated(STRING)`? | Stores enum name in DB — readable |
| 32 | Why BigDecimal for money? | Avoid floating-point rounding errors |
| 33 | Initial expense status? | PENDING — awaiting manager approval |
| 34 | Three roles in EAS? | EMPLOYEE submit · MANAGER approve · ADMIN oversee |
| 35 | JWT vs session? | JWT = stateless signed token, no server session |
| 36 | Why BCrypt? | One-way salted password hash |
| 37 | What is Bearer token? | `Authorization: Bearer <jwt>` |
| 38 | What does JwtAuthFilter do? | Validates token, sets SecurityContext |
| 39 | Where is token created? | `JwtService.generateToken()` from AuthController |
| 40 | Why is `/api/auth/login` public? | `permitAll()` — need login without existing token |
| 41 | What happens if token expired? | Filter skips auth → 401 on protected routes |
| 42 | What hashing algorithm for passwords? | BCrypt — never reversible |

---

### Week 5 — Approval Workflow + AI

| # | Question | Answer |
|---|----------|--------|
| 43 | What is `@PreAuthorize`? | Method-level role check before execution |
| 44 | Why service-layer rules beyond roles? | Business logic (e.g., can't approve own expense) |
| 45 | What is `@Async`? | Background thread — non-blocking AI processing |
| 46 | Why structured JSON from LLM? | Parseable, testable — Java controls DB updates |
| 47 | Why not let AI approve expenses? | Human accountability for financial decisions |
| 48 | `@Async` and `@Transactional` together? | Different threads = separate transactions |
| 49 | Self-invocation with `@Async`? | Same proxy issue as `@Transactional` — inject and call externally |
| 50 | N+1 problem? | 1 query for list + N queries for each related entity — fix with fetch join |

---

### Architecture & Design (Cross-Week)

| # | Question | Answer |
|---|----------|--------|
| 51 | Why layered architecture? | Separation of concerns — Controller/Service/Repository |
| 52 | Where should business logic live? | Service layer — not controller, not repository |
| 53 | Why never expose entities in API? | Hide DB schema, control fields, avoid lazy-loading issues |
| 54 | How does Spring Data know `findByTeam`? | Query derivation from method name |
| 55 | What is GlobalExceptionHandler? | `@RestControllerAdvice` — consistent error JSON |
| 56 | What is `@Valid`? | Triggers Jakarta Bean Validation on request DTO |
| 57 | 401 vs 403? | 401 = not logged in; 403 = logged in but forbidden |
| 58 | Why `.env` for secrets? | Keep keys out of Git; load via launch config |
| 59 | What is stateless session policy? | No JSESSIONID — every request self-contained |
| 60 | How many projects after Week 5? | **2 complete** (EMS + Expense). Booking = Week 6–7 |

---

## Quick Reference Diagrams

### Full Request Flow (Expense App)

```
Client
  │
  ├─ POST /api/auth/login ──► AuthController ──► AuthService
  │                                              └─► BCrypt check
  │                                              └─► JwtService.generateToken()
  │◄── { token } ─────────────────────────────────
  │
  ├─ POST /api/expenses ──► JwtAuthFilter ──► ExpenseController ──► ExpenseService
  │   Bearer token              │                                      └─► @Transactional
  │                             └─► SecurityContext set                └─► @Async AI
  │◄── 201 ExpenseResponse ────────────────────────────────────────────
  │
  └─ PATCH /api/expenses/1/approve ──► @PreAuthorize(MANAGER) ──► approve()
                                                                      └─► assertNotOwnExpense()
```

### Security Filter Chain Order

```
HTTP Request
     ↓
SecurityFilterChain
  ├── CsrfFilter          (disabled for JWT API)
  ├── JwtAuthFilter       ← OUR custom filter
  ├── UsernamePasswordAuthenticationFilter
  ├── AuthorizationFilter (authenticated? roles?)
  └── ...
     ↓
@RestController
```

---

## Projects Checklist — What You Can Demo

### Project #1 — EMS (`week-02-employee-management`)

- [ ] CRUD employees and departments via Postman (Basic Auth)
- [ ] Browser login at `/login`
- [ ] Pagination on employee list
- [ ] Natural language search: *"senior backend engineers joined after 2022"*
- [ ] AI greeting / onboarding endpoints
- [ ] Switch H2 ↔ MySQL profiles

### Project #2 — Expense Approval (`week-04-expense-approval`)

- [ ] Login as employee/manager/admin → get JWT
- [ ] Employee submits expense (status PENDING)
- [ ] Manager views pending queue
- [ ] Manager approves/rejects (cannot approve own)
- [ ] Admin views all + date-range summary
- [ ] AI categorization runs in background (`@Async`)
- [ ] Manager AI summary of pending queue

---

*CodeKerdos.in · Spring Boot + AI Bootcamp · Weeks 1–5 Study Guide*
