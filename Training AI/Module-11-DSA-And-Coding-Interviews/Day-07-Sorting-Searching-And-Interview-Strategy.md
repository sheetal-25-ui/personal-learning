# Module 11 — Day 7: Sorting, Searching, and Interview Strategy

## The Finale: Tools + Tactics

You've built real DSA muscle this week. Today has two halves:

1. **Technical:** sorting algorithms (what to know) and **binary search** (your O(log n) superpower).
2. **Strategic:** how to *actually behave* in the interview — the part that separates people who *know* the answer from people who *get the offer*.

The second half might be the most valuable thing in this entire module. Let's finish strong.

---

## Part 1: Sorting Algorithms

### The good news about sorting

In a real interview, you almost never implement a sorting algorithm from scratch — you just call Python's built-in `sorted()` (which is **O(n log n)** and excellent). What interviewers *do* expect is that you **understand** the trade-offs and can discuss them.

```python
nums = [5, 2, 8, 1, 9, 3]

print(sorted(nums))                   # [1, 2, 3, 5, 8, 9]  — returns a NEW list
nums.sort()                            # sorts IN PLACE (modifies nums)
print(sorted(nums, reverse=True))      # [9, 8, 5, 3, 2, 1]  — descending

# Sort by a custom key — VERY common in interviews
words = ["python", "ai", "code"]
print(sorted(words, key=len))          # ['ai', 'code', 'python']  — by length

people = [("Asha", 30), ("Ben", 25), ("Chen", 35)]
print(sorted(people, key=lambda p: p[1]))   # sort by age (2nd element)
# Output: [('Ben', 25), ('Asha', 30), ('Chen', 35)]
```

> **Master `sorted(..., key=...)`.** Sorting by a custom key (length, second element, absolute value, etc.) appears in tons of problems. Python's `sorted` uses **Timsort** — O(n log n), stable, and battle-tested.

### The sorting algorithms to *know about* (conceptually)

You should be able to explain these at a high level:

| Algorithm | Time (average) | Space | Key idea | Stable? |
|---|---|---|---|---|
| **Bubble Sort** | O(n²) | O(1) | Repeatedly swap adjacent out-of-order pairs | Yes |
| **Selection Sort** | O(n²) | O(1) | Repeatedly pick the smallest, place it | No |
| **Insertion Sort** | O(n²) | O(1) | Build sorted part one item at a time | Yes |
| **Merge Sort** | **O(n log n)** | O(n) | Split in half, sort each, merge | Yes |
| **Quick Sort** | **O(n log n)** avg | O(log n) | Pick a pivot, partition around it | No |
| **Timsort** (Python) | **O(n log n)** | O(n) | Hybrid merge + insertion (real-world fast) | Yes |

> **The key takeaway:** the simple sorts (bubble/selection/insertion) are O(n²) and only for tiny inputs. The "good" sorts are **O(n log n)** — that's the best a comparison sort can do. Know that **merge sort** is reliably O(n log n), and **quicksort** is fast on average but O(n²) worst case.

### Merge Sort — the one worth seeing (it teaches divide-and-conquer)

Merge sort embodies **divide and conquer**, a pattern that shows up far beyond sorting:

```
Split the array in half, sort each half, then merge them together:

         [5, 2, 8, 1]
         /          \
     [5, 2]        [8, 1]        ← split
     /   \         /   \
   [5]   [2]     [8]   [1]       ← single elements are "sorted"
     \   /         \   /
     [2, 5]        [1, 8]        ← merge sorted halves
         \          /
         [1, 2, 5, 8]            ← final merge
```

```python
def merge_sort(arr):
    if len(arr) <= 1:                 # base case: 0 or 1 element is already sorted
        return arr
    mid = len(arr) // 2
    left = merge_sort(arr[:mid])      # divide & conquer the left half
    right = merge_sort(arr[mid:])     # ...and the right half
    return merge(left, right)         # combine

def merge(left, right):
    result = []
    i = j = 0
    while i < len(left) and j < len(right):   # two pointers (Day 2!)
        if left[i] <= right[j]:
            result.append(left[i]); i += 1
        else:
            result.append(right[j]); j += 1
    result.extend(left[i:])           # append any leftovers
    result.extend(right[j:])
    return result

print(merge_sort([5, 2, 8, 1, 9, 3]))   # Output: [1, 2, 3, 5, 8, 9]
```

