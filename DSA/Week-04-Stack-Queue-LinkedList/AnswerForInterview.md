# Week 4: Stack, Queue & Linked List — How to ANSWER in a Microsoft Interview 🎤 (Java)

Welcome! This file is your **interview script** for Week 4. The goal isn't just to *solve* these problems — it's to *talk through them* like a Microsoft engineer would. Interviewers grade your **communication, structure, and trade-off thinking** as much as your code.

---

## 🏆 The Golden Rule of Every Answer

Always walk the interviewer down this staircase — out loud:

> **Clarify → Brute Force → Optimal → Best → Complexity**

Never jump straight to the optimal code. The interviewer wants to *see your thinking*, not just the destination. Saying "the brute force is X, but we can do better by…" is worth real points.

### 🗣️ The Interview Script (what to literally say, in order)

1. **Repeat the problem back.** "So I'm given a linked list, and I need to reverse it in place — let me confirm I understand…"
2. **Ask clarifying questions.** (See the per-problem lists below.) This shows maturity.
3. **State a brute force.** "The naive approach would be… it works but it's O(n²) / uses extra space."
4. **Propose the optimal.** "We can do better using a stack / two pointers / a HashMap. Here's the idea…"
5. **Dry-run a tiny example out loud** before coding. Draw the boxes-and-arrows on the whiteboard.
6. **Code it cleanly**, narrating as you go ("dummy head guards the empty case…").
7. **State complexity** — time AND space — and justify it.
8. **Test it** on the example + an edge case (empty input, single node, duplicates).

### 🔎 Common Clarifying Questions (memorize these — they apply across the week)

- "Can the input be **empty** or **null**?"
- "Are there **duplicate** values? Negative numbers?"
- "Can I **modify the input** in place, or must I keep it intact?"
- "What's the expected **size** of the input? (Hints at whether O(n²) is acceptable.)"
- "Should I optimize for **time or space**?"
- "For a Linked List: is it **singly or doubly** linked? Is there a tail pointer?"
- "Is the list **sorted**?"

---

## The Problems

> We'll cover the 8 highest-value problems for Microsoft: **Valid Parentheses, Min Stack, Daily Temperatures, Reverse Linked List, Merge Two Sorted Lists, Linked List Cycle, Copy List with Random Pointer, and LRU Cache.**

First, the shared building block you'll reuse all week:

```java
// The standard singly linked list node — interviewers usually provide this.
class ListNode {
    int val;
    ListNode next;
    ListNode() {}
    ListNode(int val) { this.val = val; }
    ListNode(int val, ListNode next) { this.val = val; this.next = next; }
}
```

---

### 1️⃣ Valid Parentheses  ⭐ (Microsoft Classic)

📋 **Full Question**
Given a string `s` containing just the characters `()[]{}`, determine if the input is valid. Brackets must close in the **correct order** and **type**.
- Input: `s = "([{}])"` → Output: `true`
- Input: `s = "([)]"` → Output: `false`
- Input: `s = "(]"` → Output: `false`

🗣️ **What to say first**
- "Can the string contain other characters (letters), or only brackets?"
- "Is an empty string considered valid?" (Usually yes.)
- "Can I assume ASCII input?"

**Approach 1 — Brute Force (repeated string removal)**
Repeatedly delete adjacent matching pairs (`()`, `[]`, `{}`) until nothing changes. If the string is empty at the end, it's valid.

```java
public boolean isValidBrute(String s) {
    int prevLen = -1;
    while (s.length() != prevLen) {       // loop until no more replacements happen
        prevLen = s.length();
        s = s.replace("()", "").replace("[]", "").replace("{}", "");
    }
    return s.isEmpty();
}
```
- **Time:** O(n²) — each pass is O(n) and we may do O(n) passes. **Space:** O(n).
- ❌ Why it's not enough: rebuilding the string every pass is wasteful — a stack does it in one pass.

**Approach 2 — Optimal (Stack)**
Push every opening bracket. On a closing bracket, the top of the stack **must** be its matching opener — otherwise it's invalid. At the end the stack must be empty.

```java
public boolean isValid(String s) {
    Deque<Character> stack = new ArrayDeque<>();   // ArrayDeque is the fast modern stack
    for (char c : s.toCharArray()) {
        if (c == '(') stack.push(')');
        else if (c == '[') stack.push(']');
        else if (c == '{') stack.push('}');
        // c is a closing bracket: it must equal what we expect on top
        else if (stack.isEmpty() || stack.pop() != c) return false;
    }
    return stack.isEmpty();   // leftover openers => invalid
}
```
*Trick:* push the **expected closer** so the comparison is a single `==`.

