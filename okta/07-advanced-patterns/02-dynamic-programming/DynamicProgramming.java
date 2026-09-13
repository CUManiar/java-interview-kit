import java.util.*;

/* ==========================================================================
 * ALGOS: DYNAMIC PROGRAMMING
 * Run: java DynamicProgramming.java
 * ==========================================================================
 *
 * WHEN IS IT DP?
 * --------------
 *  1. OPTIMAL SUBSTRUCTURE  - the answer is built from answers to smaller
 *                             instances of the SAME problem
 *  2. OVERLAPPING SUBPROBLEMS - the same smaller instance is needed many times
 *  Signals in the prompt: "how many ways", "minimum/maximum cost", "longest",
 *  "can you reach/partition", "count the paths". Plus: brute force is exponential.
 *
 *  If subproblems DON'T overlap, it's divide-and-conquer (merge sort), not DP.
 *  If a greedy local choice is provably optimal, it's greedy, not DP.
 *
 * THE 5-STEP RECIPE — follow it every time, don't improvise
 * --------------------------------------------------------
 *  1. STATE      What uniquely identifies a subproblem? -> dp[i], dp[i][j], dp[i][cap]
 *  2. RECURRENCE How does a state combine smaller states?
 *  3. BASE CASE  The smallest states, filled in directly.
 *  4. ORDER      Which direction fills the table so dependencies are ready first?
 *  5. ANSWER     Which cell holds the final result? (not always the last one)
 *
 * TOP-DOWN vs BOTTOM-UP
 * ---------------------
 *   MEMOISATION (top-down): recursion + a cache.
 *     + mirrors the natural recursive definition, only computes reachable states
 *     - recursion depth (StackOverflow at ~10k frames), function-call overhead
 *   TABULATION (bottom-up): fill an array with loops.
 *     + no stack risk, easy to space-optimise down to O(1) rows
 *     - you must work out the fill order yourself
 *   Write the recursion first, add a cache, then convert to a table if needed.
 *
 * FIBONACCI — the whole idea in one picture
 * -----------------------------------------
 *   naive recursion, exponential:              memoised, linear:
 *              f(5)                              f(5)
 *            /      \                           /    \
 *         f(4)      f(3)                     f(4)   [f(3) CACHED]
 *        /   \      /   \                    /   \
 *     f(3)  f(2) f(2)  f(1)              f(3)  [f(2) CACHED]
 *     ...   ...  ...    (recomputed!)
 *
 * THE PATTERN CATALOGUE  (recognise the shape -> you already know the code)
 * ┌────────────────────────┬──────────────────────┬────────────────────────────┐
 * │ Pattern                │ State                │ Example                    │
 * ├────────────────────────┼──────────────────────┼────────────────────────────┤
 * │ Linear / 1D            │ dp[i]                │ climb stairs, house robber │
 * │ Grid / 2D paths        │ dp[r][c]             │ unique paths, min path sum │
 * │ Two sequences          │ dp[i][j]             │ LCS, edit distance         │
 * │ 0/1 Knapsack           │ dp[i][capacity]      │ subset sum, partition      │
 * │ Unbounded knapsack     │ dp[capacity]         │ coin change, rod cutting   │
 * │ Interval               │ dp[i][j] over ranges │ burst balloons, MCM        │
 * │ Subsequence (LIS)      │ dp[i] = best ending i│ LIS, russian dolls         │
 * │ State machine          │ dp[i][state]         │ stock with cooldown/fee    │
 * │ Digit / bitmask        │ dp[mask]             │ TSP, count subsets         │
 * └────────────────────────┴──────────────────────┴────────────────────────────┘
 * ========================================================================== */
public class DynamicProgramming {

    /* ======================================================================
     * 0. FIBONACCI — three versions, to see the progression
     * ====================================================================== */
    static long fibNaive(int n) {                        // O(2^n) - don't ship this
        return n < 2 ? n : fibNaive(n - 1) + fibNaive(n - 2);
    }
    static long fibMemo(int n, long[] memo) {            // O(n) time, O(n) space
        if (n < 2) return n;
        if (memo[n] != 0) return memo[n];
        return memo[n] = fibMemo(n - 1, memo) + fibMemo(n - 2, memo);
    }
    static long fibTab(int n) {                          // O(n) time, O(1) space
        if (n < 2) return n;
        long a = 0, b = 1;
        for (int i = 2; i <= n; i++) { long c = a + b; a = b; b = c; }
        return b;
    }

