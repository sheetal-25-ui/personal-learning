# Week 6: Graphs — BFS & DFS — How to ANSWER in a Microsoft Interview 🎤 (Java)

Welcome to interview boot camp! 🥊 This week is **graphs** — the topic that scares people but is secretly the most *pattern-driven* of them all. Once you recognize the trigger words, 95% of graph problems collapse into a handful of templates: **BFS**, **DFS**, **Topological Sort**, **Union-Find**, and **Dijkstra**. Let's make you unstoppable.

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
4. *"That's O(...). I think we can do better. The pattern here looks like a graph traversal — let me use BFS/DFS/topo-sort/Dijkstra..."*
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
- Are edge weights **non-negative** (Dijkstra) or can they be negative (Bellman-Ford)?

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
There isn't really a "brute force" that avoids traversal — you *must* explore each land cell. The naive instinct is to repeatedly scan the whole grid for unvisited land. That's wasteful. The clean approach is: **scan once, and each time you hit unvisited land, flood-fill the entire island.**

> Why a better approach exists: a single linear scan + flood fill visits each cell a constant number of times → O(rows × cols). **Alternative:** Union-Find (union adjacent land cells, count roots) — also O(R·C·α).

### Approach 2 — Optimal (DFS Flood Fill 🌊)
**Idea:** Walk the grid. Every time you find a `'1'` you haven't visited, that's a *new island* → increment count, then DFS to "sink" the whole island (mark all connected land as `'0'`) so you don't count it again.

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
- Reach (2,2) → `'1'` → count = 2 → sink it.
- Reach (3,3) → `'1'` → count = 3 → sink it.
- Return **3**. ✅

**Time:** O(R × C). **Space:** O(R × C) worst case (recursion stack for one giant island).

### 🧠 Algorithm to Remember Forever
- **Pattern:** DFS / BFS **Flood Fill** on a grid.
- **Core idea:** Each unvisited land cell starts a new island; flood-fill drowns it so it's counted once.
- **Memory hook:** 🌊 A kid stamping out sandcastles — spot a castle, smash the *whole* castle flat, add 1.
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
Simulate minute by minute: each minute, scan the whole grid, rot neighbors of rotten cells, repeat until nothing changes. Works but re-scans every minute → O((R×C)²).

> Why a better approach exists: BFS from **all** rotten oranges at once processes each cell exactly once → total O(R×C).

### Approach 2 — Optimal (Multi-Source BFS 📡)
**Idea:** Put **every** initially-rotten orange into the queue at "minute 0." BFS level by level — each level = one minute. Count fresh oranges; decrement as they rot. If any remain, return -1.

```java
import java.util.*;

class Solution {
    public int orangesRotting(int[][] grid) {
        int rows = grid.length, cols = grid[0].length;
        Queue<int[]> queue = new LinkedList<>();
        int fresh = 0;

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
            int size = queue.size();          // one full "minute" level
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
- Minute 1: rot (0,1),(1,0). fresh = 4.
- Minute 2: rot (0,2),(1,1). fresh = 2.
- Minute 3: rot (2,1). fresh = 1.
- Minute 4: rot (2,2). fresh = 0 → return **4**. ✅

**Time:** O(R × C). **Space:** O(R × C) for the queue.

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Multi-Source BFS** (level-order traversal = time).
- **Core idea:** Seed the queue with all sources at once; each BFS level is one tick of time.
- **Memory hook:** 📡 Multiple radio towers broadcasting at once — signal spreads in synchronized rings.
- **Trigger phrase:** *"spread / minimum time / simultaneously from multiple starting points"* → Multi-Source BFS.

---

## Problem 3: Clone Graph 🧬

### 📋 Full Question
Given a reference to a node in a **connected undirected graph**, return a **deep copy** (clone). Each node has a value and a list of neighbors.

**Example input:** adjacency `1—2, 1—4, 2—3, 3—4` (a 4-node cycle), given node `1`.
**Expected output:** an identical but **brand-new** graph (different objects, same structure).

### 🗣️ What to say first
- "Is the graph connected, so I can reach all nodes from the given one?" (Yes.)
- "Node values are unique?" (Yes — handy for a HashMap key.)
- "Can the input be null (empty graph)?" (Return null.)

### Approach 1 — Brute Force (no map → infinite loop ⚠️)
Naively DFS and create a new node for each neighbor — but without remembering what you've already cloned, cycles cause **infinite recursion** and duplicate copies. You *must* track an original→clone mapping.

> Why a better approach exists: a HashMap from original→clone both prevents infinite loops *and* ensures each node is cloned exactly once. **Alternative:** BFS with the same map instead of recursion.

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

        if (cloned.containsKey(node)) return cloned.get(node);  // handles cycles

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
- Clone 1 → put {1→1'}. Recurse into 2.
- Clone 2 → put {2→2'}. Neighbor 1 → already in map → return 1'. Recurse into 3.
- Clone 3 → put {3→3'}. Neighbor 2 → 2'. Neighbor 4 → clone 4.
- Clone 4 → neighbors 1 and 3 already in map. Unwind. → complete independent copy. ✅

**Time:** O(V + E). **Space:** O(V) for the map + recursion.

### 🧠 Algorithm to Remember Forever
- **Pattern:** DFS/BFS traversal with a **visited→clone HashMap**.
- **Core idea:** Map each original node to its clone so cycles don't loop and nodes aren't duplicated.
- **Memory hook:** 🧬 Photocopying an interconnected family tree — keep a "who-have-I-copied" ledger.
- **Trigger phrase:** *"deep copy a graph"* or *"clone with cycles"* → DFS + HashMap.

---

## Problem 4: Word Ladder 🪜 (Hard)

### 📋 Full Question
Given `beginWord`, `endWord`, and a dictionary `wordList`, find the **length of the shortest transformation sequence** from `beginWord` to `endWord`, changing **one letter at a time**, where each intermediate word must be in `wordList`. Return 0 if no sequence exists. Length counts both endpoints.

**Example input:** `beginWord = "hit"`, `endWord = "cog"`, `wordList = ["hot","dot","dog","lot","log","cog"]`
**Expected output:** `5` (`hit → hot → dot → dog → cog`).

### 🗣️ What to say first
- "Each step changes exactly one letter, and every intermediate must be in the dictionary?" (Yes.)
- "Does `endWord` have to be in the list?" (Yes, otherwise return 0.)
- "Are all words the same length, lowercase letters?" (Typically yes.)

### Approach 1 — Brute Force (compare every pair)
Build a graph by comparing every pair of words (O(N²·L) edges), then BFS. Fine for small lists but the pairwise comparison is the bottleneck.

> Why a better approach exists: instead of comparing all pairs, generate **wildcard patterns** (`h*t`, `*it`, `hi*`) so neighbors are found in O(L) per word, not O(N).

### Approach 2 — Optimal (BFS on word graph 🪜)
**Idea:** Shortest transformation = shortest path in an unweighted graph → **BFS**. For each word, try changing each position to every letter `a–z`; if the result is in the dictionary set, it's a neighbor. BFS level = transformation step.

```java
import java.util.*;

