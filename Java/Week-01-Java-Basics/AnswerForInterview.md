# Week 1: Java Basics - Your First Steps from Python to Java — Interview Answers & Exercise Solutions

> **The Interview Golden Rule for Java Concepts** 🏆
>
> When an interviewer asks you about *any* Java concept, answer in this exact 4-beat rhythm. It makes you sound senior even on basics:
>
> 1. **Definition** — what it is, in one clean sentence.
> 2. **Why it exists** — the problem it solves (interviewers LOVE this; it shows you understand *intent*, not just syntax).
> 3. **Small code example** — 3-5 lines that prove you've actually used it.
> 4. **Gotcha / trade-off** — the trap most people fall into. Naming this is what separates a candidate who *read* about Java from one who *fought* with it.
>
> Example of the rhythm in action: *"A String is immutable (definition) — Java does this so strings can be safely cached and shared in the String Pool (why) — so `name.toUpperCase()` returns a new String and leaves the old one untouched (example) — which is exactly why beginners get burned when they forget to reassign it (gotcha)."*
>
> Every answer below follows this rhythm. Internalize it. 🎯

---

# PART A — Exercise / Practice Solutions

Every exercise from the Plan.md, fully solved in **Java**, with brief explanations. All code is correct and compilable.

---

## Section 4 — Hello World Exercises

### A4.1 — Print name, age, and city on separate lines

```java
public class AboutMe {
    public static void main(String[] args) {
        System.out.println("Name: Sheetal");
        System.out.println("Age: 25");
        System.out.println("City: Bangalore");
    }
}
```

**Explanation:** Each `println` prints its text *and* moves to a new line, so three calls give three lines. Remember the semicolons — the #1 thing Python folks forget. 🐍➡️☕

---

### A4.2 — Print a triangle pattern using `*`

```java
public class Triangle {
    public static void main(String[] args) {
        int rows = 5;
        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= i; j++) {
                System.out.print("*");   // print (no newline) so stars stay on one line
            }
            System.out.println();        // move to next row
        }
    }
}
```

**Output:**
```
*
**
***
****
*****
```

**Explanation:** Outer loop = which row, inner loop = how many stars in that row. The key trick: `print` keeps stars together, and one `println()` at the end of each row drops to the next line.

---

### A4.3 — Greet a command-line argument

```java
public class Greeter {
    public static void main(String[] args) {
        if (args.length > 0) {
            System.out.println("Hello, " + args[0] + "!");
        } else {
            System.out.println("Hello! (Tip: run with your name, e.g. java Greeter Sheetal)");
        }
    }
}
```

Run it as: `java Greeter Sheetal` → prints `Hello, Sheetal!`

**Explanation:** `args` is the `String[]` array from `main`. `args[0]` is the first word you typed after the class name. The `if (args.length > 0)` guard prevents an `ArrayIndexOutOfBoundsException` when nobody passes a name — always defend your array accesses. 🛡️

---

## Section 5 — Variables and Data Types Exercises

### A5.1 — Swiggy order variables (one of each type)

```java
public class SwiggyOrder {
    public static void main(String[] args) {
        String restaurantName = "Meghana Foods"; // text → String (capital S, it's a class)
        double itemPrice = 349.99;                // money/decimals → double
        int quantity = 2;                         // whole count → int
        boolean isDelivered = false;              // yes/no flag → boolean
        char deliveryPersonInitial = 'R';         // ONE character → char (single quotes!)

        System.out.println("Restaurant: " + restaurantName);
        System.out.println("Price: " + itemPrice);
        System.out.println("Quantity: " + quantity);
        System.out.println("Delivered: " + isDelivered);
        System.out.println("Delivery person initial: " + deliveryPersonInitial);
    }
}
```

**Explanation:** This is the "label your dabbas" exercise. Note `'R'` uses **single** quotes (char), while `"Meghana Foods"` uses **double** quotes (String). Mixing these up is a classic compile error.

---

### A5.2 — Primitive vs reference behavior

```java
public class PrimitiveVsReference {
    public static void main(String[] args) {
        // Primitives store the ACTUAL value → copy is independent
        int a = 10;
        int b = a;   // b gets its own copy of 10
        b = 20;
        System.out.println("a = " + a);  // 10 (unchanged)
        System.out.println("b = " + b);  // 20

        // Arrays store a REFERENCE (address) → both names point to one array
        int[] arr1 = {1, 2, 3};
        int[] arr2 = arr1;     // arr2 points to the SAME array
        arr2[0] = 99;
        System.out.println("arr1[0] = " + arr1[0]);  // 99 (surprise! changed via arr2)
        System.out.println("arr2[0] = " + arr2[0]);  // 99
    }
}
```

**Explanation:** Primitives copy the *value*; references copy the *address*. `arr2 = arr1` does NOT clone the array — it hands arr2 the same locker key arr1 has. This is the single most important mental model in Java memory. 🔑

---

### A5.3 — ArrayList of Integer (and why `ArrayList<int>` fails)

```java
import java.util.ArrayList;

public class IntegerListDemo {
    public static void main(String[] args) {
        // Generics only accept OBJECTS, so we use the wrapper Integer, not int
        ArrayList<Integer> numbers = new ArrayList<>();
        numbers.add(10);   // autoboxing: int 10 → Integer
        numbers.add(20);
        numbers.add(30);
        numbers.add(40);
        numbers.add(50);

        System.out.println(numbers);          // [10, 20, 30, 40, 50]
        int first = numbers.get(0);           // unboxing: Integer → int
        System.out.println("First: " + first);

        // ArrayList<int> numbers = new ArrayList<>();  // ❌ COMPILE ERROR — primitives not allowed
    }
}
```

