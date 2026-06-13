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
> Given an array of integers `nums` and an integer `target`, return the **indices** of the two numbers that add up to `target`. Exactly one solution exists, and you can't use the same element twice.
>
> **Example:** `nums = [2, 7, 11, 15]`, `target = 9` → Output: `[0, 1]` (because `2 + 7 = 9`)

### 🗣️ What to say first
*"There's exactly one answer, and I can't reuse the same index, right? Can values be negative?"*

### Approach 1 — Brute Force
*"Check every pair of numbers."*
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
- **Space:** O(1)
- *"This works, but O(n²) is slow for large arrays. I can do better."*

### Approach 2 — Optimal (HashMap) ✅
*"Store what I've seen in a hash map. For each number, check if its complement (target − num) is already there — O(1) lookup."*
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
- **Time:** O(n), **Space:** O(n)

### 🧠 Algorithm to Remember Forever — **HashMap Lookup ("Complement Trick")**
> **Core idea:** Don't search for your partner — *remember everyone you've met*, then ask "have I seen `target − me`?"
> **Memory hook:** 🤝 *"I write down everyone I meet, so when my pair walks in, I already know them."*
> **Trigger phrase:** *"Find a pair / has-it-been-seen-before"* → **HashMap**, O(n).

---

# Problem 2: Best Time to Buy and Sell Stock

### 📋 Full Question
> `prices[i]` is the stock price on day `i`. Buy on one day, sell on a **later** day. Return the max profit (0 if none possible).
>
> **Example:** `prices = [7, 1, 5, 3, 6, 4]` → Output: `5` (buy at 1, sell at 6)

### 🗣️ What to say first
*"Sell must be after buy, right? And if prices only fall, I return 0?"*

### Approach 1 — Brute Force
*"Try every buy/sell pair."*
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
- *"O(n²) — I can do this in one pass."*

### Approach 2 — Optimal (Track the minimum so far) ✅
*"Remember the cheapest price seen so far. At each day, profit = today's price − cheapest so far."*
```java
public int maxProfit(int[] prices) {
    int minPrice = Integer.MAX_VALUE;
    int best = 0;
    for (int price : prices) {
        minPrice = Math.min(minPrice, price);    // cheapest buy day so far
        best = Math.max(best, price - minPrice);  // best sell today
    }
    return best;
}
```
**Dry run** `[7,1,5,3,6,4]`:
```
7→min7,best0  1→min1,best0  5→best4  3→best4  6→best5 ✅  4→best5
```
- **Time:** O(n), **Space:** O(1)

### 🧠 Algorithm to Remember Forever — **Running Min/Max (One-Pass Greedy)**
> **Core idea:** Walk left→right, keep *the best thing seen so far* in one variable.
> **Memory hook:** 📉 *"Buy at the lowest valley behind me, sell at today's peak."*
> **Trigger phrase:** *"Max profit / max difference where smaller comes first"* → **running min**, O(n).

---

# Problem 3: Contains Duplicate

### 📋 Full Question
> Return `true` if any value appears **at least twice**, else `false`.
>
> **Example:** `nums = [1, 2, 3, 1]` → Output: `true`. `nums = [1, 2, 3, 4]` → `false`

### 🗣️ What to say first
*"Just true/false, not the duplicate itself, correct? Can the array be empty?"*

### Approach 1 — Brute Force
*"Compare every pair."*
```java
public boolean containsDuplicate(int[] nums) {
    for (int i = 0; i < nums.length; i++) {
        for (int j = i + 1; j < nums.length; j++) {
            if (nums[i] == nums[j]) return true;
        }
    }
    return false;
}
```
- **Time:** O(n²), **Space:** O(1)

### Approach 2 — Optimal (HashSet) ✅
*"Add to a set as I go. If a value is already in the set, it's a duplicate."*
```java
public boolean containsDuplicate(int[] nums) {
    Set<Integer> seen = new HashSet<>();
    for (int num : nums) {
        if (!seen.add(num)) return true; // add() returns false if already present
    }
    return false;
}
```
**Dry run** `[1,2,3,1]`: add 1→{1}, add 2→{1,2}, add 3→{1,2,3}, add 1→already there → `true` ✅
- **Time:** O(n), **Space:** O(n)

### 🧠 Algorithm to Remember Forever — **HashSet "Seen Before"**
> **Core idea:** A set answers "have I seen this exact thing?" in O(1).
> **Memory hook:** 🎟️ *"Stamp each ticket; if a ticket's already stamped, it's a repeat."*
> **Trigger phrase:** *"Any duplicates / all unique?"* → **HashSet**, O(n).

---

# Problem 4: Maximum Subarray (Kadane's Algorithm) ⭐ Very Frequent

### 📋 Full Question
> Find the contiguous subarray with the **largest sum** and return that sum.
>
> **Example:** `nums = [-2, 1, -3, 4, -1, 2, 1, -5, 4]` → Output: `6` (subarray `[4, -1, 2, 1]`)

### 🗣️ What to say first
*"Can numbers be negative? Is the subarray non-empty (at least one element)?"*

### Approach 1 — Brute Force
*"Sum every possible subarray."*
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

### Approach 2 — Optimal (Kadane's) ✅
*"At each element, either extend the previous subarray or start fresh. If the running sum goes negative, drop it."*
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
**Dry run** start at -2; the running sum resets whenever it drops below the current element, peaking at `4-1+2+1 = 6` ✅
- **Time:** O(n), **Space:** O(1)

