# 10. Graph Algorithms

Stage 5/7 — Graphs

Code: [`GraphAlgos.java`](./GraphAlgos.java) — run `java 05-graphs/02-graph-algorithms/GraphAlgos.java`

Data-structure basics (representations, BFS/DFS, Union-Find) live in [Graph Basics](../01-graph-basics/) — read that first.

## Prerequisites
- [Graph Basics](../01-graph-basics/) — hard prerequisite, everything here is built on it.
- [Heap](../../04-trees-heaps-tries/02-heap/) — Dijkstra and Prim are both "always expand the cheapest frontier node," i.e. a heap driving a traversal.
- [Sorting](../../06-core-techniques/01-sorting/) — Kruskal sorts the edge list first.

## Mental model
**Pick the right algorithm — this decision table is the whole chapter**:

| Problem | Algorithm | Complexity |
|---|---|---|
| shortest path, unweighted | BFS | O(V+E) |
| shortest path, weights 0 or 1 | 0-1 BFS (deque) | O(V+E) |
| shortest path, non-negative weights | Dijkstra | O(E log V) |
| shortest path, negative weights | Bellman-Ford | O(V·E) |
| detect a negative cycle | Bellman-Ford | O(V·E) |
| shortest path, at most K edges | Bellman-Ford | O(K·E) |
| all-pairs shortest paths | Floyd-Warshall | O(V³) |
| MST, dense | Prim | O(E log V) |
| MST, sparse/edge list | Kruskal + DSU | O(E log E) |
| dependency ordering (DAG) | Topological sort | O(V+E) |
| cycle in a directed graph | DFS 3-colour | O(V+E) |
| cycle in an undirected graph | Union-Find | O(E·α) |
| connected components | DFS/BFS or DSU | O(V+E) |

**Why Dijkstra needs non-negative weights**: its correctness proof assumes that once a node is popped with the smallest known distance, no future relaxation can beat it. A negative edge breaks that assumption — a "settle on pop" Dijkstra (permanent `visited[]`, never revisit) locks in the wrong answer:
```
A --2--> B          A->C = 1
A --1--> C          A->B->C = 2 + (-5) = -3   <- "settle on pop" Dijkstra
B --(-5)--> C                                    already finalized C at 1
```
The `dijkstra()` below only skips *stale* queue entries (`d > dist[u]`) rather than permanently marking nodes visited, so it happens to self-correct on small negative-edge cases like this one — but that's not a guarantee: it has no correctness proof for negative weights, can't detect a negative cycle, and spins forever if one exists. Use Bellman-Ford whenever weights can be negative.

## What's in the code
Dijkstra, Bellman-Ford, Floyd-Warshall (all-pairs — 5 lines of DP once you see it), topological sort (Kahn's + DFS-based), Minimum Spanning Tree (Prim and Kruskal), Course Schedule (LC 207/210, the canonical topo-sort question), Network Delay Time (LC 743).

## Connects to
- [Graph Basics](../01-graph-basics/) — the representations, BFS/DFS, and Union-Find this file assumes.
- [Heap](../../04-trees-heaps-tries/02-heap/) — Dijkstra/Prim's priority queue.
- [Queue](../../03-linear-structures/02-queue/) — 0-1 BFS reuses that file's deque instead of a heap.
- [Sorting](../../06-core-techniques/01-sorting/) — Kruskal sorts edges by weight before the greedy Union-Find pass.
- [Dynamic Programming](../../07-advanced-patterns/02-dynamic-programming/) — Bellman-Ford (state = node, rounds = edges used) and Floyd-Warshall (state = node pair, transition = route through k) are both instances of that file's 5-step recipe.

## Self-test
- Why does Dijkstra break with negative weights? Give a 3-node example.
  > Dijkstra's proof of correctness assumes the node it just popped (smallest known distance) can never be improved later. `A --2--> B`, `A --1--> C`, `B --(-5)--> C`: after relaxing from `A`, `C` has distance 1 and `B` has distance 2, so a "settle on pop" Dijkstra finalizes `C = 1` first — but the true shortest path is `A→B→C = 2 + (-5) = -3`. Once `C` is marked done, that better path is never considered. (Note: the `dijkstra()` in this file only skips *stale* queue entries rather than permanently marking nodes visited, so it happens to self-correct on this particular example — see the mental-model note above for why that's still not a substitute for Bellman-Ford.)
- Why V-1 rounds in Bellman-Ford, and what does a V-th improving round mean?
  > Any shortest path in a graph with no negative cycle uses at most V-1 edges (a simple path visits each vertex once). After round `i`, every shortest path using ≤ i edges is correct, so V-1 rounds cover every possible shortest path. If a V-th round still finds an improvement, some "shortest path" would need V or more edges — impossible without revisiting a vertex, which only helps if a cycle on the way has negative total weight. That's exactly the check in `bellmanFord()`'s final loop, which throws `IllegalStateException` when it fires.
- In LC 787, why must you snapshot `dist` before each round?
  > `cheapestKStops` limits the path to `k+1` edges by doing `k+1` rounds, each allowed to extend a path by exactly one more edge. `prev = dist.clone()` freezes the distances from *before* this round so every relaxation in the round reads only "distance using ≤ i edges." Without the snapshot, relaxing edge A then edge B in the same pass could read A's just-updated `dist` value, silently chaining two hops into one round and violating the stop-count limit.
- Kahn's algorithm: how do you detect a cycle without extra bookkeeping?
  > `topoSortKahn` counts how many nodes it actually manages to output (`idx`). Every node it emits has had all its prerequisites already emitted (in-degree dropped to 0); if a cycle exists, every node in that cycle keeps a nonzero in-degree forever and is never queued. So `idx != n` at the end, using nothing but the count already being tracked, means a cycle blocked some nodes from ever being reachable.
- Why must `k` be the outer loop in Floyd-Warshall?
  > The recurrence `d[i][j] = min(d[i][j], d[i][k] + d[k][j])` is only valid if `d[i][k]` and `d[k][j]` already account for every intermediate vertex in `0..k-1` — the DP builds up the answer by progressively allowing one more vertex as a waypoint. If `k` weren't outermost, `d[i][k]`/`d[k][j]` could be read before they've incorporated all the shorter-index waypoints they're supposed to already reflect, breaking the dependency order the DP relies on.
- Kruskal vs Prim: which fits an edge list, which fits an adjacency list?
  > Kruskal (`kruskalMST`) sorts the entire edge list once and unions endpoints greedily — it wants edges as a flat, sortable list. Prim (`primMST`) grows outward from one node, repeatedly asking "what are this tree's frontier edges" — it wants an adjacency list so it can enumerate a node's neighbors directly.
- What's the "stale entry" check in lazy Dijkstra, and why is it needed?
  > `if (d > dist[u]) continue;` in `dijkstra()`. Because the lazy variant pushes a new heap entry every time a distance improves instead of decreasing a key in place, the same node can sit in the heap multiple times with different recorded distances. When a stale (larger, superseded) entry is popped, this check skips it — otherwise you'd re-relax `u`'s neighbors using an outdated, too-large distance, wasting work.
- When is 0-1 BFS strictly better than Dijkstra?
  > When every edge weight is 0 or 1. `zeroOneBFS` gets the same correct shortest distances using a deque in O(V+E), versus Dijkstra's O(E log V) heap — strictly cheaper for that restricted weight set, with no heap overhead at all.
