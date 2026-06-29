// QUEUE using a simple array (FIFO — add at rear, remove from front).
// NOTE: this basic version wastes space at the front as `front` moves forward.
// The circular version (circularQueueUsingArray.java) fixes that.
public class queueUsingArray {

    public static void main(String[] args) {
        ArrayQueue q = new ArrayQueue(5);
        q.enqueue(1);
        q.enqueue(2);
        q.enqueue(3);
        System.out.println("Front: " + q.front());     // 1
        System.out.println("Dequeued: " + q.dequeue()); // 1 (first in, first out)
        System.out.println("Dequeued: " + q.dequeue()); // 2
        System.out.println("Size: " + q.size());         // 1
        System.out.println("Empty? " + q.isEmpty());     // false
    }
}

class ArrayQueue {
    private int[] arr;
    private int front;   // index of the front element
    private int rear;    // index of the last element
    private int count;
    private int capacity;

    public ArrayQueue(int size) {
        arr = new int[size];
        capacity = size;
        front = 0;
        rear = -1;     // -1 means nothing added yet
        count = 0;
    }

    public void enqueue(int x) {
        if (count == capacity) {           // full
            System.out.println("Queue is full!");
            return;
        }
        rear++;           // move rear forward
        arr[rear] = x;    // place value at the back
        count++;
    }

    public int dequeue() {
        if (isEmpty()) {
            System.out.println("Queue is empty!");
            return -1;
        }
        int value = arr[front];   // grab from the front
        front++;                  // move front forward
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
