# Week 5 · Class 1 — Saturday · Approval Workflow + @PreAuthorize

> **[← Week 5 Index](README.md)** · **Next → [Class 2 — AI](Class-2.md)**  
> **Coding folder:** `week-04-expense-approval`  
> **Previous week ← [Week 4](../WEEK-4/README.md)** (domain + JWT done)

---

## CLASS 1 — TOPICS

| # | Topic | Code? |
|---|-------|-------|
| 1 | Week 4 recap + JWT demo | Run |
| 2 | `@EnableMethodSecurity` + `@PreAuthorize` | Talk + code |
| 3 | Manager approve/reject endpoints | Code |
| 4 | Cannot approve own expense (business rule) | Code |
| 5 | Admin — view all expenses | Code |
| 6 | Date-range expense summary API | Code |
| 7 | Wrap + Sunday AI preview | Talk |

**Session goal:** 3-role approval workflow complete. AI features = **Sunday Class 2**.

---

## HOW TO RUN

Same as Week 4 — **F5** → **Week 4 EAS — Run with Groq (H2)**.  
Save Postman tokens: `employee_token`, `manager_token`, `admin_token`.

---

# TOPIC 1 — Week 4 Recap

### Quick fire

| Question | Answer |
|----------|--------|
| JWT vs Basic? | Login once → Bearer token |
| Submit status? | PENDING |
| Today's focus? | Manager approve/reject + admin + summary |

### RUN

```http
POST /api/auth/login
{ "email": "employee@codekerdos.in", "password": "emp123" }
```

```http
GET /api/expenses/mine
Authorization: Bearer <token>
```

### END THOUGHT

> "Today we unlock manager and admin endpoints."

---

# TOPIC 2 — Method Security: `@PreAuthorize`

### SAY

> "Authenticated is not enough — only **MANAGER** may approve.
> **`@PreAuthorize`** checks role before method runs."

### YOU DO

Add to `SecurityConfig.java`:

```java
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig { ... }
```

### DRAW

```
JWT → SecurityContext (ROLE_MANAGER) → @PreAuthorize("hasRole('MANAGER')") → Service
```

> *"Annotation = role gate. Service = business rule. Both in interviews."*

---

# TOPIC 3 — Manager Approve / Reject

### YOU DO

**`dto/RejectExpenseRequest.java`**

```java
public record RejectExpenseRequest(
        @NotBlank(message = "Rejection reason is required") String reason
) {}
```

**Extend `ExpenseService`** — `findPending()`, `approve()`, `reject()`, helpers.

**Extend `ExpenseController`:**

```java
@GetMapping("/pending")
@PreAuthorize("hasRole('MANAGER')")
public List<ExpenseResponse> pending() { ... }

@PatchMapping("/{id}/approve")
@PreAuthorize("hasRole('MANAGER')")
public ExpenseResponse approve(@PathVariable Long id, Authentication auth) { ... }

@PatchMapping("/{id}/reject")
@PreAuthorize("hasRole('MANAGER')")
public ExpenseResponse reject(@PathVariable Long id,
                              @RequestBody @Valid RejectExpenseRequest request,
                              Authentication auth) { ... }
```

**`BusinessRuleException`** + handler → **409 Conflict**

### RUN

```http
GET /api/expenses/pending
Authorization: Bearer <manager_token>
```

```http
PATCH /api/expenses/1/approve
Authorization: Bearer <manager_token>
```

---

# TOPIC 4 — Cannot Approve Own Expense

### YOU DO

```java
private void assertNotOwnExpense(Expense expense, AppUser reviewer) {
    if (expense.getSubmittedBy().getId().equals(reviewer.getId())) {
        throw new BusinessRuleException("Cannot approve or reject your own expense");
    }
}
```

### DEMO

Manager submits own expense → try approve → **409**

---

# TOPIC 5 — Admin View All

```java
@GetMapping
@PreAuthorize("hasRole('ADMIN')")
public List<ExpenseResponse> all() {
    return expenseService.findAll();
}
```

Employee token → **403** (expected)

---

# TOPIC 6 — Date-Range Summary

**`ExpenseSummaryResponse`** — totalAmount, countByStatus, countByCategory

```http
GET /api/expenses/summary?from=2026-01-01&to=2026-12-31
Authorization: Bearer <manager_token>
```

---

# TOPIC 7 — Wrap + Sunday Preview

### SAY

> "Saturday done: full approval workflow.
> **Sunday** = AI categorization, fraud flags, `@Async`, manager 3-line summary.
> **Project #2 complete** after Sunday demo."

### Homework

| # | Task |
|---|------|
| 1 | Approve + reject 2 expenses as manager |
| 2 | Test admin GET all |
| 3 | Read [Class-2.md](Class-2.md) AI pipeline section |

---

## QUICK REFERENCE

| Problem | Fix |
|---------|-----|
| 403 on /pending | Use manager token |
| 409 on approve | Own expense or not PENDING |

---

*CodeKerdos.in · Week 5 Class 1 · Approval workflow*
