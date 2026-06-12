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

```java
// WITHOUT Strategy — The PROBLEM
// Imagine Flipkart's discount system

import java.util.List;
import java.util.Map;

public class DiscountCalculatorBroken {
    /**
     * This method handles ALL discount types with if-else.
     * Every time a new discount type is added, THIS method grows.
     */
    public static double calculatePrice(double originalPrice, String discountType,
                                        Double discountValue, List<Map<String, Object>> cartItems) {
        if (discountType.equals("none")) {
            return originalPrice;

        } else if (discountType.equals("percentage")) {
            // Diwali Sale: 20% off
            return originalPrice * (1 - discountValue / 100);

        } else if (discountType.equals("flat")) {
            // Coupon: Rs.500 off
            return Math.max(0, originalPrice - discountValue);

        } else if (discountType.equals("bogo")) {
            // Buy One Get One Free
            if (cartItems != null && cartItems.size() >= 2) {
                double cheapest = cartItems.stream()
                    .mapToDouble(item -> (double) item.get("price"))
                    .min()
                    .orElse(0);
                return originalPrice - cheapest;
            }
            return originalPrice;

        } else if (discountType.equals("tiered")) {
            // Spend more, save more:
            //   Above 5000: 10% off
            //   Above 10000: 15% off
            //   Above 20000: 20% off
            if (originalPrice > 20000) {
                return originalPrice * 0.8;
            } else if (originalPrice > 10000) {
                return originalPrice * 0.85;
            } else if (originalPrice > 5000) {
                return originalPrice * 0.9;
            }
            return originalPrice;

        } else if (discountType.equals("first_order")) {
            // First order: 50% off up to Rs.150
            double discount = Math.min(originalPrice * 0.5, 150);
            return originalPrice - discount;

        } else if (discountType.equals("bank_offer")) {
            // HDFC Credit Card: 10% instant discount up to Rs.1500
            double discount = Math.min(originalPrice * 0.1, 1500);
            return originalPrice - discount;

        // PROBLEM: This method is ALREADY 40+ lines long.
        // Every new sale event (Republic Day Sale, Big Billion Days,
        // Student Discount, Senior Citizen Discount, Employee Discount)
        // adds another else-if block.
        //
        // After a year, this method will be 200+ lines with 20+ conditions.
        // Testing it is a nightmare — you must test ALL paths.
        // Two developers working on different discounts will get merge conflicts.
        // One typo in the "tiered" block could break the "bogo" block.

        } else {
            throw new IllegalArgumentException("Unknown discount type: " + discountType);
        }
    }
}
```

**What goes wrong:**
1. **Open/Closed Principle violation** — To add a new discount, you MODIFY existing code (instead of ADDING new code). Modifying existing code risks breaking things that already work.
2. **Single Responsibility violation** — One function knows about ALL discount algorithms. If the BOGO logic has a bug, you must read past 10 other algorithms to find it.
3. **Testing nightmare** — You must write tests for the entire function, including all paths. If you add a new path, you risk breaking existing tests.
4. **Merge conflicts** — If Developer A adds "Student Discount" and Developer B adds "Employee Discount" at the same time, they both edit the same function and get a merge conflict.

#### The pattern — Clean code WITH Strategy

