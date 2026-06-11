# Week 5: Trees & BST

## Microsoft asks Tree questions in EVERY interview round!

## Concept
- Tree = nodes connected by edges, no cycles
- Binary Tree = each node has at most 2 children (left, right)
- BST = left child < parent < right child

## Key Traversals
```python
# Inorder (Left → Root → Right) — gives sorted order in BST!
def inorder(node):
    if not node: return
    inorder(node.left)
    print(node.val)
    inorder(node.right)

# Preorder (Root → Left → Right) — useful for copying trees
# Postorder (Left → Right → Root) — useful for deleting trees
# Level Order (BFS) — level by level, uses queue
```

---

## Day 1-2: Binary Tree Basics

| # | Problem | LeetCode Link | Difficulty | Microsoft? |
|---|---------|--------------|------------|------------|
| 1 | Maximum Depth of Binary Tree | https://leetcode.com/problems/maximum-depth-of-binary-tree/ | Easy | Yes |
| 2 | Invert Binary Tree | https://leetcode.com/problems/invert-binary-tree/ | Easy | Yes |
| 3 | Same Tree | https://leetcode.com/problems/same-tree/ | Easy | Yes |
| 4 | Symmetric Tree | https://leetcode.com/problems/symmetric-tree/ | Easy | Yes |
| 5 | Binary Tree Level Order Traversal | https://leetcode.com/problems/binary-tree-level-order-traversal/ | Medium | Yes (Very Frequent) |
| 6 | Diameter of Binary Tree | https://leetcode.com/problems/diameter-of-binary-tree/ | Easy | Yes |

---

## Day 3-4: BST Problems

| # | Problem | LeetCode Link | Difficulty | Microsoft? |
|---|---------|--------------|------------|------------|
| 7 | Validate BST | https://leetcode.com/problems/validate-binary-search-tree/ | Medium | Yes (Frequent!) |
| 8 | Lowest Common Ancestor of BST | https://leetcode.com/problems/lowest-common-ancestor-of-a-binary-search-tree/ | Medium | Yes (Classic) |
| 9 | Kth Smallest Element in BST | https://leetcode.com/problems/kth-smallest-element-in-a-bst/ | Medium | Yes |
| 10 | Convert Sorted Array to BST | https://leetcode.com/problems/convert-sorted-array-to-binary-search-tree/ | Easy | Yes |
| 11 | Serialize and Deserialize Binary Tree | https://leetcode.com/problems/serialize-and-deserialize-binary-tree/ | Hard | Yes (Top 5 Microsoft!) |
| 12 | Binary Tree Right Side View | https://leetcode.com/problems/binary-tree-right-side-view/ | Medium | Yes |

---

## Day 5-6: Advanced Tree

| # | Problem | LeetCode Link | Difficulty | Microsoft? |
|---|---------|--------------|------------|------------|
| 13 | Lowest Common Ancestor (Binary Tree) | https://leetcode.com/problems/lowest-common-ancestor-of-a-binary-tree/ | Medium | Yes (Frequent) |
| 14 | Binary Tree Zigzag Level Order | https://leetcode.com/problems/binary-tree-zigzag-level-order-traversal/ | Medium | Yes |
| 15 | Construct BT from Preorder and Inorder | https://leetcode.com/problems/construct-binary-tree-from-preorder-and-inorder-traversal/ | Medium | Yes |
| 16 | Path Sum II | https://leetcode.com/problems/path-sum-ii/ | Medium | Yes |
| 17 | Flatten Binary Tree to Linked List | https://leetcode.com/problems/flatten-binary-tree-to-linked-list/ | Medium | Yes |
| 18 | Count Good Nodes in Binary Tree | https://leetcode.com/problems/count-good-nodes-in-binary-tree/ | Medium | Yes |

---

## Day 7: Revision

- Serialize/Deserialize (hardest, most asked)
- Level Order Traversal
- Validate BST

---

## Tree Pattern Cheat Sheet

```
"Find depth/height"         → Recursion: max(left, right) + 1
"Level by level"            → BFS with queue
"Validate BST"              → Pass min/max range down
"Lowest Common Ancestor"    → Recursion: check left and right subtrees
"Serialize/Deserialize"     → Preorder traversal + null markers
"Path problems"             → DFS with path tracking
```

---

## Self-Check

- [ ] Can write all 4 traversals from memory
- [ ] Can solve Level Order Traversal in 10 minutes
- [ ] Understand LCA approach for both BST and Binary Tree
- [ ] Can serialize and deserialize a tree
- [ ] Solved at least 15 of 18 questions
