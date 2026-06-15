# Week 5: More Design Patterns + UML Diagrams

> Missing patterns that complete the GoF set, plus UML diagrams you'll need
> in every interview and system design round.

---

# PART 1: UML DIAGRAMS

---

## 1.1 What is UML?

UML = Unified Modeling Language.

Think of it like the **blueprint of a building**. Before the builder starts
laying bricks, the architect draws diagrams. UML is the architect's drawing
for software.

Two main categories:
- **Structural Diagrams** — what the system IS (Class Diagram, Component Diagram)
- **Behavioral Diagrams** — what the system DOES (Sequence Diagram, Activity Diagram)

We focus on the two most asked in interviews: **Class Diagram** and **Sequence Diagram**.

---

## 1.2 Class Diagram

The most important UML diagram. Every LLD interview expects you to draw one.

### The Box

Every class is a **box divided into 3 sections**:

```
+---------------------------+
|       ClassName           |   <-- Section 1: Name
+---------------------------+
| - name: String            |   <-- Section 2: Attributes (fields)
| - age: int                |
| # id: UUID                |
+---------------------------+
| + getName(): String       |   <-- Section 3: Methods
| + setName(String): void   |
| - validate(): boolean     |
+---------------------------+
```

### Visibility Markers

| Symbol | Meaning   | Analogy                                      |
|--------|-----------|----------------------------------------------|
| `+`    | public    | Your name on your visiting card — anyone can see it |
| `-`    | private   | Your ATM PIN — only you know it              |
| `#`    | protected | Family recipe — only family (subclasses) can access |
| `~`    | package   | Colony WhatsApp group — only people in your colony (package) |

### Relationships Between Classes

This is where it gets interesting. There are 5 key relationships:

---

#### 1. Association (Solid Line)

**"knows about" / "uses"**

```
+----------+          +----------+
|  Teacher |--------->|  Student |
+----------+          +----------+
```

A Teacher knows about Students. That's it. No ownership, no lifecycle dependency.

**Indian analogy:** A shopkeeper knows his regular customers. If the shop closes,
customers still exist. If a customer stops coming, shopkeeper still exists.

**In Java:**
```java
class Teacher {
    private List<Student> students;  // Teacher HAS reference to students
}
```

---

#### 2. Aggregation (Empty Diamond ◇)

**"has-a" but parts can exist independently**

```
+---------------+          +---------------+
|  University   |◇-------->|   Professor   |
+---------------+          +---------------+
```

The diamond is on the **container** side (University).

**Indian analogy:** A university has professors. If the university shuts down
(like many private colleges do!), professors don't disappear — they go teach
somewhere else. The parts survive without the whole.

**More examples:**
- Department ◇→ Employee (dept closes, employee transfers)
- Library ◇→ Book (library closes, books can be moved)
- Cricket Team ◇→ Player (team disbands, players join other teams)

**In Java:**
```java
class University {
    private List<Professor> professors;  // professors exist independently

    // Professors are passed in, not created here
    public University(List<Professor> professors) {
        this.professors = professors;
    }
}
```

---

#### 3. Composition (Filled Diamond ◆)

**"has-a" but parts CANNOT exist independently**

```
+----------+          +----------+
|  House   |◆-------->|   Room   |
+----------+          +----------+
```

**Indian analogy:** A house has rooms. Demolish the house — rooms are destroyed
too. Rooms don't exist floating around without a house.

**More examples:**
- Human Body ◆→ Heart (body dies, heart is gone)
- Order ◆→ OrderItem (cancel order, items are meaningless)
- Chat ◆→ Messages (delete chat, messages gone)

**In Java:**
```java
class House {
    private List<Room> rooms;

    public House(int numberOfRooms) {
        rooms = new ArrayList<>();
        for (int i = 0; i < numberOfRooms; i++) {
            rooms.add(new Room());  // House CREATES the rooms
        }
    }
    // When House object is garbage collected, Rooms go too
}
```

**Key difference from Aggregation:**
| Aspect       | Aggregation (◇)         | Composition (◆)           |
|-------------|-------------------------|---------------------------|
| Lifecycle   | Parts outlive the whole  | Parts die with the whole   |
| Creation    | Parts created outside    | Parts created inside       |
| Ownership   | Weak "has-a"            | Strong "owns"              |
| Example     | University-Professor     | House-Room                 |

---

#### 4. Inheritance / Generalization (Triangle Arrow △)

**"is-a" relationship**

```
+----------+
|  Animal  |
+----------+
     △
     |
+----------+
|   Dog    |
+----------+
```

Triangle points toward the **parent**. Dog IS-A Animal.

**Indian analogy:** All Maruti cars (Swift, Alto, Baleno) are Cars. They inherit
the basic properties of a car (engine, wheels, steering) but add their own
specializations.

**In Java:**
```java
class Animal {
    protected String name;
    public void eat() { ... }
}

class Dog extends Animal {
    public void bark() { ... }  // Dog's own behavior
}
```

---

#### 5. Interface Implementation (Dashed Triangle Arrow)

**"can-do" / "behaves-like"**

```
+-----------+
| <<interface>> |
|  Flyable  |
+-----------+
     △ (dashed)
     |
+-----------+
|   Bird    |
+-----------+
```

Dashed line = "I promise to implement these methods."

**Indian analogy:** Think of it like a government license. If you have a
driving license (Driveable interface), you promise you can drive(). Whether
you're a truck driver, auto-rickshaw driver, or cab driver — all must
fulfill the promise.

**In Java:**
```java
interface Flyable {
    void fly();
}

class Bird implements Flyable {
    @Override
    public void fly() {
        System.out.println("Flapping wings!");
    }
}

class Airplane implements Flyable {
    @Override
    public void fly() {
        System.out.println("Using jet engines!");
    }
}
```

---

### Quick Reference: All Relationships

```
A ──────> B        Association (A knows B)
A ◇─────> B        Aggregation (A has B, B can exist alone)
A ◆─────> B        Composition (A owns B, B dies with A)
A ───△──> B        Inheritance (A is-a B)
A ---△--> B        Interface (A implements B)  [dashed line]
A -------> B       Dependency (A temporarily uses B) [dashed arrow]
```

---

### ASCII Class Diagram: Parking Lot System

This is one of the most asked LLD questions. Here's the class diagram:

```
+----------------------------------------------------------+
|                     ParkingLot                            |
+----------------------------------------------------------+
| - name: String                                           |
| - floors: List<ParkingFloor>                             |
| - entryPanels: List<EntryPanel>                          |
| - exitPanels: List<ExitPanel>                            |
+----------------------------------------------------------+
| + addFloor(ParkingFloor): void                           |
| + getAvailableSpot(VehicleType): ParkingSpot             |
+----------------------------------------------------------+
           ◆                    ◆                    ◆
           |                    |                    |
+-------------------+  +----------------+  +-----------------+
|   ParkingFloor    |  |   EntryPanel   |  |   ExitPanel     |
+-------------------+  +----------------+  +-----------------+
| - floorNumber: int|  | - panelId: int |  | - panelId: int  |
| - spots: List     |  +----------------+  +-----------------+
|   <ParkingSpot>   |  | + printTicket()|  | + scanTicket()  |
+-------------------+  |   : Ticket     |  | + processPayment|
| + getAvailableSpot|  +----------------+  |   (): Payment   |
|   (VehicleType)   |                      +-----------------+
|   : ParkingSpot   |
+-------------------+
           ◆
           |
+------------------------------+
|        ParkingSpot            |
+------------------------------+
| - spotId: String              |
| - spotType: SpotType          |
| - isAvailable: boolean        |
| - vehicle: Vehicle            |
+------------------------------+
| + assignVehicle(Vehicle): void|
| + removeVehicle(): void       |
| + isAvailable(): boolean      |
+------------------------------+
           |
           | (Association)
           |
+------------------------------+
|         Vehicle               |
+------------------------------+
| - licensePlate: String        |
| - vehicleType: VehicleType    |
| - color: String               |
+------------------------------+
           △
           |
    +------+--------+
    |               |
+--------+    +----------+
|  Car   |    |  Truck   |
+--------+    +----------+

+------------------------------+
|     <<enumeration>>          |
|       VehicleType            |
+------------------------------+
| CAR                          |
| TRUCK                        |
| BIKE                         |
| ELECTRIC                     |
+------------------------------+

+------------------------------+
|     <<enumeration>>          |
|        SpotType              |
+------------------------------+
| COMPACT                      |
| REGULAR                      |
| LARGE                        |
| ELECTRIC                     |
+------------------------------+

+------------------------------+
|         Ticket                |
+------------------------------+
| - ticketId: String            |
| - entryTime: LocalDateTime    |
| - vehicle: Vehicle            |
| - spot: ParkingSpot           |
+------------------------------+
| + calculateFee(): double      |
+------------------------------+
```

**Relationships explained:**
- ParkingLot ◆→ ParkingFloor: Composition. Lot demolished = floors gone.
- ParkingLot ◆→ EntryPanel, ExitPanel: Composition. They belong to this lot.
- ParkingFloor ◆→ ParkingSpot: Composition. Floor removed = spots gone.
- ParkingSpot → Vehicle: Association. Spot knows about vehicle, but vehicle
  exists independently.
- Car, Truck △→ Vehicle: Inheritance.

---

## 1.3 Sequence Diagram

Shows **how objects interact over time**. Think of it like a WhatsApp
conversation between objects — who sends what message to whom, and in what order.

### Structure

```
  Object1       Object2       Object3
    |              |              |
    |---message--->|              |     (Object1 calls Object2)
    |              |---message--->|     (Object2 calls Object3)
    |              |<--response---|     (Object3 returns to Object2)
    |<--response---|              |     (Object2 returns to Object1)
    |              |              |
```

**Key elements:**
- **Objects** at the top (boxes)
- **Lifelines** going down (vertical dashed lines)
- **Messages** as horizontal arrows (solid = call, dashed = return)
- **Time** flows top to bottom

---

### Example 1: User Login Flow

```
   User          LoginController    AuthService       Database
    |                  |                |                 |
    |--login(email,--->|                |                 |
    |    password)     |                |                 |
    |                  |--authenticate->|                 |
    |                  |   (email, pwd) |                 |
    |                  |                |--findUser------>|
    |                  |                |  (email)        |
    |                  |                |<--User object---|
    |                  |                |                 |
    |                  |                |--verify-------->|
    |                  |                |  password       |
    |                  |                |  (matches hash) |
    |                  |                |                 |
    |                  |<-JWT token-----|                 |
    |                  |  (or error)    |                 |
    |                  |                |                 |
    |<--200 OK---------|                |                 |
    |  {token: "xxx"}  |                |                 |
    |                  |                |                 |

    ALT [if password wrong]:
    |                  |                |                 |
    |                  |<--AUTH_FAILED--|                 |
    |<--401 Unauthorized|               |                 |
    |                  |                |                 |
```

**Reading this diagram (like reading a story):**
1. User sends login request with email + password
2. LoginController asks AuthService to authenticate
3. AuthService asks Database to find the user
4. Database returns the User object
5. AuthService verifies the password hash
6. If match → returns JWT token
7. If no match → returns AUTH_FAILED
8. Controller sends appropriate HTTP response back to User

---

### Example 2: Place Order Flow

This is a more complex, real-world example:

```
   User       OrderService    PaymentService   InventoryService    Database
    |              |                |                 |                |
    |--placeOrder->|                |                 |                |
    |  (items,     |                |                 |                |
    |   address,   |                |                 |                |
    |   paymentInfo)|               |                 |                |
    |              |                |                 |                |
    |              |--checkStock--->|                 |                |
    |              |   (items)      |--checkAvail---->|                |
    |              |                |  (itemIds)      |--query-------->|
    |              |                |                 |<--stock data---|
    |              |                |<--available/----|                |
    |              |                |   outOfStock    |                |
    |              |                |                 |                |
    |              | [IF all items available]          |                |
    |              |                |                 |                |
    |              |--processPayment|                 |                |
    |              |   (amount,     |                 |                |
    |              |    paymentInfo)|                 |                |
    |              |                |--charge-------->|                |
    |              |                |  (card/UPI)     |                |
    |              |                |<--success/fail--|                |
    |              |                |                 |                |
    |              |<--paymentResult|                 |                |
    |              |                |                 |                |
    |              | [IF payment successful]           |                |
    |              |                |                 |                |
    |              |--reserveItems->|                 |                |
    |              |   (items)      |--deductStock--->|                |
    |              |                |  (itemIds, qty) |--update------->|
    |              |                |                 |<--done---------|
    |              |                |<--reserved------|                |
    |              |                |                 |                |
    |              |--createOrder-->|                 |                |
    |              |   (all details)|                 |--save order--->|
    |              |                |                 |<--orderId------|
    |              |                |                 |                |
    |<--OrderConfirmation           |                 |                |
    |   (orderId,  |                |                 |                |
    |    ETA,       |                |                 |                |
    |    amount)    |                |                 |                |
    |              |                |                 |                |

    ALT [if payment fails]:
    |              |                |                 |                |
    |<--PaymentFailed               |                 |                |
    |  "Payment declined,           |                 |                |
    |   try another method"         |                 |                |
    |              |                |                 |                |

    ALT [if items out of stock]:
    |              |                |                 |                |
    |<--OutOfStock |                |                 |                |
    |  "Item XYZ is                 |                 |                |
    |   currently unavailable"      |                 |                |
    |              |                |                 |                |
```

**Indian analogy:** Think of ordering on Swiggy:
1. You place order (User → OrderService)
2. Swiggy checks if restaurant has the items (InventoryService)
3. If available, Swiggy processes your payment via Razorpay (PaymentService)
4. If payment works, the order is confirmed and restaurant starts cooking
5. If payment fails, you get "Payment failed, try again"
6. If item unavailable, you see "Item currently unavailable"

---

### When to Use Which Diagram?

| Diagram  | Shows               | Interview Use                          |
|----------|---------------------|----------------------------------------|
| Class    | Structure, relationships | "Design a Parking Lot / Library / ATM" |
| Sequence | Interactions over time   | "How does checkout work?" / API flows  |

---

# PART 2: SIX MISSING DESIGN PATTERNS

These complete your knowledge of the most important GoF patterns.

---

## Pattern 1: Chain of Responsibility

### The Concept

**Indian analogy:** You file a complaint at a government office. First the
clerk looks at it — "not my department." Passes it to the section officer —
"above my pay grade." Section officer passes to the department head. If even
the department head can't handle it, it goes to the minister.

Each person in the chain either:
1. **Handles** the request and stops, OR
2. **Passes** it to the next person