**Explanation:** Collections store objects only, so you must use `Integer` (wrapper), never `int`. Java auto-converts between them (autoboxing on `add`, unboxing on `get`). The commented line shows the exact error you'd get — keep it as a reminder.

---

## Section 6 — Type Casting Exercises

### A6.1 — Cast a Swiggy bill to int and print the lost amount

```java
public class BillTruncation {
    public static void main(String[] args) {
        double bill = 549.75;
        int rounded = (int) bill;        // truncates to 549 (does NOT round)
        double lost = bill - rounded;    // 0.75

        System.out.println("Original bill: " + bill);
        System.out.println("After (int) cast: " + rounded);
        System.out.println("Amount lost: " + lost);  // 0.75
    }
}
```

**Explanation:** `(int)` **truncates** (chops the decimal), it does NOT round. So `549.75` becomes `549`, losing `0.75`. This is the classic "casting doesn't round" trap. ✂️

---

### A6.2 — Cast 256 to byte (overflow)

```java
public class ByteOverflow {
    public static void main(String[] args) {
        int big = 256;
        byte small = (byte) big;
        System.out.println("256 as byte = " + small);  // 0

        int another = 130;
        System.out.println("130 as byte = " + (byte) another);  // -126
    }
}
```

**Explanation:** A `byte` holds only `-128..127`. `256` wraps around to `0` (256 mod 256), and `130` wraps to `-126`. Java gives **no warning** for overflow — it silently produces a wrong number. Always know your type ranges. 🔄

---

### A6.3 — Fix `int result = 10 / 3;` to get the decimal answer

```java
public class DivisionFix {
    public static void main(String[] args) {
        // ❌ Wrong: int / int = int → 3 (decimal dropped)
        int wrong = 10 / 3;
        System.out.println("Wrong: " + wrong);  // 3

        // ✅ Fix: make at least one operand a double
        double right = 10.0 / 3;       // 3.3333333333333335
        System.out.println("Right: " + right);

        // Alternative fix: cast one operand
        int a = 10, b = 3;
        double right2 = (double) a / b;  // cast FIRST operand, then divide
        System.out.println("Right2: " + right2);
    }
}
```

**Explanation:** When both operands are `int`, Java does integer division and the result is `int`. Force floating-point math by making one side a `double` (either `10.0` or `(double) a`). ⚠️ Watch the order: `(double)(a / b)` would be too late — the integer division `10/3=3` already happened. Cast *before* dividing.

---

## Section 7 — Operators Exercises

### A7.1 — Total bill with 18% GST

```java
public class GstCalculator {
    public static void main(String[] args) {
        double subtotal = 850.0;
        double gst = subtotal * 18 / 100;   // 18% of subtotal
        double total = subtotal + gst;

        System.out.println("Subtotal: " + subtotal);
        System.out.println("GST (18%): " + gst);     // 153.0
        System.out.println("Total: " + total);       // 1003.0
    }
}
```

**Explanation:** `subtotal * 18 / 100` works because `subtotal` is a `double`, so the whole expression stays in floating-point. If `subtotal` were an `int`, you'd risk integer division — another reason `double` is the default for money math.

---

### A7.2 — Ternary for free delivery (order > 199)

```java
public class FreeDelivery {
    public static void main(String[] args) {
        double orderValue = 250.0;
        String message = (orderValue > 199)
                ? "Free delivery! 🎉"
                : "Add more items for free delivery.";
        System.out.println(message);
    }
}
```

**Explanation:** The ternary `condition ? valueIfTrue : valueIfFalse` is Java's one-line if/else. It's the equivalent of Python's `"x" if cond else "y"`. Great for assigning one of two values; don't abuse it for complex logic.

---

### A7.3 — Pre-increment vs post-increment

```java
public class IncrementDemo {
    public static void main(String[] args) {
        int a = 5;
        int b = a++;   // POST: b gets 5 (old value), THEN a becomes 6
        System.out.println("After b = a++  → a=" + a + ", b=" + b);  // a=6, b=5

        int c = 5;
        int d = ++c;   // PRE: c becomes 6 FIRST, THEN d gets 6
        System.out.println("After d = ++c  → c=" + c + ", d=" + d);  // c=6, d=6
    }
}
```

**Explanation:** `a++` (post) returns the value *then* increments; `++a` (pre) increments *then* returns. Memory hook: in `a++`, the `a` comes first in reading order, so you "use a first." In `++a`, the `++` comes first, so you "increment first." 🔢

---

## Section 8 — Scanner I/O Exercises

### A8.1 — Zomato order system (compute the bill)

```java
import java.util.Scanner;

public class ZomatoOrder {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Restaurant name: ");
        String restaurant = scanner.nextLine();

        System.out.print("Item name: ");
        String item = scanner.nextLine();

        System.out.print("Price per item: ");
        double price = scanner.nextDouble();

        System.out.print("Quantity: ");
        int quantity = scanner.nextInt();

        double bill = price * quantity;
        System.out.println("\n----- BILL -----");
        System.out.println("Restaurant: " + restaurant);
        System.out.println(item + " x " + quantity + " = " + bill);

        scanner.close();
    }
}
```

**Explanation:** Note the order — the two `nextLine()` calls come *before* `nextDouble()`/`nextInt()`, so we dodge the infamous newline bug (see A8 note below). Always `scanner.close()` to free the resource.

---

### A8.2 — Simple calculator (two numbers + operator)

