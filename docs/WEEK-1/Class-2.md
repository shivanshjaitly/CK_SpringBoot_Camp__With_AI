# Week 1 · Class 2 — Sunday · Spring Boot + First Groq AI Call

> **[← Week 1 Index](README.md)** · **Previous ← [Class 1 — Saturday](Class-1.md)**  
> **Coding folder:** `week-01-employee-management`

---

## CLASS 2 — TOPICS (teach in this order)

| # | Topic Name |
|---|------------|
| 1 | Day 1 Recap |
| 2 | Spring Boot vs Plain Spring |
| 3 | Spring Initializr & Project Structure |
| 4 | application.yml & Configuration (+ Groq API key setup) |
| 5 | JPA Entities: Employee & Department |
| 6 | Repository & CRUD REST API |
| 7 | Spring AI + Groq Architecture |
| 8 | ChatClient: First LLM Call in 5 Lines |
| 9 | PromptTemplate: Dynamic AI Prompts |
| 10 | Full Demo + Push to GitHub |

---

**Session goal:** Spring Boot project running, Employee + Department entities with H2, basic CRUD, ChatClient connected to Groq, first PromptTemplate working.

**Time split:**

| Block | Duration | What |
|-------|----------|------|
| Recap + Groq signup | 10 min | Day 1 recap, Groq API key live |
| Theory block 1 | 20 min | Spring Boot magic — auto-config, starters |
| Live coding block 1 | 40 min | EMS project setup, entities, H2, CRUD |
| Theory block 2 | 15 min | Spring AI + Groq architecture |
| Live coding block 2 | 30 min | ChatClient + PromptTemplate + REST endpoint |
| Wrap + push | 5 min | Week 1 deliverables checklist |

**Project folder:** `week-01-employee-management`

---

## HOW TO RUN — VS Code / Cursor (do this BEFORE class)

> **You use VS Code or Cursor** — not terminal `mvn` (Maven may not be in PATH).  
> **Students:** same steps — Groq key in local `.env` file, never commit it.

### One-time setup

| Step | Action |
|------|--------|
| 1 | Open **VS Code / Cursor** → **File → Open Folder** → select `CK_SpringBoot_Camp__With_AI` (whole repo) |
| 2 | Install extensions when prompted (or install **Extension Pack for Java** + **Spring Boot Extension Pack**) |
| 3 | Wait for Maven import to finish (bottom-right progress bar) |
| 4 | Copy env file: in terminal inside `week-01-employee-management` run: `cp .env.example .env` |
| 5 | Open `week-01-employee-management/.env` → paste your Groq key after `GROQ_API_KEY=` |
| 6 | Save `.env` — this file is **gitignored** (never pushed to GitHub) |

### Every time you run the app

| Step | Action |
|------|--------|
| 1 | **Stop** any old run (red square top toolbar) |
| 2 | Press **F5** OR left sidebar **Run and Debug** (play icon) → select **"Week 1 EMS — Run with Groq"** → green **Run** |
| 3 | Wait for console: `Tomcat started on port 8080` |
| 4 | Test in Postman (see below) |

**Alternative:** Open `EmployeeManagementApplication.java` → click **Run** above `main()` — but **F5 / Run and Debug** is safer (loads `.env` automatically).

### Postman tests (after app is running)

```http
GET http://localhost:8080/api/ai/greet?name=Shivansh
```

```http
POST http://localhost:8080/api/departments
Content-Type: application/json

{ "name": "Engineering" }
```

```http
POST http://localhost:8080/api/employees
Content-Type: application/json

{
  "name": "Rahul Sharma",
  "role": "Senior Engineer",
  "team": "Backend",
  "joinedDate": "2023-06-15",
  "departmentId": 1
}
```

### Troubleshooting

| Problem | Fix |
|---------|-----|
| `401 Invalid API Key` | Check `week-01-employee-management/.env` has correct key; **restart** app with F5 |
| `500` on `/api/ai/greet` | Same as above — Groq key not loaded; use **Run and Debug**, not old Run button |
| `mvn: command not found` | **Ignore terminal mvn** — use VS Code F5 instead |
| Run config missing | Open repo root folder (not subfolder only); check `.vscode/launch.json` exists |
| Java project not found | Command Palette (`Cmd+Shift+P`) → **Java: Clean Java Language Server Workspace** → reload |

### Tell students in class

> "Create `.env` from `.env.example`, add your Groq key, run with F5. Never commit `.env`."

---

## TOPIC 1 — Day 1 Recap (5 min)

### Quick fire questions (ask class)

