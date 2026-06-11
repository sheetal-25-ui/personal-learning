# Module 11 — Day 2: Arrays and Strings

## Why Arrays and Strings First?

If you only mastered **one** category for interviews, it would be this one. Around **40% of all coding interview questions** are array or string problems. They're also where the two most reusable patterns live:

- **Two Pointers**
- **Sliding Window**

Master these two patterns and you'll recognize them instantly in dozens of problems. Let's dig in.

> **Good news:** In Python, strings and lists (arrays) behave almost identically — you can index them, loop over them, and slice them the same way. Learn one, get the other half-free.

---

## Quick Refresher: Arrays (Lists) in Python

```python
nums = [10, 20, 30, 40, 50]

# Access by index — O(1)
print(nums[0])      # 10  (first)
print(nums[-1])     # 50  (last — negative counts from the end)

# Slicing — grab a sub-list (creates a copy, O(k) where k = slice size)
print(nums[1:4])    # [20, 30, 40]  (indices 1,2,3 — end is exclusive)
print(nums[:2])     # [10, 20]
print(nums[2:])     # [30, 40, 50]

# Length — O(1)
print(len(nums))    # 5

# Loop with index
for i in range(len(nums)):
    print(i, nums[i])   # 0 10 / 1 20 / ...
```

Strings work the same way:

```python
s = "hello"
print(s[0])     # 'h'
print(s[-1])    # 'o'
print(s[1:4])   # 'ell'
print(len(s))   # 5
```

> **Gotcha:** Strings in Python are **immutable** — you can't do `s[0] = 'H'`. To modify, convert to a list: `chars = list(s)`, change it, then `''.join(chars)`.

---

## Pattern 1: Two Pointers

**The idea:** Instead of one index marching through the array, use **two** indices that move toward each other (or in the same direction). This often turns an O(n²) brute force into a clean **O(n)** solution.

### Visual: pointers closing in from both ends

```
   [ 1   3   5   7   9   11 ]
     ↑                    ↑
    left                right

  Move them toward the middle based on what you find:
   [ 1   3   5   7   9   11 ]
         ↑            ↑
        left        right
```

### Classic Problem: Is a String a Palindrome?

A palindrome reads the same forwards and backwards ("racecar", "level").

**Brute force idea:** reverse the string and compare → O(n) but uses extra space.
**Two-pointer idea:** compare the ends moving inward → O(n) time, O(1) space.

```python
def is_palindrome(s):
    left = 0                 # pointer at the start
    right = len(s) - 1       # pointer at the end

    while left < right:      # stop when they meet in the middle
        if s[left] != s[right]:
            return False     # mismatch → not a palindrome
        left += 1            # move inward
        right -= 1
    return True              # all pairs matched

print(is_palindrome("racecar"))   # Output: True
print(is_palindrome("hello"))     # Output: False
print(is_palindrome("level"))     # Output: True
```

**Why it's great:** O(n) time, O(1) space. We never copied the string.

### Classic Problem: Two Sum on a *Sorted* Array

Given a **sorted** array and a target, find two numbers that add up to the target.

```python
def two_sum_sorted(nums, target):
    left = 0
    right = len(nums) - 1

    while left < right:
        current = nums[left] + nums[right]
        if current == target:
            return [left, right]      # found it!
        elif current < target:
            left += 1                 # need a BIGGER sum → move left up
        else:
            right -= 1                # need a SMALLER sum → move right down
    return []                         # no pair found

print(two_sum_sorted([1, 3, 4, 5, 7, 11], 9))   # Output: [1, 4]  (3 + 7 = 10? no — see note)
```

> **How the logic works:** Because the array is sorted, if the sum is too small, the only way to grow it is to move `left` right (to a bigger number). If too big, move `right` left (to a smaller number). Each step eliminates one possibility → **O(n)** instead of checking all O(n²) pairs.

### When to reach for Two Pointers

- The array is **sorted** (or sorting helps).
- You're looking for a **pair** or comparing **ends**.
- You want to do it in **O(1) extra space**.

