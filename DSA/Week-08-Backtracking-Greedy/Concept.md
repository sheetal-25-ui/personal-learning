# Week 8: Backtracking & Greedy Algorithms

---

## Part 1: BACKTRACKING

---

### 1. What is Backtracking?

**Plain English:**
Backtracking is a problem-solving strategy where you TRY a choice, and if it does
not work out, you UNDO that choice and try a different one. You keep trying until
you find a solution or exhaust all possibilities.

**Real-life Analogy — Solving Sudoku:**
You are solving a Sudoku puzzle. You reach an empty cell.
1. You try putting 1 — check if valid. If yes, move to the next empty cell.
2. Later you get stuck — no number works in a future cell.
3. You come BACK to the earlier cell, ERASE the 1, and try 2 instead.
4. Keep going until the entire puzzle is solved.

That erasing and trying again IS backtracking.

**Another Analogy — A Rat in a Maze:**
```
    S . . .
    . X X .
    . . . .
    X X . E

S = Start, E = End, X = Wall, . = Open
```
The rat starts at S and tries to reach E:
1. Go right → right → right → down → stuck (wall below)
2. BACKTRACK to previous position
3. Try down instead → and so on
4. Keep trying paths until finding one that reaches E

**Another Analogy — Trying Clothes at a Store:**
You walk into a clothing store. You pick a shirt, try it on. Does not fit? Take it
OFF (undo the choice) and try the next one. Found a good shirt? Now try pants.
Same process. If nothing matches the shirt, come back and try a different shirt.

**Technical Definition:**
Backtracking = DFS (Depth-First Search) + PRUNING (cutting off invalid paths early)

- DFS explores ALL paths to their end
- Backtracking stops EARLY when it knows a path cannot lead to a valid solution

---

### 2. Why Does Backtracking Exist?

Some problems require us to EXPLORE ALL POSSIBILITIES:
- Generate all subsets of a set
- Generate all permutations of a list
- Find all valid arrangements (N-Queens, Sudoku)
- Find all combinations that satisfy a condition

Brute force would try every single possibility. Backtracking is SMARTER brute force —
it prunes (cuts off) branches early when they cannot lead to valid solutions, saving
enormous amounts of time.

---

### 3. How Backtracking Works — The Three Steps

Every backtracking algorithm follows this pattern:

```
1. CHOOSE   — Pick one option from the available choices
2. EXPLORE  — Recursively try to build the solution with that choice
3. UNCHOOSE — Remove the choice (backtrack) and try the next option
```

Think of it as: "Make a choice, see what happens, undo if it didn't work out."

---

### 4. The Backtracking Template

```java
// The universal backtracking template
void backtrack(List<Integer> currentState, List<List<Integer>> results, /* other params */) {

    // BASE CASE: Is the current state a COMPLETE, VALID solution?
    if (isComplete(currentState)) {
        results.add(new ArrayList<>(currentState));  // add a COPY of current solution
        return;
    }

    // TRY EACH CHOICE
    for (int choice : availableChoices) {

        // PRUNING: Skip invalid choices early (this is what makes backtracking efficient)
        if (!isValid(choice, currentState)) {
            continue;
        }

        // CHOOSE — add this choice to our current solution
        currentState.add(choice);

        // EXPLORE — recurse with this choice made
        backtrack(currentState, results, /* updated params */);

        // UNCHOOSE — remove this choice (BACKTRACK!)
        currentState.remove(currentState.size() - 1);
    }
}
```

**The UNCHOOSE step is what makes it "backtracking."** Without it, each recursive
call would permanently modify the state. With it, we restore the state so we can
try the next option.

---

### 5. Visualizing with Decision Trees

The best way to understand backtracking is to draw the DECISION TREE.

#### Example: Generate All Subsets of [1, 2, 3]

At each level, we DECIDE: include this element or exclude it?

