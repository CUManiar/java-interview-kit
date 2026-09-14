/* Binary heap, plain int[], no <T>. Run: java HeapPlain.java
 * For the mental model / heapsort / Dijkstra-Prim connection / full theory, see
 * ../../04-trees-heaps-tries/02-heap/README.md — this file is purely "watch it work." */
public class HeapPlain {

    /* ======================================================================
     * 1. MIN-HEAP — array-backed complete binary tree.
     * "Complete" means every level is fully filled left-to-right except
     * possibly the last, which is filled left-to-right with NO gaps. That's
     * the only reason parent(i)=(i-1)/2, left(i)=2i+1, right(i)=2i+2 work:
     * every node's children live at a fixed, predictable offset from its
     * own index, so the tree needs zero pointers — just a flat array.
     * A non-complete tree (e.g. a BST) has no such fixed offset, which is
     * why BSTs need actual Node/left/right pointers instead.
     * ====================================================================== */
    static class MinHeap {
        int[] arr = new int[16];
        int size = 0;

        private int parent(int i) { return (i - 1) / 2; }
        private int left(int i) { return 2 * i + 1; }
        private int right(int i) { return 2 * i + 2; }

        private void swap(int i, int j) { int t = arr[i]; arr[i] = arr[j]; arr[j] = t; }

        void push(int val) {
            if (size == arr.length) resize();
            arr[size] = val; // insert at the next open leaf slot (end of array), keeps tree complete
            siftUp(size);
            size++;
        }

        private void siftUp(int i) {
            while (i > 0 && arr[i] < arr[parent(i)]) { // smaller than parent -> violates min-heap property
                swap(i, parent(i));
                i = parent(i);
            }
        }

        int pop() {
            if (size == 0) throw new IllegalStateException("pop from empty heap");
            int top = arr[0];
            size--;
            arr[0] = arr[size]; // move last leaf to root - only way to remove a slot and stay complete
            siftDown(0);
            return top;
        }

        private void siftDown(int i) {
            while (true) {
                int smallest = i, l = left(i), r = right(i);
                if (l < size && arr[l] < arr[smallest]) smallest = l;
                if (r < size && arr[r] < arr[smallest]) smallest = r; // check both children, pick smaller
                if (smallest == i) break; // heap property restored
                swap(i, smallest);
                i = smallest;
            }
        }

        int peek() {
            if (size == 0) throw new IllegalStateException("peek on empty heap");
            return arr[0];
        }

        boolean isEmpty() { return size == 0; }

        private void resize() {
            int[] bigger = new int[arr.length * 2];
            System.arraycopy(arr, 0, bigger, 0, size);
            arr = bigger;
        }
    }

    /* ======================================================================
     * 2. HEAPIFY — build a heap from an arbitrary array in O(n), not O(nlogn).
     * Inserting n values one at a time (n calls to push/siftUp) costs
     * O(n log n) because siftUp can travel the full height on every call.
     * heapify instead starts at the LAST NON-LEAF node (index n/2-1) and
     * sifts DOWN, moving backwards to index 0. Why that's only O(n) total:
     * roughly half the nodes are leaves (0 sift-down distance), half of
     * the rest are one level up (distance <= 1), and so on - the number
     * of nodes able to travel distance d shrinks geometrically as d grows.
     * Summing (count at each level) * (max sift distance at that level)
     * over the whole tree converges to O(n), not O(n log n) - the cost is
     * dominated by the many cheap bottom-level sifts, not the few
     * expensive ones near the root.
     * ====================================================================== */
    static int[] heapify(int[] input) {
        int[] arr = input.clone();
        int n = arr.length;
        for (int i = n / 2 - 1; i >= 0; i--) { // n/2-1 = index of the last node with a child
            siftDownStatic(arr, n, i);
        }
        return arr;
    }

    private static void siftDownStatic(int[] arr, int size, int i) {
        while (true) {
            int smallest = i, l = 2 * i + 1, r = 2 * i + 2;
            if (l < size && arr[l] < arr[smallest]) smallest = l;
            if (r < size && arr[r] < arr[smallest]) smallest = r;
            if (smallest == i) break;
            int t = arr[i]; arr[i] = arr[smallest]; arr[smallest] = t;
            i = smallest;
        }
    }

    /* ======================================================================
     * 3. MAX-HEAP — identical mechanics to MinHeap, only the two comparisons
     * are flipped (< becomes >). That flip is the ENTIRE difference between
     * a min-heap and a max-heap; every other line is copy-pasted unchanged.
     * ====================================================================== */
    static class MaxHeap {
        int[] arr = new int[16];
        int size = 0;

        private int parent(int i) { return (i - 1) / 2; }
        private int left(int i) { return 2 * i + 1; }
        private int right(int i) { return 2 * i + 2; }

        private void swap(int i, int j) { int t = arr[i]; arr[i] = arr[j]; arr[j] = t; }

        void push(int val) {
            if (size == arr.length) resize();
            arr[size] = val;
            siftUp(size);
            size++;
        }

