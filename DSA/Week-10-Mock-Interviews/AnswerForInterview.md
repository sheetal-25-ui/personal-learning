# Week 10: Mock Interviews — How to ANSWER in a Microsoft Interview 🎤 (Java)

> This is the **dress rehearsal** week. By now you know the patterns. This week is about *performance* — turning "I can solve it at home" into "I can solve it live, out loud, with a stranger watching." 🎭
>
> Think of a coding interview like cooking on a TV show. The judges don't just taste the food — they watch how you chop, how calm you stay, how you talk through your choices. A silent genius loses to a chatty competent cook. **This file teaches you to be the chatty, competent cook.** 🍳

---

## 🎬 The Mock Interview Playbook

A Microsoft loop round is **45 minutes**. That feels long until you're in it — then it evaporates. Here is the minute-by-minute budget that keeps you on track.

### ⏱️ The 45-Minute Timeline

| Phase | Time | What you do | Why it matters |
|-------|------|-------------|----------------|
| 🤝 **Intro & rapport** | 0–3 min | Smile, intro, listen to the problem | Sets a human tone; interviewer is rooting for likeable people |
| ❓ **Clarify** | 3–7 min | Ask questions, restate the problem | Proves you don't code blindly. **Skipping this is the #1 junior mistake.** |
| 📝 **Examples** | 7–10 min | Write 1 normal + 1 edge example by hand | Forces you to actually understand inputs/outputs |
| 🐢 **Brute force + complexity** | 10–15 min | State the obvious solution & its Big-O | Shows you can always produce *something* that works |
| 🚀 **Optimize** | 15–22 min | Find the pattern, propose the better approach, get a nod | This is where you earn the "Hire" |
| ⌨️ **Code** | 22–38 min | Write clean Java, narrating as you go | Clean, working code under time pressure |
| 🧪 **Test** | 38–43 min | Dry-run your code on your examples | Catching your own bug > interviewer catching it |
| 📊 **Recap & follow-ups** | 43–45 min | State final complexity, answer "can you do better?" | Strong finish, leaves them confident |

> **Golden rule:** Never write a line of code before minute ~22. If you're coding at minute 8, you skipped clarifying and optimizing — and that's exactly where interviewers separate "hire" from "no hire." ⛔

---

### 🗣️ The Interview Script (what to literally say)

Memorize this skeleton. It's your safety rope when nerves hit. 🪢

**1. After hearing the problem — restate it:**
> *"Okay, so to make sure I understand — I'm given `<input>`, and I need to return `<output>`. Is that right?"*

**2. Clarify (ask 2–4 of these):**
> *"A few clarifying questions before I dive in..."*
> - *"Can the input be empty or null?"*
> - *"Are there duplicates? Are the values sorted?"*
> - *"What's the expected input size — hundreds, or millions?"*
> - *"If there are multiple valid answers, do you want any one, or a specific one?"*
> - *"Can I assume the input fits in memory / in an `int`?"*

**3. Walk an example:**
> *"Let me work through a quick example to lock in my understanding. If the input is `[2,7,11]` and target `9`, I'd expect `[0,1]` because `2+7=9`. Let me also think about an edge case — what if the array is empty? I'll return an empty result."*

**4. Brute force first:**
> *"The most straightforward approach that definitely works is `<brute force>`. That'd be O(n²) time. It's not optimal, but it gives us a correct baseline — let me see if I can do better before coding."*

**5. Optimize:**
> *"I notice `<observation>`. That suggests a `<HashMap / two pointers / sliding window>` approach, which should get us to O(n). The idea is `<one sentence>`. Does that approach sound reasonable to you?"*  ← *(pause, let them nod — this is a checkpoint)*

**6. Code (narrate):**
> *"I'll start by setting up `<data structure>`. Then I'll iterate and `<...>`. I'm using a `HashMap` here so lookups are O(1)..."*

**7. Test:**
> *"Let me trace through my normal example... and now the edge case... yep, that returns the right thing."*

**8. Recap:**
> *"So final complexity is O(n) time, O(n) space. If you'd like, I can talk about how this changes if `<constraint>`."*

---

### 🧭 The UMPIRE Framework (your mental checklist)

When your mind goes blank, walk these 6 letters in order. It's the GPS for any problem. 🗺️

| Letter | Meaning | Question to ask yourself |
|--------|---------|--------------------------|
| **U** | **Understand** | What are the inputs, outputs, and constraints? Restate it. |
| **M** | **Match** | What category is this? (Sorting? Graph? DP? See the cheat sheet below.) |
| **P** | **Plan** | Sketch the algorithm in plain English / pseudocode *before* Java. |
| **I** | **Implement** | Translate the plan into clean Java, narrating. |
| **R** | **Review** | Dry-run on examples. Hunt for off-by-one & null bugs. |
| **E** | **Evaluate** | State time & space complexity. Discuss trade-offs & follow-ups. |

> 🧠 **Analogy:** UMPIRE is like building IKEA furniture. **U**nderstand = read what you're building. **M**atch = "oh, this is a bookshelf, I've built shelves before." **P**lan = lay out all the parts. **I**mplement = screw it together. **R**eview = wiggle it to make sure it's sturdy. **E**valuate = "it holds 20 books, took 30 minutes." Skip steps and you end up with a wobbly shelf and 3 leftover screws. 🪑

---

### 🤔 How to Think Out Loud (the skill that gets you hired)

The interviewer cannot read your mind. **Silence = a black box, and black boxes are scary to bet a job on.** Your job is to narrate the movie playing in your head.

✅ **Do say things like:**
- *"My first instinct is brute force, but let me see if the sorted-ness helps..."*
- *"I'm choosing a HashMap here because I need O(1) lookups."*
- *"Hmm, this fails when the list is empty — let me add a guard for that."*
- *"I could do this recursively, but I'm worried about stack depth, so I'll go iterative."*

