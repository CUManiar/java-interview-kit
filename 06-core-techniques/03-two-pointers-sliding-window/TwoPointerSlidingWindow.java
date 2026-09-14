import java.util.*;

/* ==========================================================================
 * ALGOS: TWO POINTERS, SLIDING WINDOW, PREFIX SUMS
 * Run: java TwoPointerSlidingWindow.java
 * See ./README.md for the mental model of the two two-pointer shapes, the
 * sliding-window template (and why it breaks with negative numbers), and the
 * prefix-sum + HashMap trick that covers that gap — not repeated here.
 * ========================================================================== */
public class TwoPointerSlidingWindow {

    /* ======================================================================
     * PART 1 — TWO POINTERS, OPPOSITE ENDS
     * ====================================================================== */

    /* LC 167 — two sum on a SORTED array. O(n) time, O(1) space. */
    static int[] twoSumSorted(int[] a, int target) {
        int l = 0, r = a.length - 1;
        while (l < r) {
            int sum = a[l] + a[r];
            if (sum == target) return new int[]{l + 1, r + 1};   // 1-indexed
            if (sum < target) l++;        // need a bigger sum
            else              r--;        // need a smaller sum
        }
        return new int[]{-1, -1};
    }

    /* LC 125 — valid palindrome, alphanumeric only, case-insensitive. */
    static boolean isPalindrome(String s) {
        int l = 0, r = s.length() - 1;
        while (l < r) {
            while (l < r && !Character.isLetterOrDigit(s.charAt(l))) l++;
            while (l < r && !Character.isLetterOrDigit(s.charAt(r))) r--;
            if (Character.toLowerCase(s.charAt(l)) != Character.toLowerCase(s.charAt(r)))
                return false;
            l++; r--;
        }
        return true;
    }

    /* LC 11 — container with most water.
     * Area = min(h[l], h[r]) * (r - l). Moving the TALLER pointer can never help:
     * width shrinks and the height is still capped by the shorter side. So always
     * move the shorter one. That's the whole proof. */
    static int maxArea(int[] h) {
        int l = 0, r = h.length - 1, best = 0;
        while (l < r) {
            best = Math.max(best, Math.min(h[l], h[r]) * (r - l));
            if (h[l] < h[r]) l++; else r--;        // move the SHORTER side
        }
        return best;
    }

    /* LC 15 — 3Sum. Sort, fix i, then two-pointer the rest.
     * The skip-duplicate lines are what make it correct, not the loop. */
    static List<List<Integer>> threeSum(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> res = new ArrayList<>();
        for (int i = 0; i < nums.length - 2; i++) {
            if (nums[i] > 0) break;                             // sorted: no way to reach 0
            if (i > 0 && nums[i] == nums[i - 1]) continue;      // skip duplicate anchors
            int l = i + 1, r = nums.length - 1;
            while (l < r) {
                int sum = nums[i] + nums[l] + nums[r];
                if (sum < 0) l++;
                else if (sum > 0) r--;
                else {
                    res.add(List.of(nums[i], nums[l], nums[r]));
                    l++; r--;
                    while (l < r && nums[l] == nums[l - 1]) l++;   // skip dup left
                    while (l < r && nums[r] == nums[r + 1]) r--;   // skip dup right
                }
            }
        }
        return res;
    }

    /* LC 42 — trapping rain water, two-pointer O(1) space.
     * Water above bar i = min(maxLeft, maxRight) - height[i].
     * If leftMax < rightMax, the left side is the binding constraint, so we can
     * safely finalise the left bar without knowing the true right max. */
    static int trap(int[] h) {
        int l = 0, r = h.length - 1, leftMax = 0, rightMax = 0, water = 0;
        while (l < r) {
            if (h[l] < h[r]) {
                leftMax = Math.max(leftMax, h[l]);
                water += leftMax - h[l];
                l++;
            } else {
                rightMax = Math.max(rightMax, h[r]);
                water += rightMax - h[r];
                r--;
            }
        }
        return water;
    }

    /* ======================================================================
     * PART 2 — TWO POINTERS, SAME DIRECTION (read/write compaction)
     * ====================================================================== */

    /* LC 26 — remove duplicates from a sorted array in place. Returns new length. */
    static int removeDuplicates(int[] a) {
        if (a.length == 0) return 0;
        int write = 1;                                  // slow: next write position
        for (int read = 1; read < a.length; read++)     // fast: scan position
            if (a[read] != a[write - 1]) a[write++] = a[read];
        return write;
    }

