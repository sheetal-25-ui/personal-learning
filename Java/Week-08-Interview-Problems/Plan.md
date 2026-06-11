# Week 8: Java Interview Questions — Microsoft, Anthropic, Amazon, Google

## Why This Week Matters

This is where everything comes together. These are the actual questions asked in FAANG and
top-tier tech company interviews. For each question, you get:
- The question as asked by the interviewer
- WHY they ask it (what skill they are testing)
- A short 30-second answer (for rapid-fire rounds)
- A detailed answer with code
- Follow-up questions to prepare for

---

## Category 1: Core Java (10 Questions)

---

### Q1: What is the difference between == and equals()?

**Why they ask:** Tests whether you understand reference equality vs value equality — one of the
most common Java bugs.

**Short answer:**
`==` compares memory addresses (are these the exact same object?). `equals()` compares the
content/value (do these objects have the same data?). For strings, always use `equals()`.

**Detailed answer with code:**

```java
// == compares REFERENCES (memory addresses)
String a = new String("Hello");
String b = new String("Hello");
System.out.println(a == b);      // false — two different objects in memory
System.out.println(a.equals(b)); // true  — same content "Hello"

// Tricky: String literals ARE the same reference (String Pool)
String c = "Hello";
String d = "Hello";
System.out.println(c == d);      // true! Both point to same String Pool object
System.out.println(c.equals(d)); // true

// With primitives, == compares VALUES (no objects involved)
int x = 5;
int y = 5;
System.out.println(x == y);      // true — values are equal

// Integer caching trap (interview favorite!)
Integer i1 = 127;
Integer i2 = 127;
System.out.println(i1 == i2);    // true — Java caches Integers -128 to 127

Integer i3 = 128;
Integer i4 = 128;
System.out.println(i3 == i4);    // false! — Beyond cache range, different objects
System.out.println(i3.equals(i4)); // true — same value
```

**Python comparison:**
```python
# Python: == compares values, 'is' compares identity (same as Java's ==)
a = [1, 2, 3]
b = [1, 2, 3]
print(a == b)   # True  (same content)
print(a is b)   # False (different objects)
```

**Follow-up questions:**
- What happens if you override equals() but not hashCode()? (HashMap breaks)
- What is the contract between equals() and hashCode()?
- How does the String pool affect == behavior?

---

### Q2: What is the difference between String, StringBuilder, and StringBuffer?

**Why they ask:** Tests understanding of immutability, thread safety, and performance.

**Short answer:**
String is immutable (every modification creates a new object). StringBuilder is mutable and fast
but not thread-safe. StringBuffer is mutable and thread-safe but slower due to synchronization.

**Detailed answer:**

```java
// String — immutable. Every "change" creates a new object.
String s = "Hello";
s = s + " World";
// "Hello" object still exists in memory (will be garbage collected).
// s now points to a NEW "Hello World" object.

// StringBuilder — mutable. Modifies the same object.
StringBuilder sb = new StringBuilder("Hello");
sb.append(" World");
// Same object, just modified. No new objects created.
// NOT thread-safe — do not use when multiple threads modify the same StringBuilder.

// StringBuffer — mutable, thread-safe. Every method is synchronized.
StringBuffer sbuf = new StringBuffer("Hello");
sbuf.append(" World");
// Same as StringBuilder but with synchronized methods — slower due to locking overhead.
```

**When to use which:**
- **String:** When the value does not change (names, configurations, constants)
- **StringBuilder:** 99% of cases when you need to build/modify strings (single-threaded)
- **StringBuffer:** Only when multiple threads modify the same string (very rare in modern Java)

**Performance demonstration:**

```java
// String concatenation in a loop — SLOW (O(n^2) due to copying)
long start = System.currentTimeMillis();
String result = "";
for (int i = 0; i < 100000; i++) {
    result += "a"; // Creates a new String every iteration!
}
System.out.println("String: " + (System.currentTimeMillis() - start) + "ms");
// Takes several seconds!

// StringBuilder — FAST (O(n))
start = System.currentTimeMillis();
StringBuilder sb2 = new StringBuilder();
for (int i = 0; i < 100000; i++) {
    sb2.append("a"); // Modifies the same object
}
System.out.println("StringBuilder: " + (System.currentTimeMillis() - start) + "ms");
// Takes a few milliseconds!
```

**Follow-up questions:**
- Why is String immutable? (Thread safety, caching, security)
- Can you use String in a loop? When is it okay? (Small number of concatenations is fine)
- What is intern()? When would you use it?

---

### Q3: Why is String immutable in Java?

**Why they ask:** Tests deeper understanding of language design decisions.

**Short answer:**
String is immutable for security (credentials cannot be modified), thread safety (can be shared
across threads without synchronization), caching (String pool optimization), and hashCode
caching (safe as HashMap keys).

**Detailed answer:**

1. **Security:** Strings are used for database URLs, passwords, file paths, class names. If they
   were mutable, malicious code could change a filename after security checks but before the
   file is opened.

2. **String Pool:** Because Strings are immutable, Java can safely reuse them. When you write
   `"Hello"` in 100 places, Java stores it only ONCE. If Strings were mutable, changing one
   would change all 100.

3. **Thread Safety:** Immutable objects are inherently thread-safe. Multiple threads can share
   the same String without synchronization.

4. **HashMap Keys:** HashMap uses the hashCode to determine the bucket. If a String's content
   could change, its hashCode would change, and you would never be able to find it in the
   HashMap again.

5. **hashCode Caching:** Since String never changes, its hashCode is computed once and cached.
   This makes HashMap lookups with String keys very fast.

---

### Q4: What is the difference between abstract class and interface?

**Why they ask:** Tests OOP design understanding and when to use which.

