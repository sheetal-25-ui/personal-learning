// QUEUE using a linked list (FIFO). Two pointers: front (remove) and rear (add).
// Advantage over array: grows freely, no fixed capacity, no wasted space.
public class queueUsingLinkedList {

    public static void main(String[] args) {
        LinkedListQueue q = new LinkedListQueue();
        q.enqueue(1);
        q.enqueue(2);
        q.enqueue(3);
        System.out.println("Front: " + q.front());     // 1
        System.out.println("Dequeued: " + q.dequeue()); // 1
        System.out.println("Dequeued: " + q.dequeue()); // 2
        System.out.println("Size: " + q.size());         // 1
        System.out.println("Empty? " + q.isEmpty());     // false
    }
}

class LinkedListQueue {

    private static class Node {
        int data;
        Node next;
        Node(int data) { this.data = data; }
    }

    private Node front;   // remove from here
    private Node rear;    // add here
    private int count;

    public void enqueue(int x) {
        Node node = new Node(x);
        if (rear == null) {       // queue was empty
            front = rear = node;
        } else {
            rear.next = node;     // attach after the old rear
            rear = node;          // move rear forward
        }
        count++;
    }

    public int dequeue() {
        if (isEmpty()) {
            System.out.println("Queue is empty!");
            return -1;
        }
        int value = front.data;
        front = front.next;             // move front forward
        if (front == null) rear = null; // queue became empty — reset rear too
        count--;
        return value;
    }

    public int front() {
        if (isEmpty()) return -1;
        return front.data;
    }

    public boolean isEmpty() {
        return front == null;
    }

    public int size() {
        return count;
    }
}
