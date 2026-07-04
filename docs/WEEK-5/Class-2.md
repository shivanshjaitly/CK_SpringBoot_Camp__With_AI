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

First, create the folder `src/main/java/in/codekerdos/expense/service/ai/`.

**`dto/ExpenseAiAnalysis.java`** — the structured JSON Groq returns

```java
package in.codekerdos.expense.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.codekerdos.expense.enums.ExpenseCategory;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExpenseAiAnalysis(
        ExpenseCategory category,
        String fraudFlags,
        double confidence,
        String reasoning
) {}
```

> **Why `@JsonIgnoreProperties(ignoreUnknown = true)`?**  
> The LLM sometimes adds extra JSON fields we didn't ask for. This annotation tells Jackson to silently skip unknown fields instead of throwing an exception.

---

**`service/ai/AiExpenseAnalysisService.java`**

```java
package in.codekerdos.expense.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.codekerdos.expense.dto.ExpenseAiAnalysis;
import in.codekerdos.expense.entity.Expense;
import in.codekerdos.expense.repository.ExpenseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AiExpenseAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AiExpenseAnalysisService.class);

    private static final String ANALYSIS_PROMPT = """
            You are an expense policy analyst. Analyze this expense and return ONLY valid JSON.
            No markdown, no explanation — just JSON.

            Expense details:
            - Title: {title}
            - Description: {description}
            - Amount (INR): {amount}
            - Recent expenses by same user: {recentExpenses}

            Return JSON in exactly this shape:
            {
              "category": "TRAVEL|FOOD|SOFTWARE|EQUIPMENT|OTHER",
              "fraudFlags": "NONE or comma-separated: DUPLICATE_AMOUNT, EXCEEDS_POLICY, SUSPICIOUS_DESCRIPTION",
              "confidence": 0.0 to 1.0,
              "reasoning": "one sentence"
            }
            """;

    private final ChatClient chatClient;
    private final ExpenseRepository expenseRepository;
    private final ObjectMapper objectMapper;

    public AiExpenseAnalysisService(ChatClient.Builder chatClientBuilder,
                                    ExpenseRepository expenseRepository,
                                    ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.expenseRepository = expenseRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void analyzeAndUpdate(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId).orElse(null);
        if (expense == null) return;

        String recentExpenses = expenseRepository
                .findBySubmittedByOrderByCreatedAtDesc(expense.getSubmittedBy())
                .stream()
                .limit(5)
                .map(e -> e.getTitle() + " ₹" + e.getAmount())
                .reduce("", (a, b) -> a + "\n" + b);

        String filledPrompt = ANALYSIS_PROMPT
                .replace("{title}", expense.getTitle())
                .replace("{description}", expense.getDescription() != null ? expense.getDescription() : "")
                .replace("{amount}", expense.getAmount().toPlainString())
                .replace("{recentExpenses}", recentExpenses.isBlank() ? "none" : recentExpenses);

        try {
            String rawResponse = chatClient.prompt()
                    .user(filledPrompt)
                    .call()
                    .content();

            String cleaned = stripMarkdownFences(rawResponse);
            ExpenseAiAnalysis analysis = objectMapper.readValue(cleaned, ExpenseAiAnalysis.class);

            expense.setCategory(analysis.category());
            expense.setAiFraudFlags(analysis.fraudFlags());
            expense.setAiProcessedAt(Instant.now());
            expenseRepository.save(expense);

            log.info("AI analysis complete for expense {}: category={}, flags={}",
                    expenseId, analysis.category(), analysis.fraudFlags());

        } catch (Exception e) {
            log.error("AI analysis failed for expense {}: {}", expenseId, e.getMessage());
        }
    }

    private String stripMarkdownFences(String response) {
        return response
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();
    }
}
```

