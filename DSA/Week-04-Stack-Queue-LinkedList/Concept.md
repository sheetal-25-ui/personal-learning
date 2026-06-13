# Week 4: Stack, Queue & Linked List - Deep Concept Guide

---

## PART 1: STACK

---

### 1. What is a Stack?

**Plain English:**
A Stack is a data structure where you can only add and remove elements from
the TOP. The last element you put in is the first one you take out. This is
called **LIFO -- Last In, First Out**.

**Real-Life Analogy #1: Stack of Plates**
In a restaurant kitchen, plates are stacked one on top of another.
- You always place a new plate ON TOP (push)
- You always take the plate FROM THE TOP (pop)
- You cannot pull a plate from the middle without toppling the stack

```
        +-------+
        |  Plate 5  | <-- TOP (most recent, first to be removed)
        +-------+
        |  Plate 4  |
        +-------+
        |  Plate 3  |
        +-------+
        |  Plate 2  |
        +-------+
        |  Plate 1  | <-- BOTTOM (oldest, last to be removed)
        +-------+
```

**Real-Life Analogy #2: Browser Back Button**
Every page you visit is "pushed" onto a history stack.
When you press BACK, the most recent page is "popped" off.

```
Visit Google     -> Stack: [Google]
Visit YouTube    -> Stack: [Google, YouTube]
Visit Wikipedia  -> Stack: [Google, YouTube, Wikipedia]
Press BACK       -> Pop Wikipedia. Stack: [Google, YouTube]
Press BACK       -> Pop YouTube.  Stack: [Google]
```

**Real-Life Analogy #3: Undo in a Text Editor**
Every action you do (type, delete, format) is pushed onto a stack.
When you press Ctrl+Z (undo), the most recent action is popped and reversed.

---

### 2. Why Does Stack Exist?

Stacks solve problems where you need to process things in REVERSE ORDER
or track "most recent" state.

Common use cases:
- **Undo/Redo** -- reverse the last action
- **Browser history** -- go back to the previous page
- **Function call stack** -- the most recently called function returns first
- **Expression evaluation** -- process operators in correct order
- **Matching brackets** -- most recent opening bracket must match first
- **DFS traversal** -- explore the deepest path first, backtrack

---

### 3. How Stack Works Internally

A stack is typically implemented using an ARRAY with a pointer (top) tracking
the topmost element.

```
Operation: push(10), push(20), push(30), pop(), push(40)

Step 1: push(10)        Step 2: push(20)        Step 3: push(30)
top=0                   top=1                   top=2
+----+----+----+----+   +----+----+----+----+   +----+----+----+----+
| 10 |    |    |    |   | 10 | 20 |    |    |   | 10 | 20 | 30 |    |
+----+----+----+----+   +----+----+----+----+   +----+----+----+----+
  ^                        ^    ^                  ^         ^
  |                        |    |                  |         |
bottom                  bottom  top             bottom      top


Step 4: pop() -> returns 30   Step 5: push(40)
top=1                          top=2
+----+----+----+----+          +----+----+----+----+
| 10 | 20 |    |    |          | 10 | 20 | 40 |    |
+----+----+----+----+          +----+----+----+----+
  ^    ^                         ^         ^
  |    |                         |         |
bottom top                    bottom      top
```

---

### 4. Stack Operations and Time Complexity

| Operation     | Time | What it does                                  |
|---------------|------|-----------------------------------------------|
| push(x)       | O(1)| Add element x on top of the stack              |
| pop()         | O(1)| Remove and return the top element               |
| peek() / top()| O(1)| Look at the top element WITHOUT removing it     |
| isEmpty()     | O(1)| Check if the stack has no elements               |
| size()        | O(1)| Return number of elements in the stack           |

ALL operations are O(1). This is what makes stacks so efficient.

---

### 5. Java Stack Implementation

**IMPORTANT: Do NOT use the legacy `Stack` class. Use `ArrayDeque` instead.**

```java
import java.util.ArrayDeque;
import java.util.Deque;

// ===== CREATING A STACK =====
Deque<Integer> stack = new ArrayDeque<>();

// ===== PUSH: Add element on top =====
stack.push(10);   // Stack: [10]
stack.push(20);   // Stack: [20, 10]    (20 is now on top)
stack.push(30);   // Stack: [30, 20, 10]

// ===== PEEK: Look at top without removing =====
int top = stack.peek();   // 30 (top element, stack unchanged)

// ===== POP: Remove and return top element =====
int removed = stack.pop();  // 30 (removed from top)
// Stack is now: [20, 10]

// ===== CHECK EMPTY =====
boolean empty = stack.isEmpty();  // false

// ===== SIZE =====
int size = stack.size();  // 2
```

