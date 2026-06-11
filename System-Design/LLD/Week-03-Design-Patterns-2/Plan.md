# LLD Week 3: Behavioral Design Patterns

## What are Behavioral Patterns?

In Week 2, you learned how to CREATE objects (Creational) and how to STRUCTURE/COMPOSE objects (Structural). This week is about how objects COMMUNICATE and DIVIDE RESPONSIBILITIES.

Think of it this way:
- **Creational** = How do I hire the right people? (Singleton, Factory, Builder)
- **Structural** = How do I organize the team? (Adapter, Decorator, Facade)
- **Behavioral** = How do the team members talk to each other and make decisions? (Strategy, Observer, Command, State)

Behavioral patterns answer questions like:
- "When the user's order status changes, who needs to be notified and how?" (Observer)
- "How do I let the system switch between different pricing algorithms at runtime?" (Strategy)
- "How do I implement undo/redo?" (Command)
- "How does an order's behavior change as it moves from 'placed' to 'preparing' to 'out for delivery'?" (State)

**Interview note:** Strategy and Observer are the two most-asked patterns at Microsoft. If you master only two behavioral patterns, master these two.

---

## Day 1: Core Behavioral Patterns (Saturday)

---

### Pattern 1: Strategy — "Choose the algorithm at runtime"

#### What is it?

The Strategy pattern defines a FAMILY of algorithms, puts each one in its own class, and lets you swap between them at runtime. The code that uses the algorithm does not know or care which specific algorithm it is running — it just calls a method, and the currently selected strategy handles it.

Imagine you are driving from Mumbai to Pune. You could:
- Take the expressway (fast, costs toll)
- Take the old highway (slower, free)
- Take a scenic route through Lonavala (slow, beautiful)

Your CAR does not change based on the route. The route (strategy) changes, and the car just follows whatever route you selected. You can switch routes midway if there is traffic.

#### Real-life analogy: Ola/Uber fare calculation

When Ola calculates your ride fare, the algorithm depends on factors that can change dynamically:
- **Normal hours:** Base fare + per-km rate + per-minute rate
- **Surge pricing (rain/peak hours):** Same formula but with a 1.5x to 2.5x multiplier
- **Subscription rides (Ola Pass):** Discounted flat rate per km
- **Shared rides (Ola Share):** Split the fare among passengers on the same route

The ride booking system does not have a massive if-elif-else block to handle all these. Instead, it selects a **pricing strategy** based on current conditions and applies it. When conditions change (surge starts or ends), it swaps the strategy. The rest of the code — booking, driver matching, ETA calculation — remains completely unchanged.

#### What problem does it solve? (The ugly code WITHOUT the pattern)

```python
# WITHOUT Strategy — The PROBLEM
# Imagine Flipkart's discount system

def calculate_price(original_price, discount_type, discount_value=None, cart_items=None):
    """
    This function handles ALL discount types with if-elif-else.
    Every time a new discount type is added, THIS function grows.
    """
    if discount_type == "none":
        return original_price

    elif discount_type == "percentage":
        # Diwali Sale: 20% off
        return original_price * (1 - discount_value / 100)

    elif discount_type == "flat":
        # Coupon: Rs.500 off
        return max(0, original_price - discount_value)

    elif discount_type == "bogo":
        # Buy One Get One Free
        if cart_items and len(cart_items) >= 2:
            cheapest = min(item['price'] for item in cart_items)
            return original_price - cheapest
        return original_price

    elif discount_type == "tiered":
        # Spend more, save more:
        #   Above 5000: 10% off
        #   Above 10000: 15% off
        #   Above 20000: 20% off
        if original_price > 20000:
            return original_price * 0.8
        elif original_price > 10000:
            return original_price * 0.85
        elif original_price > 5000:
            return original_price * 0.9
        return original_price

    elif discount_type == "first_order":
        # First order: 50% off up to Rs.150
        discount = min(original_price * 0.5, 150)
        return original_price - discount

    elif discount_type == "bank_offer":
        # HDFC Credit Card: 10% instant discount up to Rs.1500
        discount = min(original_price * 0.1, 1500)
        return original_price - discount

    # PROBLEM: This function is ALREADY 40+ lines long.
    # Every new sale event (Republic Day Sale, Big Billion Days,
    # Student Discount, Senior Citizen Discount, Employee Discount)
    # adds another elif block.
    #
    # After a year, this function will be 200+ lines with 20+ conditions.
    # Testing it is a nightmare — you must test ALL paths.
    # Two developers working on different discounts will get merge conflicts.
    # One typo in the "tiered" block could break the "bogo" block.

    else:
        raise ValueError(f"Unknown discount type: {discount_type}")
```

**What goes wrong:**
1. **Open/Closed Principle violation** — To add a new discount, you MODIFY existing code (instead of ADDING new code). Modifying existing code risks breaking things that already work.
2. **Single Responsibility violation** — One function knows about ALL discount algorithms. If the BOGO logic has a bug, you must read past 10 other algorithms to find it.
3. **Testing nightmare** — You must write tests for the entire function, including all paths. If you add a new path, you risk breaking existing tests.
4. **Merge conflicts** — If Developer A adds "Student Discount" and Developer B adds "Employee Discount" at the same time, they both edit the same function and get a merge conflict.

#### The pattern — Clean code WITH Strategy

