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
  > Bubble, Insertion, Merge, Counting, Radix, and Bucket are stable (per the table); Selection, Quick, and Heap are not. It matters for multi-key sorting — stably sort by the secondary key first, then by the primary key, and elements with equal primary keys retain their secondary-key order. An unstable sort gives no such guarantee, so a second sort pass can silently scramble the first.
- Why is O(n log n) a lower bound for comparison sorts?
  > A comparison sort's execution is a binary decision tree where each node is one comparison; to correctly sort n distinct elements it must distinguish among all n! possible orderings, so the tree needs at least n! leaves. A binary tree with n! leaves has height ≥ log₂(n!) = Ω(n log n), so no comparison-based algorithm can beat that in the worst case.
- Why does Java use quicksort for primitives and Timsort for objects?
  > Primitive elements have no identity beyond their value — two equal `int`s are indistinguishable — so instability is invisible, and dual-pivot quicksort's speed and O(1) extra space win. Objects can be `.equals()`-equal yet still distinguishable (fields not part of the comparison), so an unstable sort could visibly reorder equal-comparing objects; Timsort is stable and also exploits pre-existing sorted runs, giving O(n) on already-sorted input, which matters more for real-world object data.
- Make quicksort O(n²). Now fix it.
  > Remove the randomization and always pick a fixed pivot (e.g. `a[hi]`) on input that's already sorted or reverse-sorted: every partition splits into sizes 1 and n-1, giving `T(n) = T(n-1) + O(n) = O(n²)`. Fix: randomize the pivot index before partitioning — `partition()` does `int r = lo + new Random().nextInt(hi - lo + 1)` — or use median-of-three, so an adversary can't construct the worst case deterministically.
- Why is quickselect O(n) but quicksort O(n log n)?
  > Quickselect only recurses into the one side that contains the target index and discards the other entirely: `T(n) = T(n/2) + O(n)`, which sums to O(n) over a geometric series. Quicksort must fully order both sides, so it recurses into both: `T(n) = 2T(n/2) + O(n) = O(n log n)`.
- Why does counting sort walk the input backwards?
  > After the prefix-sum pass, `count[v]` holds one-past the last output slot reserved for value `v`. Walking the input from the end and placing each element at `out[--count[a[i]]]` means the *last* occurrence of a value in the input claims the *last* available slot for that value, so earlier equal elements land in earlier slots — preserving their original relative order. Walking forward would reverse the order of duplicates instead.
- How do you sort an `int[]` in descending order in Java? (trick question)
  > There's no `Arrays.sort(int[], Comparator)` overload — primitive arrays can't take a comparator. You must box to `Integer[]` and call `Arrays.sort(boxed, Comparator.reverseOrder())`, or sort ascending and reverse in place, or negate/sort/negate back.
- Why is `mid = lo + (hi-lo)/2` and not `(lo+hi)/2`?
  > `lo + hi` can overflow `int` when both are large (near `Integer.MAX_VALUE`), wrapping to a negative number and producing a garbage `mid`. `lo + (hi - lo)/2` never sums two large values, so it can't overflow — used consistently for `mergeSort`'s midpoint split.