Notice it reuses **recursion** (Day 6) and **two pointers** (Day 2). Everything connects!

---

## Part 2: Binary Search — Your O(log n) Superpower

Binary search is the single most important search algorithm in interviews. It finds an item in a **sorted** array in **O(log n)** by repeatedly halving the search space.

### The intuition: guessing a number 1–100

If I'm thinking of a number 1–100 and you guess 50, I say "higher" or "lower." You just **eliminated half** the possibilities. Guess again in the middle of what's left. You'll find it in ~7 guesses, not 100. That's binary search.

```
Searching for 7 in [1, 3, 5, 7, 9, 11, 13]:

[1, 3, 5, 7, 9, 11, 13]   mid=7? found? no wait — let's trace target=9
 ↑        ↑           ↑
 lo      mid          hi    mid value = 7 < 9 → search RIGHT half

         [9, 11, 13]
          ↑   ↑    ↑
          lo mid   hi        mid value = 11 > 9 → search LEFT half

         [9]
          ↑ found! 
```

### The binary search template (memorize this exactly)

```python
def binary_search(nums, target):
    lo, hi = 0, len(nums) - 1        # search the whole array

    while lo <= hi:                   # while there's still a range to search
        mid = (lo + hi) // 2          # middle index
        if nums[mid] == target:
            return mid                # found it!
        elif nums[mid] < target:
            lo = mid + 1              # target is bigger → search RIGHT half
        else:
            hi = mid - 1              # target is smaller → search LEFT half

    return -1                         # not found

print(binary_search([1, 3, 5, 7, 9, 11, 13], 9))   # Output: 4
print(binary_search([1, 3, 5, 7, 9, 11, 13], 6))   # Output: -1
```

> **Three things to get right** (these are the common bugs):
> 1. `while lo <= hi` (note the `<=`, not `<`).
> 2. `mid = (lo + hi) // 2`.
> 3. Move `lo = mid + 1` or `hi = mid - 1` (the `+1`/`-1` prevents infinite loops).
>
> Write this template enough times that it's automatic.

### When can you use binary search?

- The array (or answer space) is **sorted** or **monotonic**.
- The problem says "find", "search", or asks for a value where some condition flips from false→true.
- A keyword to listen for: **"sorted"**. If the input is sorted, binary search should cross your mind.

> **Python shortcut:** the `bisect` module does binary search for you:
> ```python
> import bisect
> nums = [1, 3, 5, 7, 9]
> print(bisect.bisect_left(nums, 5))   # 2  (index where 5 is / would go)
> ```
> Know it exists, but also know how to write binary search by hand.

---

## Part 3: Interview Strategy — How to Actually Behave

This is the part that wins offers. Two candidates can know the same algorithm — the one who **communicates and approaches it well** gets hired. Here's the playbook.

### The 6-Step Framework (UMPIRE)

Use this exact structure for *every* problem:

```
U  — UNDERSTAND   Restate the problem. Ask clarifying questions.
M  — MATCH        Which pattern is this? (two pointers? hash map? DP?)
P  — PLAN         Describe your approach in plain English BEFORE coding.
I  — IMPLEMENT    Write clean code while narrating what you're doing.
R  — REVIEW       Walk through your code with a real example.
E  — EVALUATE     State time & space complexity. Discuss improvements.
```

Let's break down the most important moves.

### Step 1: Understand — Ask Questions FIRST (don't dive in!)

The #1 mistake candidates make: they start coding immediately. **Don't.** First, ask clarifying questions. This shows maturity and catches misunderstandings.

