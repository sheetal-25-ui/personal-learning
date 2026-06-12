# LLD Week 2: Creational and Structural Design Patterns

## What are Design Patterns?

Imagine you are cooking dal for the first time. You could experiment randomly — add salt, then turmeric, then realize you forgot to boil the lentils first. OR, you could follow a recipe that thousands of cooks have already perfected. Design patterns are those recipes, but for software.

**Formal definition:** Design patterns are reusable solutions to commonly occurring problems in software design. They are NOT code you copy-paste. They are TEMPLATES — proven approaches that you adapt to your specific situation.

**Why should you care for interviews?**
Microsoft, Amazon, and Google interviewers do not ask "explain the Singleton pattern." They give you a problem (design a notification system, design a cache) and expect you to NATURALLY use the right patterns. If you know patterns well, you will structure your solution cleanly. If you do not, you will write messy code that is hard to extend.

**The three categories:**
1. **Creational** — HOW objects are created (this week: Singleton, Factory, Builder)
2. **Structural** — HOW objects are composed/connected (this week: Adapter, Decorator, Facade)
3. **Behavioral** — HOW objects communicate (next week: Strategy, Observer, Command, State)

---

## Day 1: Creational Patterns (Saturday)

Creational patterns deal with object creation. The core question they answer is: "How do I create objects in a way that is flexible, controlled, and does not tightly couple my code to specific classes?"

---

### Pattern 1: Singleton — "Only ONE instance, ever, no matter what"

#### What is it?

The Singleton pattern ensures that a class has **exactly one instance** across your entire application, and provides a single global point of access to it.

Think of it this way: India has ONE Reserve Bank of India (RBI). You cannot create a second RBI. Every bank, every financial institution, when they need to interact with the central bank, they all talk to the SAME RBI. That is a Singleton.

#### Real-life analogy

Think about the **Aadhaar system (UIDAI)**. There is exactly ONE Aadhaar database in India. When Flipkart verifies your identity, they connect to the same UIDAI system. When PayTM does KYC, they connect to the same UIDAI system. You cannot create a second UIDAI — it would cause chaos (duplicate Aadhaar numbers, conflicting records). The system is designed so that there is one and only one instance.

In software, this is exactly what happens with:
- **Database connections** — You want ONE connection pool, not a new connection every time someone makes a query (that would crash your database)
- **Logger** — You want ONE logger writing to the log file, not 50 different loggers fighting over the same file
- **Configuration manager** — Your app reads config once, and everyone shares the same config object

#### What problem does it solve? (The ugly code WITHOUT the pattern)

```java
// WITHOUT Singleton — The PROBLEM
// Imagine every part of your Zomato backend creates its own database connection

class DatabaseConnection {
    private String connection;

    public DatabaseConnection() {
        // This is EXPENSIVE — takes 2-3 seconds, opens a network socket
        System.out.println("Opening new database connection...");
        this.connection = "Connected to MongoDB";
    }

    public String getConnection() {
        return connection;
    }
}

// In your order service:
DatabaseConnection db1 = new DatabaseConnection();  // Opens connection #1

// In your restaurant service:
DatabaseConnection db2 = new DatabaseConnection();  // Opens connection #2 (WASTEFUL!)

// In your delivery service:
DatabaseConnection db3 = new DatabaseConnection();  // Opens connection #3 (EVEN MORE WASTEFUL!)

// In your payment service:
DatabaseConnection db4 = new DatabaseConnection();  // Opens connection #4

// PROBLEM: You now have 4 separate connections!
// MongoDB has a default limit of ~100 connections.
// If every service creates its own, you will hit the limit fast.
// Also, db1 and db2 are DIFFERENT objects — changes to one do not reflect in the other.

System.out.println(db1 == db2);  // false — they are different objects!
```

**What goes wrong:**
1. Each connection uses memory and network resources
2. You can exhaust the database connection limit
3. No coordination — if one part of the code changes a setting on "its" connection, other parts do not see it
4. In a real app like Zomato handling 10,000 orders/minute, creating a new connection per request would crash everything

#### The pattern — Clean code WITH Singleton

```java
class DatabaseConnection {
    // Step 1: A class (static) variable to hold the ONE instance.
    // This variable belongs to the CLASS, not to any specific object.
    // Initially, no instance exists, so it is null.
    private static DatabaseConnection instance = null;

    private String connection;

    // Step 2: Make the constructor PRIVATE so no one outside can call "new".
    // This is how we prevent external code from creating multiple instances.
    private DatabaseConnection() {
        // Do the expensive setup ONCE
        System.out.println("Opening database connection... (this only happens ONCE)");
        this.connection = "Connected to MongoDB";
    }

    // Step 3: Provide a static method to get the single instance.
    // This is the ONLY way to get a DatabaseConnection object.
    public static DatabaseConnection getInstance() {
        // Step 4: Check if an instance already exists
        if (instance == null) {
            // First time — no instance exists yet.
            // Create the object.
            instance = new DatabaseConnection();
        }
        // Step 5: Whether we just created it or it already existed,
        // return the SAME instance every time
        return instance;
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }
}


// Let us test it:
DatabaseConnection db1 = DatabaseConnection.getInstance();  // Prints: "Opening database connection..."
DatabaseConnection db2 = DatabaseConnection.getInstance();  // Prints NOTHING — reuses existing instance
DatabaseConnection db3 = DatabaseConnection.getInstance();  // Prints NOTHING — same instance again

// All three variables point to the EXACT SAME object in memory
System.out.println(db1 == db2);  // true
System.out.println(db2 == db3);  // true

// Any change through one variable is visible through all others,
// because they all reference the same object:
db1.setConnection("Connected to PostgreSQL");
System.out.println(db3.getConnection());  // "Connected to PostgreSQL" — same object!
```

**Line-by-line walkthrough:**

| Line | What it does | Why |
|------|-------------|-----|
| `private static DatabaseConnection instance = null` | Class-level variable, shared by ALL instances | This is our "storage" for the single instance |
| `private DatabaseConnection()` | Private constructor — prevents external instantiation | No code outside this class can call `new DatabaseConnection()` |
| `public static DatabaseConnection getInstance()` | The only way to get an instance | Controls whether a new object is created at all |
| `if (instance == null)` | Check: does an instance already exist? | If yes, skip creation entirely |
| `instance = new DatabaseConnection()` | Actually create the object (only on first call) | We create via the private constructor |
| `return instance` | Always return the same instance | This is the key — every call to `getInstance()` returns the same object |

#### When to use Singleton

- Database connection pool (one pool shared across the application)
- Logger (one logger writing to the same file/service)
- Configuration manager (read config once, share everywhere)
- Cache manager (one shared cache)
- Thread pool (one pool of worker threads)

**The trigger question:** "Should there be exactly one of this thing in my entire application?" If yes, consider Singleton.

#### When NOT to use Singleton (Over-engineering warning)

