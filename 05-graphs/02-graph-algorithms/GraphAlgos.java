import java.util.*;

/* ==========================================================================
 * ALGOS: GRAPH ALGORITHMS
 * (data structure basics live in ../01-graph-basics/GraphNotes.java)
 * Run: java GraphAlgos.java
 * ==========================================================================
 *
 * PICK THE RIGHT ALGORITHM — this decision table is the whole chapter
 * ┌────────────────────────────────────────┬──────────────────┬────────────────┐
 * │ Problem                                │ Algorithm        │ Complexity     │
 * ├────────────────────────────────────────┼──────────────────┼────────────────┤
 * │ shortest path, UNWEIGHTED              │ BFS              │ O(V+E)         │
 * │ shortest path, weights all 0 or 1      │ 0-1 BFS (deque)  │ O(V+E)         │
 * │ shortest path, NON-NEGATIVE weights    │ Dijkstra         │ O(E log V)     │
 * │ shortest path, NEGATIVE weights        │ Bellman-Ford     │ O(V*E)         │
 * │ detect a negative cycle                │ Bellman-Ford     │ O(V*E)         │
 * │ shortest path, at most K edges         │ Bellman-Ford     │ O(K*E)         │
 * │ ALL pairs shortest paths               │ Floyd-Warshall   │ O(V^3)         │
 * │ minimum spanning tree, dense           │ Prim             │ O(E log V)     │
 * │ minimum spanning tree, sparse/edge list│ Kruskal + DSU    │ O(E log E)     │
 * │ dependency ordering (DAG)              │ Topological sort │ O(V+E)         │
 * │ cycle in a DIRECTED graph              │ DFS 3-colour     │ O(V+E)         │
 * │ cycle in an UNDIRECTED graph           │ Union-Find       │ O(E * alpha)   │
 * │ connected components                   │ DFS/BFS or DSU   │ O(V+E)         │
 * │ maximise path product / weird metrics  │ modified Dijkstra│ O(E log V)     │
 * └────────────────────────────────────────┴──────────────────┴────────────────┘
 *
 * WHY DIJKSTRA FAILS ON NEGATIVE EDGES
 *   Dijkstra finalises a node the moment it's popped, assuming no later path can
 *   be shorter. A negative edge can make a later path shorter. Example:
 *       A --1--> B          A->C = 2
 *       A --2--> C          A->B->C = 1 + (-5) = -4   <- Dijkstra already
 *       B --(-5)--> C                                     locked C at 2
 *
 * GRAPH SHAPES USED IN THIS FILE
 *   weighted, directed:  0 ->(4) 1,  0 ->(1) 2,  2 ->(2) 1,  1 ->(1) 3,  2 ->(5) 3
 *
 *            (4)
 *      0 ----------> 1 ---(1)---> 3
 *      |            ^             ^
 *     (1)          (2)           /
 *      |            |          (5)
 *      +----------> 2 ---------+
 *
 *   shortest 0->3 = 0->2->1->3 = 1+2+1 = 4   (NOT 0->1->3 = 5)
 * ========================================================================== */
public class GraphAlgos {

    /* ======================================================================
     * 1. DIJKSTRA — single source shortest path, non-negative weights
     *
     * Greedy: repeatedly finalise the unvisited node with the smallest known
     * distance, then relax its outgoing edges.
     *
     * "LAZY" variant (what everyone writes): don't bother decreasing keys in the
     * heap; just push the improved distance again and skip stale entries when
     * popping. Slightly more heap entries, far simpler code, same complexity.
     * ====================================================================== */
    static int[] dijkstra(int n, List<int[]>[] adj, int src) {
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;

        // {node, distance}, min-heap on distance
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(x -> x[1]));
        pq.offer(new int[]{src, 0});

        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int u = cur[0], d = cur[1];
            if (d > dist[u]) continue;              // STALE entry, already improved. Skip.