    /* ======================================================================
     * 1. LINEAR DP
     * ====================================================================== */

    /* LC 70 — climbing stairs. dp[i] = dp[i-1] + dp[i-2]. It's fibonacci. */
    static int climbStairs(int n) {
        if (n <= 2) return n;
        int prev2 = 1, prev1 = 2;
        for (int i = 3; i <= n; i++) { int cur = prev1 + prev2; prev2 = prev1; prev1 = cur; }
        return prev1;
    }

    /* LC 198 — house robber. Can't rob adjacent houses.
     * STATE: dp[i] = max loot from houses 0..i
     * RECURRENCE: dp[i] = max(dp[i-1],            <- skip house i
     *                         dp[i-2] + nums[i])  <- rob house i
     * Only two previous values are needed -> O(1) space. */
    static int rob(int[] nums) {
        int prev2 = 0, prev1 = 0;
        for (int n : nums) {
            int cur = Math.max(prev1, prev2 + n);
            prev2 = prev1;
            prev1 = cur;
        }
        return prev1;
    }

    /* LC 213 — house robber II, houses in a CIRCLE.
     * Trick: the first and last are adjacent, so the answer is
     *   max( rob(houses 0..n-2), rob(houses 1..n-1) ). */
    static int robCircular(int[] nums) {
        if (nums.length == 1) return nums[0];
        return Math.max(rob(Arrays.copyOfRange(nums, 0, nums.length - 1)),
                        rob(Arrays.copyOfRange(nums, 1, nums.length)));
    }

    /* LC 746 — min cost climbing stairs. */
    static int minCostClimbingStairs(int[] cost) {
        int a = 0, b = 0;
        for (int i = 2; i <= cost.length; i++) {
            int cur = Math.min(b + cost[i - 1], a + cost[i - 2]);
            a = b; b = cur;
        }
        return b;
    }

    /* LC 139 — word break.
     * STATE: dp[i] = can s[0..i) be segmented?
     * RECURRENCE: dp[i] = OR over j<i of (dp[j] && s[j..i) is in the dict) */
    static boolean wordBreak(String s, List<String> wordDict) {
        Set<String> dict = new HashSet<>(wordDict);
        boolean[] dp = new boolean[s.length() + 1];
        dp[0] = true;                                   // empty string is always breakable
        for (int i = 1; i <= s.length(); i++)
            for (int j = 0; j < i; j++)
                if (dp[j] && dict.contains(s.substring(j, i))) { dp[i] = true; break; }
        return dp[s.length()];
    }

    /* LC 91 — decode ways. Careful base cases and the '0' trap. */
    static int numDecodings(String s) {
        if (s.isEmpty() || s.charAt(0) == '0') return 0;
        int prev2 = 1, prev1 = 1;                       // dp[0]=1, dp[1]=1
        for (int i = 1; i < s.length(); i++) {
            int cur = 0;
            if (s.charAt(i) != '0') cur += prev1;                      // single digit
            int two = Integer.parseInt(s.substring(i - 1, i + 1));
            if (two >= 10 && two <= 26) cur += prev2;                  // two digits
            prev2 = prev1; prev1 = cur;
            if (cur == 0) return 0;
        }
        return prev1;
    }

    /* ======================================================================
     * 2. SUBSEQUENCE DP — LIS
     * ====================================================================== */

    /* LC 300 — longest increasing subsequence, O(n^2) DP.
     * STATE: dp[i] = length of the LIS that ENDS at index i. */
    static int lengthOfLIS(int[] nums) {
        int[] dp = new int[nums.length];
        Arrays.fill(dp, 1);                             // every element alone is an LIS of 1
        int best = 1;
        for (int i = 1; i < nums.length; i++) {
            for (int j = 0; j < i; j++)
                if (nums[j] < nums[i]) dp[i] = Math.max(dp[i], dp[j] + 1);
            best = Math.max(best, dp[i]);
        }
        return nums.length == 0 ? 0 : best;
    }

