# Week 3: Sliding Window & Binary Search - Deep Concept Guide

---

## PART 1: SLIDING WINDOW

---

### 1. What is the Sliding Window Technique?

**Plain English:**
Instead of looking at every possible subarray (which is slow), maintain a
"window" that slides across the array. As the window moves, add the new
element entering from the right and remove the element leaving from the left.
You never recompute the entire window from scratch.

**Real-Life Analogy:**
Imagine you are sitting in a TRAIN looking through the window. As the train
moves forward:
- New scenery ENTERS from the right side of your window
- Old scenery EXITS from the left side of your window
- At any moment, you see a FIXED-WIDTH view of the landscape
- You never need to look at the ENTIRE route -- just what is in your window
  right now

```
Train moving ->

  Window at position 1:     Window at position 2:     Window at position 3:
  +-----------+             +-----------+             +-----------+
  | 2 | 1 | 5 |            | 1 | 5 | 1 |            | 5 | 1 | 3 |
  +-----------+             +-----------+             +-----------+
   ^           ^             ^           ^             ^           ^
   |           |             |           |             |           |
  left       right          left       right          left       right

Array: [2, 1, 5, 1, 3, 2]
         -------             -------               -------
```

---

### 2. Why Does Sliding Window Exist?

**The Problem:**
Many problems ask: "Find the best subarray of size K" or "Find the
longest/shortest subarray with some property."

**Without Sliding Window (Brute Force):**
Check every subarray of size K. For each starting position, sum up K elements.

```
Array: [2, 1, 5, 1, 3, 2],  K = 3

Subarray [2,1,5]: sum = 2+1+5 = 8
Subarray [1,5,1]: sum = 1+5+1 = 7    <- recomputing 5+1 again!
Subarray [5,1,3]: sum = 5+1+3 = 9    <- recomputing 1+3 again!
Subarray [1,3,2]: sum = 1+3+2 = 6

Each subarray: O(K) to sum
Total subarrays: O(n)
Total: O(n x K)
```

**With Sliding Window:**
Instead of recomputing the entire sum, just subtract the element leaving
and add the element entering.

```
Window [2,1,5]: sum = 8
Slide: sum = 8 - 2 + 1 = 7    (remove 2, add 1)  -> [1,5,1]
Slide: sum = 7 - 1 + 3 = 9    (remove 1, add 3)  -> [5,1,3]
Slide: sum = 9 - 5 + 2 = 6    (remove 5, add 2)  -> [1,3,2]

Each slide: O(1) -- just one subtraction and one addition
Total: O(n)
```

**Improvement: from O(n x K) to O(n). Massive speedup for large K!**

---

### 3. Two Types of Sliding Window

---

#### Type 1: FIXED-SIZE Window

**Window size K is GIVEN in the problem.**

The approach:
1. Build the first window of size K (sum up the first K elements)
2. Slide: remove leftmost element, add next rightmost element
3. Track the best result at each position

**Template:**

```java
public int fixedWindowTemplate(int[] nums, int k) {
    // Step 1: Build the first window
    int windowSum = 0;
    for (int i = 0; i < k; i++) {
        windowSum += nums[i];
    }

    int maxSum = windowSum;

    // Step 2: Slide the window from left to right
    for (int i = k; i < nums.length; i++) {
        windowSum += nums[i];       // add new element entering from right
        windowSum -= nums[i - k];   // remove element leaving from left
        maxSum = Math.max(maxSum, windowSum);
    }

    return maxSum;
}
```

**Visual Example: Maximum Sum Subarray of Size 3**

```
Array: [2, 1, 5, 1, 3, 2], K = 3

Step 1: Build first window
  [2, 1, 5] | 1, 3, 2
  sum = 8, maxSum = 8

Step 2: Slide
  2, [1, 5, 1] | 3, 2      sum = 8 - 2 + 1 = 7,  maxSum = 8
  2, 1, [5, 1, 3] | 2      sum = 7 - 1 + 3 = 9,  maxSum = 9  ** new best!
  2, 1, 5, [1, 3, 2]       sum = 9 - 5 + 2 = 6,  maxSum = 9

Answer: 9 (subarray [5, 1, 3])
```

