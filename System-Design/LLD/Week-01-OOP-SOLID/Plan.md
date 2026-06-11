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
sheetal.bank_balance = 999999999  # Just changed it! No validation!
```

With encapsulation, the only way to change the balance is through controlled methods:
```
sheetal.deposit(5000)     # This checks: is amount positive? Is account active?
sheetal.withdraw(2000)    # This checks: do you have enough balance?
```

### In Code

```python
class BankAccount:
    def __init__(self, owner, initial_balance):
        self.owner = owner
        self.__balance = initial_balance  # __ makes it PRIVATE (hidden from outside)
        self.__transaction_history = []

    # Public method — the "ATM button"
    def deposit(self, amount):
        if amount <= 0:
            print("Amount must be positive!")
            return
        self.__balance += amount
        self.__transaction_history.append(f"Deposited: +{amount}")
        print(f"Deposited Rs.{amount}. New balance: Rs.{self.__balance}")

    # Public method — another "ATM button"
    def withdraw(self, amount):
        if amount <= 0:
            print("Amount must be positive!")
            return
        if amount > self.__balance:
            print(f"Insufficient balance! You have Rs.{self.__balance}")
            return
        self.__balance -= amount
        self.__transaction_history.append(f"Withdrew: -{amount}")
        print(f"Withdrew Rs.{amount}. New balance: Rs.{self.__balance}")

    # Public method — controlled access to private data
    def get_balance(self):
        return self.__balance

    def get_statement(self):
        return self.__transaction_history.copy()  # Returns a COPY, not the original


# Usage
account = BankAccount("Sheetal", 10000)
account.deposit(5000)        # Works! Goes through validation
account.withdraw(3000)       # Works! Checks balance first
account.withdraw(50000)      # "Insufficient balance!" — Protected!
print(account.get_balance()) # 12000

# This WON'T work — balance is hidden!
# account.__balance = 999999  # AttributeError!
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

```python
# PARENT class — the common stuff
class Vehicle:
    def __init__(self, brand, color, top_speed, fuel_type):
        self.brand = brand
        self.color = color
        self.top_speed = top_speed
        self.fuel_type = fuel_type
        self.is_running = False

    def start(self):
        self.is_running = True
        print(f"{self.brand} started! Vroom!")

    def stop(self):
        self.is_running = False
        print(f"{self.brand} stopped.")

    def describe(self):
        print(f"{self.color} {self.brand} | Top Speed: {self.top_speed} km/h | Fuel: {self.fuel_type}")


# CHILD class — gets everything from Vehicle + adds its own stuff
class Car(Vehicle):
    def __init__(self, brand, color, top_speed, fuel_type, num_doors, trunk_liters):
        super().__init__(brand, color, top_speed, fuel_type)  # Call parent's __init__
        self.num_doors = num_doors          # Car-specific
        self.trunk_liters = trunk_liters    # Car-specific

    def open_trunk(self):  # Car-specific method
        print(f"Trunk opened! Capacity: {self.trunk_liters} liters")


class Bike(Vehicle):
    def __init__(self, brand, color, top_speed, fuel_type, bike_type):
        super().__init__(brand, color, top_speed, fuel_type)
        self.bike_type = bike_type  # "sport", "cruiser", "scooter"

    def wheelie(self):  # Bike-specific method
        print(f"{self.brand} doing a wheelie!")


class Truck(Vehicle):
    def __init__(self, brand, color, top_speed, fuel_type, cargo_tons):
        super().__init__(brand, color, top_speed, fuel_type)
        self.cargo_tons = cargo_tons

    def load_cargo(self, tons):
        if tons > self.cargo_tons:
            print(f"Too heavy! Max capacity: {self.cargo_tons} tons")
            return
        print(f"Loaded {tons} tons into {self.brand}")


# Usage
my_car = Car("Hyundai Creta", "White", 180, "Petrol", 5, 433)
my_car.start()       # Inherited from Vehicle — "Hyundai Creta started! Vroom!"
my_car.describe()    # Inherited from Vehicle
my_car.open_trunk()  # Car's own method

my_bike = Bike("Royal Enfield", "Black", 120, "Petrol", "cruiser")
my_bike.start()      # Inherited from Vehicle
my_bike.wheelie()    # Bike's own method

# Notice: start(), stop(), describe() were written ONCE but work for all 3!
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
```python
def calculate_delivery_charge(vehicle_type, distance):
    if vehicle_type == "bike":
        return distance * 5
    elif vehicle_type == "car":
        return distance * 12
    elif vehicle_type == "bicycle":
        return distance * 3
    elif vehicle_type == "drone":
        return distance * 20
    # Every new vehicle type = modify this function!
    # What if someone misspells "bike" as "bik"? Silent bug!
