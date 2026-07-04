# Week 3 · Class 2 — Sunday · Open Q&A

> **[← Week 3 Index](README.md)** · **Previous ← [Class 1 — Security](Class-1.md)**  
> **Coding folder:** `week-02-employee-management` (optional — only if a demo answers a question)

---

## HOW TO USE THIS FILE

**This session is Q&A — not a feature build day.**

- Students ask; you answer live  
- Use EMS codebase on screen when helpful  
- Keep answers **short and practical** — redirect deep dives to Week 4+  
- No mandatory new files or homework project  

---

## SESSION PLAN

| Block | Duration | What |
|-------|----------|------|
| Warm-up recap | 10 min | CSRF + SecurityConfig one-liner recap |
| Open floor Q&A | 50 min | Students ask anything Weeks 1–3 |
| Interview lightning | 15 min | Rapid-fire from question bank below |
| Week 4–5 preview | 5 min | Expense project (2-week plan) |

---

# BLOCK 1 — Warm-Up Recap (10 min)

### SAY

> "Saturday was security theory — CSRF, filter chain, why our EMS config looks the way it does.
> Today is **your** session. No new topics unless your questions need them."

### Quick fire — same as Saturday end

| Question | Expected answer |
|----------|-----------------|
| What is CSRF? | Forged browser request using your session cookie |
| Why CSRF ignored on `/api/**`? | Postman uses Basic Auth — not cookie session for APIs |
| Where is security configured? | `SecurityConfig.java` |
| Groq key storage? | `.env` — never commit |

### END THOUGHT

> "Open floor — who has a question from Week 1, 2, or Saturday?"

---

# FULL REQUEST FLOW — From `localhost:8080` to JWT (EMS Codebase)

> Use this section to walk students through **exactly** what happens from the moment they open the browser or fire a Postman request.  
> There are **two separate login paths** in this app. This explains both — and where JWT enters the picture.

---

## THE BIG PICTURE — Two Paths, One App

```
                    ┌─────────────────────────────────────────────┐
                    │           http://localhost:8080              │
                    └───────────────────┬─────────────────────────┘
                                        │
                          Spring Security Filter Chain
                                        │
               ┌────────────────────────┴────────────────────────┐
               │                                                   │
        BROWSER PATH                                       API PATH (Postman)
        GET /  or  /login                                  POST /api/auth/login
        (Thymeleaf + Session)                              (JSON + JWT Token)
               │                                                   │
        Session Cookie                                       Bearer Token
        ← stays in browser                                   ← you copy-paste it
```

> JWT is **only for the API path**. The browser uses a traditional session cookie.

---

## PATH 1 — Browser opens `localhost:8080`

### Step 1 — You type `http://localhost:8080` in browser

```
Browser: GET /
└─► Spring Security checks: is this user authenticated?
    └─► No session cookie found → NOT authenticated
    └─► SecurityConfig: anyRequest().authenticated()    [SecurityConfig.java : line 40]
    └─► Redirect 302 → /login
```

### Step 2 — Login page loads

```
Browser: GET /login
└─► SecurityConfig: .requestMatchers("/login").permitAll()  ← public, no auth needed
└─► PageController.login()                             [PageController.java : line 14]
    └─► return "login"
    └─► Thymeleaf renders templates/login.html         [login.html]
        └─► shows form: email + password fields
            action="/login"  method="post"
```

### Step 3 — You fill the form and click Sign In

```
Browser: POST /login
Body (form-encoded): username=hr@codekerdos.in&password=hr123
└─► Spring Security's built-in UsernamePasswordAuthenticationFilter intercepts this
    (NOT our AuthController — this is Spring's own filter)
    └─► calls AuthenticationManager.authenticate(username, password)
    └─► AuthenticationManager → UserDetailsService.loadUserByUsername()
                                                       [SecurityConfig.java : line 72]
        └─► finds user "hr@codekerdos.in" in InMemoryUserDetailsManager
        └─► BCryptPasswordEncoder.matches("hr123", storedHash)
                                                       [SecurityConfig.java : line 62]
            └─► ✅ match → creates authenticated session
            └─► ❌ mismatch → redirect to /login?error
```

### Step 4 — Successful login → home page

```
└─► SecurityConfig: .defaultSuccessUrl("/", true)      [SecurityConfig.java : line 46]
└─► Redirect 302 → /
└─► PageController.home()                              [PageController.java : line 9]
    └─► return "home"
    └─► Thymeleaf renders templates/home.html

Browser now holds a SESSION COOKIE (JSESSIONID)
Every future browser request carries this cookie automatically.
JWT is NOT used here at all.
```

