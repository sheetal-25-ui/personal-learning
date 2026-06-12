# Week 5: Trees & BST — How to ANSWER in a Microsoft Interview 🎤 (Java)

> Microsoft asks tree questions in **every single round**. If you master this week, you've covered roughly a third of what they throw at you. The good news: 90% of tree problems reduce to **recursion** or **BFS with a queue**. Learn those two engines and you can answer almost anything.

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
7. **"The time complexity is O(...), space is O(...)."** → always end with complexity.
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

# 📚 The Problems

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

### Approach 1 — Brute Force / Iterative BFS counting levels
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
**Time: O(n)** — visit every node. **Space: O(w)** — w = max width of tree (queue size).
*Why a better approach exists:* It works and is O(n), but recursion expresses "depth" more naturally and is shorter — and the recursive pattern generalizes to nearly every other tree problem this week.

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
- **Pattern:** DFS via recursion (post-order — children before parent).
- **Core idea:** "My answer = combine my children's answers + 1 for me."
- **Memory hook 🌳:** Like measuring a family tree's generations — count your tallest child's line, then add yourself. 👨‍👧
- **Trigger phrase:** *"depth / height / how tall"* → recursion returning `max(left, right) + 1`.

---

## Problem 2 — Diameter of Binary Tree (Easy, Microsoft ✅)

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
Input: `root = [1,2,3,4,5]` → **Output: `3`** (path 4 → 2 → 5 → ... actually 4 → 2 → 1 → 3, length 3 edges).

### 🗣️ What to say first
- "Diameter is measured in **edges**, not nodes, right?" (LeetCode: edges.)
- "Does the path have to pass through the root?" (No — it can be entirely in one subtree.)

### Approach 1 — Brute Force: compute height for every node
At each node, diameter through it = `height(left) + height(right)`. Compute that for all nodes, take the max. But calling `height()` separately at every node recomputes heights repeatedly.

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
*Why a better approach exists:* We compute heights over and over. We can get the diameter **as a side-effect** of a single height computation — one pass.

### Approach 2 — Optimal: height + diameter in ONE pass
**Core insight:** While computing each node's height, the longest path *through* that node is `leftHeight + rightHeight`. Track a running max as we recurse.

