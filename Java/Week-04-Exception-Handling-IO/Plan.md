# Week 4: Exception Handling and File I/O

---

## 1. What are Exceptions?

### What is it?
An exception is an unexpected event that happens during program execution. Your code is running fine, and then suddenly something goes wrong — a file doesn't exist, a number is divided by zero, a network call times out. Java's exception handling lets you deal with these problems gracefully instead of crashing.

### Real-life analogy
Imagine you order Biryani from Zomato:
- **Happy path:** You order → Restaurant confirms → Food is prepared → Delivery partner picks up → You receive food. Everything works!
- **Exception scenarios:**
  - Restaurant is closed → `RestaurantClosedException`
  - Item is out of stock → `ItemNotAvailableException`
  - Payment fails → `PaymentFailedException`
  - Delivery partner cancels → `DeliveryPartnerNotFoundException`
  - Wrong address → `InvalidAddressException`

Each of these is an "exception" — something unexpected that must be handled. Without exception handling, your app would just crash with a cryptic error.

### Python comparison — the concept is the same
```python
# Python
try:
    result = 10 / 0
except ZeroDivisionError:
    print("Cannot divide by zero")
```

```java
// Java — same concept, different syntax
try {
    int result = 10 / 0;
} catch (ArithmeticException e) {
    System.out.println("Cannot divide by zero");
}
```

The key difference: Java has **checked exceptions** that FORCE you to handle them. Python has no such concept — all exceptions are optional to catch.

---

## 2. Exception Hierarchy — The Family Tree

### The complete picture
```
                    Throwable
                   /         \
                Error        Exception
               /    \        /        \
   StackOverflow  OutOfMemory   IOException    RuntimeException
   Error          Error         SQLException   /      |       \
                                FileNotFound  NullPointer  ArrayIndexOut
                                Exception     Exception    OfBounds
                                                           Exception
```

### Throwable — the root
Everything that can be "thrown" extends Throwable. It has two children:

### Error — "The house is on fire"
Errors are serious problems that your code CANNOT and SHOULD NOT try to handle:
- `StackOverflowError` — infinite recursion, call stack is full
- `OutOfMemoryError` — JVM has no more memory
- `VirtualMachineError` — the JVM itself is broken

**Real-life analogy:** Your kitchen is on fire. You don't try to cook in it — you evacuate. Similarly, you don't "catch" an Error — you fix the bug that caused it.

### Exception — "The restaurant is closed, let's find another one"
Exceptions are problems your code CAN handle. They split into two types:

### Checked exceptions (compile-time) — "You MUST plan for this"
These are exceptions that Java FORCES you to handle at compile time. If you don't handle them, your code won't even compile.

```java
// This will NOT compile without try-catch or throws:
FileReader reader = new FileReader("menu.txt");  // COMPILE ERROR!
// Java says: "What if the file doesn't exist? You MUST handle FileNotFoundException!"

// Fix option 1: try-catch
try {
    FileReader reader = new FileReader("menu.txt");
} catch (FileNotFoundException e) {
    System.out.println("File not found: " + e.getMessage());
}

// Fix option 2: declare with throws
public void readMenu() throws FileNotFoundException {
    FileReader reader = new FileReader("menu.txt");
    // "I'm not handling it — my caller must handle it"
}
```

**Common checked exceptions:**
- `IOException` — file operations, network operations
- `FileNotFoundException` — file doesn't exist
- `SQLException` — database operations
- `ClassNotFoundException` — class not found at runtime

### Unchecked exceptions (runtime) — "You should be careful, but we won't force you"
These happen because of programming mistakes. Java doesn't force you to handle them because they shouldn't happen if your code is correct.

```java
// These compile fine but crash at runtime if the bug is there:
int[] arr = {1, 2, 3};
arr[5] = 10;  // ArrayIndexOutOfBoundsException — your logic is wrong

String s = null;
s.length();   // NullPointerException — you forgot to check for null

int result = 10 / 0;  // ArithmeticException — division by zero
```

