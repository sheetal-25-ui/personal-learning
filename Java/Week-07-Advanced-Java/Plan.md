# Week 7: Advanced Java — Generics, Design Patterns, Memory, and JVM

## Why This Week Matters

This week covers topics that separate "I know Java syntax" from "I understand Java deeply."
Microsoft and Anthropic interviewers use these topics to assess whether you have a solid
foundation or just surface-level knowledge. Generics, design patterns, and memory management
come up frequently in system design and coding rounds.

---

## 1. Generics — Deep Dive

### What is it?
Generics allow you to write classes and methods that work with ANY type, while still being
type-safe. Instead of writing a separate Box class for Integer, String, and Employee, you write
ONE generic Box<T> class that works for ALL types.

### Real-life Analogy
Think of a delivery box from Flipkart. The box itself does not care what is inside — it could
be a phone, a book, or shoes. But once you LABEL the box "Phone," everyone knows what is inside
without opening it. Generics are that label.

### Why does it exist? What problem does it solve?

Before generics (Java 1.4 and earlier), collections stored `Object` — meaning ANYTHING could
go in, and you had to cast everything coming out:

```java
// WITHOUT Generics — dangerous!
List list = new ArrayList();
list.add("Hello");
list.add(42);       // No error! String and Integer mixed together
list.add(true);     // Boolean too! Total chaos.

String s = (String) list.get(1); // RUNTIME ERROR! ClassCastException — 42 is not a String
// The compiler did not catch this. You only find out when the program crashes.
```

```java
// WITH Generics — safe!
List<String> list = new ArrayList<>();
list.add("Hello");
list.add(42);       // COMPILE ERROR! Cannot add Integer to List<String>
// The compiler catches the mistake BEFORE you run the program.

String s = list.get(0); // No casting needed. Compiler knows it is a String.
```

### Python Comparison
```python
# Python lists can hold any type — no enforcement
my_list = ["hello", 42, True]  # No error, but also no safety

# Python 3.9+ has type hints, but they are NOT enforced at runtime
my_list: list[str] = ["hello"]
my_list.append(42)  # Python does NOT stop this at runtime!
```

Java generics are ENFORCED by the compiler. Python type hints are just suggestions.

### Generic Classes

```java
// A box that can hold any type
class Box<T> {     // T is a "type parameter" — a placeholder for a real type
    private T item;

    public void put(T item) {
        this.item = item;
    }

    public T get() {
        return item;
    }
}

public class GenericDemo {
    public static void main(String[] args) {
        // Box for Strings
        Box<String> nameBox = new Box<>();
        nameBox.put("Sheetal");
        String name = nameBox.get(); // No casting! Compiler knows it is String.

        // Box for Integers
        Box<Integer> numberBox = new Box<>();
        numberBox.put(42);
        int number = numberBox.get();

        // Box for a custom type
        Box<Employee> empBox = new Box<>();
        empBox.put(new Employee("Aarav", "Engineering"));
        Employee emp = empBox.get();
    }
}
```

### Multiple Type Parameters

```java
// A Pair that holds two different types
class Pair<K, V> {
    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() { return key; }
    public V getValue() { return value; }
}

// Usage
Pair<String, Integer> score = new Pair<>("Math", 95);
Pair<String, List<String>> deptEmployees = new Pair<>("Engineering", List.of("Aarav", "Rohan"));
```

### Generic Methods

```java
class Utility {
    // Generic method — the <T> before return type declares T for this method
    public static <T> void printArray(T[] array) {
        for (T item : array) {
            System.out.print(item + " ");
        }
        System.out.println();
    }

    // Generic method with return type
    public static <T extends Comparable<T>> T findMax(T[] array) {
        T max = array[0];
        for (T item : array) {
            if (item.compareTo(max) > 0) {
                max = item;
            }
        }
        return max;
    }
}

// Usage
Integer[] nums = {3, 1, 4, 1, 5};
String[] names = {"Priya", "Aarav", "Meera"};

Utility.printArray(nums);   // 3 1 4 1 5
Utility.printArray(names);  // Priya Aarav Meera

System.out.println(Utility.findMax(nums));   // 5
System.out.println(Utility.findMax(names));  // Priya (alphabetically last)
```

### Bounded Type Parameters

Sometimes you want generics but with constraints. You do not want ANY type — you want types
that have certain capabilities.