```java
private int maxDiameter = 0;

public int diameterOfBinaryTree(TreeNode root) {
    maxDiameter = 0;
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

**Dry-run on the example:**
- `height(4)=1`, `height(5)=1`. At node 2: `left+right = 1+1 = 2` → maxDiameter = 2; returns 2.
- `height(3)=1`. At node 1: `left+right = 2+1 = 3` → maxDiameter = **3** ✅; returns 3.

**Time: O(n)** — single traversal. **Space: O(h)**.

### 🧠 Algorithm to Remember Forever
- **Pattern:** DFS where the recursion **returns one thing (height)** but **updates a global (the answer)** as a side effect.
- **Core idea:** "Return height up, but record `left + right` along the way."
- **Memory hook 🛣️:** Like a road network — the longest road might be a side street that never touches the capital (root). Check every intersection. 🚗
- **Trigger phrase:** *"longest path / diameter"* → height-DFS + global max of `left + right`.

---

## Problem 3 — Binary Tree Level Order Traversal (Medium, Microsoft ✅ **Very Frequent**)

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
- "Do you want a list-per-level, or one flat list?" (Usually list-per-level.)

### Approach 1 — Brute Force: recursion with depth tracking
You *can* DFS and bucket each node into `result[depth]`. It works but is less natural for a level-order question and obscures the intent.

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
*Why a better approach exists:* It works, but "level by level" is *literally* what BFS does. The BFS version reads more clearly and is the answer the interviewer expects for this classic.

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

**Dry-run on the example:**
- Queue `[3]`. levelSize=1 → level `[3]`, enqueue 9, 20. result=`[[3]]`.
- Queue `[9,20]`. levelSize=2 → level `[9,20]`, enqueue 15, 7. result=`[[3],[9,20]]`.
- Queue `[15,7]`. levelSize=2 → level `[15,7]`. result=`[[3],[9,20],[15,7]]` ✅.

**Time: O(n)**. **Space: O(w)** — w = max width (queue).

### 🧠 Algorithm to Remember Forever
- **Pattern:** **BFS (Breadth-First Search)** with the **`levelSize` snapshot** trick.
- **Core idea:** "Snapshot the queue size to slice the tree into levels."
- **Memory hook 🏢:** Like clearing a building floor by floor — count everyone on this floor first, then send them down and let the next floor in. 🛗
- **Trigger phrase:** *"level by level / level order / per level / each row"* → BFS + `for (i < levelSize)`.

> 🔑 **This is the most important pattern of the week.** Right Side View, Zigzag, and "average of each level" are ALL this same loop with a tiny twist.

---

## Problem 4 — Validate Binary Search Tree (Medium, Microsoft ✅ **Frequent!**)

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
- "Can values be at the integer boundaries (Integer.MIN_VALUE / MAX_VALUE)?" (Yes — that's why we use `Long` or null bounds.)

### Approach 1 — Brute Force / The Classic TRAP ❌
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
**Time: O(n)** but **INCORRECT.** In the example, `3` is a valid left child of `4`, but `3` sits in `5`'s right subtree where everything must be `> 5`. Local checks miss this.
*Why a better approach exists:* A node must respect bounds from **all** its ancestors, not just its parent. We must pass down a valid **range**.

### Approach 2 — Optimal: pass a (min, max) range down
**Core insight:** Each node must lie strictly inside an inherited range. Going **left** tightens the **upper** bound; going **right** tightens the **lower** bound.

```java
public boolean isValidBST(TreeNode root) {
    return validate(root, null, null);  // null = no bound yet
}
private boolean validate(TreeNode node, Integer low, Integer high) {
    if (node == null) return true;                          // empty is valid
    if (low != null && node.val <= low) return false;       // must be > low
    if (high != null && node.val >= high) return false;     // must be < high
    // left subtree: high becomes node.val ; right subtree: low becomes node.val
    return validate(node.left, low, node.val)
        && validate(node.right, node.val, high);
}
```
> Using `Integer` (nullable) avoids the `Integer.MIN/MAX_VALUE` edge cases cleanly. `null` means "no bound on that side."

**Dry-run on the example:**
- `validate(5, null, null)` → ok.
- Left: `validate(1, null, 5)` → 1 < 5 ok.
- Right: `validate(4, 5, null)` → 4 must be `> 5`? **4 <= 5 → return false** ✅. Correctly rejected!

**Time: O(n)**. **Space: O(h)**.

### 🔁 Bonus optimal — Inorder traversal must be strictly increasing
Because **inorder of a BST yields sorted order**, you can validate by checking each value is greater than the previous one.

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
- **Memory hook 🪟:** Like a bouncer with a guest list that shrinks at each door — going left lowers the ceiling, going right raises the floor. 🚪
- **Trigger phrase:** *"is it a valid BST / verify BST property"* → range-passing DFS, or inorder-increasing.

---

## Problem 5 — Lowest Common Ancestor of a BST (Medium, Microsoft ✅ **Classic**)

### 📋 Full Question
Given a **BST** and two nodes `p` and `q`, return their **lowest (deepest) common ancestor** — the deepest node that has both `p` and `q` as descendants (a node can be a descendant of itself).

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
- "Are `p` and `q` **guaranteed to exist** in the tree?" (Usually yes.)
- "Can `p` be an ancestor of `q`?" (Yes — handle that.)

### Approach 1 — Brute Force: treat it as a general binary tree
Ignore the BST property and search both subtrees for p and q (the general-tree LCA, Problem 8 below). Works, but **wastes** the sorted structure.
**Time: O(n)**. *Why a better approach exists:* In a BST, the values themselves tell us which direction to go — we never need to explore both sides.

### Approach 2 — Optimal: use the BST ordering to walk ONE path
**Core insight:**
- If **both** `p` and `q` are **less** than current → LCA is in the **left** subtree.
- If **both** are **greater** → go **right**.
- Otherwise they **split** (one ≤ node ≤ other) → current node **is** the LCA. 🎯

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

**Dry-run (p=2, q=8):**
- At `6`: 2 < 6 but 8 > 6 → they split → return **6** ✅.

**Dry-run (p=2, q=4):**
- At `6`: both < 6 → go left to `2`.
- At `2`: p=2 equals node, q=4 > 2 → split → return **2** ✅.

**Time: O(h)** — single path down, O(log n) balanced. **Space: O(1)** iterative.

### 🧠 Algorithm to Remember Forever
- **Pattern:** BST search — let the **values steer** you down one path.
- **Core idea:** "Walk down until p and q sit on opposite sides of you."
- **Memory hook 🛤️:** Two travelers on a sorted highway — keep driving while both are ahead of you in the same direction; the moment one is behind and one ahead, you're standing between them. 🧍
- **Trigger phrase:** *"LCA in a BST"* → compare values, go left/right, stop at the split.

---

## Problem 6 — Kth Smallest Element in a BST (Medium, Microsoft ✅)

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
- "Might there be **frequent** kth-smallest queries with modifications?" (If so, augment nodes with subtree counts — mention it to impress.)

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
*Why a better approach exists:* We don't need the whole sorted list — we can **stop early** once we've seen k elements.

### Approach 2 — Optimal: inorder traversal with early stop
**Core insight:** Inorder visits BST nodes in **sorted order**. Count as you go; the k-th node you visit is the answer. Stop immediately.

```java
private int count = 0;
private int answer = 0;

