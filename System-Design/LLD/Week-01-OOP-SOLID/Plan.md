# LLD Week 1: OOP Concepts & SOLID Principles

---

## What is LLD (Low Level Design)?

Imagine you're building a house. **HLD** is the blueprint — how many rooms, where the kitchen goes, where the bathroom is. **LLD** is the detailed plan — what kind of tiles in the bathroom, where each electrical switch goes, what material for the doors.

In software:
- **HLD** = Which servers, databases, and services do we need?
- **LLD** = How do we write the actual classes, methods, and relationships in code?

**In Microsoft interviews**, they'll say:
> "Design a Parking Lot system" or "Design an Elevator system"

They want you to think about:
- What are the **objects** in this system? (Car, ParkingSpot, Ticket)
- How do these objects **relate** to each other? (A Car parks in a ParkingSpot)
- What **actions** can each object do? (Car can enter, exit. Ticket can calculate fee.)
- How do you write this **cleanly** so 50 developers can work on it without going crazy?

That's LLD. And the foundation of LLD is **OOP** (Object-Oriented Programming).

---

# Part 1: OOP — The 4 Pillars

## Why OOP Exists — The Problem It Solves

Before OOP, all code was written as one long sequence of instructions (called procedural programming). Imagine writing Zomato's entire app — user login, restaurant listing, ordering, payments, delivery tracking — all in one file, one after another.

**Problems:**
- 10 developers can't work on one file at the same time
- Change the payment logic? You might accidentally break the ordering logic
- Want to add a new payment method? Good luck finding where payment code starts and ends
- Reusing code? Copy-paste the same 200 lines in 15 places

**OOP solves this** by organizing code into **objects** — independent units that have their own data and behavior. Each object is like a person in a company — they have their own job, their own knowledge, and they communicate with others through defined channels.

---

## Pillar 1: Encapsulation — "Keep your secrets safe"

### Real Life Example

Think about an **ATM machine**:
- You can check balance, withdraw money, deposit money
- But you CANNOT directly access the vault inside, change the interest rate, or see other people's accounts
- The ATM gives you **specific buttons** (interface) but hides the **internal machinery** (implementation)

That's encapsulation — **hiding internal details and exposing only what's necessary**.

### Why It Matters

Imagine if anyone could directly change your bank balance variable:
```
sheetal.bankBalance = 999999999;  // Just changed it! No validation!
```

With encapsulation, the only way to change the balance is through controlled methods:
```
sheetal.deposit(5000);     // This checks: is amount positive? Is account active?
sheetal.withdraw(2000);    // This checks: do you have enough balance?
```

### In Code

```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BankAccount {
    private String owner;
    private double balance;  // private makes it hidden from outside
    private List<String> transactionHistory;

    public BankAccount(String owner, double initialBalance) {
        this.owner = owner;
        this.balance = initialBalance;
        this.transactionHistory = new ArrayList<>();
    }

    // Public method — the "ATM button"
    public void deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Amount must be positive!");
            return;
        }
        this.balance += amount;
        this.transactionHistory.add("Deposited: +" + amount);
        System.out.println("Deposited Rs." + amount + ". New balance: Rs." + this.balance);
    }

    // Public method — another "ATM button"
    public void withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Amount must be positive!");
            return;
        }
        if (amount > this.balance) {
            System.out.println("Insufficient balance! You have Rs." + this.balance);
            return;
        }
        this.balance -= amount;
        this.transactionHistory.add("Withdrew: -" + amount);
        System.out.println("Withdrew Rs." + amount + ". New balance: Rs." + this.balance);
    }

    // Public method — controlled access to private data
    public double getBalance() {
        return this.balance;
    }

    public List<String> getStatement() {
        return Collections.unmodifiableList(this.transactionHistory);  // Returns an unmodifiable view, not the original
    }

    // Usage
    public static void main(String[] args) {
        BankAccount account = new BankAccount("Sheetal", 10000);
        account.deposit(5000);        // Works! Goes through validation
        account.withdraw(3000);       // Works! Checks balance first
        account.withdraw(50000);      // "Insufficient balance!" — Protected!
        System.out.println(account.getBalance()); // 12000.0

        // This WON'T work — balance is hidden!
        // account.balance = 999999;  // Compilation error! balance is private
    }
}
```

### Key Takeaway
> **Encapsulation = Data (variables) is private. Access is through public methods only.**
> This prevents bugs, enforces rules, and makes code safer.

---

## Pillar 2: Inheritance — "Don't repeat yourself"

### Real Life Example

Think about **vehicles**:
- ALL vehicles have: brand, color, speed, fuel type
- ALL vehicles can: start, stop, accelerate, brake

