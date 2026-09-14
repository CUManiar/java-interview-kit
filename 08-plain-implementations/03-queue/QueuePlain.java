/* Queue, three ways — plain int values, no <T>. Run: java QueuePlain.java
 * Progression: naive fixed array (watch it break) -> circular array (fixed) -> linked list.
 * For the mental model / amortized-cost theory, see
 * ../../03-linear-structures/02-queue/README.md — this file is purely "watch it work." */
public class QueuePlain {

    /* ======================================================================
     * 1. NAIVE ARRAY QUEUE — front and rear only ever move forward, never
     * wrap around. Bug: once rear reaches capacity, enqueue refuses more
     * work even if front has advanced and left plenty of slots empty at
     * the start of the array. The array's *physical* end is treated as the
     * queue's logical end, forever.
     * ====================================================================== */
    static class NaiveArrayQueue {
        private int[] data;
        private int front = 0; // index of next element to dequeue
        private int rear = 0;  // index of next FREE slot to enqueue into

        NaiveArrayQueue(int capacity) { data = new int[capacity]; }

        boolean isEmpty() { return front == rear; }

        boolean enqueue(int val) {
            if (rear == data.length) return false; // "full" -- even if front > 0 and slots [0, front) are dead space
            data[rear++] = val;
            return true;
        }

        Integer dequeue() {
            if (isEmpty()) return null;
            return data[front++]; // slot data[front-1] is now permanently unusable, never reclaimed
        }
    }

    /* ======================================================================
     * 2. CIRCULAR ARRAY QUEUE — same fixed int[], but front/rear wrap via
     * modulo, so a dequeued slot near index 0 gets reused once rear wraps
     * around. Fixes the NaiveArrayQueue bug exactly.
     *
     * Ambiguity this design must solve: with only front/rear indices, an
     * empty queue and a full queue can both look like "front == rear" (an
     * empty queue starts that way, and a full queue wraps back to it).
     * Classic fix: sacrifice one slot. The queue is considered full one
     * slot early, when (rear + 1) % capacity == front, so front == rear
     * unambiguously means "empty" and true capacity is (data.length - 1).
     * ====================================================================== */
    static class CircularArrayQueue {
        private int[] data;
        private int front = 0;
        private int rear = 0;

        CircularArrayQueue(int capacity) { data = new int[capacity + 1]; } // +1 for the sacrificed slot

        boolean isEmpty() { return front == rear; }

        boolean isFull() { return (rear + 1) % data.length == front; } // one slot short of true wraparound

        boolean enqueue(int val) {
            if (isFull()) return false;
            data[rear] = val;
            rear = (rear + 1) % data.length; // wrap: this is the entire fix over the naive version
            return true;
        }

        Integer dequeue() {
            if (isEmpty()) return null;
            int val = data[front];
            front = (front + 1) % data.length; // freed slot becomes reusable once rear wraps back to it
            return val;
        }
    }

    /* ======================================================================
     * 3. LINKED LIST QUEUE — hand-rolled singly linked Node with both front
     * and rear pointers, so enqueue (append at rear) and dequeue (remove
     * from front) are both O(1) with no capacity limit and no wraparound
     * math at all.
     *
     * Classic bug this design invites: when the last element is dequeued,
     * front becomes null but a careless implementation leaves rear pointing
     * at the now-detached old node (a dangling/stale reference). The node
     * itself is harmless garbage, but the NEXT enqueue would append after
     * rear and never relink front -- silently corrupting the queue. Fix:
     * explicitly null out rear whenever dequeue empties the queue.
     * ====================================================================== */
    static class LinkedListQueue {
        static class Node {
            int val;
            Node next;
            Node(int val) { this.val = val; }
        }

        private Node front;
        private Node rear;

        boolean isEmpty() { return front == null; }

        void enqueue(int val) {
            Node n = new Node(val);
            if (rear == null) front = rear = n; // first element: front and rear both point at it
            else { rear.next = n; rear = n; }    // append after old rear, then advance rear
        }

        Integer dequeue() {
            if (isEmpty()) return null;
            int val = front.val;
            front = front.next;
            if (front == null) rear = null; // THE fix: without this, rear would dangle on the removed node
            return val;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== 1. NaiveArrayQueue (capacity=5) -- watch it break ===");
        NaiveArrayQueue naive = new NaiveArrayQueue(5);
        // Churn the front of the queue: enqueue+dequeue 3 times so front advances to 3,
        // leaving slots [0,1,2] logically free but never reachable again.
        for (int i = 0; i < 3; i++) {
            naive.enqueue(i);
            naive.dequeue();
        }
        System.out.println("after 3 enqueue/dequeue cycles: front=" + naive.front + " rear=" + naive.rear
                + " (3 slots at the front are dead space, but rear doesn't know that)");
        int accepted = 0;
        for (int i = 100; i < 100 + 5; i++) { // try to push 5 more values into a "capacity 5" queue
            if (naive.enqueue(i)) accepted++;
            else {
                System.out.println("enqueue(" + i + ") REJECTED -- \"full\" at front=" + naive.front
                        + " rear=" + naive.rear + " capacity=" + naive.data.length
                        + ", even though only " + (naive.rear - naive.front) + " slots are actually in use");
                break;
            }
        }
        System.out.println("only accepted " + accepted + " of 5 new items despite 3 logically-free slots -- this is the bug");

        System.out.println("\n=== 2. CircularArrayQueue (capacity=5) -- same operation sequence, now succeeds ===");
        CircularArrayQueue circ = new CircularArrayQueue(5);
        for (int i = 0; i < 3; i++) {
            circ.enqueue(i);
            circ.dequeue();
        }
        System.out.println("after 3 enqueue/dequeue cycles: front=" + circ.front + " rear=" + circ.rear);
        int circAccepted = 0;
        for (int i = 100; i < 100 + 5; i++) {
            if (circ.enqueue(i)) circAccepted++;
        }
        System.out.println("accepted " + circAccepted + " of 5 new items (all of them) -- wraparound reused the freed slots");
        System.out.println("isFull()=" + circ.isFull() + " (true capacity is data.length-1 = " + (circ.data.length - 1) + " because one slot is sacrificed)");
        StringBuilder drained = new StringBuilder();
        while (!circ.isEmpty()) drained.append(circ.dequeue()).append(" ");
        System.out.println("drained in order: " + drained.toString().trim());

        System.out.println("\n=== 3. LinkedListQueue -- no capacity limit, drain to empty, then reuse ===");
        LinkedListQueue ll = new LinkedListQueue();
        ll.enqueue(1); ll.enqueue(2); ll.enqueue(3);
        System.out.println("dequeue=" + ll.dequeue() + " dequeue=" + ll.dequeue() + " dequeue=" + ll.dequeue());
        System.out.println("queue now empty: isEmpty()=" + ll.isEmpty() + " (front and rear both null -- no dangling rear)");
        // If rear weren't nulled out above, this enqueue would attach to the stale rear
        // instead of becoming the new front, and front would still read null forever.
        ll.enqueue(42);
        System.out.println("enqueue(42) after full drain, dequeue=" + ll.dequeue() + " -- proves rear was correctly reset");
    }
}
