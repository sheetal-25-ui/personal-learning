# Week 5: Trees & BST — How to ANSWER in a Microsoft Interview 🎤 (Java)

> Microsoft asks tree questions in **every single round**. If you master this week, you've covered roughly a third of what they throw at you. The good news: 90% of tree problems reduce to **recursion (DFS)** or **BFS with a queue**. Learn those two engines, add the **inorder = sorted** trick for BSTs, and you can answer almost anything.

---

## 🏆 The Golden Rule: ALWAYS follow this order

Whenever you get a problem, walk the interviewer through these 5 stages **out loud**. Never jump straight to code.

**Clarify → Brute Force → Optimal → Best → Complexity**

The interviewer is grading your *communication and structured thinking* as much as your code. A silent genius fails; a chatty problem-solver passes.

### 🗣️ Interview Script (what to literally say out loud)

1. **"Let me make sure I understand the problem."** → restate it in your own words with a tiny example.
2. **"Let me clarify a few edge cases."** → ask the clarifying questions (see below).
3. **"My first instinct is a brute-force approach..."** → describe the naive idea, even if slow. This shows you can always produce *something*.
4. **"...but I think we can do better. The key observation is..."** → describe the optimal insight.
5. **"Let me code the optimal version."** → write clean code, narrating as you go.
6. **"Let me dry-run this on the example."** → trace your code line by line on the sample input.
7. **"The time complexity is O(...), space is O(...)."** → always end with complexity (remember recursion stack!).
8. **"Let me check edge cases: empty tree, single node, skewed tree."** → name the edge cases.

### ❓ Common clarifying questions for tree problems

- "Can the tree be **empty** (root is null)?"
- "Can node values be **negative** or **duplicated**?"
- "Is this a **Binary Search Tree** (sorted property) or a general **Binary Tree**? This changes everything."
- "Are nodes **guaranteed to exist** in the tree (for LCA / search problems)?"
- "Is the tree **balanced**, or could it be **skewed** (worst-case a linked list)?"
- "Should I return the **values**, the **nodes**, or modify the tree **in place**?"
- "How large can the tree get? (Determines if recursion stack depth is a concern.)"

---

## ☕ The TreeNode you'll use everywhere

Every problem below assumes this standard definition (LeetCode gives you this):

```java
public class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;
    TreeNode() {}
    TreeNode(int val) { this.val = val; }
    TreeNode(int val, TreeNode left, TreeNode right) {
        this.val = val;
        this.left = left;
        this.right = right;
    }
}
```

---

# 📚 The Problems (all 18)

---

## Problem 1 — Maximum Depth of Binary Tree (Easy, Microsoft ✅)

### 📋 Full Question
Given the root of a binary tree, return its **maximum depth** — the number of nodes along the longest path from the root down to the farthest leaf.

**Example:**
```
        3
       / \
      9   20
         /  \
        15   7
```
Input: `root = [3,9,20,null,null,15,7]` → **Output: `3`** (path 3 → 20 → 15 or 3 → 20 → 7).

### 🗣️ What to say first
- "Is an empty tree depth `0`? (Yes, conventionally.)"
- "Is a single node depth `1`?"
- "Can the tree be very deep / skewed? (Tells me whether to worry about recursion stack.)"

### Approach 1 — Iterative BFS counting levels
Walk level by level with a queue, increment a counter for each level processed.

```java
public int maxDepth(TreeNode root) {
    if (root == null) return 0;
    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);
    int depth = 0;
    while (!queue.isEmpty()) {
        int levelSize = queue.size();   // freeze the count for THIS level
        for (int i = 0; i < levelSize; i++) {
            TreeNode node = queue.poll();
            if (node.left != null) queue.offer(node.left);
            if (node.right != null) queue.offer(node.right);
        }
        depth++;                        // finished one level
    }
    return depth;
}
```
**Time: O(n)** — visit every node. **Space: O(w)** — w = max width (queue size).
*Why a better approach exists:* It works, but recursion expresses "depth" more naturally and is shorter — and the recursive pattern generalizes to nearly every other tree problem this week.

### Approach 2 — Optimal: Recursion (DFS)
**Core insight:** depth of a node = `1 + max(depth of left subtree, depth of right subtree)`.

```java
public int maxDepth(TreeNode root) {
    if (root == null) return 0;                    // base case: empty = 0
    int left = maxDepth(root.left);
    int right = maxDepth(root.right);
    return 1 + Math.max(left, right);              // 1 for current node
}
```

**Dry-run on the example:**
- `maxDepth(15)` → 1 + max(0, 0) = **1**
- `maxDepth(7)` → **1**
- `maxDepth(20)` → 1 + max(1, 1) = **2**
- `maxDepth(9)` → **1**
- `maxDepth(3)` → 1 + max(1, 2) = **3** ✅

**Time: O(n)**. **Space: O(h)** — h = height (recursion stack), O(log n) balanced, O(n) skewed.

### 🧠 Algorithm to Remember Forever
- **Pattern:** DFS via recursion (bottom-up — children before parent).
- **Core idea:** "My answer = combine my children's answers + 1 for me."
- **Memory hook 🌳:** Measuring a family tree's generations — count your tallest child's line, then add yourself. 👨‍👧
- **Trigger phrase:** *"depth / height / how tall"* → recursion returning `max(left, right) + 1`.

---

## Problem 2 — Invert Binary Tree (Easy, Microsoft ✅)

### 📋 Full Question
Given the root of a binary tree, **invert it** (mirror it left-to-right) and return the root.

**Example:**
```
     4                4
    / \              / \
   2   7    →       7   2
  / \ / \          / \ / \
 1  3 6  9        9  6 3  1
```
Input: `root = [4,2,7,1,3,6,9]` → **Output: `[4,7,2,9,6,3,1]`**.

### 🗣️ What to say first
- "Empty tree → return null?"
- "Modify in place and return the same root, correct?" (Yes.)

### Approach 1 — Iterative BFS swap
Use a queue; for each node swap its two children, then enqueue them.

```java
public TreeNode invertTree(TreeNode root) {
    if (root == null) return null;
    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);
    while (!queue.isEmpty()) {
        TreeNode node = queue.poll();
        TreeNode tmp = node.left;       // swap children
        node.left = node.right;
        node.right = tmp;
        if (node.left != null) queue.offer(node.left);
        if (node.right != null) queue.offer(node.right);
    }
    return root;
}
```
**Time: O(n)**. **Space: O(w)**.
*Why a better approach exists:* Recursion expresses "mirror every fork" in three lines — cleaner and the expected answer.

### Approach 2 — Optimal: Recursion (swap children, recurse)
**Core insight:** Inverting a tree = swap the two children of every node. Do it recursively.

```java
public TreeNode invertTree(TreeNode root) {
    if (root == null) return null;               // base case
    TreeNode left = invertTree(root.left);       // invert left subtree
    TreeNode right = invertTree(root.right);     // invert right subtree
    root.left = right;                           // swap
    root.right = left;
    return root;
}
```

**Dry-run on the example:** At leaf `1` and `3`, nothing changes. At `2`, swap → children become `3,1`. At `7`, swap → `9,6`. At `4`, swap subtrees → left=`7`-subtree, right=`2`-subtree ✅.

**Time: O(n)**. **Space: O(h)** recursion stack.

### 🧠 Algorithm to Remember Forever
- **Pattern:** DFS that mutates structure — swap children at every node.
- **Core idea:** "Mirror every fork; recursion handles the rest."
- **Memory hook 🔄:** Hold the tree up to a mirror — every left becomes a right. 🪞
- **Trigger phrase:** *"invert / mirror / flip the tree"* → swap children recursively.

---

## Problem 3 — Same Tree (Easy, Microsoft ✅)

### 📋 Full Question
Given the roots of two binary trees `p` and `q`, return `true` if they are **structurally identical** and have the **same node values**.

**Example:**
```
   1        1
  / \      / \
 2   3    2   3      → true
```
```
   1        1
  /          \
 2            2      → false  (different structure)
```