### 🧠 Algorithm to Remember Forever — **Kadane's Algorithm (DP)**
> **Core idea:** *"Does my past help me, or is it dead weight?"* → `current = max(num, current + num)`.
> **Memory hook:** 🎒 *"If my backpack weighs me down (negative sum), drop it and start with just this item."*
> **Trigger phrase:** *"Largest contiguous subarray sum"* → **Kadane's**, O(n).

---

# Problem 5: Move Zeroes

### 📋 Full Question
> Move all `0`s to the **end** while keeping the order of non-zero elements. Do it **in place**.
>
> **Example:** `nums = [0, 1, 0, 3, 12]` → `[1, 3, 12, 0, 0]`

### 🗣️ What to say first
*"In place, O(1) extra space, and the relative order of non-zeros must be preserved, right?"*

### Approach 1 — Brute Force (extra array)
*"Copy non-zeros into a new array, then pad with zeros."*
```java
public void moveZeroes(int[] nums) {
    int[] result = new int[nums.length];
    int idx = 0;
    for (int num : nums) {
        if (num != 0) result[idx++] = num;
    }
    // remaining slots are already 0 in a fresh int[]
    System.arraycopy(result, 0, nums, 0, nums.length);
}
```
- **Time:** O(n), **Space:** O(n)
- *"Works, but uses extra space. The follow-up wants O(1) space."*

### Approach 2 — Optimal (Two Pointers, in place) ✅
*"A `lastNonZero` pointer marks where the next non-zero goes. Swap each non-zero into place."*
```java
public void moveZeroes(int[] nums) {
    int lastNonZero = 0; // next slot for a non-zero value
    for (int i = 0; i < nums.length; i++) {
        if (nums[i] != 0) {
            int temp = nums[lastNonZero];
            nums[lastNonZero] = nums[i];
            nums[i] = temp;
            lastNonZero++;
        }
    }
}
```
**Dry run** `[0,1,0,3,12]`: swap brings non-zeros forward → `[1,3,12,0,0]` ✅
- **Time:** O(n), **Space:** O(1)

### 🧠 Algorithm to Remember Forever — **Two Pointers (Write Index / Partition)**
> **Core idea:** One "write" pointer marks where the next valid element belongs; the other scans.
> **Memory hook:** 🧹 *"Sweep the good stuff to the front; the dust (zeros) collects at the back."*
> **Trigger phrase:** *"Move / remove / partition elements in place, keep order"* → **slow/fast write pointer**, O(n).

---

# Problem 6: Plus One

### 📋 Full Question
> Given a number as a digit array (most-significant digit first), add **one** and return the resulting digit array.
>
> **Example:** `digits = [1, 2, 3]` → `[1, 2, 4]`; `digits = [9, 9]` → `[1, 0, 0]`

### 🗣️ What to say first
*"Each element is a single digit 0–9, and there are no leading zeros, right? I need to handle the carry case like 99 → 100."*

> Brute force here is essentially the same single pass as the optimal — there's no slower-but-simpler version worth showing. So I'll present the standard approach directly.

### Approach — Walk from the right, handle carry ✅
*"Start at the last digit. If it's < 9, just increment and return. If it's 9, set it to 0 and carry to the next. If we carry past the front, the number was all 9s — prepend a 1."*
```java
public int[] plusOne(int[] digits) {
    for (int i = digits.length - 1; i >= 0; i--) {
        if (digits[i] < 9) {
            digits[i]++;       // no carry needed, done
            return digits;
        }
        digits[i] = 0;         // 9 becomes 0, carry continues left
    }
    // every digit was 9 → need one extra slot, e.g. [9,9] -> [1,0,0]
    int[] result = new int[digits.length + 1];
    result[0] = 1;             // rest default to 0
    return result;
}
```
**Dry run** `[9,9]`: i=1 → 9→0; i=0 → 9→0; loop ends → new `[1,0,0]` ✅
- **Time:** O(n), **Space:** O(1) (O(n) only in the all-9s case for the new array)

### 🧠 Algorithm to Remember Forever — **Digit/Carry Propagation**
> **Core idea:** Add from the least-significant end; a carry stops the moment a digit isn't 9.
> **Memory hook:** ➕ *"Like grade-school addition — carry the 1 only when the column overflows."*
> **Trigger phrase:** *"Add to / increment a number stored as digits"* → **right-to-left carry**, O(n).

---

# Problem 7: Product of Array Except Self ⭐ Microsoft favorite

### 📋 Full Question
> Return `answer` where `answer[i]` = product of all elements **except** `nums[i]`. **No division**, run in **O(n)**.
>
> **Example:** `nums = [1, 2, 3, 4]` → Output: `[24, 12, 8, 6]`