**Why ArrayDeque over Stack class?**
- `Stack` class is from Java 1.0, is synchronized (thread-safe but SLOW),
  and extends `Vector` (bad design).
- `ArrayDeque` is faster, not synchronized, and is the recommended approach
  as per Java documentation.

**Using Stack class (legacy, avoid in new code):**
```java
import java.util.Stack;

Stack<Integer> stack = new Stack<>();
stack.push(10);
stack.peek();
stack.pop();
stack.isEmpty();
// Same methods, but slower due to synchronization overhead
```

---

### 6. Common Stack Patterns

---

#### Pattern 1: Matching Brackets (CLASSIC INTERVIEW QUESTION)

**Problem:** Given a string containing `(`, `)`, `{`, `}`, `[`, `]`,
determine if the brackets are valid (every opening bracket has a matching
closing bracket in the correct order).

**Approach:**
- When you see an OPENING bracket, PUSH it onto the stack.
- When you see a CLOSING bracket, POP and check if it matches.
- If the stack is empty at the end, all brackets matched.

```java
public boolean isValid(String s) {
    Deque<Character> stack = new ArrayDeque<>();

    for (char c : s.toCharArray()) {
        // Opening bracket: push onto stack
        if (c == '(' || c == '{' || c == '[') {
            stack.push(c);
        }
        // Closing bracket: check match
        else {
            if (stack.isEmpty()) return false;  // nothing to match with

            char top = stack.pop();
            if (c == ')' && top != '(') return false;
            if (c == '}' && top != '{') return false;
            if (c == ']' && top != '[') return false;
        }
    }

    return stack.isEmpty();  // all brackets matched?
}
```

**Trace with "{[()]}":**

```
char '{': opening -> push. Stack: [{]
char '[': opening -> push. Stack: [{, []
char '(': opening -> push. Stack: [{, [, (]
char ')': closing -> pop '(' -> matches ')'. Stack: [{, []
char ']': closing -> pop '[' -> matches ']'. Stack: [{]
char '}': closing -> pop '{' -> matches '}'. Stack: []

Stack is empty -> VALID!
```

**Trace with "([)]":**

```
char '(': push. Stack: [(]
char '[': push. Stack: [(, []
char ')': pop '[' -> does NOT match ')'. INVALID!
```

---

#### Pattern 2: Monotonic Stack (POWERFUL TECHNIQUE!)

**What is a Monotonic Stack?**
A stack that maintains elements in either INCREASING or DECREASING order.
When a new element arrives that violates the order, you POP elements until
the order is restored.

**Why is it useful?**
It efficiently solves "Next Greater Element" and "Next Smaller Element" problems
in O(n) instead of O(n^2).

---

**Next Greater Element:**

**Problem:** For each element in the array, find the NEXT element that is
GREATER than it.

```
Array:  [4, 5, 2, 25]
Output: [5, 25, 25, -1]

For 4:  next greater = 5
For 5:  next greater = 25
For 2:  next greater = 25
For 25: no next greater = -1
```

**Approach with Monotonic Stack:**
Iterate from RIGHT to LEFT. Maintain a stack of "candidates" for next greater.

```java
public int[] nextGreaterElement(int[] nums) {
    int n = nums.length;
    int[] result = new int[n];
    Deque<Integer> stack = new ArrayDeque<>();  // stores VALUES

    // Process from right to left
    for (int i = n - 1; i >= 0; i--) {
        // Pop elements that are SMALLER OR EQUAL to current
        // (they cannot be the "next greater" for anyone to the left)
        while (!stack.isEmpty() && stack.peek() <= nums[i]) {
            stack.pop();
        }

        // Top of stack is the next greater element (or -1 if empty)
        result[i] = stack.isEmpty() ? -1 : stack.peek();

        // Push current element as a candidate for future elements
        stack.push(nums[i]);
    }

    return result;
}
```

**Step-by-step trace with [4, 5, 2, 25]:**

```
Processing RIGHT to LEFT:

i=3, nums[3]=25:
  Stack: []
  No elements to pop.
  result[3] = -1 (stack empty, no next greater)
  Push 25. Stack: [25]

i=2, nums[2]=2:
  Stack: [25]
  25 > 2, so do NOT pop.
  result[2] = 25 (top of stack)
  Push 2. Stack: [2, 25]

i=1, nums[1]=5:
  Stack: [2, 25]
  2 <= 5, pop 2. Stack: [25]
  25 > 5, stop popping.
  result[1] = 25 (top of stack)
  Push 5. Stack: [5, 25]

i=0, nums[0]=4:
  Stack: [5, 25]
  5 > 4, stop.
  result[0] = 5 (top of stack)
  Push 4. Stack: [4, 5, 25]

Result: [5, 25, 25, -1]
```