### 🗣️ What to say first
- "Both empty → considered the same (true)?" (Yes.)
- "Compare both structure AND values?" (Yes.)

### Approach 1 — Recursion (the natural answer)
**Core insight:** Two trees are the same iff their roots match AND their left subtrees match AND their right subtrees match.

```java
public boolean isSameTree(TreeNode p, TreeNode q) {
    if (p == null && q == null) return true;     // both empty → same
    if (p == null || q == null) return false;    // one empty → different
    if (p.val != q.val) return false;            // values differ
    return isSameTree(p.left, q.left)
        && isSameTree(p.right, q.right);
}
```

**Dry-run:** roots 1==1, recurse left (2==2, both leaves) and right (3==3, both leaves) → **true** ✅.

**Time: O(n)** — n = nodes (visit each once). **Space: O(h)** recursion stack.

### Approach 2 — Optimal: Iterative with two queues
Same logic without recursion — useful if asked to avoid the stack on very deep trees.

```java
public boolean isSameTree(TreeNode p, TreeNode q) {
    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(p); queue.offer(q);
    while (!queue.isEmpty()) {
        TreeNode a = queue.poll(), b = queue.poll();
        if (a == null && b == null) continue;
        if (a == null || b == null || a.val != b.val) return false;
        queue.offer(a.left);  queue.offer(b.left);
        queue.offer(a.right); queue.offer(b.right);
    }
    return true;
}
```
**Time: O(n)**. **Space: O(w)**.

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Lockstep DFS** — compare two trees node-by-node simultaneously.
- **Core idea:** "Walk both trees in parallel; mismatch anywhere → false."
- **Memory hook 👯:** Two dancers must hit the same pose at the same beat. 🎵
- **Trigger phrase:** *"same / identical / equal trees"* → compare two nodes in lockstep.

---

## Problem 4 — Symmetric Tree (Easy, Microsoft ✅)

### 📋 Full Question
Given the root of a binary tree, check whether it is a **mirror of itself** (symmetric around its center).

**Example:**
```
      1
     / \
    2   2
   / \ / \
  3  4 4  3      → true (mirror image)
```
```
      1
     / \
    2   2
     \   \
      3   3      → false
```

### 🗣️ What to say first
- "Empty tree → symmetric (true)?" (Yes.)
- "Symmetric means the left subtree mirrors the right subtree, correct?"

### Approach 1 — Iterative BFS pairing mirror nodes
Push pairs `(left, right)` that should mirror each other; check each pair.

```java
public boolean isSymmetric(TreeNode root) {
    if (root == null) return true;
    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root.left); queue.offer(root.right);
    while (!queue.isEmpty()) {
        TreeNode a = queue.poll(), b = queue.poll();
        if (a == null && b == null) continue;
        if (a == null || b == null || a.val != b.val) return false;
        queue.offer(a.left);  queue.offer(b.right);   // outer pair
        queue.offer(a.right); queue.offer(b.left);    // inner pair
    }
    return true;
}
```
**Time: O(n)**. **Space: O(w)**.

### Approach 2 — Optimal: Recursion comparing mirrored pairs
**Core insight:** A tree is symmetric iff its left and right subtrees are **mirror images**. Two trees mirror iff: roots equal, AND left's-left mirrors right's-right, AND left's-right mirrors right's-left.

```java
public boolean isSymmetric(TreeNode root) {
    if (root == null) return true;
    return isMirror(root.left, root.right);
}
private boolean isMirror(TreeNode a, TreeNode b) {
    if (a == null && b == null) return true;
    if (a == null || b == null) return false;
    return a.val == b.val
        && isMirror(a.left, b.right)     // outer vs outer
        && isMirror(a.right, b.left);    // inner vs inner
}
```

**Dry-run:** isMirror(2,2): vals equal → compare (3 vs 3) outer and (4 vs 4) inner → all match → **true** ✅.

**Time: O(n)**. **Space: O(h)** recursion stack.

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Mirror DFS** — compare two nodes but cross the directions (a.left ↔ b.right).
- **Core idea:** "Walk left and right subtrees inward toward each other."
- **Memory hook 🪞:** Like checking a butterfly's wings — left and right must match flipped. 🦋
- **Trigger phrase:** *"symmetric / mirror of itself / palindrome tree"* → mirror-compare with crossed children.

---

## Problem 5 — Binary Tree Level Order Traversal (Medium, Microsoft ✅ **Very Frequent**)

### 📋 Full Question
Return the node values **level by level**, top to bottom, left to right, as a list of lists.

**Example:**
```
        3
       / \
      9   20
         /  \
        15   7
```
Input: `root = [3,9,20,null,null,15,7]` → **Output: `[[3], [9,20], [15,7]]`**.

### 🗣️ What to say first
- "Empty tree → return empty list `[]`?"
- "Left to right within each level, correct?"
- "List-per-level, or one flat list?" (Usually list-per-level.)

### Approach 1 — DFS with depth tracking
You *can* DFS and bucket each node into `result[depth]`. It works but is less natural for a level-order question.

```java
public List<List<Integer>> levelOrder(TreeNode root) {
    List<List<Integer>> result = new ArrayList<>();
    dfs(root, 0, result);
    return result;
}
private void dfs(TreeNode node, int depth, List<List<Integer>> result) {
    if (node == null) return;
    if (depth == result.size()) result.add(new ArrayList<>()); // new level seen
    result.get(depth).add(node.val);
    dfs(node.left, depth + 1, result);
    dfs(node.right, depth + 1, result);
}
```
**Time: O(n)**. **Space: O(h)** stack + O(n) output.
*Why a better approach exists:* "Level by level" is *literally* what BFS does — the BFS version reads more clearly and is the expected answer.

### Approach 2 — Optimal: BFS with a queue (the canonical answer)
**Core insight:** Process the queue one full level at a time. Freeze `levelSize = queue.size()` before the loop so you know exactly how many nodes belong to the current level.

```java
public List<List<Integer>> levelOrder(TreeNode root) {
    List<List<Integer>> result = new ArrayList<>();
    if (root == null) return result;
    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);
    while (!queue.isEmpty()) {
        int levelSize = queue.size();              // nodes in THIS level
        List<Integer> level = new ArrayList<>();
        for (int i = 0; i < levelSize; i++) {
            TreeNode node = queue.poll();
            level.add(node.val);
            if (node.left != null) queue.offer(node.left);
            if (node.right != null) queue.offer(node.right);
        }
        result.add(level);
    }
    return result;
}
```

**Dry-run:**
- Queue `[3]`. levelSize=1 → level `[3]`, enqueue 9, 20. result=`[[3]]`.
- Queue `[9,20]`. levelSize=2 → level `[9,20]`, enqueue 15, 7. result=`[[3],[9,20]]`.
- Queue `[15,7]`. levelSize=2 → level `[15,7]`. result=`[[3],[9,20],[15,7]]` ✅.

**Time: O(n)**. **Space: O(w)** — w = max width (queue).

### 🧠 Algorithm to Remember Forever
- **Pattern:** **BFS (Breadth-First Search)** with the **`levelSize` snapshot** trick.
- **Core idea:** "Snapshot the queue size to slice the tree into levels."
- **Memory hook 🏢:** Clear a building floor by floor — count everyone on this floor first, then send them down. 🛗
- **Trigger phrase:** *"level by level / level order / per level / each row"* → BFS + `for (i < levelSize)`.

> 🔑 **The most important pattern of the week.** Right Side View, Zigzag, and "average of each level" are ALL this same loop with a tiny twist.

---

## Problem 6 — Diameter of Binary Tree (Easy, Microsoft ✅)

### 📋 Full Question
Return the length of the **longest path between any two nodes** in the tree, measured in **edges**. The path may or may not pass through the root.

**Example:**
```
      1
     / \
    2   3
   / \
  4   5
```
Input: `root = [1,2,3,4,5]` → **Output: `3`** (path 4 → 2 → 1 → 3, length 3 edges).

### 🗣️ What to say first
- "Diameter is measured in **edges**, not nodes, right?" (LeetCode: edges.)
- "Does the path have to pass through the root?" (No — it can be entirely in one subtree.)