    /* LC 283 — move zeroes to the end, preserving order. */
    static void moveZeroes(int[] a) {
        int write = 0;
        for (int read = 0; read < a.length; read++)
            if (a[read] != 0) { int t = a[write]; a[write++] = a[read]; a[read] = t; }
    }

    /* LC 75 — sort colors / Dutch national flag. THREE pointers, one pass.
     *   [0..low-1] = 0s   [low..mid-1] = 1s   [high+1..] = 2s
     * Key subtlety: after swapping with `high`, do NOT advance mid - the value
     * you just pulled in is unexamined. After swapping with `low`, you CAN
     * advance, because that value is guaranteed to be a 1. */
    static void sortColors(int[] a) {
        int low = 0, mid = 0, high = a.length - 1;
        while (mid <= high) {
            switch (a[mid]) {
                case 0 -> { swap(a, low++, mid++); }
                case 1 -> mid++;
                default -> swap(a, mid, high--);      // do NOT increment mid
            }
        }
    }

    /* ======================================================================
     * PART 3 — SLIDING WINDOW, FIXED SIZE
     * ====================================================================== */

    /* Max sum of any subarray of size k. The template for all fixed windows. */
    static int maxSumFixed(int[] a, int k) {
        int sum = 0;
        for (int i = 0; i < k; i++) sum += a[i];       // first window
        int best = sum;
        for (int i = k; i < a.length; i++) {
            sum += a[i] - a[i - k];                    // slide: add new, drop old
            best = Math.max(best, sum);
        }
        return best;
    }

    /* LC 438 — find all anagram start indices. Fixed window + 26-count compare. */
    static List<Integer> findAnagrams(String s, String p) {
        List<Integer> res = new ArrayList<>();
        if (s.length() < p.length()) return res;
        int[] need = new int[26], win = new int[26];
        for (char c : p.toCharArray()) need[c - 'a']++;

        for (int i = 0; i < s.length(); i++) {
            win[s.charAt(i) - 'a']++;
            if (i >= p.length()) win[s.charAt(i - p.length()) - 'a']--;   // slide off
            if (i >= p.length() - 1 && Arrays.equals(need, win)) res.add(i - p.length() + 1);
        }
        return res;
    }

    /* ======================================================================
     * PART 4 — SLIDING WINDOW, VARIABLE SIZE
     * ====================================================================== */

