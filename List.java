/** A linked list of character data objects.
 *  (Actually, a list of Node objects, each holding a reference to a character data object.
 *  However, users of this class are not aware of the Node objects. As far as they are concerned,
 *  the class represents a list of CharData objects. Likwise, the API of the class does not
 *  mention the existence of the Node objects). */
public class List {

    private Node first;
    private int size;

    public List() {
        first = null;
        size = 0;
    }

    public int getSize() {
        return size;
    }

    public CharData getFirst() {
        if (first == null) throw new IllegalStateException("List is empty");
        return first.cp;
    }

    // Adds to the beginning (required)
    public void addFirst(char chr) {
        first = new Node(new CharData(chr), first);
        size++;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        Node current = first;
        while (current != null) {
            sb.append(current.cp);
            if (current.next != null) sb.append(" ");
            current = current.next;
        }
        sb.append(")");
        return sb.toString();
    }

    public int indexOf(char chr) {
        Node current = first;
        int i = 0;
        while (current != null) {
            if (current.cp.chr == chr) return i;
            current = current.next;
            i++;
        }
        return -1;
    }

    // If exists increment, else addFirst (required)
    public void update(char chr) {
        Node current = first;
        while (current != null) {
            if (current.cp.chr == chr) {
                current.cp.count++;
                return;
            }
            current = current.next;
        }
        addFirst(chr);
    }

    public boolean remove(char chr) {
        if (first == null) return false;

        if (first.cp.chr == chr) {
            first = first.next;
            size--;
            return true;
        }

        Node current = first;
        while (current.next != null) {
            if (current.next.cp.chr == chr) {
                current.next = current.next.next;
                size--;
                return true;
            }
            current = current.next;
        }
        return false;
    }

    public CharData get(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();

        Node current = first;
        for (int i = 0; i < index; i++) current = current.next;
        return current.cp;
    }

    public CharData[] toArray() {
        CharData[] arr = new CharData[size];
        Node current = first;
        int i = 0;
        while (current != null) {
            arr[i++] = current.cp;
            current = current.next;
        }
        return arr;
    }

    public ListIterator listIterator(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
        Node current = first;
        for (int i = 0; i < index; i++) current = current.next;
        return new ListIterator(current);
    }
}
