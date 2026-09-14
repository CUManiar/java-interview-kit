# 9. Graph Basics (representations, traversals, Union-Find)

Stage 5/7 — Graphs

Code: [`GraphNotes.java`](./GraphNotes.java) — run `java 05-graphs/01-graph-basics/GraphNotes.java`

Algorithms that run on top of these representations (Dijkstra, MST, topo sort) live in [Graph Algorithms](../02-graph-algorithms/) — read this file first.

## Prerequisites
- [Queue](../../03-linear-structures/02-queue/) — BFS here is that file's template plus a `visited` set.
- [HashMap](../../02-hashing/01-hashmap/) — the default adjacency-list representation.
- [Tree](../../04-trees-heaps-tries/01-tree/) — a tree is a graph with no cycles; if traversals felt natural there, this is the same idea generalized.

## Mental model
**Vocabulary**: V/E = vertices/edges, directed (a→b) vs undirected (stored as both a→b and b→a), weighted (edges carry cost), DAG (directed acyclic — topological sort exists), dense (E~V²) vs sparse (E~V), degree (edges touching a vertex).

Sample graph used throughout the file (undirected):
```
     0 --- 1
     |   / |
     |  /  |
     2 --- 3       4 --- 5      <- two components

  adjacency list: 0:[1,2] 1:[0,2,3] 2:[0,1,3] 3:[1,2] 4:[5] 5:[4]
```

**Representations**:

| | Space | hasEdge(u,v) | iterate neighbours |
|---|---|---|---|
| Adjacency **list** | O(V+E) | O(deg(u)) | O(deg(u)) — **best default** |
| Adjacency matrix | O(V²) | O(1) | O(V) |
| Edge list | O(E) | O(E) | O(E) |

Default to the adjacency list. Matrix only for dense graphs or when the problem hands you one. Edge list is what Kruskal's MST and Bellman-Ford want.

**BFS vs DFS — pick correctly, this is half the interview**:

| | BFS | DFS |
|---|---|---|
| shortest path (unweighted) | YES | no |
| level/distance information | YES | no |
| connected components | yes | yes |
| cycle detection | yes | YES |
| topological sort | Kahn's | YES |
| all paths / backtracking | no | YES |
| memory | O(width) | O(height) |

**The #1 bug**: forgetting `visited`, or marking it at the wrong time. BFS → mark visited when you **enqueue** (else duplicates pile up in the queue). DFS → mark visited when you **enter** the node.

## What's in the code
Generic adjacency-list graph, Union-Find/DSU (path compression + union by size), grid-as-graph (most "graph" questions are secretly grids), cycle detection (directed and undirected), Clone Graph via old→new map (LC 133), Bipartite Check via 2-coloring BFS (LC 785).

## Connects to
- [Graph Algorithms](../02-graph-algorithms/) — hard prerequisite. Dijkstra, Kruskal, and topological sort are all built directly on the representations, BFS/DFS, and Union-Find defined here.
- [Queue](../../03-linear-structures/02-queue/) — BFS is that file's queue template with a `visited` set bolted on.
- [HashMap](../../02-hashing/01-hashmap/) — `cloneGraph`'s old→new map, and the adjacency list itself, are direct applications.
- [Backtracking](../../07-advanced-patterns/01-backtracking/) — "find all paths" is the choose/explore/undo loop run on a graph instead of an array.

## Self-test
- Why mark visited on ENQUEUE in BFS rather than on dequeue?
  > If you wait until dequeue, the same node can be pushed onto the queue multiple times — once for every already-queued neighbor that discovers it before it's ever processed — since nothing stopped a second `q.offer(nb)` for a node that's merely "seen" but not yet marked. That wastes time/memory and can duplicate entries in the visit order. Marking at enqueue (`seen.add(nb)` guarding the `q.offer(nb)`, as `bfs()` does) guarantees each node enters the queue exactly once.
- Directed cycle detection needs 3 states. What does each mean, and what goes wrong with only 2?
  > `hasCycleDirected`/`dfsCycle` use 0 = unvisited, 1 = in the current recursion stack (an ancestor on the active path), 2 = fully explored (done, popped off the path). A back edge to a state-1 node is a real cycle; an edge to a state-2 node is just a cross/forward edge to work already finished via a different path — perfectly fine in a DAG. With only 2 states (visited/unvisited) you can't tell those apart: a diamond like `0→1, 0→2, 1→3, 2→3` would revisit node 3 through two different parents and get falsely flagged as a cycle.
- When is BFS strictly better than DFS, and vice versa?
  > BFS wins whenever you need shortest path/level info in an unweighted graph — DFS has no notion of "distance so far." DFS wins for exhaustive search (all paths, backtracking) since BFS can't naturally enumerate paths, and for memory on a wide-but-shallow graph, since BFS memory is O(width) while DFS is O(height) (per the table above) — so a deep, narrow graph favors BFS's memory profile, and a shallow, wide one favors DFS's.
- What do path compression and union-by-size each buy you in DSU?
  > Union-by-size keeps trees shallow in the first place by always hanging the smaller tree under the bigger one's root, bounding height to O(log n) on its own. Path compression (`find()`'s path halving: `parent[x] = parent[parent[x]]`) flattens the path to the root every time it's traversed, so repeated `find`/`union` calls on the same nodes become effectively O(1). Together they give the ~O(α(n)) (inverse Ackermann) amortized bound; either alone is only O(log n).
- In `cloneGraph`, why put the clone in the map before recursing?
  > The map doubles as the visited set, and the graph can contain cycles. If you recursed into `n.neighbors` before calling `map.put(n, copy)`, a cycle back to `n` would find no entry for it yet and recurse into `clone(n, map)` again, forever — infinite recursion. Putting the clone in the map first means the cycle's return visit to `n` hits the `existing != null` check and returns immediately.
- How do you turn a grid problem into a graph problem in one sentence?
  > Each cell is a vertex, and its up-to-4 (or 8, if diagonals count) orthogonal neighbors are the edges — `DIRS4`/`DIRS8` plus bounds-checking is the adjacency list, computed on the fly instead of stored.
- Graph Valid Tree: what two conditions must hold?
  > Exactly `n - 1` edges, and the graph is fully connected (one component). Either alone is insufficient: `n-1` edges could still contain a cycle while leaving a vertex disconnected; checking both is naturally done with Union-Find — every `union` must succeed (no cycle) and you must end with exactly one component.
