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

```python
# WITHOUT Singleton — The PROBLEM
# Imagine every part of your Zomato backend creates its own database connection

class DatabaseConnection:
    def __init__(self):
        # This is EXPENSIVE — takes 2-3 seconds, opens a network socket
        print("Opening new database connection...")
        self.connection = "Connected to MongoDB"

# In your order service:
db1 = DatabaseConnection()  # Opens connection #1

# In your restaurant service:
db2 = DatabaseConnection()  # Opens connection #2 (WASTEFUL!)

# In your delivery service:
db3 = DatabaseConnection()  # Opens connection #3 (EVEN MORE WASTEFUL!)

# In your payment service:
db4 = DatabaseConnection()  # Opens connection #4

# PROBLEM: You now have 4 separate connections!
# MongoDB has a default limit of ~100 connections.
# If every service creates its own, you will hit the limit fast.
# Also, db1 and db2 are DIFFERENT objects — changes to one do not reflect in the other.

print(db1 is db2)  # False — they are different objects!
```

**What goes wrong:**
1. Each connection uses memory and network resources
2. You can exhaust the database connection limit
3. No coordination — if one part of the code changes a setting on "its" connection, other parts do not see it
4. In a real app like Zomato handling 10,000 orders/minute, creating a new connection per request would crash everything

#### The pattern — Clean code WITH Singleton

```python
class DatabaseConnection:
    # Step 1: A class variable to hold the ONE instance
    # This variable belongs to the CLASS, not to any specific object.
    # Initially, no instance exists, so it is None.
    _instance = None

    # Step 2: Override __new__ to control object creation
    # __new__ is called BEFORE __init__. It is responsible for actually
    # creating the object in memory. By overriding it, we intercept
    # the creation process.
    def __new__(cls):
        # Step 3: Check if an instance already exists
        if cls._instance is None:
            # First time — no instance exists yet.
            # Call the parent class's __new__ to actually create the object.
            cls._instance = super().__new__(cls)
            # Do the expensive setup ONCE
            print("Opening database connection... (this only happens ONCE)")
            cls._instance.connection = "Connected to MongoDB"
        # Step 4: Whether we just created it or it already existed,
        # return the SAME instance every time
        return cls._instance


# Let us test it:
db1 = DatabaseConnection()  # Prints: "Opening database connection..."
db2 = DatabaseConnection()  # Prints NOTHING — reuses existing instance
db3 = DatabaseConnection()  # Prints NOTHING — same instance again

# All three variables point to the EXACT SAME object in memory
print(db1 is db2)  # True
print(db2 is db3)  # True

# Any change through one variable is visible through all others,
# because they all reference the same object:
db1.connection = "Connected to PostgreSQL"
print(db3.connection)  # "Connected to PostgreSQL" — same object!
```

**Line-by-line walkthrough:**

| Line | What it does | Why |
|------|-------------|-----|
| `_instance = None` | Class-level variable, shared by ALL instances | This is our "storage" for the single instance |
| `def __new__(cls)` | Override the object creation method | `__new__` runs BEFORE `__init__` — it controls whether a new object is created at all |
| `if cls._instance is None` | Check: does an instance already exist? | If yes, skip creation entirely |
| `super().__new__(cls)` | Actually create the object (only on first call) | We delegate to Python's default creation mechanism |
| `return cls._instance` | Always return the same instance | This is the key — every call to `DatabaseConnection()` returns the same object |

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
- In multi-threaded applications, the basic Singleton shown above is NOT thread-safe. Two threads could both see `_instance is None` at the same time and create two instances. For production code, use locks or Python's `threading.Lock`.

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

```python
# WITHOUT Factory — The PROBLEM
# Every place in your code that needs to send a notification has to know
# about ALL notification types and their creation details

def process_order(order):
    # Decide which notification to send based on user preference
    user_pref = order.user.notification_preference

    if user_pref == "email":
        # Must know how to create email notifications
        notif = EmailNotification(
            smtp_server="smtp.gmail.com",
            port=587,
            from_address="noreply@zomato.com"
        )
    elif user_pref == "sms":
        # Must know how to create SMS notifications
        notif = SMSNotification(
            api_key="twilio_key_123",
            from_number="+91XXXXXXXXXX"
        )
    elif user_pref == "whatsapp":
        # Must know how to create WhatsApp notifications
        notif = WhatsAppNotification(
            business_id="zomato_wa_123",
            template="order_confirmation"
        )
    elif user_pref == "push":
        notif = PushNotification(
            firebase_key="firebase_key_456"
        )

    notif.send(f"Order {order.id} confirmed!")

def process_refund(refund):
    user_pref = refund.user.notification_preference

    # SAME ugly if-elif-else block AGAIN!
    if user_pref == "email":
        notif = EmailNotification(
            smtp_server="smtp.gmail.com",
            port=587,
            from_address="noreply@zomato.com"
        )
    elif user_pref == "sms":
        notif = SMSNotification(
            api_key="twilio_key_123",
            from_number="+91XXXXXXXXXX"
        )
    # ... same block repeated EVERYWHERE
```

