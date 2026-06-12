# Week 7: Advanced Java — Generics, Design Patterns, Memory, and JVM — Interview Answers & Exercise Solutions

> **The Interview Golden Rule** 🏆
> Every strong answer follows the same four-beat rhythm:
> **Definition → Why it exists → Code example → Gotcha / trade-off.**
> Interviewers at Microsoft and Anthropic do not just want "what" — they want to hear that you
> understand *why* the feature exists and *when it bites you*. A candidate who says "type erasure
> means generic info is gone at runtime" is fine. A candidate who adds "...which is why you can't
> do `new T()` or `instanceof List<String>`, and it exists for backward compatibility with pre-Java-5
> code" gets the offer.
>
> This file has two parts:
> - **PART A** — Full worked Java solutions to all 5 practice exercises from the Plan.
> - **PART B** — 16 advanced Java interview questions with confident, spoken-style model answers.
>
> All code is plain Java and compiles on Java 17+ (a couple of features note their minimum version).

---

## PART A — Exercise / Practice Solutions

### Exercise 1: Generic `Pair<A, B>`, `Triple<A, B, C>`, and `Pair.swap`

**Task:** Create generic classes `Pair<A, B>` and `Triple<A, B, C>`. Add a static method
`Pair.swap(Pair<A,B>)` that returns `Pair<B,A>`. Test with different type combinations.

```java
import java.util.List;
import java.util.Objects;

/** An immutable pair of two values of possibly different types. */
final class Pair<A, B> {
    private final A first;
    private final B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst()  { return first; }
    public B getSecond() { return second; }

    /**
     * Generic STATIC method. Note the <A, B> after `static` — a static method cannot
     * see the class's A/B type parameters, so it must declare its own. These A/B are
     * independent placeholders, not the enclosing class's.
     */
    public static <A, B> Pair<B, A> swap(Pair<A, B> p) {
        return new Pair<>(p.getSecond(), p.getFirst());
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;
        Pair<?, ?> other = (Pair<?, ?>) o;
        return Objects.equals(first, other.first) && Objects.equals(second, other.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}

/** An immutable triple of three values of possibly different types. */
final class Triple<A, B, C> {
    private final A first;
    private final B second;
    private final C third;

    public Triple(A first, B second, C third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public A getFirst()  { return first; }
    public B getSecond() { return second; }
    public C getThird()  { return third; }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ", " + third + ")";
    }
}

public class Exercise1 {
    public static void main(String[] args) {
        // Different type combinations
        Pair<String, Integer> score = new Pair<>("Math", 95);
        Pair<Integer, String> swapped = Pair.swap(score);
        System.out.println(score);    // (Math, 95)
        System.out.println(swapped);  // (95, Math)

        // Swap is symmetric: swap(swap(x)) equals x
        System.out.println(Pair.swap(swapped).equals(score)); // true

        // Pair holding a List
        Pair<String, List<String>> dept = new Pair<>("Engineering", List.of("Aarav", "Rohan"));
        System.out.println(Pair.swap(dept)); // ([Aarav, Rohan], Engineering)

        // Triple with three distinct types
        Triple<String, Integer, Boolean> employee = new Triple<>("Priya", 28, true);
        System.out.println(employee); // (Priya, 28, true)
    }
}
```

**Talking point:** The static `swap` needs its *own* `<A, B>` declaration. A common mistake is
writing `public static Pair<B, A> swap(...)` reusing the class type params — that won't compile,
because static members exist independently of any instance and have no access to instance type
parameters.

---

### Exercise 2: Builder for a BookMyShow `MovieBooking` (Immutable)

**Task:** `MovieBooking` with `movieName, theater, city, date, seats (list), snacksIncluded (boolean),
totalPrice`. Use the Builder pattern. Make the built object immutable.

