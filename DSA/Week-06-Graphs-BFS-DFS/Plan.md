# Week 6: Graphs — BFS & DFS

## Concept
- Graph = nodes (vertices) connected by edges
- Can be directed (one-way) or undirected (two-way)
- Represented as: Adjacency List (most common) or Adjacency Matrix

## Two Ways to Explore a Graph
```
BFS (Breadth-First Search):
  Explore level by level (like ripples in water)
  Uses: Queue
  Best for: Shortest path, level-order problems

DFS (Depth-First Search):
  Go as deep as possible, then backtrack
  Uses: Stack or Recursion
  Best for: Connected components, cycle detection, path finding
```

---

## Day 1-2: BFS Problems

| # | Problem | LeetCode Link | Difficulty | Microsoft? |
|---|---------|--------------|------------|------------|
| 1 | Number of Islands | https://leetcode.com/problems/number-of-islands/ | Medium | Yes (Top 5 Microsoft!) |
| 2 | Rotting Oranges | https://leetcode.com/problems/rotting-oranges/ | Medium | Yes (Frequent) |
| 3 | Clone Graph | https://leetcode.com/problems/clone-graph/ | Medium | Yes |
| 4 | Word Ladder | https://leetcode.com/problems/word-ladder/ | Hard | Yes |
| 5 | 01 Matrix | https://leetcode.com/problems/01-matrix/ | Medium | Yes |

### BFS Template
```python
from collections import deque

def bfs(graph, start):
    queue = deque([start])
    visited = {start}

    while queue:
        node = queue.popleft()
        for neighbor in graph[node]:
            if neighbor not in visited:
                visited.add(neighbor)
                queue.append(neighbor)
```

---

## Day 3-4: DFS Problems

| # | Problem | LeetCode Link | Difficulty | Microsoft? |
|---|---------|--------------|------------|------------|
| 6 | Number of Connected Components | https://leetcode.com/problems/number-of-connected-components-in-an-undirected-graph/ | Medium | Yes |
| 7 | Pacific Atlantic Water Flow | https://leetcode.com/problems/pacific-atlantic-water-flow/ | Medium | Yes |
| 8 | Course Schedule | https://leetcode.com/problems/course-schedule/ | Medium | Yes (Frequent!) |
| 9 | Course Schedule II | https://leetcode.com/problems/course-schedule-ii/ | Medium | Yes |
| 10 | Graph Valid Tree | https://leetcode.com/problems/graph-valid-tree/ | Medium | Yes |

### DFS Template
```python
def dfs(graph, node, visited):
    visited.add(node)
    for neighbor in graph[node]:
        if neighbor not in visited:
            dfs(graph, neighbor, visited)
```

---

## Day 5-6: Advanced Graph

| # | Problem | LeetCode Link | Difficulty | Microsoft? |
|---|---------|--------------|------------|------------|
| 11 | Cheapest Flights Within K Stops | https://leetcode.com/problems/cheapest-flights-within-k-stops/ | Medium | Yes |
| 12 | Network Delay Time (Dijkstra) | https://leetcode.com/problems/network-delay-time/ | Medium | Yes |
| 13 | Surrounded Regions | https://leetcode.com/problems/surrounded-regions/ | Medium | Yes |
| 14 | Alien Dictionary | https://leetcode.com/problems/alien-dictionary/ | Hard | Yes |
| 15 | Shortest Path in Binary Matrix | https://leetcode.com/problems/shortest-path-in-binary-matrix/ | Medium | Yes |

---

## Day 7: Revision

- Number of Islands (must-know)
- Course Schedule (topological sort)
- Write BFS and DFS templates from memory

---

## When to Use BFS vs DFS?

```
Need SHORTEST path?           → BFS
Need to explore ALL paths?    → DFS
Grid/Matrix problem?          → BFS or DFS (both work)
Detect CYCLE?                 → DFS
Topological Sort?             → DFS (or BFS with Kahn's)
Connected Components?         → DFS or BFS (both work)
```

---

## Self-Check

- [ ] Can write BFS and DFS templates from memory
- [ ] Can solve Number of Islands in 10 minutes
- [ ] Understand topological sort (Course Schedule)
- [ ] Know when to use BFS vs DFS
- [ ] Solved at least 12 of 15 questions