**What goes wrong:**
1. **Code duplication** — The if-elif block is copied everywhere notifications are sent
2. **Tight coupling** — Every function knows the internal details of every notification type (API keys, server addresses)
3. **Hard to add new types** — If you add Telegram notifications, you must find and update EVERY place that creates notifications
4. **Hard to test** — You cannot easily test `process_order` without real SMTP servers and Twilio accounts

#### The pattern — Clean code WITH Factory

```python
from abc import ABC, abstractmethod

# Step 1: Define a common interface (base class)
# All notification types must have a send() method.
# This is a CONTRACT — any notification, regardless of type,
# can be used by calling .send(message).
class Notification(ABC):
    @abstractmethod
    def send(self, message):
        """Every notification type MUST implement this method."""
        pass

# Step 2: Create concrete implementations
# Each class handles its own creation details internally.
# The outside world does not need to know about SMTP servers or API keys.

class EmailNotification(Notification):
    def __init__(self):
        # Internal details — hidden from the rest of the code
        self.smtp_server = "smtp.gmail.com"
        self.port = 587

    def send(self, message):
        print(f"[EMAIL] Sending via {self.smtp_server}: {message}")

class SMSNotification(Notification):
    def __init__(self):
        self.api_key = "twilio_key_123"

    def send(self, message):
        print(f"[SMS] Sending via Twilio: {message}")

class WhatsAppNotification(Notification):
    def __init__(self):
        self.business_id = "zomato_wa_123"

    def send(self, message):
        print(f"[WhatsApp] Sending via Business API: {message}")

class PushNotification(Notification):
    def __init__(self):
        self.firebase_key = "firebase_key_456"

    def send(self, message):
        print(f"[Push] Sending via Firebase: {message}")

# Step 3: The Factory — ONE place that knows how to create each type
class NotificationFactory:
    @staticmethod
    def create(channel: str) -> Notification:
        """
        Given a channel name (string), return the correct Notification object.
        The caller never needs to know which class is being created.
        """
        creators = {
            "email": EmailNotification,
            "sms": SMSNotification,
            "whatsapp": WhatsAppNotification,
            "push": PushNotification,
        }

        creator = creators.get(channel)
        if creator is None:
            raise ValueError(f"Unknown notification channel: {channel}")

        return creator()  # Create and return an instance

# Step 4: Usage — look how clean this is!
def process_order(order):
    # The order processing code does NOT know about email servers,
    # API keys, or any notification internals. It just asks the
    # factory for a notification and calls send().
    notif = NotificationFactory.create(order.user.notification_preference)
    notif.send(f"Order {order.id} confirmed!")

def process_refund(refund):
    # Same clean code — no duplication!
    notif = NotificationFactory.create(refund.user.notification_preference)
    notif.send(f"Refund of Rs.{refund.amount} processed!")

# Direct usage example:
notif = NotificationFactory.create("whatsapp")
notif.send("Your Zomato order is on its way!")
# Output: [WhatsApp] Sending via Business API: Your Zomato order is on its way!
```

**Line-by-line walkthrough of the Factory class:**

| Line | What it does | Why |
|------|-------------|-----|
| `@staticmethod` | No `self` needed — Factory does not hold state | A factory is just a creation utility, it does not need its own data |
| `def create(channel: str) -> Notification` | Takes a string, returns a Notification | The return type is the BASE class — caller does not know the specific type |
| `creators = {...}` | Dictionary mapping strings to classes | This is the ONLY place that knows which string maps to which class |
| `creators.get(channel)` | Look up the class for the given channel | Returns `None` if the channel is not found |
| `return creator()` | Call the class to create an instance | `EmailNotification()` creates an EmailNotification object |