```java
// T must be a type that implements Comparable
// Without this bound, you cannot call compareTo() on T
class SortedBox<T extends Comparable<T>> {
    private List<T> items = new ArrayList<>();

    public void add(T item) {
        items.add(item);
        Collections.sort(items); // This works because T is Comparable
    }

    public T getSmallest() {
        return items.get(0);
    }
}

// Multiple bounds
class Processor<T extends Serializable & Comparable<T>> {
    // T must be BOTH Serializable AND Comparable
}
```

**Real-life analogy:** Like saying "I need a delivery person who has BOTH a driving license AND
knows how to use Google Maps." Bounded generics put requirements on the type parameter.

### Wildcards: ?, ? extends T, ? super T

This is a TRICKY interview topic. Pay close attention.

#### ? — Unknown type (unbounded wildcard)

```java
// "I accept a list of ANYTHING — I just want to read from it"
public static void printList(List<?> list) {
    for (Object item : list) {
        System.out.println(item);
    }
}

printList(Arrays.asList(1, 2, 3));       // OK
printList(Arrays.asList("a", "b", "c")); // OK
```

#### ? extends T — Upper bounded wildcard (READ-only)

"I accept a list of T or any SUBTYPE of T."

```java
// "I accept a list of any type that IS a Number (Integer, Double, Long...)"
public static double sum(List<? extends Number> list) {
    double total = 0;
    for (Number num : list) {
        total += num.doubleValue();
    }
    return total;
}

sum(Arrays.asList(1, 2, 3));         // List<Integer> — OK
sum(Arrays.asList(1.5, 2.5, 3.5));   // List<Double> — OK
// sum(Arrays.asList("a", "b"));     // List<String> — COMPILE ERROR! String is not a Number
```

**Key:** With `? extends T`, you can READ from the list but CANNOT ADD to it (except null).
The compiler does not know the exact type, so it cannot verify what you are adding is safe.

#### ? super T — Lower bounded wildcard (WRITE-friendly)

"I accept a list of T or any SUPERTYPE of T."

```java
// "I accept a list that can hold Integers — could be List<Integer>, List<Number>, List<Object>"
public static void addNumbers(List<? super Integer> list) {
    list.add(1);    // OK — we know the list can hold Integers
    list.add(2);
    list.add(3);
}

List<Number> numbers = new ArrayList<>();
addNumbers(numbers); // List<Number> can hold Integer
```

### PECS Rule (Producer Extends, Consumer Super)

This is THE rule to remember for wildcards:

- If the generic structure is a **producer** (you READ from it) → use `? extends T`
- If the generic structure is a **consumer** (you WRITE to it) → use `? super T`

**Real-life analogy:**
- **Producer (extends):** A Swiggy kitchen PRODUCES food. You can TAKE food from it
  (read). You do not put food back into the kitchen.
- **Consumer (super):** Your stomach CONSUMES food. You can PUT food into it (write).

### Type Erasure — What Happens at Runtime (Interview Question!)

Generics in Java are a COMPILE-TIME feature only. At runtime, all generic type information is
erased. This is called type erasure.

```java
// What you write:
List<String> strings = new ArrayList<>();
List<Integer> numbers = new ArrayList<>();

// What the JVM sees at runtime (after erasure):
List strings = new ArrayList(); // Just "List", no String
List numbers = new ArrayList(); // Just "List", no Integer

// This means:
System.out.println(strings.getClass() == numbers.getClass()); // TRUE!
// At runtime, both are just "ArrayList" — the generic type is gone.
```

**Why does Java do this?** Backward compatibility. When generics were added in Java 5, they
needed to work with all the existing non-generic code from Java 1-4. Type erasure made this
possible.

**Consequences:**
1. You cannot do `new T()` — the type T does not exist at runtime
2. You cannot do `instanceof List<String>` — only `instanceof List`
3. You cannot create a generic array: `new T[10]` is illegal

### Python Comparison
Python's type hints are also erased at runtime:
```python
x: list[str] = ["hello"]
# At runtime, Python has NO idea that x is supposed to hold only strings
```

But Java's erasure is more impactful because Java's type checking is strict at compile time.

---

## 2. Annotations

### What is it?
Annotations are metadata tags you attach to code. They do not change what the code does directly,
but tools, frameworks, and the compiler can read them and take action.