```python
from abc import ABC, abstractmethod

# Step 1: Define the Strategy interface (the contract)
# Every discount strategy MUST implement calculate().
# This ensures the rest of the code can treat ALL strategies the same way.
class DiscountStrategy(ABC):
    @abstractmethod
    def calculate(self, original_price: float, **kwargs) -> float:
        """
        Given an original price, return the discounted price.
        **kwargs allows each strategy to accept its own specific parameters.
        """
        pass

# Step 2: Implement each strategy as its own class
# Each class encapsulates ONE algorithm. It is small, focused, and testable.

class NoDiscount(DiscountStrategy):
    """Full price — no discount applied."""
    def calculate(self, original_price: float, **kwargs) -> float:
        return original_price

class PercentageDiscount(DiscountStrategy):
    """
    A percentage off the original price.
    Example: Diwali Sale — 20% off everything
    """
    def __init__(self, percentage: float):
        # Store the percentage when the strategy is created.
        # This means you can have PercentageDiscount(20) for 20% off
        # and PercentageDiscount(30) for 30% off — same class, different config.
        self.percentage = percentage

    def calculate(self, original_price: float, **kwargs) -> float:
        discount_amount = original_price * (self.percentage / 100)
        return original_price - discount_amount

class FlatDiscount(DiscountStrategy):
    """
    A fixed amount off the price.
    Example: Apply coupon FLAT500 — Rs.500 off
    """
    def __init__(self, amount: float):
        self.amount = amount

    def calculate(self, original_price: float, **kwargs) -> float:
        # Ensure price does not go below zero
        return max(0, original_price - self.amount)

class BuyOneGetOneFree(DiscountStrategy):
    """
    Buy one get one free — deducts the cheapest item's price.
    Example: Flipkart BOGO on fashion items
    """
    def calculate(self, original_price: float, **kwargs) -> float:
        cart_items = kwargs.get('cart_items', [])
        if cart_items and len(cart_items) >= 2:
            cheapest = min(item['price'] for item in cart_items)
            return original_price - cheapest
        return original_price

class TieredDiscount(DiscountStrategy):
    """
    Spend more, save more.
    Example: Big Billion Days — 10% off above 5000, 15% off above 10000, 20% off above 20000
    """
    def __init__(self, tiers: list):
        """
        tiers is a list of (threshold, percentage) tuples, sorted descending.
        Example: [(20000, 20), (10000, 15), (5000, 10)]
        """
        # Sort by threshold descending so we check highest first
        self.tiers = sorted(tiers, key=lambda t: t[0], reverse=True)

    def calculate(self, original_price: float, **kwargs) -> float:
        for threshold, percentage in self.tiers:
            if original_price > threshold:
                return original_price * (1 - percentage / 100)
        return original_price  # No tier matched — no discount

class FirstOrderDiscount(DiscountStrategy):
    """
    New customer discount — 50% off, capped at Rs.150.
    Example: Swiggy's first order discount
    """
    def __init__(self, percentage: float = 50, max_discount: float = 150):
        self.percentage = percentage
        self.max_discount = max_discount

    def calculate(self, original_price: float, **kwargs) -> float:
        discount = min(original_price * (self.percentage / 100), self.max_discount)
        return original_price - discount

# Step 3: The Context — the class that USES a strategy
# The PricingEngine does not know WHICH strategy it is using.
# It just knows it HAS a strategy, and it can call calculate() on it.
class PricingEngine:
    def __init__(self, strategy: DiscountStrategy):
        """Create a pricing engine with an initial strategy."""
        self._strategy = strategy

    def set_strategy(self, strategy: DiscountStrategy):
        """
        Switch the strategy at RUNTIME.
        Example: Normal hours use NoDiscount, but when a sale starts,
        switch to PercentageDiscount(20) without restarting the app.
        """
        self._strategy = strategy

    def calculate_final_price(self, original_price: float, **kwargs) -> float:
        """Delegate to whatever strategy is currently set."""
        final = self._strategy.calculate(original_price, **kwargs)
        return round(final, 2)


# Step 4: Usage — see how clean and flexible this is!

# Scenario 1: Regular day, no sale
engine = PricingEngine(NoDiscount())
print(f"Regular price: Rs.{engine.calculate_final_price(2000)}")
# Regular price: Rs.2000

# Scenario 2: Diwali Sale starts — just swap the strategy!
engine.set_strategy(PercentageDiscount(20))
print(f"Diwali sale price: Rs.{engine.calculate_final_price(2000)}")
# Diwali sale price: Rs.1600.0

# Scenario 3: Customer applies a coupon
engine.set_strategy(FlatDiscount(500))
print(f"Coupon price: Rs.{engine.calculate_final_price(2000)}")
# Coupon price: Rs.1500

# Scenario 4: Big Billion Days tiered discount
big_billion = TieredDiscount([(20000, 20), (10000, 15), (5000, 10)])
engine.set_strategy(big_billion)
print(f"Rs.25000 item during BBD: Rs.{engine.calculate_final_price(25000)}")
# Rs.25000 item during BBD: Rs.20000.0
print(f"Rs.7000 item during BBD: Rs.{engine.calculate_final_price(7000)}")
# Rs.7000 item during BBD: Rs.6300.0

# Scenario 5: New customer first order
engine.set_strategy(FirstOrderDiscount())
print(f"First order price for Rs.400: Rs.{engine.calculate_final_price(400)}")
# First order price for Rs.400: Rs.250.0 (50% of 400 = 200, capped at 150, so 400-150=250)

# Scenario 6: BOGO
engine.set_strategy(BuyOneGetOneFree())
cart = [{"name": "T-Shirt", "price": 800}, {"name": "Socks", "price": 200}]
total = sum(item['price'] for item in cart)
print(f"BOGO price: Rs.{engine.calculate_final_price(total, cart_items=cart)}")
# BOGO price: Rs.800.0 (cheapest item = socks Rs.200 is free)
```

**Why this is better:**

| Concern | Without Strategy | With Strategy |
|---------|-----------------|---------------|
| Adding a new discount | Modify the big function, risk breaking others | Create a new class, zero risk to existing code |
| Testing a specific discount | Test the entire function | Test only the one class |
| Two devs adding different discounts | Merge conflict (same file/function) | No conflict (different files) |
| Reading the code | Scroll through 200 lines of if-elif | Open the one 10-line class you need |
| Runtime flexibility | Must redeploy to change discount logic | Swap strategy at runtime with set_strategy() |

#### When to use Strategy

- When you have multiple algorithms that do the same thing differently (pricing, sorting, routing, compression, validation)
- When you need to switch algorithms at runtime based on context (time of day, user type, A/B test group)
- When you see a growing if-elif-else or switch-case block choosing between algorithms

**The trigger question:** "Am I choosing between different ways to do the same thing?" If yes, use Strategy.

#### When NOT to use Strategy

- If there are only 2 simple options that will never grow, a simple if-else is fine. Creating 3 classes for a binary choice is overkill.
- If the algorithm never changes at runtime and there is only one implementation, you do not need the abstraction.
- If the algorithms are tightly coupled to the context's internal state and need access to many private fields, Strategy (which promotes separation) may feel forced.

---

### Pattern 2: Observer — "When one thing changes, notify all interested parties"

#### What is it?

The Observer pattern defines a one-to-many relationship: when ONE object (the subject/publisher) changes state, ALL dependent objects (observers/subscribers) are notified automatically. The subject does not know or care who the observers are or what they do — it just says "something happened" and the observers react.

Think of it as subscribing to a YouTube channel. When the YouTuber uploads a new video (state change), all subscribers get notified. The YouTuber does not individually message each subscriber — YouTube's notification system handles it. The YouTuber does not even know how many subscribers there are or what they do with the notification (some watch immediately, some save for later, some ignore it).

#### Real-life analogy: Zomato order tracking

When your Zomato order status changes from "Preparing" to "Out for Delivery," multiple things happen simultaneously:
1. **Your phone** gets a push notification: "Your rider is on the way!"
2. **The live tracking page** updates to show the rider's location on the map
3. **The restaurant dashboard** marks the order as "Picked Up"
4. **The delivery partner's app** starts showing navigation to your address
5. **The analytics system** records the timestamp for delivery time calculation
6. **The ETA estimator** recalculates and updates the estimated arrival time

The order management system does NOT have hardcoded calls to each of these systems. Instead, it PUBLISHES an event: "Order #123 status changed to OUT_FOR_DELIVERY." All the systems that care about this event are SUBSCRIBED and react independently.

