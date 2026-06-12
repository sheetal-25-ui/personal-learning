# Week 5: Multithreading and Concurrency — Interview Answers & Exercise Solutions

> **The Interview Golden Rule** 🏆
> Whenever you answer a concurrency question, walk through four beats:
> **Definition → Why it exists → Code example → The concurrency pitfall it addresses.**
> Interviewers at Microsoft, Anthropic, Flipkart and Swiggy are not testing whether you can
> recite syntax — they are testing whether you understand *what happens when two workers touch
> the same thing at the same time*. Concurrency bugs are invisible 99% of the time and
> catastrophic the other 1% (a `-6000` bank balance, a duplicate train ticket, a frozen UI).
> So always name the pitfall: race condition, deadlock, stale read, lost update. That single
> sentence is what separates "knows the API" from "can be trusted in production." 🎯

This file has two parts:
- **PART A** — every Practice Exercise from the Plan, fully solved with correct, compilable Java.
- **PART B** — 16 interview questions with confident, spoken-style model answers and comparison tables.

---

# PART A — Exercise / Practice Solutions

The Plan lists 5 exercises. Each is solved below with a complete, runnable Java program, a plain
explanation of *why* the solution is thread-safe, and the pitfall it avoids.

---

## ✅ Exercise 1: Print Numbers Alternately

**Task:** Thread-1 prints odd numbers (1, 3, 5…), Thread-2 prints even numbers (2, 4, 6…).
Output must be in strict order: `1, 2, 3, 4, 5, 6…`

**The pitfall:** Without coordination, both threads race and you'd get `1 3 5 2 4 6` or any
jumble. We need the two threads to *take turns* — classic inter-thread communication, solved
with `wait()`/`notify()` on a shared lock plus a turn flag.

```java
public class AlternatePrinter {

    // Shared coordination object. 'final' so both threads lock the SAME monitor.
    private final Object lock = new Object();

    // true => odd thread's turn, false => even thread's turn.
    private boolean oddTurn = true;

    private final int max;

    public AlternatePrinter(int max) {
        this.max = max;
    }

    public void printOdd() {
        synchronized (lock) {
            for (int i = 1; i <= max; i += 2) {
                // Wait while it is NOT our turn. 'while' (not 'if') guards spurious wakeups.
                while (!oddTurn) {
                    try {
                        lock.wait();        // releases the lock and sleeps
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // preserve interrupt status
                        return;
                    }
                }
                System.out.println(Thread.currentThread().getName() + ": " + i);
                oddTurn = false;            // hand the turn to the even thread
                lock.notifyAll();           // wake the even thread
            }
        }
    }

    public void printEven() {
        synchronized (lock) {
            for (int i = 2; i <= max; i += 2) {
                while (oddTurn) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                System.out.println(Thread.currentThread().getName() + ": " + i);
                oddTurn = true;             // hand the turn back to the odd thread
                lock.notifyAll();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        AlternatePrinter printer = new AlternatePrinter(10);

        Thread oddThread  = new Thread(printer::printOdd,  "Odd ");
        Thread evenThread = new Thread(printer::printEven, "Even");

        oddThread.start();
        evenThread.start();

        oddThread.join();
        evenThread.join();
        System.out.println("Done. Numbers printed in strict order.");
    }
}
```

**Output (deterministic order):**
```
Odd : 1
Even: 2
Odd : 3
Even: 4
...
Even: 10
Done. Numbers printed in strict order.
```

**Why it works:** The `oddTurn` flag enforces strict alternation. A thread that grabs the lock
but finds it is not its turn calls `wait()` (which *releases* the lock so the other thread can
proceed) and only resumes after `notifyAll()`. The `while` loop re-checks the condition after
waking — this defends against spurious wakeups, the #1 wait/notify bug.

---

## ✅ Exercise 2: IRCTC Ticket Booking Simulator

**Task:** `TrainBooking` has 5 seats. 10 passengers (threads) try to book simultaneously.
No two passengers may get the same seat; no overbooking.

**The pitfall:** This is the **race condition** from the bank-account example. The dangerous
sequence is *check-then-act*: `if (seats > 0) { seats--; }`. Two threads can both pass the
`if` before either decrements, causing overbooking. The fix is to make check-and-decrement
**atomic** — here with `synchronized` (also showing an `AtomicInteger` variant).