        private void siftUp(int i) {
            while (i > 0 && arr[i] > arr[parent(i)]) { // only change from MinHeap: > instead of <
                swap(i, parent(i));
                i = parent(i);
            }
        }

        int pop() {
            if (size == 0) throw new IllegalStateException("pop from empty heap");
            int top = arr[0];
            size--;
            arr[0] = arr[size];
            siftDown(0);
            return top;
        }

        private void siftDown(int i) {
            while (true) {
                int largest = i, l = left(i), r = right(i);
                if (l < size && arr[l] > arr[largest]) largest = l; // only change: > instead of <
                if (r < size && arr[r] > arr[largest]) largest = r;
                if (largest == i) break;
                swap(i, largest);
                i = largest;
            }
        }

        int peek() {
            if (size == 0) throw new IllegalStateException("peek on empty heap");
            return arr[0];
        }

        boolean isEmpty() { return size == 0; }

        private void resize() {
            int[] bigger = new int[arr.length * 2];
            System.arraycopy(arr, 0, bigger, 0, size);
            arr = bigger;
        }
    }

    /* ======================================================================
     * 4. KTH LARGEST — a size-k MIN-heap, not a size-n max-heap.
     * Efficiency: a full max-heap of all n elements (or sorting) costs
     * O(n log n). Keeping the heap capped at size k costs only O(n log k):
     * each of the n elements does at most one O(log k) push/pop against a
     * heap that never grows past k, and k is often << n ("top 10 of a
     * million").
     * The counterintuitive part: to find the kth LARGEST you use a MIN-heap
     * (not a max-heap). The min-heap holds the k largest values seen SO FAR,
     * and its root is deliberately the SMALLEST of that group - which is
     * exactly the kth largest overall once all n elements are processed,
     * and is also exactly the value you want to evict first when a bigger
     * one arrives. A max-heap of size k would instead surface the single
     * biggest value at the root, which tells you nothing about the boundary
     * between "in the top k" and "not."
     * ====================================================================== */
    static int kthLargest(int[] nums, int k) {
        MinHeap heap = new MinHeap();
        for (int num : nums) {
            heap.push(num);
            if (heap.size > k) heap.pop(); // evict the current smallest of the top-(k+1) -> keeps exactly k
        }
        return heap.peek(); // root of a size-k min-heap = smallest of the k largest = kth largest overall
    }

    public static void main(String[] args) {
        System.out.println("=== 1. MinHeap: push then pop-all, expect ascending order ===");
        MinHeap minHeap = new MinHeap();
        int[] toPush = {5, 3, 8, 1, 9, 2, 7};
        for (int v : toPush) {
            minHeap.push(v);
            System.out.println("push(" + v + ")  peek()=" + minHeap.peek());
        }
        StringBuilder minOut = new StringBuilder();
        while (!minHeap.isEmpty()) minOut.append(minHeap.pop()).append(" ");
        System.out.println("popped order: " + minOut.toString().trim());

        System.out.println("\n=== 2. MaxHeap: push then pop-all, expect descending order ===");
        MaxHeap maxHeap = new MaxHeap();
        for (int v : toPush) {
            maxHeap.push(v);
            System.out.println("push(" + v + ")  peek()=" + maxHeap.peek());
        }
        StringBuilder maxOut = new StringBuilder();
        while (!maxHeap.isEmpty()) maxOut.append(maxHeap.pop()).append(" ");
        System.out.println("popped order: " + maxOut.toString().trim());

        System.out.println("\n=== 3. heapify: build from an arbitrary unsorted array in O(n) ===");
        int[] unsorted = {9, 4, 7, 1, 3, 8, 2, 6, 5};
        System.out.println("input array:      " + java.util.Arrays.toString(unsorted));
        int[] heapArr = heapify(unsorted);
        System.out.println("heap-ordered arr: " + java.util.Arrays.toString(heapArr));
        // drain it with the same siftDown logic to prove the heap property actually holds
        MinHeap drain = new MinHeap();
        drain.arr = heapArr;
        drain.size = heapArr.length;
        StringBuilder heapifyOut = new StringBuilder();
        while (!drain.isEmpty()) heapifyOut.append(drain.pop()).append(" ");
        System.out.println("popped order:     " + heapifyOut.toString().trim() + "  (must be ascending)");

        System.out.println("\n=== 4. kthLargest via size-k min-heap ===");
        int[] nums = {3, 2, 1, 5, 6, 4};
        int k = 2;
        System.out.println("nums=" + java.util.Arrays.toString(nums) + "  k=" + k);
        System.out.println("kthLargest = " + kthLargest(nums, k) + "  (expected 5 - sorted desc: 6,5,4,3,2,1)");

        int[] nums2 = {3, 2, 3, 1, 2, 4, 5, 5, 6};
        int k2 = 4;
        System.out.println("nums=" + java.util.Arrays.toString(nums2) + "  k=" + k2);
        System.out.println("kthLargest = " + kthLargest(nums2, k2) + "  (expected 4 - sorted desc: 6,5,5,4,3,3,2,2,1)");
    }
}
