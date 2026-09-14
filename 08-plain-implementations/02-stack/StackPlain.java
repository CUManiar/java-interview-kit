/* Stack, three ways — plain Object/int, no <T>. Run: java StackPlain.java
 * For the mental model / amortized-cost proof / LC-155 writeup, see
 * ../../03-stacks-queues/01-stack/README.md — this file is purely "watch it work." */
public class StackPlain {

    /* ======================================================================
     * 1. ARRAY-BACKED — plain Object[], manual resize-on-full.
     * Doubling (not "+1 each time") is the whole trick: if you grew by a
     * fixed amount, the Nth push would need O(N) copies roughly every K
     * pushes, giving O(n^2) total. Doubling means the copy sizes form a
     * geometric series (1,2,4,8,...N) that sums to ~2N — so the cost of
     * every resize, spread ("amortized") across all n pushes, averages
     * out to O(1) per push even though any single push that triggers a
     * resize is O(n) in that instant.
     * ====================================================================== */
    static class ArrayStack {
        private Object[] data = new Object[4];
        private int size = 0;

        void push(Object val) {
            if (size == data.length) resize(data.length * 2); // full -> double, not +1
            data[size++] = val;
        }

        Object pop() {
            if (size == 0) throw new IllegalStateException("pop from empty stack");
            Object val = data[--size];
            data[size] = null; // drop the reference so the popped object can be GC'd
            return val;
        }

        Object peek() {
            if (size == 0) throw new IllegalStateException("peek on empty stack");
            return data[size - 1];
        }

        boolean isEmpty() { return size == 0; }
        int size() { return size; }
        int capacity() { return data.length; }

        private void resize(int newCapacity) {
            Object[] bigger = new Object[newCapacity];
            System.arraycopy(data, 0, bigger, 0, size); // this copy is the O(n) spike; doubling makes it rare
            data = bigger;
        }
    }

    /* ======================================================================
     * 2. LINKED-LIST-BACKED — hand-rolled singly linked Node, push/pop at head.
     * Both ops are O(1) with NO resize step at all — there's no backing array
     * to outgrow. That's the real tradeoff vs ArrayStack, not "which is
     * faster": ArrayStack is cache-friendly (contiguous memory) but pays an
     * occasional O(n) resize spike; LinkedListStack never spikes but pays
     * per-node allocation + an 8-byte "next" pointer on every element, and
     * its nodes are scattered in memory (worse cache locality).
     * ====================================================================== */
    static class LinkedListStack {
        static class Node {
            Object val; Node next;
            Node(Object val, Node next) { this.val = val; this.next = next; }
        }

        private Node head; // top of stack
        private int size = 0;

        void push(Object val) {
            head = new Node(val, head); // new node becomes head, points at old head
            size++;
        }

        Object pop() {
            if (head == null) throw new IllegalStateException("pop from empty stack"); // same contract as ArrayStack
            Object val = head.val;
            head = head.next; // old head is now unreferenced -> GC'd
            size--;
            return val;
        }

        Object peek() {
            if (head == null) throw new IllegalStateException("peek on empty stack");
            return head.val;
        }

        boolean isEmpty() { return head == null; }
        int size() { return size; }
    }

    /* ======================================================================
     * 3. MIN STACK (LC 155) — int values, O(1) push/pop/getMin.
     * The trick: a second stack that mirrors the main one but only ever
     * records "the minimum seen so far at this depth." Pushing a value
     * that's >= the current min still pushes a COPY of the current min
     * (not the new value) onto minStack, so minStack.size() always equals
     * stack.size() and every pop() has an exact matching min to discard.
     * Without that duplication you'd need to know when the min "expires,"
     * which is the bug most people write on a whiteboard.
     * ====================================================================== */
    static class MinStack {
        private final ArrayStack stack = new ArrayStack();
        private final ArrayStack minStack = new ArrayStack(); // minStack[i] = min of stack[0..i]

        void push(int val) {
            stack.push(val);
            if (minStack.isEmpty() || val < (Integer) minStack.peek()) {
                minStack.push(val); // new record low
            } else {
                minStack.push((Integer) minStack.peek()); // re-push current min, keeps stacks in lockstep
            }
        }

        int pop() {
            int val = (Integer) stack.pop();
            minStack.pop(); // discard the min-at-this-depth entry that matched the popped value
            return val;
        }

        int peek() { return (Integer) stack.peek(); }

        int getMin() { return (Integer) minStack.peek(); } // O(1): just look at the top of minStack
    }

    public static void main(String[] args) {
        System.out.println("=== 1. ArrayStack (starts at capacity=4) ===");
        ArrayStack a = new ArrayStack();
        System.out.println("initial capacity=" + a.capacity());
        for (int i = 1; i <= 6; i++) { // 6 pushes into a capacity-4 array -> forces a resize at push #5
            a.push(i);
            System.out.println("pushed " + i + "  size=" + a.size() + "  capacity=" + a.capacity());
        }
        System.out.println("peek()=" + a.peek());
        while (!a.isEmpty()) System.out.println("pop()=" + a.pop());
        try {
            a.pop(); // pop from empty -> throws, consistent with LinkedListStack below
        } catch (IllegalStateException e) {
            System.out.println("pop on empty ArrayStack threw: " + e.getMessage());
        }

        System.out.println("\n=== 2. LinkedListStack (no resizing, ever) ===");
        LinkedListStack ll = new LinkedListStack();
        for (String s : new String[]{"x", "y", "z"}) ll.push(s);
        System.out.println("peek()=" + ll.peek() + "  size=" + ll.size());
        while (!ll.isEmpty()) System.out.println("pop()=" + ll.pop());
        try {
            ll.pop(); // same consistent contract: throw with a clear message, never a silent sentinel
        } catch (IllegalStateException e) {
            System.out.println("pop on empty LinkedListStack threw: " + e.getMessage());
        }

        System.out.println("\n=== 3. MinStack — min changes as we push down and pop back up ===");
        MinStack ms = new MinStack();
        int[] pushes = {5, 3, 7, 2, 8};
        for (int v : pushes) {
            ms.push(v);
            System.out.println("push(" + v + ")  top=" + ms.peek() + "  getMin()=" + ms.getMin());
        }
        for (int i = 0; i < pushes.length; i++) {
            System.out.println("pop()=" + ms.pop() + "  getMin() now=" + (i == pushes.length - 1 ? "N/A (empty)" : String.valueOf(ms.getMin())));
        }
    }
}