### Approach 1 — Brute Force: compute height for every node
At each node, diameter through it = `height(left) + height(right)`. Compute for all nodes, take the max — but `height()` is recomputed repeatedly.

```java
public int diameterOfBinaryTree(TreeNode root) {
    if (root == null) return 0;
    int through = height(root.left) + height(root.right);
    int left = diameterOfBinaryTree(root.left);
    int right = diameterOfBinaryTree(root.right);
    return Math.max(through, Math.max(left, right));
}
private int height(TreeNode node) {
    if (node == null) return 0;
    return 1 + Math.max(height(node.left), height(node.right));
}
```
**Time: O(n²)** — for each node we recompute heights below it. **Space: O(h)**.
*Why a better approach exists:* We can get the diameter **as a side-effect** of a single height computation — one pass.

### Approach 2 — Optimal: height + diameter in ONE pass
**Core insight:** While computing each node's height, the longest path *through* that node is `leftHeight + rightHeight`. Track a running max as we recurse.

```java
private int maxDiameter = 0;

public int diameterOfBinaryTree(TreeNode root) {
    maxDiameter = 0;                 // reset (LeetCode reuses the object!)
    height(root);
    return maxDiameter;
}

private int height(TreeNode node) {
    if (node == null) return 0;
    int left = height(node.left);
    int right = height(node.right);
    maxDiameter = Math.max(maxDiameter, left + right);  // path through this node (edges)
    return 1 + Math.max(left, right);                   // height for parent
}
```

**Dry-run:**
- `height(4)=1`, `height(5)=1`. At node 2: `left+right = 1+1 = 2` → maxDiameter = 2; returns 2.
- `height(3)=1`. At node 1: `left+right = 2+1 = 3` → maxDiameter = **3** ✅; returns 3.

**Time: O(n)** — single traversal. **Space: O(h)** recursion stack.

### 🧠 Algorithm to Remember Forever
- **Pattern:** DFS where recursion **returns one thing (height)** but **updates a global (the answer)** as a side effect.
- **Core idea:** "Return height up, but record `left + right` along the way."
- **Memory hook 🛣️:** The longest road might be a side street that never touches the capital (root). Check every intersection. 🚗
- **Trigger phrase:** *"longest path / diameter"* → height-DFS + global max of `left + right`.

---

## Problem 7 — Validate Binary Search Tree (Medium, Microsoft ✅ **Frequent!**)

### 📋 Full Question
Given the root of a binary tree, determine if it is a **valid BST**: every node's left subtree contains only values **strictly less**, right subtree only values **strictly greater**, and both subtrees must also be BSTs.

**Example:**
```
    5
   / \
  1   4        ← INVALID: 4 is in 5's right subtree but 4 < 5
     / \
    3   6
```
Input: `root = [5,1,4,null,null,3,6]` → **Output: `false`**.