---

## PATH 2 — Postman / API call (where JWT lives)

### Step 1 — Login via API to get a token

```
Postman: POST http://localhost:8080/api/auth/login
Headers: Content-Type: application/json
Body:   { "username": "hr@codekerdos.in", "password": "hr123" }

└─► SecurityConfig: /api/auth/** → permitAll()         [SecurityConfig.java : line 38]
    (no auth required to reach the login endpoint)
└─► AuthController.login()                             [AuthController.java : line 32]
    └─► authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(username, password)
        )
    └─► Same BCrypt check as PATH 1 ↑
    └─► ✅ match → authentication.getName() = "hr@codekerdos.in"
    └─► JwtService.generateToken("hr@codekerdos.in")   [JwtService.java : line 26]
        └─► Jwts.builder()
                .subject("hr@codekerdos.in")
                .issuedAt(now)
                .expiration(now + 3,600,000 ms)        ← 1 hour, from application.yml
                .signWith(HMAC-SHA key)                ← key built from jwt.secret
                .compact()
        └─► returns "eyJhbGci..."

Response: { "token": "eyJhbGci..." }   ← copy this
```

### Step 2 — Use the token on every API call

```
Postman: GET http://localhost:8080/api/employees
Headers: Authorization: Bearer eyJhbGci...

└─► Spring Security Filter Chain runs in order:
    [1] JwtAuthFilter.doFilterInternal()               [JwtAuthFilter.java : line 27]
        └─► reads header "Authorization"
        └─► header starts with "Bearer " → extract token (substring after index 7)
        └─► JwtService.extractUsername(token)          [JwtService.java : line 35]
            └─► Jwts.parser()
                    .verifyWith(key)       ← checks HMAC signature
                    .build()
                    .parseSignedClaims()   ← also auto-checks expiry
                    .getPayload()
                    .getSubject()          ← "hr@codekerdos.in"
        └─► userDetailsService.loadUserByUsername("hr@codekerdos.in")
        └─► UsernamePasswordAuthenticationToken(user, null, [ROLE_HR])
        └─► SecurityContextHolder.getContext().setAuthentication(auth)
            ↑ request is now "logged in" for this thread only

    [2] chain.doFilter() → request continues
        └─► SecurityConfig: /api/** → authenticated()  ← ✅ context is set
        └─► EmployeeController handles the request
        └─► Response returned

If token missing / bad / expired:
    └─► Exception caught silently (line 41 JwtAuthFilter)
    └─► SecurityContext stays empty
    └─► SecurityConfig: /api/** → authenticated() → ❌ → 401 Unauthorized
```

---

## SIDE BY SIDE — Both paths compared

| | Browser Path | API / Postman Path |
|---|---|---|
| Login URL | `POST /login` | `POST /api/auth/login` |
| Who handles login | Spring's built-in filter | Our `AuthController` |
| Credential format | HTML form (form-encoded) | JSON body |
| Proof of identity after login | Session cookie (JSESSIONID) | JWT Bearer token |
| Where stored | Browser auto-manages cookie | You copy-paste in Postman header |
| Expires | Server session timeout | 1 hour (`jwt.expiry-ms`) |
| JWT involved? | ❌ No | ✅ Yes |

---

# JWT FLOW — Password → Token → Protected API (EMS Codebase)

> Use this section when a student asks *"where does the password go?"* or *"how does JWT actually work in our code?"*  
> Every step maps to a real file in `week-02-employee-management`.

---

## PART A — Login (Generating the Token)

```
POST /api/auth/login
Body: { "username": "hr@codekerdos.in", "password": "hr123" }
```

### Step-by-step

```
1. Request arrives
   └─► AuthController.login()                         [AuthController.java : line 32]
       └─► authenticationManager.authenticate(
               UsernamePasswordAuthenticationToken(username, password)
           )

2. AuthenticationManager delegates to UserDetailsService
   └─► SecurityConfig.userDetailsService()            [SecurityConfig.java : line 72]
       └─► InMemoryUserDetailsManager holds:
               username = "hr@codekerdos.in"
               password = BCrypt("hr123")             ← stored as hash, never plain

3. Password check
   └─► BCryptPasswordEncoder.matches(
           rawPassword  = "hr123"      ← what you typed
           storedHash   = "$2a$..."    ← what was stored
       )                                              [SecurityConfig.java : line 62]
       └─► ✅ match → Authentication object returned
       └─► ❌ no match → AuthenticationException → 401

4. Token creation
   └─► JwtService.generateToken(username)             [JwtService.java : line 26]
       └─► Jwts.builder()
               .subject("hr@codekerdos.in")
               .issuedAt(now)
               .expiration(now + 3 600 000 ms = 1 hour)
               .signWith(HMAC-SHA key from jwt.secret)
               .compact()
       └─► returns a signed JWT string

5. Response
   └─► { "token": "eyJhbGci..." }
```