---

#### Type 2: VARIABLE-SIZE Window

**Window size is NOT given. You need to find the longest or shortest window
that satisfies some condition.**

The approach:
1. Expand the window by moving the RIGHT pointer
2. When the window VIOLATES the condition, shrink by moving the LEFT pointer
3. Track the best valid window seen so far

**Template:**

```java
public int variableWindowTemplate(int[] nums) {
    int left = 0;
    int result = 0;
    // ... any tracking variables (sum, map, set, etc.)

    for (int right = 0; right < nums.length; right++) {
        // Step 1: Add nums[right] to the window
        // ... update tracking variables

        // Step 2: Shrink window while condition is VIOLATED
        while (/* window is invalid */) {
            // ... remove nums[left] from the window
            left++;
        }

        // Step 3: Window is now valid. Update result.
        result = Math.max(result, right - left + 1);
    }

    return result;
}
```

---

### 4. Fixed Window Example: Maximum Average Subarray

**Problem:** Find a contiguous subarray of length k with the maximum average value.

```java
public double findMaxAverage(int[] nums, int k) {
    // Build first window
    double sum = 0;
    for (int i = 0; i < k; i++) {
        sum += nums[i];
    }

    double maxSum = sum;

    // Slide the window
    for (int i = k; i < nums.length; i++) {
        sum += nums[i];         // add new element
        sum -= nums[i - k];     // remove old element
        maxSum = Math.max(maxSum, sum);
    }

    return maxSum / k;  // average = sum / k
}
```

---

### 5. Variable Window Example: Longest Substring Without Repeating Characters

**Problem:** Given a string, find the length of the longest substring without
repeating characters.

**This is one of the most commonly asked interview problems.**

**Approach:**
- Expand right pointer to grow the window.
- If we encounter a character already in the window, shrink from the left
  until the duplicate is removed.
- Use a HashSet to track characters in the current window.

```java
public int lengthOfLongestSubstring(String s) {
    Set<Character> window = new HashSet<>();
    int left = 0;
    int maxLen = 0;

    for (int right = 0; right < s.length(); right++) {
        char c = s.charAt(right);

        // If character already in window, shrink from left
        while (window.contains(c)) {
            window.remove(s.charAt(left));
            left++;
        }

        // Add current character to window
        window.add(c);

        // Update max length
        maxLen = Math.max(maxLen, right - left + 1);
    }

    return maxLen;
}
```

**Detailed Walkthrough with "abcabcbb":**

```
String: a b c a b c b b
Index:  0 1 2 3 4 5 6 7

Step 0: right=0, char='a'
  window={}, 'a' not in window
  Add 'a'. window={a}
  Window: [a] b c a b c b b     length=1, maxLen=1

Step 1: right=1, char='b'
  window={a}, 'b' not in window
  Add 'b'. window={a,b}
  Window: [a b] c a b c b b     length=2, maxLen=2

Step 2: right=2, char='c'
  window={a,b}, 'c' not in window
  Add 'c'. window={a,b,c}
  Window: [a b c] a b c b b     length=3, maxLen=3

Step 3: right=3, char='a'
  window={a,b,c}, 'a' IS in window! DUPLICATE!
  Shrink: remove s[0]='a', left=1. window={b,c}
  Now 'a' not in window.
  Add 'a'. window={b,c,a}
  Window: a [b c a] b c b b     length=3, maxLen=3

Step 4: right=4, char='b'
  window={b,c,a}, 'b' IS in window! DUPLICATE!
  Shrink: remove s[1]='b', left=2. window={c,a}
  Now 'b' not in window.
  Add 'b'. window={c,a,b}
  Window: a b [c a b] c b b     length=3, maxLen=3

Step 5: right=5, char='c'
  window={c,a,b}, 'c' IS in window! DUPLICATE!
  Shrink: remove s[2]='c', left=3. window={a,b}
  Now 'c' not in window.
  Add 'c'. window={a,b,c}
  Window: a b c [a b c] b b     length=3, maxLen=3

Step 6: right=6, char='b'
  window={a,b,c}, 'b' IS in window!
  Shrink: remove s[3]='a', left=4. window={b,c}. Still has 'b'!
  Shrink: remove s[4]='b', left=5. window={c}. Now 'b' gone.
  Add 'b'. window={c,b}
  Window: a b c a b [c b] b     length=2, maxLen=3

Step 7: right=7, char='b'
  window={c,b}, 'b' IS in window!
  Shrink: remove s[5]='c', left=6. window={b}. Still has 'b'!
  Shrink: remove s[6]='b', left=7. window={}. Now 'b' gone.
  Add 'b'. window={b}
  Window: a b c a b c b [b]     length=1, maxLen=3

ANSWER: 3 (substring "abc")
```

