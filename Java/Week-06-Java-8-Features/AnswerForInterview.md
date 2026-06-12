# Week 6: Java 8 Features — The Game Changer — Interview Answers & Exercise Solutions

> **The Interview Golden Rule for Java 8** 🏆
> When asked about ANY Java 8 feature, structure your answer in four beats:
> **1️⃣ Definition** (one crisp sentence) → **2️⃣ Why Java 8 added it** (the pain it removed) → **3️⃣ Code** (old verbose way vs. new clean way) → **4️⃣ Gotcha** (the trap that proves you've actually used it).
>
> Example mini-answer for *lambdas*: *"A lambda is an inline anonymous function (definition). Java 8 added it because we were drowning in anonymous inner classes for one-method tasks (why). Before, sorting took a 6-line `Comparator` class; now it's `list.sort((a,b)->...)` (code). The gotcha: a lambda can only target a **functional interface** — one abstract method — and any variables it captures must be effectively final (gotcha)."*
>
> Nail those four beats and you sound like someone who *ships* Java 8 code, not someone who memorized a blog post. 😎

This file has two parts:
- **PART A** — every Practice Exercise + the 15 Stream Practice Problems from the Plan, fully solved in compilable Java.
- **PART B** — 16 interview questions with confident, spoken-style model answers, comparison tables, and old-vs-new code.

A reusable `Employee` model (used across many answers) is defined once at the top of PART A so all snippets compile.

---

# PART A — Exercise / Practice Solutions

## 🔧 Shared Setup — The `Employee` Class

Most stream problems reuse this model. Define it once; reuse everywhere.

```java
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

class Employee {
    private final String name;
    private final String department;
    private final String city;
    private final double salary;
    private final int age;

    public Employee(String name, String department, String city, double salary, int age) {
        this.name = name;
        this.department = department;
        this.city = city;
        this.salary = salary;
        this.age = age;
    }

    public String getName()       { return name; }
    public String getDepartment() { return department; }
    public String getCity()       { return city; }
    public double getSalary()     { return salary; }
    public int    getAge()        { return age; }

    @Override
    public String toString() {
        return name + " (" + department + ", " + city + ", " + salary + ", " + age + ")";
    }
}

class EmployeeData {
    static List<Employee> sample() {
        return Arrays.asList(
            new Employee("Aarav",  "Engineering", "Bangalore",  85000, 28),
            new Employee("Priya",  "Marketing",   "Mumbai",     65000, 32),
            new Employee("Rohan",  "Engineering", "Delhi",      92000, 35),
            new Employee("Meera",  "Marketing",   "Mumbai",     58000, 26),
            new Employee("Karan",  "Sales",       "Bangalore",  45000, 24),
            new Employee("Ananya", "Engineering", "Mumbai",    110000, 30),
            new Employee("Vikram", "Sales",       "Delhi",      52000, 29),
            new Employee("Neha",   "Engineering", "Bangalore",  78000, 27)
        );
    }
}
```

---

## 📦 Section 1 — The 15 Stream Practice Problems (from the Plan)

Here is a single runnable class that solves **all 15** problems with verified output comments. Each method is self-contained so you can copy any one into an interview whiteboard.

```java
import java.util.*;
import java.util.stream.*;

public class StreamPracticeSolutions {

    public static void main(String[] args) {
        List<Employee> employees = EmployeeData.sample();

        // ---- Problem 1: Employees with salary > 70000 ----
        List<Employee> highPaid = employees.stream()
            .filter(e -> e.getSalary() > 70000)
            .collect(Collectors.toList());
        System.out.println("P1: " + highPaid);
        // Aarav, Rohan, Ananya, Neha

        // ---- Problem 2: Names sorted by salary (descending) ----
        List<String> namesBySalaryDesc = employees.stream()
            .sorted(Comparator.comparingDouble(Employee::getSalary).reversed())
            .map(Employee::getName)
            .collect(Collectors.toList());
        System.out.println("P2: " + namesBySalaryDesc);
        // [Ananya, Rohan, Aarav, Neha, Priya, Meera, Vikram, Karan]

        // ---- Problem 3: Average salary per department ----
        Map<String, Double> avgByDept = employees.stream()
            .collect(Collectors.groupingBy(
                Employee::getDepartment,
                Collectors.averagingDouble(Employee::getSalary)));
        System.out.println("P3: " + avgByDept);
        // {Sales=48500.0, Marketing=61500.0, Engineering=91250.0}

        // ---- Problem 4: Highest paid employee ----
        Optional<Employee> highestPaid = employees.stream()
            .max(Comparator.comparingDouble(Employee::getSalary));
        System.out.println("P4: " + highestPaid.map(Employee::getName).orElse("none"));
        // Ananya

        // ---- Problem 5: Group employees by department ----
        Map<String, List<Employee>> byDept = employees.stream()
            .collect(Collectors.groupingBy(Employee::getDepartment));
        System.out.println("P5 keys: " + byDept.keySet());
        // [Sales, Marketing, Engineering]

        // ---- Problem 6: Count employees per city ----
        Map<String, Long> countByCity = employees.stream()
            .collect(Collectors.groupingBy(Employee::getCity, Collectors.counting()));
        System.out.println("P6: " + countByCity);
        // {Bangalore=3, Delhi=2, Mumbai=3}

        // ---- Problem 7: Find duplicate elements in a list ----
        List<Integer> numbers = Arrays.asList(1, 2, 3, 2, 4, 3, 5, 1);
        Set<Integer> seen = new HashSet<>();
        List<Integer> duplicates = numbers.stream()
            .filter(n -> !seen.add(n)) // add() returns false if already present
            .distinct()                // avoid reporting the same dup twice
            .collect(Collectors.toList());
        System.out.println("P7: " + duplicates);
        // [2, 3, 1]

        // ---- Problem 8: Flatten a list of lists ----
        List<List<Integer>> nested = Arrays.asList(
            Arrays.asList(1, 2, 3),
            Arrays.asList(4, 5),
            Arrays.asList(6, 7, 8, 9));
        List<Integer> flat = nested.stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
        System.out.println("P8: " + flat);
        // [1, 2, 3, 4, 5, 6, 7, 8, 9]

        // ---- Problem 9: Second highest salary ----
        Optional<Double> secondHighest = employees.stream()
            .map(Employee::getSalary)
            .distinct()
            .sorted(Comparator.reverseOrder())
            .skip(1)
            .findFirst();
        System.out.println("P9: " + secondHighest.orElse(-1.0));
        // 92000.0

        // ---- Problem 10: Uppercase + remove duplicates ----
        List<String> words = Arrays.asList("hello", "world", "Hello", "WORLD", "java");
        List<String> uniqueUpper = words.stream()
            .map(String::toUpperCase)
            .distinct()
            .collect(Collectors.toList());
        System.out.println("P10: " + uniqueUpper);
        // [HELLO, WORLD, JAVA]

        // ---- Problem 11: Map of name -> salary ----
        Map<String, Double> nameToSalary = employees.stream()
            .collect(Collectors.toMap(Employee::getName, Employee::getSalary));
        System.out.println("P11 size: " + nameToSalary.size());
        // 8

        // ---- Problem 12: Department with highest average salary ----
        Optional<Map.Entry<String, Double>> bestDept = employees.stream()
            .collect(Collectors.groupingBy(
                Employee::getDepartment,
                Collectors.averagingDouble(Employee::getSalary)))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue());
        System.out.println("P12: " + bestDept.map(Map.Entry::getKey).orElse("none"));
        // Engineering

        // ---- Problem 13: Partition into age groups (30+ vs under 30) ----
        Map<Boolean, List<String>> ageGroups = employees.stream()
            .collect(Collectors.partitioningBy(
                e -> e.getAge() >= 30,
                Collectors.mapping(Employee::getName, Collectors.toList())));
        System.out.println("P13 true (30+): " + ageGroups.get(true));
        System.out.println("P13 false (<30): " + ageGroups.get(false));
        // true=[Priya, Rohan, Ananya], false=[Aarav, Meera, Karan, Vikram, Neha]

        // ---- Problem 14: Sum of all salaries ----
        double totalSalary = employees.stream()
            .mapToDouble(Employee::getSalary)
            .sum();
        System.out.println("P14: " + totalSalary);
        // 585000.0

        // ---- Problem 15: Names starting with 'A', sorted by age ----
        List<Employee> startsWithA = employees.stream()
            .filter(e -> e.getName().startsWith("A"))
            .sorted(Comparator.comparingInt(Employee::getAge))
            .collect(Collectors.toList());
        System.out.println("P15: " + startsWithA);
        // [Aarav (28), Ananya (30)]
    }
}
```

> 💡 **Interview tip on Problem 7:** The `!seen.add(n)` trick is a *classic* — `Set.add` returns `false` when the element already exists, so the filter keeps only repeats. Mention that `.distinct()` is needed afterward if an element appears 3+ times, otherwise it shows up multiple times.

---

## 🍽️ Exercise 1 — Zomato Restaurant Finder

**Task:** `Restaurant(name, cuisine, rating, city, isVeg)`. Build 10 restaurants and use streams to (a) find Mumbai restaurants rated > 4.0, (b) group by cuisine, (c) find the highest-rated veg restaurant, (d) join all names into a CSV string.

```java
import java.util.*;
import java.util.stream.*;

class Restaurant {
    private final String name, cuisine, city;
    private final double rating;
    private final boolean isVeg;

    public Restaurant(String name, String cuisine, double rating, String city, boolean isVeg) {
        this.name = name; this.cuisine = cuisine; this.rating = rating;
        this.city = city; this.isVeg = isVeg;
    }
    public String getName()    { return name; }
    public String getCuisine() { return cuisine; }
    public String getCity()    { return city; }
    public double getRating()  { return rating; }
    public boolean isVeg()     { return isVeg; }
    @Override public String toString() { return name + "(" + rating + ")"; }
}

public class ZomatoFinder {
    public static void main(String[] args) {
        List<Restaurant> restaurants = Arrays.asList(
            new Restaurant("Trishna",        "Seafood",     4.5, "Mumbai",    false),
            new Restaurant("Britannia",      "Parsi",       4.6, "Mumbai",    false),
            new Restaurant("Swati Snacks",   "Gujarati",    4.3, "Mumbai",    true),
            new Restaurant("Bademiya",       "Mughlai",     3.9, "Mumbai",    false),
            new Restaurant("Shree Thaker",   "Gujarati",    4.7, "Mumbai",    true),
            new Restaurant("MTR",            "South Indian",4.8, "Bangalore", true),
            new Restaurant("Vidyarthi Bhavan","South Indian",4.6,"Bangalore", true),
            new Restaurant("Karavalli",      "Seafood",     4.5, "Bangalore", false),
            new Restaurant("Bukhara",        "Mughlai",     4.7, "Delhi",     false),
            new Restaurant("Saravana Bhavan","South Indian",4.2, "Delhi",     true)
        );

        // (a) Mumbai restaurants rated > 4.0
        List<Restaurant> topMumbai = restaurants.stream()
            .filter(r -> r.getCity().equals("Mumbai"))
            .filter(r -> r.getRating() > 4.0)
            .collect(Collectors.toList());
        System.out.println("(a) Top Mumbai: " + topMumbai);
        // [Trishna(4.5), Britannia(4.6), Swati Snacks(4.3), Shree Thaker(4.7)]

        // (b) Group by cuisine
        Map<String, List<Restaurant>> byCuisine = restaurants.stream()
            .collect(Collectors.groupingBy(Restaurant::getCuisine));
        System.out.println("(b) Cuisines: " + byCuisine.keySet());

        // (c) Highest-rated vegetarian restaurant
        Optional<Restaurant> bestVeg = restaurants.stream()
            .filter(Restaurant::isVeg)
            .max(Comparator.comparingDouble(Restaurant::getRating));
        System.out.println("(c) Best veg: " + bestVeg.map(Restaurant::getName).orElse("none"));
        // MTR (4.8)

        // (d) CSV of all names
        String csv = restaurants.stream()
            .map(Restaurant::getName)
            .collect(Collectors.joining(", "));
        System.out.println("(d) " + csv);
    }
}
```

---

## 🚆 Exercise 2 — IRCTC Booking Analytics

**Task:** `Booking(passengerName, train, source, destination, fare, bookingDate)`. Compute (a) total revenue for a given month, (b) most popular route, (c) average fare per route, (d) passengers who booked more than 3 times.

```java
import java.time.*;
import java.util.*;
import java.util.stream.*;

class Booking {
    private final String passengerName, train, source, destination;
    private final double fare;
    private final LocalDate bookingDate;

    public Booking(String passengerName, String train, String source,
                   String destination, double fare, LocalDate bookingDate) {
        this.passengerName = passengerName; this.train = train;
        this.source = source; this.destination = destination;
        this.fare = fare; this.bookingDate = bookingDate;
    }
    public String getPassengerName() { return passengerName; }
    public String getTrain()         { return train; }
    public String getSource()        { return source; }
    public String getDestination()   { return destination; }
    public double getFare()          { return fare; }
    public LocalDate getBookingDate(){ return bookingDate; }
    public String route()            { return source + "-" + destination; }
}

public class IrctcAnalytics {
    public static void main(String[] args) {
        List<Booking> bookings = Arrays.asList(
            new Booking("Aarav",  "Rajdhani",  "Delhi",   "Mumbai",   2500, LocalDate.of(2026, 3, 5)),
            new Booking("Priya",  "Shatabdi",  "Delhi",   "Agra",      800, LocalDate.of(2026, 3, 9)),
            new Booking("Aarav",  "Duronto",   "Mumbai",  "Pune",      450, LocalDate.of(2026, 3, 12)),
            new Booking("Rohan",  "Rajdhani",  "Delhi",   "Mumbai",   2600, LocalDate.of(2026, 4, 2)),
            new Booking("Aarav",  "Garib Rath","Delhi",   "Mumbai",   1800, LocalDate.of(2026, 3, 20)),
            new Booking("Priya",  "Shatabdi",  "Delhi",   "Agra",      820, LocalDate.of(2026, 3, 22)),
            new Booking("Aarav",  "Tejas",     "Delhi",   "Mumbai",   3000, LocalDate.of(2026, 3, 28)),
            new Booking("Meera",  "Duronto",   "Mumbai",  "Pune",      460, LocalDate.of(2026, 3, 30))
        );

        // (a) Total revenue for March 2026
        YearMonth targetMonth = YearMonth.of(2026, 3);
        double marchRevenue = bookings.stream()
            .filter(b -> YearMonth.from(b.getBookingDate()).equals(targetMonth))
            .mapToDouble(Booking::getFare)
            .sum();
        System.out.println("(a) March revenue: " + marchRevenue);
        // 12650.0

        // (b) Most popular route (by count)
        Optional<Map.Entry<String, Long>> popularRoute = bookings.stream()
            .collect(Collectors.groupingBy(Booking::route, Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue());
        System.out.println("(b) Popular route: "
            + popularRoute.map(Map.Entry::getKey).orElse("none"));
        // Delhi-Mumbai

        // (c) Average fare per route
        Map<String, Double> avgFarePerRoute = bookings.stream()
            .collect(Collectors.groupingBy(Booking::route,
                     Collectors.averagingDouble(Booking::getFare)));
        System.out.println("(c) Avg fare/route: " + avgFarePerRoute);

        // (d) Passengers who booked more than 3 times
        List<String> frequentFlyers = bookings.stream()
            .collect(Collectors.groupingBy(Booking::getPassengerName, Collectors.counting()))
            .entrySet().stream()
            .filter(e -> e.getValue() > 3)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        System.out.println("(d) Frequent passengers: " + frequentFlyers);
        // [Aarav]  (4 bookings)
    }
}
```

> 🧠 **Why `YearMonth`?** It captures *year + month* as one immutable value, so `YearMonth.from(date).equals(target)` cleanly answers "is this in March 2026?" without fiddling with `getMonthValue()` AND `getYear()` separately. This is exactly the kind of `java.time` fluency interviewers love.

---

## 🛒 Exercise 3 — Flipkart Product Catalog

**Task:** `Product(name, category, price, rating)`. (a) cheapest product per category, (b) name contains a search term (case-insensitive), (c) sort by category then price, (d) Map of category → product names sorted by rating (desc).

```java
import java.util.*;
import java.util.stream.*;

class Product {
    private final String name, category;
    private final double price, rating;
    public Product(String name, String category, double price, double rating) {
        this.name = name; this.category = category; this.price = price; this.rating = rating;
    }
    public String getName()     { return name; }
    public String getCategory() { return category; }
    public double getPrice()    { return price; }
    public double getRating()   { return rating; }
    @Override public String toString() { return name + "(" + price + ")"; }
}

public class FlipkartCatalog {
    public static void main(String[] args) {
        List<Product> products = Arrays.asList(
            new Product("iPhone 15",     "Mobile",    79999, 4.6),
            new Product("Galaxy S24",    "Mobile",    74999, 4.4),
            new Product("Redmi Note 13", "Mobile",    18999, 4.3),
            new Product("MacBook Air",   "Laptop",   114999, 4.7),
            new Product("Dell XPS",      "Laptop",    99999, 4.5),
            new Product("Sony WH-1000",  "Headphone", 26999, 4.8),
            new Product("Boat Rockerz",  "Headphone",  1499, 4.1)
        );

        // (a) Cheapest product in each category
        Map<String, Optional<Product>> cheapestPerCategory = products.stream()
            .collect(Collectors.groupingBy(Product::getCategory,
                     Collectors.minBy(Comparator.comparingDouble(Product::getPrice))));
        System.out.println("(a) Cheapest/category: " + cheapestPerCategory);
        // {Mobile=Redmi Note 13, Laptop=Dell XPS, Headphone=Boat Rockerz}

        // (b) Name contains search term (case-insensitive)
        String term = "macbook";
        List<Product> matches = products.stream()
            .filter(p -> p.getName().toLowerCase().contains(term.toLowerCase()))
            .collect(Collectors.toList());
        System.out.println("(b) Matches '" + term + "': " + matches);
        // [MacBook Air]

        // (c) Sort by category (asc) then price (asc)
        List<Product> sorted = products.stream()
            .sorted(Comparator.comparing(Product::getCategory)
                              .thenComparingDouble(Product::getPrice))
            .collect(Collectors.toList());
        System.out.println("(c) Sorted: " + sorted);

        // (d) Map category -> product names sorted by rating (descending)
        Map<String, List<String>> namesByRating = products.stream()
            .collect(Collectors.groupingBy(
                Product::getCategory,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> list.stream()
                                .sorted(Comparator.comparingDouble(Product::getRating).reversed())
                                .map(Product::getName)
                                .collect(Collectors.toList()))));
        System.out.println("(d) Names by rating: " + namesByRating);
        // {Mobile=[iPhone 15, Galaxy S24, Redmi Note 13], Laptop=[MacBook Air, Dell XPS],
        //  Headphone=[Sony WH-1000, Boat Rockerz]}
    }
}
```

> ⭐ **`collectingAndThen` is the star of (d):** it lets you group, then post-process each group's list (sort by rating, then map to names). When an interviewer sees this they know you understand *downstream collectors* — a senior-level signal.

---

## 🔗 Exercise 4 — Optional Chain (Safe Nested Lookup)

**Task:** Given `Map<String, Map<String, List<String>>>` (city → area → restaurants), safely find the first restaurant in a given area of a given city using Optional chaining. Return `"No restaurant found"` if any link is missing.

```java
import java.util.*;

public class OptionalChainExercise {

    static String firstRestaurant(
            Map<String, Map<String, List<String>>> data,
            String city, String area) {

        return Optional.ofNullable(data)                    // guard the whole map
            .map(d -> d.get(city))                          // city -> area map (or null)
            .map(areaMap -> areaMap.get(area))              // area -> restaurant list (or null)
            .filter(list -> !list.isEmpty())                // skip empty lists
            .map(list -> list.get(0))                       // first restaurant
            .orElse("No restaurant found");                 // any missing link lands here
    }

    public static void main(String[] args) {
        Map<String, Map<String, List<String>>> data = new HashMap<>();
        Map<String, List<String>> mumbai = new HashMap<>();
        mumbai.put("Bandra", Arrays.asList("Bastian", "Pali Village Cafe"));
        mumbai.put("Andheri", new ArrayList<>()); // empty on purpose
        data.put("Mumbai", mumbai);

        System.out.println(firstRestaurant(data, "Mumbai", "Bandra"));  // Bastian
        System.out.println(firstRestaurant(data, "Mumbai", "Andheri")); // No restaurant found (empty)
        System.out.println(firstRestaurant(data, "Mumbai", "Colaba"));  // No restaurant found (no area)
        System.out.println(firstRestaurant(data, "Delhi",  "CP"));      // No restaurant found (no city)
        System.out.println(firstRestaurant(null,  "Mumbai","Bandra"));  // No restaurant found (null map)
    }
}
```

> 🎯 **The whole point:** the old version was a *Pyramid of Doom* of nested `if (x != null)` checks. The Optional chain short-circuits to the default the instant any `.map()` returns null — one readable pipeline replaces 10 lines of nesting.

---

## ⚖️ Exercise 5 — Lambda Comparators (Old vs. New)

**Task:** Sort employees by department (asc) then salary (desc), the Java 8 way with `Comparator.comparing().thenComparing()`, AND the pre-Java-8 way to feel the difference.

```java
import java.util.*;

public class ComparatorExercise {
    public static void main(String[] args) {
        // === Java 8 way: fluent, chained, readable ===
        List<Employee> modern = new ArrayList<>(EmployeeData.sample());
        modern.sort(
            Comparator.comparing(Employee::getDepartment)                 // dept ascending
                      .thenComparing(Comparator.comparingDouble(Employee::getSalary).reversed()) // salary descending
        );
        System.out.println("=== Java 8 sort ===");
        modern.forEach(System.out::println);

        // === Pre-Java-8 way: anonymous inner class, manual tie-break ===
        List<Employee> legacy = new ArrayList<>(EmployeeData.sample());
        Collections.sort(legacy, new Comparator<Employee>() {
            @Override
            public int compare(Employee a, Employee b) {
                int deptCmp = a.getDepartment().compareTo(b.getDepartment());
                if (deptCmp != 0) {
                    return deptCmp;                       // primary: department ascending
                }
                // secondary: salary descending -> compare b to a
                return Double.compare(b.getSalary(), a.getSalary());
            }
        });
        System.out.println("\n=== Pre-Java-8 sort (same result) ===");
        legacy.forEach(System.out::println);

        // Both produce the identical ordering:
        // Engineering: Ananya(110000), Rohan(92000), Aarav(85000), Neha(78000)
        // Marketing:   Priya(65000), Meera(58000)
        // Sales:       Vikram(52000), Karan(45000)
    }
}
```

| Aspect | Pre-Java-8 (`Collections.sort` + inner class) | Java 8 (`Comparator.comparing`) |
|--------|----------------------------------------------|---------------------------------|
| Lines of code | ~10 | ~3 |
| Tie-breaking | Manual `if (cmp != 0)` ladder | `.thenComparing(...)` chain |
| Reverse a key | Swap `a`/`b` by hand (error-prone) | `.reversed()` |
| Readability | Buried logic | Reads like the sort spec |

---

# PART B — Interview Questions & Model Answers

> Each answer is written the way you'd actually *say* it out loud. Practice saying them — fluency beats memorization. 🎤

---

### Q1. What is a lambda expression, and why did Java 8 introduce it?

**Answer:** "A lambda is a compact, anonymous function — a block of behavior you can pass around like data. Java 8 added it because before lambdas, passing behavior meant writing a verbose anonymous inner class for a single method. For example, sorting a list used to need a 6-line `Comparator` inner class; with a lambda it's `list.sort((a, b) -> a.length() - b.length())`. Under the hood a lambda is just an instance of a **functional interface** — an interface with exactly one abstract method. The key gotcha is that any local variable a lambda captures must be **effectively final**, because the lambda might outlive the method's stack frame."

```java
// Old way
Collections.sort(cities, new Comparator<String>() {
    public int compare(String a, String b) { return a.length() - b.length(); }
});
// New way
cities.sort((a, b) -> a.length() - b.length());
```

---

### Q2. What is a functional interface? Name the four core ones.

**Answer:** "A functional interface has exactly one abstract method — that's what makes it a valid target for a lambda. You can mark it with `@FunctionalInterface` so the compiler enforces the single-method rule. Java ships a `java.util.function` package full of them. The four I reach for constantly are: **`Predicate<T>`** (`test` → boolean, for filtering), **`Function<T,R>`** (`apply` → transforms T into R), **`Consumer<T>`** (`accept` → does something, returns void), and **`Supplier<T>`** (`get` → produces a value, takes nothing). A subtle point: a functional interface can still have `default` and `static` methods — those don't count against the single-abstract-method rule."

| Interface | Method | Input → Output | Mnemonic |
|-----------|--------|----------------|----------|
| `Predicate<T>` | `test(T)` | T → boolean | "**P**ass/fail" |
| `Function<T,R>` | `apply(T)` | T → R | "**F**actory/transform" |
| `Consumer<T>` | `accept(T)` | T → void | "**C**onsumes, gives nothing back" |
| `Supplier<T>` | `get()` | nothing → T | "**S**upplies out of thin air" |

---

### Q3. Explain `Predicate`, `Function`, `Consumer`, and `Supplier` with one line of code each.

**Answer:** "Each maps to a verb. A **Predicate** answers a yes/no question. A **Function** transforms one thing into another. A **Consumer** takes something and acts on it with no return. A **Supplier** manufactures a value from nothing — great for lazy initialization."

```java
Predicate<Integer> isEven   = n -> n % 2 == 0;          isEven.test(4);          // true
Function<String, Integer> len = s -> s.length();         len.apply("Flipkart");   // 8
Consumer<String> printer    = s -> System.out.println(s); printer.accept("Hi");   // prints Hi
Supplier<Double> rand       = Math::random;              rand.get();              // 0.0..1.0
```

"And they compose: `predicate.and(other)`, `function.andThen(next)`, `consumer.andThen(next)`."

---

### Q4. What is a Stream? How is it different from a Collection?

**Answer:** "A Stream is a *pipeline* for processing a sequence of elements — you describe *what* transformation you want, and the stream runs it. The crucial difference: a **Collection stores data**; a **Stream describes a computation over data**. A Collection is about memory and structure; a Stream is about a one-time traversal. Streams don't store elements, they don't modify the source, and they're **single-use** — once you call a terminal operation, the stream is consumed and you must create a new one to iterate again. The mental model is a factory assembly line: source → map → filter → sort → collect."

| | Collection | Stream |
|---|------------|--------|
| Purpose | Store & organize data | Process data |
| Storage | Holds elements in memory | Holds nothing; pulls from source |
| Reusable | Yes — iterate as often as you like | No — single-use |
| Evaluation | Eager | Lazy (until terminal op) |
| Mutates source | Often | Never |

---

### Q5. What is the difference between intermediate and terminal operations?

**Answer:** "**Intermediate** operations — like `filter`, `map`, `sorted`, `distinct`, `limit` — return a new Stream, so they can be chained, and they're **lazy**: they build up a recipe but don't execute. **Terminal** operations — like `collect`, `forEach`, `reduce`, `count`, `findFirst`, `anyMatch` — produce a result or side effect and *trigger* the whole pipeline. Without a terminal operation, nothing runs at all. So this line does literally nothing: `list.stream().filter(x -> x > 0).map(x -> x * 2);` — there's no terminal op to pull data through it."

```java
// filter/map are intermediate (lazy); collect is terminal (triggers execution)
List<Integer> doubled = nums.stream()
    .filter(n -> n > 0)   // intermediate
    .map(n -> n * 2)      // intermediate
    .collect(Collectors.toList()); // TERMINAL — now it actually runs
```

---

### Q6. Explain lazy evaluation in streams. Why does it matter?

**Answer:** "Lazy evaluation means intermediate operations don't process anything until a terminal op demands a result — and then elements are pulled through the pipeline *one at a time*, not stage by stage. This enables two big wins: **short-circuiting** (e.g., `findFirst` or `limit` can stop early without touching the rest), and **fusion** (filter and map for a single element happen together, avoiding intermediate collections). It's also why infinite streams work: `Stream.iterate(0, n -> n + 2).limit(5)` is fine because `limit` stops the source after 5 pulls."

```java
// Prints only enough to satisfy limit(2) — laziness + short-circuit
Stream.iterate(1, n -> n + 1)
      .peek(n -> System.out.println("looking at " + n))
      .filter(n -> n % 2 == 0)
      .limit(2)
      .forEach(n -> System.out.println("got " + n));
// It never runs forever, even though iterate() is infinite.
```

---

### Q7. What is the difference between `map()` and `flatMap()`?

**Answer:** "Both transform elements, but the shape of the output differs. **`map`** is one-to-one: each input produces exactly one output, so `Stream<T>` becomes `Stream<R>`. **`flatMap`** is one-to-many-then-flattened: each input produces a *stream* of outputs, and `flatMap` glues all those mini-streams into one flat stream. The classic use: you have `List<List<X>>` or each object owns a collection, and you want a single flat stream. If I used `map` there I'd get `Stream<Stream<X>>` — nested and useless; `flatMap` flattens it to `Stream<X>`."

```java
List<List<String>> orders = Arrays.asList(
    Arrays.asList("Pizza", "Burger"),
    Arrays.asList("Dosa", "Idli"));

List<String> all = orders.stream()
    .flatMap(List::stream)          // each inner list -> stream of items, flattened
    .collect(Collectors.toList());  // [Pizza, Burger, Dosa, Idli]
```

| | `map` | `flatMap` |
|---|-------|-----------|
| Cardinality | 1 → 1 | 1 → many (then flattened) |
| Returns per element | a value | a Stream |
| Input `Stream<List<X>>` | → `Stream<List<X>>` (still nested) | → `Stream<X>` (flat) |

---

### Q8. Explain `reduce()`. Give an example.

**Answer:** "`reduce` folds a stream into a single value by repeatedly combining elements. The common form takes an **identity** (the starting value, also the result for an empty stream) and a **binary operator**. For summing it's `reduce(0, (a, b) -> a + b)` — start at 0, keep adding. There's also a no-identity form that returns `Optional<T>`, because an empty stream has no result. For combining things in parallel safely there's a three-arg form with a combiner. Key gotcha: the accumulator must be **associative** for parallel streams to give correct results."

```java
int sum = Stream.of(1, 2, 3, 4, 5).reduce(0, Integer::sum);            // 15
Optional<String> longest = Stream.of("a", "bbb", "cc")
        .reduce((x, y) -> x.length() >= y.length() ? x : y);          // Optional[bbb]
```

---

### Q9. What is `Optional`? Why is it better than returning `null`?

**Answer:** "`Optional<T>` is a container that *might* hold a value or might be empty. It exists to fight the `NullPointerException` epidemic. Returning `null` is a silent landmine — the caller has no signal that the result might be absent, so they forget to check and crash at runtime. Returning `Optional` makes absence part of the *type*, forcing the caller to handle the empty case explicitly via `orElse`, `orElseThrow`, `ifPresent`, or `map`. The best practice: use `Optional` as a **return type** for methods that may not find a result — never as a method parameter or a class field."

```java
public Optional<Employee> findById(String id) { /* ... */ return Optional.empty(); }

String dept = findById("E1")
    .map(Employee::getDepartment)
    .orElse("Unknown");        // no NPE possible
```

> ⚠️ **Gotcha:** `Optional.of(null)` throws immediately — use `Optional.ofNullable(x)` when `x` might be null. And never call `.get()` without checking presence; it throws `NoSuchElementException` on empty.

---

### Q10. What are method references? What are the four types?

**Answer:** "A method reference is shorthand for a lambda that does nothing but call one existing method — written with `::`. If the lambda is `x -> System.out.println(x)`, the noise-free version is `System.out::println`. There are four flavors: **static** (`Integer::parseInt`), **instance method of a particular object** (`System.out::println`), **instance method of an arbitrary object of a type** (`String::toUpperCase`, where the stream element becomes the receiver), and **constructor** (`StringBuilder::new`). Rule of thumb: if your lambda just forwards its argument to one method, use a method reference; if it does any extra logic, keep the lambda."

```java
nums.stream().map(Integer::parseInt);     // 1. static
list.forEach(System.out::println);        // 2. instance of a specific object
words.stream().map(String::toUpperCase);  // 3. instance of arbitrary object (element is receiver)
words.stream().map(StringBuilder::new);   // 4. constructor
```

---

### Q11. What are default methods in interfaces? Why were they added?

**Answer:** "A default method is a method *with a body* declared in an interface using the `default` keyword. They were added for **backward compatibility**. When Java 8 wanted to bolt `stream()` and `forEach()` onto the `Collection` interface, adding them as plain abstract methods would have broken every existing class that implemented `Collection`. Default methods let the language **evolve interfaces without breaking implementers** — existing classes inherit the default automatically, and can override it if they want. The famous gotcha is the **diamond problem**: if a class implements two interfaces with the same default method, it must override the method and can disambiguate with `InterfaceName.super.method()`."

```java
interface Vehicle {
    void start();                                 // abstract
    default void honk() { System.out.println("Beep!"); }  // default — optional to override
}
class Car implements Vehicle {
    public void start() { System.out.println("Car on"); }
    // honk() inherited for free
}
```

---

### Q12. Can interfaces have static methods in Java 8? How do they differ from default methods?

**Answer:** "Yes — Java 8 added `static` methods in interfaces too. A **static** method belongs to the interface itself and is called as `InterfaceName.method()`; it's not inherited by implementing classes and can't be overridden. A **default** method belongs to instances, is inherited, and *can* be overridden. Static interface methods are great for utility/factory helpers that logically live with the interface — for example, `Comparator.comparing(...)` and `Stream.of(...)` are static interface methods."

```java
interface Calculator {
    int calc(int a, int b);
    static Calculator adder() { return (a, b) -> a + b; }   // static factory
    default Calculator thenNegate() { return (a, b) -> -calc(a, b); }  // default
}
```

---

### Q13. What are parallel streams? When should you (not) use them?

**Answer:** "A parallel stream splits the work across multiple CPU cores using the common ForkJoinPool — you opt in with `.parallelStream()` or `.parallel()`. Use them when you have a **large dataset**, **CPU-bound independent work**, and **no shared mutable state**. *Avoid* them for small collections (the splitting/merging overhead outweighs the gain), for **I/O-bound** work (threads just block), when **order matters**, or when your lambdas touch shared mutable state — that's a race condition. The big gotcha: parallel streams use a *shared* common pool by default, so one badly-behaved parallel stream can starve others in the same JVM."

```java
long primes = numbers.parallelStream()   // multiple cores
    .filter(MyMath::isPrime)
    .count();
```

> 🚫 **Never** do `list.parallelStream().forEach(results::add)` on a non-thread-safe list — collect with `.collect(Collectors.toList())` instead, which is parallel-safe.

---

### Q14. Why can a stream only be consumed once? What happens if you reuse it?

**Answer:** "A stream isn't a data structure — it's a *one-shot pipeline tied to a source*. Once a terminal operation runs, the stream is marked consumed. Reusing it throws `IllegalStateException: stream has already been operated upon or closed`. The fix is simple: create a fresh stream from the source each time. If you need the same pipeline repeatedly, wrap the source in a `Supplier<Stream<T>>` and call `get()` for each use."

```java
Stream<String> s = Stream.of("a", "b");
s.forEach(System.out::println);   // OK
// s.count();                     // IllegalStateException — already consumed
```

---

### Q15. What's the difference between `findFirst()` and `findAny()`? And `anyMatch` / `allMatch` / `noneMatch`?

**Answer:** "**`findFirst`** returns the first element in encounter order; **`findAny`** returns *any* element and is allowed to be non-deterministic — which lets parallel streams return whatever's fastest without respecting order. Both return `Optional` and short-circuit. The `*Match` family returns booleans and also short-circuits: **`anyMatch`** stops at the first true, **`allMatch`** stops at the first false, and **`noneMatch`** is the inverse of `anyMatch`. A subtle edge case: on an empty stream `allMatch` and `noneMatch` both return `true` (vacuous truth), while `anyMatch` returns `false`."

```java
boolean anyRich = emps.stream().anyMatch(e -> e.getSalary() > 200000);
boolean allAdult = emps.stream().allMatch(e -> e.getAge() >= 18);
Optional<Employee> any = emps.parallelStream().filter(e -> e.getAge() > 30).findAny();
```

---

### Q16. What's the difference between `Collectors.groupingBy` and `Collectors.partitioningBy`?

**Answer:** "Both bucket elements into a `Map`, but partitioning is the binary special case. **`groupingBy`** groups by *any* classifier function, so you get a `Map<K, List<T>>` with one entry per distinct key — like grouping employees by department. **`partitioningBy`** takes a `Predicate` and always produces exactly two buckets, `Map<Boolean, List<T>>` — `true` and `false` — and both keys always exist even if one side is empty. Both accept a **downstream collector** as a second argument, so you can count, average, or map within each bucket, e.g. `groupingBy(Employee::getDept, counting())`."

```java
Map<String, List<Employee>> byDept =
    emps.stream().collect(Collectors.groupingBy(Employee::getDepartment));

Map<Boolean, List<Employee>> highVsLow =
    emps.stream().collect(Collectors.partitioningBy(e -> e.getSalary() > 60000));
```

| | `groupingBy` | `partitioningBy` |
|---|--------------|------------------|
| Classifier | Any function → K | Predicate → boolean |
| Result keys | One per distinct value | Always exactly `true` & `false` |
| Empty bucket | Key may be absent | Both keys always present |

---

## 🧠 Memory Hooks — Vivid Analogies

| Feature | Analogy | Trigger Phrase |
|---------|---------|----------------|
| **Lambda** | Ordering on Swiggy — say *what* you want, skip the chef's résumé | "Behavior as a one-liner" |
| **Functional Interface** | A single-slot power socket — only one plug (abstract method) fits | "One abstract method = one lambda slot" |
| **Predicate** | Bouncer at a club — yes you're in, no you're out | "test → boolean" |
| **Function** | A juicer — fruit in, juice out (T → R) | "apply → transform" |
| **Consumer** | A shredder — paper in, nothing out | "accept → void" |
| **Supplier** | An ATM — push button, cash appears from nowhere | "get → value from nothing" |
| **Stream** | Maruti car factory assembly line — station to station | "Pipeline, not storage" |
| **Intermediate op** | Drawing the blueprint — nothing built yet | "Lazy recipe" |
| **Terminal op** | Pressing START on the assembly line | "Now it runs" |
| **Lazy evaluation** | Netflix streaming — loads only the frame you're watching | "Pull on demand, short-circuit" |
| **map** | Photocopier — one page → one copy | "1-to-1" |
| **flatMap** | Emptying many shopping bags into one big cart | "1-to-many, flattened" |
| **reduce** | Snowball rolling downhill — everything merges into one | "Fold into one value" |
| **Optional** | "May contain nuts" food label — forces you to check | "Absence in the type system" |
| **Method reference** | Speed-dial — one tap, no typing the number | "`::` = lambda shorthand" |
| **Default method** | Free pre-installed app on a new phone — there unless you replace it | "Evolve interfaces, don't break them" |
| **Parallel stream** | Multiple checkout counters open at the mega-mart | "Many cores, no shared cart" |

---

## 🎯 Final Interview Tip

When you get a Streams question, **narrate the pipeline out loud as a sentence** before you write code: *"I'll stream the employees, filter salary over 70k, map to names, sort, and collect to a list."* This does three things: it proves you think in **data transformations** (exactly what Java-8 rounds test), it lets the interviewer correct your approach *before* you commit to syntax, and it keeps you calm because you're describing intent, not fighting brackets.

And always close the loop on the **gotchas** unprompted — "this stream is single-use," "I'd return `Optional` here, not null," "I'd avoid a parallel stream on this small list." Volunteering the trade-off is the single clearest signal that you've written real Java 8 in production, not just studied it. You've got this. 🚀