- Do NOT use Singleton for regular business objects. A `User` class should NOT be a Singleton — you have millions of users.
- Do NOT use it just because you only NEED one instance right now. If the reason is convenience, not a hard requirement, pass the object as a parameter instead.
- Singletons make unit testing harder because you cannot easily replace them with mocks. Use them only when truly necessary.
- In multi-threaded applications, the basic Singleton shown above is NOT thread-safe. Two threads could both see `instance == null` at the same time and create two instances. For production code, use `synchronized` or the Bill Pugh Singleton (static inner class holder pattern).

---

### Pattern 2: Factory — "Create objects without specifying the exact class"

#### What is it?

The Factory pattern provides a single method that creates and returns objects, where the caller does not need to know (or care about) which specific class is being instantiated. You ask for a "notification," and the factory figures out whether to give you an Email, SMS, or WhatsApp notification based on some input.

Think of it as ordering food at a restaurant. You say "I want a dosa." You do NOT go to the kitchen, pick the tava, pour the batter, and make it yourself. The kitchen (factory) handles all the creation logic. You just specify WHAT you want, and you receive the finished product.

#### Real-life analogy

Think about how **Zomato sends notifications**:
- When your order is placed: You get an **email** confirmation
- When your delivery partner is nearby: You get a **push notification**
- When your order is delivered: You get an **SMS**
- When there is a payment issue: You get a **WhatsApp** message

The Zomato backend does NOT have separate code for each scenario saying "create an EmailNotification here" and "create an SMSNotification there." Instead, it has a NotificationFactory that takes the channel type as input and returns the right notification object. The code that USES the notification does not know or care which type it is — it just calls `.send()`.

#### What problem does it solve? (The ugly code WITHOUT the pattern)

```java
// WITHOUT Factory — The PROBLEM
// Every place in your code that needs to send a notification has to know
// about ALL notification types and their creation details

public void processOrder(Order order) {
    // Decide which notification to send based on user preference
    String userPref = order.getUser().getNotificationPreference();

    Notification notif;

    if (userPref.equals("email")) {
        // Must know how to create email notifications
        notif = new EmailNotification(
            "smtp.gmail.com",
            587,
            "noreply@zomato.com"
        );
    } else if (userPref.equals("sms")) {
        // Must know how to create SMS notifications
        notif = new SMSNotification(
            "twilio_key_123",
            "+91XXXXXXXXXX"
        );
    } else if (userPref.equals("whatsapp")) {
        // Must know how to create WhatsApp notifications
        notif = new WhatsAppNotification(
            "zomato_wa_123",
            "order_confirmation"
        );
    } else if (userPref.equals("push")) {
        notif = new PushNotification(
            "firebase_key_456"
        );
    } else {
        throw new IllegalArgumentException("Unknown preference: " + userPref);
    }

    notif.send("Order " + order.getId() + " confirmed!");
}

public void processRefund(Refund refund) {
    String userPref = refund.getUser().getNotificationPreference();

    // SAME ugly if-elif-else block AGAIN!
    Notification notif;
    if (userPref.equals("email")) {
        notif = new EmailNotification(
            "smtp.gmail.com",
            587,
            "noreply@zomato.com"
        );
    } else if (userPref.equals("sms")) {
        notif = new SMSNotification(
            "twilio_key_123",
            "+91XXXXXXXXXX"
        );
    }
    // ... same block repeated EVERYWHERE
}
```

**What goes wrong:**
1. **Code duplication** — The if-elif block is copied everywhere notifications are sent
2. **Tight coupling** — Every function knows the internal details of every notification type (API keys, server addresses)
3. **Hard to add new types** — If you add Telegram notifications, you must find and update EVERY place that creates notifications
4. **Hard to test** — You cannot easily test `processOrder` without real SMTP servers and Twilio accounts

#### The pattern — Clean code WITH Factory

```java
// Step 1: Define a common interface
// All notification types must have a send() method.
// This is a CONTRACT — any notification, regardless of type,
// can be used by calling .send(message).
abstract class Notification {
    public abstract void send(String message);
}

// Step 2: Create concrete implementations
// Each class handles its own creation details internally.
// The outside world does not need to know about SMTP servers or API keys.

class EmailNotification extends Notification {
    private String smtpServer;
    private int port;

    public EmailNotification() {
        // Internal details — hidden from the rest of the code
        this.smtpServer = "smtp.gmail.com";
        this.port = 587;
    }

    @Override
    public void send(String message) {
        System.out.println("[EMAIL] Sending via " + smtpServer + ": " + message);
    }
}

class SMSNotification extends Notification {
    private String apiKey;

    public SMSNotification() {
        this.apiKey = "twilio_key_123";
    }

    @Override
    public void send(String message) {
        System.out.println("[SMS] Sending via Twilio: " + message);
    }
}

class WhatsAppNotification extends Notification {
    private String businessId;

    public WhatsAppNotification() {
        this.businessId = "zomato_wa_123";
    }

    @Override
    public void send(String message) {
        System.out.println("[WhatsApp] Sending via Business API: " + message);
    }
}

class PushNotification extends Notification {
    private String firebaseKey;

    public PushNotification() {
        this.firebaseKey = "firebase_key_456";
    }

    @Override
    public void send(String message) {
        System.out.println("[Push] Sending via Firebase: " + message);
    }
}

// Step 3: The Factory — ONE place that knows how to create each type
class NotificationFactory {
    public static Notification create(String channel) {
        // Given a channel name (string), return the correct Notification object.
        // The caller never needs to know which class is being created.
        switch (channel) {
            case "email":    return new EmailNotification();
            case "sms":      return new SMSNotification();
            case "whatsapp": return new WhatsAppNotification();
            case "push":     return new PushNotification();
            default:
                throw new IllegalArgumentException("Unknown notification channel: " + channel);
        }
    }
}

// Step 4: Usage — look how clean this is!
public void processOrder(Order order) {
    // The order processing code does NOT know about email servers,
    // API keys, or any notification internals. It just asks the
    // factory for a notification and calls send().
    Notification notif = NotificationFactory.create(order.getUser().getNotificationPreference());
    notif.send("Order " + order.getId() + " confirmed!");
}

public void processRefund(Refund refund) {
    // Same clean code — no duplication!
    Notification notif = NotificationFactory.create(refund.getUser().getNotificationPreference());
    notif.send("Refund of Rs." + refund.getAmount() + " processed!");
}

// Direct usage example:
Notification notif = NotificationFactory.create("whatsapp");
notif.send("Your Zomato order is on its way!");
// Output: [WhatsApp] Sending via Business API: Your Zomato order is on its way!
```

**Line-by-line walkthrough of the Factory class:**

| Line | What it does | Why |
|------|-------------|-----|
| `public static Notification create(String channel)` | Takes a string, returns a Notification | The return type is the BASE class — caller does not know the specific type |
| `switch (channel)` | Determines which class to instantiate based on the channel string | This is the ONLY place that knows which string maps to which class |
| `case "email": return new EmailNotification()` | Creates and returns the appropriate instance | Encapsulates all creation logic in one place |
| `default: throw ...` | Throws an exception if the channel is not found | Fail-fast for unknown channel types |