**Short answer:**
An abstract class is a partial implementation that child classes complete. An interface is a
contract that classes agree to fulfill. Use abstract class for "is-a" relationships with shared
code. Use interface for "can-do" capabilities.

**Detailed answer:**

| Feature | Abstract Class | Interface |
|---------|---------------|-----------|
| Methods | Can have abstract AND concrete | All abstract (before Java 8); can have default methods (Java 8+) |
| Variables | Can have instance variables | Only public static final constants |
| Constructor | CAN have constructors | CANNOT have constructors |
| Inheritance | Single (extend ONE class) | Multiple (implement MANY interfaces) |
| Access modifiers | Any (private, protected, public) | Only public (before Java 9) |
| When to use | Common code to share among related classes | Define a capability/contract |

```java
// Abstract class — "is a" relationship with shared code
abstract class Vehicle {
    protected String brand;
    protected int speed;

    public Vehicle(String brand) {  // Constructor
        this.brand = brand;
    }

    public void accelerate() {       // Concrete method (shared)
        speed += 10;
    }

    abstract void start();           // Abstract method (each subclass implements differently)
}

class Car extends Vehicle {
    public Car(String brand) { super(brand); }

    @Override
    void start() { System.out.println("Turn key"); }
}

// Interface — "can do" capability
interface GPS {
    void navigate(String destination);  // What to do
    default void showMap() {            // How to do it (default)
        System.out.println("Showing map...");
    }
}

interface Bluetooth {
    void connect(String device);
}

// A car IS a Vehicle and CAN DO GPS and Bluetooth
class SmartCar extends Vehicle implements GPS, Bluetooth {
    public SmartCar(String brand) { super(brand); }

    @Override void start() { System.out.println("Press button"); }
    @Override public void navigate(String dest) { System.out.println("Navigating to " + dest); }
    @Override public void connect(String device) { System.out.println("Connected to " + device); }
}
```

**Follow-up:** When would you use an abstract class over an interface?
When you need shared state (instance variables) or constructors, or when the classes are closely
related (e.g., all are types of Vehicle).

---

### Q5: Explain method overloading vs method overriding

**Why they ask:** Tests compile-time vs runtime polymorphism understanding.

**Short answer:**
Overloading = same method name, different parameters, in the SAME class (decided at compile
time). Overriding = same method name AND parameters, in a SUBCLASS (decided at runtime).

```java
// OVERLOADING — same class, different parameters
class Calculator {
    int add(int a, int b) { return a + b; }           // Two ints
    double add(double a, double b) { return a + b; }  // Two doubles
    int add(int a, int b, int c) { return a + b + c; } // Three ints
    // Compiler picks the right one based on arguments
}

// OVERRIDING — subclass redefines parent's method
class Animal {
    void speak() { System.out.println("..."); }
}

class Dog extends Animal {
    @Override
    void speak() { System.out.println("Woof!"); }  // SAME name, SAME parameters
}

Animal a = new Dog();
a.speak(); // "Woof!" — decided at RUNTIME based on actual object type
```

**Key interview point:** Overloading is resolved at COMPILE time (static polymorphism).
Overriding is resolved at RUNTIME (dynamic polymorphism). This is why:

```java
Animal a = new Dog();
// Compile time: a is type Animal (overloading would look at Animal's methods)
// Runtime: a is actually a Dog (overriding calls Dog's method)
```

---

### Q6: What is autoboxing and unboxing?

**Why they ask:** Tests understanding of primitive vs wrapper types and performance implications.

**Short answer:**
Autoboxing is automatic conversion from primitive to wrapper (`int` to `Integer`). Unboxing is
the reverse (`Integer` to `int`). Java does this automatically, but it has a performance cost.

```java
// Autoboxing: primitive -> wrapper
Integer num = 42;        // int 42 automatically wrapped into Integer object
List<Integer> list = new ArrayList<>();
list.add(5);             // int 5 is autoboxed to Integer

// Unboxing: wrapper -> primitive
int value = num;         // Integer automatically unwrapped to int
int sum = num + 10;      // num unboxed, added to 10

// DANGER: NullPointerException with unboxing!
Integer nullNum = null;
int crash = nullNum;     // NullPointerException! Cannot unbox null.

// PERFORMANCE TRAP in loops
Long total = 0L;  // Wrapper type!
for (int i = 0; i < 1000000; i++) {
    total += i;   // Creates a new Long object EVERY iteration! Very slow.
}
// Fix: use primitive 'long' instead of wrapper 'Long'
long total2 = 0L; // Primitive — no boxing overhead
```

---

### Q7: What are checked vs unchecked exceptions?

**Why they ask:** Tests exception handling design understanding.

**Short answer:**
Checked exceptions MUST be handled (try-catch or throws) — compiler forces you. Unchecked
exceptions (RuntimeException and subclasses) do not require handling — they indicate programming
bugs.

```java
// CHECKED — compiler forces you to handle it
try {
    FileReader reader = new FileReader("file.txt");  // Throws FileNotFoundException (checked)
} catch (FileNotFoundException e) {
    System.out.println("File not found!");
}

// Or declare it:
public void readFile() throws FileNotFoundException {
    FileReader reader = new FileReader("file.txt");
}

// UNCHECKED — compiler does not force handling
int[] arr = {1, 2, 3};
int value = arr[10];  // ArrayIndexOutOfBoundsException (unchecked)
// You CAN catch it, but you do not HAVE to.

String s = null;
s.length();  // NullPointerException (unchecked)
```

**Hierarchy:**
```
Throwable
├── Error (unrecoverable: OutOfMemoryError, StackOverflowError — do not catch)
└── Exception
    ├── Checked Exceptions (IOException, SQLException — MUST handle)
    └── RuntimeException (unchecked)
        ├── NullPointerException
        ├── ArrayIndexOutOfBoundsException
        ├── IllegalArgumentException
        └── ClassCastException
```

