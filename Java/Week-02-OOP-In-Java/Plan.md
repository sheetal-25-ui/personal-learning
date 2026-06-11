# Week 2: Object-Oriented Programming in Java

---

## 1. Classes and Objects — Java Forces OOP

### What is it?
A class is a blueprint or template. An object is a real thing built from that blueprint. Think of a class as a recipe and an object as the actual dish made from that recipe.

### Why Java forces everything into classes
In Python, you can write code anywhere — in a file, at the top level, in a function. Java says NO. Every single piece of code must live inside a class. There's no "loose code" in Java.

### Real-life analogy
Think of Ola/Uber. The "Car" class is the blueprint:
- Every car has: brand, color, number plate, mileage (these are **fields/attributes**)
- Every car can: start, stop, accelerate, honk (these are **methods**)

The blueprint is the same, but each actual car on the road is a different **object** with different values.

### Python comparison
```python
# Python — you CAN write code outside classes
print("Hello")  # This works at the top level

class Car:
    def __init__(self, brand, color):
        self.brand = brand
        self.color = color

    def start(self):
        print(f"{self.brand} is starting")

my_car = Car("Maruti", "White")
my_car.start()
```

```java
// Java — EVERYTHING must be inside a class
public class Car {
    // Fields (attributes)
    String brand;
    String color;

    // Method
    void start() {
        System.out.println(brand + " is starting");
    }

    // Main method — the entry point (also inside a class!)
    public static void main(String[] args) {
        Car myCar = new Car();   // Create an object
        myCar.brand = "Maruti";  // Set field values
        myCar.color = "White";
        myCar.start();           // Call method
    }
}
```

### Key differences from Python
| Concept | Python | Java |
|---------|--------|------|
| Create object | `car = Car()` | `Car car = new Car();` |
| Access field | `car.brand` | `car.brand` (same!) |
| Self reference | `self` (explicit parameter) | `this` (implicit, no parameter needed) |
| Constructor | `__init__` | Same name as the class |
| No class needed | `print("hi")` at top level | Impossible — must be in a class |

### The `new` keyword
In Java, objects are created using `new`. This:
1. Allocates memory for the object
2. Calls the constructor
3. Returns a reference (memory address) to the object

```java
Car myCar = new Car();
// │    │      │    │
// │    │      │    └─ Calls the constructor
// │    │      └─ Creates new object in memory
// │    └─ Variable name (reference)
// └─ Type (must match or be a parent class)
```

### Practice exercises
1. Create a `SwiggyOrder` class with fields: customerName, restaurantName, itemName, price, quantity. Add a method `printBill()` that prints a formatted bill.
2. Create 3 different order objects and call printBill() on each
3. Create a `BankAccount` class with fields: accountNumber, holderName, balance. Add methods: deposit(), withdraw(), checkBalance()

---

## 2. Constructors

### What is it?
A constructor is a special method that runs automatically when you create an object. It's used to set initial values. In Python, this is `__init__`.

### Real-life analogy
When a baby is born in India, certain things are set immediately: name, date of birth, parents' names, hospital. You don't wait and set these later — they're filled in at the moment of creation. That's what a constructor does.

### Types of constructors

**Default constructor (no parameters):**
```java
public class Student {
    String name;
    int age;

    // Default constructor — no parameters
    public Student() {
        name = "Unknown";
        age = 0;
    }
}

Student s = new Student();  // name = "Unknown", age = 0
```

**Parameterized constructor (with parameters):**
```java
public class Student {
    String name;
    int age;

    // Parameterized constructor
    public Student(String name, int age) {
        this.name = name;  // 'this.name' = field, 'name' = parameter
        this.age = age;
    }
}

Student s = new Student("Sheetal", 25);  // name = "Sheetal", age = 25
```

### The `this` keyword
`this` refers to the current object. It's used when the parameter name is the same as the field name.

```java
public Student(String name, int age) {
    this.name = name;  // this.name = the object's field
                       // name = the parameter passed to the constructor
    this.age = age;
}
```

### Python comparison
```python
class Student:
    def __init__(self, name, age):
        self.name = name    # self = Python's version of 'this'
        self.age = age
```

In Python, you MUST write `self` as the first parameter. In Java, `this` is available automatically — you don't list it as a parameter.

### Constructor Overloading (multiple constructors)
Just like method overloading, you can have multiple constructors with different parameters:

```java
public class ZomatoOrder {
    String restaurant;
    String item;
    int quantity;
    String instructions;

    // Constructor 1: Just restaurant and item
    public ZomatoOrder(String restaurant, String item) {
        this.restaurant = restaurant;
        this.item = item;
        this.quantity = 1;  // default quantity
        this.instructions = "None";  // default instructions
    }

    // Constructor 2: Restaurant, item, and quantity
    public ZomatoOrder(String restaurant, String item, int quantity) {
        this.restaurant = restaurant;
        this.item = item;
        this.quantity = quantity;
        this.instructions = "None";
    }

    // Constructor 3: Everything
    public ZomatoOrder(String restaurant, String item, int quantity, String instructions) {
        this.restaurant = restaurant;
        this.item = item;
        this.quantity = quantity;
        this.instructions = instructions;
    }
}

// All three work:
ZomatoOrder o1 = new ZomatoOrder("Paradise", "Biryani");
ZomatoOrder o2 = new ZomatoOrder("Paradise", "Biryani", 2);
ZomatoOrder o3 = new ZomatoOrder("Paradise", "Biryani", 2, "Extra raita");
```

### Constructor chaining with `this()`
You can call one constructor from another to avoid duplicating code:

```java
public ZomatoOrder(String restaurant, String item) {
    this(restaurant, item, 1, "None");  // Calls the 4-parameter constructor
}

public ZomatoOrder(String restaurant, String item, int quantity) {
    this(restaurant, item, quantity, "None");  // Calls the 4-parameter constructor
}

public ZomatoOrder(String restaurant, String item, int quantity, String instructions) {
    this.restaurant = restaurant;
    this.item = item;
    this.quantity = quantity;
    this.instructions = instructions;
}
```

### Common mistakes
1. **Giving the constructor a return type** — Constructors NEVER have a return type. `public void Student()` is a regular method, not a constructor!
2. **Not using `this`** — if parameter names match field names and you don't use `this`, the parameter shadows the field and the field never gets set
3. **Forgetting that Java provides a default constructor ONLY if you don't write any constructor** — once you write even one constructor, the default disappears

### Practice exercises
1. Create a `FlipkartProduct` class with overloaded constructors: one with just name and price, one with name, price, and category, one with all fields including discount percentage
2. Use constructor chaining (this()) to avoid code duplication
3. Create a `UPIPayment` class that has constructors for: quick pay (just amount), full pay (amount + note), and scheduled pay (amount + note + date)

---

## 3. Access Modifiers — public, private, protected, default

### What is it?
Access modifiers control WHO can see and use your fields and methods. It's Java's security system for code.

### Real-life analogy
Think of a house:
- **public** — The front door. Anyone can come in. Like a shop — open to everyone.
- **private** — Your bedroom. Only you can access it. Even your neighbors can't see inside.
- **protected** — Family rooms. Your family (child classes) can access, but outsiders can't.
- **default (package-private)** — Your building's common area. People in the same building (package) can use it, but people from other buildings can't.

### The four access levels
```java
public class BankAccount {
    public String holderName;       // Anyone can see this
    private double balance;         // Only this class can see this
    protected String branch;        // This class + child classes + same package
    String accountType;             // Default (package-private) — same package only
}
```

| Modifier | Same Class | Same Package | Subclass (other package) | World |
|----------|-----------|-------------|------------------------|-------|
| `public` | Yes | Yes | Yes | Yes |
| `protected` | Yes | Yes | Yes | No |
| default | Yes | Yes | No | No |
| `private` | Yes | No | No | No |

### Python comparison
```python
class BankAccount:
    def __init__(self):
        self.holder_name = "Sheetal"     # Public (no prefix)
        self._branch = "Mumbai"          # Protected by CONVENTION (_prefix)
        self.__balance = 10000           # Private by CONVENTION (__prefix)
        # But nothing is truly enforced!
        # You can still do: account.__balance or account._BankAccount__balance
```

```java
public class BankAccount {
    public String holderName;    // Truly public
    protected String branch;     // Truly protected — compiler enforces it
    private double balance;      // Truly private — compiler BLOCKS access from outside
}
```

**The BIG difference:** Python uses naming conventions (underscore prefixes) that are just suggestions — nothing stops you from accessing `_private` variables. Java uses real access modifiers that the compiler ENFORCES. If a field is `private`, you literally cannot access it from outside the class — the code won't compile.

### Why do we need access modifiers?
Without them (imagine everything is public):
- Anyone could set `balance = -50000` directly
- No validation, no checks, no control
- A change to how balance is stored would break every file that accesses it directly

With `private` balance and a public `withdraw()` method:
- The method can check "is there enough money?"
- The method can log the transaction
- You can change how balance is stored internally without affecting anyone

### Common mistakes
1. **Accessing private fields from another class** — `account.balance` from outside won't compile if balance is private
2. **Confusing "default" with "public"** — if you don't write any modifier, it's NOT public — it's package-private
3. **Making everything public** — defeats the purpose. Default to `private` and only make things more accessible when needed