**Why is this important?** Because if you add a new feature next month (say, a loyalty points system that awards points when the order is delivered), you just SUBSCRIBE the loyalty system to the event. You do NOT modify the order management code at all.

#### What problem does it solve? (The ugly code WITHOUT the pattern)

```python
# WITHOUT Observer — The PROBLEM
# The order system must KNOW about every system that cares about order events

class OrderService:
    def __init__(self):
        # The order service must hold references to EVERY dependent system
        self.push_notification = PushNotificationService()
        self.email_service = EmailService()
        self.sms_service = SMSService()
        self.tracking_service = TrackingService()
        self.restaurant_dashboard = RestaurantDashboard()
        self.analytics = AnalyticsService()
        self.eta_calculator = ETACalculator()

    def update_order_status(self, order_id, new_status):
        # Update the order in the database
        print(f"Order {order_id} -> {new_status}")

        # NOW: manually call EVERY system that might care
        if new_status == "preparing":
            self.push_notification.send(f"Order {order_id} is being prepared")
            self.restaurant_dashboard.update(order_id, "preparing")
            self.analytics.log(order_id, "preparation_started")

        elif new_status == "out_for_delivery":
            self.push_notification.send(f"Rider is on the way for order {order_id}")
            self.sms_service.send(f"Your order {order_id} is out for delivery")
            self.tracking_service.start_tracking(order_id)
            self.restaurant_dashboard.update(order_id, "picked_up")
            self.analytics.log(order_id, "picked_up")
            self.eta_calculator.recalculate(order_id)

        elif new_status == "delivered":
            self.push_notification.send(f"Order {order_id} delivered!")
            self.email_service.send_receipt(order_id)
            self.tracking_service.stop_tracking(order_id)
            self.restaurant_dashboard.update(order_id, "completed")
            self.analytics.log(order_id, "delivered")
            # OOPS! Forgot to add loyalty_service.award_points() here
            # And forgot to add feedback_service.request_rating() here
            # Easy to forget when you must manually add each call!

# PROBLEMS:
# 1. OrderService is tightly coupled to 7+ services
# 2. Adding a new subscriber means MODIFYING OrderService
# 3. Easy to forget to add a new service to every relevant status change
# 4. Cannot dynamically subscribe/unsubscribe at runtime
# 5. Testing OrderService requires mocking all 7 services
# 6. If the analytics service is down, it might crash the order update!
```

#### The pattern — Clean code WITH Observer

```python
from abc import ABC, abstractmethod
from datetime import datetime

# Step 1: Define what an "observer" looks like (the subscriber interface)
# Any class that wants to be notified must implement this method.
class OrderObserver(ABC):
    @abstractmethod
    def on_order_update(self, order_id: str, old_status: str, new_status: str, timestamp: datetime):
        """Called when an order's status changes."""
        pass

# Step 2: Define the "subject" (the publisher) — the EventManager
# This is the backbone of the Observer pattern. It maintains a list of
# subscribers and notifies them when events occur.
class OrderEventManager:
    def __init__(self):
        # Dictionary: event_name -> list of observers
        # This allows different observers to subscribe to different events.
        # Example: PushNotification subscribes to ALL events,
        #          but TrackingService only subscribes to "out_for_delivery"
        self._listeners = {}

    def subscribe(self, event: str, observer: OrderObserver):
        """
        Register an observer for a specific event.
        Example: manager.subscribe("delivered", feedback_service)
        """
        if event not in self._listeners:
            self._listeners[event] = []
        if observer not in self._listeners[event]:  # prevent duplicate subscriptions
            self._listeners[event].append(observer)
            print(f"  [EventManager] {observer.__class__.__name__} subscribed to '{event}'")

    def unsubscribe(self, event: str, observer: OrderObserver):
        """
        Remove an observer from a specific event.
        Useful for: temporarily disabling a service, A/B tests, etc.
        """
        if event in self._listeners:
            self._listeners[event].remove(observer)

    def notify(self, event: str, order_id: str, old_status: str, new_status: str):
        """
        Notify ALL observers subscribed to this event.
        The subject does NOT know who the observers are or what they do.
        It just calls on_order_update() on each one.
        """
        timestamp = datetime.now()
        listeners = self._listeners.get(event, [])
        for listener in listeners:
            try:
                listener.on_order_update(order_id, old_status, new_status, timestamp)
            except Exception as e:
                # IMPORTANT: One failing observer should NOT crash the whole system.
                # If analytics is down, the user should still get their notification.
                print(f"  [EventManager] Error notifying {listener.__class__.__name__}: {e}")

# Step 3: Concrete observers — each handles the event in its own way

class PushNotificationObserver(OrderObserver):
    """Sends push notifications to the customer's phone."""
    MESSAGES = {
        "preparing": "Your order is being prepared by the restaurant!",
        "out_for_delivery": "Your rider is on the way! Track live on the app.",
        "delivered": "Your order has been delivered. Enjoy your meal!",
    }

    def on_order_update(self, order_id, old_status, new_status, timestamp):
        message = self.MESSAGES.get(new_status, f"Order {order_id} status: {new_status}")
        print(f"  [PUSH] {message}")

class SMSObserver(OrderObserver):
    """Sends SMS for critical updates (out for delivery, delivered)."""
    def on_order_update(self, order_id, old_status, new_status, timestamp):
        print(f"  [SMS] Order {order_id}: Status changed to {new_status}")

class EmailObserver(OrderObserver):
    """Sends email receipt when order is delivered."""
    def on_order_update(self, order_id, old_status, new_status, timestamp):
        print(f"  [EMAIL] Sending receipt for order {order_id}")

class RestaurantDashboardObserver(OrderObserver):
    """Updates the restaurant's kitchen display system."""
    def on_order_update(self, order_id, old_status, new_status, timestamp):
        print(f"  [RESTAURANT] Dashboard updated: Order {order_id} -> {new_status}")

class AnalyticsObserver(OrderObserver):
    """Records order events for analytics and reporting."""
    def __init__(self):
        self.events = []

    def on_order_update(self, order_id, old_status, new_status, timestamp):
        event = {
            "order_id": order_id,
            "from": old_status,
            "to": new_status,
            "timestamp": timestamp.isoformat(),
        }
        self.events.append(event)
        print(f"  [ANALYTICS] Recorded: {old_status} -> {new_status} for order {order_id}")

class LiveTrackingObserver(OrderObserver):
    """Starts/stops live GPS tracking of the delivery rider."""
    def on_order_update(self, order_id, old_status, new_status, timestamp):
        if new_status == "out_for_delivery":
            print(f"  [TRACKING] Started live tracking for order {order_id}")
        elif new_status == "delivered":
            print(f"  [TRACKING] Stopped live tracking for order {order_id}")

class LoyaltyPointsObserver(OrderObserver):
    """Awards loyalty points when order is delivered.
    This was added MONTHS after the original system —
    required ZERO changes to OrderService!"""
    def on_order_update(self, order_id, old_status, new_status, timestamp):
        print(f"  [LOYALTY] Awarded 50 points for order {order_id}")

# Step 4: The OrderService — now CLEAN and decoupled
class OrderService:
    def __init__(self, event_manager: OrderEventManager):
        # The order service only knows about the event manager.
        # It does NOT know about push notifications, SMS, analytics, etc.
        self._event_manager = event_manager
        self._orders = {}

    def update_status(self, order_id: str, new_status: str):
        old_status = self._orders.get(order_id, "placed")
        self._orders[order_id] = new_status
        print(f"\nOrder {order_id}: {old_status} -> {new_status}")
        # Just publish the event — let observers handle the rest
        self._event_manager.notify(new_status, order_id, old_status, new_status)


# Step 5: Wire it all together

# Create the event manager
events = OrderEventManager()

# Create observers
push = PushNotificationObserver()
sms = SMSObserver()
email = EmailObserver()
restaurant = RestaurantDashboardObserver()
analytics = AnalyticsObserver()
tracking = LiveTrackingObserver()
loyalty = LoyaltyPointsObserver()

# Subscribe each observer to the events they care about
# (not every observer cares about every event)
events.subscribe("preparing", push)
events.subscribe("preparing", restaurant)
events.subscribe("preparing", analytics)

events.subscribe("out_for_delivery", push)
events.subscribe("out_for_delivery", sms)
events.subscribe("out_for_delivery", restaurant)
events.subscribe("out_for_delivery", tracking)
events.subscribe("out_for_delivery", analytics)

events.subscribe("delivered", push)
events.subscribe("delivered", sms)
events.subscribe("delivered", email)
events.subscribe("delivered", restaurant)
events.subscribe("delivered", tracking)
events.subscribe("delivered", analytics)
events.subscribe("delivered", loyalty)  # Added later — no code change to OrderService!

# Create the order service
order_service = OrderService(events)

# Simulate an order going through its lifecycle:
print("=" * 60)
order_service.update_status("ORD-001", "preparing")
# Order ORD-001: placed -> preparing
#   [PUSH] Your order is being prepared by the restaurant!
#   [RESTAURANT] Dashboard updated: Order ORD-001 -> preparing
#   [ANALYTICS] Recorded: placed -> preparing for order ORD-001

print("=" * 60)
order_service.update_status("ORD-001", "out_for_delivery")
# Order ORD-001: preparing -> out_for_delivery
#   [PUSH] Your rider is on the way! Track live on the app.
#   [SMS] Order ORD-001: Status changed to out_for_delivery
#   [RESTAURANT] Dashboard updated: Order ORD-001 -> out_for_delivery
#   [TRACKING] Started live tracking for order ORD-001
#   [ANALYTICS] Recorded: preparing -> out_for_delivery for order ORD-001

print("=" * 60)
order_service.update_status("ORD-001", "delivered")
# Order ORD-001: out_for_delivery -> delivered
#   [PUSH] Your order has been delivered. Enjoy your meal!
#   [SMS] Order ORD-001: Status changed to delivered
#   [EMAIL] Sending receipt for order ORD-001
#   [RESTAURANT] Dashboard updated: Order ORD-001 -> delivered
#   [TRACKING] Stopped live tracking for order ORD-001
#   [ANALYTICS] Recorded: out_for_delivery -> delivered for order ORD-001
#   [LOYALTY] Awarded 50 points for order ORD-001
```

