# Week 2: Object-Oriented Programming in Java — Interview Answers & Exercise Solutions

> **The Interview Golden Rule for OOP** 🏆
> Whenever you're asked an OOP question, answer in this exact order:
> **1. Definition** (one crisp sentence) → **2. Real-life analogy** (so it sticks) → **3. Java code** (to prove you can do it) → **4. Why it matters** (the design payoff).
>
> Interviewers don't just want "what is encapsulation" — they want to know you understand *why* it exists. A candidate who says "encapsulation is private fields with getters/setters" sounds memorized. A candidate who says "encapsulation is hiding internal data behind a controlled gate — like an ATM that validates your PIN before giving cash — so you can change the internals tomorrow without breaking every caller" sounds like an engineer. Be the second one. 🙌

This file has two parts:
- **PART A** — Full worked Java solutions to every exercise and the capstone project in `Plan.md`.
- **PART B** — 16 likely Java OOP interview questions with confident, spoken-style model answers, trap questions, and comparison tables.

All code is plain Java and compiles as-is. Where a snippet is a fragment (a single method or class), assume it lives inside a normal `class` / `main` as shown by the surrounding runnable examples.

---

# PART A — Exercise / Practice Solutions

## Section 1 — Classes and Objects

### Exercise 1.1 + 1.2 — `SwiggyOrder` with `printBill()`, then 3 objects

```java
public class SwiggyOrder {
    String customerName;
    String restaurantName;
    String itemName;
    double price;
    int quantity;

    void printBill() {
        double total = price * quantity;
        System.out.println("=========== SWIGGY BILL ===========");
        System.out.println("Customer   : " + customerName);
        System.out.println("Restaurant : " + restaurantName);
        System.out.println("Item       : " + itemName);
        System.out.println("Price      : Rs " + price + " x " + quantity);
        System.out.println("-----------------------------------");
        System.out.println("TOTAL      : Rs " + total);
        System.out.println("===================================\n");
    }

    public static void main(String[] args) {
        // Exercise 1.2 — three different order objects
        SwiggyOrder o1 = new SwiggyOrder();
        o1.customerName = "Sheetal";
        o1.restaurantName = "Paradise Biryani";
        o1.itemName = "Chicken Biryani";
        o1.price = 320;
        o1.quantity = 2;

        SwiggyOrder o2 = new SwiggyOrder();
        o2.customerName = "Priya";
        o2.restaurantName = "Domino's";
        o2.itemName = "Margherita Pizza";
        o2.price = 199;
        o2.quantity = 1;

        SwiggyOrder o3 = new SwiggyOrder();
        o3.customerName = "Rahul";
        o3.restaurantName = "McDonald's";
        o3.itemName = "McAloo Tikki";
        o3.price = 60;
        o3.quantity = 3;

        o1.printBill();
        o2.printBill();
        o3.printBill();
    }
}
```

**Teaching note** 📝: notice that the *blueprint* (the `SwiggyOrder` class) is written once, but we stamp out three independent *objects*, each with its own field values. That's the whole point of a class.

### Exercise 1.3 — `BankAccount` with deposit / withdraw / checkBalance

```java
public class BankAccount {
    String accountNumber;
    String holderName;
    double balance;

    void deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Deposit amount must be positive.");
            return;
        }
        balance += amount;
        System.out.println("Deposited Rs " + amount + ". New balance: Rs " + balance);
    }

    void withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Withdrawal amount must be positive.");
            return;
        }
        if (amount > balance) {
            System.out.println("Insufficient funds. Balance is only Rs " + balance);
            return;
        }
        balance -= amount;
        System.out.println("Withdrew Rs " + amount + ". New balance: Rs " + balance);
    }

    void checkBalance() {
        System.out.println("Account " + accountNumber + " (" + holderName + ") balance: Rs " + balance);
    }

    public static void main(String[] args) {
        BankAccount acc = new BankAccount();
        acc.accountNumber = "SBIN0012345";
        acc.holderName = "Sheetal";
        acc.balance = 5000;

        acc.checkBalance();   // 5000
        acc.deposit(2000);    // 7000
        acc.withdraw(3000);   // 4000
        acc.withdraw(10000);  // rejected
        acc.checkBalance();   // 4000
    }
}
```

> ⚠️ At this stage `balance` is public, so anyone can do `acc.balance = -50000`. We fix that properly in the Encapsulation section — that's the natural motivation for getters/setters.

---

## Section 2 — Constructors

### Exercise 2.1 + 2.2 — `FlipkartProduct` with overloaded constructors + chaining

```java
public class FlipkartProduct {
    String name;
    double price;
    String category;
    double discountPercent;

    // Constructor 1: name + price only
    public FlipkartProduct(String name, double price) {
        this(name, price, "General", 0.0);   // chaining → calls the master constructor
    }

    // Constructor 2: name + price + category
    public FlipkartProduct(String name, double price, String category) {
        this(name, price, category, 0.0);    // chaining
    }

    // Constructor 3 (master): everything
    public FlipkartProduct(String name, double price, String category, double discountPercent) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.discountPercent = discountPercent;
    }

    double finalPrice() {
        return price - (price * discountPercent / 100);
    }

    void describe() {
        System.out.println(name + " [" + category + "] MRP Rs " + price
                + ", discount " + discountPercent + "% -> pay Rs " + finalPrice());
    }

    public static void main(String[] args) {
        FlipkartProduct p1 = new FlipkartProduct("USB Cable", 199);
        FlipkartProduct p2 = new FlipkartProduct("T-Shirt", 799, "Fashion");
        FlipkartProduct p3 = new FlipkartProduct("Laptop", 65000, "Electronics", 10);

        p1.describe();
        p2.describe();
        p3.describe();
    }
}
```

**Why chaining matters** 🔗: all the real assignment logic lives in ONE constructor (the master). The others just supply defaults and delegate. If you later add validation (e.g. "price can't be negative"), you write it in one place. `this(...)` **must be the first statement** in the constructor.

### Exercise 2.3 — `UPIPayment` with quick / full / scheduled constructors

