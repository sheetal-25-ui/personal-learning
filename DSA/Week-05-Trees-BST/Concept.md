# Week 5: Trees & Binary Search Trees (BST)

---

## Part 1: TREES

---

### 1. What is a Tree?

**Plain English:**
A Tree is a data structure where data is organized in a HIERARCHY — like a family tree
or a company org chart. There is ONE starting point (called the root), and from there,
data branches out downward.

**Real-life Analogy — Family Tree (Vanshavali):**
Think of your family tree. Your Dada-Dadi (grandparents) are at the TOP (root).
Their children (your parents and uncles/aunts) are one level below. YOU and your
cousins are one more level below. Each person is connected to their parent.
Nobody is connected in a circle — your child can never be your grandparent!

**Another Analogy — Company Org Chart:**
```
             CEO
           /     \
         CTO      CFO
        /   \       \
    Dev Lead  QA Lead  Finance Lead
    /    \
  Dev1   Dev2
```
- CEO is at the top (root)
- CTO and CFO report to CEO (children of root)
- Dev Lead and QA Lead report to CTO
- Dev1 and Dev2 report to Dev Lead
- Finance Lead reports to CFO

**Technical Definition:**
A tree is a hierarchical data structure made up of NODES connected by EDGES.
- It has exactly ONE root node (no parent)
- Every other node has exactly ONE parent
- A node can have ZERO or more children
- There are NO CYCLES (you can never go in a circle)

---

### 2. Why Do Trees Exist?

**What problem do they solve?**

Arrays and LinkedLists are LINEAR — data is arranged in a single line.
But the real world is often HIERARCHICAL:
- File systems (folders inside folders)
- HTML/XML documents (tags inside tags)
- Organization structures
- Decision making (if this, then that, else other thing)

Trees let us represent this hierarchy naturally.

**Performance advantage:**
A balanced tree gives us O(log n) search, insert, and delete — MUCH faster than
O(n) for linked lists and O(n) for unsorted arrays. It combines the best of both
arrays (fast search) and linked lists (fast insert/delete).

---

### 3. Tree Terminology

Let us understand every term with this example tree:

```
              1          ← Level 0 (Root)
            /   \
           2     3       ← Level 1
          / \     \
         4   5     6     ← Level 2
        /
       7                 ← Level 3
```

| Term | Meaning | Example |
|------|---------|---------|
| **Root** | The topmost node (no parent) | Node 1 |
| **Parent** | The node directly above | 2 is parent of 4 and 5 |
| **Child** | The node directly below | 4 and 5 are children of 2 |
| **Sibling** | Nodes with the same parent | 4 and 5 are siblings |
| **Leaf** | A node with NO children | 7, 5, 6 are leaves |
| **Internal Node** | A node with at least one child | 1, 2, 3, 4 are internal |
| **Edge** | The connection between two nodes | Line from 1 to 2 is an edge |
| **Path** | Sequence of nodes connected by edges | 1 → 2 → 4 → 7 |
| **Height of tree** | Edges from root to deepest leaf | 3 (path: 1→2→4→7) |
| **Depth of node** | Edges from root to that node | Depth of node 5 = 2 |
| **Level** | Same as depth | Node 3 is at level 1 |
| **Subtree** | Any node + all its descendants | Subtree at 2: {2, 4, 5, 7} |
| **Degree** | Number of children a node has | Degree of 2 = 2, Degree of 3 = 1 |

**Height vs Depth:**
```
              1          depth=0, height=3
            /   \
           2     3       depth=1, height of 2=2, height of 3=1
          / \     \
         4   5     6     depth=2, height of 4=1, height of 5=0
        /
       7                 depth=3, height=0
```
- **Depth** = count DOWNWARD from root TO the node
- **Height** = count UPWARD from the node to the DEEPEST leaf below it
- Height of the tree = height of the root = depth of the deepest leaf

---

### 4. Binary Tree

