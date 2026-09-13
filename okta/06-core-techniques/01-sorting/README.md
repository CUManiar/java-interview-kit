# 11. Sorting

Stage 6/7 — Core Algorithmic Techniques

Code: [`SortingAlgos.java`](./SortingAlgos.java) — run `java 06-core-techniques/01-sorting/SortingAlgos.java`

## Prerequisites
- [Generics](../../01-foundations/01-generics/) — every comparison sort here is parameterized by `Comparable`/`Comparator`.

## Mental model
**Know this table cold — you will be asked**:

| Algorithm | Best | Average | Worst | Space | Stable | Notes |
|---|---|---|---|---|---|---|
| Bubble | O(n) | O(n²) | O(n²) | O(1) | YES | teaching only |
| Selection | O(n²) | O(n²) | O(n²) | O(1) | no | fewest swaps: n-1 |
| Insertion | O(n) | O(n²) | O(n²) | O(1) | YES | great for small/near-sorted; used inside real sorts for n<~32 |
| Merge | O(n log n) | O(n log n) | O(n log n) | O(n) | YES | predictable; linked lists; external sort |
| Quick | O(n log n) | O(n log n) | O(n²) | O(log n) | no | fastest in practice |
| Heap | O(n log n) | O(n log n) | O(n log n) | O(1) | no | in-place, poor cache behavior |
| Counting | O(n+k) | O(n+k) | O(n+k) | O(k) | YES | small integer range |
| Radix | O(d(n+k)) | O(d(n+k)) | O(d(n+k)) | O(n+k) | YES | fixed-width keys |
| Bucket | O(n+k) | O(n+k) | O(n²) | O(n) | YES | uniform distribution |

**Stable** means equal elements keep their relative order — matters for multi-key sorting.

**The O(n log n) lower bound**: any comparison sort needs Ω(n log n) — n! permutations, a binary decision tree with n! leaves has height ≥ log₂(n!) = Ω(n log n). Counting/radix/bucket beat it only because they don't compare — they exploit the structure of the keys.

**What Java actually does** (say this, it lands well): `Arrays.sort(int[])` → dual-pivot quicksort, not stable, O(1) extra space (fine — two equal ints are indistinguishable). `Arrays.sort(Object[])` / `Collections.sort(List)` → Timsort (merge+insertion hybrid), **stable**, O(n) space, finds existing sorted runs so it's O(n) on already-sorted input.

## What's in the code
All ten classic sorts (bubble, selection, insertion, merge, quick, heap, counting, radix, bucket) plus quickselect (kth-smallest in O(n) average), and the Java API cheat sheet for what you actually call.

## Connects to
- [Generics](../../01-foundations/01-generics/) — `Comparable` vs `Comparator`, applied here.
- [Heap](../../04-trees-heaps-tries/02-heap/) — heapsort is that structure used as a sort; quickselect (same file) is the cheaper alternative when you only need "top-K," not a full order.
- [Searching](../02-searching/) — binary search requires sorted (or monotonic) input; this is where that guarantee comes from.
- [Two Pointers & Sliding Window](../03-two-pointers-sliding-window/) — the "opposite ends" two-pointer shape needs a sorted array; quicksort's partition step *is* the same-direction two-pointer shape.
- [Backtracking](../../07-advanced-patterns/01-backtracking/) — duplicate-skipping in subsets/permutations only works after sorting first.
- [Graph Algorithms](../../05-graphs/02-graph-algorithms/) — Kruskal's MST sorts the edge list before the greedy pass.

## Self-test
- Which sorts are stable, and why does it matter?
- Why is O(n log n) a lower bound for comparison sorts?
- Why does Java use quicksort for primitives and Timsort for objects?
- Make quicksort O(n²). Now fix it.
- Why is quickselect O(n) but quicksort O(n log n)?
- Why does counting sort walk the input backwards?
- How do you sort an `int[]` in descending order in Java? (trick question)
- Why is `mid = lo + (hi-lo)/2` and not `(lo+hi)/2`?
