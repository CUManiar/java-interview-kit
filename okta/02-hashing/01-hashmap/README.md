# 2. HashMap / HashSet — highest ROI structure in the repo

Stage 2/7 — Hashing

Code: [`HashMapNotes.java`](./HashMapNotes.java) — run `java 02-hashing/01-hashmap/HashMapNotes.java`

## Prerequisites
- [Generics](../../01-foundations/01-generics/) — `HashMap<K,V>` and the `equals`/`hashCode` contract below both lean on type parameters working correctly.

## Why this early
Placed right after Generics, before any other data structure, because it powers roughly half the patterns in every topic that follows: O(1) lookups, frequency counting, memoized recursion, graph clone maps, prefix-sum counting. Learn it once here, recognize it everywhere else.

## Mental model
```
key ──> hashCode() ──> spread ──> & (cap-1) ──> bucket index

buckets (array of length 16, always a power of 2)
 0 -> null
 1 -> ["apple"=1] -> ["grape"=9]        <- COLLISION, resolved by chaining
 2 -> null
 3 -> ["mango"=5]
```
**Why `& (cap-1)` instead of `% cap`**: if cap is a power of two, `cap-1` is `000...111`, so AND == modulo but ~5x faster. This is exactly why `HashMap` always rounds capacity up to a power of 2.

**Why `h ^ (h >>> 16)`** (the "spread" step): masking with `(cap-1)` only keeps the low bits. If hashCodes differ only in high bits, everything collides. XOR-ing the high 16 bits into the low 16 mixes them in cheaply — the actual JDK implementation.

**Collision strategies**: chaining (each bucket holds a list/tree — what Java does), linear probing (`i = (i+1) % cap`, clusters, deletes need tombstones), double hashing (`i = (i + j*h2(k)) % cap`, spreads better), resizing (past `size > cap * 0.75`, double and rehash everything).

**Java 8+ treeification**: a bucket chain of ≥8 nodes (table ≥64) converts to a red-black tree — worst case goes from O(n) to O(log n), untreeifies below 6. The #1 "do you actually know HashMap" question.

## Complexity
get/put/remove: O(1) average, O(log n) worst (treeified), O(n) if `hashCode()` is garbage. Space O(n).

## The equals/hashCode contract — memorize verbatim
1. `a.equals(b)` ⟹ `a.hashCode() == b.hashCode()` (MUST)
2. `a.hashCode() == b.hashCode()` does **not** imply `equals` (collisions are legal)
3. `hashCode` must stay stable while the object is a key (never key on a mutable field)

Break rule 1 and your object silently vanishes from the map — a classic production bug.

## What's in the code
From-scratch implementation (separate chaining + resize), writing a correct custom key (`equals`/`hashCode`), the Java API idioms you need in muscle memory (`merge`, `computeIfAbsent`, `getOrDefault`), then Two Sum (LC 1), Group Anagrams (LC 49), Top K Frequent via bucket sort (LC 347), Longest Consecutive Sequence (LC 128), and an LRU Cache via `LinkedHashMap` (LC 146).

## Connects to
- [Trie](../../04-trees-heaps-tries/03-trie/) — a trie's entire reason to exist is answering prefix queries a HashMap structurally can't; that trade-off table lives there.
- [Graph Basics](../../05-graphs/01-graph-basics/) — `cloneGraph` maps old node → new node so cycles don't cause infinite recursion; adjacency lists themselves are usually `Map<Integer, List<Integer>>`.
- [Two Pointers & Sliding Window](../../06-core-techniques/03-two-pointers-sliding-window/) — when a sliding window's constraint isn't monotonic (negative numbers), you fall back to prefix-sum + HashMap instead.
- [Dynamic Programming](../../07-advanced-patterns/02-dynamic-programming/) — top-down memoization is recursion plus a HashMap cache keyed on the state.
- [Backtracking](../../07-advanced-patterns/01-backtracking/) LRU here foreshadows the "choose a structure that gives O(1) for the operation you do most" instinct you'll need throughout.

## Self-test
- What breaks if you override `equals` but not `hashCode`?
- Why must HashMap capacity be a power of two?
- What does `h ^ (h >>> 16)` buy you?
- At what chain length does Java treeify a bucket, and why?
- Difference between `HashMap`, `ConcurrentHashMap`, and `Hashtable`?
- Why is `containsKey` not the same as `get(k) != null`?
- Explain `merge()` vs `compute()` vs `computeIfAbsent()`.
