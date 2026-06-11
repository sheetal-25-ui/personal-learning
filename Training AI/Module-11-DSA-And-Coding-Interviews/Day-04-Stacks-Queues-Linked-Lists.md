# Module 11 — Day 4: Stacks, Queues, and Linked Lists

## Today's Theme: Order Matters

So far we've worked with arrays and hash maps. Today we learn three structures where **the order things go in and come out** is the whole point:

- **Stack** — Last In, First Out (LIFO)
- **Queue** — First In, First Out (FIFO)
- **Linked List** — items chained together by pointers

These show up constantly: stacks power "undo" and bracket-matching, queues power task schedulers and BFS (Day 5), and linked lists are a perennial interview favorite for testing pointer manipulation.

---

## Stacks (Last In, First Out)

### The intuition: a stack of plates

```
   push →  ┌─────┐
           │  3  │  ← top (last in, first out)
           ├─────┤
           │  2  │
           ├─────┤
           │  1  │  ← bottom (first in)
           └─────┘
   pop  ←  takes the TOP plate (3) first
```

You can only add to the top (`push`) and remove from the top (`pop`). The **last** thing you put on is the **first** thing you take off — just like a stack of plates.

### Stacks in Python (just use a list!)

```python
stack = []

stack.append(1)   # push  → [1]
stack.append(2)   # push  → [1, 2]
stack.append(3)   # push  → [1, 2, 3]

print(stack[-1])  # peek at top → 3  (look without removing)
print(stack.pop())  # pop → 3, stack is now [1, 2]
print(stack.pop())  # pop → 2, stack is now [1]
print(len(stack) == 0)  # is it empty? → False
```

> Both `append()` and `pop()` (from the end) are **O(1)**. A Python list IS a stack — no special import needed.

### When to use a Stack

- **Matching pairs** — brackets, parentheses, HTML tags.
- **Undo / history** — the most recent action is undone first.
- **"Most recent" logic** — backtracking, DFS (Day 5), expression evaluation.

### Classic Problem: Valid Parentheses

Given a string of brackets, decide if they're properly matched and nested. `"({[]})"` is valid; `"([)]"` is not.

```python
def is_valid(s):
    stack = []
    pairs = {")": "(", "]": "[", "}": "{"}   # closing → matching opening

    for char in s:
        if char in pairs:                # it's a CLOSING bracket
            # pop the last opening bracket; use "#" if stack is empty
            top = stack.pop() if stack else "#"
            if pairs[char] != top:       # mismatch!
                return False
        else:                            # it's an OPENING bracket
            stack.append(char)

    return len(stack) == 0               # valid only if nothing's left over

print(is_valid("()[]{}"))   # Output: True
print(is_valid("(]"))        # Output: False
print(is_valid("([)]"))      # Output: False
print(is_valid("{[]}"))      # Output: True
```

**Why a stack is perfect:** every closing bracket must match the **most recently opened** one that isn't closed yet — that's exactly "last in, first out." O(n) time.

---

## Queues (First In, First Out)

### The intuition: a line at a coffee shop

```
   enqueue (back)                    dequeue (front)
        →   [ 3 ] [ 2 ] [ 1 ]   →
            back            front

   First person in line (1) is served first.
```

You add to the **back** and remove from the **front**. The **first** thing in is the **first** thing out — like a fair queue at a shop.

### Queues in Python (use `deque`!)

> **Important:** Don't use a plain list as a queue. Removing from the front with `list.pop(0)` is **O(n)** (it shifts every element). Use `collections.deque` — its front operations are **O(1)**.

```python
from collections import deque

queue = deque()

queue.append(1)        # enqueue (add to back)  → [1]
queue.append(2)        #                          → [1, 2]
queue.append(3)        #                          → [1, 2, 3]

print(queue.popleft()) # dequeue (remove front) → 1, queue is [2, 3]
print(queue.popleft()) #                          → 2, queue is [3]
print(queue[0])        # peek at front           → 3
print(len(queue))      # size                     → 1
```

