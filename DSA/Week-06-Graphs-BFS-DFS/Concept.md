# Week 6: Graphs, BFS & DFS

---

## Part 1: GRAPHS

---

### 1. What is a Graph?

**Plain English:**
A Graph is a collection of THINGS (nodes) connected by RELATIONSHIPS (edges).
Unlike trees, there is no "root" or strict hierarchy. Any node can connect to any other node.

**Real-life Analogy — City Road Map:**
Think of a map of Indian cities connected by highways.
- Cities = Nodes (also called Vertices)
- Highways between cities = Edges
- Some highways are one-way (directed) — like a one-way street
- Some highways are two-way (undirected) — like a normal highway
- Each highway has a distance (weight) — Delhi to Mumbai = 1,400 km

```
    Delhi -------- Jaipur
      |    \         |
      |     \        |
    Agra    Lucknow  |
      |              |
      |              |
    Mumbai --------- Ahmedabad
```
This is a graph! 6 cities (nodes), connected by roads (edges).

**Another Analogy — Social Network:**
- People = Nodes
- Friendships = Edges
- Instagram follows are DIRECTED (you follow them, they may not follow back)
- Facebook friendships are UNDIRECTED (always mutual)
- LinkedIn connections are UNDIRECTED

**Technical Definition:**
A graph G = (V, E) consists of:
- V = set of vertices (nodes)
- E = set of edges (connections between pairs of vertices)

---

### 2. Why Do Graphs Exist?

**What problem do they solve?**

Trees can only represent hierarchical relationships. But the real world is full of
NON-hierarchical connections:
- Road networks (Google Maps routing)
- Social networks (friend recommendations)
- Internet (web pages linked to each other)
- Course prerequisites (take Math 101 before Math 201)
- Flight routes (cities connected by flights)
- Dependencies (this task must finish before that task)

Graphs are the MOST GENERAL data structure for representing relationships.
In fact, a tree is just a special type of graph (connected, acyclic, undirected).

---

### 3. Graph Terminology

```
    A -------- B
    |  \       |
    |   \      |
    C    D --- E
    |
    F
```

| Term | Meaning | Example |
|------|---------|---------|
| **Vertex / Node** | A point in the graph | A, B, C, D, E, F |
| **Edge** | Connection between two vertices | A-B, A-C, A-D, B-E, D-E, C-F |
| **Adjacent** | Two nodes connected by an edge | A and B are adjacent |
| **Degree** | Number of edges connected to a node | Degree of A = 3 (B, C, D) |
| **Path** | Sequence of vertices connected by edges | A → D → E → B is a path |
| **Cycle** | Path that starts and ends at the same vertex | A → B → E → D → A |
| **Connected** | Path exists between every pair of vertices | This graph is connected |
| **Disconnected** | Some vertices cannot reach each other | If we remove C-F edge, F is isolated |

**Directed vs Undirected:**
```
Undirected:  A --- B     (A can reach B, B can reach A)
Directed:    A --> B     (A can reach B, but B cannot reach A)
```

**Weighted vs Unweighted:**
```
Unweighted:  A --- B        (edge exists or doesn't)
Weighted:    A --5-- B      (edge has a cost/weight of 5)
```

**In-degree / Out-degree (directed graphs only):**
```
    A --> B --> C
    |         ^
    v        /
    D ------
```
- In-degree of C = 2 (B→C and D→C, two arrows pointing IN)
- Out-degree of A = 2 (A→B and A→D, two arrows pointing OUT)
- In-degree of A = 0 (no arrows pointing to A)

**DAG — Directed Acyclic Graph:**
A directed graph with NO cycles. Very important for:
- Course prerequisites
- Task scheduling
- Build systems (compile A before B)

```
    Math101 --> Math201 --> Math301
                  |
                  v
              Stats101 --> Stats201

This is a DAG. You can follow arrows, but you can never go in a circle.
```

---

### 4. Graph Representation

There are two main ways to store a graph in code:

#### 4a. Adjacency Matrix

