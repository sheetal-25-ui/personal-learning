# LLD Week 2: Creational and Structural Design Patterns — Interview Answers & Exercise Solutions

Welcome back! 👋 This is your interview-prep companion for Week 2. Last week you learned the *grammar* of good OO design (SOLID). This week you learn the *idioms* — the proven phrases native speakers use. Patterns are how senior engineers talk to each other in code.

## 🏆 The LLD Interview Golden Rule

Whenever an interviewer asks about a design pattern — or you spot one hiding in a design problem — answer in this exact shape. It signals seniority instantly:

> **Definition → Real-life analogy → Problem it solves → Java code (bad vs good) → When to use / when NOT to use**

Most candidates blurt out a textbook definition and freeze. You will walk the interviewer through *why the pattern exists*, show the painful code without it, then the clean code with it, and finish by naming the trade-offs. That last part — knowing when **NOT** to use a pattern — is what separates a mid-level engineer from a senior one. Over-engineering ("I'll make everything a Singleton!") is a red flag in interviews.

**One more thing the FAANG interviewers actually test:** they rarely say "explain Decorator." They say *"design a coffee shop billing system"* or *"design a notification service"* and watch whether the right pattern falls out of your hands naturally. So this guide drills both: the textbook Q&A **and** the "smell the trigger word" instinct.

---

# PART A — Exercise / Practice Solutions

The Plan.md gives us **4 exercises** (3 core + 1 bonus). Here are complete, compilable Java solutions for every one. Each file is self-contained — drop it into a `.java` file, compile with `javac`, run with `java`.

---

## ✅ Exercise 1: Singleton Logger (Swiggy/Ola/Flipkart style)

**The ask:** One and only one logger for the whole app. Multiple loggers fighting over the same file = corrupted logs. Methods: `info`, `warning`, `error`, `getLogs`, `clearLogs`.

```java
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton Logger.
 *
 * Uses the "Bill Pugh" static holder idiom so it is thread-safe WITHOUT the
 * cost of synchronizing every getInstance() call. The inner Holder class is
 * only loaded by the JVM the first time getInstance() is referenced, and class
 * loading is guaranteed thread-safe by the JVM. This is the production-grade
 * way to do lazy Singletons in Java.
 */
public class Logger {

    // Internal storage for all log lines. Private so nothing can bypass our methods.
    private final List<String> logs = new ArrayList<>();

    // Timestamp format chosen for readability in log files; ISO-like, sorts correctly.
    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Private constructor: nobody outside can do `new Logger()`.
    private Logger() { }

    // The static holder. Loaded lazily and thread-safely on first getInstance().
    private static class Holder {
        private static final Logger INSTANCE = new Logger();
    }

    public static Logger getInstance() {
        return Holder.INSTANCE;
    }

    // --- The three log levels. synchronized so concurrent threads don't corrupt the list. ---

    public synchronized void info(String message) {
        log("INFO", message);
    }

    public synchronized void warning(String message) {
        log("WARNING", message);
    }

    public synchronized void error(String message) {
        log("ERROR", message);
    }

    private void log(String level, String message) {
        String line = "[" + level + "] " + LocalDateTime.now().format(TS) + " - " + message;
        logs.add(line);
        System.out.println(line); // also echo to console, like a real logger
    }

    // Return a COPY so callers cannot mutate our internal list (defensive copy).
    public synchronized List<String> getLogs() {
        return Collections.unmodifiableList(new ArrayList<>(logs));
    }

    public synchronized void clearLogs() {
        logs.clear();
    }

    // --- Demo / test harness ---
    public static void main(String[] args) {
        Logger logger1 = Logger.getInstance();
        Logger logger2 = Logger.getInstance();

        logger1.info("Server started");
        logger2.warning("High memory usage");
        logger1.error("Database connection failed");

        // Both references point to the SAME object:
        if (logger1 != logger2) {
            throw new AssertionError("Logger is not a Singleton!");
        }
        System.out.println("logger1 == logger2 ? " + (logger1 == logger2)); // true

        // Logs added via logger1 are visible via logger2 — same object:
        if (logger2.getLogs().size() != 3) {
            throw new AssertionError("Expected 3 logs, got " + logger2.getLogs().size());
        }

        System.out.println("\nAll logs:");
        logger2.getLogs().forEach(System.out::println);

        logger2.clearLogs();
        System.out.println("\nAfter clear, log count = " + logger1.getLogs().size()); // 0
    }
}
```

**Sample output:**
```
[INFO] 2026-06-12 10:30:01 - Server started
[WARNING] 2026-06-12 10:30:01 - High memory usage
[ERROR] 2026-06-12 10:30:01 - Database connection failed
logger1 == logger2 ? true

All logs:
[INFO] 2026-06-12 10:30:01 - Server started
[WARNING] 2026-06-12 10:30:01 - High memory usage
[ERROR] 2026-06-12 10:30:01 - Database connection failed

After clear, log count = 0
```

**Why this beats the naive version:** The Plan.md showed the `if (instance == null)` lazy approach, which is *not* thread-safe. In an interview, if you write the basic version, say out loud: *"This is the teaching version; for production I'd use the Bill Pugh static holder to make it thread-safe lazily."* That single sentence earns big points.

---

## ✅ Exercise 2: Vehicle Factory (Ola/Uber style)

**The ask:** Base `Vehicle` with `start/stop/fuelType/capacity/baseFare`. Concrete `Auto/Bike/MiniCar/Sedan/SUV`. A `VehicleFactory.create(rideType)` and a `calculateFare(rideType, distanceKm)`.

