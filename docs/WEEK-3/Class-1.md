# Week 3 · Class 1 — Saturday · Web Security & CSRF

> **[← Week 3 Index](README.md)** · **Next → [Class 2 — Q&A](Class-2.md)**  
> **Coding folder:** `week-02-employee-management` (same EMS — **theory + small SecurityConfig discussion**)

---

## HOW TO USE THIS FILE

This is a **security lecture week** — lighter on live coding, heavier on concepts.

For each topic:

1. **SAY** — read aloud  
2. **DRAW** — whiteboard  
3. **YOU DO** — open existing EMS `SecurityConfig.java` and point at lines  
4. **DEMO** (optional) — Postman / browser only where noted  

**No new project folder this week.** Students keep using EMS from Week 2.

---

## CLASS 1 — TOPICS

| # | Topic | Code? |
|---|-------|-------|
| 1 | Week 2 recap — what Security we already have | Talk |
| 2 | Why security matters (layman + interview) | Talk |
| 3 | Spring Security filter chain | Draw |
| 4 | **CSRF — what it is** | Talk + draw |
| 5 | CSRF in our EMS `SecurityConfig` | Read code |
| 6 | API (Postman) vs browser (form login) | Talk |
| 7 | Security headers — `frameOptions` | Read code |
| 8 | Passwords & secrets — `{noop}` vs BCrypt, `.env` | Talk |
| 9 | Quick demo — CSRF on protected route | Optional demo |
| 10 | Wrap + Week 4 preview | Talk |

**Time split:**

| Block | Duration | Topics |
|-------|----------|--------|
| Recap + why security | 15 min | 1, 2 |
| Filter chain + CSRF | 35 min | 3, 4, 5 |
| API vs browser + headers | 20 min | 6, 7 |
| Secrets + demo + wrap | 15 min | 8, 9, 10 |

---

## HOW TO RUN — VS Code / Cursor

Same as Week 2 — open existing EMS project:

| Step | Action |
|------|--------|
| 1 | **F5** → **Week 2 EMS — Run with Groq (H2)** or **Run with MySQL** |
| 2 | Browser: `http://localhost:8080/login` |
| 3 | Postman: Basic Auth `hr@codekerdos.in` / `hr123` |

Full steps: [`docs/WEEK-2/Class-1.md`](../WEEK-2/Class-1.md) → **HOW TO RUN**

---

# TOPIC 1 — Week 2 Recap: Security We Already Have

### SAY

> "Week 2 we added Spring Security to EMS — HTTP Basic for Postman, form login for browser.
> Today we **understand what that code actually protects** — especially **CSRF**.
> We are not building a new app this week."

### Quick fire questions

| Question | Expected answer |
|----------|-----------------|
| What file configures security? | `config/SecurityConfig.java` |
| Demo login? | `hr@codekerdos.in` / `hr123` |
| What blocks unauthenticated API calls? | `authorizeHttpRequests` — `/api/**` needs auth |
| Why permit `/h2-console/**`? | Dev database UI — locked down in production |

### YOU DO

Open `week-02-employee-management/.../config/SecurityConfig.java` on screen.

### END THOUGHT

> "You already use Security — today we learn **why** each line exists."

---

# TOPIC 2 — Why Security Matters

### SAY

> "Your API holds HR data — names, teams, salaries context.
> **Security = who can access what + protecting against tricks**, not just a login form.
> Interviewers ask CSRF, auth, and secrets — this class covers the basics."

### Contents (board — 5 min)

| Threat | One-line meaning |
|--------|------------------|
| Broken auth | Anyone hits `/api/employees` without login |
| CSRF | Evil site tricks **logged-in browser** into doing something |
| Leaked secrets | API keys in GitHub — bots find them in minutes |
| XSS | Injected script steals session (mention only — Week 3 scope is CSRF) |

### END THOUGHT

> "Spring Security handles most of this if we configure it correctly. Topic 3 — the filter chain."

---

# TOPIC 3 — Spring Security Filter Chain

### SAY

