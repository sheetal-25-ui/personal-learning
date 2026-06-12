# Week 9: Microsoft Most-Asked Questions (Top 20) — How to ANSWER in a Microsoft Interview 🎤 (Java)

Welcome to your **Microsoft interview battle plan**! 🛡️ This file doesn't just give you code — it teaches you *how to talk through* the 9 most-loved Microsoft problems the way an interviewer wants to hear it. Think of yourself as a tour guide walking the interviewer through your brain. 🧠✨

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

# The 9 Must-Know Problems 🎯

---

## 1️⃣ LRU Cache (THE #1 Microsoft question)

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

### Approach 1 — Brute Force
Use a list/array. On every access, scan to find the item and move it to the front; evict from the back.

```java
// Brute force idea: ArrayList<int[]> ordered by recency.
// get(key): linear scan, move found item to end.   -> O(n)
// put(key): linear scan; if full remove index 0.    -> O(n)
```
**Time:** O(n) per operation • **Space:** O(n).
❌ Not enough — Microsoft explicitly wants **O(1)** for both operations.

### Approach 2 — Optimal: HashMap + Doubly Linked List
A `HashMap` gives O(1) lookup; a **doubly linked list** gives O(1) move/remove. The head = most recent, the tail = least recent (the eviction victim).

```java
class LRUCache {
    // Doubly linked list node
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

**Dry run** (capacity 2): `put(1,1)` → list `[1]`. `put(2,2)` → `[2,1]`. `get(1)` → move 1 to front `[1,2]`, returns 1. `put(3,3)` → full, evict tail.prev = key 2, list `[3,1]`. `get(2)` → null → -1. ✅

**Time:** O(1) get & put • **Space:** O(capacity).

> 💡 Pro move: mention `LinkedHashMap` with `removeEldestEntry` as the "I know the shortcut but here's the manual version" flex.

### 🧠 Algorithm to Remember Forever
- **Pattern:** HashMap + Doubly Linked List
- **Core idea:** Hash for *find fast*, linked list for *reorder fast*.
- **Memory hook:** 🚪 A nightclub with a guest list (HashMap) and a velcro rope line (linked list). The bouncer can instantly find anyone (map) and yank them to the front (list). The person at the very back gets kicked out. 🕺
- **Trigger phrase:** "O(1) get/put with eviction" → LRU = Map + DLL.

---

## 2️⃣ Two Sum

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

## 3️⃣ Add Two Numbers (Linked List)

### 📋 Full Question
Two numbers stored as linked lists in **reverse** order (each node = one digit). Add them, return the sum as a linked list.
**Example:** `(2→4→3) + (5→6→4)` = 342 + 465 = 807 → `(7→0→8)`.

### 🗣️ What to say first
- "Digits stored least-significant first?" (Yes — that's convenient!)
- "Can lists be different lengths?" (Yes.)
- "Can there be a leading carry?" (Yes — e.g. 5+5=10.)

### Approach 1 — Brute Force
Convert each list to a number, add, convert back. ❌ Breaks for huge numbers (overflow beyond `long`). Don't do this — mention it only to dismiss it.

### Approach 2 — Optimal: Elementary-school addition with carry
```java
class ListNode { int val; ListNode next; ListNode(int v){val=v;} }

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

## 4️⃣ Number of Islands

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
There isn't really a "non-graph" brute force — but a naive version uses a separate `visited[][]` array instead of mutating the grid. Costs O(m·n) extra space. We can do better on space.

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

## 6️⃣ Reverse Linked List

### 📋 Full Question
Reverse a singly linked list.
**Example:** `1→2→3→4→5` → `5→4→3→2→1`.

### 🗣️ What to say first
- "Iterative or recursive preferred?" (Show iterative; mention recursive.)
- "Modify in place or return new?" (In place is expected.)

### Approach 1 — Brute Force
Push all values to a stack/array, then rebuild. O(n) extra space. ❌ Wasteful — we can flip pointers in place.

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

## 7️⃣ Longest Substring Without Repeating Characters

### 📋 Full Question
Find the length of the longest substring with no repeating characters.
**Example:** `"abcabcbb"` → `3` (the substring `"abc"`).

### 🗣️ What to say first
- "Substring (contiguous) or subsequence?" (Substring.)
- "What charset — ASCII, Unicode?" (Affects map vs array.)
- "Return the length or the string itself?" (Length.)

### Approach 1 — Brute Force
Check every substring for uniqueness using a set — O(n³) or O(n²). ❌ Too slow.

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

## 8️⃣ Product of Array Except Self

### 📋 Full Question
Return an array where `output[i]` = product of all elements **except** `nums[i]`. **No division allowed.**
**Example:** `[1,2,3,4]` → `[24,12,8,6]`.