> **CODE WALKTHROUGH — AiExpenseAnalysisService:**
>
> | Section | Say aloud |
> |---------|-----------|
> | `ANALYSIS_PROMPT` text block | "We write the prompt as a Java text block — `{title}` etc. are simple `String.replace` placeholders. No special library needed." |
> | `"Return ONLY valid JSON"` | "This is the most important line in the prompt. Without it, Groq adds prose like 'Here is the analysis:' which breaks `ObjectMapper.readValue()`." |
> | `recentExpenses` last 5 | "We give Groq context — same user submitted ₹500 hotel 3 times this week? It can flag `DUPLICATE_AMOUNT`." |
> | `chatClient.prompt().user(filledPrompt).call().content()` | "The Spring AI ChatClient API. `.call()` blocks until Groq responds. `.content()` extracts the text. Same API we used in EMS." |
> | `stripMarkdownFences(rawResponse)` | "Groq sometimes wraps JSON in ````json ... ```. We strip those before parsing. Always do this defensive cleanup." |
> | `objectMapper.readValue(cleaned, ExpenseAiAnalysis.class)` | "Jackson deserializes the JSON string into our `ExpenseAiAnalysis` record. If the model deviated from the shape, `@JsonIgnoreProperties` saves us." |
> | `catch (Exception e)` + `log.error` | "AI must NEVER crash the app. If Groq is down or returns garbage, we log the error and move on — the expense stays with `category=null`." |

---

# TOPIC 3 — `@EnableAsync` + AsyncExpenseAiProcessor

### SAY

> "We call Groq after the expense is saved. But `POST /api/expenses` should return the 201 response immediately — not wait 2-3 seconds for Groq.
> `@Async` runs the AI call on a background thread. User gets a fast response; AI fills in category silently."

### YOU DO

**Step 1 — Enable async in main class**

```java
package in.codekerdos.expense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ExpenseApprovalApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExpenseApprovalApplication.class, args);
    }
}
```

---

**Step 2 — `service/ai/AsyncExpenseAiProcessor.java`**

```java
package in.codekerdos.expense.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AsyncExpenseAiProcessor {

    private static final Logger log = LoggerFactory.getLogger(AsyncExpenseAiProcessor.class);

    private final AiExpenseAnalysisService aiExpenseAnalysisService;

    public AsyncExpenseAiProcessor(AiExpenseAnalysisService aiExpenseAnalysisService) {
        this.aiExpenseAnalysisService = aiExpenseAnalysisService;
    }

    @Async
    public void processExpenseAsync(Long expenseId) {
        log.info("Starting async AI analysis for expense {}", expenseId);
        aiExpenseAnalysisService.analyzeAndUpdate(expenseId);
        log.info("Async AI analysis done for expense {}", expenseId);
    }
}
```

> **Why a separate `@Component` instead of `@Async` directly on the service?**  
> `@Async` works through a Spring proxy — same self-invocation rule as `@Transactional`.  
> If `AiExpenseAnalysisService` called its own `@Async` method, the proxy is bypassed and it runs synchronously.  
> Separating into `AsyncExpenseAiProcessor` guarantees the proxy intercepts the call.

---

**Step 3 — Wire into `ExpenseService.submit()`**

```java
// In ExpenseService — add this field:
private final AsyncExpenseAiProcessor asyncExpenseAiProcessor;

// Update constructor:
public ExpenseService(ExpenseRepository expenseRepository,
                      AppUserRepository appUserRepository,
                      AsyncExpenseAiProcessor asyncExpenseAiProcessor) {
    this.expenseRepository = expenseRepository;
    this.appUserRepository = appUserRepository;
    this.asyncExpenseAiProcessor = asyncExpenseAiProcessor;
}

// Update submit() — add one line after save:
@Transactional
public ExpenseResponse submit(SubmitExpenseRequest request, String userEmail) {
    AppUser user = appUserRepository.findByEmail(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Expense expense = new Expense();
    expense.setTitle(request.title());
    expense.setDescription(request.description());
    expense.setAmount(request.amount());
    expense.setExpenseDate(request.expenseDate());
    expense.setStatus(ExpenseStatus.PENDING);
    expense.setSubmittedBy(user);

    Expense saved = expenseRepository.save(expense);
    asyncExpenseAiProcessor.processExpenseAsync(saved.getId());  // ← fire and forget

    return ExpenseResponse.from(saved);
}
```

> **CODE WALKTHROUGH — `@Async` flow:**
>
> ```
> POST /api/expenses (main thread)
>   → ExpenseService.submit()
>   → expenseRepository.save()         ← DB write, gets id=7
>   → asyncExpenseAiProcessor          ← Spring hands off to thread pool
>        .processExpenseAsync(7)        ← returns immediately (non-blocking)
>   → return ExpenseResponse { category: null }   ← 201 returned to client
>
> Background thread (parallel):
>   → AiExpenseAnalysisService.analyzeAndUpdate(7)
>   → ChatClient → Groq API → JSON
>   → expense.setCategory(TRAVEL)
>   → expenseRepository.save()        ← DB updated
> ```

### RUN — wow moment

