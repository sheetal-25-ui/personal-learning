# Week 1: Arrays & Strings - Deep Concept Guide

---

## PART 1: ARRAYS

---

### 1. What is an Array?

**Plain English:**
An array is a collection of elements stored in a fixed-size, ordered container where every
element has a numbered position (called an index).

**Real-Life Analogy:**
Think of a row of numbered lockers in a school corridor.

```
  Locker#0   Locker#1   Locker#2   Locker#3   Locker#4
 +--------+ +--------+ +--------+ +--------+ +--------+
 | Pencil | |  Book  | | Bottle | | Lunch  | | Phone  |
 +--------+ +--------+ +--------+ +--------+ +--------+
```

Key observations:
- Each locker has a FIXED NUMBER (index) starting from 0.
- You can go DIRECTLY to locker #3 without opening #0, #1, #2 first.
- The total number of lockers is FIXED. If you built 5 lockers, you cannot
  magically add a 6th without tearing down the wall and rebuilding.
- Every locker holds the SAME TYPE of item (all integers, all strings, etc.).

That direct access -- walking straight to locker #3 -- is what we call
**O(1) random access**. It is the single most powerful feature of arrays.

---

### 2. Why Do Arrays Exist?

Arrays solve the most fundamental problem in programming:
**How do I store multiple values of the same type and access any one of them instantly?**

Without arrays, you would need separate variables:
```java
int marks0 = 85;
int marks1 = 90;
int marks2 = 78;
// ... imagine doing this for 1000 students!
```

With an array:
```java
int[] marks = {85, 90, 78, 92, 88};  // all in one container
System.out.println(marks[3]);         // instantly get the 4th student's marks
```

---

### 3. How Arrays Are Stored in Memory

This is where the magic of O(1) access comes from.

**Contiguous Memory Allocation:**
When you create `int[] nums = new int[5]`, Java asks the operating system for
a CONTINUOUS block of memory -- all 5 slots sit RIGHT NEXT TO EACH OTHER in RAM.

```
Memory Address:  1000    1004    1008    1012    1016
                +-------+-------+-------+-------+-------+
  nums[] =      |  10   |  20   |  30   |  40   |  50   |
                +-------+-------+-------+-------+-------+
Index:            [0]     [1]     [2]     [3]     [4]

Each int = 4 bytes, so addresses increase by 4.
```

**Why O(1) access?**
To find element at index `i`, the CPU does ONE math operation:

```
address = base_address + (index x element_size)
```

Example: To access `nums[3]`:
```
address = 1000 + (3 x 4) = 1012
```

No searching. No scanning. Just math. That is why array access is O(1).

**Why insert/delete in the middle is O(n)?**
Because elements are contiguous, inserting at position 2 means EVERY element
after position 2 must SHIFT RIGHT to make room:

```
Before insert 99 at index 2:
[10, 20, 30, 40, 50]

Step 1: Shift 50 right    [10, 20, 30, 40, __, 50]
Step 2: Shift 40 right    [10, 20, 30, __, 40, 50]
Step 3: Shift 30 right    [10, 20, __, 30, 40, 50]
Step 4: Place 99           [10, 20, 99, 30, 40, 50]

That's 3 shifts for n=5. In general: O(n) shifts.
```

---

### 4. Array Operations and Time Complexity

| Operation              | Time    | Why                                          |
|------------------------|---------|----------------------------------------------|
| Access by index        | O(1)   | Direct memory address calculation             |
| Search (unsorted)      | O(n)   | Must check every element one by one           |
| Search (sorted)        | O(log n)| Binary search -- halve the search space      |
| Insert at end          | O(1)   | Just place at the next available position     |
| Insert at beginning    | O(n)   | Must shift ALL elements one position right    |
| Insert at middle       | O(n)   | Must shift elements after insert point        |
| Delete from end        | O(1)   | Just remove the last element                  |
| Delete from middle     | O(n)   | Must shift elements to fill the gap           |
| Update at index        | O(1)   | Direct access + overwrite                     |