Good questions to ask on almost any problem:
- "Can the input be empty? What should I return then?"
- "Are there duplicates? Can numbers be negative?"
- "Is the array sorted?"
- "How large can the input get?" (hints at the expected complexity)
- "Should I optimize for time or space?"

> **Restate the problem in your own words** and confirm: *"So I'm given an array of integers and a target, and I need to return the indices of two numbers that sum to the target — is that right?"* The interviewer nods, and now you're aligned.

### Step 2: Match — Name the Pattern Out Loud

Think back through the week:

```
"two numbers summing to target"     →  hash map / complement (Day 3)
"contiguous subarray"               →  sliding window (Day 2)
"sorted array, find pair"           →  two pointers (Day 2)
"level by level"                    →  BFS (Day 5)
"how many ways / max / min"         →  dynamic programming (Day 6)
"sorted, find a value"              →  binary search (Day 7)
"matching/undo"                     →  stack (Day 4)
```

Say it: *"This looks like a sliding window problem because we're dealing with a contiguous subarray."* This is gold — it shows you recognize patterns, not just memorize.

### Step 3: Plan — Talk Before You Type

Describe your approach **in plain English** and get a thumbs-up before writing code:

> *"My plan: I'll use a hash map to store each number's index as I go. For each number, I'll check if its complement — target minus the number — is already in the map. If so, I've found my pair. This is one pass, so O(n) time and O(n) space. Sound good?"*

Often the interviewer will nod (great, keep going) or nudge you (saving you from a wrong path). **Either outcome is a win.**

> **THINK OUT LOUD the entire time.** Silence is your enemy. The interviewer is evaluating your *thought process*, not just your final code. Even when stuck, narrate: *"Hmm, the brute force is O(n²)... I'm wondering if sorting first would help here..."*

### Step 4: Implement — Clean Code, Narrated

- Write **clean, readable** code with good variable names (`left`, `right`, `seen` — not `x`, `y`, `z`).
- Narrate as you type: *"I'll initialize an empty dictionary called seen..."*
- It's fine to start with a brute force and improve it: *"Let me get a working solution first, then optimize."* A working O(n²) beats a broken O(n).

### Step 5: Review — Trace Through an Example

After coding, **don't say "done."** Walk through your code with a concrete example, line by line, as if you're the computer:

> *"Let me trace through `[2, 7, 11, 15]` with target 9. i=0, num=2, complement=7, not in seen, store {2:0}. i=1, num=7, complement=2, it IS in seen, return [0, 1]. Correct!"*

This catches bugs **before** the interviewer points them out — which is exactly the impression you want to make.

### Step 6: Evaluate — State Complexity & Edge Cases

Close it out like a pro:
- *"Time complexity is O(n) because we do one pass. Space is O(n) for the hash map."*
- *"Edge cases I'd test: empty array, no valid pair, duplicate numbers, negative numbers."*

### Test Cases You Should Always Consider

```
✓ Empty input         []          ""        None
✓ Single element      [5]         "a"
✓ Two elements        [1, 2]
✓ All same            [3, 3, 3]
✓ Already sorted      [1, 2, 3]
✓ Reverse sorted      [3, 2, 1]
✓ Negative numbers    [-1, -5, 3]
✓ Duplicates          [1, 1, 2]
✓ The "no answer" case (target can't be made, etc.)
```

> **Mentioning edge cases — even if you don't code them all — signals senior-level thinking.** Always do it.

### If You Get Stuck (it happens to everyone)

1. **Say it out loud:** *"I'm stuck on how to avoid the nested loop here."* Interviewers often give hints — taking a hint gracefully is fine.
2. **Go back to brute force.** Get *something* working, then optimize. Partial credit is real.
3. **Try a small example by hand.** Working through `[1,2,3]` manually often reveals the pattern.
4. **Think about which pattern fits.** Run through the Day 1–7 list mentally.

### What Interviewers Are *Really* Scoring