```java
import java.util.*;

// Step 1: Define the Strategy interface (the contract)
// Every discount strategy MUST implement calculate().
// This ensures the rest of the code can treat ALL strategies the same way.
interface DiscountStrategy {
    /**
     * Given an original price, return the discounted price.
     * context allows each strategy to accept its own specific parameters.
     */
    double calculate(double originalPrice, Map<String, Object> context);
}

// Step 2: Implement each strategy as its own class
// Each class encapsulates ONE algorithm. It is small, focused, and testable.

class NoDiscount implements DiscountStrategy {
    /** Full price — no discount applied. */
    @Override
    public double calculate(double originalPrice, Map<String, Object> context) {
        return originalPrice;
    }
}

class PercentageDiscount implements DiscountStrategy {
    /**
     * A percentage off the original price.
     * Example: Diwali Sale — 20% off everything
     */
    private final double percentage;

    public PercentageDiscount(double percentage) {
        // Store the percentage when the strategy is created.
        // This means you can have PercentageDiscount(20) for 20% off
        // and PercentageDiscount(30) for 30% off — same class, different config.
        this.percentage = percentage;
    }

    @Override
    public double calculate(double originalPrice, Map<String, Object> context) {
        double discountAmount = originalPrice * (this.percentage / 100);
        return originalPrice - discountAmount;
    }
}

class FlatDiscount implements DiscountStrategy {
    /**
     * A fixed amount off the price.
     * Example: Apply coupon FLAT500 — Rs.500 off
     */
    private final double amount;

    public FlatDiscount(double amount) {
        this.amount = amount;
    }

    @Override
    public double calculate(double originalPrice, Map<String, Object> context) {
        // Ensure price does not go below zero
        return Math.max(0, originalPrice - this.amount);
    }
}

class BuyOneGetOneFree implements DiscountStrategy {
    /**
     * Buy one get one free — deducts the cheapest item's price.
     * Example: Flipkart BOGO on fashion items
     */
    @Override
    @SuppressWarnings("unchecked")
    public double calculate(double originalPrice, Map<String, Object> context) {
        List<Map<String, Object>> cartItems =
            (List<Map<String, Object>>) context.getOrDefault("cartItems", new ArrayList<>());
        if (cartItems != null && cartItems.size() >= 2) {
            double cheapest = cartItems.stream()
                .mapToDouble(item -> (double) item.get("price"))
                .min()
                .orElse(0);
            return originalPrice - cheapest;
        }
        return originalPrice;
    }
}

class TieredDiscount implements DiscountStrategy {
    /**
     * Spend more, save more.
     * Example: Big Billion Days — 10% off above 5000, 15% off above 10000, 20% off above 20000
     */
    private final List<double[]> tiers;

    public TieredDiscount(List<double[]> tiers) {
        /**
         * tiers is a list of {threshold, percentage} arrays, sorted descending.
         * Example: {{20000, 20}, {10000, 15}, {5000, 10}}
         */
        // Sort by threshold descending so we check highest first
        this.tiers = new ArrayList<>(tiers);
        this.tiers.sort((a, b) -> Double.compare(b[0], a[0]));
    }

    @Override
    public double calculate(double originalPrice, Map<String, Object> context) {
        for (double[] tier : this.tiers) {
            double threshold = tier[0];
            double percentage = tier[1];
            if (originalPrice > threshold) {
                return originalPrice * (1 - percentage / 100);
            }
        }
        return originalPrice; // No tier matched — no discount
    }
}

class FirstOrderDiscount implements DiscountStrategy {
    /**
     * New customer discount — 50% off, capped at Rs.150.
     * Example: Swiggy's first order discount
     */
    private final double percentage;
    private final double maxDiscount;

    public FirstOrderDiscount() {
        this(50, 150);
    }

    public FirstOrderDiscount(double percentage, double maxDiscount) {
        this.percentage = percentage;
        this.maxDiscount = maxDiscount;
    }

    @Override
    public double calculate(double originalPrice, Map<String, Object> context) {
        double discount = Math.min(originalPrice * (this.percentage / 100), this.maxDiscount);
        return originalPrice - discount;
    }
}

// Step 3: The Context — the class that USES a strategy
// The PricingEngine does not know WHICH strategy it is using.
// It just knows it HAS a strategy, and it can call calculate() on it.
class PricingEngine {
    private DiscountStrategy strategy;

    public PricingEngine(DiscountStrategy strategy) {
        /** Create a pricing engine with an initial strategy. */
        this.strategy = strategy;
    }

    public void setStrategy(DiscountStrategy strategy) {
        /**
         * Switch the strategy at RUNTIME.
         * Example: Normal hours use NoDiscount, but when a sale starts,
         * switch to PercentageDiscount(20) without restarting the app.
         */
        this.strategy = strategy;
    }

    public double calculateFinalPrice(double originalPrice, Map<String, Object> context) {
        /** Delegate to whatever strategy is currently set. */
        double finalPrice = this.strategy.calculate(originalPrice, context);
        return Math.round(finalPrice * 100.0) / 100.0;
    }

    public double calculateFinalPrice(double originalPrice) {
        return calculateFinalPrice(originalPrice, new HashMap<>());
    }
}


// Step 4: Usage — see how clean and flexible this is!

class StrategyDemo {
    public static void main(String[] args) {
        // Scenario 1: Regular day, no sale
        PricingEngine engine = new PricingEngine(new NoDiscount());
        System.out.println("Regular price: Rs." + engine.calculateFinalPrice(2000));
        // Regular price: Rs.2000

        // Scenario 2: Diwali Sale starts — just swap the strategy!
        engine.setStrategy(new PercentageDiscount(20));
        System.out.println("Diwali sale price: Rs." + engine.calculateFinalPrice(2000));
        // Diwali sale price: Rs.1600.0

        // Scenario 3: Customer applies a coupon
        engine.setStrategy(new FlatDiscount(500));
        System.out.println("Coupon price: Rs." + engine.calculateFinalPrice(2000));
        // Coupon price: Rs.1500

        // Scenario 4: Big Billion Days tiered discount
        TieredDiscount bigBillion = new TieredDiscount(Arrays.asList(
            new double[]{20000, 20}, new double[]{10000, 15}, new double[]{5000, 10}
        ));
        engine.setStrategy(bigBillion);
        System.out.println("Rs.25000 item during BBD: Rs." + engine.calculateFinalPrice(25000));
        // Rs.25000 item during BBD: Rs.20000.0
        System.out.println("Rs.7000 item during BBD: Rs." + engine.calculateFinalPrice(7000));
        // Rs.7000 item during BBD: Rs.6300.0

        // Scenario 5: New customer first order
        engine.setStrategy(new FirstOrderDiscount());
        System.out.println("First order price for Rs.400: Rs." + engine.calculateFinalPrice(400));
        // First order price for Rs.400: Rs.250.0 (50% of 400 = 200, capped at 150, so 400-150=250)

        // Scenario 6: BOGO
        engine.setStrategy(new BuyOneGetOneFree());
        List<Map<String, Object>> cart = new ArrayList<>();
        cart.add(Map.of("name", "T-Shirt", "price", 800.0));
        cart.add(Map.of("name", "Socks", "price", 200.0));
        double total = cart.stream().mapToDouble(item -> (double) item.get("price")).sum();
        Map<String, Object> context = new HashMap<>();
        context.put("cartItems", cart);
        System.out.println("BOGO price: Rs." + engine.calculateFinalPrice(total, context));
        // BOGO price: Rs.800.0 (cheapest item = socks Rs.200 is free)
    }
}
```

**Why this is better:**

| Concern | Without Strategy | With Strategy |
|---------|-----------------|---------------|
| Adding a new discount | Modify the big function, risk breaking others | Create a new class, zero risk to existing code |
| Testing a specific discount | Test the entire function | Test only the one class |
| Two devs adding different discounts | Merge conflict (same file/function) | No conflict (different files) |
| Reading the code | Scroll through 200 lines of if-elif | Open the one 10-line class you need |
| Runtime flexibility | Must redeploy to change discount logic | Swap strategy at runtime with setStrategy() |

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

