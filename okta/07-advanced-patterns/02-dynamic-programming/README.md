# 15. Dynamic Programming

Stage 7/7 — Advanced Patterns

Code: [`DynamicProgramming.java`](./DynamicProgramming.java) — run `java 07-advanced-patterns/02-dynamic-programming/DynamicProgramming.java`

## Prerequisites
- [Backtracking](../01-backtracking/) — **read first.** DP is backtracking's optimized form; the recursion you write there is step zero here.
- [HashMap](../../02-hashing/01-hashmap/) — top-down memoization is a cache keyed by state.

## Mental model
**When is it DP?** (1) Optimal substructure — the answer is built from answers to smaller instances of the *same* problem. (2) Overlapping subproblems — the same smaller instance is needed many times. Signals in the prompt: "how many ways," "minimum/maximum cost," "longest," "can you reach/partition," "count the paths" — plus brute force being exponential.

If subproblems don't overlap, it's divide-and-conquer (merge sort), not DP. If a greedy local choice is provably optimal, it's greedy, not DP.

**The 5-step recipe — follow it every time, don't improvise**:
1. **State** — what uniquely identifies a subproblem? `dp[i]`, `dp[i][j]`, `dp[i][cap]`
2. **Recurrence** — how does a state combine smaller states?
3. **Base case** — the smallest states, filled in directly
4. **Order** — which direction fills the table so dependencies are ready first?
5. **Answer** — which cell holds the final result? (not always the last one)

**Top-down vs bottom-up**: memoization (top-down) = recursion + a cache — mirrors the natural definition, only computes reachable states, but risks stack overflow (~10k frames) and has call overhead. Tabulation (bottom-up) = fill an array with loops — no stack risk, easy to space-optimize, but you must work out the fill order yourself. Write the recursion first, add a cache, then convert to a table if needed.

```
naive recursion, exponential:              memoized, linear:
           f(5)                              f(5)
         /      \                           /    \
      f(4)      f(3)                     f(4)   [f(3) CACHED]
     /   \      /   \                    /   \
  f(3)  f(2) f(2)  f(1)              f(3)  [f(2) CACHED]
  ...   ...  ...    (recomputed!)
```

**The pattern catalogue — recognize the shape, you already know the code**:

| Pattern | State | Example |
|---|---|---|
| Linear / 1D | `dp[i]` | climb stairs, house robber |
| Grid / 2D paths | `dp[r][c]` | unique paths, min path sum |
| Two sequences | `dp[i][j]` | LCS, edit distance |
| 0/1 Knapsack | `dp[i][capacity]` | subset sum, partition |
| Unbounded knapsack | `dp[capacity]` | coin change, rod cutting |
| Interval | `dp[i][j]` over ranges | burst balloons, MCM |
| Subsequence (LIS) | `dp[i]` = best ending at i | LIS, russian dolls |
| State machine | `dp[i][state]` | stock with cooldown/fee |
| Digit / bitmask | `dp[mask]` | TSP, count subsets |

## What's in the code
Fibonacci in three versions (to see the progression), Linear DP, Subsequence DP (LIS), Grid DP, Two-Sequence DP, Knapsack, Palindrome DP, Subarray DP, State-Machine DP, and a memoization template for when recursion is easier than the table.

## Connects to
- [Backtracking](../01-backtracking/) — the prerequisite. Write the brute-force recursion first; if the same state recurs, cache it — that's memoization.
- [HashMap](../../02-hashing/01-hashmap/) — the cache under top-down memoization.
- [Graph Algorithms](../../05-graphs/02-graph-algorithms/) — Bellman-Ford and Floyd-Warshall are this file's 5-step recipe applied to graphs (state = node or node-pair, recurrence = relax through an edge or an intermediate vertex).
- [Searching](../../06-core-techniques/02-searching/) — some DP problems (LIS) are more efficiently solved in O(n log n) by binary-searching the answer than by filling the full table.
- [Stack](../../03-linear-structures/01-stack/) — compare the monotonic-stack solution to Largest Rectangle in Histogram against a DP framing of the same problem once you've done both.

## Self-test
- Name the 5 steps of setting up a DP.
- 0/1 knapsack iterates capacity downward, unbounded upward. Why?
- LC 518 vs LC 377: which loop is outer, and what does swapping them change?
- Why must `maxProduct` track the minimum too?
- LIS in O(n log n): what does `tails[k]` actually mean? Is `tails` the LIS?
- When is memoization better than tabulation, and vice versa?
- Edit distance: name the three predecessor cells and their operations.
- Why is expand-around-center preferred over the DP table for LC 5?
