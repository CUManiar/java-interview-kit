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

**Why Dijkstra fails on negative edges**: it finalizes a node the moment it's popped, assuming no later path can be shorter.
```
A --1--> B          A->C = 2
A --2--> C          A->B->C = 1 + (-5) = -4   <- Dijkstra already
B --(-5)--> C                                     locked C at 2
```

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
- Why V-1 rounds in Bellman-Ford, and what does a V-th improving round mean?
- In LC 787, why must you snapshot `dist` before each round?
- Kahn's algorithm: how do you detect a cycle without extra bookkeeping?
- Why must `k` be the outer loop in Floyd-Warshall?
- Kruskal vs Prim: which fits an edge list, which fits an adjacency list?
- What's the "stale entry" check in lazy Dijkstra, and why is it needed?
- When is 0-1 BFS strictly better than Dijkstra?