```
                        []
                      /     \
              include 1     exclude 1
                [1]           []
               /    \        /    \
          inc 2   exc 2   inc 2   exc 2
          [1,2]   [1]     [2]     []
         /    \   /  \   /   \   /  \
       i3  e3  i3  e3  i3  e3  i3  e3
    [1,2,3] [1,2] [1,3] [1] [2,3] [2] [3] []
```

**All subsets:** [], [1], [2], [3], [1,2], [1,3], [2,3], [1,2,3]
That is 2^3 = 8 subsets. For n elements, there are 2^n subsets.

**Java — Subsets:**
```java
// Generate all subsets of the given array
List<List<Integer>> subsets(int[] nums) {
    List<List<Integer>> result = new ArrayList<>();
    backtrackSubsets(nums, 0, new ArrayList<>(), result);
    return result;
}

void backtrackSubsets(int[] nums, int start, List<Integer> current, List<List<Integer>> result) {
    // Every state is a valid subset — add it!
    // (we add a COPY because 'current' keeps changing)
    result.add(new ArrayList<>(current));

    // Try including each remaining element
    for (int i = start; i < nums.length; i++) {
        current.add(nums[i]);                              // CHOOSE
        backtrackSubsets(nums, i + 1, current, result);    // EXPLORE (i+1 to avoid duplicates)
        current.remove(current.size() - 1);                // UNCHOOSE (backtrack!)
    }
}
```

---

#### Example: Generate All Permutations of [1, 2, 3]

At each level, we choose ONE of the REMAINING elements:

```
                            []
                    /        |        \
                  [1]       [2]       [3]
                /    \     /    \    /    \
            [1,2]  [1,3] [2,1] [2,3] [3,1] [3,2]
              |      |     |     |     |      |
          [1,2,3] [1,3,2] [2,1,3] [2,3,1] [3,1,2] [3,2,1]
```

**All permutations:** [1,2,3], [1,3,2], [2,1,3], [2,3,1], [3,1,2], [3,2,1]
That is 3! = 6 permutations. For n elements, there are n! permutations.

**Java — Permutations:**
```java
// Generate all permutations of the given array
List<List<Integer>> permute(int[] nums) {
    List<List<Integer>> result = new ArrayList<>();
    backtrackPermute(nums, new ArrayList<>(), new boolean[nums.length], result);
    return result;
}

void backtrackPermute(int[] nums, List<Integer> current, boolean[] used, List<List<Integer>> result) {
    // BASE CASE: permutation is complete when its length equals input length
    if (current.size() == nums.length) {
        result.add(new ArrayList<>(current));
        return;
    }

    // Try each unused element
    for (int i = 0; i < nums.length; i++) {
        if (used[i]) continue;     // skip elements already in this permutation

        used[i] = true;            // CHOOSE — mark as used
        current.add(nums[i]);      // CHOOSE — add to current permutation

        backtrackPermute(nums, current, used, result);  // EXPLORE

        current.remove(current.size() - 1);  // UNCHOOSE — remove from permutation
        used[i] = false;                     // UNCHOOSE — mark as available again
    }
}
```

---

### 6. Classic Backtracking Problem: N-Queens

**Problem:** Place N queens on an N x N chessboard so that NO two queens
attack each other. Queens attack in same row, column, and both diagonals.

**Walkthrough for N = 4:**

