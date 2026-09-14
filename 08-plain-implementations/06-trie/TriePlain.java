import java.util.HashMap;

/* Trie (prefix tree), two node layouts + delete — plain String/char, no <T>. Run: java TriePlain.java
 * For the mental model / complexity table / LC 208, 211, 212, 421 writeups, see
 * ../../04-trees-heaps-tries/03-trie/README.md — this file is purely "watch it work." */
public class TriePlain {

    /* ======================================================================
     * 1. ARRAY TRIE — each Node holds a fixed Node[26], indexed by c - 'a'.
     * isEnd is mandatory: the PATH existing is not the same as the WORD
     * existing. If only "carpet" is ever inserted, the path c->a->r is live
     * (carpet passes through it), so a search("car") that only checked
     * "did I run out of path without hitting null" would wrongly say true.
     * isEnd marks the exact nodes where an inserted word actually terminates.
     * ====================================================================== */
    static class ArrayTrie {
        static class Node {
            Node[] children = new Node[26];
            boolean isEnd;
        }

        private final Node root = new Node();

        void insert(String word) {
            Node cur = root;
            for (int i = 0; i < word.length(); i++) {
                int idx = word.charAt(i) - 'a';
                if (cur.children[idx] == null) cur.children[idx] = new Node();
                cur = cur.children[idx];
            }
            cur.isEnd = true; // mark ONLY the last node of this word, not every node walked
        }

        boolean search(String word) {
            Node cur = walk(word);
            return cur != null && cur.isEnd; // path AND explicit end-marker both required
        }

        boolean startsWith(String prefix) {
            return walk(prefix) != null; // path existing is enough; isEnd irrelevant here
        }

        // Shared traversal: returns the node at the end of the path, or null if the path breaks.
        private Node walk(String s) {
            Node cur = root;
            for (int i = 0; i < s.length(); i++) {
                int idx = s.charAt(i) - 'a';
                if (cur.children[idx] == null) return null;
                cur = cur.children[idx];
            }
            return cur;
        }

        /* ------------------------------------------------------------------
         * DELETE — recursive, prunes dead nodes on the way back up.
         * A node is safe to unlink from its parent only when BOTH hold:
         *   1. it is not itself the end of some OTHER word (!isEnd), and
         *   2. it has no remaining children (nothing else passes through it).
         * Either condition alone is not enough: isEnd-but-no-children means
         * some other word ends exactly here; has-children-but-not-isEnd means
         * it's mid-path for some other word. Only "neither" means genuinely dead.
         * ------------------------------------------------------------------ */
        void delete(String word) {
            deleteHelper(root, word, 0);
        }

        // Returns true if `cur` should be unlinked by ITS caller (i.e. cur is now dead).
        private boolean deleteHelper(Node cur, String word, int depth) {
            if (depth == word.length()) {
                if (!cur.isEnd) return false; // word was never actually inserted, nothing to do
                cur.isEnd = false; // un-mark; node may still be needed as a prefix for other words
                return isEmpty(cur);
            }
            int idx = word.charAt(depth) - 'a';
            Node child = cur.children[idx];
            if (child == null) return false; // word not present, nothing to delete

            boolean childShouldDie = deleteHelper(child, word, depth + 1);
            if (childShouldDie) {
                cur.children[idx] = null; // unlink the dead subtree
            }
            // cur itself is only prune-eligible if it's not an end-of-word AND now has no children
            return !cur.isEnd && isEmpty(cur);
        }

        private boolean isEmpty(Node n) {
            for (Node child : n.children) if (child != null) return false;
            return true;
        }
    }

    /* ======================================================================
     * 2. MAP TRIE — same behavior, children stored as HashMap<Character, Node>
     * instead of a fixed array.
     *
     * Real tradeoff: ArrayTrie allocates 26 references PER NODE regardless of
     * how many children actually exist — for a large trie where most nodes
     * branch to only 1-2 children (long, sparse words), that's a lot of wasted
     * pointers, but indexing is O(1) branchless array access. MapTrie only
     * pays for children that exist (memory proportional to actual branching),
     * but every get/put goes through hashCode() + bucket lookup — slower per
     * character even though still O(1) amortized. Array wins on speed and
     * fixed lowercase alphabets; map wins on memory for sparse/large alphabets
     * (unicode, mixed case, symbols).
     * ====================================================================== */
    static class MapTrie {
        static class Node {
            HashMap<Character, Node> children = new HashMap<>();
            boolean isEnd;
        }

        private final Node root = new Node();

        void insert(String word) {
            Node cur = root;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                cur = cur.children.computeIfAbsent(c, k -> new Node()); // allocate only if branch is new
            }
            cur.isEnd = true;
        }

        boolean search(String word) {
            Node cur = walk(word);
            return cur != null && cur.isEnd;
        }

        boolean startsWith(String prefix) {
            return walk(prefix) != null;
        }

        private Node walk(String s) {
            Node cur = root;
            for (int i = 0; i < s.length(); i++) {
                cur = cur.children.get(s.charAt(i));
                if (cur == null) return null;
            }
            return cur;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== 1. ArrayTrie — shared prefixes: cat, car, care, dog ===");
        ArrayTrie t = new ArrayTrie();
        for (String w : new String[]{"cat", "car", "care", "dog"}) t.insert(w);

        // These are exactly the cases a broken isEnd handling would trip on.
        System.out.println("search(car)=" + t.search("car"));       // true - "car" was inserted directly
        System.out.println("search(ca)=" + t.search("ca"));         // false - "ca" is only a path, never inserted
        System.out.println("search(care)=" + t.search("care"));     // true
        System.out.println("search(careful)=" + t.search("careful")); // false - path breaks after "care"
        System.out.println("startsWith(ca)=" + t.startsWith("ca")); // true - path exists (cat/car/care all pass through)
        System.out.println("startsWith(do)=" + t.startsWith("do")); // true
        System.out.println("startsWith(x)=" + t.startsWith("x"));   // false - no path at all

        System.out.println("\n=== 2. MapTrie — identical behavior, HashMap<Character,Node> children ===");
        MapTrie mt = new MapTrie();
        for (String w : new String[]{"cat", "car", "care", "dog"}) mt.insert(w);
        System.out.println("search(car)=" + mt.search("car"));
        System.out.println("search(ca)=" + mt.search("ca"));
        System.out.println("startsWith(car)=" + mt.startsWith("car"));
        System.out.println("startsWith(z)=" + mt.startsWith("z"));

        System.out.println("\n=== 3. ArrayTrie delete — prune without breaking a shared-prefix sibling ===");
        ArrayTrie dt = new ArrayTrie();
        dt.insert("car");
        dt.insert("care");
        System.out.println("before delete: search(car)=" + dt.search("car") + "  search(care)=" + dt.search("care"));
        dt.delete("car");
        System.out.println("after delete(car):");
        System.out.println("  search(car)=" + dt.search("car") + "   (expected false - 'car' un-marked as a word)");
        System.out.println("  search(care)=" + dt.search("care") + "  (expected true - 'care' untouched)");
        System.out.println("  startsWith(car)=" + dt.startsWith("car") + " (expected true - path c-a-r still needed by 'care')");
    }
}
