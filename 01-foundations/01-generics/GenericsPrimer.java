import java.util.*;
import java.util.function.*;

/* ==========================================================================
 * GENERICS PRIMER  —  read this FIRST, every other file assumes it.
 * Run: java GenericsPrimer.java
 * See ./README.md for the mental model (type erasure, naming convention,
 * bounded params, wildcards + PECS) — not repeated here.
 * ========================================================================== */
public class GenericsPrimer {

    /*
     * ---------------------------------------------------------------
     * 1. GENERIC CLASS
     * ---------------------------------------------------------------
     */
    static class Box<T> {
        private T value; // T erased to Object in bytecode

        Box(T value) {
            this.value = value;
        }

        T get() {
            return value;
        }

        void set(T v) {
            this.value = v;
        }

        // Generic METHOD inside a generic class; <U> is independent of <T>.
        <U> Box<U> map(Function<T, U> fn) {
            return new Box<>(fn.apply(value));
        }

        @Override
        public String toString() {
            return "Box(" + value + ")";
        }
    }

    /*
     * ---------------------------------------------------------------
     * 2. GENERIC CLASS WITH TWO PARAMS (this is literally Map.Entry)
     * ---------------------------------------------------------------
     */
    static class Pair<K, V> {
        final K key;
        final V val;

        Pair(K k, V v) {
            key = k;
            val = v;
        }

        // Static generic factory: <K,V> BEFORE the return type.
        // Static methods cannot use the CLASS type params - they need their own.
        static <A, B> Pair<A, B> of(A a, B b) {
            return new Pair<>(a, b);
        }

        @Override
        public String toString() {
            return "(" + key + ", " + val + ")";
        }
    }

    /*
     * ---------------------------------------------------------------
     * 3. BOUNDED TYPE PARAMETER
     * `T extends Comparable<T>` unlocks compareTo(). Without the bound the
     * compiler only knows T is an Object and a.compareTo(b) won't compile.
     * ---------------------------------------------------------------
     */
    static <T extends Comparable<T>> T max(List<T> list) {
        if (list == null || list.isEmpty())
            throw new IllegalArgumentException("empty");
        T best = list.get(0);
        for (T t : list)
            if (t.compareTo(best) > 0)
                best = t;
        return best;
    }

    /*
     * The JDK-grade version. Note `? super T` on Comparable: lets you compare
     * Integer using a Comparable<Number>. This is PECS applied to the bound.
     */
    static <T extends Comparable<? super T>> T maxStrict(Collection<? extends T> c) {
        Iterator<? extends T> it = c.iterator();
        T best = it.next();
        while (it.hasNext()) {
            T t = it.next();
            if (t.compareTo(best) > 0)
                best = t;
        }
        return best;
    }

    /*
     * ---------------------------------------------------------------
     * 4. PECS IN PRACTICE
     * ---------------------------------------------------------------
     */
    // PRODUCER: we only pull Numbers out -> `extends`
    static double sumAll(List<? extends Number> nums) {
        double s = 0;
        for (Number n : nums)
            s += n.doubleValue();
        // nums.add(1); // COMPILE ERROR: could be List<Double>, adding Integer unsafe
        return s;
    }

    // CONSUMER: we only push Integers in -> `super`
    static void fill(List<? super Integer> sink, int n) {
        for (int i = 0; i < n; i++)
            sink.add(i); // legal: Integer fits any supertype list
        // Integer x = sink.get(0); // COMPILE ERROR: reads come back as Object
    }

    /*
     * ---------------------------------------------------------------
     * 5. GENERIC ARRAY WORKAROUND (you WILL need this for Trie/Heap/HashMap)
     * You cannot do `new T[n]`. Two accepted workarounds:
     * a) (T[]) new Object[n] + @SuppressWarnings("unchecked")
     * b) Use ArrayList<T> internally
     * ---------------------------------------------------------------
     */
    static class DynArray<T> {
        private T[] data;
        private int size;

        @SuppressWarnings("unchecked")
        DynArray(int cap) {
            data = (T[]) new Object[cap];
        } // heap pollution is contained

        DynArray() {
            this(8);
        }

        void add(T t) {
            if (size == data.length)
                data = Arrays.copyOf(data, size * 2);
            data[size++] = t;
        }