```java
public class UPIPayment {
    double amount;
    String note;
    String scheduledDate;   // null means "pay now"

    // Quick pay: just amount
    public UPIPayment(double amount) {
        this(amount, "No note", null);
    }

    // Full pay: amount + note
    public UPIPayment(double amount, String note) {
        this(amount, note, null);
    }

    // Scheduled pay: amount + note + date
    public UPIPayment(double amount, String note, String scheduledDate) {
        this.amount = amount;
        this.note = note;
        this.scheduledDate = scheduledDate;
    }

    void execute() {
        if (scheduledDate == null) {
            System.out.println("Paying Rs " + amount + " now. Note: " + note);
        } else {
            System.out.println("Scheduled Rs " + amount + " for " + scheduledDate + ". Note: " + note);
        }
    }

    public static void main(String[] args) {
        new UPIPayment(500).execute();
        new UPIPayment(1200, "Rent share").execute();
        new UPIPayment(9000, "EMI", "2026-07-05").execute();
    }
}
```

---

## Section 3 — Access Modifiers

### Exercise 3.1 — `AadhaarCard` (private number, public name, protected address)

```java
public class AadhaarCard {
    public String name;            // anyone can read/write
    protected String address;      // same package + subclasses
    private String aadhaarNumber;  // ONLY this class

    public AadhaarCard(String name, String address, String aadhaarNumber) {
        this.name = name;
        this.address = address;
        this.aadhaarNumber = aadhaarNumber;
    }

    // Controlled, masked access to the private number
    public String getMaskedNumber() {
        return "XXXX-XXXX-" + aadhaarNumber.substring(aadhaarNumber.length() - 4);
    }
}

// A DIFFERENT class trying to access the fields:
class AadhaarTester {
    public static void main(String[] args) {
        AadhaarCard card = new AadhaarCard("Sheetal", "Mumbai", "123456789012");

        System.out.println(card.name);            // ✅ public — works
        System.out.println(card.address);         // ✅ works ONLY because same package
        // System.out.println(card.aadhaarNumber); // ❌ COMPILE ERROR: aadhaarNumber has private access

        System.out.println(card.getMaskedNumber()); // ✅ XXXX-XXXX-9012
    }
}
```

**Observed errors** 👀:
- `card.aadhaarNumber` → *"aadhaarNumber has private access in AadhaarCard"* — won't compile.
- `card.address` would also fail **if `AadhaarTester` lived in a different package**. Within the same package, `protected` (and even default) is accessible.

### Exercise 3.2 — `MobileWallet` (Paytm-style) with private balance + validated methods

```java
public class MobileWallet {
    private String userName;
    private double balance;   // private — the heart of encapsulation

    public MobileWallet(String userName, double openingBalance) {
        this.userName = userName;
        this.balance = Math.max(openingBalance, 0);
    }

    public void addMoney(double amount) {
        if (amount <= 0) {
            System.out.println("Add amount must be positive.");
            return;
        }
        balance += amount;
        System.out.println("Added Rs " + amount + ". Wallet balance: Rs " + balance);
    }

    public void sendMoney(double amount, String toWhom) {
        if (amount <= 0) {
            System.out.println("Send amount must be positive.");
            return;
        }
        if (amount > balance) {
            System.out.println("Insufficient balance to send Rs " + amount);
            return;
        }
        balance -= amount;
        System.out.println("Sent Rs " + amount + " to " + toWhom + ". Balance: Rs " + balance);
    }

    public double checkBalance() {
        return balance;
    }

    public static void main(String[] args) {
        MobileWallet w = new MobileWallet("Sheetal", 1000);
        w.addMoney(500);            // 1500
        w.sendMoney(2000, "Priya"); // rejected
        w.sendMoney(700, "Priya");  // 800
        System.out.println("Final: Rs " + w.checkBalance());
    }
}
```

Because `balance` is `private`, nobody outside can write `w.balance = -50000`. The only way in is through validated methods. That guarantee is the whole reason `private` exists.

---

## Section 4 — Encapsulation (Getters & Setters)

### Exercise 4.1 — `FlipkartUser` (validated email/phone, write-only password)

```java
public class FlipkartUser {
    private String email;
    private String phone;
    private String password;   // no getter — you can never READ it back

    public String getEmail() { return email; }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email must contain '@'");
        }
        this.email = email;
    }

    public String getPhone() { return phone; }

    public void setPhone(String phone) {
        if (phone == null || !phone.matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone must be exactly 10 digits");
        }
        this.phone = phone;
    }

    // Password: write-only via setter, never exposed via getter
    public void setPassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        this.password = password;
    }

    // The ONLY way to interact with the password: verify, never read
    public boolean verifyPassword(String attempt) {
        return password != null && password.equals(attempt);
    }

    public static void main(String[] args) {
        FlipkartUser u = new FlipkartUser();
        u.setEmail("sheetal@gmail.com");
        u.setPhone("9876543210");
        u.setPassword("secret123");

        System.out.println("Email: " + u.getEmail());
        System.out.println("Login (wrong): " + u.verifyPassword("oops"));     // false
        System.out.println("Login (right): " + u.verifyPassword("secret123")); // true

        try {
            u.setEmail("not-an-email");   // throws
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected: " + e.getMessage());
        }
    }
}
```

**The password trick is the star here** ⭐: a field with a setter but **no getter** is "write-only." That's exactly how real systems handle secrets — you can set/change them and verify against them, but never read them back. This is a great point to volunteer in an interview.

### Exercise 4.2 — `Temperature` (Celsius store, dual getters, absolute-zero guard)

```java
public class Temperature {
    private double celsius;   // single source of truth

    public Temperature(double celsius) {
        setCelsius(celsius);  // reuse validation
    }

    public double getCelsius() {
        return celsius;
    }

    // Fahrenheit is COMPUTED on the fly — no separate field to keep in sync
    public double getFahrenheit() {
        return (celsius * 9 / 5) + 32;
    }

    public void setCelsius(double celsius) {
        if (celsius < -273.15) {   // absolute zero
            throw new IllegalArgumentException("Below absolute zero (-273.15 C) is impossible");
        }
        this.celsius = celsius;
    }

    public static void main(String[] args) {
        Temperature t = new Temperature(37);
        System.out.println(t.getCelsius() + " C = " + t.getFahrenheit() + " F"); // 37.0 C = 98.6 F

        try {
            t.setCelsius(-300);   // throws
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected: " + e.getMessage());
        }
    }
}
```