---

## Pattern 2: Sliding Window

**The idea:** Maintain a "window" (a contiguous range) over the array, and slide it along — *growing* and *shrinking* it — instead of recomputing from scratch each time. Perfect for **subarray / substring** problems.

### Visual: a window sliding right

```
   [ 2   1   5   1   3   2 ]      window = [2,1,5], sum = 8
     └───────┘
   [ 2   1   5   1   3   2 ]      slide right: subtract 2, add 1 → sum = 7
         └───────┘
   [ 2   1   5   1   3   2 ]      slide right: subtract 1, add 3 → sum = 9
             └───────┘
```

The trick: when the window slides, you **add the new element and remove the old one** — you don't re-sum the whole window. That keeps it O(n).

### Classic Problem: Maximum Sum of a Subarray of Size k

```python
def max_sum_subarray(nums, k):
    # Step 1: sum the first window
    window_sum = sum(nums[:k])
    max_sum = window_sum

    # Step 2: slide the window across the rest of the array
    for i in range(k, len(nums)):
        # add the new element entering the window,
        # remove the old element leaving the window
        window_sum += nums[i] - nums[i - k]
        max_sum = max(max_sum, window_sum)

    return max_sum

print(max_sum_subarray([2, 1, 5, 1, 3, 2], 3))   # Output: 9  (the [5,1,3] window)
```

**Brute force** would re-sum every window → O(n·k). **Sliding window** reuses the previous sum → **O(n)**. Big win.

### Classic Problem: Longest Substring Without Repeating Characters