### Real-life Analogy
Think of stickers on a package. A "FRAGILE" sticker does not change the contents of the package,
but it tells the delivery person to handle it carefully. Annotations are stickers for your code.

### Built-in Annotations

```java
// @Override — tells the compiler "I intend to override a parent method"
class Dog extends Animal {
    @Override
    public void speak() {  // Compiler checks that this actually overrides something
        System.out.println("Woof!");
    }

    @Override
    public void spek() {   // COMPILE ERROR! No method "spek" in parent. Typo caught!
    }
}

// @Deprecated — marks something as "do not use, will be removed"
class OldAPI {
    @Deprecated
    public void oldMethod() {
        // This still works, but users get a warning
    }
}

// @SuppressWarnings — tell compiler to stop showing a specific warning
@SuppressWarnings("unchecked")  // I know about this warning, I accept the risk
List rawList = new ArrayList();

// @FunctionalInterface — ensures the interface has exactly one abstract method
@FunctionalInterface
interface Calculator {
    int calculate(int a, int b);
    // If you add another abstract method, compiler will complain
}
```

### Custom Annotations

```java
import java.lang.annotation.*;

// Define a custom annotation
@Retention(RetentionPolicy.RUNTIME)    // Available at runtime (for reflection)
@Target(ElementType.METHOD)             // Can only be used on methods
public @interface LogExecution {
    String value() default "INFO";      // Optional parameter with default
}

// Use it
class OrderService {
    @LogExecution("DEBUG")
    public void placeOrder(String orderId) {
        // ... business logic
    }
}
```

### Annotations in Spring (Preview)

You will see these constantly when working with Spring Boot (used at Microsoft and most Java
companies):

```java
@RestController               // "This class handles HTTP requests"
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired                 // "Inject this dependency automatically"
    private OrderService orderService;

    @GetMapping("/{id}")       // "Handle GET requests to /api/orders/{id}"
    public Order getOrder(@PathVariable String id) {
        return orderService.findById(id);
    }

    @PostMapping               // "Handle POST requests to /api/orders"
    public Order createOrder(@RequestBody Order order) {
        return orderService.create(order);
    }
}
```

---

## 3. Reflection API

### What is it?
Reflection lets you examine and modify the structure of a class at RUNTIME — inspect its methods,
fields, constructors, and even access private members.

### Real-life Analogy
Normally, you use a car by driving it (normal API). Reflection is like opening the hood and
poking at the engine directly. You can see every part, modify things that are not meant to be
modified. Powerful but dangerous.

### Why does it exist?
Frameworks like Spring, Hibernate, and JUnit use reflection heavily. When Spring sees
`@Autowired`, it uses reflection to find the field's type, create an instance, and set the field
value — all without you writing the wiring code.

```java
import java.lang.reflect.*;

class Employee {
    private String name;
    private double salary;

    public Employee(String name, double salary) {
        this.name = name;
        this.salary = salary;
    }

    private void secretMethod() {
        System.out.println("This is private!");
    }

    @Override
    public String toString() {
        return name + ": " + salary;
    }
}

public class ReflectionDemo {
    public static void main(String[] args) throws Exception {
        // Get the Class object
        Class<?> clazz = Employee.class;

        // Inspect all fields (including private ones)
        System.out.println("Fields:");
        for (Field field : clazz.getDeclaredFields()) {
            System.out.println("  " + field.getType().getSimpleName()
                + " " + field.getName());
        }
        // Output:
        //   String name
        //   double salary

        // Inspect all methods
        System.out.println("Methods:");
        for (Method method : clazz.getDeclaredMethods()) {
            System.out.println("  " + method.getName());
        }

        // Create an instance dynamically
        Constructor<?> constructor = clazz.getConstructor(String.class, double.class);
        Object emp = constructor.newInstance("Aarav", 85000);
        System.out.println(emp); // Aarav: 85000.0

        // Access private field!
        Field nameField = clazz.getDeclaredField("name");
        nameField.setAccessible(true);  // Override private access
        nameField.set(emp, "Modified Aarav");
        System.out.println(emp); // Modified Aarav: 85000.0

        // Call private method!
        Method secret = clazz.getDeclaredMethod("secretMethod");
        secret.setAccessible(true);
        secret.invoke(emp); // "This is private!"
    }
}
```