---

### 6. Variable Window Example: Minimum Size Subarray Sum

**Problem:** Find the smallest contiguous subarray whose sum is >= target.

```java
public int minSubArrayLen(int target, int[] nums) {
    int left = 0;
    int sum = 0;
    int minLen = Integer.MAX_VALUE;

    for (int right = 0; right < nums.length; right++) {
        sum += nums[right];  // expand window

        // Shrink while sum is >= target (window is valid)
        while (sum >= target) {
            minLen = Math.min(minLen, right - left + 1);  // record valid window
            sum -= nums[left];  // shrink from left
            left++;
        }
    }

    return minLen == Integer.MAX_VALUE ? 0 : minLen;
}
```

**Trace with target=7, nums=[2, 3, 1, 2, 4, 3]:**

```
right=0: sum=2.  2<7, continue.
right=1: sum=5.  5<7, continue.
right=2: sum=6.  6<7, continue.
right=3: sum=8.  8>=7! minLen=4. Shrink: sum=8-2=6, left=1. 6<7, stop.
right=4: sum=10. 10>=7! minLen=3. Shrink: sum=10-3=7, left=2. 7>=7! minLen=2.
         Shrink: sum=7-1=6, left=3. 6<7, stop.
right=5: sum=9.  9>=7! minLen=2. Shrink: sum=9-2=7, left=4. 7>=7! minLen=2.
         Shrink: sum=7-4=3, left=5. 3<7, stop.

Answer: 2 (subarray [4,3])
```

---

### 7. When to Use Sliding Window (TRIGGER WORDS)

| Trigger in Problem Statement             | Window Type    |
|------------------------------------------|----------------|
| "Maximum/minimum sum of subarray of size K" | Fixed size   |
| "Average of subarray of size K"            | Fixed size     |
| "Longest substring with at most K distinct" | Variable size |
| "Shortest subarray with sum >= target"     | Variable size  |
| "Longest substring without repeating"      | Variable size  |
| "Contiguous subarray/substring"            | Either type    |
| "Longest/shortest with condition"          | Variable size  |

**Key insight: If the problem mentions "contiguous" and asks for
"longest/shortest/maximum/minimum", think sliding window!**

---

### 8. Sliding Window vs Two Pointers

They look similar (both use two pointers moving right), but the intent differs:

| Sliding Window | Two Pointers |
|----------------|-------------|
| Maintains a RANGE (subarray/substring) | Uses two indices for comparison |
| Tracks aggregate (sum, count, set) | Compares values at pointer positions |
| Window grows and shrinks | Pointers move based on conditions |
| For subarray/substring problems | For sorted array pair-finding |

---

## PART 2: BINARY SEARCH

---

### 1. What is Binary Search?

**Plain English:**
A technique to find an element in a SORTED collection by repeatedly dividing
the search space in half. Each step eliminates 50% of the remaining
possibilities.

**Real-Life Analogy:**
You are looking for the word "MANGO" in a PHYSICAL DICTIONARY.