```java
import java.util.Locale;

// --- The product contract: every vehicle must answer these. ---
abstract class Vehicle {
    public abstract void start();
    public abstract void stop();
    public abstract String fuelType();
    public abstract int capacity();
    public abstract double baseFare();
}

class Auto extends Vehicle {
    public void start() { System.out.println("Auto started. Hold tight!"); }
    public void stop()  { System.out.println("Auto stopped."); }
    public String fuelType() { return "CNG"; }
    public int capacity() { return 3; }
    public double baseFare() { return 30; }
}

class Bike extends Vehicle {
    public void start() { System.out.println("Bike started. Wear your helmet!"); }
    public void stop()  { System.out.println("Bike stopped."); }
    public String fuelType() { return "Petrol"; }
    public int capacity() { return 1; }
    public double baseFare() { return 15; }
}

class MiniCar extends Vehicle {
    public void start() { System.out.println("MiniCar started. Budget-friendly ride!"); }
    public void stop()  { System.out.println("MiniCar stopped."); }
    public String fuelType() { return "Petrol"; }
    public int capacity() { return 4; }
    public double baseFare() { return 50; }
}

class Sedan extends Vehicle {
    public void start() { System.out.println("Sedan started. Enjoy your comfortable ride!"); }
    public void stop()  { System.out.println("Sedan stopped."); }
    public String fuelType() { return "Petrol/Diesel"; }
    public int capacity() { return 4; }
    public double baseFare() { return 80; }
}

class SUV extends Vehicle {
    public void start() { System.out.println("SUV started. Plenty of space for everyone!"); }
    public void stop()  { System.out.println("SUV stopped."); }
    public String fuelType() { return "Diesel"; }
    public int capacity() { return 6; }
    public double baseFare() { return 120; }
}

// --- The Factory: the ONE place that maps a string to a concrete class. ---
class VehicleFactory {
    public static Vehicle create(String rideType) {
        if (rideType == null) {
            throw new IllegalArgumentException("Ride type cannot be null");
        }
        switch (rideType.toLowerCase(Locale.ROOT)) {
            case "auto":    return new Auto();
            case "bike":    return new Bike();
            case "mini":    return new MiniCar();
            case "sedan":   return new Sedan();
            case "suv":     return new SUV();
            default:
                throw new IllegalArgumentException("Unknown ride type: " + rideType);
        }
    }
}

public class VehicleFactoryDemo {

    // Per-km rate: bikes are cheaper (Rs.12/km), everything else Rs.15/km.
    private static final double BIKE_RATE_PER_KM = 12;
    private static final double DEFAULT_RATE_PER_KM = 15;

    static double calculateFare(String rideType, double distanceKm) {
        Vehicle v = VehicleFactory.create(rideType);
        double perKm = (v instanceof Bike) ? BIKE_RATE_PER_KM : DEFAULT_RATE_PER_KM;
        return v.baseFare() + (distanceKm * perKm);
    }

    public static void main(String[] args) {
        Vehicle vehicle = VehicleFactory.create("sedan");
        vehicle.start();                          // Sedan started. Enjoy your comfortable ride!
        System.out.println(vehicle.fuelType());   // Petrol/Diesel
        System.out.println(vehicle.capacity());   // 4

        double autoFare = calculateFare("auto", 5);   // 30 + 5*15 = 105
        System.out.println("Auto fare for 5 km: Rs." + autoFare);

        double bikeFare = calculateFare("bike", 5);   // 15 + 5*12 = 75
        System.out.println("Bike fare for 5 km: Rs." + bikeFare);

        double suvFare = calculateFare("suv", 10);    // 120 + 10*15 = 270
        System.out.println("SUV fare for 10 km: Rs." + suvFare);
    }
}
```

**Sample output:**
```
Sedan started. Enjoy your comfortable ride!
Petrol/Diesel
4
Auto fare for 5 km: Rs.105.0
Bike fare for 5 km: Rs.75.0
SUV fare for 10 km: Rs.270.0
```

**Interview polish:** Notice `calculateFare` doesn't care which concrete class it gets — it programs to the `Vehicle` abstraction. Adding "Electric Auto" tomorrow = one new class + one `case`. Zero changes to `calculateFare`. That is the **Open/Closed Principle** (from Week 1's SOLID) being *delivered* by the Factory pattern. Connect the two out loud in the interview.

> ⚠️ A small note: using `instanceof Bike` to pick the rate works for this exercise, but in a richer design you'd add an abstract `ratePerKm()` method to `Vehicle` so the per-km rate lives with each vehicle and you never branch on type. Mention this — interviewers love hearing you critique your own code.

---

## ✅ Exercise 3: Coffee Builder (Starbucks / CCD style)

**The ask:** A `Coffee` with many optional parts, built via a chainable `CoffeeBuilder`. Plus `toString()` and `price()`.