class Solution {
    public int ladderLength(String beginWord, String endWord, List<String> wordList) {
        Set<String> dict = new HashSet<>(wordList);
        if (!dict.contains(endWord)) return 0;

        Queue<String> queue = new LinkedList<>();
        queue.offer(beginWord);
        Set<String> visited = new HashSet<>();
        visited.add(beginWord);
        int level = 1;                          // length counts beginWord

        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                String word = queue.poll();
                if (word.equals(endWord)) return level;

                char[] chars = word.toCharArray();
                for (int j = 0; j < chars.length; j++) {
                    char original = chars[j];
                    for (char ch = 'a'; ch <= 'z'; ch++) {
                        if (ch == original) continue;
                        chars[j] = ch;
                        String next = new String(chars);
                        if (dict.contains(next) && !visited.contains(next)) {
                            visited.add(next);
                            queue.offer(next);
                        }
                    }
                    chars[j] = original;        // restore for next position
                }
            }
            level++;
        }
        return 0;
    }
}
```

**Dry-run on the example:**
- Level 1: `hit`. Neighbors in dict: `hot`. Queue=[hot].
- Level 2: `hot` → `dot`,`lot`. Queue=[dot,lot].
- Level 3: `dot`→`dog`, `lot`→`log`. Queue=[dog,log].
- Level 4: `dog`→`cog`, `log`→`cog`. Queue=[cog].
- Level 5: pop `cog` == endWord → return **5**. ✅

**Time:** O(N · L²) where N = words, L = word length (L positions × 26 letters × O(L) string build). **Space:** O(N · L).

### 🧠 Algorithm to Remember Forever
- **Pattern:** **BFS on an implicit word graph** (shortest path, unweighted).
- **Core idea:** Words are nodes; one-letter changes are edges; BFS finds the fewest steps.
- **Memory hook:** 🪜 Climb a word ladder one rung (one letter) at a time — BFS finds the shortest ladder.
- **Trigger phrase:** *"transform one letter/step at a time, fewest steps"* → BFS.

---

## Problem 5: 01 Matrix 🔢

### 📋 Full Question
Given an `m × n` binary matrix `mat`, return a matrix where each cell holds the **distance to the nearest `0`** (4-directional distance).

**Example input:**
```
mat = [
  [0,0,0],
  [0,1,0],
  [1,1,1]
]
```
**Expected output:**
```
[[0,0,0],
 [0,1,0],
 [1,2,1]]
```

### 🗣️ What to say first
- "Distance is the number of 4-directional steps to the closest zero?" (Yes.)
- "Every matrix has at least one 0?" (Assume yes; otherwise some cells are infinite.)
- "Can I modify the input matrix?" (Usually yes.)

### Approach 1 — Brute Force (BFS per cell)
From each `1`, BFS outward until you hit a `0`. That's O((m·n)²) — far too slow.

> Why a better approach exists: **reverse it** — start BFS from *all* zeros at once (multi-source). Each cell's first time being reached = its nearest-zero distance.

### Approach 2 — Optimal (Multi-Source BFS 📡)
**Idea:** Seed the queue with **every `0`** (distance 0) and mark all `1`s as "unknown" (`-1`). BFS outward; when an unvisited neighbor is reached, its distance = current + 1.

```java
import java.util.*;

