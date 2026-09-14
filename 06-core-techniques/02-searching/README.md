# 12. Binary Search (and friends)

Stage 6/7 — Core Algorithmic Techniques

Code: [`SearchAlgos.java`](./SearchAlgos.java) — run `java 06-core-techniques/02-searching/SearchAlgos.java`

## Prerequisites
- [Sorting](../01-sorting/) — binary search needs a sorted array, or at minimum a monotonic predicate.

## Mental model
Every comparison eliminates half the search space: O(log n). Binary search does **not** require an array — it requires a monotonic predicate: some boolean `f(x)` that is `false...false, true...true`. You are finding the boundary.
```
index:  0  1  2  3  4  5  6
arr  : [1, 3, 5, 7, 9,11,13]   target = 9

lo=0 hi=6  mid=3 -> 7 < 9  -> lo=4
lo=4 hi=6  mid=5 -> 11> 9  -> hi=4
lo=4 hi=4  mid=4 -> 9 == 9 -> FOUND
```

**The three bugs everyone writes**:
1. **Overflow**: `mid = (lo + hi) / 2` overflows for large ints. Always: `mid = lo + (hi - lo) / 2`.
2. **Infinite loop**: with `while (lo < hi)` and `lo = mid`, if `hi == lo+1` then `mid == lo` and nothing moves. Use `mid = lo + (hi-lo+1)/2` (round up) when you write `lo = mid`.
3. **Off-by-one**: pick one template and never deviate.

**Template 1** — exact match, closed interval `[lo, hi]`: `while (lo <= hi) { ... lo = mid+1 / hi = mid-1 }`. After: `lo == insertion point`, `hi == lo-1`.

**Template 2** — first index where predicate is true, half-open `[lo, hi)`: `while (lo < hi) { if (ok(mid)) hi = mid; else lo = mid+1; } return lo;`. Answers lowerBound, upperBound, "minimum capacity that works," rotated array, peak finding. **Learn this one best.**

**Binary search on the answer**: when the question is "find the minimum x such that it's feasible," binary search over the *answer range*, not an array, with a monotonic `feasible(x)`. Koko bananas, ship packages, split array. Recognizing this is worth more than any other single search skill.

## What's in the code
Classic exact-match binary search (LC 704), lower/upper bound (the two most reusable variants), rotated sorted array (LC 33/153), peak element without a sorted array (LC 162), binary search on the answer via Koko Eating Bananas (LC 875), Search a 2D Matrix as one flat array (LC 74), Median of Two Sorted Arrays in O(log(min(m,n))) (LC 4), binary search on doubles (fixed iteration count, no epsilon dance).

## Connects to
- [Sorting](../01-sorting/) — the sorted-input guarantee this file depends on.
- [Two Pointers & Sliding Window](../03-two-pointers-sliding-window/) — both patterns exploit monotonicity; binary search cuts the *search space*, two pointers cut the *number of comparisons*.
- [Dynamic Programming](../../07-advanced-patterns/02-dynamic-programming/) — "binary search on the answer" often replaces an exponential DP with a feasibility check plus a log-factor search (see LIS in O(n log n) in that file's self-test).

## Self-test
- Write the overflow-safe mid. Why does the naive form break?
  > `mid = lo + (hi - lo) / 2`, used throughout (`binarySearch`, `lowerBound`, etc.). The naive `(lo + hi) / 2` breaks because `lo + hi` can overflow `int` when both are large — near `Integer.MAX_VALUE` the sum wraps negative, producing a nonsensical `mid` and either an `ArrayIndexOutOfBoundsException` or a corrupted search.
- When do you need mid rounded UP, and why?
  > When your loop narrows with `lo = mid` (not `mid + 1`) — i.e. `mid` itself might still be the answer and must stay in play. With floor-rounded `mid = lo + (hi-lo)/2`, if `hi == lo + 1` then `mid == lo`, so `lo = mid` doesn't move `lo` and the loop never terminates. Rounding up, `mid = lo + (hi-lo+1)/2`, forces `mid` past `lo` in that case.
- What does `Arrays.binarySearch` return for a missing key, and how do you turn it into an insertion index?
  > `-(insertionPoint) - 1` — always negative when the key is absent, since a real index is never negative. Recover the insertion point with `int ip = -result - 1;` (shown in `SearchAlgos.main`).
- Rotated array: what's the invariant that makes it work?
  > At any `mid`, at least one of `[lo..mid]` or `[mid..hi]` is a contiguous, un-rotated (fully sorted) run, because the array has at most one rotation point. `searchRotated` checks `a[lo] <= a[mid]` to determine which half is sorted, then checks whether `target` falls inside that sorted half's value range to decide which side to keep and which to discard.
- Recognizing "binary search on the answer": what must be true of `feasible()`?
  > It must be monotonic over the answer range — `false...false, true...true` (or the mirror image) — so there's exactly one boundary to search for. `minEatingSpeed`'s `canFinish` is exactly this: if speed `k` finishes in time, every speed greater than `k` finishes too.
- Why search the SHORTER array in median-of-two-sorted-arrays?
  > The binary search walks `i` from `0` to `m` (A's length) and derives `j = half - i` for B. If you searched the longer array instead, `i` could range up to that longer length while `half` is roughly half the combined length, forcing `j = half - i` negative — out of bounds. Searching the shorter array (enforced by `if (A.length > B.length) return findMedianSortedArrays(B, A);`) keeps `j` always within `[0, n]`, and incidentally gives the O(log(min(m,n))) time bound the method is named for.
