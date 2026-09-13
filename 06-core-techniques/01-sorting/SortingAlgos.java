import java.util.*;

/* ==========================================================================
 * ALGOS: SORTING
 * Run: java SortingAlgos.java
 * ==========================================================================
 *
 * THE TABLE — know this cold, you will be asked
 * ┌──────────────┬──────────┬──────────┬──────────┬────────┬────────┬──────────────────────┐
 * │ Algorithm    │ Best     │ Average  │ Worst    │ Space  │ Stable │ Notes                │
 * ├──────────────┼──────────┼──────────┼──────────┼────────┼────────┼──────────────────────┤
 * │ Bubble       │ O(n)     │ O(n^2)   │ O(n^2)   │ O(1)   │ YES    │ teaching only        │
 * │ Selection    │ O(n^2)   │ O(n^2)   │ O(n^2)   │ O(1)   │ no     │ fewest swaps: n-1    │
 * │ Insertion    │ O(n)     │ O(n^2)   │ O(n^2)   │ O(1)   │ YES    │ great for small/near │
 * │              │          │          │          │        │        │ sorted; used inside  │
 * │              │          │          │          │        │        │ real sorts for n<~32 │
 * │ Merge        │ O(n logn)│ O(n logn)│ O(n logn)│ O(n)   │ YES    │ predictable; linked  │
 * │              │          │          │          │        │        │ lists; external sort │
 * │ Quick        │ O(n logn)│ O(n logn)│ O(n^2)   │ O(logn)│ no     │ fastest in practice  │
 * │ Heap         │ O(n logn)│ O(n logn)│ O(n logn)│ O(1)   │ no     │ in-place, poor cache │
 * │ Counting     │ O(n+k)   │ O(n+k)   │ O(n+k)   │ O(k)   │ YES    │ small integer range  │
 * │ Radix        │ O(d(n+k))│ O(d(n+k))│ O(d(n+k))│ O(n+k) │ YES    │ fixed-width keys     │
 * │ Bucket       │ O(n+k)   │ O(n+k)   │ O(n^2)   │ O(n)   │ YES    │ uniform distribution │
 * └──────────────┴──────────┴──────────┴──────────┴────────┴────────┴──────────────────────┘
 *
 * WHAT "STABLE" MEANS AND WHY IT MATTERS
 *   Equal elements keep their relative order.
 *   [("b",2), ("a",2)] sorted by the number stays in that order if stable.
 *   Matters when you sort by one key and then another (multi-key sorting), and
 *   for anything user-facing where order is meaningful.
 *
 * THE O(n log n) LOWER BOUND
 *   Any COMPARISON sort needs Omega(n log n): there are n! permutations, a binary
 *   decision tree with n! leaves has height >= log2(n!) = Omega(n log n).
 *   Counting/radix/bucket beat it only because they DON'T compare - they exploit
 *   the structure of the keys.
 *
 * WHAT JAVA ACTUALLY DOES  (say this, it lands well)
 *   Arrays.sort(int[])      -> DUAL-PIVOT QUICKSORT. Not stable. O(1) extra space.
 *                              Stability doesn't matter for primitives - two equal
 *                              ints are indistinguishable.
 *   Arrays.sort(Object[])   -> TIMSORT (merge + insertion hybrid). STABLE. O(n) space.
 *   Collections.sort(List)  -> delegates to List.sort -> Timsort.
 *   Arrays.parallelSort()   -> parallel merge sort, worth it above ~8k elements.
 *   Timsort finds existing sorted "runs" and merges them -> O(n) on sorted input.
 * ========================================================================== */
public class SortingAlgos {