    /* LC 3 — longest substring without repeating characters.
     * Store the LAST INDEX of each char. On a repeat, jump `left` past it.
     * The Math.max is essential - a stale index must never move left backwards. */
    static int lengthOfLongestSubstring(String s) {
        Map<Character, Integer> last = new HashMap<>();
        int left = 0, best = 0;
        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            if (last.containsKey(c))
                left = Math.max(left, last.get(c) + 1);   // never move left backwards
            last.put(c, right);
            best = Math.max(best, right - left + 1);
        }
        return best;
    }

    /* LC 424 — longest repeating character replacement (at most k changes).
     * Window is valid when (windowLength - countOfMostFrequentChar) <= k.
     * Note maxCount is never decreased - that's intentional and still correct
     * for finding the MAXIMUM length (the window only ever shrinks by one). */
    static int characterReplacement(String s, int k) {
        int[] count = new int[26];
        int left = 0, maxCount = 0, best = 0;
        for (int right = 0; right < s.length(); right++) {
            maxCount = Math.max(maxCount, ++count[s.charAt(right) - 'A']);
            while (right - left + 1 - maxCount > k) {     // too many replacements needed
                count[s.charAt(left) - 'A']--;
                left++;
            }
            best = Math.max(best, right - left + 1);
        }
        return best;
    }

    /* LC 209 — minimum size subarray with sum >= target. Positive numbers only. */
    static int minSubArrayLen(int target, int[] a) {
        int left = 0, sum = 0, best = Integer.MAX_VALUE;
        for (int right = 0; right < a.length; right++) {
            sum += a[right];
            while (sum >= target) {                       // shrink while still valid
                best = Math.min(best, right - left + 1);
                sum -= a[left++];
            }
        }
        return best == Integer.MAX_VALUE ? 0 : best;
    }

    /* LC 76 — minimum window substring. THE hardest standard window problem.
     * Track `need` counts and a `formed` counter of how many DISTINCT required
     * chars have hit their required count. Window is valid when formed == required. */
    static String minWindow(String s, String t) {
        if (s.length() < t.length()) return "";
        Map<Character, Integer> need = new HashMap<>();
        for (char c : t.toCharArray()) need.merge(c, 1, Integer::sum);

        Map<Character, Integer> win = new HashMap<>();
        int required = need.size(), formed = 0;
        int left = 0, bestLen = Integer.MAX_VALUE, bestStart = 0;

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            win.merge(c, 1, Integer::sum);
            if (need.containsKey(c) && win.get(c).intValue() == need.get(c).intValue())
                formed++;

            while (formed == required) {                  // valid -> try to shrink
                if (right - left + 1 < bestLen) { bestLen = right - left + 1; bestStart = left; }
                char lc = s.charAt(left++);
                win.merge(lc, -1, Integer::sum);
                if (need.containsKey(lc) && win.get(lc) < need.get(lc)) formed--;
            }
        }
        return bestLen == Integer.MAX_VALUE ? "" : s.substring(bestStart, bestStart + bestLen);
    }

    /* LC 121 — best time to buy and sell stock. A sliding window in disguise:
     * track the minimum so far and the best profit against it. */
    static int maxProfit(int[] prices) {
        int minPrice = Integer.MAX_VALUE, best = 0;
        for (int p : prices) {
            minPrice = Math.min(minPrice, p);
            best = Math.max(best, p - minPrice);
        }
        return best;
    }

    /* ======================================================================
     * PART 5 — PREFIX SUMS
     * ====================================================================== */

    /* LC 303 — immutable range sum. Build once, query O(1). */
    static class NumArray {
        private final int[] prefix;
        NumArray(int[] nums) {
            prefix = new int[nums.length + 1];
            for (int i = 0; i < nums.length; i++) prefix[i + 1] = prefix[i] + nums[i];
        }
        int sumRange(int i, int j) { return prefix[j + 1] - prefix[i]; }
    }

    /* LC 560 — count subarrays summing to k. WORKS WITH NEGATIVES (a sliding
     * window would not). If prefix[j] - prefix[i] == k, then prefix[i] = prefix[j] - k.
     * So count how many earlier prefixes equal (current - k).
     * Seed the map with {0: 1} for subarrays that start at index 0. */
    static int subarraySum(int[] nums, int k) {
        Map<Integer, Integer> seen = new HashMap<>();
        seen.put(0, 1);                      // empty prefix
        int sum = 0, count = 0;
        for (int n : nums) {
            sum += n;
            count += seen.getOrDefault(sum - k, 0);
            seen.merge(sum, 1, Integer::sum);
        }
        return count;
    }

    /* LC 238 — product of array except self, O(n), no division.
     * Pass 1: prefix products. Pass 2: multiply by suffix products on the fly. */
    static int[] productExceptSelf(int[] nums) {
        int n = nums.length;
        int[] res = new int[n];
        res[0] = 1;
        for (int i = 1; i < n; i++) res[i] = res[i - 1] * nums[i - 1];   // prefix
        int suffix = 1;
        for (int i = n - 1; i >= 0; i--) { res[i] *= suffix; suffix *= nums[i]; }
        return res;
    }

    /* ======================================================================
     * JAVA API NOTES FOR ARRAY/STRING WORK — demonstrated live above (char
     * math via c - 'a', Character.isLetterOrDigit in isPalindrome). Full
     * Arrays/String/StringBuilder syntax reference lives in
     * ../../java-api-examples.md.
     * ====================================================================== */

    private static void swap(int[] a, int i, int j) { int t = a[i]; a[i] = a[j]; a[j] = t; }

    /* ================================================================== */
    public static void main(String[] args) {
        System.out.println("--- two pointers, opposite ends ---");
        System.out.println("twoSumSorted([2,7,11,15],9) = " +
                Arrays.toString(twoSumSorted(new int[]{2,7,11,15}, 9)));
        System.out.println("isPalindrome('A man, a plan, a canal: Panama') = " +
                isPalindrome("A man, a plan, a canal: Panama"));
        System.out.println("maxArea([1,8,6,2,5,4,8,3,7]) = " +
                maxArea(new int[]{1,8,6,2,5,4,8,3,7}));
        System.out.println("threeSum([-1,0,1,2,-1,-4]) = " +
                threeSum(new int[]{-1,0,1,2,-1,-4}));
        System.out.println("trap([0,1,0,2,1,0,1,3,2,1,2,1]) = " +
                trap(new int[]{0,1,0,2,1,0,1,3,2,1,2,1}));

        System.out.println("\n--- two pointers, same direction ---");
        int[] dup = {1,1,2,2,3};
        System.out.println("removeDuplicates -> len " + removeDuplicates(dup)
                + ", arr " + Arrays.toString(dup));
        int[] z = {0,1,0,3,12};
        moveZeroes(z);
        System.out.println("moveZeroes = " + Arrays.toString(z));
        int[] colors = {2,0,2,1,1,0};
        sortColors(colors);
        System.out.println("sortColors = " + Arrays.toString(colors));

        System.out.println("\n--- fixed window ---");
        System.out.println("maxSumFixed([2,1,5,1,3,2], k=3) = " +
                maxSumFixed(new int[]{2,1,5,1,3,2}, 3));
        System.out.println("findAnagrams('cbaebabacd','abc') = " +
                findAnagrams("cbaebabacd", "abc"));

        System.out.println("\n--- variable window ---");
        System.out.println("lengthOfLongestSubstring('abcabcbb') = " +
                lengthOfLongestSubstring("abcabcbb"));
        System.out.println("characterReplacement('AABABBA', k=1) = " +
                characterReplacement("AABABBA", 1));
        System.out.println("minSubArrayLen(7, [2,3,1,2,4,3]) = " +
                minSubArrayLen(7, new int[]{2,3,1,2,4,3}));
        System.out.println("minWindow('ADOBECODEBANC','ABC') = " +
                minWindow("ADOBECODEBANC", "ABC"));
        System.out.println("maxProfit([7,1,5,3,6,4]) = " +
                maxProfit(new int[]{7,1,5,3,6,4}));

        System.out.println("\n--- prefix sums ---");
        NumArray na = new NumArray(new int[]{-2,0,3,-5,2,-1});
        System.out.println("sumRange(0,2)=" + na.sumRange(0,2)
                + " sumRange(2,5)=" + na.sumRange(2,5));
        System.out.println("subarraySum([1,-1,0], k=0) = " +
                subarraySum(new int[]{1,-1,0}, 0) + "   (negatives! not a window problem)");
        System.out.println("productExceptSelf([1,2,3,4]) = " +
                Arrays.toString(productExceptSelf(new int[]{1,2,3,4})));
    }
}