```
Step 1: Place Q in row 0, col 0
  . . . .        Q . . .
  . . . .   →    . . . .
  . . . .        . . . .
  . . . .        . . . .

Step 2: Row 1 — try col 0 (same column as Q, INVALID)
                  try col 1 (same diagonal as Q, INVALID)
                  try col 2 (VALID!)
  Q . . .
  . . Q .
  . . . .
  . . . .

Step 3: Row 2 — try col 0 (INVALID, diagonal with row1Q)
                  try col 1 (INVALID, diagonal with row0Q)
                  try col 2 (INVALID, same column)
                  try col 3 (INVALID, diagonal with row1Q)
  ALL INVALID → BACKTRACK! Remove queen from row 1.

Step 4: Row 1 — try col 3 (VALID!)
  Q . . .
  . . . Q
  . . . .
  . . . .

Step 5: Row 2 — try col 0 (INVALID, diagonal)
                  try col 1 (VALID!)
  Q . . .
  . . . Q
  . Q . .
  . . . .

Step 6: Row 3 — try col 0 (INVALID, same col as row0)
                  try col 1 (INVALID, same col as row2)
                  try col 2 (INVALID, diagonal with row1)
                  try col 3 (INVALID, same col as row1)
  ALL INVALID → BACKTRACK! Remove queen from row 2.

Step 7: Row 2 — try col 2 (INVALID), try col 3 (INVALID)
  ALL INVALID → BACKTRACK! Remove queen from row 1.
  Row 1 — no more columns to try → BACKTRACK! Remove queen from row 0.

Step 8: Place Q in row 0, col 1
  . Q . .
  . . . .
  . . . .
  . . . .

Step 9: Continue... eventually find:
  . Q . .       Solution 1
  . . . Q
  Q . . .
  . . Q .

  . . Q .       Solution 2
  Q . . .
  . . . Q
  . Q . .
```

**Java — N-Queens:**
```java
// N-Queens: find all valid placements of N queens on N×N board
List<List<String>> solveNQueens(int n) {
    List<List<String>> results = new ArrayList<>();
    // queens[row] = column where queen is placed in that row
    int[] queens = new int[n];
    Arrays.fill(queens, -1);
    backtrackQueens(n, 0, queens, results);
    return results;
}

void backtrackQueens(int n, int row, int[] queens, List<List<String>> results) {
    // BASE CASE: all rows have queens placed — valid solution!
    if (row == n) {
        results.add(buildBoard(queens, n));
        return;
    }

    // Try placing queen in each column of current row
    for (int col = 0; col < n; col++) {
        if (isValidPlacement(queens, row, col)) {
            queens[row] = col;                            // CHOOSE
            backtrackQueens(n, row + 1, queens, results); // EXPLORE next row
            queens[row] = -1;                             // UNCHOOSE (backtrack)
        }
    }
}

boolean isValidPlacement(int[] queens, int row, int col) {
    for (int prevRow = 0; prevRow < row; prevRow++) {
        int prevCol = queens[prevRow];

        // Same column?
        if (prevCol == col) return false;

        // Same diagonal? (row difference == column difference)
        if (Math.abs(prevRow - row) == Math.abs(prevCol - col)) return false;
    }
    return true;
}

List<String> buildBoard(int[] queens, int n) {
    List<String> board = new ArrayList<>();
    for (int row = 0; row < n; row++) {
        char[] rowChars = new char[n];
        Arrays.fill(rowChars, '.');
        rowChars[queens[row]] = 'Q';
        board.add(new String(rowChars));
    }
    return board;
}
```

---

### 7. Combination Sum — Another Classic

**Problem:** Given candidates = [2, 3, 6, 7] and target = 7,
find all unique combinations that sum to 7. Can reuse numbers.

**Decision tree (partial):**
```
                    target=7
                /    |     \      \
              2      3      6      7
            t=5    t=4    t=1    t=0 ✓ → [7]
           / | \   / | \    |
          2  3  6  2  3  6   (none ≤ 1 except... no valid)
        t=3 t=2 ...
        ...
```

**Java:**
```java
List<List<Integer>> combinationSum(int[] candidates, int target) {
    List<List<Integer>> result = new ArrayList<>();
    backtrackCombSum(candidates, target, 0, new ArrayList<>(), result);
    return result;
}

void backtrackCombSum(int[] candidates, int remaining, int start,
                      List<Integer> current, List<List<Integer>> result) {
    // BASE CASE: found a valid combination
    if (remaining == 0) {
        result.add(new ArrayList<>(current));
        return;
    }

    // BASE CASE: overshot the target
    if (remaining < 0) return;

    // Try each candidate starting from 'start' index
    // (start prevents duplicate combinations like [2,3] and [3,2])
    for (int i = start; i < candidates.length; i++) {
        current.add(candidates[i]);                                     // CHOOSE
        // Pass i (not i+1) because we CAN reuse the same number
        backtrackCombSum(candidates, remaining - candidates[i], i, current, result);  // EXPLORE
        current.remove(current.size() - 1);                             // UNCHOOSE
    }
}

// For candidates=[2,3,6,7], target=7:
// Result: [[2,2,3], [7]]
```

