# Week 6: Java 8 Features — The Game Changer

## Why This Week Matters

Java 8 (released 2014) was the BIGGEST update in Java's history. Before Java 8, Java felt old
and verbose compared to Python, Ruby, and JavaScript. Java 8 added lambdas, streams, and
functional programming features that made Java feel modern again.

At Microsoft and Anthropic interviews, **Streams API questions are asked in almost every Java
round.** They test whether you can think in terms of data transformations rather than imperative
loops.

---

## 1. Lambda Expressions

### What is it?
A lambda expression is a short way to write a small, unnamed function. Instead of creating a
whole class with a method, you just write the logic inline.

### Real-life Analogy
Think of ordering on Swiggy. You do not need to know the chef's name, qualifications, or
restaurant address. You just say "I want paneer butter masala." The system finds someone to
make it. A lambda is the same — you just say WHAT to do, without all the ceremony of creating
a named class and method.

### Why does it exist? What problem does it solve?

Before Java 8, even simple tasks required writing a LOT of boilerplate code. Look at this
nightmare:

```java
// BEFORE Java 8: Sorting a list of strings by length
// You had to create an anonymous inner class. SO UGLY.
List<String> cities = Arrays.asList("Mumbai", "Delhi", "Goa", "Chennai", "Kolkata");

Collections.sort(cities, new Comparator<String>() {
    @Override
    public int compare(String a, String b) {
        return Integer.compare(a.length(), b.length());
    }
});
// That is 6 lines of code just to say "sort by length"!
```

```java
// AFTER Java 8: Same thing with a lambda
List<String> cities = Arrays.asList("Mumbai", "Delhi", "Goa", "Chennai", "Kolkata");

cities.sort((a, b) -> Integer.compare(a.length(), b.length()));
// ONE line. Clean. Readable. Beautiful.
```

### Lambda Syntax

```
(parameters) -> expression                    // Single expression
(parameters) -> { statements; return value; } // Multiple statements
```

**Examples:**

```java
// No parameters
Runnable task = () -> System.out.println("Hello!");

// One parameter (parentheses optional for single param)
Consumer<String> greet = name -> System.out.println("Hello, " + name);

// Two parameters
Comparator<Integer> compare = (a, b) -> a - b;

// Multiple statements — need curly braces and return
Function<Integer, String> converter = (num) -> {
    if (num > 0) return "Positive";
    else if (num < 0) return "Negative";
    else return "Zero";
};
```

### Python Comparison
```python
# Python lambda — limited to single expression
sort_key = lambda s: len(s)
cities.sort(key=lambda s: len(s))

# Python supports full functions as first-class citizens
def my_compare(a, b):
    return len(a) - len(b)
```

Java lambdas are more powerful than Python lambdas because Java lambdas can have multiple
statements (with curly braces). Python lambdas are limited to a single expression.

---

## 2. Functional Interfaces

### What is it?
A functional interface is an interface with EXACTLY ONE abstract method. Lambdas can only be
used where a functional interface is expected.

### Why does it exist?
Java needed a type system to support lambdas. A lambda needs to "become" something with a type.
That type is a functional interface. The `@FunctionalInterface` annotation tells the compiler
to enforce the single-method rule.

```java
@FunctionalInterface
interface MathOperation {
    int operate(int a, int b);
}

public class FunctionalInterfaceDemo {
    public static void main(String[] args) {
        // Lambda implements the single method of the interface
        MathOperation add = (a, b) -> a + b;
        MathOperation multiply = (a, b) -> a * b;

        System.out.println(add.operate(10, 20));       // 30
        System.out.println(multiply.operate(10, 20));   // 200
    }
}
```

### Built-in Functional Interfaces (java.util.function)

Java provides many ready-made functional interfaces so you rarely need to create your own:

#### Predicate<T> — Tests a condition, returns boolean

```java
import java.util.function.Predicate;

// "Is this number even?"
Predicate<Integer> isEven = n -> n % 2 == 0;
System.out.println(isEven.test(4));  // true
System.out.println(isEven.test(7));  // false

// Real example: Filter Zomato restaurants that are open
Predicate<Restaurant> isOpen = restaurant -> restaurant.isOpen();

// Combine predicates
Predicate<Integer> isPositive = n -> n > 0;
Predicate<Integer> isPositiveAndEven = isPositive.and(isEven);
System.out.println(isPositiveAndEven.test(4));   // true
System.out.println(isPositiveAndEven.test(-4));  // false
```