/* ==========================================================================
 * PRACTICE — BLIND 75 / NEETCODE
 * ==========================================================================
 *  TWO POINTERS
 *   LC 125  Valid Palindrome                      easy
 *   LC 167  Two Sum II (sorted)                   med
 *   LC 15   3Sum                                  med    skip duplicates
 *   LC 11   Container With Most Water             med    move the shorter side
 *   LC 42   Trapping Rain Water                   hard   two-pointer O(1) space
 *   LC 26   Remove Duplicates from Sorted Array   easy   read/write pointers
 *   LC 283  Move Zeroes                           easy
 *   LC 75   Sort Colors                           med    Dutch national flag
 *
 *  SLIDING WINDOW
 *   LC 121  Best Time to Buy and Sell Stock       easy
 *   LC 3    Longest Substring Without Repeating   med    THE window problem
 *   LC 424  Longest Repeating Char Replacement    med
 *   LC 567  Permutation in String                 med    fixed window
 *   LC 438  Find All Anagrams in a String         med    fixed window
 *   LC 209  Minimum Size Subarray Sum             med
 *   LC 76   Minimum Window Substring              hard   formed/required counter
 *   LC 239  Sliding Window Maximum                hard   monotonic deque
 *
 *  PREFIX SUMS
 *   LC 303  Range Sum Query - Immutable           easy
 *   LC 238  Product of Array Except Self          med    prefix * suffix
 *   LC 560  Subarray Sum Equals K                 med    prefix + hashmap
 *   LC 523  Continuous Subarray Sum               med    prefix mod k
 *   LC 525  Contiguous Array                      med    map 0 -> -1, prefix
 *   LC 304  Range Sum Query 2D                    med    2D prefix sums
 *
 * Self-test questions + answers: see ./README.md
 * ========================================================================== */
