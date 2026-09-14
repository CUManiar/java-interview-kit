# 8. Plain Implementations — no generics, just the mechanics

A separate, fast-cram track. Does not replace or modify `01-`…`07-` — it's a parallel
set of the same data structures with the generics stripped out, so you can focus
entirely on *how the structure works* instead of also parsing `<K, V>` syntax.

Every file here uses plain concrete types (`String`, `int`, `Object`) instead of
type parameters. Once the mechanics click, the generic versions in the numbered
folders are the same code with type safety bolted on — nothing new to learn there
except the generics syntax itself (`01-foundations/01-generics/`).

## Layout

```text
01-hashmap/                 chaining (2 ways) + open addressing, side by side
02-stack/                   array-backed vs linked-list-backed
03-queue/                   naive array (shows why it breaks) -> circular -> linked list
04-tree/                    BST insert/search/delete + all four traversals
05-heap/                    array-backed binary heap, min and max, sift up/down, heapify
06-trie/                    prefix tree, insert/search/startsWith/delete
07-graph/                   adjacency list vs matrix, BFS, DFS, union-find (cycle detection)
08-java-collections-api/    java.util cheat sheet: every collection type + every
                            interview-relevant method, Java 8 through 21, runnable
```

## How to use this

1. Read `01-hashmap/HashMapPlain.java` top to bottom, run it, watch the collisions happen.
2. Work through `02-stack` → `07-graph` in order — each is standalone, but the numbering
   mirrors the same study order as the generic `01-`…`07-` folders.
3. Once the from-scratch mechanics are automatic, `08-java-collections-api` is your
   reference for what the JDK gives you for free — so you stop hand-rolling a stack
   with `ArrayList` when `ArrayDeque` already does it, and know which `java.util`
   type to reach for under interview time pressure.

Every `.java` file is standalone and runnable, same as the rest of the repo:

```bash
java 08-plain-implementations/01-hashmap/HashMapPlain.java
```
