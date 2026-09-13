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
- Directed cycle detection needs 3 states. What does each mean, and what goes wrong with only 2?
- When is BFS strictly better than DFS, and vice versa?
- What do path compression and union-by-size each buy you in DSU?
- In `cloneGraph`, why put the clone in the map before recursing?
- How do you turn a grid problem into a graph problem in one sentence?
- Graph Valid Tree: what two conditions must hold?