Now think about specific vehicles:
- A **Car** has everything above PLUS: number of doors, trunk size
- A **Bike** has everything above PLUS: has sidecar or not
- A **Truck** has everything above PLUS: cargo capacity

Would you write the brand, color, speed code separately for Car, Bike, AND Truck? That's 3x the same code! If you find a bug in the speed calculation, you have to fix it in 3 places.

**Inheritance says**: Write the common stuff ONCE in a parent class (Vehicle), and let children (Car, Bike, Truck) automatically get it.

### In Code

```java
// PARENT class — the common stuff
class Vehicle {
    protected String brand;
    protected String color;
    protected int topSpeed;
    protected String fuelType;
    protected boolean isRunning;

    public Vehicle(String brand, String color, int topSpeed, String fuelType) {
        this.brand = brand;
        this.color = color;
        this.topSpeed = topSpeed;
        this.fuelType = fuelType;
        this.isRunning = false;
    }

    public void start() {
        this.isRunning = true;
        System.out.println(this.brand + " started! Vroom!");
    }

    public void stop() {
        this.isRunning = false;
        System.out.println(this.brand + " stopped.");
    }

    public void describe() {
        System.out.println(this.color + " " + this.brand + " | Top Speed: " + this.topSpeed + " km/h | Fuel: " + this.fuelType);
    }
}


// CHILD class — gets everything from Vehicle + adds its own stuff
class Car extends Vehicle {
    private int numDoors;
    private int trunkLiters;

    public Car(String brand, String color, int topSpeed, String fuelType, int numDoors, int trunkLiters) {
        super(brand, color, topSpeed, fuelType);  // Call parent's constructor
        this.numDoors = numDoors;          // Car-specific
        this.trunkLiters = trunkLiters;    // Car-specific
    }

    public void openTrunk() {  // Car-specific method
        System.out.println("Trunk opened! Capacity: " + this.trunkLiters + " liters");
    }
}


class Bike extends Vehicle {
    private String bikeType;

    public Bike(String brand, String color, int topSpeed, String fuelType, String bikeType) {
        super(brand, color, topSpeed, fuelType);
        this.bikeType = bikeType;  // "sport", "cruiser", "scooter"
    }

    public void wheelie() {  // Bike-specific method
        System.out.println(this.brand + " doing a wheelie!");
    }
}


class Truck extends Vehicle {
    private double cargoTons;

    public Truck(String brand, String color, int topSpeed, String fuelType, double cargoTons) {
        super(brand, color, topSpeed, fuelType);
        this.cargoTons = cargoTons;
    }

    public void loadCargo(double tons) {
        if (tons > this.cargoTons) {
            System.out.println("Too heavy! Max capacity: " + this.cargoTons + " tons");
            return;
        }
        System.out.println("Loaded " + tons + " tons into " + this.brand);
    }
}


// Usage
public class Main {
    public static void main(String[] args) {
        Car myCar = new Car("Hyundai Creta", "White", 180, "Petrol", 5, 433);
        myCar.start();       // Inherited from Vehicle — "Hyundai Creta started! Vroom!"
        myCar.describe();    // Inherited from Vehicle
        myCar.openTrunk();   // Car's own method

        Bike myBike = new Bike("Royal Enfield", "Black", 120, "Petrol", "cruiser");
        myBike.start();      // Inherited from Vehicle
        myBike.wheelie();    // Bike's own method

        // Notice: start(), stop(), describe() were written ONCE but work for all 3!
    }
}
```

### Key Takeaway
> **Inheritance = Child classes get everything from the parent class for free.**
> Write common code once. Specific classes add only what's unique to them.

---

## Pillar 3: Polymorphism — "Same action, different behavior"

### Real Life Example

Think about the word **"open"**:
- "Open" a **door** — you push/pull it
- "Open" a **book** — you flip the cover
- "Open" a **laptop** — you lift the screen
- "Open" a **bottle** — you twist the cap

Same word, same concept ("open"), but the **actual action is different** depending on the object!

In software, this means: you can call the same method name on different objects, and each one does its own thing.

### Why It Matters — A Real Problem

Imagine you're building Zomato. You need to calculate delivery charges for different vehicle types:

**Without polymorphism** (ugly, fragile):
```java
public class DeliveryChargeCalculator {
    public static double calculateDeliveryCharge(String vehicleType, double distance) {
        if (vehicleType.equals("bike")) {
            return distance * 5;
        } else if (vehicleType.equals("car")) {
            return distance * 12;
        } else if (vehicleType.equals("bicycle")) {
            return distance * 3;
        } else if (vehicleType.equals("drone")) {
            return distance * 20;
        }
        // Every new vehicle type = modify this function!
        // What if someone misspells "bike" as "bik"? Silent bug!
        return 0;
    }
}
```