```java
import java.util.Scanner;

public class Calculator {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("First number: ");
        double a = scanner.nextDouble();

        System.out.print("Operator (+ - * /): ");
        char op = scanner.next().charAt(0);   // read a token, take its first char

        System.out.print("Second number: ");
        double b = scanner.nextDouble();

        double result;
        switch (op) {
            case '+' -> result = a + b;
            case '-' -> result = a - b;
            case '*' -> result = a * b;
            case '/' -> {
                if (b == 0) {
                    System.out.println("Cannot divide by zero!");
                    scanner.close();
                    return;
                }
                result = a / b;
            }
            default -> {
                System.out.println("Unknown operator: " + op);
                scanner.close();
                return;
            }
        }
        System.out.println("Result: " + result);
        scanner.close();
    }
}
```

**Explanation:** A switch expression with arrows handles each operator. We guard against divide-by-zero explicitly — robust input handling is exactly what interviewers look for. `scanner.next().charAt(0)` grabs a single-character token.

---

### A8.3 — Average of 5 subject marks

```java
import java.util.Scanner;

public class MarksAverage {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        double sum = 0;
        int subjects = 5;

        for (int i = 1; i <= subjects; i++) {
            System.out.print("Marks for subject " + i + ": ");
            sum += scanner.nextDouble();
        }

        double average = sum / subjects;
        System.out.println("Average: " + average);
        scanner.close();
    }
}
```

**Explanation:** Accumulate into `sum` (a `double`, so division stays floating-point), then divide by the count. Looping the input keeps the code tiny instead of five repeated blocks.

> **⚠️ The Scanner `nextLine()` bug (must-know):** After `nextInt()`/`nextDouble()`, the Enter key's `\n` stays in the buffer. A following `nextLine()` reads that leftover and returns `""`. Fix: add a throwaway `scanner.nextLine();` right after the `nextInt()`/`nextDouble()` to consume the newline.

---

## Section 9 — Strings Exercises

### A9.1 — Demonstrate the String Pool

```java
public class StringPoolDemo {
    public static void main(String[] args) {
        String a = "Hello";              // goes in the String Pool
        String b = "Hello";              // REUSES the same pooled object
        String c = new String("Hello");  // NEW object on the heap (not pooled)

        System.out.println("a == b      : " + (a == b));        // true  (same pooled object)
        System.out.println("a == c      : " + (a == c));        // false (different objects)
        System.out.println("a.equals(b) : " + a.equals(b));     // true  (same content)
        System.out.println("a.equals(c) : " + a.equals(c));     // true  (same content)
    }
}
```

**Explanation:** String literals are interned in the pool, so `a` and `b` are literally the same object (`==` true). `new String(...)` forces a fresh heap object, so `a == c` is false even though content matches. Moral: compare String *content* with `.equals()`, never `==`. 🏊

---

### A9.2 — Count vowels in a user-input string

```java
import java.util.Scanner;

public class VowelCounter {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a string: ");
        String input = scanner.nextLine().toLowerCase();

        int count = 0;
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u') {
                count++;
            }
        }
        System.out.println("Vowel count: " + count);
        scanner.close();
    }
}
```

**Explanation:** `toLowerCase()` lets us check only lowercase vowels. We walk the string with `charAt(i)` (Java has no `s[i]` indexing). `length()` is a *method* on strings — parentheses required.

---

### A9.3 — Reverse a string WITHOUT `StringBuilder.reverse()`

```java
public class StringReverse {
    public static void main(String[] args) {
        String input = "Sheetal";
        String reversed = reverse(input);
        System.out.println(input + " → " + reversed);  // Sheetal → lateehS
    }

    public static String reverse(String s) {
        char[] chars = s.toCharArray();
        int left = 0, right = chars.length - 1;
        while (left < right) {
            char temp = chars[left];   // swap the two ends
            chars[left] = chars[right];
            chars[right] = temp;
            left++;
            right--;
        }
        return new String(chars);
    }
}
```

**Explanation:** Convert to a `char[]`, then use the **two-pointer swap** pattern: one pointer from the start, one from the end, swapping and moving inward until they meet. This is the classic interview way to reverse in O(n) time and O(n) space (for the char array).

---

### A9.4 — Check if two strings are anagrams (character sorting)

```java
import java.util.Arrays;

public class AnagramCheck {
    public static void main(String[] args) {
        System.out.println(areAnagrams("listen", "silent"));  // true
        System.out.println(areAnagrams("hello", "world"));    // false
    }

    public static boolean areAnagrams(String a, String b) {
        // Normalize: ignore case and spaces
        a = a.replaceAll("\\s", "").toLowerCase();
        b = b.replaceAll("\\s", "").toLowerCase();

        if (a.length() != b.length()) return false;   // quick reject

        char[] ca = a.toCharArray();
        char[] cb = b.toCharArray();
        Arrays.sort(ca);
        Arrays.sort(cb);
        return Arrays.equals(ca, cb);   // same sorted characters → anagram
    }
}
```

**Explanation:** Two strings are anagrams if their sorted characters match. We normalize (strip spaces, lowercase), early-reject on length mismatch, sort both char arrays, and compare with `Arrays.equals`. (A faster O(n) alternative uses a 26-slot frequency count — good to mention as a follow-up.)

---

## Section 10 — Arrays Exercises

### A10.1 — Highest-rated restaurant from an array of ratings

```java
public class HighestRating {
    public static void main(String[] args) {
        double[] ratings = {4.2, 3.8, 4.7, 4.1, 3.5};

        double highest = ratings[0];
        for (int i = 1; i < ratings.length; i++) {
            if (ratings[i] > highest) {
                highest = ratings[i];
            }
        }
        System.out.println("Highest rating: " + highest);  // 4.7
    }
}
```

**Explanation:** Seed `highest` with the first element, then sweep the rest, updating whenever you find something bigger. Starting at index `1` avoids redundantly comparing element 0 to itself.

---

### A10.2 — Reverse an array in place