A 2D array where `matrix[i][j] = 1` means there is an edge from node i to node j.

**Example — undirected graph:**
```
    0 --- 1
    |   / |
    |  /  |
    2 --- 3

Adjacency Matrix:
      0  1  2  3
  0 [ 0, 1, 1, 0 ]    Node 0 connects to: 1, 2
  1 [ 1, 0, 1, 1 ]    Node 1 connects to: 0, 2, 3
  2 [ 1, 1, 0, 1 ]    Node 2 connects to: 0, 1, 3
  3 [ 0, 1, 1, 0 ]    Node 3 connects to: 1, 2
```

Notice: for undirected graphs, the matrix is SYMMETRIC (matrix[i][j] == matrix[j][i]).

**Java:**
```java
int V = 4;  // number of vertices
int[][] matrix = new int[V][V];

// Add edge between 0 and 1 (undirected)
matrix[0][1] = 1;
matrix[1][0] = 1;

// Check if edge exists between 0 and 1
if (matrix[0][1] == 1) { /* edge exists */ }  // O(1) lookup!
```

**Pros:** O(1) to check if an edge exists.
**Cons:** Uses O(V^2) space. Wasteful for sparse graphs (few edges).

---

#### 4b. Adjacency List

An array of lists. `list[i]` contains all neighbors of node i.

**Same example:**
```
    0 --- 1
    |   / |
    |  /  |
    2 --- 3

Adjacency List:
  0 → [1, 2]
  1 → [0, 2, 3]
  2 → [0, 1, 3]
  3 → [1, 2]
```

**Java:**
```java
int V = 4;
// Option 1: List of Lists (most common for numbered nodes)
List<List<Integer>> adj = new ArrayList<>();
for (int i = 0; i < V; i++) {
    adj.add(new ArrayList<>());
}

// Add edge between 0 and 1 (undirected — add both directions)
adj.get(0).add(1);
adj.get(1).add(0);

// Get all neighbors of node 0
List<Integer> neighbors = adj.get(0);  // [1, 2]

// Option 2: Map (useful when nodes are not numbered 0 to V-1)
Map<String, List<String>> graph = new HashMap<>();
graph.computeIfAbsent("Delhi", k -> new ArrayList<>()).add("Mumbai");
graph.computeIfAbsent("Mumbai", k -> new ArrayList<>()).add("Delhi");
```

**Pros:** Space efficient — O(V + E). Good for sparse graphs.
**Cons:** O(degree) to check if a specific edge exists.

---

#### When to Use Which?

| | Adjacency Matrix | Adjacency List |
|---|---|---|
| Space | O(V^2) | O(V + E) |
| Check edge exists? | O(1) | O(degree) |
| Find all neighbors? | O(V) | O(degree) |
| Add edge? | O(1) | O(1) |
| Best for | Dense graphs (many edges) | Sparse graphs (few edges) |

**Rule of thumb:** If edges < V^2, use adjacency list. This is almost always the case
in real-world graphs and interview problems. **Default to adjacency list.**

---

### 5. Building a Graph from Edge List (Interview Input Format)

Most interview problems give you edges as a list of pairs. Here is how to build the adjacency list:

```java
// Given: n = 5 (nodes 0 to 4), edges = [[0,1], [0,2], [1,3], [2,4], [3,4]]
// Build undirected adjacency list

int n = 5;
int[][] edges = {{0,1}, {0,2}, {1,3}, {2,4}, {3,4}};

List<List<Integer>> adj = new ArrayList<>();
for (int i = 0; i < n; i++) {
    adj.add(new ArrayList<>());
}

for (int[] edge : edges) {
    adj.get(edge[0]).add(edge[1]);  // add edge in both directions
    adj.get(edge[1]).add(edge[0]);  // (skip this line for directed graphs)
}

// Result:
// 0 → [1, 2]
// 1 → [0, 3]
// 2 → [0, 4]
// 3 → [1, 4]
// 4 → [2, 3]
```

---

## Part 2: BFS (Breadth-First Search)

---

### 1. What is BFS?