**Adding a new notification type (e.g., Telegram) requires exactly TWO changes:**
1. Create a new `TelegramNotification` class
2. Add `"telegram": TelegramNotification` to the dictionary in the factory

That is it. ZERO changes to `process_order`, `process_refund`, or any other code that uses notifications.

#### When to use Factory

- When your code needs to create objects but should not care about the specific class
- When you have a family of related classes (all notifications, all payments, all vehicles)
- When the creation logic is complex and you want to centralize it
- When you want to make it easy to add new types without changing existing code

**The trigger question:** "Am I creating different types of objects based on some input (string, enum, config)?" If yes, use a Factory.

#### When NOT to use Factory

- If you only have ONE type of object, a factory is overkill. Just create it directly.
- If the creation logic is trivial (just `MyClass()`), adding a factory adds complexity for no benefit.
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

```python
# WITHOUT Builder — The PROBLEM
# Imagine constructing a database query with many optional parts

def build_query(table, columns=None, where_conditions=None, join_table=None,
                join_condition=None, group_by=None, having=None, order_by=None,
                order_direction="ASC", limit=None, offset=None, distinct=False):
    """
    Look at this MONSTER function signature.
    It has 12 parameters! Most are optional.
    """
    query = "SELECT "
    if distinct:
        query += "DISTINCT "
    if columns:
        query += ", ".join(columns)
    else:
        query += "*"
    query += f" FROM {table}"
    if join_table:
        query += f" JOIN {join_table} ON {join_condition}"
    if where_conditions:
        query += " WHERE " + " AND ".join(where_conditions)
    if group_by:
        query += f" GROUP BY {group_by}"
    if having:
        query += f" HAVING {having}"
    if order_by:
        query += f" ORDER BY {order_by} {order_direction}"
    if limit:
        query += f" LIMIT {limit}"
    if offset:
        query += f" OFFSET {offset}"
    return query

# Calling this is PAINFUL — what does each positional argument mean?
query = build_query(
    "orders",                            # table
    ["customer_name", "total"],          # columns
    ["status = 'delivered'", "city = 'Mumbai'"],  # where
    "customers",                          # join_table
    "orders.customer_id = customers.id", # join_condition
    None,                                 # group_by
    None,                                 # having
    "total",                              # order_by
    "DESC",                               # order_direction
    10,                                   # limit
    0,                                    # offset
    False                                 # distinct
)
# QUESTION: When you read the call above, can you tell what "None, None" mean?
# No! You have to count parameters to figure it out. This is UNREADABLE.
```

**What goes wrong:**
1. **Unreadable calls** — You cannot tell what each argument means without checking the function signature
2. **Easy to make mistakes** — Accidentally swap `limit` and `offset` and you get wrong results with no error
3. **All-or-nothing** — You must pass `None` for parameters you do not need (like `group_by` and `having` above)
4. **Hard to add new features** — Adding a new option means changing the function signature, which breaks all existing calls

#### The pattern — Clean code WITH Builder

