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
  > Two objects that are `.equals()` can land in different buckets (rule 1 is broken). The map ends up with "duplicate" keys, `get`/`containsKey` silently fail to find an entry you already put in, and `HashSet` stops deduping. Classic symptom: `map.put(key1, v); map.get(key2)` returns `null` even though `key1.equals(key2)`.
- Why must HashMap capacity be a power of two?
  > So bucket index can be computed with `hash & (cap-1)` instead of `hash % cap`. When cap is a power of two, `cap-1` is all 1-bits, making `&` mathematically equal to `%` but much cheaper — and it guarantees every bit pattern of `cap-1` is reachable, so resizing (always ×2) cleanly rehashes.
- What does `h ^ (h >>> 16)` buy you?
  > It XORs the high 16 bits of the hash into the low 16 bits before masking. Since `& (cap-1)` only looks at low bits, hashCodes that differ only in high bits would otherwise all collide; spreading mixes that entropy down cheaply.
- At what chain length does Java treeify a bucket, and why?
  > At 8 nodes in one bucket (and only if the table itself has ≥ 64 buckets — otherwise it resizes instead). It converts the linked list to a red-black tree so worst-case lookup in that bucket goes from O(n) to O(log n), guarding against bad/adversarial `hashCode()` implementations. Untreeifies back to a list below 6 nodes.
- Difference between `HashMap`, `ConcurrentHashMap`, and `Hashtable`?
  > `HashMap` — not thread-safe, allows one `null` key and many `null` values, fastest for single-threaded use. `Hashtable` — legacy, thread-safe via one lock on the whole table (coarse-grained, so heavy contention under concurrency), no `null` keys/values allowed. `ConcurrentHashMap` — thread-safe via fine-grained locking/CAS (per-bin, not whole-table), much higher concurrent throughput than `Hashtable`, no `null` keys/values allowed either.
- Why is `containsKey` not the same as `get(k) != null`?
  > `get(k)` returns `null` in two different cases: the key isn't present, *or* the key is present and mapped to a stored `null` value. You can't tell those apart from the return value alone. `containsKey(k)` unambiguously answers "is this key present," regardless of what it maps to.
- Explain `merge()` vs `compute()` vs `computeIfAbsent()`.
  > `computeIfAbsent(k, fn)` — only runs `fn` and inserts if `k` is absent (or mapped to `null`); leaves an existing non-null value untouched. Classic use: `map.computeIfAbsent(k, x -> new ArrayList<>()).add(v)`.
  > `compute(k, fn)` — always runs `fn(k, currentValueOrNull)` regardless of presence; whatever it returns becomes the new value, and returning `null` removes the entry. Most general of the three.
  > `merge(k, v, fn)` — if `k` is absent, just stores `v`; if present, stores `fn(oldValue, v)`; returning `null` from `fn` removes the entry. Classic use: frequency counting, `map.merge(k, 1, Integer::sum)`.