**Common unchecked exceptions:**
- `NullPointerException` — calling a method on null
- `ArrayIndexOutOfBoundsException` — accessing invalid array index
- `ClassCastException` — casting to the wrong type
- `IllegalArgumentException` — passing invalid argument to a method
- `NumberFormatException` — parsing "abc" as a number

### Checked vs Unchecked — The Debate

| Feature | Checked | Unchecked |
|---------|---------|-----------|
| When detected | Compile time | Runtime |
| Must handle? | YES (or code won't compile) | NO (but should) |
| Extends | Exception (not RuntimeException) | RuntimeException |
| Caused by | External factors (file, network, DB) | Programming bugs |
| Examples | IOException, SQLException | NullPointerException, ClassCastException |

### Python comparison — Python only has unchecked exceptions
```python
# Python NEVER forces you to handle exceptions
open("nonexistent.txt")  # Will crash, but Python doesn't force try-except at compile time

# You CHOOSE to handle them
try:
    open("nonexistent.txt")
except FileNotFoundError:
    print("File not found")
```

**Why Java forces checked exceptions:**
- It makes you think about what can go wrong BEFORE it happens
- Your code becomes more robust — you can't accidentally ignore a potential failure
- Critics say: it makes code verbose and cluttered with try-catch blocks

**The industry consensus:** Many modern Java frameworks (Spring) convert checked exceptions to unchecked. The debate continues, but understanding both types is essential for interviews.

---

## 3. try-catch-finally — The Core Mechanism

### Basic syntax
```java
try {
    // Code that might throw an exception
    int result = 10 / 0;
} catch (ArithmeticException e) {
    // Handle the exception
    System.out.println("Error: " + e.getMessage());
} finally {
    // Always runs — whether exception occurred or not
    System.out.println("This always runs");
}
```

### Real-life analogy
Think of cooking:
- `try` — You're cooking biryani. Things might go wrong.
- `catch` — If the rice burns, you switch to ordering from Swiggy.
- `finally` — Whether cooking succeeded or failed, you ALWAYS clean the kitchen.

### Multiple catch blocks (order matters!)
```java
try {
    String[] names = {"Sheetal", "Priya"};
    System.out.println(names[5]);  // ArrayIndexOutOfBoundsException
    int result = Integer.parseInt("abc");  // NumberFormatException
} catch (ArrayIndexOutOfBoundsException e) {
    System.out.println("Invalid index: " + e.getMessage());
} catch (NumberFormatException e) {
    System.out.println("Invalid number: " + e.getMessage());
} catch (Exception e) {
    // Catch-all for any other exception
    System.out.println("Something went wrong: " + e.getMessage());
}
```

**IMPORTANT: Order from MOST specific to LEAST specific!**
```java
// WRONG — won't compile!
try { ... }
catch (Exception e) { ... }           // This catches EVERYTHING
catch (ArithmeticException e) { ... }  // This will never be reached!

// RIGHT — specific first, general last
try { ... }
catch (ArithmeticException e) { ... }  // Specific
catch (Exception e) { ... }            // General catch-all
```

### Multi-catch (Java 7+)
```java
// Instead of writing separate catch blocks with the same handling:
try {
    // risky code
} catch (IOException | SQLException e) {  // Catch either type
    System.out.println("I/O or database error: " + e.getMessage());
}
```

### The finally block — guaranteed execution
```java
FileReader reader = null;
try {
    reader = new FileReader("data.txt");
    // Read file...
} catch (FileNotFoundException e) {
    System.out.println("File not found");
} finally {
    // Always runs — even if an exception occurred
    // Perfect for cleanup: closing files, database connections, etc.
    if (reader != null) {
        try {
            reader.close();
        } catch (IOException e) {
            System.out.println("Error closing file");
        }
    }
}
```

Notice how ugly that `finally` block is? You need ANOTHER try-catch just to close the file! That's why `try-with-resources` was invented...

### Python comparison
```python
# Python try-except-finally
try:
    result = 10 / 0
except ZeroDivisionError as e:
    print(f"Error: {e}")
finally:
    print("Always runs")

# Python multi-except
try:
    pass
except (ValueError, TypeError) as e:
    print(f"Error: {e}")
```

### Common mistakes
1. **Empty catch blocks** — `catch (Exception e) {}` — the exception is silently swallowed. You'll never know something went wrong! At minimum, log the error.
2. **Catching Exception too broadly** — `catch (Exception e)` catches EVERYTHING, including bugs you want to see. Be specific.
3. **Not using finally for cleanup** — resources (files, connections) must be closed. Use finally or try-with-resources.
4. **Wrong catch order** — putting a parent exception before a child exception.

### Practice exercises
1. Write a program that takes two numbers as input and divides them. Handle: ArithmeticException (divide by zero), InputMismatchException (non-numeric input), and any other exception.
2. Write a program that accesses an array element at a user-specified index. Handle ArrayIndexOutOfBoundsException gracefully.
3. Demonstrate what happens when you DON'T have a finally block and an exception occurs while a file is open.

---

## 4. try-with-resources — The Modern Way

### What is it?
try-with-resources automatically closes resources (files, connections, etc.) when the block finishes, whether normally or with an exception. No more messy finally blocks!

### Real-life analogy
Think of a hotel room. When you check out (leave the try block), the room (resource) is automatically cleaned up. You don't need to call housekeeping yourself.

### The code
```java
// OLD way (messy):
BufferedReader reader = null;
try {
    reader = new BufferedReader(new FileReader("data.txt"));
    String line = reader.readLine();
    System.out.println(line);
} catch (IOException e) {
    System.out.println("Error: " + e.getMessage());
} finally {
    if (reader != null) {
        try {
            reader.close();
        } catch (IOException e) {
            System.out.println("Error closing");
        }
    }
}

// NEW way (clean):
try (BufferedReader reader = new BufferedReader(new FileReader("data.txt"))) {
    String line = reader.readLine();
    System.out.println(line);
} catch (IOException e) {
    System.out.println("Error: " + e.getMessage());
}
// reader is automatically closed here — no finally needed!
```

### Multiple resources
```java
try (
    FileReader fr = new FileReader("input.txt");
    BufferedReader br = new BufferedReader(fr);
    FileWriter fw = new FileWriter("output.txt");
    BufferedWriter bw = new BufferedWriter(fw)
) {
    String line;
    while ((line = br.readLine()) != null) {
        bw.write(line);
        bw.newLine();
    }
}
// ALL four resources are automatically closed, in reverse order!
```

### AutoCloseable interface
Any class that implements `AutoCloseable` can be used in try-with-resources:
```java
public class DatabaseConnection implements AutoCloseable {
    public DatabaseConnection() {
        System.out.println("Connection opened");
    }

    public void query(String sql) {
        System.out.println("Executing: " + sql);
    }

    @Override
    public void close() {
        System.out.println("Connection closed");  // Called automatically!
    }
}

try (DatabaseConnection db = new DatabaseConnection()) {
    db.query("SELECT * FROM users");
}
// Output:
// Connection opened
// Executing: SELECT * FROM users
// Connection closed   ← automatic!
```

### Python comparison
```python
# Python's 'with' statement — same concept!
with open("data.txt", "r") as file:
    content = file.read()
# File is automatically closed here

# Python doesn't need to catch exceptions in the with block
# Java's try-with-resources combines 'with' + 'try-catch'
```

---

## 5. throw vs throws

### throw — Manually create and throw an exception
```java
public void setAge(int age) {
    if (age < 0 || age > 150) {
        throw new IllegalArgumentException("Age must be between 0 and 150, got: " + age);
        // This creates an exception object and throws it immediately
    }
    this.age = age;
}

// Usage:
try {
    setAge(-5);
} catch (IllegalArgumentException e) {
    System.out.println(e.getMessage());  // "Age must be between 0 and 150, got: -5"
}
```

### throws — Declare that a method MIGHT throw an exception
```java
// This method doesn't handle the exception — it passes it to the caller
public String readFile(String filename) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    return reader.readLine();
}

// The CALLER must handle it:
try {
    String content = readFile("menu.txt");
} catch (IOException e) {
    System.out.println("Could not read file: " + e.getMessage());
}
```

### Real-life analogy
- **throw** = You find a cockroach in your food and you THROW it to the waiter's attention
- **throws** = The menu says "May contain nuts" — it DECLARES a potential problem. The customer (caller) must decide what to do.

### Python comparison
```python
# Python 'raise' = Java 'throw'
def set_age(age):
    if age < 0:
        raise ValueError(f"Age must be positive, got: {age}")

# Python has NO equivalent of 'throws'
# You never declare what exceptions a function might raise
# This is one of the big philosophical differences
```

| Feature | throw | throws |
|---------|-------|--------|
| What it does | Actually creates and throws an exception | Declares a method might throw an exception |
| Where used | Inside a method body | In the method signature |
| How many? | One exception at a time | Multiple: `throws IOException, SQLException` |
| Required for | Manually signaling an error | Checked exceptions (compiler forces it) |

---

## 6. Custom Exceptions

### When to create your own?
When built-in exceptions don't describe your specific error well enough. For example, `InsufficientBalanceException` is much clearer than `RuntimeException("not enough money")`.

### How to create custom exceptions
```java
// Custom checked exception (extends Exception)
public class InsufficientBalanceException extends Exception {
    private double currentBalance;
    private double requestedAmount;

    public InsufficientBalanceException(double currentBalance, double requestedAmount) {
        super("Insufficient balance: current=" + currentBalance + ", requested=" + requestedAmount);
        this.currentBalance = currentBalance;
        this.requestedAmount = requestedAmount;
    }

    public double getDeficit() {
        return requestedAmount - currentBalance;
    }
}

// Custom unchecked exception (extends RuntimeException)
public class InvalidOrderException extends RuntimeException {
    public InvalidOrderException(String message) {
        super(message);
    }
}
```

### Using custom exceptions
```java
public class PaytmWallet {
    private double balance;

    public PaytmWallet(double balance) {
        this.balance = balance;
    }

    // Checked exception — caller MUST handle it
    public void withdraw(double amount) throws InsufficientBalanceException {
        if (amount > balance) {
            throw new InsufficientBalanceException(balance, amount);
        }
        balance -= amount;
        System.out.println("Withdrawn: Rs " + amount + ". New balance: Rs " + balance);
    }
}

// Usage:
PaytmWallet wallet = new PaytmWallet(500);
try {
    wallet.withdraw(800);
} catch (InsufficientBalanceException e) {
    System.out.println(e.getMessage());
    System.out.println("You need Rs " + e.getDeficit() + " more");
}
```

### When to use checked vs unchecked custom exceptions

| Type | Extend | Use when |
|------|--------|----------|
| Checked | `Exception` | The caller CAN reasonably recover (retry, use a default, ask user for different input) |
| Unchecked | `RuntimeException` | It's a programming error that should have been prevented (invalid state, null argument, broken invariant) |

### Python comparison
```python
# Python custom exception
class InsufficientBalanceError(Exception):
    def __init__(self, balance, amount):
        super().__init__(f"Balance: {balance}, Requested: {amount}")
        self.deficit = amount - balance

# Usage
try:
    raise InsufficientBalanceError(500, 800)
except InsufficientBalanceError as e:
    print(e)
    print(f"Need Rs {e.deficit} more")
```

### Practice exercises
1. Create a `InvalidPINException` for an ATM system. It should store the number of failed attempts. After 3 attempts, throw a `CardBlockedException`.
2. Create a `RestaurantClosedException` for a Zomato-like app with the restaurant name and closing time.
3. Build a simple banking system with custom exceptions: `InsufficientBalanceException`, `InvalidAmountException` (negative amount), `AccountNotFoundException`.

---

## 7. Common Exceptions Every Java Developer Must Know

### NullPointerException (NPE) — The Billion Dollar Mistake

Tony Hoare, who invented null references, called it his "billion dollar mistake." NPE is the single most common exception in Java.

```java
String name = null;
name.length();  // NullPointerException!

// The fix: always check for null
if (name != null) {
    System.out.println(name.length());
}

// Or use Optional (Java 8+)
Optional<String> maybeName = Optional.ofNullable(name);
maybeName.ifPresent(n -> System.out.println(n.length()));
```

**Where NPE commonly happens:**
```java
// 1. Returning null from a method
User user = findUser("unknownId");  // returns null
user.getName();  // NPE!

// 2. Uninitialized objects in arrays
String[] names = new String[5];  // All elements are null
names[0].toUpperCase();  // NPE!

// 3. Map.get() for missing key
HashMap<String, Integer> map = new HashMap<>();
int price = map.get("nonexistent");  // Returns null, unboxing to int = NPE!
// Fix: use getOrDefault()
int price = map.getOrDefault("nonexistent", 0);
```

### ClassCastException
```java
Object obj = "Hello";
Integer num = (Integer) obj;  // ClassCastException! String is not Integer

// Fix: use instanceof before casting
if (obj instanceof Integer) {
    Integer num = (Integer) obj;
}
```

### StackOverflowError
```java
// Infinite recursion
public static int factorial(int n) {
    return n * factorial(n - 1);  // Never stops! No base case!
    // Fix: add base case
    // if (n <= 1) return 1;
}
```

### OutOfMemoryError
```java
// Creating too many objects or too-large data structures
List<int[]> lists = new ArrayList<>();
while (true) {
    lists.add(new int[1000000]);  // Eventually runs out of memory
}
```

### ConcurrentModificationException
```java
List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C"));
for (String s : list) {
    if (s.equals("B")) {
        list.remove(s);  // ConcurrentModificationException!
    }
}

// Fix: use Iterator.remove()
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    if (it.next().equals("B")) {
        it.remove();  // Safe!
    }
}

// Or use removeIf (Java 8+)
list.removeIf(s -> s.equals("B"));
```

### Quick Reference

| Exception | When it happens | How to prevent |
|-----------|----------------|----------------|
| NullPointerException | Calling methods on null | Check for null, use Optional |
| ArrayIndexOutOfBoundsException | Invalid array index | Check bounds before accessing |
| ClassCastException | Wrong type cast | Use instanceof before casting |
| NumberFormatException | Parsing non-numeric string as number | Validate input before parsing |
| ArithmeticException | Division by zero | Check divisor before dividing |
| IllegalArgumentException | Invalid method argument | Validate arguments at method entry |
| StackOverflowError | Infinite recursion | Always have a base case |
| ConcurrentModificationException | Modifying collection while iterating | Use Iterator.remove() or removeIf() |

---

## 8. File I/O — Reading and Writing Files

### What is it?
File I/O (Input/Output) means reading data from files and writing data to files. Java provides several ways to do this, from old-school byte streams to modern convenience methods.

### Real-life analogy
- **Reading a file** = Reading a letter. You open the envelope (open the file), read the content (read bytes/characters), and close the envelope (close the file).
- **Writing to a file** = Writing a letter. You get paper (create/open file), write your message (write bytes/characters), and seal the envelope (close the file).
- **Buffered I/O** = Instead of reading one letter at a time from the mailbox, you grab a whole stack and read them all at your desk. Much faster!

### Reading Files

**Method 1: BufferedReader (traditional, line by line)**
```java
try (BufferedReader reader = new BufferedReader(new FileReader("students.txt"))) {
    String line;
    while ((line = reader.readLine()) != null) {
        System.out.println(line);
    }
} catch (IOException e) {
    System.out.println("Error reading file: " + e.getMessage());
}
```

**Method 2: Files.readAllLines() (modern, read everything at once)**
```java
import java.nio.file.Files;
import java.nio.file.Path;

try {
    List<String> lines = Files.readAllLines(Path.of("students.txt"));
    for (String line : lines) {
        System.out.println(line);
    }
} catch (IOException e) {
    System.out.println("Error: " + e.getMessage());
}
```

**Method 3: Files.readString() (Java 11+, entire file as one string)**
```java
try {
    String content = Files.readString(Path.of("students.txt"));
    System.out.println(content);
} catch (IOException e) {
    System.out.println("Error: " + e.getMessage());
}
```

**Method 4: Scanner (good for parsing structured data)**
```java
try (Scanner scanner = new Scanner(new File("students.txt"))) {
    while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        String[] parts = line.split(",");
        System.out.println("Name: " + parts[0] + ", Marks: " + parts[1]);
    }
} catch (FileNotFoundException e) {
    System.out.println("File not found");
}
```

### Writing Files

**Method 1: BufferedWriter (traditional)**
```java
try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"))) {
    writer.write("Student Report");
    writer.newLine();  // Platform-independent newline
    writer.write("Name: Sheetal, Marks: 92");
    writer.newLine();
    writer.write("Name: Priya, Marks: 85");
} catch (IOException e) {
    System.out.println("Error writing: " + e.getMessage());
}
```

**Method 2: Files.write() (modern)**
```java
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

// Write a list of lines
List<String> lines = List.of("Line 1", "Line 2", "Line 3");
try {
    Files.write(Path.of("output.txt"), lines);
} catch (IOException e) {
    System.out.println("Error: " + e.getMessage());
}

// Append to existing file
try {
    Files.writeString(Path.of("output.txt"), "\nNew line appended",
                      StandardOpenOption.APPEND);
} catch (IOException e) {
    System.out.println("Error: " + e.getMessage());
}
```

**Method 3: PrintWriter (most convenient for formatted output)**
```java
try (PrintWriter writer = new PrintWriter(new FileWriter("report.txt"))) {
    writer.println("Student Report");
    writer.println("==============");
    writer.printf("Name: %-15s Marks: %d%n", "Sheetal", 92);
    writer.printf("Name: %-15s Marks: %d%n", "Priya", 85);
} catch (IOException e) {
    System.out.println("Error: " + e.getMessage());
}
```

### Python comparison
```python
# Python reading
with open("students.txt", "r") as f:
    lines = f.readlines()
    # or
    content = f.read()

# Python writing
with open("output.txt", "w") as f:
    f.write("Hello\n")

# Python appending
with open("output.txt", "a") as f:
    f.write("Appended line\n")
```

```java
// Java equivalent of Python's open("file", "r")
Files.readAllLines(Path.of("students.txt"));

// Java equivalent of Python's open("file", "w")
Files.writeString(Path.of("output.txt"), "Hello\n");

// Java equivalent of Python's open("file", "a")
Files.writeString(Path.of("output.txt"), "Appended\n", StandardOpenOption.APPEND);
```

### java.nio.file — The Modern Way

The `java.nio.file` package (New I/O) is the modern replacement for the old `java.io` classes. Prefer it for all new code.

```java
import java.nio.file.*;

// Check if file exists
boolean exists = Files.exists(Path.of("data.txt"));

// Create directories
Files.createDirectories(Path.of("output/reports/2024"));

// Copy a file
Files.copy(Path.of("source.txt"), Path.of("backup.txt"),
           StandardCopyOption.REPLACE_EXISTING);

// Move/rename a file
Files.move(Path.of("old.txt"), Path.of("new.txt"));

// Delete a file
Files.deleteIfExists(Path.of("temp.txt"));

// Get file size
long size = Files.size(Path.of("data.txt"));

// List files in a directory
try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of("."))) {
    for (Path entry : stream) {
        System.out.println(entry.getFileName());
    }
}

// Walk a directory tree (recursive)
try (Stream<Path> paths = Files.walk(Path.of("."))) {
    paths.filter(Files::isRegularFile)
         .forEach(System.out::println);
}
```

### File vs Path
- `File` (java.io.File) — old, from Java 1.0
- `Path` (java.nio.file.Path) — modern, from Java 7. Use this.

```java
// Old way
File file = new File("data.txt");
boolean exists = file.exists();

// New way (preferred)
Path path = Path.of("data.txt");
boolean exists = Files.exists(path);
```

### Common mistakes
1. **Not closing files** — always use try-with-resources. Unclosed files leak resources.
2. **Using `FileReader`/`FileWriter` without buffering** — wrapping in `BufferedReader`/`BufferedWriter` is 10-100x faster for large files because it reads/writes in chunks instead of character by character.
3. **Hardcoding file paths with `\`** — use `Path.of()` or `/` which works on all operating systems. `"C:\Users\data"` won't work on Mac/Linux.
4. **Reading entire large files into memory** — for multi-GB files, read line by line with BufferedReader instead of `Files.readAllLines()`.

### Practice exercises
1. Write a program that reads a CSV file of student records (name,marks,grade) and prints a formatted table
2. Write a program that counts the number of words, lines, and characters in a text file (like Linux `wc` command)
3. Write a program that reads a file, converts all text to uppercase, and writes it to a new file
4. Write a program that merges two text files into one, alternating lines from each file

---

## 9. Serialization Basics

### What is it?
Serialization converts a Java object into a sequence of bytes that can be saved to a file or sent over a network. Deserialization is the reverse — converting bytes back into an object.

### Real-life analogy
Think of IKEA furniture:
- **Serialization** = Disassembling a cupboard into flat-pack pieces with assembly instructions. Now it can be shipped (stored, transmitted).
- **Deserialization** = Receiving the flat-pack and assembling it back into a cupboard. You get the original object back.

### The code
```java
import java.io.*;

// Step 1: Make your class Serializable
public class Student implements Serializable {
    private static final long serialVersionUID = 1L;  // Version control for serialization

    String name;
    int age;
    double gpa;
    transient String password;  // 'transient' = don't serialize this field!

    public Student(String name, int age, double gpa, String password) {
        this.name = name;
        this.age = age;
        this.gpa = gpa;
        this.password = password;
    }

    @Override
    public String toString() {
        return "Student{name='" + name + "', age=" + age +
               ", gpa=" + gpa + ", password='" + password + "'}";
    }
}

// Step 2: Serialize (save to file)
Student student = new Student("Sheetal", 25, 9.2, "secret123");

try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("student.ser"))) {
    oos.writeObject(student);
    System.out.println("Student serialized to file");
} catch (IOException e) {
    System.out.println("Error: " + e.getMessage());
}