```python
class QueryBuilder:
    def __init__(self):
        """Start with an empty query — nothing is set yet."""
        self._table = ""
        self._columns = []
        self._conditions = []
        self._joins = []
        self._order_by = ""
        self._order_direction = "ASC"
        self._limit = None
        self._offset = None
        self._group_by = ""
        self._distinct = False

    def table(self, name: str):
        """Set which table to query from. Like choosing a restaurant on Zomato."""
        self._table = name
        return self  # KEY: return self so we can chain calls!

    def select(self, *columns):
        """Choose which columns to fetch. Like picking items from a menu."""
        self._columns.extend(columns)
        return self

    def where(self, condition: str):
        """Add a filter condition. Like applying filters on Flipkart."""
        self._conditions.append(condition)
        return self

    def join(self, table: str, condition: str):
        """Join with another table. Like combining data from two sources."""
        self._joins.append(f"JOIN {table} ON {condition}")
        return self

    def order_by(self, field: str, direction: str = "ASC"):
        """Sort the results. Like sorting by price on Amazon."""
        self._order_by = field
        self._order_direction = direction
        return self

    def limit(self, n: int):
        """Limit how many results to return. Like 'show 10 per page'."""
        self._limit = n
        return self

    def offset(self, n: int):
        """Skip the first N results. Used for pagination."""
        self._offset = n
        return self

    def distinct(self):
        """Remove duplicate rows from results."""
        self._distinct = True
        return self

    def build(self) -> str:
        """
        Final step — assemble all the pieces into a SQL query string.
        This is where the actual object (the query) is constructed.
        """
        # Start building the SELECT clause
        query = "SELECT "
        if self._distinct:
            query += "DISTINCT "

        if self._columns:
            query += ", ".join(self._columns)
        else:
            query += "*"

        # FROM clause
        query += f" FROM {self._table}"

        # JOIN clauses (there can be multiple)
        for join in self._joins:
            query += f" {join}"

        # WHERE clause (combine all conditions with AND)
        if self._conditions:
            query += " WHERE " + " AND ".join(self._conditions)

        # GROUP BY
        if self._group_by:
            query += f" GROUP BY {self._group_by}"

        # ORDER BY
        if self._order_by:
            query += f" ORDER BY {self._order_by} {self._order_direction}"

        # LIMIT and OFFSET
        if self._limit is not None:
            query += f" LIMIT {self._limit}"
        if self._offset is not None:
            query += f" OFFSET {self._offset}"

        return query


# USAGE — look how readable this is compared to the 12-parameter function!

# Example 1: Simple query
query = (QueryBuilder()
         .table("users")
         .where("age > 25")
         .where("city = 'Mumbai'")
         .order_by("name")
         .limit(10)
         .build())

print(query)
# SELECT * FROM users WHERE age > 25 AND city = 'Mumbai' ORDER BY name ASC LIMIT 10

# Example 2: Complex query — still readable!
query = (QueryBuilder()
         .table("orders")
         .select("customer_name", "total")
         .join("customers", "orders.customer_id = customers.id")
         .where("status = 'delivered'")
         .where("city = 'Mumbai'")
         .order_by("total", "DESC")
         .limit(10)
         .build())

print(query)
# SELECT customer_name, total FROM orders JOIN customers ON orders.customer_id = customers.id WHERE status = 'delivered' AND city = 'Mumbai' ORDER BY total DESC LIMIT 10

# Example 3: Minimal query — only use what you need
query = (QueryBuilder()
         .table("products")
         .limit(5)
         .build())

print(query)
# SELECT * FROM products LIMIT 5
```

**Why does each method return `self`?**

This is called **method chaining** or a **fluent interface**. Each method does its work (sets a value) and then returns the builder object itself. This allows you to write:

```python
builder.table("x").where("y").limit(5).build()
```

Instead of:

```python
builder.table("x")
builder.where("y")
builder.limit(5)
result = builder.build()
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
- Do not use a builder when a simple dictionary or data class would suffice

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

```python
# You are building a payment system for an e-commerce app.
# You have integrated with Razorpay.

# The OLD Razorpay SDK (can't change — it's a third-party library!)
class RazorpaySDK:
    def create_payment(self, amount_in_paise: int, currency: str = "INR"):
        """Razorpay takes amount in PAISE (100 paise = 1 rupee)"""
        print(f"Razorpay: Charging {amount_in_paise} paise ({currency})")
        return {"payment_id": "pay_razor_123", "status": "success"}

# Your application code expects amounts in RUPEES (not paise!)
# And your app uses a standard interface:
#   def pay(amount_in_rupees, description) -> bool

# WITHOUT an adapter, your code is littered with conversions:
def checkout(cart_total_rupees):
    razorpay = RazorpaySDK()
    # You must remember to convert EVERY TIME you call Razorpay
    result = razorpay.create_payment(int(cart_total_rupees * 100))
    # You must know the Razorpay response format EVERY TIME
    return result["status"] == "success"

# PROBLEM: If you later switch from Razorpay to Stripe (which takes
# amount in dollars with a different method name), you must find and
# change EVERY place in your code that calls Razorpay.
# In a large app, this could be 50+ places.
```

#### The pattern — Clean code WITH Adapter

```python
from abc import ABC, abstractmethod

# Step 1: Define the interface YOUR code expects
# This is the "shape" that all payment gateways must fit.
class PaymentGateway(ABC):
    @abstractmethod
    def pay(self, amount_in_rupees: float, description: str) -> bool:
        """Pay the given amount in rupees. Return True if successful."""
        pass

# Step 2: The old third-party SDK (we CANNOT change this)
class RazorpaySDK:
    def create_payment(self, amount_in_paise: int, currency: str = "INR"):
        print(f"Razorpay: Charging {amount_in_paise} paise ({currency})")
        return {"payment_id": "pay_razor_123", "status": "success"}