class Solution {
    public int[][] updateMatrix(int[][] mat) {
        int rows = mat.length, cols = mat[0].length;
        Queue<int[]> queue = new LinkedList<>();

        // Seed all zeros; mark ones as unvisited (-1)
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (mat[r][c] == 0) queue.offer(new int[]{r, c});
                else mat[r][c] = -1;
            }
        }

        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            for (int[] d : dirs) {
                int nr = cell[0] + d[0], nc = cell[1] + d[1];
                if (nr >= 0 && nc >= 0 && nr < rows && nc < cols
                        && mat[nr][nc] == -1) {           // unvisited 1
                    mat[nr][nc] = mat[cell[0]][cell[1]] + 1;
                    queue.offer(new int[]{nr, nc});
                }
            }
        }
        return mat;
    }
}
```

**Dry-run on the example:**
- Seed all zeros (distance 0). The lone inner `1` at (1,1) becomes 1 (reached from a 0 neighbor).
- Bottom row `1`s: (2,0)→1, (2,2)→1, (2,1)→2 (reached from (1,1) or its neighbors). ✅

**Time:** O(R × C). **Space:** O(R × C) for the queue.

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Multi-Source BFS** (distance from nearest target).
- **Core idea:** Start from all targets simultaneously; first arrival = shortest distance.
- **Memory hook:** 📡 Every zero is a sprinkler turning on at once — the water reaches each tile in nearest-source time.
- **Trigger phrase:** *"distance to nearest X for every cell"* → Multi-Source BFS.

---

## Problem 6: Number of Connected Components 🔗

### 📋 Full Question
Given `n` nodes labeled `0..n-1` and a list of undirected `edges`, return the **number of connected components**.

**Example input:** `n = 5`, `edges = [[0,1],[1,2],[3,4]]`
**Expected output:** `2` (component `{0,1,2}` and component `{3,4}`).

### 🗣️ What to say first
- "Undirected graph, nodes labeled 0 to n-1?" (Yes.)
- "Can there be duplicate edges or self-loops?" (Handle gracefully.)
- "Isolated nodes count as their own component?" (Yes.)

### Approach 1 — Brute Force (DFS/BFS from each unvisited node)
Build an adjacency list, then walk every node; each new DFS from an unvisited node is one component. Perfectly valid and O(V + E).

> Why a better approach exists: Both DFS and Union-Find are optimal here. **Union-Find** is especially clean for "count components" and handles streaming edges without rebuilding a graph.

### Approach 2 — Optimal (Union-Find 🌳)
**Idea:** Start with `n` components. For each edge, **union** the two endpoints; every successful union (two different roots merged) reduces the count by 1.

```java
class Solution {
    public int countComponents(int n, int[][] edges) {
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;   // each node its own root
        int components = n;

        for (int[] e : edges) {
            int rootA = find(parent, e[0]);
            int rootB = find(parent, e[1]);
            if (rootA != rootB) {                    // merge two components
                parent[rootA] = rootB;
                components--;
            }
        }
        return components;
    }

    private int find(int[] parent, int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];           // path compression
            x = parent[x];
        }
        return x;
    }
}
```

**Dry-run on the example:**
- Start: components = 5.
- Edge [0,1] → union → components = 4.
- Edge [1,2] → union (1 and 2 differ) → components = 3.
- Edge [3,4] → union → components = 2.
- Return **2**. ✅

**Time:** O(V + E · α(V)) ≈ near-linear. **Space:** O(V).

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Union-Find (Disjoint Set Union)**.
- **Core idea:** Start with n islands; each merging edge reduces the island count by one.
- **Memory hook:** 🌳 Merging family trees — each marriage joins two families into one.
- **Trigger phrase:** *"count connected components / are these nodes connected"* → Union-Find (or DFS).

---

## Problem 7: Pacific Atlantic Water Flow 🌊

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
**Expected output:** `[[0,4],[1,3],[1,4],[2,2],[3,0],[3,1],[4,0]]`.

### 🗣️ What to say first
- "Water flows from high to **equal-or-lower**, correct?" (Yes, `≤`.)
- "A cell on an ocean edge already touches that ocean?" (Yes.)
- "Order of output cells doesn't matter?" (Usually no.)

### Approach 1 — Brute Force (DFS from every cell)
From each of the m×n cells, DFS to see if it reaches the Pacific and (separately) the Atlantic. O((m×n)²) — too slow.

> Why a better approach exists: **reverse the flow** — start at the oceans and climb uphill. One DFS per ocean covers everything.

### Approach 2 — Optimal (Reverse DFS from oceans ⛰️)
**Idea:** DFS from all **Pacific-edge** cells climbing *uphill* (neighbor ≥ current), marking reachable cells. Same from all **Atlantic-edge** cells. Cells in **both** sets are the answer.

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

**Dry-run (intuition):** Top/left edges mark every cell that could drain to the Pacific (climbing up means it can flow back down). Bottom/right do the same for the Atlantic. Cells like `[2,2]` (a high point) drain to both. ✅

**Time:** O(R × C) — each cell visited at most twice. **Space:** O(R × C).

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Reverse DFS/BFS from targets** (multi-source from edges).
- **Core idea:** Flip the problem — ask "what can the ocean reach by climbing uphill."
- **Memory hook:** ⛰️ Don't trace every raindrop downhill; stand at the sea and walk *up* the mountain.
- **Trigger phrase:** *"which cells can reach multiple destinations"* → Reverse traversal from each destination.

---

## Problem 8: Course Schedule 📚 (Very Frequent!)

### 📋 Full Question
There are `numCourses` courses (0 to n-1). `prerequisites[i] = [a, b]` means you must take `b` before `a`. Return `true` if you can finish all courses (i.e., **no cycle**), else `false`.

**Example input:** `numCourses = 4`, `prerequisites = [[1,0],[2,1],[3,2]]`
**Expected output:** `true` (take 0→1→2→3). With `[[1,0],[0,1]]` → `false` (cycle).

### 🗣️ What to say first
- "This is detecting whether a **directed graph has a cycle**, correct?"
- "Can prerequisites contain duplicates or self-loops?" ([a,a] = instant cycle → false.)
- "Edges point prerequisite → course, or course → prerequisite? Let me define direction clearly."

### Approach 1 — Brute Force (DFS cycle check from each node)
Run DFS from every node tracking the current recursion path; revisiting a node on the *current* path = cycle. Works but without memoization you re-explore nodes.

> Why a better approach exists: Kahn's algorithm (BFS topological sort) is iterative (no stack-overflow risk), handles disconnected graphs naturally, and each node is processed once.

### Approach 2 — Optimal (Topological Sort via Kahn's BFS 🎓)
**Idea:** Compute **in-degree** (number of prerequisites) for every course. Queue all in-degree-0 courses. Process them, decrementing neighbors' in-degrees; queue any that hit 0. If we process all `numCourses` → no cycle.

```java
import java.util.*;

