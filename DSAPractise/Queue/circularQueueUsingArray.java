// CIRCULAR QUEUE using an array. Fixes the wasted-space problem of a plain
// array queue by wrapping indexes back to 0 using modulo (%).
public class circularQueueUsingArray {

    public static void main(String[] args) {
        CircularQueue q = new CircularQueue(3);   // capacity 3
        q.enqueue(1);
        q.enqueue(2);
        q.enqueue(3);
        q.enqueue(4);                                // "Full" — only 3 slots
        System.out.println("Front: " + q.front());   // 1
        System.out.println("Dequeued: " + q.dequeue()); // 1
        q.enqueue(4);                                // now fits — wraps to index 0
        System.out.println("Dequeued: " + q.dequeue()); // 2
        System.out.println("Dequeued: " + q.dequeue()); // 3
        System.out.println("Dequeued: " + q.dequeue()); // 4
        System.out.println("Empty? " + q.isEmpty());     // true
    }
}

class CircularQueue {
    private int[] arr;
    private int front;     // index of the front element
    private int rear;      // index of the last element
    private int count;     // current number of elements
    private int capacity;

    public CircularQueue(int size) {
        arr = new int[size];
        capacity = size;
        front = 0;
        rear = -1;
        count = 0;
    }

    public void enqueue(int x) {
        if (count == capacity) {
            System.out.println("Queue is full!");
            return;
        }
        rear = (rear + 1) % capacity;   // wrap back to 0 when reaching the end
        arr[rear] = x;
        count++;
    }

    public int dequeue() {
        if (isEmpty()) {
            System.out.println("Queue is empty!");
            return -1;
        }
        int value = arr[front];
        front = (front + 1) % capacity; // wrap back to 0 when reaching the end
        count--;
        return value;
    }

    public int front() {
        if (isEmpty()) return -1;
        return arr[front];
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public int size() {
        return count;
    }
}