---

### Q8: Explain the final, finally, and finalize keywords

**Why they ask:** A classic Java trivia question that tests breadth of knowledge.

**Short answer:**
`final` prevents modification (final variable cannot be reassigned, final method cannot be
overridden, final class cannot be extended). `finally` is a block that ALWAYS executes after
try-catch. `finalize()` is a deprecated method called by GC before destroying an object.

```java
// final — prevents modification
final int MAX = 100;
// MAX = 200;  // COMPILE ERROR! Cannot reassign final variable

final class ImmutableClass {
    // Cannot be extended (no subclasses)
}

class Parent {
    final void criticalMethod() {
        // Cannot be overridden by subclasses
    }
}

// finally — always executes
try {
    int result = 10 / 0;
} catch (ArithmeticException e) {
    System.out.println("Error!");
} finally {
    System.out.println("This ALWAYS runs — error or not");
    // Used for cleanup: closing files, database connections, etc.
}

// finalize — deprecated since Java 9, removed in Java 18
// Was called by GC before destroying an object. Unreliable.
// Use try-with-resources or Cleaner instead.
```

---

### Q9: What is the difference between ArrayList and LinkedList?

**Why they ask:** Tests data structure understanding and ability to choose the right tool.

**Short answer:**
ArrayList uses a dynamic array — fast random access O(1), slow insert/delete in middle O(n).
LinkedList uses doubly-linked nodes — slow random access O(n), fast insert/delete O(1) if you
have the node reference.

| Operation | ArrayList | LinkedList |
|-----------|-----------|------------|
| get(index) | O(1) — direct index access | O(n) — must traverse from head |
| add(end) | O(1) amortized | O(1) |
| add(middle) | O(n) — shift elements | O(1) if at node, O(n) to find position |
| remove(middle) | O(n) — shift elements | O(1) if at node, O(n) to find position |
| Memory | Less (just array) | More (each node stores prev/next pointers) |
| Cache performance | Better (contiguous memory) | Worse (nodes scattered in heap) |

**Real-life analogy:**
- **ArrayList** = A row of seats in a movie theater. Finding seat #47 is instant (count from
  the start). But inserting a new seat in the middle means moving everyone else over.
- **LinkedList** = A train. Each coach knows only the next and previous coach. To find coach #47,
  you must walk through all 46 coaches. But detaching/attaching a coach is easy.

**Interview answer:** In practice, **ArrayList is almost always the better choice.** Modern CPUs
heavily favor contiguous memory (cache-friendly). LinkedList's theoretical O(1) insert advantage
is usually outweighed by its terrible cache performance. Use LinkedList only when you frequently
add/remove from both ends (use it as a Deque).

---

### Q10: How does HashMap work internally?

**Why they ask:** THE most important data structure question for Java interviews. Tests deep
understanding.

**Short answer:**
HashMap uses an array of "buckets." When you put(key, value), it computes hashCode of the key,
finds the bucket index, and stores the entry. On collision, it uses a linked list (or tree for
8+ entries). get(key) follows the same path to find the value.

**Detailed answer:**

```
HashMap Internal Structure:

Bucket Array (Node<K,V>[])
Index:  [0]   [1]   [2]   [3]   [4]   [5]   [6]   [7]
         |     |     |     |     |     |     |     |
        null  null  Entry  null  Entry null  null  Entry
                     |            |                  |
                    Entry        null               Entry
                     |                               |
                    null                            null

Each bucket is a linked list of entries (Node<K,V>).
If a bucket has 8+ entries, it converts to a Red-Black Tree (O(log n) instead of O(n)).
```

**Step by step — put(key, value):**

```java
HashMap<String, Integer> map = new HashMap<>();
map.put("Aarav", 85000);

// Step 1: Calculate hashCode of key
int hash = "Aarav".hashCode(); // Some integer, e.g., 63476538

// Step 2: Calculate bucket index
int index = hash & (capacity - 1); // Bitwise AND with (array size - 1)
// e.g., 63476538 & 15 = 10 (if capacity is 16)

// Step 3: Check if bucket is empty
// If empty: create a new Node and put it there
// If not empty: traverse the linked list/tree in that bucket
//   - If key already exists (equals() check): replace the value
//   - If key is new: add to the end of the list

// Step 4: Check load factor
// If (size / capacity) > 0.75, RESIZE the array to double (rehash all entries)
```

**Step by step — get(key):**

```java
int salary = map.get("Aarav");

// Step 1: Calculate hashCode of "Aarav"
// Step 2: Find bucket index (same formula as put)
// Step 3: Search the bucket's linked list/tree
//   - Compare hashCode first (fast integer comparison)
//   - If hashCode matches, compare with equals() (slower, but necessary for collisions)
// Step 4: Return the value, or null if not found
```

**Critical interview points:**
1. **hashCode() and equals() contract:** If two objects are equal (`equals()` returns true),
   they MUST have the same hashCode. But two objects with the same hashCode are NOT necessarily
   equal (collision).

2. **Why capacity is always a power of 2:** So `hash & (capacity - 1)` works as a fast modulo.

3. **Treeification (Java 8+):** When a bucket's linked list grows to 8 entries, it converts to
   a Red-Black Tree. This changes worst-case lookup from O(n) to O(log n).

4. **Default capacity:** 16 buckets. **Default load factor:** 0.75. When 75% full, the array
   doubles in size and ALL entries are rehashed.

---

## Category 2: Multithreading (5 Questions)

---

### Q11: What is the difference between start() and run()?

**Why they ask:** Ensures you understand thread creation, not just method calling.

