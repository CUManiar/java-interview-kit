# Java API Examples — compressed syntax for interviews

[templates.md](templates.md) has the algorithm shapes. This file is the Java **syntax**
firepower to write them fast: which collection to reach for, how to filter/transform
without a manual loop, and the newer language features that turn 6 lines into 1.

Nothing here is a data structure lesson — that's [ROADMAP.md](ROADMAP.md) and the topic
folders. This is purely "what do I type."

## 0. Coming from JS? The traps that bite hardest

Not syntax you need to learn — instinct you need to unlearn. These cause silent wrong
answers or compile errors under pressure, not gentle warnings, so they're worth drilling
first.

```java
arr.length      // array: a FIELD, no parens
str.length()    // String: a METHOD
list.size()     // List/Map/Set: a METHOD called size(), not length()
```
Three different spellings for "how many." Mixing them up is the single most common typo a JS dev makes in Java — the compiler catches it, but it costs you seconds you don't have.

```java
int avg = (5 / 2);        // 2   -- int / int TRUNCATES, no error, just silently wrong
double avg2 = 5 / 2;      // 2.0 -- still truncated first, THEN widened. Still 2.0, not 2.5!
double avg3 = 5 / 2.0;    // 2.5 -- one operand must already be floating-point
```
This is the dangerous one: it never throws, it just gives you a wrong number. Any time you compute a midpoint, average, or ratio, ask "are both sides `int`?"

```java
String a = new String("cat");
String b = new String("cat");
a == b;         // false -- reference equality, always, for every object type
a.equals(b);    // true  -- content equality
```
`==` on anything that isn't a primitive (`int`, `char`, `boolean`, ...) compares references, full stop. This applies to `String`, `Integer`, your own classes, everything. JS's `===` at least compares primitive strings/numbers by value — Java's `==` never does that for objects. Always `.equals()` unless you specifically want reference identity.

```java
void increment(int x) { x++; }              // does NOTHING to the caller's variable
int n = 5; increment(n); // n is still 5

void mutate(int[] arr) { arr[0] = 99; }     // THIS works — same array, contents changed
int[] a = {1,2,3}; mutate(a); // a[0] is now 99
```
Java is pass-by-value, always — including for object references. You can't reassign a caller's variable through a parameter, but you CAN mutate the object/array a reference points to. There's no swap(a, b)-on-two-ints trick; return a new value (or wrap in a 1-element array) instead.

```java
int total = 0;
list.forEach(x -> total += x);    // COMPILE ERROR: variable used in lambda must be final/effectively final
```
Unlike a JS closure, a Java lambda can only *read* a captured local variable, never reassign it. Use `int[] total = {0};` and mutate `total[0]`, or better, use `.reduce(0, Integer::sum)` / `.sum()`.

```java
int[] arr = new int[5];   // fixed size FOREVER. No push, no resize.
```
Unlike a JS array, a Java array cannot grow. If you don't know the size up front, use `ArrayList<Integer>` instead — but note it can't hold primitive `int` directly (autoboxes to `Integer`), which matters for very tight performance problems.

## 1. JS array methods → Java equivalents

You already know the shape — `map`/`filter`/`reduce`/`forEach` chains are exactly how
you should still be thinking. Java has all of it; it's just spelled differently, and for
raw arrays (`int[]`) it's funneled through `Arrays.stream(...)` first instead of being a
method directly on `[]`. This table is the direct lookup by JS method name.