### Practice exercises
1. Create an `AadhaarCard` class where the Aadhaar number is private, name is public, and address is protected. Try accessing all three from another class and observe the errors.
2. Create a `MobileWallet` class (like Paytm) where balance is private. Write public methods to add money, send money, and check balance — all with proper validation.

---

## 4. Encapsulation — Getters and Setters

### What is it?
Encapsulation means hiding the internal details of a class and providing controlled access through methods. Make fields private, provide public getters (to read) and setters (to write with validation).

### Real-life analogy
Think of an ATM:
- You can't reach inside the ATM and grab cash (fields are private)
- You use the screen and keypad (public methods) to interact
- The ATM validates your PIN before giving cash (validation in setter)
- You don't know how the ATM works internally (hidden implementation)

### The pattern
```java
public class Employee {
    // Private fields — nobody can access directly
    private String name;
    private double salary;
    private int age;

    // Getter — read access
    public String getName() {
        return name;
    }

    // Setter — write access with validation
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.name = name;
    }

    // Getter
    public double getSalary() {
        return salary;
    }

    // Setter with validation
    public void setSalary(double salary) {
        if (salary < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
        this.salary = salary;
    }

    // Getter
    public int getAge() {
        return age;
    }

    // Setter with validation
    public void setAge(int age) {
        if (age < 18 || age > 65) {
            throw new IllegalArgumentException("Age must be between 18 and 65");
        }
        this.age = age;
    }
}
```

### Python comparison
```python
class Employee:
    def __init__(self, name, salary):
        self._name = name       # Convention-based "private"
        self._salary = salary

    @property                    # Python's getter
    def salary(self):
        return self._salary

    @salary.setter               # Python's setter
    def salary(self, value):
        if value < 0:
            raise ValueError("Salary cannot be negative")
        self._salary = value

# Usage:
emp = Employee("Sheetal", 50000)
print(emp.salary)       # Uses the getter
emp.salary = 60000      # Uses the setter
```

```java
// Java usage:
Employee emp = new Employee();
emp.setName("Sheetal");
emp.setSalary(50000);
System.out.println(emp.getName());
System.out.println(emp.getSalary());
```

**Key difference:** Python uses `@property` decorators so getters/setters look like direct field access (`emp.salary`). Java uses explicit `getName()`/`setName()` method calls. Java's approach is more verbose but makes it obvious you're calling a method.

### JavaBeans Convention
In the Java world, this pattern of private fields + getters + setters is called the **JavaBeans convention**. Most Java frameworks (Spring, Hibernate, Jackson) expect this pattern. It's not optional — it's how Java ecosystem works.

### Common mistakes
1. **Not validating in setters** — if you just do `this.salary = salary`, there's no point in having a setter. Add validation!
2. **Making getters/setters for everything blindly** — only create them for fields that SHOULD be accessible. If a field should never change after creation, only provide a getter (no setter).
3. **Returning mutable objects from getters** — if you return an ArrayList, the caller can modify it. Return a copy or an unmodifiable view.

### Practice exercises
1. Create a `FlipkartUser` class with proper encapsulation: email (must contain @), phone (must be 10 digits), password (private, no getter — only a `verifyPassword()` method)
2. Create a `Temperature` class with a private field in Celsius. Provide getters for both Celsius and Fahrenheit (calculated on the fly). The setter should reject values below absolute zero (-273.15).

---

## 5. Inheritance — extends keyword

### What is it?
Inheritance lets you create a new class based on an existing class. The new class (child/subclass) gets all the fields and methods of the existing class (parent/superclass), and can add its own.

### Real-life analogy
Think of Zomato delivery partners:
- **DeliveryPartner** (parent): has name, phone, rating, vehicle type. Can accept order, deliver order.
- **BikePartner** (child): has everything a DeliveryPartner has, PLUS helmet number, bike registration. Can also navigate narrow lanes.
- **CarPartner** (child): has everything a DeliveryPartner has, PLUS car AC status, trunk capacity. Can deliver large orders.

BikePartner IS-A DeliveryPartner. CarPartner IS-A DeliveryPartner. This "IS-A" relationship is inheritance.

### The code
```java
// Parent class
public class DeliveryPartner {
    String name;
    String phone;
    double rating;

    public DeliveryPartner(String name, String phone) {
        this.name = name;
        this.phone = phone;
        this.rating = 5.0;  // New partners start with 5.0
    }

    public void acceptOrder(String orderId) {
        System.out.println(name + " accepted order " + orderId);
    }

    public void deliver() {
        System.out.println(name + " delivered the order");
    }
}

// Child class — inherits from DeliveryPartner
public class BikePartner extends DeliveryPartner {
    String bikeNumber;

    public BikePartner(String name, String phone, String bikeNumber) {
        super(name, phone);  // Call parent constructor FIRST
        this.bikeNumber = bikeNumber;
    }

    // New method only in BikePartner
    public void navigateNarrowLanes() {
        System.out.println(name + " navigating through gali on bike " + bikeNumber);
    }
}
```

