import java.util.*;

/* ==========================================================================
 * DS: GRAPH  (representations + traversals + union-find)
 * Algorithms (Dijkstra, MST, topo sort, etc.) live in ../02-graph-algorithms/GraphAlgos.java
 * Run: java GraphNotes.java
 * ==========================================================================
 *
 * VOCABULARY
 * ----------
 *   V, E            vertices, edges
 *   DIRECTED        edges have a direction (a -> b)
 *   UNDIRECTED      a -- b, stored as BOTH a->b and b->a
 *   WEIGHTED        edges carry a cost
 *   DAG             directed acyclic graph -> topological sort exists
 *   CONNECTED       every vertex reachable from every other (undirected)
 *   DENSE / SPARSE  E ~ V^2  /  E ~ V
 *   DEGREE          edges touching a vertex; in-degree / out-degree when directed
 *
 * THE SAMPLE GRAPH USED IN THIS FILE (undirected)
 * -----------------------------------------------
 *      0 --- 1
 *      |   / |
 *      |  /  |
 *      2 --- 3       4 --- 5      <- two components
 *
 *   adjacency list: 0:[1,2] 1:[0,2,3] 2:[0,1,3] 3:[1,2] 4:[5] 5:[4]
 *
 * REPRESENTATIONS
 * ┌──────────────────┬──────────────┬───────────────┬──────────────────────┐
 * │                  │ Space        │ hasEdge(u,v)  │ iterate neighbours   │
 * ├──────────────────┼──────────────┼───────────────┼──────────────────────┤
 * │ Adjacency LIST   │ O(V+E)       │ O(deg(u))     │ O(deg(u))  BEST      │
 * │ Adjacency MATRIX │ O(V^2)       │ O(1)          │ O(V)                 │
 * │ Edge LIST        │ O(E)         │ O(E)          │ O(E)                 │
 * └──────────────────┴──────────────┴───────────────┴──────────────────────┘
 *   Default to the ADJACENCY LIST. Matrix only for dense graphs or when the
 *   problem literally hands you a matrix (e.g. LC 547 provinces).
 *   Edge list is what Kruskal's MST and Bellman-Ford want.
 *
 * JAVA SHAPES YOU'LL WRITE
 *   Map<Integer, List<Integer>> g = new HashMap<>();            // sparse / labelled
 *   List<List<Integer>> g = new ArrayList<>();                  // vertices 0..n-1
 *   int[][] g = new int[n][n];                                  // matrix
 *   int[][] edges = {{u,v,w}, ...};                             // edge list
 *
 * BFS vs DFS — pick correctly, this is half the interview
 * ┌──────────────────────────────────┬───────┬───────┐
 * │                                  │ BFS   │ DFS   │
 * ├──────────────────────────────────┼───────┼───────┤
 * │ shortest path (UNWEIGHTED)       │ YES   │ no    │
 * │ level / distance information     │ YES   │ no    │
 * │ connected components / flood fill│ yes   │ yes   │
 * │ cycle detection                  │ yes   │ YES   │
 * │ topological sort                 │ Kahn  │ YES   │
 * │ path existence / reachability    │ yes   │ yes   │
 * │ backtracking, all paths          │ no    │ YES   │
 * │ memory                           │ O(w)  │ O(h)  │
 * └──────────────────────────────────┴───────┴───────┘
 *   w = max width of a level, h = max depth. For a wide shallow graph DFS uses
 *   less memory; for a deep narrow graph BFS does.
 *
 * THE #1 BUG: forgetting `visited`, or marking it at the wrong time.
 *   BFS -> mark visited when you ENQUEUE (else duplicates pile up in the queue)
 *   DFS -> mark visited when you ENTER the node
 * ========================================================================== */
public class GraphNotes {

    /* ======================================================================
     * 1. GENERIC ADJACENCY-LIST GRAPH
     * ====================================================================== */
    static class Graph<T> {
        private final Map<T, List<T>> adj = new LinkedHashMap<>();  // Linked = stable order
        private final boolean directed;

