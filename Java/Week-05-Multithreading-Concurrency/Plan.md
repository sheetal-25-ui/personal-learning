# Week 5: Multithreading and Concurrency

## Why This Week Matters

Every modern application does multiple things at the same time. When you open Zomato, it loads
restaurant images, fetches your location, checks offers, and loads reviews ALL at the same time.
If it did these one-by-one, you would be staring at a blank screen for 30 seconds. This is
multithreading in action.

Microsoft and Anthropic LOVE asking multithreading questions because they reveal whether you
truly understand how programs execute, not just how to write syntax.

---

## 1. What is Multithreading? Why Do We Need It?

### What is it?
Multithreading means running multiple tasks within the same program at the same time. Each task
runs on its own "thread" — think of a thread as an independent worker inside your program.

### Real-life Analogy: Restaurant Waiters

Imagine a restaurant with only **1 waiter** (single-threaded):
- Customer A orders. Waiter goes to kitchen, waits, brings food.
- Customer B is waiting... and waiting... and waiting.
- Customer C walks out because it is taking too long.

Now imagine the same restaurant with **10 waiters** (multi-threaded):
- Waiter 1 takes Customer A's order.
- While Waiter 1 is in the kitchen, Waiter 2 takes Customer B's order.
- Waiter 3 serves Customer C.
- Everyone is happy. Restaurant makes more money.

**Another Analogy:** You are downloading a movie on your laptop while also playing a song on
Spotify. Both happen at the same time. The movie download is one thread, the music player is
another thread. Your CPU switches between them so fast that it FEELS like both run simultaneously.

### Why does it exist? What problem does it solve?
1. **Better performance** — Use all CPU cores instead of just one
2. **Better user experience** — UI does not freeze while heavy work happens in background
3. **Better resource utilization** — While one thread waits for network/disk, another thread can
   use the CPU

### Python Comparison
In Python, you have the `threading` module, BUT Python has the **GIL (Global Interpreter Lock)**
which means only one thread truly runs at a time. Python threads are useful for I/O tasks
(waiting for API calls, file reads) but NOT for CPU-heavy work.

Java has NO such limitation. Java threads run truly in parallel on multiple CPU cores. This is
why Java is the language of choice for high-performance backend systems at companies like
Flipkart and Swiggy.

---

## 2. Process vs Thread

### What is it?
A **process** is a running program — it has its own memory, its own resources, its own everything.
A **thread** is a lightweight worker INSIDE a process — threads share the same memory of the
process they belong to.

### Real-life Analogy
**Process** = A separate Zomato delivery person on a separate bike. Each has their own vehicle,
their own GPS, their own bag. They do not share anything.

**Thread** = Multiple chefs in the SAME kitchen. They share the same stove, same utensils, same
ingredients. They work together but need to coordinate so they do not bump into each other.

### Key Differences

| Feature | Process | Thread |
|---------|---------|--------|
| Memory | Has its own memory | Shares memory with other threads |
| Creation cost | Expensive (like hiring new employee) | Cheap (like giving existing employee another task) |
| Communication | Hard (IPC needed) | Easy (shared memory) |
| Crash impact | One process crash does not affect others | One thread crash can kill the entire process |
| Example | Opening Chrome AND Word | Multiple tabs in Chrome |

### How to see this on your computer
Open Task Manager (Windows) or Activity Monitor (Mac):
- Each row is a **process** (Chrome, VS Code, Spotify)
- Click on Chrome — you will see it uses multiple **threads** inside

### Python Comparison
```python
# Python — multiprocessing for true parallelism
from multiprocessing import Process  # separate process
from threading import Thread          # thread within same process
```

In Java, you almost always work with threads (not processes), because Java threads are real
OS-level threads with true parallelism.

---

## 3. Creating Threads — Two Ways

### Way 1: Extending the Thread Class

```java
// Step 1: Create a class that extends Thread
class MyThread extends Thread {

    // Step 2: Override the run() method — this is what the thread will DO
    @Override
    public void run() {
        for (int i = 1; i <= 5; i++) {
            System.out.println(Thread.currentThread().getName() + " - Count: " + i);
            try {
                Thread.sleep(500); // Sleep for 500ms (half a second)
            } catch (InterruptedException e) {
                System.out.println("Thread was interrupted!");
            }
        }
    }
}

public class ThreadDemo {
    public static void main(String[] args) {
        // Step 3: Create thread objects
        MyThread thread1 = new MyThread();
        MyThread thread2 = new MyThread();

        // Give them names so we can tell them apart
        thread1.setName("Waiter-1");
        thread2.setName("Waiter-2");

        // Step 4: Start the threads — this creates new threads of execution
        thread1.start();
        thread2.start();

        System.out.println("Main thread continues doing its own work...");
    }
}
```

