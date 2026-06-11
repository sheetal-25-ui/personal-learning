# Week 3: Java Collections Framework

---

## 1. What is the Collections Framework?

### What is it?
The Collections Framework is Java's organized toolbox of data structures. Instead of building your own linked list, hash table, or sorted set from scratch, Java gives you ready-made, tested, optimized versions.

### Real-life analogy
Think of it like a kitchen in an Indian household:
- You have different containers for different purposes: a steel dabba for rice, a glass jar for pickles, a masala box with compartments
- Each container is designed for a specific use — you wouldn't store pickle in a cloth bag
- The Collections Framework is your kitchen — it gives you the right container for every kind of data

### Why does it exist? What problem does it solve?
**Without Collections Framework:**
- You'd write your own resizable array (buggy, slow)
- You'd write your own hash table (complex, error-prone)
- Every developer's implementation would be different
- No standard way to pass data structures between libraries

**With Collections Framework:**
- Battle-tested implementations used by millions of developers
- Standard interfaces — any List can be swapped for another List
- Optimized for performance
- Generics provide type safety

### Python comparison
```python
# Python gives you built-in data structures
my_list = [1, 2, 3]        # Like Java's ArrayList
my_set = {1, 2, 3}         # Like Java's HashSet
my_dict = {"a": 1, "b": 2} # Like Java's HashMap
```

```java
// Java — you choose specific implementations
ArrayList<Integer> list = new ArrayList<>();
HashSet<Integer> set = new HashSet<>();
HashMap<String, Integer> map = new HashMap<>();
```

The key difference: Python gives you ONE list type, ONE dict type. Java gives you MANY — each optimized for different use cases. Knowing which to use is an interview skill.

---

## 2. Collection Hierarchy — The Family Tree

Here's how all the collections are related:

```
                        Iterable
                           |
                       Collection
                      /    |     \
                   List   Set    Queue
                  / | \    |  \     |  \
        ArrayList  |  \  HashSet TreeSet  PriorityQueue  Deque
        LinkedList |   \  LinkedHashSet                  ArrayDeque
                Vector  \
                 |     (not commonly used)
                Stack
                (legacy)


                         Map (separate hierarchy — NOT a Collection!)
                        / | \
                  HashMap  TreeMap  LinkedHashMap
                    |
              ConcurrentHashMap
```

### The interfaces (contracts)
- **Collection** — The root. "I hold a group of elements."
- **List** — "I'm ordered and allow duplicates." (like a queue at a chai stall — order matters, same person can come twice)
- **Set** — "I have no duplicates." (like a guest list — each name appears once)
- **Queue** — "First in, first out." (like a movie ticket line)
- **Map** — "I store key-value pairs." (like a dictionary — look up a word, get a meaning)

---

## 3. List Interface

### ArrayList — The Workhorse

#### What is it?
ArrayList is a resizable array. Unlike a regular array (fixed size), ArrayList grows automatically when you add elements. It's the most commonly used collection in Java.

#### Real-life analogy
Think of your phone's photo gallery. You can keep adding photos without deciding the total count upfront. The gallery expands as needed. But if you want to see photo #50, you can jump to it instantly — that's random access.

#### Basic usage
```java
import java.util.ArrayList;

// Create an ArrayList of Strings
ArrayList<String> restaurants = new ArrayList<>();

// Add elements
restaurants.add("Paradise Biryani");      // Index 0
restaurants.add("Haldiram's");            // Index 1
restaurants.add("Barbeque Nation");        // Index 2
restaurants.add("Saravana Bhavan");        // Index 3

// Access by index — O(1) time
String first = restaurants.get(0);  // "Paradise Biryani"

// Size
int count = restaurants.size();  // 4

// Update
restaurants.set(1, "Haldiram's Nagpur");  // Replace index 1

// Remove
restaurants.remove(2);              // Remove by index
restaurants.remove("Haldiram's Nagpur");  // Remove by value

// Check if exists
boolean hasSaravana = restaurants.contains("Saravana Bhavan");  // true

// Iterate
for (String restaurant : restaurants) {
    System.out.println(restaurant);
}
```