**Plain English:**
BFS explores a graph LEVEL BY LEVEL. It visits ALL nodes at distance 1 first,
then ALL nodes at distance 2, then distance 3, and so on.

**Real-life Analogy — Ripples in Water:**
Drop a stone in a still pond. Ripples spread outward in CIRCLES — the nearest
water moves first, then the next ring, then the next. BFS explores exactly
like this — nearest nodes first, then further ones.

**Another Analogy — Spreading a Rumor:**
You tell a rumor to your 5 friends (distance 1). Each of THEM tells their
friends (distance 2). Those friends tell their friends (distance 3). Each
"generation" of rumor spreading is one BFS level.

**Key property:** BFS finds the SHORTEST PATH in an unweighted graph.
Because it explores level by level, the first time it reaches a node is
guaranteed to be via the shortest path.

---

### 2. How BFS Works — Step by Step

**Data structure:** QUEUE (First In, First Out)

**Algorithm:**
1. Put the starting node in the queue, mark it as visited
2. While queue is not empty:
   a. Dequeue the front node
   b. Process it (print, check condition, etc.)
   c. Enqueue all its UNVISITED neighbors, mark them visited
3. Done when queue is empty

**Visual walkthrough:**
```
Graph:
    0 --- 1 --- 4
    |     |
    2 --- 3

Start: node 0
Visited: {0}

Step 1: Queue = [0]
  Dequeue 0 → Visit 0
  Neighbors of 0: 1, 2 (both unvisited)
  Enqueue 1, 2 → Queue = [1, 2]
  Visited: {0, 1, 2}

Step 2: Queue = [1, 2]
  Dequeue 1 → Visit 1
  Neighbors of 1: 0, 3, 4
    0 already visited, skip
    3, 4 unvisited → enqueue
  Queue = [2, 3, 4]
  Visited: {0, 1, 2, 3, 4}

Step 3: Queue = [2, 3, 4]
  Dequeue 2 → Visit 2
  Neighbors of 2: 0, 3
    0 already visited, 3 already visited → skip all
  Queue = [3, 4]

Step 4: Queue = [3, 4]
  Dequeue 3 → Visit 3
  Neighbors of 3: 1, 2
    Both already visited → skip
  Queue = [4]

Step 5: Queue = [4]
  Dequeue 4 → Visit 4
  Neighbors of 4: 1
    Already visited → skip
  Queue = [] → DONE!

BFS order: 0, 1, 2, 3, 4
Levels: Level 0: {0}, Level 1: {1, 2}, Level 2: {3, 4}
```

---

### 3. BFS Time and Space Complexity

| | Complexity | Why |
|---|---|---|
| Time | O(V + E) | Visit each vertex once, check each edge once |
| Space | O(V) | Queue can hold up to V nodes; visited array is size V |

---

### 4. Java BFS Template

```java
// BFS on a graph represented as an adjacency list
// Finds shortest distance from source to all reachable nodes
void bfs(List<List<Integer>> adj, int source, int n) {
    boolean[] visited = new boolean[n];   // track which nodes we have seen
    Queue<Integer> queue = new LinkedList<>();

    visited[source] = true;               // mark source as visited
    queue.offer(source);                  // add source to queue

    int level = 0;                        // tracks distance from source

    while (!queue.isEmpty()) {
        int size = queue.size();          // number of nodes at THIS level
        System.out.println("Level " + level + ":");

        for (int i = 0; i < size; i++) {
            int node = queue.poll();      // dequeue front
            System.out.print(node + " ");

            // Add all unvisited neighbors to queue
            for (int neighbor : adj.get(node)) {
                if (!visited[neighbor]) {
                    visited[neighbor] = true;   // mark visited WHEN ADDING to queue
                    queue.offer(neighbor);       // (not when processing — prevents duplicates)
                }
            }
        }
        System.out.println();
        level++;
    }
}
```

**IMPORTANT:** Mark nodes as visited when you ADD them to the queue, NOT when you
process them. If you mark when processing, the same node can get added to the queue
multiple times from different neighbors — wasting time and potentially giving wrong results.