Nobody needs to know who else is in the chain. Each handler only knows about
the next one.

### Real Example: Leave Approval

```
Employee applies for leave:
  - 1-2 days → Team Lead can approve
  - 3-5 days → Manager can approve
  - 6-10 days → Director can approve
  - 11+ days → CEO must approve
```

### Java Code

```java
// Step 1: Abstract Handler
abstract class LeaveApprover {
    protected String name;
    protected LeaveApprover nextApprover;  // link to next in chain

    public LeaveApprover(String name) {
        this.name = name;
    }

    // Set the next handler in chain
    public void setNextApprover(LeaveApprover next) {
        this.nextApprover = next;
    }

    // Each subclass decides: handle or pass
    public abstract void approveLeave(LeaveRequest request);
}

// Simple data class for leave request
class LeaveRequest {
    private String employeeName;
    private int numberOfDays;

    public LeaveRequest(String employeeName, int numberOfDays) {
        this.employeeName = employeeName;
        this.numberOfDays = numberOfDays;
    }

    public String getEmployeeName() { return employeeName; }
    public int getNumberOfDays() { return numberOfDays; }
}

// Step 2: Concrete Handlers

class TeamLead extends LeaveApprover {
    public TeamLead(String name) { super(name); }

    @Override
    public void approveLeave(LeaveRequest request) {
        if (request.getNumberOfDays() <= 2) {
            System.out.println(name + " (Team Lead) approved "
                + request.getEmployeeName() + "'s leave for "
                + request.getNumberOfDays() + " days.");
        } else if (nextApprover != null) {
            System.out.println(name + " (Team Lead): Can't approve "
                + request.getNumberOfDays() + " days. Passing up...");
            nextApprover.approveLeave(request);
        } else {
            System.out.println("Nobody can approve this leave!");
        }
    }
}

class Manager extends LeaveApprover {
    public Manager(String name) { super(name); }

    @Override
    public void approveLeave(LeaveRequest request) {
        if (request.getNumberOfDays() <= 5) {
            System.out.println(name + " (Manager) approved "
                + request.getEmployeeName() + "'s leave for "
                + request.getNumberOfDays() + " days.");
        } else if (nextApprover != null) {
            System.out.println(name + " (Manager): Can't approve "
                + request.getNumberOfDays() + " days. Passing up...");
            nextApprover.approveLeave(request);
        } else {
            System.out.println("Nobody can approve this leave!");
        }
    }
}

class Director extends LeaveApprover {
    public Director(String name) { super(name); }

    @Override
    public void approveLeave(LeaveRequest request) {
        if (request.getNumberOfDays() <= 10) {
            System.out.println(name + " (Director) approved "
                + request.getEmployeeName() + "'s leave for "
                + request.getNumberOfDays() + " days.");
        } else if (nextApprover != null) {
            System.out.println(name + " (Director): Can't approve "
                + request.getNumberOfDays() + " days. Passing up...");
            nextApprover.approveLeave(request);
        } else {
            System.out.println("Nobody can approve this leave!");
        }
    }
}

class CEO extends LeaveApprover {
    public CEO(String name) { super(name); }

    @Override
    public void approveLeave(LeaveRequest request) {
        // CEO is the final authority — approves everything
        System.out.println(name + " (CEO) approved "
            + request.getEmployeeName() + "'s leave for "
            + request.getNumberOfDays() + " days.");
    }
}

// Step 3: Build the chain and use it
public class ChainOfResponsibilityDemo {
    public static void main(String[] args) {
        // Build the chain: TeamLead → Manager → Director → CEO
        LeaveApprover teamLead = new TeamLead("Rahul");
        LeaveApprover manager = new Manager("Priya");
        LeaveApprover director = new Director("Amit");
        LeaveApprover ceo = new CEO("Sundar");

        teamLead.setNextApprover(manager);
        manager.setNextApprover(director);
        director.setNextApprover(ceo);

        // Test different leave requests
        System.out.println("--- Request 1: 1 day leave ---");
        teamLead.approveLeave(new LeaveRequest("Karan", 1));

        System.out.println("\n--- Request 2: 4 day leave ---");
        teamLead.approveLeave(new LeaveRequest("Neha", 4));

        System.out.println("\n--- Request 3: 8 day leave ---");
        teamLead.approveLeave(new LeaveRequest("Rohan", 8));

        System.out.println("\n--- Request 4: 15 day leave ---");
        teamLead.approveLeave(new LeaveRequest("Anjali", 15));
    }
}
```

**Output:**
```
--- Request 1: 1 day leave ---
Rahul (Team Lead) approved Karan's leave for 1 days.

--- Request 2: 4 day leave ---
Rahul (Team Lead): Can't approve 4 days. Passing up...
Priya (Manager) approved Neha's leave for 4 days.

--- Request 3: 8 day leave ---
Rahul (Team Lead): Can't approve 8 days. Passing up...
Priya (Manager): Can't approve 8 days. Passing up...
Amit (Director) approved Rohan's leave for 8 days.

--- Request 4: 15 day leave ---
Rahul (Team Lead): Can't approve 15 days. Passing up...
Priya (Manager): Can't approve 15 days. Passing up...
Amit (Director): Can't approve 15 days. Passing up...
Sundar (CEO) approved Anjali's leave for 15 days.
```

### Where It's Used in Real Life

| Framework          | How Chain of Responsibility is used                        |
|--------------------|------------------------------------------------------------|
| **Servlet Filters** | Request passes through Filter1 → Filter2 → ... → Servlet |
| **Spring Security** | SecurityFilterChain: AuthFilter → CorsFilter → CsrfFilter |
| **Exception Handling** | catch blocks — try one, if not handled, next catch block |
| **Logging (Log4j)** | TRACE → DEBUG → INFO → WARN → ERROR → FATAL              |
| **ATM Dispenser**  | 2000 note handler → 500 → 200 → 100                       |

### Key Points for Interviews

- Decouples sender from receiver — sender doesn't know WHO handles it
- Can add/remove handlers without changing client code (Open-Closed Principle)
- Request might not be handled at all if chain is incomplete (be careful!)
- Different from if-else ladder: Chain of Responsibility allows each handler
  to be a separate class with its own logic, easily reorderable and extensible

---

## Pattern 2: Template Method

### The Concept

**Indian analogy:** Making chai and coffee. Both follow the SAME steps:

```
1. Boil water
2. Brew (tea leaves for chai, coffee powder for coffee)
3. Pour in cup
4. Add condiments (sugar+ginger for chai, sugar+cream for coffee)
```

The **template** (algorithm skeleton) is the same. Only the **specific steps**
differ. The parent class defines the template, child classes fill in the blanks.

### Java Code