#### How ArrayList grows internally (INTERVIEW QUESTION)
1. ArrayList starts with an internal array of capacity 10 (by default)
2. When it's full and you add element #11, ArrayList creates a NEW array that's 50% bigger (capacity 15)
3. It copies all elements from the old array to the new one
4. The old array is garbage collected

```
Initial:  [_, _, _, _, _, _, _, _, _, _]  (capacity 10, size 0)
After 10: [a, b, c, d, e, f, g, h, i, j]  (capacity 10, size 10)
Add 11th: [a, b, c, d, e, f, g, h, i, j, k, _, _, _, _]  (NEW array, capacity 15, size 11)
```

**Performance implication:** That copy operation is expensive for large lists. If you know you'll have 10,000 elements, tell ArrayList upfront:
```java
ArrayList<String> bigList = new ArrayList<>(10000);  // Start with capacity 10000
```

#### Python comparison
```python
my_list = ["Paradise", "Haldiram's", "BBQ Nation"]
my_list.append("Saravana Bhavan")  # Add
my_list[0]                         # Access
my_list.remove("Haldiram's")       # Remove
len(my_list)                       # Size
```

```java
ArrayList<String> list = new ArrayList<>();
list.add("Paradise");        // append
list.get(0);                 // Access (not [] brackets!)
list.remove("Haldiram's");   // Remove
list.size();                 // Size (not len()!)
```

### LinkedList — When Insertion Matters More Than Access

#### What is it?
A doubly linked list. Each element points to the previous and next element. Great for frequent insertions/deletions at the beginning or middle. Bad for random access.

#### Real-life analogy
Think of a train with bogies:
- Adding a bogie in the middle: just disconnect two bogies and attach the new one between them. Easy!
- Finding the 50th bogie: you have to walk through bogies 1 through 49 to get there. Slow!

Compare with ArrayList (more like numbered seats in a cinema hall):
- Finding seat 50: just walk directly to it. Fast!
- Adding a seat in the middle: everyone after it must shift. Slow!

#### When to use which
```java
// Use ArrayList when:
// - You access elements by index frequently (get, set)
// - You mostly add to the end
// - You read more than you write

// Use LinkedList when:
// - You frequently add/remove at the beginning or middle
// - You process elements sequentially (no random access)
// - You use it as a Queue or Deque
```

### ArrayList vs LinkedList — Time Complexity

| Operation | ArrayList | LinkedList |
|-----------|-----------|------------|
| `get(index)` | O(1) | O(n) |
| `add(element)` at end | O(1) amortized | O(1) |
| `add(index, element)` at beginning | O(n) | O(1) |
| `remove(index)` at beginning | O(n) | O(1) |
| `contains(element)` | O(n) | O(n) |
| Memory per element | Less (just the value) | More (value + 2 pointers) |

**Rule of thumb:** Use ArrayList 95% of the time. LinkedList only when you frequently add/remove at the beginning.

### Practice exercises
1. Create an ArrayList of your favorite 10 Zomato restaurants. Sort them alphabetically, reverse the order, and find the 5th restaurant.
2. Compare the time to add 100,000 elements at the beginning of an ArrayList vs LinkedList (use `System.nanoTime()` to measure)
3. Implement a "Recently Viewed Items" list (like Flipkart) using LinkedList — max 5 items, new items go to front, oldest drops off

---

## 4. Set Interface

### What is it?
A Set is a collection that stores UNIQUE elements only. If you try to add a duplicate, it's silently ignored.

### Real-life analogy
Think of a Voter ID list. Each person's name appears exactly once. Even if Rahul tries to register twice, the list keeps only one entry.

### HashSet — Fast, Unordered

