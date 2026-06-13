# Week 2: HashMap & Two Pointers — How to ANSWER in a Microsoft Interview 🎤 (Java)

Welcome to your interview-answer playbook for this week! 🎉 This file doesn't just give you solutions — it teaches you **how to talk through them out loud** the way a Microsoft interviewer wants to hear. Read it like a script, practice it like a play. 🎭

---

## 🏆 The Golden Rule of Every Coding Interview

Never jump straight to code. **Always** walk this ladder, one rung at a time:

> **Clarify → Brute Force → Optimal → Best → Complexity**

Interviewers are not grading "did you get the answer." They're grading **how you think**. Showing the brute force first proves you understand the problem; jumping to the optimal makes them wonder if you memorized it. So always say "the naive way is X, but we can do better." 🪜

### 🗣️ The Interview Script (what to literally say, in order)

1. **"Let me make sure I understand the problem."** — Restate it in your own words.
2. **"Can I ask a few clarifying questions?"** — Ask about inputs, edge cases, constraints (see list below).
3. **"Let me start with a brute-force idea so we have something working."** — Describe it verbally first.
4. **"The brute force is O(?) — I think we can do better using [HashMap / two pointers]."** — Name the pattern.
5. **"Here's my plan in plain English before I code."** — 2-3 sentences of the optimal approach.
6. **"Let me code it now."** — Write clean Java, talking as you go.
7. **"Let me dry-run it on the example."** — Trace through with the given input.
8. **"Final complexity is Time O(?) and Space O(?)."** — State both, and any tradeoffs.
9. **"Edge cases I'd test: empty input, one element, duplicates, negatives."** — Show you think about failure.

### ❓ Common Clarifying Questions (memorize these — they work on almost any array problem)

- **Sorted?** "Is the array sorted, or can I assume it is?" (Decides HashMap vs two pointers!)
- **Range of values?** "Can values be negative? Zero? How large?"
- **Duplicates?** "Can there be duplicate elements?"
- **Empty / null?** "What should I return for empty or null input?"
- **Multiple answers?** "If multiple valid answers exist, return any one or all?"
- **In-place?** "Can I modify the input array, or should it stay unchanged?"
- **Output format?** "Indices or values? 0-indexed or 1-indexed?"
- **Guaranteed solution?** "Is a valid answer guaranteed to exist?"

---

## Problem 1: Two Sum 🎯 (Microsoft asked — REVISE!)

### 📋 Full Question
Given an array of integers `nums` and an integer `target`, return the **indices** of the two numbers that add up to `target`. Exactly one solution exists, and you may not use the same element twice.

- **Input:** `nums = [2, 7, 11, 15]`, `target = 9`
- **Expected Output:** `[0, 1]`  (because `nums[0] + nums[1] = 2 + 7 = 9`)

### 🗣️ What to say first
- "Is the array sorted?" (No → HashMap shines.)
- "Is there exactly one solution guaranteed, and can I reuse an index?" (One solution; no reuse.)

### Approach 1 — Brute Force
Check every pair.

```java
public int[] twoSumBrute(int[] nums, int target) {
    for (int i = 0; i < nums.length; i++) {
        for (int j = i + 1; j < nums.length; j++) {
            if (nums[i] + nums[j] == target) {
                return new int[] { i, j };
            }
        }
    }
    return new int[] {}; // no pair found
}
```
- **Time:** O(n²) — every pair. **Space:** O(1).
- ❌ Why not enough: nested loops re-scan the whole array for each element — too slow for large inputs.

### Approach 2 — Optimal (HashMap "complement" trick)
As you walk the array, for each number ask: *"Have I already seen the number that completes me?"* Store each value's index in a map so the lookup is O(1).

```java
public int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> seen = new HashMap<>(); // value -> index
    for (int i = 0; i < nums.length; i++) {
        int need = target - nums[i];          // the complement I'm looking for
        if (seen.containsKey(need)) {
            return new int[] { seen.get(need), i };
        }
        seen.put(nums[i], i);                  // remember me for future numbers
    }
    return new int[] {};
}
```

**🔍 Dry run** on `[2, 7, 11, 15]`, target `9`:
| i | nums[i] | need = 9 - nums[i] | seen before? | action |
|---|---------|--------------------|--------------|--------|
| 0 | 2 | 7 | no | put {2:0} |
| 1 | 7 | 2 | **yes!** (index 0) | return `[0, 1]` ✅ |

- **Time:** O(n) — one pass. **Space:** O(n) — the map.

### 🧠 Algorithm to Remember Forever
- **Pattern:** Complement HashMap (a.k.a. "Seen Map").
- **Core idea:** Store what you've seen; for each new item, look up the *complement* it needs.
- **Memory hook:** 🧦 *The Sock Drawer* — for each sock you hold, you instantly check the drawer for its matching pair instead of trying every sock one by one.
- **Trigger phrase:** *"find two numbers that add up to..."* on an **unsorted** array.

---

## Problem 2: Roman to Integer 🏛️ (Microsoft asked)

### 📋 Full Question
Convert a Roman numeral string to an integer. Symbols: I=1, V=5, X=10, L=50, C=100, D=500, M=1000. Usually you add, but when a smaller value sits **before** a larger one, you subtract (e.g., IV = 4, IX = 9).

- **Input:** `s = "MCMXCIV"`
- **Expected Output:** `1994`  (M=1000, CM=900, XC=90, IV=4)

### 🗣️ What to say first
- "Is the input always a valid uppercase Roman numeral?" (Assume yes.)
- "Max value range?" (Up to 3999 by convention.)

### Approach 1 — Brute Force
Replace the six subtractive pairs first, then add everything up. Works but feels hacky and string-mutation-heavy.

```java
public int romanToIntBrute(String s) {
    s = s.replace("IV", "IIII").replace("IX", "VIIII");
    s = s.replace("XL", "XXXX").replace("XC", "LXXXX");
    s = s.replace("CD", "CCCC").replace("CM", "DCCCC");
    Map<Character, Integer> val = Map.of('I',1,'V',5,'X',10,'L',50,'C',100,'D',500,'M',1000);
    int total = 0;
    for (char c : s.toCharArray()) total += val.get(c);
    return total;
}
```
- **Time:** O(n). **Space:** O(n) (new strings).
- ❌ Why not enough: correct, but the string replacements are fragile and harder to reason about than the clean single-pass version.

### Approach 2 — Optimal (compare with the neighbor to the right)
Scan left to right. If the current symbol is **smaller** than the one to its right, subtract it; otherwise add it.

```java
public int romanToInt(String s) {
    Map<Character, Integer> val = Map.of('I',1,'V',5,'X',10,'L',50,'C',100,'D',500,'M',1000);
    int total = 0;
    for (int i = 0; i < s.length(); i++) {
        int cur = val.get(s.charAt(i));
        // If a bigger symbol follows, this one is subtractive (like IV, IX)
        if (i + 1 < s.length() && cur < val.get(s.charAt(i + 1))) {
            total -= cur;
        } else {
            total += cur;
        }
    }
    return total;
}
```

