import java.util.*;

/* ==========================================================================
 * DS: HEAP / PRIORITY QUEUE
 * Run: java HeapNotes.java
 * See ./README.md for the heap property, array representation (parent/left/
 * right index formulas), sift up/down, complexity, and heap-vs-BST — not
 * repeated here.
 * ========================================================================== */
public class HeapNotes {

    /* ======================================================================
     * 1. IMPLEMENTATION — generic binary heap with a pluggable Comparator
     * Passing a Comparator means ONE class serves as both min- and max-heap.
     * ====================================================================== */
    static class BinaryHeap<T> {
        private final List<T> a = new ArrayList<>();
        private final Comparator<? super T> cmp;      // PECS: it only consumes T

        BinaryHeap(Comparator<? super T> cmp) { this.cmp = cmp; }

        /* Natural-order min-heap factory for Comparable types. */
        static <T extends Comparable<? super T>> BinaryHeap<T> minHeap() {
            return new BinaryHeap<>(Comparator.naturalOrder());
        }
        static <T extends Comparable<? super T>> BinaryHeap<T> maxHeap() {
            return new BinaryHeap<>(Comparator.reverseOrder());
        }

        /* Build from an existing collection in O(n), not O(n log n). */
        BinaryHeap<T> heapify(Collection<? extends T> src) {
            a.addAll(src);
            for (int i = a.size() / 2 - 1; i >= 0; i--) siftDown(i);   // bottom-up
            return this;
        }

        void push(T val) {
            a.add(val);                 // append at the end
            siftUp(a.size() - 1);       // restore the property upward
        }

        T peek() {
            if (a.isEmpty()) throw new NoSuchElementException("heap empty");
            return a.get(0);
        }

        T pop() {
            if (a.isEmpty()) throw new NoSuchElementException("heap empty");
            T top = a.get(0);
            T last = a.remove(a.size() - 1);       // O(1) on ArrayList
            if (!a.isEmpty()) { a.set(0, last); siftDown(0); }
            return top;
        }

        private void siftUp(int i) {
            while (i > 0) {
                int p = (i - 1) / 2;
                if (cmp.compare(a.get(i), a.get(p)) >= 0) break;   // in place
                swap(i, p);
                i = p;
            }
        }

        private void siftDown(int i) {
            int n = a.size();
            while (true) {
                int l = 2 * i + 1, r = 2 * i + 2, best = i;
                if (l < n && cmp.compare(a.get(l), a.get(best)) < 0) best = l;
                if (r < n && cmp.compare(a.get(r), a.get(best)) < 0) best = r;
                if (best == i) break;                              // in place
                swap(i, best);
                i = best;
            }
        }

        private void swap(int i, int j) { T t = a.get(i); a.set(i, a.get(j)); a.set(j, t); }

        int size()        { return a.size(); }
        boolean isEmpty() { return a.isEmpty(); }
        @Override public String toString() { return a.toString(); }
    }

    /* ======================================================================
     * 2. HEAPSORT — in-place, O(n log n), O(1) space, NOT stable
     * Build a MAX-heap, then repeatedly swap root to the end and shrink.
     * ====================================================================== */
    static void heapSort(int[] arr) {
        int n = arr.length;
        for (int i = n / 2 - 1; i >= 0; i--) sink(arr, i, n);     // build max-heap, O(n)
        for (int end = n - 1; end > 0; end--) {
            int t = arr[0]; arr[0] = arr[end]; arr[end] = t;      // largest to its slot
            sink(arr, 0, end);                                    // shrink and restore
        }
    }
    private static void sink(int[] a, int i, int n) {
        while (true) {
            int l = 2*i + 1, r = 2*i + 2, big = i;
            if (l < n && a[l] > a[big]) big = l;
            if (r < n && a[r] > a[big]) big = r;
            if (big == i) return;
            int t = a[i]; a[i] = a[big]; a[big] = t;
            i = big;
        }
    }

    /* ======================================================================
     * 3. JAVA API — PriorityQueue. Construction, comparator idioms, and the
     * "toString isn't sorted" / O(n) remove gotchas are demonstrated live in
     * main() and the patterns below (findKthLargest, kClosest, mergeKSorted).
     * Note: min by default; pass Comparator.reverseOrder() for a max-heap, and
     * prefer Comparator.comparingInt over `(a,b) -> a - b` (overflow risk).
     * ====================================================================== */

