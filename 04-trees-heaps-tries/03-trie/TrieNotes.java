import java.util.*;

/* ==========================================================================
 * DS: TRIE  (prefix tree, pronounced "try", from reTRIEval)
 * Run: java TrieNotes.java
 * See ./README.md for the mental model (edge = character), why isEnd is
 * mandatory, node-layout options, complexity, and trie-vs-hashmap — not
 * repeated here.
 * ========================================================================== */
public class TrieNotes {

    /* ======================================================================
     * 1. IMPLEMENTATION A — array children, lowercase a-z  (LC 208)
     * ====================================================================== */
    static class Trie {
        static class Node {
            Node[] children = new Node[26];
            boolean isEnd;
            int wordCount;     // how many words END here (supports counting/dupes)
            int prefixCount;   // how many words pass THROUGH here (supports countPrefix)
        }

        private final Node root = new Node();

        void insert(String word) {
            Node cur = root;
            for (char c : word.toCharArray()) {
                int i = c - 'a';
                if (cur.children[i] == null) cur.children[i] = new Node();
                cur = cur.children[i];
                cur.prefixCount++;
            }
            cur.isEnd = true;
            cur.wordCount++;
        }

        /* Walk the path; return the node at the end of `s`, or null if it breaks. */
        private Node walk(String s) {
            Node cur = root;
            for (char c : s.toCharArray()) {
                int i = c - 'a';
                if (i < 0 || i >= 26 || cur.children[i] == null) return null;
                cur = cur.children[i];
            }
            return cur;
        }

        boolean search(String word)    { Node n = walk(word); return n != null && n.isEnd; }
        boolean startsWith(String pre) { return walk(pre) != null; }
        int countWordsEqualTo(String w){ Node n = walk(w);   return n == null ? 0 : n.wordCount; }
        int countWordsStartingWith(String p){ Node n = walk(p); return n == null ? 0 : n.prefixCount; }

        /* DELETE — remove the word, then prune nodes that became useless.
         * A node can be pruned only if it is not the end of another word AND has
         * no remaining children. Recursion returns "should my parent unlink me?" */
        boolean delete(String word) {
            if (!search(word)) return false;
            delete(root, word, 0);
            return true;
        }
        private boolean delete(Node cur, String word, int depth) {
            if (depth == word.length()) {
                cur.wordCount--;
                if (cur.wordCount == 0) cur.isEnd = false;
                return !cur.isEnd && isLeaf(cur);
            }
            int i = word.charAt(depth) - 'a';
            Node child = cur.children[i];
            child.prefixCount--;
            boolean prune = delete(child, word, depth + 1);
            if (prune) cur.children[i] = null;                 // unlink -> GC
            return !cur.isEnd && isLeaf(cur);
        }
        private boolean isLeaf(Node n) {
            for (Node c : n.children) if (c != null) return false;
            return true;
        }

        /* AUTOCOMPLETE: all words under a prefix, in lexicographic order (DFS a..z). */
        List<String> wordsWithPrefix(String prefix, int limit) {
            List<String> out = new ArrayList<>();
            Node start = walk(prefix);
            if (start == null) return out;
            dfs(start, new StringBuilder(prefix), out, limit);
            return out;
        }
        private void dfs(Node n, StringBuilder path, List<String> out, int limit) {
            if (out.size() >= limit) return;
            if (n.isEnd) out.add(path.toString());
            for (int i = 0; i < 26; i++) {
                if (n.children[i] == null) continue;
                path.append((char) ('a' + i));
                dfs(n.children[i], path, out, limit);
                path.deleteCharAt(path.length() - 1);    // BACKTRACK - undo the append
            }
        }

        /* Longest stored word that is a prefix of `s`. Used in word-break / routing. */
        String longestPrefixOf(String s) {
            Node cur = root;
            int best = 0;
            for (int i = 0; i < s.length(); i++) {
                int idx = s.charAt(i) - 'a';
                if (idx < 0 || idx >= 26 || cur.children[idx] == null) break;
                cur = cur.children[idx];
                if (cur.isEnd) best = i + 1;
            }
            return s.substring(0, best);
        }
    }

    /* ======================================================================
     * 2. IMPLEMENTATION B — HashMap children, any character set
     * ====================================================================== */
    static class MapTrie {
        static class Node {
            Map<Character, Node> children = new HashMap<>();
            boolean isEnd;
        }
        private final Node root = new Node();