```java
import java.util.HashSet;

HashSet<String> cuisines = new HashSet<>();
cuisines.add("North Indian");
cuisines.add("Chinese");
cuisines.add("South Indian");
cuisines.add("North Indian");  // Duplicate — ignored!

System.out.println(cuisines.size());  // 3 (not 4!)
System.out.println(cuisines);  // [Chinese, South Indian, North Indian]
// NOTE: Order is NOT guaranteed! It might print in any order.

// Check if exists
boolean hasChinese = cuisines.contains("Chinese");  // true — O(1) time!

// Remove
cuisines.remove("Chinese");
```

**How HashSet works internally:**
HashSet uses a HashMap behind the scenes! Each element becomes a key in the HashMap with a dummy value. When you call `contains()`, it uses hashing to find the element in O(1) time.

### LinkedHashSet — Unique + Maintains Insertion Order

```java
import java.util.LinkedHashSet;

LinkedHashSet<String> visited = new LinkedHashSet<>();
visited.add("Mumbai");
visited.add("Delhi");
visited.add("Bangalore");
visited.add("Mumbai");  // Duplicate — ignored, but order preserved

System.out.println(visited);  // [Mumbai, Delhi, Bangalore] — always in insertion order!
```

### TreeSet — Unique + Sorted

```java
import java.util.TreeSet;

TreeSet<Integer> scores = new TreeSet<>();
scores.add(78);
scores.add(92);
scores.add(85);
scores.add(71);
scores.add(92);  // Duplicate — ignored

System.out.println(scores);  // [71, 78, 85, 92] — always sorted!

// TreeSet bonus methods:
scores.first();    // 71 (smallest)
scores.last();     // 92 (largest)
scores.headSet(85);  // [71, 78] (elements before 85)
scores.tailSet(85);  // [85, 92] (elements from 85 onwards)
```

### When to use which Set

| Set | Duplicates | Order | Time complexity | Use when |
|-----|-----------|-------|----------------|----------|
| HashSet | No | None | O(1) for add/remove/contains | You just need uniqueness and speed |
| LinkedHashSet | No | Insertion order | O(1) for add/remove/contains | Uniqueness + remembering the order you added |
| TreeSet | No | Sorted | O(log n) for add/remove/contains | Uniqueness + elements always sorted |

### Python comparison
```python
my_set = {1, 2, 3}           # Like HashSet (unordered)
# Python doesn't have LinkedHashSet or TreeSet built-in
# For sorted: sorted(my_set) gives a sorted list
```

### Practice exercises
1. Given a list of Swiggy restaurant names with duplicates, use a Set to find all unique restaurants
2. Use a TreeSet to maintain a leaderboard of scores — always sorted, no duplicates
3. Use LinkedHashSet to track the order in which cities were visited on a road trip

---

## 5. Map Interface (MOST IMPORTANT FOR INTERVIEWS)

### What is it?
A Map stores key-value pairs. Every key is unique, and each key maps to exactly one value. Think of it like a dictionary: you look up a word (key) and get its meaning (value).

### Real-life analogy
Think of a contact list on your phone:
- Key = Person's name ("Sheetal")
- Value = Phone number ("9876543210")
- Each name (key) maps to one number (value)
- Two people can have different names but the same number
- Two people CANNOT have the same name in the same contact list (keys are unique)

### HashMap — The Most Used Collection in Java

#### Basic usage
```java
import java.util.HashMap;

// Create: HashMap<KeyType, ValueType>
HashMap<String, Integer> menu = new HashMap<>();

// Put (add key-value pairs)
menu.put("Biryani", 250);
menu.put("Butter Chicken", 320);
menu.put("Dal Makhani", 180);
menu.put("Naan", 40);

// Get (retrieve value by key) — O(1)
int biryaniPrice = menu.get("Biryani");  // 250

// Check if key exists
boolean hasNaan = menu.containsKey("Naan");  // true

// Check if value exists
boolean has250 = menu.containsValue(250);  // true

// Size
int itemCount = menu.size();  // 4

// Remove
menu.remove("Naan");

// Update (put with existing key overwrites)
menu.put("Biryani", 280);  // Price updated from 250 to 280

// Get with default value (if key doesn't exist)
int price = menu.getOrDefault("Pizza", 0);  // 0 (Pizza doesn't exist)

// Iterate over entries
for (Map.Entry<String, Integer> entry : menu.entrySet()) {
    System.out.println(entry.getKey() + " : Rs " + entry.getValue());
}

// Iterate over keys only
for (String item : menu.keySet()) {
    System.out.println(item);
}

// Iterate over values only
for (int p : menu.values()) {
    System.out.println(p);
}
```