```java
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MovieBooking {              // final → cannot be subclassed and mutated
    private final String movieName;
    private final String theater;
    private final String city;
    private final LocalDate date;
    private final List<String> seats;          // mutable type → must be defended
    private final boolean snacksIncluded;
    private final double totalPrice;

    // Private constructor — only the Builder can build us.
    private MovieBooking(Builder b) {
        this.movieName      = b.movieName;
        this.theater        = b.theater;
        this.city           = b.city;
        this.date           = b.date;
        // DEFENSIVE COPY: don't store the builder's list directly — the caller still
        // holds a reference and could mutate it after build().
        this.seats          = new ArrayList<>(b.seats);
        this.snacksIncluded = b.snacksIncluded;
        this.totalPrice     = b.totalPrice;
    }

    public String getMovieName()     { return movieName; }
    public String getTheater()       { return theater; }
    public String getCity()          { return city; }
    public LocalDate getDate()       { return date; } // LocalDate is itself immutable — safe to return
    public boolean isSnacksIncluded(){ return snacksIncluded; }
    public double getTotalPrice()    { return totalPrice; }

    // Return an UNMODIFIABLE view so callers cannot mutate our internal list.
    public List<String> getSeats() {
        return Collections.unmodifiableList(seats);
    }

    public static class Builder {
        // Required fields go in the Builder constructor.
        private final String movieName;
        private final String theater;
        private final String city;
        private final LocalDate date;
        // Optional fields with sensible defaults.
        private List<String> seats = new ArrayList<>();
        private boolean snacksIncluded = false;
        private double totalPrice = 0.0;

        public Builder(String movieName, String theater, String city, LocalDate date) {
            this.movieName = movieName;
            this.theater   = theater;
            this.city      = city;
            this.date      = date;
        }

        public Builder seats(List<String> seats) {
            this.seats = new ArrayList<>(seats); // copy on the way in too
            return this;
        }

        public Builder snacksIncluded(boolean snacksIncluded) {
            this.snacksIncluded = snacksIncluded;
            return this;
        }

        public Builder totalPrice(double totalPrice) {
            this.totalPrice = totalPrice;
            return this;
        }

        public MovieBooking build() {
            // A great place to enforce invariants before constructing.
            if (seats.isEmpty()) {
                throw new IllegalStateException("Booking must have at least one seat");
            }
            if (totalPrice < 0) {
                throw new IllegalStateException("Total price cannot be negative");
            }
            return new MovieBooking(this);
        }

        @Override
        public String toString() { return "Builder for " + movieName; }
    }

    @Override
    public String toString() {
        return movieName + " @ " + theater + " (" + city + ") on " + date
                + " | seats=" + seats + " | snacks=" + snacksIncluded
                + " | Rs." + totalPrice;
    }

    public static void main(String[] args) {
        MovieBooking booking = new MovieBooking.Builder(
                    "Dune Part Two", "PVR Forum", "Bangalore", LocalDate.of(2026, 6, 20))
                .seats(List.of("H12", "H13"))
                .snacksIncluded(true)
                .totalPrice(950.00)
                .build();

        System.out.println(booking);

        // Prove immutability: mutating the returned list throws.
        try {
            booking.getSeats().add("H14");
        } catch (UnsupportedOperationException e) {
            System.out.println("Cannot modify seats — booking is immutable. ✅");
        }
    }
}
```

**Talking point:** Immutability here rests on three pillars — `final` class + `final` fields,
**defensive copy** of the mutable `List<String>` in the constructor, and an **unmodifiable view**
from the getter. Drop any one and the object is no longer truly immutable.

---

### Exercise 3: Payment Factory + Strategy (with Lambdas)

**Task:** Combine Factory and Strategy: a `PaymentFactory` that creates payment processors, each
with its own fee strategy (UPI = 0%, Credit Card = 2%, International Card = 3.5% + currency
conversion). Use lambdas for the strategies.

