# Week 6: Graphs — BFS & DFS — How to ANSWER in a Microsoft Interview 🎤 (Java)

Welcome to interview boot camp! 🥊 This week is **graphs** — the topic that scares people but is secretly the most *pattern-driven* of them all. Once you recognize the trigger words, 90% of graph problems collapse into 3 templates: **BFS**, **DFS**, and **Topological Sort**. Let's make you unstoppable.

---

## 🏆 The Golden Rule of Every Interview Answer

**NEVER jump straight to code.** Microsoft interviewers grade your *communication* as much as your solution. Always walk this ladder:

> **Clarify → Brute Force → Optimal → Best → Complexity**

1. **Clarify** — ask questions so you solve the *right* problem.
2. **Brute Force** — state the naive idea out loud (shows you understand the problem).
3. **Optimal** — explain the smart approach *before* coding it.
4. **Best** — code the optimal cleanly, narrating as you go.
5. **Complexity** — finish with Time + Space. Always.

### 🗣️ Interview Script (what to literally say out loud)

1. *"Let me make sure I understand the problem..."* → restate it in your own words.
2. *"Can I clarify a few things?"* → ask 2–3 clarifying questions.
3. *"Let me start with a brute-force idea so we have a baseline..."* → describe it.
4. *"That's O(...). I think we can do better. The pattern here looks like a graph traversal — let me use BFS/DFS..."*
5. *"Let me dry-run my approach on your example before I trust it."*
6. *"Here's my implementation..."* → narrate while coding.
7. *"Time complexity is O(...), space is O(...). Edge cases I'd test are empty input, single node, disconnected graph."*

### ❓ Common Clarifying Questions for Graph Problems

- Is the graph **directed or undirected**?
- Can there be **cycles**? Self-loops? Duplicate edges?
- Is the graph **connected**, or could there be **isolated components**?
- For a grid: do **diagonals** count as neighbors, or just up/down/left/right?
- What should I return for **empty input** or a single node?
- Are node values **unique**? What's the **range** of node count (affects O(n²) vs O(n))?
- Can I **modify the input** (e.g., mark visited cells in-place to save space)?

---

## Problem 1: Number of Islands 🏝️ (Microsoft Top 5! ⭐)

### 📋 Full Question
Given a 2D grid of `'1'` (land) and `'0'` (water), count the number of **islands**. An island is land connected horizontally or vertically (not diagonally), surrounded by water.

**Example input:**
```
grid = [
  ['1','1','0','0'],
  ['1','1','0','0'],
  ['0','0','1','0'],
  ['0','0','0','1']
]
```
**Expected output:** `3` (top-left blob, the single `1` in the middle, and the single `1` bottom-right).

### 🗣️ What to say first
- "Are diagonals connected, or only up/down/left/right?" (Answer: only 4 directions.)
- "Can I modify the grid in place to mark visited cells?" (Usually yes — saves space.)
- "What do I return for an empty grid?" (Return 0.)

### Approach 1 — Brute Force (naive scan, no traversal)
There isn't really a "brute force" that avoids traversal — you *must* explore each land cell. The naive instinct is to try counting connected blobs by repeatedly scanning the whole grid for unvisited land. That's wasteful because you re-scan. The clean approach is: **scan once, and each time you hit unvisited land, flood-fill the entire island.**

> Why a better approach exists: a single linear scan + flood fill visits each cell at most a constant number of times, giving O(rows × cols).

### Approach 2 — Optimal (DFS Flood Fill 🌊)
**Idea:** Walk the grid. Every time you find a `'1'` you haven't visited, that's a *new island* → increment count, then DFS/BFS to "sink" the whole island (mark all connected land as `'0'`) so you don't count it again.

```java
class Solution {
    public int numIslands(char[][] grid) {
        if (grid == null || grid.length == 0) return 0;
        int rows = grid.length, cols = grid[0].length;
        int count = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == '1') {   // found new island
                    count++;
                    sink(grid, r, c);      // flood-fill it away
                }
            }
        }
        return count;
    }

    private void sink(char[][] grid, int r, int c) {
        // Out of bounds or water → stop
        if (r < 0 || c < 0 || r >= grid.length || c >= grid[0].length
                || grid[r][c] == '0') {
            return;
        }
        grid[r][c] = '0';          // mark visited by sinking
        sink(grid, r + 1, c);      // down
        sink(grid, r - 1, c);      // up
        sink(grid, r, c + 1);      // right
        sink(grid, r, c - 1);      // left
    }
}
```