**Definition:** A tree where each node has AT MOST 2 children — called **left child** and **right child**.

This is the most common type of tree in interviews. When someone says "tree" in DSA,
they usually mean binary tree.

**TreeNode in Java:**
```java
class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;

    TreeNode(int val) {
        this.val = val;
        this.left = null;
        this.right = null;
    }
}
```

#### Types of Binary Trees:

**1. Full Binary Tree** — every node has EXACTLY 0 or 2 children (never 1)
```
        1
       / \
      2   3          ✓ Full (every node: 0 or 2 children)
     / \
    4   5
```

**2. Complete Binary Tree** — all levels filled, last level filled from LEFT to RIGHT
```
        1
       / \
      2   3          ✓ Complete
     / \  /
    4  5  6
```
```
        1
       / \
      2   3          ✗ NOT Complete (last level not filled left-to-right)
     / \    \
    4  5     6
```

**3. Perfect Binary Tree** — ALL levels completely filled, every leaf at same depth
```
        1
       / \
      2   3          ✓ Perfect
     / \ / \
    4  5 6  7
```
- A perfect binary tree with height h has (2^(h+1) - 1) nodes
- Height 0: 1 node, Height 1: 3 nodes, Height 2: 7 nodes, Height 3: 15 nodes

**4. Balanced Binary Tree** — for EVERY node, height difference of left and right subtrees is at most 1
```
        1
       / \
      2   3          ✓ Balanced (height diff ≤ 1 everywhere)
     / \
    4   5
```
```
        1
       /
      2              ✗ NOT Balanced (root: left height=2, right height=0, diff=2)
     /
    3
```

---

### 5. Tree Traversals (MOST IMPORTANT TOPIC)

Traversal = visiting every node in the tree in a specific ORDER.

There are 4 main traversals. For EVERY interview, you MUST know all 4.

**Example tree for all traversals:**
```
          1
         / \
        2   3
       / \   \
      4   5   6
```

---

#### 5a. Inorder Traversal (Left → Root → Right)

**Process:** Visit left subtree FIRST, then the current node, then the right subtree.

**Step-by-step trace:**
```
Start at 1 → go LEFT to 2
  At 2 → go LEFT to 4
    At 4 → go LEFT (null, stop)
    VISIT 4 ✓
    At 4 → go RIGHT (null, stop)
  Back at 2 → VISIT 2 ✓
  At 2 → go RIGHT to 5
    At 5 → go LEFT (null, stop)
    VISIT 5 ✓
    At 5 → go RIGHT (null, stop)
Back at 1 → VISIT 1 ✓
At 1 → go RIGHT to 3
  At 3 → go LEFT (null, stop)
  VISIT 3 ✓
  At 3 → go RIGHT to 6
    At 6 → go LEFT (null, stop)
    VISIT 6 ✓
    At 6 → go RIGHT (null, stop)

Result: 4, 2, 5, 1, 3, 6
```

**KEY INSIGHT:** For a BST, inorder traversal gives nodes in SORTED order!

**Java — Recursive:**
```java
// Inorder: Left → Root → Right
void inorder(TreeNode root) {
    if (root == null) return;   // base case: empty tree/subtree

    inorder(root.left);         // step 1: visit entire left subtree
    System.out.print(root.val + " ");  // step 2: visit current node
    inorder(root.right);        // step 3: visit entire right subtree
}
```

**Java — Iterative (uses explicit stack):**
```java
// Iterative inorder using a stack
// The stack simulates the recursion call stack manually
void inorderIterative(TreeNode root) {
    Stack<TreeNode> stack = new Stack<>();
    TreeNode current = root;

    while (current != null || !stack.isEmpty()) {
        // Go as far LEFT as possible, pushing nodes onto stack
        while (current != null) {
            stack.push(current);
            current = current.left;
        }
        // Pop from stack = visit the node
        current = stack.pop();
        System.out.print(current.val + " ");
        // Now go RIGHT
        current = current.right;
    }
}
```