    /* ======================================================================
     * 1. BUBBLE SORT — adjacent swaps bubble the max to the end each pass
     * The early-exit flag is what gives it O(n) on already-sorted input.
     * ====================================================================== */
    static void bubbleSort(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < a.length - 1 - i; j++) {   // -i: the tail is already sorted
                if (a[j] > a[j + 1]) { swap(a, j, j + 1); swapped = true; }
            }
            if (!swapped) return;                          // already sorted, bail out
        }
    }

    /* ======================================================================
     * 2. SELECTION SORT — find the min, swap it into place. Exactly n-1 swaps.
     * Use when writes are expensive (flash memory) and reads are cheap.
     * ====================================================================== */
    static void selectionSort(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            int min = i;
            for (int j = i + 1; j < a.length; j++) if (a[j] < a[min]) min = j;
            if (min != i) swap(a, i, min);
        }
    }

    /* ======================================================================
     * 3. INSERTION SORT — take a[i], slide it left into the sorted prefix
     *
     *   [2,4,7 | 3,1]   key=3
     *    sorted   ^
     *   shift 7 and 4 right, drop 3 in:  [2,3,4,7 | 1]
     *
     * O(n) when nearly sorted. This is why Timsort/quicksort fall back to it
     * for small subarrays.
     * ====================================================================== */
    static void insertionSort(int[] a) {
        for (int i = 1; i < a.length; i++) {
            int key = a[i], j = i - 1;
            while (j >= 0 && a[j] > key) { a[j + 1] = a[j]; j--; }   // shift right
            a[j + 1] = key;                                          // drop into the hole
        }
    }

    /* ======================================================================
     * 4. MERGE SORT — divide, sort halves, merge. Stable, predictable.
     *
     *            [5,2,9,1,7,3]
     *           /             \
     *      [5,2,9]          [1,7,3]
     *      /     \          /     \
     *    [5]   [2,9]      [1]   [7,3]
     *           /  \             /  \
     *         [2]  [9]         [7]  [3]
     *    merge up: [2,9]   [3,7]
     *              [2,5,9] [1,3,7]
     *              [1,2,3,5,7,9]
     *
     * Allocating the temp buffer ONCE (not per call) is the optimisation
     * interviewers look for.
     * ====================================================================== */
    static void mergeSort(int[] a) {
        if (a.length < 2) return;
        mergeSort(a, new int[a.length], 0, a.length - 1);
    }
    private static void mergeSort(int[] a, int[] tmp, int lo, int hi) {
        if (lo >= hi) return;
        int mid = lo + (hi - lo) / 2;          // NOT (lo+hi)/2 -> overflow
        mergeSort(a, tmp, lo, mid);
        mergeSort(a, tmp, mid + 1, hi);
        if (a[mid] <= a[mid + 1]) return;      // already ordered, skip the merge
        merge(a, tmp, lo, mid, hi);
    }
    private static void merge(int[] a, int[] tmp, int lo, int mid, int hi) {
        System.arraycopy(a, lo, tmp, lo, hi - lo + 1);
        int i = lo, j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (i > mid)               a[k] = tmp[j++];
            else if (j > hi)           a[k] = tmp[i++];
            else if (tmp[j] < tmp[i])  a[k] = tmp[j++];
            else                       a[k] = tmp[i++];   // <= keeps it STABLE
        }
    }

    /* ======================================================================
     * 5. QUICK SORT — pick a pivot, partition, recurse.
     *
     * Lomuto partition (easier to write, what most people use in interviews):
     *   pivot = a[hi]; i = lo-1
     *   walk j from lo..hi-1, if a[j] <= pivot then i++, swap(i,j)
     *   finally swap(i+1, hi) -> pivot lands at i+1, its final position
     *
     *   [3,7,8,5,2,1,9,5]  pivot=5
     *   after: [3,2,1,5 | 5,8,9,7]   everything left <= 5, right > 5
     *                ^ pivot index
     *
     * WHY IT DEGRADES TO O(n^2): already-sorted input with a fixed last-element
     * pivot makes every partition of size 1 and n-1.
     * FIX: randomise the pivot (done below) or use median-of-three.
     * Recurse into the SMALLER side first to keep stack depth O(log n).
     * ====================================================================== */
    static void quickSort(int[] a) { quickSort(a, 0, a.length - 1); }
    private static void quickSort(int[] a, int lo, int hi) {
        while (lo < hi) {
            if (hi - lo < 16) { insertionRange(a, lo, hi); return; }   // small -> insertion
            int p = partition(a, lo, hi);
            if (p - lo < hi - p) { quickSort(a, lo, p - 1); lo = p + 1; }  // tail-call the big side
            else                 { quickSort(a, p + 1, hi); hi = p - 1; }
        }
    }
    private static int partition(int[] a, int lo, int hi) {
        int r = lo + new Random().nextInt(hi - lo + 1);   // randomised pivot
        swap(a, r, hi);
        int pivot = a[hi], i = lo - 1;
        for (int j = lo; j < hi; j++)
            if (a[j] <= pivot) swap(a, ++i, j);
        swap(a, i + 1, hi);
        return i + 1;
    }
    private static void insertionRange(int[] a, int lo, int hi) {
        for (int i = lo + 1; i <= hi; i++) {
            int key = a[i], j = i - 1;
            while (j >= lo && a[j] > key) { a[j + 1] = a[j]; j--; }
            a[j + 1] = key;
        }
    }

    /* ======================================================================
     * 6. QUICKSELECT — kth smallest in O(n) AVERAGE. Quicksort that recurses
     * into only ONE side. This is the optimal answer to LC 215.
     *   T(n) = T(n/2) + O(n) = O(n)   (vs quicksort's 2 T(n/2) + O(n))
     * ====================================================================== */
    static int quickSelect(int[] a, int k) {      // k is 1-indexed: k=1 -> smallest
        int lo = 0, hi = a.length - 1, target = k - 1;
        while (true) {
            int p = partition(a, lo, hi);
            if (p == target) return a[p];
            if (p < target) lo = p + 1;           // discard the left half entirely
            else            hi = p - 1;
        }
    }

    /* ======================================================================
     * 7. HEAP SORT — build a max-heap, swap root to the end, shrink, sink.
     * In-place O(1) space, guaranteed O(n log n), but cache-hostile so it loses
     * to quicksort in wall-clock time.
     * ====================================================================== */
    static void heapSort(int[] a) {
        int n = a.length;
        for (int i = n / 2 - 1; i >= 0; i--) sink(a, i, n);     // heapify, O(n)
        for (int end = n - 1; end > 0; end--) {
            swap(a, 0, end);
            sink(a, 0, end);
        }
    }
    private static void sink(int[] a, int i, int n) {
        while (true) {
            int l = 2*i + 1, r = 2*i + 2, big = i;
            if (l < n && a[l] > a[big]) big = l;
            if (r < n && a[r] > a[big]) big = r;
            if (big == i) return;
            swap(a, i, big);
            i = big;
        }
    }

    /* ======================================================================
     * 8. COUNTING SORT — O(n + k). No comparisons. Needs a known small range.
     * Building the PREFIX SUM of counts and walking the input BACKWARDS is what
     * makes it stable. Foundation of radix sort.
     * ====================================================================== */
    static int[] countingSort(int[] a, int maxVal) {
        int[] count = new int[maxVal + 1];
        for (int v : a) count[v]++;
        for (int i = 1; i <= maxVal; i++) count[i] += count[i - 1];   // prefix sums
        int[] out = new int[a.length];
        for (int i = a.length - 1; i >= 0; i--)                       // backwards = STABLE
            out[--count[a[i]]] = a[i];
        return out;
    }

    /* ======================================================================
     * 9. RADIX SORT (LSD) — counting sort digit by digit, least significant first.
     * O(d(n+k)) where d = number of digits. Beats n log n for large fixed-width keys.
     * Relies on the inner sort being STABLE, otherwise earlier digits get scrambled.
     * ====================================================================== */
    static void radixSort(int[] a) {          // non-negative ints only
        if (a.length == 0) return;
        int max = Arrays.stream(a).max().getAsInt();
        int[] out = new int[a.length];
        for (int exp = 1; max / exp > 0; exp *= 10) {
            int[] count = new int[10];
            for (int v : a) count[(v / exp) % 10]++;
            for (int i = 1; i < 10; i++) count[i] += count[i - 1];
            for (int i = a.length - 1; i >= 0; i--) {     // backwards = stable
                int digit = (a[i] / exp) % 10;
                out[--count[digit]] = a[i];
            }
            System.arraycopy(out, 0, a, 0, a.length);
        }
    }

    /* ======================================================================
     * 10. BUCKET SORT — scatter into buckets by value range, sort each, concatenate.
     * O(n) when the data is uniformly distributed. This is exactly the trick used
     * in "Top K Frequent Elements" (bucket index = frequency).
     * ====================================================================== */
    static void bucketSort(double[] a) {      // values in [0,1)
        int n = a.length;
        if (n == 0) return;
        List<List<Double>> buckets = new ArrayList<>(n);
        for (int i = 0; i < n; i++) buckets.add(new ArrayList<>());
        for (double v : a) buckets.get((int) (v * n)).add(v);      // scatter
        int idx = 0;
        for (List<Double> b : buckets) {
            Collections.sort(b);                                    // each bucket is tiny
            for (double v : b) a[idx++] = v;                        // gather
        }
    }

    /* ======================================================================
     * 11. JAVA API — WHAT YOU ACTUALLY CALL
     * ======================================================================
     *   Arrays.sort(int[])                       dual-pivot quicksort, not stable
     *   Arrays.sort(int[], from, to)             partial range
     *   Arrays.sort(Integer[])                   TIMSORT, stable
     *   Arrays.sort(T[], comparator)
     *   Arrays.parallelSort(arr)                 worth it for n > ~8192
     *   Collections.sort(list)  /  list.sort(cmp)
     *   Arrays.binarySearch(sortedArr, key)      returns -(insertionPoint)-1 if absent
     *
     *   ── TRAP: THERE IS NO Arrays.sort(int[], Comparator) ──────────────
     *   You CANNOT sort a primitive array with a comparator (e.g. descending).
     *   Options:
     *     a) box it:  Integer[] b = Arrays.stream(a).boxed().toArray(Integer[]::new);
     *                 Arrays.sort(b, Comparator.reverseOrder());
     *     b) sort ascending then reverse in place
     *     c) negate, sort, negate back
     *
     *   ── COMPARATORS ────────────────────────────────────────────────────
     *     Comparator.naturalOrder() / reverseOrder()
     *     Comparator.comparingInt(Person::age)
     *     Comparator.comparing(Person::name).thenComparingInt(Person::age)
     *     cmp.reversed()
     *     Comparator.nullsFirst(cmp)
     *     // 2D array by column 0, then column 1:
     *     Arrays.sort(intervals, (x,y) -> x[0]!=y[0] ? x[0]-y[0] : x[1]-y[1]);
     *     Arrays.sort(intervals, Comparator.<int[]>comparingInt(x -> x[0])
     *                                       .thenComparingInt(x -> x[1]));  // overflow-safe
     *
     *   ── SORTING 2D INTERVALS: the single most common interview sort ────
     *     Arrays.sort(intervals, Comparator.comparingInt(a -> a[0]));
     */

    /* helpers */
    private static void swap(int[] a, int i, int j) { int t = a[i]; a[i] = a[j]; a[j] = t; }
    private static int[] sample() { return new int[]{5, 2, 9, 1, 7, 3, 8, 2, 6}; }

    /* ================================================================== */
    public static void main(String[] args) {
        System.out.println("input: " + Arrays.toString(sample()));

        int[] a;
        a = sample(); bubbleSort(a);    System.out.println("bubble    : " + Arrays.toString(a));
        a = sample(); selectionSort(a); System.out.println("selection : " + Arrays.toString(a));
        a = sample(); insertionSort(a); System.out.println("insertion : " + Arrays.toString(a));
        a = sample(); mergeSort(a);     System.out.println("merge     : " + Arrays.toString(a));
        a = sample(); quickSort(a);     System.out.println("quick     : " + Arrays.toString(a));
        a = sample(); heapSort(a);      System.out.println("heap      : " + Arrays.toString(a));
        a = sample(); radixSort(a);     System.out.println("radix     : " + Arrays.toString(a));
        System.out.println("counting  : " + Arrays.toString(countingSort(sample(), 9)));

        double[] d = {0.42, 0.13, 0.99, 0.51, 0.07};
        bucketSort(d);
        System.out.println("bucket    : " + Arrays.toString(d));

        System.out.println("\n--- quickselect ---");
        System.out.println("3rd smallest of sample = " + quickSelect(sample(), 3));
        int[] s = sample();
        System.out.println("2nd largest = kth smallest with k=n-1 = "
                + quickSelect(s, s.length - 1));

        System.out.println("\n--- Java API ---");
        Integer[] boxed = {5, 2, 9, 1};
        Arrays.sort(boxed, Comparator.reverseOrder());
        System.out.println("descending (boxed): " + Arrays.toString(boxed));

        int[][] intervals = {{3,4},{1,5},{1,2},{8,9}};
        Arrays.sort(intervals, Comparator.<int[]>comparingInt(x -> x[0])
                                          .thenComparingInt(x -> x[1]));
        System.out.println("intervals by start,end: " + Arrays.deepToString(intervals));

        int[] sorted = {1,3,5,7,9};
        System.out.println("binarySearch(5)=" + Arrays.binarySearch(sorted, 5)
                + "  binarySearch(6)=" + Arrays.binarySearch(sorted, 6)
                + "  (negative => -(insertionPoint)-1, so insert at index "
                + (-Arrays.binarySearch(sorted, 6) - 1) + ")");
    }
}