| JS | Java | Note |
|---|---|---|
| `arr.forEach(x => ...)` | `list.forEach(x -> ...)` or `for (var x : list)` | Raw arrays have no `.forEach` — use `Arrays.stream(arr).forEach(...)` or a for loop. |
| `arr.map(x => ...)` | `list.stream().map(x -> ...).collect(Collectors.toList())` | Non-mutating, returns a new list — same as JS. |
| `arr.filter(x => ...)` | `list.stream().filter(x -> ...).collect(Collectors.toList())` | Same non-mutating shape as JS. |
| `arr.reduce((acc,x) => ..., init)` | `list.stream().reduce(init, (acc, x) -> ...)` | For building a single value. Building a collection instead? See "reduce-into-a-map" below. |
| `arr.some(x => ...)` | `list.stream().anyMatch(x -> ...)` | |
| `arr.every(x => ...)` | `list.stream().allMatch(x -> ...)` | |
| `arr.find(x => ...)` | `list.stream().filter(x -> ...).findFirst()` | Returns `Optional<T>`, not the value or `undefined`. |
| `arr.findIndex(x => ...)` | `IntStream.range(0, list.size()).filter(i -> cond(list.get(i))).findFirst()` | No direct stream method — a manual for loop is often just as clear. |
| `arr.includes(x)` | `list.contains(x)` | Uses `.equals()`, not `==` — see section 0. |
| `arr.indexOf(x)` | `list.indexOf(x)` | |
| `[...arr].sort()` / `.sort((a,b)=>...)` | `list.sort(comparator)` / `Collections.sort(list)` / `Arrays.sort(arr)` | Mutates in place, like JS — but Java's default numeric sort is actually numeric. JS's bare `.sort()` compares as strings: `[10,1,2].sort()` → `[1,10,2]`. |
| `arr.slice(i, j)` | `Arrays.copyOfRange(arr, i, j)` / `new ArrayList<>(list.subList(i, j))` | `list.subList(i,j)` alone is a **live view**, not a copy — mutating it mutates the original. Wrap in `new ArrayList<>(...)` to actually copy. |
| `arr.splice(i, count, ...items)` | `list.subList(i, i+count).clear(); list.addAll(i, List.of(items));` | No single method — closest two-step equivalent. |
| `arr.concat(arr2)` | `var out = new ArrayList<>(list1); out.addAll(list2);` | |
| `arr.flat()` | `nested.stream().flatMap(List::stream).collect(Collectors.toList())` | |
| `arr.join(sep)` | `String.join(sep, listOfStrings)` | Non-`String` elements: `list.stream().map(String::valueOf).collect(Collectors.joining(sep))`. |
| `arr.push(x)` / `arr.pop()` | `list.add(x)` / `list.remove(list.size()-1)` | |
| `arr.shift()` / `arr.unshift(x)` | `list.remove(0)` / `list.add(0, x)` | O(n) on `ArrayList` — use `ArrayDeque` (`pollFirst`/`addFirst`) for O(1) both ends. |
| `arr.reverse()` | `Collections.reverse(list)` | |
| `[...arr]` (copy) | `new ArrayList<>(list)` | |
| `const [a, b] = arr` | `int a = arr[0], b = arr[1];` | No destructuring — index manually. |
| `Object.keys/values/entries(obj)` | `map.keySet()` / `map.values()` / `map.entrySet()` | |
| `arr.fill(x)` | `Arrays.fill(arr, x)` / `Collections.fill(list, x)` | |
| `Array.from({length:n}, (_,i) => f(i))` | `IntStream.range(0, n).mapToObj(i -> f(i)).collect(Collectors.toList())` | |

**Reduce-into-a-map** — the JS `arr.reduce((acc, x) => { acc[x.id] = x; return acc; }, {})` trick:
```java
Map<String, Person> byId = people.stream()
        .collect(Collectors.toMap(Person::id, p -> p));
```
Reach for `Collectors.toMap` / `Collectors.groupingBy` instead of hand-rolling a `reduce`
with a mutable accumulator — it's the idiomatic Java shape for "reduce into a collection."

**Streams are lazy AND single-use** — the biggest structural surprise coming from JS:
```java
Stream<Integer> s = list.stream().filter(x -> x > 0);
s.count();       // consumes the stream
s.forEach(...);  // IllegalStateException: stream has already been operated upon or closed
```
A JS array survives `.map()`/`.filter()` and can be reused forever. A Java `Stream` is a
one-shot pipe: build it, run exactly one terminal operation (`collect`, `reduce`,
`forEach`, `count`, `anyMatch`, ...), and it's spent. Need to run it twice? Call
`.stream()` on the source collection again — the *collection* is reusable, the *stream*
is not.

## 2. Which collection, when

