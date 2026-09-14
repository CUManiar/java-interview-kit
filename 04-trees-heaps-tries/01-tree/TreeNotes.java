import java.util.*;

/* ==========================================================================
 * DS: BINARY TREE / BINARY SEARCH TREE
 * Run: java TreeNotes.java
 * See ./README.md for vocabulary, the sample tree, traversal orders,
 * complexity, and the top-down/bottom-up recursion template — not repeated here.
 * ========================================================================== */
public class TreeNotes {

    static class TreeNode {
        int val; TreeNode left, right;
        TreeNode(int v) { val = v; }
        TreeNode(int v, TreeNode l, TreeNode r) { val = v; left = l; right = r; }
    }

    /* ======================================================================
     * 1. BST IMPLEMENTATION (generic)
     * ====================================================================== */
    static class BST<T extends Comparable<T>> {
        private static class Node<T> {
            T val; Node<T> left, right;
            Node(T v) { val = v; }
        }

        private Node<T> root;
        private int size;

        /* Recursive insert: reassigning the returned subtree is the clean idiom.
         * "insert into this subtree and give me back its (possibly new) root." */
        void insert(T val) { root = insert(root, val); }
        private Node<T> insert(Node<T> node, T val) {
            if (node == null) { size++; return new Node<>(val); }
            int cmp = val.compareTo(node.val);
            if (cmp < 0)      node.left  = insert(node.left,  val);
            else if (cmp > 0) node.right = insert(node.right, val);
            // cmp == 0 -> duplicate, do nothing
            return node;
        }

        boolean contains(T val) {          // iterative: O(h) time, O(1) space
            Node<T> cur = root;
            while (cur != null) {
                int cmp = val.compareTo(cur.val);
                if (cmp == 0) return true;
                cur = (cmp < 0) ? cur.left : cur.right;
            }
            return false;
        }

        T min() { Node<T> c = root; if (c==null) return null;
                  while (c.left  != null) c = c.left;  return c.val; }
        T max() { Node<T> c = root; if (c==null) return null;
                  while (c.right != null) c = c.right; return c.val; }

        /* DELETE — the only genuinely fiddly BST operation. Three cases:
         *   0 children -> return null
         *   1 child    -> return that child
         *   2 children -> replace value with the INORDER SUCCESSOR (min of right
         *                 subtree), then delete that successor from the right subtree.
         *                 (The predecessor = max of left subtree works equally well.)
         *
         *       delete 3                 replace with 4 (min of right subtree)
         *           3                            4
         *          / \          =>              / \
         *         1   6                        1   6
         *            / \                            \
         *           4   7                            7
         */
        void delete(T val) { root = delete(root, val); }
        private Node<T> delete(Node<T> node, T val) {
            if (node == null) return null;
            int cmp = val.compareTo(node.val);
            if (cmp < 0)      node.left  = delete(node.left,  val);
            else if (cmp > 0) node.right = delete(node.right, val);
            else {
                size--;
                if (node.left  == null) return node.right;   // 0 or 1 child
                if (node.right == null) return node.left;    // 1 child
                Node<T> succ = node.right;                   // 2 children
                while (succ.left != null) succ = succ.left;  // inorder successor
                node.val = succ.val;
                size++;                                      // undo: the recursive call decrements
                node.right = delete(node.right, succ.val);
            }
            return node;
        }

        List<T> inorder() {                 // sorted output
            List<T> out = new ArrayList<>();
            inorder(root, out);
            return out;
        }
        private void inorder(Node<T> n, List<T> out) {
            if (n == null) return;
            inorder(n.left, out);
            out.add(n.val);
            inorder(n.right, out);
        }
        int size() { return size; }
    }

    /* ======================================================================
     * 2. TRAVERSALS — recursive
     * ====================================================================== */
    static void preorder(TreeNode n, List<Integer> out) {
        if (n == null) return;
        out.add(n.val);                 // node
        preorder(n.left, out);
        preorder(n.right, out);
    }
    static void inorder(TreeNode n, List<Integer> out) {
        if (n == null) return;
        inorder(n.left, out);
        out.add(n.val);                 // node
        inorder(n.right, out);
    }
    static void postorder(TreeNode n, List<Integer> out) {
        if (n == null) return;
        postorder(n.left, out);
        postorder(n.right, out);
        out.add(n.val);                 // node
    }

