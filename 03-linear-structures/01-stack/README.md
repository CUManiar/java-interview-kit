# 3. Stack (LIFO)

Stage 3/7 — Linear Structures

Code: [`StackNotes.java`](./StackNotes.java) — run `java 03-linear-structures/01-stack/StackNotes.java`

## Prerequisites
- [Generics](../../01-foundations/01-generics/) — the from-scratch `LinkedStack<T>` / `ArrayStack<T>` use plain generic type params and the `(T[]) new Object[cap]` workaround from that file.

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
  > `java.util.Stack` extends `Vector`, so every method is `synchronized` — dead weight for the single-threaded case an interview always is — and it iterates bottom-to-top, which is backwards for a stack. `ArrayDeque` has no locking overhead, iterates top-to-bottom when used as a stack (as shown in the file's `main`), and is the JDK-recommended replacement.
- What is the amortized cost of push on an array-backed stack, and why?
  > O(1) amortized. Most pushes are a plain `data[size++] = val`, O(1). When the array is full, `push` pays for one O(n) copy (`Arrays.copyOf(data, size * 2)`) to double the capacity. Because capacity doubles each time, that O(n) cost is spread over the n pushes that filled the array since the last resize, so the average cost per push stays O(1).
- How do you recognize a monotonic stack problem from the prompt?
  > The prompt asks, for every element, "what's the nearest element to the left/right that is greater/smaller than this one" (or an equivalent framing like Daily Temperatures' "how many days until a warmer temperature"). Any per-element nearest-greater/nearest-smaller query over a sequence is the monotonic-stack signal.
- Why is a monotonic stack O(n) and not O(n²)?
  > The nested `while` inside the `for` loop looks like it could be O(n²), but each index is pushed onto the stack exactly once and popped at most once over the whole run (see `dailyTemperatures`: `st.push(i)` happens once per `i`, and `st.pop()` only removes indices already on the stack). Total push+pop operations are bounded by 2n, so the amortized cost per element is O(1) and the algorithm is O(n) overall.
