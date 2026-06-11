# Week 8: Backtracking & Greedy

## Backtracking — "Try All Options, Undo Bad Ones"

Think of solving a maze:
- Go forward until you hit a wall
- Backtrack to the last decision point
- Try a different path
- Repeat until you find the exit

```python
def backtrack(choices, current_path, result):
    if IS_COMPLETE(current_path):
        result.append(current_path.copy())
        return

    for choice in choices:
        if IS_VALID(choice):
            current_path.append(choice)       # make choice
            backtrack(choices, current_path, result)  # explore
            current_path.pop()                 # undo choice (backtrack!)
```

## Greedy — "Always Pick the Best Option Right Now"

- At each step, pick the locally optimal choice
- Hope it leads to the globally optimal solution
- Faster than DP, but doesn't always work

---

## Day 1-3: Backtracking

| # | Problem | LeetCode Link | Difficulty | Microsoft? |
|---|---------|--------------|------------|------------|
| 1 | Subsets | https://leetcode.com/problems/subsets/ | Medium | Yes |
| 2 | Permutations | https://leetcode.com/problems/permutations/ | Medium | Yes (Frequent) |
| 3 | Combination Sum | https://leetcode.com/problems/combination-sum/ | Medium | Yes |
| 4 | Letter Combinations of Phone | https://leetcode.com/problems/letter-combinations-of-a-phone-number/ | Medium | Yes (Classic!) |
| 5 | N-Queens | https://leetcode.com/problems/n-queens/ | Hard | Yes |
| 6 | Word Search | https://leetcode.com/problems/word-search/ | Medium | Yes (Frequent!) |
| 7 | Generate Parentheses | https://leetcode.com/problems/generate-parentheses/ | Medium | Yes |

---

## Day 4-5: Greedy

| # | Problem | LeetCode Link | Difficulty | Microsoft? |
|---|---------|--------------|------------|------------|
| 8 | Jump Game | https://leetcode.com/problems/jump-game/ | Medium | Yes |
| 9 | Jump Game II | https://leetcode.com/problems/jump-game-ii/ | Medium | Yes |
| 10 | Task Scheduler | https://leetcode.com/problems/task-scheduler/ | Medium | Yes (Frequent!) |
| 11 | Meeting Rooms II | https://leetcode.com/problems/meeting-rooms-ii/ | Medium | Yes (Classic) |
| 12 | Gas Station | https://leetcode.com/problems/gas-station/ | Medium | Yes |

---

## Day 6-7: Revision

- Word Search + Permutations (backtracking)
- Task Scheduler + Meeting Rooms (greedy)
- Write backtracking template from memory

---

## Self-Check

- [ ] Can write backtracking template from memory
- [ ] Understand difference between backtracking, DFS, and brute force
- [ ] Can solve Permutations and Subsets in 15 minutes
- [ ] Know when greedy works vs when you need DP
- [ ] Solved at least 10 of 12 questions