**Output** (order will vary each time you run — that is the nature of threads!):
```
Main thread continues doing its own work...
Waiter-1 - Count: 1
Waiter-2 - Count: 1
Waiter-1 - Count: 2
Waiter-2 - Count: 2
...
```

### Way 2: Implementing Runnable Interface (PREFERRED)

```java
// Step 1: Create a class that implements Runnable
class MyTask implements Runnable {

    private String taskName;

    public MyTask(String taskName) {
        this.taskName = taskName;
    }

    // Step 2: Implement the run() method
    @Override
    public void run() {
        for (int i = 1; i <= 5; i++) {
            System.out.println(taskName + " - Step: " + i);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                System.out.println("Interrupted!");
            }
        }
    }
}

public class RunnableDemo {
    public static void main(String[] args) {
        // Step 3: Create Runnable objects
        MyTask task1 = new MyTask("Download Movie");
        MyTask task2 = new MyTask("Play Music");

        // Step 4: Pass Runnable to Thread constructor and start
        Thread thread1 = new Thread(task1);
        Thread thread2 = new Thread(task2);

        thread1.start();
        thread2.start();
    }
}
```

### WHY is Runnable Preferred Over Extending Thread?

This is an important interview question. Three reasons:

1. **Java does not allow multiple inheritance.** If your class extends Thread, it cannot extend
   anything else. But if it implements Runnable, it can still extend another class.

2. **Separation of concerns.** The Runnable is the TASK (what to do). The Thread is the WORKER
   (who does it). Keeping them separate is cleaner design.

3. **Reusability.** The same Runnable can be passed to a Thread, to a ThreadPool, to an
   ExecutorService — it is more flexible.

**Think of it this way:** Extending Thread is like saying "I AM a waiter." Implementing Runnable
is like saying "I HAVE a task that any waiter can do." The second approach is more flexible.

### Python Comparison
```python
# Python Way 1: Extending Thread
import threading

class MyThread(threading.Thread):
    def run(self):
        print(f"{self.name} running")

# Python Way 2: Passing target function (like Runnable)
def my_task():
    print("Task running")

t = threading.Thread(target=my_task)
t.start()
```

Java's Runnable is similar to Python's `target=function` approach.

### Using Lambda (Java 8+) — The Cleanest Way

```java
public class LambdaThreadDemo {
    public static void main(String[] args) {
        // Since Runnable has only one method (run), we can use a lambda
        Thread thread = new Thread(() -> {
            System.out.println("Hello from a lambda thread!");
        });
        thread.start();
    }
}
```

---

## 4. Thread Lifecycle

A thread goes through these states during its life:

```
                    +-----------+
                    |    NEW    |  (Thread object created, not started yet)
                    +-----------+
                         |
                      start()
                         |
                         v
                   +------------+
               +-->| RUNNABLE   |  (Ready to run, waiting for CPU time)
               |   +------------+
               |        |
               |    CPU assigned
               |        |
               |        v
               |   +------------+
               |   |  RUNNING   |  (Actually executing code)
               |   +------------+
               |     |       |
               |  sleep()  wait()
               |  join()   synchronized
               |     |       |
               |     v       v
               |  +--------------+
               |  |BLOCKED/WAITING|  (Paused, waiting for something)
               |  +--------------+
               |        |
               |   notify()/timeout
               |        |
               +--------+
                         |
                    run() finishes
                         |
                         v
                  +--------------+
                  |  TERMINATED  |  (Thread is done, cannot restart)
                  +--------------+
```

### States Explained

1. **NEW** — You created the thread object (`new Thread()`) but have not called `start()` yet.
   Like hiring a waiter but they have not started their shift.

2. **RUNNABLE** — You called `start()`. The thread is ready to run but waiting for the CPU to
   give it time. Like the waiter has arrived, uniform on, but waiting to be assigned a table.

3. **RUNNING** — The thread is actively executing its `run()` method. The waiter is serving
   customers.

4. **BLOCKED/WAITING** — The thread is alive but paused:
   - `sleep()` — "Take a 5 minute break"
   - `wait()` — "Wait until someone tells you to continue"
   - Waiting for a lock — "Wait until the kitchen is free"

5. **TERMINATED** — The `run()` method has finished. The thread is dead and CANNOT be restarted.
   The waiter's shift is over; they have gone home.

### Checking Thread State