```java
import java.util.ArrayList;
import java.util.List;

class Coffee {
    // Package-private fields set by the builder. Immutable-ish: no setters exposed.
    final String size;          // small / medium / large
    final String milkType;      // regular / oat / almond / soy / none
    final int sugarSpoons;      // 0 = no sugar
    final String flavor;        // vanilla / hazelnut / caramel / mocha / null
    final boolean whippedCream;
    final boolean extraShot;
    final boolean iced;         // true = cold coffee, false = hot

    Coffee(CoffeeBuilder b) {
        this.size = b.size;
        this.milkType = b.milkType;
        this.sugarSpoons = b.sugarSpoons;
        this.flavor = b.flavor;
        this.whippedCream = b.whippedCream;
        this.extraShot = b.extraShot;
        this.iced = b.iced;
    }

    public double price() {
        double price;
        switch (size) {
            case "small":  price = 150; break;
            case "medium": price = 200; break;
            case "large":  price = 250; break;
            default: throw new IllegalStateException("Unknown size: " + size);
        }
        // Alternative (non-regular, non-none) milk costs Rs.30.
        if (milkType != null && !milkType.equals("regular") && !milkType.equals("none")) {
            price += 30;
        }
        if (flavor != null)  price += 20;  // any flavor syrup
        if (whippedCream)    price += 30;
        if (extraShot)       price += 40;
        return price;
    }

    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add(capitalize(size));
        parts.add(iced ? "iced" : "hot");
        parts.add("coffee");

        if (milkType == null || milkType.equals("none")) {
            parts.add("with no milk");
        } else {
            parts.add("with " + milkType + " milk");
        }

        parts.add(sugarSpoons == 0 ? "no sugar" : sugarSpoons + " sugar");

        if (flavor != null)  parts.add(flavor + " flavor");
        if (whippedCream)    parts.add("whipped cream");
        if (extraShot)       parts.add("extra shot");

        return String.join(", ", parts);
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}

class CoffeeBuilder {
    // Sensible defaults so a minimal coffee is still valid.
    String size = "small";
    String milkType = "none";
    int sugarSpoons = 0;
    String flavor = null;
    boolean whippedCream = false;
    boolean extraShot = false;
    boolean iced = false;

    public CoffeeBuilder size(String size) { this.size = size; return this; }
    public CoffeeBuilder milk(String type) { this.milkType = type; return this; }
    public CoffeeBuilder sugar(int spoons)  { this.sugarSpoons = spoons; return this; }
    public CoffeeBuilder flavor(String name){ this.flavor = name; return this; }
    public CoffeeBuilder withWhippedCream() { this.whippedCream = true; return this; }
    public CoffeeBuilder extraShot()        { this.extraShot = true; return this; }
    public CoffeeBuilder iced()             { this.iced = true; return this; }

    public Coffee build() {
        // A good builder VALIDATES before constructing. Fail fast on bad input.
        if (!size.equals("small") && !size.equals("medium") && !size.equals("large")) {
            throw new IllegalStateException("Size must be small, medium, or large");
        }
        return new Coffee(this);
    }
}

public class CoffeeBuilderDemo {
    public static void main(String[] args) {
        Coffee coffee = new CoffeeBuilder()
                .size("large")
                .milk("oat")
                .sugar(2)
                .flavor("hazelnut")
                .withWhippedCream()
                .iced()
                .build();

        System.out.println(coffee);
        // Large, iced, coffee, with oat milk, 2 sugar, hazelnut flavor, whipped cream
        System.out.println("Price: Rs." + coffee.price());
        // 250 + 30 (oat) + 20 (flavor) + 30 (whipped cream) = 330
        System.out.println();

        Coffee simple = new CoffeeBuilder()
                .size("small")
                .build();
        System.out.println(simple);   // Small, hot, coffee, with no milk, no sugar
        System.out.println("Price: Rs." + simple.price());  // 150
    }
}
```

**Sample output:**
```
Large, iced, coffee, with oat milk, 2 sugar, hazelnut flavor, whipped cream
Price: Rs.330.0

Small, hot, coffee, with no milk, no sugar
Price: Rs.150.0
```

**Interview polish:** The killer feature of Builder isn't just readability — it's that **optional fields don't force you to pass `null`**. A constructor with 7 params would force `new Coffee("large", "oat", 2, "hazelnut", true, false, true)` — quick, which boolean was `iced`? Builder names every step, so the call reads like English. Also, the builder gives you ONE place to validate (`build()`), so a half-built, invalid Coffee can never escape.

---

## ✅ Exercise 4 (Bonus): Combine Patterns — Payment Processing System

**The ask:** Combine **Singleton** (one `PaymentProcessor`) + **Factory** (pick the gateway) + **Adapter** (each gateway wraps a third-party SDK with a foreign interface). This is the realistic "show me you can mix patterns" interview question.

