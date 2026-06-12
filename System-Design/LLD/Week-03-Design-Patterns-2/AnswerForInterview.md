# LLD Week 3: Behavioral Design Patterns — Interview Answers & Exercise Solutions

> Companion answer key for `Plan.md`. Every exercise is fully solved in **Java**, and every pattern has interview-ready model answers. All Java here is written to be correct and compilable (Java 11+; a few snippets use `Map.of` / streams, available since Java 9/8).

---

## 🌟 The LLD Interview Golden Rule

When an interviewer asks you about *any* design pattern, never just recite the definition. Answer in this **5-part shape** — it instantly signals senior-level understanding:

1. **Definition** — one crisp sentence ("what is it?").
2. **Real-life analogy** — a vivid everyday example (Ola, Zomato, Google Docs). Analogies prove you *understand*, not just *memorized*.
3. **Problem it solves** — show the *ugly code without the pattern* (the if-else monster).
4. **Java code (bad vs good)** — refactor the mess into the pattern.
5. **When to use / when NOT to use** — the senior-engineer signal. Junior devs apply patterns everywhere; seniors know when *not* to.

> 🎯 **Memory aid for the four patterns this week:**
> **S.O.C.S.** — **S**trategy (swap the algorithm), **O**bserver (notify the watchers), **C**ommand (wrap + undo the action), **S**tate (behavior changes with state).

All four are **Behavioral** patterns — they are about how objects **communicate** and **divide responsibility**, not how they're created (Creational) or composed (Structural).

---

# PART A — Exercise / Practice Solutions

The Plan.md ends with **4 practice exercises** plus a self-check. Here is a complete, compilable Java solution for each.

---

## ✅ Exercise 1 — Flipkart Discount Strategy System

**Goal:** A pricing system using the **Strategy** pattern with a `ShoppingCart` context that can swap discount strategies at runtime.

> ⚠️ **Watch the test math.** The Plan's expected output `Rs.55000` for `CappedPercentageDiscount(50, 2000)` on a `Rs.57000` cart works because the discount (50% = 28,500) is **capped at 2,000**, so final = 57,000 − 2,000 = **55,000**. The cap is the *maximum rupees off*, not a percentage cap.

```java
import java.util.*;

// ---- The Strategy contract ----
interface DiscountStrategy {
    /** Given the cart's original total, return the discounted total. */
    double calculate(double originalPrice, Map<String, Object> context);
}

// ---- Concrete strategies (each = one small, testable class) ----

class NoDiscount implements DiscountStrategy {
    @Override
    public double calculate(double originalPrice, Map<String, Object> context) {
        return originalPrice;
    }
}

class PercentageDiscount implements DiscountStrategy {
    private final double percentage;
    public PercentageDiscount(double percentage) { this.percentage = percentage; }

    @Override
    public double calculate(double originalPrice, Map<String, Object> context) {
        return originalPrice * (1 - percentage / 100.0);
    }
}

class FlatDiscount implements DiscountStrategy {
    private final double amount;
    public FlatDiscount(double amount) { this.amount = amount; }

    @Override
    public double calculate(double originalPrice, Map<String, Object> context) {
        return Math.max(0, originalPrice - amount); // never below zero
    }
}

class BuyOneGetOneFree implements DiscountStrategy {
    /** Cheapest item in the cart becomes free. Reads "cartItems" from context. */
    @Override
    @SuppressWarnings("unchecked")
    public double calculate(double originalPrice, Map<String, Object> context) {
        List<Map<String, Object>> items =
            (List<Map<String, Object>>) context.getOrDefault("cartItems", new ArrayList<>());
        if (items != null && items.size() >= 2) {
            double cheapest = items.stream()
                .mapToDouble(i -> ((Number) i.get("price")).doubleValue())
                .min().orElse(0);
            return originalPrice - cheapest;
        }
        return originalPrice;
    }
}

class CappedPercentageDiscount implements DiscountStrategy {
    /** e.g. 50% off, but never more than maxDiscount rupees off (Swiggy first order). */
    private final double percentage;
    private final double maxDiscount;
    public CappedPercentageDiscount(double percentage, double maxDiscount) {
        this.percentage = percentage;
        this.maxDiscount = maxDiscount;
    }

    @Override
    public double calculate(double originalPrice, Map<String, Object> context) {
        double rawDiscount = originalPrice * (percentage / 100.0);
        double appliedDiscount = Math.min(rawDiscount, maxDiscount); // cap it
        return originalPrice - appliedDiscount;
    }
}

// ---- The Context: ShoppingCart uses a strategy but doesn't know which one ----
class ShoppingCart {
    private final List<Map<String, Object>> items = new ArrayList<>();
    private DiscountStrategy discount = new NoDiscount(); // sensible default

    public void addItem(String name, double price) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("price", price);
        items.add(item);
    }

    public void setDiscount(DiscountStrategy strategy) { // swap at RUNTIME
        this.discount = strategy;
    }

    private double subtotal() {
        return items.stream()
            .mapToDouble(i -> ((Number) i.get("price")).doubleValue())
            .sum();
    }

    public String checkout() {
        double original = subtotal();
        Map<String, Object> context = new HashMap<>();
        context.put("cartItems", items); // pass items so BOGO can inspect them
        double finalPrice = discount.calculate(original, context);
        finalPrice = Math.round(finalPrice * 100.0) / 100.0;
        return "Subtotal: Rs." + original + " | Final: Rs." + finalPrice;
    }
}

// ---- Demo ----
public class Exercise1Demo {
    public static void main(String[] args) {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem("Laptop", 55000);
        cart.addItem("Mouse", 500);
        cart.addItem("Keyboard", 1500);
        // subtotal = 57000

        cart.setDiscount(new NoDiscount());
        System.out.println(cart.checkout());                 // Final: Rs.57000.0

        cart.setDiscount(new PercentageDiscount(10));
        System.out.println(cart.checkout());                 // Final: Rs.51300.0

        cart.setDiscount(new CappedPercentageDiscount(50, 2000));
        System.out.println(cart.checkout());                 // Final: Rs.55000.0

        cart.setDiscount(new BuyOneGetOneFree());
        System.out.println(cart.checkout());                 // cheapest (Mouse 500) free -> 56500.0
    }
}
```