**🔬 Dry-run on `"([{}])"`:**
| char | action | stack (top→bottom) |
|------|--------|--------------------|
| `(` | push `)` | `)` |
| `[` | push `]` | `] )` |
| `{` | push `}` | `} ] )` |
| `}` | pop `}` ✅ | `] )` |
| `]` | pop `]` ✅ | `)` |
| `)` | pop `)` ✅ | *(empty)* |

Stack empty → `true`. ✅

- **Time:** O(n). **Space:** O(n).

🧠 **Algorithm to Remember Forever**
- **Pattern:** *Stack for matching/nesting.*
- **Core idea:** The most recent unmatched opener is what a closer must match → LIFO → stack.
- **Memory hook:** 🥞 *A stack of plates — the last plate you put down is the first you pick up.* Every `}` must "see" its `{` right on top.
- **Trigger phrase:** "valid", "balanced", "matching brackets", "nested", "well-formed".

---

### 2️⃣ Min Stack  ⭐ (Frequent at Microsoft)

📋 **Full Question**
Design a stack that supports `push`, `pop`, `top`, and retrieving the **minimum element — all in O(1)**.
- `push(-2); push(0); push(-3); getMin()` → `-3`; `pop(); top()` → `0`; `getMin()` → `-2`.

🗣️ **What to say first**
- "Will `getMin`/`pop`/`top` ever be called on an empty stack?" (Assume valid calls.)
- "Can values be duplicated?" (Yes — important for the second stack.)
- "Are values within `int` range?"

**Approach 1 — Brute Force (scan for min)**
One normal stack; `getMin` loops through all elements to find the minimum.

```java
class MinStackBrute {
    private Deque<Integer> stack = new ArrayDeque<>();
    public void push(int x) { stack.push(x); }
    public void pop()       { stack.pop(); }
    public int top()        { return stack.peek(); }
    public int getMin() {                       // O(n) scan
        int min = Integer.MAX_VALUE;
        for (int v : stack) min = Math.min(min, v);
        return min;
    }
}
```
- **Time:** `getMin` is O(n). **Space:** O(n).
- ❌ Why it's not enough: the problem demands **O(1)** `getMin`.

**Approach 2 — Optimal (Two Stacks / min alongside)**
Keep a second stack whose top always holds the **current minimum**. Push to it the smaller of (new value, current min). Pop both together.

```java
class MinStack {
    private Deque<Integer> stack    = new ArrayDeque<>();
    private Deque<Integer> minStack = new ArrayDeque<>();   // top = min of everything below

    public void push(int x) {
        stack.push(x);
        // if minStack empty, x is the min; else keep the smaller
        minStack.push(minStack.isEmpty() ? x : Math.min(x, minStack.peek()));
    }
    public void pop() { stack.pop(); minStack.pop(); }       // stay in lock-step
    public int top()    { return stack.peek(); }
    public int getMin() { return minStack.peek(); }          // O(1)!
}
```

**🔬 Dry-run:** push -2 → min `[-2]`; push 0 → min(0,-2)= -2 → `[-2,-2]`; push -3 → min(-3,-2)=-3 → `[-2,-2,-3]`. `getMin()` = top of minStack = **-3** ✅. After `pop()`, minStack top = **-2** ✅.

- **Time:** O(1) for every operation. **Space:** O(n).

🧠 **Algorithm to Remember Forever**
- **Pattern:** *Auxiliary stack carrying a running aggregate.*
- **Core idea:** If you can't compute something fast on demand, **pre-compute and carry it** alongside each element.
- **Memory hook:** 🪜 *Each plate carries a sticky note saying "the smallest plate from me down."* Read the top note instantly.
- **Trigger phrase:** "O(1) min/max", "design a stack/queue that also returns…", "constant time getMin".

---

### 3️⃣ Daily Temperatures  ⭐ (Monotonic Stack)

📋 **Full Question**
Given an array `temperatures`, return an array `answer` where `answer[i]` is the number of days you have to wait after day `i` for a **warmer** temperature. If none, put `0`.
- Input: `[73,74,75,71,69,72,76,73]` → Output: `[1,1,4,2,1,1,0,0]`

