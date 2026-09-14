import java.util.LinkedList;

/* Binary Search Tree, plain int values, no <T>. Run: java TreePlain.java
 * For the mental model (vocabulary, why inorder is sorted, diameter/LCA/etc.),
 * see ../../04-trees-heaps-tries/01-tree/README.md — this file is purely "watch it work." */
public class TreePlain {

    /* ======================================================================
     * NODE — plain int payload, two children. A tree node is just a linked
     * list node with a second "next" pointer.
     * ====================================================================== */
    static class Node {
        int val;
        Node left, right;
        Node(int val) { this.val = val; }
    }

    /* ======================================================================
     * BST — insert, search, all four traversals, delete, height/isBalanced.
     * ====================================================================== */
    static class BST {
        Node root;

        /* ---------- 1. insert / search ---------- */

        void insert(int val) { root = insert(root, val); }

        private Node insert(Node node, int val) {
            if (node == null) return new Node(val); // fell off the tree -> this is where it belongs
            if (val < node.val) node.left = insert(node.left, val);
            else if (val > node.val) node.right = insert(node.right, val);
            // equal: no duplicates, leave tree unchanged
            return node;
        }

        boolean search(int val) {
            Node cur = root;
            while (cur != null) {
                if (val == cur.val) return true;
                cur = val < cur.val ? cur.left : cur.right; // one comparison picks the only subtree that could hold it
            }
            return false;
        }

        /* ---------- 2. traversals ---------- */

        void preorder(Node node, StringBuilder out) { // node, left, right
            if (node == null) return;
            out.append(node.val).append(" ");
            preorder(node.left, out);
            preorder(node.right, out);
        }

        void inorder(Node node, StringBuilder out) { // left, node, right -> ascending for a BST, always
            if (node == null) return;
            inorder(node.left, out);
            out.append(node.val).append(" ");
            inorder(node.right, out);
        }

        void postorder(Node node, StringBuilder out) { // left, right, node
            if (node == null) return;
            postorder(node.left, out);
            postorder(node.right, out);
            out.append(node.val).append(" ");
        }

        String levelOrder() { // BFS: a queue, not a stack, is what makes it level-by-level
            StringBuilder out = new StringBuilder();
            if (root == null) return out.toString();
            LinkedList<Node> queue = new LinkedList<>();
            queue.add(root);
            while (!queue.isEmpty()) {
                int levelSize = queue.size(); // snapshot size BEFORE the loop so we know where this level ends
                for (int i = 0; i < levelSize; i++) {
                    Node n = queue.poll();
                    out.append(n.val).append(" ");
                    if (n.left != null) queue.add(n.left);
                    if (n.right != null) queue.add(n.right);
                }
                out.append("| "); // level separator, purely cosmetic
            }
            return out.toString();
        }

        /* ---------- 3. delete ---------- */

        void delete(int val) { root = delete(root, val); }

        private Node delete(Node node, int val) {
            if (node == null) return null; // value not present, nothing to do
            if (val < node.val) { node.left = delete(node.left, val); return node; }
            if (val > node.val) { node.right = delete(node.right, val); return node; }

            // found the node to delete
            if (node.left == null) return node.right;  // leaf (both null) falls here too -> returns null
            if (node.right == null) return node.left;  // one child (left only, or right only)

            // Two children: we CANNOT just unlink this node and promote either child directly —
            // node.left is itself a whole subtree possibly containing a right child, and node.right
            // likewise may have a left child, so neither can simply take the deleted node's slot
            // without losing or misplacing the other subtree. Instead we copy in a value that is
            // already guaranteed to sit correctly between both subtrees: the inorder successor
            // (smallest value in the right subtree), then delete that successor from the right
            // subtree — a deletion that is always the easy leaf/one-child case, since the
            // successor by definition has no left child.
            Node successor = node.right;
            while (successor.left != null) successor = successor.left; // leftmost node in right subtree = smallest there
            node.val = successor.val;                    // copy value up
            node.right = delete(node.right, successor.val); // remove the now-duplicated successor node
            return node;
        }

        /* ---------- 4. height / isBalanced ---------- */

        int height(Node node) { // edges down to the deepest leaf; empty tree = -1, single node = 0
            if (node == null) return -1;
            return 1 + Math.max(height(node.left), height(node.right));
        }

        boolean isBalanced(Node root) {
            return balHeight(root) != -1;
        }