```java
// Step 1: Abstract class with the template method
abstract class BeverageMaker {

    // THE TEMPLATE METHOD — defines the algorithm skeleton
    // "final" so no subclass can change the order of steps
    public final void prepare() {
        boilWater();
        brew();           // abstract — subclass fills this in
        pourInCup();
        addCondiments();  // abstract — subclass fills this in
        System.out.println("Your " + getBeverageName() + " is ready!\n");
    }

    // Common steps — same for all beverages
    private void boilWater() {
        System.out.println("Boiling water...");
    }

    private void pourInCup() {
        System.out.println("Pouring into cup...");
    }

    // Abstract steps — subclasses MUST provide these
    protected abstract void brew();
    protected abstract void addCondiments();
    protected abstract String getBeverageName();
}

// Step 2: Concrete implementations

class ChaiMaker extends BeverageMaker {
    @Override
    protected void brew() {
        System.out.println("Adding tea leaves, boiling with milk...");
    }

    @Override
    protected void addCondiments() {
        System.out.println("Adding sugar, ginger, cardamom...");
    }

    @Override
    protected String getBeverageName() {
        return "Masala Chai";
    }
}

class CoffeeMaker extends BeverageMaker {
    @Override
    protected void brew() {
        System.out.println("Dripping coffee through filter...");
    }

    @Override
    protected void addCondiments() {
        System.out.println("Adding sugar, cream...");
    }

    @Override
    protected String getBeverageName() {
        return "Filter Coffee";
    }
}

class GreenTeaMaker extends BeverageMaker {
    @Override
    protected void brew() {
        System.out.println("Steeping green tea leaves for 2 minutes...");
    }

    @Override
    protected void addCondiments() {
        System.out.println("Adding honey and lemon...");
    }

    @Override
    protected String getBeverageName() {
        return "Green Tea";
    }
}

// Step 3: Use it
public class TemplateMethodDemo {
    public static void main(String[] args) {
        BeverageMaker chai = new ChaiMaker();
        chai.prepare();

        BeverageMaker coffee = new CoffeeMaker();
        coffee.prepare();

        BeverageMaker greenTea = new GreenTeaMaker();
        greenTea.prepare();
    }
}
```

**Output:**
```
Boiling water...
Adding tea leaves, boiling with milk...
Pouring into cup...
Adding sugar, ginger, cardamom...
Your Masala Chai is ready!

Boiling water...
Dripping coffee through filter...
Pouring into cup...
Adding sugar, cream...
Your Filter Coffee is ready!

Boiling water...
Steeping green tea leaves for 2 minutes...
Pouring into cup...
Adding honey and lemon...
Your Green Tea is ready!
```

### Where It's Used in Real Life

| Framework          | How Template Method is used                              |
|--------------------|----------------------------------------------------------|
| **JdbcTemplate**   | getConnection → createStatement → executeQuery → processResults → closeConnection. You only write the query + result processing |
| **HttpServlet**    | service() is the template → calls doGet()/doPost() etc. You override the specific HTTP method handlers |
| **JUnit**          | setUp() → test() → tearDown(). Framework controls the flow, you fill in the test |
| **Spring Data**    | AbstractRoutingDataSource — template for DB routing, you override determineCurrentLookupKey() |
| **Collections.sort** | Algorithm is fixed (merge sort), you provide the Comparator |

### Template Method vs Strategy

| Aspect           | Template Method                | Strategy                       |
|------------------|--------------------------------|--------------------------------|
| How it varies    | Subclass overrides steps       | Pass different strategy object |
| Relationship     | Inheritance (IS-A)             | Composition (HAS-A)           |
| When decided     | Compile-time (class hierarchy) | Runtime (swap strategies)      |
| Granularity      | Vary a few steps of algorithm  | Vary the entire algorithm      |

---

## Pattern 3: Memento

### The Concept

**Indian analogy:** Playing a video game (like GTA Vice City back in the day).
You save your game before a difficult mission. If you die, you **restore from
the checkpoint**. The save file captures EVERYTHING — health, weapons, position,
money. You don't need to know what's inside the save file to use it.

Three roles:
1. **Originator** — the object whose state we want to save (the Game)
2. **Memento** — the save file (snapshot of state)
3. **Caretaker** — manages the save files (the save slot list)

### Java Code

```java
import java.util.ArrayList;
import java.util.List;

// Step 1: Memento — the "save file"
// Notice: no setters! Memento is immutable. Nobody can tamper with the save.
class EditorMemento {
    private final String content;
    private final int cursorPosition;
    private final String fontName;

    public EditorMemento(String content, int cursorPosition, String fontName) {
        this.content = content;
        this.cursorPosition = cursorPosition;
        this.fontName = fontName;
    }

    // Only the Originator should access these, but in Java we trust
    // the pattern discipline (in production, use package-private access)
    String getContent() { return content; }
    int getCursorPosition() { return cursorPosition; }
    String getFontName() { return fontName; }
}

// Step 2: Originator — the object whose state we save/restore
class TextEditor {
    private String content;
    private int cursorPosition;
    private String fontName;

    public TextEditor() {
        this.content = "";
        this.cursorPosition = 0;
        this.fontName = "Arial";
    }

    // Normal editor operations
    public void type(String text) {
        content = content.substring(0, cursorPosition) + text
                  + content.substring(cursorPosition);
        cursorPosition += text.length();
    }

    public void setFont(String font) {
        this.fontName = font;
    }

    public void moveCursor(int position) {
        this.cursorPosition = Math.min(position, content.length());
    }

    // SAVE — create a memento (snapshot)
    public EditorMemento save() {
        return new EditorMemento(content, cursorPosition, fontName);
    }

    // RESTORE — apply a memento
    public void restore(EditorMemento memento) {
        this.content = memento.getContent();
        this.cursorPosition = memento.getCursorPosition();
        this.fontName = memento.getFontName();
    }

    @Override
    public String toString() {
        return "Content: \"" + content + "\" | Cursor: "
               + cursorPosition + " | Font: " + fontName;
    }
}

// Step 3: Caretaker — manages the undo history
class UndoManager {
    private List<EditorMemento> history = new ArrayList<>();
    private int currentIndex = -1;

    public void saveState(EditorMemento memento) {
        // When we save a new state, discard any "future" states
        // (if we undid some things and then typed new text)
        while (history.size() > currentIndex + 1) {
            history.remove(history.size() - 1);
        }
        history.add(memento);
        currentIndex++;
    }

    public EditorMemento undo() {
        if (currentIndex <= 0) {
            System.out.println("Nothing to undo!");
            return null;
        }
        currentIndex--;
        return history.get(currentIndex);
    }

    public EditorMemento redo() {
        if (currentIndex >= history.size() - 1) {
            System.out.println("Nothing to redo!");
            return null;
        }
        currentIndex++;
        return history.get(currentIndex);
    }
}

// Step 4: Demo
public class MementoDemo {
    public static void main(String[] args) {
        TextEditor editor = new TextEditor();
        UndoManager undoManager = new UndoManager();

        // Save initial state
        undoManager.saveState(editor.save());

        // Type some text
        editor.type("Hello ");
        System.out.println("After typing 'Hello ': " + editor);
        undoManager.saveState(editor.save());

        editor.type("World!");
        System.out.println("After typing 'World!': " + editor);
        undoManager.saveState(editor.save());

        editor.setFont("Comic Sans");
        editor.type(" :)");
        System.out.println("After font change + smiley: " + editor);
        undoManager.saveState(editor.save());

        // UNDO!
        System.out.println("\n--- Pressing Ctrl+Z (Undo) ---");
        EditorMemento prev = undoManager.undo();
        if (prev != null) editor.restore(prev);
        System.out.println("After undo: " + editor);

        System.out.println("\n--- Pressing Ctrl+Z again ---");
        prev = undoManager.undo();
        if (prev != null) editor.restore(prev);
        System.out.println("After undo: " + editor);

        // REDO!
        System.out.println("\n--- Pressing Ctrl+Y (Redo) ---");
        EditorMemento next = undoManager.redo();
        if (next != null) editor.restore(next);
        System.out.println("After redo: " + editor);
    }
}
```

