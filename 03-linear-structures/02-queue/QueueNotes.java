import java.util.*;

/* ==========================================================================
 * DS: QUEUE  (FIFO - First In, First Out)  + Deque + Circular Queue
 * Run: java QueueNotes.java
 * See ./README.md for the mental model, complexity, and when to reach
 * for a queue — not repeated here.
 * ========================================================================== */
public class QueueNotes {

    /* ======================================================================
     * 1. IMPLEMENTATION A — linked list backed
     * ====================================================================== */
    static class LinkedQueue<T> {
        private static class Node<T> {
            T data; Node<T> next;
            Node(T d) { data = d; }
        }

        private Node<T> front, rear;
        private int size;

        void enqueue(T val) {
            Node<T> n = new Node<>(val);
            if (rear == null) {           // empty queue: front and rear both point here
                front = rear = n;
            } else {
                rear.next = n;            // link old rear to new node
                rear = n;                 // new node is the rear
            }
            size++;
        }

        T dequeue() {
            if (front == null) throw new NoSuchElementException("queue empty");
            T out = front.data;
            front = front.next;
            if (front == null) rear = null;   // CRITICAL: queue became empty, reset rear
            size--;
            return out;
        }

        T peek() {
            if (front == null) throw new NoSuchElementException("queue empty");
            return front.data;
        }
        boolean isEmpty() { return size == 0; }
        int size() { return size; }
    }

    /* ======================================================================
     * 2. IMPLEMENTATION B — circular array (fixed capacity). LC 622.
     * ====================================================================== */
    static class CircularQueue<T> {
        private final T[] data;
        private int front = 0, rear = -1, size = 0;
        private final int cap;

        @SuppressWarnings("unchecked")
        CircularQueue(int cap) { this.cap = cap; this.data = (T[]) new Object[cap]; }

        boolean enqueue(T val) {
            if (isFull()) return false;
            rear = (rear + 1) % cap;      // wrap
            data[rear] = val;
            size++;
            return true;
        }

        T dequeue() {
            if (isEmpty()) throw new NoSuchElementException("queue empty");
            T out = data[front];
            data[front] = null;           // help GC
            front = (front + 1) % cap;    // wrap
            size--;
            return out;
        }

        T peek()         { if (isEmpty()) throw new NoSuchElementException(); return data[front]; }
        boolean isFull() { return size == cap; }
        boolean isEmpty(){ return size == 0; }
        int size()       { return size; }
    }

    /* ======================================================================
     * 3. CLASSIC: QUEUE FROM TWO STACKS  (LC 232) — amortized O(1)
     *
     *   in:  push here
     *   out: pop here; when empty, DUMP all of `in` into `out` (reverses order)
     *
     *        in [3,2,1]  --dump-->  out [1,2,3]
     *                                    ^ pop gives 1 = FIFO
     *
     *   Each element is moved at most twice -> amortized O(1).
     *   NEVER dump unless `out` is empty, or you scramble the order.
     * ====================================================================== */
    static class QueueFromStacks {
        private final Deque<Integer> in = new ArrayDeque<>();
        private final Deque<Integer> out = new ArrayDeque<>();

        void push(int x) { in.push(x); }

        int pop() { shift(); return out.pop(); }
        int peek() { shift(); return out.peek(); }
        boolean empty() { return in.isEmpty() && out.isEmpty(); }

        private void shift() {
            if (out.isEmpty())                       // ONLY when out is drained
                while (!in.isEmpty()) out.push(in.pop());
        }
    }

    /* ======================================================================
     * 4. JAVA API — ArrayDeque as Queue/Deque, demonstrated live in main()
     * below. Full offer/poll/peek + addFirst/addLast syntax reference lives
     * in ../../java-api-examples.md.
     * ====================================================================== */

    /* ======================================================================
     * 5. PATTERN: BFS LEVEL-ORDER — the single most reused queue template
     * The `int levelSize = q.size()` snapshot is what separates levels.
     * ====================================================================== */
    static class TreeNode {
        int val; TreeNode left, right;
        TreeNode(int v) { val = v; }
    }

    static List<List<Integer>> levelOrder(TreeNode root) {
        List<List<Integer>> res = new ArrayList<>();
        if (root == null) return res;

        Queue<TreeNode> q = new ArrayDeque<>();
        q.offer(root);

        while (!q.isEmpty()) {
            int levelSize = q.size();            // freeze count BEFORE adding children
            List<Integer> level = new ArrayList<>(levelSize);
            for (int i = 0; i < levelSize; i++) {
                TreeNode n = q.poll();
                level.add(n.val);
                if (n.left  != null) q.offer(n.left);
                if (n.right != null) q.offer(n.right);
            }
            res.add(level);
        }
        return res;
    }

    /* ======================================================================
     * 6. PATTERN: BFS ON A GRID (shortest path, unweighted) — LC 994 / 200 / 1091
     * Template: seed queue, mark visited AT ENQUEUE TIME (not dequeue), 4 dirs.
     * ====================================================================== */
    static final int[][] DIRS = {{1,0},{-1,0},{0,1},{0,-1}};