```java
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// ============================================================
// 1. The interface OUR application speaks. Everything adapts to this.
// ============================================================
interface PaymentGateway {
    PaymentResult pay(double amountInRupees, String description);
}

// A small value object for results — cleaner than passing raw Maps around.
class PaymentResult {
    final boolean success;
    final String transactionId;
    final String gatewayName;

    PaymentResult(boolean success, String transactionId, String gatewayName) {
        this.success = success;
        this.transactionId = transactionId;
        this.gatewayName = gatewayName;
    }

    @Override
    public String toString() {
        return gatewayName + " -> " + (success ? "SUCCESS" : "FAILED")
                + " (txn=" + transactionId + ")";
    }
}

// ============================================================
// 2. Three "third-party SDKs" we cannot modify. Each has a DIFFERENT interface.
// ============================================================
class UPIServiceSDK {                       // speaks in rupees + VPA
    public Map<String, Object> initiateCollect(double rupees, String vpa) {
        System.out.println("[UPI SDK] Collecting Rs." + rupees + " from " + vpa);
        Map<String, Object> r = new HashMap<>();
        r.put("ref", "upi_ref_001");
        r.put("ok", true);
        return r;
    }
}

class CardProcessorSDK {                    // speaks in CENTS + token
    public Map<String, Object> authorizeAndCapture(long amountCents, String cardToken) {
        System.out.println("[CARD SDK] Charging " + amountCents + " cents on " + cardToken);
        Map<String, Object> r = new HashMap<>();
        r.put("auth_code", "card_auth_777");
        r.put("approved", "Y");
        return r;
    }
}

class NetBankingSDK {                        // speaks in paise + bank code
    public boolean debit(int paise, String bankCode, StringBuilder outReferenceId) {
        System.out.println("[NETBANKING SDK] Debiting " + paise + " paise from bank " + bankCode);
        outReferenceId.append("nb_txn_555");
        return true;
    }
}

// ============================================================
// 3. Adapters: translate OUR PaymentGateway interface to each SDK's dialect.
// ============================================================
class UPIAdapter implements PaymentGateway {
    private final UPIServiceSDK sdk = new UPIServiceSDK();
    private final String vpa;

    UPIAdapter(String vpa) { this.vpa = vpa; }

    @Override
    public PaymentResult pay(double amountInRupees, String description) {
        Map<String, Object> res = sdk.initiateCollect(amountInRupees, vpa);
        boolean ok = Boolean.TRUE.equals(res.get("ok"));
        return new PaymentResult(ok, String.valueOf(res.get("ref")), "UPI");
    }
}

class CardAdapter implements PaymentGateway {
    private final CardProcessorSDK sdk = new CardProcessorSDK();
    private final String cardToken;

    CardAdapter(String cardToken) { this.cardToken = cardToken; }

    @Override
    public PaymentResult pay(double amountInRupees, String description) {
        // Card SDK wants cents (smallest unit). Round carefully to avoid float drift.
        long cents = Math.round(amountInRupees * 100);
        Map<String, Object> res = sdk.authorizeAndCapture(cents, cardToken);
        boolean ok = "Y".equals(res.get("approved"));
        return new PaymentResult(ok, String.valueOf(res.get("auth_code")), "CARD");
    }
}

class NetBankingAdapter implements PaymentGateway {
    private final NetBankingSDK sdk = new NetBankingSDK();
    private final String bankCode;

    NetBankingAdapter(String bankCode) { this.bankCode = bankCode; }

    @Override
    public PaymentResult pay(double amountInRupees, String description) {
        int paise = (int) Math.round(amountInRupees * 100);
        StringBuilder ref = new StringBuilder();
        boolean ok = sdk.debit(paise, bankCode, ref);
        return new PaymentResult(ok, ref.toString(), "NETBANKING");
    }
}

// ============================================================
// 4. Factory: pick the correct adapter based on the method string.
// ============================================================
class PaymentGatewayFactory {
    public static PaymentGateway create(String method) {
        switch (method.toLowerCase(Locale.ROOT)) {
            case "upi":        return new UPIAdapter("sheetal@oksbi");
            case "card":       return new CardAdapter("tok_visa_4242");
            case "netbanking": return new NetBankingAdapter("HDFC0001234");
            default:
                throw new IllegalArgumentException("Unsupported payment method: " + method);
        }
    }
}

// ============================================================
// 5. Singleton: ONE PaymentProcessor for the whole app, using the Factory.
// ============================================================
class PaymentProcessor {
    private PaymentProcessor() { }

    private static class Holder {
        private static final PaymentProcessor INSTANCE = new PaymentProcessor();
    }

    public static PaymentProcessor getInstance() {
        return Holder.INSTANCE;
    }

    public PaymentResult process(String method, double amount, String description) {
        // The processor never news-up a concrete gateway: it delegates to the Factory.
        PaymentGateway gateway = PaymentGatewayFactory.create(method);
        return gateway.pay(amount, description);
    }
}

public class PaymentSystemDemo {
    public static void main(String[] args) {
        // Singleton: every call returns the same processor.
        PaymentProcessor p1 = PaymentProcessor.getInstance();
        PaymentProcessor p2 = PaymentProcessor.getInstance();
        System.out.println("Same processor? " + (p1 == p2)); // true

        System.out.println(p1.process("upi", 499.99, "Order #1001"));
        System.out.println(p1.process("card", 1299.50, "Order #1002"));
        System.out.println(p1.process("netbanking", 250.00, "Order #1003"));
    }
}
```

**Sample output:**
```
Same processor? true
[UPI SDK] Collecting Rs.499.99 from sheetal@oksbi
UPI -> SUCCESS (txn=upi_ref_001)
[CARD SDK] Charging 129950 cents on tok_visa_4242
CARD -> SUCCESS (txn=card_auth_777)
[NETBANKING SDK] Debiting 25000 paise from bank HDFC0001234
NETBANKING -> SUCCESS (txn=nb_txn_555)
```

**Why this answer wins the interview:** You demonstrated three patterns working together, each doing ONE job:
- **Singleton** → exactly one stateless processor (cheap to share, no duplication).
- **Factory** → the `process()` method is blind to concrete gateway classes (Open/Closed: add "wallet" with one class + one `case`).
- **Adapter** → each foreign SDK (cents, paise, VPA, bank codes) is translated to our single clean `pay(rupees, description)` contract.

Say this sentence to the interviewer: *"The Factory decides **which** gateway, the Adapter handles **how** each gateway talks, and the Singleton ensures there's **one** entry point. Each pattern has a single, separate responsibility — that's why they compose cleanly."*

---

# PART B — Interview Questions & Model Answers

For each pattern: **problem it solves → real-life analogy → correct Java → when to use / pitfalls.** Then a battery of cross-cutting questions with comparison tables.

---

## 🔹 Pattern 1 — Singleton

**Q: What is the Singleton pattern and what problem does it solve?**

> Singleton ensures a class has **exactly one instance** for the entire application lifetime and gives a single global access point to it. It solves the problem of *uncontrolled duplication of a resource that is meant to be shared* — like a database connection pool, a logger, or a configuration object. Creating ten DB connection pools wastes sockets and can exhaust the database's connection limit; ten loggers fighting over one file corrupt it. Singleton guarantees coordination because everyone literally shares the same object in memory.

**Real-life analogy:** The Reserve Bank of India (RBI) or Aadhaar/UIDAI. There is *one* central system; every bank and app talks to the same one. A second RBI would create chaos (conflicting records).

**Correct, thread-safe Java (production-grade Bill Pugh idiom):**

```java
public class ConfigManager {
    private final java.util.Map<String, String> settings = new java.util.HashMap<>();

    private ConfigManager() {
        settings.put("env", "production");   // load config once, here
    }

    // Inner holder is loaded by the JVM only on first getInstance() call.
    // Class initialization is thread-safe by the JLS — no synchronized needed.
    private static class Holder {
        private static final ConfigManager INSTANCE = new ConfigManager();
    }

    public static ConfigManager getInstance() {
        return Holder.INSTANCE;
    }

    public String get(String key) { return settings.get(key); }
}
```

**When to use:** DB connection pool, logger, config manager, cache manager, thread pool — anything where "exactly one shared instance" is a hard requirement.

