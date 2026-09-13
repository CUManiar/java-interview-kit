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
- Why mark `visited` when you ENQUEUE and not when you DEQUEUE?
- Multi-source BFS: what changes vs. single source?
- Why does the two-stack queue only dump when `out` is empty?
- Deque sliding-window-max: why is popping the back safe?