**Design insight** 💡: there is no `fahrenheit` field. We store ONE canonical value (Celsius) and *derive* Fahrenheit. Storing both invites bugs where one is updated and the other goes stale. A getter that computes is perfectly legal and often the right call.

---

## Section 5 — Inheritance

### Exercise 5.1 — `Vehicle` → `Car` / `Bike` with overridden `describe()`

```java
public class Vehicle {
    protected String make;
    protected String model;
    protected int year;

    public Vehicle(String make, String model, int year) {
        this.make = make;
        this.model = model;
        this.year = year;
    }

    public void describe() {
        System.out.println(year + " " + make + " " + model);
    }
}

class Car extends Vehicle {
    private int numDoors;
    private double trunkSize;

    public Car(String make, String model, int year, int numDoors, double trunkSize) {
        super(make, model, year);   // parent constructor FIRST
        this.numDoors = numDoors;
        this.trunkSize = trunkSize;
    }

    @Override
    public void describe() {
        super.describe();   // reuse parent's line, then add our own
        System.out.println("  Car: " + numDoors + " doors, " + trunkSize + "L trunk");
    }
}

class Bike extends Vehicle {
    private int engineCC;
    private boolean hasCarrier;

    public Bike(String make, String model, int year, int engineCC, boolean hasCarrier) {
        super(make, model, year);
        this.engineCC = engineCC;
        this.hasCarrier = hasCarrier;
    }

    @Override
    public void describe() {
        super.describe();
        System.out.println("  Bike: " + engineCC + "cc, carrier=" + hasCarrier);
    }

    public static void main(String[] args) {
        Vehicle car = new Car("Maruti", "Swift", 2023, 4, 350);
        Vehicle bike = new Bike("Royal Enfield", "Classic", 2022, 350, true);
        car.describe();
        bike.describe();
    }
}
```

### Exercise 5.2 — `OlaRide` → `OlaMini` / `OlaPrime` with overridden fare

```java
public class OlaRide {
    protected String riderName;
    protected double distanceKm;

    public OlaRide(String riderName, double distanceKm) {
        this.riderName = riderName;
        this.distanceKm = distanceKm;
    }

    public double calculateFare() {
        return distanceKm * 8;   // base rate Rs 8/km
    }
}

class OlaMini extends OlaRide {
    public OlaMini(String riderName, double distanceKm) {
        super(riderName, distanceKm);
    }

    @Override
    public double calculateFare() {
        return 30 + distanceKm * 7;   // Rs 30 base + Rs 7/km
    }
}

class OlaPrime extends OlaRide {
    public OlaPrime(String riderName, double distanceKm) {
        super(riderName, distanceKm);
    }

    @Override
    public double calculateFare() {
        return 50 + distanceKm * 12;  // Rs 50 base + Rs 12/km
    }

    public static void main(String[] args) {
        OlaRide r1 = new OlaMini("Sheetal", 10);
        OlaRide r2 = new OlaPrime("Priya", 10);
        System.out.println("Mini : Rs " + r1.calculateFare());   // 100.0
        System.out.println("Prime: Rs " + r2.calculateFare());   // 170.0
    }
}
```

### Exercise 5.3 — Demonstrating `super` to call a parent method

This is shown inside `describe()` above (`super.describe()`), which prints the parent's line *and then* the child's extra detail. The `super` keyword does two distinct jobs: `super(...)` calls the parent **constructor**, and `super.method()` calls the parent's **method** even when the child has overridden it.

---

## Section 6 — Polymorphism

### Exercise 6.1 — `Notification` hierarchy processed as a mixed array

```java
public class Notification {
    protected String recipient;

    public Notification(String recipient) {
        this.recipient = recipient;
    }

    public void send() {
        System.out.println("Sending generic notification to " + recipient);
    }
}

class SMSNotification extends Notification {
    public SMSNotification(String recipient) { super(recipient); }
    @Override public void send() {
        System.out.println("SMS to " + recipient + ": your OTP is 4821");
    }
}

class EmailNotification extends Notification {
    public EmailNotification(String recipient) { super(recipient); }
    @Override public void send() {
        System.out.println("Email to " + recipient + ": Your order has shipped.");
    }
}

class PushNotification extends Notification {
    public PushNotification(String recipient) { super(recipient); }
    @Override public void send() {
        System.out.println("Push to " + recipient + ": Flash sale ends in 1 hour!");
    }

    public static void main(String[] args) {
        // Parent-typed array holding child objects — pure polymorphism
        Notification[] feed = {
            new SMSNotification("9876543210"),
            new EmailNotification("sheetal@gmail.com"),
            new PushNotification("device-token-xyz")
        };

        for (Notification n : feed) {
            n.send();   // each runs ITS OWN version, decided at runtime
        }
    }
}
```

### Exercise 6.2 + 6.3 — `Shape` hierarchy with upcasting, downcasting, `instanceof`

```java
public abstract class Shape {
    abstract double area();
}

class Circle extends Shape {
    double radius;
    Circle(double radius) { this.radius = radius; }
    @Override double area() { return Math.PI * radius * radius; }
    double circumference() { return 2 * Math.PI * radius; }  // Circle-specific
}

class Rectangle extends Shape {
    double width, height;
    Rectangle(double width, double height) { this.width = width; this.height = height; }
    @Override double area() { return width * height; }
}

class Triangle extends Shape {
    double base, height;
    Triangle(double base, double height) { this.base = base; this.height = height; }
    @Override double area() { return 0.5 * base * height; }

    public static void main(String[] args) {
        // Upcasting — implicit & always safe
        Shape[] shapes = { new Circle(5), new Rectangle(4, 6), new Triangle(3, 8) };

        for (Shape s : shapes) {
            System.out.printf("Area = %.2f%n", s.area());

            // instanceof guard before downcasting — SAFE pattern
            if (s instanceof Circle) {
                Circle c = (Circle) s;                       // explicit downcast
                System.out.printf("  (circle circumference = %.2f)%n", c.circumference());
            }
        }

        // What happens WITHOUT the instanceof guard (the trap):
        Shape s = new Rectangle(2, 2);
        // Circle bad = (Circle) s;  // would throw ClassCastException at runtime!
    }
}
```

