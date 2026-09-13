import java.util.*;

/* ==========================================================================
 * DS: STACK  (LIFO - Last In, First Out)
 * Run: java StackNotes.java
 * ==========================================================================
 *
 * MENTAL MODEL
 * ------------
 *   push(1) push(2) push(3)            pop() -> 3
 *
 *      top -> [ 3 ]                        top -> [ 2 ]
 *             [ 2 ]                               [ 1 ]
 *             [ 1 ]                               -----
 *             -----
 *
 * Linked-list view (what we implement):
 *
 *   top
 *    |
 *    v
 *   [3|next]--->[2|next]--->[1|null]
 *
 *   push: newNode.next = top; top = newNode;      <- O(1), prepend
 *   pop : val = top.data;   top = top.next;       <- O(1), behead
 *
 * COMPLEXITY
 * ----------
 *   push O(1) | pop O(1) | peek O(1) | search O(n) | space O(n)
 *
 * WHEN THE INTERVIEWER EXPECTS A STACK
 * ------------------------------------
 *   - matching/balancing pairs  ()[]{}
 *   - "previous/next greater or smaller element"  -> MONOTONIC STACK
 *   - undo history, backtracking, DFS iterative
 *   - expression parsing / evaluation (RPN, infix)
 *   - anything with nested structure
 * ========================================================================== */
public class StackNotes {

    /*
     * ======================================================================
     * 1. IMPLEMENTATION A — linked list backed (no resizing, always O(1))
     * ======================================================================
     */
    static class LinkedStack<T> {
        // Static nested class: does NOT need the outer <T>, declares its own.
        private static class Node<T> {
            T data;
            Node<T> next;

            Node(T data) {
                this.data = data;
            }
        }

        private Node<T> top;
        private int size;

        void push(T val) {
            Node<T> n = new Node<>(val);
            n.next = top; // link new node to old top
            top = n; // new node becomes top
            size++;
        }

        T pop() {
            if (top == null)
                throw new NoSuchElementException("stack empty");
            T out = top.data;
            top = top.next; // old head is now unreachable -> GC
            size--;
            return out;
        }

        T peek() {
            if (top == null)
                throw new NoSuchElementException("stack empty");
            return top.data;
        }

        boolean isEmpty() {
            return size == 0;
        }

        int size() {
            return size;
        }
    }

    /*
     * ======================================================================
     * 2. IMPLEMENTATION B — array backed (better cache locality; amortized O(1))
     * ======================================================================
     */
    static class ArrayStack<T> {
        private T[] data;
        private int size;

        @SuppressWarnings("unchecked")
        ArrayStack(int cap) {
            data = (T[]) new Object[cap];
        } // can't do new T[cap]

        ArrayStack() {
            this(16);
        }

        void push(T val) {
            if (size == data.length)
                data = Arrays.copyOf(data, size * 2); // amortized O(1)
            data[size++] = val;
        }

        T pop() {
            if (size == 0)
                throw new NoSuchElementException("stack empty");
            T out = data[--size];
            data[size] = null; // IMPORTANT: null it out or you leak the object
            return out;
        }

        T peek() {
            if (size == 0)
                throw new NoSuchElementException("stack empty");
            return data[size - 1];
        }

        boolean isEmpty() {
            return size == 0;
        }

        int size() {
            return size;
        }
    }

    /*
     * ======================================================================
     * 3. CLASSIC FOLLOW-UP: MIN STACK — getMin() in O(1)
     * Trick: keep a second stack of "min so far". Push to it on every push.
     *
     * push 5,3,7,2
     * main: [2,7,3,5] min: [2,3,3,5]
     * ^ top of min == current minimum
     * ======================================================================
     */
    static class MinStack {
        private final Deque<Integer> main = new ArrayDeque<>();
        private final Deque<Integer> mins = new ArrayDeque<>();

        void push(int x) {
            main.push(x);
            mins.push(mins.isEmpty() ? x : Math.min(x, mins.peek()));
        }

