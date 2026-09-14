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
  > The `while` loop runs while `curr != null`, so by the time it exits, `curr` has advanced past the end of the list and is `null` — returning it would return an empty list. `prev` holds the last real node processed, which is exactly the new head of the reversed list.
- In recursive reversal, what happens if you skip `head.next = null`?
  > `head.next` still points forward to its old successor (call it `node2`), and the line above it already did `head.next.next = head`, i.e. `node2.next = head`. Without clearing `head.next`, you now have `head -> node2` and `node2 -> head` at the same time — a 2-node cycle — instead of `node2 -> head -> null`.
- Prove why Floyd's reset-to-head finds the cycle entry.
  > Let `L` = distance from `head` to the cycle start, `C` = cycle length, `x` = distance from the cycle start to the meeting point. When slow and fast meet, slow has moved `L+x` steps and fast has moved `2(L+x)` steps, and fast's extra distance is some whole number of laps: `2(L+x) = L+x+nC`, so `L+x = nC`, i.e. `L = nC - x`. That means the meeting point is exactly `L` steps *before* completing another `n` laps back to the cycle start — equivalently, `nC - x` steps forward from the meeting point lands on the cycle start, and that distance equals `L`. So a pointer walking `L` steps from `head` and a pointer walking `L` steps forward from the meeting point both land on the cycle start at the same time, which is exactly what `detectCycleStart`'s second `while (p != slow)` loop does.
- When does a dummy head save you, specifically?
  > Whenever the result list's first node isn't known in advance, or the node being removed/changed might be the head itself. `mergeTwoLists` doesn't know upfront whether `a` or `b` will end up first, so it builds onto `dummy` and returns `dummy.next`; `removeNthFromEnd` might need to remove the actual head (when `n` equals the list length), and starting `slow`/`fast` at `dummy` makes that case identical to removing any other node — no separate `if (head is being removed)` branch needed.
- Why can a doubly linked list remove a node in O(1) but a singly one can't?
  > Given just a reference to the node, a doubly linked list can reach its predecessor directly via `n.prev` and relink around the node (`n.prev.next = n.next; n.next.prev = n.prev;`, as in `DoublyLinkedList.remove`). A singly linked list's nodes only know their `next`, not their predecessor, so removing a given node still requires walking from `head` to find whoever points at it — O(n) — even if you're already holding a reference to the node itself.
- When would you genuinely pick `java.util.LinkedList` over `ArrayList`?
  > Almost never for plain stack/queue/deque use — `ArrayDeque` is faster and uses less memory for that. `LinkedList`'s real edge is O(1) insertion/removal at an arbitrary position while iterating via a `ListIterator`, when you don't need random access (`get(i)` is O(n) since it walks the chain) — e.g., repeatedly splicing nodes in/out near a cursor rather than indexing into the structure.