```java
Thread t = new Thread(() -> {
    try { Thread.sleep(1000); } catch (InterruptedException e) {}
});

System.out.println(t.getState()); // NEW
t.start();
System.out.println(t.getState()); // RUNNABLE (or TIMED_WAITING if sleep started)
```

---

## 5. Important Thread Methods

### start() vs run() — EVERY INTERVIEWER ASKS THIS

```java
class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("Running in: " + Thread.currentThread().getName());
    }
}

public class StartVsRun {
    public static void main(String[] args) {
        MyThread t = new MyThread();

        // WRONG WAY: calling run() directly
        t.run();   // Output: "Running in: main"
        // This just calls the method normally in the main thread. NO new thread created!

        // RIGHT WAY: calling start()
        t.start(); // Output: "Running in: Thread-0"
        // This creates a NEW thread and runs run() in that new thread.
    }
}
```

**Key Difference:**
- `start()` — Creates a new thread, then calls `run()` in that new thread
- `run()` — Just a normal method call in the CURRENT thread. No new thread is created.

### sleep() — Pause the Thread

```java
// Pause current thread for 2 seconds
try {
    Thread.sleep(2000); // 2000 milliseconds = 2 seconds
} catch (InterruptedException e) {
    System.out.println("Someone woke me up!");
}
```

**Real-life:** Like telling a Zomato delivery person "wait 2 minutes outside, the food is not
ready yet."

### join() — Wait for Another Thread to Finish

```java
public class JoinDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread downloadThread = new Thread(() -> {
            System.out.println("Downloading file...");
            try { Thread.sleep(3000); } catch (InterruptedException e) {}
            System.out.println("Download complete!");
        });

        downloadThread.start();

        // Main thread WAITS here until downloadThread finishes
        downloadThread.join();

        // This line only runs AFTER download is complete
        System.out.println("Now processing the downloaded file...");
    }
}
```

**Real-life:** You ordered biryani on Swiggy. You cannot eat until the delivery arrives. You
"join" (wait for) the delivery thread to complete before you start eating.

### yield() — Give Other Threads a Chance

```java
Thread.yield(); // "I am done for now, let someone else run"
```

This is a HINT to the scheduler. It may or may not be honored. Like raising your hand in a
meeting and saying "I will let someone else speak" — the moderator might still ask you to
continue.

### interrupt() — Signal a Thread to Stop

```java
Thread t = new Thread(() -> {
    while (!Thread.currentThread().isInterrupted()) {
        System.out.println("Working...");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("I was interrupted! Cleaning up...");
            break; // Exit the loop
        }
    }
});

t.start();
Thread.sleep(3000);
t.interrupt(); // Signal the thread to stop
```

**Real-life:** Like a manager telling a Flipkart warehouse worker "stop packing, we have an
urgent task." The worker does not stop immediately — they finish their current box, then stop.

---

## 6. Thread Synchronization — The Race Condition Problem

### What is a Race Condition?

A race condition happens when two threads access shared data at the same time, and the result
depends on the ORDER in which they run.

### Real-life Analogy: The Bank Account Problem

You and your spouse both have cards for the SAME bank account with Rs. 10,000.

1. You check ATM: Balance is Rs. 10,000 ✓
2. Your spouse checks ATM: Balance is Rs. 10,000 ✓
3. You withdraw Rs. 8,000 (you think Rs. 2,000 will remain)
4. Your spouse withdraws Rs. 8,000 (they also think Rs. 2,000 will remain)
5. Bank account now has Rs. -6,000. Bank loses money!

This happened because both of you READ the balance BEFORE either of you WROTE the new balance.
That is a race condition.

### Code Example — The Problem

```java
class BankAccount {
    private int balance = 10000;

    // THIS IS NOT THREAD-SAFE!
    public void withdraw(int amount) {
        if (balance >= amount) {
            System.out.println(Thread.currentThread().getName()
                + " is withdrawing " + amount);

            // Simulate some processing time
            try { Thread.sleep(100); } catch (InterruptedException e) {}

            balance -= amount;
            System.out.println(Thread.currentThread().getName()
                + " completed. New balance: " + balance);
        } else {
            System.out.println(Thread.currentThread().getName()
                + " - Insufficient funds! Balance: " + balance);
        }
    }

    public int getBalance() { return balance; }
}

public class RaceConditionDemo {
    public static void main(String[] args) throws InterruptedException {
        BankAccount account = new BankAccount();

        Thread you = new Thread(() -> account.withdraw(8000), "You");
        Thread spouse = new Thread(() -> account.withdraw(8000), "Spouse");

        you.start();
        spouse.start();

        you.join();
        spouse.join();

        System.out.println("Final balance: " + account.getBalance());
        // Could be -6000! Both threads passed the if-check before either subtracted.
    }
}
```

