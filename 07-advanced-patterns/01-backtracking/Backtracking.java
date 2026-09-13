import java.util.*;

/* ==========================================================================
 * ALGOS: BACKTRACKING (+ recursion, bit manipulation, intervals, greedy)
 * Run: java Backtracking.java
 * ==========================================================================
 *
 * WHAT BACKTRACKING IS
 * --------------------
 * DFS over a decision tree. At each node you CHOOSE an option, EXPLORE deeper,
 * then UNDO the choice so you can try the next one.
 *
 *   void backtrack(path, choices) {
 *       if (goalReached) { record(path); return; }
 *       for (choice : choices) {
 *           if (!valid(choice)) continue;   // PRUNE - the real optimisation
 *           path.add(choice);               // CHOOSE
 *           backtrack(path, next);          // EXPLORE
 *           path.remove(last);              // UNDO   <- forget this and everything breaks
 *       }
 *   }
 *
 * THE DECISION TREE for subsets of [1,2,3]
 *
 *                        []
 *              /                    \
 *          [1] (take 1)          [] (skip 1)
 *          /     \                /      \
 *      [1,2]    [1]            [2]       []
 *      /  \     /  \           /  \      /  \
 *  [1,2,3][1,2][1,3][1]    [2,3] [2]  [3]  []
 *
 *   2^3 = 8 leaves = 8 subsets. Every backtracking problem is a tree like this;
 *   the only differences are the branching rule and the pruning rule.
 *
 * COMPLEXITY CHEAT SHEET
 * ----------------------
 *   subsets       O(n * 2^n)      2^n subsets, O(n) to copy each
 *   permutations  O(n * n!)
 *   combinations  O(k * C(n,k))
 *   N-queens      O(n!)           with pruning, far better in practice
 *   word search   O(m*n*4^L)
 *
 * THE THREE THINGS PEOPLE GET WRONG
 *   1. Forgetting to undo the choice.
 *   2. Adding the path by REFERENCE instead of copying -> everything ends up empty.
 *      Always `new ArrayList<>(path)` when recording.
 *   3. Handling duplicates: SORT first, then skip `i > start && a[i] == a[i-1]`.
 * ========================================================================== */
public class Backtracking {