---

### 5. Java Array Implementation

```java
// ===== DECLARATION =====

// Method 1: Declare then initialize
int[] nums = new int[5];          // creates array of 5 zeros
// nums = [0, 0, 0, 0, 0]

// Method 2: Declare with values
int[] nums2 = {10, 20, 30, 40, 50};

// Method 3: Using new keyword with values
int[] nums3 = new int[]{10, 20, 30};


// ===== ACCESSING ELEMENTS =====

int first = nums2[0];     // 10  (indices start at 0)
int last  = nums2[4];     // 50
int len   = nums2.length; // 5   (note: .length, NOT .length())


// ===== ITERATING =====

// Way 1: Classic for loop (use when you need the index)
for (int i = 0; i < nums2.length; i++) {
    System.out.println("Index " + i + " = " + nums2[i]);
}

// Way 2: Enhanced for loop (use when you only need values)
for (int num : nums2) {
    System.out.println(num);
}


// ===== COMMON OPERATIONS =====

// Sorting
int[] arr = {5, 2, 8, 1, 9};
Arrays.sort(arr);
// arr = [1, 2, 5, 8, 9]

// Copying
int[] copy = Arrays.copyOf(arr, arr.length);     // full copy
int[] partial = Arrays.copyOfRange(arr, 1, 4);   // elements at index 1,2,3

// Filling
int[] zeros = new int[10];
Arrays.fill(zeros, -1);   // all elements become -1

// Comparing
boolean same = Arrays.equals(arr, copy);  // true if same contents

// Converting to String (for printing)
System.out.println(Arrays.toString(arr)); // [1, 2, 5, 8, 9]
```

---

### 6. ArrayList (Dynamic Array)

**The Problem:**
Regular arrays have FIXED size. What if you do not know how many elements you
will need? What if you are reading user input and could get 5 or 5000 items?

**The Solution: ArrayList**
A dynamic array that GROWS automatically when it runs out of space.

**Real-Life Analogy:**
Imagine a notebook with 10 pages. When all 10 pages are full, you do not throw
it away -- you buy a BIGGER notebook (15 pages), copy everything over, and
continue writing. That is exactly what ArrayList does internally.

**How ArrayList Grows Internally:**

```
Step 1: You create an ArrayList. Internal array has capacity 10.

  Capacity = 10, Size = 0
  [_, _, _, _, _, _, _, _, _, _]

Step 2: You add elements. Size grows, capacity stays.

  add(5), add(3), add(8) ...
  Capacity = 10, Size = 3
  [5, 3, 8, _, _, _, _, _, _, _]

Step 3: You keep adding until size = 10 (capacity full).

  Capacity = 10, Size = 10
  [5, 3, 8, 1, 7, 2, 9, 4, 6, 0]

Step 4: You add one more element. ArrayList RESIZES!
  - Creates NEW internal array of size 15 (1.5x the old size)
  - Copies all 10 elements to the new array
  - Adds the new element
  - Old array is garbage collected

  Capacity = 15, Size = 11
  [5, 3, 8, 1, 7, 2, 9, 4, 6, 0, 42, _, _, _, _]
                                     ^^ new element
```

**Why is add() called "amortized O(1)"?**
- Most of the time, add() is O(1) -- just place at the next slot.
- Occasionally (when full), add() is O(n) -- must copy everything.
- But that expensive copy happens so RARELY that when you average over all
  operations, each add() costs O(1) on average. That is "amortized."

**Java ArrayList Usage:**