---

### 5. BFS on a 2D Grid (Matrix)

Many interview problems give you a 2D grid instead of an explicit graph.
Each cell is a node. Its neighbors are the 4 adjacent cells (up, down, left, right).

**Example — Shortest path in a maze:**
```
Grid:
  0 0 0 0
  1 1 0 1
  0 0 0 0
  0 1 1 0

0 = open path, 1 = wall
Find shortest path from (0,0) to (3,3)
```

**Java BFS on Grid:**
```java
// BFS on a 2D grid — finds shortest path from top-left to bottom-right
// Returns the shortest distance, or -1 if no path exists
int shortestPath(int[][] grid) {
    int rows = grid.length, cols = grid[0].length;

    // Edge case: start or end is a wall
    if (grid[0][0] == 1 || grid[rows-1][cols-1] == 1) return -1;

    // 4 directions: up, down, left, right
    int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    boolean[][] visited = new boolean[rows][cols];
    Queue<int[]> queue = new LinkedList<>();

    queue.offer(new int[]{0, 0});   // start at top-left
    visited[0][0] = true;
    int distance = 0;

    while (!queue.isEmpty()) {
        int size = queue.size();    // all nodes at current distance

        for (int i = 0; i < size; i++) {
            int[] cell = queue.poll();
            int row = cell[0], col = cell[1];

            // Check if we reached the destination
            if (row == rows - 1 && col == cols - 1) {
                return distance;
            }

            // Try all 4 directions
            for (int[] dir : dirs) {
                int newRow = row + dir[0];
                int newCol = col + dir[1];

                // Check bounds, not a wall, not visited
                if (newRow >= 0 && newRow < rows &&
                    newCol >= 0 && newCol < cols &&
                    grid[newRow][newCol] == 0 &&
                    !visited[newRow][newCol]) {

                    visited[newRow][newCol] = true;
                    queue.offer(new int[]{newRow, newCol});
                }
            }
        }
        distance++;   // finished one level — distance increases
    }
    return -1;  // no path found
}
```

---

## Part 3: DFS (Depth-First Search)

---

### 1. What is DFS?

**Plain English:**
DFS explores a graph by going as DEEP as possible along each path before backtracking.
It picks one direction and follows it all the way to the end, then comes back and
tries the next direction.

**Real-life Analogy — Exploring a Maze:**
You are in a maze. You pick the LEFT path at every junction. You walk and walk until
you hit a DEAD END. Then you walk BACK to the last junction and try a DIFFERENT path.
You keep doing this until you find the exit or have tried everything.

**Another Analogy — Reading a Book with Footnotes:**
You are reading a book. You see a footnote (1), so you jump to it. That footnote
references another footnote (1a), so you jump to THAT. Eventually you reach a footnote
with no further references — dead end. You come BACK to where you left off in the
book and continue reading. This is DFS — go deep, then backtrack.

---

### 2. How DFS Works — Step by Step

**Data structure:** STACK (or recursion — recursion IS a stack!)

**Algorithm:**
1. Start at a node, mark it visited
2. For each unvisited neighbor:
   a. Mark it visited
   b. Recursively DFS from that neighbor (go DEEPER)
3. When all neighbors are visited (dead end), BACKTRACK

**Visual walkthrough:**
```
Graph:
    0 --- 1 --- 4
    |     |
    2 --- 3

Start: node 0
Visited: {}

DFS(0):
  Visit 0, Visited: {0}
  Neighbor 1 unvisited → DFS(1)
    Visit 1, Visited: {0, 1}
    Neighbor 0 already visited → skip
    Neighbor 3 unvisited → DFS(3)
      Visit 3, Visited: {0, 1, 3}
      Neighbor 1 already visited → skip
      Neighbor 2 unvisited → DFS(2)
        Visit 2, Visited: {0, 1, 3, 2}
        Neighbor 0 already visited → skip
        Neighbor 3 already visited → skip
        All neighbors visited → BACKTRACK to 3
      All neighbors visited → BACKTRACK to 1
    Neighbor 4 unvisited → DFS(4)
      Visit 4, Visited: {0, 1, 3, 2, 4}
      Neighbor 1 already visited → skip
      BACKTRACK to 1
    All neighbors visited → BACKTRACK to 0
  Neighbor 2 already visited → skip
  All neighbors visited → DONE

DFS order: 0, 1, 3, 2, 4
```

