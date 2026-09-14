import java.util.*;

/* ==========================================================================
 * DS: HASH MAP / HASH TABLE / HASH SET
 * Run: java HashMapNotes.java
 * See ./README.md for the mental model, complexity, collision strategies,
 * treeification, and the equals/hashCode contract — not repeated here.
 * ========================================================================== */
public class HashMapNotes {

    /* ======================================================================
     * 1. IMPLEMENTATION — separate chaining + resize
     * ====================================================================== */
    static class MyHashMap<K, V> {
        private static class Node<K, V> {
            final K key; V val; Node<K, V> next;
            Node(K k, V v, Node<K, V> next) { key = k; val = v; this.next = next; }
        }

        private Node<K, V>[] table;
        private int size;
        private static final double LOAD_FACTOR = 0.75;

        @SuppressWarnings("unchecked")
        MyHashMap(int cap) {
            int c = 1;
            while (c < cap) c <<= 1;              // round UP to power of two
            table = new Node[c];
        }
        MyHashMap() { this(16); }

        /* hash -> spread -> mask */
        private int indexFor(Object key) {
            if (key == null) return 0;            // null key always lands in bucket 0
            int h = key.hashCode();
            h ^= (h >>> 16);                      // mix high bits into low
            return h & (table.length - 1);        // == h % table.length, since power of 2
        }

        private static boolean eq(Object a, Object b) {
            return a == null ? b == null : a.equals(b);
        }

        public V put(K key, V val) {
            int i = indexFor(key);
            for (Node<K, V> n = table[i]; n != null; n = n.next) {
                if (eq(n.key, key)) {             // key exists -> overwrite, return old
                    V old = n.val;
                    n.val = val;
                    return old;
                }
            }
            table[i] = new Node<>(key, val, table[i]);   // prepend, O(1)
            if (++size > table.length * LOAD_FACTOR) resize();
            return null;
        }

        public V get(Object key) {
            for (Node<K, V> n = table[indexFor(key)]; n != null; n = n.next)
                if (eq(n.key, key)) return n.val;
            return null;
        }

        public V remove(Object key) {
            int i = indexFor(key);
            Node<K, V> prev = null;
            for (Node<K, V> n = table[i]; n != null; prev = n, n = n.next) {
                if (eq(n.key, key)) {
                    if (prev == null) table[i] = n.next;   // removing the head
                    else prev.next = n.next;               // splice out
                    size--;
                    return n.val;
                }
            }
            return null;
        }

        public boolean containsKey(Object key) {
            for (Node<K, V> n = table[indexFor(key)]; n != null; n = n.next)
                if (eq(n.key, key)) return true;
            return false;    // note: don't use get()!=null, a key can legitimately map to null
        }

        public int size() { return size; }

        /* Double capacity and re-place every node — every node's bucket index can
         * change since indexFor masks with the new (larger) table.length. O(n),
         * amortized away because it only fires once per doubling of size.
         * (No treeification here — this from-scratch version stays O(n) worst case
         * per bucket; see README for how java.util.HashMap avoids that.) */
        @SuppressWarnings("unchecked")
        private void resize() {
            Node<K, V>[] old = table;
            table = new Node[old.length << 1];
            size = 0;
            for (Node<K, V> head : old)
                for (Node<K, V> n = head; n != null; n = n.next)
                    put(n.key, n.val);
        }

        @Override public String toString() {
            StringBuilder sb = new StringBuilder("{");
            for (Node<K, V> head : table)
                for (Node<K, V> n = head; n != null; n = n.next)
                    sb.append(n.key).append('=').append(n.val).append(", ");
            if (sb.length() > 1) sb.setLength(sb.length() - 2);
            return sb.append('}').toString();
        }
    }

    /* ======================================================================
     * 2. CUSTOM KEY — how to write equals/hashCode correctly
     * ====================================================================== */
    static final class Point {
        final int x, y;                    // FINAL: keys must be immutable
        Point(int x, int y) { this.x = x; this.y = y; }