        T get(int i) {
            if (i < 0 || i >= size)
                throw new IndexOutOfBoundsException("" + i);
            return data[i];
        }

        int size() {
            return size;
        }
    }

    /*
     * ---------------------------------------------------------------
     * 6. RECURSIVE GENERIC BOUND — appears in every tree/node interview
     * "T is a type that can be compared to itself"
     * ---------------------------------------------------------------
     */
    static class Node<T extends Comparable<T>> {
        T val;
        Node<T> left, right;

        Node(T v) {
            val = v;
        }
    }

    /*
     * ---------------------------------------------------------------
     * 7. COMPARABLE vs COMPARATOR — the single most used generic interfaces
     *
     * Comparable<T> -> `int compareTo(T o)` natural order, ON the class
     * Comparator<T> -> `int compare(T a, T b)` external order, pluggable
     *
     * Contract: negative => a before b, 0 => equal, positive => a after b.
     * MEMORY HOOK: "a.compareTo(b) < 0  reads as  a < b"
     * NEVER write `a - b` for ints that can overflow. Use Integer.compare(a,b).
     * ---------------------------------------------------------------
     */
    record Person(String name, int age) {
    }

    /* ================================================================== */
    public static void main(String[] args) {
        System.out.println("--- 1. Generic class ---");
        Box<String> b = new Box<>("hello");
        System.out.println(b + " -> mapped: " + b.map(String::length));

        System.out.println("\n--- 2. Pair ---");
        System.out.println(Pair.of("id", 42));

        System.out.println("\n--- 3. Bounded param ---");
        System.out.println("max = " + max(Arrays.asList(3, 9, 2, 7)));
        System.out.println("maxStrict = " + maxStrict(Set.of("pear", "apple", "zebra")));

        System.out.println("\n--- 4. PECS ---");
        List<Integer> ints = List.of(1, 2, 3);
        System.out.println("sumAll(List<Integer>) = " + sumAll(ints)); // extends: ok
        List<Object> sink = new ArrayList<>();
        fill(sink, 3); // super: ok
        System.out.println("filled sink = " + sink);

        System.out.println("\n--- 5. Generic array workaround ---");
        DynArray<String> da = new DynArray<>(2);
        da.add("a");
        da.add("b");
        da.add("c"); // triggers grow
        System.out.println("size=" + da.size() + " [1]=" + da.get(1));

        System.out.println("\n--- 6. Type erasure proof ---");
        List<String> ls = new ArrayList<>();
        List<Integer> li = new ArrayList<>();
        // At runtime both are just java.util.ArrayList:
        System.out.println("ls.getClass() == li.getClass() ? " + ls.getClass() + " == " + li.getClass() + " ? "
                + (ls.getClass() == li.getClass()));

        System.out.println("\n--- 7. Comparable vs Comparator ---");
        List<Person> people = new ArrayList<>(List.of(
                new Person("Ann", 30), new Person("Bob", 25), new Person("Cat", 30)));

        // Comparator chaining - memorise this fluent form, it saves minutes in
        // interviews. Gotcha: reversed() flips the WHOLE chain (age AND name both
        // go descending), not just the primary key - a common interview trap.
        people.sort(Comparator.comparingInt(Person::age)
                .thenComparing(Person::name)
                .reversed());
        System.out.println("sorted desc by (age,name): " + people);

        // Lambda form, explicit
        people.sort((p1, p2) -> Integer.compare(p1.age(), p2.age()));
        System.out.println("sorted asc by age       : " + people);
    }
}

/*
 * ==========================================================================
 * QUICK SYNTAX REFERENCE (forms not otherwise exercised above)
 * ==========================================================================
 * DECLARE A GENERIC METHOD static <T> void f(T t) // <T> before return type
 * DIAMOND new HashMap<>() // infer from left side
 * VAR + GENERICS var m = new HashMap<String,Integer>();
 * RAW TYPE List l = ... // never do this
 * UNBOUNDED WILDCARD void print(Collection<?> c) // read-only as Object
 * CAPTURE HELPER private static <T> void swapHelper(List<T> l,int i,int j)
 * // needed when you have List<?> and must write to it
 *
 * Interview one-liners + why-they're-true: see ./README.md
 * ==========================================================================
 */