| Question | Expected answer |
|----------|-----------------|
| What is IoC? | Spring controls object creation |
| What is DI? | Spring injects dependencies into your classes |
| Preferred injection style? | Constructor injection |
| What is a bean? | Object managed by Spring container |
| What does `@Service` mean? | Business logic layer bean |
| Default bean scope? | Singleton |

### END THOUGHT

> "Spring Boot = Spring Core + auto-configuration + embedded server + opinionated defaults. Less config, more building."

---

## TOPIC 2 — Spring Boot vs Plain Spring

### Contents

| Plain Spring (Day 1) | Spring Boot (Today) |
|----------------------|---------------------|
| Manual `ApplicationContext` | Auto-created for you |
| Write lots of XML/Java config | `application.yml` + sensible defaults |
| Deploy WAR to external Tomcat | Embedded Tomcat — run `main()` |
| Add dependencies one by one | **Starters** bundle related deps |

### Explanation (layman)

> Day 1 = you built the car manually with Spring's help.
>
> Spring Boot = **Tesla**. Sit down, press start, it works. Spring Boot **guesses** what you need based on what's on the classpath.
>
> Add `spring-boot-starter-web` → Boot sees it → auto-configures Tomcat + Jackson JSON + Spring MVC. You didn't ask for any of that — Boot figured it out.

### @SpringBootApplication — three annotations in one

```java
@SpringBootApplication
// equals:
// @SpringBootConfiguration  → "I am a config class"
// @EnableAutoConfiguration  → "Spring Boot, auto-wire everything you detect"
// @ComponentScan             → "Scan this package and below for @Component"
public class EmployeeManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagementApplication.class, args);
    }
}
```

### DEMO

**F5** → Run **Week 1 EMS — Run with Groq** → show Tomcat starts on port 8080.

### END THOUGHT

> "Spring Boot doesn't replace Spring. It **sits on top** and removes boilerplate. Under the hood — still IoC, still DI, still beans."

---

## TOPIC 3 — Spring Initializr & Project Structure

### Contents

| File / Folder | Purpose |
|---------------|---------|
| `pom.xml` | Dependencies — now with `spring-boot-starter-parent` |
| `application.yml` | All config — DB, server port, AI keys |
| `src/main/java/.../controller/` | REST endpoints (HTTP layer) |
| `src/main/java/.../service/` | Business logic |
| `src/main/java/.../repository/` | Database access |
| `src/main/java/.../entity/` | JPA database tables as Java classes |
| `src/main/java/.../dto/` | Data Transfer Objects — API request/response shapes |

### Layered architecture (draw this)

```
HTTP Request
     ↓
Controller  ← "@RestController — receives JSON, returns JSON"
     ↓
Service     ← "@Service — business rules live here"
     ↓
Repository  ← "JpaRepository — talks to database"
     ↓
Database (H2 / MySQL)
```

**Layman:** Controller = receptionist. Service = manager. Repository = filing cabinet.

### Dependencies we added (show pom.xml)

```xml
spring-boot-starter-web          <!-- REST APIs + Tomcat -->
spring-boot-starter-data-jpa     <!-- Database ORM -->
h2                               <!-- In-memory DB for today -->
spring-boot-starter-validation   <!-- @NotNull, @Email etc -->
spring-ai-openai-spring-boot-starter  <!-- LLM — Groq compatible -->
```

### DEMO

Walk through folder structure in `week-01-employee-management`.

### END THOUGHT

> "This layered structure appears in **every** enterprise Spring project. Learn it once."

---

## TOPIC 4 — application.yml & Configuration

### Contents

| Topic | Detail |
|-------|--------|
| `.properties` vs `.yml` | YAML is hierarchical, cleaner for nested config |
| Profiles | `application-dev.yml`, `application-prod.yml` (Week 5) |
| Env variables | `${GROQ_API_KEY}` — secrets never hardcoded |

### application.yml (explain each section)

```yaml
server:
  port: 8080

spring:
  application:
    name: employee-management

  # H2 in-memory database — no MySQL install needed today
  datasource:
    url: jdbc:h2:mem:emsdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  h2:
    console:
      enabled: true          # Browser UI at /h2-console
      path: /h2-console

  jpa:
    hibernate:
      ddl-auto: update       # Auto-create tables from @Entity classes
    show-sql: true           # Print SQL in console — great for learning

  ai:
    openai:
      api-key: ${GROQ_API_KEY}           # From environment variable
      base-url: https://api.groq.com/openai   # Groq speaks OpenAI format
      chat:
        options:
          model: llama-3.3-70b-versatile
```