🗣️ **What to say first**
- "If no warmer day ever comes, the answer is 0 — correct?"
- "Can temperatures be equal? (Equal isn't 'warmer'.)"
- "What's the value range? (LeetCode: 30–100.)"

**Approach 1 — Brute Force (nested loop)**
For each day, scan forward until a warmer day is found.

```java
public int[] dailyTemperaturesBrute(int[] t) {
    int n = t.length;
    int[] ans = new int[n];
    for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
            if (t[j] > t[i]) { ans[i] = j - i; break; }   // first warmer day wins
        }
    }
    return ans;
}
```
- **Time:** O(n²). **Space:** O(1) extra.
- ❌ Why it's not enough: with large arrays, O(n²) times out.

**Approach 2 — Optimal (Monotonic Decreasing Stack of indices)**
Walk left→right. Keep a stack of **indices** waiting for a warmer day. When today is warmer than the day at the stack's top, that older day's wait is resolved (`i - oldIndex`).

```java
public int[] dailyTemperatures(int[] t) {
    int n = t.length;
    int[] ans = new int[n];
    Deque<Integer> stack = new ArrayDeque<>();   // stores indices, temps decreasing top→down
    for (int i = 0; i < n; i++) {
        // today resolves every colder day still waiting on the stack
        while (!stack.isEmpty() && t[i] > t[stack.peek()]) {
            int prev = stack.pop();
            ans[prev] = i - prev;                // days waited
        }
        stack.push(i);
    }
    return ans;   // unresolved indices keep their default 0
}
```

**🔬 Dry-run on `[73,74,75,71,69,72,76,73]`:**
- i=0 (73): stack `[0]`
- i=1 (74>73): pop 0 → ans[0]=1; push → `[1]`
- i=2 (75>74): pop 1 → ans[1]=1; push → `[2]`
- i=3 (71): push → `[2,3]`
- i=4 (69): push → `[2,3,4]`
- i=5 (72>69→pop4 ans[4]=1; 72>71→pop3 ans[3]=2; 72<75 stop): push → `[2,5]`
- i=6 (76>72→pop5 ans[5]=1; 76>75→pop2 ans[2]=4): push → `[6]`
- i=7 (73): push → `[6,7]`. Leftovers stay 0.

Result: `[1,1,4,2,1,1,0,0]` ✅

- **Time:** O(n) — each index pushed/popped once. **Space:** O(n).

🧠 **Algorithm to Remember Forever**
- **Pattern:** *Monotonic Stack* (the king of "next greater/smaller element").
- **Core idea:** Keep a stack that stays sorted; when a new element breaks the order, it *resolves* everything it dominates.
- **Memory hook:** 🧗 *People queued waiting for a taller person. When someone taller arrives, everyone shorter who was waiting gets their answer and leaves.*
- **Trigger phrase:** "next greater", "next warmer/smaller", "span", "how many days/steps until…".

---

### 4️⃣ Reverse Linked List  ⭐⭐⭐ (Top 5 Microsoft — must do in 5 min)

📋 **Full Question**
Reverse a singly linked list and return the new head.
- Input: `1 → 2 → 3 → 4 → 5` → Output: `5 → 4 → 3 → 2 → 1`

🗣️ **What to say first**
- "Should I reverse **in place** (O(1) space) or can I build a new list?"
- "Can the list be **empty** or have a **single** node?" (Both should just work.)
- "Do you want the iterative or recursive solution? I'll do iterative — it's O(1) space."

**Approach 1 — Brute Force (extra storage)**
Push all values to a stack (or list), then rebuild. Works but uses O(n) extra space.

```java
public ListNode reverseListBrute(ListNode head) {
    Deque<Integer> stack = new ArrayDeque<>();
    for (ListNode p = head; p != null; p = p.next) stack.push(p.val);
    ListNode dummy = new ListNode(0), tail = dummy;
    while (!stack.isEmpty()) { tail.next = new ListNode(stack.pop()); tail = tail.next; }
    return dummy.next;
}
```
- **Time:** O(n). **Space:** O(n).
- ❌ Why it's not enough: we can reverse pointers in place with **O(1)** space.

**Approach 2 — Optimal (Iterative pointer flip)**
Walk the list, flipping each `next` pointer backward, carrying `prev`.