**Why this is a strong interview answer:** Adding a "Republic Day Sale" tomorrow = write *one new class*, touch *nothing* existing (Open/Closed Principle). The `ShoppingCart` never grows.

---

## ✅ Exercise 2 — Sensex Stock Market Observer

**Goal:** A `StockExchange` (subject) notifies multiple observers when a stock price changes. Different observers can subscribe to different stocks.

> 🔑 **Key design choice:** subscriptions are keyed **per-stock** (`Map<String, List<StockObserver>>`), so `PortfolioCalculator` only wakes up when a stock it *holds* moves — not for every stock in the market.

```java
import java.util.*;

// ---- Observer contract ----
interface StockObserver {
    void onPriceChange(String stock, double oldPrice, double newPrice);
}

// ---- Subject / Publisher ----
class StockExchange {
    private final Map<String, Double> prices = new HashMap<>();
    private final Map<String, List<StockObserver>> subscribers = new HashMap<>();

    /** Set an initial price WITHOUT notifying (seeding the market). */
    public void setPrice(String stock, double price) {
        prices.put(stock, price);
    }

    public void subscribe(String stock, StockObserver observer) {
        subscribers.computeIfAbsent(stock, k -> new ArrayList<>());
        if (!subscribers.get(stock).contains(observer)) { // avoid duplicates
            subscribers.get(stock).add(observer);
        }
    }

    public void unsubscribe(String stock, StockObserver observer) {
        List<StockObserver> list = subscribers.get(stock);
        if (list != null) list.remove(observer);
    }

    /** Update a price and notify everyone watching THIS stock. */
    public void updatePrice(String stock, double newPrice) {
        double oldPrice = prices.getOrDefault(stock, newPrice);
        prices.put(stock, newPrice);
        for (StockObserver o : subscribers.getOrDefault(stock, Collections.emptyList())) {
            try {
                o.onPriceChange(stock, oldPrice, newPrice);
            } catch (Exception e) { // one bad observer must not break the others
                System.out.println("  [Exchange] Observer error: " + e.getMessage());
            }
        }
    }

    public double getPrice(String stock) {
        return prices.getOrDefault(stock, 0.0);
    }
}

// ---- Concrete observers ----

class MobileAppAlert implements StockObserver {
    @Override
    public void onPriceChange(String stock, double oldPrice, double newPrice) {
        System.out.println("  [MOBILE] " + stock + " price changed to Rs." + newPrice);
    }
}

class WebDashboard implements StockObserver {
    @Override
    public void onPriceChange(String stock, double oldPrice, double newPrice) {
        double pct = oldPrice == 0 ? 0 : ((newPrice - oldPrice) / oldPrice) * 100;
        pct = Math.round(pct * 100.0) / 100.0;
        String arrow = newPrice >= oldPrice ? "▲" : "▼";
        System.out.println("  [WEB] " + stock + " Rs." + newPrice + " (" + arrow + pct + "%)");
    }
}

class EmailDigest implements StockObserver {
    private final List<String> buffer = new ArrayList<>();
    @Override
    public void onPriceChange(String stock, double oldPrice, double newPrice) {
        buffer.add(stock + ": Rs." + oldPrice + " -> Rs." + newPrice);
    }
    public void sendDigest() {
        System.out.println("  [EMAIL DIGEST] Today's changes:");
        buffer.forEach(line -> System.out.println("      " + line));
    }
}

class PortfolioCalculator implements StockObserver {
    private final Map<String, Integer> holdings; // stock -> quantity owned
    private final StockExchange exchange;
    public PortfolioCalculator(Map<String, Integer> holdings, StockExchange exchange) {
        this.holdings = new HashMap<>(holdings);
        this.exchange = exchange;
    }
    @Override
    public void onPriceChange(String stock, double oldPrice, double newPrice) {
        if (!holdings.containsKey(stock)) return; // ignore stocks we don't own
        double total = 0;
        for (Map.Entry<String, Integer> e : holdings.entrySet()) {
            total += exchange.getPrice(e.getKey()) * e.getValue();
        }
        System.out.println("  [PORTFOLIO] Portfolio value = Rs." + total);
    }
}

// ---- Demo ----
public class Exercise2Demo {
    public static void main(String[] args) {
        StockExchange exchange = new StockExchange();
        exchange.setPrice("TCS", 3500);
        exchange.setPrice("INFY", 1800);
        exchange.setPrice("RELIANCE", 2500);

        MobileAppAlert mobile = new MobileAppAlert();
        // PortfolioCalculator needs the exchange to read current prices of ALL its holdings
        PortfolioCalculator portfolio =
            new PortfolioCalculator(Map.of("TCS", 10, "INFY", 5), exchange);

        exchange.subscribe("TCS", mobile);
        exchange.subscribe("TCS", portfolio);
        exchange.subscribe("INFY", portfolio);

        exchange.updatePrice("TCS", 3600);
        // [MOBILE] TCS price changed to Rs.3600.0
        // [PORTFOLIO] Portfolio value = Rs.45000.0  (10*3600 + 5*1800)

        exchange.updatePrice("INFY", 1750);
        // [PORTFOLIO] Portfolio value = Rs.44750.0  (10*3600 + 5*1750)
    }
}
```