**Python comparison:** Like `filter(lambda x: x % 2 == 0, numbers)`

#### Function<T, R> — Takes T, returns R (transforms data)

```java
import java.util.function.Function;

// Convert a name to its length
Function<String, Integer> nameLength = name -> name.length();
System.out.println(nameLength.apply("Flipkart"));  // 8

// Chain functions
Function<String, String> toUpper = s -> s.toUpperCase();
Function<String, String> addStars = s -> "*** " + s + " ***";
Function<String, String> formatted = toUpper.andThen(addStars);
System.out.println(formatted.apply("sale"));  // *** SALE ***
```

**Python comparison:** Like `map(lambda x: len(x), names)`

#### Consumer<T> — Takes T, returns nothing (performs an action)

```java
import java.util.function.Consumer;

// Print a formatted message
Consumer<String> printGreeting = name -> System.out.println("Welcome, " + name + "!");
printGreeting.accept("Sheetal");  // Welcome, Sheetal!

// Chain consumers
Consumer<String> logToConsole = msg -> System.out.println("[LOG] " + msg);
Consumer<String> logToFile = msg -> writeToFile(msg); // hypothetical
Consumer<String> logBoth = logToConsole.andThen(logToFile);
```

**Python comparison:** Like `for item in list: print(item)` — the function is for side effects.

#### Supplier<T> — Takes nothing, returns T (produces a value)

```java
import java.util.function.Supplier;

// Generate a random OTP
Supplier<Integer> otpGenerator = () -> (int) (Math.random() * 9000) + 1000;
System.out.println("Your OTP is: " + otpGenerator.get());  // e.g., 5832

// Lazy initialization
Supplier<DatabaseConnection> dbConnector = () -> new DatabaseConnection("mongodb://...");
// Connection is NOT created until you call dbConnector.get()
```

**Python comparison:** Like a factory function: `def make_otp(): return random.randint(1000, 9999)`

### Summary Table

| Interface | Method | Input | Output | Use Case |
|-----------|--------|-------|--------|----------|
| Predicate<T> | test(T) | T | boolean | Filtering, conditions |
| Function<T,R> | apply(T) | T | R | Transforming data |
| Consumer<T> | accept(T) | T | void | Actions, side effects |
| Supplier<T> | get() | nothing | T | Producing values |
| BiFunction<T,U,R> | apply(T,U) | T, U | R | Two-input transform |
| UnaryOperator<T> | apply(T) | T | T | Same type in and out |
| BinaryOperator<T> | apply(T,T) | T, T | T | Combining two values |

---

## 3. Method References — The :: Operator

### What is it?
A method reference is an even shorter way to write a lambda when the lambda just calls an
existing method.

### Why does it exist?
If your lambda is literally just `x -> System.out.println(x)`, why write the wrapping? Just
point directly to the method: `System.out::println`.

### Types of Method References

```java
List<String> cities = Arrays.asList("Mumbai", "Delhi", "Goa", "Chennai");

// 1. Reference to a static method — Class::staticMethod
// Lambda version:
cities.stream().map(s -> Integer.parseInt(s));
// Method reference version:
List<String> numbers = Arrays.asList("1", "2", "3");
numbers.stream().map(Integer::parseInt);

// 2. Reference to an instance method on an object — object::instanceMethod
// Lambda version:
cities.forEach(city -> System.out.println(city));
// Method reference version:
cities.forEach(System.out::println);

// 3. Reference to an instance method on the argument — Class::instanceMethod
// Lambda version:
cities.stream().map(s -> s.toUpperCase());
// Method reference version:
cities.stream().map(String::toUpperCase);

// 4. Reference to a constructor — Class::new
// Lambda version:
cities.stream().map(s -> new StringBuilder(s));
// Method reference version:
cities.stream().map(StringBuilder::new);
```

### When to Use Method References
**Rule of thumb:** If your lambda is just passing its argument to a single method call, use a
method reference. If the lambda does anything else (comparison, calculation, multiple steps),
keep the lambda.

