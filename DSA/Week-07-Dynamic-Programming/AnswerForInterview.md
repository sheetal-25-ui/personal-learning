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

### 🧠 Algorithm to Remember Forever
- **Pattern:** *Pick-or-skip 1D DP* (a cousin of 0/1 knapsack on a line).
- **Recurrence (one line):** `dp[i] = max(dp[i-1], nums[i] + dp[i-2])`.
- **Memory hook:** 🥷 *A ninja burglar who can't hit two doors in a row* — at each door, "grab it + the loot from two doors back" vs "tiptoe past."
- **Trigger phrase:** *"Max sum with no two adjacent elements."*

---

## 3. House Robber II 🏠🔄 *(Microsoft — the circular twist)*

### 📋 Full Question
> Same as House Robber, but the houses are arranged in a **circle** — so the **first and last houses are adjacent**. Robbing both triggers the alarm. Return the maximum money you can rob.
>
> **Input:** `nums = [2, 3, 2]` → **Output:** `3`
> (You can't rob house 0 and house 2 (they're now neighbors), so the best single grab is house 1 = 3.)

### 🗣️ What to say first
- "The only new constraint is: **house 0 and house n-1 can't both be robbed**."
- "**Trick:** split into two independent linear House-Robber problems and take the max:
   1. rob houses `[0 .. n-2]` (allow first, exclude last)
   2. rob houses `[1 .. n-1]` (exclude first, allow last)."
- "Edge case: if there's only **one house**, just return it (the windows would be empty otherwise)."

### Approach 1 — Brute force recursion
```java
// Exponential: try both "exclude last" and "exclude first" universes,
// each explored with the same rob/skip branching as House Robber I.
public int rob(int[] nums) {
    int n = nums.length;
    if (n == 1) return nums[0]; // single house: no circular conflict
    int excludeLast  = robRange(nums, 0, n - 2, 0); // houses 0..n-2
    int excludeFirst = robRange(nums, 1, n - 1, 1); // houses 1..n-1
    return Math.max(excludeLast, excludeFirst);
}
// Recurse over a fixed inclusive range [lo..hi]
private int robRange(int[] nums, int lo, int hi, int i) {
    if (i > hi) return 0;
    int robThis  = nums[i] + robRange(nums, lo, hi, i + 2);
    int skipThis = robRange(nums, lo, hi, i + 1);
    return Math.max(robThis, skipThis);
}
// Time: O(2^n)  |  Space: O(n) stack
```

### Approach 2 — Optimal (run the O(1) linear robber twice)
**Recurrence (per range):** `dp[i] = max(dp[i-1], nums[i] + dp[i-2])`, applied once excluding the last house and once excluding the first.

```java
public int rob(int[] nums) {
    int n = nums.length;
    if (n == 1) return nums[0];
    // Case A: consider houses [0 .. n-2]; Case B: houses [1 .. n-1]
    return Math.max(robLinear(nums, 0, n - 2),
                    robLinear(nums, 1, n - 1));
}
// Standard linear House Robber over inclusive indices [lo, hi]
private int robLinear(int[] nums, int lo, int hi) {
    int prev2 = 0, prev1 = 0;
    for (int i = lo; i <= hi; i++) {
        int cur = Math.max(prev1, nums[i] + prev2);
        prev2 = prev1;
        prev1 = cur;
    }
    return prev1;
}
// Time: O(n)  |  Space: O(1)
```

**Dry-run (`[2,3,2]`):** robLinear over `[0..1]` = max(2,3) = 3; robLinear over `[1..2]` = max(3,2) = 3 → answer **3** ✅

### 🧠 Algorithm to Remember Forever
- **Pattern:** *Circular 1D DP = "run the linear DP twice, drop one endpoint each time."*
- **Recurrence (one line):** `answer = max(rob[0..n-2], rob[1..n-1])`.
- **Memory hook:** 🔄 *Cut the necklace* — break the circle at one bead and solve the line; do it from both cut points and keep the richer loot.
- **Trigger phrase:** *"Same DP but the array is circular / wraps around."*

---

## 4. Coin Change 🪙 *(Microsoft — VERY FREQUENT! Must solve in 10 min)*

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

## 5. Longest Increasing Subsequence (LIS) 📈 *(Microsoft asked)*

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

## 6. Word Break 📖 *(Microsoft — Frequent)*

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

## 7. Unique Paths 🗺️ *(Microsoft — Classic grid DP)*

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

## 8. Longest Common Subsequence (LCS) 🔗 *(Microsoft asked — revision must-know)*

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

## 9. Edit Distance ✏️ *(Microsoft CLASSIC — appears constantly!)*

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

## 10. Partition Equal Subset Sum 🎒 *(Microsoft — 0/1 Knapsack flavor)*

### 📋 Full Question
> Given a non-empty array `nums` of positive integers, decide whether it can be **partitioned into two subsets with equal sum**.
>
> **Input:** `nums = [1, 5, 11, 5]` → **Output:** `true`
> (`[1, 5, 5]` and `[11]` both sum to 11.)