### 🗣️ What to say first
*"No division — otherwise I'd just divide the total product. Can the array contain zeros? (Prefix/suffix handles that naturally.)"*

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
*"answer[i] = (product to the left) × (product to the right). Bank left products in one pass, multiply by right products in a second pass."*
```java
public int[] productExceptSelf(int[] nums) {
    int n = nums.length;
    int[] res = new int[n];
    int left = 1;
    for (int i = 0; i < n; i++) {
        res[i] = left;     // product of everything left of i
        left *= nums[i];
    }
    int right = 1;
    for (int i = n - 1; i >= 0; i--) {
        res[i] *= right;   // multiply in product of everything right of i
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
- **Time:** O(n), **Space:** O(1) (output array doesn't count)

### 🧠 Algorithm to Remember Forever — **Prefix / Suffix Products**
> **Core idea:** "Except myself" = (everything left) × (everything right).
> **Memory hook:** 🔦 *"Shine a light from the left, then from the right — every spot is lit except its own shadow."*
> **Trigger phrase:** *"Compute per index from all OTHER elements / no division"* → **prefix + suffix**, O(n).

---

# Problem 8: 3Sum

### 📋 Full Question
> Find all **unique triplets** `[a, b, c]` such that `a + b + c == 0`.
>
> **Example:** `nums = [-1, 0, 1, 2, -1, -4]` → `[[-1, -1, 2], [-1, 0, 1]]`

### 🗣️ What to say first
*"Triplets must be unique (no duplicate sets), order within a triplet doesn't matter, right?"*

### Approach 1 — Brute Force
*"Three nested loops, dedupe with a set."*
```java
public List<List<Integer>> threeSum(int[] nums) {
    Set<List<Integer>> set = new HashSet<>();
    int n = nums.length;
    Arrays.sort(nums); // sorting each triplet lets the set dedupe
    for (int i = 0; i < n; i++)
        for (int j = i + 1; j < n; j++)
            for (int k = j + 1; k < n; k++)
                if (nums[i] + nums[j] + nums[k] == 0)
                    set.add(Arrays.asList(nums[i], nums[j], nums[k]));
    return new ArrayList<>(set);
}
```
- **Time:** O(n³), **Space:** O(n) for results
- *"O(n³) is too slow. Sorting plus two pointers gets it to O(n²)."*

### Approach 2 — Optimal (Sort + Two Pointers) ✅
*"Sort the array. Fix one number, then use two pointers from both ends of the rest to find pairs summing to its negative. Skip duplicates to keep triplets unique."*
```java
public List<List<Integer>> threeSum(int[] nums) {
    Arrays.sort(nums);
    List<List<Integer>> res = new ArrayList<>();
    for (int i = 0; i < nums.length - 2; i++) {
        if (i > 0 && nums[i] == nums[i - 1]) continue;       // skip dup anchor
        int lo = i + 1, hi = nums.length - 1;
        while (lo < hi) {
            int sum = nums[i] + nums[lo] + nums[hi];
            if (sum < 0) {
                lo++;
            } else if (sum > 0) {
                hi--;
            } else {
                res.add(Arrays.asList(nums[i], nums[lo], nums[hi]));
                while (lo < hi && nums[lo] == nums[lo + 1]) lo++; // skip dup
                while (lo < hi && nums[hi] == nums[hi - 1]) hi--; // skip dup
                lo++;
                hi--;
            }
        }
    }
    return res;
}
```
**Dry run** sorted `[-4,-1,-1,0,1,2]`: anchor -1 → two pointers find (-1,2) and (0,1) → `[-1,-1,2]`,`[-1,0,1]` ✅
- **Time:** O(n²), **Space:** O(1) extra (ignoring output / sort)

### 🧠 Algorithm to Remember Forever — **Sort + Two Pointers (fix one, scan rest)**
> **Core idea:** Reduce a 3-number search to many 2-number searches; sorted order lets two pointers converge.
> **Memory hook:** 📌 *"Pin one nail, then squeeze the two ends of a rope toward zero."*
> **Trigger phrase:** *"Triplets / k-sum / pairs in sorted data"* → **sort + two pointers**, O(n²).

---

# Problem 9: Container With Most Water

### 📋 Full Question
> Each `height[i]` is a vertical line at position `i`. Pick two lines forming a container that holds the **most water**. Return the max area.
>
> **Example:** `height = [1, 8, 6, 2, 5, 4, 8, 3, 7]` → Output: `49`

### 🗣️ What to say first
*"Area = width × the shorter of the two heights, right? Lines don't slant, so water can't spill over the shorter wall."*

### Approach 1 — Brute Force
*"Try every pair of lines."*
```java
public int maxArea(int[] height) {
    int best = 0;
    for (int i = 0; i < height.length; i++) {
        for (int j = i + 1; j < height.length; j++) {
            int area = (j - i) * Math.min(height[i], height[j]);
            best = Math.max(best, area);
        }
    }
    return best;
}
```
- **Time:** O(n²), **Space:** O(1)

### Approach 2 — Optimal (Two Pointers, converge) ✅
*"Start wide — one pointer at each end. The area is capped by the shorter wall, so move that pointer inward (moving the taller one can never help)."*
```java
public int maxArea(int[] height) {
    int lo = 0, hi = height.length - 1, best = 0;
    while (lo < hi) {
        int area = (hi - lo) * Math.min(height[lo], height[hi]);
        best = Math.max(best, area);
        if (height[lo] < height[hi]) {
            lo++;   // shorter wall limits us — try to find a taller one
        } else {
            hi--;
        }
    }
    return best;
}
```
**Dry run** `[1,8,6,2,5,4,8,3,7]`: widest span and tall walls 8 & 7 give `7 × 7 = 49` ✅
- **Time:** O(n), **Space:** O(1)

### 🧠 Algorithm to Remember Forever — **Converging Two Pointers (Greedy)**
> **Core idea:** Start at the widest gap; always move the pointer at the **shorter** wall, because it's the bottleneck.
> **Memory hook:** 🪣 *"Water spills over the shorter wall — so go raise the shorter side."*
> **Trigger phrase:** *"Max area / two lines / widest container"* → **two pointers from the ends**, O(n).

---

# Problem 10: Rotate Array

### 📋 Full Question
> Rotate the array to the **right** by `k` steps, in place.
>
> **Example:** `nums = [1,2,3,4,5,6,7]`, `k = 3` → `[5,6,7,1,2,3,4]`

### 🗣️ What to say first
*"Rotate right, and `k` can be larger than the array length, right? So I should take `k % n`."*

### Approach 1 — Brute Force (extra array)
*"Place each element at `(i + k) % n` in a copy."*
```java
public void rotate(int[] nums, int k) {
    int n = nums.length;
    k %= n;
    int[] result = new int[n];
    for (int i = 0; i < n; i++) {
        result[(i + k) % n] = nums[i];
    }
    System.arraycopy(result, 0, nums, 0, n);
}
```
- **Time:** O(n), **Space:** O(n)
- *"Works but uses O(n) extra space. The follow-up wants O(1)."*

### Approach 2 — Optimal (Reverse Three Times, in place) ✅
*"Reverse the whole array, then reverse the first `k`, then reverse the rest. The two halves snap into rotated order."*
```java
public void rotate(int[] nums, int k) {
    int n = nums.length;
    k %= n;
    reverse(nums, 0, n - 1);   // whole array
    reverse(nums, 0, k - 1);   // first k
    reverse(nums, k, n - 1);   // remaining
}