> "Every HTTP request passes through a **chain of filters** before your Controller.
> Login, JWT, CSRF — all filters. You configure rules in `SecurityFilterChain`."

### DRAW

```
HTTP Request
     ↓
SecurityFilterChain
  ├── CsrfFilter
  ├── AuthenticationFilter  (Basic / Form / JWT)
  ├── AuthorizationFilter   (roles, paths)
  └── ...
     ↓
Your @RestController
```

**Layman:** Airport security — bag scan, ID check, boarding pass — then you reach the gate (controller).

### YOU DO

Point at `securityFilterChain` bean in `SecurityConfig.java` — *"This method builds the chain."*

### END THOUGHT

> "CSRF is one filter in that chain. Topic 4 — what CSRF actually is."

---

# TOPIC 4 — CSRF: Cross-Site Request Forgery

### SAY

> "**CSRF** = you are logged into **our** site in the browser.
> You visit a **malicious** site. That site secretly submits a form **to our site** using **your cookies**.
> Our server thinks **you** did it — because the session cookie went along."

### DRAW — classic story

```
1. HR manager logged into EMS at localhost:8080  (session cookie in browser)
2. Manager opens evil-blog.com in another tab
3. evil-blog.com has hidden form: POST /api/employees/delete/1  → localhost:8080
4. Browser auto-sends session cookie
5. EMS performs action — manager never clicked our app
```

### Contents

| Term | Meaning |
|------|---------|
| CSRF token | Secret value only **our real pages** know — evil site can't guess it |
| SameSite cookie | Modern browsers limit cross-site cookie sends (extra layer) |
| Stateless API | Postman sends auth header each time — no browser cookie → different CSRF story |

### SAY — crisp answer for students

> "CSRF targets **browser sessions with cookies**. Postman with Basic Auth every request is a different model — that's why we treated `/api/**` differently in EMS."

### END THOUGHT

> "Topic 5 — open our EMS config and read the CSRF line together."

---

# TOPIC 5 — CSRF in Our EMS SecurityConfig

### SAY

> "In Week 2 we wrote this on purpose — understand it before you copy-paste configs from the internet."

### YOU DO — read aloud this block

Open `SecurityConfig.java`:

```java
.csrf(csrf -> csrf
        .ignoringRequestMatchers("/api/**", "/h2-console/**")
)
```

### Contents — line by line

| Line | Meaning |
|------|---------|
| CSRF enabled by default | Spring protects **form POST** from browser |
| `.ignoringRequestMatchers("/api/**")` | REST APIs called from Postman/mobile **don't use CSRF token** — they use Basic Auth header instead |
| `/h2-console/**` ignored | Dev tool — convenience for class; **never in production** |

### SAY

> "We did **not** disable CSRF globally — we **exempted API paths** used by Postman.
> Browser form login to `/login` still goes through CSRF protection when enabled for those routes."

### STUCK? — student asks "Is ignoring CSRF bad?"

> "For **stateless JWT APIs** — common to disable CSRF entirely. For **session + cookie** browser apps — keep CSRF on forms. Match protection to how clients authenticate."

### END THOUGHT

> "Topic 6 — when CSRF matters vs when it doesn't."

---

# TOPIC 6 — API (Postman) vs Browser (Form Login)

### SAY

> "EMS has **two clients**: Postman (Basic Auth header) and browser (session after form login).
> Security rules should match **how each client authenticates**."

### Contents

| Client | Auth mechanism | CSRF relevant? |
|--------|----------------|----------------|
| Postman `/api/**` | `Authorization: Basic ...` each request | No — no automatic cookie |
| Browser `/login` form | Session cookie after login | **Yes** — cookie sent automatically |
| Mobile app + JWT | Bearer token header | No — stateless |

### DRAW

```
Postman  ──► Authorization: Basic xxx  ──► /api/employees   (CSRF skipped)
Browser  ──► Cookie: JSESSIONID      ──► /login POST       (CSRF token needed if enabled)
```

### YOU DO

