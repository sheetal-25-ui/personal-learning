# Module 11 — Day 6: Recursion and Dynamic Programming

## The Topic Everyone Fears (and why you won't)

Ask any engineer what scared them most in interviews and most will say: **dynamic programming (DP)**. It has a reputation for being magical and impossible.

Here's the secret nobody tells you: **DP is just recursion + remembering your answers.** That's it. If you understand recursion and you understand the hash map from Day 3, you already have both pieces. Today we snap them together.

We'll build up slowly: recursion → memoization → DP → the classic problems. By the end, DP will feel like a *recipe*, not magic.

---

## Part 1: Recursion (the foundation)

### The intuition: a function that calls itself

Recursion is when a function **solves a big problem by calling itself on a smaller version** of the same problem, until the problem is small enough to answer directly.

Think of Russian nesting dolls: to open the biggest doll, you open it to find a smaller doll, which you open to find a smaller one... until you reach the tiny solid one that can't be opened. That tiny doll is the **base case**.

### Every recursion needs two things

1. **Base case** — the smallest version you can answer directly (stops the recursion).
2. **Recursive case** — break the problem into a smaller version and call yourself.

```python
def factorial(n):
    # Base case: factorial of 0 or 1 is 1 (stops the recursion)
    if n <= 1:
        return 1
    # Recursive case: n! = n × (n-1)!
    return n * factorial(n - 1)

print(factorial(5))   # Output: 120  (5 × 4 × 3 × 2 × 1)
```

**How it unfolds:**

```
factorial(5)
= 5 × factorial(4)
= 5 × 4 × factorial(3)
= 5 × 4 × 3 × factorial(2)
= 5 × 4 × 3 × 2 × factorial(1)   ← base case! returns 1
= 5 × 4 × 3 × 2 × 1
= 120
```

> **Warning:** If you forget the base case, the function calls itself forever → `RecursionError: maximum recursion depth exceeded`. **Always write the base case first.**

### Recursion you've already seen

You used recursion on Day 5 for tree traversals! `dfs(node.left); dfs(node.right)` is recursion — each call handles a smaller subtree, and the base case is `if not node: return`. So you're already comfortable with it.

---

## Part 2: When Naive Recursion Goes Wrong

Let's compute Fibonacci numbers (each number is the sum of the previous two: 0, 1, 1, 2, 3, 5, 8, 13...).

```python
def fib(n):
    if n <= 1:           # base case: fib(0)=0, fib(1)=1
        return n
    return fib(n - 1) + fib(n - 2)   # recursive case

print(fib(10))   # Output: 55  (correct!)
print(fib(40))   # Output: 102334155  (correct, but... try it — it's SLOW)
```

This is **correct** but catastrophically **slow**. Why? It recomputes the same values over and over:

```
                  fib(5)
                /        \
           fib(4)         fib(3)
           /    \         /    \
       fib(3)  fib(2)  fib(2)  fib(1)
       /   \    ...     ...
   fib(2) fib(1)
   ...

   Notice fib(3) is computed TWICE, fib(2) THREE times...
```