**Dry-run on the example:**
- Scan (0,0) → `'1'` → count = 1 → sink the whole top-left 2×2 blob.
- Continue scanning... reach (2,2) → `'1'` → count = 2 → sink it (just one cell).
- Reach (3,3) → `'1'` → count = 3 → sink it.
- No more land → return **3**. ✅

**Time:** O(R × C) — each cell visited once. **Space:** O(R × C) worst case (recursion stack for one giant island).

### 🧠 Algorithm to Remember Forever
- **Pattern:** DFS / BFS **Flood Fill** on a grid.
- **Core idea:** Each unvisited land cell starts a new island; flood-fill drowns it so it's counted once.
- **Memory hook:** 🌊 You're a kid stamping out sandcastles on a beach. Each time you spot a castle, you smash the *whole* castle flat (flood fill) and add 1 to your tally.
- **Trigger phrase:** *"count connected regions in a grid"* → Flood Fill.

---

## Problem 2: Rotting Oranges 🍊 (Frequent!)

### 📋 Full Question
A grid where `0` = empty, `1` = fresh orange, `2` = rotten orange. Every minute, a rotten orange rots all **4-directionally adjacent** fresh oranges. Return the **minimum minutes** until no fresh orange remains, or `-1` if impossible.

**Example input:**
```
grid = [
  [2,1,1],
  [1,1,0],
  [0,1,1]
]
```
**Expected output:** `4`.

### 🗣️ What to say first
- "All rotten oranges rot simultaneously each minute, right?" (Yes — this screams **multi-source BFS**.)
- "Return -1 if some fresh orange can never be reached?" (Yes.)
- "What if there are zero fresh oranges at the start?" (Return 0.)

### Approach 1 — Brute Force (repeated full scans)
Simulate minute by minute: each minute, scan the whole grid, find all rotten oranges, rot their neighbors, repeat until nothing changes. This works but re-scans the entire grid every minute → O((R×C)²) in the worst case.

> Why a better approach exists: BFS from **all** rotten oranges at once processes each cell exactly once, turning the time spent per minute into total O(R×C).

### Approach 2 — Optimal (Multi-Source BFS 📡)
**Idea:** Put **every** initially-rotten orange into the queue at "minute 0." BFS level by level — each level = one minute. Count fresh oranges; decrement as they rot. If any remain, return -1.

```java
import java.util.*;

class Solution {
    public int orangesRotting(int[][] grid) {
        int rows = grid.length, cols = grid[0].length;
        Queue<int[]> queue = new LinkedList<>();
        int fresh = 0;

        // Seed queue with ALL rotten oranges (multi-source)
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == 2) queue.offer(new int[]{r, c});
                else if (grid[r][c] == 1) fresh++;
            }
        }

        if (fresh == 0) return 0;   // nothing to rot

        int minutes = 0;
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};

        while (!queue.isEmpty() && fresh > 0) {
            minutes++;
            int size = queue.size();          // process one full "minute" level
            for (int i = 0; i < size; i++) {
                int[] cell = queue.poll();
                for (int[] d : dirs) {
                    int nr = cell[0] + d[0], nc = cell[1] + d[1];
                    if (nr >= 0 && nc >= 0 && nr < rows && nc < cols
                            && grid[nr][nc] == 1) {
                        grid[nr][nc] = 2;     // rot it
                        fresh--;
                        queue.offer(new int[]{nr, nc});
                    }
                }
            }
        }
        return fresh == 0 ? minutes : -1;
    }
}
```

**Dry-run on the example:**
- Minute 0: queue = [(0,0)]. fresh = 6.
- Minute 1: rot (0,1) and (1,0). fresh = 4.
- Minute 2: rot (0,2),(1,1). fresh = 2.
- Minute 3: rot (2,1). fresh = 1.
- Minute 4: rot (2,2). fresh = 0 → return **4**. ✅

**Time:** O(R × C). **Space:** O(R × C) for the queue.

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Multi-Source BFS** (level-order traversal = time).
- **Core idea:** Seed the queue with all sources at once; each BFS level is one tick of time.
- **Memory hook:** 📡 Multiple radio towers broadcasting at once — the signal spreads outward in synchronized rings.
- **Trigger phrase:** *"spread / minimum time / simultaneously from multiple starting points"* → Multi-Source BFS.

