# 7. Heap / Priority Queue

Stage 4/7 — Trees, Heaps & Tries

Code: [`HeapNotes.java`](./HeapNotes.java) — run `java 04-trees-heaps-tries/02-heap/HeapNotes.java`

## Prerequisites
- [Tree](../01-tree/) — a heap is a complete binary tree; you need that definition before the array trick below makes sense.

## Mental model
A **complete** binary tree obeying the heap property — min-heap: every parent ≤ its children; max-heap: every parent ≥ its children. There is no left-right ordering; siblings are unrelated. A heap is only "partially sorted" — that's exactly why it's cheaper than a BST.

**Array representation** — because it's complete, no pointers needed:
```
           1
         /   \                 index: 0  1  2  3  4  5
        3     5        array: [ 1, 3, 5, 4, 8, 9 ]
       / \   /
      4   8 9

parent(i) = (i - 1) / 2
left(i)   = 2*i + 1
right(i)  = 2*i + 2
```
Memorize these three lines — everything else is derived from them.

**The two operations**: sift up (after inserting at the end: while smaller than parent, swap up), sift down (after removing the root: move the last element to the root, then while bigger than the smallest child, swap down).

## Complexity
peek O(1) · push/pop O(log n) · `heapify(array)` O(n) — **not** O(n log n), build bottom-up from index `n/2-1` down · contains/arbitrary-remove O(n) (the weakness — use lazy deletion) · heapsort O(n log n) time, O(1) space, **not stable**.

**Heap vs balanced BST**:

| | Heap | Balanced BST |
|---|---|---|
| find min/max | O(1) | O(log n) |
| insert | O(log n) | O(log n) |
| delete min | O(log n) | O(log n) |
| search value x | O(n) | O(log n) |
| sorted output | O(n log n) | O(n) inorder |

## When the interviewer expects a heap
- "top K" / "kth largest" / "kth smallest"
- "merge K sorted things"
- "median of a stream" → **two** heaps
- scheduling by priority, Dijkstra, Prim, Huffman
- "closest K points"

**The kth-largest rule people get backwards**: kth **largest** → keep a **min**-heap of size k (root is the answer, evict the smallest). kth **smallest** → keep a **max**-heap of size k. Cost O(n log k), beats sorting's O(n log n) when k ≪ n.

## What's in the code
Generic binary heap with a pluggable `Comparator`, heapsort, the `PriorityQueue` API, then Kth Largest (LC 215), Top K Frequent via heap (LC 347), K Closest Points (LC 973), Merge K Sorted Lists (LC 23), Median From a Data Stream via two heaps (LC 295), Task Scheduler (LC 621).

## Connects to
- [Tree](../01-tree/) — "complete binary tree" is defined there; this file exploits completeness to drop pointers entirely.
- [Sorting](../../06-core-techniques/01-sorting/) — heapsort is this structure used as a sort; quickselect in that same file is the partition-only alternative to building a heap for "top-K" questions.
- [Graph Algorithms](../../05-graphs/02-graph-algorithms/) — Dijkstra and Prim are "always expand the closest known frontier node next" — exactly what a min-heap gives you for free.
- [Queue](../../03-linear-structures/02-queue/) — same "always process next" idea without ordering degenerates to plain FIFO; that file calls out the naming confusion directly.

## Self-test
- Give the three index formulas for an array-backed heap.
  > `parent(i) = (i-1)/2`, `left(i) = 2*i+1`, `right(i) = 2*i+2` (integer division). These fall directly out of storing a complete tree level-by-level, left to right, in a flat array — each node's children always live at fixed offsets from its own index, so no pointers are needed.
- Why is building a heap O(n) and not O(n log n)?
  > `heapify()` starts at the last non-leaf index (`n/2-1`) and sifts *down* toward the root, rather than sifting up while inserting elements one at a time. Most nodes live near the bottom of the tree and have very little height left to sift through — only a few nodes near the root have the full O(log n) height to traverse. Summing sift-down cost across all levels gives a series that converges to O(n), not O(n log n).
- kth **largest**: min-heap or max-heap of size k? Why?
  > A min-heap of size k (`findKthLargest`). Push every element, popping the current minimum whenever the heap exceeds size k; once done, the heap holds exactly the k largest values seen, and its root — the smallest of that set — is the kth largest. Cost O(n log k), cheaper than sorting when k ≪ n.
- Why is `PriorityQueue.toString()` not sorted?
  > The backing array is only *heap-ordered* (each parent <= its children), not fully sorted — sorting on every insert would defeat the point of O(log n) `push`. `toString()`/iteration just walks the underlying array in storage order; only repeated `poll()` calls, which perform the sift-down that restores heap order, yield values in sorted sequence.
- Why is `remove(Object)` O(n) on a `PriorityQueue`, and how do you work around it?
  > A heap array has no index for finding an arbitrary value by identity/equality — locating it requires a linear scan, and removing an interior element then needs a sift to restore heap order. The workaround is lazy deletion: keep a separate "stale" set of removed values, and skip (discard) any polled entry that's in that set instead of physically removing it from the heap immediately.
- `MedianFinder`: state the two invariants between its two heaps.
  > (1) Size balance: `lo.size() == hi.size()` or `lo.size() == hi.size() + 1` — `lo` (max-heap of the smaller half) never trails `hi` and is at most one element ahead. (2) Ordering: every element in `lo` is <= every element in `hi`. Together these guarantee `findMedian` is O(1): `lo.peek()` alone when sizes differ, otherwise the average of both peeks.
- Why `Comparator.comparingInt` instead of `(a,b) -> a - b`?
  > `a - b` silently overflows for values near `Integer.MIN_VALUE`/`MAX_VALUE`, flipping the sign and producing a wrong, hard-to-reproduce ordering. `Comparator.comparingInt` compares the extracted keys directly without subtraction, so it can't overflow — the safe default the file recommends over the manual-subtraction lambda.
