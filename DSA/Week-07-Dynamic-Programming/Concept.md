# Week 7: Dynamic Programming (DP)

---

## Part 1: Understanding DP — The Big Picture

---

### 1. What is Dynamic Programming?

**Plain English:**
Dynamic Programming is a technique where you solve a big problem by breaking it into
SMALLER subproblems, solving each subproblem ONCE, STORING the result, and REUSING it
whenever the same subproblem comes up again.

**The key insight:** Instead of solving the same thing over and over, REMEMBER the answer.

**Real-life Analogy — Climbing Stairs:**
Imagine you live on the 10th floor. Someone asks: "How many ways can you climb to
step 10 if you can take 1 or 2 steps at a time?" You think hard and figure it out.

Now they ask: "How many ways to reach step 11?"

**Without DP:** You start from scratch and redo ALL the calculations. Waste of effort!

**With DP:** You say, "I already know the answer for step 10 and step 9. Step 11 is
just step 10 answer + step 9 answer!" You REUSE your previous work.

**Another Analogy — Phone Directory (Indian style):**
Your mom asks for Sharma Uncle's phone number. You look through the entire directory
and find it. 10 minutes later, your dad asks for the same number. Do you search
the entire directory again? NO! You REMEMBER it (or write it down). That is DP.

**Technical Definition:**
DP = Recursion + Memoization (remembering previous results)
OR
DP = Building a solution table from smallest subproblems to the final answer

---

### 2. Why Does DP Exist?

**The problem it solves: REDUNDANT COMPUTATION.**

Consider Fibonacci: fib(n) = fib(n-1) + fib(n-2)

**Without DP (plain recursion), fib(5) causes this explosion:**
```
                         fib(5)
                       /        \
                  fib(4)        fib(3)
                 /     \        /    \
            fib(3)   fib(2)  fib(2) fib(1)
            /   \     /  \    /  \
        fib(2) fib(1) fib(1) fib(0) fib(1) fib(0)
        /   \
    fib(1) fib(0)
```

Count the calls:
- fib(3) is calculated 2 times
- fib(2) is calculated 3 times
- fib(1) is calculated 5 times
- fib(0) is calculated 3 times

For fib(5), we make 15 calls. For fib(50), we make over 2^50 calls — that is
over 1,000,000,000,000,000 calls! Your computer will take YEARS.

**With DP:** Each value is calculated ONCE and stored. fib(50) = 50 calculations. Done instantly.

---

### 3. When is a Problem a DP Problem?

A problem is solvable with DP when BOTH of these conditions are true:

#### Condition 1: Optimal Substructure
The optimal solution to the big problem CONTAINS optimal solutions to its subproblems.

**Example:** Shortest path from Delhi to Chennai.
If the shortest path goes through Mumbai, then:
- The Delhi→Mumbai segment must also be the shortest path Delhi→Mumbai
- The Mumbai→Chennai segment must also be the shortest path Mumbai→Chennai

You can combine optimal sub-solutions to get the optimal total solution.

#### Condition 2: Overlapping Subproblems
The SAME subproblems are solved MULTIPLE times during recursion.

**Example:** In Fibonacci, fib(3) is needed by both fib(5) and fib(4).
This overlap is what makes caching/storing results valuable.

**If subproblems DON'T overlap** → use Divide and Conquer instead (like Merge Sort).

---

### 4. DP vs Greedy vs Divide and Conquer

| | Dynamic Programming | Greedy | Divide and Conquer |
|---|---|---|---|
| **Subproblems overlap?** | YES | N/A | NO |
| **Optimal substructure?** | YES | YES | YES |
| **Approach** | Try ALL options, remember best | Take best option NOW | Split, solve independently, merge |
| **Correctness** | Always finds optimal | Only if greedy property holds | Always correct |
| **Speed** | Moderate (polynomial) | Fast (often O(n log n)) | Depends |
| **Example** | 0/1 Knapsack, Coin Change | Activity Selection | Merge Sort, Quick Sort |