### Python Comparison
```python
# Python has built-in introspection (no special API needed)
class Employee:
    def __init__(self, name, salary):
        self.name = name
        self.salary = salary

emp = Employee("Aarav", 85000)
print(dir(emp))           # List all attributes and methods
print(vars(emp))          # {'name': 'Aarav', 'salary': 85000}
emp.name = "Modified"     # Python has no true private fields
```

Java reflection is more verbose because Java takes access control (private/public) seriously.
Python does not enforce access control, so introspection is trivially easy.

---

## 4. Design Patterns in Java

Design patterns are proven solutions to common software design problems. Interviewers love
these because they test your ability to write clean, maintainable code.

### Singleton Pattern — Only One Instance

#### What is it?
Ensures a class has only ONE instance throughout the application.

#### Real-life Analogy
India has only ONE President. You cannot create a second one. When anyone needs to talk to the
President, they go to the SAME person. That is a Singleton.

#### Why does it exist?
Some things should have only one instance: database connection pool, configuration manager,
logging service. Creating multiple instances would waste resources or cause conflicts.

#### Thread-Safe Singleton (4 Ways — Know All for Interviews)

**Way 1: Eager Initialization (Simplest)**

```java
class DatabaseConnection {
    // Instance created at class loading time — thread-safe because JVM handles it
    private static final DatabaseConnection INSTANCE = new DatabaseConnection();

    private DatabaseConnection() {} // Private constructor — nobody else can create instances

    public static DatabaseConnection getInstance() {
        return INSTANCE;
    }
}
```
**Downside:** Instance is created even if nobody ever uses it (wastes memory if expensive).

**Way 2: Lazy with Double-Checked Locking**

```java
class DatabaseConnection {
    // volatile ensures all threads see the latest value
    private static volatile DatabaseConnection instance;

    private DatabaseConnection() {}

    public static DatabaseConnection getInstance() {
        if (instance == null) {                    // First check (no lock — fast)
            synchronized (DatabaseConnection.class) {
                if (instance == null) {            // Second check (with lock — safe)
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }
}
```
**Why double-checked?** The outer `if` avoids the synchronized block for 99.9% of calls (when
instance already exists). The inner `if` protects against two threads that both passed the outer
`if` before the instance was created.

**Way 3: Bill Pugh Singleton (Best for Most Cases)**

```java
class DatabaseConnection {
    private DatabaseConnection() {}

    // Inner static class is not loaded until getInstance() is called
    private static class Holder {
        private static final DatabaseConnection INSTANCE = new DatabaseConnection();
    }

    public static DatabaseConnection getInstance() {
        return Holder.INSTANCE;
    }
}
```
**Why is this best?** It is lazy (created only when needed), thread-safe (JVM handles class
loading), and requires no synchronization.

**Way 4: Enum Singleton (Recommended by Joshua Bloch, author of Effective Java)**

```java
enum DatabaseConnection {
    INSTANCE;

    private Connection conn;

    DatabaseConnection() {
        conn = createConnection();
    }

    public Connection getConnection() {
        return conn;
    }
}

// Usage
DatabaseConnection.INSTANCE.getConnection();
```
**Why enum?** Thread-safe by default, prevents reflection attacks, handles serialization
correctly. The simplest and most robust approach.

### Factory Pattern

#### What is it?
A method that creates objects without exposing the creation logic. The caller says "give me a
payment processor" and the factory decides which concrete class to create.

#### Real-life Analogy
When you pay on BookMyShow, you choose "UPI" or "Credit Card" or "Net Banking." BookMyShow
does not make you create the payment object yourself — a factory decides which processor to
give you based on your choice.

```java
// Step 1: Common interface
interface PaymentProcessor {
    void processPayment(double amount);
}

// Step 2: Concrete implementations
class UPIProcessor implements PaymentProcessor {
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing Rs. " + amount + " via UPI");
    }
}

class CreditCardProcessor implements PaymentProcessor {
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing Rs. " + amount + " via Credit Card");
    }
}

class NetBankingProcessor implements PaymentProcessor {
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing Rs. " + amount + " via Net Banking");
    }
}

// Step 3: Factory
class PaymentFactory {
    public static PaymentProcessor create(String type) {
        switch (type.toLowerCase()) {
            case "upi":         return new UPIProcessor();
            case "credit_card": return new CreditCardProcessor();
            case "net_banking": return new NetBankingProcessor();
            default: throw new IllegalArgumentException("Unknown payment type: " + type);
        }
    }
}

// Step 4: Usage
PaymentProcessor processor = PaymentFactory.create("upi");
processor.processPayment(499.00);
// Output: Processing Rs. 499.0 via UPI
```

