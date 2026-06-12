# Week 8: Java Interview Questions — Microsoft, Anthropic, Amazon, Google — Interview Answers & Solutions

> Your complete, battle-tested answer sheet. Every question and coding problem from the Plan, fully worked in **Java**, with the *why* behind every tricky output — plus a broad revision set of the highest-frequency Java interview questions.

---

## 🎯 How to Approach Java Interview Questions (Read This First)

Java interviews come in three flavors, and each needs a different mental gear:

| Flavor | What it looks like | Your move |
|--------|--------------------|-----------|
| **Concept question** | "What's the difference between X and Y?" | Lead with a one-sentence answer, then a table, then code. Interviewers love structure. |
| **Coding problem** | "Reverse a string without `reverse()`" | Clarify constraints → state approach + complexity → code → walk a test case. |
| **Tricky output** | "What does this print?" | **Slow down.** Don't guess. Reason about references, the string pool, autoboxing, and the Integer cache out loud. |

### The Golden Rules for Output-Prediction Questions 🧩

These are *designed* to trip you up. Before you blurt an answer, run this checklist in your head:

1. **Is it a primitive or an object?** Primitives compare by value with `==`. Objects compare by *reference* with `==`.
2. **String literal or `new String()`?** Literals live in the **String Pool** (shared); `new String()` always makes a fresh heap object.
3. **Is autoboxing happening?** `Integer x = 127` boxes an `int`. Watch for the **Integer cache (-128..127)**.
4. **`==` or `.equals()`?** `==` = "same object in memory?" `.equals()` = "same content?"
5. **Pass-by-value, always.** Java *never* passes references themselves — it passes *copies* of references. Reassigning a parameter never affects the caller.

> 🗣️ **Think out loud.** Interviewers grade your *reasoning*, not just the final answer. Saying "this is a `new String`, so `==` is false because they're distinct heap objects" earns more points than a silent correct guess.

---

# PART A — Worked Solutions

Every question and coding problem from the Plan, solved and explained.

---

## Category 1: Core Java

### Q1: Difference between `==` and `equals()`

**Answer:** `==` compares **references** for objects (and **values** for primitives). `equals()` compares **content/value**. For strings and wrapper objects, always use `equals()`.

```java
public class EqualsDemo {
    public static void main(String[] args) {
        // == compares REFERENCES for objects
        String a = new String("Hello");
        String b = new String("Hello");
        System.out.println(a == b);       // false — two distinct heap objects
        System.out.println(a.equals(b));  // true  — same content

        // String literals share the String Pool
        String c = "Hello";
        String d = "Hello";
        System.out.println(c == d);       // true  — same pooled object
        System.out.println(c.equals(d));  // true

        // Primitives: == compares values
        int x = 5, y = 5;
        System.out.println(x == y);       // true

        // Integer cache trap (-128..127)
        Integer i1 = 127, i2 = 127;
        System.out.println(i1 == i2);     // true  — cached, same object
        Integer i3 = 128, i4 = 128;
        System.out.println(i3 == i4);     // false — outside cache, new objects
        System.out.println(i3.equals(i4));// true  — same value
    }
}
```

**Why the tricky lines behave this way:**
- `new String("Hello")` **forces** a brand-new object on the heap, so `a == b` is `false` even though the text matches.
- `"Hello"` literals are **interned** into the String Pool, so `c` and `d` point to the *exact same* object → `c == d` is `true`.
- `Integer.valueOf` (used by autoboxing) **caches** boxed values from -128 to 127. So `127` reuses the cached object (`==` true), but `128` creates fresh objects (`==` false).

**Follow-ups:**
- *Override `equals` but not `hashCode`?* → Your objects break as `HashMap`/`HashSet` keys (equal objects may land in different buckets).
- *The contract?* → If `a.equals(b)` then `a.hashCode() == b.hashCode()`. The reverse need not hold (collisions allowed).

---

### Q2: `String` vs `StringBuilder` vs `StringBuffer`

**Answer:** `String` is **immutable** (every change makes a new object). `StringBuilder` is **mutable, fast, not thread-safe**. `StringBuffer` is **mutable, thread-safe (synchronized), slower**.

```java
// String — immutable; "+=" creates a new object each time
String s = "Hello";
s = s + " World";   // old "Hello" becomes garbage; s now references a new object

// StringBuilder — mutable, single-threaded, fast (default choice)
StringBuilder sb = new StringBuilder("Hello");
sb.append(" World"); // same object, modified in place

// StringBuffer — mutable, thread-safe via synchronized methods (rarely needed)
StringBuffer sbuf = new StringBuffer("Hello");
sbuf.append(" World");
```

| | String | StringBuilder | StringBuffer |
|--|--------|---------------|--------------|
| Mutable? | ❌ No | ✅ Yes | ✅ Yes |
| Thread-safe? | ✅ (immutable) | ❌ No | ✅ (synchronized) |
| Speed | Slow for many edits | ⚡ Fastest | Slower (locking) |
| Use when | Value never changes | Building strings (99%) | Shared across threads |

**Why the loop matters:** `result += "a"` 100,000 times is **O(n²)** — each `+=` copies the whole string into a new object. `StringBuilder.append` is **O(n)** total because it modifies one growing buffer.

**Follow-ups:** Why immutable? → thread safety, pooling, security, cached hashCode. `intern()` → forces a string into the pool so `==` works against pooled literals.

---

### Q3: Why is `String` immutable?

**Answer:** Five reasons:

1. **Security** 🔒 — Strings carry passwords, URLs, file paths, class names. Immutability prevents a value from being changed *after* a security check.
2. **String Pool** ♻️ — Immutability lets Java safely share one `"Hello"` across the whole program. If it were mutable, changing one would corrupt all.
3. **Thread safety** 🧵 — Immutable objects need no synchronization; they're safe to share freely.
4. **HashMap keys** 🔑 — If a key's content (and thus hashCode) could change, you'd never find it again.
5. **Cached hashCode** ⚡ — Computed once, reused forever → fast `HashMap` lookups.