```java
import java.util.ArrayList;

// Create
ArrayList<Integer> list = new ArrayList<>();   // empty, capacity 10
ArrayList<String> names = new ArrayList<>(100); // empty, capacity 100

// Add elements
list.add(10);        // [10]         -- adds at end
list.add(20);        // [10, 20]
list.add(30);        // [10, 20, 30]
list.add(1, 15);     // [10, 15, 20, 30]  -- insert at index 1

// Access
int val = list.get(2);   // 20  (O(1) -- same as array)

// Update
list.set(0, 99);     // [99, 15, 20, 30]

// Remove
list.remove(1);      // [99, 20, 30]  -- removes element at index 1
// Note: remove shifts elements left, so it is O(n)

// Size
int size = list.size();  // 3

// Search
boolean has20 = list.contains(20);   // true  (O(n) linear search)
int idx = list.indexOf(20);          // 1

// Iterate
for (int num : list) {
    System.out.println(num);
}

// Sort
Collections.sort(list);  // sorts in ascending order

// Convert to array
Integer[] arr = list.toArray(new Integer[0]);
```

---

### 7. 2D Arrays (Matrix)

**What is it?**
An array of arrays. Think of it as a TABLE or GRID with rows and columns.

**Real-Life Analogy:**
A classroom seating chart. Row 2, Seat 3 = the student sitting in the 3rd
seat of the 2nd row. You need TWO numbers to identify any seat.

```
         Col 0   Col 1   Col 2   Col 3
       +-------+-------+-------+-------+
Row 0  |   1   |   2   |   3   |   4   |
       +-------+-------+-------+-------+
Row 1  |   5   |   6   |   7   |   8   |
       +-------+-------+-------+-------+
Row 2  |   9   |  10   |  11   |  12   |
       +-------+-------+-------+-------+

matrix[1][2] = 7   (row 1, column 2)
```

**Java 2D Array:**

```java
// Declaration
int[][] matrix = new int[3][4];  // 3 rows, 4 columns

// With values
int[][] grid = {
    {1, 2, 3, 4},
    {5, 6, 7, 8},
    {9, 10, 11, 12}
};

// Access
int val = grid[1][2];  // 7

// Iterate
for (int i = 0; i < grid.length; i++) {           // rows
    for (int j = 0; j < grid[0].length; j++) {    // columns
        System.out.print(grid[i][j] + " ");
    }
    System.out.println();
}

// Dimensions
int rows = grid.length;       // 3
int cols = grid[0].length;    // 4
```

---

## PART 2: STRINGS

---

### 1. What is a String in Java?

**Plain English:**
A String is a sequence of characters. Under the hood, it is stored as an array
of characters -- but with one critical property: **it is IMMUTABLE**.

**Real-Life Analogy:**
Think of a PRINTED BOOK. You can read any page (charAt), count the pages
(length), look up a chapter (indexOf), or photocopy a section (substring).
But you CANNOT erase a word and write a new one on the printed page. To
change something, you must PRINT A NEW BOOK (create a new String).

```
String name = "Sheetal";

Internally stored as a char array:
Index:   0    1    2    3    4    5    6
       +----+----+----+----+----+----+----+
       | 'S'| 'h'| 'e'| 'e'| 't'| 'a'| 'l'|
       +----+----+----+----+----+----+----+

name.charAt(0) = 'S'
name.charAt(4) = 't'
name.length()  = 7    (note: .length() with parentheses, unlike arrays!)
```

**Immutability means:**
```java
String s = "Hello";
s.toUpperCase();           // returns "HELLO" but s is STILL "Hello"
s = s.toUpperCase();       // NOW s points to the NEW string "HELLO"
                           // the old "Hello" still exists in memory (until GC)
```

Every "modification" creates a new String object. The original is never changed.

---

### 2. String Pool (INTERVIEW FAVORITE)

**What is the String Pool?**
A special area inside the Java Heap memory where Java stores string LITERALS.
When you write `"hello"` in code, Java first checks: "Does `hello` already
exist in my pool?" If yes, it reuses that same object. If no, it creates one.

**Why?**
Memory optimization. If 100 variables all say "India", Java stores the word
"India" only ONCE in the pool, and all 100 variables point to it.