```java
import java.util.concurrent.atomic.AtomicInteger;

class TrainBooking {

    private int availableSeats;
    // Track the next seat number to assign so no two passengers get the same seat.
    private int nextSeatNumber = 1;

    public TrainBooking(int totalSeats) {
        this.availableSeats = totalSeats;
    }

    // synchronized makes the ENTIRE check-then-book atomic — only one thread at a time.
    public synchronized boolean bookSeat(String passenger) {
        if (availableSeats > 0) {
            int assignedSeat = nextSeatNumber++;
            availableSeats--;
            System.out.println("✅ " + passenger + " booked seat #" + assignedSeat
                    + " (seats left: " + availableSeats + ")");
            return true;
        } else {
            System.out.println("❌ " + passenger + " — SOLD OUT, no seat assigned.");
            return false;
        }
    }
}

public class IrctcBookingSimulator {
    public static void main(String[] args) throws InterruptedException {
        TrainBooking train = new TrainBooking(5);   // 5 seats only

        Thread[] passengers = new Thread[10];
        for (int i = 1; i <= 10; i++) {
            String name = "Passenger-" + i;
            passengers[i - 1] = new Thread(() -> train.bookSeat(name));
        }

        for (Thread p : passengers) p.start();
        for (Thread p : passengers) p.join();

        System.out.println("All booking attempts complete. Exactly 5 succeeded.");
    }
}
```

**Sample output (order varies, but always exactly 5 succeed):**
```
✅ Passenger-3 booked seat #1 (seats left: 4)
✅ Passenger-1 booked seat #2 (seats left: 3)
✅ Passenger-7 booked seat #3 (seats left: 2)
✅ Passenger-2 booked seat #4 (seats left: 1)
✅ Passenger-9 booked seat #5 (seats left: 0)
❌ Passenger-5 — SOLD OUT, no seat assigned.
... (remaining 4 also SOLD OUT)
All booking attempts complete. Exactly 5 succeeded.
```

**AtomicInteger variant (lock-free):** if all you need is to prevent overbooking, you can do the
check-and-decrement atomically with a compare-and-swap loop — no `synchronized` block needed.

```java
import java.util.concurrent.atomic.AtomicInteger;

class TrainBookingAtomic {
    private final AtomicInteger availableSeats;

    public TrainBookingAtomic(int totalSeats) {
        this.availableSeats = new AtomicInteger(totalSeats);
    }

    public boolean bookSeat(String passenger) {
        while (true) {
            int current = availableSeats.get();
            if (current <= 0) {
                System.out.println("❌ " + passenger + " — SOLD OUT.");
                return false;
            }
            // Only succeed if the value is still 'current' (no one slipped in between).
            if (availableSeats.compareAndSet(current, current - 1)) {
                System.out.println("✅ " + passenger + " booked a seat. Left: " + (current - 1));
                return true;
            }
            // CAS failed -> another thread changed it -> retry the loop.
        }
    }
}
```

**Why it works:** `synchronized` serializes the whole check-and-book so the `if` and the `seats--`
happen as one indivisible unit. The atomic version uses CAS: it only decrements if the seat count
hasn't changed since it read it, retrying on conflict. Both make overbooking impossible.

---

## ✅ Exercise 3: Parallel Price Comparator

**Task:** Use `CompletableFuture` to fetch prices from 4 "websites" (simulated with delays).
Find the lowest price and its website name. **Show each result as it arrives** — don't wait for
all of them before printing anything.

**The pitfall:** Doing this sequentially would take `sum` of all delays (~6.5s). Done in
parallel it takes only the *slowest* site (~2s). The trick for "show as it arrives" is to attach
a `thenAccept` callback to each future so it prints the moment *that* site responds, while
`allOf` aggregates the final winner.

```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PriceComparator {

    // A tiny record-like holder for a site's result.
    static class Quote {
        final String site;
        final int price;
        Quote(String site, int price) { this.site = site; this.price = price; }
    }

    // Simulate a network call to one site with a given delay and price.
    static CompletableFuture<Quote> fetchPrice(String site, int delayMs, int price) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(delayMs);   // simulate network latency
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new Quote(site, price);
        }).thenApply(quote -> {
            // Print the moment THIS site responds — not after all are done.
            System.out.println("📥 " + quote.site + " responded: Rs. " + quote.price);
            return quote;
        });
    }

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();

        List<CompletableFuture<Quote>> quotes = List.of(
                fetchPrice("Flipkart", 1500, 45000),
                fetchPrice("Amazon",   2000, 43000),
                fetchPrice("Croma",    1000, 46000),
                fetchPrice("Reliance", 1800, 44000)
        );

        // allOf completes only when ALL four are done.
        CompletableFuture<Void> all = CompletableFuture.allOf(
                quotes.toArray(new CompletableFuture[0]));

        // When everything is done, gather results and find the minimum.
        CompletableFuture<Quote> cheapest = all.thenApply(v ->
                quotes.stream()
                      .map(CompletableFuture::join)   // safe: all already complete
                      .min(Comparator.comparingInt(q -> q.price))
                      .orElseThrow());

        Quote best = cheapest.get();   // block here only for the final aggregate
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("\n🏆 Lowest price: Rs. " + best.price + " at " + best.site);
        System.out.println("⏱  Total time ~" + elapsed + "ms (parallel, ≈ slowest site)");
    }
}
```