```java
// USE method reference — lambda just delegates
names.forEach(System.out::println);

// KEEP lambda — it does a calculation
names.sort((a, b) -> a.length() - b.length());
```

---

## 4. Streams API — THE Most Important Java 8 Feature

### What is it?
A Stream is a pipeline for processing collections of data. Instead of writing loops, you
describe WHAT transformations you want, and the Stream does it.

### Real-life Analogy: Factory Assembly Line

Think of a Maruti Suzuki car factory:
1. **Raw steel arrives** (data source — your list)
2. **Cut to shape** (map — transform each piece)
3. **Remove defective pieces** (filter — keep only what matches)
4. **Sort by size** (sorted — order them)
5. **Count the output** (terminal operation — get the result)

Each station does ONE thing and passes the result to the next station. That is a Stream pipeline.

### Why does it exist?

Before Streams, processing a collection meant writing loops with lots of temporary variables:

```java
// BEFORE: Find names of employees with salary > 50000, sorted alphabetically
List<String> result = new ArrayList<>();
for (Employee emp : employees) {
    if (emp.getSalary() > 50000) {
        result.add(emp.getName());
    }
}
Collections.sort(result);
// 6 lines, mutable state, easy to get wrong
```

```java
// AFTER: Same thing with Streams
List<String> result = employees.stream()
    .filter(emp -> emp.getSalary() > 50000)
    .map(Employee::getName)
    .sorted()
    .collect(Collectors.toList());
// Reads like English: filter by salary, get names, sort, collect
```

### Python Comparison
```python
# Python list comprehension — similar idea
result = sorted([emp.name for emp in employees if emp.salary > 50000])

# Python also has map/filter functions
result = list(filter(lambda e: e.salary > 50000, employees))
```

Streams are Java's answer to Python's list comprehensions and functional tools, but more
powerful because they support lazy evaluation and parallelism.

### Creating Streams

```java
// From a List
List<String> names = Arrays.asList("Aarav", "Priya", "Rohan");
Stream<String> stream1 = names.stream();

// From an array
String[] arr = {"a", "b", "c"};
Stream<String> stream2 = Arrays.stream(arr);

// Using Stream.of()
Stream<Integer> stream3 = Stream.of(1, 2, 3, 4, 5);

// Using Stream.generate() — infinite stream!
Stream<Double> randoms = Stream.generate(Math::random).limit(5);

// Using Stream.iterate()
Stream<Integer> evens = Stream.iterate(0, n -> n + 2).limit(10);
// 0, 2, 4, 6, 8, 10, 12, 14, 16, 18
```

---

### Intermediate Operations (return a Stream — can be chained)

These are LAZY — they do not execute until a terminal operation is called.

#### filter() — Keep elements that match a condition

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

// Keep only even numbers
List<Integer> evens = numbers.stream()
    .filter(n -> n % 2 == 0)
    .collect(Collectors.toList());
// [2, 4, 6, 8, 10]

// Real-world: Zomato — filter restaurants that deliver to your area
List<Restaurant> nearby = restaurants.stream()
    .filter(r -> r.getCity().equals("Bangalore"))
    .filter(r -> r.getRating() >= 4.0)
    .filter(Restaurant::isOpen)
    .collect(Collectors.toList());
```

#### map() — Transform each element

```java
List<String> names = Arrays.asList("aarav", "priya", "rohan");

// Convert each name to uppercase
List<String> upper = names.stream()
    .map(String::toUpperCase)
    .collect(Collectors.toList());
// [AARAV, PRIYA, ROHAN]

// Extract a field from objects
List<String> employeeNames = employees.stream()
    .map(Employee::getName)
    .collect(Collectors.toList());

// Transform: get the length of each name
List<Integer> lengths = names.stream()
    .map(String::length)
    .collect(Collectors.toList());
// [5, 5, 5]
```

#### flatMap() — Flatten nested structures

```java
// Each customer has a list of orders. We want ALL orders in one flat list.
List<List<String>> ordersByCustomer = Arrays.asList(
    Arrays.asList("Pizza", "Burger"),
    Arrays.asList("Dosa", "Idli", "Vada"),
    Arrays.asList("Biryani")
);