**Short answer:**
`start()` creates a new thread of execution and then calls `run()` in that new thread.
Calling `run()` directly just executes the method in the CURRENT thread — no new thread is
created.

```java
class MyThread extends Thread {
    public void run() {
        System.out.println("Running in: " + Thread.currentThread().getName());
    }
}

MyThread t = new MyThread();

t.run();   // Output: "Running in: main"     — NO new thread! Just a method call.
t.start(); // Output: "Running in: Thread-0"  — NEW thread created!
```

**Follow-up:** Can you call start() twice on the same thread?
No. It throws `IllegalThreadStateException`. A thread can only be started once. Once terminated,
it is dead — create a new Thread if you need to run the task again.

---

### Q12: What is a deadlock? How to prevent it?

**Why they ask:** Tests understanding of a critical concurrency problem.

**Short answer:**
Deadlock is when two or more threads wait for each other to release locks, creating a circular
wait. Nobody can proceed. Prevent it by always acquiring locks in the same order.

```java
// DEADLOCK: Thread 1 holds A, needs B. Thread 2 holds B, needs A.
Object lockA = new Object();
Object lockB = new Object();

// Thread 1
new Thread(() -> {
    synchronized (lockA) {
        Thread.sleep(100);
        synchronized (lockB) { /* never reaches here */ }
    }
}).start();

// Thread 2
new Thread(() -> {
    synchronized (lockB) {
        Thread.sleep(100);
        synchronized (lockA) { /* never reaches here */ }
    }
}).start();

// FIX: Both threads lock in the same order (A then B)
// Thread 2 should also do synchronized(lockA) { synchronized(lockB) { ... } }
```

**Four conditions for deadlock (ALL must be true):**
1. Mutual Exclusion — resource held exclusively
2. Hold and Wait — holding one, waiting for another
3. No Preemption — cannot force-take a lock
4. Circular Wait — A waits for B, B waits for A

**Break any one to prevent deadlock.**

---

### Q13: What is the difference between synchronized and Lock?

**Why they ask:** Tests awareness of modern concurrency tools.

**Short answer:**
`synchronized` is simpler but less flexible. `Lock` (ReentrantLock) offers tryLock(), timed
waits, interruptibility, and fairness — but requires manual unlock in a finally block.

See the comparison table in Week 5. Key addition:

```java
// Lock allows trying without blocking
ReentrantLock lock = new ReentrantLock();

if (lock.tryLock(5, TimeUnit.SECONDS)) {
    try {
        // Got the lock within 5 seconds
    } finally {
        lock.unlock();
    }
} else {
    // Could not get lock — do something else (avoid deadlock!)
}
```

---

### Q14: Explain the producer-consumer problem

**Why they ask:** Classic concurrency problem that tests synchronization skills.

Refer to the detailed implementation in Week 5. Key points for the interview:

1. Show the wait/notify solution (proves understanding of thread communication)
2. Mention BlockingQueue as the production solution
3. Explain why you use `while` not `if` around `wait()` (spurious wakeups)
4. Explain why `notifyAll()` is safer than `notify()`

---

### Q15: What is the volatile keyword?

**Why they ask:** Tests understanding of memory visibility between threads.

**Short answer:**
`volatile` ensures that a variable's value is always read from main memory, not from a thread's
local cache. It guarantees visibility but NOT atomicity.

```java
// Without volatile: thread might read stale cached value
// With volatile: thread always reads latest value from main memory

private volatile boolean running = true;

// Thread 1 (writer)
public void stop() { running = false; }

// Thread 2 (reader)
public void work() {
    while (running) {
        // Without volatile, this loop might run forever because
        // Thread 2 reads a cached value of 'running' that never updates
    }
}
```

**Key point:** `volatile` is NOT enough for `count++` because that is read + modify + write
(three operations). Use `AtomicInteger` or `synchronized` for compound operations.

---

## Category 3: Java 8 (10 Questions with Code)

---

### Q16: Write a stream to find the second highest number in a list

```java
List<Integer> numbers = Arrays.asList(5, 3, 9, 1, 9, 7, 3, 8);

Optional<Integer> secondHighest = numbers.stream()
    .distinct()                            // Remove duplicates [5, 3, 9, 1, 7, 8]
    .sorted(Comparator.reverseOrder())     // Sort descending [9, 8, 7, 5, 3, 1]
    .skip(1)                               // Skip the first (highest)
    .findFirst();                          // Get the second

System.out.println(secondHighest.orElse(-1)); // 8
```

---

### Q17: Write a stream to group employees by department

```java
Map<String, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment));

// With count per department
Map<String, Long> countByDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));

// With names per department
Map<String, List<String>> namesByDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.mapping(Employee::getName, Collectors.toList())
    ));
```

---

### Q18: Write a stream to find duplicate elements

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 2, 4, 3, 5);

// Method 1: Using Set
Set<Integer> seen = new HashSet<>();
List<Integer> duplicates = numbers.stream()
    .filter(n -> !seen.add(n))    // add() returns false if already present
    .collect(Collectors.toList());
// [2, 3]

// Method 2: Using groupingBy (pure functional — no mutable state)
Set<Integer> duplicates2 = numbers.stream()
    .collect(Collectors.groupingBy(n -> n, Collectors.counting()))
    .entrySet().stream()
    .filter(e -> e.getValue() > 1)
    .map(Map.Entry::getKey)
    .collect(Collectors.toSet());
```

**Interview tip:** Method 1 uses shared mutable state (`seen`) which is technically impure and
not safe for parallel streams. Mention Method 2 as the proper functional approach.

---

### Q19: What is the difference between map() and flatMap()?

**Why they ask:** flatMap is tricky and shows depth of understanding.

**Short answer:**
`map()` transforms each element one-to-one. `flatMap()` transforms each element to a stream,
then flattens all the streams into one.

```java
// map: one element -> one element
List<String> words = Arrays.asList("Hello", "World");
List<Integer> lengths = words.stream()
    .map(String::length)
    .collect(Collectors.toList());