**Output:**
```
After typing 'Hello ': Content: "Hello " | Cursor: 6 | Font: Arial
After typing 'World!': Content: "Hello World!" | Cursor: 12 | Font: Arial
After font change + smiley: Content: "Hello World! :)" | Cursor: 15 | Font: Comic Sans

--- Pressing Ctrl+Z (Undo) ---
After undo: Content: "Hello World!" | Cursor: 12 | Font: Arial

--- Pressing Ctrl+Z again ---
After undo: Content: "Hello " | Cursor: 6 | Font: Arial

--- Pressing Ctrl+Y (Redo) ---
After redo: Content: "Hello World!" | Cursor: 12 | Font: Arial
```

### Command vs Memento — The Critical Difference

This is a common interview question!

| Aspect            | Command Pattern              | Memento Pattern               |
|-------------------|------------------------------|-------------------------------|
| What it stores    | **ACTIONS** (what was done)  | **STATE** (snapshot of data)  |
| Undo approach     | Execute reverse action       | Restore previous snapshot     |
| Memory usage      | Lower (stores operations)    | Higher (stores full states)   |
| Complexity        | Need reverse for each command| Simple save/restore           |
| Example           | "Delete chars 5-10"          | "Full text was: Hello World"  |
| Best for          | Simple reversible operations | Complex state with many fields|

**Indian analogy:**
- **Command** = You remember the steps you took: "I added salt, then sugar."
  To undo, you think: "I need to remove the sugar, then remove the salt."
- **Memento** = You took a photo of the dish before each change. To undo,
  you just recreate the dish from the photo. No need to remember what you did.

---

## Pattern 4: Prototype

### The Concept

**Indian analogy:** You have a perfectly formatted resume. Instead of creating
a new resume from scratch for each job application, you **photocopy** your
existing resume and make small changes (company name, cover letter). The
photocopy is the clone.

Prototype says: **clone an existing object instead of creating a new one
from scratch**.

### When to Use

- Object creation is expensive (DB calls, API calls, heavy computation)
- You need many objects similar to an existing one
- You want to avoid complex factory class hierarchies

### Java Code

```java
import java.util.ArrayList;
import java.util.List;

// Step 1: Prototype interface (Java has Cloneable built in)
interface GameUnitPrototype extends Cloneable {
    GameUnitPrototype clone();
    void showDetails();
}

// Step 2: Concrete prototype
class Soldier implements GameUnitPrototype {
    private String name;
    private int health;
    private String weapon;
    private List<String> skills;  // reference type — important for deep copy!

    public Soldier(String name, int health, String weapon, List<String> skills) {
        this.name = name;
        this.health = health;
        this.weapon = weapon;
        this.skills = skills;
    }

    // SHALLOW COPY — skills list is shared!
    public Soldier shallowClone() {
        return new Soldier(this.name, this.health, this.weapon, this.skills);
    }

    // DEEP COPY — skills list is duplicated
    @Override
    public Soldier clone() {
        List<String> clonedSkills = new ArrayList<>(this.skills);
        return new Soldier(this.name, this.health, this.weapon, clonedSkills);
    }

    // Setters for customization after cloning
    public void setName(String name) { this.name = name; }
    public void setWeapon(String weapon) { this.weapon = weapon; }
    public void addSkill(String skill) { this.skills.add(skill); }

    @Override
    public void showDetails() {
        System.out.println("Soldier: " + name
            + " | HP: " + health
            + " | Weapon: " + weapon
            + " | Skills: " + skills);
    }
}

// Step 3: Demo showing SHALLOW vs DEEP copy
public class PrototypeDemo {
    public static void main(String[] args) {

        // Create the original soldier (expensive setup)
        List<String> baseSkills = new ArrayList<>();
        baseSkills.add("march");
        baseSkills.add("shoot");
        Soldier original = new Soldier("Template", 100, "AK-47", baseSkills);

        System.out.println("=== SHALLOW COPY PROBLEM ===\n");

        Soldier shallowCopy = original.shallowClone();
        shallowCopy.setName("Soldier-Shallow");
        shallowCopy.setWeapon("Sniper");
        shallowCopy.addSkill("snipe");  // This ALSO modifies original!

        System.out.println("Original:");
        original.showDetails();
        System.out.println("Shallow Copy:");
        shallowCopy.showDetails();
        System.out.println("PROBLEM: Original now has 'snipe' skill too!\n");

        System.out.println("=== DEEP COPY SOLUTION ===\n");

        // Reset original
        List<String> freshSkills = new ArrayList<>();
        freshSkills.add("march");
        freshSkills.add("shoot");
        Soldier original2 = new Soldier("Template", 100, "AK-47", freshSkills);

        Soldier deepCopy = original2.clone();  // deep copy
        deepCopy.setName("Soldier-Deep");
        deepCopy.setWeapon("Shotgun");
        deepCopy.addSkill("breach");  // Only affects the clone

        System.out.println("Original:");
        original2.showDetails();
        System.out.println("Deep Copy:");
        deepCopy.showDetails();
        System.out.println("Original is SAFE — no 'breach' skill.");
    }
}
```

**Output:**
```
=== SHALLOW COPY PROBLEM ===

Original:
Soldier: Template | HP: 100 | Weapon: AK-47 | Skills: [march, shoot, snipe]
Shallow Copy:
Soldier: Soldier-Shallow | HP: 100 | Weapon: Sniper | Skills: [march, shoot, snipe]
PROBLEM: Original now has 'snipe' skill too!

=== DEEP COPY SOLUTION ===

Original:
Soldier: Template | HP: 100 | Weapon: AK-47 | Skills: [march, shoot]
Deep Copy:
Soldier: Soldier-Deep | HP: 100 | Weapon: Shotgun | Skills: [march, shoot, breach]
Original is SAFE — no 'breach' skill.
```

### Shallow vs Deep Copy — The Full Picture

```
SHALLOW COPY:
  Original            Copy
  +---------+         +---------+
  | name: A |         | name: A |    <-- primitives are duplicated (safe)
  | skills ----+      | skills ----+
  +---------+  |      +---------+  |
               |                   |
               +--> [march, shoot] <--+   <-- SAME list object! Danger!

DEEP COPY:
  Original            Copy
  +---------+         +---------+
  | name: A |         | name: A |    <-- primitives duplicated
  | skills ----> [march, shoot]
  +---------+         | skills ----> [march, shoot]   <-- SEPARATE copy
                      +---------+
```

| Aspect              | Shallow Copy                  | Deep Copy                    |
|---------------------|-------------------------------|------------------------------|
| Primitives (int, etc.) | Copied (safe)              | Copied (safe)               |
| References (List, etc.)| Shared (dangerous!)        | Duplicated (safe)            |
| Speed               | Fast                          | Slower                       |
| Memory              | Less                          | More                         |
| When to use         | Only primitive fields          | When object has reference fields |

---

## Pattern 5: Flyweight

### The Concept

**Indian analogy:** A printing press. When printing a newspaper, does the
press create a brand new letter 'A' for every 'A' that appears? No!
It reuses the same metal block for 'A' everywhere. The letter 'A' looks
the same everywhere — only its **position on the page** changes.

Flyweight says: **share the common stuff, only store the unique stuff per instance**.

Two types of state:
- **Intrinsic** (shared) — doesn't change across instances → store ONCE
- **Extrinsic** (unique) — different per instance → store per instance

### Java Code: Word Processor Character Rendering

