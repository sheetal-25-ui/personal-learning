# Module 11 — Day 1: Big-O and Complexity Analysis

## Why This Is Day 1

Before you write a single interview solution, you need to speak the language interviewers speak: **Big-O notation**.

Here's a scene that plays out in every coding interview:

> You: "...and that's my solution!"
> Interviewer: "Great. What's the time complexity?"
> You: *(panic)*

By the end of today, that question will feel **easy**. Big-O is just a way of answering one question:

> **"As my input gets bigger, how much slower (or more memory-hungry) does my code get?"**

That's it. Let's build the intuition.

---

## The Big Idea: We Care About *Growth*, Not Exact Speed

Imagine two pizza delivery strategies for a city:

- **Strategy A:** Walk to each house one by one.
- **Strategy B:** Have a copy of yourself appear at every house instantly.

For **3 houses**, both are fine. For **3 million houses**, Strategy A collapses and Strategy B is still instant.

Big-O ignores small inputs and asks: **what happens when the input becomes huge?** That's where good and bad code reveal themselves.

> **Key insight:** We don't measure in seconds (that depends on your laptop). We measure in *number of operations as a function of input size n*.

---

## Meet `n`

`n` = the size of your input.

- A list of 1,000 numbers → `n = 1000`
- A string of 50 characters → `n = 50`
- A graph with 10,000 nodes → `n = 10000`

Big-O describes how the work grows **as a function of n**.

---

## The Common Complexities (memorize this table!)

From fastest (best) to slowest (worst):

| Big-O | Name | "If n doubles, work..." | Real-world feel |
|---|---|---|---|
| **O(1)** | Constant | stays the same | Instant. Looking up a word by page number. |
| **O(log n)** | Logarithmic | grows by 1 step | Crazy fast. Binary search in a phone book. |
| **O(n)** | Linear | doubles | Fair. Reading every name in a list once. |
| **O(n log n)** | Linearithmic | a bit more than doubles | Good. The best sorting algorithms. |
| **O(n²)** | Quadratic | quadruples (×4) | Slow. Comparing every pair of people. |
| **O(2ⁿ)** | Exponential | *squares* — explodes | Terrible. Trying every combination. |
| **O(n!)** | Factorial | unthinkable | Catastrophic. Every possible ordering. |

### A picture of how they grow

```
operations
   ▲
   │                                    O(2ⁿ)   O(n²)
   │                                   /       /
   │                                  /       /
   │                                 /      /
   │                                /     /        O(n log n)
   │                               /    /        /
   │                              /   /       /
   │                             /  /      /          O(n)
   │                            / /     /          /
   │                           //    /         /
   │                          //  /       /              O(log n)
   │                         /_/_______________________________ O(1)
   │________________________________________________________▶ n
```

**The further left a curve shoots up, the worse the algorithm.** Your job in interviews is usually to push your solution *down* this list (e.g., from O(n²) to O(n)).

### What these numbers mean for n = 1,000,000

| Complexity | Approx operations | Feels like |
|---|---|---|
| O(1) | 1 | instant |
| O(log n) | ~20 | instant |
| O(n) | 1,000,000 | fast |
| O(n log n) | ~20,000,000 | fine |
| O(n²) | 1,000,000,000,000 | your laptop freezes |
| O(2ⁿ) | more atoms than the universe | never finishes |

This is why interviewers care. The difference between O(n) and O(n²) is the difference between "works" and "crashes the server."

---

## How to Analyze a Loop (the practical skill)

You'll analyze complexity by **counting how the loops scale**. Here are the rules.

### Rule 1: A single loop over n items → O(n)

```python
# @define example — counting operations in a single loop
def print_all(items):
    for item in items:   # runs n times
        print(item)      # 1 operation each
# Total work ≈ n operations  →  O(n)
```

If the list has 10 items, the loop runs 10 times. 1,000 items → 1,000 times. The work grows **linearly** with `n`. That's **O(n)**.

### Rule 2: Constant work → O(1)

```python
def get_first(items):
    return items[0]   # one operation, no matter how big the list
# O(1) — doesn't depend on n at all
```