---

## PART B — Using the Token (Every Subsequent Request)

```
GET /api/employees
Header: Authorization: Bearer eyJhbGci...
```

### Step-by-step

```
1. Request enters Spring's filter chain
   └─► JwtAuthFilter.doFilterInternal()               [JwtAuthFilter.java : line 27]
       └─► reads header: "Authorization: Bearer <token>"
       └─► strips "Bearer " prefix → raw token string

2. Token verification
   └─► JwtService.extractUsername(token)              [JwtService.java : line 35]
       └─► Jwts.parser()
               .verifyWith(key)          ← checks HMAC signature
               .build()
               .parseSignedClaims(token) ← also checks expiry automatically
               .getPayload()
               .getSubject()             ← returns "hr@codekerdos.in"
       └─► ❌ bad signature or expired → exception caught silently (line 41)
                                        → SecurityContext stays empty → 401

3. Load UserDetails
   └─► userDetailsService.loadUserByUsername("hr@codekerdos.in")
       └─► returns UserDetails with roles ["ROLE_HR"]

4. Set authentication in context
   └─► UsernamePasswordAuthenticationToken(user, null, authorities)
   └─► SecurityContextHolder.getContext().setAuthentication(auth)
       └─► request is now "logged in" for this thread

5. Filter chain continues
   └─► chain.doFilter(request, response)
   └─► SecurityConfig rule:  /api/** → .authenticated()   → ✅ allowed
                                                           [SecurityConfig.java : line 39]
   └─► Controller runs, response returned
```

---

## PART C — What Protects What (SecurityConfig)

```java
// SecurityConfig.java — lines 37–42
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**", "/login", "/css/**", "/h2-console/**").permitAll()
    .requestMatchers("/api/**").authenticated()   // ← JWT required here
    .anyRequest().authenticated()
)
.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
```

| URL pattern | Auth needed? | Who handles it |
|-------------|-------------|----------------|
| `POST /api/auth/login` | ❌ No | `AuthController` — issues token |
| `GET /api/employees` | ✅ Yes — JWT | `JwtAuthFilter` → `EmployeeController` |
| `POST /api/employees` | ✅ Yes — JWT | `JwtAuthFilter` → `EmployeeController` |
| `/h2-console/**` | ❌ No (dev only) | Direct |

---

## PART D — Key Classes at a Glance

| Class | File | Single job |
|-------|------|-----------|
| `JwtService` | `config/JwtService.java` | **Create** and **verify** tokens using `jwt.secret` |
| `JwtAuthFilter` | `config/JwtAuthFilter.java` | **Intercept** every request, validate token, set context |
| `AuthController` | `controller/AuthController.java` | **Login endpoint** — takes username+password, returns token |
| `SecurityConfig` | `config/SecurityConfig.java` | **Wires everything** — rules, filter order, BCrypt, users |

---

## PART E — One-liner summaries for interview

| Question | Answer |
|----------|--------|
| Where is the password checked? | `AuthenticationManager` → `BCryptPasswordEncoder.matches()` in `SecurityConfig` |
| Where is the token created? | `JwtService.generateToken()` — called from `AuthController` |
| Where is the token validated on every request? | `JwtAuthFilter.doFilterInternal()` — runs before every `/api/**` call |
| What happens if token is expired? | Exception silently caught, `SecurityContext` not set → Spring returns 401 |
| Why is `/api/auth/login` public? | `permitAll()` in `SecurityConfig` — you need to reach login without a token |
| What hashing algorithm? | BCrypt — one-way, salted — never reversible |

---

# BLOCK 2 — Open Q&A (50 min)

### SAY — ground rules

> "One question at a time. If it's off-topic we park it for after class.
> If it's 'how do I fix my laptop' — quick answer or follow up on Slack."

### How to run

1. Ask for hands / chat queue  
2. Repeat question aloud so whole class hears  
3. Answer in **≤ 3 minutes** when possible  
4. If needs demo → open EMS project and show **one** file or Postman call  
5. If too deep → *"Great question — we'll touch that in Week 4 JWT / Week 5 RAG"*

---

## Question bank — Security (likely after Saturday)