> 💡 **Interview-grade detail:** `PortfolioCalculator` holds a reference to the `exchange` so it can recompute the *whole portfolio* from current prices. A naive solution that only knows the *one changed stock* can't compute total value correctly. Mentioning this shows you thought about the data the observer actually needs.

---

## ✅ Exercise 3 — Text Editor with Undo/Redo (Bold + Batch + History)

**Goal:** Extend the Command-pattern editor with: a **Bold** command, a **batch/macro** command (multiple ops = one undo), and `showHistory()` / `showRedoStack()`.

> 🧩 **The star here is the `BatchCommand` (a.k.a. Macro Command):** it *is* a `Command` but it *holds* a list of commands. Its `execute()` runs them forward; its `undo()` runs them in **reverse order**. That reverse order matters — undoing must unwind the stack like LIFO.

```java
import java.util.*;

// ---- Command contract ----
interface Command {
    void execute();
    void undo();
    String description();
}

// ---- The document being edited ----
class TextEditor {
    private String content = "";
    private final Deque<Command> history = new ArrayDeque<>();  // executed commands
    private final Deque<Command> redoStack = new ArrayDeque<>(); // undone commands

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public void executeCommand(Command cmd) {
        cmd.execute();
        history.push(cmd);
        redoStack.clear(); // new action invalidates the redo timeline
    }

    public void undo() {
        if (history.isEmpty()) { System.out.println("Nothing to undo!"); return; }
        Command cmd = history.pop();
        cmd.undo();
        redoStack.push(cmd);
        System.out.println("Undid: " + cmd.description());
    }

    public void redo() {
        if (redoStack.isEmpty()) { System.out.println("Nothing to redo!"); return; }
        Command cmd = redoStack.pop();
        cmd.execute();
        history.push(cmd);
        System.out.println("Redid: " + cmd.description());
    }

    public void showHistory() {
        System.out.println("--- Undo History (top = most recent) ---");
        if (history.isEmpty()) System.out.println("  (empty)");
        int i = 1;
        for (Command c : history) System.out.println("  " + (i++) + ". " + c.description());
    }

    public void showRedoStack() {
        System.out.println("--- Redo Stack (top = next redo) ---");
        if (redoStack.isEmpty()) System.out.println("  (empty)");
        int i = 1;
        for (Command c : redoStack) System.out.println("  " + (i++) + ". " + c.description());
    }

    // ---- Convenience entry points ----
    public void typeText(String text) {
        executeCommand(new InsertTextCommand(this, text, content.length()));
    }

    /** Replace-all as ONE undo unit: built from individual replace steps inside a batch. */
    public void batchReplace(String oldText, String newText) {
        List<Command> steps = new ArrayList<>();
        int idx = content.indexOf(oldText);
        // Build one DeleteText + InsertText per occurrence, left to right.
        // We simulate the running content so positions stay correct.
        String running = content;
        int searchFrom = 0;
        while (true) {
            int pos = running.indexOf(oldText, searchFrom);
            if (pos < 0) break;
            steps.add(new DeleteTextCommand(this, pos, oldText.length()));
            steps.add(new InsertTextCommand(this, newText, pos));
            running = running.substring(0, pos) + newText + running.substring(pos + oldText.length());
            searchFrom = pos + newText.length();
        }
        if (steps.isEmpty()) { System.out.println("No occurrences of '" + oldText + "'"); return; }
        executeCommand(new BatchCommand(
            "Replace all '" + oldText + "' with '" + newText + "'", steps));
    }
}

// ---- Concrete commands ----
class InsertTextCommand implements Command {
    private final TextEditor editor; private final String text; private final int position;
    public InsertTextCommand(TextEditor editor, String text, int position) {
        this.editor = editor; this.text = text; this.position = position;
    }
    @Override public void execute() {
        String c = editor.getContent();
        editor.setContent(c.substring(0, position) + text + c.substring(position));
    }
    @Override public void undo() {
        String c = editor.getContent();
        editor.setContent(c.substring(0, position) + c.substring(position + text.length()));
    }
    @Override public String description() {
        String p = text.length() > 20 ? text.substring(0, 20) + "..." : text;
        return "Insert '" + p + "' at " + position;
    }
}

class DeleteTextCommand implements Command {
    private final TextEditor editor; private final int position; private final int length;
    private String deleted = "";
    public DeleteTextCommand(TextEditor editor, int position, int length) {
        this.editor = editor; this.position = position; this.length = length;
    }
    @Override public void execute() {
        String c = editor.getContent();
        deleted = c.substring(position, position + length); // remember for undo
        editor.setContent(c.substring(0, position) + c.substring(position + length));
    }
    @Override public void undo() {
        String c = editor.getContent();
        editor.setContent(c.substring(0, position) + deleted + c.substring(position));
    }
    @Override public String description() {
        return "Delete " + length + " chars at " + position;
    }
}

class BoldTextCommand implements Command {
    /** Wraps a [start, end) range in ** markers (Markdown bold). */
    private final TextEditor editor; private final int start; private final int end;
    public BoldTextCommand(TextEditor editor, int start, int end) {
        this.editor = editor; this.start = start; this.end = end;
    }
    @Override public void execute() {
        String c = editor.getContent();
        String bolded = "**" + c.substring(start, end) + "**";
        editor.setContent(c.substring(0, start) + bolded + c.substring(end));
    }
    @Override public void undo() {
        // Remove the 2 markers we added before `start` and 2 after the original `end`.
        String c = editor.getContent();
        // After execute, content grew by 4. Strip "**" at start and "**" before (end+2).
        String inner = c.substring(start + 2, end + 2);
        editor.setContent(c.substring(0, start) + inner + c.substring(end + 4));
    }
    @Override public String description() {
        return "Bold range [" + start + ", " + end + ")";
    }
}

class BatchCommand implements Command {
    /** A macro: many commands treated as ONE undo unit. */
    private final String label;
    private final List<Command> commands;
    public BatchCommand(String label, List<Command> commands) {
        this.label = label; this.commands = commands;
    }
    @Override public void execute() {
        for (Command c : commands) c.execute();          // forward order
    }
    @Override public void undo() {
        ListIterator<Command> it = commands.listIterator(commands.size());
        while (it.hasPrevious()) it.previous().undo();    // REVERSE order (LIFO)
    }
    @Override public String description() {
        return label + " (" + commands.size() + " steps)";
    }
}

// ---- Demo ----
public class Exercise3Demo {
    public static void main(String[] args) {
        TextEditor editor = new TextEditor();
        editor.typeText("Hello World Hello India Hello Python");
        editor.showHistory();                 // 1 command

        editor.batchReplace("Hello", "Namaste");
        System.out.println(editor.getContent());  // "Namaste World Namaste India Namaste Python"
        editor.showHistory();                 // 2 commands (the batch counts as ONE)

        editor.undo();                        // one undo reverts ALL three replacements
        System.out.println(editor.getContent());  // "Hello World Hello India Hello Python"

        editor.showRedoStack();               // the batch is now redoable
        editor.redo();
        System.out.println(editor.getContent());  // back to "Namaste ..."
    }
}
```

