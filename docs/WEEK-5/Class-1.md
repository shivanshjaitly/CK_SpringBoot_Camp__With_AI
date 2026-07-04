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
package in.codekerdos.expense.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectExpenseRequest(
        @NotBlank(message = "Rejection reason is required") String reason
) {}
```

---

**`service/BusinessRuleException.java`**

```java
package in.codekerdos.expense.service;

public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
```

Add handler in `GlobalExceptionHandler.java`:
```java
@ExceptionHandler(BusinessRuleException.class)
public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage(), Instant.now()));
}
```

---

**Extend `ExpenseService`** — add `findPending()`, `approve()`, `reject()`:

```java
@Transactional(readOnly = true)
public List<ExpenseResponse> findPending() {
    return expenseRepository.findByStatusOrderByCreatedAtAsc(ExpenseStatus.PENDING).stream()
            .map(ExpenseResponse::from)
            .toList();
}

@Transactional
public ExpenseResponse approve(Long expenseId, String reviewerEmail) {
    Expense expense = getExpenseOrThrow(expenseId);
    AppUser reviewer = getUserOrThrow(reviewerEmail);

    assertStatus(expense, ExpenseStatus.PENDING);
    assertNotOwnExpense(expense, reviewer);

    expense.setStatus(ExpenseStatus.APPROVED);
    expense.setReviewedBy(reviewer);

    return ExpenseResponse.from(expenseRepository.save(expense));
}

@Transactional
public ExpenseResponse reject(Long expenseId, String reason, String reviewerEmail) {
    Expense expense = getExpenseOrThrow(expenseId);
    AppUser reviewer = getUserOrThrow(reviewerEmail);

    assertStatus(expense, ExpenseStatus.PENDING);
    assertNotOwnExpense(expense, reviewer);

    expense.setStatus(ExpenseStatus.REJECTED);
    expense.setReviewedBy(reviewer);
    expense.setRejectionReason(reason);

    return ExpenseResponse.from(expenseRepository.save(expense));
}

// ── private helpers ──────────────────────────────────────────────

private Expense getExpenseOrThrow(Long id) {
    return expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
}

private AppUser getUserOrThrow(String email) {
    return appUserRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
}

private void assertStatus(Expense expense, ExpenseStatus required) {
    if (expense.getStatus() != required) {
        throw new BusinessRuleException(
                "Expense is already " + expense.getStatus() + " — cannot change");
    }
}

private void assertNotOwnExpense(Expense expense, AppUser reviewer) {
    if (expense.getSubmittedBy().getId().equals(reviewer.getId())) {
        throw new BusinessRuleException("Cannot approve or reject your own expense");
    }
}
```

> **CODE WALKTHROUGH:**
>
> | Method | Say aloud |
> |--------|-----------|
> | `assertStatus(expense, PENDING)` | "You can't approve something twice, or approve an already-rejected expense. This guard fires a 409 Conflict." |
> | `assertNotOwnExpense(...)` | "The business rule. Manager submits their own ₹50,000 expense and approves it themselves — fraud. We block this." |
> | `expense.setReviewedBy(reviewer)` | "This sets the FK in the DB — we know who acted on it and when." |

---

**Extend `ExpenseController`** — add manager endpoints:

```java
@GetMapping("/pending")
@PreAuthorize("hasRole('MANAGER')")
public ResponseEntity<List<ExpenseResponse>> pending() {
    return ResponseEntity.ok(expenseService.findPending());
}

@PatchMapping("/{id}/approve")
@PreAuthorize("hasRole('MANAGER')")
public ResponseEntity<ExpenseResponse> approve(@PathVariable Long id,
                                               Authentication authentication) {
    return ResponseEntity.ok(expenseService.approve(id, authentication.getName()));
}