### Groq setup (do this LIVE with class — 5 min)

**Step 1 — Get key**

1. Go to [https://console.groq.com](https://console.groq.com)
2. Sign up (free, no credit card)
3. **API Keys → Create API Key** → copy (starts with `gsk_`)

**Step 2 — VS Code / Cursor (recommended for class)**

1. In project folder `week-01-employee-management`:
   ```bash
   cp .env.example .env
   ```
2. Open `.env` → set:
   ```
   GROQ_API_KEY=gsk_your_key_here
   ```
3. Save — **never commit `.env`**

**Step 3 — Run app**

1. **F5** (or Run and Debug → **Week 1 EMS — Run with Groq**)
2. Wait for `Tomcat started on port 8080`

**Why not paste key in `application.yml`?**

```yaml
# ✅ CORRECT (in application.yml — committed to Git)
api-key: ${GROQ_API_KEY}

# ❌ WRONG
api-key: ${gsk_abc123...}   # tries to find env var named gsk_abc...
api-key: gsk_abc123...      # works locally but leaks if you push to GitHub
```

**IntelliJ users (optional):** Run → Edit Configurations → Environment Variables → `GROQ_API_KEY=gsk_...`

### DEMO

1. Confirm `.env` file exists with key
2. **F5** → start app
3. Postman: `GET http://localhost:8080/api/ai/greet?name=Shivansh` → should return AI message (not 500)
4. H2 console: `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:mem:emsdb`
   - User: `sa`, Password: (empty)

### END THOUGHT

> "Config outside code = 12-factor app principle. Keys in env vars, not in source files."

---

## TOPIC 5 — JPA Entities: Employee & Department

### Contents

| Annotation | Purpose |
|------------|---------|
| `@Entity` | This Java class = database table |
| `@Table(name="...")` | Custom table name |
| `@Id` | Primary key |
| `@GeneratedValue` | Auto-increment ID |
| `@Column` | Column name, nullable, length |
| `@OneToMany` | One department → many employees |
| `@ManyToOne` | Many employees → one department |

### Explanation (layman)

> **JPA** = Java Persistence API. Write Java classes, Hibernate creates SQL tables automatically.
>
> You don't write `CREATE TABLE` — you write `@Entity class Employee` and Spring creates the table.

### Code

**Department.java:**

```java
@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private List<Employee> employees = new ArrayList<>();
}
```

**Employee.java:**

```java
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String role;       // e.g. "Senior Engineer"
    private String team;       // e.g. "Backend"
    private LocalDate joinedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
}
```

**Relationship layman:**

> One **Department** (Engineering) has many **Employees**. Each employee belongs to one department. Like one school has many students.

### DEMO

1. Start app → watch console SQL (`create table employees...`)
2. Open H2 console → `SELECT * FROM DEPARTMENTS;`

### END THOUGHT

> "Week 2 we switch H2 → MySQL. The entity code stays **exactly the same** — that's the power of JPA."

---

## TOPIC 6 — Repository & CRUD REST API

### Contents

| Layer | Class | Responsibility |
|-------|-------|----------------|
| Repository | `EmployeeRepository extends JpaRepository` | DB queries — free CRUD from Spring |
| Service | `EmployeeService` | Business logic, validation |
| Controller | `EmployeeController` | HTTP endpoints |

### Repository (zero SQL needed)

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByTeam(String team);
    List<Employee> findByRoleContainingIgnoreCase(String role);
    List<Employee> findByJoinedDateAfter(LocalDate date);
}
```

> Spring Data JPA **generates SQL from method names**. `findByTeam` → `SELECT * FROM employees WHERE team = ?`

### REST Controller

```java
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public List<EmployeeResponse> getAll() { ... }

    @GetMapping("/{id}")
    public EmployeeResponse getById(@PathVariable Long id) { ... }

    @PostMapping
    public EmployeeResponse create(@RequestBody @Valid CreateEmployeeRequest request) { ... }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { ... }
}
```

### HTTP methods (table on board)

| Method | URL | Action |
|--------|-----|--------|
| GET | `/api/employees` | List all |
| GET | `/api/employees/1` | Get one |
| POST | `/api/employees` | Create |
| DELETE | `/api/employees/1` | Delete |

### DEMO (Postman live)

**Create employee:**

```http
POST http://localhost:8080/api/employees
Content-Type: application/json