```java
import java.util.function.DoubleUnaryOperator;

interface PaymentProcessor {
    /** @return the total amount the customer pays, including fees. */
    double process(double amount);
    String name();
}

/**
 * A single concrete processor parameterized by a fee STRATEGY (a lambda).
 * This is Factory (who creates it) + Strategy (how the fee is computed) combined.
 */
class FeeBasedProcessor implements PaymentProcessor {
    private final String name;
    // The strategy: given a base amount, return the fee to add.
    private final DoubleUnaryOperator feeStrategy;

    FeeBasedProcessor(String name, DoubleUnaryOperator feeStrategy) {
        this.name = name;
        this.feeStrategy = feeStrategy;
    }

    @Override
    public double process(double amount) {
        double fee = feeStrategy.applyAsDouble(amount);
        double total = amount + fee;
        System.out.printf("[%s] base=Rs.%.2f fee=Rs.%.2f total=Rs.%.2f%n",
                name, amount, fee, total);
        return total;
    }

    @Override
    public String name() { return name; }
}

class PaymentFactory {
    // Fee strategies expressed as lambdas — no separate class per strategy needed.
    private static final DoubleUnaryOperator UPI_FEE   = amt -> 0.0;          // 0%
    private static final DoubleUnaryOperator CARD_FEE  = amt -> amt * 0.02;   // 2%
    // International: 3.5% fee + a flat currency conversion charge of Rs.50.
    private static final DoubleUnaryOperator INTL_FEE  = amt -> amt * 0.035 + 50.0;

    public static PaymentProcessor create(String type) {
        switch (type.toLowerCase()) {
            case "upi":           return new FeeBasedProcessor("UPI", UPI_FEE);
            case "credit_card":   return new FeeBasedProcessor("Credit Card", CARD_FEE);
            case "international":  return new FeeBasedProcessor("International Card", INTL_FEE);
            default:
                throw new IllegalArgumentException("Unknown payment type: " + type);
        }
    }
}

public class Exercise3 {
    public static void main(String[] args) {
        double amount = 1000.0;

        PaymentFactory.create("upi").process(amount);          // fee 0
        PaymentFactory.create("credit_card").process(amount);  // fee 20
        PaymentFactory.create("international").process(amount); // fee 85

        // Strategies are interchangeable at runtime — swap one in without touching the factory.
        PaymentProcessor promo = new FeeBasedProcessor("Festival UPI", amt -> -amt * 0.05); // 5% cashback
        promo.process(amount); // total Rs.950 — negative fee = discount
    }
}
```

**Talking point:** The **Factory** decides *which* object to hand back; the **Strategy** (a lambda)
decides *how* the fee is computed. Because the strategy is a `DoubleUnaryOperator`, I can inject a
brand-new pricing rule (even a cashback) at runtime without modifying any existing class — that's
the Open/Closed Principle in action.

---

### Exercise 4: Observer for Ola Ride Tracking

**Task:** Observer pattern. The `Ride` subject notifies observers (CustomerApp, DriverApp,
SupportDashboard) when ride status changes through REQUESTED → ACCEPTED → EN_ROUTE → ARRIVED →
IN_PROGRESS → COMPLETED.

```java
import java.util.ArrayList;
import java.util.List;

enum RideStatus {
    REQUESTED, ACCEPTED, EN_ROUTE, ARRIVED, IN_PROGRESS, COMPLETED
}

interface RideObserver {
    void onStatusChange(String rideId, RideStatus oldStatus, RideStatus newStatus);
}

class Ride {                                   // The Subject
    private final String rideId;
    private RideStatus status = RideStatus.REQUESTED;
    private final List<RideObserver> observers = new ArrayList<>();

    Ride(String rideId) { this.rideId = rideId; }

    public void subscribe(RideObserver o)   { observers.add(o); }
    public void unsubscribe(RideObserver o) { observers.remove(o); }

    public void setStatus(RideStatus newStatus) {
        RideStatus old = this.status;
        this.status = newStatus;
        // Iterate over a copy so an observer that unsubscribes mid-notify
        // doesn't cause a ConcurrentModificationException.
        for (RideObserver o : new ArrayList<>(observers)) {
            o.onStatusChange(rideId, old, newStatus);
        }
    }

    public RideStatus getStatus() { return status; }
}

class CustomerApp implements RideObserver {
    @Override
    public void onStatusChange(String rideId, RideStatus oldS, RideStatus newS) {
        System.out.println("[Customer] Ride " + rideId + " is now " + newS);
    }
}

class DriverApp implements RideObserver {
    @Override
    public void onStatusChange(String rideId, RideStatus oldS, RideStatus newS) {
        System.out.println("[Driver]   Ride " + rideId + ": " + oldS + " -> " + newS);
    }
}

class SupportDashboard implements RideObserver {
    @Override
    public void onStatusChange(String rideId, RideStatus oldS, RideStatus newS) {
        if (newS == RideStatus.COMPLETED) {
            System.out.println("[Support]  Ride " + rideId + " closed. Archiving.");
        } else {
            System.out.println("[Support]  Tracking " + rideId + " -> " + newS);
        }
    }
}

public class Exercise4 {
    public static void main(String[] args) {
        Ride ride = new Ride("OLA-7781");
        RideObserver customer = new CustomerApp();
        ride.subscribe(customer);
        ride.subscribe(new DriverApp());
        ride.subscribe(new SupportDashboard());

        // Walk the ride through its lifecycle.
        ride.setStatus(RideStatus.ACCEPTED);
        ride.setStatus(RideStatus.EN_ROUTE);
        ride.setStatus(RideStatus.ARRIVED);

        // Customer closes the app — stop notifying them.
        ride.unsubscribe(customer);

        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStatus(RideStatus.COMPLETED);
    }
}
```

