# LLD Week 1 — Interview Answers & Exercise Solutions (OOP + SOLID)

> This file has **two parts**:
> 1. **Part A — Exercise Solutions:** Full worked answers to all 4 practice exercises from `Plan.md`.
> 2. **Part B — Interview Q&A:** The exact OOP + SOLID questions Microsoft/Amazon/Google ask, with model answers you can speak out loud.
>
> 🎯 **Interview golden rule for LLD:** They don't just want the *definition*. They want
> **definition → real-life analogy → bad code → good code → why it matters.** Always answer in that shape.

---

# PART A — EXERCISE SOLUTIONS

---

## ✅ Exercise 1: Identify the OOP Pillar

| # | Scenario | Pillar | Why |
|---|----------|--------|-----|
| 1 | `Logger` hides whether it writes to file/console/cloud — you just call `log(message)` | **Abstraction** | You see WHAT (`log`), not HOW (file vs cloud). The complexity is hidden behind a simple interface. |
| 2 | `Shape` → `Circle`, `Rectangle`, `Triangle` all have `area()` but calculate differently | **Polymorphism** | Same method name `area()`, different behavior per subclass. (Also uses inheritance to share the `Shape` type.) |
| 3 | `Dog`, `Cat`, `Fish` inherit `name`, `age`, `eat()` from `Animal` | **Inheritance** | Children get the parent's fields and methods for free — write common code once. |
| 4 | You can call `getBalance()` but can't directly access `balance` | **Encapsulation** | Data is private; access is only through controlled public methods. |

> ⚠️ **Common trap:** #1 vs #4. *Abstraction* = hiding **complexity** (don't care how it works). *Encapsulation* = hiding **data** (can't touch the variable directly). Interviewers love this distinction — see Part B Q5.

---

## ✅ Exercise 2: Spot the SOLID Violation

```java
public class ReportGenerator {
    public void fetchDataFromDatabase() { /* ... */ }
    public void processData()           { /* ... */ }
    public void generatePdf()           { /* ... */ }
    public void sendEmailWithReport()   { /* ... */ }
    public void uploadToS3()            { /* ... */ }
    public void notifySlack()           { /* ... */ }
}
```

### 1. Which principle does it violate?
**Single Responsibility Principle (SRP)** — and it sets up an **OCP/DIP** violation too.

This one class has **6 reasons to change**:
1. Database schema changes → change this class
2. Report format changes (PDF → Excel) → change this class
3. Email provider changes (SMTP → SendGrid) → change this class
4. Storage changes (S3 → Azure Blob) → change this class
5. Slack API changes → change this class
6. Data-processing rules change → change this class

> A class fetching data **and** generating PDFs **and** sending emails **and** uploading to S3 **and** pinging Slack is doing the job of 5 specialists. If the email library breaks, you're editing the same file that does database queries — risky.

### 2. How to refactor — split by responsibility

```java
// Each class = ONE job, ONE reason to change

class ReportDataRepository {          // ONLY fetches data
    public ReportData fetch() { /* query DB */ return new ReportData(); }
}

class ReportProcessor {               // ONLY transforms data
    public ReportData process(ReportData raw) { /* business logic */ return raw; }
}

class PdfReportRenderer {             // ONLY builds the PDF
    public byte[] render(ReportData data) { /* make PDF */ return new byte[0]; }
}

class EmailSender {                   // ONLY sends email
    public void send(String to, byte[] attachment) { /* SMTP/SendGrid */ }
}

class S3Uploader {                    // ONLY uploads
    public String upload(byte[] file) { /* put to S3 */ return "s3://..."; }
}

class SlackNotifier {                 // ONLY notifies Slack
    public void notify(String message) { /* Slack webhook */ }
}

// An orchestrator wires them together — it COORDINATES, it doesn't DO the work itself
class ReportWorkflow {
    private final ReportDataRepository repo;
    private final ReportProcessor processor;
    private final PdfReportRenderer renderer;
    private final EmailSender email;
    private final S3Uploader uploader;
    private final SlackNotifier slack;

    public ReportWorkflow(ReportDataRepository repo, ReportProcessor processor,
                          PdfReportRenderer renderer, EmailSender email,
                          S3Uploader uploader, SlackNotifier slack) {
        this.repo = repo; this.processor = processor; this.renderer = renderer;
        this.email = email; this.uploader = uploader; this.slack = slack;
    }

    public void run(String recipient) {
        ReportData data = processor.process(repo.fetch());
        byte[] pdf = renderer.render(data);
        String url = uploader.upload(pdf);
        email.send(recipient, pdf);
        slack.notify("Report ready: " + url);
    }
}
```

