# 14. Backtracking (+ recursion, bit manipulation, intervals, greedy)

Stage 7/7 — Advanced Patterns

Code: [`Backtracking.java`](./Backtracking.java) — run `java 07-advanced-patterns/01-backtracking/Backtracking.java`

## Prerequisites
- [Tree](../../04-trees-heaps-tries/01-tree/) — backtracking is DFS over an *implicit* decision tree instead of an explicit one; same recursion shape.
- [Sorting](../../06-core-techniques/01-sorting/) — duplicate-handling needs sorted input first.

Read this **before** [Dynamic Programming](../02-dynamic-programming/) — DP is this pattern's optimized form.

## Mental model
DFS over a decision tree. At each node you **choose** an option, **explore** deeper, then **undo** the choice so you can try the next one.
```
void backtrack(path, choices) {
    if (goalReached) { record(path); return; }
    for (choice : choices) {
        if (!valid(choice)) continue;   // PRUNE - the real optimization
        path.add(choice);               // CHOOSE
        backtrack(path, next);          // EXPLORE
        path.remove(last);              // UNDO   <- forget this and everything breaks
    }
}
```
Decision tree for subsets of `[1,2,3]`:
```
                      []
            /                    \
        [1] (take 1)          [] (skip 1)
        /     \                /      \
    [1,2]    [1]            [2]       []
    /  \     /  \           /  \      /  \
[1,2,3][1,2][1,3][1]    [2,3] [2]  [3]  []
```
2³ = 8 leaves = 8 subsets. Every backtracking problem is a tree like this; only the branching rule and the pruning rule change.

**Complexity cheat sheet**: subsets O(n·2ⁿ) · permutations O(n·n!) · combinations O(k·C(n,k)) · N-Queens O(n!) (far better in practice with pruning) · word search O(m·n·4ᴸ).

**The three things people get wrong**: (1) forgetting to undo the choice, (2) adding the path by reference instead of copying — always `new ArrayList<>(path)` when recording, (3) handling duplicates without sorting first + skipping `i > start && a[i] == a[i-1]`.

## What's in the code
Subsets (LC 78, the base template), Permutations (LC 46), Combination Sum (LC 39, unlimited reuse via recursing with `i` not `i+1`), Word Search (LC 79), N-Queens (LC 51, the pruning showcase), Letter Combinations of a Phone Number (LC 17), Palindrome Partitioning (LC 131), Subsets via bitmask (no recursion), a bit-manipulation cheat sheet, Intervals (sort first, then sweep — always), and Greedy Jump Game.

## Connects to
- [Tree](../../04-trees-heaps-tries/01-tree/) — same DFS shape; the call stack *is* the undo mechanism.
- [Dynamic Programming](../02-dynamic-programming/) — **read these two together.** DP is backtracking on a problem with overlapping subproblems, where you cache a result instead of recomputing it. If you can write the brute-force backtracking, you can almost always turn it into a DP by adding memoization.
- [Sorting](../../06-core-techniques/01-sorting/) — duplicate-skipping here only works on sorted input.
- [Graph Basics](../../05-graphs/01-graph-basics/) — "find all paths" is this choose/explore/undo loop run on a graph instead of an array.
- [Trie](../../04-trees-heaps-tries/03-trie/) — Word Search II combines that file's trie pruning with this file's backtracking loop.

## Self-test
- Write the 4-line backtracking skeleton from memory.
  > `if (goalReached) { record(path); return; }` then loop over choices: `if (!valid(choice)) continue;` (prune), `path.add(choice)` (choose), `backtrack(path, next)` (explore), `path.remove(last)` (undo).
- Why `new ArrayList<>(path)` and not `path` when recording a result?
  > `path` is one mutable list reused across the whole recursion — every subsequent `add`/`remove` mutates it in place. Storing the bare reference in `res` means every "saved" result actually points to the same object, which ends up empty (or wrong) once backtracking pops everything back out. `new ArrayList<>(path)` copies the current contents into an independent list that later mutation can't touch.
- Combination Sum I recurses with `i`, II with `i+1`. Why?
  > Combination Sum I (LC 39) allows unlimited reuse of a candidate, so recursing with `combo(c, remain - c[i], i, ...)` keeps `i` as the next start index, letting the same element be chosen again. Combination Sum II (LC 40) treats candidates as a multiset used at most once each, so it recurses with `i + 1`, advancing past the current index so it can never repeat.
- The duplicate-skip guard is `i > start`, not `i > 0`. Why does that matter?
  > `i > start` only skips a value when it duplicates the *previous sibling already tried at this same recursion level* (same `for` loop). `i > 0` would instead compare against `nums[i-1]` even when `i-1` belongs to an ancestor call (already baked into `path`, not a rejected sibling), incorrectly skipping valid combinations that legitimately reuse a duplicate value across different depths (e.g. `[2,2]`).
- N-Queens: what are `r-c` and `r+c`, and why offset the diagonal index by n?
  > `r-c` is constant along every "\" diagonal and `r+c` is constant along every "/" diagonal, so each value uniquely identifies one diagonal for an O(1) `boolean[]` lookup. `r-c` ranges from `-(n-1)` to `n-1`, which includes negatives that can't index an array, so the code adds `n` (`r - c + n`) to shift it into `[0, 2n-2]`. `r+c` already ranges from `0` to `2n-2`, so it needs no offset.
- Word Search: why restore the cell, and why doesn't flood fill need to?
  > Word Search backtracks: a cell marked `'#'` for one candidate direction must look unvisited again for a sibling branch (a different direction, or a different starting cell's search), so `b[r][c] = saved` undoes the mark once that branch is done. Flood fill (e.g. counting islands) never needs a cell to look unvisited to a different branch — each cell belongs to exactly one connected component and stays marked for the rest of the traversal, so there's no undo step.
- Merge Intervals sorts by start; Erase Overlap Intervals sorts by end. Why the difference?
  > Merge Intervals just needs to sweep intervals in the order they begin so a running `end` can be extended or flushed — sorting by start is enough. Erase Overlap Intervals is greedy interval scheduling: to keep the maximum number of non-overlapping intervals you always keep whichever interval finishes earliest, since it leaves the most room for everything after it, which requires sorting by end.
- What does `x & (x-1)` do, and what's it used for?
  > It clears the lowest set bit of `x` (`x-1` turns the trailing zeros into ones and the lowest set bit into zero, so ANDing with `x` zeroes just that bit). Used in `hammingWeight` to count set bits in one iteration per set bit instead of per bit position (Brian Kernighan's algorithm), and to test for a power of two: `(x & (x-1)) == 0` for `x > 0`.
