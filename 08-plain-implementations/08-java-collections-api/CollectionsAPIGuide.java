import java.util.*;
import java.util.stream.*;

/* java.util cheat sheet — every collection type + every interview-relevant method,
 * Java 8 through 21, all runnable. Run: java CollectionsAPIGuide.java
 * For quick JS->Java syntax lookups see ../../java-api-examples.md — this file goes
 * deeper: full NavigableSet/NavigableMap APIs, Deque method set, fail-fast iterators,
 * Collectors edge cases, and exactly which Java version introduced what. */
public class CollectionsAPIGuide {

    record Person(String name, int age) {}

    // Java 17 - sealed: a closed, compiler-known set of implementations (must be a top-level
    // or static member type - local sealed classes/interfaces inside a method are not allowed).
    sealed interface Shape permits Circle, Square {}
    record Circle(double radius) implements Shape {}
    record Square(double side) implements Shape {}

    public static void main(String[] args) {
        list();
        set();
        map();
        queueDeque();
        iterationAndBulkOps();
        streamsAndCollectors();
        javaVersionHighlights();
    }

    /* ======================================================================
     * 1. LIST
     * ====================================================================== */
    static void list() {
        System.out.println("=== 1. LIST ===");

        System.out.println("-- ArrayList vs LinkedList: when each wins --");
        System.out.println("ArrayList: contiguous array, O(1) random access (get/set), O(n) insert/remove in the middle.");
        System.out.println("LinkedList: doubly-linked nodes, O(1) insert/remove at a known node (e.g. via ListIterator),");
        System.out.println("            O(n) get(i) since it must walk from head or tail. Default to ArrayList unless");
        System.out.println("            you specifically need cheap insert/remove at both ends without index math (ArrayDeque usually wins there anyway).");

        System.out.println("\n-- List.of(...) (Java 9) — fully immutable --");
        List<String> immutable = List.of("a", "b", "c");
        System.out.println("List.of(a,b,c) = " + immutable);
        try {
            immutable.add("d");
            System.out.println("add succeeded (should not happen)");
        } catch (UnsupportedOperationException e) {
            System.out.println("immutable.add(\"d\") threw UnsupportedOperationException, as expected");
        }

        System.out.println("\n-- Arrays.asList(arr) — fixed-size VIEW backed by the array --");
        Integer[] backing = {1, 2, 3};
        List<Integer> asListView = Arrays.asList(backing);
        asListView.set(0, 99); // allowed: fixed-size, not immutable
        System.out.println("after asListView.set(0,99): backing array = " + Arrays.toString(backing));
        try {
            asListView.add(4);
            System.out.println("add succeeded (should not happen)");
        } catch (UnsupportedOperationException e) {
            System.out.println("asListView.add(4) threw UnsupportedOperationException (fixed-size, not resizable)");
        }
        backing[1] = 500; // mutate the ARRAY directly
        System.out.println("after backing[1]=500 (mutated array directly): asListView = " + asListView + " <- view sees it, it's backed by the array");

        System.out.println("\n-- Collections.unmodifiableList --");
        List<Integer> real = new ArrayList<>(List.of(10, 20, 30));
        List<Integer> readOnlyView = Collections.unmodifiableList(real);
        try {
            readOnlyView.add(40);
        } catch (UnsupportedOperationException e) {
            System.out.println("unmodifiableList.add(40) threw UnsupportedOperationException");
        }
        real.add(40); // mutating the BACKING list still works
        System.out.println("after real.add(40) on the backing list, readOnlyView = " + readOnlyView + " <- also just a view, not a real copy");

        System.out.println("\n-- list.sort(Comparator) vs Collections.sort(list) --");
        List<Integer> a1 = new ArrayList<>(List.of(5, 3, 4, 1, 2));
        List<Integer> a2 = new ArrayList<>(a1);
        a1.sort(Comparator.reverseOrder());      // instance method, added Java 8, default method on List
        Collections.sort(a2);                     // static utility, pre-8, natural order only unless overload passed
        System.out.println("a1.sort(reverseOrder) = " + a1);
        System.out.println("Collections.sort(a2)  = " + a2 + "  (Collections.sort also has a 2-arg Comparator overload)");

        System.out.println("\n-- removeIf --");
        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8));
        nums.removeIf(n -> n % 2 == 0);
        System.out.println("removeIf(even) -> " + nums);

        System.out.println("\n-- replaceAll --");
        List<Integer> squares = new ArrayList<>(List.of(1, 2, 3, 4));
        squares.replaceAll(n -> n * n);
        System.out.println("replaceAll(n -> n*n) -> " + squares);

        System.out.println("\n-- subList is a LIVE view, not a copy --");
        List<Integer> backingList = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        List<Integer> sub = backingList.subList(1, 4); // view over indices [1,4)
        System.out.println("backingList = " + backingList + ", sub = " + sub);
        sub.set(0, 99);
        System.out.println("after sub.set(0,99): backingList = " + backingList + " <- mutation through the view hit the original");
        sub.clear();
        System.out.println("after sub.clear(): backingList = " + backingList + " <- clearing the view removed those elements from the original");

        System.out.println("\n-- indexOf/contains cost: ArrayList vs LinkedList --");
        System.out.println("Both are O(n) worst case on either structure (no index/hash to shortcut a value search),");
        System.out.println("but ArrayList's contiguous array wins on constant factor (cache-friendly linear scan)");
        System.out.println("while LinkedList must chase pointers node to node. Use a Set/Map instead if contains() is hot.");
    }

    /* ======================================================================
     * 2. SET
     * ====================================================================== */
    static void set() {
        System.out.println("\n=== 2. SET ===");

        System.out.println("-- HashSet: no order guarantee --");
        Set<Integer> hashSet = new HashSet<>(List.of(5, 3, 1, 4, 2));
        System.out.println("HashSet iteration order (bucket order, NOT insertion order): " + hashSet);

        System.out.println("\n-- LinkedHashSet: insertion order preserved --");
        Set<Integer> linkedHashSet = new LinkedHashSet<>();
        for (int n : new int[]{5, 3, 1, 4, 2}) linkedHashSet.add(n);
        System.out.println("LinkedHashSet iteration order (insertion order): " + linkedHashSet);

        System.out.println("\n-- TreeSet: sorted + full NavigableSet API --");
        TreeSet<Integer> ts = new TreeSet<>(List.of(1, 5, 9, 12, 20, 30));
        System.out.println("TreeSet (always sorted) = " + ts);
        System.out.println("first()          = " + ts.first());
        System.out.println("last()           = " + ts.last());
        System.out.println("higher(9)        = " + ts.higher(9) + "  (strictly greater than 9)");
        System.out.println("lower(9)         = " + ts.lower(9) + "  (strictly less than 9)");
        System.out.println("ceiling(10)      = " + ts.ceiling(10) + "  (smallest value >= 10)");
        System.out.println("floor(10)        = " + ts.floor(10) + "  (largest value <= 10)");
        System.out.println("headSet(9)       = " + ts.headSet(9) + "  (all values < 9)");
        System.out.println("tailSet(9)       = " + ts.tailSet(9) + "  (all values >= 9)");
        System.out.println("subSet(5,20)     = " + ts.subSet(5, 20) + "  ([5,20) half-open range)");
        System.out.println("descendingSet()  = " + ts.descendingSet());
        System.out.println("pollFirst()      = " + ts.pollFirst() + "  -> set now " + ts);
        System.out.println("pollLast()       = " + ts.pollLast() + "  -> set now " + ts);

        System.out.println("\n-- Set.of(...) (Java 9): immutable, throws on duplicate at construction --");
        Set<Integer> immutableSet = Set.of(1, 2, 3);
        System.out.println("Set.of(1,2,3) = " + immutableSet);
        try {
            Set.of(1, 2, 2); // duplicate literal element
            System.out.println("Set.of with duplicate succeeded (should not happen)");
        } catch (IllegalArgumentException e) {
            System.out.println("Set.of(1,2,2) threw IllegalArgumentException: " + e.getMessage());
        }
        try {
            immutableSet.add(4);
        } catch (UnsupportedOperationException e) {
            System.out.println("immutableSet.add(4) threw UnsupportedOperationException");
        }
    }

    /* ======================================================================
     * 3. MAP
     * ====================================================================== */
    static void map() {
        System.out.println("\n=== 3. MAP ===");

        System.out.println("-- HashMap: no order guarantee --");
        Map<String, Integer> hashMap = new HashMap<>();
        hashMap.put("banana", 2); hashMap.put("apple", 1); hashMap.put("cherry", 3);
        System.out.println("HashMap iteration order (bucket order): " + hashMap);

        System.out.println("\n-- LinkedHashMap: insertion-order mode (default) --");
        Map<String, Integer> insertionOrder = new LinkedHashMap<>();
        insertionOrder.put("banana", 2); insertionOrder.put("apple", 1); insertionOrder.put("cherry", 3);
        System.out.println("LinkedHashMap insertion order: " + insertionOrder);

        System.out.println("\n-- LinkedHashMap: ACCESS-order mode (3-arg ctor) — LRU building block --");
        // 3rd arg true = access order: get()/put() move an entry to the end (most-recently-used).
        Map<String, Integer> lru = new LinkedHashMap<>(16, 0.75f, true);
        lru.put("a", 1); lru.put("b", 2); lru.put("c", 3);
        System.out.println("initial order: " + lru + " (a,b,c)");
        lru.get("a"); // touching "a" should move it to the end
        System.out.println("after get(\"a\"): " + lru + " <- \"a\" moved to the end (most recently used)");

        System.out.println("\n-- TreeMap: full NavigableMap API --");
        TreeMap<Integer, String> tm = new TreeMap<>();
        tm.put(10, "ten"); tm.put(20, "twenty"); tm.put(30, "thirty"); tm.put(40, "forty");
        System.out.println("TreeMap (always sorted by key) = " + tm);
        System.out.println("floorKey(25)       = " + tm.floorKey(25));
        System.out.println("ceilingKey(25)     = " + tm.ceilingKey(25));
        System.out.println("firstEntry()       = " + tm.firstEntry());
        System.out.println("lastEntry()        = " + tm.lastEntry());
        System.out.println("headMap(30)        = " + tm.headMap(30) + "  (keys < 30)");
        System.out.println("tailMap(30)        = " + tm.tailMap(30) + "  (keys >= 30)");
        System.out.println("subMap(10,30)      = " + tm.subMap(10, 30) + "  ([10,30) half-open range)");
        System.out.println("descendingMap()    = " + tm.descendingMap());
        System.out.println("pollFirstEntry()   = " + tm.pollFirstEntry() + "  -> map now " + tm);

        System.out.println("\n-- Map.of / Map.entry / Map.ofEntries (Java 9) --");
        Map<String, Integer> mapOf = Map.of("x", 1, "y", 2);
        System.out.println("Map.of(x,1,y,2) = " + mapOf);
        Map<String, Integer> mapOfEntries = Map.ofEntries(
                Map.entry("p", 10),
                Map.entry("q", 20),
                Map.entry("r", 30));
        System.out.println("Map.ofEntries(Map.entry(...)...) = " + mapOfEntries);

        System.out.println("\n-- merge / compute / computeIfAbsent / computeIfPresent / getOrDefault / putIfAbsent / replaceAll / forEach --");
        Map<String, Integer> wordCount = new HashMap<>();
        for (String w : new String[]{"cat", "dog", "cat", "bird", "cat", "dog"}) {
            wordCount.merge(w, 1, Integer::sum); // classic frequency-count one-liner
        }
        System.out.println("merge-based frequency count: " + wordCount);

        Map<String, List<Integer>> groups = new HashMap<>();
        groups.computeIfAbsent("evens", k -> new ArrayList<>()).add(2);
        groups.computeIfAbsent("evens", k -> new ArrayList<>()).add(4); // no new list created 2nd time
        System.out.println("computeIfAbsent-built groups: " + groups);

        Map<String, Integer> scores = new HashMap<>(Map.of("alice", 90));
        scores.computeIfPresent("alice", (k, v) -> v + 5); // only runs because key exists
        scores.computeIfPresent("bob", (k, v) -> v + 5);   // no-op, "bob" absent
        System.out.println("computeIfPresent on existing/missing key: " + scores);

        System.out.println("getOrDefault(\"missing\", -1) = " + scores.getOrDefault("missing", -1));

        scores.putIfAbsent("alice", 0);   // no-op, already present
        scores.putIfAbsent("carol", 70);  // inserted
        System.out.println("after putIfAbsent(alice,0) [no-op] and putIfAbsent(carol,70): " + scores);

        scores.compute("alice", (k, v) -> v == null ? 1 : v + 1); // increment-or-init in one call
        System.out.println("compute(alice, increment) = " + scores);

        scores.replaceAll((k, v) -> v * 10);
        System.out.println("replaceAll(v -> v*10) = " + scores);

        StringBuilder forEachOut = new StringBuilder();
        scores.forEach((k, v) -> forEachOut.append(k).append("=").append(v).append(" "));
        System.out.println("forEach concatenation: " + forEachOut.toString().trim());
    }

    /* ======================================================================
     * 4. QUEUE / DEQUE
     * ====================================================================== */
    static void queueDeque() {
        System.out.println("\n=== 4. QUEUE / DEQUE ===");

        System.out.println("-- Queue: throwing vs non-throwing method pairs --");
        Queue<Integer> q = new LinkedList<>();
        System.out.println("add(1)=" + q.add(1) + "  offer(2)=" + q.offer(2) + "  (both insert; add() would throw on a capacity-bounded full queue, offer() returns false instead)");
        System.out.println("remove()=" + q.remove() + "  poll()=" + q.poll() + "  queue now empty");
        try {
            q.remove(); // empty queue
        } catch (NoSuchElementException e) {
            System.out.println("remove() on empty queue threw NoSuchElementException");
        }
        System.out.println("poll() on empty queue returns (no throw): " + q.poll());
        try {
            q.element(); // empty queue
        } catch (NoSuchElementException e) {
            System.out.println("element() on empty queue threw NoSuchElementException");
        }
        System.out.println("peek() on empty queue returns (no throw): " + q.peek());

        System.out.println("\n-- ArrayDeque as a STACK (push/pop) — replaces legacy java.util.Stack (synchronized, avoid) --");
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(1); stack.push(2); stack.push(3); // pushes onto the head
        System.out.println("stack after push 1,2,3: " + stack + "  pop()=" + stack.pop() + "  pop()=" + stack.pop());

        System.out.println("\n-- ArrayDeque as a QUEUE (offer/poll) — replaces a hand-rolled queue --");
        Deque<Integer> asQueue = new ArrayDeque<>();
        asQueue.offer(1); asQueue.offer(2); asQueue.offer(3); // offers onto the tail
        System.out.println("queue after offer 1,2,3: " + asQueue + "  poll()=" + asQueue.poll() + "  poll()=" + asQueue.poll());
        System.out.println("Same ArrayDeque instance type serves both roles — no reason to reach for java.util.Stack or LinkedList for either.");

        System.out.println("\n-- PriorityQueue: min-heap by default --");
        PriorityQueue<Integer> minHeap = new PriorityQueue<>(List.of(5, 1, 4, 2, 3));
        StringBuilder minOrder = new StringBuilder();
        while (!minHeap.isEmpty()) minOrder.append(minHeap.poll()).append(" ");
        System.out.println("polled in order (ascending): " + minOrder.toString().trim());

        System.out.println("\n-- PriorityQueue: max-heap via Collections.reverseOrder() --");
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
        maxHeap.addAll(List.of(5, 1, 4, 2, 3));
        StringBuilder maxOrder = new StringBuilder();
        while (!maxHeap.isEmpty()) maxOrder.append(maxHeap.poll()).append(" ");
        System.out.println("polled in order (descending): " + maxOrder.toString().trim());

        System.out.println("\n-- Deque: full method set (addFirst/addLast/offerFirst/offerLast/pollFirst/pollLast/peekFirst/peekLast) --");
        Deque<Integer> deque = new ArrayDeque<>();
        deque.addFirst(2); deque.addFirst(1);   // deque: [1,2]
        deque.addLast(3); deque.addLast(4);     // deque: [1,2,3,4]
        System.out.println("after addFirst(2),addFirst(1),addLast(3),addLast(4): " + deque);
        deque.offerFirst(0); deque.offerLast(5); // deque: [0,1,2,3,4,5]
        System.out.println("after offerFirst(0), offerLast(5): " + deque);
        System.out.println("peekFirst()=" + deque.peekFirst() + "  peekLast()=" + deque.peekLast() + "  (peek does not remove)");
        System.out.println("pollFirst()=" + deque.pollFirst() + "  pollLast()=" + deque.pollLast() + "  -> deque now " + deque);
    }

    /* ======================================================================
     * 5. ITERATION & BULK OPS
     * ====================================================================== */
    static void iterationAndBulkOps() {
        System.out.println("\n=== 5. ITERATION & BULK OPS ===");

        System.out.println("-- Fail-fast iterator: ConcurrentModificationException --");
        List<Integer> failFastList = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        try {
            for (Integer n : failFastList) {
                if (n == 3) failFastList.remove(n); // structural modification mid for-each
            }
            System.out.println("no exception thrown (should not happen)");
        } catch (ConcurrentModificationException e) {
            System.out.println("removing from a list during for-each threw ConcurrentModificationException, as expected");
        }

        System.out.println("Fix #1 — removeIf:");
        List<Integer> fixed1 = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        fixed1.removeIf(n -> n == 3);
        System.out.println("  " + fixed1);

        System.out.println("Fix #2 — Iterator.remove():");
        List<Integer> fixed2 = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        Iterator<Integer> it = fixed2.iterator();
        while (it.hasNext()) {
            if (it.next() == 3) it.remove(); // safe: the iterator itself tracks the structural change
        }
        System.out.println("  " + fixed2);

        System.out.println("\n-- Collections utility methods --");
        List<Integer> util = new ArrayList<>(List.of(3, 1, 4, 1, 5, 9, 2, 6));
        System.out.println("Collections.max = " + Collections.max(util));
        System.out.println("Collections.min = " + Collections.min(util));
        List<Integer> toReverse = new ArrayList<>(util);
        Collections.reverse(toReverse);
        System.out.println("Collections.reverse -> " + toReverse);
        List<Integer> toShuffle = new ArrayList<>(util);
        Collections.shuffle(toShuffle, new Random(42)); // seeded -> reproducible output every run
        System.out.println("Collections.shuffle(seed=42) -> " + toShuffle);
        System.out.println("Collections.frequency(util, 1) = " + Collections.frequency(util, 1));
        System.out.println("Collections.nCopies(3, \"x\") = " + Collections.nCopies(3, "x"));
        System.out.println("Collections.emptyList() = " + Collections.emptyList());
        System.out.println("Collections.singletonList(42) = " + Collections.singletonList(42));

        System.out.println("\n-- Comparator composition --");
        List<Person> people = new ArrayList<>(List.of(
                new Person("Bob", 30), new Person("Alice", 30),
                new Person("Carol", 25), new Person("Dave", 40)));
        people.sort(Comparator.comparing(Person::age).thenComparing(Person::name));
        System.out.println("comparing(age).thenComparing(name): " + people);

        List<Integer> naturalDemo = new ArrayList<>(List.of(3, 1, 2));
        naturalDemo.sort(Comparator.naturalOrder());
        System.out.println("Comparator.naturalOrder(): " + naturalDemo);
        naturalDemo.sort(Comparator.reverseOrder());
        System.out.println("Comparator.reverseOrder(): " + naturalDemo);

        List<String> withNulls = new ArrayList<>(Arrays.asList("banana", null, "apple", null, "cherry"));
        withNulls.sort(Comparator.nullsFirst(Comparator.naturalOrder()));
        System.out.println("Comparator.nullsFirst(naturalOrder()): " + withNulls);
    }

    /* ======================================================================
     * 6. STREAMS & COLLECTORS (Java 8)
     * ====================================================================== */
    static void streamsAndCollectors() {
        System.out.println("\n=== 6. STREAMS & COLLECTORS (Java 8) ===");

        System.out.println("-- filter / map / reduce --");
        List<Integer> nums = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        int sumOfSquaresOfEvens = nums.stream()
                .filter(n -> n % 2 == 0)
                .map(n -> n * n)
                .reduce(0, Integer::sum);
        System.out.println("sum of squares of evens = " + sumOfSquaresOfEvens);

        System.out.println("\n-- Collectors.toList / toSet --");
        List<Integer> evensList = nums.stream().filter(n -> n % 2 == 0).collect(Collectors.toList());
        Set<Integer> evensSet = nums.stream().filter(n -> n % 2 == 0).collect(Collectors.toSet());
        System.out.println("toList: " + evensList + "   toSet: " + evensSet);

        System.out.println("\n-- Collectors.toMap: throws IllegalStateException on duplicate keys WITHOUT a merge function --");
        List<String> words = List.of("apple", "avocado", "banana", "blueberry", "cherry");
        try {
            Map<Character, String> firstLetterMap = words.stream()
                    .collect(Collectors.toMap(w -> w.charAt(0), w -> w)); // 'a' and 'b' both repeat -> collision
            System.out.println("toMap without merge succeeded (should not happen): " + firstLetterMap);
        } catch (IllegalStateException e) {
            System.out.println("toMap without a merge function threw IllegalStateException on duplicate key, as expected");
        }
        Map<Character, String> firstLetterMerged = words.stream()
                .collect(Collectors.toMap(w -> w.charAt(0), w -> w, (existing, incoming) -> existing + "," + incoming));
        System.out.println("toMap WITH merge function (3-arg form): " + firstLetterMerged);

        System.out.println("\n-- Collectors.groupingBy --");
        Map<Integer, List<String>> byLength = words.stream().collect(Collectors.groupingBy(String::length));
        System.out.println("groupingBy(length): " + byLength);
        Map<Integer, Long> countByLength = words.stream().collect(Collectors.groupingBy(String::length, Collectors.counting()));
        System.out.println("groupingBy(length, counting()): " + countByLength);

        System.out.println("\n-- Collectors.partitioningBy --");
        Map<Boolean, List<Integer>> evenOdd = nums.stream().collect(Collectors.partitioningBy(n -> n % 2 == 0));
        System.out.println("partitioningBy(even): " + evenOdd);

        System.out.println("\n-- Collectors.joining --");
        String joined = words.stream().collect(Collectors.joining(", ", "[", "]"));
        System.out.println("joining(\", \", \"[\", \"]\"): " + joined);

        System.out.println("\n-- Collectors.counting --");
        long count = words.stream().filter(w -> w.length() > 5).collect(Collectors.counting());
        System.out.println("count of words longer than 5 chars: " + count);

        System.out.println("\n-- IntStream: range / sum / average --");
        int rangeSum = IntStream.range(0, 5).sum(); // 0..4
        System.out.println("IntStream.range(0,5).sum() = " + rangeSum);
        int rangeClosedSum = IntStream.rangeClosed(1, 5).sum(); // 1..5
        System.out.println("IntStream.rangeClosed(1,5).sum() = " + rangeClosedSum);
        OptionalDouble avg = IntStream.rangeClosed(1, 5).average();
        System.out.println("IntStream.rangeClosed(1,5).average() = " + avg.getAsDouble());
    }

    /* ======================================================================
     * 7. MODERN JAVA VERSION HIGHLIGHTS (9 through 21)
     * ====================================================================== */
    static void javaVersionHighlights() {
        System.out.println("\n=== 7. MODERN JAVA VERSION HIGHLIGHTS ===");

        System.out.println("Java 9 - immutable collection factories: List.of/Set.of/Map.of/Map.entry/Map.ofEntries");
        System.out.println("  (already demonstrated in full in sections 1-3 above)");

        System.out.println("\nJava 10 - var local type inference:");
        var inferredList = new ArrayList<String>(); // type still fixed at compile time, just not spelled out
        inferredList.add("inferred");
        System.out.println("  var inferredList = new ArrayList<String>(); -> " + inferredList + "  (still statically typed, javac just infers it)");

        System.out.println("\nJava 11 - var in lambda parameters (lets you attach annotations/modifiers to lambda params):");
        java.util.function.BiFunction<Integer, Integer, Integer> addFn = (var x, var y) -> x + y;
        System.out.println("  (var x, var y) -> x + y  applied to (3,4) = " + addFn.apply(3, 4));

        System.out.println("\nJava 11 - Collection.toArray(IntFunction<T[]>):");
        List<String> strList = List.of("a", "b", "c");
        String[] strArr = strList.toArray(String[]::new); // generator overload, cleaner than toArray(new String[0])
        System.out.println("  list.toArray(String[]::new) = " + Arrays.toString(strArr));

        System.out.println("\nJava 12 - Collectors.teeing (fan a single stream into two collectors, combine results):");
        List<Integer> teeNums = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        String teeResult = teeNums.stream().collect(Collectors.teeing(
                Collectors.counting(),
                Collectors.summingInt(Integer::intValue),
                (count, sum) -> "count=" + count + ", sum=" + sum));
        System.out.println("  teeing(counting, summingInt) in one pass -> " + teeResult);

        System.out.println("\nJava 14 - switch EXPRESSIONS with yield (arrow form + yield for multi-statement branches):");
        int dayNum = 3;
        String dayType = switch (dayNum) {
            case 1, 2, 3, 4, 5 -> "weekday";
            case 6, 7 -> "weekend";
            default -> {
                yield "invalid"; // yield needed when the branch is a block, not a single expression
            }
        };
        System.out.println("  switch(" + dayNum + ") as an expression -> " + dayType);

        System.out.println("\nJava 16 - records: compact, structural equals/hashCode/toString for free, safe as a Map key:");
        record Point(int x, int y) {}
        Map<Point, String> pointMap = new HashMap<>();
        pointMap.put(new Point(1, 2), "origin-ish");
        String lookup = pointMap.get(new Point(1, 2)); // a DIFFERENT instance, same field values
        System.out.println("  put(new Point(1,2)), get(new Point(1,2)) [different instance] -> " + lookup + "  (structural equals/hashCode worked)");

        System.out.println("\nJava 16 - pattern matching for instanceof (cast + bind in one expression):");
        Object obj = "pattern-matched string";
        if (obj instanceof String s && s.length() > 10) {
            System.out.println("  obj instanceof String s -> s.toUpperCase() = " + s.toUpperCase());
        }

        System.out.println("\nJava 16 - Stream.toList() as shorthand for .collect(Collectors.toList()):");
        List<Integer> shorthand = teeNums.stream().filter(n -> n > 5).toList(); // returns an unmodifiable list
        System.out.println("  stream().filter(...).toList() -> " + shorthand);

        System.out.println("\nJava 17 - sealed interfaces/classes (closed, compiler-known set of implementations):");
        System.out.println("  see the Shape/Circle/Square declarations at the top of this class - sealed types");
        System.out.println("  can't be declared locally inside a method, only top-level or as static members.");
        List<Shape> shapes = List.of(new Circle(2.0), new Square(3.0));
        for (Shape shape : shapes) {
            // Java 21 - pattern matching for switch + record patterns: destructure AND branch in one expression.
            double area = switch (shape) {
                case Circle(double r) -> Math.PI * r * r;
                case Square(double s) -> s * s;
            };
            System.out.printf("  %s -> area = %.2f  (sealed permits list lets switch be exhaustive with no default)%n", shape, area);
        }

        System.out.println("\nJava 21 - Sequenced Collections: List/ArrayDeque/LinkedHashSet/LinkedHashMap all get");
        System.out.println("          getFirst()/getLast()/addFirst()/addLast()/removeFirst()/removeLast()/reversed() directly.");
        List<Integer> seq = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        System.out.println("  list.getFirst() = " + seq.getFirst() + "   (used to be list.get(0))");
        System.out.println("  list.getLast()  = " + seq.getLast() + "   (used to be list.get(list.size()-1))");
        System.out.println("  list.reversed() = " + seq.reversed() + "   (a reversed VIEW, no manual Collections.reverse needed)");
        seq.addFirst(0);
        seq.addLast(6);
        System.out.println("  after addFirst(0), addLast(6): " + seq);

        LinkedHashSet<String> seqSet = new LinkedHashSet<>(List.of("x", "y", "z"));
        System.out.println("  LinkedHashSet now implements SequencedSet: getFirst()=" + seqSet.getFirst() + "  getLast()=" + seqSet.getLast());

        LinkedHashMap<String, Integer> seqMap = new LinkedHashMap<>();
        seqMap.put("a", 1); seqMap.put("b", 2); seqMap.put("c", 3);
        System.out.println("  LinkedHashMap now implements SequencedMap: firstEntry()=" + seqMap.firstEntry() + "  lastEntry()=" + seqMap.lastEntry());
        System.out.println("  seqMap.reversed() = " + seqMap.reversed());
        System.out.println("  seqMap.sequencedKeySet() = " + seqMap.sequencedKeySet());

        Deque<Integer> seqDeque = new ArrayDeque<>(List.of(10, 20, 30));
        System.out.println("  ArrayDeque already had first/last semantics; now formalized via SequencedCollection: getFirst()=" + seqDeque.getFirst() + "  getLast()=" + seqDeque.getLast());
    }
}