### The Solution: synchronized Keyword

```java
class SafeBankAccount {
    private int balance = 10000;

    // The synchronized keyword means: ONLY ONE THREAD can execute this method at a time.
    // Other threads must WAIT in line.
    public synchronized void withdraw(int amount) {
        if (balance >= amount) {
            System.out.println(Thread.currentThread().getName()
                + " is withdrawing " + amount);
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            balance -= amount;
            System.out.println(Thread.currentThread().getName()
                + " completed. New balance: " + balance);
        } else {
            System.out.println(Thread.currentThread().getName()
                + " - Insufficient funds! Balance: " + balance);
        }
    }

    public int getBalance() { return balance; }
}
```

**Real-life analogy:** `synchronized` is like a lock on the ATM room. When you go in, the door
locks. Your spouse must wait outside until you finish. Only then can they enter.

### Synchronized Block (More Granular)

```java
class SafeBankAccountV2 {
    private int balance = 10000;
    private final Object lock = new Object(); // Lock object

    public void withdraw(int amount) {
        // Only THIS part is synchronized, not the entire method
        synchronized (lock) {
            if (balance >= amount) {
                balance -= amount;
                System.out.println("Withdrew " + amount + ". Balance: " + balance);
            }
        }
        // Code here runs without holding the lock — other threads can proceed
        System.out.println("Transaction logged."); // Does not need synchronization
    }
}
```

**Why use a synchronized block instead of a synchronized method?**
Synchronized methods lock the ENTIRE method. If only 2 lines out of 50 need protection, you are
making 48 lines unnecessarily slow. A synchronized block locks only what needs protection.

### Python Comparison
```python
import threading

lock = threading.Lock()

def withdraw(amount):
    with lock:  # Python's context manager — similar to synchronized block
        if balance >= amount:
            balance -= amount
```

### Why Synchronization Slows Things Down

Synchronization is a trade-off:
- **Without sync:** Fast but WRONG (race conditions)
- **With sync:** Slower but CORRECT (threads wait in line)

The goal is to synchronize the MINIMUM amount of code necessary.

---

## 7. Locks — ReentrantLock and ReadWriteLock

### What is it?
Locks are a more flexible alternative to `synchronized`. They give you more control over when to
lock and unlock.

### ReentrantLock

```java
import java.util.concurrent.locks.ReentrantLock;

class TicketBooking {
    private int availableSeats = 10;
    private final ReentrantLock lock = new ReentrantLock();

    public void bookSeat(String passenger) {
        lock.lock(); // Acquire the lock
        try {
            if (availableSeats > 0) {
                System.out.println(passenger + " booked seat " + availableSeats);
                availableSeats--;
            } else {
                System.out.println(passenger + " - No seats available!");
            }
        } finally {
            lock.unlock(); // ALWAYS unlock in finally block!
        }
    }
}
```

**CRITICAL:** Always put `unlock()` in a `finally` block. If an exception happens between
`lock()` and `unlock()`, and you do not have `finally`, the lock is NEVER released. Every other
thread will wait FOREVER. This is like someone passing out inside the ATM room with the door
locked — nobody else can ever use it.

### When to Use Lock Over Synchronized

| Feature | synchronized | Lock |
|---------|-------------|------|
| Simplicity | Simpler syntax | More code |
| Try-lock | No | Yes (`tryLock()`) |
| Timed wait | No | Yes (`tryLock(5, TimeUnit.SECONDS)`) |
| Interruptible | No | Yes (`lockInterruptibly()`) |
| Fair ordering | No | Yes (`new ReentrantLock(true)`) |
| Multiple conditions | No | Yes (`lock.newCondition()`) |

### tryLock() — Non-Blocking Lock Attempt

```java
if (lock.tryLock()) {
    try {
        // Do critical work
    } finally {
        lock.unlock();
    }
} else {
    System.out.println("Could not acquire lock, doing something else...");
}
```

**Real-life:** Like checking if the ATM is free. If it is, you go in. If someone is inside, you
go to the next ATM instead of waiting.

### ReadWriteLock — Multiple Readers, Single Writer

```java
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class FlipkartProductCatalog {
    private Map<String, Double> prices = new HashMap<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    // Multiple threads can read at the same time
    public double getPrice(String product) {
        rwLock.readLock().lock();
        try {
            return prices.getOrDefault(product, 0.0);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    // Only one thread can write at a time (and no readers allowed during write)
    public void updatePrice(String product, double newPrice) {
        rwLock.writeLock().lock();
        try {
            prices.put(product, newPrice);
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
```