---

### Q4: Abstract class vs Interface

**Answer:** An **abstract class** = a partial implementation with shared state/code (an "**is-a**" relationship). An **interface** = a capability contract (a "**can-do**" relationship). A class extends ONE abstract class but implements MANY interfaces.

| Feature | Abstract Class | Interface |
|---------|---------------|-----------|
| Methods | Abstract + concrete | Abstract + `default`/`static` (Java 8+) |
| Variables | Instance variables allowed | Only `public static final` constants |
| Constructor | ✅ Yes | ❌ No |
| Inheritance | Single | Multiple |
| Use when | Share code/state among related classes | Define a capability/contract |

```java
abstract class Vehicle {
    protected String brand;
    Vehicle(String brand) { this.brand = brand; }   // constructors allowed
    void accelerate() { /* shared concrete code */ } // concrete
    abstract void start();                            // each subclass differs
}

interface GPS {
    void navigate(String destination);                // abstract
    default void showMap() { System.out.println("Showing map..."); } // default
}
interface Bluetooth { void connect(String device); }

// A SmartCar IS-A Vehicle and CAN-DO GPS + Bluetooth
class SmartCar extends Vehicle implements GPS, Bluetooth {
    SmartCar(String brand) { super(brand); }
    @Override void start() { System.out.println("Press button"); }
    @Override public void navigate(String d) { System.out.println("Navigating to " + d); }
    @Override public void connect(String d) { System.out.println("Connected to " + d); }
}
```

**When abstract over interface?** When you need shared **state** (instance fields) or **constructors**, or the classes are tightly related (all are `Vehicle`s).

---

### Q5: Overloading vs Overriding

**Answer:** **Overloading** = same name, *different parameters*, same class, resolved at **compile time** (static polymorphism). **Overriding** = same name *and* parameters, in a *subclass*, resolved at **runtime** (dynamic polymorphism).

```java
class Calculator {                              // OVERLOADING
    int add(int a, int b) { return a + b; }
    double add(double a, double b) { return a + b; }
    int add(int a, int b, int c) { return a + b + c; }
}

class Animal { void speak() { System.out.println("..."); } }
class Dog extends Animal {                      // OVERRIDING
    @Override void speak() { System.out.println("Woof!"); }
}

Animal a = new Dog();
a.speak(); // "Woof!" — runtime picks Dog's version (dynamic dispatch)
```

**The key insight:** the *compiler* sees `a` as type `Animal` (matters for overload selection), but at *runtime* the JVM sees the real `Dog` object (matters for override selection).

---

### Q6: Autoboxing and Unboxing

**Answer:** **Autoboxing** = automatic `int` → `Integer`. **Unboxing** = `Integer` → `int`. Convenient, but has a cost and a `null` trap.

```java
Integer num = 42;                  // autobox: int -> Integer
List<Integer> list = new ArrayList<>();
list.add(5);                       // autobox: int 5 -> Integer

int value = num;                   // unbox: Integer -> int
int sum = num + 10;                // unbox then add

Integer nullNum = null;
// int crash = nullNum;            // 💥 NullPointerException — can't unbox null!

// Performance trap: wrapper in a hot loop boxes every iteration
Long totalBad = 0L;
for (int i = 0; i < 1_000_000; i++) totalBad += i; // creates a new Long each time — slow
long totalGood = 0L;               // ✅ primitive — zero boxing overhead
for (int i = 0; i < 1_000_000; i++) totalGood += i;
```

**Why the NPE?** Unboxing `null` calls `null.intValue()` → `NullPointerException`. **Why the slow loop?** `Long` is immutable; `totalBad += i` unboxes, adds, and **boxes a brand-new `Long`** every iteration.

---

### Q7: Checked vs Unchecked Exceptions

**Answer:** **Checked** exceptions MUST be handled (try-catch or `throws`) — the compiler enforces it (e.g., `IOException`). **Unchecked** exceptions (`RuntimeException` and subclasses) need not be handled — they signal programming bugs.

```java
// CHECKED — compiler forces handling
try {
    FileReader r = new FileReader("file.txt"); // FileNotFoundException (checked)
} catch (FileNotFoundException e) {
    System.out.println("File not found!");
}

// UNCHECKED — optional to catch
int[] arr = {1, 2, 3};
// int v = arr[10];   // ArrayIndexOutOfBoundsException (unchecked)
String s = null;
// s.length();        // NullPointerException (unchecked)
```

```
Throwable
├── Error           (OutOfMemoryError, StackOverflowError — don't catch)
└── Exception
    ├── Checked      (IOException, SQLException — MUST handle)
    └── RuntimeException (unchecked: NPE, AIOOBE, IllegalArgumentException, ClassCastException)
```

---

### Q8: `final`, `finally`, `finalize`

**Answer:** Three unrelated things that sound alike:

- **`final`** — prevents change: a `final` variable can't be reassigned, a `final` method can't be overridden, a `final` class can't be extended.
- **`finally`** — a block that **always** runs after `try`/`catch` (used for cleanup).
- **`finalize()`** — a *deprecated* method the GC *used to* call before destroying an object. Unreliable; removed in Java 18. Use try-with-resources or `Cleaner`.

```java
final int MAX = 100;        // MAX = 200; would not compile

try {
    int r = 10 / 0;
} catch (ArithmeticException e) {
    System.out.println("Error!");
} finally {
    System.out.println("Always runs — error or not"); // cleanup goes here
}
```

---

### Q9: `ArrayList` vs `LinkedList`

**Answer:** `ArrayList` = dynamic array → **O(1) random access**, **O(n)** middle insert/delete. `LinkedList` = doubly-linked nodes → **O(n) access**, **O(1)** insert/delete *if you hold the node*.