### 🗣️ What to say first
- "Is it **strict** inequality (no duplicates allowed)?" (LeetCode: strict.)
- "Can values be at the integer boundaries (Integer.MIN/MAX_VALUE)?" (Yes — that's why we use `Integer`/null bounds.)

### Approach 1 — The Classic TRAP ❌
The tempting-but-wrong answer: just check `node.left.val < node.val < node.right.val` at each node.

```java
// ⚠️ WRONG — only checks immediate children, not the whole subtree!
public boolean isValidBST(TreeNode root) {
    if (root == null) return true;
    if (root.left != null && root.left.val >= root.val) return false;
    if (root.right != null && root.right.val <= root.val) return false;
    return isValidBST(root.left) && isValidBST(root.right);
}
```
**Time: O(n)** but **INCORRECT.** `3` is a valid left child of `4`, but it sits in `5`'s right subtree where everything must be `> 5`. Local checks miss this.
*Why a better approach exists:* A node must respect bounds from **all** its ancestors, not just its parent. Pass down a valid **range**.

### Approach 2 — Optimal: pass a (low, high) range down
**Core insight:** Each node must lie strictly inside an inherited range. Going **left** tightens the **upper** bound; going **right** tightens the **lower** bound.

```java
public boolean isValidBST(TreeNode root) {
    return validate(root, null, null);  // null = no bound yet
}
private boolean validate(TreeNode node, Integer low, Integer high) {
    if (node == null) return true;                          // empty is valid
    if (low != null && node.val <= low) return false;       // must be > low
    if (high != null && node.val >= high) return false;     // must be < high
    return validate(node.left, low, node.val)               // left: high = node.val
        && validate(node.right, node.val, high);            // right: low = node.val
}
```
> Using `Integer` (nullable) avoids the `Integer.MIN/MAX_VALUE` edge cases cleanly. `null` means "no bound on that side."

**Dry-run:**
- `validate(5, null, null)` → ok.
- Left: `validate(1, null, 5)` → 1 < 5 ok.
- Right: `validate(4, 5, null)` → 4 must be `> 5`? **4 <= 5 → return false** ✅.

**Time: O(n)**. **Space: O(h)** recursion stack.

### 🔁 Alternative optimal — Inorder must be strictly increasing
Because **inorder of a BST yields sorted order**, check each value is greater than the previous.

```java
private Integer prev = null;
public boolean isValidBST(TreeNode root) {
    prev = null;
    return inorder(root);
}
private boolean inorder(TreeNode node) {
    if (node == null) return true;
    if (!inorder(node.left)) return false;
    if (prev != null && node.val <= prev) return false; // not strictly increasing
    prev = node.val;
    return inorder(node.right);
}
```

### 🧠 Algorithm to Remember Forever
- **Pattern:** DFS with a **(low, high) range** passed down — OR **inorder = sorted** check.
- **Core idea:** "Every node must fit the window its ancestors carved out."
- **Memory hook 🪟:** A bouncer with a guest list that shrinks at each door — left lowers the ceiling, right raises the floor. 🚪
- **Trigger phrase:** *"is it a valid BST / verify BST property"* → range-passing DFS, or inorder-increasing.

---

## Problem 8 — Lowest Common Ancestor of a BST (Medium, Microsoft ✅ **Classic**)

### 📋 Full Question
Given a **BST** and two nodes `p` and `q`, return their **lowest (deepest) common ancestor** — the deepest node that has both as descendants (a node can be a descendant of itself).

**Example:**
```
        6
       / \
      2    8
     / \  / \
    0  4 7   9
      / \
     3   5
```
Input: `root = [6,2,8,0,4,7,9,null,null,3,5]`, `p = 2`, `q = 8` → **Output: `6`**.
(For `p = 2, q = 4` → output `2`, since a node can be its own ancestor.)

### 🗣️ What to say first
- "It's a **BST**, so I can use the sorted property — confirm?"
- "Are `p` and `q` **guaranteed to exist**?" (Usually yes.)
- "Can `p` be an ancestor of `q`?" (Yes — handle that.)

### Approach 1 — Brute Force: treat it as a general binary tree
Ignore the BST property and search both subtrees (the general-tree LCA, Problem 13). Works, but **wastes** the sorted structure.
**Time: O(n)**. *Why a better approach exists:* In a BST, the values themselves tell us which direction to go — we never explore both sides.

### Approach 2 — Optimal: use BST ordering to walk ONE path
**Core insight:**
- If **both** `p` and `q` are **less** than current → LCA is in the **left** subtree.
- If **both** are **greater** → go **right**.
- Otherwise they **split** → current node **is** the LCA. 🎯

```java
public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
    TreeNode node = root;
    while (node != null) {
        if (p.val < node.val && q.val < node.val) {
            node = node.left;            // both smaller → go left
        } else if (p.val > node.val && q.val > node.val) {
            node = node.right;           // both larger → go right
        } else {
            return node;                 // split point → this is the LCA
        }
    }
    return null;
}
```

**Dry-run (p=2, q=8):** At `6`: 2 < 6 but 8 > 6 → split → return **6** ✅.
**Dry-run (p=2, q=4):** At `6`: both < 6 → go left to `2`. At `2`: p=2 equals node → split → return **2** ✅.

**Time: O(h)** — single path down, O(log n) balanced. **Space: O(1)** iterative.

### 🧠 Algorithm to Remember Forever
- **Pattern:** BST search — let the **values steer** you down one path.
- **Core idea:** "Walk down until p and q sit on opposite sides of you."
- **Memory hook 🛤️:** Two travelers on a sorted highway — drive while both are ahead; the moment one is behind and one ahead, you're between them. 🧍
- **Trigger phrase:** *"LCA in a BST"* → compare values, go left/right, stop at the split.

---

## Problem 9 — Kth Smallest Element in a BST (Medium, Microsoft ✅)

### 📋 Full Question
Given a BST, return the value of the **k-th smallest** element (1-indexed).

**Example:**
```
      5
     / \
    3   6
   / \
  2   4
 /
1
```
Input: `root = [5,3,6,2,4,null,null,1]`, `k = 3` → **Output: `3`** (sorted: 1,2,3,4,5,6 → 3rd is 3).

### 🗣️ What to say first
- "Is `k` **1-indexed**?" (Yes — k=1 is the smallest.)
- "Is `k` guaranteed valid (1 ≤ k ≤ n)?"
- "Frequent kth-smallest queries with modifications?" (If so, augment nodes with subtree counts — mention it to impress.)

### Approach 1 — Brute Force: full inorder into a list
Collect the entire sorted order, then index `k-1`.

```java
public int kthSmallest(TreeNode root, int k) {
    List<Integer> sorted = new ArrayList<>();
    inorder(root, sorted);
    return sorted.get(k - 1);
}
private void inorder(TreeNode node, List<Integer> out) {
    if (node == null) return;
    inorder(node.left, out);
    out.add(node.val);
    inorder(node.right, out);
}
```
**Time: O(n)**. **Space: O(n)** for the list.
*Why a better approach exists:* We don't need the whole list — we can **stop early** after k elements.

### Approach 2 — Optimal: inorder traversal with early stop
**Core insight:** Inorder visits BST nodes in **sorted order**. Count as you go; the k-th node visited is the answer.

```java
private int count = 0;
private int answer = 0;

public int kthSmallest(TreeNode root, int k) {
    count = 0;                           // reset (object reused across tests)
    inorder(root, k);
    return answer;
}
private void inorder(TreeNode node, int k) {
    if (node == null) return;
    inorder(node.left, k);
    count++;
    if (count == k) { answer = node.val; return; }  // found the k-th
    if (count < k) inorder(node.right, k);           // only recurse right if not done
}
```

**Dry-run (k=3):** visit 1 (count1), 2 (count2), 3 (count3 == k) → answer = **3** ✅.

**Time: O(h + k)** — descend to smallest, then k steps. **Space: O(h)** recursion stack.

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Inorder traversal** (the BST → sorted superpower) + a counter.
- **Core idea:** "Inorder *is* a sorted walk — count off k steps."
- **Memory hook 🥇:** A race where runners cross in sorted order — watch for the k-th finisher and stop the clock. ⏱️
- **Trigger phrase:** *"k-th smallest / k-th largest / sorted order in BST"* → inorder (reverse-inorder for k-th largest).

---

## Problem 10 — Convert Sorted Array to BST (Easy, Microsoft ✅)

### 📋 Full Question
Given an integer array sorted in **ascending order**, convert it to a **height-balanced** BST (depth of the two subtrees of every node differs by at most one).

**Example:**
```
nums = [-10, -3, 0, 5, 9]

           0
          / \
       -3     9
       /     /
    -10     5         (one valid balanced BST)
```
Input: `nums = [-10,-3,0,5,9]` → **Output:** any height-balanced BST, e.g. `[0,-3,9,-10,null,5]`.

### 🗣️ What to say first
- "Array is already **sorted ascending**, correct?"
- "Does it need to be **height-balanced**? Any valid balanced tree accepted?" (Yes.)
- "Empty array → null root?"

### Approach 1 — Optimal (the only sensible approach): pick the middle as root
**Core insight:** To stay balanced, the **middle element** becomes the root (equal halves on each side). Recurse on the left half for the left subtree and the right half for the right subtree. This is binary-search-style divide and conquer.

```java
public TreeNode sortedArrayToBST(int[] nums) {
    return build(nums, 0, nums.length - 1);
}
private TreeNode build(int[] nums, int lo, int hi) {
    if (lo > hi) return null;                  // empty range
    int mid = lo + (hi - lo) / 2;              // middle (avoids overflow)
    TreeNode root = new TreeNode(nums[mid]);   // middle → balanced root
    root.left = build(nums, lo, mid - 1);      // left half
    root.right = build(nums, mid + 1, hi);     // right half
    return root;
}
```

**Dry-run on `[-10,-3,0,5,9]`:**
- `build(0,4)`: mid=2 → root `0`.
- Left `build(0,1)`: mid=0 → root `-10`... actually mid = 0 → root `-10`, right `build(1,1)` → `-3`. (Balanced.)
- Right `build(3,4)`: mid=3 → root `5`, right `build(4,4)` → `9`.
- Root `0`, balanced ✅.

**Time: O(n)** — each element becomes exactly one node. **Space: O(log n)** recursion stack (balanced by construction).

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Divide & conquer** — middle is root, recurse on halves.
- **Core idea:** "Always grab the middle → both sides stay equal → balanced."
- **Memory hook ⚖️:** Like binary search building a tree — pivot on the middle every time. 🎯
- **Trigger phrase:** *"sorted array → balanced BST / build balanced tree"* → pick mid as root, recurse halves.

---

## Problem 11 — Serialize and Deserialize Binary Tree (Hard, Microsoft ✅ **Top 5!**)

### 📋 Full Question
Design two functions: `serialize` turns a binary tree into a **string**, and `deserialize` turns that string back into the **exact same tree** (a general binary tree, not a BST).

**Example:**
```
    1
   / \
  2   3
     / \
    4   5
```
`serialize(root)` → `"1,2,#,#,3,4,#,#,5,#,#,"` (one valid encoding) → `deserialize(...)` rebuilds the identical tree.

### 🗣️ What to say first
- "Can node values be **negative** / multi-digit?" (Use a delimiter like `,`.)
- "General binary tree (no BST property)?" (Yes.)
- "Any format I want, as long as serialize↔deserialize round-trips?" (Yes.)

### Approach 1 — BFS (level-order) serialization
Works (this is how LeetCode displays trees), but the index bookkeeping for nulls during BFS rebuild is fiddly and error-prone under interview pressure.
*Why a better approach exists:* **Preorder DFS with null markers** is far simpler — recursion rebuilds structure in the same order it was written.

### Approach 2 — Optimal: Preorder DFS + null markers (`#`)
**Core insight:** Write nodes in **preorder** (Root → Left → Right), using `#` for null. To rebuild, read tokens in the **same order**: consume one token as the root, then recursively build left, then right. The `#` markers tell us exactly where subtrees end. 🧩

```java
public class Codec {
    private static final String NULL = "#";
    private static final String SEP = ",";

    // ---- Serialize: preorder DFS ----
    public String serialize(TreeNode root) {
        StringBuilder sb = new StringBuilder();
        buildString(root, sb);
        return sb.toString();
    }
    private void buildString(TreeNode node, StringBuilder sb) {
        if (node == null) {
            sb.append(NULL).append(SEP);        // marker for empty
            return;
        }
        sb.append(node.val).append(SEP);        // root
        buildString(node.left, sb);             // left
        buildString(node.right, sb);            // right
    }

    // ---- Deserialize: consume tokens in the same preorder ----
    public TreeNode deserialize(String data) {
        Queue<String> tokens = new LinkedList<>(Arrays.asList(data.split(SEP)));
        return buildTree(tokens);
    }
    private TreeNode buildTree(Queue<String> tokens) {
        String token = tokens.poll();
        if (token.equals(NULL)) return null;    // marker → empty subtree
        TreeNode node = new TreeNode(Integer.parseInt(token));
        node.left = buildTree(tokens);          // build left first (preorder)
        node.right = buildTree(tokens);         // then right
        return node;
    }
}
```

**Dry-run:**
- `serialize` → `"1,2,#,#,3,4,#,#,5,#,#,"`.
- `deserialize`: poll `1` → node(1); left: poll `2` → node(2), its left `#`→null, right `#`→null; 1's right: poll `3` → node(3), left `4`→node(4) with two `#` children, right `5`→node(5) with two `#` children. Tree rebuilt identically ✅.

**Time: O(n)** both. **Space: O(n)** for the string + O(h) recursion stack.

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Preorder DFS** to write, **preorder consume** to read, with **null markers**.
- **Core idea:** "Record the shape AND the gaps (nulls); rebuild by reading in the same order."
- **Memory hook 📼:** Like recording a video and playing it back — capture the *blank frames* (nulls) too, or playback desyncs. The `#` are your blank frames. ⏯️
- **Trigger phrase:** *"serialize / deserialize / encode a tree / reconstruct a tree"* → preorder + `#` markers + token queue.

> 🚨 One of Microsoft's most-asked questions. Practice writing it from memory until it's automatic.

---

## Problem 12 — Binary Tree Right Side View (Medium, Microsoft ✅)

### 📋 Full Question
Standing on the **right** of the tree, return the values you can see, **top to bottom** (the rightmost node of each level).

**Example:**
```
    1
   / \
  2   3
   \   \
    5   4
```
Input: `root = [1,2,3,null,5,null,4]` → **Output: `[1, 3, 4]`**.

### 🗣️ What to say first
- "Right side = the **last** (rightmost) node of each level, right?"
- "Empty tree → `[]`?"

### Approach 1 — Brute Force: full level order, take last of each
Run the BFS from Problem 5, then for each level grab `level.get(level.size()-1)`. Works — just builds extra lists we throw away.
*Why a better approach exists:* We only need the last node per level, so grab it during BFS without storing whole levels.

### Approach 2 — Optimal: BFS, capture the last node of each level
**Core insight:** It's the level-order BFS again — but when `i == levelSize - 1` we're at the rightmost node.

```java
public List<Integer> rightSideView(TreeNode root) {
    List<Integer> result = new ArrayList<>();
    if (root == null) return result;
    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);
    while (!queue.isEmpty()) {
        int levelSize = queue.size();
        for (int i = 0; i < levelSize; i++) {
            TreeNode node = queue.poll();
            if (i == levelSize - 1) result.add(node.val);  // rightmost of level
            if (node.left != null) queue.offer(node.left);
            if (node.right != null) queue.offer(node.right);
        }
    }
    return result;
}
```

**Dry-run:** Level `[1]` → 1. Level `[2,3]` → 3. Level `[5,4]` → 4 → result `[1,3,4]` ✅.

**Time: O(n)**. **Space: O(w)**.

### 🧠 Algorithm to Remember Forever
- **Pattern:** **BFS with `levelSize`** (same engine as Level Order) + "take the last one."
- **Core idea:** "Walk each floor left-to-right; remember only whoever's at the right window."
- **Memory hook 🪟:** Standing outside on the right, you only see the person at the rightmost window. 👀
- **Trigger phrase:** *"right side view / rightmost / what you see from the side"* → BFS + grab the boundary node per level.

---

## Problem 13 — Lowest Common Ancestor of a Binary Tree (Medium, Microsoft ✅ **Frequent**)

### 📋 Full Question
Same as Problem 8, but now a **general binary tree** — **no** BST ordering to exploit. Find the LCA of nodes `p` and `q`.

**Example:**
```
        3
       / \
      5    1
     / \  / \
    6   2 0  8
       / \
      7   4
```
Input: `root = [3,5,1,6,2,0,8,null,null,7,4]`, `p = 5`, `q = 1` → **Output: `3`**.
(For `p = 5, q = 4` → output `5`, since 5 is an ancestor of 4.)

### 🗣️ What to say first
- "Are both `p` and `q` **guaranteed present**?" (Usually yes.)
- "Can one node be the ancestor of the other?" (Yes.)
- "No BST property here, correct?" (Correct — we search both sides.)

### Approach 1 — Brute Force: find root-to-node paths, compare
Find the path from root to `p` and root to `q` as two lists, then walk both until they diverge — last common node is the LCA.
**Time: O(n)**, **Space: O(n)** for storing paths, plus two traversals.
*Why a better approach exists:* We can find the LCA in a **single recursive pass** with O(h) space — the expected answer.

### Approach 2 — Optimal: single-pass recursion
**Core insight:** Recurse left and right.
- A subtree containing `p` or `q` returns that node (bubbles a "found" signal up).
- If one is found **left** and the other **right** → the **current node** is the LCA.
- If only one side returns non-null, the LCA is up that side.

```java
public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
    if (root == null || root == p || root == q) return root;  // found one, or empty
    TreeNode left = lowestCommonAncestor(root.left, p, q);
    TreeNode right = lowestCommonAncestor(root.right, p, q);
    if (left != null && right != null) return root;  // p and q split here → LCA!
    return (left != null) ? left : right;            // both on one side (or neither)
}
```

**Dry-run (p=5, q=1):** At `3`: left subtree returns `5`, right subtree returns `1` → both non-null → return **3** ✅.
**Dry-run (p=5, q=4):** Subtree at `5` returns `5` immediately (`root == p`); right side null → bubbles up to **5** ✅ (ancestor-of-self handled by `root == p`).

**Time: O(n)** — may visit every node. **Space: O(h)** recursion stack.

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Post-order DFS** that **bubbles up** found nodes; the split point is the LCA.
- **Core idea:** "If p and q come back from different sides, I'm the meeting point."
- **Memory hook 🤝:** Two people lost in a building send signals up the staircases — where left and right signals both arrive is where they'd reunite. 🪜
- **Trigger phrase:** *"LCA in a binary tree (not BST)"* → recurse both sides, split = answer.

---

## Problem 14 — Binary Tree Zigzag Level Order Traversal (Medium, Microsoft ✅)

### 📋 Full Question
Return the level-order traversal, but **alternate direction** per level: left-to-right, then right-to-left, then left-to-right, etc. (a zigzag / spiral).

**Example:**
```
        3
       / \
      9   20
         /  \
        15   7
```
Input: `root = [3,9,20,null,null,15,7]` → **Output: `[[3], [20,9], [15,7]]`**.

### 🗣️ What to say first
- "First level left-to-right, then flip each subsequent level?" (Yes.)
- "Empty tree → `[]`?"

### Approach 1 — BFS then reverse alternate levels
Do plain level-order BFS, then `Collections.reverse` every odd-indexed level. Simple and correct.

```java
public List<List<Integer>> zigzagLevelOrder(TreeNode root) {
    List<List<Integer>> result = new ArrayList<>();
    if (root == null) return result;
    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);
    boolean leftToRight = true;
    while (!queue.isEmpty()) {
        int levelSize = queue.size();
        List<Integer> level = new ArrayList<>();
        for (int i = 0; i < levelSize; i++) {
            TreeNode node = queue.poll();
            level.add(node.val);
            if (node.left != null) queue.offer(node.left);
            if (node.right != null) queue.offer(node.right);
        }
        if (!leftToRight) Collections.reverse(level);
        result.add(level);
        leftToRight = !leftToRight;
    }
    return result;
}
```
**Time: O(n)** (reversal is O(level) total O(n)). **Space: O(w)**.

### Approach 2 — Optimal: insert at front for right-to-left levels
**Core insight:** Avoid the extra reverse by inserting at index 0 of a `LinkedList` (a deque) when the level should be reversed — building it backwards as we go.

```java
public List<List<Integer>> zigzagLevelOrder(TreeNode root) {
    List<List<Integer>> result = new ArrayList<>();
    if (root == null) return result;
    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);
    boolean leftToRight = true;
    while (!queue.isEmpty()) {
        int levelSize = queue.size();
        LinkedList<Integer> level = new LinkedList<>();
        for (int i = 0; i < levelSize; i++) {
            TreeNode node = queue.poll();
            if (leftToRight) level.addLast(node.val);   // normal order
            else             level.addFirst(node.val);  // build reversed
            if (node.left != null) queue.offer(node.left);
            if (node.right != null) queue.offer(node.right);
        }
        result.add(level);
        leftToRight = !leftToRight;
    }
    return result;
}
```

**Dry-run:** Level0 `[3]` (L→R). Level1 nodes 9,20 with addFirst → `[20,9]`. Level2 nodes 15,7 (L→R) → `[15,7]`. Result `[[3],[20,9],[15,7]]` ✅.

**Time: O(n)**. **Space: O(w)**.

### 🧠 Algorithm to Remember Forever
- **Pattern:** **BFS + `levelSize`** (Level Order engine) + a `leftToRight` flag.
- **Core idea:** "Same level-order loop, just flip insert direction each level."
- **Memory hook 🐍:** A snake slithering down the tree, switching sides each row. 〰️
- **Trigger phrase:** *"zigzag / spiral / alternate direction per level"* → BFS + alternate insert (addFirst/addLast).

---

## Problem 15 — Construct Binary Tree from Preorder and Inorder Traversal (Medium, Microsoft ✅)

### 📋 Full Question
Given two integer arrays `preorder` and `inorder` (no duplicate values) representing the preorder and inorder traversal of a binary tree, **rebuild and return the tree**.

**Example:**
```
preorder = [3, 9, 20, 15, 7]
inorder  = [9, 3, 15, 20, 7]

        3
       / \
      9   20
         /  \
        15   7
```

### 🗣️ What to say first
- "Are all values **unique**?" (Yes — needed to locate the root in inorder.)
- "Preorder = Root,Left,Right and Inorder = Left,Root,Right, correct?"

### Approach 1 — Recursion with array search (clear but slower)
**Core insight:** `preorder[0]` is the **root**. Find that root in `inorder`: everything to its **left** is the left subtree, everything to its **right** is the right subtree. Recurse.

```java
public TreeNode buildTree(int[] preorder, int[] inorder) {
    return build(preorder, 0, preorder.length - 1,
                 inorder, 0, inorder.length - 1);
}
private TreeNode build(int[] pre, int preLo, int preHi,
                       int[] in, int inLo, int inHi) {
    if (preLo > preHi) return null;
    int rootVal = pre[preLo];
    TreeNode root = new TreeNode(rootVal);
    int idx = inLo;
    while (in[idx] != rootVal) idx++;          // find root in inorder  (O(n))
    int leftSize = idx - inLo;
    root.left  = build(pre, preLo + 1, preLo + leftSize, in, inLo, idx - 1);
    root.right = build(pre, preLo + leftSize + 1, preHi, in, idx + 1, inHi);
    return root;
}
```
**Time: O(n²)** worst case (linear search per node). **Space: O(h)** stack.
*Why a better approach exists:* The repeated linear search for the root is wasteful — a hash map gives O(1) lookups.

### Approach 2 — Optimal: HashMap for inorder index + preorder pointer
**Core insight:** Precompute `value → index` in inorder for O(1) root location, and walk preorder with a single moving pointer.

```java
private int preIndex = 0;
private Map<Integer, Integer> inorderIndex = new HashMap<>();

public TreeNode buildTree(int[] preorder, int[] inorder) {
    preIndex = 0;
    inorderIndex.clear();                          // reset for object reuse
    for (int i = 0; i < inorder.length; i++) inorderIndex.put(inorder[i], i);
    return build(preorder, 0, inorder.length - 1);
}
private TreeNode build(int[] preorder, int inLo, int inHi) {
    if (inLo > inHi) return null;
    int rootVal = preorder[preIndex++];            // next preorder value = root
    TreeNode root = new TreeNode(rootVal);
    int mid = inorderIndex.get(rootVal);           // O(1) split point
    root.left  = build(preorder, inLo, mid - 1);   // build LEFT first (preorder order!)
    root.right = build(preorder, mid + 1, inHi);
    return root;
}
```
> ⚠️ Build the **left** subtree before the right — preorder consumes left-subtree nodes before right-subtree nodes.

**Dry-run:** preIndex 0 → root `3`, mid=1 in inorder. Left `build(0,0)` → root `9` (leaf). Right `build(2,4)` → root `20`, mid=3 → left `15`, right `7`. Tree matches ✅.

**Time: O(n)**. **Space: O(n)** for the map + O(h) recursion stack.

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Divide & conquer** — preorder gives the root, inorder splits into halves.
- **Core idea:** "First preorder value is the root; its position in inorder cuts left from right."
- **Memory hook 🧱:** Pre gives the root brick; in tells you how many bricks go left vs right. 🏗️
- **Trigger phrase:** *"build / construct tree from preorder + inorder"* → root from preorder, split inorder, recurse left-then-right.

---

## Problem 16 — Path Sum II (Medium, Microsoft ✅)

### 📋 Full Question
Given the root and a target sum, return **all root-to-leaf paths** where the sum of node values equals the target. Each path is a list of values.

**Example:**
```
          5
         / \
        4   8
       /   / \
      11  13  4
     /  \    / \
    7    2  5   1
```
Input: `targetSum = 22` → **Output: `[[5,4,11,2], [5,8,4,5]]`** (both sum to 22).

### 🗣️ What to say first
- "Path must go from **root to a leaf** (not partial)?" (Yes.)
- "Can values be negative?" (Possibly — so we can't prune early on sum alone.)
- "Return all paths, or just count?" (All paths here.)

### Approach 1 — DFS with backtracking (the standard answer)
**Core insight:** Walk down each root-to-leaf path tracking the current path and remaining sum. At a **leaf**, if remaining hits the leaf's value, record a **copy** of the path. **Backtrack** (remove the node) when returning so the path list is reused cleanly.

```java
public List<List<Integer>> pathSum(TreeNode root, int targetSum) {
    List<List<Integer>> result = new ArrayList<>();
    dfs(root, targetSum, new ArrayList<>(), result);
    return result;
}
private void dfs(TreeNode node, int remaining,
                 List<Integer> path, List<List<Integer>> result) {
    if (node == null) return;
    path.add(node.val);                                  // choose
    if (node.left == null && node.right == null && remaining == node.val) {
        result.add(new ArrayList<>(path));               // leaf hits target → copy!
    } else {
        dfs(node.left,  remaining - node.val, path, result);
        dfs(node.right, remaining - node.val, path, result);
    }
    path.remove(path.size() - 1);                        // backtrack (un-choose)
}
```
> 🔑 Two non-obvious points: (1) add a **copy** `new ArrayList<>(path)` — the live `path` keeps mutating. (2) **Backtrack** by removing the last element so siblings start from the right prefix.

**Dry-run (target 22):** path 5→4→11→7 sums 27 (≠22, 7 isn't where remaining matches). path 5→4→11→2: at leaf 2, remaining was 22-5-4-11=2 == 2 → record `[5,4,11,2]` ✅. path 5→8→4→5: remaining 22-5-8-4=5 == 5 → record `[5,8,4,5]` ✅.

**Time: O(n)** to visit + O(n) per path copy → O(n²) worst case for output. **Space: O(h)** recursion + path.

### 🧠 Algorithm to Remember Forever
- **Pattern:** **DFS + backtracking** with a running path list.
- **Core idea:** "Hike down, drop breadcrumbs, record at the leaf, then pick the breadcrumbs back up."
- **Memory hook 🥾:** A hiker leaving breadcrumbs on the trail, collecting them on the way back. 🍞
- **Trigger phrase:** *"all root-to-leaf paths / path sum / collect paths"* → DFS + backtracking (add copy at leaf, remove on return).

---

## Problem 17 — Flatten Binary Tree to Linked List (Medium, Microsoft ✅)

### 📋 Full Question
Flatten the tree **in place** into a "linked list": each node's `right` points to the next node in **preorder**, and every `left` is set to `null`.

**Example:**
```
     1                1
    / \                \
   2   5      →         2
  / \   \                \
 3   4   6                3
                           \
                            4
                             \
                              5
                               \
                                6
```
Input: `root = [1,2,5,3,4,null,6]` → **Output: `[1,null,2,null,3,null,4,null,5,null,6]`** (preorder, all on the right).

### 🗣️ What to say first
- "Flatten **in place** (no new nodes), order = **preorder**, correct?"
- "Set all `left` pointers to null?" (Yes.)

### Approach 1 — Recursion using a global "previous" pointer
**Core insight:** Process nodes in **reverse preorder** (Right → Left → Root). Keep a `prev` pointer to the node that should come *after* the current one; rewire current's right to `prev`, left to null.

```java
private TreeNode prev = null;

public void flatten(TreeNode root) {
    prev = null;
    reverse(root);
}
private void reverse(TreeNode node) {
    if (node == null) return;
    reverse(node.right);      // process right first
    reverse(node.left);       // then left
    node.right = prev;        // current's next = previously processed node
    node.left = null;         // clear left
    prev = node;              // current becomes the new "previous"
}
```
**Time: O(n)**. **Space: O(h)** recursion stack.
*Why a better approach exists:* This is clean, but interviewers love the **O(1) space** Morris-style version below.

### Approach 2 — Optimal: O(1) space, rewire with the predecessor
**Core insight:** For each node with a left child, find the **rightmost node of the left subtree** (its preorder predecessor of the right subtree). Splice the right subtree below it, move the left subtree to the right, and move on.

```java
public void flatten(TreeNode root) {
    TreeNode cur = root;
    while (cur != null) {
        if (cur.left != null) {
            TreeNode prev = cur.left;            // rightmost of left subtree
            while (prev.right != null) prev = prev.right;
            prev.right = cur.right;              // attach old right subtree there
            cur.right = cur.left;                // move left subtree to the right
            cur.left = null;                     // clear left
        }
        cur = cur.right;                         // advance down the new chain
    }
}
```

**Dry-run:** At `1` (left=2): rightmost of left subtree is `4`; `4.right = 5`; `1.right = 2`, `1.left = null`. Move to `2` (left=3): rightmost is `3`; `3.right = 4`; `2.right = 3`, `2.left = null`. Continue → chain `1→2→3→4→5→6` ✅.

**Time: O(n)** — each node visited a constant number of times. **Space: O(1)** — no recursion, no stack.

### 🧠 Algorithm to Remember Forever
- **Pattern:** In-place rewiring — splice left subtree to the right, hook old right below the left subtree's tail.
- **Core idea:** "Slide each left subtree onto the right spine, in preorder."
- **Memory hook 🧵:** Unspooling the tree into a single thread that always goes right. ➡️
- **Trigger phrase:** *"flatten tree to linked list / in-place preorder chain"* → rightmost-of-left splice (O(1)) or reverse-preorder + prev.

---

## Problem 18 — Count Good Nodes in Binary Tree (Medium, Microsoft ✅)

### 📋 Full Question
A node `X` is **good** if, on the path from the **root** to `X`, there is no node with a value **greater than** `X`. Return the number of good nodes. (The root is always good.)

**Example:**
```
        3
       / \
      1    4
     /    / \
    3    1   5
```
Input: `root = [3,1,4,3,null,1,5]` → **Output: `4`**.
Good nodes: `3` (root), `4` (3≤4), `5` (max on path ≤5), and the leaf `3` under the first `1` (path max is 3, and 3 ≥ 3). The `1`s are NOT good (a `3` precedes them).

### 🗣️ What to say first
- "Good = value ≥ the **max** value seen on the path from root, correct?"
- "Root counts as good?" (Yes.)
- "Can values be negative?" (Possibly — so start the running max at the root's value or Integer.MIN_VALUE.)

### Approach 1 — Optimal (the natural approach): DFS carrying the path max
**Core insight:** Walk top-down, passing the **maximum value seen so far** on the path. A node is good if its value ≥ that max. Update the max as you descend.

```java
public int goodNodes(TreeNode root) {
    return dfs(root, Integer.MIN_VALUE);   // nothing seen yet → MIN so root counts
}
private int dfs(TreeNode node, int maxSoFar) {
    if (node == null) return 0;
    int count = (node.val >= maxSoFar) ? 1 : 0;       // good if ≥ path max
    int newMax = Math.max(maxSoFar, node.val);        // update running max
    count += dfs(node.left, newMax);
    count += dfs(node.right, newMax);
    return count;
}
```

**Dry-run:**
- `3` (root): 3 ≥ MIN → good (1). newMax=3.
- Left `1`: 1 ≥ 3? no. newMax stays 3. Its child `3`: 3 ≥ 3 → good (1).
- Right `4`: 4 ≥ 3 → good (1). newMax=4. Child `1`: 1 ≥ 4? no. Child `5`: 5 ≥ 4 → good (1). newMax=5.
- Total good = root + 4 + 5 + leaf 3 = **4** ✅.

**Time: O(n)** — visit each node once. **Space: O(h)** recursion stack.

### Approach 2 — Iterative DFS with a stack (avoids recursion stack on deep trees)
Same logic, carry the running max on the stack alongside the node.

```java
public int goodNodes(TreeNode root) {
    if (root == null) return 0;
    Deque<TreeNode> nodes = new ArrayDeque<>();
    Deque<Integer> maxes = new ArrayDeque<>();
    nodes.push(root); maxes.push(Integer.MIN_VALUE);
    int count = 0;
    while (!nodes.isEmpty()) {
        TreeNode node = nodes.pop();
        int maxSoFar = maxes.pop();
        if (node.val >= maxSoFar) count++;
        int newMax = Math.max(maxSoFar, node.val);
        if (node.left != null)  { nodes.push(node.left);  maxes.push(newMax); }
        if (node.right != null) { nodes.push(node.right); maxes.push(newMax); }
    }
    return count;
}
```
**Time: O(n)**. **Space: O(h)** explicit stack.

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Top-down DFS** carrying state (the path max) *down* the recursion.
- **Core idea:** "Pass the biggest-so-far down; count whoever isn't overshadowed."
- **Memory hook 🏔️:** Climbing a ridge — you can see the view ("good") only if no peak before you was taller. 🥾
- **Trigger phrase:** *"good nodes / no greater value on the path / running max along root-to-node"* → top-down DFS passing the max.

---

## 📊 Complexity Cheat Sheet (all 18)

| # | Problem | Brute Force | Optimal | Technique |
|---|---------|-------------|---------|-----------|
| 1 | Maximum Depth | O(n) / O(w) (BFS) | **O(n) / O(h)** | Recursion `max(L,R)+1` |
| 2 | Invert Binary Tree | O(n) / O(w) (BFS) | **O(n) / O(h)** | Swap children recursively |
| 3 | Same Tree | — | **O(n) / O(h)** | Lockstep DFS (or 2-queue BFS) |
| 4 | Symmetric Tree | O(n) / O(w) (BFS) | **O(n) / O(h)** | Mirror DFS (crossed children) |
| 5 | Level Order Traversal | O(n) / O(h) (DFS bucket) | **O(n) / O(w)** | BFS + `levelSize` snapshot |
| 6 | Diameter | O(n²) / O(h) | **O(n) / O(h)** | Height-DFS + global max `L+R` |
| 7 | Validate BST | O(n) but WRONG (local) | **O(n) / O(h)** | Range (low,high) DFS or inorder |
| 8 | LCA of BST | O(n) (general LCA) | **O(h) / O(1)** | Walk down via value comparison |
| 9 | Kth Smallest in BST | O(n) / O(n) (full list) | **O(h+k) / O(h)** | Inorder + counter, early stop |
| 10 | Sorted Array → BST | — | **O(n) / O(log n)** | Divide & conquer, mid = root |
| 11 | Serialize/Deserialize | O(n) / O(n) (BFS, fiddly) | **O(n) / O(n)** | Preorder DFS + `#` markers + queue |
| 12 | Right Side View | O(n) / O(n) (full levels) | **O(n) / O(w)** | BFS + last node per level |
| 13 | LCA of Binary Tree | O(n) / O(n) (path lists) | **O(n) / O(h)** | Post-order recursion, split = LCA |
| 14 | Zigzag Level Order | O(n) / O(w) (reverse) | **O(n) / O(w)** | BFS + alternate insert direction |
| 15 | Build from Pre+Inorder | O(n²) / O(h) (search) | **O(n) / O(n)** | Root from pre, split inorder (hashmap) |
| 16 | Path Sum II | — | **O(n²) / O(h)** | DFS + backtracking path list |
| 17 | Flatten to Linked List | O(n) / O(h) (recursion) | **O(n) / O(1)** | Rightmost-of-left splice (in place) |
| 18 | Count Good Nodes | — | **O(n) / O(h)** | Top-down DFS carrying path max |

> *n = nodes, h = height, w = max width. Balanced tree: h ≈ log n. Skewed tree: h ≈ n. Recursion uses O(h) stack space.*

---

## 🧠🧠 ALGORITHM RECOGNITION TABLE

> When you hear the trigger, your hand should already be reaching for the algorithm.

| If the question says... (Trigger) | Use Algorithm | Memory Hook | Big-O |
|---|---|---|---|
| "depth / height / how tall" | Recursion `1 + max(L, R)` | 🌳 Tallest child + me | O(n) / O(h) |
| "invert / mirror / flip the tree" | Swap children recursively | 🔄 Hold tree to a mirror | O(n) / O(h) |
| "same / identical / equal trees" | Lockstep DFS (compare in parallel) | 👯 Two dancers, same beat | O(n) / O(h) |
| "symmetric / mirror of itself" | Mirror DFS (a.left ↔ b.right) | 🦋 Butterfly wings match flipped | O(n) / O(h) |
| "level by level / per level / each row" | **BFS + `levelSize` loop** | 🏢 Clear the building floor by floor | O(n) / O(w) |
| "longest path / diameter" | Height-DFS + global max `L+R` | 🛣️ Longest road may skip the capital | O(n) / O(h) |
| "is it a valid BST" | Range (low, high) DFS / inorder | 🪟 Shrinking guest-list window | O(n) / O(h) |
| "LCA in a **BST**" | Walk down by value comparison | 🛤️ Drive till they split | O(h) / O(1) |
| "k-th smallest / sorted order in BST" | **Inorder traversal** + counter | 🥇 Watch the k-th finisher | O(h+k) / O(h) |
| "sorted array → balanced BST" | Divide & conquer, mid = root | ⚖️ Binary-search the middle | O(n) / O(log n) |
| "serialize / encode / reconstruct tree" | **Preorder DFS + `#` markers** | 📼 Record the blank frames too | O(n) / O(n) |
| "right side view / rightmost / side view" | BFS + take boundary node per level | 🪟 Person at the right window | O(n) / O(w) |
| "LCA in a **binary tree**" | Post-order recursion, split = ans | 🤝 Signals meet at the staircase | O(n) / O(h) |
| "zigzag / spiral / alternate per level" | BFS + alternate insert direction | 🐍 Snake slithering down levels | O(n) / O(w) |
| "build tree from preorder + inorder" | Root from pre, split inorder (map) | 🧱 Pre gives root, in gives the halves | O(n) / O(n) |
| "all root-to-leaf paths / path sum" | DFS + backtracking path list | 🥾 Hike, drop breadcrumbs, backtrack | O(n²) / O(h) |
| "flatten / in-place preorder chain" | Rightmost-of-left splice (in place) | 🧵 Unspool tree into one thread | O(n) / O(1) |
| "good nodes / no greater on the path" | Top-down DFS carrying running max | 🏔️ See the view if no taller peak before | O(n) / O(h) |

---

## ☕ Java Tips for Trees (memorize these templates)

### 1. The TreeNode (always given, but know it cold)
```java
class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode(int val) { this.val = val; }
}
```

### 2. DFS recursion template (the workhorse)
```java
ReturnType dfs(TreeNode node) {
    if (node == null) return /* base case: 0, true, null, etc. */;
    ReturnType left  = dfs(node.left);
    ReturnType right = dfs(node.right);
    return /* combine left, right, and node.val */;
}
```
**Always handle the `node == null` base case first** — it prevents NullPointerExceptions and is your recursion's stopping condition.

### 3. BFS with a Queue (level-order template)
```java
Queue<TreeNode> queue = new LinkedList<>();   // LinkedList implements Queue
queue.offer(root);                            // add to back
while (!queue.isEmpty()) {
    int levelSize = queue.size();             // 🔑 freeze the level count
    for (int i = 0; i < levelSize; i++) {
        TreeNode node = queue.poll();         // remove from front
        // ... process node ...
        if (node.left  != null) queue.offer(node.left);
        if (node.right != null) queue.offer(node.right);
    }
}
```
- `offer()` = enqueue, `poll()` = dequeue, `peek()` = front without removing.
- `poll()`/`peek()` return `null` on empty (no exception) — but always guard with `!queue.isEmpty()`.

### 4. Inorder = sorted (BST superpower)
```java
void inorder(TreeNode node, List<Integer> out) {
    if (node == null) return;
    inorder(node.left, out);    // Left
    out.add(node.val);          // Root  ← values come out SORTED for a BST
    inorder(node.right, out);   // Right
}
```
- **Inorder** (L,Root,R) → sorted ascending. Use for *validate BST*, *kth smallest*.
- **Reverse inorder** (R,Root,L) → sorted descending. Use for *kth largest*.

### 5. Top-down vs bottom-up DFS (know which state goes where)
- **Bottom-up** (return values up): depth, diameter, LCA, same tree, symmetric. Combine children's results.
- **Top-down** (pass state down): validate BST (range), count good nodes (path max), path sum (running path). Carry context into the recursion.

### 6. Returning multiple things from recursion
Java has no tuples. Options:
- Use an **instance/field variable** (e.g., `private int maxDiameter`) updated as a side effect.
- Use a 1-element array as a mutable holder: `int[] result = {0};` then mutate `result[0]`.
- Return a small helper class / `int[]` of values.

### 7. Watch out for these Java gotchas
- Use `Integer` (nullable) instead of `int` when you need "no bound" sentinels (Validate BST).
- For huge values use `Long` to avoid `Integer.MIN/MAX_VALUE` overflow in comparisons.
- Compute mid with `lo + (hi - lo) / 2` to avoid integer overflow (Sorted Array → BST).
- In Path Sum / backtracking, add a **copy** `new ArrayList<>(path)` to results, then **remove** the last element to backtrack.
- `Math.max(a, b)` / `Math.min(a, b)` — your constant companions.
- **Reset any instance/field variables** at the **start** of the public method (LeetCode reuses the object across test cases!).
- `Arrays.asList(data.split(","))` to turn a serialized string into a token list.
- `Collections.reverse(list)` or `LinkedList.addFirst()` for zigzag.

---

## ✅ Final Interview Checklist

Before you say "I'm done," confirm:

- [ ] **I restated the problem** and gave a concrete example before coding.
- [ ] **I asked clarifying questions** (empty tree? BST or general? duplicates? negatives?).
- [ ] **I mentioned the brute force** before jumping to optimal (shows range).
- [ ] **I picked the right engine**: recursion for depth/path/structure, BFS for level-based, inorder for BST-sorted.
- [ ] **I handled the `null` / empty-tree base case** first.
- [ ] **I dry-ran my code** on the example, out loud, line by line.
- [ ] **I stated time AND space complexity** in terms of n, h, w — including recursion stack space.
- [ ] **I named edge cases**: empty tree, single node, skewed (linked-list) tree, duplicate values.
- [ ] **I reset instance variables** if I used any (LeetCode reuses the object).
- [ ] **For the famous ones** (Serialize/Deserialize, Level Order, Validate BST, both LCAs), I can write them **from memory** with zero hesitation.

> 🎯 **Master these for Microsoft above all else:** Serialize/Deserialize (Top 5), Level Order Traversal (Very Frequent), Validate BST (Frequent), and both LCA variants. If you can write those cold, you're interview-ready for the tree round. Good luck! 🍀