**Real-life:** Think of a Flipkart product page. Thousands of people can VIEW the price at the
same time (read lock allows multiple readers). But when an admin UPDATES the price, everyone
must see the update atomically — so writing blocks all readers temporarily.

---

## 8. Deadlock

### What is it?
A deadlock is when two or more threads are waiting for each other to release locks, and NONE of
them can proceed. The program freezes forever.

### Real-life Analogy
Two people meet in a narrow corridor. Person A is going right, Person B is going left. Both stop.
Person A says: "You move first." Person B says: "No, YOU move first." Neither moves. They stand
there forever. That is a deadlock.

**Indian context:** Two auto-rickshaws meeting in a narrow gali (lane). Neither can reverse,
neither can go forward. Both are waiting for the other to move. Traffic behind them builds up.
Everything is stuck.

### Code Example — Creating a Deadlock

```java
public class DeadlockDemo {
    private static final Object lockA = new Object();
    private static final Object lockB = new Object();

    public static void main(String[] args) {
        // Thread 1: Locks A first, then tries to lock B
        Thread thread1 = new Thread(() -> {
            synchronized (lockA) {
                System.out.println("Thread 1: Holding Lock A...");
                try { Thread.sleep(100); } catch (InterruptedException e) {}

                System.out.println("Thread 1: Waiting for Lock B...");
                synchronized (lockB) {
                    System.out.println("Thread 1: Holding Lock A and Lock B");
                }
            }
        });

        // Thread 2: Locks B first, then tries to lock A
        Thread thread2 = new Thread(() -> {
            synchronized (lockB) {
                System.out.println("Thread 2: Holding Lock B...");
                try { Thread.sleep(100); } catch (InterruptedException e) {}

                System.out.println("Thread 2: Waiting for Lock A...");
                synchronized (lockA) {
                    System.out.println("Thread 2: Holding Lock B and Lock A");
                }
            }
        });

        thread1.start();
        thread2.start();

        // This program will HANG forever. Thread 1 has A, needs B.
        // Thread 2 has B, needs A. Neither can proceed.
    }
}
```

### Four Conditions for Deadlock (All Must Be True)

1. **Mutual Exclusion** — A resource can only be held by one thread at a time
2. **Hold and Wait** — A thread holds one resource while waiting for another
3. **No Preemption** — Resources cannot be forcibly taken away
4. **Circular Wait** — Thread 1 waits for Thread 2, Thread 2 waits for Thread 1

### How to Prevent Deadlock

Break ANY ONE of the four conditions:

**Best approach: Always acquire locks in the SAME ORDER.**

```java
// FIXED: Both threads lock A first, then B. No circular wait possible.
Thread thread1 = new Thread(() -> {
    synchronized (lockA) {
        synchronized (lockB) {
            System.out.println("Thread 1 has both locks");
        }
    }
});

Thread thread2 = new Thread(() -> {
    synchronized (lockA) { // Same order as thread1!
        synchronized (lockB) {
            System.out.println("Thread 2 has both locks");
        }
    }
});
```

**Other approaches:**
- Use `tryLock()` with a timeout — if you cannot get the lock in 5 seconds, give up
- Avoid nested locks whenever possible
- Use higher-level concurrency tools (ExecutorService, BlockingQueue) that handle locking for you

---

## 9. Producer-Consumer Problem — Classic Interview Question

### What is it?
One thread PRODUCES data (adds to a shared buffer), another thread CONSUMES data (removes from
the buffer). They must coordinate so:
- Producer does not add when buffer is FULL
- Consumer does not remove when buffer is EMPTY

### Real-life Analogy
**Zomato kitchen and delivery:**
- **Producer** = Chef cooking food and placing it on the counter
- **Consumer** = Delivery person picking food from the counter
- **Buffer** = The counter (limited space)
- If the counter is full, the chef must WAIT
- If the counter is empty, the delivery person must WAIT

### Solution Using wait() and notify()

