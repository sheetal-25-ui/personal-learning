# Week 1: Java Basics - Your First Steps from Python to Java

---
Nikita Tyagi 

## 1. What is Java and Why Do Companies Like Microsoft Use It?

### What is it?
Java is a programming language that runs on almost any computer, phone, or server in the world. It was created in 1995 by Sun Microsystems (now owned by Oracle) with one big promise: **"Write Once, Run Anywhere."**

### Real-life analogy
Think of Java like Hindi. If you speak Hindi, you can communicate in Delhi, Mumbai, Kolkata, or even in many shops abroad. You don't need to learn a different language for each city. Similarly, Java code written on a Windows laptop runs on a Linux server, a Mac, or even an Android phone — without changes.

Python is more like a local dialect — super comfortable and easy to speak at home, but sometimes you need Hindi (Java) for the bigger, more formal settings.

### Why does it exist? What problem does it solve?
Before Java, if you wrote a program in C for Windows, it wouldn't run on Mac or Linux. You had to rewrite big chunks of code for each operating system. Java solved this by introducing the JVM (we'll cover this next) — a translator that sits between your code and the computer.

### Why do Microsoft, Google, Amazon, Flipkart use Java instead of Python?

| Reason | Explanation |
|--------|-------------|
| **Speed** | Java is 10-50x faster than Python for most tasks. When Flipkart handles 10 crore users during Big Billion Days sale, every millisecond matters. |
| **Type Safety** | Java catches errors BEFORE your code runs (at compile time). Python only catches them WHILE running. Imagine finding a spelling mistake in your Zomato order before it goes to the restaurant vs. after the food arrives wrong. |
| **Multithreading** | Java can handle thousands of tasks at the same time very efficiently. This is why most backend systems at Amazon, Uber, Ola are written in Java. |
| **Mature Ecosystem** | Java has been around for 30+ years. There are libraries, frameworks, and tools for literally everything. Spring Boot (Java's web framework) powers most enterprise backends. |
| **Scaling** | When you go from 1000 users to 10 crore users, Java handles the growth much better than Python. |

### Python comparison
- Python: Great for scripting, data science, quick prototypes, automation
- Java: Great for large-scale backend systems, Android apps, enterprise software
- Both are used heavily in interviews — but Java is the dominant language for system design and backend roles at most big tech companies

---

## 2. JDK, JRE, JVM - "Why Do I Need to Install So Much Stuff?"

### What is it?
When you installed Python, you just ran `pip install` or downloaded one thing and you were done. Java has three layers, and understanding them helps you debug problems later.

### Real-life analogy
Think of it like watching a movie:
- **JVM (Java Virtual Machine)** = The projector. It actually plays (runs) the movie (your code). You never interact with it directly, but without it, nothing works.
- **JRE (Java Runtime Environment)** = The projector + the screen + the sound system. Everything needed to WATCH (run) a movie. If someone gives you a Java app to run, you need JRE.
- **JDK (Java Development Kit)** = The projector + screen + sound system + camera + editing software. Everything needed to MAKE (develop) AND watch a movie. As a developer, you need JDK.

### The hierarchy (important!)
```
JDK (Development Kit)
 |--- JRE (Runtime Environment)
 |     |--- JVM (Virtual Machine)
 |     |--- Core Libraries (String, Math, Collections, etc.)
 |--- Compiler (javac) — converts your .java file to .class file
 |--- Debugger, Documentation tools, etc.
```

### Why does it exist?
**Without JVM:** You'd need to compile your code separately for Windows, Mac, Linux, etc.
**Without JRE:** You'd have no standard libraries. Imagine writing Java without String or ArrayList — you'd have to build everything from scratch.
**Without JDK:** You could run Java programs but not write them. Like having a Kindle but no Microsoft Word.

### How Java code runs (step by step)
```
You write: HelloWorld.java (human-readable code)
        ↓
Compiler (javac): Converts to HelloWorld.class (bytecode — not human-readable, not machine code either)
        ↓
JVM: Reads the .class file and converts bytecode to machine code for YOUR specific computer
        ↓
Your computer executes it
```

### Python comparison
```
Python:  You write code → Python interpreter runs it directly (interpreted)
Java:    You write code → Compiler creates bytecode → JVM runs bytecode (compiled + interpreted)
```

This is why Java is faster — the compilation step optimizes your code before running it.

### Common mistakes
1. **Installing JRE instead of JDK** — You need JDK to write code. JRE is only for running.
2. **Not setting JAVA_HOME** — Many tools need to know where Java is installed. Always set this environment variable.
3. **Multiple Java versions** — Having Java 8 and Java 17 installed can cause confusion. Use a version manager or make sure your PATH points to the right one.

---

## 3. Setting Up Java

### Install JDK
1. Go to https://adoptium.net/ (free, open source JDK)
2. Download JDK 17 or JDK 21 (LTS versions — LTS means "Long Term Support", they'll be maintained for years)
3. Install it
4. Verify: Open terminal and type:
```bash
java --version    # Should show java 17.x.x or 21.x.x
javac --version   # Should show javac 17.x.x or 21.x.x
```

### VS Code Setup
1. Install "Extension Pack for Java" by Microsoft (this installs everything you need)
2. This gives you: syntax highlighting, auto-complete, debugging, running Java files

### Python comparison
```
Python setup: Install Python → done
Java setup:   Install JDK → verify java and javac commands → optionally set JAVA_HOME → install IDE extensions
```

Yes, Java setup is more involved. But once it's done, you rarely touch it again.

---

## 4. First Java Program - Hello World (Every Word Explained)

### The code
```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```

### Line-by-line explanation

**Line 1: `public class HelloWorld {`**
- `public` — This class can be accessed from anywhere. Think of it like a shop with an "Open" sign — anyone can walk in.
- `class` — In Java, ALL code must live inside a class. There's no "loose code" like Python. A class is like a blueprint.
- `HelloWorld` — The name of the class. **MUST match the filename** (HelloWorld.java). If they don't match, Java refuses to compile.
- `{` — Opening brace. In Python, you use indentation. In Java, you use curly braces `{}` to mark code blocks.

**Line 2: `public static void main(String[] args) {`**
This is the most important line. Let's break down EVERY word:
- `public` — This method can be called from outside the class. The JVM needs to call it, so it must be public.
- `static` — This method belongs to the CLASS, not to any specific object. The JVM calls `main` without creating an object of your class. (We'll cover static deeply in Week 2)
- `void` — This method does not return anything. In Python, a function returns `None` by default. In Java, you MUST explicitly say "this returns nothing" using `void`.
- `main` — This is the entry point. When you run a Java program, the JVM looks for a method called exactly `main`. Not `Main`, not `run`, not anything else — it MUST be `main`.
- `String[] args` — An array of strings that holds command-line arguments. If you run `java HelloWorld Sheetal`, then `args[0]` would be "Sheetal".

**Line 3: `System.out.println("Hello, World!");`**
- `System` — A built-in class that gives you access to system-level things
- `out` — The standard output stream (your terminal/console)
- `println` — "print line" — prints text and moves to the next line
- `"Hello, World!"` — The text to print. Strings use double quotes in Java (single quotes are for single characters only!)
- `;` — Every statement in Java ends with a semicolon. **This is the #1 thing Python developers forget.**

### Python comparison
```python
# Python — entire program
print("Hello, World!")
```
```java
// Java — same thing, but with all the ceremony
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```

Yes, Java is more verbose. But that structure gives you safety — the compiler catches errors before you ever run the code.

### How to run it
```bash
# Step 1: Compile (creates HelloWorld.class)
javac HelloWorld.java

# Step 2: Run (JVM executes the bytecode)
java HelloWorld
```

### Common mistakes
1. **Forgetting the semicolon** — Every statement needs `;` at the end
2. **File name doesn't match class name** — `HelloWorld.java` must contain `public class HelloWorld`
3. **Using single quotes for strings** — `'Hello'` is WRONG in Java. Use `"Hello"`. Single quotes are only for single characters: `'H'`
4. **Forgetting `static` in main** — The JVM calls main without creating an object, so it must be static
5. **Writing `print` instead of `println`** — `print` doesn't add a newline, `println` does. Both exist, but beginners usually want `println`

### Practice exercises
1. Write a program that prints your name, age, and city on separate lines
2. Write a program that prints a triangle pattern using `*`
3. Write a program that takes a command-line argument (your name) and prints "Hello, <name>!"

---

## 5. Variables and Data Types

### What is it?
A variable is a named container that stores a value. In Java, you MUST tell the compiler what TYPE of data the variable will hold — before you use it.

### Real-life analogy
Think of variables like labeled dabba (containers) in an Indian kitchen:
- The "Rice" dabba can only hold rice. You can't suddenly put dal in it.
- The "Sugar" dabba can only hold sugar.

In Python, your dabbas are flexible — you can put rice today and dal tomorrow. In Java, once you label a dabba "Rice" (int), it can ONLY hold rice (integers) forever.

### Python vs Java approach
```python
# Python — no type declaration needed
name = "Sheetal"       # Python figures out it's a string
age = 25               # Python figures out it's an integer
price = 99.99          # Python figures out it's a float
is_student = True      # Python figures out it's a boolean
```

```java
// Java — you MUST declare the type
String name = "Sheetal";    // Must say it's a String
int age = 25;               // Must say it's an int
double price = 99.99;       // Must say it's a double
boolean isStudent = true;   // Must say it's a boolean
```

### Primitive Data Types (stored directly in memory)
These are the building blocks. Java has exactly 8 primitive types:

| Type | Size | Range | Example | Use case |
|------|------|-------|---------|----------|
| `byte` | 1 byte | -128 to 127 | `byte b = 100;` | Saving memory in large arrays |
| `short` | 2 bytes | -32,768 to 32,767 | `short s = 30000;` | Rarely used |
| `int` | 4 bytes | -2.1 billion to 2.1 billion | `int age = 25;` | **Most common** for whole numbers |
| `long` | 8 bytes | Very large range | `long pop = 140_00_00_000L;` | Phone numbers, populations (note the L suffix!) |
| `float` | 4 bytes | ~7 decimal digits | `float pi = 3.14f;` | Rarely used (note the f suffix!) |
| `double` | 8 bytes | ~15 decimal digits | `double price = 99.99;` | **Most common** for decimals |
| `char` | 2 bytes | Single character | `char grade = 'A';` | Single characters (uses single quotes!) |
| `boolean` | 1 bit* | true or false | `boolean pass = true;` | Conditions, flags |

*boolean actual storage depends on the JVM implementation

### Reference Types (stored as pointers to memory)
Anything that's not a primitive is a reference type. This includes:
- `String` (capital S — it's a class, not a primitive!)
- Arrays
- Any object you create from a class
- All Collections (ArrayList, HashMap, etc.)

### Primitive vs Reference — WHY does this matter?

```java
// Primitive — stores the ACTUAL value
int a = 10;
int b = a;     // b gets a COPY of 10
b = 20;        // changing b does NOT change a
System.out.println(a);  // Still 10

// Reference — stores the MEMORY ADDRESS
int[] arr1 = {1, 2, 3};
int[] arr2 = arr1;       // arr2 points to the SAME array as arr1
arr2[0] = 99;            // changing arr2 ALSO changes arr1!
System.out.println(arr1[0]);  // 99 (surprise!)
```

### Python comparison
```python
# In Python, EVERYTHING is a reference type
# But for immutable types (int, str), it behaves like a copy
a = 10
b = a
b = 20
print(a)  # Still 10 (because int is immutable in Python)

# For mutable types, same problem as Java
arr1 = [1, 2, 3]
arr2 = arr1
arr2[0] = 99
print(arr1[0])  # 99 (same as Java reference behavior)
```

### int vs Integer — The Autoboxing Confusion

This confuses EVERYONE coming from Python. In Java:
- `int` is a primitive — just a raw number, no methods, no null
- `Integer` is a class (wrapper) — it's an object that holds an int, has methods, can be null

```java
// Primitive — cannot be null, no methods
int x = 5;
// x.toString()  ← ERROR! Primitives don't have methods.
// x = null;     ← ERROR! Primitives can't be null.

// Wrapper — can be null, has methods
Integer y = 5;
y.toString();   // Works! "5"
y = null;       // Works! (useful for "no value" scenarios)
```

### Why do both exist?
- Collections (ArrayList, HashMap) can ONLY store objects, not primitives
- You can't write `ArrayList<int>` — you must write `ArrayList<Integer>`
- Primitives are faster and use less memory
- So Java has both: primitives for speed, wrappers for when you need objects

### Autoboxing and Unboxing
Java automatically converts between primitives and wrappers:
```java
Integer a = 5;      // Autoboxing: int 5 → Integer object (Java does this automatically)
int b = a;          // Unboxing: Integer object → int 5 (Java does this automatically)

// This works seamlessly
ArrayList<Integer> list = new ArrayList<>();
list.add(10);       // Autoboxing: int 10 → Integer
int val = list.get(0);  // Unboxing: Integer → int
```

### Common mistakes
1. **Forgetting `L` suffix for long**: `long x = 9999999999;` gives ERROR. Must be `long x = 9999999999L;`
2. **Forgetting `f` suffix for float**: `float x = 3.14;` gives ERROR. Must be `float x = 3.14f;` (or just use double)
3. **Using `==` with Integer**: `new Integer(5) == new Integer(5)` is `false`! (comparing references, not values). Use `.equals()`.
4. **Null with primitives**: `int x = null;` is ERROR. Use `Integer x = null;` if you need null.

### Practice exercises
1. Create variables for a Swiggy order: restaurant name (String), item price (double), quantity (int), is_delivered (boolean), delivery person initial (char)
2. Demonstrate the difference between primitive and reference types using two int variables and two arrays
3. Create an ArrayList of Integer and add 5 numbers. Try creating ArrayList of int and see the error.

---

## 6. Type Casting

### What is it?
Converting a value from one data type to another. Like converting rupees to dollars — the value changes form but represents the same thing.

### Implicit casting (widening) — Safe, automatic
When you put a small type into a bigger type, Java does it automatically. No data is lost.
```
byte → short → int → long → float → double
```

```java
int myInt = 100;
double myDouble = myInt;    // Automatically converts 100 to 100.0
System.out.println(myDouble);  // 100.0

// Like pouring a small glass of water into a big bucket — nothing spills
```

### Explicit casting (narrowing) — Risky, you must do it manually
When you put a big type into a smaller type, data might be lost. Java forces you to be explicit.

```java
double myDouble = 9.78;
int myInt = (int) myDouble;    // You MUST write (int) to force it
System.out.println(myInt);     // 9 (the .78 is LOST, not rounded!)

// Like pouring a bucket into a small glass — water spills (data is lost)
```

### Python comparison
```python
# Python — much more relaxed
x = int(9.78)     # Explicit: 9
y = float(5)      # Explicit: 5.0
# Python also does some implicit conversions:
result = 5 + 3.2  # int + float = float (7.2) automatically
```

```java
// Java — same behavior but stricter syntax
int x = (int) 9.78;      // Must cast explicitly
double y = 5;             // Implicit (int to double is safe)
double result = 5 + 3.2;  // int + double = double (7.2) automatically
```

### When things go wrong
```java
int bigNumber = 130;
byte smallByte = (byte) bigNumber;
System.out.println(smallByte);  // -126 (OVERFLOW! byte can only hold -128 to 127)

// This is like trying to fit a 6-digit PIN into a 3-digit lock — it wraps around
```

### Common mistakes
1. **Assuming casting rounds**: `(int) 9.99` gives `9`, not `10`. It truncates (chops off decimals).
2. **Integer division**: `int result = 7 / 2;` gives `3`, not `3.5`. Both operands are int, so result is int. Fix: `double result = 7.0 / 2;`
3. **Overflow without error**: Java doesn't warn you about overflow. `(byte) 200` silently gives a wrong answer.

### Practice exercises
1. Convert a double Swiggy bill (549.75) to int and print the "lost" amount
2. Show what happens when you cast 256 to byte (overflow example)
3. Fix this code: `int result = 10 / 3;` to get the correct decimal answer

---

## 7. Operators

### Arithmetic Operators
```java
int a = 10, b = 3;

System.out.println(a + b);   // 13 (addition)
System.out.println(a - b);   // 7  (subtraction)
System.out.println(a * b);   // 30 (multiplication)
System.out.println(a / b);   // 3  (integer division — NOT 3.33!)
System.out.println(a % b);   // 1  (modulus — remainder)
```

**IMPORTANT: Integer division in Java drops the decimal part!**
```java
int result = 7 / 2;          // 3 (not 3.5!)
double result2 = 7.0 / 2;    // 3.5 (at least one operand must be double)
```

### Python comparison
```python
# Python 3
7 / 2    # 3.5 (always gives float)
7 // 2   # 3   (floor division)
```
```java
// Java
7 / 2    // 3   (integer division — like Python's //)
7.0 / 2  // 3.5 (float division)
// Java has NO // operator
```

### Comparison Operators
```java
int a = 10, b = 20;

a == b    // false (equal to)
a != b    // true  (not equal to)
a > b     // false (greater than)
a < b     // true  (less than)
a >= b    // false (greater than or equal)
a <= b    // true  (less than or equal)
```

### Logical Operators
```java
boolean isRaining = true;
boolean hasUmbrella = false;

isRaining && hasUmbrella    // false (AND — both must be true)
isRaining || hasUmbrella    // true  (OR — at least one must be true)
!isRaining                  // false (NOT — flips the value)
```

### Python comparison
```python
# Python uses words
True and False    # False
True or False     # True
not True          # False
```
```java
// Java uses symbols
true && false    // false
true || false    // true
!true            // false
```

### Ternary Operator (Java's one-line if-else)
```java
int age = 20;
String status = (age >= 18) ? "Adult" : "Minor";
// If age >= 18, status = "Adult", else status = "Minor"
```

### Python comparison
```python
status = "Adult" if age >= 18 else "Minor"
```

### Increment/Decrement (Java has these, Python doesn't!)
```java
int count = 5;
count++;     // count is now 6 (same as count = count + 1)
count--;     // count is now 5 (same as count = count - 1)

// Pre vs Post increment (interview question!)
int a = 5;
int b = a++;   // b = 5, a = 6 (post: uses THEN increments)
int c = ++a;   // c = 7, a = 7 (pre: increments THEN uses)
```

### Practice exercises
1. Write a program that calculates the total bill with 18% GST using arithmetic operators
2. Use the ternary operator to check if a Zomato order qualifies for free delivery (order > 199)
3. Demonstrate pre-increment vs post-increment with a clear example

---
End Day 1
## 8. Input/Output - Scanner Class

### What is it?
Scanner is Java's way of reading user input from the keyboard. It's like Python's `input()` function, but more complex because Java needs to know what TYPE of input you're expecting.

### Real-life analogy
Think of Scanner like a waiter at a restaurant. In Python, the waiter just takes whatever you say and writes it down as a string. In Java, the waiter asks: "Is that a number? A word? A sentence?" and writes it down accordingly.

### The code
```java
import java.util.Scanner;  // Must import Scanner (like Python's import)

public class InputExample {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);  // Create a Scanner that reads from keyboard

        System.out.print("Enter your name: ");
        String name = scanner.nextLine();  // Reads entire line (like Python's input())

        System.out.print("Enter your age: ");
        int age = scanner.nextInt();  // Reads an integer specifically

        System.out.print("Enter your CGPA: ");
        double cgpa = scanner.nextDouble();  // Reads a double specifically

        System.out.println("Hello " + name + "! Age: " + age + ", CGPA: " + cgpa);

        scanner.close();  // Always close the scanner when done
    }
}
```

### Scanner methods (which to use when)
| Method | Reads | Python equivalent |
|--------|-------|-------------------|
| `nextLine()` | Entire line as String | `input()` |
| `next()` | Single word (stops at space) | `input().split()[0]` |
| `nextInt()` | Integer | `int(input())` |
| `nextDouble()` | Double | `float(input())` |
| `nextBoolean()` | Boolean (true/false) | No direct equivalent |

### Python comparison
```python
# Python — simple and clean
name = input("Enter your name: ")
age = int(input("Enter your age: "))
```
```java
// Java — more verbose but type-safe
Scanner scanner = new Scanner(System.in);
System.out.print("Enter your name: ");
String name = scanner.nextLine();
System.out.print("Enter your age: ");
int age = scanner.nextInt();
```

### The INFAMOUS nextLine() bug (MUST KNOW)
This is the single most common bug when using Scanner:

```java
Scanner scanner = new Scanner(System.in);

System.out.print("Enter age: ");
int age = scanner.nextInt();        // User types 25 and hits Enter

System.out.print("Enter name: ");
String name = scanner.nextLine();   // SKIPS this! name = "" (empty string)

System.out.println("Name: " + name);  // Prints "Name: "
```

**Why?** When you type `25` and press Enter, the input is `25\n`. `nextInt()` reads `25` but leaves `\n` in the buffer. `nextLine()` then reads that leftover `\n` and thinks you entered an empty line.

**The fix — add an extra `nextLine()` to consume the leftover newline:**
```java
int age = scanner.nextInt();
scanner.nextLine();  // Consume the leftover \n
String name = scanner.nextLine();  // Now this works correctly
```

### Common mistakes
1. **The nextLine() bug** described above — almost every beginner hits this
2. **Not importing Scanner** — `import java.util.Scanner;` is required
3. **Not closing Scanner** — always call `scanner.close()` to free resources
4. **Type mismatch** — if the user types "abc" when `nextInt()` is waiting, you get `InputMismatchException`

### Practice exercises
1. Build a Zomato order system: take restaurant name, item name, price, quantity as input and print the bill
2. Build a simple calculator that takes two numbers and an operator (+, -, *, /) as input
3. Build a program that takes a student's 5 subject marks and calculates the average

---

## 9. Strings in Java (MAJOR Interview Topic)

### What is it?
A String in Java is a sequence of characters — like words and sentences. But unlike Python strings, Java Strings have some very specific behaviors that interviewers LOVE to ask about.

### The most important thing: Strings are IMMUTABLE
Once a String is created in Java, it can NEVER be changed. Any "modification" actually creates a NEW String.

```java
String name = "Sheetal";
name.toUpperCase();          // Does NOT change name!
System.out.println(name);   // Still "Sheetal"

name = name.toUpperCase();  // Creates NEW string "SHEETAL" and reassigns
System.out.println(name);   // Now "SHEETAL"
```

### Python comparison — strings are immutable in both!
```python
name = "Sheetal"
name.upper()       # Returns "SHEETAL" but doesn't change name
name = name.upper()  # Creates new string and reassigns — SAME as Java
```

This part is actually the same! The difference is in HOW Java stores strings.

### The String Pool (INTERVIEW FAVORITE)

Java has a special memory area called the "String Pool" that stores one copy of each unique string literal to save memory.

```java
String a = "Hello";        // Created in String Pool
String b = "Hello";        // Reuses the SAME object from String Pool
String c = new String("Hello");  // Creates a NEW object in heap (NOT in pool)

System.out.println(a == b);    // true  (same object in pool)
System.out.println(a == c);    // false (different objects!)
System.out.println(a.equals(c));  // true (same content)
```

### Real-life analogy
Think of the String Pool like a shared WhatsApp group:
- `String a = "Hello"` is like posting "Hello" in the group. Everyone sees the same message.
- `String b = "Hello"` — another person wants to say "Hello"? They don't post again. They point to the existing message.
- `new String("Hello")` is like writing "Hello" on a separate piece of paper and keeping it private. Same text, different paper.

### == vs .equals() (ASKED IN EVERY INTERVIEW)

| Operator | What it compares | Use for |
|----------|-----------------|---------|
| `==` | Memory addresses (are they the SAME object?) | Primitives (int, double, etc.) |
| `.equals()` | Content (do they have the SAME value?) | Objects (String, Integer, etc.) |

```java
String s1 = "Hello";
String s2 = "Hello";
String s3 = new String("Hello");

// == compares addresses
s1 == s2    // true  (both point to same pool object)
s1 == s3    // false (different objects in memory)

// .equals() compares content
s1.equals(s2)    // true  (same content)
s1.equals(s3)    // true  (same content)
```

**Rule: ALWAYS use .equals() to compare Strings. NEVER use ==.**

### Python comparison
```python
# Python's == compares values (like Java's .equals())
s1 = "Hello"
s2 = "Hello"
s1 == s2    # True (compares content, like Java's .equals())

# Python's 'is' compares identity (like Java's ==)
s1 is s2    # True (CPython interns small strings, similar to String Pool)
```

### Important String methods
```java
String s = "Hello, World!";

s.length()              // 13 (not len(s) like Python!)
s.charAt(0)             // 'H' (not s[0] like Python!)
s.substring(0, 5)       // "Hello" (like Python's s[0:5])
s.toLowerCase()         // "hello, world!"
s.toUpperCase()         // "HELLO, WORLD!"
s.trim()                // Removes leading/trailing spaces
s.contains("World")     // true
s.indexOf("World")      // 7
s.replace("World", "Java")  // "Hello, Java!"
s.split(", ")           // String array: ["Hello", "World!"]
s.startsWith("Hello")   // true
s.endsWith("!")         // true
s.isEmpty()             // false
s.equals("Hello, World!")  // true
s.equalsIgnoreCase("hello, world!")  // true
```

### String concatenation
```java
// Using + operator (simple but creates many objects for many concatenations)
String greeting = "Hello" + " " + "World";

// Using StringBuilder (efficient for many concatenations — interview topic!)
StringBuilder sb = new StringBuilder();
sb.append("Hello");
sb.append(" ");
sb.append("World");
String result = sb.toString();  // "Hello World"
```

**Why StringBuilder?** When you do `"a" + "b" + "c" + "d"`, Java creates intermediate strings "ab", "abc" before "abcd". With 1000 concatenations, that's 999 wasted strings. StringBuilder modifies in place — much faster.

### Common mistakes
1. **Using == instead of .equals()** — #1 most common Java mistake
2. **Forgetting strings are immutable** — `s.toUpperCase()` alone doesn't change `s`
3. **Using `s.length` instead of `s.length()`** — it's a method, not a property (unlike arrays which use `.length` without parentheses!)
4. **Indexing with []** — Java strings don't support `s[0]`. Use `s.charAt(0)`

### Practice exercises
1. Demonstrate the String Pool: create 3 strings ("Hello" literal, "Hello" literal, new String("Hello")) and compare with both == and .equals()
2. Write a program to count the number of vowels in a user-input string
3. Write a program that reverses a string without using StringBuilder.reverse()
4. Write a program that checks if two strings are anagrams (use character sorting)

---

## 10. Arrays

### What is it?
An array is a container that holds a FIXED number of elements of the SAME type. Once you create an array of size 5, it's always size 5 — you can't grow or shrink it.

### Real-life analogy
Think of an array like an egg tray. A 12-egg tray can hold exactly 12 eggs. You can't suddenly make it hold 15. You can't put a banana in an egg slot. And if you have only 3 eggs, the other 9 slots are still there (with default values).

### Python comparison
```python
# Python lists are flexible
my_list = [1, 2, 3]
my_list.append(4)      # Can grow dynamically
my_list.append("hello")  # Can mix types
```
```java
// Java arrays are fixed
int[] myArray = new int[3];  // Fixed size of 3, only ints
myArray[0] = 1;
myArray[1] = 2;
myArray[2] = 3;
// myArray[3] = 4;  ← ERROR! ArrayIndexOutOfBoundsException
// Can't add "hello" — it only holds ints
```

### Three ways to create arrays
```java
// Method 1: Declare size, fill later
int[] numbers = new int[5];  // [0, 0, 0, 0, 0] — filled with default values
numbers[0] = 10;
numbers[1] = 20;

// Method 2: Declare and initialize together
int[] scores = {85, 90, 78, 92, 88};  // Size is automatically 5

// Method 3: Using new keyword with values
int[] marks = new int[]{85, 90, 78};
```

### Default values (Java fills empty array slots)
```java
int[] nums = new int[3];       // [0, 0, 0]
double[] prices = new double[3];  // [0.0, 0.0, 0.0]
boolean[] flags = new boolean[3]; // [false, false, false]
String[] names = new String[3];   // [null, null, null]
```

**Python comparison:** Python lists don't have default values. You get what you put in.

### Iterating through arrays
```java
int[] marks = {85, 90, 78, 92, 88};

// Method 1: Traditional for loop
for (int i = 0; i < marks.length; i++) {
    System.out.println("Mark " + i + ": " + marks[i]);
}

// Method 2: Enhanced for-each loop (cleaner, no index)
for (int mark : marks) {
    System.out.println("Mark: " + mark);
}
```

### Python comparison
```python
marks = [85, 90, 78, 92, 88]

# Python for loop
for mark in marks:
    print(f"Mark: {mark}")

# Python with index
for i, mark in enumerate(marks):
    print(f"Mark {i}: {mark}")
```

### int[] vs ArrayList (preview)
| Feature | int[] (Array) | ArrayList<Integer> |
|---------|--------------|-------------------|
| Size | Fixed at creation | Grows dynamically |
| Types | Primitives and objects | Objects only |
| Syntax | `int[] a = new int[5];` | `ArrayList<Integer> a = new ArrayList<>();` |
| Access | `a[0]` | `a.get(0)` |
| Length | `a.length` (no parentheses!) | `a.size()` (with parentheses!) |

You'll use ArrayList 90% of the time in real projects. Arrays are mostly for interviews and performance-critical code.

### Common mistakes
1. **ArrayIndexOutOfBoundsException** — accessing index 5 in an array of size 5 (valid indices are 0-4)
2. **Confusing `.length` and `.length()`** — arrays use `.length` (property), strings use `.length()` (method)
3. **Trying to print an array directly** — `System.out.println(arr)` prints a weird code like `[I@15db9742`. Use `Arrays.toString(arr)` instead!
4. **Trying to resize an array** — you can't. Create a new bigger array and copy elements.

### Practice exercises
1. Create an array of 5 Zomato restaurant ratings and find the highest rated
2. Write a program to reverse an array in place (without creating a new array)
3. Write a program that finds the second largest element in an array
4. Merge two sorted arrays into one sorted array

---

## 11. Control Flow

### if / else if / else
```java
int score = 75;

if (score >= 90) {
    System.out.println("Grade A");
} else if (score >= 80) {
    System.out.println("Grade B");
} else if (score >= 70) {
    System.out.println("Grade C");
} else {
    System.out.println("Fail");
}
```

### Python comparison
```python
if score >= 90:
    print("Grade A")
elif score >= 80:
    print("Grade B")
elif score >= 70:
    print("Grade C")
else:
    print("Fail")
```

Key differences:
- Java uses `else if`, Python uses `elif`
- Java uses curly braces `{}`, Python uses indentation
- Java conditions must be in parentheses `()`, Python doesn't require them

### switch statement (Java-specific)
Java's switch is different from Python's match (Python 3.10+). It's great when you're comparing one variable against many values.

```java
int day = 3;

switch (day) {
    case 1:
        System.out.println("Monday");
        break;  // Without break, it "falls through" to the next case!
    case 2:
        System.out.println("Tuesday");
        break;
    case 3:
        System.out.println("Wednesday");
        break;
    default:
        System.out.println("Other day");
}
```

**The modern way (Java 14+) — switch expressions:**
```java
String dayName = switch (day) {
    case 1 -> "Monday";
    case 2 -> "Tuesday";
    case 3 -> "Wednesday";
    default -> "Other day";
};
```

### Common mistakes with switch
1. **Forgetting `break`** — without break, execution falls through to the next case. This is a bug 99% of the time.
2. **Not handling default** — always include a default case
3. **Types allowed** — switch works with int, char, String, enum — NOT with double or boolean

---

## 12. Loops

### for loop
```java
// Print numbers 1 to 5
for (int i = 1; i <= 5; i++) {
    System.out.println(i);
}
// Breakdown: (initialize; condition; update)
// int i = 1    → Start at 1
// i <= 5       → Keep going while i is 5 or less
// i++          → After each loop, add 1 to i
```

### Python comparison
```python
for i in range(1, 6):    # Python
    print(i)
```
```java
for (int i = 1; i <= 5; i++) {  // Java — more explicit
    System.out.println(i);
}
```

### while loop
```java
int count = 0;
while (count < 5) {
    System.out.println("Count: " + count);
    count++;
}
```

### do-while loop (Java has this, Python doesn't!)
The body executes AT LEAST ONCE, even if the condition is false.

```java
int num = 10;
do {
    System.out.println("Number: " + num);  // Prints once even though 10 > 5
    num++;
} while (num <= 5);
```

### Real-life analogy for do-while
Think of it like an OTP verification screen on Paytm:
- You ALWAYS get to enter the OTP at least once (the "do" part)
- Then it checks if it's correct (the "while" part)
- If wrong, you try again

A regular while loop would check BEFORE letting you type — which makes no sense for OTP entry.

### Enhanced for-each loop
```java
String[] cities = {"Mumbai", "Delhi", "Bangalore", "Chennai"};
for (String city : cities) {
    System.out.println(city);
}
// Read as: "for each String city in cities"
```

### Python comparison
```python
cities = ["Mumbai", "Delhi", "Bangalore", "Chennai"]
for city in cities:   # Python's for loop is like Java's enhanced for
    print(city)
```

### break and continue
```java
// break — exit the loop entirely
for (int i = 0; i < 10; i++) {
    if (i == 5) break;  // Stop at 5
    System.out.println(i);  // Prints 0, 1, 2, 3, 4
}

// continue — skip this iteration, go to next
for (int i = 0; i < 10; i++) {
    if (i % 2 == 0) continue;  // Skip even numbers
    System.out.println(i);  // Prints 1, 3, 5, 7, 9
}
```

### Practice exercises
1. Print a multiplication table for a given number (like the ones in school notebooks)
2. Write a program that prints the Fibonacci sequence up to n terms
3. Write a number guessing game using do-while (generate random number, let user guess until correct)
4. Print a pattern:
```
*
**
***
****
*****
```

---

## 13. Methods (Functions)

### What is it?
A method is a block of code that performs a specific task. In Python, you call them "functions." In Java, they're called "methods" because they must always belong to a class.

### Python comparison — key differences
```python
# Python — no return type needed, no access modifier needed
def add(a, b):
    return a + b

def greet(name):
    print(f"Hello, {name}")
```

```java
// Java — must specify return type and access modifier
public static int add(int a, int b) {  // Returns int
    return a + b;
}

public static void greet(String name) {  // Returns nothing (void)
    System.out.println("Hello, " + name);
}
```

### Anatomy of a Java method
```java
public static int calculateTotal(int price, int quantity) {
//│       │      │      │              │
//│       │      │      │              └─ Parameters (with types!)
//│       │      │      └─ Method name
//│       │      └─ Return type (int, String, void, etc.)
//│       └─ Static (belongs to class, not object)
//└─ Access modifier (who can call this)
}
```

### Return types — Java REQUIRES them
```java
// Returns an int
public static int square(int n) {
    return n * n;
}

// Returns a String
public static String getGreeting(String name) {
    return "Hello, " + name;
}

// Returns nothing — void
public static void printMessage(String msg) {
    System.out.println(msg);
    // No return statement needed (or just: return;)
}

// Returns a boolean
public static boolean isAdult(int age) {
    return age >= 18;
}
```

### Method Overloading (Java can, Python can't easily)
In Java, you can have multiple methods with the SAME name but DIFFERENT parameters. The compiler picks the right one based on what arguments you pass.

```java
// All three methods are named "add" — this is overloading
public static int add(int a, int b) {
    return a + b;
}

public static double add(double a, double b) {
    return a + b;
}

public static int add(int a, int b, int c) {
    return a + b + c;
}

// Java picks the right one automatically
add(5, 10);        // Calls the first one (int, int)
add(5.5, 10.5);    // Calls the second one (double, double)
add(1, 2, 3);      // Calls the third one (int, int, int)
```

### Real-life analogy for method overloading
Think of a Zomato restaurant that has different versions of "Order":
- Order with just food name: `order("Biryani")`
- Order with food name and quantity: `order("Biryani", 2)`
- Order with food name, quantity, and special instructions: `order("Biryani", 2, "Extra spicy")`

All are "ordering," but the restaurant handles each differently based on what information you provide.

### Python comparison for overloading
```python
# Python can't truly overload — uses default parameters instead
def add(a, b, c=0):
    return a + b + c

add(5, 10)      # Works: c defaults to 0
add(5, 10, 15)  # Works: c is 15
# But you CAN'T have different types like Java's overloading
```

### Passing data — Java is "pass by value"
```java
// Primitives — changes DON'T affect the original
public static void changeValue(int x) {
    x = 100;  // Only changes the local copy
}

int num = 5;
changeValue(num);
System.out.println(num);  // Still 5!

// Objects/Arrays — the REFERENCE is copied, so changes to the OBJECT affect the original
public static void changeArray(int[] arr) {
    arr[0] = 100;  // Changes the actual array (because we modify through the reference)
}

int[] myArr = {1, 2, 3};
changeArray(myArr);
System.out.println(myArr[0]);  // 100 (changed!)
```

### Common mistakes
1. **Forgetting return type** — `public static add(int a, int b)` is ERROR. Must be `public static int add(int a, int b)`
2. **Forgetting to return a value** — if return type is `int`, EVERY code path must return an int
3. **Calling a non-static method from main** — `main` is static, so it can only directly call other static methods. This confuses every beginner.
4. **Thinking Java has named/keyword arguments** — Java doesn't support `add(b=5, a=3)` like Python does

### Practice exercises
1. Write overloaded methods: `calculateArea(int side)` for square, `calculateArea(int length, int breadth)` for rectangle, `calculateArea(double radius)` for circle
2. Write a method that checks if a number is prime
3. Write a method that takes a Swiggy bill amount and returns the amount after applying the right GST slab (5% for under 500, 12% for 500-2000, 18% for above 2000)
4. Write a method that takes a String and returns it in Title Case ("hello world" -> "Hello World")
5. Write a recursive method to calculate factorial

---

## Week 1 Summary: Java vs Python Quick Reference

| Concept | Python | Java |
|---------|--------|------|
| Print | `print("Hi")` | `System.out.println("Hi");` |
| Input | `input("Name: ")` | `scanner.nextLine()` |
| Variable | `x = 5` | `int x = 5;` |
| String compare | `==` | `.equals()` |
| Array/List | `[1,2,3]` (flexible) | `int[] a = {1,2,3};` (fixed) |
| If/else | `if x > 5:` | `if (x > 5) {` |
| For loop | `for i in range(5):` | `for (int i = 0; i < 5; i++) {` |
| Function | `def add(a, b):` | `public static int add(int a, int b) {` |
| No return | Returns None implicitly | Must declare `void` explicitly |
| Line ending | Nothing | `;` (semicolon) |
| Block syntax | Indentation | `{ }` curly braces |
| Boolean | `True`, `False` | `true`, `false` (lowercase!) |
| Null | `None` | `null` |
| Comments | `# comment` | `// comment` or `/* multi-line */` |
| Overloading | Not supported natively | Supported (same name, diff params) |

---

## Practice Project for Week 1

**Build a "Zomato Bill Calculator"**

Requirements:
1. Take user input: restaurant name, 3 food items with prices
2. Calculate subtotal
3. Apply GST based on restaurant type (5% for delivery, 12% for dine-in)
4. Apply a discount: 10% off if subtotal > 500, 20% off if subtotal > 1000
5. Print a formatted bill with restaurant name, items, subtotal, GST, discount, and final total
6. Use methods for each calculation (separate methods for GST, discount, total)

This project uses: variables, data types, Scanner, Strings, arrays, if-else, methods, and method overloading.
