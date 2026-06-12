# Week 1: Spring Core Fundamentals — Interview Answers & Exercise Solutions

---

## 🎯 The Interview Golden Rule

Whenever an interviewer asks you a Spring Core concept, answer in **four beats**. This makes you sound senior, not like someone who memorised a definition:

1. **Definition** → say what it *is* in one clean sentence.
2. **The problem it solves** → why does this exist? What pain did it remove?
3. **Code / annotation example** → show, don't just tell. One small snippet.
4. **Trade-off** → mention the catch, the alternative, or "when NOT to use it". This is what separates a 3-year engineer from a fresher.

> 🗣️ **Example out loud:** "Dependency Injection *(definition)* means an object receives its dependencies from outside instead of creating them. It *(problem)* removes tight coupling so I can swap implementations and unit-test with mocks. *(code)* I just declare `private final PaymentService payment;` in the constructor and Spring wires it. *(trade-off)* The only catch is too many constructor params can signal a class doing too much — that's a design smell, not a DI problem."

Use this rhythm for **every** answer below and you will sound fluent.

---

# PART A — Exercise / Practice Solutions

The Plan.md lists **5 practice exercises** (Section 9) plus several "common mistake" fix-it scenarios. Here is every one, fully worked with correct, runnable Java/Spring code.

---

## ✅ Exercise 1: Hello Spring

**Task:** A `GreetingService` that returns `"Namaste, {name}!"`, a `GreetingController` with `GET /greet/{name}`, controller uses **constructor injection**.

```java
// GreetingService.java
package com.example.myfirstapp.service;

import org.springframework.stereotype.Service;

@Service  // Spring creates and manages a single instance (bean) of this class
public class GreetingService {

    public String greet(String name) {
        return "Namaste, " + name + "!";
    }
}
```

```java
// GreetingController.java
package com.example.myfirstapp.controller;

import com.example.myfirstapp.service.GreetingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController  // Handles HTTP requests and returns the value directly as the response body
public class GreetingController {

    private final GreetingService greetingService;  // 'final' = injected once, never reassigned

    // Constructor injection. With a SINGLE constructor, @Autowired is OPTIONAL —
    // Spring auto-detects it. Shown here for clarity.
    public GreetingController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping("/greet/{name}")  // {name} is a path variable pulled from the URL
    public String greet(@PathVariable String name) {
        return greetingService.greet(name);
    }
}
```

**Test it:** Visit `http://localhost:8080/greet/Sheetal` → `Namaste, Sheetal!`

> 💡 **Interview gold:** Mention *why* you used `final` + constructor injection — "it makes the dependency immutable and the class trivially unit-testable: `new GreetingController(mockService)`."

---

## ✅ Exercise 2: Configuration Practice (`@Value`)

**Task:** Read `app.company.*` properties and return them as JSON.

```properties
# application.properties
app.company.name=Flipkart
app.company.founded=2007
app.company.ceo=Kalyan Krishnamurthy
```

```java
// CompanyInfoService.java
package com.example.myfirstapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CompanyInfoService {

    @Value("${app.company.name}")     // injects "Flipkart"
    private String name;

    @Value("${app.company.founded}")  // injects 2007 — Spring converts String -> int
    private int founded;

    @Value("${app.company.ceo}")      // injects "Kalyan Krishnamurthy"
    private String ceo;

    public Map<String, Object> getCompanyInfo() {
        // LinkedHashMap preserves insertion order so the JSON keys appear in a predictable order
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", name);
        info.put("founded", founded);
        info.put("ceo", ceo);
        return info;
    }
}
```

```java
// CompanyController.java
package com.example.myfirstapp.controller;

import com.example.myfirstapp.service.CompanyInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CompanyController {

    private final CompanyInfoService companyInfoService;

    public CompanyController(CompanyInfoService companyInfoService) {
        this.companyInfoService = companyInfoService;
    }

    @GetMapping("/company")
    public Map<String, Object> getCompany() {
        // Returning a Map from a @RestController -> Spring serialises it to JSON automatically
        return companyInfoService.getCompanyInfo();
    }
}
```