```java
import java.util.LinkedList;
import java.util.Queue;

class SharedBuffer {
    private Queue<Integer> buffer = new LinkedList<>();
    private int capacity;

    public SharedBuffer(int capacity) {
        this.capacity = capacity;
    }

    // Producer calls this
    public synchronized void produce(int item) throws InterruptedException {
        // If buffer is full, WAIT until consumer removes something
        while (buffer.size() == capacity) {
            System.out.println("Buffer full! Producer waiting...");
            wait(); // Release lock and wait
        }

        buffer.add(item);
        System.out.println("Produced: " + item + " | Buffer size: " + buffer.size());

        // Notify waiting consumers that there is data available
        notifyAll();
    }

    // Consumer calls this
    public synchronized int consume() throws InterruptedException {
        // If buffer is empty, WAIT until producer adds something
        while (buffer.isEmpty()) {
            System.out.println("Buffer empty! Consumer waiting...");
            wait(); // Release lock and wait
        }

        int item = buffer.poll();
        System.out.println("Consumed: " + item + " | Buffer size: " + buffer.size());

        // Notify waiting producers that there is space available
        notifyAll();
        return item;
    }
}

public class ProducerConsumerDemo {
    public static void main(String[] args) {
        SharedBuffer buffer = new SharedBuffer(5);

        // Producer thread
        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                try {
                    buffer.produce(i);
                    Thread.sleep(200);
                } catch (InterruptedException e) {}
            }
        });

        // Consumer thread
        Thread consumer = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                try {
                    buffer.consume();
                    Thread.sleep(500); // Consumer is slower than producer
                } catch (InterruptedException e) {}
            }
        });

        producer.start();
        consumer.start();
    }
}
```

### Key Points About wait(), notify(), notifyAll()

- `wait()` — The thread releases the lock and goes to sleep. It will wake up when another thread
  calls `notify()` or `notifyAll()` on the same object.
- `notify()` — Wakes up ONE waiting thread (which one is unpredictable)
- `notifyAll()` — Wakes up ALL waiting threads (safer, recommended)
- These methods MUST be called inside a `synchronized` block/method
- Always use `while` (not `if`) around `wait()` — a thread can be woken up for reasons other
  than your condition being true (spurious wakeups)

### Easier Modern Way: BlockingQueue

```java
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class EasyProducerConsumer {
    public static void main(String[] args) {
        // BlockingQueue handles ALL the synchronization for you!
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);

        // Producer
        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                try {
                    queue.put(i); // Blocks if queue is full
                    System.out.println("Produced: " + i);
                } catch (InterruptedException e) {}
            }
        });

        // Consumer
        Thread consumer = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                try {
                    int item = queue.take(); // Blocks if queue is empty
                    System.out.println("Consumed: " + item);
                } catch (InterruptedException e) {}
            }
        });

        producer.start();
        consumer.start();
    }
}
```

**BlockingQueue** is the production way to solve this. It handles all the wait/notify logic
internally. In an interview, show the wait/notify solution first (proves understanding), then
mention BlockingQueue as the practical production approach.

---

## 10. Thread Pool and ExecutorService

### What is it?
A thread pool is a group of pre-created threads that sit idle, waiting for tasks. When a task
arrives, an idle thread picks it up. When done, the thread goes back to the pool instead of
dying.

### Why Thread Pools?
Creating a new thread is EXPENSIVE (takes time and memory). Imagine if Flipkart created a new
thread for every customer request during a Big Billion Days sale — millions of threads would
crash the server.

**Real-life analogy:** Think of Ola drivers. Ola does not hire a new driver for every ride
request. They have a POOL of drivers. When a ride comes in, an available driver is assigned. When
the ride ends, the driver goes back to the pool waiting for the next ride.

### Using ExecutorService

```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolDemo {
    public static void main(String[] args) {
        // Create a pool of 3 threads
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Submit 10 tasks — but only 3 run at a time
        for (int i = 1; i <= 10; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("Task " + taskId + " running on "
                    + Thread.currentThread().getName());
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
                System.out.println("Task " + taskId + " complete");
            });
        }

        // IMPORTANT: Shut down the executor when done
        executor.shutdown(); // No new tasks accepted, finishes existing ones
    }
}
```

### Types of Thread Pools

```java
// 1. Fixed Thread Pool — exactly N threads
ExecutorService fixed = Executors.newFixedThreadPool(10);
// Use when: You know the workload and want to limit resource usage
// Like: IRCTC allowing only 10 booking agents at a time

// 2. Cached Thread Pool — creates threads as needed, reuses idle ones
ExecutorService cached = Executors.newCachedThreadPool();
// Use when: Many short-lived tasks with unpredictable volume
// Like: Zomato handling variable dinner rush orders

// 3. Single Thread Executor — only 1 thread
ExecutorService single = Executors.newSingleThreadExecutor();
// Use when: Tasks must run sequentially (one after another)
// Like: Writing to a log file (must be in order)

// 4. Scheduled Thread Pool — run tasks at fixed intervals
ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(5);
scheduled.scheduleAtFixedRate(() -> {
    System.out.println("Checking for new orders...");
}, 0, 5, TimeUnit.SECONDS); // Run every 5 seconds
// Like: Swiggy checking for new orders every few seconds
```