```

**With polymorphism** (clean, extensible):
```python
class DeliveryVehicle:
    def calculate_charge(self, distance):
        raise NotImplementedError("Each vehicle must define its charge!")

class BikeDelivery(DeliveryVehicle):
    def calculate_charge(self, distance):
        return distance * 5  # Rs. 5 per km

class CarDelivery(DeliveryVehicle):
    def calculate_charge(self, distance):
        return distance * 12  # Rs. 12 per km

class DroneDelivery(DeliveryVehicle):
    def calculate_charge(self, distance):
        return distance * 20  # Rs. 20 per km


# The BEAUTY of polymorphism:
def process_delivery(vehicle: DeliveryVehicle, distance):
    charge = vehicle.calculate_charge(distance)  # Same method call for ANY vehicle!
    print(f"Delivery charge: Rs.{charge}")

# Works with ANY vehicle — present or future!
process_delivery(BikeDelivery(), 10)    # Rs.50
process_delivery(CarDelivery(), 10)     # Rs.120
process_delivery(DroneDelivery(), 10)   # Rs.200

# Adding a new vehicle? Just add a new class. ZERO changes to existing code!
class BicycleDelivery(DeliveryVehicle):
    def calculate_charge(self, distance):
        return distance * 3

process_delivery(BicycleDelivery(), 10)  # Rs.30 — works immediately!
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

```python
from abc import ABC, abstractmethod

# Abstract class — defines WHAT a payment processor does
# But NOT how — that's for each specific processor to decide
class PaymentProcessor(ABC):

    @abstractmethod
    def process_payment(self, amount, customer_id):
        """Process a payment. Each processor implements this differently."""
        pass

    @abstractmethod
    def refund(self, transaction_id):
        """Refund a payment."""
        pass

    @abstractmethod
    def get_transaction_status(self, transaction_id):
        """Check if a transaction succeeded."""
        pass


# Concrete class — defines HOW UPI payment actually works
class UPIPayment(PaymentProcessor):
    def process_payment(self, amount, customer_id):
        print(f"Opening UPI app...")
        print(f"Requesting Rs.{amount} from {customer_id}'s UPI ID")
        print("Waiting for PIN entry...")
        return {"status": "success", "method": "UPI", "txn_id": "UPI123"}

    def refund(self, transaction_id):
        print(f"UPI refund initiated for {transaction_id}")
        print("Amount will be credited in 2 minutes")
        return True

    def get_transaction_status(self, transaction_id):
        return "completed"


# Concrete class — defines HOW Card payment actually works
class CardPayment(PaymentProcessor):
    def process_payment(self, amount, customer_id):
        print(f"Charging Rs.{amount} to card ending in ****4532")
        print("Contacting bank gateway... OTP sent to registered mobile...")
        return {"status": "success", "method": "Card", "txn_id": "CARD456"}

    def refund(self, transaction_id):
        print(f"Card refund for {transaction_id}")
        print("Refund will appear in 5-7 business days")
        return True

    def get_transaction_status(self, transaction_id):
        return "completed"


# Your checkout code doesn't care WHICH payment method — it just works!
def checkout(payment: PaymentProcessor, amount, customer_id):
    result = payment.process_payment(amount, customer_id)
    status = payment.get_transaction_status(result['txn_id'])
    print(f"Payment {result['status']}! Method: {result['method']}")

checkout(UPIPayment(), 499, "sheetal_123")    # UPI flow
checkout(CardPayment(), 499, "sheetal_123")    # Card flow
# Both work! checkout() doesn't know or care about the internal details

# You CANNOT do this:
# p = PaymentProcessor()  # ERROR! Abstract class can't be created directly
# It's just a blueprint — not a real thing
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
```python
class OrderManager:
    def create_order(self, items, user):
        # Creates the order in database
        print(f"Order created for {user}")

    def calculate_total(self, items):
        # Calculates price with tax and discount
        total = sum(item.price for item in items)
        return total * 1.18  # 18% GST

    def charge_payment(self, amount, payment_method):
        # Charges the customer
        print(f"Charging Rs.{amount} via {payment_method}")

    def send_confirmation_sms(self, user, order_id):
        # Sends SMS to customer
        print(f"SMS sent to {user.phone}: Order #{order_id} confirmed!")

    def notify_restaurant(self, restaurant, order):
        # Tells restaurant about new order
        print(f"New order sent to {restaurant.name}")

    def assign_delivery_partner(self, order):
        # Finds and assigns delivery person
        print(f"Delivery partner assigned for order #{order.id}")