| Operation | ArrayList | LinkedList |
|-----------|-----------|------------|
| `get(index)` | O(1) | O(n) |
| `add(end)` | O(1) amortized | O(1) |
| `add(middle)` | O(n) | O(1) at node, O(n) to find |
| `remove(middle)` | O(n) | O(1) at node, O(n) to find |
| Memory / cache | Less, contiguous (cache-friendly) | More (pointers), scattered |

🎬 **Analogy:** ArrayList = numbered theater seats (jump to #47 instantly, but inserting shifts everyone). LinkedList = train coaches (walk to coach #47, but attach/detach easily).

**Interview verdict:** **ArrayList wins almost always** — modern CPUs love contiguous memory. Use LinkedList only as a `Deque` (frequent add/remove at both ends).

---

### Q10: How `HashMap` works internally

**Answer:** An array of **buckets**. `put(k,v)` computes the key's `hashCode`, derives a bucket index, and stores the entry. Collisions chain in a **linked list**, which **treeifies** to a Red-Black Tree at **8+ entries**. `get(k)` retraces the path.

**`put` steps:**
1. Compute `hashCode(key)` (then a spread/perturbation to mix high bits).
2. Index = `hash & (capacity - 1)` (fast modulo — works because capacity is a power of 2).
3. Empty bucket → place node. Non-empty → walk chain; if key `equals` → replace value, else append.
4. If `size / capacity > 0.75` (load factor) → **resize to double** and rehash.

**`get` steps:** same hash → same index → scan chain, comparing `hash` first (cheap int compare), then `equals` (only on hash match).

**Critical points:**
1. **`equals`/`hashCode` contract** — equal objects MUST share a hashCode; equal hashCodes needn't be equal objects.
2. **Power-of-2 capacity** makes `hash & (cap-1)` a fast modulo.
3. **Treeification (Java 8+)** turns a long chain (8+) into O(log n) lookup.
4. **Defaults:** capacity 16, load factor 0.75.

---

## Category 2: Multithreading

### Q11: `start()` vs `run()`

**Answer:** `start()` spawns a **new thread** and runs `run()` *inside it*. Calling `run()` directly is just a **plain method call** in the current thread — no new thread.

```java
class MyThread extends Thread {
    public void run() {
        System.out.println("Running in: " + Thread.currentThread().getName());
    }
}
MyThread t = new MyThread();
t.run();   // "Running in: main"     — no new thread
t.start(); // "Running in: Thread-0" — new thread
```

**Follow-up:** Calling `start()` twice → `IllegalThreadStateException`. A thread runs once; create a new one to rerun.

---

### Q12: Deadlock and prevention

**Answer:** A **deadlock** is a circular wait: thread 1 holds lock A and wants B; thread 2 holds B and wants A. Nobody proceeds. **Prevent it by always acquiring locks in the same global order.**

```java
final Object lockA = new Object(), lockB = new Object();

// DEADLOCK-PRONE: opposite lock orders
// T1: synchronized(lockA){ synchronized(lockB){...} }
// T2: synchronized(lockB){ synchronized(lockA){...} }

// FIX: both threads lock A then B (consistent ordering)
Runnable safe = () -> {
    synchronized (lockA) {
        synchronized (lockB) { /* work */ }
    }
};
new Thread(safe).start();
new Thread(safe).start();
```

**Four Coffman conditions (ALL must hold):** Mutual Exclusion, Hold-and-Wait, No Preemption, Circular Wait. **Break any one** → no deadlock. (Consistent lock ordering breaks Circular Wait; `tryLock` with timeout breaks Hold-and-Wait.)

---

### Q13: `synchronized` vs `Lock`

**Answer:** `synchronized` is simpler (auto lock/unlock at block boundaries) but rigid. `ReentrantLock` adds `tryLock()`, **timed** waits, **interruptibility**, and **fairness** — at the cost of a manual `unlock()` in `finally`.

```java
ReentrantLock lock = new ReentrantLock();
try {
    if (lock.tryLock(5, TimeUnit.SECONDS)) { // wait up to 5s — avoids deadlock
        try {
            // critical section
        } finally {
            lock.unlock();                   // MUST unlock in finally
        }
    } else {
        // couldn't acquire — back off / do something else
    }
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}
```

| | `synchronized` | `ReentrantLock` |
|--|----------------|-----------------|
| Unlock | Automatic | Manual (`finally`) |
| `tryLock` / timeout | ❌ | ✅ |
| Interruptible wait | ❌ | ✅ |
| Fairness policy | ❌ | ✅ (optional) |

---

### Q14: Producer-Consumer problem

**Answer:** Producers add items to a shared buffer; consumers remove them. They must coordinate so producers block when the buffer is **full** and consumers block when it's **empty**.

**Talking points the interviewer wants:**
1. **`wait`/`notify` solution** proves you understand thread communication.
2. **`BlockingQueue`** is the production-grade answer (handles blocking for you).
3. Use **`while`** (not `if`) around `wait()` — guards against **spurious wakeups**.
4. **`notifyAll()`** is safer than `notify()` — avoids waking the wrong (single) thread and stalling.

```java
class Buffer {
    private final Queue<Integer> q = new LinkedList<>();
    private final int capacity;
    Buffer(int capacity) { this.capacity = capacity; }

    public synchronized void produce(int v) throws InterruptedException {
        while (q.size() == capacity) wait();   // while, not if — spurious wakeups
        q.add(v);
        notifyAll();                            // notifyAll, not notify
    }
    public synchronized int consume() throws InterruptedException {
        while (q.isEmpty()) wait();
        int v = q.poll();
        notifyAll();
        return v;
    }
}
```
*(See Problem 6 below for the clean `BlockingQueue` version — the one to write in a real interview.)*

---

### Q15: The `volatile` keyword

**Answer:** `volatile` guarantees **visibility** — every read sees the latest write from main memory (no stale thread-local cache). It does **NOT** guarantee **atomicity**.

```java
private volatile boolean running = true;

public void stop() { running = false; }   // writer thread
public void work() {
    while (running) { /* ... */ }          // reader sees the update promptly
}
```

**Key point:** `count++` is read-modify-write (3 steps), so `volatile` is **not** enough — two threads can interleave and lose updates. Use **`AtomicInteger`** or `synchronized` for compound operations.

---

## Category 3: Java 8

### Q16: Second highest number with streams

```java
List<Integer> numbers = Arrays.asList(5, 3, 9, 1, 9, 7, 3, 8);
Optional<Integer> secondHighest = numbers.stream()
    .distinct()                          // [5, 3, 9, 1, 7, 8]
    .sorted(Comparator.reverseOrder())   // [9, 8, 7, 5, 3, 1]
    .skip(1)                             // drop the max
    .findFirst();
System.out.println(secondHighest.orElse(-1)); // 8
```
**Why `distinct()`?** Without it, two `9`s would make the "second highest" also `9`.

---

### Q17: Group employees by department

```java
// Lists per department
Map<String, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment));

// Count per department
Map<String, Long> countByDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));

// Names per department (downstream mapping)
Map<String, List<String>> namesByDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.mapping(Employee::getName, Collectors.toList())));
```
**Why it works:** `groupingBy` takes a **classifier** (the key) plus an optional **downstream collector** (`counting()`, `mapping(...)`) that reduces each group.

---

### Q18: Find duplicate elements

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 2, 4, 3, 5);