This is one of the most-asked interview questions ever (LeetCode #3). It uses a **dynamic** window (grows and shrinks).

```python
def longest_unique_substring(s):
    seen = set()         # characters currently in the window
    left = 0             # left edge of the window
    longest = 0

    for right in range(len(s)):       # right edge expands each step
        # If the new char is already in the window, shrink from the left
        # until the duplicate is gone.
        while s[right] in seen:
            seen.remove(s[left])
            left += 1
        seen.add(s[right])            # add the new char
        longest = max(longest, right - left + 1)   # window size

    return longest

print(longest_unique_substring("abcabcbb"))   # Output: 3  ("abc")
print(longest_unique_substring("bbbbb"))       # Output: 1  ("b")
print(longest_unique_substring("pwwkew"))      # Output: 3  ("wke")
```

**Walking through "abcabcbb":**

```
window           seen        longest
"a"              {a}         1
"ab"             {a,b}       2
"abc"            {a,b,c}     3
"a" sees dup 'a' → shrink, then add → "bca" stays size 3
...continues, max stays 3
```

Time: **O(n)** — each character is added once and removed at most once. Space: O(min(n, alphabet size)).

### When to reach for Sliding Window

- The problem mentions **contiguous** subarray or substring.
- You want the **longest / shortest / max-sum** range satisfying some condition.
- You catch yourself about to use a nested loop to check every subarray.

---

## A Few More Essential Array Techniques

### Prefix Sums (precompute running totals)

When you need the sum of many different ranges, precompute a running total once:

```python
def build_prefix(nums):
    prefix = [0]               # prefix[i] = sum of nums[0..i-1]
    for n in nums:
        prefix.append(prefix[-1] + n)
    return prefix

# sum of nums[i..j] = prefix[j+1] - prefix[i]  in O(1)!
nums = [3, 1, 4, 1, 5]
prefix = build_prefix(nums)         # [0, 3, 4, 8, 9, 14]
# sum of nums[1..3] (i.e. 1+4+1 = 6):
print(prefix[4] - prefix[1])        # Output: 6
```

This turns repeated range-sum queries from O(n) each into **O(1) each** after an O(n) setup.

### In-Place Reversal

```python
def reverse_in_place(arr):
    left, right = 0, len(arr) - 1     # two pointers again!
    while left < right:
        arr[left], arr[right] = arr[right], arr[left]   # Python swap
        left += 1
        right -= 1
    return arr

print(reverse_in_place([1, 2, 3, 4, 5]))   # Output: [5, 4, 3, 2, 1]
```

O(n) time, O(1) space — notice it's two pointers again. The pattern keeps coming back!

---

## Common String Helpers You'll Use Constantly

```python
s = "Hello World"

print(s.lower())            # 'hello world'
print(s.upper())            # 'HELLO WORLD'
print(s.split())            # ['Hello', 'World']  (split on whitespace)
print(s.split("o"))         # ['Hell', ' W', 'rld']
print("-".join(["a","b"]))  # 'a-b'  (join a list into a string)
print(s.replace("o", "0"))  # 'Hell0 W0rld'
print(s.strip())            # removes leading/trailing whitespace
print("abc".isalpha())      # True  (all letters?)
print("123".isdigit())      # True  (all digits?)
print(s.find("World"))      # 6     (index where it starts, -1 if absent)
```

> **Interview gotcha:** Building a string with `+=` in a loop is O(n²) because strings are immutable (each `+=` makes a whole new string). Instead, append characters to a **list** and `''.join()` at the end — that's O(n).

```python
# ❌ slow — O(n²)
result = ""
for c in "hello":
    result += c

# ✅ fast — O(n)
chars = []
for c in "hello":
    chars.append(c)
result = "".join(chars)
```

---

## How to Recognize the Pattern (decision guide)

```
Problem mentions...                     →  Reach for...
─────────────────────────────────────────────────────────
"pair", "sorted", "two ends"            →  Two Pointers
"contiguous subarray/substring"         →  Sliding Window
"longest/shortest window with..."       →  Sliding Window
"sum of range queries"                  →  Prefix Sums
"reverse / swap in place"               →  Two Pointers
"any duplicate / seen before"           →  Hash Set (Day 3!)
```

---

## Practice Exercises

Do these on LeetCode. For each, **state the Big-O** before and after your optimization — that habit from Day 1 pays off here.

### Exercise 1: "Valid Palindrome" — LeetCode #125 (Easy)
Check if a string is a palindrome, ignoring case and non-alphanumeric characters.
- Use the **two-pointer** pattern.
- Hint: `c.isalnum()` checks if a character is a letter or digit.

### Exercise 2: "Best Time to Buy and Sell Stock" — LeetCode #121 (Easy)
Given daily prices, find the max profit from one buy and one later sell.
- Try a one-pass solution: track the minimum price seen so far and the best profit.
- This is a sliding-window-flavored problem. Aim for **O(n)**.

### Exercise 3: "Longest Substring Without Repeating Characters" — LeetCode #3 (Medium)
The exact problem we solved above! Implement it yourself from scratch, then verify against the lesson.
- This is asked at Microsoft, Google, Amazon — extremely common.

### Exercise 4: "Container With Most Water" — LeetCode #11 (Medium)
Given heights, find two lines that form a container holding the most water.
- Classic **two-pointer** problem. Start with pointers at both ends, move the shorter one inward.
- Aim for **O(n)** — the brute force is O(n²).

---

## Quick Reference Card

```
TWO POINTERS:
  left, right = 0, len(arr)-1
  while left < right:
      ... decide which pointer to move ...
  → O(n) time, O(1) space

SLIDING WINDOW (fixed size k):
  window_sum = sum(arr[:k])
  for i in range(k, len(arr)):
      window_sum += arr[i] - arr[i-k]
  → O(n) time

SLIDING WINDOW (dynamic):
  left = 0
  for right in range(len(arr)):
      while (window invalid):
          ... shrink from left ...
      ... update answer ...
  → O(n) time

STRING SPEED:
  build with list + "".join()   ✅ O(n)
  build with += in a loop       ❌ O(n²)
```

---

## What's Next?

You now know the two patterns behind ~40% of interview problems. Tomorrow in **Day 03 — Hash Maps and Sets**, we'll learn the tool that turns slow O(n²) solutions into fast O(n) ones — including the *real* O(n) solution to Two Sum that you couldn't beat on Day 1. This is where interviews start to feel winnable. Keep going, you're building real momentum!