---

### 8. When to Use Backtracking — Triggers

| If you see this in the problem... | Think Backtracking |
|---|---|
| "Generate ALL subsets / combinations / permutations" | Yes |
| "Find ALL valid arrangements" | Yes |
| "Can you fill/place such that condition is satisfied" | Yes (Sudoku, N-Queens) |
| "Constraint satisfaction problem" | Yes |
| "All possible paths" | Yes |
| "Print / return all solutions" | Yes |

**Backtracking time complexity:** Usually exponential (O(2^n) for subsets, O(n!) for permutations).
But pruning makes it faster than brute force in practice.

---

## Part 2: GREEDY ALGORITHMS

---

### 1. What is a Greedy Algorithm?

**Plain English:**
A Greedy algorithm makes the LOCALLY BEST choice at each step, hoping that
these local choices lead to the GLOBALLY best solution.

**Real-life Analogy — Buffet Strategy:**
You are at a wedding buffet with limited plate space. Greedy strategy:
at each dish, take the ONE that looks MOST delicious to you right now.
You don't think about what dishes are coming up ahead — you just take
the best looking one at each moment.

Sometimes this works great (you end up with a delicious plate).
Sometimes it fails (you fill up on starters and miss the biryani).

**Another Analogy — Making Change:**
A cashier needs to give you 41 rupees change using minimum coins.
Available coins: 25, 10, 5, 1

Greedy approach:
1. Largest coin that fits: 25. Remaining: 41 - 25 = 16
2. Largest coin that fits: 10. Remaining: 16 - 10 = 6
3. Largest coin that fits: 5. Remaining: 6 - 5 = 1
4. Largest coin that fits: 1. Remaining: 1 - 1 = 0
Total: 4 coins (25 + 10 + 5 + 1). This IS optimal!

**Technical Definition:**
At each step, make the choice that looks best RIGHT NOW without considering
future consequences. Never go back and reconsider previous choices.

---

### 2. Does Greedy Always Work? NO!

This is the MOST IMPORTANT thing to understand about greedy algorithms.

**Example where greedy WORKS:** Coins [1, 5, 10, 25], amount = 41
- Greedy: 25 + 10 + 5 + 1 = 4 coins. Optimal!

**Example where greedy FAILS:** Coins [1, 3, 4], amount = 6
- Greedy: 4 + 1 + 1 = 3 coins
- Optimal: 3 + 3 = 2 coins (need DP to find this!)

**Why did greedy fail?** Taking the locally best coin (4) prevented us from finding
the globally best combination (3 + 3). Greedy cannot "undo" the choice of 4.

**Greedy works when:** The problem has the "GREEDY CHOICE PROPERTY" —
making the locally optimal choice at each step guarantees a globally optimal solution.

**How do you know if greedy works?** You have to PROVE it (or recognize the problem type).
For interviews, memorize the common greedy problem types.

---

### 3. Greedy vs DP — When to Use Which?

| | Greedy | Dynamic Programming |
|---|---|---|
| **Decision at each step** | Take the BEST option now | Try ALL options, pick best |
| **Reconsider?** | Never | Yes (stores results of all options) |
| **Speed** | Usually O(n log n) or O(n) | Usually O(n^2) or O(n * W) |
| **Correctness** | Only if greedy property holds | Always correct |
| **When to use** | Can PROVE local = global optimal | Cannot prove greedy works |
| **Implementation** | Simple — sort + scan | More complex — table/memoization |