> 🧠 **Why batch undo is reverse order:** If a macro does A → B → C, the editor's state after each is layered. To unwind cleanly you must undo C, then B, then A — exactly like a stack. Doing them forward (A.undo, B.undo, C.undo) would corrupt positions.

---

## ✅ Exercise 4 (Bonus) — Ola Ride State Machine

**Goal:** The full Ola ride lifecycle with the **State** pattern. States: `SEARCHING, DRIVER_ASSIGNED, WAITING, IN_RIDE, COMPLETED, CANCELLED`. The same action (`cancel()`) behaves differently — even charges different fees — per state.

```java
// ---- State contract: every state must answer EVERY action ----
interface RideState {
    void cancel(Ride ride);
    void driverFound(Ride ride);
    void driverArrived(Ride ride);
    void startRide(Ride ride);
    void endRide(Ride ride);
    void rate(Ride ride, int stars);
    String name();
}

// ---- The context ----
class Ride {
    private RideState state = new SearchingState();
    private double cancellationFee = 0;
    private int rating = 0;

    public void setState(RideState newState) {
        System.out.println("  [State: " + state.name() + " -> " + newState.name() + "]");
        this.state = newState;
    }
    public void setCancellationFee(double fee) { this.cancellationFee = fee; }
    public double getCancellationFee() { return cancellationFee; }
    public void setRating(int stars) { this.rating = stars; }
    public int getRating() { return rating; }

    // Delegate every action to the current state object
    public void cancel()        { state.cancel(this); }
    public void driverFound()   { state.driverFound(this); }
    public void driverArrived() { state.driverArrived(this); }
    public void startRide()     { state.startRide(this); }
    public void endRide()       { state.endRide(this); }
    public void rate(int stars) { state.rate(this, stars); }
    public String currentState(){ return state.name(); }
}

// ---- States ----
class SearchingState implements RideState {
    @Override public void cancel(Ride r) {
        System.out.println("Ride cancelled. No charge."); r.setState(new CancelledState());
    }
    @Override public void driverFound(Ride r) {
        System.out.println("Driver assigned!"); r.setState(new DriverAssignedState());
    }
    @Override public void driverArrived(Ride r){ System.out.println("No driver yet."); }
    @Override public void startRide(Ride r)    { System.out.println("No driver assigned yet."); }
    @Override public void endRide(Ride r)      { System.out.println("Ride hasn't started."); }
    @Override public void rate(Ride r, int s)  { System.out.println("Can't rate — no ride yet."); }
    @Override public String name() { return "SEARCHING"; }
}

class DriverAssignedState implements RideState {
    @Override public void cancel(Ride r) {
        System.out.println("Ride cancelled. Rs.50 cancellation fee.");
        r.setCancellationFee(50); r.setState(new CancelledState());
    }
    @Override public void driverFound(Ride r)  { System.out.println("Driver already assigned."); }
    @Override public void driverArrived(Ride r){
        System.out.println("Driver has arrived at pickup."); r.setState(new WaitingState());
    }
    @Override public void startRide(Ride r)    { System.out.println("Driver hasn't arrived yet."); }
    @Override public void endRide(Ride r)      { System.out.println("Ride hasn't started."); }
    @Override public void rate(Ride r, int s)  { System.out.println("Can't rate — ride not done."); }
    @Override public String name() { return "DRIVER_ASSIGNED"; }
}

class WaitingState implements RideState {
    @Override public void cancel(Ride r) {
        System.out.println("Ride cancelled. Rs.100 cancellation fee.");
        r.setCancellationFee(100); r.setState(new CancelledState());
    }
    @Override public void driverFound(Ride r)  { System.out.println("Driver already here."); }
    @Override public void driverArrived(Ride r){ System.out.println("Driver already waiting."); }
    @Override public void startRide(Ride r)    {
        System.out.println("Ride started. Enjoy!"); r.setState(new InRideState());
    }
    @Override public void endRide(Ride r)      { System.out.println("Ride hasn't started."); }
    @Override public void rate(Ride r, int s)  { System.out.println("Can't rate — ride not done."); }
    @Override public String name() { return "WAITING"; }
}

class InRideState implements RideState {
    @Override public void cancel(Ride r)       { System.out.println("NOT ALLOWED — you're in the car!"); }
    @Override public void driverFound(Ride r)  { System.out.println("Already riding."); }
    @Override public void driverArrived(Ride r){ System.out.println("Already riding."); }
    @Override public void startRide(Ride r)    { System.out.println("Already riding."); }
    @Override public void endRide(Ride r)      {
        System.out.println("Destination reached. Ride completed."); r.setState(new CompletedState());
    }
    @Override public void rate(Ride r, int s)  { System.out.println("Finish the ride first."); }
    @Override public String name() { return "IN_RIDE"; }
}

class CompletedState implements RideState {
    @Override public void cancel(Ride r)       { System.out.println("NOT ALLOWED — ride is over!"); }
    @Override public void driverFound(Ride r)  { System.out.println("Ride already completed."); }
    @Override public void driverArrived(Ride r){ System.out.println("Ride already completed."); }
    @Override public void startRide(Ride r)    { System.out.println("Ride already completed."); }
    @Override public void endRide(Ride r)      { System.out.println("Ride already completed."); }
    @Override public void rate(Ride r, int stars) {
        r.setRating(stars); System.out.println("Thanks! You rated " + stars + " stars.");
    }
    @Override public String name() { return "COMPLETED"; }
}

class CancelledState implements RideState {
    @Override public void cancel(Ride r)       { System.out.println("Already cancelled."); }
    @Override public void driverFound(Ride r)  { System.out.println("Ride was cancelled."); }
    @Override public void driverArrived(Ride r){ System.out.println("Ride was cancelled."); }
    @Override public void startRide(Ride r)    { System.out.println("Ride was cancelled."); }
    @Override public void endRide(Ride r)      { System.out.println("Ride was cancelled."); }
    @Override public void rate(Ride r, int s)  { System.out.println("Can't rate a cancelled ride."); }
    @Override public String name() { return "CANCELLED"; }
}

// ---- Demo: a full happy-path lifecycle ----
public class Exercise4Demo {
    public static void main(String[] args) {
        Ride ride = new Ride();
        System.out.println("Start: " + ride.currentState());   // SEARCHING

        ride.cancel();         // tip: free cancel during search... but let's continue the happy path instead
        // (above would move to CANCELLED; for the demo, start fresh)

        Ride r2 = new Ride();
        r2.driverFound();      // SEARCHING -> DRIVER_ASSIGNED
        r2.driverArrived();    // DRIVER_ASSIGNED -> WAITING
        r2.startRide();        // WAITING -> IN_RIDE
        r2.cancel();           // NOT ALLOWED — you're in the car!
        r2.endRide();          // IN_RIDE -> COMPLETED
        r2.rate(5);            // Thanks! You rated 5 stars.
        r2.cancel();           // NOT ALLOWED — ride is over!
    }
}
```