```

**What's wrong?** If the SMS provider changes (Twilio → AWS SNS), you modify `OrderManager`. If the payment logic changes, you modify `OrderManager`. If delivery assignment algorithm changes, you modify `OrderManager`. **One class changes for 6 different reasons!** And every time you change it, you might accidentally break something unrelated.

**Good Code (Each class has ONE job):**
```python
class OrderService:
    """Creates and manages orders — that's it"""
    def create_order(self, items, user):
        total = self.calculate_total(items)
        order_id = f"ORD-{user.id}-{int(time.time())}"
        print(f"Order {order_id} created for {user.name}. Total: Rs.{total}")
        return order_id

    def calculate_total(self, items):
        subtotal = sum(item.price for item in items)
        gst = subtotal * 0.18
        return subtotal + gst


class PaymentService:
    """Handles payments — that's it"""
    def charge(self, amount, payment_method, customer_id):
        print(f"Charging Rs.{amount} via {payment_method} for customer {customer_id}")
        return {"status": "success", "txn_id": "TXN123"}


class NotificationService:
    """Sends notifications — that's it"""
    def send_order_confirmation(self, user_phone, order_id):
        print(f"SMS to {user_phone}: Your order #{order_id} is confirmed!")

    def notify_restaurant(self, restaurant_phone, order_id):
        print(f"Alert to restaurant: New order #{order_id}")


class DeliveryService:
    """Manages delivery partners — that's it"""
    def assign_partner(self, order_id, pickup_location, drop_location):
        print(f"Finding nearest delivery partner for #{order_id}...")
        return {"partner": "Rahul", "eta": "15 min"}
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
```python
class DiscountCalculator:
    def calculate(self, customer_type, amount):
        if customer_type == "regular":
            return amount * 0.05          # 5% off
        elif customer_type == "premium":
            return amount * 0.15          # 15% off
        elif customer_type == "gold":
            return amount * 0.20          # 20% off
        # Product manager says "add student discount!"
        # You MUST modify this existing method
        # What if you accidentally break the "premium" logic while adding "student"?
        # What if there are 20 discount types? This becomes a 200-line if/elif chain!
```

**Good Code (Add new discounts without touching existing code):**
```python
from abc import ABC, abstractmethod

class DiscountStrategy(ABC):
    """The "socket" — defines what a discount must do"""
    @abstractmethod
    def calculate(self, amount):
        pass

    @abstractmethod
    def description(self):
        pass


class RegularDiscount(DiscountStrategy):
    def calculate(self, amount):
        return amount * 0.05
    def description(self):
        return "Regular member: 5% off"

class PremiumDiscount(DiscountStrategy):
    def calculate(self, amount):
        return amount * 0.15
    def description(self):
        return "Premium member: 15% off"

class GoldDiscount(DiscountStrategy):
    def calculate(self, amount):
        return amount * 0.20
    def description(self):
        return "Gold member: 20% off"


# The checkout code — NEVER needs to change!
def apply_discount(strategy: DiscountStrategy, amount):
    discount = strategy.calculate(amount)
    final = amount - discount
    print(f"{strategy.description()}")
    print(f"Original: Rs.{amount} | Discount: Rs.{discount} | Pay: Rs.{final}")

apply_discount(GoldDiscount(), 10000)
# Gold member: 20% off
# Original: Rs.10000 | Discount: Rs.2000 | Pay: Rs.8000


# 3 months later: "Add student discount!" — just add a NEW class!
class StudentDiscount(DiscountStrategy):
    def calculate(self, amount):
        return amount * 0.25  # 25% off for students
    def description(self):
        return "Student discount: 25% off"

# ZERO changes to existing code. Existing discounts still work perfectly.
apply_discount(StudentDiscount(), 10000)  # Works immediately!
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
```python
class Bird:
    def fly(self):
        print("Flying high!")

    def eat(self):
        print("Eating food")