### Builder Pattern

#### What is it?
A way to construct complex objects step by step, instead of using a constructor with many
parameters.

#### Why Java Needs It More Than Python

Python has named/keyword arguments:
```python
# Python — crystal clear which argument is which
employee = Employee(name="Aarav", dept="Engineering", salary=85000, city="Bangalore", age=28)
```

Java does NOT have named arguments:
```java
// Java — what does each parameter mean? Nobody knows without reading the constructor!
Employee emp = new Employee("Aarav", "Engineering", 85000, "Bangalore", 28, true, "Senior");
// Is 85000 the salary or the employee ID? Is true for isActive or isManager?
```

The Builder pattern solves this:

```java
class Employee {
    private final String name;
    private final String department;
    private final double salary;
    private final String city;
    private final int age;

    // Private constructor — only the Builder can create Employee
    private Employee(Builder builder) {
        this.name = builder.name;
        this.department = builder.department;
        this.salary = builder.salary;
        this.city = builder.city;
        this.age = builder.age;
    }

    // Static inner Builder class
    public static class Builder {
        private String name;        // Required
        private String department;  // Required
        private double salary;
        private String city;
        private int age;

        public Builder(String name, String department) {
            this.name = name;
            this.department = department;
        }

        public Builder salary(double salary) {
            this.salary = salary;
            return this;  // Return this for method chaining
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder age(int age) {
            this.age = age;
            return this;
        }

        public Employee build() {
            return new Employee(this);
        }
    }

    @Override
    public String toString() {
        return name + " | " + department + " | " + salary + " | " + city;
    }
}

// Usage — reads like English!
Employee emp = new Employee.Builder("Aarav", "Engineering")
    .salary(85000)
    .city("Bangalore")
    .age(28)
    .build();
```

### Observer Pattern

#### What is it?
One object (the "subject") notifies multiple other objects (the "observers") when its state
changes. Like a YouTube subscription — when a creator uploads a video, all subscribers get
notified.

```java
import java.util.*;

// Observer interface
interface StockObserver {
    void update(String stockName, double newPrice);
}

// Subject
class StockMarket {
    private Map<String, Double> prices = new HashMap<>();
    private List<StockObserver> observers = new ArrayList<>();

    public void addObserver(StockObserver observer) {
        observers.add(observer);
    }

    public void updatePrice(String stock, double price) {
        prices.put(stock, price);
        // Notify all observers
        for (StockObserver observer : observers) {
            observer.update(stock, price);
        }
    }
}

// Concrete observers
class MobileApp implements StockObserver {
    @Override
    public void update(String stockName, double newPrice) {
        System.out.println("[Mobile Notification] " + stockName + " is now Rs. " + newPrice);
    }
}

class EmailAlert implements StockObserver {
    @Override
    public void update(String stockName, double newPrice) {
        System.out.println("[Email Alert] " + stockName + " changed to Rs. " + newPrice);
    }
}

// Usage
StockMarket market = new StockMarket();
market.addObserver(new MobileApp());
market.addObserver(new EmailAlert());

market.updatePrice("RELIANCE", 2450.50);
// [Mobile Notification] RELIANCE is now Rs. 2450.5
// [Email Alert] RELIANCE changed to Rs. 2450.5
```

### Strategy Pattern with Lambdas

#### What is it?
Define a family of algorithms, put each in a separate class, and make them interchangeable.
Java 8 lambdas make this pattern MUCH cleaner.

```java
// Before Java 8: Needed separate classes for each strategy
// After Java 8: Just use lambdas!

import java.util.function.BiFunction;

class PricingEngine {
    private BiFunction<Double, Integer, Double> discountStrategy;

    public void setDiscountStrategy(BiFunction<Double, Integer, Double> strategy) {
        this.discountStrategy = strategy;
    }

    public double calculatePrice(double basePrice, int quantity) {
        return discountStrategy.apply(basePrice, quantity);
    }
}

// Usage
PricingEngine engine = new PricingEngine();

// Diwali Sale strategy
engine.setDiscountStrategy((price, qty) -> price * qty * 0.7); // 30% off

// Bulk order strategy
engine.setDiscountStrategy((price, qty) -> {
    if (qty > 100) return price * qty * 0.5;  // 50% off for bulk
    if (qty > 50)  return price * qty * 0.7;  // 30% off for medium
    return price * qty * 0.9;                  // 10% off for small
});

System.out.println(engine.calculatePrice(100, 200)); // 10000.0 (50% off)
```