```
                HEAP MEMORY
  +------------------------------------------+
  |                                          |
  |   +------------------+                   |
  |   |   STRING POOL    |                   |
  |   |                  |                   |
  |   |   "hello" <------+--- String a       |
  |   |      ^           |                   |
  |   |      +---------- +--- String b       |
  |   |                  |                   |
  |   |   "world" <------+--- String c       |
  |   |                  |                   |
  |   +------------------+                   |
  |                                          |
  |   String d = new String("hello")         |
  |   +-------+                              |
  |   |"hello"| <--- d (separate object      |
  |   +-------+      outside the pool!)      |
  |                                          |
  +------------------------------------------+
```

**Why == vs .equals() matters -- THIS IS ASKED IN EVERY INTERVIEW:**

```java
// == compares MEMORY ADDRESS (are they the EXACT SAME object in memory?)
// .equals() compares CONTENT (do they contain the same characters?)

String a = "hello";
String b = "hello";
String c = new String("hello");
String d = new String("hello");

// Using ==
a == b       // true  -- both point to SAME object in string pool
a == c       // false -- a is in pool, c is a NEW object on heap
c == d       // false -- two DIFFERENT objects on heap

// Using .equals()
a.equals(b)  // true  -- same content "hello"
a.equals(c)  // true  -- same content "hello"
c.equals(d)  // true  -- same content "hello"
```

**RULE: ALWAYS use .equals() to compare String content. NEVER use == for Strings.**

---

### 3. StringBuilder vs String

**The Problem: String Concatenation in Loops**

```java
// BAD -- creates n new String objects! O(n^2) time
String result = "";
for (int i = 0; i < 10000; i++) {
    result = result + i;  // each + creates a brand new String
}
```

Why is this O(n^2)? Each concatenation:
1. Creates a new String object
2. Copies all existing characters + the new character
3. Copy lengths: 1, 2, 3, 4, ..., n = n*(n+1)/2 = O(n^2)

```
Iteration 1: "" + "0"       -> copy 1 char   -> "0"
Iteration 2: "0" + "1"      -> copy 2 chars  -> "01"
Iteration 3: "01" + "2"     -> copy 3 chars  -> "012"
Iteration 4: "012" + "3"    -> copy 4 chars  -> "0123"
...
Total copies = 1 + 2 + 3 + ... + n = O(n^2)  SLOW!
```

**The Solution: StringBuilder**

```java
// GOOD -- modifies in place. O(n) time
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 10000; i++) {
    sb.append(i);  // adds to the SAME internal buffer, no new objects
}
String result = sb.toString();
```

**When to use which:**
| Scenario                      | Use         | Why                         |
|-------------------------------|-------------|-----------------------------|
| Simple concatenation (a + b)  | String      | Compiler optimizes it       |
| Concatenation inside a loop   | StringBuilder| Avoids O(n^2) object creation|
| Thread-safe concatenation     | StringBuffer | Synchronized, slower        |

---

### 4. Important String Methods for DSA

```java
String s = "Hello World";

// Basic access
s.charAt(0)           // 'H'     -- character at index
s.length()            // 11      -- total characters (note: parentheses!)

// Searching
s.indexOf('o')        // 4       -- first occurrence of 'o'
s.indexOf("World")    // 6       -- first occurrence of substring
s.lastIndexOf('o')    // 7       -- last occurrence

// Extracting
s.substring(6)        // "World"      -- from index 6 to end
s.substring(0, 5)     // "Hello"      -- from index 0 to 4 (5 is exclusive!)

// Comparison
s.equals("Hello World")       // true
s.equalsIgnoreCase("hello world")  // true
s.compareTo("Apple")          // positive (H comes after A)

// Transformation (returns NEW string, original unchanged)
s.toLowerCase()       // "hello world"
s.toUpperCase()       // "HELLO WORLD"
s.trim()              // removes leading/trailing spaces
s.replace('l', 'r')   // "Herro Worrd"

// Conversion
s.toCharArray()       // char[] {'H','e','l','l','o',' ','W','o','r','l','d'}
String.valueOf(123)   // "123"  -- int to String
Integer.parseInt("42")// 42     -- String to int

// Splitting
"a,b,c".split(",")   // String[] {"a", "b", "c"}

// Checking
s.startsWith("Hell")  // true
s.endsWith("ld")      // true
s.contains("World")   // true
s.isEmpty()           // false
```

