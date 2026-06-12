# LLD Week 4: Interview Problems — Full Solutions — Interview Answers & Worked LLD Designs

> 🎯 **How to use this file:** This is your *spoken script* for a Microsoft LLD round. For every problem you'll see the exact words to say, the classes to draw, the Java skeletons to write on the whiteboard, and the patterns to name out loud. Treat the framework below as a checklist you run **every single time** — the interviewer scores your *process* as much as your code.

---

## 🧭 The Universal LLD Interview Framework (apply this to EVERY problem)

Think of an LLD interview like cooking a dish you've made a hundred times. You don't panic — you follow the same recipe, narrating each step so the chef (interviewer) sees you know what you're doing. Here is the 7-step recipe:

| Step | What you do | What you SAY out loud (the magic words) |
|------|-------------|------------------------------------------|
| **1. Clarify requirements & scope** | Ask 3-4 sharp questions. Pin down functional + non-functional needs. Don't code yet. | *"Before I design, let me clarify — should this support X? What scale are we at? Is concurrency in scope?"* |
| **2. Identify core objects/entities (the nouns)** | Underline the nouns in the problem. Nouns → classes. | *"The nouns I see are User, Car, Spot, Ticket — those become my classes."* |
| **3. Define attributes & actions (the verbs)** | For each class, what does it *hold* (fields) and *do* (methods)? Verbs → methods. | *"A Spot holds its type and occupancy; it can `park()` and `release()`."* |
| **4. Establish relationships** | HAS-A (composition/aggregation) vs IS-A (inheritance) vs USES-A (association). | *"A ParkingLot HAS-A list of Floors — composition. A Car IS-A Vehicle — inheritance."* |
| **5. Apply SOLID + the right design patterns** | Name the pattern AND justify it. Never name a pattern without a reason. | *"Fees vary, so I'll use **Strategy** — that respects the **Open/Closed Principle**: open to new pricing, closed for modification."* |
| **6. Write clean class skeletons in Java** | Enums first, then data classes, then the orchestrator. Implement the *important* methods. | *(start writing — enums, then leaf classes, then the 'god' class)* |
| **7. Walk through a usage flow** | Trace one real scenario end-to-end. Prove the wiring works. | *"Let me trace it: a car arrives → `parkVehicle()` finds a spot → issues a ticket → on exit, Strategy computes the fee."* |

### 🔑 The 5 SOLID principles — your vocabulary

| Letter | Principle | One-liner you can say |
|--------|-----------|------------------------|
| **S** | Single Responsibility | *"Each class has one reason to change. A Ticket tracks time; it does NOT calculate fees."* |
| **O** | Open/Closed | *"Open for extension, closed for modification — I add a new Strategy subclass instead of editing an `if/else`."* |
| **L** | Liskov Substitution | *"Any FeeStrategy subclass can replace the base without breaking the ParkingLot."* |
| **I** | Interface Segregation | *"Small focused interfaces — `Observer` only has `update()`, not 10 unrelated methods."* |
| **D** | Dependency Inversion | *"ParkingLot depends on the FeeStrategy *abstraction*, not a concrete HourlyFee class."* |

### 🎨 The 6 patterns you'll reuse across these problems

- **Strategy** 🎯 — swappable algorithms (pricing, splitting, scheduling). *Replaces if/else chains.*
- **Observer** 📣 — "tell everyone who cares when X happens" (waitlists, order status, floor displays).
- **State** 🚦 — an object behaves differently per state (elevator IDLE/MOVING, seat AVAILABLE/HELD/BOOKED).
- **Factory** 🏭 — centralize object creation (which Strategy? which Notification type?).
- **Singleton** 1️⃣ — exactly one instance (the ParkingLot, the NotificationManager).
- **Facade** 🎭 — one simple front door hiding a messy backend (Library, OrderService).

> 💡 **Golden rule:** *Think out loud. Name the pattern. Justify with SOLID.* A silent genius scores lower than a chatty good engineer in these rounds.

---

# PART A — Worked LLD Designs

---

## 🅿️ Problem 1: Parking Lot System

### Step 1 — Clarify (say this)
*"Multiple floors, multiple spot sizes? Different vehicle types mapped to spot types? Do we need different pricing models and runtime switching? In-memory or DB-backed? I'll assume multi-floor, 3 vehicle/spot types, pluggable pricing, in-memory."*

**Functional:** find spot → issue ticket on entry → compute fee on exit → track occupancy → swappable pricing.
**Non-functional:** O(1)-ish spot lookup, thread-safety later, single physical lot (Singleton candidate).

### Step 2-3 — Entities (nouns) & their jobs (verbs)

| Class / Enum | Holds | Does |
|--------------|-------|------|
| `VehicleType`, `SpotType` (enums) | named constants | prevent typos |
| `Vehicle` | plate, type | (data only) |
| `ParkingSpot` | id, type, occupancy, vehicle | `park()`, `release()` |
| `ParkingFloor` | floor#, spots | `findAvailableSpot()`, `getAvailableCount()` |
| `ParkingTicket` | id, vehicle, spot, times | `getDurationHours()`, `close()` |
| `FeeStrategy` (abstract) | rates | `calculateFee()` |
| `ParkingLot` (orchestrator) | floors, active tickets, strategy | `parkVehicle()`, `exitVehicle()` |

### Step 4 — Relationship overview

```
ParkingLot ──has many──▶ ParkingFloor ──has many──▶ ParkingSpot ──refers to──▶ Vehicle (or null)
     │                                                                              ▲
     ├──has many──▶ ParkingTicket ──────────────────refers to──────────────────────┘
     └──USES-A────▶ FeeStrategy  (Strategy: Hourly / FlatRate / WeekendSurge)
```
Composition: floors/spots can't exist without the lot. Association: a ticket *refers* to a vehicle + spot.

### Step 6 — Key Java skeletons

```java
import java.time.*;
import java.util.*;

enum VehicleType { BIKE, CAR, BUS }
enum SpotType   { COMPACT, REGULAR, LARGE }

class Vehicle {
    private final String plate;
    private final VehicleType type;
    Vehicle(String plate, VehicleType type) { this.plate = plate; this.type = type; }
    String getPlate() { return plate; }
    VehicleType getType() { return type; }
}

class ParkingSpot {
    private final String id;
    private final SpotType type;
    private final int floor;
    private boolean occupied;
    private Vehicle vehicle;
    ParkingSpot(String id, SpotType type, int floor) { this.id = id; this.type = type; this.floor = floor; }

    boolean park(Vehicle v) {            // verb
        if (occupied) return false;
        this.vehicle = v; this.occupied = true; return true;
    }
    Vehicle release() {                  // verb
        Vehicle v = vehicle; vehicle = null; occupied = false; return v;
    }
    String getId() { return id; }
    SpotType getType() { return type; }
    int getFloor() { return floor; }
    boolean isOccupied() { return occupied; }
}

class ParkingFloor {
    private final int floor;
    private final List<ParkingSpot> spots = new ArrayList<>();
    ParkingFloor(int floor) { this.floor = floor; }
    void addSpot(ParkingSpot s) { spots.add(s); }

    ParkingSpot findAvailableSpot(SpotType type) {
        for (ParkingSpot s : spots)
            if (s.getType() == type && !s.isOccupied()) return s;
        return null;
    }
    int getFloor() { return floor; }
}

class ParkingTicket {
    private static int counter = 0;
    private final String id;
    private final Vehicle vehicle;
    private final ParkingSpot spot;
    private final LocalDateTime entryTime;
    private LocalDateTime exitTime;

    ParkingTicket(Vehicle v, ParkingSpot s) {
        this.id = String.format("TKT-%04d", ++counter);
        this.vehicle = v; this.spot = s; this.entryTime = LocalDateTime.now();
    }
    void close(LocalDateTime exit) { this.exitTime = (exit != null) ? exit : LocalDateTime.now(); }
    double getDurationHours() {
        LocalDateTime exit = (exitTime != null) ? exitTime : LocalDateTime.now();
        double h = Duration.between(entryTime, exit).getSeconds() / 3600.0;
        return Math.max(h, 0.5); // min 30 min — WHY: avoid free parking for drive-throughs
    }
    ParkingSpot getSpot() { return spot; }
    String getId() { return id; }
}

// ----- Strategy pattern: pluggable pricing -----
interface FeeStrategy { double calculateFee(ParkingTicket t); String name(); }

class HourlyFeeStrategy implements FeeStrategy {
    private final Map<SpotType,Integer> rate = Map.of(
        SpotType.COMPACT, 20, SpotType.REGULAR, 40, SpotType.LARGE, 80); // Rs/hr
    public double calculateFee(ParkingTicket t) {
        return Math.ceil(t.getDurationHours()) * rate.get(t.getSpot().getType());
    }
    public String name() { return "Hourly"; }
}

class WeekendSurgeFeeStrategy implements FeeStrategy {
    private final FeeStrategy base; private final double mult;
    WeekendSurgeFeeStrategy(FeeStrategy base, double mult) { this.base = base; this.mult = mult; }
    public double calculateFee(ParkingTicket t) { return base.calculateFee(t) * mult; }
    public String name() { return "Weekend x" + mult; }
}

// ----- Orchestrator (Singleton in production) -----
class ParkingLot {
    private static final Map<VehicleType,SpotType> MAP = Map.of(
        VehicleType.BIKE, SpotType.COMPACT,
        VehicleType.CAR,  SpotType.REGULAR,
        VehicleType.BUS,  SpotType.LARGE);

    private final List<ParkingFloor> floors = new ArrayList<>();
    private final Map<String, ParkingTicket> active = new HashMap<>();
    private FeeStrategy feeStrategy;
    ParkingLot(FeeStrategy s) { this.feeStrategy = s; }

    void addFloor(ParkingFloor f) { floors.add(f); }
    void setFeeStrategy(FeeStrategy s) { this.feeStrategy = s; }   // runtime swap — Strategy power

    ParkingSpot findSpot(VehicleType type) {
        SpotType need = MAP.get(type);
        for (ParkingFloor f : floors) {
            ParkingSpot s = f.findAvailableSpot(need);
            if (s != null) return s;
        }
        return null;
    }
    ParkingTicket parkVehicle(Vehicle v) {
        if (active.containsKey(v.getPlate())) return null;       // already parked
        ParkingSpot spot = findSpot(v.getType());
        if (spot == null) return null;                            // lot full
        spot.park(v);
        ParkingTicket t = new ParkingTicket(v, spot);
        active.put(v.getPlate(), t);
        return t;
    }
    double exitVehicle(String plate, LocalDateTime exit) {
        ParkingTicket t = active.remove(plate);
        if (t == null) return 0;
        t.close(exit);
        double fee = feeStrategy.calculateFee(t);                 // Strategy decides the price
        t.getSpot().release();
        return fee;
    }
}
```