❌ **Don't:**
- Go silent for 2 minutes while you scribble. (Interviewer thinks you're lost.)
- Mumble *"this is easy"* then freeze. (Overconfidence + freeze = worst combo.)
- Write code that disagrees with what you said out loud. (Confusing — narrate the *real* plan.)

> 💡 **Pro tip:** If you need silent thinking time, *say so*: *"Give me 30 seconds to think through the edge cases."* That's professional. Silently staring is not.

---

### 🏆 What Interviewers Score You On

You are NOT scored only on "did the code work." Microsoft rubrics usually rate **four** things:

| Dimension | What they're watching for | How to win it |
|-----------|---------------------------|---------------|
| 🧩 **Problem Solving** | Did you find a good approach? Did you optimize? | Always state brute force → then optimize. Show the journey. |
| 💻 **Coding** | Is the code clean, correct, idiomatic? | Good names, small helpers, handle edge cases, no global mess. |
| 📡 **Communication** | Did you think out loud? Take hints well? | Narrate constantly. Treat hints as gifts, not failures. |
| 🧪 **Verification** | Did you test your own code? | Dry-run before they ask. Find your own bugs first. |

> 🎯 **The big secret:** A candidate who reaches a *clean O(n log n)* solution while communicating beautifully often **beats** a silent candidate who reaches the *perfect O(n)* solution. Communication is half the score. Talk!

---

# PART A — Worked Solutions 📚

Below is every problem from the 5 mock sets, solved the way you'd solve it *live*. For each: the question, clarifying questions, brute force, optimal, a dry-run, and a one-line pattern to burn into memory.

---

## 🟦 Mock 1, Problem 1: Merge Intervals

### 📋 Full Question
Given an array of intervals where `intervals[i] = [start_i, end_i]`, merge all overlapping intervals and return an array of the non-overlapping intervals that cover all the input intervals.
**Example:** `[[1,3],[2,6],[8,10],[15,18]]` → `[[1,6],[8,10],[15,18]]` (because `[1,3]` and `[2,6]` overlap).

### 🗣️ Clarifying Questions
- *"Are the intervals sorted by start time?"* (Usually no — assume not.)
- *"Can intervals be empty? Can a single interval like `[5,5]` appear?"*
- *"Is touching considered overlapping — e.g., do `[1,4]` and `[4,5]` merge into `[1,5]`?"* (Standard answer: yes, touching merges.)

### 🐢 Brute Force
Repeatedly scan the list and merge any pair that overlaps, restarting until no merges happen. That's roughly **O(n²)** (or worse) and fiddly. Mention it, then immediately pivot to the sorted approach.

### 🚀 Optimal — Sort, then Sweep
**Key insight:** if you sort by start time, any interval that overlaps the current one must come *right after* it. So you sweep left to right and either extend the last interval or start a new one.

```java
import java.util.*;

public class MergeIntervals {
    public int[][] merge(int[][] intervals) {
        if (intervals == null || intervals.length <= 1) return intervals;

        // Sort by start time so overlaps are adjacent.
        Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));

        List<int[]> merged = new ArrayList<>();
        int[] current = intervals[0];
        merged.add(current);

        for (int i = 1; i < intervals.length; i++) {
            int[] next = intervals[i];
            if (next[0] <= current[1]) {
                // Overlap (or touch): extend the end of the current interval.
                current[1] = Math.max(current[1], next[1]);
            } else {
                // No overlap: start a fresh interval.
                current = next;
                merged.add(current);
            }
        }
        return merged.toArray(new int[merged.size()][]);
    }
}
```

### 🔍 Dry-Run on `[[1,3],[2,6],[8,10],[15,18]]`
1. Sort → `[[1,3],[2,6],[8,10],[15,18]]` (already sorted).
2. `current = [1,3]`, add it.
3. `next=[2,6]`: `2 <= 3` → overlap → `current[1] = max(3,6) = 6` → `current=[1,6]`.
4. `next=[8,10]`: `8 <= 6`? No → new interval → add `[8,10]`.
5. `next=[15,18]`: `15 <= 10`? No → new interval → add `[15,18]`.
6. Result: `[[1,6],[8,10],[15,18]]` ✅

### ⏱️ Complexity
- **Time:** O(n log n) — dominated by the sort.
- **Space:** O(n) for the output (O(log n) auxiliary for the sort).

### 🧠 Pattern to Remember
> **"Merge / overlapping intervals → SORT by start, then sweep & extend."** Sorting turns a tangled mess into a neat left-to-right scan.

---

## 🟦 Mock 1, Problem 2: Validate Binary Search Tree

### 📋 Full Question
Given the root of a binary tree, determine if it is a valid Binary Search Tree (BST). A valid BST: every node's left subtree has values **strictly less**, right subtree has values **strictly greater**, and both subtrees are themselves BSTs.

### 🗣️ Clarifying Questions
- *"Are duplicate values allowed? If a node equals an ancestor, is it valid?"* (Standard: duplicates make it invalid — strict inequality.)
- *"Could the tree be empty?"* (Yes → an empty tree is a valid BST.)
- *"Can values be very large/negative? Should I worry about `Integer.MIN_VALUE`/`MAX_VALUE`?"* (Yes — use `Long` or `Integer` object bounds to be safe.)

### 🐢 Brute Force (the classic trap ⚠️)
The tempting wrong answer: "check `node.left.val < node.val < node.right.val` at each node." **This is buggy** — it only checks direct children, missing violations deeper in the tree (e.g., a deep-left node larger than the root). Mention this trap, then fix it with bounds.

### 🚀 Optimal — Recurse with (min, max) Bounds
**Key insight:** each node must fall within an allowed `(low, high)` range that *narrows* as you go down. Going left tightens the upper bound; going right tightens the lower bound.

```java
public class ValidateBST {
    // TreeNode is the standard: int val; TreeNode left, right;
    public boolean isValidBST(TreeNode root) {
        return validate(root, null, null);
    }

    // low/high are Long objects so null means "no bound yet".
    private boolean validate(TreeNode node, Long low, Long high) {
        if (node == null) return true; // empty subtree is valid

        if (low != null && node.val <= low) return false;  // must be > low
        if (high != null && node.val >= high) return false; // must be < high

        // Left subtree: high bound becomes this node's value.
        // Right subtree: low bound becomes this node's value.
        return validate(node.left, low, (long) node.val)
            && validate(node.right, (long) node.val, high);
    }
}

class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode(int val) { this.val = val; }
}
```

### 🔍 Dry-Run on a tricky tree
```
        5
       / \
      1   6
         / \
        3   7   <-- 3 is in the RIGHT subtree of 5, but 3 < 5 → INVALID
```
- `validate(5, null, null)` → ok.
- `validate(1, null, 5)` → `1 < 5` ✅.
- `validate(6, 5, null)` → `6 > 5` ✅.
- `validate(3, 5, 6)` → `3 <= low(5)` → **return false** ✅ correctly caught!

The naive "children-only" check would *miss* this. That's why bounds win.

### ⏱️ Complexity
- **Time:** O(n) — visit each node once.
- **Space:** O(h) recursion stack, where `h` = tree height (O(n) worst case skewed, O(log n) balanced).

### 🧠 Pattern to Remember
> **"Validate BST → pass DOWN a (min, max) range; left narrows max, right narrows min."** Alternative: an **in-order traversal of a BST is strictly increasing** — check that instead.

---

## 🟩 Mock 2, Problem 1: LRU Cache

### 📋 Full Question
Design a data structure for a **Least Recently Used (LRU) cache** with capacity `c`. Support:
- `get(key)` → return value if present (and mark it most-recently-used), else `-1`.
- `put(key, value)` → insert/update; if over capacity, **evict the least-recently-used** item.
Both operations must run in **O(1)** average time.

### 🗣️ Clarifying Questions
- *"What's the capacity range — is it always ≥ 1?"*
- *"Does `put` on an existing key count as a 'use' (move it to most-recent)?"* (Yes.)
- *"Are keys and values both integers?"* (Usually yes for the interview version.)
- *"Can I use built-in `LinkedHashMap`, or do you want me to build it from scratch?"* ← **Always ask this.** If they allow it, the answer is 10 lines. If not, build the HashMap + doubly-linked list yourself (shown below — this is what they usually want).

### 🐢 Brute Force
Use an `ArrayList` ordered by recency. `get`/`put` scan the list to find/move items → **O(n)** per op. Violates the O(1) requirement — so we need a smarter structure.

### 🚀 Optimal — HashMap + Doubly-Linked List
**Key insight:** we need two superpowers at once: **O(1) lookup** (→ HashMap) and **O(1) reordering** (→ doubly-linked list). Combine them. The HashMap maps `key → node`; the linked list keeps recency order, with the *head* = most-recent and *tail* = least-recent. Dummy head/tail sentinels avoid null checks. 🛡️

```java
import java.util.*;

public class LRUCache {
    // Doubly-linked node holding key+value (key needed to evict from the map).
    private static class Node {
        int key, value;
        Node prev, next;
        Node(int key, int value) { this.key = key; this.value = value; }
    }

    private final int capacity;
    private final Map<Integer, Node> map = new HashMap<>();
    private final Node head = new Node(0, 0); // dummy: head.next = most recent
    private final Node tail = new Node(0, 0); // dummy: tail.prev = least recent

    public LRUCache(int capacity) {
        this.capacity = capacity;
        head.next = tail;
        tail.prev = head;
    }

    public int get(int key) {
        Node node = map.get(key);
        if (node == null) return -1;
        moveToFront(node); // accessing it makes it most-recent
        return node.value;
    }

    public void put(int key, int value) {
        Node node = map.get(key);
        if (node != null) {
            node.value = value;
            moveToFront(node);
            return;
        }
        if (map.size() == capacity) {
            Node lru = tail.prev;   // least-recently-used is just before tail
            remove(lru);
            map.remove(lru.key);
        }
        Node fresh = new Node(key, value);
        map.put(key, fresh);
        addToFront(fresh);
    }

    // --- linked-list helpers (all O(1)) ---
    private void addToFront(Node node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    private void remove(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void moveToFront(Node node) {
        remove(node);
        addToFront(node);
    }
}
```

### 🔍 Dry-Run (capacity = 2)
1. `put(1,1)` → list: `[1]`
2. `put(2,2)` → list: `[2,1]` (2 is newest)
3. `get(1)` → returns `1`, move 1 to front → `[1,2]`
4. `put(3,3)` → at capacity → evict LRU = `2` → `[3,1]`
5. `get(2)` → returns `-1` (evicted) ✅
6. `put(4,4)` → evict LRU = `1` → `[4,3]`
7. `get(1)` → `-1`, `get(3)` → `3`, `get(4)` → `4` ✅

### ⏱️ Complexity
- **Time:** O(1) for both `get` and `put`.
- **Space:** O(capacity).

### 🧠 Pattern to Remember
> **"O(1) cache / 'most-recently-used' → HashMap (lookup) + Doubly-Linked List (order)."** HashMap finds it fast; linked list re-orders it fast. Use dummy head/tail sentinels to kill null checks.

---

## 🟩 Mock 2, Problem 2: Coin Change

### 📋 Full Question
Given coins of different denominations and a total `amount`, return the **fewest number of coins** needed to make up that amount. If it's impossible, return `-1`. You have an unlimited supply of each coin.
**Example:** `coins = [1,2,5]`, `amount = 11` → `3` (because `5 + 5 + 1`).

### 🗣️ Clarifying Questions
- *"Can a coin value be 0 or negative?"* (No — positive denominations.)
- *"Is `amount = 0` valid? What do I return?"* (Return `0` — zero coins needed.)
- *"Unlimited coins of each type, right?"* (Yes — that's why it's *unbounded* knapsack.)

### 🐢 Brute Force — Greedy (and why it FAILS ⚠️)
Tempting: always grab the biggest coin ≤ remaining. **But greedy is wrong here!** For `coins=[1,3,4]`, `amount=6`: greedy gives `4+1+1 = 3 coins`, but optimal is `3+3 = 2 coins`. **Say this out loud** — recognizing that greedy fails is a strong signal you understand the problem. This is why we need DP.

### 🚀 Optimal — Bottom-Up DP
**Key insight:** `dp[a]` = fewest coins to make amount `a`. To make `a`, try each coin `c`: if I use coin `c`, the rest is `dp[a-c]`, so `dp[a] = min(dp[a], dp[a-c] + 1)`. Build up from `0` to `amount`.

```java
import java.util.*;

public class CoinChange {
    public int coinChange(int[] coins, int amount) {
        // dp[a] = min coins to make amount a. Init to "infinity" (amount+1).
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, amount + 1); // amount+1 is unreachable → acts as infinity
        dp[0] = 0; // zero coins to make amount 0

        for (int a = 1; a <= amount; a++) {
            for (int coin : coins) {
                if (coin <= a) {
                    dp[a] = Math.min(dp[a], dp[a - coin] + 1);
                }
            }
        }
        // If still "infinity", amount was impossible.
        return dp[amount] > amount ? -1 : dp[amount];
    }
}
```

### 🔍 Dry-Run on `coins=[1,2,5]`, `amount=11`
| amount | best combo | dp value |
|--------|-----------|----------|
| 0 | — | 0 |
| 1 | 1 | 1 |
| 2 | 2 | 1 |
| 3 | 2+1 | 2 |
| 4 | 2+2 | 2 |
| 5 | 5 | 1 |
| 6 | 5+1 | 2 |
| ... | ... | ... |
| 11 | 5+5+1 | **3** ✅ |

### ⏱️ Complexity
- **Time:** O(amount × numberOfCoins).
- **Space:** O(amount) for the `dp` array.

### 🧠 Pattern to Remember
> **"Fewest/most ways to reach a target with reusable items → Unbounded Knapsack DP."** `dp[a] = min(dp[a], dp[a-coin]+1)`. And remember: **greedy fails on coin change** — always DP.

---

## 🟪 Mock 3, Problem 1: Number of Islands

### 📋 Full Question
Given a 2D grid of `'1'`s (land) and `'0'`s (water), count the number of islands. An island is land connected **horizontally or vertically** (not diagonally), surrounded by water.
**Example:** a grid with two separate land clusters → `2`.

### 🗣️ Clarifying Questions
- *"Are diagonal connections counted?"* (Standard: **No** — only up/down/left/right.)
- *"Can I modify the input grid?"* (Usually yes — we 'sink' visited land to `'0'`. If not, use a separate `visited` boolean grid.)
- *"What are the cell values — chars `'1'`/`'0'` or ints?"* (Classic LeetCode uses `char`.)

### 🐢 Brute Force
There isn't really a meaningfully different "brute force" — graph traversal *is* the natural solution. You can mention Union-Find as an alternative, but DFS/BFS flood-fill is the expected answer.

### 🚀 Optimal — DFS Flood Fill
**Key insight:** scan every cell. When you hit unvisited land (`'1'`), that's a **new island** — increment the count, then **flood-fill** (DFS) the entire connected landmass, marking each cell visited (`'0'`) so you never recount it. It's like dropping ink on a continent and watching it spread to fill the whole landmass. 🌊

```java
public class NumberOfIslands {
    public int numIslands(char[][] grid) {
        if (grid == null || grid.length == 0) return 0;
        int count = 0;
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[0].length; c++) {
                if (grid[r][c] == '1') {
                    count++;            // found a new island
                    sink(grid, r, c);   // flood-fill to remove the whole landmass
                }
            }
        }
        return count;
    }

    // DFS that turns all connected '1's into '0's.
    private void sink(char[][] grid, int r, int c) {
        if (r < 0 || c < 0 || r >= grid.length || c >= grid[0].length
                || grid[r][c] == '0') {
            return; // out of bounds or water → stop
        }
        grid[r][c] = '0';          // mark as visited
        sink(grid, r + 1, c);      // down
        sink(grid, r - 1, c);      // up
        sink(grid, r, c + 1);      // right
        sink(grid, r, c - 1);      // left
    }
}
```

### 🔍 Dry-Run
```
1 1 0 0
1 0 0 1
0 0 0 1
```
- `(0,0)` is `'1'` → count=1 → sink fills `(0,0),(0,1),(1,0)` → all become `'0'`.
- Continue scanning... `(1,3)` is `'1'` → count=2 → sink fills `(1,3),(2,3)`.
- No more land. **Answer: 2** ✅

### ⏱️ Complexity
- **Time:** O(rows × cols) — every cell visited at most once.
- **Space:** O(rows × cols) worst case for the recursion stack (a grid that's all land).

### 🧠 Pattern to Remember
> **"Count connected regions in a grid → DFS/BFS flood fill, marking visited as you go."** Each new unvisited land cell = one new island; the DFS erases its whole landmass so you never double-count.

---

## 🟪 Mock 3, Problem 2: Minimum Window Substring

### 📋 Full Question
Given strings `s` and `t`, return the **smallest substring of `s`** that contains every character of `t` (including duplicates). If no such window exists, return `""`.
**Example:** `s = "ADOBECODEBANC"`, `t = "ABC"` → `"BANC"`.

### 🗣️ Clarifying Questions
- *"Is the answer guaranteed unique, or do I return any minimal window?"* (Return any minimal one.)
- *"Do duplicate chars in `t` matter — if `t="AAB"`, do I need two A's?"* (Yes — counts matter.)
- *"Case-sensitive?"* (Usually yes — `'a'` ≠ `'A'`.)
- *"What if `t` is longer than `s`?"* (Then return `""`.)

### 🐢 Brute Force
Generate every substring of `s` (O(n²) of them), and for each check if it contains all of `t` (O(n) check) → **O(n³)**. Correct but far too slow. State it, then pivot to sliding window.

### 🚀 Optimal — Sliding Window with Two Pointers
**Key insight:** expand the right edge to *include* characters until the window is "valid" (contains all of `t`), then shrink from the left to make it as small as possible, recording the best. The window grows and shrinks like an inchworm. 🐛

```java
import java.util.*;

public class MinimumWindowSubstring {
    public String minWindow(String s, String t) {
        if (s == null || t == null || s.length() < t.length() || t.isEmpty()) {
            return "";
        }

        // need[c] = how many of char c we still must include.
        int[] need = new int[128];
        for (char c : t.toCharArray()) need[c]++;
        int required = t.length(); // total chars still needed (counts duplicates)

        int left = 0, bestLen = Integer.MAX_VALUE, bestStart = 0;

        for (int right = 0; right < s.length(); right++) {
            char rc = s.charAt(right);
            // If this char was still needed, consuming it reduces 'required'.
            if (need[rc] > 0) required--;
            need[rc]--; // may go negative for surplus chars (that's fine)

            // Window is valid → try to shrink from the left.
            while (required == 0) {
                if (right - left + 1 < bestLen) {
                    bestLen = right - left + 1;
                    bestStart = left;
                }
                char lc = s.charAt(left);
                need[lc]++;                 // we're about to drop this char
                if (need[lc] > 0) required++; // we now need it again → window breaks
                left++;
            }
        }
        return bestLen == Integer.MAX_VALUE ? "" : s.substring(bestStart, bestStart + bestLen);
    }
}
```

### 🔍 Dry-Run on `s="ADOBECODEBANC"`, `t="ABC"`
- Expand right until window `"ADOBEC"` contains A, B, C → valid (required hits 0).
- Shrink left: `"DOBEC"` still has all? It lost A → invalid → stop shrinking. Best so far = `"ADOBEC"` (len 6).
- Keep expanding/shrinking... eventually window `"BANC"` (len 4) becomes the smallest valid window.
- **Answer: `"BANC"`** ✅

### ⏱️ Complexity
- **Time:** O(|s| + |t|) — each character of `s` is visited at most twice (once by right, once by left).
- **Space:** O(1) — the `need` array is fixed at 128 (ASCII).

### 🧠 Pattern to Remember
> **"Smallest/longest substring satisfying a condition → Sliding Window + frequency count."** Right pointer *expands* to satisfy, left pointer *shrinks* to optimize. A `required` counter tells you instantly when the window is valid.

---

## 🟥 Mock 4, Problem 1: Trapping Rain Water

### 📋 Full Question
Given `n` non-negative integers representing an elevation map where each bar has width 1, compute how much **rain water** it can trap after raining.
**Example:** `[0,1,0,2,1,0,1,3,2,1,2,1]` → `6` units of water.

### 🗣️ Clarifying Questions
- *"Can heights be 0? Can the array be empty?"* (Yes to both — empty → 0 water.)
- *"Can values be very large?"* (Could overflow `int` if extreme — usually fine, but worth noting.)
- *"Is the width of each bar exactly 1?"* (Yes — standard.)

### 🐢 Brute Force
For each bar, the water on top = `min(maxLeft, maxRight) - height[i]`. The brute force re-scans left and right for *every* index → **O(n²)** time, **O(1)** space. State it — it's the clearest way to express the core idea — then optimize the repeated scanning away.

```java
// Brute force — O(n^2). Good to state, not to submit.
public int trapBrute(int[] height) {
    int total = 0;
    for (int i = 0; i < height.length; i++) {
        int maxLeft = 0, maxRight = 0;
        for (int l = 0; l <= i; l++) maxLeft = Math.max(maxLeft, height[l]);
        for (int r = i; r < height.length; r++) maxRight = Math.max(maxRight, height[r]);
        total += Math.min(maxLeft, maxRight) - height[i];
    }
    return total;
}
```

### 🚀 Optimal — Two Pointers
**Key insight:** water above a bar depends on `min(maxLeft, maxRight)`. Use two pointers from both ends. The side with the *smaller* wall is the bottleneck, so we can safely process that side — we *know* the water level there is set by its own running max. Move inward from the shorter side. 🧱

```java
public class TrappingRainWater {
    public int trap(int[] height) {
        if (height == null || height.length < 3) return 0;

        int left = 0, right = height.length - 1;
        int leftMax = 0, rightMax = 0;
        int water = 0;

        while (left < right) {
            if (height[left] < height[right]) {
                // Left wall is the limiter → process left.
                if (height[left] >= leftMax) {
                    leftMax = height[left];           // new wall, traps nothing
                } else {
                    water += leftMax - height[left];  // trapped above this bar
                }
                left++;
            } else {
                // Right wall is the limiter → process right.
                if (height[right] >= rightMax) {
                    rightMax = height[right];
                } else {
                    water += rightMax - height[right];
                }
                right--;
            }
        }
        return water;
    }
}
```

### 🔍 Dry-Run (intuition) on `[0,1,0,2,1,0,1,3,2,1,2,1]`
- The `3` at index 7 is the tallest wall. Water pools in the dips to its left and right where the lower surrounding walls hold it.
- The two-pointer sweep accumulates: the dips around indices 2, 4, 5, 6, 8–10 add up to **6** ✅.

### ⏱️ Complexity
- **Time:** O(n) — single pass, each pointer moves inward.
- **Space:** O(1) — just a few variables.

### 🧠 Pattern to Remember
> **"Water/area trapped between bars → Two Pointers tracking leftMax & rightMax, move the shorter side."** The shorter wall is always the bottleneck, so its running max is safe to use.

---

## 🟥 Mock 4, Problem 2: Serialize and Deserialize Binary Tree

### 📋 Full Question
Design an algorithm to **serialize** a binary tree to a string, and **deserialize** that string back into the exact same tree. (Serialization = encoding a structure into a transmittable/storable format.)

### 🗣️ Clarifying Questions
- *"Can node values be negative or multi-digit?"* (Yes — so use a delimiter like `,`.)
- *"Is the format up to me, as long as serialize→deserialize round-trips?"* (Yes.)
- *"How do I represent null children?"* (Pick a sentinel like `"#"` or `"null"`.)

### 🐢 Brute Force / Naive
Storing *only* an in-order traversal **can't** reconstruct the tree uniquely (many trees share an in-order). You need structure info — so we encode nulls explicitly. Mention this so the interviewer sees you understand *why* nulls must be stored.

### 🚀 Optimal — Pre-order DFS with Null Markers
**Key insight:** a pre-order traversal (`root, left, right`) that records `#` for every null child **uniquely** determines the tree. To rebuild, consume tokens left-to-right in the same pre-order, recursing — the `#` tokens tell you exactly where subtrees end. 🌳

```java
import java.util.*;

public class Codec {
    private static final String NULL = "#";
    private static final String SEP = ",";

    // --- Serialize: pre-order DFS into a comma-separated string ---
    public String serialize(TreeNode root) {
        StringBuilder sb = new StringBuilder();
        buildString(root, sb);
        return sb.toString();
    }

    private void buildString(TreeNode node, StringBuilder sb) {
        if (node == null) {
            sb.append(NULL).append(SEP);
            return;
        }
        sb.append(node.val).append(SEP); // root
        buildString(node.left, sb);      // left
        buildString(node.right, sb);     // right
    }

    // --- Deserialize: rebuild in the same pre-order using a queue of tokens ---
    public TreeNode deserialize(String data) {
        Queue<String> tokens = new LinkedList<>(Arrays.asList(data.split(SEP)));
        return buildTree(tokens);
    }

    private TreeNode buildTree(Queue<String> tokens) {
        String token = tokens.poll();
        if (token.equals(NULL)) return null;
        TreeNode node = new TreeNode(Integer.parseInt(token));
        node.left = buildTree(tokens);   // build left first (pre-order)
        node.right = buildTree(tokens);  // then right
        return node;
    }
}

class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode(int val) { this.val = val; }
}
```

### 🔍 Dry-Run on
```
    1
   / \
  2   3
     / \
    4   5
```
- **Serialize** (pre-order with nulls): `"1,2,#,#,3,4,#,#,5,#,#,"`
- **Deserialize:** read `1` → root. Read `2` → left child; its children are `#,#` (nulls). Back up, read `3` → right child; read `4` (its left), `#,#`, then `5` (its right), `#,#`. Tree rebuilt identically ✅.

### ⏱️ Complexity
- **Time:** O(n) for both serialize and deserialize — each node touched once.
- **Space:** O(n) for the string and the recursion stack.

### 🧠 Pattern to Remember
> **"Serialize/Deserialize a tree → Pre-order DFS + explicit null markers (`#`)."** Nulls are what make the encoding unique. Deserialize by consuming tokens in the *same* pre-order.

---

## 🟨 Mock 5: Full Microsoft Simulation

Mock 5 pulls **2 random problems from Weeks 1–8** plus a **System Design** question. Since the specific picks are randomized, here's how to attack each slot — plus a fully worked "random" warm-up so you're not caught off guard.

### 🎲 Slots 1 & 2 — Random Weeks 1–8 (20 min each)
Treat these exactly like Mocks 1–4: **UMPIRE every time**, no matter how "easy" it looks. The Pattern Cheat Sheet below is your decoder ring — match the trigger phrase to the algorithm in seconds.

#### 🔥 Worked warm-up: Two Sum (the canonical Week-1 problem)
**Question:** Given `nums` and a `target`, return indices of the two numbers that add to `target`. Exactly one solution; can't reuse an element.

🗣️ *Clarify:* "Exactly one answer guaranteed? Same element reused? Sorted?"

🐢 *Brute force:* check all pairs → O(n²).

🚀 *Optimal — HashMap complement trick:* for each number, ask "have I already seen `target - num`?" If yes, done.

```java
import java.util.*;

public class TwoSum {
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> seen = new HashMap<>(); // value -> index
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            if (seen.containsKey(complement)) {
                return new int[] { seen.get(complement), i };
            }
            seen.put(nums[i], i);
        }
        return new int[] {}; // no pair (won't happen if guaranteed)
    }
}
```
⏱️ O(n) time, O(n) space.
🧠 **"Find a pair summing to X → HashMap of complements."** The hash map remembers what you've seen so you never re-scan.

### 🏗️ Slot 3 — System Design (35 min)
A coding-loop system design question is *lighter* than a senior SD interview, but you still follow a flow. Use this **5-step script**:

| Step | What to do | Say this |
|------|-----------|----------|
| 1️⃣ **Requirements** | Clarify functional + scale | *"Let me nail down requirements. What's the read/write ratio? Roughly how many users?"* |
| 2️⃣ **Estimate** | Back-of-envelope QPS, storage | *"At 1M daily users and ~10 requests each, that's ~115 QPS average, maybe 5× at peak."* |
| 3️⃣ **High-level design** | Draw boxes: client → LB → API → DB → cache | *"Client hits a load balancer, then stateless app servers, with a cache in front of the database."* |
| 4️⃣ **Deep dive** | Pick the hard part (data model, sharding, consistency) | *"The tricky part is X — let me design the schema and how we scale it."* |
| 5️⃣ **Bottlenecks & trade-offs** | Caching, replication, CAP trade-offs | *"To handle read spikes I'd add a Redis cache and read replicas. The trade-off is eventual consistency."* |

> 🧰 **Mental toolbox** to mention when relevant: **Load Balancer** (spread traffic), **Cache/Redis** (fast reads), **Database sharding** (split big data), **Replication** (availability), **Message Queue/Kafka** (decouple & absorb spikes), **CDN** (static assets), **CAP theorem** (you can't have Consistency *and* Availability under Partition — pick two).
>
> 🗣️ **Communication is even MORE important in SD** — there's no "compiling" to hide behind. Keep narrating, keep drawing, and keep asking "does this design meet the requirement?"

---

# PART B — Common Interview Q&A 💬

The questions interviewers ask *around* the code. Have crisp answers ready.

### 🧮 "What's your complexity?"
State **time AND space**, and **why**:
> *"Time is O(n log n) because the sort dominates the linear sweep. Space is O(n) for the output array, O(log n) auxiliary for the sort's recursion."*

Don't just blurt a Big-O — *justify* it by pointing at the loop/sort/recursion that causes it.

### 🚀 "Can you do better?"
This usually means **yes, and they want you to find it.** Don't panic — it's an invitation, not an accusation.
> *"Let me think... my current bottleneck is the nested loop. If I trade space for time using a HashMap, I can drop the inner loop and get from O(n²) to O(n)."*

The classic upgrades to reach for:
- O(n²) nested loop → **HashMap** → O(n)
- O(n²) on sorted data → **Two Pointers** → O(n)
- O(n) repeated scanning → **Sliding Window** → O(n) single pass
- O(n) search on sorted data → **Binary Search** → O(log n)
- Recomputing subproblems → **DP / memoization**

### 🧨 "What about edge cases?"
Always proactively list these (don't wait to be asked):
- 📭 **Empty / null** input → guard at the top.
- 1️⃣ **Single element** → does your loop/logic still work?
- 🔁 **Duplicates** → does the problem care?
- ➖ **Negatives / zero** → especially in DP, sums, indices.
- 🔢 **Overflow** → very large sums/products → consider `long`.
- 🎯 **All-same or already-sorted** input → boundary behavior.

> *"Before I say I'm done, let me check edge cases: empty array — handled by my guard. Single element — returns it directly. Duplicates — my HashMap handles them. Looks solid."*

### 🆘 "How do I handle being STUCK?" (the most important skill)
Getting stuck is **normal** — interviewers expect it. What they score is *how you behave* when stuck. Do this:

1. 🗣️ **Narrate the stuck-ness:** *"I'm trying to figure out how to avoid the O(n²) scan here..."* Silence is the enemy.
2. 🐢 **Fall back to brute force:** A working slow solution beats a broken fast one. Code it, get partial credit, *then* optimize.
3. ✍️ **Work a small example by hand:** Patterns often jump out when you trace `[1,2,3]` manually.
4. 🔍 **Match to a pattern:** "Is this sorted? → maybe binary search. Substring? → maybe sliding window." (Use the cheat sheet!)
5. 🤝 **Ask for a hint gracefully** (see below).

> 🧠 **Reframe:** Interviewers aren't looking for someone who's never stuck. They're looking for someone they'd want *next to them when production breaks at 2am.* Calm + methodical when stuck = exactly that person.

### 🙋 "How do I ask for a HINT without looking weak?"
Asking well is a *strength* signal — it shows collaboration. Frame it as **sharing your thinking and inviting input**, not surrendering:

✅ *"Here's where I am: I've got an O(n²) approach working. I suspect a HashMap could remove the inner loop, but I'm not certain it covers the duplicate case. Am I on the right track, or is there a direction you'd nudge me toward?"*

❌ *"I don't know. Can you tell me the answer?"* (passive, gives them nothing to work with)

The difference: the good version shows your reasoning *and* a specific question. You're treating the interviewer as a teammate. 🤝

### 🎭 Behavioral Questions (the "tell me about a time" round)

Microsoft cares about its leadership-principle-style values. Use the **STAR** method: **S**ituation → **T**ask → **A**ction → **R**esult.

| Question | What they're really checking | STAR angle |
|----------|------------------------------|------------|
| *"Tell me about a challenging bug you fixed."* | Persistence, debugging skill | Pick a real bug. Emphasize your *systematic* process and the *measurable* result. |
| *"A time you disagreed with a teammate?"* | Collaboration, ego management | Show you listened, found data, reached a *shared* decision. |
| *"A project you're proud of?"* | Ownership, impact | Quantify the impact ("cut load time 40%"). |
| *"A time you failed / missed a deadline?"* | Self-awareness, growth | Own it honestly, focus on what you *learned* and changed after. |
| *"Why Microsoft?"* | Genuine interest | Tie to specific products/teams/values, not generic "big company." |

> 🌟 **STAR tip:** Spend ~70% of your answer on **Action** (what *you* specifically did) and **Result** (the measurable outcome). Don't ramble on Situation. And always say "**I**" not just "we" — they're hiring *you*.

---

# 📊 Pattern Recognition Cheat Sheet (Weeks 1–9 Master Table)

This is your **decoder ring** 🔑. In the first 60 seconds, match the problem's words to a row → you instantly know the tool and the complexity. **Memorize the "Trigger Phrase" column — it's how you diagnose a problem on sight.**

| 🎯 Trigger Phrase in the Problem | 🛠️ Pattern / Algorithm | ⏱️ Typical Big-O | 💡 Quick Tell |
|----------------------------------|------------------------|------------------|---------------|
| "find a pair / two numbers that sum to X", "have I seen this before?", "count occurrences" | **HashMap / HashSet** | O(n) time, O(n) space | Need O(1) lookups or frequency counts |
| "sorted array", "pair/triplet summing to target", "remove duplicates in place", "trapping water" | **Two Pointers** | O(n) time, O(1) space | Two indices moving toward/with each other |
| "longest/shortest/smallest substring or subarray with condition", "contiguous window", "max sum of size k" | **Sliding Window** | O(n) time, O(1)–O(k) space | A contiguous range that grows/shrinks |
| "sorted input + search", "find in O(log n)", "first/last position", "minimum that satisfies / search answer space" | **Binary Search** | O(log n) time | Sorted data, or a monotonic yes/no answer space |
| "tree/graph", "connected components", "shortest path (unweighted)", "level order", "flood fill", "islands" | **BFS / DFS** | O(V + E) time | Explore nodes/cells; BFS for shortest, DFS for "fill all" |
| "number of ways", "min/max cost to reach", "can you make X", "fewest coins", "overlapping subproblems" | **Dynamic Programming** | O(n·m) typical | Optimal substructure + repeated subproblems |
| "all combinations / permutations / subsets", "generate every valid arrangement", "N-Queens / Sudoku" | **Backtracking** | O(2ⁿ) / O(n!) | Build candidates, undo (backtrack) on dead ends |
| "maximize/minimize by always taking the best local choice", "intervals", "scheduling", "fewest items" | **Greedy** | O(n log n) often | A locally-optimal choice proves globally optimal (verify it does!) |
| "top K", "K largest/smallest", "merge K sorted", "running median" | **Heap / Priority Queue** | O(n log k) | "K-th" or "top K" → heap |
| "next greater element", "valid parentheses", "monotonic", "evaluate expression" | **Stack (often monotonic)** | O(n) | Match/undo most-recent, or track increasing/decreasing |
| "detect cycle in list", "find middle", "k-th from end" | **Fast & Slow Pointers** | O(n) time, O(1) space | One pointer moves 2×, the other 1× |
| "merge overlapping intervals", "meeting rooms", "insert interval" | **Sort + Interval Sweep** | O(n log n) | Sort by start, then sweep & merge |

> ⚡ **How to use it live:** Read the problem → underline the trigger words → glance at this table mentally → announce *"This looks like a sliding-window problem because we want the smallest contiguous substring meeting a condition."* That single sentence tells the interviewer you've **diagnosed** the problem, not just memorized solutions. 🩺

---

# ✅ Final Pre-Interview Checklist

### 🌙 The Night Before
- [ ] 😴 **Sleep 8 hours.** A rested brain beats one extra LeetCode problem. Seriously — this is the highest-ROI prep there is.
- [ ] 🔁 **Re-read this file's Cheat Sheet and the Interview Script**, not 20 new problems. Reinforce, don't cram.
- [ ] 🧪 **Verify your setup:** laptop charged, IDE/CoderPad working, camera & mic tested, quiet room booked.
- [ ] 📶 **Check internet** + have a phone hotspot backup ready.
- [ ] 💧 **Water + snack** nearby. Bathroom break before you start.
- [ ] 🎯 **Prepare 2–3 questions to ask THEM** ("What does a typical day look like?", "What's the team working on?"). Curiosity = signal of genuine interest.
- [ ] 🧘 **Prep 2 STAR stories** (a hard bug, a teammate disagreement) so behavioral questions don't catch you cold.
- [ ] 🛑 **Stop studying by 9pm.** Last-minute panic-cramming lowers next-day performance.

### ⏰ The First 5 Minutes (in the interview)
- [ ] 🤝 **Smile and breathe.** The interviewer is a future teammate, not a judge. They *want* you to do well.
- [ ] 👂 **Listen to the whole problem** before reacting. Don't interrupt with a solution.
- [ ] 🔁 **Restate the problem** in your own words to confirm understanding.
- [ ] ❓ **Ask 2–4 clarifying questions** (input size, empty/null, duplicates, sorted?). **Never skip this.**
- [ ] 📝 **Write one normal + one edge example** by hand.
- [ ] 🐢 **State the brute force + its complexity** out loud before optimizing.
- [ ] 🗣️ **Commit to narrating everything** from here on. Silence is the only real way to fail.

---

## 🎤 Final Pep Talk

You've done 9 weeks of patterns. You **know** this stuff. 💪

The interview isn't a test of whether you can solve problems you've never seen — it's a test of whether you can **stay calm, think out loud, and collaborate** while solving a problem you've *basically* seen before.

Remember the formula:
> **Clarify → Examples → Brute force → Optimize → Code (narrating) → Test → Recap.**

Trust the process, talk through the movie in your head, treat the interviewer as a teammate, and let your 9 weeks of work carry you. 🚀

**You're not hoping to get lucky. You're prepared. Go show them. 🎯**
