import java.util.*;

/* ==========================================================================
 * DS: LINKED LIST  (singly, doubly, circular)
 * Run: java LinkedListNotes.java
 * See ./README.md for the mental model, the array-vs-linked-list comparison,
 * and the 3 core techniques (dummy head, two pointers, prev/curr/next
 * reversal) — not repeated here.
 * ========================================================================== */
public class LinkedListNotes {

    /* The canonical LeetCode node. Non-generic on purpose - that's how it's given. */
    static class ListNode {
        int val; ListNode next;
        ListNode(int v) { val = v; }
        ListNode(int v, ListNode n) { val = v; next = n; }
    }

    /* ======================================================================
     * 1. GENERIC SINGLY LINKED LIST IMPLEMENTATION
     * ====================================================================== */
    static class SinglyLinkedList<T> {
        private static class Node<T> {
            T data; Node<T> next;
            Node(T d) { data = d; }
        }

        private Node<T> head, tail;   // tail pointer makes append O(1) instead of O(n)
        private int size;

        void append(T val) {                       // O(1) thanks to tail
            Node<T> n = new Node<>(val);
            if (head == null) head = tail = n;
            else { tail.next = n; tail = n; }
            size++;
        }

        void prepend(T val) {                      // O(1)
            Node<T> n = new Node<>(val);
            n.next = head;
            head = n;
            if (tail == null) tail = n;            // was empty
            size++;
        }

        boolean delete(T val) {                    // O(n)
            if (head == null) return false;
            if (Objects.equals(head.data, val)) {  // deleting the head
                head = head.next;
                if (head == null) tail = null;     // list became empty
                size--;
                return true;
            }
            for (Node<T> cur = head; cur.next != null; cur = cur.next) {
                if (Objects.equals(cur.next.data, val)) {
                    if (cur.next == tail) tail = cur;   // deleting the tail
                    cur.next = cur.next.next;           // splice out
                    size--;
                    return true;
                }
            }
            return false;
        }

        boolean contains(T val) {
            for (Node<T> c = head; c != null; c = c.next)
                if (Objects.equals(c.data, val)) return true;
            return false;
        }

        int size() { return size; }

        @Override public String toString() {
            StringJoiner sj = new StringJoiner(" -> ", "", " -> null");
            for (Node<T> c = head; c != null; c = c.next) sj.add(String.valueOf(c.data));
            return size == 0 ? "null" : sj.toString();
        }
    }

    /* ======================================================================
     * 2. DOUBLY LINKED LIST — the engine inside an LRU cache
     * Sentinel head/tail nodes remove every null check. Do this.
     * ====================================================================== */
    static class DoublyLinkedList<T> {
        static class Node<T> {
            T data; Node<T> prev, next;
            Node(T d) { data = d; }
        }

        private final Node<T> head = new Node<>(null);  // sentinel, never removed
        private final Node<T> tail = new Node<>(null);  // sentinel, never removed
        private int size;

        DoublyLinkedList() { head.next = tail; tail.prev = head; }

        /* insert `n` right after `head` - i.e. at the front */
        Node<T> addFirst(T val) {
            Node<T> n = new Node<>(val);
            n.next = head.next;
            n.prev = head;
            head.next.prev = n;
            head.next = n;
            size++;
            return n;      // returning the node is what lets a cache do O(1) removal
        }

        /* O(1) removal given the node itself - impossible in a singly linked list */
        void remove(Node<T> n) {
            n.prev.next = n.next;
            n.next.prev = n.prev;
            n.prev = n.next = null;   // help GC / prevent stale traversal
            size--;
        }

        T removeLast() {
            if (size == 0) throw new NoSuchElementException();
            Node<T> last = tail.prev;
            remove(last);
            return last.data;
        }

        int size() { return size; }

        @Override public String toString() {
            StringJoiner sj = new StringJoiner(" <-> ", "[", "]");
            for (Node<T> c = head.next; c != tail; c = c.next) sj.add(String.valueOf(c.data));
            return sj.toString();
        }
    }

    /* ======================================================================
     * 3. JAVA API — java.util.LinkedList implements both List and Deque, but
     * interview problems hand you a raw ListNode and expect pointer surgery
     * instead. Full syntax reference lives in ../../java-api-examples.md.
     * ====================================================================== */