> 🔒 **Why the interface is the safety net:** because `RideState` declares *all six actions*, when you add a new state (say `EMERGENCY`), the compiler **forces** you to decide what every action does in that state. With if-else, you'd silently forget a case. That compiler-enforced completeness is the State pattern's superpower — say this in the interview.

---

## ✅ Self-Check Answers (the Plan's checklist)

| Self-check item | Quick answer |
|---|---|
| Explain all 4 patterns in plain English | Strategy = swap algorithm; Observer = notify watchers; Command = wrap action (+undo); State = behavior changes with state |
| Indian analogy for each | Strategy = Ola fare/Flipkart discount; Observer = Zomato order tracking; Command = Google Docs undo / PayTM reversal; State = Ola ride lifecycle |
| Write each from memory | See Part A solutions — practice the 4 `interface` shells first |
| Trigger question for each | "Choosing between ways to do the same thing?" → Strategy. "One change, many reactions?" → Observer. "Need undo/queue/log?" → Command. "Behavior depends on state w/ transitions?" → State |
| When NOT to use | See "pitfalls" in each Part B section below |
| Identify pattern from a problem | Use the 🧠 Memory Hooks table at the bottom |
| All 10 patterns (Wk2+Wk3) | Singleton, Factory, Builder / Adapter, Decorator, Facade / Strategy, Observer, Command, State |