        // Single post-order pass, O(n): returns the real height, OR -1 as a sentinel meaning
        // "some subtree below here is already unbalanced." The naive approach — call height()
        // independently at every node to compare |height(left) - height(right)| <= 1 — recomputes
        // the height of every subtree once per ancestor, giving O(n log n) on a balanced tree and
        // O(n^2) on a degenerate one. Here each node's height is computed exactly once, and an
        // imbalance found deep in the tree short-circuits immediately (if (l == -1) return -1)
        // instead of letting the recursion keep computing heights it will never use.
        private int balHeight(Node node) {
            if (node == null) return -1;
            int l = balHeight(node.left);
            if (l == -1) return -1; // left subtree already unbalanced -> stop, propagate the sentinel up
            int r = balHeight(node.right);
            if (r == -1) return -1;
            if (Math.abs(l - r) > 1) return -1; // this node itself is unbalanced
            return 1 + Math.max(l, r);
        }
    }

    public static void main(String[] args) {
        /* Demo tree:
         *                 8
         *               /   \
         *              3     10
         *             / \      \
         *            1   6      14
         *               / \     /
         *              4   7   13
         */
        BST tree = new BST();
        int[] values = {8, 3, 10, 1, 6, 14, 4, 7, 13};
        System.out.println("=== 1. insert " + java.util.Arrays.toString(values) + " ===");
        for (int v : values) tree.insert(v);

        System.out.println("search(7)=" + tree.search(7) + "  search(99)=" + tree.search(99));

        System.out.println("\n=== 2. traversals (same tree — compare inorder to the rest) ===");
        StringBuilder pre = new StringBuilder(), in = new StringBuilder(), post = new StringBuilder();
        tree.preorder(tree.root, pre);
        tree.inorder(tree.root, in);
        tree.postorder(tree.root, post);
        System.out.println("preorder    : " + pre);
        System.out.println("inorder     : " + in + " <- sorted! this is the whole point of inorder on a BST");
        System.out.println("postorder   : " + post);
        System.out.println("level-order : " + tree.levelOrder());

        System.out.println("\n=== 3. delete — two-children, leaf, one-child (in that order), checking inorder stays sorted after each ===");
        System.out.println("(two-children must go first here: node 3's children are {1, 6-subtree}; deleting 1 first would");
        System.out.println(" leave node 3 with only one child, so the order below is what makes each case genuine)");

        System.out.println("\n-- delete(3): two-children node (left=1, right=6 with children 4,7) --");
        System.out.println("   inorder successor = smallest value in right subtree of 3 = leftmost node reached via 6 -> 4 = 4");
        System.out.println("   so node 3's slot gets value 4 copied in, then the original leaf node holding 4 is deleted from under 6");
        tree.delete(3);
        printInorder(tree);

        System.out.println("\n-- delete(1): leaf node (no children) --");
        tree.delete(1);
        printInorder(tree);

        System.out.println("\n-- delete(14): one-child node (only left child 13) --");
        tree.delete(14);
        printInorder(tree);

        System.out.println("\nfinal level-order: " + tree.levelOrder());

        System.out.println("\n=== 4. height / isBalanced ===");
        System.out.println("height(root) = " + tree.height(tree.root));
        System.out.println("isBalanced(root) = " + tree.isBalanced(tree.root));

        BST skewed = new BST();
        for (int v : new int[]{1, 2, 3, 4, 5}) skewed.insert(v); // ascending inserts -> pure right-skewed chain
        System.out.println("\nskewed tree (inserted 1..5 in order): height=" + skewed.height(skewed.root)
                + "  isBalanced=" + skewed.isBalanced(skewed.root));
    }

    private static void printInorder(BST tree) {
        StringBuilder in = new StringBuilder();
        tree.inorder(tree.root, in);
        System.out.println("   inorder now: " + in + (isSorted(in.toString()) ? "  (still sorted, BST invariant holds)" : "  (NOT SORTED - BUG)"));
    }

    private static boolean isSorted(String spaceSeparated) {
        String[] parts = spaceSeparated.trim().isEmpty() ? new String[0] : spaceSeparated.trim().split("\\s+");
        for (int i = 1; i < parts.length; i++) {
            if (Integer.parseInt(parts[i - 1]) > Integer.parseInt(parts[i])) return false;
        }
        return true;
    }
}