    static int shortestPathBinaryGrid(int[][] grid) {
        int rows = grid.length, cols = grid[0].length;
        if (grid[0][0] == 1 || grid[rows-1][cols-1] == 1) return -1;

        Queue<int[]> q = new ArrayDeque<>();
        boolean[][] seen = new boolean[rows][cols];
        q.offer(new int[]{0, 0});
        seen[0][0] = true;
        int steps = 0;

        while (!q.isEmpty()) {
            int sz = q.size();
            for (int i = 0; i < sz; i++) {
                int[] cur = q.poll();
                if (cur[0] == rows - 1 && cur[1] == cols - 1) return steps;
                for (int[] d : DIRS) {
                    int nr = cur[0] + d[0], nc = cur[1] + d[1];
                    if (nr < 0 || nc < 0 || nr >= rows || nc >= cols) continue;
                    if (seen[nr][nc] || grid[nr][nc] == 1) continue;
                    seen[nr][nc] = true;         // mark on ENQUEUE -> no duplicates in queue
                    q.offer(new int[]{nr, nc});
                }
            }
            steps++;
        }
        return -1;
    }

    /* ======================================================================
     * 7. PATTERN: MONOTONIC DEQUE — sliding window maximum  (LC 239)
     *
     * Deque holds INDICES with DECREASING values. Front is always the window max.
     *   - pop from FRONT when the index falls out of the window
     *   - pop from BACK while the incoming value is >= back's value (they can never win)
     * Each index enters and leaves once -> O(n).
     *
     *   nums=[1,3,-1,-3,5,3,6,7] k=3
     *   window [1,3,-1] -> deque idx[1(=3), 2(=-1)] -> max = 3
     * ====================================================================== */
    static int[] maxSlidingWindow(int[] nums, int k) {
        int n = nums.length;
        int[] res = new int[n - k + 1];
        Deque<Integer> dq = new ArrayDeque<>();   // indices, values decreasing

        for (int i = 0; i < n; i++) {
            // 1. drop indices that slid out of the window
            while (!dq.isEmpty() && dq.peekFirst() <= i - k) dq.pollFirst();
            // 2. drop smaller values from the back - they're dominated by nums[i]
            while (!dq.isEmpty() && nums[dq.peekLast()] <= nums[i]) dq.pollLast();
            dq.offerLast(i);
            // 3. once the first full window exists, record the front
            if (i >= k - 1) res[i - k + 1] = nums[dq.peekFirst()];
        }
        return res;
    }

    /* ================================================================== */
    public static void main(String[] args) {
        System.out.println("--- custom LinkedQueue ---");
        LinkedQueue<Integer> lq = new LinkedQueue<>();
        lq.enqueue(1); lq.enqueue(2); lq.enqueue(3);
        System.out.println("dequeue=" + lq.dequeue() + " peek=" + lq.peek() + " size=" + lq.size());

        System.out.println("\n--- CircularQueue(cap=3) ---");
        CircularQueue<String> cq = new CircularQueue<>(3);
        cq.enqueue("a"); cq.enqueue("b"); cq.enqueue("c");
        System.out.println("isFull=" + cq.isFull() + " enqueue(d)=" + cq.enqueue("d"));
        System.out.println("dequeue=" + cq.dequeue() + " then enqueue(d)=" + cq.enqueue("d"));

        System.out.println("\n--- QueueFromStacks ---");
        QueueFromStacks qs = new QueueFromStacks();
        qs.push(1); qs.push(2); qs.push(3);
        System.out.println("pop=" + qs.pop() + " peek=" + qs.peek());

        System.out.println("\n--- java.util Deque as queue ---");
        Deque<Integer> dq = new ArrayDeque<>();
        dq.offerLast(1); dq.offerLast(2); dq.offerFirst(0);
        System.out.println("deque=" + dq + " pollFirst=" + dq.pollFirst() + " pollLast=" + dq.pollLast());

        System.out.println("\n--- BFS level order ---");
        TreeNode root = new TreeNode(3);
        root.left = new TreeNode(9);
        root.right = new TreeNode(20);
        root.right.left = new TreeNode(15);
        root.right.right = new TreeNode(7);
        System.out.println(levelOrder(root));

        System.out.println("\n--- BFS on grid (0=open,1=wall) ---");
        int[][] grid = {{0,0,0},{1,1,0},{0,0,0}};
        System.out.println("shortest steps = " + shortestPathBinaryGrid(grid));

        System.out.println("\n--- monotonic deque: sliding window max ---");
        System.out.println(Arrays.toString(
                maxSlidingWindow(new int[]{1,3,-1,-3,5,3,6,7}, 3)));
    }
}

/* ==========================================================================
 * PRACTICE — BLIND 75 / NEETCODE (queue & BFS)
 * ==========================================================================
 *   LC 232  Implement Queue using Stacks          easy   amortized O(1)
 *   LC 622  Design Circular Queue                 med    modulo arithmetic
 *   LC 102  Binary Tree Level Order Traversal     med    THE bfs template
 *   LC 199  Binary Tree Right Side View           med    last node per level
 *   LC 200  Number of Islands                     med    BFS or DFS flood fill
 *   LC 994  Rotting Oranges                       med    MULTI-SOURCE bfs
 *   LC 286  Walls and Gates                       med    multi-source bfs
 *   LC 133  Clone Graph                           med    BFS + map old->new
 *   LC 207  Course Schedule                       med    BFS topological (Kahn)
 *   LC 239  Sliding Window Maximum                hard   monotonic deque
 *   LC 127  Word Ladder                           hard   BFS on implicit graph
 *
 * Self-test questions + answers: see ./README.md
 * ========================================================================== */