**Sample output:**
```
📥 Croma responded: Rs. 46000
📥 Flipkart responded: Rs. 45000
📥 Reliance responded: Rs. 44000
📥 Amazon responded: Rs. 43000

🏆 Lowest price: Rs. 43000 at Amazon
⏱  Total time ~2000ms (parallel, ≈ slowest site)
```

**Why it works:** Each `supplyAsync` runs on the common `ForkJoinPool`, so all four "network
calls" happen at once. The chained `thenApply` prints each result the instant it arrives.
`allOf` waits for all four; only then do we `join` (which is now non-blocking) and pick the
minimum. Total wall-clock time ≈ the slowest single site, not the sum.

---

## ✅ Exercise 4: Thread-Safe Counter (3 versions + comparison)

**Task:** A counter incremented by 50 threads, 10,000 times each. Final count must be exactly
**500,000**. Implement three versions — `synchronized`, `ReentrantLock`, `AtomicInteger` — plus
an unsafe version to *prove* the race exists.

**The pitfall:** `count++` is NOT atomic — it's three steps (read, add, write). With 50 threads
interleaving, updates get lost, so the unsafe version produces a number *less* than 500,000.

```java
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadSafeCounterDemo {

    static final int THREADS = 50;
    static final int INCREMENTS_PER_THREAD = 10_000;
    static final int EXPECTED = THREADS * INCREMENTS_PER_THREAD; // 500_000

    // --- Version 0: UNSAFE (demonstrates the race) ---
    static class UnsafeCounter {
        private int count = 0;
        void increment() { count++; }     // read-modify-write: NOT atomic
        int get() { return count; }
    }

    // --- Version 1: synchronized ---
    static class SyncCounter {
        private int count = 0;
        synchronized void increment() { count++; }
        synchronized int get() { return count; }
    }

    // --- Version 2: ReentrantLock ---
    static class LockCounter {
        private int count = 0;
        private final ReentrantLock lock = new ReentrantLock();
        void increment() {
            lock.lock();
            try { count++; }
            finally { lock.unlock(); }    // unlock ALWAYS, even on exception
        }
        int get() {
            lock.lock();
            try { return count; }
            finally { lock.unlock(); }
        }
    }

    // --- Version 3: AtomicInteger (lock-free) ---
    static class AtomicCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        void increment() { count.incrementAndGet(); }
        int get() { return count.get(); }
    }

    // Generic test harness: runs 'task' on 50 threads and reports the final value.
    static int runTest(Runnable incrementTask) throws InterruptedException {
        Thread[] threads = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                    incrementTask.run();
                }
            });
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        return 0; // value fetched by caller from the counter object
    }

    public static void main(String[] args) throws InterruptedException {
        // Unsafe
        UnsafeCounter unsafe = new UnsafeCounter();
        runTest(unsafe::increment);
        System.out.printf("Unsafe      : %d  (expected %d) -> %s%n",
                unsafe.get(), EXPECTED, unsafe.get() == EXPECTED ? "OK" : "WRONG ❌");

        // synchronized
        SyncCounter sync = new SyncCounter();
        runTest(sync::increment);
        System.out.printf("synchronized: %d  (expected %d) -> %s%n",
                sync.get(), EXPECTED, sync.get() == EXPECTED ? "OK ✅" : "WRONG");

        // ReentrantLock
        LockCounter lockCounter = new LockCounter();
        runTest(lockCounter::increment);
        System.out.printf("ReentrantLock: %d (expected %d) -> %s%n",
                lockCounter.get(), EXPECTED, lockCounter.get() == EXPECTED ? "OK ✅" : "WRONG");

        // AtomicInteger
        AtomicCounter atomic = new AtomicCounter();
        runTest(atomic::increment);
        System.out.printf("AtomicInteger: %d (expected %d) -> %s%n",
                atomic.get(), EXPECTED, atomic.get() == EXPECTED ? "OK ✅" : "WRONG");
    }
}
```