    /* ======================================================================
     * 4. PATTERN: Kth LARGEST  (LC 215) — min-heap of size k, O(n log k)
     * ====================================================================== */
    static int findKthLargest(int[] nums, int k) {
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();   // MIN heap
        for (int n : nums) {
            minHeap.offer(n);
            if (minHeap.size() > k) minHeap.poll();   // evict the smallest, keep top-k
        }
        return minHeap.peek();                        // smallest of the top k = kth largest
    }

    /* ======================================================================
     * 5. PATTERN: TOP K FREQUENT  (LC 347) via heap — O(n log k)
     * (Bucket sort in HashMapNotes is O(n) and better; know both.)
     * ====================================================================== */
    static int[] topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> freq = new HashMap<>();
        for (int n : nums) freq.merge(n, 1, Integer::sum);

        // min-heap ordered by frequency; keep only k entries
        PriorityQueue<Map.Entry<Integer,Integer>> pq =
                new PriorityQueue<>(Map.Entry.comparingByValue());
        for (var e : freq.entrySet()) {
            pq.offer(e);
            if (pq.size() > k) pq.poll();
        }
        int[] res = new int[pq.size()];
        for (int i = res.length - 1; i >= 0; i--) res[i] = pq.poll().getKey();
        return res;
    }

    /* ======================================================================
     * 6. PATTERN: K CLOSEST POINTS TO ORIGIN  (LC 973) — max-heap of size k
     * Compare squared distance; skip the sqrt, it's monotonic.
     * ====================================================================== */
    static int[][] kClosest(int[][] points, int k) {
        PriorityQueue<int[]> maxHeap = new PriorityQueue<>(
                (a, b) -> (b[0]*b[0] + b[1]*b[1]) - (a[0]*a[0] + a[1]*a[1]));
        for (int[] p : points) {
            maxHeap.offer(p);
            if (maxHeap.size() > k) maxHeap.poll();   // drop the farthest
        }
        return maxHeap.toArray(new int[0][]);
    }

    /* ======================================================================
     * 7. PATTERN: MERGE K SORTED LISTS  (LC 23) — heap of the k heads, O(N log k)
     * ====================================================================== */
    static List<Integer> mergeKSorted(List<List<Integer>> lists) {
        // entry = {value, listIndex, elementIndex}
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
        for (int i = 0; i < lists.size(); i++)
            if (!lists.get(i).isEmpty()) pq.offer(new int[]{lists.get(i).get(0), i, 0});

        List<Integer> out = new ArrayList<>();
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            out.add(cur[0]);
            int li = cur[1], ei = cur[2] + 1;
            if (ei < lists.get(li).size())
                pq.offer(new int[]{lists.get(li).get(ei), li, ei});   // refill from that list
        }
        return out;
    }

    /* ======================================================================
     * 8. PATTERN: MEDIAN FROM A DATA STREAM  (LC 295) — TWO heaps
     *
     *   lo = MAX-heap of the smaller half   hi = MIN-heap of the larger half
     *
     *        [ 1 2 3 ]  |  [ 4 5 6 ]
     *              ^          ^
     *          lo.peek()   hi.peek()
     *
     *   Invariants:  lo.size() == hi.size()  or  lo.size() == hi.size() + 1
     *                every element in lo <= every element in hi
     *   median = lo.peek() if odd, else (lo.peek()+hi.peek())/2.0
     *
     *   add: push to lo, then move lo's max into hi (guarantees ordering),
     *        then rebalance sizes. add O(log n), findMedian O(1).
     * ====================================================================== */
    static class MedianFinder {
        private final PriorityQueue<Integer> lo = new PriorityQueue<>(Comparator.reverseOrder());
        private final PriorityQueue<Integer> hi = new PriorityQueue<>();

        void addNum(int num) {
            lo.offer(num);            // always enters the low half first
            hi.offer(lo.poll());      // ...then hand the largest low element to hi
            if (hi.size() > lo.size()) lo.offer(hi.poll());   // keep lo >= hi in size
        }

        double findMedian() {
            if (lo.size() > hi.size()) return lo.peek();
            return (lo.peek() + hi.peek()) / 2.0;
        }
    }

    /* ======================================================================
     * 9. PATTERN: TASK SCHEDULER / greedy by count  (LC 621 idea)
     * Always run the most frequent available task -> max-heap on counts.
     * ====================================================================== */
    static int leastInterval(char[] tasks, int n) {
        int[] cnt = new int[26];
        for (char c : tasks) cnt[c - 'A']++;
        PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.reverseOrder());
        for (int c : cnt) if (c > 0) pq.offer(c);

        Deque<int[]> cooling = new ArrayDeque<>();   // {remainingCount, availableAtTime}
        int time = 0;
        while (!pq.isEmpty() || !cooling.isEmpty()) {
            time++;
            if (!pq.isEmpty()) {
                int left = pq.poll() - 1;
                if (left > 0) cooling.offerLast(new int[]{left, time + n});
            }
            if (!cooling.isEmpty() && cooling.peekFirst()[1] == time)
                pq.offer(cooling.pollFirst()[0]);
        }
        return time;
    }

    /* ================================================================== */
    public static void main(String[] args) {
        System.out.println("--- custom BinaryHeap ---");
        BinaryHeap<Integer> h = BinaryHeap.minHeap();
        for (int v : new int[]{5, 1, 8, 3, 9, 2}) h.push(v);
        System.out.println("internal array: " + h + "   (NOT sorted, only heap-ordered)");
        StringBuilder sb = new StringBuilder();
        while (!h.isEmpty()) sb.append(h.pop()).append(' ');
        System.out.println("pop order     : " + sb.toString().trim());

        System.out.println("\n--- O(n) heapify + max-heap ---");
        BinaryHeap<Integer> mx = BinaryHeap.<Integer>maxHeap().heapify(List.of(5,1,8,3,9,2));
        System.out.println("max peek = " + mx.peek());

        System.out.println("\n--- heapSort ---");
        int[] arr = {5, 2, 9, 1, 7, 3};
        heapSort(arr);
        System.out.println(Arrays.toString(arr));

        System.out.println("\n--- java.util.PriorityQueue ---");
        PriorityQueue<Integer> pq = new PriorityQueue<>(List.of(5, 1, 8, 3));  // O(n) heapify
        System.out.println("toString (UNSORTED, don't trust it): " + pq);
        System.out.print("poll order (correct): ");
        while (!pq.isEmpty()) System.out.print(pq.poll() + " ");
        System.out.println();

        System.out.println("\n--- patterns ---");
        System.out.println("findKthLargest([3,2,1,5,6,4], k=2) = " +
                findKthLargest(new int[]{3,2,1,5,6,4}, 2));
        System.out.println("topKFrequent([1,1,1,2,2,3], k=2)   = " +
                Arrays.toString(topKFrequent(new int[]{1,1,1,2,2,3}, 2)));
        System.out.println("kClosest k=2 = " +
                Arrays.deepToString(kClosest(new int[][]{{1,3},{-2,2},{5,8},{0,1}}, 2)));
        System.out.println("mergeKSorted = " + mergeKSorted(List.of(
                List.of(1,4,5), List.of(1,3,4), List.of(2,6))));

        System.out.println("\n--- MedianFinder (two heaps) ---");
        MedianFinder mf = new MedianFinder();
        for (int v : new int[]{5, 15, 1, 3}) {
            mf.addNum(v);
            System.out.println("  added " + v + " -> median " + mf.findMedian());
        }

        System.out.println("\n--- task scheduler ---");
        System.out.println("leastInterval(AAABBB, n=2) = " +
                leastInterval("AAABBB".toCharArray(), 2));
    }
}

/* ==========================================================================
 * PRACTICE — BLIND 75 / NEETCODE (heap)
 * ==========================================================================
 *   LC 703  Kth Largest Element in a Stream       easy   size-k min heap
 *   LC 1046 Last Stone Weight                     easy   max heap warm-up
 *   LC 973  K Closest Points to Origin            med    size-k max heap
 *   LC 215  Kth Largest Element in an Array       med    heap OR quickselect O(n)
 *   LC 347  Top K Frequent Elements               med    heap OR bucket sort
 *   LC 621  Task Scheduler                        med    greedy + max heap
 *   LC 355  Design Twitter                        med    merge k feeds via heap
 *   LC 23   Merge k Sorted Lists                  hard   heap of k heads
 *   LC 295  Find Median from Data Stream          hard   two heaps
 *   LC 1834 Single-Threaded CPU                   med    sort + heap
 *   LC 767  Reorganize String                     med    max heap on counts
 *   LC 253  Meeting Rooms II                      med    min heap of end times
 *
 * Self-test questions + answers: see ./README.md
 * ========================================================================== */