```
Step 1: Open the middle page. You see "HORSE".
        "MANGO" comes AFTER "HORSE" alphabetically.
        -> Ignore the entire first half!

Step 2: Open the middle of the remaining half. You see "ORANGE".
        "MANGO" comes BEFORE "ORANGE".
        -> Ignore the second quarter!

Step 3: Open the middle of what is left. You see "LEMON".
        "MANGO" comes AFTER "LEMON".
        -> Continue narrowing...

Step 4: You find "MANGO"!
```

A dictionary with 1000 pages? Found in about 10 steps.
A dictionary with 1,000,000 pages? Found in about 20 steps.

That is the power of **O(log n)**.

```
n           | Linear O(n) | Binary O(log n)
------------|-------------|----------------
100         | 100 steps   | 7 steps
1,000       | 1,000       | 10 steps
1,000,000   | 1,000,000   | 20 steps
1,000,000,000| 1 billion  | 30 steps !!
```

---

### 2. Why Does Binary Search Exist?

**The Problem:**
Searching in a sorted array using linear search is O(n). Can we do better?

**The Insight:**
If the array is sorted, we can use the ordering to eliminate half the
candidates with each comparison. This is fundamentally more efficient --
O(log n) instead of O(n).

**Prerequisite:** The data MUST be sorted (or the search space must be
monotonic -- more on this later).

---

### 3. How Binary Search Works - Step by Step

**Search for 23 in sorted array:**

```
Array: [2, 5, 8, 12, 16, 23, 38, 56, 72, 91]
Index:  0  1  2   3   4   5   6   7   8   9

Step 1: left=0, right=9, mid=(0+9)/2=4
        nums[4]=16.  23 > 16.
        -> Target is in RIGHT half. left = mid + 1 = 5

        [2, 5, 8, 12, 16, 23, 38, 56, 72, 91]
                        ^mid
         ELIMINATED     |----search here----|

Step 2: left=5, right=9, mid=(5+9)/2=7
        nums[7]=56.  23 < 56.
        -> Target is in LEFT half. right = mid - 1 = 6

        [2, 5, 8, 12, 16, 23, 38, 56, 72, 91]
                                  ^mid
                       |--here--|  ELIMINATED

Step 3: left=5, right=6, mid=(5+6)/2=5
        nums[5]=23.  23 == 23.
        -> FOUND IT! Return index 5.

        [2, 5, 8, 12, 16, 23, 38, 56, 72, 91]
                            ^
                          FOUND!

Total comparisons: 3 (compared to 6 for linear search)
```

---

### 4. Three Binary Search Templates

#### Template 1: Standard Binary Search (Find Exact Element)

```java
public int binarySearch(int[] nums, int target) {
    int left = 0;
    int right = nums.length - 1;

    while (left <= right) {         // NOTE: <= not <
        int mid = left + (right - left) / 2;
        // WHY not (left+right)/2? Because left+right can overflow int range.
        // left + (right-left)/2 is mathematically equivalent but safe.

        if (nums[mid] == target) {
            return mid;              // found it!
        } else if (nums[mid] < target) {
            left = mid + 1;          // target is in right half
        } else {
            right = mid - 1;         // target is in left half
        }
    }

    return -1;  // not found
}
```

**Key details:**
- Loop condition: `left <= right` (includes the case where left == right,
  which means one element left to check)
- Move: `left = mid + 1` or `right = mid - 1` (always exclude mid since
  we already checked it)
- Safe mid calculation: `left + (right - left) / 2` to prevent integer overflow

---

#### Template 2: Lower Bound (First Element >= Target)

**Use when:** "Find the first position where target could be inserted" or
"Find the leftmost occurrence of target."

```java
// Returns the INDEX of the first element >= target.
// If all elements are < target, returns nums.length.
public int lowerBound(int[] nums, int target) {
    int left = 0;
    int right = nums.length;  // NOTE: not nums.length - 1

    while (left < right) {    // NOTE: < not <=
        int mid = left + (right - left) / 2;

        if (nums[mid] < target) {
            left = mid + 1;   // mid is too small, answer is to the right
        } else {
            right = mid;      // mid could be the answer, keep it
        }
    }

    return left;  // left == right at this point
}
```

