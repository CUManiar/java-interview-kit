# Templates — copy, adapt, ship

Skeleton code for every pattern in [ROADMAP.md](ROADMAP.md)'s triage table, in one place,
so interview day is "recognize the shape → paste → rename variables" instead of
reconstructing the loop from scratch under pressure.

This is a **recall aid, not a study doc**. Every template below is explained properly —
why it works, what it's for, the bugs people hit — in its topic's `README.md`, linked at
the end of each section. If a template doesn't immediately make sense, that's the signal
to go read the explanation once, not to memorize the code blindly. Once you've understood
it there, this page is what gets it back into your fingers fast.

The `/* ... */` blanks are exactly what changes from problem to problem — everything
else is boilerplate you should be able to type without thinking.

## Index

1. [Two pointers — opposite ends](#1-two-pointers--opposite-ends)
2. [Two pointers — same direction](#2-two-pointers--same-direction-in-place-compaction)
3. [Sliding window — variable size](#3-sliding-window--variable-size)
4. [Sliding window — fixed size](#4-sliding-window--fixed-size-k)
5. [Prefix sum + HashMap](#5-prefix-sum--hashmap-subarray-sum-equals-k)
6. [Binary search — exact match](#6-binary-search--exact-match)
7. [Binary search — first true / on the answer](#7-binary-search--first-true--on-the-answer)
8. [Monotonic stack — next greater/smaller](#8-monotonic-stack--next-greatersmaller)
9. [Monotonic deque — sliding window max](#9-monotonic-deque--sliding-window-maximum)
10. [Fast/slow pointer — cycle / middle](#10-fastslow-pointer--cycle-detection--middle-of-list)
11. [BFS — level order / shortest path](#11-bfs--level-order--shortest-path-unweighted)
12. [DFS — recursive graph/grid](#12-dfs--recursive-graphgrid)
13. [Backtracking skeleton](#13-backtracking-skeleton)
14. [Union-Find (DSU)](#14-union-find-dsu)
15. [Dijkstra](#15-dijkstra--shortest-path-non-negative-weights)
16. [Topological sort (Kahn's)](#16-topological-sort--kahns-algorithm)
17. [DP — top-down memoization](#17-dp--top-down-memoization)
18. [DP — bottom-up tabulation](#18-dp--bottom-up-tabulation)
19. [Merge intervals / sweep](#19-merge-intervals--sweep)

---

### 1. Two pointers — opposite ends
**Use when**: array is sorted (or symmetric) and you need a pair/triplet, e.g. "two numbers that sum to target," palindrome check, container/area.
```java
int lo = 0, hi = arr.length - 1;
while (lo < hi) {
    int sum = arr[lo] + arr[hi];
    if (sum == target) { /* found: use lo, hi */ break; }
    else if (sum < target) lo++;
    else hi--;
}
```
Full writeup: [06-core-techniques/03-two-pointers-sliding-window/](06-core-techniques/03-two-pointers-sliding-window/)

### 2. Two pointers — same direction (in-place compaction)
**Use when**: "remove in place," "move zeroes," "partition," dedupe a sorted array.
```java
int slow = 0;
for (int fast = 0; fast < arr.length; fast++) {
    if (/* arr[fast] should be kept */ true) {
        arr[slow++] = arr[fast];
    }
}
// arr[0 .. slow-1] is the compacted result; slow is the new length
```
Full writeup: [06-core-techniques/03-two-pointers-sliding-window/](06-core-techniques/03-two-pointers-sliding-window/)

### 3. Sliding window — variable size
**Use when**: "contiguous subarray/substring," "longest/shortest," "at most K," and the constraint is monotonic in window size (no negative numbers involved).
```java
int left = 0, best = 0;
Map<Character, Integer> count = new HashMap<>();
for (int right = 0; right < s.length(); right++) {
    count.merge(s.charAt(right), 1, Integer::sum);              // expand
    while (/* window invalid, e.g. count.size() > k */ false) {
        char c = s.charAt(left);
        count.merge(c, -1, Integer::sum);
        if (count.get(c) == 0) count.remove(c);
        left++;                                                  // shrink
    }
    best = Math.max(best, right - left + 1);
}
```
Full writeup: [06-core-techniques/03-two-pointers-sliding-window/](06-core-techniques/03-two-pointers-sliding-window/)

### 4. Sliding window — fixed size k
**Use when**: the window size is given directly ("subarray of length k").
```java
int sum = 0, best = Integer.MIN_VALUE;
for (int i = 0; i < arr.length; i++) {
    sum += arr[i];
    if (i >= k) sum -= arr[i - k];
    if (i >= k - 1) best = Math.max(best, sum);
}
```
Full writeup: [06-core-techniques/03-two-pointers-sliding-window/](06-core-techniques/03-two-pointers-sliding-window/)

### 5. Prefix sum + HashMap (subarray sum equals K)
**Use when**: subarray-sum problem where the array **can contain negatives** (sliding window's monotonic assumption breaks).
```java
Map<Integer, Integer> prefixCount = new HashMap<>();
prefixCount.put(0, 1);                 // empty prefix, seeds the "whole prefix matches" case
int sum = 0, count = 0;
for (int x : arr) {
    sum += x;
    count += prefixCount.getOrDefault(sum - k, 0);
    prefixCount.merge(sum, 1, Integer::sum);
}
```
Full writeup: [06-core-techniques/03-two-pointers-sliding-window/](06-core-techniques/03-two-pointers-sliding-window/)

### 6. Binary search — exact match
**Use when**: find a specific value (or its insertion point) in a sorted array.
```java
int lo = 0, hi = arr.length - 1;
while (lo <= hi) {
    int mid = lo + (hi - lo) / 2;         // overflow-safe
    if (arr[mid] == target) return mid;
    else if (arr[mid] < target) lo = mid + 1;
    else hi = mid - 1;
}
return -1; // not found; lo == insertion point
```
Full writeup: [06-core-techniques/02-searching/](06-core-techniques/02-searching/)

### 7. Binary search — first true / on the answer
**Use when**: "find the first index where X holds," or "find the minimum value that satisfies a feasibility check" (Koko bananas, ship packages).
```java
int lo = 0, hi = n; // hi is exclusive — one past the last valid candidate
while (lo < hi) {
    int mid = lo + (hi - lo) / 2;
    if (feasible(mid)) hi = mid;   // mid works — the answer is mid or smaller
    else lo = mid + 1;             // mid doesn't work — answer is bigger
}
return lo; // first value where feasible() is true
```
Full writeup: [06-core-techniques/02-searching/](06-core-techniques/02-searching/)

### 8. Monotonic stack — next greater/smaller
**Use when**: "next/previous greater or smaller element," largest rectangle, daily temperatures.
```java
Deque<Integer> stack = new ArrayDeque<>();   // holds INDICES, values kept decreasing
int[] result = new int[arr.length];          // default 0 = "no answer found"
for (int i = 0; i < arr.length; i++) {
    while (!stack.isEmpty() && arr[i] > arr[stack.peek()]) {
        int idx = stack.pop();
        result[idx] = i;                     // or i - idx, or arr[i] — depends on the question
    }
    stack.push(i);
}
```
Full writeup: [03-linear-structures/01-stack/](03-linear-structures/01-stack/)

### 9. Monotonic deque — sliding window maximum
**Use when**: max/min over every window of size k, in O(n).
```java
Deque<Integer> dq = new ArrayDeque<>();      // indices, values kept decreasing front-to-back
int[] result = new int[arr.length - k + 1];
for (int i = 0; i < arr.length; i++) {
    if (!dq.isEmpty() && dq.peekFirst() <= i - k) dq.pollFirst();      // evict out-of-window
    while (!dq.isEmpty() && arr[dq.peekLast()] <= arr[i]) dq.pollLast(); // evict smaller
    dq.offerLast(i);
    if (i >= k - 1) result[i - k + 1] = arr[dq.peekFirst()];
}
```
Full writeup: [03-linear-structures/02-queue/](03-linear-structures/02-queue/)

### 10. Fast/slow pointer — cycle detection / middle of list
**Use when**: linked list cycle, find the middle node, nth-from-end.
```java
ListNode slow = head, fast = head;
while (fast != null && fast.next != null) {
    slow = slow.next;
    fast = fast.next.next;
    if (slow == fast) { /* cycle found */ break; }
}
// if the loop exits via fast == null, slow is the middle (or just past it) with no cycle
```
Full writeup: [03-linear-structures/03-linked-list/](03-linear-structures/03-linked-list/)

### 11. BFS — level order / shortest path (unweighted)
**Use when**: shortest path with no weights, level-by-level processing, multi-source spread.
```java
Queue<Integer> queue = new ArrayDeque<>();
boolean[] visited = new boolean[n];
queue.add(start);
visited[start] = true;                 // mark visited on ENQUEUE, not on dequeue
int dist = 0;
while (!queue.isEmpty()) {
    int levelSize = queue.size();
    for (int i = 0; i < levelSize; i++) {
        int node = queue.poll();
        // process `node`, currently at distance `dist` from start
        for (int next : adj.get(node)) {
            if (!visited[next]) {
                visited[next] = true;
                queue.add(next);
            }
        }
    }
    dist++;
}
```
Full writeup: [03-linear-structures/02-queue/](03-linear-structures/02-queue/) and [05-graphs/01-graph-basics/](05-graphs/01-graph-basics/)

### 12. DFS — recursive graph/grid
**Use when**: connectivity, flood fill, "all paths," cycle detection.
```java
void dfs(int node, boolean[] visited, Map<Integer, List<Integer>> adj) {
    visited[node] = true;              // mark visited on ENTRY
    // process `node`
    for (int next : adj.get(node)) {
        if (!visited[next]) dfs(next, visited, adj);
    }
}
```
Full writeup: [05-graphs/01-graph-basics/](05-graphs/01-graph-basics/)

### 13. Backtracking skeleton
**Use when**: "all subsets/permutations/combinations/partitions," N-Queens, word search.
```java
void backtrack(List<Integer> path, /* remaining choices / state */ Object state) {
    if (/* goal reached, e.g. path.size() == n */ false) {
        result.add(new ArrayList<>(path));   // COPY — never store `path` by reference
        return;
    }
    for (int choice : /* candidates at this step */ new int[0]) {
        if (/* choice invalid, e.g. already used, or duplicate at this level */ false) continue;
        path.add(choice);                    // CHOOSE
        backtrack(path, state);              // EXPLORE
        path.remove(path.size() - 1);        // UNDO — forget this and everything breaks
    }
}
```
Full writeup: [07-advanced-patterns/01-backtracking/](07-advanced-patterns/01-backtracking/)

### 14. Union-Find (DSU)
**Use when**: "are these connected," cycle detection in an undirected graph, dynamic grouping/merging, Kruskal's MST.
```java
int[] parent, size;
void init(int n) {
    parent = new int[n];
    size = new int[n];
    for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }
}
int find(int x) {
    return parent[x] == x ? x : (parent[x] = find(parent[x]));   // path compression
}
void union(int a, int b) {
    int ra = find(a), rb = find(b);
    if (ra == rb) return;                    // already connected — this edge would form a cycle
    if (size[ra] < size[rb]) { int t = ra; ra = rb; rb = t; }    // union by size
    parent[rb] = ra;
    size[ra] += size[rb];
}
```
Full writeup: [05-graphs/01-graph-basics/](05-graphs/01-graph-basics/)

### 15. Dijkstra — shortest path, non-negative weights
**Use when**: weighted graph, all weights ≥ 0, single-source shortest path.
```java
int[] dist = new int[n];
Arrays.fill(dist, Integer.MAX_VALUE);
dist[src] = 0;
PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1])); // [node, dist]
pq.add(new int[]{src, 0});
while (!pq.isEmpty()) {
    int[] cur = pq.poll();
    int u = cur[0], d = cur[1];
    if (d > dist[u]) continue;               // stale entry — a better path was already found
    for (int[] edge : adj.get(u)) {          // edge = {neighbor, weight}
        int v = edge[0], w = edge[1];
        if (dist[u] + w < dist[v]) {
            dist[v] = dist[u] + w;
            pq.add(new int[]{v, dist[v]});
        }
    }
}
```
Full writeup: [05-graphs/02-graph-algorithms/](05-graphs/02-graph-algorithms/)

### 16. Topological sort — Kahn's algorithm
**Use when**: dependency ordering, prerequisites, "is this a valid DAG."
```java
int[] indegree = new int[n];
for (List<Integer> neighbors : adj) for (int v : neighbors) indegree[v]++;

Queue<Integer> queue = new ArrayDeque<>();
for (int i = 0; i < n; i++) if (indegree[i] == 0) queue.add(i);

List<Integer> order = new ArrayList<>();
while (!queue.isEmpty()) {
    int u = queue.poll();
    order.add(u);
    for (int v : adj.get(u)) {
        if (--indegree[v] == 0) queue.add(v);
    }
}
// order.size() < n  =>  a cycle exists, no valid ordering
```
Full writeup: [05-graphs/02-graph-algorithms/](05-graphs/02-graph-algorithms/)

### 17. DP — top-down memoization
**Use when**: the recursive definition is natural but recomputes the same state repeatedly.
```java
Map<String, Integer> memo = new HashMap<>();
int solve(int i, int j) {
    if (/* base case */ false) return /* base value */ 0;
    String key = i + "," + j;                 // or encode as a single long, or a 2D array
    if (memo.containsKey(key)) return memo.get(key);
    int result = /* combine solve(smaller i/j...) per the recurrence */ 0;
    memo.put(key, result);
    return result;
}
```
Full writeup: [07-advanced-patterns/02-dynamic-programming/](07-advanced-patterns/02-dynamic-programming/)

### 18. DP — bottom-up tabulation
**Use when**: you already have the recurrence and want no recursion-depth risk.
```java
int[][] dp = new int[n + 1][m + 1];
for (int i = 0; i <= n; i++) dp[i][0] = /* base case */ 0;
for (int j = 0; j <= m; j++) dp[0][j] = /* base case */ 0;
for (int i = 1; i <= n; i++) {
    for (int j = 1; j <= m; j++) {
        dp[i][j] = /* recurrence using dp[i-1][j], dp[i][j-1], dp[i-1][j-1]... */ 0;
    }
}
return dp[n][m];   // or wherever the recipe's "answer" step says to look
```
Full writeup: [07-advanced-patterns/02-dynamic-programming/](07-advanced-patterns/02-dynamic-programming/)

### 19. Merge intervals / sweep
**Use when**: overlapping ranges — merge them, count them, or remove the minimum to eliminate overlap.
```java
Arrays.sort(intervals, Comparator.comparingInt(a -> a[0]));   // sort by start
List<int[]> merged = new ArrayList<>();
for (int[] cur : intervals) {
    if (merged.isEmpty() || merged.get(merged.size() - 1)[1] < cur[0]) {
        merged.add(cur);
    } else {
        int[] last = merged.get(merged.size() - 1);
        last[1] = Math.max(last[1], cur[1]);
    }
}
```
Full writeup: [07-advanced-patterns/01-backtracking/](07-advanced-patterns/01-backtracking/) (Intervals section)

---
See also: [ROADMAP.md](ROADMAP.md) for how these patterns connect to each other, [java-api-examples.md](java-api-examples.md) for the collection/stream/lambda syntax to write them fast.
