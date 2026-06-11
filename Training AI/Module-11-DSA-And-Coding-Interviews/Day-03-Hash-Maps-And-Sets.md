# Module 11 — Day 3: Hash Maps and Sets

## The Single Most Important Tool in Interviews

If Day 2's patterns cover 40% of problems, **hash maps are the secret weapon** that cracks a huge fraction of the rest. Here's the magic sentence that interviewers love to hear:

> "I can use a hash map to do that lookup in **O(1)** instead of scanning the array in O(n)."

That one move — replacing a search with a hash lookup — is the most common way to turn a slow **O(n²)** solution into a fast **O(n)** one. Today you'll learn exactly when and how.

---

## What's a Hash Map? (intuition first)

A **hash map** (Python calls it a `dict`) is like a **magical filing cabinet**:

- You give it a **key** ("Sheetal's phone number").
- It instantly hands you the **value** — without searching through every drawer.

```
   KEY              VALUE
   ─────────────────────────
   "apple"    →     3
   "banana"   →     5
   "cherry"   →     2
```

The "magic" is **hashing**: the computer runs the key through a math function that points *directly* to where the value lives in memory. No scanning. That's why lookups are **O(1)** — constant time, no matter how big the map gets.

### Hash Map vs. List — the speed difference

| Task | List | Hash Map / Set |
|---|---|---|
| Find if `x` exists | O(n) — scan everything | **O(1)** — instant |
| Get value for a key | O(n) — scan | **O(1)** |
| Add an item | O(1) (append) | **O(1)** |

> **The whole point:** Any time you find yourself writing `if x in my_list:` inside a loop, stop — that's O(n) inside O(n) = O(n²). Use a set or dict to make it O(1).

---

## Dictionaries (Hash Maps) in Python

```python
# Create a dict
fruit_count = {"apple": 3, "banana": 5, "cherry": 2}

# Look up a value — O(1)
print(fruit_count["apple"])        # 3

# Safe lookup (no crash if key missing) — returns default
print(fruit_count.get("mango", 0)) # 0  (mango isn't there → default 0)

# Add or update — O(1)
fruit_count["mango"] = 7
fruit_count["apple"] = 10          # overwrites the old value

# Check if a key exists — O(1)
print("banana" in fruit_count)     # True
print("grape" in fruit_count)      # False

# Delete a key
del fruit_count["cherry"]

# Loop over keys, values, or both
for key in fruit_count:
    print(key, fruit_count[key])

for key, value in fruit_count.items():
    print(f"{key}: {value}")
```

> **Use `.get(key, default)`** to avoid `KeyError` crashes. This is cleaner than checking `if key in d` first.

---

## Sets in Python

A **set** is a hash map with only keys (no values). It stores **unique** items and gives O(1) membership checks.

```python
# Create a set
seen = set()
seen.add(5)
seen.add(10)
seen.add(5)          # duplicate — ignored, sets only keep uniques

print(seen)          # {10, 5}
print(5 in seen)     # True   — O(1) lookup!
print(99 in seen)    # False

# Build a set from a list (instantly removes duplicates)
nums = [1, 2, 2, 3, 3, 3]
unique = set(nums)   # {1, 2, 3}
print(len(unique))   # 3
```

> **Use a set when** you only care "have I seen this before?" — you don't need an associated value.

---

## Pattern 1: Frequency Counting

"Count how many times each thing appears" is one of the most common sub-tasks in interviews. Hash maps are perfect for it.

```python
def count_chars(s):
    counts = {}
    for c in s:
        # if c isn't in the dict yet, .get returns 0
        counts[c] = counts.get(c, 0) + 1
    return counts

print(count_chars("banana"))
# Output: {'b': 1, 'a': 3, 'n': 2}
```

Python even gives you a built-in for this — **`Counter`** from the `collections` module:

```python
from collections import Counter

counts = Counter("banana")
print(counts)                 # Counter({'a': 3, 'n': 2, 'b': 1})
print(counts["a"])            # 3
print(counts.most_common(2))  # [('a', 3), ('n', 2)]  — top 2 most frequent
```