---

### 5. Common String Patterns in DSA

**Pattern 1: Character Frequency using int[26]**

When dealing with lowercase English letters only, use an array of size 26
instead of a HashMap. It is faster and simpler.

```java
// Count frequency of each character
String s = "abracadabra";
int[] freq = new int[26];  // index 0='a', 1='b', ..., 25='z'

for (char c : s.toCharArray()) {
    freq[c - 'a']++;
    // 'a' - 'a' = 0, 'b' - 'a' = 1, etc.
}
// freq[0]=5 (a appears 5 times), freq[1]=2 (b appears 2 times), etc.
```

**Pattern 2: Two Pointers for Palindrome Check**

```java
// A palindrome reads the same forward and backward: "madam", "racecar"
public boolean isPalindrome(String s) {
    int left = 0;
    int right = s.length() - 1;

    while (left < right) {
        if (s.charAt(left) != s.charAt(right)) {
            return false;  // mismatch found, not a palindrome
        }
        left++;   // move inward from left
        right--;  // move inward from right
    }
    return true;  // all characters matched
}
```

```
Example: "racecar"
 left=0, right=6: 'r' == 'r' -> match, move inward
 left=1, right=5: 'a' == 'a' -> match, move inward
 left=2, right=4: 'c' == 'c' -> match, move inward
 left=3, right=3: left >= right -> STOP -> palindrome!
```

---

## PART 3: KEY PATTERNS FOR WEEK 1

---

### Pattern 1: Kadane's Algorithm (Maximum Subarray Sum)

**Problem:** Given an integer array, find the contiguous subarray with the
largest sum and return that sum.

**The Idea:**
Walk through the array maintaining two variables:
- `currentMax` -- the best sum ENDING at the current position
- `globalMax` -- the best sum seen ANYWHERE so far

At each position, you make a choice:
- Either EXTEND the previous subarray by adding current element
- Or START FRESH from the current element (if previous sum was negative)

If `currentMax` becomes negative, it means the previous subarray is HURTING
us. Drop it. Start fresh.

**Step-by-Step Walkthrough:**

```
Array: [-2, 1, -3, 4, -1, 2, 1, -5, 4]

Index 0: num = -2
  currentMax = max(-2, 0 + (-2)) = -2
  globalMax  = max(-inf, -2) = -2

Index 1: num = 1
  currentMax = max(1, -2 + 1) = max(1, -1) = 1    <- START FRESH!
  globalMax  = max(-2, 1) = 1

Index 2: num = -3
  currentMax = max(-3, 1 + (-3)) = max(-3, -2) = -2
  globalMax  = max(1, -2) = 1

Index 3: num = 4
  currentMax = max(4, -2 + 4) = max(4, 2) = 4     <- START FRESH!
  globalMax  = max(1, 4) = 4

Index 4: num = -1
  currentMax = max(-1, 4 + (-1)) = max(-1, 3) = 3  <- EXTEND
  globalMax  = max(4, 3) = 4

Index 5: num = 2
  currentMax = max(2, 3 + 2) = max(2, 5) = 5       <- EXTEND
  globalMax  = max(4, 5) = 5

Index 6: num = 1
  currentMax = max(1, 5 + 1) = max(1, 6) = 6       <- EXTEND
  globalMax  = max(5, 6) = 6                         ** NEW BEST!

Index 7: num = -5
  currentMax = max(-5, 6 + (-5)) = max(-5, 1) = 1   <- EXTEND
  globalMax  = max(6, 1) = 6

Index 8: num = 4
  currentMax = max(4, 1 + 4) = max(4, 5) = 5        <- EXTEND
  globalMax  = max(6, 5) = 6

ANSWER: 6 (subarray [4, -1, 2, 1])
```

