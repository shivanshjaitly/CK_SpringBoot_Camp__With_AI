# Week 4 · Class 2 — Sunday · JWT Auth + Employee Submit Flow

> **[← Week 4 Index](README.md)** · **Previous ← [Class 1 — Domain](Class-1.md)**  
> **Coding folder:** `week-04-expense-approval`  
> **Next week → [Week 5 — Approval + AI](../WEEK-5/README.md)**

---

## CLASS 2 — TOPICS

| # | Topic | Code? |
|---|-------|-------|
| 1 | Saturday recap + JWT architecture (vs Basic Auth) | Talk + draw |
| 2 | JwtService + JwtAuthFilter + SecurityConfig | ✅ **security/** + **config/** |
| 3 | AuthController + DataLoader (seed users) | ✅ **controller/** + **service/** |
| 4 | ExpenseController — employee endpoints | ✅ **controller/** |
| 5 | GlobalExceptionHandler | ✅ **exception/** |
| 6 | Full demo + homework | Run |

**Session goal:** Login returns JWT. Employee can submit expense and list own items. **Manager + AI = Week 5.**

**Time split:**

| Block | Duration | Topics |
|-------|----------|--------|
| Recap + JWT theory | 20 min | 1 |
| JWT implementation | 40 min | 2–3 |
| Employee APIs + errors | 25 min | 4–5 |
| Demo + wrap | 10 min | 6 |

---

## HOW TO RUN — VS Code / Cursor

| Step | Action |
|------|--------|
| 1 | `cp week-04-expense-approval/.env.example week-04-expense-approval/.env` |
| 2 | Add `GROQ_API_KEY` + `JWT_SECRET=codekerdos-demo-secret-change-in-prod-min-32-chars` |
| 3 | **F5** → **Week 4 EAS — Run with Groq (H2)** |
| 4 | `POST /api/auth/login` → copy token → `Authorization: Bearer <token>` |

### Demo users (seeded on startup)

| Email | Password | Role |
|-------|----------|------|
| `employee@codekerdos.in` | `emp123` | EMPLOYEE |
| `manager@codekerdos.in` | `mgr123` | MANAGER |
| `admin@codekerdos.in` | `adm123` | ADMIN |

---

# TOPIC 1 — Saturday Recap + JWT Architecture

### Quick fire questions

| Question | Expected answer |
|----------|-----------------|
| What entities did we create? | AppUser, Expense |
| Initial expense status? | PENDING |
| Three enums? | Role, ExpenseStatus, ExpenseCategory |
| What's today? | JWT security + employee REST endpoints |

### SAY

> "Week 2 EMS used **HTTP Basic**. Production apps use **JWT** — login once, Bearer token on every call.
> **Stateless** — no server session table."

### Contents

| | HTTP Basic (Week 2) | JWT (Week 4) |
|--|---------------------|--------------|
| Login | Password every request | Login once → token |
| Header | `Authorization: Basic ...` | `Authorization: Bearer eyJ...` |
| Passwords | `{noop}` demo | **BCrypt** hash |

### DRAW

```
POST /api/auth/login → AuthService → JwtService.sign → { token }
GET /api/expenses/mine + Bearer token → JwtAuthFilter → Controller
```

### END THOUGHT

> "Topic 2 — build JwtService, filter, SecurityConfig."

---

# TOPIC 2 — JwtService + JwtAuthFilter + SecurityConfig

### YOU DO

Add DTOs if not done: **`LoginRequest.java`**, **`AuthResponse.java`** — see [Class-1.md](Class-1.md) Topic 7 pattern.

**`security/JwtService.java`**, **`security/JwtAuthFilter.java`**, **`config/SecurityConfig.java`** — full code in previous bootcamp reference; key points:

- `SessionCreationPolicy.STATELESS`
- `csrf.disable()` for JWT API (discuss Week 3 CSRF lecture)
- `BCryptPasswordEncoder` bean
- Filter before `UsernamePasswordAuthenticationFilter`

> Say: *"BCrypt replaces Week 2 `{noop}` — real password hashing."*

### STUCK?

| Problem | Fix |
|---------|-----|
| Invalid JWT signature | `JWT_SECRET` min 32 chars in `.env` |
| 403 on all routes | Check JwtAuthFilter order |

### END THOUGHT

> "Security wired. Topic 3 — login + seed users."

---

# TOPIC 3 — AuthController + DataLoader

### YOU DO

**`service/AuthService.java`** — validate BCrypt password, return JWT via `JwtService`.

**`controller/AuthController.java`**:

```java
@PostMapping("/login")
public AuthResponse login(@RequestBody @Valid LoginRequest request) {
    return authService.login(request);
}
```

**`config/DataLoader.java`** — seed employee, manager, admin with BCrypt passwords.

### RUN

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{ "email": "employee@codekerdos.in", "password": "emp123" }
```

Expected: `{ "token": "eyJ...", "role": "EMPLOYEE", ... }`

### END THOUGHT

> "Login works. Topic 4 — expense endpoints."

---

# TOPIC 4 — ExpenseController (Employee Endpoints)

### YOU DO

**`controller/ExpenseController.java`**

```java
@PostMapping
public ExpenseResponse submit(@RequestBody @Valid SubmitExpenseRequest request,
                              Authentication authentication) {
    return expenseService.submit(request, authentication.getName());
}

@GetMapping("/mine")
public List<ExpenseResponse> mine(Authentication authentication) {
    return expenseService.findMine(authentication.getName());
}
```

### RUN

```http
POST http://localhost:8080/api/expenses
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Flight to Mumbai",
  "description": "Client meeting",
  "amount": 8500.00,
  "expenseDate": "2026-06-15"
}
```

```http
GET http://localhost:8080/api/expenses/mine
Authorization: Bearer <token>
```

No token → **401**

### END THOUGHT

> "Employee flow live. Topic 5 — error handling."

---

# TOPIC 5 — GlobalExceptionHandler

### YOU DO

**`exception/GlobalExceptionHandler.java`** — handle `ResourceNotFoundException`, `BadCredentialsException`, `MethodArgumentNotValidException` → JSON `{ status, message, timestamp }`.

Same pattern as EMS Week 2.

### END THOUGHT

> "Topic 6 — demo and Week 5 preview."

---

# TOPIC 6 — Full Demo + Homework

### Demo checklist

```
✅ 1. POST /api/auth/login (employee) → JWT
✅ 2. POST /api/expenses (Bearer) → PENDING
✅ 3. GET  /api/expenses/mine → list
✅ 4. Login as manager → different role in response
✅ 5. GET /api/expenses/mine (no auth) → 401
✅ 6. H2 console → users + expenses tables
```

### Homework

| # | Task |
|---|------|
| 1 | Submit 3 expenses as employee |
| 2 | Test all 3 demo logins |
| 3 | Read [Week 5 Class-1](../WEEK-5/Class-1.md) preview — `@PreAuthorize` |
| 4 | Push to GitHub — no `.env` |

### SAY — Week 5 preview

> "**Week 5 Saturday** = manager approve/reject, admin view all, date summary, own-expense rule.
> **Week 5 Sunday** = AI categorization, fraud flags, `@Async`, manager summary.
> **Project #2 complete** end of Week 5."

### END THOUGHT

> "Week 4 done: domain + JWT + employee submit. Week 5 = workflow + AI."

---

## QUICK REFERENCE

| Problem | Fix |
|---------|-----|
| 401 on APIs | Login first; Bearer header |
| Token expired | Login again |
| Users table empty | Check DataLoader |

---

## WEEK 4 CLASS 2 — Interview Quick Reference

| Question | Answer |
|----------|--------|
| JWT vs session? | JWT stateless signed token |
| Why BCrypt? | One-way salted password hash |
| What is Bearer token? | `Authorization: Bearer <jwt>` |
| What does JwtAuthFilter do? | Validates token, sets SecurityContext |

---

*CodeKerdos.in · Week 4 Class 2 · JWT + employee flow — approval in Week 5*