```java
import java.util.Arrays;

public class ArrayReverse {
    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 4, 5};

        int left = 0, right = arr.length - 1;
        while (left < right) {
            int temp = arr[left];
            arr[left] = arr[right];
            arr[right] = temp;
            left++;
            right--;
        }
        System.out.println(Arrays.toString(arr));  // [5, 4, 3, 2, 1]
    }
}
```

**Explanation:** Same two-pointer swap as the string reverse, but on the array itself ("in place" = no new array). Note `Arrays.toString(arr)` — printing an array directly gives ugly output like `[I@15db9742`. 🖨️

---

### A10.3 — Second largest element

```java
public class SecondLargest {
    public static void main(String[] args) {
        int[] arr = {12, 35, 1, 10, 34, 1};
        System.out.println("Second largest: " + secondLargest(arr));  // 34
    }

    public static int secondLargest(int[] arr) {
        int largest = Integer.MIN_VALUE;
        int second = Integer.MIN_VALUE;
        for (int num : arr) {
            if (num > largest) {
                second = largest;   // old largest becomes second
                largest = num;
            } else if (num > second && num != largest) {
                second = num;       // new runner-up (skip duplicates of the max)
            }
        }
        return second;
    }
}
```

**Explanation:** Track the top two in a single pass (O(n)). When a new max appears, the old max slides into second place. The `num != largest` check prevents a duplicate of the maximum from being counted as second. Start both at `Integer.MIN_VALUE` so any real value beats them.

---

### A10.4 — Merge two sorted arrays into one sorted array

```java
import java.util.Arrays;

public class MergeSorted {
    public static void main(String[] args) {
        int[] a = {1, 3, 5, 7};
        int[] b = {2, 4, 6, 8};
        System.out.println(Arrays.toString(merge(a, b)));  // [1, 2, 3, 4, 5, 6, 7, 8]
    }

    public static int[] merge(int[] a, int[] b) {
        int[] result = new int[a.length + b.length];
        int i = 0, j = 0, k = 0;

        while (i < a.length && j < b.length) {
            if (a[i] <= b[j]) result[k++] = a[i++];
            else              result[k++] = b[j++];
        }
        while (i < a.length) result[k++] = a[i++];  // drain leftovers from a
        while (j < b.length) result[k++] = b[j++];  // drain leftovers from b
        return result;
    }
}
```

**Explanation:** The classic **merge step from merge sort**. Three pointers: `i` for array a, `j` for array b, `k` for the result. Pick the smaller front element each time, then drain whatever remains. Runs in O(n + m). 🔀

---

## Section 12 — Loops Exercises

### A12.1 — Multiplication table for a given number

```java
public class MultiplicationTable {
    public static void main(String[] args) {
        int n = 7;
        for (int i = 1; i <= 10; i++) {
            System.out.println(n + " x " + i + " = " + (n * i));
        }
    }
}
```

**Explanation:** A single `for` loop from 1 to 10, printing `n * i` each time. The parentheses around `(n * i)` matter: without them, `+` would treat it as string concatenation due to left-to-right evaluation.

---

### A12.2 — Fibonacci up to n terms

```java
public class Fibonacci {
    public static void main(String[] args) {
        int n = 10;
        long a = 0, b = 1;   // long, in case numbers grow large
        System.out.print("Fibonacci: ");
        for (int i = 0; i < n; i++) {
            System.out.print(a + " ");
            long next = a + b;
            a = b;
            b = next;
        }
        System.out.println();  // 0 1 1 2 3 5 8 13 21 34
    }
}
```

**Explanation:** Keep two running values `a` and `b`; each step the next term is their sum, then slide the window forward. Using `long` guards against overflow once terms get big (int overflows around the 47th term).

---

### A12.3 — Number guessing game using do-while

```java
import java.util.Scanner;

public class GuessingGame {
    public static void main(String[] args) {
        int secret = (int) (Math.random() * 100) + 1;  // 1..100
        Scanner scanner = new Scanner(System.in);
        int guess;

        do {
            System.out.print("Guess (1-100): ");
            guess = scanner.nextInt();
            if (guess < secret)      System.out.println("Too low!");
            else if (guess > secret) System.out.println("Too high!");
            else                     System.out.println("Correct! 🎉");
        } while (guess != secret);

        scanner.close();
    }
}
```

**Explanation:** `do-while` is *perfect* here because the player must always guess **at least once** before we can check correctness — the condition is tested at the *bottom*. `Math.random() * 100 + 1` yields 1–100.

---

### A12.4 — Star pyramid pattern

```java
public class StarPattern {
    public static void main(String[] args) {
        int rows = 5;
        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= i; j++) {
                System.out.print("*");
            }
            System.out.println();
        }
    }
}
```

**Output:**
```
*
**
***
****
*****
```

**Explanation:** Identical structure to A4.2 — nested loops where the inner count equals the current row number. Worth memorizing this skeleton; pattern questions are common warm-ups. ⭐

---

## Section 13 — Methods Exercises

### A13.1 — Overloaded `calculateArea` (square / rectangle / circle)

```java
public class AreaCalculator {
    // Square: one int side
    public static int calculateArea(int side) {
        return side * side;
    }

    // Rectangle: length × breadth
    public static int calculateArea(int length, int breadth) {
        return length * breadth;
    }

    // Circle: π × r²  (double parameter distinguishes it from the square version)
    public static double calculateArea(double radius) {
        return Math.PI * radius * radius;
    }

    public static void main(String[] args) {
        System.out.println("Square (5):       " + calculateArea(5));        // 25
        System.out.println("Rectangle (4,6):  " + calculateArea(4, 6));     // 24
        System.out.println("Circle (3.0):     " + calculateArea(3.0));      // 28.27...
    }
}
```

**Explanation:** **Method overloading** — same name, different parameter lists. The compiler picks the right one by the number and types of arguments. Note the circle version takes a `double`, which both differentiates its signature and matches the math.