```java
// WITHOUT Observer — The PROBLEM
// The order system must KNOW about every system that cares about order events

class OrderServiceBroken {
    // The order service must hold references to EVERY dependent system
    private PushNotificationService pushNotification;
    private EmailService emailService;
    private SMSService smsService;
    private TrackingService trackingService;
    private RestaurantDashboard restaurantDashboard;
    private AnalyticsService analytics;
    private ETACalculator etaCalculator;

    public OrderServiceBroken() {
        this.pushNotification = new PushNotificationService();
        this.emailService = new EmailService();
        this.smsService = new SMSService();
        this.trackingService = new TrackingService();
        this.restaurantDashboard = new RestaurantDashboard();
        this.analytics = new AnalyticsService();
        this.etaCalculator = new ETACalculator();
    }

    public void updateOrderStatus(String orderId, String newStatus) {
        // Update the order in the database
        System.out.println("Order " + orderId + " -> " + newStatus);

        // NOW: manually call EVERY system that might care
        if (newStatus.equals("preparing")) {
            pushNotification.send("Order " + orderId + " is being prepared");
            restaurantDashboard.update(orderId, "preparing");
            analytics.log(orderId, "preparation_started");

        } else if (newStatus.equals("out_for_delivery")) {
            pushNotification.send("Rider is on the way for order " + orderId);
            smsService.send("Your order " + orderId + " is out for delivery");
            trackingService.startTracking(orderId);
            restaurantDashboard.update(orderId, "picked_up");
            analytics.log(orderId, "picked_up");
            etaCalculator.recalculate(orderId);

        } else if (newStatus.equals("delivered")) {
            pushNotification.send("Order " + orderId + " delivered!");
            emailService.sendReceipt(orderId);
            trackingService.stopTracking(orderId);
            restaurantDashboard.update(orderId, "completed");
            analytics.log(orderId, "delivered");
            // OOPS! Forgot to add loyaltyService.awardPoints() here
            // And forgot to add feedbackService.requestRating() here
            // Easy to forget when you must manually add each call!
        }
    }
}

// PROBLEMS:
// 1. OrderService is tightly coupled to 7+ services
// 2. Adding a new subscriber means MODIFYING OrderService
// 3. Easy to forget to add a new service to every relevant status change
// 4. Cannot dynamically subscribe/unsubscribe at runtime
// 5. Testing OrderService requires mocking all 7 services
// 6. If the analytics service is down, it might crash the order update!
```

#### The pattern — Clean code WITH Observer

