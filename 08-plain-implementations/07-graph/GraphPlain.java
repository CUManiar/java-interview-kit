import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/* Graph basics, plain int vertices 0..n-1, no <T>. Run: java GraphPlain.java
 * For the mental model (BFS vs DFS table, 3-state cycle detection, DSU theory),
 * see ../../05-graphs/01-graph-basics/README.md -- this file is purely "watch it work."
 * Scope: representations + traversal + union-find. No weighted shortest-path here. */
public class GraphPlain {

    /* ======================================================================
     * 1. ADJACENCY LIST -- List<Integer>[] adj, undirected (addEdge stores
     * both directions). O(V+E) space: total list entries across all buckets
     * is exactly 2*E (one per direction), so a SPARSE graph (E close to V)
     * costs barely more than the vertex count itself. This is why adjacency
     * list is the default representation for almost every graph problem.
     * ====================================================================== */
    static class AdjListGraph {
        int n;
        List<Integer>[] adj;

        @SuppressWarnings("unchecked")
        AdjListGraph(int n) {
            this.n = n;
            adj = new List[n];
            for (int i = 0; i < n; i++) adj[i] = new ArrayList<>();
        }

        void addEdge(int u, int v) {
            adj[u].add(v);
            adj[v].add(u); // undirected: record both directions
        }

        boolean hasEdge(int u, int v) {
            return adj[u].contains(v); // O(degree(u)) -- must scan u's neighbor list
        }
    }

    /* ======================================================================
     * 2. ADJACENCY MATRIX -- boolean[n][n], SAME graph as above so the
     * tradeoff is concrete. matrix[u][v] is O(1) edge lookup, but the
     * n*n array is allocated up front no matter how few edges exist --
     * O(V^2) space always, even for a graph with almost no edges. For a
     * sparse graph (like the one below) this wastes most of its cells on
     * "false".
     * ====================================================================== */
    static class AdjMatrixGraph {
        int n;
        boolean[][] matrix;

        AdjMatrixGraph(int n) {
            this.n = n;
            matrix = new boolean[n][n]; // O(V^2) cells reserved regardless of edge count
        }

        void addEdge(int u, int v) {
            matrix[u][v] = true;
            matrix[v][u] = true; // undirected: mirror across the diagonal
        }

        boolean hasEdge(int u, int v) {
            return matrix[u][v]; // O(1) -- direct array lookup, no scanning
        }
    }

    /* ======================================================================
     * 3. BFS -- queue + visited[]. Visited is marked at ENQUEUE time, not at
     * dequeue. If marking were deferred to dequeue, a node could be pushed
     * onto the queue once for every already-queued neighbor that discovers
     * it before it's ever processed -- the same node piling up multiple
     * times in the queue before its first (and only correct) visit. Marking
     * at enqueue guarantees each node enters the queue exactly once.
     * ====================================================================== */
    static List<Integer> bfs(AdjListGraph g, int start) {
        List<Integer> order = new ArrayList<>();
        boolean[] visited = new boolean[g.n];
        ArrayDeque<Integer> queue = new ArrayDeque<>();

        queue.add(start);
        visited[start] = true; // mark HERE (enqueue time), not when it's dequeued below

        while (!queue.isEmpty()) {
            int u = queue.poll();
            order.add(u);
            for (int v : g.adj[u]) {
                if (!visited[v]) {
                    visited[v] = true; // again: enqueue time
                    queue.add(v);
                }
            }
        }
        return order;
    }

    /* ======================================================================
     * 4. DFS -- recursive and iterative-with-explicit-stack. Both are valid
     * DFS orders but can differ from EACH OTHER (recursion explores neighbors
     * in list order; the explicit-stack version pushes them all first, so it
     * pops and explores the LAST-added neighbor first -- effectively reversed
     * order at each branch) and both can differ from BFS order. None of this
     * is a bug: BFS/DFS/DFS-iterative are three different valid visiting
     * orders of the same graph.
     * ====================================================================== */
    static List<Integer> dfsRecursive(AdjListGraph g, int start) {
        List<Integer> order = new ArrayList<>();
        boolean[] visited = new boolean[g.n];
        dfsRecursiveHelper(g, start, visited, order);
        return order;
    }

    private static void dfsRecursiveHelper(AdjListGraph g, int u, boolean[] visited, List<Integer> order) {
        visited[u] = true; // mark on ENTER, mirroring BFS's "mark before it can be re-queued"
        order.add(u);
        for (int v : g.adj[u]) {
            if (!visited[v]) dfsRecursiveHelper(g, v, visited, order);
        }
    }

    static List<Integer> dfsIterative(AdjListGraph g, int start) {
        List<Integer> order = new ArrayList<>();
        boolean[] visited = new boolean[g.n];
        ArrayDeque<Integer> stack = new ArrayDeque<>();

        stack.push(start);
        while (!stack.isEmpty()) {
            int u = stack.pop();
            if (visited[u]) continue; // a node can be pushed more than once before it's ever popped
            visited[u] = true;
            order.add(u);
            for (int v : g.adj[u]) {
                if (!visited[v]) stack.push(v); // pushed in list order -> popped in REVERSE order
            }
        }
        return order;
    }