#### How HashMap Works Internally (INTERVIEW FAVORITE)

This is asked in almost every senior Java interview. Understanding this deeply will set you apart.

**Step 1: The bucket array**
HashMap internally uses an array of "buckets" (default size 16). Each bucket can hold one or more entries.

```
Index:  [0] [1] [2] [3] [4] [5] [6] [7] [8] [9] [10] [11] [12] [13] [14] [15]
         |        |                   |
       entry    entry              entry
```

**Step 2: The hash function**
When you do `map.put("Biryani", 250)`:
1. Java calls `"Biryani".hashCode()` — gets a number (like 726533405)
2. It then applies: `index = hashCode % arraySize` → e.g., `726533405 % 16 = 13`
3. The key-value pair goes into bucket 13

**Step 3: Collision handling**
What if two keys hash to the same index? This is called a "collision."

Before Java 8: Each bucket is a linked list. Colliding entries are chained together.
After Java 8: If a bucket has more than 8 entries, it converts from a linked list to a balanced tree (for O(log n) instead of O(n) lookup).

```
Bucket 13: "Biryani"->250 → "Paneer"->200 → "Dosa"->150  (linked list of collisions)
```

**Step 4: Resizing (rehashing)**
When the HashMap is 75% full (load factor = 0.75), it doubles the array size and rehashes all entries.
- 16 buckets → 12 entries triggers resize → 32 buckets
- This is expensive but happens infrequently

**Step 5: get() operation**
When you do `map.get("Biryani")`:
1. Compute hashCode of "Biryani" → index 13
2. Go to bucket 13
3. Walk the linked list/tree, using `.equals()` to find the exact key
4. Return the value

**This is why `hashCode()` and `equals()` must be correct for HashMap keys!**

#### Python comparison
```python
# Python dict IS a hash map
menu = {"Biryani": 250, "Naan": 40}
menu["Biryani"]          # Get
menu["Dosa"] = 120       # Put
"Biryani" in menu        # containsKey
del menu["Naan"]         # Remove
len(menu)                # Size

for key, value in menu.items():   # Iterate
    print(f"{key}: {value}")
```

```java
HashMap<String, Integer> menu = new HashMap<>();
menu.put("Biryani", 250);
menu.get("Biryani");
menu.containsKey("Biryani");
menu.remove("Naan");
menu.size();

for (Map.Entry<String, Integer> entry : menu.entrySet()) {
    System.out.println(entry.getKey() + ": " + entry.getValue());
}
```

### LinkedHashMap — Maintains Insertion Order

```java
LinkedHashMap<String, Integer> orderedMenu = new LinkedHashMap<>();
orderedMenu.put("Starter", 150);
orderedMenu.put("Main Course", 350);
orderedMenu.put("Dessert", 120);

// Always prints in insertion order:
// Starter: 150
// Main Course: 350
// Dessert: 120
```

Use when you need HashMap speed BUT also need to remember the order elements were added.

### TreeMap — Sorted by Keys

```java
TreeMap<String, Integer> sortedMenu = new TreeMap<>();
sortedMenu.put("Dosa", 80);
sortedMenu.put("Biryani", 250);
sortedMenu.put("Aloo Paratha", 60);

// Always sorted alphabetically by key:
// Aloo Paratha: 60
// Biryani: 250
// Dosa: 80

// Bonus methods:
sortedMenu.firstKey();    // "Aloo Paratha"
sortedMenu.lastKey();     // "Dosa"
```

### HashMap vs LinkedHashMap vs TreeMap