**The key insight:** The `OrderService` has NO imports, NO references, and NO knowledge of push notifications, SMS, email, analytics, tracking, or loyalty points. It just publishes events. The event manager and observers handle everything else. This means:
- Adding a new notification channel = create a new observer class, subscribe it. Zero changes to OrderService.
- Removing a channel temporarily = unsubscribe it. Zero changes to OrderService.
- Testing OrderService = mock the event manager. No need to mock 7 services.

#### When to use Observer

- Event-driven systems (order events, payment events, user activity events)
- UI frameworks (React's state management, Android's LiveData)
- Real-time notifications and alerts
- Pub/sub messaging systems
- Any situation where one change should trigger multiple reactions

**The trigger question:** "When this thing changes, should multiple other things react — and I do not want the source to know about all the reactors?" If yes, use Observer.

#### When NOT to use Observer

- If there is only ONE observer and it will always be the same one, direct method calls are simpler
- If the order of notification matters critically (Observer does not guarantee order), you need a different approach
- Be careful of "event storms" — one event triggering an observer that triggers another event, creating a cascade. Design event flows carefully.
- Memory leaks: if observers subscribe but never unsubscribe (especially in UI apps), they remain in memory forever

---

### Pattern 3: Command — "Wrap actions as objects (with undo!)"

#### What is it?

The Command pattern turns a REQUEST (an action, an operation) into a standalone object. This object contains everything needed to perform the action AND to undo it. By storing these command objects in a list, you get undo/redo functionality for free.

Think of it as a restaurant order slip. When you tell the waiter "one butter chicken and two naan," the waiter writes it on a slip (creates a command object). The slip contains all the details needed to execute the order. If you change your mind, the waiter tears up the slip (undo). The cook does not know who ordered it — they just execute whatever slips come in.

#### Real-life analogy: Google Docs editing

When you type in Google Docs, every keystroke is a Command:
1. You type "Hello" — that is an `InsertTextCommand("Hello", position=0)`
2. You make it bold — that is a `FormatTextCommand("bold", range=0:5)`
3. You press Ctrl+Z (undo) — the last command (`FormatTextCommand`) reverses itself, removing the bold
4. You press Ctrl+Z again — the `InsertTextCommand` reverses itself, deleting "Hello"
5. You press Ctrl+Y (redo) — the `InsertTextCommand` re-executes, typing "Hello" again

Each action is stored as an object that knows how to DO itself and UNDO itself. Google Docs literally keeps a list of every command since you opened the document.

**This pattern is also used in:**
- PayTM/GPay transaction reversal (each transaction is a command that can be reversed)
- Photoshop's history panel (each edit is a command)
- Git commits (each commit is a command with diff and reverse-diff)

#### What problem does it solve? (The ugly code WITHOUT the pattern)

```python
# WITHOUT Command — The PROBLEM
# A text editor where undo is implemented with manual state tracking

class TextEditorBroken:
    def __init__(self):
        self.content = ""
        # To support undo, you would need to save the ENTIRE content
        # before every operation. For a 100MB document, this is insane.
        self._snapshots = []

    def type_text(self, text):
        self._snapshots.append(self.content)  # Save entire state!
        self.content += text

    def delete_last_n(self, n):
        self._snapshots.append(self.content)  # Save entire state again!
        self.content = self.content[:-n]

    def replace(self, old, new):
        self._snapshots.append(self.content)  # And again!
        self.content = self.content.replace(old, new)

    def undo(self):
        if self._snapshots:
            self.content = self._snapshots.pop()  # Restore ENTIRE content

# PROBLEMS:
# 1. Memory: Saving the entire document for every keystroke is extremely wasteful.
#    A 100-page document with 10,000 edits = 10,000 full copies in memory.
# 2. No redo: Once you undo, the forward history is lost.
# 3. No selective undo: You cannot undo just the "replace" operation while keeping
#    the "type" operations. It is all-or-nothing.
# 4. No batching: You cannot group multiple operations into one "undo unit."
```