private void reverse(int[] a, int i, int j) {
    while (i < j) {
        int t = a[i]; a[i] = a[j]; a[j] = t;
        i++; j--;
    }
}
```
**Dry run** `[1..7]`, k=3: reverse all → `[7,6,5,4,3,2,1]`; reverse first 3 → `[5,6,7,4,3,2,1]`; reverse rest → `[5,6,7,1,2,3,4]` ✅
- **Time:** O(n), **Space:** O(1)

### 🧠 Algorithm to Remember Forever — **Triple Reverse (In-Place Rotation)**
> **Core idea:** Reverse all, reverse the two parts — the boundary at `k` flips them into place.
> **Memory hook:** 🔄 *"Flip the whole pancake, then flip each half back."*
> **Trigger phrase:** *"Rotate array in place, O(1) space"* → **reverse 3 times** (don't forget `k %= n`), O(n).

---

# Problem 11: Find Minimum in Rotated Sorted Array

### 📋 Full Question
> A sorted array was rotated at some pivot. Find the **minimum** element in **O(log n)**. No duplicates.
>
> **Example:** `nums = [4, 5, 6, 7, 0, 1, 2]` → Output: `0`

### 🗣️ What to say first
*"It's a sorted array rotated once, all elements distinct, and they want O(log n) — so binary search, right?"*

### Approach 1 — Brute Force
*"Linear scan for the smallest."*
```java
public int findMin(int[] nums) {
    int min = nums[0];
    for (int num : nums) min = Math.min(min, num);
    return min;
}
```
- **Time:** O(n), **Space:** O(1)
- *"This ignores the sorted structure. Binary search gives O(log n)."*

### Approach 2 — Optimal (Binary Search) ✅
*"Compare `nums[mid]` to `nums[hi]`. If `nums[mid] > nums[hi]`, the rotation point (and the min) is to the **right**; else it's at mid or to the **left**."*
```java
public int findMin(int[] nums) {
    int lo = 0, hi = nums.length - 1;
    while (lo < hi) {
        int mid = lo + (hi - lo) / 2; // avoids overflow
        if (nums[mid] > nums[hi]) {
            lo = mid + 1;   // min is in the right half
        } else {
            hi = mid;       // min is at mid or in the left half
        }
    }
    return nums[lo]; // lo == hi points at the minimum
}
```
**Dry run** `[4,5,6,7,0,1,2]`: mid=7 > hi=2 → go right; narrows down to index 4 → `0` ✅
- **Time:** O(log n), **Space:** O(1)

### 🧠 Algorithm to Remember Forever — **Binary Search on Rotation (compare mid vs. hi)**
> **Core idea:** The half that's "out of order" (mid > hi) contains the wrap-around minimum.
> **Memory hook:** 🌀 *"Follow the cliff edge — the min is wherever the values suddenly drop."*
> **Trigger phrase:** *"Rotated sorted array / O(log n)"* → **binary search comparing mid to hi**, O(log n).

---

# Problem 12: Merge Intervals ⭐ Very Frequent

### 📋 Full Question
> Given intervals `[start, end]`, **merge all overlapping** ones and return the result.
>
> **Example:** `intervals = [[1,3],[2,6],[8,10],[15,18]]` → `[[1,6],[8,10],[15,18]]`

### 🗣️ What to say first
*"Do touching intervals like [1,2] and [2,3] count as overlapping? (Usually yes.) Is the input sorted? (Usually not — I'll sort first.)"*

> There's no meaningfully simpler "brute force" worth showing — the sort-then-sweep approach IS the standard answer, so I'll present it directly.

### Approach — Sort by start, then sweep ✅
*"Sort intervals by start time. Walk through them: if the current interval overlaps the last merged one (its start ≤ the last end), extend the last end; otherwise append it as a new interval."*
```java
public int[][] merge(int[][] intervals) {
    Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));
    List<int[]> merged = new ArrayList<>();
    for (int[] interval : intervals) {
        // no overlap if list empty or current start is beyond last end
        if (merged.isEmpty() || interval[0] > merged.get(merged.size() - 1)[1]) {
            merged.add(interval);
        } else {
            // overlap → extend the end of the last interval
            merged.get(merged.size() - 1)[1] =
                Math.max(merged.get(merged.size() - 1)[1], interval[1]);
        }
    }
    return merged.toArray(new int[merged.size()][]);
}
```
**Dry run** sorted `[[1,3],[2,6],[8,10],[15,18]]`: [1,3] in; [2,6] overlaps → [1,6]; [8,10] separate; [15,18] separate → `[[1,6],[8,10],[15,18]]` ✅
- **Time:** O(n log n) (dominated by the sort), **Space:** O(n) for output

### 🧠 Algorithm to Remember Forever — **Sort + Sweep Merge**
> **Core idea:** Sort by start, then a single sweep merges anything that overlaps the running interval.
> **Memory hook:** 📅 *"Line up the meetings by start time; glue together any that bump into each other."*
> **Trigger phrase:** *"Overlapping intervals / ranges / merge schedules"* → **sort by start, sweep**, O(n log n).

---

# Problem 13: Valid Palindrome

### 📋 Full Question
> Return `true` if a string is a palindrome, considering **only alphanumeric** characters and **ignoring case**.
>
> **Example:** `"A man, a plan, a canal: Panama"` → `true`; `"race a car"` → `false`

### 🗣️ What to say first
*"I ignore spaces and punctuation, and compare case-insensitively, right? Is an empty string a valid palindrome? (Usually yes.)"*

### Approach 1 — Brute Force (clean + reverse)
*"Strip non-alphanumerics, lowercase, and compare to the reverse."*
```java
public boolean isPalindrome(String s) {
    StringBuilder sb = new StringBuilder();
    for (char c : s.toCharArray()) {
        if (Character.isLetterOrDigit(c)) {
            sb.append(Character.toLowerCase(c));
        }
    }
    String forward = sb.toString();
    String backward = sb.reverse().toString();
    return forward.equals(backward);
}
```
- **Time:** O(n), **Space:** O(n) (extra string)
- *"Clean, but uses O(n) extra space. Two pointers avoid the copy."*

### Approach 2 — Optimal (Two Pointers, in place) ✅
*"One pointer from each end. Skip non-alphanumerics, compare lowercased characters as I move inward."*
```java
public boolean isPalindrome(String s) {
    int lo = 0, hi = s.length() - 1;
    while (lo < hi) {
        while (lo < hi && !Character.isLetterOrDigit(s.charAt(lo))) lo++;
        while (lo < hi && !Character.isLetterOrDigit(s.charAt(hi))) hi--;
        if (Character.toLowerCase(s.charAt(lo)) != Character.toLowerCase(s.charAt(hi))) {
            return false;
        }
        lo++;
        hi--;
    }
    return true;
}
```
**Dry run** `"A man...Panama"`: pointers skip punctuation/spaces and every matched pair agrees → `true` ✅
- **Time:** O(n), **Space:** O(1)

### 🧠 Algorithm to Remember Forever — **Two Pointers from the Ends**
> **Core idea:** Walk inward from both ends, skipping junk, comparing as you go.
> **Memory hook:** 👉👈 *"Two readers start at opposite covers and meet in the middle — they must read the same letters."*
> **Trigger phrase:** *"Palindrome / compare from both ends"* → **two pointers**, O(n).

---

# Problem 14: Valid Anagram

### 📋 Full Question
> Return `true` if `t` is an anagram of `s` (same characters, same counts, different order).
>
> **Example:** `s = "anagram"`, `t = "nagaram"` → `true`; `s = "rat"`, `t = "car"` → `false`

### 🗣️ What to say first
*"Are both strings lowercase English letters only, or could there be Unicode? (Affects whether I use `int[26]` or a HashMap.)"*

### Approach 1 — Sort & Compare
*"Anagrams have identical sorted forms."*
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
- **Time:** O(n log n), **Space:** O(n)

### Approach 2 — Optimal (Count characters) ✅
*"Count each character: `+1` for `s`, `−1` for `t`. If every count ends at 0, they match."*
```java
public boolean isAnagram(String s, String t) {
    if (s.length() != t.length()) return false;
    int[] count = new int[26];
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
**Dry run** `"anagram"`/`"nagaram"`: every letter cancels to 0 → `true` ✅
- **Time:** O(n), **Space:** O(1) (fixed 26-letter alphabet)

### 🧠 Algorithm to Remember Forever — **Frequency Count**
> **Core idea:** Tally letters instead of comparing order: `+1` for A, `−1` for B; all zeros = equal.
> **Memory hook:** 🗳️ *"Count the votes — same letters in, same counts out."*
> **Trigger phrase:** *"Anagram / same characters / how many times"* → **`int[26]` count**, O(n) beats sorting's O(n log n).

---

# Problem 15: Longest Substring Without Repeating Characters ⭐ Very Frequent

### 📋 Full Question
> Find the length of the **longest substring** with no repeating characters.
>
> **Example:** `s = "abcabcbb"` → Output: `3` (the substring `"abc"`)

### 🗣️ What to say first
*"I return the length, not the substring itself, right? Substring means contiguous (not subsequence)?"*

### Approach 1 — Brute Force
*"Check every substring for all-unique characters."*
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
*"Keep a window `[left, right]` of unique chars. When `s[right]` repeats inside the window, jump `left` past the duplicate's last position."*
```java
public int lengthOfLongestSubstring(String s) {
    Map<Character, Integer> lastSeen = new HashMap<>(); // char -> last index
    int left = 0, best = 0;
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
**Dry run** `"abcabcbb"`: window grows to `"abc"`(3); the second `a` jumps `left`, window stays size 3 → `3` ✅
- **Time:** O(n), **Space:** O(min(n, alphabet))

### 🧠 Algorithm to Remember Forever — **Sliding Window**
> **Core idea:** Grow the window from the right; when it breaks the rule, shrink from the left until valid.
> **Memory hook:** 🪟 *"Open the window wider until you smell smoke (duplicate), then slide the front up until the air is clean."*
> **Trigger phrase:** *"Longest/shortest substring or subarray with a condition"* → **Sliding Window**, O(n).

---

# Problem 16: String to Integer (atoi) ⭐ Microsoft Classic

### 📋 Full Question
> Convert a string to a 32-bit signed integer (like C's `atoi`): skip leading whitespace, read an optional `+`/`−`, read digits until a non-digit, and **clamp** to `[-2³¹, 2³¹−1]` on overflow.
>
> **Example:** `"   -42"` → `-42`; `"4193 with words"` → `4193`; `"words 987"` → `0`; `"-91283472332"` → `-2147483648` (clamped)

### 🗣️ What to say first
*"Lots of edge cases — let me confirm the rules: skip leading spaces, one optional sign, stop at the first non-digit, and clamp on overflow to INT_MIN/INT_MAX, right?"*

> This one is about careful edge-case handling, not a brute-vs-optimal trade-off — there's a single correct linear pass, so I'll present it directly.

### Approach — Careful single pass ✅
*"Four phases: (1) skip spaces, (2) read sign, (3) read digits while watching for overflow, (4) apply sign. I detect overflow **before** it happens by comparing against INT_MAX/10."*
```java
public int myAtoi(String s) {
    int i = 0, n = s.length();
    // 1) skip leading whitespace
    while (i < n && s.charAt(i) == ' ') i++;
    if (i == n) return 0;

    // 2) optional sign
    int sign = 1;
    if (s.charAt(i) == '+' || s.charAt(i) == '-') {
        if (s.charAt(i) == '-') sign = -1;
        i++;
    }

    // 3) read digits with overflow clamping
    int result = 0;
    while (i < n && Character.isDigit(s.charAt(i))) {
        int digit = s.charAt(i) - '0';
        // would result*10 + digit overflow a 32-bit int?
        if (result > Integer.MAX_VALUE / 10 ||
            (result == Integer.MAX_VALUE / 10 && digit > 7)) { // 2147483647 ends in 7
            return sign == 1 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }
        result = result * 10 + digit;
        i++;
    }
    // 4) apply sign
    return result * sign;
}
```
**Dry run** `"   -42"`: skip 3 spaces → sign `-` → read 4,2 → result 42 → `-42` ✅
**Dry run** `"-91283472332"`: digits overflow the 32-bit limit → clamp to `-2147483648` ✅
- **Time:** O(n), **Space:** O(1)

### 🧠 Algorithm to Remember Forever — **Parse-with-State + Overflow Guard**
> **Core idea:** March through phases (spaces → sign → digits) and check overflow *before* multiplying, not after.
> **Memory hook:** 🚦 *"Read it like a careful cashier: ignore the spaces, note the sign, ring up digits — and refuse the sale if the total blows the register limit."*
> **Trigger phrase:** *"Parse a number / atoi / handle overflow"* → **phased scan + `> MAX/10` guard**, O(n).

---

# Problem 17: Reverse Words in a String

### 📋 Full Question
> Reverse the **order of words** in a string. Trim leading/trailing spaces and collapse multiple spaces to one.
>
> **Example:** `"  the sky  is blue  "` → `"blue is sky the"`

### 🗣️ What to say first
*"Reverse word order (not the letters within words), and I should collapse extra spaces and trim, right?"*

### Approach 1 — Brute Force (built-ins)
*"Split on whitespace, reverse the list, join with single spaces."*
```java
public String reverseWords(String s) {
    String[] words = s.trim().split("\\s+"); // split on runs of whitespace
    Collections.reverse(Arrays.asList(words));
    return String.join(" ", words);
}
```
- **Time:** O(n), **Space:** O(n)
- *"Clean and correct. Some interviewers ask to avoid built-in split — here's a manual version."*

### Approach 2 — Manual two-pointer scan ✅
*"Walk from the end, collecting each word into a StringBuilder. Skip spaces between words."*
```java
public String reverseWords(String s) {
    StringBuilder sb = new StringBuilder();
    int i = s.length() - 1;
    while (i >= 0) {
        while (i >= 0 && s.charAt(i) == ' ') i--;        // skip trailing spaces
        if (i < 0) break;
        int end = i;                                     // word end (inclusive)
        while (i >= 0 && s.charAt(i) != ' ') i--;        // find word start
        if (sb.length() > 0) sb.append(' ');             // separator between words
        sb.append(s, i + 1, end + 1);                    // append this word
    }
    return sb.toString();
}
```
**Dry run** `"  the sky  is blue  "`: scanning from the right collects `blue`, `is`, `sky`, `the` → `"blue is sky the"` ✅
- **Time:** O(n), **Space:** O(n) (output)

### 🧠 Algorithm to Remember Forever — **Scan from the End / Word Boundaries**
> **Core idea:** Walk backward, grabbing whole words, skipping the spaces between them.
> **Memory hook:** 🔁 *"Read the sentence backward word by word, like rewinding a tape."*
> **Trigger phrase:** *"Reverse words / tokens, keep words intact"* → **split + reverse, or back-scan boundaries**, O(n).

---

# Problem 18: Group Anagrams

### 📋 Full Question
> Group words that are anagrams of one another.
>
> **Example:** `["eat","tea","tan","ate","nat","bat"]` → `[["eat","tea","ate"], ["tan","nat"], ["bat"]]`

### 🗣️ What to say first
*"All lowercase letters? Order of groups (and within groups) doesn't matter, right?"*

### Approach 1 — Brute Force (compare every pair)
*"For each word, scan existing groups and test anagram-ness against a representative."*
```java
public List<List<String>> groupAnagrams(String[] strs) {
    List<List<String>> groups = new ArrayList<>();
    for (String word : strs) {
        boolean placed = false;
        for (List<String> group : groups) {
            if (isAnagram(word, group.get(0))) { // compare to group's first word
                group.add(word);
                placed = true;
                break;
            }
        }
        if (!placed) {
            List<String> newGroup = new ArrayList<>();
            newGroup.add(word);
            groups.add(newGroup);
        }
    }
    return groups;
}

private boolean isAnagram(String a, String b) {
    if (a.length() != b.length()) return false;
    int[] count = new int[26];
    for (int i = 0; i < a.length(); i++) {
        count[a.charAt(i) - 'a']++;
        count[b.charAt(i) - 'a']--;
    }
    for (int c : count) if (c != 0) return false;
    return true;
}
```
- **Time:** O(n · g · k) where g = groups, k = word length — up to O(n²·k), **Space:** O(n·k)
- *"Comparing against every group is wasteful. A hash key collapses each anagram family to one bucket."*

### Approach 2 — Optimal (Sorted-string key) ✅
*"Anagrams share the same sorted letters. Use the sorted word as a HashMap key; every anagram lands in the same bucket."*
```java
public List<List<String>> groupAnagrams(String[] strs) {
    Map<String, List<String>> map = new HashMap<>();
    for (String word : strs) {
        char[] chars = word.toCharArray();
        Arrays.sort(chars);
        String key = new String(chars);                 // canonical signature
        map.computeIfAbsent(key, k -> new ArrayList<>()).add(word);
    }
    return new ArrayList<>(map.values());
}
```
**Dry run** `"eat","tea","ate"` all sort to `"aet"` → same bucket; `"tan","nat"` → `"ant"`; `"bat"` → `"abt"` ✅
- **Time:** O(n · k log k) (k = word length, for sorting each word), **Space:** O(n·k)
- 💡 *"Could use a 26-count signature instead of sorting to get O(n·k) — a nice optimization to mention."*

### 🧠 Algorithm to Remember Forever — **Canonical Key Bucketing (HashMap of Groups)**
> **Core idea:** Map each item to a canonical signature (sorted letters); equal signatures fall in the same bucket.
> **Memory hook:** 🗂️ *"File every word under its 'sorted' name — all spellings of the same letters share one folder."*
> **Trigger phrase:** *"Group / bucket items that are 'the same' under some transform"* → **HashMap keyed by canonical form**, O(n·k log k).

---

## 📊 Complexity Cheat Sheet — All 18 Problems

| # | Problem | Brute Force | Optimal | Technique |
|---|---------|------------|---------|-----------|
| 1 | Two Sum | O(n²) | **O(n)** | HashMap (complement) |
| 2 | Buy & Sell Stock | O(n²) | **O(n)** | Running min |
| 3 | Contains Duplicate | O(n²) | **O(n)** | HashSet |
| 4 | Maximum Subarray | O(n²) | **O(n)** | Kadane's |
| 5 | Move Zeroes | O(n) / O(n) space | **O(n) / O(1)** | Two pointers (write index) |
| 6 | Plus One | — | **O(n)** | Digit carry |
| 7 | Product Except Self | O(n²) | **O(n)** | Prefix / Suffix |
| 8 | 3Sum | O(n³) | **O(n²)** | Sort + two pointers |
| 9 | Container With Most Water | O(n²) | **O(n)** | Converging two pointers |
| 10 | Rotate Array | O(n) / O(n) space | **O(n) / O(1)** | Triple reverse |
| 11 | Min in Rotated Sorted | O(n) | **O(log n)** | Binary search |
| 12 | Merge Intervals | — | **O(n log n)** | Sort + sweep |
| 13 | Valid Palindrome | O(n) / O(n) space | **O(n) / O(1)** | Two pointers |
| 14 | Valid Anagram | O(n log n) | **O(n)** | Char count |
| 15 | Longest Substring | O(n²) | **O(n)** | Sliding window |
| 16 | String to Integer (atoi) | — | **O(n)** | Phased parse + overflow guard |
| 17 | Reverse Words | O(n) | **O(n)** | Split/reverse or back-scan |
| 18 | Group Anagrams | O(n²·k) | **O(n·k log k)** | Canonical-key bucketing |

> **Notice the pattern:** almost every "O(n²) → O(n)" jump comes from
> **a HashMap, Two Pointers, or a Sliding Window.** When stuck, ask:
> *"Can a hash map remember what I've seen? Can two pointers replace the inner loop?"*

---

## 🧠🧠 ALGORITHM RECOGNITION TABLE — Remember This Forever

> The *most important* table. If you can look at a question and instantly name the
> algorithm, half the battle is won. **Hear the trigger words → fire the algorithm.**

| If the question says... (Trigger) | Use this Algorithm | Memory Hook | Big-O |
|-----------------------------------|--------------------|-------------|-------|
| "Find a **pair** / seen X before?" | **HashMap Lookup** | 🤝 Write down everyone you meet | O(n) |
| "Any **duplicates** / all unique?" | **HashSet** | 🎟️ Stamp each ticket | O(n) |
| "Max profit / max difference (smaller first)" | **Running Min/Max** | 📉 Buy at the lowest valley behind you | O(n) |
| "Largest **contiguous** subarray sum" | **Kadane's** | 🎒 Drop the backpack if it weighs you down | O(n) |
| "Move / remove / partition **in place**, keep order" | **Two Pointers (write index)** | 🧹 Sweep good stuff to the front | O(n) |
| "Add to a number stored as **digits**" | **Carry Propagation** | ➕ Carry the 1 when a column overflows | O(n) |
| "Value at each index from **all others**, no division" | **Prefix / Suffix Products** | 🔦 Light from left, then right | O(n) |
| "**Triplets / k-sum** in (sortable) array" | **Sort + Two Pointers** | 📌 Pin one nail, squeeze the rope | O(n²) |
| "Max **area** / widest container / two lines" | **Converging Two Pointers** | 🪣 Water spills over the shorter wall | O(n) |
| "**Rotate** array in place, O(1) space" | **Triple Reverse** | 🔄 Flip the whole pancake, then each half | O(n) |
| "**Rotated sorted** array / O(log n)" | **Binary Search (mid vs hi)** | 🌀 Follow the cliff edge | O(log n) |
| "Overlapping **intervals / ranges**" | **Sort + Sweep** | 📅 Glue meetings that bump | O(n log n) |
| "**Palindrome** / compare from both ends" | **Two Pointers (ends)** | 👉👈 Readers meet in the middle | O(n) |
| "**Anagram** / same chars / how many times" | **Frequency Count** | 🗳️ Count the votes | O(n) |
| "**Longest/shortest** substring with a condition" | **Sliding Window** | 🪟 Open till smoke, slide front till clean | O(n) |
| "Parse a **number / atoi**, handle overflow" | **Phased Parse + Guard** | 🚦 Careful cashier refuses an over-limit sale | O(n) |
| "Reverse **words / tokens**" | **Split + Reverse / Back-scan** | 🔁 Rewind the tape word by word | O(n) |
| "**Group / bucket** items 'the same' under a transform" | **Canonical-Key HashMap** | 🗂️ File under the sorted name | O(n·k log k) |

### The 3 Questions to Ask Yourself on ANY Array/String Problem
1. **"Can a HashMap remember what I've already seen?"** → kills most O(n²) search loops.
2. **"Can two pointers replace my inner loop?"** → sorted arrays, palindromes, pairs, in-place moves.
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

## ☕ Java Tips for the Whiteboard (Arrays & Strings)

- **HashMap:** `map.containsKey(k)`, `map.get(k)`, `map.put(k, v)`, `map.getOrDefault(k, 0)`, `map.computeIfAbsent(k, x -> new ArrayList<>())`
- **HashSet:** `set.contains(x)`, `set.add(x)` (returns `false` if already present — handy for duplicate checks)
- **Char to index:** `s.charAt(i) - 'a'` gives 0–25 for lowercase letters; `Character.isLetterOrDigit(c)`, `Character.isDigit(c)`, `Character.toLowerCase(c)`
- **Sorting:** `Arrays.sort(arr)`; for 2D, `Arrays.sort(arr, (a,b) -> Integer.compare(a[0], b[0]))`; sort chars via `char[] c = s.toCharArray(); Arrays.sort(c); new String(c)`
- **Array compare / fill / copy:** `Arrays.equals(a, b)`, `Arrays.fill(arr, 0)`, `System.arraycopy(src, 0, dst, 0, n)`
- **Min/Max:** `Math.min(a, b)`, `Math.max(a, b)`; guards: `Integer.MAX_VALUE`, `Integer.MIN_VALUE`
- **Overflow-safe mid:** `int mid = lo + (hi - lo) / 2;` (avoids `(lo+hi)` overflow)
- **Edge guards:** `if (nums == null || nums.length == 0) return ...;`
- **Return an int array:** `return new int[] { i, j };`
- **2D result:** `list.toArray(new int[list.size()][]);`
- **StringBuilder** for building strings (avoid `+` in loops — it's O(n²)): `sb.append(c)`, `sb.append(s, start, end)`, `sb.reverse()`, `sb.toString()`
- **Split / join:** `s.trim().split("\\s+")` splits on whitespace runs; `String.join(" ", words)`

---

## ✅ Final Interview Checklist (do this every single time)

1. ☐ **Repeat the question** back in your own words
2. ☐ **Ask clarifying questions** (empty? null? duplicates? negatives? sorted? overflow?)
3. ☐ **State brute force first** + its complexity — *never skip this*
4. ☐ **Optimize** — explain the key insight ("a HashMap lets me look up in O(1)")
5. ☐ **Code the optimal solution** cleanly
6. ☐ **Dry-run a small example** out loud
7. ☐ **Check edge cases** (empty, null, one element, all same, negatives, overflow)
8. ☐ **State final Time & Space complexity**
9. ☐ Ask: *"Would you like me to optimize space further or handle any other cases?"*

> 💬 **Remember:** Microsoft cares *how* you think more than getting the perfect answer.
> Talking through Brute → Optimal → Best out loud is exactly what they're scoring you on.
