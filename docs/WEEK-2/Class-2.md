# Week 2 · Class 2 — Sunday · MySQL + Pagination + NL Search + Login Page

> **[← Week 2 Index](README.md)** · **Previous ← [Class 1 — Saturday](Class-1.md)**  
> **Coding folder:** `week-02-employee-management`

---

## CLASS 2 — TOPICS (teach in this order)

| # | Topic Name | Code? |
|---|------------|-------|
| 1 | Saturday recap + secured demo | Run |
| 2 | AI pipeline deep-dive (NL search design) | Talk + draw |
| 3 | Local MySQL setup + new connection | Talk + terminal |
| 4 | Switch application.yml to MySQL profile | Code |
| 5 | JPA portability — same entities, new DB | Run |
| 6 | Pagination & sorting — Pageable | Code |
| 7 | Filtering — query params | Code |
| 8 | NL HR Search — design + criteria DTO | Talk + Code |
| 9 | AiEmployeeSearchService + REST endpoint | Code |
| 10 | Form login + Thymeleaf `/login` page | Code |
| 11 | Full demo + Week 3 preview | Wrap |

**Session goal:** EMS on MySQL, paginated APIs, natural language employee search via Groq, browser login page. **Project #1 complete.**

**Time split:**

| Block | Duration | What |
|-------|----------|------|
| Recap + AI pipeline | 15 min | Topics 1, 2 |
| MySQL setup + switch | 25 min | Topics 3–5 |
| Pagination + filtering | 25 min | Topics 6, 7 |
| NL search (star feature) | 25 min | Topics 8, 9 |
| Form login page | 20 min | Topic 10 |
| Wrap | 5 min | Topic 11 |

---

## HOW TO RUN — VS Code / Cursor

### Saturday profile (H2) — default

**F5** → **Week 2 EMS — Run with Groq (H2)**

### Sunday profile (MySQL)

| Step | Action |
|------|--------|
| 1 | Complete MySQL setup (Topic 3) |
| 2 | **F5** → **Week 2 EMS — Run with MySQL** |
| 3 | Console: `Tomcat started on port 8080` |
| 4 | Browser: `http://localhost:8080/login` |
| 5 | Postman: Basic Auth OR session cookie after browser login |

---

# TOPIC 1 — Saturday Recap (5 min)

### Quick fire questions

| Question | Expected answer |
|----------|-----------------|
| Why DTOs? | Clean API contract, hide entity internals |
| What protects our APIs? | Spring Security — HTTP Basic |
| Demo credentials? | hr@codekerdos.in / hr123 |
| Two AI endpoints so far? | greet + onboarding-checklist |
| What's today's killer feature? | Natural language employee search |

### RUN — 2 min secured demo

Postman with Basic Auth:
- `GET /api/employees`
- `GET /api/ai/greet?name=Test`

### END THOUGHT

> "Today we make EMS production-shaped: real DB, pagination, AI search, login page."

---

# TOPIC 2 — AI Pipeline Deep-Dive (NL Search Design)

### SAY

> "Sunday's AI is **not** chat for fun — it **translates English into database filters**.
> User types: *'show senior backend engineers who joined after 2022'*
> Groq returns JSON → Java parses it → JPA queries the database → paginated results."

### DRAW — full pipeline

```
POST /api/ai/search-employees
        ↓
  { "query": "senior backend engineers joined after 2022" }
        ↓
  AiEmployeeSearchService
        ↓
  ChatClient → Groq → JSON criteria:
  {
    "roleContains": "Senior",
    "team": "Backend",
    "joinedAfter": "2022-01-01"
  }
        ↓
  EmployeeSpecifications → JPA query
        ↓
  Paginated EmployeeResponse list
```

### Contents

| Step | Technology |
|------|------------|
| User input | Natural language string |
| LLM job | Extract structured filters only — JSON, no prose |
| Java job | Parse JSON → `EmployeeSearchCriteria` → Specification |
| DB job | Dynamic WHERE clause via JPA |

### SAY — crisp AI answer for students

> "We use AI as a **translator** between human language and database queries.
> We don't let AI touch the database directly — **we** run the query. Safer, testable, interview-friendly."

### END THOUGHT

> "Topic 3 — set up MySQL locally so results persist."

---

# TOPIC 3 — Local MySQL Setup + New Connection

### SAY