### Step 5 — Patterns & SOLID (say which + WHY)

| Pattern / Principle | Where | Why |
|---------------------|-------|-----|
| **Strategy** 🎯 | `FeeStrategy` | Pricing varies (hourly/flat/surge) and we swap it at runtime. ParkingLot never changes when a new price model arrives. |
| **Factory** 🏭 (implicit) | `MAP` vehicle→spot | Centralizes the "which spot does this vehicle need" rule. |
| **Singleton** 1️⃣ | `ParkingLot` | One physical lot = one instance. |
| **Open/Closed** | adding `EVChargingFeeStrategy` | New pricing = new class, zero edits to existing code. |
| **Single Responsibility** | `Ticket` vs `FeeStrategy` | Ticket tracks *time*; Strategy computes *money*. Separate reasons to change. |

### Step 7 — Usage walkthrough
*"A car `MH-02-1234` drives in → `parkVehicle()` → `findSpot(CAR)` scans floors, returns first free REGULAR spot → spot.park() flips occupancy → a `TKT-0001` ticket is born. Three hours later the car exits → `exitVehicle()` closes the ticket, the **Hourly Strategy** computes `ceil(3)*40 = Rs.120`, the spot releases. If I'd swapped to **WeekendSurge x1.5** first, the same drive would cost Rs.180 — and I changed nothing inside ParkingLot. That's the Strategy + Open/Closed payoff."*

---

## 🗄️ Problem 2: LRU Cache

> The Microsoft favorite. It's less "patterns" and more "do you know HashMap + Doubly Linked List, and can you keep both `get` and `put` at **O(1)**?"

### Step 1 — Clarify
*"Fixed capacity? `get` returns -1 on miss? Both ops must be O(1)? Accessing a key counts as 'used'? Yes to all — classic LeetCode 146."*

### The core insight (say this — it's the whole interview)
*"I need two things at once: **O(1) lookup by key** and **O(1) ordering by recency**. A HashMap gives lookup but no order. A linked list gives order but O(n) lookup. So I **combine them**: HashMap maps `key → Node`, and a **doubly** linked list orders nodes by recency. Doubly — not singly — because I must unlink a node from the middle in O(1), which needs its `prev` pointer."*

```
HashMap: { 1→N1, 3→N3, 2→N2 }
List:   [HEAD] ⇄ N3 ⇄ N1 ⇄ N2 ⇄ [TAIL]
                MRU            LRU
```
Dummy `HEAD`/`TAIL` sentinels kill all the "is this the first/last node?" edge cases.

### Key Java skeleton

```java
import java.util.*;

class LRUCache {
    private static class Node {
        int key, value;
        Node prev, next;
        Node(int k, int v) { key = k; value = v; }
    }
    private final int capacity;
    private final Map<Integer, Node> map = new HashMap<>();
    private final Node head = new Node(0, 0);  // dummy MRU side
    private final Node tail = new Node(0, 0);  // dummy LRU side

    LRUCache(int capacity) {
        this.capacity = capacity;
        head.next = tail; tail.prev = head;     // empty list
    }

    private void remove(Node n) {               // O(1) unlink
        n.prev.next = n.next;
        n.next.prev = n.prev;
    }
    private void addFront(Node n) {              // O(1) insert after head
        n.prev = head; n.next = head.next;
        head.next.prev = n; head.next = n;
    }

    public int get(int key) {
        if (!map.containsKey(key)) return -1;
        Node n = map.get(key);
        remove(n); addFront(n);                  // touch → becomes MRU
        return n.value;
    }
    public void put(int key, int value) {
        if (map.containsKey(key)) {
            Node n = map.get(key);
            n.value = value;
            remove(n); addFront(n);
            return;
        }
        if (map.size() >= capacity) {            // evict LRU (node before tail)
            Node lru = tail.prev;
            remove(lru);
            map.remove(lru.key);                 // also drop from map!
        }
        Node n = new Node(key, value);
        map.put(key, n);
        addFront(n);
    }
}
```

### Complexity & patterns

| Op | Time | Why |
|----|------|-----|
| `get` | **O(1)** | map lookup + 2 pointer ops |
| `put` | **O(1)** | map lookup + eviction + 2 pointer ops |
| space | O(capacity) | map + list hold ≤ capacity nodes |

**Patterns:** mostly a data-structure problem. *Mention for bonus:* **Strategy** to swap eviction policy (LRU↔LFU↔FIFO), **Observer** to write-back to DB on eviction, **Singleton** if app-wide.

### Walkthrough
*"`put(1),put(2),put(3)` fills the cache `[3,2,1]`. `get(1)` moves 1 to front → `[1,3,2]`, so 2 is now LRU. `put(4)` is over capacity → evict `tail.prev`=2, remove from both list and map → `[4,1,3]`. `get(2)` now returns -1. Every step touched only a constant number of pointers — O(1) guaranteed."*

> ⚠️ **Classic bug to avoid (say you know it):** *"The #1 mistake is forgetting `map.remove(lru.key)` on eviction — the node leaves the list but lingers in the map, leaking memory and corrupting `containsKey`."*

---

## 📚 Problem 3: Library Management System

### Step 1 — Clarify
*"Multiple physical copies per title? Borrow limit and due dates? Late fines? And — key feature — do we notify a waitlist when a popular book returns? Yes → that's an **Observer**."*

### Entities & relationships

```
Library (Facade) ──has──▶ Book (each copy, status, waitlist)
       │                     ▲ Observer: notifies waiting Members
       ├──has──▶ Member (implements BookObserver) ──has──▶ BorrowRecord (book, due date, fine)
```

### Key Java skeletons