| Feature | HashMap | LinkedHashMap | TreeMap |
|---------|---------|--------------|---------|
| Order | No order | Insertion order | Sorted by key |
| put/get time | O(1) | O(1) | O(log n) |
| null keys | 1 null key allowed | 1 null key allowed | No null keys |
| Use when | Speed is priority | Need order + speed | Need sorted keys |

### HashMap vs Hashtable vs ConcurrentHashMap

| Feature | HashMap | Hashtable | ConcurrentHashMap |
|---------|---------|-----------|-------------------|
| Thread-safe? | No | Yes (but slow) | Yes (fast) |
| null keys? | Yes (1) | No | No |
| null values? | Yes | No | No |
| Performance | Fastest (single thread) | Slow (locks entire map) | Fast (locks segments) |
| When to use | Single-threaded code | Never (legacy) | Multi-threaded code |

**Rule:** Never use Hashtable. Use HashMap for single-threaded, ConcurrentHashMap for multi-threaded.

### Common mistakes
1. **Using mutable objects as keys** — if you change a key after putting it in the map, you'll never find it again (hashCode changes)
2. **Not overriding hashCode() for custom key classes** — HashMap won't work correctly
3. **Assuming HashMap is ordered** — it's NOT. Use LinkedHashMap if you need order
4. **NullPointerException with unboxing** — `int x = map.get("missing")` throws NPE because get returns null, and null can't be unboxed to int. Use `getOrDefault()`.

### Practice exercises
1. Build a word frequency counter: read a paragraph and count how many times each word appears using HashMap
2. Use a HashMap to build a phone book: store name-to-number mappings. Allow lookup by name, add new contacts, and update existing ones.
3. Given two arrays, find if they have common elements using a HashSet (O(n) solution)
4. Use a TreeMap to implement a ranking system: student name (key) and score (value), always displayed in alphabetical order
5. Implement an LRU (Least Recently Used) cache using LinkedHashMap (hint: LinkedHashMap has a `removeEldestEntry` method)

---

## 6. Queue Interface

### What is it?
A Queue follows FIFO — First In, First Out. Like a line at a movie theater: the first person in line gets the ticket first.

### PriorityQueue — Not FIFO, But Priority-Based

PriorityQueue is a min-heap by default. The smallest element always comes out first, regardless of insertion order.

```java
import java.util.PriorityQueue;

PriorityQueue<Integer> pq = new PriorityQueue<>();
pq.add(30);
pq.add(10);
pq.add(50);
pq.add(20);

// Elements come out in sorted order (smallest first)
while (!pq.isEmpty()) {
    System.out.println(pq.poll());  // 10, 20, 30, 50
}
```

### Real-life analogy
Think of a hospital emergency room:
- Patients don't get treated in order of arrival
- The most critical (highest priority) patient gets treated first
- That's a PriorityQueue

### Custom priority with Comparator
```java
// Max-heap: largest first (reverse order)
PriorityQueue<Integer> maxPQ = new PriorityQueue<>(Collections.reverseOrder());
maxPQ.add(30);
maxPQ.add(10);
maxPQ.add(50);
System.out.println(maxPQ.poll());  // 50 (largest first)

// Custom objects: prioritize by urgency
PriorityQueue<String> tasks = new PriorityQueue<>((a, b) -> a.length() - b.length());
// Shorter strings (simpler tasks) come out first
```

### Deque (ArrayDeque) — Double-Ended Queue

A Deque (pronounced "deck") allows adding/removing from BOTH ends.

```java
import java.util.ArrayDeque;

ArrayDeque<String> deque = new ArrayDeque<>();

// Use as a Queue (FIFO)
deque.addLast("First");    // or offer()
deque.addLast("Second");
deque.removeFirst();       // "First" — or poll()

// Use as a Stack (LIFO)
deque.addFirst("Bottom");
deque.addFirst("Top");
deque.removeFirst();       // "Top"
```