> "H2 = great for learning, data vanishes on restart.
> MySQL = real database, same JPA entities — **zero entity code changes**."

### YOU DO — Step by step (live with class)

**Step 1 — Verify MySQL is installed**

```bash
mysql --version
```

| OS | If not installed |
|----|------------------|
| macOS | `brew install mysql` then `brew services start mysql` |
| Windows | Download MySQL Installer from dev.mysql.com |
| Linux | `sudo apt install mysql-server` |

**Step 2 — Open MySQL shell**

```bash
mysql -u root -p
```

**Step 3 — Create database and user**

```sql
CREATE DATABASE ems_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'ems_user'@'localhost' IDENTIFIED BY 'ems_pass';
GRANT ALL PRIVILEGES ON ems_db.* TO 'ems_user'@'localhost';
FLUSH PRIVILEGES;

EXIT;
```

**Step 4 — Test connection**

```bash
mysql -u ems_user -p ems_db
```

Enter `ems_pass` → you should see `mysql>` prompt. Type `SHOW TABLES;` (empty for now).

### Create a new connection (GUI tools)

**MySQL Workbench:**

1. Open MySQL Workbench
2. Click **+** next to "MySQL Connections"
3. Connection Name: `EMS Local`
4. Hostname: `127.0.0.1` · Port: `3306`
5. Username: `ems_user` · Click **Store in Keychain/Vault**
6. Click **Test Connection** → enter `ems_pass` → **OK**

**VS Code / Cursor Database:**

1. Install **Database Client** or use built-in if available
2. Add connection → MySQL
3. Host: `localhost`, Port: `3306`, User: `ems_user`, Password: `ems_pass`, Database: `ems_db`
4. Test → Connect

**IntelliJ Database panel:**

1. View → Tool Windows → Database
2. **+** → Data Source → MySQL
3. Same settings → **Test Connection** → Apply

### STUCK?

| Problem | Fix |
|---------|-----|
| Access denied | Re-run GRANT or check password |
| Can't connect | `brew services list` — ensure mysql is started |
| Port 3306 in use | Another MySQL instance running — stop it |

### END THOUGHT

> "Database exists. Topic 4 — point Spring Boot at MySQL."

---

# TOPIC 4 — Switch to MySQL Profile

### SAY

> "We use **Spring profiles** — `h2` for Saturday, `mysql` for Sunday.
> Same `application.yml`, different datasource block."

### YOU DO

**Update `application.yml`** — add mysql profile block (already in repo):

```yaml
---
spring:
  config:
    activate:
      on-profile: mysql
  datasource:
    url: jdbc:mysql://localhost:3306/ems_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
    username: ems_user
    password: ems_pass
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
```

**Run with MySQL profile:**

- VS Code F5 → **Week 2 EMS — Run with MySQL**
- Or set env: `SPRING_PROFILES_ACTIVE=mysql`

> Say: *"`mysql-connector-j` was already in pom.xml from Saturday."*

### RUN

Console shows:
- `HikariPool-1 - Starting...`
- Hibernate `create table departments...` in MySQL dialect
- `Tomcat started on port 8080`

Verify in MySQL Workbench: `SHOW TABLES;` → `departments`, `employees`

### END THOUGHT

> "Same Java, real database. Topic 5 — prove portability."

---

# TOPIC 5 — JPA Portability Demo

### SAY

> "We changed **zero** entity code. Only config changed.
> That's why enterprises use JPA — swap H2 → MySQL → PostgreSQL with config only."

### DEMO

1. POST department + 3 employees via Postman (Basic Auth)
2. MySQL Workbench: `SELECT * FROM employees;`
3. Restart app → data **still there** (unlike H2 mem)

### END THOUGHT

> "Data persists. Topic 6 — pagination for large employee lists."

---

# TOPIC 6 — Pagination & Sorting

### SAY

> "Never return 10,000 employees in one JSON array.
> **Pageable** = `page`, `size`, `sort` — standard REST pattern."

### YOU DO