**Result of `GET /company`:**
```json
{ "name": "Flipkart", "founded": 2007, "ceo": "Kalyan Krishnamurthy" }
```

> 🏆 **Bonus / better-practice answer:** For a *group* of related properties, the cleaner approach is `@ConfigurationProperties` instead of scattered `@Value`:
> ```java
> @Component
> @ConfigurationProperties(prefix = "app.company")
> public class CompanyProperties {
>     private String name;
>     private int founded;
>     private String ceo;
>     // getters + setters — Spring binds app.company.* onto these fields
> }
> ```
> Saying this in an interview shows you know `@Value` is fine for one-off values but `@ConfigurationProperties` is the production pattern for grouped config.

---

## ✅ Exercise 3: Multiple Implementations (`@Qualifier`)

**Task:** Interface `NotificationService` with two implementations; `OrderService` injects the **SMS** one via `@Qualifier`.

```java
// NotificationService.java
package com.example.myfirstapp.service;

public interface NotificationService {
    void send(String message);
}
```

```java
// EmailNotificationService.java
package com.example.myfirstapp.service;

import org.springframework.stereotype.Service;

@Service("emailNotificationService")  // explicit bean name (also defaults to this if omitted)
public class EmailNotificationService implements NotificationService {
    @Override
    public void send(String message) {
        System.out.println("Sending EMAIL: " + message);
    }
}
```

```java
// SmsNotificationService.java
package com.example.myfirstapp.service;

import org.springframework.stereotype.Service;

@Service("smsNotificationService")
public class SmsNotificationService implements NotificationService {
    @Override
    public void send(String message) {
        System.out.println("Sending SMS: " + message);
    }
}
```

```java
// OrderService.java
package com.example.myfirstapp.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final NotificationService notificationService;

    // Two beans implement NotificationService, so a plain type-match would be ambiguous
    // (NoUniqueBeanDefinitionException). @Qualifier tells Spring EXACTLY which bean to inject.
    public OrderService(@Qualifier("smsNotificationService") NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void placeOrder(String orderId) {
        // ... order logic ...
        notificationService.send("Your order " + orderId + " has been placed!");
    }
}
```

**Output of `placeOrder("ORD-42")`:** `Sending SMS: Your order ORD-42 has been placed!`

> 💡 **Why this matters:** Without `@Qualifier`, Spring sees two `NotificationService` beans and throws `NoUniqueBeanDefinitionException` at startup. Mention the alternative: marking one impl `@Primary` makes it the default winner when no qualifier is given.

---

## ✅ Exercise 4: Bean Scopes (Singleton vs Prototype)

**Task:** A singleton counter (counts ALL requests) + a prototype object (new unique ID each time). Endpoint shows both.

```java
// RequestCounter.java  — SINGLETON (default scope)
package com.example.myfirstapp.scope;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component  // default scope is singleton — ONE shared instance for the whole app
public class RequestCounter {

    // AtomicLong: thread-safe increment. A singleton is shared across concurrent requests,
    // so a plain 'long count++' could lose increments under load (race condition).
    private final AtomicLong count = new AtomicLong(0);

    public long incrementAndGet() {
        return count.incrementAndGet();
    }
}
```

```java
// PrototypeId.java  — PROTOTYPE
package com.example.myfirstapp.scope;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)  // a NEW instance every time it is requested
public class PrototypeId {

    // Generated once per instance. Because each request creates a new bean, this changes each time.
    private final String id = UUID.randomUUID().toString();

    public String getId() {
        return id;
    }
}
```