---

## Problem 3: Course Schedule 📚 (Very Frequent!)

### 📋 Full Question
There are `numCourses` courses (0 to n-1). `prerequisites[i] = [a, b]` means you must take `b` before `a`. Return `true` if you can finish all courses (i.e., **no cycle**), else `false`.

**Example input:** `numCourses = 4`, `prerequisites = [[1,0],[2,1],[3,2]]`
**Expected output:** `true` (take 0→1→2→3).
With `[[1,0],[0,1]]` → `false` (cycle).

### 🗣️ What to say first
- "This is detecting whether a **directed graph has a cycle**, correct?"
- "Can prerequisites contain duplicates or self-loops?" ([a,a] = instant cycle → false.)
- "Edges point prerequisite → course, or course → prerequisite? Let me define my direction clearly."

### Approach 1 — Brute Force (DFS cycle check from each node)
Run a DFS from every node tracking the current recursion path; if you revisit a node on the *current* path, there's a cycle. Works but if not careful with memoization you re-explore nodes repeatedly.

> Why a better approach exists: Kahn's algorithm (BFS topological sort) is iterative (no stack-overflow risk), naturally handles disconnected graphs, and is often easier to explain — each node processed once.

### Approach 2 — Optimal (Topological Sort via Kahn's BFS 🎓)
**Idea:** Compute **in-degree** (number of prerequisites) for every course. Queue all courses with in-degree 0 (no prereqs). Process them, "removing" their outgoing edges and decrementing neighbors' in-degrees. If a neighbor hits 0, queue it. If we process all `numCourses` → no cycle.

```java
import java.util.*;

class Solution {
    public boolean canFinish(int numCourses, int[][] prerequisites) {
        // Build adjacency list + in-degree array
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) adj.add(new ArrayList<>());
        int[] indegree = new int[numCourses];

        for (int[] pre : prerequisites) {
            int course = pre[0], prereq = pre[1];
            adj.get(prereq).add(course);   // prereq → course
            indegree[course]++;
        }

        // Queue all courses with no prerequisites
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < numCourses; i++) {
            if (indegree[i] == 0) queue.offer(i);
        }

        int taken = 0;
        while (!queue.isEmpty()) {
            int course = queue.poll();
            taken++;
            for (int next : adj.get(course)) {
                if (--indegree[next] == 0) queue.offer(next);
            }
        }

        return taken == numCourses;   // all taken ⇒ no cycle
    }
}
```

**Dry-run on `[[1,0],[2,1],[3,2]]`:**
- in-degree: [0,1,1,1]. Queue = [0].
- Take 0 → taken=1 → decrement 1 → in-degree[1]=0 → queue 1.
- Take 1 → taken=2 → in-degree[2]=0 → queue 2.
- Take 2 → taken=3 → in-degree[3]=0 → queue 3.
- Take 3 → taken=4. taken == 4 → **true**. ✅