---

## Part 2: The Two Approaches to DP

---

### Approach 1: Top-Down (Memoization)

**Idea:** Start from the BIG problem. Recursively break it down. Before computing
a subproblem, check if you have already solved it. If yes, return the stored answer.
If no, solve it, STORE it, then return it.

**Think of it as:** "I need fib(10). Let me ask for fib(9) and fib(8).
Oh wait, I already calculated fib(8) before — let me just look it up."

**Template:**
```java
// Top-Down = Recursion + Cache (memo array or HashMap)
int[] memo;  // stores previously computed results

int solve(int n) {
    if (n <= base_case) return base_value;   // base case
    if (memo[n] != -1) return memo[n];       // already computed? return cached result!

    // Not computed yet — compute and store
    memo[n] = /* recursive formula using solve(n-1), solve(n-2), etc. */;
    return memo[n];
}
```

---

### Approach 2: Bottom-Up (Tabulation)

**Idea:** Start from the SMALLEST subproblems (base cases). Build up the answer
in a table, step by step, until you reach the final answer.

**Think of it as:** Building a house. You lay the foundation first (base cases),
then build floor 1, then floor 2, and so on. You cannot build floor 5 without floor 4.

**Template:**
```java
// Bottom-Up = Iterative + Table (dp array)
int solve(int n) {
    int[] dp = new int[n + 1];

    // Base cases
    dp[0] = base_value_0;
    dp[1] = base_value_1;

    // Fill table from smallest to largest
    for (int i = 2; i <= n; i++) {
        dp[i] = /* formula using dp[i-1], dp[i-2], etc. */;
    }

    return dp[n];  // the answer!
}
```

---

### Comparison: Top-Down vs Bottom-Up

| | Top-Down (Memoization) | Bottom-Up (Tabulation) |
|---|---|---|
| **Direction** | Starts from n, goes to base case | Starts from base case, goes to n |
| **Implementation** | Recursive + memo array/map | Iterative + dp array |
| **Computes** | Only subproblems actually needed | ALL subproblems from 0 to n |
| **Overhead** | Recursion overhead (function calls) | No recursion overhead |
| **Ease of writing** | More natural (just add caching to recursion) | Need to figure out iteration order |
| **Space optimization** | Harder | Easier (can often reduce to O(1)) |

Both give the SAME ANSWER. In interviews, either is accepted. Bottom-up is slightly
preferred because it avoids stack overflow for large n and is easier to space-optimize.

---

## Part 3: The 5-Step DP Framework

For ANY DP problem, follow these 5 steps:

### Step 1: Define the State
What does `dp[i]` (or `dp[i][j]`) represent?

This is the HARDEST step. Get this right and the rest follows.

### Step 2: Define the Transition (Recurrence)
How does `dp[i]` relate to previously computed states?
This is the mathematical formula that builds the answer.

### Step 3: Define the Base Case
What are the smallest subproblems with KNOWN answers?
These are where recursion stops / where the table starts.

### Step 4: Define the Answer
Which entry in the dp table is the FINAL answer?
Usually `dp[n]` or `dp[n-1]` or `dp[n][m]`.

### Step 5: Optimize Space (Optional)
Do you really need the entire dp array? Often you only need the last 1-2 values.

---

## Part 4: Fibonacci — Deep Walkthrough of All Approaches

Let us apply everything to Fibonacci: fib(n) = fib(n-1) + fib(n-2)

### Approach 0: Plain Recursion (NO DP) — O(2^n)

```java
// BAD! Exponential time — each call branches into TWO more calls
int fib(int n) {
    if (n <= 1) return n;
    return fib(n - 1) + fib(n - 2);  // same subproblems solved repeatedly
}
```

