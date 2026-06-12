# Week 1 · Class 1 — Saturday · Spring Framework Core

> **[← Week 1 Index](README.md)** · **Next → [Class 2 — Sunday](Class-2.md)**  
> **Your private folder:** `week-01-spring-core-demo` (students do NOT see your repo — you build live)

---

## HOW TO USE THIS FILE (read once)

You teach **Topic 1 → 9 in order**.  
For each topic:

1. **SAY** — read aloud to students  
2. **DRAW** — whiteboard (optional)  
3. **YOU DO** — exact IntelliJ clicks + files to create  
4. **CODE** — copy-paste or type slowly  
5. **RUN** — what output you must see  
6. **STUCK?** — Copilot one-liner prompt  

**Students follow you in THEIR IntelliJ.** You build from zero on screen.

---

## CLASS 1 — TOPICS

| # | Topic | Do you create files? |
|---|-------|---------------------|
| 1 | Welcome & Bootcamp Contract | No — talk only |
| 2 | Why Spring? | No — Scratch file or board only |
| 3 | Maven & Project Structure | ✅ **Create project + pom.xml** |
| 4 | IoC + ApplicationContext | ✅ **App.java + AppConfig + GreetingService** |
| 5 | Dependency Injection | ✅ **MessageSender + Email + NotificationService** |
| 6 | @Autowired, @Qualifier, @Primary | ✅ **Add @Primary on Email** |
| 7 | @Configuration & @Bean | ✅ **Explain AppConfig (already there)** |
| 8 | Bean Scope | ✅ **RequestLogger + update App.java** |
| 9 | Full run + Homework | ✅ **Run everything** |

---

## FILES YOU WILL CREATE (final picture)

```
spring-core-demo/                          ← project name in IntelliJ
├── pom.xml
└── src/main/java/in/codekerdos/demo/
    ├── App.java
    ├── config/AppConfig.java
    ├── service/GreetingService.java
    ├── service/NotificationService.java
    ├── sender/MessageSender.java
    ├── sender/EmailMessageSender.java
    └── scope/RequestLogger.java
```

---

**Time split:**

| Block | Duration | Topics |
|-------|----------|--------|
| Welcome | 10 min | Topic 1 |
| Theory + first code | 25 min | Topic 2, 3 |
| Live coding | 35 min | Topic 4, 5 |
| More theory + code | 25 min | Topic 6, 7, 8 |
| Wrap | 10 min | Topic 9 |

---

# TOPIC 1 — Welcome & Bootcamp Contract

### SAY

> "Welcome to CodeKerdos. Every class = live coding. By the end: 3 projects with AI features.
> Saturday = Spring Core. Sunday = Spring Boot + Groq AI.
> Push your code to GitHub after class. Never commit API keys."

### DRAW — Bootcamp journey

```
WEEK 1 Sat → Spring Core (today)
WEEK 1 Sun → Spring Boot + Groq
WEEK 2-5  → EMS → Expense System → Booking + RAG
```

### YOU DO

1. Open IntelliJ (empty — no project yet)
2. Share screen
3. Poll students: `java -version` (need 17+)
4. **Do NOT open your private repo on screen**

### DEMO

Tell students: *"Create an empty folder on desktop — `spring-core-demo` — we'll use it from Topic 3."*

### END THOUGHT

> "Next: why companies use Spring instead of plain Java."

---

# TOPIC 2 — Why Spring? Problems with Raw Java

### SAY

> "Without Spring, YOU create every object with `new`. Change one class → fix 10 places. Hard to test.
> Spring = a manager that creates objects and wires them for you."

### Contents (show on slide or board)

| Problem | What goes wrong |
|---------|-----------------|
| Manual `new` | Hard to change |
| Tight coupling | Stuck to one implementation |
| Hard to test | Can't swap fake DB |
| Boilerplate | `main()` wires everything |

### YOU DO — Show pain code (NO project yet)

**Option A — Scratch file (easiest):**

1. IntelliJ → **File → New → Scratch File**
2. Choose **Java**
3. Paste this — say *"This is BAD enterprise code"*:

```java
// ❌ WITHOUT SPRING — show students, don't save as project file

class EmailService {
    void send(String to, String msg) {
        System.out.println("Email to " + to);
    }
}

class UserRepository {
    void save(String name) {
        System.out.println("Saved " + name);
    }
}

class UserService {
    // Problem: YOU hardcode dependencies — stuck forever
    private EmailService emailService = new EmailService();
    private UserRepository repo = new UserRepository();

    void register(String name) {
        repo.save(name);
        emailService.send(name + "@mail.com", "Welcome!");
    }
}

class BadDemo {
    public static void main(String[] args) {
        UserService service = new UserService();  // you manage everything
        service.register("Rahul");
    }
}
```

4. Point at lines with `new EmailService()` — *"Can't swap for test. Can't change easily."*

5. Below it paste **GOOD version** (still no Spring — just constructor):

```java
// ✅ BETTER STYLE — still plain Java, but ready for Spring later

class UserServiceFixed {
    private final EmailService emailService;
    private final UserRepository repo;

    // Dependencies come FROM OUTSIDE — not created inside
    UserServiceFixed(EmailService email, UserRepository repo) {
        this.emailService = email;
        this.repo = repo;
    }

    void register(String name) {
        repo.save(name);
        emailService.send(name + "@mail.com", "Welcome!");
    }
}
```

6. Say: *"Spring does this wiring automatically. Topic 3 we start the project."*

7. **Close scratch file** — you don't need it anymore.

**Option B — Whiteboard only:** Draw `main() → new Email → new UserService` if scratch file feels messy.

### DRAW

```
WITHOUT SPRING:  main() creates everything with new
WITH SPRING:     App starts → Container creates & injects objects
```

### RUN

Scratch file optional — no run required. Story + code on screen is enough.

### STUCK?

Copilot: *"Explain tight coupling with EmailService and UserService Java example"*

### END THOUGHT

> "Spring manages object creation. Topic 3 — we create our Maven project."

---

# TOPIC 3 — Maven & Project Structure

### SAY

> "Maven = downloads libraries for us. We list what we need in `pom.xml`. Spring comes as a JAR from the internet."

### YOU DO — Create project (step by step)

**Step 1 — New Maven project**

1. IntelliJ → **File → New → Project**
2. Select **Maven** (left side) — NOT Spring Boot
3. Click **Create**
4. **Name:** `spring-core-demo`
5. **Location:** your desktop or `week-01-spring-core-demo`
6. **GroupId:** `in.codekerdos`
7. **ArtifactId:** `spring-core-demo`
8. Click **Create**

**Step 2 — Fix folder structure**

IntelliJ should show:

```
spring-core-demo
├── pom.xml
└── src
    └── main
        └── java
        └── resources
```

If `java` folder missing: right-click `main` → **New → Directory** → type `java`

**Step 3 — Create package**

1. Right-click `src/main/java` → **New → Package**
2. Name: `in.codekerdos.demo`

**Step 4 — Replace `pom.xml` entire content**

Open `pom.xml` → select all → paste:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>in.codekerdos</groupId>
    <artifactId>spring-core-demo</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <spring.version>6.1.5</spring.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>${spring.version}</version>
        </dependency>
    </dependencies>
</project>
```

**Step 5 — Load Maven**

1. Right side → **Maven** tab (or View → Tool Windows → Maven)
2. Click **Reload** (circular arrow icon)
3. Wait until `spring-context` downloads — tell students *"Maven is downloading Spring"*

### DRAW

```
pom.xml → Maven Central → spring-context JAR → your project
```

### RUN

Nothing to run yet. Check Maven tab — no red errors on `pom.xml`.

### STUCK?

Copilot: *"Create pom.xml for plain Spring 6 project Java 17 with spring-context dependency"*

### END THOUGHT

> "Project exists. Next — Spring container + first bean."

---

# TOPIC 4 — IoC Container & ApplicationContext

### SAY

> "ApplicationContext = Spring's warehouse. We start it once. Spring creates objects (beans) and stores them.
> IoC = Spring calls `new` for us, not you."

### YOU DO — Create 3 files (in order)

---

**FILE 1 — `GreetingService.java`**

1. Right-click package `in.codekerdos.demo` → **New → Package** → name: `service`
2. Right-click `service` → **New → Java Class** → name: `GreetingService`
3. Paste:

```java
package in.codekerdos.demo.service;