#### The pattern — Clean code WITH Command

```python
from abc import ABC, abstractmethod

# Step 1: Define the Command interface
# Every command must know how to EXECUTE itself and UNDO itself.
class Command(ABC):
    @abstractmethod
    def execute(self):
        """Perform the action."""
        pass

    @abstractmethod
    def undo(self):
        """Reverse the action — restore to the state before execute()."""
        pass

    @abstractmethod
    def description(self) -> str:
        """Human-readable description for the history panel."""
        pass

# Step 2: Concrete commands — each knows ONE operation

class InsertTextCommand(Command):
    """Inserts text at a specific position in the document."""
    def __init__(self, editor, text: str, position: int):
        self._editor = editor      # Reference to the editor we are modifying
        self._text = text           # The text to insert
        self._position = position   # Where to insert it

    def execute(self):
        # Insert text at the specified position
        content = self._editor.content
        self._editor.content = (
            content[:self._position] + self._text + content[self._position:]
        )

    def undo(self):
        # Remove the text we inserted
        # We know exactly where it was and how long it was,
        # so we can surgically remove it (no need to save the whole document!)
        content = self._editor.content
        self._editor.content = (
            content[:self._position] + content[self._position + len(self._text):]
        )

    def description(self):
        preview = self._text[:20] + "..." if len(self._text) > 20 else self._text
        return f"Insert '{preview}' at position {self._position}"

class DeleteTextCommand(Command):
    """Deletes text from a specific position."""
    def __init__(self, editor, position: int, length: int):
        self._editor = editor
        self._position = position
        self._length = length
        self._deleted_text = ""  # We will store what was deleted (for undo)

    def execute(self):
        content = self._editor.content
        # Save what we are about to delete (so we can restore it on undo)
        self._deleted_text = content[self._position:self._position + self._length]
        # Perform the deletion
        self._editor.content = (
            content[:self._position] + content[self._position + self._length:]
        )

    def undo(self):
        # Re-insert the deleted text at its original position
        content = self._editor.content
        self._editor.content = (
            content[:self._position] + self._deleted_text + content[self._position:]
        )

    def description(self):
        return f"Delete {self._length} chars at position {self._position}"

class ReplaceTextCommand(Command):
    """Replaces all occurrences of old_text with new_text."""
    def __init__(self, editor, old_text: str, new_text: str):
        self._editor = editor
        self._old_text = old_text
        self._new_text = new_text
        self._original_content = ""  # Save for undo (replace is complex)

    def execute(self):
        self._original_content = self._editor.content
        self._editor.content = self._editor.content.replace(self._old_text, self._new_text)

    def undo(self):
        # For replace, we saved the original content because
        # "find and replace" can affect multiple locations
        self._editor.content = self._original_content

    def description(self):
        return f"Replace '{self._old_text}' with '{self._new_text}'"

# Step 3: The TextEditor — manages the command history

class TextEditor:
    def __init__(self):
        self.content = ""
        self._history = []        # Stack of executed commands (for undo)
        self._redo_stack = []     # Stack of undone commands (for redo)

    def execute_command(self, command: Command):
        """Execute a command and add it to the history."""
        command.execute()
        self._history.append(command)
        # When a new command is executed, the redo stack is cleared.
        # (You cannot redo commands from a "different timeline.")
        self._redo_stack.clear()

    def undo(self):
        """Undo the last command."""
        if not self._history:
            print("Nothing to undo!")
            return
        command = self._history.pop()
        command.undo()
        self._redo_stack.append(command)  # Move to redo stack
        print(f"Undid: {command.description()}")

    def redo(self):
        """Redo the last undone command."""
        if not self._redo_stack:
            print("Nothing to redo!")
            return
        command = self._redo_stack.pop()
        command.execute()
        self._history.append(command)  # Move back to history
        print(f"Redid: {command.description()}")

    def show_history(self):
        """Show the edit history (like Photoshop's History panel)."""
        print("\n--- Edit History ---")
        for i, cmd in enumerate(self._history, 1):
            print(f"  {i}. {cmd.description()}")
        if not self._history:
            print("  (empty)")
        print("---")

    # Convenience methods that create and execute commands:
    def type_text(self, text: str):
        """Type text at the end of the document."""
        cmd = InsertTextCommand(self, text, len(self.content))
        self.execute_command(cmd)

    def insert_at(self, text: str, position: int):
        """Insert text at a specific position."""
        cmd = InsertTextCommand(self, text, position)
        self.execute_command(cmd)

    def delete(self, position: int, length: int):
        """Delete text at a specific position."""
        cmd = DeleteTextCommand(self, position, length)
        self.execute_command(cmd)

    def replace(self, old_text: str, new_text: str):
        """Replace all occurrences of old_text with new_text."""
        cmd = ReplaceTextCommand(self, old_text, new_text)
        self.execute_command(cmd)


# Step 4: Usage — see the full undo/redo in action

editor = TextEditor()

# Type some text
editor.type_text("Hello World")
print(f'Content: "{editor.content}"')      # "Hello World"

# Type more
editor.type_text("! Welcome to India")
print(f'Content: "{editor.content}"')      # "Hello World! Welcome to India"

# Replace a word
editor.replace("World", "Sheetal")
print(f'Content: "{editor.content}"')      # "Hello Sheetal! Welcome to India"

# Show history
editor.show_history()
# --- Edit History ---
#   1. Insert 'Hello World' at position 0
#   2. Insert '! Welcome to India' at position 11
#   3. Replace 'World' with 'Sheetal'
# ---

# Undo the replace
editor.undo()
print(f'Content: "{editor.content}"')      # "Hello World! Welcome to India"
# Undid: Replace 'World' with 'Sheetal'

# Undo the second insert
editor.undo()
print(f'Content: "{editor.content}"')      # "Hello World"
# Undid: Insert '! Welcome to India' at position 11

# Redo!
editor.redo()
print(f'Content: "{editor.content}"')      # "Hello World! Welcome to India"
# Redid: Insert '! Welcome to India' at position 11
```

**Memory comparison:**

| Approach | Memory for 10,000 edits on a 100KB document |
|----------|----------------------------------------------|
| Saving full snapshots (old way) | 10,000 x 100KB = ~1 GB |
| Command pattern (storing operations) | 10,000 x ~100 bytes = ~1 MB |

The Command pattern stores the OPERATION (what to do and what to undo), not the ENTIRE STATE. This is roughly 1000x more memory-efficient.