public int kthSmallest(TreeNode root, int k) {
    count = 0;
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
> Cleaner with an explicit stack if you want to truly halt all recursion early — but this version is interview-acceptable and clear.

**Dry-run (k=3):** visit 1 (count1), 2 (count2), 3 (count3 == k) → answer = **3** ✅. Stop.

**Time: O(h + k)** — descend to smallest, then k steps. **Space: O(h)**.

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Inorder traversal** (the BST → sorted superpower) + a counter.
- **Core idea:** "Inorder *is* a sorted walk — count off k steps."
- **Memory hook 🥇:** A race where runners cross the line in sorted order — just watch for the k-th finisher and stop the clock. ⏱️
- **Trigger phrase:** *"k-th smallest / k-th largest / sorted order in BST"* → inorder traversal (reverse-inorder for k-th largest).

---

## Problem 7 — Serialize and Deserialize Binary Tree (Hard, Microsoft ✅ **Top 5!**)

### 📋 Full Question
Design two functions: `serialize` turns a binary tree into a **string**, and `deserialize` turns that string back into the **exact same tree**. (It's a general binary tree, not a BST.)

**Example:**
```
    1
   / \
  2   3
     / \
    4   5
```
`serialize(root)` → `"1,2,#,#,3,4,#,#,5,#,#"` (one valid encoding) → `deserialize(...)` rebuilds the identical tree.

### 🗣️ What to say first
- "Can node values be **negative** / multi-digit?" (Use a delimiter like `,`.)
- "Is it a general binary tree (no BST property to exploit)?" (Yes.)
- "Any format I want, as long as serialize↔deserialize round-trips?" (Yes — pick whatever is cleanest.)

### Approach 1 — Brute Force idea: level-order (BFS) serialization
Works (this is how LeetCode displays trees), but the index bookkeeping for nulls during BFS rebuild is fiddly and error-prone under interview pressure.
*Why a better approach exists:* **Preorder DFS with null markers** is far simpler to both write and reason about — recursion naturally rebuilds structure in the same order it was written.

### Approach 2 — Optimal: Preorder DFS + null markers (`#`)
**Core insight:** Write nodes in **preorder** (Root → Left → Right), using a sentinel (`#`) for null. To rebuild, read tokens in the **same order**: consume one token as the root, then recursively build left, then right. The `#` markers tell us exactly where subtrees end. 🧩

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
        if (token.equals(NULL)) return null;    // hit a marker → empty subtree
        TreeNode node = new TreeNode(Integer.parseInt(token));
        node.left = buildTree(tokens);          // build left first (preorder)
        node.right = buildTree(tokens);         // then right
        return node;
    }
}
```

**Dry-run:**
- `serialize` → `"1,2,#,#,3,4,#,#,5,#,#,"`.
- `deserialize`: poll `1` → node(1); build left: poll `2` → node(2), its left poll `#`→null, right poll `#`→null; back to 1's right: poll `3` → node(3), left poll `4`→node(4) with two `#` children, right poll `5`→node(5) with two `#` children. Tree rebuilt identically ✅.

**Time: O(n)** for both. **Space: O(n)** for the string + O(h) recursion.

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Preorder DFS** to write, **preorder consume** to read, with **null markers**.
- **Core idea:** "Record the shape AND the gaps (nulls); rebuild by reading in the same order."
- **Memory hook 📼:** Like recording a video and playing it back — you must capture the *blank frames* (nulls) too, or playback desyncs. The `#` are your blank frames. ⏯️
- **Trigger phrase:** *"serialize / deserialize / encode a tree to a string / reconstruct a tree"* → preorder + `#` markers + a token queue.

> 🚨 This is one of Microsoft's most-asked questions. Practice writing it from memory until it's automatic.

---

## Problem 8 — Lowest Common Ancestor of a Binary Tree (Medium, Microsoft ✅ **Frequent**)

