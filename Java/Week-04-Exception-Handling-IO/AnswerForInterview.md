# Week 4: Exception Handling and File I/O — Interview Answers & Exercise Solutions

> **The Interview Golden Rule** 🏆
> For every concept, answer in this order:
> **Definition → Why it exists → Code example → Gotcha / Best practice.**
> A short, confident definition first proves you *understand* it; the "why" proves you can *reason* about it; the code proves you can *use* it; the gotcha proves you've been *burned by it in real life*. That last part is what separates a senior from a fresher in interviews.

This file has two parts:
- **PART A** — Full worked Java solutions to every exercise and project in `Plan.md`.
- **PART B** — 16 likely interview questions with confident, spoken-style model answers.

All code is plain Java (Java 11+ where noted) and compiles as-is. Where a snippet needs a `main` to run, one is included.

---

# PART A — Exercise / Practice Solutions

The `Plan.md` has practice exercises in Sections 3, 6, 8, and a full project in Section 10. Every one is solved below.

---

## A1. Section 3 — try-catch-finally Exercises

### Exercise 3.1 — Divide two numbers, handle ArithmeticException, InputMismatchException, and any other exception

```java
import java.util.InputMismatchException;
import java.util.Scanner;

public class SafeDivision {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.print("Enter numerator: ");
            int numerator = scanner.nextInt();

            System.out.print("Enter denominator: ");
            int denominator = scanner.nextInt();

            int result = numerator / denominator;   // may throw ArithmeticException
            System.out.println("Result = " + result);

        } catch (ArithmeticException e) {
            // Triggered specifically by integer divide-by-zero
            System.out.println("Cannot divide by zero!");
        } catch (InputMismatchException e) {
            // Triggered when the user types non-numeric input (e.g. "abc")
            System.out.println("Please enter valid integers only.");
        } catch (Exception e) {
            // Catch-all safety net — MUST be last (most general)
            System.out.println("Something went wrong: " + e.getMessage());
        } finally {
            scanner.close();   // always release the resource
            System.out.println("Program finished. Scanner closed.");
        }
    }
}
```
**Key teaching point:** catch order is most-specific → least-specific. `Exception` is last because it is the parent of both `ArithmeticException` and `InputMismatchException`.

---

### Exercise 3.2 — Access an array element at a user-specified index, handle ArrayIndexOutOfBoundsException

```java
import java.util.Scanner;

public class SafeArrayAccess {
    public static void main(String[] args) {
        String[] names = {"Sheetal", "Priya", "Arjun", "Meera"};
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.print("Enter an index (0-" + (names.length - 1) + "): ");
            int index = scanner.nextInt();

            System.out.println("Name at index " + index + " = " + names[index]);

        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Invalid index! Please pick 0 to " + (names.length - 1) + ".");
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
```
**Gotcha:** the exception message for an out-of-bounds access is just the bad index (e.g. `Index 5 out of bounds for length 4`), so always add context for the user.

---

### Exercise 3.3 — Demonstrate what happens WITHOUT a finally block when an exception occurs while a file is open

```java
import java.io.FileReader;
import java.io.IOException;

public class NoFinallyLeak {

    // BAD: no finally, no try-with-resources. If readLine throws,
    // the file is NEVER closed and the descriptor leaks.
    static void leakyRead() throws IOException {
        FileReader reader = new FileReader("data.txt");
        System.out.println("File opened.");
        int c = reader.read();                 // imagine this throws an IOException
        if (c == -1) throw new IOException("Simulated read failure!");
        reader.close();                        // <-- UNREACHABLE if line above throws
        System.out.println("File closed.");    // never prints on failure
    }

    // GOOD: finally guarantees close, even on exception.
    static void safeRead() {
        FileReader reader = null;
        try {
            reader = new FileReader("data.txt");
            System.out.println("File opened.");
            throw new IOException("Simulated read failure!");
        } catch (IOException e) {
            System.out.println("Caught: " + e.getMessage());
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException ignored) {}
            }
            System.out.println("finally ran -> file definitely closed.");
        }
    }

    public static void main(String[] args) {
        try {
            leakyRead();
        } catch (IOException e) {
            System.out.println("leakyRead failed -> reader was left OPEN (resource leak).");
        }
        safeRead();
    }
}
```
**Lesson:** without `finally` (or try-with-resources), an exception thrown between *open* and *close* leaks the file handle. This is exactly the pain that try-with-resources solves.

---

## A2. Section 6 — Custom Exception Exercises

### Exercise 6.1 — ATM: `InvalidPINException` (tracks attempts) → after 3 failures throw `CardBlockedException`

