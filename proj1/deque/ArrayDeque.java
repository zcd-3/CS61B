package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private int size;
    private T[] items;
    private int nextFirst;
    private int nextLast;

    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        nextFirst = 3;
        nextLast = 4;
    }

    @Override
    public int size() {
        return size;
    }

    private void resize(int s) {
        T[] newItems = (T[]) new Object[s];
        int firstIdx = (s - size) / 2;
        System.arraycopy(items, nextFirst + 1, newItems, firstIdx, size);
        items = newItems;
        nextFirst = firstIdx - 1;
        nextLast = firstIdx + size;
    }

    private void shrinkSize() {
        if (size * 4 < items.length) {
            if (size >= 4) {
                resize(size * 2);
            } else {
                resize(8);
            }
        }
    }

    @Override
    public void addFirst(T value) {
        items[nextFirst] = value;
        size++;
        nextFirst--;
        if (nextFirst == -1) {
            resize(size * 2);
        }
    }

    @Override
    public void addLast(T value) {
        items[nextLast] = value;
        size++;
        nextLast++;
        if (nextLast == items.length) {
            resize(size * 2);
        }
    }

    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        nextFirst++;
        size--;
        T res = items[nextFirst];
        shrinkSize();
        return res;
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        nextLast--;
        size--;
        T res = items[nextLast];
        shrinkSize();
        return res;
    }

    @Override
    public T get(int index) {
        if (index >= size) {
            return null;
        }
        return items[index + nextFirst + 1];
    }

    @Override
    public void printDeque() {
        for (int i = nextFirst + 1; i <= nextFirst + size; i++) {
            System.out.print(items[i] + " ");
        }
        System.out.println();
    }

    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int p;

        ArrayDequeIterator() {
            p = 0;
        }

        @Override
        public boolean hasNext() {
            return p < size;
        }

        @Override
        public T next() {
            T res = get(p);
            p++;
            return res;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Deque) {
            Deque<?> lld = (Deque<?>) o;
            if (size != lld.size()) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                if (!(lld.get(i).equals(get(i)))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
