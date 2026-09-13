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
- Why `new ArrayList<>(path)` and not `path` when recording a result?
- Combination Sum I recurses with `i`, II with `i+1`. Why?
- The duplicate-skip guard is `i > start`, not `i > 0`. Why does that matter?
- N-Queens: what are `r-c` and `r+c`, and why offset the diagonal index by n?
- Word Search: why restore the cell, and why doesn't flood fill need to?
- Merge Intervals sorts by start; Erase Overlap Intervals sorts by end. Why the difference?
- What does `x & (x-1)` do, and what's it used for?