# Step 3: The Adapter — translates between YOUR interface and Razorpay's
class RazorpayAdapter(PaymentGateway):
    def __init__(self):
        # The adapter WRAPS the old SDK — it holds a reference to it
        self.razorpay = RazorpaySDK()

    def pay(self, amount_in_rupees: float, description: str) -> bool:
        """
        Translates our standard interface to Razorpay's interface.
        Our code says: pay(499.99, "Laptop case")
        Razorpay wants: create_payment(49999, "INR")
        """
        # Convert rupees to paise (Razorpay's expected format)
        amount_in_paise = int(amount_in_rupees * 100)
        # Call Razorpay's method using ITS interface
        result = self.razorpay.create_payment(amount_in_paise)
        # Convert Razorpay's response to OUR expected format (boolean)
        return result["status"] == "success"

# Now suppose we ALSO want to support Stripe (which takes USD):
class StripeSDK:
    def charge(self, amount_cents: int, currency: str):
        print(f"Stripe: Charging {amount_cents} cents ({currency})")
        return {"id": "ch_stripe_456", "paid": True}

class StripeAdapter(PaymentGateway):
    def __init__(self, exchange_rate: float = 83.0):
        self.stripe = StripeSDK()
        self.exchange_rate = exchange_rate  # INR to USD conversion rate

    def pay(self, amount_in_rupees: float, description: str) -> bool:
        # Convert INR to USD, then to cents
        amount_usd = amount_in_rupees / self.exchange_rate
        amount_cents = int(amount_usd * 100)
        result = self.stripe.charge(amount_cents, "USD")
        return result["paid"]

# Step 4: Usage — your checkout code is now CLEAN and gateway-agnostic

def checkout(cart_total: float, gateway: PaymentGateway):
    """
    This function works with ANY payment gateway — Razorpay, Stripe,
    PayTM, or any future gateway. It does not know or care which one.
    """
    success = gateway.pay(cart_total, "Cart checkout")
    if success:
        print(f"Payment of Rs.{cart_total} successful!")
    else:
        print("Payment failed. Please try again.")

# Using Razorpay:
checkout(499.99, RazorpayAdapter())
# Output: Razorpay: Charging 49999 paise (INR)
# Output: Payment of Rs.499.99 successful!

# Switching to Stripe — only change ONE line, not 50:
checkout(499.99, StripeAdapter())
# Output: Stripe: Charging 602 cents (USD)
# Output: Payment of Rs.499.99 successful!
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

```python
# WITHOUT Decorator — The PROBLEM
# You start with a simple data service

class DataService:
    def get_data(self, query):
        print(f"Fetching: {query}")
        return {"result": "some data"}

# Now you need logging. Do you modify DataService?
class DataServiceWithLogging:
    def get_data(self, query):
        print(f"[LOG] Query: {query}")  # added logging
        print(f"Fetching: {query}")
        result = {"result": "some data"}
        print(f"[LOG] Done")  # added logging
        return result

# Now you ALSO need caching. Another class?
class DataServiceWithLoggingAndCaching:
    def __init__(self):
        self.cache = {}

    def get_data(self, query):
        print(f"[LOG] Query: {query}")
        if query in self.cache:
            print(f"[CACHE HIT]")
            return self.cache[query]
        print(f"Fetching: {query}")
        result = {"result": "some data"}
        self.cache[query] = result
        print(f"[LOG] Done")
        return result

# Now you ALSO need authentication? Metrics? Rate limiting?
# You would need:
# - DataServiceWithLoggingAndCachingAndAuth
# - DataServiceWithLoggingAndAuth (no caching)
# - DataServiceWithCachingAndAuth (no logging)
# - DataServiceWithCaching (no logging, no auth)
# ... and EVERY COMBINATION!
# With 4 features, that is 2^4 = 16 classes. EXPLOSION!
```

#### The pattern — Clean code WITH Decorator