public class GreetingService {

    private final String message;

    public GreetingService(String message) {
        this.message = message;
    }

    public String getGreeting() {
        return "Hello from " + message + "!";
    }
}
```

> Say: *"Plain Java class. No Spring annotation yet. @Bean will create this."*

---

**FILE 2 — `AppConfig.java`**

1. Right-click `in.codekerdos.demo` → **New → Package** → `config`
2. Right-click `config` → **New → Java Class** → `AppConfig`
3. Paste:

```java
package in.codekerdos.demo.config;

import in.codekerdos.demo.service.GreetingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public GreetingService greetingService() {
        return new GreetingService("CodeKerdos Spring Boot + AI Bootcamp");
    }
}
```

4. If imports red → Alt+Enter → Import

> Say: *"@Configuration = this class configures Spring. @Bean = this method creates an object Spring manages."*

---

**FILE 3 — `App.java`**

1. Right-click `in.codekerdos.demo` → **New → Java Class** → `App`
2. Paste:

```java
package in.codekerdos.demo;

import in.codekerdos.demo.config.AppConfig;
import in.codekerdos.demo.service.GreetingService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class App {

    public static void main(String[] args) {
        // Step 1: Start Spring container
        ApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);

        // Step 2: Ask Spring for GreetingService — YOU did not write "new"
        GreetingService greetingService = context.getBean(GreetingService.class);

        // Step 3: Use it
        System.out.println(greetingService.getGreeting());
    }
}
```

---

### RUN

1. Right-click `App.java` → **Run 'App.main()'**

**Expected output:**

```
Hello from CodeKerdos Spring Boot + AI Bootcamp!
```

If you see this → **IoC works.** Spring created `GreetingService` via `@Bean`.

### DRAW

```
App.java → new AnnotationConfigApplicationContext(AppConfig)
              → runs greetingService() @Bean method
              → getBean(GreetingService.class) returns ready object
```

### STUCK?

- Red on `@Configuration` → Maven not loaded — reload Maven  
- Copilot: *"Plain Spring Java App with ApplicationContext and one @Bean GreetingService"*

### END THOUGHT

> "Container is alive. Next — real Dependency Injection with @Service and @Component."

---

# TOPIC 5 — Dependency Injection (Constructor Injection)

### SAY

> "NotificationService NEEDS something to send messages. Instead of `new`, we declare it in the constructor.
> Spring sees constructor → finds EmailMessageSender → injects automatically."

### YOU DO — Create 3 more files

---

**FILE 4 — `MessageSender.java` (interface)**

1. Right-click `in.codekerdos.demo` → **New → Package** → `sender`
2. Right-click `sender` → **New → Java Class** → type **Interface** (dropdown) → `MessageSender`
3. Paste:

```java
package in.codekerdos.demo.sender;

public interface MessageSender {
    void send(String to, String message);
}
```

---

**FILE 5 — `EmailMessageSender.java`**

1. Right-click `sender` → **New → Java Class** → `EmailMessageSender`
2. Paste:

```java
package in.codekerdos.demo.sender;

import org.springframework.stereotype.Component;

@Component
public class EmailMessageSender implements MessageSender {

    @Override
    public void send(String to, String message) {
        System.out.println("📧 Email to " + to + ": " + message);
    }
}
```

> Say: *"@Component = Spring, please manage this class as a bean."*

---

**FILE 6 — `NotificationService.java`**

1. Right-click `service` → **New → Java Class** → `NotificationService`
2. Paste:

```java
package in.codekerdos.demo.service;

import in.codekerdos.demo.sender.MessageSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final MessageSender messageSender;

    // Constructor injection — Spring auto-fills messageSender
    public NotificationService(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public void notifyUser(String email, String message) {
        messageSender.send(email, message);
    }
}
```

---

**UPDATE `AppConfig.java`** — add component scan (Spring must FIND @Component classes):

Open `AppConfig.java` → add `@ComponentScan`:

```java
package in.codekerdos.demo.config;