For `fib(40)`, the same subproblems are recomputed *millions* of times. The complexity is a horrifying **O(2ⁿ)** — exponential (remember Day 1's "explodes" curve?).

> **This is the key DP insight:** the problem has **overlapping subproblems** — the same smaller problems appear again and again. If we just *remember* each answer the first time, we never recompute it.

---

## Part 3: Memoization (recursion + a memory)

**Memoization** = recursion + a cache (a hash map!) that stores answers we've already computed. The word looks like "memorization" — because that's exactly what it does.

```python
def fib(n, memo=None):
    if memo is None:
        memo = {}
    if n <= 1:                      # base case
        return n
    if n in memo:                   # already computed? return instantly — O(1)
        return memo[n]
    # compute it once, then REMEMBER it
    memo[n] = fib(n - 1, memo) + fib(n - 2, memo)
    return memo[n]

print(fib(40))   # Output: 102334155  — now INSTANT!
print(fib(100))  # Output: 354224848179261915075  — still instant!
```

**What changed?** We added a `memo` dict. The first time we compute `fib(7)`, we store it. Every later request for `fib(7)` is an O(1) lookup. We compute each value **exactly once**.

- **Before:** O(2ⁿ) — exponential, dies at n=40.
- **After:** O(n) — linear, handles n=1000 instantly.

We turned an unusable solution into a fast one **just by remembering answers.** That's the heart of DP.

> Python even has a built-in memoizer! Decorate any function with `@lru_cache`:
> ```python
> from functools import lru_cache
>
> @lru_cache(maxsize=None)   # automatically memoizes!
> def fib(n):
>     if n <= 1:
>         return n
>     return fib(n - 1) + fib(n - 2)
>
> print(fib(100))   # instant
> ```
> Knowing `@lru_cache` exists impresses interviewers — but you should also be able to write the memo dict by hand.

---

## Part 4: Dynamic Programming (the recipe)

**Dynamic Programming** is just a name for "solve a problem by combining solutions to overlapping subproblems." There are two styles:

```
TOP-DOWN (memoization):   start at the big problem, recurse down, cache results.
                          ← what we just did with fib.

BOTTOM-UP (tabulation):   start at the smallest subproblems, build UP to the answer
                          in a loop, storing results in a table (usually a list).
```

Both have the same complexity. **Top-down is easier to write** (just add a memo to your recursion). **Bottom-up avoids recursion** (no risk of stack overflow) and is often what interviewers want to see eventually.

### Fibonacci, bottom-up

```python
def fib_bottom_up(n):
    if n <= 1:
        return n
    dp = [0] * (n + 1)        # dp[i] will hold fib(i)
    dp[1] = 1
    for i in range(2, n + 1):
        dp[i] = dp[i - 1] + dp[i - 2]   # build up from small to big
    return dp[n]

print(fib_bottom_up(10))   # Output: 55
```

We fill a table from the bottom (`dp[0], dp[1], dp[2]...`) up to `dp[n]`. No recursion, O(n) time.

### The DP recipe (how to approach ANY DP problem)

1. **Define the subproblem.** What does `dp[i]` *mean*? (e.g., "the number of ways to reach step i")
2. **Find the recurrence.** How does `dp[i]` relate to smaller subproblems? (e.g., `dp[i] = dp[i-1] + dp[i-2]`)
3. **Set the base cases.** The smallest answers you know directly. (e.g., `dp[0] = 1`)
4. **Decide the order.** Bottom-up loop, or top-down recursion + memo.
5. **Return the answer.** Usually `dp[n]` or `dp[-1]`.

Follow this recipe and DP stops being scary.

---

## Part 5: The Classic DP Problems

### Climbing Stairs (the "hello world" of DP)

You can climb 1 or 2 steps at a time. How many distinct ways to reach step `n`?

**Think about it:** To reach step `n`, you either came from step `n-1` (a 1-step) or step `n-2` (a 2-step). So `ways(n) = ways(n-1) + ways(n-2)`. **That's Fibonacci in disguise!**

```python
def climb_stairs(n):
    # dp[i] = number of ways to reach step i
    if n <= 2:
        return n
    dp = [0] * (n + 1)
    dp[1] = 1        # 1 way to reach step 1
    dp[2] = 2        # 2 ways to reach step 2: (1+1) or (2)
    for i in range(3, n + 1):
        dp[i] = dp[i - 1] + dp[i - 2]   # came from one step below or two below
    return dp[n]

print(climb_stairs(5))   # Output: 8
```

> **Recognizing this pattern** — "ways to reach a state by combining ways to reach previous states" — is half the battle. Many DP problems are this shape.

### House Robber (the "can't pick adjacent" pattern)

You rob houses in a row, but can't rob two adjacent houses (alarms!). Maximize the loot.

**Subproblem:** `dp[i]` = max loot considering houses `0..i`. At house `i`, you either:
- **skip it** → keep `dp[i-1]`, or
- **rob it** → `nums[i] + dp[i-2]` (the loot here plus the best from two houses back).

Take the bigger choice:

```python
def rob(nums):
    if not nums:
        return 0
    if len(nums) == 1:
        return nums[0]
    dp = [0] * len(nums)
    dp[0] = nums[0]
    dp[1] = max(nums[0], nums[1])
    for i in range(2, len(nums)):
        # skip house i, OR rob it + best up to i-2
        dp[i] = max(dp[i - 1], nums[i] + dp[i - 2])
    return dp[-1]

print(rob([2, 7, 9, 3, 1]))   # Output: 12  (rob houses 2 + 9 + 1)
```

The "include it vs. skip it" decision is a hugely common DP shape.

### 0/1 Knapsack (intro to the most famous DP)

You have a knapsack with weight capacity `W`. Each item has a weight and a value. Maximize total value without exceeding capacity. For each item you make a binary choice: **take it or leave it** (that's the "0/1").

```python
def knapsack(weights, values, capacity):
    n = len(weights)
    # dp[i][w] = max value using first i items with capacity w
    dp = [[0] * (capacity + 1) for _ in range(n + 1)]

    for i in range(1, n + 1):
        for w in range(capacity + 1):
            # Option 1: don't take item i-1
            dp[i][w] = dp[i - 1][w]
            # Option 2: take item i-1 (if it fits) — add its value
            if weights[i - 1] <= w:
                take = values[i - 1] + dp[i - 1][w - weights[i - 1]]
                dp[i][w] = max(dp[i][w], take)
    return dp[n][capacity]

weights = [1, 3, 4, 5]
values  = [1, 4, 5, 7]
print(knapsack(weights, values, 7))   # Output: 9  (items with weight 3+4, value 4+5)
```

This uses a **2D DP table** (`dp[i][w]`). Don't worry if it takes a few reads — knapsack is genuinely the hardest thing this week. The "take vs. leave, build a table" idea underlies many problems (coin change, subset sum, partition).

> **The same "take it or leave it" choice** powers Coin Change, Subset Sum, and Partition problems. Learn knapsack and you've learned a whole family.

---

## How to Recognize a DP Problem

```
Signs it's probably DP...                          →  Then...
────────────────────────────────────────────────────────────────
"how many ways to..."                              →  count-based DP
"maximum / minimum / longest / shortest..."        →  optimization DP
"can you reach / partition / make exactly..."      →  boolean/target DP
the problem has CHOICES at each step               →  "take vs skip" DP
naive recursion recomputes the same subproblems    →  add memoization
the answer to step n depends on earlier steps      →  build a dp table
```

> **The mental flow in an interview:**
> 1. Solve it with **plain recursion** first (get it correct, even if slow).
> 2. Notice the **overlapping subproblems**.
> 3. **Add a memo** (top-down) — now it's fast.
> 4. If asked, convert to **bottom-up** with a table.
>
> This progression *is* the interview answer. Walk through it out loud and you'll impress.

---

## Practice Exercises

Start each with plain recursion, then add memoization. State the before/after Big-O.

### Exercise 1: "Climbing Stairs" — LeetCode #70 (Easy)
The exact problem above. Solve it three ways: naive recursion, memoized, bottom-up. This is *the* DP starter problem.

### Exercise 2: "Fibonacci Number" — LeetCode #509 (Easy)
Implement it with memoization and with `@lru_cache`. Compare the speed against naive recursion for `fib(40)` — *feel* the difference.

### Exercise 3: "House Robber" — LeetCode #198 (Medium)
The "no two adjacent" problem above. Very commonly asked. Implement bottom-up.

### Exercise 4: "Coin Change" — LeetCode #322 (Medium)
Given coin denominations and an amount, find the fewest coins to make that amount.
- A knapsack-family problem. `dp[amount] = min coins to make amount`. Asked everywhere at big tech.

### Exercise 5: "Longest Increasing Subsequence" — LeetCode #300 (Medium)
Find the length of the longest strictly increasing subsequence.
- A classic O(n²) DP (with a slick O(n log n) version for later). Great for solidifying the "dp[i] depends on all earlier dp[j]" pattern.

### Exercise 6: "Word Break" — LeetCode #139 (Medium)
Can a string be segmented into dictionary words? A boolean DP that combines hash sets (Day 3!) with DP. A great capstone for the week.

---

## Quick Reference Card

```python
# RECURSION (always: base case + recursive case)
def solve(n):
    if base_condition:        # base case — STOPS recursion
        return base_value
    return solve(smaller)     # recursive case

# MEMOIZATION (top-down DP) — recursion + a cache
def solve(n, memo=None):
    if memo is None: memo = {}
    if base_condition: return base_value
    if n in memo: return memo[n]      # remembered! O(1)
    memo[n] = ... solve(smaller) ...  # compute once, store
    return memo[n]

# or just decorate:
from functools import lru_cache
@lru_cache(maxsize=None)

# TABULATION (bottom-up DP) — fill a table small → big
dp = [base] * (n + 1)
for i in range(start, n + 1):
    dp[i] = ... dp[i-1] ... dp[i-2] ...
return dp[n]

# THE DP RECIPE:
# 1. define dp[i] meaning   2. find recurrence
# 3. set base cases         4. choose order   5. return answer
```

---

## What's Next?

You just tamed the most-feared interview topic — and you saw it's only recursion plus a hash map. Tomorrow is the finale: **Day 07 — Sorting, Searching, and Interview Strategy**. We'll cover binary search (your O(log n) superpower), a tour of sorting algorithms, and — most importantly — **how to actually behave in the interview**: how to break down a problem out loud, what to say, and how to test your code. Then we'll point you to Module 12. One day left — finish strong!