// [5, 5]

// flatMap: one element -> multiple elements (flattened)
List<String> sentences = Arrays.asList("Hello World", "Java Streams");
List<String> allWords = sentences.stream()
    .flatMap(sentence -> Arrays.stream(sentence.split(" ")))
    .collect(Collectors.toList());
// [Hello, World, Java, Streams]

// Without flatMap, you would get List<Stream<String>> — not useful!
```

**Visual:**
```
map:     [A, B, C] -> [f(A), f(B), f(C)]           One to one
flatMap: [A, B, C] -> [a1,a2, b1,b2,b3, c1]        One to many, then flatten
```

---

### Q20: What is Optional? How to use it properly?

**Short answer:**
Optional is a container for a value that might be absent. Use it instead of returning null.

```java
// WRONG: returning null
public Employee findById(String id) {
    // ... might return null
}
Employee emp = findById("123");
emp.getName(); // NullPointerException if null!

// RIGHT: returning Optional
public Optional<Employee> findById(String id) {
    // ... returns Optional.empty() if not found
}

// Safe usage
findById("123")
    .map(Employee::getName)
    .ifPresent(name -> System.out.println("Found: " + name));

// Or with default
String name = findById("123")
    .map(Employee::getName)
    .orElse("Unknown Employee");
```

**Rules:**
- DO use for return types
- DO NOT use for method parameters
- DO NOT use for class fields
- NEVER call `.get()` without checking `.isPresent()` first (use `orElse` or `orElseThrow`)

---

### Q21: Write a lambda to sort a list of strings by length

```java
List<String> words = Arrays.asList("Bangalore", "Goa", "Mumbai", "Delhi", "Kochi");

// Lambda
words.sort((a, b) -> Integer.compare(a.length(), b.length()));

// Method reference with Comparator
words.sort(Comparator.comparingInt(String::length));

// Reverse order
words.sort(Comparator.comparingInt(String::length).reversed());

System.out.println(words);
// [Goa, Delhi, Kochi, Mumbai, Bangalore]
```

---

### Q22: What is a functional interface? Give examples.

**Short answer:**
An interface with exactly one abstract method. Can be used as a target for lambda expressions.

```java
// Built-in examples:
// Predicate<T>:    T -> boolean     (test)
// Function<T,R>:   T -> R           (apply)
// Consumer<T>:     T -> void        (accept)
// Supplier<T>:     () -> T          (get)
// Comparator<T>:   (T, T) -> int    (compare)
// Runnable:        () -> void       (run)
// Callable<V>:     () -> V          (call)

// Custom:
@FunctionalInterface
interface Validator<T> {
    boolean validate(T item);
}

Validator<String> emailValidator = email -> email.contains("@") && email.contains(".");
```

---

### Q23: Explain Predicate, Function, Consumer, Supplier with code

```java
import java.util.function.*;

// Predicate — takes input, returns boolean
Predicate<Integer> isAdult = age -> age >= 18;
System.out.println(isAdult.test(25)); // true

// Function — takes input, returns output
Function<String, Integer> wordCount = sentence -> sentence.split(" ").length;
System.out.println(wordCount.apply("Hello World from Java")); // 4

// Consumer — takes input, returns nothing (side effect)
Consumer<String> sendSMS = phone -> System.out.println("SMS sent to " + phone);
sendSMS.accept("9876543210");

// Supplier — takes nothing, returns output
Supplier<LocalDate> today = LocalDate::now;
System.out.println(today.get()); // 2026-06-03

// Combining them in a real scenario
List<Employee> employees = getEmployees();

employees.stream()
    .filter(isHighPaid)       // Predicate
    .map(toDisplayName)       // Function
    .forEach(sendNotification); // Consumer
```

---

### Q24: Difference between stream() and parallelStream()

```java
// stream() — processes elements sequentially in the calling thread
long start = System.currentTimeMillis();
list.stream()
    .map(this::expensiveOperation)  // One at a time
    .collect(Collectors.toList());
// Takes: N * operationTime

// parallelStream() — splits work across multiple threads (ForkJoinPool)
list.parallelStream()
    .map(this::expensiveOperation)  // Multiple at a time
    .collect(Collectors.toList());
// Takes: roughly N * operationTime / numberOfCores
```

**When to use parallelStream:**
- Large dataset (10,000+ elements)
- CPU-intensive operations (not I/O)
- Operations are independent (no shared mutable state)
- Order does not matter

**When NOT to use:**
- Small datasets (overhead > benefit)
- I/O operations (threads will block, wastes thread pool)
- When order matters and you need sequential processing
- When operations modify shared state

---

### Q25: Write a stream to convert List<String> to Map<String, Integer> (word to length)

```java
List<String> words = Arrays.asList("Mumbai", "Delhi", "Goa", "Chennai");

Map<String, Integer> wordToLength = words.stream()
    .collect(Collectors.toMap(
        word -> word,           // key: the word itself
        String::length          // value: its length
    ));
// {Mumbai=6, Delhi=5, Goa=3, Chennai=7}

// Handling duplicate keys (if words can repeat)
List<String> withDuplicates = Arrays.asList("hi", "hello", "hi");
Map<String, Integer> safe = withDuplicates.stream()
    .collect(Collectors.toMap(
        word -> word,
        String::length,
        (existing, replacement) -> existing  // Keep the first value on conflict
    ));