// WITHOUT flatMap — you get List<List<String>>
// WITH flatMap — you get List<String>
List<String> allOrders = ordersByCustomer.stream()
    .flatMap(List::stream)  // Flatten: each inner list becomes a stream of elements
    .collect(Collectors.toList());
// [Pizza, Burger, Dosa, Idli, Vada, Biryani]
```

**map vs flatMap:**
- `map()` — one input produces one output. Like: name -> name.toUpperCase()
- `flatMap()` — one input produces MULTIPLE outputs (a stream). Like: customer -> customer.getOrders()

**Python comparison:**
```python
# Python equivalent of flatMap
all_orders = [order for orders in orders_by_customer for order in orders]
```

#### sorted() — Sort elements

```java
List<String> cities = Arrays.asList("Mumbai", "Delhi", "Goa", "Chennai");

// Natural order (alphabetical for strings)
List<String> sorted1 = cities.stream()
    .sorted()
    .collect(Collectors.toList());
// [Chennai, Delhi, Goa, Mumbai]

// Custom comparator — sort by length
List<String> sorted2 = cities.stream()
    .sorted(Comparator.comparingInt(String::length))
    .collect(Collectors.toList());
// [Goa, Delhi, Mumbai, Chennai]

// Reverse sort
List<String> sorted3 = cities.stream()
    .sorted(Comparator.reverseOrder())
    .collect(Collectors.toList());
// [Mumbai, Goa, Delhi, Chennai]
```

#### distinct() — Remove duplicates

```java
List<Integer> numbers = Arrays.asList(1, 2, 2, 3, 3, 3, 4);
List<Integer> unique = numbers.stream()
    .distinct()
    .collect(Collectors.toList());
// [1, 2, 3, 4]
```

#### peek() — Look at elements without changing them (for debugging)

```java
List<String> result = names.stream()
    .filter(n -> n.length() > 3)
    .peek(n -> System.out.println("After filter: " + n))  // Debug
    .map(String::toUpperCase)
    .peek(n -> System.out.println("After map: " + n))      // Debug
    .collect(Collectors.toList());
```

#### limit() and skip()

```java
// Get first 3 elements
List<Integer> first3 = numbers.stream().limit(3).collect(Collectors.toList());

// Skip first 2, take the rest
List<Integer> afterSkip = numbers.stream().skip(2).collect(Collectors.toList());

// Pagination: page 2, page size 5
List<Employee> page2 = employees.stream()
    .skip(5)    // skip first page
    .limit(5)   // take second page
    .collect(Collectors.toList());
```

---

### Terminal Operations (produce a result — end the pipeline)

#### collect() — Gather results into a collection

```java
// To List
List<String> list = stream.collect(Collectors.toList());

// To Set (removes duplicates)
Set<String> set = stream.collect(Collectors.toSet());

// To Map
Map<String, Integer> nameToAge = employees.stream()
    .collect(Collectors.toMap(
        Employee::getName,    // key
        Employee::getAge      // value
    ));
```

#### forEach() — Do something with each element

```java
names.stream().forEach(System.out::println);
// Or simply:
names.forEach(System.out::println);
```

#### reduce() — Combine all elements into one result

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

// Sum all numbers
int sum = numbers.stream()
    .reduce(0, (a, b) -> a + b);
// 15

// Same thing using method reference
int sum2 = numbers.stream()
    .reduce(0, Integer::sum);

// Find the longest string
Optional<String> longest = names.stream()
    .reduce((a, b) -> a.length() >= b.length() ? a : b);
```

**Python comparison:**
```python
from functools import reduce
total = reduce(lambda a, b: a + b, [1, 2, 3, 4, 5])
```

#### count(), min(), max()

```java
long count = names.stream().filter(n -> n.startsWith("A")).count();

Optional<Integer> max = numbers.stream().max(Integer::compareTo);
Optional<Integer> min = numbers.stream().min(Integer::compareTo);
```

#### findFirst(), findAny()

```java
Optional<Employee> firstHighPaid = employees.stream()
    .filter(e -> e.getSalary() > 100000)
    .findFirst();

// findAny() is faster with parallel streams (order does not matter)
Optional<Employee> anyHighPaid = employees.parallelStream()
    .filter(e -> e.getSalary() > 100000)
    .findAny();
```

#### anyMatch(), allMatch(), noneMatch()

