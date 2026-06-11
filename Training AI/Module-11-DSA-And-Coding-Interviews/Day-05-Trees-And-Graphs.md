# Module 11 — Day 5: Trees and Graphs

## The "Scary" Topics — Let's Demystify Them

Trees and graphs scare a lot of people. They shouldn't. Once you see that they're just **nodes connected by pointers** (you already met nodes yesterday with linked lists!), and that there are only **two ways to explore them** (DFS and BFS), the fear melts away.

Today's promise: by the end, you'll be able to walk any tree or graph using a handful of templates you can reuse forever.

---

## Part 1: Binary Trees

### The intuition: a family tree, but each parent has at most 2 children

```
              ┌───┐
              │ 1 │   ← root (the top)
              └─┬─┘
          ┌─────┴─────┐
        ┌─┴─┐       ┌─┴─┐
        │ 2 │       │ 3 │
        └─┬─┘       └─┬─┘
       ┌──┴──┐        │
     ┌─┴─┐ ┌─┴─┐    ┌─┴─┐
     │ 4 │ │ 5 │    │ 6 │   ← leaves (no children)
     └───┘ └───┘    └───┘
```

**Key vocabulary:**
- **Root** — the top node (the entry point).
- **Leaf** — a node with no children.
- **Children / Parent** — each node has up to 2 children: `left` and `right`.
- **Height / Depth** — how many levels deep the tree goes.

### Defining a Binary Tree in Python

It's almost identical to a linked list node — just two pointers instead of one:

```python
class TreeNode:
    def __init__(self, val=0, left=None, right=None):
        self.val = val
        self.left = left      # pointer to left child
        self.right = right    # pointer to right child

# Build this tree:
#        1
#       / \
#      2   3
#     / \
#    4   5
root = TreeNode(1)
root.left = TreeNode(2)
root.right = TreeNode(3)
root.left.left = TreeNode(4)
root.left.right = TreeNode(5)
```

---

## Tree Traversals: The Two Big Ways to Explore

There are exactly two strategies to visit every node:

- **DFS (Depth-First Search)** — go *deep* first, all the way down one branch before backtracking. Powered by **recursion** (or a stack).
- **BFS (Breadth-First Search)** — go *wide* first, level by level. Powered by a **queue**.

```
   DFS goes deep:           BFS goes wide:
        1                        1            ← level 0
       /↓                       / \
      2  ...                    2   3          ← level 1
     /↓                        / \
    4 (all the way down       4   5            ← level 2
       before going right)
```

### DFS with Recursion (the clean way)

DFS has three flavors depending on *when* you "visit" the current node:

```python
def inorder(node):        # Left → Node → Right  (gives sorted order for a BST!)
    if not node:
        return
    inorder(node.left)
    print(node.val, end=" ")
    inorder(node.right)

def preorder(node):       # Node → Left → Right  (good for copying a tree)
    if not node:
        return
    print(node.val, end=" ")
    preorder(node.left)
    preorder(node.right)

def postorder(node):      # Left → Right → Node  (good for deleting a tree)
    if not node:
        return
    postorder(node.left)
    postorder(node.right)
    print(node.val, end=" ")

# Using the tree from above:
inorder(root)     # Output: 4 2 5 1 3
print()
preorder(root)    # Output: 1 2 4 5 3
print()
postorder(root)   # Output: 4 5 2 3 1
```

> **The recursion template for trees:**
> 1. **Base case** — if the node is `None`, return (nothing to do).
> 2. **Recurse** on `node.left` and `node.right`.
> 3. **Combine** the results.
>
> Almost every tree problem follows this shape. Memorize it.

### BFS with a Queue (level-by-level)

When the problem mentions **levels** ("print level by level", "find the shortest path", "rightmost node per level"), reach for BFS with a queue:

```python
from collections import deque

def level_order(root):
    if not root:
        return []
    result = []
    queue = deque([root])         # start with the root

    while queue:
        level_size = len(queue)   # how many nodes on this level
        level = []
        for _ in range(level_size):
            node = queue.popleft()      # take from the front (FIFO)
            level.append(node.val)
            if node.left:               # add children for the NEXT level
                queue.append(node.left)
            if node.right:
                queue.append(node.right)
        result.append(level)
    return result

print(level_order(root))
# Output: [[1], [2, 3], [4, 5]]   ← each inner list is one level!
```