**Sample output:**
```
Unsafe      : 487213  (expected 500000) -> WRONG ❌
synchronized: 500000  (expected 500000) -> OK ✅
ReentrantLock: 500000 (expected 500000) -> OK ✅
AtomicInteger: 500000 (expected 500000) -> OK ✅
```
*(The unsafe number changes every run — sometimes 491022, sometimes 478900 — which is exactly
why race conditions are so dangerous: they're non-deterministic.)*

**Comparison:**

| Version | Correct? | Mechanism | Speed | Notes |
|---|---|---|---|---|
| Unsafe `count++` | ❌ No | none | fastest | Lost updates — never use for shared state |
| `synchronized` | ✅ Yes | monitor lock | slowest under contention | Simplest, blocks other threads |
| `ReentrantLock` | ✅ Yes | explicit lock | similar to synchronized | More features (tryLock, fairness) |
| `AtomicInteger` | ✅ Yes | CAS (lock-free) | fastest of the safe options | Best for a single counter |

**Takeaway for interviews:** For a single numeric counter, `AtomicInteger` wins — it's correct
*and* fast because it uses a CPU-level Compare-And-Swap instead of blocking.

---

## ✅ Exercise 5: Dining Philosophers (deadlock, then fixed)

**Task:** 5 philosophers around a table, 5 forks (one between each pair). A philosopher needs
**both** neighboring forks to eat. First write a version that **deadlocks**, then fix it with
**ordered lock acquisition**.

**The pitfall:** This is the textbook **circular wait** deadlock. If every philosopher grabs
their *left* fork first and then waits for their *right*, all 5 can hold their left fork
simultaneously and wait forever for a right fork that never frees up.

### Part 1 — The Deadlocking Version

```java
public class DiningPhilosophersDeadlock {

    static class Fork {
        final int id;
        Fork(int id) { this.id = id; }
    }

    static class Philosopher extends Thread {
        private final int id;
        private final Fork left;
        private final Fork right;

        Philosopher(int id, Fork left, Fork right) {
            this.id = id; this.left = left; this.right = right;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    System.out.println("Philosopher " + id + " is thinking 🤔");
                    Thread.sleep((long) (Math.random() * 100));

                    // BUG: everyone grabs LEFT first, then RIGHT -> circular wait -> deadlock.
                    synchronized (left) {
                        System.out.println("Philosopher " + id + " picked up LEFT fork " + left.id);
                        Thread.sleep(50);   // pause increases the chance of deadlock
                        synchronized (right) {
                            System.out.println("Philosopher " + id + " picked up RIGHT fork "
                                    + right.id + " and is EATING 🍝");
                            Thread.sleep(50);
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        int n = 5;
        Fork[] forks = new Fork[n];
        for (int i = 0; i < n; i++) forks[i] = new Fork(i);

        for (int i = 0; i < n; i++) {
            Fork left = forks[i];
            Fork right = forks[(i + 1) % n];
            new Philosopher(i, left, right).start();
        }
        // This version will FREEZE — all philosophers hold a left fork, wait for a right.
    }
}
```

### Part 2 — The Fixed Version (Ordered Lock Acquisition)

The fix: every philosopher acquires the **lower-numbered fork first**, regardless of whether it's
their left or right. This breaks the circular-wait condition — at least one philosopher will
always be able to grab both forks.

```java
public class DiningPhilosophersFixed {

    static class Fork {
        final int id;
        Fork(int id) { this.id = id; }
    }

    static class Philosopher extends Thread {
        private final int id;
        private final Fork first;   // ALWAYS the lower-id fork
        private final Fork second;  // ALWAYS the higher-id fork

        Philosopher(int id, Fork left, Fork right) {
            this.id = id;
            // Order locks by fork id so all philosophers agree on acquisition order.
            if (left.id < right.id) { this.first = left;  this.second = right; }
            else                    { this.first = right; this.second = left;  }
        }

        @Override
        public void run() {
            try {
                for (int meal = 0; meal < 3; meal++) {   // eat 3 times then leave
                    System.out.println("Philosopher " + id + " is thinking 🤔");
                    Thread.sleep((long) (Math.random() * 100));

                    synchronized (first) {                // lower id first — same global order
                        synchronized (second) {           // higher id second
                            System.out.println("Philosopher " + id + " is EATING 🍝 "
                                    + "(forks " + first.id + " & " + second.id + ")");
                            Thread.sleep(50);
                        }
                    }
                    System.out.println("Philosopher " + id + " finished a meal ✅");
                }
                System.out.println("Philosopher " + id + " is full and leaves the table 🚶");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int n = 5;
        Fork[] forks = new Fork[n];
        for (int i = 0; i < n; i++) forks[i] = new Fork(i);

        Philosopher[] philosophers = new Philosopher[n];
        for (int i = 0; i < n; i++) {
            Fork left = forks[i];
            Fork right = forks[(i + 1) % n];
            philosophers[i] = new Philosopher(i, left, right);
            philosophers[i].start();
        }

        for (Philosopher p : philosophers) p.join();
        System.out.println("All philosophers ate without deadlock. 🎉");
    }
}
```

**Why the fix works:** Deadlock requires all four Coffman conditions, including **circular
wait**. By forcing a *global ordering* on lock acquisition (always grab the smaller fork id
first), a cycle becomes impossible — there's always one philosopher whose "first" fork is free,
so they eat, release, and the chain unblocks. This is the single most important deadlock-prevention
technique: **acquire locks in a consistent global order everywhere.**

*Alternative fixes worth mentioning in an interview:* use `tryLock()` with a timeout and back off
if you can't get both forks; cap the number of philosophers allowed to eat at once with a
`Semaphore(4)`; or make one philosopher "left-handed" (grabs right first) to break the symmetry.

---

# PART B — Interview Questions & Model Answers

Below are the questions most likely to come up. Each answer is written the way you'd *say* it
out loud — confident, structured, and ending with the pitfall it addresses.

---

### Q1. What is the difference between a process and a thread?

> "A **process** is an independent running program with its own memory space, file handles, and
> resources — like a separate Zomato delivery rider with their own bike and bag. A **thread** is a
> lightweight unit of execution *inside* a process, and crucially, threads **share** the process's
> memory — like several chefs working in the same kitchen sharing the same stove. Threads are
> cheap to create and easy to communicate between because of shared memory, but that same shared
> memory is the source of race conditions, which is the whole reason synchronization exists. The
> trade-off: if one thread corrupts shared state or crashes badly, it can take down the whole
> process, whereas one process crashing doesn't touch another."

| | Process | Thread |
|---|---|---|
| Memory | Own isolated memory | Shared with sibling threads |
| Creation cost | Expensive | Cheap |
| Communication | Hard (IPC) | Easy (shared memory) |
| Crash blast radius | Isolated | Can kill whole process |
| Example | Chrome **and** Word | Tabs **inside** Chrome |

---

### Q2. What's the difference between `Runnable`, `Thread`, and `Callable`?

> "`Thread` is the *worker* — the actual unit of execution. `Runnable` and `Callable` are *tasks*
> — descriptions of work you hand to a worker. The difference between the two task types is the
> return value: `Runnable.run()` returns `void` and can't throw checked exceptions, while
> `Callable.call()` **returns a value** and **can** throw checked exceptions. You submit a
> `Callable` to an `ExecutorService` and get back a `Future` to retrieve the result later. In
> practice I prefer `Runnable`/`Callable` over extending `Thread`, because Java has single
> inheritance — extending `Thread` burns my one superclass slot, and it also couples the task to
> the worker. Implementing an interface keeps the task reusable across raw threads, thread pools,
> and executors."

| Feature | Runnable | Callable | Thread |
|---|---|---|---|
| Role | Task | Task | Worker |
| Method | `run()` | `call()` | `start()` runs `run()` |
| Returns a value | ❌ void | ✅ yes (generic `<T>`) | ❌ |
| Checked exceptions | ❌ | ✅ | — |
| Used with | Thread, Executor | Executor only | itself |

---

### Q3. Why is implementing `Runnable` preferred over extending `Thread`?

> "Three reasons. **One — inheritance:** Java doesn't allow multiple inheritance, so if I extend
> `Thread` my class can't extend anything else; implementing `Runnable` keeps that slot free.
> **Two — separation of concerns:** `Runnable` is the *task* (what to do), `Thread` is the
> *worker* (who does it); keeping them separate is cleaner. **Three — reusability:** the same
> `Runnable` can go to a raw `Thread`, an `ExecutorService`, or a scheduled pool. The mental
> model: extending `Thread` says 'I *am* a waiter'; implementing `Runnable` says 'I *have* a task
> any waiter can do.' The second is far more flexible."

---

### Q4. What's the difference between calling `start()` and `run()`? (asked in almost every interview)

> "`start()` creates a **brand-new thread** and the JVM invokes `run()` *on that new thread*.
> Calling `run()` directly is just an ordinary method call — it executes on the **current** thread,
> so no new thread is created and there's zero concurrency. The giveaway is the thread name:
> with `start()`, `Thread.currentThread().getName()` inside `run()` prints something like
> `Thread-0`; with a direct `run()` call it prints `main`. Calling `run()` instead of `start()`
> is the single most common beginner bug — the code looks like it should be multithreaded but
> runs completely sequentially."

---

### Q5. Walk me through the thread lifecycle.

> "A thread moves through six states. **NEW** — the object is created but `start()` hasn't been
> called. **RUNNABLE** — `start()` was called; it's eligible to run and waiting for the CPU
> scheduler. **RUNNING** isn't a separate JVM enum but conceptually it's when the scheduler is
> actually executing it. Then there are the paused states: **BLOCKED** — waiting to acquire a
> monitor lock; **WAITING** — parked indefinitely via `wait()`, `join()`, or `park()` until
> signaled; and **TIMED_WAITING** — paused with a timeout via `sleep(ms)`, `wait(ms)`, or
> `join(ms)`. Finally **TERMINATED** — `run()` has finished. The key gotcha: a terminated thread
> is **dead and cannot be restarted** — calling `start()` on it throws `IllegalThreadStateException`.
> You'd create a new thread instead."

```java
Thread t = new Thread(() -> {});
System.out.println(t.getState());  // NEW
t.start();
// ... RUNNABLE / TIMED_WAITING / TERMINATED depending on timing
```

---

### Q6. What is the `synchronized` keyword, and how does it work?

> "`synchronized` enforces **mutual exclusion**: only one thread can execute a synchronized
> method or block on a given lock object at a time; others queue up. Every Java object has an
> intrinsic lock called a *monitor*. A synchronized **instance method** locks on `this`; a
> synchronized **static method** locks on the `Class` object; a synchronized **block** locks on
> whatever object you name. It does two things at once: it provides mutual exclusion *and* a
> memory-visibility guarantee — when a thread exits a synchronized block, its writes are flushed
> and become visible to the next thread that enters. The pitfall it solves is the race condition
> on *compound* actions like check-then-act or read-modify-write. The cost is contention: threads
> wait in line, so you synchronize the **minimum** code necessary — prefer a tight synchronized
> block over locking a whole 50-line method."

---

### Q7. Synchronized method vs synchronized block — when do you use which?

> "A synchronized **method** locks the entire method body on `this` (or the class for static
> methods) — simple but coarse. A synchronized **block** lets me lock only the few critical lines
> and on a *dedicated private final lock object* of my choosing. I reach for a block when only a
> small part of a method touches shared state, or when I want a private lock so external code
> can't accidentally synchronize on my object and cause a deadlock. Rule of thumb: lock the
> narrowest scope on the most private object you can."