---

#### 5b. Preorder Traversal (Root → Left → Right)

**Process:** Visit current node FIRST, then left subtree, then right subtree.

**Step-by-step trace:**
```
Start at 1 → VISIT 1 ✓
  Go LEFT to 2 → VISIT 2 ✓
    Go LEFT to 4 → VISIT 4 ✓
      LEFT null, RIGHT null
    Go RIGHT to 5 → VISIT 5 ✓
      LEFT null, RIGHT null
  Go RIGHT to 3 → VISIT 3 ✓
    LEFT null
    Go RIGHT to 6 → VISIT 6 ✓
      LEFT null, RIGHT null

Result: 1, 2, 4, 5, 3, 6
```

**Use case:** Useful for COPYING or SERIALIZING a tree (saving it to a file).
If you rebuild the tree by inserting nodes in preorder sequence, you get the same tree.

**Java:**
```java
// Preorder: Root → Left → Right
void preorder(TreeNode root) {
    if (root == null) return;

    System.out.print(root.val + " ");  // step 1: visit current node FIRST
    preorder(root.left);               // step 2: visit left subtree
    preorder(root.right);              // step 3: visit right subtree
}
```

---

#### 5c. Postorder Traversal (Left → Right → Root)

**Process:** Visit left subtree, then right subtree, then current node LAST.

**Step-by-step trace:**
```
Start at 1 → go LEFT to 2
  At 2 → go LEFT to 4
    At 4 → LEFT null, RIGHT null → VISIT 4 ✓
  At 2 → go RIGHT to 5
    At 5 → LEFT null, RIGHT null → VISIT 5 ✓
  VISIT 2 ✓
At 1 → go RIGHT to 3
  At 3 → LEFT null
  At 3 → go RIGHT to 6
    At 6 → LEFT null, RIGHT null → VISIT 6 ✓
  VISIT 3 ✓
VISIT 1 ✓

Result: 4, 5, 2, 6, 3, 1
```

**Use case:** Useful for DELETING a tree (delete children before parent).
Also used for evaluating expression trees.

**Java:**
```java
// Postorder: Left → Right → Root
void postorder(TreeNode root) {
    if (root == null) return;

    postorder(root.left);              // step 1: visit left subtree
    postorder(root.right);             // step 2: visit right subtree
    System.out.print(root.val + " ");  // step 3: visit current node LAST
}
```

---

#### 5d. Level Order Traversal (BFS — Breadth First Search)

**Process:** Visit nodes LEVEL by LEVEL, left to right. Uses a QUEUE.

**Real-life analogy:** Imagine distributing sweets at a wedding. You serve the
head table first (level 0), then the next row of tables (level 1), and so on.
You go LEFT to RIGHT within each row.

**Step-by-step trace:**
```
Queue: [1]
  Dequeue 1 → VISIT 1 ✓ → Enqueue children: [2, 3]
Queue: [2, 3]
  Dequeue 2 → VISIT 2 ✓ → Enqueue children: [3, 4, 5]
Queue: [3, 4, 5]
  Dequeue 3 → VISIT 3 ✓ → Enqueue children: [4, 5, 6]
Queue: [4, 5, 6]
  Dequeue 4 → VISIT 4 ✓ → No children: [5, 6]
Queue: [5, 6]
  Dequeue 5 → VISIT 5 ✓ → No children: [6]
Queue: [6]
  Dequeue 6 → VISIT 6 ✓ → No children: []
Queue: [] → DONE

Result: 1, 2, 3, 4, 5, 6
```