### 📋 Full Question
Same as Problem 5, but now it's a **general binary tree** — **no** BST ordering to exploit. Find the lowest common ancestor of nodes `p` and `q`.

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
- "Are both `p` and `q` **guaranteed present** in the tree?" (Usually yes.)
- "Can one node be the ancestor of the other?" (Yes — a node is its own descendant.)
- "No BST property here, correct?" (Correct — we must search both sides.)

### Approach 1 — Brute Force: find root-to-node paths, compare
Find the path from root to `p` and root to `q` as two lists, then walk both until they diverge — last common node is the LCA.
**Time: O(n)** but **Space: O(n)** for storing paths, and it's two traversals + extra comparison logic.
*Why a better approach exists:* We can find the LCA in a **single recursive pass** with O(h) space — cleaner and the expected answer.

### Approach 2 — Optimal: single-pass recursion
**Core insight:** Recurse on left and right.
- If a subtree **contains `p` or `q`**, it returns that node (bubbles up a "found" signal).
- If `p` is found in the **left** subtree and `q` in the **right** (or vice versa), the **current node** is the LCA.
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

**Dry-run (p=5, q=1):**
- At `3`: recurse left into subtree rooted `5` → returns `5` (matches p). Recurse right into subtree rooted `1` → returns `1` (matches q).
- `left=5` and `right=1` both non-null → return **3** ✅.

**Dry-run (p=5, q=4):**
- Subtree at `5`: recursing finds `5` immediately (root == p), returns `5` up. Right side returns null at `3`. Result bubbles up to `5`. Returns **5** ✅ (ancestor-of-self case handled by `root == p`).

**Time: O(n)** — may visit every node. **Space: O(h)** recursion.

### 🧠 Algorithm to Remember Forever
- **Pattern:** **Post-order DFS** that **bubbles up** found nodes; the split point is the LCA.
- **Core idea:** "If p and q come back from different sides, I'm the meeting point."
- **Memory hook 🤝:** Two people lost in a building send signals up the staircases — wherever the left and right signals first **both** arrive, that's where they'd reunite. 🪜
- **Trigger phrase:** *"LCA in a binary tree (not BST) / lowest common ancestor general tree"* → recurse both sides, split = answer.

---

## Problem 9 — Binary Tree Right Side View (Medium, Microsoft ✅)

### 📋 Full Question
Imagine standing on the **right** of the tree. Return the values of the nodes you can see, **top to bottom** (the rightmost node of each level).

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

### Approach 1 — Brute Force: collect full level order, take last of each
Run the standard BFS from Problem 3, then for each level grab `level.get(level.size()-1)`. Works perfectly — just builds extra lists we throw away.
*Why a better approach exists:* We only need the last node per level, so we can grab it during BFS without storing whole levels.

### Approach 2 — Optimal: BFS, capture the last node of each level
**Core insight:** It's Problem 3's BFS again — but when `i == levelSize - 1` we're at the rightmost node of that level.

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

**Dry-run:**
- Level `[1]` → last is 1 → result `[1]`.
- Level `[2,3]` → last is 3 → result `[1,3]`.
- Level `[5,4]` → last is 4 → result `[1,3,4]` ✅.

**Time: O(n)**. **Space: O(w)**.

### 🧠 Algorithm to Remember Forever
- **Pattern:** **BFS with `levelSize`** (same engine as Level Order) + "take the last one."
- **Core idea:** "Walk each floor left-to-right; remember only whoever's standing by the right window."
- **Memory hook 🪟:** Each floor of a building — you, standing outside on the right, only see the person at the rightmost window. 👀
- **Trigger phrase:** *"right side view / leftmost / rightmost of each level / what you see from the side"* → BFS + grab the boundary node per level.

---

## 📊 Complexity Cheat Sheet

| Problem | Brute Force | Optimal | Technique |
|---|---|---|---|
| Maximum Depth | O(n) time / O(w) space (BFS) | **O(n) / O(h)** | Recursion `max(L,R)+1` |
| Diameter | O(n²) / O(h) | **O(n) / O(h)** | Height-DFS + global max of `L+R` |
| Level Order Traversal | O(n) / O(h) (DFS bucket) | **O(n) / O(w)** | BFS + `levelSize` snapshot |
| Validate BST | O(n) but WRONG (local check) | **O(n) / O(h)** | Range (low,high) DFS or inorder-increasing |
| LCA of BST | O(n) (general LCA) | **O(h) / O(1)** | Walk down via value comparison |
| Kth Smallest in BST | O(n) / O(n) (full list) | **O(h+k) / O(h)** | Inorder + counter, early stop |
| Serialize/Deserialize | O(n) / O(n) (BFS, fiddly) | **O(n) / O(n)** | Preorder DFS + `#` markers + token queue |
| LCA of Binary Tree | O(n) / O(n) (path lists) | **O(n) / O(h)** | Post-order recursion, split = LCA |
| Right Side View | O(n) / O(n) (full levels) | **O(n) / O(w)** | BFS + last node per level |