```java
public ListNode reverseList(ListNode head) {
    ListNode prev = null, curr = head;
    while (curr != null) {
        ListNode next = curr.next;   // 1. save the rest of the list
        curr.next = prev;            // 2. flip the arrow backward
        prev = curr;                 // 3. advance prev
        curr = next;                 // 4. advance curr
    }
    return prev;                     // prev is the new head
}
```

**🔬 Dry-run on `1→2→3`:**
| step | prev | curr | list so far |
|------|------|------|-------------|
| start | null | 1 | 1→2→3 |
| after | 1 | 2 | 1→null, 2→3 |
| after | 2 | 3 | 2→1→null, 3 |
| after | 3 | null | 3→2→1→null |

Return `prev = 3`. ✅

- **Time:** O(n). **Space:** O(1).

🧠 **Algorithm to Remember Forever**
- **Pattern:** *Three-pointer in-place reversal* (`prev`, `curr`, `next`).
- **Core idea:** Save next → flip → shuffle both pointers forward. The **order is sacred** — save before you flip, or you lose the tail.
- **Memory hook:** 🚂 *A train reversing track by track — you must hold onto the next car (`next`) before you uncouple, or it rolls away.*
- **Trigger phrase:** "reverse the list", "reverse in groups", "palindrome linked list" (uses this as a sub-step).

---

### 5️⃣ Merge Two Sorted Lists  ⭐ (Very Frequent)

📋 **Full Question**
Merge two **sorted** linked lists into one sorted list by splicing nodes together. Return the merged head.
- Input: `l1 = 1→2→4`, `l2 = 1→3→4` → Output: `1→1→2→3→4→4`

🗣️ **What to say first**
- "Both lists are already sorted ascending — confirm?"
- "Can either list be empty?" (Yes — return the other.)
- "Should I reuse existing nodes or create new ones?" (Reuse → O(1) extra space.)

**Approach 1 — Brute Force (collect & sort)**
Dump all values into a list, sort, rebuild.

```java
public ListNode mergeBrute(ListNode l1, ListNode l2) {
    List<Integer> vals = new ArrayList<>();
    for (ListNode p = l1; p != null; p = p.next) vals.add(p.val);
    for (ListNode p = l2; p != null; p = p.next) vals.add(p.val);
    Collections.sort(vals);                       // throws away the "already sorted" gift
    ListNode dummy = new ListNode(0), tail = dummy;
    for (int v : vals) { tail.next = new ListNode(v); tail = tail.next; }
    return dummy.next;
}
```
- **Time:** O((n+m) log(n+m)). **Space:** O(n+m).
- ❌ Why it's not enough: it **ignores that the lists are already sorted** — a merge is linear.

**Approach 2 — Optimal (Two-pointer merge with dummy head)**
Use a **dummy head** so you never special-case the first node. Always attach the smaller current node.

```java
public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
    ListNode dummy = new ListNode(0), tail = dummy;   // dummy avoids empty-head edge cases
    while (l1 != null && l2 != null) {
        if (l1.val <= l2.val) { tail.next = l1; l1 = l1.next; }
        else                  { tail.next = l2; l2 = l2.next; }
        tail = tail.next;
    }
    tail.next = (l1 != null) ? l1 : l2;   // append whatever remains (already sorted)
    return dummy.next;
}
```

**🔬 Dry-run on `1→2→4` and `1→3→4`:**
- compare 1,1 → take l1's 1 → `1`
- compare 2,1 → take l2's 1 → `1→1`
- compare 2,3 → take 2 → `1→1→2`
- compare 4,3 → take 3 → `1→1→2→3`
- compare 4,4 → take 4 → `1→1→2→3→4`
- l1 exhausted → append remaining `4` → `1→1→2→3→4→4` ✅

- **Time:** O(n+m). **Space:** O(1).

🧠 **Algorithm to Remember Forever**
- **Pattern:** *Two-pointer merge* (the merge half of merge sort) + *dummy head*.
- **Core idea:** Two sorted streams → repeatedly pick the smaller front. The dummy head removes the "is this the first node?" headache.
- **Memory hook:** 🤝 *Two sorted queues of people merging into one line — let the shorter person at each front step in next.*
- **Trigger phrase:** "merge sorted", "combine two lists", "merge k sorted" (this is the k=2 base case).

---

### 6️⃣ Linked List Cycle  ⭐ (Floyd's Tortoise & Hare)