---

**Daily Temperatures (Application of Monotonic Stack):**

**Problem:** Given daily temperatures, for each day, find how many days you
must wait until a warmer temperature. If no future day is warmer, output 0.

```
Input:  [73, 74, 75, 71, 69, 72, 76, 73]
Output: [1,  1,  4,  2,  1,  1,  0,  0]

Day 0 (73): next warmer = Day 1 (74), wait = 1 day
Day 1 (74): next warmer = Day 2 (75), wait = 1 day
Day 2 (75): next warmer = Day 6 (76), wait = 4 days
Day 3 (71): next warmer = Day 5 (72), wait = 2 days
...
```

```java
public int[] dailyTemperatures(int[] temperatures) {
    int n = temperatures.length;
    int[] result = new int[n];
    Deque<Integer> stack = new ArrayDeque<>();  // stores INDICES (not values)

    for (int i = 0; i < n; i++) {
        // Pop all days that have found a warmer day (today!)
        while (!stack.isEmpty() && temperatures[i] > temperatures[stack.peek()]) {
            int prevDay = stack.pop();
            result[prevDay] = i - prevDay;  // days waited
        }

        stack.push(i);  // push current day's INDEX
    }

    // Remaining days in stack have no warmer day -> result stays 0

    return result;
}
```

**Time: O(n)** -- each element is pushed and popped at most once.
**Space: O(n)** -- stack can hold at most n elements.

---

#### Pattern 3: Evaluate Reverse Polish Notation

```
Input:  ["2", "1", "+", "3", "*"]
Output: 9
Explanation: ((2 + 1) * 3) = 9
```

```java
public int evalRPN(String[] tokens) {
    Deque<Integer> stack = new ArrayDeque<>();

    for (String token : tokens) {
        if (token.equals("+") || token.equals("-") ||
            token.equals("*") || token.equals("/")) {
            int b = stack.pop();  // second operand (popped first!)
            int a = stack.pop();  // first operand
            switch (token) {
                case "+": stack.push(a + b); break;
                case "-": stack.push(a - b); break;
                case "*": stack.push(a * b); break;
                case "/": stack.push(a / b); break;
            }
        } else {
            stack.push(Integer.parseInt(token));  // operand, push to stack
        }
    }

    return stack.pop();
}
```

---

## PART 2: QUEUE

---

### 1. What is a Queue?

**Plain English:**
A Queue is a data structure where you add elements at the BACK and remove
elements from the FRONT. The first element in is the first one out. This is
called **FIFO -- First In, First Out**.

**Real-Life Analogy: Movie Ticket Line**
People stand in a line (queue) at the ticket counter.
- New people join at the BACK of the line (enqueue)
- The person at the FRONT gets served first (dequeue)
- No cutting in line!

```
    FRONT                                     BACK
    (exit)                                   (entry)
      |                                        |
      v                                        v
   +------+  +------+  +------+  +------+  +------+
   | Raj  |  | Priya|  | Amit |  | Neha |  | Ravi |
   +------+  +------+  +------+  +------+  +------+
     ^                                         ^
     |                                         |
   First to                               Last to
   be served                               be served
```

**Real-Life Analogy: Printer Queue**
When multiple people send documents to a printer, they are processed in order.
First document sent = first document printed.

---

### 2. Why Does Queue Exist?

Queues solve problems where you need to process things in the ORDER THEY
ARRIVED. This is fundamental to:
- **BFS (Breadth-First Search)** -- explore level by level
- **Task scheduling** -- process tasks in order received
- **Message passing** -- handle messages in arrival order
- **Buffering** -- manage data flow between producer and consumer

---

### 3. Queue Operations and Time Complexity

| Operation            | Time | What it does                          |
|----------------------|------|---------------------------------------|
| enqueue / offer(x)   | O(1)| Add element at the back               |
| dequeue / poll()     | O(1)| Remove and return the front element   |
| peek()               | O(1)| Look at front element without removing|
| isEmpty()            | O(1)| Check if queue has no elements        |
| size()               | O(1)| Return number of elements             |

---

### 4. Java Queue Implementation

```java
import java.util.LinkedList;
import java.util.Queue;
import java.util.ArrayDeque;

// ===== CREATING A QUEUE =====
Queue<Integer> queue = new LinkedList<>();
// OR (faster):
Queue<Integer> queue2 = new ArrayDeque<>();

// ===== ENQUEUE: Add to the back =====
queue.offer(10);   // Queue: [10]
queue.offer(20);   // Queue: [10, 20]
queue.offer(30);   // Queue: [10, 20, 30]
//                    front ^           ^ back

// ===== PEEK: Look at front =====
int front = queue.peek();   // 10 (front element, queue unchanged)

// ===== DEQUEUE: Remove from front =====
int removed = queue.poll();  // 10 (removed from front)
// Queue is now: [20, 30]

// ===== CHECK EMPTY =====
boolean empty = queue.isEmpty();  // false

// ===== SIZE =====
int size = queue.size();  // 2
```