| Question | Short answer |
|----------|--------------|
| Can I disable CSRF completely? | Yes for pure JWT/stateless APIs; risky for cookie-based browser apps |
| What's the difference between 401 and 403? | 401 = not authenticated; 403 = authenticated but not allowed |
| Is HTTP Basic safe? | OK for dev/HTTPS; production prefers OAuth2/JWT over HTTPS |
| Why `{noop}` password? | Demo only — encodes plain text for Spring Security teaching |
| What is BCrypt? | One-way password hash with salt — use in real apps (Week 4) |
| SQL injection — are we safe? | JPA parameterized queries help; never concat SQL strings |
| Should H2 console be public? | **Never in production** — dev convenience only |

---

## Question bank — Spring Boot / EMS (Weeks 1–2)

| Question | Short answer |
|----------|--------------|
| `@RestController` vs `@Controller`? | REST returns JSON body; Controller often returns view name (Thymeleaf) |
| What is `@Transactional`? | All DB ops in method succeed or roll back together |
| DTO vs Entity? | Entity = DB shape; DTO = API contract — don't expose entities directly |
| What is Pageable? | Page number + size + sort for large lists |
| H2 vs MySQL? | H2 in-memory for dev; MySQL persistent for production-shaped demo |
| How does NL search work? | Groq → JSON criteria → JPA Specification → DB query |
| Why layered architecture? | Controller / Service / Repository — separation of concerns |
| What is IoC / DI? | Spring creates and injects beans — from Week 1 |

---

## Question bank — Spring AI / Groq

| Question | Short answer |
|----------|--------------|
| Are we training AI? | **No** — calling Groq hosted LLM via ChatClient |
| Why JSON from LLM? | Parseable, testable — Java controls DB, not the model |
| 401 from Groq? | Wrong/missing `GROQ_API_KEY` in `.env` |
| 429 from Groq? | Rate limit — wait 30s (free tier) |
| Can AI replace SQL? | **No** — AI suggests filters; Java runs safe queries |

---

## Question bank — Tools / Setup

| Question | Short answer |
|----------|--------------|
| Maven red imports? | Reload Maven project; check Java 17 |
| F5 vs Run button? | F5 loads `.env` from launch.json — preferred |
| Postman Basic Auth not working? | `hr@codekerdos.in` / `hr123` on Authorization tab |
| MySQL connection refused? | Start MySQL service; check profile `mysql` |
| Accidentally committed `.env`? | Revoke Groq key immediately; remove from Git history |

---

### STUCK? — if room is quiet

Pick 2–3 questions from the bank above and ask the class *"Can anyone answer this?"* before you explain.

---

# BLOCK 3 — Interview Lightning Round (15 min)

### SAY

> "Quick answers — interview style. 30 seconds each."

| # | Question |
|---|----------|
| 1 | Explain CSRF in one sentence |
| 2 | JWT vs session — one difference |
| 3 | Why use DTOs in REST APIs? |
| 4 | What does `@SpringBootApplication` include? |
| 5 | How does Spring Data know `findByTeam`? |
| 6 | What is PromptTemplate used for? |
| 7 | Why never commit API keys? |
| 8 | What is `@PreAuthorize`? (preview — Week 4) |

Call on students. Fill gaps briefly.

---

# BLOCK 4 — Week 4–5 Preview (5 min)

### SAY

> "**Project #2 spans two weeks** — not one weekend.
> **Week 4** = new folder `week-04-expense-approval` — domain + JWT + employee submit (Sat–Sun).
> **Week 5** = manager approve/reject + AI categorization + fraud → **Project #2 complete**."

### Contents — what's coming

| Week | Sat | Sun |
|------|-----|-----|
| **4** | Enums, entities, ExpenseService | JWT, login, employee APIs |
| **5** | `@PreAuthorize`, approve/reject, summary | AI + `@Async`, manager digest |

### DRAW

```
Week 3 (this week)     →  Security + Q&A  (EMS)
Week 4                 →  Expense part 1 (domain + JWT)
Week 5                 →  Expense part 2 (workflow + AI) → Project #2 done
Week 6+                →  Booking + RAG
```

### Homework (optional)

| # | Task |
|---|------|
| 1 | Fix any open bugs in your EMS fork |
| 2 | Skim [`docs/WEEK-4/Class-1.md`](../WEEK-4/Class-1.md) — don't code ahead |
| 3 | Ensure Groq `.env` still works — `GET /api/ai/greet?name=Test` |

### END THOUGHT

> "Week 3 = security + clear doubts. Weeks 4–5 = build Project #2 properly."

---

*CodeKerdos.in · Week 3 Class 2 · Q&A session — no mandatory coding*