        Graph(boolean directed) { this.directed = directed; }

        void addVertex(T v) { adj.computeIfAbsent(v, k -> new ArrayList<>()); }

        void addEdge(T u, T v) {
            adj.computeIfAbsent(u, k -> new ArrayList<>()).add(v);
            adj.computeIfAbsent(v, k -> new ArrayList<>());   // make sure v exists
            if (!directed) adj.get(v).add(u);                 // undirected = both ways
        }

        List<T> neighbors(T v) { return adj.getOrDefault(v, List.of()); }
        Set<T> vertices()      { return adj.keySet(); }

        /* BFS from a source. Returns visit order. */
        List<T> bfs(T start) {
            List<T> order = new ArrayList<>();
            if (!adj.containsKey(start)) return order;
            Set<T> seen = new HashSet<>();
            Queue<T> q = new ArrayDeque<>();
            q.offer(start); seen.add(start);          // mark at ENQUEUE
            while (!q.isEmpty()) {
                T cur = q.poll();
                order.add(cur);
                for (T nb : neighbors(cur))
                    if (seen.add(nb)) q.offer(nb);    // Set.add returns false if already present
            }
            return order;
        }

        /* DFS, recursive. */
        List<T> dfs(T start) {
            List<T> order = new ArrayList<>();
            dfs(start, new HashSet<>(), order);
            return order;
        }
        private void dfs(T v, Set<T> seen, List<T> order) {
            if (!seen.add(v)) return;                 // mark on ENTER
            order.add(v);
            for (T nb : neighbors(v)) dfs(nb, seen, order);
        }

        /* DFS, iterative — when recursion depth is a risk (n up to 1e5+). */
        List<T> dfsIterative(T start) {
            List<T> order = new ArrayList<>();
            Set<T> seen = new HashSet<>();
            Deque<T> st = new ArrayDeque<>();
            st.push(start);
            while (!st.isEmpty()) {
                T cur = st.pop();
                if (!seen.add(cur)) continue;         // may be pushed multiple times
                order.add(cur);
                List<T> nbs = neighbors(cur);
                for (int i = nbs.size() - 1; i >= 0; i--) {   // reverse -> same order as recursive
                    T nb = nbs.get(i);
                    if (!seen.contains(nb)) st.push(nb);
                }
            }
            return order;
        }

        /* Shortest path in an UNWEIGHTED graph = BFS + parent map. */
        List<T> shortestPath(T from, T to) {
            Map<T, T> parent = new HashMap<>();
            Set<T> seen = new HashSet<>();
            Queue<T> q = new ArrayDeque<>();
            q.offer(from); seen.add(from); parent.put(from, null);

            while (!q.isEmpty()) {
                T cur = q.poll();
                if (cur.equals(to)) break;
                for (T nb : neighbors(cur))
                    if (seen.add(nb)) { parent.put(nb, cur); q.offer(nb); }
            }
            if (!parent.containsKey(to)) return List.of();     // unreachable

            LinkedList<T> path = new LinkedList<>();
            for (T at = to; at != null; at = parent.get(at)) path.addFirst(at);  // walk back
            return path;
        }

        int countComponents() {                       // undirected
            Set<T> seen = new HashSet<>();
            int c = 0;
            for (T v : adj.keySet())
                if (!seen.contains(v)) { c++; dfs(v, seen, new ArrayList<>()); }
            return c;
        }

        @Override public String toString() { return adj.toString(); }
    }

    /* ======================================================================
     * 2. UNION-FIND / DISJOINT SET UNION (DSU)
     *
     * Answers "are u and v in the same group?" and "merge these two groups" in
     * ~O(1) amortized (inverse Ackermann). The go-to for:
     *   - connected components on a stream of edges
     *   - cycle detection in an UNDIRECTED graph
     *   - Kruskal's MST
     *   - "accounts merge", "redundant connection", "number of provinces"
     *
     * TWO OPTIMISATIONS, both required:
     *   PATH COMPRESSION  - flatten the tree during find()
     *   UNION BY RANK/SIZE- always hang the smaller tree under the bigger one
     *
     *   union(1,2), union(3,4), union(2,3):
     *       1        1          1
     *       |   ->   |    ->   / \
     *       2        2        2   3
     *                              \
     *                               4
     * ====================================================================== */
    static class UnionFind {
        private final int[] parent;
        private final int[] size;
        private int components;

