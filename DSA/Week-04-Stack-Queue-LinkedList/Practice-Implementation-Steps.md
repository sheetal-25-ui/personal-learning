# Stack, Queue, LinkedList — Implementation Practice Steps

> Goal: Learn to **build these data structures yourself** (from scratch), without using built-in libraries.
> Rule: First UNDERSTAND → then BUILD → then PRACTISE. Don't rush. One structure per day.

---

## How to practise each day (simple routine)

1. First read the **concept** (given here).
2. Try to **write the code yourself** (on paper or in an editor) without looking.
3. If you get stuck, look at the solution below, then **close it and write again**.
4. Finish with 2-3 small **practice problems**.

If it's not correct the first time — that's normal. Write it 3 times and it'll stick.

---

# PART 1 — STACK

### Concept (in one line)
Stack = **LIFO** (Last In First Out). Like a pile of plates — the last plate you put on top is the first one you pick up.

### Real life examples
- Browser **back button** (last page opens first)
- **Undo** (Ctrl+Z) — last action gets undone first
- A stack of plates / books

### Stack operations (memorize these)
| Operation | What it does |
|-----------|--------------|
| `push(x)` | Add element on top |
| `pop()`   | Remove the top element |
| `peek()`  | Look at top (don't remove) |
| `isEmpty()` | Is it empty? |
| `size()`  | How many elements |

---

## STEP 1 — Stack using Array (start here)

**Idea:** Take an array. Keep a variable `top` that tracks where the last element is.
- Initially `top = -1` (means empty).
- In `push`, increase `top` by 1, then place the value there.
- In `pop`, take the value, then decrease `top` by 1.

```java
class ArrayStack {
    private int[] arr;
    private int top;
    private int capacity;

    public ArrayStack(int size) {
        arr = new int[size];
        capacity = size;
        top = -1;          // -1 means empty stack
    }

    public void push(int x) {
        if (top == capacity - 1) {   // no space left
            System.out.println("Stack Full");
            return;
        }
        top++;              // first move the pointer up
        arr[top] = x;       // then place the value
    }

    public int pop() {
        if (isEmpty()) {
            System.out.println("Stack Empty");
            return -1;
        }
        int value = arr[top];  // first grab the value
        top--;                 // then move pointer down
        return value;
    }

    public int peek() {
        if (isEmpty()) return -1;
        return arr[top];       // just look, don't change top
    }

    public boolean isEmpty() {
        return top == -1;
    }

    public int size() {
        return top + 1;
    }
}
```

**Practise:** Write this twice without looking. Then test:
```java
ArrayStack s = new ArrayStack(5);
s.push(10); s.push(20); s.push(30);
System.out.println(s.pop());   // 30
System.out.println(s.peek());  // 20
System.out.println(s.size());  // 2
```

---

## STEP 2 — Stack using LinkedList (do this after the array version)

**Why?** Array has a fixed size. A LinkedList can grow without limit.

**Idea:** Each element is a "Node". Always add the new element at the **head** (front) — that's the top.

```java
class StackLL {
    // Small node — value + pointer to next node
    private class Node {
        int data;
        Node next;
        Node(int data) { this.data = data; }
    }

    private Node top;   // the head is the top
    private int count;

    public void push(int x) {
        Node node = new Node(x);
        node.next = top;   // new node points to the old top
        top = node;        // now the new node is the top
        count++;
    }

    public int pop() {
        if (isEmpty()) return -1;
        int value = top.data;
        top = top.next;    // move top forward
        count--;
        return value;
    }

    public int peek() {
        if (isEmpty()) return -1;
        return top.data;
    }

    public boolean isEmpty() { return top == null; }
    public int size() { return count; }
}
```

### Stack — Practice Problems (solve using push/pop)
1. **Reverse a string** using a stack (push each char, then pop and join).
2. **Valid Parentheses** — is `"(){}[]"` balanced? (LeetCode 20).
3. **Min Stack** — keep a second stack that tracks the minimum (LeetCode 155).
4. **Next Greater Element** (LeetCode 496).

---

# PART 2 — QUEUE

### Concept (in one line)
Queue = **FIFO** (First In First Out). Like people standing in a line — whoever comes first, leaves first.

### Real life examples
- Bank / ticket **line**
- Printer queue (the file sent first prints first)
- WhatsApp message delivery

### Queue operations
| Operation | What it does |
|-----------|--------------|
| `enqueue(x)` | Add at the back (rear) |
| `dequeue()`  | Remove from the front |
| `front()`    | Look at the front element |
| `isEmpty()`  | Is it empty |

> Remember: In a Stack both operations happen on one side (top). In a Queue you **add on one side, remove on the other**.

---

## STEP 3 — Queue using LinkedList (easiest way to build a queue)

**Idea:** Keep two pointers — `front` (where you remove) and `rear` (where you add).
- `enqueue` → attach a new node at the rear.
- `dequeue` → remove from the front.

```java
class QueueLL {
    private class Node {
        int data;
        Node next;
        Node(int data) { this.data = data; }
    }

    private Node front, rear;
    private int count;

    public void enqueue(int x) {
        Node node = new Node(x);
        if (rear == null) {          // queue was empty
            front = rear = node;
        } else {
            rear.next = node;        // attach after the old rear
            rear = node;             // move rear forward
        }
        count++;
    }

    public int dequeue() {
        if (isEmpty()) return -1;
        int value = front.data;
        front = front.next;          // move front forward
        if (front == null) rear = null;  // now it's empty
        count--;
        return value;
    }

    public int front() {
        if (isEmpty()) return -1;
        return front.data;
    }

    public boolean isEmpty() { return front == null; }
    public int size() { return count; }
}
```

**Test:**
```java
QueueLL q = new QueueLL();
q.enqueue(1); q.enqueue(2); q.enqueue(3);
System.out.println(q.dequeue()); // 1  (first in, first out)
System.out.println(q.front());   // 2
```

---

## STEP 4 — Circular Queue using Array (a bit advanced — do this later)

**Problem:** In a normal array queue, as `front` moves forward the space at the start gets wasted.
**Solution:** Treat the array as **circular** — wrap back to the start using modulo `%`.

```java
class CircularQueue {
    private int[] arr;
    private int front, rear, count, capacity;

    public CircularQueue(int size) {
        arr = new int[size];
        capacity = size;
        front = 0; rear = -1; count = 0;
    }

    public void enqueue(int x) {
        if (count == capacity) { System.out.println("Full"); return; }
        rear = (rear + 1) % capacity;   // wrap around back to 0
        arr[rear] = x;
        count++;
    }

    public int dequeue() {
        if (count == 0) { System.out.println("Empty"); return -1; }
        int value = arr[front];
        front = (front + 1) % capacity; // wrap around back to 0
        count--;
        return value;
    }

    public boolean isEmpty() { return count == 0; }
}
```

> What `% capacity` means: when an index reaches `capacity`, it wraps back to 0. That's the "circular" magic.

### Queue — Practice Problems
1. **Implement Stack using Queues** (LeetCode 225).
2. **Implement Queue using Stacks** (LeetCode 232).
3. **Design Circular Queue** (LeetCode 622).
4. **Number of Recent Calls** (LeetCode 933).

---

# PART 3 — LINKED LIST

### Concept (in one line)
LinkedList = a chain of nodes. Each node holds **data** + the **address of the next node (next)**. Unlike an array, the memory isn't continuous — the nodes are scattered but linked by the chain.

### Array vs LinkedList (interviewers ask this)
| | Array | LinkedList |
|---|-------|-----------|
| Memory | Continuous (together) | Scattered (linked by next) |
| Size | Fixed | Grow/shrink freely |
| Access by index | Fast O(1) | Slow O(n) (must walk) |
| Insert at start | Slow O(n) (shift) | Fast O(1) |

### What a Node looks like
```java
class Node {
    int data;
    Node next;     // address of the next node
    Node(int data) { this.data = data; this.next = null; }
}
```
Visual: `[10|•] → [20|•] → [30|null]`

---

## STEP 5 — Build a Singly LinkedList (learn this by heart, everything builds on it)

```java
class LinkedList {
    Node head;   // the first node — you hold the whole list through this

    class Node {
        int data;
        Node next;
        Node(int data) { this.data = data; }
    }

    // 1. Add at the end
    public void insertEnd(int x) {
        Node node = new Node(x);
        if (head == null) { head = node; return; }
        Node temp = head;
        while (temp.next != null) temp = temp.next;  // walk to the last node
        temp.next = node;                            // attach after the last
    }

    // 2. Add at the start (O(1) — fast)
    public void insertFront(int x) {
        Node node = new Node(x);
        node.next = head;   // new node points to the old head
        head = node;        // new head
    }

    // 3. Delete a value
    public void delete(int x) {
        if (head == null) return;
        if (head.data == x) { head = head.next; return; }  // delete the head
        Node temp = head;
        while (temp.next != null && temp.next.data != x) temp = temp.next;
        if (temp.next != null) temp.next = temp.next.next; // skip over the node
    }

    // 4. Print
    public void print() {
        Node temp = head;
        while (temp != null) {
            System.out.print(temp.data + " -> ");
            temp = temp.next;
        }
        System.out.println("null");
    }
}
```

**Test:**
```java
LinkedList list = new LinkedList();
list.insertEnd(10);
list.insertEnd(20);
list.insertFront(5);
list.print();        // 5 -> 10 -> 20 -> null
list.delete(10);
list.print();        // 5 -> 20 -> null
```

---

## STEP 6 — LinkedList MUST-KNOW algorithms

These 3 come up most in interviews. Don't memorize blindly — understand, then remember.

### A) Reverse a LinkedList (super important)
**Idea:** 3 pointers — `prev`, `curr`, `next`. Flip each node's arrow as you go.
```java
public Node reverse(Node head) {
    Node prev = null;
    Node curr = head;
    while (curr != null) {
        Node next = curr.next;  // remember the next (or you'll lose it)
        curr.next = prev;       // flip the arrow
        prev = curr;            // move prev forward
        curr = next;            // move curr forward
    }
    return prev;   // prev is the new head
}
```

### B) Find Middle (Slow-Fast pointer trick)
**Idea:** `slow` moves 1 step, `fast` moves 2 steps. When fast reaches the end, slow is at the middle.
```java
public Node findMiddle(Node head) {
    Node slow = head, fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;        // 1 step
        fast = fast.next.next;   // 2 steps
    }
    return slow;  // middle
}
```

### C) Detect Cycle (Floyd's — is there a loop?)
**Idea:** Run slow and fast together. If there's a loop, they'll meet at some point.
```java
public boolean hasCycle(Node head) {
    Node slow = head, fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
        if (slow == fast) return true;  // they met = loop exists
    }
    return false;
}
```

### LinkedList — Practice Problems
1. **Reverse Linked List** (LeetCode 206).
2. **Middle of the Linked List** (LeetCode 876).
3. **Linked List Cycle** (LeetCode 141).
4. **Merge Two Sorted Lists** (LeetCode 21).
5. **Remove Nth Node From End** (LeetCode 19).
6. **Palindrome Linked List** (LeetCode 234) — uses both middle + reverse.

---

# Your 6-Day Practice Plan (~45 min/day)

| Day | What to build | Practice |
|-----|---------------|----------|
| Day 1 | Stack using Array | Reverse string, Valid Parentheses |
| Day 2 | Stack using LinkedList | Min Stack |
| Day 3 | Queue using LinkedList | Queue using Stacks |
| Day 4 | Circular Queue | Design Circular Queue (LC 622) |
| Day 5 | Singly LinkedList (insert/delete/print) | Rewrite insertFront, delete |
| Day 6 | LinkedList Reverse + Middle + Cycle | LC 206, 876, 141 |

**Most important rule:** Write each structure yourself **at least 2 times** without looking. The first time you'll get stuck — that's normal. The second time you'll understand the flow. The third time you'll have it memorized.

---

## Quick memory shortcut

- **Stack** = LIFO = work on one side (top). Plates.
- **Queue** = FIFO = add at the back, remove at the front. A line.
- **LinkedList** = a chain of nodes = data + next. Everything starts from `head`.

That's it. One structure a day, slow and correct. You've got this. 💪
