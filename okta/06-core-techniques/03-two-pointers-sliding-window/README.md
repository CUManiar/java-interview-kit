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
- Why does sliding window fail when the array contains negatives?
- Container With Most Water: prove moving the taller pointer can't help.
- LC 3: why `Math.max` when updating `left`?
- LC 424: why is a maxCount that never decreases still correct?
- Sort Colors: why don't you increment `mid` after swapping with `high`?
- LC 560: why seed the map with `{0: 1}`?
