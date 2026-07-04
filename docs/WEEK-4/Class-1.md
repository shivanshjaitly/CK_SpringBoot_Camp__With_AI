# Week 4 · Class 1 — Saturday · Expense Domain + Project Scaffold

> **[← Week 4 Index](README.md)** · **Next → [Class 2 — JWT + Employee Flow](Class-2.md)**  
> **Coding folder:** `week-04-expense-approval` (NEW folder — Project #2 starts here)

---

## HOW TO USE THIS FILE (read once)

You teach **Topic 1 → 9 in order**.  
For each topic:

1. **SAY** — read aloud to students  
2. **DRAW** — whiteboard (optional)  
3. **YOU DO** — exact VS Code / Cursor clicks + files to create  
4. **CODE** — copy-paste or type slowly  
5. **RUN** — what output you must see  
6. **STUCK?** — Copilot one-liner prompt  

**Students follow you in THEIR IDE.** You build live on screen — do NOT open your private reference repo.

**Pattern:** talk → code → talk → code (no two heavy theory blocks back-to-back).

---

## CLASS 1 — TOPICS

| # | Topic | Do you create files? |
|---|-------|---------------------|
| 1 | Week 3 security recap + Project #2 intro | No — talk only |
| 2 | **AI in Expense project (crisp)** | No — board only |
| 3 | New folder + 3-role architecture | ✅ **Scaffold project** |
| 4 | Enums — Role, ExpenseStatus, ExpenseCategory | ✅ **enums/** |
| 5 | JPA entities — AppUser & Expense | ✅ **entity/** |
| 6 | Repositories | ✅ **repository/** |
| 7 | DTOs + validation | ✅ **dto/** |
| 8 | ExpenseService — submit + list mine | ✅ **service/** |
| 9 | Wrap + homework | Talk |

**Sunday (Class 2):** JWT auth + login + employee REST endpoints — see [Class-2.md](Class-2.md)  
**Week 5:** Manager approve/reject + AI — see [Week 5](../WEEK-5/README.md)

---

## FILES YOU WILL CREATE (Class 1 end state — Saturday)

```
week-04-expense-approval/
├── pom.xml
├── .env.example
└── src/main/
    ├── resources/application.yml
    └── java/in/codekerdos/expense/
        ├── ExpenseApprovalApplication.java
        ├── enums/Role.java, ExpenseStatus.java, ExpenseCategory.java
        ├── entity/AppUser.java, Expense.java
        ├── repository/AppUserRepository.java, ExpenseRepository.java
        ├── dto/SubmitExpenseRequest.java, ExpenseResponse.java
        └── service/ExpenseService.java, ResourceNotFoundException.java
```

> **Sunday adds:** JWT security, auth login, ExpenseController, GlobalExceptionHandler

---

**Time split:**

| Block | Duration | Topics |
|-------|----------|--------|
| Recap + AI crisp | 15 min | 1, 2 |
| Scaffold + domain model | 25 min | 3–5 |
| DTOs + service layer | 40 min | 6–8 |
| Wrap | 5 min | 9 |

---

## HOW TO RUN — VS Code / Cursor (do this BEFORE class)

### One-time setup

| Step | Action |
|------|--------|
| 1 | Open repo root in VS Code / Cursor |
| 2 | Install **Extension Pack for Java** + **Spring Boot Extension Pack** |
| 3 | Wait for Maven import (bottom-right progress bar) |
| 4 | In terminal: `cp week-04-expense-approval/.env.example week-04-expense-approval/.env` |
| 5 | Open `.env` → paste Groq key after `GROQ_API_KEY=` |
| 6 | Add `JWT_SECRET=codekerdos-demo-secret-change-in-prod-min-32-chars` to `.env` |
| 7 | Save `.env` — **never commit** |

### Every time you run the app

| Step | Action |
|------|--------|
| 1 | Stop any old run (red square) |
| 2 | **F5** → **Week 4 EAS — Run with Groq (H2)** |
| 3 | Wait for: `Tomcat started on port 8080` |
| 4 | Postman: `POST /api/auth/login` → copy `token` from response |
| 5 | All other requests: Header `Authorization: Bearer <token>` |

### Demo users (seeded on startup)

| Email | Password | Role |
|-------|----------|------|
| `employee@codekerdos.in` | `emp123` | EMPLOYEE |
| `manager@codekerdos.in` | `mgr123` | MANAGER |
| `admin@codekerdos.in` | `adm123` | ADMIN |

---

# TOPIC 1 — Week 3 Recap + Project #2 Intro

### SAY

> "Week 2 = **Project #1 complete** — EMS with MySQL, pagination, NL search, login page.
> Week 3 = **Security lecture** — CSRF, filter chain, why our SecurityConfig looks that way.
> Today we start **Project #2 — Expense Approval System** in a **new folder**.
> Same Spring patterns — new domain, new security model: **JWT + BCrypt instead of Basic + `{noop}`**."

### Quick fire questions

| Question | Expected answer |
|----------|-----------------|
| What protected EMS APIs? | Spring Security — HTTP Basic + form login |
| What is Pageable? | Page number, size, sort for large lists |
| How does NL search work? | LLM → JSON criteria → JPA Specification → DB |
| Why DTOs? | Clean API contract, hide entity internals |
| How many portfolio projects? | 3 total — EMS done, Expense today, Booking later |

### DRAW

```
week-02-employee-management  →  Project #1 ✅ DONE
week-04-expense-approval     →  Project #2 (today + Sunday)
```

### Contents — Project #2 at a glance

| Feature | Detail |
|---------|--------|
| Roles | EMPLOYEE submits · MANAGER approves/rejects · ADMIN views all |
| Security | JWT Bearer tokens (stateless) |
| Workflow | PENDING → APPROVED / REJECTED |
| Business rule | Manager cannot approve own expense (Sunday) |
| AI (Sunday) | Auto-categorize, fraud flags, manager summary |

### END THOUGHT

> "Next: what AI does in the Expense project — students always ask this."

---

# TOPIC 2 — AI in Expense Project (Crisp)

### SAY

> "Same rule as Week 1–2: we **call Groq from Java** via Spring AI ChatClient.
> We don't train models. AI is a **service inside our Spring app**."

### Contents (draw on board — 8 minutes max)

| Bootcamp project | AI feature | When |
|------------------|------------|------|
| EMS ✅ | Greet, onboarding, NL search | Week 1–2 |
| **Expense (this project)** | Categorize + fraud flag + manager summary | **Sunday** |
| Booking + RAG | Document Q&A with retrieval | Week 4+ |

### Expense AI roadmap (this week)

| Feature | AI does what | When |
|---------|--------------|------|
| Auto-categorization | Travel / Food / Software / Equipment from title + description | Sunday |
| Fraud flagging | Duplicates, unusual amounts, policy violations | Sunday |
| Manager summary | 3-line plain-English pending approvals digest | Sunday |

### DRAW

```
Your Java code  →  ChatClient  →  Groq API  →  Llama model  →  JSON back
     ↑                                                              ↓
  @Service bean                                            update Expense entity
```

> Say: *"Today = domain + JWT. Sunday = structured JSON output + @Async AI calls."*

### YOU DO

Nothing — board only.

### END THOUGHT

> "AI is a tool inside our Spring app — not a separate magic layer. Topic 3 — scaffold the project."

---

# TOPIC 3 — New Folder + 3-Role Architecture

### SAY

> "New folder `week-04-expense-approval`. Fresh project — **do not** copy EMS entities.
> Three roles, one workflow. Every enterprise approval system looks like this."

### DRAW — role workflow

```
EMPLOYEE                    MANAGER                    ADMIN
   │                           │                         │
   │ POST /api/expenses        │ PATCH .../approve       │ GET /api/expenses
   │ GET  /api/expenses/mine   │ PATCH .../reject        │ GET /api/expenses/summary
   │                           │ GET  /api/expenses/pending
   ▼                           ▼                         ▼
         Expense status:  PENDING  →  APPROVED / REJECTED
```

### DRAW — layered architecture (same as EMS)

```
HTTP Request (+ JWT Bearer header)
     ↓
Controller  ← @RestController — JSON in/out
     ↓
Service     ← @Service — business rules + @PreAuthorize (Sunday)
     ↓
Repository  ← JpaRepository — database
     ↓
Database (H2 today, MySQL optional Sunday)
```

**Layman:** Employee fills a form → Manager signs off → Admin sees everything.

### YOU DO — Scaffold project

**Step 1 — Create folder**

1. In repo root, create folder: `week-04-expense-approval`
2. Tell students: *"New domain, new package `in.codekerdos.expense` — follow me from scratch."*

**Step 2 — Replace `pom.xml`**

Paste full content (web, JPA, validation, security, H2, MySQL, Spring AI, **JJWT**):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.1</version>
        <relativePath/>
    </parent>

    <groupId>in.codekerdos</groupId>
    <artifactId>week-04-expense-approval</artifactId>
    <version>3.0-SNAPSHOT</version>
    <name>CodeKerdos Week 4 - Expense Approval System</name>
    <description>Expense approval with JWT, roles, and Groq AI</description>

    <properties>
        <java.version>17</java.version>
        <spring-ai.version>1.0.0-M5</spring-ai.version>
        <jjwt.version>0.12.6</jjwt.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.ai</groupId>
                <artifactId>spring-ai-bom</artifactId>
                <version>${spring-ai.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
        </dependency>
        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

    <repositories>
        <repository>
            <id>spring-milestones</id>
            <url>https://repo.spring.io/milestone</url>
            <snapshots><enabled>false</enabled></snapshots>
        </repository>
    </repositories>
</project>
```

> Say: *"JJWT = Java library to create and verify JWT tokens. No Thymeleaf this week — API-only."*

**Step 3 — Create package + main class**

`src/main/java/in/codekerdos/expense/ExpenseApprovalApplication.java`

```java
package in.codekerdos.expense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExpenseApprovalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseApprovalApplication.class, args);
    }
}
```

**Step 4 — `application.yml`**

```yaml
server:
  port: 8080

spring:
  application:
    name: expense-approval
  profiles:
    active: h2

  ai:
    openai:
      api-key: ${GROQ_API_KEY}
      base-url: https://api.groq.com/openai
      chat:
        options:
          model: llama-3.3-70b-versatile

app:
  jwt:
    secret: ${JWT_SECRET:codekerdos-demo-secret-change-in-prod-min-32-chars}
    expiration-ms: 86400000

---
spring:
  config:
    activate:
      on-profile: h2
  datasource:
    url: jdbc:h2:mem:expensedb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

---
spring:
  config:
    activate:
      on-profile: mysql
  datasource:
    url: jdbc:mysql://localhost:3306/expense_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
    username: expense_user
    password: expense_pass
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect
```

**Step 5 — `.env.example`**

```env
GROQ_API_KEY=gsk_your_key_here
JWT_SECRET=codekerdos-demo-secret-change-in-prod-min-32-chars
```

**Step 6 — Maven reload**

VS Code → Maven panel → Reload. Wait for dependencies.

### RUN

F5 → app starts (Security blocks most routes — that's OK for now).

### STUCK?

Copilot: *"Create Spring Boot 3.4 pom with web, JPA, validation, security, H2, Spring AI Groq, JJWT 0.12"*

### END THOUGHT

> "Skeleton exists. Topic 4 — enums for roles and workflow states."

---

# TOPIC 4 — Enums: Role, ExpenseStatus, ExpenseCategory

### SAY

> "Enums = fixed set of values Java understands at compile time.
> `@Enumerated(STRING)` stores the name in DB — readable in H2 console."

### YOU DO

**FILE 1 — `enums/Role.java`**

```java
package in.codekerdos.expense.enums;

public enum Role {
    EMPLOYEE,
    MANAGER,
    ADMIN
}
```

**FILE 2 — `enums/ExpenseStatus.java`**

```java
package in.codekerdos.expense.enums;

public enum ExpenseStatus {
    PENDING,
    APPROVED,
    REJECTED
}
```

**FILE 3 — `enums/ExpenseCategory.java`**

```java
package in.codekerdos.expense.enums;

public enum ExpenseCategory {
    TRAVEL,
    FOOD,
    SOFTWARE,
    EQUIPMENT,
    OTHER
}
```

> **CODE WALKTHROUGH — explain as you type:**
>
> | Enum | Values | Say aloud |
> |------|--------|-----------|
> | `Role` | EMPLOYEE, MANAGER, ADMIN | "These three roles control what every user can do in the system. EMPLOYEE submits, MANAGER approves, ADMIN watches everything." |
> | `ExpenseStatus` | PENDING → APPROVED / REJECTED | "An expense can only be in one of these three states. It always starts at PENDING — we enforce that in the service layer, not here." |
> | `ExpenseCategory` | TRAVEL, FOOD, SOFTWARE, EQUIPMENT, OTHER | "We define valid categories in code, not as free-text in the DB. AI will pick one from this list on Sunday. OTHER is the fallback." |
>
> **Why enum, not a String field?**  
> If you used `String category`, someone could set `"travel"` (typo) and your DB would have garbage.  
> With an enum, the compiler rejects any invalid value at compile time.

### DRAW

```
Expense lifecycle:
  submit ──► PENDING ──approve──► APPROVED
                  └──reject──► REJECTED
```

### END THOUGHT

> "Fixed vocabulary for our domain. Topic 5 — entities."

---

# TOPIC 5 — JPA Entities: AppUser & Expense

### SAY

> "Two core tables: **users** (who) and **expenses** (what).
> Expense links to the employee who submitted it and optionally the manager who reviewed."

### YOU DO

**FILE 1 — `entity/AppUser.java`**

```java
package in.codekerdos.expense.entity;

import in.codekerdos.expense.enums.Role;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
```

> **CODE WALKTHROUGH — explain line by line as you type:**
>
> | Line | What it does | Say aloud |
> |------|--------------|-----------|
> | `@Entity` | Tells JPA "make a DB table from this class" | "This annotation is the bridge between Java and the database" |
> | `@Table(name = "users")` | Names the table `users` instead of `app_user` | "We control the table name — `users` reads better in SQL" |
> | `@Id` + `@GeneratedValue` | Auto-increment primary key — DB assigns 1, 2, 3… | "We never set the ID ourselves — the DB handles that" |
> | `@Column(unique = true)` on email | Database-level uniqueness constraint | "Two users cannot share an email — DB enforces this, not just Java code" |
> | `@Enumerated(EnumType.STRING)` | Stores `"EMPLOYEE"` in DB, not `0` | "STRING is readable in H2 console — always prefer it over ORDINAL" |
> | getters / setters | Spring and JPA need them to read/write fields | "Private fields + public getters/setters = encapsulation" |

**FILE 2 — `entity/Expense.java`**

```java
package in.codekerdos.expense.entity;

import in.codekerdos.expense.enums.ExpenseCategory;
import in.codekerdos.expense.enums.ExpenseStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseStatus status = ExpenseStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_id", nullable = false)
    private AppUser submittedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private AppUser reviewedBy;

    private String rejectionReason;

    // AI fields (populated Week 5 Sunday)
    private String aiFraudFlags;
    private Instant aiProcessedAt;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }

    public ExpenseStatus getStatus() { return status; }
    public void setStatus(ExpenseStatus status) { this.status = status; }

    public ExpenseCategory getCategory() { return category; }
    public void setCategory(ExpenseCategory category) { this.category = category; }

    public AppUser getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(AppUser submittedBy) { this.submittedBy = submittedBy; }

    public AppUser getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(AppUser reviewedBy) { this.reviewedBy = reviewedBy; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getAiFraudFlags() { return aiFraudFlags; }
    public void setAiFraudFlags(String aiFraudFlags) { this.aiFraudFlags = aiFraudFlags; }

    public Instant getAiProcessedAt() { return aiProcessedAt; }
    public void setAiProcessedAt(Instant aiProcessedAt) { this.aiProcessedAt = aiProcessedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
```

> **CODE WALKTHROUGH — explain line by line as you type:**
>
> | Line | What it does | Say aloud |
> |------|--------------|-----------|
> | `BigDecimal amount` | Exact decimal math — no rounding errors | "Never use `double` for money. `double` gives you 8499.999... — BigDecimal gives you 8500.00 exactly" |
> | `LocalDate expenseDate` | Date only, no time zone | "An expense happened on a day, not at a second — `LocalDate` is the right type" |
> | `status = ExpenseStatus.PENDING` | Default value in Java, before DB even touches it | "Every new expense is born PENDING — we never let a caller set the status directly" |
> | `category` (no `nullable = false`) | Nullable — AI fills this on Sunday | "We leave it null today. Week 5 AI will look at the title and set TRAVEL / FOOD / etc." |
> | `@ManyToOne` submittedBy | Foreign key to the user who filed it | "One employee can submit many expenses — many expenses, one user" |
> | `@ManyToOne` reviewedBy | Nullable — only filled when manager acts | "Until a manager approves or rejects, this stays null" |
> | `aiFraudFlags` | String — will hold JSON from Groq | "Placeholder for Week 5. AI will write something like `{\"duplicate\": true}` here" |
> | `createdAt = Instant.now()` | Timestamp auto-set in Java at object creation | "No need for `@CreatedDate` — the default value handles it" |

### DRAW

```
AppUser (1) ──────< Expense (many)
employee@...         Flight to Mumbai ₹8500 PENDING
manager@...          (reviewedBy when approved/rejected)
```

### JPA RELATIONSHIPS — deep dive (teach this after drawing)

#### The two relationships in Expense.java

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "submitted_by_id", nullable = false)
private AppUser submittedBy;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "reviewed_by_id")
private AppUser reviewedBy;
```

| Concept | Explanation | Say aloud |
|---------|-------------|-----------|
| `@ManyToOne` | Many expenses can belong to one user | "Read from right-to-left: one user HAS many expenses. From Expense's side it's Many-To-One." |
| `@JoinColumn(name = "submitted_by_id")` | Name of the FK column in the `expenses` table | "Without this, Hibernate picks an ugly name. Always name it explicitly." |
| `nullable = false` on submittedBy | Every expense must have a submitter — DB constraint | "You cannot insert an expense with no owner — the DB rejects it." |
| No `nullable = false` on reviewedBy | Manager hasn't acted yet | "This FK starts null and gets set when approve/reject is called." |
| `FetchType.LAZY` | Don't load the AppUser row until `.getSubmittedBy()` is called | "If you fetch 100 expenses and never touch `submittedBy`, no extra DB queries. Default for `@ManyToOne` in JPA 2+ is actually EAGER — always override with LAZY." |

#### FetchType — LAZY vs EAGER

```
FetchType.EAGER (avoid)             FetchType.LAZY (use this)
─────────────────────               ──────────────────────────
SELECT * FROM expenses              SELECT * FROM expenses
SELECT * FROM users WHERE id=1  ←   ← only runs if you call
SELECT * FROM users WHERE id=2       expense.getSubmittedBy()
SELECT * FROM users WHERE id=3
... (N+1 queries!)
```

**N+1 Problem** — this is a common interview question:  
> "If you fetch N expenses and each one EAGERly loads its user, you run 1 + N queries total."  
> LAZY loading prevents this — one query, load the rest on demand.

#### Why no `@OneToMany` on AppUser?

You *could* add this to `AppUser`:
```java
// optional — we deliberately skip it in this project
@OneToMany(mappedBy = "submittedBy")
private List<Expense> expenses;
```

We skip it because:
- We don't need to navigate from User → Expenses in code (we query through `ExpenseRepository` instead)
- Adding `@OneToMany` without `FetchType.LAZY` would load ALL expenses every time you load a user
- **Rule:** Only add the `@OneToMany` side if you need to traverse that direction in your code

#### `@JoinColumn` explained

```
expenses table (DB)
┌────┬────────┬──────────────────┬─────────────────┐
│ id │ title  │ submitted_by_id  │ reviewed_by_id  │
├────┼────────┼──────────────────┼─────────────────┤
│  1 │ Flight │       3          │      NULL       │  ← manager not acted
│  2 │ Hotel  │       3          │       5         │  ← manager (id=5) approved
└────┴────────┴──────────────────┴─────────────────┘
```
`submitted_by_id` IS the FK column → points to `users.id`.  
`@JoinColumn(name = "submitted_by_id")` just tells Hibernate the column name.

### RUN

Restart app → console shows `create table users...` and `create table expenses...`

### END THOUGHT

> "Tables exist. Topic 6 — repositories."

---

# TOPIC 6 — Repositories

### SAY

> "Same pattern as EMS — `JpaRepository` gives CRUD free.
> Custom finder methods for manager pending list and date-range queries."

### YOU DO

**FILE 1 — `repository/AppUserRepository.java`**

```java
package in.codekerdos.expense.repository;

import in.codekerdos.expense.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);
}
```

**FILE 2 — `repository/ExpenseRepository.java`**

```java
package in.codekerdos.expense.repository;

import in.codekerdos.expense.entity.AppUser;
import in.codekerdos.expense.entity.Expense;
import in.codekerdos.expense.enums.ExpenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findBySubmittedByOrderByCreatedAtDesc(AppUser submittedBy);

    List<Expense> findByStatusOrderByCreatedAtAsc(ExpenseStatus status);

    List<Expense> findByExpenseDateBetween(LocalDate from, LocalDate to);
}
```

> **CODE WALKTHROUGH — explain method naming as you type:**
>
> | Method | SQL it generates | Say aloud |
> |--------|-----------------|-----------|
> | `findByEmail(String email)` | `SELECT * FROM users WHERE email = ?` | "Spring reads the method name and writes the SQL for you. Capital B in `By` separates field name." |
> | `existsByEmail(String email)` | `SELECT COUNT(*) > 0 FROM users WHERE email = ?` | "Used in DataLoader to skip seeding if users already exist" |
> | `findBySubmittedByOrderByCreatedAtDesc(AppUser user)` | `SELECT * FROM expenses WHERE submitted_by_id = ? ORDER BY created_at DESC` | "Long method name — Spring parses every word. `OrderBy` + `Desc` = newest first." |
> | `findByStatusOrderByCreatedAtAsc(ExpenseStatus status)` | `SELECT * FROM expenses WHERE status = ? ORDER BY created_at ASC` | "Manager uses this to see all PENDING expenses, oldest first — fairness." |
> | `findByExpenseDateBetween(LocalDate from, LocalDate to)` | `SELECT * FROM expenses WHERE expense_date BETWEEN ? AND ?` | "Admin date-range report — no SQL written, just method naming convention." |

> "Zero SQL for basics. Topic 7 — DTOs."

### END THOUGHT

---

# TOPIC 7 — DTOs + Validation

### SAY

> "Never expose `@Entity` or password hash in JSON responses.
> Login gets a request DTO; login returns JWT — not the user entity."

### YOU DO

**FILE 1 — `dto/LoginRequest.java`**

```java
package in.codekerdos.expense.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {}
```

**FILE 2 — `dto/AuthResponse.java`**

```java
package in.codekerdos.expense.dto;

public record AuthResponse(
        String token,
        String email,
        String role,
        long expiresInMs
) {}
```

**FILE 3 — `dto/SubmitExpenseRequest.java`**

```java
package in.codekerdos.expense.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record SubmitExpenseRequest(
        @NotBlank(message = "Title is required") String title,
        String description,
        @NotNull @DecimalMin(value = "0.01", message = "Amount must be positive") BigDecimal amount,
        @NotNull LocalDate expenseDate
) {}
```

**FILE 4 — `dto/ExpenseResponse.java`**

```java
package in.codekerdos.expense.dto;

import in.codekerdos.expense.entity.Expense;
import in.codekerdos.expense.enums.ExpenseCategory;
import in.codekerdos.expense.enums.ExpenseStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        String title,
        String description,
        BigDecimal amount,
        LocalDate expenseDate,
        ExpenseStatus status,
        ExpenseCategory category,
        String submittedByEmail,
        String reviewedByEmail,
        String rejectionReason,
        String aiFraudFlags,
        Instant createdAt
) {
    public static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getTitle(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getExpenseDate(),
                expense.getStatus(),
                expense.getCategory(),
                expense.getSubmittedBy().getEmail(),
                expense.getReviewedBy() != null ? expense.getReviewedBy().getEmail() : null,
                expense.getRejectionReason(),
                expense.getAiFraudFlags(),
                expense.getCreatedAt()
        );
    }
}
```

> **CODE WALKTHROUGH — key teaching points:**
>
> **Why `record` instead of a normal class?**  
> A `record` auto-generates a constructor, getters, `equals`, `hashCode`, and `toString`.  
> For immutable data-transfer objects this saves 30+ lines. Say: *"We never modify a DTO after creating it — `record` is perfect."*
>
> | DTO | What it carries | Say aloud |
> |-----|----------------|-----------|
> | `LoginRequest` | Email + password coming IN | "`@Email` validates format before it even reaches your service — Spring fires a 400 automatically if it fails" |
> | `AuthResponse` | Token + metadata going OUT | "The client stores this token and sends it back on every future request" |
> | `SubmitExpenseRequest` | New expense data coming IN | "`@DecimalMin(\"0.01\")` means amount zero or negative is rejected before service runs" |
> | `ExpenseResponse` | Expense data going OUT | "We only send email strings, never the full AppUser object — hides password hash from JSON" |
>
> **The `from()` static factory:**  
> ```java
> expense.getReviewedBy() != null ? expense.getReviewedBy().getEmail() : null
> ```  
> The manager has not acted yet → `reviewedBy` is null → we send `null` in JSON.  
> Calling `.getEmail()` on null would throw a `NullPointerException` — the ternary guard is required.

> "API contract is clear. Topic 8 — business logic."

### END THOUGHT

---

# TOPIC 8 — ExpenseService (Submit + List Mine)

### SAY

> "Employee submits → status always starts PENDING.
> Employee can only see their own expenses — filter by logged-in user."

### YOU DO

**FILE 1 — `service/ResourceNotFoundException.java`**

```java
package in.codekerdos.expense.service;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

**FILE 2 — `service/ExpenseService.java`**

```java
package in.codekerdos.expense.service;

import in.codekerdos.expense.dto.ExpenseResponse;
import in.codekerdos.expense.dto.SubmitExpenseRequest;
import in.codekerdos.expense.entity.AppUser;
import in.codekerdos.expense.entity.Expense;
import in.codekerdos.expense.enums.ExpenseStatus;
import in.codekerdos.expense.repository.AppUserRepository;
import in.codekerdos.expense.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final AppUserRepository appUserRepository;

    public ExpenseService(ExpenseRepository expenseRepository,
                          AppUserRepository appUserRepository) {
        this.expenseRepository = expenseRepository;
        this.appUserRepository = appUserRepository;
    }

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

        return ExpenseResponse.from(expenseRepository.save(expense));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> findMine(String userEmail) {
        AppUser user = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return expenseRepository.findBySubmittedByOrderByCreatedAtDesc(user).stream()
                .map(ExpenseResponse::from)
                .toList();
    }
}
```

> **CODE WALKTHROUGH — explain each decision as you type:**
>
> **`ResourceNotFoundException`** — why a custom exception?  
> When a user or expense is not found, we throw this instead of returning null.  
> The `GlobalExceptionHandler` (Sunday) catches it and returns `404` JSON automatically.  
> Say: *"Never return null from a service. Throw a named exception — callers know exactly what went wrong."*
>
> **Constructor injection** (not `@Autowired` on fields):  
> ```java
> public ExpenseService(ExpenseRepository expenseRepository, AppUserRepository appUserRepository) { ... }
> ```  
> Say: *"Spring Boot 3 recommends constructor injection. Dependencies are visible, final, and testable."*
>
> **`submit()` method — walk through each line:**
>
> | Line | Why |
> |------|-----|
> | `findByEmail(userEmail).orElseThrow(...)` | We look up the real user from the DB — we never trust a raw String ID from the caller |
> | `expense.setStatus(ExpenseStatus.PENDING)` | We force PENDING here — caller cannot pass a different status |
> | `expense.setSubmittedBy(user)` | Links this expense to the authenticated user via FK in DB |
> | `ExpenseResponse.from(expenseRepository.save(...))` | Save returns the saved entity with generated ID; we immediately convert to DTO |
>
> **`@Transactional` vs `@Transactional(readOnly = true)`**  
> `submit()` writes → use `@Transactional` (opens a read-write transaction).  
> `findMine()` only reads → use `readOnly = true` (DB can optimize, no dirty-check overhead).  
> Say: *"readOnly is a performance hint — always use it on read-only methods."*
>
> **`findMine()` — why not just `findAll()`?**  
> ```java
> expenseRepository.findBySubmittedByOrderByCreatedAtDesc(user)
> ```  
> An employee must only see their own expenses.  
> `findAll()` would return everyone's data — a security leak.  
> We filter at the repository level so the DB does less work too.

> "Domain + submit logic ready — no security yet (Sunday). Topic 9 — wrap."

### END THOUGHT

---

# TOPIC 8B — `@Transactional` Deep Dive

> *Teach this right after ExpenseService. 8 minutes. High interview value.*

### SAY

> "Every database operation needs a transaction. `@Transactional` is Spring's way of saying:
> 'start a transaction before this method, commit when it succeeds, roll back if it throws'."

### DRAW — what a transaction is

```
@Transactional
submit() {
    ┌─── BEGIN TRANSACTION ──────────────────────────┐
    │  1. findByEmail → SELECT                        │
    │  2. new Expense()                               │
    │  3. expenseRepository.save → INSERT             │
    │                                                 │
    │  ✅ No exception? → COMMIT (data persisted)     │
    │  ❌ Exception?    → ROLLBACK (nothing saved)    │
    └─────────────────────────────────────────────────┘
}
```

### The four rules — say each, then move on

**Rule 1 — Write methods get `@Transactional`**
```java
@Transactional                // INSERT / UPDATE / DELETE
public ExpenseResponse submit(...) { ... }
```

**Rule 2 — Read methods get `@Transactional(readOnly = true)`**
```java
@Transactional(readOnly = true)   // SELECT only
public List<ExpenseResponse> findMine(...) { ... }
```

| `readOnly = true` benefit | Explanation |
|--------------------------|-------------|
| Database optimization | Some DBs (MySQL) skip dirty-check and flush on read-only |
| Hibernate optimization | Skips snapshot comparison for change detection |
| Routing support | DB connection pools can route to a read replica |

**Rule 3 — Rollback on unchecked exceptions**

```java
@Transactional
public ExpenseResponse submit(...) {
    AppUser user = appUserRepository.findByEmail(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    // ↑ throws RuntimeException → Spring catches it → ROLLBACK → no partial data in DB
    ...
}
```

> "If the user lookup fails after we already did some work, everything is rolled back automatically.
> No need to manually undo anything."

**Rule 4 — `@Transactional` does NOT work if you call the method from within the same class**

```java
// WRONG — self-invocation bypasses the proxy
public void doSomething() {
    this.submit(request, email);   // @Transactional ignored!
}

// CORRECT — inject the service and call from outside
expenseService.submit(request, email);  // proxy intercepts → transaction starts
```

> Say: *"Spring wraps your `@Service` bean in a proxy. The proxy starts the transaction before calling your method.
> If you call the method directly from inside the same class, you bypass the proxy — transaction doesn't start."*

### Interview Q&A

| Question | Answer |
|----------|--------|
| What does `@Transactional` do? | Wraps method in a DB transaction — commit on success, rollback on exception |
| When is rollback triggered? | Any unchecked exception (`RuntimeException`) by default |
| Why `readOnly = true`? | Tells DB/Hibernate this is a SELECT — enables optimizations |
| What is self-invocation problem? | Calling `@Transactional` method from same class bypasses Spring proxy |
| Where should `@Transactional` live? | Service layer — not controller, not repository |

---

# TOPIC 9 — Wrap + Homework

### SAY

> "Saturday = **foundation** — new project, enums, entities, ExpenseService.
> We intentionally skipped JWT today so we don't rush two hard topics in one class.
> **Sunday** = JWT + login + employee APIs. **Week 5** = manager workflow + AI."

### YOU DO — quick check (2 min)

Start app → no expense API exposed yet without Sunday's controller — that's OK.  
Show H2 console: `users` and `expenses` tables exist after you temporarily call service from a test or wait for Sunday.

### Homework

| # | Task |
|---|------|
| 1 | Finish Topics 3–8 if behind |
| 2 | Read [`Class-2.md`](Class-2.md) JWT architecture section before Sunday |
| 3 | Ensure `.env` has `GROQ_API_KEY` and `JWT_SECRET` ready |
| 4 | Push to GitHub — never commit `.env` |

### SAY — Sunday preview

> "Sunday Class 2 = **JWT**, login endpoint, seed users, `POST /api/expenses`, `GET /api/expenses/mine`.
> Week 5 Saturday = manager approve/reject. Week 5 Sunday = AI categorization + fraud."

### END THOUGHT

> "Solid domain day. Tomorrow we lock it down with JWT."

---

## QUICK REFERENCE — If you forget mid-class

| Problem | Fix |
|---------|-----|
| Groq not needed today | AI is Week 5 — only scaffold Spring AI dep in pom |
| Red imports | Maven → Reload |
| H2 console blank | JDBC URL = `jdbc:h2:mem:expensedb` |
| Tables not created | Check `@Entity` annotations + restart app |

---

## WEEK 4 CLASS 1 — Interview Quick Reference

| Question | Answer |
|----------|--------|
| What is `@Enumerated(STRING)`? | Stores enum name in DB — readable |
| Why BigDecimal for money? | Avoid floating-point rounding errors |
| PENDING on submit? | Every new expense starts awaiting manager approval |
| Three roles? | EMPLOYEE submit · MANAGER approve · ADMIN oversee |
| Why split across 2 weeks? | Domain + JWT + workflow + AI — too much for 2 classes |

---

*CodeKerdos.in · Week 4 Class 1 · Domain + scaffold — JWT on Sunday*