        @Override public boolean equals(Object o) {
            if (this == o) return true;                       // fast path
            if (!(o instanceof Point p)) return false;        // pattern matching (Java 16+)
            return x == p.x && y == p.y;
        }
        @Override public int hashCode() {
            return Objects.hash(x, y);     // or: 31 * x + y  (31 = odd prime, JIT-friendly)
        }
        @Override public String toString() { return "(" + x + "," + y + ")"; }
    }

    /* A `record` gives you equals/hashCode/toString for free. Use it in interviews. */
    record Cell(int row, int col) { }

    /* ======================================================================
     * 3. JAVA API IDIOMS — demonstrated live in main() below (merge counting,
     * computeIfAbsent grouping, TreeMap navigation). Full syntax reference
     * (containsValue, entrySet iteration, HashMap/TreeMap/EnumMap variants,
     * removeIf-safe iteration) lives in ../../java-api-examples.md.
     * ====================================================================== */

    /* ======================================================================
     * 4. PATTERN: TWO SUM  (LC 1) — value -> index, single pass
     * ====================================================================== */
    static int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> seen = new HashMap<>();     // value -> index
        for (int i = 0; i < nums.length; i++) {
            Integer j = seen.get(target - nums[i]);       // did we already see the partner?
            if (j != null) return new int[]{j, i};
            seen.put(nums[i], i);                         // put AFTER the check (handles x+x)
        }
        return new int[]{-1, -1};
    }

    /* ======================================================================
     * 5. PATTERN: GROUP ANAGRAMS  (LC 49) — canonical key + computeIfAbsent
     * Key options: sorted chars O(n k log k), or a 26-count signature O(n k).
     * ====================================================================== */
    static List<List<String>> groupAnagrams(String[] strs) {
        Map<String, List<String>> groups = new HashMap<>();
        for (String s : strs) {
            int[] cnt = new int[26];
            for (char c : s.toCharArray()) cnt[c - 'a']++;
            String key = Arrays.toString(cnt);            // "#1#0#2..." signature, O(k)
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
        }
        return new ArrayList<>(groups.values());
    }

    /* ======================================================================
     * 6. PATTERN: TOP K FREQUENT  (LC 347) — freq map + BUCKET SORT = O(n)
     * Bucket index = frequency. Max frequency is n, so n+1 buckets suffice.
     * ====================================================================== */
    static int[] topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> freq = new HashMap<>();
        for (int n : nums) freq.merge(n, 1, Integer::sum);

        @SuppressWarnings("unchecked")
        List<Integer>[] buckets = new List[nums.length + 1];
        for (var e : freq.entrySet()) {
            int f = e.getValue();
            if (buckets[f] == null) buckets[f] = new ArrayList<>();
            buckets[f].add(e.getKey());
        }

        int[] res = new int[k];
        int idx = 0;
        for (int f = buckets.length - 1; f >= 1 && idx < k; f--) {   // walk high -> low
            if (buckets[f] == null) continue;
            for (int v : buckets[f]) { if (idx == k) break; res[idx++] = v; }
        }
        return res;
    }

    /* ======================================================================
     * 7. PATTERN: LONGEST CONSECUTIVE SEQUENCE  (LC 128) — O(n) with a HashSet
     * Insight: only START counting from a number n where n-1 is ABSENT.
     * That guarantees each element is visited O(1) times overall.
     * ====================================================================== */
    static int longestConsecutive(int[] nums) {
        Set<Integer> set = new HashSet<>();
        for (int n : nums) set.add(n);

        int best = 0;
        for (int n : set) {
            if (set.contains(n - 1)) continue;   // not a sequence start, skip
            int len = 1;
            while (set.contains(n + len)) len++;
            best = Math.max(best, len);
        }
        return best;
    }

    /* ======================================================================
     * 8. PATTERN: LRU CACHE  (LC 146) — LinkedHashMap in access order
     * 3rd ctor arg `accessOrder=true` moves a key to the end on every get().
     * Override removeEldestEntry to auto-evict.
     * ====================================================================== */
    static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private final int cap;
        LRUCache(int cap) {
            super(16, 0.75f, true);      // <-- true = ACCESS order, not insertion order
            this.cap = cap;
        }
        @Override protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > cap;          // evict LRU automatically
        }
    }

    /* ================================================================== */
    public static void main(String[] args) {
        System.out.println("--- custom MyHashMap (watch the resize) ---");
        MyHashMap<String, Integer> m = new MyHashMap<>(4);
        m.put("a", 1); m.put("b", 2); m.put("c", 3); m.put("d", 4); m.put("e", 5);
        System.out.println(m + "  size=" + m.size());
        System.out.println("get(c)=" + m.get("c") + " remove(c)=" + m.remove("c")
                + " containsKey(c)=" + m.containsKey("c"));

        System.out.println("\n--- custom key with equals/hashCode ---");
        Map<Point, String> pm = new HashMap<>();
        pm.put(new Point(1, 2), "origin-ish");
        System.out.println("lookup with a NEW but equal Point: " + pm.get(new Point(1, 2)));
        System.out.println("record Cell equality: " + new Cell(1,2).equals(new Cell(1,2)));

        System.out.println("\n--- API idioms ---");
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : "mississippi".toCharArray()) freq.merge(c, 1, Integer::sum);
        System.out.println("merge counting : " + freq);

        Map<Integer, List<String>> byLen = new HashMap<>();
        for (String w : List.of("hi", "to", "cat", "dog", "bird"))
            byLen.computeIfAbsent(w.length(), k -> new ArrayList<>()).add(w);
        System.out.println("computeIfAbsent: " + byLen);

        System.out.println("\n--- TreeMap navigation ---");
        TreeMap<Integer, String> tm = new TreeMap<>(Map.of(10,"a",20,"b",30,"c"));
        System.out.println("floorKey(25)=" + tm.floorKey(25) + " ceilingKey(25)=" + tm.ceilingKey(25)
                + " firstEntry=" + tm.firstEntry() + " headMap(30)=" + tm.headMap(30));

        System.out.println("\n--- patterns ---");
        System.out.println("twoSum([2,7,11,15],9) = " + Arrays.toString(twoSum(new int[]{2,7,11,15}, 9)));
        System.out.println("groupAnagrams         = " +
                groupAnagrams(new String[]{"eat","tea","tan","ate","nat","bat"}));
        System.out.println("topKFrequent k=2      = " +
                Arrays.toString(topKFrequent(new int[]{1,1,1,2,2,3}, 2)));
        System.out.println("longestConsecutive    = " +
                longestConsecutive(new int[]{100,4,200,1,3,2}));

        System.out.println("\n--- LRU cache (cap=2) ---");
        LRUCache<Integer,String> lru = new LRUCache<>(2);
        lru.put(1,"one"); lru.put(2,"two");
        lru.get(1);                    // touch 1 -> 2 becomes the eldest
        lru.put(3,"three");            // evicts 2
        System.out.println(lru);
    }
}