```java
boolean hasRichEmployee = employees.stream()
    .anyMatch(e -> e.getSalary() > 200000);  // At least one?

boolean allAdults = employees.stream()
    .allMatch(e -> e.getAge() >= 18);         // ALL match?

boolean noInterns = employees.stream()
    .noneMatch(e -> e.getRole().equals("Intern")); // NONE match?
```

---

### Collectors — Advanced Collection

#### groupingBy() — Group elements by a property

```java
// Group employees by department
Map<String, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment));
// {Engineering=[...], Marketing=[...], Sales=[...]}

// Count per department
Map<String, Long> countByDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));
// {Engineering=15, Marketing=8, Sales=12}

// Average salary per department
Map<String, Double> avgSalaryByDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.averagingDouble(Employee::getSalary)
    ));
```

**Python comparison:**
```python
from itertools import groupby
# Python requires sorting first, then groupby
employees.sort(key=lambda e: e.department)
grouped = {k: list(v) for k, v in groupby(employees, key=lambda e: e.department)}
```

Java's `groupingBy` is much more convenient — no pre-sorting needed.

#### partitioningBy() — Split into two groups (true/false)

```java
// Split employees into high-paid and low-paid
Map<Boolean, List<Employee>> partition = employees.stream()
    .collect(Collectors.partitioningBy(e -> e.getSalary() > 50000));

List<Employee> highPaid = partition.get(true);
List<Employee> lowPaid = partition.get(false);
```

#### joining() — Concatenate strings

```java
String allNames = employees.stream()
    .map(Employee::getName)
    .collect(Collectors.joining(", "));
// "Aarav, Priya, Rohan, Meera"

String formatted = employees.stream()
    .map(Employee::getName)
    .collect(Collectors.joining(", ", "[", "]"));
// "[Aarav, Priya, Rohan, Meera]"
```

---

### Parallel Streams

```java
// Sequential stream — processes elements one by one
long count1 = numbers.stream()
    .filter(n -> isPrime(n))
    .count();

// Parallel stream — processes elements in parallel using multiple CPU cores
long count2 = numbers.parallelStream()
    .filter(n -> isPrime(n))
    .count();
```

#### When to Use Parallel Streams
- Large datasets (millions of elements)
- CPU-intensive operations (prime checking, complex math)
- Operations that are independent (no shared mutable state)

#### When NOT to Use Parallel Streams
- Small datasets (overhead of parallelism is more than the gain)
- I/O operations (threads will block)
- Operations that depend on order
- When using shared mutable state

---

### 15+ Stream Practice Problems

These are the types of problems asked in every Java interview. Make sure you can solve them
fluently.

**Setup — Employee class used in examples:**

```java
class Employee {
    private String name;
    private String department;
    private String city;
    private double salary;
    private int age;

    // Constructor, getters, toString...

    public Employee(String name, String department, String city, double salary, int age) {
        this.name = name;
        this.department = department;
        this.city = city;
        this.salary = salary;
        this.age = age;
    }

    // getters...
}

List<Employee> employees = Arrays.asList(
    new Employee("Aarav", "Engineering", "Bangalore", 85000, 28),
    new Employee("Priya", "Marketing", "Mumbai", 65000, 32),
    new Employee("Rohan", "Engineering", "Delhi", 92000, 35),
    new Employee("Meera", "Marketing", "Mumbai", 58000, 26),
    new Employee("Karan", "Sales", "Bangalore", 45000, 24),
    new Employee("Ananya", "Engineering", "Mumbai", 110000, 30),
    new Employee("Vikram", "Sales", "Delhi", 52000, 29),
    new Employee("Neha", "Engineering", "Bangalore", 78000, 27)
);
```

#### Problem 1: Find all employees with salary > 70000

```java
List<Employee> highPaid = employees.stream()
    .filter(e -> e.getSalary() > 70000)
    .collect(Collectors.toList());
```

#### Problem 2: Get names of employees sorted by salary (descending)

```java
List<String> names = employees.stream()
    .sorted(Comparator.comparingDouble(Employee::getSalary).reversed())
    .map(Employee::getName)
    .collect(Collectors.toList());
// [Ananya, Rohan, Aarav, Neha, Priya, Meera, Vikram, Karan]
```

