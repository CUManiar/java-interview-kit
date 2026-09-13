import java.util.*;

/* ==========================================================================
 * ALGOS: BINARY SEARCH (and friends)
 * Run: java SearchAlgos.java
 * ==========================================================================
 *
 * THE IDEA
 * --------
 * Every comparison eliminates half the search space. O(log n).
 * Binary search does NOT require an array. It requires a MONOTONIC PREDICATE:
 * some boolean f(x) that is false...false, true...true. You are finding the
 * boundary.
 *
 *        index:  0  1  2  3  4  5  6
 *        arr  : [1, 3, 5, 7, 9,11,13]   target = 9
 *
 *        lo=0 hi=6  mid=3 -> 7 < 9  -> lo=4
 *        lo=4 hi=6  mid=5 -> 11> 9  -> hi=4
 *        lo=4 hi=4  mid=4 -> 9 == 9 -> FOUND
 *
 * THE THREE BUGS EVERYONE WRITES
 * ------------------------------
 *   1. OVERFLOW:    mid = (lo + hi) / 2  overflows for large ints.
 *                   ALWAYS: mid = lo + (hi - lo) / 2
 *   2. INFINITE LOOP: with `while (lo < hi)` and `lo = mid`, if hi == lo+1 then
 *                   mid == lo and nothing moves. Use mid = lo + (hi-lo+1)/2
 *                   (round UP) when you write `lo = mid`.
 *   3. OFF-BY-ONE:  pick ONE template and never deviate. Mine are below.
 *
 * TEMPLATE 1 — exact match, closed interval [lo, hi]
 *   while (lo <= hi) { ... lo = mid+1 / hi = mid-1 }
 *   After the loop: lo == insertion point, hi == lo-1.
 *
 * TEMPLATE 2 — find the FIRST index where predicate is true, half-open [lo, hi)
 *   while (lo < hi) { if (ok(mid)) hi = mid; else lo = mid+1; }
 *   return lo;
 *   This one answers lowerBound, upperBound, "minimum capacity that works",
 *   rotated array, peak finding. LEARN THIS ONE BEST.
 *
 * "BINARY SEARCH ON THE ANSWER"
 *   When the question is "find the MINIMUM x such that it's feasible", binary
 *   search over the ANSWER RANGE, not over an array. You need a feasible(x)
 *   function that is monotonic. Koko bananas, ship packages, split array.
 *   Recognising this is worth more than any other single search skill.
 * ========================================================================== */
public class SearchAlgos {