### The `super` keyword
`super` refers to the parent class. Used for:
1. **Calling parent constructor**: `super(name, phone)` — MUST be the first line in child constructor
2. **Calling parent method**: `super.deliver()` — useful when you override a method but still want the parent's version

### Python comparison
```python
class DeliveryPartner:
    def __init__(self, name, phone):
        self.name = name
        self.phone = phone

class BikePartner(DeliveryPartner):     # Python uses ()
    def __init__(self, name, phone, bike_number):
        super().__init__(name, phone)   # Call parent constructor
        self.bike_number = bike_number
```

```java
public class BikePartner extends DeliveryPartner {  // Java uses 'extends'
    String bikeNumber;

    public BikePartner(String name, String phone, String bikeNumber) {
        super(name, phone);  // Call parent constructor
        this.bikeNumber = bikeNumber;
    }
}
```

### Method Overriding
A child class can provide its own version of a parent method:

```java
public class DeliveryPartner {
    public void deliver() {
        System.out.println("Delivering order...");
    }
}

public class BikePartner extends DeliveryPartner {
    @Override  // Annotation — tells Java "I'm intentionally overriding"
    public void deliver() {
        System.out.println("Delivering on bike through traffic!");
    }
}

// Usage
DeliveryPartner dp = new BikePartner("Rahul", "9876543210", "MH04AB1234");
dp.deliver();  // "Delivering on bike through traffic!" — child's version runs!
```

### @Override annotation
This is optional but HIGHLY recommended:
- It tells Java "I intend to override a parent method"
- If you misspell the method name, Java gives a compile error instead of silently creating a new method
- Interviewers expect to see it

### Single Inheritance Only — No Multiple Inheritance!
```java
// Java — ONLY one parent class
public class BikePartner extends DeliveryPartner { }  // OK

// Python — multiple parents allowed
class BikePartner(DeliveryPartner, InsuredVehicle):   # OK in Python
    pass
```

**Why?** The Diamond Problem:
```
Imagine both DeliveryPartner and InsuredVehicle have a method called getStatus().
If BikePartner inherits from both, which getStatus() should it use?

     DeliveryPartner     InsuredVehicle
         getStatus()       getStatus()
              \               /
               \             /
                BikePartner
                getStatus()  ← Which one??
```

Java avoids this confusion by allowing only single inheritance. But you CAN implement multiple interfaces (covered later) to get similar flexibility.

### Common mistakes
1. **Forgetting `super()` in child constructor** — Java requires it (if the parent has no default constructor)
2. **Not using `@Override`** — without it, typos in method names silently create new methods instead of overriding
3. **Trying multiple inheritance** — `class C extends A, B` does NOT compile in Java

### Practice exercises
1. Create a hierarchy: `Vehicle` (parent) with fields (make, model, year). `Car` extends Vehicle (adds: numDoors, trunkSize). `Bike` extends Vehicle (adds: engineCC, hasCarrier). Override a `describe()` method in each.
2. Create: `OlaRide` (parent) with fare calculation. `OlaMini` and `OlaPrime` (children) that override fare calculation with different rates.
3. Demonstrate the `super` keyword by calling a parent method from a child method.

---

## 6. Polymorphism

### What is it?
Polymorphism means "many forms." The same method call can behave differently depending on which object it's called on. There are two types: compile-time (method overloading) and runtime (method overriding).

### Real-life analogy
Think of a "Pay" button in Google Pay:
- If you're paying a friend, it does UPI transfer
- If you're paying a merchant, it generates a QR code
- If you're paying a bill, it contacts the biller

Same button ("Pay"), different behavior based on context. That's polymorphism.

### Compile-time polymorphism (Method Overloading)
We covered this in Week 1. Same method name, different parameters. The compiler decides which to call at compile time.

```java
public class Calculator {
    int add(int a, int b) { return a + b; }
    double add(double a, double b) { return a + b; }
    int add(int a, int b, int c) { return a + b + c; }
}
```

### Runtime polymorphism (Method Overriding)
The JVM decides which method to call at runtime, based on the actual object type.