```http
POST http://localhost:8080/api/expenses
Authorization: Bearer <employee_token>
Content-Type: application/json

{
  "title": "Flight to Delhi",
  "description": "Client meeting",
  "amount": 8500.00,
  "expenseDate": "2026-07-01"
}
```

Response comes back immediately: `"category": null`

Wait 2–3 seconds → call:
```http
GET http://localhost:8080/api/expenses/mine
Authorization: Bearer <employee_token>
```

Now the same expense shows: `"category": "TRAVEL"`, `"aiFraudFlags": "NONE"`

> Say: *"The user didn't wait for Groq. AI ran in the background and silently updated the record."*

---

# TOPIC 4 — AiManagerSummaryService

### SAY

> "The manager wants a quick digest of pending expenses without reading every row.
> We send the full pending list to Groq and ask for a 3-line plain-English summary."

### YOU DO

**`service/ai/AiManagerSummaryService.java`**

```java
package in.codekerdos.expense.service.ai;

import in.codekerdos.expense.entity.Expense;
import in.codekerdos.expense.enums.ExpenseStatus;
import in.codekerdos.expense.repository.ExpenseRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiManagerSummaryService {

    private static final String SUMMARY_PROMPT = """
            You are an expense manager assistant. Write a 3-line plain-English summary of these pending expenses.
            Be concise. Flag anything suspicious. Use bullet points. No JSON.

            Pending expenses:
            {pendingExpenses}
            """;

    private final ChatClient chatClient;
    private final ExpenseRepository expenseRepository;

    public AiManagerSummaryService(ChatClient.Builder chatClientBuilder,
                                   ExpenseRepository expenseRepository) {
        this.chatClient = chatClientBuilder.build();
        this.expenseRepository = expenseRepository;
    }

    public String generateSummary() {
        List<Expense> pending = expenseRepository.findByStatusOrderByCreatedAtAsc(ExpenseStatus.PENDING);

        if (pending.isEmpty()) {
            return "No pending expenses at this time.";
        }

        String expenseList = pending.stream()
                .map(e -> "- %s | ₹%s | %s | fraud: %s".formatted(
                        e.getTitle(),
                        e.getAmount().toPlainString(),
                        e.getSubmittedBy().getEmail(),
                        e.getAiFraudFlags() != null ? e.getAiFraudFlags() : "not analysed"
                ))
                .reduce("", (a, b) -> a + "\n" + b);

        String filledPrompt = SUMMARY_PROMPT.replace("{pendingExpenses}", expenseList);

        return chatClient.prompt()
                .user(filledPrompt)
                .call()
                .content();
    }
}
```

---

**`controller/AiController.java`** — expose the summary endpoint

```java
package in.codekerdos.expense.controller;

import in.codekerdos.expense.service.ai.AiManagerSummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiManagerSummaryService aiManagerSummaryService;

    public AiController(AiManagerSummaryService aiManagerSummaryService) {
        this.aiManagerSummaryService = aiManagerSummaryService;
    }

    @GetMapping("/manager-summary")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<String> managerSummary() {
        return ResponseEntity.ok(aiManagerSummaryService.generateSummary());
    }
}
```

> **CODE WALKTHROUGH — AiManagerSummaryService:**
>
> | Section | Say aloud |
> |---------|-----------|
> | `expenseRepository.findByStatusOrderByCreatedAtAsc(PENDING)` | "We fetch only PENDING expenses — no point summarising already decided ones" |
> | `pending.isEmpty()` guard | "If there's nothing to review, skip the Groq API call. Saves tokens and latency." |
> | `.formatted(...)` | "Java 15+ method — like `String.format` but cleaner. Builds one line per expense." |
> | Fraud flags in the context | "We include the AI-analysed fraud flags so Groq can say 'two of these look suspicious'" |
> | `@PreAuthorize("hasRole('MANAGER')")` on controller | "Only managers can see the summary. Employee token → 403." |
> | Returns plain `String` not JSON | "This is prose, not structured data. The manager reads it, they don't parse it." |

### RUN

```http
GET http://localhost:8080/api/ai/manager-summary
Authorization: Bearer <manager_token>
```

Expected response (plain text):
```
• 3 pending expenses totalling ₹21,500 from employee@codekerdos.in
• Flight to Delhi (₹8,500) flagged as DUPLICATE_AMOUNT — submitted twice this week
• Hotel Chennai (₹4,000) looks legitimate; approve when ready
```

Try with employee token → **403 Forbidden**

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
