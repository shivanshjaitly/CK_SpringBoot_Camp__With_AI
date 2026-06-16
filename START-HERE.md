# START HERE — CodeKerdos Bootcamp

## One rule: which file to open

| When | Open this file only | Code in VS Code / Cursor |
|------|---------------------|--------------------------|
| **Week 1 · Saturday** | [`docs/WEEK-1/Class-1.md`](docs/WEEK-1/Class-1.md) | `week-01-spring-core-demo` |
| **Week 1 · Sunday** | [`docs/WEEK-1/Class-2.md`](docs/WEEK-1/Class-2.md) | `week-01-employee-management` |
| **Week 2 · Saturday** | [`docs/WEEK-2/Class-1.md`](docs/WEEK-2/Class-1.md) | `week-02-employee-management` |
| **Week 2 · Sunday** | [`docs/WEEK-2/Class-2.md`](docs/WEEK-2/Class-2.md) | `week-02-employee-management` |
| Week 3+ | `docs/WEEK-3/Class-1.md` (not created yet) | TBD |

---

## Repo layout (simple)

```
docs/
  WEEK-1/
    Class-1.md    ← Week 1 Saturday script
    Class-2.md    ← Week 1 Sunday script
  WEEK-2/
    Class-1.md    ← Week 2 Saturday script
    Class-2.md    ← Week 2 Sunday script

week-01-spring-core-demo/         ← Week 1 Saturday code
week-01-employee-management/      ← Week 1 Sunday checkpoint (application.yml)
week-02-employee-management/      ← Week 2 full EMS — Project #1
```

**No other doc files needed.** Architecture is inside each topic — not a separate file.

---

## What's ready vs not ready

| Week | Status |
|------|--------|
| Week 1 | ✅ Ready (Class 1 + Class 2) |
| Week 2 | ✅ Ready (Class 1 + Class 2) |
| Week 3–7 | ❌ Not created yet |

---

## Week topic names (quick reference)

**Week 1 Sat:** Welcome → Why Spring → Maven → IoC → DI → @Autowired → @Configuration → Scopes

**Week 1 Sun:** Spring Boot → application.yml → JPA → CRUD → Spring AI → ChatClient → PromptTemplate

**Week 2 Sat:** Recap → AI crisp → Entities → CRUD → ChatClient → PromptTemplate → Security (Basic)

**Week 2 Sun:** MySQL → Pagination → NL HR Search → Form login (Thymeleaf) → Project #1 complete

---

## Run Week 2 project (VS Code / Cursor)

1. `cp week-02-employee-management/.env.example week-02-employee-management/.env`
2. Paste Groq key in `.env` → save
3. **F5** → **Week 2 EMS — Run with Groq (H2)** (Saturday)
4. **F5** → **Week 2 EMS — Run with MySQL** (Sunday — MySQL must be running)
5. Postman Basic Auth: `hr@codekerdos.in` / `hr123`
6. Browser login: `http://localhost:8080/login`

Full steps: [`docs/WEEK-2/Class-1.md`](docs/WEEK-2/Class-1.md) → **HOW TO RUN**

---

CodeKerdos.in · Spring Boot + AI Bootcamp