**Talking point:** The two production-grade details interviewers look for: (1) iterate over a
**copy** of the observer list so an observer unsubscribing during notification doesn't throw
`ConcurrentModificationException`, and (2) the subject knows observers only through the
`RideObserver` interface — loose coupling means you can add a `FraudMonitor` observer later with
zero changes to `Ride`.

---

### Exercise 5: Memory Analysis — GC Behavior With and Without Object Pooling

**Task:** Create 1 million objects in a loop with `-verbose:gc`. Observe GC. Then reuse objects
(object pooling) and compare GC behavior.

```java
/**
 * Run with:   java -verbose:gc Exercise5
 * Or, for richer output on Java 17+:
 *   java -Xlog:gc Exercise5
 *
 * Compare the two phases:
 *  - Phase 1 allocates a fresh object every iteration → lots of short-lived garbage → many Minor GCs.
 *  - Phase 2 reuses one object (pooling) → almost no garbage → few/no GCs.
 */
public class Exercise5 {

    // A small object we will allocate a million times.
    static class Packet {
        int id;
        byte[] payload = new byte[64]; // 64 bytes so each allocation is non-trivial
        Packet(int id) { this.id = id; }
        void reset(int id) { this.id = id; } // reuse instead of reallocating
    }

    static long ALLOCATIONS = 1_000_000;

    public static void main(String[] args) {
        System.out.println("=== Phase 1: fresh allocation every iteration ===");
        long start = System.nanoTime();
        long checksum = 0;
        for (int i = 0; i < ALLOCATIONS; i++) {
            Packet p = new Packet(i);   // new object each loop → garbage
            checksum += p.id;           // touch it so JIT can't optimize it away
        }
        long t1 = System.nanoTime() - start;
        System.out.println("Phase 1 done in " + (t1 / 1_000_000) + " ms (checksum=" + checksum + ")");

        System.out.println("\n=== Phase 2: object pooling (reuse one instance) ===");
        start = System.nanoTime();
        checksum = 0;
        Packet pooled = new Packet(0); // ONE object, reused a million times
        for (int i = 0; i < ALLOCATIONS; i++) {
            pooled.reset(i);            // no allocation → no garbage
            checksum += pooled.id;
        }
        long t2 = System.nanoTime() - start;
        System.out.println("Phase 2 done in " + (t2 / 1_000_000) + " ms (checksum=" + checksum + ")");

        System.out.println("\nExpect Phase 1 to trigger many Minor GCs in the -verbose:gc log,");
        System.out.println("while Phase 2 triggers few or none because it produces almost no garbage.");
    }
}
```

**What you'll observe & how to explain it:**
- **Phase 1** prints many `[GC (Allocation Failure) ...]` lines — Eden fills up repeatedly with
  short-lived `Packet` objects, each fill triggering a fast **Minor GC**. Most objects die young
  (the *generational hypothesis*), so collection is cheap, but it happens often.
- **Phase 2** prints far fewer (often zero) GC lines — one object is reused, so almost nothing
  becomes garbage and Eden rarely fills.

**Talking points / caveats:**
- Object pooling is **not always a win**. The JVM's young-gen GC is so fast that pooling small
  objects often *hurts* (it adds complexity and keeps objects alive longer, promoting them to Old
  Gen where collection is expensive). Pool only **expensive** resources — threads, DB connections,
  large buffers.
- Use `-Xlog:gc` on modern JVMs (the older `-XX:+PrintGCDetails` is deprecated). To make GC pressure
  obvious, shrink the heap: `java -Xms32m -Xmx32m -Xlog:gc Exercise5`.

---

## PART B — Interview Questions & Model Answers

### Q1. What are generics and why were they added to Java?

