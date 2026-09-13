# 3. Stack (LIFO)

Stage 3/7 — Linear Structures

Code: [`StackNotes.java`](./StackNotes.java) — run `java 03-linear-structures/01-stack/StackNotes.java`

## Prerequisites
- [Generics](../../01-foundations/01-generics/) — the from-scratch `LinkedStack<T>` / `ArrayStack<T>` use bounded type params and the `(T[]) new Object[cap]` workaround from that file.

## Mental model
```
push(1) push(2) push(3)            pop() -> 3

   top -> [ 3 ]                        top -> [ 2 ]
          [ 2 ]                               [ 1 ]
          [ 1 ]                               -----
          -----
```
Linked-list view (what the file implements):
```
top
 |
 v
[3|next]--->[2|next]--->[1|null]

push: newNode.next = top; top = newNode;      <- O(1), prepend
pop : val = top.data;   top = top.next;       <- O(1), behead
```

## Complexity
push/pop/peek O(1) · search O(n) · space O(n)

## When the interviewer expects a stack
- matching/balancing pairs `()[]{}`
- "previous/next greater or smaller element" → **monotonic stack**
- undo history, backtracking, iterative DFS
- expression parsing/evaluation (RPN, infix)
- anything with nested structure

## Java API
Use `ArrayDeque`, never `java.util.Stack` — it extends `Vector`, every method is synchronized, and it iterates bottom-to-top (backwards). `push`=`addFirst` (O(1)), `pop`=`removeFirst` (throws if empty), `peek`=`peekFirst` (null if empty).

## What's in the code
Two from-scratch implementations (linked-list backed, array backed), Min Stack (`getMin()` in O(1)), then the patterns: Valid Parentheses (LC 20), monotonic-stack Next Greater Element (LC 739 / 496), Evaluate RPN (LC 150), Largest Rectangle in Histogram (LC 84).

## Connects to
- [Queue](../../03-linear-structures/02-queue/) — the monotonic *stack* here (next-greater-element) is the same shrinking-invariant idea as the monotonic *deque* there (sliding window max); one is LIFO, the other is double-ended.
- [Dynamic Programming](../../07-advanced-patterns/02-dynamic-programming/) — once you've solved Largest Rectangle with a monotonic stack, compare it to the DP framing — same problem, different lens.
- [Backtracking](../../07-advanced-patterns/01-backtracking/) — an explicit stack is how you turn any recursive DFS/backtrack into an iterative one.

## Self-test
- Why ArrayDeque over `java.util.Stack`?
- What is the amortized cost of push on an array-backed stack, and why?
- How do you recognize a monotonic stack problem from the prompt?
- Why is a monotonic stack O(n) and not O(n²)?