Show both flows live:
1. Postman GET `/api/employees` with Basic Auth — works  
2. Browser login at `/login` — works  

No code changes required this topic.

### END THOUGHT

> "Topic 7 — one more line in SecurityConfig: clickjacking protection."

---

# TOPIC 7 — Security Headers: frameOptions

### SAY

> "**Clickjacking** = attacker embeds your site in invisible iframe, tricks clicks.
> `frameOptions(sameOrigin)` = only our pages can iframe our app."

### YOU DO

Point at:

```java
.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
```

### Contents

| Header | Purpose |
|--------|---------|
| `X-Frame-Options: SAMEORIGIN` | Stops evil site embedding EMS in iframe |
| (Future) CORS | Which **frontends** may call API — React on another port (mention only) |

### END THOUGHT

> "Small line, real protection. Topic 8 — passwords and secrets."

---

# TOPIC 8 — Passwords & Secrets Recap

### SAY

> "Security is not only CSRF. **How we store passwords and API keys** matters equally."

### Contents

| Topic | EMS today | Production |
|-------|-----------|------------|
| HR password | `{noop}hr123` in code | BCrypt hash + env var |
| Groq key | `${GROQ_API_KEY}` in `.env` | ✅ correct pattern |
| `.env` | Gitignored | Never commit |

### SAY

> "`{noop}` = **class demo only**. We said this in Week 2 — repeat until it sticks.
> Real apps: `PasswordEncoder` + BCrypt. Week 4 Expense project will use BCrypt + JWT."

### YOU DO

Show `.gitignore` entries for `.env` and `application-local.yml`.

### END THOUGHT

> "Topic 9 — optional CSRF demo if time."

---

# TOPIC 9 — Optional Demo: CSRF Block (2 min)

### SAY

> "If CSRF is **on** for a browser POST and you skip the token, Spring returns **403 Forbidden**.
> Our API paths ignore CSRF — Postman still works. This demo is **concept only**."

### YOU DO (optional)

Explain without breaking EMS:

1. Browser form POST without CSRF token → 403 (theory)  
2. Same call from Postman with Basic Auth to `/api/**` → 200 (CSRF ignored)  

> "That's exactly why we split browser vs API rules."

### END THOUGHT

> "Topic 10 — wrap and preview Week 4."

---

# TOPIC 10 — Wrap + Week 4 Preview

### SAY — what students learned today

| # | Takeaway |
|---|----------|
| 1 | Spring Security = filter chain before controller |
| 2 | CSRF = forged requests using browser cookies |
| 3 | Our EMS ignores CSRF on `/api/**` for Postman — intentional |
| 4 | Browser sessions need CSRF on forms; stateless APIs often don't |
| 5 | `{noop}` demo passwords ≠ production; secrets in `.env` |

### SAY — Week 4–5 preview

> "Sunday = **open Q&A** — bring doubts from Weeks 1–3.
> **Project #2 spans Week 4 + Week 5** (four classes):
> Week 4 = domain + JWT + employee submit.
> Week 5 = manager workflow + AI → Project #2 complete."

### Homework (light)

| # | Task |
|---|------|
| 1 | Re-read `SecurityConfig.java` — explain each block in your own words |
| 2 | Write 3 interview answers: CSRF, filter chain, why `.env` for Groq key |
| 3 | List questions for Sunday Q&A session |

### END THOUGHT

> "Security is understanding **why** the config exists — not memorizing annotations. See you Sunday for Q&A."

---

## QUICK REFERENCE

| Question | Answer |
|----------|--------|
| What is CSRF? | Trick logged-in browser into unwanted requests using cookies |
| Why ignore CSRF on `/api/**`? | Postman uses Basic Auth header — not cookie-based session for those calls |
| What is filter chain? | Ordered security checks before controller |
| Is `{noop}` safe? | **No** — demo only; use BCrypt in real apps |
| Where do API keys go? | Environment variables — never `application.yml` committed |

---

*CodeKerdos.in · Week 3 Class 1 · Security lecture — no new code folder*