### 🗣️ What to say first
- "If the total sum is **odd**, it's impossible — return false immediately."
- "Otherwise let `target = sum / 2`. The question reduces to: **can I pick a subset that sums exactly to `target`?**"
- "**State:** `dp[s]` = can I form sum `s` using some subset of the numbers seen so far?"
- "**Recurrence (0/1 knapsack):** for each `num`, iterate `s` from `target` **down to** `num`: `dp[s] = dp[s] || dp[s - num]`."
- "⚠️ The inner loop goes **downward** so each number is used **at most once** (that's what makes it 0/1, not unbounded)."

### Approach 1 — Brute force recursion (include / exclude each number)
```java
// Exponential: at each index, take it toward the target or skip it
public boolean canPartition(int[] nums) {
    int sum = 0;
    for (int x : nums) sum += x;
    if (sum % 2 != 0) return false; // odd total can't split evenly
    return subsetSum(nums, 0, sum / 2);
}
private boolean subsetSum(int[] nums, int i, int remaining) {
    if (remaining == 0) return true;          // found a subset
    if (remaining < 0 || i == nums.length) return false;
    // take nums[i] OR skip it
    return subsetSum(nums, i + 1, remaining - nums[i])
        || subsetSum(nums, i + 1, remaining);
}
// Time: O(2^n)  |  Space: O(n) stack
```

### Approach 2 — Optimal (1D boolean knapsack)
**Recurrence:** `dp[s] = dp[s] || dp[s - num]`, with `dp[0] = true`, looping `s` from `target` down to `num`.

```java
public boolean canPartition(int[] nums) {
    int sum = 0;
    for (int x : nums) sum += x;
    if (sum % 2 != 0) return false;
    int target = sum / 2;
    boolean[] dp = new boolean[target + 1];
    dp[0] = true; // sum 0 is always achievable (empty subset)
    for (int num : nums) {
        // Iterate DOWNWARD so each num is used at most once (0/1 knapsack)
        for (int s = target; s >= num; s--) {
            dp[s] = dp[s] || dp[s - num];
        }
    }
    return dp[target];
}
// Time: O(n × target)  |  Space: O(target)
```

**Dry-run (`[1,5,11,5]`, target=11):** start `dp[0]=T`.
After 1 → reachable {0,1}. After 5 → {0,1,5,6}. After 11 → {…,11} becomes true → returns **true** ✅

### 🧠 Algorithm to Remember Forever
- **Pattern:** *0/1 Knapsack (subset-sum decision)*.
- **Recurrence (one line):** `dp[s] = dp[s] || dp[s - num]` (loop s downward).
- **Memory hook:** 🎒 *Packing a bag to an exact weight* — each item is either in or out (once), and you sweep capacities top-down so you don't reuse it.
- **Trigger phrase:** *"Can we pick a subset that hits an exact target sum?"*

---

## 11. Maximal Square ⬛ *(Microsoft — grid DP with a twist)*

### 📋 Full Question
> Given an `m × n` binary matrix filled with `'0'` and `'1'`, find the **largest square containing only `1`s** and return its **area**.
>
> **Input:**
> ```
> 1 0 1 0 0
> 1 0 1 1 1
> 1 1 1 1 1
> 1 0 0 1 0
> ```
> → **Output:** `4` (a 2×2 square of 1s, area = 2² = 4).

### 🗣️ What to say first
- "**State:** `dp[i][j]` = the **side length** of the largest all-1 square whose **bottom-right corner** is at `(i, j)`."
- "**Recurrence:** if `matrix[i][j] == '1'`, then `dp[i][j] = 1 + min(dp[i-1][j], dp[i][j-1], dp[i-1][j-1])` — a square can only grow if its top, left, and top-left neighbors all support it. If the cell is `'0'`, `dp[i][j] = 0`."
- "Track the **max side** seen; answer is `maxSide²`."

### Approach 1 — Brute force (try every square)
```java
// O((m·n)·min(m,n)^2): for each cell try growing squares and verify all 1s
public int maximalSquare(char[][] matrix) {
    int m = matrix.length, n = matrix[0].length, best = 0;
    for (int i = 0; i < m; i++) {
        for (int j = 0; j < n; j++) {
            int maxSide = Math.min(m - i, n - j);
            for (int side = 1; side <= maxSide; side++) {
                if (allOnes(matrix, i, j, side)) best = Math.max(best, side);
                else break; // larger squares would also fail
            }
        }
    }
    return best * best;
}
private boolean allOnes(char[][] m, int r, int c, int side) {
    for (int i = r; i < r + side; i++)
        for (int j = c; j < c + side; j++)
            if (m[i][j] != '1') return false;
    return true;
}
// Time: O(m·n·min(m,n)²)  |  Space: O(1)
```

### Approach 2 — Optimal (2D DP)
**Recurrence:** `dp[i][j] = 1 + min(dp[i-1][j], dp[i][j-1], dp[i-1][j-1])` when `matrix[i][j] == '1'`, else `0`.

```java
public int maximalSquare(char[][] matrix) {
    int m = matrix.length, n = matrix[0].length;
    // Pad with a top row & left column of zeros so i-1 / j-1 are always valid.
    int[][] dp = new int[m + 1][n + 1];
    int maxSide = 0;
    for (int i = 1; i <= m; i++) {
        for (int j = 1; j <= n; j++) {
            if (matrix[i - 1][j - 1] == '1') {
                dp[i][j] = 1 + Math.min(dp[i - 1][j],
                              Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
                maxSide = Math.max(maxSide, dp[i][j]);
            }
        }
    }
    return maxSide * maxSide; // area = side²
}
// Time: O(m × n)  |  Space: O(m × n)  (reducible to O(n) with a rolling row)
```