**Recursion tree for fib(5):**
```
                         fib(5)
                       /        \
                  fib(4)        fib(3)      ← fib(3) computed TWICE
                 /     \        /    \
            fib(3)   fib(2)  fib(2) fib(1)  ← fib(2) computed THREE times
            /   \     /  \    /  \
        fib(2) fib(1) f(1) f(0) f(1) f(0)
        /   \
    fib(1) fib(0)

Total calls for fib(5): 15
Total calls for fib(50): 2^50 ≈ 10^15 (DISASTER)
```

---

### Approach 1: Top-Down (Memoization) — O(n) time, O(n) space

```java
// GOOD! Each subproblem computed ONCE, cached for reuse
int[] memo;

int fib(int n) {
    memo = new int[n + 1];
    Arrays.fill(memo, -1);        // -1 means "not computed yet"
    return fibHelper(n);
}

int fibHelper(int n) {
    if (n <= 1) return n;                // base case: fib(0)=0, fib(1)=1
    if (memo[n] != -1) return memo[n];   // already computed? return cached!

    memo[n] = fibHelper(n - 1) + fibHelper(n - 2);  // compute and cache
    return memo[n];
}
```

**What happens for fib(5) with memoization:**
```
fib(5) → needs fib(4), fib(3)
  fib(4) → needs fib(3), fib(2)
    fib(3) → needs fib(2), fib(1)
      fib(2) → needs fib(1), fib(0)
        fib(1) = 1 (base case)
        fib(0) = 0 (base case)
      fib(2) = 1  [STORE in memo[2]]
      fib(1) = 1 (base case)
    fib(3) = 2  [STORE in memo[3]]
    fib(2) = memo[2] = 1  [CACHE HIT! No recomputation!]
  fib(4) = 3  [STORE in memo[4]]
  fib(3) = memo[3] = 2  [CACHE HIT! No recomputation!]
fib(5) = 5

Total unique computations: 5 (not 15!)
```

---

### Approach 2: Bottom-Up (Tabulation) — O(n) time, O(n) space

```java
// GOOD! Iterative, no recursion overhead
int fib(int n) {
    if (n <= 1) return n;

    int[] dp = new int[n + 1];
    dp[0] = 0;               // base case
    dp[1] = 1;               // base case

    for (int i = 2; i <= n; i++) {
        dp[i] = dp[i - 1] + dp[i - 2];   // build from previously computed values
    }

    return dp[n];
}
```

**Table being filled for fib(6):**
```
Index: | 0 | 1 | 2 | 3 | 4 | 5 | 6 |
Value: | 0 | 1 | 1 | 2 | 3 | 5 | 8 |
         ↑   ↑   ↑
       base base  dp[2] = dp[1] + dp[0] = 1 + 0 = 1
                        dp[3] = dp[2] + dp[1] = 1 + 1 = 2
                              dp[4] = dp[3] + dp[2] = 2 + 1 = 3
                                    dp[5] = dp[4] + dp[3] = 3 + 2 = 5
                                          dp[6] = dp[5] + dp[4] = 5 + 3 = 8
```

---

### Approach 3: Space Optimized — O(n) time, O(1) space

```java
// BEST! We only ever need the previous TWO values
int fib(int n) {
    if (n <= 1) return n;

    int prev2 = 0;   // fib(i-2), starts as fib(0)
    int prev1 = 1;   // fib(i-1), starts as fib(1)

    for (int i = 2; i <= n; i++) {
        int current = prev1 + prev2;   // fib(i) = fib(i-1) + fib(i-2)
        prev2 = prev1;                 // shift: old prev1 becomes new prev2
        prev1 = current;               // shift: current becomes new prev1
    }

    return prev1;
}
```

**Why this works:** At any point, dp[i] only depends on dp[i-1] and dp[i-2].
We do not need the full array — just the last two values. This is a common
space optimization in DP.

---

## Part 5: Classic DP Patterns

---

### Pattern 1: 1D DP — dp[i] depends on previous elements

