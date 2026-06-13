# Week 4: Stack, Queue & Linked List — How to ANSWER in a Microsoft Interview 🎤 (Java)

Welcome! This file is your **interview script** for Week 4. The goal isn't just to *solve* these problems — it's to *talk through them* like a Microsoft engineer would. Interviewers grade your **communication, structure, and trade-off thinking** as much as your code.

This file covers **all 15 problems** in the week.

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

The shared building block you'll reuse all week:

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

**Design choice (no real "brute force"):**
One normal stack scans all elements for `getMin` → O(n). The problem demands **O(1)**, so we carry the running minimum. Choosing **two stacks** (the data stack + a parallel min stack) lets every operation stay O(1) and handles duplicates cleanly.

**Approach 1 — Naive (scan for min)**
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

### 4️⃣ Evaluate Reverse Polish Notation  ⭐ (Stack Evaluation)

📋 **Full Question**
Evaluate the value of an arithmetic expression in **Reverse Polish Notation (RPN / postfix)**. Valid operators are `+`, `-`, `*`, `/`. Each operand may be an integer or another expression. Integer division truncates toward zero.
- Input: `["2","1","+","3","*"]` → `((2+1)*3)` = `9`
- Input: `["4","13","5","/","+"]` → `(4 + (13/5))` = `6`