**Dry-run (the 4×5 grid above):** the `dp` reaches a `2` at the cell where a 2×2 block of 1s bottoms out → `maxSide = 2` → area **4** ✅

### 🧠 Algorithm to Remember Forever
- **Pattern:** *Grid DP (min-of-three neighbors)*.
- **Recurrence (one line):** `dp[i][j] = 1 + min(up, left, up-left)` if cell is 1.
- **Memory hook:** ⬛ *A square only grows as big as its weakest corner* — top, left, and diagonal must all already support a square.
- **Trigger phrase:** *"Largest square of 1s / square sub-matrix."*

---

## 12. Decode Ways 🔢 *(Microsoft — sneaky 1D DP)*

### 📋 Full Question
> A message of digits is encoded with `A→1, B→2, …, Z→26`. Given a digit string `s`, return the **number of ways to decode** it.
>
> **Input:** `s = "226"` → **Output:** `3`
> (`"2 2 6"` = BBF, `"22 6"` = VF, `"2 26"` = BZ.)

### 🗣️ What to say first
- "**State:** `dp[i]` = number of ways to decode the first `i` characters (`s[0..i-1]`)."
- "**Base case:** `dp[0] = 1` (empty string decodes one way — the empty decoding)."
- "**Recurrence:** at position `i`, I can take **one digit** `s[i-1]` if it's `'1'..'9'` (not `'0'`) → add `dp[i-1]`; and I can take **two digits** `s[i-2..i-1]` if that number is `10..26` → add `dp[i-2]`."
- "⚠️ Leading `'0'` is the trap — `'0'` can't stand alone and only valid inside `10` or `20`."

### Approach 1 — Brute force recursion
```java
// Exponential: at each index, decode 1 digit or 2 digits
public int numDecodings(String s) {
    return decode(s, 0);
}
private int decode(String s, int i) {
    if (i == s.length()) return 1;       // reached the end = 1 valid decoding
    if (s.charAt(i) == '0') return 0;    // a leading 0 can't be decoded
    int ways = decode(s, i + 1);         // take a single digit
    if (i + 1 < s.length()) {
        int two = Integer.parseInt(s.substring(i, i + 2));
        if (two <= 26) ways += decode(s, i + 2); // take two digits if 10..26
    }
    return ways;
}
// Time: O(2^n)  |  Space: O(n) stack
```

### Approach 2 — Optimal (bottom-up, space-optimized)
**Recurrence:** `dp[i] = (s[i-1]!='0' ? dp[i-1] : 0) + (10 <= s[i-2..i-1] <= 26 ? dp[i-2] : 0)`.

```java
public int numDecodings(String s) {
    if (s == null || s.isEmpty() || s.charAt(0) == '0') return 0;
    int n = s.length();
    int prev2 = 1; // dp[0]: empty string -> 1 way
    int prev1 = 1; // dp[1]: first char valid (checked above) -> 1 way
    for (int i = 2; i <= n; i++) {
        int cur = 0;
        if (s.charAt(i - 1) != '0') cur += prev1;              // single digit 1..9
        int two = (s.charAt(i - 2) - '0') * 10 + (s.charAt(i - 1) - '0');
        if (two >= 10 && two <= 26) cur += prev2;              // two-digit 10..26
        prev2 = prev1;
        prev1 = cur;
    }
    return prev1;
}
// Time: O(n)  |  Space: O(1)
```

**Dry-run (`"226"`):** dp[0]=1, dp[1]=1 (`"2"`). i=2 (`"22"`): single '2' → +1, two=22≤26 → +1 ⇒ dp=2. i=3 (`"226"`): single '6' → +2, two=26≤26 → +1 ⇒ **dp=3** ✅

### 🧠 Algorithm to Remember Forever
- **Pattern:** *1D ways-with-constraint DP* (Fibonacci shape, but each term is gated by a validity check).
- **Recurrence (one line):** `dp[i] = dp[i-1]·valid1 + dp[i-2]·valid2`.
- **Memory hook:** 🔢 *Reading a digit code* — at each step you may "speak one digit" or "speak two digits," and you sum the ways, skipping illegal `0`s.
- **Trigger phrase:** *"Count the number of ways to decode / interpret a string."*

---

## 13. Longest Palindromic Substring 🪞 *(Microsoft — Frequent!)*

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

> 🧱 **2D DP alternative** (`O(n²)` time/space): `dp[i][j]` = is `s[i..j]` a palindrome; `dp[i][j] = (s[i]==s[j]) && (j-i<2 || dp[i+1][j-1])`. Fill by increasing length. Mention it, but expand-around-center is cleaner.

### 🧠 Algorithm to Remember Forever
- **Pattern:** *Expand-around-center* (a 2D-DP-equivalent done in O(1) space).
- **Recurrence (one line):** `palindrome(i,j) = s[i]==s[j] && palindrome(i+1,j-1)`.
- **Memory hook:** 🪞 *Mirror, mirror* — stand at each character (and each gap) and push two mirrors outward until they disagree.
- **Trigger phrase:** *"Longest palindromic substring / contiguous palindrome."*

---

## 14. Regular Expression Matching 🧬 *(Microsoft — Hard 2D DP)*