**When to recognize:** Problem asks about sequences, steps, or linear progression.

**Template:**
```java
int[] dp = new int[n + 1];
dp[0] = base;
for (int i = 1; i <= n; i++) {
    dp[i] = someFunction(dp[i-1], dp[i-2], ...);
}
return dp[n];
```

**Classic problems:**
- Climbing Stairs (dp[i] = dp[i-1] + dp[i-2])
- House Robber (dp[i] = max(dp[i-1], dp[i-2] + nums[i]))
- Coin Change (see detailed walkthrough below)

---

### Pattern 2: 2D DP — dp[i][j] depends on neighbors in 2D table

**When to recognize:** Two sequences/dimensions, grids, or two variables to track.

**Template:**
```java
int[][] dp = new int[m + 1][n + 1];
// Fill base cases (first row, first column)
for (int i = 1; i <= m; i++) {
    for (int j = 1; j <= n; j++) {
        dp[i][j] = someFunction(dp[i-1][j], dp[i][j-1], dp[i-1][j-1]);
    }
}
return dp[m][n];
```

**Classic problems:**
- Unique Paths (dp[i][j] = dp[i-1][j] + dp[i][j-1])
- Longest Common Subsequence
- Edit Distance

---

### Pattern 3: Knapsack Pattern — Include or Exclude

**When to recognize:** "Given items with weights/values, maximize value within weight limit"
or "Can you partition into two subsets with equal sum?"

**Two types:**
- **0/1 Knapsack:** Each item can be used AT MOST once
- **Unbounded Knapsack:** Each item can be used UNLIMITED times

**0/1 Knapsack template:**
```java
// dp[i][w] = max value using first i items with capacity w
int[][] dp = new int[n + 1][capacity + 1];

for (int i = 1; i <= n; i++) {
    for (int w = 0; w <= capacity; w++) {
        dp[i][w] = dp[i-1][w];   // don't include item i

        if (weight[i-1] <= w) {   // CAN include item i
            dp[i][w] = Math.max(
                dp[i][w],                                    // exclude
                dp[i-1][w - weight[i-1]] + value[i-1]       // include
            );
        }
    }
}
return dp[n][capacity];
```

---

### Pattern 4: String DP — Compare characters of two strings

**When to recognize:** "Longest common subsequence", "edit distance", "palindrome"

**Template:**
```java
int[][] dp = new int[s1.length() + 1][s2.length() + 1];

for (int i = 1; i <= s1.length(); i++) {
    for (int j = 1; j <= s2.length(); j++) {
        if (s1.charAt(i-1) == s2.charAt(j-1)) {
            dp[i][j] = dp[i-1][j-1] + 1;    // characters match
        } else {
            dp[i][j] = Math.max(dp[i-1][j], dp[i][j-1]);  // skip one character
        }
    }
}
return dp[s1.length()][s2.length()];
```

---

### Pattern 5: Decision Making DP — Choose between options at each step

**When to recognize:** "Buy/sell stock", "rob house", "choose path"

At each step, you are in a STATE and must DECIDE between options.
The state tracks what decisions you have made so far.

---

## Part 6: Detailed Walkthrough — Coin Change Problem

This is one of the most commonly asked DP problems. Let us solve it step by step.

**Problem:** Given coins of different denominations and a total amount, find the
MINIMUM number of coins needed to make that amount. If impossible, return -1.

**Example:** coins = [1, 3, 4], amount = 6
Answer: 2 (using two 3-coins: 3 + 3 = 6)

---

### Applying the 5-Step Framework:

**Step 1: Define State**
`dp[i]` = minimum number of coins needed to make amount `i`

**Step 2: Define Transition**
For each amount `i`, try EACH coin. If we use a coin of value `c`:
- We need `dp[i - c] + 1` coins (the coins for amount `i-c`, plus this one coin)
- We want the MINIMUM across all coin choices

`dp[i] = min(dp[i - c] + 1)` for each coin `c` where `c <= i`

