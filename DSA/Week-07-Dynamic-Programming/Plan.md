# Week 7: Dynamic Programming (DP)

## Don't Fear DP! It's Just "Smart Recursion"

### What is DP?
- Break a big problem into smaller subproblems
- Solve each subproblem ONCE and store the result
- Use stored results instead of recalculating

### Think of it Like This
```
Without DP (Recursion):
  "What's fib(5)?"
  → fib(4) + fib(3)
  → fib(3) + fib(2) + fib(2) + fib(1)
  → fib(2) + fib(1) + fib(1) + fib(0) + ...
  → Same values calculated MANY times! Slow!

With DP (Memoization):
  fib(0) = 0 ← store it
  fib(1) = 1 ← store it
  fib(2) = fib(1) + fib(0) = 1 ← store it
  fib(3) = fib(2) + fib(1) = 2 ← store it (already have fib(2)!)
  fib(4) = fib(3) + fib(2) = 3 ← store it
  fib(5) = fib(4) + fib(3) = 5 ← done! Each value computed ONCE!
```

### Two Approaches
```
Top-Down (Memoization): Start from the big problem, use recursion + cache
Bottom-Up (Tabulation): Start from smallest subproblem, build up with a table/array
```

---

## Day 1-2: 1D DP (Easy Start)

| # | Problem | LeetCode Link | Difficulty | Microsoft? |
|---|---------|--------------|------------|------------|
| 1 | Climbing Stairs | https://leetcode.com/problems/climbing-stairs/ | Easy | Yes |
| 2 | House Robber | https://leetcode.com/problems/house-robber/ | Medium | Yes (Frequent) |
| 3 | House Robber II | https://leetcode.com/problems/house-robber-ii/ | Medium | Yes |
| 4 | Coin Change | https://leetcode.com/problems/coin-change/ | Medium | Yes (Very Frequent!) |
| 5 | Longest Increasing Subsequence | https://leetcode.com/problems/longest-increasing-subsequence/ | Medium | Yes |
| 6 | Word Break | https://leetcode.com/problems/word-break/ | Medium | Yes (Frequent) |

### Template: 1D DP
```python
# Coin Change Example
def coinChange(coins, amount):
    # dp[i] = minimum coins needed for amount i
    dp = [float('inf')] * (amount + 1)
    dp[0] = 0  # base case: 0 coins for amount 0

    for i in range(1, amount + 1):
        for coin in coins:
            if coin <= i:
                dp[i] = min(dp[i], dp[i - coin] + 1)

    return dp[amount] if dp[amount] != float('inf') else -1
```

---

## Day 3-4: 2D DP

| # | Problem | LeetCode Link | Difficulty | Microsoft? |
|---|---------|--------------|------------|------------|
| 7 | Unique Paths | https://leetcode.com/problems/unique-paths/ | Medium | Yes (Classic) |
| 8 | Longest Common Subsequence | https://leetcode.com/problems/longest-common-subsequence/ | Medium | Yes |
| 9 | Edit Distance | https://leetcode.com/problems/edit-distance/ | Medium | Yes (Microsoft Classic!) |
| 10 | 0/1 Knapsack | https://leetcode.com/problems/partition-equal-subset-sum/ | Medium | Yes |
| 11 | Maximal Square | https://leetcode.com/problems/maximal-square/ | Medium | Yes |
| 12 | Decode Ways | https://leetcode.com/problems/decode-ways/ | Medium | Yes |

---

## Day 5-6: Hard DP

| # | Problem | LeetCode Link | Difficulty | Microsoft? |
|---|---------|--------------|------------|------------|
| 13 | Longest Palindromic Substring | https://leetcode.com/problems/longest-palindromic-substring/ | Medium | Yes (Frequent!) |
| 14 | Regular Expression Matching | https://leetcode.com/problems/regular-expression-matching/ | Hard | Yes |
| 15 | Minimum Path Sum | https://leetcode.com/problems/minimum-path-sum/ | Medium | Yes |
| 16 | Target Sum | https://leetcode.com/problems/target-sum/ | Medium | Yes |
| 17 | Palindrome Partitioning | https://leetcode.com/problems/palindrome-partitioning/ | Medium | Yes |
| 18 | Burst Balloons | https://leetcode.com/problems/burst-balloons/ | Hard | Yes |

---

## Day 7: Revision

- Coin Change (must solve in 10 min)
- Edit Distance (Microsoft classic)
- Longest Common Subsequence

---

## DP Pattern Recognition

```
"How many ways to reach...?"        → Climbing Stairs pattern
"Minimum cost/coins for...?"        → Coin Change pattern
"Maximum profit/value with...?"     → Knapsack pattern
"Longest subsequence/substring?"    → LCS/LIS pattern
"Can you partition into...?"        → Subset Sum pattern
"Edit/transform one to another?"    → Edit Distance pattern
```

---

## Self-Check

- [ ] Can explain DP in simple words to someone
- [ ] Can solve Climbing Stairs, Coin Change, House Robber from memory
- [ ] Understand the difference between top-down and bottom-up
- [ ] Can identify "this is a DP problem" in interviews
- [ ] Solved at least 14 of 18 questions