🗣️ **What to say first**
- "Is the input always a **valid** RPN expression?" (Assume yes.)
- "How does division behave — truncate toward zero?" (Yes, per spec.)
- "Can numbers be negative or multi-digit?" (Yes — parse the whole token, don't read char-by-char.)

**Approach 1 — Naive (recursive/manual parse)**
You *could* repeatedly find the first operator and collapse it with the two tokens before it into a single value, rebuilding the list each time. That's O(n²) and fiddly.

```java
// Conceptual naive version: repeatedly find first operator, fold preceding two operands.
// Each fold rebuilds the token list → O(n) per fold, O(n) folds → O(n²).
```
- **Time:** O(n²). **Space:** O(n).
- ❌ Why it's not enough: rebuilding the list each fold is wasteful — a stack folds in a single pass.

**Approach 2 — Optimal (Stack)**
Push numbers. On an operator, pop the **top two** operands (order matters: the second pop is the left operand), apply, push the result. The final stack value is the answer.

```java
public int evalRPN(String[] tokens) {
    Deque<Integer> stack = new ArrayDeque<>();
    for (String tok : tokens) {
        switch (tok) {
            case "+": case "-": case "*": case "/":
                int b = stack.pop();           // right operand (popped first)
                int a = stack.pop();           // left operand
                stack.push(apply(a, b, tok));
                break;
            default:
                stack.push(Integer.parseInt(tok));  // handles negatives & multi-digit
        }
    }
    return stack.pop();
}

private int apply(int a, int b, String op) {
    switch (op) {
        case "+": return a + b;
        case "-": return a - b;
        case "*": return a * b;
        default:  return a / b;   // Java int division already truncates toward zero
    }
}
```

**🔬 Dry-run on `["2","1","+","3","*"]`:**
- `2` → stack `[2]`
- `1` → stack `[2,1]`
- `+` → pop 1, pop 2 → 2+1=3 → `[3]`
- `3` → `[3,3]`
- `*` → pop 3, pop 3 → 3*3=9 → `[9]`
- Answer: **9** ✅

- **Time:** O(n). **Space:** O(n).

🧠 **Algorithm to Remember Forever**
- **Pattern:** *Stack-based expression evaluation.*
- **Core idea:** Postfix is *made* for a stack — operands pile up, an operator consumes the two most recent.
- **Memory hook:** 🧮 *An abacus where each operator grabs the last two beads, smushes them, and drops the result back on.*
- **Trigger phrase:** "postfix / prefix / RPN", "evaluate expression", "calculator".

---

### 5️⃣ Next Greater Element I  ⭐ (Monotonic Stack + HashMap)

📋 **Full Question**
`nums1` is a **subset** of `nums2`. For each `x` in `nums1`, find the **next greater element** to its right *in `nums2`*. If none, answer `-1`.
- Input: `nums1 = [4,1,2]`, `nums2 = [1,3,4,2]` → Output: `[-1,3,-1]`
  - `4`: nothing greater to its right → `-1`
  - `1`: next greater is `3` → `3`
  - `2`: nothing greater to its right → `-1`

🗣️ **What to say first**
- "Are all elements **unique**?" (Yes per spec — lets us key a HashMap by value.)
- "Is `nums1` always a subset of `nums2`?" (Yes.)
- "What do I return if there's no greater element?" (-1.)

**Approach 1 — Brute Force (nested scan)**
For each value in `nums1`, find it in `nums2`, then scan rightward for the first greater value.

```java
public int[] nextGreaterElementBrute(int[] nums1, int[] nums2) {
    int[] ans = new int[nums1.length];
    for (int i = 0; i < nums1.length; i++) {
        ans[i] = -1;
        int j = 0;
        while (nums2[j] != nums1[i]) j++;        // locate the element
        for (int k = j + 1; k < nums2.length; k++) {
            if (nums2[k] > nums1[i]) { ans[i] = nums2[k]; break; }
        }
    }
    return ans;
}
```
- **Time:** O(n1 × n2). **Space:** O(1) extra.
- ❌ Why it's not enough: we re-scan `nums2` for every query — a monotonic stack precomputes all answers in one pass.

**Approach 2 — Optimal (Monotonic Stack + HashMap)**
Precompute, for every value in `nums2`, its next greater element using a **decreasing monotonic stack**, storing results in a HashMap. Then answer each `nums1` query in O(1).

```java
public int[] nextGreaterElement(int[] nums1, int[] nums2) {
    Map<Integer, Integer> nextGreater = new HashMap<>();  // value -> its next greater
    Deque<Integer> stack = new ArrayDeque<>();            // values, decreasing top→down
    for (int n : nums2) {
        // n resolves every smaller value still waiting on the stack
        while (!stack.isEmpty() && n > stack.peek()) {
            nextGreater.put(stack.pop(), n);
        }
        stack.push(n);
    }
    // anything left on the stack has no greater element → defaults to -1 below
    int[] ans = new int[nums1.length];
    for (int i = 0; i < nums1.length; i++) {
        ans[i] = nextGreater.getOrDefault(nums1[i], -1);
    }
    return ans;
}
```

**🔬 Dry-run on `nums2 = [1,3,4,2]`:**
- `1` → stack `[1]`
- `3` → 3>1 pop1, map{1→3} → push → `[3]`
- `4` → 4>3 pop3, map{1→3,3→4} → push → `[4]`
- `2` → 2<4 → push → `[4,2]`. Leftovers 4,2 have no greater → absent → -1.
- Query `[4,1,2]` → `[-1, 3, -1]` ✅

- **Time:** O(n1 + n2). **Space:** O(n2).

🧠 **Algorithm to Remember Forever**
- **Pattern:** *Monotonic stack* to precompute "next greater", plus a HashMap to answer queries.
- **Core idea:** Same as Daily Temperatures — but store the *value* (not the distance), and route answers through a map because the query array is a subset.
- **Memory hook:** 🧗 + 📒 *Tall person resolves the line; jot each answer in a notebook so later lookups are instant.*
- **Trigger phrase:** "next greater element", "subset query", "to its right".

---

### 6️⃣ Largest Rectangle in Histogram  ⭐ (Hard — Monotonic Stack)

📋 **Full Question**
Given `heights` representing a histogram's bar heights (each bar width 1), return the **area of the largest rectangle** that fits inside.
- Input: `[2,1,5,6,2,3]` → Output: `10` (bars of height 5 and 6 → width 2 × height 5 = 10)

🗣️ **What to say first**
- "Can heights be 0? Can the array be empty?" (Heights ≥ 0; handle empty → 0.)
- "Are heights non-negative integers?" (Yes.)
- "The rectangle must be contiguous and bounded by the *minimum* bar in its span — confirm?"

**Approach 1 — Brute Force (expand around each bar)**
For each bar `i`, treat its height as the limiting height and expand left/right while bars are ≥ `heights[i]`.

```java
public int largestRectangleBrute(int[] heights) {
    int n = heights.length, best = 0;
    for (int i = 0; i < n; i++) {
        int left = i, right = i;
        while (left  > 0      && heights[left  - 1] >= heights[i]) left--;
        while (right < n - 1  && heights[right + 1] >= heights[i]) right++;
        best = Math.max(best, heights[i] * (right - left + 1));
    }
    return best;
}
```
- **Time:** O(n²). **Space:** O(1).
- ❌ Why it's not enough: each bar may re-scan the whole array — too slow for large inputs.

**Approach 2 — Optimal (Monotonic Increasing Stack of indices)**
Keep a stack of indices with **increasing** heights. When a shorter bar arrives, pop taller bars and compute the rectangle each can form: its height × the width between the new boundary and the element now below it on the stack. A sentinel `0` at the end flushes everything.

```java
public int largestRectangleArea(int[] heights) {
    Deque<Integer> stack = new ArrayDeque<>();   // indices, heights increasing top→down
    int best = 0, n = heights.length;
    for (int i = 0; i <= n; i++) {
        int curr = (i == n) ? 0 : heights[i];    // sentinel 0 flushes the stack at the end
        while (!stack.isEmpty() && heights[stack.peek()] >= curr) {
            int height = heights[stack.pop()];   // the bar we're closing off
            // width spans from the bar after the new top to i-1
            int width = stack.isEmpty() ? i : i - stack.peek() - 1;
            best = Math.max(best, height * width);
        }
        stack.push(i);
    }
    return best;
}
```

**🔬 Dry-run on `[2,1,5,6,2,3]`:**
- i=0 (2): push → `[0]`
- i=1 (1<2): pop 0 (h=2), width = i=1 → area 2; push → `[1]`
- i=2 (5): push → `[1,2]`
- i=3 (6): push → `[1,2,3]`
- i=4 (2<6): pop 3 (h=6), width = 4-2-1=1 → area 6; 2<5 pop 2 (h=5), width = 4-1-1=2 → **area 10**; push → `[1,4]`
- i=5 (3): push → `[1,4,5]`
- i=6 (sentinel 0): pop 5 (h=3) width 6-4-1=1 → 3; pop 4 (h=2) width 6-1-1=4 → 8; pop 1 (h=1) width 6 → 6.
- Best = **10** ✅

- **Time:** O(n) — each index pushed/popped once. **Space:** O(n).

🧠 **Algorithm to Remember Forever**
- **Pattern:** *Monotonic increasing stack* — find, for each bar, how far left and right it can extend.
- **Core idea:** A bar's rectangle is bounded by the first **shorter** bar on each side. The stack reveals those boundaries as you pop.
- **Memory hook:** 🏙️ *Skyline — each building can only stretch sideways until a shorter building blocks it.*
- **Trigger phrase:** "largest rectangle", "max area histogram", "maximal rectangle" (2D version builds on this).

---

### 7️⃣ Implement Queue using Stacks  ⭐

📋 **Full Question**
Implement a **FIFO queue** (`push`, `pop`, `peek`, `empty`) using only **two stacks**.
- `push(1); push(2); peek()` → `1`; `pop()` → `1`; `empty()` → `false`.

🗣️ **What to say first**
- "I can only use standard stack operations (push/pop/peek/isEmpty) — confirm?"
- "Should operations be amortized O(1)? Or strict O(1)?" (Amortized O(1) is the expected answer.)
- "Are calls always valid (no pop on empty)?" (Assume yes.)

**Design choice (no real "brute force"):**
A single stack is LIFO; a queue needs FIFO. The fix is **two stacks** — an `in` stack receives pushes, an `out` stack serves removals. Reversing elements once (in → out) flips LIFO into FIFO. We **only transfer when `out` is empty**, which gives amortized O(1) because each element moves between stacks at most once.

**Approach — Two Stacks (lazy transfer)**

```java
class MyQueue {
    private Deque<Integer> in  = new ArrayDeque<>();   // newest pushes land here
    private Deque<Integer> out = new ArrayDeque<>();   // serves the front of the queue

    public void push(int x) { in.push(x); }

    public int pop() {
        peek();                 // ensure out has the front element
        return out.pop();
    }

    public int peek() {
        if (out.isEmpty()) {                  // only refill when out is drained
            while (!in.isEmpty()) out.push(in.pop());   // reverses order → FIFO
        }
        return out.peek();
    }

    public boolean empty() { return in.isEmpty() && out.isEmpty(); }
}
```

**🔬 Dry-run:** `push(1)`, `push(2)` → in=`[2,1]`(top→bottom), out=`[]`. `peek()` drains in→out → out=`[1,2]`, returns **1**. `pop()` → out=`[2]`, returns **1**. `push(3)` → in=`[3]`. `pop()` → out non-empty, returns **2**. FIFO order 1,2,3 ✅

- **Time:** push O(1); pop/peek **amortized O(1)** (each element transferred once). **Space:** O(n).

🧠 **Algorithm to Remember Forever**
- **Pattern:** *Two-stack simulation (lazy transfer).*
- **Core idea:** Reversing a stack once flips LIFO into FIFO. Defer the transfer until `out` empties so each element moves at most once.
- **Memory hook:** 🔄 *Pour water between two cups — the bottom of the first becomes the top of the second.*
- **Trigger phrase:** "queue using stacks", "FIFO from LIFO".

---

### 8️⃣ Implement Stack using Queues  ⭐

📋 **Full Question**
Implement a **LIFO stack** (`push`, `pop`, `top`, `empty`) using only **queues** (FIFO).
- `push(1); push(2); top()` → `2`; `pop()` → `2`; `empty()` → `false`.

🗣️ **What to say first**
- "Can I use one queue or two?" (One queue suffices with a rotate trick.)
- "Which operation should be the costly one — push or pop?" (Here we make **push** O(n) so pop/top are O(1).)
- "Standard queue ops only (offer/poll/peek)?" (Yes.)

**Design choice (no real "brute force"):**
A queue is FIFO; a stack needs the **last** pushed element out first. The trick: after enqueuing a new element, **rotate** the queue so the newest element sits at the *front*. Then `pop`/`top` are simple O(1) front operations. One queue is enough.

**Approach — Single Queue (rotate on push)**

```java
class MyStack {
    private Deque<Integer> q = new ArrayDeque<>();   // used strictly as a FIFO queue

    public void push(int x) {
        q.offer(x);                       // add to back
        // rotate so the newly added element moves to the front
        for (int i = 1; i < q.size(); i++) q.offer(q.poll());
    }

    public int pop()  { return q.poll(); }   // front is the most recently pushed
    public int top()  { return q.peek(); }
    public boolean empty() { return q.isEmpty(); }
}
```

**🔬 Dry-run:** `push(1)` → q=`[1]`. `push(2)`: offer 2 → `[1,2]`, rotate once (move 1 to back) → `[2,1]`. `top()` = front = **2** ✅. `pop()` → **2**, q=`[1]`. `top()` = **1**. LIFO order ✅

- **Time:** push O(n); pop/top/empty O(1). **Space:** O(n).

🧠 **Algorithm to Remember Forever**
- **Pattern:** *Single-queue rotation.*
- **Core idea:** Keep the queue's front equal to the stack's top by rotating after every push.
- **Memory hook:** 🎠 *A carousel — push a new horse on, then spin it around to the front.*
- **Trigger phrase:** "stack using queues", "LIFO from FIFO".

---

### 9️⃣ Design Circular Queue  ⭐

📋 **Full Question**
Design a fixed-capacity **circular queue** (ring buffer) supporting `enQueue`, `deQueue`, `Front`, `Rear`, `isEmpty`, `isFull`. The last position connects back to the first to reuse space.
- `MyCircularQueue(3); enQueue(1)→true; enQueue(2)→true; enQueue(3)→true; enQueue(4)→false (full); Rear()→3; isFull()→true; deQueue()→true; enQueue(4)→true; Rear()→4.`

🗣️ **What to say first**
- "Is the capacity fixed at construction?" (Yes.)
- "Should operations return a boolean for success/failure?" (Yes per spec.)
- "Can I use an array with head/tail indices and modular arithmetic?" (That's the canonical design.)

**Design choice (no real "brute force"):**
A plain array shift is O(n) per dequeue. Instead use a **fixed array + `head` index + `size` counter**. Modular arithmetic (`% capacity`) wraps indices around, so enqueue/dequeue are O(1) with no shifting. Tracking `size` avoids the classic "full vs empty look identical" ambiguity.

**Approach — Array + head + size**

```java
class MyCircularQueue {
    private final int[] data;
    private int head;       // index of the front element
    private int size;       // number of elements currently stored
    private final int capacity;

    public MyCircularQueue(int k) {
        capacity = k;
        data = new int[k];
        head = 0;
        size = 0;
    }

    public boolean enQueue(int value) {
        if (isFull()) return false;
        int tail = (head + size) % capacity;   // next free slot, wraps around
        data[tail] = value;
        size++;
        return true;
    }

    public boolean deQueue() {
        if (isEmpty()) return false;
        head = (head + 1) % capacity;           // advance front, wraps around
        size--;
        return true;
    }

    public int Front() { return isEmpty() ? -1 : data[head]; }
    public int Rear()  { return isEmpty() ? -1 : data[(head + size - 1) % capacity]; }
    public boolean isEmpty() { return size == 0; }
    public boolean isFull()  { return size == capacity; }
}
```

**🔬 Dry-run (k=3):** enQueue 1,2,3 → data=[1,2,3], head=0, size=3. enQueue 4 → full → false. Rear = data[(0+3-1)%3]=data[2]=**3**. deQueue → head=1, size=2. enQueue 4 → tail=(1+2)%3=0 → data=[4,2,3], size=3. Rear = data[(1+3-1)%3]=data[0]=**4** ✅

- **Time:** O(1) for all operations. **Space:** O(k).

🧠 **Algorithm to Remember Forever**
- **Pattern:** *Ring buffer — array + head index + size, modular arithmetic.*
- **Core idea:** Don't shift elements; move *indices* and wrap with `% capacity`. Track `size` to disambiguate full vs empty.
- **Memory hook:** 🎡 *A Ferris wheel of fixed seats — riders board and exit at moving positions, the wheel loops forever.*
- **Trigger phrase:** "circular queue", "ring buffer", "fixed-size queue", "reuse space".

---

### 🔟 Reverse Linked List  ⭐⭐⭐ (Top 5 Microsoft — must do in 5 min)

📋 **Full Question**
Reverse a singly linked list and return the new head.
- Input: `1 → 2 → 3 → 4 → 5` → Output: `5 → 4 → 3 → 2 → 1`

🗣️ **What to say first**
- "Should I reverse **in place** (O(1) space) or can I build a new list?"
- "Can the list be **empty** or have a **single** node?" (Both should just work.)
- "Do you want the iterative or recursive solution? I'll do iterative — it's O(1) space."

**Approach 1 — Brute Force (extra storage)**
Push all values to a stack, then rebuild. Works but uses O(n) extra space.

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

### 1️⃣1️⃣ Merge Two Sorted Lists  ⭐ (Very Frequent)

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

### 1️⃣2️⃣ Linked List Cycle  ⭐ (Floyd's Tortoise & Hare)

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

### 1️⃣3️⃣ Add Two Numbers  ⭐ (Classic)

📋 **Full Question**
Two numbers are represented by linked lists, each node a single digit, stored in **reverse order** (ones digit first). Add them and return the sum as a linked list (also reverse order).
- Input: `l1 = 2→4→3` (=342), `l2 = 5→6→4` (=465) → Output: `7→0→8` (=807)

🗣️ **What to say first**
- "Digits are stored **least-significant first** — confirm?" (Yes — this is why addition is easy: we add front-to-back.)
- "Can the lists have **different lengths**?" (Yes — treat the missing digit as 0.)
- "Should I handle a final **carry** that creates a new leading digit?" (Yes — e.g., 5+5=10 → `0→1`.)

**Approach 1 — Naive (convert to integers)**
Read each list into a number, add, rebuild a list. Fails for large inputs (overflow) and ignores the elegance of digit-by-digit addition.

```java
// Conceptual: build long from l1, build long from l2, sum, then split digits back into a list.
// Breaks when the number exceeds long's range (lists can be very long).
```
- **Time:** O(n+m). **Space:** O(n+m).
- ❌ Why it's not enough: integer/long **overflows** for long lists — we must add digit by digit.

**Approach 2 — Optimal (Digit-by-digit with carry + dummy head)**
Walk both lists together, summing digit + digit + carry. Use a **dummy head** to build the result; remember a leftover carry at the end.

```java
public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
    ListNode dummy = new ListNode(0), tail = dummy;
    int carry = 0;
    while (l1 != null || l2 != null || carry != 0) {   // continue while digits or carry remain
        int sum = carry;
        if (l1 != null) { sum += l1.val; l1 = l1.next; }
        if (l2 != null) { sum += l2.val; l2 = l2.next; }
        carry = sum / 10;                  // carry to the next digit
        tail.next = new ListNode(sum % 10);// store this digit
        tail = tail.next;
    }
    return dummy.next;
}
```

**🔬 Dry-run on `2→4→3` + `5→6→4`:**
- 2+5+0 = 7, carry 0 → `7`
- 4+6+0 = 10, digit 0, carry 1 → `7→0`
- 3+4+1 = 8, carry 0 → `7→0→8`
- no more digits, carry 0 → stop. Result `7→0→8` (=807) ✅

Edge: `5` + `5` → 5+5=10 → digit 0 carry 1 → loop again (carry≠0) → digit 1 → `0→1` (=10) ✅

- **Time:** O(max(n,m)). **Space:** O(max(n,m)) for the output.

🧠 **Algorithm to Remember Forever**
- **Pattern:** *Elementary addition with carry* + *dummy head*.
- **Core idea:** Reverse-order storage means we add exactly like grade-school arithmetic: lowest digit first, carry rolls forward. The loop condition `l1 || l2 || carry` handles unequal lengths and the final carry in one shot.
- **Memory hook:** ➕ *Grade-school column addition — write the digit, carry the one.*
- **Trigger phrase:** "add two numbers as lists", "digits in a list", "carry".

---

### 1️⃣4️⃣ Copy List with Random Pointer  ⭐ (Frequent at Microsoft)

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

### 1️⃣5️⃣ LRU Cache  ⭐⭐⭐ (Top 3 Microsoft — almost guaranteed)

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

| # | Problem | Brute Force | Optimal | Technique |
|---|---------|-------------|---------|-----------|
| 1 | Valid Parentheses | O(n²) time | **O(n)** time, O(n) space | Stack (matching) |
| 2 | Min Stack | O(n) getMin | **O(1)** all ops | Auxiliary min-stack |
| 3 | Daily Temperatures | O(n²) | **O(n)** | Monotonic stack |
| 4 | Evaluate RPN | O(n²) (naive folds) | **O(n)** time, O(n) space | Stack evaluation |
| 5 | Next Greater Element I | O(n1 × n2) | **O(n1 + n2)** | Monotonic stack + HashMap |
| 6 | Largest Rectangle in Histogram | O(n²) | **O(n)** time, O(n) space | Monotonic increasing stack |
| 7 | Implement Queue using Stacks | — | **amortized O(1)** ops | Two-stack simulation |
| 8 | Implement Stack using Queues | — | push O(n), pop/top **O(1)** | Single-queue rotation |
| 9 | Design Circular Queue | O(n) shift (array) | **O(1)** all ops | Ring buffer (array + indices) |
| 10 | Reverse Linked List | O(n) time, O(n) space | O(n) time, **O(1)** space | 3-pointer flip |
| 11 | Merge Two Sorted Lists | O(N log N) | **O(n+m)** time, O(1) space | Two-pointer + dummy |
| 12 | Linked List Cycle | O(n) space | O(n) time, **O(1)** space | Fast & slow pointers |
| 13 | Add Two Numbers | O(n+m) (overflow risk) | **O(max(n,m))** | Digit add + carry + dummy |
| 14 | Copy List w/ Random | O(n) space (map) | O(n) time, **O(1)** space | Node interleaving |
| 15 | LRU Cache | O(n) evict | **O(1)** get/put | HashMap + Doubly LL |

---

## 🧠🧠 ALGORITHM RECOGNITION TABLE

> *If the question says X → use Algorithm Y → Memory Hook → Big-O.* Internalize this and you'll **name the pattern before you finish reading the prompt.**

| If the question says… (trigger) | Use this algorithm | Memory hook | Big-O |
|---------------------------------|--------------------|-------------|-------|
| "valid / balanced / matching brackets / nested" | **Stack** | 🥞 Plates — last in, first out | O(n) |
| "O(1) min/max" alongside a stack/queue | **Auxiliary aggregate stack** | 🪜 Sticky note on each plate | O(1) |
| "next greater / warmer / smaller / span" | **Monotonic stack** | 🧗 Taller person resolves the line | O(n) |
| "evaluate postfix / prefix / RPN / calculator" | **Stack evaluation** | 🧮 Abacus smushes last two beads | O(n) |
| "next greater element of a subset" | **Monotonic stack + HashMap** | 🧗📒 Resolve line, jot answers | O(n) |
| "largest rectangle / max area histogram" | **Monotonic increasing stack** | 🏙️ Skyline blocked by shorter building | O(n) |
| "queue using stacks" | **Two-stack lazy transfer** | 🔄 Pour between two cups | Amortized O(1) |
| "stack using queues" | **Single-queue rotation** | 🎠 Carousel spin to front | O(n) push |
| "circular buffer / fixed-size queue" | **Array + head/size indices (mod)** | 🎡 Ferris wheel of slots | O(1) |
| "reverse the list / reverse in place" | **3-pointer flip** | 🚂 Hold the next car before uncoupling | O(n), O(1) |
| "merge two sorted lists" | **Two-pointer merge + dummy** | 🤝 Two queues merging | O(n+m) |
| "detect cycle / find middle / nth from end" | **Fast & slow pointers** | 🐢🐇 Hare laps the tortoise | O(n), O(1) |
| "add numbers stored as lists / carry" | **Digit add + carry + dummy** | ➕ Grade-school column addition | O(max(n,m)) |
| "deep copy / clone with random pointer" | **Node interleaving / HashMap clone** | 👯 Twin standing right behind | O(n) |
| "design a cache / O(1) get+put / LRU / evict" | **HashMap + Doubly Linked List** | 🎟️ Bouncer + velvet rope | O(1) |

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

**3. Standard `ListNode` definition (have it memorized).**
```java
class ListNode {
    int val;
    ListNode next;
    ListNode() {}
    ListNode(int val) { this.val = val; }
    ListNode(int val, ListNode next) { this.val = val; this.next = next; }
}
```

**4. Always reach for the `dummy head` on list-building problems.**
```java
ListNode dummy = new ListNode(0), tail = dummy;
// ... tail.next = something; tail = tail.next; ...
return dummy.next;   // skips the placeholder, dodges all empty-list edge cases
```
Used in Merge Two Sorted Lists, Add Two Numbers, Reverse (brute), Copy List — it eliminates the "is this the first node?" special case.

**5. Fast/slow pointer template (cycle, middle, palindrome).**
```java
ListNode slow = head, fast = head;
while (fast != null && fast.next != null) {  // guard BOTH or you'll NPE
    slow = slow.next;
    fast = fast.next.next;
}
// when loop ends, slow is at the middle (for even length, the 2nd middle)
```

**6. The three-pointer reversal is muscle memory — save BEFORE you flip.**
```java
ListNode next = curr.next;  // save first…
curr.next = prev;           // …then flip. Reverse the order = lose the list.
```

**7. `getOrDefault` keeps HashMap code clean (Next Greater Element, LRU brute).**
```java
map.put(key, map.getOrDefault(key, 0) + 1);
```

**8. `LinkedHashMap` is a built-in LRU shortcut (mention, don't rely on it).**
```java
new LinkedHashMap<Integer,Integer>(cap, 0.75f, true) {  // true = access-order
    protected boolean removeEldestEntry(Map.Entry<Integer,Integer> e) {
        return size() > cap;   // auto-evicts the LRU entry
    }
};
```

**9. Modular arithmetic for ring buffers (Circular Queue).**
```java
int tail = (head + size) % capacity;   // next free slot wraps around
head = (head + 1) % capacity;          // advance front, wraps around
```

**10. Null-safety on linked lists:** before `p.next.next`, confirm `p.next != null`. The #1 cause of `NullPointerException` in these problems.

---

## ✅ Final Interview Checklist

Before you say "I'm done," tick every box:

- [ ] 🗣️ **Did I clarify** before coding? (null/empty, duplicates, in-place, sorted, size, capacity)
- [ ] 🪜 **Did I state the brute force first**, then improve to optimal? (For design problems, explain the data-structure choice instead.)
- [ ] ✏️ **Did I dry-run a tiny example** on the whiteboard before coding?
- [ ] 🧱 **Did I use a dummy head** for any list-building problem? (Merge, Add Two Numbers, Copy)
- [ ] 🐢🐇 **Did I guard `fast != null && fast.next != null`** in fast/slow loops?
- [ ] 🚂 **Did I save `next` before flipping** in the reversal?
- [ ] ➕ **Did I keep looping while `carry != 0`** in Add Two Numbers (final carry digit)?
- [ ] 🧗 **Did I name the monotonic stack** for next-greater / histogram problems?
- [ ] 🎡 **Did I use `% capacity` and track `size`** in the circular queue (full vs empty)?
- [ ] ⏱️ **Did I state BOTH time and space complexity** — and justify them? (Note *amortized* O(1) for two-stack queue.)
- [ ] 🧪 **Did I test edge cases?** empty list, single node, capacity 1, all duplicates, no cycle, unequal lengths, final carry.
- [ ] 📛 **Did I name the pattern out loud?** ("This is a classic monotonic-stack / fast-slow-pointer problem.") Naming patterns signals seniority.
- [ ] 🧹 **Is my code clean?** meaningful names, no dead code, handled the empty case.
- [ ] 💬 **Did I narrate while coding**, not in silence? Microsoft grades communication heavily.

> **Golden mantra:** *Clarify → Brute Force → Optimal → Best → Complexity.* Say it before every answer. 🎤

You've got this. Reverse that list in 5 minutes, design that LRU cache from memory, and **name your pattern with confidence.** 🚀
