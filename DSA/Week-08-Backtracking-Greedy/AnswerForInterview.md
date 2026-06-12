# Week 8: Backtracking & Greedy — How to ANSWER in a Microsoft Interview 🎤 (Java)

Welcome to interview boot camp! 🥾 This week is about two big "decision-making" patterns:
**Backtracking** (try everything, undo the bad ones) and **Greedy** (grab the best-looking option right now). The interviewer doesn't just want a working answer — they want to *watch you think*. This guide teaches you the exact words to say and the exact code to write.

---

## 🏆 The Golden Rule — ALWAYS Follow This Order

No matter what problem you get, narrate your solution in this order:

> **Clarify → Brute Force → Optimal → Best → Complexity**

1. **Clarify** 🔍 — Restate the problem. Ask about edge cases, input ranges, duplicates, empty inputs.
2. **Brute Force** 🐢 — Say the naive idea out loud ("I could try all possibilities..."). This shows you understand the search space.
3. **Optimal** ⚡ — Improve it. For backtracking, prune invalid branches early. For greedy, identify the locally-optimal choice.
4. **Best** 💎 — State the final approach you'll code, and *why* it's correct.
5. **Complexity** 📊 — Give Time AND Space Big-O before AND after coding.

### 🗣️ Interview Script (memorize this opening)

> "Let me make sure I understand the problem... *(restate it)*.
> A few clarifying questions: *(ask 2-3)*.
> The brute-force approach would be *(X)*, which costs *(Big-O)*.
> I think I can do better using **backtracking / greedy** because *(reason)*.
> Let me code that up and walk you through it."

### ❓ Common Clarifying Questions
- Can the input be **empty** or **null**? What should I return?
- Are there **duplicate** values? Should the output contain duplicates?
- Does the **order** of the output matter?
- What's the **range/size** of the input? (Tells you if exponential is acceptable.)
- Are all numbers **positive**? Can they be **zero / negative**?
- Should I return **all** answers, **count** them, or just **true/false**?

---

## 🔁 The Backtracking Template — "Choose → Explore → Un-choose"

Backtracking is **DFS with a memory eraser**. You build a partial solution, recurse, then *undo* your last move so you can try the next option.

```java
void backtrack(List<List<Integer>> result, List<Integer> path, /* state */) {
    if (isComplete(path)) {                 // base case: found a full solution
        result.add(new ArrayList<>(path));  // COPY it (path is reused!)
        return;
    }
    for (int choice : choices) {
        if (!isValid(choice)) continue;     // prune bad branches early
        path.add(choice);                   // 1. CHOOSE
        backtrack(result, path, /* next state */); // 2. EXPLORE
        path.remove(path.size() - 1);       // 3. UN-CHOOSE (the magic!)
    }
}
```

🔑 **The three sacred lines**: `add` → recurse → `remove`. If you forget the `remove`, your `path` gets polluted and every answer is wrong. If you forget to **copy** (`new ArrayList<>(path)`), every result points to the same list that ends up empty.

### 🧭 How to spot GREEDY vs needing DP

| Signal | Use Greedy 🤑 | Use DP 🧮 |
|---|---|---|
| "Can a locally-best choice never hurt me later?" | ✅ Yes → greedy | ❌ No → DP |
| Sorting reveals an obvious "take this first" rule | ✅ Greedy | — |
| Choices interact / earlier choice changes future options | — | ✅ DP |
| You need ALL solutions / combinations / permutations | ❌ that's **backtracking** | — |

**Rule of thumb:** If you can *prove* "the obvious choice now is always safe," it's greedy. If choosing now might trap you later (and you'd need to remember past states), it's DP. If they ask for *every* valid arrangement, it's backtracking.

---

# 🎯 The Problems

---

## 1️⃣ Subsets (Microsoft asked ✅)

### 📋 Full Question
Given an array of **unique** integers `nums`, return **all possible subsets** (the power set). The solution set must not contain duplicate subsets.

**Example:**
`nums = [1, 2, 3]`
**Output:** `[[], [1], [2], [3], [1,2], [1,3], [2,3], [1,2,3]]` (8 subsets = 2³)

### 🗣️ What to say first
- "Are all numbers unique, or can there be duplicates?" (Duplicates change the approach!)
- "Does the order of subsets in the output matter?" (Usually no.)
- "Should the empty subset be included?" (Yes, always.)

### ⚡ Approach — Backtracking (include/exclude each element)
Each element has 2 choices: **in** the subset or **out**. We use a `start` index so we never revisit earlier elements (avoids duplicate subsets).

