# Week 3: Sliding Window & Binary Search

## Sliding Window — The Pattern Microsoft LOVES

### When to Use
- "Find longest/shortest **subarray or substring** with some condition"
- Converts O(n²) brute force to O(n)

### Template
```python
def slidingWindow(s):
    left = 0
    window = {}  # or a counter
    result = 0

    for right in range(len(s)):
        # 1. Add right element to window
        window[s[right]] = window.get(s[right], 0) + 1

        # 2. Shrink window if condition is violated
        while CONDITION_VIOLATED:
            window[s[left]] -= 1
            left += 1

        # 3. Update result
        result = max(result, right - left + 1)

    return result
```

---

## Day 1-2: Sliding Window

| # | Problem | LeetCode Link | Difficulty | Microsoft? |
|---|---------|--------------|------------|------------|
| 1 | Longest Substring Without Repeating | https://leetcode.com/problems/longest-substring-without-repeating-characters/ | Medium | Yes (Top 3 Microsoft!) |
| 2 | Minimum Window Substring | https://leetcode.com/problems/minimum-window-substring/ | Hard | Yes |
| 3 | Longest Repeating Character Replacement | https://leetcode.com/problems/longest-repeating-character-replacement/ | Medium | Yes |
| 4 | Permutation in String | https://leetcode.com/problems/permutation-in-string/ | Medium | Yes |
| 5 | Maximum Average Subarray I | https://leetcode.com/problems/maximum-average-subarray-i/ | Easy | Yes |
| 6 | Fruit Into Baskets | https://leetcode.com/problems/fruit-into-baskets/ | Medium | Yes |
| 7 | Sliding Window Maximum | https://leetcode.com/problems/sliding-window-maximum/ | Hard | Yes |

---

## Binary Search — Not Just "Find a Number"

### When to Use
- Sorted array + find something → Binary Search
- "Find minimum/maximum that satisfies condition" → Binary Search on Answer

### Template
```python
def binarySearch(nums, target):
    left, right = 0, len(nums) - 1

    while left <= right:
        mid = (left + right) // 2
        if nums[mid] == target:
            return mid
        elif nums[mid] < target:
            left = mid + 1
        else:
            right = mid - 1

    return -1  # not found
```

---

## Day 3-5: Binary Search

| # | Problem | LeetCode Link | Difficulty | Microsoft? |
|---|---------|--------------|------------|------------|
| 8 | Binary Search | https://leetcode.com/problems/binary-search/ | Easy | Yes |
| 9 | Search in Rotated Sorted Array | https://leetcode.com/problems/search-in-rotated-sorted-array/ | Medium | Yes (Very Frequent) |
| 10 | Find First and Last Position | https://leetcode.com/problems/find-first-and-last-position-of-element-in-sorted-array/ | Medium | Yes |
| 11 | Search a 2D Matrix | https://leetcode.com/problems/search-a-2d-matrix/ | Medium | Yes |
| 12 | Koko Eating Bananas | https://leetcode.com/problems/koko-eating-bananas/ | Medium | Yes |
| 13 | Median of Two Sorted Arrays | https://leetcode.com/problems/median-of-two-sorted-arrays/ | Hard | Yes (Classic!) |
| 14 | Find Peak Element | https://leetcode.com/problems/find-peak-element/ | Medium | Yes |
| 15 | Time Based Key-Value Store | https://leetcode.com/problems/time-based-key-value-store/ | Medium | Yes |

---

## Day 6: Mixed Practice (Both Patterns)

Redo any 3 hard problems from this week.

## Day 7: Revision

Write from memory:
1. Sliding Window template
2. Binary Search template
3. For each problem — which pattern and why?

---

## Self-Check

- [ ] Can write Sliding Window template from memory
- [ ] Can write Binary Search template from memory
- [ ] Know the difference between "binary search on array" vs "binary search on answer"
- [ ] Solved at least 12 of 15 questions