```java
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

enum BookStatus { AVAILABLE, BORROWED, RESERVED, LOST }
enum BookCategory { FICTION, NON_FICTION, SCIENCE, TECHNOLOGY, HISTORY }

// ----- Observer pattern: waitlist notifications -----
interface BookObserver { void onBookAvailable(Book book); String getName(); }

class Book {
    private final String isbn, title, author;
    private final BookCategory category;
    private BookStatus status = BookStatus.AVAILABLE;
    private final List<BookObserver> waitlist = new ArrayList<>();   // subscribers

    Book(String isbn, String title, String author, BookCategory c) {
        this.isbn = isbn; this.title = title; this.author = author; this.category = c;
    }
    void addToWaitlist(BookObserver o) { if (!waitlist.contains(o)) waitlist.add(o); }
    void notifyWaitlist() {                                          // fire event
        for (BookObserver o : waitlist) o.onBookAvailable(this);
        waitlist.clear();
    }
    String getTitle() { return title; }
    BookStatus getStatus() { return status; }
    void setStatus(BookStatus s) { status = s; }
}

class BorrowRecord {
    private final Book book; private final Member member;
    private final LocalDateTime borrowDate, dueDate;
    private LocalDateTime returnDate;
    private double fine;
    private static final int LOAN_DAYS = 14;   // 2-week loan — library standard
    private static final int FINE_PER_DAY = 5; // Rs.5/day late fee

    BorrowRecord(Book b, Member m, LocalDateTime when) {
        this.book = b; this.member = m;
        this.borrowDate = (when != null) ? when : LocalDateTime.now();
        this.dueDate = borrowDate.plusDays(LOAN_DAYS);
    }
    double calculateFine(LocalDateTime ret) {
        LocalDateTime r = (ret != null) ? ret : LocalDateTime.now();
        if (r.isAfter(dueDate)) fine = ChronoUnit.DAYS.between(dueDate, r) * FINE_PER_DAY;
        return fine;
    }
    Book getBook() { return book; }
    LocalDateTime getDueDate() { return dueDate; }
    LocalDateTime getReturnDate() { return returnDate; }
    void setReturnDate(LocalDateTime r) { returnDate = r; }
}

class Member implements BookObserver {            // a Member IS an Observer
    static final int MAX_BOOKS = 5;
    private final String memberId, name;
    private final List<BorrowRecord> borrowed = new ArrayList<>();
    private double totalFines;

    Member(String id, String name) { this.memberId = id; this.name = name; }
    boolean canBorrow() {
        long active = borrowed.stream().filter(r -> r.getReturnDate() == null).count();
        return active < MAX_BOOKS;
    }
    public void onBookAvailable(Book b) {          // Observer callback
        System.out.println("[NOTIFY] " + name + ": '" + b.getTitle() + "' is available!");
    }
    public String getName() { return name; }
    String getId() { return memberId; }
    List<BorrowRecord> getBorrowed() { return borrowed; }
    void addFine(double f) { totalFines += f; }
}

class Library {                                    // Facade
    private final List<Book> books = new ArrayList<>();
    private final Map<String, Member> members = new HashMap<>();

    void addBook(Book b) { books.add(b); }
    void registerMember(Member m) { members.put(m.getId(), m); }

    private Book findAvailable(String title) {
        for (Book b : books)
            if (b.getTitle().equalsIgnoreCase(title) && b.getStatus() == BookStatus.AVAILABLE) return b;
        return null;
    }
    BorrowRecord borrowBook(String memberId, String title, LocalDateTime when) {
        Member m = members.get(memberId);
        if (m == null || !m.canBorrow()) return null;
        Book b = findAvailable(title);
        if (b == null) {                            // all copies out → waitlist
            books.stream().filter(x -> x.getTitle().equalsIgnoreCase(title))
                 .findFirst().ifPresent(x -> x.addToWaitlist(m));
            return null;
        }
        BorrowRecord r = new BorrowRecord(b, m, when);
        b.setStatus(BookStatus.BORROWED);
        m.getBorrowed().add(r);
        return r;
    }
    double returnBook(String memberId, String title, LocalDateTime when) {
        Member m = members.get(memberId);
        if (m == null) return 0;
        BorrowRecord r = m.getBorrowed().stream()
            .filter(x -> x.getBook().getTitle().equalsIgnoreCase(title) && x.getReturnDate() == null)
            .findFirst().orElse(null);
        if (r == null) return 0;
        r.setReturnDate((when != null) ? when : LocalDateTime.now());
        double fine = r.calculateFine(r.getReturnDate());
        m.addFine(fine);
        r.getBook().setStatus(BookStatus.AVAILABLE);
        r.getBook().notifyWaitlist();               // Observer fires here!
        return fine;
    }
}
```

### Patterns & SOLID

| Pattern | Where | Why |
|---------|-------|-----|
| **Observer** 📣 | `Book.waitlist` + `Member.onBookAvailable()` | Returned book auto-notifies everyone waiting — no manual polling. |
| **Facade** 🎭 | `Library` | One door (`borrowBook`/`returnBook`) hides books, members, records, notifications. |
| **Strategy** 🎯 (extension) | fine calculation | Flat vs tiered vs %-of-value fines, swappable. |
| **Interface Segregation** | `BookObserver` has only `onBookAvailable` + `getName` | Tiny, focused contract. |

### Walkthrough
*"Two copies of *Clean Code*. Sheetal and Rahul each grab one → both BORROWED. Priya asks → no copy free → she's added to the **waitlist**. Sheetal returns on time (no fine) → `notifyWaitlist()` fires → Priya gets `[NOTIFY] 'Clean Code' is available!` automatically. Rahul returns 5 days late → `calculateFine` = 5×5 = Rs.25. The Observer meant nobody had to manually track Priya's request."*

---

## 💸 Problem 4: Splitwise (Expense Sharing)

### Step 1 — Clarify
*"Groups of users? Three split types — equal, exact, percentage? And do we **minimize the number of settlement transactions**? That debt-simplification is the meaty algorithmic part."*

### The two interesting bits
1. **Strategy** for *how* an expense splits (equal / exact / percentage).
2. A **greedy debt-simplification** algorithm so 4 friends settle in 2 transfers, not 6.

### Key Java skeletons

```java
import java.util.*;

enum SplitType { EQUAL, EXACT, PERCENTAGE }

// ----- Strategy: how to split an expense -----
interface SplitStrategy {
    // returns {userId -> amountOwed}; must sum to total
    Map<String, Double> calculateSplits(double total, List<String> participants,
                                        Map<String, Double> details);
}

class EqualSplit implements SplitStrategy {
    public Map<String, Double> calculateSplits(double total, List<String> p, Map<String, Double> d) {
        double per = Math.round(total / p.size() * 100.0) / 100.0;
        Map<String, Double> out = new LinkedHashMap<>();
        for (String u : p) out.put(u, per);
        double rem = Math.round((total - per * p.size()) * 100.0) / 100.0; // give rounding remainder to first
        if (rem != 0) out.put(p.get(0), out.get(p.get(0)) + rem);
        return out;
    }
}
class ExactSplit implements SplitStrategy {
    public Map<String, Double> calculateSplits(double total, List<String> p, Map<String, Double> d) {
        double sum = d.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(sum - total) > 0.01) throw new IllegalArgumentException("exact amounts must sum to total");
        return d;
    }
}
class PercentageSplit implements SplitStrategy {
    public Map<String, Double> calculateSplits(double total, List<String> p, Map<String, Double> d) {
        double pct = d.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(pct - 100) > 0.01) throw new IllegalArgumentException("percentages must sum to 100");
        Map<String, Double> out = new LinkedHashMap<>();
        for (var e : d.entrySet()) out.put(e.getKey(), Math.round(total * e.getValue() / 100.0 * 100.0) / 100.0);
        return out;
    }
}

class User { final String id, name; User(String id, String name){this.id=id;this.name=name;} }

class Expense {
    private static final Map<SplitType, SplitStrategy> STRAT = Map.of(   // Factory map
        SplitType.EQUAL, new EqualSplit(),
        SplitType.EXACT, new ExactSplit(),
        SplitType.PERCENTAGE, new PercentageSplit());
    final double total; final User paidBy; final Map<String, Double> splits;

    Expense(double total, User paidBy, SplitType type, List<User> parts, Map<String, Double> details) {
        this.total = total; this.paidBy = paidBy;
        List<String> ids = new ArrayList<>(); for (User u : parts) ids.add(u.id);
        this.splits = STRAT.get(type).calculateSplits(total, ids, details);  // Strategy picks algorithm
    }
}

class Group {
    private final Map<String, User> members = new LinkedHashMap<>();
    private final List<Expense> expenses = new ArrayList<>();

    void addMember(User u) { members.put(u.id, u); }
    void addExpense(Expense e) { expenses.add(e); }

    // net balance: + means others owe you, - means you owe
    Map<String, Double> getBalances() {
        Map<String, Double> bal = new LinkedHashMap<>();
        for (String id : members.keySet()) bal.put(id, 0.0);
        for (Expense e : expenses) {
            bal.merge(e.paidBy.id, e.total, Double::sum);                 // payer credited full amount
            for (var s : e.splits.entrySet()) bal.merge(s.getKey(), -s.getValue(), Double::sum); // each owes share
        }
        bal.replaceAll((k, v) -> Math.round(v * 100.0) / 100.0);
        return bal;
    }

    // greedy min-transactions: biggest debtor pays biggest creditor
    List<String[]> simplifyDebts() {
        Map<String, Double> bal = getBalances();
        List<Object[]> credit = new ArrayList<>(), debit = new ArrayList<>();
        for (var e : bal.entrySet()) {
            if (e.getValue() > 0.01) credit.add(new Object[]{e.getKey(), e.getValue()});
            else if (e.getValue() < -0.01) debit.add(new Object[]{e.getKey(), -e.getValue()});
        }
        credit.sort((a, b) -> Double.compare((double) b[1], (double) a[1]));
        debit.sort((a, b) -> Double.compare((double) b[1], (double) a[1]));
        List<String[]> tx = new ArrayList<>();
        int i = 0, j = 0;
        while (i < debit.size() && j < credit.size()) {
            double owe = (double) debit.get(i)[1], get = (double) credit.get(j)[1];
            double pay = Math.min(owe, get);
            tx.add(new String[]{(String) debit.get(i)[0], (String) credit.get(j)[0],
                                String.valueOf(Math.round(pay * 100.0) / 100.0)});
            debit.get(i)[1]  = owe - pay;
            credit.get(j)[1] = get - pay;
            if ((double) debit.get(i)[1] < 0.01) i++;
            if ((double) credit.get(j)[1] < 0.01) j++;
        }
        return tx;
    }
}
```