### 📋 Full Question
> Implement regex matching for `.` (matches **any single char**) and `*` (matches **zero or more** of the **preceding** element). The match must cover the **entire** input string `s`.
>
> **Input:** `s = "aab"`, `p = "c*a*b"` → **Output:** `true`
> (`c*` matches zero `c`s, `a*` matches `aa`, `b` matches `b`.)

### 🗣️ What to say first
- "**State:** `dp[i][j]` = does `s`'s first `i` chars match `p`'s first `j` chars?"
- "**Base case:** `dp[0][0] = true` (empty matches empty). For empty `s`, a pattern like `a*b*` can still match via the `*`-eats-zero rule."
- "**Recurrence:**
   - If `p[j-1]` is a normal char or `.`: it must match `s[i-1]` → `dp[i][j] = dp[i-1][j-1] && (p[j-1]=='.' || p[j-1]==s[i-1])`.
   - If `p[j-1] == '*'`: two cases — **zero** of the preceding char → `dp[i][j-2]`; **one or more** → `dp[i-1][j]` provided the preceding pattern char matches `s[i-1]`."

### Approach 1 — Brute force recursion
```java
// Exponential: '*' branches into "use zero" vs "consume one and stay"
public boolean isMatch(String s, String p) {
    return match(s, p, 0, 0);
}
private boolean match(String s, String p, int i, int j) {
    if (j == p.length()) return i == s.length(); // pattern exhausted
    boolean firstMatch = i < s.length()
        && (p.charAt(j) == s.charAt(i) || p.charAt(j) == '.');
    if (j + 1 < p.length() && p.charAt(j + 1) == '*') {
        // skip "x*" (zero occurrences) OR consume one matching char of s
        return match(s, p, i, j + 2)
            || (firstMatch && match(s, p, i + 1, j));
    }
    return firstMatch && match(s, p, i + 1, j + 1);
}
// Time: O(2^(m+n)) worst case  |  Space: O(m+n) stack
```

### Approach 2 — Optimal (2D tabulation)
**Recurrence (see above):** normal/`.` → diagonal AND char match; `*` → `dp[i][j-2]` OR (preceding matches AND `dp[i-1][j]`).

```java
public boolean isMatch(String s, String p) {
    int m = s.length(), n = p.length();
    boolean[][] dp = new boolean[m + 1][n + 1];
    dp[0][0] = true; // empty s matches empty p
    // Empty s vs patterns like a* / a*b* — '*' can erase its preceding char.
    for (int j = 1; j <= n; j++) {
        if (p.charAt(j - 1) == '*') dp[0][j] = dp[0][j - 2];
    }
    for (int i = 1; i <= m; i++) {
        for (int j = 1; j <= n; j++) {
            char pc = p.charAt(j - 1);
            if (pc == '*') {
                // Zero occurrences of the char before '*'
                dp[i][j] = dp[i][j - 2];
                // One+ occurrences: preceding pattern char must match s[i-1]
                char prev = p.charAt(j - 2);
                if (prev == '.' || prev == s.charAt(i - 1)) {
                    dp[i][j] = dp[i][j] || dp[i - 1][j];
                }
            } else if (pc == '.' || pc == s.charAt(i - 1)) {
                dp[i][j] = dp[i - 1][j - 1]; // single char matches
            }
        }
    }
    return dp[m][n];
}
// Time: O(m × n)  |  Space: O(m × n)
```

**Dry-run (`s="aab"`, `p="c*a*b"`):** `c*` lets `dp[0][2]=true`; `a*` absorbs `aa`; final `b` matches `b` → `dp[3][5] = true` ✅

### 🧠 Algorithm to Remember Forever
- **Pattern:** *Two-string 2D DP with branching (regex / wildcard matching)*.
- **Recurrence (one line):** `'*' → dp[i][j-2] (zero) || (preceding matches && dp[i-1][j]) (more)`.
- **Memory hook:** 🧬 *The star is a copy machine* — `x*` can print zero copies (skip two pattern chars) or one more copy (eat a char of `s` and stay put).
- **Trigger phrase:** *"Pattern / wildcard / regex matching with `*`."*

---

## 15. Minimum Path Sum 🧭 *(Microsoft — grid min-cost)*

### 📋 Full Question
> Given an `m × n` grid of non-negative numbers, find a path from **top-left to bottom-right** that **minimizes the sum** of numbers along the path. You can only move **right or down**.
>
> **Input:** `grid = [[1,3,1],[1,5,1],[4,2,1]]` → **Output:** `7`
> (Path `1→3→1→1→1` sums to 7.)

### 🗣️ What to say first
- "**State:** `dp[i][j]` = minimum cost to reach cell `(i, j)`."
- "**Base case:** `dp[0][0] = grid[0][0]`; first row and first column accumulate left-to-right / top-to-bottom (only one way in)."
- "**Recurrence:** `dp[i][j] = grid[i][j] + min(dp[i-1][j], dp[i][j-1])`."
- "Sibling of Unique Paths — same grid shape, but **min** instead of **sum-of-ways**."