**Step 3: Define Base Case**
`dp[0] = 0` — zero coins needed to make amount 0

**Step 4: Define Answer**
`dp[amount]` — minimum coins for the target amount

**Step 5: Space**
We need the full array since dp[i] can depend on dp[i - c] for various c values.

---

### Filling the Table Step by Step

coins = [1, 3, 4], amount = 6

Initialize: dp[0] = 0, all others = infinity (meaning "impossible so far")

```
Amount:     | 0 | 1 | 2 | 3 | 4 | 5 | 6 |
Initial:    | 0 | ∞ | ∞ | ∞ | ∞ | ∞ | ∞ |
```

**Fill dp[1]:** Try each coin ≤ 1:
- coin=1: dp[1-1] + 1 = dp[0] + 1 = 0 + 1 = 1
- coin=3: 3 > 1, skip
- coin=4: 4 > 1, skip
- dp[1] = min(1) = **1**

```
Amount:     | 0 | 1 | 2 | 3 | 4 | 5 | 6 |
            | 0 | 1 | ∞ | ∞ | ∞ | ∞ | ∞ |
```

**Fill dp[2]:** Try each coin ≤ 2:
- coin=1: dp[2-1] + 1 = dp[1] + 1 = 1 + 1 = 2
- coin=3: 3 > 2, skip
- coin=4: 4 > 2, skip
- dp[2] = min(2) = **2**

```
Amount:     | 0 | 1 | 2 | 3 | 4 | 5 | 6 |
            | 0 | 1 | 2 | ∞ | ∞ | ∞ | ∞ |
```

**Fill dp[3]:** Try each coin ≤ 3:
- coin=1: dp[3-1] + 1 = dp[2] + 1 = 2 + 1 = 3
- coin=3: dp[3-3] + 1 = dp[0] + 1 = 0 + 1 = 1  ← BETTER!
- coin=4: 4 > 3, skip
- dp[3] = min(3, 1) = **1**

```
Amount:     | 0 | 1 | 2 | 3 | 4 | 5 | 6 |
            | 0 | 1 | 2 | 1 | ∞ | ∞ | ∞ |
```

**Fill dp[4]:** Try each coin ≤ 4:
- coin=1: dp[3] + 1 = 1 + 1 = 2
- coin=3: dp[1] + 1 = 1 + 1 = 2
- coin=4: dp[0] + 1 = 0 + 1 = 1  ← BEST!
- dp[4] = min(2, 2, 1) = **1**

```
Amount:     | 0 | 1 | 2 | 3 | 4 | 5 | 6 |
            | 0 | 1 | 2 | 1 | 1 | ∞ | ∞ |
```

**Fill dp[5]:** Try each coin ≤ 5:
- coin=1: dp[4] + 1 = 1 + 1 = 2
- coin=3: dp[2] + 1 = 2 + 1 = 3
- coin=4: dp[1] + 1 = 1 + 1 = 2
- dp[5] = min(2, 3, 2) = **2**

```
Amount:     | 0 | 1 | 2 | 3 | 4 | 5 | 6 |
            | 0 | 1 | 2 | 1 | 1 | 2 | ∞ |
```

**Fill dp[6]:** Try each coin ≤ 6:
- coin=1: dp[5] + 1 = 2 + 1 = 3
- coin=3: dp[3] + 1 = 1 + 1 = 2  ← BEST! (3 + 3 = 6, two coins)
- coin=4: dp[2] + 1 = 2 + 1 = 3
- dp[6] = min(3, 2, 3) = **2**

```
Amount:     | 0 | 1 | 2 | 3 | 4 | 5 | 6 |
Final:      | 0 | 1 | 2 | 1 | 1 | 2 | 2 |
                                        ↑
                                     ANSWER: 2 coins (3+3)
```

---

### Java Code — Coin Change