### Patterns & SOLID

| Pattern | Where | Why |
|---------|-------|-----|
| **Strategy** 🎯 | `SplitStrategy` subclasses | Split logic varies; add "by shares" later without touching `Expense`. |
| **Factory** 🏭 | `STRAT` map | Maps `SplitType` → strategy object. |
| **Facade** 🎭 | `Group` | `addExpense` / `getBalances` hide the balance + simplification math. |
| **Open/Closed** | new split type = new class | No edits to existing strategies. |

### Walkthrough
*"Goa trip: Sheetal pays Rs.12000 hotel (equal, 4 ways → each owes 3000, Sheetal credited +12000). Rahul pays 3200 cab. Dinner splits by **percentage** (Amit ate more → 35%). Water sports only 3 people (equal among them). `getBalances()` nets everyone. Then `simplifyDebts()` sorts debtors/creditors descending and greedily matches biggest-to-biggest — so instead of everyone-pays-everyone, we get the **minimum transfers**. Greedy is near-optimal and runs in O(n log n)."*

> 💡 **Edge case to mention:** floating-point money. *"I round to 2 decimals everywhere and dump the rounding remainder onto the first person — otherwise splits silently fail to sum to the total."*

---

## ❌⭕ Problem 5: Tic Tac Toe

### Step 1 — Clarify
*"3×3 or general N×N? Two players, X and O? Detect win (row/col/both diagonals) and draw? Human-vs-human for now, but I'll keep it extensible to AI."*

### Entities

```
Game (controller, holds State) ──has──▶ Board (N×N grid) ──holds──▶ Symbol[][]
     └──has 2──▶ Player (name, symbol, wins)
```

### Key Java skeletons

```java
enum Symbol { X, O, EMPTY }
enum GameState { IN_PROGRESS, WON, DRAW }   // the State pattern, lightweight

class Player {
    final String name; final Symbol symbol; int wins;
    Player(String name, Symbol symbol) { this.name = name; this.symbol = symbol; }
}

class Board {
    private final int size;
    private final Symbol[][] grid;
    private int moves;
    Board(int size) {
        this.size = size;
        grid = new Symbol[size][size];
        for (Symbol[] row : grid) java.util.Arrays.fill(row, Symbol.EMPTY);
    }
    boolean place(int r, int c, Symbol s) {
        if (r < 0 || r >= size || c < 0 || c >= size) return false; // bounds
        if (grid[r][c] != Symbol.EMPTY) return false;               // occupied
        grid[r][c] = s; moves++; return true;
    }
    boolean isFull() { return moves == size * size; }
    boolean checkWinner(Symbol s) {
        int n = size;
        for (int r = 0; r < n; r++) {            // rows
            boolean all = true;
            for (int c = 0; c < n; c++) if (grid[r][c] != s) { all = false; break; }
            if (all) return true;
        }
        for (int c = 0; c < n; c++) {            // cols
            boolean all = true;
            for (int r = 0; r < n; r++) if (grid[r][c] != s) { all = false; break; }
            if (all) return true;
        }
        boolean diag = true, anti = true;        // both diagonals
        for (int i = 0; i < n; i++) {
            if (grid[i][i] != s) diag = false;
            if (grid[i][n - 1 - i] != s) anti = false;
        }
        return diag || anti;
    }
}

class Game {
    private final Board board;
    private final Player[] players;
    private int turn = 0;
    private GameState state = GameState.IN_PROGRESS;
    private Player winner;

    Game(String p1, String p2, int size) {
        board = new Board(size);
        players = new Player[]{ new Player(p1, Symbol.X), new Player(p2, Symbol.O) };
    }
    boolean makeMove(int r, int c) {
        if (state != GameState.IN_PROGRESS) return false;   // State guards behavior
        Player p = players[turn];
        if (!board.place(r, c, p.symbol)) return false;     // invalid move
        if (board.checkWinner(p.symbol)) { state = GameState.WON; winner = p; p.wins++; return true; }
        if (board.isFull())              { state = GameState.DRAW; return true; }
        turn = 1 - turn;                                    // toggle 0↔1
        return true;
    }
    GameState getState() { return state; }
    Player getWinner() { return winner; }
}
```

### Patterns & SOLID

| Pattern | Where | Why |
|---------|-------|-----|
| **State** 🚦 | `GameState` gates `makeMove` | Behavior differs by state — moves rejected once WON/DRAW. |
| **Single Responsibility** | `Board` (grid logic) vs `Game` (turns/outcome) | Board doesn't know about turns; Game doesn't know grid internals. |
| **Open/Closed** (extension) | add `AIPlayer` strategy | A `MoveStrategy` interface (Human/Minimax) plugs in without changing `Game`. |

### Walkthrough
*"Sheetal is X, Rahul is O. `makeMove(0,0)` X, `(0,1)` O, `(1,1)` X, `(0,2)` O, `(2,2)` X → `checkWinner(X)` sees the main diagonal all-X → state flips to WON, Sheetal's win count bumps. Any further `makeMove` returns false because the **State** guard blocks it. To add an unbeatable AI, I'd introduce a `MoveStrategy` and drop in Minimax — Game wouldn't change."*

---

## 🏨 Problem 6: Hotel Booking System (OYO-style) — *Practice Exercise 1*

### Step 1 — Clarify
*"Room types (Standard/Deluxe/Suite) with per-night prices? Search by city + date range? Bookings + cancellations with **policy-based refunds**? Pricing varies weekday/weekend/seasonal? And notify staff on book/cancel? Yes — so I'll need **two Strategies** (pricing + cancellation) and an **Observer**."*

### Entities & relationships

```
HotelService (Facade)
   └─has many─▶ Hotel (city) ─has many─▶ Room (type, RoomStatus)
                    │
   Booking ──refers──┘ (room, guest, dates, amount, BookingStatus)
   PricingStrategy 🎯 (Weekday / Weekend / Seasonal)
   CancellationPolicy 🎯 (Flexible / Moderate / Strict)
   BookingObserver 📣 (StaffNotifier)
```

### Key Java skeletons