            for (int[] e : adj[u]) {                // e = {to, weight}
                int v = e[0], w = e[1];
                if (dist[u] + w < dist[v]) {        // RELAX
                    dist[v] = dist[u] + w;
                    pq.offer(new int[]{v, dist[v]});
                }
            }
        }
        return dist;
    }

    /* Dijkstra + path reconstruction via a parent array. */
    static List<Integer> dijkstraPath(int n, List<int[]>[] adj, int src, int dst) {
        int[] dist = new int[n], parent = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(parent, -1);
        dist[src] = 0;

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(x -> x[1]));
        pq.offer(new int[]{src, 0});
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            if (cur[1] > dist[cur[0]]) continue;
            for (int[] e : adj[cur[0]]) {
                if (dist[cur[0]] + e[1] < dist[e[0]]) {
                    dist[e[0]] = dist[cur[0]] + e[1];
                    parent[e[0]] = cur[0];
                    pq.offer(new int[]{e[0], dist[e[0]]});
                }
            }
        }
        if (dist[dst] == Integer.MAX_VALUE) return List.of();
        LinkedList<Integer> path = new LinkedList<>();
        for (int at = dst; at != -1; at = parent[at]) path.addFirst(at);
        return path;
    }

    /* ======================================================================
     * 2. BELLMAN-FORD — handles NEGATIVE weights, detects negative cycles
     *
     * Relax EVERY edge, V-1 times. Why V-1? Any shortest path has at most V-1
     * edges, and after the i-th round all shortest paths using <= i edges are
     * correct. If a V-th round still improves something, a negative cycle exists.
     *
     * LC 787 (cheapest flight within K stops) is Bellman-Ford with K+1 rounds,
     * using a snapshot of the previous round so one round can't chain.
     * ====================================================================== */
    static int[] bellmanFord(int n, int[][] edges, int src) {
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;

        for (int round = 0; round < n - 1; round++) {
            boolean changed = false;
            for (int[] e : edges) {                 // e = {u, v, w}
                if (dist[e[0]] == Integer.MAX_VALUE) continue;
                if (dist[e[0]] + e[2] < dist[e[1]]) {
                    dist[e[1]] = dist[e[0]] + e[2];
                    changed = true;
                }
            }
            if (!changed) break;                    // early exit, converged
        }

        for (int[] e : edges) {                     // one extra round = cycle check
            if (dist[e[0]] != Integer.MAX_VALUE && dist[e[0]] + e[2] < dist[e[1]])
                throw new IllegalStateException("negative weight cycle detected");
        }
        return dist;
    }

    /* LC 787 — cheapest price with at most k stops. The snapshot is the key line. */
    static int cheapestKStops(int n, int[][] flights, int src, int dst, int k) {
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;
        for (int i = 0; i <= k; i++) {                   // k stops = k+1 edges
            int[] prev = dist.clone();                   // SNAPSHOT: prevents chaining
            for (int[] f : flights) {
                if (prev[f[0]] == Integer.MAX_VALUE) continue;
                dist[f[1]] = Math.min(dist[f[1]], prev[f[0]] + f[2]);
            }
        }
        return dist[dst] == Integer.MAX_VALUE ? -1 : dist[dst];
    }

    /* ======================================================================
     * 3. FLOYD-WARSHALL — all pairs shortest path, O(V^3). 5 lines of DP.
     * dist[i][j] = min over k of (dist[i][k] + dist[k][j])
     * The k loop MUST be outermost: "allow paths through vertices 0..k".
     * ====================================================================== */
    static int[][] floydWarshall(int[][] w) {
        int n = w.length;
        int[][] d = new int[n][n];
        for (int i = 0; i < n; i++) d[i] = w[i].clone();

        for (int k = 0; k < n; k++)                    // intermediate vertex, OUTERMOST
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    if (d[i][k] != Integer.MAX_VALUE && d[k][j] != Integer.MAX_VALUE)
                        d[i][j] = Math.min(d[i][j], d[i][k] + d[k][j]);
        return d;
    }

    /* ======================================================================
     * 4. TOPOLOGICAL SORT — linear ordering of a DAG
     *
     *   Course 0 -> 1 -> 3
     *              2 ->/
     *   valid order: 0,1,2,3 or 0,2,1,3 (not unique)
     *
     * A. KAHN'S (BFS): repeatedly take a node with in-degree 0.
     *    If the output has fewer than n nodes -> there's a CYCLE.
     *    This is why LC 207 "Course Schedule" is a topo-sort problem.
     * ====================================================================== */
    static int[] topoSortKahn(int n, List<List<Integer>> adj) {
        int[] indeg = new int[n];
        for (List<Integer> nbs : adj) for (int v : nbs) indeg[v]++;

        Queue<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) if (indeg[i] == 0) q.offer(i);

        int[] order = new int[n];
        int idx = 0;
        while (!q.isEmpty()) {
            int u = q.poll();
            order[idx++] = u;
            for (int v : adj.get(u))
                if (--indeg[v] == 0) q.offer(v);      // v's last dependency is gone
        }
        if (idx != n) return new int[0];              // cycle -> no valid ordering
        return order;
    }

    /* B. DFS variant: post-order, then REVERSE. A node is appended only after
     *    all its descendants, so reversing gives dependencies-first order.
     *    3-colour state doubles as cycle detection. */
    static int[] topoSortDFS(int n, List<List<Integer>> adj) {
        int[] state = new int[n];             // 0 unvisited, 1 in-stack, 2 done
        Deque<Integer> stack = new ArrayDeque<>();
        for (int i = 0; i < n; i++)
            if (state[i] == 0 && !topoDfs(i, adj, state, stack)) return new int[0];

        int[] order = new int[n];
        for (int i = 0; i < n; i++) order[i] = stack.pop();   // pop = reverse post-order
        return order;
    }
    private static boolean topoDfs(int u, List<List<Integer>> adj, int[] state,
                                   Deque<Integer> stack) {
        state[u] = 1;
        for (int v : adj.get(u)) {
            if (state[v] == 1) return false;                  // back edge -> cycle
            if (state[v] == 0 && !topoDfs(v, adj, state, stack)) return false;
        }
        state[u] = 2;
        stack.push(u);                                        // push AFTER children
        return true;
    }

    /* ======================================================================
     * 5. MINIMUM SPANNING TREE
     *
     * MST = cheapest set of edges connecting all V vertices, exactly V-1 edges,
     * no cycles. Only defined for CONNECTED UNDIRECTED graphs.
     *
     * A. KRUSKAL — sort all edges, add the cheapest that doesn't create a cycle.
     *    Cycle check = Union-Find. Best when you're handed an EDGE LIST.
     * ====================================================================== */
    static class UnionFind {
        int[] p, sz;
        UnionFind(int n) { p = new int[n]; sz = new int[n];
            for (int i = 0; i < n; i++) { p[i] = i; sz[i] = 1; } }
        int find(int x) { while (p[x] != x) { p[x] = p[p[x]]; x = p[x]; } return x; }
        boolean union(int a, int b) {
            int ra = find(a), rb = find(b);
            if (ra == rb) return false;                 // same set -> would make a cycle
            if (sz[ra] < sz[rb]) { int t = ra; ra = rb; rb = t; }
            p[rb] = ra; sz[ra] += sz[rb];
            return true;
        }
    }

    static int kruskalMST(int n, int[][] edges) {       // edges = {u, v, w}
        Arrays.sort(edges, Comparator.comparingInt(e -> e[2]));   // cheapest first
        UnionFind uf = new UnionFind(n);
        int total = 0, used = 0;
        for (int[] e : edges) {
            if (uf.union(e[0], e[1])) {                 // no cycle -> take it
                total += e[2];
                if (++used == n - 1) break;             // MST complete
            }
        }
        return used == n - 1 ? total : -1;              // -1 = graph is disconnected
    }

    /* B. PRIM — grow one tree from a start node; always take the cheapest edge
     *    leaving the tree. Uses a heap. Best with an ADJACENCY LIST.
     *    This is LC 1584 "Min Cost to Connect All Points". */
    static int primMST(int n, List<int[]>[] adj) {
        boolean[] inTree = new boolean[n];
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(x -> x[1]));
        pq.offer(new int[]{0, 0});                       // {node, edgeCost}
        int total = 0, count = 0;

        while (!pq.isEmpty() && count < n) {
            int[] cur = pq.poll();
            if (inTree[cur[0]]) continue;                // stale entry
            inTree[cur[0]] = true;
            total += cur[1];
            count++;
            for (int[] e : adj[cur[0]])
                if (!inTree[e[0]]) pq.offer(new int[]{e[0], e[1]});
        }
        return count == n ? total : -1;
    }

    /* ======================================================================
     * 6. 0-1 BFS — weights are only 0 or 1. Deque instead of a heap -> O(V+E).
     *   weight 0 edge -> addFirst (free, explore now)
     *   weight 1 edge -> addLast  (costs one, explore later)
     * Used for LC 1091 variants, LC 1368 "minimum cost to make a valid path".
     * ====================================================================== */
    static int[] zeroOneBFS(int n, List<int[]>[] adj, int src) {
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;
        Deque<Integer> dq = new ArrayDeque<>();
        dq.offerFirst(src);

        while (!dq.isEmpty()) {
            int u = dq.pollFirst();
            for (int[] e : adj[u]) {
                int v = e[0], w = e[1];
                if (dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w;
                    if (w == 0) dq.offerFirst(v); else dq.offerLast(v);
                }
            }
        }
        return dist;
    }

    /* ======================================================================
     * 7. COURSE SCHEDULE  (LC 207 / 210) — the canonical topo-sort interview Q
     * ====================================================================== */
    static boolean canFinish(int numCourses, int[][] prerequisites) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) adj.add(new ArrayList<>());
        for (int[] p : prerequisites) adj.get(p[1]).add(p[0]);   // p[1] BEFORE p[0]
        return topoSortKahn(numCourses, adj).length == numCourses;
    }

    static int[] findOrder(int numCourses, int[][] prerequisites) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) adj.add(new ArrayList<>());
        for (int[] p : prerequisites) adj.get(p[1]).add(p[0]);
        return topoSortKahn(numCourses, adj);
    }

    /* ======================================================================
     * 8. NETWORK DELAY TIME  (LC 743) — Dijkstra, answer = max finalised distance
     * ====================================================================== */
    @SuppressWarnings("unchecked")
    static int networkDelayTime(int[][] times, int n, int k) {
        List<int[]>[] adj = new List[n + 1];
        for (int i = 0; i <= n; i++) adj[i] = new ArrayList<>();
        for (int[] t : times) adj[t[0]].add(new int[]{t[1], t[2]});

        int[] dist = dijkstra(n + 1, adj, k);
        int max = 0;
        for (int i = 1; i <= n; i++) {
            if (dist[i] == Integer.MAX_VALUE) return -1;   // unreachable node
            max = Math.max(max, dist[i]);
        }
        return max;
    }

    /* helper: build an adjacency list of {to, weight} */
    @SuppressWarnings("unchecked")
    static List<int[]>[] buildAdj(int n, int[][] edges, boolean undirected) {
        List<int[]>[] adj = new List[n];
        for (int i = 0; i < n; i++) adj[i] = new ArrayList<>();
        for (int[] e : edges) {
            adj[e[0]].add(new int[]{e[1], e[2]});
            if (undirected) adj[e[1]].add(new int[]{e[0], e[2]});
        }
        return adj;
    }

    /* ================================================================== */
    public static void main(String[] args) {
        int n = 4;
        int[][] edges = {{0,1,4}, {0,2,1}, {2,1,2}, {1,3,1}, {2,3,5}};
        List<int[]>[] adj = buildAdj(n, edges, false);

        System.out.println("--- Dijkstra from 0 ---");
        System.out.println("dist = " + Arrays.toString(dijkstra(n, adj, 0)));
        System.out.println("path 0->3 = " + dijkstraPath(n, adj, 0, 3) + "  (cost 4, not 5)");

        System.out.println("\n--- Bellman-Ford (with a negative edge) ---");
        int[][] negEdges = {{0,1,4}, {0,2,1}, {2,1,-3}, {1,3,1}};
        System.out.println("dist = " + Arrays.toString(bellmanFord(4, negEdges, 0)));

        System.out.println("\n--- cheapest flight within k stops ---");
        int[][] flights = {{0,1,100},{1,2,100},{0,2,500}};
        System.out.println("k=1 -> " + cheapestKStops(3, flights, 0, 2, 1));
        System.out.println("k=0 -> " + cheapestKStops(3, flights, 0, 2, 0));

        System.out.println("\n--- Floyd-Warshall ---");
        int INF = Integer.MAX_VALUE;
        int[][] w = {
            {0,   4,   1,   INF},
            {INF, 0,   INF, 1  },
            {INF, 2,   0,   5  },
            {INF, INF, INF, 0  }
        };
        int[][] apsp = floydWarshall(w);
        for (int[] row : apsp)
            System.out.println("  " + Arrays.toString(
                    Arrays.stream(row).map(x -> x == INF ? -1 : x).toArray()));

        System.out.println("\n--- Topological sort ---");
        List<List<Integer>> dag = List.of(
                List.of(1, 2),    // 0 -> 1, 0 -> 2
                List.of(3),       // 1 -> 3
                List.of(3),       // 2 -> 3
                List.of());
        System.out.println("Kahn = " + Arrays.toString(topoSortKahn(4, dag)));
        System.out.println("DFS  = " + Arrays.toString(topoSortDFS(4, dag)));

        System.out.println("\n--- Course Schedule ---");
        System.out.println("canFinish(2, [[1,0]])       = " + canFinish(2, new int[][]{{1,0}}));
        System.out.println("canFinish(2, [[1,0],[0,1]]) = " +
                canFinish(2, new int[][]{{1,0},{0,1}}) + "  (cycle)");
        System.out.println("findOrder(4, ...)           = " +
                Arrays.toString(findOrder(4, new int[][]{{1,0},{2,0},{3,1},{3,2}})));

        System.out.println("\n--- MST ---");
        int[][] undirected = {{0,1,4},{0,2,1},{1,2,2},{1,3,1},{2,3,5}};
        System.out.println("Kruskal total = " + kruskalMST(4, cloneEdges(undirected)));
        System.out.println("Prim    total = " + primMST(4, buildAdj(4, undirected, true)));

        System.out.println("\n--- 0-1 BFS ---");
        int[][] zo = {{0,1,0},{1,2,1},{0,2,1},{2,3,0}};
        System.out.println("dist = " + Arrays.toString(zeroOneBFS(4, buildAdj(4, zo, false), 0)));

        System.out.println("\n--- Network Delay Time ---");
        System.out.println(networkDelayTime(new int[][]{{2,1,1},{2,3,1},{3,4,1}}, 4, 2));
    }

    private static int[][] cloneEdges(int[][] e) {
        int[][] c = new int[e.length][];
        for (int i = 0; i < e.length; i++) c[i] = e[i].clone();
        return c;
    }
}