---

# PART B — Interview Questions & Model Answers

For each pattern: **problem it solves → analogy → Java → when to use / pitfalls**. Then cross-cutting Q&A with comparison tables.

---

## 🎯 Pattern 1: Strategy

**Q: "What is the Strategy pattern and when would you use it?"**

**Model answer:**

> The Strategy pattern defines a **family of interchangeable algorithms**, encapsulates each in its own class, and lets you swap them at **runtime**. The client code calls one method and doesn't know or care which concrete algorithm runs.

**Analogy:** Ola fare calculation. Normal pricing, surge pricing (1.5–2.5×), subscription flat-rate, shared-ride split — the booking system just picks a *pricing strategy* for current conditions. Booking, driver-matching, ETA code never change.

**Problem it solves:** Kills the growing `if-elif-else` block that selects between algorithms. Each new option = one new class, not an edit to a 200-line method (Open/Closed Principle).

**Minimal Java:**

```java
interface PaymentStrategy { void pay(double amount); }

class UpiPayment implements PaymentStrategy {
    public void pay(double amount) { System.out.println("Paid Rs." + amount + " via UPI"); }
}
class CardPayment implements PaymentStrategy {
    public void pay(double amount) { System.out.println("Paid Rs." + amount + " via Card"); }
}

class Checkout {
    private PaymentStrategy strategy;
    public void setStrategy(PaymentStrategy s) { this.strategy = s; } // swap at runtime
    public void pay(double amount) { strategy.pay(amount); }
}
```

**When to use:** Multiple ways to do the same thing (pricing, sorting, compression, routing, validation); algorithm chosen by context (time, user type, A/B group); you see a growing switch over algorithms.

**Pitfalls / when NOT to use:**
- Only 2 simple options that'll never grow → a plain `if-else` is fine; 3 classes for a coin-flip is over-engineering.
- The client must know the strategies exist to pick one (unless you pair it with a Factory).
- Strategies that need lots of the context's private state feel forced.

---

## 🎯 Pattern 2: Observer

**Q: "Explain the Observer pattern with a real use case."**

**Model answer:**

> Observer defines a **one-to-many** dependency: when the **subject** changes state, all registered **observers** are notified automatically. The subject doesn't know who the observers are or what they do — it just publishes "something happened."

**Analogy:** Zomato order tracking. When status flips to *Out for Delivery*, your push notification, the live map, the restaurant dashboard, the rider app, analytics, and the ETA estimator all react — independently. The order service just publishes the event.

**Problem it solves:** Removes tight coupling where one service holds hardcoded references to every dependent service. Add a loyalty-points feature later? Just *subscribe* it — zero changes to the order service.

**Minimal Java:**

```java
import java.util.*;

interface Observer { void update(String event); }

class Subject {
    private final List<Observer> observers = new ArrayList<>();
    public void subscribe(Observer o)   { observers.add(o); }
    public void unsubscribe(Observer o) { observers.remove(o); }
    public void notifyAll(String event) {
        for (Observer o : new ArrayList<>(observers)) { // copy = safe if someone unsubscribes mid-notify
            try { o.update(event); }
            catch (Exception e) { System.out.println("Observer failed: " + e.getMessage()); }
        }
    }
}
```

**When to use:** Event-driven systems, UI state binding (React, Android LiveData), real-time alerts, pub/sub — anywhere "one change → many reactions."

**Pitfalls / when NOT to use:**
- **No guaranteed order** of notification — don't rely on it.
- **Memory leaks:** observers that subscribe but never unsubscribe (classic in long-lived UIs) stay in memory forever.
- **Event storms / cascades:** an observer that triggers another event can loop. Design flows carefully.
- One slow/failing observer can stall others if you notify synchronously — wrap each in try/catch (and consider async).

---

## 🎯 Pattern 3: Command

**Q: "How would you implement undo/redo? Which pattern?"**

**Model answer:**

> Command turns a **request into an object** that knows how to `execute()` itself *and* `undo()` itself. Store executed commands on a stack → free undo. Move undone ones to a redo stack → free redo.

**Analogy:** Google Docs. Every keystroke/format is a command object; Ctrl+Z pops the last one and reverses it. Also: Photoshop's history panel, PayTM transaction reversal, task queues, macros.

**Problem it solves:** Naively, undo means snapshotting the whole document on every edit — ~1 GB for 10k edits on a 100 KB doc. Command stores only the *operation* (~1 MB) — roughly **1000× less memory** — and adds redo, selective undo, and batching.

**Minimal Java:**

```java
import java.util.*;

interface Command { void execute(); void undo(); }

class Invoker {
    private final Deque<Command> history = new ArrayDeque<>();
    private final Deque<Command> redo = new ArrayDeque<>();
    public void run(Command c)  { c.execute(); history.push(c); redo.clear(); }
    public void undo() { if (!history.isEmpty()) { Command c = history.pop(); c.undo(); redo.push(c); } }
    public void redo() { if (!redo.isEmpty())   { Command c = redo.pop(); c.execute(); history.push(c); } }
}
```