```java
// ScopeController.java
package com.example.myfirstapp.controller;

import com.example.myfirstapp.scope.PrototypeId;
import com.example.myfirstapp.scope.RequestCounter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class ScopeController {

    private final RequestCounter requestCounter;       // singleton — injected ONCE
    private final ObjectProvider<PrototypeId> idProvider;  // factory for fresh prototype beans

    // CRITICAL GOTCHA: A prototype bean injected directly into a singleton is created only
    // ONCE (when the singleton is built), defeating the point. ObjectProvider.getObject()
    // pulls a brand-new prototype instance on every call. This is the correct pattern.
    public ScopeController(RequestCounter requestCounter, ObjectProvider<PrototypeId> idProvider) {
        this.requestCounter = requestCounter;
        this.idProvider = idProvider;
    }

    @GetMapping("/scope")
    public Map<String, Object> showScopes() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalRequests", requestCounter.incrementAndGet());  // keeps climbing
        result.put("prototypeId", idProvider.getObject().getId());      // changes every call
        return result;
    }
}
```

**Observed behaviour across calls to `/scope`:**

| Call | `totalRequests` (singleton) | `prototypeId` (prototype) |
|------|------------------------------|----------------------------|
| 1st  | 1                            | `a1b2...` |
| 2nd  | 2                            | `c3d4...` (different!) |
| 3rd  | 3                            | `e5f6...` (different again!) |

The singleton counter **accumulates**; the prototype ID is **fresh every request**. That contrast IS the lesson.

> 🏆 **Senior-level point:** The `ObjectProvider` (or `@Lookup`, or a `Provider<>`) trick is the #1 thing freshers get wrong about prototype scope — they inject it directly and wonder why it never changes. Knowing this is a real differentiator.

---

## ✅ Exercise 5: Profile Configuration (dev / staging / prod)

**Task:** Three profiles, each with a different `app.environment` and `server.port`. Endpoint `/env` returns the environment name.

```properties
# application.properties  (common + chooses active profile)
spring.profiles.active=dev
```

```properties
# application-dev.properties
app.environment=Development
server.port=8080
```

```properties
# application-staging.properties
app.environment=Staging
server.port=8081
```

```properties
# application-prod.properties
app.environment=Production
server.port=80
```

```java
// EnvController.java
package com.example.myfirstapp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EnvController {

    @Value("${app.environment}")  // resolves from whichever profile is active
    private String environment;

    @GetMapping("/env")
    public String currentEnvironment() {
        return "Current environment: " + environment;
    }
}
```

**Run with different profiles:**
```bash
# Dev (from the active profile in application.properties)
./mvnw spring-boot:run                                   # -> port 8080, "Development"

# Staging (override at launch)
java -jar target/my-first-app.jar --spring.profiles.active=staging   # -> port 8081, "Staging"

# Prod
java -jar target/my-first-app.jar --spring.profiles.active=prod      # -> port 80, "Production"
```

**Results of `GET /env`:**
- dev → `Current environment: Development`
- staging → `Current environment: Staging`
- prod → `Current environment: Production`

> 💡 **Interview tie-in:** "In real companies the active profile is set via an environment variable (`SPRING_PROFILES_ACTIVE=prod`) on the server, so developers never edit production config files and secrets stay out of git."

---

## ✅ Bonus: The "Common Mistakes" Fix-Its (Section 8)

The Plan also embeds five mistake/fix scenarios. Here are the corrected, complete versions worth knowing — interviewers love asking "what's wrong with this code?"

### Fix 1 — Don't store user state in a singleton
```java
// ❌ WRONG: one shared list for ALL users (singleton bean)
@Service
public class CartService {
    private final List<String> items = new ArrayList<>(); // User A and User B share this!
    public void addToCart(String item) { items.add(item); }
}

// ✅ RIGHT: state lives in the database keyed by user, bean stays stateless
@Service
public class CartService {
    private final CartRepository cartRepo;
    public CartService(CartRepository cartRepo) { this.cartRepo = cartRepo; }

    public void addToCart(Long userId, String item) {
        Cart cart = cartRepo.findByUserId(userId);  // each user has their OWN cart row
        cart.addItem(item);
        cartRepo.save(cart);
    }
}
```

### Fix 2 — Prefer constructor over field injection
```java
// ❌ field injection: hard to test, hidden deps, can't be final
@Service
public class OrderService {
    @Autowired private PaymentService paymentService;
}

// ✅ constructor injection: explicit, testable, immutable
@Service
public class OrderService {
    private final PaymentService paymentService;
    public OrderService(PaymentService paymentService) { this.paymentService = paymentService; }
}
```