```java
// Coin Change — find minimum coins to make the target amount
// Returns -1 if it is impossible
int coinChange(int[] coins, int amount) {
    // dp[i] = minimum coins needed to make amount i
    int[] dp = new int[amount + 1];

    // Initialize all amounts as "impossible" (use amount+1 as infinity
    // because we can never need more than 'amount' coins even with coin=1)
    Arrays.fill(dp, amount + 1);

    dp[0] = 0;  // base case: 0 coins for amount 0

    // Fill the table for each amount from 1 to target
    for (int i = 1; i <= amount; i++) {
        // Try each coin denomination
        for (int coin : coins) {
            if (coin <= i) {  // can only use this coin if it fits
                // Option: use this coin, then need dp[i - coin] more coins
                dp[i] = Math.min(dp[i], dp[i - coin] + 1);
            }
        }
    }

    // If dp[amount] is still "impossible", return -1
    return dp[amount] > amount ? -1 : dp[amount];
}
```

---

## Part 7: More Classic Problems with Walkthrough

---

### Climbing Stairs

**Problem:** You can climb 1 or 2 steps. How many ways to reach step n?

**State:** dp[i] = number of ways to reach step i
**Transition:** dp[i] = dp[i-1] + dp[i-2] (come from 1 step below OR 2 steps below)
**Base:** dp[0] = 1 (one way to stay at ground), dp[1] = 1 (one way to reach step 1)

```java
int climbStairs(int n) {
    if (n <= 1) return 1;
    int prev2 = 1, prev1 = 1;  // dp[0]=1, dp[1]=1
    for (int i = 2; i <= n; i++) {
        int current = prev1 + prev2;
        prev2 = prev1;
        prev1 = current;
    }
    return prev1;
}
// This is literally Fibonacci! Just with different base cases.
```

---

### House Robber

**Problem:** Array of house values. Cannot rob two adjacent houses. Maximize total.

**State:** dp[i] = max money robbing from houses 0 to i
**Transition:** dp[i] = max(dp[i-1], dp[i-2] + nums[i])
  - Skip house i: take dp[i-1]
  - Rob house i: take dp[i-2] + nums[i] (must skip i-1)
**Base:** dp[0] = nums[0], dp[1] = max(nums[0], nums[1])

```java
int rob(int[] nums) {
    if (nums.length == 1) return nums[0];
    int prev2 = nums[0];
    int prev1 = Math.max(nums[0], nums[1]);
    for (int i = 2; i < nums.length; i++) {
        int current = Math.max(prev1, prev2 + nums[i]);
        prev2 = prev1;
        prev1 = current;
    }
    return prev1;
}
```

**Example:** nums = [2, 7, 9, 3, 1]
```
i=0: prev2=2
i=1: prev1=max(2,7)=7
i=2: current=max(7, 2+9)=11, prev2=7, prev1=11
i=3: current=max(11, 7+3)=11, prev2=11, prev1=11
i=4: current=max(11, 11+1)=12
Answer: 12 (rob houses 0, 2, 4: 2+9+1=12)
```

---

### Longest Common Subsequence (LCS)

**Problem:** Given two strings, find the length of the longest subsequence common to both.

**State:** dp[i][j] = LCS of first i chars of s1 and first j chars of s2
**Transition:**
- If s1[i-1] == s2[j-1]: dp[i][j] = dp[i-1][j-1] + 1 (characters match, extend)
- Else: dp[i][j] = max(dp[i-1][j], dp[i][j-1]) (skip one character from either string)
**Base:** dp[0][j] = 0, dp[i][0] = 0 (empty string has LCS = 0)

```java
int longestCommonSubsequence(String s1, String s2) {
    int m = s1.length(), n = s2.length();
    int[][] dp = new int[m + 1][n + 1];  // dp[0][*] and dp[*][0] are 0 by default

    for (int i = 1; i <= m; i++) {
        for (int j = 1; j <= n; j++) {
            if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                dp[i][j] = dp[i - 1][j - 1] + 1;   // match! extend LCS
            } else {
                dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);  // skip from one string
            }
        }
    }
    return dp[m][n];
}
```