```java
public List<List<Integer>> subsets(int[] nums) {
    List<List<Integer>> result = new ArrayList<>();
    backtrack(result, new ArrayList<>(), nums, 0);
    return result;
}

private void backtrack(List<List<Integer>> result, List<Integer> path,
                       int[] nums, int start) {
    result.add(new ArrayList<>(path));   // EVERY node is a valid subset
    for (int i = start; i < nums.length; i++) {
        path.add(nums[i]);               // CHOOSE
        backtrack(result, path, nums, i + 1); // EXPLORE (i+1 = no reuse)
        path.remove(path.size() - 1);    // UN-CHOOSE
    }
}
```

### 🔬 Dry-run for `[1,2,3]`
```
[]            → add []
 add 1 → [1]  → add [1]
   add 2 → [1,2] → add [1,2]
     add 3 → [1,2,3] → add [1,2,3], remove 3
   remove 2
   add 3 → [1,3] → add [1,3], remove 3
 remove 1
 add 2 → [2] → add [2]
   add 3 → [2,3] → add [2,3] ...
 add 3 → [3] → add [3]
```
Result: all 8 subsets. ✅

### 📊 Complexity
- **Time:** O(N · 2ᴺ) — 2ᴺ subsets, each copy costs up to O(N).
- **Space:** O(N) recursion depth (excluding output).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Backtracking (subset / power-set generation).
- **Core idea:** *Every node in the recursion tree is itself a valid answer — add it before looping.*
- **Memory hook:** 🪆 **Russian nesting dolls** — each doll (subset) opens into bigger ones, and you collect every doll you see, not just the biggest.
- **Trigger phrase:** "all possible subsets" / "power set."

---

## 2️⃣ Permutations (Microsoft asked — Very Frequent ✅✅)

### 📋 Full Question
Given an array of **distinct** integers `nums`, return **all possible permutations** (every ordering).

**Example:**
`nums = [1, 2, 3]`
**Output:** `[[1,2,3],[1,3,2],[2,1,3],[2,3,1],[3,1,2],[3,2,1]]` (3! = 6)