**The golden safety rule** 🛡️: *upcasting* (child → parent) is automatic and always safe. *Downcasting* (parent → child) is a promise to the compiler — if you're wrong, you get a `ClassCastException` at runtime. Always guard with `instanceof` first.

---

## Section 7 — Abstraction (Abstract Classes & Interfaces)

### Exercise 7.1 — Abstract `BankAccount` with `calculateInterest()`

```java
public abstract class BankAccount {
    protected double balance;

    public BankAccount(double balance) {
        this.balance = balance;
    }

    // Abstract: every account type MUST define its own interest rule
    public abstract double calculateInterest();

    // Concrete: shared by all subclasses
    public void showInterest() {
        System.out.printf("Interest on Rs %.2f = Rs %.2f%n", balance, calculateInterest());
    }
}

class SavingsAccount extends BankAccount {
    public SavingsAccount(double balance) { super(balance); }
    @Override public double calculateInterest() { return balance * 0.04; } // 4%
}

class FixedDeposit extends BankAccount {
    public FixedDeposit(double balance) { super(balance); }
    @Override public double calculateInterest() { return balance * 0.07; } // 7%

    public static void main(String[] args) {
        BankAccount sav = new SavingsAccount(100000);
        BankAccount fd  = new FixedDeposit(100000);
        sav.showInterest();  // 4000
        fd.showInterest();   // 7000
        // new BankAccount(100); // ❌ won't compile — abstract class can't be instantiated
    }
}
```

### Exercise 7.2 — `Searchable` + `Filterable` implemented by `AmazonProduct`

```java
interface Searchable {
    boolean matchesKeyword(String keyword);
}

interface Filterable {
    boolean withinPriceRange(double min, double max);
}

class AmazonProduct implements Searchable, Filterable {   // implements MULTIPLE interfaces
    private String title;
    private double price;

    public AmazonProduct(String title, double price) {
        this.title = title;
        this.price = price;
    }

    @Override
    public boolean matchesKeyword(String keyword) {
        return title.toLowerCase().contains(keyword.toLowerCase());
    }

    @Override
    public boolean withinPriceRange(double min, double max) {
        return price >= min && price <= max;
    }

    public static void main(String[] args) {
        AmazonProduct p = new AmazonProduct("Boat Bluetooth Headphones", 1499);
        System.out.println(p.matchesKeyword("bluetooth"));    // true
        System.out.println(p.withinPriceRange(1000, 2000));   // true
    }
}
```

### Exercise 7.3 — `PaymentProcessor` interface with 3 implementations

```java
interface PaymentProcessor {
    void processPayment(double amount);
    void refund(double amount);

    // Java 8 default method — every processor gets this free
    default void logTransaction(String type, double amount) {
        System.out.println("[LOG] " + type + " of Rs " + amount);
    }
}

class PaytmProcessor implements PaymentProcessor {
    @Override public void processPayment(double amount) {
        logTransaction("Paytm payment", amount);
        System.out.println("Paid Rs " + amount + " via Paytm wallet");
    }
    @Override public void refund(double amount) {
        System.out.println("Refunded Rs " + amount + " to Paytm wallet");
    }
}

class PhonePeProcessor implements PaymentProcessor {
    @Override public void processPayment(double amount) {
        logTransaction("PhonePe payment", amount);
        System.out.println("Paid Rs " + amount + " via PhonePe UPI");
    }
    @Override public void refund(double amount) {
        System.out.println("Refunded Rs " + amount + " to PhonePe UPI");
    }
}

class GPayProcessor implements PaymentProcessor {
    @Override public void processPayment(double amount) {
        logTransaction("GPay payment", amount);
        System.out.println("Paid Rs " + amount + " via Google Pay");
    }
    @Override public void refund(double amount) {
        System.out.println("Refunded Rs " + amount + " to Google Pay");
    }

    public static void main(String[] args) {
        // Program to the INTERFACE, not the implementation
        PaymentProcessor[] processors = {
            new PaytmProcessor(), new PhonePeProcessor(), new GPayProcessor()
        };
        for (PaymentProcessor pp : processors) {
            pp.processPayment(500);
        }
    }
}
```

**Interface superpower** 🚀: the loop holds `PaymentProcessor` references and doesn't care which concrete class it is. Add a `CredProcessor` tomorrow — the loop doesn't change. This is "program to an interface, not an implementation."

---

## Section 8 — `static` Keyword

### Exercise 8.1 — `Student` with a static counter

```java
public class Student {
    private String name;
    private static int totalStudents = 0;  // shared across ALL students

    public Student(String name) {
        this.name = name;
        totalStudents++;   // every new student bumps the shared counter
    }

    public static int getTotalStudents() {
        return totalStudents;
    }

    public static void main(String[] args) {
        new Student("Sheetal");
        new Student("Priya");
        new Student("Rahul");
        System.out.println("Total students: " + Student.getTotalStudents()); // 3
    }
}
```

### Exercise 8.2 — `StringHelper` utility (all static)

```java
public class StringHelper {

    public static boolean isPalindrome(String s) {
        if (s == null) return false;
        String clean = s.toLowerCase().replaceAll("[^a-z0-9]", "");
        int i = 0, j = clean.length() - 1;
        while (i < j) {
            if (clean.charAt(i) != clean.charAt(j)) return false;
            i++; j--;
        }
        return true;
    }

    public static int countVowels(String s) {
        if (s == null) return 0;
        int count = 0;
        for (char c : s.toLowerCase().toCharArray()) {
            if ("aeiou".indexOf(c) >= 0) count++;
        }
        return count;
    }

    public static String reverseString(String s) {
        if (s == null) return null;
        return new StringBuilder(s).reverse().toString();
    }

    public static void main(String[] args) {
        System.out.println(isPalindrome("Madam"));     // true
        System.out.println(countVowels("Education"));  // 5
        System.out.println(reverseString("Sheetal"));  // lateehS
    }
}
```