import in.codekerdos.demo.service.GreetingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "in.codekerdos.demo")
public class AppConfig {

    @Bean
    public GreetingService greetingService() {
        return new GreetingService("CodeKerdos Spring Boot + AI Bootcamp");
    }
}
```

> Say: *"@ComponentScan tells Spring: look in this package for @Component and @Service."*

---

**UPDATE `App.java`** — add notification demo:

Replace `main` body with:

```java
public static void main(String[] args) {
    ApplicationContext context =
            new AnnotationConfigApplicationContext(AppConfig.class);

    System.out.println("=== CodeKerdos Week 1 — Class 1 ===\n");

    // Demo 1: @Bean
    GreetingService greetingService = context.getBean(GreetingService.class);
    System.out.println("1. Greeting: " + greetingService.getGreeting());

    // Demo 2: Constructor injection
    NotificationService notificationService = context.getBean(NotificationService.class);
    System.out.println("\n2. Notification (DI):");
    notificationService.notifyUser("student@codekerdos.in", "Welcome to Week 1!");
}
```

---

### RUN

**Expected output:**

```
=== CodeKerdos Week 1 — Class 1 ===

1. Greeting: Hello from CodeKerdos Spring Boot + AI Bootcamp!

2. Notification (DI):
📧 Email to student@codekerdos.in: Welcome to Week 1!
```

### STUCK?

- `No qualifying bean of type MessageSender` → forgot `@ComponentScan`  
- Copilot: *"Spring @Service NotificationService with constructor injection of @Component MessageSender"*

### END THOUGHT

> "DI is working. EmailMessageSender was injected into NotificationService automatically."

---

# TOPIC 6 — @Autowired, @Qualifier, @Primary

### SAY

> "When TWO classes implement MessageSender, Spring gets confused. @Primary = default choice. @Qualifier = pick by name."

### YOU DO

**UPDATE `EmailMessageSender.java`** — add `@Primary`:

```java
package in.codekerdos.demo.sender;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class EmailMessageSender implements MessageSender {

    @Override
    public void send(String to, String message) {
        System.out.println("📧 Email to " + to + ": " + message);
    }
}
```

**Show on screen only (homework — don't build now unless time):**

```java
// Students will add this for homework
@Component("smsMessageSender")
public class SmsMessageSender implements MessageSender {
    public void send(String to, String message) {
        System.out.println("📱 SMS to " + to + ": " + message);
    }
}
```

Say: *"If both Email and SMS exist without @Primary, Spring throws error. @Primary on Email fixes it."*

### RUN

Run `App.java` again — same output as Topic 5. Still works.

### END THOUGHT

> "One implementation = no @Autowired needed on constructor. Multiple = @Primary or @Qualifier."

---

# TOPIC 7 — @Configuration & @Bean (explain what you already built)

### SAY

> "@Component = put sticker on YOUR class. @Bean = factory method in config class for objects you build manually."

### YOU DO

1. Open `AppConfig.java` — walk line by line (don't create new file)
2. Point at `@Bean greetingService()` — *"We pass custom message string"*
3. Point at `@ComponentScan` — *"Finds EmailMessageSender and NotificationService"*
4. Compare:

| Pattern | Example in project |
|---------|-------------------|
| `@Bean` | `GreetingService` in AppConfig |
| `@Component` | `EmailMessageSender` |
| `@Service` | `NotificationService` |

### DRAW

```
AppConfig (@Configuration)
   ├── @Bean → GreetingService
   └── @ComponentScan → finds @Component, @Service in package
```

### RUN

No change — explain only.

### END THOUGHT

> "Both @Bean and @Component end up as beans in the same container."

---

# TOPIC 8 — Bean Scope: Singleton vs Prototype

### SAY

> "Singleton = one object for whole app (default). Prototype = new object every time you ask."

### YOU DO

**FILE 7 — `RequestLogger.java`**

1. Right-click `in.codekerdos.demo` → **New → Package** → `scope`
2. Right-click `scope` → **New → Java Class** → `RequestLogger`
3. Paste:

```java
package in.codekerdos.demo.scope;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope("prototype")
public class RequestLogger {