---

## 5. Memory Management and Garbage Collection

### What is it?
Java automatically manages memory through Garbage Collection (GC). You create objects, and
the GC automatically frees memory when objects are no longer used. You do not manually
allocate/free memory like in C/C++.

### Stack vs Heap — Where Do Variables Live?

#### Stack

- Stores **local variables** and **method calls**
- Each thread has its OWN stack
- LIFO (Last In, First Out) — when a method finishes, its variables are removed
- Very fast access
- Limited size (usually a few MB)

#### Heap

- Stores **objects** and **instance variables**
- Shared by ALL threads
- Managed by Garbage Collector
- Larger size (can be GBs)
- Slower access than stack

```java
public void processOrder() {
    int orderId = 12345;                    // orderId is on the STACK
    String status = "PENDING";              // status reference is on STACK,
                                            // "PENDING" string object is on HEAP

    Order order = new Order(orderId);       // order reference is on STACK,
                                            // Order object is on HEAP

    // When processOrder() finishes:
    // - orderId, status, order (references) are removed from STACK
    // - The Order object on HEAP becomes eligible for GC
    //   (if nothing else references it)
}
```

**Real-life analogy:**
- **Stack** = Your desk. Small, organized, only holds what you are currently working on. When you
  finish a task, those papers are removed immediately.
- **Heap** = The warehouse. Huge, stores everything. A cleaning crew (GC) periodically removes
  stuff nobody needs anymore.

### Heap Generations

The heap is divided into generations based on object age:

```
HEAP MEMORY
+--------------------------------------------+
|  Young Generation                          |
|  +--------+  +---------+  +---------+     |
|  |  Eden  |  |Survivor1|  |Survivor2|     |
|  | (new   |  | (older) |  | (older) |     |
|  |objects)|  |         |  |         |     |
|  +--------+  +---------+  +---------+     |
+--------------------------------------------+
|  Old Generation (Tenured)                  |
|  (long-lived objects that survived many    |
|   GC cycles)                               |
+--------------------------------------------+
|  Metaspace (class metadata, not on heap)   |
+--------------------------------------------+
```

**How it works:**
1. New objects are created in **Eden** (Young Generation)
2. When Eden is full, a **Minor GC** runs. Surviving objects move to Survivor space
3. After surviving several Minor GCs, objects are promoted to **Old Generation**
4. When Old Generation is full, a **Major GC** (Full GC) runs — this is SLOW and causes pauses

**Real-life analogy:**
Think of a Flipkart warehouse:
- **Eden** = Incoming dock. New packages arrive here. Most are short-lived (picked up quickly).
- **Survivor** = Temporary storage. Packages that were not picked up go here.
- **Old Generation** = Long-term storage. Items that have been around for a while.
- **GC** = Warehouse cleanup crew. They check what is no longer needed and remove it.

### GC Algorithms (High Level)

| Algorithm | Description | Use Case |
|-----------|-------------|----------|
| Serial GC | Single thread, simple, stop-the-world | Small apps, development |
| Parallel GC | Multiple threads for GC, stop-the-world | Throughput-focused apps |
| G1 GC | Divides heap into regions, predictable pauses | Default since Java 9, general purpose |
| ZGC | Very low latency (< 10ms pauses) | Latency-sensitive apps (trading, real-time) |

### Memory Leaks in Java — Yes, They Happen!

Even with GC, you can leak memory if you keep references to objects you no longer need:

```java
// Memory leak example 1: Static collection that grows forever
class LeakyCache {
    // This map NEVER shrinks. Every cached item stays forever.
    private static Map<String, Object> cache = new HashMap<>();

    public static void put(String key, Object value) {
        cache.put(key, value);
    }
    // No method to remove items! Memory grows forever until OutOfMemoryError.
}

// Fix: Use WeakHashMap or add eviction logic
private static Map<String, Object> cache = new WeakHashMap<>();
// WeakHashMap allows GC to remove entries when keys are no longer referenced elsewhere.

// Memory leak example 2: Unclosed resources
public void readFile(String path) {
    InputStream stream = new FileInputStream(path);
    // If an exception happens here, stream is NEVER closed
    // Use try-with-resources instead
}

// Fix:
public void readFile(String path) {
    try (InputStream stream = new FileInputStream(path)) {
        // stream is automatically closed, even if exception occurs
    }
}

// Memory leak example 3: Listeners never unregistered
button.addActionListener(listener);
// If you never remove the listener, the button holds a reference to it FOREVER
```