```

---

## Category 4: Collections Deep Dive (5 Questions)

---

### Q26: HashMap vs TreeMap vs LinkedHashMap

**Short answer:**

| Feature | HashMap | TreeMap | LinkedHashMap |
|---------|---------|---------|---------------|
| Order | No order | Sorted by key | Insertion order |
| Performance | O(1) get/put | O(log n) get/put | O(1) get/put |
| Null keys | One null key allowed | NO null keys | One null key allowed |
| Implementation | Hash table | Red-Black Tree | Hash table + linked list |
| Use when | Default choice, fastest | Need sorted keys | Need insertion order preserved |

```java
// HashMap — no order
Map<String, Integer> hashMap = new HashMap<>();
hashMap.put("Banana", 2);
hashMap.put("Apple", 1);
hashMap.put("Cherry", 3);
// Iteration order: unpredictable

// TreeMap — sorted by key
Map<String, Integer> treeMap = new TreeMap<>();
treeMap.put("Banana", 2);
treeMap.put("Apple", 1);
treeMap.put("Cherry", 3);
// Iteration order: Apple, Banana, Cherry (alphabetical)

// LinkedHashMap — insertion order
Map<String, Integer> linkedMap = new LinkedHashMap<>();
linkedMap.put("Banana", 2);
linkedMap.put("Apple", 1);
linkedMap.put("Cherry", 3);
// Iteration order: Banana, Apple, Cherry (insertion order)
```

---

### Q27: How does ConcurrentHashMap work?

**Why they ask:** Tests knowledge of thread-safe collections.

**Short answer:**
ConcurrentHashMap allows concurrent reads and segment-level locking for writes. Unlike
synchronized HashMap (which locks the entire map), ConcurrentHashMap only locks the bucket
being modified — so multiple threads can write to different buckets simultaneously.

```java
// WRONG: Regular HashMap is NOT thread-safe
Map<String, Integer> map = new HashMap<>(); // Multiple threads writing = data corruption

// SLOW: Synchronized wrapper locks EVERYTHING
Map<String, Integer> syncMap = Collections.synchronizedMap(new HashMap<>());
// Every read AND write locks the entire map. Very slow under high concurrency.

// RIGHT: ConcurrentHashMap — fine-grained locking
ConcurrentHashMap<String, Integer> concMap = new ConcurrentHashMap<>();
concMap.put("key", 1);                       // Thread-safe
concMap.computeIfAbsent("key", k -> 1);      // Atomic compute
concMap.merge("key", 1, Integer::sum);        // Atomic merge
```

**Java 8 changes:** ConcurrentHashMap in Java 8+ uses CAS (Compare-And-Swap) operations and
node-level locking instead of segment locks — even more fine-grained and faster.

---

### Q28: Comparable vs Comparator

**Short answer:**
`Comparable` defines the DEFAULT/natural ordering of a class (implemented inside the class
itself). `Comparator` defines an EXTERNAL/custom ordering (implemented outside the class).

```java
// Comparable — the class decides its own natural ordering
class Employee implements Comparable<Employee> {
    private String name;
    private double salary;

    @Override
    public int compareTo(Employee other) {
        return Double.compare(this.salary, other.salary);  // Natural order: by salary
    }
}
Collections.sort(employees); // Uses compareTo (by salary)

// Comparator — external ordering, can have multiple
Comparator<Employee> byName = Comparator.comparing(Employee::getName);
Comparator<Employee> bySalaryDesc = Comparator.comparingDouble(Employee::getSalary).reversed();
Comparator<Employee> byDeptThenSalary = Comparator.comparing(Employee::getDepartment)
    .thenComparingDouble(Employee::getSalary);

employees.sort(byName);           // Sort by name
employees.sort(bySalaryDesc);     // Sort by salary descending
employees.sort(byDeptThenSalary); // Sort by dept, then salary within dept
```

**Use Comparable for:** the one obvious default order (strings alphabetically, numbers ascending).
**Use Comparator for:** any custom or alternative ordering.

---

### Q29: What happens when you put a duplicate key in HashMap?

**Short answer:**
The old value is replaced by the new value. The key is NOT duplicated.

```java
Map<String, Integer> map = new HashMap<>();
map.put("Aarav", 85000);
map.put("Aarav", 92000);   // Replaces 85000

System.out.println(map.get("Aarav")); // 92000
System.out.println(map.size());        // 1 (not 2!)

// The put() method returns the OLD value (or null if key was new)
Integer oldValue = map.put("Aarav", 100000); // Returns 92000
```

**How it works internally:**
1. HashMap computes hashCode of the key
2. Finds the bucket
3. Traverses the bucket's linked list
4. For each node, checks: `node.hash == hash && node.key.equals(newKey)`
5. If match found: replace `node.value` with new value
6. If no match: add new node to the bucket

---

### Q30: Explain fail-fast vs fail-safe iterators

**Short answer:**
Fail-fast iterators throw `ConcurrentModificationException` if the collection is modified during
iteration (ArrayList, HashMap). Fail-safe iterators work on a copy of the collection and allow
modification (ConcurrentHashMap, CopyOnWriteArrayList).

```java
// FAIL-FAST — throws ConcurrentModificationException
List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C"));
for (String item : list) {
    if (item.equals("B")) {
        list.remove(item);   // ConcurrentModificationException!
    }
}

// FIX 1: Use Iterator's remove method
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    if (it.next().equals("B")) {
        it.remove();         // Safe! Iterator knows about the removal.
    }
}

// FIX 2: Use removeIf (Java 8+)
list.removeIf(item -> item.equals("B"));