        void pop() {
            main.pop();
            mins.pop();
        }

        int top() {
            return main.peek();
        }

        int getMin() {
            return mins.peek();
        }
    }

    /*
     * ======================================================================
     * 4. JAVA API — WHAT TO ACTUALLY TYPE IN AN INTERVIEW
     * ======================================================================
     *
     * ┌────────────────────────────────────────────────────────────────┐
     * │ DO NOT USE java.util.Stack. It extends Vector, every method is │
     * │ synchronized, and it iterates BOTTOM-TO-TOP which is backwards.│
     * │ USE ArrayDeque. Say this out loud in the interview, it scores. │
     * └────────────────────────────────────────────────────────────────┘
     *
     * Deque<Integer> st = new ArrayDeque<>();
     *
     * st.push(x) // == addFirst -> O(1)
     * st.pop() // == removeFirst, throws NoSuchElementException if empty
     * st.peek() // == peekFirst, returns null if empty
     * st.isEmpty()
     * st.size()
     *
     * THROWS vs RETURNS-NULL (Deque has both flavours):
     * throws: addFirst / removeFirst / getFirst
     * null: offerFirst / pollFirst / peekFirst
     *
     * Iterating an ArrayDeque used as a stack goes TOP -> BOTTOM. Correct.
     * ArrayDeque forbids null elements. Use LinkedList if you truly need nulls.
     */

    /*
     * ======================================================================
     * 5. PATTERN: VALID PARENTHESES (LC 20)
     * Push openers; on a closer, the top must be its match.
     * ======================================================================
     */
    static boolean isValidParens(String s) {
        Deque<Character> st = new ArrayDeque<>();
        Map<Character, Character> pair = Map.of(')', '(', ']', '[', '}', '{');
        for (char c : s.toCharArray()) {
            if (pair.containsValue(c)) { // an opener
                st.push(c);
            } else if (pair.containsKey(c)) { // a closer
                if (st.isEmpty() || st.pop() != pair.get(c))
                    return false;
            }
        }
        return st.isEmpty(); // leftovers = unclosed
    }

    /*
     * ======================================================================
     * 6. PATTERN: MONOTONIC STACK — "next greater element" (LC 739 / 496)
     *
     * Invariant: stack holds INDICES whose values are strictly decreasing.
     * When the incoming value breaks the invariant, everything it beats gets
     * resolved and popped. Each index is pushed once and popped once -> O(n).
     *
     * temps = [73,74,75,71,69,72,76,73]
     * i=1 (74) > 73 -> resolve index0, answer[0]=1
     * ...
     *
     * stack (indices, values decreasing):
     * [75]
     * [74] <- incoming 76 pops all of these
     * [73]
     * ======================================================================
     */
    static int[] dailyTemperatures(int[] t) {
        int[] res = new int[t.length];
        Deque<Integer> st = new ArrayDeque<>(); // indices, values monotonically decreasing
        for (int i = 0; i < t.length; i++) {
            while (!st.isEmpty() && t[i] > t[st.peek()]) {
                int prev = st.pop();
                res[prev] = i - prev; // days waited
            }
            st.push(i);
        }
        return res; // unresolved stay 0
    }

    /*
     * ======================================================================
     * 7. PATTERN: EVALUATE REVERSE POLISH NOTATION (LC 150)
     * Operands push, operator pops TWO. Order matters for - and /.
     * ======================================================================
     */
    static int evalRPN(String[] tokens) {
        Deque<Integer> st = new ArrayDeque<>();
        for (String tk : tokens) {
            switch (tk) {
                case "+" -> st.push(st.pop() + st.pop());
                case "*" -> st.push(st.pop() * st.pop());
                case "-" -> {
                    int b = st.pop(), a = st.pop();
                    st.push(a - b);
                } // order!
                case "/" -> {
                    int b = st.pop(), a = st.pop();
                    st.push(a / b);
                } // order!
                default -> st.push(Integer.parseInt(tk));
            }
        }
        return st.pop();
    }