**offer() vs add(), poll() vs remove(), peek() vs element():**

| Operation | Returns null if empty | Throws exception if empty |
|-----------|-----------------------|---------------------------|
| Add       | offer(x)              | add(x)                    |
| Remove    | poll()                | remove()                  |
| Examine   | peek()                | element()                 |

**Prefer offer/poll/peek** -- they return null gracefully instead of crashing.

---

### 5. Deque (Double-Ended Queue)

A Deque allows adding and removing from BOTH ends.

```
           +------+------+------+------+------+
  addFirst | Ravi | Raj  | Priya| Amit | Neha | addLast
 <------   +------+------+------+------+------+   ------>
removeFirst                                      removeLast
```

```java
Deque<Integer> deque = new ArrayDeque<>();

// Add to front
deque.offerFirst(10);   // [10]
deque.offerFirst(20);   // [20, 10]

// Add to back
deque.offerLast(30);    // [20, 10, 30]

// Remove from front
int f = deque.pollFirst();  // 20, deque = [10, 30]

// Remove from back
int b = deque.pollLast();   // 30, deque = [10]

// Peek at both ends
deque.peekFirst();  // front element
deque.peekLast();   // back element
```

**ArrayDeque can be used as BOTH a Stack and a Queue:**
- As Stack: push/pop/peek (operate on the front)
- As Queue: offer/poll/peek (add at back, remove from front)

---

### 6. PriorityQueue (Min-Heap / Max-Heap)

**What is it?**
A special queue where elements come out in PRIORITY ORDER, not in the order
they were added.

By default, Java PriorityQueue is a MIN-HEAP: the SMALLEST element comes out first.

```java
// ===== MIN-HEAP (default) =====
PriorityQueue<Integer> minHeap = new PriorityQueue<>();
minHeap.offer(30);
minHeap.offer(10);
minHeap.offer(20);

minHeap.poll();  // 10 (smallest first!)
minHeap.poll();  // 20
minHeap.poll();  // 30

// ===== MAX-HEAP (use reverseOrder) =====
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
maxHeap.offer(30);
maxHeap.offer(10);
maxHeap.offer(20);

maxHeap.poll();  // 30 (largest first!)
maxHeap.poll();  // 20
maxHeap.poll();  // 10

// ===== CUSTOM COMPARATOR =====
// Sort by string length
PriorityQueue<String> pq = new PriorityQueue<>((a, b) -> a.length() - b.length());
pq.offer("banana");     // length 6
pq.offer("apple");      // length 5
pq.offer("fig");         // length 3

pq.poll();  // "fig"    (shortest first)
pq.poll();  // "apple"
pq.poll();  // "banana"
```

**PriorityQueue operations:**

| Operation | Time     | What it does                       |
|-----------|----------|------------------------------------|
| offer(x)  | O(log n)| Add element (maintains heap order)  |
| poll()    | O(log n)| Remove and return min/max element   |
| peek()    | O(1)    | Look at min/max without removing    |
| size()    | O(1)    | Number of elements                  |

**Common DSA uses of PriorityQueue:**
- Top K elements (use a min-heap of size K)
- Merge K sorted lists
- Dijkstra's shortest path algorithm
- Task scheduling by priority

---

## PART 3: LINKED LIST

---

### 1. What is a Linked List?

**Plain English:**
A Linked List is a data structure where each element (called a NODE) contains
two things: the DATA and a POINTER to the NEXT node. Nodes are scattered
throughout memory -- NOT contiguous like arrays.

**Real-Life Analogy: A Treasure Hunt**
Imagine a treasure hunt game. Each clue (node) has two things:
1. A treasure piece (the data)
2. Directions to the NEXT clue (the pointer)

You start at Clue #1, which tells you where Clue #2 is. Clue #2 tells you
where Clue #3 is. And so on. You MUST follow the chain -- you cannot jump
directly to Clue #5 without visiting 1, 2, 3, 4 first.

```
 Clue 1          Clue 2          Clue 3          Clue 4
+--------+      +--------+      +--------+      +--------+
| Data: A |      | Data: B |      | Data: C |      | Data: D |
| Next: --+----->| Next: --+----->| Next: --+----->| Next: null|
+--------+      +--------+      +--------+      +--------+
  (head)                                           (tail)
```

