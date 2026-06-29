import java.util.ArrayDeque;
import java.util.Deque;

// STACK using Java's built-in Deque (the real-world / interview-preferred way).
// WHY ArrayDeque over the old java.util.Stack class: Stack is legacy & synchronized
// (slower). ArrayDeque is the recommended modern choice for a stack.
public class stackUsingCollections {

    public static void main(String[] args) {
        Deque<Integer> stack = new ArrayDeque<>();

        stack.push(10);   // add on top
        stack.push(20);
        stack.push(30);

        System.out.println("Top (peek): " + stack.peek());  // 30, not removed
        System.out.println("Popped: " + stack.pop());        // 30
        System.out.println("Popped: " + stack.pop());        // 20
        System.out.println("Size: " + stack.size());          // 1
        System.out.println("Empty? " + stack.isEmpty());      // false
        System.out.println("Stack now: " + stack);            // [10]
    }
}