// FAIL-SAFE — works on a copy, no exception
CopyOnWriteArrayList<String> safeList = new CopyOnWriteArrayList<>(Arrays.asList("A", "B", "C"));
for (String item : safeList) {
    if (item.equals("B")) {
        safeList.remove(item);  // No exception! Iterator works on a snapshot.
    }
}
```

---

## 10 Coding Problems Commonly Asked in Java Interviews

---

### Problem 1: Reverse a String Without Using Built-in reverse()

```java
public static String reverse(String input) {
    char[] chars = input.toCharArray();
    int left = 0, right = chars.length - 1;

    while (left < right) {
        // Swap characters
        char temp = chars[left];
        chars[left] = chars[right];
        chars[right] = temp;
        left++;
        right--;
    }

    return new String(chars);
}

// Test
System.out.println(reverse("Hello")); // "olleH"
```

---

### Problem 2: Check if Two Strings are Anagrams

```java
public static boolean isAnagram(String s1, String s2) {
    if (s1.length() != s2.length()) return false;

    // Count character frequencies
    int[] counts = new int[256]; // ASCII characters

    for (char c : s1.toCharArray()) counts[c]++;
    for (char c : s2.toCharArray()) counts[c]--;

    for (int count : counts) {
        if (count != 0) return false;
    }
    return true;
}

// Java 8 way using streams
public static boolean isAnagramStream(String s1, String s2) {
    return s1.length() == s2.length() &&
        s1.chars().sorted().toArray().equals(s2.chars().sorted().toArray());
    // Note: use Arrays.equals for array comparison
}

System.out.println(isAnagram("listen", "silent")); // true
System.out.println(isAnagram("hello", "world"));   // false
```

---

### Problem 3: Find the First Non-Repeating Character in a String

```java
public static char firstNonRepeating(String input) {
    // LinkedHashMap preserves insertion order
    Map<Character, Integer> counts = new LinkedHashMap<>();

    for (char c : input.toCharArray()) {
        counts.put(c, counts.getOrDefault(c, 0) + 1);
    }

    // Find first with count 1
    for (Map.Entry<Character, Integer> entry : counts.entrySet()) {
        if (entry.getValue() == 1) {
            return entry.getKey();
        }
    }

    return '_'; // No non-repeating character found
}

// Java 8 streams way
public static Character firstNonRepeatingStream(String input) {
    return input.chars()
        .mapToObj(c -> (char) c)
        .collect(Collectors.groupingBy(c -> c, LinkedHashMap::new, Collectors.counting()))
        .entrySet().stream()
        .filter(e -> e.getValue() == 1)
        .map(Map.Entry::getKey)
        .findFirst()
        .orElse('_');
}

System.out.println(firstNonRepeating("aabbcdeff")); // 'c'
```

---

### Problem 4: FizzBuzz (Yes, It Is Still Asked)

```java
// Classic way
for (int i = 1; i <= 100; i++) {
    if (i % 15 == 0) System.out.println("FizzBuzz");
    else if (i % 3 == 0) System.out.println("Fizz");
    else if (i % 5 == 0) System.out.println("Buzz");
    else System.out.println(i);
}

// Java 8 Streams way (shows you know modern Java)
IntStream.rangeClosed(1, 100)
    .mapToObj(i -> i % 15 == 0 ? "FizzBuzz" :
                   i % 3 == 0  ? "Fizz" :
                   i % 5 == 0  ? "Buzz" :
                   String.valueOf(i))
    .forEach(System.out::println);
```

---

### Problem 5: Implement a Custom HashMap

```java
class SimpleHashMap<K, V> {
    private static final int DEFAULT_CAPACITY = 16;

    // Each node in the bucket's linked list
    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private Node<K, V>[] buckets;
    private int size;

    @SuppressWarnings("unchecked")
    public SimpleHashMap() {
        buckets = new Node[DEFAULT_CAPACITY];
        size = 0;
    }

    private int getBucketIndex(K key) {
        return Math.abs(key.hashCode()) % buckets.length;
    }

    public void put(K key, V value) {
        int index = getBucketIndex(key);
        Node<K, V> current = buckets[index];

        // Check if key already exists
        while (current != null) {
            if (current.key.equals(key)) {
                current.value = value; // Update
                return;
            }
            current = current.next;
        }

        // Key does not exist — add to front of list
        Node<K, V> newNode = new Node<>(key, value);
        newNode.next = buckets[index];
        buckets[index] = newNode;
        size++;
    }

    public V get(K key) {
        int index = getBucketIndex(key);
        Node<K, V> current = buckets[index];

        while (current != null) {
            if (current.key.equals(key)) {
                return current.value;
            }
            current = current.next;
        }
        return null; // Not found
    }

    public int size() { return size; }
}

// Test
SimpleHashMap<String, Integer> map = new SimpleHashMap<>();
map.put("Aarav", 85000);
map.put("Priya", 65000);
System.out.println(map.get("Aarav")); // 85000
System.out.println(map.size());        // 2
```

---

### Problem 6: Producer-Consumer Using BlockingQueue

```java
import java.util.concurrent.*;

