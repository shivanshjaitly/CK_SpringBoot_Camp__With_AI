# Week 3 | Security Discussion

> Employee Management System (EMS) — CodeKerdos Spring Boot Camp  
> Based on: `week-02-employee-management`

---

## Table of Contents

1. [Why is `ResourceNotFoundException` inside `service/` and not `exception/`?](#1-why-is-resourcenotfoundexception-inside-service-and-not-exception)
2. [Security Config — End to End](#2-security-config--end-to-end)
3. [Interview Questions (Spring / Spring Boot / This Project)](#3-interview-questions)

---

## 1. Why is `ResourceNotFoundException` inside `service/` and not `exception/`?

### What we have today

```
src/main/java/in/codekerdos/ems/
├── service/
│   ├── ResourceNotFoundException.java   ← exception class lives here
│   ├── EmployeeService.java             ← throws it
│   └── DepartmentService.java           ← throws it
└── exception/
    └── GlobalExceptionHandler.java      ← catches it and returns HTTP 404
```

**Thrown in service layer:**

```java
// EmployeeService.java
employeeRepository.findById(id)
    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
```

**Handled in web layer:**

```java
// GlobalExceptionHandler.java
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(...);
}
```

### Short answer

Both placements are valid. We put it in `service/` because **the service layer owns the business rule** “this employee/department must exist.” The `exception/` package is reserved for **cross-cutting web concerns** — things that translate errors into HTTP responses (`@RestControllerAdvice`).

### The layered responsibility

| Layer | Responsibility |
|-------|----------------|
| **Service** | Business logic. Decides *when* something is “not found.” |
| **Exception handler** | HTTP translation. Decides *how* to respond (status code, JSON body). |
| **Exception class** | The signal between them. Can live in either package. |

### Why NOT always put it in `exception/`?

Putting every custom exception in `exception/` is a common beginner pattern. It works, but it mixes two ideas:

- **Domain / application exceptions** — “Employee 42 does not exist” (business meaning)
- **Infrastructure / presentation exceptions** — “How to format a 404 JSON response” (web meaning)

`ResourceNotFoundException` is a **domain-level signal**. The service says: *“I looked for this resource and it is not there.”* The controller never checks existence; the service does.

### Why NOT always put it in `service/`?

Some teams prefer a dedicated package:

```
exception/
├── ResourceNotFoundException.java
├── GlobalExceptionHandler.java
└── ApiErrorResponse.java
```

That is **cleaner for larger projects** where many layers (service, repository, external APIs) throw the same exceptions.

### Recommended refactor (optional, for students)

If you want the “textbook” package structure, move the file — no logic change:

**Step 1 — Move the class**

```java
// src/main/java/in/codekerdos/ems/exception/ResourceNotFoundException.java
package in.codekerdos.ems.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

**Step 2 — Update imports in 3 files**

```java
// EmployeeService.java, DepartmentService.java
import in.codekerdos.ems.exception.ResourceNotFoundException;

// GlobalExceptionHandler.java — remove the service import, use same package
import in.codekerdos.ems.exception.ResourceNotFoundException;
```

**Step 3 — Delete** `service/ResourceNotFoundException.java`

### Checked vs unchecked

`ResourceNotFoundException` extends `RuntimeException` (unchecked). That is intentional:

- Service methods do not need `throws` in their signature
- Spring `@Transactional` rolls back on unchecked exceptions by default
- `@ExceptionHandler` can catch it globally without try/catch in every controller

### Interview one-liner

> “The exception class defines the error type; the service throws it when a business rule fails; the `@RestControllerAdvice` maps it to HTTP. Package placement is a team convention — we kept the handler in `exception/` and the domain exception close to where it is thrown.”

---

## 2. Security Config — End to End

### 2.1 What is in `config/` today?

Currently there is **one file**:

```
config/
└── SecurityConfig.java
```

There is no separate `WebMvcConfig`, `JpaConfig`, or `AiConfig` — Spring Boot auto-configuration handles datasource, JPA, Thymeleaf, and Spring AI from `application.yml` and `pom.xml`.

`SecurityConfig` is the **only explicit security wiring** in this project.

---

### 2.2 Full walkthrough of `SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
```

| Annotation | Purpose |
|------------|---------|
| `@Configuration` | Tells Spring: “this class defines `@Bean`s.” |
| `@EnableWebSecurity` | Turns on Spring Security filter chain for a servlet (web) app. |

Without `@EnableWebSecurity`, adding `spring-boot-starter-security` would still secure the app with **default** rules (everything locked, generated password in logs). Our class **replaces** those defaults.

---

#### Bean 1: `SecurityFilterChain`

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
```

This is the **heart of Spring Security 6** (Spring Boot 3.x). One filter chain = one set of rules applied to incoming HTTP requests.

**Request authorization:**

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/login", "/css/**", "/h2-console/**").permitAll()
    .requestMatchers("/api/**").authenticated()
    .anyRequest().authenticated()
)
```

| URL pattern | Who can access? | Why |
|-------------|-----------------|-----|
| `/login` | Anyone | Custom login page must load without auth |
| `/css/**` | Anyone | Static styles for login page |
| `/h2-console/**` | Anyone | H2 DB console (dev only — never in production) |
| `/api/**` | Logged-in user only | REST APIs |
| Everything else (`/`, etc.) | Logged-in user only | Thymeleaf home page |

**Order matters:** First matching rule wins. `permitAll()` rules must come **before** broad `authenticated()` rules.

---

**Two authentication mechanisms enabled at once:**

```java
.httpBasic(Customizer.withDefaults())
.formLogin(form -> form
    .loginPage("/login")
    .defaultSuccessUrl("/", true)
    .permitAll()
)
```

This is important for students — we use **both**:

| Mechanism | Used by | How credentials are sent |
|-----------|---------|----------------------------|
| **Form login** | Browser (Thymeleaf UI) | HTML `<form>` POST → session cookie (`JSESSIONID`) |
| **HTTP Basic** | Postman / curl / API clients | `Authorization: Basic base64(user:pass)` header |

You do **not** need two separate user databases. Both use the same `UserDetailsService` bean.

**Flow — Browser (form login):**

```
1. User opens http://localhost:8080/
2. Not authenticated → redirect to /login
3. User submits hr@codekerdos.in / hr123
4. Spring Security validates against UserDetailsService
5. Creates HTTP session, sets JSESSIONID cookie
6. Redirect to / (home.html)
7. Subsequent requests send cookie → authenticated
```

**Flow — API (HTTP Basic):**

```bash
curl -u hr@codekerdos.in:hr123 http://localhost:8080/api/employees
```

```
1. Client sends Authorization header on every request
2. Spring Security decodes username/password
3. Validates against UserDetailsService
4. No session cookie required
5. Request proceeds to EmployeeController
```

---

**Logout:**

```java
.logout(logout -> logout
    .logoutSuccessUrl("/login?logout")
    .permitAll()
)
```

Default logout URL: `POST /logout` (Spring Security default). After logout → `/login?logout` → Thymeleaf shows success message.

---

**CSRF (Cross-Site Request Forgery):**

```java
.csrf(csrf -> csrf
    .ignoringRequestMatchers("/api/**", "/h2-console/**")
)
```

| Context | CSRF |
|---------|------|
| Browser form (`POST /login`) | **Protected** — Spring adds hidden CSRF token |
| REST `/api/**` | **Disabled** — stateless clients (Postman) do not send CSRF tokens |
| H2 console | **Disabled** — H2 UI cannot send CSRF token easily |

> **Production note:** If you move APIs to session + browser, re-enable CSRF for APIs or use JWT instead.

---

**Headers (H2 iframe):**

```java
.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
```

H2 console loads in an iframe. Without `sameOrigin`, Spring Security blocks it with `X-Frame-Options: DENY`.

---

#### Bean 2: `UserDetailsService`

```java
@Bean
public UserDetailsService userDetailsService() {
    UserDetails hrUser = User.builder()
            .username("hr@codekerdos.in")
            .password("{noop}hr123")
            .roles("HR")
            .build();
    return new InMemoryUserDetailsManager(hrUser);
}
```

| Part | Meaning |
|------|---------|
| `UserDetailsService` | Spring Security interface: “load user by username” |
| `InMemoryUserDetailsManager` | Users stored in RAM — **lost on restart**, fine for demos |
| `{noop}hr123` | `{noop}` = no password hashing (plaintext). **Never use in production** |
| `.roles("HR")` | Grants role `ROLE_HR` internally |

**We are NOT using JWT in this project.** Authentication is **session-based (browser)** or **per-request Basic auth (API)**.

---

### 2.3 How Spring Security fits in the request pipeline

```mermaid
sequenceDiagram
    participant Client
    participant FilterChain as Security Filter Chain
    participant Auth as AuthenticationManager
    participant UDS as UserDetailsService
    participant Controller

    Client->>FilterChain: HTTP Request
    FilterChain->>FilterChain: Check authorizeHttpRequests rules
    alt Not authenticated and protected URL
        FilterChain-->>Client: 302 → /login or 401 Unauthorized
    else Authenticated (session or Basic)
        FilterChain->>Controller: Forward request
        Controller-->>Client: Response
    end

    Note over FilterChain,UDS: On login: Auth validates via UserDetailsService
```

**Key classes students should know:**

| Class | Role |
|-------|------|
| `SecurityFilterChain` | Defines security rules (our `@Bean`) |
| `UserDetailsService` | Loads users |
| `AuthenticationManager` | Validates credentials |
| `SecurityContextHolder` | Holds current user in thread-local storage |
| `HttpSession` | Stores auth after form login |

---

### 2.4 JWT — How it works (and why we are NOT using it yet)

#### What is JWT?

**JWT (JSON Web Token)** = signed string with 3 parts:

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJociJ9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
|______Header______|.|_____Payload_____|.|_________Signature_________|
```

| Part | Contains |
|------|----------|
| Header | Algorithm (e.g. HS256) |
| Payload | Claims: `sub` (username), `exp` (expiry), `roles` |
| Signature | HMAC/RSA sign(header + payload, secret) — proves token was issued by server |

#### JWT flow (typical production API)

```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant JwtService
    participant API

    Client->>AuthController: POST /auth/login {username, password}
    AuthController->>AuthController: Validate credentials
    AuthController->>JwtService: generateToken(user)
    JwtService-->>Client: { "token": "eyJ..." }

    Client->>API: GET /api/employees<br/>Authorization: Bearer eyJ...
    API->>JwtService: validateToken(token)
    JwtService-->>API: username + roles
    API-->>Client: 200 JSON
```

**Differences from our current setup:**

| Topic | Current (Week 2/3) | JWT approach |
|-------|-------------------|--------------|
| State | Server session (`JSESSIONID`) | Stateless — server only validates signature |
| API auth | HTTP Basic every request | `Bearer <token>` header |
| Expiry | Session timeout | `exp` claim in token |
| Logout | Invalidate session | Blacklist token or wait for expiry |
| Scalability | Sessions need sticky sessions / shared store | Easier horizontal scaling |

#### “JWT inside JWT” — common student confusion

Sometimes people hear **nested JWT** or **JWE (encrypted JWT)**. Clarification:

- **Normal JWT** = signed (JWS), readable by anyone who has the token (payload is Base64, not encrypted)
- **JWE** = encrypted JWT — inner payload hidden
- **Nested JWT** = a JWT inside another JWT (rare; used in some OIDC flows)

For this bootcamp: **one JWT per login** is enough. No nesting required.

#### “1 or 2 authentication parts” — clarification

Students often ask: “Why do we have form login AND httpBasic?”

Answer: **One identity store (`UserDetailsService`), two entry points.**

```
                    ┌─────────────────────┐
                    │  UserDetailsService │
                    │  (hr@codekerdos.in) │
                    └──────────▲──────────┘
                               │
              ┌────────────────┴────────────────┐
              │                                 │
     FormLoginFilter                    BasicAuthenticationFilter
     (browser + session)                (Authorization header)
```

It is **not** two separate authentication systems — it is **one authentication manager, two filters**.

---

### 2.5 Alternative security approaches (for students)

| Approach | Best for | Complexity |
|----------|----------|------------|
| **In-memory user + form + Basic** (current) | Learning, demos | Low |
| **Database users + BCrypt** | Real apps with few users | Medium |
| **JWT + stateless API** | SPAs, mobile apps | Medium–High |
| **OAuth2 / Login with Google** | Production SaaS | High |
| **Spring Authorization Server** | You are the identity provider | High |

---

### 2.6 Common student issues and fixes

| Problem | Cause | Fix |
|---------|-------|-----|
| `403 Forbidden` on POST `/api/employees` | CSRF enabled for API | Already fixed: `.ignoringRequestMatchers("/api/**")` |
| `401` on API from Postman | No credentials | Use **Authorization → Basic Auth** or `-u user:pass` |
| Redirect loop on `/login` | `/login` not `permitAll` | Already in `requestMatchers(...).permitAll()` |
| H2 console blank / blocked | Frame options | `frameOptions(sameOrigin)` — already set |
| `Whitelabel Error` after login | Wrong `defaultSuccessUrl` | Set to `/` — already done |
| Password not working | Wrong encoding | Week 3 uses **BCrypt** — password is still `hr123`, but stored hashed via `passwordEncoder.encode()` |
| API works in browser but not Postman | Browser has session cookie | Postman needs Basic auth, not cookies |

---

### 2.7 Code changes students can do (progressive exercises)

> **All exercises below are already implemented in the codebase.** Open the linked files in class — no need to copy-paste from this doc.

---

#### Exercise A — Move to `exception/` package

| What | File |
|------|------|
| Real exception (canonical) | `src/main/java/in/codekerdos/ems/exception/ResourceNotFoundException.java` |
| Old location kept as pointer | `src/main/java/in/codekerdos/ems/service/ResourceNotFoundException.java` |
| Handler (same package now) | `src/main/java/in/codekerdos/ems/exception/GlobalExceptionHandler.java` |
| Services import `exception` | `EmployeeService.java`, `DepartmentService.java` |

**In class, show students:** the `service/` file is a thin `@Deprecated` subclass that points to `exception/`. Old imports still compile; new code should use `in.codekerdos.ems.exception.ResourceNotFoundException`.

---

#### Exercise B — Hash passwords with BCrypt

| What | File |
|------|------|
| `PasswordEncoder` + BCrypt user | `src/main/java/in/codekerdos/ems/config/SecurityConfig.java` → `passwordEncoder()` and `userDetailsService()` beans |

Demo login still works: `hr@codekerdos.in` / `hr123` (encoded at startup, not `{noop}`).

---

#### Exercise C — Role-based access (`@PreAuthorize`)

| What | File |
|------|------|
| `@EnableMethodSecurity` | `src/main/java/in/codekerdos/ems/config/SecurityConfig.java` |
| `@PreAuthorize("hasRole('HR')")` on delete | `src/main/java/in/codekerdos/ems/controller/EmployeeController.java` |

---

#### Exercise D — Add JWT

| What | File |
|------|------|
| JWT dependencies | `pom.xml` (jjwt-api, jjwt-impl, jjwt-jackson 0.12.6) |
| Token generate / validate | `src/main/java/in/codekerdos/ems/config/JwtService.java` |
| Bearer filter | `src/main/java/in/codekerdos/ems/config/JwtAuthFilter.java` |
| Login → token endpoint | `src/main/java/in/codekerdos/ems/controller/AuthController.java` |
| Filter + `AuthenticationManager` | `src/main/java/in/codekerdos/ems/config/SecurityConfig.java` |
| Secret + expiry | `src/main/resources/application.yml` → `jwt.secret`, `jwt.expiry-ms` |

**Three ways to call APIs (all work together):**

1. Browser form login → session cookie  
2. HTTP Basic → `curl -u hr@codekerdos.in:hr123 http://localhost:8080/api/employees`  
3. JWT → login for token, then Bearer header  

**Test JWT in class:**

```bash
# 1. Get token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"hr@codekerdos.in","password":"hr123"}'

# 2. Use token
curl http://localhost:8080/api/employees \
  -H "Authorization: Bearer <paste-token-here>"
```

---

#### Exercise E — Disable security for local dev only

| What | File |
|------|------|
| Open security chain | `src/main/java/in/codekerdos/ems/config/DevOpenSecurityConfig.java` |

Activate with:

```yaml
spring.profiles.active: dev-open,h2
```

`SecurityConfig` and `AuthController` use `@Profile("!dev-open")` — they are skipped when `dev-open` is active.

---

### 2.8 How security connects to the rest of the app

```
Browser                          Postman / curl
   │                                   │
   ▼                                   ▼
/login (PageController)          Authorization: Basic ...
   │                                   │
   └──────────► SecurityFilterChain ◄─┘
                      │
                      ▼ (authenticated)
            ┌─────────────────────┐
            │ EmployeeController  │  /api/employees
            │ DepartmentController│  /api/departments
            │ AiController        │  /api/ai/*
            └─────────────────────┘
                      │
                      ▼
                 Service layer
                      │
                      ▼
              JPA Repository → H2 / MySQL
```

`PageController` serves Thymeleaf (`home.html`, `login.html`).  
`EmployeeController` etc. are REST — all under `/api/**` → must be authenticated.

---

## 3. Interview Questions

Organized by topic. Answers reference **this project's code** where possible.

---

### 3.1 Spring Core & Spring Boot fundamentals

1. **What is Spring Boot? How is it different from Spring Framework?**  
   Spring Boot auto-configures Spring Framework using starters, embedded server, and convention-over-configuration. Our `pom.xml` uses `spring-boot-starter-parent` 3.4.1 — we do not manually configure a `DispatcherServlet` or XML beans.

2. **What does `@SpringBootApplication` do?**  
   Combines `@Configuration`, `@EnableAutoConfiguration`, and `@ComponentScan`. Entry point: `EmployeeManagementApplication.main()`.

3. **What is dependency injection? Show an example from this project.**  
   Spring creates beans and injects them via constructors. Example: `EmployeeController` receives `EmployeeService` through its constructor — no `new EmployeeService()`.

4. **What is the difference between `@Component`, `@Service`, `@Repository`, and `@Controller`?**  
   All are stereotype annotations for component scanning. `@Service` = business layer (`EmployeeService`), `@Repository` = data layer (`EmployeeRepository`), `@Controller` / `@RestController` = web layer.

5. **What is `@RestController` vs `@Controller`?**  
   `@RestController` = `@Controller` + `@ResponseBody` — returns JSON directly. `EmployeeController` uses it. `PageController` uses `@Controller` — returns view names like `"home"` for Thymeleaf.

6. **What is constructor injection and why is it preferred?**  
   Dependencies are `final`, immutable, and required at construction. All controllers and services in this project use constructor injection.

7. **What is Spring Boot auto-configuration?**  
   When `spring-boot-starter-data-jpa` is on the classpath, Boot auto-configures `DataSource`, `EntityManagerFactory`, etc. from `application.yml` without manual `@Bean` definitions.

8. **What is `application.yml` used for in this project?**  
   Server port, active profile (`h2` / `mysql`), datasource, JPA settings, Groq AI API config.

9. **What are Spring profiles? How do we use them here?**  
   Profile-specific config blocks in `application.yml`. `spring.profiles.active: h2` uses in-memory H2; `mysql` profile switches to MySQL connection.

10. **What is the Spring Application Context?**  
    IoC container holding all beans (`EmployeeService`, `SecurityConfig`, repositories, etc.).

---

### 3.2 Web layer (REST, validation, DTOs)

11. **What HTTP method and path creates an employee?**  
    `POST /api/employees` — `EmployeeController.create()`.

12. **What does `@Valid` do on `CreateEmployeeRequest`?**  
    Triggers Bean Validation (`@NotBlank`, `@NotNull`). Failures throw `MethodArgumentNotValidException` → handled by `GlobalExceptionHandler` → 400.

13. **Why use DTOs (`EmployeeResponse`, `CreateEmployeeRequest`) instead of returning `Employee` entity?**  
    Decouples API contract from DB schema; avoids lazy-loading issues; hides internal fields; `EmployeeResponse.from(employee)` controls exposed data.

14. **What is a Java `record`? Where is it used?**  
    Immutable data carrier with auto-generated constructor/accessors. Used for DTOs: `CreateEmployeeRequest`, `EmployeeResponse`, etc.

15. **What does `@PathVariable` vs `@RequestParam` do?**  
    `@PathVariable Long id` on `/api/employees/{id}` — id in URL path. `@RequestParam int page` on `?page=0&size=10` — query string.

16. **What status code does `create()` return and why?**  
    `201 CREATED` via `@ResponseStatus(HttpStatus.CREATED)`.

17. **What does `ResponseEntity.noContent()` mean for delete?**  
    HTTP `204 No Content` — success with no response body.

18. **What is `@RestControllerAdvice`?**  
    Global exception handling for all `@RestController` classes. Our `GlobalExceptionHandler` centralizes 404, 400, validation errors.

19. **What is content negotiation?**  
    Spring MVC maps return types to JSON (Jackson on classpath via `spring-boot-starter-web`).

20. **What is Thymeleaf's role in this app?**  
    Server-side HTML for `/` and `/login`. `thymeleaf-extras-springsecurity6` can show/hide UI based on auth (available in `pom.xml`).

---

### 3.3 JPA & database

21. **What is JPA? What is Hibernate's role?**  
    JPA is the specification; Hibernate is the implementation. We use Spring Data JPA on top.

22. **What does `@Entity` and `@Table` do?**  
    Maps `Employee` class to `employees` table.

23. **Explain `@Id` and `@GeneratedValue(strategy = GenerationType.IDENTITY)`.**  
    Primary key auto-generated by database (auto-increment).

24. **What is `@ManyToOne` on `Employee.department`?**  
    Many employees belong to one department. `FetchType.LAZY` loads department only when accessed.

25. **What is `@OneToMany(mappedBy = "department")` on `Department.employees`?**  
    Bidirectional relationship; `mappedBy` means `Employee` owns the foreign key (`department_id`).

26. **What is `ddl-auto: update`?**  
    Hibernate creates/updates tables on startup. Fine for dev; use Flyway/Liquibase in production.

27. **What is `JpaRepository<Employee, Long>`?**  
    Provides `save`, `findById`, `findAll`, `deleteById` without writing SQL.

28. **What are derived query methods in `EmployeeRepository`?**  
    `findByTeam`, `findByRoleContainingIgnoreCase` — Spring Data generates queries from method names.

29. **What is `JpaSpecificationExecutor`?**  
    Enables dynamic queries via `Specification<Employee>`. Used in `EmployeeSpecifications` for flexible search.

30. **What is the N+1 problem?**  
    Loading N employees then lazily loading each department = N+1 queries. Fix: `@EntityGraph`, `join fetch`, or DTO projections.

31. **What is `@Transactional` on `EmployeeService`?**  
    All public methods run in a DB transaction. `readOnly = true` on queries optimizes performance.

32. **Difference between `persist` and `merge`?**  
    `save()` on new entity → persist (INSERT); on detached → merge (UPDATE). Our `create()` always builds new `Employee` → INSERT.

33. **H2 vs MySQL in this project?**  
    H2: in-memory, fast for class demos, console at `/h2-console`. MySQL: persistent, activated via `mysql` profile.

---

### 3.4 Spring Security (this project's focus)

34. **What happens if you add `spring-boot-starter-security` with no config?**  
    All endpoints secured; random password printed in logs; no custom login page.

35. **What is `SecurityFilterChain`?**  
    Defines authorization, login, logout, CSRF, headers. Replaces deprecated `WebSecurityConfigurerAdapter` (Spring Security 6).

36. **Why is `/login` permitted without authentication?**  
    Otherwise infinite redirect: protected page → login → login is protected → login → ...

37. **Explain form login vs HTTP Basic in our config.**  
    Form login: browser session cookie. Basic: Base64 credentials in header per request. Same `UserDetailsService` for both.

38. **What is `UserDetailsService`?**  
    Interface to load user by username. We use `InMemoryUserDetailsManager` with one HR user.

39. **What does `{noop}` mean in the password?**  
    `DelegatingPasswordEncoder` prefix for plaintext. Production uses `{bcrypt}` or `BCryptPasswordEncoder`.

40. **What is CSRF and why disable it for `/api/**`?**  
    CSRF prevents cross-site form submission abuse. REST clients do not use browser cookies the same way — token APIs often disable CSRF or use JWT.

41. **What is `ROLE_HR` vs `hasRole('HR')`?**  
    Spring adds `ROLE_` prefix internally. `.roles("HR")` stores `ROLE_HR`. `@PreAuthorize("hasRole('HR')")` checks it.

42. **What is `SecurityContextHolder`?**  
    Thread-local storage of current `Authentication` after login.

43. **What is the difference between authentication and authorization?**  
    Authentication: *who are you?* Authorization: *what can you do?* Our config authenticates all `/api/**` users; role-based authorization is an optional exercise.

44. **Why `frameOptions(sameOrigin)` for H2?**  
    Allows H2 console iframe on same origin.

45. **How would you test secured endpoints?**  
    `spring-security-test` dependency (in `pom.xml`); `@WithMockUser(roles = "HR")` on tests; or `@AutoConfigureMockMvc` with `.with(httpBasic(...))`.

46. **What is JWT and when would you use it over sessions?**  
    Stateless signed token for APIs/SPAs. Sessions suit server-rendered apps like our Thymeleaf UI.

47. **What is OAuth2? Is it in this project?**  
    Delegated auth (Login with Google). Not in this project — we use local `UserDetailsService`.

48. **How do you store users in production instead of in-memory?**  
    `JdbcUserDetailsManager`, custom `UserDetailsService` loading from `users` table, or external IdP (Keycloak, Auth0).

---

### 3.5 Exception handling & service design

49. **Why throw `ResourceNotFoundException` from service, not controller?**  
    Controller should not know repository details. Service enforces “must exist” business rule.

50. **Why unchecked exception for not-found?**  
    No forced `throws` clause; works with `@ExceptionHandler`; cleaner service API.

51. **What HTTP status does `GlobalExceptionHandler` return for not found?**  
    `404 NOT_FOUND` with JSON `{ timestamp, status, message }`.

52. **How are validation errors handled?**  
    `MethodArgumentNotValidException` → 400 with first field error message.

53. **What is layered architecture in this project?**  
    Controller → Service → Repository → Database. DTOs at boundaries; entities inside service/persistence.

---

### 3.6 Spring AI (project-specific)

54. **How is Groq integrated?**  
    `spring-ai-openai-spring-boot-starter` with `base-url: https://api.groq.com/openai` and `GROQ_API_KEY` env var.

55. **What does `ChatClient` do in `AiEmployeeSearchService`?**  
    Sends natural language query to LLM; LLM returns JSON criteria; service parses and calls `EmployeeService.searchByCriteria()`.

56. **What is the AI search flow end-to-end?**  
    `POST /api/ai/search-employees` → `AiEmployeeSearchService` → Groq LLM → `EmployeeSearchCriteria` → JPA Specification → paginated results.

57. **What are risks of LLM-generated JSON?**  
    Invalid JSON, hallucinated fields — production apps need parsing validation and fallbacks.

---

### 3.7 Specifications & advanced JPA

58. **What is the Specification pattern?**  
    Composable, type-safe dynamic queries. `EmployeeSpecifications.fromCriteria()` builds `Predicate` list.

59. **What is `CriteriaBuilder` and `Predicate`?**  
    JPA Criteria API objects to build SQL WHERE clauses programmatically.

60. **Why `JoinType.LEFT` for department in specifications?**  
    Include employees even if department join is optional (defensive; our model requires department on create).

---

### 3.8 Maven & project structure

61. **What is `spring-boot-starter-parent`?**  
    BOM managing dependency versions — we use 3.4.1.

62. **What Java version does this project use?**  
    Java 17 (`<java.version>17</java.version>`).

63. **What is the package structure convention?**  
    `controller`, `service`, `repository`, `entity`, `dto`, `config`, `exception`, `specification`.

64. **What does `spring-boot-maven-plugin` do?**  
    Packages executable fat JAR; `java -jar` or `mvn spring-boot:run`.

---

### 3.9 Scenario / design questions (senior-style)

65. **A student gets 401 on every API call. Debug steps?**  
    Check auth header; verify username/password; confirm `/api/**` requires auth; try `curl -u hr@codekerdos.in:hr123 ...`.

66. **How would you add a new role `MANAGER` who can only read employees?**  
    Add user with `.roles("MANAGER")`; `@PreAuthorize("hasAnyRole('HR','MANAGER')")` on GET; restrict POST/DELETE to HR.

67. **How would you paginate department employees?**  
    Add endpoint with `Pageable`; repository method `findByDepartmentId(Long id, Pageable pageable)`.

68. **How do you prevent exposing stack traces in API errors?**  
    `@RestControllerAdvice` returns controlled JSON; set `server.error.include-stacktrace=never` in production.

69. **Monolith vs microservices — where is this app?**  
    Monolith: single JAR, all layers together. Appropriate for bootcamp scope.

70. **How would you deploy this to production?**  
    MySQL profile, BCrypt passwords, disable H2 console, HTTPS, secrets via env vars, JWT or OAuth2, proper CORS for SPA.

---

### 3.10 Quick-fire coding questions

71. Write the annotation to map `GET /api/departments`. → `@GetMapping` on `@RequestMapping("/api/departments")`.

72. How do you fetch employee by ID from DB? → `employeeRepository.findById(id)`.

73. How do you return 404 when not found? → `orElseThrow(() -> new ResourceNotFoundException(...))` + `@ExceptionHandler`.

74. How do you validate that name is not blank? → `@NotBlank` on DTO field + `@Valid` on controller param.

75. How do you allow unauthenticated access to one URL? → `.requestMatchers("/that-url").permitAll()` in `SecurityFilterChain`.

---

## Quick reference — demo credentials

| Field | Value |
|-------|-------|
| Username | `hr@codekerdos.in` |
| Password | `hr123` |
| Role | `HR` |
| Login page | http://localhost:8080/login |
| API example | `curl -u hr@codekerdos.in:hr123 http://localhost:8080/api/employees` |

---

*Document version: Week 3 — aligned with `week-02-employee-management` v2.0-SNAPSHOT, Spring Boot 3.4.1*