    /* ======================================================================
     * 4. REVERSE A LINKED LIST  (LC 206) — iterative AND recursive
     * ====================================================================== */
    static ListNode reverseIterative(ListNode head) {
        ListNode prev = null, curr = head;
        while (curr != null) {
            ListNode temp = curr.next;   // 1. save
            curr.next = prev;            // 2. flip
            prev = curr;                 // 3. advance prev
            curr = temp;                 // 4. advance curr
        }
        return prev;                     // prev is the new head
    }

    /* Recursion: reverse the tail, then make my successor point back at me.
     *   head=[1] -> rest=[2]->[3]
     *   newHead comes back as [3]; head.next is still [2]
     *   head.next.next = head   =>  [2] -> [1]
     *   head.next = null        =>  [1] -> null   (else you build a 2-cycle)
     * Space O(n) from the call stack - mention this trade-off. */
    static ListNode reverseRecursive(ListNode head) {
        if (head == null || head.next == null) return head;   // base: 0 or 1 node
        ListNode newHead = reverseRecursive(head.next);
        head.next.next = head;
        head.next = null;
        return newHead;
    }

    /* ======================================================================
     * 5. TWO POINTERS: MIDDLE  (LC 876)
     * fast moves 2, slow moves 1 -> when fast hits the end, slow is at the middle.
     * Even length [1,2,3,4]: this returns the SECOND middle (3).
     * For the FIRST middle, loop on (fast.next != null && fast.next.next != null).
     * ====================================================================== */
    static ListNode middleNode(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }

    /* ======================================================================
     * 6. CYCLE DETECTION — Floyd's tortoise & hare  (LC 141 / 142)
     *
     *   If there's a cycle, fast gains 1 step per iteration on slow, so it must
     *   eventually land on it. If no cycle, fast falls off the end.
     *
     *   FINDING THE ENTRY (LC 142) — the proof, compressed:
     *     Let L = distance head->cycleStart, C = cycle length, x = distance
     *     cycleStart->meeting point.
     *     slow travelled L+x, fast travelled 2(L+x) and also L+x+nC
     *     => L+x = nC => L = nC - x.
     *     So walking L steps from head, and L steps from the meeting point,
     *     lands both on the cycle start. Hence: reset one pointer to head,
     *     advance both by 1, they meet at the entry.
     * ====================================================================== */
    static boolean hasCycle(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) return true;
        }
        return false;
    }

    static ListNode detectCycleStart(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) {                     // met inside the cycle
                ListNode p = head;
                while (p != slow) { p = p.next; slow = slow.next; }
                return p;                           // the entry node
            }
        }
        return null;
    }

    /* ======================================================================
     * 7. DUMMY HEAD PATTERN: merge two sorted lists  (LC 21)
     * The dummy means you never write "if (result == null) result = ..." branches.
     * ====================================================================== */
    static ListNode mergeTwoLists(ListNode a, ListNode b) {
        ListNode dummy = new ListNode(0), tail = dummy;
        while (a != null && b != null) {
            if (a.val <= b.val) { tail.next = a; a = a.next; }
            else                { tail.next = b; b = b.next; }
            tail = tail.next;
        }
        tail.next = (a != null) ? a : b;   // attach whatever remains
        return dummy.next;                 // skip the dummy
    }

    /* ======================================================================
     * 8. REMOVE Nth FROM END  (LC 19) — gap of n between two pointers
     * Dummy handles "remove the head" (n == length) for free.
     * ====================================================================== */
    static ListNode removeNthFromEnd(ListNode head, int n) {
        ListNode dummy = new ListNode(0, head);
        ListNode fast = dummy, slow = dummy;
        for (int i = 0; i < n; i++) fast = fast.next;     // open a gap of n
        while (fast.next != null) { fast = fast.next; slow = slow.next; }
        slow.next = slow.next.next;                        // slow is just before the target
        return dummy.next;
    }

    /* ======================================================================
     * 9. REORDER LIST  (LC 143) — split + reverse + weave. Combines everything.
     *   [1,2,3,4,5]  ->  [1,5,2,4,3]
     * ====================================================================== */
    static void reorderList(ListNode head) {
        if (head == null || head.next == null) return;

        // a) find the middle (first-middle variant so the halves split cleanly)
        ListNode slow = head, fast = head;
        while (fast.next != null && fast.next.next != null) {
            slow = slow.next; fast = fast.next.next;
        }

        // b) reverse the second half, cut the link
        ListNode second = reverseIterative(slow.next);
        slow.next = null;

        // c) weave: grab both "rest of list" pointers BEFORE rewiring next,
        // or first.next = second (etc.) would overwrite the only reference
        // to the remainder of that half and strand it.
        ListNode first = head;
        while (second != null) {
            ListNode t1 = first.next, t2 = second.next;
            first.next = second;
            second.next = t1;
            first = t1; second = t2;
        }
    }

    /* helpers */
    static ListNode build(int... vals) {
        ListNode dummy = new ListNode(0), t = dummy;
        for (int v : vals) { t.next = new ListNode(v); t = t.next; }
        return dummy.next;
    }
    static String str(ListNode h) {
        StringJoiner sj = new StringJoiner(" -> ", "", " -> null");
        for (ListNode c = h; c != null; c = c.next) sj.add(String.valueOf(c.val));
        return sj.toString();
    }

    /* ================================================================== */
    public static void main(String[] args) {
        System.out.println("--- SinglyLinkedList ---");
        SinglyLinkedList<Integer> sl = new SinglyLinkedList<>();
        sl.append(1); sl.append(2); sl.append(3); sl.prepend(0);
        System.out.println(sl + "  size=" + sl.size());
        sl.delete(2);
        System.out.println("after delete(2): " + sl + " contains(3)=" + sl.contains(3));

        System.out.println("\n--- DoublyLinkedList (sentinels) ---");
        DoublyLinkedList<String> dl = new DoublyLinkedList<>();
        dl.addFirst("c");
        DoublyLinkedList.Node<String> nodeB = dl.addFirst("b");
        dl.addFirst("a");
        System.out.println(dl);
        dl.remove(nodeB);                       // O(1) - we held the reference
        System.out.println("after O(1) remove(b): " + dl + " removeLast=" + dl.removeLast());

        System.out.println("\n--- reverse ---");
        System.out.println("iterative: " + str(reverseIterative(build(1,2,3,4,5))));
        System.out.println("recursive: " + str(reverseRecursive(build(1,2,3,4,5))));

        System.out.println("\n--- two pointers ---");
        System.out.println("middle of 1..5 = " + middleNode(build(1,2,3,4,5)).val);
        System.out.println("removeNthFromEnd(1..5, n=2) = " + str(removeNthFromEnd(build(1,2,3,4,5), 2)));

        System.out.println("\n--- cycle ---");
        ListNode c = build(1,2,3,4);
        c.next.next.next.next = c.next;          // 4 -> 2, cycle entry is node 2
        System.out.println("hasCycle=" + hasCycle(c) + " entry=" + detectCycleStart(c).val);

        System.out.println("\n--- merge / reorder ---");
        System.out.println("merge [1,3,5]+[2,4,6] = " + str(mergeTwoLists(build(1,3,5), build(2,4,6))));
        ListNode r = build(1,2,3,4,5);
        reorderList(r);
        System.out.println("reorder 1..5          = " + str(r));
    }
}

/* ==========================================================================
 * PRACTICE — BLIND 75 / NEETCODE (linked list)
 * ==========================================================================
 *   LC 206  Reverse Linked List                   easy   do BOTH ways
 *   LC 21   Merge Two Sorted Lists                easy   dummy head
 *   LC 141  Linked List Cycle                     easy   Floyd
 *   LC 142  Linked List Cycle II                  med    Floyd + reset to head
 *   LC 876  Middle of the Linked List             easy   slow/fast
 *   LC 19   Remove Nth Node From End              med    gap of n + dummy
 *   LC 143  Reorder List                          med    split+reverse+weave
 *   LC 2    Add Two Numbers                       med    carry + dummy
 *   LC 138  Copy List with Random Pointer         med    map old->new, or interleave
 *   LC 23   Merge k Sorted Lists                  hard   PQ or divide & conquer
 *   LC 25   Reverse Nodes in k-Group              hard   reverse + relink
 *   LC 146  LRU Cache                             med    HashMap + doubly linked list
 *   LC 287  Find the Duplicate Number             med    Floyd on an ARRAY
 *
 * Self-test questions + answers: see ./README.md
 * ========================================================================== */