```java
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

enum RoomType { STANDARD, DELUXE, SUITE }
enum RoomStatus { AVAILABLE, BOOKED }
enum BookingStatus { CONFIRMED, CANCELLED }

// ---- Strategy 1: nightly pricing ----
interface PricingStrategy { double priceFor(RoomType type, LocalDate night); }

class StandardPricing implements PricingStrategy {
    private static final Map<RoomType, Double> BASE = Map.of(
        RoomType.STANDARD, 2000.0, RoomType.DELUXE, 3500.0, RoomType.SUITE, 6000.0);
    public double priceFor(RoomType type, LocalDate night) {
        double price = BASE.get(type);
        DayOfWeek d = night.getDayOfWeek();
        if (d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY) price *= 1.25; // weekend +25%
        return price;
    }
}
class SeasonalPricing implements PricingStrategy {           // e.g. New Year surge
    private final PricingStrategy base; private final double surge;
    SeasonalPricing(PricingStrategy base, double surge) { this.base = base; this.surge = surge; }
    public double priceFor(RoomType type, LocalDate night) { return base.priceFor(type, night) * surge; }
}

// ---- Strategy 2: cancellation refund policy ----
interface CancellationPolicy { double refund(double paid, long daysBefore); String name(); }

class FlexiblePolicy implements CancellationPolicy {        // full refund anytime
    public double refund(double paid, long days) { return paid; }
    public String name() { return "Flexible"; }
}
class ModeratePolicy implements CancellationPolicy {        // 100% if >2 days, else 50%
    public double refund(double paid, long days) { return days >= 2 ? paid : paid * 0.5; }
    public String name() { return "Moderate"; }
}
class StrictPolicy implements CancellationPolicy {          // 50% if >7 days, else 0
    public double refund(double paid, long days) { return days >= 7 ? paid * 0.5 : 0; }
    public String name() { return "Strict"; }
}

// ---- Observer: notify staff ----
interface BookingObserver { void onBooking(Booking b); void onCancellation(Booking b); }
class StaffNotifier implements BookingObserver {
    public void onBooking(Booking b)      { System.out.println("[STAFF] New booking " + b.getId()); }
    public void onCancellation(Booking b) { System.out.println("[STAFF] Cancelled " + b.getId()); }
}

class Room {
    final String id; final RoomType type; RoomStatus status = RoomStatus.AVAILABLE;
    Room(String id, RoomType type) { this.id = id; this.type = type; }
}

class Booking {
    private static int counter = 0;
    private final String id;
    final Room room; final String guest;
    final LocalDate checkIn, checkOut;
    final double amount;
    BookingStatus status = BookingStatus.CONFIRMED;
    Booking(Room room, String guest, LocalDate in, LocalDate out, double amount) {
        this.id = String.format("BKG-%04d", ++counter);
        this.room = room; this.guest = guest; this.checkIn = in; this.checkOut = out; this.amount = amount;
    }
    String getId() { return id; }
}

class Hotel {
    final String name, city;
    final List<Room> rooms = new ArrayList<>();
    Hotel(String name, String city) { this.name = name; this.city = city; }
    Room findAvailable(RoomType type) {
        for (Room r : rooms) if (r.type == type && r.status == RoomStatus.AVAILABLE) return r;
        return null;
    }
}

class HotelService {                                        // Facade
    private final List<Hotel> hotels = new ArrayList<>();
    private final Map<String, Booking> bookings = new HashMap<>();
    private PricingStrategy pricing = new StandardPricing();
    private CancellationPolicy policy = new ModeratePolicy();
    private final List<BookingObserver> observers = new ArrayList<>();

    void addHotel(Hotel h) { hotels.add(h); }
    void subscribe(BookingObserver o) { observers.add(o); }
    void setPricing(PricingStrategy p) { this.pricing = p; }
    void setPolicy(CancellationPolicy p) { this.policy = p; }

    Booking book(String city, RoomType type, String guest, LocalDate in, LocalDate out) {
        Hotel hotel = hotels.stream().filter(h -> h.city.equalsIgnoreCase(city)).findFirst().orElse(null);
        if (hotel == null) return null;
        Room room = hotel.findAvailable(type);
        if (room == null) return null;                       // sold out
        double total = 0;
        for (LocalDate d = in; d.isBefore(out); d = d.plusDays(1))
            total += pricing.priceFor(type, d);              // Strategy sums nightly prices
        room.status = RoomStatus.BOOKED;
        Booking b = new Booking(room, guest, in, out, total);
        bookings.put(b.getId(), b);
        observers.forEach(o -> o.onBooking(b));              // Observer fires
        return b;
    }
    double cancel(String bookingId, LocalDate today) {
        Booking b = bookings.get(bookingId);
        if (b == null || b.status == BookingStatus.CANCELLED) return 0;
        long daysBefore = ChronoUnit.DAYS.between(today, b.checkIn);
        double refund = policy.refund(b.amount, daysBefore); // Strategy decides refund
        b.status = BookingStatus.CANCELLED;
        b.room.status = RoomStatus.AVAILABLE;
        observers.forEach(o -> o.onCancellation(b));
        return refund;
    }
}
```

### Patterns & SOLID

| Pattern | Where | Why |
|---------|-------|-----|
| **Strategy** 🎯 ×2 | `PricingStrategy`, `CancellationPolicy` | Two independent varying algorithms — price and refund — each swappable. |
| **Observer** 📣 | `BookingObserver` | Staff (and later: housekeeping, billing) react to book/cancel events. |
| **Facade** 🎭 | `HotelService` | `book` / `cancel` hide search, pricing, inventory, notifications. |
| **Dependency Inversion** | service depends on policy/pricing *interfaces* | Concrete policies injected, not hard-coded. |

### Walkthrough
*"Guest books a Deluxe in Mumbai for Fri–Sun → pricing sums Fri (base) + Sat (×1.25) + Sun (×1.25); room flips to BOOKED; StaffNotifier prints `[STAFF] New booking`. Guest cancels 5 days out under **Moderate** policy → `days≥2` → full refund; room returns to AVAILABLE. Switch to **Strict** and the same 5-day cancellation refunds 0 — the service didn't change, only the injected policy did."*

---

## 🛗 Problem 7: Elevator System — *Practice Exercise 2 (hardest)*

### Step 1 — Clarify
*"N floors, M elevators? External hall buttons (UP/DOWN per floor) and internal car buttons (floor numbers)? A **scheduler** decides which car serves a request? Cars are stateful — IDLE / MOVING_UP / MOVING_DOWN / DOOR_OPEN. And floor displays update as cars move? Yes — so this is **State + Strategy + Observer** all at once."*

### Entities & relationships

```
ElevatorSystem (controller)
   ├─has M─▶ Elevator (current floor, ElevatorState 🚦, request set) ─notifies─▶ FloorDisplay 📣
   └─USES──▶ SchedulingStrategy 🎯 (Nearest / FCFS / LOOK)
Request (floor, Direction)
```

### Key Java skeletons

```java
import java.util.*;

enum Direction { UP, DOWN, IDLE }
enum ElevatorState { IDLE, MOVING_UP, MOVING_DOWN, DOOR_OPEN }

class Request {
    final int floor; final Direction dir;
    Request(int floor, Direction dir) { this.floor = floor; this.dir = dir; }
}

// ---- Observer: floor display panels ----
interface ElevatorObserver { void onMove(int elevatorId, int floor, ElevatorState state); }
class FloorDisplay implements ElevatorObserver {
    public void onMove(int id, int floor, ElevatorState s) {
        System.out.println("[DISPLAY] Elevator " + id + " at floor " + floor + " (" + s + ")");
    }
}

class Elevator {
    final int id;
    private int currentFloor = 0;
    private ElevatorState state = ElevatorState.IDLE;
    private final TreeSet<Integer> upStops = new TreeSet<>();              // sorted ascending
    private final TreeSet<Integer> downStops = new TreeSet<>(Collections.reverseOrder());
    private final List<ElevatorObserver> observers = new ArrayList<>();

    Elevator(int id) { this.id = id; }
    void subscribe(ElevatorObserver o) { observers.add(o); }

    void addStop(int floor) {                       // State decides which queue
        if (floor > currentFloor) upStops.add(floor);
        else if (floor < currentFloor) downStops.add(floor);
    }
    int distanceTo(int floor) { return Math.abs(currentFloor - floor); }
    int getCurrentFloor() { return currentFloor; }
    ElevatorState getState() { return state; }

    // one simulation tick: move toward the next stop (LOOK-style: finish current direction first)
    void step() {
        TreeSet<Integer> active = (state == ElevatorState.MOVING_DOWN) ? downStops : upStops;
        if (active.isEmpty()) active = upStops.isEmpty() ? downStops : upStops;
        if (active.isEmpty()) { state = ElevatorState.IDLE; return; }

        int target = active.first();
        if (currentFloor < target)      { currentFloor++; state = ElevatorState.MOVING_UP; }
        else if (currentFloor > target) { currentFloor--; state = ElevatorState.MOVING_DOWN; }
        if (currentFloor == target) {                 // arrived → open doors
            active.remove(target);
            state = ElevatorState.DOOR_OPEN;
        }
        notifyObservers();
    }
    private void notifyObservers() {
        for (ElevatorObserver o : observers) o.onMove(id, currentFloor, state);
    }
}

// ---- Strategy: which elevator serves a request ----
interface SchedulingStrategy { Elevator pick(List<Elevator> cars, Request req); }

class NearestElevatorStrategy implements SchedulingStrategy {
    public Elevator pick(List<Elevator> cars, Request req) {
        Elevator best = null; int bestDist = Integer.MAX_VALUE;
        for (Elevator e : cars) {
            int d = e.distanceTo(req.floor);
            if (d < bestDist) { bestDist = d; best = e; }
        }
        return best;
    }
}
class FCFSStrategy implements SchedulingStrategy {           // first car, simplest baseline
    public Elevator pick(List<Elevator> cars, Request req) { return cars.get(0); }
}

class ElevatorSystem {
    private final List<Elevator> cars = new ArrayList<>();
    private SchedulingStrategy scheduler = new NearestElevatorStrategy();
    ElevatorSystem(int m) {
        for (int i = 0; i < m; i++) {
            Elevator e = new Elevator(i);
            e.subscribe(new FloorDisplay());               // wire Observer
            cars.add(e);
        }
    }
    void setScheduler(SchedulingStrategy s) { this.scheduler = s; }
    void request(Request req) {
        Elevator chosen = scheduler.pick(cars, req);        // Strategy chooses the car
        chosen.addStop(req.floor);
        System.out.println("Request floor " + req.floor + " → Elevator " + chosen.id);
    }
    void runTick() { cars.forEach(Elevator::step); }        // advance simulation
}
```