**Rule of thumb:**
- Try greedy first (it is simpler and faster)
- If you cannot prove greedy works, or find a counterexample, use DP

---

### 4. Classic Greedy Pattern: Activity Selection / Interval Scheduling

**Problem:** Given N activities with start and end times, find the MAXIMUM number
of non-overlapping activities you can attend.

**Real-life:** You have multiple meetings today. Some overlap. Find the maximum
number of meetings you can attend (one at a time).

**Greedy strategy:** Sort by END TIME. Always pick the activity that ENDS earliest
(as long as it does not overlap with the previously selected activity).

**Why this works:** By choosing the earliest-ending activity, we leave the MOST
room for future activities. This is provably optimal.

**Example:**
```
Activities:  A(1,4), B(3,5), C(0,6), D(5,7), E(3,9), F(5,9), G(6,10), H(8,11)

Sorted by end time:
  A: |-----|           (1-4)
  B:   |-----|         (3-5)
  C: |---------|       (0-6)
  D:       |-----|     (5-7)
  E:   |---------|     (3-9)
  F:       |---------|  (5-9)
  G:        |---------|  (6-10)
  H:            |-----| (8-11)

Step 1: Pick A (ends at 4) ✓
Step 2: B starts at 3 < 4 → OVERLAP, skip
Step 3: C starts at 0 < 4 → OVERLAP, skip
Step 4: D starts at 5 >= 4 → NO OVERLAP, pick D ✓ (ends at 7)
Step 5: E starts at 3 < 7 → OVERLAP, skip
Step 6: F starts at 5 < 7 → OVERLAP, skip
Step 7: G starts at 6 < 7 → OVERLAP, skip
Step 8: H starts at 8 >= 7 → NO OVERLAP, pick H ✓ (ends at 11)

Selected: A, D, H (3 activities — this is the maximum!)
```

**Java — Activity Selection:**
```java
// Activity Selection — find maximum number of non-overlapping activities
// Each activity is represented as [start, end]
int activitySelection(int[][] activities) {
    // Step 1: Sort by END time (greedy: pick the one that ends earliest)
    Arrays.sort(activities, (a, b) -> a[1] - b[1]);

    int count = 1;                         // always pick the first activity
    int lastEnd = activities[0][1];        // end time of last selected activity

    // Step 2: Greedily pick non-overlapping activities
    for (int i = 1; i < activities.length; i++) {
        if (activities[i][0] >= lastEnd) { // start time >= last end time → no overlap
            count++;
            lastEnd = activities[i][1];    // update last end time
        }
        // else: overlaps with last selected activity, skip it
    }

    return count;
}

// Time: O(n log n) for sorting + O(n) for scanning = O(n log n)
// Space: O(1) extra (or O(n) for sorting depending on implementation)
```

**Step-by-step trace:**
```
Sorted activities by end time:
  [1,4], [3,5], [0,6], [5,7], [3,9], [5,9], [6,10], [8,11]

i=0: Pick [1,4]. count=1, lastEnd=4
i=1: [3,5] → 3 < 4, SKIP (overlaps)
i=2: [0,6] → 0 < 4, SKIP
i=3: [5,7] → 5 >= 4, PICK! count=2, lastEnd=7
i=4: [3,9] → 3 < 7, SKIP
i=5: [5,9] → 5 < 7, SKIP
i=6: [6,10] → 6 < 7, SKIP
i=7: [8,11] → 8 >= 7, PICK! count=3, lastEnd=11

Result: 3 non-overlapping activities
```

---

### 5. Common Greedy Patterns

#### Pattern 1: Interval Problems (sort by start or end time)

**Maximum non-overlapping intervals:** Sort by END time, greedily pick.
**Minimum number of intervals to remove for no overlap:** Sort by END time, count overlaps.
**Merge overlapping intervals:** Sort by START time, merge when current overlaps previous.

---