---

### A13.2 — Check if a number is prime

```java
public class PrimeCheck {
    public static void main(String[] args) {
        System.out.println(isPrime(7));   // true
        System.out.println(isPrime(12));  // false
        System.out.println(isPrime(1));   // false
    }

    public static boolean isPrime(int n) {
        if (n < 2) return false;                 // 0, 1, negatives are not prime
        for (int i = 2; i * i <= n; i++) {       // only check up to √n
            if (n % i == 0) return false;        // found a divisor → not prime
        }
        return true;
    }
}
```

**Explanation:** A number is prime if nothing from 2 up to √n divides it evenly. The `i * i <= n` trick checks only up to the square root — a key efficiency win interviewers like to see (O(√n) instead of O(n)). 🔍

---

### A13.3 — GST by slab (5% / 12% / 18%)

```java
public class GstSlab {
    public static void main(String[] args) {
        System.out.println(applyGst(400));   // 5%  → 420.0
        System.out.println(applyGst(1000));  // 12% → 1120.0
        System.out.println(applyGst(2500));  // 18% → 2950.0
    }

    public static double applyGst(double amount) {
        double rate;
        if (amount < 500)        rate = 0.05;   // under 500 → 5%
        else if (amount <= 2000) rate = 0.12;   // 500–2000 → 12%
        else                     rate = 0.18;   // above 2000 → 18%
        return amount + (amount * rate);
    }
}
```

**Explanation:** Classic if/else-if ladder mapping a value to a tier. Order matters — checks flow top to bottom, and the first matching branch wins. Returning `amount + amount*rate` gives the GST-inclusive total.

---

### A13.4 — Title Case a string

```java
public class TitleCase {
    public static void main(String[] args) {
        System.out.println(toTitleCase("hello world"));        // Hello World
        System.out.println(toTitleCase("java is fun today"));  // Java Is Fun Today
    }

    public static String toTitleCase(String input) {
        String[] words = input.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (words[i].isEmpty()) continue;
            sb.append(Character.toUpperCase(words[i].charAt(0)))  // capitalize first letter
              .append(words[i].substring(1).toLowerCase());       // lowercase the rest
            if (i < words.length - 1) sb.append(" ");
        }
        return sb.toString();
    }
}
```

**Explanation:** Split on spaces, then for each word uppercase the first char and lowercase the remainder. We use `StringBuilder` (not `+`) because we're concatenating in a loop — the efficient choice. `substring(1)` safely handles the "rest of the word."

---

### A13.5 — Recursive factorial

```java
public class Factorial {
    public static void main(String[] args) {
        System.out.println(factorial(5));  // 120
        System.out.println(factorial(0));  // 1
    }

    public static long factorial(int n) {
        if (n <= 1) return 1;           // base case stops the recursion
        return n * factorial(n - 1);    // recursive case
    }
}
```

**Explanation:** Recursion needs two parts: a **base case** (`n <= 1` returns 1, including 0! = 1) that stops the descent, and a **recursive case** that shrinks the problem (`n - 1`). Return type is `long` because factorials explode fast (13! already overflows `int`). 🌀

---

## Week 1 Capstone — Zomato Bill Calculator

A full program combining variables, Scanner, Strings, arrays, if-else, methods, and overloading.

```java
import java.util.Scanner;

public class ZomatoBillCalculator {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Restaurant name: ");
        String restaurant = scanner.nextLine();

        String[] items = new String[3];
        double[] prices = new double[3];
        double subtotal = 0;

        for (int i = 0; i < 3; i++) {
            System.out.print("Item " + (i + 1) + " name: ");
            items[i] = scanner.nextLine();
            System.out.print("Item " + (i + 1) + " price: ");
            prices[i] = scanner.nextDouble();
            scanner.nextLine();          // consume leftover newline (the famous bug fix)
            subtotal += prices[i];
        }

        System.out.print("Order type (delivery/dine-in): ");
        String orderType = scanner.nextLine();

        double gst = calculateGst(subtotal, orderType);   // overloaded-friendly method
        double discount = calculateDiscount(subtotal);
        double total = calculateTotal(subtotal, gst, discount);

        printBill(restaurant, items, prices, subtotal, gst, discount, total);
        scanner.close();
    }

    // GST: 5% for delivery, 12% for dine-in
    public static double calculateGst(double subtotal, String orderType) {
        double rate = orderType.equalsIgnoreCase("delivery") ? 0.05 : 0.12;
        return subtotal * rate;
    }

    // Discount: 20% off above 1000, else 10% off above 500, else nothing
    public static double calculateDiscount(double subtotal) {
        if (subtotal > 1000)      return subtotal * 0.20;
        else if (subtotal > 500)  return subtotal * 0.10;
        else                      return 0;
    }

    public static double calculateTotal(double subtotal, double gst, double discount) {
        return subtotal + gst - discount;
    }

    public static void printBill(String restaurant, String[] items, double[] prices,
                                 double subtotal, double gst, double discount, double total) {
        System.out.println("\n========== BILL ==========");
        System.out.println("Restaurant: " + restaurant);
        System.out.println("--------------------------");
        for (int i = 0; i < items.length; i++) {
            System.out.println(items[i] + " : " + prices[i]);
        }
        System.out.println("--------------------------");
        System.out.printf("Subtotal : %.2f%n", subtotal);
        System.out.printf("GST      : %.2f%n", gst);
        System.out.printf("Discount : -%.2f%n", discount);
        System.out.printf("TOTAL    : %.2f%n", total);
        System.out.println("==========================");
    }
}
```