```python
from abc import ABC, abstractmethod

# Step 1: Define the interface that both the real service and
# all decorators will implement
class DataService(ABC):
    @abstractmethod
    def get_data(self, query: str) -> dict:
        pass

# Step 2: The actual service that does real work
class RealDataService(DataService):
    def get_data(self, query: str) -> dict:
        # This is the actual database call / API call / computation
        print(f"  [DB] Fetching from database: {query}")
        return {"result": f"data for {query}"}

# Step 3: Decorators — each one wraps a DataService and adds ONE behavior

class LoggingDecorator(DataService):
    """Adds logging before and after every call."""
    def __init__(self, wrapped_service: DataService):
        # KEY: The decorator holds a reference to the thing it wraps.
        # That thing could be the real service OR another decorator.
        self._wrapped = wrapped_service

    def get_data(self, query: str) -> dict:
        print(f"  [LOG] Incoming query: {query}")
        # Delegate to the wrapped service (could be real service or another decorator)
        result = self._wrapped.get_data(query)
        print(f"  [LOG] Query completed, result keys: {list(result.keys())}")
        return result

class CachingDecorator(DataService):
    """Adds caching — returns cached result if available, skipping the actual fetch."""
    def __init__(self, wrapped_service: DataService):
        self._wrapped = wrapped_service
        self._cache = {}

    def get_data(self, query: str) -> dict:
        if query in self._cache:
            print(f"  [CACHE] Hit! Returning cached result for: {query}")
            return self._cache[query]
        print(f"  [CACHE] Miss. Fetching fresh data for: {query}")
        result = self._wrapped.get_data(query)
        self._cache[query] = result
        return result

class AuthDecorator(DataService):
    """Adds authentication check before allowing the query."""
    def __init__(self, wrapped_service: DataService, allowed_users: list):
        self._wrapped = wrapped_service
        self._allowed_users = allowed_users
        self._current_user = None

    def set_user(self, username: str):
        self._current_user = username

    def get_data(self, query: str) -> dict:
        if self._current_user not in self._allowed_users:
            print(f"  [AUTH] Access denied for user: {self._current_user}")
            raise PermissionError(f"User {self._current_user} not authorized")
        print(f"  [AUTH] User {self._current_user} authorized")
        return self._wrapped.get_data(query)

# Step 4: Stack decorators like layers!
# The order matters — outermost runs first.

# Start with the real service
service = RealDataService()

# Wrap it with logging (logs every call)
service = LoggingDecorator(service)

# Wrap THAT with caching (checks cache before logging)
service = CachingDecorator(service)

# Now use it:
print("--- First call (cache miss) ---")
service.get_data("users")
# [CACHE] Miss. Fetching fresh data for: users
# [LOG] Incoming query: users
# [DB] Fetching from database: users
# [LOG] Query completed, result keys: ['result']

print("\n--- Second call (cache hit) ---")
service.get_data("users")
# [CACHE] Hit! Returning cached result for: users
# (No LOG, no DB call — cache returned the result immediately!)

print("\n--- Different query (cache miss) ---")
service.get_data("orders")
# [CACHE] Miss. Fetching fresh data for: orders
# [LOG] Incoming query: orders
# [DB] Fetching from database: orders
# [LOG] Query completed, result keys: ['result']
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
- Logging + Caching: `CachingDecorator(LoggingDecorator(RealDataService()))`
- Just Logging: `LoggingDecorator(RealDataService())`
- Auth + Caching: `CachingDecorator(AuthDecorator(RealDataService(), allowed_users))`
- All three: `AuthDecorator(CachingDecorator(LoggingDecorator(RealDataService())), users)`

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

```python
# WITHOUT Facade — The PROBLEM
# The client (UI/API layer) must know about and coordinate ALL subsystems

class InventorySystem:
    def check_stock(self, restaurant_id, items):
        print(f"Checking stock at restaurant {restaurant_id}...")
        return True  # simplified

    def reserve_items(self, restaurant_id, items):
        print(f"Reserving items at restaurant {restaurant_id}...")

class PricingSystem:
    def calculate_total(self, items, coupon_code=None):
        subtotal = sum(item['price'] for item in items)
        gst = subtotal * 0.05
        delivery_fee = 30
        discount = 0
        if coupon_code == "FIRST50":
            discount = subtotal * 0.5
        total = subtotal + gst + delivery_fee - discount
        print(f"Total: Rs.{total:.2f}")
        return total

class PaymentSystem:
    def charge(self, user_id, amount, method):
        print(f"Charging Rs.{amount:.2f} via {method}")
        return {"transaction_id": "txn_123", "status": "success"}

class RestaurantNotifier:
    def send_to_kitchen(self, restaurant_id, order_details):
        print(f"Order sent to kitchen at restaurant {restaurant_id}")