```java
private final Object lock = new Object();   // private + final
void doWork() {
    // unsynchronized prep work happens in parallel
    synchronized (lock) { /* only the critical section */ }
    // unsynchronized logging happens in parallel
}
```

---

### Q8. What does `volatile` do, and how is it different from `synchronized`?

> "`volatile` guarantees **visibility** — every read goes to main memory and every write is
> flushed to main memory, so threads never see a stale cached copy. It does **not** provide
> **atomicity** for compound operations. So `volatile boolean running` is the perfect use case
> for a stop-flag that one thread sets and another reads in a loop. But `volatile int count;
> count++` is still broken, because `count++` is read-modify-write — three steps — and `volatile`
> only makes each individual read and write visible, not the trio atomic. For that I need
> `synchronized` or an `AtomicInteger`. So: `volatile` for simple flags, `synchronized`/atomics
> for compound updates."

| | volatile | synchronized |
|---|---|---|
| Guarantees | Visibility only | Visibility **+** mutual exclusion |
| Atomic compound ops | ❌ No | ✅ Yes |
| Blocks threads | No | Yes |
| Best for | on/off flags | check-then-act, count++ |

---

### Q9. Explain `wait()`, `notify()`, and `notifyAll()`. Why must they be in a synchronized block?

> "These are the **inter-thread communication** primitives, defined on `Object` not `Thread`.
> `wait()` makes the current thread **release the lock** and park until another thread calls
> `notify()` or `notifyAll()` on the same object. `notify()` wakes one arbitrary waiting thread;
> `notifyAll()` wakes them all. They must be called from inside a `synchronized` block on that
> same object — otherwise you get `IllegalMonitorStateException` — because the whole mechanism
> relies on holding the monitor to safely check the condition and release it atomically. Two
> non-negotiable rules: always wrap `wait()` in a **`while` loop**, never an `if`, to guard
> against **spurious wakeups** and stale conditions; and prefer `notifyAll()` over `notify()` to
> avoid a thread waking the 'wrong' waiter and causing a missed-signal hang. This is the engine
> behind the producer-consumer pattern."