class Sparrow(Bird):
    def fly(self):
        print("Sparrow flying!")  # Works fine!

class Penguin(Bird):
    def fly(self):
        raise Exception("I can't fly!")
        # BREAKS the promise!
        # Any code that has a list of Birds and calls .fly()
        # will CRASH when it hits a Penguin!
```

Imagine this code somewhere in the app:
```python
def bird_show(birds):
    for bird in birds:
        bird.fly()  # Works for Sparrow, Eagle, Parrot... CRASHES on Penguin!
```

**Good Code (Honest class hierarchy):**
```python
class Bird(ABC):
    """All birds can eat and make sounds"""
    @abstractmethod
    def eat(self): pass

    @abstractmethod
    def make_sound(self): pass

class FlyingBird(Bird):
    """Only birds that CAN fly"""
    @abstractmethod
    def fly(self): pass

class SwimmingBird(Bird):
    """Only birds that swim"""
    @abstractmethod
    def swim(self): pass

class Sparrow(FlyingBird):
    def eat(self): print("Sparrow eating seeds")
    def make_sound(self): print("Chirp chirp!")
    def fly(self): print("Sparrow soaring!")

class Penguin(SwimmingBird):
    def eat(self): print("Penguin eating fish")
    def make_sound(self): print("Squawk!")
    def swim(self): print("Penguin diving deep!")
    # No fly() method — because Penguin never promised it could fly!

# Now this is SAFE:
def flying_bird_show(birds: list[FlyingBird]):
    for bird in birds:
        bird.fly()  # Only FlyingBirds here — no Penguins, no crashes!
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
```python
class SmartDevice(ABC):
    @abstractmethod
    def make_call(self): pass

    @abstractmethod
    def send_text(self): pass

    @abstractmethod
    def browse_internet(self): pass

    @abstractmethod
    def take_photo(self): pass

    @abstractmethod
    def scan_fingerprint(self): pass

class iPhone(SmartDevice):
    def make_call(self): print("Calling via iPhone...")
    def send_text(self): print("iMessage sent!")
    def browse_internet(self): print("Safari opened!")
    def take_photo(self): print("Click! Photo taken!")
    def scan_fingerprint(self): print("Touch ID verified!")
    # iPhone can do ALL of these — no problem!

class NokiaBasicPhone(SmartDevice):
    def make_call(self): print("Calling...")
    def send_text(self): print("SMS sent!")
    def browse_internet(self): pass         # Can't do this!
    def take_photo(self): pass               # Can't do this!
    def scan_fingerprint(self): pass         # Can't do this!
    # Nokia is FORCED to implement 3 methods it can't do. Ugly!
```

**Good Code (Small, focused interfaces):**
```python
class Callable(ABC):
    """Can make phone calls"""
    @abstractmethod
    def make_call(self): pass

class Messageable(ABC):
    """Can send text messages"""
    @abstractmethod
    def send_text(self): pass

class InternetEnabled(ABC):
    """Can browse the internet"""
    @abstractmethod
    def browse_internet(self): pass

class CameraEnabled(ABC):
    """Has a camera"""
    @abstractmethod
    def take_photo(self): pass

class BiometricEnabled(ABC):
    """Has fingerprint/face scanner"""
    @abstractmethod
    def scan_fingerprint(self): pass


# iPhone implements ALL (because it CAN do all)
class iPhone(Callable, Messageable, InternetEnabled, CameraEnabled, BiometricEnabled):
    def make_call(self): print("Calling via iPhone...")
    def send_text(self): print("iMessage sent!")
    def browse_internet(self): print("Safari opened!")
    def take_photo(self): print("Click!")
    def scan_fingerprint(self): print("Touch ID!")

# Nokia implements ONLY what it can actually do
class NokiaBasicPhone(Callable, Messageable):
    def make_call(self): print("Calling...")
    def send_text(self): print("SMS sent!")
    # No fake empty methods! Clean and honest.
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
```python
class MySQLDatabase:
    def save(self, data):
        print(f"INSERT INTO users VALUES ('{data['name']}', '{data['email']}')")