```
┌─────────────────────────────────────────────────┐
│  Problem-solving    — how you APPROACH it         │
│  Coding             — clean, correct, bug-free    │
│  Communication      — thinking out loud           │
│  Complexity         — can you analyze Big-O?       │
│  Testing            — do you verify your code?     │
└─────────────────────────────────────────────────┘
```

Notice: **only one of these is "did you get the perfect answer."** A thoughtful, well-communicated, *mostly*-working solution beats a silent, perfect one. Communication is half the battle.

### Practical Logistics

- **Practice on a whiteboard or plain editor** sometimes (no autocomplete) — many interviews use bare environments.
- **Mock interviews** are gold: pramp.com and interviewing.io let you practice with real people for free.
- **Time yourself:** aim to solve a Medium in ~25-30 minutes including talking.
- **The week before:** review your "mistakes notebook," do easy problems to stay warm, sleep well. Don't cram new hard topics the night before.

---

## Practice Exercises

### Exercise 1: "Binary Search" — LeetCode #704 (Easy)
Implement the binary search template from scratch. Get the `lo <= hi` and `mid ± 1` exactly right. This is foundational.

### Exercise 2: "Search Insert Position" — LeetCode #35 (Easy)
Find the index where a target is, or where it would be inserted. A binary search variant — great for solidifying the template.

### Exercise 3: "Merge Sort" (implement it — LeetCode #912 "Sort an Array", Medium)
Implement merge sort yourself. It cements divide-and-conquer, recursion, and the two-pointer merge.

### Exercise 4: "Search in Rotated Sorted Array" — LeetCode #33 (Medium)
Binary search on a sorted array that's been rotated. A modified binary search — very commonly asked at Microsoft, Google, Amazon. A perfect capstone challenge.

### Exercise 5: Mock Interview Drill (no LeetCode — do this!)
Pick any Medium problem you've solved this week. Set a 30-minute timer. Solve it **out loud** using the full UMPIRE framework (record yourself or use a mock-interview site). Practicing the *talking* is as important as the coding.

---

## Quick Reference Card

```python
# SORTING
sorted(nums)                      # new sorted list,  O(n log n)
nums.sort()                       # sort in place
sorted(items, key=lambda x: x[1]) # custom key
sorted(nums, reverse=True)        # descending

# BINARY SEARCH (sorted input → O(log n))
lo, hi = 0, len(nums) - 1
while lo <= hi:
    mid = (lo + hi) // 2
    if nums[mid] == target: return mid
    elif nums[mid] < target: lo = mid + 1
    else: hi = mid - 1
return -1

# THE UMPIRE FRAMEWORK
# Understand → Match → Plan → Implement → Review → Evaluate

# INTERVIEW CHECKLIST
# □ Ask clarifying questions FIRST
# □ Name the pattern out loud
# □ Explain plan before coding
# □ Think out loud the whole time
# □ Trace through an example
# □ State time & space complexity
# □ Mention edge cases
```

---

## What's Next?

**Congratulations, Sheetal — you finished Module 11!**

Take a second to appreciate how far you've come. Seven days ago, "what's the time complexity?" was scary. Now you can:
- Analyze any algorithm's Big-O
- Wield two pointers, sliding windows, and hash maps
- Navigate stacks, queues, linked lists, trees, and graphs
- Tackle the most-feared topic — dynamic programming
- Run binary search in your sleep
- And most importantly, **carry yourself like a strong candidate in the room**

DSA is a skill that grows with practice. Keep working through your **150–300 problem plan** from the OVERVIEW (NeetCode 150 is your best friend), keep your mistakes notebook, and do mock interviews. Consistency beats intensity — a couple of problems a day, every day, will get you there.

**Next up: Module 12** — where we go beyond raw coding into **System Design and ML System Design**, the rounds that come *after* you ace these DSA interviews. That's where your AI/ML knowledge from earlier modules combines with the engineering rigor you built here. The DSA round gets you in the door; system design gets you the senior offer.

You've got the foundation. You've got the discipline. Big tech is absolutely within your reach. Now go solve some problems — I'm rooting for you!

➡️ **Continue to Module 12: System Design & ML System Design**