> *n = nodes, h = height, w = max width. Balanced tree: h ≈ log n. Skewed tree: h ≈ n.*

---

## 🧠🧠 ALGORITHM RECOGNITION TABLE

> When you hear the trigger, your hand should already be reaching for the algorithm.

| If the question says... (Trigger) | Use Algorithm | Memory Hook | Big-O |
|---|---|---|---|
| "depth / height / how tall" | Recursion `1 + max(L, R)` | 🌳 Tallest child + me | O(n) / O(h) |
| "longest path / diameter" | Height-DFS + global max `L+R` | 🛣️ Longest road may skip the capital | O(n) / O(h) |
| "level by level / per level / each row" | **BFS + `levelSize` loop** | 🏢 Clear the building floor by floor | O(n) / O(w) |
| "rightmost / leftmost / side view" | BFS + take boundary node | 🪟 Person at the right window | O(n) / O(w) |
| "zigzag / spiral" | BFS + alternate insert direction | 🐍 Snake slithering down levels | O(n) / O(w) |
| "is it a valid BST" | Range (low, high) DFS / inorder | 🪟 Shrinking guest-list window | O(n) / O(h) |
| "k-th smallest / sorted order in BST" | **Inorder traversal** + counter | 🥇 Watch the k-th finisher | O(h+k) / O(h) |
| "LCA in a **BST**" | Walk down by value comparison | 🛤️ Drive till they split | O(h) / O(1) |
| "LCA in a **binary tree**" | Post-order recursion, split = ans | 🤝 Signals meet at the staircase | O(n) / O(h) |
| "serialize / encode / reconstruct tree" | **Preorder DFS + `#` markers** | 📼 Record the blank frames too | O(n) / O(n) |
| "path sum / root-to-leaf path" | DFS + backtracking path list | 🥾 Hike down, drop breadcrumbs, backtrack | O(n) / O(h) |
| "same / symmetric / mirror" | Compare two nodes in lockstep | 🪞 Mirror walk left↔right | O(n) / O(h) |
| "invert / flip the tree" | Swap children recursively | 🔄 Mirror every fork | O(n) / O(h) |
| "build tree from preorder + inorder" | Recursion: root from pre, split inorder | 🧱 Pre gives the root, in gives the halves | O(n) / O(n) |

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

### 5. Returning multiple things from recursion
Java has no tuples. Options:
- Use an **instance/field variable** (e.g., `private int maxDiameter`) updated as a side effect.
- Use a 1-element array as a mutable holder: `int[] result = {0};` then mutate `result[0]`.
- Return a small helper class / `int[]` of values.

### 6. Watch out for these Java gotchas
- Use `Integer` (nullable) instead of `int` when you need "no bound" sentinels (Validate BST).
- For huge values use `Long` to avoid `Integer.MIN/MAX_VALUE` overflow in comparisons.
- `Math.max(a, b)` / `Math.min(a, b)` — your constant companions.
- Reset any instance/field variables at the **start** of the public method (LeetCode reuses the object across test cases!).
- `Arrays.asList(data.split(","))` to turn a serialized string into a token list.

---

## ✅ Final Interview Checklist

Before you say "I'm done," confirm:

- [ ] **I restated the problem** and gave a concrete example before coding.
- [ ] **I asked clarifying questions** (empty tree? BST or general? duplicates? negatives?).
- [ ] **I mentioned the brute force** before jumping to optimal (shows range).
- [ ] **I picked the right engine**: recursion for depth/path/structure, BFS for level-based, inorder for BST-sorted.
- [ ] **I handled the `null` / empty-tree base case** first.
- [ ] **I dry-ran my code** on the example, out loud, line by line.
- [ ] **I stated time AND space complexity** in terms of n, h, w.
- [ ] **I named edge cases**: empty tree, single node, skewed (linked-list) tree, duplicate values.
- [ ] **I reset instance variables** if I used any (LeetCode reuses the object).
- [ ] **For the famous ones** (Serialize/Deserialize, Level Order, Validate BST), I can write them **from memory** with zero hesitation.

> 🎯 **Master these three for Microsoft above all else:** Serialize/Deserialize (Top 5), Level Order Traversal (Very Frequent), and Validate BST (Frequent). If you can write all three cold, you're interview-ready for the tree round. Good luck! 🍀