    /* ======================================================================
     * 5. UNION-FIND (DSU) -- path compression in find(), union-by-size in
     * union(). Used here for cycle detection over an undirected edge list:
     * process edges one at a time; if find(u) == find(v) BEFORE unioning,
     * u and v were already connected by some other path, so this edge
     * closes a cycle instead of joining two new components.
     *
     * Path compression alone still gives good amortized performance (each
     * find() flattens part of the path it walks, so trees stay shallow over
     * time even without union-by-size). But union-by-size alone ALSO helps
     * (always hangs the smaller tree under the bigger root, capping height
     * at O(log n) on its own). It's the COMBINATION of both that yields the
     * near-O(1) (inverse-Ackermann) amortized bound -- each optimization
     * attacks the same problem (tall trees) from a different angle, and
     * together they reinforce each other.
     * ====================================================================== */
    static class UnionFind {
        int[] parent;
        int[] size;

        UnionFind(int n) {
            parent = new int[n];
            size = new int[n];
            for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; } // everyone starts as their own root
        }

        int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]); // path compression: point directly at the root on the way back up
            }
            return parent[x];
        }

        // returns false if u and v were already in the same set (this edge would close a cycle)
        boolean union(int u, int v) {
            int ru = find(u), rv = find(v);
            if (ru == rv) return false; // already connected -- no-op, caller treats this as "cycle detected"
            if (size[ru] < size[rv]) { int tmp = ru; ru = rv; rv = tmp; } // ensure ru is the bigger (or equal) root
            parent[rv] = ru;   // hang the smaller tree under the bigger root
            size[ru] += size[rv];
            return true;
        }
    }

    public static void main(String[] args) {
        // Example graph: 7 vertices, two disconnected components, one isolated vertex.
        //   component A: 0-1, 0-2, 1-2, 1-3, 2-3   (a small cycle-containing cluster)
        //   component B: 4-5
        //   component C: 6                          (isolated, degree 0)
        int n = 7;
        int[][] edges = {{0, 1}, {0, 2}, {1, 2}, {1, 3}, {2, 3}, {4, 5}};

        System.out.println("=== 1. AdjListGraph ===");
        AdjListGraph listGraph = new AdjListGraph(n);
        for (int[] e : edges) listGraph.addEdge(e[0], e[1]);
        for (int v = 0; v < n; v++) System.out.println("  " + v + ": " + listGraph.adj[v]);
        System.out.println("hasEdge(1,3)=" + listGraph.hasEdge(1, 3) + "  hasEdge(0,3)=" + listGraph.hasEdge(0, 3)
                + "  (each check scans a neighbor list -- O(degree))");

        System.out.println("\n=== 2. AdjMatrixGraph (same graph) ===");
        AdjMatrixGraph matrixGraph = new AdjMatrixGraph(n);
        for (int[] e : edges) matrixGraph.addEdge(e[0], e[1]);
        int trueCells = 0;
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) if (matrixGraph.matrix[i][j]) trueCells++;
        System.out.println("matrix is " + n + "x" + n + " = " + (n * n) + " cells allocated, only " + trueCells
                + " are true (" + edges.length + " edges x 2 directions) -- most of the matrix is wasted space here");
        System.out.println("hasEdge(1,3)=" + matrixGraph.hasEdge(1, 3) + "  hasEdge(0,3)=" + matrixGraph.hasEdge(0, 3)
                + "  (each check is a single O(1) array read, no scanning)");

        System.out.println("\n=== 3. BFS from vertex 0 ===");
        List<Integer> bfsOrder = bfs(listGraph, 0);
        System.out.println("visit order: " + bfsOrder);
        System.out.println("vertices 4, 5, 6 are absent -- unreachable from 0 (separate components), proving BFS"
                + " only explores 0's connected component, not the whole graph");

        System.out.println("\n=== 4. DFS from vertex 0 -- recursive vs iterative ===");
        List<Integer> dfsRecOrder = dfsRecursive(listGraph, 0);
        List<Integer> dfsIterOrder = dfsIterative(listGraph, 0);
        System.out.println("recursive order:  " + dfsRecOrder);
        System.out.println("iterative order:  " + dfsIterOrder);
        System.out.println("BFS order was:    " + bfsOrder);
        System.out.println("all three can differ from each other -- every one is still a valid traversal order");

        System.out.println("\n=== 5. Union-Find cycle detection over an edge list ===");
        // Same 7 vertices. Edges 0-1, 1-2, 2-3 build a path (a tree, no cycle yet).
        // Edge 3-0 then reconnects two vertices already in the same set -- that's the cycle-closing edge.
        // Edges 4-5, 5-6 build a second, separate tree with no cycle.
        int[][] ufEdges = {{0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}};
        UnionFind uf = new UnionFind(n);
        for (int[] e : ufEdges) {
            int u = e[0], v = e[1];
            boolean joined = uf.union(u, v);
            if (joined) {
                System.out.println("  edge (" + u + "," + v + ") -> joined two separate components");
            } else {
                System.out.println("  edge (" + u + "," + v + ") -> CYCLE DETECTED (both already in the same set)");
            }
        }
    }
}