**When to use:** Undo/redo, transaction reversal, job/task queues (each job is a command object), macro recording, audit logs.

**Pitfalls / when NOT to use:**
- Fire-and-forget actions with no undo/queue/log need = pure overhead.
- Some actions can't truly be undone (a sent email). Then `undo()` is a *compensating action* (send a correction), not a real reversal — be honest about that.
- Memory still grows with unbounded history; cap the stack if needed.

---

## 🎯 Pattern 4: State

**Q: "When does the State pattern beat a big switch statement?"**

**Model answer:**

> State lets an object **change its behavior when its internal state changes** — it behaves as if it changed its class. Each state is a class; the context delegates to its current state object.

**Analogy:** Ola ride lifecycle. `cancel()` is free while *Searching*, Rs.50 once a *Driver is Assigned*, Rs.100 while *Waiting*, and *not allowed* mid-ride. Same action, different behavior — and different *transitions* — per state.

**Problem it solves:** Without it, *every method* repeats the same `if (state == ...)` block, and adding a new state means editing every method (easy to miss one). With State, all logic for a state lives in one class, and the interface forces every state to handle every action.

**Minimal Java:**

```java
interface State { void handle(Context c); }

class Context {
    private State state;
    public void setState(State s) { this.state = s; }
    public void request() { state.handle(this); } // behavior depends on current state
}
```

**When to use:** Well-defined lifecycle/workflow with transitions — order status, document approval, connection (disconnected/connecting/connected), UI button (idle/loading/disabled), game character states.

**Pitfalls / when NOT to use:**
- Only 2 states with trivial behavior → if-else is simpler.
- States with no transitions between them → you're probably overcomplicating.
- Class explosion: many states × few-line behaviors can feel heavy.

---

## 🔀 Cross-Cutting Interview Q&A

### Q1. Strategy vs State — they look identical. What's the difference?

Both wrap behavior in interchangeable classes behind an interface. The difference is **intent** and **who triggers the switch**:

| Aspect | **Strategy** | **State** |
|---|---|---|
| Intent | Pick *one of many* ways to do *the same task* | Change behavior as the object moves through a *lifecycle* |
| Who changes it | The **client** chooses/sets the strategy | The **states themselves** trigger transitions to the next state |
| Do the classes know each other? | No — strategies are independent, unaware of siblings | Yes — a state often decides the *next* state (`setState(new NextState())`) |
| "Switching" | Optional & external (set it once, or swap on a whim) | Core to the pattern — transitions are the whole point |
| Mental model | "Which **algorithm**?" | "Which **phase** am I in?" |

> 🗣️ **One-liner for the interview:** *"Strategy is about interchangeable algorithms chosen from outside; State is about an object's behavior evolving through a lifecycle, where states drive their own transitions."*

---

### Q2. Observer — give a concrete real-world use beyond textbook examples.

- **React/Redux & MobX:** components observe a store; state change re-renders subscribers.
- **Android LiveData / Kotlin Flow:** UI observes data, auto-updates on change.
- **Kafka / RabbitMQ pub-sub:** producers publish, consumers (observers) react — distributed Observer.
- **Spring `ApplicationEvent` / `@EventListener`:** publish an event, any number of listeners react.
- **WebSockets / Server-Sent Events:** clients subscribe; server pushes on change.
- **Stock tickers, sports score apps, Slack notifications** — all one-to-many "on change, notify" systems.

---

### Q3. Decorator vs Inheritance — why prefer wrapping?

| | **Inheritance** | **Decorator (composition)** |
|---|---|---|
| When combinations are decided | **Compile time** (fixed class hierarchy) | **Runtime** (wrap dynamically) |
| Combining features | Class explosion: `CoffeeWithMilkAndSugarAndCream`... | Stack decorators: `Sugar(Milk(Coffee()))` |
| Flexibility | Rigid — one chain of `extends` | Mix & match any order, any count |
| Coupling | Tight to parent | Loose — decorator only needs the interface |

> 🗣️ **One-liner:** *"Inheritance fixes behavior at compile time and explodes combinatorially; Decorator adds behavior at runtime by wrapping, so N features = N decorators instead of 2^N subclasses."* (Decorator is structural — Week 2 — but interviewers love comparing it to behavioral choices.)

---

### Q4. Adapter vs Facade — both "wrap" something. Difference?