A utility class like this is a classic `static` use case — there's no per-object state, so you never need to create a `StringHelper` object. You call `StringHelper.reverseString(...)` directly.

### Exercise 8.3 — Why is `main` static? (written answer)

`public static void main(String[] args)` is the JVM's entry point. It must be `static` because **the JVM has to call it before any object exists**. When you run `java Student`, the JVM hasn't created a single object yet — there's nothing to call an instance method on. Because `static` methods belong to the *class* rather than to an object, the JVM can invoke `main` straight from the class. If `main` were an instance method, the JVM would face a chicken-and-egg problem: it needs an object to call `main`, but it needs `main` to start the program that creates objects. `static` breaks that loop. 🥚🐔

---

## Section 9 — `final` Keyword

There are no separate numbered exercises here, so here are quick worked demonstrations of all three uses.

```java
public class FinalDemo {

    // final variable — a true constant the compiler enforces
    static final double GST_RATE = 0.18;

    // final method — subclasses cannot override it
    final double addGst(double amount) {
        return amount + (amount * GST_RATE);
    }

    public static void main(String[] args) {
        // GST_RATE = 0.12;  // ❌ compile error: cannot assign a value to final variable
        FinalDemo d = new FinalDemo();
        System.out.println("With GST: Rs " + d.addGst(1000)); // 1180.0
    }
}

// final class — cannot be extended (just like java.lang.String)
final class TaxConfig { }
// class Hack extends TaxConfig { } // ❌ compile error: cannot inherit from final TaxConfig
```

**Subtle trap to remember** ⚠️: `final` on an *object reference* means you can't reassign the reference, but the object's internals can still change.
```java
final StringBuilder sb = new StringBuilder("hi");
sb.append(" there");   // ✅ allowed — we're mutating, not reassigning
// sb = new StringBuilder(); // ❌ not allowed — reassigning a final reference
```

---

## Section 10 — `Object` Class (`toString` / `equals` / `hashCode`)

### Exercise 10.1 + 10.2 — `Product` with all three, plus `HashSet` de-dup

```java
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Product {
    private int productId;
    private String name;

    public Product(int productId, String name) {
        this.productId = productId;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Product{id=" + productId + ", name='" + name + "'}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Product other = (Product) obj;
        return productId == other.productId && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, name);
    }

    public static void main(String[] args) {
        Product a = new Product(101, "iPhone");
        Product b = new Product(101, "iPhone");  // logically identical to 'a'

        System.out.println(a);             // Product{id=101, name='iPhone'}
        System.out.println(a == b);        // false — different objects in memory
        System.out.println(a.equals(b));   // true  — same content

        Set<Product> set = new HashSet<>();
        set.add(a);
        set.add(b);
        System.out.println("Set size: " + set.size()); // 1 — b is a duplicate
    }
}
```

### Exercise 10.3 — Break it: `equals()` WITHOUT `hashCode()`

```java
import java.util.HashSet;
import java.util.Set;

public class BrokenProduct {
    private int productId;

    public BrokenProduct(int productId) { this.productId = productId; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return productId == ((BrokenProduct) obj).productId;
    }
    // ❌ Deliberately NO hashCode() override — inherits Object's identity hashCode

    public static void main(String[] args) {
        Set<BrokenProduct> set = new HashSet<>();
        set.add(new BrokenProduct(101));
        set.add(new BrokenProduct(101));   // "equal" but different hashCode

        System.out.println("Set size: " + set.size()); // 2 — BUG! Should be 1
    }
}
```

**What went wrong** 🐛: `HashSet` first uses `hashCode()` to pick a bucket, *then* uses `equals()` within that bucket. Two "equal" objects with different hashcodes land in different buckets, so the set never even compares them with `equals()` — the duplicate sneaks in. **Rule: if you override `equals`, you MUST override `hashCode`.** They are a package deal.

---

## Section 11 — Enums

### Exercise 11.1 — `RideType` enum with base fare + per-km rate

```java
public enum RideType {
    MINI(30, 7),
    SEDAN(50, 11),
    PRIME(50, 12),
    AUTO(20, 5);

    private final double baseFare;
    private final double perKmRate;

    RideType(double baseFare, double perKmRate) {   // enum constructors are always private
        this.baseFare = baseFare;
        this.perKmRate = perKmRate;
    }

    public double fareFor(double km) {
        return baseFare + perKmRate * km;
    }

    public static void main(String[] args) {
        for (RideType type : RideType.values()) {
            System.out.printf("%-6s 10km fare = Rs %.2f%n", type, type.fareFor(10));
        }
    }
}
```

### Exercise 11.2 — `City` enum with population + state

```java
public enum City {
    MUMBAI(20_400_000, "Maharashtra"),
    DELHI(16_800_000, "Delhi"),
    BANGALORE(12_300_000, "Karnataka"),
    CHENNAI(7_000_000, "Tamil Nadu");

    private final int population;
    private final String state;

    City(int population, String state) {
        this.population = population;
        this.state = state;
    }

    public int getPopulation() { return population; }
    public String getState() { return state; }

    public static void main(String[] args) {
        City c = City.BANGALORE;
        System.out.println(c + " is in " + c.getState()
                + ", pop ~" + c.getPopulation());
    }
}
```

### Exercise 11.3 — `DayOfWeek` weekday/weekend classifier

```java
public class DayClassifier {
    enum DayOfWeek { MON, TUE, WED, THU, FRI, SAT, SUN }

    static String classify(DayOfWeek day) {
        switch (day) {
            case SAT:
            case SUN:
                return "Weekend";
            default:
                return "Weekday";
        }
    }

    public static void main(String[] args) {
        System.out.println(classify(DayOfWeek.WED)); // Weekday
        System.out.println(classify(DayOfWeek.SAT)); // Weekend
    }
}
```

---

## Capstone — Ola Ride Booking System (uses EVERY Week-2 concept)