    /* LC 300 — O(n log n) via patience sorting.
     * `tails[k]` = smallest possible tail of an increasing subsequence of length k+1.
     * tails is always sorted, so binary search it. The array is NOT the LIS itself,
     * only its LENGTH is meaningful. Interviewers love this one. */
    static int lengthOfLISFast(int[] nums) {
        int[] tails = new int[nums.length];
        int size = 0;
        for (int x : nums) {
            int lo = 0, hi = size;
            while (lo < hi) {                           // lowerBound
                int mid = lo + (hi - lo) / 2;
                if (tails[mid] < x) lo = mid + 1; else hi = mid;
            }
            tails[lo] = x;                              // replace or append
            if (lo == size) size++;
        }
        return size;
    }

    /* ======================================================================
     * 3. GRID DP
     * ====================================================================== */

    /* LC 62 — unique paths in an m x n grid, moving only right/down.
     * dp[r][c] = dp[r-1][c] + dp[r][c-1]. Collapses to a single row. */
    static int uniquePaths(int m, int n) {
        int[] row = new int[n];
        Arrays.fill(row, 1);                            // top row: exactly one path each
        for (int r = 1; r < m; r++)
            for (int c = 1; c < n; c++)
                row[c] += row[c - 1];                   // row[c] is "from above", row[c-1] "from left"
        return row[n - 1];
    }

    /* LC 64 — minimum path sum. Same shape, min instead of sum. */
    static int minPathSum(int[][] grid) {
        int m = grid.length, n = grid[0].length;
        int[] dp = new int[n];
        dp[0] = grid[0][0];
        for (int c = 1; c < n; c++) dp[c] = dp[c - 1] + grid[0][c];
        for (int r = 1; r < m; r++) {
            dp[0] += grid[r][0];
            for (int c = 1; c < n; c++)
                dp[c] = Math.min(dp[c], dp[c - 1]) + grid[r][c];
        }
        return dp[n - 1];
    }

    /* ======================================================================
     * 4. TWO-SEQUENCE DP  (dp[i][j] over two strings)
     * ====================================================================== */

    /* LC 1143 — longest common subsequence.
     *
     *        ""  a  c  e
     *    "" [ 0  0  0  0 ]
     *    a  [ 0  1  1  1 ]
     *    b  [ 0  1  1  1 ]
     *    c  [ 0  1  2  2 ]
     *    d  [ 0  1  2  2 ]
     *    e  [ 0  1  2  3 ]  <- answer
     *
     * chars match  -> dp[i][j] = dp[i-1][j-1] + 1   (diagonal)
     * chars differ -> dp[i][j] = max(dp[i-1][j], dp[i][j-1])
     */
    static int longestCommonSubsequence(String a, String b) {
        int m = a.length(), n = b.length();
        int[][] dp = new int[m + 1][n + 1];             // +1 row/col for the empty prefix
        for (int i = 1; i <= m; i++)
            for (int j = 1; j <= n; j++)
                dp[i][j] = (a.charAt(i-1) == b.charAt(j-1))
                        ? dp[i-1][j-1] + 1
                        : Math.max(dp[i-1][j], dp[i][j-1]);
        return dp[m][n];
    }