**Key difference from Arrays:**

```
ARRAY (contiguous memory):
+---+---+---+---+---+
| A | B | C | D | E |   All elements side by side
+---+---+---+---+---+
1000 1004 1008 1012 1016

LINKED LIST (scattered memory):
+---+---+    +---+---+    +---+---+    +---+---+
| A | --+--->| B | --+--->| C | --+--->| D |null|
+---+---+    +---+---+    +---+---+    +---+---+
 addr 1000    addr 5048    addr 2096    addr 7832
              (anywhere!)  (anywhere!)  (anywhere!)
```

---

### 2. Why Does Linked List Exist?

**The Problem with Arrays:**
- FIXED SIZE: you must know the size upfront
- INSERT/DELETE at the beginning is O(n) -- must shift all elements

**Linked List solves these:**
- DYNAMIC SIZE: just create a new node and link it
- INSERT/DELETE at the beginning is O(1) -- just change one pointer

---

### 3. Array vs Linked List (Detailed Comparison)

| Operation                | Array          | Linked List      |
|--------------------------|----------------|------------------|
| Access by index          | O(1) - direct  | O(n) - must traverse |
| Insert at beginning      | O(n) - shift all | O(1) - change head pointer |
| Insert at end            | O(1) amortized | O(1) if tail pointer |
| Insert in middle         | O(n) - shift   | O(1) if position known* |
| Delete from beginning    | O(n) - shift all | O(1) - change head pointer |
| Delete from end          | O(1)           | O(n) for singly linked** |
| Delete in middle         | O(n) - shift   | O(1) if position known* |
| Search                   | O(n) or O(log n)| O(n)            |
| Memory                   | Contiguous, compact | Scattered, extra pointer overhead |
| Cache performance        | Excellent      | Poor (elements scattered) |

*O(1) if you already have the pointer to the previous node. Finding the
position is still O(n).

**O(n) for singly linked because you must find the second-to-last node.
O(1) for doubly linked.

---

### 4. Types of Linked Lists

#### Type 1: Singly Linked List

Each node points to the NEXT node only. Can only traverse forward.

```
head -> [10 | -]--> [20 | -]--> [30 | -]--> [40 | null]
```

#### Type 2: Doubly Linked List

Each node has pointers to BOTH next AND previous. Can traverse both directions.

```
null <--[- | 10 | -]<-->[- | 20 | -]<-->[- | 30 | -]--> null
          head                                 tail
```

#### Type 3: Circular Linked List

The last node points back to the first node, forming a loop.

```
head -> [10 | -]--> [20 | -]--> [30 | -]--> [40 | -]--+
  ^                                                     |
  +-----------------------------------------------------+
```

---

### 5. Java Implementation from Scratch

**Step 1: Define the Node class**

```java
class ListNode {
    int val;        // the data stored in this node
    ListNode next;  // pointer to the next node

    // Constructor
    ListNode(int val) {
        this.val = val;
        this.next = null;  // by default, points to nothing
    }
}
```

**Step 2: Build a Linked List manually**

```java
// Create nodes
ListNode node1 = new ListNode(10);
ListNode node2 = new ListNode(20);
ListNode node3 = new ListNode(30);

// Link them together
node1.next = node2;   // 10 -> 20
node2.next = node3;   // 20 -> 30

// head points to the first node
ListNode head = node1;
// Result: head -> [10] -> [20] -> [30] -> null
```

**Step 3: Common Operations**

```java
// ===== TRAVERSE (Print all elements) =====
public void printList(ListNode head) {
    ListNode current = head;
    while (current != null) {
        System.out.print(current.val + " -> ");
        current = current.next;
    }
    System.out.println("null");
}
// Output: 10 -> 20 -> 30 -> null


// ===== ADD AT BEGINNING (O(1)) =====
public ListNode addFirst(ListNode head, int val) {
    ListNode newNode = new ListNode(val);
    newNode.next = head;   // new node points to current head
    return newNode;        // new node IS the new head
}

// Before: head -> [10] -> [20] -> [30] -> null
// addFirst(head, 5)
// After:  head -> [5] -> [10] -> [20] -> [30] -> null

// Visual:
//   newNode
//   +---+
//   | 5 |--+
//   +---+  |
//           v
//   [10] -> [20] -> [30] -> null
//    ^
//    old head


// ===== ADD AT END (O(n) without tail pointer) =====
public void addLast(ListNode head, int val) {
    ListNode newNode = new ListNode(val);

    if (head == null) {
        head = newNode;
        return;
    }

    // Traverse to the last node
    ListNode current = head;
    while (current.next != null) {
        current = current.next;
    }

    current.next = newNode;  // last node now points to new node
}


// ===== DELETE A NODE WITH GIVEN VALUE =====
public ListNode delete(ListNode head, int val) {
    // Special case: delete the head
    if (head != null && head.val == val) {
        return head.next;
    }

    ListNode current = head;
    while (current.next != null) {
        if (current.next.val == val) {
            current.next = current.next.next;  // skip the node
            return head;
        }
        current = current.next;
    }

    return head;  // value not found
}

// Before: [10] -> [20] -> [30] -> null
// delete(head, 20)
// After:  [10] -> [30] -> null
//
// Visual of pointer change:
//   [10] -> [20] -> [30] -> null
//        |           ^
//        +-----------+     (10's next now points to 30, skipping 20)


// ===== GET LENGTH =====
public int length(ListNode head) {
    int count = 0;
    ListNode current = head;
    while (current != null) {
        count++;
        current = current.next;
    }
    return count;
}


// ===== SEARCH FOR A VALUE =====
public boolean contains(ListNode head, int val) {
    ListNode current = head;
    while (current != null) {
        if (current.val == val) return true;
        current = current.next;
    }
    return false;
}
```

