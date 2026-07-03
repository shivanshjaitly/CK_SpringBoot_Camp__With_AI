# Week 2 · Class 1 — Saturday · EMS Core + First AI + Security

> **[← Week 2 Index](README.md)** · **Next → [Class 2 — Sunday](Class-2.md)**  
> **Coding folder:** `week-02-employee-management` (NEW folder — continues EMS from Week 1 checkpoint)

---

## HOW TO USE THIS FILE (read once)

You teach **Topic 1 → 14 in order**.  
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
| 1 | Week 1 recap + where we stopped | No — talk only |
| 2 | **AI in this bootcamp (crisp)** | No — board only |
| 3 | New folder + layered architecture | ✅ **Scaffold project** |
| 4 | JPA entities — Department & Employee | ✅ **entity/** |
| 5 | Repositories | ✅ **repository/** |
| 6 | DTOs + validation | ✅ **dto/** |
| 7 | Service layer | ✅ **service/** |
| 8 | CRUD REST APIs | ✅ **controller/** + Postman |
| 9 | Spring AI + Groq architecture | No — talk only |
| 10 | ChatClient — first AI call | ✅ **service/ai/** + AiController |
| 11 | PromptTemplate — onboarding checklist | ✅ **AiOnboardingService** |
| 12 | REST polish — GlobalExceptionHandler | ✅ **exception/** |
| 13 | Spring Security — HTTP Basic | ✅ **config/SecurityConfig** |
| 14 | Full demo + homework | ✅ **Run everything** |

---

## FILES YOU WILL CREATE (final picture — Class 1 end state)

```
week-02-employee-management/
├── pom.xml
├── .env.example
└── src/main/
    ├── resources/application.yml
    └── java/in/codekerdos/ems/
        ├── EmployeeManagementApplication.java
        ├── entity/Department.java, Employee.java
        ├── repository/DepartmentRepository.java, EmployeeRepository.java
        ├── dto/Create*Request.java, *Response.java, OnboardingRequest.java
        ├── service/DepartmentService.java, EmployeeService.java, ResourceNotFoundException.java
        ├── service/ai/AiGreetingService.java, AiOnboardingService.java
        ├── controller/DepartmentController.java, EmployeeController.java, AiController.java
        ├── exception/GlobalExceptionHandler.java
        └── config/SecurityConfig.java
```

---

**Time split:**

| Block | Duration | Topics |
|-------|----------|--------|
| Recap + AI crisp | 15 min | 1, 2 |
| Scaffold + architecture | 10 min | 3 |
| Core EMS (live coding) | 45 min | 4–8 |
| First AI | 25 min | 9–11 |
| REST polish + Security | 15 min | 12–13 |
| Wrap | 5 min | 14 |

---

## HOW TO RUN — VS Code / Cursor (do this BEFORE class)

### One-time setup

| Step | Action |
|------|--------|
| 1 | Open repo root in VS Code / Cursor |
| 2 | Install **Extension Pack for Java** + **Spring Boot Extension Pack** |
| 3 | Wait for Maven import (bottom-right progress bar) |
| 4 | In terminal: `cp week-02-employee-management/.env.example week-02-employee-management/.env` |
| 5 | Open `.env` → paste Groq key after `GROQ_API_KEY=` |
| 6 | Save `.env` — **never commit** |

### Every time you run the app

| Step | Action |
|------|--------|
| 1 | Stop any old run (red square) |
| 2 | **F5** → **Week 2 EMS — Run with Groq (H2)** |
| 3 | Wait for: `Tomcat started on port 8080` |
| 4 | Postman: use **Basic Auth** — user `hr@codekerdos.in`, password `hr123` |

---

# TOPIC 1 — Week 1 Recap + Where We Stopped

### SAY

> "Week 1 Saturday = plain Spring Core — IoC, DI, beans.
> Week 1 Sunday we started Spring Boot EMS but only got through **application.yml**.
> Today = **Week 2, new folder**, we finish the EMS from that checkpoint and add Security.
> By Sunday you'll have **Project #1 complete** — full EMS with MySQL and AI search."

### Quick fire questions

| Question | Expected answer |
|----------|-----------------|
| What is IoC? | Spring controls object creation |
| What is DI? | Spring injects dependencies |
| What is `@SpringBootApplication`? | Config + auto-config + component scan |
| Where did we stop in Week 1? | `application.yml` — config only |
| Why a new Week 2 folder? | Clean segregation — Week 1 = checkpoint, Week 2 = finished project |

### DRAW

```
week-01-employee-management  →  stopped at application.yml
week-02-employee-management  →  full EMS (today + Sunday)
```

### END THOUGHT

> "Next: what AI actually does in this bootcamp — students always ask this."

---

# TOPIC 2 — AI in This Bootcamp (Crisp)

### SAY

> "We are **NOT training AI models**. No Python notebooks. No fine-tuning.
> We **call Groq's hosted LLM** (Llama 3.3) from Java using **Spring AI ChatClient**.
> Same DI pattern as any `@Service` — inject and use."

### Contents (draw on board — 8 minutes max)

| Bootcamp project | AI feature | When |
|------------------|------------|------|
| **EMS (this project)** | Greet, onboarding checklist, **natural language employee search** | Week 1–2 |
| Expense System | Categorize + fraud + manager summary | Week 4–5 |
| Booking + RAG | Document Q&A with retrieval | Week 5 |

### EMS AI roadmap (this week)

| Endpoint | AI does what |
|----------|--------------|
| `GET /api/ai/greet` | Demo — proves LLM works from Java |
| `POST /api/ai/onboarding-checklist` | Dynamic HR checklist via PromptTemplate |
| `POST /api/ai/search-employees` | **Real utility** — English → JSON filters → DB query (Sunday) |

### DRAW

```
Your Java code  →  ChatClient  →  Groq API  →  Llama model  →  text/JSON back
     ↑                                                              ↓
  @Service bean                                            use in REST response
```

### YOU DO

Nothing — board only. Tell students: *"Today Topics 10–11 = first two AI features. Sunday = the killer search."*

### END THOUGHT

> "AI is a tool inside our Spring app — not a separate magic layer. Topic 3 — scaffold the project."

---

# TOPIC 3 — New Folder + Layered Architecture

### SAY

> "New folder `week-02-employee-management`. Same EMS domain, fresh start from Week 1 checkpoint.
> We use **layered architecture** — every enterprise Spring project follows this."

### DRAW

```
HTTP Request
     ↓
Controller  ← @RestController — JSON in/out
     ↓
Service     ← @Service — business rules
     ↓
Repository  ← JpaRepository — database
     ↓
Database (H2 today, MySQL Sunday)
```

**Layman:** Controller = receptionist · Service = manager · Repository = filing cabinet

### YOU DO — Scaffold project

**Step 1 — Create folder**

1. In repo root, create folder: `week-02-employee-management`
2. Tell students: *"Copy your Week 1 `pom.xml`, `application.yml`, and main class — or follow me from scratch."*

**Step 2 — Replace `pom.xml`**

Paste full content (includes web, JPA, validation, security, H2, MySQL, Spring AI):

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
    <artifactId>week-02-employee-management</artifactId>
    <version>2.0-SNAPSHOT</version>

    <properties>
        <java.version>17</java.version>
        <spring-ai.version>1.0.0-M5</spring-ai.version>
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

> Say: *"Security and MySQL deps added now — we use them later today and Sunday."*

**Step 3 — Create package + main class**

1. `src/main/java/in/codekerdos/ems/EmployeeManagementApplication.java`

```java
package in.codekerdos.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EmployeeManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagementApplication.class, args);
    }
}
```

**Step 4 — `application.yml`**

```yaml
server:
  port: 8080

spring:
  application:
    name: employee-management
  profiles:
    active: h2

  ai:
    openai:
      api-key: ${GROQ_API_KEY}
      base-url: https://api.groq.com/openai
      chat:
        options:
          model: llama-3.3-70b-versatile

---
spring:
  config:
    activate:
      on-profile: h2
  datasource:
    url: jdbc:h2:mem:emsdb
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
```

**Step 5 — Maven reload**

VS Code → Maven panel → Reload. Wait for dependencies.

### RUN

F5 → app starts (Security will block APIs later — that's OK for now).

### STUCK?

Copilot: *"Create Spring Boot 3.4 pom with web, JPA, validation, security, H2, Spring AI Groq"*

### END THOUGHT

> "Skeleton exists. Topic 4 — database tables as Java classes."

---

# TOPIC 4 — JPA Entities: Department & Employee

### SAY

> "JPA = Java Persistence API. Write `@Entity` classes → Hibernate creates SQL tables.
> You don't write `CREATE TABLE` — Spring does it from your Java."

### YOU DO — Create entity files

**FILE 1 — `entity/Department.java`**

```java
package in.codekerdos.ems.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Employee> employees = new ArrayList<>();

    // getters and setters for id, name, employees
}
```

**FILE 2 — `entity/Employee.java`**

```java
package in.codekerdos.ems.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String role;
    private String team;
    private LocalDate joinedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    // getters and setters
}
```

> Say: *"One Department has many Employees. `@ManyToOne` on Employee side."*

### DRAW

```
Department (1) ──────< Employee (many)
Engineering          Rahul, Priya, Amit
```

### RUN

Start app → console shows `create table departments...` and `create table employees...`

### END THOUGHT

> "Tables exist in H2 memory. Topic 5 — repositories to talk to DB."

---

# TOPIC 5 — Repositories

### SAY

> "`JpaRepository` gives you save, findAll, findById, delete — **free**.
> Spring Data generates SQL from method names."

### YOU DO

**FILE 3 — `repository/DepartmentRepository.java`**

```java
package in.codekerdos.ems.repository;

import in.codekerdos.ems.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
```

**FILE 4 — `repository/EmployeeRepository.java`**

```java
package in.codekerdos.ems.repository;

import in.codekerdos.ems.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByTeam(String team);

    List<Employee> findByRoleContainingIgnoreCase(String role);

    List<Employee> findByJoinedDateAfter(LocalDate date);
}
```

> Say: *"`findByTeam` → Spring generates `WHERE team = ?` automatically."*

### END THOUGHT

> "Zero SQL written. Topic 6 — DTOs for clean API shapes."

---

# TOPIC 6 — DTOs + Validation

### SAY

> "Never expose `@Entity` directly in REST APIs. Use **DTOs** (Data Transfer Objects).
> `@Valid` + `@NotBlank` catch bad requests before they hit your service."

### YOU DO — Create DTOs

**CreateDepartmentRequest.java**, **DepartmentResponse.java**, **CreateEmployeeRequest.java**, **EmployeeResponse.java**, **OnboardingRequest.java** — see `week-02-employee-management/src/main/java/in/codekerdos/ems/dto/` for full code.

Key pattern — request record with validation:

```java
public record CreateEmployeeRequest(
        @NotBlank(message = "Name is required") String name,
        String role,
        String team,
        LocalDate joinedDate,
        @NotNull(message = "Department ID is required") Long departmentId
) {}
```

Response factory method:

```java
public static EmployeeResponse from(Employee employee) {
    return new EmployeeResponse(
            employee.getId(),
            employee.getName(),
            employee.getRole(),
            employee.getTeam(),
            employee.getJoinedDate(),
            employee.getDepartment() != null ? employee.getDepartment().getName() : null
    );
}
```

### END THOUGHT

> "API contract is clear. Topic 7 — business logic in services."

---

# TOPIC 7 — Service Layer

### SAY

> "Controllers stay thin. **Services** hold business rules and transactions.
> `@Transactional` = all DB operations in one method succeed or fail together."

### YOU DO

**FILE — `service/ResourceNotFoundException.java`**

```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

**FILE — `service/DepartmentService.java`** — create, findAll, findById (throws if missing)

**FILE — `service/EmployeeService.java`** — create (loads department by id), findAll, findById, delete

> Point at constructor injection — *"Same DI pattern from Week 1 Class 1."*

### END THOUGHT

> "Business layer ready. Topic 8 — expose via REST."

---

# TOPIC 8 — CRUD REST APIs

### SAY

> "`@RestController` = every method return value becomes JSON automatically.
> HTTP methods map to CRUD operations."

### YOU DO

**DepartmentController** — `POST /api/departments`, `GET /api/departments`

**EmployeeController** — `GET /api/employees`, `GET /api/employees/{id}`, `POST /api/employees`, `DELETE /api/employees/{id}`

### HTTP methods (board)

| Method | URL | Action |
|--------|-----|--------|
| GET | `/api/employees` | List |
| GET | `/api/employees/1` | Get one |
| POST | `/api/employees` | Create |
| DELETE | `/api/employees/1` | Delete |

### RUN — Postman (no auth yet — Security comes Topic 13)

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

```http
GET http://localhost:8080/api/employees
```

### END THOUGHT

> "CRUD works. Topic 9 — quick AI architecture, then we code it."

---

# TOPIC 9 — Spring AI + Groq Architecture (5 min)

### SAY

> "Groq hosts Llama 3.3 in the cloud. Spring AI's OpenAI starter works because Groq speaks **OpenAI-compatible API** — we just change `base-url`.
> `ChatClient` is injected like any `@Service`."

### DRAW

```
/api/ai/greet  →  AiGreetingService  →  ChatClient  →  Groq  →  text response
```

### END THOUGHT

> "Five lines of AI logic coming up."

---

# TOPIC 10 — ChatClient: First AI Call

### YOU DO

**FILE — `service/ai/AiGreetingService.java`**

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
                .user("Say a warm one-line welcome to " + name
                        + " who just joined CodeKerdos Spring Boot + AI Bootcamp. Keep it under 20 words.")
                .call()
                .content();
    }
}
```

**Update `AiController.java`** — add `GET /api/ai/greet`

### RUN

```http
GET http://localhost:8080/api/ai/greet?name=Shivansh
```

Expected: JSON with `aiMessage` from Groq. Requires `GROQ_API_KEY` in `.env` + F5 restart.

### END THOUGHT

> "LLM works from Java. Topic 11 — reusable prompts."

---

# TOPIC 11 — PromptTemplate: Onboarding Checklist

### YOU DO

**FILE — `service/ai/AiOnboardingService.java`**

Use `PromptTemplate` with `{name}`, `{role}`, `{department}`, `{team}` placeholders.

**Add to AiController:**

```http
POST http://localhost:8080/api/ai/onboarding-checklist
Content-Type: application/json

{
  "name": "Priya Patel",
  "role": "Junior Developer",
  "department": "Engineering",
  "team": "Frontend"
}
```

### END THOUGHT

> "Two AI features done. Topic 12 — professional error handling."

---

# TOPIC 12 — REST Polish: GlobalExceptionHandler

### SAY

> "Without this, errors return ugly stack traces. `@RestControllerAdvice` catches exceptions globally and returns clean JSON."

### YOU DO

**FILE — `exception/GlobalExceptionHandler.java`**

Handle:
- `ResourceNotFoundException` → 404 JSON
- `MethodArgumentNotValidException` → 400 JSON with field message

### RUN

```http
GET http://localhost:8080/api/employees/999
```

Expected: `{ "status": 404, "message": "Employee not found with id: 999" }`

### END THOUGHT

> "APIs behave professionally. Topic 13 — lock them down with Security."

---

# TOPIC 13 — Spring Security: HTTP Basic

### SAY

> "HR data must not be public. **HTTP Basic Auth** = username + password on every API request.
> Postman Authorization tab → Basic Auth. Fast, perfect for APIs."

### YOU DO

**Add to pom.xml** (if not already): `spring-boot-starter-security`

**FILE — `config/SecurityConfig.java`**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/h2-console/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails hrUser = User.builder()
                .username("hr@codekerdos.in")
                .password("{noop}hr123")
                .roles("HR")
                .build();
        return new InMemoryUserDetailsManager(hrUser);
    }
}
```

> Say: *"`{noop}` = plain text password for class demo only. Production uses BCrypt + env vars."*

### RUN

1. `GET /api/employees` **without** auth → **401 Unauthorized**
2. Postman → Authorization → Basic Auth → `hr@codekerdos.in` / `hr123` → **200 OK**

### END THOUGHT

> "APIs are protected. Sunday we add a **login page** with Thymeleaf + MySQL + AI search."

---

# TOPIC 14 — Full Demo + Homework

### YOU DO — Final demo (5 min)

Run this sequence with Basic Auth on every request:

```
✅ 1.  POST /api/departments  →  "Engineering"
✅ 2.  POST /api/employees    →  2 employees
✅ 3.  GET  /api/employees    →  list
✅ 4.  GET  /api/ai/greet?name=YourName
✅ 5.  POST /api/ai/onboarding-checklist
✅ 6.  GET  /api/employees/999  →  show 404 handler
✅ 7.  GET  /api/employees (no auth)  →  show 401
```

### Homework

| # | Task |
|---|------|
| 1 | Repeat today's build in your IDE |
| 2 | Add `PUT /api/employees/{id}` update endpoint |
| 3 | Install MySQL locally — create `ems_db` (see Class-2 Topic 3) |
| 4 | Push to GitHub — never commit `.env` |

### SAY — Sunday preview

> "Sunday = MySQL, pagination, **natural language employee search**, and a **login page**.
> That's when EMS becomes **Project #1 — complete**."

### END THOUGHT

> "Today: full EMS core + first AI + Basic Auth. Sunday: production touches + killer AI feature."

---

## QUICK REFERENCE — If you forget mid-class

| Problem | Fix |
|---------|-----|
| 401 on all APIs | Add Basic Auth in Postman |
| Groq 401/500 | Check `.env` + restart F5 |
| Red imports | Maven → Reload |
| H2 console blank | JDBC URL = `jdbc:h2:mem:emsdb` |
| Security blocks H2 | Permit `/h2-console/**` in SecurityConfig |

---

## WEEK 2 CLASS 1 — Interview Quick Reference

| Question | Answer |
|----------|--------|
| Why DTOs? | Decouple API contract from database schema |
| Why `@Transactional`? | Atomic DB operations |
| What is JPA? | Maps Java objects to relational tables |
| What is Groq? | Free fast LLM API, OpenAI-compatible |
| What is ChatClient? | Spring AI bean to call any LLM |
| What is PromptTemplate? | Reusable prompt with `{variables}` |
| Why HTTP Basic? | Simple auth for REST APIs / Postman |
| Why new Week 2 folder? | Clean project segregation from Week 1 checkpoint |

---

*CodeKerdos.in · Week 2 Class 1 · Build live, don't show reference repo on screen*