### JVM Flags

```bash
# Set initial heap size to 256MB
java -Xms256m MyApp

# Set maximum heap size to 2GB
java -Xmx2g MyApp

# Use G1 garbage collector
java -XX:+UseG1GC MyApp

# Print GC activity (for debugging)
java -verbose:gc MyApp

# Common production setup
java -Xms512m -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 MyApp
```

### Python Comparison
```python
# Python uses reference counting + cycle collector
# No explicit heap management
# No generation tuning
# gc module for debugging:
import gc
gc.collect()  # Force garbage collection
```

Java gives you MUCH more control over memory management than Python. This is both a benefit
(tuning for performance) and a burden (more things to learn).

---

## 6. String Internals

### String Pool

```java
String s1 = "Hello";      // Created in String Pool
String s2 = "Hello";      // Reuses SAME object from String Pool
String s3 = new String("Hello"); // Creates NEW object on heap (NOT in pool)

System.out.println(s1 == s2);      // true  (same reference, same pool object)
System.out.println(s1 == s3);      // false (different objects!)
System.out.println(s1.equals(s3)); // true  (same content)

// Force s3 into the pool
String s4 = s3.intern();
System.out.println(s1 == s4);      // true (now points to pool object)
```

### String vs StringBuilder vs StringBuffer

| Feature | String | StringBuilder | StringBuffer |
|---------|--------|---------------|--------------|
| Mutability | Immutable | Mutable | Mutable |
| Thread-safe | Yes (immutable) | NO | YES (synchronized) |
| Performance | Slow for concatenation | Fast | Slower than StringBuilder |
| Use when | Value does not change | Single-threaded string building | Multi-threaded string building |

### Why String Concatenation in a Loop Is Terrible

```java
// BAD — creates a NEW String object every iteration!
String result = "";
for (int i = 0; i < 100000; i++) {
    result += i + ",";  // Each += creates a NEW String + copies all previous characters
}
// This creates 100,000 intermediate String objects. Very slow. Wastes memory.

// GOOD — uses a single mutable buffer
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 100000; i++) {
    sb.append(i).append(",");  // Modifies the same object. No copies.
}
String result = sb.toString();
// Much faster! Only creates ONE object.
```

**Real-life analogy:** Using String concatenation in a loop is like writing a letter, then
copying the ENTIRE letter to a new page just to add one more sentence. StringBuilder is like
just adding the sentence to the existing page.

---

## 7. Immutability

### What is it?
An immutable object cannot be modified after creation. Any "modification" creates a NEW object.

### Why Immutability Matters
1. **Thread safety for free** — If an object cannot change, multiple threads can read it
   simultaneously without any synchronization
2. **Safe as HashMap keys** — HashMap relies on the key's hashCode not changing
3. **Easier to reason about** — No unexpected changes from other parts of the code

### How to Create an Immutable Class

```java
// Rules: final class, final fields, no setters, deep copy in constructor and getters
public final class Money {
    private final String currency;
    private final double amount;
    private final List<String> tags;

    public Money(String currency, double amount, List<String> tags) {
        this.currency = currency;
        this.amount = amount;
        // DEEP COPY — do not store the original list (caller could modify it!)
        this.tags = new ArrayList<>(tags);
    }

    public String getCurrency() { return currency; }
    public double getAmount() { return amount; }

    // Return a COPY — caller cannot modify our internal list
    public List<String> getTags() {
        return Collections.unmodifiableList(tags);
    }

    // "Modification" creates a new object
    public Money add(double extra) {
        return new Money(this.currency, this.amount + extra, this.tags);
    }
}
```

### Java Records (Java 16+) — Easy Immutability

```java
// A record automatically creates: constructor, getters, equals, hashCode, toString
// All fields are final and private
public record Money(String currency, double amount) {
    // That is it! No boilerplate.
}

Money m = new Money("INR", 500);
System.out.println(m.currency()); // INR
System.out.println(m.amount());   // 500.0
System.out.println(m);            // Money[currency=INR, amount=500.0]
```