    /* ======================================================================
     * 3. TRAVERSALS — iterative (interviewers ask for these to test stack thinking)
     * ====================================================================== */
    static List<Integer> preorderIter(TreeNode root) {
        List<Integer> out = new ArrayList<>();
        if (root == null) return out;
        Deque<TreeNode> st = new ArrayDeque<>();
        st.push(root);
        while (!st.isEmpty()) {
            TreeNode n = st.pop();
            out.add(n.val);
            if (n.right != null) st.push(n.right);   // RIGHT first, so LEFT pops first
            if (n.left  != null) st.push(n.left);
        }
        return out;
    }

    /* Inorder iterative: go left as far as possible, pop, then go right once. */
    static List<Integer> inorderIter(TreeNode root) {
        List<Integer> out = new ArrayList<>();
        Deque<TreeNode> st = new ArrayDeque<>();
        TreeNode cur = root;
        while (cur != null || !st.isEmpty()) {
            while (cur != null) { st.push(cur); cur = cur.left; }   // dive left
            cur = st.pop();
            out.add(cur.val);
            cur = cur.right;                                        // one step right
        }
        return out;
    }

    /* Postorder trick: do preorder as (node,RIGHT,left) then reverse the list.
     * That yields left,right,node. Far easier than the two-stack/lastVisited version. */
    static List<Integer> postorderIter(TreeNode root) {
        LinkedList<Integer> out = new LinkedList<>();
        if (root == null) return out;
        Deque<TreeNode> st = new ArrayDeque<>();
        st.push(root);
        while (!st.isEmpty()) {
            TreeNode n = st.pop();
            out.addFirst(n.val);                      // prepend == reverse at the end
            if (n.left  != null) st.push(n.left);
            if (n.right != null) st.push(n.right);
        }
        return out;
    }