    /* LC 72 — edit distance (Levenshtein). Three operations, three predecessors:
     *   dp[i-1][j]   = DELETE from a
     *   dp[i][j-1]   = INSERT into a
     *   dp[i-1][j-1] = REPLACE
     */
    static int editDistance(String a, String b) {
        int m = a.length(), n = b.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) dp[i][0] = i;      // delete everything
        for (int j = 0; j <= n; j++) dp[0][j] = j;      // insert everything
        for (int i = 1; i <= m; i++)
            for (int j = 1; j <= n; j++)
                dp[i][j] = (a.charAt(i-1) == b.charAt(j-1))
                        ? dp[i-1][j-1]
                        : 1 + Math.min(dp[i-1][j-1], Math.min(dp[i-1][j], dp[i][j-1]));
        return dp[m][n];
    }

    /* ======================================================================
     * 5. KNAPSACK
     * ====================================================================== */

    /* 0/1 KNAPSACK — each item used AT MOST ONCE.
     * STATE: dp[cap] = best value achievable with capacity cap.
     * ITERATE CAPACITY DOWNWARDS. That's the entire difference from unbounded:
     * going downwards means dp[cap - w] still refers to the PREVIOUS item row,
     * so each item is used at most once. */
    static int knapsack01(int[] weights, int[] values, int capacity) {
        int[] dp = new int[capacity + 1];
        for (int i = 0; i < weights.length; i++)
            for (int cap = capacity; cap >= weights[i]; cap--)      // DOWNWARDS
                dp[cap] = Math.max(dp[cap], dp[cap - weights[i]] + values[i]);
        return dp[capacity];
    }

    /* LC 416 — partition equal subset sum. 0/1 knapsack over a boolean target. */
    static boolean canPartition(int[] nums) {
        int sum = Arrays.stream(nums).sum();
        if ((sum & 1) == 1) return false;               // odd total can't split evenly
        int target = sum / 2;
        boolean[] dp = new boolean[target + 1];
        dp[0] = true;
        for (int n : nums)
            for (int t = target; t >= n; t--)           // DOWNWARDS: each number once
                dp[t] |= dp[t - n];
        return dp[target];
    }

    /* LC 322 — coin change, UNBOUNDED knapsack (unlimited coins).
     * ITERATE UPWARDS so dp[amt - coin] can already include this coin. */
    static int coinChange(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, amount + 1);                    // "infinity" sentinel
        dp[0] = 0;
        for (int coin : coins)
            for (int amt = coin; amt <= amount; amt++)  // UPWARDS
                dp[amt] = Math.min(dp[amt], dp[amt - coin] + 1);
        return dp[amount] > amount ? -1 : dp[amount];
    }

    /* LC 518 — coin change II, COUNT the combinations.
     * Coin loop OUTSIDE, amount loop inside -> counts COMBINATIONS {1,2} once.
     * Swap the loops and you count PERMUTATIONS ({1,2} and {2,1} separately).
     * This loop-order distinction is a favourite interview trap. */
    static int coinChangeCombinations(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        dp[0] = 1;
        for (int coin : coins)                          // OUTER: coin
            for (int amt = coin; amt <= amount; amt++)  // INNER: amount
                dp[amt] += dp[amt - coin];
        return dp[amount];
    }

    /* ======================================================================
     * 6. PALINDROME DP
     * ====================================================================== */

    /* LC 5 — longest palindromic substring, EXPAND AROUND CENTER.
     * O(n^2) time, O(1) space. Simpler than the DP table and just as fast.
     * 2n-1 centers: n single chars + n-1 gaps between chars. */
    static String longestPalindrome(String s) {
        if (s.isEmpty()) return "";
        int start = 0, len = 1;
        for (int i = 0; i < s.length(); i++) {
            int l1 = expand(s, i, i);         // odd length, center on a char
            int l2 = expand(s, i, i + 1);     // even length, center between chars
            int cur = Math.max(l1, l2);
            if (cur > len) { len = cur; start = i - (cur - 1) / 2; }
        }
        return s.substring(start, start + len);
    }
    private static int expand(String s, int l, int r) {
        while (l >= 0 && r < s.length() && s.charAt(l) == s.charAt(r)) { l--; r++; }
        return r - l - 1;
    }

    /* LC 647 — count palindromic substrings. Same expansion. */
    static int countSubstrings(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            count += countExpand(s, i, i);
            count += countExpand(s, i, i + 1);
        }
        return count;
    }
    private static int countExpand(String s, int l, int r) {
        int c = 0;
        while (l >= 0 && r < s.length() && s.charAt(l) == s.charAt(r)) { l--; r++; c++; }
        return c;
    }

    /* ======================================================================
     * 7. SUBARRAY DP
     * ====================================================================== */

    /* LC 53 — maximum subarray (Kadane's).
     * At each index: either extend the previous best, or start fresh here. */
    static int maxSubArray(int[] nums) {
        int cur = nums[0], best = nums[0];
        for (int i = 1; i < nums.length; i++) {
            cur = Math.max(nums[i], cur + nums[i]);     // extend or restart
            best = Math.max(best, cur);
        }
        return best;
    }

    /* LC 152 — maximum product subarray. Negatives flip min<->max, so track BOTH.
     * A large negative min becomes a large positive max when multiplied by a negative. */
    static int maxProduct(int[] nums) {
        int max = nums[0], min = nums[0], best = nums[0];
        for (int i = 1; i < nums.length; i++) {
            int n = nums[i];
            if (n < 0) { int t = max; max = min; min = t; }   // swap on a negative
            max = Math.max(n, max * n);
            min = Math.min(n, min * n);
            best = Math.max(best, max);
        }
        return best;
    }

    /* ======================================================================
     * 8. STATE-MACHINE DP
     * ====================================================================== */

    /* LC 309 — stock with cooldown. Three states per day:
     *   HOLD    = holding a stock
     *   SOLD    = just sold today (must cool down tomorrow)
     *   REST    = free to buy
     *
     *        buy            sell
     *   REST ---> HOLD ------------> SOLD
     *    ^  \___stay___/   \__stay__/  |
     *    |________________cooldown_____|
     */
    static int maxProfitWithCooldown(int[] prices) {
        if (prices.length == 0) return 0;
        int hold = -prices[0], sold = 0, rest = 0;
        for (int i = 1; i < prices.length; i++) {
            int prevSold = sold;
            sold = hold + prices[i];                     // sell today
            hold = Math.max(hold, rest - prices[i]);     // keep holding, or buy today
            rest = Math.max(rest, prevSold);             // stay resting, or cooldown ends
        }
        return Math.max(sold, rest);
    }

    /* ======================================================================
     * 9. MEMOISATION TEMPLATE — when the recursion is easier than the table
     * ====================================================================== */
    static int uniquePathsMemo(int m, int n) {
        Integer[][] memo = new Integer[m][n];            // Integer so null = "not computed"
        return paths(0, 0, m, n, memo);
    }
    private static int paths(int r, int c, int m, int n, Integer[][] memo) {
        if (r == m - 1 && c == n - 1) return 1;          // reached the target
        if (r >= m || c >= n) return 0;                  // off the grid
        if (memo[r][c] != null) return memo[r][c];       // cache hit
        return memo[r][c] = paths(r + 1, c, m, n, memo) + paths(r, c + 1, m, n, memo);
    }

    /* ================================================================== */
    public static void main(String[] args) {
        System.out.println("--- fibonacci ---");
        long t0 = System.nanoTime(); fibNaive(32);
        long naive = (System.nanoTime() - t0) / 1_000_000;
        t0 = System.nanoTime(); fibMemo(32, new long[33]);
        long memo = (System.nanoTime() - t0) / 1_000_000;
        System.out.println("fib(32) naive " + naive + "ms vs memo " + memo + "ms");
        System.out.println("fibTab(50) = " + fibTab(50));

        System.out.println("\n--- linear DP ---");
        System.out.println("climbStairs(5)          = " + climbStairs(5));
        System.out.println("rob([2,7,9,3,1])        = " + rob(new int[]{2,7,9,3,1}));
        System.out.println("robCircular([2,3,2])    = " + robCircular(new int[]{2,3,2}));
        System.out.println("minCostClimbing([10,15,20]) = " +
                minCostClimbingStairs(new int[]{10,15,20}));
        System.out.println("wordBreak('leetcode')   = " +
                wordBreak("leetcode", List.of("leet","code")));
        System.out.println("numDecodings('226')     = " + numDecodings("226"));

        System.out.println("\n--- LIS ---");
        int[] lis = {10,9,2,5,3,7,101,18};
        System.out.println("O(n^2)    = " + lengthOfLIS(lis));
        System.out.println("O(n logn) = " + lengthOfLISFast(lis));

        System.out.println("\n--- grid DP ---");
        System.out.println("uniquePaths(3,7)     = " + uniquePaths(3, 7));
        System.out.println("uniquePathsMemo(3,7) = " + uniquePathsMemo(3, 7));
        System.out.println("minPathSum           = " +
                minPathSum(new int[][]{{1,3,1},{1,5,1},{4,2,1}}));

        System.out.println("\n--- two sequences ---");
        System.out.println("LCS('abcde','ace')            = " +
                longestCommonSubsequence("abcde", "ace"));
        System.out.println("editDistance('horse','ros')   = " +
                editDistance("horse", "ros"));

        System.out.println("\n--- knapsack ---");
        System.out.println("knapsack01 w=[1,3,4,5] v=[1,4,5,7] cap=7 = " +
                knapsack01(new int[]{1,3,4,5}, new int[]{1,4,5,7}, 7));
        System.out.println("canPartition([1,5,11,5])   = " +
                canPartition(new int[]{1,5,11,5}));
        System.out.println("coinChange([1,2,5], 11)    = " +
                coinChange(new int[]{1,2,5}, 11));
        System.out.println("coinChangeCombos([1,2,5],5)= " +
                coinChangeCombinations(new int[]{1,2,5}, 5));

        System.out.println("\n--- palindrome ---");
        System.out.println("longestPalindrome('babad') = " + longestPalindrome("babad"));
        System.out.println("countSubstrings('aaa')     = " + countSubstrings("aaa"));

        System.out.println("\n--- subarray ---");
        System.out.println("maxSubArray([-2,1,-3,4,-1,2,1,-5,4]) = " +
                maxSubArray(new int[]{-2,1,-3,4,-1,2,1,-5,4}));
        System.out.println("maxProduct([2,3,-2,4])               = " +
                maxProduct(new int[]{2,3,-2,4}));

        System.out.println("\n--- state machine ---");
        System.out.println("maxProfitWithCooldown([1,2,3,0,2]) = " +
                maxProfitWithCooldown(new int[]{1,2,3,0,2}));
    }
}