```java
import java.util.HashMap;
import java.util.Map;

// Step 1: Flyweight — the shared part
// This is the "metal block" — character appearance is shared
class CharacterStyle {
    private final char character;
    private final String fontFamily;
    private final int fontSize;
    private final String color;

    // These are INTRINSIC properties — shared across all 'A's with same style
    public CharacterStyle(char character, String fontFamily, int fontSize, String color) {
        this.character = character;
        this.fontFamily = fontFamily;
        this.fontSize = fontSize;
        this.color = color;
        System.out.println("  [Creating new style object for '" + character
            + "' in " + fontFamily + " " + fontSize + "pt " + color + "]");
    }

    // Render at a specific position (extrinsic data passed in)
    public void render(int row, int col) {
        System.out.println("Rendering '" + character + "' (" + fontFamily
            + ", " + fontSize + "pt, " + color + ") at position ("
            + row + ", " + col + ")");
    }
}

// Step 2: Flyweight Factory — manages shared instances
class CharacterStyleFactory {
    private static Map<String, CharacterStyle> styleCache = new HashMap<>();

    public static CharacterStyle getStyle(char character, String font,
                                           int size, String color) {
        // Create a key combining all intrinsic properties
        String key = character + "-" + font + "-" + size + "-" + color;

        if (!styleCache.containsKey(key)) {
            styleCache.put(key, new CharacterStyle(character, font, size, color));
        }
        return styleCache.get(key);
    }

    public static int getCacheSize() {
        return styleCache.size();
    }
}

// Step 3: The document that uses flyweights
class Document {
    // Each position stores a reference to a SHARED style + its UNIQUE position
    private CharacterStyle[][] characters;
    private int rows, cols;

    public Document(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.characters = new CharacterStyle[rows][cols];
    }

    public void addCharacter(int row, int col, char c, String font,
                              int size, String color) {
        // Get shared style from factory (might already exist!)
        characters[row][col] = CharacterStyleFactory.getStyle(c, font, size, color);
    }

    public void render() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (characters[r][c] != null) {
                    characters[r][c].render(r, c);
                }
            }
        }
    }
}

// Step 4: Demo
public class FlyweightDemo {
    public static void main(String[] args) {
        Document doc = new Document(3, 10);

        System.out.println("Adding characters to document:\n");

        // "HELLO" in Arial 12pt Black
        doc.addCharacter(0, 0, 'H', "Arial", 12, "black");
        doc.addCharacter(0, 1, 'E', "Arial", 12, "black");
        doc.addCharacter(0, 2, 'L', "Arial", 12, "black");
        doc.addCharacter(0, 3, 'L', "Arial", 12, "black");  // reuses 'L' style!
        doc.addCharacter(0, 4, 'O', "Arial", 12, "black");

        // "HELLO" again on row 2 — ALL styles reused!
        System.out.println("\nAdding same text on another line:");
        doc.addCharacter(2, 0, 'H', "Arial", 12, "black");
        doc.addCharacter(2, 1, 'E', "Arial", 12, "black");
        doc.addCharacter(2, 2, 'L', "Arial", 12, "black");
        doc.addCharacter(2, 3, 'L', "Arial", 12, "black");
        doc.addCharacter(2, 4, 'O', "Arial", 12, "black");

        System.out.println("\nTotal characters added: 10");
        System.out.println("Style objects created: " + CharacterStyleFactory.getCacheSize());
        System.out.println("Memory saved: " + (10 - CharacterStyleFactory.getCacheSize())
            + " objects reused!\n");

        System.out.println("--- Rendering document ---\n");
        doc.render();
    }
}
```

**Output:**
```
Adding characters to document:

  [Creating new style object for 'H' in Arial 12pt black]
  [Creating new style object for 'E' in Arial 12pt black]
  [Creating new style object for 'L' in Arial 12pt black]
  [Creating new style object for 'O' in Arial 12pt black]

Adding same text on another line:

Total characters added: 10
Style objects created: 4
Memory saved: 6 objects reused!

--- Rendering document ---

Rendering 'H' (Arial, 12pt, black) at position (0, 0)
Rendering 'E' (Arial, 12pt, black) at position (0, 1)
Rendering 'L' (Arial, 12pt, black) at position (0, 2)
Rendering 'L' (Arial, 12pt, black) at position (0, 3)
Rendering 'O' (Arial, 12pt, black) at position (0, 4)
Rendering 'H' (Arial, 12pt, black) at position (2, 0)
Rendering 'E' (Arial, 12pt, black) at position (2, 1)
Rendering 'L' (Arial, 12pt, black) at position (2, 2)
Rendering 'L' (Arial, 12pt, black) at position (2, 3)
Rendering 'O' (Arial, 12pt, black) at position (2, 4)
```

### Flyweight in Java's Own Code — You Already Use It!

**1. String Pool (the most famous flyweight):**
```java
String a = "hello";       // goes to string pool
String b = "hello";       // reuses from pool — same object!
System.out.println(a == b);  // true! Same memory address

String c = new String("hello");  // forced new object
System.out.println(a == c);      // false — different objects
System.out.println(a.equals(c)); // true — same content
```

**2. Integer Cache (-128 to 127):**
```java
Integer x = 100;
Integer y = 100;
System.out.println(x == y);  // true! Cached flyweight

Integer p = 200;
Integer q = 200;
System.out.println(p == q);  // false! Outside cache range
```

Java caches Integer values from -128 to 127. These are flyweight objects —
created once, reused everywhere. That's why `== ` works for small integers
but fails for large ones. This is a classic interview trick question!

**3. Boolean.TRUE / Boolean.FALSE** — only two instances ever exist.

---

## Pattern 6: Visitor

### The Concept

**Indian analogy:** A tax inspector visits different businesses. At a
restaurant, he checks food license + GST. At a factory, he checks
pollution certificate + labor compliance. At a shop, he checks trade
license + billing records.

The **inspector** (Visitor) has different behavior for each **business type**
(Element). The businesses don't change — they just "accept" the inspector
and let him do his thing.

This is called **double dispatch**: the operation that runs depends on BOTH
the visitor type AND the element type.

### Java Code: Shopping Cart with Tax Calculation