### Patterns & SOLID

| Pattern | Where | Why |
|---------|-------|-----|
| **State** 🚦 | `ElevatorState` drives `step()` | Movement logic depends on IDLE/MOVING_UP/DOWN/DOOR_OPEN. |
| **Strategy** 🎯 | `SchedulingStrategy` | Dispatch policy (Nearest / FCFS / LOOK) swappable at runtime — testable in isolation. |
| **Observer** 📣 | `ElevatorObserver` / `FloorDisplay` | Displays update automatically as cars move; add a logger later for free. |
| **Open/Closed** | new scheduler = new class | Algorithm research without touching `ElevatorSystem`. |
| **Single Responsibility** | Elevator (motion) vs System (dispatch) | Each has one job. |

### Walkthrough
*"5 requests arrive. For each, the **NearestElevator Strategy** scans cars and picks the closest, adding the floor to that car's up/down `TreeSet` (sorted so we serve floors in order — LOOK behavior). Each `runTick()` moves every car one floor toward its next stop, flips its **State**, and the **Observer** prints the floor-panel update. Swap to **FCFS** and dispatch changes with zero edits to the elevators. To go production I'd add real LOOK direction-reversal and concurrency locks on the request queues."*

> 🎯 **Senior signal:** *"The scheduling Strategy is the part interviewers probe. I'd discuss SCAN vs LOOK vs nearest-car and the trade-off between throughput and worst-case wait time."*

---

## 🎬 Problem 8: BookMyShow (Movie Booking) — *Practice Exercise 3*

### Step 1 — Clarify
*"Movies → theaters → screens → shows, each show with a seat map (Silver/Gold/Platinum)? The crux: **prevent double-booking** with a temporary seat **hold (5-min lock)**, releasing on timeout/failure? And confirmation notifications? Yes — so **State** on seats, **Strategy** on pricing, **Observer** on confirmation, and careful concurrency talk."*

### Entities & relationships

```
BookingService (Facade)
   Movie ─shown at─▶ Theater ─has─▶ Screen ─hosts─▶ Show ─has─▶ ShowSeat (SeatState 🚦)
   Booking (user, show, seats, amount, BookingStatus)
   PricingStrategy 🎯 (by category, weekday/weekend)
   BookingObserver 📣 (confirmation notifier)
```

### Key Java skeletons

```java
import java.time.*;
import java.util.*;

enum SeatCategory { SILVER, GOLD, PLATINUM }
enum SeatState { AVAILABLE, HELD, BOOKED }           // State pattern on each seat
enum BookingStatus { PENDING, CONFIRMED, FAILED }

// ---- Strategy: seat pricing ----
interface PricingStrategy { double price(SeatCategory cat, LocalDate date); }
class StandardPricing implements PricingStrategy {
    private static final Map<SeatCategory, Double> BASE = Map.of(
        SeatCategory.SILVER, 150.0, SeatCategory.GOLD, 250.0, SeatCategory.PLATINUM, 400.0);
    public double price(SeatCategory c, LocalDate d) {
        double p = BASE.get(c);
        DayOfWeek w = d.getDayOfWeek();
        if (w == DayOfWeek.SATURDAY || w == DayOfWeek.SUNDAY) p *= 1.3; // weekend +30%
        return p;
    }
}

// ---- Observer: confirmation ----
interface BookingObserver { void onConfirmed(Booking b); }
class NotificationService implements BookingObserver {
    public void onConfirmed(Booking b) {
        System.out.println("[NOTIFY] Booking " + b.id + " confirmed for " + b.user);
    }
}

class ShowSeat {
    final String seatNo; final SeatCategory category;
    private SeatState state = SeatState.AVAILABLE;
    private LocalDateTime heldUntil;
    private static final int HOLD_MINUTES = 5;        // 5-min lock window

    ShowSeat(String seatNo, SeatCategory c) { this.seatNo = seatNo; this.category = c; }

    synchronized boolean hold() {                     // State transition: AVAILABLE → HELD
        if (state == SeatState.AVAILABLE ||
            (state == SeatState.HELD && heldUntil.isBefore(LocalDateTime.now()))) { // expired hold reclaimed
            state = SeatState.HELD;
            heldUntil = LocalDateTime.now().plusMinutes(HOLD_MINUTES);
            return true;
        }
        return false;                                 // already HELD (live) or BOOKED → reject
    }
    synchronized void confirm() { if (state == SeatState.HELD) state = SeatState.BOOKED; }
    synchronized void release() { if (state == SeatState.HELD) { state = SeatState.AVAILABLE; heldUntil = null; } }
    SeatState getState() { return state; }
    SeatCategory getCategory() { return category; }
}

class Show {
    final String id; final LocalDate date;
    final Map<String, ShowSeat> seats = new LinkedHashMap<>();
    Show(String id, LocalDate date) { this.id = id; this.date = date; }
    void addSeat(ShowSeat s) { seats.put(s.seatNo, s); }
}

class Booking {
    private static int counter = 0;
    final String id; final String user; final Show show; final List<ShowSeat> seats;
    double amount; BookingStatus status = BookingStatus.PENDING;
    Booking(String user, Show show, List<ShowSeat> seats) {
        this.id = String.format("BMS-%04d", ++counter);
        this.user = user; this.show = show; this.seats = seats;
    }
}

class BookingService {                                 // Facade
    private PricingStrategy pricing = new StandardPricing();
    private final List<BookingObserver> observers = new ArrayList<>();
    void subscribe(BookingObserver o) { observers.add(o); }

    // Step 1: try to hold all requested seats atomically
    Booking reserve(String user, Show show, List<String> seatNos) {
        List<ShowSeat> held = new ArrayList<>();
        for (String no : seatNos) {
            ShowSeat seat = show.seats.get(no);
            if (seat == null || !seat.hold()) {        // any failure → roll back
                held.forEach(ShowSeat::release);
                System.out.println("Seat " + no + " unavailable — reservation failed");
                return null;
            }
            held.add(seat);
        }
        Booking b = new Booking(user, show, held);
        b.amount = held.stream().mapToDouble(s -> pricing.price(s.getCategory(), show.date)).sum();
        return b;                                       // seats are HELD, awaiting payment
    }
    // Step 2: payment result decides confirm vs release
    boolean pay(Booking b, boolean paymentOk) {
        if (paymentOk) {
            b.seats.forEach(ShowSeat::confirm);         // HELD → BOOKED
            b.status = BookingStatus.CONFIRMED;
            observers.forEach(o -> o.onConfirmed(b));   // Observer fires
            return true;
        }
        b.seats.forEach(ShowSeat::release);             // HELD → AVAILABLE
        b.status = BookingStatus.FAILED;
        return false;
    }
}
```

### Patterns & SOLID

| Pattern | Where | Why |
|---------|-------|-----|
| **State** 🚦 | `SeatState` AVAILABLE→HELD→BOOKED | The whole anti-double-booking flow is a state machine. |
| **Strategy** 🎯 | `PricingStrategy` | Per-category + weekend pricing, swappable. |
| **Observer** 📣 | `BookingObserver` | Confirmation to user/SMS/email on success. |
| **Facade** 🎭 | `BookingService` | `reserve` + `pay` hide holds, pricing, rollback, notify. |

### The double-booking edge case (say this — it's the whole point)
*"Two users grab seat A1 at the same instant. `hold()` is `synchronized`, so it's atomic: the first wins (AVAILABLE→HELD), the second sees HELD-and-live → rejected. If the first user's payment fails or the 5-minute window lapses, `release()` (or the next `hold()` reclaiming an **expired** hold) returns A1 to AVAILABLE. I also **roll back** partial holds — if I grabbed A1 but A2 fails, I release A1 so I never strand seats."*