**🔍 Dry run** on `"MCMXCIV"`:
| char | value | next bigger? | running total |
|------|-------|--------------|---------------|
| M | 1000 | no | 1000 |
| C | 100 | yes (M) | 900 |
| M | 1000 | no | 1900 |
| X | 10 | yes (C) | 1890 |
| C | 100 | no | 1990 |
| I | 1 | yes (V) | 1989 |
| V | 5 | no | **1994** ✅ |

- **Time:** O(n). **Space:** O(1) (map is fixed size).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Lookahead comparison (peek at neighbor).
- **Core idea:** A symbol is subtractive only if a larger symbol follows it.
- **Memory hook:** 🪜 *Stairs that occasionally dip* — you normally climb (add), but when the next step is higher than you, you step back first (subtract).
- **Trigger phrase:** *"convert this notation where order changes meaning."*

---

## Problem 3: Majority Element 👑 (Microsoft asked)

### 📋 Full Question
Given an array of size `n`, return the element that appears **more than ⌊n/2⌋ times**. Assume the majority element always exists.

- **Input:** `nums = [2, 2, 1, 1, 1, 2, 2]`
- **Expected Output:** `2`  (appears 4 times, n=7, more than 3)

### 🗣️ What to say first
- "Is a majority element guaranteed to exist?" (Yes — this unlocks Boyer-Moore.)
- "Can I modify the array?" (Matters if I want to sort.)

### Approach 1 — Brute Force (HashMap count)
Count every element, return the one whose count exceeds n/2.

```java
public int majorityBrute(int[] nums) {
    Map<Integer, Integer> count = new HashMap<>();
    for (int num : nums) {
        count.merge(num, 1, Integer::sum); // count[num] += 1
        if (count.get(num) > nums.length / 2) return num;
    }
    return -1;
}
```
- **Time:** O(n). **Space:** O(n).
- ❌ Why not enough: correct and fast in time, but uses O(n) extra space — the interviewer will ask "can you do it in O(1) space?"

### Approach 2 — Optimal (Boyer-Moore Voting)
Keep a `candidate` and a `count`. Same element → +1 vote; different → −1 vote. When count hits 0, adopt the current element as the new candidate. The majority survives because it out-votes everyone else.

```java
public int majorityElement(int[] nums) {
    int candidate = nums[0];
    int count = 0;
    for (int num : nums) {
        if (count == 0) candidate = num;     // no one in the lead -> new candidate
        count += (num == candidate) ? 1 : -1; // vote for or against
    }
    return candidate;
}
```

**🔍 Dry run** on `[2, 2, 1, 1, 1, 2, 2]`:
| num | count before | candidate | count after |
|-----|--------------|-----------|-------------|
| 2 | 0 | 2 | 1 |
| 2 | 1 | 2 | 2 |
| 1 | 2 | 2 | 1 |
| 1 | 1 | 2 | 0 |
| 1 | 0 | 1 | 1 |
| 2 | 1 | 1 | 0 |
| 2 | 0 | 2 | 1 → answer **2** ✅ |

- **Time:** O(n). **Space:** O(1).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Boyer-Moore Majority Voting.
- **Core idea:** Pair off opposites; the majority is whoever's left standing.
- **Memory hook:** 🗳️ *Election cancellation* — each opposing vote cancels one of yours, but the true majority always has votes left over.
- **Trigger phrase:** *"appears more than half the time"* + *"O(1) space."*

---

## Problem 4: Top K Frequent Elements 🔝 (Microsoft asked)

### 📋 Full Question
Given an integer array `nums` and an integer `k`, return the `k` most frequent elements (in any order).

- **Input:** `nums = [1, 1, 1, 2, 2, 3]`, `k = 2`
- **Expected Output:** `[1, 2]`  (1 appears 3×, 2 appears 2×)

### 🗣️ What to say first
- "Is `k` always valid (≤ number of distinct elements)?"
- "Does output order matter, and is there a tie-break rule?" (Usually order-free, no tie-break.)

### Approach 1 — Brute Force (count + full sort)
Count frequencies, then sort all distinct elements by frequency descending and take the top k.

```java
public int[] topKFrequentBrute(int[] nums, int k) {
    Map<Integer, Integer> freq = new HashMap<>();
    for (int n : nums) freq.merge(n, 1, Integer::sum);

    List<Integer> distinct = new ArrayList<>(freq.keySet());
    distinct.sort((a, b) -> freq.get(b) - freq.get(a)); // high freq first

    int[] result = new int[k];
    for (int i = 0; i < k; i++) result[i] = distinct.get(i);
    return result;
}
```
- **Time:** O(n + m log m) where m = distinct elements. **Space:** O(m).
- ❌ Why not enough: sorting *all* distinct elements is wasteful when we only need the top k — a heap or bucket does better.

### Approach 2 — Optimal (Bucket Sort by frequency)
Frequency can be at most `n`. Make buckets indexed by frequency; drop each element into its frequency bucket; read buckets from the high-frequency end until you collect k. This is **O(n)**.

```java
public int[] topKFrequent(int[] nums, int k) {
    Map<Integer, Integer> freq = new HashMap<>();
    for (int n : nums) freq.merge(n, 1, Integer::sum);

    // buckets[f] = list of numbers that appear exactly f times
    List<Integer>[] buckets = new List[nums.length + 1];
    for (int num : freq.keySet()) {
        int f = freq.get(num);
        if (buckets[f] == null) buckets[f] = new ArrayList<>();
        buckets[f].add(num);
    }

    int[] result = new int[k];
    int idx = 0;
    for (int f = buckets.length - 1; f >= 1 && idx < k; f--) {
        if (buckets[f] == null) continue;
        for (int num : buckets[f]) {
            result[idx++] = num;
            if (idx == k) break;
        }
    }
    return result;
}
```

**🔍 Dry run** on `[1,1,1,2,2,3]`, `k=2`:
- freq = {1:3, 2:2, 3:1}
- buckets: index 3 → [1], index 2 → [2], index 1 → [3]
- Scan from high freq: f=3 grabs 1, f=2 grabs 2 → result `[1, 2]` ✅

- **Time:** O(n). **Space:** O(n).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Frequency Map + Bucket Sort (or a min-heap of size k).
- **Core idea:** Frequencies are bounded by n, so index *by frequency* and read from the top.
- **Memory hook:** 🪣 *Mailbox slots by popularity* — slot #3 holds everyone seen 3 times. Walk down from the most popular slot until you've grabbed k.
- **Trigger phrase:** *"top k most frequent / most common."* (Heap if asked for O(n log k); bucket for O(n).)

---

## Problem 5: Subarray Sum Equals K 🧮 (Microsoft — VERY FREQUENT!)

### 📋 Full Question
Given an integer array `nums` and an integer `k`, return the **number of contiguous subarrays** whose sum equals `k`. Values can be negative.

- **Input:** `nums = [1, 1, 1]`, `k = 2`
- **Expected Output:** `2`  (subarrays `[1,1]` at indices 0-1 and 1-2)