    /*
     * ======================================================================
     * 8. PATTERN: LARGEST RECTANGLE IN HISTOGRAM (LC 84) — monotonic INCREASING
     * When we pop a bar, that bar's height can extend:
     * right -> up to current i (exclusive)
     * left -> just after the new stack top
     * ======================================================================
     */
    static int largestRectangleArea(int[] heights) {
        Deque<Integer> st = new ArrayDeque<>(); // indices, heights increasing
        int best = 0;
        for (int i = 0; i <= heights.length; i++) {
            int cur = (i == heights.length) ? 0 : heights[i]; // sentinel flushes stack
            while (!st.isEmpty() && heights[st.peek()] >= cur) {
                int h = heights[st.pop()];
                int leftBound = st.isEmpty() ? -1 : st.peek();
                int width = i - leftBound - 1;
                best = Math.max(best, h * width);
            }
            st.push(i);
        }
        return best;
    }

    /* ================================================================== */
    public static void main(String[] args) {
        System.out.println("--- custom LinkedStack ---");
        LinkedStack<Integer> ls = new LinkedStack<>();
        ls.push(1);
        ls.push(2);
        ls.push(3);
        System.out.println("peek=" + ls.peek() + " pop=" + ls.pop() + " size=" + ls.size());

        System.out.println("\n--- custom ArrayStack ---");
        ArrayStack<String> as = new ArrayStack<>(2);
        as.push("a");
        as.push("b");
        as.push("c");
        System.out.println("pop=" + as.pop() + " peek=" + as.peek());

        System.out.println("\n--- MinStack ---");
        MinStack ms = new MinStack();
        ms.push(5);
        ms.push(3);
        ms.push(7);
        ms.push(2);
        System.out.println("min=" + ms.getMin() + " top=" + ms.top());
        ms.pop();
        System.out.println("after pop, min=" + ms.getMin());

        System.out.println("\n--- java.util.ArrayDeque as stack ---");
        Deque<Integer> st = new ArrayDeque<>();
        st.push(10);
        st.push(20);
        st.push(30);
        System.out.println("iteration order (top->bottom): " + st);

        System.out.println("\n--- patterns ---");
        System.out.println("isValidParens(\"{[()]}\") = " + isValidParens("{[()]}"));
        System.out.println("isValidParens(\"([)]\")   = " + isValidParens("([)]"));
        System.out.println("dailyTemperatures        = " +
                Arrays.toString(dailyTemperatures(new int[] { 73, 74, 75, 71, 69, 72, 76, 73 })));
        System.out.println("evalRPN [2,1,+,3,*]      = " +
                evalRPN(new String[] { "2", "1", "+", "3", "*" }));
        System.out.println("largestRectangle [2,1,5,6,2,3] = " +
                largestRectangleArea(new int[] { 2, 1, 5, 6, 2, 3 }));
    }
}

/*
 * ==========================================================================
 * PRACTICE — BLIND 75 / NEETCODE (stack)
 * ==========================================================================
 * LC 20 Valid Parentheses easy warm-up, do first
 * LC 155 Min Stack med two-stack trick
 * LC 150 Evaluate Reverse Polish Notation med operand order
 * LC 22 Generate Parentheses med backtracking + counts
 * LC 739 Daily Temperatures med monotonic decreasing
 * LC 853 Car Fleet med sort + monotonic
 * LC 84 Largest Rectangle in Histogram hard monotonic increasing
 * LC 232 Implement Queue using Stacks easy amortized O(1) trick
 * LC 394 Decode String med two stacks (num, str)
 * LC 42 Trapping Rain Water hard stack OR two-pointer
 *
 * SELF-TEST QUESTIONS
 * - Why ArrayDeque over java.util.Stack?
 * - What is the amortized cost of push on an array-backed stack, and why?
 * - How do you recognise a monotonic stack problem from the prompt?
 * - Why is a monotonic stack O(n) and not O(n^2)?
 * ==========================================================================
 */