**Adding a new notification type (e.g., Telegram) requires exactly TWO changes:**
1. Create a new `TelegramNotification` class
2. Add `case "telegram": return new TelegramNotification();` in the factory's switch

That is it. ZERO changes to `processOrder`, `processRefund`, or any other code that uses notifications.

#### When to use Factory

- When your code needs to create objects but should not care about the specific class
- When you have a family of related classes (all notifications, all payments, all vehicles)
- When the creation logic is complex and you want to centralize it
- When you want to make it easy to add new types without changing existing code

**The trigger question:** "Am I creating different types of objects based on some input (string, enum, config)?" If yes, use a Factory.

#### When NOT to use Factory

- If you only have ONE type of object, a factory is overkill. Just create it directly.
- If the creation logic is trivial (just `new MyClass()`), adding a factory adds complexity for no benefit.
- Do not use a factory when the calling code genuinely NEEDS to know the specific type and use type-specific methods.

---

### Pattern 3: Builder — "Build complex objects step by step"

#### What is it?

The Builder pattern lets you construct complex objects piece by piece, in a readable sequence, rather than passing 15 parameters to a constructor. Each step is a method that sets one piece of the object and returns the builder itself (so you can chain calls).

Think of it as ordering a custom pizza at Domino's online. You do not say "Give me a large thin-crust pizza with mozzarella, onions, capsicum, paneer, extra cheese, oregano seasoning, and a side of garlic bread" in one breath. Instead, you go step by step: choose size, then crust, then toppings one by one, then extras. The Builder pattern works the same way.

#### Real-life analogy

Think about **building a Swiggy Instamart order**:
1. You start with an empty cart (empty builder)
2. You add milk (step 1)
3. You add bread (step 2)
4. You choose "No bag needed" (step 3)
5. You add a delivery tip (step 4)
6. You select express delivery (step 5)
7. You place the order (build)

Not every step is required. You might skip the tip. You might skip express delivery. The Builder pattern handles optional parts gracefully because each step is independent.

#### What problem does it solve? (The ugly code WITHOUT the pattern)

```java
// WITHOUT Builder — The PROBLEM
// Imagine constructing a database query with many optional parts

public static String buildQuery(String table, String[] columns, String[] whereConditions,
                                 String joinTable, String joinCondition, String groupBy,
                                 String having, String orderBy, String orderDirection,
                                 Integer limit, Integer offset, boolean distinct) {
    // Look at this MONSTER method signature.
    // It has 12 parameters! Most are optional.

    StringBuilder query = new StringBuilder("SELECT ");
    if (distinct) {
        query.append("DISTINCT ");
    }
    if (columns != null && columns.length > 0) {
        query.append(String.join(", ", columns));
    } else {
        query.append("*");
    }
    query.append(" FROM ").append(table);
    if (joinTable != null) {
        query.append(" JOIN ").append(joinTable).append(" ON ").append(joinCondition);
    }
    if (whereConditions != null && whereConditions.length > 0) {
        query.append(" WHERE ").append(String.join(" AND ", whereConditions));
    }
    if (groupBy != null) {
        query.append(" GROUP BY ").append(groupBy);
    }
    if (having != null) {
        query.append(" HAVING ").append(having);
    }
    if (orderBy != null) {
        query.append(" ORDER BY ").append(orderBy).append(" ").append(orderDirection);
    }
    if (limit != null) {
        query.append(" LIMIT ").append(limit);
    }
    if (offset != null) {
        query.append(" OFFSET ").append(offset);
    }
    return query.toString();
}

// Calling this is PAINFUL — what does each positional argument mean?
String query = buildQuery(
    "orders",                                              // table
    new String[]{"customer_name", "total"},                // columns
    new String[]{"status = 'delivered'", "city = 'Mumbai'"}, // where
    "customers",                                           // joinTable
    "orders.customer_id = customers.id",                   // joinCondition
    null,                                                  // groupBy
    null,                                                  // having
    "total",                                               // orderBy
    "DESC",                                                // orderDirection
    10,                                                    // limit
    0,                                                     // offset
    false                                                  // distinct
);
// QUESTION: When you read the call above, can you tell what "null, null" mean?
// No! You have to count parameters to figure it out. This is UNREADABLE.
```

**What goes wrong:**
1. **Unreadable calls** — You cannot tell what each argument means without checking the function signature
2. **Easy to make mistakes** — Accidentally swap `limit` and `offset` and you get wrong results with no error
3. **All-or-nothing** — You must pass `null` for parameters you do not need (like `groupBy` and `having` above)
4. **Hard to add new features** — Adding a new option means changing the function signature, which breaks all existing calls

#### The pattern — Clean code WITH Builder

```java
import java.util.ArrayList;
import java.util.List;

class QueryBuilder {
    private String table = "";
    private List<String> columns = new ArrayList<>();
    private List<String> conditions = new ArrayList<>();
    private List<String> joins = new ArrayList<>();
    private String orderByField = "";
    private String orderDirection = "ASC";
    private Integer limit = null;
    private Integer offset = null;
    private String groupBy = "";
    private boolean distinct = false;

    // Start with an empty query — nothing is set yet.

    public QueryBuilder table(String name) {
        // Set which table to query from. Like choosing a restaurant on Zomato.
        this.table = name;
        return this;  // KEY: return this so we can chain calls!
    }

    public QueryBuilder select(String... cols) {
        // Choose which columns to fetch. Like picking items from a menu.
        for (String col : cols) {
            this.columns.add(col);
        }
        return this;
    }

    public QueryBuilder where(String condition) {
        // Add a filter condition. Like applying filters on Flipkart.
        this.conditions.add(condition);
        return this;
    }

    public QueryBuilder join(String joinTable, String condition) {
        // Join with another table. Like combining data from two sources.
        this.joins.add("JOIN " + joinTable + " ON " + condition);
        return this;
    }

    public QueryBuilder orderBy(String field, String direction) {
        // Sort the results. Like sorting by price on Amazon.
        this.orderByField = field;
        this.orderDirection = direction;
        return this;
    }

    public QueryBuilder orderBy(String field) {
        return orderBy(field, "ASC");
    }

    public QueryBuilder limit(int n) {
        // Limit how many results to return. Like 'show 10 per page'.
        this.limit = n;
        return this;
    }

    public QueryBuilder offset(int n) {
        // Skip the first N results. Used for pagination.
        this.offset = n;
        return this;
    }

    public QueryBuilder distinct() {
        // Remove duplicate rows from results.
        this.distinct = true;
        return this;
    }

    public String build() {
        // Final step — assemble all the pieces into a SQL query string.
        // This is where the actual object (the query) is constructed.

        // Start building the SELECT clause
        StringBuilder query = new StringBuilder("SELECT ");
        if (this.distinct) {
            query.append("DISTINCT ");
        }

        if (!this.columns.isEmpty()) {
            query.append(String.join(", ", this.columns));
        } else {
            query.append("*");
        }

        // FROM clause
        query.append(" FROM ").append(this.table);

        // JOIN clauses (there can be multiple)
        for (String join : this.joins) {
            query.append(" ").append(join);
        }

        // WHERE clause (combine all conditions with AND)
        if (!this.conditions.isEmpty()) {
            query.append(" WHERE ").append(String.join(" AND ", this.conditions));
        }

        // GROUP BY
        if (!this.groupBy.isEmpty()) {
            query.append(" GROUP BY ").append(this.groupBy);
        }

        // ORDER BY
        if (!this.orderByField.isEmpty()) {
            query.append(" ORDER BY ").append(this.orderByField).append(" ").append(this.orderDirection);
        }

        // LIMIT and OFFSET
        if (this.limit != null) {
            query.append(" LIMIT ").append(this.limit);
        }
        if (this.offset != null) {
            query.append(" OFFSET ").append(this.offset);
        }

        return query.toString();
    }
}


// USAGE — look how readable this is compared to the 12-parameter function!

// Example 1: Simple query
String query = new QueryBuilder()
        .table("users")
        .where("age > 25")
        .where("city = 'Mumbai'")
        .orderBy("name")
        .limit(10)
        .build();

System.out.println(query);
// SELECT * FROM users WHERE age > 25 AND city = 'Mumbai' ORDER BY name ASC LIMIT 10

// Example 2: Complex query — still readable!
query = new QueryBuilder()
        .table("orders")
        .select("customer_name", "total")
        .join("customers", "orders.customer_id = customers.id")
        .where("status = 'delivered'")
        .where("city = 'Mumbai'")
        .orderBy("total", "DESC")
        .limit(10)
        .build();

System.out.println(query);
// SELECT customer_name, total FROM orders JOIN customers ON orders.customer_id = customers.id WHERE status = 'delivered' AND city = 'Mumbai' ORDER BY total DESC LIMIT 10

// Example 3: Minimal query — only use what you need
query = new QueryBuilder()
        .table("products")
        .limit(5)
        .build();

System.out.println(query);
// SELECT * FROM products LIMIT 5
```