> "Generics let me parameterize a class or method by type — write `Box<T>` once and use it for
> `String`, `Integer`, or `Employee` with full type safety. Before Java 5, collections stored
> `Object`, so anything could go in and everything coming out needed a cast — and a wrong cast blew
> up at *runtime* with a `ClassCastException`. Generics move that error to *compile time*, where it's
> cheap to fix. The trade-off is type erasure: the generic info is a compile-time fiction and is gone
> at runtime, which leads to some restrictions we can talk about."

**Gotcha:** Generics only work with reference types, not primitives — you write `List<Integer>`, not
`List<int>`. Autoboxing hides this but has a cost.

---

### Q2. Explain type erasure. What can't you do because of it?

> "Type erasure means the compiler uses generic type info for checking, then *erases* it — at runtime
> `List<String>` and `List<Integer>` are both just `List`. Java did this for backward compatibility:
> generics arrived in Java 5 and had to interoperate with all the pre-existing non-generic code."

Because the type is gone at runtime, you **cannot**:
1. Do `new T()` — `T` doesn't exist at runtime.
2. Do `obj instanceof List<String>` — only `instanceof List` is legal.
3. Create a generic array: `new T[10]` is illegal.
4. Have two overloads that differ only by generic parameter (`foo(List<String>)` and
   `foo(List<Integer>)` clash — same erased signature).

```java
List<String> a = new ArrayList<>();
List<Integer> b = new ArrayList<>();
System.out.println(a.getClass() == b.getClass()); // true — both are just ArrayList at runtime
```

**Gotcha:** To work around `new T()`, frameworks pass a `Class<T>` token and call
`clazz.getDeclaredConstructor().newInstance()`.

---

### Q3. Explain bounded type parameters and the difference from wildcards.

> "A bounded **type parameter** like `<T extends Comparable<T>>` constrains what `T` can be when I
> *declare* a generic class or method — it lets me call `compareTo` on `T`. A **wildcard** like
> `List<? extends Number>` constrains a *type argument* at the point of *use* — typically a method
> parameter — when I don't care about the exact type. Rule of thumb: use a named type parameter when
> you need to *refer to the type* again (e.g., return it); use a wildcard when you only need to
> read or write through the collection."

```java
// Type parameter — I refer to T in the return type:
static <T extends Comparable<T>> T max(List<T> list) { /* ... */ }

// Wildcard — I never name the element type, I just sum:
static double sum(List<? extends Number> list) { /* ... */ }
```

---

### Q4. Explain `? extends T` vs `? super T` and the PECS rule.

> "`? extends T` is an **upper-bounded** wildcard — 'T or any subtype'. You can **read** `T` out
> safely but can't **add** anything (except `null`), because the compiler doesn't know the exact
> subtype. `? super T` is **lower-bounded** — 'T or any supertype'. You can **add** `T` safely but
> reads only give you back `Object`. The mnemonic is **PECS: Producer Extends, Consumer Super**. If a
> structure *produces* values you consume (you read), use `extends`. If it *consumes* values you
> supply (you write), use `super`."

```java
List<? extends Number> producer = List.of(1, 2, 3);
Number n = producer.get(0);     // ✅ read OK
// producer.add(4);             // ❌ compile error — can't write

List<? super Integer> consumer = new ArrayList<Number>();
consumer.add(42);               // ✅ write OK
Object o = consumer.get(0);     // reads come back as Object only
```

**Analogy:** A Swiggy kitchen *produces* food (extends — you take). Your stomach *consumes* it
(super — you put in).

---

### Q5. What are annotations, and how do `@Retention` and `@Target` work?

> "Annotations are metadata attached to code — like a 'FRAGILE' sticker on a package. They don't
> change behavior by themselves; tools, the compiler, or frameworks read them and act. When I define
> a custom annotation I control two meta-annotations: `@Retention` says how long it survives —
> `SOURCE` (compiler only, like `@Override`), `CLASS` (in the .class file but not loadable), or
> `RUNTIME` (available via reflection — required for Spring/JUnit-style frameworks). `@Target` says
> where it can be applied — `METHOD`, `FIELD`, `TYPE`, etc."

```java
@Retention(RetentionPolicy.RUNTIME)  // visible to reflection at runtime
@Target(ElementType.METHOD)          // only usable on methods
public @interface LogExecution {
    String value() default "INFO";
}
```

**Gotcha:** If you forget `@Retention(RUNTIME)`, your framework's reflection code will silently never
see the annotation — a classic head-scratcher.