### Walkthrough
*"User picks A1(Gold), A2(Gold) → `reserve()` holds both, sums weekend Gold price (250×1.3 each). Payment gateway returns OK → `pay()` flips both HELD→BOOKED, status CONFIRMED, **NotificationService** sends confirmation. If payment had failed, both seats return to AVAILABLE for the next customer. For real scale I'd move the lock to Redis with a TTL so it works across servers."*

---

## 🍔 Problem 9: Food Delivery (Zomato/Swiggy) — *Practice Exercise 4 (most comprehensive)*

### Step 1 — Clarify
*"Restaurants with menus, search/sort, cart → order, assign a delivery partner, and an order **lifecycle** (PLACED→CONFIRMED→PREPARING→OUT_FOR_DELIVERY→DELIVERED) where each transition **notifies** user + restaurant + partner? Plus delivery-fee variants? This one uses **every** pattern — Factory, Strategy, Observer, State, Facade, Singleton."*

### Entities & relationships

```
OrderService (Facade)
   Restaurant ─has─▶ MenuItem        DeliveryPartnerPool (Singleton 1️⃣)
   Cart ─builds─▶ Order (OrderState 🚦) ─assigned─▶ DeliveryPartner
   DeliveryFeeStrategy 🎯 (Flat / Distance / FreeAbove)
   NotificationFactory 🏭 → Notification (Push / SMS / Email)
   OrderObserver 📣 (User, Restaurant, Partner)
```

### Key Java skeletons

```java
import java.util.*;

enum OrderState { PLACED, CONFIRMED, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED }
enum NotificationType { PUSH, SMS, EMAIL }

class MenuItem { final String name; final double price; final String category;
    MenuItem(String n, double p, String c){name=n;price=p;category=c;} }

class Restaurant {
    final String id, name, cuisine; double rating;
    final List<MenuItem> menu = new ArrayList<>();
    Restaurant(String id, String name, String cuisine, double rating){
        this.id=id;this.name=name;this.cuisine=cuisine;this.rating=rating; }
}

// ---- Factory: build the right notification channel ----
interface Notification { void send(String to, String msg); }
class PushNotification  implements Notification { public void send(String t,String m){ System.out.println("[PUSH→"+t+"] "+m);} }
class SmsNotification   implements Notification { public void send(String t,String m){ System.out.println("[SMS→"+t+"] "+m);} }
class EmailNotification implements Notification { public void send(String t,String m){ System.out.println("[EMAIL→"+t+"] "+m);} }
class NotificationFactory {
    static Notification create(NotificationType type) {
        switch (type) {
            case PUSH:  return new PushNotification();
            case SMS:   return new SmsNotification();
            case EMAIL: return new EmailNotification();
            default: throw new IllegalArgumentException("unknown type");
        }
    }
}

// ---- Strategy: delivery fee ----
interface DeliveryFeeStrategy { double fee(double cartTotal, double distanceKm); }
class FlatFee implements DeliveryFeeStrategy { public double fee(double t,double d){ return 40; } }
class DistanceFee implements DeliveryFeeStrategy { public double fee(double t,double d){ return 10 * d; } } // Rs10/km
class FreeAbove500 implements DeliveryFeeStrategy {
    public double fee(double t,double d){ return t >= 500 ? 0 : 50; } }

// ---- Observer: order status listeners ----
interface OrderObserver { void onStatusChange(Order o, OrderState s); }
class CustomerObserver implements OrderObserver {
    public void onStatusChange(Order o, OrderState s){ System.out.println("[CUSTOMER] Order "+o.id+" → "+s); } }
class RestaurantObserver implements OrderObserver {
    public void onStatusChange(Order o, OrderState s){ if(s==OrderState.CONFIRMED) System.out.println("[KITCHEN] start "+o.id); } }
class PartnerObserver implements OrderObserver {
    public void onStatusChange(Order o, OrderState s){ if(s==OrderState.OUT_FOR_DELIVERY) System.out.println("[PARTNER] pick up "+o.id); } }

// ---- Singleton: delivery partner pool ----
class DeliveryPartnerPool {
    private static final DeliveryPartnerPool INSTANCE = new DeliveryPartnerPool();
    private final Deque<String> freePartners = new ArrayDeque<>(List.of("Ravi","Sunil","Manoj"));
    private DeliveryPartnerPool() {}
    static DeliveryPartnerPool getInstance() { return INSTANCE; }
    synchronized String assign() { return freePartners.isEmpty() ? null : freePartners.poll(); }
    synchronized void release(String p) { freePartners.offer(p); }
}

class Order {
    private static int counter = 0;
    final String id; final Restaurant restaurant; final List<MenuItem> items;
    double itemTotal, deliveryFee; String partner;
    private OrderState state = OrderState.PLACED;
    private final List<OrderObserver> observers = new ArrayList<>();

    Order(Restaurant r, List<MenuItem> items) {
        this.id = String.format("ORD-%04d", ++counter);
        this.restaurant = r; this.items = items;
        this.itemTotal = items.stream().mapToDouble(i -> i.price).sum();
    }
    void subscribe(OrderObserver o) { observers.add(o); }
    void transitionTo(OrderState next) {              // State + Observer in one place
        this.state = next;
        observers.forEach(o -> o.onStatusChange(this, next));
    }
    OrderState getState() { return state; }
}

class OrderService {                                   // Facade orchestrating everything
    private DeliveryFeeStrategy feeStrategy = new FreeAbove500();
    void setFeeStrategy(DeliveryFeeStrategy s) { this.feeStrategy = s; }

    Order placeOrder(Restaurant r, List<MenuItem> items, double distanceKm, String customerEmail) {
        Order order = new Order(r, items);
        order.deliveryFee = feeStrategy.fee(order.itemTotal, distanceKm);     // Strategy
        order.subscribe(new CustomerObserver());                              // Observer wiring
        order.subscribe(new RestaurantObserver());
        order.subscribe(new PartnerObserver());

        order.transitionTo(OrderState.CONFIRMED);                            // State machine drives flow
        order.partner = DeliveryPartnerPool.getInstance().assign();          // Singleton pool
        order.transitionTo(OrderState.PREPARING);
        order.transitionTo(OrderState.OUT_FOR_DELIVERY);
        order.transitionTo(OrderState.DELIVERED);
        DeliveryPartnerPool.getInstance().release(order.partner);

        NotificationFactory.create(NotificationType.EMAIL)                   // Factory
            .send(customerEmail, "Your order " + order.id + " is delivered! Total Rs."
                  + (order.itemTotal + order.deliveryFee));
        return order;
    }
}
```

### Patterns & SOLID — the full house

| Pattern | Where | Why |
|---------|-------|-----|
| **Factory** 🏭 | `NotificationFactory` | Create Push/SMS/Email without `new` scattered everywhere. |
| **Strategy** 🎯 | `DeliveryFeeStrategy` | Flat / distance / free-above-500, swap per campaign. |
| **Observer** 📣 | `OrderObserver` trio | One status change fans out to customer, kitchen, partner. |
| **State** 🚦 | `OrderState` lifecycle | Each transition is explicit and triggers side-effects. |
| **Singleton** 1️⃣ | `DeliveryPartnerPool` | One shared pool of partners across all orders; `synchronized` assign/release. |
| **Facade** 🎭 | `OrderService` | `placeOrder` orchestrates fee + assignment + lifecycle + notify. |

### Walkthrough
*"Customer orders 2 items (Rs.520) 3 km away. `placeOrder` → **FreeAbove500 Strategy** → total ≥ 500 → delivery free. We wire three **Observers**, then drive the **State machine**: CONFIRMED (kitchen starts), grab a partner from the **Singleton** pool, PREPARING → OUT_FOR_DELIVERY (partner picks up) → DELIVERED, release the partner. Finally the **Factory** builds an Email notification. Every pattern earns its place — none is decoration. This is the closest to a real end-to-end Microsoft LLD."*

---

# PART B — Common LLD Interview Follow-up Q&A

> These are the *follow-ups* that separate a mid from a senior. Read the model answers out loud until they're muscle memory.

### Q1. "How would you make the Parking Lot (or any of these) thread-safe?"
**A:** *"Concurrent threads racing for the **same** spot is the danger — two cars getting the same slot. I'd protect the critical section: synchronize `parkVehicle`/`exitVehicle`, or finer-grained, make `ParkingSpot.park()`/`release()` atomic with a per-spot lock or a CAS on an `AtomicBoolean occupied`. For the active-tickets map I'd use `ConcurrentHashMap`. The principle: lock the **smallest** region that guarantees correctness, so I don't serialize the whole lot and kill throughput."*