### Stack class — Legacy, Don't Use!
```java
// OLD way (don't use):
Stack<Integer> stack = new Stack<>();  // Legacy, synchronized, slow

// NEW way (use this):
Deque<Integer> stack = new ArrayDeque<>();  // Modern, fast
stack.push(1);
stack.push(2);
stack.pop();   // 2
```

### Python comparison
```python
# Python Queue
from collections import deque
q = deque()
q.append("first")        # Add to right
q.popleft()              # Remove from left

# Python priority queue
import heapq
heap = []
heapq.heappush(heap, 30)
heapq.heappush(heap, 10)
heapq.heappop(heap)      # 10 (smallest)
```

### Practice exercises
1. Simulate a Zomato order queue: orders come in, are processed FIFO. Print the order being processed.
2. Use PriorityQueue to implement a task scheduler where tasks with shorter duration are executed first
3. Use ArrayDeque as a stack to check if parentheses are balanced in an expression: `{[()]}` is valid, `{[(])}` is invalid

---

## 7. Iterators

### What is it?
An Iterator is a way to traverse (go through) a collection one element at a time. Think of it as a cursor that moves forward through the elements.

### Different ways to iterate

```java
List<String> cities = new ArrayList<>(Arrays.asList("Mumbai", "Delhi", "Bangalore"));

// Method 1: Enhanced for-each (most common)
for (String city : cities) {
    System.out.println(city);
}

// Method 2: Iterator (when you need to remove elements while iterating)
Iterator<String> it = cities.iterator();
while (it.hasNext()) {
    String city = it.next();
    if (city.equals("Delhi")) {
        it.remove();  // Safe removal during iteration!
    }
}

// Method 3: forEach with lambda (Java 8+)
cities.forEach(city -> System.out.println(city));

// Method 4: forEach with method reference (Java 8+)
cities.forEach(System.out::println);

// Method 5: Traditional for loop with index (ArrayList only)
for (int i = 0; i < cities.size(); i++) {
    System.out.println(cities.get(i));
}
```

### Why use Iterator instead of for-each?
```java
// THIS WILL CRASH — ConcurrentModificationException!
for (String city : cities) {
    if (city.equals("Delhi")) {
        cities.remove(city);  // Modifying collection while iterating = CRASH
    }
}

// THIS IS SAFE — use Iterator's remove()
Iterator<String> it = cities.iterator();
while (it.hasNext()) {
    if (it.next().equals("Delhi")) {
        it.remove();  // Safe! Iterator knows about this removal
    }
}
```

### Common mistakes
1. **ConcurrentModificationException** — modifying a collection while iterating with for-each. Use Iterator's remove() instead.
2. **Calling next() twice** — each call to `next()` advances the cursor. Calling it twice reads TWO elements, not one.

---

## 8. Comparable vs Comparator — Custom Sorting

### What is it?
When you sort a collection, Java needs to know HOW to compare two elements. For numbers and strings, Java already knows. But for YOUR custom objects (Student, Product, Order), YOU must tell Java how to compare them.

### Comparable — "I can compare myself to another of my kind"

```java
public class Student implements Comparable<Student> {
    String name;
    int marks;

    public Student(String name, int marks) {
        this.name = name;
        this.marks = marks;
    }

    // Natural ordering: by marks (ascending)
    @Override
    public int compareTo(Student other) {
        return this.marks - other.marks;
        // Negative = this comes first
        // Zero = equal
        // Positive = other comes first
    }

    @Override
    public String toString() {
        return name + "(" + marks + ")";
    }
}

// Usage:
List<Student> students = new ArrayList<>();
students.add(new Student("Sheetal", 85));
students.add(new Student("Priya", 92));
students.add(new Student("Rahul", 78));

Collections.sort(students);
System.out.println(students);  // [Rahul(78), Sheetal(85), Priya(92)] — sorted by marks
```

### Comparator — "I define a custom comparison rule"

Comparator is used when:
1. You want to sort by different criteria at different times
2. You can't modify the class (it's from a library)
3. You want multiple sorting options