```java
// --- Custom exceptions ---
class InvalidPINException extends Exception {
    private final int attemptsRemaining;
    public InvalidPINException(int attemptsRemaining) {
        super("Invalid PIN. Attempts remaining: " + attemptsRemaining);
        this.attemptsRemaining = attemptsRemaining;
    }
    public int getAttemptsRemaining() { return attemptsRemaining; }
}

class CardBlockedException extends Exception {
    public CardBlockedException() {
        super("Card blocked after 3 failed attempts. Contact your bank.");
    }
}

// --- ATM logic ---
class ATM {
    private static final int MAX_ATTEMPTS = 3;   // banking standard: 3 tries then lock
    private final int correctPin;
    private int failedAttempts = 0;
    private boolean blocked = false;

    public ATM(int correctPin) { this.correctPin = correctPin; }

    public void enterPin(int pin) throws InvalidPINException, CardBlockedException {
        if (blocked) throw new CardBlockedException();

        if (pin == correctPin) {
            failedAttempts = 0;                  // reset on success
            System.out.println("PIN accepted. Welcome!");
            return;
        }

        failedAttempts++;
        if (failedAttempts >= MAX_ATTEMPTS) {
            blocked = true;
            throw new CardBlockedException();
        }
        throw new InvalidPINException(MAX_ATTEMPTS - failedAttempts);
    }
}

public class ATMDemo {
    public static void main(String[] args) {
        ATM atm = new ATM(1234);
        int[] attempts = {1111, 2222, 3333, 4444}; // all wrong → triggers block on the 3rd

        for (int pin : attempts) {
            try {
                atm.enterPin(pin);
            } catch (InvalidPINException e) {
                System.out.println(e.getMessage());
            } catch (CardBlockedException e) {
                System.out.println(e.getMessage());
                break;   // stop trying once blocked
            }
        }
    }
}
```

---

### Exercise 6.2 — `RestaurantClosedException` (name + closing time)

```java
import java.time.LocalTime;

class RestaurantClosedException extends Exception {
    private final String restaurantName;
    private final LocalTime closingTime;

    public RestaurantClosedException(String restaurantName, LocalTime closingTime) {
        super(restaurantName + " is closed. It shut at " + closingTime + ".");
        this.restaurantName = restaurantName;
        this.closingTime = closingTime;
    }
    public String getRestaurantName() { return restaurantName; }
    public LocalTime getClosingTime() { return closingTime; }
}

class Restaurant {
    private final String name;
    private final LocalTime opening;
    private final LocalTime closing;

    public Restaurant(String name, LocalTime opening, LocalTime closing) {
        this.name = name; this.opening = opening; this.closing = closing;
    }

    public void placeOrder(String item, LocalTime now) throws RestaurantClosedException {
        if (now.isBefore(opening) || now.isAfter(closing)) {
            throw new RestaurantClosedException(name, closing);
        }
        System.out.println("Order placed for " + item + " at " + name);
    }
}

public class ZomatoDemo {
    public static void main(String[] args) {
        Restaurant biryaniHouse =
            new Restaurant("Biryani House", LocalTime.of(11, 0), LocalTime.of(23, 0));
        try {
            biryaniHouse.placeOrder("Chicken Biryani", LocalTime.of(23, 30)); // too late
        } catch (RestaurantClosedException e) {
            System.out.println(e.getMessage());
            System.out.println("Try again after it reopens. Closed at: " + e.getClosingTime());
        }
    }
}
```

---

### Exercise 6.3 — Banking system with `InsufficientBalanceException`, `InvalidAmountException`, `AccountNotFoundException`