/* ==========================================================================
 * PRACTICE — BLIND 75 / NEETCODE (graph algorithms)
 * ==========================================================================
 *   LC 207  Course Schedule                       med    Kahn / DFS cycle
 *   LC 210  Course Schedule II                    med    topological order
 *   LC 269  Alien Dictionary                      hard   build graph + topo
 *   LC 743  Network Delay Time                    med    Dijkstra
 *   LC 787  Cheapest Flights Within K Stops       med    Bellman-Ford + snapshot
 *   LC 1631 Path With Minimum Effort              med    Dijkstra on a grid (minimax)
 *   LC 778  Swim in Rising Water                  hard   Dijkstra / binary search + BFS
 *   LC 1584 Min Cost to Connect All Points        med    Prim MST
 *   LC 1489 Critical Connections / MST edges      hard   Kruskal variants
 *   LC 684  Redundant Connection                  med    Union-Find
 *   LC 261  Graph Valid Tree                      med    UF: n-1 edges + connected
 *   LC 323  Number of Connected Components        med    UF
 *   LC 332  Reconstruct Itinerary                 hard   Hierholzer (Eulerian path)
 *   LC 1091 Shortest Path in Binary Matrix        med    BFS with 8 directions
 *   LC 127  Word Ladder                           hard   BFS on an implicit graph
 *   LC 399  Evaluate Division                     med    weighted graph DFS / DSU
 *
 * SELF-TEST QUESTIONS
 *   - Why does Dijkstra break with negative weights? Give a 3-node example.
 *   - Why V-1 rounds in Bellman-Ford, and what does a V-th improving round mean?
 *   - In LC 787, why must you snapshot dist before the round?
 *   - Kahn's: how do you detect a cycle without any extra bookkeeping?
 *   - Why must k be the OUTER loop in Floyd-Warshall?
 *   - Kruskal vs Prim: which fits an edge list, which fits an adjacency list?
 *   - What's the "stale entry" check in lazy Dijkstra and why is it needed?
 *   - When is 0-1 BFS strictly better than Dijkstra?
 * ========================================================================== */