### When to use a Queue

- **Process things in arrival order** — task schedulers, print queues.
- **BFS (Breadth-First Search)** on trees and graphs — *the* big one (Day 5).
- **"Levels" or "waves"** — processing things layer by layer.

### Quick Example: Simulating a Ticket Queue

```python
from collections import deque

def serve_customers(customers):
    queue = deque(customers)
    order = []
    while queue:
        person = queue.popleft()   # serve the front person — O(1)
        order.append(person)
    return order

print(serve_customers(["Asha", "Ben", "Chen"]))
# Output: ['Asha', 'Ben', 'Chen']  — served in arrival order
```

---

## Stack vs. Queue — the one-line difference

```
STACK (LIFO):  add to top,    remove from top     →  reverses order
QUEUE (FIFO):  add to back,   remove from front    →  preserves order
```

| | Stack | Queue |
|---|---|---|
| Python tool | `list` | `collections.deque` |
| Add | `append()` | `append()` |
| Remove | `pop()` (end) | `popleft()` (front) |
| Used in | DFS, undo, brackets | BFS, scheduling |

---

## Linked Lists

### The intuition: a treasure hunt

An array stores items in one contiguous block. A **linked list** stores each item in its own little box (a **node**), and each box holds a **pointer** to the next box — like clues in a treasure hunt.

```
   ┌────┬───┐    ┌────┬───┐    ┌────┬───┐    
   │ 1  │ •─┼──▶ │ 2  │ •─┼──▶ │ 3  │ ✗ │ (None = end)
   └────┴───┘    └────┴───┘    └────┴───┘
    val  next     val  next     val  next
   ▲
   head (entry point)
```

Each node has two parts: the **value** and a pointer (`next`) to the following node. The last node points to `None`.

### Array vs. Linked List trade-offs

| Operation | Array (list) | Linked List |
|---|---|---|
| Access by index `[i]` | **O(1)** ✅ | O(n) — must walk from head |
| Insert/delete at front | O(n) — shift all | **O(1)** ✅ |
| Insert/delete in middle (given the node) | O(n) | **O(1)** ✅ |
| Memory | contiguous block | scattered nodes + pointers |

> **Why interviews love linked lists:** they test whether you can carefully manipulate pointers without losing track of nodes — a real skill that reveals careful thinking. You rarely use them in day-to-day Python, but they're an interview rite of passage.

### Defining a Linked List in Python

```python
class ListNode:
    def __init__(self, val=0, next=None):
        self.val = val      # the data
        self.next = next    # pointer to the next node (or None)

# Build the list 1 → 2 → 3 manually
head = ListNode(1)
head.next = ListNode(2)
head.next.next = ListNode(3)

# Traverse it
current = head
while current:               # stop when we hit None
    print(current.val, end=" → ")
    current = current.next   # follow the pointer
print("None")
# Output: 1 → 2 → 3 → None
```

### Classic Problem: Reverse a Linked List

This is *the* most-asked linked list question. The trick: walk through, flipping each `next` pointer to point **backwards**.

```python
def reverse_list(head):
    prev = None          # the reversed part starts empty
    current = head

    while current:
        next_node = current.next   # 1. save the next node before we lose it
        current.next = prev        # 2. flip the pointer backwards
        prev = current             # 3. move prev forward
        current = next_node        # 4. move current forward

    return prev          # prev is the new head

# Test it
head = ListNode(1, ListNode(2, ListNode(3)))
reversed_head = reverse_list(head)

current = reversed_head
while current:
    print(current.val, end=" → ")
    current = current.next
print("None")
# Output: 3 → 2 → 1 → None
```

**Visualizing the pointer flip:**

```
Before:  None    1 → 2 → 3 → None
                 ↑
              current

Step 1:  None ← 1   2 → 3 → None   (flip 1's pointer to None)
After:   3 → 2 → 1 → None
```