### Q2. "How do you prevent double-booking in BookMyShow under high concurrency?"
**A:** *"A seat is a tiny state machine guarded by a lock. `hold()` is `synchronized` (or a DB row lock / Redis `SETNX` with TTL at scale) so only one thread flips AVAILABLE→HELD. Holds **expire** (5-min TTL) so a crashed checkout doesn't freeze the seat forever. And I **roll back** partial holds so a failed multi-seat reservation never strands seats. At web scale the lock lives in Redis, not in-process, so it works across many app servers."*

### Q3. "Which pattern fits when behavior must vary, and how is it different from inheritance?"
**A:** *"**Strategy** when an *algorithm* varies (pricing, splitting, scheduling) — you compose a swappable object and can change it at **runtime**. Inheritance bakes the variation into the type at **compile time** and risks a class explosion. Strategy favors **composition over inheritance** and honors **Open/Closed**: add a new strategy class, edit nothing. Rule of thumb: if you see a growing `if/else`/`switch` on a 'type', that's a Strategy waiting to happen."*

### Q4. "Strategy vs State — they look identical. When do you use which?"
**A:** *"Structurally similar (both delegate to a pluggable object), but **intent** differs. **Strategy** = the *client* picks the algorithm, and strategies don't know about each other (fee = hourly vs flat). **State** = the *object* transitions itself between states, and states often know the next state (seat AVAILABLE→HELD→BOOKED, order PLACED→CONFIRMED→…). Strategy is about *how*; State is about *what phase am I in*."*

### Q5. "How would you extend the Parking Lot for electric-vehicle charging spots?"
**A:** *"Add `EV` to `VehicleType` and a `CHARGING` `SpotType`, map them, and add an `EVChargingFeeStrategy` (time-of-parking + kWh). Because pricing is **Strategy** and the vehicle→spot rule is a centralized map, the orchestrator needs **zero** changes — pure **Open/Closed**. I'd also let an EV fall back to a REGULAR spot if no charger is free."*

### Q6. "Why HashMap **and** a doubly-linked list for LRU? Can't a LinkedHashMap do it?"
**A:** *"I need O(1) lookup (HashMap) **and** O(1) reordering by recency (linked list). A singly-linked list can't unlink a middle node in O(1) — no `prev` — so it must be **doubly**-linked. And yes, Java's `LinkedHashMap` with `accessOrder=true` and an overridden `removeEldestEntry` *is* a real LRU in production code — I built it from scratch here to demonstrate I understand the mechanics, which is what the interviewer is actually testing."*

### Q7. "Your Splitwise uses doubles for money — what's wrong and how do you fix it?"
**A:** *"Floating-point can't represent 0.1 exactly, so splits silently drift and don't sum to the total. Two fixes: (1) compute in **integer paise** (`long`), or (2) use `BigDecimal` with an explicit rounding mode. I also assign the rounding **remainder** deterministically (to the payer or the first participant) so the books always balance to the cent."*

### Q8. "How would you scale the Elevator scheduler from a toy to a real building?"
**A:** *"Swap the **Strategy** from nearest-car to proper **LOOK/SCAN**: a car finishes all requests in its current direction before reversing, minimizing total travel. I'd factor in current load and direction-compatibility (don't send an UP-bound car to a DOWN request below it). Each car's request queues need concurrency protection. Because dispatch is a Strategy, I can A/B-test algorithms by injecting a different one — no elevator code changes."*

### Q9. "Where does the Observer pattern break down, and how do you harden it?"
**A:** *"Three classic problems: (1) **memory leaks** — observers that never unsubscribe (I'd offer `unsubscribe` and/or weak references); (2) **slow/blocking observers** stalling the publisher (push events onto an **async queue** so notification is fire-and-forget); (3) **exceptions** in one observer killing the loop (wrap each `notify` in try/catch so one bad listener doesn't break the rest)."*

### Q10. "How do you handle an order that fails mid-lifecycle in Food Delivery?"
**A:** *"The **State machine** makes this clean: I add a `CANCELLED`/`FAILED` state and only allow legal transitions (you can't go DELIVERED→PREPARING). On failure I run **compensating actions** — release the partner back to the **Singleton** pool, refund payment, notify all **Observers** of the cancellation. Centralizing transitions in `transitionTo()` means I validate every move in one place instead of scattered `if`s."*

### Q11. "When is a Singleton an anti-pattern, and what would you use instead?"
**A:** *"Singletons hide global state, make unit testing hard (you can't inject a mock), and become a concurrency hotspot. I keep them for genuinely-one-of-a-kind, stateless-ish coordinators (a connection pool, a config). Otherwise I prefer **dependency injection** — pass one shared instance explicitly — which keeps the class testable and the dependency visible. If I must use Singleton, I make it thread-safe (eager init or holder idiom) and keep its surface tiny."*

### Q12. "Walk me through how you'd add a new feature to one of these systems without touching existing code."
**A:** *"That's literally the **Open/Closed Principle** test. For each system I built a seam: Parking pricing = `FeeStrategy`, Splitwise splitting = `SplitStrategy`, Elevator dispatch = `SchedulingStrategy`, notifications = `NotificationFactory`. To add behavior I write a **new class** implementing the existing interface and inject it. The interviewer is checking whether my abstractions are in the right place — if a 'simple' new feature forces edits across many files, my design was too rigid."*

---

# 🧠 Memory Hooks

> Burn this table into memory. In the interview, recall the **key objects** + the **anchor pattern**, and the rest of the design unfolds from there.

| # | LLD Problem | 🔑 Key Objects (the nouns) | 🎯 Anchor Pattern(s) | Mnemonic |
|---|-------------|----------------------------|----------------------|----------|
| 1 | **Parking Lot** | Lot → Floor → Spot → Vehicle, Ticket | **Strategy** (fees) + Singleton | *"Price changes ⇒ Strategy"* |
| 2 | **LRU Cache** | HashMap + Doubly Linked List + Node | None (DS) — *Strategy* for eviction | *"Map for speed, list for order"* |
| 3 | **Library** | Library → Book → Member → BorrowRecord | **Observer** (waitlist) + Facade | *"Book returns ⇒ tell the waitlist"* |
| 4 | **Splitwise** | Group → Expense → User, Balances | **Strategy** (split) + greedy settle | *"Split varies ⇒ Strategy"* |
| 5 | **Tic Tac Toe** | Game → Board → Player, Symbol | **State** (in-progress/won/draw) | *"Game phase ⇒ State"* |
| 6 | **Hotel Booking** | Hotel → Room → Booking | **Strategy ×2** (price + cancel) + Observer | *"Two policies ⇒ two Strategies"* |
| 7 | **Elevator** | System → Elevator → Request, Display | **State + Strategy + Observer** | *"Move=State, dispatch=Strategy, display=Observer"* |
| 8 | **BookMyShow** | Show → ShowSeat → Booking | **State** (seat lock) + Strategy + Observer | *"Seat lifecycle ⇒ State machine"* |
| 9 | **Food Delivery** | Restaurant → Order → Partner | **ALL six** (Factory/Strategy/Observer/State/Singleton/Facade) | *"The everything-bagel"* |

### 🪝 Pattern-trigger cheat sheet (spot the keyword → name the pattern)
- *"...varies / pricing / algorithm / policy..."* → **Strategy** 🎯
- *"...notify / when X happens / subscribe / waitlist..."* → **Observer** 📣
- *"...lifecycle / phases / transitions / can't do X when Y..."* → **State** 🚦
- *"...create different kinds of..."* → **Factory** 🏭
- *"...exactly one / shared pool / global..."* → **Singleton** 1️⃣
- *"...simple interface hiding complexity / orchestrates..."* → **Facade** 🎭

---

# 🎤 Final Interview Tip

> **Narrate your thinking like a tour guide, not a magician.** 🗣️ The interviewer can't read your mind — they score what you *say*. So as you design: name the noun ("this becomes a class"), justify each method ("a Ticket tracks time, not money — Single Responsibility"), and **every single time** you reach for a pattern, say its name *and the reason*: *"Pricing varies, so I'll use **Strategy** here — that keeps the lot **Open for extension, Closed for modification**."*
>
> Start **small and correct** (one floor, one strategy), get it working, *then* extend — a clean 70%-complete design beats a sprawling broken one. Always close by **walking one real scenario end-to-end** to prove the wiring works, and **volunteer the trade-offs** (thread-safety, scale, edge cases) before they ask. Do that, and you're not just coding — you're demonstrating exactly the senior judgment Microsoft is hiring for. You've got this. 💪