// Method 1: stateful Set (concise, NOT parallel-safe)
Set<Integer> seen = new HashSet<>();
List<Integer> dups = numbers.stream()
    .filter(n -> !seen.add(n))           // add() returns false if already present
    .collect(Collectors.toList());        // [2, 3]

// Method 2: pure functional (parallel-safe)
Set<Integer> dups2 = numbers.stream()
    .collect(Collectors.groupingBy(n -> n, Collectors.counting()))
    .entrySet().stream()
    .filter(e -> e.getValue() > 1)
    .map(Map.Entry::getKey)
    .collect(Collectors.toSet());
```
**Interview tip:** Method 1 mutates a shared `seen` set — *impure* and unsafe for `parallelStream()`. Mention Method 2 as the correct functional approach.

---

### Q19: `map()` vs `flatMap()`

**Answer:** `map()` transforms **one-to-one**. `flatMap()` transforms each element into a **stream**, then **flattens** all those streams into one.

```java
// map: one -> one
List<Integer> lengths = Stream.of("Hello", "World")
    .map(String::length).collect(Collectors.toList());      // [5, 5]

// flatMap: one -> many, flattened
List<String> allWords = Stream.of("Hello World", "Java Streams")
    .flatMap(s -> Arrays.stream(s.split(" ")))
    .collect(Collectors.toList());                          // [Hello, World, Java, Streams]
```
```
map:     [A, B, C] -> [f(A), f(B), f(C)]
flatMap: [A, B, C] -> [a1,a2, b1, c1,c2,c3]   (then flattened into one stream)
```
Without `flatMap` you'd get an awkward `List<Stream<String>>`.

---

### Q20: `Optional` — proper use

**Answer:** A container that may hold a value or be empty — return it instead of `null`.

```java
Optional<Employee> findById(String id) { /* Optional.empty() if missing */ return Optional.empty(); }

// Safe chaining
findById("123")
    .map(Employee::getName)
    .ifPresent(name -> System.out.println("Found: " + name));

// With a default
String name = findById("123").map(Employee::getName).orElse("Unknown Employee");
```
**Rules:** ✅ use for **return types**. ❌ not for parameters or fields. ❌ never `.get()` without checking — prefer `orElse` / `orElseThrow` / `ifPresent`.

---

### Q21: Sort strings by length (lambda)

```java
List<String> words = new ArrayList<>(Arrays.asList("Bangalore","Goa","Mumbai","Delhi","Kochi"));

words.sort((a, b) -> Integer.compare(a.length(), b.length())); // lambda
words.sort(Comparator.comparingInt(String::length));           // method ref (cleaner)
words.sort(Comparator.comparingInt(String::length).reversed());// longest first
// [Goa, Delhi, Kochi, Mumbai, Bangalore]
```
**Note:** `Arrays.asList(...)` is fixed-size; wrap in `new ArrayList<>(...)` to allow `sort` to work safely on a real list. (Sorting is fine on `asList`, but mutation like `add`/`remove` is not.)

---

### Q22: What is a functional interface?

**Answer:** An interface with **exactly one abstract method** (SAM) — the target type for a lambda. `@FunctionalInterface` makes the compiler enforce it.

```java
@FunctionalInterface
interface Validator<T> { boolean validate(T item); }

Validator<String> emailValidator = e -> e.contains("@") && e.contains(".");
```
Built-ins: `Predicate<T>` (T→boolean), `Function<T,R>` (T→R), `Consumer<T>` (T→void), `Supplier<T>` (()→T), `Comparator<T>`, `Runnable`, `Callable<V>`.

---

### Q23: `Predicate`, `Function`, `Consumer`, `Supplier`

```java
import java.util.function.*;

Predicate<Integer> isAdult = age -> age >= 18;     // T -> boolean
System.out.println(isAdult.test(25));               // true

Function<String, Integer> wordCount = s -> s.split(" ").length; // T -> R
System.out.println(wordCount.apply("Hello World from Java"));   // 4

Consumer<String> sendSMS = p -> System.out.println("SMS sent to " + p); // T -> void
sendSMS.accept("9876543210");