**Why does each method return `this`?**

This is called **method chaining** or a **fluent interface**. Each method does its work (sets a value) and then returns the builder object itself. This allows you to write:

```java
new QueryBuilder().table("x").where("y").limit(5).build();
```

Instead of:

```java
QueryBuilder builder = new QueryBuilder();
builder.table("x");
builder.where("y");
builder.limit(5);
String result = builder.build();
```

Both work, but chaining is more concise and reads like a sentence.

#### When to use Builder

- When an object has many optional parameters (more than 4-5)
- When you want the construction process to be readable and self-documenting
- When the object must be constructed in steps (like assembling a meal, a query, or a document)
- Common in real codebases: SQL query builders, HTTP request builders, UI component builders, test data builders

**The trigger question:** "Am I building something complex with many optional parts?" If yes, use a Builder.

#### When NOT to use Builder

- If the object only has 2-3 required parameters, a simple constructor is fine
- If the object is always built the same way (no variation), a builder adds unnecessary code
- Do not use a builder when a simple POJO or data class would suffice

---

## Day 2: Structural Patterns (Sunday)

Structural patterns deal with how objects are composed — how you put simple pieces together to form larger structures. The core question: "How do I combine objects in flexible ways without creating a tangled mess?"

---

### Pattern 4: Adapter — "Make incompatible things work together"

#### What is it?

The Adapter pattern wraps an existing class with an incompatible interface and makes it compatible with the interface your code expects. It acts as a translator between two systems that speak different "languages."

Just like a power adapter lets you plug an Indian appliance (round pins, 230V) into a US socket (flat pins, 120V), a software adapter lets old code work with new interfaces (or vice versa) WITHOUT changing either one.

#### Real-life analogy

Think about **UPI (Unified Payments Interface)**. Before UPI, every bank had its own payment system:
- SBI used one API format
- HDFC used a completely different API format
- ICICI had yet another format

UPI acts as an ADAPTER. PhonePe, Google Pay, and PayTM all talk to UPI in ONE standard format, and UPI translates that into whatever format each bank understands. The banks did NOT change their systems. The apps did NOT learn each bank's format. The adapter (UPI/NPCI) sits in the middle and translates.

#### What problem does it solve? (The ugly code WITHOUT the pattern)

```java
// You are building a payment system for an e-commerce app.
// You have integrated with Razorpay.

// The OLD Razorpay SDK (can't change — it's a third-party library!)
class RazorpaySDK {
    public java.util.Map<String, Object> createPayment(int amountInPaise, String currency) {
        // Razorpay takes amount in PAISE (100 paise = 1 rupee)
        System.out.println("Razorpay: Charging " + amountInPaise + " paise (" + currency + ")");
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("payment_id", "pay_razor_123");
        result.put("status", "success");
        return result;
    }

    public java.util.Map<String, Object> createPayment(int amountInPaise) {
        return createPayment(amountInPaise, "INR");
    }
}

// Your application code expects amounts in RUPEES (not paise!)
// And your app uses a standard interface:
//   boolean pay(double amountInRupees, String description)

// WITHOUT an adapter, your code is littered with conversions:
public boolean checkout(double cartTotalRupees) {
    RazorpaySDK razorpay = new RazorpaySDK();
    // You must remember to convert EVERY TIME you call Razorpay
    java.util.Map<String, Object> result = razorpay.createPayment((int) (cartTotalRupees * 100));
    // You must know the Razorpay response format EVERY TIME
    return "success".equals(result.get("status"));
}

// PROBLEM: If you later switch from Razorpay to Stripe (which takes
// amount in dollars with a different method name), you must find and
// change EVERY place in your code that calls Razorpay.
// In a large app, this could be 50+ places.
```

#### The pattern — Clean code WITH Adapter