class Solution {
    public boolean canFinish(int numCourses, int[][] prerequisites) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) adj.add(new ArrayList<>());
        int[] indegree = new int[numCourses];

        for (int[] pre : prerequisites) {
            int course = pre[0], prereq = pre[1];
            adj.get(prereq).add(course);   // prereq → course
            indegree[course]++;
        }

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
- in-degree: [0,1,1,1]. Queue=[0].
- Take 0 → taken=1 → in-degree[1]=0 → queue 1.
- Take 1 → taken=2 → in-degree[2]=0 → queue 2.
- Take 2 → taken=3 → in-degree[3]=0 → queue 3.
- Take 3 → taken=4 == numCourses → **true**. ✅

**Time:** O(V + E). **Space:** O(V + E).

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Topological Sort** (Kahn's BFS) — also detects cycles.
- **Core idea:** Repeatedly remove zero-in-degree nodes; if you remove all, there's no cycle.
- **Memory hook:** 🎓 Peel a dependency onion — keep peeling layers that depend on nothing.
- **Trigger phrase:** *"order of tasks with dependencies / can you finish / cycle in directed graph"* → Topological Sort.

---

## Problem 9: Course Schedule II 📝

### 📋 Full Question
Same setup as Course Schedule, but return a **valid ordering** of courses to take. If impossible (cycle), return an **empty array**.

**Example input:** `numCourses = 4`, `prerequisites = [[1,0],[2,0],[3,1],[3,2]]`
**Expected output:** `[0,1,2,3]` (or any valid topological order, e.g., `[0,2,1,3]`).

### 🗣️ What to say first
- "Any valid order is fine, or a specific one?" (Usually any valid order.)
- "Return empty array if there's a cycle, right?"

### Approach 1 — Brute Force
Try permutations and check each respects all prerequisites → O(n!). Absurd. Mention it only to show you know it's a search-space explosion.

> Why a better approach exists: Topological sort directly *constructs* a valid order in linear time — no searching.

### Approach 2 — Optimal (Kahn's BFS, record the order 📝)
**Idea:** Identical to Course Schedule, but each time you pop a course, **append it to the result**. If the result contains all courses, return it; otherwise return empty.

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

        return idx == numCourses ? order : new int[0];   // cycle ⇒ empty
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
- **Core idea:** The order Kahn's removes zero-in-degree nodes *is* the valid schedule.
- **Memory hook:** 📝 A to-do list where a task unlocks only when everything it depends on is checked off.
- **Trigger phrase:** *"return a valid ordering with dependencies"* → Topological Sort (record order).

---

## Problem 10: Graph Valid Tree 🌲

### 📋 Full Question
Given `n` nodes labeled `0..n-1` and a list of undirected `edges`, determine if these edges form a **valid tree**. A valid tree is **fully connected** AND has **no cycle**.

**Example input:** `n = 5`, `edges = [[0,1],[0,2],[0,3],[1,4]]`
**Expected output:** `true`. With `edges = [[0,1],[1,2],[2,3],[1,3],[1,4]]` → `false` (has a cycle).

### 🗣️ What to say first
- "A valid tree must be connected with no cycles, right?"
- "Key insight: a tree with n nodes has **exactly n-1 edges**. More → cycle; fewer → disconnected."
- "Can there be duplicate or self-edges?" (Handle as cycles.)

### Approach 1 — Brute Force (DFS connectivity + cycle check)
DFS from node 0 tracking the parent; if you reach an already-visited node that isn't the parent, there's a cycle. Then verify all n nodes were visited (connected). Correct, O(V + E).

> Why a better approach exists: a slick shortcut — check `edges.length == n - 1` first (necessary), then just verify connectivity via Union-Find (no separate cycle scan needed).

### Approach 2 — Optimal (Union-Find 🌳)
**Idea:** First, a tree must have exactly `n - 1` edges — otherwise reject immediately. Then union all edges; if any edge connects two nodes already in the same set, there's a **cycle** → not a tree. With `n-1` edges and no cycle, the graph is automatically connected.

```java
class Solution {
    public boolean validTree(int n, int[][] edges) {
        // A tree with n nodes has exactly n-1 edges
        if (edges.length != n - 1) return false;

        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;

        for (int[] e : edges) {
            int rootA = find(parent, e[0]);
            int rootB = find(parent, e[1]);
            if (rootA == rootB) return false;   // cycle detected
            parent[rootA] = rootB;              // union
        }
        return true;   // n-1 edges + no cycle ⇒ connected tree
    }

    private int find(int[] parent, int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];      // path compression
            x = parent[x];
        }
        return x;
    }
}
```

**Dry-run on `n=5`, `edges=[[0,1],[0,2],[0,3],[1,4]]`:**
- edges.length = 4 == n-1 = 4 ✅.
- Union (0,1),(0,2),(0,3),(1,4) — every union joins different roots, no cycle.
- Return **true**. ✅

**Time:** O(V + E · α(V)) ≈ near-linear. **Space:** O(V).

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Union-Find** (or DFS) for tree validation.
- **Core idea:** Tree ⇔ exactly `n-1` edges + no cycle (which then forces connectivity).
- **Memory hook:** 🌲 A tree is a "just right" graph: one fewer edge than nodes, no loops, all in one piece.
- **Trigger phrase:** *"valid tree / connected and acyclic / n-1 edges"* → Union-Find.

---

## Problem 11: Cheapest Flights Within K Stops 🛫

### 📋 Full Question
`n` cities, `flights[i] = [from, to, price]` (directed). Find the **cheapest price** from `src` to `dst` using **at most `k` stops**. Return -1 if no such route.

**Example input:** `n = 4`, `flights = [[0,1,100],[1,2,100],[2,0,100],[1,3,600],[2,3,200]]`, `src = 0`, `dst = 3`, `k = 1`
**Expected output:** `700` (`0→1→3` costs 100+600=700; the cheaper `0→1→2→3`=400 uses 2 stops > k).

### 🗣️ What to say first
- "`k` stops means at most `k+1` edges, right?" (Yes — k intermediate cities.)
- "Edges directed, prices non-negative?" (Yes.)
- "Return -1 if unreachable within k stops?" (Yes.)

### Approach 1 — Brute Force (DFS all paths)
DFS every path from src, pruning when stops exceed k, tracking min cost. Exponential in the worst case — too slow for large graphs.

> Why a better approach exists: **Bellman-Ford bounded to k+1 relaxations** processes each edge at most k+1 times → O(K·E). It naturally caps the number of stops.

### Approach 2 — Optimal (Bellman-Ford, k+1 rounds 🔁)
**Idea:** `dist[i]` = cheapest cost to reach city `i`. Relax **all edges** exactly `k+1` times. Crucial detail: relax using a **snapshot** of the previous round's distances, so each round adds at most one more edge (one more stop). After `k+1` rounds, `dist[dst]` is the answer.

```java
import java.util.*;

class Solution {
    public int findCheapestPrice(int n, int[][] flights, int src, int dst, int k) {
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;

        // Relax all edges k+1 times (k stops = k+1 edges)
        for (int i = 0; i <= k; i++) {
            int[] snapshot = dist.clone();   // use previous round's values only
            for (int[] f : flights) {
                int from = f[0], to = f[1], price = f[2];
                if (snapshot[from] != Integer.MAX_VALUE
                        && snapshot[from] + price < dist[to]) {
                    dist[to] = snapshot[from] + price;
                }
            }
        }

        return dist[dst] == Integer.MAX_VALUE ? -1 : dist[dst];
    }
}
```

**Dry-run on the example (k=1, so 2 rounds):**
- Round 1 (≤1 edge from src): dist[1]=100. Others (reachable in 1 hop) updated.
- Round 2 (≤2 edges): from snapshot dist[1]=100 → dist[3]=100+600=700; dist[2]=200.
- dist[3] = **700**. (`0→1→2→3`=400 needs 3 edges = 2 stops, beyond k=1.) ✅

**Time:** O(K × E). **Space:** O(V).

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Bellman-Ford with a stop cap** (bounded relaxation). Alternative: BFS/Dijkstra-with-stops state `(cost, node, stops)`.
- **Core idea:** Each relaxation round extends paths by one more edge; cap the rounds at k+1.
- **Memory hook:** 🛫 A budget traveler with a fixed number of layovers — each round buys one more flight leg.
- **Trigger phrase:** *"cheapest/shortest path with at most K stops/edges"* → Bellman-Ford (bounded).

---

## Problem 12: Network Delay Time ⏱️ (Dijkstra)

### 📋 Full Question
A network of `n` nodes (1..n). `times[i] = [u, v, w]` = a directed edge from `u` to `v` taking `w` time. A signal starts at node `k`. Return the **time for all nodes to receive** the signal, or `-1` if some node is unreachable.

**Example input:** `times = [[2,1,1],[2,3,1],[3,4,1]]`, `n = 4`, `k = 2`
**Expected output:** `2` (longest shortest-path: 2→3→4 takes 2).

### 🗣️ What to say first
- "Edges directed and weights non-negative?" (Yes — non-negative means **Dijkstra** is valid.)
- "Return -1 if any node can't be reached from k?"
- "The answer is the **maximum** of all shortest distances?" (Yes — last node to hear the signal.)

### Approach 1 — Brute Force (Bellman-Ford / repeated relaxation)
Relax all edges `n−1` times → O(V × E). Correct, even handles negatives, but slower than Dijkstra for non-negative weights.

> Why a better approach exists: with **non-negative weights**, Dijkstra with a min-heap greedily locks in the closest node each step → O(E log V).

### Approach 2 — Optimal (Dijkstra with min-heap 🎯)
**Idea:** Min-heap of `(distance, node)`. Pop the closest unsettled node, settle it, relax its edges. The answer is the max settled distance (or -1 if some node never settles).

```java
import java.util.*;

class Solution {
    public int networkDelayTime(int[][] times, int n, int k) {
        Map<Integer, List<int[]>> adj = new HashMap<>();
        for (int[] t : times) {
            adj.computeIfAbsent(t[0], x -> new ArrayList<>())
               .add(new int[]{t[1], t[2]});
        }

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
- Pop (1,1) → dist[1]=1.
- Pop (1,3) → dist[3]=1 → push (2,4).
- Pop (2,4) → dist[4]=2.
- All 4 nodes settled. max = **2**. ✅

**Time:** O(E log V). **Space:** O(V + E).

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Dijkstra's shortest path** (greedy + min-heap).
- **Core idea:** Always expand the nearest unsettled node; non-negative weights guarantee it's final.
- **Memory hook:** 🎯 A GPS that explores the cheapest-so-far route first, locking in arrivals one city at a time.
- **Trigger phrase:** *"shortest path with weighted, non-negative edges"* → Dijkstra.

---

## Problem 13: Surrounded Regions 🔲

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
(The bottom-left `'O'` touches the border, so it survives; inner ones get captured.)

### 🗣️ What to say first
- "An `'O'` survives only if it connects to a border `'O'`, right?"
- "Can I mutate the board in place?" (Yes.)

### Approach 1 — Brute Force (check each region for border contact)
For each `'O'` region, flood-fill and check if *any* cell touches the border; if not, flip it. Doable but bookkeeping-heavy and may re-scan regions.

> Why a better approach exists: **invert the logic** — mark the *safe* ones first (border-connected), then flip everything else in one pass.

### Approach 2 — Optimal (Border DFS + flip 🚩)
**Idea:** DFS from every `'O'` on the **border**, marking those (and everything connected) as safe with temp marker `'#'`. Then sweep: any remaining `'O'` is surrounded → flip to `'X'`; restore `'#'` back to `'O'`.

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
                if (board[r][c] == 'O') board[r][c] = 'X';       // surrounded
                else if (board[r][c] == '#') board[r][c] = 'O';  // safe
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
- Border DFS finds the bottom-left `'O'` at (3,1) → mark `'#'`. No other border `'O'`s connect inward.
- Sweep: inner `'O'`s at (1,1),(1,2),(2,2) → flip to `'X'`. The `'#'` at (3,1) → back to `'O'`. ✅

**Time:** O(R × C). **Space:** O(R × C) recursion worst case.

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Border DFS / invert-the-problem** flood fill.
- **Core idea:** Find what's *safe* (touches the edge) first; capture everything else.
- **Memory hook:** 🚩 In Go/Othello, stones touching the wall can't be surrounded — protect the wall-huggers.
- **Trigger phrase:** *"regions surrounded / enclosed unless touching the border"* → Border DFS.

---

## Problem 14: Alien Dictionary 👽 (Hard)

### 📋 Full Question
A new language uses lowercase letters in an **unknown order**. Given a list of `words` **sorted lexicographically** in this alien language, derive the order of letters. Return any valid order, or `""` if invalid.

**Example input:** `words = ["wrt","wrf","er","ett","rftt"]`
**Expected output:** `"wertf"` (from comparisons: t<f, w<e, r<t, e<r).

### 🗣️ What to say first
- "The words are already sorted in the alien order, and I derive the alphabet from adjacent pairs?" (Yes.)
- "Return `""` if there's a cycle (contradiction)?" (Yes.)
- "Edge case: if a longer word is a prefix of a shorter one before it (e.g., `['abc','ab']`), that's invalid → return `""`."

### Approach 1 — Brute Force
Try all 26! letter permutations and check which is consistent with the sorting. Astronomically slow. Mention only to motivate the graph approach.

> Why a better approach exists: each adjacent word pair gives **one ordering edge** (first differing char). That's a directed graph → **topological sort** gives the alphabet in linear time.

### Approach 2 — Optimal (Build graph + Topological Sort 🎓)
**Idea:** Collect all unique letters as nodes. For each adjacent word pair, find the **first differing character** → that's an edge `c1 → c2`. Then run Kahn's topological sort. If you can't order all letters (cycle), return `""`. Also reject the prefix-violation edge case.

```java
import java.util.*;

class Solution {
    public String alienOrder(String[] words) {
        // Initialize graph nodes for every letter that appears
        Map<Character, Set<Character>> adj = new HashMap<>();
        Map<Character, Integer> indegree = new HashMap<>();
        for (String w : words) {
            for (char ch : w.toCharArray()) {
                adj.putIfAbsent(ch, new HashSet<>());
                indegree.putIfAbsent(ch, 0);
            }
        }

        // Build edges from adjacent word pairs
        for (int i = 0; i < words.length - 1; i++) {
            String first = words[i], second = words[i + 1];
            int len = Math.min(first.length(), second.length());
            // Invalid: "abc" before "ab" (longer prefix can't precede its prefix)
            if (first.length() > second.length()
                    && first.startsWith(second)) {
                return "";
            }
            for (int j = 0; j < len; j++) {
                char c1 = first.charAt(j), c2 = second.charAt(j);
                if (c1 != c2) {
                    if (!adj.get(c1).contains(c2)) {
                        adj.get(c1).add(c2);
                        indegree.put(c2, indegree.get(c2) + 1);
                    }
                    break;   // only the first differing char matters
                }
            }
        }

        // Kahn's topological sort
        Queue<Character> queue = new LinkedList<>();
        for (char ch : indegree.keySet()) {
            if (indegree.get(ch) == 0) queue.offer(ch);
        }

        StringBuilder sb = new StringBuilder();
        while (!queue.isEmpty()) {
            char ch = queue.poll();
            sb.append(ch);
            for (char next : adj.get(ch)) {
                indegree.put(next, indegree.get(next) - 1);
                if (indegree.get(next) == 0) queue.offer(next);
            }
        }

        // If not all letters ordered, there was a cycle
        return sb.length() == indegree.size() ? sb.toString() : "";
    }
}
```

**Dry-run on `["wrt","wrf","er","ett","rftt"]`:**
- Edges: `wrt` vs `wrf` → t→f; `wrf` vs `er` → w→e; `er` vs `ett` → r→t; `ett` vs `rftt` → e→r.
- Topo sort: w (indeg 0) → e → r → t → f → `"wertf"`. ✅

**Time:** O(C) where C = total characters across all words (edges ≤ unique pairs, ≤ 26²). **Space:** O(1) for the alphabet (≤26 nodes), O(C) to read input.

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Build graph from constraints + Topological Sort**.
- **Core idea:** Each adjacent pair's first differing letter is one ordering edge; topo-sort the alphabet.
- **Memory hook:** 👽 Reverse-engineer an alien alphabet by spotting where two sorted words first disagree.
- **Trigger phrase:** *"derive an order from sorted/relative constraints"* → Topological Sort.

---

## Problem 15: Shortest Path in Binary Matrix 🏃

### 📋 Full Question
Given an `n × n` binary grid, return the length of the **shortest clear path** from top-left `(0,0)` to bottom-right `(n-1,n-1)`. A clear path moves through `0` cells, **8-directionally** (including diagonals). Path length = number of cells visited. Return -1 if none.

**Example input:**
```
grid = [
 [0,0,0],
 [1,1,0],
 [1,1,0]
]
```
**Expected output:** `4` (path `(0,0)→(0,1)→(0,2)→(1,2)→(2,2)`... actually `(0,0)→(0,1)→(1,2)→(2,2)` = 4 cells).

### 🗣️ What to say first
- "Movement is **8-directional** (diagonals allowed)?" (Yes — note the 8 directions.)
- "Start and end must both be `0`, else return -1?" (Yes.)
- "Path length counts **cells**, not edges?" (Yes — start cell counts as 1.)

### Approach 1 — Brute Force (DFS all paths)
DFS exploring every clear path, tracking the minimum length. Exponential — re-explores cells. Too slow.

> Why a better approach exists: shortest path in an **unweighted grid** → **BFS** finds it in one sweep, visiting each cell once.

### Approach 2 — Optimal (BFS, 8 directions 🧭)
**Idea:** BFS from `(0,0)`. Each BFS level is one more step of path length. Use all 8 directions. First time you reach `(n-1,n-1)`, that's the shortest. Mark cells visited (set to 1 in place) to avoid revisiting.

```java
import java.util.*;

class Solution {
    public int shortestPathBinaryMatrix(int[][] grid) {
        int n = grid.length;
        if (grid[0][0] == 1 || grid[n-1][n-1] == 1) return -1;

        int[][] dirs = {
            {1,0},{-1,0},{0,1},{0,-1},
            {1,1},{1,-1},{-1,1},{-1,-1}   // 8 directions (diagonals)
        };

        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{0, 0});
        grid[0][0] = 1;                   // mark visited
        int pathLen = 1;                  // start cell counts

        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                int[] cell = queue.poll();
                if (cell[0] == n - 1 && cell[1] == n - 1) return pathLen;
                for (int[] d : dirs) {
                    int nr = cell[0] + d[0], nc = cell[1] + d[1];
                    if (nr >= 0 && nc >= 0 && nr < n && nc < n
                            && grid[nr][nc] == 0) {
                        grid[nr][nc] = 1;            // mark visited
                        queue.offer(new int[]{nr, nc});
                    }
                }
            }
            pathLen++;
        }
        return -1;
    }
}
```

**Dry-run on the example:**
- Level 1 (len 1): (0,0). Not target.
- Level 2 (len 2): (0,1) [and any 8-dir 0 neighbors]. 
- Level 3 (len 3): (0,2),(1,2)...
- Level 4 (len 4): (2,2) target reached → return **4**. ✅

**Time:** O(n²) — each cell visited once. **Space:** O(n²) for the queue.

### 🧠 Algorithm to Remember Forever
- **Pattern:** **BFS on a grid** (shortest path, unweighted, 8-directional).
- **Core idea:** BFS level count = shortest path length; first arrival at the target wins.
- **Memory hook:** 🧭 Ripples spread outward in all 8 compass directions; the ring that first touches the goal is shortest.
- **Trigger phrase:** *"shortest path in an unweighted grid"* → BFS.

---

## 📊 Complexity Cheat Sheet

| # | Problem | Brute Force | Optimal | Technique |
|---|---------|-------------|---------|-----------|
| 1 | Number of Islands | O(R·C) w/ re-scans | **O(R·C)** | DFS/BFS Flood Fill |
| 2 | Rotting Oranges | O((R·C)²) | **O(R·C)** | Multi-Source BFS |
| 3 | Clone Graph | ∞ (loops w/o map) | **O(V+E)** | DFS/BFS + HashMap |
| 4 | Word Ladder | O(N²·L) pairwise | **O(N·L²)** | BFS (word graph) |
| 5 | 01 Matrix | O((R·C)²) BFS-per-cell | **O(R·C)** | Multi-Source BFS |
| 6 | Connected Components | O(V+E) DFS | **O(V+E·α)** | Union-Find / DFS |
| 7 | Pacific Atlantic | O((R·C)²) | **O(R·C)** | Reverse DFS from oceans |
| 8 | Course Schedule | O(V+E) DFS w/ care | **O(V+E)** | Topological Sort (Kahn's) |
| 9 | Course Schedule II | O(n!) permutations | **O(V+E)** | Topological Sort (record order) |
| 10 | Graph Valid Tree | O(V+E) DFS | **O(V+E·α)** | Union-Find (n-1 edges + no cycle) |
| 11 | Cheapest Flights ≤ K Stops | O(exp) DFS | **O(K·E)** | Bellman-Ford (bounded) |
| 12 | Network Delay Time | O(V·E) Bellman-Ford | **O(E log V)** | Dijkstra (min-heap) |
| 13 | Surrounded Regions | O(R·C) w/ rescans | **O(R·C)** | Border DFS (invert) |
| 14 | Alien Dictionary | O(26!) | **O(C)** | Topological Sort |
| 15 | Shortest Path Binary Matrix | O(exp) DFS | **O(n²)** | BFS (8-directional) |

*(α = inverse-Ackermann ≈ constant; N = #words, L = word length; C = total characters.)*

---

## 🧠🧠 ALGORITHM RECOGNITION TABLE

> **The interviewer's words are your cheat code.** Train your ears for these triggers.

| If the question says… (Trigger) | Use this Algorithm | Memory Hook | Big-O |
|---|---|---|---|
| "count connected regions in a grid" | **Flood Fill (DFS/BFS)** | 🌊 Smash each sandcastle flat | O(R·C) |
| "shortest path in **unweighted** graph / grid" | **BFS** | 🌊 Ripples in a pond, level by level | O(V+E) |
| "minimum time to spread from many sources" / "distance to nearest X" | **Multi-Source BFS** | 📡 Many towers broadcasting at once | O(R·C) |
| "order tasks with **prerequisites** / can finish / cycle in **directed** graph / derive an order" | **Topological Sort (Kahn's)** | 🎓 Peel the dependency onion | O(V+E) |
| "deep copy a graph / clone with cycles" | **DFS + HashMap** | 🧬 Photocopy with a ledger | O(V+E) |
| "cells reaching multiple destinations" | **Reverse DFS from targets** | ⛰️ Walk uphill from the sea | O(R·C) |
| "regions enclosed unless touching border" | **Border DFS (invert)** | 🚩 Protect the wall-huggers | O(R·C) |
| "shortest path, **weighted, non-negative** edges" | **Dijkstra (min-heap)** | 🎯 GPS picks cheapest route first | O(E log V) |
| "cheapest path within **K stops/edges**" | **Bellman-Ford (bounded)** | 🛫 Limited layovers | O(K·E) |
| "transform word one letter at a time, fewest steps" | **BFS (word graph)** | 🪜 Climb a word ladder rung by rung | O(N·L²) |
| "count components / connected? / **valid tree** / cycle in **undirected** graph" | **Union-Find** (or DFS) | 🌳 n-1 edges & all connected = tree | O(E·α(V)) |

**Quick decision tree:**
- Grid + "count regions" → Flood Fill DFS.
- Grid/graph + "shortest steps" + unweighted → BFS (multi-source if many starts).
- "Dependencies / ordering / prerequisites" → Topological Sort.
- "Connected components / valid tree / undirected cycle" → Union-Find.
- "Shortest path + weights" → non-negative? Dijkstra. Negative or "K stops"? Bellman-Ford.

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
For multi-source / level-by-level BFS, snapshot `int size = queue.size();` **before** the inner loop so you process exactly one level (= one unit of distance/time).

**3. Visited set — pick the right tool:**
```java
Set<Integer> visited = new HashSet<>();         // for node-id graphs
boolean[][] visited = new boolean[rows][cols];  // for grids (faster, no hashing)
```
On grids, you can also mark visited *in place* (sink `'1'`→`'0'`, or `0`→`1`) for O(1) extra space — mention you're mutating the input first!

**4. Grid directions array — write once, reuse everywhere:**
```java
int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};   // 4-dir: down, up, right, left
// 8-directional (with diagonals):
int[][] dirs8 = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
for (int[] d : dirs) {
    int nr = r + d[0], nc = c + d[1];
    if (nr >= 0 && nc >= 0 && nr < rows && nc < cols && !visited[nr][nc]) {
        // explore (nr, nc)
    }
}
```

**5. Min-heap (PriorityQueue) for Dijkstra:**
```java
PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]); // by distance
pq.offer(new int[]{0, start});   // {distance, node}
```
Always store `{distance, node}` ordered by distance. Use `Integer.compare(a[0], b[0])` if overflow is a worry. Skip a node when popped if it's already settled.

**6. Topological Sort (Kahn's BFS) skeleton:**
```java
int[] indegree = new int[n];
List<List<Integer>> adj = new ArrayList<>();
for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
for (int[] e : edges) { adj.get(e[0]).add(e[1]); indegree[e[1]]++; }