```java
import java.time.LocalDateTime;
import java.util.*;

// Step 1: Define what an "observer" looks like (the subscriber interface)
// Any class that wants to be notified must implement this method.
interface OrderObserver {
    /** Called when an order's status changes. */
    void onOrderUpdate(String orderId, String oldStatus, String newStatus, LocalDateTime timestamp);
}

// Step 2: Define the "subject" (the publisher) — the EventManager
// This is the backbone of the Observer pattern. It maintains a list of
// subscribers and notifies them when events occur.
class OrderEventManager {
    // Map: eventName -> list of observers
    // This allows different observers to subscribe to different events.
    // Example: PushNotification subscribes to ALL events,
    //          but TrackingService only subscribes to "out_for_delivery"
    private final Map<String, List<OrderObserver>> listeners = new HashMap<>();

    public void subscribe(String event, OrderObserver observer) {
        /**
         * Register an observer for a specific event.
         * Example: manager.subscribe("delivered", feedbackService)
         */
        listeners.computeIfAbsent(event, k -> new ArrayList<>());
        if (!listeners.get(event).contains(observer)) { // prevent duplicate subscriptions
            listeners.get(event).add(observer);
            System.out.println("  [EventManager] " + observer.getClass().getSimpleName() + " subscribed to '" + event + "'");
        }
    }

    public void unsubscribe(String event, OrderObserver observer) {
        /**
         * Remove an observer from a specific event.
         * Useful for: temporarily disabling a service, A/B tests, etc.
         */
        if (listeners.containsKey(event)) {
            listeners.get(event).remove(observer);
        }
    }

    public void notify(String event, String orderId, String oldStatus, String newStatus) {
        /**
         * Notify ALL observers subscribed to this event.
         * The subject does NOT know who the observers are or what they do.
         * It just calls onOrderUpdate() on each one.
         */
        LocalDateTime timestamp = LocalDateTime.now();
        List<OrderObserver> eventListeners = listeners.getOrDefault(event, new ArrayList<>());
        for (OrderObserver listener : eventListeners) {
            try {
                listener.onOrderUpdate(orderId, oldStatus, newStatus, timestamp);
            } catch (Exception e) {
                // IMPORTANT: One failing observer should NOT crash the whole system.
                // If analytics is down, the user should still get their notification.
                System.out.println("  [EventManager] Error notifying " + listener.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }
}

// Step 3: Concrete observers — each handles the event in its own way

class PushNotificationObserver implements OrderObserver {
    /** Sends push notifications to the customer's phone. */
    private static final Map<String, String> MESSAGES = Map.of(
        "preparing", "Your order is being prepared by the restaurant!",
        "out_for_delivery", "Your rider is on the way! Track live on the app.",
        "delivered", "Your order has been delivered. Enjoy your meal!"
    );

    @Override
    public void onOrderUpdate(String orderId, String oldStatus, String newStatus, LocalDateTime timestamp) {
        String message = MESSAGES.getOrDefault(newStatus, "Order " + orderId + " status: " + newStatus);
        System.out.println("  [PUSH] " + message);
    }
}

class SMSObserver implements OrderObserver {
    /** Sends SMS for critical updates (out for delivery, delivered). */
    @Override
    public void onOrderUpdate(String orderId, String oldStatus, String newStatus, LocalDateTime timestamp) {
        System.out.println("  [SMS] Order " + orderId + ": Status changed to " + newStatus);
    }
}

class EmailObserver implements OrderObserver {
    /** Sends email receipt when order is delivered. */
    @Override
    public void onOrderUpdate(String orderId, String oldStatus, String newStatus, LocalDateTime timestamp) {
        System.out.println("  [EMAIL] Sending receipt for order " + orderId);
    }
}

class RestaurantDashboardObserver implements OrderObserver {
    /** Updates the restaurant's kitchen display system. */
    @Override
    public void onOrderUpdate(String orderId, String oldStatus, String newStatus, LocalDateTime timestamp) {
        System.out.println("  [RESTAURANT] Dashboard updated: Order " + orderId + " -> " + newStatus);
    }
}

class AnalyticsObserver implements OrderObserver {
    /** Records order events for analytics and reporting. */
    private final List<Map<String, String>> events = new ArrayList<>();

    @Override
    public void onOrderUpdate(String orderId, String oldStatus, String newStatus, LocalDateTime timestamp) {
        Map<String, String> event = new HashMap<>();
        event.put("order_id", orderId);
        event.put("from", oldStatus);
        event.put("to", newStatus);
        event.put("timestamp", timestamp.toString());
        events.add(event);
        System.out.println("  [ANALYTICS] Recorded: " + oldStatus + " -> " + newStatus + " for order " + orderId);
    }
}

class LiveTrackingObserver implements OrderObserver {
    /** Starts/stops live GPS tracking of the delivery rider. */
    @Override
    public void onOrderUpdate(String orderId, String oldStatus, String newStatus, LocalDateTime timestamp) {
        if (newStatus.equals("out_for_delivery")) {
            System.out.println("  [TRACKING] Started live tracking for order " + orderId);
        } else if (newStatus.equals("delivered")) {
            System.out.println("  [TRACKING] Stopped live tracking for order " + orderId);
        }
    }
}

class LoyaltyPointsObserver implements OrderObserver {
    /**
     * Awards loyalty points when order is delivered.
     * This was added MONTHS after the original system —
     * required ZERO changes to OrderService!
     */
    @Override
    public void onOrderUpdate(String orderId, String oldStatus, String newStatus, LocalDateTime timestamp) {
        System.out.println("  [LOYALTY] Awarded 50 points for order " + orderId);
    }
}

// Step 4: The OrderService — now CLEAN and decoupled
class OrderService {
    // The order service only knows about the event manager.
    // It does NOT know about push notifications, SMS, analytics, etc.
    private final OrderEventManager eventManager;
    private final Map<String, String> orders = new HashMap<>();

    public OrderService(OrderEventManager eventManager) {
        this.eventManager = eventManager;
    }

    public void updateStatus(String orderId, String newStatus) {
        String oldStatus = orders.getOrDefault(orderId, "placed");
        orders.put(orderId, newStatus);
        System.out.println("\nOrder " + orderId + ": " + oldStatus + " -> " + newStatus);
        // Just publish the event — let observers handle the rest
        eventManager.notify(newStatus, orderId, oldStatus, newStatus);
    }
}


// Step 5: Wire it all together

class ObserverDemo {
    public static void main(String[] args) {
        // Create the event manager
        OrderEventManager events = new OrderEventManager();

        // Create observers
        PushNotificationObserver push = new PushNotificationObserver();
        SMSObserver sms = new SMSObserver();
        EmailObserver email = new EmailObserver();
        RestaurantDashboardObserver restaurant = new RestaurantDashboardObserver();
        AnalyticsObserver analytics = new AnalyticsObserver();
        LiveTrackingObserver tracking = new LiveTrackingObserver();
        LoyaltyPointsObserver loyalty = new LoyaltyPointsObserver();

        // Subscribe each observer to the events they care about
        // (not every observer cares about every event)
        events.subscribe("preparing", push);
        events.subscribe("preparing", restaurant);
        events.subscribe("preparing", analytics);

        events.subscribe("out_for_delivery", push);
        events.subscribe("out_for_delivery", sms);
        events.subscribe("out_for_delivery", restaurant);
        events.subscribe("out_for_delivery", tracking);
        events.subscribe("out_for_delivery", analytics);

        events.subscribe("delivered", push);
        events.subscribe("delivered", sms);
        events.subscribe("delivered", email);
        events.subscribe("delivered", restaurant);
        events.subscribe("delivered", tracking);
        events.subscribe("delivered", analytics);
        events.subscribe("delivered", loyalty); // Added later — no code change to OrderService!

        // Create the order service
        OrderService orderService = new OrderService(events);

        // Simulate an order going through its lifecycle:
        System.out.println("============================================================");
        orderService.updateStatus("ORD-001", "preparing");
        // Order ORD-001: placed -> preparing
        //   [PUSH] Your order is being prepared by the restaurant!
        //   [RESTAURANT] Dashboard updated: Order ORD-001 -> preparing
        //   [ANALYTICS] Recorded: placed -> preparing for order ORD-001

        System.out.println("============================================================");
        orderService.updateStatus("ORD-001", "out_for_delivery");
        // Order ORD-001: preparing -> out_for_delivery
        //   [PUSH] Your rider is on the way! Track live on the app.
        //   [SMS] Order ORD-001: Status changed to out_for_delivery
        //   [RESTAURANT] Dashboard updated: Order ORD-001 -> out_for_delivery
        //   [TRACKING] Started live tracking for order ORD-001
        //   [ANALYTICS] Recorded: preparing -> out_for_delivery for order ORD-001

        System.out.println("============================================================");
        orderService.updateStatus("ORD-001", "delivered");
        // Order ORD-001: out_for_delivery -> delivered
        //   [PUSH] Your order has been delivered. Enjoy your meal!
        //   [SMS] Order ORD-001: Status changed to delivered
        //   [EMAIL] Sending receipt for order ORD-001
        //   [RESTAURANT] Dashboard updated: Order ORD-001 -> delivered
        //   [TRACKING] Stopped live tracking for order ORD-001
        //   [ANALYTICS] Recorded: out_for_delivery -> delivered for order ORD-001
        //   [LOYALTY] Awarded 50 points for order ORD-001
    }
}
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

```java
// WITHOUT Command — The PROBLEM
// A text editor where undo is implemented with manual state tracking

import java.util.ArrayList;
import java.util.List;

class TextEditorBroken {
    private String content = "";
    // To support undo, you would need to save the ENTIRE content
    // before every operation. For a 100MB document, this is insane.
    private List<String> snapshots = new ArrayList<>();