Whether the list has 5 items or 5 billion, grabbing index 0 takes the same time. That's **O(1)** — constant time.

### Rule 3: A loop inside a loop → O(n²)

```python
def print_all_pairs(items):
    for a in items:          # runs n times
        for b in items:      # runs n times FOR EACH a
            print(a, b)       # so n × n = n² total
# Total work ≈ n²  →  O(n²)
```

This is the classic **nested loop**. For each of the `n` outer items, you do `n` inner operations → `n × n = n²`. With 1,000 items, that's **1,000,000** operations.

> **Interview tip:** Nested loops over the same input are a red flag. Interviewers often want you to replace them with a hash map (Day 3!) to get down to O(n).

### Rule 4: Drop the constants and lower terms

Big-O is about *growth*, so we simplify:

```python
def example(items):
    print(items[0])          # O(1)
    for item in items:       # O(n)
        print(item)
    for item in items:       # O(n)
        print(item)
# Total = O(1) + O(n) + O(n) = O(2n + 1)
# We DROP constants:  →  O(n)
```

- `O(2n)` becomes **O(n)** (drop the 2).
- `O(n + 100)` becomes **O(n)** (drop the 100).
- `O(n² + n)` becomes **O(n²)** (keep only the biggest term — n² dominates).

> **Why?** When `n` is huge, the biggest term swamps everything else. `n² + n` at n=1,000,000 is `1,000,000,000,000 + 1,000,000` — that extra million doesn't matter.

### Rule 5: Loops that cut the problem in half → O(log n)

```python
def keep_halving(n):
    count = 0
    while n > 1:
        n = n // 2    # cut in half each time
        count += 1
    return count
# Starting at n=1000: 1000 → 500 → 250 → 125 → ... → 1  (about 10 steps)
# Halving repeatedly  →  O(log n)
```

Each step throws away **half** the remaining work. To go from 1,000,000 down to 1 takes only ~20 steps. That's the magic of **O(log n)** — and it's exactly how binary search works (Day 7).

---

## Time Complexity vs. Space Complexity

Big-O measures two things:

- **Time complexity** — how many *operations*.
- **Space complexity** — how much *extra memory* you use.

```python
def sum_list(items):
    total = 0              # one variable — O(1) extra space
    for item in items:     # O(n) time
        total += item
    return total
# Time:  O(n)   (loop over all items)
# Space: O(1)   (we only made ONE extra variable, regardless of n)
```

Compare with:

```python
def double_list(items):
    result = []            # a NEW list
    for item in items:     # O(n) time
        result.append(item * 2)   # grows to size n
    return result
# Time:  O(n)
# Space: O(n)   (the result list grows with the input!)
```

> **The classic trade-off:** Often you can make code *faster* by using *more memory* (e.g., a hash map). Interviewers love when you can discuss this trade-off out loud.

---

## A Worked Example: Analyzing a Real Function

Let's analyze this step by step, like you would in an interview.

```python
def has_duplicate(nums):
    # Approach: compare every pair
    for i in range(len(nums)):        # outer loop: n times
        for j in range(i + 1, len(nums)):   # inner loop: up to n times
            if nums[i] == nums[j]:    # O(1) comparison
                return True
    return False

print(has_duplicate([1, 2, 3, 4]))   # Output: False
print(has_duplicate([1, 2, 3, 1]))   # Output: True
```