    /*
     * ======================================================================
     * 1. CLASSIC BINARY SEARCH — exact match (LC 704)
     * ======================================================================
     */
    static int binarySearch(int[] a, int target) {
        int lo = 0, hi = a.length - 1; // CLOSED interval
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2; // overflow-safe
            if (a[mid] == target)
                return mid;
            if (a[mid] < target)
                lo = mid + 1;
            else
                hi = mid - 1;
        }
        return -1; // lo is now the insertion point
    }

    /*
     * ======================================================================
     * 2. LOWER / UPPER BOUND — the two most reusable variants
     *
     * arr = [1,2,2,2,3] target = 2
     * lowerBound -> 1 (first index with a[i] >= 2)
     * upperBound -> 4 (first index with a[i] > 2)
     * count of target = upperBound - lowerBound = 3
     * ======================================================================
     */
    static int lowerBound(int[] a, int target) {
        int lo = 0, hi = a.length; // HALF-OPEN [lo, hi)
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] >= target)
                hi = mid; // mid might be the answer, keep it
            else
                lo = mid + 1; // mid is definitely too small
        }
        return lo;
    }

    static int upperBound(int[] a, int target) {
        int lo = 0, hi = a.length;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] > target)
                hi = mid;
            else
                lo = mid + 1;
        }
        return lo;
    }

    /* LC 34 — first and last position. Direct application of the two bounds. */
    static int[] searchRange(int[] a, int target) {
        int lo = lowerBound(a, target);
        if (lo == a.length || a[lo] != target)
            return new int[] { -1, -1 };
        return new int[] { lo, upperBound(a, target) - 1 };
    }

    /*
     * ======================================================================
     * 3. ROTATED SORTED ARRAY (LC 33 / 153)
     *
     * [4,5,6,7,0,1,2]
     * \_______/ \___/
     * sorted sorted
     *
     * KEY INSIGHT: at any mid, AT LEAST ONE HALF IS SORTED. Figure out which,
     * check whether the target lies inside that sorted half, and discard the
     * other half.
     * ======================================================================
     */
    static int searchRotated(int[] a, int target) {
        int lo = 0, hi = a.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] == target)
                return mid;

            if (a[lo] <= a[mid]) { // LEFT half is sorted
                if (a[lo] <= target && target < a[mid])
                    hi = mid - 1; // in the left
                else
                    lo = mid + 1;
            } else { // RIGHT half is sorted
                if (a[mid] < target && target <= a[hi])
                    lo = mid + 1; // in the right
                else
                    hi = mid - 1;
            }
        }
        return -1;
    }

    /*
     * LC 153 — minimum in a rotated sorted array. Compare mid with hi, NOT with lo.
     * If a[mid] > a[hi], the pivot is to the right. Otherwise mid could be it.
     */
    static int findMinRotated(int[] a) {
        int lo = 0, hi = a.length - 1;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] > a[hi])
                lo = mid + 1; // min is strictly right of mid
            else
                hi = mid; // min is at mid or left of it
        }
        return a[lo];
    }

    /*
     * ======================================================================
     * 4. PEAK ELEMENT (LC 162) — binary search WITHOUT a sorted array
     * If a[mid] < a[mid+1] you're on an ascending slope -> a peak is to the right.
     * Works because the boundaries are treated as -infinity.
     * ======================================================================
     */
    static int findPeakElement(int[] a) {
        int lo = 0, hi = a.length - 1;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] < a[mid + 1])
                lo = mid + 1; // uphill -> go right
            else
                hi = mid; // downhill or peak -> stay left
        }
        return lo;
    }

    /*
     * ======================================================================
     * 5. BINARY SEARCH ON THE ANSWER — Koko eating bananas (LC 875)
     *
     * Question: minimum eating speed k so all piles are finished within h hours.
     * feasible(k) is MONOTONIC: if speed k works, any speed > k also works.
     *
     * k: 1 2 3 4 5 6 ...
     * feasible: F F F T T T
     * ^ we want this boundary
     *
     * Search range: [1, max(piles)]. Same shape solves:
     * LC 1011 ship packages in D days
     * LC 410 split array largest sum
     * LC 1482 minimum days to make bouquets
     * LC 2064 minimum size of products
     * ======================================================================
     */
    static int minEatingSpeed(int[] piles, int h) {
        int lo = 1, hi = Arrays.stream(piles).max().getAsInt();
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (canFinish(piles, mid, h))
                hi = mid; // mid works, try slower
            else
                lo = mid + 1;
        }
        return lo;
    }

    private static boolean canFinish(int[] piles, int speed, int h) {
        long hours = 0;
        for (int p : piles)
            hours += (p + speed - 1L) / speed; // ceiling division
        return hours <= h;
    }

    /*
     * LC 1011 — least ship capacity to deliver in `days`. Same template.
     * Lower bound of the range is max(weights) because one package must fit.
     */
    static int shipWithinDays(int[] weights, int days) {
        int lo = Arrays.stream(weights).max().getAsInt();
        int hi = Arrays.stream(weights).sum();
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (daysNeeded(weights, mid) <= days)
                hi = mid;
            else
                lo = mid + 1;
        }
        return lo;
    }

    private static int daysNeeded(int[] w, int cap) {
        int days = 1, load = 0;
        for (int x : w) {
            if (load + x > cap) {
                days++;
                load = 0;
            }
            load += x;
        }
        return days;
    }

    /*
     * ======================================================================
     * 6. SEARCH A 2D MATRIX (LC 74) — treat it as one flat sorted array
     * index i maps to (i / cols, i % cols).
     * ======================================================================
     */
    static boolean searchMatrix(int[][] m, int target) {
        int rows = m.length, cols = m[0].length;
        int lo = 0, hi = rows * cols - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            int val = m[mid / cols][mid % cols]; // the mapping
            if (val == target)
                return true;
            if (val < target)
                lo = mid + 1;
            else
                hi = mid - 1;
        }
        return false;
    }

    /*
     * ======================================================================
     * 7. MEDIAN OF TWO SORTED ARRAYS (LC 4) — O(log(min(m,n)))
     *
     * Partition both arrays so that the left parts together hold exactly half
     * the elements, and maxLeft <= minRight on BOTH sides.
     *
     * A: [1, 3 | 8, 9]
     * B: [7 | 11, 18, 19, 21, 25]
     * ^ binary search the cut in A; the cut in B is forced
     *
     * Conditions: Aleft <= Bright AND Bleft <= Aright
     * Always binary search the SHORTER array.
     * ======================================================================
     */
    static double findMedianSortedArrays(int[] A, int[] B) {
        if (A.length > B.length)
            return findMedianSortedArrays(B, A); // search the shorter
        int m = A.length, n = B.length, half = (m + n + 1) / 2;
        int lo = 0, hi = m;

        while (lo <= hi) {
            int i = lo + (hi - lo) / 2; // elements taken from A
            int j = half - i; // elements taken from B

            int Aleft = (i == 0) ? Integer.MIN_VALUE : A[i - 1];
            int Aright = (i == m) ? Integer.MAX_VALUE : A[i];
            int Bleft = (j == 0) ? Integer.MIN_VALUE : B[j - 1];
            int Bright = (j == n) ? Integer.MAX_VALUE : B[j];

            if (Aleft <= Bright && Bleft <= Aright) { // correct cut
                if (((m + n) & 1) == 1)
                    return Math.max(Aleft, Bleft); // odd total
                return (Math.max(Aleft, Bleft) + Math.min(Aright, Bright)) / 2.0;
            }
            if (Aleft > Bright)
                hi = i - 1; // took too much from A
            else
                lo = i + 1; // took too little from A
        }
        throw new IllegalArgumentException("input arrays are not sorted");
    }

    /*
     * ======================================================================
     * 8. BINARY SEARCH ON DOUBLES — fixed iteration count, no epsilon dance
     * 100 iterations halves the range 2^100 times. Always enough.
     * ======================================================================
     */
    static double sqrt(double x) {
        double lo = 0, hi = Math.max(1, x);
        for (int i = 0; i < 100; i++) {
            double mid = (lo + hi) / 2;
            if (mid * mid < x)
                lo = mid;
            else
                hi = mid;
        }
        return lo;
    }

    /*
     * LC 69 — integer sqrt. Careful with overflow: compare with division, or use
     * long.
     */
    static int mySqrt(int x) {
        int lo = 0, hi = x;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            long sq = (long) mid * mid; // cast BEFORE multiplying
            if (sq == x)
                return mid;
            if (sq < x)
                lo = mid + 1;
            else
                hi = mid - 1;
        }
        return hi; // floor
    }

    /*
     * ======================================================================
     * 9. JAVA API
     * ======================================================================
     * Arrays.binarySearch(int[] a, int key)
     * found -> the index (NOT guaranteed to be the first if duplicates!)
     * not found -> -(insertionPoint) - 1
     * recover: int ip = -result - 1;
     * Arrays.binarySearch(a, fromIndex, toIndex, key)
     * Arrays.binarySearch(T[] a, T key, Comparator<T> c)
     * Collections.binarySearch(list, key)
     *
     * PREFER TreeSet/TreeMap when you want bound semantics, they're clearer:
     * TreeSet.floor(x) <= x TreeSet.ceiling(x) >= x
     * TreeSet.lower(x) < x TreeSet.higher(x) > x
     * TreeMap.floorEntry / ceilingEntry / subMap / headMap / tailMap
     *
     * IMPORTANT: Arrays.binarySearch requires the array to be SORTED. Results
     * are undefined otherwise - it won't throw, it'll just lie.
     */

    /* ================================================================== */
    public static void main(String[] args) {
        int[] a = { 1, 3, 5, 7, 9, 11, 13 };
        System.out.println("arr = " + Arrays.toString(a));
        System.out.println("binarySearch(9)  = " + binarySearch(a, 9));
        System.out.println("binarySearch(10) = " + binarySearch(a, 10));

        int[] dup = { 1, 2, 2, 2, 3 };
        System.out.println("\narr = " + Arrays.toString(dup));
        System.out.println("lowerBound(2) = " + lowerBound(dup, 2)
                + "  upperBound(2) = " + upperBound(dup, 2)
                + "  count = " + (upperBound(dup, 2) - lowerBound(dup, 2)));
        System.out.println("searchRange(2) = " + Arrays.toString(searchRange(dup, 2)));

        int[] rot = { 4, 5, 6, 7, 0, 1, 2 };
        System.out.println("\nrotated = " + Arrays.toString(rot));
        System.out.println("searchRotated(0) = " + searchRotated(rot, 0));
        System.out.println("searchRotated(3) = " + searchRotated(rot, 3));
        System.out.println("findMinRotated   = " + findMinRotated(rot));

        System.out.println("\nfindPeakElement([1,2,3,1]) = " +
                findPeakElement(new int[] { 1, 2, 3, 1 }));

        System.out.println("\n--- binary search on the ANSWER ---");
        System.out.println("minEatingSpeed([3,6,7,11], h=8)   = " +
                minEatingSpeed(new int[] { 3, 6, 7, 11 }, 8));
        System.out.println("shipWithinDays([1..10], days=5)   = " +
                shipWithinDays(new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }, 5));

        System.out.println("\nsearchMatrix(3) = " + searchMatrix(
                new int[][] { { 1, 3, 5, 7 }, { 10, 11, 16, 20 }, { 23, 30, 34, 60 } }, 3));

        System.out.println("\nmedian([1,3],[2])     = " +
                findMedianSortedArrays(new int[] { 1, 3 }, new int[] { 2 }));
        System.out.println("median([1,2],[3,4])   = " +
                findMedianSortedArrays(new int[] { 1, 2 }, new int[] { 3, 4 }));

        System.out.println("\nsqrt(2.0) = " + sqrt(2.0) + "   mySqrt(8) = " + mySqrt(8));

        System.out.println("\n--- Java API ---");
        int r = Arrays.binarySearch(a, 10);
        System.out.println("Arrays.binarySearch(arr,10) = " + r
                + " -> insertion point " + (-r - 1));
        TreeSet<Integer> ts = new TreeSet<>(List.of(1, 3, 5, 7, 9));
        System.out.println("TreeSet floor(6)=" + ts.floor(6) + " ceiling(6)=" + ts.ceiling(6));
    }
}

