# Week 1 — How to ANSWER in a Microsoft Interview 🎤 (Java)

> **The golden rule at Microsoft (and Google, Amazon, etc.):**
> Don't jump to the best code. **Talk through your thinking.**
> Always present in this order so the interviewer sees how you think:
>
> 1. **Clarify** the question (ask 1–2 questions)
> 2. **Brute Force** — "The simplest thing that works" (even if slow)
> 3. **Optimal** — "Here's how I make it faster"
> 4. **Best / Cleanest** — final code + edge cases
> 5. **Complexity** — Time & Space for each approach
>
> Saying *"My brute force is O(n²), but I can do better with a HashMap to get O(n)"*
> is what gets you hired — it shows you understand the trade-off.

---

## 🗣️ The Interview Script (memorize this flow)

```
Step 1: "Let me make sure I understand the problem..."  (repeat it back)
Step 2: "Can I clarify a few things?"                   (ask about edge cases)
Step 3: "Let me start with a brute-force approach..."   (always start simple)
Step 4: "The time complexity here is O(...). I think I can optimize..."
Step 5: "A better approach would be to use [HashMap/Two Pointers]..."
Step 6: "Let me code the optimal solution and walk through an example."
Step 7: "Edge cases: empty input, duplicates, negatives..."
Step 8: "Final complexity: Time O(...), Space O(...)."
```

**Clarifying questions that work for almost any array/string problem:**
- "Can the array be empty or null?"
- "Are there duplicates?"
- "Can numbers be negative? Is it sorted?"
- "Is there exactly one answer, or multiple?"
- "Do I need to optimize for time or space?"

---

# Problem 1: Two Sum ⭐ (Microsoft asks this ALL the time)

### 📋 Full Question
> Given an array of integers `nums` and an integer `target`, return the **indices** of the two numbers that add up to `target`. You may assume exactly one solution, and you can't use the same element twice.
>
> **Example:** `nums = [2, 7, 11, 15]`, `target = 9` → Output: `[0, 1]` (because `2 + 7 = 9`)

### 🗣️ What to say first
*"So I need to find two indices whose values sum to the target. Let me confirm — there's exactly one answer, and I can't reuse the same index, right?"*

### Approach 1 — Brute Force (start here!)
*"The simplest approach: check every pair of numbers."*

```java
public int[] twoSum(int[] nums, int target) {
    for (int i = 0; i < nums.length; i++) {
        for (int j = i + 1; j < nums.length; j++) {
            if (nums[i] + nums[j] == target) {
                return new int[] { i, j };
            }
        }
    }
    return new int[] {}; // no solution
}
```
- **Time:** O(n²) — nested loop checks every pair
- **Space:** O(1) — no extra storage
- *"This works, but O(n²) is slow for large arrays. I can do better."*

### Approach 2 — Optimal (HashMap) ✅
*"Instead of searching for the complement every time, I'll store what I've seen in a hash map. For each number, I check if its complement (target − num) is already in the map — that's O(1) lookup."*

```java
public int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> seen = new HashMap<>(); // value -> index
    for (int i = 0; i < nums.length; i++) {
        int complement = target - nums[i];
        if (seen.containsKey(complement)) {
            return new int[] { seen.get(complement), i };
        }
        seen.put(nums[i], i);
    }
    return new int[] {};
}
```
**Dry run** (`target = 9`):
```
num=2 → need 7 → not in {}        → store {2:0}
num=7 → need 2 → FOUND at index 0 → return [0, 1] ✅
```
- **Time:** O(n) — one pass through the array
- **Space:** O(n) — the hash map
- *"This is the best we can do — we have to look at each element at least once."*

### 🎯 Trade-off to mention
*"I traded space for time: O(n) memory to get O(n) speed instead of O(n²). For an interview, this is the expected answer."*

### 🧠 Algorithm to Remember Forever — **HashMap Lookup ("Complement Trick")**
> **Core idea:** Instead of searching for the partner, *remember everyone you've met.*
> For sum problems: store each number; for the next number, ask *"have I already seen `target − me`?"*
>
> **Memory hook:** 🤝 *"I don't go looking for my pair — I write down everyone I meet, so when my pair walks in, I already know them."*
>
> **Trigger phrase:** *"Find a pair / has-it-been-seen-before"* → **HashMap**. Lookup is O(1), so one pass = O(n).