```java
import java.util.HashMap;
import java.util.Map;

// Step 1: Define the interface YOUR code expects
// This is the "shape" that all payment gateways must fit.
interface PaymentGateway {
    boolean pay(double amountInRupees, String description);
}

// Step 2: The old third-party SDK (we CANNOT change this)
class RazorpaySDK {
    public Map<String, Object> createPayment(int amountInPaise, String currency) {
        System.out.println("Razorpay: Charging " + amountInPaise + " paise (" + currency + ")");
        Map<String, Object> result = new HashMap<>();
        result.put("payment_id", "pay_razor_123");
        result.put("status", "success");
        return result;
    }

    public Map<String, Object> createPayment(int amountInPaise) {
        return createPayment(amountInPaise, "INR");
    }
}

// Step 3: The Adapter — translates between YOUR interface and Razorpay's
class RazorpayAdapter implements PaymentGateway {
    private RazorpaySDK razorpay;

    public RazorpayAdapter() {
        // The adapter WRAPS the old SDK — it holds a reference to it
        this.razorpay = new RazorpaySDK();
    }

    @Override
    public boolean pay(double amountInRupees, String description) {
        // Translates our standard interface to Razorpay's interface.
        // Our code says: pay(499.99, "Laptop case")
        // Razorpay wants: createPayment(49999, "INR")

        // Convert rupees to paise (Razorpay's expected format)
        int amountInPaise = (int) (amountInRupees * 100);
        // Call Razorpay's method using ITS interface
        Map<String, Object> result = this.razorpay.createPayment(amountInPaise);
        // Convert Razorpay's response to OUR expected format (boolean)
        return "success".equals(result.get("status"));
    }
}

// Now suppose we ALSO want to support Stripe (which takes USD):
class StripeSDK {
    public Map<String, Object> charge(int amountCents, String currency) {
        System.out.println("Stripe: Charging " + amountCents + " cents (" + currency + ")");
        Map<String, Object> result = new HashMap<>();
        result.put("id", "ch_stripe_456");
        result.put("paid", true);
        return result;
    }
}

class StripeAdapter implements PaymentGateway {
    private StripeSDK stripe;
    private double exchangeRate;  // INR to USD conversion rate

    public StripeAdapter(double exchangeRate) {
        this.stripe = new StripeSDK();
        this.exchangeRate = exchangeRate;
    }

    public StripeAdapter() {
        this(83.0);
    }

    @Override
    public boolean pay(double amountInRupees, String description) {
        // Convert INR to USD, then to cents
        double amountUsd = amountInRupees / this.exchangeRate;
        int amountCents = (int) (amountUsd * 100);
        Map<String, Object> result = this.stripe.charge(amountCents, "USD");
        return (boolean) result.get("paid");
    }
}

// Step 4: Usage — your checkout code is now CLEAN and gateway-agnostic

public static void checkout(double cartTotal, PaymentGateway gateway) {
    // This function works with ANY payment gateway — Razorpay, Stripe,
    // PayTM, or any future gateway. It does not know or care which one.
    boolean success = gateway.pay(cartTotal, "Cart checkout");
    if (success) {
        System.out.println("Payment of Rs." + cartTotal + " successful!");
    } else {
        System.out.println("Payment failed. Please try again.");
    }
}

// Using Razorpay:
checkout(499.99, new RazorpayAdapter());
// Output: Razorpay: Charging 49999 paise (INR)
// Output: Payment of Rs.499.99 successful!

// Switching to Stripe — only change ONE line, not 50:
checkout(499.99, new StripeAdapter());
// Output: Stripe: Charging 602 cents (USD)
// Output: Payment of Rs.499.99 successful!
```

#### When to use Adapter

- When integrating with third-party libraries/APIs that have different interfaces than your code expects
- When replacing one library with another (e.g., switching payment gateways, switching email providers)
- When connecting legacy code with new code
- When you need to make several unrelated classes work with the same interface

**The trigger question:** "Am I trying to use something that has a different interface than what my code expects?" If yes, use an Adapter.

#### When NOT to use Adapter

- If you control both sides (the caller and the callee), just change one of them to match the other
- If the interface mismatch is trivial (e.g., just a different method name with the same parameters), renaming might be simpler
- Do not create an adapter when you should be redesigning the interface itself

---

### Pattern 5: Decorator — "Add behavior without changing the original class"

#### What is it?

The Decorator pattern lets you dynamically add new behavior to an object by wrapping it inside another object. The wrapper has the SAME interface as the original, so the rest of the code cannot tell the difference. You can stack multiple decorators like layers.

Think of it as adding toppings to a Subway sandwich. The base sandwich is the same. Adding cheese does not change the base — it wraps around it. Adding jalapeños does not change the cheese or the base — it wraps around everything. Each topping adds something extra without modifying what is underneath.

#### Real-life analogy

Think about how **Flipkart processes an API request**:
1. The raw request comes in (base service)
2. First, **authentication** checks if the user is logged in (decorator 1)
3. Then, **rate limiting** checks if the user has made too many requests (decorator 2)
4. Then, **logging** records what was requested (decorator 3)
5. Then, **caching** checks if we already have the result stored (decorator 4)
6. Finally, the actual business logic runs

Each layer adds a concern WITHOUT the business logic knowing about it. The business logic does not know it is being authenticated, rate-limited, logged, or cached. Each decorator wraps the previous one.

#### What problem does it solve? (The ugly code WITHOUT the pattern)

```java
// WITHOUT Decorator — The PROBLEM
// You start with a simple data service

class DataService {
    public java.util.Map<String, String> getData(String query) {
        System.out.println("Fetching: " + query);
        java.util.Map<String, String> result = new java.util.HashMap<>();
        result.put("result", "some data");
        return result;
    }
}

// Now you need logging. Do you modify DataService?
class DataServiceWithLogging {
    public java.util.Map<String, String> getData(String query) {
        System.out.println("[LOG] Query: " + query);  // added logging
        System.out.println("Fetching: " + query);
        java.util.Map<String, String> result = new java.util.HashMap<>();
        result.put("result", "some data");
        System.out.println("[LOG] Done");  // added logging
        return result;
    }
}

// Now you ALSO need caching. Another class?
class DataServiceWithLoggingAndCaching {
    private java.util.Map<String, java.util.Map<String, String>> cache = new java.util.HashMap<>();

    public java.util.Map<String, String> getData(String query) {
        System.out.println("[LOG] Query: " + query);
        if (cache.containsKey(query)) {
            System.out.println("[CACHE HIT]");
            return cache.get(query);
        }
        System.out.println("Fetching: " + query);
        java.util.Map<String, String> result = new java.util.HashMap<>();
        result.put("result", "some data");
        cache.put(query, result);
        System.out.println("[LOG] Done");
        return result;
    }
}

// Now you ALSO need authentication? Metrics? Rate limiting?
// You would need:
// - DataServiceWithLoggingAndCachingAndAuth
// - DataServiceWithLoggingAndAuth (no caching)
// - DataServiceWithCachingAndAuth (no logging)
// - DataServiceWithCaching (no logging, no auth)
// ... and EVERY COMBINATION!
// With 4 features, that is 2^4 = 16 classes. EXPLOSION!
```

#### The pattern — Clean code WITH Decorator

