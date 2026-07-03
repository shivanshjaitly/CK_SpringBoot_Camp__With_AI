# Week 4 · Class 2 — Sunday · JWT Auth + Employee Submit Flow

> **[← Week 4 Index](README.md)** · **Previous ← [Class 1 — Domain](Class-1.md)**  
> **Coding folder:** `week-04-expense-approval`  
> **Next week → [Week 5 — Approval + AI](../WEEK-5/README.md)**

---

## CLASS 2 — TOPICS

| # | Topic | Code? |
|---|-------|-------|
| 1 | Saturday recap + JWT architecture (vs Basic Auth) | Talk + draw |
| 2 | JwtService + JwtAuthFilter + SecurityConfig | ✅ **security/** + **config/** |
| 3 | AuthController + DataLoader (seed users) | ✅ **controller/** + **service/** |
| 4 | ExpenseController — employee endpoints | ✅ **controller/** |
| 5 | GlobalExceptionHandler | ✅ **exception/** |
| 6 | Full demo + homework | Run |

**Session goal:** Login returns JWT. Employee can submit expense and list own items. **Manager + AI = Week 5.**

**Time split:**

| Block | Duration | Topics |
|-------|----------|--------|
| Recap + JWT theory | 20 min | 1 |
| JWT implementation | 40 min | 2–3 |
| Employee APIs + errors | 25 min | 4–5 |
| Demo + wrap | 10 min | 6 |

---

## HOW TO RUN — VS Code / Cursor

| Step | Action |
|------|--------|
| 1 | `cp week-04-expense-approval/.env.example week-04-expense-approval/.env` |
| 2 | Add `GROQ_API_KEY` + `JWT_SECRET=codekerdos-demo-secret-change-in-prod-min-32-chars` |
| 3 | **F5** → **Week 4 EAS — Run with Groq (H2)** |
| 4 | `POST /api/auth/login` → copy token → `Authorization: Bearer <token>` |

### Demo users (seeded on startup)

| Email | Password | Role |
|-------|----------|------|
| `employee@codekerdos.in` | `emp123` | EMPLOYEE |
| `manager@codekerdos.in` | `mgr123` | MANAGER |
| `admin@codekerdos.in` | `adm123` | ADMIN |

---

# TOPIC 1 — Saturday Recap + JWT Architecture

### Quick fire questions

| Question | Expected answer |
|----------|-----------------|
| What entities did we create? | AppUser, Expense |
| Initial expense status? | PENDING |
| Three enums? | Role, ExpenseStatus, ExpenseCategory |
| What's today? | JWT security + employee REST endpoints |

### SAY

> "Week 2 EMS used **HTTP Basic**. Production apps use **JWT** — login once, Bearer token on every call.
> **Stateless** — no server session table."

### Contents

| | HTTP Basic (Week 2) | JWT (Week 4) |
|--|---------------------|--------------|
| Login | Password every request | Login once → token |
| Header | `Authorization: Basic ...` | `Authorization: Bearer eyJ...` |
| Passwords | `{noop}` demo | **BCrypt** hash |

### DRAW

```
POST /api/auth/login → AuthService → JwtService.sign → { token }
GET /api/expenses/mine + Bearer token → JwtAuthFilter → Controller
```

### END THOUGHT

> "Topic 2 — build JwtService, filter, SecurityConfig."

---

# TOPIC 2 — JwtService + JwtAuthFilter + SecurityConfig

### YOU DO

Add DTOs if not done: **`LoginRequest.java`**, **`AuthResponse.java`** — see [Class-1.md](Class-1.md) Topic 7.

---

**`security/JwtService.java`**

```java
package in.codekerdos.expense.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    public String generateToken(String email, Map<String, Object> extraClaims) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
```

> **CODE WALKTHROUGH — JwtService:**
>
> | Method | What it does | Say aloud |
> |--------|--------------|-----------|
> | `generateToken(email, extraClaims)` | Builds a signed JWT string | "We embed the email as `subject` and put role in extra claims — both travel inside the token" |
> | `.subject(email)` | Standard JWT field — who this token is for | "We'll read this back in the filter to know who is making the request" |
> | `.expiration(new Date(...))` | Token dies after 24 hours | "After expiry, the user must log in again — old tokens are permanently invalid" |
> | `.signWith(getSigningKey())` | HMAC-SHA256 signature using our secret | "If anyone tampers with the token body, the signature check fails — token rejected" |
> | `getSigningKey()` | Converts `JWT_SECRET` string → `SecretKey` object | "The secret must be at least 32 characters for HMAC-256 — that's why we set a long string in `.env`" |
> | `isTokenValid(token, userDetails)` | Checks email match AND not expired | "Two checks: is this token for this user, and is it still fresh?" |
> | `extractEmail(token)` | Reads `subject` field from token payload | "No DB call — the email is already inside the token. That's the stateless benefit." |

---

**`security/JwtAuthFilter.java`**

```java
package in.codekerdos.expense.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String userEmail = jwtService.extractEmail(jwt);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

> **CODE WALKTHROUGH — JwtAuthFilter:**
>
> **What is `OncePerRequestFilter`?**  
> Spring ensures this filter runs exactly once per HTTP request — no duplicate processing.
>
> **Walk through the flow step by step:**
>
> ```
> Request arrives
>   → Read "Authorization" header
>   → No header / not "Bearer "? → skip filter, continue chain (public route)
>   → Extract JWT after "Bearer "
>   → Extract email from token (no DB yet)
>   → Load user from DB by email
>   → Validate token (email match + not expired)
>   → Set authentication in SecurityContext
>   → Continue to Controller
> ```
>
> | Line | Say aloud |
> |------|-----------|
> | `authHeader.substring(7)` | "`Bearer ` is 7 characters. We chop it off to get the raw JWT string" |
> | `SecurityContextHolder.getContext().getAuthentication() == null` | "Don't re-authenticate if already set — prevents double processing" |
> | `new UsernamePasswordAuthenticationToken(userDetails, null, authorities)` | "Three args: principal, credentials (null — JWT already proved identity), roles. This is what `authentication.getName()` reads in the controller." |
> | `filterChain.doFilter(request, response)` | "Always call this at the end — passes the request to the next filter or controller" |

---

**`config/SecurityConfig.java`**

```java
package in.codekerdos.expense.config;

import in.codekerdos.expense.repository.AppUserRepository;
import in.codekerdos.expense.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AppUserRepository appUserRepository;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(AppUserRepository appUserRepository, JwtAuthFilter jwtAuthFilter) {
        this.appUserRepository = appUserRepository;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> appUserRepository.findByEmail(username)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .roles(user.getRole().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

> **CODE WALKTHROUGH — SecurityConfig:**
>
> | Setting | Why | Say aloud |
> |---------|-----|-----------|
> | `csrf.disable()` | JWT APIs are stateless — no session cookie to protect | "Week 3 recap: CSRF attacks exploit session cookies. JWT has no cookies — CSRF doesn't apply. Safe to disable." |
> | `SessionCreationPolicy.STATELESS` | Spring must not create HttpSession | "Every request is self-contained. The token is the session. No server memory used." |
> | `.permitAll()` on `/api/auth/**` | Login must work without a token | "Chicken-and-egg: you need to be able to log in before you have a token" |
> | `.permitAll()` on `/h2-console/**` | H2 browser console access during dev | "Development only — remove or restrict this in production" |
> | `.anyRequest().authenticated()` | Everything else requires a valid JWT | "One rule covers all future endpoints — you don't whitelist every URL" |
> | `addFilterBefore(jwtAuthFilter, ...)` | Our filter runs before Spring's default auth filter | "We intercept the request first, validate JWT, set the user — then Spring's filter sees it's already authenticated" |
> | `headers.frameOptions().disable()` | H2 console uses iframes — blocked by default | "H2 console won't load without this. Remove in production." |
> | `BCryptPasswordEncoder` bean | Shared password encoder across the app | "One bean, injected everywhere. Never create `new BCryptPasswordEncoder()` inline." |
> | `@EnableMethodSecurity` | Enables `@PreAuthorize` on service/controller methods | "Week 5 will use `@PreAuthorize(\"hasRole('MANAGER')\")` — this annotation activates that feature" |

### STUCK?

| Problem | Fix |
|---------|-----|
| Invalid JWT signature | `JWT_SECRET` min 32 chars in `.env` |
| 403 on all routes | Check JwtAuthFilter order |

### END THOUGHT

> "Security wired. Topic 3 — login + seed users."

---

# TOPIC 3 — AuthController + DataLoader

### YOU DO

**`service/AuthService.java`**

```java
package in.codekerdos.expense.service;

import in.codekerdos.expense.dto.AuthResponse;
import in.codekerdos.expense.dto.LoginRequest;
import in.codekerdos.expense.entity.AppUser;
import in.codekerdos.expense.repository.AppUserRepository;
import in.codekerdos.expense.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository appUserRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        Map<String, Object> extraClaims = Map.of("role", user.getRole().name());
        String token = jwtService.generateToken(user.getEmail(), extraClaims);

        return new AuthResponse(token, user.getEmail(), user.getRole().name(), jwtService.getExpirationMs());
    }
}
```

> **CODE WALKTHROUGH — AuthService:**
>
> | Line | Say aloud |
> |------|-----------|
> | `appUserRepository.findByEmail(...).orElseThrow(...)` | "We look up the user first. If the email doesn't exist, we throw `BadCredentialsException` — same error as wrong password. Don't reveal which one is wrong — security." |
> | `passwordEncoder.matches(rawPassword, hashedPassword)` | "BCrypt is one-way. We can't decrypt. `matches()` hashes the input again and compares — that's how BCrypt verification works." |
> | `Map.of("role", user.getRole().name())` | "We embed the role inside the JWT payload. The client can read it (base64 decode) to show the right UI." |
> | `new AuthResponse(token, ...)` | "We return the token plus metadata so the client knows when it expires without decoding the JWT themselves." |
>
> **Why the same error message for both bad email and bad password?**  
> If we say *"email not found"* vs *"wrong password"*, an attacker knows which emails exist in your system. Always say *"invalid credentials"* for both.

---

**`controller/AuthController.java`**

```java
package in.codekerdos.expense.controller;

import in.codekerdos.expense.dto.AuthResponse;
import in.codekerdos.expense.dto.LoginRequest;
import in.codekerdos.expense.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
```

> **CODE WALKTHROUGH — AuthController:**
>
> | Annotation | Say aloud |
> |-----------|-----------|
> | `@RestController` | "Combines `@Controller` + `@ResponseBody` — every method return value becomes JSON automatically" |
> | `@RequestMapping("/api/auth")` | "Base path for all auth endpoints in this class. `/api/auth/login` = `"/api/auth"` + `"/login"`" |
> | `@RequestBody @Valid LoginRequest` | "`@RequestBody` deserializes JSON into the record. `@Valid` triggers validation annotations — `@NotBlank`, `@Email`. If validation fails, Spring returns 400 before reaching our code." |

---

**`config/DataLoader.java`** — seeds three demo users on startup

```java
package in.codekerdos.expense.config;

import in.codekerdos.expense.entity.AppUser;
import in.codekerdos.expense.enums.Role;
import in.codekerdos.expense.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner seedUsers(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!appUserRepository.existsByEmail("employee@codekerdos.in")) {
                AppUser employee = new AppUser();
                employee.setEmail("employee@codekerdos.in");
                employee.setPassword(passwordEncoder.encode("emp123"));
                employee.setFullName("Demo Employee");
                employee.setRole(Role.EMPLOYEE);
                appUserRepository.save(employee);
            }

            if (!appUserRepository.existsByEmail("manager@codekerdos.in")) {
                AppUser manager = new AppUser();
                manager.setEmail("manager@codekerdos.in");
                manager.setPassword(passwordEncoder.encode("mgr123"));
                manager.setFullName("Demo Manager");
                manager.setRole(Role.MANAGER);
                appUserRepository.save(manager);
            }

            if (!appUserRepository.existsByEmail("admin@codekerdos.in")) {
                AppUser admin = new AppUser();
                admin.setEmail("admin@codekerdos.in");
                admin.setPassword(passwordEncoder.encode("adm123"));
                admin.setFullName("Demo Admin");
                admin.setRole(Role.ADMIN);
                appUserRepository.save(admin);
            }
        };
    }
}
```

> **CODE WALKTHROUGH — DataLoader:**
>
> | Pattern | Say aloud |
> |---------|-----------|
> | `CommandLineRunner` | "Spring Boot runs this method once after all beans are ready — perfect for seeding test data" |
> | `existsByEmail(...)` check | "If we restart the app, we don't want to save duplicate users and crash on the unique email constraint" |
> | `passwordEncoder.encode("emp123")` | "We never store plain text passwords. BCrypt hashes `emp123` to something like `$2a$10$...` — nobody can reverse it" |
> | Three roles seeded | "One of each role so we can test all three security paths in Postman" |

### RUN

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{ "email": "employee@codekerdos.in", "password": "emp123" }
```

Expected: `{ "token": "eyJ...", "role": "EMPLOYEE", ... }`

### END THOUGHT

> "Login works. Topic 4 — expense endpoints."

---

# TOPIC 4 — ExpenseController (Employee Endpoints)

### YOU DO

**`controller/ExpenseController.java`**

```java
package in.codekerdos.expense.controller;

import in.codekerdos.expense.dto.ExpenseResponse;
import in.codekerdos.expense.dto.SubmitExpenseRequest;
import in.codekerdos.expense.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> submit(
            @RequestBody @Valid SubmitExpenseRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.submit(request, authentication.getName()));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ExpenseResponse>> mine(Authentication authentication) {
        return ResponseEntity.ok(expenseService.findMine(authentication.getName()));
    }
}
```

> **CODE WALKTHROUGH — ExpenseController:**
>
> | Line | Say aloud |
> |------|-----------|
> | `Authentication authentication` parameter | "Spring injects this automatically from `SecurityContextHolder`. Our `JwtAuthFilter` put it there. We never pass usernames ourselves — Spring reads the token." |
> | `authentication.getName()` | "Returns the `subject` we set in `JwtService.generateToken()` — the user's email. This is how we know who is calling." |
> | `HttpStatus.CREATED` (201) | "Creating a resource returns 201, not 200. Semantically correct — Postman and frontend code can distinguish create vs retrieve." |
> | `@Valid` on request body | "Same as login — validation fires before `expenseService.submit()` is called. Bad data never reaches business logic." |
>
> **Show students the flow end to end:**
> ```
> Postman → POST /api/expenses + Bearer token
>   → JwtAuthFilter validates token → sets email in SecurityContext
>   → ExpenseController.submit() called
>   → authentication.getName() returns "employee@codekerdos.in"
>   → ExpenseService.submit() looks up user, creates Expense, saves to H2
>   → returns ExpenseResponse JSON with status PENDING
> ```

### RUN

```http
POST http://localhost:8080/api/expenses
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Flight to Mumbai",
  "description": "Client meeting",
  "amount": 8500.00,
  "expenseDate": "2026-06-15"
}
```

```http
GET http://localhost:8080/api/expenses/mine
Authorization: Bearer <token>
```

No token → **401**

### END THOUGHT

> "Employee flow live. Topic 5 — error handling."

---

# TOPIC 5 — GlobalExceptionHandler

### YOU DO

**`exception/GlobalExceptionHandler.java`**

```java
package in.codekerdos.expense.exception;

import in.codekerdos.expense.service.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid email or password", Instant.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        Map<String, Object> body = Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "message", "Validation failed",
                "errors", fieldErrors,
                "timestamp", Instant.now()
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "An unexpected error occurred", Instant.now()));
    }

    public record ErrorResponse(int status, String message, Instant timestamp) {}
}
```

> **CODE WALKTHROUGH — GlobalExceptionHandler:**
>
> **What is `@RestControllerAdvice`?**  
> A global interceptor that catches exceptions thrown from any controller.  
> Without it, Spring returns a default HTML error page — ugly and leaks stack traces.  
> Say: *"One class handles all errors for the whole application — single place to change error format."*
>
> | Handler | Exception | HTTP status | Say aloud |
> |---------|-----------|-------------|-----------|
> | `handleNotFound` | `ResourceNotFoundException` | 404 | "User or expense not found in DB — we throw this from the service, it lands here" |
> | `handleBadCredentials` | `BadCredentialsException` | 401 | "Wrong email or password at login. Note: we return generic 'Invalid email or password' — never reveal which is wrong." |
> | `handleValidation` | `MethodArgumentNotValidException` | 400 | "Fires when `@Valid` rejects input. We collect all field errors into a map so the client knows exactly what to fix." |
> | `handleGeneral` | `Exception` | 500 | "Catch-all safety net. Never expose stack traces in production — we return a generic message." |
>
> **Validation error response shape:**  
> When you send `amount: -5`, the client gets:
> ```json
> {
>   "status": 400,
>   "message": "Validation failed",
>   "errors": {
>     "amount": "Amount must be positive"
>   },
>   "timestamp": "2026-06-15T10:30:00Z"
> }
> ```
> That `"Amount must be positive"` message comes directly from `@DecimalMin(message = "Amount must be positive")` in `SubmitExpenseRequest`.

### END THOUGHT

> "Topic 6 — demo and Week 5 preview."

---

# TOPIC 6 — Full Demo + Homework

### Demo checklist

```
✅ 1. POST /api/auth/login (employee) → JWT
✅ 2. POST /api/expenses (Bearer) → PENDING
✅ 3. GET  /api/expenses/mine → list
✅ 4. Login as manager → different role in response
✅ 5. GET /api/expenses/mine (no auth) → 401
✅ 6. H2 console → users + expenses tables
```

### Homework

| # | Task |
|---|------|
| 1 | Submit 3 expenses as employee |
| 2 | Test all 3 demo logins |
| 3 | Read [Week 5 Class-1](../WEEK-5/Class-1.md) preview — `@PreAuthorize` |
| 4 | Push to GitHub — no `.env` |

### SAY — Week 5 preview

> "**Week 5 Saturday** = manager approve/reject, admin view all, date summary, own-expense rule.
> **Week 5 Sunday** = AI categorization, fraud flags, `@Async`, manager summary.
> **Project #2 complete** end of Week 5."

### END THOUGHT

> "Week 4 done: domain + JWT + employee submit. Week 5 = workflow + AI."

---

## QUICK REFERENCE

| Problem | Fix |
|---------|-----|
| 401 on APIs | Login first; Bearer header |
| Token expired | Login again |
| Users table empty | Check DataLoader |

---

## WEEK 4 CLASS 2 — Interview Quick Reference

| Question | Answer |
|----------|--------|
| JWT vs session? | JWT stateless signed token |
| Why BCrypt? | One-way salted password hash |
| What is Bearer token? | `Authorization: Bearer <jwt>` |
| What does JwtAuthFilter do? | Validates token, sets SecurityContext |

---

*CodeKerdos.in · Week 4 Class 2 · JWT + employee flow — approval in Week 5*