### Python Comparison
```python
# Python has no built-in way to make truly immutable objects
# Closest: namedtuple or frozen dataclass

from dataclasses import dataclass

@dataclass(frozen=True)  # Makes it immutable
class Money:
    currency: str
    amount: float
```

---

## 8. JVM Internals (High Level)

### How Java Code Executes

```
Your Code (.java)
      |
      v
Java Compiler (javac)
      |
      v
Bytecode (.class)       <-- Platform-independent intermediate code
      |
      v
JVM (Java Virtual Machine)
   |
   +-- ClassLoader       -- Loads .class files into memory
   |
   +-- Bytecode Verifier -- Checks bytecode is valid and safe
   |
   +-- Interpreter       -- Executes bytecode line by line (slow)
   |
   +-- JIT Compiler      -- Compiles hot code to native machine code (fast!)
   |
   v
Native Machine Code     <-- Runs on the actual CPU
```

### Why "Write Once, Run Anywhere" Works

Your Java code compiles to **bytecode**, not native machine code. Bytecode is platform-
independent — the same .class file runs on Windows, Mac, and Linux. Each platform has its own
JVM that translates bytecode to native code.

**Real-life analogy:** English is like bytecode — it is understood everywhere. Each country's
JVM is like a local translator that converts English into the local language (machine code).

### ClassLoader

The ClassLoader loads .class files into the JVM. There are three levels:

1. **Bootstrap ClassLoader** — Loads core Java classes (java.lang, java.util)
2. **Extension ClassLoader** — Loads extension classes (javax.*)
3. **Application ClassLoader** — Loads YOUR classes from the classpath

### JIT (Just-In-Time) Compilation

The JVM starts by INTERPRETING bytecode (slow, like reading instructions one at a time). When
it detects "hot" code (methods called many times), the JIT compiler converts that bytecode
directly to native machine code. After JIT compilation, that code runs as fast as C/C++.

This is why Java is SLOW to start (interpreting + JIT warmup) but FAST once warmed up.

**Python comparison:** Python is interpreted and does NOT have JIT by default (CPython). That
is one reason Java is much faster than Python for computation-heavy tasks. (PyPy is a Python
implementation with JIT, but it is not the standard.)

---

## Common Mistakes

1. **Using raw types instead of generics** — `List` instead of `List<String>`. Always use
   generics for type safety.

2. **Not understanding type erasure** — Trying to do `if (obj instanceof List<String>)` which
   does not work at runtime.

3. **Making Singleton not thread-safe** — Using simple lazy initialization without
   synchronization in a multi-threaded environment.

4. **String concatenation in loops** — Use StringBuilder. This is a common performance problem.

5. **Not making defensive copies in immutable classes** — Storing a reference to a mutable
   collection passed to the constructor.

6. **Overusing reflection** — Reflection is slow and bypasses compile-time checks. Use it only
   when necessary (frameworks, not application code).

---

## Practice Exercises

### Exercise 1: Generic Pair and Triple
Create generic classes `Pair<A, B>` and `Triple<A, B, C>`. Add a static method
`Pair.swap(Pair<A,B>)` that returns `Pair<B,A>`. Test with different type combinations.

### Exercise 2: Builder for a BookMyShow Booking
Create a `MovieBooking` class with fields: movieName, theater, city, date, seats (list),
snacksIncluded (boolean), totalPrice. Use the Builder pattern. Make the built object immutable.

### Exercise 3: Payment Factory with Strategy
Combine Factory and Strategy patterns: Create a PaymentFactory that creates different payment
processors, each with their own fee calculation strategy (UPI = 0% fee, Credit Card = 2% fee,
International Card = 3.5% fee + currency conversion). Use lambdas for the strategies.

### Exercise 4: Observer for Ola Ride Tracking
Create an Observer pattern for Ola ride tracking. The `Ride` subject notifies observers
(CustomerApp, DriverApp, SupportDashboard) when ride status changes (REQUESTED, ACCEPTED,
EN_ROUTE, ARRIVED, IN_PROGRESS, COMPLETED).

### Exercise 5: Memory Analysis
Write a program that creates 1 million objects in a loop with `-verbose:gc` flag enabled.
Observe how GC runs. Then modify the program to reuse objects (object pooling) and compare
GC behavior.
