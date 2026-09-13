# Java DS & Algos — interview prep

A staged learning path, not a pile of files. Start with **[ROADMAP.md](ROADMAP.md)** —
it explains how the 15 topics below depend on and reuse each other, and how to actually
study from this repo instead of memorizing 15 unrelated files.

Five root docs, five different jobs:

| File | For | When to open it |
|---|---|---|
| [ROADMAP.md](ROADMAP.md) | Understanding | First. How the 15 topics connect, and how to study them. |
| `01-` … `07-` folders | Learning one topic deeply | While working through a topic — README first, then the code. |
| [templates.md](templates.md) | Recall | Interview day. Copy-adapt algorithm skeletons, no explanation. |
| [java-api-examples.md](java-api-examples.md) | Recall | Interview day. Which collection/stream/lambda syntax to reach for, plus JS→Java muscle-memory traps. |
| [staff-level-prep.md](staff-level-prep.md) | A different round entirely | System design script + glossary, and a behavioral/leadership story-bank template. This repo is otherwise 100% coding-round — Staff loops usually weight design and leadership signal higher than that. |

The last three assume you already did the understanding part — they're compressed on
purpose and won't make much sense cold.

Every `.java` file is standalone and runnable: no build tool, no packages, no
dependencies.

```bash
# run any file directly (Java 11+ single-file source launcher)
java 02-hashing/01-hashmap/HashMapNotes.java
java 07-advanced-patterns/02-dynamic-programming/DynamicProgramming.java

# or compile the lot
javac -d /tmp/dsa $(find . -name "*.java") && java -cp /tmp/dsa TreeNotes
```

## Layout

```text
01-foundations/            generics — everything else assumes this
02-hashing/                the highest-ROI structure, taught early on purpose
03-linear-structures/      stack, queue, linked list
04-trees-heaps-tries/      tree, heap, trie
05-graphs/                 representations first, then the algorithms on top
06-core-techniques/        sorting, searching, two pointers / sliding window
07-advanced-patterns/      backtracking, then dynamic programming
```

Each topic is a folder with two files:

- **`README.md`** — mental model, ASCII diagrams, complexity, when to reach for it,
  **Prerequisites** (what to know first) and **Connects to** (where the same idea
  resurfaces later), and the self-test questions. Read this first, without the code.
- **`<Topic>.java`** — the from-scratch implementation, the Java API cheat sheet, the
  interview patterns as working methods, and a `main()` that demonstrates all of it.

Folders are numbered so they sort into study order in Finder/VS Code/`ls` exactly as
written — `01-` before `02-`, and the same one level down inside each stage.

## Study loop (short version — full version in ROADMAP.md)

Read the topic's `README.md` → try to redraw the diagram / recall the complexity from
memory → open the `.java` file and run it → close it and answer the self-test questions
out loud. If you can't answer one, go back before moving on. The **Connects to** section
in each README is there so the next topic feels like a continuation, not a cold start.