### 🗣️ What to say first
- "Can numbers be negative?" (**Critical** — yes, so sliding window won't work; we need prefix sums.)
- "Do we count overlapping subarrays separately, and return the count or the subarrays?" (Overlaps count; return the count.)

### Approach 1 — Brute Force
Try every start/end pair and sum it.

```java
public int subarraySumBrute(int[] nums, int k) {
    int count = 0;
    for (int start = 0; start < nums.length; start++) {
        int sum = 0;
        for (int end = start; end < nums.length; end++) {
            sum += nums[end];
            if (sum == k) count++;
        }
    }
    return count;
}
```
- **Time:** O(n²). **Space:** O(1).
- ❌ Why not enough: O(n²) is too slow for large arrays — we recompute overlapping sums repeatedly.

### Approach 2 — Optimal (Prefix Sum + HashMap)
Track the running `prefixSum`. If at some point `prefixSum - k` was seen before, then the chunk between then and now sums to exactly `k`. Store how many times each prefix sum occurred. Seed the map with `{0: 1}` so subarrays starting at index 0 count.

```java
public int subarraySum(int[] nums, int k) {
    Map<Integer, Integer> prefixCounts = new HashMap<>();
    prefixCounts.put(0, 1);   // empty prefix has sum 0, seen once
    int prefixSum = 0, count = 0;
    for (int num : nums) {
        prefixSum += num;
        // If (prefixSum - k) existed, those many subarrays end here with sum k
        count += prefixCounts.getOrDefault(prefixSum - k, 0);
        prefixCounts.merge(prefixSum, 1, Integer::sum);
    }
    return count;
}
```

**🔍 Dry run** on `[1, 1, 1]`, `k = 2`:
| num | prefixSum | need (sum-k) | found count | total | map after |
|-----|-----------|--------------|-------------|-------|-----------|
| start | 0 | — | — | 0 | {0:1} |
| 1 | 1 | -1 | 0 | 0 | {0:1, 1:1} |
| 1 | 2 | 0 | 1 | 1 | {0:1, 1:1, 2:1} |
| 1 | 3 | 1 | 1 | **2** ✅ | {0:1,1:1,2:1,3:1} |

- **Time:** O(n). **Space:** O(n).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Prefix Sum + HashMap (count of running sums).
- **Core idea:** A subarray sum = (prefix at end) − (prefix at start); store seen prefixes to find matches in O(1).
- **Memory hook:** 🛣️ *Mile markers on a highway* — distance between two markers = (later mile) − (earlier mile). You remember every marker you passed so you can instantly check distances.
- **Trigger phrase:** *"number of subarrays that sum to k"* (especially with **negatives** → not sliding window).

---

## Problem 6: Longest Consecutive Sequence 🔗 (Microsoft asked)

### 📋 Full Question
Given an unsorted array `nums`, return the length of the longest run of **consecutive integers** (order in the array doesn't matter). Must run in O(n).

- **Input:** `nums = [100, 4, 200, 1, 3, 2]`
- **Expected Output:** `4`  (the sequence `1, 2, 3, 4`)

### 🗣️ What to say first
- "Can I sort?" (Sorting gives O(n log n); they usually want O(n).)
- "Are there duplicates / can values be negative?" (Both fine — a HashSet handles them.)

### Approach 1 — Brute Force (sort then scan)
Sort, then count consecutive runs.

```java
public int longestConsecutiveBrute(int[] nums) {
    if (nums.length == 0) return 0;
    Arrays.sort(nums);
    int longest = 1, current = 1;
    for (int i = 1; i < nums.length; i++) {
        if (nums[i] == nums[i - 1]) continue;        // skip duplicates
        if (nums[i] == nums[i - 1] + 1) current++;   // extends the run
        else current = 1;                            // run broke
        longest = Math.max(longest, current);
    }
    return longest;
}
```
- **Time:** O(n log n) from sorting. **Space:** O(1) (or O(n) depending on sort).
- ❌ Why not enough: the problem explicitly demands O(n) — sorting violates that.

### Approach 2 — Optimal (HashSet, start only at run beginnings)
Put everything in a HashSet. A number is the **start** of a sequence only if `num - 1` is NOT in the set. From each start, walk upward counting. Each number is visited at most twice → O(n).

```java
public int longestConsecutive(int[] nums) {
    Set<Integer> set = new HashSet<>();
    for (int n : nums) set.add(n);

    int longest = 0;
    for (int num : set) {
        // Only start counting from the beginning of a streak
        if (!set.contains(num - 1)) {
            int length = 1;
            int next = num + 1;
            while (set.contains(next)) {  // walk the streak upward
                length++;
                next++;
            }
            longest = Math.max(longest, length);
        }
    }
    return longest;
}
```

**🔍 Dry run** on `[100, 4, 200, 1, 3, 2]`:
- set = {100, 4, 200, 1, 3, 2}
- 1: is 0 in set? no → start! walk 1→2→3→4, length 4.
- 4: is 3 in set? yes → skip (not a start).
- 100: is 99 in set? no → start! length 1.
- 200: is 199 in set? no → start! length 1.
- longest = **4** ✅

- **Time:** O(n). **Space:** O(n).

### 🧠 Algorithm to Remember Forever
- **Pattern:** HashSet + "only expand from the start of a streak."
- **Core idea:** Avoid re-counting by starting a walk only when there's no smaller neighbor.
- **Memory hook:** 🚂 *Find the engine first* — only count train cars starting from the engine (no car in front of it), never from the middle.
- **Trigger phrase:** *"longest consecutive run"* + *"unsorted"* + *"O(n)."*

---

## Problem 7: First Unique Character in a String 🔤 (Microsoft asked)

### 📋 Full Question
Given a string `s`, return the **index** of the first non-repeating character. If none exists, return `-1`.

- **Input:** `s = "leetcode"`
- **Expected Output:** `0`  (`'l'` is the first character that never repeats)
- Another: `s = "loveleetcode"` → `2` (`'v'`)

### 🗣️ What to say first
- "Only lowercase English letters?" (Often yes — lets me use a fixed size-26 array.)
- "Return the index or the character?" (Index.)

### Approach 1 — Brute Force (for each char, scan the rest)
For each character, scan the whole string to check if it appears again.

```java
public int firstUniqCharBrute(String s) {
    for (int i = 0; i < s.length(); i++) {
        boolean unique = true;
        for (int j = 0; j < s.length(); j++) {
            if (j != i && s.charAt(j) == s.charAt(i)) { unique = false; break; }
        }
        if (unique) return i;
    }
    return -1;
}
```
- **Time:** O(n²). **Space:** O(1).
- ❌ Why not enough: re-scanning the string for every character is quadratic; a single frequency pass is far faster.

### Approach 2 — Optimal (Frequency Map, two passes)
Pass 1: count every character's frequency. Pass 2: walk left to right and return the first index whose count is exactly 1.

```java
public int firstUniqChar(String s) {
    int[] freq = new int[26];                 // counts for 'a'..'z'
    for (char c : s.toCharArray()) freq[c - 'a']++;

    for (int i = 0; i < s.length(); i++) {
        if (freq[s.charAt(i) - 'a'] == 1) return i; // first one seen exactly once
    }
    return -1;
}
```
> If the alphabet isn't restricted, swap the `int[26]` for a `Map<Character, Integer>` built with `freq.merge(c, 1, Integer::sum)`.

**🔍 Dry run** on `"leetcode"`:
- freq → l:1, e:3, t:1, c:1, o:1, d:1
- Pass 2: index 0 is 'l', freq 1 → return **0** ✅

- **Time:** O(n) (two passes). **Space:** O(1) (fixed 26-slot array).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Frequency Map + ordered second pass.
- **Core idea:** Count first, then re-walk in order to honor "first."
- **Memory hook:** 🎟️ *Roll call after tallying* — tally every name, then call the register in order; the first name called only once wins.
- **Trigger phrase:** *"first non-repeating / first unique character."*

---

## Problem 8: Two Sum II — Input Array Is Sorted ↔️ (Microsoft asked)

### 📋 Full Question
Given a **1-indexed sorted** array `numbers` and a `target`, return the two 1-based indices whose values sum to `target`. Use O(1) extra space. Exactly one solution.

- **Input:** `numbers = [2, 7, 11, 15]`, `target = 9`
- **Expected Output:** `[1, 2]`  (1-indexed: numbers[1]=2, numbers[2]=7)

### 🗣️ What to say first
- "The array is sorted ascending — confirm?" (Yes → two pointers!)
- "1-indexed output and O(1) space required?" (Yes — rules out the HashMap version.)

### Approach 1 — Brute Force
Same nested loop as classic Two Sum.

```java
public int[] twoSumSortedBrute(int[] numbers, int target) {
    for (int i = 0; i < numbers.length; i++)
        for (int j = i + 1; j < numbers.length; j++)
            if (numbers[i] + numbers[j] == target)
                return new int[] { i + 1, j + 1 }; // 1-indexed
    return new int[] {};
}
```
- **Time:** O(n²). **Space:** O(1).
- ❌ Why not enough: ignores the gift of sortedness — we can exploit order for O(n).

### Approach 2 — Optimal (Two Pointers, opposite ends)
Start `left` at the smallest, `right` at the largest. If the sum is too big, shrink from the right; too small, grow from the left. Sorted order guarantees this converges.

```java
public int[] twoSumSorted(int[] numbers, int target) {
    int left = 0, right = numbers.length - 1;
    while (left < right) {
        int sum = numbers[left] + numbers[right];
        if (sum == target) return new int[] { left + 1, right + 1 }; // 1-indexed
        if (sum < target) left++;   // need a bigger sum
        else right--;               // need a smaller sum
    }
    return new int[] {};
}
```

**🔍 Dry run** on `[2, 7, 11, 15]`, target `9`:
| left | right | sum | vs target | move |
|------|-------|-----|-----------|------|
| 0 (2) | 3 (15) | 17 | too big | right-- |
| 0 (2) | 2 (11) | 13 | too big | right-- |
| 0 (2) | 1 (7) | 9 | match! | return `[1, 2]` ✅ |

- **Time:** O(n). **Space:** O(1).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Two Pointers (opposite direction) on a sorted array.
- **Core idea:** Squeeze inward — too big? drop the high end; too small? raise the low end.
- **Memory hook:** 🎚️ *Tuning a dial from both sides* — pinch the range until the sum lands exactly on target.
- **Trigger phrase:** *"sorted array"* + *"find a pair / two sum"* + *"O(1) space."*

---

## Problem 9: 3Sum 🎲 (Microsoft asked — REVISE!)

### 📋 Full Question
Return **all unique triplets** `[a, b, c]` from `nums` such that `a + b + c = 0`. No duplicate triplets in the output.

- **Input:** `nums = [-1, 0, 1, 2, -1, -4]`
- **Expected Output:** `[[-1, -1, 2], [-1, 0, 1]]`

### 🗣️ What to say first
- "Can I sort the array?" (Yes — sorting enables two pointers and easy dedup.)
- "Must triplets be unique, values or indices?" (Unique triplets; values.)

### Approach 1 — Brute Force
Three nested loops, dedup with a set.

```java
public List<List<Integer>> threeSumBrute(int[] nums) {
    Set<List<Integer>> result = new HashSet<>();
    int n = nums.length;
    Arrays.sort(nums); // sort each triplet's order for dedup
    for (int i = 0; i < n; i++)
        for (int j = i + 1; j < n; j++)
            for (int k = j + 1; k < n; k++)
                if (nums[i] + nums[j] + nums[k] == 0)
                    result.add(Arrays.asList(nums[i], nums[j], nums[k]));
    return new ArrayList<>(result);
}
```
- **Time:** O(n³). **Space:** O(n) for the dedup set.
- ❌ Why not enough: O(n³) is far too slow; we can fix one number and two-pointer the rest.

### Approach 2 — Optimal (Sort + fix one + Two Pointers)
Sort. For each index `i`, treat `-nums[i]` as the target for a two-pointer search in the remaining sorted subarray. Skip duplicates to keep triplets unique.

```java
public List<List<Integer>> threeSum(int[] nums) {
    Arrays.sort(nums);
    List<List<Integer>> result = new ArrayList<>();
    for (int i = 0; i < nums.length - 2; i++) {
        if (i > 0 && nums[i] == nums[i - 1]) continue; // skip duplicate anchors
        if (nums[i] > 0) break;                        // sorted: no way to reach 0
        int left = i + 1, right = nums.length - 1;
        while (left < right) {
            int sum = nums[i] + nums[left] + nums[right];
            if (sum == 0) {
                result.add(Arrays.asList(nums[i], nums[left], nums[right]));
                left++; right--;
                while (left < right && nums[left] == nums[left - 1]) left++;   // skip dup
                while (left < right && nums[right] == nums[right + 1]) right--; // skip dup
            } else if (sum < 0) {
                left++;   // need bigger
            } else {
                right--;  // need smaller
            }
        }
    }
    return result;
}
```

**🔍 Dry run** on `[-1, 0, 1, 2, -1, -4]` → sorted `[-4, -1, -1, 0, 1, 2]`:
- i=0 (-4): left=-1, right=2 → sums never reach 0 (too negative), no triplet.
- i=1 (-1): target 1. left=-1, right=2 → sum 0 ✅ → `[-1, -1, 2]`. Then left=0,right=1 → sum 0 ✅ → `[-1, 0, 1]`.
- i=2 (-1): duplicate anchor → skip.
- i=3 (0): left=1, right=2 → sum 3 > 0, shrink; no triplet.
- Result `[[-1,-1,2], [-1,0,1]]` ✅

- **Time:** O(n²) — outer loop × inner two-pointer. **Space:** O(1) extra (ignoring output).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Sort + Fix-One + Two Pointers (reduce kSum by one dimension).
- **Core idea:** Lock one element, then solve a 2Sum on the rest with two pointers.
- **Memory hook:** 🪢 *Pin one knot, tighten the rope* — pin a number, then squeeze the remaining range from both ends.
- **Trigger phrase:** *"triplets / find three numbers that sum to..."* (and its cousin 4Sum = two pins).

---

## Problem 10: Valid Palindrome 🪞 (Microsoft asked)

### 📋 Full Question
Given a string `s`, return `true` if it is a palindrome considering **only alphanumeric characters** and ignoring case.

- **Input:** `s = "A man, a plan, a canal: Panama"`
- **Expected Output:** `true`  (cleaned: `"amanaplanacanalpanama"`)
- Another: `s = "race a car"` → `false`

### 🗣️ What to say first
- "Do I ignore spaces, punctuation, and case?" (Yes.)
- "Is an empty string a palindrome?" (Yes — returns true.)

### Approach 1 — Brute Force (clean then reverse-compare)
Build a cleaned, lowercased string, reverse it, and compare. Simple but uses extra space.

```java
public boolean isPalindromeBrute(String s) {
    StringBuilder sb = new StringBuilder();
    for (char c : s.toCharArray()) {
        if (Character.isLetterOrDigit(c)) sb.append(Character.toLowerCase(c));
    }
    String cleaned = sb.toString();
    String reversed = sb.reverse().toString();
    return cleaned.equals(reversed);
}
```
- **Time:** O(n). **Space:** O(n) (two extra strings).
- ❌ Why not enough: correct, but builds extra strings; two pointers does it in O(1) space.

### Approach 2 — Optimal (Two Pointers, opposite ends)
Walk `left` inward and `right` inward. Skip non-alphanumeric characters. Compare lowercased characters; any mismatch means not a palindrome.

```java
public boolean isPalindrome(String s) {
    int left = 0, right = s.length() - 1;
    while (left < right) {
        while (left < right && !Character.isLetterOrDigit(s.charAt(left))) left++;
        while (left < right && !Character.isLetterOrDigit(s.charAt(right))) right--;
        if (Character.toLowerCase(s.charAt(left)) != Character.toLowerCase(s.charAt(right))) {
            return false; // mismatch -> not a palindrome
        }
        left++;
        right--;
    }
    return true;
}
```

**🔍 Dry run** on `"A man, a plan, a canal: Panama"`:
- left lands on 'A', right lands on 'a' → equal (lowercased). Move inward.
- Spaces, commas, colon are skipped each step.
- Pointers keep matching ('m'/'m', 'a'/'a', ...) until they cross → **true** ✅

- **Time:** O(n). **Space:** O(1).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Two Pointers (opposite direction) with skip filter.
- **Core idea:** Compare mirror positions, skipping characters that don't count.
- **Memory hook:** 🪞 *Mirror walk* — two people walk toward a mirror's center; if every matched step looks identical, it's symmetric.
- **Trigger phrase:** *"is it a palindrome / reads the same forwards and backwards."*

---

## Problem 11: Remove Duplicates from Sorted Array 🐢🐇 (Microsoft asked)

### 📋 Full Question
Given a **sorted** array `nums`, remove duplicates **in place** so each element appears once. Return the new length `k`; the first `k` slots must hold the unique values in order.

- **Input:** `nums = [0, 0, 1, 1, 1, 2, 2, 3, 3, 4]`
- **Expected Output:** `5`, with `nums` starting `[0, 1, 2, 3, 4, ...]`

### 🗣️ What to say first
- "Is the array sorted?" (Yes — duplicates are adjacent, which makes this easy.)
- "Modify in place and return the count, not a new array?" (Yes.)

### Approach 1 — Brute Force (build a new array / use a set)
Collect uniques into a separate structure, then copy back. Uses extra space.

```java
public int removeDuplicatesBrute(int[] nums) {
    LinkedHashSet<Integer> seen = new LinkedHashSet<>(); // preserves order
    for (int n : nums) seen.add(n);
    int i = 0;
    for (int n : seen) nums[i++] = n;
    return seen.size();
}
```
- **Time:** O(n). **Space:** O(n) (the set).
- ❌ Why not enough: uses O(n) extra space and ignores that the array is already sorted — two pointers needs no extra space.

### Approach 2 — Optimal (Two Pointers, slow/fast)
`slow` marks the last unique position written. `fast` scans ahead. When `fast` finds a value different from `nums[slow]`, advance `slow` and copy it in.

```java
public int removeDuplicates(int[] nums) {
    if (nums.length == 0) return 0;
    int slow = 0; // last unique value sits at index slow
    for (int fast = 1; fast < nums.length; fast++) {
        if (nums[fast] != nums[slow]) {
            slow++;
            nums[slow] = nums[fast]; // place the next unique value
        }
    }
    return slow + 1; // count of uniques
}
```

**🔍 Dry run** on `[0, 0, 1, 1, 1, 2, 2, 3, 3, 4]`:
| fast | nums[fast] | nums[slow] | action | slow | array prefix |
|------|-----------|-----------|--------|------|--------------|
| 1 | 0 | 0 | equal, skip | 0 | [0,...] |
| 2 | 1 | 0 | diff → slow=1, nums[1]=1 | 1 | [0,1,...] |
| 4 | 1 | 1 | equal, skip | 1 | [0,1,...] |
| 5 | 2 | 1 | diff → slow=2, nums[2]=2 | 2 | [0,1,2,...] |
| 7 | 3 | 2 | diff → slow=3, nums[3]=3 | 3 | [0,1,2,3,...] |
| 9 | 4 | 3 | diff → slow=4, nums[4]=4 | 4 | [0,1,2,3,4] |

Return **5** ✅

- **Time:** O(n). **Space:** O(1).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Two Pointers (same direction, slow/fast write pointer).
- **Core idea:** `slow` builds the clean prefix while `fast` scouts ahead for new values.
- **Memory hook:** 🐢🐇 *Tortoise & hare* — the hare races ahead spotting new values; the tortoise only steps forward to plant each new one.
- **Trigger phrase:** *"remove duplicates from sorted, in place"* / *"in-place compaction."*

---

## Problem 12: Sort Colors — Dutch National Flag 🚦 (Microsoft — CLASSIC!)

### 📋 Full Question
Sort an array containing only `0`s, `1`s, and `2`s **in place**, in a single pass, without a library sort.

- **Input:** `nums = [2, 0, 2, 1, 1, 0]`
- **Expected Output:** `[0, 0, 1, 1, 2, 2]`

### 🗣️ What to say first
- "Only values 0, 1, 2, sorted in place?" (Yes.)
- "Single pass / O(1) space?" (Yes — that's the point of Dutch National Flag.)

### Approach 1 — Brute Force (counting sort, two pass)
Count 0s, 1s, 2s, then overwrite. Correct but two passes.

```java
public void sortColorsBrute(int[] nums) {
    int[] count = new int[3];
    for (int n : nums) count[n]++;
    int idx = 0;
    for (int color = 0; color < 3; color++)
        while (count[color]-- > 0) nums[idx++] = color;
}
```
- **Time:** O(n) (two passes). **Space:** O(1).
- ❌ Why not enough: works, but the interviewer specifically wants the elegant **one-pass** three-pointer technique.

### Approach 2 — Optimal (Dutch National Flag, three pointers)
`low` marks the next 0 slot, `high` the next 2 slot, `mid` scans. Swap 0s to the front, 2s to the back, leave 1s in the middle.

```java
public void sortColors(int[] nums) {
    int low = 0, mid = 0, high = nums.length - 1;
    while (mid <= high) {
        if (nums[mid] == 0) {
            swap(nums, low++, mid++);   // send 0 left, advance both
        } else if (nums[mid] == 1) {
            mid++;                       // 1 is already centered
        } else { // nums[mid] == 2
            swap(nums, mid, high--);     // send 2 right; don't advance mid (recheck swapped value)
        }
    }
}

private void swap(int[] a, int i, int j) {
    int t = a[i]; a[i] = a[j]; a[j] = t;
}
```

**🔍 Dry run** on `[2, 0, 2, 1, 1, 0]` (low/mid/high in brackets):
| array | low | mid | high | action |
|-------|-----|-----|------|--------|
| [2,0,2,1,1,0] | 0 | 0 | 5 | mid=2 → swap mid,high → [0,0,2,1,1,2], high=4 |
| [0,0,2,1,1,2] | 0 | 0 | 4 | mid=0 → swap low,mid → low=1,mid=1 |
| [0,0,2,1,1,2] | 1 | 1 | 4 | mid=0 → swap low,mid → low=2,mid=2 |
| [0,0,2,1,1,2] | 2 | 2 | 4 | mid=2 → swap mid,high → [0,0,1,1,2,2], high=3 |
| [0,0,1,1,2,2] | 2 | 2 | 3 | mid=1 → mid=3 |
| [0,0,1,1,2,2] | 2 | 3 | 3 | mid=1 → mid=4 → stop |
| **[0,0,1,1,2,2]** ✅ | | | | done |

- **Time:** O(n) single pass. **Space:** O(1).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Dutch National Flag (three-way partition).
- **Core idea:** Three regions (< pivot, = pivot, > pivot); push extremes to their ends as you scan.
- **Memory hook:** 🇳🇱 *The Dutch flag* — red, white, blue stripes; sweep reds to top, blues to bottom, white falls in between.
- **Trigger phrase:** *"only three distinct values"* / *"sort 0s, 1s, 2s in one pass."*

---

## Problem 13: Trapping Rain Water 🌧️ (Microsoft — FREQUENT, HARD!)

### 📋 Full Question
Given `height` representing an elevation map (bar widths = 1), compute how much rainwater is trapped after raining.

- **Input:** `height = [0,1,0,2,1,0,1,3,2,1,2,1]`
- **Expected Output:** `6`

### 🗣️ What to say first
- "Water above bar `i` = min(tallest to its left, tallest to its right) − height[i], if positive — agreed?"
- "Can heights be 0, and do you want O(1) space?" (Yes, and the two-pointer version achieves it.)

### Approach 1 — Brute Force (scan left & right for each bar)
For each bar, scan both directions for the max walls.

```java
public int trapBrute(int[] height) {
    int total = 0;
    for (int i = 0; i < height.length; i++) {
        int leftMax = 0, rightMax = 0;
        for (int l = 0; l <= i; l++) leftMax = Math.max(leftMax, height[l]);
        for (int r = i; r < height.length; r++) rightMax = Math.max(rightMax, height[r]);
        total += Math.min(leftMax, rightMax) - height[i];
    }
    return total;
}
```
- **Time:** O(n²). **Space:** O(1).
- ❌ Why not enough: re-scanning both sides per bar is quadratic — too slow.

### Approach 2 — Optimal (Two Pointers from both ends)
Walk `left` and `right` inward, tracking `leftMax` and `rightMax`. The shorter side bounds the water, so process whichever side is lower. Each bar's trapped water = (current side's max) − height.

```java
public int trap(int[] height) {
    int left = 0, right = height.length - 1;
    int leftMax = 0, rightMax = 0, total = 0;
    while (left < right) {
        if (height[left] < height[right]) {
            // left side is the limiting wall
            if (height[left] >= leftMax) leftMax = height[left];
            else total += leftMax - height[left];
            left++;
        } else {
            // right side is the limiting wall
            if (height[right] >= rightMax) rightMax = height[right];
            else total += rightMax - height[right];
            right--;
        }
    }
    return total;
}
```

**🔍 Dry run** (abbreviated) on `[0,1,0,2,1,0,1,3,2,1,2,1]`:
- Pointers squeeze inward; whenever the current bar is below the running max on its (limiting) side, that gap fills with water.
- Trapped contributions add up across the dips (the 0s and 1s nestled between the 2s and the 3) to a total of **6** ✅.
- Key intuition to state: *"Because I always move the smaller side, I know the opposite wall is at least as tall, so `min(leftMax, rightMax)` is safely the side I'm using."*

- **Time:** O(n) single pass. **Space:** O(1).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Two Pointers + running max (the "limited by the shorter wall" insight).
- **Core idea:** Water over a bar = min(maxLeft, maxRight) − height; the shorter pointer is always the bound, so advance it.
- **Memory hook:** 🪣 *A bucket leaks at its shortest stave* — water level is capped by the lower wall, so trust the shorter side and move it in.
- **Trigger phrase:** *"trapped water / container / elevation map."* (Pure max area? That's the sibling "Container With Most Water.")

---

## Problem 14: 4Sum 🎰 (Microsoft asked)

### 📋 Full Question
Return **all unique quadruplets** `[a, b, c, d]` from `nums` such that `a + b + c + d == target`. No duplicate quadruplets.

- **Input:** `nums = [1, 0, -1, 0, -2, 2]`, `target = 0`
- **Expected Output:** `[[-2, -1, 1, 2], [-2, 0, 0, 2], [-1, 0, 0, 1]]`

### 🗣️ What to say first
- "Can I sort, and must quadruplets be unique?" (Yes and yes.)
- "Could the sum overflow `int`?" (Use `long` for the running sum to be safe.)

### Approach 1 — Brute Force
Four nested loops, dedup with a set.

```java
public List<List<Integer>> fourSumBrute(int[] nums, int target) {
    Set<List<Integer>> result = new HashSet<>();
    Arrays.sort(nums); // sort so each quad is in canonical order for dedup
    int n = nums.length;
    for (int i = 0; i < n; i++)
        for (int j = i + 1; j < n; j++)
            for (int k = j + 1; k < n; k++)
                for (int l = k + 1; l < n; l++)
                    if ((long) nums[i] + nums[j] + nums[k] + nums[l] == target)
                        result.add(Arrays.asList(nums[i], nums[j], nums[k], nums[l]));
    return new ArrayList<>(result);
}
```
- **Time:** O(n⁴). **Space:** O(n) for the dedup set.
- ❌ Why not enough: O(n⁴) is far too slow; fixing two numbers and two-pointering the rest is O(n³).

### Approach 2 — Optimal (Sort + fix two + Two Pointers)
Sort. Fix the outer two indices `i` and `j`, then run a two-pointer search on the remaining sorted subarray for the needed sum. Skip duplicates at every level. Use `long` to avoid overflow.

```java
public List<List<Integer>> fourSum(int[] nums, int target) {
    Arrays.sort(nums);
    List<List<Integer>> result = new ArrayList<>();
    int n = nums.length;
    for (int i = 0; i < n - 3; i++) {
        if (i > 0 && nums[i] == nums[i - 1]) continue;           // skip dup first anchor
        for (int j = i + 1; j < n - 2; j++) {
            if (j > i + 1 && nums[j] == nums[j - 1]) continue;   // skip dup second anchor
            int left = j + 1, right = n - 1;
            while (left < right) {
                long sum = (long) nums[i] + nums[j] + nums[left] + nums[right];
                if (sum == target) {
                    result.add(Arrays.asList(nums[i], nums[j], nums[left], nums[right]));
                    left++; right--;
                    while (left < right && nums[left] == nums[left - 1]) left++;   // skip dup
                    while (left < right && nums[right] == nums[right + 1]) right--; // skip dup
                } else if (sum < target) {
                    left++;   // need bigger
                } else {
                    right--;  // need smaller
                }
            }
        }
    }
    return result;
}
```

**🔍 Dry run** on `[1, 0, -1, 0, -2, 2]`, target `0` → sorted `[-2, -1, 0, 0, 1, 2]`:
- i=-2, j=-1: need 3 from `[0,0,1,2]`. left=0,right=2 → sum 1+(-2-1)= 1−3... compute directly: -2 + -1 + 0 + 2 = -1 (<0) → left++; -2 + -1 + 0 + 2 again with left at second 0 → still <0; left at 1: -2 -1 +1 +2 = 0 ✅ → `[-2,-1,1,2]`.
- i=-2, j=0: -2 + 0 + 0 + 2 = 0 ✅ → `[-2,0,0,2]`.
- i=-1, j=0: -1 + 0 + 0 + 1 = 0 ✅ → `[-1,0,0,1]`.
- Result `[[-2,-1,1,2], [-2,0,0,2], [-1,0,0,1]]` ✅

- **Time:** O(n³) — two fixed loops × inner two-pointer. **Space:** O(1) extra (ignoring output).

### 🧠 Algorithm to Remember Forever
- **Pattern:** Sort + Fix-Two + Two Pointers (the kSum generalization).
- **Core idea:** Each extra "sum" target adds one fixed loop; the innermost 2Sum is always two pointers.
- **Memory hook:** 🪢🪢 *Pin two knots, tighten the rope* — 3Sum pins one, 4Sum pins two, then squeeze the rest.
- **Trigger phrase:** *"four numbers that sum to target"* — and generally *"kSum."*

---

## Problem 15: Intersection of Two Arrays II 🔁 (Microsoft asked)

### 📋 Full Question
Given two integer arrays `nums1` and `nums2`, return their **intersection including duplicates** — each element appears as many times as it shows in *both* arrays. Output order doesn't matter.

- **Input:** `nums1 = [1, 2, 2, 1]`, `nums2 = [2, 2]`
- **Expected Output:** `[2, 2]`
- Another: `nums1 = [4,9,5]`, `nums2 = [9,4,9,8,4]` → `[4, 9]` (order-free)

### 🗣️ What to say first
- "Should duplicates be repeated in the result by how many times they appear in both?" (Yes — that's the 'II' twist.)
- "Are the arrays sorted, and which is smaller?" (If unsorted, I'll hash the smaller one to save space.)

### Approach 1 — Brute Force (nested scan with used markers)
For each element of `nums1`, scan `nums2` for an unused match; mark it used so duplicates pair up correctly.

```java
public int[] intersectBrute(int[] nums1, int[] nums2) {
    boolean[] used = new boolean[nums2.length];
    List<Integer> result = new ArrayList<>();
    for (int a : nums1) {
        for (int j = 0; j < nums2.length; j++) {
            if (!used[j] && nums2[j] == a) {
                result.add(a);
                used[j] = true; // consume this match so it can't pair again
                break;
            }
        }
    }
    return result.stream().mapToInt(Integer::intValue).toArray();
}
```
- **Time:** O(n × m). **Space:** O(m) for the used flags.
- ❌ Why not enough: scanning `nums2` for every element of `nums1` is quadratic; a frequency map makes lookups O(1).

### Approach 2 — Optimal (HashMap frequency count)
Count frequencies of the smaller array. Walk the other array; when an element has remaining count, add it and decrement. This naturally caps each value at the min of its two counts.

```java
public int[] intersect(int[] nums1, int[] nums2) {
    // Count the smaller array to minimize space
    if (nums1.length > nums2.length) return intersect(nums2, nums1);

    Map<Integer, Integer> count = new HashMap<>();
    for (int n : nums1) count.merge(n, 1, Integer::sum);

    List<Integer> result = new ArrayList<>();
    for (int n : nums2) {
        int remaining = count.getOrDefault(n, 0);
        if (remaining > 0) {
            result.add(n);             // pair one occurrence
            count.put(n, remaining - 1); // consume it
        }
    }

    int[] out = new int[result.size()];
    for (int i = 0; i < out.length; i++) out[i] = result.get(i);
    return out;
}
```
> **Sorted-input variant:** if both arrays are sorted, use two pointers — advance the pointer at the smaller value, and on equality emit and advance both. O(1) extra space.

**🔍 Dry run** on `nums1 = [1,2,2,1]`, `nums2 = [2,2]`:
- count (from nums1) = {1:2, 2:2}
- nums2: first 2 → remaining 2>0 → add 2, count{2:1}. Second 2 → remaining 1>0 → add 2, count{2:0}.
- result `[2, 2]` ✅

- **Time:** O(n + m). **Space:** O(min(n, m)) for the map.

### 🧠 Algorithm to Remember Forever
- **Pattern:** Frequency Map (count-and-consume) — or Two Pointers if both are sorted.
- **Core idea:** Tally one side's counts, then "spend" them while scanning the other side.
- **Memory hook:** 🎫 *Ticket stubs* — you hold a stub for each item in array A; every match in array B tears one stub off, so you never over-count.
- **Trigger phrase:** *"intersection of two arrays"* + *"include duplicates / as many times as it appears in both."*

---

## 📊 Complexity Cheat Sheet

| # | Problem | Brute Force | Optimal | Technique |
|---|---------|-------------|---------|-----------|
| 1 | Two Sum | O(n²) / O(1) | O(n) / O(n) | Complement HashMap |
| 2 | Roman to Integer | O(n) / O(n) | O(n) / O(1) | Lookahead compare |
| 3 | Majority Element | O(n) / O(n) | O(n) / O(1) | Boyer-Moore Voting |
| 4 | Top K Frequent | O(n + m log m) / O(m) | O(n) / O(n) | Freq Map + Bucket Sort |
| 5 | Subarray Sum = K | O(n²) / O(1) | O(n) / O(n) | Prefix Sum + HashMap |
| 6 | Longest Consecutive | O(n log n) / O(1) | O(n) / O(n) | HashSet streak-start |
| 7 | First Unique Character | O(n²) / O(1) | O(n) / O(1) | Frequency Map (2-pass) |
| 8 | Two Sum II (sorted) | O(n²) / O(1) | O(n) / O(1) | Two Pointers (opposite) |
| 9 | 3Sum | O(n³) / O(n) | O(n²) / O(1) | Sort + Fix-One + Two Ptr |
| 10 | Valid Palindrome | O(n) / O(n) | O(n) / O(1) | Two Pointers + skip filter |
| 11 | Remove Duplicates (sorted) | O(n) / O(n) | O(n) / O(1) | Two Pointers (slow/fast) |
| 12 | Sort Colors | O(n) / O(1) (2-pass) | O(n) / O(1) (1-pass) | Dutch National Flag |
| 13 | Trapping Rain Water | O(n²) / O(1) | O(n) / O(1) | Two Pointers + max |
| 14 | 4Sum | O(n⁴) / O(n) | O(n³) / O(1) | Sort + Fix-Two + Two Ptr |
| 15 | Intersection of Two Arrays II | O(n × m) / O(m) | O(n + m) / O(min(n,m)) | Freq Map (count-and-consume) |

*(m = number of distinct elements or second-array length, per problem. Space for results excluded.)*

---

## 🧠🧠 ALGORITHM RECOGNITION TABLE

> The single most valuable skill: hearing the **trigger** and instantly knowing the **algorithm**.

| If the question says... (Trigger) | → Use Algorithm | → Memory Hook | → Big-O |
|-----------------------------------|-----------------|---------------|---------|
| "two numbers add up to target" + **unsorted** | Complement HashMap | 🧦 Sock drawer | O(n) / O(n) |
| "two numbers add up to target" + **sorted**, O(1) space | Two Pointers (opposite) | 🎚️ Tuning dial | O(n) / O(1) |
| "convert notation where order changes meaning" | Lookahead compare | 🪜 Dipping stairs | O(n) / O(1) |
| "appears more than half the time", O(1) space | Boyer-Moore Voting | 🗳️ Election cancellation | O(n) / O(1) |
| "top k most frequent / common" | Freq Map + Bucket/Heap | 🪣 Mailbox slots | O(n) / O(n) |
| "number of subarrays summing to k" (+ negatives) | Prefix Sum + HashMap | 🛣️ Mile markers | O(n) / O(n) |
| "longest consecutive run", unsorted, O(n) | HashSet streak-start | 🚂 Find the engine | O(n) / O(n) |
| "first non-repeating / first unique character" | Frequency Map + 2nd pass | 🎟️ Roll call | O(n) / O(1) |
| "three / four numbers sum to target" | Sort + Fix + Two Pointers | 🪢 Pin one/two knots | O(n²) / O(n³) |
| "is it a palindrome / reads same both ways" | Two Pointers (opposite) | 🪞 Mirror walk | O(n) / O(1) |
| "remove duplicates from sorted, in place" | Two Pointers (slow/fast) | 🐢🐇 Tortoise & hare | O(n) / O(1) |
| "sort 0s, 1s, 2s in one pass" | Dutch National Flag | 🇳🇱 Dutch flag | O(n) / O(1) |
| "trapped water / elevation map" | Two Pointers + max | 🪣 Shortest stave | O(n) / O(1) |
| "intersection with duplicates" | Freq Map count-and-consume | 🎫 Ticket stubs | O(n+m) / O(min(n,m)) |
| "count / track what I've seen" | HashMap / HashSet | 👀 Memory notebook | O(n) / O(n) |

---

## ☕ Java Tips for HashMap, HashSet & Two Pointers

These idioms make your interview code clean and senior-looking:

**HashMap counting (frequency map) — the one-liner:**
```java
Map<Integer, Integer> freq = new HashMap<>();
for (int n : nums) freq.merge(n, 1, Integer::sum); // freq[n] = freq.getOrDefault(n,0)+1
```

**Counting characters fast (lowercase only):**
```java
int[] freq = new int[26];
for (char c : s.toCharArray()) freq[c - 'a']++; // 'a' maps to index 0
```

**Safe reads (never NullPointerException):**
```java
int count = freq.getOrDefault(key, 0);     // default when absent
freq.putIfAbsent(key, new ArrayList<>());  // init only if missing
```

**HashSet for "have I seen it?" / dedup:**
```java
Set<Integer> seen = new HashSet<>();
if (!seen.add(x)) { /* x was already present -> duplicate */ }
```
> ⚡ `set.add(x)` returns `false` if `x` was already there — a slick duplicate detector.

**Iterating a map:**
```java
for (Map.Entry<Integer, Integer> e : freq.entrySet()) {
    int key = e.getKey(), value = e.getValue();
}
```

**Two-pointer skeletons to memorize:**
```java
// Opposite ends (sorted-array pair problems, palindrome, trapping water)
int left = 0, right = n - 1;
while (left < right) { /* compare / process, then move a pointer inward */ }

// Same direction (slow/fast, in-place compaction)
int slow = 0;
for (int fast = 1; fast < n; fast++) {
    if (nums[fast] != nums[slow]) nums[++slow] = nums[fast];
}
// length of unique prefix = slow + 1
```

**kSum reduction skeleton (3Sum / 4Sum):**
```java
Arrays.sort(nums);
// fix (k-2) anchors with for-loops, skipping duplicates,
// then run a two-pointer 2Sum on the remaining suffix.
// Use `long` for the running sum in 4Sum to avoid int overflow.
```

**Sorting + custom comparator (for 3Sum, Top K, etc.):**
```java
Arrays.sort(nums);                                   // ascending in place
list.sort((a, b) -> freq.get(b) - freq.get(a));      // by frequency desc
```

**Characters:**
```java
Character.isLetterOrDigit(c);   // alphanumeric filter (Valid Palindrome)
Character.toLowerCase(c);       // normalize case
```

**Watch-outs (classic Java interview traps):**
- ⚠️ Use `.equals()` not `==` when comparing `Integer` objects above 127 (autoboxing cache!). Inside maps you're safe, but never write `if (boxedA == boxedB)`.
- ⚠️ `int[]` has no `.length()` — it's `.length` (no parens). `String` uses `.length()`. `List` uses `.size()`.
- ⚠️ Initialize `leftMax`/`rightMax` to 0 (heights are non-negative) — state this assumption aloud.
- ⚠️ `Map.of(...)` is **immutable** — great for fixed lookup tables (Roman), but don't try to `put` into it.
- ⚠️ In 4Sum, cast to `long` before summing four `int`s — four large values can overflow `int`.

---

## ✅ Final Interview Checklist

Before you say "I'm done," tick every box: 🎯

- [ ] **Restated** the problem and **asked clarifying questions** (sorted? duplicates? negatives? empty?).
- [ ] **Mentioned the brute force** and its complexity before optimizing.
- [ ] **Named the pattern out loud** ("this is a complement-HashMap problem...").
- [ ] **Explained the optimal plan in plain English** before coding.
- [ ] Wrote **clean, compiling Java** — correct `.length` vs `.length()`, no `==` on Integers.
- [ ] **Dry-ran** the code on the given example, narrating each step.
- [ ] **Stated final Time and Space** complexity, including tradeoffs.
- [ ] **Handled edge cases**: empty array, single element, all duplicates, negatives, no-solution.
- [ ] Stayed **calm and talked continuously** — silence is the enemy; think out loud. 🗣️

> 💡 **Golden mantra:** *"Sorted? → Two Pointers. Counting / pairs / subarrays? → HashMap."* Say it under your breath when you're stuck, and the right pattern will surface. 🌟

Good luck — you've got this! 💪🍀
</content>
</invoke>