class UserService:
    def __init__(self):
        self.db = MySQLDatabase()  # HARDCODED! Stuck with MySQL forever!

    def create_user(self, name, email):
        self.db.save({"name": name, "email": email})

# Problems:
# 1. Want to switch to MongoDB? Must CHANGE UserService
# 2. Want to test without a real database? Must CHANGE UserService
# 3. UserService (business logic) is tied to MySQL (infrastructure detail)
```

**Good Code (UserService depends on abstraction, works with ANY database):**
```python
from abc import ABC, abstractmethod

# The abstraction (the "USB-C standard")
class Database(ABC):
    @abstractmethod
    def save(self, data): pass

    @abstractmethod
    def find(self, query): pass

    @abstractmethod
    def delete(self, id): pass


# Implementation 1: MySQL
class MySQLDatabase(Database):
    def save(self, data):
        print(f"MySQL: INSERT INTO users VALUES (...)")

    def find(self, query):
        print(f"MySQL: SELECT * FROM users WHERE {query}")

    def delete(self, id):
        print(f"MySQL: DELETE FROM users WHERE id = {id}")


# Implementation 2: MongoDB
class MongoDatabase(Database):
    def save(self, data):
        print(f"MongoDB: db.users.insertOne({data})")

    def find(self, query):
        print(f"MongoDB: db.users.find({query})")

    def delete(self, id):
        print(f"MongoDB: db.users.deleteOne({{_id: {id}}})")


# Implementation 3: Fake database for testing (no real DB needed!)
class FakeDatabase(Database):
    def __init__(self):
        self.storage = []

    def save(self, data):
        self.storage.append(data)
        print(f"Fake DB: Stored {data}")

    def find(self, query):
        return [d for d in self.storage if query in str(d)]

    def delete(self, id):
        self.storage = [d for d in self.storage if d.get('id') != id]


# UserService depends on the ABSTRACTION (Database), not specific implementation
class UserService:
    def __init__(self, db: Database):  # Accepts ANY database!
        self.db = db

    def create_user(self, name, email):
        self.db.save({"name": name, "email": email})

    def find_user(self, email):
        return self.db.find(f"email={email}")


# Production — use MongoDB
service = UserService(MongoDatabase())
service.create_user("Sheetal", "sheetal@gmail.com")

# Testing — use fake DB (fast, no real database needed)
test_service = UserService(FakeDatabase())
test_service.create_user("Test User", "test@test.com")

# Switching databases? Change ONE line where you create UserService.
# UserService itself doesn't change AT ALL.
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
```python
class ReportGenerator:
    def fetch_data_from_database(self): ...
    def process_data(self): ...
    def generate_pdf(self): ...
    def send_email_with_report(self): ...
    def upload_to_s3(self): ...
    def notify_slack(self): ...
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
```python
class OnlineStore:
    def add_product(self, name, price): ...
    def remove_product(self, product_id): ...
    def calculate_total(self, cart): ...
    def apply_discount(self, cart, discount_type):
        if discount_type == "percentage":
            # percentage logic
        elif discount_type == "flat":
            # flat discount logic
        elif discount_type == "bogo":
            # buy one get one logic
    def charge_credit_card(self, amount, card_number): ...
    def charge_upi(self, amount, upi_id): ...
    def send_order_confirmation(self, order): ...
    def generate_invoice(self, order): ...
    def update_inventory(self, product_id, quantity): ...
    def track_delivery(self, order_id): ...
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