#### Problem 3: Find average salary per department

```java
Map<String, Double> avgByDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.averagingDouble(Employee::getSalary)
    ));
// {Engineering=91250.0, Marketing=61500.0, Sales=48500.0}
```

#### Problem 4: Find the highest paid employee

```java
Optional<Employee> highestPaid = employees.stream()
    .max(Comparator.comparingDouble(Employee::getSalary));
// Ananya - 110000
```

#### Problem 5: Group employees by department

```java
Map<String, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment));
```

#### Problem 6: Count employees per city

```java
Map<String, Long> countByCity = employees.stream()
    .collect(Collectors.groupingBy(Employee::getCity, Collectors.counting()));
// {Bangalore=3, Mumbai=3, Delhi=2}
```

#### Problem 7: Find duplicate elements in a list

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 2, 4, 3, 5, 1);

Set<Integer> seen = new HashSet<>();
List<Integer> duplicates = numbers.stream()
    .filter(n -> !seen.add(n))  // add() returns false if already present
    .collect(Collectors.toList());
// [2, 3, 1]

// Cleaner way using groupingBy
List<Integer> duplicates2 = numbers.stream()
    .collect(Collectors.groupingBy(n -> n, Collectors.counting()))
    .entrySet().stream()
    .filter(e -> e.getValue() > 1)
    .map(Map.Entry::getKey)
    .collect(Collectors.toList());
```

#### Problem 8: Flatten a list of lists

```java
List<List<Integer>> nested = Arrays.asList(
    Arrays.asList(1, 2, 3),
    Arrays.asList(4, 5),
    Arrays.asList(6, 7, 8, 9)
);

List<Integer> flat = nested.stream()
    .flatMap(List::stream)
    .collect(Collectors.toList());
// [1, 2, 3, 4, 5, 6, 7, 8, 9]
```

#### Problem 9: Find the second highest salary

```java
Optional<Double> secondHighest = employees.stream()
    .map(Employee::getSalary)
    .distinct()                                           // Remove duplicate salaries
    .sorted(Comparator.reverseOrder())                    // Sort descending
    .skip(1)                                              // Skip the first (highest)
    .findFirst();                                         // Get the second
// 92000.0
```

#### Problem 10: Convert list of strings to uppercase and remove duplicates

```java
List<String> words = Arrays.asList("hello", "world", "Hello", "WORLD", "java");

List<String> result = words.stream()
    .map(String::toUpperCase)
    .distinct()
    .collect(Collectors.toList());
// [HELLO, WORLD, JAVA]
```

#### Problem 11: Create a map of employee name to salary

```java
Map<String, Double> nameToSalary = employees.stream()
    .collect(Collectors.toMap(Employee::getName, Employee::getSalary));
// {Aarav=85000.0, Priya=65000.0, ...}
```

#### Problem 12: Find the department with the highest average salary

```java
Optional<Map.Entry<String, Double>> bestDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.averagingDouble(Employee::getSalary)
    ))
    .entrySet().stream()
    .max(Map.Entry.comparingByValue());
// Engineering=91250.0
```

#### Problem 13: Partition employees into age groups (under 30 and 30+)

```java
Map<Boolean, List<String>> ageGroups = employees.stream()
    .collect(Collectors.partitioningBy(
        e -> e.getAge() >= 30,
        Collectors.mapping(Employee::getName, Collectors.toList())
    ));
// {true=[Priya, Rohan, Ananya], false=[Aarav, Meera, Karan, Vikram, Neha]}
```

#### Problem 14: Sum of all salaries

```java
double totalSalary = employees.stream()
    .mapToDouble(Employee::getSalary)
    .sum();
// 585000.0
```

#### Problem 15: Find employees whose names start with 'A', sorted by age

```java
List<Employee> result = employees.stream()
    .filter(e -> e.getName().startsWith("A"))
    .sorted(Comparator.comparingInt(Employee::getAge))
    .collect(Collectors.toList());