```java
class Payment {
    void process() {
        System.out.println("Processing payment...");
    }
}

class UPIPayment extends Payment {
    @Override
    void process() {
        System.out.println("Processing UPI payment via Google Pay");
    }
}

class CardPayment extends Payment {
    @Override
    void process() {
        System.out.println("Processing credit card payment");
    }
}

class CODPayment extends Payment {
    @Override
    void process() {
        System.out.println("Cash on delivery — collect from customer");
    }
}

// The magic of polymorphism:
Payment p1 = new UPIPayment();    // Parent reference, child object
Payment p2 = new CardPayment();
Payment p3 = new CODPayment();

p1.process();  // "Processing UPI payment via Google Pay"
p2.process();  // "Processing credit card payment"
p3.process();  // "Cash on delivery — collect from customer"

// Even more powerful — put them in an array!
Payment[] payments = {new UPIPayment(), new CardPayment(), new CODPayment()};
for (Payment p : payments) {
    p.process();  // Each calls its OWN version. This is polymorphism!
}
```

### Upcasting and Downcasting

**Upcasting** (child to parent) — always safe, implicit:
```java
Payment p = new UPIPayment();  // UPIPayment "is-a" Payment, so this works
// p can only call methods defined in Payment
// But at runtime, the overridden version in UPIPayment runs
```

**Downcasting** (parent to child) — risky, must be explicit:
```java
Payment p = new UPIPayment();
UPIPayment upi = (UPIPayment) p;  // Explicit downcast — works because p IS a UPIPayment

Payment p2 = new CardPayment();
UPIPayment upi2 = (UPIPayment) p2;  // ClassCastException! p2 is NOT a UPIPayment
```

**Safe downcasting with `instanceof`:**
```java
if (p instanceof UPIPayment) {
    UPIPayment upi = (UPIPayment) p;
    upi.getUPIId();  // Safe to call UPIPayment-specific method
}
```

### Python comparison
```python
# Python does this naturally (duck typing)
class UPIPayment:
    def process(self):
        print("Processing UPI payment")

class CardPayment:
    def process(self):
        print("Processing card payment")

# No need for a common parent in Python
payments = [UPIPayment(), CardPayment()]
for p in payments:
    p.process()  # Just works — "if it looks like a duck..."
```

In Python, you don't need a shared parent class. If it has a `process()` method, Python calls it. Java requires a common type (parent class or interface) for polymorphism to work.

### Practice exercises
1. Create a `Notification` system: parent class `Notification` with `send()` method. Children: `SMSNotification`, `EmailNotification`, `PushNotification` — each overrides `send()` differently. Process an array of mixed notifications.
2. Demonstrate upcasting and downcasting with a `Shape` hierarchy (Circle, Rectangle, Triangle).
3. Use `instanceof` to safely downcast and call child-specific methods.

---

## 7. Abstraction — Abstract Classes and Interfaces

### What is it?
Abstraction means hiding the complex implementation and showing only the essential features. It's like driving a car — you use the steering wheel and pedals (the interface) without knowing how the engine works internally.

### Abstract Classes

An abstract class is a class that cannot be instantiated directly. It can have both complete methods and incomplete methods (that child classes MUST fill in).

```java
// You can't create: new Vehicle() — it's abstract
abstract class Vehicle {
    String brand;

    // Constructor — yes, abstract classes can have constructors!
    public Vehicle(String brand) {
        this.brand = brand;
    }

    // Abstract method — NO body. Children MUST implement this.
    abstract void startEngine();

    // Concrete method — has a body. Children inherit this.
    void honk() {
        System.out.println(brand + " goes beep beep!");
    }
}

class Car extends Vehicle {
    public Car(String brand) {
        super(brand);
    }

    @Override
    void startEngine() {
        System.out.println(brand + " car engine starts with key turn");
    }
}

class ElectricScooter extends Vehicle {
    public ElectricScooter(String brand) {
        super(brand);
    }

    @Override
    void startEngine() {
        System.out.println(brand + " scooter starts silently — electric motor on");
    }
}
```

### Interfaces

An interface is a contract. It says "any class that implements me MUST provide these methods." It has NO fields (only constants) and NO method bodies (before Java 8).

```java
// Interface — pure contract
interface Payable {
    void makePayment(double amount);
    double getBalance();
    // No method bodies! Just signatures.
}

interface Trackable {
    String getCurrentLocation();
    void updateLocation(String location);
}

// A class can implement MULTIPLE interfaces
class OlaDriver implements Payable, Trackable {
    private double wallet = 0;
    private String location = "Unknown";

    @Override
    public void makePayment(double amount) {
        wallet += amount;
        System.out.println("Payment of Rs " + amount + " received");
    }

    @Override
    public double getBalance() {
        return wallet;
    }

    @Override
    public String getCurrentLocation() {
        return location;
    }

    @Override
    public void updateLocation(String location) {
        this.location = location;
    }
}
```

### Abstract Class vs Interface (INTERVIEW FAVORITE)