    public void typeText(String text) {
        snapshots.add(content);  // Save entire state!
        content += text;
    }

    public void deleteLastN(int n) {
        snapshots.add(content);  // Save entire state again!
        content = content.substring(0, content.length() - n);
    }

    public void replace(String oldText, String newText) {
        snapshots.add(content);  // And again!
        content = content.replace(oldText, newText);
    }

    public void undo() {
        if (!snapshots.isEmpty()) {
            content = snapshots.remove(snapshots.size() - 1);  // Restore ENTIRE content
        }
    }
}

// PROBLEMS:
// 1. Memory: Saving the entire document for every keystroke is extremely wasteful.
//    A 100-page document with 10,000 edits = 10,000 full copies in memory.
// 2. No redo: Once you undo, the forward history is lost.
// 3. No selective undo: You cannot undo just the "replace" operation while keeping
//    the "type" operations. It is all-or-nothing.
// 4. No batching: You cannot group multiple operations into one "undo unit."
```

#### The pattern — Clean code WITH Command

```java
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

// Step 1: Define the Command interface
// Every command must know how to EXECUTE itself and UNDO itself.
interface Command {
    /** Perform the action. */
    void execute();

    /** Reverse the action — restore to the state before execute(). */
    void undo();

    /** Human-readable description for the history panel. */
    String description();
}

// Step 2: Concrete commands — each knows ONE operation

class InsertTextCommand implements Command {
    /** Inserts text at a specific position in the document. */
    private final TextEditor editor;    // Reference to the editor we are modifying
    private final String text;          // The text to insert
    private final int position;         // Where to insert it

    public InsertTextCommand(TextEditor editor, String text, int position) {
        this.editor = editor;
        this.text = text;
        this.position = position;
    }

    @Override
    public void execute() {
        // Insert text at the specified position
        String content = editor.getContent();
        editor.setContent(
            content.substring(0, position) + text + content.substring(position)
        );
    }

    @Override
    public void undo() {
        // Remove the text we inserted
        // We know exactly where it was and how long it was,
        // so we can surgically remove it (no need to save the whole document!)
        String content = editor.getContent();
        editor.setContent(
            content.substring(0, position) + content.substring(position + text.length())
        );
    }

    @Override
    public String description() {
        String preview = text.length() > 20 ? text.substring(0, 20) + "..." : text;
        return "Insert '" + preview + "' at position " + position;
    }
}

class DeleteTextCommand implements Command {
    /** Deletes text from a specific position. */
    private final TextEditor editor;
    private final int position;
    private final int length;
    private String deletedText = "";  // We will store what was deleted (for undo)

    public DeleteTextCommand(TextEditor editor, int position, int length) {
        this.editor = editor;
        this.position = position;
        this.length = length;
    }

    @Override
    public void execute() {
        String content = editor.getContent();
        // Save what we are about to delete (so we can restore it on undo)
        deletedText = content.substring(position, position + length);
        // Perform the deletion
        editor.setContent(
            content.substring(0, position) + content.substring(position + length)
        );
    }

    @Override
    public void undo() {
        // Re-insert the deleted text at its original position
        String content = editor.getContent();
        editor.setContent(
            content.substring(0, position) + deletedText + content.substring(position)
        );
    }

    @Override
    public String description() {
        return "Delete " + length + " chars at position " + position;
    }
}

class ReplaceTextCommand implements Command {
    /** Replaces all occurrences of oldText with newText. */
    private final TextEditor editor;
    private final String oldText;
    private final String newText;
    private String originalContent = "";  // Save for undo (replace is complex)

    public ReplaceTextCommand(TextEditor editor, String oldText, String newText) {
        this.editor = editor;
        this.oldText = oldText;
        this.newText = newText;
    }

    @Override
    public void execute() {
        originalContent = editor.getContent();
        editor.setContent(editor.getContent().replace(oldText, newText));
    }

    @Override
    public void undo() {
        // For replace, we saved the original content because
        // "find and replace" can affect multiple locations
        editor.setContent(originalContent);
    }

    @Override
    public String description() {
        return "Replace '" + oldText + "' with '" + newText + "'";
    }
}

// Step 3: The TextEditor — manages the command history

class TextEditor {
    private String content = "";
    private final Stack<Command> history = new Stack<>();       // Stack of executed commands (for undo)
    private final Stack<Command> redoStack = new Stack<>();     // Stack of undone commands (for redo)

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void executeCommand(Command command) {
        /** Execute a command and add it to the history. */
        command.execute();
        history.push(command);
        // When a new command is executed, the redo stack is cleared.
        // (You cannot redo commands from a "different timeline.")
        redoStack.clear();
    }

    public void undo() {
        /** Undo the last command. */
        if (history.isEmpty()) {
            System.out.println("Nothing to undo!");
            return;
        }
        Command command = history.pop();
        command.undo();
        redoStack.push(command);  // Move to redo stack
        System.out.println("Undid: " + command.description());
    }

    public void redo() {
        /** Redo the last undone command. */
        if (redoStack.isEmpty()) {
            System.out.println("Nothing to redo!");
            return;
        }
        Command command = redoStack.pop();
        command.execute();
        history.push(command);  // Move back to history
        System.out.println("Redid: " + command.description());
    }

    public void showHistory() {
        /** Show the edit history (like Photoshop's History panel). */
        System.out.println("\n--- Edit History ---");
        int i = 1;
        for (Command cmd : history) {
            System.out.println("  " + i + ". " + cmd.description());
            i++;
        }
        if (history.isEmpty()) {
            System.out.println("  (empty)");
        }
        System.out.println("---");
    }

    // Convenience methods that create and execute commands:
    public void typeText(String text) {
        /** Type text at the end of the document. */
        Command cmd = new InsertTextCommand(this, text, content.length());
        executeCommand(cmd);
    }