### Set family
```java
Set<Integer> seen = new HashSet<>();            // O(1) add/contains, NO order guarantee
seen.add(5); seen.contains(5);                  // default choice for "have I seen this"

Set<Integer> ordered = new LinkedHashSet<>();   // HashSet + preserves insertion order
ordered.addAll(List.of(3, 1, 2, 1));            // iterates 3, 1, 2 — dedup, order kept

TreeSet<Integer> ts = new TreeSet<>(List.of(1, 5, 9, 12));  // O(log n), always sorted
ts.floor(10);     // 9  -> largest value <= 10
ts.ceiling(10);   // 12 -> smallest value >= 10
ts.higher(9);     // 12 -> strictly greater than 9
ts.lower(9);      // 5  -> strictly less than 9
ts.first(); ts.last();
ts.pollFirst(); ts.pollLast();                  // remove-and-return the min/max
```
**Interview signal**: "closest/next/previous element to X" → `TreeSet`, not sort-then-binary-search every query.

### Map family
```java
Map<String, Integer> freq = new HashMap<>();    // default for counting/lookup/grouping

final int capacity = 100;
Map<Integer, Integer> lru = new LinkedHashMap<>(16, 0.75f, true) {  // true = access-order
    protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
        return size() > capacity;               // evict the least-recently-used entry
    }
};                                               // this alone IS an LRU cache

TreeMap<Integer, String> tm = new TreeMap<>();
tm.floorKey(x); tm.ceilingKey(x); tm.higherKey(x); tm.lowerKey(x);
tm.firstEntry(); tm.lastEntry();
tm.headMap(x);        // view of all keys <  x
tm.tailMap(x);        // view of all keys >= x
tm.subMap(a, b);       // view of keys in [a, b)
```
**Interview signal**: calendar/interval problems, "the event starting just before/after time T," running order statistics → `TreeMap`.

### List / Deque family
- `ArrayList` — default. Random access, amortized O(1) append.
- `ArrayDeque` — use for **both** stack and queue. Never `java.util.Stack` (synchronized `Vector`, iterates backwards) or `LinkedList` (worse cache locality, more GC churn) for this. Full reasoning: [Stack](03-linear-structures/01-stack/) / [Queue](03-linear-structures/02-queue/).
- `List.of(...)` — fully immutable, throws `UnsupportedOperationException` on any mutation.
- `Arrays.asList(arr)` — fixed-**size** (no add/remove) but `set(i, v)` works — backed directly by the array. Classic gotcha: people expect it to behave like `List.of`.

## 3. Filtering — loop vs Streams
```java
// filter a List
List<Integer> evens = nums.stream().filter(n -> n % 2 == 0).collect(Collectors.toList());

// filter IN PLACE, no new collection
list.removeIf(n -> n % 2 != 0);

// filter an array
int[] evens = Arrays.stream(nums).filter(n -> n % 2 == 0).toArray();

// filter a Map by value
Map<String, Integer> big = map.entrySet().stream()
        .filter(e -> e.getValue() > 10)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

// filter a Set
Set<Integer> big2 = set.stream().filter(n -> n > 10).collect(Collectors.toSet());
```
**Gotcha**: never call `list.remove(x)` inside a `for (x : list)` — `ConcurrentModificationException`. Use `removeIf(...)`, or an explicit `Iterator` with `it.remove()`.

## 4. Optional
```java
Optional<String> opt = Optional.ofNullable(map.get(key));
opt.orElse("default");                    // EAGER — "default" is constructed even if opt is present
opt.orElseGet(() -> computeDefault());    // LAZY — supplier only runs if opt is empty
opt.orElseThrow(NoSuchElementException::new);
opt.map(String::toUpperCase).ifPresent(System.out::println);
opt.filter(s -> s.length() > 3).isPresent();
```
**Interview one-liner**: "`orElse` always evaluates its argument; `orElseGet` only runs the supplier when empty." Use `orElseGet` whenever the default is expensive to build.

## 5. Lambdas & method references
```java
Comparator<String> byLen = (a, b) -> a.length() - b.length();          // lambda
Comparator<String> byLen2 = Comparator.comparingInt(String::length);   // method reference, idiomatic

Function<String, Integer>        f    = String::length;
BiFunction<Integer, Integer, Integer> add  = Integer::sum;
Predicate<String>                empty = String::isEmpty;
Supplier<List<Integer>>          make = ArrayList::new;
Consumer<String>                 out  = System.out::println;
```
Four forms of method reference: `Class::staticMethod`, `instance::instanceMethod`, `Class::instanceMethod` (the first lambda argument becomes the receiver), `Class::new` (constructor reference).