**Update `EmployeeRepository`** — add later for NL search:

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
```

**Create `dto/PagedEmployeeResponse.java`:**

```java
public record PagedEmployeeResponse(
        List<EmployeeResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
```

**Update `EmployeeService.findAllPaged(page, size, sort, team)`**

**Update `EmployeeController`:**

```java
@GetMapping
public PagedEmployeeResponse getAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "joinedDate,desc") String sort,
        @RequestParam(required = false) String team
) {
    return employeeService.findAllPaged(page, size, sort, team);
}
```

### RUN

```http
GET http://localhost:8080/api/employees?page=0&size=2&sort=joinedDate,desc
Authorization: Basic ...
```

Expected JSON:

```json
{
  "content": [ ... ],
  "page": 0,
  "size": 2,
  "totalElements": 5,
  "totalPages": 3
}
```

### END THOUGHT

> "Pagination works. Topic 7 — optional team filter."

---

# TOPIC 7 — Filtering via Query Params

### SAY

> "Pagination + filter = `?team=Backend&page=0&size=10`.
> We use JPA **Specification** for dynamic filters (needed for AI search too)."

### YOU DO

**Create `specification/EmployeeSpecifications.java`** with `hasTeam(String team)` method.

**Wire into `EmployeeService.findAllPaged`** — if `team` param present, use specification.

### RUN

```http
GET http://localhost:8080/api/employees?team=Backend&page=0&size=10
```

### END THOUGHT

> "Manual filters work. Topic 8–9 — AI generates the same filters from English."

---

# TOPIC 8 — NL HR Search: Design + Criteria DTO

### SAY

> "We ask Groq to return **JSON only** — no markdown, no explanation.
> Java parses JSON into `EmployeeSearchCriteria` — a typed record."

### YOU DO

**Create DTOs:**

```java
public record EmployeeSearchCriteria(
        String roleContains,
        String team,
        String departmentName,
        LocalDate joinedAfter,
        LocalDate joinedBefore
) {}

public record NaturalLanguageSearchRequest(
        @NotBlank String query,
        Integer page,
        Integer size
) {}

public record NaturalLanguageSearchResponse(
        String query,
        EmployeeSearchCriteria parsedCriteria,
        PagedEmployeeResponse results
) {}
```

**Extend `EmployeeSpecifications.fromCriteria(criteria)`** — build dynamic predicates for all fields.

### DRAW

```
"senior backend in Engineering after 2022"
        ↓ Groq
{ roleContains: "Senior", team: "Backend", departmentName: "Engineering", joinedAfter: "2022-01-01" }
        ↓ Java Specification
SELECT ... WHERE role LIKE '%Senior%' AND team = 'Backend' AND ...
```

### END THOUGHT

> "Design is clear. Topic 9 — wire Groq."

---

# TOPIC 9 — AiEmployeeSearchService + REST Endpoint

### YOU DO

**FILE — `service/ai/AiEmployeeSearchService.java`**

Key logic:
1. Send prompt asking for JSON criteria only
2. Strip markdown fences from LLM response
3. Parse with `ObjectMapper` + `JavaTimeModule`
4. Call `employeeService.searchByCriteria(criteria, page, size)`

**Add to `AiController`:**

```java
@PostMapping("/search-employees")
public NaturalLanguageSearchResponse searchEmployees(
        @RequestBody @Valid NaturalLanguageSearchRequest request) {
    return aiEmployeeSearchService.search(request);
}
```

### RUN — the wow moment 🎉

First seed data — 5+ employees with different roles, teams, dates.

```http
POST http://localhost:8080/api/ai/search-employees
Authorization: Basic hr@codekerdos.in / hr123
Content-Type: application/json

{
  "query": "show senior backend engineers who joined after 2022",
  "page": 0,
  "size": 10
}
```

**Expected response:**

```json
{
  "query": "show senior backend engineers who joined after 2022",
  "parsedCriteria": {
    "roleContains": "Senior",
    "team": "Backend",
    "departmentName": null,
    "joinedAfter": "2022-01-01",
    "joinedBefore": null
  },
  "results": {
    "content": [ ... matching employees ... ],
    "page": 0,
    "size": 10,
    "totalElements": 2,
    "totalPages": 1
  }
}
```

> Point at `parsedCriteria` — *"AI did translation. Java did the query. This is production-safe AI."*

### STUCK?

| Problem | Fix |
|---------|-----|
| AI returns prose not JSON | Tighten prompt: "Return ONLY valid JSON" |
| Parse error | Log raw response; strip ```json fences |
| Empty results | Criteria too strict — test with broader query |

### END THOUGHT

> "Killer feature done. Topic 10 — login page for HR portal feel."