    public void insertAt(String text, int position) {
        /** Insert text at a specific position. */
        Command cmd = new InsertTextCommand(this, text, position);
        executeCommand(cmd);
    }

    public void delete(int position, int length) {
        /** Delete text at a specific position. */
        Command cmd = new DeleteTextCommand(this, position, length);
        executeCommand(cmd);
    }

    public void replace(String oldText, String newText) {
        /** Replace all occurrences of oldText with newText. */
        Command cmd = new ReplaceTextCommand(this, oldText, newText);
        executeCommand(cmd);
    }
}


// Step 4: Usage — see the full undo/redo in action

class CommandDemo {
    public static void main(String[] args) {
        TextEditor editor = new TextEditor();

        // Type some text
        editor.typeText("Hello World");
        System.out.println("Content: \"" + editor.getContent() + "\"");      // "Hello World"

        // Type more
        editor.typeText("! Welcome to India");
        System.out.println("Content: \"" + editor.getContent() + "\"");      // "Hello World! Welcome to India"

        // Replace a word
        editor.replace("World", "Sheetal");
        System.out.println("Content: \"" + editor.getContent() + "\"");      // "Hello Sheetal! Welcome to India"

        // Show history
        editor.showHistory();
        // --- Edit History ---
        //   1. Insert 'Hello World' at position 0
        //   2. Insert '! Welcome to India' at position 11
        //   3. Replace 'World' with 'Sheetal'
        // ---

        // Undo the replace
        editor.undo();
        System.out.println("Content: \"" + editor.getContent() + "\"");      // "Hello World! Welcome to India"
        // Undid: Replace 'World' with 'Sheetal'

        // Undo the second insert
        editor.undo();
        System.out.println("Content: \"" + editor.getContent() + "\"");      // "Hello World"
        // Undid: Insert '! Welcome to India' at position 11

        // Redo!
        editor.redo();
        System.out.println("Content: \"" + editor.getContent() + "\"");      // "Hello World! Welcome to India"
        // Redid: Insert '! Welcome to India' at position 11
    }
}
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

```java
// WITHOUT State — The PROBLEM
// A vending machine with state-dependent behavior

import java.util.HashMap;
import java.util.Map;

class VendingMachineBroken {
    private String state = "idle"; // States: idle, has_coin, dispensing, out_of_stock
    private Map<String, Integer> stock = new HashMap<>(Map.of("chips", 5, "cola", 3, "water", 10));
    private int insertedAmount = 0;

    public void insertCoin(int amount) {
        // Every method must check the state with if-else
        if (state.equals("idle")) {
            insertedAmount = amount;
            state = "has_coin";
            System.out.println("Rs." + amount + " inserted");
        } else if (state.equals("has_coin")) {
            insertedAmount += amount;
            System.out.println("Rs." + amount + " more inserted. Total: Rs." + insertedAmount);
        } else if (state.equals("dispensing")) {
            System.out.println("Please wait, dispensing in progress");
        } else if (state.equals("out_of_stock")) {
            System.out.println("Machine is out of stock. Returning your money.");
            // return coins
        }
    }

    public void selectItem(String item) {
        // SAME if-else pattern for EVERY method!
        if (state.equals("idle")) {
            System.out.println("Please insert a coin first");
        } else if (state.equals("has_coin")) {
            if (!stock.containsKey(item) || stock.get(item) <= 0) {
                System.out.println(item + " is out of stock");
                return;
            }
            Map<String, Integer> prices = Map.of("chips", 20, "cola", 30, "water", 10);
            int price = prices.get(item);
            if (insertedAmount >= price) {
                state = "dispensing";
                stock.put(item, stock.get(item) - 1);
                int change = insertedAmount - price;
                System.out.println("Dispensing " + item + "...");
                if (change > 0) {
                    System.out.println("Returning change: Rs." + change);
                }
                insertedAmount = 0;
                state = "idle";
            } else {
                System.out.println("Insufficient amount. " + item + " costs Rs." + price + ", you inserted Rs." + insertedAmount);
            }
        } else if (state.equals("dispensing")) {
            System.out.println("Please wait, dispensing in progress");
        } else if (state.equals("out_of_stock")) {
            System.out.println("Machine is out of stock");
        }
    }

    public void cancel() {
        // AGAIN — same if-else!
        if (state.equals("idle")) {
            System.out.println("Nothing to cancel");
        } else if (state.equals("has_coin")) {
            System.out.println("Returning Rs." + insertedAmount);
            insertedAmount = 0;
            state = "idle";
        } else if (state.equals("dispensing")) {
            System.out.println("Cannot cancel — dispensing in progress");
        } else if (state.equals("out_of_stock")) {
            System.out.println("Nothing to cancel");
        }
    }
}

// PROBLEMS:
// 1. Every method has the SAME if-else block checking this.state
// 2. Adding a new state (e.g., "maintenance") requires modifying EVERY method
// 3. It is easy to forget to handle a state in a new method
// 4. The logic for each state is SCATTERED across all methods instead of being in one place
// 5. As states and actions grow, this becomes a massive matrix of conditions
```

#### The pattern — Clean code WITH State