📋 **Full Question**
Given the head of a linked list, determine if it has a **cycle** (some node's `next` points back to an earlier node).
- Input: `3→2→0→-4`, where `-4.next` points back to `2` → Output: `true`
- Input: `1→2` with no loop → Output: `false`

🗣️ **What to say first**
- "Can I modify the list (e.g., mark nodes)?" (If not → forces the two-pointer trick.)
- "Could the list be empty or a single node?" (Then no cycle, unless it points to itself.)
- "Do you want me to also return *where* the cycle starts?" (That's the follow-up, Cycle II.)

**Approach 1 — Brute Force (HashSet of visited nodes)**
Track every node seen; if we revisit one, there's a cycle.

```java
public boolean hasCycleBrute(ListNode head) {
    Set<ListNode> seen = new HashSet<>();
    for (ListNode p = head; p != null; p = p.next) {
        if (!seen.add(p)) return true;   // add() returns false if already present
    }
    return false;
}
```
- **Time:** O(n). **Space:** O(n).
- ❌ Why it's not enough: it uses O(n) extra memory; we can do it in **O(1)**.

**Approach 2 — Optimal (Floyd's Tortoise & Hare)**
Two pointers: slow moves 1 step, fast moves 2. If there's a loop, fast laps slow and they **collide**. If fast hits `null`, no cycle.

```java
public boolean hasCycle(ListNode head) {
    ListNode slow = head, fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;        // tortoise: 1 step
        fast = fast.next.next;   // hare: 2 steps
        if (slow == fast) return true;   // they met → cycle
    }
    return false;                // fast escaped to null → no cycle
}
```

**🔬 Dry-run on `3→2→0→-4→(back to 2)`:**
- slow=3, fast=3
- slow=2, fast=0
- slow=0, fast=2 (fast wrapped around)
- slow=-4, fast=-4 → **collision** → `true` ✅

For a list with no cycle, `fast` eventually reaches `null` and we return `false`.

- **Time:** O(n). **Space:** O(1).

🧠 **Algorithm to Remember Forever**
- **Pattern:** *Fast & Slow Pointers (Floyd's cycle detection).*
- **Core idea:** On a circular track, a faster runner inevitably laps a slower one. On a straight track, the fast runner just finishes.
- **Memory hook:** 🐢🐇 *Tortoise and hare on a track — if the track loops, the hare catches the tortoise from behind.*
- **Trigger phrase:** "detect a cycle", "find the middle", "nth from end", "O(1) space" on a linked list.

---

### 7️⃣ Copy List with Random Pointer  ⭐ (Frequent at Microsoft)

📋 **Full Question**
Each node has `val`, `next`, **and** a `random` pointer (to any node or `null`). Return a **deep copy** — new nodes, with `next`/`random` mirroring the original structure.
- Input: nodes `[7,null],[13,0],[11,4],[10,2],[1,0]` (each `[val, randomIndex]`) → Output: an independent identical structure.

🗣️ **What to say first**
- "Can `random` point to `null`, to itself, or to any node — including earlier ones?" (Yes, any.)
- "Must the copy be fully independent (deep) — modifying it won't affect the original?" (Yes.)
- "Are values unique?" (Don't rely on values; map by node identity.)

**Approach 1 — Brute Force (HashMap old→new, two passes)**
Pass 1: create clones, map `original → clone`. Pass 2: wire `next` and `random` using the map.

```java
class Node { int val; Node next; Node random; Node(int v){ val = v; } }

public Node copyRandomListBrute(Node head) {
    Map<Node, Node> map = new HashMap<>();
    for (Node p = head; p != null; p = p.next)        // pass 1: clone each node
        map.put(p, new Node(p.val));
    for (Node p = head; p != null; p = p.next) {       // pass 2: connect pointers via map
        map.get(p).next   = map.get(p.next);           // map.get(null) returns null — fine
        map.get(p).random = map.get(p.random);
    }
    return map.get(head);
}
```
- **Time:** O(n). **Space:** O(n) for the map.
- ✅ This is a perfectly acceptable answer! State it, then offer the O(1)-space upgrade.

**Approach 2 — Optimal (Interweaving, O(1) extra space)**
Weave each clone right after its original (`A→A'→B→B'`), set `random` via `orig.next.random = orig.random.next`, then unweave.

```java
public Node copyRandomList(Node head) {
    if (head == null) return null;
    // 1. interleave clones: A -> A' -> B -> B' -> ...
    for (Node p = head; p != null; p = p.next.next) {
        Node copy = new Node(p.val);
        copy.next = p.next;
        p.next = copy;
    }
    // 2. assign randoms: the clone of p is p.next; its random is p.random's clone
    for (Node p = head; p != null; p = p.next.next) {
        if (p.random != null) p.next.random = p.random.next;
    }
    // 3. detach the cloned list from the original
    Node dummy = new Node(0), copyTail = dummy;
    for (Node p = head; p != null; p = p.next) {
        copyTail.next = p.next;     // pull out the clone
        copyTail = copyTail.next;
        p.next = p.next.next;       // restore original's next
    }
    return dummy.next;
}
```

**🔬 Dry-run idea on `A→B`:** After step 1: `A→A'→B→B'`. If `A.random=B`, then `A'.random = A.random.next = B.next = B'` ✅ — the clone's random correctly points to the clone of B. Step 3 splits the two lists back apart.

- **Time:** O(n). **Space:** O(1) (excluding output).

🧠 **Algorithm to Remember Forever**
- **Pattern:** *Node interleaving / HashMap clone.*
- **Core idea:** The clone of any node X is *physically adjacent* to X (`X.next`), so you can find any clone in O(1) without a map.
- **Memory hook:** 👯 *Give every person a twin standing right behind them. To find anyone's twin, just look one step back.*
- **Trigger phrase:** "deep copy", "clone with random/arbitrary pointer", "duplicate a graph/list with extra links".

---

### 8️⃣ LRU Cache  ⭐⭐⭐ (Top 3 Microsoft — almost guaranteed)

📋 **Full Question**
Design a **Least Recently Used** cache with capacity `C`. `get(key)` and `put(key, value)` must be **O(1)**. When full, evict the **least recently used** item on insert.
- `LRUCache(2); put(1,1); put(2,2); get(1)→1; put(3,3)` (evicts 2); `get(2)→-1`; `put(4,4)` (evicts 1); `get(1)→-1; get(3)→3; get(4)→4`.

🗣️ **What to say first**
- "Both `get` and `put` must be O(1) — so I'll need a HashMap for lookup plus a doubly linked list for ordering."
- "Does reading (`get`) count as 'using' an item, making it most-recently-used?" (Yes.)
- "What does `get` return on a miss?" (-1.)

**Approach 1 — Brute Force (HashMap + timestamps)**
Store a timestamp per key; on eviction, scan for the oldest.

```java
class LRUBrute {
    private Map<Integer,int[]> map = new HashMap<>();   // key -> {value, lastUsedTime}
    private int cap, clock = 0;
    LRUBrute(int capacity) { cap = capacity; }
    public int get(int key) {
        if (!map.containsKey(key)) return -1;
        map.get(key)[1] = clock++;          // touch
        return map.get(key)[0];
    }
    public void put(int key, int value) {
        if (!map.containsKey(key) && map.size() == cap) {
            int oldestKey = -1, oldestTime = Integer.MAX_VALUE;
            for (var e : map.entrySet())     // O(n) scan to find LRU — the slow part
                if (e.getValue()[1] < oldestTime) { oldestTime = e.getValue()[1]; oldestKey = e.getKey(); }
            map.remove(oldestKey);
        }
        map.put(key, new int[]{value, clock++});
    }
}
```
- **Time:** `put` eviction is O(n). **Space:** O(n).
- ❌ Why it's not enough: eviction scans all keys — the spec demands **O(1)**.

**Approach 2 — Optimal (HashMap + Doubly Linked List)**
A HashMap gives O(1) lookup; a doubly linked list maintains usage order. Move-to-front on access; evict from the back. Use **dummy head & tail** sentinels so add/remove never hit `null` checks.

```java
class LRUCache {
    private class DNode {                  // doubly linked list node
        int key, val;
        DNode prev, next;
        DNode(int k, int v) { key = k; val = v; }
    }
    private final Map<Integer, DNode> map = new HashMap<>();
    private final DNode head = new DNode(0, 0);   // most-recently-used side (sentinel)
    private final DNode tail = new DNode(0, 0);   // least-recently-used side (sentinel)
    private final int capacity;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        head.next = tail;                  // wire the two sentinels together
        tail.prev = head;
    }

    private void remove(DNode n) {         // unlink a node — O(1)
        n.prev.next = n.next;
        n.next.prev = n.prev;
    }
    private void addFront(DNode n) {        // insert right after head (most recent) — O(1)
        n.next = head.next;
        n.prev = head;
        head.next.prev = n;
        head.next = n;
    }

    public int get(int key) {
        if (!map.containsKey(key)) return -1;
        DNode n = map.get(key);
        remove(n); addFront(n);            // touching it makes it most-recently-used
        return n.val;
    }

    public void put(int key, int value) {
        if (map.containsKey(key)) {
            DNode n = map.get(key);
            n.val = value;
            remove(n); addFront(n);        // update + promote to front
            return;
        }
        if (map.size() == capacity) {       // full → evict the LRU node (just before tail)
            DNode lru = tail.prev;
            remove(lru);
            map.remove(lru.key);            // why we store key in the node: to clean the map
        }
        DNode fresh = new DNode(key, value);
        map.put(key, fresh);
        addFront(fresh);
    }
}
```

**🔬 Dry-run (capacity 2):**
- `put(1,1)` → list: `1`
- `put(2,2)` → list: `2,1` (front=most recent)
- `get(1)` → returns 1, promote → `1,2`
- `put(3,3)` → full; evict tail.prev = `2`; → `3,1`
- `get(2)` → not in map → `-1` ✅
- `put(4,4)` → full; evict `1`; → `4,3`
- `get(1)` → `-1` ✅, `get(3)` → `3` ✅, `get(4)` → `4` ✅

- **Time:** O(1) for `get` and `put`. **Space:** O(capacity).

💡 *Interview shortcut:* Java's `LinkedHashMap` with `accessOrder=true` gives LRU in ~10 lines (override `removeEldestEntry`). **Mention it**, but implement the HashMap+DLL version — that's what they actually want to see.

🧠 **Algorithm to Remember Forever**
- **Pattern:** *HashMap + Doubly Linked List* (the canonical O(1) cache design).
- **Core idea:** HashMap = instant lookup; DLL = instant reordering. Front = newest, back = oldest. Always evict the back.
- **Memory hook:** 🎟️ *A nightclub with a HashMap bouncer (find anyone instantly) and a velvet-rope line (DLL). Use your card → move to the front. Club's full → kick out the person at the very back.*
- **Trigger phrase:** "design a cache", "O(1) get and put", "least/most recently used", "evict", "fixed capacity".

---

## 📊 Complexity Cheat Sheet

| Problem | Brute Force | Optimal | Technique |
|---------|-------------|---------|-----------|
| Valid Parentheses | O(n²) time | **O(n)** time, O(n) space | Stack (matching) |
| Min Stack | O(n) getMin | **O(1)** all ops | Auxiliary min-stack |
| Daily Temperatures | O(n²) | **O(n)** | Monotonic stack |
| Reverse Linked List | O(n) time, O(n) space | O(n) time, **O(1)** space | 3-pointer flip |
| Merge Two Sorted Lists | O(N log N) | **O(n+m)** time, O(1) space | Two-pointer + dummy |
| Linked List Cycle | O(n) space | O(n) time, **O(1)** space | Fast & slow pointers |
| Copy List w/ Random | O(n) space (map) | O(n) time, **O(1)** space | Node interleaving |
| LRU Cache | O(n) evict | **O(1)** get/put | HashMap + Doubly LL |

---

## 🧠🧠 ALGORITHM RECOGNITION TABLE

> *If the question says X → use Algorithm Y → Memory Hook → Big-O.* Internalize this and you'll **name the pattern before you finish reading the prompt.**

| If the question says… (trigger) | Use this algorithm | Memory hook | Big-O |
|---------------------------------|--------------------|-------------|-------|
| "valid / balanced / matching brackets / nested" | **Stack** | 🥞 Plates — last in, first out | O(n) |
| "O(1) min/max" alongside a stack/queue | **Auxiliary aggregate stack** | 🪜 Sticky note on each plate | O(1) |
| "next greater / warmer / smaller / span" | **Monotonic stack** | 🧗 Taller person resolves the line | O(n) |
| "reverse the list / reverse in place" | **3-pointer flip** | 🚂 Hold the next car before uncoupling | O(n), O(1) |
| "merge two sorted lists" | **Two-pointer merge + dummy** | 🤝 Two queues merging | O(n+m) |
| "detect cycle / find middle / nth from end" | **Fast & slow pointers** | 🐢🐇 Hare laps the tortoise | O(n), O(1) |
| "deep copy / clone with random pointer" | **Node interleaving / HashMap clone** | 👯 Twin standing right behind | O(n) |
| "design a cache / O(1) get+put / LRU / evict" | **HashMap + Doubly Linked List** | 🎟️ Bouncer + velvet rope | O(1) |
| "queue using stacks / stack using queues" | **Two-container simulation** | 🔄 Pour between two cups | Amortized O(1) |
| "circular buffer / fixed-size queue" | **Array + head/tail indices (mod)** | 🎡 Ferris wheel of slots | O(1) |

---

## ☕ Java Tips for Stacks, Queues & Linked Lists

**1. Use `ArrayDeque`, not the legacy `Stack` class.**
```java
Deque<Integer> stack = new ArrayDeque<>();   // ✅ fast, modern
stack.push(x); stack.pop(); stack.peek(); stack.isEmpty();
// Avoid:  Stack<Integer> s = new Stack<>();   // ❌ synchronized & slow (legacy)
```

**2. `ArrayDeque` is also your queue.**
```java
Deque<Integer> queue = new ArrayDeque<>();
queue.offer(x);   // enqueue (add to back)
queue.poll();     // dequeue (remove from front)
queue.peek();     // front without removing
```
⚠️ `ArrayDeque` **cannot hold `null`**. If you need nulls, use `LinkedList`.

**3. Always reach for the `dummy head` on list-building problems.**
```java
ListNode dummy = new ListNode(0), tail = dummy;
// ... tail.next = something; tail = tail.next; ...
return dummy.next;   // skips the placeholder, dodges all empty-list edge cases
```
It eliminates the "is this the first node?" special case — a huge bug-reducer.

**4. Fast/slow pointer template (cycle, middle, palindrome).**
```java
ListNode slow = head, fast = head;
while (fast != null && fast.next != null) {  // guard BOTH or you'll NPE
    slow = slow.next;
    fast = fast.next.next;
}
// when loop ends, slow is at the middle (for even length, the 2nd middle)
```

**5. The three-pointer reversal is muscle memory — save BEFORE you flip.**
```java
ListNode next = curr.next;  // save first…
curr.next = prev;           // …then flip. Reverse the order = lose the list.
```

**6. `getOrDefault` keeps HashMap code clean.**
```java
map.put(key, map.getOrDefault(key, 0) + 1);
```

**7. `LinkedHashMap` is a built-in LRU shortcut (mention, don't rely on it).**
```java
new LinkedHashMap<Integer,Integer>(cap, 0.75f, true) {  // true = access-order
    protected boolean removeEldestEntry(Map.Entry<Integer,Integer> e) {
        return size() > cap;   // auto-evicts the LRU entry
    }
};
```

**8. Null-safety on linked lists:** before `p.next.next`, confirm `p.next != null`. The #1 cause of `NullPointerException` in these problems.

---

## ✅ Final Interview Checklist

Before you say "I'm done," tick every box:

- [ ] 🗣️ **Did I clarify** before coding? (null/empty, duplicates, in-place, sorted, size)
- [ ] 🪜 **Did I state the brute force first**, then improve to optimal? (Don't skip straight to the answer.)
- [ ] ✏️ **Did I dry-run a tiny example** on the whiteboard before coding?
- [ ] 🧱 **Did I use a dummy head** for any list-building problem?
- [ ] 🐢🐇 **Did I guard `fast != null && fast.next != null`** in fast/slow loops?
- [ ] 🚂 **Did I save `next` before flipping** in the reversal?
- [ ] ⏱️ **Did I state BOTH time and space complexity** — and justify them?
- [ ] 🧪 **Did I test edge cases?** empty list, single node, capacity 1, all duplicates, no cycle.
- [ ] 📛 **Did I name the pattern out loud?** ("This is a classic monotonic-stack / fast-slow-pointer problem.") Naming patterns signals seniority.
- [ ] 🧹 **Is my code clean?** meaningful names, no dead code, handled the empty case.
- [ ] 💬 **Did I narrate while coding**, not in silence? Microsoft grades communication heavily.

> **Golden mantra:** *Clarify → Brute Force → Optimal → Best → Complexity.* Say it before every answer. 🎤

You've got this. Reverse that list in 5 minutes, design that LRU cache from memory, and **name your pattern with confidence.** 🚀