### Fix 3 — Component scanning across packages
```java
// Main class lives in com.example.myfirstapp -> only that tree is scanned by default.
// A @Service in com.other.package is INVISIBLE. Two fixes:

// Fix A (preferred): move the bean under com.example.myfirstapp.*

// Fix B: widen the scan
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.myfirstapp", "com.other.package"})
public class Application { }
```

### Fix 4 — Break a circular dependency
```java
// ❌ A needs B, B needs A -> Spring can't construct either (with constructor injection it
//    fails fast at startup; this is GOOD — it surfaces the design flaw).
// ✅ Extract the shared logic into a third collaborator that both depend on,
//    or break the cycle so the dependency only flows one way.
@Service
public class ServiceA {
    private final SharedService shared;
    public ServiceA(SharedService shared) { this.shared = shared; }
}
@Service
public class ServiceB {
    private final SharedService shared;
    public ServiceB(SharedService shared) { this.shared = shared; }
}
@Service
public class SharedService { /* the common behaviour A and B both needed */ }
```
*(A last-resort, not-recommended escape hatch is `@Lazy` on one of the constructor params, which defers creation. Mention it but say you'd refactor instead.)*

### Fix 5 — Beans are singletons by default
```java
// Both services receive the SAME PaymentService instance — that's expected & efficient,
// because singletons are stateless and shared. Only switch scope if the bean holds per-use state.
@Service public class OrderService  { @Autowired PaymentService p; } // object #12345
@Service public class RefundService { @Autowired PaymentService p; } // SAME object #12345
```

---

# PART B — Interview Questions & Model Answers

Spoken-style, confident answers. Each follows the **Definition → Problem → Example → Trade-off** rhythm.

---

### Q1. What is Inversion of Control (IoC)?

> "IoC means the **control of creating and wiring objects is inverted** — instead of my code calling `new`, the framework creates objects and hands them to me. The problem it solves is that manual object creation scatters wiring logic everywhere and couples classes to concrete implementations. In Spring, the **IoC container (ApplicationContext)** owns object creation; I just declare what I need. The trade-off is a slight loss of 'obviousness' — you can't always `Ctrl+click` to see who creates an object — but you gain testability and loose coupling."

🍽️ **Analogy:** Home cooking (you control everything) vs. a restaurant (you order, the kitchen controls how it's made).

---

### Q2. What is Dependency Injection (DI)? Is it the same as IoC?

> "DI is the **specific technique** for achieving IoC: a class receives its dependencies from the outside rather than constructing them itself. IoC is the broad principle; DI is the most common way Spring implements it. It solves **tight coupling** — without DI, swapping Razorpay for Paytm means editing every class that did `new PaymentService()`. With DI I inject the dependency through the constructor, so swapping is a one-line config change and I can pass a mock in tests. The trade-off: there's indirection and a bit of 'magic', so you must understand how Spring resolves beans to debug it."

```java
@Service
public class OrderService {
    private final PaymentService payment;       // dependency
    public OrderService(PaymentService payment) // injected from outside
    { this.payment = payment; }
}
```

---

### Q3. What are the three types of Dependency Injection? Which do you prefer?

| Type | How | Pros | Cons |
|------|-----|------|------|
| **Constructor** ⭐ | Deps passed in constructor | Immutable (`final`), explicit, fails fast, easy to test, no Spring needed in unit tests | Verbose with many deps (but that's a design smell) |
| **Setter** | `@Autowired` on a setter | Good for *optional* deps; can re-configure | Mutable, object can exist half-wired |
| **Field** | `@Autowired` on the field | Least code | Can't be `final`, hidden deps, can't mock without reflection/Spring |

> "I prefer **constructor injection**. It makes dependencies explicit and immutable, the object is fully valid the moment it's created, and I can unit-test with a plain `new`. Field injection is tempting for its brevity but it hides dependencies and resists testing — I only use it in throwaway demos. Setter injection I reserve for genuinely optional collaborators."

---

### Q4. What is a Bean?

> "A bean is simply **any object that the Spring IoC container instantiates, wires, and manages**. The distinction is ownership: if I write `new Foo()`, that's a plain object Spring knows nothing about; if Spring creates it (because it's annotated `@Component`/`@Service`/etc. or declared with `@Bean`), it's a *managed* bean with a full lifecycle. The benefit is Spring handles its creation, dependency injection, scope, and destruction. There's no real trade-off — it's the foundational unit of Spring."

👔 **Analogy:** A bean is a registered employee on the payroll; a `new` object is a freelancer who walked in off the street.

---

### Q5. Walk me through the Spring Bean lifecycle.

> "When the context starts: **(1)** Spring scans for bean definitions, **(2)** instantiates each bean, **(3)** injects its dependencies, **(4)** runs any `@PostConstruct` initialisation method, **(5)** the bean is now in service. On shutdown, **(6)** Spring calls `@PreDestroy` for cleanup. The problem these hooks solve is needing to run setup *after* injection (a constructor can't, because deps aren't set yet) and cleanup *before* JVM exit."

```java
@Service
public class CacheService {
    @PostConstruct void init()    { System.out.println("Warming cache..."); }   // after injection
    @PreDestroy   void cleanup()  { System.out.println("Flushing cache..."); }  // before shutdown
}
```
> "Trade-off: `@PreDestroy` only fires for **singletons** on a graceful shutdown — prototype beans aren't destroyed by the container, so you must clean those up yourself."

---

### Q6. What are the bean scopes? When do you use each?

| Scope | Instances | Lifetime | Use when | Analogy |
|-------|-----------|----------|----------|---------|
| **singleton** (default) | One per container | Whole app | Stateless services, repos, controllers | 🏫 One school principal |
| **prototype** | New every request | GC'd when unreferenced | Bean holds per-use mutable state | 🎟️ Movie ticket per person |
| **request** (web) | One per HTTP request | The request | Per-request data like a request ID | 🏦 Bank token number |
| **session** (web) | One per user session | The session | Per-user data like a shopping context | 🏋️ Gym locker for your visit |

> "Default is **singleton**, and that's right ~95% of the time because well-designed services are stateless. The cardinal rule: **never store user-specific mutable state in a singleton** — all users share that one instance, so User A would see User B's data. `request` and `session` only exist in web apps. The trade-off with `prototype` is that the container creates it but doesn't manage its destruction, and injecting it into a singleton needs an `ObjectProvider` to actually get fresh instances."

---

### Q7. `@Component` vs `@Bean` — what's the difference?

| | `@Component` (+ `@Service`/`@Repository`/`@Controller`) | `@Bean` |
|---|---|---|
| Where | On the **class** | On a **method** inside a `@Configuration` class |
| Detection | Auto-detected by component scanning | Explicitly declared by you |
| Best for | **Your own** classes you can annotate | **Third-party** classes you can't modify (e.g., `RestTemplate`, `ObjectMapper`) |
| Control | Spring constructs it | You write the construction code |

> "`@Component` says 'scan and manage my class.' `@Bean` is a factory method where *I* build the object and return it — essential when I can't put an annotation on a third-party class like Spring's own `RestTemplate`. So the deciding question is simply: *do I own the class?* If yes, `@Component`; if no, `@Bean` in a config class."

```java
@Configuration
public class AppConfig {
    @Bean RestTemplate restTemplate() { return new RestTemplate(); } // can't annotate RestTemplate itself
}
```

---

### Q8. What's the difference between `@Component`, `@Service`, `@Repository`, `@Controller`?

> "**Functionally they're nearly identical** — all four register the class as a bean (`@Service`, `@Repository`, `@Controller` are *specialisations* of `@Component`). The difference is **semantic clarity** plus two special behaviours: `@Repository` triggers **exception translation**, converting vendor-specific DB exceptions (like a MySQL `SQLException`) into Spring's uniform `DataAccessException`, so my service layer is database-agnostic. `@Controller` (and `@RestController`) hook into Spring MVC's request mapping. So I pick the annotation that matches the layer — it documents intent *and* unlocks layer-specific behaviour."

| Annotation | Layer | Special behaviour |
|------------|-------|-------------------|
| `@Component` | any | none — generic |
| `@Service` | business logic | none (semantic label) |
| `@Repository` | data access | **DB exception → `DataAccessException` translation** |
| `@Controller` | web (views) | MVC request mapping |
| `@RestController` | web (REST) | `@Controller` + `@ResponseBody` (returns JSON) |

---

### Q9. What is `@Autowired` and how does Spring resolve which bean to inject?

> "`@Autowired` tells Spring 'find a matching bean and inject it here.' Resolution order: **(1)** match by **type** first; **(2)** if exactly one bean of that type exists, inject it; **(3)** if **none** exists → `NoSuchBeanDefinitionException` at startup; **(4)** if **multiple** exist → `NoUniqueBeanDefinitionException`, unless I disambiguate with `@Primary` or `@Qualifier`, or the field name matches a bean name. The big win is **failing fast at startup** rather than with a `NullPointerException` at runtime."

> "Trade-off / nuance: on a class with a single constructor, `@Autowired` is **optional** — Spring infers it. And field-level `@Autowired` uses reflection, which is why it resists clean unit testing."

---

### Q10. `@Qualifier` vs `@Primary` — when do you use which?

> "Both resolve the 'multiple beans of the same type' ambiguity, but at different ends. **`@Primary`** marks one bean as the **default winner** when no other hint is given — good when there's an obvious 'normal' choice. **`@Qualifier`** is the **explicit, per-injection-point override** — it names exactly which bean to wire, and it **beats `@Primary`** if both are present. So: `@Primary` sets the sensible default once; `@Qualifier` overrides it where a specific injection needs something else."

```java
@Service @Primary
class EmailNotificationService implements NotificationService { }   // default everywhere

@Service("smsNotificationService")
class SmsNotificationService implements NotificationService { }

@Service
class OrderService {
    OrderService(@Qualifier("smsNotificationService") NotificationService n) { } // override the default
}
```

| | `@Primary` | `@Qualifier` |
|---|-----------|--------------|
| Scope | Global default | Per injection point |
| Where | On the bean definition | At the injection site |
| Wins if both present? | No | **Yes** |

---

### Q11. `ApplicationContext` vs `BeanFactory` — what's the difference?

| | `BeanFactory` | `ApplicationContext` |
|---|---------------|----------------------|
| Role | Basic IoC container | Superset of BeanFactory |
| Bean creation | **Lazy** (on first request) | **Eager** for singletons (at startup) |
| Extras | DI only | Event publishing, i18n messages, `@Value`/property resolution, `BeanPostProcessor` auto-registration, AOP integration |
| Use in practice | Rarely directly | **The standard** — what Spring Boot creates |

> "`BeanFactory` is the bare-bones IoC container that just does lazy DI. `ApplicationContext` extends it with everything an enterprise app needs: eager singleton instantiation so wiring errors surface at startup, event publishing, internationalisation, annotation processing, and AOP. In practice I always use `ApplicationContext` — `SpringApplication.run()` returns one. The only reason to prefer `BeanFactory` would be an extremely memory-constrained environment, which is rare today."

---

### Q12. What is the IoC Container, concretely, and what does it do?

> "Concretely it's the **`ApplicationContext`** — a registry that **creates** beans, **wires** their dependencies, **manages** their lifecycle and scope, and **serves** them on demand. `SpringApplication.run()` builds it. The problem it solves is centralising all object management in one place so my business classes stay focused on logic, not plumbing. I rarely touch it directly — I let `@Autowired` pull from it — but I *can*: `context.getBean(OrderService.class)`."

---

### Q13. What happens when you run a Spring Boot application?

> "`SpringApplication.run()` kicks off a sequence: **(1)** it creates the `ApplicationContext`; **(2)** `@SpringBootApplication` triggers **component scanning** of the main class's package and below, discovering all `@Component`/`@Service`/etc. beans; **(3)** **auto-configuration** inspects the classpath and configures beans for what it finds (e.g., spots `spring-boot-starter-web` → configures Spring MVC + an embedded Tomcat); **(4)** it instantiates beans and injects dependencies, running `@PostConstruct` hooks; **(5)** it starts the **embedded server** on port 8080; **(6)** the app is live. The headline benefit is *convention over configuration* — sensible defaults mean almost zero boilerplate to get running."

---

### Q14. How does Spring Boot auto-configuration work?

> "Auto-configuration means Spring Boot **looks at your classpath and conditionally configures beans** so you don't have to. `@SpringBootApplication` bundles `@EnableAutoConfiguration`, which loads auto-config classes; each is guarded by `@Conditional` annotations like `@ConditionalOnClass` and `@ConditionalOnMissingBean`. So if `HikariCP` is on the classpath *and* you haven't defined your own `DataSource`, Boot creates a pooled one for you. The problem it solves is the pages of XML/Java config plain Spring required. The trade-off: it can feel like 'magic' — when something configures unexpectedly, `--debug` prints the **auto-configuration report** showing what matched and why, and `@ConditionalOnMissingBean` means *your* explicit bean always overrides Boot's default."

---

### Q15. Constructor vs Field injection — and why is constructor injection 'testable'?

> "Constructor injection makes the dependency a required, `final` parameter, so the object can't exist in a half-built state and the dependency can't be reassigned. It's **testable** because in a unit test I just call `new OrderService(mockPayment)` — **no Spring context needed**. Field injection sets a private field via reflection, so to inject a mock I'd need Spring's test machinery or reflection hacks, and the dependencies are hidden inside the class body rather than declared up front. That's why constructor injection is the recommended default."

---

### Q16. What is a circular dependency and how do you resolve it?

> "It's when bean A depends on B and B depends on A — Spring can't finish constructing either, because each needs the other to exist first. With **constructor injection it fails fast at startup**, which is actually a *feature*: it surfaces a design flaw early. The right fix is to **refactor** — usually the shared behaviour belongs in a third service both can depend on, or the dependency should only flow one direction. As a last-resort patch you can put `@Lazy` on one constructor param so Spring injects a proxy and defers creation, but I treat that as a smell to revisit, not a real solution."

---

### Q17. What is AOP (Aspect-Oriented Programming) in Spring? (Bonus / commonly asked)

> "AOP lets me **modularise cross-cutting concerns** — logging, security checks, transactions, metrics — that would otherwise be copy-pasted across many methods. The problem: code like 'log entry/exit' or 'open a transaction' clutters every method and violates DRY. With AOP I write the concern once as an **aspect** and declare *where* it applies via a **pointcut**; Spring weaves it in using **proxies** at runtime."

Key vocabulary to drop confidently:

| Term | Meaning |
|------|---------|
| **Aspect** | The module bundling a cross-cutting concern |
| **Advice** | The action: `@Before`, `@After`, `@Around`, `@AfterReturning`, `@AfterThrowing` |
| **Join point** | A point where advice *could* run (a method execution) |
| **Pointcut** | The expression selecting *which* join points |
| **Weaving** | Linking aspects into target objects (Spring does this via proxies at runtime) |

```java
@Aspect
@Component
public class LoggingAspect {
    @Before("execution(* com.example.myfirstapp.service.*.*(..))")  // pointcut
    public void logEntry(JoinPoint jp) {                            // advice
        System.out.println("Entering: " + jp.getSignature().getName());
    }
}
```
> "The classic real-world use is `@Transactional` — it's AOP under the hood: a proxy starts a transaction before your method and commits/rolls back after. Trade-off: AOP is proxy-based, so **self-invocation** (a method in a bean calling another method *in the same bean*) bypasses the proxy and the advice won't fire — a famous gotcha."

---

### Q18. How do you manage different configurations for dev, staging, and production?

> "With **Spring Profiles**. I keep a base `application.properties` for shared settings and per-environment files like `application-dev.properties`, `application-prod.properties`. Spring loads the base plus whichever profile is active, with the profile-specific values overriding. I activate it via `spring.profiles.active`, but in production that's set through an **environment variable** (`SPRING_PROFILES_ACTIVE=prod`) on the server — so developers never edit prod config and secrets stay out of source control. I can also annotate beans with `@Profile("dev")` so certain beans only exist in certain environments. The problem this solves is shipping one artifact that behaves correctly in every environment without code changes."

---

## 🧠 Memory Hooks — Analogies for Instant Recall

Memorise the **trigger word** in the right column; it instantly pulls the whole concept.

| Concept | Vivid Analogy | Trigger 🔑 |
|---------|---------------|------------|
| **Spring Framework** | A company **manager** who arranges your laptop, desk, ID before you even ask | "Manager arranges everything" |
| **Spring Boot** | A **Swiggy pizza kit** — dough/sauce/cheese pre-made, just assemble & bake | "Just add water" |
| **IoC** | A **restaurant** — you order, the kitchen controls how it's cooked | "You order, kitchen cooks" |
| **DI** | The **waiter brings the ingredients** to your table instead of you farming them | "Waiter brings it to you" |
| **Constructor Injection** | Car factory **installs the engine** before the car leaves the line (can't drive without it) | "Engine bolted in at build" |
| **Field Injection** | Sliding an engine in **through the window** later — works, but sketchy & hard to inspect | "Engine through the window" |
| **Bean** | A **registered employee** on the payroll (vs. a freelancer who walked in) | "On the payroll" |
| **Bean Lifecycle** | Employee: **hired → onboarded (`@PostConstruct`) → works → exit interview (`@PreDestroy`)** | "Hire → onboard → exit" |
| **Singleton scope** | **One school principal** everyone shares | "One principal" |
| **Prototype scope** | A **movie ticket per person** — never shared | "Ticket per person" |
| **Request scope** | A **bank token number** — fresh per visit, discarded after | "Bank token" |
| **Session scope** | A **gym locker** — yours for the whole visit, cleaned after | "Gym locker" |
| **`@Component` vs `@Bean`** | Annotate **your own house** (`@Component`) vs. a **rented Airbnb you describe** (`@Bean`) | "Own it vs. describe it" |
| **`@Qualifier`** | Telling the waiter **"the SMS one, specifically"** when two dishes look alike | "Name it exactly" |
| **`@Primary`** | The **house special** chosen by default unless you ask otherwise | "House special" |
| **`@Autowired`** | "**Spring, fetch me one of these**" | "Fetch me one" |
| **ApplicationContext vs BeanFactory** | **Smartphone (ApplicationContext)** vs. **brick phone (BeanFactory)** — both call, one does everything | "Smartphone vs. brick" |
| **AOP** | A **CCTV/security guard** layered over every room without rebuilding the rooms | "Security guard over every room" |
| **Profiles** | **Same play, different stage sets** for dev/staging/prod | "Same play, different set" |
| **Auto-configuration** | A **smart home** that turns on lights based on what it senses | "Smart home senses & sets up" |
| **Circular dependency** | Two people each saying **"after you"** at a doorway forever | "After you, no after you" |

---

## 🎤 Final Interview Tip

When you don't fully know an answer, **never freeze and never bluff**. Do this instead:

1. **Anchor on what you DO know** — start from the definition you're sure of.
2. **Reason out loud** — "I'd expect it to work like X because Spring tends to favour fail-fast at startup..." Interviewers grade your *thinking*, not just recall.
3. **Tie it to a real scenario** — "On a project I'd reach for this when..." Concrete beats abstract every time.
4. **End with the trade-off** — naming the catch (when *not* to use something) is the single strongest signal of seniority.

And remember the rhythm for **every** answer: **Definition → Problem it solves → Code/annotation → Trade-off.** Practise saying three of these out loud today — fluency comes from your mouth, not just your eyes. You've got this. 🚀