**Example: Find first element >= 5 in [1, 3, 5, 5, 5, 8, 9]**

```
left=0, right=7, mid=3: nums[3]=5 >= 5 -> right=3
left=0, right=3, mid=1: nums[1]=3 < 5  -> left=2
left=2, right=3, mid=2: nums[2]=5 >= 5 -> right=2
left=2, right=2: STOP. Answer = index 2 (first 5).
```

---

#### Template 3: Upper Bound (Last Element <= Target)

**Use when:** "Find the rightmost occurrence" or "Find the last position
where target appears."

```java
// Returns the INDEX of the last element <= target.
// If all elements are > target, returns -1.
public int upperBound(int[] nums, int target) {
    int left = 0;
    int right = nums.length - 1;

    while (left <= right) {
        int mid = left + (right - left) / 2;

        if (nums[mid] <= target) {
            left = mid + 1;   // mid could be answer, but maybe there is
                              // a later one -> move right
        } else {
            right = mid - 1;  // mid is too big
        }
    }

    return right;  // right is the last position <= target
}
```

**Example: Find last element <= 5 in [1, 3, 5, 5, 5, 8, 9]**

```
left=0, right=6, mid=3: nums[3]=5 <= 5 -> left=4
left=4, right=6, mid=5: nums[5]=8 > 5  -> right=4
left=4, right=4, mid=4: nums[4]=5 <= 5 -> left=5
left=5, right=4: STOP. Answer = right = index 4 (last 5).
```

---

### 5. Finding First and Last Position of Element

**Problem:** Given a sorted array, find the starting and ending position of
a given target value. (LeetCode 34)

This combines lower bound and upper bound.

```java
public int[] searchRange(int[] nums, int target) {
    int first = lowerBound(nums, target);
    int last = upperBound(nums, target);

    // Check if target actually exists
    if (first >= nums.length || nums[first] != target) {
        return new int[]{-1, -1};
    }

    return new int[]{first, last};
}
```

**Visual:**

```
Array: [5, 7, 7, 8, 8, 8, 10],  target = 8

lowerBound(8) -> index 3 (first 8)
upperBound(8) -> index 5 (last 8)

Result: [3, 5]

         [5, 7, 7, 8, 8, 8, 10]
                   ^        ^
                 first    last
```

---

### 6. Binary Search on Rotated Sorted Array

**Problem:** Array was sorted but then rotated. Find the target.
Example: [4, 5, 6, 7, 0, 1, 2] (original: [0,1,2,4,5,6,7], rotated at index 4)

**Key Insight:** In a rotated sorted array, at least ONE HALF is always sorted.
Determine which half is sorted, then check if target is in that half.

```java
public int search(int[] nums, int target) {
    int left = 0;
    int right = nums.length - 1;

    while (left <= right) {
        int mid = left + (right - left) / 2;

        if (nums[mid] == target) return mid;

        // Determine which half is sorted
        if (nums[left] <= nums[mid]) {
            // LEFT half is sorted
            if (target >= nums[left] && target < nums[mid]) {
                right = mid - 1;  // target is in the sorted left half
            } else {
                left = mid + 1;   // target is in the right half
            }
        } else {
            // RIGHT half is sorted
            if (target > nums[mid] && target <= nums[right]) {
                left = mid + 1;   // target is in the sorted right half
            } else {
                right = mid - 1;  // target is in the left half
            }
        }
    }

    return -1;
}
```

**Visual Trace: Find 0 in [4, 5, 6, 7, 0, 1, 2]**

```
left=0, right=6, mid=3: nums[3]=7. Not target.
  nums[0]=4 <= nums[3]=7 -> left half [4,5,6,7] is sorted.
  Is 0 in [4,7)? No (0 < 4). -> left = 4.

left=4, right=6, mid=5: nums[5]=1. Not target.
  nums[4]=0 <= nums[5]=1 -> left half [0,1] is sorted.
  Is 0 in [0,1)? Yes! -> right = 4.

left=4, right=4, mid=4: nums[4]=0. FOUND! Return 4.
```