{
  "name": "Rahul Sharma",
  "role": "Senior Engineer",
  "team": "Backend",
  "joinedDate": "2023-06-15",
  "departmentId": 1
}
```

**List all:**

```http
GET http://localhost:8080/api/employees
```

### END THOUGHT

> "CRUD is the skeleton. Week 2 we add pagination, sorting, Spring Security login, and **AI natural language search**."

---

## TOPIC 7 — Spring AI + Groq Architecture

### Contents

| Component | Role |
|-----------|------|
| **Groq** | Cloud LLM provider — hosts Llama, Mixtral models, free tier |
| **Spring AI** | Spring's official LLM library — same DI patterns |
| **ChatClient** | Main interface to talk to any LLM |
| **PromptTemplate** | Prompts with `{variables}` — reusable templates |

### Explanation (layman)

> **Groq** = a super-fast brain in the cloud. You send text, it sends text back. Free.
>
> **Spring AI** = a translator between your Java code and that brain. You inject `ChatClient` just like you inject `EmployeeService`.
>
> **Groq uses OpenAI-compatible API** — so Spring AI's OpenAI starter works with a different `base-url`. No Python. Pure Java.

### Architecture diagram (draw)

```
Your REST API (/api/ai/onboarding-checklist)
        ↓
  AiOnboardingService  (@Service)
        ↓
    ChatClient  (Spring AI — injected)
        ↓
  HTTP POST → https://api.groq.com/openai/v1/chat/completions
        ↓
  Groq runs Llama 3.3 model
        ↓
  JSON response → plain text back to your API
        ↓
  Student sees AI-generated checklist in Postman
```

### Why Groq for bootcamp?

| Reason | Detail |
|--------|--------|
| Free | No credit card |
| Fast | Fastest inference — good for live demos |
| OpenAI-compatible | Works with Spring AI OpenAI starter |
| Good models | Llama 3.3 70B — strong for code + text |

### END THOUGHT

> "Same Spring patterns: interface → inject → use. ChatClient is just another bean."

---

## TOPIC 8 — ChatClient: First LLM Call in 5 Lines

### Contents

| Step | What |
|------|------|
| 1 | Add Spring AI OpenAI starter to pom.xml |
| 2 | Configure Groq in application.yml |
| 3 | Set GROQ_API_KEY env var |
| 4 | Inject ChatClient in a @Service |
| 5 | Expose via @RestController |

### Code — AiGreetingService.java

```java
@Service
public class AiGreetingService {

    private final ChatClient chatClient;

    public AiGreetingService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String greet(String name) {
        return chatClient
            .prompt()
            .user("Say a warm one-line welcome to " + name +
                  " who just joined CodeKerdos Spring Boot + AI Bootcamp. Keep it under 20 words.")
            .call()
            .content();
    }
}
```

**That's it. 5 lines of AI logic.**

### Code — AiController.java

```java
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiGreetingService aiGreetingService;
    private final AiOnboardingService aiOnboardingService;

    @GetMapping("/greet")
    public Map<String, String> greet(@RequestParam(defaultValue = "Student") String name) {
        String message = aiGreetingService.greet(name);
        return Map.of("name", name, "aiMessage", message);
    }

    @PostMapping("/onboarding-checklist")
    public Map<String, String> onboardingChecklist(@RequestBody OnboardingRequest request) {
        String checklist = aiOnboardingService.generateChecklist(request);
        return Map.of("employee", request.getName(), "checklist", checklist);
    }
}
```

### DEMO (the wow moment)

```http
GET http://localhost:8080/api/ai/greet?name=Shivansh
```

**Expected response:**

```json
{
  "name": "Shivansh",
  "aiMessage": "Welcome Shivansh! Excited to have you building AI-powered apps with us!"
}
```

> 🎉 Class reaction moment — live AI from Java, no Python, 5 lines.

### END THOUGHT

> "You just called an LLM from a Spring Boot REST API. Most CS graduates can't do this. You can — on Day 2."

---

## TOPIC 9 — PromptTemplate: Dynamic AI Prompts

### Contents

| Concept | Detail |
|---------|--------|
| **PromptTemplate** | String template with `{placeholders}` |
| **Variable injection** | Pass Java values into prompt at runtime |
| **Why not string concat?** | Reusable, testable, readable prompts |

### Explanation (layman)

> Hardcoding prompts is like hardcoding SQL with string concat — messy.
>
> PromptTemplate = `String.format()` for AI. Write the prompt once, inject different employee names every time.

### Code — AiOnboardingService.java

```java
@Service
public class AiOnboardingService {

    private final ChatClient chatClient;