```java
import java.util.HashMap;
import java.util.Map;

// --- Custom exceptions ---
class InsufficientBalanceException extends Exception {
    private final double deficit;
    public InsufficientBalanceException(double balance, double requested) {
        super("Insufficient balance: have " + balance + ", need " + requested);
        this.deficit = requested - balance;
    }
    public double getDeficit() { return deficit; }
}

class InvalidAmountException extends RuntimeException {   // programming/usage error → unchecked
    public InvalidAmountException(double amount) {
        super("Amount must be positive, got: " + amount);
    }
}

class AccountNotFoundException extends Exception {
    public AccountNotFoundException(String accountId) {
        super("No account found with id: " + accountId);
    }
}

// --- Bank ---
class Bank {
    private final Map<String, Double> accounts = new HashMap<>();

    public void open(String id, double initial) { accounts.put(id, initial); }

    private double balanceOf(String id) throws AccountNotFoundException {
        Double bal = accounts.get(id);
        if (bal == null) throw new AccountNotFoundException(id);
        return bal;
    }

    public void withdraw(String id, double amount)
            throws AccountNotFoundException, InsufficientBalanceException {
        if (amount <= 0) throw new InvalidAmountException(amount);
        double balance = balanceOf(id);
        if (amount > balance) throw new InsufficientBalanceException(balance, amount);
        accounts.put(id, balance - amount);
        System.out.println("Withdrew " + amount + ". New balance: " + accounts.get(id));
    }
}

public class BankDemo {
    public static void main(String[] args) {
        Bank bank = new Bank();
        bank.open("ACC100", 500.0);

        // 1. Insufficient balance
        try { bank.withdraw("ACC100", 800); }
        catch (InsufficientBalanceException e) {
            System.out.println(e.getMessage() + " | Short by " + e.getDeficit());
        } catch (AccountNotFoundException e) { System.out.println(e.getMessage()); }

        // 2. Account not found
        try { bank.withdraw("GHOST", 100); }
        catch (AccountNotFoundException e) { System.out.println(e.getMessage()); }
        catch (InsufficientBalanceException e) { System.out.println(e.getMessage()); }

        // 3. Invalid (negative) amount — unchecked, so no checked catch needed
        try { bank.withdraw("ACC100", -50); }
        catch (InvalidAmountException e) { System.out.println(e.getMessage()); }
        catch (Exception e) { System.out.println(e.getMessage()); }
    }
}
```
**Design note:** `InsufficientBalanceException` and `AccountNotFoundException` are *checked* (caller can recover — retry, top up, re-enter id). `InvalidAmountException` is *unchecked* because passing a negative amount is a caller bug, not a recoverable runtime condition.

---

## A3. Section 8 — File I/O Exercises

> All examples first write a sample input file so they run standalone.

### Exercise 8.1 — Read a CSV of student records (name,marks,grade) and print a formatted table

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvTablePrinter {
    public static void main(String[] args) throws IOException {
        Path file = Path.of("students.csv");
        // Seed sample data so this runs on its own:
        Files.write(file, List.of(
            "Sheetal,92,A+",
            "Priya,85,A",
            "Arjun,67,C"
        ));

        List<String> lines = Files.readAllLines(file);

        System.out.printf("%-15s %-8s %-6s%n", "NAME", "MARKS", "GRADE");
        System.out.println("-".repeat(31));
        for (String line : lines) {
            String[] p = line.split(",");
            if (p.length < 3) continue;                 // skip malformed rows defensively
            System.out.printf("%-15s %-8s %-6s%n", p[0], p[1].trim(), p[2].trim());
        }
    }
}
```

---

### Exercise 8.2 — Count words, lines, and characters in a text file (like Linux `wc`)

```java
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WordCount {
    public static void main(String[] args) throws IOException {
        Path file = Path.of("sample.txt");
        Files.writeString(file, "Hello world\nJava exceptions and IO\nThree lines here\n");

        long lines = 0, words = 0, chars = 0;
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines++;
                chars += line.length() + 1;             // +1 for the stripped newline
                if (!line.isBlank()) {
                    words += line.trim().split("\\s+").length;  // split on any whitespace
                }
            }
        }
        System.out.printf("Lines: %d, Words: %d, Chars: %d%n", lines, words, chars);
    }
}
```
**Gotcha:** `readLine()` strips the newline, so we add `+1` per line to count characters like `wc` does.

---

### Exercise 8.3 — Read a file, convert all text to UPPERCASE, write to a new file

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UppercaseConverter {
    public static void main(String[] args) throws IOException {
        Path in  = Path.of("input.txt");
        Path out = Path.of("output_upper.txt");

        Files.writeString(in, "make me loud\nsecond line\n");   // seed input

        String content = Files.readString(in);                  // Java 11+
        Files.writeString(out, content.toUpperCase());

        System.out.println("Wrote uppercase version to " + out);
        System.out.println(Files.readString(out));
    }
}
```

---

### Exercise 8.4 — Merge two text files into one, alternating lines from each

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AlternatingMerge {
    public static void main(String[] args) throws IOException {
        Path a = Path.of("fileA.txt");
        Path b = Path.of("fileB.txt");
        Path merged = Path.of("merged.txt");

        Files.write(a, List.of("A1", "A2", "A3"));
        Files.write(b, List.of("B1", "B2"));

        List<String> linesA = Files.readAllLines(a);
        List<String> linesB = Files.readAllLines(b);

        List<String> result = new ArrayList<>();
        int max = Math.max(linesA.size(), linesB.size());
        for (int i = 0; i < max; i++) {
            if (i < linesA.size()) result.add(linesA.get(i));   // take from A if available
            if (i < linesB.size()) result.add(linesB.get(i));   // then from B
        }

        Files.write(merged, result);
        System.out.println("Merged file contents:");
        result.forEach(System.out::println);   // A1, B1, A2, B2, A3
    }
}
```
**Gotcha:** files can have different line counts — guard each access with a bounds check so the shorter file doesn't cause an `IndexOutOfBoundsException`.

---

## A4. Section 10 — Practice Project: File-Based Student Record System

A complete, compilable implementation. It uses serialization for the binary store, CSV import/export, custom exceptions, `Comparable`, statistics, automatic backup, and try-with-resources throughout. Save each top-level class in its own `.java` file (or keep nested as shown) and run `StudentRecordSystem`.

```java
import java.io.*;
import java.nio.file.*;
import java.util.*;