#### When to use Command

- Undo/Redo functionality (text editors, drawing apps, form builders)
- Transaction systems (banking operations that might need reversal)
- Task queuing (background job systems where jobs are queued as command objects)
- Macro recording (recording a sequence of commands to replay later)
- Audit logging (each command is a record of what happened)

**The trigger question:** "Do I need to undo actions, queue them, log them, or replay them?" If yes, use Command.

#### When NOT to use Command

- If actions are fire-and-forget (no undo needed, no queuing, no logging), commands add complexity for nothing
- Simple CRUD operations that do not need undo are fine without commands
- If the "undo" of an action is trivial or impossible (e.g., sending an email cannot be unsent), the pattern may not apply fully

---

## Day 2: More Patterns + Practice (Sunday)

---

### Pattern 4: State — "Object changes its behavior based on its internal state"

#### What is it?

The State pattern allows an object to change its behavior when its internal state changes. It looks as if the object changed its class. Each state is represented by a separate class, and the object delegates behavior to its current state object.

Think of how you behave differently at different times of day. In the morning, "alarm rings" makes you wake up. During the day, "alarm rings" makes you check a reminder. At night, "alarm rings" makes you turn it off and stay in bed. Same trigger ("alarm rings"), different behavior based on your current STATE (morning/day/night).

#### Real-life analogy: Ola ride lifecycle

An Ola ride goes through distinct states, and the same actions produce different results depending on the state:

**State: SEARCHING**
- Customer taps "Cancel" -> Ride cancelled, no charge
- Driver found -> Move to DRIVER_ASSIGNED state

**State: DRIVER_ASSIGNED**
- Customer taps "Cancel" -> Ride cancelled, Rs.50 cancellation fee
- Driver arrives -> Move to WAITING state
- Driver cancels -> Back to SEARCHING state

**State: WAITING** (driver waiting at pickup)
- Customer enters car -> Move to IN_RIDE state
- 5 minutes pass -> Waiting charge starts (Rs.2/min)
- Customer cancels -> Ride cancelled, Rs.100 cancellation fee

**State: IN_RIDE**
- Customer taps "Cancel" -> NOT ALLOWED (you are in the car!)
- Destination reached -> Move to COMPLETED state
- Emergency button -> Move to EMERGENCY state

**State: COMPLETED**
- Customer taps "Cancel" -> NOT ALLOWED (ride is over!)
- Customer rates -> Rating recorded
- Payment processed -> Transaction completed

Notice how "Cancel" does something different in EVERY state. Without the State pattern, you would have if-elif blocks checking the current state everywhere.

#### What problem does it solve? (The ugly code WITHOUT the pattern)

```python
# WITHOUT State — The PROBLEM
# A vending machine with state-dependent behavior

class VendingMachineBroken:
    def __init__(self):
        self.state = "idle"  # States: idle, has_coin, dispensing, out_of_stock
        self.stock = {"chips": 5, "cola": 3, "water": 10}
        self.inserted_amount = 0

    def insert_coin(self, amount):
        # Every method must check the state with if-elif
        if self.state == "idle":
            self.inserted_amount = amount
            self.state = "has_coin"
            print(f"Rs.{amount} inserted")
        elif self.state == "has_coin":
            self.inserted_amount += amount
            print(f"Rs.{amount} more inserted. Total: Rs.{self.inserted_amount}")
        elif self.state == "dispensing":
            print("Please wait, dispensing in progress")
        elif self.state == "out_of_stock":
            print("Machine is out of stock. Returning your money.")
            # return coins

    def select_item(self, item):
        # SAME if-elif pattern for EVERY method!
        if self.state == "idle":
            print("Please insert a coin first")
        elif self.state == "has_coin":
            if item not in self.stock or self.stock[item] <= 0:
                print(f"{item} is out of stock")
                return
            price = {"chips": 20, "cola": 30, "water": 10}[item]
            if self.inserted_amount >= price:
                self.state = "dispensing"
                self.stock[item] -= 1
                change = self.inserted_amount - price
                print(f"Dispensing {item}...")
                if change > 0:
                    print(f"Returning change: Rs.{change}")
                self.inserted_amount = 0
                self.state = "idle"
            else:
                print(f"Insufficient amount. {item} costs Rs.{price}, you inserted Rs.{self.inserted_amount}")
        elif self.state == "dispensing":
            print("Please wait, dispensing in progress")
        elif self.state == "out_of_stock":
            print("Machine is out of stock")

    def cancel(self):
        # AGAIN — same if-elif!
        if self.state == "idle":
            print("Nothing to cancel")
        elif self.state == "has_coin":
            print(f"Returning Rs.{self.inserted_amount}")
            self.inserted_amount = 0
            self.state = "idle"
        elif self.state == "dispensing":
            print("Cannot cancel — dispensing in progress")
        elif self.state == "out_of_stock":
            print("Nothing to cancel")

# PROBLEMS:
# 1. Every method has the SAME if-elif-else block checking self.state
# 2. Adding a new state (e.g., "maintenance") requires modifying EVERY method
# 3. It is easy to forget to handle a state in a new method
# 4. The logic for each state is SCATTERED across all methods instead of being in one place
# 5. As states and actions grow, this becomes a massive matrix of conditions
```

#### The pattern — Clean code WITH State