    /* ======================================================================
     * 1. SUBSETS  (LC 78) — the base template. Every element: take it or don't.
     * ====================================================================== */
    static List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        backtrackSubsets(nums, 0, new ArrayList<>(), res);
        return res;
    }
    private static void backtrackSubsets(int[] nums, int start,
                                         List<Integer> path, List<List<Integer>> res) {
        res.add(new ArrayList<>(path));          // COPY - every node is a valid subset
        for (int i = start; i < nums.length; i++) {
            path.add(nums[i]);                   // CHOOSE
            backtrackSubsets(nums, i + 1, path, res);   // EXPLORE (i+1: no reuse)
            path.remove(path.size() - 1);        // UNDO
        }
    }

    /* LC 90 — subsets with duplicates. Sort, then skip repeats AT THE SAME LEVEL.
     * The `i > start` guard is what limits the skip to siblings, not ancestors. */
    static List<List<Integer>> subsetsWithDup(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> res = new ArrayList<>();
        dupSubsets(nums, 0, new ArrayList<>(), res);
        return res;
    }
    private static void dupSubsets(int[] nums, int start,
                                   List<Integer> path, List<List<Integer>> res) {
        res.add(new ArrayList<>(path));
        for (int i = start; i < nums.length; i++) {
            if (i > start && nums[i] == nums[i - 1]) continue;   // skip duplicate SIBLING
            path.add(nums[i]);
            dupSubsets(nums, i + 1, path, res);
            path.remove(path.size() - 1);
        }
    }

    /* ======================================================================
     * 2. PERMUTATIONS  (LC 46) — order matters, so iterate from 0 with a used[] flag
     * ====================================================================== */
    static List<List<Integer>> permute(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        backtrackPermute(nums, new boolean[nums.length], new ArrayList<>(), res);
        return res;
    }
    private static void backtrackPermute(int[] nums, boolean[] used,
                                         List<Integer> path, List<List<Integer>> res) {
        if (path.size() == nums.length) { res.add(new ArrayList<>(path)); return; }
        for (int i = 0; i < nums.length; i++) {       // from 0, NOT from start
            if (used[i]) continue;
            used[i] = true;  path.add(nums[i]);
            backtrackPermute(nums, used, path, res);
            path.remove(path.size() - 1);  used[i] = false;    // UNDO both
        }
    }

    /* ======================================================================
     * 3. COMBINATION SUM  (LC 39) — unlimited reuse: recurse with `i`, not `i+1`
     * ====================================================================== */
    static List<List<Integer>> combinationSum(int[] candidates, int target) {
        Arrays.sort(candidates);                      // enables the break-prune below
        List<List<Integer>> res = new ArrayList<>();
        combo(candidates, target, 0, new ArrayList<>(), res);
        return res;
    }
    private static void combo(int[] c, int remain, int start,
                              List<Integer> path, List<List<Integer>> res) {
        if (remain == 0) { res.add(new ArrayList<>(path)); return; }
        for (int i = start; i < c.length; i++) {
            if (c[i] > remain) break;                 // PRUNE: sorted, so all later are worse
            path.add(c[i]);
            combo(c, remain - c[i], i, path, res);    // `i` -> reuse the same candidate
            path.remove(path.size() - 1);
        }
    }

    /* LC 40 — combination sum II: each candidate used once, and no duplicate sets. */
    static List<List<Integer>> combinationSum2(int[] candidates, int target) {
        Arrays.sort(candidates);
        List<List<Integer>> res = new ArrayList<>();
        combo2(candidates, target, 0, new ArrayList<>(), res);
        return res;
    }
    private static void combo2(int[] c, int remain, int start,
                               List<Integer> path, List<List<Integer>> res) {
        if (remain == 0) { res.add(new ArrayList<>(path)); return; }
        for (int i = start; i < c.length; i++) {
            if (i > start && c[i] == c[i - 1]) continue;   // skip duplicate sibling
            if (c[i] > remain) break;
            path.add(c[i]);
            combo2(c, remain - c[i], i + 1, path, res);    // i+1 -> use each once
            path.remove(path.size() - 1);
        }
    }

    /* ======================================================================
     * 4. WORD SEARCH  (LC 79) — backtracking on a grid.
     * Mark the cell, recurse in 4 directions, then RESTORE it. The restore is
     * what makes it backtracking rather than flood fill.
     * ====================================================================== */
    static boolean exist(char[][] board, String word) {
        for (int r = 0; r < board.length; r++)
            for (int c = 0; c < board[0].length; c++)
                if (dfsWord(board, r, c, word, 0)) return true;
        return false;
    }
    private static boolean dfsWord(char[][] b, int r, int c, String w, int idx) {
        if (idx == w.length()) return true;                       // matched everything
        if (r < 0 || c < 0 || r >= b.length || c >= b[0].length) return false;
        if (b[r][c] != w.charAt(idx)) return false;

        char saved = b[r][c];
        b[r][c] = '#';                                            // mark as on-path
        boolean found = dfsWord(b, r+1, c, w, idx+1)
                     || dfsWord(b, r-1, c, w, idx+1)
                     || dfsWord(b, r, c+1, w, idx+1)
                     || dfsWord(b, r, c-1, w, idx+1);
        b[r][c] = saved;                                          // UNDO
        return found;
    }

    /* ======================================================================
     * 5. N-QUEENS  (LC 51) — the pruning showcase.
     *
     * Place one queen per ROW. Track three conflict sets:
     *   cols          -> column c is taken
     *   diag  (r - c) -> the "\" diagonal; constant along it (offset by n to stay >= 0)
     *   anti  (r + c) -> the "/" diagonal; constant along it
     *
     *   r-c for a 4x4:        r+c for a 4x4:
     *     0  -1 -2 -3           0  1  2  3
     *     1   0 -1 -2           1  2  3  4
     *     2   1  0 -1           2  3  4  5
     *     3   2  1  0           3  4  5  6
     *
     * O(1) conflict checks turn an O(n!) search into something that finishes.
     * ====================================================================== */
    static List<List<String>> solveNQueens(int n) {
        List<List<String>> res = new ArrayList<>();
        int[] queenCol = new int[n];                  // queenCol[r] = column of the queen in row r
        boolean[] cols = new boolean[n];
        boolean[] diag = new boolean[2 * n];          // r - c + n
        boolean[] anti = new boolean[2 * n];          // r + c
        placeQueen(0, n, queenCol, cols, diag, anti, res);
        return res;
    }
    private static void placeQueen(int r, int n, int[] qc, boolean[] cols,
                                   boolean[] diag, boolean[] anti, List<List<String>> res) {
        if (r == n) { res.add(render(qc, n)); return; }
        for (int c = 0; c < n; c++) {
            int d = r - c + n, a = r + c;
            if (cols[c] || diag[d] || anti[a]) continue;     // PRUNE in O(1)
            cols[c] = diag[d] = anti[a] = true;  qc[r] = c;  // CHOOSE
            placeQueen(r + 1, n, qc, cols, diag, anti, res); // EXPLORE
            cols[c] = diag[d] = anti[a] = false;             // UNDO
        }
    }
    private static List<String> render(int[] qc, int n) {
        List<String> board = new ArrayList<>(n);
        for (int r = 0; r < n; r++) {
            char[] row = new char[n];
            Arrays.fill(row, '.');
            row[qc[r]] = 'Q';
            board.add(new String(row));
        }
        return board;
    }

    /* ======================================================================
     * 6. LETTER COMBINATIONS OF A PHONE NUMBER  (LC 17)
     * ====================================================================== */
    private static final String[] PAD = {"", "", "abc", "def", "ghi", "jkl",
                                         "mno", "pqrs", "tuv", "wxyz"};
    static List<String> letterCombinations(String digits) {
        List<String> res = new ArrayList<>();
        if (digits.isEmpty()) return res;
        letters(digits, 0, new StringBuilder(), res);
        return res;
    }
    private static void letters(String d, int i, StringBuilder sb, List<String> res) {
        if (i == d.length()) { res.add(sb.toString()); return; }
        for (char c : PAD[d.charAt(i) - '0'].toCharArray()) {
            sb.append(c);
            letters(d, i + 1, sb, res);
            sb.deleteCharAt(sb.length() - 1);       // UNDO
        }
    }

    /* ======================================================================
     * 7. PALINDROME PARTITIONING  (LC 131)
     * ====================================================================== */
    static List<List<String>> partition(String s) {
        List<List<String>> res = new ArrayList<>();
        part(s, 0, new ArrayList<>(), res);
        return res;
    }
    private static void part(String s, int start, List<String> path, List<List<String>> res) {
        if (start == s.length()) { res.add(new ArrayList<>(path)); return; }
        for (int end = start + 1; end <= s.length(); end++) {
            if (!isPal(s, start, end - 1)) continue;      // PRUNE non-palindromic prefixes
            path.add(s.substring(start, end));
            part(s, end, path, res);
            path.remove(path.size() - 1);
        }
    }
    private static boolean isPal(String s, int l, int r) {
        while (l < r) if (s.charAt(l++) != s.charAt(r--)) return false;
        return true;
    }

    /* ======================================================================
     * 8. SUBSETS VIA BITMASK — no recursion. Great to mention as an alternative.
     * For n elements there are 2^n masks; bit i set means "include element i".
     *   mask = 5 = 101b -> include elements 0 and 2
     * ====================================================================== */
    static List<List<Integer>> subsetsBitmask(int[] nums) {
        int n = nums.length;
        List<List<Integer>> res = new ArrayList<>(1 << n);
        for (int mask = 0; mask < (1 << n); mask++) {
            List<Integer> sub = new ArrayList<>();
            for (int i = 0; i < n; i++)
                if ((mask & (1 << i)) != 0) sub.add(nums[i]);
            res.add(sub);
        }
        return res;
    }

    /* ======================================================================
     * 9. BIT MANIPULATION CHEAT SHEET — shows up constantly
     * ======================================================================
     *   x & 1                is x odd?
     *   x >> 1               divide by 2
     *   x << 1               multiply by 2
     *   x & (1 << i)         test bit i
     *   x | (1 << i)         set bit i
     *   x & ~(1 << i)        clear bit i
     *   x ^ (1 << i)         toggle bit i
     *   x & (x - 1)          clear the LOWEST set bit   <- Brian Kernighan
     *   x & (-x)             isolate the lowest set bit
     *   x ^ x == 0           XOR with self cancels      <- LC 136 single number
     *   a ^ b ^ b == a       XOR is its own inverse
     *   (x & (x-1)) == 0     is x a power of two? (for x > 0)
     *   >>>                  UNSIGNED right shift - use for hashing/bit counting
     *
     *   Integer.bitCount(x)       popcount
     *   Integer.toBinaryString(x)
     *   Integer.reverse(x) / highestOneBit / numberOfTrailingZeros
     *   Integer.MAX_VALUE = 2^31 - 1, Integer.MIN_VALUE = -2^31
     */

    /* LC 136 — single number. Everything pairs up and cancels under XOR. */
    static int singleNumber(int[] nums) {
        int x = 0;
        for (int n : nums) x ^= n;
        return x;
    }

    /* LC 191 — count set bits, Brian Kernighan: loops once per SET bit, not 32 times. */
    static int hammingWeight(int n) {
        int count = 0;
        while (n != 0) { n &= (n - 1); count++; }
        return count;
    }

    /* LC 338 — counting bits for 0..n. dp[i] = dp[i >> 1] + (i & 1). */
    static int[] countBits(int n) {
        int[] dp = new int[n + 1];
        for (int i = 1; i <= n; i++) dp[i] = dp[i >> 1] + (i & 1);
        return dp;
    }

    /* LC 268 — missing number via XOR. Index XOR value cancels everything but the gap. */
    static int missingNumber(int[] nums) {
        int x = nums.length;
        for (int i = 0; i < nums.length; i++) x ^= i ^ nums[i];
        return x;
    }

    /* LC 371 — sum without +. carry = (a&b)<<1, sum = a^b, repeat. */
    static int getSum(int a, int b) {
        while (b != 0) { int carry = (a & b) << 1; a = a ^ b; b = carry; }
        return a;
    }

    /* ======================================================================
     * 10. INTERVALS — sort first, then sweep. Always.
     * ====================================================================== */

    /* LC 56 — merge intervals. Sort by START, then extend or append. */
    static int[][] mergeIntervals(int[][] intervals) {
        if (intervals.length == 0) return intervals;
        Arrays.sort(intervals, Comparator.comparingInt(a -> a[0]));
        List<int[]> out = new ArrayList<>();
        int[] cur = intervals[0].clone();
        for (int i = 1; i < intervals.length; i++) {
            if (intervals[i][0] <= cur[1]) cur[1] = Math.max(cur[1], intervals[i][1]); // overlap
            else { out.add(cur); cur = intervals[i].clone(); }
        }
        out.add(cur);
        return out.toArray(new int[0][]);
    }

    /* LC 435 — minimum removals to make intervals non-overlapping.
     * GREEDY: sort by END. Always keep the interval that finishes earliest -
     * it leaves the most room for everything after it. */
    static int eraseOverlapIntervals(int[][] intervals) {
        if (intervals.length == 0) return 0;
        Arrays.sort(intervals, Comparator.comparingInt(a -> a[1]));   // by END
        int end = intervals[0][1], kept = 1;
        for (int i = 1; i < intervals.length; i++) {
            if (intervals[i][0] >= end) { kept++; end = intervals[i][1]; }
        }
        return intervals.length - kept;
    }

    /* LC 253 — meeting rooms II. Min heap of END times = number of rooms in use. */
    static int minMeetingRooms(int[][] intervals) {
        if (intervals.length == 0) return 0;
        Arrays.sort(intervals, Comparator.comparingInt(a -> a[0]));
        PriorityQueue<Integer> endTimes = new PriorityQueue<>();
        for (int[] m : intervals) {
            if (!endTimes.isEmpty() && endTimes.peek() <= m[0]) endTimes.poll();  // room freed
            endTimes.offer(m[1]);
        }
        return endTimes.size();
    }

    /* ======================================================================
     * 11. GREEDY — jump game. Track the furthest reachable index.
     * Greedy works when a local optimum is provably globally optimal.
     * ====================================================================== */
    static boolean canJump(int[] nums) {
        int reach = 0;
        for (int i = 0; i < nums.length; i++) {
            if (i > reach) return false;                // this index is unreachable
            reach = Math.max(reach, i + nums[i]);
        }
        return true;
    }

    /* LC 45 — jump game II, minimum jumps. BFS-by-levels over the array. */
    static int jump(int[] nums) {
        int jumps = 0, curEnd = 0, farthest = 0;
        for (int i = 0; i < nums.length - 1; i++) {
            farthest = Math.max(farthest, i + nums[i]);
            if (i == curEnd) { jumps++; curEnd = farthest; }   // end of this "level"
        }
        return jumps;
    }

    /* ================================================================== */
    public static void main(String[] args) {
        System.out.println("--- subsets ---");
        System.out.println("subsets([1,2,3])        = " + subsets(new int[]{1,2,3}));
        System.out.println("subsetsBitmask([1,2,3]) = " + subsetsBitmask(new int[]{1,2,3}));
        System.out.println("subsetsWithDup([1,2,2]) = " + subsetsWithDup(new int[]{1,2,2}));

        System.out.println("\n--- permutations ---");
        System.out.println(permute(new int[]{1,2,3}));

        System.out.println("\n--- combination sum ---");
        System.out.println("combinationSum([2,3,6,7], 7)   = " +
                combinationSum(new int[]{2,3,6,7}, 7));
        System.out.println("combinationSum2([10,1,2,7,6,1,5], 8) = " +
                combinationSum2(new int[]{10,1,2,7,6,1,5}, 8));

        System.out.println("\n--- word search ---");
        char[][] board = {{'A','B','C','E'},{'S','F','C','S'},{'A','D','E','E'}};
        System.out.println("exist('ABCCED') = " + exist(board, "ABCCED"));
        System.out.println("exist('ABCB')   = " + exist(board, "ABCB"));

        System.out.println("\n--- N-Queens (n=4) ---");
        for (List<String> sol : solveNQueens(4)) {
            sol.forEach(row -> System.out.println("  " + row));
            System.out.println();
        }
        System.out.println("solutions for n=8: " + solveNQueens(8).size());

        System.out.println("--- misc backtracking ---");
        System.out.println("letterCombinations('23') = " + letterCombinations("23"));
        System.out.println("partition('aab')         = " + partition("aab"));

        System.out.println("\n--- bit manipulation ---");
        System.out.println("singleNumber([4,1,2,1,2]) = " + singleNumber(new int[]{4,1,2,1,2}));
        System.out.println("hammingWeight(11)         = " + hammingWeight(11)
                + "  (" + Integer.toBinaryString(11) + ")");
        System.out.println("countBits(5)              = " + Arrays.toString(countBits(5)));
        System.out.println("missingNumber([3,0,1])    = " + missingNumber(new int[]{3,0,1}));
        System.out.println("getSum(7, 5)              = " + getSum(7, 5));
        System.out.println("11 & (11-1) = " + (11 & 10) + "  (cleared the lowest set bit)");

        System.out.println("\n--- intervals ---");
        System.out.println("mergeIntervals = " + Arrays.deepToString(
                mergeIntervals(new int[][]{{1,3},{2,6},{8,10},{15,18}})));
        System.out.println("eraseOverlap   = " +
                eraseOverlapIntervals(new int[][]{{1,2},{2,3},{3,4},{1,3}}));
        System.out.println("minMeetingRooms= " +
                minMeetingRooms(new int[][]{{0,30},{5,10},{15,20}}));

        System.out.println("\n--- greedy ---");
        System.out.println("canJump([2,3,1,1,4]) = " + canJump(new int[]{2,3,1,1,4}));
        System.out.println("canJump([3,2,1,0,4]) = " + canJump(new int[]{3,2,1,0,4}));
        System.out.println("jump([2,3,1,1,4])    = " + jump(new int[]{2,3,1,1,4}));
    }
}