---

# Problem 2: Best Time to Buy and Sell Stock

### 📋 Full Question
> You're given an array `prices` where `prices[i]` is the price of a stock on day `i`. Maximize your profit by choosing **one day to buy** and a **later day to sell**. Return the max profit (or 0 if no profit possible).
>
> **Example:** `prices = [7, 1, 5, 3, 6, 4]` → Output: `5` (buy at 1, sell at 6)

### Approach 1 — Brute Force
*"Try every buy/sell pair and track the max profit."*
```java
public int maxProfit(int[] prices) {
    int best = 0;
    for (int i = 0; i < prices.length; i++) {
        for (int j = i + 1; j < prices.length; j++) {
            best = Math.max(best, prices[j] - prices[i]);
        }
    }
    return best;
}
```
- **Time:** O(n²), **Space:** O(1)

### Approach 2 — Optimal (Track the minimum so far) ✅
*"I only need to remember the cheapest price I've seen so far. At each day, profit = today's price − cheapest so far."*
```java
public int maxProfit(int[] prices) {
    int minPrice = Integer.MAX_VALUE;
    int best = 0;
    for (int price : prices) {
        minPrice = Math.min(minPrice, price);   // cheapest buy day so far
        best = Math.max(best, price - minPrice); // best sell today
    }
    return best;
}
```
- **Time:** O(n) — single pass, **Space:** O(1)
- **Pattern:** "Track a running minimum/maximum" — appears constantly.

### 🧠 Algorithm to Remember Forever — **Running Min/Max (One-Pass Greedy)**
> **Core idea:** As you walk left → right, keep *the best thing seen so far* in one variable, and compare every new element against it.
>
> **Memory hook:** 📉 *"Buy at the lowest valley behind me, sell at today's peak."* You never look back — you just remember the lowest price so far.
>
> **Trigger phrase:** *"Best profit / max difference where the smaller comes first"* → **keep a running min, update answer as you go.** One pass = O(n).

---

# Problem 3: Maximum Subarray (Kadane's Algorithm) ⭐ Very Frequent

### 📋 Full Question
> Find the contiguous subarray with the **largest sum** and return that sum.
>
> **Example:** `nums = [-2, 1, -3, 4, -1, 2, 1, -5, 4]` → Output: `6` (subarray `[4, -1, 2, 1]`)

### Approach 1 — Brute Force
*"Check the sum of every possible subarray."*
```java
public int maxSubArray(int[] nums) {
    int best = Integer.MIN_VALUE;
    for (int i = 0; i < nums.length; i++) {
        int total = 0;
        for (int j = i; j < nums.length; j++) {
            total += nums[j];
            best = Math.max(best, total);
        }
    }
    return best;
}
```
- **Time:** O(n²), **Space:** O(1)

### Approach 2 — Optimal (Kadane's Algorithm) ✅
*"Key insight: at each element, either I extend the previous subarray or start fresh from here. I keep a running sum, and if it ever goes negative, I drop it."*
```java
public int maxSubArray(int[] nums) {
    int current = nums[0];
    int best = nums[0];
    for (int i = 1; i < nums.length; i++) {
        current = Math.max(nums[i], current + nums[i]); // extend or restart
        best = Math.max(best, current);                 // global best
    }
    return best;
}
```
**Why it works:** if `current + num` is worse than `num` alone, the previous part was dragging us down — so we restart.
- **Time:** O(n), **Space:** O(1)
- *"This is Kadane's algorithm — the classic answer. O(n) is optimal since we must read every element."*

### 🧠 Algorithm to Remember Forever — **Kadane's Algorithm (Dynamic Programming)**
> **Core idea:** At every element ask one question — *"Does my past help me, or is it dead weight?"* If the running sum went negative, throw it away and **start fresh from here.**
>
> **The one line that IS Kadane's:** `current = max(num, current + num)` → *"extend or restart."*
> Then `best = max(best, current)`.
>
> **Memory hook:** 🎒 *"If the backpack I'm carrying weighs me down (negative sum), I drop it and start with just this item."*
>
> **Trigger phrase:** *"Maximum/largest contiguous subarray sum"* → **Kadane's.** O(n).

