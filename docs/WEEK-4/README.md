# Week 4

| Class | When | File | Code |
|-------|------|------|------|
| **Class 1** | Saturday | [Class-1.md](Class-1.md) | `week-04-expense-approval` |
| **Class 2** | Sunday | [Class-2.md](Class-2.md) | `week-04-expense-approval` |

**Project #2 spans Week 4 + Week 5** (4 classes total — not rushed into 2).

| Week | Classes | Covers |
|------|---------|--------|
| **Week 4** | Sat + Sun | Domain scaffold + JWT + employee submit |
| **Week 5** | Sat + Sun | Approve/reject + AI + **Project #2 complete** |

**Starting checkpoint:** Week 3 (Security + Q&A) + Project #1 (EMS).

Extra: [Groq setup](../groq-setup.md) · **Next → [Week 5](../WEEK-5/README.md)**

[← Back to Week 3](../WEEK-3/README.md) · [← START-HERE](../../START-HERE.md)

---

## HLD — What We Are Building (Week 4 + Week 5)

### The Big Idea

We are building an **Expense Approval System** — a REST API that digitises the real-world flow:

> Employee submits a work expense → Manager reviews and approves/rejects → AI silently checks for fraud and auto-categorises in the background.

This is Project #2. It builds on everything from Weeks 1–3 (Spring Core, JPA, REST, Spring Security) and adds JWT auth + Spring AI on top.

---

### Roles & Who Can Do What

```
EMPLOYEE  → submit expenses · view own submissions
MANAGER   → see pending queue · approve · reject · get AI summary
ADMIN     → view ALL expenses · get date-range summary
```

Three roles, one app, role-based access on every endpoint.

---

### Domain Model

```
AppUser
├── id, email (unique), password (BCrypt), fullName
└── role: EMPLOYEE | MANAGER | ADMIN

Expense
├── id, title, description, amount, expenseDate
├── status: PENDING → APPROVED | REJECTED
├── category: TRAVEL | FOOD | SOFTWARE | EQUIPMENT | OTHER  ← set by AI
├── submittedBy → AppUser (many-to-one)
├── reviewedBy  → AppUser (many-to-one, nullable)
├── rejectionReason (nullable)
├── aiFraudFlags (nullable)  ← set by AI: DUPLICATE_AMOUNT, EXCEEDS_POLICY, SUSPICIOUS_DESCRIPTION
└── aiProcessedAt, createdAt
```

---

### API Surface

| Method | Path | Who | What |
|--------|------|-----|------|
| `POST` | `/api/auth/login` | anyone | get JWT token |
| `POST` | `/api/expenses` | EMPLOYEE | submit a new expense |
| `GET` | `/api/expenses/mine` | EMPLOYEE | list my own expenses |
| `GET` | `/api/expenses/pending` | MANAGER | see all PENDING expenses |
| `PATCH` | `/api/expenses/{id}/approve` | MANAGER | approve an expense |
| `PATCH` | `/api/expenses/{id}/reject` | MANAGER | reject with reason |
| `GET` | `/api/expenses` | ADMIN | list every expense |
| `GET` | `/api/expenses/summary` | MANAGER / ADMIN | totals by status over a date range |
| `GET` | `/api/ai/manager-summary` | MANAGER | AI plain-English summary of pending queue |

---

### How JWT Auth Works

```
Client → POST /api/auth/login {email, password}
       ← 200 { token: "eyJhbG..." }

Every subsequent request:
Client → Authorization: Bearer <token>
Server → JwtAuthFilter extracts user → sets SecurityContext → route allowed/denied
```

Token is valid for 24 hours. No session, no cookie — fully stateless.

---

### AI Integration (Groq + LLaMA 3.3 70B)

Two AI features, both powered by Spring AI calling Groq:

**1 — Auto-categorise + fraud check (async, per expense)**
- Triggers immediately after an employee submits an expense
- Runs in background (`@Async`) — submit API returns instantly, AI fills in later
- AI reads: title, description, amount, user's last 5 expenses
- AI returns: `category`, `fraudFlags`, `confidence`, `reasoning`
- Result stored back on the `Expense` row (`aiFraudFlags`, `aiProcessedAt`, `category`)

**2 — Manager queue summary (on-demand)**
- Manager calls `GET /api/ai/manager-summary`
- AI reads all PENDING expenses and writes a 3-bullet plain-English summary
- Flags anything that looks suspicious
- Useful before a bulk approve/reject session

---

### Tech Stack

| Layer | Technology |
|-------|------------|
| Framework | Spring Boot 3.x |
| Security | Spring Security 6 + JWT (JJWT) |
| Database | H2 (dev / demo) · MySQL (prod profile) |
| ORM | Spring Data JPA / Hibernate |
| AI | Spring AI → Groq API (LLaMA 3.3 70B) |
| Validation | Jakarta Bean Validation (`@Valid`) |
| Password | BCrypt |

---

### Week-by-Week Build Plan

```
WEEK 4 — Saturday (Class 1)
  └── Project scaffold · enums · entities · repositories · DTOs · ExpenseService (submit + list mine)

WEEK 4 — Sunday (Class 2)
  └── JWT (JwtService + JwtAuthFilter) · SecurityConfig · BCrypt · AuthController (login) · ExpenseController (employee endpoints) · GlobalExceptionHandler

WEEK 5 — Saturday (Class 1)
  └── @PreAuthorize · manager approve/reject · admin list all · date-range summary

WEEK 5 — Sunday (Class 2) — PROJECT COMPLETE
  └── AI categorisation (AiExpenseAnalysisService) · fraud flags · @Async processing · AiManagerSummaryService · AiController · end-to-end demo
```

---

### Flow Diagram (text)

```
Employee                     API                          Manager / Admin
   │                          │                                 │
   ├── POST /login ──────────►│◄─── POST /login ───────────────┤
   │◄── JWT token ────────────┤──── JWT token ────────────────►│
   │                          │                                 │
   ├── POST /expenses ────────►│                                 │
   │   (Bearer token)         │── save PENDING ──────────────►DB│
   │◄── 201 ExpenseResponse ──│                                 │
   │                          │── @Async AI analysis ──────►Groq│
   │                          │   (sets category + fraudFlags)  │
   │                          │                                 │
   │                          │◄── GET /pending ───────────────┤
   │                          │──── PENDING list ─────────────►│
   │                          │                                 │
   │                          │◄── PATCH /{id}/approve ────────┤
   │                          │──── status=APPROVED ──────────►DB
   │                          │                                 │
   │                          │◄── GET /ai/manager-summary ─────┤
   │                          │──── AI summary ───────────────►│
```
