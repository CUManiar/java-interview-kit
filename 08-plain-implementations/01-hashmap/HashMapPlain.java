import java.util.LinkedList;

/* HashMap, three ways — plain String->Integer, no <K,V>. Run: java HashMapPlain.java
 * For the mental model / bit-mask math / treeification theory, see
 * ../../02-hashing/01-hashmap/README.md — this file is purely "watch it work." */
public class HashMapPlain {

    /* ======================================================================
     * 1. CHAINING — hand-rolled singly linked list per bucket
     * ====================================================================== */
    static class ChainedHashMap {
        static class Node {
            String key; Integer val; Node next;
            Node(String k, Integer v, Node next) { key = k; val = v; this.next = next; }
        }

        private Node[] buckets;
        private int size;

        ChainedHashMap(int capacity) { buckets = new Node[capacity]; }

        private int indexFor(String key) {
            return (key.hashCode() & 0x7fffffff) % buckets.length; // mask off sign bit first
        }

        void put(String key, Integer val) {
            int i = indexFor(key);
            for (Node n = buckets[i]; n != null; n = n.next) {
                if (n.key.equals(key)) { n.val = val; return; } // overwrite
            }
            buckets[i] = new Node(key, val, buckets[i]); // prepend, O(1)
            size++;
        }

        Integer get(String key) {
            for (Node n = buckets[indexFor(key)]; n != null; n = n.next)
                if (n.key.equals(key)) return n.val;
            return null;
        }

        boolean remove(String key) {
            int i = indexFor(key);
            Node prev = null;
            for (Node n = buckets[i]; n != null; prev = n, n = n.next) {
                if (n.key.equals(key)) {
                    if (prev == null) buckets[i] = n.next; else prev.next = n.next;
                    size--;
                    return true;
                }
            }
            return false;
        }

        void printBuckets() {
            for (int i = 0; i < buckets.length; i++) {
                StringBuilder sb = new StringBuilder("  [" + i + "] ");
                for (Node n = buckets[i]; n != null; n = n.next) sb.append(n.key).append("=").append(n.val).append(" -> ");
                sb.append("null");
                System.out.println(sb);
            }
        }
    }

    /* ======================================================================
     * 2. CHAINING — same idea, but each bucket is a java.util.LinkedList
     * instead of hand-rolled nodes. Slightly slower (LinkedList's own node
     * overhead + iterator allocation) but shows how you'd wire this with
     * built-ins if asked "don't write your own linked list."
     * ====================================================================== */
    static class LinkedListBucketMap {
        static class Entry {
            String key; Integer val;
            Entry(String k, Integer v) { key = k; val = v; }
        }

        @SuppressWarnings("unchecked")
        private LinkedList<Entry>[] buckets = new LinkedList[8];

        private int indexFor(String key) {
            return (key.hashCode() & 0x7fffffff) % buckets.length;
        }

        void put(String key, Integer val) {
            int i = indexFor(key);
            if (buckets[i] == null) buckets[i] = new LinkedList<>();
            for (Entry e : buckets[i]) if (e.key.equals(key)) { e.val = val; return; }
            buckets[i].addLast(new Entry(key, val)); // LinkedList gives you addLast/removeIf for free
        }

        Integer get(String key) {
            LinkedList<Entry> chain = buckets[indexFor(key)];
            if (chain == null) return null;
            for (Entry e : chain) if (e.key.equals(key)) return e.val;
            return null;
        }

        boolean remove(String key) {
            LinkedList<Entry> chain = buckets[indexFor(key)];
            return chain != null && chain.removeIf(e -> e.key.equals(key)); // no manual prev/next splicing
        }
    }

    /* ======================================================================
     * 3. OPEN ADDRESSING — linear probing, NO chaining at all.
     * On collision, walk forward to the next empty/tombstoned slot instead
     * of growing a list. Deletion needs a TOMBSTONE (not plain null), or a
     * later lookup that should skip past a "hole" will stop early and
     * wrongly report "not found."
     * ====================================================================== */
    static class LinearProbingMap {
        private static final String TOMBSTONE = "\0__DELETED__"; // sentinel, never a real key
        private String[] keys;
        private Integer[] vals;
        private int size;

        LinearProbingMap(int capacity) { keys = new String[capacity]; vals = new Integer[capacity]; }

        private int indexFor(String key) { return (key.hashCode() & 0x7fffffff) % keys.length; }

        void put(String key, Integer val) {
            int i = indexFor(key);
            int firstTombstone = -1;
            for (int probes = 0; probes < keys.length; probes++, i = (i + 1) % keys.length) {
                if (keys[i] == null) {
                    int slot = firstTombstone != -1 ? firstTombstone : i; // reuse a tombstone if we passed one
                    keys[slot] = key; vals[slot] = val; size++;
                    return;
                }
                if (keys[i] == TOMBSTONE) { if (firstTombstone == -1) firstTombstone = i; continue; }
                if (keys[i].equals(key)) { vals[i] = val; return; } // overwrite
            }
            throw new IllegalStateException("table full"); // real impls resize before this happens
        }

        Integer get(String key) {
            int i = indexFor(key);
            for (int probes = 0; probes < keys.length; probes++, i = (i + 1) % keys.length) {
                if (keys[i] == null) return null;              // true empty slot -> never inserted here
                if (keys[i] != TOMBSTONE && keys[i].equals(key)) return vals[i];
                // TOMBSTONE or a mismatched key: keep probing, don't stop
            }
            return null;
        }

        boolean remove(String key) {
            int i = indexFor(key);
            for (int probes = 0; probes < keys.length; probes++, i = (i + 1) % keys.length) {
                if (keys[i] == null) return false;
                if (keys[i] != TOMBSTONE && keys[i].equals(key)) {
                    keys[i] = TOMBSTONE; vals[i] = null; size--; // NOT null - would break later probes
                    return true;
                }
            }
            return false;
        }

        void printSlots() {
            for (int i = 0; i < keys.length; i++) {
                String label = keys[i] == null ? "empty" : keys[i] == TOMBSTONE ? "TOMBSTONE" : keys[i] + "=" + vals[i];
                System.out.println("  [" + i + "] " + label);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== 1. Chaining (hand-rolled nodes), capacity=4 ===");
        ChainedHashMap m1 = new ChainedHashMap(4);
        for (String k : new String[]{"a", "e", "i", "o", "u"}) m1.put(k, (int) k.charAt(0)); // 5 keys, 4 buckets -> forced collision
        m1.printBuckets();
        System.out.println("get(i)=" + m1.get("i") + "  remove(i)=" + m1.remove("i") + "  get(i) after=" + m1.get("i"));

        System.out.println("\n=== 2. Chaining via java.util.LinkedList buckets ===");
        LinkedListBucketMap m2 = new LinkedListBucketMap();
        m2.put("cat", 1); m2.put("dog", 2); m2.put("bird", 3);
        System.out.println("get(dog)=" + m2.get("dog") + "  remove(cat)=" + m2.remove("cat") + "  get(cat)=" + m2.get("cat"));

        System.out.println("\n=== 3. Open addressing (linear probing), capacity=5 ===");
        LinearProbingMap m3 = new LinearProbingMap(5);
        m3.put("a", 1); m3.put("f", 2); // 'a'%5 and 'f'%5 likely collide depending on hashCode -> probe forward
        m3.put("k", 3);
        m3.printSlots();
        System.out.println("remove(f)=" + m3.remove("f"));
        m3.printSlots();
        System.out.println("get(k) after removing f (probes PAST the tombstone) = " + m3.get("k"));
    }
}