/* ==========================================================================
 * PRACTICE
 * ==========================================================================
 *   LC 912  Sort an Array                         med    implement merge/quick
 *   LC 215  Kth Largest Element                   med    QUICKSELECT is the O(n) answer
 *   LC 75   Sort Colors                           med    Dutch national flag, 3-way partition
 *   LC 56   Merge Intervals                       med    sort by start
 *   LC 57   Insert Interval                       med    no sort needed
 *   LC 252  Meeting Rooms                         easy   sort + adjacency check
 *   LC 253  Meeting Rooms II                      med    sort + min heap
 *   LC 148  Sort List                             med    MERGE sort on a linked list
 *   LC 179  Largest Number                        med    custom comparator (b+a vs a+b)
 *   LC 973  K Closest Points                      med    heap or quickselect
 *   LC 347  Top K Frequent                        med    BUCKET sort
 *   LC 164  Maximum Gap                           hard   radix/bucket, O(n)
 *
 * SELF-TEST QUESTIONS
 *   - Which sorts are stable, and why does it matter?
 *   - Why is O(n log n) a lower bound for comparison sorts?
 *   - Why does Java use quicksort for primitives and timsort for objects?
 *   - Make quicksort O(n^2). Now fix it.
 *   - Why is quickselect O(n) but quicksort O(n log n)?
 *   - Why does counting sort walk the input backwards?
 *   - How do you sort an int[] in descending order in Java? (trick question)
 *   - Why is `mid = lo + (hi-lo)/2` and not `(lo+hi)/2`?
 * ========================================================================== */