```python
from abc import ABC, abstractmethod

# Step 1: Define the State interface
# Every state class must implement all possible actions.
# This ensures that when you add a new state, the type checker forces you
# to handle every action — no forgotten cases.
class VendingMachineState(ABC):
    @abstractmethod
    def insert_coin(self, machine, amount: int):
        pass

    @abstractmethod
    def select_item(self, machine, item: str):
        pass

    @abstractmethod
    def cancel(self, machine):
        pass

    @abstractmethod
    def state_name(self) -> str:
        pass

# Step 2: Implement each state as its own class
# ALL logic for a given state lives in ONE class.
# Reading the code, you can see exactly what happens in each state.

class IdleState(VendingMachineState):
    """Machine is waiting for a customer. No coins inserted."""

    def insert_coin(self, machine, amount):
        machine.inserted_amount = amount
        print(f"Rs.{amount} inserted. Select an item.")
        # Transition to the next state
        machine.set_state(HasCoinState())

    def select_item(self, machine, item):
        print("Please insert a coin first!")

    def cancel(self, machine):
        print("Nothing to cancel — machine is idle.")

    def state_name(self):
        return "IDLE"

class HasCoinState(VendingMachineState):
    """Customer has inserted a coin. Waiting for item selection."""

    def insert_coin(self, machine, amount):
        machine.inserted_amount += amount
        print(f"Rs.{amount} more inserted. Total: Rs.{machine.inserted_amount}")

    def select_item(self, machine, item):
        prices = {"chips": 20, "cola": 30, "water": 10}

        if item not in prices:
            print(f"Unknown item: {item}")
            return

        if machine.stock.get(item, 0) <= 0:
            print(f"{item} is out of stock! Choose something else.")
            return

        price = prices[item]
        if machine.inserted_amount < price:
            print(f"{item} costs Rs.{price}, you have Rs.{machine.inserted_amount}. Insert Rs.{price - machine.inserted_amount} more.")
            return

        # Enough money — start dispensing!
        machine.set_state(DispensingState(item, price))
        # Trigger dispensing immediately
        machine.state.dispense(machine)

    def cancel(self, machine):
        print(f"Transaction cancelled. Returning Rs.{machine.inserted_amount}")
        machine.inserted_amount = 0
        machine.set_state(IdleState())

    def state_name(self):
        return "HAS_COIN"

class DispensingState(VendingMachineState):
    """Machine is dispensing an item. No actions allowed during this."""

    def __init__(self, item: str = "", price: int = 0):
        self._item = item
        self._price = price

    def dispense(self, machine):
        """Actually dispense the item. Called internally, not by the user."""
        # Deduct stock
        machine.stock[self._item] -= 1
        change = machine.inserted_amount - self._price
        print(f"Dispensing {self._item}...")
        if change > 0:
            print(f"Returning change: Rs.{change}")
        print(f"Enjoy your {self._item}!")
        machine.inserted_amount = 0

        # Check if machine has ANY stock left
        if all(qty <= 0 for qty in machine.stock.values()):
            machine.set_state(OutOfStockState())
        else:
            machine.set_state(IdleState())

    def insert_coin(self, machine, amount):
        print("Please wait — dispensing in progress.")

    def select_item(self, machine, item):
        print("Please wait — dispensing in progress.")

    def cancel(self, machine):
        print("Cannot cancel — dispensing in progress.")

    def state_name(self):
        return "DISPENSING"

class OutOfStockState(VendingMachineState):
    """Machine has no items left. Only a refill can fix this."""

    def insert_coin(self, machine, amount):
        print("Sorry, machine is completely out of stock. Returning your Rs.{amount}.")

    def select_item(self, machine, item):
        print("Machine is out of stock. Please try another machine.")

    def cancel(self, machine):
        if machine.inserted_amount > 0:
            print(f"Returning Rs.{machine.inserted_amount}")
            machine.inserted_amount = 0

    def state_name(self):
        return "OUT_OF_STOCK"

# Step 3: The VendingMachine context
# It delegates ALL behavior to its current state object.
class VendingMachine:
    def __init__(self):
        self.stock = {"chips": 2, "cola": 1, "water": 3}
        self.inserted_amount = 0
        self.state = IdleState()

    def set_state(self, state: VendingMachineState):
        """Transition to a new state."""
        old_name = self.state.state_name()
        self.state = state
        print(f"  [State: {old_name} -> {state.state_name()}]")

    def insert_coin(self, amount):
        self.state.insert_coin(self, amount)

    def select_item(self, item):
        self.state.select_item(self, item)

    def cancel(self):
        self.state.cancel(self)

    def show_status(self):
        print(f"\nMachine Status: {self.state.state_name()}")
        print(f"Inserted: Rs.{self.inserted_amount}")
        print(f"Stock: {self.stock}")


# Step 4: Usage — see how the machine's behavior changes with state

vm = VendingMachine()
vm.show_status()

# Try selecting without inserting coin
vm.select_item("chips")
# "Please insert a coin first!"

# Insert a coin
vm.insert_coin(10)
# "Rs.10 inserted. Select an item."
# [State: IDLE -> HAS_COIN]

# Try to buy chips (costs 20, only have 10)
vm.select_item("chips")
# "chips costs Rs.20, you have Rs.10. Insert Rs.10 more."

# Insert more
vm.insert_coin(20)
# "Rs.20 more inserted. Total: Rs.30"

# Now buy chips
vm.select_item("chips")
# [State: HAS_COIN -> DISPENSING]
# "Dispensing chips..."
# "Returning change: Rs.10"
# "Enjoy your chips!"
# [State: DISPENSING -> IDLE]

# Cancel flow
vm.insert_coin(50)
vm.cancel()
# "Transaction cancelled. Returning Rs.50"
# [State: HAS_COIN -> IDLE]
```

**Before vs. After:**

| Without State | With State |
|---|---|
| All state logic scattered across every method | All logic for a state is in ONE class |
| Adding new state = modify every method | Adding new state = create one new class |
| Easy to forget a state in a method | Abstract base class FORCES you to implement all actions |
| State transitions hidden in assignments (`self.state = "x"`) | State transitions are explicit (`set_state(NewState())`) |

#### When to use State

- Objects with distinct behavioral states (order lifecycle, document workflow, game character states)
- When behavior depends heavily on the current state AND state transitions are well-defined
- Replacing complex state-machine if-elif blocks
- UI components (button: enabled/disabled/loading states)
- Network connections (disconnected/connecting/connected/error)

**The trigger question:** "Does this object behave very differently depending on its current state, and are there well-defined transitions between states?" If yes, use State.

#### When NOT to use State

- If the object only has 2 states with simple behavior, if-else is fine
- If states are independent (no transitions between them), you might be overcomplicating things
- If the behavior difference between states is tiny (e.g., just a different message), the full State pattern is overkill

---

## Quick Reference — All Design Patterns (Week 2 + Week 3)

| Pattern | Category | What It Does | Real Indian Example | Interview Frequency |
|---------|----------|-------------|---------------------|-------------------|
| Singleton | Creational | One instance only | UIDAI/Aadhaar database | High |
| Factory | Creational | Create without specifying class | Zomato notification channels | High |
| Builder | Creational | Step-by-step construction | Swiggy cart / Domino's customizer | Medium |
| Adapter | Structural | Convert incompatible interfaces | UPI connecting banks & apps | Medium |
| Decorator | Structural | Add behavior by wrapping | Flipkart API middleware layers | High |
| Facade | Structural | Simplify complex subsystem | Swiggy "Place Order" button | Medium |
| **Strategy** | **Behavioral** | **Swap algorithms at runtime** | **Flipkart discount calculation** | **Very High** |
| **Observer** | **Behavioral** | **Notify all watchers on change** | **Zomato order status events** | **Very High** |
| **Command** | **Behavioral** | **Wrap actions as objects (undo/redo)** | **Google Docs edit history** | **High** |
| **State** | **Behavioral** | **Change behavior based on state** | **Ola ride lifecycle** | **Medium** |

---

## How Patterns Work Together

In real systems, you NEVER use just one pattern. Here is how they combine in a real-world Swiggy-like system:

```
User places order
    |
    v
OrderFacade (FACADE — hides complexity of 7 subsystems)
    |
    ├── NotificationFactory.create("push") (FACTORY — creates the right notification)
    |       |
    |       └── PushNotification with LoggingDecorator (DECORATOR — adds logging)
    |
    ├── PricingEngine (STRATEGY — applies current discount strategy)
    |       |
    |       └── BigBillionDayDiscount (current strategy)
    |
    ├── OrderEventManager.notify("order_placed") (OBSERVER — notifies all subscribers)
    |       |
    |       ├── AnalyticsObserver
    |       ├── RestaurantDashboardObserver
    |       └── DeliveryAssignmentObserver
    |
    ├── Order.set_state(PreparingState()) (STATE — order transitions to preparing)
    |
    └── DatabaseConnection.get_instance() (SINGLETON — one shared DB connection)
```

---

## Practice Exercises

### Exercise 1: Flipkart Discount Strategy System (Estimated time: 30 minutes)

**What to build:**
A complete pricing system using the Strategy pattern. This is almost exactly what you might be asked in a Microsoft interview: "Design a discount system for an e-commerce platform."

**Requirements:**
1. Create a `DiscountStrategy` base class with a `calculate(original_price, **kwargs)` method
2. Implement these concrete strategies:
   - `NoDiscount` — returns full price
   - `PercentageDiscount(percentage)` — e.g., 20% off
   - `FlatDiscount(amount)` — e.g., Rs.500 off (cannot go below 0)
   - `BuyOneGetOneFree(cart_items)` — cheapest item free
   - `CappedPercentageDiscount(percentage, max_discount)` — e.g., 50% off up to Rs.150 (like Swiggy's first order)
3. Create a `ShoppingCart` class that:
   - Holds items (list of dicts with name and price)
   - Has a `set_discount(strategy)` method to change the strategy
   - Has a `checkout()` method that calculates and displays the final price
4. Demonstrate switching strategies at runtime (simulating a sale starting)

**Test your solution:**
```python
cart = ShoppingCart()
cart.add_item("Laptop", 55000)
cart.add_item("Mouse", 500)
cart.add_item("Keyboard", 1500)

cart.set_discount(NoDiscount())
print(cart.checkout())  # Rs.57000

cart.set_discount(PercentageDiscount(10))
print(cart.checkout())  # Rs.51300

cart.set_discount(CappedPercentageDiscount(50, 2000))
print(cart.checkout())  # Rs.55000 (50% of 57000 = 28500, capped at 2000, so 57000-2000)
```

---

### Exercise 2: Sensex Stock Market Observer (Estimated time: 30 minutes)

**What to build:**
A stock price monitoring system where multiple subscribers react when stock prices change. Think of how Zerodha, Groww, and Angel One apps all show you the same stock prices but in different formats.

**Requirements:**
1. Create a `StockExchange` class (the subject/publisher) that:
   - Maintains current prices for stocks (dict of stock_name -> price)
   - Has `subscribe(stock, observer)` and `unsubscribe(stock, observer)` methods
   - When a stock price changes, notifies all observers subscribed to that stock
2. Create these observer classes:
   - `MobileAppAlert` — shows a push notification with the stock name and new price
   - `WebDashboard` — updates a dashboard display with price and percentage change
   - `EmailDigest` — accumulates changes and shows a summary
   - `PortfolioCalculator` — recalculates total portfolio value based on holdings
3. The `PortfolioCalculator` should accept a dict of holdings (e.g., `{"TCS": 10, "INFY": 5}`) and recalculate total value whenever a held stock's price changes

**Test your solution:**
```python
exchange = StockExchange()
exchange.set_price("TCS", 3500)
exchange.set_price("INFY", 1800)
exchange.set_price("RELIANCE", 2500)

mobile = MobileAppAlert()
portfolio = PortfolioCalculator({"TCS": 10, "INFY": 5})

exchange.subscribe("TCS", mobile)
exchange.subscribe("TCS", portfolio)
exchange.subscribe("INFY", portfolio)

exchange.update_price("TCS", 3600)
# MobileAppAlert: TCS price changed to Rs.3600
# PortfolioCalculator: Portfolio value = Rs.45000 (TCS:10*3600 + INFY:5*1800)

exchange.update_price("INFY", 1750)
# PortfolioCalculator: Portfolio value = Rs.44750 (TCS:10*3600 + INFY:5*1750)
```

---

### Exercise 3: Text Editor with Undo/Redo (Estimated time: 35 minutes)

**What to build:**
Extend the Command pattern text editor to support these additional features:

1. **Bold formatting command** — wraps selected text in `**` markers (for Markdown bold)
2. **Batch commands** — group multiple commands into one undo unit (e.g., "Find and Replace All" is one undo action even though it makes 5 replacements)
3. A `show_history()` method that displays all commands in the history stack
4. A `show_redo_stack()` method that shows what can be re-done

**Test your solution:**
```python
editor = TextEditor()
editor.type_text("Hello World Hello India Hello Python")
editor.show_history()  # 1 command

# Replace all "Hello" with "Namaste" as one batch
editor.batch_replace("Hello", "Namaste")
print(editor.content)  # "Namaste World Namaste India Namaste Python"
editor.show_history()  # 2 commands (the batch counts as 1)

# One undo should revert ALL replacements
editor.undo()
print(editor.content)  # "Hello World Hello India Hello Python"
```

---

### Exercise 4 (Bonus): Ola Ride State Machine (Estimated time: 40 minutes)

**What to build:**
Implement the complete Ola ride lifecycle using the State pattern. This is a realistic interview question.

**States:** SEARCHING, DRIVER_ASSIGNED, WAITING, IN_RIDE, COMPLETED, CANCELLED

**Actions and their behavior per state:**

| Action | SEARCHING | DRIVER_ASSIGNED | WAITING | IN_RIDE | COMPLETED |
|--------|-----------|----------------|---------|---------|-----------|
| cancel() | Cancel (free) | Cancel (Rs.50 fee) | Cancel (Rs.100 fee) | NOT ALLOWED | NOT ALLOWED |
| driver_found() | -> DRIVER_ASSIGNED | "Already assigned" | N/A | N/A | N/A |
| driver_arrived() | N/A | -> WAITING | "Already waiting" | N/A | N/A |
| start_ride() | N/A | N/A | -> IN_RIDE | "Already riding" | N/A |
| end_ride() | N/A | N/A | N/A | -> COMPLETED | N/A |
| rate(stars) | N/A | N/A | N/A | N/A | Record rating |

Implement the `Ride` class and all state classes. Show a complete ride lifecycle from booking to rating.

---

## Self-Check

Before moving to Week 4, verify:

- [ ] I can explain all 4 behavioral patterns in plain English (what, why, when)
- [ ] I can give an Indian real-life analogy for each (Ola, Zomato, Flipkart, etc.)
- [ ] I can write Strategy, Observer, Command, and State from memory
- [ ] I understand the "trigger question" for each pattern
- [ ] I know when NOT to use each pattern
- [ ] I can identify which pattern fits a given interview problem
- [ ] I know ALL 10 patterns from Week 2 + Week 3
- [ ] I completed Exercises 1, 2, and 3
- [ ] I attempted Exercise 4 (bonus)
- [ ] I can explain how multiple patterns work together in a real system