---

### Q6. What is reflection? When should you use it, and when not?

> "Reflection is the ability to inspect and manipulate a class at *runtime* — list its fields,
> methods, and constructors, instantiate objects dynamically, even read or set `private` members via
> `setAccessible(true)`. It's the engine behind frameworks: Spring uses it to wire `@Autowired`
> fields, JUnit to find `@Test` methods, Jackson to map JSON onto fields. I reach for it when writing
> *framework-level* code, not application logic."

**When NOT to use it:** It's slow (no JIT inlining), bypasses compile-time type checking, and breaks
encapsulation. In Java 9+ modules, `setAccessible` can also be blocked by the module system.

```java
Field f = Employee.class.getDeclaredField("salary");
f.setAccessible(true);            // bypass private
f.setDouble(emp, 99000);          // mutate a private field — powerful but dangerous
```

---

### Q7. Give four thread-safe Singleton implementations and say which you'd use.

| Approach | Lazy? | Thread-safe | Notes |
|----------|-------|-------------|-------|
| Eager (`static final` init) | ❌ | ✅ (class loading) | Simple; wastes memory if never used |
| Double-checked locking (`volatile`) | ✅ | ✅ | `volatile` is mandatory; verbose & error-prone |
| Bill Pugh (static holder class) | ✅ | ✅ | Lazy + no sync; best general choice |
| Enum singleton | ❌ (eager-ish) | ✅ | Bloch's pick; immune to reflection & serialization attacks |

> "For most cases I use the **Bill Pugh** holder idiom — the inner static `Holder` class isn't loaded
> until `getInstance()` is first called, and the JVM guarantees class initialization is thread-safe,
> so I get laziness *and* safety with zero synchronization. If I need bulletproof protection against
> reflection and serialization, I use the **enum singleton**, which Joshua Bloch recommends in
> *Effective Java*."

```java
class Config {
    private Config() {}
    private static class Holder { static final Config INSTANCE = new Config(); }
    public static Config getInstance() { return Holder.INSTANCE; }
}
```

**Gotcha for double-checked locking:** without `volatile` on the instance field, another thread can
see a *partially constructed* object due to instruction reordering.

---

### Q8. Why does double-checked locking need `volatile`?

> "`new DatabaseConnection()` isn't atomic — it's roughly (1) allocate memory, (2) run the
> constructor, (3) assign the reference. The JVM is allowed to reorder (2) and (3). Without
> `volatile`, Thread B could see the reference assigned in step 3 *before* the constructor finished
> in step 2 — it skips the null check and returns a half-built object. `volatile` forbids that
> reordering and guarantees visibility across threads, so other threads see the fully constructed
> instance or nothing at all."

---

### Q9. Factory vs Builder vs Strategy — when do you use each?

| Pattern | Solves | One-liner |
|---------|--------|-----------|
| **Factory** | "Which concrete class do I create?" | Hide `new`; pick a subtype by input |
| **Builder** | "Too many constructor params / optional fields" | Fluent, readable, immutable construction |
| **Strategy** | "Same task, swappable algorithm" | Inject behavior (often a lambda) |

> "Factory centralizes *object creation* so callers depend on an interface, not a concrete class.
> Builder solves the *telescoping constructor* problem — Java has no named arguments, so a
> seven-parameter constructor is unreadable; the Builder makes construction read like English and
> produces an immutable object. Strategy makes an *algorithm* a plug-in — since Java 8, a strategy is
> usually just a lambda implementing a functional interface."

---

### Q10. Explain the Java memory model: stack vs heap.

> "The **stack** holds local variables and method-call frames; each thread has its own, it's LIFO,
> very fast, and frames vanish when a method returns. The **heap** holds all objects and instance
> variables; it's shared across all threads and managed by the garbage collector. So when I write
> `Order o = new Order()`, the *reference* `o` lives on the stack and the *object* lives on the heap."

| | Stack | Heap |
|--|-------|------|
| Stores | Locals, call frames, references | Objects, instance fields |
| Shared? | Per-thread | All threads |
| Speed | Very fast | Slower |
| Cleanup | Auto on method return | Garbage Collector |
| Error | `StackOverflowError` | `OutOfMemoryError` |