> Now: email provider changes → only `EmailSender` changes. PDF → Excel → only the renderer changes (and thanks to an interface, you can add `ExcelReportRenderer` without touching anything — that's OCP).

---

## ✅ Exercise 3: Design a Notification System

**Requirements:** Email, SMS, Push, WhatsApp. Adding Telegram must NOT change existing code (OCP). Each type has its own logic (Polymorphism). Main app shouldn't know the concrete type (DIP).

```java
import java.util.List;

// 1. THE ABSTRACTION (the "socket") — every notifier must follow this contract
interface Notifier {
    void send(String recipient, String message);
    String channelName();
}

// 2. CONCRETE IMPLEMENTATIONS — each one decides HOW (Polymorphism)
class EmailNotifier implements Notifier {
    public void send(String recipient, String message) {
        System.out.println("📧 Email to " + recipient + ": " + message);
    }
    public String channelName() { return "Email"; }
}

class SmsNotifier implements Notifier {
    public void send(String recipient, String message) {
        System.out.println("📱 SMS to " + recipient + ": " + message);
    }
    public String channelName() { return "SMS"; }
}

class PushNotifier implements Notifier {
    public void send(String recipient, String message) {
        System.out.println("🔔 Push to device " + recipient + ": " + message);
    }
    public String channelName() { return "Push"; }
}

class WhatsAppNotifier implements Notifier {
    public void send(String recipient, String message) {
        System.out.println("💬 WhatsApp to " + recipient + ": " + message);
    }
    public String channelName() { return "WhatsApp"; }
}

// 3. HIGH-LEVEL SERVICE — depends on the abstraction (Notifier), NOT concrete classes (DIP)
class NotificationService {
    private final List<Notifier> notifiers;

    // Dependency Injection: the channels are PASSED IN, not created inside
    public NotificationService(List<Notifier> notifiers) {
        this.notifiers = notifiers;
    }

    public void notifyAll(String recipient, String message) {
        for (Notifier n : notifiers) {
            System.out.println("Sending via " + n.channelName() + "...");
            n.send(recipient, message);
        }
    }
}

// 4. USAGE
public class Main {
    public static void main(String[] args) {
        NotificationService service = new NotificationService(List.of(
            new EmailNotifier(),
            new SmsNotifier(),
            new WhatsAppNotifier()
        ));
        service.notifyAll("sheetal@gmail.com", "Your order is confirmed!");

        // ➕ Adding Telegram later = just a NEW class. ZERO changes above. (OCP ✅)
    }
}

// Added 3 months later — existing code untouched
class TelegramNotifier implements Notifier {
    public void send(String recipient, String message) {
        System.out.println("✈️ Telegram to " + recipient + ": " + message);
    }
    public String channelName() { return "Telegram"; }
}
```

### Which principle does each part satisfy?
| Part | Principle |
|------|-----------|
| `Notifier` interface | **Abstraction** + **DIP** (high-level code depends on it) |
| Each `*Notifier` has its own `send()` | **Polymorphism** + **SRP** (each = one channel) |
| Adding `TelegramNotifier` without edits | **Open/Closed (OCP)** |
| `NotificationService` takes notifiers in the constructor | **Dependency Injection** (enables DIP) |
| Small, single-method `Notifier` | **Interface Segregation (ISP)** |

---

## ✅ Exercise 4: Real-World Refactoring (OnlineStore)

```java
public class OnlineStore {
    public void addProduct(String name, double price) { }
    public void removeProduct(String productId) { }
    public double calculateTotal(Cart cart) { return 0; }
    public void applyDiscount(Cart cart, String discountType) {
        if (discountType.equals("percentage")) { }
        else if (discountType.equals("flat")) { }
        else if (discountType.equals("bogo")) { }
    }
    public void chargeCreditCard(double amount, String cardNumber) { }
    public void chargeUpi(double amount, String upiId) { }
    public void sendOrderConfirmation(Order order) { }
    public void generateInvoice(Order order) { }
    public void updateInventory(String productId, int quantity) { }
    public void trackDelivery(String orderId) { }
}
```

### Violations found
| Principle | Violation |
|-----------|-----------|
| **SRP** | One class has ~6 responsibilities: product catalog, pricing/discount, payment, notifications, invoicing, inventory, delivery. |
| **OCP** | `applyDiscount` is an `if/else` chain — a new discount type forces you to **edit** this method. Same for payments (`chargeCreditCard`, `chargeUpi` — adding wallet means a new method here). |
| **DIP** | Payment is hardcoded as concrete methods, not an abstraction. Can't swap or test payment in isolation. |

### Refactored design

```java
// ---------- SRP: split into focused classes ----------
class ProductCatalog {
    public void add(String name, double price) { /* ... */ }
    public void remove(String productId) { /* ... */ }
}

class InventoryService {
    public void update(String productId, int quantity) { /* ... */ }
}

class InvoiceService {
    public void generate(Order order) { /* ... */ }
}

class DeliveryTracker {
    public void track(String orderId) { /* ... */ }
}

// ---------- OCP: discounts as strategies (add new = new class) ----------
interface DiscountStrategy {
    double apply(double amount);
}
class PercentageDiscount implements DiscountStrategy {
    private final double percent;
    public PercentageDiscount(double percent) { this.percent = percent; }
    public double apply(double amount) { return amount - amount * percent / 100; }
}
class FlatDiscount implements DiscountStrategy {
    private final double off;
    public FlatDiscount(double off) { this.off = off; }
    public double apply(double amount) { return Math.max(0, amount - off); }
}
class BogoDiscount implements DiscountStrategy {
    public double apply(double amount) { return amount / 2; } // simplified
}

// ---------- OCP + DIP: payments behind an abstraction ----------
interface PaymentMethod {
    void pay(double amount);
}
class CreditCardPayment implements PaymentMethod {
    private final String cardNumber;
    public CreditCardPayment(String cardNumber) { this.cardNumber = cardNumber; }
    public void pay(double amount) { System.out.println("Charged Rs." + amount + " to card"); }
}
class UpiPayment implements PaymentMethod {
    private final String upiId;
    public UpiPayment(String upiId) { this.upiId = upiId; }
    public void pay(double amount) { System.out.println("Charged Rs." + amount + " via UPI"); }
}

// ---------- SRP: notifications ----------
class OrderNotificationService {
    public void sendConfirmation(Order order) { /* ... */ }
}

// ---------- The orchestrator: depends on ABSTRACTIONS, coordinates only ----------
class CheckoutService {
    private final PaymentMethod paymentMethod;     // DIP: abstraction, injected
    private final DiscountStrategy discount;        // OCP: strategy, injected
    private final OrderNotificationService notifier;
    private final InvoiceService invoiceService;

    public CheckoutService(PaymentMethod paymentMethod, DiscountStrategy discount,
                           OrderNotificationService notifier, InvoiceService invoiceService) {
        this.paymentMethod = paymentMethod;
        this.discount = discount;
        this.notifier = notifier;
        this.invoiceService = invoiceService;
    }

    public void checkout(Cart cart, Order order, double total) {
        double finalAmount = discount.apply(total);
        paymentMethod.pay(finalAmount);
        invoiceService.generate(order);
        notifier.sendConfirmation(order);
    }
}
```

> **Result:** add a wallet payment → new `WalletPayment` class, zero edits. Add a festival discount → new `DiscountStrategy` class, zero edits. Test checkout → inject a `FakePayment`. Each class now changes for exactly one reason.

---

# PART B — INTERVIEW QUESTIONS & MODEL ANSWERS

> Speak these out loud. The structure that scores: **define → analogy → why → tiny example.**

---

## 🔹 OOP Questions

### Q1. What is OOP and why does it exist?
**Answer:** OOP organizes code into **objects** — self-contained units that bundle **data** (fields) and **behavior** (methods). It exists to solve the problems of procedural code: when everything is one long script, teams can't work in parallel, a change in one place breaks unrelated logic, and there's tons of copy-paste. OOP gives us 4 tools — **Encapsulation, Inheritance, Polymorphism, Abstraction** — to keep large codebases safe, reusable, and easy to extend.

### Q2. Explain the 4 pillars of OOP. *(The single most-asked LLD question.)*
> Use the one-line + analogy form:

| Pillar | One line | Analogy |
|--------|----------|---------|
| **Encapsulation** | Private data, controlled public access | ATM gives buttons, hides the vault |
| **Inheritance** | Children reuse the parent's code | All vehicles share start/stop |
| **Polymorphism** | Same method name, different behavior | "Open" a door vs a book |
| **Abstraction** | Show WHAT, hide HOW | Drive a car without knowing the engine |

### Q3. Difference between Abstraction and Encapsulation? *(Classic trap)*
**Answer:** They sound similar but solve different problems.
- **Encapsulation** = hiding **data**. *How* you protect state — make fields `private`, expose `getBalance()`. It's about **safety/integrity**.
- **Abstraction** = hiding **complexity**. *What* you expose — a `PaymentProcessor.process()` that the caller uses without knowing UPI vs Card internals. It's about **simplicity**.

> One-liner: *"Encapsulation hides the data; abstraction hides the implementation. Encapsulation is achieved with access modifiers; abstraction with abstract classes/interfaces."*

### Q4. Overloading vs Overriding? *(Two types of polymorphism)*
- **Overloading (compile-time):** same method name, **different parameters** in the same class. `add(int, int)` vs `add(double, double)`. Resolved at compile time.
- **Overriding (run-time):** a **child** redefines a parent's method with the **same signature**. `Penguin` overrides `makeSound()`. Resolved at runtime based on the actual object.

```java
class Calc { int add(int a, int b){return a+b;} double add(double a, double b){return a+b;} } // overloading
class Sparrow extends Bird { @Override void makeSound(){ System.out.println("Chirp"); } }        // overriding
```

### Q5. Abstract class vs Interface? When to use which? *(Very frequent)*
| | Abstract Class | Interface |
|---|---------------|-----------|
| Methods | Can have both concrete + abstract methods | Methods are abstract (Java 8+: can have `default`/`static`) |
| Fields | Can have instance state (fields) | Only constants (`public static final`) |
| Inheritance | A class extends **one** abstract class | A class implements **many** interfaces |
| Use when | Classes share **common code/state** (`Vehicle` base) | You want a **pure contract / capability** (`Comparable`, `Notifier`) |

> Rule of thumb: *"**Is-a** with shared code → abstract class. **Can-do** capability, or you need multiple inheritance of type → interface."* In the notification exercise I used an interface because notifiers share no state, only a contract.

### Q6. What is composition, and "composition over inheritance"?
**Answer:** Composition = building a class by **holding references** to other objects (HAS-A), instead of extending them (IS-A). Inheritance is rigid — a deep hierarchy is hard to change, and the child is tightly coupled to the parent. Composition is flexible — you swap parts at runtime. A `Car` *has-a* `Engine` (composition) is better than `Car extends Engine` (nonsense). Most real designs prefer composition; that's exactly what Dependency Injection does.

---

## 🔹 SOLID Questions

### Q7. What does SOLID stand for? *(Know this cold)*
| Letter | Principle | One-line |
|--------|-----------|----------|
| **S** | Single Responsibility | One class, one reason to change |
| **O** | Open/Closed | Open for extension, closed for modification |
| **L** | Liskov Substitution | A child must be usable wherever the parent is |
| **I** | Interface Segregation | Many small interfaces > one fat interface |
| **D** | Dependency Inversion | Depend on abstractions, not concrete classes |

### Q8. Explain SRP with an example.
**Answer:** Every class should have ONE reason to change. Restaurant analogy: the chef cooks, the waiter serves, the cashier bills — not one person doing all. In code, an `OrderManager` that creates orders, charges payment, sends SMS, **and** assigns delivery has 4 reasons to change. If the SMS provider changes, I shouldn't be touching payment code. Fix: split into `OrderService`, `PaymentService`, `NotificationService`, `DeliveryService`.

### Q9. Explain OCP. How do you add a feature without modifying code?
**Answer:** Open for extension, closed for modification — add new behavior via **new classes**, not by editing tested code. Analogy: an electrical socket — you plug in new appliances without rewiring the wall. The tool is **polymorphism + an interface**. Instead of a `if discountType == "gold"...` chain, I define a `DiscountStrategy` interface and add `GoldDiscount`, `StudentDiscount` as new classes. The checkout code never changes.

### Q10. Explain LSP with a violation example.
**Answer:** A subclass must honor everything the parent promises — you should be able to substitute the child anywhere the parent is used, with no surprises. Classic violation: `Penguin extends Bird` where `Bird.fly()` exists, so `penguin.fly()` throws an exception — any loop over `List<Bird>` calling `fly()` crashes. Fix: split the hierarchy — `Bird` (eat, makeSound), `FlyingBird extends Bird` (fly), `SwimmingBird extends Bird` (swim). Penguin extends `SwimmingBird`, never promises flight. *Rectangle/Square is the other textbook example.*

### Q11. Explain ISP.
**Answer:** Don't force a class to implement methods it doesn't need. One fat `SmartDevice` interface with `browseInternet()`, `takePhoto()` forces a basic Nokia phone to write empty stub methods — dishonest and fragile. Split into small interfaces — `Callable`, `Messageable`, `CameraEnabled` — and each device implements only what it can actually do. Analogy: separate government form sections for drivers vs doctors, not one mega-form everyone fills with "N/A".

### Q12. Explain DIP. How is it different from Dependency Injection?
**Answer:**
- **DIP (the principle):** High-level business logic should depend on an **abstraction**, not a concrete low-level detail. `UserService` should depend on a `Database` interface, not a hardcoded `MySQLDatabase`. Analogy: a laptop depends on the USB-C **standard**, not one specific charger.
- **Dependency Injection (the technique):** *How* you achieve DIP — pass the dependency **in** (via constructor) instead of creating it inside with `new`. `new UserService(new MongoDatabase())`.

> One-liner: *"DIP is the goal (depend on abstractions); DI is the mechanism (inject them in)."* Benefit: swap MySQL → Mongo in one line, and inject a `FakeDatabase` for fast tests.

### Q13. Why do SOLID principles matter in a real company?
**Answer:** In real projects 5–50 engineers share a codebase that lives for years, with requirements changing weekly. Without SOLID you get "spaghetti code" — one change breaks three unrelated things, no one can add a feature safely, and testing is impossible. SOLID keeps code **changeable** (SRP), **extensible** (OCP), **substitutable** (LSP), **focused** (ISP), and **testable/swappable** (DIP). It's the difference between a 2-day feature and a 2-week one.

### Q14. Can you over-apply SOLID? *(Senior-level follow-up)*
**Answer:** Yes. Splitting every tiny thing into its own class/interface creates needless indirection — 10 files to follow one simple flow. SOLID is a guide, not religion. Apply it where code **changes often** or has **multiple reasons to change**. For a stable 20-line utility, a single class is fine. The goal is maintainability, not maximum abstraction.

---

## 🧠 The Memory Hooks — Remember SOLID Forever

| Letter | Hook | Trigger to spot a violation |
|--------|------|-----------------------------|
| **S** | 🍳 *One chef, one job* | A class name has "And" in it, or it touches DB **and** email **and** PDF |
| **O** | 🔌 *Socket: plug in, don't rewire* | You see a growing `if/else` or `switch` on a type string |
| **L** | 🐧 *Penguin can't fly* | A child throws `UnsupportedOperationException` or returns a dummy value |
| **I** | 📋 *Separate forms, not one mega-form* | A class implements methods with empty `{}` bodies |
| **D** | 🔋 *USB-C standard, not one charger* | A class does `new ConcreteThing()` inside its own constructor for a dependency |

> 💬 **Final interview tip:** When given *"Design a Parking Lot / Elevator / Vending Machine"*, start by listing **objects** (nouns), their **relationships**, and their **actions** (verbs) — then apply SOLID: one class per responsibility (S), interfaces for the things that vary like pricing/payment (O, D), and small role-based interfaces (I). Narrate which principle you're using as you go — that's what gets you hired.