class DeliverySystem:
    def assign_rider(self, restaurant_id, delivery_address):
        print(f"Assigning rider from restaurant {restaurant_id} to {delivery_address}")
        return {"rider_id": "rider_456", "eta": "30 mins"}

class NotificationService:
    def send_notification(self, user_id, message):
        print(f"Notification to {user_id}: {message}")

# The CLIENT must know about ALL these systems and call them in the RIGHT ORDER:
def place_order_without_facade(user_id, restaurant_id, items, coupon, payment_method, address):
    # 1. Check inventory
    inv = InventorySystem()
    if not inv.check_stock(restaurant_id, items):
        print("Items not available!")
        return

    # 2. Calculate price
    pricing = PricingSystem()
    total = pricing.calculate_total(items, coupon)

    # 3. Process payment
    payment = PaymentSystem()
    txn = payment.charge(user_id, total, payment_method)
    if txn["status"] != "success":
        print("Payment failed!")
        return

    # 4. Reserve items
    inv.reserve_items(restaurant_id, items)

    # 5. Notify restaurant
    notifier = RestaurantNotifier()
    notifier.send_to_kitchen(restaurant_id, items)

    # 6. Assign delivery
    delivery = DeliverySystem()
    rider = delivery.assign_rider(restaurant_id, address)

    # 7. Notify user
    notifications = NotificationService()
    notifications.send_notification(user_id, f"Order confirmed! ETA: {rider['eta']}")

# The client must know:
# - Which systems exist
# - What order to call them in
# - How to handle failures at each step
# - All the internal details of each system
```

#### The pattern — Clean code WITH Facade

```python
class OrderFacade:
    """
    The Facade hides ALL the complexity behind a single, simple method.
    The client just calls place_order() and everything is handled.
    """
    def __init__(self):
        # The facade knows about all the subsystems
        self._inventory = InventorySystem()
        self._pricing = PricingSystem()
        self._payment = PaymentSystem()
        self._restaurant = RestaurantNotifier()
        self._delivery = DeliverySystem()
        self._notifications = NotificationService()

    def place_order(self, user_id: str, restaurant_id: str, items: list,
                    coupon: str = None, payment_method: str = "upi",
                    delivery_address: str = "") -> dict:
        """
        ONE method that handles the entire order flow.
        The caller does not need to know about inventory, pricing,
        payment, delivery, or notification systems.
        """
        # Step 1: Check if items are available
        if not self._inventory.check_stock(restaurant_id, items):
            return {"status": "failed", "reason": "Items not available"}

        # Step 2: Calculate the total price
        total = self._pricing.calculate_total(items, coupon)

        # Step 3: Process payment
        transaction = self._payment.charge(user_id, total, payment_method)
        if transaction["status"] != "success":
            return {"status": "failed", "reason": "Payment failed"}

        # Step 4: Reserve items in inventory
        self._inventory.reserve_items(restaurant_id, items)

        # Step 5: Send order to restaurant kitchen
        self._restaurant.send_to_kitchen(restaurant_id, items)

        # Step 6: Assign delivery partner
        rider = self._delivery.assign_rider(restaurant_id, delivery_address)

        # Step 7: Send confirmation to user
        self._notifications.send_notification(
            user_id, f"Order confirmed! Rider {rider['rider_id']} assigned. ETA: {rider['eta']}"
        )

        return {
            "status": "success",
            "transaction_id": transaction["transaction_id"],
            "rider_id": rider["rider_id"],
            "eta": rider["eta"],
        }


# Usage — look how simple this is for the caller!
facade = OrderFacade()
result = facade.place_order(
    user_id="user_sheetal",
    restaurant_id="rest_123",
    items=[{"name": "Butter Chicken", "price": 350}, {"name": "Naan", "price": 50}],
    coupon="FIRST50",
    payment_method="upi",
    delivery_address="Andheri West, Mumbai"
)
print(f"\nOrder result: {result}")
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
1. The Logger class must be a Singleton — calling `Logger()` anywhere returns the same instance
2. It must have three methods:
   - `info(message)` — logs an informational message with timestamp prefix `[INFO]`
   - `warning(message)` — logs a warning with prefix `[WARNING]`
   - `error(message)` — logs an error with prefix `[ERROR]`
3. All log messages are stored in an internal list called `logs`
4. A method `get_logs()` returns all logs
5. A method `clear_logs()` empties the log list