    /* BFS level order — the template from QueueNotes */
    static List<List<Integer>> levelOrder(TreeNode root) {
        List<List<Integer>> res = new ArrayList<>();
        if (root == null) return res;
        Queue<TreeNode> q = new ArrayDeque<>();
        q.offer(root);
        while (!q.isEmpty()) {
            int sz = q.size();                        // snapshot: separates levels
            List<Integer> level = new ArrayList<>(sz);
            for (int i = 0; i < sz; i++) {
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
     * 4. CORE PROBLEMS
     * ====================================================================== */

    /* LC 104 — max depth. The simplest bottom-up recursion. */
    static int maxDepth(TreeNode n) {
        return n == null ? 0 : 1 + Math.max(maxDepth(n.left), maxDepth(n.right));
    }

    /* LC 226 — invert. Swap children everywhere. */
    static TreeNode invert(TreeNode n) {
        if (n == null) return null;
        TreeNode t = n.left;
        n.left = invert(n.right);
        n.right = invert(t);
        return n;
    }

    /* LC 100 — same tree. Structural + value equality. */
    static boolean isSame(TreeNode a, TreeNode b) {
        if (a == null || b == null) return a == b;    // both null -> true
        return a.val == b.val && isSame(a.left, b.left) && isSame(a.right, b.right);
    }

    /* LC 543 — diameter. Longest path between ANY two nodes; may not pass the root.
     * Bottom-up: each node returns its HEIGHT, and on the way up we consider the
     * path that bends at this node = leftHeight + rightHeight. */
    static int diameterBest;
    static int diameter(TreeNode root) {
        diameterBest = 0;
        height(root);
        return diameterBest;
    }
    private static int height(TreeNode n) {
        if (n == null) return 0;
        int l = height(n.left), r = height(n.right);
        diameterBest = Math.max(diameterBest, l + r);   // path bending here, in EDGES
        return 1 + Math.max(l, r);
    }

    /* LC 110 — balanced. Return -1 as a sentinel meaning "already unbalanced",
     * so we short-circuit instead of recomputing heights = O(n) not O(n^2). */
    static boolean isBalanced(TreeNode root) { return balHeight(root) != -1; }
    private static int balHeight(TreeNode n) {
        if (n == null) return 0;
        int l = balHeight(n.left);  if (l == -1) return -1;
        int r = balHeight(n.right); if (r == -1) return -1;
        if (Math.abs(l - r) > 1) return -1;
        return 1 + Math.max(l, r);
    }

    /* LC 98 — VALIDATE BST. The classic wrong answer is only comparing with children.
     * You must carry down a (min, max) RANGE. Use Long to survive Integer.MIN/MAX values.
     *
     *        5
     *       / \        6 is > 5 but it's in the LEFT subtree -> INVALID.
     *      1   4       Checking node-vs-child alone misses this.
     *         / \
     *        3   6
     */
    static boolean isValidBST(TreeNode root) { return valid(root, Long.MIN_VALUE, Long.MAX_VALUE); }
    private static boolean valid(TreeNode n, long lo, long hi) {
        if (n == null) return true;
        if (n.val <= lo || n.val >= hi) return false;
        return valid(n.left, lo, n.val) && valid(n.right, n.val, hi);
    }

    /* LC 235 — LCA in a BST. O(h), no recursion needed.
     * If both values are smaller, go left. Both bigger, go right. Otherwise you
     * are standing on the split point = the LCA. */
    static TreeNode lcaBST(TreeNode root, int p, int q) {
        TreeNode cur = root;
        while (cur != null) {
            if (p < cur.val && q < cur.val)      cur = cur.left;
            else if (p > cur.val && q > cur.val) cur = cur.right;
            else return cur;
        }
        return null;
    }

    /* LC 236 — LCA in a general binary tree. Post-order.
     * Return the node if found; if both sides return non-null, I'm the LCA. */
    static TreeNode lca(TreeNode root, TreeNode p, TreeNode q) {
        if (root == null || root == p || root == q) return root;
        TreeNode l = lca(root.left, p, q);
        TreeNode r = lca(root.right, p, q);
        if (l != null && r != null) return root;   // split here
        return (l != null) ? l : r;                // bubble up the non-null side
    }

    /* LC 230 — kth smallest in a BST. Inorder gives sorted order; stop at k. */
    static int kthSmallest(TreeNode root, int k) {
        Deque<TreeNode> st = new ArrayDeque<>();
        TreeNode cur = root;
        while (cur != null || !st.isEmpty()) {
            while (cur != null) { st.push(cur); cur = cur.left; }
            cur = st.pop();
            if (--k == 0) return cur.val;
            cur = cur.right;
        }
        return -1;
    }

    /* LC 124 — max path sum. Bottom-up again. A node returns the best STRAIGHT
     * downward path (can only use ONE child), but the ANSWER may bend at the node. */
    static int maxPathBest;
    static int maxPathSum(TreeNode root) {
        maxPathBest = Integer.MIN_VALUE;
        gain(root);
        return maxPathBest;
    }
    private static int gain(TreeNode n) {
        if (n == null) return 0;
        int l = Math.max(gain(n.left), 0);    // clamp at 0: a negative branch is dropped
        int r = Math.max(gain(n.right), 0);
        maxPathBest = Math.max(maxPathBest, n.val + l + r);   // bend here
        return n.val + Math.max(l, r);                        // pass up: only one branch
    }

    /* LC 105 — build from preorder + inorder.
     * preorder[0] is the root. Find it in inorder: everything left of it is the
     * left subtree, everything right is the right subtree. Index map -> O(n). */
    static int preIdx;
    static TreeNode buildTree(int[] preorder, int[] inorder) {
        Map<Integer, Integer> pos = new HashMap<>();
        for (int i = 0; i < inorder.length; i++) pos.put(inorder[i], i);
        preIdx = 0;
        return build(preorder, 0, inorder.length - 1, pos);
    }
    private static TreeNode build(int[] pre, int lo, int hi, Map<Integer,Integer> pos) {
        if (lo > hi) return null;
        int rootVal = pre[preIdx++];
        TreeNode root = new TreeNode(rootVal);
        int mid = pos.get(rootVal);
        root.left  = build(pre, lo, mid - 1, pos);    // MUST be built before right
        root.right = build(pre, mid + 1, hi, pos);
        return root;
    }

    /* LC 297 — serialize/deserialize. Preorder with explicit "#" for nulls. */
    static String serialize(TreeNode root) {
        StringBuilder sb = new StringBuilder();
        ser(root, sb);
        return sb.toString();
    }
    private static void ser(TreeNode n, StringBuilder sb) {
        if (n == null) { sb.append("#,"); return; }
        sb.append(n.val).append(',');
        ser(n.left, sb);
        ser(n.right, sb);
    }
    static TreeNode deserialize(String data) {
        return des(new ArrayDeque<>(Arrays.asList(data.split(","))));
    }
    private static TreeNode des(Deque<String> q) {
        String t = q.poll();
        if (t == null || t.equals("#")) return null;
        TreeNode n = new TreeNode(Integer.parseInt(t));
        n.left  = des(q);
        n.right = des(q);
        return n;
    }

    /* ======================================================================
     * 5. JAVA API — TreeMap/TreeSet (red-black trees) demonstrated live in
     * main() below. Full method reference lives in ../../java-api-examples.md.
     * Note: there is no built-in java.util BinaryTree — nodes are always hand-rolled.
     * ====================================================================== */

    /* helper: build the sample tree */
    static TreeNode sample() {
        TreeNode n = new TreeNode(8,
                new TreeNode(3, new TreeNode(1),
                        new TreeNode(6, new TreeNode(4), new TreeNode(7))),
                new TreeNode(10, null,
                        new TreeNode(14, new TreeNode(13), null)));
        return n;
    }

    /* ================================================================== */
    public static void main(String[] args) {
        TreeNode root = sample();

        System.out.println("--- traversals (recursive) ---");
        List<Integer> a = new ArrayList<>(); preorder(root, a);
        List<Integer> b = new ArrayList<>(); inorder(root, b);
        List<Integer> c = new ArrayList<>(); postorder(root, c);
        System.out.println("pre  : " + a);
        System.out.println("in   : " + b + "   <- sorted, because it's a BST");
        System.out.println("post : " + c);

        System.out.println("\n--- traversals (iterative) ---");
        System.out.println("pre  : " + preorderIter(root));
        System.out.println("in   : " + inorderIter(root));
        System.out.println("post : " + postorderIter(root));
        System.out.println("bfs  : " + levelOrder(root));

        System.out.println("\n--- properties ---");
        System.out.println("maxDepth    = " + maxDepth(root));
        System.out.println("diameter    = " + diameter(root) + " (edges)");
        System.out.println("isBalanced  = " + isBalanced(root));
        System.out.println("isValidBST  = " + isValidBST(root));
        System.out.println("kthSmallest(3) = " + kthSmallest(root, 3));
        System.out.println("maxPathSum  = " + maxPathSum(root));

        System.out.println("\n--- LCA ---");
        System.out.println("lcaBST(4,7) = " + lcaBST(root, 4, 7).val);
        System.out.println("lcaBST(1,13)= " + lcaBST(root, 1, 13).val);

        System.out.println("\n--- serialize round trip ---");
        String s = serialize(root);
        System.out.println(s);
        System.out.println("same after deserialize? " + isSame(root, deserialize(s)));

        System.out.println("\n--- build from pre+in ---");
        TreeNode rebuilt = buildTree(new int[]{3,9,20,15,7}, new int[]{9,3,15,20,7});
        System.out.println("levelOrder = " + levelOrder(rebuilt));

        System.out.println("\n--- generic BST class ---");
        BST<Integer> bst = new BST<>();
        for (int v : new int[]{8,3,10,1,6,14,4,7,13}) bst.insert(v);
        System.out.println("inorder=" + bst.inorder() + " min=" + bst.min() + " max=" + bst.max());
        bst.delete(3);
        System.out.println("after delete(3): " + bst.inorder() + " size=" + bst.size());

        System.out.println("\n--- TreeMap/TreeSet API ---");
        TreeSet<Integer> ts = new TreeSet<>(List.of(10, 20, 30, 40));
        System.out.println("floor(25)=" + ts.floor(25) + " ceiling(25)=" + ts.ceiling(25)
                + " subSet(15,35)=" + ts.subSet(15, 35) + " descending=" + ts.descendingSet());
    }
}

/* ==========================================================================
 * PRACTICE — BLIND 75 / NEETCODE (trees), in a sensible order
 * ==========================================================================
 *   LC 226  Invert Binary Tree                    easy
 *   LC 104  Maximum Depth of Binary Tree          easy
 *   LC 100  Same Tree                             easy
 *   LC 572  Subtree of Another Tree               easy   isSame at every node
 *   LC 543  Diameter of Binary Tree               easy   bottom-up height
 *   LC 110  Balanced Binary Tree                  easy   -1 sentinel
 *   LC 235  LCA of a BST                          med    walk the split point
 *   LC 236  LCA of a Binary Tree                  med    post-order bubbling
 *   LC 102  Level Order Traversal                 med    BFS template
 *   LC 199  Right Side View                       med    last of each level
 *   LC 1448 Count Good Nodes                      med    pass max down
 *   LC 98   Validate BST                          med    (min,max) range
 *   LC 230  Kth Smallest in BST                   med    inorder, stop at k
 *   LC 105  Build Tree from Preorder + Inorder    med    index map
 *   LC 124  Binary Tree Max Path Sum              hard   clamp negatives at 0
 *   LC 297  Serialize / Deserialize               hard   preorder with '#'
 *
 * Self-test questions + answers: see ./README.md
 * ========================================================================== */