```java
// Sort by name (alphabetical)
Comparator<Student> byName = (s1, s2) -> s1.name.compareTo(s2.name);
students.sort(byName);

// Sort by marks descending
Comparator<Student> byMarksDesc = (s1, s2) -> s2.marks - s1.marks;
students.sort(byMarksDesc);

// Using Comparator.comparing (cleaner Java 8+ way)
students.sort(Comparator.comparing(s -> s.name));                     // By name
students.sort(Comparator.comparingInt(s -> s.marks));                 // By marks
students.sort(Comparator.comparingInt((Student s) -> s.marks).reversed()); // By marks descending

// Chain comparisons: first by marks, then by name if marks are equal
students.sort(Comparator.comparingInt((Student s) -> s.marks)
                        .thenComparing(s -> s.name));
```

### Comparable vs Comparator (Interview Question)

| Feature | Comparable | Comparator |
|---------|-----------|------------|
| Package | java.lang | java.util |
| Method | `compareTo(T other)` | `compare(T o1, T o2)` |
| Modifies class? | Yes (implements in the class) | No (external) |
| How many sortings? | ONE natural ordering | MANY different orderings |
| Use when | There's ONE obvious way to sort | You need flexible/multiple sorting |

### Python comparison
```python
# Python — sort with key function (like Comparator)
students = [("Sheetal", 85), ("Priya", 92), ("Rahul", 78)]
students.sort(key=lambda s: s[1])  # Sort by marks

# Python — __lt__ for natural ordering (like Comparable)
class Student:
    def __lt__(self, other):
        return self.marks < other.marks
```

### Practice exercises
1. Create a `FlipkartProduct` class with name, price, and rating. Implement Comparable to sort by price. Then use Comparator to sort by rating, and by name.
2. Sort a list of strings by their length (shortest first) using a Comparator
3. Sort a list of employees first by department (alphabetical), then by salary (highest first) within each department

---

## 9. Collections Utility Class

### What is it?
`Collections` (with an 's') is a utility class with static methods for working with collections. Like a Swiss Army knife.

```java
List<Integer> numbers = new ArrayList<>(Arrays.asList(5, 2, 8, 1, 9, 3));

// Sort
Collections.sort(numbers);            // [1, 2, 3, 5, 8, 9]

// Reverse
Collections.reverse(numbers);         // [9, 8, 5, 3, 2, 1]

// Shuffle (random order)
Collections.shuffle(numbers);         // Random order each time

// Min and Max
int min = Collections.min(numbers);   // 1
int max = Collections.max(numbers);   // 9

// Frequency (count occurrences)
List<String> words = Arrays.asList("apple", "banana", "apple", "cherry", "apple");
int appleCount = Collections.frequency(words, "apple");  // 3

// Unmodifiable (read-only) list
List<String> readOnly = Collections.unmodifiableList(words);
// readOnly.add("grape");  ← UnsupportedOperationException!

// Fill
Collections.fill(numbers, 0);  // [0, 0, 0, 0, 0, 0]

// Swap
Collections.swap(numbers, 0, 3);  // Swaps elements at index 0 and 3

// Binary search (list must be sorted first!)
Collections.sort(numbers);
int index = Collections.binarySearch(numbers, 5);  // Index of 5
```

---

## 10. Generics Basics

### What is it?
Generics let you write code that works with any type, while still being type-safe. The `<String>` in `ArrayList<String>` is a generic — it says "this list holds Strings."

### Why not just use raw types?
```java
// Without generics (raw type) — DANGEROUS
ArrayList list = new ArrayList();
list.add("Hello");
list.add(42);        // No error at compile time!
String s = (String) list.get(1);  // CRASH at runtime! 42 is not a String

// With generics — SAFE
ArrayList<String> list = new ArrayList<>();
list.add("Hello");
// list.add(42);     // COMPILE ERROR! Can't add int to String list
String s = list.get(0);  // No cast needed, type is guaranteed
```

