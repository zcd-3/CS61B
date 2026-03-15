package deque;

import java.util.Iterator;

public class LinkedListDeque<T> {
    class Node<T> {
        public T value;
        public Node<T> last;
        public Node<T> next;
        public Node(T v) {
            value = v;
        }
    }

    private Node<T> sentinel;
    private int size;

    public LinkedListDeque() {
        sentinel = new Node<>(null);
        sentinel.last = sentinel;
        sentinel.next = sentinel;
        size = 0;
    }

    public void addFirst(T s) {
        Node<T> first = sentinel.next;
        Node<T> newFirst = new Node<>(s);
        sentinel.next = newFirst;
        first.last = newFirst;
        newFirst.last = sentinel;
        newFirst.next = first;
        size++;
    }

    public void addLast(T s) {
        Node<T> last = sentinel.last;
        Node<T> newLast = new Node<>(s);
        sentinel.last = newLast;
        last.next = newLast;
        newLast.next = sentinel;
        newLast.last = last;
        size++;
    }

    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        Node<T> first = sentinel.next;
        Node<T> newFirst = first.next;
        T res = first.value;
        sentinel.next = newFirst;
        newFirst.last = sentinel;
        size--;
        return res;
    }

    public T removeLast() {
        if (size == 0) {
            return null;
        }
        Node<T> last = sentinel.last;
        Node<T> newLast = last.last;
        T res = last.value;
        sentinel.last = newLast;
        newLast.next = sentinel;
        size--;
        return res;
    }

    public int size() {return size;}

    public boolean isEmpty() {
        if (size == 0) {
            return true;
        }
        return false;
    }

    public T get(int index) {
        if (size == 0) {
            return null;
        }
        Node<T> cur = sentinel;
        for (int i = 0; i < index + 1; i++) {
            cur = cur.next;
        }
        return cur.value;
    }

    public T getRecursive(int index) {
        return getRecursiveHelper(sentinel.next, index);
    }

    private T getRecursiveHelper(Node<T> cur, int index) {
        if (index == 0) {
            return cur.value;
        }
        return getRecursiveHelper(cur.next, index - 1);
    }

    public void printDeque() {
        Node<T> cur = sentinel;
        for (int i = 0; i < size; i++) {
            cur = cur.next;
            System.out.print(cur.value + " ");
        }
        System.out.println();
    }

    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private Node<T> p;

        LinkedListDequeIterator() {
            p = sentinel.next;
        }

        @Override
        public boolean hasNext() {
            return p != sentinel;
        }

        @Override
        public T next() {
            T value = p.value;
            p = p.next;
            return value;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LinkedListDeque lld) {
            if (size != lld.size()) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                if (lld.get(i) != get(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