```java
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

// Step 1: Define the interface that both the real service and
// all decorators will implement
interface DataService {
    Map<String, String> getData(String query);
}

// Step 2: The actual service that does real work
class RealDataService implements DataService {
    @Override
    public Map<String, String> getData(String query) {
        // This is the actual database call / API call / computation
        System.out.println("  [DB] Fetching from database: " + query);
        Map<String, String> result = new HashMap<>();
        result.put("result", "data for " + query);
        return result;
    }
}

// Step 3: Decorators — each one wraps a DataService and adds ONE behavior

class LoggingDecorator implements DataService {
    // Adds logging before and after every call.
    private DataService wrapped;

    public LoggingDecorator(DataService wrappedService) {
        // KEY: The decorator holds a reference to the thing it wraps.
        // That thing could be the real service OR another decorator.
        this.wrapped = wrappedService;
    }

    @Override
    public Map<String, String> getData(String query) {
        System.out.println("  [LOG] Incoming query: " + query);
        // Delegate to the wrapped service (could be real service or another decorator)
        Map<String, String> result = this.wrapped.getData(query);
        System.out.println("  [LOG] Query completed, result keys: " + result.keySet());
        return result;
    }
}

class CachingDecorator implements DataService {
    // Adds caching — returns cached result if available, skipping the actual fetch.
    private DataService wrapped;
    private Map<String, Map<String, String>> cache = new HashMap<>();

    public CachingDecorator(DataService wrappedService) {
        this.wrapped = wrappedService;
    }

    @Override
    public Map<String, String> getData(String query) {
        if (cache.containsKey(query)) {
            System.out.println("  [CACHE] Hit! Returning cached result for: " + query);
            return cache.get(query);
        }
        System.out.println("  [CACHE] Miss. Fetching fresh data for: " + query);
        Map<String, String> result = this.wrapped.getData(query);
        cache.put(query, result);
        return result;
    }
}

class AuthDecorator implements DataService {
    // Adds authentication check before allowing the query.
    private DataService wrapped;
    private List<String> allowedUsers;
    private String currentUser;

    public AuthDecorator(DataService wrappedService, List<String> allowedUsers) {
        this.wrapped = wrappedService;
        this.allowedUsers = allowedUsers;
        this.currentUser = null;
    }

    public void setUser(String username) {
        this.currentUser = username;
    }

    @Override
    public Map<String, String> getData(String query) {
        if (!allowedUsers.contains(currentUser)) {
            System.out.println("  [AUTH] Access denied for user: " + currentUser);
            throw new SecurityException("User " + currentUser + " not authorized");
        }
        System.out.println("  [AUTH] User " + currentUser + " authorized");
        return this.wrapped.getData(query);
    }
}

// Step 4: Stack decorators like layers!
// The order matters — outermost runs first.

// Start with the real service
DataService service = new RealDataService();

// Wrap it with logging (logs every call)
service = new LoggingDecorator(service);

// Wrap THAT with caching (checks cache before logging)
service = new CachingDecorator(service);

// Now use it:
System.out.println("--- First call (cache miss) ---");
service.getData("users");
// [CACHE] Miss. Fetching fresh data for: users
// [LOG] Incoming query: users
// [DB] Fetching from database: users
// [LOG] Query completed, result keys: ['result']

System.out.println("\n--- Second call (cache hit) ---");
service.getData("users");
// [CACHE] Hit! Returning cached result for: users
// (No LOG, no DB call — cache returned the result immediately!)

System.out.println("\n--- Different query (cache miss) ---");
service.getData("orders");
// [CACHE] Miss. Fetching fresh data for: orders
// [LOG] Incoming query: orders
// [DB] Fetching from database: orders
// [LOG] Query completed, result keys: ['result']
```

**How the wrapping works visually:**

```
Without decorators:
  Request -> RealDataService -> Response

With CachingDecorator(LoggingDecorator(RealDataService)):
  Request -> CachingDecorator -> LoggingDecorator -> RealDataService -> Response
             (checks cache)    (logs the call)     (actual DB fetch)
```

**The beauty:** You can combine ANY subset of decorators in ANY order:
- Logging + Caching: `new CachingDecorator(new LoggingDecorator(new RealDataService()))`
- Just Logging: `new LoggingDecorator(new RealDataService())`
- Auth + Caching: `new CachingDecorator(new AuthDecorator(new RealDataService(), allowedUsers))`
- All three: `new AuthDecorator(new CachingDecorator(new LoggingDecorator(new RealDataService())), users)`

Instead of 2^N classes, you have N decorator classes that combine freely.

#### When to use Decorator

- Adding cross-cutting concerns: logging, caching, authentication, metrics, rate limiting
- When you need to add behavior at runtime (not compile time)
- When subclassing would lead to a combinatorial explosion of classes
- When the original class cannot or should not be modified

**The trigger question:** "Am I adding behavior that is independent of the core logic and could be optionally layered on?" If yes, use a Decorator.

#### When NOT to use Decorator

- If you only need to add behavior in ONE specific way (no combinations), just extend the class
- If the decorator significantly changes the interface (not just adding behavior), it is not a decorator
- Deeply nested decorators (5+ layers) can make debugging difficult — be mindful of stack depth

---

### Pattern 6: Facade — "One simple interface for a complex system"

#### What is it?

The Facade pattern provides a simplified interface to a complex subsystem. Instead of forcing the client to interact with 5 different classes in the right order, you create ONE class with ONE method that orchestrates everything behind the scenes.

Think of it as using **MakeMyTrip** to book a vacation. Behind the scenes, MakeMyTrip talks to airlines for flights, hotels for rooms, cab services for airport transfers, insurance companies for travel insurance, and visa agencies. But YOU just see one page: "Book Trip." You do not individually call the airline, then the hotel, then the cab company. MakeMyTrip is the facade.

#### Real-life analogy

Think about **placing an order on Swiggy**:
When you tap "Place Order," what happens behind the scenes?
1. **Inventory system** checks if the restaurant has the items in stock
2. **Pricing system** calculates the total with offers, GST, delivery fee
3. **Payment system** charges your card/UPI
4. **Restaurant system** sends the order to the restaurant's kitchen display
5. **Delivery system** assigns a delivery partner
6. **Notification system** sends you an order confirmation
7. **Tracking system** starts live tracking

That is 7 different systems! But you just tapped ONE button. The Swiggy order service acts as a FACADE — it knows the right order to call these systems and handles the coordination.

#### What problem does it solve? (The ugly code WITHOUT the pattern)