**Pitfalls / when NOT to use:**
- Don't make business entities (`User`, `Order`) Singletons — you have millions of them.
- Singletons are **global mutable state in disguise** → they make unit tests hard (you can't easily swap a mock). Prefer dependency injection where possible.
- The naive `if (instance == null)` version is **NOT thread-safe**. Two threads can both see `null` and create two instances.

---

## 🔹 Pattern 2 — Factory

**Q: What is the Factory pattern and what problem does it solve?**

> Factory centralizes object creation behind a single method so the caller asks for *what* it wants ("give me a notification for this channel") without knowing *which concrete class* gets built or how. It solves **code duplication** (the same `if/else new ...` block scattered everywhere) and **tight coupling** (every caller knowing every concrete type's constructor details). Adding a new type becomes a one-place change.

**Real-life analogy:** Ordering a dosa at a restaurant — you say what you want; the kitchen (factory) handles all the cooking/creation. Or Zomato's notification system: ask for a notification by channel, get the right Email/SMS/WhatsApp object back.

**Correct Java:**

```java
interface Notification { void send(String message); }

class EmailNotification implements Notification {
    public void send(String m) { System.out.println("[EMAIL] " + m); }
}
class SMSNotification implements Notification {
    public void send(String m) { System.out.println("[SMS] " + m); }
}

class NotificationFactory {
    public static Notification create(String channel) {
        switch (channel) {
            case "email": return new EmailNotification();
            case "sms":   return new SMSNotification();
            default: throw new IllegalArgumentException("Unknown channel: " + channel);
        }
    }
}

// Usage — caller is blind to concrete classes:
// Notification n = NotificationFactory.create(user.getPref());
// n.send("Order confirmed!");
```

**When to use:** You create different objects based on some input (string/enum/config); you have a family of related classes; you want adding a new type to be a one-place change (Open/Closed Principle).

**Pitfalls / when NOT to use:** Only one type → factory is overkill. Trivial creation (just `new X()`) → no benefit. If callers genuinely need the concrete type's special methods, a factory hiding the type fights them.

---

## 🔹 Pattern 3 — Builder

**Q: What is the Builder pattern and what problem does it solve?**

> Builder constructs a complex object step-by-step using chained, named method calls, instead of a constructor with a long list of positional (often optional) parameters. It solves the **telescoping constructor** problem and the **unreadable call site** problem — `new Pizza("L", true, false, null, 2, true)` is impossible to read, while `.size("L").cheese().sugar(2).build()` reads like English. It also gracefully handles optional fields without forcing `null` arguments, and gives one place (`build()`) to validate.

**Real-life analogy:** Customizing a Domino's pizza online or filling a Swiggy Instamart cart — choose size, then crust, then add toppings one at a time, skip what you don't want, then "place order."

**Correct Java:**

```java
class HttpRequest {
    private final String url;
    private final String method;
    private final java.util.Map<String, String> headers;

    private HttpRequest(Builder b) {
        this.url = b.url; this.method = b.method; this.headers = b.headers;
    }

    static class Builder {
        private final String url;                        // required → constructor arg
        private String method = "GET";                   // optional → default
        private final java.util.Map<String, String> headers = new java.util.HashMap<>();

        Builder(String url) { this.url = url; }          // force required fields up front

        Builder method(String m) { this.method = m; return this; }
        Builder header(String k, String v) { headers.put(k, v); return this; }

        HttpRequest build() {
            if (url == null || url.isEmpty())
                throw new IllegalStateException("URL is required");
            return new HttpRequest(this);
        }
    }
}

// HttpRequest req = new HttpRequest.Builder("https://api.zomato.com")
//         .method("POST").header("Auth", "token123").build();
```

**When to use:** Objects with many parameters (4-5+), especially optional ones; when you want readable, self-documenting construction; when you want immutable objects built in steps (SQL query builders, HTTP requests, test data).

**Pitfalls / when NOT to use:** Only 2-3 required fields → a plain constructor is simpler. If the object never varies, a builder is dead weight.

---

## 🔹 Pattern 4 — Adapter

**Q: What is the Adapter pattern and what problem does it solve?**

> Adapter wraps a class whose interface is incompatible with what your code expects, translating between the two without changing either side. It solves the problem of **integrating foreign code** — a third-party SDK, a legacy module, a different vendor — that speaks a different "language" (different method names, units, or data shapes) than your application's interface.

**Real-life analogy:** A travel power adapter letting your Indian plug work in a US socket. Or UPI/NPCI sitting between payment apps and banks — the apps speak one standard format, UPI translates to each bank's format; neither side had to change.

**Correct Java:**

```java
// Target interface our code expects:
interface PaymentGateway { boolean pay(double rupees, String desc); }

// Foreign SDK we cannot modify (wants paise):
class RazorpaySDK {
    java.util.Map<String,Object> createPayment(int paise) {
        java.util.Map<String,Object> r = new java.util.HashMap<>();
        r.put("status", "success"); return r;
    }
}

// Adapter translates rupees<->paise and response<->boolean:
class RazorpayAdapter implements PaymentGateway {
    private final RazorpaySDK sdk = new RazorpaySDK();
    public boolean pay(double rupees, String desc) {
        int paise = (int) Math.round(rupees * 100);           // unit translation
        java.util.Map<String,Object> res = sdk.createPayment(paise);
        return "success".equals(res.get("status"));           // shape translation
    }
}
```

**When to use:** Integrating third-party libraries/APIs with mismatched interfaces; swapping one vendor for another (payment gateways, email providers); bridging legacy and new code.

**Pitfalls / when NOT to use:** If you control both sides, just change one to match the other. If the mismatch is a trivial method rename, an adapter is overkill. Don't use an adapter to mask a design you should actually fix.

---

## 🔹 Pattern 5 — Decorator

**Q: What is the Decorator pattern and what problem does it solve?**

> Decorator dynamically adds behavior to an object by wrapping it in another object that shares the **same interface**. Because the wrapper looks identical to the wrapped thing, you can stack decorators in any combination and order. It solves the **class explosion** problem: without it, supporting N optional features by subclassing requires 2^N subclasses (LoggingCaching, LoggingAuth, CachingAuth...). With decorators you write N small classes that combine freely.

**Real-life analogy:** Adding toppings to a Subway sandwich — cheese wraps the base, jalapeños wrap the cheese; each layer adds something without altering what's underneath. Or Flipkart's API middleware: auth → rate-limit → logging → caching, each layer wrapping the next.

**Correct Java:**

```java
interface DataService { String getData(String query); }

class RealDataService implements DataService {
    public String getData(String q) { return "data for " + q; }
}

// Base decorator holds a reference to the wrapped service.
class LoggingDecorator implements DataService {
    private final DataService wrapped;
    LoggingDecorator(DataService w) { this.wrapped = w; }
    public String getData(String q) {
        System.out.println("[LOG] query=" + q);
        return wrapped.getData(q);                 // delegate
    }
}

class CachingDecorator implements DataService {
    private final DataService wrapped;
    private final java.util.Map<String,String> cache = new java.util.HashMap<>();
    CachingDecorator(DataService w) { this.wrapped = w; }
    public String getData(String q) {
        return cache.computeIfAbsent(q, wrapped::getData);
    }
}

// Stack them: caching wraps logging wraps the real service.
// DataService s = new CachingDecorator(new LoggingDecorator(new RealDataService()));
```

**When to use:** Cross-cutting concerns layered onto core logic — logging, caching, auth, metrics, rate limiting; when behavior must be added at runtime; when subclassing would explode combinatorially. (Java's `java.io` streams — `BufferedReader(new FileReader(...))` — are the classic real example.)

**Pitfalls / when NOT to use:** If you only ever need one fixed combination, subclassing is simpler. Deep stacks (5+ layers) make debugging and stack traces painful. A "decorator" that changes the interface isn't a decorator.

---

## 🔹 Pattern 6 — Facade

**Q: What is the Facade pattern and what problem does it solve?**

> Facade provides one simplified entry point to a complex subsystem of many classes. It solves the problem of **clients having to know and orchestrate too much** — which subsystems exist, what order to call them in, and how to handle each one's errors. The facade encapsulates that choreography behind a single method.

**Real-life analogy:** MakeMyTrip's "Book Trip" — behind one button it talks to airlines, hotels, cabs, insurance, visa. Or Swiggy's "Place Order": one tap triggers inventory, pricing, payment, kitchen, delivery assignment, and notifications in the right sequence.

**Correct Java:**

```java
class Inventory { boolean inStock(String id) { return true; } }
class Pricing   { double total(double sub)   { return sub * 1.05 + 30; } }
class Payment   { boolean charge(double amt) { return true; } }
class Delivery  { String assignRider()       { return "rider_42"; } }

// The Facade hides the 4-system dance behind ONE method.
class OrderFacade {
    private final Inventory inventory = new Inventory();
    private final Pricing pricing = new Pricing();
    private final Payment payment = new Payment();
    private final Delivery delivery = new Delivery();

    public String placeOrder(String itemId, double subtotal) {
        if (!inventory.inStock(itemId)) return "FAILED: out of stock";
        double total = pricing.total(subtotal);
        if (!payment.charge(total)) return "FAILED: payment";
        String rider = delivery.assignRider();
        return "SUCCESS: total=Rs." + total + ", rider=" + rider;
    }
}
// Client just calls: new OrderFacade().placeOrder("item1", 400);
```

**When to use:** A complex subsystem with many classes used together in sequence; you want a clean public API for other teams/apps; you want to reduce coupling between subsystem and clients. (API gateways in microservices are facades.)

**Pitfalls / when NOT to use:** If the subsystem is already simple (1-2 classes), a facade is needless indirection. A facade should *simplify*, not *imprison* — keep subsystems accessible for clients that need fine control. Avoid a single "god facade" wrapping the entire app.

---

## 🔸 Cross-Cutting Interview Questions & Model Answers

### Q1. Singleton vs. a class with all static methods — what's the difference? When prefer which?

A **static utility class** (all methods `static`, never instantiated, like `java.lang.Math`) and a **Singleton** both give you "one shared thing," but they differ in important ways:

| Aspect | Singleton | Static class |
|---|---|---|
| Is it an object? | Yes — a real instance | No — just methods on a class |
| Can implement an interface? | ✅ Yes | ❌ No (static methods can't satisfy interfaces) |
| Can be passed as a parameter / injected? | ✅ Yes | ❌ No |
| Polymorphism / can be subclassed or mocked? | ✅ Yes | ❌ No |
| Lazy initialization? | ✅ Easy | Harder (static init is eager-ish) |
| Holds state cleanly? | ✅ Designed for it | Possible but smells (global static state) |
| Best for | Shared *object* with state (cache, connection pool) | Stateless pure functions (Math, StringUtils) |

**Model answer:** "Use a static class for stateless helpers like `Math.max`. Use a Singleton when the single thing is a real *object* with state and behavior — a connection pool or cache — especially if I want it to implement an interface so I can mock it in tests or swap implementations. Singleton keeps polymorphism; static classes throw it away."

---

### Q2. Factory vs. Abstract Factory — what's the difference?

| | **Factory (Method)** | **Abstract Factory** |
|---|---|---|
| Creates | ONE product type | a **family** of related products |
| Example | `NotificationFactory.create("sms")` → one Notification | `UIFactory` → creates Button + Checkbox + Menu that all match a theme |
| Granularity | One method, one product | An interface with multiple create methods |
| When | Vary which single object you make | Vary an entire set of objects that must be consistent together |

**Model answer:** "A simple Factory makes one kind of product based on a parameter. An Abstract Factory is a factory *of factories* — it produces a whole *family* of related objects that must go together. Classic example: a `WindowsUIFactory` makes a Windows button, Windows checkbox, Windows scrollbar — all consistent — while a `MacUIFactory` makes the Mac variants of all three. You switch the whole family by switching one factory. Use Abstract Factory when products come in matching *sets*; use plain Factory when you just need to vary one object."

**Mini Abstract Factory in Java:**
```java
interface Button { void render(); }
interface Checkbox { void render(); }

interface UIFactory {                 // the abstract factory: a family creator
    Button createButton();
    Checkbox createCheckbox();
}

class WindowsButton implements Button { public void render(){ System.out.println("Win button"); } }
class WindowsCheckbox implements Checkbox { public void render(){ System.out.println("Win checkbox"); } }
class WindowsFactory implements UIFactory {
    public Button createButton() { return new WindowsButton(); }
    public Checkbox createCheckbox() { return new WindowsCheckbox(); }
}
// A MacFactory would create MacButton + MacCheckbox — a matching family.
```

---

### Q3. Is the Singleton an anti-pattern?

**Model answer:** "It's not inherently an anti-pattern, but it's the *most abused* pattern, so it's earned a bad reputation. Used correctly — for a genuinely single shared resource like a connection pool — it's fine. The problems come from misuse:
1. **Hidden global state.** A Singleton accessed via `getInstance()` everywhere is a global variable wearing a tuxedo. It hides dependencies — a class secretly depends on it without declaring it in its constructor.
2. **Testing pain.** You can't easily substitute a mock, and state leaks between tests because the instance persists.
3. **Tight coupling.** Code calling `Logger.getInstance()` is welded to that concrete class.

The modern fix is **dependency injection**: create one instance at app startup (the DI container guarantees 'one') and *inject* it where needed, rather than reaching for a global `getInstance()`. So: Singleton the *concept* (one instance) — good; Singleton the *global static access pattern* — use sparingly. In an interview I'd say 'I'd prefer to manage the single instance via DI, but the classic Singleton is acceptable for a logger or config.'"

---

### Q4. How do you make a Singleton thread-safe? Walk through the options.

| Approach | Thread-safe? | Lazy? | Notes |
|---|---|---|---|
| `if (instance == null) instance = new X()` | ❌ No | ✅ | Two threads can both see null → two instances. **Never in production.** |
| `synchronized getInstance()` | ✅ Yes | ✅ | Correct but slow — locks on *every* call, even after init. |
| Double-checked locking (`volatile` + double `if`) | ✅ Yes | ✅ | Fast, but tricky; `volatile` is **required** or you get a partially-constructed object. |
| **Bill Pugh static holder** (inner class) | ✅ Yes | ✅ | Best of both — lazy + fast + simple. JVM guarantees class-init thread safety. |
| **`enum` Singleton** | ✅ Yes | ❌ (eager) | Simplest, serialization-safe, reflection-proof. Joshua Bloch's recommendation. |

**Double-checked locking (know this one — interviewers love it):**
```java
public class Singleton {
    private static volatile Singleton instance;  // volatile is MANDATORY here
    private Singleton() {}
    public static Singleton getInstance() {
        if (instance == null) {                  // 1st check (no lock, fast path)
            synchronized (Singleton.class) {
                if (instance == null) {          // 2nd check (inside lock)
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```
**Why `volatile`?** Without it, `instance = new Singleton()` can be reordered so the reference is published *before* the constructor finishes, letting another thread see a half-built object. `volatile` forbids that reordering.

**The enum trick (cleanest):**
```java
public enum Singleton {
    INSTANCE;
    public void doWork() { /* ... */ }
}
// Usage: Singleton.INSTANCE.doWork();
```

---

### Q5. Adapter vs. Decorator vs. Facade — they all "wrap" something. How are they different?

All three hold a reference to another object, but their **intent** differs — and interviewers test whether you know intent, not mechanism:

| Pattern | Intent | Interface relationship | One-liner |
|---|---|---|---|
| **Adapter** | Make incompatible interfaces work together | **Changes** the interface (target ≠ adaptee) | "Translate" |
| **Decorator** | Add behavior without changing interface | **Keeps the same** interface, adds behavior | "Enhance" |
| **Facade** | Simplify a complex subsystem | **New, simpler** interface over many classes | "Simplify" |

**Model answer:** "Adapter changes an interface to match what the client expects — same behavior, different shape. Decorator keeps the exact same interface but adds new behavior, and can be stacked. Facade introduces a brand-new simplified interface in front of *many* objects to hide their complexity. Memory hook: Adapter *translates*, Decorator *enhances*, Facade *simplifies*."

---

### Q6. Decorator vs. inheritance (subclassing) — why prefer Decorator?

**Model answer:** "Inheritance fixes behavior at *compile time* and explodes combinatorially. If I have Logging, Caching, and Auth as features, subclassing forces me to create every combination: `LoggingCaching`, `LoggingAuth`, `CachingAuth`, `LoggingCachingAuth`... 2^N classes. Decorator gives me N classes that combine at *runtime* in any order I want. I can decide 'this request needs caching + auth but not logging' on the fly. Also, decorators follow 'favor composition over inheritance' (from Week 1) — they compose objects instead of rigidly extending classes."

---

### Q7. Builder vs. telescoping constructors vs. setters (JavaBeans)?

| Approach | Readable? | Immutable result? | Safe (no half-built object)? |
|---|---|---|---|
| Telescoping constructors (`new X(a)`, `new X(a,b)`, ...) | ❌ Poor at 4+ args | ✅ | ✅ |
| Setters / JavaBeans (`x.setA(); x.setB();`) | ✅ OK | ❌ Mutable | ❌ Object exists in inconsistent state mid-build |
| **Builder** | ✅ Excellent | ✅ Can be immutable | ✅ Validates in `build()` |

**Model answer:** "Telescoping constructors become unreadable and you can't skip middle optional params. JavaBean setters fix readability but leave the object mutable and temporarily in a half-built, possibly-invalid state — bad for thread safety. Builder gives readable, chained construction, produces an immutable object, and validates once in `build()` so an invalid object can never escape."

---

### Q8. Where do these patterns show up in the JDK / real frameworks? (Interviewers love this — it proves you've read real code.)

| Pattern | Real JDK / framework example |
|---|---|
| Singleton | `Runtime.getRuntime()`, Spring beans (default scope = singleton) |
| Factory | `Calendar.getInstance()`, `NumberFormat.getInstance()`, JDBC `DriverManager.getConnection()` |
| Builder | `StringBuilder`, `Stream.Builder`, `java.time` builders, OkHttp `Request.Builder` |
| Adapter | `java.util.Arrays.asList()` (array → List), `InputStreamReader` (byte stream → char stream) |
| Decorator | `java.io` streams: `new BufferedReader(new FileReader(f))`, `Collections.unmodifiableList()` |
| Facade | `javax.faces`, SLF4J over logging backends, Spring's `JdbcTemplate` over raw JDBC |

---

### Q9. Given the problem "Design a notification system," which pattern(s) and why?

**Model answer:** "I'd reach for **Factory** to create the right channel object (Email/SMS/WhatsApp/Push) from the user's preference string, so the calling code stays blind to concrete types. If notifications need optional cross-cutting layers — retry, rate-limiting, delivery logging — I'd add **Decorator** to wrap a notification with those concerns. And if 'how to choose a channel at runtime' is itself a swappable policy (cheapest channel vs. fastest vs. user-preferred), that's a **Strategy** (next week). I'd start with Factory, and explicitly say I'm not adding Decorator/Strategy yet to avoid over-engineering — only if requirements demand it."

(That last sentence — naming a pattern but *declining* to add it yet — is a senior signal. Interviewers want to see restraint, not pattern-stuffing.)

---

### Q10. Can a Singleton be broken? How would you defend it?

**Model answer:** "Yes — three classic attacks:
1. **Reflection** can call the private constructor (`constructor.setAccessible(true)`). Defense: throw an exception from the constructor if the instance already exists, or use an `enum` Singleton (reflection can't instantiate enums).
2. **Serialization** can create a second instance on deserialization. Defense: implement `readResolve()` to return the existing instance.
3. **Cloning** via `clone()`. Defense: override `clone()` to throw `CloneNotSupportedException`.

The cleanest blanket defense against all three is the `enum` Singleton — it's reflection-proof, serialization-safe, and can't be cloned, which is exactly why Joshua Bloch recommends it in *Effective Java*."

---

### Q11. What's the difference between the GoF "Factory Method" and "Simple Factory"?

**Model answer:** "What we built in the exercises — one class with a static `create(type)` method and a switch — is technically a **Simple Factory** (sometimes called a 'static factory'), not the textbook GoF Factory Method. The true **Factory Method** pattern makes the creation method *abstract* and pushes the 'which class' decision down to subclasses that override it. So a `Dialog` base class declares `abstract Button createButton()`, and `WindowsDialog` overrides it to return a `WindowsButton`. Both are commonly called 'Factory' in interviews; if pressed, I'd note Simple Factory centralizes creation in one method while Factory Method distributes it across subclasses via inheritance."

```java
// True GoF Factory Method: subclass decides the product.
abstract class Dialog {
    abstract Button createButton();          // the factory METHOD
    void render() { createButton().render(); }   // base logic uses it
}
class WindowsDialog extends Dialog {
    Button createButton() { return new WindowsButton(); }
}
```

---

### Q12. Aren't patterns just extra boilerplate? When should you NOT use a pattern?

**Model answer:** "Patterns are tools, not goals. The biggest junior mistake is *pattern-stuffing* — forcing Singleton, Factory, and Decorator into a 50-line script. Skip a pattern when: there's only one concrete type (no Factory needed), the object has 2 fields (no Builder needed), you control both interfaces (no Adapter needed), or the subsystem is already simple (no Facade needed). A pattern earns its keep only when it removes *real* duplication, coupling, or rigidity. In an interview I state the trade-off out loud: 'I could add a Factory here, but with a single notification type it'd be over-engineering, so I'll keep it direct and introduce the Factory the moment a second type appears.'"

---

## 🧠 Memory Hooks

Use these to recall the right pattern *fast* under interview pressure. The "trigger" column is gold — interviewers phrase problems with these words.

| Pattern | Category | Vivid Analogy | Trigger — use this pattern when the problem says... |
|---|---|---|---|
| **Singleton** | Creational | RBI / Aadhaar — only ONE, ever | "...exactly one shared...", "...one connection pool / one logger / one config..." |
| **Factory** | Creational | Restaurant kitchen — order a dosa, don't cook it | "...create the right *type* based on input/config...", "...add new types easily..." |
| **Builder** | Creational | Domino's pizza customizer — step by step | "...many optional fields...", "...build step by step...", "...too many constructor params..." |
| **Adapter** | Structural | Travel power plug / UPI between banks | "...make incompatible things work...", "...integrate a third-party SDK / legacy system..." |
| **Decorator** | Structural | Subway sandwich toppings — wrap & stack | "...add logging/caching/auth on top...", "...layer behavior...", "...avoid 2^N subclasses..." |
| **Facade** | Structural | MakeMyTrip "Book Trip" / Swiggy "Place Order" | "...one simple button hides many systems...", "...simplify a complex subsystem..." |

**The 3-second decision tree:**
- Problem is about **creating** objects? → Creational (Singleton / Factory / Builder)
- Problem is about **connecting/composing** objects? → Structural (Adapter / Decorator / Facade)
- "Translate" → Adapter. "Enhance/layer" → Decorator. "Simplify many into one" → Facade.

---

## 🎯 Final Interview Tip

**Don't name-drop patterns — *derive* them.** Weak candidates announce "I'll use a Factory here" with no justification. Strong candidates narrate the *pain first*: "If I write the creation logic inline, every caller couples to every concrete class and I duplicate this `if/else` everywhere — so I'll extract it into a Factory." You showed the interviewer you understand the *force* the pattern resolves, not just its name.

And always close the loop with **restraint**: mention the pattern you're *not* using and why ("a Builder would be over-engineering for two fields"). In LLD interviews, demonstrating you know when *not* to apply a pattern is the single clearest signal of senior-level judgment. Patterns are a vocabulary — fluency means using the right word at the right time, and staying silent when plain code says it better. 🚀

Good luck — you've got this! ☕