### 🗣️ What to say first
- "Are the numbers distinct?" (If duplicates exist, we'd need a sort + skip rule.)
- "Do I need a specific output order?" (Usually no.)
- "Is the input small enough that N! is OK?" (Permutations are inherently factorial.)

### ⚡ Approach — Backtracking with a `used[]` marker
Unlike subsets (which use `start`), permutations can revisit **earlier** elements — just not ones already in the current path. We track that with a boolean `used[]` array.

```java
public List<List<Integer>> permute(int[] nums) {
    List<List<Integer>> result = new ArrayList<>();
    backtrack(result, new ArrayList<>(), nums, new boolean[nums.length]);
    return result;
}

private void backtrack(List<List<Integer>> result, List<Integer> path,
                       int[] nums, boolean[] used) {
    if (path.size() == nums.length) {        // full-length permutation
        result.add(new ArrayList<>(path));
        return;
    }
    for (int i = 0; i < nums.length; i++) {
        if (used[i]) continue;               // skip already-placed numbers
        used[i] = true;  path.add(nums[i]);  // CHOOSE
        backtrack(result, path, nums, used); // EXPLORE
        used[i] = false; path.remove(path.size() - 1); // UN-CHOOSE
    }
}
```

### 🔬 Dry-run for `[1,2,3]`
```
pick 1 → pick 2 → pick 3 → [1,2,3] ✅
              ← undo 3, undo 2
        pick 3 → pick 2 → [1,3,2] ✅
pick 2 → ... → [2,1,3], [2,3,1]
pick 3 → ... → [3,1,2], [3,2,1]
```
Every position is filled by every still-available number. ✅

### 📊 Complexity
- **Time:** O(N · N!) — N! permutations, each costs O(N) to build/copy.
- **Space:** O(N) for recursion + `used[]`.

### 🧠 Algorithm to Remember Forever
- **Pattern:** Backtracking (permutation generation).
- **Core idea:** *Fill each slot with every number not yet used; mark used, recurse, unmark.*
- **Memory hook:** 🪑 **Musical chairs** — every player (number) tries every chair (position); when the music stops you record the seating, then everyone gets up and tries again.
- **Trigger phrase:** "all orderings" / "all permutations" / "arrange all."

> 🆚 **Subsets vs Permutations:** Subsets use a `start` index (order doesn't matter, no reuse backward). Permutations use a `used[]` array (order matters, every element appears once). Knowing *which* to use is the interview's hidden test.

---

## 3️⃣ Combination Sum (Microsoft asked ✅)

### 📋 Full Question
Given an array of **distinct** positive integers `candidates` and a `target`, return all unique combinations where the chosen numbers **sum to target**. The **same number may be reused unlimited times**.

**Example:**
`candidates = [2, 3, 6, 7]`, `target = 7`
**Output:** `[[2,2,3], [7]]`

### 🗣️ What to say first
- "Can I reuse the same number multiple times?" (Yes here — that's the twist.)
- "Are candidates distinct and positive?" (Positivity guarantees the sum only grows → safe to stop early.)
- "Are two combinations the same if they're reorderings?" (Yes → we use `start` to keep them sorted.)

### ⚡ Approach — Backtracking with reuse (pass `i`, not `i+1`)
Because reuse is allowed, when we recurse we pass `i` (stay on same element) instead of `i+1`. We prune when `remaining < 0`.

```java
public List<List<Integer>> combinationSum(int[] candidates, int target) {
    List<List<Integer>> result = new ArrayList<>();
    backtrack(result, new ArrayList<>(), candidates, target, 0);
    return result;
}

private void backtrack(List<List<Integer>> result, List<Integer> path,
                       int[] candidates, int remaining, int start) {
    if (remaining == 0) {                    // exact hit
        result.add(new ArrayList<>(path));
        return;
    }
    if (remaining < 0) return;               // overshot → prune
    for (int i = start; i < candidates.length; i++) {
        path.add(candidates[i]);                          // CHOOSE
        backtrack(result, path, candidates, remaining - candidates[i], i); // reuse i!
        path.remove(path.size() - 1);                     // UN-CHOOSE
    }
}
```

### 🔬 Dry-run for `candidates=[2,3,6,7]`, `target=7`
```
2 → 2 → 2 → (rem 1) → 2 over(-1) prune; 3 over prune ...
2 → 2 → 3 → rem 0 ✅ [2,2,3]
2 → 3 → ... rem<0 paths pruned
3 → ... no exact hit reaching 7 with start≥3 except...
7 → rem 0 ✅ [7]
```
Result: `[[2,2,3],[7]]`. ✅

### 📊 Complexity
- **Time:** O(N^(T/min)) roughly — exponential in target/smallest candidate; hard to tighten, mention the branching factor.
- **Space:** O(T/min) recursion depth.

### 🧠 Algorithm to Remember Forever
- **Pattern:** Backtracking (combination sum with reuse).
- **Core idea:** *Subtract a candidate from the target and recurse, allowing the SAME index again; stop when target hits 0 or goes negative.*
- **Memory hook:** 🪙 **Making change with unlimited coins** — keep grabbing coins (reusing the same denomination is fine) until you hit the exact amount or overshoot.
- **Trigger phrase:** "combinations that sum to target" + "reuse allowed."

---

## 4️⃣ Letter Combinations of a Phone Number (Microsoft — Classic! ⭐)

### 📋 Full Question
Given a string of digits 2-9, return **all letter combinations** the number could spell (old phone keypad). `2→abc`, `3→def`, ..., `9→wxyz`.

**Example:**
`digits = "23"`
**Output:** `["ad","ae","af","bd","be","bf","cd","ce","cf"]`

### 🗣️ What to say first
- "What if `digits` is empty?" (Return an empty list, not `[""]`.)
- "Are the digits always in range 2-9?" (1 and 0 have no letters.)
- "Does output order matter?" (Usually no.)

### ⚡ Approach — Backtracking over digit positions
For each digit we try each of its letters, recursing to the next digit. This is a **cartesian product** built via backtracking.

```java
private static final String[] MAP = {
    "", "", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"
}; // index = digit; 0 and 1 map to nothing

public List<String> letterCombinations(String digits) {
    List<String> result = new ArrayList<>();
    if (digits == null || digits.isEmpty()) return result; // edge case!
    backtrack(result, new StringBuilder(), digits, 0);
    return result;
}

private void backtrack(List<String> result, StringBuilder path,
                       String digits, int index) {
    if (index == digits.length()) {          // built one full combination
        result.add(path.toString());
        return;
    }
    String letters = MAP[digits.charAt(index) - '0'];
    for (char c : letters.toCharArray()) {
        path.append(c);                       // CHOOSE
        backtrack(result, path, digits, index + 1); // EXPLORE next digit
        path.deleteCharAt(path.length() - 1); // UN-CHOOSE
    }
}
```

### 🔬 Dry-run for `"23"`
```
digit '2' → a,b,c ; digit '3' → d,e,f
a → ad, ae, af
b → bd, be, bf
c → cd, ce, cf
```
9 combinations (3 × 3). ✅

### 📊 Complexity
- **Time:** O(4ᴺ · N) — up to 4 letters per digit, N digits, O(N) to build each string.
- **Space:** O(N) recursion depth.

### 🧠 Algorithm to Remember Forever
- **Pattern:** Backtracking (cartesian product).
- **Core idea:** *Each digit is a level of the tree; branch on each letter, descend to the next digit.*
- **Memory hook:** ☎️ **Old Nokia texting** — press '2' three times for 'c'; here you try *all* the letters on each key and combine across keys.
- **Trigger phrase:** "all combinations across multiple choice-sets" / "phone keypad."

---

## 5️⃣ Word Search (Microsoft — Very Frequent! ✅✅)

### 📋 Full Question
Given an `m × n` grid of characters and a `word`, return **true** if the word exists in the grid. The word is built from **adjacent** cells (up/down/left/right), and the **same cell can't be used twice**.

**Example:**
```
board = [['A','B','C','E'],
         ['S','F','C','S'],
         ['A','D','E','E']]
word = "ABCCED"  →  true
word = "ABCB"    →  false  (would reuse 'B')
```

### 🗣️ What to say first
- "Can the same cell be reused?" (No — that's the key constraint → mark visited.)
- "Only 4-directional moves, or diagonals too?" (Standard: 4-directional.)
- "Could the board or word be empty?" (Handle null/empty.)

### ⚡ Approach — DFS Backtracking from every cell
Try to start the word at every cell. Mark a cell visited (temporarily overwrite with `#`), recurse to neighbors, then **restore** it (the un-choose step).

```java
public boolean exist(char[][] board, String word) {
    for (int r = 0; r < board.length; r++)
        for (int c = 0; c < board[0].length; c++)
            if (dfs(board, word, r, c, 0)) return true;
    return false;
}

private boolean dfs(char[][] board, String word, int r, int c, int idx) {
    if (idx == word.length()) return true;                 // matched all chars
    if (r < 0 || r >= board.length || c < 0 || c >= board[0].length
        || board[r][c] != word.charAt(idx)) return false;  // out of bounds / mismatch

    char temp = board[r][c];
    board[r][c] = '#';                                     // CHOOSE (mark visited)
    boolean found = dfs(board, word, r + 1, c, idx + 1)    // EXPLORE 4 directions
                 || dfs(board, word, r - 1, c, idx + 1)
                 || dfs(board, word, r, c + 1, idx + 1)
                 || dfs(board, word, r, c - 1, idx + 1);
    board[r][c] = temp;                                    // UN-CHOOSE (restore)
    return found;
}
```

### 🔬 Dry-run for `word="ABCCED"`
```
Start at (0,0)='A' ✓ → (0,1)='B' ✓ → (0,2)='C' ✓
→ (1,2)='C' ✓ → (2,2)='E' ✓ → (2,1)='D' ✓ → idx==len → TRUE
```
For `"ABCB"`: after A,B,C the only unused neighbor with 'B' would force reusing (0,1) → blocked → false. ✅

### 📊 Complexity
- **Time:** O(M · N · 4ᴸ) — start from each cell, branch ≤4 per letter, L = word length.
- **Space:** O(L) recursion depth (in-place visited marking → no extra grid).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Backtracking on a grid (DFS with restore).
- **Core idea:** *Match the word letter-by-letter along a path; temporarily block the current cell so the path can't cross itself, then unblock when backing out.*
- **Memory hook:** 🐜 **Ant leaving a scent trail** — it walks one tile at a time, marks where it's been so it won't loop, and erases the mark when it backtracks.
- **Trigger phrase:** "word/path in a grid" + "adjacent cells" + "can't reuse."

---

## 6️⃣ Generate Parentheses (Microsoft asked ✅)

### 📋 Full Question
Given `n` pairs of parentheses, generate **all combinations of well-formed (valid) parentheses**.

**Example:**
`n = 3`
**Output:** `["((()))","(()())","(())()","()(())","()()()"]`

### 🗣️ What to say first
- "Must every output be a *valid* (balanced) string?" (Yes — that drives the pruning.)
- "Is `n` the number of *pairs*?" (Yes, so total length is 2n.)
- "Order of output?" (Doesn't matter.)

### ⚡ Approach — Backtracking with two counters
Track `open` and `close` used. **Rules that keep it valid (smart pruning):**
- Add `(` only if `open < n`.
- Add `)` only if `close < open` (never close more than you've opened).

```java
public List<String> generateParenthesis(int n) {
    List<String> result = new ArrayList<>();
    backtrack(result, new StringBuilder(), 0, 0, n);
    return result;
}

private void backtrack(List<String> result, StringBuilder path,
                       int open, int close, int n) {
    if (path.length() == 2 * n) {            // used all n pairs
        result.add(path.toString());
        return;
    }
    if (open < n) {                          // can still open
        path.append('(');
        backtrack(result, path, open + 1, close, n);
        path.deleteCharAt(path.length() - 1);
    }
    if (close < open) {                      // can close only if unmatched '(' exists
        path.append(')');
        backtrack(result, path, open, close + 1, n);
        path.deleteCharAt(path.length() - 1);
    }
}
```

### 🔬 Dry-run for `n=2`
```
(            open=1
((           open=2 (can't open more)
(()          close=1
(())   ✅    close=2 → length 4
()           close=1 (open=1)
()(          open=2
()()   ✅
```
Output: `["(())","()()"]`. ✅

### 📊 Complexity
- **Time:** O(4ⁿ / √n) — the nth Catalan number (count of valid strings).
- **Space:** O(n) recursion depth.

### 🧠 Algorithm to Remember Forever
- **Pattern:** Backtracking with validity constraints.
- **Core idea:** *Only place `(` while you have spare opens, and `)` only while there's an unmatched `(` — invalid strings never even get built.*
- **Memory hook:** 🧱 **Stacking and matching blocks** — you can lay an open block any time you have stock, but you can only cap one if there's an uncapped open below it.
- **Trigger phrase:** "generate all valid combinations" + a balancing rule.

---

## 7️⃣ Jump Game (Microsoft asked ✅) — GREEDY 🤑

### 📋 Full Question
Given an array `nums` where `nums[i]` is the **max jump length** from index `i`, return **true** if you can reach the last index starting from index 0.

**Example:**
`nums = [2,3,1,1,4]` → **true** (0→1→4)
`nums = [3,2,1,0,4]` → **false** (stuck at index 3)

### 🗣️ What to say first
- "Can values be 0?" (Yes — a 0 can trap you.)
- "Do I need the path, or just yes/no?" (Just reachability.)
- "Is the array non-empty?" (A single element is trivially reachable.)

### ⚡ Approach — Greedy: track the farthest reachable index
Scan left to right. Keep `maxReach`. If the current index is **beyond** what's reachable, you're stuck → false. Otherwise extend reach.

```java
public boolean canJump(int[] nums) {
    int maxReach = 0;                        // farthest index we can get to
    for (int i = 0; i < nums.length; i++) {
        if (i > maxReach) return false;      // can't even stand here → stuck
        maxReach = Math.max(maxReach, i + nums[i]); // greedily extend reach
    }
    return true;                             // never got stuck → last index reachable
}
```

**Why greedy is safe here:** if you *can* reach index `i`, you can reach everything before it; so the only thing that matters is the *farthest* point reachable so far. No need to remember individual paths → no DP needed.

### 🔬 Dry-run
```
[2,3,1,1,4]: i0 reach=2, i1 reach=max(2,4)=4, i2 reach 4, ... → true
[3,2,1,0,4]: i0 reach=3, i1 reach=3, i2 reach=3, i3 reach=max(3,3)=3,
             i4: 4 > maxReach 3 → return false
```
✅

### 📊 Complexity
- **Time:** O(N) — single pass.
- **Space:** O(1).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Greedy (reachability frontier).
- **Core idea:** *Track the farthest index reachable so far; if you ever stand past it, you're stuck.*
- **Memory hook:** 🦘 **A kangaroo with a fuel gauge** — only the *maximum* distance it could possibly hop matters, not which exact hops it took.
- **Trigger phrase:** "can you reach the end" / "max jump length."

> 🆚 **Jump Game II** (the sequel) asks for the *minimum number of jumps*. Still greedy: treat it like BFS levels — track the end of the current jump's range, and when you reach it, increment jumps and extend the range to `farthest`. O(N).

---

## 8️⃣ Task Scheduler (Microsoft — Very Frequent! ✅✅) — GREEDY 🤑

### 📋 Full Question
Given tasks (chars A-Z) and an integer `n` (cooldown), the **same task** must be separated by at least `n` intervals. Each task takes 1 unit; CPU can be **idle**. Return the **minimum total time** to finish all tasks.

**Example:**
`tasks = ["A","A","A","B","B","B"]`, `n = 2`
**Output:** `8` → `A B idle A B idle A B`

### 🗣️ What to say first
- "Can I reorder tasks freely?" (Yes — only the cooldown constrains us.)
- "Is `n` the *gap* between identical tasks?" (Yes, minimum idle separation.)
- "Could `n` be 0?" (Then answer is just `tasks.length`.)

### ⚡ Approach — Greedy with a math formula (most-frequent task anchors the schedule)
The **most frequent** task forces the skeleton. If `maxFreq` is its count, it creates `(maxFreq - 1)` gaps of size `(n + 1)`, plus the tasks tied for the max at the end. But if there are *many* distinct tasks, no idling is needed — so take the max with `tasks.length`.

```java
public int leastInterval(char[] tasks, int n) {
    int[] freq = new int[26];
    for (char t : tasks) freq[t - 'A']++;     // count each task

    int maxFreq = 0;
    for (int f : freq) maxFreq = Math.max(maxFreq, f);

    int countMax = 0;                          // how many tasks share the max freq
    for (int f : freq) if (f == maxFreq) countMax++;

    // skeleton built around the most frequent task:
    int slots = (maxFreq - 1) * (n + 1) + countMax;

    return Math.max(slots, tasks.length);      // can't be less than total tasks
}
```

**Why greedy is safe:** scheduling the *rarest tasks into the idle gaps of the most frequent task* is always optimal — there's no future cost that a different ordering avoids. The formula captures the best possible packing.

### 🔬 Dry-run for `["A","A","A","B","B","B"], n=2`
```
maxFreq = 3 (A and B both appear 3×) → countMax = 2
slots = (3-1)*(2+1) + 2 = 2*3 + 2 = 8
tasks.length = 6
answer = max(8, 6) = 8  →  A B _ A B _ A B
```
✅

### 📊 Complexity
- **Time:** O(N) to count (the 26-array loops are O(1)).
- **Space:** O(1) — fixed 26-size array.

### 🧠 Algorithm to Remember Forever
- **Pattern:** Greedy (frequency-driven scheduling).
- **Core idea:** *The most frequent task defines the timeline skeleton; fill its idle gaps with other tasks, and never go below the total task count.*
- **Memory hook:** 🍞 **A sandwich** — the busiest task is the bread laid out with fixed gaps; everything else is filling stuffed into those gaps.
- **Trigger phrase:** "cooldown" / "same task can't repeat within n" / "minimum time to schedule."

---

## 9️⃣ Meeting Rooms II (Microsoft — Classic ⭐) — GREEDY 🤑

### 📋 Full Question
Given meeting intervals `[[start, end], ...]`, return the **minimum number of conference rooms** required so no two overlapping meetings share a room.

**Example:**
`intervals = [[0,30],[5,10],[15,20]]`
**Output:** `2` ([0,30] overlaps [5,10] → need 2 rooms; [15,20] reuses a freed room)

### 🗣️ What to say first
- "Is the end time exclusive?" (i.e., does a meeting ending at 10 free the room for one starting at 10? Usually yes.)
- "Can intervals be empty?" (Return 0.)
- "Are intervals sorted?" (Usually not — I'll sort them.)

### ⚡ Approach — Greedy with a min-heap of end times
Sort meetings by **start**. Use a **min-heap** of end times (the room that frees up soonest). For each meeting: if the earliest-ending room is free by the time this meeting starts, reuse it (pop); otherwise allocate a new room. Heap size = rooms needed.

```java
public int minMeetingRooms(int[][] intervals) {
    if (intervals == null || intervals.length == 0) return 0;
    // sort by start time
    Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));

    // min-heap of end times: the room freeing up soonest sits on top
    PriorityQueue<Integer> heap = new PriorityQueue<>();
    for (int[] meeting : intervals) {
        if (!heap.isEmpty() && heap.peek() <= meeting[0]) {
            heap.poll();                     // earliest room is free → reuse it
        }
        heap.offer(meeting[1]);              // occupy a room until this meeting ends
    }
    return heap.size();                      // peak concurrent rooms
}
```

### 🔬 Dry-run for `[[0,30],[5,10],[15,20]]`
```
sorted: [0,30],[5,10],[15,20]
[0,30]: heap empty → add 30        heap=[30]   rooms=1
[5,10]: top 30 > 5 → new room      heap=[10,30] rooms=2
[15,20]: top 10 <= 15 → reuse, pop heap=[30]
         add 20                    heap=[20,30] rooms=2
answer = heap.size() = 2
```
✅

### 📊 Complexity
- **Time:** O(N log N) — sort + heap operations.
- **Space:** O(N) — heap can hold all end times in the worst case.

### 🧠 Algorithm to Remember Forever
- **Pattern:** Greedy + min-heap (interval scheduling / overlap counting).
- **Core idea:** *Process meetings in start order; always free up the room that ends earliest if it's available, else open a new one. The peak heap size is the answer.*
- **Memory hook:** 🏨 **A hotel front desk** — guests (meetings) arrive in order; if a room has already checked out, you reuse it; otherwise you build a new room. Count the rooms.
- **Trigger phrase:** "minimum rooms / resources" + "overlapping intervals."

---

## 🔟 Gas Station (Microsoft asked ✅) — GREEDY 🤑

### 📋 Full Question
There are `n` gas stations in a circle. `gas[i]` is fuel at station `i`; `cost[i]` is fuel needed to travel from `i` to `i+1`. Return the **starting station index** to complete the circuit once, or **-1** if impossible. (Solution is guaranteed unique if it exists.)

**Example:**
`gas = [1,2,3,4,5]`, `cost = [3,4,5,1,2]`
**Output:** `3` (start at station 3)

### 🗣️ What to say first
- "Is a solution guaranteed unique?" (Yes, if total gas ≥ total cost.)
- "Do I return the index or the route?" (Index.)
- "Can I assume the tank starts empty?" (Yes.)

### ⚡ Approach — Greedy single pass
**Key insight:** If total gas < total cost → impossible (-1). Otherwise a valid start exists. As we scan, if the running tank goes negative at station `i`, then **no station from the previous start through `i` can be the answer** — so jump the start to `i+1` and reset the tank.

```java
public int canCompleteCircuit(int[] gas, int[] cost) {
    int totalTank = 0;   // overall feasibility check
    int currTank  = 0;   // fuel since the candidate start
    int start     = 0;

    for (int i = 0; i < gas.length; i++) {
        int gain = gas[i] - cost[i];
        totalTank += gain;
        currTank  += gain;
        if (currTank < 0) {                  // can't reach i+1 from current start
            start = i + 1;                   // greedily restart after the failure
            currTank = 0;                    // reset the tank
        }
    }
    return totalTank >= 0 ? start : -1;      // feasible only if total gas covers cost
}
```

**Why greedy is safe:** if you run out of gas going from start `s` to `i`, every station between `s` and `i` had a non-negative cumulative surplus when you reached it (otherwise you'd have reset earlier) — so none of them could be a better start. The first feasible start after the last failure is THE answer.

### 🔬 Dry-run for `gas=[1,2,3,4,5]`, `cost=[3,4,5,1,2]`
```
diff = [-2,-2,-2,3,3]
i0: curr=-2 <0 → start=1, curr=0   total=-2
i1: curr=-2 <0 → start=2, curr=0   total=-4
i2: curr=-2 <0 → start=3, curr=0   total=-6
i3: curr=3                          total=-3
i4: curr=6                          total=0
total 0 >= 0 → return start = 3
```
✅

### 📊 Complexity
- **Time:** O(N) — one pass.
- **Space:** O(1).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Greedy (running-balance with restart).
- **Core idea:** *If total gas ≥ total cost a start exists; whenever the tank dips below zero, the answer must lie after that point — reset start there.*
- **Memory hook:** ⛽ **A road trip budget** — the moment your wallet hits negative, you know none of the earlier stops could've been your true starting point; begin fresh from the next one.
- **Trigger phrase:** "circular route" + "start index" + "fuel/resource balance."

---

# 📊 Complexity Cheat Sheet

| # | Problem | Approach | Time | Space |
|---|---------|----------|------|-------|
| 1 | Subsets | Backtracking (start index) | O(N · 2ᴺ) | O(N) |
| 2 | Permutations | Backtracking (used[]) | O(N · N!) | O(N) |
| 3 | Combination Sum | Backtracking (reuse i) | O(N^(T/min)) | O(T/min) |
| 4 | Letter Combinations | Backtracking (cartesian) | O(4ᴺ · N) | O(N) |
| 5 | Word Search | DFS Backtracking on grid | O(M·N·4ᴸ) | O(L) |
| 6 | Generate Parentheses | Backtracking + pruning | O(4ⁿ/√n) | O(n) |
| 7 | Jump Game | Greedy (max reach) | O(N) | O(1) |
| 8 | Task Scheduler | Greedy (frequency formula) | O(N) | O(1) |
| 9 | Meeting Rooms II | Greedy + min-heap | O(N log N) | O(N) |
| 10 | Gas Station | Greedy (running balance) | O(N) | O(1) |

---

# 🧠🧠 PATTERN RECOGNITION TABLE

> **The master key:** Backtracking = *"find ALL / generate every combination or permutation."* Greedy = *"maximize/minimize a single answer via a locally-optimal, provably-safe choice."*

| If the question says... (Trigger) | Pattern | Memory Hook | Big-O |
|---|---|---|---|
| "all **subsets** / power set" | Backtracking (start idx) | 🪆 Nesting dolls | O(N·2ᴺ) |
| "all **permutations** / orderings" | Backtracking (used[]) | 🪑 Musical chairs | O(N·N!) |
| "all **combinations** summing to target" (reuse) | Backtracking (pass i) | 🪙 Unlimited coins | O(N^(T/min)) |
| "all combinations across **choice sets**" (keypad) | Backtracking (cartesian) | ☎️ Nokia texting | O(4ᴺ·N) |
| "**path/word in a grid**, adjacent, no reuse" | Backtracking DFS + restore | 🐜 Ant scent trail | O(M·N·4ᴸ) |
| "generate all **valid** (balanced) strings" | Backtracking + prune | 🧱 Matching blocks | O(4ⁿ/√n) |
| "**can you reach** the end / max jump" | Greedy (max reach) | 🦘 Kangaroo fuel gauge | O(N) |
| "**cooldown** / minimum schedule time" | Greedy (freq formula) | 🍞 Sandwich gaps | O(N) |
| "**minimum rooms** / overlapping intervals" | Greedy + min-heap | 🏨 Hotel front desk | O(N log N) |
| "**circular route**, start index, fuel balance" | Greedy (running balance) | ⛽ Road-trip budget | O(N) |

**The 5-second triage:**
1. Do they want **every** answer / arrangement? → **Backtracking** (choose → explore → un-choose).
2. Do they want **one** optimal number (min/max/count/reachability) AND a locally-best choice is provably safe? → **Greedy**.
3. Locally-best choice could trap you later (choices interact)? → that's **DP**, not greedy.

---

# ☕ Java Tips

### For Backtracking 🔁
- **Result container:** `List<List<Integer>> result = new ArrayList<>();`
- **The path:** reuse ONE `List<Integer> path` across recursion — don't allocate a new one each call.
- **ALWAYS copy on add:** `result.add(new ArrayList<>(path));` — adding `path` directly stores a *reference* that mutates to empty later. This is the #1 backtracking bug. 🐛
- **Un-choose with the right remove:** `path.remove(path.size() - 1)` removes the LAST element (by index). ⚠️ `list.remove(Integer)` vs `list.remove(int)` is a classic trap — `remove(int)` removes by *index*. Removing the last index is always safe.
- **Strings:** use a `StringBuilder` as the path; `append(c)` to choose, `deleteCharAt(len-1)` to un-choose. Far cheaper than string concatenation.
- **Grid visited:** overwrite the cell in place (`board[r][c]='#'`) and restore it — saves a separate `boolean[][]` and O(M·N) space.
- **Pruning:** `if (!isValid) continue;` early — it's what makes backtracking faster than brute force.

### For Greedy 🤑
- **Sort with a Comparator:**
  - By field: `Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));`
  - Use `Integer.compare(a, b)` not `a - b` (avoids integer overflow on large values!). 🔢
- **Min-heap (smallest on top):** `PriorityQueue<Integer> pq = new PriorityQueue<>();`
- **Max-heap (largest on top):** `new PriorityQueue<>(Collections.reverseOrder());` or `(a, b) -> b - a`.
- **Peek before poll:** `pq.peek()` reads the top without removing; `pq.poll()` removes and returns it.
- **Frequency counting:** for A-Z use `int[26]` indexed by `c - 'A'` — O(1) space, faster than a HashMap.
- **Running totals:** greedy often needs just one or two `int` accumulators and a single pass — resist the urge to over-engineer.

---

# ✅ Final Interview Checklist

Before you say "I'm done," tick every box:

- [ ] 🗣️ **Restated the problem** and asked clarifying questions (duplicates? empty input? order matters?).
- [ ] 🐢 **Mentioned brute force** and its complexity before jumping to the optimal.
- [ ] 🧭 **Identified the pattern out loud**: "This is *backtracking* because we need all X" or "This is *greedy* because the locally-best choice is safe since Y."
- [ ] 🔁 For backtracking: wrote **choose → explore → un-choose**, and **copied** the path on add (`new ArrayList<>(path)`).
- [ ] ✂️ Added **pruning / validity checks** to cut dead branches.
- [ ] 🤑 For greedy: **justified WHY the greedy choice is safe** (this is what separates a pass from a fail).
- [ ] 🔬 **Dry-ran** the code on the example input, out loud, line by line.
- [ ] 🧪 Checked **edge cases**: empty array, single element, all-same values, a `0` in Jump Game, `n=0` in Task Scheduler.
- [ ] 📊 Stated **Time AND Space** complexity and explained the dominant term.
- [ ] 🧹 Mentioned a **trade-off or follow-up** ("If we needed the count instead of the list, we could..." / "Jump Game II extends this to BFS levels").

🎤 **Remember:** The interviewer is hiring a *thinker*, not a *coder*. Narrate every decision. A clear, correct, well-explained O(N) greedy beats a silent, buggy "clever" solution every time. You've got this! 🚀