**Java Code:**

```java
public int maxSubArray(int[] nums) {
    int currentMax = nums[0];  // best sum ending at current position
    int globalMax = nums[0];   // best sum seen anywhere

    for (int i = 1; i < nums.length; i++) {
        // Choice: extend previous subarray or start fresh
        currentMax = Math.max(nums[i], currentMax + nums[i]);

        // Update global best if current is better
        globalMax = Math.max(globalMax, currentMax);
    }

    return globalMax;
}
```

**Time: O(n)** -- single pass through the array.
**Space: O(1)** -- only two variables.

---

### Pattern 2: Prefix Sum (Range Sum Queries in O(1))

**Problem:** Given an array, answer multiple queries: "What is the sum of
elements from index L to index R?"

**Brute force:** For each query, loop from L to R and add up. That is O(n)
per query. If you have Q queries, total = O(n x Q).

**Prefix Sum approach:** Build a prefix array ONCE in O(n), then answer
every query in O(1).

**How it works:**

```
Original array: [2, 4, 5, 1, 6, 3]
Index:           0  1  2  3  4  5

Prefix array: prefix[i] = sum of elements from index 0 to i

prefix[0] = 2
prefix[1] = 2 + 4 = 6
prefix[2] = 2 + 4 + 5 = 11
prefix[3] = 2 + 4 + 5 + 1 = 12
prefix[4] = 2 + 4 + 5 + 1 + 6 = 18
prefix[5] = 2 + 4 + 5 + 1 + 6 + 3 = 21

prefix = [2, 6, 11, 12, 18, 21]


Query: Sum from index 2 to index 4?

  Sum(2, 4) = prefix[4] - prefix[1]
            = 18 - 6
            = 12

  Check: arr[2] + arr[3] + arr[4] = 5 + 1 + 6 = 12  Correct!

Formula:
  Sum(L, R) = prefix[R] - prefix[L-1]     (when L > 0)
  Sum(0, R) = prefix[R]                    (when L = 0)
```

**Visual:**

```
Original:  [2,   4,   5,   1,   6,   3]
Prefix:    [2,   6,  11,  12,  18,  21]
                  |              |
            prefix[1]=6    prefix[4]=18

Sum(2..4) = prefix[4] - prefix[1] = 18 - 6 = 12
            ^^^^^^^^^   ^^^^^^^^^^
            sum(0..4)   sum(0..1)   -> difference = sum(2..4)
```

**Java Code:**

```java
// Build prefix sum array
int[] prefix = new int[nums.length];
prefix[0] = nums[0];
for (int i = 1; i < nums.length; i++) {
    prefix[i] = prefix[i - 1] + nums[i];
}

// Answer a query: sum from L to R
public int rangeSum(int[] prefix, int L, int R) {
    if (L == 0) return prefix[R];
    return prefix[R] - prefix[L - 1];
}
```

**Time:** O(n) to build + O(1) per query.
**Space:** O(n) for the prefix array.

---

### Pattern 3: Two-Pointer Technique (Preview)

We will cover this in detail in Week 2, but here is a preview since some
Week 1 problems use it.

**Core Idea:**
Instead of two nested loops (O(n^2)), use two pointers that move through the
array intelligently, reducing time to O(n).

**Example: Two Sum in Sorted Array**

```
Find two numbers in sorted array that add up to target = 9.
Array: [1, 2, 4, 6, 8, 10]

      left              right
       |                  |
       v                  v
      [1, 2, 4, 6, 8, 10]

Step 1: sum = 1 + 10 = 11 > 9  -> too big, move right LEFT
Step 2: sum = 1 + 8  = 9  = 9  -> FOUND IT! Return [0, 4]
```