@PatchMapping("/{id}/reject")
@PreAuthorize("hasRole('MANAGER')")
public ResponseEntity<ExpenseResponse> reject(@PathVariable Long id,
                                              @RequestBody @Valid RejectExpenseRequest request,
                                              Authentication authentication) {
    return ResponseEntity.ok(expenseService.reject(id, request.reason(), authentication.getName()));
}
```

> **Why `@PatchMapping` not `@PutMapping`?**  
> `PUT` replaces the entire resource. `PATCH` updates specific fields — we're only changing `status` and `reviewedBy`. Use `PATCH` for partial updates.

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

Already wired via `assertNotOwnExpense()` in Topic 3.

### DEMO

1. Login as manager (`manager@codekerdos.in`)
2. Submit an expense as manager:
```http
POST /api/expenses
Authorization: Bearer <manager_token>
{ "title": "Laptop Stand", "amount": 1500, "expenseDate": "2026-07-01" }
```
3. Try to approve that same expense:
```http
PATCH /api/expenses/{id}/approve
Authorization: Bearer <manager_token>
```
Expected → **409 Conflict**
```json
{
  "status": 409,
  "message": "Cannot approve or reject your own expense",
  "timestamp": "..."
}
```

### SAY

> "This is a service-layer rule — not a role rule. `@PreAuthorize` only checks *who* you are.
> Business rules like 'own expense' are coded in the service — that's why we separate controller from service."

---

# TOPIC 5 — Admin View All

**Add to `ExpenseService`:**

```java
@Transactional(readOnly = true)
public List<ExpenseResponse> findAll() {
    return expenseRepository.findAll().stream()
            .map(ExpenseResponse::from)
            .toList();
}
```

**Add to `ExpenseController`:**

```java
@GetMapping
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<List<ExpenseResponse>> all() {
    return ResponseEntity.ok(expenseService.findAll());
}
```

### RUN

```http
GET /api/expenses
Authorization: Bearer <admin_token>
```
→ All expenses from all users.

```http
GET /api/expenses
Authorization: Bearer <employee_token>
```
→ **403 Forbidden** (expected — `@PreAuthorize` rejects non-ADMIN)

---

# TOPIC 6 — Date-Range Expense Summary

**`dto/ExpenseSummaryResponse.java`**

```java
package in.codekerdos.expense.dto;

import in.codekerdos.expense.enums.ExpenseCategory;
import in.codekerdos.expense.enums.ExpenseStatus;

import java.math.BigDecimal;
import java.util.Map;

public record ExpenseSummaryResponse(
        BigDecimal totalAmount,
        long totalCount,
        Map<ExpenseStatus, Long> countByStatus,
        Map<ExpenseCategory, Long> countByCategory
) {}
```

**Add to `ExpenseService`:**

```java
@Transactional(readOnly = true)
public ExpenseSummaryResponse getSummary(LocalDate from, LocalDate to) {
    List<Expense> expenses = expenseRepository.findByExpenseDateBetween(from, to);

    BigDecimal total = expenses.stream()
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    Map<ExpenseStatus, Long> byStatus = expenses.stream()
            .collect(java.util.stream.Collectors.groupingBy(Expense::getStatus,
                    java.util.stream.Collectors.counting()));

    Map<ExpenseCategory, Long> byCategory = expenses.stream()
            .filter(e -> e.getCategory() != null)
            .collect(java.util.stream.Collectors.groupingBy(Expense::getCategory,
                    java.util.stream.Collectors.counting()));

    return new ExpenseSummaryResponse(total, expenses.size(), byStatus, byCategory);
}
```

**Add to `ExpenseController`:**

```java
@GetMapping("/summary")
@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
public ResponseEntity<ExpenseSummaryResponse> summary(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return ResponseEntity.ok(expenseService.getSummary(from, to));
}
```

### RUN

```http
GET /api/expenses/summary?from=2026-01-01&to=2026-12-31
Authorization: Bearer <manager_token>
```

Expected:
```json
{
  "totalAmount": 18500.00,
  "totalCount": 3,
  "countByStatus": { "PENDING": 1, "APPROVED": 2 },
  "countByCategory": { "TRAVEL": 2, "FOOD": 1 }
}
```

> **CODE WALKTHROUGH:**
>
> | Line | Say aloud |
> |------|-----------|
> | `BigDecimal.ZERO, BigDecimal::add` | "Sum all amounts. We use `BigDecimal.ZERO` as identity — same reason we never use `double` for money." |
> | `Collectors.groupingBy(Expense::getStatus, counting())` | "Stream API builds the count-by-status map in one line. No for-loop needed." |
> | `hasRole('MANAGER') or hasRole('ADMIN')` | "Either role can see the summary. Admins for compliance, managers for their team." |
> | `@DateTimeFormat(iso = ISO.DATE)` | "Tells Spring to parse `?from=2026-01-01` as a `LocalDate`. Without this, Spring can't deserialize the query param." |

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