---

### 7. Binary Search on Answer (ADVANCED BUT IMPORTANT)

**What if there is no array? What if you are searching for the ANSWER itself?**

Some problems ask: "What is the minimum/maximum value X such that condition
is satisfied?" The answer lies in a RANGE, and the condition is monotonic
(if X works, then X+1 also works, or vice versa).

**Template:**

```java
public int binarySearchOnAnswer(int low, int high) {
    while (low < high) {
        int mid = low + (high - low) / 2;

        if (canAchieve(mid)) {
            high = mid;      // mid works, try smaller (find minimum)
        } else {
            low = mid + 1;   // mid does not work, try bigger
        }
    }

    return low;  // minimum value that satisfies the condition
}

// This function checks: "Can we achieve the goal with parameter = mid?"
private boolean canAchieve(int mid) {
    // problem-specific logic
}
```

**Classic Example: Koko Eating Bananas**

**Problem:** Koko has N piles of bananas. She can eat at most `speed` bananas
per hour from one pile. She has H hours. What is the MINIMUM speed to finish
all bananas in H hours?

```
Piles: [3, 6, 7, 11],  H = 8 hours

Speed 1: hours needed = ceil(3/1)+ceil(6/1)+ceil(7/1)+ceil(11/1) = 3+6+7+11 = 27. Too slow!
Speed 4: hours needed = ceil(3/4)+ceil(6/4)+ceil(7/4)+ceil(11/4) = 1+2+2+3 = 8.  Exactly H!
Speed 3: hours needed = ceil(3/3)+ceil(6/3)+ceil(7/3)+ceil(11/3) = 1+2+3+4 = 10. Too slow!

Minimum speed = 4.
```

**The search space is [1, max(piles)]. Binary search on this range.**

```java
public int minEatingSpeed(int[] piles, int h) {
    int low = 1;
    int high = Arrays.stream(piles).max().getAsInt();

    while (low < high) {
        int mid = low + (high - low) / 2;

        if (canFinish(piles, h, mid)) {
            high = mid;        // this speed works, try slower
        } else {
            low = mid + 1;     // too slow, try faster
        }
    }

    return low;
}

private boolean canFinish(int[] piles, int h, int speed) {
    int hours = 0;
    for (int pile : piles) {
        hours += (pile + speed - 1) / speed;  // ceiling division
        // equivalent to Math.ceil((double)pile / speed) but avoids floating point
    }
    return hours <= h;
}
```

**Binary search trace:**

```
Piles: [3, 6, 7, 11],  H = 8
Search range: [1, 11]

low=1, high=11, mid=6:  canFinish(6)?  1+1+2+2=6 <= 8? Yes.  high=6.
low=1, high=6,  mid=3:  canFinish(3)?  1+2+3+4=10 <= 8? No.  low=4.
low=4, high=6,  mid=5:  canFinish(5)?  1+2+2+3=8 <= 8? Yes.  high=5.
low=4, high=5,  mid=4:  canFinish(4)?  1+2+2+3=8 <= 8? Yes.  high=4.
low=4, high=4: STOP. Answer = 4.
```

---

### 8. When to Use Binary Search (TRIGGERS)

| Trigger                                      | Type                      |
|----------------------------------------------|---------------------------|
| "Sorted array" + "find element"              | Standard binary search    |
| "Sorted array" + "find first/last"           | Lower/upper bound         |
| "Rotated sorted array"                       | Modified binary search    |
| "Minimum speed/capacity/size to achieve X"   | Binary search on answer   |
| "Maximum value such that condition holds"    | Binary search on answer   |
| "Search in matrix (sorted rows/columns)"     | Binary search on 2D       |
| "Find peak element"                          | Binary search on gradient |
| "Square root of a number"                    | Binary search on answer   |

**Decision Flow:**