| | **Adapter** | **Facade** |
|---|---|---|
| Purpose | Make an **incompatible** interface usable (convert A→B) | **Simplify** a complex subsystem behind one easy interface |
| Wraps | Usually **one** class with the "wrong" interface | **Many** classes / a whole subsystem |
| Changes the interface? | Yes — translates to what the client expects | Yes — offers a *new, simpler* one (doesn't hide the originals) |
| Analogy | UPI adapter between a bank's API and an app | Swiggy "Place Order" button hiding 7 subsystems |

> 🗣️ **One-liner:** *"Adapter is about compatibility (one mismatched thing), Facade is about simplicity (many things behind one door)."*

---

### Q5. Strategy vs Command — both encapsulate behavior. How are they different?

| | **Strategy** | **Command** |
|---|---|---|
| Encapsulates | An **algorithm** (a *how*) | A **request/action** (a *what to do*) |
| Has undo? | No | Yes — `undo()` is central |
| Reusable / parameterized? | Picked to compute a result | Often stored, queued, logged, replayed |
| Typical method | `calculate()` / `execute(input) → output` | `execute()` + `undo()`, often no return |

> 🗣️ *"Strategy answers 'which algorithm runs', Command packages 'an action I can run later, queue, log, or undo'."*

---

### Q6. Observer vs Pub-Sub (Mediator/Message Broker) — same thing?

Closely related, not identical:

| | **Observer** | **Pub-Sub (broker)** |
|---|---|---|
| Coupling | Subject holds **direct references** to observers | Publishers & subscribers **don't know each other**; a broker sits between |
| Topology | In-process, one subject → many observers | Often cross-process / distributed (Kafka, Redis) |
| Filtering | By subscribing to a subject | By **topic/channel**, broker routes |

> Observer is the in-memory pattern; pub-sub generalizes it across services with a message broker as the middleman.

---

### Q7. How does Strategy relate to the Open/Closed Principle?

Strategy is the *textbook embodiment* of OCP — **open for extension, closed for modification.** Adding a new behavior = adding a new strategy class; existing strategies and the context are untouched. No risk of breaking working code, no merge conflicts between devs adding different strategies. (Same argument applies to State and Observer-observers.)

---

### Q8. In Observer, how do you prevent a memory leak?

- **Always unsubscribe** when an observer's lifecycle ends (e.g., UI component unmounts).
- Use **weak references** for observers so the GC can reclaim them even if you forget to unsubscribe.
- Provide a clear `unsubscribe()` and document the ownership/lifetime.
- In modern stacks, prefer disposable subscriptions (RxJava `Disposable`, returning an unsubscribe function) so cleanup is explicit.

---

### Q9. Can patterns be combined? Give an example.

Yes — real systems layer them. A Swiggy-style order flow:

- **Facade** (`OrderFacade`) hides 7 subsystems behind "Place Order".
- **Factory** creates the right `Notification` channel; wrapped by a **Decorator** that adds logging.
- **Strategy** (`PricingEngine`) applies the current discount algorithm.
- **Observer** (`OrderEventManager`) notifies analytics, restaurant dashboard, delivery assignment.
- **State** drives the order through Placed → Preparing → Out-for-delivery → Delivered.
- **Singleton** shares one DB connection.

> Mentioning *how patterns compose* is a strong senior signal — patterns are tools, not religions.

---

### Q10. Strategy vs simple `if-else` — when is if-else actually better?

Be the engineer who knows when *not* to pattern:
- Only **2–3 fixed** branches that will **never grow** → if-else is clearer and shorter.
- The logic is **trivial** (a different message string) → a `Map<String,String>` beats 4 classes.
- The branches share lots of **mutable context state** → extracting strategies adds plumbing.

Use Strategy once you see the if-else **growing**, **changing at runtime**, or causing **merge conflicts**.

---

### Q11. What's a "context" in Strategy/State, and why does it matter?

The **context** is the class that *holds* and *delegates to* the current strategy/state (`PricingEngine`, `VendingMachine`, `Ride`). It's the stable public API the client talks to; the swappable object behind it changes. Interviewers like hearing you name it — it shows you understand the pattern's structure, not just the vibe.

---

### Q12. How does Command enable a job/task queue?

Because a Command is a self-contained object (`execute()` + its data), you can **put it on a queue** and run it later, on another thread, or on another machine. The producer creates commands; a worker pool pops and executes them. This is exactly how background-job frameworks (Sidekiq, Celery-style task objects) and `Runnable` in Java work — a `Runnable` *is* a Command with just `execute()`.

---

## 🧠 Memory Hooks

| Pattern | Vivid analogy | Trigger — "use this when the problem says…" |
|---|---|---|
| **Strategy** | 🚗 Ola route/fare: same car, swap the route/pricing | "…choose between **different algorithms** for the same task at runtime" / "growing switch over *how*" |
| **Observer** | 📺 YouTube subscribe: upload → all subscribers pinged | "…when **one thing changes**, many others must **react**" / "notify", "event", "subscribe" |
| **Command** | ⌨️ Google Docs Ctrl+Z: each edit is an undoable slip | "…**undo/redo**", "queue actions", "log/replay", "macro", "transaction reversal" |
| **State** | 🛺 Ola ride lifecycle: cancel costs differently per phase | "…behavior **depends on current state**" with "**lifecycle / transitions / status**" |

**Trigger-word cheat sheet:**
- "algorithm", "pricing", "sorting", "swap at runtime" → **Strategy**
- "notify", "subscribe", "event", "on change react" → **Observer**
- "undo", "redo", "queue", "history", "reverse" → **Command**
- "status", "lifecycle", "state machine", "transitions" → **State**

---

## 🏁 Final Interview Tip

> **Don't just name the pattern — narrate the journey.** The candidates who get hired say: *"Here's the ugly if-else this leads to → here's the analogy that makes it obvious → here's the refactor → and here's when I'd NOT bother."* That arc proves you understand the *forces* behind the pattern, not just its name.
>
> And always close with **trade-offs**. Saying *"Strategy adds classes, so for two fixed options I'd just use if-else"* is worth more than flawlessly reciting all four patterns. **Interviewers hire judgment, not memorization.** 🎯
