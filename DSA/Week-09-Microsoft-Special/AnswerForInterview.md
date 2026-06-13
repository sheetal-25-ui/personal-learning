# Week 9: Microsoft Most-Asked Questions (Top 20) — How to ANSWER in a Microsoft Interview 🎤 (Java)

Welcome to your **Microsoft interview battle plan**! 🛡️ This file doesn't just give you code — it teaches you *how to talk through* the **20 most-loved Microsoft problems** the way an interviewer wants to hear it. Think of yourself as a tour guide walking the interviewer through your brain. 🧠✨

---

## 🏆 The Golden Rule of Every Interview Answer

No matter which problem they throw at you, **follow this exact order every single time**:

> **Clarify → Brute Force → Optimal → Best → Complexity**

1. **Clarify** 🗣️ — Ask 2-3 sharp questions. Never assume.
2. **Brute Force** 🐢 — State the obvious solution. "The naive way would be…" This shows you can always produce *something*.
3. **Optimal** 🚀 — "But we can do better by noticing…" This is where you win.
4. **Best** 💎 — Mention any further micro-optimizations (space, single pass, in-place).
5. **Complexity** 📊 — Always end with "Time is O(…), Space is O(…)."

### 🎬 Interview Script (what to literally say out loud)

> "Great, let me make sure I understand the problem first. *(ask clarifying questions)*
> A brute-force approach would be to *(describe)* — that's O(…) which is too slow.
> I think we can optimize using *(pattern)*. The key insight is *(one sentence)*.
> Let me code that up… *(narrate as you type)*.
> Let me dry-run with the example to check… *(walk through)*.
> Final complexity: Time O(…), Space O(…). I'd also add tests for empty input and edge cases."

### ❓ Common Clarifying Questions (memorize these — they work on almost any problem)

- "Can the input be empty or null?"
- "Can values be negative / can there be duplicates?"
- "Is the array sorted? Are inputs guaranteed valid?"
- "What's the expected size of the input? (hints at required complexity)"
- "Should I optimize for time or space?"
- "Is there a single answer guaranteed, or could there be none/multiple?"

---

## 🧱 Standard Class Definitions (reuse these everywhere)

```java
// Singly linked list node
class ListNode {
    int val;
    ListNode next;
    ListNode() {}
    ListNode(int val) { this.val = val; }
    ListNode(int val, ListNode next) { this.val = val; this.next = next; }
}

// Binary tree node
class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode() {}
    TreeNode(int val) { this.val = val; }
    TreeNode(int val, TreeNode left, TreeNode right) {
        this.val = val; this.left = left; this.right = right;
    }
}
```

---

# The 20 Must-Know Problems 🎯

---

## 1️⃣ Two Sum

### 📋 Full Question
Given an array `nums` and a `target`, return **indices** of the two numbers that add up to target.
**Example:** `nums = [2, 7, 11, 15]`, `target = 9` → `[0, 1]` (because 2 + 7 = 9).

### 🗣️ What to say first
- "Is there exactly one solution?" (Yes, classically.)
- "Can I use the same element twice?" (No.)
- "Return indices or values?" (Indices.)

### Approach 1 — Brute Force
Check every pair.
```java
public int[] twoSumBrute(int[] nums, int target) {
    for (int i = 0; i < nums.length; i++)
        for (int j = i + 1; j < nums.length; j++)
            if (nums[i] + nums[j] == target)
                return new int[]{i, j};
    return new int[]{};
}
```
**Time:** O(n²) • **Space:** O(1). ❌ Too slow for large arrays.

### Approach 2 — Optimal: HashMap complement lookup
For each number, ask: "Have I already seen `target - num`?"
```java
public int[] twoSum(int[] nums, int target) {
    java.util.HashMap<Integer, Integer> seen = new java.util.HashMap<>();
    for (int i = 0; i < nums.length; i++) {
        int complement = target - nums[i];
        if (seen.containsKey(complement))
            return new int[]{seen.get(complement), i};
        seen.put(nums[i], i);
    }
    return new int[]{}; // no solution
}
```
**Dry run:** i=0 num=2, need 7 → not seen, store {2:0}. i=1 num=7, need 2 → found at 0 → return `[0,1]`. ✅

**Time:** O(n) • **Space:** O(n).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Complement HashMap (one-pass).
- **Core idea:** Store what you've seen, look up what you need.
- **Memory hook:** 🧩 You're missing a puzzle piece. Instead of searching every box, you keep a labeled drawer of pieces you've already seen and instantly check it.
- **Trigger phrase:** "Find a pair summing to target" → HashMap complement.

---

## 2️⃣ Add Two Numbers (Linked List)

### 📋 Full Question
Two numbers stored as linked lists in **reverse** order (each node = one digit). Add them, return the sum as a linked list.
**Example:** `(2→4→3) + (5→6→4)` = 342 + 465 = 807 → `(7→0→8)`.