**With polymorphism** (clean, extensible):
```java
abstract class DeliveryVehicle {
    public abstract double calculateCharge(double distance);
}

class BikeDelivery extends DeliveryVehicle {
    public double calculateCharge(double distance) {
        return distance * 5;  // Rs. 5 per km
    }
}

class CarDelivery extends DeliveryVehicle {
    public double calculateCharge(double distance) {
        return distance * 12;  // Rs. 12 per km
    }
}

class DroneDelivery extends DeliveryVehicle {
    public double calculateCharge(double distance) {
        return distance * 20;  // Rs. 20 per km
    }
}


// The BEAUTY of polymorphism:
public class Main {
    public static void processDelivery(DeliveryVehicle vehicle, double distance) {
        double charge = vehicle.calculateCharge(distance);  // Same method call for ANY vehicle!
        System.out.println("Delivery charge: Rs." + charge);
    }

    public static void main(String[] args) {
        // Works with ANY vehicle — present or future!
        processDelivery(new BikeDelivery(), 10);    // Rs.50.0
        processDelivery(new CarDelivery(), 10);     // Rs.120.0
        processDelivery(new DroneDelivery(), 10);   // Rs.200.0

        // Adding a new vehicle? Just add a new class. ZERO changes to existing code!
        processDelivery(new BicycleDelivery(), 10);  // Rs.30.0 — works immediately!
    }
}

class BicycleDelivery extends DeliveryVehicle {
    public double calculateCharge(double distance) {
        return distance * 3;
    }
}
```

### Key Takeaway
> **Polymorphism = Same method name, different behavior depending on the object.**
> You can add new types without changing existing code. This is HUGE for big teams.

---

## Pillar 4: Abstraction — "Hide the complexity, show only what matters"

### Real Life Example

When you **drive a car**:
- You use: steering wheel, accelerator, brake, gear shift
- You DON'T need to know: how the engine combustion works, how fuel injection timing is calculated, how the transmission converts gear ratios

The car **abstracts away** the complexity. You interact with a simple interface (steering, pedals) while thousands of complex parts work behind the scenes.

Another example — **Google Maps**:
- You see: "Enter destination → Get directions"
- Behind the scenes: GPS satellites, road graph algorithms, real-time traffic from millions of phones, machine learning for ETA prediction, cellular triangulation...

You never see this complexity. That's abstraction.

### In Software

Abstraction means: **define WHAT something does, not HOW it does it.** You create a "contract" (abstract class) that says "any payment processor must be able to process payments and issue refunds." Each specific processor (UPI, Card, Wallet) decides HOW to do it.

### In Code

```java
import java.util.HashMap;
import java.util.Map;

// Abstract class — defines WHAT a payment processor does
// But NOT how — that's for each specific processor to decide
abstract class PaymentProcessor {

    public abstract Map<String, String> processPayment(double amount, String customerId);

    public abstract boolean refund(String transactionId);

    public abstract String getTransactionStatus(String transactionId);
}


// Concrete class — defines HOW UPI payment actually works
class UPIPayment extends PaymentProcessor {
    public Map<String, String> processPayment(double amount, String customerId) {
        System.out.println("Opening UPI app...");
        System.out.println("Requesting Rs." + amount + " from " + customerId + "'s UPI ID");
        System.out.println("Waiting for PIN entry...");
        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("method", "UPI");
        result.put("txn_id", "UPI123");
        return result;
    }

    public boolean refund(String transactionId) {
        System.out.println("UPI refund initiated for " + transactionId);
        System.out.println("Amount will be credited in 2 minutes");
        return true;
    }

    public String getTransactionStatus(String transactionId) {
        return "completed";
    }
}


// Concrete class — defines HOW Card payment actually works
class CardPayment extends PaymentProcessor {
    public Map<String, String> processPayment(double amount, String customerId) {
        System.out.println("Charging Rs." + amount + " to card ending in ****4532");
        System.out.println("Contacting bank gateway... OTP sent to registered mobile...");
        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("method", "Card");
        result.put("txn_id", "CARD456");
        return result;
    }

    public boolean refund(String transactionId) {
        System.out.println("Card refund for " + transactionId);
        System.out.println("Refund will appear in 5-7 business days");
        return true;
    }

    public String getTransactionStatus(String transactionId) {
        return "completed";
    }
}


// Your checkout code doesn't care WHICH payment method — it just works!
public class Main {
    public static void checkout(PaymentProcessor payment, double amount, String customerId) {
        Map<String, String> result = payment.processPayment(amount, customerId);
        String status = payment.getTransactionStatus(result.get("txn_id"));
        System.out.println("Payment " + result.get("status") + "! Method: " + result.get("method"));
    }

    public static void main(String[] args) {
        checkout(new UPIPayment(), 499, "sheetal_123");    // UPI flow
        checkout(new CardPayment(), 499, "sheetal_123");    // Card flow
        // Both work! checkout() doesn't know or care about the internal details

        // You CANNOT do this:
        // PaymentProcessor p = new PaymentProcessor();  // ERROR! Abstract class can't be instantiated
        // It's just a blueprint — not a real thing
    }
}
```

