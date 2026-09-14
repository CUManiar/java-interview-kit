# 4. Queue (FIFO) + Deque + Circular Queue

Stage 3/7 — Linear Structures

Code: [`QueueNotes.java`](./QueueNotes.java) — run `java 03-linear-structures/02-queue/QueueNotes.java`

## Prerequisites
- [Generics](../../01-foundations/01-generics/) — same node-and-type-param shape as Stack.
- [Stack](../../03-linear-structures/01-stack/) — read together; FIFO is easiest to understand as "the opposite of what you just learned."

## Mental model
```
enqueue at REAR, dequeue at FRONT.

     dequeue                              enqueue
       <--  [1] -> [2] -> [3] -> null  <--
           front              rear

enqueue(4):   rear.next = new; rear = new;
dequeue():    val = front.data; front = front.next;
              if (front == null) rear = null;   // <-- THE bug everyone hits
```
Keeping a `rear` pointer is why enqueue is O(1) instead of O(n) (walking to the tail).

**Circular queue** (array-backed, avoids shifting elements):
```
cap = 5
index:  0    1    2    3    4
      [ _ ][ B ][ C ][ D ][ _ ]
             ^front      ^rear
rear = (rear + 1) % cap      <- the wrap-around one-liner
```

## Complexity
enqueue/dequeue/peek O(1) · space O(n)

## When the interviewer expects a queue
- BFS on graphs/trees/grids (level-order, shortest path in unweighted graphs)
- sliding window **maximum** → monotonic deque
- rate limiting, task scheduling, producer-consumer
- "process in arrival order"

## What's in the code
Linked-list backed queue, circular array queue (LC 622), Queue-from-two-stacks (LC 232, amortized O(1)), the Java API you actually type, then the two template patterns: BFS level-order (the single most reused queue template) and BFS-on-a-grid (LC 994/200/1091), plus a monotonic deque for sliding window maximum (LC 239).

## Connects to
- [Stack](../../03-linear-structures/01-stack/) — FIFO vs LIFO; the monotonic-deque pattern here is the double-ended sibling of that file's monotonic stack.
- [Tree](../../04-trees-heaps-tries/01-tree/) — level-order traversal *is* this file's BFS template pointed at a tree instead of a grid.
- [Graph Basics](../../05-graphs/01-graph-basics/) — BFS shortest-path-in-unweighted-graph is this exact template with a `visited` set added.
- [Heap](../../04-trees-heaps-tries/02-heap/) — `PriorityQueue` is not FIFO despite the name; see that file for why (this file calls out the confusion directly).
- [Graph Algorithms](../../05-graphs/02-graph-algorithms/) — 0-1 BFS reuses this file's deque instead of a heap, because edge weights are only 0 or 1.

## Self-test
- In `dequeue()`, why must you null out `rear` when the queue empties?
  > `front` and `rear` are independent pointers into the same chain. When the last element is dequeued, `front` becomes `null`, but `rear` still references that now-removed node unless you reset it. If you don't, the next `enqueue` takes the `rear != null` branch and does `rear.next = n; rear = n;` on a node that `front` no longer points to — the new node is linked onto an orphaned tail, `front` stays `null`, and the queue looks empty forever even though something was enqueued.
- Why mark `visited` when you ENQUEUE and not when you DEQUEUE?
  > A cell can be discovered as a neighbor by multiple other cells before any of those copies reaches the front of the queue. If you wait until dequeue to mark it seen, the same cell gets enqueued once per neighbor that finds it, wasting work and breaking the "each cell visited once" guarantee BFS needs for correctness. Marking `seen[nr][nc] = true` at the moment of `q.offer(...)` (as `shortestPathBinaryGrid` does) guarantees a cell enters the queue at most once.
- Multi-source BFS: what changes vs. single source?
  > Instead of seeding the queue with one start node, you enqueue and mark every source as visited before the BFS loop starts, treating them all as level 0 simultaneously. The level-by-level expansion logic is identical afterward; distances now measure "steps from the nearest source" rather than from a single origin.
- Why does the two-stack queue only dump when `out` is empty?
  > `shift()` reverses order — `in`'s LIFO pop order becomes `out`'s FIFO pop order — which is only correct the first time elements move. If you dumped `in` into `out` while `out` still held older elements, the newer elements would end up on top of (i.e., popped before) elements that were enqueued earlier, breaking FIFO order. Guarding with `if (out.isEmpty())` ensures a dump only happens after `out`'s existing elements are fully drained.
- Deque sliding-window-max: why is popping the back safe?
  > When `nums[i]` arrives, any smaller value sitting at the back of the deque can never be the answer for any window that also contains `nums[i]`, because `nums[i]` is both newer (it will stay in-window at least as long) and larger. So popping those dominated values (`while (!dq.isEmpty() && nums[dq.peekLast()] <= nums[i]) dq.pollLast();`) discards no value that could ever win, while keeping the deque's remaining values in decreasing order with the max always at the front.