```java
import java.util.ArrayList;
import java.util.List;

// Step 1: Visitor interface — one visit method per element type
interface ShoppingVisitor {
    double visit(Book book);
    double visit(Electronics electronics);
    double visit(Grocery grocery);
}

// Step 2: Element interface — accepts a visitor
interface CartItem {
    double accept(ShoppingVisitor visitor);
    String getName();
}

// Step 3: Concrete elements

class Book implements CartItem {
    private String name;
    private double price;
    private String isbn;

    public Book(String name, double price, String isbn) {
        this.name = name;
        this.price = price;
        this.isbn = isbn;
    }

    public double getPrice() { return price; }
    public String getIsbn() { return isbn; }
    public String getName() { return name; }

    @Override
    public double accept(ShoppingVisitor visitor) {
        return visitor.visit(this);  // double dispatch: calls visit(Book)
    }
}

class Electronics implements CartItem {
    private String name;
    private double price;
    private double weight;  // for shipping calculation

    public Electronics(String name, double price, double weight) {
        this.name = name;
        this.price = price;
        this.weight = weight;
    }

    public double getPrice() { return price; }
    public double getWeight() { return weight; }
    public String getName() { return name; }

    @Override
    public double accept(ShoppingVisitor visitor) {
        return visitor.visit(this);  // calls visit(Electronics)
    }
}

class Grocery implements CartItem {
    private String name;
    private double price;
    private boolean isEssential;

    public Grocery(String name, double price, boolean isEssential) {
        this.name = name;
        this.price = price;
        this.isEssential = isEssential;
    }

    public double getPrice() { return price; }
    public boolean isEssential() { return isEssential; }
    public String getName() { return name; }

    @Override
    public double accept(ShoppingVisitor visitor) {
        return visitor.visit(this);  // calls visit(Grocery)
    }
}

// Step 4: Concrete Visitors

// Tax Calculator — different tax rates per item type (like Indian GST slabs!)
class TaxCalculator implements ShoppingVisitor {
    @Override
    public double visit(Book book) {
        // Books have 0% GST in India (educational items)
        System.out.println("  " + book.getName() + ": Rs." + book.getPrice()
            + " (0% GST on books)");
        return 0;
    }

    @Override
    public double visit(Electronics electronics) {
        // Electronics: 18% GST
        double tax = electronics.getPrice() * 0.18;
        System.out.println("  " + electronics.getName() + ": Rs."
            + electronics.getPrice() + " -> GST 18% = Rs." + tax);
        return tax;
    }

    @Override
    public double visit(Grocery grocery) {
        if (grocery.isEssential()) {
            // Essential groceries: 0% GST
            System.out.println("  " + grocery.getName() + ": Rs."
                + grocery.getPrice() + " (0% GST on essentials)");
            return 0;
        } else {
            // Non-essential: 5% GST
            double tax = grocery.getPrice() * 0.05;
            System.out.println("  " + grocery.getName() + ": Rs."
                + grocery.getPrice() + " -> GST 5% = Rs." + tax);
            return tax;
        }
    }
}

// Shipping Cost Calculator — another visitor, same elements
class ShippingCalculator implements ShoppingVisitor {
    @Override
    public double visit(Book book) {
        // Flat Rs.30 shipping for books
        System.out.println("  " + book.getName() + ": Shipping Rs.30 (flat rate)");
        return 30;
    }

    @Override
    public double visit(Electronics electronics) {
        // Rs.50 per kg for electronics
        double shipping = electronics.getWeight() * 50;
        System.out.println("  " + electronics.getName() + ": Shipping Rs."
            + shipping + " (" + electronics.getWeight() + "kg x Rs.50)");
        return shipping;
    }

    @Override
    public double visit(Grocery grocery) {
        // Free delivery on groceries!
        System.out.println("  " + grocery.getName() + ": FREE delivery");
        return 0;
    }
}

// Step 5: Demo
public class VisitorDemo {
    public static void main(String[] args) {
        // Create shopping cart
        List<CartItem> cart = new ArrayList<>();
        cart.add(new Book("Head First Design Patterns", 599, "978-0596007126"));
        cart.add(new Electronics("Laptop", 65000, 2.5));
        cart.add(new Grocery("Rice (5kg)", 350, true));
        cart.add(new Grocery("Chocolate", 150, false));
        cart.add(new Electronics("Earphones", 1200, 0.1));

        // Calculate tax using Tax Visitor
        System.out.println("=== TAX CALCULATION ===\n");
        ShoppingVisitor taxCalc = new TaxCalculator();
        double totalTax = 0;
        for (CartItem item : cart) {
            totalTax += item.accept(taxCalc);
        }
        System.out.println("\nTotal GST: Rs." + totalTax);

        // Calculate shipping using Shipping Visitor
        System.out.println("\n=== SHIPPING CALCULATION ===\n");
        ShoppingVisitor shippingCalc = new ShippingCalculator();
        double totalShipping = 0;
        for (CartItem item : cart) {
            totalShipping += item.accept(shippingCalc);
        }
        System.out.println("\nTotal Shipping: Rs." + totalShipping);

        // Grand total
        double subtotal = 599 + 65000 + 350 + 150 + 1200;
        System.out.println("\n=== ORDER SUMMARY ===");
        System.out.println("Subtotal:  Rs." + subtotal);
        System.out.println("GST:       Rs." + totalTax);
        System.out.println("Shipping:  Rs." + totalShipping);
        System.out.println("TOTAL:     Rs." + (subtotal + totalTax + totalShipping));
    }
}
```

### How Double Dispatch Works

```
cart.get(0).accept(taxCalc)
    |                 |
    Book             TaxCalculator

Step 1: Java calls Book.accept(visitor)          → dispatch on Element type
Step 2: Inside accept(), calls visitor.visit(this) → dispatch on Visitor type
                                                      (this = Book, so visit(Book) is called)

Two dispatches → "Double Dispatch"
```

Why not just use `instanceof`? Because with Visitor:
- Adding a new operation = add a new Visitor class (no existing code changes)
- The compiler forces you to handle every element type (one visit method per type)
- Each visitor class is cohesive — all tax logic in TaxCalculator, all
  shipping logic in ShippingCalculator

### When NOT to Use Visitor

- If element types change frequently (adding a new element type means updating
  EVERY visitor — painful!)
- If you only have 1-2 operations — the overhead isn't worth it
- Use Visitor when you have stable element types but frequently add new operations

---

# PART 3: COMPLETE PATTERN SELECTION TABLE

> "I have this problem. Which pattern do I use?"

This table covers ALL 16 patterns we've studied (10 from weeks 1-4 + 6 from
this week). Use this as your cheat sheet during design rounds.

---

## Creational Patterns — "How to create objects"

| Problem / Need | Pattern | One-Line Description |
|----------------|---------|---------------------|
| Only one instance should exist (config, DB pool, logger) | **Singleton** | One instance, global access point |
| Create objects without specifying exact class | **Factory Method** | Subclass decides which class to instantiate |
| Create families of related objects (UI themes, DB drivers) | **Abstract Factory** | Factory of factories |
| Construct complex objects step-by-step (query builders, meal combos) | **Builder** | Step-by-step construction, same process → different representations |
| Clone existing object instead of creating from scratch | **Prototype** | Copy an existing object, customize the copy |

---

## Structural Patterns — "How to compose objects"

| Problem / Need | Pattern | One-Line Description |
|----------------|---------|---------------------|
| Make incompatible interfaces work together (old API + new code) | **Adapter** | Converter between two incompatible interfaces |
| Add responsibilities dynamically without subclassing (toppings on pizza) | **Decorator** | Wrapper that adds behavior |
| Too many objects eating memory, most share common state | **Flyweight** | Share common data, store only unique data per instance |

---

## Behavioral Patterns — "How objects communicate"

| Problem / Need | Pattern | One-Line Description |
|----------------|---------|---------------------|
| Multiple algorithms, swap at runtime (payment methods, sorting) | **Strategy** | Encapsulate algorithms, make them interchangeable |
| Notify multiple objects when state changes (event systems, UI updates) | **Observer** | Publisher-subscriber, one-to-many dependency |
| One object's behavior depends on its state (order lifecycle, ATM) | **State** | Object changes behavior when internal state changes |
| Execute, undo, queue, or log operations (editor undo, order processing) | **Command** | Encapsulate a request as an object |
| Request passes through a chain of handlers (approval flow, filters) | **Chain of Responsibility** | Pass request along a chain until someone handles it |
| Algorithm skeleton is fixed, but some steps vary (tea/coffee, JdbcTemplate) | **Template Method** | Parent defines steps, children fill in specifics |
| Save and restore object state (undo/redo, game save, snapshots) | **Memento** | Capture and externalize internal state |
| Add operations to objects without modifying them (tax calc, serialization) | **Visitor** | Separate algorithm from object structure |