Supplier<LocalDate> today = LocalDate::now;          // () -> T
System.out.println(today.get());
```

| Interface | Signature | Method | Mnemonic |
|-----------|-----------|--------|----------|
| `Predicate<T>` | T → boolean | `test` | "Is it true?" |
| `Function<T,R>` | T → R | `apply` | "Transform it" |
| `Consumer<T>` | T → void | `accept` | "Use it, return nothing" |
| `Supplier<T>` | () → T | `get` | "Give me one" |

---

### Q24: `stream()` vs `parallelStream()`

```java
list.stream().map(this::expensiveOp).collect(Collectors.toList());        // sequential
list.parallelStream().map(this::expensiveOp).collect(Collectors.toList());// ForkJoinPool
```
**Use parallel when:** large dataset (10k+), CPU-bound, independent ops, order doesn't matter.
**Avoid when:** small data (overhead > benefit), I/O-bound (threads block), order matters, or shared mutable state.

---

### Q25: `List<String>` → `Map<String, Integer>` (word → length)

```java
List<String> words = Arrays.asList("Mumbai","Delhi","Goa","Chennai");
Map<String, Integer> wordToLength = words.stream()
    .collect(Collectors.toMap(w -> w, String::length));
// {Mumbai=6, Delhi=5, Goa=3, Chennai=7}

// Duplicate keys would throw IllegalStateException — supply a merge function:
List<String> dupes = Arrays.asList("hi","hello","hi");
Map<String, Integer> safe = dupes.stream()
    .collect(Collectors.toMap(w -> w, String::length, (existing, replacement) -> existing));
```
**Gotcha:** `toMap` *without* a merge function **throws** on duplicate keys. Always pass the 3rd arg if collisions are possible.

---

## Category 4: Collections Deep Dive

### Q26: `HashMap` vs `TreeMap` vs `LinkedHashMap`

| Feature | HashMap | TreeMap | LinkedHashMap |
|---------|---------|---------|---------------|
| Order | None | Sorted by key | Insertion order |
| Get/Put | O(1) | O(log n) | O(1) |
| Null key | 1 allowed | ❌ none | 1 allowed |
| Backing | Hash table | Red-Black Tree | Hash table + linked list |
| Use when | Default/fastest | Need sorted keys | Need predictable order |

```java
Map<String,Integer> hash   = new HashMap<>();       // unpredictable order
Map<String,Integer> tree   = new TreeMap<>();       // Apple, Banana, Cherry (sorted)
Map<String,Integer> linked = new LinkedHashMap<>(); // preserves insertion order
```

---

### Q27: How `ConcurrentHashMap` works

**Answer:** Thread-safe with **fine-grained locking** — it locks only the bucket being written (Java 8+ uses **CAS** + node-level synchronization), not the whole map. Reads are mostly lock-free.

```java
Map<String,Integer> bad  = new HashMap<>();                          // ❌ not thread-safe
Map<String,Integer> slow = Collections.synchronizedMap(new HashMap<>()); // locks everything
ConcurrentHashMap<String,Integer> good = new ConcurrentHashMap<>();  // ✅ fine-grained

good.put("k", 1);
good.computeIfAbsent("k", key -> 1);  // atomic
good.merge("k", 1, Integer::sum);     // atomic increment
```
**Note:** `ConcurrentHashMap` does **not** allow `null` keys or values (ambiguity with "absent").

---

### Q28: `Comparable` vs `Comparator`

**Answer:** `Comparable` = the class's **natural ordering** (`compareTo`, implemented *inside* the class). `Comparator` = an **external/custom ordering** (`compare`, defined *outside*, and you can have many).

```java
class Employee implements Comparable<Employee> {
    String name; double salary;
    @Override public int compareTo(Employee o) {
        return Double.compare(this.salary, o.salary);   // natural order: salary
    }
}
Collections.sort(employees); // uses compareTo

Comparator<Employee> byName        = Comparator.comparing(e -> e.name);
Comparator<Employee> bySalaryDesc  = Comparator.comparingDouble((Employee e) -> e.salary).reversed();
Comparator<Employee> byDeptThenSal = Comparator.comparing((Employee e) -> e.department)
                                               .thenComparingDouble(e -> e.salary);
```
**Use `Comparable`** for the one obvious order; **`Comparator`** for everything else.

---

### Q29: Duplicate key in `HashMap`

**Answer:** The **value is replaced**; the key is not duplicated. `put` returns the **old value** (or `null` if new).

```java
Map<String,Integer> map = new HashMap<>();
map.put("Aarav", 85000);
map.put("Aarav", 92000);          // replaces 85000
System.out.println(map.get("Aarav")); // 92000
System.out.println(map.size());        // 1
Integer old = map.put("Aarav", 100000); // returns 92000
```
**Mechanism:** find bucket → walk chain → if `hash == hash && key.equals(newKey)` → overwrite value; else append new node.

---

### Q30: Fail-fast vs Fail-safe iterators

**Answer:** **Fail-fast** iterators (`ArrayList`, `HashMap`) throw `ConcurrentModificationException` if the collection is structurally modified mid-iteration. **Fail-safe** iterators (`CopyOnWriteArrayList`, `ConcurrentHashMap`) iterate over a snapshot and tolerate modification.

```java
// FAIL-FAST — throws CME
List<String> list = new ArrayList<>(Arrays.asList("A","B","C"));
// for (String x : list) if (x.equals("B")) list.remove(x); // 💥 ConcurrentModificationException

// FIX 1: Iterator.remove()
Iterator<String> it = list.iterator();
while (it.hasNext()) if (it.next().equals("B")) it.remove();   // safe

// FIX 2: removeIf (Java 8+)
list.removeIf(x -> x.equals("B"));

// FAIL-SAFE — snapshot, no exception
CopyOnWriteArrayList<String> safe = new CopyOnWriteArrayList<>(Arrays.asList("A","B","C"));
for (String x : safe) if (x.equals("B")) safe.remove(x);       // no CME
```
**Why CME?** Fail-fast iterators track a `modCount`; an external structural change makes it mismatch → exception. It's a *bug detector*, not a guarantee.

---

## 10 Coding Problems

### Problem 1: Reverse a String (no built-in `reverse()`)

```java
public static String reverse(String input) {
    char[] chars = input.toCharArray();
    int left = 0, right = chars.length - 1;
    while (left < right) {
        char tmp = chars[left];
        chars[left++] = chars[right];
        chars[right--] = tmp;
    }
    return new String(chars);
}
// reverse("Hello") -> "olleH"
```
**Complexity:** O(n) time, O(n) space (char array). Two-pointer swap from both ends.

---

### Problem 2: Anagram check

```java
public static boolean isAnagram(String s1, String s2) {
    if (s1.length() != s2.length()) return false;
    int[] counts = new int[256];               // ASCII frequency
    for (char c : s1.toCharArray()) counts[c]++;
    for (char c : s2.toCharArray()) counts[c]--;
    for (int n : counts) if (n != 0) return false;
    return true;
}

