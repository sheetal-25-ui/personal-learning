# Week 3: Java Collections Framework — Interview Answers & Exercise Solutions

> **The Interview Golden Rule** 🏆
> For *every* Collections question, answer in this order:
> **1️⃣ Definition** (what it is, one crisp line) → **2️⃣ When to use which collection** (the decision) → **3️⃣ Code** (show, don't tell) → **4️⃣ Time complexity / trade-offs** (prove you understand the cost).
>
> Interviewers don't just want "ArrayList stores a list." They want: *"It's a resizable array — O(1) random access, O(n) middle insert. I'd pick it over LinkedList unless I'm constantly inserting at the front."* That's the difference between a junior and a senior answer.

This file has two parts:
- **PART A** — Full worked Java solutions to *every* practice exercise and the Week 3 project in `Plan.md`.
- **PART B** — 16 high-probability Collections interview questions with confident, spoken-style model answers, big-O tables, and comparison tables.

All code is plain Java (compilable on Java 8+ unless noted).

---

# PART A — Exercise / Practice Solutions

---

## 🍽️ Section 3 (List) Exercises

### Exercise 3.1 — ArrayList of 10 restaurants: sort, reverse, find the 5th

```java
import java.util.*;

public class RestaurantListDemo {
    public static void main(String[] args) {
        ArrayList<String> restaurants = new ArrayList<>(Arrays.asList(
            "Paradise Biryani", "Haldiram's", "Barbeque Nation", "Saravana Bhavan",
            "Bikanervala", "Mainland China", "Truffles", "Empire", "Karim's", "Sagar Ratna"
        ));

        // Sort alphabetically
        Collections.sort(restaurants);
        System.out.println("Sorted: " + restaurants);

        // Reverse the order
        Collections.reverse(restaurants);
        System.out.println("Reversed: " + restaurants);

        // Find the 5th restaurant (index 4 — lists are 0-based)
        String fifth = restaurants.get(4);
        System.out.println("5th restaurant: " + fifth);
    }
}
```
**Key point:** `get(4)` is O(1) on ArrayList — that's its superpower. Note "5th" = index 4.

---

### Exercise 3.2 — Benchmark: add 100,000 elements at the *beginning* of ArrayList vs LinkedList

```java
import java.util.*;

public class FrontInsertBenchmark {
    static final int N = 100_000; // WHY: large enough that O(n) vs O(1) front-insert is clearly visible

    public static void main(String[] args) {
        List<Integer> arrayList = new ArrayList<>();
        long start = System.nanoTime();
        for (int i = 0; i < N; i++) {
            arrayList.add(0, i); // insert at front — O(n), shifts everything right
        }
        long arrayListMs = (System.nanoTime() - start) / 1_000_000;

        List<Integer> linkedList = new LinkedList<>();
        start = System.nanoTime();
        for (int i = 0; i < N; i++) {
            linkedList.add(0, i); // insert at front — O(1), just relink head
        }
        long linkedListMs = (System.nanoTime() - start) / 1_000_000;

        System.out.println("ArrayList front-insert : " + arrayListMs + " ms");
        System.out.println("LinkedList front-insert: " + linkedListMs + " ms");
        System.out.println("LinkedList is ~" +
            (arrayListMs == 0 ? "much" : (arrayListMs / Math.max(1, linkedListMs))) + "x faster here");
    }
}
```
**Expected result:** LinkedList finishes in a few ms; ArrayList takes hundreds of ms to seconds, because each `add(0, i)` shifts all existing elements right (O(n) per insert → O(n²) total).

---

### Exercise 3.3 — "Recently Viewed Items" (max 5, newest at front, oldest drops off)

```java
import java.util.*;

public class RecentlyViewed {
    private static final int MAX_SIZE = 5; // WHY: product requirement — Flipkart shows last 5 viewed
    private final LinkedList<String> items = new LinkedList<>();

    public void view(String item) {
        items.remove(item);       // if already present, move it to the front (de-dupe)
        items.addFirst(item);     // newest at front — O(1) on a LinkedList
        if (items.size() > MAX_SIZE) {
            items.removeLast();   // drop the oldest — O(1)
        }
    }

    public List<String> getRecent() {
        return new ArrayList<>(items); // defensive copy
    }

    public static void main(String[] args) {
        RecentlyViewed rv = new RecentlyViewed();
        for (String p : new String[]{"Shoes", "Phone", "Laptop", "Watch", "Bag", "Headphones"}) {
            rv.view(p);
        }
        rv.view("Phone"); // re-view bumps it to the front
        System.out.println(rv.getRecent());
        // [Phone, Headphones, Bag, Watch, Laptop]  — "Shoes" dropped off
    }
}
```

---

## 🎟️ Section 4 (Set) Exercises

### Exercise 4.1 — Find unique restaurant names from a list with duplicates

```java
import java.util.*;

public class UniqueRestaurants {
    public static void main(String[] args) {
        List<String> withDupes = Arrays.asList(
            "Paradise", "Bawarchi", "Paradise", "Shah Ghouse", "Bawarchi", "Pista House"
        );

        Set<String> unique = new HashSet<>(withDupes); // constructor de-dupes for you
        System.out.println("Unique count: " + unique.size()); // 4
        System.out.println("Unique: " + unique);

        // If you want them sorted instead, use a TreeSet:
        Set<String> sortedUnique = new TreeSet<>(withDupes);
        System.out.println("Sorted unique: " + sortedUnique);
    }
}
```

---

### Exercise 4.2 — TreeSet leaderboard (sorted, no duplicates)

```java
import java.util.*;

public class Leaderboard {
    public static void main(String[] args) {
        TreeSet<Integer> scores = new TreeSet<>();
        int[] incoming = {780, 920, 850, 710, 920, 880}; // 920 appears twice
        for (int s : incoming) scores.add(s);

        System.out.println("Leaderboard (ascending): " + scores);            // [710, 780, 850, 880, 920]
        System.out.println("Top score: " + scores.last());                   // 920
        System.out.println("Lowest score: " + scores.first());               // 710

        // Descending leaderboard (most common display):
        System.out.println("Top to bottom: " + scores.descendingSet());      // [920, 880, 850, 780, 710]

        // Who scored 850 or above?
        System.out.println("850+: " + scores.tailSet(850));                  // [850, 880, 920]
    }
}
```

---

### Exercise 4.3 — LinkedHashSet to track road-trip cities in visit order

```java
import java.util.*;

public class RoadTrip {
    public static void main(String[] args) {
        LinkedHashSet<String> route = new LinkedHashSet<>();
        // We pass through Pune twice, but only want it listed once — in first-visit order
        String[] visits = {"Mumbai", "Pune", "Kolhapur", "Pune", "Goa", "Mumbai"};
        for (String city : visits) route.add(city);

        System.out.println("Trip route (insertion order, de-duped): " + route);
        // [Mumbai, Pune, Kolhapur, Goa]
    }
}
```

---

## 🗺️ Section 5 (Map) Exercises

### Exercise 5.1 — Word frequency counter

```java
import java.util.*;

public class WordFrequency {
    public static void main(String[] args) {
        String paragraph = "the cat sat on the mat the cat ran";
        Map<String, Integer> freq = new HashMap<>();

        for (String word : paragraph.toLowerCase().split("\\s+")) {
            // merge is the cleanest idiom: if absent put 1, else add 1
            freq.merge(word, 1, Integer::sum);
            // Equivalent classic form:
            // freq.put(word, freq.getOrDefault(word, 0) + 1);
        }

        System.out.println(freq); // {sat=1, ran=1, cat=2, on=1, mat=1, the=3}

        // Print sorted by count descending — a common follow-up ask:
        freq.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(e -> System.out.println(e.getKey() + " -> " + e.getValue()));
    }
}
```
**Interview gold:** mention `merge()` and `getOrDefault()` — they show you know the modern Map API and avoid the NPE-prone `map.get()` pattern.

---

### Exercise 5.2 — Phone book (lookup, add, update)

```java
import java.util.*;

public class PhoneBook {
    private final Map<String, String> contacts = new HashMap<>();

    public void addOrUpdate(String name, String number) {
        // put() inserts a new key OR overwrites an existing one — handles both cases
        contacts.put(name, number);
    }

    public String lookup(String name) {
        return contacts.getOrDefault(name, "Not found"); // avoids null / NPE
    }

    public static void main(String[] args) {
        PhoneBook pb = new PhoneBook();
        pb.addOrUpdate("Sheetal", "9876543210");
        pb.addOrUpdate("Priya", "9123456780");

        System.out.println(pb.lookup("Sheetal")); // 9876543210
        pb.addOrUpdate("Sheetal", "9000000000");   // update existing
        System.out.println(pb.lookup("Sheetal")); // 9000000000
        System.out.println(pb.lookup("Rahul"));    // Not found
    }
}
```

---

### Exercise 5.3 — Common elements between two arrays in O(n) using a HashSet

```java
import java.util.*;

public class CommonElements {
    public static Set<Integer> findCommon(int[] a, int[] b) {
        Set<Integer> seen = new HashSet<>();
        for (int x : a) seen.add(x);            // O(n) to load array a

        Set<Integer> common = new LinkedHashSet<>(); // preserve order, no dupes
        for (int y : b) {                        // O(m) to scan array b
            if (seen.contains(y)) common.add(y); // contains is O(1) average
        }
        return common; // total O(n + m), beats the naive O(n*m) nested loop
    }

    public static void main(String[] args) {
        int[] a = {1, 2, 3, 4, 5};
        int[] b = {4, 5, 6, 7, 4};
        System.out.println(findCommon(a, b)); // [4, 5]
    }
}
```

---

### Exercise 5.4 — TreeMap ranking system (alphabetical by student name)

```java
import java.util.*;

public class AlphabeticalRanking {
    public static void main(String[] args) {
        TreeMap<String, Integer> ranking = new TreeMap<>();
        ranking.put("Sheetal", 85);
        ranking.put("Aarav", 91);
        ranking.put("Priya", 78);
        ranking.put("Mohan", 88);

        // TreeMap keeps keys sorted automatically
        for (Map.Entry<String, Integer> e : ranking.entrySet()) {
            System.out.println(e.getKey() + " : " + e.getValue());
        }
        // Aarav : 91
        // Mohan : 88
        // Priya : 78
        // Sheetal : 85

        System.out.println("First alphabetically: " + ranking.firstKey()); // Aarav
        System.out.println("Last alphabetically : " + ranking.lastKey());  // Sheetal
    }
}
```

---

### Exercise 5.5 — LRU cache using LinkedHashMap (`removeEldestEntry`)

```java
import java.util.*;

public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    public LRUCache(int capacity) {
        // accessOrder = true → entries reorder on every get(), so least-recently-USED
        // (not just least-recently-inserted) ends up at the front and gets evicted.
        super(16, 0.75f, true);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity; // when true, the eldest (LRU) entry is auto-removed
    }

    public static void main(String[] args) {
        LRUCache<Integer, String> cache = new LRUCache<>(3);
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");
        cache.get(1);          // access 1 → now 2 is the least recently used
        cache.put(4, "D");     // capacity exceeded → evicts key 2

        System.out.println(cache.keySet()); // [3, 1, 4]  (2 was evicted)
    }
}
```
**Interview gold:** the magic is `accessOrder=true` in the constructor + overriding `removeEldestEntry`. This is the canonical "implement an LRU cache" answer — about 6 lines of real logic.

---

## 🏥 Section 6 (Queue) Exercises

### Exercise 6.1 — Zomato order queue (FIFO)

```java
import java.util.*;

public class OrderQueue {
    public static void main(String[] args) {
        Queue<String> orders = new LinkedList<>(); // LinkedList implements Queue
        orders.offer("Order #101 - Biryani");
        orders.offer("Order #102 - Pizza");
        orders.offer("Order #103 - Dosa");

        while (!orders.isEmpty()) {
            String current = orders.poll(); // removes & returns the head (oldest)
            System.out.println("Processing: " + current);
        }
        // Processing: Order #101 ... then 102 ... then 103  (FIFO)
    }
}
```

---

### Exercise 6.2 — Task scheduler: shortest duration first (PriorityQueue)

```java
import java.util.*;

public class TaskScheduler {
    record Task(String name, int durationMinutes) {}

    public static void main(String[] args) {
        // Min-heap ordered by duration — shortest task always comes out first
        PriorityQueue<Task> scheduler =
            new PriorityQueue<>(Comparator.comparingInt(Task::durationMinutes));

        scheduler.offer(new Task("Deploy", 30));
        scheduler.offer(new Task("Code review", 10));
        scheduler.offer(new Task("Write docs", 45));
        scheduler.offer(new Task("Fix typo", 2));

        while (!scheduler.isEmpty()) {
            Task t = scheduler.poll();
            System.out.println("Run: " + t.name() + " (" + t.durationMinutes() + " min)");
        }
        // Run: Fix typo (2) → Code review (10) → Deploy (30) → Write docs (45)
    }
}
```
*(`record` needs Java 16+. On Java 8, replace with a small class holding `name` and `durationMinutes`.)*

---

### Exercise 6.3 — Balanced parentheses using ArrayDeque as a stack

```java
import java.util.*;

public class BalancedParens {
    public static boolean isBalanced(String expr) {
        Deque<Character> stack = new ArrayDeque<>(); // modern stack — faster than legacy Stack
        Map<Character, Character> pairs = Map.of(')', '(', ']', '[', '}', '{');

        for (char c : expr.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
            } else if (pairs.containsKey(c)) {
                // closing bracket: top of stack must be its matching opener
                if (stack.isEmpty() || stack.pop() != pairs.get(c)) {
                    return false;
                }
            }
        }
        return stack.isEmpty(); // leftover openers → unbalanced
    }

    public static void main(String[] args) {
        System.out.println(isBalanced("{[()]}")); // true
        System.out.println(isBalanced("{[(])}")); // false
        System.out.println(isBalanced("((("));    // false
    }
}
```

---

## 🛒 Section 8 (Comparable / Comparator) Exercises

### Exercise 8.1 — FlipkartProduct: Comparable by price, Comparators by rating and name

```java
import java.util.*;

public class FlipkartProduct implements Comparable<FlipkartProduct> {
    String name;
    double price;
    double rating;

    public FlipkartProduct(String name, double price, double rating) {
        this.name = name;
        this.price = price;
        this.rating = rating;
    }

    // Natural ordering: by price ascending.
    // WHY Double.compare and not (price - other.price): avoids floating-point sign/precision bugs.
    @Override
    public int compareTo(FlipkartProduct other) {
        return Double.compare(this.price, other.price);
    }

    @Override
    public String toString() {
        return name + " (Rs " + price + ", " + rating + "★)";
    }

    public static void main(String[] args) {
        List<FlipkartProduct> products = new ArrayList<>(List.of(
            new FlipkartProduct("Phone", 19999, 4.3),
            new FlipkartProduct("Earbuds", 1999, 4.6),
            new FlipkartProduct("Laptop", 54999, 4.1)
        ));

        Collections.sort(products); // uses compareTo → by price ascending
        System.out.println("By price: " + products);

        products.sort(Comparator.comparingDouble((FlipkartProduct p) -> p.rating).reversed());
        System.out.println("By rating (high→low): " + products);

        products.sort(Comparator.comparing(p -> p.name));
        System.out.println("By name: " + products);
    }
}
```

---

### Exercise 8.2 — Sort strings by length (shortest first)

```java
import java.util.*;

public class SortByLength {
    public static void main(String[] args) {
        List<String> words = new ArrayList<>(List.of(
            "biryani", "dal", "paneer butter masala", "naan", "lassi"
        ));
        words.sort(Comparator.comparingInt(String::length));
        System.out.println(words);
        // [dal, naan, lassi, biryani, paneer butter masala]
    }
}
```

---

### Exercise 8.3 — Sort employees by department (A→Z), then salary (high→low)

```java
import java.util.*;

public class EmployeeSort {
    record Employee(String name, String dept, int salary) {}

    public static void main(String[] args) {
        List<Employee> emps = new ArrayList<>(List.of(
            new Employee("Asha",  "Sales",   60000),
            new Employee("Bharat","Eng",     90000),
            new Employee("Chitra","Sales",   75000),
            new Employee("Deepak","Eng",     90000),
            new Employee("Esha",  "Eng",     85000)
        ));

        emps.sort(
            Comparator.comparing(Employee::dept)                       // dept A→Z
                      .thenComparing(Comparator.comparingInt(Employee::salary).reversed()) // salary high→low
        );

        emps.forEach(e -> System.out.println(e.dept() + " | " + e.name() + " | " + e.salary()));
        // Eng | Bharat | 90000
        // Eng | Deepak | 90000
        // Eng | Esha   | 85000
        // Sales | Chitra | 75000
        // Sales | Asha   | 60000
    }
}
```
**Interview gold:** `Comparator.comparing(...).thenComparing(...)` is the clean, modern way to express multi-key sorts. Reversing only the salary key (not the whole comparator) is the subtle part interviewers look for.

---

## 🏆 Week 3 Practice Project — Flipkart Product Catalog System (full solution)

This one program touches **every** collection type: HashMap, TreeMap, HashSet, PriorityQueue, ArrayList, LinkedHashMap, plus Comparable/Comparator/equals/hashCode and Collections utilities.

```java
import java.util.*;
import java.util.stream.Collectors;

class Product implements Comparable<Product> {
    final int id;
    final String name;
    final String category;
    final double price;
    final double rating;

    Product(int id, String name, String category, double price, double rating) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.rating = rating;
    }

    // Natural ordering: by price ascending.
    @Override
    public int compareTo(Product other) {
        return Double.compare(this.price, other.price);
    }

    // Identity is the id ONLY — two products are "equal" if they share an id.
    // equals & hashCode must agree, or HashMap/HashSet break.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        return this.id == ((Product) o).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "#" + id + " " + name + " [" + category + "] Rs" + price + " " + rating + "★";
    }
}

public class FlipkartCatalog {
    // (2) O(1) lookup by id
    private final Map<Integer, Product> byId = new HashMap<>();
    // (3) products sorted by name
    private final TreeMap<String, Product> byName = new TreeMap<>();
    // (4) unique categories
    private final Set<String> categories = new HashSet<>();
    // (8) recently viewed — newest first, capped at 10
    private static final int RECENT_CAP = 10; // WHY: UX requirement — show last 10 viewed
    private final LinkedHashMap<Integer, Product> recentlyViewed =
        new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Product> e) {
                return size() > RECENT_CAP;
            }
        };

    // (10) Add a product, guarding against duplicates and nulls
    public void addProduct(Product p) {
        if (p == null) throw new IllegalArgumentException("Product cannot be null");
        if (byId.containsKey(p.id)) {
            System.out.println("Duplicate id " + p.id + " ignored");
            return;
        }
        byId.put(p.id, p);
        byName.put(p.name, p);
        categories.add(p.category);
    }

    // (2) O(1) lookup, records a view
    public Product getById(int id) {
        Product p = byId.get(id);
        if (p != null) recentlyViewed.put(id, p); // touch → marks as recently used
        return p;
    }

    // (7) Search by name substring (case-insensitive)
    public List<Product> searchByName(String q) {
        if (q == null || q.isBlank()) return List.of();
        String needle = q.toLowerCase();
        return byId.values().stream()
            .filter(p -> p.name.toLowerCase().contains(needle))
            .collect(Collectors.toList());
    }

    // (7) Search by category
    public List<Product> searchByCategory(String category) {
        return byId.values().stream()
            .filter(p -> p.category.equalsIgnoreCase(category))
            .collect(Collectors.toList());
    }

    // (7) Search by price range [min, max]
    public List<Product> searchByPriceRange(double min, double max) {
        return byId.values().stream()
            .filter(p -> p.price >= min && p.price <= max)
            .sorted() // by price, via Comparable
            .collect(Collectors.toList());
    }

    // (5) Top-N cheapest using a PriorityQueue (min-heap by price)
    public List<Product> topCheapest(int n) {
        PriorityQueue<Product> minHeap = new PriorityQueue<>(byId.values()); // uses compareTo
        List<Product> result = new ArrayList<>();
        for (int i = 0; i < n && !minHeap.isEmpty(); i++) {
            result.add(minHeap.poll());
        }
        return result;
    }

    // (6) Flexible sorting via Comparators
    public List<Product> sortedByPriceAsc() {
        List<Product> list = new ArrayList<>(byId.values());
        list.sort(Comparator.comparingDouble(p -> p.price));
        return list;
    }

    public List<Product> sortedByRatingDesc() {
        List<Product> list = new ArrayList<>(byId.values());
        list.sort(Comparator.comparingDouble((Product p) -> p.rating).reversed());
        return list;
    }

    public List<Product> sortedByName() {
        return new ArrayList<>(byName.values()); // TreeMap already sorted by name
    }

    // (9) Collections utility methods: min / max price
    public Product cheapest() {
        return Collections.min(byId.values()); // uses compareTo (price)
    }

    public Product mostExpensive() {
        return Collections.max(byId.values());
    }

    // (9) Count products per category
    public Map<String, Long> countPerCategory() {
        return byId.values().stream()
            .collect(Collectors.groupingBy(p -> p.category, Collectors.counting()));
    }

    public Set<String> getCategories() {
        return Collections.unmodifiableSet(categories); // read-only view
    }

    public List<Product> getRecentlyViewed() {
        // LinkedHashMap with accessOrder=true lists oldest→newest; reverse for newest-first display
        List<Product> list = new ArrayList<>(recentlyViewed.values());
        Collections.reverse(list);
        return list;
    }

    // ---- Demo ----
    public static void main(String[] args) {
        FlipkartCatalog cat = new FlipkartCatalog();
        cat.addProduct(new Product(1, "iPhone 15", "Mobiles", 79999, 4.6));
        cat.addProduct(new Product(2, "Galaxy S24", "Mobiles", 74999, 4.4));
        cat.addProduct(new Product(3, "MacBook Air", "Laptops", 99999, 4.7));
        cat.addProduct(new Product(4, "Noise Buds", "Audio", 1499, 4.1));
        cat.addProduct(new Product(5, "Boat Rockerz", "Audio", 1299, 4.0));
        cat.addProduct(new Product(1, "Dupe", "Mobiles", 1, 1)); // (10) duplicate id ignored

        System.out.println("By price:  " + cat.sortedByPriceAsc());
        System.out.println("By rating: " + cat.sortedByRatingDesc());
        System.out.println("By name:   " + cat.sortedByName());
        System.out.println("Top 3 cheapest: " + cat.topCheapest(3));
        System.out.println("Categories: " + cat.getCategories());
        System.out.println("Audio search: " + cat.searchByCategory("Audio"));
        System.out.println("Name 'a' search: " + cat.searchByName("a"));
        System.out.println("Rs 1000-80000: " + cat.searchByPriceRange(1000, 80000));
        System.out.println("Cheapest: " + cat.cheapest());
        System.out.println("Priciest: " + cat.mostExpensive());
        System.out.println("Per category: " + cat.countPerCategory());

        cat.getById(3); cat.getById(1); cat.getById(4); // simulate views
        System.out.println("Recently viewed: " + cat.getRecentlyViewed());

        System.out.println("Empty search: " + cat.searchByName("")); // (10) edge case → []
    }
}
```
**What makes this a senior-level answer:** `equals`/`hashCode` keyed on `id` (so duplicate detection works in HashMap/HashSet), `Double.compare` for price, `removeEldestEntry` for the bounded recently-viewed cache, defensive null/blank handling, and `Collections.unmodifiableSet` to hand out read-only views.

---

# PART B — Interview Questions & Model Answers

> Answer style: short, confident, structured. Open with the one-line definition, give the decision/trade-off, then a tiny example if asked.

---

### Q1. ArrayList vs LinkedList — when do you use each?

**Model answer:** "ArrayList is a resizable array — backed by a contiguous block of memory. LinkedList is a doubly-linked list of nodes. The practical difference is access vs. insertion. ArrayList gives O(1) random access by index but O(n) insertion/removal in the middle because it shifts elements. LinkedList gives O(1) insertion/removal at the ends but O(n) access because you walk the chain. In practice I use ArrayList about 95% of the time — it's more cache-friendly and uses less memory. I only reach for LinkedList when I'm constantly adding/removing at the front, or using it as a Queue/Deque."

| Operation | ArrayList | LinkedList |
|-----------|-----------|------------|
| `get(i)` | **O(1)** | O(n) |
| `add` at end | O(1)* | O(1) |
| `add`/`remove` at front | O(n) | **O(1)** |
| `remove` in middle | O(n) | O(n)** |
| Memory/element | low (just value) | high (value + 2 pointers) |
| Cache locality | excellent | poor |

\*amortized · \**O(1) if you already hold the node reference

---

### Q2. How does HashMap work internally?

**Model answer:** "A HashMap is an array of buckets — default 16. On `put(key, value)`, it calls `key.hashCode()`, spreads the bits (a perturbation/`spread` step to reduce collisions), then maps it to a bucket index using `hash & (n-1)`. If the bucket is empty, the entry goes in. If not — a collision — the entry is appended to that bucket. Before Java 8 buckets were linked lists; since Java 8, once a bucket holds 8+ entries *and* the table is at least 64 in size, it's converted (treeified) into a balanced red-black tree, so worst-case lookup drops from O(n) to O(log n). When the map gets 75% full — the load factor — it doubles the bucket array and rehashes everything. On `get`, it recomputes the index, then walks the bucket using `equals()` to find the exact key. That's why correct `hashCode()` *and* `equals()` are mandatory for keys."

Average case: **O(1)** for put/get/remove. Worst case with bad hashCode: O(n), or O(log n) after treeify.

---

### Q3. HashMap vs Hashtable vs ConcurrentHashMap?

| Feature | HashMap | Hashtable | ConcurrentHashMap |
|---------|---------|-----------|-------------------|
| Thread-safe | ❌ No | ✅ Yes (locks whole map) | ✅ Yes (fine-grained) |
| Null key | ✅ 1 allowed | ❌ No | ❌ No |
| Null values | ✅ Yes | ❌ No | ❌ No |
| Performance | Fastest (single-thread) | Slow (one big lock) | Fast under concurrency |
| Locking | none | synchronizes every method | bucket/CAS-level (Java 8+) |
| When | single-threaded | never (legacy) | multi-threaded |

**Model answer:** "HashMap is the default for single-threaded code — fastest, allows one null key. Hashtable is legacy: it's thread-safe but synchronizes the entire map on every operation, so it doesn't scale; I never use it. ConcurrentHashMap is the modern choice for concurrency — in Java 8+ it locks only individual buckets (using CAS and synchronized on bins) instead of the whole map, so multiple threads can write to different buckets simultaneously. Neither Hashtable nor ConcurrentHashMap allows null keys or values, because a null return from `get` becomes ambiguous in a concurrent setting."

---

### Q4. HashSet vs LinkedHashSet vs TreeSet?

| Set | Order | Duplicates | add/contains/remove | Backed by |
|-----|-------|-----------|---------------------|-----------|
| HashSet | none | no | **O(1)** avg | HashMap |
| LinkedHashSet | insertion order | no | **O(1)** avg | LinkedHashMap |
| TreeSet | sorted | no | O(log n) | TreeMap (red-black tree) |

**Model answer:** "All three store unique elements. HashSet is fastest but unordered. LinkedHashSet keeps insertion order using a linked list threaded through the entries, at a small memory cost. TreeSet keeps elements sorted by natural order or a Comparator, with O(log n) operations because it's a red-black tree — and it gives you bonus navigation methods like `first`, `last`, `headSet`, `ceiling`, and `floor`. Pick HashSet by default, LinkedHashSet when order of insertion matters, TreeSet when you need sorted order or range queries."

---

### Q5. Explain the equals() and hashCode() contract.

**Model answer:** "The contract has three rules. One: if two objects are equal by `equals()`, they **must** have the same `hashCode()`. Two: if hashCodes are equal, the objects *may or may not* be equal — collisions are allowed. Three: both must be consistent — same inputs, same result every time. The practical consequence: if you override `equals()` you **must** override `hashCode()` together, otherwise hash-based collections break. For example, two 'equal' objects with different hashCodes could both end up in a HashSet as duplicates, or you'd `put` with a key and never be able to `get` it back. The reverse — equal hashCodes but unequal objects — is fine; that's just a collision, and `equals()` disambiguates within the bucket."

---

### Q6. Comparable vs Comparator?

| | Comparable | Comparator |
|---|-----------|------------|
| Package | `java.lang` | `java.util` |
| Method | `compareTo(T o)` | `compare(T a, T b)` |
| Where defined | inside the class | external / lambda |
| Orderings | one (the *natural* order) | many |
| Use when | one obvious sort order | multiple/flexible sorts, or you can't edit the class |

**Model answer:** "Comparable defines a single natural ordering *inside* the class — like Student sorting by marks. Comparator defines ordering *externally*, so you can have many different sorts and even sort classes you don't own. I implement Comparable when there's one obvious order, and use Comparator (especially the lambda form `Comparator.comparing(...).thenComparing(...)`) when I need to sort by different keys at different times. Tip: use `Integer.compare`/`Double.compare` instead of subtraction to avoid overflow and floating-point bugs."

---

### Q7. Fail-fast vs fail-safe iterators?

**Model answer:** "A fail-fast iterator throws `ConcurrentModificationException` the moment it detects the collection was structurally modified during iteration — it tracks a `modCount` and compares it on each `next()`. ArrayList, HashMap, HashSet iterators are fail-fast. A fail-safe iterator never throws; it iterates over a snapshot or a separate copy of the data, so changes made during iteration aren't reflected. `CopyOnWriteArrayList` and `ConcurrentHashMap` are fail-safe. The catch with fail-safe is you may iterate over slightly stale data. If I need to remove during iteration on a normal collection, I use the iterator's own `remove()` — that updates modCount correctly and doesn't trip the check."

```java
// Safe removal during iteration:
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    if (it.next().equals("Delhi")) it.remove(); // ✅ no exception
}
```

---

### Q8. What is the load factor, and why 0.75?

**Model answer:** "Load factor is the threshold — by default 0.75 — at which a HashMap resizes. With 16 buckets, once 12 entries are stored, the map doubles to 32 buckets and rehashes everything. 0.75 is the sweet spot between time and space: lower (say 0.5) wastes memory with too many empty buckets but reduces collisions; higher (say 1.0) saves space but increases collisions and slows lookups. 0.75 keeps average lookup close to O(1) while keeping memory reasonable. If I know I'll store N entries, I pre-size the map to `N / 0.75 + 1` to avoid repeated rehashing."

---

### Q9. How does HashMap handle collisions and what is treeify?

**Model answer:** "When two keys map to the same bucket, that's a collision. HashMap chains them in the bucket — historically a linked list, walked with `equals()` to find the right key. The problem: with many collisions, that bucket degrades to O(n). Java 8 fixed this with treeification — when a single bucket reaches 8 entries (`TREEIFY_THRESHOLD`) *and* the table size is at least 64, that bucket is converted from a linked list to a red-black tree, giving O(log n) worst-case lookup. If the bucket later shrinks below 6 (`UNTREEIFY_THRESHOLD`), it reverts to a list. So a maliciously crafted hash-collision attack can no longer degrade lookups to O(n)."

---

### Q10. Why must HashMap keys be immutable?

**Model answer:** "Because the bucket index is computed from the key's `hashCode()` at `put` time. If you mutate the key afterward in a way that changes its hashCode, it now hashes to a *different* bucket than where it was stored — so `get` looks in the wrong bucket and returns null, even though the entry is still in the map. It's effectively lost. That's why immutable types like String and Integer are ideal keys. If you use a custom class as a key, make the fields used in `hashCode`/`equals` final, or never mutate them while it's in a map."

---

### Q11. Why is String a good HashMap key?

**Model answer:** "Three reasons. It's immutable, so its hashCode can't change after insertion — no lost-key bug. Its hashCode is cached after first computation, so repeated lookups are cheap. And String has well-distributed, correct `hashCode()` and `equals()` implementations built in. That combination makes it the most common and safest map key in Java."

---

### Q12. ConcurrentModificationException — what causes it and how do you avoid it?

**Model answer:** "It's thrown by fail-fast iterators when you structurally modify a collection — add or remove — while iterating it with a for-each loop. The iterator detects that `modCount` changed underneath it and bails out to avoid undefined behavior. To avoid it: use the iterator's own `remove()`; or use `removeIf()` (Java 8+); or iterate over a copy; or use a concurrent/copy-on-write collection if you're in a multithreaded context."

```java
list.removeIf(city -> city.equals("Delhi")); // ✅ clean, no exception
```

---

### Q13. Array vs ArrayList?

| | Array | ArrayList |
|---|-------|-----------|
| Size | fixed at creation | grows dynamically |
| Type | primitives or objects | objects only (autoboxing for primitives) |
| Length | `array.length` | `list.size()` |
| Performance | slightly faster, no overhead | small overhead, resizing cost |
| Generics | covariant, no generics | fully generic, type-safe |

**Model answer:** "An array is fixed-size and can hold primitives directly. An ArrayList is a resizable wrapper around an array — it grows automatically (by ~50% when full), holds only objects (primitives are autoboxed), and is type-safe via generics. I use a plain array for fixed-size, performance-critical primitive data; otherwise ArrayList for its convenience and resizing."

---

### Q14. HashMap vs TreeMap vs LinkedHashMap?

| Feature | HashMap | LinkedHashMap | TreeMap |
|---------|---------|---------------|---------|
| Order | none | insertion (or access) order | sorted by key |
| put/get | **O(1)** | O(1) | O(log n) |
| Null key | 1 allowed | 1 allowed | ❌ none |
| Backed by | bucket array | bucket array + linked list | red-black tree |
| Use when | speed | order + speed / LRU caches | sorted keys, range queries |

**Model answer:** "HashMap is the default — fastest, unordered. LinkedHashMap adds predictable iteration order (insertion order, or access order if you want an LRU cache) at slight memory cost. TreeMap keeps keys sorted and gives navigation methods like `firstKey`, `floorKey`, `ceilingKey`, `subMap` — but at O(log n) and with no null keys, since it must compare keys. I pick based on whether I need raw speed, order preservation, or sorted/range access."

---

### Q15. What is the difference between `poll()`/`peek()` and `remove()`/`element()` in a Queue?

**Model answer:** "They do the same thing — `poll`/`remove` retrieve and remove the head, `peek`/`element` retrieve without removing. The difference is failure behavior on an empty queue: `poll` and `peek` return `null`, while `remove` and `element` throw an exception (`NoSuchElementException`). I prefer `poll`/`peek` for normal flow control because they let me check `null` instead of catching exceptions. Same idea with `offer` vs `add` for insertion — `offer` returns false on a capacity-bounded queue instead of throwing."

---

### Q16. Why prefer ArrayDeque over the legacy Stack class?

**Model answer:** "`Stack` extends `Vector`, which means every operation is synchronized — slow, even single-threaded — and it exposes index-based methods that break the stack abstraction (you can poke at the middle of a 'stack'). `ArrayDeque` is the modern replacement: it's not synchronized, so it's faster, and it implements `Deque`, giving you clean `push`/`pop`/`peek` for stack use and `offer`/`poll` for queue use. The official Javadoc itself recommends ArrayDeque over Stack. The only reason to keep Stack is legacy code."

---

# 🧠 Memory Hooks

| Collection | Pick it when… | Vivid analogy |
|------------|---------------|---------------|
| **ArrayList** | random access by index, mostly reads/append | 🎬 Numbered cinema seats — jump straight to seat 50, but inserting a seat shifts everyone |
| **LinkedList** | constant add/remove at the ends, queue/deque | 🚂 Train bogies — easy to splice one in the middle, slow to count to bogie 50 |
| **HashSet** | uniqueness + speed, order irrelevant | 🗳️ Voter ID list — every name once, found instantly, no order |
| **LinkedHashSet** | uniqueness + remember insertion order | 🧳 Road-trip cities visited — each once, in the order you arrived |
| **TreeSet** | uniqueness + always sorted / ranges | 🏆 Auto-sorting leaderboard — always ranked, no ties listed twice |
| **HashMap** | key→value, raw speed | 📒 Phone contacts — name → number, instant lookup |
| **LinkedHashMap** | key→value + order (or LRU cache) | 🍽️ A printed menu — items stay in the order they were listed |
| **TreeMap** | key→value, keys sorted / range queries | 📖 A dictionary — words sorted A→Z, easy to find "all words from D to F" |
| **PriorityQueue** | always pull the most urgent next | 🏥 ER triage — most critical patient treated first, not first-arrived |
| **ArrayDeque** | fast stack OR queue, both ends | 🍽️ Stack of plates — add/take from top; or a ticket line from the front |
| **Hashtable** | …basically never | 🦕 A fossil — works, but extinct; use ConcurrentHashMap |
| **ConcurrentHashMap** | key→value across many threads | 🏪 Supermarket with many checkout lanes — shoppers at different lanes don't block each other |

**One-line decision tree to memorize:**
> Need key→value? → **Map** (HashMap default, TreeMap sorted, LinkedHashMap ordered, ConcurrentHashMap threaded).
> Need uniqueness? → **Set** (HashSet default, TreeSet sorted, LinkedHashSet ordered).
> Else ordered with dupes? → **List** (ArrayList default, LinkedList for front-ops) or **Queue/Deque** (PriorityQueue for priority, ArrayDeque for FIFO/LIFO).

---

# 🎯 Final Interview Tip

When asked "which collection would you use?", **never just name one — narrate the trade-off out loud.** Say *"I'd start with a HashMap for O(1) lookups; if the interviewer later needs sorted keys I'd switch to a TreeMap and accept O(log n)."* That single sentence proves three things at once: you know the default, you know the alternative, and you understand the *cost* of the swap. Pair every collection with its big-O and one reason you'd swap it, and you'll sound like someone who has actually shipped code — which is exactly who they want to hire. 🚀