---

## Quick Decision Flowchart

```
"I need to..."

CREATE objects?
  ├── Only one instance? ──────────────────────→ Singleton
  ├── Choose type at runtime? ─────────────────→ Factory Method
  ├── Create families of related types? ───────→ Abstract Factory
  ├── Build complex object step by step? ──────→ Builder
  └── Copy existing object? ───────────────────→ Prototype

STRUCTURE objects?
  ├── Make old + new work together? ───────────→ Adapter
  ├── Add features dynamically? ───────────────→ Decorator
  └── Save memory with shared state? ──────────→ Flyweight

MANAGE BEHAVIOR?
  ├── Swap algorithm at runtime? ──────────────→ Strategy
  ├── Notify many when one changes? ───────────→ Observer
  ├── Behavior depends on state? ──────────────→ State
  ├── Encapsulate operation as object? ────────→ Command
  ├── Pass through chain of handlers? ─────────→ Chain of Responsibility
  ├── Same skeleton, different details? ───────→ Template Method
  ├── Save/restore snapshots? ─────────────────→ Memento
  └── New operations without modifying classes? → Visitor
```

---

## Patterns That Are Often Confused

### Strategy vs Template Method

| Aspect             | Strategy                          | Template Method                    |
|--------------------|-----------------------------------|------------------------------------|
| Mechanism          | Composition (HAS-A strategy)      | Inheritance (IS-A subclass)        |
| What varies        | Entire algorithm                  | Specific steps of an algorithm     |
| When decided       | Runtime (swap strategies)         | Compile-time (class hierarchy)     |
| Flexibility        | Can change on the fly             | Fixed once object is created       |
| Indian analogy     | Choosing Ola vs Uber vs Auto      | Making chai vs coffee (same process)|

### Command vs Memento

| Aspect             | Command                           | Memento                            |
|--------------------|-----------------------------------|------------------------------------|
| Stores             | Actions (operations performed)    | State (snapshots of data)          |
| Undo approach      | Execute reverse action            | Restore previous snapshot          |
| Memory             | Lower (just operations)           | Higher (full state copies)         |
| Indian analogy     | Remember recipe steps to reverse  | Take photo of dish before changes  |

### Observer vs Chain of Responsibility

| Aspect             | Observer                          | Chain of Responsibility            |
|--------------------|-----------------------------------|------------------------------------|
| Direction          | One-to-MANY (broadcast)           | One-to-ONE (pass until handled)    |
| Who handles        | ALL observers notified            | FIRST capable handler              |
| Indian analogy     | WhatsApp group (everyone gets msg)| Government complaint (escalation)  |

### State vs Strategy

| Aspect             | State                             | Strategy                           |
|--------------------|-----------------------------------|------------------------------------|
| Who initiates      | Object changes its own behavior   | Client sets the behavior           |
| Transitions        | States know about each other      | Strategies are independent         |
| Indian analogy     | Traffic light (auto-cycles)       | GPS navigation (you pick the route)|

### Adapter vs Decorator

| Aspect             | Adapter                           | Decorator                          |
|--------------------|-----------------------------------|------------------------------------|
| Purpose            | Make incompatible compatible      | Add new behavior                   |
| Interface          | Changes the interface             | Keeps same interface               |
| Indian analogy     | Plug converter (India to US)      | Phone case (adds grip, same phone) |

---

## How Patterns Combine in Real Systems

Real systems rarely use just one pattern. Here's how they combine:

### E-Commerce Order System
```
Order creation     → Builder (complex order with many optional fields)
Payment processing → Strategy (UPI / Card / COD — swappable)
Order lifecycle    → State (Placed → Confirmed → Shipped → Delivered)
Notifications      → Observer (email, SMS, push — all notified on state change)
Price calculation  → Visitor (different tax rules for books, electronics, groceries)
Discount pipeline  → Chain of Responsibility (coupon → loyalty → seasonal → final price)
```

### Document Editor
```
Undo/Redo          → Memento (save/restore document state)
Toolbar actions    → Command (cut, copy, paste as command objects)
File export        → Strategy (export as PDF / DOCX / HTML)
Character storage  → Flyweight (share font/style, only position is unique)
Plugin system      → Template Method (plugin lifecycle: init → run → cleanup)
```

### Game Engine
```
Character creation → Prototype (clone base character, customize)
Game objects       → Flyweight (shared textures, models)
AI behavior        → State (Idle → Patrol → Chase → Attack)
Damage pipeline    → Chain of Responsibility (armor → shield → health)
Save system        → Memento (checkpoint saves)
Event system       → Observer (damage events, achievement triggers)
```

---

## Final Tips for Interviews

1. **Don't force patterns** — if a simple if-else solves it, use that. Patterns
   are for recurring problems that benefit from structure.

2. **Name the pattern explicitly** — "I'll use the Strategy pattern here
   because we need to swap payment methods at runtime."

3. **Explain the trade-off** — "I'm choosing Strategy over Template Method
   because we need runtime flexibility, even though it means an extra interface."

4. **Draw the class diagram** — even a rough box-and-arrow sketch shows the
   interviewer you can think structurally.

5. **Know the Java standard library examples** — interviewers love asking
   "Where have you seen this pattern in Java?"

| Pattern                  | Java Standard Library Example                    |
|--------------------------|--------------------------------------------------|
| Singleton                | `Runtime.getRuntime()`                           |
| Factory Method           | `Calendar.getInstance()`                         |
| Abstract Factory         | `DocumentBuilderFactory.newInstance()`            |
| Builder                  | `StringBuilder`, `Stream.Builder`                |
| Prototype                | `Object.clone()`                                 |
| Adapter                  | `Arrays.asList()`, `InputStreamReader`           |
| Decorator                | `BufferedReader(new FileReader(...))`             |
| Flyweight                | `String pool`, `Integer.valueOf()` cache         |
| Strategy                 | `Comparator` passed to `Collections.sort()`      |
| Observer                 | `java.util.Observer` (deprecated but classic)    |
| State                    | `Thread.State` (NEW, RUNNABLE, BLOCKED, etc.)    |
| Command                  | `Runnable` (encapsulates action as object)       |
| Chain of Responsibility  | `Servlet Filters`, `java.util.logging.Handler`   |
| Template Method          | `HttpServlet.service()` → `doGet()`/`doPost()`  |
| Memento                  | `java.io.Serializable` (save/restore state)      |
| Visitor                  | `FileVisitor` (Java NIO), `AnnotationProcessor`  |

---

## Week 5 Checklist

- [ ] Can draw a class diagram with all 5 relationship types
- [ ] Can explain aggregation vs composition with examples
- [ ] Can draw a sequence diagram for any multi-service flow
- [ ] Chain of Responsibility: complaint escalation / leave approval
- [ ] Template Method: tea vs coffee / JdbcTemplate
- [ ] Memento: game save / text editor undo — and how it differs from Command
- [ ] Prototype: shallow vs deep copy — and WHY it matters
- [ ] Flyweight: String pool, Integer cache, character rendering
- [ ] Visitor: double dispatch, tax calculation, when NOT to use
- [ ] Can pick the right pattern for any given problem from the selection table
- [ ] Know at least one Java stdlib example for each pattern