/* ==========================================================================
 * PRACTICE — BLIND 75 / NEETCODE
 * ==========================================================================
 *  BACKTRACKING
 *   LC 78   Subsets                               med    the base template
 *   LC 90   Subsets II                            med    sort + skip siblings
 *   LC 46   Permutations                          med    used[] flag
 *   LC 47   Permutations II                       med    sort + skip
 *   LC 39   Combination Sum                       med    reuse: recurse with i
 *   LC 40   Combination Sum II                    med    no reuse: i+1
 *   LC 17   Letter Combinations of a Phone Number med
 *   LC 79   Word Search                           med    mark and restore
 *   LC 131  Palindrome Partitioning               med
 *   LC 51   N-Queens                              hard   3 conflict sets
 *   LC 37   Sudoku Solver                         hard   constraint propagation
 *   LC 22   Generate Parentheses                  med    open/close counters
 *
 *  BIT MANIPULATION
 *   LC 136  Single Number                         easy   XOR
 *   LC 191  Number of 1 Bits                      easy   x & (x-1)
 *   LC 338  Counting Bits                         easy   DP on bits
 *   LC 268  Missing Number                        easy   XOR or Gauss sum
 *   LC 190  Reverse Bits                          easy
 *   LC 371  Sum of Two Integers                   med    carry loop
 *
 *  INTERVALS / GREEDY
 *   LC 56   Merge Intervals                       med    sort by START
 *   LC 57   Insert Interval                       med    no sorting needed
 *   LC 435  Non-overlapping Intervals             med    sort by END (greedy)
 *   LC 252  Meeting Rooms                         easy
 *   LC 253  Meeting Rooms II                      med    min heap of end times
 *   LC 55   Jump Game                             med    furthest reach
 *   LC 45   Jump Game II                          med    BFS levels
 *   LC 763  Partition Labels                      med    last-index map
 *
 * SELF-TEST QUESTIONS
 *   - Write the 4-line backtracking skeleton from memory.
 *   - Why `new ArrayList<>(path)` and not `path` when recording a result?
 *   - Combination sum I recurses with `i`, II with `i+1`. Why?
 *   - The duplicate-skip guard is `i > start`, not `i > 0`. Why does that matter?
 *   - N-Queens: what are r-c and r+c, and why offset the diagonal index by n?
 *   - Word search: why restore the cell, and why doesn't flood fill need to?
 *   - Merge intervals sorts by start; erase-overlap sorts by end. Why the difference?
 *   - What does x & (x-1) do, and what's it used for?
 * ========================================================================== */