### 🗣️ What to say first
- "Digits stored least-significant first?" (Yes — that's convenient!)
- "Can lists be different lengths?" (Yes.)
- "Can there be a leading carry?" (Yes — e.g. 5+5=10.)

### Approach 1 — Brute Force
Convert each list to a number, add, convert back.
```java
// Convert l1 -> long, l2 -> long, add, rebuild list from the sum's digits.
// Works for tiny inputs but OVERFLOWS for long lists (numbers exceed long range).
```
❌ Breaks for huge numbers (overflow beyond `long`). Mention it only to dismiss it.

### Approach 2 — Optimal: Elementary-school addition with carry
```java
public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
    ListNode dummy = new ListNode(0);
    ListNode cur = dummy;
    int carry = 0;
    while (l1 != null || l2 != null || carry != 0) {
        int sum = carry;
        if (l1 != null) { sum += l1.val; l1 = l1.next; }
        if (l2 != null) { sum += l2.val; l2 = l2.next; }
        carry = sum / 10;
        cur.next = new ListNode(sum % 10);
        cur = cur.next;
    }
    return dummy.next;
}
```
**Dry run:** 2+5=7 carry0 →7. 4+6=10 carry1 →0. 3+4+1=8 →8. Result `7→0→8`. ✅

**Time:** O(max(m,n)) • **Space:** O(max(m,n)).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Dummy head + carry traversal.
- **Core idea:** Add digit by digit, carry the overflow — just like grade-school math. ➕
- **Memory hook:** 🧮 An abacus: each bead column adds, and the "10 makes 1" carry slides to the next column.
- **Trigger phrase:** "Linked list digit arithmetic" → dummy node + carry loop.

---

## 3️⃣ Longest Substring Without Repeating Characters

### 📋 Full Question
Find the length of the longest substring with no repeating characters.
**Example:** `"abcabcbb"` → `3` (the substring `"abc"`).

### 🗣️ What to say first
- "Substring (contiguous) or subsequence?" (Substring.)
- "What charset — ASCII, Unicode?" (Affects map vs array.)
- "Return the length or the string itself?" (Length.)

### Approach 1 — Brute Force
Check every substring for uniqueness using a set.
```java
public int lengthBrute(String s) {
    int max = 0;
    for (int i = 0; i < s.length(); i++) {
        java.util.Set<Character> set = new java.util.HashSet<>();
        for (int j = i; j < s.length(); j++) {
            if (!set.add(s.charAt(j))) break; // repeat found
            max = Math.max(max, j - i + 1);
        }
    }
    return max;
}
```
**Time:** O(n²) • **Space:** O(min(n, charset)). ❌ Too slow.

### Approach 2 — Optimal: Sliding Window + last-seen map
Expand the window with `right`; when you hit a repeat, jump `left` past the duplicate's previous position.
```java
public int lengthOfLongestSubstring(String s) {
    java.util.HashMap<Character, Integer> lastSeen = new java.util.HashMap<>();
    int maxLen = 0, left = 0;
    for (int right = 0; right < s.length(); right++) {
        char ch = s.charAt(right);
        if (lastSeen.containsKey(ch) && lastSeen.get(ch) >= left) {
            left = lastSeen.get(ch) + 1;   // shrink window past the duplicate
        }
        lastSeen.put(ch, right);
        maxLen = Math.max(maxLen, right - left + 1);
    }
    return maxLen;
}
```
**Dry run** `"abcabcbb"`: window grows a,b,c (len3). At right=3 'a' seen at 0≥left → left=1. Keeps sliding, max stays 3. ✅

**Time:** O(n) • **Space:** O(min(n, charset)).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Sliding Window with a hash map.
- **Core idea:** Grow the window; on a repeat, snap the left edge forward. 🪟
- **Memory hook:** 🐛 An inchworm: the front stretches forward, and when it overlaps itself the back catches up.
- **Trigger phrase:** "Longest substring with a constraint" → sliding window.

---

## 4️⃣ String to Integer (atoi)

### 📋 Full Question
Convert a string to a 32-bit signed integer (like C's `atoi`): skip leading spaces, read an optional sign, read digits until a non-digit, clamp to `[-2³¹, 2³¹-1]`.
**Example:** `"   -42abc"` → `-42`; `"4193 with words"` → `4193`; `"words 99"` → `0`; `"91283472332"` → `2147483647` (clamped).

### 🗣️ What to say first
- "Should I trim leading whitespace only?" (Yes, leading only.)
- "What if it overflows int range?" (Clamp to INT_MAX / INT_MIN.)
- "Stop at the first non-digit after the sign?" (Yes.)

### Approach 1 — Brute Force
Strip, regex-extract a number, `Integer.parseInt`.
```java
// "Lazy" version: regex out the number, parse it.
// Fails subtly on overflow (parseInt throws) and on partial-prefix rules.
// Mention only to dismiss — the manual char scan is the expected answer.
```
❌ `parseInt` throws on overflow and doesn't follow atoi's "stop at first non-digit" rule cleanly.

### Approach 2 — Optimal: Careful character scan with overflow clamp
```java
public int myAtoi(String s) {
    int i = 0, n = s.length();
    while (i < n && s.charAt(i) == ' ') i++;          // 1. skip spaces
    if (i == n) return 0;

    int sign = 1;
    if (s.charAt(i) == '+' || s.charAt(i) == '-') {   // 2. optional sign
        if (s.charAt(i) == '-') sign = -1;
        i++;
    }

    int result = 0;
    while (i < n && Character.isDigit(s.charAt(i))) {  // 3. read digits
        int digit = s.charAt(i) - '0';
        // 4. overflow check BEFORE multiplying
        if (result > (Integer.MAX_VALUE - digit) / 10) {
            return sign == 1 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }
        result = result * 10 + digit;
        i++;
    }
    return result * sign;
}
```
**Dry run** `"   -42abc"`: skip 3 spaces → '-' sign=-1 → '4' result=4 → '2' result=42 → 'a' stop → 42×(-1) = -42. ✅

**Time:** O(n) • **Space:** O(1).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Stateful string parse (skip → sign → digits → clamp).
- **Core idea:** March left-to-right through 4 phases; check overflow *before* it happens.
- **Memory hook:** 🛂 Airport passport control: queue (spaces) → show passport (sign) → process travelers one by one (digits) → turn back anyone over the limit (clamp).
- **Trigger phrase:** "Parse a number/token from messy input" → stateful char scan.

---

## 5️⃣ Merge Intervals

### 📋 Full Question
Given intervals, merge all overlapping ones.
**Example:** `[[1,3],[2,6],[8,10],[15,18]]` → `[[1,6],[8,10],[15,18]]` (1-3 and 2-6 overlap → 1-6).

### 🗣️ What to say first
- "Are intervals sorted?" (Usually not — I'll sort first.)
- "Do touching intervals like [1,2],[2,3] count as overlapping?" (Define it — usually yes.)

### Approach 1 — Brute Force
Compare every interval against every other and merge repeatedly until stable — O(n²) or worse. ❌ Messy and slow.

### Approach 2 — Optimal: Sort by start, then sweep
Sort by start time, then walk through. If the current interval overlaps the last merged one, extend it; otherwise add a new one.
```java
public int[][] merge(int[][] intervals) {
    if (intervals.length <= 1) return intervals;
    java.util.Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));
    java.util.List<int[]> result = new java.util.ArrayList<>();
    int[] current = intervals[0];
    result.add(current);
    for (int[] next : intervals) {
        if (next[0] <= current[1]) {           // overlap → extend end
            current[1] = Math.max(current[1], next[1]);
        } else {                               // no overlap → start new
            current = next;
            result.add(current);
        }
    }
    return result.toArray(new int[result.size()][]);
}
```
**Dry run:** Sorted: `[1,3],[2,6],[8,10],[15,18]`. current=[1,3]. next [2,6]: 2≤3 → extend to [1,6]. next [8,10]: 8>6 → add. next [15,18]: 15>10 → add. → `[[1,6],[8,10],[15,18]]`. ✅

**Time:** O(n log n) (the sort) • **Space:** O(n) for output (or O(log n) ignoring output).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Sort + linear sweep merge.
- **Core idea:** Line everything up by start, then greedily glue overlaps. 🩹
- **Memory hook:** 📅 Merging back-to-back calendar meetings into one big block.
- **Trigger phrase:** "Overlapping intervals" → sort by start, sweep.

---

## 6️⃣ Product of Array Except Self

### 📋 Full Question
Return an array where `output[i]` = product of all elements **except** `nums[i]`. **No division allowed.**
**Example:** `[1,2,3,4]` → `[24,12,8,6]`.

### 🗣️ What to say first
- "Am I allowed to use division?" (No — that's the catch.)
- "Are there zeros?" (Yes — which is exactly why division is banned.)
- "Can I use extra space for output?" (Output doesn't count — can I do O(1) extra beyond it?)

### Approach 1 — Brute Force
For each `i`, multiply all other elements with a nested loop.
```java
public int[] productBrute(int[] nums) {
    int n = nums.length;
    int[] out = new int[n];
    for (int i = 0; i < n; i++) {
        int prod = 1;
        for (int j = 0; j < n; j++) if (j != i) prod *= nums[j];
        out[i] = prod;
    }
    return out;
}
```
**Time:** O(n²) • **Space:** O(1). ❌ Too slow.

### Approach 2 — Optimal: Prefix × Suffix products
Each answer = (product of everything to the left) × (product of everything to the right).
```java
public int[] productExceptSelf(int[] nums) {
    int n = nums.length;
    int[] result = new int[n];

    result[0] = 1;
    for (int i = 1; i < n; i++)               // left products
        result[i] = result[i - 1] * nums[i - 1];

    int rightProduct = 1;
    for (int i = n - 1; i >= 0; i--) {        // multiply right products in place
        result[i] *= rightProduct;
        rightProduct *= nums[i];
    }
    return result;
}
```
**Dry run** `[1,2,3,4]`: left pass → `[1,1,2,6]`. right pass: i=3 ×1=6, right=4; i=2 ×4=8, right=12; i=1 ×12=12, right=24; i=0 ×24=24 → `[24,12,8,6]`. ✅

**Time:** O(n) • **Space:** O(1) extra (output array doesn't count).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Prefix/Suffix product (two sweeps).
- **Core idea:** Left-of-me times right-of-me, no division needed. ✖️
- **Memory hook:** 🤝 Two hands meeting in the middle — left hand carries the running product from the left, right hand from the right.
- **Trigger phrase:** "Product/sum except self, no division" → prefix & suffix passes.

---

## 7️⃣ Search in Rotated Sorted Array

### 📋 Full Question
A sorted array was rotated at an unknown pivot. Find `target`'s index in **O(log n)**, or -1.
**Example:** `nums = [4,5,6,7,0,1,2]`, `target = 0` → `4`.

### 🗣️ What to say first
- "Are there duplicates?" (Classic version: no.)
- "Is exactly one rotation point guaranteed?"
- "Return -1 if not found?" (Yes.)

### Approach 1 — Brute Force
Linear scan.
```java
public int searchBrute(int[] nums, int target) {
    for (int i = 0; i < nums.length; i++)
        if (nums[i] == target) return i;
    return -1;
}
```
**Time:** O(n) • **Space:** O(1). ❌ Ignores the sortedness; interviewer wants log n.

### Approach 2 — Optimal: Modified Binary Search
At each step, **one half is always sorted**. Figure out which, then decide where the target could be.
```java
public int search(int[] nums, int target) {
    int low = 0, high = nums.length - 1;
    while (low <= high) {
        int mid = low + (high - low) / 2;     // avoids overflow
        if (nums[mid] == target) return mid;

        if (nums[low] <= nums[mid]) {          // left half is sorted
            if (nums[low] <= target && target < nums[mid]) high = mid - 1;
            else low = mid + 1;
        } else {                               // right half is sorted
            if (nums[mid] < target && target <= nums[high]) low = mid + 1;
            else high = mid - 1;
        }
    }
    return -1;
}
```
**Dry run** target=0: low=0 high=6 mid=3 (7). Left [4..7] sorted, 0 not in [4,7) → low=4. mid=5 (1). Left [0..1] sorted, 0 in [0,1) → high=4. mid=4 (0) → return 4. ✅

**Time:** O(log n) • **Space:** O(1).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Binary Search on a rotated/sorted-half array.
- **Core idea:** Half is always sorted — search there or jump to the other half. ✂️
- **Memory hook:** 🎡 A spun ferris wheel: even rotated, each side is still in order; check which side is "right-side up" and aim there.
- **Trigger phrase:** "Sorted but rotated, find in O(log n)" → modified binary search.

---

## 8️⃣ Trapping Rain Water

### 📋 Full Question
Given heights of bars (width 1 each), compute how much water is trapped after rain.
**Example:** `[0,1,0,2,1,0,1,3,2,1,2,1]` → `6` units of water.

### 🗣️ What to say first
- "Can heights be zero? Negative?" (Zero yes, negative no.)
- "Bar width is 1?" (Yes.)
- "Return total trapped water, not the shape?" (Total.)

### Approach 1 — Brute Force
For each bar, water = `min(maxLeft, maxRight) - height[i]`, scanning left and right each time.
```java
public int trapBrute(int[] height) {
    int total = 0;
    for (int i = 0; i < height.length; i++) {
        int leftMax = 0, rightMax = 0;
        for (int l = i; l >= 0; l--) leftMax = Math.max(leftMax, height[l]);
        for (int r = i; r < height.length; r++) rightMax = Math.max(rightMax, height[r]);
        total += Math.min(leftMax, rightMax) - height[i];
    }
    return total;
}
```
**Time:** O(n²) • **Space:** O(1). ❌ Re-scans the array for every bar.

### Approach 2 — Optimal: Two pointers
The shorter side bounds the water. Move the pointer on the shorter side inward, tracking each side's running max.
```java
public int trap(int[] height) {
    int left = 0, right = height.length - 1;
    int leftMax = 0, rightMax = 0, water = 0;
    while (left < right) {
        if (height[left] < height[right]) {
            if (height[left] >= leftMax) leftMax = height[left];
            else water += leftMax - height[left];
            left++;
        } else {
            if (height[right] >= rightMax) rightMax = height[right];
            else water += rightMax - height[right];
            right--;
        }
    }
    return water;
}
```
**Dry run** (abridged): pointers close in; whenever the current bar is below the running max of its (shorter) side, the gap fills with water. Totals to 6. ✅

**Time:** O(n) • **Space:** O(1).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Two pointers with running maxima.
- **Core idea:** Water above a bar = min(tallest left, tallest right) − bar; the shorter wall decides. 🌊
- **Memory hook:** 🪣 Two kids walking toward each other along a wall, each remembering the tallest brick behind them; the shorter one always pours the bucket.
- **Trigger phrase:** "Water/area bounded by both sides" → two pointers + max-so-far.

---

## 9️⃣ Min Stack

### 📋 Full Question
Design a stack supporting `push`, `pop`, `top`, and `getMin` — all in **O(1)**.
**Example:**
```
push(-2); push(0); push(-3);
getMin(); // -3
pop();
top();    // 0
getMin(); // -2
```

### 🗣️ What to say first
- "Should every operation be O(1), including getMin?" (Yes.)
- "Can the stack be empty when getMin/top is called?" (Assume valid calls, or guard.)
- "Duplicates of the min value allowed?" (Yes — handle them.)

### Standard Design — Pair each value with the min-so-far
Instead of recomputing the min, store the running minimum alongside each pushed value (or use a second stack). Then `getMin` is just a peek.

```java
class MinStack {
    private final java.util.Deque<int[]> stack = new java.util.ArrayDeque<>();
    // each entry: {value, minAtOrBelowThisPoint}

    public MinStack() {}

    public void push(int val) {
        int min = stack.isEmpty() ? val : Math.min(val, stack.peek()[1]);
        stack.push(new int[]{val, min});
    }

    public void pop() { stack.pop(); }

    public int top() { return stack.peek()[0]; }

    public int getMin() { return stack.peek()[1]; }
}
```
**Dry run:** push(-2)→{-2,-2}; push(0)→{0,-2}; push(-3)→{-3,-3}. getMin=-3. pop removes -3. top={0}→0. getMin=-2. ✅

**Time:** O(1) all ops • **Space:** O(n).

> 💡 Alternative: two stacks — a normal value stack and a "min stack" that only pushes a new value when it's ≤ current min. Same O(1), slightly less memory in the best case.

### 🧠 Algorithm to Remember Forever
- **Pattern:** Augmented stack (store auxiliary info per frame).
- **Core idea:** Carry the answer with the data so you never recompute. 📦
- **Memory hook:** 🏷️ Every box on the stack has a sticky note: "smallest box at or below me." Read the top note instantly.
- **Trigger phrase:** "Stack with O(1) min/max" → store min-so-far per element.

---

## 🔟 Reverse Linked List

### 📋 Full Question
Reverse a singly linked list.
**Example:** `1→2→3→4→5` → `5→4→3→2→1`.

### 🗣️ What to say first
- "Iterative or recursive preferred?" (Show iterative; mention recursive.)
- "Modify in place or return new?" (In place is expected.)

### Approach 1 — Brute Force
Push all values to a stack/array, then rebuild.
```java
public ListNode reverseBrute(ListNode head) {
    java.util.Deque<Integer> stack = new java.util.ArrayDeque<>();
    for (ListNode n = head; n != null; n = n.next) stack.push(n.val);
    ListNode dummy = new ListNode(0), cur = dummy;
    while (!stack.isEmpty()) { cur.next = new ListNode(stack.pop()); cur = cur.next; }
    return dummy.next;
}
```
**Time:** O(n) • **Space:** O(n). ❌ Wasteful — we can flip pointers in place.

### Approach 2 — Optimal: Three-pointer pointer flip
```java
public ListNode reverseList(ListNode head) {
    ListNode prev = null;
    ListNode cur = head;
    while (cur != null) {
        ListNode nextTemp = cur.next; // save next
        cur.next = prev;              // flip pointer
        prev = cur;                   // advance prev
        cur = nextTemp;               // advance cur
    }
    return prev; // new head
}
```
**Dry run:** cur=1: save 2, 1→null, prev=1. cur=2: 2→1, prev=2. … ends prev=5 with chain `5→4→3→2→1`. ✅

**Time:** O(n) • **Space:** O(1).

### 🧠 Algorithm to Remember Forever
- **Pattern:** In-place pointer reversal (prev/cur/next).
- **Core idea:** Walk forward while flipping each arrow backward. ↩️
- **Memory hook:** 🚂 A train reversing direction: each car uncouples from the one ahead and re-couples to the one behind.
- **Trigger phrase:** "Reverse / flip a linked list" → prev-cur-next trio.

---

## 1️⃣1️⃣ Copy List with Random Pointer

### 📋 Full Question
Each node has `next` and a `random` pointer (to any node or null). Return a **deep copy** of the list.
**Example:** Node A(random→C), B(random→A), C(random→B) → an entirely new list with the same value & random structure.

### 🗣️ What to say first
- "Can `random` point to itself or null?" (Yes, both.)
- "Must it be a true deep copy (new nodes)?" (Yes.)
- "Can the list be empty?" (Yes — return null.)

### Approach 1 — Brute Force: HashMap old→new
First pass: clone every node, store `old→new` in a map. Second pass: wire up `next` and `random` using the map.
```java
class Node { int val; Node next, random; Node(int v){val=v;} }

public Node copyRandomListMap(Node head) {
    if (head == null) return null;
    java.util.Map<Node, Node> map = new java.util.HashMap<>();
    for (Node cur = head; cur != null; cur = cur.next)
        map.put(cur, new Node(cur.val));            // clone values
    for (Node cur = head; cur != null; cur = cur.next) {
        map.get(cur).next = map.get(cur.next);      // wire next
        map.get(cur).random = map.get(cur.random);  // wire random
    }
    return map.get(head);
}
```
**Time:** O(n) • **Space:** O(n). ✅ Correct and clean — totally interview-acceptable. The "better" version below drops the map.

### Approach 2 — Optimal: Interleave clones (O(1) extra space)
Weave each clone right after its original (`A→A'→B→B'…`), set randoms via `cur.next`, then unweave.
```java
public Node copyRandomList(Node head) {
    if (head == null) return null;

    // 1. Interleave: A -> A' -> B -> B' ...
    for (Node cur = head; cur != null; cur = cur.next.next) {
        Node clone = new Node(cur.val);
        clone.next = cur.next;
        cur.next = clone;
    }
    // 2. Assign randoms: clone's random = original's random's clone
    for (Node cur = head; cur != null; cur = cur.next.next) {
        if (cur.random != null) cur.next.random = cur.random.next;
    }
    // 3. Unweave the two lists
    Node dummy = new Node(0), copyCur = dummy;
    for (Node cur = head; cur != null; cur = cur.next) {
        copyCur.next = cur.next;     // pull out the clone
        copyCur = copyCur.next;
        cur.next = cur.next.next;    // restore original
    }
    return dummy.next;
}
```
**Dry run:** Interleaving makes each clone reachable as `cur.next`, so `cur.random.next` is exactly the cloned target. Final unweave separates originals from clones. ✅

**Time:** O(n) • **Space:** O(1) extra.

### 🧠 Algorithm to Remember Forever
- **Pattern:** Clone-and-interleave (or old→new HashMap).
- **Core idea:** Make each clone *findable from its original* before wiring randoms.
- **Memory hook:** 👯 Put each twin right behind its original in line; now "your twin" is always the person right behind the target.
- **Trigger phrase:** "Deep copy with cross-pointers" → map or interleave.

---

## 1️⃣2️⃣ LRU Cache (THE classic Microsoft design)

### 📋 Full Question
Design a data structure with `get(key)` and `put(key, value)` that both run in **O(1)**. When capacity is exceeded, evict the **least recently used** item.
**Example:**
```
LRUCache cache = new LRUCache(2);   // capacity 2
cache.put(1, 1);                    // {1=1}
cache.put(2, 2);                    // {1=1, 2=2}
cache.get(1);      // returns 1     // 1 is now most-recent → {2=2, 1=1}
cache.put(3, 3);   // evicts key 2  // {1=1, 3=3}
cache.get(2);      // returns -1 (not found)
```

### 🗣️ What to say first
- "By 'used', do `get` AND `put` both count as a use?" (Yes.)
- "What do I return on a missing `get`?" (-1.)
- "Is capacity always ≥ 1?"

### Approach 1 — Naive design (list scan)
Use a list/array. On every access, scan to find the item and move it to the front; evict from the back.
```java
// Naive: ArrayList<int[]> ordered by recency.
// get(key): linear scan, move found item to end.   -> O(n)
// put(key): linear scan; if full remove index 0.    -> O(n)
```
**Time:** O(n) per operation • **Space:** O(n). ❌ Microsoft explicitly wants **O(1)** for both.

### Approach 2 — Optimal: HashMap + Doubly Linked List
A `HashMap` gives O(1) lookup; a **doubly linked list** gives O(1) move/remove. The head = most recent, the tail = least recent (the eviction victim).
```java
class LRUCache {
    private static class Node {
        int key, value;
        Node prev, next;
        Node(int key, int value) { this.key = key; this.value = value; }
    }

    private final int capacity;
    private final java.util.HashMap<Integer, Node> map = new java.util.HashMap<>();
    private final Node head, tail; // sentinels: head <-> ... <-> tail

    public LRUCache(int capacity) {
        this.capacity = capacity;
        head = new Node(0, 0);
        tail = new Node(0, 0);
        head.next = tail;
        tail.prev = head;
    }

    public int get(int key) {
        Node node = map.get(key);
        if (node == null) return -1;
        remove(node);        // detach
        insertFront(node);   // mark as most-recent
        return node.value;
    }

    public void put(int key, int value) {
        Node existing = map.get(key);
        if (existing != null) {
            existing.value = value;
            remove(existing);
            insertFront(existing);
            return;
        }
        if (map.size() == capacity) {       // evict least-recent (just before tail)
            Node lru = tail.prev;
            remove(lru);
            map.remove(lru.key);
        }
        Node node = new Node(key, value);
        map.put(key, node);
        insertFront(node);
    }

    private void remove(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void insertFront(Node node) {   // insert right after head
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }
}
```
**Dry run** (capacity 2): `put(1,1)` → `[1]`. `put(2,2)` → `[2,1]`. `get(1)` → move 1 to front `[1,2]`, returns 1. `put(3,3)` → full, evict tail.prev = key 2, `[3,1]`. `get(2)` → null → -1. ✅

**Time:** O(1) get & put • **Space:** O(capacity).

> 💡 Pro move: mention `LinkedHashMap` with `removeEldestEntry` as the "I know the shortcut but here's the manual version" flex.

### 🧠 Algorithm to Remember Forever
- **Pattern:** HashMap + Doubly Linked List.
- **Core idea:** Hash for *find fast*, linked list for *reorder fast*.
- **Memory hook:** 🚪 A nightclub with a guest list (HashMap) and a velcro rope line (linked list). The bouncer instantly finds anyone and yanks them to the front. The person at the very back gets kicked out. 🕺
- **Trigger phrase:** "O(1) get/put with eviction" → LRU = Map + DLL.

---

## 1️⃣3️⃣ Number of Islands

### 📋 Full Question
A 2D grid of `'1'` (land) and `'0'` (water). Count the number of islands (land connected horizontally/vertically).
**Example:**
```
1 1 0 0
1 1 0 0
0 0 1 0
0 0 0 1
```
→ **3** islands.

### 🗣️ What to say first
- "Are diagonals connected?" (No — only 4 directions.)
- "Can I modify the grid?" (Usually yes — saves a `visited` array.)
- "Could the grid be empty?"

### Approach 1 — Brute Force
A naive version uses a separate `visited[][]` array instead of mutating the grid. Costs O(m·n) extra space.
```java
// Same DFS but with boolean[][] visited instead of sinking cells.
// Correct, just uses O(m*n) extra space when mutation is allowed.
```
**Time:** O(m·n) • **Space:** O(m·n) extra. ❌ We can drop the extra space by sinking.

### Approach 2 — Optimal: DFS flood fill, sink the island
Walk the grid. Each time you hit land, increment the count and **sink** the whole connected island so you don't count it again.
```java
public int numIslands(char[][] grid) {
    if (grid == null || grid.length == 0) return 0;
    int count = 0;
    for (int r = 0; r < grid.length; r++) {
        for (int c = 0; c < grid[0].length; c++) {
            if (grid[r][c] == '1') {
                count++;
                sink(grid, r, c);
            }
        }
    }
    return count;
}

private void sink(char[][] grid, int r, int c) {
    if (r < 0 || c < 0 || r >= grid.length || c >= grid[0].length || grid[r][c] != '1')
        return;
    grid[r][c] = '0';            // mark visited by sinking
    sink(grid, r + 1, c);
    sink(grid, r - 1, c);
    sink(grid, r, c + 1);
    sink(grid, r, c - 1);
}
```
**Dry run:** Top-left 2x2 block of 1s → count=1, all sunk. Middle single 1 → count=2. Bottom-right single 1 → count=3. ✅

**Time:** O(m·n) • **Space:** O(m·n) worst-case recursion stack.

### 🧠 Algorithm to Remember Forever
- **Pattern:** Grid DFS / Flood Fill.
- **Core idea:** Found land? Drown the whole island so it's counted once. 🌊
- **Memory hook:** 🎨 The paint-bucket tool in MS Paint: click one pixel, it floods the connected region.
- **Trigger phrase:** "Connected regions in a grid" → DFS/BFS flood fill.

---

## 1️⃣4️⃣ Course Schedule

### 📋 Full Question
`numCourses` courses (0..n-1) and `prerequisites[i] = [a, b]` meaning "to take a, first take b." Return `true` if you can finish all courses (i.e., the dependency graph has **no cycle**).
**Example:** `numCourses=2, prereqs=[[1,0]]` → `true`. `[[1,0],[0,1]]` → `false` (cycle).

### 🗣️ What to say first
- "So this is really 'is the directed graph acyclic'?" (Yes.)
- "Can there be duplicate edges?" (Assume not, but handle gracefully.)
- "Just true/false, or the actual order?" (This variant: true/false.)

### Approach 1 — Brute Force: DFS cycle detection
DFS from each node tracking the current recursion path; if you revisit a node on the path, there's a cycle.
```java
public boolean canFinishDFS(int numCourses, int[][] prerequisites) {
    java.util.List<java.util.List<Integer>> graph = new java.util.ArrayList<>();
    for (int i = 0; i < numCourses; i++) graph.add(new java.util.ArrayList<>());
    for (int[] p : prerequisites) graph.get(p[1]).add(p[0]);

    int[] state = new int[numCourses]; // 0=unseen, 1=in-progress, 2=done
    for (int i = 0; i < numCourses; i++)
        if (state[i] == 0 && hasCycle(i, graph, state)) return false;
    return true;
}

private boolean hasCycle(int node, java.util.List<java.util.List<Integer>> graph, int[] state) {
    state[node] = 1;                         // mark in-progress
    for (int next : graph.get(node)) {
        if (state[next] == 1) return true;   // back-edge → cycle
        if (state[next] == 0 && hasCycle(next, graph, state)) return true;
    }
    state[node] = 2;                         // fully explored
    return false;
}
```
**Time:** O(V+E) • **Space:** O(V+E). ✅ Perfectly valid — the BFS version below is the alternate "classic" answer.

### Approach 2 — Optimal: Topological Sort (Kahn's BFS, in-degrees)
Repeatedly remove courses with **0 prerequisites**. If you can remove all of them, no cycle.
```java
public boolean canFinish(int numCourses, int[][] prerequisites) {
    java.util.List<java.util.List<Integer>> graph = new java.util.ArrayList<>();
    int[] indegree = new int[numCourses];
    for (int i = 0; i < numCourses; i++) graph.add(new java.util.ArrayList<>());
    for (int[] p : prerequisites) {
        graph.get(p[1]).add(p[0]);   // edge b -> a
        indegree[p[0]]++;
    }

    java.util.Queue<Integer> queue = new java.util.LinkedList<>();
    for (int i = 0; i < numCourses; i++)
        if (indegree[i] == 0) queue.offer(i);

    int taken = 0;
    while (!queue.isEmpty()) {
        int course = queue.poll();
        taken++;
        for (int next : graph.get(course))
            if (--indegree[next] == 0) queue.offer(next);
    }
    return taken == numCourses;   // all taken ⇒ acyclic
}
```
**Dry run** `[[1,0]]`: indegree[1]=1, indegree[0]=0. Queue=[0]. Take 0, decrement indegree[1]→0, enqueue 1. Take 1. taken=2=numCourses → true. ✅

**Time:** O(V+E) • **Space:** O(V+E).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Topological Sort (Kahn's algorithm) / cycle detection.
- **Core idea:** Keep removing the things that depend on nothing; if anything's left, it's a cycle. 🎓
- **Memory hook:** 🎓 Course catalog: you can only take a class once all its prereqs are checked off — if a class can never get unlocked, there's a dependency loop.
- **Trigger phrase:** "Prerequisites / ordering / detect cycle" → topological sort.

---

## 1️⃣5️⃣ Word Search

### 📋 Full Question
Given a 2D board of letters and a word, return `true` if the word exists in the grid via adjacent (up/down/left/right) cells, no cell reused.
**Example:** board `[[A,B,C,E],[S,F,C,S],[A,D,E,E]]`, word `"ABCCED"` → `true`.

### 🗣️ What to say first
- "Can a cell be used more than once in one path?" (No.)
- "Are diagonal moves allowed?" (No — 4 directions.)
- "Is the word guaranteed non-empty?" (Clarify; guard empty.)

### Approach 1 — Brute Force (concept)
Generate every possible path of length `word.length()` from every cell and check. Exponential and impractical — backtracking with pruning *is* the practical brute-force-to-optimal here.

### Approach 2 — Optimal: DFS Backtracking
From each cell, try to match the word char by char, temporarily marking visited cells, and undoing (backtracking) when a path fails.
```java
public boolean exist(char[][] board, String word) {
    if (board == null || board.length == 0) return false;
    for (int r = 0; r < board.length; r++)
        for (int c = 0; c < board[0].length; c++)
            if (dfs(board, word, r, c, 0)) return true;
    return false;
}

private boolean dfs(char[][] board, String word, int r, int c, int idx) {
    if (idx == word.length()) return true;                 // matched all chars
    if (r < 0 || c < 0 || r >= board.length || c >= board[0].length) return false;
    if (board[r][c] != word.charAt(idx)) return false;

    char temp = board[r][c];
    board[r][c] = '#';                                     // mark visited
    boolean found = dfs(board, word, r + 1, c, idx + 1)
                 || dfs(board, word, r - 1, c, idx + 1)
                 || dfs(board, word, r, c + 1, idx + 1)
                 || dfs(board, word, r, c - 1, idx + 1);
    board[r][c] = temp;                                    // backtrack (restore)
    return found;
}
```
**Dry run** `"ABCCED"`: start at (0,0)='A' matches → 'B' right → 'C' right → 'C' down → 'E' down → 'D' left → idx reaches length → true. ✅

**Time:** O(m·n·4^L) where L = word length • **Space:** O(L) recursion.

### 🧠 Algorithm to Remember Forever
- **Pattern:** Grid DFS Backtracking.
- **Core idea:** Try a path, mark cells as used, undo on failure. 🔙
- **Memory hook:** 🧶 Walking a maze with a ball of yarn: you mark where you've been, and if you hit a dead end you reel the yarn back and try another turn.
- **Trigger phrase:** "Find a path/word in a grid, no reuse" → DFS + backtrack (mark/unmark).

---

## 1️⃣6️⃣ Binary Tree Level Order Traversal

### 📋 Full Question
Return the node values level by level, top to bottom, left to right.
**Example:** tree `3 / (9, 20) , 20 / (15, 7)` → `[[3],[9,20],[15,7]]`.

### 🗣️ What to say first
- "Group each level into its own list?" (Yes.)
- "Left-to-right within a level?" (Yes.)
- "Can the tree be empty?" (Yes — return empty list.)

### Approach 1 — Brute Force: DFS with depth
Recurse, passing the level; append each node into the list for its level.
```java
public java.util.List<java.util.List<Integer>> levelOrderDFS(TreeNode root) {
    java.util.List<java.util.List<Integer>> res = new java.util.ArrayList<>();
    dfs(root, 0, res);
    return res;
}
private void dfs(TreeNode node, int level, java.util.List<java.util.List<Integer>> res) {
    if (node == null) return;
    if (level == res.size()) res.add(new java.util.ArrayList<>());
    res.get(level).add(node.val);
    dfs(node.left, level + 1, res);
    dfs(node.right, level + 1, res);
}
```
**Time:** O(n) • **Space:** O(h) recursion. ✅ Works — but BFS is the textbook "level order" answer.

### Approach 2 — Optimal: BFS with a queue
Process one full level at a time by snapshotting the queue size.
```java
public java.util.List<java.util.List<Integer>> levelOrder(TreeNode root) {
    java.util.List<java.util.List<Integer>> result = new java.util.ArrayList<>();
    if (root == null) return result;

    java.util.Queue<TreeNode> queue = new java.util.LinkedList<>();
    queue.offer(root);
    while (!queue.isEmpty()) {
        int levelSize = queue.size();                 // nodes on this level
        java.util.List<Integer> level = new java.util.ArrayList<>();
        for (int i = 0; i < levelSize; i++) {
            TreeNode node = queue.poll();
            level.add(node.val);
            if (node.left != null) queue.offer(node.left);
            if (node.right != null) queue.offer(node.right);
        }
        result.add(level);
    }
    return result;
}
```
**Dry run:** Queue=[3]. Level size 1 → [3], enqueue 9,20. Size 2 → [9,20], enqueue 15,7. Size 2 → [15,7]. Result `[[3],[9,20],[15,7]]`. ✅

**Time:** O(n) • **Space:** O(n) (queue width).

### 🧠 Algorithm to Remember Forever
- **Pattern:** BFS (queue) level-by-level.
- **Core idea:** Snapshot `queue.size()` to know exactly how many nodes are on the current level. 🪜
- **Memory hook:** 🏢 An elevator stopping floor by floor: it loads everyone on one floor before going to the next.
- **Trigger phrase:** "Level by level / shortest path in unweighted graph" → BFS queue.

---

## 1️⃣7️⃣ Serialize and Deserialize Binary Tree

### 📋 Full Question
Design `serialize(root)` → String and `deserialize(String)` → tree, so that the round trip reproduces the original tree.
**Example:** tree `1 / (2, 3) , 3 / (4, 5)` → `"1,2,#,#,3,4,#,#,5,#,#"` → back to the same tree.

### 🗣️ What to say first
- "Any format is fine as long as it round-trips?" (Yes.)
- "Can node values be negative / multi-digit?" (Yes — use a delimiter.)
- "Can the tree be empty?" (Yes — represent null.)

### Approach 1 — Naive (level-order / BFS like LeetCode's display)
You *can* serialize with BFS and `null` markers, but indexing children during deserialize is fiddlier. Preorder DFS below is cleaner and the standard interview answer.

### Approach 2 — Optimal: Preorder DFS with null markers
Serialize preorder, writing `#` for null. Deserialize by consuming tokens in the same preorder.
```java
public class Codec {
    private static final String NULL = "#";
    private static final String SEP = ",";

    public String serialize(TreeNode root) {
        StringBuilder sb = new StringBuilder();
        buildString(root, sb);
        return sb.toString();
    }
    private void buildString(TreeNode node, StringBuilder sb) {
        if (node == null) { sb.append(NULL).append(SEP); return; }
        sb.append(node.val).append(SEP);
        buildString(node.left, sb);
        buildString(node.right, sb);
    }

    public TreeNode deserialize(String data) {
        java.util.Deque<String> tokens =
            new java.util.ArrayDeque<>(java.util.Arrays.asList(data.split(SEP)));
        return buildTree(tokens);
    }
    private TreeNode buildTree(java.util.Deque<String> tokens) {
        String val = tokens.poll();
        if (NULL.equals(val)) return null;
        TreeNode node = new TreeNode(Integer.parseInt(val));
        node.left = buildTree(tokens);   // preorder: left subtree first
        node.right = buildTree(tokens);
        return node;
    }
}
```
**Dry run:** Serialize `1,2,#,#,3,4,#,#,5,#,#`. Deserialize: poll 1→node, left=poll 2→node, its children #,# → null, right of 1 = poll 3 → … rebuilds the exact tree. ✅

**Time:** O(n) both ways • **Space:** O(n).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Preorder DFS with null sentinels.
- **Core idea:** Record nulls too — that's what makes the structure unambiguous and rebuildable in one pass.
- **Memory hook:** 🍞 Hansel & Gretel: drop a breadcrumb at *every* spot, even the empty ones (`#`), so you can retrace the exact path home.
- **Trigger phrase:** "Encode/rebuild a tree" → preorder traversal + null markers.

---

## 1️⃣8️⃣ Coin Change

### 📋 Full Question
Given coin denominations and an `amount`, return the **fewest coins** to make that amount, or `-1` if impossible. Unlimited coins of each type.
**Example:** `coins = [1,2,5]`, `amount = 11` → `3` (5 + 5 + 1).

### 🗣️ What to say first
- "Unlimited supply of each coin?" (Yes.)
- "Return count of coins, not which ones?" (Count.)
- "Return -1 if amount can't be formed?" (Yes.)

### Approach 1 — Brute Force: recursion (try every coin)
For each amount, try every coin and recurse on the remainder; take the min. Exponential without memoization.
```java
public int coinChangeBrute(int[] coins, int amount) {
    if (amount == 0) return 0;
    if (amount < 0) return -1;
    int min = Integer.MAX_VALUE;
    for (int coin : coins) {
        int sub = coinChangeBrute(coins, amount - coin);
        if (sub >= 0) min = Math.min(min, sub + 1);
    }
    return min == Integer.MAX_VALUE ? -1 : min;
}
```
**Time:** O(coins^amount) • **Space:** O(amount) stack. ❌ Recomputes the same subproblems endlessly.

### Approach 2 — Optimal: Bottom-up DP
`dp[x]` = fewest coins to make amount `x`. Build from 0 up to `amount`.
```java
public int coinChange(int[] coins, int amount) {
    int[] dp = new int[amount + 1];
    java.util.Arrays.fill(dp, amount + 1);   // "infinity" sentinel (can't exceed amount coins)
    dp[0] = 0;
    for (int x = 1; x <= amount; x++) {
        for (int coin : coins) {
            if (coin <= x) {
                dp[x] = Math.min(dp[x], dp[x - coin] + 1);
            }
        }
    }
    return dp[amount] > amount ? -1 : dp[amount];
}
```
**Dry run** `coins=[1,2,5], amount=11`: dp[1]=1, dp[2]=1, dp[5]=1, dp[6]=2, …, dp[10]=2 (5+5), dp[11]=dp[6]+1=3 or dp[10]+1=3 → 3. ✅

**Time:** O(amount × coins) • **Space:** O(amount).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Unbounded-knapsack Dynamic Programming.
- **Core idea:** Best for amount X = 1 + min over coins of (best for X − coin). 🪜
- **Memory hook:** 🪜 Climbing stairs by the fewest hops — to reach step X, look back at the cheapest reachable step and add one hop.
- **Trigger phrase:** "Fewest/most ways to reach a total with reusable items" → DP table.

---

## 1️⃣9️⃣ Edit Distance

### 📋 Full Question
Minimum operations (insert, delete, replace) to convert `word1` into `word2`.
**Example:** `"horse" → "ros"` = `3` (horse→rorse→rose→ros).

### 🗣️ What to say first
- "All three operations cost 1?" (Yes, classic.)
- "Case-sensitive?" (Assume yes.)
- "Either string can be empty?" (Yes.)

### Approach 1 — Brute Force: recursion
Compare last chars; if equal, recurse on both shortened. Else try insert/delete/replace and take min+1. Exponential without memo.
```java
public int editBrute(String a, String b) {
    return rec(a, b, a.length(), b.length());
}
private int rec(String a, String b, int i, int j) {
    if (i == 0) return j;                 // insert all of b's remaining
    if (j == 0) return i;                 // delete all of a's remaining
    if (a.charAt(i - 1) == b.charAt(j - 1)) return rec(a, b, i - 1, j - 1);
    return 1 + Math.min(rec(a, b, i - 1, j),       // delete
             Math.min(rec(a, b, i, j - 1),         // insert
                      rec(a, b, i - 1, j - 1)));    // replace
}
```
**Time:** O(3^(m+n)) • **Space:** O(m+n) stack. ❌ Massive recomputation.

### Approach 2 — Optimal: 2D DP table
`dp[i][j]` = edit distance between first `i` chars of `word1` and first `j` of `word2`.
```java
public int minDistance(String word1, String word2) {
    int m = word1.length(), n = word2.length();
    int[][] dp = new int[m + 1][n + 1];

    for (int i = 0; i <= m; i++) dp[i][0] = i;   // delete i chars
    for (int j = 0; j <= n; j++) dp[0][j] = j;   // insert j chars

    for (int i = 1; i <= m; i++) {
        for (int j = 1; j <= n; j++) {
            if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                dp[i][j] = dp[i - 1][j - 1];      // chars match → no op
            } else {
                dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],          // replace
                              Math.min(dp[i - 1][j],               // delete
                                       dp[i][j - 1]));             // insert
            }
        }
    }
    return dp[m][n];
}
```
**Dry run** `"horse"→"ros"`: table fills so `dp[5][3] = 3`. ✅

**Time:** O(m·n) • **Space:** O(m·n) (can be reduced to O(n) with two rows).

### 🧠 Algorithm to Remember Forever
- **Pattern:** 2D string DP (Levenshtein).
- **Core idea:** Match → carry diagonal; mismatch → 1 + min(replace ↖, delete ↑, insert ←). 📐
- **Memory hook:** 🗺️ A grid of intersections — at each crossing you either walk straight (match) or pay a toll to detour (insert/delete/replace).
- **Trigger phrase:** "Transform one string into another / similarity" → edit-distance DP grid.

---

## 2️⃣0️⃣ Task Scheduler

### 📋 Full Question
Given task labels and a cooldown `n` (same task must be `n` slots apart), return the **minimum total time units** (including idle slots) to finish all tasks.
**Example:** `tasks = [A,A,A,B,B,B], n = 2` → `8` (A B idle A B idle A B).

### 🗣️ What to say first
- "Cooldown applies only between *identical* tasks?" (Yes.)
- "Can I reorder tasks freely?" (Yes.)
- "Return total time including idles, not the schedule?" (Total.)

### Approach 1 — Brute Force: simulate with a max-heap
Each round, pick up to `n+1` of the most-frequent available tasks; put the rest on cooldown; advance time.
```java
public int leastIntervalSim(char[] tasks, int n) {
    int[] freq = new int[26];
    for (char t : tasks) freq[t - 'A']++;
    java.util.PriorityQueue<Integer> heap =
        new java.util.PriorityQueue<>(java.util.Collections.reverseOrder());
    for (int f : freq) if (f > 0) heap.offer(f);

    int time = 0;
    while (!heap.isEmpty()) {
        java.util.List<Integer> temp = new java.util.ArrayList<>();
        for (int i = 0; i <= n; i++) {           // one cooldown cycle
            if (!heap.isEmpty()) {
                int cur = heap.poll() - 1;
                if (cur > 0) temp.add(cur);
            }
            time++;
            if (heap.isEmpty() && temp.isEmpty()) break; // all done
        }
        heap.addAll(temp);
    }
    return time;
}
```
**Time:** O(total time) • **Space:** O(1) (26 letters). ✅ Correct; the math formula below is the slick O(n) answer.

### Approach 2 — Optimal: Greedy math (fill the gaps of the most frequent task)
The busiest task forms a skeleton of `(maxFreq - 1)` gaps of size `(n + 1)`; other tasks fill those idle slots. Answer = `max(tasks.length, formula)`.
```java
public int leastInterval(char[] tasks, int n) {
    int[] freq = new int[26];
    for (char t : tasks) freq[t - 'A']++;

    int maxFreq = 0;
    for (int f : freq) maxFreq = Math.max(maxFreq, f);

    int countMax = 0;                            // how many tasks hit maxFreq
    for (int f : freq) if (f == maxFreq) countMax++;

    // (maxFreq-1) full chunks of size (n+1), plus the final row of countMax tasks
    int slots = (maxFreq - 1) * (n + 1) + countMax;

    return Math.max(tasks.length, slots);        // if no idle needed, just length
}
```
**Dry run** `[A,A,A,B,B,B], n=2`: maxFreq=3, countMax=2. slots=(3-1)*(2+1)+2 = 6+2 = 8. max(6, 8)=8. ✅

**Time:** O(total tasks) • **Space:** O(1).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Greedy frequency scheduling (heap or formula).
- **Core idea:** The most frequent task dictates the skeleton; everything else fills its idle gaps. 🧱
- **Memory hook:** 🧱 Laying bricks with mortar gaps: the tallest stack of identical bricks sets the wall's width; you stuff other bricks into the mortar gaps so they don't go to waste.
- **Trigger phrase:** "Schedule with cooldown / rearrange so same items are k apart" → greedy + max frequency.

---

## 📊 Complexity Cheat Sheet

| # | Problem | Brute Force | Optimal | Technique |
|---|---|---|---|---|
| 1 | Two Sum | O(n²) | **O(n)** | Complement HashMap |
| 2 | Add Two Numbers | O(n) (overflow-prone) | **O(max(m,n))** | Dummy head + carry |
| 3 | Longest Substring No Repeat | O(n²) | **O(n)** | Sliding Window + map |
| 4 | String to Integer (atoi) | O(n) (fragile) | **O(n)** | Stateful char scan + clamp |
| 5 | Merge Intervals | O(n²) | **O(n log n)** | Sort + sweep |
| 6 | Product Except Self | O(n²) | **O(n), O(1) extra** | Prefix × Suffix |
| 7 | Search Rotated Array | O(n) | **O(log n)** | Modified Binary Search |
| 8 | Trapping Rain Water | O(n²) | **O(n), O(1)** | Two pointers + max-so-far |
| 9 | Min Stack | — | **O(1) all ops** | Augmented stack (min per frame) |
| 10 | Reverse Linked List | O(n) space | **O(n) time, O(1) space** | Pointer flip (prev/cur/next) |
| 11 | Copy List w/ Random Ptr | O(n) + O(n) map | **O(n), O(1) extra** | Interleave clones |
| 12 | LRU Cache | O(n) per op | **O(1) get/put** | HashMap + Doubly Linked List |
| 13 | Number of Islands | O(m·n) + extra | **O(m·n)** | DFS / Flood Fill |
| 14 | Course Schedule | O(V+E) DFS | **O(V+E)** | Topological Sort (Kahn) |
| 15 | Word Search | exponential | **O(m·n·4^L)** | DFS Backtracking |
| 16 | Level Order Traversal | O(n) DFS | **O(n)** | BFS queue (level snapshot) |
| 17 | Serialize/Deserialize BT | — | **O(n)** | Preorder DFS + null markers |
| 18 | Coin Change | O(coins^amount) | **O(amount·coins)** | Bottom-up DP |
| 19 | Edit Distance | O(3^(m+n)) | **O(m·n)** | 2D string DP |
| 20 | Task Scheduler | O(total time) | **O(n)** | Greedy + max frequency |

---

## 🧠🧠 ALGORITHM RECOGNITION TABLE

> If the question says X → reach for algorithm Y. **This is your secret decoder ring.** 🔑

| If the question says… (trigger) | Use this algorithm | Memory hook | Big-O |
|---|---|---|---|
| "Find a pair that sums to target" | Complement HashMap | 🧩 Labeled puzzle-piece drawer | O(n) |
| "Add/manipulate digits in a linked list" | Dummy node + carry loop | 🧮 Abacus carry | O(n) |
| "Longest substring with a constraint" | Sliding Window + map | 🐛 Inchworm | O(n) |
| "Parse a number/token from messy input" | Stateful char scan + clamp | 🛂 Passport control | O(n) |
| "Merge overlapping intervals" | Sort by start + sweep | 📅 Merging calendar blocks | O(n log n) |
| "Compute X except self, no division" | Prefix & Suffix products | 🤝 Two hands meeting | O(n) |
| "Sorted but rotated, search fast" | Modified Binary Search | 🎡 Spun ferris wheel | O(log n) |
| "Water/area bounded by both sides" | Two pointers + max-so-far | 🪣 Two kids along a wall | O(n) |
| "Stack with O(1) min/max" | Augmented stack | 🏷️ Sticky-note boxes | O(1) |
| "Reverse / flip a linked list" | prev-cur-next pointer flip | 🚂 Train reversing | O(n), O(1) space |
| "Deep copy with cross-pointers" | HashMap or interleave clones | 👯 Twins behind originals | O(n) |
| "O(1) get/put with eviction" | HashMap + Doubly Linked List | 🚪 Nightclub list + velcro rope | O(1) |
| "Count connected regions in a grid" | DFS / BFS Flood Fill | 🎨 Paint bucket | O(m·n) |
| "Prerequisites / ordering / detect cycle" | Topological Sort (Kahn) | 🎓 Course prereq chain | O(V+E) |
| "Find a path/word in a grid, no reuse" | DFS Backtracking (mark/unmark) | 🧶 Yarn through a maze | O(m·n·4^L) |
| "Level by level / shortest unweighted path" | BFS queue | 🏢 Elevator floor by floor | O(n) |
| "Encode/rebuild a tree" | Preorder DFS + null markers | 🍞 Breadcrumbs everywhere | O(n) |
| "Fewest/most ways to reach a total" | Dynamic Programming | 🪜 Build smaller answers up | varies |
| "Transform one string into another" | 2D string DP (edit distance) | 🗺️ Toll-road grid | O(m·n) |
| "Schedule with cooldown / k apart" | Greedy + max frequency | 🧱 Bricks with mortar gaps | O(n) |
| "Find Kth largest / streaming top-K" | Heap (PriorityQueue) | 🏔️ Tip of the mountain | O(n log k) |

---

## ☕ Java Tips

- **Overflow-safe mid:** always `int mid = low + (high - low) / 2;` — never `(low + high) / 2`.
- **Overflow check in atoi:** test `result > (Integer.MAX_VALUE - digit) / 10` *before* multiplying.
- **Use a dummy/sentinel node** for linked-list problems so you don't special-case the head.
- **`HashMap.getOrDefault(k, 0)`** is cleaner than `containsKey` + `get` for counting.
- **`Arrays.sort(arr, (a,b) -> Integer.compare(a[0], b[0]))`** — use `Integer.compare`, never `a[0]-b[0]` (that can overflow).
- **`ArrayDeque`** is your go-to for stacks and queues — faster than `Stack`/`LinkedList`, no legacy locking.
- **`PriorityQueue`** = min-heap by default; for a max-heap use `new PriorityQueue<>(Collections.reverseOrder())`.
- **Convert `List<int[]>` to `int[][]`:** `list.toArray(new int[list.size()][]);`
- **Char-to-index:** `s.charAt(i) - 'a'` (or `- 'A'`) for fixed 26-letter arrays — faster than a HashMap.
- **2D DP allocation:** `new int[m + 1][n + 1]` and remember row/col 0 are the base cases.
- **Backtracking restore:** always undo your mark (`board[r][c] = temp;`) after the recursive calls.

### 🪟 Microsoft Interview Behavior Tips

- **Think out loud — always.** 🗣️ Silence is the #1 way to fail. Narrate even your false starts: "Hmm, that would be O(n²), let me reconsider…"
- **Test edge cases before they ask.** ✅ Empty input, single element, all-same values, negatives, nulls, capacity 1. Say "Let me check the empty case…" — interviewers love this.
- **Ask before you assume.** ❓ "Can I mutate the input?" "Are there duplicates?" "Is capacity ≥ 1?" Wrong assumptions silently sink you.
- **Brute force first, then optimize.** 🐢→🚀 Microsoft grades your *process*, not just the final answer. Show the journey.
- **Write clean, named code.** Use `left`/`right`/`carry`/`indegree`, not `i`/`j` everywhere.
- **State complexity unprompted.** End every problem with "Time O(…), Space O(…)."
- **Stay calm if stuck.** Restate the problem, try a tiny example by hand — patterns jump out.
- **For design problems** (LRU, Min Stack), state the data-structure combo *first* ("HashMap for lookup + DLL for ordering"), then code. Interviewers want the architecture before the syntax.

---

## ✅ Final Interview Checklist

Before you say "I'm done," confirm:

- [ ] I **clarified** the problem (empty? duplicates? sorted? mutate input? capacity?) before coding.
- [ ] I **stated the brute force** (or the standard design) out loud, then explained why we can do better.
- [ ] I **described the optimal approach + key insight** before typing.
- [ ] I **narrated** my code as I wrote it (thinking out loud).
- [ ] I **dry-ran** the example by hand and it produced the expected output.
- [ ] I **tested edge cases**: empty, single element, nulls, negatives, all-same, overflow.
- [ ] I **stated Time and Space complexity** at the end.
- [ ] My code is **clean**: meaningful names, dummy/sentinel nodes, overflow-safe arithmetic, restored backtracking marks.
- [ ] I can **answer a follow-up**: "What if the input is huge / streamed / has duplicates / needs O(1) space?"

🎤 Now go own that Microsoft interview. You've got all 20 patterns, the hooks, and the script. Talk, code, test, smile. 🚀