**Analogy:** Stack = your desk (small, current task, cleared when done). Heap = the warehouse (huge,
a cleanup crew/GC removes unused stuff).

---

### Q11. Walk me through heap generations and how Minor vs Major GC differ.

> "The heap is split by object age. New objects land in **Eden** (Young Generation). When Eden fills,
> a fast **Minor GC** runs and survivors move to a **Survivor** space. Objects that survive several
> Minor GCs get promoted to the **Old/Tenured Generation**. When Old fills, a **Major (Full) GC**
> runs — that's slower and causes longer pauses. Class metadata lives in **Metaspace**, which is
> off-heap. The whole design leans on the *weak generational hypothesis*: most objects die young, so
> collecting the young gen frequently and cheaply handles the bulk of garbage."

**Gotcha:** Long pauses usually mean too many objects being promoted to Old Gen — often a sign of a
cache or collection holding references it shouldn't.

---

### Q12. Compare the common GC algorithms.

| Algorithm | Style | Pause behavior | Use case |
|-----------|-------|----------------|----------|
| Serial GC | Single-threaded, stop-the-world | Long | Small apps / dev |
| Parallel GC | Multi-threaded, stop-the-world | Throughput-oriented | Batch / throughput jobs |
| G1 GC | Region-based, mostly concurrent | Predictable, target-tunable | **Default since Java 9**, general purpose |
| ZGC | Concurrent, region-based | Sub-millisecond, ~10ms or less | Latency-critical (trading, real-time) |

> "I default to **G1** — it splits the heap into regions and lets me target a max pause with
> `-XX:MaxGCPauseMillis`. For ultra-low-latency systems where even a 50 ms pause is unacceptable, I'd
> reach for **ZGC**, which keeps pauses in the single-digit milliseconds regardless of heap size, at
> some throughput cost."

---

### Q13. Can Java have memory leaks even with GC? How?

> "Absolutely. GC only frees objects that are *unreachable*. If I keep a reference to something I no
> longer need, GC can't collect it — that's a leak. The classic culprits are (1) a static collection
> that only grows, (2) unclosed resources like streams or connections, and (3) listeners/callbacks
> that are never unregistered, so the publisher pins them forever."

```java
// Leak: a static cache that never evicts
static final Map<String, Object> CACHE = new HashMap<>();   // grows forever → OOM

// Fixes:
// - WeakHashMap (entries collectible when the key is otherwise unreferenced)
// - a bounded cache with eviction (e.g., Caffeine / LinkedHashMap LRU)
// - try-with-resources for streams; explicit removeListener() for callbacks
```

**Gotcha:** `ThreadLocal` in a thread pool is a notorious leak — pooled threads live forever, so the
ThreadLocal value never gets cleaned up unless you call `remove()`.

---

### Q14. Why is immutability valuable, and how do you write a correct immutable class?