Notice how DFS went DEEP first (0→1→3→2) before backtracking to try another path (4).

---

### 3. DFS Time and Space Complexity

| | Complexity | Why |
|---|---|---|
| Time | O(V + E) | Visit each vertex once, check each edge once |
| Space | O(V) | Recursion stack can go V levels deep; visited array is size V |

---

### 4. Java DFS Templates

**Recursive DFS (most common, simplest):**
```java
// DFS on a graph represented as an adjacency list
void dfs(List<List<Integer>> adj, int node, boolean[] visited) {
    visited[node] = true;               // mark current node as visited
    System.out.print(node + " ");       // process the node

    // Visit all unvisited neighbors
    for (int neighbor : adj.get(node)) {
        if (!visited[neighbor]) {
            dfs(adj, neighbor, visited);  // go DEEPER (recursive call)
        }
    }
    // When this function returns, we have BACKTRACKED
}

// Usage:
// boolean[] visited = new boolean[n];
// dfs(adj, 0, visited);
```

**Iterative DFS (using explicit stack):**
```java
// Iterative DFS using an explicit stack
// Useful when recursion depth might cause StackOverflow
void dfsIterative(List<List<Integer>> adj, int source, int n) {
    boolean[] visited = new boolean[n];
    Stack<Integer> stack = new Stack<>();

    stack.push(source);

    while (!stack.isEmpty()) {
        int node = stack.pop();

        if (visited[node]) continue;  // skip if already visited
        visited[node] = true;
        System.out.print(node + " ");

        // Push all unvisited neighbors onto stack
        // (they will be processed in reverse order — LIFO)
        for (int neighbor : adj.get(node)) {
            if (!visited[neighbor]) {
                stack.push(neighbor);
            }
        }
    }
}
```

**DFS on a 2D Grid:**
```java
// DFS on a 2D grid — e.g., counting islands
// Each connected group of 1s is one island
void dfsGrid(int[][] grid, int row, int col, boolean[][] visited) {
    int rows = grid.length, cols = grid[0].length;

    // Boundary checks: out of bounds, water (0), or already visited
    if (row < 0 || row >= rows || col < 0 || col >= cols) return;
    if (grid[row][col] == 0 || visited[row][col]) return;

    visited[row][col] = true;   // mark as visited

    // Explore all 4 directions (up, down, left, right)
    dfsGrid(grid, row - 1, col, visited);  // up
    dfsGrid(grid, row + 1, col, visited);  // down
    dfsGrid(grid, row, col - 1, visited);  // left
    dfsGrid(grid, row, col + 1, visited);  // right
}

// Usage — count islands:
int countIslands(int[][] grid) {
    int rows = grid.length, cols = grid[0].length;
    boolean[][] visited = new boolean[rows][cols];
    int islands = 0;

    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            if (grid[i][j] == 1 && !visited[i][j]) {
                dfsGrid(grid, i, j, visited);   // explore entire island
                islands++;                       // found one more island
            }
        }
    }
    return islands;
}
```

---

### 5. BFS vs DFS — Comparison

| | BFS | DFS |
|---|---|---|
| **Data structure** | Queue (FIFO) | Stack / Recursion (LIFO) |
| **Explores** | Level by level (wide first) | Path by path (deep first) |
| **Shortest path?** | YES (unweighted graphs) | NO |
| **Memory** | O(width of graph) | O(depth of graph) |
| **Implementation** | Iterative with queue | Recursive (or iterative with stack) |
| **Best for** | Shortest path, level order | Cycle detection, topological sort, path existence |
| **Think of it as** | Ripples in water | Exploring a maze |