**Java — Level Order (returns list of lists, one per level):**
```java
// Level order traversal using a Queue (BFS)
// Returns a list of lists — each inner list is one level
List<List<Integer>> levelOrder(TreeNode root) {
    List<List<Integer>> result = new ArrayList<>();
    if (root == null) return result;

    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);  // start with root

    while (!queue.isEmpty()) {
        int levelSize = queue.size();  // how many nodes at THIS level
        List<Integer> currentLevel = new ArrayList<>();

        for (int i = 0; i < levelSize; i++) {
            TreeNode node = queue.poll();          // dequeue front
            currentLevel.add(node.val);            // visit it

            if (node.left != null)  queue.offer(node.left);   // enqueue left child
            if (node.right != null) queue.offer(node.right);  // enqueue right child
        }
        result.add(currentLevel);
    }
    return result;
}
// For our example tree: [[1], [2, 3], [4, 5, 6]]
```

---

#### Traversal Summary

| Traversal | Order | Use Case | Data Structure |
|-----------|-------|----------|----------------|
| Inorder | Left → Root → Right | Sorted order in BST | Stack/Recursion |
| Preorder | Root → Left → Right | Copy/serialize tree | Stack/Recursion |
| Postorder | Left → Right → Root | Delete tree, evaluate expressions | Stack/Recursion |
| Level Order | Level by level, L→R | Level-based questions, shortest path | Queue |

**Memory trick:** The name tells you where ROOT goes:
- **In**order = root in the MIDDLE (Left, **Root**, Right)
- **Pre**order = root FIRST (**Root**, Left, Right)
- **Post**order = root LAST (Left, Right, **Root**)

---

## Part 2: BINARY SEARCH TREE (BST)

---

### 1. What is a BST?

**Definition:** A Binary Search Tree is a binary tree with ONE extra rule:
> For EVERY node: all values in the LEFT subtree < node value < all values in RIGHT subtree