This single program demonstrates: **abstract class**, **inheritance**, **method overriding (polymorphism)**, **two interfaces**, **enums with fields**, **static counter**, **encapsulation**, and **`toString`/`equals`/`hashCode`**.

```java
import java.util.Objects;

// --- Enums (4 & 3) ---
enum RideStatus { REQUESTED, DRIVER_ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED }

enum PaymentMethod {
    UPI("Instant"), CARD("1-2 days"), CASH("On trip end"), WALLET("Instant");
    private final String settlement;
    PaymentMethod(String settlement) { this.settlement = settlement; }
    public String getSettlement() { return settlement; }
}

// --- Interfaces (5 & 6) ---
interface Trackable {
    String getLocation();
    int getETA();   // minutes
}

interface Payable {
    void processPayment();
    void generateReceipt();
}

// --- Abstract base (1) with encapsulation (9), static counter (8),
//     and Object overrides (10). Implements both interfaces (7). ---
abstract class Ride implements Trackable, Payable {

    private static int totalRides = 0;      // shared across ALL ride types

    private final int rideId;
    private String riderName;               // private fields → encapsulation
    private double distanceKm;
    private RideStatus status;
    private PaymentMethod paymentMethod;
    private String location = "Start point";

    public Ride(String riderName, double distanceKm, PaymentMethod paymentMethod) {
        this.rideId = ++totalRides;         // assign id AND bump counter
        this.riderName = riderName;
        this.distanceKm = distanceKm;
        this.paymentMethod = paymentMethod;
        this.status = RideStatus.REQUESTED;
    }

    // Abstract: each ride type prices differently (2)
    public abstract double calculateFare();

    // --- Getters / Setters (encapsulation) ---
    public int getRideId() { return rideId; }
    public String getRiderName() { return riderName; }
    public double getDistanceKm() { return distanceKm; }
    public RideStatus getStatus() { return status; }
    public void setStatus(RideStatus status) { this.status = status; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public static int getTotalRides() { return totalRides; }

    // --- Trackable ---
    @Override public String getLocation() { return location; }
    @Override public int getETA() { return (int) Math.ceil(distanceKm * 2.5); } // ~2.5 min/km

    // --- Payable ---
    @Override
    public void processPayment() {
        setStatus(RideStatus.COMPLETED);
        System.out.printf("Paid Rs %.2f via %s (settles %s)%n",
                calculateFare(), paymentMethod, paymentMethod.getSettlement());
    }
    @Override
    public void generateReceipt() {
        System.out.println("Receipt #" + rideId + " for " + riderName
                + " | Rs " + String.format("%.2f", calculateFare()));
    }

    // --- Object overrides (10) ---
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + rideId + ", rider='" + riderName
                + "', km=" + distanceKm + ", fare=Rs " + String.format("%.2f", calculateFare())
                + ", status=" + status + "}";
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ride other = (Ride) o;
        return rideId == other.rideId;   // rideId uniquely identifies a ride
    }
    @Override
    public int hashCode() { return Objects.hash(rideId); }
}

// --- Concrete ride types (2): each overrides calculateFare() ---
class OlaMini extends Ride {
    public OlaMini(String rider, double km, PaymentMethod pm) { super(rider, km, pm); }
    @Override public double calculateFare() { return 30 + getDistanceKm() * 7; }
}
class OlaPrime extends Ride {
    public OlaPrime(String rider, double km, PaymentMethod pm) { super(rider, km, pm); }
    @Override public double calculateFare() { return 50 + getDistanceKm() * 12; }
}
class OlaAuto extends Ride {
    public OlaAuto(String rider, double km, PaymentMethod pm) { super(rider, km, pm); }
    @Override public double calculateFare() { return 20 + getDistanceKm() * 5; }
}

// --- Driver program: polymorphism via a Ride[] array (11) ---
public class OlaBookingSystem {
    public static void main(String[] args) {
        Ride[] rides = {
            new OlaMini("Sheetal", 8, PaymentMethod.UPI),
            new OlaPrime("Priya", 15, PaymentMethod.CARD),
            new OlaAuto("Rahul", 4, PaymentMethod.CASH)
        };

        for (Ride r : rides) {
            r.setStatus(RideStatus.DRIVER_ASSIGNED);
            System.out.println(r);                       // toString()
            System.out.println("  ETA: " + r.getETA() + " min, at " + r.getLocation());
            r.processPayment();                          // polymorphic fare + status update
            r.generateReceipt();
            System.out.println();
        }

        System.out.println("Total rides booked: " + Ride.getTotalRides()); // 3

        // equals/hashCode demo
        Ride a = rides[0];
        System.out.println("a.equals(itself): " + a.equals(rides[0])); // true
    }
}
```

**Why this project is the perfect interview talking point** 🎯: if asked "have you built anything with OOP?", walk through this. Point at the abstract `Ride` (abstraction + shared state), the three subclasses (inheritance + overriding = polymorphism), the two interfaces (capabilities, multiple inheritance of *type*), the `static totalRides` (class-level state), the private fields with getters (encapsulation), and the `equals`/`hashCode` pair. That single example touches every pillar.

---

# PART B — Interview Questions & Model Answers

> Answers below are written in a **confident, spoken style** — read them aloud once and they'll feel natural in the room. 🎤

### Q1. What are the four pillars of OOP, in Java terms?

"There are four: **Encapsulation, Inheritance, Polymorphism, and Abstraction**. In Java specifically — Encapsulation is making fields `private` and exposing them through getters/setters so I control access; Inheritance is the `extends` keyword letting a child reuse a parent's code with an IS-A relationship; Polymorphism is the same call behaving differently depending on the actual object, achieved through method overriding plus `@Override`; and Abstraction is hiding the *how* and exposing only the *what*, using `abstract` classes and `interface`s. A quick memory aid is **'A PIE'** — Abstraction, Polymorphism, Inheritance, Encapsulation."

### Q2. Difference between an abstract class and an interface? When do you pick which?