---

### Q10. What's the difference between `sleep()` and `wait()`?

> "Both pause a thread, but they're fundamentally different. `sleep()` is a static method on
> `Thread`; it pauses for a fixed time and **does not release any lock** it holds — it just stops
> the CPU from scheduling it. `wait()` is on `Object`; it **releases the lock** and waits to be
> signaled by `notify`/`notifyAll`, and must be called inside a synchronized block. So `sleep` is
> for 'pause this thread for a bit'; `wait` is for 'pause until another thread tells me a
> condition is met.' The lock-release difference is the one interviewers care about — calling
> `sleep` while holding a lock can needlessly block every other thread."

| | sleep() | wait() |
|---|---|---|
| Defined on | Thread (static) | Object |
| Releases lock | ❌ No | ✅ Yes |
| Wakes on | timeout | notify / notifyAll / timeout |
| Must be in synchronized | No | Yes |
| Purpose | timed pause | inter-thread signaling |

---

### Q11. What is a deadlock? What are its conditions and how do you prevent it?

> "A **deadlock** is when two or more threads each hold a lock the other needs, so none can ever
> proceed — the program freezes. Picture two autos stuck nose-to-nose in a narrow lane, each
> waiting for the other to reverse. It needs all four **Coffman conditions** simultaneously:
> **mutual exclusion** (locks are exclusive), **hold-and-wait** (hold one, wait for another),
> **no preemption** (locks can't be force-taken), and **circular wait** (a cycle in the
> wait-for graph). Break **any one** and deadlock is impossible. The cleanest fix in practice is
> to destroy circular wait by always acquiring locks in a **consistent global order** — that's
> exactly how I fixed Dining Philosophers by always grabbing the lower-numbered fork first.
> Other tools: `tryLock()` with a timeout so you back off instead of blocking forever, avoiding
> nested locks, and using higher-level constructs like `BlockingQueue` that manage locking for you."