```
Is the search space SORTED or MONOTONIC?
  |
  +--YES--> Is it a direct array/list?
  |           |
  |           +--YES--> Is the array modified (rotated, peaks)?
  |           |           |
  |           |           +--YES--> Modified binary search
  |           |           +--NO---> Standard / lower / upper bound
  |           |
  |           +--NO---> Are you searching for a VALUE (answer)?
  |                       |
  |                       +--YES--> Binary search on answer
  |
  +--NO---> Binary search does NOT apply.
            Consider HashMap, Two Pointers, etc.
```

---

### 9. Binary Search in Java Standard Library

```java
import java.util.Arrays;
import java.util.Collections;

// Arrays.binarySearch -- for primitive arrays
int[] arr = {1, 3, 5, 7, 9};
int idx = Arrays.binarySearch(arr, 5);    // returns 2 (index of 5)
int idx2 = Arrays.binarySearch(arr, 4);   // returns -3 (negative = not found)
// Return value when not found: -(insertion point) - 1
// For 4: insertion point would be 2, so return -(2)-1 = -3

// Collections.binarySearch -- for Lists
List<Integer> list = Arrays.asList(1, 3, 5, 7, 9);
int idx3 = Collections.binarySearch(list, 7);  // returns 3
```

---

### 10. Common Mistakes

1. **Infinite loop from wrong mid calculation**
   ```java
   // WRONG: when left=5, right=6, mid=(5+6)/2=5, left=mid=5 -> infinite loop
   // This happens when using left = mid (instead of left = mid + 1)
   // Always ensure the search space SHRINKS each iteration
   ```

2. **Integer overflow in mid calculation**
   ```java
   // WRONG: can overflow when left and right are both large
   int mid = (left + right) / 2;

   // CORRECT: safe from overflow
   int mid = left + (right - left) / 2;
   ```

3. **Off-by-one in loop condition**
   ```java
   // Standard search: use <=
   while (left <= right)

   // Lower bound: use <
   while (left < right)

   // Mixing these up causes either missing elements or infinite loops
   ```

4. **Using binary search on unsorted data**
   ```java
   // Binary search REQUIRES sorted/monotonic data
   // If data is unsorted, sort it first or use a different approach
   ```

5. **Wrong half selection in rotated array**
   ```java
   // Must check nums[left] <= nums[mid] (not <)
   // The = handles the case where left == mid (two elements left)
   ```

6. **Forgetting ceiling division in "binary search on answer"**
   ```java
   // WRONG: integer division truncates
   int hours = pile / speed;  // 7/4 = 1, but you need 2 hours!

   // CORRECT: ceiling division
   int hours = (pile + speed - 1) / speed;  // (7+4-1)/4 = 10/4 = 2
   ```

---

### 11. Quick Summary

```
+----------------------------+-------------------------------------------+
| Concept                    | Key Takeaway                              |
+----------------------------+-------------------------------------------+
| Sliding Window (fixed)     | Size K given, slide: -left +right, O(n)  |
| Sliding Window (variable)  | Expand right, shrink left, track best     |
| SW trigger words           | "longest/shortest subarray with..."       |
| Binary Search              | Sorted data, halve search space, O(log n)|
| Standard BS                | Find exact element, left <= right         |
| Lower Bound                | First element >= target, left < right     |
| Upper Bound                | Last element <= target                    |
| BS on Rotated Array        | One half always sorted, check which       |
| BS on Answer               | Search in answer range, check feasibility |
| BS trigger words           | "minimum X to achieve Y", sorted + find   |
+----------------------------+-------------------------------------------+

Time Complexities to MEMORIZE:
  Fixed sliding window:      O(n)
  Variable sliding window:   O(n)
  Standard binary search:    O(log n)
  Binary search on answer:   O(n log m) where m = answer range
  Brute force subarray:      O(n x K) -- sliding window replaces this
  Brute force search:        O(n)     -- binary search replaces this
```

---

**You are now ready to tackle Week 3 LeetCode problems!**
Sliding Window and Binary Search are pattern-based techniques.
Once you recognize the trigger words, the template writes itself.
Practice identifying these patterns before jumping to code.