```java
import java.util.HashMap;
import java.util.Map;

// Step 1: Define the State interface
// Every state class must implement all possible actions.
// This ensures that when you add a new state, the compiler forces you
// to handle every action — no forgotten cases.
interface VendingMachineState {
    void insertCoin(VendingMachine machine, int amount);
    void selectItem(VendingMachine machine, String item);
    void cancel(VendingMachine machine);
    String stateName();
}

// Step 2: Implement each state as its own class
// ALL logic for a given state lives in ONE class.
// Reading the code, you can see exactly what happens in each state.

class IdleState implements VendingMachineState {
    /** Machine is waiting for a customer. No coins inserted. */

    @Override
    public void insertCoin(VendingMachine machine, int amount) {
        machine.setInsertedAmount(amount);
        System.out.println("Rs." + amount + " inserted. Select an item.");
        // Transition to the next state
        machine.setState(new HasCoinState());
    }

    @Override
    public void selectItem(VendingMachine machine, String item) {
        System.out.println("Please insert a coin first!");
    }

    @Override
    public void cancel(VendingMachine machine) {
        System.out.println("Nothing to cancel — machine is idle.");
    }

    @Override
    public String stateName() {
        return "IDLE";
    }
}

class HasCoinState implements VendingMachineState {
    /** Customer has inserted a coin. Waiting for item selection. */

    @Override
    public void insertCoin(VendingMachine machine, int amount) {
        machine.setInsertedAmount(machine.getInsertedAmount() + amount);
        System.out.println("Rs." + amount + " more inserted. Total: Rs." + machine.getInsertedAmount());
    }

    @Override
    public void selectItem(VendingMachine machine, String item) {
        Map<String, Integer> prices = Map.of("chips", 20, "cola", 30, "water", 10);

        if (!prices.containsKey(item)) {
            System.out.println("Unknown item: " + item);
            return;
        }

        if (machine.getStock().getOrDefault(item, 0) <= 0) {
            System.out.println(item + " is out of stock! Choose something else.");
            return;
        }

        int price = prices.get(item);
        if (machine.getInsertedAmount() < price) {
            System.out.println(item + " costs Rs." + price + ", you have Rs." + machine.getInsertedAmount() + ". Insert Rs." + (price - machine.getInsertedAmount()) + " more.");
            return;
        }

        // Enough money — start dispensing!
        DispensingState dispensingState = new DispensingState(item, price);
        machine.setState(dispensingState);
        // Trigger dispensing immediately
        dispensingState.dispense(machine);
    }

    @Override
    public void cancel(VendingMachine machine) {
        System.out.println("Transaction cancelled. Returning Rs." + machine.getInsertedAmount());
        machine.setInsertedAmount(0);
        machine.setState(new IdleState());
    }

    @Override
    public String stateName() {
        return "HAS_COIN";
    }
}

class DispensingState implements VendingMachineState {
    /** Machine is dispensing an item. No actions allowed during this. */
    private final String item;
    private final int price;

    public DispensingState() {
        this("", 0);
    }

    public DispensingState(String item, int price) {
        this.item = item;
        this.price = price;
    }

    public void dispense(VendingMachine machine) {
        /** Actually dispense the item. Called internally, not by the user. */
        // Deduct stock
        machine.getStock().put(item, machine.getStock().get(item) - 1);
        int change = machine.getInsertedAmount() - price;
        System.out.println("Dispensing " + item + "...");
        if (change > 0) {
            System.out.println("Returning change: Rs." + change);
        }
        System.out.println("Enjoy your " + item + "!");
        machine.setInsertedAmount(0);

        // Check if machine has ANY stock left
        boolean allEmpty = machine.getStock().values().stream().allMatch(qty -> qty <= 0);
        if (allEmpty) {
            machine.setState(new OutOfStockState());
        } else {
            machine.setState(new IdleState());
        }
    }

    @Override
    public void insertCoin(VendingMachine machine, int amount) {
        System.out.println("Please wait — dispensing in progress.");
    }

    @Override
    public void selectItem(VendingMachine machine, String item) {
        System.out.println("Please wait — dispensing in progress.");
    }

    @Override
    public void cancel(VendingMachine machine) {
        System.out.println("Cannot cancel — dispensing in progress.");
    }

    @Override
    public String stateName() {
        return "DISPENSING";
    }
}

class OutOfStockState implements VendingMachineState {
    /** Machine has no items left. Only a refill can fix this. */

    @Override
    public void insertCoin(VendingMachine machine, int amount) {
        System.out.println("Sorry, machine is completely out of stock. Returning your Rs." + amount + ".");
    }

    @Override
    public void selectItem(VendingMachine machine, String item) {
        System.out.println("Machine is out of stock. Please try another machine.");
    }

    @Override
    public void cancel(VendingMachine machine) {
        if (machine.getInsertedAmount() > 0) {
            System.out.println("Returning Rs." + machine.getInsertedAmount());
            machine.setInsertedAmount(0);
        }
    }

    @Override
    public String stateName() {
        return "OUT_OF_STOCK";
    }
}

// Step 3: The VendingMachine context
// It delegates ALL behavior to its current state object.
class VendingMachine {
    private Map<String, Integer> stock = new HashMap<>(Map.of("chips", 2, "cola", 1, "water", 3));
    private int insertedAmount = 0;
    private VendingMachineState state = new IdleState();

    public Map<String, Integer> getStock() { return stock; }
    public int getInsertedAmount() { return insertedAmount; }
    public void setInsertedAmount(int amount) { this.insertedAmount = amount; }
    public VendingMachineState getState() { return state; }

    public void setState(VendingMachineState state) {
        /** Transition to a new state. */
        String oldName = this.state.stateName();
        this.state = state;
        System.out.println("  [State: " + oldName + " -> " + state.stateName() + "]");
    }

    public void insertCoin(int amount) {
        state.insertCoin(this, amount);
    }

    public void selectItem(String item) {
        state.selectItem(this, item);
    }

    public void cancel() {
        state.cancel(this);
    }

    public void showStatus() {
        System.out.println("\nMachine Status: " + state.stateName());
        System.out.println("Inserted: Rs." + insertedAmount);
        System.out.println("Stock: " + stock);
    }
}


// Step 4: Usage — see how the machine's behavior changes with state

class StateDemo {
    public static void main(String[] args) {
        VendingMachine vm = new VendingMachine();
        vm.showStatus();

        // Try selecting without inserting coin
        vm.selectItem("chips");
        // "Please insert a coin first!"

        // Insert a coin
        vm.insertCoin(10);
        // "Rs.10 inserted. Select an item."
        // [State: IDLE -> HAS_COIN]

        // Try to buy chips (costs 20, only have 10)
        vm.selectItem("chips");
        // "chips costs Rs.20, you have Rs.10. Insert Rs.10 more."

        // Insert more
        vm.insertCoin(20);
        // "Rs.20 more inserted. Total: Rs.30"

        // Now buy chips
        vm.selectItem("chips");
        // [State: HAS_COIN -> DISPENSING]
        // "Dispensing chips..."
        // "Returning change: Rs.10"
        // "Enjoy your chips!"
        // [State: DISPENSING -> IDLE]

        // Cancel flow
        vm.insertCoin(50);
        vm.cancel();
        // "Transaction cancelled. Returning Rs.50"
        // [State: HAS_COIN -> IDLE]
    }
}
```