---

# TOPIC 10 — Form Login + Thymeleaf `/login` Page (~20 min)

### SAY

> "HTTP Basic works in Postman. HR managers expect a **browser login page**.
> **Thymeleaf** = server-side HTML templates in Spring Boot.
> We keep Basic Auth **and** add form login — both work."

### YOU DO

**Step 1 — Add dependencies to pom.xml**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

**Step 2 — Update SecurityConfig**

Add form login alongside httpBasic:

```java
.formLogin(form -> form
        .loginPage("/login")
        .defaultSuccessUrl("/", true)
        .permitAll()
)
.logout(logout -> logout
        .logoutSuccessUrl("/login?logout")
        .permitAll()
)
```

Permit `/login` and `/css/**` without auth.

**Step 3 — PageController**

```java
@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
```

**Step 4 — Templates**

Create:
- `src/main/resources/templates/login.html` — email + password form
- `src/main/resources/templates/home.html` — welcome + API list + logout
- `src/main/resources/static/css/style.css` — simple styling

Demo credentials on login page: `hr@codekerdos.in` / `hr123`

### RUN

1. Browser → `http://localhost:8080/` → redirects to `/login`
2. Login → home page shows logged-in user
3. Postman still works with Basic Auth (both auth methods active)

### END THOUGHT

> "EMS has a face and a lock. Topic 11 — full demo and Project #1 wrap."

---

# TOPIC 11 — Full Demo + Week 3 Preview

### End-to-end checklist (run live)

```
✅ 1.  MySQL running — ems_db exists
✅ 2.  F5 → Week 2 EMS — Run with MySQL
✅ 3.  Browser login at /login
✅ 4.  POST /api/departments + POST /api/employees (seed 5+ records)
✅ 5.  GET /api/employees?page=0&size=3&sort=joinedDate,desc
✅ 6.  GET /api/employees?team=Backend
✅ 7.  POST /api/ai/search-employees  →  NL query wow moment
✅ 8.  GET /api/ai/greet?name=YourName
✅ 9.  MySQL Workbench → SELECT * FROM employees
✅ 10. git add → commit → push (no .env)
```

### WEEK 2 — Deliverables (Project #1 COMPLETE)

| # | Deliverable |
|---|-------------|
| 1 | `week-02-employee-management` on MySQL |
| 2 | CRUD + validation + exception handling |
| 3 | HTTP Basic + form login page |
| 4 | Paginated + filtered employee list |
| 5 | Natural language HR search via Groq |
| 6 | Code on GitHub |

### SAY — Week 3 preview

> "EMS = **Project #1 — done**. One of three portfolio projects.
> Week 3 = **Security lecture** — CSRF, filter chain, `SecurityConfig`. Same EMS codebase.
> Week 4–5 = **Expense Approval** — Project #2 (JWT, workflow, AI). See you Saturday."

### END THOUGHT

> "You built a full AI-powered HR system in Java. Most bootcamps stop at CRUD. You have NL search."

---

## QUICK REFERENCE — Common Issues

| Problem | Fix |
|---------|-----|
| MySQL connection refused | Start MySQL service |
| Unknown database ems_db | Run CREATE DATABASE SQL |
| Wrong profile (still H2) | Use **Run with MySQL** launch config |
| 401 in Postman | Basic Auth: hr@codekerdos.in / hr123 |
| Login page 404 | Check templates/login.html exists |
| NL search parse error | Check Groq response in logs; tighten prompt |
| Empty paginated list | Seed employees first |

---

## WEEK 2 — Interview Quick Reference

| Question | Answer |
|----------|--------|
| H2 vs MySQL? | H2 = in-memory dev; MySQL = persistent production DB |
| What is Pageable? | Spring abstraction for page/size/sort |
| What is JpaSpecificationExecutor? | Dynamic query builder for flexible filters |
| How does NL search work? | LLM → JSON criteria → JPA Specification → DB |
| Why not let AI query DB directly? | Security + testability — Java controls the query |
| HTTP Basic vs form login? | Basic = API/Postman; form = browser session |
| What is Thymeleaf? | Server-side HTML template engine for Spring |
| How many projects after Week 2? | **1 complete** (EMS). 2 more coming. |

---

*CodeKerdos.in · Week 2 Class 2 · Project #1 complete — push code after session*