### Key Takeaway
> **Abstraction = Define the interface (WHAT), hide the implementation (HOW).**
> The calling code doesn't need to know the internal details.

---

## Quick OOP Summary

| Pillar | One Sentence | Real Life | Software |
|--------|-------------|-----------|----------|
| **Encapsulation** | Keep secrets safe | ATM hides the vault, gives you buttons | Private data, public methods |
| **Inheritance** | Don't repeat yourself | All vehicles share: start, stop, accelerate | Child class gets parent's code free |
| **Polymorphism** | Same action, different behavior | "Open" a door vs "open" a book | Same method name, different implementation |
| **Abstraction** | Hide complexity | Driving a car without knowing engine internals | Abstract class defines WHAT, child defines HOW |

---

# Part 2: SOLID Principles

## What is SOLID?

SOLID is a set of **5 rules** for writing code that doesn't turn into a nightmare when your project grows. These rules were defined by Robert C. Martin (Uncle Bob) and are followed by every major tech company including Microsoft, Google, Amazon.

**Why should you care?** Because in real projects:
- 5-50 developers work on the same codebase simultaneously
- Code lives for years, not days
- Requirements change every week (product manager says "add this feature!")
- Without rules, the code becomes an unmaintainable mess ("spaghetti code")

SOLID prevents spaghetti code. Let's learn each letter.

---
End of Day 1
## S — Single Responsibility Principle (SRP)

### Plain English
> **Every class should have ONE job. ONE reason to change.**

### Real Life Example