**Test your solution:**
```python
logger1 = Logger()
logger2 = Logger()

logger1.info("Server started")
logger2.warning("High memory usage")
logger1.error("Database connection failed")

# Both should be the same instance:
assert logger1 is logger2, "Logger is not a Singleton!"

# All logs should be visible from either reference:
assert len(logger2.get_logs()) == 3
print(logger2.get_logs())
# Should show all 3 messages with timestamps and severity levels
```

---

### Exercise 2: Vehicle Factory — Ola/Uber Style (Estimated time: 25 minutes)

**What to build:**
When you open Ola, you see different ride options: Auto, Mini, Sedan, SUV, Bike. Each has different properties (fuel type, capacity, base fare). Build a Factory that creates the right vehicle based on the ride type.

**Requirements:**
1. Create a base class `Vehicle` with methods:
   - `start()` — prints a start message
   - `stop()` — prints a stop message
   - `fuel_type()` — returns the fuel type as a string
   - `capacity()` — returns number of passengers
   - `base_fare()` — returns base fare in rupees
2. Create concrete classes: `Auto`, `Bike`, `MiniCar`, `Sedan`, `SUV`
   - Auto: CNG fuel, 3 passengers, Rs.30 base fare
   - Bike: Petrol, 1 passenger, Rs.15 base fare
   - MiniCar: Petrol, 4 passengers, Rs.50 base fare
   - Sedan: Petrol/Diesel, 4 passengers, Rs.80 base fare
   - SUV: Diesel, 6 passengers, Rs.120 base fare
3. Create a `VehicleFactory` with a `create(ride_type)` method
4. Create a `calculate_fare(ride_type, distance_km)` function that uses the factory to create the vehicle and calculates fare as: `base_fare + (distance * 12)` for bikes, `base_fare + (distance * 15)` for others

**Test your solution:**
```python
vehicle = VehicleFactory.create("sedan")
vehicle.start()   # "Sedan started. Enjoy your comfortable ride!"
print(vehicle.fuel_type())  # "Petrol/Diesel"
print(vehicle.capacity())   # 4

fare = calculate_fare("auto", 5)  # 30 + (5 * 15) = 105
print(f"Auto fare for 5 km: Rs.{fare}")
```

---

### Exercise 3: Coffee Builder — Starbucks Style (Estimated time: 20 minutes)

**What to build:**
Build a coffee ordering system like CCD (Cafe Coffee Day) or Starbucks using the Builder pattern. A coffee order has many optional parts — you might want milk but no sugar, or extra sugar but no whipped cream.

**Requirements:**
1. Create a `Coffee` class with attributes: size, milk_type, sugar_spoons, flavor, whipped_cream, extra_shot, temperature
2. Create a `CoffeeBuilder` class with these chainable methods:
   - `size(size)` — "small", "medium", "large"
   - `milk(type)` — "regular", "oat", "almond", "soy", "none"
   - `sugar(spoons)` — number of spoons (0 = no sugar)
   - `flavor(name)` — "vanilla", "hazelnut", "caramel", "mocha", or None
   - `with_whipped_cream()` — adds whipped cream
   - `extra_shot()` — adds an extra espresso shot
   - `iced()` — makes it a cold coffee
   - `build()` — returns the final Coffee object
3. The `Coffee` class should have a `__str__` method that prints a readable description
4. Add a `price()` method on Coffee:
   - Small: Rs.150, Medium: Rs.200, Large: Rs.250
   - Each add-on costs: milk (free if regular, Rs.30 for alternatives), flavor Rs.20, whipped cream Rs.30, extra shot Rs.40

**Test your solution:**
```python
coffee = (CoffeeBuilder()
          .size("large")
          .milk("oat")
          .sugar(2)
          .flavor("hazelnut")
          .with_whipped_cream()
          .iced()
          .build())

print(coffee)
# Large iced coffee with oat milk, 2 sugar, hazelnut flavor, whipped cream
print(f"Price: Rs.{coffee.price()}")
# Price: Rs.330 (250 + 30 oat milk + 20 flavor + 30 whipped cream)

# Simple coffee — only required options
simple = (CoffeeBuilder()
          .size("small")
          .build())
print(simple)
# Small hot coffee, no milk, no sugar
print(f"Price: Rs.{simple.price()}")
# Price: Rs.150
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
