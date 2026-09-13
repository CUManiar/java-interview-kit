# 1. Generics — read this first

Stage 1/7 — Foundations

Code: [`GenericsPrimer.java`](./GenericsPrimer.java) — run `java 01-foundations/01-generics/GenericsPrimer.java`

## Prerequisites
None. This is the floor everything else stands on.

## Why first
Every other topic in this repo declares a `<T>`, a bounded wildcard, or a `Comparator`. If erasure and PECS aren't automatic, every later file reads like syntax noise instead of a data structure.

## Mental model
Pre-Java 5: `List l = new ArrayList(); Integer i = (Integer) l.get(0);` — compiles fine, blows up at **runtime** with `ClassCastException`.
With generics: `List<String> l = ...; Integer i = l.get(0);` — the compiler stops you. Errors move from runtime to compile time. That's the entire point.

**Type erasure — the one thing that confuses everyone.** Generics are a compile-time fiction; the JVM erases them:
```
Source:              Box<String>          Box<Integer>
                          |                     |
                          +----- erasure -------+
                                   |
Bytecode:                       Box            (T -> Object)
```
Consequences you must be able to state out loud in an interview:
1. `new T()` — illegal, no type info at runtime
2. `new T[10]` — illegal, use `(T[]) new Object[10]` + `@SuppressWarnings`
3. `List<String>.class` — illegal, only `List.class` exists
4. `f(List<String>)` and `f(List<Integer>)` — same erasure, won't compile as overloads
5. `instanceof List<String>` — illegal, only `instanceof List<?>`
6. can't `catch (MyEx<String> e)`
7. static fields are **shared** across every parameterization

**Naming convention**: `T`=Type, `E`=Element (collections), `K`=Key, `V`=Value, `N`=Number, `R`=Return, `U`/`S`=extra type params.

**Bounded type parameters**: `<T extends Comparable<T>>` (must be comparable to itself), `<T extends Number>` (upper bound), `<T extends A & B>` (class first, then interfaces — no `implements` in a bound).

**Wildcards + PECS** (Producer Extends, Consumer Super). Core problem: generics are invariant — `List<String>` is NOT a `List<Object>`.
```
List<Object>            <-- NOT a supertype of List<String>
List<? extends Object>  <-- IS  a supertype of List<String>   (covariant)
List<? super String>    <-- IS  a supertype of List<Object>   (contravariant)
```
- `? extends T` → you can only **read** T out of it (it *produces* T)
- `? super T` → you can only **write** T into it (it *consumes* T); reads come back as `Object`
- rule of thumb: param only gives you data → `? extends T`; only takes your data → `? super T`; does both → plain `T`

Proof from the JDK: `Collections.copy(List<? super T> dest, List<? extends T> src)`.

## What's in the code
Generic class, generic class with two params (`Pair` = `Map.Entry`), bounded type parameter, PECS in practice, the generic-array workaround (needed later for Trie/Heap/HashMap), a recursive generic bound (`<T extends Comparable<T>>`, shows up in every tree/node interview), and `Comparable` vs `Comparator` including chained comparators.

## Connects to
- Every topic after this one. `Node<T>`, `LinkedStack<T>`, the heap's pluggable `Comparator`, the trie's generics-free-but-erasure-aware array workaround — all of it is this vocabulary applied.
- [Sorting](../../06-core-techniques/01-sorting/) and [Tree](../../04-trees-heaps-tries/01-tree/) — `Comparable` vs `Comparator`, and `thenComparing` chains, are generics applied to ordering.
- [HashMap](../../02-hashing/01-hashmap/) — the `equals`/`hashCode` contract you're about to read depends on generic keys behaving correctly.

## Self-test
- What does type erasure remove at runtime, and name three consequences.
- Why can't you write `new T[10]`? What's the workaround?
- State PECS in one sentence and give the JDK method signature that proves it.
- Why is `List<String>` not a subtype of `List<Object>`?
- When would you write `<T extends A & B>`?
- Why can static fields not be parameterized per generic instance?