/*
 * ==========================================================================
 * PRACTICE — BLIND 75 / NEETCODE (binary search)
 * ==========================================================================
 * LC 704 Binary Search easy the template
 * LC 35 Search Insert Position easy lowerBound
 * LC 34 First and Last Position med two bounds
 * LC 74 Search a 2D Matrix med flatten the index
 * LC 240 Search a 2D Matrix II med staircase from top-right, O(m+n)
 * LC 153 Find Minimum in Rotated Sorted Array med compare mid with hi
 * LC 33 Search in Rotated Sorted Array med which half is sorted
 * LC 162 Find Peak Element med slope
 * LC 875 Koko Eating Bananas med search the ANSWER
 * LC 1011 Capacity to Ship Packages med search the ANSWER
 * LC 410 Split Array Largest Sum hard search the ANSWER
 * LC 981 Time Based Key-Value Store med binary search per key
 * LC 4 Median of Two Sorted Arrays hard partition both arrays
 * LC 287 Find the Duplicate Number med binary search on value range
 *
 * SELF-TEST QUESTIONS
 * - Write the overflow-safe mid. Why does the naive form break?
 * - When do you need mid rounded UP, and why?
 * - What does Arrays.binarySearch return for a missing key, and how do you
 * turn it into an insertion index?
 * - Rotated array: what's the invariant that makes it work?
 * - Recognising "binary search on the answer": what must be true of feasible()?
 * - Why search the SHORTER array in median-of-two-sorted-arrays?
 * ==========================================================================
 */
