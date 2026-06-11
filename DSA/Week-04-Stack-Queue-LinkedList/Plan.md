# Week 4: Stack, Queue & Linked List

## Stack (LIFO — Last In First Out)
- Think of a stack of plates — you can only add/remove from the top
- Use for: matching brackets, undo operations, next greater element

## Queue (FIFO — First In First Out)
- Think of a line at a ticket counter — first person in line gets served first
- Use for: BFS, scheduling, sliding window problems

## Linked List
- Chain of nodes, each pointing to the next
- Microsoft asks Linked List questions VERY frequently!

---

## Day 1-2: Stack

| # | Problem | LeetCode Link | Difficulty | Microsoft? |
|---|---------|--------------|------------|------------|
| 1 | Valid Parentheses | https://leetcode.com/problems/valid-parentheses/ | Easy | Yes (Classic) |
| 2 | Min Stack | https://leetcode.com/problems/min-stack/ | Medium | Yes (Frequent!) |
| 3 | Daily Temperatures | https://leetcode.com/problems/daily-temperatures/ | Medium | Yes |
| 4 | Evaluate Reverse Polish Notation | https://leetcode.com/problems/evaluate-reverse-polish-notation/ | Medium | Yes |
| 5 | Next Greater Element I | https://leetcode.com/problems/next-greater-element-i/ | Easy | Yes |
| 6 | Largest Rectangle in Histogram | https://leetcode.com/problems/largest-rectangle-in-histogram/ | Hard | Yes |

---

## Day 3-4: Queue

| # | Problem | LeetCode Link | Difficulty | Microsoft? |
|---|---------|--------------|------------|------------|
| 7 | Implement Queue using Stacks | https://leetcode.com/problems/implement-queue-using-stacks/ | Easy | Yes |
| 8 | Implement Stack using Queues | https://leetcode.com/problems/implement-stack-using-queues/ | Easy | Yes |
| 9 | Design Circular Queue | https://leetcode.com/problems/design-circular-queue/ | Medium | Yes |

---

## Day 5-6: Linked List (Microsoft Favorite!)

| # | Problem | LeetCode Link | Difficulty | Microsoft? |
|---|---------|--------------|------------|------------|
| 10 | Reverse Linked List | https://leetcode.com/problems/reverse-linked-list/ | Easy | Yes (Top 5 Microsoft!) |
| 11 | Merge Two Sorted Lists | https://leetcode.com/problems/merge-two-sorted-lists/ | Easy | Yes (Very Frequent) |
| 12 | Linked List Cycle | https://leetcode.com/problems/linked-list-cycle/ | Easy | Yes |
| 13 | Add Two Numbers | https://leetcode.com/problems/add-two-numbers/ | Medium | Yes (Classic) |
| 14 | Copy List with Random Pointer | https://leetcode.com/problems/copy-list-with-random-pointer/ | Medium | Yes (Frequent!) |
| 15 | LRU Cache | https://leetcode.com/problems/lru-cache/ | Medium | Yes (Top 3 Microsoft!) |

### Key Pattern: Reverse Linked List
```python
def reverseList(head):
    prev = None
    current = head

    while current:
        next_node = current.next  # save next
        current.next = prev       # reverse pointer
        prev = current            # move prev forward
        current = next_node       # move current forward

    return prev  # prev is now the new head
```

---

## Day 7: Revision

- LRU Cache (most important — asked in almost every Microsoft interview)
- Reverse Linked List (must solve in 5 min)
- Valid Parentheses

---

## Self-Check

- [ ] Can implement Stack and Queue from scratch
- [ ] Can reverse a Linked List in 5 minutes
- [ ] Understand LRU Cache design and implementation
- [ ] Solved at least 12 of 15 questions