**When to use BFS:**
- "Shortest path" or "minimum steps" in an unweighted graph/grid
- "Level order" anything
- "Nearest" something
- Multi-source BFS (start from multiple sources simultaneously)

**When to use DFS:**
- "Find if a path exists"
- "Count connected components" (like counting islands)
- "Detect cycles"
- "Topological sort"
- "Generate all paths/combinations" (backtracking)

---

## Part 4: TOPOLOGICAL SORT

---

### 1. What is Topological Sort?

**Plain English:**
Topological sort is an ordering of nodes in a Directed Acyclic Graph (DAG)
where every node comes BEFORE all the nodes it points to.

**Real-life Analogy — Course Prerequisites:**
```
    Math101 → Math201 → Math301
                ↓
            Stats201
```
You MUST take Math101 before Math201, and Math201 before Math301 or Stats201.

A valid topological order: Math101, Math201, Math301, Stats201
Another valid order: Math101, Math201, Stats201, Math301

Both are valid! There can be MULTIPLE valid topological orders.

**Another Analogy — Getting Dressed:**
```
Underwear → Pants → Belt
Undershirt → Shirt → Jacket
Socks → Shoes
```
You must put on underwear before pants, pants before belt, etc.
A valid order: Underwear, Undershirt, Socks, Pants, Shirt, Belt, Shoes, Jacket

**Important:** Topological sort only works on DAGs. If there is a cycle,
topological sort is IMPOSSIBLE (chicken and egg problem).

---

### 2. Two Approaches

#### Approach 1: BFS-based (Kahn's Algorithm)

**Idea:** Start with nodes that have NO prerequisites (in-degree = 0). Process them,
then reduce in-degrees of their neighbors. Repeat.

```java
// Kahn's Algorithm — BFS-based Topological Sort
// Returns topological order, or empty list if cycle exists
List<Integer> topologicalSortBFS(int n, List<List<Integer>> adj) {
    int[] inDegree = new int[n];    // count of incoming edges for each node
    List<Integer> result = new ArrayList<>();

    // Step 1: Calculate in-degree for each node
    for (int u = 0; u < n; u++) {
        for (int v : adj.get(u)) {
            inDegree[v]++;           // edge u → v means v gets one more incoming edge
        }
    }

    // Step 2: Add all nodes with in-degree 0 to queue
    // These nodes have NO prerequisites — they can go first
    Queue<Integer> queue = new LinkedList<>();
    for (int i = 0; i < n; i++) {
        if (inDegree[i] == 0) {
            queue.offer(i);
        }
    }

    // Step 3: Process nodes level by level
    while (!queue.isEmpty()) {
        int node = queue.poll();
        result.add(node);            // add to topological order

        // "Remove" this node: reduce in-degree of all neighbors
        for (int neighbor : adj.get(node)) {
            inDegree[neighbor]--;
            if (inDegree[neighbor] == 0) {  // neighbor now has no remaining prerequisites
                queue.offer(neighbor);
            }
        }
    }

    // Step 4: Check for cycle — if we processed fewer nodes than exist, there is a cycle
    if (result.size() != n) {
        return new ArrayList<>();    // cycle detected! topological sort impossible
    }
    return result;
}
```

**Walkthrough:**
```
Graph: 0→1, 0→2, 1→3, 2→3

In-degrees: [0:0, 1:1, 2:1, 3:2]
Queue: [0] (only 0 has in-degree 0)

Process 0: result=[0], reduce in-degree of 1,2
  In-degrees: [0:0, 1:0, 2:0, 3:2]
  Queue: [1, 2]

Process 1: result=[0,1], reduce in-degree of 3
  In-degrees: [0:0, 1:0, 2:0, 3:1]
  Queue: [2]

Process 2: result=[0,1,2], reduce in-degree of 3
  In-degrees: [0:0, 1:0, 2:0, 3:0]
  Queue: [3]

Process 3: result=[0,1,2,3]
Queue empty, result.size()==4==n → valid!
Topological order: [0, 1, 2, 3]
```

---

