# Week 5 · Class 2 — Sunday · AI Categorization, Fraud & Manager Summary

> **[← Week 5 Index](README.md)** · **Previous ← [Class 1 — Approval](Class-1.md)**  
> **Coding folder:** `week-04-expense-approval`

---

## CLASS 2 — TOPICS

| # | Topic | Code? |
|---|-------|-------|
| 1 | Saturday recap + AI pipeline design | Talk + draw |
| 2 | `AiExpenseAnalysisService` — structured JSON | Code |
| 3 | `@EnableAsync` on expense submit | Code |
| 4 | `AiManagerSummaryService` — 3-line digest | Code |
| 5 | Full demo + **Project #2 complete** | Wrap |

**Session goal:** AI auto-categorizes expenses, flags fraud, async processing, manager weekly summary.

---

# TOPIC 1 — AI Pipeline Design

### SAY

> "AI **enriches** expenses — never approves them.
> Groq returns **JSON** → Java parses → updates entity. Same pattern as EMS NL search."

### DRAW

```
POST /api/expenses → save PENDING → @Async AI → JSON { category, fraudFlags } → update DB
GET /api/ai/manager-summary → PromptTemplate(pending list) → 3-line text
```

### Contents

| Piece | Technology |
|-------|------------|
| Categorize | Travel / Food / Software / Equipment |
| Fraud flags | DUPLICATE_AMOUNT, EXCEEDS_POLICY, etc. |
| Context | PromptTemplate + recent expenses by same user |
| Async | User gets 201 immediately |

---

# TOPIC 2 — AiExpenseAnalysisService

### YOU DO

**`dto/ExpenseAiAnalysis.java`** — category, fraudFlags, confidence, reasoning

**`service/ai/AiExpenseAnalysisService.java`**

- PromptTemplate with `{title}`, `{description}`, `{amount}`, `{recentExpenses}`
- ChatClient → Groq → **JSON ONLY**
- Strip markdown fences → `ObjectMapper` parse → save category + `aiFraudFlags`

> Say: *"Return ONLY valid JSON — tighten prompt if model adds prose."*

### STUCK?

| Problem | Fix |
|---------|-----|
| Parse error | Log raw response; strip ```json fences |
| Wrong category | Add policy examples in prompt |

---

# TOPIC 3 — `@EnableAsync`

### YOU DO

**Main class:** `@EnableAsync`

**`AsyncExpenseAiProcessor`** — `@Async processExpenseAsync(expenseId)`

**`ExpenseService.submit()`** — call async processor after save

### RUN — wow moment

1. POST expense → immediate response, `category: null`  
2. Wait 3s → GET `/mine` → `TRAVEL` + fraud flags  

---

# TOPIC 4 — AiManagerSummaryService

**`GET /api/ai/manager-summary`** — `@PreAuthorize("hasRole('MANAGER')")`

PromptTemplate injects pending expenses + fraud flags → exactly **3 lines** plain English.

```http
GET /api/ai/manager-summary
Authorization: Bearer <manager_token>
```

---

# TOPIC 5 — Full Demo + Project #2 Complete

### Checklist

```
✅  1. Employee submit 2 expenses (one duplicate amount)
✅  2. Wait 3s → AI category + fraud flags
✅  3. Manager GET /pending + /api/ai/manager-summary
✅  4. Approve + reject
✅  5. Own-expense rule → 409
✅  6. Admin GET all + summary
✅  7. git push (no .env)
```

### WEEK 5 — Deliverables (Project #2 COMPLETE)

| # | Deliverable |
|---|-------------|
| 1 | JWT + 3-role workflow |
| 2 | `@PreAuthorize` + own-expense rule |
| 3 | Date-range summary |
| 4 | AI categorization (structured JSON) |
| 5 | Fraud flagging + `@Async` |
| 6 | Manager 3-line summary |

### SAY — Week 6 preview

> "**Project #2 done.** Next: **Booking Service + RAG** — AI that reads uploaded PDFs."

---

## WEEK 5 — Interview Quick Reference

| Question | Answer |
|----------|--------|
| `@PreAuthorize`? | Method-level role check |
| Why service-layer rules? | Domain logic beyond role (own-expense) |
| `@Async`? | Background thread — non-blocking AI |
| Structured JSON? | Parseable LLM output — Java controls DB |
| Why not AI approve? | Human accountability |

---

*CodeKerdos.in · Week 5 Class 2 · Project #2 complete*
