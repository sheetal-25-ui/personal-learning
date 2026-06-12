# Week 3: Sliding Window & Binary Search — How to ANSWER in a Microsoft Interview 🎤 (Java)

Welcome! This file teaches you how to **talk through** this week's problems like a pro in a Microsoft coding interview. The goal isn't just to get the right answer — it's to show the interviewer your **thought process**. Interviewers hire the way you *think*, not just the code you *type*. 🧠

---

## 🏆 The Golden Rule of Interview Answers

For EVERY problem, walk this ladder out loud:

> **Clarify → Brute Force → Optimal → Best → Complexity**

Never jump straight to the optimal solution silently. Narrate. The interviewer is grading your reasoning.

### 🗣️ The Interview Script (what to say out loud)

1. **"Let me make sure I understand the problem."** — Restate it in your own words and give a tiny example.
2. **"Let me ask a few clarifying questions."** — Edge cases, input size, character set, duplicates, etc.
3. **"Let me start with a brute-force idea so we have something correct."** — Describe it, state its Big-O. (Don't always code it fully — sometimes just describe it.)
4. **"I think we can do better. Notice that…"** — Reveal the key insight (sliding window / binary search).
5. **"Here's my optimal approach."** — Code it cleanly, talking as you go.
6. **"Let me dry-run it on the example."** — Trace a few steps to prove it works.
7. **"Complexity is O(…) time and O(…) space."** — Always end here.
8. **"Let me check edge cases."** — Empty input, single element, all-same, target not found.

### ❓ Common Clarifying Questions (memorize these)

- "What's the size of the input? Could it be empty or null?"
- "Are values/characters only ASCII, or full Unicode?"
- "Can the array contain duplicates? Negative numbers?"
- "Is the array sorted? Rotated? Are there guaranteed bounds?"
- "If there are multiple valid answers, which one should I return?"
- "Should I optimize for time or for memory?"
- "Can I assume the input fits in memory?"

> 💡 **Pro move:** Even if you know the answer, asking ONE good clarifying question signals maturity. Microsoft loves candidates who think about edge cases before coding.

---

## 1️⃣ Longest Substring Without Repeating Characters ⭐ (Top 3 Microsoft!)

📋 **Full Question**
Given a string `s`, find the length of the **longest substring** without repeating characters.
- Input: `s = "abcabcbb"` → Output: `3` (the answer is `"abc"`)
- Input: `s = "bbbbb"` → Output: `1` (the answer is `"b"`)
- Input: `s = "pwwkew"` → Output: `3` (the answer is `"wke"` — note `"pwke"` is a *subsequence*, not a substring)

🗣️ **What to say first**
- "Substring means contiguous, right? Not subsequence?"
- "What character set — ASCII, lowercase only, or full Unicode?"
- "Should I return the length or the actual substring?" (It's the length.)
- "Can the string be empty?" (Then return 0.)

**Approach 1 — Brute Force**
Check every possible substring and test if it has all unique characters.

```java
public int lengthOfLongestSubstringBrute(String s) {
    int n = s.length();
    int best = 0;
    for (int i = 0; i < n; i++) {
        for (int j = i; j < n; j++) {
            if (allUnique(s, i, j)) {
                best = Math.max(best, j - i + 1);
            }
        }
    }
    return best;
}

private boolean allUnique(String s, int start, int end) {
    boolean[] seen = new boolean[128]; // ASCII
    for (int k = start; k <= end; k++) {
        char c = s.charAt(k);
        if (seen[c]) return false;
        seen[c] = true;
    }
    return true;
}
```
- ⏱️ Time: **O(n³)** (n² substrings × O(n) uniqueness check), Space: **O(1)** (fixed 128 array).
- ❌ Why not enough: Re-checking uniqueness from scratch wastes work — way too slow for large strings.

**Approach 2 — Optimal (Sliding Window)**
Grow a window with `right`. When a duplicate appears, shrink from `left` until the duplicate is gone. Track the max window size.

```java
public int lengthOfLongestSubstring(String s) {
    int[] lastIndex = new int[128]; // last seen index + 1 for each ASCII char
    java.util.Arrays.fill(lastIndex, 0);
    int left = 0, best = 0;
    for (int right = 0; right < s.length(); right++) {
        char c = s.charAt(right);
        // If we've seen c inside the current window, jump left past it
        left = Math.max(left, lastIndex[c]);
        best = Math.max(best, right - left + 1);
        lastIndex[c] = right + 1; // store next valid start
    }
    return best;
}
```

🔍 **Dry-run on `"abcabcbb"`:**
| right | char | left jumps to | window | best |
|-------|------|---------------|--------|------|
| 0 | a | 0 | "a" | 1 |
| 1 | b | 0 | "ab" | 2 |
| 2 | c | 0 | "abc" | 3 |
| 3 | a | 1 (saw 'a' at 0) | "bca" | 3 |
| 4 | b | 2 | "cab" | 3 |
| 5 | c | 3 | "abc" | 3 |
| 6 | b | 5 | "cb" | 3 |
| 7 | b | 6 | "b" | 3 |

Result: **3** ✅

- ⏱️ Time: **O(n)** (each char visited once), Space: **O(1)** (fixed 128 array, or O(min(n, charset))).

🧠 **Algorithm to Remember Forever**
- **Pattern:** Sliding Window (variable size).
- **Core idea:** Expand right, shrink left only when a rule breaks.
- **Memory hook:** 🐛 Think of an **inchworm** crawling along the string — its head (right) moves forward, and its tail (left) catches up only when it has to.
- **Trigger phrase:** *"longest substring with [some condition]"*

---

## 2️⃣ Minimum Window Substring (Hard) ⭐

📋 **Full Question**
Given strings `s` and `t`, return the **smallest substring** of `s` that contains **all** characters of `t` (including duplicates). If none exists, return `""`.
- Input: `s = "ADOBECODEBANC"`, `t = "ABC"` → Output: `"BANC"`
- Input: `s = "a"`, `t = "aa"` → Output: `""` (not enough a's)

🗣️ **What to say first**
- "Do I need to preserve the order of `t`'s characters? (No — just contain them all.)"
- "Do duplicates in `t` matter? (Yes — if `t = "aa"` I need two a's.)"
- "If multiple minimum windows exist, is any one fine?"

**Approach 1 — Brute Force**
Try every substring of `s`, check if it contains all of `t`, keep the shortest.
```java
public String minWindowBrute(String s, String t) {
    String best = "";
    for (int i = 0; i < s.length(); i++) {
        for (int j = i; j < s.length(); j++) {
            String sub = s.substring(i, j + 1);
            if (contains(sub, t) && (best.isEmpty() || sub.length() < best.length())) {
                best = sub;
            }
        }
    }
    return best;
}
private boolean contains(String sub, String t) {
    int[] need = new int[128];
    for (char c : t.toCharArray()) need[c]++;
    for (char c : sub.toCharArray()) need[c]--;
    for (int v : need) if (v > 0) return false;
    return true;
}
```
- ⏱️ Time: **O(n² · m)**, Space: **O(1)**.
- ❌ Why not enough: Quadratic substring generation + per-substring check is far too slow.

**Approach 2 — Optimal (Sliding Window with `have`/`need` counters)**
Expand right until the window is valid (contains all of `t`), then shrink left to minimize, recording the best.

```java
public String minWindow(String s, String t) {
    if (s.length() < t.length()) return "";
    int[] need = new int[128];
    for (char c : t.toCharArray()) need[c]++;
    int required = t.length();        // total chars still needed
    int left = 0, bestLen = Integer.MAX_VALUE, bestStart = 0;

    for (int right = 0; right < s.length(); right++) {
        char c = s.charAt(right);
        if (need[c] > 0) required--;   // this char was useful
        need[c]--;

        while (required == 0) {        // window is valid → try to shrink
            if (right - left + 1 < bestLen) {
                bestLen = right - left + 1;
                bestStart = left;
            }
            char lc = s.charAt(left);
            need[lc]++;
            if (need[lc] > 0) required++; // we just lost a needed char
            left++;
        }
    }
    return bestLen == Integer.MAX_VALUE ? "" : s.substring(bestStart, bestStart + bestLen);
}
```

🔍 **Dry-run on `s = "ADOBECODEBANC"`, `t = "ABC"`:**
- Expand right until window `"ADOBEC"` first contains A, B, C → `required == 0`.
- Shrink left: `"DOBEC"`? lost A → invalid, stop. Best so far = `"ADOBEC"` (len 6).
- Keep expanding... window `"CODEBA"`... eventually valid windows shrink to `"BANC"` (len 4).
- Final best: **`"BANC"`** ✅

- ⏱️ Time: **O(n + m)** (each char enters/leaves the window once), Space: **O(1)** (128 array).

🧠 **Algorithm to Remember Forever**
- **Pattern:** Sliding Window + frequency counter (`have`/`need`).
- **Core idea:** Grow to become *valid*, shrink to become *minimal*.
- **Memory hook:** 🎯 A **camera zoom** — zoom out until everyone's in frame (valid), then zoom in as tight as possible without cutting anyone off (minimal).
- **Trigger phrase:** *"smallest/minimum window/substring containing all of …"*

---

## 3️⃣ Longest Repeating Character Replacement (Medium) ⭐

📋 **Full Question**
Given a string `s` and integer `k`, you may replace at most `k` characters with any uppercase letter. Return the length of the **longest substring containing the same letter** you can get.
- Input: `s = "AABABBA"`, `k = 1` → Output: `4` (replace one B → `"AABA"`→`"AAAA"`... best is `"ABBA"`→`"BBBB"` length 4)
- Input: `s = "ABAB"`, `k = 2` → Output: `4`

🗣️ **What to say first**
- "Only uppercase English letters? (Yes — 26-size array works.)"
- "Can `k` be larger than the string? (Then the whole string is the answer.)"
- "I want the longest substring that becomes all-same after ≤ k replacements, right?"

**Approach 1 — Brute Force**
For every substring, find its most frequent char; if `(length - maxFreq) <= k`, it's valid.
```java
public int characterReplacementBrute(String s, int k) {
    int best = 0;
    for (int i = 0; i < s.length(); i++) {
        int[] count = new int[26];
        int maxFreq = 0;
        for (int j = i; j < s.length(); j++) {
            count[s.charAt(j) - 'A']++;
            maxFreq = Math.max(maxFreq, count[s.charAt(j) - 'A']);
            int windowLen = j - i + 1;
            if (windowLen - maxFreq <= k) best = Math.max(best, windowLen);
        }
    }
    return best;
}
```
- ⏱️ Time: **O(n²)**, Space: **O(1)**.
- ❌ Why not enough: O(n²) is too slow when n is large; we recompute overlapping windows.

**Approach 2 — Optimal (Sliding Window with maxFreq)**
A window is valid if `windowLength - maxFrequencyOfAnyChar <= k`. Slide right; if invalid, shrink left by one.

```java
public int characterReplacement(String s, int k) {
    int[] count = new int[26];
    int left = 0, maxFreq = 0, best = 0;
    for (int right = 0; right < s.length(); right++) {
        count[s.charAt(right) - 'A']++;
        maxFreq = Math.max(maxFreq, count[s.charAt(right) - 'A']);
        // If chars to replace exceed k, shrink window from the left
        while ((right - left + 1) - maxFreq > k) {
            count[s.charAt(left) - 'A']--;
            left++;
        }
        best = Math.max(best, right - left + 1);
    }
    return best;
}
```

🔍 **Dry-run on `s = "AABABBA"`, `k = 1`:**
- Window grows `"AAB"` → len 3, maxFreq(A)=2, 3-2=1 ≤ 1 ✅ best=3.
- `"AABA"` → maxFreq(A)=3, 4-3=1 ≤ 1 ✅ best=4.
- `"AABAB"` → maxFreq(A)=3, 5-3=2 > 1 ❌ shrink left → `"ABAB"`.
- Continues, best stays **4** ✅

- ⏱️ Time: **O(n)**, Space: **O(1)** (26 letters).

> 🧩 **Sneaky note for the interviewer:** "I never *decrease* `maxFreq` when shrinking. That's fine — the window length only matters when it grows, and a stale `maxFreq` can never make us report a window longer than a previously valid one. It's a classic optimization here."

🧠 **Algorithm to Remember Forever**
- **Pattern:** Sliding Window + "fill the gaps" counting.
- **Core idea:** Keep the window where *non-dominant chars ≤ k*.
- **Memory hook:** 🎨 You have `k` buckets of paint — repaint up to k tiles so the longest stretch becomes one solid color.
- **Trigger phrase:** *"longest substring after replacing at most k characters"*

---

## 4️⃣ Permutation in String (Medium) ⭐

📋 **Full Question**
Given `s1` and `s2`, return `true` if `s2` contains a **permutation of `s1`** as a substring.
- Input: `s1 = "ab"`, `s2 = "eidbaooo"` → Output: `true` (`"ba"` is a permutation of `"ab"`)
- Input: `s1 = "ab"`, `s2 = "eidboaoo"` → Output: `false`

🗣️ **What to say first**
- "Permutation means same characters with same counts, any order — right?"
- "Lowercase only? (Yes → 26 array.)"
- "If `s1` is longer than `s2`, the answer is automatically false, correct?"

**Approach 1 — Brute Force**
Sort `s1`; for every window of length `s1.length()` in `s2`, sort it and compare.
```java
public boolean checkInclusionBrute(String s1, String s2) {
    if (s1.length() > s2.length()) return false;
    char[] target = s1.toCharArray();
    java.util.Arrays.sort(target);
    String sortedTarget = new String(target);
    for (int i = 0; i + s1.length() <= s2.length(); i++) {
        char[] window = s2.substring(i, i + s1.length()).toCharArray();
        java.util.Arrays.sort(window);
        if (new String(window).equals(sortedTarget)) return true;
    }
    return false;
}
```
- ⏱️ Time: **O(n · m log m)**, Space: **O(m)**.
- ❌ Why not enough: Sorting each window is wasteful; a fixed-size frequency window is far faster.

**Approach 2 — Optimal (Fixed-Size Sliding Window of frequencies)**
Maintain a window of `s1.length()` in `s2`. Compare frequency arrays as you slide.

```java
public boolean checkInclusion(String s1, String s2) {
    if (s1.length() > s2.length()) return false;
    int[] need = new int[26];
    int[] window = new int[26];
    for (int i = 0; i < s1.length(); i++) {
        need[s1.charAt(i) - 'a']++;
        window[s2.charAt(i) - 'a']++;
    }
    int matches = 0;
    for (int i = 0; i < 26; i++) if (need[i] == window[i]) matches++;

    for (int right = s1.length(); right < s2.length(); right++) {
        if (matches == 26) return true;
        // add right char
        int idxIn = s2.charAt(right) - 'a';
        window[idxIn]++;
        if (window[idxIn] == need[idxIn]) matches++;
        else if (window[idxIn] == need[idxIn] + 1) matches--;
        // remove left char
        int idxOut = s2.charAt(right - s1.length()) - 'a';
        window[idxOut]--;
        if (window[idxOut] == need[idxOut]) matches++;
        else if (window[idxOut] == need[idxOut] - 1) matches--;
    }
    return matches == 26;
}
```

🔍 **Dry-run on `s1 = "ab"`, `s2 = "eidbaooo"`:**
- need = {a:1, b:1}. Initial window `"ei"` → no match.
- Slide to `"id"`, `"db"`, `"ba"` → window `"ba"` has a:1, b:1 → all 26 buckets match → **true** ✅

- ⏱️ Time: **O(n)** (26 is constant per step), Space: **O(1)**.

🧠 **Algorithm to Remember Forever**
- **Pattern:** Fixed-Size Sliding Window + frequency match.
- **Core idea:** A window of fixed length that slides one step at a time, updating counts in O(1).
- **Memory hook:** 🚪 A **conveyor belt** — one item rolls on the right as one rolls off the left; the box stays the same size.
- **Trigger phrase:** *"contains a permutation / anagram of …"*

---

## 5️⃣ Maximum Average Subarray I (Easy) ⭐

📋 **Full Question**
Given an integer array `nums` and integer `k`, find the **contiguous subarray of length `k`** with the **maximum average** and return that average.
- Input: `nums = [1,12,-5,-6,50,3]`, `k = 4` → Output: `12.75` (subarray `[12,-5,-6,50]`, sum 51 / 4)

🗣️ **What to say first**
- "Fixed length exactly `k`, correct?"
- "Can numbers be negative? (Yes — so I can't be greedy.)"
- "Return a double / average, not a sum?"

**Approach 1 — Brute Force**
Compute the sum of every length-`k` window from scratch.
```java
public double findMaxAverageBrute(int[] nums, int k) {
    double best = -Double.MAX_VALUE;
    for (int i = 0; i + k <= nums.length; i++) {
        int sum = 0;
        for (int j = i; j < i + k; j++) sum += nums[j];
        best = Math.max(best, (double) sum / k);
    }
    return best;
}
```
- ⏱️ Time: **O(n · k)**, Space: **O(1)**.
- ❌ Why not enough: Recomputing each window's sum repeats work — easily avoided.

**Approach 2 — Optimal (Fixed Window, running sum)**
Slide the window: add the new right element, subtract the element that left.

```java
public double findMaxAverage(int[] nums, int k) {
    int sum = 0;
    for (int i = 0; i < k; i++) sum += nums[i];   // first window
    int best = sum;
    for (int right = k; right < nums.length; right++) {
        sum += nums[right] - nums[right - k];     // slide by one
        best = Math.max(best, sum);
    }
    return (double) best / k;
}
```

🔍 **Dry-run on `[1,12,-5,-6,50,3]`, `k = 4`:**
- First window `[1,12,-5,-6]` sum=2, best=2.
- Slide: +50 -1 → sum=51, best=51 (`[12,-5,-6,50]`).
- Slide: +3 -12 → sum=42, best stays 51.
- Answer: 51/4 = **12.75** ✅

- ⏱️ Time: **O(n)**, Space: **O(1)**.

🧠 **Algorithm to Remember Forever**
- **Pattern:** Fixed-Size Sliding Window (running sum).
- **Core idea:** Don't recompute — adjust the sum by the difference.
- **Memory hook:** 🧮 A **rolling odometer** — add the new mile, drop the oldest; never re-count the whole trip.
- **Trigger phrase:** *"subarray of size k with max/min sum or average"*

---

## 6️⃣ Fruit Into Baskets (Medium) ⭐

📋 **Full Question**
You walk along trees (array `fruits`) and have **2 baskets**, each holding only one type of fruit. Pick the longest contiguous run containing **at most 2 distinct fruit types**. Return its length.
- Input: `fruits = [1,2,1]` → Output: `3`
- Input: `fruits = [0,1,2,2]` → Output: `3` (`[1,2,2]`)
- Input: `fruits = [1,2,3,2,2]` → Output: `4` (`[2,3,2,2]`)

🗣️ **What to say first**
- "So this is literally 'longest subarray with at most 2 distinct values'?"
- "Could it be generalized to k baskets? (Yes — same code with k.)"
- "Can the array be empty?"

**Approach 1 — Brute Force**
For each start, extend while distinct types ≤ 2.
```java
public int totalFruitBrute(int[] fruits) {
    int best = 0;
    for (int i = 0; i < fruits.length; i++) {
        java.util.Set<Integer> types = new java.util.HashSet<>();
        for (int j = i; j < fruits.length; j++) {
            types.add(fruits[j]);
            if (types.size() > 2) break;
            best = Math.max(best, j - i + 1);
        }
    }
    return best;
}
```
- ⏱️ Time: **O(n²)**, Space: **O(1)**.
- ❌ Why not enough: Quadratic; re-scans overlapping ranges.

**Approach 2 — Optimal (Sliding Window, at most K distinct)**
Track counts in a map. When distinct types > 2, shrink left until back to 2.

```java
public int totalFruit(int[] fruits) {
    java.util.Map<Integer, Integer> count = new java.util.HashMap<>();
    int left = 0, best = 0;
    for (int right = 0; right < fruits.length; right++) {
        count.merge(fruits[right], 1, Integer::sum);
        while (count.size() > 2) {               // more than 2 baskets needed
            int leftFruit = fruits[left];
            count.merge(leftFruit, -1, Integer::sum);
            if (count.get(leftFruit) == 0) count.remove(leftFruit);
            left++;
        }
        best = Math.max(best, right - left + 1);
    }
    return best;
}
```

🔍 **Dry-run on `[1,2,3,2,2]`:**
- right=0 `{1:1}` → best 1.
- right=1 `{1:1,2:1}` → best 2.
- right=2 `{1:1,2:1,3:1}` size 3 → shrink: remove 1 → `{2:1,3:1}`, left=1 → best 2.
- right=3 `{2:2,3:1}` → best 3.
- right=4 `{2:3,3:1}` → best **4** ✅

- ⏱️ Time: **O(n)**, Space: **O(1)** (map size ≤ 3).

🧠 **Algorithm to Remember Forever**
- **Pattern:** Sliding Window — "longest with at most K distinct."
- **Core idea:** Keep a count map; shrink whenever distinct count exceeds K.
- **Memory hook:** 🧺 Two **baskets** only — when a third fruit shows up, drop fruit from the back of your trail until you're down to two types again.
- **Trigger phrase:** *"longest subarray with at most K distinct/different …"*

---

## 7️⃣ Search in Rotated Sorted Array (Medium) ⭐ (Very Frequent!)

📋 **Full Question**
A sorted array was rotated at an unknown pivot. Given `nums` and `target`, return its index, or `-1`. Must run in **O(log n)**.
- Input: `nums = [4,5,6,7,0,1,2]`, `target = 0` → Output: `4`
- Input: `nums = [4,5,6,7,0,1,2]`, `target = 3` → Output: `-1`

🗣️ **What to say first**
- "Are there duplicates? (Classic version: no duplicates — important, it changes the logic.)"
- "Is exactly one rotation applied, or could it be the original sorted array?"
- "They want O(log n), so linear scan won't satisfy them — confirming."

**Approach 1 — Brute Force**
Linear scan.
```java
public int searchBrute(int[] nums, int target) {
    for (int i = 0; i < nums.length; i++) {
        if (nums[i] == target) return i;
    }
    return -1;
}
```
- ⏱️ Time: **O(n)**, Space: **O(1)**.
- ❌ Why not enough: The interviewer explicitly wants **O(log n)** — linear scan ignores the sorted structure.

**Approach 2 — Optimal (Modified Binary Search)**
At each step, **one half is always sorted**. Figure out which half is sorted, then check whether the target lies inside that sorted half.

```java
public int search(int[] nums, int target) {
    int left = 0, right = nums.length - 1;
    while (left <= right) {
        int mid = left + (right - left) / 2;   // overflow-safe midpoint
        if (nums[mid] == target) return mid;

        if (nums[left] <= nums[mid]) {          // left half is sorted
            if (nums[left] <= target && target < nums[mid]) {
                right = mid - 1;                // target in sorted left half
            } else {
                left = mid + 1;
            }
        } else {                                // right half is sorted
            if (nums[mid] < target && target <= nums[right]) {
                left = mid + 1;                 // target in sorted right half
            } else {
                right = mid - 1;
            }
        }
    }
    return -1;
}
```

🔍 **Dry-run on `[4,5,6,7,0,1,2]`, `target = 0`:**
- left=0, right=6, mid=3 → nums[3]=7. Left half `[4..7]` sorted. Is 0 in [4,7)? No → search right, left=4.
- left=4, right=6, mid=5 → nums[5]=1. Left half `[0,1]` sorted. Is 0 in [0,1)? Yes → right=4.
- left=4, right=4, mid=4 → nums[4]=0 == target → return **4** ✅

- ⏱️ Time: **O(log n)**, Space: **O(1)**.

🧠 **Algorithm to Remember Forever**
- **Pattern:** Modified Binary Search (rotated array).
- **Core idea:** One half is always sorted — decide which, then check if target is in it.
- **Memory hook:** 🃏 A deck cut into two piles — **one pile is always still in order**. Look there first.
- **Trigger phrase:** *"sorted array that's been rotated"* + *"O(log n)"*

---

## 8️⃣ Koko Eating Bananas (Medium) ⭐ — Binary Search on the ANSWER

📋 **Full Question**
Koko eats bananas at speed `k` per hour. There are `piles[i]` bananas in each pile; each hour she eats from one pile (and if a pile has fewer than `k`, she finishes it and stops for that hour). Given `h` hours, find the **minimum integer speed `k`** to finish all bananas within `h` hours.
- Input: `piles = [3,6,7,11]`, `h = 8` → Output: `4`
- Input: `piles = [30,11,23,4,20]`, `h = 5` → Output: `30`

🗣️ **What to say first**
- "The answer (speed) ranges from 1 to max(piles), right?"
- "Hours per pile = ceil(pile / k), and h ≥ number of piles is guaranteed?"
- "I want the *minimum* speed that still finishes in time — so this is binary search on the answer."

**Approach 1 — Brute Force**
Try every speed `k` from 1 upward; return the first that finishes within `h`.
```java
public int minEatingSpeedBrute(int[] piles, int h) {
    int maxPile = 0;
    for (int p : piles) maxPile = Math.max(maxPile, p);
    for (int k = 1; k <= maxPile; k++) {
        if (hoursNeeded(piles, k) <= h) return k;
    }
    return maxPile;
}
private long hoursNeeded(int[] piles, int k) {
    long hours = 0;
    for (int p : piles) hours += (p + k - 1) / k; // ceil division
    return hours;
}
```
- ⏱️ Time: **O(maxPile · n)**, Space: **O(1)**.
- ❌ Why not enough: Linear scan over speeds is slow when `maxPile` is huge (up to 10⁹).

**Approach 2 — Optimal (Binary Search on Answer)**
The "can finish in time?" check is **monotonic**: if speed `k` works, any larger speed also works. Binary search the smallest `k` that works.

```java
public int minEatingSpeed(int[] piles, int h) {
    int left = 1, right = 0;
    for (int p : piles) right = Math.max(right, p);  // max possible speed
    while (left < right) {
        int mid = left + (right - left) / 2;
        if (hoursNeeded(piles, mid) <= h) {
            right = mid;        // mid works → try slower
        } else {
            left = mid + 1;     // too slow → speed up
        }
    }
    return left;               // smallest speed that finishes in time
}
private long hoursNeeded(int[] piles, int k) {
    long hours = 0;
    for (int p : piles) hours += (p + k - 1) / k;    // ceil(p / k)
    return hours;
}
```

🔍 **Dry-run on `[3,6,7,11]`, `h = 8`:**
- left=1, right=11. mid=6 → hours = 1+1+2+2 = 6 ≤ 8 ✅ → right=6.
- left=1, right=6. mid=3 → hours = 1+2+3+4 = 10 > 8 ❌ → left=4.
- left=4, right=6. mid=5 → hours = 1+2+2+3 = 8 ≤ 8 ✅ → right=5.
- left=4, right=5. mid=4 → hours = 1+2+2+3 = 8 ≤ 8 ✅ → right=4.
- left==right=4 → answer **4** ✅

- ⏱️ Time: **O(n · log(maxPile))**, Space: **O(1)**.

🧠 **Algorithm to Remember Forever**
- **Pattern:** Binary Search on the Answer (parametric / "monotone predicate").
- **Core idea:** Don't search the array — search the *range of possible answers*; use a feasibility check.
- **Memory hook:** 🍌 A **thermostat dial** — turn the speed dial up/down and check "does it finish in time?" until you land on the lowest comfortable setting.
- **Trigger phrase:** *"minimum/maximum [rate/capacity/size] such that [condition holds]"*

---

## 9️⃣ Find First and Last Position (Medium) ⭐

📋 **Full Question**
Given a sorted array and a `target`, return `[firstIndex, lastIndex]` of the target. If absent, return `[-1, -1]`. Must be **O(log n)**.
- Input: `nums = [5,7,7,8,8,10]`, `target = 8` → Output: `[3, 4]`
- Input: `nums = [5,7,7,8,8,10]`, `target = 6` → Output: `[-1, -1]`

🗣️ **What to say first**
- "Array is sorted ascending with possible duplicates, right?"
- "They want O(log n), so I'll do two binary searches — leftmost and rightmost."

**Approach 1 — Brute Force**
Linear scan, record first and last occurrence.
```java
public int[] searchRangeBrute(int[] nums, int target) {
    int first = -1, last = -1;
    for (int i = 0; i < nums.length; i++) {
        if (nums[i] == target) {
            if (first == -1) first = i;
            last = i;
        }
    }
    return new int[]{first, last};
}
```
- ⏱️ Time: **O(n)**, Space: **O(1)**.
- ❌ Why not enough: Doesn't use sorted structure; interviewer wants log n.

**Approach 2 — Optimal (Two Binary Searches: lower & upper bound)**
Find the leftmost index where `nums[i] == target`, then the rightmost.

```java
public int[] searchRange(int[] nums, int target) {
    int first = findBound(nums, target, true);
    if (first == -1) return new int[]{-1, -1};
    int last = findBound(nums, target, false);
    return new int[]{first, last};
}

// findFirst = true → leftmost; false → rightmost
private int findBound(int[] nums, int target, boolean findFirst) {
    int left = 0, right = nums.length - 1, result = -1;
    while (left <= right) {
        int mid = left + (right - left) / 2;
        if (nums[mid] == target) {
            result = mid;
            if (findFirst) right = mid - 1;  // keep looking left
            else           left = mid + 1;   // keep looking right
        } else if (nums[mid] < target) {
            left = mid + 1;
        } else {
            right = mid - 1;
        }
    }
    return result;
}
```

🔍 **Dry-run on `[5,7,7,8,8,10]`, `target = 8`:**
- findFirst: lands on index 3 (keeps pushing left when it sees 8) → first = 3.
- findLast: keeps pushing right when it sees 8 → last = 4.
- Answer: **[3, 4]** ✅

- ⏱️ Time: **O(log n)**, Space: **O(1)**.

🧠 **Algorithm to Remember Forever**
- **Pattern:** Binary Search — lower bound & upper bound.
- **Core idea:** When you find the target, *don't stop* — keep searching toward the edge you want.
- **Memory hook:** 📏 Two **bookmarks** — slide one to the far-left copy, one to the far-right copy of the same word.
- **Trigger phrase:** *"first and last position"* / *"range of a value in a sorted array"*

---

## 🔟 Median of Two Sorted Arrays (Hard) ⭐ (Classic!)

📋 **Full Question**
Given two sorted arrays `nums1` and `nums2`, return the median of the combined sorted array in **O(log(m+n))**.
- Input: `nums1 = [1,3]`, `nums2 = [2]` → Output: `2.0`
- Input: `nums1 = [1,2]`, `nums2 = [3,4]` → Output: `2.5`

🗣️ **What to say first**
- "Both arrays are already sorted, correct?"
- "They want O(log(m+n)) — so merging (O(m+n)) won't fully satisfy, but I can mention it as a baseline."
- "Either array can be empty?"

**Approach 1 — Brute Force (merge, then pick middle)**
Merge both into one sorted array, then take the middle element(s).
```java
public double findMedianSortedArraysBrute(int[] nums1, int[] nums2) {
    int m = nums1.length, n = nums2.length;
    int[] merged = new int[m + n];
    int i = 0, j = 0, k = 0;
    while (i < m && j < n) merged[k++] = nums1[i] <= nums2[j] ? nums1[i++] : nums2[j++];
    while (i < m) merged[k++] = nums1[i++];
    while (j < n) merged[k++] = nums2[j++];
    int total = m + n;
    if (total % 2 == 1) return merged[total / 2];
    return (merged[total / 2 - 1] + merged[total / 2]) / 2.0;
}
```
- ⏱️ Time: **O(m+n)**, Space: **O(m+n)**.
- ❌ Why not enough: Correct, but the interviewer asked for **O(log(m+n))** — we must binary-search the partition.

**Approach 2 — Optimal (Binary Search on the Partition)**
Binary-search a cut in the **smaller** array. The cut splits both arrays so that the left half has `(m+n+1)/2` elements and `maxLeft ≤ minRight`.

```java
public double findMedianSortedArrays(int[] nums1, int[] nums2) {
    // Always binary search the SMALLER array for efficiency
    if (nums1.length > nums2.length) return findMedianSortedArrays(nums2, nums1);
    int m = nums1.length, n = nums2.length;
    int half = (m + n + 1) / 2;           // size of combined left half
    int left = 0, right = m;

    while (left <= right) {
        int cut1 = left + (right - left) / 2;  // elements taken from nums1
        int cut2 = half - cut1;                // elements taken from nums2

        int left1  = (cut1 == 0) ? Integer.MIN_VALUE : nums1[cut1 - 1];
        int right1 = (cut1 == m) ? Integer.MAX_VALUE : nums1[cut1];
        int left2  = (cut2 == 0) ? Integer.MIN_VALUE : nums2[cut2 - 1];
        int right2 = (cut2 == n) ? Integer.MAX_VALUE : nums2[cut2];

        if (left1 <= right2 && left2 <= right1) {       // correct partition
            if (((m + n) & 1) == 1) {                   // odd total
                return Math.max(left1, left2);
            }
            return (Math.max(left1, left2) + Math.min(right1, right2)) / 2.0;
        } else if (left1 > right2) {
            right = cut1 - 1;    // took too many from nums1
        } else {
            left = cut1 + 1;     // took too few from nums1
        }
    }
    throw new IllegalArgumentException("Input arrays are not sorted.");
}
```

🔍 **Dry-run on `nums1 = [1,2]`, `nums2 = [3,4]`:**
- m=2, n=2, half=2. left=0, right=2, cut1=1, cut2=1.
- left1=1, right1=2, left2=3, right2=4. Check: 1 ≤ 4 ✅ and 3 ≤ 2? ❌ → left2 > right1 → left = cut1+1 = 2.
- cut1=2, cut2=0. left1=2, right1=MAX, left2=MIN, right2=3. Check: 2 ≤ 3 ✅ and MIN ≤ MAX ✅.
- Even total → (max(2, MIN) + min(MAX, 3))/2 = (2 + 3)/2 = **2.5** ✅

- ⏱️ Time: **O(log(min(m, n)))**, Space: **O(1)**.

> 🎓 **Honesty tip:** This is genuinely hard. If you blank on the partition logic, say so and offer the O(m+n) merge first — a working O(m+n) beats a broken O(log) attempt. Then *try* the partition approach out loud.

🧠 **Algorithm to Remember Forever**
- **Pattern:** Binary Search on the Partition.
- **Core idea:** Cut both arrays so the left side holds exactly half the elements AND `maxLeft ≤ minRight`.
- **Memory hook:** ✂️ A **guillotine** slicing both decks at once so the top halves total exactly half the cards and stay in order across the cut.
- **Trigger phrase:** *"median of two sorted arrays in O(log…)"*

---

## 1️⃣1️⃣ Find Peak Element (Medium) ⭐

📋 **Full Question**
A peak is an element strictly greater than its neighbors. Given `nums` where `nums[-1] = nums[n] = -∞`, return the index of **any** peak in **O(log n)**.
- Input: `nums = [1,2,3,1]` → Output: `2` (nums[2]=3 is a peak)
- Input: `nums = [1,2,1,3,5,6,4]` → Output: `5` (or `1` — any peak is fine)

🗣️ **What to say first**
- "Any peak is acceptable, not necessarily the global max, right?"
- "Neighbors outside the array count as -infinity, so the ends can be peaks?"
- "They want O(log n) — so I climb 'uphill' with binary search."

**Approach 1 — Brute Force**
Scan for any element greater than both neighbors.
```java
public int findPeakElementBrute(int[] nums) {
    for (int i = 0; i < nums.length; i++) {
        boolean leftOk  = (i == 0) || nums[i] > nums[i - 1];
        boolean rightOk = (i == nums.length - 1) || nums[i] > nums[i + 1];
        if (leftOk && rightOk) return i;
    }
    return -1; // unreachable given the problem guarantees a peak
}
```
- ⏱️ Time: **O(n)**, Space: **O(1)**.
- ❌ Why not enough: Linear; the interviewer wants O(log n).

**Approach 2 — Optimal (Binary Search toward the higher neighbor)**
If `nums[mid] < nums[mid+1]`, a peak must exist on the right; otherwise on the left (or at mid).

```java
public int findPeakElement(int[] nums) {
    int left = 0, right = nums.length - 1;
    while (left < right) {
        int mid = left + (right - left) / 2;
        if (nums[mid] < nums[mid + 1]) {
            left = mid + 1;   // uphill is to the right → peak is right
        } else {
            right = mid;      // mid could be the peak, or it's to the left
        }
    }
    return left;              // left == right → a peak
}
```

🔍 **Dry-run on `[1,2,1,3,5,6,4]`:**
- left=0, right=6, mid=3 → nums[3]=3 < nums[4]=5 → go right, left=4.
- left=4, right=6, mid=5 → nums[5]=6 > nums[6]=4 → go left, right=5.
- left=4, right=5, mid=4 → nums[4]=5 < nums[5]=6 → left=5.
- left==right=5 → peak index **5** ✅

- ⏱️ Time: **O(log n)**, Space: **O(1)**.

🧠 **Algorithm to Remember Forever**
- **Pattern:** Binary Search on an unsorted-but-structured array.
- **Core idea:** Always move toward the higher neighbor — uphill always leads to a peak.
- **Memory hook:** ⛰️ **Always walk uphill** — keep stepping toward higher ground and you must reach a summit.
- **Trigger phrase:** *"find a peak / local maximum in O(log n)"*

---

## 📊 Complexity Cheat Sheet

| Problem | Brute Force | Optimal | Technique |
|---------|-------------|---------|-----------|
| Longest Substring w/o Repeating | O(n³) | **O(n)** | Variable Sliding Window |
| Minimum Window Substring | O(n²·m) | **O(n+m)** | Sliding Window + need/have counters |
| Longest Repeating Char Replacement | O(n²) | **O(n)** | Sliding Window + maxFreq |
| Permutation in String | O(n·m log m) | **O(n)** | Fixed-Size Sliding Window |
| Maximum Average Subarray I | O(n·k) | **O(n)** | Fixed-Size Sliding Window |
| Fruit Into Baskets | O(n²) | **O(n)** | Sliding Window (≤ K distinct) |
| Search in Rotated Sorted Array | O(n) | **O(log n)** | Modified Binary Search |
| Koko Eating Bananas | O(maxPile·n) | **O(n·log maxPile)** | Binary Search on Answer |
| Find First and Last Position | O(n) | **O(log n)** | Binary Search (lower/upper bound) |
| Median of Two Sorted Arrays | O(m+n) | **O(log(min(m,n)))** | Binary Search on Partition |
| Find Peak Element | O(n) | **O(log n)** | Binary Search (walk uphill) |

---

## 🧠🧠 ALGORITHM RECOGNITION TABLE

> The single most useful table in this file. Learn to map **trigger words → algorithm → Big-O** instantly.

| If the question says… (Trigger) | Use this Algorithm | Memory Hook | Big-O |
|---------------------------------|--------------------|-------------|-------|
| "longest substring/subarray with [condition]" | Variable Sliding Window | 🐛 Inchworm crawl | O(n) |
| "smallest/minimum window containing all of…" | Sliding Window + need/have | 🎯 Camera zoom | O(n) |
| "at most K distinct/different" | Sliding Window (count map) | 🧺 Two baskets | O(n) |
| "contains a permutation/anagram of…" | Fixed-Size Sliding Window | 🚪 Conveyor belt | O(n) |
| "subarray of size k with max/min …" | Fixed-Size Sliding Window | 🧮 Rolling odometer | O(n) |
| "replace at most k characters" | Sliding Window + maxFreq | 🎨 k paint buckets | O(n) |
| "sorted array" + "find / O(log n)" | Binary Search | 📖 Open the dictionary middle | O(log n) |
| "sorted array that's rotated" | Modified Binary Search | 🃏 One pile stays in order | O(log n) |
| "first and last position / range" | Binary Search lower/upper bound | 📏 Two bookmarks | O(log n) |
| "minimum/maximum rate so that [condition]" | Binary Search on Answer | 🍌 Thermostat dial | O(n·log range) |
| "find a peak / local maximum" | Binary Search (uphill) | ⛰️ Walk uphill | O(log n) |
| "median of two sorted arrays in O(log…)" | Binary Search on Partition | ✂️ Guillotine cut | O(log(min(m,n))) |

---

## ☕ Java Tips for This Week

**1. Overflow-safe midpoint — ALWAYS.**
```java
int mid = left + (right - left) / 2;   // ✅ never overflows
// int mid = (left + right) / 2;       // ❌ overflows when left+right > Integer.MAX_VALUE
```
Microsoft interviewers *love* catching the naive `(left + right) / 2`. Use the safe form every time.

**2. Two binary-search loop shapes — know when to use which.**
```java
// Shape A: searching for an exact value (Binary Search, First/Last Position)
while (left <= right) {  ...  left = mid + 1;  right = mid - 1;  }

// Shape B: converging to a boundary (Find Peak, Koko, Binary Search on Answer)
while (left < right)  {  ...  left = mid + 1;  right = mid;  }   // note: right = mid, NOT mid - 1
```
In Shape B, returning `left` (== `right`) gives the boundary. Mixing `right = mid` with `<=` causes infinite loops — be deliberate.

**3. Ceil division without floating point.**
```java
long hours = (pile + k - 1) / k;   // ceil(pile / k) — used in Koko
```

**4. Frequency arrays beat HashMaps for fixed alphabets.**
```java
int[] count = new int[26];          // lowercase letters
int[] ascii = new int[128];         // general ASCII
count[c - 'a']++;                   // O(1), cache-friendly, no boxing
```
Use `int[26]` / `int[128]` for sliding-window counters when the alphabet is fixed — it's faster and cleaner than `HashMap<Character,Integer>`.

**5. `Map.merge` for clean count maps (variable alphabet).**
```java
count.merge(key, 1, Integer::sum);          // increment
count.merge(key, -1, Integer::sum);         // decrement
if (count.get(key) == 0) count.remove(key); // keep size == distinct count
```

**6. Watch sum overflow in window sums.** If values are large or k is big, accumulate in a `long`, not an `int`.

**7. The variable sliding-window skeleton (memorize it):**
```java
int left = 0, best = 0;
for (int right = 0; right < n; right++) {
    // 1. include arr[right] in the window
    while (/* window is invalid */) {
        // 2. remove arr[left], left++
    }
    // 3. update best with (right - left + 1)
}
```

---

## ✅ Final Interview Checklist

Before you say "I'm done," confirm:

- [ ] 🗣️ I **restated the problem** and gave a concrete example.
- [ ] ❓ I asked at least one **clarifying question** (input size, duplicates, empty input, char set).
- [ ] 🐌 I mentioned the **brute force** and its complexity (even if I didn't fully code it).
- [ ] 💡 I clearly stated the **key insight** before coding the optimal solution.
- [ ] ⌨️ I **narrated while coding** — the interviewer should never wonder what I'm thinking.
- [ ] 🔍 I did a **dry-run** on the example to prove correctness.
- [ ] 🧱 I checked **edge cases**: empty/null input, single element, target absent, all-same values.
- [ ] 🛡️ I used the **overflow-safe midpoint** `left + (right - left) / 2`.
- [ ] ⏱️ I stated **time AND space complexity** explicitly.
- [ ] 🤝 I stayed **collaborative** — "Does this approach sound good to you?" beats silent typing.

> 🌟 **Remember:** A calm, well-narrated O(n²) often beats a panicked, buggy O(n). Communicate, verify, then optimize. You've got this! 🚀