"An **abstract class** is for an **IS-A** relationship where subclasses share state and some common code — it can have instance fields, constructors, and a mix of concrete and abstract methods, but a class can only extend one. An **interface** is for a **CAN-DO** capability — it's a contract, can be implemented by many unrelated classes, and a class can implement several, which is how Java does multiple inheritance of *type*. My rule of thumb: if I want to share *implementation and state*, abstract class; if I just want to guarantee a *capability*, interface. Concretely — `Ride` is an abstract class because all rides share `riderName` and `distanceKm`, but `Payable` and `Trackable` are interfaces because totally unrelated things — a driver, a restaurant, a customer — could all be payable."

**Comparison table:**

| Feature | Abstract Class | Interface |
|---|---|---|
| Relationship | IS-A | CAN-DO |
| Instance fields | ✅ Yes | ❌ Only `public static final` constants |
| Constructor | ✅ Yes | ❌ No |
| Method bodies | Concrete + abstract | Abstract; `default`/`static` since Java 8; `private` since Java 9 |
| How many can you have | One (`extends`) | Many (`implements`) |
| Keyword | `extends` | `implements` |

### Q3. Overloading vs Overriding — and which is compile-time vs runtime?

"**Overloading** is *same method name, different parameter list* in the **same class** — it's resolved by the compiler, so it's **compile-time (static) polymorphism**. **Overriding** is a *child class redefining a parent method with the same signature* — it's resolved by the JVM based on the actual object at runtime, so it's **runtime (dynamic) polymorphism**. The trap interviewers love: return type alone does NOT distinguish an overload — you need different parameters. And you can't override a `static`, `final`, or `private` method."

| | Overloading | Overriding |
|---|---|---|
| Where | Same class | Parent → child |
| Signature | Different parameters | Identical signature |
| Resolved | Compile time | Runtime |
| Polymorphism type | Static | Dynamic |
| `@Override` applies | No | Yes (recommended) |

### Q4. What's the difference between `this` and `super`?

"`this` refers to the **current object** — I use it to disambiguate a field from a parameter of the same name, like `this.name = name`, or to call another constructor in the same class with `this(...)`. `super` refers to the **parent class** — I use `super(...)` to call the parent's constructor as the first line of a child constructor, or `super.method()` to call the parent's version of a method I've overridden. So `this` looks at the current class, `super` looks one level up the inheritance chain."

### Q5. What is a constructor? Does Java give you one for free?

"A constructor is a special method with **the same name as the class and no return type** — it runs automatically when you `new` an object, to initialize it. Java provides a **default no-arg constructor only if you write no constructor at all**. The moment I declare even one constructor — say a parameterized one — that free default disappears, and `new MyClass()` will fail to compile unless I add the no-arg one back myself. That's a very common gotcha."

### Q6. Trap: "Constructors can be overridden." True or false?

"**False.** Constructors are **not inherited**, so they can't be overridden. They can be **overloaded** — multiple constructors with different parameter lists in the same class — but a child class does not inherit its parent's constructors; it can only *call* them via `super(...)`. People confuse 'I wrote several constructors' (overloading) with overriding."

### Q7. `static` vs instance members — and why can't a static method use instance fields?

"A **static** member belongs to the **class** and is shared by every object — like a counter of total rides. An **instance** member belongs to a specific object — each ride's rider name. A static method **can't directly touch an instance field** because a static method runs without any particular object, so there's no `this` — the JVM wouldn't know *which* object's field you mean. That's also exactly why `main` is static: the JVM calls it before any object exists."

### Q8. What does `final` do in its three positions?

"`final` means 'can't change', and it means three different things by position. On a **variable**, it's a constant — assign once, never reassign. On a **method**, it can't be overridden by a subclass — useful for locking down security-sensitive logic. On a **class**, it can't be extended at all — that's why `String`, `Integer`, and `Double` are final, which is part of how they stay immutable. One subtlety: `final` on an object reference stops *reassignment*, not *mutation* — a `final List` can't point to a new list, but you can still `add()` to it."

### Q9. Explain the four access modifiers from most to least restrictive.

"From most open to most closed: **`public`** — visible everywhere; **`protected`** — same package plus subclasses anywhere; **default** (no keyword, a.k.a. package-private) — same package only; and **`private`** — same class only. My default instinct is to make everything `private` and only widen access when there's a real reason — least privilege. The one people forget is **default isn't public** — leaving off the keyword does NOT make it public, it makes it package-private."

| Modifier | Same class | Same package | Subclass (other pkg) | Everywhere |
|---|---|---|---|---|
| `public` | ✅ | ✅ | ✅ | ✅ |
| `protected` | ✅ | ✅ | ✅ | ❌ |
| default | ✅ | ✅ | ❌ | ❌ |
| `private` | ✅ | ❌ | ❌ | ❌ |

### Q10. What is encapsulation and what does it buy you?

"Encapsulation is **bundling data with the methods that operate on it, and hiding the data behind a controlled interface** — private fields, public getters/setters. The payoff is threefold: I can **validate** on write (reject a negative salary), I can **change the internal representation** later without breaking callers (store Celsius, expose Fahrenheit computed on the fly), and I can make fields **read-only or write-only** (a password with a setter but no getter). It's the ATM analogy — you interact through the screen and keypad, never reach into the cash drawer directly."

### Q11. Composition vs Inheritance — what do you prefer?

"Inheritance is an **IS-A** relationship — a `Car` IS-A `Vehicle`. Composition is a **HAS-A** relationship — a `Car` HAS-A `Engine`. The modern guidance is **'favor composition over inheritance'**, because inheritance is rigid: it tightly couples child to parent, exposes you to the fragile-base-class problem, and Java only allows one parent. Composition is more flexible — I can swap the composed part, and I can compose many behaviors. I reach for inheritance only when there's a genuine, stable IS-A; otherwise I compose. For shared *capabilities* across unrelated classes, I use interfaces."

```java
// Inheritance (IS-A)              // Composition (HAS-A)
class Car extends Vehicle { }      class Car { private Engine engine; }
```

### Q12. Why does Java NOT support multiple inheritance of classes?

"To avoid the **Diamond Problem**. If `C` could extend both `A` and `B`, and both define `getStatus()`, then `C` wouldn't know which one to inherit — it's ambiguous. So Java allows only **single class inheritance**. But you still get multiple inheritance of *type* through **interfaces** — a class can implement many. And since Java 8, interfaces can have `default` methods, so the diamond *can* reappear there — but Java forces you to resolve it explicitly by overriding the method and choosing `InterfaceName.super.method()`."

