# Week 3 · Class 2 — Sunday · Open Q&A

> **[← Week 3 Index](README.md)** · **Previous ← [Class 1 — Security](Class-1.md)**  
> **Coding folder:** `week-02-employee-management` (optional — only if a demo answers a question)

---

## HOW TO USE THIS FILE

**This session is Q&A — not a feature build day.**

- Students ask; you answer live  
- Use EMS codebase on screen when helpful  
- Keep answers **short and practical** — redirect deep dives to Week 4+  
- No mandatory new files or homework project  

---

## SESSION PLAN

| Block | Duration | What |
|-------|----------|------|
| Warm-up recap | 10 min | CSRF + SecurityConfig one-liner recap |
| Open floor Q&A | 50 min | Students ask anything Weeks 1–3 |
| Interview lightning | 15 min | Rapid-fire from question bank below |
| Week 4–5 preview | 5 min | Expense project (2-week plan) |

---

# BLOCK 1 — Warm-Up Recap (10 min)

### SAY

> "Saturday was security theory — CSRF, filter chain, why our EMS config looks the way it does.
> Today is **your** session. No new topics unless your questions need them."

### Quick fire — same as Saturday end

| Question | Expected answer |
|----------|-----------------|
| What is CSRF? | Forged browser request using your session cookie |
| Why CSRF ignored on `/api/**`? | Postman uses Basic Auth — not cookie session for APIs |
| Where is security configured? | `SecurityConfig.java` |
| Groq key storage? | `.env` — never commit |

### END THOUGHT

> "Open floor — who has a question from Week 1, 2, or Saturday?"

---

# BLOCK 2 — Open Q&A (50 min)

### SAY — ground rules

> "One question at a time. If it's off-topic we park it for after class.
> If it's 'how do I fix my laptop' — quick answer or follow up on Slack."

### How to run

1. Ask for hands / chat queue  
2. Repeat question aloud so whole class hears  
3. Answer in **≤ 3 minutes** when possible  
4. If needs demo → open EMS project and show **one** file or Postman call  
5. If too deep → *"Great question — we'll touch that in Week 4 JWT / Week 5 RAG"*

---

## Question bank — Security (likely after Saturday)

| Question | Short answer |
|----------|--------------|
| Can I disable CSRF completely? | Yes for pure JWT/stateless APIs; risky for cookie-based browser apps |
| What's the difference between 401 and 403? | 401 = not authenticated; 403 = authenticated but not allowed |
| Is HTTP Basic safe? | OK for dev/HTTPS; production prefers OAuth2/JWT over HTTPS |
| Why `{noop}` password? | Demo only — encodes plain text for Spring Security teaching |
| What is BCrypt? | One-way password hash with salt — use in real apps (Week 4) |
| SQL injection — are we safe? | JPA parameterized queries help; never concat SQL strings |
| Should H2 console be public? | **Never in production** — dev convenience only |

---

## Question bank — Spring Boot / EMS (Weeks 1–2)

| Question | Short answer |
|----------|--------------|
| `@RestController` vs `@Controller`? | REST returns JSON body; Controller often returns view name (Thymeleaf) |
| What is `@Transactional`? | All DB ops in method succeed or roll back together |
| DTO vs Entity? | Entity = DB shape; DTO = API contract — don't expose entities directly |
| What is Pageable? | Page number + size + sort for large lists |
| H2 vs MySQL? | H2 in-memory for dev; MySQL persistent for production-shaped demo |
| How does NL search work? | Groq → JSON criteria → JPA Specification → DB query |
| Why layered architecture? | Controller / Service / Repository — separation of concerns |
| What is IoC / DI? | Spring creates and injects beans — from Week 1 |

---

## Question bank — Spring AI / Groq

| Question | Short answer |
|----------|--------------|
| Are we training AI? | **No** — calling Groq hosted LLM via ChatClient |
| Why JSON from LLM? | Parseable, testable — Java controls DB, not the model |
| 401 from Groq? | Wrong/missing `GROQ_API_KEY` in `.env` |
| 429 from Groq? | Rate limit — wait 30s (free tier) |
| Can AI replace SQL? | **No** — AI suggests filters; Java runs safe queries |

---

## Question bank — Tools / Setup

| Question | Short answer |
|----------|--------------|
| Maven red imports? | Reload Maven project; check Java 17 |
| F5 vs Run button? | F5 loads `.env` from launch.json — preferred |
| Postman Basic Auth not working? | `hr@codekerdos.in` / `hr123` on Authorization tab |
| MySQL connection refused? | Start MySQL service; check profile `mysql` |
| Accidentally committed `.env`? | Revoke Groq key immediately; remove from Git history |

---

### STUCK? — if room is quiet

Pick 2–3 questions from the bank above and ask the class *"Can anyone answer this?"* before you explain.

---

# BLOCK 3 — Interview Lightning Round (15 min)

### SAY

> "Quick answers — interview style. 30 seconds each."

| # | Question |
|---|----------|
| 1 | Explain CSRF in one sentence |
| 2 | JWT vs session — one difference |
| 3 | Why use DTOs in REST APIs? |
| 4 | What does `@SpringBootApplication` include? |
| 5 | How does Spring Data know `findByTeam`? |
| 6 | What is PromptTemplate used for? |
| 7 | Why never commit API keys? |
| 8 | What is `@PreAuthorize`? (preview — Week 4) |

Call on students. Fill gaps briefly.

---

# BLOCK 4 — Week 4–5 Preview (5 min)

### SAY

> "**Project #2 spans two weeks** — not one weekend.
> **Week 4** = new folder `week-04-expense-approval` — domain + JWT + employee submit (Sat–Sun).
> **Week 5** = manager approve/reject + AI categorization + fraud → **Project #2 complete**."

### Contents — what's coming

| Week | Sat | Sun |
|------|-----|-----|
| **4** | Enums, entities, ExpenseService | JWT, login, employee APIs |
| **5** | `@PreAuthorize`, approve/reject, summary | AI + `@Async`, manager digest |

### DRAW

```
Week 3 (this week)     →  Security + Q&A  (EMS)
Week 4                 →  Expense part 1 (domain + JWT)
Week 5                 →  Expense part 2 (workflow + AI) → Project #2 done
Week 6+                →  Booking + RAG
```

### Homework (optional)

| # | Task |
|---|------|
| 1 | Fix any open bugs in your EMS fork |
| 2 | Skim [`docs/WEEK-4/Class-1.md`](../WEEK-4/Class-1.md) — don't code ahead |
| 3 | Ensure Groq `.env` still works — `GET /api/ai/greet?name=Test` |

### END THOUGHT

> "Week 3 = security + clear doubts. Weeks 4–5 = build Project #2 properly."

---

*CodeKerdos.in · Week 3 Class 2 · Q&A session — no mandatory coding*