### Python comparison
```python
# Python lists have no type enforcement
my_list = ["Hello", 42, True, [1,2,3]]  # Mix anything — no error

# Python type hints (optional, not enforced)
my_list: list[str] = ["Hello", "World"]
my_list.append(42)  # Type checker warns, but Python still allows it
```

Java generics are ENFORCED by the compiler. Python type hints are SUGGESTIONS. This is a fundamental philosophical difference.

### Writing your own generic class
```java
// A generic Box that can hold any type
public class Box<T> {
    private T item;

    public void put(T item) {
        this.item = item;
    }

    public T get() {
        return item;
    }
}

// Usage:
Box<String> stringBox = new Box<>();
stringBox.put("Biryani");
String food = stringBox.get();  // No cast needed!

Box<Integer> intBox = new Box<>();
intBox.put(42);
int number = intBox.get();
```

---

## 11. Big-O for Every Collection Operation

This table is your quick reference for interviews:

### List Operations
| Operation | ArrayList | LinkedList |
|-----------|-----------|------------|
| get(index) | O(1) | O(n) |
| add(element) at end | O(1)* | O(1) |
| add(0, element) at start | O(n) | O(1) |
| remove(index) | O(n) | O(n)** |
| contains(element) | O(n) | O(n) |
| size() | O(1) | O(1) |

*amortized (occasional resizing is O(n))
**O(1) if you already have the node reference

### Set Operations
| Operation | HashSet | LinkedHashSet | TreeSet |
|-----------|---------|--------------|---------|
| add(element) | O(1) | O(1) | O(log n) |
| remove(element) | O(1) | O(1) | O(log n) |
| contains(element) | O(1) | O(1) | O(log n) |

### Map Operations
| Operation | HashMap | LinkedHashMap | TreeMap |
|-----------|---------|--------------|---------|
| put(key, value) | O(1) | O(1) | O(log n) |
| get(key) | O(1) | O(1) | O(log n) |
| remove(key) | O(1) | O(1) | O(log n) |
| containsKey(key) | O(1) | O(1) | O(log n) |

### Queue Operations
| Operation | PriorityQueue | ArrayDeque |
|-----------|--------------|------------|
| add/offer | O(log n) | O(1) |
| poll/remove | O(log n) | O(1) |
| peek | O(1) | O(1) |

---

## 12. Choosing the Right Collection (Decision Guide)

```
Do you need key-value pairs?
├── YES → Use a Map
│   ├── Need sorted keys? → TreeMap
│   ├── Need insertion order? → LinkedHashMap
│   ├── Multi-threaded? → ConcurrentHashMap
│   └── Otherwise → HashMap (default choice)
│
├── Do you need unique elements only?
│   ├── YES → Use a Set
│   │   ├── Need sorted? → TreeSet
│   │   ├── Need insertion order? → LinkedHashSet
│   │   └── Otherwise → HashSet (default choice)
│   │
│   └── Do you need ordered elements with duplicates?
│       ├── Need priority ordering? → PriorityQueue
│       ├── Need FIFO queue? → ArrayDeque
│       ├── Need stack (LIFO)? → ArrayDeque
│       ├── Frequent add/remove at start? → LinkedList
│       └── Otherwise → ArrayList (default choice)
```

---

## Week 3 Practice Project

**Build a "Flipkart Product Catalog System"**

Requirements:
1. `Product` class with: id, name, category, price, rating. Implements Comparable (by price) and overrides equals/hashCode (by id).
2. Use HashMap to store products by id for O(1) lookup
3. Use TreeMap to maintain products sorted by name
4. Use HashSet to track unique categories
5. Use PriorityQueue to find top 5 cheapest products
6. Use ArrayList to store products and sort them by: price (ascending), rating (descending), name (alphabetical) using Comparators
7. Implement search: by name (contains), by category, by price range
8. Implement a "recently viewed" feature using LinkedHashMap (max 10 items)
9. Use Collections utility methods: find min/max price, count products per category
10. Handle edge cases: duplicate products, empty searches, null values

This project uses EVERY collection type from this week.