| Feature | Abstract Class | Interface |
|---------|---------------|-----------|
| Methods | Can have both abstract and concrete | All abstract (before Java 8). Can have default methods (Java 8+) |
| Fields | Can have instance fields | Only constants (public static final) |
| Constructor | Yes | No |
| Inheritance | Single inheritance (extends one) | Multiple interfaces (implements many) |
| Use when... | Classes share common state/behavior | Classes share a contract but have different implementations |
| Keyword | `extends` | `implements` |

### When to use which? (The Flipkart test)
- **Abstract class**: "A BikePartner IS-A DeliveryPartner" — they share state (name, phone, rating) and common behavior (acceptOrder). Use abstract class when there's a clear parent-child relationship with shared code.
- **Interface**: "A DeliveryPartner IS Payable, IS Trackable" — these are capabilities, not identities. Many unrelated classes might be Payable (driver, restaurant, customer). Use interface when defining what something CAN DO, not what it IS.

### Java 8 default methods — changed everything
Before Java 8, interfaces could ONLY have abstract methods. This was a problem: if you added a new method to an interface, EVERY class implementing it would break.

Java 8 introduced `default` methods — interfaces can now have method bodies:

```java
interface Deliverable {
    void deliver();

    // Default method — has a body. Classes get this for free.
    default void trackDelivery() {
        System.out.println("Tracking delivery...");
    }
}

class ZomatoDelivery implements Deliverable {
    @Override
    public void deliver() {
        System.out.println("Zomato delivery in progress");
    }
    // trackDelivery() is inherited from the interface — no need to implement
}
```

### Python comparison
```python
# Python abstract class (using abc module)
from abc import ABC, abstractmethod

class Vehicle(ABC):
    @abstractmethod
    def start_engine(self):
        pass

    def honk(self):  # Concrete method
        print("Beep beep!")

# Python doesn't have interfaces as a language feature
# You use abstract classes with all abstract methods to simulate interfaces
```

### Common mistakes
1. **Trying to instantiate an abstract class** — `new Vehicle()` is a compile error
2. **Forgetting to implement all abstract methods** — if a child doesn't implement all, IT also becomes abstract
3. **Confusing when to use abstract class vs interface** — ask "IS-A or CAN-DO?"

### Practice exercises
1. Create an abstract class `BankAccount` with abstract method `calculateInterest()`. Implement `SavingsAccount` (4% interest) and `FixedDeposit` (7% interest).
2. Create interfaces `Searchable` and `Filterable`. Implement both in an `AmazonProduct` class.
3. Create a payment processing system using interfaces: `PaymentProcessor` interface with `processPayment()` and `refund()`. Implement `PaytmProcessor`, `PhonePeProcessor`, `GPAYProcessor`.

---

## 8. static Keyword

### What is it?
`static` means "belongs to the CLASS, not to any specific object." A static field or method is shared across ALL objects of that class.

### Real-life analogy
Think of a school class (like 10th-A):
- Each student has their own name, roll number (instance fields — different for each)
- But the class teacher name, classroom number are the SAME for all students (static fields — shared)

### Static variables
```java
public class OlaRide {
    // Static — shared by ALL rides
    static int totalRides = 0;
    static double totalRevenue = 0;

    // Instance — unique to each ride
    String riderName;
    double fare;

    public OlaRide(String riderName, double fare) {
        this.riderName = riderName;
        this.fare = fare;
        totalRides++;           // Increment the shared counter
        totalRevenue += fare;   // Add to shared total
    }
}

// Usage:
new OlaRide("Sheetal", 200);
new OlaRide("Priya", 350);
new OlaRide("Rahul", 150);

System.out.println(OlaRide.totalRides);    // 3 (shared across all objects)
System.out.println(OlaRide.totalRevenue);  // 700.0
```

### Static methods
```java
public class MathHelper {
    // Static method — call without creating an object
    public static int square(int n) {
        return n * n;
    }

    public static double circleArea(double radius) {
        return Math.PI * radius * radius;
    }
}

// Call directly on the class — no object needed
int result = MathHelper.square(5);       // 25
double area = MathHelper.circleArea(7);  // 153.93...
```

You've been using static methods since Day 1! `Math.sqrt()`, `Integer.parseInt()`, `Arrays.sort()` — all static.

### Static methods cannot access instance fields
```java
public class Example {
    int instanceVar = 10;
    static int staticVar = 20;

    public static void staticMethod() {
        // System.out.println(instanceVar);  ← ERROR! Can't access instance from static
        System.out.println(staticVar);        // OK — static can access static
    }

    public void instanceMethod() {
        System.out.println(instanceVar);  // OK
        System.out.println(staticVar);    // OK — instance CAN access static
    }
}
```

