# Roadmap — how the 15 topics connect

The old flat `ds/` + `algos/` layout listed 15 files with no visible relationship between
them. They were never actually independent — the code just never said so out loud. This
document is the map: what depends on what, and why, so you can trace an idea across
topics instead of re-memorizing it 15 separate times.

## How to actually use this repo

1. Work through the stage folders **in order**, `01-` through `07-`. Within a stage, the
   subfolders are numbered in the order to read them too.
2. Open a topic's `README.md` first. **Don't open the `.java` file yet.** Read the mental
   model, then close your eyes and redraw the diagram / state the complexity from memory.
3. Now open the `.java` file. Read the implementation, then the patterns. Run it:
   `java <stage>/<topic>/File.java`.
4. Close the code. Answer the **Self-test** questions in the README out loud. If you can't
   answer one, you don't own it yet — go back, don't push forward on a shaky base.
5. Before starting the next topic, read its **Prerequisites** and **Connects to** sections.
   Those backlinks point at the topic(s) you just finished — that's not a coincidence,
   it's the join. Understanding *why* topic N+1 needed topic N is the actual goal here,
   more than any individual algorithm.

That loop — concept, code, recall, link — is the difference between understanding a
structure and memorizing 15 unrelated files.

## The stage flow

```text
 1  FOUNDATIONS            Generics
        |                  (every <T> below assumes this)
        v
 2  HASHING                HashMap / HashSet
        |                  (powers roughly half of everything downstream)
        v
 3  LINEAR STRUCTURES      Stack -> Queue -> Linked List
        |
        v
 4  TREES, HEAPS & TRIES   Tree -> Heap -> Trie
        |
        v
 5  GRAPHS                 Graph Basics -> Graph Algorithms
        |
        v
 6  CORE TECHNIQUES        Sorting -> Searching -> Two Pointers / Sliding Window
        |
        v
 7  ADVANCED PATTERNS      Backtracking -> Dynamic Programming
```

One deliberate change from the old reading order: **Backtracking now comes before
Dynamic Programming.** DP's own file says "write the recursion first, add a cache, then
convert to a table" — that recursion *is* backtracking. Seeing the brute-force
choose/explore/undo loop first makes memoization look like the one-line optimization it
actually is, instead of a new topic to memorize from scratch.

## Recurring threads

Fifteen files, but far fewer *ideas*. The same handful of mechanisms keep reappearing in
a different costume. Once you notice a thread, later topics stop feeling new.

**Recursion, three costumes.**
[Tree](04-trees-heaps-tries/01-tree/) traversal → [Backtracking](07-advanced-patterns/01-backtracking/)'s
decision tree → [Dynamic Programming](07-advanced-patterns/02-dynamic-programming/)'s
memoized recursion. Same call-stack mechanics each time; DP just adds a cache because the
same state recurs.

**Hashing for O(1) lookup.**
[HashMap](02-hashing/01-hashmap/) fundamentals → Two Sum / Group Anagrams (same file) →
prefix-sum counting in [Two Pointers & Sliding Window](06-core-techniques/03-two-pointers-sliding-window/)
→ the old→new map in [Graph Basics](05-graphs/01-graph-basics/)'s `cloneGraph` → the
memoization cache in [Dynamic Programming](07-advanced-patterns/02-dynamic-programming/).
Different problem every time, same instinct: trade memory for a lookup that would
otherwise cost you a scan.

**BFS / shortest-path.**
[Queue](03-linear-structures/02-queue/)'s level-order template → tree level-order
([Tree](04-trees-heaps-tries/01-tree/)) → [Graph Basics](05-graphs/01-graph-basics/)'
shortest-path-in-unweighted-graphs → [Graph Algorithms](05-graphs/02-graph-algorithms/)'
Dijkstra (weighted) and 0-1 BFS (weights 0/1, reuses the deque instead of a heap).

**Heap as "always process the best thing next."**
[Heap](04-trees-heaps-tries/02-heap/)'s top-K/kth-largest → heapsort
([Sorting](06-core-techniques/01-sorting/)) → Dijkstra and Prim
([Graph Algorithms](05-graphs/02-graph-algorithms/)), which are just "pop the closest
frontier node" using this exact structure.

**Union-Find.**
Introduced in [Graph Basics](05-graphs/01-graph-basics/) for undirected-cycle detection
and connected components, then reused as-is inside Kruskal's MST in
[Graph Algorithms](05-graphs/02-graph-algorithms/).

**The monotonic invariant.**
[Stack](03-linear-structures/01-stack/)'s monotonic stack (next greater element) and
[Queue](03-linear-structures/02-queue/)'s monotonic deque (sliding window maximum) are
the same trick — maintain a sequence that's always sorted in one direction, so each
element is pushed and popped at most once.

**Sorting as a prerequisite, not just a topic.**
[Sorting](06-core-techniques/01-sorting/) enables the opposite-ends shape in
[Two Pointers](06-core-techniques/03-two-pointers-sliding-window/), the duplicate-skip
guard in [Backtracking](07-advanced-patterns/01-backtracking/), and the edge-order in
Kruskal ([Graph Algorithms](05-graphs/02-graph-algorithms/)).

**Two pointers, same family.**
Slow/fast pointers for cycle detection in
[Linked List](03-linear-structures/03-linked-list/) are the exact same idea as
same-direction two pointers on arrays in
[Two Pointers & Sliding Window](06-core-techniques/03-two-pointers-sliding-window/).