**Four conditions (all required):** Mutual Exclusion · Hold-and-Wait · No Preemption · Circular Wait.

---

### Q12. Differentiate deadlock, livelock, and starvation.

> "All three are **liveness** failures. **Deadlock** — threads are stuck blocked forever, each
> waiting on a lock the other holds; nothing moves and CPU is idle. **Livelock** — threads are
> *active* and keep responding to each other but make no progress, like two people in a hallway
> who keep stepping the same way to let the other pass and never actually pass; CPU is busy but
> useless work. **Starvation** — a thread *can* run but never gets the chance because greedy
> higher-priority threads or unfair locks keep hogging the resource; it's perpetually skipped.
> The fixes differ: deadlock → consistent lock ordering; livelock → add randomized backoff so the
> symmetry breaks; starvation → use **fair** locks (`new ReentrantLock(true)`) or fair scheduling."

| | Deadlock | Livelock | Starvation |
|---|---|---|---|
| Threads blocked? | Yes (stuck) | No (busy) | No (waiting turn) |
| Making progress? | Never | Never | Eventually maybe never |
| CPU usage | Idle | Busy (wasted) | Busy elsewhere |
| Typical fix | Lock ordering | Random backoff | Fair locks / priorities |

---

### Q13. What is an `ExecutorService` and why use a thread pool instead of `new Thread()`?

> "An `ExecutorService` is a high-level API that manages a **pool of reusable threads** and a task
> queue, so you submit work instead of manually creating threads. Creating a thread is expensive —
> memory for its stack plus OS scheduling overhead — and unbounded thread creation under load (say
> a new thread per request on Big Billion Days) will exhaust memory and crash the server. A pool
> caps concurrency, reuses threads, and queues excess work. Think Ola: a fixed pool of drivers
> reassigned ride to ride, not a new hire per ride. The main factory methods are
> `newFixedThreadPool(n)` for a known capacity, `newCachedThreadPool()` for bursty short tasks,
> `newSingleThreadExecutor()` for strictly sequential work, and `newScheduledThreadPool(n)` for
> recurring jobs. One must-do: always call `shutdown()` — otherwise the pool's threads stay alive
> and the JVM never exits."

```java
ExecutorService pool = Executors.newFixedThreadPool(3);
pool.submit(() -> System.out.println("task on " + Thread.currentThread().getName()));
pool.shutdown();   // never forget this
```

---

### Q14. Explain `Future` vs `CompletableFuture`.

> "A `Future` is a handle to a result that will be ready later — you `submit` a `Callable` and get
> a `Future`, then call `get()` to retrieve the result. The problem: `Future.get()` **blocks** the
> calling thread, and a plain `Future` can't be chained or combined. `CompletableFuture` (Java 8+)
> fixes both — it's **non-blocking and composable**. I can say 'when this finishes, *then* apply
> this transformation' with `thenApply`, react with `thenAccept`, chain another async call with
> `thenCompose`, combine two with `thenCombine`, and wait for many with `allOf`/`anyOf`, plus
> handle errors with `exceptionally`/`handle`. So I build a *pipeline* of async work without ever
> blocking a thread until I genuinely need the final value. That's exactly what the parallel
> price-comparator exercise uses."

| | Future | CompletableFuture |
|---|---|---|
| Blocking | `get()` blocks | callbacks, non-blocking |
| Chaining | ❌ | ✅ `thenApply`/`thenCompose` |
| Combine many | ❌ manual | ✅ `allOf`/`anyOf`/`thenCombine` |
| Error handling | try/catch around get | `exceptionally`/`handle` |
| Manual completion | ❌ | ✅ `complete(value)` |

---

### Q15. What are atomic classes and how do they work (CAS)?