#### Approach 2: DFS-based Topological Sort

**Idea:** Do a DFS. When you FINISH processing a node (all descendants done),
add it to the front of the result. This naturally puts dependencies before dependents.

```java
// DFS-based Topological Sort
List<Integer> topologicalSortDFS(int n, List<List<Integer>> adj) {
    boolean[] visited = new boolean[n];
    Stack<Integer> stack = new Stack<>();   // will hold the reverse topological order

    for (int i = 0; i < n; i++) {
        if (!visited[i]) {
            dfsTopoSort(adj, i, visited, stack);
        }
    }

    // Stack now has topological order (top = first in order)
    List<Integer> result = new ArrayList<>();
    while (!stack.isEmpty()) {
        result.add(stack.pop());
    }
    return result;
}

void dfsTopoSort(List<List<Integer>> adj, int node, boolean[] visited, Stack<Integer> stack) {
    visited[node] = true;

    for (int neighbor : adj.get(node)) {
        if (!visited[neighbor]) {
            dfsTopoSort(adj, neighbor, visited, stack);
        }
    }

    // POST-ORDER: push to stack AFTER all neighbors are processed
    // This ensures all dependents are already in the stack (below this node)
    stack.push(node);
}
```

---

## Part 5: CYCLE DETECTION

---

### 1. Cycle Detection in Undirected Graphs

**Method:** DFS. If we visit an already-visited node that is NOT the parent of the
current node, we found a cycle.

```java
// Detect cycle in an undirected graph using DFS
boolean hasCycle(List<List<Integer>> adj, int n) {
    boolean[] visited = new boolean[n];

    // Check each connected component (graph might be disconnected)
    for (int i = 0; i < n; i++) {
        if (!visited[i]) {
            if (dfsCycleUndirected(adj, i, visited, -1)) {
                return true;   // cycle found!
            }
        }
    }
    return false;   // no cycle in any component
}

boolean dfsCycleUndirected(List<List<Integer>> adj, int node, boolean[] visited, int parent) {
    visited[node] = true;

    for (int neighbor : adj.get(node)) {
        if (!visited[neighbor]) {
            // Unvisited neighbor — explore it
            if (dfsCycleUndirected(adj, neighbor, visited, node)) {
                return true;
            }
        } else if (neighbor != parent) {
            // Visited AND not our parent — we found a BACK EDGE → CYCLE!
            return true;
        }
        // If visited AND is our parent — that is just the edge we came from, ignore
    }
    return false;
}
```

**Why check `neighbor != parent`?**
In an undirected graph, if A connects to B, then B also connects to A. When we go
from A to B, B's neighbor list includes A. But A is B's parent — that is not a cycle,
just the same edge in reverse. A cycle requires reaching a visited node via a DIFFERENT path.

---

### 2. Cycle Detection in Directed Graphs

**Method:** DFS with 3 states (colors):
- **WHITE (0)**: not visited yet
- **GRAY (1)**: currently being processed (in the current DFS path)
- **BLACK (2)**: fully processed (all descendants done)

If we encounter a GRAY node → we have found a back edge → CYCLE!

```java
// Detect cycle in a directed graph using DFS with 3 colors
boolean hasCycleDirected(List<List<Integer>> adj, int n) {
    int[] color = new int[n];  // 0=WHITE, 1=GRAY, 2=BLACK

    for (int i = 0; i < n; i++) {
        if (color[i] == 0) {   // WHITE — not visited
            if (dfsCycleDirected(adj, i, color)) {
                return true;
            }
        }
    }
    return false;
}

boolean dfsCycleDirected(List<List<Integer>> adj, int node, int[] color) {
    color[node] = 1;   // GRAY — we are currently exploring this path

    for (int neighbor : adj.get(node)) {
        if (color[neighbor] == 1) {
            return true;   // GRAY neighbor — we came back to a node in our current path!
                           // This is a CYCLE
        }
        if (color[neighbor] == 0) {   // WHITE — unvisited, explore it
            if (dfsCycleDirected(adj, neighbor, color)) {
                return true;
            }
        }
        // BLACK — fully processed, ignore (it is not part of our current path)
    }

    color[node] = 2;   // BLACK — done exploring all paths from this node
    return false;
}
```