/* ==========================================================================
 * PRACTICE — BLIND 75 / NEETCODE (hashing), in the order you should do them
 * ==========================================================================
 *   LC 217  Contains Duplicate                    easy   HashSet
 *   LC 1    Two Sum                               easy   value->index
 *   LC 242  Valid Anagram                         easy   int[26]
 *   LC 49   Group Anagrams                        med    canonical key
 *   LC 347  Top K Frequent Elements               med    bucket sort O(n)
 *   LC 271  Encode and Decode Strings             med    length-prefix protocol
 *   LC 36   Valid Sudoku                          med    3 sets of sets
 *   LC 128  Longest Consecutive Sequence          med    only start at n-1 absent
 *   LC 3    Longest Substring w/o Repeating       med    window + last-index map
 *   LC 424  Longest Repeating Char Replacement    med    window + freq + maxCount
 *   LC 76   Minimum Window Substring              hard   window + need/have counts
 *   LC 133  Clone Graph                           med    map old node -> new node
 *   LC 380  Insert Delete GetRandom O(1)          med    map + list + swap-last
 *   LC 146  LRU Cache                             med    LinkedHashMap or map+DLL
 *   LC 560  Subarray Sum Equals K                 med    prefix-sum -> count map
 *
 * Self-test questions + answers: see ./README.md
 * ========================================================================== */