**Before vs. After:**

| Without State | With State |
|---|---|
| All state logic scattered across every method | All logic for a state is in ONE class |
| Adding new state = modify every method | Adding new state = create one new class |
| Easy to forget a state in a method | Interface FORCES you to implement all actions |
| State transitions hidden in assignments (`this.state = "x"`) | State transitions are explicit (`setState(new NewState())`) |

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
1. Create a `DiscountStrategy` base class with a `calculate(originalPrice, context)` method
2. Implement these concrete strategies:
   - `NoDiscount` — returns full price
   - `PercentageDiscount(percentage)` — e.g., 20% off
   - `FlatDiscount(amount)` — e.g., Rs.500 off (cannot go below 0)
   - `BuyOneGetOneFree(cartItems)` — cheapest item free
   - `CappedPercentageDiscount(percentage, maxDiscount)` — e.g., 50% off up to Rs.150 (like Swiggy's first order)
3. Create a `ShoppingCart` class that:
   - Holds items (list of maps with name and price)
   - Has a `setDiscount(strategy)` method to change the strategy
   - Has a `checkout()` method that calculates and displays the final price
4. Demonstrate switching strategies at runtime (simulating a sale starting)

**Test your solution:**
```java
ShoppingCart cart = new ShoppingCart();
cart.addItem("Laptop", 55000);
cart.addItem("Mouse", 500);
cart.addItem("Keyboard", 1500);

cart.setDiscount(new NoDiscount());
System.out.println(cart.checkout());  // Rs.57000

cart.setDiscount(new PercentageDiscount(10));
System.out.println(cart.checkout());  // Rs.51300

cart.setDiscount(new CappedPercentageDiscount(50, 2000));
System.out.println(cart.checkout());  // Rs.55000 (50% of 57000 = 28500, capped at 2000, so 57000-2000)
```

---

### Exercise 2: Sensex Stock Market Observer (Estimated time: 30 minutes)

**What to build:**
A stock price monitoring system where multiple subscribers react when stock prices change. Think of how Zerodha, Groww, and Angel One apps all show you the same stock prices but in different formats.

**Requirements:**
1. Create a `StockExchange` class (the subject/publisher) that:
   - Maintains current prices for stocks (map of stockName -> price)
   - Has `subscribe(stock, observer)` and `unsubscribe(stock, observer)` methods
   - When a stock price changes, notifies all observers subscribed to that stock
2. Create these observer classes:
   - `MobileAppAlert` — shows a push notification with the stock name and new price
   - `WebDashboard` — updates a dashboard display with price and percentage change
   - `EmailDigest` — accumulates changes and shows a summary
   - `PortfolioCalculator` — recalculates total portfolio value based on holdings
3. The `PortfolioCalculator` should accept a map of holdings (e.g., `{"TCS": 10, "INFY": 5}`) and recalculate total value whenever a held stock's price changes

**Test your solution:**
```java
StockExchange exchange = new StockExchange();
exchange.setPrice("TCS", 3500);
exchange.setPrice("INFY", 1800);
exchange.setPrice("RELIANCE", 2500);

MobileAppAlert mobile = new MobileAppAlert();
PortfolioCalculator portfolio = new PortfolioCalculator(Map.of("TCS", 10, "INFY", 5));

exchange.subscribe("TCS", mobile);
exchange.subscribe("TCS", portfolio);
exchange.subscribe("INFY", portfolio);

exchange.updatePrice("TCS", 3600);
// MobileAppAlert: TCS price changed to Rs.3600
// PortfolioCalculator: Portfolio value = Rs.45000 (TCS:10*3600 + INFY:5*1800)

exchange.updatePrice("INFY", 1750);
// PortfolioCalculator: Portfolio value = Rs.44750 (TCS:10*3600 + INFY:5*1750)
```

---

### Exercise 3: Text Editor with Undo/Redo (Estimated time: 35 minutes)

**What to build:**
Extend the Command pattern text editor to support these additional features:

1. **Bold formatting command** — wraps selected text in `**` markers (for Markdown bold)
2. **Batch commands** — group multiple commands into one undo unit (e.g., "Find and Replace All" is one undo action even though it makes 5 replacements)
3. A `showHistory()` method that displays all commands in the history stack
4. A `showRedoStack()` method that shows what can be re-done

**Test your solution:**
```java
TextEditor editor = new TextEditor();
editor.typeText("Hello World Hello India Hello Python");
editor.showHistory();  // 1 command

// Replace all "Hello" with "Namaste" as one batch
editor.batchReplace("Hello", "Namaste");
System.out.println(editor.getContent());  // "Namaste World Namaste India Namaste Python"
editor.showHistory();  // 2 commands (the batch counts as 1)

// One undo should revert ALL replacements
editor.undo();
System.out.println(editor.getContent());  // "Hello World Hello India Hello Python"
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
| driverFound() | -> DRIVER_ASSIGNED | "Already assigned" | N/A | N/A | N/A |
| driverArrived() | N/A | -> WAITING | "Already waiting" | N/A | N/A |
| startRide() | N/A | N/A | -> IN_RIDE | "Already riding" | N/A |
| endRide() | N/A | N/A | N/A | -> COMPLETED | N/A |
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