```java
// WITHOUT Facade — The PROBLEM
// The client (UI/API layer) must know about and coordinate ALL subsystems

import java.util.List;
import java.util.Map;
import java.util.HashMap;

class InventorySystem {
    public boolean checkStock(String restaurantId, List<Map<String, Object>> items) {
        System.out.println("Checking stock at restaurant " + restaurantId + "...");
        return true;  // simplified
    }

    public void reserveItems(String restaurantId, List<Map<String, Object>> items) {
        System.out.println("Reserving items at restaurant " + restaurantId + "...");
    }
}

class PricingSystem {
    public double calculateTotal(List<Map<String, Object>> items, String couponCode) {
        double subtotal = 0;
        for (Map<String, Object> item : items) {
            subtotal += (double) item.get("price");
        }
        double gst = subtotal * 0.05;
        double deliveryFee = 30;
        double discount = 0;
        if ("FIRST50".equals(couponCode)) {
            discount = subtotal * 0.5;
        }
        double total = subtotal + gst + deliveryFee - discount;
        System.out.printf("Total: Rs.%.2f%n", total);
        return total;
    }
}

class PaymentSystem {
    public Map<String, String> charge(String userId, double amount, String method) {
        System.out.printf("Charging Rs.%.2f via %s%n", amount, method);
        Map<String, String> result = new HashMap<>();
        result.put("transaction_id", "txn_123");
        result.put("status", "success");
        return result;
    }
}

class RestaurantNotifier {
    public void sendToKitchen(String restaurantId, List<Map<String, Object>> orderDetails) {
        System.out.println("Order sent to kitchen at restaurant " + restaurantId);
    }
}

class DeliverySystem {
    public Map<String, String> assignRider(String restaurantId, String deliveryAddress) {
        System.out.println("Assigning rider from restaurant " + restaurantId + " to " + deliveryAddress);
        Map<String, String> result = new HashMap<>();
        result.put("rider_id", "rider_456");
        result.put("eta", "30 mins");
        return result;
    }
}

class NotificationService {
    public void sendNotification(String userId, String message) {
        System.out.println("Notification to " + userId + ": " + message);
    }
}

// The CLIENT must know about ALL these systems and call them in the RIGHT ORDER:
public void placeOrderWithoutFacade(String userId, String restaurantId,
        List<Map<String, Object>> items, String coupon, String paymentMethod, String address) {

    // 1. Check inventory
    InventorySystem inv = new InventorySystem();
    if (!inv.checkStock(restaurantId, items)) {
        System.out.println("Items not available!");
        return;
    }

    // 2. Calculate price
    PricingSystem pricing = new PricingSystem();
    double total = pricing.calculateTotal(items, coupon);

    // 3. Process payment
    PaymentSystem payment = new PaymentSystem();
    Map<String, String> txn = payment.charge(userId, total, paymentMethod);
    if (!"success".equals(txn.get("status"))) {
        System.out.println("Payment failed!");
        return;
    }

    // 4. Reserve items
    inv.reserveItems(restaurantId, items);

    // 5. Notify restaurant
    RestaurantNotifier notifier = new RestaurantNotifier();
    notifier.sendToKitchen(restaurantId, items);

    // 6. Assign delivery
    DeliverySystem delivery = new DeliverySystem();
    Map<String, String> rider = delivery.assignRider(restaurantId, address);

    // 7. Notify user
    NotificationService notifications = new NotificationService();
    notifications.sendNotification(userId, "Order confirmed! ETA: " + rider.get("eta"));
}

// The client must know:
// - Which systems exist
// - What order to call them in
// - How to handle failures at each step
// - All the internal details of each system
```

#### The pattern — Clean code WITH Facade

```java
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class OrderFacade {
    // The Facade hides ALL the complexity behind a single, simple method.
    // The client just calls placeOrder() and everything is handled.

    private InventorySystem inventory;
    private PricingSystem pricing;
    private PaymentSystem payment;
    private RestaurantNotifier restaurant;
    private DeliverySystem delivery;
    private NotificationService notifications;

    public OrderFacade() {
        // The facade knows about all the subsystems
        this.inventory = new InventorySystem();
        this.pricing = new PricingSystem();
        this.payment = new PaymentSystem();
        this.restaurant = new RestaurantNotifier();
        this.delivery = new DeliverySystem();
        this.notifications = new NotificationService();
    }

    public Map<String, String> placeOrder(String userId, String restaurantId,
            List<Map<String, Object>> items, String coupon,
            String paymentMethod, String deliveryAddress) {
        // ONE method that handles the entire order flow.
        // The caller does not need to know about inventory, pricing,
        // payment, delivery, or notification systems.

        // Step 1: Check if items are available
        if (!this.inventory.checkStock(restaurantId, items)) {
            Map<String, String> result = new HashMap<>();
            result.put("status", "failed");
            result.put("reason", "Items not available");
            return result;
        }

        // Step 2: Calculate the total price
        double total = this.pricing.calculateTotal(items, coupon);

        // Step 3: Process payment
        Map<String, String> transaction = this.payment.charge(userId, total, paymentMethod);
        if (!"success".equals(transaction.get("status"))) {
            Map<String, String> result = new HashMap<>();
            result.put("status", "failed");
            result.put("reason", "Payment failed");
            return result;
        }

        // Step 4: Reserve items in inventory
        this.inventory.reserveItems(restaurantId, items);

        // Step 5: Send order to restaurant kitchen
        this.restaurant.sendToKitchen(restaurantId, items);

        // Step 6: Assign delivery partner
        Map<String, String> rider = this.delivery.assignRider(restaurantId, deliveryAddress);

        // Step 7: Send confirmation to user
        this.notifications.sendNotification(
            userId,
            "Order confirmed! Rider " + rider.get("rider_id") + " assigned. ETA: " + rider.get("eta")
        );

        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("transaction_id", transaction.get("transaction_id"));
        result.put("rider_id", rider.get("rider_id"));
        result.put("eta", rider.get("eta"));
        return result;
    }
}


// Usage — look how simple this is for the caller!
OrderFacade facade = new OrderFacade();

List<Map<String, Object>> items = new ArrayList<>();
Map<String, Object> item1 = new HashMap<>();
item1.put("name", "Butter Chicken");
item1.put("price", 350.0);
items.add(item1);

Map<String, Object> item2 = new HashMap<>();
item2.put("name", "Naan");
item2.put("price", 50.0);
items.add(item2);

Map<String, String> result = facade.placeOrder(
    "user_sheetal",
    "rest_123",
    items,
    "FIRST50",
    "upi",
    "Andheri West, Mumbai"
);
System.out.println("\nOrder result: " + result);
```

**Before vs. After:**

| Without Facade | With Facade |
|---|---|
| Client creates 6 objects | Client creates 1 object |
| Client calls 7 methods in specific order | Client calls 1 method |
| Client handles errors at each step | Facade handles all errors |
| Client knows internal details of each system | Client knows nothing about internals |
| Adding a new step means changing client code | Adding a new step means changing only the Facade |

#### When to use Facade

- When you have a complex subsystem with many classes that must be used together in a specific sequence
- When you want to provide a simple API for external consumers (other teams, mobile apps, third-party integrations)
- When you want to reduce coupling between your subsystem and client code
- Microservice gateways are essentially facades for backend services

**The trigger question:** "Am I forcing the caller to coordinate multiple systems in a specific order?" If yes, create a Facade.

#### When NOT to use Facade

- If the subsystem is already simple (1-2 classes, 1-2 calls), a facade adds unnecessary indirection
- A facade should simplify, not limit. If clients sometimes NEED fine-grained control over the subsystems, keep the subsystems accessible too. The facade is an OPTION, not a prison.
- Do not create a "god facade" that wraps the entire application — it defeats the purpose

---

## Quick Reference

| Pattern | Category | What It Does | Real-Life Example | Interview Frequency |
|---------|----------|-------------|-------------------|-------------------|
| **Singleton** | Creational | Ensures only one instance exists | RBI, Aadhaar (UIDAI) | High |
| **Factory** | Creational | Creates objects without specifying exact class | Zomato notification system | High |
| **Builder** | Creational | Builds complex objects step by step | Swiggy cart, Domino's pizza customizer | Medium |
| **Adapter** | Structural | Makes incompatible interfaces work together | UPI connecting banks with payment apps | Medium |
| **Decorator** | Structural | Adds behavior dynamically by wrapping | Flipkart API middleware layers | High |
| **Facade** | Structural | Simplifies a complex subsystem into one interface | Swiggy "Place Order" button | Medium |