#### Pattern 2: Fractional Knapsack

**Problem:** Items have weight and value. Bag has capacity W. Maximize value.
You CAN take fractions of items (unlike 0/1 knapsack).

**Greedy strategy:** Sort by value/weight ratio (most valuable per kg first).
Take as much of the best item as possible, then next best, etc.

```java
// Fractional Knapsack — can take fractions of items
// items[i] = [value, weight]
double fractionalKnapsack(int[][] items, int capacity) {
    // Sort by value-to-weight ratio in descending order
    // (highest value per unit weight first)
    Arrays.sort(items, (a, b) -> Double.compare(
        (double) b[0] / b[1],   // ratio of item b
        (double) a[0] / a[1]    // ratio of item a
    ));

    double totalValue = 0;
    int remainingCapacity = capacity;

    for (int[] item : items) {
        int value = item[0], weight = item[1];

        if (weight <= remainingCapacity) {
            // Take the entire item
            totalValue += value;
            remainingCapacity -= weight;
        } else {
            // Take a fraction of this item (fill remaining capacity)
            totalValue += value * ((double) remainingCapacity / weight);
            break;  // bag is full
        }
    }

    return totalValue;
}
```

**Why greedy works here but NOT for 0/1 knapsack:**
- Fractional: You can always take the best bits of each item → greedy works
- 0/1: You must take the whole item or nothing → sometimes a less valuable item
  fits better with other items → need DP to explore all combinations

---

#### Pattern 3: Jump Game

**Problem:** Array of non-negative integers. Each element = max jump length from that position.
Can you reach the last index?

**Greedy strategy:** Track the FARTHEST position reachable. Scan left to right.

```java
// Jump Game — can you reach the last index?
boolean canJump(int[] nums) {
    int farthest = 0;  // farthest index reachable so far

    for (int i = 0; i < nums.length; i++) {
        if (i > farthest) return false;       // cannot reach this index — stuck!
        farthest = Math.max(farthest, i + nums[i]);  // update farthest reachable
    }

    return true;  // farthest >= last index (guaranteed by loop completing)
}

// Example: nums = [2, 3, 1, 1, 4]
// i=0: farthest = max(0, 0+2) = 2
// i=1: farthest = max(2, 1+3) = 4 → can reach index 4 (last index)!
// i=2: farthest = max(4, 2+1) = 4
// ...
// Result: true
```

---

#### Pattern 4: Assign Tasks / Meeting Rooms

**Problem:** Given meeting intervals, find the MINIMUM number of meeting rooms needed.

**Greedy strategy:** Sort by start time. Use a min-heap to track end times of ongoing meetings.
If the earliest-ending meeting ends before the new one starts, reuse that room.

```java
// Minimum meeting rooms needed
int minMeetingRooms(int[][] intervals) {
    Arrays.sort(intervals, (a, b) -> a[0] - b[0]);  // sort by start time

    // Min-heap stores end times of ongoing meetings
    // The smallest end time is at the top — the meeting ending soonest
    PriorityQueue<Integer> minHeap = new PriorityQueue<>();

    for (int[] interval : intervals) {
        // If the earliest-ending meeting ends before this one starts, reuse the room
        if (!minHeap.isEmpty() && minHeap.peek() <= interval[0]) {
            minHeap.poll();   // remove the finished meeting (free the room)
        }
        minHeap.offer(interval[1]);  // add this meeting's end time (occupy a room)
    }

    return minHeap.size();  // number of rooms currently in use = minimum rooms needed
}
```

---

### 6. Greedy vs Backtracking vs DP — The Big Picture

| | Greedy | Backtracking | DP |
|---|---|---|---|
| **Strategy** | Take best now | Try all, prune | Try all, cache |
| **Goes back?** | Never | Yes (undo choices) | No (stores all) |
| **Finds** | One solution (optimal if property holds) | All solutions | Optimal solution |
| **Speed** | Fastest (O(n log n) typical) | Slowest (exponential) | Middle (polynomial) |
| **Use when** | Can prove greedy works | Need ALL solutions | Need optimal, no greedy proof |