**Real-life Analogy — Organized Bookshelf:**
Imagine a library bookshelf organized by book number.
- Shelf has a middle divider with book #50
- ALL books numbered < 50 go on the LEFT side
- ALL books numbered > 50 go on the RIGHT side
- The left side has its OWN divider (say #25): books < 25 go left, > 25 go right
- This keeps going until you reach individual books

To find book #37: start at 50 (go left) → 25 (go right) → 37 (found!).
You eliminated HALF the books at each step. That is O(log n)!

**Visual example:**
```
          8           Every node follows: left < parent < right
         / \
        3   10        3 < 8 < 10 ✓
       / \    \
      1   6    14     1 < 3, 6 > 3, 14 > 10 ✓
         / \   /
        4   7 13      4 < 6, 7 > 6, 13 < 14 ✓
```

**Inorder traversal of this BST:** 1, 3, 4, 6, 7, 8, 10, 13, 14 — SORTED!

---

### 2. Why Does BST Exist?

| Data Structure | Search | Insert | Delete |
|----------------|--------|--------|--------|
| Sorted Array | O(log n) binary search | O(n) shift elements | O(n) shift elements |
| Linked List | O(n) | O(1) if at head | O(1) if pointer known |
| **BST (balanced)** | **O(log n)** | **O(log n)** | **O(log n)** |

BST gives us fast search (like sorted array) AND fast insert/delete (like linked list).
Best of both worlds!

---

### 3. BST Operations — Visual Walkthrough

#### 3a. SEARCH

**Goal:** Find if value exists in the BST.

**Algorithm:** Compare target with current node. Go left if smaller, right if larger.

**Example: Search for 6 in this BST:**
```
          8
         / \
        3   10
       / \    \
      1   6    14

Step 1: Current = 8, Target = 6. 6 < 8 → go LEFT
Step 2: Current = 3, Target = 6. 6 > 3 → go RIGHT
Step 3: Current = 6, Target = 6. FOUND! ✓
```

**Example: Search for 5 (not in tree):**
```
Step 1: Current = 8, 5 < 8 → go LEFT
Step 2: Current = 3, 5 > 3 → go RIGHT
Step 3: Current = 6, 5 < 6 → go LEFT
Step 4: Current = null → NOT FOUND ✗
```

---

#### 3b. INSERT

**Goal:** Add a new value. It will ALWAYS be added as a LEAF node.

**Algorithm:** Search for the value. When you reach null, that is where it goes.

**Example: Insert 5 into the BST:**
```
          8                          8
         / \                        / \
        3   10       Insert 5      3   10
       / \    \      -------->    / \    \
      1   6    14                1   6    14
                                    /
                                   5   ← new leaf!

Step 1: 5 < 8 → go LEFT
Step 2: 5 > 3 → go RIGHT
Step 3: 5 < 6 → go LEFT
Step 4: null → INSERT 5 here!
```

---

#### 3c. DELETE

This is the TRICKIEST operation. There are 3 cases:

**Case 1: Node is a LEAF (no children)** — just remove it!
```
Delete 1:
          8                    8
         / \                  / \
        3   10    ------>    3   10
       / \    \               \    \
      1   6    14              6    14
```

**Case 2: Node has ONE child** — replace node with its child
```
Delete 10 (has one child: 14):
          8                    8
         / \                  / \
        3   10    ------>    3   14
       / \    \             / \
      1   6    14          1   6
```

**Case 3: Node has TWO children** — find INORDER SUCCESSOR (smallest node in right subtree), copy its value, then delete the successor
```
Delete 3 (has two children: 1 and 6):

Step 1: Find inorder successor of 3 = smallest in right subtree of 3
        Right subtree of 3 starts at 6. Go left: no left child.
        Inorder successor = 6 (but if 6 had left child 4, it would be 4)

Step 2: Copy successor's value to node being deleted (replace 3 with 4 or 6)

Step 3: Delete the successor from its original position

          8                    8
         / \                  / \
        3   10    ------>    4   10
       / \    \             / \    \
      1   6    14          1   6    14
         /
        4                  (4 moved up, original 4 position deleted)
```

Wait, let me redo that more carefully with the original tree:
```
          8
         / \
        3   10
       / \    \
      1   6    14
         / \   /
        4   7 13

Delete 3:
  Step 1: 3 has two children (1 and 6)
  Step 2: Inorder successor = smallest in right subtree of 3
          Right subtree root = 6, go left → 4, go left → null
          Inorder successor = 4
  Step 3: Copy 4's value to 3's position
  Step 4: Delete original 4 (it is a leaf, so Case 1)

Result:
          8
         / \
        4   10         ← 3 replaced by 4
       / \    \
      1   6    14      ← original 4 is gone
           \   /
            7 13
```

---

### 4. BST Time Complexity

| Operation | Average (Balanced) | Worst (Skewed) |
|-----------|--------------------|----------------|
| Search | O(log n) | O(n) |
| Insert | O(log n) | O(n) |
| Delete | O(log n) | O(n) |
| Inorder | O(n) | O(n) |

**Why can worst case be O(n)?**

If you insert elements in SORTED order (1, 2, 3, 4, 5), the BST becomes a straight line — basically a linked list!

```
Insert 1, 2, 3, 4, 5:

1                   Looks like a linked list!
 \                  Height = 4 (should be 2 if balanced)
  2                 Search for 5 = visit ALL 5 nodes = O(n)
   \
    3
     \
      4
       \
        5

vs. balanced insertion order (3, 1, 4, 2, 5):

      3             Height = 2
     / \            Search for 5 = visit 3 nodes = O(log n)
    1   4
     \   \
      2   5
```

**Solution:** Self-balancing trees (AVL, Red-Black) automatically keep the tree balanced.
Java's TreeMap and TreeSet use Red-Black trees internally.
For interviews: know they EXIST and WHY, but you usually don't need to implement them.

---

### 5. Java BST Implementation

```java
class TreeNode {
    int val;
    TreeNode left, right;

    TreeNode(int val) {
        this.val = val;
        this.left = null;
        this.right = null;
    }
}

class BST {

    TreeNode root;

    // =========== SEARCH ===========
    // Returns true if the value exists in the BST
    // At each step, we eliminate half the tree — O(log n) average
    boolean search(TreeNode node, int target) {
        if (node == null) return false;          // base case: not found
        if (target == node.val) return true;     // found it!
        if (target < node.val) {
            return search(node.left, target);    // target is smaller, go left
        } else {
            return search(node.right, target);   // target is larger, go right
        }
    }

    // =========== INSERT ===========
    // Inserts a new value into the BST and returns the (possibly new) root
    // New values are always added as leaf nodes
    TreeNode insert(TreeNode node, int val) {
        if (node == null) {
            return new TreeNode(val);            // found the spot, create new node
        }
        if (val < node.val) {
            node.left = insert(node.left, val);  // go left, attach returned subtree
        } else if (val > node.val) {
            node.right = insert(node.right, val); // go right, attach returned subtree
        }
        // if val == node.val, do nothing (no duplicates in standard BST)
        return node;
    }

    // =========== DELETE ===========
    // Deletes a value from the BST and returns the (possibly new) root
    TreeNode delete(TreeNode node, int val) {
        if (node == null) return null;           // value not found

        if (val < node.val) {
            node.left = delete(node.left, val);  // target is in left subtree
        } else if (val > node.val) {
            node.right = delete(node.right, val); // target is in right subtree
        } else {
            // FOUND the node to delete! Handle 3 cases:

            // Case 1 & 2: Node has 0 or 1 child
            if (node.left == null) return node.right;  // replace with right child (or null)
            if (node.right == null) return node.left;   // replace with left child

            // Case 3: Node has 2 children
            // Find inorder successor (smallest in right subtree)
            TreeNode successor = findMin(node.right);
            node.val = successor.val;                   // copy successor's value
            node.right = delete(node.right, successor.val); // delete successor
        }
        return node;
    }

    // Helper: find the smallest node in a subtree (go left until you can't)
    TreeNode findMin(TreeNode node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    // =========== INORDER TRAVERSAL ===========
    // For BST, this prints values in SORTED order
    void inorder(TreeNode node) {
        if (node == null) return;
        inorder(node.left);
        System.out.print(node.val + " ");
        inorder(node.right);
    }

    // =========== USAGE ===========
    public static void main(String[] args) {
        BST bst = new BST();

        // Build the tree
        bst.root = bst.insert(bst.root, 8);
        bst.insert(bst.root, 3);
        bst.insert(bst.root, 10);
        bst.insert(bst.root, 1);
        bst.insert(bst.root, 6);
        bst.insert(bst.root, 14);
        bst.insert(bst.root, 4);
        bst.insert(bst.root, 7);

        System.out.print("Inorder: ");
        bst.inorder(bst.root);  // Output: 1 3 4 6 7 8 10 14 (sorted!)

        System.out.println("\nSearch 6: " + bst.search(bst.root, 6));  // true
        System.out.println("Search 5: " + bst.search(bst.root, 5));   // false

        bst.root = bst.delete(bst.root, 3);
        System.out.print("After deleting 3: ");
        bst.inorder(bst.root);  // Output: 1 4 6 7 8 10 14
    }
}
```

---

### 6. Key BST Patterns for Interviews

#### Pattern 1: Validate BST
**Problem:** Given a binary tree, check if it is a valid BST.

**Trick:** Pass a MIN and MAX range down. Each node must be within its valid range.
```java
// The root can be any value: range is (-infinity, +infinity)
// Left child of 8 must be in range (-infinity, 8)
// Right child of 8 must be in range (8, +infinity)
// Left child of 3 (which is left of 8) must be in range (-infinity, 3)
boolean isValidBST(TreeNode node, long min, long max) {
    if (node == null) return true;  // empty tree is valid
    if (node.val <= min || node.val >= max) return false;  // out of range!

    // Left subtree must have values in (min, node.val)
    // Right subtree must have values in (node.val, max)
    return isValidBST(node.left, min, node.val) &&
           isValidBST(node.right, node.val, max);
}

// Call: isValidBST(root, Long.MIN_VALUE, Long.MAX_VALUE);
```

#### Pattern 2: Lowest Common Ancestor (LCA) in BST
**Problem:** Find the lowest node that is an ancestor of both p and q.

**Trick:** In a BST, leverage the ordering:
- If both p and q are smaller than current → LCA is in left subtree
- If both are larger → LCA is in right subtree
- If one is on each side (or one equals current) → current IS the LCA
```java
TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
    if (p.val < root.val && q.val < root.val) {
        return lowestCommonAncestor(root.left, p, q);   // both in left subtree
    }
    if (p.val > root.val && q.val > root.val) {
        return lowestCommonAncestor(root.right, p, q);  // both in right subtree
    }
    return root;  // split point — this is the LCA
}
```

#### Pattern 3: Kth Smallest Element
**Problem:** Find the Kth smallest element in a BST.

**Trick:** Inorder traversal gives sorted order. Just count to K!
```java
int count = 0;
int result = 0;

void kthSmallest(TreeNode node, int k) {
    if (node == null) return;

    kthSmallest(node.left, k);     // go left first (smaller values)

    count++;                        // this is the count-th smallest
    if (count == k) {
        result = node.val;          // found it!
        return;
    }

    kthSmallest(node.right, k);    // then go right (larger values)
}
```

---

### 7. When to Use Trees — Pattern Recognition Triggers

| If you see this in the problem... | Think this... |
|-----------------------------------|---------------|
| "Hierarchical data" | Tree |
| "Sorted data with fast insert/delete/search" | BST |
| "Level by level" or "minimum depth" | BFS / Level Order traversal |
| "Path from root to leaf" or "all paths" | DFS (recursion) |
| "Sorted order" from a tree | Inorder traversal |
| "Serialize/deserialize a tree" | Preorder traversal |
| "Height" or "depth" or "balanced" | Recursion returning heights |
| "Lowest common ancestor" | LCA algorithm |
| "Validate BST" | Min/max range passing |
| "Kth smallest/largest" | Inorder traversal + counting |

---

### 8. Common Mistakes

1. **Forgetting the base case:** Always check `if (node == null)` at the start of recursive functions
2. **BST validation:** Don't just check node.left < node < node.right — you must check ALL descendants, not just immediate children
   ```
        5
       / \
      1   6
         / \
        3   7    ← 3 < 5 but is in RIGHT subtree! Invalid BST!
   ```
   Node 3 is less than its immediate parent 6, but it is in the RIGHT subtree of 5, violating BST property.
3. **BST delete with two children:** Don't forget to actually DELETE the inorder successor after copying its value
4. **Confusing height and depth:** Height goes DOWN (root to leaf), depth goes DOWN from root to current node. Height of tree = depth of deepest leaf.
5. **Not handling the skewed case:** A BST can degenerate to a linked list with O(n) operations if you insert sorted data

---

### 9. Quick Summary

```
TREE:
  - Hierarchical structure: root → children → grandchildren
  - Binary tree: max 2 children per node
  - 4 traversals: Inorder (L,Root,R), Preorder (Root,L,R),
    Postorder (L,R,Root), Level Order (BFS with Queue)

BST:
  - Binary tree + rule: left < root < right (for ALL nodes)
  - Balanced: O(log n) search/insert/delete
  - Skewed: O(n) — essentially a linked list
  - Inorder traversal = sorted order
  - Key patterns: validate BST (min/max), LCA (leverage ordering),
    Kth smallest (inorder + count)

REMEMBER:
  - Recursion is your best friend for tree problems
  - Most tree problems = choose the right traversal
  - Draw the tree on paper. Trace the algorithm step by step.
  - 90% of tree interview questions use these building blocks
```

---

*Next: Week 6 — Graphs, BFS, DFS. Trees are actually a special case of graphs!*