public class ProducerConsumer {
    public static void main(String[] args) {
        BlockingQueue<String> orderQueue = new ArrayBlockingQueue<>(5);

        // Zomato restaurant (producer)
        Thread restaurant = new Thread(() -> {
            String[] dishes = {"Biryani", "Dosa", "Pizza", "Burger", "Paneer"};
            for (String dish : dishes) {
                try {
                    orderQueue.put(dish);
                    System.out.println("Prepared: " + dish);
                    Thread.sleep(500);
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }, "Restaurant");

        // Zomato delivery person (consumer)
        Thread delivery = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                try {
                    String dish = orderQueue.take();
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

---

### Problem 7: Implement LRU Cache

```java
import java.util.*;

class LRUCache<K, V> {
    private final int capacity;
    private final Map<K, V> cache;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        // LinkedHashMap with accessOrder=true automatically moves accessed entries to the end
        this.cache = new LinkedHashMap<K, V>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > capacity; // Remove oldest when capacity exceeded
            }
        };
    }

    public V get(K key) {
        return cache.getOrDefault(key, null);
    }

    public void put(K key, V value) {
        cache.put(key, value);
    }
}

// Test
LRUCache<Integer, String> cache = new LRUCache<>(3);
cache.put(1, "One");
cache.put(2, "Two");
cache.put(3, "Three");
cache.get(1);              // Access 1 — moves to end
cache.put(4, "Four");      // Evicts 2 (least recently used)
System.out.println(cache.get(2)); // null — evicted!
System.out.println(cache.get(1)); // "One" — still there
```

---

### Problem 8: Flatten a Nested List Using Recursion and Streams

```java
// Input: [[1, 2, [3]], 4, [[5, 6], 7]]
// Output: [1, 2, 3, 4, 5, 6, 7]

public static List<Integer> flatten(List<?> nested) {
    List<Integer> result = new ArrayList<>();
    for (Object item : nested) {
        if (item instanceof List) {
            result.addAll(flatten((List<?>) item));
        } else {
            result.add((Integer) item);
        }
    }
    return result;
}
```

---

### Problem 9: Implement Thread-Safe Singleton

```java
// Bill Pugh Singleton — preferred approach
public class ConnectionPool {
    private final List<Connection> connections;

    private ConnectionPool() {
        connections = new ArrayList<>();
        // Initialize pool
        for (int i = 0; i < 10; i++) {
            connections.add(createConnection());
        }
    }

    // Inner class is not loaded until getInstance() is called
    private static class Holder {
        private static final ConnectionPool INSTANCE = new ConnectionPool();
    }

    public static ConnectionPool getInstance() {
        return Holder.INSTANCE;
    }

    public Connection getConnection() {
        // Return an available connection
        synchronized (connections) {
            if (!connections.isEmpty()) {
                return connections.remove(0);
            }
            throw new RuntimeException("No connections available!");
        }
    }
}
```

---

### Problem 10: Stream Pipeline — Employee Analytics

```java
// Given a list of employees, write a SINGLE stream pipeline that:
// 1. Filters employees with salary > 50000
// 2. Groups by department
// 3. For each department, finds the highest paid employee's name

Map<String, Optional<String>> topPerDept = employees.stream()
    .filter(e -> e.getSalary() > 50000)
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.collectingAndThen(
            Collectors.maxBy(Comparator.comparingDouble(Employee::getSalary)),
            optEmp -> optEmp.map(Employee::getName)
        )
    ));
// {Engineering=Optional[Ananya], Marketing=Optional[Priya]}
```

---

## Tips for Java Interviews at Microsoft Specifically

### 1. Microsoft Values System Design Knowledge
When writing Java code in an interview, be ready to discuss:
- How would this scale to millions of users?
- What data structure would you use and why?
- What are the thread safety implications?

### 2. Know Collections Inside Out
Microsoft interviewers love asking about HashMap internals, ConcurrentHashMap, and when to
choose which collection. Be able to explain time complexity for every operation.

### 3. Streams Fluency Is Expected
You should be able to solve any collection manipulation problem using streams in under 2 minutes.
Practice the 15 stream problems from Week 6 until they are second nature.

### 4. Concurrency Questions Are Common
Microsoft builds highly concurrent systems (Azure, Office 365). Expect at least one question on:
- Thread safety
- Deadlock prevention
- Producer-consumer
- ExecutorService

### 5. Clean Code Matters
Microsoft interviewers notice:
- Meaningful variable names (not `a`, `b`, `temp`)
- Proper error handling (not ignoring exceptions)
- Edge case handling (null inputs, empty collections)
- Using the right access modifiers (not everything public)

### 6. The "Tell Me About" Pattern
Microsoft often asks behavioral questions mixed with technical:
- "Tell me about a time you had to handle concurrency in your code"
- "Describe a situation where choosing the wrong data structure caused problems"

Have real examples ready that demonstrate Java knowledge in practice.

### 7. Prepare for Live Coding
Microsoft uses live coding (shared screen or whiteboard). Practice:
- Writing compilable Java code without an IDE
- Explaining your thought process out loud
- Handling follow-up modifications ("Now make this thread-safe")

### 8. Exception Handling Design
Microsoft values robust error handling. In your interview code:
- Use specific exceptions, not generic `Exception`
- Always clean up resources (try-with-resources)
- Validate inputs at method entry
- Return meaningful error messages

### 9. Testing Mindset
Even if not asked to write tests, mention:
- "I would add a unit test for this edge case"
- "This would need integration testing because..."
- "The thread safety can be verified by..."

### 10. Know Java 8+ Features Well
Modern Microsoft teams use Java 8+ extensively. Demonstrate fluency with:
- Streams and lambdas (most important)
- Optional for null safety
- CompletableFuture for async operations
- The new Date/Time API

---

## Quick Reference: Time Complexity Cheat Sheet

| Operation | ArrayList | LinkedList | HashMap | TreeMap | HashSet | TreeSet |
|-----------|-----------|------------|---------|---------|---------|---------|
| Access by index | O(1) | O(n) | N/A | N/A | N/A | N/A |
| Search | O(n) | O(n) | O(1) | O(log n) | O(1) | O(log n) |
| Insert at end | O(1)* | O(1) | O(1)* | O(log n) | O(1)* | O(log n) |
| Insert at middle | O(n) | O(1)** | N/A | N/A | N/A | N/A |
| Delete | O(n) | O(1)** | O(1)* | O(log n) | O(1)* | O(log n) |

\* Amortized — occasionally O(n) when resizing
\** O(1) only if you already have a reference to the node; O(n) to find it first