> **This is why Day 4's queue mattered.** BFS *is* a queue-driven traversal. See how the pieces connect?

### A Useful Tree Problem: Maximum Depth

```python
def max_depth(node):
    if not node:               # base case: empty tree has depth 0
        return 0
    # depth = 1 (this node) + the deeper of the two subtrees
    return 1 + max(max_depth(node.left), max_depth(node.right))

print(max_depth(root))   # Output: 3
```

Notice the template: base case → recurse on both children → combine with `max`. Clean and tiny.

---

## Part 2: Binary Search Trees (BST)

A **Binary Search Tree** is a binary tree with one special rule:

> **For every node: everything in the LEFT subtree is smaller, everything in the RIGHT subtree is larger.**

```
              ┌───┐
              │ 8 │
              └─┬─┘
          ┌─────┴─────┐
        ┌─┴─┐       ┌──┴──┐
        │ 3 │       │ 10  │
        └─┬─┘       └──┬──┘
       ┌──┴──┐         └──┐
     ┌─┴─┐ ┌─┴─┐       ┌──┴─┐
     │ 1 │ │ 6 │       │ 14 │
     └───┘ └───┘       └────┘

   Everything left of 8 is < 8.  Everything right is > 8.
```

This ordering means you can **search in O(log n)** — at each step, you eliminate half the tree, just like binary search!

```python
def search_bst(node, target):
    while node:
        if target == node.val:
            return node              # found it!
        elif target < node.val:
            node = node.left         # target is smaller → go left
        else:
            node = node.right        # target is larger → go right
    return None                      # not found

# (using a BST root)
```

> **Cool fact:** An **in-order traversal of a BST visits nodes in sorted order** (`4 2 5 1 3` becomes sorted for a real BST). Interviewers love testing whether you know this. "Validate a BST" is a classic problem built on it.

---

## Part 3: Graphs

A **graph** is the general case: nodes (called **vertices**) connected by edges, with *no* rules about how many connections each node has. Think social networks (people = nodes, friendships = edges), maps (cities = nodes, roads = edges), or the web (pages = nodes, links = edges).

```
        A ───── B
        │       │
        │       │
        C ───── D ───── E
```

A tree is actually just a special kind of graph (one with no cycles and a single root). So everything you learned about DFS/BFS applies here too!

### Representing a Graph: the Adjacency List

The most common way to store a graph is a dict mapping each node to its neighbors:

```python
# The graph above
graph = {
    "A": ["B", "C"],
    "B": ["A", "D"],
    "C": ["A", "D"],
    "D": ["B", "C", "E"],
    "E": ["D"],
}
# graph["A"] → ["B", "C"]  means A connects to B and C
```

### DFS on a Graph (with a `visited` set — crucial!)

The big difference from trees: **graphs can have cycles**, so you *must* track visited nodes or you'll loop forever.

```python
def dfs(graph, start, visited=None):
    if visited is None:
        visited = set()

    visited.add(start)             # mark as visited — prevents infinite loops!
    print(start, end=" ")

    for neighbor in graph[start]:
        if neighbor not in visited:   # O(1) set check (Day 3!)
            dfs(graph, neighbor, visited)

dfs(graph, "A")   # Output: A B D C E  (one possible deep-first order)
```

### BFS on a Graph (shortest path in unweighted graphs!)

BFS explores level by level — so the **first time** it reaches a node, it's via the **shortest path** (fewest edges). This makes BFS the go-to for shortest-path-in-an-unweighted-graph problems.

```python
from collections import deque

def bfs(graph, start):
    visited = set([start])
    queue = deque([start])

    while queue:
        node = queue.popleft()
        print(node, end=" ")
        for neighbor in graph[node]:
            if neighbor not in visited:
                visited.add(neighbor)
                queue.append(neighbor)

bfs(graph, "A")   # Output: A B C D E  (visits closer nodes first)
```

### DFS vs. BFS — when to use which

```
Use DFS when...                         Use BFS when...
──────────────────────────────────────────────────────────
exploring all paths                     finding SHORTEST path (unweighted)
detecting cycles                        processing level by level
"does a path exist?"                    "fewest steps to reach X?"
backtracking problems                   "nearest" anything
uses recursion or a stack               uses a queue
```

> **Memory hook:** **B**FS uses a **B**ucket-line queue (FIFO). **D**FS goes **D**eep with recursion/stack.