// ===== Custom exceptions =====
class StudentNotFoundException extends Exception {
    public StudentNotFoundException(String key) { super("No student found for: " + key); }
}
class DuplicateRollNumberException extends Exception {
    public DuplicateRollNumberException(String roll) { super("Roll number already exists: " + roll); }
}
class InvalidInputException extends RuntimeException {   // bad marks = caller error → unchecked
    public InvalidInputException(String msg) { super(msg); }
}

// ===== Domain model =====
class Student implements Serializable, Comparable<Student> {
    private static final long serialVersionUID = 1L;   // lock the serialization contract

    private static int counter = 1000;                 // auto-id source
    private final int id;
    private String name;
    private String rollNumber;
    private String department;
    private int[] marks;          // 5 subjects
    private double percentage;
    private String grade;

    public Student(String name, String rollNumber, String department, int[] marks) {
        validate(marks);
        this.id = ++counter;
        this.name = name;
        this.rollNumber = rollNumber;
        this.department = department;
        this.marks = marks.clone();
        recompute();
    }

    private static void validate(int[] marks) {
        if (marks == null || marks.length != 5)
            throw new InvalidInputException("Exactly 5 subject marks required.");
        for (int m : marks)
            if (m < 0 || m > 100)
                throw new InvalidInputException("Mark out of range (0-100): " + m);
    }

    private void recompute() {
        int total = 0;
        for (int m : marks) total += m;
        this.percentage = total / 5.0;       // 5 subjects each out of 100
        this.grade = calcGrade(percentage);
    }

    private static String calcGrade(double pct) {
        if (pct >= 90) return "A+";
        if (pct >= 80) return "A";
        if (pct >= 70) return "B";
        if (pct >= 60) return "C";
        if (pct >= 50) return "D";
        return "F";
    }

    public void updateMarks(int[] newMarks) {
        validate(newMarks);
        this.marks = newMarks.clone();
        recompute();
    }

    // getters
    public String getRollNumber() { return rollNumber; }
    public String getName() { return name; }
    public double getPercentage() { return percentage; }
    public String getGrade() { return grade; }
    public int[] getMarks() { return marks.clone(); }
    public String getDepartment() { return department; }

    @Override public int compareTo(Student other) {
        // sort by percentage, highest first
        return Double.compare(other.percentage, this.percentage);
    }

    public String toCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(",").append(rollNumber).append(",").append(department);
        for (int m : marks) sb.append(",").append(m);
        return sb.toString();
    }

    public static Student fromCsv(String line) {
        String[] p = line.split(",");
        if (p.length != 8) throw new InvalidInputException("Bad CSV row: " + line);
        int[] marks = new int[5];
        for (int i = 0; i < 5; i++) marks[i] = Integer.parseInt(p[3 + i].trim());
        return new Student(p[0].trim(), p[1].trim(), p[2].trim(), marks);
    }

    @Override public String toString() {
        return String.format("%-6d %-12s %-10s %-8s %6.2f%%  %-3s",
                id, name, rollNumber, department, percentage, grade);
    }
}

// ===== Service / repository =====
class StudentService {
    private static final Path DATA   = Path.of("students.dat");
    private static final Path BACKUP = Path.of("students_backup.dat");
    private static final Path CSV    = Path.of("students.csv");

