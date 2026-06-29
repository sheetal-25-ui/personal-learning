# Week 2: HashMap & Two Pointers

## Concept Summary

### HashMap (Dictionary in Python)
- Stores key-value pairs
- Lookup, insert, delete: O(1) average
- **Most useful data structure in interviews!**
- Use when: counting things, finding duplicates, checking existence

### Two Pointers
- Use two pointers (indices) that move toward or away from each other
- Works best on **sorted arrays**
- Reduces O(n²) brute force to O(n)

---

## Key Patterns

| Pattern | When to Use | Example |
|---------|-------------|---------|
| **Frequency Map** | Count occurrences | "Find most common element" |
| **Seen Map** | Track what you've visited | "Find first duplicate" |
| **Two Pointers (opposite)** | Start from both ends | "Is palindrome?", "Two sum in sorted" |
| **Two Pointers (same dir)** | Fast and slow pointer | "Remove duplicates", "detect cycle" |

---

## Day 1-2: HashMap Problems

| # | Problem | LeetCode Link | Difficulty | Microsoft? |
|---|---------|--------------|------------|------------|
| 1 | Two Sum | https://leetcode.com/problems/two-sum/ | Easy | Yes (revise!) |
| 2 | Roman to Integer | https://leetcode.com/problems/roman-to-integer/ | Easy | Yes |
| 3 | Majority Element | https://leetcode.com/problems/majority-element/ | Easy | Yes |
| 4 | Top K Frequent Elements | https://leetcode.com/problems/top-k-frequent-elements/ | Medium | Yes |
| 5 | Subarray Sum Equals K | https://leetcode.com/problems/subarray-sum-equals-k/ | Medium | Yes (Frequent) |
| 6 | Longest Consecutive Sequence | https://leetcode.com/problems/longest-consecutive-sequence/ | Medium | Yes |
| 7 | First Unique Character in String | https://leetcode.com/problems/first-unique-character-in-a-string/ | Easy | Yes |

### Pattern: Frequency Map
```python
# Count how many times each element appears
from collections import Counter

nums = [1, 2, 2, 3, 3, 3]
freq = Counter(nums)  # {3: 3, 2: 2, 1: 1}

# Or manually:
freq = {}
for num in nums:
    freq[num] = freq.get(num, 0) + 1
```

### Pattern: Prefix Sum + HashMap
```python
# Subarray Sum Equals K
# Key idea: if prefix_sum[j] - prefix_sum[i] == k, then subarray [i+1..j] sums to k

def subarraySum(nums, k):
    count = 0
    current_sum = 0
    prefix_sums = {0: 1}  # base case: empty prefix has sum 0

    for num in nums:
        current_sum += num
        if current_sum - k in prefix_sums:
            count += prefix_sums[current_sum - k]
        prefix_sums[current_sum] = prefix_sums.get(current_sum, 0) + 1

    return count
```

---

## Day 3-4: Two Pointer Problems

| # | Problem | LeetCode Link | Difficulty | Microsoft? |
|---|---------|--------------|------------|------------|
| 8 | Two Sum II (Sorted Array) | https://leetcode.com/problems/two-sum-ii-input-array-is-sorted/ | Medium | Yes |
| 9 | 3Sum | https://leetcode.com/problems/3sum/ | Medium | Yes (revise) |
| 10 | Valid Palindrome | https://leetcode.com/problems/valid-palindrome/ | Easy | Yes | - 2
| 11 | Remove Duplicates from Sorted Array | https://leetcode.com/problems/remove-duplicates-from-sorted-array/ | Easy | Yes | -2
| 12 | Sort Colors (Dutch National Flag) | https://leetcode.com/problems/sort-colors/ | Medium | Yes (Classic!) | - 2
| 13 | Trapping Rain Water | https://leetcode.com/problems/trapping-rain-water/ | Hard | Yes (Frequent) | -2

### Pattern: Two Pointers (Opposite Direction)
```python
# Two Sum in sorted array
def twoSum(numbers, target):
    left, right = 0, len(numbers) - 1

    while left < right:
        total = numbers[left] + numbers[right]
        if total == target:
            return [left + 1, right + 1]
        elif total < target:
            left += 1    # need bigger sum → move left pointer right
        else:
            right -= 1   # need smaller sum → move right pointer left
```

### Pattern: Two Pointers (Same Direction — Fast/Slow)
```python
# Remove duplicates in-place
def removeDuplicates(nums):
    slow = 0  # position to place next unique element
    for fast in range(1, len(nums)):
        if nums[fast] != nums[slow]:
            slow += 1
            nums[slow] = nums[fast]
    return slow + 1
```

---

## Day 5-6: Mixed Practice

| # | Problem | LeetCode Link | Difficulty | Microsoft? |
|---|---------|--------------|------------|------------|
| 14 | 4Sum | https://leetcode.com/problems/4sum/ | Medium | Yes |
| 15 | Intersection of Two Arrays II | https://leetcode.com/problems/intersection-of-two-arrays-ii/ | Easy | Yes |

---

## Day 7: Revision

- Redo Trapping Rain Water (the hardest one this week)
- Redo Subarray Sum Equals K
- Write all patterns from memory on paper

---

## Quick Decision: HashMap or Two Pointers?

```
Is the array SORTED?
├── YES → Try Two Pointers first
└── NO
    ├── Need to COUNT or FIND something? → HashMap
    ├── Need to find PAIRS? → HashMap (or sort + two pointers)
    └── Need to find SUBARRAYS? → Prefix Sum + HashMap
```

---

## Self-Check

- [ ] Can explain Prefix Sum + HashMap pattern
- [ ] Can solve Two Sum in sorted array in 5 minutes
- [ ] Understand Trapping Rain Water approach
- [ ] Know when to use HashMap vs Two Pointers
- [ ] Solved at least 12 of 15 questions