> "Immutable objects can't change after construction, which buys you three things: **thread safety
> for free** (no synchronization needed to share them), **safe use as HashMap keys** (the hashCode
> can't drift), and **easier reasoning** (no spooky action at a distance). To build one correctly I:
> mark the class `final`, make all fields `private final`, provide no setters, **defensively copy**
> any mutable field in the constructor, and **return copies or unmodifiable views** from getters."

```java
public final class Money {
    private final String currency;
    private final List<String> tags;
    public Money(String currency, List<String> tags) {
        this.currency = currency;
        this.tags = new ArrayList<>(tags);               // copy in
    }
    public List<String> getTags() {
        return Collections.unmodifiableList(tags);        // no leak out
    }
}
```

**Modern shortcut:** Java 16+ **records** generate the constructor, getters, `equals`, `hashCode`,
and `toString`, with all fields final — `public record Money(String currency, double amount) {}`.
(Note: a record still needs a defensive copy in a compact constructor if it holds a mutable field.)

---

### Q15. Explain the String pool, `==` vs `equals`, and String vs StringBuilder vs StringBuffer.

> "String literals are interned in the **String pool**, so two identical literals share one object —
> `s1 == s2` is true. But `new String("Hello")` forces a fresh heap object outside the pool, so
> `s1 == s3` is false even though `equals` is true. Lesson: always compare String *content* with
> `equals`, never `==`. `intern()` can push a String into the pool to get reference equality back."

```java
String s1 = "Hello", s2 = "Hello", s3 = new String("Hello");
System.out.println(s1 == s2);        // true  — same pooled object
System.out.println(s1 == s3);        // false — new heap object
System.out.println(s1.equals(s3));   // true  — same content
System.out.println(s1 == s3.intern()); // true — interned back into pool
```

| | String | StringBuilder | StringBuffer |
|--|--------|---------------|--------------|
| Mutable | ❌ | ✅ | ✅ |
| Thread-safe | ✅ (immutable) | ❌ | ✅ (synchronized) |
| Speed | Slow for concatenation | Fast | Slower (sync overhead) |
| Use | Fixed values | Single-thread building | Multi-thread building |

**Gotcha:** Concatenating in a loop with `+` creates a new String and copies all prior characters
each iteration — O(n²). Use `StringBuilder` (O(n)).

---

### Q16. How does the JVM execute code? Cover bytecode, ClassLoaders, and JIT.

> "`javac` compiles `.java` to platform-independent **bytecode** (`.class`). The JVM loads it via
> **ClassLoaders** — Bootstrap (core `java.*`), Platform/Extension, then the Application classloader
> for my classes, using parent-delegation so core classes can't be spoofed. A **bytecode verifier**
> checks it's safe. The JVM then **interprets** bytecode initially, and the **JIT compiler** watches
> for 'hot' methods and compiles them to native machine code. That's why Java is slow to warm up but
> runs near-native once hot. And because bytecode is portable while each platform ships its own JVM,
> we get 'write once, run anywhere'."

**Analogy:** Bytecode is English (understood everywhere); each platform's JVM is a local translator
turning it into the native language (machine code).

**Gotcha:** Benchmarking Java without a *warm-up* phase measures the cold interpreter, not the
JIT-optimized code — always warm up (or use JMH) before timing.

---

## 🧠 Memory Hooks

| Concept | Vivid Analogy | Trigger Phrase |
|---------|---------------|----------------|
| Generics | Labeled Flipkart box — type is the label | "Label the box" |
| Type erasure | Label peeled off before shipping (runtime) | "Erased at the door" |
| `? extends` (Producer) | Swiggy kitchen — you only *take* food | "Producer Extends" |
| `? super` (Consumer) | Your stomach — you only *put* food in | "Consumer Super" |
| Bounded type param | Driver needs license *AND* Google Maps | "Requirements on the type" |
| Annotations | FRAGILE sticker on a package | "Sticker, not contents" |
| Reflection | Opening the car hood to poke the engine | "Hood open, gloves off" |
| Singleton | India has only ONE President | "One and only" |
| Bill Pugh holder | Vault opens only when you knock | "Lazy vault" |
| Factory | BookMyShow picks your payment object | "You choose, it builds" |
| Builder | Ordering a sandwich step by step | "...and hold the onions" |
| Observer | YouTube subscribe bell | "Ding! New upload" |
| Strategy (lambda) | Swappable discount coupon | "Plug-in the algorithm" |
| Stack vs Heap | Your desk vs the warehouse | "Desk vs warehouse" |
| Eden/Survivor/Old | Incoming dock → temp shelf → long-term store | "Packages age in the warehouse" |
| Minor vs Major GC | Quick sweep vs deep clean | "Most packages die young" |
| Memory leak (GC) | A box you forgot to throw away | "GC can't toss what you hold" |
| Immutability | A printed receipt — can't edit, only reprint | "Reprint, never erase" |
| String pool | Shared community whiteboard for literals | "Same words, same board" |
| StringBuilder loop | Add a line vs recopy the whole letter | "Don't recopy the letter" |
| Bytecode + JIT | English + local translator | "Write once, run anywhere" |
| ClassLoader delegation | Ask your parent before doing it yourself | "Parent first" |

---

## 🎯 Final Interview Tip

When you hit an advanced question, **don't dump everything you know — answer in the golden-rule
rhythm and then offer the trade-off.** Say the definition in one sentence, give the *why* it exists,
show or describe a tiny code example, then volunteer the gotcha *before they ask*. Volunteering the
gotcha ("...but the catch with double-checked locking is you must mark the field `volatile`, or
threads can see a half-constructed object") is the single biggest signal of senior-level depth. It
tells the interviewer you've actually *been burned* by this in production — and that's exactly the
engineer they want to hire. Pause, let them probe, and treat the interview as a conversation, not a
recital. 💪