    private static final PromptTemplate ONBOARDING_TEMPLATE = new PromptTemplate("""
        You are an HR assistant at a tech company.
        Generate a concise onboarding checklist (5 bullet points) for:
        - Name: {name}
        - Role: {role}
        - Department: {department}
        - Team: {team}

        Include: tools to set up, people to meet, first-week goals.
        Format as plain text bullet points.
        """);

    public String generateChecklist(OnboardingRequest req) {
        Prompt prompt = ONBOARDING_TEMPLATE.create(Map.of(
            "name", req.getName(),
            "role", req.getRole(),
            "department", req.getDepartment(),
            "team", req.getTeam()
        ));

        return chatClient.prompt(prompt).call().content();
    }
}
```

### DEMO (Postman)

```http
POST http://localhost:8080/api/ai/onboarding-checklist
Content-Type: application/json

{
  "name": "Priya Patel",
  "role": "Junior Developer",
  "department": "Engineering",
  "team": "Frontend",
  "joinedDate": "2025-01-10"
}
```

**Expected:** 5-bullet onboarding checklist tailored to Priya.

### END THOUGHT

> "PromptTemplate is Week 1 AI. Week 2 we add **structured JSON output** for natural language employee search. Week 5 — full **RAG pipeline**."

---

## DAY 2 — Full Demo Flow (end-to-end checklist)

Run this sequence live:

```
✅ 1.  Set GROQ_API_KEY in IntelliJ run config
✅ 2.  Start EmployeeManagementApplication
✅ 3.  POST /api/departments → create "Engineering" department
✅ 4.  POST /api/employees   → create 2-3 employees
✅ 5.  GET  /api/employees   → list all
✅ 6.  GET  /api/ai/greet?name=YourName → first AI call
✅ 7.  POST /api/ai/onboarding-checklist → PromptTemplate demo
✅ 8.  H2 console → show data in tables
✅ 9.  git add → commit → push
```

**Create department (Postman):**

```http
POST http://localhost:8080/api/departments
Content-Type: application/json

{ "name": "Engineering" }
```

---

## WEEK 1 — Deliverables Checklist

| # | Deliverable | Status |
|---|-------------|--------|
| 1 | Plain Spring IoC/DI demo working | Day 1 |
| 2 | Spring Boot EMS project running | Day 2 |
| 3 | Employee + Department entities + H2 | Day 2 |
| 4 | CRUD REST APIs tested in Postman | Day 2 |
| 5 | ChatClient connected to Groq | Day 2 |
| 6 | PromptTemplate onboarding checklist | Day 2 |
| 7 | Code pushed to GitHub | Both days |

---

## WEEK 1 — Common Issues & Fixes (keep this open during class)

| Problem | Fix |
|---------|-----|
| `GROQ_API_KEY` not set | Add to IntelliJ Run Config → Environment Variables |
| 401 from Groq | Key wrong or expired — regenerate at console.groq.com |
| 429 rate limit | Wait 30 sec; Groq free tier has limits |
| H2 console won't connect | JDBC URL must be exactly `jdbc:h2:mem:emsdb` |
| Port 8080 in use | Kill other app or set `server.port=8081` |
| Maven dependencies not downloading | Check internet; reload Maven in IntelliJ |
| `ChatClient` bean not found | Ensure `spring-ai-openai-spring-boot-starter` in pom.xml |

---

## WEEK 1 — Interview Quick Reference (rapid fire at end)

| Question | Answer |
|----------|--------|
| What is IoC? | Inversion of Control — Spring manages object lifecycle |
| What is DI? | Dependencies provided by container, not created with `new` |
| Constructor vs Field injection? | Constructor — mandatory, testable, immutable |
| What is `@SpringBootApplication`? | `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan` |
| What is JPA? | Java Persistence API — ORM mapping objects to DB tables |
| What is `JpaRepository`? | Spring Data interface — free CRUD + query methods |
| What is Groq? | Free LLM API provider, OpenAI-compatible, ultra-fast |
| What is Spring AI ChatClient? | Unified interface to call any LLM from Spring |
| What is PromptTemplate? | Reusable prompt with `{variable}` placeholders |
| Why env vars for API keys? | Security — never commit secrets to Git |

---

## WEEK 2 PREVIEW (tease at end of Day 2)

> "Week 2 Saturday — full REST API design, `@RestController` deep dive, Spring Security login page.
> Week 2 Sunday — switch to **MySQL**, pagination, and the killer feature: **Natural Language HR Search** — type 'show me senior backend engineers who joined after 2022' and Groq finds them.
> See you next Saturday."


---

*CodeKerdos.in · Week 1 Class 2 · Push code after session*
