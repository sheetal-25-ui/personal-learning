public class stackUsingLinkedList {

    public static void main(String[] args) {
        LinkedListStack s = new LinkedListStack();
        s.push(10);
        s.push(20);
        s.push(30);
        System.out.println("Top: " + s.peek());     // 30
        System.out.println("Popped: " + s.pop());    // 30
        System.out.println("Popped: " + s.pop());    // 20
        System.out.println("Size: " + s.size());      // 1
        System.out.println("Empty? " + s.isEmpty());  // false
    }
}

class LinkedListStack {

    // A node holds a value and a link to the node below it in the stack
    private static class Node {
        int data;
        Node next;   // ← a Node reference, NOT an int

        Node(int data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node head;   // head = top of the stack
    private int count;   // how many elements

    public LinkedListStack() {
        head = null;     // empty stack
        count = 0;
    }

    // push: create a new node and put it at the FRONT (new top)
    public void push(int elem) {
        Node node = new Node(elem);
        node.next = head;   // new node points to the old top
        head = node;        // new node becomes the top
        count++;
    }

    // pop: remove and return the front node (the top)
    public int pop() {
        if (isEmpty()) {
            System.out.println("Stack is empty!");
            return -1;
        }
        int removed = head.data;
        head = head.next;   // move top down to the next node
        count--;
        return removed;
    }

    // peek: look at the top without removing it
    public int peek() {
        if (isEmpty()) {
            System.out.println("Stack is empty!");
            return -1;
        }
        return head.data;
    }

    public boolean isEmpty() {
        return head == null;
    }

    public int size() {
        return count;
    }
}
