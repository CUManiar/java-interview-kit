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
- [Searching](../../06-core-techniques/02-searching/) — some DP problems (LIS) are more efficiently solved in O(n log n) by binary search — here, finding the insertion point in a maintained `tails` array, not the "binary search on the answer" feasibility-check pattern from that file — than by filling the full table.
- [Stack](../../03-linear-structures/01-stack/) — compare the monotonic-stack solution to Largest Rectangle in Histogram against a DP framing of the same problem once you've done both.

## Self-test
- Name the 5 steps of setting up a DP.
  > State (what identifies a subproblem, e.g. `dp[i]`), Recurrence (how a state combines smaller states), Base case (smallest states, filled directly), Order (which direction fills the table so dependencies are ready first), Answer (which cell holds the final result — not always the last one).
- 0/1 knapsack iterates capacity downward, unbounded upward. Why?
  > In 0/1 knapsack each item is used at most once, so `dp[cap - weight]` must still reflect the *previous* item's pass, not one already updated by the current item — iterating capacity from high to low guarantees that. In unbounded knapsack (coin change) the same item can repeat, so iterating capacity upward is exactly what allows `dp[amt - coin]` to already include this same coin from earlier in the same pass.
- LC 518 vs LC 377: which loop is outer, and what does swapping them change?
  > LC 518 (`coinChangeCombinations`) puts the coin loop outer and amount loop inner (`for coin { for amt { dp[amt] += dp[amt-coin] } } }`), which counts each combination once regardless of coin order ({1,2} counted once). Swapping to amount outer / coin inner (LC 377, Combination Sum IV) counts ordered sequences instead, so {1,2} and {2,1} are counted separately as distinct permutations.
- Why must `maxProduct` track the minimum too?
  > Multiplying by a negative number flips sign, so the most negative running product can become the largest positive product on the next negative multiplication. Tracking only the running max would miss that; the code also tracks the running min and swaps `max`/`min` whenever the current number is negative, so `max` always reflects the best product ending at index `i`.
- LIS in O(n log n): what does `tails[k]` actually mean? Is `tails` the LIS?
  > `tails[k]` is the smallest possible tail (ending value) among all increasing subsequences of length `k+1` found so far. `tails` is not an actual LIS — its length equals the LIS length, but its values get overwritten as smaller tails are found for the same length, so they don't necessarily come from one real increasing subsequence together.
- When is memoization better than tabulation, and vice versa?
  > Memoization (top-down) wins when only part of the full state space is actually reachable (it skips states you'd never need) or when the natural recursive definition is much easier to write than a fill order. Tabulation (bottom-up) wins when you need to avoid recursion's stack-depth risk (~10k frames) and call overhead, or want to space-optimize down to a rolling window of previous rows.
- Edit distance: name the three predecessor cells and their operations.
  > `dp[i-1][j]` = delete a character from `a`, `dp[i][j-1]` = insert a character into `a`, `dp[i-1][j-1]` = replace a character (or free, with no `+1`, when `a.charAt(i-1) == b.charAt(j-1)`).
- Why is expand-around-center preferred over the DP table for LC 5?
  > Both run in O(n²) time, but expand-around-center uses O(1) extra space versus the DP table's O(n²), and needs no explicit table-indexing scheme to get right — per the code comment, it's "simpler than the DP table and just as fast."