**Why?** Static methods belong to the class, not any object. They don't know WHICH object's instanceVar to use — there could be 100 objects, each with a different value.

### Python comparison
```python
class OlaRide:
    total_rides = 0  # Class variable (like Java static)

    def __init__(self, rider_name, fare):
        self.rider_name = rider_name  # Instance variable
        OlaRide.total_rides += 1

    @staticmethod
    def get_total_rides():
        return OlaRide.total_rides
```

In Python, class variables (defined outside `__init__`) behave like Java static fields. `@staticmethod` is like Java's `static` keyword.

### Practice exercises
1. Create a `Student` class with a static variable `totalStudents` that counts how many students have been created
2. Create a utility class `StringHelper` with static methods: `isPalindrome()`, `countVowels()`, `reverseString()`
3. Explain why `main` is static (what would happen if it wasn't?)

---

## 9. final Keyword

### What is it?
`final` means "this cannot be changed." It can be applied to variables, methods, and classes.

### Real-life analogy
- **final variable** = Your Aadhaar number. Once assigned, it never changes.
- **final method** = The Supreme Court's final verdict. Lower courts cannot override it.
- **final class** = A sealed envelope. Nobody can open (extend) it.

### final variables (constants)
```java
final double GST_RATE = 0.18;        // Cannot be changed
// GST_RATE = 0.12;                  // ERROR! Cannot reassign

final String COUNTRY = "India";
// COUNTRY = "USA";                  // ERROR!

// Convention: Constants are UPPER_SNAKE_CASE in Java
static final int MAX_RETRIES = 3;
static final String API_URL = "https://api.flipkart.com";
```

### Python comparison
```python
# Python has NO true constants. Convention only:
GST_RATE = 0.18   # Convention: ALL_CAPS means "don't change this"
GST_RATE = 0.12   # But you CAN change it — Python doesn't stop you!
```

Java's `final` is enforced by the compiler. Python's ALL_CAPS naming is just a polite suggestion.

### final methods (cannot be overridden)
```java
class Payment {
    // This method cannot be overridden by child classes
    final void generateReceipt() {
        System.out.println("Receipt generated");
    }
}

class UPIPayment extends Payment {
    // @Override
    // void generateReceipt() { }  ← ERROR! Cannot override final method
}
```

### final classes (cannot be extended)
```java
final class String {
    // Nobody can create a subclass of String
    // This is why you can't do: class MyString extends String
}
```

`String`, `Integer`, `Double` — all final classes in Java. This is for security and immutability.

---

## 10. The Object Class — toString(), equals(), hashCode()

### What is it?
Every class in Java automatically extends the `Object` class. It's the ultimate parent of every class. This gives every Java object three important methods:

### toString()
```java
public class Student {
    String name;
    int age;

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // Without overriding toString():
    // System.out.println(student); → "Student@2f4d3709" (useless!)

    // Override toString() to get useful output:
    @Override
    public String toString() {
        return "Student{name='" + name + "', age=" + age + "}";
    }
}

Student s = new Student("Sheetal", 25);
System.out.println(s);  // "Student{name='Sheetal', age=25}"
```

### Python comparison
```python
class Student:
    def __repr__(self):      # Python's equivalent of toString()
        return f"Student(name={self.name}, age={self.age})"
```

### equals() — comparing object content
By default, `equals()` compares memory addresses (same as `==`). You MUST override it to compare content.

```java
public class Student {
    String name;
    int rollNumber;

    @Override
    public boolean equals(Object obj) {
        // Step 1: Same reference?
        if (this == obj) return true;

        // Step 2: Null or different class?
        if (obj == null || getClass() != obj.getClass()) return false;

        // Step 3: Compare fields
        Student other = (Student) obj;
        return rollNumber == other.rollNumber && name.equals(other.name);
    }
}

Student s1 = new Student("Sheetal", 101);
Student s2 = new Student("Sheetal", 101);

s1 == s2         // false (different objects in memory)
s1.equals(s2)    // true (same content — because we overrode equals)
```

### hashCode()
When you override `equals()`, you MUST also override `hashCode()`. This is a Java contract: if two objects are equal (by equals()), they MUST have the same hashCode.

```java
@Override
public int hashCode() {
    return Objects.hash(name, rollNumber);
}
```

**Why?** HashMap, HashSet, and other hash-based collections use hashCode to determine where to store objects. If equals() says two objects are the same but hashCode() gives different values, these collections break silently.

### The contract (interview question)
1. If `a.equals(b)` is true, then `a.hashCode() == b.hashCode()` MUST be true
2. If `a.hashCode() == b.hashCode()`, `a.equals(b)` MAY OR MAY NOT be true (hash collisions happen)
3. If `a.equals(b)` is false, hashCodes CAN be same or different

### Practice exercises
1. Create a `Product` class with toString(), equals(), and hashCode() based on productId and name
2. Put two equal Product objects in a HashSet — verify only one gets stored (because equals + hashCode work correctly)
3. Break it: implement equals() WITHOUT hashCode() and show what goes wrong with HashSet

---

## 11. Enums

### What is it?
An enum (enumeration) is a special class that represents a fixed set of constants. Use it when a variable can only be one of a predefined set of values.

### Real-life analogy
Think of order status on Zomato: it can ONLY be one of these:
- PLACED, CONFIRMED, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED

It can never be "FLYING" or "SLEEPING." An enum enforces this.

### Without enums (the problem)
```java
// Using strings — dangerous!
String status = "PREPARING";
// Nothing stops you from writing:
status = "PRAPARING";  // Typo! No error, just a bug
status = "flying";      // Nonsense value! No error
```

### With enums (the solution)
```java
public enum OrderStatus {
    PLACED,
    CONFIRMED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}

OrderStatus status = OrderStatus.PREPARING;
// status = OrderStatus.FLYING;  ← COMPILE ERROR! FLYING doesn't exist

// Switch works beautifully with enums
switch (status) {
    case PLACED:
        System.out.println("Order placed, waiting for restaurant");
        break;
    case PREPARING:
        System.out.println("Restaurant is cooking your food");
        break;
    case DELIVERED:
        System.out.println("Enjoy your meal!");
        break;
}
```

### Java enums are MORE powerful than Python enums
Java enums can have fields, constructors, and methods:

```java
public enum PaymentMethod {
    UPI("Instant", 0),
    CREDIT_CARD("1-2 days", 2.5),
    DEBIT_CARD("Instant", 1.0),
    COD("On delivery", 30);

    private final String settlementTime;
    private final double chargePercent;

    // Constructor (always private in enums)
    PaymentMethod(String settlementTime, double chargePercent) {
        this.settlementTime = settlementTime;
        this.chargePercent = chargePercent;
    }

    public String getSettlementTime() { return settlementTime; }
    public double getChargePercent() { return chargePercent; }
}

// Usage:
PaymentMethod method = PaymentMethod.UPI;
System.out.println(method.getSettlementTime());  // "Instant"
System.out.println(method.getChargePercent());    // 0.0
```

### Python comparison
```python
from enum import Enum

class OrderStatus(Enum):
    PLACED = "placed"
    CONFIRMED = "confirmed"
    PREPARING = "preparing"

# Python enums are simpler — they can't easily have multiple fields and methods
```

### Practice exercises
1. Create an enum `RideType` for Ola with values: MINI, SEDAN, PRIME, AUTO. Each should have a base fare and per-km rate.
2. Create an enum `City` with values: MUMBAI, DELHI, BANGALORE, CHENNAI. Each has a population and state.
3. Write a method that takes a `DayOfWeek` enum and returns whether it's a weekday or weekend.

---

## Week 2 Summary: OOP Concepts at a Glance

| Concept | What it does | Keyword |
|---------|-------------|---------|
| Class | Blueprint for objects | `class` |
| Object | Instance of a class | `new` |
| Constructor | Initializes an object | Same name as class |
| Encapsulation | Hides internal data | `private` + getters/setters |
| Inheritance | Child gets parent's stuff | `extends` |
| Polymorphism | Same call, different behavior | `@Override` |
| Abstraction | Hide complexity | `abstract`, `interface` |
| static | Belongs to class, not object | `static` |
| final | Cannot be changed/overridden/extended | `final` |
| Enum | Fixed set of constants | `enum` |

---

## Week 2 Practice Project

**Build an "Ola Ride Booking System"**

Requirements:
1. Abstract class `Ride` with: riderName, distance, abstract method `calculateFare()`
2. Concrete classes: `OlaMini` (Rs 7/km), `OlaPrime` (Rs 12/km), `OlaAuto` (Rs 5/km) — each overrides calculateFare()
3. Enum `RideStatus`: REQUESTED, DRIVER_ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED
4. Enum `PaymentMethod`: UPI, CARD, CASH, WALLET
5. Interface `Trackable` with methods: `getLocation()`, `getETA()`
6. Interface `Payable` with methods: `processPayment()`, `generateReceipt()`
7. All rides implement both interfaces
8. Static variable to track total rides across all types
9. Proper encapsulation — private fields with getters/setters
10. Override toString(), equals(), hashCode() in the Ride class
11. Use polymorphism: store different ride types in a `Ride[]` array and process them

This project uses EVERY OOP concept from this week.