### Q13. What is upcasting and downcasting? Where's the danger?

"**Upcasting** is treating a child object as its parent type — `Shape s = new Circle()`. It's implicit and always safe because a `Circle` truly IS-A `Shape`. **Downcasting** is the reverse — `Circle c = (Circle) s` — and it's risky, because the compiler trusts your cast but at runtime it'll throw `ClassCastException` if `s` isn't actually a `Circle`. So before downcasting I always guard with `instanceof`. As of Java 16 you can even combine them with pattern matching: `if (s instanceof Circle c) { ... }`."

### Q14. Why must you override `hashCode()` whenever you override `equals()`?

"Because hash-based collections like `HashMap` and `HashSet` rely on a contract: **equal objects must have equal hash codes**. They first use `hashCode()` to find the right bucket, then `equals()` within it. If I override `equals()` but leave the default identity `hashCode()`, two logically-equal objects can land in different buckets — so a `HashSet` won't recognize the duplicate, and a `HashMap` lookup with an equal-but-not-same key returns null. The collection breaks silently, which is the worst kind of bug. So they're always overridden together — I usually use `Objects.hash(...)` and `Objects.equals(...)`."

### Q15. Trap: "If two objects have the same `hashCode`, they are equal." True or false?

"**False** — that's the reverse of the contract. The contract only guarantees: *if two objects are equal, their hashcodes are equal.* The converse doesn't hold — two **unequal** objects can absolutely share a hashcode; that's a **hash collision**, and it's normal and expected because hashcodes are `int`s mapping an infinite object space. So same hashcode means 'maybe equal, go check `equals()`'; different hashcode means 'definitely not equal'."

### Q16. What changed about interfaces in Java 8 (and after)?

"Before Java 8, interfaces were pure — only abstract methods and constants. The problem was **evolution**: adding a method to a published interface broke every implementer. Java 8 fixed this with **`default` methods** — methods with a body that implementers inherit for free, so you can extend an interface without breaking anyone — plus **`static` methods** on interfaces. Java 9 then added **`private` interface methods** to share code between default methods. That's why I could give `PaymentProcessor` a `default logTransaction()` and every processor got it automatically without implementing it."

### Q17. (Bonus) Can a class be both `abstract` and `final`? Can a method?

"**No** — those keywords are contradictory. `abstract` means 'must be subclassed/implemented'; `final` means 'cannot be subclassed/overridden'. So `abstract final` doesn't compile, on either a class or a method. Same logic: an `abstract` method can't be `private` either, because a private method can't be seen — let alone overridden — by a subclass."

### Q18. (Bonus) What's the difference between `==` and `.equals()`?

"`==` compares **references** for objects — are these literally the same object in memory? `.equals()` compares **content**, if it's been overridden. For primitives, `==` compares values directly. The classic gotcha is `String` — two strings with the same text can be different objects, so `==` may be `false` while `.equals()` is `true`. Rule: use `==` for primitives and identity checks, `.equals()` for object content."

---

# 🧠 Memory Hooks

Vivid analogies that make each concept *stick* in the interview room. When your mind blanks, recall the picture first — the definition follows.

| Concept | Analogy 🎬 | Trigger — use it when... |
|---|---|---|
| Class vs Object | Recipe vs the actual dish 🍲 | Explaining "blueprint produces many instances" |
| Constructor | Filling a baby's birth certificate at birth 👶 | Asked why initialization happens automatically |
| `this` keyword | Pointing at *yourself* 👈 | Field and parameter share a name |
| `super` keyword | Asking *your parent* 👆 | Calling parent constructor or overridden method |
| Encapsulation | ATM — keypad in, no reaching into the cash drawer 🏧 | Justifying private + getters/setters |
| Write-only field | A locked drop-box — you can post, never peek 🔒 | Passwords / secrets with setter, no getter |
| Access modifiers | House rooms: front door / bedroom / family room / building lobby 🏠 | Explaining public/private/protected/default |
| Inheritance | Zomato: BikePartner IS-A DeliveryPartner 🛵 | IS-A relationship, code reuse |
| Composition | A Car HAS-A Engine 🚗⚙️ | HAS-A; favor over inheritance |
| Polymorphism | One "Pay" button, many behaviors 💳 | Same call, different object, different result |
| Upcast / Downcast | Promotion is automatic ⬆️; demotion needs proof ⬇️ | Casting between parent/child types |
| Abstract class | A half-built house — frame given, you finish rooms 🏗️ | Shared state + some methods left to subclasses |
| Interface | A contract / job description 📜 | Capability shared by unrelated classes |
| `default` method (Java 8) | A free firmware update to the contract 📲 | Evolving an interface without breaking implementers |
| `static` | The class teacher's name — same for every student 👩‍🏫 | Shared across all objects; `main`; utility methods |
| `final` | Aadhaar number / Supreme Court verdict / sealed envelope 🪪⚖️✉️ | Constant / no-override / no-extend |
| `equals` + `hashCode` | Twins must carry the same ID badge 👯 | Hash collections; always override together |
| Enum | Order status: can be DELIVERED, never "FLYING" ✈️🚫 | Fixed, finite set of valid values |
| Diamond Problem | Two parents giving conflicting orders 💎 | Why Java bans multiple class inheritance |

---

## 🎯 Final Interview Tip

When you get an OOP question, **resist the urge to define-and-stop**. The candidates who get hired don't just recite "encapsulation is private fields and getters/setters" — they finish the thought with *why it matters*: "...so I can validate input, change internals later without breaking callers, and make fields read-only when needed." Always close the loop with the **design payoff**.

And when you can, **anchor your answer in a concrete example you've actually written** — your `Ola Ride Booking System` from the capstone is gold here. Saying "in a ride-booking system I built, `Ride` was abstract because all rides share `riderName` and `distanceKm`, but `Payable` was an interface because a restaurant could be payable too" shows real understanding, not memorization. Definition → analogy → code → why it matters. Every single time. You've got this. 💪