**Explanation:** Each calculation lives in its own method (separation of concerns), exactly as the project asks. We use `.equalsIgnoreCase()` for the order type (never `==` on strings!), fix the Scanner newline bug after `nextDouble()`, and use `printf` with `%.2f` to format money to two decimals. This single program exercises nearly every Week-1 skill. 🍔

---

# PART B — Interview Questions & Model Answers

Confident, spoken-style answers — the way you'd actually say them in the room. 🎤

---

### Q1. What is Java, and what does "Write Once, Run Anywhere" mean?

*"Java is a compiled, object-oriented language whose code runs on the **JVM** rather than directly on the OS. 'Write Once, Run Anywhere' means I compile my source once into **bytecode** — a platform-neutral format — and that same bytecode runs on any machine that has a JVM, whether it's Windows, Mac, Linux, or Android. The OS-specific translation is the JVM's job, not mine. The trade-off is you need a JVM installed everywhere, but in exchange you never rewrite code per platform — which is exactly the portability problem Java was invented to solve in 1995."*

---

### Q2. Explain the difference between JDK, JRE, and JVM.

*"Think of three nested layers. The **JVM** is the engine that actually executes bytecode and handles things like memory management and garbage collection. The **JRE** is the JVM plus the core libraries — everything you need to *run* a Java app. The **JDK** is the JRE plus developer tools like the compiler `javac` and the debugger — everything you need to *write and build* Java. As a developer I install the JDK; an end user who only runs apps needs just the JRE."*