### The "Number of Islands" Pattern (grid as a graph)

A super-common interview format: a 2D grid where you treat each cell as a graph node. Counting connected regions ("islands") uses DFS/BFS to "sink" each island you find.

```python
def num_islands(grid):
    if not grid:
        return 0
    rows, cols = len(grid), len(grid[0])
    count = 0

    def sink(r, c):
        # stop if out of bounds or hitting water
        if r < 0 or r >= rows or c < 0 or c >= cols or grid[r][c] == "0":
            return
        grid[r][c] = "0"          # mark as visited by "sinking" it
        sink(r + 1, c)            # explore all 4 directions
        sink(r - 1, c)
        sink(r, c + 1)
        sink(r, c - 1)

    for r in range(rows):
        for c in range(cols):
            if grid[r][c] == "1":     # found new land → it's a new island
                count += 1
                sink(r, c)            # sink the whole island
    return count

grid = [
    ["1", "1", "0", "0"],
    ["1", "0", "0", "1"],
    ["0", "0", "1", "1"],
]
print(num_islands(grid))   # Output: 2
```

This single pattern (grid + DFS flood fill) unlocks dozens of problems. Recognize it!

---

## Decision Guide

```
Problem involves...                        →  Approach...
──────────────────────────────────────────────────────────────
a tree, "visit all nodes"                  →  DFS recursion template
"level by level" / "rightmost per level"   →  BFS with queue
a sorted/searchable tree                    →  BST (O(log n) search)
"shortest path", "fewest steps" (unweighted)→  BFS
"all paths" / "is there a path" / cycles    →  DFS + visited set
a 2D grid of connected cells                 →  DFS/BFS flood fill
ANY graph traversal                          →  remember the visited set!
```

---

## Practice Exercises

For tree/graph problems, always ask first: **DFS or BFS?** Then pick the template.

### Exercise 1: "Maximum Depth of Binary Tree" — LeetCode #104 (Easy)
The exact problem above. Implement the recursive version. The simplest possible tree-recursion template — perfect to lock it in.

### Exercise 2: "Invert Binary Tree" — LeetCode #226 (Easy)
Swap every node's left and right children. Famous (it's the "I couldn't invert a binary tree so Google didn't hire me" tweet). Three lines with recursion!

### Exercise 3: "Binary Tree Level Order Traversal" — LeetCode #102 (Medium)
Return the node values level by level. The exact **BFS-with-queue** template above. Extremely common.

### Exercise 4: "Validate Binary Search Tree" — LeetCode #98 (Medium)
Check if a tree is a valid BST. Hint: pass down a valid `(min, max)` range as you recurse, or use the in-order-is-sorted fact.

### Exercise 5: "Number of Islands" — LeetCode #200 (Medium)
The grid flood-fill problem above. Asked at Microsoft, Google, Amazon — *constantly*. Implement DFS, then try BFS.

### Exercise 6: "Course Schedule" — LeetCode #207 (Medium)
Detect if you can finish all courses given prerequisites (cycle detection in a directed graph). A great stretch problem that combines graphs + DFS + the visited concept.

---

## Quick Reference Card

```python
# TREE NODE
class TreeNode:
    def __init__(self, val=0, left=None, right=None):
        self.val, self.left, self.right = val, left, right

# DFS TEMPLATE (recursion)
def dfs(node):
    if not node:           # base case
        return
    # ... process node ...
    dfs(node.left)
    dfs(node.right)

# BFS TEMPLATE (queue)
from collections import deque
def bfs(root):
    queue = deque([root])
    while queue:
        node = queue.popleft()
        # ... process node ...
        if node.left:  queue.append(node.left)
        if node.right: queue.append(node.right)

# GRAPH DFS (MUST track visited!)
def dfs_graph(graph, node, visited):
    if node in visited: return
    visited.add(node)
    for nb in graph[node]:
        dfs_graph(graph, nb, visited)

# GRID FLOOD FILL: check bounds, mark visited, recurse 4 directions
```

---

## What's Next?

You just conquered the topics that intimidate most candidates! You've also seen recursion appear everywhere today — which is the perfect setup for tomorrow. In **Day 06 — Recursion and Dynamic Programming**, we'll go deep on recursion and then unlock **dynamic programming** (DP), the most feared interview category — made intuitive with fibonacci, climbing stairs, and the knapsack. Five days down, two to go. You're nearly there!