        void insert(String w) {
            Node cur = root;
            for (char c : w.toCharArray())
                cur = cur.children.computeIfAbsent(c, k -> new Node());   // the idiom
            cur.isEnd = true;
        }
        boolean search(String w) {
            Node n = walk(w);
            return n != null && n.isEnd;
        }
        boolean startsWith(String p) { return walk(p) != null; }
        private Node walk(String s) {
            Node cur = root;
            for (char c : s.toCharArray()) {
                cur = cur.children.get(c);
                if (cur == null) return null;
            }
            return cur;
        }
    }

    /* ======================================================================
     * 3. WILDCARD SEARCH  (LC 211) — '.' matches any single character
     * At a '.', you must branch into EVERY child -> DFS with backtracking.
     * Worst case O(26^L); in practice fine because tries are sparse.
     * ====================================================================== */
    static class WordDictionary {
        private final Trie.Node root = new Trie.Node();

        void addWord(String w) {
            Trie.Node cur = root;
            for (char c : w.toCharArray()) {
                int i = c - 'a';
                if (cur.children[i] == null) cur.children[i] = new Trie.Node();
                cur = cur.children[i];
            }
            cur.isEnd = true;
        }

        boolean search(String w) { return dfs(root, w, 0); }

        private boolean dfs(Trie.Node node, String w, int idx) {
            if (node == null) return false;
            if (idx == w.length()) return node.isEnd;
            char c = w.charAt(idx);
            if (c == '.') {
                for (Trie.Node child : node.children)          // try every branch
                    if (dfs(child, w, idx + 1)) return true;
                return false;
            }
            return dfs(node.children[c - 'a'], w, idx + 1);
        }
    }

    /* ======================================================================
     * 4. WORD SEARCH II  (LC 212) — trie + DFS on a grid. THE hard trie problem.
     *
     * Naive: DFS from every cell for every word -> way too slow.
     * Trie version: DFS the grid ONCE, walking the trie in lockstep. The moment
     * the current path isn't a trie prefix, prune the whole branch.
     *
     * Two important optimisations interviewers look for:
     *   - store the whole word on the terminal node (no StringBuilder juggling)
     *   - null out node.word after collecting it, so you never add a duplicate
     * ====================================================================== */
    static class WordNode {
        WordNode[] next = new WordNode[26];
        String word;                      // non-null only at a terminal node
    }

    static List<String> findWords(char[][] board, String[] words) {
        WordNode root = new WordNode();
        for (String w : words) {          // build the trie
            WordNode cur = root;
            for (char c : w.toCharArray()) {
                int i = c - 'a';
                if (cur.next[i] == null) cur.next[i] = new WordNode();
                cur = cur.next[i];
            }
            cur.word = w;
        }

        List<String> res = new ArrayList<>();
        for (int r = 0; r < board.length; r++)
            for (int c = 0; c < board[0].length; c++)
                gridDfs(board, r, c, root, res);
        return res;
    }

    private static void gridDfs(char[][] b, int r, int c, WordNode node, List<String> res) {
        if (r < 0 || c < 0 || r >= b.length || c >= b[0].length) return;
        char ch = b[r][c];
        if (ch == '#') return;                       // already on the current path
        WordNode nxt = node.next[ch - 'a'];
        if (nxt == null) return;                     // PRUNE: not a valid prefix

        if (nxt.word != null) { res.add(nxt.word); nxt.word = null; }  // collect once

        b[r][c] = '#';                               // mark visited
        gridDfs(b, r + 1, c, nxt, res);
        gridDfs(b, r - 1, c, nxt, res);
        gridDfs(b, r, c + 1, nxt, res);
        gridDfs(b, r, c - 1, nxt, res);
        b[r][c] = ch;                                // BACKTRACK: restore
    }

    /* ======================================================================
     * 5. BINARY TRIE — maximum XOR of two numbers  (LC 421)
     * Insert each number as a 32-bit path. To maximise a XOR b, at each bit
     * greedily walk to the OPPOSITE bit if it exists.
     * ====================================================================== */
    static class BinaryTrie {
        static class Node { Node[] kid = new Node[2]; }
        private final Node root = new Node();
        private static final int BITS = 31;      // enough for non-negative ints