**Why 3 colors instead of just visited/not-visited?**
In a directed graph, visiting an already-visited node does NOT always mean a cycle.
Consider: A→B, A→C, B→C. When we process A→B→C (mark C visited), then come back
and try A→C, C is already visited but there is NO cycle. With colors, C is BLACK
(fully processed) — not GRAY (in current path) — so we correctly skip it.

---

### 6. When to Use Graphs — Pattern Recognition Triggers

| If you see this in the problem... | Think this... |
|-----------------------------------|---------------|
| "Connected components" or "islands" | Graph + DFS/BFS |
| "Shortest path" (unweighted) | BFS |
| "Level by level" or "minimum steps" | BFS |
| "Can we reach X from Y?" | DFS or BFS |
| "Detect cycle" | DFS (undirected: parent check, directed: 3 colors) |
| "Prerequisites" or "dependencies" | Topological Sort (DAG) |
| "Valid ordering" or "course schedule" | Topological Sort |
| "Number of connected groups" | DFS/BFS + counting |
| "Grid/matrix traversal" | Treat as graph, cells = nodes, 4-directional neighbors |
| "Bipartite" or "two-coloring" | BFS/DFS with 2-color check |

---

### 7. Common Mistakes

1. **Forgetting the visited array:** Without it, you will visit nodes infinitely in cycles or revisit the same node many times.

2. **BFS: marking visited when processing instead of when enqueueing:** This causes the same node to be added to the queue multiple times from different neighbors, wasting time and potentially giving incorrect shortest path.

3. **Undirected cycle detection: not checking for parent:** Every undirected edge looks like a "cycle" if you do not exclude the parent — A connects to B, B connects back to A is NOT a cycle.

4. **Directed cycle detection: using only visited boolean instead of 3 colors:** In directed graphs, reaching an already-visited node is not always a cycle. You need to distinguish "currently in my DFS path" (GRAY) from "fully processed" (BLACK).

5. **Forgetting disconnected components:** Always loop through ALL nodes as potential starting points. Some nodes may not be reachable from node 0.

6. **Grid BFS/DFS: forgetting boundary checks:** Always check `row >= 0 && row < rows && col >= 0 && col < cols` before accessing `grid[row][col]`.

7. **Topological sort on a graph with cycles:** Topological sort is only defined for DAGs. Always check for cycles first (or detect it when your result size does not match n).

---

### 8. Quick Summary

```
GRAPHS:
  - Nodes + edges. No hierarchy required (unlike trees)
  - Directed vs Undirected, Weighted vs Unweighted
  - Representation: Adjacency List (almost always) or Adjacency Matrix
  - Build from edge list: loop through edges, add to both directions (undirected)

BFS:
  - Uses QUEUE. Explores level by level (nearest first)
  - Finds SHORTEST PATH in unweighted graphs
  - Time: O(V+E), Space: O(V)
  - Template: queue + visited + level counting

DFS:
  - Uses STACK / RECURSION. Explores as deep as possible, then backtracks
  - Good for: cycle detection, topological sort, path finding, connected components
  - Time: O(V+E), Space: O(V)
  - Template: recursive function + visited array

TOPOLOGICAL SORT:
  - Linear ordering of DAG nodes respecting dependencies
  - BFS approach (Kahn's): start with in-degree 0 nodes, peel off layers
  - DFS approach: post-order push to stack

CYCLE DETECTION:
  - Undirected: DFS + parent check
  - Directed: DFS + 3 colors (WHITE/GRAY/BLACK)

CHOOSING:
  - "Shortest path" → BFS
  - "Cycle detection" → DFS
  - "Prerequisites/ordering" → Topological sort
  - "Count components/islands" → DFS or BFS
  - "All paths" → DFS (backtracking)
```

---

*Next: Week 7 — Dynamic Programming. The most feared topic, made simple!*