---

# Problem 4: Product of Array Except Self ⭐ Microsoft favorite

### 📋 Full Question
> Return an array `answer` where `answer[i]` is the product of all elements **except** `nums[i]`. **You must NOT use division**, and it must run in **O(n)**.
>
> **Example:** `nums = [1, 2, 3, 4]` → Output: `[24, 12, 8, 6]`

### 🗣️ What to say first
*"The tricky constraint is no division — otherwise I'd just compute the total product and divide. So I'll use prefix and suffix products."*

### Approach 1 — Brute Force
```java
public int[] productExceptSelf(int[] nums) {
    int n = nums.length;
    int[] res = new int[n];
    for (int i = 0; i < n; i++) {
        int prod = 1;
        for (int j = 0; j < n; j++) {
            if (i != j) prod *= nums[j];
        }
        res[i] = prod;
    }
    return res;
}
```
- **Time:** O(n²), **Space:** O(1) extra

### Approach 2 — Optimal (Prefix × Suffix) ✅
*"For each index, the answer = (product of everything to the left) × (product of everything to the right). I compute left products in one pass, then multiply by right products in a second pass."*
```java
public int[] productExceptSelf(int[] nums) {
    int n = nums.length;
    int[] res = new int[n];

    // left products
    int left = 1;
    for (int i = 0; i < n; i++) {
        res[i] = left;
        left *= nums[i];
    }
    // right products
    int right = 1;
    for (int i = n - 1; i >= 0; i--) {
        res[i] *= right;
        right *= nums[i];
    }
    return res;
}
```
**Dry run** `[1,2,3,4]`:
```
After left pass:  [1, 1, 2, 6]
After right pass: [24, 12, 8, 6] ✅
```
- **Time:** O(n), **Space:** O(1) (output array doesn't count as extra)

### 🧠 Algorithm to Remember Forever — **Prefix / Suffix Products (Two-Pass Scan)**
> **Core idea:** Anything "except myself" = *(everything on my left) × (everything on my right).* Sweep once left→right to bank left products, once right→left to multiply in right products.
>
> **Memory hook:** 🔦 *"Shine a light from the left, then from the right — every spot gets lit by both sides except its own shadow."*
>
> **Trigger phrase:** *"Compute something for each index using all OTHER elements" / "no division allowed"* → **prefix + suffix arrays.** Two passes = O(n).

---

# Problem 5: Valid Anagram

### 📋 Full Question
> Given two strings `s` and `t`, return `true` if `t` is an anagram of `s` (same characters, same counts, different order).
>
> **Example:** `s = "anagram"`, `t = "nagaram"` → `true`

### Approach 1 — Sort & Compare
*"Two strings are anagrams if their sorted versions are identical."*
```java
public boolean isAnagram(String s, String t) {
    if (s.length() != t.length()) return false;
    char[] a = s.toCharArray();
    char[] b = t.toCharArray();
    Arrays.sort(a);
    Arrays.sort(b);
    return Arrays.equals(a, b);
}
```
- **Time:** O(n log n) — because of sorting, **Space:** O(n)

### Approach 2 — Optimal (Count characters) ✅
*"Better: count each character. If both strings have the same counts, they're anagrams. Counting is O(n) vs sorting's O(n log n)."*
```java
public boolean isAnagram(String s, String t) {
    if (s.length() != t.length()) return false;
    int[] count = new int[26]; // only 26 lowercase letters
    for (int i = 0; i < s.length(); i++) {
        count[s.charAt(i) - 'a']++;
        count[t.charAt(i) - 'a']--;
    }
    for (int c : count) {
        if (c != 0) return false;
    }
    return true;
}
```
- **Time:** O(n), **Space:** O(1) — at most 26 letters (fixed alphabet)
- 💡 *"Since there are only 26 lowercase letters, the space is bounded by a constant — effectively O(1)."*

### 🧠 Algorithm to Remember Forever — **Frequency Count (Counting / Hashing)**
> **Core idea:** Don't compare strings character-by-character — *tally* each letter. `+1` for string A, `−1` for string B. If every tally ends at 0, they match.
>
> **Memory hook:** 🗳️ *"Count the votes. Same letters in = same counts out. Anything left over means they're not equal."*
>
> **Trigger phrase:** *"Anagram / same characters / how many times does X appear"* → **count array (`int[26]`) or HashMap.** O(n) beats sorting's O(n log n).

---

# Problem 6: Longest Substring Without Repeating Characters ⭐ Very Frequent

### 📋 Full Question
> Find the length of the **longest substring** without repeating characters.
>
> **Example:** `s = "abcabcbb"` → Output: `3` (the substring `"abc"`)

### 🗣️ What to say first
*"This is a classic **sliding window** problem — I expand a window to the right and shrink from the left when I hit a duplicate."*

### Approach 1 — Brute Force
*"Check every substring and test if it has all unique characters."*
```java
public int lengthOfLongestSubstring(String s) {
    int best = 0;
    for (int i = 0; i < s.length(); i++) {
        Set<Character> seen = new HashSet<>();
        for (int j = i; j < s.length(); j++) {
            if (seen.contains(s.charAt(j))) break;
            seen.add(s.charAt(j));
            best = Math.max(best, j - i + 1);
        }
    }
    return best;
}
```
- **Time:** O(n²), **Space:** O(n)

### Approach 2 — Optimal (Sliding Window) ✅
*"I keep a window `[left, right]` with all-unique characters. When `s[right]` is a repeat, I move `left` forward past the last occurrence."*
```java
public int lengthOfLongestSubstring(String s) {
    Map<Character, Integer> lastSeen = new HashMap<>(); // char -> last index
    int left = 0;
    int best = 0;
    for (int right = 0; right < s.length(); right++) {
        char ch = s.charAt(right);
        if (lastSeen.containsKey(ch) && lastSeen.get(ch) >= left) {
            left = lastSeen.get(ch) + 1; // shrink window past the duplicate
        }
        lastSeen.put(ch, right);
        best = Math.max(best, right - left + 1);
    }
    return best;
}
```
**Dry run** `"abcabcbb"`:
```
a→win "a"(1)  b→"ab"(2)  c→"abc"(3)
a→repeat, left jumps → "bca"(3) ... max stays 3 ✅
```
- **Time:** O(n) — each char visited at most twice, **Space:** O(min(n, alphabet))
- **Pattern:** Sliding Window — *"expand right, shrink left."*

### 🧠 Algorithm to Remember Forever — **Sliding Window (Two Pointers)**
> **Core idea:** Keep a window `[left, right]`. **Grow** it from the right; the moment it breaks the rule (a duplicate), **shrink** it from the left until it's valid again. Track the biggest valid window.
>
> **Memory hook:** 🪟 *"A window slides along a train — open it wider until you smell smoke (duplicate), then slide the front up until the air is clean again."*
>
> **Trigger phrase:** *"Longest / shortest substring or subarray that satisfies a condition"* → **Sliding Window.** Each pointer moves forward only → O(n).

---

## 📊 Complexity Cheat Sheet — What "Good" Looks Like

| Problem | Brute Force | Optimal | Technique |
|---------|------------|---------|-----------|
| Two Sum | O(n²) | **O(n)** | HashMap |
| Buy/Sell Stock | O(n²) | **O(n)** | Running min |
| Maximum Subarray | O(n²) | **O(n)** | Kadane's |
| Product Except Self | O(n²) | **O(n)** | Prefix/Suffix |
| Valid Anagram | O(n log n) | **O(n)** | Char count |
| Longest Substring | O(n²) | **O(n)** | Sliding Window |

> **Notice the pattern:** almost every "brute force O(n²) → optimal O(n)" jump comes from
> **a HashMap, Two Pointers, or a Sliding Window.** When stuck, ask yourself:
> *"Can a hash map remember what I've seen? Can two pointers replace the inner loop?"*

---

## 🧠🧠 ALGORITHM RECOGNITION TABLE — Remember This Forever

> This is the *most important* table. If you can look at a question and instantly name the
> algorithm, half the battle is won. **When you hear the trigger words → fire the algorithm.**

| If the question says... (Trigger) | Use this Algorithm | Memory Hook | Big-O |
|-----------------------------------|--------------------|-------------|-------|
| "Find a **pair** / have I seen X before?" | **HashMap Lookup** | 🤝 Write down everyone you meet | O(n) |
| "Max profit / max difference (smaller first)" | **Running Min/Max** | 📉 Buy at the lowest valley behind you | O(n) |
| "Largest **contiguous** subarray sum" | **Kadane's Algorithm** | 🎒 Drop the backpack if it weighs you down | O(n) |
| "Value at each index from **all others**, no division" | **Prefix / Suffix Products** | 🔦 Light from left, then from right | O(n) |
| "**Anagram** / same chars / how many times" | **Frequency Count** | 🗳️ Count the votes | O(n) |
| "**Longest/shortest** substring with a condition" | **Sliding Window** | 🪟 Open till smoke, slide front till clean | O(n) |
| "Sorted array, find a pair/triplet" | **Two Pointers** | 👉👈 One at each end, walk inward | O(n) |

### The 3 Questions to Ask Yourself on ANY Array/String Problem
1. **"Can a HashMap remember what I've already seen?"** → kills most O(n²) search loops.
2. **"Can two pointers replace my inner loop?"** → sorted arrays, palindromes, pairs.
3. **"Am I looking for a longest/shortest window?"** → Sliding Window.

> 🔑 **The forever-truth:** O(n²) almost always means a *nested loop*. The way out is almost
> always **HashMap, Two Pointers, or Sliding Window** — they let you do in ONE pass what the
> inner loop was re-doing every time. Memorize these three escape hatches.

---

## ⏱️ Big-O Quick Reference (say these confidently)

| Notation | Name | Example |
|----------|------|---------|
| O(1) | Constant | HashMap lookup, array index |
| O(log n) | Logarithmic | Binary search |
| O(n) | Linear | Single loop through array |
| O(n log n) | Linearithmic | Sorting (`Arrays.sort`) |
| O(n²) | Quadratic | Nested loops (brute force) |
| O(2ⁿ) | Exponential | Recursion without memoization |

---

## ☕ Java Tips for the Whiteboard

- **HashMap:** `map.containsKey(k)`, `map.get(k)`, `map.put(k, v)`, `map.getOrDefault(k, 0)`
- **HashSet:** `set.contains(x)`, `set.add(x)` — great for "have I seen this?"
- **Char to index:** `s.charAt(i) - 'a'` gives 0–25 for lowercase letters
- **Array fill / compare:** `Arrays.sort(arr)`, `Arrays.equals(a, b)`, `Arrays.fill(arr, 0)`
- **Min/Max:** `Math.min(a, b)`, `Math.max(a, b)`
- **Edge guards:** `if (nums == null || nums.length == 0) return ...;`
- **Return an int array:** `return new int[] { i, j };`
- **StringBuilder** for building strings (avoid `+` in loops — it's O(n²)):
  `StringBuilder sb = new StringBuilder(); sb.append(c); sb.toString();`

---

## ✅ Final Interview Checklist (do this every single time)

1. ☐ **Repeat the question** back in your own words
2. ☐ **Ask clarifying questions** (empty? null? duplicates? negatives? sorted?)
3. ☐ **State brute force first** + its complexity — *never skip this*
4. ☐ **Optimize** — explain the key insight ("a HashMap lets me look up in O(1)")
5. ☐ **Code the optimal solution** cleanly
6. ☐ **Dry-run a small example** out loud
7. ☐ **Check edge cases** (empty, null, one element, all same, negatives)
8. ☐ **State final Time & Space complexity**
9. ☐ Ask: *"Would you like me to optimize space further or handle any other cases?"*

> 💬 **Remember:** Microsoft cares *how* you think more than getting the perfect answer.
> Talking through Brute → Optimal → Best out loud is exactly what they're scoring you on.