// [Aarav (28), Ananya (30)]
```

---

## 5. Optional — Solving the NullPointerException Problem

### What is it?
Optional is a container that may or may not hold a value. It forces you to think about the
"no value" case instead of getting a surprise NullPointerException at runtime.

### Real-life Analogy
You order on Swiggy. Sometimes the restaurant has your item, sometimes it does not. Optional
is like the app saying "This item MAY be available" instead of crashing when it is not.

### Why does it exist?
NullPointerException is the most common exception in Java. Before Optional:

```java
// DANGEROUS — what if getEmployee returns null?
Employee emp = findEmployee("EMP001");
String dept = emp.getDepartment(); // NullPointerException if emp is null!
String upper = dept.toUpperCase(); // Another potential NPE!
```

### Using Optional

```java
import java.util.Optional;

// Creating Optionals
Optional<String> present = Optional.of("Hello");         // Value exists (null NOT allowed)
Optional<String> empty = Optional.empty();                // No value
Optional<String> maybe = Optional.ofNullable(getName());  // Might be null, might not

// Checking and getting values
if (present.isPresent()) {
    System.out.println(present.get());  // "Hello"
}

// Better: ifPresent — do something only if value exists
present.ifPresent(value -> System.out.println("Got: " + value));

// Default value if empty
String name = maybe.orElse("Unknown");

// Default with lazy computation
String name2 = maybe.orElseGet(() -> computeDefaultName());

// Throw exception if empty
String name3 = maybe.orElseThrow(() -> new RuntimeException("Name not found!"));
```

### Chaining with map() and flatMap()

```java
// Without Optional — null checks everywhere (Pyramid of Doom)
String city = null;
if (employee != null) {
    Address address = employee.getAddress();
    if (address != null) {
        city = address.getCity();
        if (city != null) {
            city = city.toUpperCase();
        }
    }
}

// With Optional — clean chain
String city = Optional.ofNullable(employee)
    .map(Employee::getAddress)
    .map(Address::getCity)
    .map(String::toUpperCase)
    .orElse("UNKNOWN");
// If ANY step returns null, the chain short-circuits to "UNKNOWN"
```

### When to Use Optional

- **YES:** Return types of methods that might not have a result
  ```java
  public Optional<Employee> findById(String id) { ... }
  ```

- **NO:** Method parameters (makes calling the method awkward)
  ```java
  // BAD — don't do this
  public void process(Optional<String> name) { ... }
  // GOOD — just use nullable
  public void process(String name) { ... }
  ```

- **NO:** Class fields (use null with clear documentation instead)

### Python Comparison
```python
# Python does not have Optional, but has similar patterns:

# Python way: default value
name = get_name() or "Unknown"

# Python way: None checks
city = employee.address.city.upper() if employee and employee.address and employee.address.city else "UNKNOWN"
```

Java's Optional is more explicit and safer than Python's `or` and `None` checks.

---

## 6. Date/Time API (java.time)

### What is it?
Java 8 introduced a completely new date and time API in the `java.time` package. The old
`java.util.Date` and `Calendar` classes were terrible — mutable, confusing, and error-prone.

### Why the old API was terrible

```java
// OLD WAY — confusing!
Date date = new Date(2024, 1, 15);
// Wait... months are 0-indexed! January = 0, February = 1.
// And year is offset from 1900! So 2024 actually means year 3924.
// This creates March 15, 3924. WHAT?!

Calendar cal = Calendar.getInstance();
cal.set(Calendar.MONTH, 1); // This is FEBRUARY, not January!
// Also, Date is mutable — any code can change it. Nightmare.
```

### New java.time API — Clean and Immutable

```java
import java.time.*;
import java.time.format.DateTimeFormatter;

// LocalDate — date without time
LocalDate today = LocalDate.now();
LocalDate birthday = LocalDate.of(1995, 8, 15);  // August 15, 1995
LocalDate parsed = LocalDate.parse("2024-03-15");

// LocalTime — time without date
LocalTime now = LocalTime.now();
LocalTime meeting = LocalTime.of(14, 30);  // 2:30 PM

// LocalDateTime — both date and time
LocalDateTime appointmentTime = LocalDateTime.of(2024, 3, 15, 14, 30);
LocalDateTime current = LocalDateTime.now();

// Period — difference between dates
Period age = Period.between(birthday, today);
System.out.println("Age: " + age.getYears() + " years");

// Duration — difference between times
Duration duration = Duration.between(LocalTime.of(9, 0), LocalTime.of(17, 30));
System.out.println("Work hours: " + duration.toHours());