### Approach 1 — Brute force recursion
```java
// Exponential: branch right/down and add costs
public int minPathSum(int[][] grid) {
    return path(grid, 0, 0);
}
private int path(int[][] g, int i, int j) {
    int m = g.length, n = g[0].length;
    if (i == m - 1 && j == n - 1) return g[i][j]; // reached destination
    if (i >= m || j >= n) return Integer.MAX_VALUE; // off the grid
    int down  = path(g, i + 1, j);
    int right = path(g, i, j + 1);
    return g[i][j] + Math.min(down, right);
}
// Time: O(2^(m+n))  |  Space: O(m+n) stack
```

### Approach 2 — Optimal (in-place / rolling-row DP)
**Recurrence:** `dp[i][j] = grid[i][j] + min(dp[i-1][j], dp[i][j-1])`.

```java
// O(n) space: a single row carries "min cost to reach this column"
public int minPathSum(int[][] grid) {
    int m = grid.length, n = grid[0].length;
    int[] dp = new int[n];
    dp[0] = grid[0][0];
    for (int j = 1; j < n; j++) dp[j] = dp[j - 1] + grid[0][j]; // first row
    for (int i = 1; i < m; i++) {
        dp[0] += grid[i][0]; // first column: only "from above"
        for (int j = 1; j < n; j++) {
            // dp[j] (old) = from above; dp[j-1] = from the left
            dp[j] = grid[i][j] + Math.min(dp[j], dp[j - 1]);
        }
    }
    return dp[n - 1];
}
// Time: O(m × n)  |  Space: O(n)
```

**Dry-run (`[[1,3,1],[1,5,1],[4,2,1]]`):** first row → `[1,4,5]`; row 1 → `[2,7,6]`; row 2 → `[6,8,7]` → answer **7** ✅

### 🧠 Algorithm to Remember Forever
- **Pattern:** *Grid DP (min-cost path)*.
- **Recurrence (one line):** `dp[i][j] = grid[i][j] + min(up, left)`.
- **Memory hook:** 🧭 *Cheapest hike downhill-right* — each cell's cost = its own toll + the cheaper of the two doors you could have entered from.
- **Trigger phrase:** *"Minimum cost path in a grid, right/down."*

---

## 16. Target Sum 🎯 *(Microsoft — subset-sum in disguise)*

### 📋 Full Question
> Given `nums` and a target `T`, assign a `+` or `-` sign to **each** number so the resulting expression equals `T`. Return the **number of ways** to do it.
>
> **Input:** `nums = [1, 1, 1, 1, 1]`, `T = 3` → **Output:** `5`
> (e.g. `-1+1+1+1+1`, `+1-1+1+1+1`, … five sign assignments sum to 3.)

### 🗣️ What to say first
- "Let `P` = the subset assigned `+`, `N` = the subset assigned `-`. Then `sum(P) - sum(N) = T` and `sum(P) + sum(N) = total`."
- "Adding them: `sum(P) = (total + T) / 2`. So the problem becomes: **count subsets that sum to `(total + T) / 2`** — a classic 0/1 subset-sum **counting** DP!"
- "Guard: if `(total + T)` is **odd** or `|T| > total`, answer is `0` (no valid assignment)."
- "**State:** `dp[s]` = number of subsets summing to `s`. **Recurrence:** `dp[s] += dp[s - num]`, looping `s` downward."

### Approach 1 — Brute force recursion (try ± on each number)
```java
// Exponential: 2 branches (plus / minus) per element
public int findTargetSumWays(int[] nums, int target) {
    return ways(nums, 0, 0, target);
}
private int ways(int[] nums, int i, int running, int target) {
    if (i == nums.length) return running == target ? 1 : 0;
    // assign + then -
    return ways(nums, i + 1, running + nums[i], target)
         + ways(nums, i + 1, running - nums[i], target);
}
// Time: O(2^n)  |  Space: O(n) stack
```

### Approach 2 — Optimal (count subset-sum, 1D DP)
**Recurrence:** `dp[s] += dp[s - num]` (loop `s` from `subsetTarget` down to `num`), with `dp[0] = 1`.

```java
public int findTargetSumWays(int[] nums, int target) {
    int total = 0;
    for (int x : nums) total += x;
    // Need subset sum P with P = (total + target) / 2
    if (Math.abs(target) > total || (total + target) % 2 != 0) return 0;
    int subset = (total + target) / 2;
    int[] dp = new int[subset + 1];
    dp[0] = 1; // one way to make sum 0: choose nothing
    for (int num : nums) {
        for (int s = subset; s >= num; s--) { // downward => 0/1 (each num once)
            dp[s] += dp[s - num];
        }
    }
    return dp[subset];
}
// Time: O(n × subset)  |  Space: O(subset)
```

**Dry-run (`[1,1,1,1,1]`, T=3):** total=5, subset=(5+3)/2=4. Counting subsets of five 1s that sum to 4 = "choose 4 of the 5 ones" = **5** ✅

### 🧠 Algorithm to Remember Forever
- **Pattern:** *0/1 Knapsack — counting variant (subset-sum count).*
- **Recurrence (one line):** `dp[s] += dp[s - num]` after reducing to subset sum `(total+T)/2`.
- **Memory hook:** 🎯 *Flip the minus signs into a bucket* — the `-` group's size is fixed by algebra, so just count how many ways to fill the `+` bucket.
- **Trigger phrase:** *"Assign +/- signs to hit a target — count the ways."*

---

## 17. Palindrome Partitioning (Min Cuts) ✂️ *(Microsoft — interval/partition DP)*