```java
public int[] twoSum(int[] nums, int target) {
    int left = 0;
    int right = nums.length - 1;

    while (left < right) {
        int sum = nums[left] + nums[right];
        if (sum == target) {
            return new int[]{left, right};
        } else if (sum < target) {
            left++;    // need a bigger sum, move left pointer right
        } else {
            right--;   // need a smaller sum, move right pointer left
        }
    }
    return new int[]{};  // no solution found
}
```

---

### 6. When to Use Arrays?

**Use arrays when:**
- You need O(1) random access by index
- You know the size in advance (or can estimate it)
- You need to store a collection of same-type elements
- Memory efficiency matters (arrays have less overhead than linked lists)

**Use ArrayList when:**
- You do not know the size in advance
- You need dynamic resizing
- You need built-in methods (add, remove, contains, etc.)

**Pattern recognition triggers in LeetCode:**
- "Given an array of integers..." -- obvious
- "Contiguous subarray" -- think prefix sum or sliding window
- "Maximum/minimum subarray" -- think Kadane's
- "Sorted array + find pair" -- think two pointers
- "In-place modification" -- think two pointers (same direction)

---

### 7. Common Mistakes

1. **ArrayIndexOutOfBoundsException**
   - Accessing index -1 or index >= length
   - Fix: always check bounds before accessing

2. **Confusing .length (arrays) with .length() (strings) and .size() (ArrayList)**
   ```java
   int[] arr = {1, 2, 3};
   String s = "hello";
   ArrayList<Integer> list = new ArrayList<>();

   arr.length    // no parentheses for arrays
   s.length()    // parentheses for strings
   list.size()   // .size() for ArrayList
   ```

3. **Modifying array while iterating**
   - Can cause skipped elements or errors
   - Use a separate array or iterate backwards

4. **Using == instead of .equals() for Strings**
   - == compares memory address, .equals() compares content
   - ALWAYS use .equals() for string comparison

5. **String concatenation in loops**
   - Use StringBuilder, not + operator in loops
   - + in loops creates O(n) new objects = O(n^2) total

6. **Forgetting that substring end index is EXCLUSIVE**
   ```java
   "Hello".substring(1, 3)  // "el", NOT "ell"
   // includes index 1 and 2, but NOT 3
   ```

7. **Off-by-one errors in 2D arrays**
   ```java
   grid.length       // number of ROWS
   grid[0].length    // number of COLUMNS
   // do not mix these up!
   ```

---

### 8. Quick Summary

```
+------------------+------------------------------------------+
| Concept          | Key Takeaway                             |
+------------------+------------------------------------------+
| Array            | Fixed size, O(1) access, contiguous      |
| ArrayList        | Dynamic size, amortized O(1) add         |
| String           | Immutable char array, use .equals()      |
| String Pool      | Reuses literals, why == can be tricky    |
| StringBuilder    | Mutable, use for loops, O(n) vs O(n^2)  |
| Kadane's         | Max subarray: extend or start fresh      |
| Prefix Sum       | Build once O(n), query O(1)              |
| Two Pointers     | Sorted array pair finding, O(n)          |
| char freq[26]    | Fast character counting for a-z          |
+------------------+------------------------------------------+

Time Complexities to MEMORIZE:
  Array access:        O(1)
  Array search:        O(n) unsorted, O(log n) sorted
  Array insert/delete: O(n) middle, O(1) end
  ArrayList add:       O(1) amortized
  String concat loop:  O(n^2) -- use StringBuilder!
  StringBuilder:       O(n)
```

---

**You are now ready to tackle Week 1 LeetCode problems!**
Go to the Problems.md file and start solving. Refer back to this
Concept.md whenever you get stuck on the underlying theory.