// Java 8 stream version — note: must use Arrays.equals, NOT .equals on arrays
public static boolean isAnagramStream(String s1, String s2) {
    return s1.length() == s2.length()
        && Arrays.equals(s1.chars().sorted().toArray(),
                         s2.chars().sorted().toArray());
}
// isAnagram("listen","silent") -> true ;  isAnagram("hello","world") -> false
```
**⚠️ Fix vs the Plan:** the Plan's stream version used `array1.equals(array2)`, which compares **references** (always `false` here). The correct comparison is **`Arrays.equals(...)`**. Counting (first method) is **O(n)** and beats the O(n log n) sort.

---

### Problem 3: First non-repeating character

```java
public static char firstNonRepeating(String input) {
    Map<Character,Integer> counts = new LinkedHashMap<>(); // preserves order
    for (char c : input.toCharArray())
        counts.merge(c, 1, Integer::sum);
    for (Map.Entry<Character,Integer> e : counts.entrySet())
        if (e.getValue() == 1) return e.getKey();
    return '_';
}

public static Character firstNonRepeatingStream(String input) {
    return input.chars().mapToObj(c -> (char) c)
        .collect(Collectors.groupingBy(c -> c, LinkedHashMap::new, Collectors.counting()))
        .entrySet().stream()
        .filter(e -> e.getValue() == 1)
        .map(Map.Entry::getKey)
        .findFirst().orElse('_');
}
// firstNonRepeating("aabbcdeff") -> 'c'
```
**Why `LinkedHashMap`?** "First" means insertion order — a plain `HashMap` would lose it.

---

### Problem 4: FizzBuzz

```java
// Classic
for (int i = 1; i <= 100; i++) {
    if (i % 15 == 0) System.out.println("FizzBuzz");
    else if (i % 3 == 0) System.out.println("Fizz");
    else if (i % 5 == 0) System.out.println("Buzz");
    else System.out.println(i);
}

// Java 8 streams (shows modern fluency)
IntStream.rangeClosed(1, 100)
    .mapToObj(i -> i % 15 == 0 ? "FizzBuzz"
                 : i % 3  == 0 ? "Fizz"
                 : i % 5  == 0 ? "Buzz"
                 : String.valueOf(i))
    .forEach(System.out::println);
```
**Key trick:** check `% 15` **first** (both 3 and 5) before the individual checks.

---

### Problem 5: Custom HashMap

```java
class SimpleHashMap<K, V> {
    private static final int DEFAULT_CAPACITY = 16;
    private static class Node<K, V> {
        K key; V value; Node<K, V> next;
        Node(K key, V value) { this.key = key; this.value = value; }
    }
    private Node<K, V>[] buckets;
    private int size;

    @SuppressWarnings("unchecked")
    public SimpleHashMap() { buckets = new Node[DEFAULT_CAPACITY]; }

    private int getBucketIndex(K key) {
        return Math.abs(key.hashCode()) % buckets.length;
    }
    public void put(K key, V value) {
        int idx = getBucketIndex(key);
        for (Node<K, V> cur = buckets[idx]; cur != null; cur = cur.next) {
            if (cur.key.equals(key)) { cur.value = value; return; } // update
        }
        Node<K, V> node = new Node<>(key, value);                   // prepend
        node.next = buckets[idx];
        buckets[idx] = node;
        size++;
    }
    public V get(K key) {
        for (Node<K, V> cur = buckets[getBucketIndex(key)]; cur != null; cur = cur.next)
            if (cur.key.equals(key)) return cur.value;
        return null;
    }
    public int size() { return size; }
}
```
**Demonstrates:** buckets, hash→index, collision chaining, update-vs-insert. (Real `HashMap` adds resizing, treeification, and hash spreading.)

---

### Problem 6: Producer-Consumer with `BlockingQueue`

```java
import java.util.concurrent.*;