    private final List<Student> students = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public void load() {
        if (!Files.exists(DATA)) {
            System.out.println("(No data file yet — starting fresh.)");
            return;     // first run: FileNotFound handled by just skipping
        }
        try (ObjectInputStream ois =
                 new ObjectInputStream(Files.newInputStream(DATA))) {
            students.addAll((List<Student>) ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Could not load data: " + e.getMessage());
        }
    }

    private void backupThenSave() {
        try {
            if (Files.exists(DATA))
                Files.copy(DATA, BACKUP, StandardCopyOption.REPLACE_EXISTING);
            try (ObjectOutputStream oos =
                     new ObjectOutputStream(Files.newOutputStream(DATA))) {
                oos.writeObject(students);
            }
        } catch (IOException e) {
            System.out.println("Save failed: " + e.getMessage());
        }
    }

    public void add(Student s) throws DuplicateRollNumberException {
        for (Student existing : students)
            if (existing.getRollNumber().equalsIgnoreCase(s.getRollNumber()))
                throw new DuplicateRollNumberException(s.getRollNumber());
        students.add(s);
        backupThenSave();
    }

    public Student search(String key) throws StudentNotFoundException {
        for (Student s : students)
            if (s.getRollNumber().equalsIgnoreCase(key) || s.getName().equalsIgnoreCase(key))
                return s;
        throw new StudentNotFoundException(key);
    }

    public void updateMarks(String roll, int[] marks) throws StudentNotFoundException {
        Student s = search(roll);
        s.updateMarks(marks);
        backupThenSave();
    }

    public void delete(String roll) throws StudentNotFoundException {
        Student s = search(roll);
        students.remove(s);
        backupThenSave();
    }

    public void viewAll() {
        if (students.isEmpty()) { System.out.println("No students yet."); return; }
        List<Student> sorted = new ArrayList<>(students);
        Collections.sort(sorted);   // uses Comparable → by percentage desc
        System.out.printf("%-6s %-12s %-10s %-8s %7s  %-3s%n",
                "ID", "NAME", "ROLL", "DEPT", "PCT", "GR");
        sorted.forEach(System.out::println);
    }

    public void statistics() {
        if (students.isEmpty()) { System.out.println("No data for statistics."); return; }
        double sum = 0, highest = Double.MIN_VALUE, lowest = Double.MAX_VALUE;
        Student top = null, bottom = null;
        double[] subjectTotals = new double[5];
        for (Student s : students) {
            double p = s.getPercentage();
            sum += p;
            if (p > highest) { highest = p; top = s; }
            if (p < lowest)  { lowest = p; bottom = s; }
            int[] m = s.getMarks();
            for (int i = 0; i < 5; i++) subjectTotals[i] += m[i];
        }
        int n = students.size();
        System.out.println("Total students : " + n);
        System.out.printf("Class average  : %.2f%%%n", sum / n);
        System.out.printf("Top scorer     : %s (%.2f%%)%n", top.getName(), highest);
        System.out.printf("Lowest scorer  : %s (%.2f%%)%n", bottom.getName(), lowest);
        for (int i = 0; i < 5; i++)
            System.out.printf("Subject %d avg  : %.2f%n", i + 1, subjectTotals[i] / n);
    }

    public void exportCsv() {
        List<String> rows = new ArrayList<>();
        for (Student s : students) rows.add(s.toCsv());
        try {
            Files.write(CSV, rows);
            System.out.println("Exported " + rows.size() + " records to " + CSV);
        } catch (IOException e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    public void importCsv() {
        try {
            List<String> rows = Files.readAllLines(CSV);
            int imported = 0;
            for (String row : rows) {
                if (row.isBlank()) continue;
                try {
                    add(Student.fromCsv(row));
                    imported++;
                } catch (DuplicateRollNumberException dup) {
                    System.out.println("Skipped duplicate: " + dup.getMessage());
                } catch (InvalidInputException bad) {
                    System.out.println("Skipped bad row: " + bad.getMessage());
                }
            }
            System.out.println("Imported " + imported + " records.");
        } catch (IOException e) {
            System.out.println("Import failed: " + e.getMessage());
        }
    }
}

// ===== Menu-driven entry point =====
public class StudentRecordSystem {
    public static void main(String[] args) {
        StudentService service = new StudentService();
        service.load();
        Scanner sc = new Scanner(System.in);

        boolean running = true;
        while (running) {
            System.out.println("\n=== Student Record Management System ===");
            System.out.println("1. Add  2. View All  3. Search  4. Update Marks");
            System.out.println("5. Delete  6. Statistics  7. Export CSV  8. Import CSV  9. Exit");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> {
                        System.out.print("Name: ");       String name = sc.nextLine();
                        System.out.print("Roll: ");       String roll = sc.nextLine();
                        System.out.print("Department: "); String dept = sc.nextLine();
                        int[] marks = new int[5];
                        for (int i = 0; i < 5; i++) {
                            System.out.print("Marks subject " + (i + 1) + ": ");
                            marks[i] = Integer.parseInt(sc.nextLine().trim());
                        }
                        service.add(new Student(name, roll, dept, marks));
                        System.out.println("Added.");
                    }
                    case "2" -> service.viewAll();
                    case "3" -> {
                        System.out.print("Search by name or roll: ");
                        System.out.println(service.search(sc.nextLine().trim()));
                    }
                    case "4" -> {
                        System.out.print("Roll to update: ");
                        String roll = sc.nextLine().trim();
                        int[] marks = new int[5];
                        for (int i = 0; i < 5; i++) {
                            System.out.print("New marks subject " + (i + 1) + ": ");
                            marks[i] = Integer.parseInt(sc.nextLine().trim());
                        }
                        service.updateMarks(roll, marks);
                        System.out.println("Updated.");
                    }
                    case "5" -> {
                        System.out.print("Roll to delete: ");
                        service.delete(sc.nextLine().trim());
                        System.out.println("Deleted.");
                    }
                    case "6" -> service.statistics();
                    case "7" -> service.exportCsv();
                    case "8" -> service.importCsv();
                    case "9" -> { running = false; System.out.println("Goodbye!"); }
                    default  -> System.out.println("Invalid option.");
                }
            } catch (StudentNotFoundException | DuplicateRollNumberException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (InvalidInputException e) {
                System.out.println("Invalid input: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Marks must be whole numbers.");
            }
        }
        sc.close();
    }
}
```
**What this project demonstrates (interview talking points):**
- **Checked exceptions** (`StudentNotFoundException`, `DuplicateRollNumberException`) for recoverable domain errors.
- **Unchecked** (`InvalidInputException`) for caller misuse (bad marks).
- **try-with-resources** on every stream (`ObjectInputStream`/`ObjectOutputStream`).
- **Serialization** with `serialVersionUID` for the binary store; **CSV** for interop.
- **Comparable** for sorting by percentage; **automatic backup** before each write.

---

# PART B — Interview Questions & Model Answers

> Spoken-style answers — say these out loud as written; each opens with a crisp definition and closes with a gotcha.

### Q1. What is an exception, and what is the exception hierarchy in Java?
"An exception is an event that disrupts the normal flow of a program at runtime — like dividing by zero or a missing file. In Java everything throwable extends `Throwable`, which has two children: `Error` and `Exception`. `Error` represents serious JVM-level failures you shouldn't catch — `OutOfMemoryError`, `StackOverflowError`. `Exception` is for conditions your code can handle, and it splits into *checked* exceptions and *unchecked* exceptions, where unchecked ones extend `RuntimeException`. The golden rule: **catch Exceptions, never Errors.**"

---

### Q2. Checked vs unchecked exceptions — what's the difference?
"Checked exceptions are verified at *compile time* — the compiler forces you to either catch them or declare them with `throws`. They extend `Exception` but not `RuntimeException`. Examples are `IOException` and `SQLException`, usually caused by external factors like files or networks. Unchecked exceptions extend `RuntimeException`, are not enforced by the compiler, and represent programming bugs like `NullPointerException` or `ArrayIndexOutOfBoundsException`. The rule of thumb: checked = the caller can reasonably recover; unchecked = fix your code."

| Feature | Checked | Unchecked |
|---|---|---|
| Detected | Compile time | Runtime |
| Must handle? | Yes (compile error otherwise) | No |
| Extends | `Exception` (not `RuntimeException`) | `RuntimeException` |
| Caused by | External factors | Programming bugs |
| Examples | `IOException`, `SQLException` | `NullPointerException`, `ClassCastException` |

---

### Q3. `throw` vs `throws` — explain the difference.
"`throw` is a statement that actually creates and throws an exception object — `throw new IllegalArgumentException("bad")`. It lives inside a method body and throws one exception at a time. `throws` is a clause in the method *signature* that declares the method *might* throw certain exceptions, passing responsibility to the caller — `void read() throws IOException, SQLException`. So: `throw` = I'm throwing it now; `throws` = I'm warning you I might."

| | `throw` | `throws` |
|---|---|---|
| Purpose | Actually throws an exception | Declares a method *may* throw |
| Location | Inside method body | Method signature |
| Count | One at a time | Multiple, comma-separated |

---

### Q4. Difference between `final`, `finally`, and `finalize`?
"Three completely unrelated things with similar names — a classic trick question. **`final`** is a keyword: a `final` variable can't be reassigned, a `final` method can't be overridden, a `final` class can't be extended. **`finally`** is a block in try-catch that *always* executes — used for cleanup like closing resources. **`finalize`** was a method on `Object` called by the garbage collector before reclaiming an object; it's *deprecated since Java 9* and you should never rely on it — use try-with-resources or `Cleaner` instead."

| Term | What it is | Use |
|---|---|---|
| `final` | Keyword | Constants, no override, no inheritance |
| `finally` | Block | Guaranteed cleanup |
| `finalize` | Method (deprecated) | Old GC hook — avoid |

---

### Q5. What is try-with-resources and why is it better than finally?
"try-with-resources, added in Java 7, automatically closes any resource declared in its parentheses, as long as the resource implements `AutoCloseable`. You write `try (BufferedReader r = new BufferedReader(...)) { ... }` and Java closes `r` automatically — even if an exception is thrown. It's better than a manual `finally` because the old way needed a nested try-catch just to call `close()`, which was verbose and easy to get wrong. With multiple resources, they're closed in *reverse* order of declaration. It also handles *suppressed exceptions* — if both your code and `close()` throw, the close exception is attached as suppressed rather than masking the original."

---

### Q6. Why does catch order matter, and what is multi-catch?
"Catch blocks are evaluated top to bottom, so you must order them from most-specific to most-general. If you put `catch (Exception e)` before `catch (ArithmeticException e)`, the code won't even compile — Java knows the second is unreachable because `Exception` already caught everything. Multi-catch, from Java 7, lets you handle several unrelated exception types in one block with the pipe operator: `catch (IOException | SQLException e)`. The caught variable is implicitly `final`, and the types can't be in a parent-child relationship — that would be redundant."

---

### Q7. What happens if a `finally` block has a `return` statement?
"It overrides everything. A `return` in `finally` will swallow any return value or exception coming from the `try` or `catch` blocks — even an exception that was propagating up. So if `try` returns 1 but `finally` returns 2, the method returns 2. And if `try` throws an exception but `finally` returns a value, the exception silently disappears. This is considered an anti-pattern — **never `return` or `throw` from a `finally` block** because it hides errors. It's a favourite interview gotcha."

```java
static int trap() {
    try { return 1; }
    finally { return 2; }   // method returns 2; the 1 is lost
}
```

---

### Q8. How do you create a custom exception, and when checked vs unchecked?
"To create a custom exception, extend `Exception` for a checked one or `RuntimeException` for an unchecked one, and provide constructors that call `super(message)` — ideally also one taking a `Throwable cause` so you preserve the stack trace when wrapping. You choose *checked* when the caller can reasonably recover — like `InsufficientBalanceException`, where they could prompt for a smaller amount. You choose *unchecked* for programmer errors that shouldn't happen if the code is correct — like passing a negative amount. Best practice: give the exception meaningful fields and getters, like a `getDeficit()` method, so handlers get useful context, not just a string."

---

### Q9. What is the difference between byte streams and character streams?
"Byte streams handle raw binary data 8 bits at a time and are built on `InputStream`/`OutputStream` — use them for images, audio, serialized objects, any binary file. Character streams handle text and are built on `Reader`/`Writer` — they understand character encoding like UTF-8, so they correctly handle multi-byte characters. As a rule: **text → character streams (`FileReader`, `BufferedReader`), binary → byte streams (`FileInputStream`, `ObjectOutputStream`).** Using a byte stream for text risks corrupting non-ASCII characters."

| | Byte streams | Character streams |
|---|---|---|
| Base classes | `InputStream` / `OutputStream` | `Reader` / `Writer` |
| Unit | 8-bit bytes | 16-bit chars (encoding-aware) |
| Use for | Images, audio, serialization | Text files |
| Examples | `FileInputStream`, `ObjectOutputStream` | `FileReader`, `BufferedReader` |

---

### Q10. What is buffering and why use `BufferedReader`/`BufferedWriter`?
"A plain `FileReader` hits the disk on every single character read — extremely slow. A `BufferedReader` wraps it and reads a big chunk into an in-memory buffer at once, then serves characters from memory, so it can be 10–100x faster for large files. It also adds the convenient `readLine()` method. Same idea for writing with `BufferedWriter` and its `newLine()`. The pattern is always *wrap the low-level stream in a buffered one*: `new BufferedReader(new FileReader(path))`. The analogy: instead of walking to the mailbox for one letter at a time, you grab the whole stack at once."

---

### Q11. What is serialization, and what do `serialVersionUID` and `transient` do?
"Serialization converts a Java object into a byte stream so it can be saved to disk or sent over a network; deserialization reconstructs the object. You enable it by implementing the marker interface `Serializable` — it has no methods, it just flags the class. `serialVersionUID` is a version number for the class; if you serialize an object and later change the class, deserialization fails with `InvalidClassException` unless the UID matches — so you declare it explicitly to control compatibility. `transient` marks a field to be *skipped* during serialization — perfect for passwords, derived values, or anything non-serializable; on deserialization those fields come back as defaults like `null` or `0`."

---

### Q12. What is `NullPointerException` and how do you prevent it?
"It's the most common runtime exception in Java — Tony Hoare called null his 'billion-dollar mistake.' It happens when you call a method or access a field on a `null` reference, like `name.length()` when `name` is null. You prevent it with null checks, by using `Optional` to make absence explicit, by calling `getOrDefault` on maps instead of `get`, by using `Objects.requireNonNull` to fail fast at method entry, and by writing string comparisons as `"constant".equals(variable)` so the constant is never null. Java 14+ even gives 'helpful NullPointerExceptions' that tell you exactly which reference was null."

---

### Q13. What is `ConcurrentModificationException`?
"It's thrown when you modify a collection while iterating over it with a for-each loop or iterator. The iterator keeps a `modCount` and detects that the structure changed underneath it, then fails fast to avoid undefined behaviour. The fixes are: use the iterator's own `remove()` method, use `removeIf()` from Java 8, iterate over a copy, or use a concurrent collection like `CopyOnWriteArrayList`. The classic trap is calling `list.remove(x)` inside a `for (String x : list)` loop."

---

### Q14. What's the difference between `java.io.File` and `java.nio.file.Path`/`Files`?
"`File` is the original API from Java 1.0 — it's clunky, many methods return a boolean instead of throwing, so failures are easy to ignore. `Path` and the `Files` utility class came in Java 7 as the NIO.2 API and are the modern, preferred choice. `Files` gives you clean one-liners — `Files.readAllLines`, `Files.readString`, `Files.write`, `Files.copy`, `Files.walk` — and throws descriptive `IOException`s instead of silent booleans. It also has better symbolic-link, permission, and large-directory support. For any new code, use `Path`/`Files`."

---

### Q15. Can you catch multiple exceptions, and what is exception chaining?
"Yes — either with multiple `catch` blocks (specific to general) or a single multi-catch block with `IOException | SQLException`. Exception chaining is wrapping a low-level exception inside a higher-level one while preserving the original cause: `throw new ServiceException("Failed to load user", e)`. You pass the original as the *cause* so the full stack trace is retained — you can later retrieve it with `getCause()`. This is essential for translating, say, a `SQLException` into a domain `RepositoryException` without losing the root-cause information for debugging."

---

### Q16. Is it good practice to catch `Exception` or `Throwable` broadly? What's exception swallowing?
"Generally no. Catching `Exception` broadly hides bugs you actually want to see, and catching `Throwable` is worse because it also catches `Error`s like `OutOfMemoryError` that you can't recover from. You should catch the most specific exception you can meaningfully handle. 'Exception swallowing' is an empty catch block — `catch (Exception e) {}` — where the exception vanishes silently and you never learn something failed. At absolute minimum, log it. The principle is: handle what you can recover from, log or rethrow the rest, and never silently swallow."

---

## 🧠 Memory Hooks

| Concept | Vivid analogy | Trigger phrase |
|---|---|---|
| Exception vs Error | Restaurant closed (handle it) vs kitchen on fire (evacuate) | "Closed → catch, Fire → flee" |
| Checked exception | Menu warns "may contain nuts" — you *must* plan | "Compiler nags you" |
| Unchecked exception | You tripped over your own shoelaces | "Your own bug" |
| try-catch-finally | Cook → if it burns order Swiggy → always clean kitchen | "Finally cleans up" |
| try-with-resources | Hotel room auto-cleaned at checkout | "Checkout = auto-close" |
| `throw` vs `throws` | Throw the cockroach now vs menu *declares* nuts | "Now vs warning" |
| `final/finally/finalize` | Locked door / always-runs / dead body cleanup | "Lock, Always, Funeral" |
| `return` in `finally` | The finally block is the bouncer who overrides the manager | "Bouncer wins" |
| Custom exception | A name tag: "InsufficientBalance" beats "error #5" | "Name your pain" |
| Byte vs character stream | Shipping crates (bytes) vs reading a letter (chars) | "Binary crate, text letter" |
| Buffered I/O | Grab the whole stack of mail, not one letter at a time | "Bulk = fast" |
| Serialization | IKEA flat-pack → ship → reassemble | "Flat-pack the object" |
| `transient` | Invisible ink — written, never saved | "Skip me" |
| `serialVersionUID` | Passport number — must match to re-enter | "Version passport" |
| NullPointerException | Phoning a number that doesn't exist | "Null = dead line" |
| ConcurrentModification | Rearranging chairs while people are sitting | "Don't edit mid-loop" |

---

## 🎯 Final Interview Tip

When asked *any* exception/IO question, **always close with the best practice** — it's what makes you sound senior. Say "...and the modern best practice is try-with-resources" or "...and I'd use `Files` from NIO.2, not the old `File` API." Interviewers aren't just testing if you *know* the syntax; they're testing whether you've *felt the pain* of leaked file handles, swallowed exceptions, and `ConcurrentModificationException` in production. Show that scar tissue. Lead with the definition, prove the "why," drop the code, and land the gotcha — every single time. 💪