        UnionFind(int n) {
            parent = new int[n];
            size = new int[n];
            components = n;
            for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }
        }

        int find(int x) {
            while (parent[x] != x) {
                parent[x] = parent[parent[x]];   // path halving - compresses as we go
                x = parent[x];
            }
            return x;
        }

        /* returns false if u and v were ALREADY connected -> that edge forms a CYCLE */
        boolean union(int u, int v) {
            int ru = find(u), rv = find(v);
            if (ru == rv) return false;
            if (size[ru] < size[rv]) { int t = ru; ru = rv; rv = t; }  // attach small->big
            parent[rv] = ru;
            size[ru] += size[rv];
            components--;
            return true;
        }

        boolean connected(int u, int v) { return find(u) == find(v); }
        int componentCount() { return components; }
        int sizeOf(int x)    { return size[find(x)]; }
    }

    /* ======================================================================
     * 3. GRID AS A GRAPH — most "graph" interview questions are secretly grids
     * Every cell is a vertex; neighbours are the 4 (or 8) adjacent cells.
     * ====================================================================== */
    static final int[][] DIRS4 = {{1,0},{-1,0},{0,1},{0,-1}};
    static final int[][] DIRS8 = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};

    /* LC 200 — number of islands. DFS flood fill, mutating the grid as "visited". */
    static int numIslands(char[][] grid) {
        if (grid.length == 0) return 0;
        int count = 0;
        for (int r = 0; r < grid.length; r++)
            for (int c = 0; c < grid[0].length; c++)
                if (grid[r][c] == '1') { count++; sink(grid, r, c); }
        return count;
    }
    private static void sink(char[][] g, int r, int c) {
        if (r < 0 || c < 0 || r >= g.length || c >= g[0].length || g[r][c] != '1') return;
        g[r][c] = '0';                                  // mark visited in place
        for (int[] d : DIRS4) sink(g, r + d[0], c + d[1]);
    }

    /* LC 994 — MULTI-SOURCE BFS. Seed EVERY source at level 0, then expand once.
     * This is the trick people miss: don't run BFS per source, run one BFS with
     * all sources already in the queue. */
    static int orangesRotting(int[][] grid) {
        int rows = grid.length, cols = grid[0].length, fresh = 0;
        Queue<int[]> q = new ArrayDeque<>();
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == 2) q.offer(new int[]{r, c});   // ALL sources
                else if (grid[r][c] == 1) fresh++;
            }
        if (fresh == 0) return 0;

        int minutes = 0;
        while (!q.isEmpty() && fresh > 0) {
            int sz = q.size();
            for (int i = 0; i < sz; i++) {
                int[] cur = q.poll();
                for (int[] d : DIRS4) {
                    int nr = cur[0] + d[0], nc = cur[1] + d[1];
                    if (nr < 0 || nc < 0 || nr >= rows || nc >= cols) continue;
                    if (grid[nr][nc] != 1) continue;
                    grid[nr][nc] = 2;
                    fresh--;
                    q.offer(new int[]{nr, nc});
                }
            }
            minutes++;
        }
        return fresh == 0 ? minutes : -1;
    }

    /* ======================================================================
     * 4. CYCLE DETECTION
     * ====================================================================== */

    /* UNDIRECTED: a cycle exists if DFS reaches an already-visited node that is
     * NOT the node we came from. (Or just: UnionFind.union returns false.) */
    static boolean hasCycleUndirected(int n, int[][] edges) {
        UnionFind uf = new UnionFind(n);
        for (int[] e : edges) if (!uf.union(e[0], e[1])) return true;
        return false;
    }

    /* DIRECTED: needs THREE colours, not two.
     *   0 = unvisited, 1 = in the current recursion stack, 2 = fully done
     * Hitting a node coloured 1 means a BACK EDGE = cycle.
     * Hitting a node coloured 2 is fine - it's a cross edge to finished work.
     * This distinction is the whole question. */
    static boolean hasCycleDirected(int n, List<List<Integer>> adj) {
        int[] state = new int[n];
        for (int i = 0; i < n; i++)
            if (state[i] == 0 && dfsCycle(i, adj, state)) return true;
        return false;
    }
    private static boolean dfsCycle(int v, List<List<Integer>> adj, int[] state) {
        state[v] = 1;                                   // entering
        for (int nb : adj.get(v)) {
            if (state[nb] == 1) return true;            // back edge -> cycle
            if (state[nb] == 0 && dfsCycle(nb, adj, state)) return true;
        }
        state[v] = 2;                                   // fully explored
        return false;
    }

    /* ======================================================================
     * 5. CLONE GRAPH  (LC 133) — deep copy via map old -> new
     * The map doubles as the visited set. Put the clone in the map BEFORE
     * recursing, or a cycle sends you into infinite recursion.
     * ====================================================================== */
    static class GNode {
        int val; List<GNode> neighbors = new ArrayList<>();
        GNode(int v) { val = v; }
    }
    static GNode cloneGraph(GNode node) { return clone(node, new HashMap<>()); }
    private static GNode clone(GNode n, Map<GNode, GNode> map) {
        if (n == null) return null;
        GNode existing = map.get(n);
        if (existing != null) return existing;
        GNode copy = new GNode(n.val);
        map.put(n, copy);                                // BEFORE recursing
        for (GNode nb : n.neighbors) copy.neighbors.add(clone(nb, map));
        return copy;
    }

    /* ======================================================================
     * 6. BIPARTITE CHECK  (LC 785) — 2-colour with BFS
     * A graph is bipartite iff it has no odd-length cycle.
     * ====================================================================== */
    static boolean isBipartite(int[][] graph) {
        int n = graph.length;
        int[] color = new int[n];          // 0 = uncoloured, 1 / -1 = the two sides
        for (int start = 0; start < n; start++) {
            if (color[start] != 0) continue;
            Queue<Integer> q = new ArrayDeque<>();
            q.offer(start); color[start] = 1;
            while (!q.isEmpty()) {
                int cur = q.poll();
                for (int nb : graph[cur]) {
                    if (color[nb] == color[cur]) return false;    // same side -> not bipartite
                    if (color[nb] == 0) { color[nb] = -color[cur]; q.offer(nb); }
                }
            }
        }
        return true;
    }

    /* ================================================================== */
    public static void main(String[] args) {
        System.out.println("--- generic Graph ---");
        Graph<Integer> g = new Graph<>(false);
        g.addEdge(0,1); g.addEdge(0,2); g.addEdge(1,2); g.addEdge(1,3); g.addEdge(2,3);
        g.addEdge(4,5);
        System.out.println("adj = " + g);
        System.out.println("bfs(0)          = " + g.bfs(0));
        System.out.println("dfs(0)          = " + g.dfs(0));
        System.out.println("dfsIterative(0) = " + g.dfsIterative(0));
        System.out.println("shortestPath 0->3 = " + g.shortestPath(0, 3));
        System.out.println("shortestPath 0->5 = " + g.shortestPath(0, 5) + "  (disconnected)");
        System.out.println("components      = " + g.countComponents());

        System.out.println("\n--- UnionFind ---");
        UnionFind uf = new UnionFind(6);
        uf.union(0,1); uf.union(1,2); uf.union(2,3); uf.union(4,5);
        System.out.println("connected(0,3)=" + uf.connected(0,3)
                + " connected(0,5)=" + uf.connected(0,5)
                + " components=" + uf.componentCount()
                + " sizeOf(0)=" + uf.sizeOf(0));
        System.out.println("union(0,3) again returns " + uf.union(0,3) + " -> that edge is a cycle");

        System.out.println("\n--- grids ---");
        char[][] islands = {
            {'1','1','0','0','0'},
            {'1','1','0','0','0'},
            {'0','0','1','0','0'},
            {'0','0','0','1','1'}
        };
        System.out.println("numIslands = " + numIslands(islands));

        int[][] oranges = {{2,1,1},{1,1,0},{0,1,1}};
        System.out.println("orangesRotting (multi-source BFS) = " + orangesRotting(oranges));

        System.out.println("\n--- cycles ---");
        System.out.println("undirected 0-1,1-2,2-0 has cycle = " +
                hasCycleUndirected(3, new int[][]{{0,1},{1,2},{2,0}}));
        System.out.println("undirected 0-1,1-2 has cycle     = " +
                hasCycleUndirected(3, new int[][]{{0,1},{1,2}}));

        List<List<Integer>> dag = List.of(List.of(1), List.of(2), List.of());
        List<List<Integer>> cyc = List.of(List.of(1), List.of(2), List.of(0));
        System.out.println("directed DAG has cycle    = " + hasCycleDirected(3, dag));
        System.out.println("directed 0->1->2->0 cycle = " + hasCycleDirected(3, cyc));

        System.out.println("\n--- clone graph ---");
        GNode a = new GNode(1), b = new GNode(2);
        a.neighbors.add(b); b.neighbors.add(a);       // 2-cycle
        GNode copy = cloneGraph(a);
        System.out.println("copy.val=" + copy.val
                + " copy!=a? " + (copy != a)
                + " copy.nb[0].nb[0]==copy? " + (copy.neighbors.get(0).neighbors.get(0) == copy));

        System.out.println("\n--- bipartite ---");
        System.out.println("square 0-1-2-3-0 = " +
                isBipartite(new int[][]{{1,3},{0,2},{1,3},{0,2}}));
        System.out.println("triangle         = " +
                isBipartite(new int[][]{{1,2},{0,2},{0,1}}));
    }
}

