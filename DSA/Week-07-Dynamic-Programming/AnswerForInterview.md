# Week 7: Dynamic Programming (DP) — How to ANSWER in a Microsoft Interview 🎤 (Java)

> **The Golden Rule of DP interviews:** *Never* jump straight to a `dp[]` array. Walk the interviewer up the ladder so they see your thinking:
>
> **Clarify → Brute-force recursion → Memoization (top-down) → Tabulation (bottom-up) → Space-optimized → Complexity**
>
> This shows you understand *why* DP works (you're caching repeated work), not just that you memorized a table. Microsoft interviewers love watching a candidate turn an exponential recursion into a linear table — it's the whole point of the round.

---

## 🪜 The 5-Step DP Ladder (memorize this!)

| Rung | What you say | Why it scores points |
|------|--------------|----------------------|
| 1️⃣ Brute recursion | "Let me define the problem recursively first." | Shows you found the **recurrence** |
| 2️⃣ Spot the repeat | "Notice `f(3)` is computed many times — overlapping subproblems!" | This is the **trigger** for DP |
| 3️⃣ Memoize | "Cache results in a map/array → top-down DP." | Easy win, same recursion + cache |
| 4️⃣ Tabulate | "Now bottom-up: fill smallest subproblems first." | Removes recursion stack |
| 5️⃣ Optimize space | "I only need the last 1–2 rows → O(1)/O(n) space." | The senior-engineer flourish ✨ |

---

## 🗣️ Interview Script (say this almost verbatim)

> "This looks like a **dynamic programming** problem because I can break it into overlapping subproblems with optimal substructure.
> Let me first write the **recurrence** as a brute-force recursion so we agree on the logic.
> Then I'll notice we recompute the same states, so I'll **memoize** it — that's top-down DP, *O(states × work-per-state)*.
> Finally I'll convert it to a **bottom-up table** and see if I can shrink the space."

Then: define your **state** out loud — *"Let `dp[i]` mean ___."* This single sentence is 50% of a DP interview.

### Common clarifying questions (ask 2–3, don't dump all)
- "Can the input be **empty** or null? What should I return?"
- "Are the numbers **non-negative**? Any bounds on the values / array size?"
- "Do you want the **value** of the answer, or the actual **sequence/path** reconstructed?"
- "Are duplicates allowed? Is the array **sorted**?"
- "For 'count ways' — could the count **overflow** `int`? Should I use `long`?"

### 🔍 How to recognize a DP problem (the 2-question test)
1. **Overlapping subproblems** — does solving the big problem re-solve the same smaller problem repeatedly? (e.g., `fib(5)` recomputes `fib(3)` twice.)
2. **Optimal substructure** — is the optimal answer built from optimal answers to subproblems?

If **both** are true → it's DP. The wording almost always gives it away:

> 🚩 **Trigger phrases:** "**count the number of ways**", "**minimum / maximum** cost / value / length", "**can you reach / partition / make**", "**longest** subsequence / substring", "**how many distinct**". The moment you hear these, your brain should whisper *"define `dp[i] = ...`"*.

---

# 🎯 The Problems

---

## 1. Climbing Stairs 🪜 *(Microsoft asked — the "Hello World" of DP)*

### 📋 Full Question
> You're climbing a staircase with `n` steps. Each time you can climb **1 or 2 steps**. In how many **distinct ways** can you reach the top?
>
> **Input:** `n = 4` → **Output:** `5`
> (The ways: `1+1+1+1`, `1+1+2`, `1+2+1`, `2+1+1`, `2+2`.)

### 🗣️ What to say first
- "Can `n` be 0? I'll assume 1 way (do nothing) for `n = 0`."
- "**State:** `dp[i]` = number of ways to reach step `i`."
- "**Recurrence:** to land on step `i`, I came from `i-1` (one step) or `i-2` (two steps) → `dp[i] = dp[i-1] + dp[i-2]`. It's literally **Fibonacci**!"

### Approach 1 — Brute force recursion
```java
// Exponential — recomputes the same step counts over and over
public int climbStairs(int n) {
    if (n <= 2) return n;          // 1 way for n=1, 2 ways for n=2
    return climbStairs(n - 1) + climbStairs(n - 2);
}
// Time: O(2^n)  |  Space: O(n) recursion stack
```

### Approach 2 — Optimal (tabulation → space-optimized)
**Recurrence:** `dp[i] = dp[i-1] + dp[i-2]`, with `dp[1]=1, dp[2]=2`.

```java
// Bottom-up, O(1) space — we only ever need the last two values
public int climbStairs(int n) {
    if (n <= 2) return n;
    int prev2 = 1; // ways to reach step 1
    int prev1 = 2; // ways to reach step 2
    for (int i = 3; i <= n; i++) {
        int cur = prev1 + prev2; // dp[i] = dp[i-1] + dp[i-2]
        prev2 = prev1;
        prev1 = cur;
    }
    return prev1;
}
// Time: O(n)  |  Space: O(1)
```

**Dry-run (`n = 4`):** prev2=1, prev1=2 → i=3: cur=3 → prev2=2, prev1=3 → i=4: cur=5 ✅

### 🧠 Algorithm to Remember Forever
- **Pattern:** *Fibonacci-style 1D DP* (count-ways).
- **Recurrence (one line):** `dp[i] = dp[i-1] + dp[i-2]`.
- **Memory hook:** 🐰 *Rabbits on a staircase* — each step's ways = sum of the two steps below. It's Fibonacci wearing sneakers.
- **Trigger phrase:** *"How many distinct ways to reach…"*

---

## 2. House Robber 🏠💰 *(Microsoft — Frequent)*

### 📋 Full Question
> A street of houses each holding some money. Adjacent houses have connected alarms — robbing **two adjacent** houses triggers it. Return the **maximum** money you can rob.
>
> **Input:** `nums = [2, 7, 9, 3, 1]` → **Output:** `12`
> (Rob house 0 (2) + house 2 (9) + house 4 (1) = 12.)

### 🗣️ What to say first
- "**State:** `dp[i]` = max money robbing among houses `0..i`."
- "At house `i` I have a **choice**: rob it (`nums[i] + dp[i-2]`) or skip it (`dp[i-1]`)."
- "**Recurrence:** `dp[i] = max(dp[i-1], nums[i] + dp[i-2])`."

### Approach 1 — Brute force recursion
```java
// Exponential: at each house we branch rob/skip
public int rob(int[] nums) {
    return robFrom(nums, 0);
}
private int robFrom(int[] nums, int i) {
    if (i >= nums.length) return 0;
    int robThis = nums[i] + robFrom(nums, i + 2); // skip the neighbor
    int skipThis = robFrom(nums, i + 1);
    return Math.max(robThis, skipThis);
}
// Time: O(2^n)  |  Space: O(n) stack
```

### Approach 2 — Optimal (space-optimized 1D DP)
**Recurrence:** `dp[i] = max(dp[i-1], nums[i] + dp[i-2])`.

```java
// O(1) space — track "best including up to i-1" and "i-2"
public int rob(int[] nums) {
    int prev2 = 0; // best for houses up to i-2
    int prev1 = 0; // best for houses up to i-1
    for (int money : nums) {
        int cur = Math.max(prev1, money + prev2); // skip vs rob
        prev2 = prev1;
        prev1 = cur;
    }
    return prev1;
}
// Time: O(n)  |  Space: O(1)
```

**Dry-run (`[2,7,9,3,1]`):**
| money | rob = money+prev2 | skip = prev1 | cur |
|-------|------|------|-----|
| 2 | 2+0=2 | 0 | **2** |
| 7 | 7+0=7 | 2 | **7** |
| 9 | 9+2=11 | 7 | **11** |
| 3 | 3+7=10 | 11 | **11** |
| 1 | 1+11=12 | 11 | **12** ✅ |

> 💡 **House Robber II** (circular street): houses form a circle, so house 0 and house n-1 are adjacent. **Trick:** run the same linear solver twice — once on `[0 .. n-2]` (exclude last) and once on `[1 .. n-1]` (exclude first) — and take the max.

### 🧠 Algorithm to Remember Forever
- **Pattern:** *Pick-or-skip 1D DP* (a cousin of 0/1 knapsack on a line).
- **Recurrence (one line):** `dp[i] = max(dp[i-1], nums[i] + dp[i-2])`.
- **Memory hook:** 🥷 *A ninja burglar who can't hit two doors in a row* — at each door, "grab it + the loot from two doors back" vs "tiptoe past."
- **Trigger phrase:** *"Max sum with no two adjacent elements."*

---

## 3. Coin Change 🪙 *(Microsoft — VERY FREQUENT! Must solve in 10 min)*

### 📋 Full Question
> Given `coins` of different denominations and a target `amount`, return the **fewest number of coins** to make that amount. If it's impossible, return `-1`. You have an unlimited supply of each coin.
>
> **Input:** `coins = [1, 2, 5]`, `amount = 11` → **Output:** `3`
> (`5 + 5 + 1 = 11`, three coins.)

### 🗣️ What to say first
- "**State:** `dp[a]` = fewest coins to make amount `a`."
- "**Base case:** `dp[0] = 0` (zero coins make amount 0)."
- "**Recurrence:** for each coin, `dp[a] = min(dp[a], dp[a - coin] + 1)`."
- "This is **unbounded knapsack** — I can reuse each coin, so I iterate amounts outward."
- ⚠️ "Greedy (take biggest coin first) **fails** — e.g. coins `[1,3,4]`, amount 6: greedy gives 4+1+1=3, but 3+3=2 is better. So I'll use DP."

### Approach 1 — Brute force recursion
```java
// Exponential: try every coin at every amount
public int coinChange(int[] coins, int amount) {
    if (amount == 0) return 0;
    if (amount < 0) return -1;
    int best = Integer.MAX_VALUE;
    for (int coin : coins) {
        int res = coinChange(coins, amount - coin);
        if (res >= 0) best = Math.min(best, res + 1);
    }
    return best == Integer.MAX_VALUE ? -1 : best;
}
// Time: O(coins^amount)  |  Space: O(amount) stack
```

### Approach 2 — Optimal (bottom-up tabulation)
**Recurrence:** `dp[a] = min(dp[a], dp[a - coin] + 1)` for every coin with `coin <= a`.

```java
public int coinChange(int[] coins, int amount) {
    // dp[a] = min coins to make amount a; use amount+1 as "infinity"
    // WHY amount+1: any real answer needs <= amount coins (all 1s),
    // so amount+1 safely represents "unreachable" and won't overflow.
    int[] dp = new int[amount + 1];
    Arrays.fill(dp, amount + 1);
    dp[0] = 0; // base case
    for (int a = 1; a <= amount; a++) {
        for (int coin : coins) {
            if (coin <= a) {
                dp[a] = Math.min(dp[a], dp[a - coin] + 1);
            }
        }
    }
    return dp[amount] > amount ? -1 : dp[amount];
}
// Time: O(amount × coins)  |  Space: O(amount)
```

**Dry-run (`coins=[1,2,5]`, `amount=11`):** dp[1]=1, dp[2]=1, dp[3]=2, dp[4]=2, dp[5]=1, dp[6]=2, dp[7]=2, dp[8]=3, dp[9]=3, dp[10]=2, **dp[11] = dp[6]+1 = 3** ✅

### 🧠 Algorithm to Remember Forever
- **Pattern:** *Unbounded Knapsack* (reuse items freely).
- **Recurrence (one line):** `dp[a] = min(dp[a], dp[a-coin] + 1)`.
- **Memory hook:** 🪙 *A vending machine giving change* — for each total, ask "what's the cheapest way to get here from one coin less?"
- **Trigger phrase:** *"Minimum coins / items to reach a target, unlimited supply."*

---

## 4. Longest Increasing Subsequence (LIS) 📈 *(Microsoft asked)*

### 📋 Full Question
> Given an integer array `nums`, return the length of the **longest strictly increasing subsequence** (elements need not be contiguous).
>
> **Input:** `nums = [10, 9, 2, 5, 3, 7, 101, 18]` → **Output:** `4`
> (One LIS is `[2, 3, 7, 101]`.)

### 🗣️ What to say first
- "**State:** `dp[i]` = length of the longest increasing subsequence **ending at** index `i`."
- "**Recurrence:** `dp[i] = 1 + max(dp[j])` over all `j < i` with `nums[j] < nums[i]`; default `dp[i] = 1`."
- "Answer is `max(dp[i])` over all `i`, not `dp[n-1]`."
- "I can do O(n²). If you want, there's an O(n log n) patience-sorting / binary-search version too."

### Approach 1 — Brute force recursion (take / skip each element)
```java
// Exponential: for each element choose include (if valid) or skip
public int lengthOfLIS(int[] nums) {
    return lis(nums, 0, Integer.MIN_VALUE);
}
private int lis(int[] nums, int i, int prev) {
    if (i == nums.length) return 0;
    int skip = lis(nums, i + 1, prev);
    int take = 0;
    if (nums[i] > prev) take = 1 + lis(nums, i + 1, nums[i]);
    return Math.max(take, skip);
}
// Time: O(2^n)  |  Space: O(n) stack
```

### Approach 2 — Optimal (O(n²) tabulation)
**Recurrence:** `dp[i] = 1 + max{ dp[j] : j < i and nums[j] < nums[i] }`.

```java
public int lengthOfLIS(int[] nums) {
    int n = nums.length;
    int[] dp = new int[n];
    Arrays.fill(dp, 1); // every element alone is an LIS of length 1
    int best = 1;
    for (int i = 1; i < n; i++) {
        for (int j = 0; j < i; j++) {
            if (nums[j] < nums[i]) {
                dp[i] = Math.max(dp[i], dp[j] + 1);
            }
        }
        best = Math.max(best, dp[i]);
    }
    return best;
}
// Time: O(n²)  |  Space: O(n)
```

**Dry-run (`[10,9,2,5,3,7,101,18]`):** dp = `[1,1,1,2,2,3,4,4]` → max = **4** ✅

> ⚡ **O(n log n) bonus:** keep a `tails` list; for each num, binary-search the first element `>= num` and replace it (or append). The list length is the LIS length. Mention this to impress — but only code it if asked.

### 🧠 Algorithm to Remember Forever
- **Pattern:** *LIS / "ending-here" 1D DP*.
- **Recurrence (one line):** `dp[i] = 1 + max(dp[j] for j<i, nums[j]<nums[i])`.
- **Memory hook:** 📈 *Climbing a mountain range* — each peak's longest trail = the best lower peak's trail + 1.
- **Trigger phrase:** *"Longest increasing / chain / nesting subsequence."*

---

## 5. Word Break 📖 *(Microsoft — Frequent)*

### 📋 Full Question
> Given a string `s` and a dictionary `wordDict`, return `true` if `s` can be **segmented** into a space-separated sequence of one or more dictionary words.
>
> **Input:** `s = "leetcode"`, `wordDict = ["leet", "code"]` → **Output:** `true`
> (`"leet" + "code"`.)

### 🗣️ What to say first
- "**State:** `dp[i]` = can the **prefix** `s[0..i-1]` (length `i`) be fully segmented?"
- "**Base case:** `dp[0] = true` (empty string is trivially segmentable)."
- "**Recurrence:** `dp[i]` is true if there exists `j < i` where `dp[j]` is true AND `s[j..i-1]` is in the dictionary."
- "I'll put the dictionary in a `HashSet` for O(1) lookups."

### Approach 1 — Brute force recursion
```java
// Exponential: try breaking at every prefix that's a word
public boolean wordBreak(String s, List<String> wordDict) {
    Set<String> dict = new HashSet<>(wordDict);
    return canBreak(s, 0, dict);
}
private boolean canBreak(String s, int start, Set<String> dict) {
    if (start == s.length()) return true;
    for (int end = start + 1; end <= s.length(); end++) {
        if (dict.contains(s.substring(start, end)) && canBreak(s, end, dict)) {
            return true;
        }
    }
    return false;
}
// Time: O(2^n)  |  Space: O(n) stack
```

### Approach 2 — Optimal (bottom-up tabulation)
**Recurrence:** `dp[i] = OR over j<i of ( dp[j] AND s.substring(j, i) ∈ dict )`.

```java
public boolean wordBreak(String s, List<String> wordDict) {
    Set<String> dict = new HashSet<>(wordDict); // O(1) lookups
    int n = s.length();
    boolean[] dp = new boolean[n + 1];
    dp[0] = true; // empty prefix is always breakable
    for (int i = 1; i <= n; i++) {
        for (int j = 0; j < i; j++) {
            if (dp[j] && dict.contains(s.substring(j, i))) {
                dp[i] = true;
                break; // found one valid split, no need to check further
            }
        }
    }
    return dp[n];
}
// Time: O(n² × L) where L = avg substring length  |  Space: O(n)
```

**Dry-run (`"leetcode"`):** dp[0]=T → dp[4]=T (`"leet"`) → dp[8]=T (`dp[4]` && `"code"`) → returns **true** ✅

### 🧠 Algorithm to Remember Forever
- **Pattern:** *Prefix / partition 1D DP on a string*.
- **Recurrence (one line):** `dp[i] = ∃ j<i : dp[j] && s[j..i) ∈ dict`.
- **Memory hook:** 🧩 *Snapping a string into puzzle pieces* — a string is breakable if some earlier breakable point is followed by a valid word.
- **Trigger phrase:** *"Can the string be split into dictionary words?"*

---

## 6. Unique Paths 🗺️ *(Microsoft — Classic grid DP)*

### 📋 Full Question
> A robot sits in the top-left of an `m × n` grid and wants to reach the bottom-right. It can only move **right or down**. How many **unique paths** are there?
>
> **Input:** `m = 3, n = 7` → **Output:** `28`

### 🗣️ What to say first
- "**State:** `dp[i][j]` = number of ways to reach cell `(i, j)`."
- "**Base case:** the entire first row and first column = 1 (only one straight-line way)."
- "**Recurrence:** `dp[i][j] = dp[i-1][j] + dp[i][j-1]` (came from above or from the left)."
- "I can compress to O(n) space since I only need the previous row."

### Approach 1 — Brute force recursion
```java
// Exponential: branch right/down at every cell
public int uniquePaths(int m, int n) {
    return paths(0, 0, m, n);
}
private int paths(int i, int j, int m, int n) {
    if (i == m - 1 || j == n - 1) return 1; // reached last row or col
    return paths(i + 1, j, m, n) + paths(i, j + 1, m, n);
}
// Time: O(2^(m+n))  |  Space: O(m+n) stack
```

### Approach 2 — Optimal (space-optimized to 1 row)
**Recurrence:** `dp[i][j] = dp[i-1][j] + dp[i][j-1]`.

```java
// O(n) space: roll a single row downward
public int uniquePaths(int m, int n) {
    int[] dp = new int[n];
    Arrays.fill(dp, 1); // first row: one way to each cell
    for (int i = 1; i < m; i++) {
        for (int j = 1; j < n; j++) {
            // dp[j] (old) = value from row above; dp[j-1] = value from left
            dp[j] = dp[j] + dp[j - 1];
        }
    }
    return dp[n - 1];
}
// Time: O(m × n)  |  Space: O(n)
```

**Dry-run (`m=3, n=7`):** row starts `[1,1,1,1,1,1,1]` → after row 2: `[1,2,3,4,5,6,7]` → after row 3: `[1,3,6,10,15,21,28]` → **28** ✅

### 🧠 Algorithm to Remember Forever
- **Pattern:** *Grid DP (count-ways)*.
- **Recurrence (one line):** `dp[i][j] = dp[i-1][j] + dp[i][j-1]`.
- **Memory hook:** 🤖 *A robot that only walks right/down* — every cell's path count is the sum of the cell above and the cell to the left.
- **Trigger phrase:** *"Count paths in a grid, moving only right/down."*

---

## 7. Longest Common Subsequence (LCS) 🔗 *(Microsoft asked — revision must-know)*

### 📋 Full Question
> Given two strings `text1` and `text2`, return the length of their **longest common subsequence** (characters in the same relative order, not necessarily contiguous). Return 0 if none.
>
> **Input:** `text1 = "abcde"`, `text2 = "ace"` → **Output:** `3`
> (LCS is `"ace"`.)

### 🗣️ What to say first
- "**State:** `dp[i][j]` = LCS length of `text1`'s first `i` chars and `text2`'s first `j` chars."
- "**Recurrence:** if `text1[i-1] == text2[j-1]` → `dp[i][j] = dp[i-1][j-1] + 1`; else `dp[i][j] = max(dp[i-1][j], dp[i][j-1])`."
- "**Base case:** `dp[0][*] = dp[*][0] = 0` (empty string has no common subsequence)."

### Approach 1 — Brute force recursion
```java
// Exponential: match-or-skip on both strings
public int longestCommonSubsequence(String a, String b) {
    return lcs(a, b, a.length(), b.length());
}
private int lcs(String a, String b, int i, int j) {
    if (i == 0 || j == 0) return 0;
    if (a.charAt(i - 1) == b.charAt(j - 1)) return 1 + lcs(a, b, i - 1, j - 1);
    return Math.max(lcs(a, b, i - 1, j), lcs(a, b, i, j - 1));
}
// Time: O(2^(m+n))  |  Space: O(m+n) stack
```

### Approach 2 — Optimal (2D tabulation)
**Recurrence:**
- match: `dp[i][j] = dp[i-1][j-1] + 1`
- mismatch: `dp[i][j] = max(dp[i-1][j], dp[i][j-1])`

```java
public int longestCommonSubsequence(String a, String b) {
    int m = a.length(), n = b.length();
    int[][] dp = new int[m + 1][n + 1]; // row/col 0 are the empty-string base cases
    for (int i = 1; i <= m; i++) {
        for (int j = 1; j <= n; j++) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                dp[i][j] = dp[i - 1][j - 1] + 1;     // chars match → extend diagonal
            } else {
                dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]); // drop one char
            }
        }
    }
    return dp[m][n];
}
// Time: O(m × n)  |  Space: O(m × n)  (can be reduced to O(min(m,n)) with two rows)
```

**Dry-run (`"abcde"` vs `"ace"`):** matches at a, c, e along the diagonal → final `dp[5][3] = 3` ✅

### 🧠 Algorithm to Remember Forever
- **Pattern:** *LCS / two-string 2D DP* (the parent of Edit Distance).
- **Recurrence (one line):** `match → diag+1, else → max(up, left)`.
- **Memory hook:** 🔗 *Two zippers* — when teeth match, link them and move both up diagonally; when they don't, slide whichever side gives more.
- **Trigger phrase:** *"Longest common subsequence between two strings."*

---

## 8. Edit Distance ✏️ *(Microsoft CLASSIC — appears constantly!)*

### 📋 Full Question
> Given two words `word1` and `word2`, return the **minimum number of operations** to convert `word1` into `word2`. Allowed operations: **insert**, **delete**, or **replace** a character.
>
> **Input:** `word1 = "horse"`, `word2 = "ros"` → **Output:** `3`
> (horse → rorse (replace h→r) → rose (delete r) → ros (delete e).)

### 🗣️ What to say first
- "**State:** `dp[i][j]` = min operations to turn `word1`'s first `i` chars into `word2`'s first `j` chars."
- "**Base cases:** `dp[i][0] = i` (delete all `i` chars), `dp[0][j] = j` (insert all `j` chars)."
- "**Recurrence:** if chars match → `dp[i-1][j-1]` (free); else `1 + min(replace, delete, insert)` = `1 + min(dp[i-1][j-1], dp[i-1][j], dp[i][j-1])`."

### Approach 1 — Brute force recursion
```java
// Exponential: three-way branch on every mismatch
public int minDistance(String a, String b) {
    return edit(a, b, a.length(), b.length());
}
private int edit(String a, String b, int i, int j) {
    if (i == 0) return j; // insert remaining j chars
    if (j == 0) return i; // delete remaining i chars
    if (a.charAt(i - 1) == b.charAt(j - 1)) return edit(a, b, i - 1, j - 1);
    int replace = edit(a, b, i - 1, j - 1);
    int delete  = edit(a, b, i - 1, j);
    int insert  = edit(a, b, i, j - 1);
    return 1 + Math.min(replace, Math.min(delete, insert));
}
// Time: O(3^(m+n))  |  Space: O(m+n) stack
```

### Approach 2 — Optimal (2D tabulation)
**Recurrence:**
- match: `dp[i][j] = dp[i-1][j-1]`
- mismatch: `dp[i][j] = 1 + min(dp[i-1][j-1] /*replace*/, dp[i-1][j] /*delete*/, dp[i][j-1] /*insert*/)`

```java
public int minDistance(String a, String b) {
    int m = a.length(), n = b.length();
    int[][] dp = new int[m + 1][n + 1];
    for (int i = 0; i <= m; i++) dp[i][0] = i; // delete all i chars
    for (int j = 0; j <= n; j++) dp[0][j] = j; // insert all j chars
    for (int i = 1; i <= m; i++) {
        for (int j = 1; j <= n; j++) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                dp[i][j] = dp[i - 1][j - 1]; // no operation needed
            } else {
                dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],          // replace
                              Math.min(dp[i - 1][j], dp[i][j - 1])); // delete, insert
            }
        }
    }
    return dp[m][n];
}
// Time: O(m × n)  |  Space: O(m × n)  (reducible to O(n) with two rows)
```

**Dry-run (`"horse"` → `"ros"`):** the table fills to `dp[5][3] = 3` ✅ (replace h→r, delete r, delete e).

### 🧠 Algorithm to Remember Forever
- **Pattern:** *Edit Distance / Levenshtein 2D DP*.
- **Recurrence (one line):** `match → diag; else → 1 + min(diag, up, left)`.
- **Memory hook:** ✏️ *Autocorrect's brain* — diagonal = replace, up = delete, left = insert; pick the cheapest neighbor + 1.
- **Trigger phrase:** *"Min operations to transform one string into another."*

---

## 9. Longest Palindromic Substring 🪞 *(Microsoft — Frequent!)*

### 📋 Full Question
> Given a string `s`, return the **longest substring** that is a **palindrome** (reads the same forwards and backwards; must be contiguous).
>
> **Input:** `s = "babad"` → **Output:** `"bab"` (or `"aba"` — both are valid length-3 answers).

### 🗣️ What to say first
- "Note: **substring** = contiguous (different from subsequence)."
- "I'll use **expand-around-center**: every palindrome has a center. There are `2n-1` centers (n single-char centers + n-1 gaps for even-length palindromes)."
- "For each center, expand outward while characters match, and track the longest span."
- "There's also a 2D DP `dp[i][j] = (s[i]==s[j] && dp[i+1][j-1])`, but expand-around-center is O(1) space and easier to code under pressure."

### Approach 1 — Brute force (check every substring)
```java
// O(n^3): n^2 substrings × O(n) palindrome check each
public String longestPalindrome(String s) {
    String best = "";
    for (int i = 0; i < s.length(); i++) {
        for (int j = i; j < s.length(); j++) {
            if (isPalindrome(s, i, j) && (j - i + 1) > best.length()) {
                best = s.substring(i, j + 1);
            }
        }
    }
    return best;
}
private boolean isPalindrome(String s, int lo, int hi) {
    while (lo < hi) {
        if (s.charAt(lo++) != s.charAt(hi--)) return false;
    }
    return true;
}
// Time: O(n^3)  |  Space: O(1)
```

### Approach 2 — Optimal (expand around center)
**Idea (the "recurrence" here is the expansion):** a palindrome of center `c` extends as long as `s[left] == s[right]`.

```java
public String longestPalindrome(String s) {
    if (s == null || s.length() < 1) return "";
    int start = 0, maxLen = 1;
    for (int i = 0; i < s.length(); i++) {
        int len1 = expand(s, i, i);     // odd-length center (single char)
        int len2 = expand(s, i, i + 1); // even-length center (between two chars)
        int len = Math.max(len1, len2);
        if (len > maxLen) {
            maxLen = len;
            start = i - (len - 1) / 2;   // back up to the palindrome's start
        }
    }
    return s.substring(start, start + maxLen);
}
// Returns the length of the palindrome expanding from (left, right)
private int expand(String s, int left, int right) {
    while (left >= 0 && right < s.length() && s.charAt(left) == s.charAt(right)) {
        left--;
        right++;
    }
    return right - left - 1; // window shrunk one step past the valid edges
}
// Time: O(n²)  |  Space: O(1)
```

**Dry-run (`"babad"`):** center at index 1 (`a`) expands to `"bab"` (len 3); center at index 2 (`b`) expands to `"aba"` (len 3). First found `"bab"` is returned ✅

### 🧠 Algorithm to Remember Forever
- **Pattern:** *Expand-around-center* (a 2D-DP-equivalent done in O(1) space).
- **Recurrence (one line):** `palindrome(i,j) = s[i]==s[j] && palindrome(i+1,j-1)`.
- **Memory hook:** 🪞 *Mirror, mirror* — stand at each character (and each gap) and push two mirrors outward until they disagree.
- **Trigger phrase:** *"Longest palindromic substring / contiguous palindrome."*

---

# 📊 Complexity Cheat Sheet

| Problem | Brute Recursion | Optimal DP | DP Pattern |
|---------|-----------------|------------|------------|
| Climbing Stairs | O(2ⁿ) | **O(n) time, O(1) space** | Fibonacci 1D |
| House Robber | O(2ⁿ) | **O(n) time, O(1) space** | Pick/skip 1D |
| Coin Change | O(coinsᵃᵐᵒᵘⁿᵗ) | **O(amount × coins), O(amount)** | Unbounded knapsack |
| Longest Increasing Subseq | O(2ⁿ) | **O(n²)** (or O(n log n)) | LIS / ending-here |
| Word Break | O(2ⁿ) | **O(n²·L), O(n)** | Prefix partition |
| Unique Paths | O(2^(m+n)) | **O(m·n), O(n)** | Grid count-ways |
| Longest Common Subseq | O(2^(m+n)) | **O(m·n)** | LCS 2D |
| Edit Distance | O(3^(m+n)) | **O(m·n)** | Levenshtein 2D |
| Longest Palindromic Substr | O(n³) | **O(n²), O(1)** | Expand-around-center |

---

# 🧠🧠 DP Pattern Recognition Table

| If the question says… (trigger) | → DP Pattern | State / Recurrence | Big-O |
|---------------------------------|--------------|--------------------|-------|
| "How many **ways** to reach the top/end?" | Fibonacci / count-ways 1D | `dp[i] = dp[i-1] + dp[i-2]` | O(n) |
| "**Max sum**, no two **adjacent**" | Pick-or-skip 1D | `dp[i] = max(dp[i-1], nums[i]+dp[i-2])` | O(n) |
| "**Min coins / items**, unlimited supply" | Unbounded knapsack | `dp[a] = min(dp[a], dp[a-coin]+1)` | O(n·k) |
| "**Can we partition / make** a target sum?" | 0/1 subset-sum knapsack | `dp[s] = dp[s] \|\| dp[s-num]` | O(n·sum) |
| "**Longest increasing** subsequence" | LIS | `dp[i] = 1 + max(dp[j]: nums[j]<nums[i])` | O(n²) / O(n log n) |
| "**Longest common** subseq of two strings" | LCS 2D | match→diag+1, else→max(up,left) | O(m·n) |
| "**Min edits** to transform string A→B" | Edit distance 2D | match→diag, else→1+min(diag,up,left) | O(m·n) |
| "Count **paths in a grid**, right/down" | Grid count-ways | `dp[i][j] = dp[i-1][j] + dp[i][j-1]` | O(m·n) |
| "**Min path sum** in a grid" | Grid min-cost | `dp[i][j] = grid+min(up,left)` | O(m·n) |
| "Can string be **split** into dict words?" | Prefix partition | `dp[i] = ∃j: dp[j] && s[j..i)∈dict` | O(n²·L) |
| "**Longest palindromic** substring" | Expand-around-center | `s[i]==s[j] && inner` | O(n²) |
| "**Decode / count** valid interpretations" | 1D ways-with-constraint | `dp[i] = dp[i-1](if valid) + dp[i-2](if valid)` | O(n) |

---

# ☕ Java Tips for DP

```java
// 1️⃣ 1D table of ints — Arrays.fill to set a base/"infinity" value
int[] dp = new int[n + 1];
Arrays.fill(dp, Integer.MAX_VALUE);   // or amount+1 to avoid overflow in min()
dp[0] = 0;

// 2️⃣ 2D table — Java auto-initializes int[][] to all 0, perfect for LCS base case
int[][] dp = new int[m + 1][n + 1];   // row 0 / col 0 already 0

// 3️⃣ boolean table for yes/no DP (Word Break, subset-sum) — auto-false
boolean[] dp = new boolean[n + 1];
dp[0] = true;

// 4️⃣ Top-down memo with HashMap when the state isn't a simple index
Map<String, Integer> memo = new HashMap<>();
String key = i + "," + j;             // encode multi-dimensional state
if (memo.containsKey(key)) return memo.get(key);
// ... compute ...
memo.put(key, result);

// 5️⃣ Use Integer[] (not int[]) when you need null to mean "not yet computed".
//    int[] defaults to 0, which is ambiguous if 0 is a valid answer.
Integer[] memo = new Integer[n + 1];  // null = uncomputed, distinct from 0
if (memo[i] != null) return memo[i];

// 6️⃣ Watch for OVERFLOW on "count ways" problems — use long, or mod 1e9+7 if asked
long[] dp = new long[n + 1];

// 7️⃣ Careful with String.substring(start, end): end is EXCLUSIVE.
//    s.substring(2, 5) gives chars at indices 2,3,4 (length 3).

// 8️⃣ Math.min / Math.max take exactly 2 args — nest them for 3-way:
int best = Math.min(a, Math.min(b, c));
```

> ⚠️ **Common Java DP bugs to avoid:**
> - Using `int` for "infinity" then doing `MAX_VALUE + 1` → **overflow** to negative. Use `amount + 1` or `n + 1` as your sentinel instead.
> - Off-by-one between **string index** (`s.charAt(i-1)`) and **dp index** (`dp[i]`). Keep the convention: `dp[i]` = answer for the **first `i` characters**.
> - Forgetting the **base case** initialization (e.g., `dp[0] = true`/`0`/`1`). The whole table builds on it.

---

# ✅ Final Interview Checklist

Before you say "I'm done," tick every box:

- [ ] 🗣️ **Recognized it as DP out loud** — named overlapping subproblems + optimal substructure.
- [ ] ❓ **Asked clarifying questions** — empty input, bounds, value-vs-reconstruction, overflow.
- [ ] 📝 **Defined the state in one sentence** — *"`dp[i]` means ___."*
- [ ] 🔁 **Wrote the recurrence explicitly** before coding, and stated the **base case**.
- [ ] 🐢 **Started with brute-force recursion** and pointed out the repeated work.
- [ ] ⚡ **Upgraded to memoization or tabulation**, explaining the speedup.
- [ ] 📉 **Mentioned space optimization** (rolling row / two variables) — even if you don't code it.
- [ ] 🧪 **Dry-ran a small example** by hand to prove correctness.
- [ ] 🧮 **Stated final Time & Space complexity** clearly.
- [ ] 🪲 **Checked edge cases** — empty string/array, single element, no valid answer (return `-1`/`false`/`0`).
- [ ] 🧩 **Verified the answer location** — is it `dp[n]`, `dp[m][n]`, or `max(dp[i])`? (LIS trips people up here!)

> 🎤 **Closing line for the interviewer:** *"The brute force was exponential because of overlapping subproblems; by caching each state once I brought it down to polynomial time, and I can shave the space to O(n) by keeping only the rows I still need."* — That sentence is the soul of every DP round at Microsoft. 🚀