| Component | Contains | Who needs it |
|-----------|----------|--------------|
| **JVM** | Bytecode execution engine, GC | Everyone (it's inside JRE) |
| **JRE** | JVM + core libraries | People running Java apps |
| **JDK** | JRE + `javac`, debugger, tools | Developers |

---

### Q3. How does a `.java` file actually get executed? Walk me through it.

*"Four steps. First, I write human-readable source in a `.java` file. Second, the compiler `javac` turns it into **bytecode** in a `.class` file — that's not machine code yet, it's an intermediate format. Third, the JVM loads that bytecode and, at runtime, converts it to machine code for the specific CPU it's running on — often using a JIT compiler to optimize hot paths. Fourth, the CPU executes it. So Java is both **compiled** (source → bytecode) and **interpreted/JIT-compiled** (bytecode → machine code), which is why it's faster than a purely interpreted language like Python but still portable."*

---

### Q4. Why does the `main` method signature have to be exactly `public static void main(String[] args)`?

*"Every word is load-bearing. **`public`** so the JVM can call it from outside the class. **`static`** so the JVM can call it *without first creating an object* of my class — there's nothing to instantiate at program start. **`void`** because main returns nothing to the JVM. **`main`** is the exact name the JVM searches for as the entry point — not `Main`, not `run`. And **`String[] args`** receives command-line arguments. If any of these is wrong, the JVM either won't find the entry point or won't be able to invoke it."*

---

### Q5. What's the difference between a primitive type and a reference type?

*"A **primitive** — like `int`, `double`, `char`, `boolean` — stores the actual value directly in its memory slot, can't be null, and has no methods. There are exactly 8 of them. A **reference type** — like `String`, arrays, or any object — stores a *pointer* to where the object lives on the heap; it can be null and has methods. The practical consequence: when I assign one primitive to another, I copy the value, so they're independent. When I assign one reference to another, both point to the *same* object, so mutating through one is visible through the other. That single distinction explains most 'why did my array change?' surprises."*

---

### Q6. What are the 8 primitive types, and which do you use most?

*"`byte`, `short`, `int`, `long` for whole numbers; `float`, `double` for decimals; `char` for a single character; and `boolean` for true/false. In practice I reach for **`int`** for whole numbers, **`double`** for decimals, **`boolean`** for flags, and occasionally **`long`** when a value can exceed about 2.1 billion — like timestamps or large IDs. `byte`, `short`, and `float` are rare in everyday code; I'd use `byte` only for memory-tight large arrays or raw binary data."*

| Type | Size | Common use |
|------|------|-----------|
| `int` | 4 bytes | Default whole number ⭐ |
| `long` | 8 bytes | Big numbers (needs `L` suffix) |
| `double` | 8 bytes | Default decimal ⭐ |
| `float` | 4 bytes | Rare (needs `f` suffix) |
| `char` | 2 bytes | Single character (single quotes) |
| `boolean` | ~1 bit | Flags / conditions |
| `byte` / `short` | 1 / 2 bytes | Memory-tight cases |

---

### Q7. (TRAP) What's the difference between `int` and `Integer`?

*"`int` is a **primitive** — a raw number, fast, can't be null, no methods. `Integer` is the **wrapper class** — an object that boxes an int; it can be null and has methods like `.parseInt()` and `.compareTo()`. Both exist because collections like `ArrayList` can only hold objects, so I can't write `ArrayList<int>` — I have to write `ArrayList<Integer>`. Java auto-converts between them via **autoboxing** (int → Integer) and **unboxing** (Integer → int). The trap: an `Integer` can be `null`, so unboxing a null Integer into an int throws a `NullPointerException` — a sneaky bug."*

---

### Q8. (TRAP) What's the difference between `==` and `.equals()`?

*"`==` compares **references** — it asks 'are these the exact same object in memory?' `.equals()` compares **content** — 'do these objects represent the same value?' For primitives, `==` is correct because there's no object, just a value. For objects, especially Strings, I always use `.equals()`. The classic trap is `new String("hi") == "hi"` returning `false` because they're different objects, even though their content is identical — `.equals()` would return `true`. Rule of thumb: **primitives use `==`, objects use `.equals()`.**"*

| | `==` | `.equals()` |
|---|------|-------------|
| Compares | Memory address (identity) | Content (value) |
| Use for | Primitives | Objects (String, Integer...) |
| `new String("x") == "x"` | `false` ❌ | `true` ✅ |

---

### Q9. (TRAP) Why are `new Integer(5) == new Integer(5)` and `someInteger == anotherInteger` dangerous?

*"`new Integer(5) == new Integer(5)` is `false` because `new` forces two distinct objects, and `==` compares their addresses, not their values. It gets even sneakier with autoboxing: Java caches Integer objects from **-128 to 127**, so `Integer a = 100; Integer b = 100; a == b` is `true`, but `Integer a = 200; b = 200; a == b` is `false` — same code, different result, purely because 200 is outside the cache range. The lesson: never use `==` to compare wrapper objects for value; always use `.equals()`."*

---

### Q10. Why are Strings immutable in Java? What are the benefits?

*"Once created, a String's contents can never change — any 'modifying' method like `toUpperCase()` returns a brand-new String. Java does this for several reasons: **security** (a file path or DB connection string can't be tampered with after a check), **thread-safety** (immutable objects are automatically safe to share across threads), **caching** (the hashcode can be computed once and cached, which makes Strings great HashMap keys), and the **String Pool** (identical literals can be safely shared because nobody can mutate them). The gotcha: people forget to capture the result — `s.trim();` does nothing unless you write `s = s.trim();`."*

---

### Q11. What is the String Pool?

*"The String Pool is a special region of memory where the JVM stores one copy of each unique string **literal**. When I write `String a = "Hello"` and `String b = "Hello"`, both point to the *same* pooled object — that's a memory optimization made safe by immutability. But `new String("Hello")` deliberately bypasses the pool and creates a fresh object on the heap. That's why `a == b` is true but `a == new String("Hello")` is false. I can also force a heap string into the pool with `.intern()`."*

---

### Q12. (TRAP) Explain integer division. What does `7 / 2` give in Java?

*"In Java, `7 / 2` gives **3**, not 3.5 — when both operands are `int`, Java performs integer division and discards the decimal. It behaves like Python's floor division `//`. To get `3.5`, at least one operand must be floating-point: `7.0 / 2` or `(double) 7 / 2`. The classic bug is averaging: `int sum = 7; int avg = sum / 2;` silently loses precision. And watch the cast order — `(double)(7/2)` is still `3.0` because the integer division already happened before the cast."*

---

### Q13. Explain widening vs narrowing conversion (type casting).

*"**Widening** (implicit) is going from a smaller type to a larger one — like `int` to `double`. Java does it automatically because no data is lost; it's like pouring a glass into a bucket. **Narrowing** (explicit) goes from larger to smaller — like `double` to `int` — and Java forces me to cast manually with `(int)` because data *can* be lost. Two big gotchas: casting a double to int **truncates**, it doesn't round, so `(int) 9.99` is `9`; and narrowing can **overflow silently** — `(byte) 130` wraps to `-126` with no warning."*

---

### Q14. What's the difference between `String`, `StringBuilder`, and when would you use each?

*"`String` is immutable, so every concatenation creates a new object. `StringBuilder` is **mutable** — `append()` modifies the same internal buffer in place. For a few concatenations, `+` on Strings is fine and readable. But inside a loop, `String result += x;` creates a new object every iteration — 1000 iterations means ~999 throwaway objects and O(n²) work. There I use `StringBuilder`, which is O(n). Quick rule: **looping concatenation → StringBuilder**; one-off joins → plain `+`. There's also `StringBuffer`, which is the thread-safe (synchronized) version of StringBuilder, but it's slower and rarely needed."*

| | String | StringBuilder | StringBuffer |
|---|--------|---------------|--------------|
| Mutable? | No | Yes | Yes |
| Thread-safe? | Yes (immutable) | No | Yes (synchronized) |
| Best for | Constants, few joins | Heavy loop concatenation | Multithreaded edits (rare) |

---

### Q15. (TRAP) What's the `switch` fall-through behavior, and which types does switch support?

*"In a traditional `switch`, if I forget the `break`, execution **falls through** into the next case and keeps running until it hits a break or the end — which is a bug 99% of the time. The newer arrow syntax `case 1 -> ...` (Java 14+) doesn't fall through, so it's safer. As for types, switch works with `int` (and smaller integer types), `char`, `String`, and `enum` — but **not** `double`, `float`, `long`, or `boolean`. For booleans you just use an if/else."*

---

### Q16. When would you use a `do-while` loop over a `while` loop?

*"Use `do-while` when the body must run **at least once**, because it checks the condition at the *bottom* instead of the top. The textbook example is input validation or an OTP screen — you always let the user enter something once, *then* check if it's valid and loop if not. A regular `while` checks first, so if the condition starts false, the body never runs at all. So: 'run, then check' → `do-while`; 'check, then maybe run' → `while`."*

---

### Q17. Does Java pass arguments by value or by reference?

*"Java is **always pass-by-value** — but the nuance trips people up. For primitives, the *value* is copied, so changes inside a method don't affect the caller's variable. For objects, the **reference** (the address) is copied by value — so the method gets its own copy of the pointer, but that pointer still points to the *same* object. That means I *can* mutate the object's internals (like `arr[0] = 99`) and the caller sees it, but if I reassign the parameter to a new object, the caller's reference is unaffected. Short version: Java passes references *by value*."*

---

### Q18. What is method overloading, and how does it differ from overriding?

*"**Overloading** is having multiple methods with the *same name* but *different parameter lists* in the same class — the compiler picks the right one at **compile time** based on the arguments, like `add(int,int)` versus `add(double,double)`. It's resolved by signature, and return type alone can't distinguish overloads. **Overriding** is when a subclass provides a new implementation of a method it inherited — resolved at **runtime** via polymorphism. Quick contrast: overloading = same name, different args, compile-time; overriding = same signature, parent/child, runtime."*

| | Overloading | Overriding |
|---|-------------|-----------|
| Where | Same class | Subclass redefines parent method |
| Signature | Same name, different params | Identical signature |
| Resolved | Compile time | Runtime |

---

### Q19. (TRAP) Why does Scanner's `nextLine()` sometimes return an empty string after `nextInt()`?

*"Because of a leftover newline in the input buffer. When the user types `25` and presses Enter, the buffer holds `25\n`. `nextInt()` consumes the `25` but **leaves the `\n`**. The next `nextLine()` immediately reads up to that newline and returns an empty string — it never gives the user a chance to type. The fix is to add a throwaway `scanner.nextLine();` right after the `nextInt()` to swallow the leftover newline. It's the single most common Scanner bug for beginners."*

---

### Q20. What's the difference between an array and an ArrayList?

*"An **array** is fixed-size and can hold primitives or objects; you set its length at creation and can't change it. An **ArrayList** is a resizable collection that grows and shrinks dynamically, but it can only hold **objects** — so `ArrayList<Integer>`, never `ArrayList<int>`. Syntax differs too: arrays use `arr[0]` and `arr.length` (a field, no parentheses), while ArrayList uses `list.get(0)` and `list.size()` (a method). I use arrays for fixed, performance-critical data; ArrayList for the 90% of cases where the size changes or I want convenience methods."*

| | Array | ArrayList |
|---|-------|-----------|
| Size | Fixed | Dynamic |
| Holds | Primitives + objects | Objects only |
| Length | `.length` (field) | `.size()` (method) |
| Access | `a[0]` | `a.get(0)` |

---

### Q21. (TRAP) `s.length` vs `s.length()` vs `arr.length` — what's correct?

*"This is a deliberate Java inconsistency that catches everyone. For a **String**, it's `s.length()` — a *method*, with parentheses. For an **array**, it's `arr.length` — a *field*, no parentheses. And an **ArrayList** uses `list.size()`. So: array → `.length` (field), String → `.length()` (method), ArrayList → `.size()`. Just memorize the three; the compiler will remind you, but in an interview you want to say it without flinching."*

---

# 🧠 Memory Hooks

Vivid analogies to lock each concept into long-term memory. When the trigger appears, fire the hook. 🔥

| Concept | 🎯 Hook / Analogy | When it triggers |
|---------|-------------------|------------------|
| **JVM / JRE / JDK** | 🎬 Movie: JVM = projector, JRE = projector + screen + sound (to *watch*), JDK = all that + camera + editing (to *make*) | Asked to explain the three or what to install |
| **Write Once Run Anywhere** | 🗣️ Hindi: speak it in Delhi, Mumbai, or abroad — one language, many cities | "Why Java?" / portability question |
| **Bytecode** | 🛂 Translator at an airport — neutral middle language the JVM interprets per country (OS) | How `.java` runs |
| **Primitive vs Reference** | 🔑 Primitive = the cash in your hand (copy it, independent); Reference = a locker *key* (copy the key, same locker) | Array/object "why did it change?" surprises |
| **int vs Integer** | 📦 `int` = loose item; `Integer` = item in a labeled box (boxing) — boxes can be empty (null) | Collections, NullPointerException on unboxing |
| **String immutability** | 🪨 Carved in stone — to "edit" you carve a *new* stone; the old one is untouched | `s.trim()` did nothing |
| **String Pool** | 💬 Shared WhatsApp message — everyone points to the same "Hello"; `new String` writes a private sticky note | `==` true vs false on strings |
| **`==` vs `.equals()`** | 👯 Twins: `==` asks "are you literally the same person?"; `.equals()` asks "do you look the same?" | Comparing any objects |
| **Integer division** | ✂️ Sharing 7 samosas between 2 people whole → 3 each, the half is dropped | `7/2` gives 3 |
| **Narrowing cast** | 🪣➡️🥛 Pouring a bucket into a glass — water spills (data lost / overflow) | `(int)`, `(byte)` casts |
| **StringBuilder** | 📝 A whiteboard you keep editing vs printing a new page every change | Loop concatenation |
| **switch fall-through** | 🚪 Open doors with no walls between cases — you keep walking until a `break` wall stops you | Missing `break` bugs |
| **do-while** | 📱 OTP screen — you *always* type once, *then* it checks | "At least once" loops |
| **Pass by value** | 📋 Photocopy of a key: copy the key (reference), same locker; but burn your copy (reassign) and the original key still works | Method argument behavior |
| **Method overloading** | 🍔 Zomato "Order": by name, by name+qty, by name+qty+notes — same word, different forms | Same-name-different-params question |
| **Scanner nextLine bug** | 🍽️ Leftover crumb (`\n`) on the plate — next bite (`nextLine`) eats the crumb instead of real food | Empty string after `nextInt()` |
| **`.length` vs `.length()`** | 📏 Array = a ruler with length *printed on it* (field); String = you *ask* its length (method) | Confusing the two |

---

# 🎓 Final Interview Tip

When you hit a question you're unsure about, **narrate your reasoning out loud** instead of going silent. Interviewers are hiring a thinking partner, not a search engine — saying *"I'd expect this to be false because `==` compares references, but let me reason through autoboxing..."* scores higher than a confident wrong one-word answer.

And always close the loop with the **trade-off**. Anyone can recite "Strings are immutable." The candidate who adds *"...which is great for thread-safety and caching, but means heavy concatenation should use StringBuilder to avoid creating throwaway objects"* sounds like someone who has actually shipped code. That one extra sentence — the *why* and the *when-not-to* — is what gets you to the next round. 🚀

> **The golden rhythm, one more time:** Definition → Why it exists → Code example → Gotcha/trade-off. Run every answer through it and you'll sound senior on day one. ☕
