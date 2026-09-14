# 8. Trie (prefix tree)

Stage 4/7 — Trees, Heaps & Tries

Code: [`TrieNotes.java`](./TrieNotes.java) — run `java 04-trees-heaps-tries/03-trie/TrieNotes.java`

## Prerequisites
- [Tree](../01-tree/) — same recursive node/children shape.
- [HashMap](../../02-hashing/01-hashmap/) — you need it as the point of comparison below, and as one of the two node-layout options.

## Mental model
A tree where each **edge** is a character and each path from the root spells a prefix. Words sharing a prefix share the path — the character lives in the edge/index, not the node.

Inserting `cat, car, card, dog`:
```
              (root)
              /    \
            c        d
           /          \
          a            o
         / \            \
        t*  r*           g*
             \
              d*
```
`*` = `isEnd` (a complete word ends here). "car" and "card" share 3 nodes; "ca" exists as a prefix but is **not** a word — that's exactly why `isEnd` is mandatory: without it you can't tell "is 'ca' a stored word?" from "is 'ca' a prefix of something?". `search()` checks `isEnd`; `startsWith()` doesn't.

**Node layout — two options**: `TrieNode[26]` array (O(1) lookup, ~208 bytes/node even when mostly null, use for lowercase a-z — the interview default), or `Map<Character, TrieNode>` (memory proportional to actual branches, use for unicode/large alphabets).

## Complexity
(L = word length, N = number of words) insert/search/startsWith/delete: all O(L) — independent of N. That's the whole point: a `HashMap<String,?>` also gives O(L) lookup, but it cannot answer prefix queries.

**Trie vs HashMap**:

| | Trie | HashMap |
|---|---|---|
| exact lookup | O(L) | O(L) |
| prefix "starts with" | O(L) | O(N·L) |
| list all words with prefix | O(result) | O(N·L) |
| lexicographic iteration | free (DFS) | needs sort |
| memory | heavier | lighter |

## When the interviewer expects a trie
- autocomplete / typeahead / search suggestions
- spell check, "words within edit distance"
- wildcard matching against a dictionary (`.` matches any char)
- "find all dictionary words in this grid" → trie + DFS
- IP routing tables, T9 predictive text
- XOR maximization on integers → **binary trie** (bit-level, 2 children)

## What's in the code
Array-children implementation (LC 208), HashMap-children implementation, Wildcard Search (LC 211), Word Search II — trie + DFS on a grid, the hard trie problem (LC 212), and a Binary Trie for maximum XOR of two numbers (LC 421).

## Connects to
- [HashMap](../../02-hashing/01-hashmap/) — the trade-off table above is the clearest illustration in the whole repo of *why data structure choice matters* even when both give O(L) lookup.
- [Tree](../01-tree/) — a trie's DFS is a tree DFS where the "value" is the accumulated path string.
- [Backtracking](../../07-advanced-patterns/01-backtracking/) — Word Search II is this file's DFS pruning combined with backtracking's choose/explore/undo loop.

## Self-test
- Why can't a `HashMap<String,Boolean>` efficiently answer "give me all words starting with 'car'," while a trie can?
  > A HashMap only supports exact-key lookup — there's no structural relationship between "car" and "card" as keys, so answering a prefix query means scanning every stored key and checking `startsWith`, i.e. O(N·L). A trie stores keys along shared paths (`walk()`), so the same query is: reach the node for "car" in O(L), then DFS everything beneath it — cost proportional to the result size, not to N.
- What exactly does `isEnd` buy you?
  > It distinguishes "a word actually ends here" from "this path merely exists because some longer word passes through here." Without it, "ca" being a valid path (because "cat"/"car" exist) would be indistinguishable from "ca" itself being an inserted word. `search()` checks `isEnd`; `startsWith()`/`walk()` doesn't need to, since a live path is enough for a prefix match.
- When would you use HashMap children instead of a 26-slot array?
  > When the character set is large or unbounded — Unicode, mixed case, arbitrary symbols — where a fixed-size array either can't cover the input or wastes memory on branches that never occur. `MapTrie` shows this: `Map<Character, Node>` (via `computeIfAbsent`) allocates only for branches that actually exist, trading the array's O(1) index for hashing overhead per character.
- In `delete()`, when is it safe to prune a node?
  > Only when it is not the end of another word (`!cur.isEnd`) *and* has no remaining children (`isLeaf(cur)`) — i.e., it's no longer needed by any stored word. `delete()` computes this bottom-up: each recursive call returns whether its own node now qualifies, and the parent unlinks it (`cur.children[i] = null`) only when true, so pruning cascades upward exactly as far as necessary and no further.
- In Word Search II, why null out `node.word` after adding it to the result?
  > The grid DFS can reach the same trie terminal node again through a different cell path; nulling `word` right after collecting it (`nxt.word = null`) stops that word from being added to the results twice, and lets the cheap `if (nxt.word != null)` check double as a "not yet collected" guard without a separate seen-set.
- In the grid DFS, why restore the board cell on the way out?
  > `'#'` temporarily marks the current path so the DFS can't reuse the same cell twice within one word attempt. But it must not leak into other branches — sibling directions explored after backtracking, or entirely different starting cells. Restoring the original character (`b[r][c] = ch`) after all four directions are explored undoes that marking, which is exactly the backtrack step.
- Binary trie: why does walking to the opposite bit maximize XOR?
  > XOR of two bits is 1 — and thus contributes to a larger result — only when the bits differ, and higher bit positions are worth more to the final integer value. Greedily choosing, from the most significant bit down, the child representing the *opposite* bit of the current number (when that branch exists in the trie, via `cur.kid[want]`) maximizes the count of leading 1-bits in the XOR, which maximizes the value; falling back to `cur.kid[bit]` only happens when no inserted number has the ideal bit at that position.