### Getting Results Back: Callable and Future

`Runnable.run()` returns void. What if you need a RESULT?

```java
import java.util.concurrent.*;

public class CallableDemo {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Callable is like Runnable, but it RETURNS a value
        Callable<Integer> priceChecker = () -> {
            System.out.println("Checking Flipkart price...");
            Thread.sleep(2000);
            return 49999; // The price
        };

        // Future holds the result that will be available LATER
        Future<Integer> futurePrice = executor.submit(priceChecker);

        System.out.println("Doing other things while price is being checked...");

        // get() BLOCKS until the result is ready
        Integer price = futurePrice.get(); // Waits here until Callable finishes
        System.out.println("Price is: Rs. " + price);

        executor.shutdown();
    }
}
```

**Real-life:** `Future` is like a receipt from a restaurant. You place an order (submit Callable),
get a receipt (Future). You can do other things. When food is ready, you show the receipt and
get your food (`future.get()`).

---

## 11. CompletableFuture — Modern Async Programming

### What is it?
CompletableFuture (Java 8+) is a powerful way to write asynchronous code. It lets you chain
operations, handle errors, and combine results from multiple async tasks.

### Why CompletableFuture Over Future?
`Future.get()` BLOCKS the thread. CompletableFuture lets you say "when the result is ready,
do THIS" without blocking.

```java
import java.util.concurrent.CompletableFuture;

public class CompletableFutureDemo {
    public static void main(String[] args) throws Exception {

        // Run a task asynchronously
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("Fetching restaurant data from Zomato...");
            try { Thread.sleep(2000); } catch (InterruptedException e) {}
            return "Biryani Palace - 4.5 stars";
        });

        // Chain: when result is ready, transform it
        CompletableFuture<String> result = future
            .thenApply(restaurant -> restaurant.toUpperCase())      // Transform
            .thenApply(restaurant -> "*** " + restaurant + " ***"); // Transform again

        // This runs WITHOUT blocking
        System.out.println("Doing other things while data loads...");

        // Get the final result (blocks only if not yet complete)
        System.out.println(result.get());
        // Output: *** BIRYANI PALACE - 4.5 STARS ***
    }
}
```

### Combining Multiple Async Tasks

```java
// Scenario: Search for the best price across Flipkart, Amazon, and Croma simultaneously

CompletableFuture<Integer> flipkart = CompletableFuture.supplyAsync(() -> {
    try { Thread.sleep(1500); } catch (InterruptedException e) {}
    return 45000; // Flipkart price
});

CompletableFuture<Integer> amazon = CompletableFuture.supplyAsync(() -> {
    try { Thread.sleep(2000); } catch (InterruptedException e) {}
    return 43000; // Amazon price
});

CompletableFuture<Integer> croma = CompletableFuture.supplyAsync(() -> {
    try { Thread.sleep(1000); } catch (InterruptedException e) {}
    return 46000; // Croma price
});

// Wait for ALL to complete, then find the minimum
CompletableFuture<Void> allDone = CompletableFuture.allOf(flipkart, amazon, croma);
allDone.thenRun(() -> {
    try {
        int best = Math.min(flipkart.get(), Math.min(amazon.get(), croma.get()));
        System.out.println("Best price: Rs. " + best);
    } catch (Exception e) {}
});

Thread.sleep(3000); // Wait for demo to complete
```

### Python Comparison
```python
# Python equivalent using asyncio
import asyncio

async def fetch_price(site, delay, price):
    await asyncio.sleep(delay)
    return price

async def main():
    results = await asyncio.gather(
        fetch_price("Flipkart", 1.5, 45000),
        fetch_price("Amazon", 2.0, 43000),
        fetch_price("Croma", 1.0, 46000)
    )
    print(f"Best price: Rs. {min(results)}")

asyncio.run(main())
```

---

## 12. volatile Keyword

### What is it?
`volatile` tells Java: "This variable might be changed by other threads. Always read its latest
value from main memory, not from a thread's local cache."

### When to Use
When one thread writes a variable and another thread reads it, but you do not need full
synchronization (no compound operations like check-then-update).

```java
class ServerStatus {
    // Without volatile, thread might read a cached (stale) value
    private volatile boolean running = true;

    public void stop() {
        running = false; // Written by main thread
    }

    public void serve() {
        while (running) { // Read by worker thread
            System.out.println("Serving requests...");
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
        }
        System.out.println("Server stopped.");
    }
}
```

### volatile vs synchronized

| Feature | volatile | synchronized |
|---------|----------|-------------|
| Protects | Single variable read/write | Block of code |
| Atomicity | Only for read/write of the variable | Full atomicity for the block |
| Use case | Flag variables (boolean on/off) | Compound operations (check + update) |
| Performance | Faster | Slower |