> **Interview-ready tip:** `Counter` is your friend for any "most frequent", "anagram", or "count" problem. Interviewers are happy when you use it (it shows Python fluency).

### Classic Problem: Valid Anagram

Two strings are anagrams if they have the same characters in the same counts ("listen" / "silent").

```python
from collections import Counter

def is_anagram(s, t):
    # Anagrams must have identical character counts
    return Counter(s) == Counter(t)

print(is_anagram("listen", "silent"))   # Output: True
print(is_anagram("hello", "world"))     # Output: False
```

**O(n)** time — we count each string once. Compare that to sorting both strings (O(n log n)) — the hash map approach is faster *and* cleaner.

---

## Pattern 2: The "Two Sum" Pattern (the most famous interview move)

On Day 1, you wrote Two Sum as O(n²). Today you'll crush it in **O(n)** with a hash map. This single idea unlocks dozens of problems.

**The problem:** Given an array and a target, return the indices of the two numbers that add up to the target.

**The brute force (slow):**

```python
def two_sum_slow(nums, target):
    for i in range(len(nums)):
        for j in range(i + 1, len(nums)):
            if nums[i] + nums[j] == target:
                return [i, j]
    return []
# O(n²) — nested loops
```

**The hash map magic (fast):**

The key insight: as you walk through the array, for each number `x` you need its **complement** `target - x`. Instead of *searching* the array for the complement (O(n)), store everything you've seen in a dict and *look it up* (O(1)).

```python
def two_sum(nums, target):
    seen = {}                       # maps value → its index
    for i, num in enumerate(nums):
        complement = target - num   # the number we still need
        if complement in seen:      # O(1) lookup!
            return [seen[complement], i]
        seen[num] = i               # remember this number and its index
    return []

print(two_sum([2, 7, 11, 15], 9))   # Output: [0, 1]  (2 + 7 = 9)
print(two_sum([3, 2, 4], 6))         # Output: [1, 2]  (2 + 4 = 6)
```

**Walking through `[2, 7, 11, 15]`, target `9`:**

```
i=0, num=2:  need 9-2=7. Is 7 in seen? No.  → store {2:0}
i=1, num=7:  need 9-7=2. Is 2 in seen? YES! → return [seen[2], 1] = [0, 1]
```

We found the answer in **one pass**: **O(n) time, O(n) space**. We traded a little memory (the `seen` dict) for a massive speed gain. *This is the trade-off Day 1 talked about.*

> **The "complement" trick is everywhere.** "Find two things that combine to X" → store what you've seen, look up what you need. Burn this into your brain.

---

## Pattern 3: Grouping with Hash Maps

When you need to **group** items by some shared property, use a dict mapping `property → list of items`.

### Classic Problem: Group Anagrams

Group words that are anagrams of each other.

```python
from collections import defaultdict

def group_anagrams(words):
    groups = defaultdict(list)      # missing keys default to an empty list
    for word in words:
        # The sorted letters are identical for all anagrams → perfect key
        key = "".join(sorted(word))   # "eat" → "aet", "tea" → "aet"
        groups[key].append(word)
    return list(groups.values())

print(group_anagrams(["eat", "tea", "tan", "ate", "nat", "bat"]))
# Output: [['eat', 'tea', 'ate'], ['tan', 'nat'], ['bat']]
```

> **`defaultdict(list)`** auto-creates an empty list for any new key, so you can `.append()` without first checking `if key in groups`. Cleaner code.

The signature `sorted(word)` is the **canonical form** — all anagrams map to the same key. This "use a canonical form as the key" idea is a powerful pattern by itself.

---

## Pattern 4: Set for Deduplication & Membership

### Classic Problem: Contains Duplicate (the O(n) version)

Remember Day 1's `has_duplicate_fast`? Here it is again, now that you fully understand *why* it's fast:

```python
def contains_duplicate(nums):
    seen = set()
    for num in nums:
        if num in seen:        # O(1) check
            return True
        seen.add(num)          # O(1) add
    return False

print(contains_duplicate([1, 2, 3, 1]))   # Output: True
print(contains_duplicate([1, 2, 3, 4]))   # Output: False
```

Even simpler, using the fact that sets drop duplicates:

```python
def contains_duplicate_oneliner(nums):
    # If the set is smaller than the list, there were duplicates
    return len(set(nums)) < len(nums)
```

Both are **O(n)**.

---

## Pattern 5: Caching / Memoization (preview of Day 6)

Hash maps are how we **remember answers we've already computed** so we never redo work. This is the heart of *memoization* and *dynamic programming* (Day 6). A tiny taste:

```python
def fib(n, memo={}):
    if n <= 1:
        return n
    if n in memo:              # already computed? return instantly — O(1)
        return memo[n]
    memo[n] = fib(n-1, memo) + fib(n-2, memo)   # compute & remember
    return memo[n]

print(fib(30))   # Output: 832040  (instant, thanks to the memo dict!)
```

Without the memo dict, computing `fib(30)` recomputes the same values *millions* of times. With it, each value is computed once. The hash map is what makes it fast. More on Day 6!

---

## When to Reach for a Hash Map or Set (decision guide)

```
Problem says...                          →  Use...
──────────────────────────────────────────────────────────
"have I seen this before?"               →  Set
"count occurrences / most frequent"      →  dict or Counter
"find a pair summing to target"          →  dict (complement trick)
"are these anagrams?"                    →  Counter or sorted-key
"group items by property"                →  defaultdict(list)
"remember computed results"              →  dict (memoization)
"remove duplicates"                      →  set(my_list)
"checking 'in' inside a loop is slow"    →  convert list to set first
```

> **The golden rule:** If your solution has a nested loop searching for something, ask: *"Can a hash map make that inner search O(1)?"* Nine times out of ten, yes.

---

## Practice Exercises

State the Big-O before and after using a hash map — the improvement is the whole point.

### Exercise 1: "Two Sum" — LeetCode #1 (Easy)
Implement the O(n) hash-map version from scratch. This is *the* most-asked interview question. Know it cold.

### Exercise 2: "Valid Anagram" — LeetCode #242 (Easy)
Determine if two strings are anagrams.
- Solve it with `Counter`.
- Then solve it with a plain dict (count one string up, count the other down, check all zeros). Knowing both shows depth.

### Exercise 3: "Group Anagrams" — LeetCode #49 (Medium)
Group a list of words into anagram groups. Use the `defaultdict` + sorted-key pattern above.

### Exercise 4: "Top K Frequent Elements" — LeetCode #347 (Medium)
Given an array, return the `k` most frequent elements.
- Use `Counter` and `.most_common(k)`.
- Very common at Microsoft & Amazon. Aim for **O(n log k)** or better.

### Exercise 5: "Longest Consecutive Sequence" — LeetCode #128 (Medium)
Find the length of the longest run of consecutive integers (e.g., `[100,4,200,1,3,2]` → `4` for `1,2,3,4`).
- The trick: put everything in a **set** for O(1) lookups, then only start counting from numbers that are the *start* of a sequence. Aim for **O(n)** — a beautiful hash-set problem.

---

## Quick Reference Card

```python
# DICT (hash map)
d = {}
d[key] = value           # add/update     O(1)
d.get(key, default)      # safe lookup    O(1)
key in d                 # membership      O(1)
for k, v in d.items():   # iterate

# COUNTER (frequency counting)
from collections import Counter
c = Counter(items)
c.most_common(k)         # top k frequent

# DEFAULTDICT (auto-init values)
from collections import defaultdict
groups = defaultdict(list)
groups[key].append(item)  # no "if key in" needed

# SET (uniqueness + fast membership)
s = set()
s.add(x)                 # O(1)
x in s                   # O(1)
set(my_list)             # dedupe a list

# THE TWO-SUM / COMPLEMENT PATTERN:
seen = {}
for i, num in enumerate(nums):
    if target - num in seen:
        return [seen[target - num], i]
    seen[num] = i
```

---

## What's Next?

You now hold the key to the most common interview optimization: **trade memory for speed with a hash map**. Tomorrow in **Day 04 — Stacks, Queues, and Linked Lists**, we'll learn the linear data structures that power everything from undo buttons to graph traversal. These come with their own crisp set of patterns. You're three days in and already dangerous — keep it up!