// Step 3: Deserialize (read from file)
try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("student.ser"))) {
    Student loaded = (Student) ois.readObject();
    System.out.println(loaded);
    // Output: Student{name='Sheetal', age=25, gpa=9.2, password='null'}
    // Note: password is null because it was transient!
} catch (IOException | ClassNotFoundException e) {
    System.out.println("Error: " + e.getMessage());
}
```

### Key concepts
- **Serializable** — a marker interface (no methods). Just tells Java "this class can be serialized."
- **serialVersionUID** — version number. If the class changes (fields added/removed), deserialization of old data fails unless the UID matches.
- **transient** — fields marked transient are NOT serialized. Use for passwords, temporary data, cached values.

### Python comparison
```python
import pickle

# Python serialization
student = {"name": "Sheetal", "age": 25}
with open("student.pkl", "wb") as f:
    pickle.dump(student, f)

# Python deserialization
with open("student.pkl", "rb") as f:
    loaded = pickle.load(f)
```

### Modern alternatives
In practice, most Java applications use JSON (Jackson library) or Protocol Buffers instead of Java's built-in serialization:
```java
// Using Jackson (much more common in real projects)
ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(student);  // Object → JSON string
Student loaded = mapper.readValue(json, Student.class);  // JSON string → Object
```

---

## 10. Practice Project: File-Based Student Record System

### Requirements
Build a command-line application that manages student records using file I/O:

**Student class:**
- Fields: id (auto-generated), name, rollNumber, department, marks (array of 5 subjects), percentage, grade
- Implements Serializable
- Implements Comparable (sort by percentage)
- Custom toString() for display

**Features:**
1. **Add Student** — Take input from user, calculate percentage and grade, save to file
2. **View All Students** — Read from file and display in a formatted table
3. **Search Student** — Search by name or roll number
4. **Update Student** — Find by roll number, update marks, recalculate percentage and grade
5. **Delete Student** — Remove a student record
6. **Statistics** — Show: total students, class average, highest/lowest scorers, subject-wise average
7. **Export to CSV** — Write all records to a CSV file
8. **Import from CSV** — Read records from a CSV file

**Exception Handling:**
- Custom `StudentNotFoundException` when searching for non-existent student
- Custom `DuplicateRollNumberException` when adding a student with existing roll number
- Handle `FileNotFoundException` when the data file doesn't exist yet (first run)
- Handle `InvalidInputException` for invalid marks (< 0 or > 100)
- Graceful handling of all I/O errors

**Grade calculation:**
```
Percentage >= 90: Grade A+
Percentage >= 80: Grade A
Percentage >= 70: Grade B
Percentage >= 60: Grade C
Percentage >= 50: Grade D
Percentage < 50:  Grade F
```

**Sample menu:**
```
=== Student Record Management System ===
1. Add Student
2. View All Students
3. Search Student
4. Update Student Marks
5. Delete Student
6. View Statistics
7. Export to CSV
8. Import from CSV
9. Exit
Choose an option:
```

**File structure:**
- `students.dat` — serialized student objects (binary)
- `students.csv` — CSV export/import
- `students_backup.dat` — automatic backup before any modification

**Implementation tips:**
- Read all students into an ArrayList at startup
- Perform operations on the ArrayList
- Write the entire ArrayList back to file after any change
- Create a backup before every write operation
- Use try-with-resources for all file operations
- Use custom exceptions for domain-specific errors

This project uses EVERY concept from Week 4: exception handling (try-catch, custom exceptions, checked/unchecked), file I/O (reading, writing, serialization), and integrates with Week 1-3 concepts (classes, collections, sorting).

---

## Week 4 Summary: Quick Reference

### Exception handling
| Syntax | Purpose |
|--------|---------|
| `try { } catch { }` | Handle exceptions |
| `finally { }` | Always runs (cleanup) |
| `try (resource) { }` | Auto-close resources |
| `throw new X()` | Create and throw an exception |
| `throws X` | Declare method might throw |
| `extends Exception` | Custom checked exception |
| `extends RuntimeException` | Custom unchecked exception |

### File I/O
| Task | Modern way | Old way |
|------|-----------|---------|
| Read all lines | `Files.readAllLines(Path.of("f"))` | BufferedReader + FileReader |
| Read entire file | `Files.readString(Path.of("f"))` | BufferedReader + StringBuilder |
| Write lines | `Files.write(Path.of("f"), lines)` | BufferedWriter + FileWriter |
| Write string | `Files.writeString(Path.of("f"), s)` | PrintWriter |
| Append | `Files.writeString(p, s, APPEND)` | FileWriter(file, true) |
| File exists | `Files.exists(Path.of("f"))` | new File("f").exists() |
| Create dirs | `Files.createDirectories(Path.of("d"))` | new File("d").mkdirs() |

### Python-to-Java translation
| Python | Java |
|--------|------|
| `try: ... except: ...` | `try { } catch { }` |
| `raise ValueError("msg")` | `throw new IllegalArgumentException("msg")` |
| `with open("f") as file:` | `try (var reader = ...) { }` |
| `open("f").read()` | `Files.readString(Path.of("f"))` |
| `open("f","w").write(s)` | `Files.writeString(Path.of("f"), s)` |
| `pickle.dump(obj, f)` | `ObjectOutputStream.writeObject(obj)` |
| No forced exception handling | Checked exceptions MUST be handled |
