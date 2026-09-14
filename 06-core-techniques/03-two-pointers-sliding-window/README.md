# 13. Two Pointers, Sliding Window, Prefix Sums

Stage 6/7 — Core Algorithmic Techniques

Code: [`TwoPointerSlidingWindow.java`](./TwoPointerSlidingWindow.java) — run `java 06-core-techniques/03-two-pointers-sliding-window/TwoPointerSlidingWindow.java`

## Prerequisites
- [Sorting](../01-sorting/) — the opposite-ends two-pointer shape needs sorted input.
- [HashMap](../../02-hashing/01-hashmap/) — the prefix-sum variant pairs a running sum with a map of counts.

These three patterns cover a huge slice of array/string interview questions. Recognizing which one applies is 80% of the work.

## Mental model
**Two pointers, opposite ends** (converging, needs a sorted array or symmetry):
```
[1, 3, 5, 7, 9]   target sum = 10
 L           R     1+9=10 -> found
```
Move L right to increase the sum, R left to decrease it. Signals: "sorted," "pair that sums to," "palindrome," "container/area."

**Two pointers, same direction** (fast/slow, in-place filtering/compaction): `slow` = write position, `fast` = read position. Signals: "remove duplicates in place," "move zeroes," "partition."

**Sliding window** — a contiguous `[left, right]` window that expands on the right, shrinks from the left. Each index enters once, leaves once → O(n):
```
for (right = 0..n-1) {
    add arr[right] to the window state
    while (window is INVALID) { remove arr[left]; left++; }
    answer = max/min(answer, right - left + 1)
}
```
Fixed size k → shrink exactly when `(right-left+1) > k`. Variable size → shrink while a constraint is violated. Signals: "contiguous," "substring," "subarray," "at most K," "longest/shortest."

**The trap**: sliding window needs the constraint to be *monotonic* in window size. "Subarray sum equals K" with negative numbers is **not** a sliding-window problem — growing the window doesn't monotonically grow the sum. Use prefix-sum + HashMap instead.

**Prefix sums**: `prefix[i]` = sum of the first i elements, `sum(i..j) = prefix[j+1] - prefix[i]`. Combine with a HashMap of "how many times have I seen this prefix" to count subarrays with a given sum in O(n) — even with negative numbers.

## What's in the code
Part 1 opposite-ends two pointers, Part 2 same-direction two pointers, Part 3 fixed-size sliding window, Part 4 variable-size sliding window, Part 5 prefix sums, plus the array/string Java API notes.

## Connects to
- [Linked List](../../03-linear-structures/03-linked-list/) — the same-direction (fast/slow) shape here is identical to that file's cycle-detection and find-the-middle pointers, just on an array instead of nodes.
- [Sorting](../01-sorting/) — opposite-ends two pointers require sorted input first.
- [HashMap](../../02-hashing/01-hashmap/) — the fallback for the "trap" above: prefix-sum counting via `map.merge`.
- [Stack](../../03-linear-structures/01-stack/) / [Queue](../../03-linear-structures/02-queue/) — when the window needs a running max/min instead of a running sum, the monotonic stack/deque in those files is the tool, not a plain sliding window.

## Self-test
- How do you tell a sliding-window problem from a prefix-sum problem?
  > Sliding window needs the tracked quantity to move monotonically as the window grows — adding an element only makes the invalidity condition worse, so shrinking from the left always has a clear, correct direction. If growing the window can make things *better* (e.g. a negative number lowers the sum), there's no monotonic relationship to shrink against, and you need prefix sums, usually paired with a HashMap of counts, instead — see `subarraySum` (LC 560).
- Why does sliding window fail when the array contains negatives?
  > The core loop assumes `add arr[right]` only pushes the window state further toward invalid, so `while (window is INVALID) left++` can always restore validity by shrinking. A negative element can make the window *more* valid right after you just grew it, so there's no consistent rule for when to move `left` — you'd have to reconsider windows you'd already discarded.
- Container With Most Water: prove moving the taller pointer can't help.
  > Area = `min(h[l], h[r]) * (r - l)`. Say `h[l] < h[r]` (left is shorter), so the current area is capped by `h[l]`. Moving `r` inward instead: the width `r - l` strictly decreases, and the height is still `min(h[l], h[r_new]) <= h[l]` since `h[l]` is unchanged — so the new area is a smaller-or-equal height times a smaller width, never larger. Only moving the shorter pointer has a chance of finding a taller bar that raises the height cap, which is why `maxArea` always does `if (h[l] < h[r]) l++; else r--;`.
- LC 3: why `Math.max` when updating `left`?
  > `last.get(c)` can point to an occurrence of `c` that's already outside the current window (before `left`, excluded by an earlier shrink). Setting `left = last.get(c) + 1` unconditionally could move `left` *backwards*, re-expanding the window to include characters already correctly excluded. `Math.max(left, last.get(c) + 1)` guarantees `left` only ever advances.
- LC 424: why is a maxCount that never decreases still correct?
  > `maxCount` is a historical high-water mark of the most frequent character seen in any window ending at the current `right`, not necessarily the true count of the most frequent character in the current (possibly shrunk) window. That's fine because the algorithm only cares about the maximum valid window length ever achieved: the window only ever slides (one shrink per expansion) rather than shrinking below a previously found length, so a stale `maxCount` can at worst delay an unnecessary shrink — it can never cause an invalid, longer window to be recorded as `best`, since `best` only grows when a strictly larger window becomes valid, i.e. when `maxCount` itself increases.
- Sort Colors: why don't you increment `mid` after swapping with `high`?
  > The value swapped into `mid` from `a[high]` hasn't been examined yet — it could be a 0, 1, or 2 — so `mid` must stay put to be checked next iteration. Contrast with the `a[mid]==0` case, which swaps with `low`: the invariant that `[low..mid-1]` are all 1s guarantees the value swapped in from `low` is a 1, so it's safe to advance `mid` past it immediately.
- LC 560: why seed the map with `{0: 1}`?
  > `{0: 1}` represents the empty prefix (sum 0 before any elements are taken). Without it, a subarray that starts at index 0 and sums exactly to `k` would never be counted: at the point where the running `sum == k`, the lookup `seen.getOrDefault(sum - k, 0)` is checking for a prior prefix of `0`, and that prefix only exists in the map because it was seeded.