    private final UUID id = UUID.randomUUID();

    public UUID getId() {
        return id;
    }
}
```

---

**UPDATE `App.java`** — add scope demo at end of main:

```java
public static void main(String[] args) {
    ApplicationContext context =
            new AnnotationConfigApplicationContext(AppConfig.class);

    System.out.println("=== CodeKerdos Week 1 — Class 1 ===\n");

    GreetingService greetingService = context.getBean(GreetingService.class);
    System.out.println("1. Greeting: " + greetingService.getGreeting());

    NotificationService notificationService = context.getBean(NotificationService.class);
    System.out.println("\n2. Notification (DI):");
    notificationService.notifyUser("student@codekerdos.in", "Welcome to Week 1!");

    // Demo 3: Prototype scope
    RequestLogger logger1 = context.getBean(RequestLogger.class);
    RequestLogger logger2 = context.getBean(RequestLogger.class);
    System.out.println("\n3. Scope Demo (Prototype):");
    System.out.println("   Logger 1 ID: " + logger1.getId());
    System.out.println("   Logger 2 ID: " + logger2.getId());
    System.out.println("   Same instance? " + (logger1 == logger2));

    System.out.println("\n=== Class 1 Complete ===");
}
```

Add import at top:

```java
import in.codekerdos.demo.scope.RequestLogger;
```

---

### RUN

**Expected output:**

```
=== CodeKerdos Week 1 — Class 1 ===

1. Greeting: Hello from CodeKerdos Spring Boot + AI Bootcamp!

2. Notification (DI):
📧 Email to student@codekerdos.in: Welcome to Week 1!

3. Scope Demo (Prototype):
   Logger 1 ID: a1b2c3d4-e5f6-7890-abcd-ef1234567890
   Logger 2 ID: f0987654-3210-fedc-ba09-876543210987
   Same instance? false

=== Class 1 Complete ===
```

> Point at `Same instance? false` — *"Prototype = different UUID each time."*

### END THOUGHT

> "99% beans are Singleton. Prototype is interview trivia + special cases."

---

# TOPIC 9 — Full Run + Homework

### YOU DO — Final demo (5 min)

1. Run `App.java` one more time — explain every line to class
2. Show Project tree on left — all 7 files you created today
3. Tell students: *"Push to YOUR GitHub after class"*

### Final file checklist

| # | File | Created in Topic |
|---|------|------------------|
| ✅ | `pom.xml` | 3 |
| ✅ | `service/GreetingService.java` | 4 |
| ✅ | `config/AppConfig.java` | 4 (updated in 5) |
| ✅ | `App.java` | 4 (updated in 5, 8) |
| ✅ | `sender/MessageSender.java` | 5 |
| ✅ | `sender/EmailMessageSender.java` | 5 (updated in 6) |
| ✅ | `service/NotificationService.java` | 5 |
| ✅ | `scope/RequestLogger.java` | 8 |

### Homework (give students)

| # | Task |
|---|------|
| 1 | Repeat today's project in your IntelliJ |
| 2 | Add `SmsMessageSender implements MessageSender` with `@Component("smsMessageSender")` |
| 3 | Create `AlertService` with `@Qualifier("smsMessageSender")` in constructor |
| 4 | Push to your GitHub |
| 5 | Sign up at [console.groq.com](https://console.groq.com) for Sunday |

### SAY — Sunday preview

```
Sunday = Spring Boot + REST API + H2 database + first Groq AI call
Open Class-2.md on your laptop (private — same as today)
```

### END THOUGHT

> "Today: IoC, DI, beans, Maven. Sunday: Spring Boot makes this 10x faster + AI."

---

## QUICK REFERENCE — If you forget mid-class

| Problem | Fix |
|---------|-----|
| Red imports | Maven → Reload |
| No bean found | Add `@ComponentScan` to AppConfig |
| Can't run main | Right-click App.java → Run |
| Copilot help | "Fix Spring No qualifying bean error" |

---

*CodeKerdos.in · Week 1 Class 1 · Private tutor script — build live, don't show repo*