---

### 7. When to Use Greedy — Triggers

| If you see this in the problem... | Think Greedy |
|---|---|
| "Minimum number of intervals to remove" | Sort by end time, count non-overlapping |
| "Maximum number of non-overlapping intervals" | Sort by end time, greedy pick |
| "Merge overlapping intervals" | Sort by start time, merge |
| "Minimum meeting rooms / platforms" | Sort + min-heap |
| "Can you reach the end?" (jump game) | Track farthest reachable |
| "Assign tasks to minimize cost/time" | Sort + greedy assignment |
| "Fractional knapsack" (can take parts) | Sort by value/weight ratio |
| "Huffman encoding" | Greedy tree building |

**Red flags that greedy will NOT work:**
- "0/1 knapsack" (must take whole item or nothing) → use DP
- "Count ALL ways" → use DP or backtracking
- "Find ALL valid solutions" → use backtracking
- Counterexample exists where greedy gives wrong answer → use DP

---

### 8. Common Mistakes

#### Backtracking Mistakes:
1. **Forgetting to UNCHOOSE:** If you do not remove the choice after recursion, the state
   carries over to the next iteration, giving wrong results.
2. **Not making a COPY of the result:** `results.add(current)` adds a reference that keeps
   changing. Always do `results.add(new ArrayList<>(current))` to add a snapshot.
3. **Not handling duplicates:** If input has duplicates like [1, 1, 2], subsets will have
   duplicates too. Sort the array first, then skip same elements at the same level:
   ```java
   if (i > start && nums[i] == nums[i-1]) continue;  // skip duplicates
   ```
4. **Not pruning enough:** Without pruning, backtracking degenerates to brute force.
   Always add validity checks before the recursive call.

#### Greedy Mistakes:
1. **Assuming greedy always works:** It does NOT. Always verify with examples or prove
   the greedy choice property. Try to find a counterexample first.
2. **Wrong sorting criteria:** Interval scheduling must sort by END time (not start time).
   Fractional knapsack must sort by value/weight RATIO (not value alone).
3. **Not considering edge cases:** Empty input, single element, all elements same.
4. **Using greedy when DP is needed:** If the problem says "0/1 knapsack" or you find
   a counterexample where greedy gives a suboptimal answer, switch to DP.

---

### 9. Quick Summary

```
BACKTRACKING:
  - Try all possibilities: CHOOSE → EXPLORE → UNCHOOSE
  - DFS + pruning (cut off invalid paths early)
  - Returns ALL valid solutions
  - Time: usually exponential (O(2^n) or O(n!))
  - Use when: "generate all", "find all valid", constraint satisfaction

  Template:
    for each choice:
      if valid:
        make choice
        recurse
        undo choice

  Classic problems: Subsets, Permutations, Combination Sum, N-Queens, Sudoku

GREEDY:
  - Make the LOCALLY best choice at each step
  - Never go back and reconsider
  - Fast (usually O(n log n))
  - Only works when greedy choice property holds
  - Use when: interval scheduling, fractional knapsack, jump game

  Common patterns:
    - Sort by end time → pick non-overlapping (activity selection)
    - Sort by value/weight ratio → take best first (fractional knapsack)
    - Track farthest reachable → can you reach end? (jump game)

CHOOSING BETWEEN THEM:
  - Need ALL solutions? → Backtracking
  - Need OPTIMAL and can prove greedy works? → Greedy
  - Need OPTIMAL but greedy fails? → DP
  - Not sure? Try greedy, look for counterexample, fall back to DP

REMEMBER:
  - Backtracking: the UNDO step is what makes it backtracking
  - Greedy: PROVE it works or find a counterexample
  - Both are tools in your toolkit — know when each applies
```

---

*This completes Week 8! You now have the tools to tackle the most common algorithm patterns in coding interviews.*