**Binary search beyond arrays.**
[Searching](06-core-techniques/02-searching/)'s "search on the answer" turns some
[Dynamic Programming](07-advanced-patterns/02-dynamic-programming/) problems (LIS in
O(n log n)) into a monotonic feasibility check plus a log-factor search.

## Full topic index

| # | Stage | Topic | Path |
|---|---|---|---|
| 1 | 1. Foundations | Generics | [`01-foundations/01-generics/`](01-foundations/01-generics/) |
| 2 | 2. Hashing | HashMap / HashSet | [`02-hashing/01-hashmap/`](02-hashing/01-hashmap/) |
| 3 | 3. Linear Structures | Stack | [`03-linear-structures/01-stack/`](03-linear-structures/01-stack/) |
| 4 | 3. Linear Structures | Queue + Deque | [`03-linear-structures/02-queue/`](03-linear-structures/02-queue/) |
| 5 | 3. Linear Structures | Linked List | [`03-linear-structures/03-linked-list/`](03-linear-structures/03-linked-list/) |
| 6 | 4. Trees, Heaps & Tries | Tree / BST | [`04-trees-heaps-tries/01-tree/`](04-trees-heaps-tries/01-tree/) |
| 7 | 4. Trees, Heaps & Tries | Heap / Priority Queue | [`04-trees-heaps-tries/02-heap/`](04-trees-heaps-tries/02-heap/) |
| 8 | 4. Trees, Heaps & Tries | Trie | [`04-trees-heaps-tries/03-trie/`](04-trees-heaps-tries/03-trie/) |
| 9 | 5. Graphs | Graph Basics | [`05-graphs/01-graph-basics/`](05-graphs/01-graph-basics/) |
| 10 | 5. Graphs | Graph Algorithms | [`05-graphs/02-graph-algorithms/`](05-graphs/02-graph-algorithms/) |
| 11 | 6. Core Techniques | Sorting | [`06-core-techniques/01-sorting/`](06-core-techniques/01-sorting/) |
| 12 | 6. Core Techniques | Searching | [`06-core-techniques/02-searching/`](06-core-techniques/02-searching/) |
| 13 | 6. Core Techniques | Two Pointers / Sliding Window | [`06-core-techniques/03-two-pointers-sliding-window/`](06-core-techniques/03-two-pointers-sliding-window/) |
| 14 | 7. Advanced Patterns | Backtracking | [`07-advanced-patterns/01-backtracking/`](07-advanced-patterns/01-backtracking/) |
| 15 | 7. Advanced Patterns | Dynamic Programming | [`07-advanced-patterns/02-dynamic-programming/`](07-advanced-patterns/02-dynamic-programming/) |

## Interview-time triage

Once the topics are connected in your head, this table is the fast lookup for "which
thread do I pull on":

| Prompt says | Reach for |
|---|---|
| sorted array, pair/triplet | two pointers |
| contiguous subarray/substring, "at most K" | sliding window |
| subarray sum with negatives | prefix sum + HashMap |
| "next/previous greater or smaller" | monotonic stack |
| top K, kth largest, merge K | heap (or quickselect) |
| shortest path, unweighted | BFS |
| shortest path, weighted non-negative | Dijkstra |
| dependencies, ordering, prerequisites | topological sort |
| "are these connected", dynamic merging | Union-Find |
| prefix / autocomplete / dictionary | Trie |
| all combinations/permutations/partitions | backtracking |
| count ways, min/max cost, longest | DP |
| sorted-ish, "find the minimum X that works" | binary search on the answer |
| overlapping ranges | sort by start (merge) or end (greedy) |

## Complexity quick reference

| Structure | Access | Search | Insert | Delete |
|---|---|---|---|---|
| Array | O(1) | O(n) | O(n) | O(n) |
| ArrayList | O(1) | O(n) | O(1) amort. | O(n) |
| LinkedList | O(n) | O(n) | O(1)\* | O(1)\* |
| Stack / Queue | — | O(n) | O(1) | O(1) |
| HashMap / HashSet | — | O(1) avg | O(1) avg | O(1) avg |
| TreeMap / TreeSet | — | O(log n) | O(log n) | O(log n) |
| Heap (PriorityQueue) | O(1) peek | O(n) | O(log n) | O(log n) |
| Trie | — | O(L) | O(L) | O(L) |
| Balanced BST | — | O(log n) | O(log n) | O(log n) |

\* given a reference to the node

## Java collections — what to reach for

| Need | Use | Not |
|---|---|---|
| Stack | `ArrayDeque` | `java.util.Stack` (sync'd Vector, iterates backwards) |
| Queue | `ArrayDeque` | `LinkedList` (more GC churn) |
| Deque | `ArrayDeque` | — |
| List | `ArrayList` | `LinkedList` |
| Map | `HashMap` | `Hashtable` |
| Sorted map | `TreeMap` | — |
| Heap | `PriorityQueue` | — |
| Thread-safe map | `ConcurrentHashMap` | `Collections.synchronizedMap` |

Counting: `map.merge(k, 1, Integer::sum)`
Grouping: `map.computeIfAbsent(k, x -> new ArrayList<>()).add(v)`
Max-heap: `new PriorityQueue<>(Comparator.reverseOrder())`
Overflow-safe mid: `lo + (hi - lo) / 2`
Never: `(a, b) -> a - b` as a comparator (overflow) — use `Integer.compare`