/* ==========================================================================
 * PRACTICE — BLIND 75 / NEETCODE (DP), in the order you should do them
 * ==========================================================================
 *  1D
 *   LC 70   Climbing Stairs                       easy   start here
 *   LC 746  Min Cost Climbing Stairs              easy
 *   LC 198  House Robber                          med
 *   LC 213  House Robber II                       med    circle trick
 *   LC 91   Decode Ways                           med    the '0' trap
 *   LC 139  Word Break                            med
 *   LC 300  Longest Increasing Subsequence        med    do BOTH n^2 and n log n
 *   LC 152  Maximum Product Subarray              med    track min AND max
 *   LC 53   Maximum Subarray                      med    Kadane
 *   LC 322  Coin Change                           med    unbounded knapsack
 *   LC 518  Coin Change II                        med    loop ORDER matters
 *   LC 377  Combination Sum IV                    med    the opposite loop order
 *   LC 416  Partition Equal Subset Sum            med    0/1 knapsack
 *   LC 55   Jump Game                             med    greedy beats DP here
 *   LC 45   Jump Game II                          med
 *
 *  2D
 *   LC 62   Unique Paths                          med
 *   LC 64   Minimum Path Sum                      med
 *   LC 1143 Longest Common Subsequence            med    the 2-string template
 *   LC 72   Edit Distance                         hard   3 predecessors
 *   LC 5    Longest Palindromic Substring         med    expand around center
 *   LC 647  Palindromic Substrings                med
 *   LC 10   Regular Expression Matching           hard
 *   LC 97   Interleaving String                   med
 *   LC 309  Best Time to Buy/Sell with Cooldown   med    state machine
 *   LC 312  Burst Balloons                        hard   interval DP
 *   LC 329  Longest Increasing Path in a Matrix   hard   DFS + memo
 *
 * SELF-TEST QUESTIONS
 *   - Name the 5 steps of setting up a DP.
 *   - 0/1 knapsack iterates capacity downwards, unbounded upwards. WHY?
 *   - LC 518 vs LC 377: which loop is outer, and what does swapping them change?
 *   - Why must maxProduct track the minimum too?
 *   - LIS in O(n log n): what does tails[k] actually mean? Is tails the LIS?
 *   - When is memoisation better than tabulation, and vice versa?
 *   - Edit distance: name the three predecessor cells and their operations.
 *   - Why is expand-around-center preferred over the DP table for LC 5?
 * ========================================================================== */