### 📋 Full Question
> Given a string `s`, partition it so that **every** substring is a palindrome. Return the **minimum number of cuts** needed.
>
> **Input:** `s = "aab"` → **Output:** `1`
> (Cut once: `"aa" | "b"` — both palindromes.)
>
> *(Note: the LeetCode "Palindrome Partitioning I" asks to enumerate all partitions via backtracking; this DP "min-cuts" version — Partitioning II — is the canonical DP interview question. I'll show both the DP and mention the enumeration.)*

### 🗣️ What to say first
- "**Precompute** a palindrome table `isPal[i][j]` = is `s[i..j]` a palindrome, in O(n²)."
- "**State:** `cuts[i]` = minimum cuts needed for the prefix `s[0..i]`."
- "**Recurrence:** `cuts[i] = min over j (0..i) of ( s[j..i] is palindrome ? (j==0 ? 0 : cuts[j-1] + 1) )`."
- "If the whole prefix `s[0..i]` is already a palindrome, `cuts[i] = 0`."

### Approach 1 — Brute force recursion
```java
// Exponential: try every cut position; recurse on the remainder
public int minCut(String s) {
    return cut(s, 0);
}
private int cut(String s, int start) {
    if (start == s.length()) return -1; // no cut needed past the end (offsets +1 below)
    int best = Integer.MAX_VALUE;
    for (int end = start; end < s.length(); end++) {
        if (isPalindrome(s, start, end)) {
            best = Math.min(best, 1 + cut(s, end + 1));
        }
    }
    return best;
}
private boolean isPalindrome(String s, int lo, int hi) {
    while (lo < hi) if (s.charAt(lo++) != s.charAt(hi--)) return false;
    return true;
}
// Time: O(2^n) worst case  |  Space: O(n) stack
```

### Approach 2 — Optimal (palindrome table + 1D cuts DP)
**Recurrence:** `cuts[i] = 0` if `s[0..i]` is a palindrome, else `min(cuts[j-1] + 1)` over all `j` where `s[j..i]` is a palindrome.

```java
public int minCut(String s) {
    int n = s.length();
    // 1) Precompute palindrome substrings: isPal[i][j] = s[i..j] palindrome?
    boolean[][] isPal = new boolean[n][n];
    for (int j = 0; j < n; j++) {
        for (int i = j; i >= 0; i--) {
            if (s.charAt(i) == s.charAt(j) && (j - i < 2 || isPal[i + 1][j - 1])) {
                isPal[i][j] = true;
            }
        }
    }
    // 2) cuts[i] = min cuts for prefix s[0..i]
    int[] cuts = new int[n];
    for (int i = 0; i < n; i++) {
        if (isPal[0][i]) {
            cuts[i] = 0; // whole prefix is a palindrome, no cut needed
        } else {
            cuts[i] = i; // worst case: cut before every char
            for (int j = 1; j <= i; j++) {
                if (isPal[j][i]) {
                    cuts[i] = Math.min(cuts[i], cuts[j - 1] + 1);
                }
            }
        }
    }
    return cuts[n - 1];
}
// Time: O(n²)  |  Space: O(n²)
```

**Dry-run (`"aab"`):** isPal: `"aa"` true. cuts[0]=0 (`"a"`), cuts[1]=0 (`"aa"` palindrome), cuts[2]: `"aab"` not palindrome → try `j=2` (`"b"` pal) → `cuts[1]+1 = 1` → **1** ✅

> 🧩 **Partitioning I (enumerate all)**: backtrack — at each start, try every end where `s[start..end]` is a palindrome, recurse on the rest, collect lists. That's `O(2^n)` partitions, exponential by nature.

### 🧠 Algorithm to Remember Forever
- **Pattern:** *Partition DP (1D cuts) on top of a precomputed palindrome table*.
- **Recurrence (one line):** `cuts[i] = min(cuts[j-1] + 1)` over palindromic suffixes `s[j..i]`.
- **Memory hook:** ✂️ *Snip the string into mirror pieces* — first know which slices are palindromes, then find the fewest snips.
- **Trigger phrase:** *"Minimum cuts / partitions so each piece satisfies a property."*

---

## 18. Burst Balloons 🎈 *(Microsoft — Hard interval DP)*

### 📋 Full Question
> You have `n` balloons with values `nums`. Bursting balloon `i` earns `nums[left] * nums[i] * nums[right]` coins, where `left`/`right` are the **currently adjacent** balloons (treat out-of-bounds as `1`). Return the **maximum** coins from bursting all balloons.
>
> **Input:** `nums = [3, 1, 5, 8]` → **Output:** `167`
> (Burst order 1→5→3→8 yields 3·1·5 + 3·5·8 + 1·3·8 + 1·8·1 = 15+120+24+8 = 167.)

### 🗣️ What to say first
- "The trick: instead of asking 'which balloon to burst **first**', ask 'which balloon to burst **LAST** in a range'."
- "Pad `nums` with `1`s on both ends → `arr`. **State:** `dp[i][j]` = max coins bursting **all balloons strictly between** `i` and `j` (exclusive boundaries)."
- "**Recurrence:** pick `k` as the **last** balloon to pop in `(i, j)`. When `k` is last, its neighbors are exactly `arr[i]` and `arr[j]` (everything else inside is gone). So:
   `dp[i][j] = max over i<k<j of ( dp[i][k] + arr[i]*arr[k]*arr[j] + dp[k][j] )`."
- "This is **interval DP** — fill by increasing range length."

### Approach 1 — Brute force recursion (try every burst order)
```java
// Exponential: try bursting each remaining balloon first (n! orderings)
public int maxCoins(int[] nums) {
    List<Integer> balloons = new ArrayList<>();
    for (int x : nums) balloons.add(x);
    return burst(balloons);
}
private int burst(List<Integer> b) {
    if (b.isEmpty()) return 0;
    int best = 0;
    for (int i = 0; i < b.size(); i++) {
        int left  = i - 1 >= 0 ? b.get(i - 1) : 1;
        int right = i + 1 < b.size() ? b.get(i + 1) : 1;
        int gain = left * b.get(i) * right;
        List<Integer> next = new ArrayList<>(b);
        next.remove(i); // burst balloon i
        best = Math.max(best, gain + burst(next));
    }
    return best;
}
// Time: O(n!) — astronomically slow  |  Space: O(n) depth
```

### Approach 2 — Optimal (interval DP, "last to burst")
**Recurrence:** `dp[i][j] = max_{i<k<j} ( dp[i][k] + arr[i]*arr[k]*arr[j] + dp[k][j] )`.

```java
public int maxCoins(int[] nums) {
    int n = nums.length;
    // Pad with virtual 1-valued balloons at both ends.
    int[] arr = new int[n + 2];
    arr[0] = arr[n + 1] = 1;
    for (int i = 0; i < n; i++) arr[i + 1] = nums[i];

    // dp[i][j] = max coins from bursting all balloons strictly between i and j.
    int[][] dp = new int[n + 2][n + 2];
    // Iterate by interval length; len = number of balloons inside (i, j)
    for (int len = 1; len <= n; len++) {
        for (int i = 0; i + len + 1 <= n + 1; i++) {
            int j = i + len + 1; // right boundary (exclusive interior)
            for (int k = i + 1; k < j; k++) { // k = LAST balloon burst in (i, j)
                int coins = arr[i] * arr[k] * arr[j]; // k's neighbors are now i and j
                dp[i][j] = Math.max(dp[i][j],
                                    dp[i][k] + coins + dp[k][j]);
            }
        }
    }
    return dp[0][n + 1];
}
// Time: O(n³)  |  Space: O(n²)
```

**Dry-run (`[3,1,5,8]`):** padded `arr = [1,3,1,5,8,1]`. Filling intervals of growing length, the optimal "last burst" choices build up to `dp[0][5] = 167` ✅

### 🧠 Algorithm to Remember Forever
- **Pattern:** *Interval DP ("last to burst / last to multiply")*.
- **Recurrence (one line):** `dp[i][j] = max_k( dp[i][k] + arr[i]*arr[k]*arr[j] + dp[k][j] )`.
- **Memory hook:** 🎈 *Save the best balloon for last* — flip the question from "first" to "last," because the last balloon in a range has clean, fixed neighbors.
- **Trigger phrase:** *"Pick an order over a range to maximize/minimize a combine cost"* (also matrix-chain, stone-merge).

---

# 📊 Complexity Cheat Sheet

| Problem | Brute Recursion | Optimal DP | DP Pattern |
|---------|-----------------|------------|------------|
| Climbing Stairs | O(2ⁿ) | **O(n) time, O(1) space** | Fibonacci 1D |
| House Robber | O(2ⁿ) | **O(n) time, O(1) space** | Pick/skip 1D |
| House Robber II | O(2ⁿ) | **O(n) time, O(1) space** | Circular 1D (run twice) |
| Coin Change | O(coinsᵃᵐᵒᵘⁿᵗ) | **O(amount × coins), O(amount)** | Unbounded knapsack |
| Longest Increasing Subseq | O(2ⁿ) | **O(n²)** (or O(n log n)) | LIS / ending-here |
| Word Break | O(2ⁿ) | **O(n²·L), O(n)** | Prefix partition |
| Unique Paths | O(2^(m+n)) | **O(m·n), O(n)** | Grid count-ways |
| Longest Common Subseq | O(2^(m+n)) | **O(m·n)** | LCS 2D |
| Edit Distance | O(3^(m+n)) | **O(m·n)** | Levenshtein 2D |
| Partition Equal Subset Sum | O(2ⁿ) | **O(n·sum), O(sum)** | 0/1 knapsack (decision) |
| Maximal Square | O(m·n·min(m,n)²) | **O(m·n), O(n)** | Grid min-of-3 |
| Decode Ways | O(2ⁿ) | **O(n), O(1)** | 1D ways-with-constraint |
| Longest Palindromic Substr | O(n³) | **O(n²), O(1)** | Expand-around-center |
| Regular Expression Matching | O(2^(m+n)) | **O(m·n)** | 2D string DP (branching) |
| Minimum Path Sum | O(2^(m+n)) | **O(m·n), O(n)** | Grid min-cost |
| Target Sum | O(2ⁿ) | **O(n·sum), O(sum)** | 0/1 knapsack (count) |
| Palindrome Partitioning (cuts) | O(2ⁿ) | **O(n²)** | Partition DP + pal table |
| Burst Balloons | O(n!) | **O(n³), O(n²)** | Interval DP (last to burst) |

---

# 🧠🧠 DP Pattern Recognition Table

| If the question says… (trigger) | → DP Pattern | State / Recurrence | Big-O |
|---------------------------------|--------------|--------------------|-------|
| "How many **ways** to reach the top/end?" | Fibonacci / count-ways 1D | `dp[i] = dp[i-1] + dp[i-2]` | O(n) |
| "**Max sum**, no two **adjacent**" | Pick-or-skip 1D | `dp[i] = max(dp[i-1], nums[i]+dp[i-2])` | O(n) |
| "Same DP but the array is **circular**" | Circular 1D (run twice) | `max(rob[0..n-2], rob[1..n-1])` | O(n) |
| "**Min coins / items**, unlimited supply" | Unbounded knapsack | `dp[a] = min(dp[a], dp[a-coin]+1)` | O(n·k) |
| "**Can we partition / make** a target sum?" | 0/1 subset-sum (decision) | `dp[s] = dp[s] \|\| dp[s-num]` (s downward) | O(n·sum) |
| "**Count** the ways to hit a target (±, subsets)" | 0/1 subset-sum (count) | `dp[s] += dp[s-num]` (s downward) | O(n·sum) |
| "**Longest increasing** subsequence" | LIS | `dp[i] = 1 + max(dp[j]: nums[j]<nums[i])` | O(n²) / O(n log n) |
| "**Longest common** subseq of two strings" | LCS 2D | match→diag+1, else→max(up,left) | O(m·n) |
| "**Min edits** to transform string A→B" | Edit distance 2D | match→diag, else→1+min(diag,up,left) | O(m·n) |
| "**Pattern / regex / wildcard** matching" | 2D string DP (branching) | `'*' → dp[i][j-2] \|\| (match && dp[i-1][j])` | O(m·n) |
| "Count **paths in a grid**, right/down" | Grid count-ways | `dp[i][j] = dp[i-1][j] + dp[i][j-1]` | O(m·n) |
| "**Min path sum** in a grid" | Grid min-cost | `dp[i][j] = grid+min(up,left)` | O(m·n) |
| "**Largest square** of 1s in a matrix" | Grid min-of-3 | `dp[i][j] = 1 + min(up,left,diag)` | O(m·n) |
| "Can string be **split** into dict words?" | Prefix partition | `dp[i] = ∃j: dp[j] && s[j..i)∈dict` | O(n²·L) |
| "**Decode / count** valid interpretations" | 1D ways-with-constraint | `dp[i] = dp[i-1](if valid) + dp[i-2](if valid)` | O(n) |
| "**Longest palindromic** substring" | Expand-around-center | `s[i]==s[j] && inner` | O(n²) |
| "**Min cuts / partitions** so each piece is valid" | Partition DP + pal table | `cuts[i] = min(cuts[j-1]+1)` over pal `s[j..i]` | O(n²) |
| "**Order over a range** to max/min a combine cost" | Interval DP (last to act) | `dp[i][j] = max_k(dp[i][k]+cost+dp[k][j])` | O(n³) |

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

// 9️⃣ 0/1 knapsack on a 1D array: loop the capacity DOWNWARD so each item is used once.
//    Unbounded knapsack (coins): loop the capacity UPWARD so items can repeat.
for (int s = target; s >= num; s--) dp[s] = dp[s] || dp[s - num]; // 0/1
for (int a = coin; a <= amount; a++) dp[a] = Math.min(dp[a], dp[a - coin] + 1); // unbounded

// 🔟 2D char arrays for grids: char[][] for Maximal Square ('1'/'0'), int[][] for path grids.
//    Pad with an extra zero row/col (dp[m+1][n+1]) to avoid i-1/j-1 bounds checks.
```

> ⚠️ **Common Java DP bugs to avoid:**
> - Using `int` for "infinity" then doing `MAX_VALUE + 1` → **overflow** to negative. Use `amount + 1` or `n + 1` as your sentinel instead.
> - Off-by-one between **string index** (`s.charAt(i-1)`) and **dp index** (`dp[i]`). Keep the convention: `dp[i]` = answer for the **first `i` characters**.
> - Forgetting the **base case** initialization (e.g., `dp[0] = true`/`0`/`1`). The whole table builds on it.
> - **Knapsack loop direction**: downward = each item once (0/1); upward = reuse (unbounded). Mixing them up is the #1 subset-sum bug.
> - **Interval DP** must iterate by **increasing length** so smaller intervals are ready before larger ones use them.

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
- [ ] 🎒 **Got the knapsack loop direction right** — downward for 0/1, upward for unbounded.
- [ ] 🧪 **Dry-ran a small example** by hand to prove correctness.
- [ ] 🧮 **Stated final Time & Space complexity** clearly.
- [ ] 🪲 **Checked edge cases** — empty string/array, single element, no valid answer (return `-1`/`false`/`0`).
- [ ] 🧩 **Verified the answer location** — is it `dp[n]`, `dp[m][n]`, `dp[0][n+1]`, or `max(dp[i])`? (LIS & interval DP trip people up here!)

> 🎤 **Closing line for the interviewer:** *"The brute force was exponential because of overlapping subproblems; by caching each state once I brought it down to polynomial time, and I can shave the space to O(n) by keeping only the rows I still need."* — That sentence is the soul of every DP round at Microsoft. 🚀