**Time:** O(V + E). **Space:** O(V + E).

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Topological Sort** (Kahn's BFS) — also detects cycles.
- **Core idea:** Repeatedly remove nodes with zero in-degree; if you can remove all of them, there's no cycle.
- **Memory hook:** 🎓 Peeling an onion of dependencies — keep peeling the layers that depend on nothing. If a core never peels, you're stuck in a cycle.
- **Trigger phrase:** *"order of tasks with dependencies"* or *"can you finish / is there a cycle in a directed graph"* → Topological Sort.

---

## Problem 4: Course Schedule II 📝

### 📋 Full Question
Same setup as Course Schedule, but return a **valid ordering** of courses to take. If impossible (cycle), return an **empty array**.

**Example input:** `numCourses = 4`, `prerequisites = [[1,0],[2,0],[3,1],[3,2]]`
**Expected output:** `[0,1,2,3]` (or any valid topological order, e.g., `[0,2,1,3]`).

### 🗣️ What to say first
- "Any valid order is fine, or a specific one?" (Usually any valid order.)
- "Return empty array if there's a cycle, right?"

### Approach 1 — Brute Force
Try permutations and check each respects all prerequisites → O(n!). Absurd. Mentioning it only shows you know it's a search-space explosion.

> Why a better approach exists: Topological sort directly *constructs* a valid order in linear time — no need to search.

### Approach 2 — Optimal (Kahn's BFS, record the order 📝)
**Idea:** Identical to Course Schedule, but every time you pop a course, **append it to the result list**. If the result contains all courses, return it; otherwise return empty.

```java
import java.util.*;

class Solution {
    public int[] findOrder(int numCourses, int[][] prerequisites) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) adj.add(new ArrayList<>());
        int[] indegree = new int[numCourses];

        for (int[] pre : prerequisites) {
            adj.get(pre[1]).add(pre[0]);
            indegree[pre[0]]++;
        }

        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < numCourses; i++) {
            if (indegree[i] == 0) queue.offer(i);
        }

        int[] order = new int[numCourses];
        int idx = 0;
        while (!queue.isEmpty()) {
            int course = queue.poll();
            order[idx++] = course;            // record the order
            for (int next : adj.get(course)) {
                if (--indegree[next] == 0) queue.offer(next);
            }
        }

        // If we couldn't take all courses, there's a cycle
        return idx == numCourses ? order : new int[0];
    }
}
```

**Dry-run on `[[1,0],[2,0],[3,1],[3,2]]`:**
- in-degree: [0,1,1,2]. Queue=[0].
- Pop 0 → order=[0] → in-degree[1]=0, in-degree[2]=0 → queue [1,2].
- Pop 1 → order=[0,1] → in-degree[3]=1.
- Pop 2 → order=[0,1,2] → in-degree[3]=0 → queue [3].
- Pop 3 → order=[0,1,2,3]. idx==4 → **[0,1,2,3]**. ✅

**Time:** O(V + E). **Space:** O(V + E).

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Topological Sort** — output the dequeue order.
- **Core idea:** The order in which Kahn's algorithm removes zero-in-degree nodes *is* the valid schedule.
- **Memory hook:** 📝 A morning to-do list where you can only do a task once everything it depends on is checked off.
- **Trigger phrase:** *"return a valid ordering with dependencies"* → Topological Sort (record order).

---

## Problem 5: Clone Graph 🧬

### 📋 Full Question
Given a reference to a node in a **connected undirected graph**, return a **deep copy** (clone). Each node has a value and a list of neighbors.

**Example input:** adjacency `1—2, 1—4, 2—3, 3—4` (a 4-node cycle), given node `1`.
**Expected output:** an identical but **brand-new** graph (different objects, same structure).

### 🗣️ What to say first
- "Is the graph connected, so I can reach all nodes from the given one?" (Yes.)
- "Node values are unique?" (Yes — handy for a HashMap key.)
- "Can the input be null (empty graph)?" (Return null.)

### Approach 1 — Brute Force (no map → infinite loop ⚠️)
Naively DFS and create a new node for each neighbor — but without remembering what you've already cloned, cycles cause **infinite recursion** and you create duplicate copies. So you *must* track visited→clone mapping.

> Why a better approach exists: a HashMap from original→clone both prevents infinite loops *and* ensures each node is cloned exactly once.

### Approach 2 — Optimal (DFS + HashMap 🗺️)
**Idea:** Keep a map `original → clone`. When visiting a node: if already cloned, return the clone; otherwise create the clone, store it in the map *before* recursing (to break cycles), then clone each neighbor.

```java
import java.util.*;

// Definition for a Node.
class Node {
    public int val;
    public List<Node> neighbors;
    public Node(int val) { this.val = val; this.neighbors = new ArrayList<>(); }
}

class Solution {
    private Map<Node, Node> cloned = new HashMap<>();

    public Node cloneGraph(Node node) {
        if (node == null) return null;

        // Already cloned? Return the existing copy (handles cycles)
        if (cloned.containsKey(node)) return cloned.get(node);

        Node copy = new Node(node.val);
        cloned.put(node, copy);                 // store BEFORE recursing

        for (Node neighbor : node.neighbors) {
            copy.neighbors.add(cloneGraph(neighbor));
        }
        return copy;
    }
}
```

**Dry-run on the 4-node cycle:**
- Clone 1 → put {1→1'}. Recurse into neighbor 2.
- Clone 2 → put {2→2'}. Recurse into neighbor 1 → already in map → return 1'. Recurse into 3.
- Clone 3 → put {3→3'}. Neighbor 2 → in map → 2'. Neighbor 4 → clone 4.
- Clone 4 → neighbors 1 and 3 both already in map. Unwind.
- Result: a complete, independent copy. ✅

**Time:** O(V + E). **Space:** O(V) for the map + recursion.

### 🧠 Algorithm to Remember Forever
- **Pattern:** DFS/BFS traversal with a **visited→clone HashMap**.
- **Core idea:** Map each original node to its clone so cycles don't loop forever and nodes aren't duplicated.
- **Memory hook:** 🧬 Photocopying a family tree where cousins are interconnected — keep a "who-have-I-copied" ledger so you don't re-photocopy the same person.
- **Trigger phrase:** *"deep copy a graph"* or *"clone with cycles"* → DFS + HashMap.

---

## Problem 6: Pacific Atlantic Water Flow 🌊

### 📋 Full Question
An `m × n` grid of heights. Water flows from a cell to a neighbor (4-directional) if the neighbor's height is **≤** current. The Pacific touches the **top + left** edges; the Atlantic touches the **bottom + right** edges. Return all cells from which water can reach **both** oceans.

**Example input:**
```
heights = [
 [1,2,2,3,5],
 [3,2,3,4,4],
 [2,4,5,3,1],
 [6,7,1,4,5],
 [5,1,1,2,4]
]
```
**Expected output:** `[[0,4],[1,3],[1,4],[2,2],[3,0],[3,1],[4,0]]` (cells reaching both oceans).

### 🗣️ What to say first
- "Water flows from high to **equal-or-lower**, correct?" (Yes, `≤`.)
- "A cell on an ocean edge already touches that ocean?" (Yes.)
- "Order of output cells doesn't matter?" (Usually no.)

### Approach 1 — Brute Force (DFS from every cell)
From each of the m×n cells, DFS to see if it can reach the Pacific and (separately) the Atlantic. That's O((m×n)²) — way too slow for big grids.

> Why a better approach exists: instead of asking "can each cell reach an ocean," **reverse the flow** — start at the oceans and climb uphill. One DFS per ocean covers everything.

### Approach 2 — Optimal (Reverse DFS from oceans ⛰️)
**Idea:** Start DFS from all **Pacific-edge** cells, climbing *uphill* (neighbor ≥ current), marking everything reachable. Do the same from all **Atlantic-edge** cells. Cells in **both** reachable sets are the answer.

```java
import java.util.*;

class Solution {
    private int rows, cols;
    private int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};

    public List<List<Integer>> pacificAtlantic(int[][] heights) {
        List<List<Integer>> result = new ArrayList<>();
        if (heights == null || heights.length == 0) return result;
        rows = heights.length; cols = heights[0].length;

        boolean[][] pacific = new boolean[rows][cols];
        boolean[][] atlantic = new boolean[rows][cols];

        // Pacific = top row + left col; Atlantic = bottom row + right col
        for (int c = 0; c < cols; c++) {
            dfs(heights, 0, c, pacific);          // top edge
            dfs(heights, rows - 1, c, atlantic);  // bottom edge
        }
        for (int r = 0; r < rows; r++) {
            dfs(heights, r, 0, pacific);          // left edge
            dfs(heights, r, cols - 1, atlantic);  // right edge
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (pacific[r][c] && atlantic[r][c]) {
                    result.add(Arrays.asList(r, c));
                }
            }
        }
        return result;
    }

    // Climb uphill from the ocean: neighbor must be >= current height
    private void dfs(int[][] h, int r, int c, boolean[][] reach) {
        reach[r][c] = true;
        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            if (nr >= 0 && nc >= 0 && nr < rows && nc < cols
                    && !reach[nr][nc] && h[nr][nc] >= h[r][c]) {
                dfs(h, nr, nc, reach);
            }
        }
    }
}
```

**Dry-run (intuition):** From the top and left edges we mark every cell water could *drain down to* the Pacific from (climbing up means it can flow back down). Same for bottom/right and the Atlantic. The overlap — cells like `[2,2]` (height 5, a high point) — drains to both. ✅

**Time:** O(R × C) — each cell visited at most twice (once per ocean). **Space:** O(R × C).

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Reverse DFS/BFS from targets** (multi-source from edges).
- **Core idea:** Flip the problem — instead of "can this reach the ocean," ask "what can the ocean reach by climbing uphill."
- **Memory hook:** ⛰️ Don't trace every raindrop downhill; stand at the sea and walk *up* the mountain to mark everywhere water could've come from.
- **Trigger phrase:** *"which cells can reach multiple destinations"* → Reverse traversal from each destination.

---

## Problem 7: Surrounded Regions 🔲

### 📋 Full Question
Given an `m × n` board of `'X'` and `'O'`, capture all regions **surrounded** by `'X'` by flipping their `'O'`s to `'X'`. An `'O'` is **safe** if it's connected (4-directionally) to an `'O'` on the **border**.

**Example input:**
```
[['X','X','X','X'],
 ['X','O','O','X'],
 ['X','X','O','X'],
 ['X','O','X','X']]
```
**Expected output:**
```
[['X','X','X','X'],
 ['X','X','X','X'],
 ['X','X','X','X'],
 ['X','O','X','X']]
```
(The bottom-left `'O'` touches the border, so it survives; the inner ones get captured.)

### 🗣️ What to say first
- "An `'O'` survives only if it connects to a border `'O'`, right?"
- "Can I mutate the board in place?" (Yes.)

### Approach 1 — Brute Force (check each region for border contact)
For each `'O'` region, flood-fill and check if *any* cell touches the border; if not, flip it. Doable but bookkeeping-heavy and you may re-scan regions.

> Why a better approach exists: **invert the logic** — mark the *safe* ones first (border-connected), then flip everything else in one pass.

### Approach 2 — Optimal (Border DFS + flip 🚩)
**Idea:** DFS from every `'O'` on the **border**, marking those (and everything connected) as safe with a temp marker `'#'`. Then sweep the board: any remaining `'O'` is surrounded → flip to `'X'`; restore `'#'` back to `'O'`.

```java
class Solution {
    private int rows, cols;

    public void solve(char[][] board) {
        if (board == null || board.length == 0) return;
        rows = board.length; cols = board[0].length;

        // Mark border-connected 'O's as safe ('#')
        for (int r = 0; r < rows; r++) {
            mark(board, r, 0);
            mark(board, r, cols - 1);
        }
        for (int c = 0; c < cols; c++) {
            mark(board, 0, c);
            mark(board, rows - 1, c);
        }

        // Capture surrounded, restore safe
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (board[r][c] == 'O') board[r][c] = 'X';   // surrounded
                else if (board[r][c] == '#') board[r][c] = 'O'; // safe
            }
        }
    }

    private void mark(char[][] board, int r, int c) {
        if (r < 0 || c < 0 || r >= rows || c >= cols || board[r][c] != 'O') {
            return;
        }
        board[r][c] = '#';            // temp safe marker
        mark(board, r + 1, c);
        mark(board, r - 1, c);
        mark(board, r, c + 1);
        mark(board, r, c - 1);
    }
}
```

**Dry-run on the example:**
- Border DFS finds the bottom-left `'O'` at (3,1) → mark it `'#'`. No other border `'O'`s connect inward.
- Sweep: inner `'O'`s at (1,1),(1,2),(2,2) → flip to `'X'`. The `'#'` at (3,1) → back to `'O'`. ✅

**Time:** O(R × C). **Space:** O(R × C) recursion worst case.

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Border DFS / invert-the-problem** flood fill.
- **Core idea:** Find what's *safe* (touches the edge) first; capture everything else.
- **Memory hook:** 🚩 In a game of Go/Othello, stones touching the wall can't be surrounded — protect the wall-huggers, flip the trapped middle.
- **Trigger phrase:** *"regions surrounded / enclosed unless touching the border"* → Border DFS.

---

## Problem 8: Network Delay Time ⏱️ (Dijkstra)

### 📋 Full Question
A network of `n` nodes (1..n). `times[i] = [u, v, w]` = a directed edge from `u` to `v` taking `w` time. A signal starts at node `k`. Return the **time for all nodes to receive** the signal, or `-1` if some node is unreachable.

**Example input:** `times = [[2,1,1],[2,3,1],[3,4,1]]`, `n = 4`, `k = 2`
**Expected output:** `2` (longest shortest-path: 2→3→4 takes 2).

### 🗣️ What to say first
- "Edges are directed and weights are non-negative?" (Yes — non-negative means **Dijkstra** is valid.)
- "Return -1 if any node can't be reached from k?"
- "The answer is the **maximum** of all shortest distances?" (Yes — last node to hear the signal.)

### Approach 1 — Brute Force (Bellman-Ford / repeated relaxation)
Relax all edges `n−1` times → O(V × E). Correct and even handles negative edges, but slower than Dijkstra for non-negative weights.

> Why a better approach exists: with **non-negative weights**, Dijkstra with a min-heap greedily locks in the closest node each step → O(E log V), faster.

### Approach 2 — Optimal (Dijkstra with min-heap 🎯)
**Idea:** Min-heap of `(distance, node)`. Pop the closest unsettled node, settle its distance, relax its edges. The answer is the max settled distance (or -1 if some node never settles).

```java
import java.util.*;

class Solution {
    public int networkDelayTime(int[][] times, int n, int k) {
        // Build adjacency list: node -> list of (neighbor, weight)
        Map<Integer, List<int[]>> adj = new HashMap<>();
        for (int[] t : times) {
            adj.computeIfAbsent(t[0], x -> new ArrayList<>())
               .add(new int[]{t[1], t[2]});
        }

        // Min-heap ordered by distance
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]);
        pq.offer(new int[]{0, k});          // (dist, node)

        Map<Integer, Integer> dist = new HashMap<>();
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int d = cur[0], node = cur[1];
            if (dist.containsKey(node)) continue;   // already settled
            dist.put(node, d);
            for (int[] edge : adj.getOrDefault(node, new ArrayList<>())) {
                int next = edge[0], w = edge[1];
                if (!dist.containsKey(next)) {
                    pq.offer(new int[]{d + w, next});
                }
            }
        }

        if (dist.size() != n) return -1;    // some node unreachable
        int max = 0;
        for (int d : dist.values()) max = Math.max(max, d);
        return max;
    }
}
```

**Dry-run on the example:**
- Heap=[(0,2)]. Pop (0,2) → dist[2]=0 → push (1,1),(1,3).
- Pop (1,1) → dist[1]=1 (no out-edges).
- Pop (1,3) → dist[3]=1 → push (2,4).
- Pop (2,4) → dist[4]=2.
- dist has all 4 nodes. max = **2**. ✅

**Time:** O(E log V). **Space:** O(V + E).

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Dijkstra's shortest path** (greedy + min-heap).
- **Core idea:** Always expand the nearest unsettled node; non-negative weights guarantee it's final.
- **Memory hook:** 🎯 A GPS that always explores the cheapest-so-far route first, locking in arrivals one city at a time.
- **Trigger phrase:** *"shortest path with weighted edges, non-negative"* → Dijkstra.

---

## 📊 Complexity Cheat Sheet

| Problem | Brute Force | Optimal | Technique |
|---|---|---|---|
| Number of Islands | O(R·C) scan w/ re-visits | **O(R·C)** | DFS/BFS Flood Fill |
| Rotting Oranges | O((R·C)²) repeated scans | **O(R·C)** | Multi-Source BFS |
| Course Schedule | O(V+E) DFS w/ care | **O(V+E)** | Topological Sort (Kahn's BFS) |
| Course Schedule II | O(n!) permutations | **O(V+E)** | Topological Sort (record order) |
| Clone Graph | ∞ (loops w/o map) | **O(V+E)** | DFS/BFS + HashMap |
| Pacific Atlantic | O((R·C)²) | **O(R·C)** | Reverse DFS from oceans |
| Surrounded Regions | O(R·C) w/ rescans | **O(R·C)** | Border DFS (invert) |
| Network Delay Time | O(V·E) Bellman-Ford | **O(E log V)** | Dijkstra (min-heap) |

---

## 🧠🧠 ALGORITHM RECOGNITION TABLE

> **The interviewer's words are your cheat code.** Train your ears for these triggers.

| If the question says… (Trigger) | Use this Algorithm | Memory Hook | Big-O |
|---|---|---|---|
| "count connected regions in a grid" | **Flood Fill (DFS/BFS)** | 🌊 Smash each sandcastle flat | O(R·C) |
| "shortest path in unweighted graph / grid" | **BFS** | 🌊 Ripples in a pond, level by level | O(V+E) |
| "minimum time to spread from many sources" | **Multi-Source BFS** | 📡 Many radio towers broadcasting | O(R·C) |
| "order tasks with dependencies / can finish / cycle in directed graph" | **Topological Sort (Kahn's)** | 🎓 Peel the dependency onion | O(V+E) |
| "deep copy a graph / clone with cycles" | **DFS + HashMap** | 🧬 Photocopy with a ledger | O(V+E) |
| "cells reaching multiple destinations" | **Reverse DFS from targets** | ⛰️ Walk uphill from the sea | O(R·C) |
| "regions enclosed unless touching border" | **Border DFS (invert)** | 🚩 Protect the wall-huggers | O(R·C) |
| "shortest path, weighted, non-negative edges" | **Dijkstra (min-heap)** | 🎯 GPS picks cheapest route first | O(E log V) |
| "cheapest path within K stops" | **BFS/Bellman-Ford (bounded)** | 🛫 Limited layovers | O(K·E) |
| "transform word one letter at a time, fewest steps" | **BFS (word graph)** | 🪜 Climb a word ladder rung by rung | O(N·L²) |
| "detect cycle in undirected graph / valid tree" | **Union-Find or DFS** | 🌳 n-1 edges & all connected = tree | O(E·α(V)) |

---

## ☕ Java Tips for Graph Problems

**1. Build an adjacency list cleanly:**
```java
Map<Integer, List<Integer>> adj = new HashMap<>();
for (int[] e : edges) {
    adj.computeIfAbsent(e[0], k -> new ArrayList<>()).add(e[1]);
    adj.computeIfAbsent(e[1], k -> new ArrayList<>()).add(e[0]); // undirected
}
```
`computeIfAbsent` saves you the "if key missing, create list" boilerplate. 🙌

**2. BFS with a Queue (use LinkedList):**
```java
Queue<Integer> queue = new LinkedList<>();
queue.offer(start);          // add to back
int node = queue.poll();     // remove from front
```
For multi-source / level-by-level BFS, snapshot `int size = queue.size();` **before** the inner loop so you process exactly one level.

**3. Visited set — pick the right tool:**
```java
Set<Integer> visited = new HashSet<>();      // for node-id graphs
boolean[][] visited = new boolean[rows][cols]; // for grids (faster, no hashing)
```
On grids, you can also mark visited *in place* (sink `'1'`→`'0'`) to use O(1) extra space — but mention you're mutating the input first!

**4. Grid directions array — write it once, reuse everywhere:**
```java
int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};   // down, up, right, left
for (int[] d : dirs) {
    int nr = r + d[0], nc = c + d[1];
    if (nr >= 0 && nc >= 0 && nr < rows && nc < cols && !visited[nr][nc]) {
        // explore (nr, nc)
    }
}
```
For 8-directional (diagonals), add `{1,1},{1,-1},{-1,1},{-1,-1}`.

**5. Min-heap (PriorityQueue) for Dijkstra:**
```java
PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]); // by distance
```
Always store `{distance, node}` and order by distance. Use `Integer.compare(a[0], b[0])` if overflow is a worry.

**6. Recursion depth:** Java's default stack can overflow on a ~10⁴+ deep DFS (e.g., a giant single island). Mention you'd switch to an explicit `Deque<int[]>` stack (iterative DFS) for very large inputs.

---

## ✅ Final Interview Checklist

Before you say "I'm done," tick every box:

- [ ] 🗣️ **Restated the problem** and asked clarifying questions (directed? cycles? grid diagonals?).
- [ ] 🧩 **Identified the pattern** out loud (BFS / DFS / Topo Sort / Dijkstra) using a trigger word.
- [ ] 📉 **Mentioned brute force** and why the optimal beats it.
- [ ] 🧪 **Dry-ran** the optimal on the given example before trusting it.
- [ ] 🛡️ **Handled edge cases**: empty input, single node, disconnected graph, cycle, unreachable nodes.
- [ ] 🚫 **Used a visited set / in-degree / HashMap** to avoid infinite loops and re-processing.
- [ ] ⏱️ **Stated Time AND Space complexity** in terms of V and E (or R and C).
- [ ] 🧹 **Clean code**: meaningful names, `computeIfAbsent`, a single `dirs` array, no magic numbers.
- [ ] 🔁 **Offered a tradeoff**: "BFS gives shortest path; DFS is simpler for connectivity" — shows depth.
- [ ] 😊 **Stayed calm & narrated** — interviewers hire communicators, not silent typers.

**Remember:** Graphs reward *recognition*. Hear the trigger → name the pattern → reach for the template → adapt. You've got this! 🚀