---

## Practice Exercises

### Exercise 1: Singleton Logger (Estimated time: 20 minutes)

**What to build:**
A Logger class that acts as a Singleton. In any real application (Swiggy, Ola, Flipkart), there is exactly ONE logger that writes to the log file. Multiple loggers writing to the same file simultaneously would corrupt the file.

**Requirements:**
1. The Logger class must be a Singleton — calling `Logger.getInstance()` anywhere returns the same instance
2. It must have three methods:
   - `info(message)` — logs an informational message with timestamp prefix `[INFO]`
   - `warning(message)` — logs a warning with prefix `[WARNING]`
   - `error(message)` — logs an error with prefix `[ERROR]`
3. All log messages are stored in an internal list called `logs`
4. A method `getLogs()` returns all logs
5. A method `clearLogs()` empties the log list

**Test your solution:**
```java
Logger logger1 = Logger.getInstance();
Logger logger2 = Logger.getInstance();

logger1.info("Server started");
logger2.warning("High memory usage");
logger1.error("Database connection failed");

// Both should be the same instance:
assert logger1 == logger2 : "Logger is not a Singleton!";

// All logs should be visible from either reference:
assert logger2.getLogs().size() == 3;
System.out.println(logger2.getLogs());
// Should show all 3 messages with timestamps and severity levels
```

---

### Exercise 2: Vehicle Factory — Ola/Uber Style (Estimated time: 25 minutes)

**What to build:**
When you open Ola, you see different ride options: Auto, Mini, Sedan, SUV, Bike. Each has different properties (fuel type, capacity, base fare). Build a Factory that creates the right vehicle based on the ride type.

**Requirements:**
1. Create a base class `Vehicle` with methods:
   - `start()` — prints a start message
   - `stop()` — prints a stop message
   - `fuelType()` — returns the fuel type as a string
   - `capacity()` — returns number of passengers
   - `baseFare()` — returns base fare in rupees
2. Create concrete classes: `Auto`, `Bike`, `MiniCar`, `Sedan`, `SUV`
   - Auto: CNG fuel, 3 passengers, Rs.30 base fare
   - Bike: Petrol, 1 passenger, Rs.15 base fare
   - MiniCar: Petrol, 4 passengers, Rs.50 base fare
   - Sedan: Petrol/Diesel, 4 passengers, Rs.80 base fare
   - SUV: Diesel, 6 passengers, Rs.120 base fare
3. Create a `VehicleFactory` with a `create(rideType)` method
4. Create a `calculateFare(rideType, distanceKm)` function that uses the factory to create the vehicle and calculates fare as: `baseFare + (distance * 12)` for bikes, `baseFare + (distance * 15)` for others

**Test your solution:**
```java
Vehicle vehicle = VehicleFactory.create("sedan");
vehicle.start();   // "Sedan started. Enjoy your comfortable ride!"
System.out.println(vehicle.fuelType());  // "Petrol/Diesel"
System.out.println(vehicle.capacity());   // 4

double fare = calculateFare("auto", 5);  // 30 + (5 * 15) = 105
System.out.println("Auto fare for 5 km: Rs." + fare);
```

---

### Exercise 3: Coffee Builder — Starbucks Style (Estimated time: 20 minutes)

**What to build:**
Build a coffee ordering system like CCD (Cafe Coffee Day) or Starbucks using the Builder pattern. A coffee order has many optional parts — you might want milk but no sugar, or extra sugar but no whipped cream.

**Requirements:**
1. Create a `Coffee` class with attributes: size, milkType, sugarSpoons, flavor, whippedCream, extraShot, temperature
2. Create a `CoffeeBuilder` class with these chainable methods:
   - `size(size)` — "small", "medium", "large"
   - `milk(type)` — "regular", "oat", "almond", "soy", "none"
   - `sugar(spoons)` — number of spoons (0 = no sugar)
   - `flavor(name)` — "vanilla", "hazelnut", "caramel", "mocha", or null
   - `withWhippedCream()` — adds whipped cream
   - `extraShot()` — adds an extra espresso shot
   - `iced()` — makes it a cold coffee
   - `build()` — returns the final Coffee object
3. The `Coffee` class should have a `toString()` method that prints a readable description
4. Add a `price()` method on Coffee:
   - Small: Rs.150, Medium: Rs.200, Large: Rs.250
   - Each add-on costs: milk (free if regular, Rs.30 for alternatives), flavor Rs.20, whipped cream Rs.30, extra shot Rs.40

**Test your solution:**
```java
Coffee coffee = new CoffeeBuilder()
        .size("large")
        .milk("oat")
        .sugar(2)
        .flavor("hazelnut")
        .withWhippedCream()
        .iced()
        .build();

System.out.println(coffee);
// Large iced coffee with oat milk, 2 sugar, hazelnut flavor, whipped cream
System.out.println("Price: Rs." + coffee.price());
// Price: Rs.330 (250 + 30 oat milk + 20 flavor + 30 whipped cream)

// Simple coffee — only required options
Coffee simple = new CoffeeBuilder()
        .size("small")
        .build();
System.out.println(simple);
// Small hot coffee, no milk, no sugar
System.out.println("Price: Rs." + simple.price());
// Price: Rs.150
```

---

### Exercise 4 (Bonus): Combine Patterns — Payment Processing System

**What to build:**
Build a payment processing system for an e-commerce site that uses THREE patterns together:
1. **Singleton** — for the PaymentProcessor (one processor for the whole app)
2. **Factory** — to create the right payment gateway based on method (UPI, Credit Card, Net Banking, Wallet)
3. **Adapter** — each gateway wraps a "third-party SDK" with a different interface

This is realistic — in a Microsoft interview, you are expected to combine patterns naturally.

**Hint for structure:**
```
PaymentProcessor (Singleton)
  |
  |--- uses PaymentGatewayFactory (Factory)
         |
         |--- creates UPIAdapter (Adapter wrapping UPIServiceSDK)
         |--- creates CardAdapter (Adapter wrapping CardProcessorSDK)
         |--- creates NetBankingAdapter (Adapter wrapping NetBankingSDK)
```

---

## Self-Check

Before moving to Week 3, verify:

- [ ] I can explain WHAT each of the 6 patterns does in plain English (no code)
- [ ] I can explain WHY each pattern exists — what problem it solves
- [ ] I can give a real-life analogy for each pattern (Indian context preferred)
- [ ] I can write the code for all 6 patterns from memory (without looking at notes)
- [ ] I know when to use each pattern (the "trigger question")
- [ ] I know when NOT to use each pattern (avoid over-engineering)
- [ ] I completed Exercises 1, 2, and 3
- [ ] I attempted Exercise 4 (bonus)
- [ ] I can identify which pattern to use when given a problem description (e.g., "Design a notification system" -> Factory + Strategy)
