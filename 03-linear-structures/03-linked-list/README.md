# 5. Linked List (singly, doubly, circular)

Stage 3/7 — Linear Structures

Code: [`LinkedListNotes.java`](./LinkedListNotes.java) — run `java 03-linear-structures/03-linked-list/LinkedListNotes.java`

## Prerequisites
- [Generics](../../01-foundations/01-generics/) — the node type.
- [Stack](../../03-linear-structures/01-stack/) / [Queue](../../03-linear-structures/02-queue/) — you already built a linked node in each; this file is that idea studied on its own.

## Mental model
```
SINGLY
  head
   |
   v
  [1|·]──>[2|·]──>[3|null]

DOUBLY
  null<─[1|·]<─>[2|·]<─>[3|·]─>null
         ^                  ^
        head              tail
```

**Array vs Linked List — know this cold**:

| Operation | ArrayList | LinkedList |
|---|---|---|
| get(i) random access | O(1) | O(n) |
| insert/delete head | O(n) shift | O(1) |
| insert/delete tail | O(1) amort. | O(1) (doubly) |
| insert/delete middle | O(n) | O(1) if you hold the node |
| memory per element | low | +2 pointers |
| cache locality | excellent | terrible |

In real code `ArrayList` wins ~95% of the time. `LinkedList` matters in interviews and inside other structures (LRU cache, hash buckets, adjacency lists).

**The 3 techniques that solve ~all linked-list problems**:
1. **Dummy head** — removes the "what if the head changes" special case
2. **Two pointers** — slow/fast for midpoint, cycle detection, nth-from-end
3. **Prev/curr/next** — in-place reversal

**Pointer-reversal movie** (memorize the 4 lines, in order):
```
prev=null  curr=head
  null   [1]->[2]->[3]->null
   ^      ^
  prev   curr

temp = curr.next        // 1. save the rest before you destroy the link
curr.next = prev        // 2. flip the arrow
prev = curr             // 3. advance prev
curr = temp             // 4. advance curr
```
Ends with `curr==null`, `prev==new head`. **Return `prev`, not `curr`.**

## What's in the code
Generic singly linked list, doubly linked list (the engine inside an LRU cache), the Java API, then: Reverse a Linked List iterative + recursive (LC 206), find the Middle via two pointers (LC 876), Floyd's cycle detection (LC 141/142), dummy-head merge of two sorted lists (LC 21), Remove Nth From End (LC 19), Reorder List (LC 143, combines everything).

## Connects to
- [Stack](../../03-linear-structures/01-stack/) / [Queue](../../03-linear-structures/02-queue/) — `LinkedStack`/linked-list-backed queue are this exact node shape reused.
- [Two Pointers & Sliding Window](../../06-core-techniques/03-two-pointers-sliding-window/) — slow/fast here is the identical two-pointer idea applied to arrays there.
- [Tree](../../04-trees-heaps-tries/01-tree/) — a tree node is a linked-list node with two `next` pointers instead of one; recursion replaces the explicit `while` loop.

## Self-test
- Why does iterative reversal return `prev` and not `curr`?
- In recursive reversal, what happens if you skip `head.next = null`?
- Prove why Floyd's reset-to-head finds the cycle entry.
- When does a dummy head save you, specifically?
- Why can a doubly linked list remove a node in O(1) but a singly one can't?
- When would you genuinely pick `java.util.LinkedList` over `ArrayList`?