**Analysis (say this out loud in an interview):**
1. "There's an outer loop running n times."
2. "Inside it, another loop running up to n times."
3. "Nested loops over the same input → **O(n²) time**."
4. "I only use a few variables, no extra data structures → **O(1) space**."
5. "We could do better — using a set, we can solve this in O(n) time." *(That's Day 3!)*

Here's that faster version as a teaser:

```python
def has_duplicate_fast(nums):
    seen = set()              # O(n) extra space
    for num in nums:          # single loop: O(n) time
        if num in seen:       # set lookup is O(1)!
            return True
        seen.add(num)
    return False

print(has_duplicate_fast([1, 2, 3, 1]))   # Output: True
# Time: O(n), Space: O(n) — traded memory for speed!
```

**We turned O(n²) into O(n).** That's the single most common improvement in all of interview prep, and you'll do it constantly.

---

## Common Big-O Cheat Sheet for Python Operations

Memorize these — they come up constantly:

| Operation | Complexity | Note |
|---|---|---|
| `list[i]` (access by index) | O(1) | direct lookup |
| `list.append(x)` | O(1) | adds to end |
| `list.pop()` (from end) | O(1) | |
| `list.pop(0)` (from front) | **O(n)** | shifts everything! avoid |
| `x in list` (search) | **O(n)** | scans the whole list |
| `x in set` / `x in dict` | **O(1)** | hash lookup — fast! |
| `dict[key]` | O(1) | hash lookup |
| `list.sort()` | O(n log n) | uses Timsort |
| `len(list)` | O(1) | Python tracks the length |

> **Sheetal's gotcha:** `x in my_list` is O(n) but `x in my_set` is O(1). If you're checking membership in a loop, **use a set**. This one fact fixes countless slow solutions.

---

## How to Talk About Big-O in Interviews

When asked "what's the complexity?", structure your answer:

1. **State time complexity** with reasoning: *"It's O(n) time because I loop through the array once."*
2. **State space complexity** with reasoning: *"It's O(n) space because I store every element in a hash map."*
3. **Mention the trade-off** if relevant: *"I used extra space to avoid the nested loop, bringing time down from O(n²) to O(n)."*

This shows you don't just memorize — you *understand*.

---

## Practice Exercises

These are about *analyzing* complexity (the skill you'll use on every problem). Try them on paper first, then verify your reasoning. The LeetCode problems will exercise the patterns we analyzed.

### Exercise 1: Analyze These Functions (paper exercise)
For each function below, state the **time** and **space** complexity. Write your reasoning in one sentence.

```python
# (a)
def func_a(nums):
    return nums[0] + nums[-1]

# (b)
def func_b(nums):
    total = 0
    for n in nums:
        total += n
    return total

# (c)
def func_c(nums):
    pairs = []
    for i in nums:
        for j in nums:
            pairs.append((i, j))
    return pairs

# (d)
def func_d(n):
    while n > 1:
        n = n // 2
    return n
```
*(Answers: a) O(1)/O(1)  b) O(n)/O(1)  c) O(n²)/O(n²)  d) O(log n)/O(1))*

### Exercise 2: "Contains Duplicate" — LeetCode #217 (Easy)
Given an array, return `True` if any value appears at least twice.
- First write the O(n²) nested-loop version.
- Then rewrite it as O(n) using a set.
- State both complexities. This problem *is* the lesson above.

### Exercise 3: "Two Sum" — LeetCode #1 (Easy)
Given an array and a target, return indices of two numbers that add up to the target.
- Try the brute-force O(n²) version first.
- Note what its complexity is (you'll learn the O(n) hash-map trick on Day 3 — don't worry if you can't beat O(n²) yet).

### Exercise 4: "Maximum Subarray" — LeetCode #53 (Medium)
Find the contiguous subarray with the largest sum.
- Don't worry about solving it perfectly yet — just write *any* working solution and **analyze its Big-O**. Practicing the analysis is the goal today.

---

## Quick Reference Card

```
BIG-O FROM BEST TO WORST:
  O(1) < O(log n) < O(n) < O(n log n) < O(n²) < O(2ⁿ) < O(n!)

HOW TO READ CODE:
  single loop          → O(n)
  nested loop          → O(n²)
  halving each step    → O(log n)
  constant work        → O(1)

SIMPLIFY:
  drop constants:   O(2n)    → O(n)
  drop small terms: O(n²+n)  → O(n²)

REMEMBER:
  "x in set"  = O(1)   ✅ fast
  "x in list" = O(n)   ⚠️ slow
```

---

## What's Next?

You now speak the language of interviews! Tomorrow in **Day 02 — Arrays and Strings**, we'll learn the two most powerful patterns in all of interviewing: **two pointers** and **sliding window**. These show up in ~40% of problems, and you'll use today's Big-O skills to prove your solutions are fast.

You just unlocked the foundation that everything else builds on. Onward!