Think about people in a restaurant:
- **Chef** — cooks food (that's their ONE job)
- **Waiter** — takes orders and serves food
- **Cashier** — handles payments
- **Cleaner** — cleans tables

Now imagine ONE person does ALL of this. What happens?
- They're overwhelmed and make mistakes
- If the payment system changes (cash → UPI), the same person who cooks needs to learn new payment rules
- If they're sick, EVERYTHING stops — cooking, serving, billing, cleaning
- You can't hire a specialized chef because one person does everything

**SRP says: each class (person) has ONE specific responsibility.**

### Real Software Example

At Zomato, imagine one class that handles everything about an order:

**Bad Code (One class, 6 responsibilities):**
```java
public class OrderManager {
    public void createOrder(List<Item> items, User user) {
        // Creates the order in database
        System.out.println("Order created for " + user);
    }

    public double calculateTotal(List<Item> items) {
        // Calculates price with tax and discount
        double total = items.stream().mapToDouble(Item::getPrice).sum();
        return total * 1.18;  // 18% GST
    }

    public void chargePayment(double amount, String paymentMethod) {
        // Charges the customer
        System.out.println("Charging Rs." + amount + " via " + paymentMethod);
    }

    public void sendConfirmationSms(User user, String orderId) {
        // Sends SMS to customer
        System.out.println("SMS sent to " + user.getPhone() + ": Order #" + orderId + " confirmed!");
    }

    public void notifyRestaurant(Restaurant restaurant, Order order) {
        // Tells restaurant about new order
        System.out.println("New order sent to " + restaurant.getName());
    }

    public void assignDeliveryPartner(Order order) {
        // Finds and assigns delivery person
        System.out.println("Delivery partner assigned for order #" + order.getId());
    }
}
```

**What's wrong?** If the SMS provider changes (Twilio → AWS SNS), you modify `OrderManager`. If the payment logic changes, you modify `OrderManager`. If delivery assignment algorithm changes, you modify `OrderManager`. **One class changes for 6 different reasons!** And every time you change it, you might accidentally break something unrelated.

**Good Code (Each class has ONE job):**
```java
import java.util.List;

// Creates and manages orders — that's it
class OrderService {
    public String createOrder(List<Item> items, User user) {
        double total = calculateTotal(items);
        String orderId = "ORD-" + user.getId() + "-" + System.currentTimeMillis();
        System.out.println("Order " + orderId + " created for " + user.getName() + ". Total: Rs." + total);
        return orderId;
    }

    public double calculateTotal(List<Item> items) {
        double subtotal = items.stream().mapToDouble(Item::getPrice).sum();
        double gst = subtotal * 0.18;
        return subtotal + gst;
    }
}


// Handles payments — that's it
class PaymentService {
    public Map<String, String> charge(double amount, String paymentMethod, String customerId) {
        System.out.println("Charging Rs." + amount + " via " + paymentMethod + " for customer " + customerId);
        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("txn_id", "TXN123");
        return result;
    }
}


// Sends notifications — that's it
class NotificationService {
    public void sendOrderConfirmation(String userPhone, String orderId) {
        System.out.println("SMS to " + userPhone + ": Your order #" + orderId + " is confirmed!");
    }

    public void notifyRestaurant(String restaurantPhone, String orderId) {
        System.out.println("Alert to restaurant: New order #" + orderId);
    }
}


// Manages delivery partners — that's it
class DeliveryService {
    public Map<String, String> assignPartner(String orderId, String pickupLocation, String dropLocation) {
        System.out.println("Finding nearest delivery partner for #" + orderId + "...");
        Map<String, String> result = new HashMap<>();
        result.put("partner", "Rahul");
        result.put("eta", "15 min");
        return result;
    }
}
```

Now if SMS provider changes → only `NotificationService` changes.
Payment logic changes → only `PaymentService` changes.
Other classes are completely untouched. Safe!

---

## O — Open/Closed Principle (OCP)

### Plain English
> **Open for EXTENSION, Closed for MODIFICATION.**
> You should add new features WITHOUT changing existing code that already works.

### Real Life Example

Think about **electrical sockets** in your house:
- The socket is CLOSED — you never modify the wiring when you buy a new appliance
- But it's OPEN for extension — you can plug in a TV, phone charger, iron, mixer, laptop — anything with a compatible plug!

The socket's "interface" (3-pin plug shape) stays the same forever. New appliances extend the system without touching existing wiring.

Another example — **USB ports**:
- Your laptop's USB port was designed years ago
- Today you plug in devices that didn't exist back then (USB-C drives, VR headsets)
- The port didn't change. New devices just follow the USB standard.

### Real Software Example

You're building a discount system for Flipkart:

**Bad Code (Must modify existing code for every new discount type):**
```java
public class DiscountCalculator {
    public double calculate(String customerType, double amount) {
        if (customerType.equals("regular")) {
            return amount * 0.05;          // 5% off
        } else if (customerType.equals("premium")) {
            return amount * 0.15;          // 15% off
        } else if (customerType.equals("gold")) {
            return amount * 0.20;          // 20% off
        }
        // Product manager says "add student discount!"
        // You MUST modify this existing method
        // What if you accidentally break the "premium" logic while adding "student"?
        // What if there are 20 discount types? This becomes a 200-line if/elif chain!
        return 0;
    }
}
```

**Good Code (Add new discounts without touching existing code):**
```java
// The "socket" — defines what a discount must do
abstract class DiscountStrategy {
    public abstract double calculate(double amount);
    public abstract String description();
}


class RegularDiscount extends DiscountStrategy {
    public double calculate(double amount) {
        return amount * 0.05;
    }
    public String description() {
        return "Regular member: 5% off";
    }
}

class PremiumDiscount extends DiscountStrategy {
    public double calculate(double amount) {
        return amount * 0.15;
    }
    public String description() {
        return "Premium member: 15% off";
    }
}

class GoldDiscount extends DiscountStrategy {
    public double calculate(double amount) {
        return amount * 0.20;
    }
    public String description() {
        return "Gold member: 20% off";
    }
}


public class Main {
    // The checkout code — NEVER needs to change!
    public static void applyDiscount(DiscountStrategy strategy, double amount) {
        double discount = strategy.calculate(amount);
        double finalAmount = amount - discount;
        System.out.println(strategy.description());
        System.out.println("Original: Rs." + amount + " | Discount: Rs." + discount + " | Pay: Rs." + finalAmount);
    }

    public static void main(String[] args) {
        applyDiscount(new GoldDiscount(), 10000);
        // Gold member: 20% off
        // Original: Rs.10000.0 | Discount: Rs.2000.0 | Pay: Rs.8000.0


        // 3 months later: "Add student discount!" — just add a NEW class!
        applyDiscount(new StudentDiscount(), 10000);  // Works immediately!
    }
}

// ZERO changes to existing code. Existing discounts still work perfectly.
class StudentDiscount extends DiscountStrategy {
    public double calculate(double amount) {
        return amount * 0.25;  // 25% off for students
    }
    public String description() {
        return "Student discount: 25% off";
    }
}
```

### Key Takeaway
> **Existing code that works should NEVER be modified to add new features.**
> New features = new classes that follow the same interface.

---

## L — Liskov Substitution Principle (LSP)

### Plain English
> **If you replace a parent with its child, everything should still work correctly.**
> A child class must be able to do everything the parent promises.

### Real Life Example

You order food from Zomato. They promise "a restaurant that serves food."
- **South Indian restaurant** → serves dosa, idli → great, promise kept!
- **Pizza restaurant** → serves pizza, pasta → great, promise kept!
- **Cloud kitchen that's permanently closed** → serves nothing → PROMISE BROKEN!

If something claims to be a "restaurant", it MUST serve food. If it can't, it shouldn't be listed as a restaurant.

Your TV remote says it works with "any TV":
- Samsung TV → all buttons work
- LG TV → all buttons work
- A microwave (that someone labeled as "TV") → volume button causes explosion!

**LSP says:** A child must honor every promise the parent makes. If it can't, the class hierarchy is wrong.

### Real Software Example

**Bad Code (Penguin breaks Bird's promise):**
```java
class Bird {
    public void fly() {
        System.out.println("Flying high!");
    }

    public void eat() {
        System.out.println("Eating food");
    }
}

class Sparrow extends Bird {
    @Override
    public void fly() {
        System.out.println("Sparrow flying!");  // Works fine!
    }
}

class Penguin extends Bird {
    @Override
    public void fly() {
        throw new UnsupportedOperationException("I can't fly!");
        // BREAKS the promise!
        // Any code that has a list of Birds and calls .fly()
        // will CRASH when it hits a Penguin!
    }
}
```

Imagine this code somewhere in the app:
```java
public static void birdShow(List<Bird> birds) {
    for (Bird bird : birds) {
        bird.fly();  // Works for Sparrow, Eagle, Parrot... CRASHES on Penguin!
    }
}
```

**Good Code (Honest class hierarchy):**
```java
import java.util.List;

// All birds can eat and make sounds
abstract class Bird {
    public abstract void eat();
    public abstract void makeSound();
}

// Only birds that CAN fly
abstract class FlyingBird extends Bird {
    public abstract void fly();
}

// Only birds that swim
abstract class SwimmingBird extends Bird {
    public abstract void swim();
}

class Sparrow extends FlyingBird {
    public void eat() { System.out.println("Sparrow eating seeds"); }
    public void makeSound() { System.out.println("Chirp chirp!"); }
    public void fly() { System.out.println("Sparrow soaring!"); }
}

class Penguin extends SwimmingBird {
    public void eat() { System.out.println("Penguin eating fish"); }
    public void makeSound() { System.out.println("Squawk!"); }
    public void swim() { System.out.println("Penguin diving deep!"); }
    // No fly() method — because Penguin never promised it could fly!
}

public class Main {
    // Now this is SAFE:
    public static void flyingBirdShow(List<FlyingBird> birds) {
        for (FlyingBird bird : birds) {
            bird.fly();  // Only FlyingBirds here — no Penguins, no crashes!
        }
    }
}
```

---

## I — Interface Segregation Principle (ISP)

### Plain English
> **Don't force a class to implement methods it doesn't need.**
> Make small, focused interfaces instead of one big one.

### Real Life Example

Imagine a government form that asks EVERYONE to fill:
- Name, Aadhaar number, address *(everyone needs this)*
- Driving license number *(only drivers need this)*
- Medical license number *(only doctors need this)*
- FSSAI license *(only restaurant owners need this)*
- Bar council number *(only lawyers need this)*

A software engineer filling this form writes "N/A" for driving license, medical license, FSSAI, and bar council. **Forced to deal with 4 irrelevant fields!**

**ISP says:** Make separate sections. Common section for everyone. Driver section only for drivers. Doctor section only for doctors. You only fill what's relevant to you.

### Real Software Example

**Bad Code (One fat interface forces useless implementations):**
```java
interface SmartDevice {
    void makeCall();
    void sendText();
    void browseInternet();
    void takePhoto();
    void scanFingerprint();
}

class iPhone implements SmartDevice {
    public void makeCall() { System.out.println("Calling via iPhone..."); }
    public void sendText() { System.out.println("iMessage sent!"); }
    public void browseInternet() { System.out.println("Safari opened!"); }
    public void takePhoto() { System.out.println("Click! Photo taken!"); }
    public void scanFingerprint() { System.out.println("Touch ID verified!"); }
    // iPhone can do ALL of these — no problem!
}

class NokiaBasicPhone implements SmartDevice {
    public void makeCall() { System.out.println("Calling..."); }
    public void sendText() { System.out.println("SMS sent!"); }
    public void browseInternet() { }         // Can't do this!
    public void takePhoto() { }               // Can't do this!
    public void scanFingerprint() { }         // Can't do this!
    // Nokia is FORCED to implement 3 methods it can't do. Ugly!
}
```

**Good Code (Small, focused interfaces):**
```java
// Can make phone calls
interface Callable {
    void makeCall();
}

// Can send text messages
interface Messageable {
    void sendText();
}

// Can browse the internet
interface InternetEnabled {
    void browseInternet();
}

// Has a camera
interface CameraEnabled {
    void takePhoto();
}

// Has fingerprint/face scanner
interface BiometricEnabled {
    void scanFingerprint();
}


// iPhone implements ALL (because it CAN do all)
class iPhone implements Callable, Messageable, InternetEnabled, CameraEnabled, BiometricEnabled {
    public void makeCall() { System.out.println("Calling via iPhone..."); }
    public void sendText() { System.out.println("iMessage sent!"); }
    public void browseInternet() { System.out.println("Safari opened!"); }
    public void takePhoto() { System.out.println("Click!"); }
    public void scanFingerprint() { System.out.println("Touch ID!"); }
}

// Nokia implements ONLY what it can actually do
class NokiaBasicPhone implements Callable, Messageable {
    public void makeCall() { System.out.println("Calling..."); }
    public void sendText() { System.out.println("SMS sent!"); }
    // No fake empty methods! Clean and honest.
}
```

---

## D — Dependency Inversion Principle (DIP)

### Plain English
> **Don't depend on specific implementations. Depend on abstractions.**
> High-level business logic shouldn't be tied to low-level details.

### Real Life Example

Think about **laptop chargers**:
- Your laptop has a USB-C port (this is the abstraction/interface)
- You can plug in: Apple charger, Samsung charger, Anker charger, boat charger — ANY USB-C charger
- Your laptop doesn't care WHO made the charger — just that it follows the USB-C standard

Now imagine your laptop was hardwired to ONLY work with one specific Apple charger (serial number A1234). If that charger breaks, your entire laptop is useless. You can't use ANY other charger, even if it outputs the same power.

**DIP says:** Depend on the standard (USB-C), not the specific product (Apple charger model A1234).

### Real Software Example

**Bad Code (UserService is married to MySQL forever):**
```java
class MySQLDatabase {
    public void save(Map<String, String> data) {
        System.out.println("INSERT INTO users VALUES ('" + data.get("name") + "', '" + data.get("email") + "')");
    }
}

class UserService {
    private MySQLDatabase db;

    public UserService() {
        this.db = new MySQLDatabase();  // HARDCODED! Stuck with MySQL forever!
    }

    public void createUser(String name, String email) {
        Map<String, String> data = new HashMap<>();
        data.put("name", name);
        data.put("email", email);
        this.db.save(data);
    }
}

// Problems:
// 1. Want to switch to MongoDB? Must CHANGE UserService
// 2. Want to test without a real database? Must CHANGE UserService
// 3. UserService (business logic) is tied to MySQL (infrastructure detail)
```

**Good Code (UserService depends on abstraction, works with ANY database):**
```java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// The abstraction (the "USB-C standard")
interface Database {
    void save(Map<String, String> data);
    void find(String query);
    void delete(String id);
}


// Implementation 1: MySQL
class MySQLDatabase implements Database {
    public void save(Map<String, String> data) {
        System.out.println("MySQL: INSERT INTO users VALUES (...)");
    }

    public void find(String query) {
        System.out.println("MySQL: SELECT * FROM users WHERE " + query);
    }

    public void delete(String id) {
        System.out.println("MySQL: DELETE FROM users WHERE id = " + id);
    }
}


// Implementation 2: MongoDB
class MongoDatabase implements Database {
    public void save(Map<String, String> data) {
        System.out.println("MongoDB: db.users.insertOne(" + data + ")");
    }

    public void find(String query) {
        System.out.println("MongoDB: db.users.find(" + query + ")");
    }

    public void delete(String id) {
        System.out.println("MongoDB: db.users.deleteOne({_id: " + id + "})");
    }
}


// Implementation 3: Fake database for testing (no real DB needed!)
class FakeDatabase implements Database {
    private List<Map<String, String>> storage = new ArrayList<>();

    public void save(Map<String, String> data) {
        storage.add(data);
        System.out.println("Fake DB: Stored " + data);
    }

    public void find(String query) {
        List<Map<String, String>> results = storage.stream()
            .filter(d -> d.toString().contains(query))
            .collect(Collectors.toList());
        System.out.println("Fake DB: Found " + results);
    }

    public void delete(String id) {
        storage.removeIf(d -> id.equals(d.get("id")));
    }
}


// UserService depends on the ABSTRACTION (Database), not specific implementation
class UserService {
    private Database db;

    public UserService(Database db) {  // Accepts ANY database!
        this.db = db;
    }

    public void createUser(String name, String email) {
        Map<String, String> data = new HashMap<>();
        data.put("name", name);
        data.put("email", email);
        this.db.save(data);
    }

    public void findUser(String email) {
        this.db.find("email=" + email);
    }
}


public class Main {
    public static void main(String[] args) {
        // Production — use MongoDB
        UserService service = new UserService(new MongoDatabase());
        service.createUser("Sheetal", "sheetal@gmail.com");

        // Testing — use fake DB (fast, no real database needed)
        UserService testService = new UserService(new FakeDatabase());
        testService.createUser("Test User", "test@test.com");

        // Switching databases? Change ONE line where you create UserService.
        // UserService itself doesn't change AT ALL.
    }
}
```

---

## SOLID Summary — Remember This Table

| Letter | Principle | Restaurant Analogy | When You Violate It |
|--------|-----------|-------------------|---------------------|
| **S** | Single Responsibility | Chef only cooks. Waiter only serves. Cashier only bills. | One class doing 10 things. Change SMS provider → touching order logic. |
| **O** | Open/Closed | New menu item = new recipe card. Don't rewrite the whole menu. | Adding "student discount" requires modifying the discount if/elif chain. |
| **L** | Liskov Substitution | If it's listed as "restaurant", it must serve food. Period. | Penguin.fly() throws exception. Child breaks parent's promise. |
| **I** | Interface Segregation | Different forms for drivers, doctors, lawyers. Not one mega-form for everyone. | Nokia forced to implement `browse_internet()` with an empty method. |
| **D** | Dependency Inversion | Laptop uses USB-C standard, not hardwired to one specific charger. | UserService hardcodes MySQL. Can't switch DB or test without real DB. |

---

## Practice Exercises

### Exercise 1: Identify OOP Pillars
For each scenario, name which OOP pillar is being used:
1. A `Logger` class that hides whether it writes to a file, console, or cloud — you just call `log(message)`
2. A `Shape` class with `Circle`, `Rectangle`, `Triangle` subclasses that all have an `area()` method but calculate it differently
3. `Dog`, `Cat`, `Fish` all inherit `name`, `age`, `eat()` from `Animal` class
4. A class where you can call `get_balance()` but can't directly access `__balance`

### Exercise 2: Spot the SOLID Violations
```java
public class ReportGenerator {
    public void fetchDataFromDatabase() { /* ... */ }
    public void processData() { /* ... */ }
    public void generatePdf() { /* ... */ }
    public void sendEmailWithReport() { /* ... */ }
    public void uploadToS3() { /* ... */ }
    public void notifySlack() { /* ... */ }
}
```
1. Which SOLID principle does this violate? Why?
2. How would you refactor it? Draw out the new classes.

### Exercise 3: Design a Notification System
Using SOLID principles, design classes for a notification system that can send:
- Email notifications
- SMS notifications
- Push notifications
- WhatsApp notifications

Requirements:
- Adding a new notification type (Telegram) should NOT change any existing code (OCP)
- Each type has its own sending logic (Polymorphism)
- The main app code shouldn't know which type is being used (DIP)
- Write the full classes with methods.

### Exercise 4: Real-World Refactoring
This code violates MULTIPLE SOLID principles. Identify each violation and rewrite it properly:
```java
public class OnlineStore {
    public void addProduct(String name, double price) { /* ... */ }
    public void removeProduct(String productId) { /* ... */ }
    public double calculateTotal(Cart cart) { /* ... */ return 0; }
    public void applyDiscount(Cart cart, String discountType) {
        if (discountType.equals("percentage")) {
            // percentage logic
        } else if (discountType.equals("flat")) {
            // flat discount logic
        } else if (discountType.equals("bogo")) {
            // buy one get one logic
        }
    }
    public void chargeCreditCard(double amount, String cardNumber) { /* ... */ }
    public void chargeUpi(double amount, String upiId) { /* ... */ }
    public void sendOrderConfirmation(Order order) { /* ... */ }
    public void generateInvoice(Order order) { /* ... */ }
    public void updateInventory(String productId, int quantity) { /* ... */ }
    public void trackDelivery(String orderId) { /* ... */ }
}
```

Violations to find:
- SRP: How many responsibilities does this class have?
- OCP: What happens when you add a new discount type or payment method?
- DIP: Is payment logic abstracted or hardcoded?

---

## Self-Check Before Week 2

- [ ] Can explain all 4 OOP pillars with your OWN real-life examples (not from this doc)
- [ ] Can explain all 5 SOLID principles with your OWN examples
- [ ] Can spot OOP and SOLID violations in any code
- [ ] Completed all 4 exercises
- [ ] Understand WHY these principles exist, not just what they are
- [ ] Can answer: "If someone asks in an interview — what is the Open/Closed principle and why does it matter?" — confidently, with a real example
