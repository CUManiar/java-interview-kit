# 6. Binary Tree / Binary Search Tree

Stage 4/7 — Trees, Heaps & Tries

Code: [`TreeNotes.java`](./TreeNotes.java) — run `java 04-trees-heaps-tries/01-tree/TreeNotes.java`

## Prerequisites
- [Linked List](../../03-linear-structures/03-linked-list/) — a tree node is a list node with two `next` pointers instead of one.
- [Queue](../../03-linear-structures/02-queue/) — level-order traversal is that file's BFS template.

## Mental model
**Vocabulary**: binary tree (≤2 children, no ordering), BST (left subtree < node < right subtree — *all* of it, not just the immediate children), full (every node has 0 or 2 children), complete (all levels filled except possibly the last, left→right — this is what a **heap** is), perfect (full + all leaves same depth), balanced (`|height(left) - height(right)| <= 1` everywhere), height (edges down to a leaf), depth (edges from root down).

Sample tree used throughout the file:
```
            8
          /   \
         3     10
        / \      \
       1   6      14
          / \     /
         4   7   13
```

**Traversals — the whole game**:

| Order | Sequence | Use |
|---|---|---|
| Preorder (node, left, right) | 8 3 1 6 4 7 10 14 13 | copy/serialize a tree |
| Inorder (left, node, right) | 1 3 4 6 7 8 10 13 14 | **sorted**, for a BST! |
| Postorder (left, right, node) | 1 4 7 6 3 13 14 10 8 | delete, bottom-up sums |
| Level order (BFS) | 8 \| 3 10 \| 1 6 14 \| 4 7 13 | shortest structural distance |

Memory hook: the prefix says *where the node is visited* — pre = first, in = middle, post = last. "Inorder of a BST is sorted" is the single most reused fact in tree interviews.

**Recursion template — every tree problem fits one of these two shapes**:
```
TOP-DOWN (pass state down):        POST-ORDER / BOTTOM-UP (return info up):
  void go(node, state) {              R go(node) {
    if (node == null) return;           if (node==null) return BASE;
    use(state);                         R l = go(node.left);
    go(node.left,  state');             R r = go(node.right);
    go(node.right, state');             return combine(l, r, node);
  }                                   }
```
When in doubt, reach for bottom-up. It solves diameter, balanced-check, LCA, and max-path-sum.

## Complexity
BST search/insert/delete: O(h); h = log n balanced, n if degenerate (sorted input inserted in order). Traversals: O(n) time, O(h) space for the recursion stack. `TreeMap`/`TreeSet` are red-black trees — self-balancing, h = O(log n) guaranteed.

## What's in the code
Generic BST implementation, recursive and iterative traversals (iterative tests your stack thinking), core problems (validate BST, diameter, balanced check, LCA, max path sum, serialize/deserialize, BST delete), and why you should use `TreeMap`/`TreeSet` instead of hand-rolling a balanced tree.

## Connects to
- [Heap](../02-heap/) — a heap *is* a complete binary tree (the definition above), just array-stored instead of node-stored, trading ordering for O(1) min/max.
- [Trie](../03-trie/) — same recursive node-with-children shape; a trie's DFS is a tree DFS where the accumulated path is the payload.
- [Backtracking](../../07-advanced-patterns/01-backtracking/) — DFS-over-a-tree here is the same shape as DFS-over-a-decision-tree there; only "children" changes meaning.
- [Dynamic Programming](../../07-advanced-patterns/02-dynamic-programming/) — the bottom-up/return-info-up template above is tree-shaped memoization.
- [Graph Basics](../../05-graphs/01-graph-basics/) — a tree is a connected, acyclic graph; the traversal code barely changes, you just add a `visited` set because graphs can cycle.

## Self-test
- Why is inorder of a BST sorted?
- Give a tree where checking node-vs-immediate-children wrongly says "valid BST."
- Why does the balanced check need a -1 sentinel to stay O(n)?
- Diameter: why does the node return height but update the answer with `l+r`?
- Max path sum: why clamp child gains at 0?
- Delete from a BST with 2 children: which node replaces it, and why?
- Iterative postorder: why does the reversed (node, right, left) trick work?