public class ProducerConsumer {
    public static void main(String[] args) {
        BlockingQueue<String> orderQueue = new ArrayBlockingQueue<>(5);

        Thread restaurant = new Thread(() -> {                 // producer
            String[] dishes = {"Biryani","Dosa","Pizza","Burger","Paneer"};
            for (String dish : dishes) {
                try {
                    orderQueue.put(dish);                      // blocks if full
                    System.out.println("Prepared: " + dish);
                    Thread.sleep(500);
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }, "Restaurant");

        Thread delivery = new Thread(() -> {                   // consumer
            for (int i = 0; i < 5; i++) {
                try {
                    String dish = orderQueue.take();           // blocks if empty
                    System.out.println("Delivering: " + dish);
                    Thread.sleep(1000);
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }, "Delivery");

        restaurant.start();
        delivery.start();
    }
}
```
**Why this is the "good" answer:** `BlockingQueue` handles all the `wait`/`notify` blocking internally — `put` blocks when full, `take` blocks when empty. No manual synchronization bugs.

---

### Problem 7: LRU Cache

```java
import java.util.*;

class LRUCache<K, V> {
    private final int capacity;
    private final Map<K, V> cache;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        // accessOrder=true => get() moves entry to the end (most-recently-used)
        this.cache = new LinkedHashMap<K, V>(capacity, 0.75f, true) {
            @Override protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > LRUCache.this.capacity;  // evict the eldest
            }
        };
    }
    public V get(K key)            { return cache.getOrDefault(key, null); }
    public void put(K key, V value){ cache.put(key, value); }
}

// LRUCache<Integer,String> c = new LRUCache<>(3);
// c.put(1,"One"); c.put(2,"Two"); c.put(3,"Three");
// c.get(1);          // 1 becomes most-recently-used
// c.put(4,"Four");   // evicts 2 (least-recently-used)
// c.get(2) -> null ;  c.get(1) -> "One"
```
**The magic:** `LinkedHashMap(cap, 0.75f, true)` enables **access order**, and overriding `removeEldestEntry` auto-evicts the LRU entry — a 3-line LRU.

---

### Problem 8: Flatten a nested list

```java
// [[1, 2, [3]], 4, [[5, 6], 7]] -> [1, 2, 3, 4, 5, 6, 7]
public static List<Integer> flatten(List<?> nested) {
    List<Integer> result = new ArrayList<>();
    for (Object item : nested) {
        if (item instanceof List) result.addAll(flatten((List<?>) item)); // recurse
        else                      result.add((Integer) item);
    }
    return result;
}
```
**Why recursion?** Nesting depth is unknown; recursion naturally handles arbitrary depth.

---

### Problem 9: Thread-safe Singleton (Bill Pugh)

```java
public class ConnectionPool {
    private ConnectionPool() { /* private — no external new */ }

    // Holder is loaded lazily, only when getInstance() is first called.
    private static class Holder {
        private static final ConnectionPool INSTANCE = new ConnectionPool();
    }
    public static ConnectionPool getInstance() { return Holder.INSTANCE; }
}
```
**Why Bill Pugh wins:** the JVM guarantees class initialization is **thread-safe** and **lazy** — `Holder` isn't loaded until `getInstance()` runs. No `synchronized`, no double-checked-locking pitfalls, no `volatile` needed.

---

### Problem 10: Employee analytics — single stream pipeline

```java
// 1) salary > 50000  2) group by dept  3) name of top earner per dept
Map<String, Optional<String>> topPerDept = employees.stream()
    .filter(e -> e.getSalary() > 50000)
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.collectingAndThen(
            Collectors.maxBy(Comparator.comparingDouble(Employee::getSalary)),
            optEmp -> optEmp.map(Employee::getName))));
// {Engineering=Optional[Ananya], Marketing=Optional[Priya]}
```
**Why `collectingAndThen`?** It applies a **finishing function** to the result of `maxBy` (an `Optional<Employee>`), turning it into the employee's *name*.

---

# PART B — Most-Asked Java Interview Q&A (Broad Revision Set)

A rapid-fire revision set across core Java, OOP, collections, strings, exceptions, multithreading, and Java 8.

---

### B1. Explain the four pillars of OOP.

| Pillar | One-liner | Example |
|--------|-----------|---------|
| **Encapsulation** | Hide data behind methods | `private` fields + getters/setters |
| **Inheritance** | Reuse via "is-a" | `Dog extends Animal` |
| **Polymorphism** | One interface, many forms | `Animal a = new Dog(); a.speak();` |
| **Abstraction** | Expose *what*, hide *how* | interfaces / abstract classes |

---

### B2. JDK vs JRE vs JVM?

- **JVM** — the engine that runs bytecode (platform-specific). "Write once, run anywhere."
- **JRE** — JVM + core libraries (to *run* Java apps).
- **JDK** — JRE + compiler (`javac`) + tools (to *develop* Java apps).

`JDK ⊃ JRE ⊃ JVM`. Source `.java` → `javac` → bytecode `.class` → JVM executes it.

---

### B3. Stack vs Heap memory?

| | Stack | Heap |
|--|-------|------|
| Stores | Local variables, method frames, references | Objects, instance fields |
| Scope | Per-thread | Shared across threads |
| Lifetime | Until method returns | Until garbage collected |
| Speed | Fast | Slower (GC-managed) |

A local `Person p = new Person()`: `p` (the reference) lives on the **stack**; the `Person` object lives on the **heap**.

---

### B4. Is Java pass-by-value or pass-by-reference?

**Always pass-by-value.** For objects, the *value passed is a copy of the reference*. So you can mutate the object's fields (both refer to the same object), but **reassigning** the parameter does nothing to the caller.

```java
void mutate(StringBuilder sb) { sb.append("X"); } // caller sees "X" — same object
void reassign(StringBuilder sb) { sb = new StringBuilder("New"); } // caller unaffected
```

---

### B5. `equals()` and `hashCode()` contract?

1. If `a.equals(b)` is true → `a.hashCode() == b.hashCode()` **must** be true.
2. Equal hashCodes do **not** require equal objects (collisions allowed).
3. Override **both** together, or hash-based collections (`HashMap`, `HashSet`) break.

---

### B6. `HashSet` vs `TreeSet` vs `LinkedHashSet`?

| | HashSet | TreeSet | LinkedHashSet |
|--|---------|---------|---------------|
| Order | None | Sorted | Insertion |
| Performance | O(1) | O(log n) | O(1) |
| Null | 1 allowed | ❌ | 1 allowed |
| Backed by | HashMap | TreeMap (RB-tree) | LinkedHashMap |

---

### B7. `throw` vs `throws`?

- **`throw`** — *statement* that actually raises an exception: `throw new IllegalArgumentException("bad");`
- **`throws`** — *declaration* in a method signature listing exceptions it may propagate: `void read() throws IOException`.

---

### B8. Can you override a `static` method?

**No** — static methods are **hidden**, not overridden (no dynamic dispatch). They belong to the class, resolved at compile time by the reference type. You also can't override `private` or `final` methods.

---

### B9. What is the `transient` keyword?

A field marked `transient` is **skipped during serialization** (its value isn't written out; it deserializes to the default — `null`/`0`). Use it for sensitive data (passwords) or derived/cacheable fields.

---

### B10. `ConcurrentHashMap` vs `Hashtable` vs `synchronizedMap`?

| | Hashtable | `synchronizedMap` | ConcurrentHashMap |
|--|-----------|-------------------|-------------------|
| Locking | Whole map | Whole map | Bucket/CAS level |
| Concurrency | Low | Low | High |
| Null keys/values | ❌ | ✅ (1 key) | ❌ |
| Status | Legacy | Wrapper | **Preferred** |

---

### B11. What does the `default` method in an interface solve?

It lets you **add methods to an interface without breaking existing implementors** (backward compatibility). This is how `Collection.stream()` and `Iterable.forEach()` were added in Java 8 without breaking millions of classes.

---

### B12. `Runnable` vs `Callable`?

| | `Runnable` | `Callable<V>` |
|--|-----------|---------------|
| Method | `run()` | `call()` |
| Returns | `void` | `V` (a value) |
| Throws checked? | ❌ | ✅ |
| Use with | `Thread`, `ExecutorService` | `ExecutorService` (→ `Future<V>`) |

---

### B13. What is `ExecutorService` and why prefer it over `new Thread()`?

It's a **thread pool** manager — you submit tasks, it reuses a fixed set of threads. Benefits: no per-task thread-creation cost, bounded resource usage, `Future` for results, graceful `shutdown()`.

```java
ExecutorService pool = Executors.newFixedThreadPool(4);
Future<Integer> f = pool.submit(() -> 2 + 2);
System.out.println(f.get()); // 4
pool.shutdown();
```

---

### B14. Difference between `wait()` and `sleep()`?

| | `wait()` | `sleep()` |
|--|----------|-----------|
| Defined in | `Object` | `Thread` |
| Releases lock? | ✅ Yes | ❌ No (keeps the monitor) |
| Wakes on | `notify()`/`notifyAll()` or timeout | timer expiry |
| Must hold monitor? | ✅ (synchronized block) | ❌ |

---

### B15. What is a memory leak in Java if GC exists?

GC only reclaims **unreachable** objects. A leak happens when you keep **unintended references** — e.g., objects stuck in a static `Map`/cache, unremoved listeners, or `ThreadLocal`s not cleaned up. They stay reachable, so GC never frees them.

---

### B16. What is the Stream API's laziness?

Stream **intermediate** operations (`map`, `filter`) are **lazy** — they build a pipeline but do nothing until a **terminal** operation (`collect`, `forEach`, `count`) runs. This enables short-circuiting (`findFirst`, `limit`) and fused, single-pass execution.

```java
Stream.of(1,2,3,4)
   .filter(n -> { System.out.println("filter " + n); return n % 2 == 0; })
   .findFirst();   // only processes until it finds 2 — not all elements
