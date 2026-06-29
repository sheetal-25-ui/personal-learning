// SINGLY LINKED LIST — a chain of nodes (data + next). Everything starts from head.
// Includes the 3 MUST-KNOW interview algorithms: reverse, find middle, detect cycle.
public class singlyLinkedList {

    public static void main(String[] args) {
        MyLinkedList list = new MyLinkedList();
        list.insertEnd(10);
        list.insertEnd(20);
        list.insertFront(5);
        list.print();              // 5 -> 10 -> 20 -> null

        list.delete(10);
        list.print();              // 5 -> 20 -> null

        list.insertEnd(30);
        list.insertEnd(40);
        list.print();              // 5 -> 20 -> 30 -> 40 -> null

        System.out.println("Middle: " + list.findMiddle().data);  // 30
        System.out.println("Has cycle? " + list.hasCycle());      // false

        list.reverse();
        list.print();              // 40 -> 30 -> 20 -> 5 -> null
    }
}

class MyLinkedList {

    static class Node {
        int data;
        Node next;
        Node(int data) { this.data = data; }
    }

    Node head;   // first node — hold the whole list through this

    // 1. Add at the END — O(n) (walk to last node)
    public void insertEnd(int x) {
        Node node = new Node(x);
        if (head == null) { head = node; return; }
        Node temp = head;
        while (temp.next != null) temp = temp.next;  // walk to the last node
        temp.next = node;
    }

    // 2. Add at the FRONT — O(1) (fast)
    public void insertFront(int x) {
        Node node = new Node(x);
        node.next = head;   // new node points to old head
        head = node;        // new node becomes head
    }

    // 3. DELETE a value — O(n)
    public void delete(int x) {
        if (head == null) return;
        if (head.data == x) { head = head.next; return; }  // delete head
        Node temp = head;
        while (temp.next != null && temp.next.data != x) temp = temp.next;
        if (temp.next != null) temp.next = temp.next.next; // skip over the node
    }

    // 4. PRINT
    public void print() {
        Node temp = head;
        while (temp != null) {
            System.out.print(temp.data + " -> ");
            temp = temp.next;
        }
        System.out.println("null");
    }

    // ---------- MUST-KNOW INTERVIEW ALGORITHMS ----------

    // A) REVERSE — 3 pointers (prev, curr, next). Flip each arrow. O(n).
    public void reverse() {
        Node prev = null;
        Node curr = head;
        while (curr != null) {
            Node next = curr.next;  // remember next (or we lose the rest)
            curr.next = prev;       // flip the arrow backwards
            prev = curr;            // move prev forward
            curr = next;            // move curr forward
        }
        head = prev;   // prev is the new head
    }

    // B) FIND MIDDLE — slow/fast pointer. Slow moves 1, fast moves 2. O(n).
    public Node findMiddle() {
        Node slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;        // 1 step
            fast = fast.next.next;   // 2 steps
        }
        return slow;   // when fast hits the end, slow is at the middle
    }

    // C) DETECT CYCLE — Floyd's. If slow and fast ever meet, there's a loop. O(n).
    public boolean hasCycle() {
        Node slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) return true;  // they met = loop exists
        }
        return false;
    }
}
