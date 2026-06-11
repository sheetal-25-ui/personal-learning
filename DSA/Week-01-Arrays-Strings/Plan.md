# Week 1: Arrays & Strings

## Concept Summary

### Arrays
- Continuous block of memory storing elements of same type
- Access by index: O(1), Search: O(n), Insert/Delete: O(n)
- Most fundamental data structure — 30-40% of interview questions!

### Strings
- Array of characters
- In Python, strings are immutable (cannot change in place)
- String operations: slicing, reverse, find, replace

---

## Key Patterns to Learn This Week

| Pattern | What It Does | When to Use |
|---------|-------------|-------------|
| **Two Pass** | Go through array twice | When you need info from full array before deciding |
| **In-place modification** | Modify array without extra space | When asked O(1) space |
| **Prefix Sum** | Pre-compute sums for range queries | Subarray sum problems |
| **String manipulation** | Reverse, check palindrome, anagrams | String comparison problems |

---

## Day 1-2: Easy Arrays (Warm Up)

| # | Problem | LeetCode Link | Difficulty | Microsoft Asked? |
|---|---------|--------------|------------|-----------------|
| 1 | Two Sum | https://leetcode.com/problems/two-sum/ | Easy | Yes (Very Frequent) |
| 2 | Best Time to Buy and Sell Stock | https://leetcode.com/problems/best-time-to-buy-and-sell-stock/ | Easy | Yes |
| 3 | Contains Duplicate | https://leetcode.com/problems/contains-duplicate/ | Easy | Yes |
| 4 | Maximum Subarray (Kadane's) | https://leetcode.com/problems/maximum-subarray/ | Medium | Yes (Very Frequent) |
| 5 | Move Zeroes | https://leetcode.com/problems/move-zeroes/ | Easy | Yes |
| 6 | Plus One | https://leetcode.com/problems/plus-one/ | Easy | Yes |

### How to Solve "Two Sum" (Example Walkthrough)
```
Problem: Find two numbers in array that add up to target
Input: nums = [2, 7, 11, 15], target = 9
Output: [0, 1] (because nums[0] + nums[1] = 2 + 7 = 9)

Brute Force (O(n²)):
  Check every pair → slow

Optimal (O(n)):
  Use a HashMap!
  For each number, check if (target - number) exists in map

  Step 1: num=2, need 9-2=7, map={} → not found, store {2:0}
  Step 2: num=7, need 9-7=2, map={2:0} → FOUND! Return [0,1]
```

```python
def twoSum(nums, target):
    seen = {}  # value → index
    for i, num in enumerate(nums):
        complement = target - num
        if complement in seen:
            return [seen[complement], i]
        seen[num] = i
```

---

## Day 3-4: Medium Arrays

| # | Problem | LeetCode Link | Difficulty | Microsoft Asked? |
|---|---------|--------------|------------|-----------------|
| 7 | Product of Array Except Self | https://leetcode.com/problems/product-of-array-except-self/ | Medium | Yes (Very Frequent) |
| 8 | 3Sum | https://leetcode.com/problems/3sum/ | Medium | Yes |
| 9 | Container With Most Water | https://leetcode.com/problems/container-with-most-water/ | Medium | Yes |
| 10 | Rotate Array | https://leetcode.com/problems/rotate-array/ | Medium | Yes |
| 11 | Find Minimum in Rotated Sorted Array | https://leetcode.com/problems/find-minimum-in-rotated-sorted-array/ | Medium | Yes |
| 12 | Merge Intervals | https://leetcode.com/problems/merge-intervals/ | Medium | Yes (Very Frequent) |

---

## Day 5-6: Strings

| # | Problem | LeetCode Link | Difficulty | Microsoft Asked? |
|---|---------|--------------|------------|-----------------|
| 13 | Valid Palindrome | https://leetcode.com/problems/valid-palindrome/ | Easy | Yes |
| 14 | Valid Anagram | https://leetcode.com/problems/valid-anagram/ | Easy | Yes |
| 15 | Longest Substring Without Repeating | https://leetcode.com/problems/longest-substring-without-repeating-characters/ | Medium | Yes (Very Frequent) |
| 16 | String to Integer (atoi) | https://leetcode.com/problems/string-to-integer-atoi/ | Medium | Yes (Microsoft Classic!) |
| 17 | Reverse Words in a String | https://leetcode.com/problems/reverse-words-in-a-string/ | Medium | Yes |
| 18 | Group Anagrams | https://leetcode.com/problems/group-anagrams/ | Medium | Yes |

---

## Day 7: Revision Day

- Redo any 3 questions you found hard
- Without looking at solutions!
- Write the pattern name next to each question
- Time yourself: target 20 min per Medium, 10 min per Easy

---

## Patterns Cheat Sheet (Week 1)

```
Two Sum Pattern:
  "Find pair that satisfies condition" → Use HashMap

Kadane's Algorithm:
  "Maximum subarray sum" → Track current_max and global_max

Merge Intervals:
  "Overlapping ranges" → Sort by start, merge if overlap

Sliding Window (preview for Week 3):
  "Longest/shortest substring with condition" → Expand right, shrink left

Two Pointers:
  "Sorted array, find pair" → One pointer at start, one at end
```

---

## Self-Check: Am I Ready for Week 2?

- [ ] Can solve Two Sum in under 5 minutes
- [ ] Understand Kadane's algorithm without looking at notes
- [ ] Can explain Merge Intervals approach in plain words
- [ ] Solved at least 15 of the 18 questions
- [ ] Can identify "this is a HashMap problem" vs "this is a two-pointer problem"
