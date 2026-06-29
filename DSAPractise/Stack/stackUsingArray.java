public class stackUsingArray {

    public static void main(String[] args) {
        ArrayStack s = new ArrayStack(5);   // a stack that holds up to 5 items
        s.push(10);
        s.push(20);
        s.push(30);
        System.out.println("Top: " + s.peek());   // 30
        System.out.println("Popped: " + s.pop());  // 30
        System.out.println("Popped: " + s.pop());  // 20
        System.out.println("Size: " + s.size());    // 1
        System.out.println("Empty? " + s.isEmpty()); // false
    }
}

class ArrayStack {
    private int top;        // index of the top element; -1 means empty
    private int[] stack;    // backing array

    // Constructor: create the array and mark the stack empty
    public ArrayStack(int capacity) {
        stack = new int[capacity];
        top = -1;   // -1 means "nothing pushed yet"
    }

    public void push(int elem) {
        if (top == stack.length - 1) {          // overflow check
            System.out.println("Stack is full!");
            return;
        }
        top++;
        stack[top] = elem;
    }

    public int pop() {
        if (isEmpty()) {                         // underflow check
            System.out.println("Stack is empty!");
            return -1;
        }
        int removed = stack[top];
        top--;
        return removed;
    }

    public int peek() {                          // look at top without removing
        if (isEmpty()) {
            System.out.println("Stack is empty!");
            return -1;
        }
        return stack[top];
    }

    public boolean isEmpty() {
        return top == -1;
    }

    public int size() {
        return top + 1;
    }
}