**Rule of thumb:** If you are doing `count++` (read + modify + write), `volatile` is NOT enough.
Use `synchronized` or `AtomicInteger`.

---

## 13. Atomic Classes

### What is it?
Atomic classes provide thread-safe operations on single variables WITHOUT locks. They use
CPU-level instructions (Compare-And-Swap) which are much faster than synchronized.

```java
import java.util.concurrent.atomic.AtomicInteger;

class VisitorCounter {
    // AtomicInteger is thread-safe without any locks
    private AtomicInteger count = new AtomicInteger(0);

    public void visit() {
        count.incrementAndGet(); // Atomic: read + increment + write in one operation
    }

    public int getCount() {
        return count.get();
    }
}

public class AtomicDemo {
    public static void main(String[] args) throws InterruptedException {
        VisitorCounter counter = new VisitorCounter();

        // 100 threads, each incrementing 1000 times
        Thread[] threads = new Thread[100];
        for (int i = 0; i < 100; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    counter.visit();
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) t.join();

        System.out.println("Total visits: " + counter.getCount());
        // ALWAYS prints 100000. Without AtomicInteger, the count would be wrong.
    }
}
```

### Common Atomic Classes
- `AtomicInteger` — thread-safe int
- `AtomicLong` — thread-safe long
- `AtomicBoolean` — thread-safe boolean
- `AtomicReference<T>` — thread-safe reference to any object

---

## 14. Common Concurrency Interview Questions

### Q1: What is the difference between Runnable and Callable?

| Feature | Runnable | Callable |
|---------|----------|----------|
| Return value | void (nothing) | Returns a value |
| Exception | Cannot throw checked exceptions | Can throw checked exceptions |
| Method | run() | call() |
| Usage | Thread, ExecutorService | ExecutorService only |

### Q2: What is a ThreadLocal variable?

A `ThreadLocal` gives each thread its own copy of a variable. Used for per-thread context
like database connections or user sessions.

```java
ThreadLocal<String> userContext = new ThreadLocal<>();

// In thread 1
userContext.set("User-A");
System.out.println(userContext.get()); // "User-A"

// In thread 2 (different thread)
userContext.set("User-B");
System.out.println(userContext.get()); // "User-B"
// Thread 1's value is NOT affected!
```

### Q3: What is the difference between sleep() and wait()?

| Feature | sleep() | wait() |
|---------|---------|--------|
| Belongs to | Thread class | Object class |
| Releases lock? | NO | YES |
| Called from | Anywhere | Only inside synchronized |
| Wake up | After timeout | notify()/notifyAll() or timeout |
| Purpose | Pause execution | Inter-thread communication |

---

## Common Mistakes Beginners Make

1. **Calling run() instead of start()** — This is the number one mistake. run() does NOT create
   a new thread.

2. **Not using finally with Lock.unlock()** — If an exception occurs, the lock is never released.
   Always use try-finally.

3. **Using if instead of while with wait()** — Spurious wakeups can happen. Always use while.

4. **Synchronizing on a non-final object** — If the reference changes, different threads may
   synchronize on different objects (no synchronization at all!).

5. **Not calling executor.shutdown()** — Your program will never terminate because the thread
   pool threads keep running.

6. **Over-synchronizing** — Making everything synchronized kills performance. Only synchronize
   what needs to be shared.

---

## Practice Exercises

### Exercise 1: Print Numbers Alternately
Create two threads. Thread-1 prints odd numbers (1, 3, 5...), Thread-2 prints even numbers
(2, 4, 6...). Output should be: 1, 2, 3, 4, 5, 6...

### Exercise 2: IRCTC Ticket Booking Simulator
Create a class `TrainBooking` with 5 available seats. Create 10 threads (passengers) that try
to book seats simultaneously. Ensure no two passengers get the same seat and no overbooking
happens.

### Exercise 3: Parallel Price Comparator
Use CompletableFuture to fetch prices from 4 different "websites" (simulate with delays).
Find the lowest price and the website name. Display results as each site responds (do not wait
for all to finish before showing anything).

### Exercise 4: Thread-Safe Counter
Implement a counter that is incremented by 50 threads simultaneously, 10000 times each.
Verify the final count is exactly 500000. Implement three versions: synchronized, Lock, and
AtomicInteger. Compare correctness.

### Exercise 5: Dining Philosophers
Implement the classic Dining Philosophers problem with 5 philosophers. First create a version
that can deadlock, then fix it using ordered lock acquisition.