---

### 6. Key Linked List Techniques

---

#### Technique 1: Runner (Fast-Slow Pointer)

**Find the Middle of a Linked List:**

Use two pointers: SLOW moves 1 step, FAST moves 2 steps. When fast reaches
the end, slow is at the middle.

```java
public ListNode findMiddle(ListNode head) {
    ListNode slow = head;
    ListNode fast = head;

    while (fast != null && fast.next != null) {
        slow = slow.next;          // 1 step
        fast = fast.next.next;     // 2 steps
    }

    return slow;  // slow is now at the middle
}
```

**Visual trace with [1, 2, 3, 4, 5]:**

```
Start:
  slow -> [1] -> [2] -> [3] -> [4] -> [5] -> null
  fast -> [1]

Iteration 1:
  slow -> [2]
  fast -> [3]

  [1] -> [2] -> [3] -> [4] -> [5] -> null
          ^      ^
         slow   fast

Iteration 2:
  slow -> [3]
  fast -> [5]

  [1] -> [2] -> [3] -> [4] -> [5] -> null
                  ^                    ^
                 slow                 fast

fast.next == null -> STOP!
slow is at [3] = MIDDLE!
```

---

**Detect Cycle in Linked List (Floyd's Algorithm):**

If there is a cycle, the fast pointer will eventually "lap" the slow pointer
and they will meet. If there is no cycle, fast will reach null.

```java
public boolean hasCycle(ListNode head) {
    ListNode slow = head;
    ListNode fast = head;

    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;

        if (slow == fast) {
            return true;   // they met! There is a cycle.
        }
    }

    return false;  // fast reached end, no cycle
}
```

**Visual: List with a cycle**

```
[1] -> [2] -> [3] -> [4] -> [5]
                ^              |
                |              |
                +--------------+
                (5 points back to 3)

Iteration 1: slow=[2], fast=[3]
Iteration 2: slow=[3], fast=[5]
Iteration 3: slow=[4], fast=[4]  <- fast went 5->3->4
              slow == fast -> CYCLE DETECTED!
```

**Find where the cycle starts:**

```java
public ListNode detectCycleStart(ListNode head) {
    ListNode slow = head;
    ListNode fast = head;

    // Phase 1: Detect cycle
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
        if (slow == fast) break;
    }

    if (fast == null || fast.next == null) return null;  // no cycle

    // Phase 2: Find cycle start
    // Reset slow to head, keep fast at meeting point.
    // Move both one step at a time. They will meet at cycle start.
    slow = head;
    while (slow != fast) {
        slow = slow.next;
        fast = fast.next;
    }

    return slow;  // cycle starts here
}
```

---

#### Technique 2: Reverse a Linked List (MOST ASKED QUESTION!)

**Problem:** Reverse the direction of all pointers.

```
Before: [1] -> [2] -> [3] -> [4] -> [5] -> null
After:  null <- [1] <- [2] <- [3] <- [4] <- [5]
        (head is now [5])
```

**Three-pointer approach: prev, current, next**

```java
public ListNode reverseList(ListNode head) {
    ListNode prev = null;       // what comes BEFORE current
    ListNode current = head;    // the node we are processing
    // ListNode next is declared inside the loop

    while (current != null) {
        ListNode next = current.next;   // save the next node
        current.next = prev;            // REVERSE the pointer
        prev = current;                 // move prev forward
        current = next;                 // move current forward
    }

    return prev;  // prev is now the new head
}
```

**Step-by-step ASCII trace:**

```
Initial state:
  prev = null
  current -> [1] -> [2] -> [3] -> null

Step 1:
  next = current.next = [2]
  current.next = prev = null    // [1] now points to null
  prev = current = [1]
  current = next = [2]

  null <- [1]    [2] -> [3] -> null
           ^      ^
          prev  current

Step 2:
  next = current.next = [3]
  current.next = prev = [1]    // [2] now points to [1]
  prev = current = [2]
  current = next = [3]

  null <- [1] <- [2]    [3] -> null
                  ^       ^
                 prev   current

Step 3:
  next = current.next = null
  current.next = prev = [2]    // [3] now points to [2]
  prev = current = [3]
  current = next = null

  null <- [1] <- [2] <- [3]
                          ^
                         prev    current = null -> STOP

Return prev = [3] (new head)
Result: [3] -> [2] -> [1] -> null
```

**Recursive version (for understanding, iterative is preferred):**

```java
public ListNode reverseListRecursive(ListNode head) {
    // Base case: empty list or single node
    if (head == null || head.next == null) {
        return head;
    }

    // Reverse the rest of the list
    ListNode newHead = reverseListRecursive(head.next);

    // Make the next node point back to current
    head.next.next = head;
    head.next = null;

    return newHead;
}
```

---

#### Technique 3: Dummy Head (Simplify Edge Cases)

**The Problem:**
Many linked list operations have special cases for the HEAD node. For example,
deleting the first node requires different code than deleting a middle node.

**The Solution:**
Create a DUMMY node before the head. This ensures there is always a node
before the target, eliminating edge cases.

```java
// Without dummy head: must handle head deletion separately
public ListNode removeElements(ListNode head, int val) {
    // Special case: remove from head
    while (head != null && head.val == val) {
        head = head.next;
    }

    // Remove from rest of list
    ListNode current = head;
    while (current != null && current.next != null) {
        if (current.next.val == val) {
            current.next = current.next.next;
        } else {
            current = current.next;
        }
    }
    return head;
}


// With dummy head: clean and uniform
public ListNode removeElementsDummy(ListNode head, int val) {
    ListNode dummy = new ListNode(0);   // dummy node
    dummy.next = head;                  // dummy points to real head

    ListNode current = dummy;
    while (current.next != null) {
        if (current.next.val == val) {
            current.next = current.next.next;  // skip the node
        } else {
            current = current.next;
        }
    }

    return dummy.next;  // real head (might have changed!)
}
```

**Visual:**

```
Before: head -> [6] -> [1] -> [6] -> [3] -> [6] -> null
Remove all 6s.

With dummy:
  dummy -> [6] -> [1] -> [6] -> [3] -> [6] -> null
   ^
  current

  current.next.val == 6? Yes! Skip it.
  dummy -> [1] -> [6] -> [3] -> [6] -> null

  current = [1]? No wait, current is still dummy.
  current.next is now [1]. val=1 != 6. Move to [1].
  current = [1]. current.next = [6]. val=6! Skip.
  dummy -> [1] -> [3] -> [6] -> null

  current.next = [3]. val=3 != 6. Move to [3].
  current = [3]. current.next = [6]. val=6! Skip.
  dummy -> [1] -> [3] -> null

Return dummy.next = [1].
```

**When to use dummy head:**
- Merging two sorted lists
- Removing elements by value
- Inserting at a position
- Any time the head might change

---

#### Technique 4: Merge Two Sorted Linked Lists

```java
public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
    ListNode dummy = new ListNode(0);  // dummy head
    ListNode current = dummy;

    while (l1 != null && l2 != null) {
        if (l1.val <= l2.val) {
            current.next = l1;   // take from list 1
            l1 = l1.next;
        } else {
            current.next = l2;   // take from list 2
            l2 = l2.next;
        }
        current = current.next;
    }

    // Attach remaining nodes (one list might have leftover)
    current.next = (l1 != null) ? l1 : l2;

    return dummy.next;
}
```

**Trace with l1=[1,2,4], l2=[1,3,4]:**

```
dummy -> ?

Step 1: l1=1, l2=1. 1<=1, take l1. dummy -> [1]
Step 2: l1=2, l2=1. 2>1, take l2. dummy -> [1] -> [1]
Step 3: l1=2, l2=3. 2<=3, take l1. dummy -> [1] -> [1] -> [2]
Step 4: l1=4, l2=3. 4>3, take l2. dummy -> [1] -> [1] -> [2] -> [3]
Step 5: l1=4, l2=4. 4<=4, take l1. dummy -> [1] -> [1] -> [2] -> [3] -> [4]
Step 6: l1=null, l2=4. Attach remaining: ... -> [4]

Result: [1] -> [1] -> [2] -> [3] -> [4] -> [4]
```

---

### 7. Java's Built-in LinkedList

```java
import java.util.LinkedList;

LinkedList<Integer> list = new LinkedList<>();

// Add
list.addFirst(10);    // [10]
list.addLast(30);     // [10, 30]
list.add(1, 20);      // [10, 20, 30]  -- add at index 1

// Access
list.getFirst();      // 10
list.getLast();        // 30
list.get(1);          // 20

// Remove
list.removeFirst();   // removes 10, list = [20, 30]
list.removeLast();    // removes 30, list = [20]

// Size
list.size();          // 1

// The built-in LinkedList is a DOUBLY linked list.
// It also implements the Queue and Deque interfaces.
```

---

### 8. When to Use Which Data Structure?

| Need                                     | Use                    |
|------------------------------------------|------------------------|
| LIFO (reverse order processing)          | Stack                  |
| FIFO (first come first served)           | Queue                  |
| Process by priority                      | PriorityQueue          |
| Frequent insert/delete at beginning      | Linked List            |
| Random access by index                   | Array/ArrayList        |
| Next greater/smaller element             | Monotonic Stack        |
| BFS traversal                            | Queue                  |
| DFS traversal (iterative)               | Stack                  |
| Undo/redo, bracket matching             | Stack                  |
| Task scheduling in order                 | Queue                  |

---

### 9. Common Mistakes

1. **Stack: Peeking or popping an empty stack**
   ```java
   // WRONG: throws exception
   stack.pop();  // when stack is empty!

   // CORRECT: always check first
   if (!stack.isEmpty()) {
       stack.pop();
   }
   ```

2. **Queue: Using add() instead of offer()**
   ```java
   // add() throws exception if queue is full (for bounded queues)
   // offer() returns false -- safer
   queue.offer(element);  // prefer this
   ```

3. **Linked List: Losing the head pointer**
   ```java
   // WRONG: after this loop, you lost the head!
   while (head != null) {
       head = head.next;
   }

   // CORRECT: use a separate pointer
   ListNode current = head;
   while (current != null) {
       current = current.next;
   }
   // head is still intact
   ```

4. **Linked List: NullPointerException**
   ```java
   // WRONG: current.next.val when current.next could be null
   while (current.next.val != target) { ... }

   // CORRECT: check for null first
   while (current.next != null && current.next.val != target) { ... }
   ```

5. **Linked List: Forgetting to update pointers**
   ```java
   // When inserting, order of pointer updates MATTERS!
   // WRONG: lose the rest of the list
   current.next = newNode;
   newNode.next = current.next;  // current.next is now newNode, infinite loop!

   // CORRECT: save next first, then update
   newNode.next = current.next;   // new node points to the rest
   current.next = newNode;        // current points to new node
   ```

6. **Confusing ArrayDeque usage as Stack vs Queue**
   ```java
   Deque<Integer> deque = new ArrayDeque<>();

   // As STACK:   push/pop/peek    (operate on front)
   // As QUEUE:   offer/poll/peek  (add at back, remove from front)

   // Do NOT mix: push (adds to front) + poll (removes from front) = LIFO, not FIFO!
   ```

---

### 10. Quick Summary

```
+-------------------+------------------------------------------+
| Concept           | Key Takeaway                             |
+-------------------+------------------------------------------+
| Stack             | LIFO, push/pop O(1), use ArrayDeque      |
| Queue             | FIFO, offer/poll O(1), use ArrayDeque    |
| Deque             | Both ends, addFirst/addLast              |
| PriorityQueue     | Min-heap default, poll gives smallest    |
| Singly Linked List| Node has val + next pointer              |
| Doubly Linked List| Node has val + next + prev pointers      |
| Monotonic Stack   | Next greater/smaller in O(n)             |
| Fast-Slow Pointer | Find middle, detect cycles               |
| Reverse LL        | Three pointers: prev, current, next      |
| Dummy Head        | Eliminates head edge cases               |
| Merge Sorted LLs  | Two pointers + dummy head                |
+-------------------+------------------------------------------+

Time Complexities to MEMORIZE:
  Stack push/pop/peek:       O(1)
  Queue offer/poll/peek:     O(1)
  PriorityQueue offer/poll:  O(log n)
  PriorityQueue peek:        O(1)
  LL access by index:        O(n)
  LL insert at beginning:    O(1)
  LL search:                 O(n)
  Monotonic stack problems:  O(n)
  LL reversal:               O(n) time, O(1) space
```

---

**You are now ready to tackle Week 4 LeetCode problems!**
Stack, Queue, and Linked List are foundational data structures.
Every tree and graph algorithm builds on these. Master them well
before moving to Week 5 (Trees).