/* ==========================================================================
 * PRACTICE — BLIND 75 / NEETCODE (graphs)
 * ==========================================================================
 *   LC 200  Number of Islands                     med    flood fill
 *   LC 133  Clone Graph                           med    map old->new
 *   LC 695  Max Area of Island                    med    DFS returning a size
 *   LC 417  Pacific Atlantic Water Flow           med    reverse DFS from borders
 *   LC 130  Surrounded Regions                    med    mark from the border first
 *   LC 994  Rotting Oranges                       med    multi-source BFS
 *   LC 286  Walls and Gates                       med    multi-source BFS
 *   LC 207  Course Schedule                       med    directed cycle / Kahn
 *   LC 210  Course Schedule II                    med    topological order
 *   LC 261  Graph Valid Tree                      med    n-1 edges + connected (UF)
 *   LC 323  Number of Connected Components        med    UnionFind
 *   LC 684  Redundant Connection                  med    UF, first union that fails
 *   LC 547  Number of Provinces                   med    matrix + UF
 *   LC 785  Is Graph Bipartite                    med    2-colouring
 *   LC 127  Word Ladder                           hard   BFS on an implicit graph
 *   LC 269  Alien Dictionary                      hard   build graph + topo sort
 *   LC 269/332/778/1584 -> see ../02-graph-algorithms/GraphAlgos.java for Dijkstra/MST/topo
 *
 * SELF-TEST QUESTIONS
 *   - Why mark visited on ENQUEUE in BFS rather than on dequeue?
 *   - Directed cycle detection needs 3 states. What does each mean, and what
 *     goes wrong with only 2?
 *   - When is BFS strictly better than DFS, and vice versa?
 *   - What do path compression and union-by-size each buy you in DSU?
 *   - In cloneGraph, why put the clone in the map before recursing?
 *   - How do you turn a grid problem into a graph problem in one sentence?
 *   - Graph Valid Tree: what two conditions must hold?
 * ========================================================================== */