O(n) time, O(1) space. Practice this until you can write it without thinking.

### The "Two Pointer" Trick for Linked Lists: Fast & Slow

A famous technique uses two pointers moving at different speeds. The **slow** pointer moves one step, the **fast** pointer moves two. This detects cycles and finds the middle.

```python
def find_middle(head):
    slow = fast = head
    # fast moves 2x as fast — when fast hits the end, slow is in the middle
    while fast and fast.next:
        slow = slow.next          # 1 step
        fast = fast.next.next     # 2 steps
    return slow

head = ListNode(1, ListNode(2, ListNode(3, ListNode(4, ListNode(5)))))
print(find_middle(head).val)   # Output: 3  (the middle of 1-2-3-4-5)
```

This same fast/slow idea (called **Floyd's cycle detection** or the "tortoise and hare") detects if a linked list loops back on itself — a very common interview question.

> **Pro tip — the dummy node:** Many linked list problems get cleaner if you create a fake "dummy" node before the head. It saves you from special-casing the empty list or the first element. Learn this trick once and reuse it everywhere.

---

## Decision Guide: Which Structure?

```
Need to...                                    →  Use...
─────────────────────────────────────────────────────────
match/undo, "most recent first"               →  Stack (list)
process in arrival order, BFS                  →  Queue (deque)
detect cycle / find middle of a list           →  Fast & slow pointers
insert/delete at front constantly              →  Linked list (or deque)
random access by index                         →  Array (list)
```

---

## Practice Exercises

For each, identify the structure *before* coding. State the Big-O.

### Exercise 1: "Valid Parentheses" — LeetCode #20 (Easy)
The exact problem above. Implement the **stack** solution from scratch. Asked everywhere.

### Exercise 2: "Implement Queue using Stacks" — LeetCode #232 (Easy)
Build a FIFO queue using only two LIFO stacks.
- This teaches you the relationship between the two structures deeply. A favorite "do you really understand these?" question.

### Exercise 3: "Reverse Linked List" — LeetCode #206 (Easy)
The most-asked linked list problem. Write the iterative version (above), then try the recursive version too.

### Exercise 4: "Linked List Cycle" — LeetCode #141 (Easy/Medium)
Detect if a linked list has a cycle.
- Use the **fast & slow pointer** technique. If they ever meet, there's a cycle. O(n) time, O(1) space.

### Exercise 5: "Daily Temperatures" — LeetCode #739 (Medium)
For each day, find how many days until a warmer temperature.
- Use a **monotonic stack** (a stack that stays sorted). A powerful pattern that comes up a lot at Google & Amazon. Aim for **O(n)**.

---

## Quick Reference Card

```python
# STACK (LIFO) — use a list
stack = []
stack.append(x)    # push    O(1)
stack.pop()        # pop top  O(1)
stack[-1]          # peek

# QUEUE (FIFO) — use deque, NOT list!
from collections import deque
q = deque()
q.append(x)        # enqueue  O(1)
q.popleft()        # dequeue  O(1)
q[0]               # peek front

# LINKED LIST NODE
class ListNode:
    def __init__(self, val=0, next=None):
        self.val = val
        self.next = next

# REVERSE A LINKED LIST
prev = None
while current:
    nxt = current.next
    current.next = prev
    prev = current
    current = nxt
# prev is the new head

# FAST & SLOW POINTERS (middle / cycle detection)
slow = fast = head
while fast and fast.next:
    slow = slow.next
    fast = fast.next.next
```

---

## What's Next?

You've now got the linear structures down — and you've met the **queue**, which is the engine behind one of tomorrow's big techniques. In **Day 05 — Trees and Graphs**, we tackle the topics most people find intimidating: binary trees, traversals, and graph search with **DFS** (built on stacks/recursion) and **BFS** (built on queues). With four days of foundation under you, you're more ready than you think. Let's climb the tree!