```

---

### B17. `final` vs `finally` vs `finalize` (quick table)

| Keyword | Kind | Purpose |
|---------|------|---------|
| `final` | modifier | Prevent reassignment / override / extension |
| `finally` | block | Always runs after try/catch (cleanup) |
| `finalize` | method | Deprecated GC hook — don't use |

---

### B18. What are generics and type erasure?

**Generics** provide compile-time type safety (`List<String>` can't hold an `Integer`). **Type erasure** means generic type info is **removed at compile time** — at runtime `List<String>` and `List<Integer>` are both just `List`. That's why you can't do `new T()` or `instanceof List<String>`.

---

## 🧠 Memory Hooks — The Trickiest Java Gotchas

Pin these to never get tripped up. Each gotcha → a hook you'll remember under pressure.

| Gotcha | The trap | 🧠 Memory Hook |
|--------|----------|----------------|
| **String Pool** | `"Hi" == "Hi"` is `true`, but `new String("Hi") == new String("Hi")` is `false` | **"Literals share a pool; `new` builds a private cabin."** Literals live in the shared pool; `new` always builds fresh. |
| **Integer cache (-128..127)** | `Integer a=127,b=127; a==b` → `true`; at `128` → `false` | **"127 is the speed limit."** Inside ±127 you ride the *same cached car*; over it, you get a new one. |
| **`==` vs `equals()`** | `==` on objects checks identity, not content | **"`==` asks 'same house?', `.equals()` asks 'same furniture?'"** |
| **Autoboxing NPE** | `Integer x = null; int y = x;` → NPE | **"Unboxing `null` is opening an empty box."** Always null-check before unboxing. |
| **Pass-by-value** | Reassigning a parameter never affects the caller | **"You get a photocopy of the address, not the house keys."** Mutate the object = OK; reassign the copy = no effect. |
| **`toMap` duplicate keys** | Collision throws `IllegalStateException` | **"Two keys, one slot → fight."** Pass a merge function: `(a,b) -> a`. |
| **Array `.equals()`** | `arr1.equals(arr2)` compares references | **"Arrays don't do `.equals` — call `Arrays.equals`."** |
| **`while` around `wait()`** | `if` lets spurious wakeups slip through | **"Wake up and *re-check* — loops guard against false alarms."** |
| **`volatile` ≠ atomic** | `volatile count++` still races | **"`volatile` = fresh eyes, not steady hands."** Visibility, not atomicity. Use `AtomicInteger`. |
| **Fail-fast remove** | `for(x:list) list.remove(x)` → CME | **"Don't saw the branch you're sitting on."** Use `Iterator.remove()` or `removeIf()`. |

---

## 🎤 Final Interview Tip

> **Lead with structure, then prove it with code.** When asked anything, give a crisp one-sentence answer first ("`==` compares references, `equals()` compares content"), *then* a table or example, *then* the gotcha. Interviewers are tired of rambling — a confident headline + a tight example + "and the classic trap here is…" signals senior-level thinking.

And for **output-prediction questions: slow down and narrate.** Say out loud: *"This is a `new String`, so distinct objects — `==` is false. But these are literals, so they share the pool — `==` is true. And this Integer is 128, past the cache, so `==` is false."* That narration is worth more than the right answer alone, because it proves you understand **references, the string pool, autoboxing, and the Integer cache** — the exact things these questions exist to test. 🚀

You've got this. Go get the offer. 💪