        void insert(int num) {
            Node cur = root;
            for (int b = BITS; b >= 0; b--) {
                int bit = (num >> b) & 1;
                if (cur.kid[bit] == null) cur.kid[bit] = new Node();
                cur = cur.kid[bit];
            }
        }

        int maxXorWith(int num) {
            Node cur = root;
            int best = 0;
            for (int b = BITS; b >= 0; b--) {
                int bit = (num >> b) & 1;
                int want = 1 - bit;                       // opposite bit maximises XOR
                if (cur.kid[want] != null) { best |= (1 << b); cur = cur.kid[want]; }
                else cur = cur.kid[bit];
            }
            return best;
        }
    }

    static int findMaximumXOR(int[] nums) {
        BinaryTrie t = new BinaryTrie();
        int best = 0;
        for (int n : nums) t.insert(n);
        for (int n : nums) best = Math.max(best, t.maxXorWith(n));
        return best;
    }

    /* ================================================================== */
    public static void main(String[] args) {
        System.out.println("--- Trie basics ---");
        Trie t = new Trie();
        for (String w : new String[]{"cat", "car", "card", "care", "dog", "do"}) t.insert(w);

        System.out.println("search('car')      = " + t.search("car"));
        System.out.println("search('ca')       = " + t.search("ca") + "   <- prefix, not a word");
        System.out.println("startsWith('ca')   = " + t.startsWith("ca"));
        System.out.println("startsWith('cax')  = " + t.startsWith("cax"));
        System.out.println("countStartingWith('car') = " + t.countWordsStartingWith("car"));

        System.out.println("\n--- autocomplete ---");
        System.out.println("prefix 'car' -> " + t.wordsWithPrefix("car", 10));
        System.out.println("prefix 'c'   -> " + t.wordsWithPrefix("c", 10));
        System.out.println("longestPrefixOf('cardinal') = " + t.longestPrefixOf("cardinal"));

        System.out.println("\n--- delete + prune ---");
        System.out.println("delete('card') = " + t.delete("card"));
        System.out.println("search('card') = " + t.search("card")
                + ", search('car') still = " + t.search("car"));
        System.out.println("prefix 'car' -> " + t.wordsWithPrefix("car", 10));

        System.out.println("\n--- MapTrie (any charset) ---");
        MapTrie mt = new MapTrie();
        mt.insert("नमस्ते"); mt.insert("hello");
        System.out.println("search unicode = " + mt.search("नमस्ते") + ", startsWith('hel') = "
                + mt.startsWith("hel"));

        System.out.println("\n--- WordDictionary with '.' wildcard ---");
        WordDictionary wd = new WordDictionary();
        wd.addWord("bad"); wd.addWord("dad"); wd.addWord("mad");
        System.out.println("search('pad') = " + wd.search("pad"));
        System.out.println("search('bad') = " + wd.search("bad"));
        System.out.println("search('.ad') = " + wd.search(".ad"));
        System.out.println("search('b..') = " + wd.search("b.."));

        System.out.println("\n--- Word Search II (trie + grid DFS) ---");
        char[][] board = {
            {'o','a','a','n'},
            {'e','t','a','e'},
            {'i','h','k','r'},
            {'i','f','l','v'}
        };
        System.out.println(findWords(board, new String[]{"oath","pea","eat","rain"}));

        System.out.println("\n--- Binary trie: max XOR ---");
        System.out.println("findMaximumXOR([3,10,5,25,2,8]) = " +
                findMaximumXOR(new int[]{3,10,5,25,2,8}));
    }
}

/* ==========================================================================
 * PRACTICE — BLIND 75 / NEETCODE (trie)
 * ==========================================================================
 *   LC 208  Implement Trie (Prefix Tree)          med    the base implementation
 *   LC 211  Design Add and Search Words           med    '.' wildcard DFS
 *   LC 212  Word Search II                        hard   trie + grid DFS + prune
 *   LC 1268 Search Suggestions System             med    autocomplete, top-3
 *   LC 648  Replace Words                         med    longestPrefixOf
 *   LC 720  Longest Word in Dictionary            med    DFS with isEnd chain
 *   LC 421  Maximum XOR of Two Numbers            med    binary trie
 *   LC 139  Word Break                            med    trie or DP
 *   LC 745  Prefix and Suffix Search              hard   two tries or combined keys
 *   LC 336  Palindrome Pairs                      hard   reversed-word trie
 *
 * Self-test questions + answers: see ./README.md
 * ========================================================================== */