Chained comparators — reused constantly for "sort by X then Y":
```java
list.sort(Comparator.comparingInt(Person::age)
                     .thenComparing(Person::name)
                     .reversed());
```

## 6. Streams toolkit
```java
list.stream()
    .filter(x -> x > 0)
    .map(x -> x * 2)
    .sorted()
    .distinct()
    .limit(10)
    .skip(2)
    .collect(Collectors.toList());

int sum = list.stream().reduce(0, Integer::sum);
Optional<Integer> max = list.stream().max(Integer::compareTo);

// List<List<Integer>> -> List<Integer>
List<Integer> flat = nested.stream().flatMap(List::stream).collect(Collectors.toList());

// group / partition — your hammer for "bucket by key" instead of hand-rolled computeIfAbsent
Map<Integer, List<String>> byLen      = words.stream().collect(Collectors.groupingBy(String::length));
Map<Integer, Long>         countByLen = words.stream().collect(Collectors.groupingBy(String::length, Collectors.counting()));
Map<Boolean, List<Integer>> evenOdd   = nums.stream().collect(Collectors.partitioningBy(n -> n % 2 == 0));

String csv = words.stream().collect(Collectors.joining(", ", "[", "]"));

// toMap NEEDS a merge function if keys can repeat, else IllegalStateException
Map<String, Integer> freq = words.stream()
        .collect(Collectors.toMap(w -> w, w -> 1, Integer::sum));

// numeric loops without hand-managing an index
IntStream.range(0, n).forEach(i -> { /* ... */ });
int total = IntStream.rangeClosed(1, n).sum();
```

## 7. Newer syntax that saves keystrokes
```java
var seen = new HashMap<Integer, Integer>();     // type inferred, still statically typed

switch (day) {                                   // arrow form: no fallthrough, can yield a value
    case MON, TUE, WED, THU, FRI -> System.out.println("weekday");
    case SAT, SUN -> System.out.println("weekend");
}
int numLetters = switch (day) {
    case MON, FRI, SUN -> 6;
    case TUE           -> 7;
    default             -> { yield 9; }
};

record Point(int x, int y) {}                    // ctor + getters + equals/hashCode/toString, free

if (obj instanceof String s && s.length() > 3) { // pattern matching: cast + bind in one line
    System.out.println(s.toUpperCase());
}

List<Integer> imm = List.of(1, 2, 3);            // List.of / Set.of / Map.of — immutable factories
Map<String, Integer> m = Map.of("a", 1, "b", 2);
```

## 8. One-liners worth having in muscle memory
```java
Collections.max(list, Comparator.comparingInt(Person::age));
Collections.frequency(list, target);
Collections.reverse(list);
list.sort(Comparator.reverseOrder());
Arrays.fill(arr, -1);
Arrays.sort(intervals, Comparator.comparingInt(a -> a[0]));         // sort a 2D array by column 0
String rev = new StringBuilder(str).reverse().toString();
Character.isDigit(c); Character.isLetter(c); Character.toLowerCase(c);
Integer.parseInt(s); String.valueOf(n);
int[][] copy = Arrays.stream(grid).map(int[]::clone).toArray(int[][]::new);   // shallow-clone each row
```

## 9. Autoboxing gotcha
```java
Integer a = 127, b = 127;
a == b;        // true  -- both come from the Integer cache, range -128..127
Integer c = 200, d = 200;
c == d;        // false -- outside the cache range, two distinct objects
c.equals(d);   // true  -- ALWAYS use equals for boxed types
```
`HashMap`/`HashSet` are safe — they use `equals`/`hashCode` internally. This only bites you when you write `==` yourself, e.g. comparing two `Integer` values pulled off a stack or out of an array.

---
See also: [ROADMAP.md](ROADMAP.md) for how these fit into the bigger structures, [templates.md](templates.md) for the full algorithm-pattern skeletons.