**Example:** s1 = "abcde", s2 = "ace"
```
        ""  a  c  e
    ""   0  0  0  0
    a    0  1  1  1      a matches a → dp[1][1] = dp[0][0]+1 = 1
    b    0  1  1  1      b doesn't match → max(dp[0][1], dp[1][0]) = 1
    c    0  1  2  2      c matches c → dp[2][2] = dp[1][1]+1 = 2
    d    0  1  2  2      d doesn't match → stays 2
    e    0  1  2  3      e matches e → dp[4][2]+1 = 2+1 = 3

LCS = 3 ("ace")
```

---

## Part 8: When to Use DP — Pattern Recognition Triggers

| If you see this in the problem... | Think this... |
|-----------------------------------|---------------|
| "Minimum/maximum number of..." | DP (optimization) |
| "How many ways to..." | DP (counting) |
| "Is it possible to..." (with choices) | DP (feasibility) |
| "Longest/shortest subsequence/substring" | String DP |
| "Partition into subsets" | Knapsack DP |
| "Can you make amount X with coins" | Coin Change (unbounded knapsack) |
| "Climbing stairs / jumping" | 1D DP |
| "Grid paths" | 2D DP |
| "Include or exclude items" | 0/1 Knapsack |
| Problem has both optimal substructure AND overlapping subproblems | DP |

---

### 9. Common Mistakes

1. **Forgetting base cases:** dp[0] must be set correctly. Off-by-one errors are very common.

2. **Wrong state definition:** If dp[i] does not clearly represent something, the transition will be wrong. Spend time getting the state right.

3. **Not initializing the dp array properly:** For minimum problems, initialize to infinity (or a large value). For maximum problems, initialize to 0 or negative infinity.

4. **Trying to jump straight to bottom-up:** If you are stuck, start with top-down (recursion + memo). It is more natural. Once it works, convert to bottom-up.

5. **Thinking every recursion is DP:** DP requires OVERLAPPING subproblems. If subproblems are independent (like in Merge Sort), it is Divide and Conquer, not DP.

6. **Confusing subsequence and substring:** Subsequence = characters in order but not necessarily contiguous. Substring = contiguous characters. The DP transitions are different!

7. **Off-by-one with string indices:** When dp is (m+1) x (n+1) sized, dp[i][j] corresponds to s1[i-1] and s2[j-1]. Easy to mix up.

---

### 10. Quick Summary

```
DYNAMIC PROGRAMMING:
  - Solve big problem by solving and STORING results of smaller subproblems
  - Two conditions: optimal substructure + overlapping subproblems

TWO APPROACHES:
  - Top-Down (Memoization): recursion + cache
  - Bottom-Up (Tabulation): iterative + dp table
  Both give same result. Bottom-up is slightly preferred.

5-STEP FRAMEWORK:
  1. Define state (what does dp[i] mean?)
  2. Define transition (how does dp[i] relate to smaller states?)
  3. Define base case (smallest known answers)
  4. Define answer (which dp entry is final?)
  5. Optimize space (often only need last 1-2 rows/values)

COMMON PATTERNS:
  - 1D DP: climbing stairs, house robber, coin change
  - 2D DP: unique paths, LCS, edit distance
  - Knapsack: include/exclude items with weight limit
  - String DP: compare two strings char by char
  - Decision DP: buy/sell stock, choice at each step

REMEMBER:
  - Start with recursion. Add memoization. Convert to bottom-up.
  - dp[i] definition is the HARDEST part. Get it right first.
  - Draw the table. Fill it by hand. THEN write code.
  - If stuck, ask: "What decision am I making at step i?"
```

---

*Next: Week 8 — Backtracking and Greedy. The art of exploring ALL possibilities vs. taking shortcuts!*