Queue<Integer> q = new LinkedList<>();
for (int i = 0; i < n; i++) if (indegree[i] == 0) q.offer(i);
List<Integer> order = new ArrayList<>();
while (!q.isEmpty()) {
    int node = q.poll(); order.add(node);
    for (int next : adj.get(node)) if (--indegree[next] == 0) q.offer(next);
}
// order.size() == n  ⇒  no cycle; otherwise a cycle exists.
```

**7. Union-Find (Disjoint Set Union) skeleton:**
```java
int[] parent = new int[n];
for (int i = 0; i < n; i++) parent[i] = i;

int find(int x) {                       // with path compression
    while (parent[x] != x) { parent[x] = parent[parent[x]]; x = parent[x]; }
    return x;
}
boolean union(int a, int b) {
    int ra = find(a), rb = find(b);
    if (ra == rb) return false;         // already connected (cycle if undirected)
    parent[ra] = rb;
    return true;
}
```
Use Union-Find for connected components, cycle detection in undirected graphs, and "valid tree."

**8. Recursion depth:** Java's default stack can overflow on a ~10⁴+ deep DFS (a giant single island). Mention you'd switch to an explicit `Deque<int[]>` stack (iterative DFS) for very large inputs.

---

## ✅ Final Interview Checklist

Before you say "I'm done," tick every box:

- [ ] 🗣️ **Restated the problem** and asked clarifying questions (directed? cycles? grid diagonals? weighted?).
- [ ] 🧩 **Identified the pattern** out loud (BFS / DFS / Topo Sort / Union-Find / Dijkstra / Bellman-Ford) using a trigger word.
- [ ] 📉 **Mentioned brute force** and why the optimal beats it.
- [ ] 🧪 **Dry-ran** the optimal on the given example before trusting it.
- [ ] 🛡️ **Handled edge cases**: empty input, single node, disconnected graph, cycle, unreachable nodes, prefix violations.
- [ ] 🚫 **Used a visited set / in-degree / HashMap / parent array** to avoid infinite loops and re-processing.
- [ ] ⏱️ **Stated Time AND Space complexity** in terms of V and E (or R and C).
- [ ] 🧹 **Clean code**: meaningful names, `computeIfAbsent`, a single `dirs` array, no magic numbers.
- [ ] 🔁 **Offered a tradeoff**: "BFS gives shortest path; DFS is simpler for connectivity; Union-Find shines for components" — shows depth.
- [ ] 😊 **Stayed calm & narrated** — interviewers hire communicators, not silent typers.

**Remember:** Graphs reward *recognition*. Hear the trigger → name the pattern → reach for the template → adapt. You've got this! 🚀