### 🗣️ What to say first
- "Am I allowed to use division?" (No — that's the catch.)
- "Are there zeros?" (Yes — which is exactly why division is banned.)
- "Can I use extra space for output?" (Output doesn't count — can I do O(1) extra beyond it?)

### Approach 1 — Brute Force
For each `i`, multiply all other elements with a nested loop. O(n²). ❌ Too slow.

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

## 9️⃣ Search in Rotated Sorted Array

### 📋 Full Question
A sorted array was rotated at an unknown pivot. Find `target`'s index in **O(log n)**, or -1.
**Example:** `nums = [4,5,6,7,0,1,2]`, `target = 0` → `4`.

### 🗣️ What to say first
- "Are there duplicates?" (Classic version: no.)
- "Is exactly one rotation point guaranteed?"
- "Return -1 if not found?" (Yes.)

### Approach 1 — Brute Force
Linear scan. O(n). ❌ Ignores the sortedness; interviewer wants log n.

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

## 📊 Complexity Cheat Sheet

| Problem | Brute Force | Optimal | Technique |
|---|---|---|---|
| LRU Cache | O(n) per op | **O(1)** get/put | HashMap + Doubly Linked List |
| Two Sum | O(n²) | **O(n)** | Complement HashMap |
| Add Two Numbers | O(n) (overflow-prone) | **O(max(m,n))** | Dummy head + carry |
| Number of Islands | O(m·n) + extra | **O(m·n)** | DFS / Flood Fill |
| Merge Intervals | O(n²) | **O(n log n)** | Sort + sweep |
| Reverse Linked List | O(n) space | **O(n) time, O(1) space** | Pointer flip (prev/cur/next) |
| Longest Substring No Repeat | O(n²)/O(n³) | **O(n)** | Sliding Window + map |
| Product Except Self | O(n²) | **O(n), O(1) extra** | Prefix × Suffix |
| Search Rotated Array | O(n) | **O(log n)** | Modified Binary Search |

---

## 🧠🧠 ALGORITHM RECOGNITION TABLE

> If the question says X → reach for algorithm Y. **This is your secret decoder ring.** 🔑

| If the question says… (trigger) | Use this algorithm | Memory hook | Big-O |
|---|---|---|---|
| "O(1) get/put with eviction" | HashMap + Doubly Linked List | 🚪 Nightclub list + velcro rope | O(1) |
| "Find a pair that sums to target" | Complement HashMap | 🧩 Labeled puzzle-piece drawer | O(n) |
| "Add/manipulate digits in a linked list" | Dummy node + carry loop | 🧮 Abacus carry | O(n) |
| "Count connected regions in a grid" | DFS / BFS Flood Fill | 🎨 Paint bucket | O(m·n) |
| "Merge overlapping intervals" | Sort by start + sweep | 📅 Merging calendar blocks | O(n log n) |
| "Reverse / flip a linked list" | prev-cur-next pointer flip | 🚂 Train reversing | O(n), O(1) space |
| "Longest substring with a constraint" | Sliding Window + map | 🐛 Inchworm | O(n) |
| "Compute X except self, no division" | Prefix & Suffix products | 🤝 Two hands meeting | O(n) |
| "Sorted but rotated, search fast" | Modified Binary Search | 🎡 Spun ferris wheel | O(log n) |
| "Find Kth largest / streaming top-K" | Heap (PriorityQueue) | 🏔️ Tip of the mountain | O(n log k) |
| "Schedule with prerequisites / cycle?" | Topological Sort (BFS Kahn) | 🎓 Course prereq chain | O(V+E) |
| "Min coins / fewest steps to reach N" | Dynamic Programming | 🪜 Build smaller answers up | varies |

---

## ☕ Java Tips

- **Overflow-safe mid:** always `int mid = low + (high - low) / 2;` — never `(low + high) / 2`.
- **Use a dummy/sentinel node** for linked-list problems so you don't special-case the head.
- **`HashMap.getOrDefault(k, 0)`** is cleaner than `containsKey` + `get` for counting.
- **`Arrays.sort(arr, (a,b) -> Integer.compare(a[0], b[0]))`** — use `Integer.compare`, never `a[0]-b[0]` (that can overflow).
- **`ArrayDeque`** is your go-to for stacks and queues — faster than `Stack`/`LinkedList`, no legacy locking.
- **`PriorityQueue`** = min-heap by default; for a max-heap use `new PriorityQueue<>(Collections.reverseOrder())`.
- **Convert `List<int[]>` to `int[][]`:** `list.toArray(new int[list.size()][]);`
- **Char-to-index:** `s.charAt(i) - 'a'` for lowercase-only fixed arrays (faster than a HashMap).

### 🪟 Microsoft Interview Behavior Tips

- **Think out loud — always.** 🗣️ Silence is the #1 way to fail. Narrate even your false starts: "Hmm, that would be O(n²), let me reconsider…"
- **Test edge cases before they ask.** ✅ Empty input, single element, all-same values, negatives, nulls. Say "Let me check the empty case…" — interviewers love this.
- **Ask before you assume.** ❓ "Can I mutate the input?" "Are there duplicates?" Wrong assumptions silently sink you.
- **Brute force first, then optimize.** 🐢→🚀 Microsoft grades your *process*, not just the final answer. Show the journey.
- **Write clean, named code.** Use `left`/`right`/`carry`, not `i`/`j` everywhere.
- **State complexity unprompted.** End every problem with "Time O(…), Space O(…)."
- **Stay calm if stuck.** Restate the problem, try a tiny example by hand — patterns jump out.

---

## ✅ Final Interview Checklist

Before you say "I'm done," confirm:

- [ ] I **clarified** the problem (empty? duplicates? sorted? mutate input?) before coding.
- [ ] I **stated the brute force** out loud, then explained why it's not enough.
- [ ] I **described the optimal approach + key insight** before typing.
- [ ] I **narrated** my code as I wrote it (thinking out loud).
- [ ] I **dry-ran** the example by hand and it produced the expected output.
- [ ] I **tested edge cases**: empty, single element, nulls, negatives, all-same.
- [ ] I **stated Time and Space complexity** at the end.
- [ ] My code is **clean**: meaningful names, dummy nodes, overflow-safe arithmetic.
- [ ] I can **answer a follow-up**: "What if the input is huge / streamed / has duplicates?"

🎤 Now go own that Microsoft interview. You've got the patterns, the hooks, and the script. Talk, code, test, smile. 🚀