> "Atomic classes like `AtomicInteger`, `AtomicLong`, `AtomicBoolean`, and `AtomicReference`
> provide thread-safe operations on a single variable **without locks**. Internally they use a
> CPU instruction called **Compare-And-Swap (CAS)**: 'update the value to N *only if* it still
> equals the expected old value; otherwise tell me you failed and I'll retry.' Because there's no
> blocking, they're much faster than `synchronized` under contention for simple counters and
> flags. `count.incrementAndGet()` is a single atomic operation, unlike the broken `count++`. The
> caveat is they only cover **one** variable — if I need to update several fields consistently as
> a unit, I still need a lock or a single `AtomicReference` to an immutable object holding all of
> them. Also worth mentioning the ABA problem, which `AtomicStampedReference` solves with a
> version stamp."

---

### Q16. Why is `HashMap` unsafe with threads, and what does `ConcurrentHashMap` do differently?

> "A plain `HashMap` is **not thread-safe** — concurrent writes can corrupt its internal buckets,
> and in older Java versions a concurrent resize could even spin into an infinite loop. The old
> fix, `Collections.synchronizedMap`, wraps every method in one big lock, so the whole map is
> single-threaded and slow. `ConcurrentHashMap` is the modern answer: it allows **concurrent
> reads with no locking at all** and locks only the **individual bucket** being written (bin-level
> / CAS-based locking in Java 8+), so many threads can write to different buckets at once. It also
> gives atomic compound operations like `putIfAbsent`, `compute`, and `merge` so you don't have to
> roll your own check-then-act. The trade-offs to remember: it **doesn't allow null keys or
> values**, and its iterators are **weakly consistent** — they don't throw
> `ConcurrentModificationException` but may not reflect updates made after iteration began."

| | HashMap | Collections.synchronizedMap | ConcurrentHashMap |
|---|---|---|---|
| Thread-safe | ❌ | ✅ | ✅ |
| Locking | none | one global lock | per-bucket / CAS |
| Concurrent reads | n/a | blocked | lock-free |
| Null key/value | allowed | allowed | ❌ not allowed |
| Atomic compound ops | ❌ | ❌ | ✅ putIfAbsent/compute/merge |

**Bonus — what *is* a race condition?** "It's when the correctness of a program depends on the
unpredictable *timing/interleaving* of threads accessing shared mutable state — like two ATM
cards reading a ₹10,000 balance before either writes the new one, ending at ₹-6,000. The cure is
to make the read-modify-write **atomic** via `synchronized`, a `Lock`, or an atomic class."

---

# 🧠 Memory Hooks

| Concept | Vivid Analogy | Trigger to Recall It |
|---|---|---|
| Thread | Chefs sharing one kitchen | "Shared memory, must coordinate" |
| Process | Separate delivery riders, own bikes | "Isolated, own everything" |
| Runnable vs Thread | "I *have* a task" vs "I *am* a waiter" | Prefer Runnable = more flexible |
| start() vs run() | Hiring a new waiter vs doing it yourself | run() = same thread, no concurrency |
| synchronized | Lock on the ATM room door | One person in at a time |
| volatile | A live scoreboard everyone sees instantly | Visibility, NOT atomicity |
| wait()/notify() | Chef rings bell when food's on the counter | Release lock, signal, `while` loop |
| Race condition | Couple both withdraw ₹8000 from ₹10000 | Check-then-act on shared state |
| Deadlock | Two autos nose-to-nose in a narrow gali | Circular wait — neither reverses |
| Livelock | Two people dodging the same way in a hallway | Busy but no progress |
| Starvation | Polite person who never gets a turn at the buffet | Unfair scheduling |
| Thread Pool / Executor | Ola's pool of drivers, reassigned per ride | Reuse threads, don't create per task |
| Future | Restaurant order receipt | Show receipt later → get food |
| CompletableFuture | "When my food's ready, *then* add sauce" | Non-blocking pipeline |
| AtomicInteger (CAS) | "Update only if no one changed it; else retry" | Lock-free counter |
| ConcurrentHashMap | Many cashiers, each locks only their own till | Per-bucket locking, lock-free reads |
| ReentrantLock + finally | Always unlock the ATM, even if you faint | unlock() in `finally` |
| Dining Philosophers fix | Everyone grabs the lower-numbered fork first | Global lock ordering breaks cycles |

---

# 🎯 Final Interview Tip

When you get a concurrency question, **don't just answer the API — narrate the danger.** Say the
words *race condition*, *stale read*, *deadlock*, or *lost update* out loud, then show how your
solution closes that exact hole. And whenever you can, offer two levels: the **fundamentals**
answer (`wait`/`notify`, `synchronized`) that proves you understand the machinery, *then* the
**modern production** answer (`BlockingQueue`, `ConcurrentHashMap`, `AtomicInteger`,
`CompletableFuture`, `ExecutorService`) that proves you'd actually ship safe code. That one-two
punch — "here's how it works underneath, and here's what I'd really use" — is what makes an
interviewer trust you with a production system. 🚀

> **Remember the golden rule one last time:** Definition → Why → Code → Pitfall. Every. Single. Time.