// Formatting
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
String formatted = today.format(formatter);  // "03-06-2026"

// Parsing
LocalDate parsed2 = LocalDate.parse("15-08-1995", formatter);

// Date arithmetic
LocalDate nextWeek = today.plusWeeks(1);
LocalDate lastMonth = today.minusMonths(1);
LocalDate twoYearsLater = today.plusYears(2);

// Comparison
boolean isBefore = birthday.isBefore(today);  // true
boolean isAfter = today.isAfter(birthday);     // true
```

### Python Comparison
```python
from datetime import date, datetime, timedelta

today = date.today()
birthday = date(1995, 8, 15)
age_days = (today - birthday).days
next_week = today + timedelta(weeks=1)
formatted = today.strftime("%d-%m-%Y")
```

Java's new API is very similar to Python's `datetime` module — both are clean and intuitive.

---

## 7. Default Methods in Interfaces

### What is it?
Before Java 8, interfaces could only have abstract methods (no body). Default methods let you
add method implementations directly in the interface.

### Why does it exist?
When Java 8 added streams, they needed to add methods like `stream()` to the `Collection`
interface. But if they added a new abstract method, EVERY class that implemented `Collection`
would break. Default methods solved this — they add new methods without breaking existing code.

```java
interface Vehicle {
    void start();  // Abstract — implementing class MUST provide this

    // Default method — implementing class CAN override, but does not have to
    default void honk() {
        System.out.println("Beep beep!");
    }
}

class Car implements Vehicle {
    @Override
    public void start() {
        System.out.println("Car started with key");
    }
    // honk() is inherited from the interface — no need to implement
}

class Truck implements Vehicle {
    @Override
    public void start() {
        System.out.println("Truck started with button");
    }

    @Override
    public void honk() {
        System.out.println("HOOOOONK!"); // Trucks honk louder
    }
}
```

### Python Comparison
Python does not need this because Python never required you to implement all methods of a parent.
Python has always allowed default implementations in base classes and mixins.

---

## Common Mistakes

1. **Modifying a collection while streaming it** — Throws ConcurrentModificationException.
   Streams create a pipeline; do not mutate the source.

2. **Reusing a stream** — A stream can only be consumed ONCE. After a terminal operation, it
   is closed. Create a new stream if you need to process again.

3. **Using Optional.get() without checking** — `get()` on an empty Optional throws
   NoSuchElementException. Always use `orElse()`, `orElseThrow()`, or `ifPresent()`.

4. **Side effects in stream operations** — `map()` and `filter()` should be pure functions
   (no printing, no modifying external variables). Use `forEach()` for side effects.

5. **Parallel streams with shared mutable state** — This is a race condition. Parallel streams
   should only work with thread-safe or immutable data.

---

## Practice Exercises

### Exercise 1: Zomato Restaurant Finder
Create a `Restaurant` class with name, cuisine, rating, city, and isVeg. Create a list of 10
restaurants. Use streams to:
- Find all restaurants in "Mumbai" with rating > 4.0
- Group restaurants by cuisine type
- Find the highest-rated vegetarian restaurant
- Get a comma-separated string of all restaurant names

### Exercise 2: IRCTC Booking Analytics
Create a `Booking` class with passengerName, train, source, destination, fare, and bookingDate.
Use streams to:
- Total revenue for a given month
- Most popular route (source-destination pair)
- Average fare per route
- List of passengers who booked more than 3 times

### Exercise 3: Flipkart Product Catalog
Given a list of `Product` objects with name, category, price, and rating, write stream queries to:
- Find the cheapest product in each category
- Find all products where the name contains a given search term (case-insensitive)
- Sort by category first, then by price within each category
- Create a Map of category -> list of product names sorted by rating

### Exercise 4: Optional Chain
Write a method that takes a Map<String, Map<String, List<String>>> (city -> area -> restaurants)
and safely finds the first restaurant in a given area of a given city using Optional chaining.
Return "No restaurant found" if any part of the chain is missing.

### Exercise 5: Lambda Comparators
Create an Employee list and sort it by:
- Department (ascending), then salary (descending) within each department
- Using `Comparator.comparing().thenComparing()` with lambdas
- Also implement the same sort using the pre-Java-8 Comparator approach, to see the difference
