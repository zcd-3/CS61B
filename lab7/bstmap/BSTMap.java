package bstmap;

import afu.org.checkerframework.checker.oigj.qual.O;

import java.util.*;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V>{
    @Override
    public Iterator<K> iterator() {
        return new BSTMapIterator(root);
    }

    private class BSTMapIterator implements Iterator<K> {
        private final Deque<Node> stack = new ArrayDeque<>();

        BSTMapIterator(Node start) {
            pushLeft(start);
        }

        private void pushLeft(Node x) {
            while (x != null) {
                stack.push(x);
                x = x.left;
            }
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Node cur = stack.pop();
            pushLeft(cur.right);
            return cur.key;
        }
    }

    private class Node {
        private K key;
        private V val;
        private Node left, right;

        private Node(K key, V val) {
            this.key = key;
            this.val = val;
        }
    }

    private Node root;
    private int size;
    public BSTMap() {
        clear();
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        Node T = find(root, key);
        return T != null;
    }

    @Override
    public V get(K key) {
        Node T = find(root, key);
        if (T == null) {
            return null;
        } else {
            return T.val;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        root = put(root, key, value);
        size++;
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        collectKeys(root, keys);
        return keys;
    }

    private void collectKeys(Node T, Set<K> keys) {
        if (T == null) {
            return;
        }
        collectKeys(T.left, keys);
        keys.add(T.key);
        collectKeys(T.right, keys);
    }

    @Override
    public V remove(K key) {
        Removed r = new Removed();
        root = remove(root, key, r);
        if (r.found) {
            size--;
            return r.val;
        } else {
            return null;
        }
    }

    private class Removed {
        V val;
        boolean found = false;
    }

    private Node remove(Node T, K key, Removed r) {
        if (T == null) {
            return null;
        }
        int cmp = key.compareTo(T.key);
        if (cmp > 0) {
            T.right = remove(T.right, key, r);
        } else if (cmp < 0) {
            T.left = remove(T.left, key, r);
        } else {
            r.found = true;
            r.val = T.val;
            if (T.left == null) {
                return T.right;
            }
            if (T.right == null) {
                return T.left;
            }

            Node x = T;
            T = min(x.right);
            T.right = deleteMin(x.right);
            T.left = x.left;
        }
        return T;
    }

    private Node deleteMin(Node x) {
        if (x.left == null) {
            return x.right;
        }
        x.left = deleteMin(x.left);
        return x;
    }

    private Node min(Node x) {
        while (x.left != null) {
            x = x.left;
        }
        return x;
    }
    @Override
    public V remove(K key, V value) {
        if (!containsKey(key)) return null;
        V cur = get(key);
        if (cur.equals(value)) {
            return remove(key);
        } else {
            return null;
        }
    }

    public void printInOrder() {
        printInOrder(root);
    }

    private void printInOrder(Node T) {
        if (T == null) {
            return;
        }
        printInOrder(T.left);
        System.out.print(T.val + " ");
        printInOrder(T.right);
    }

    private Node find(Node T, K key) {
        if (T == null) {
            return null;
        }
        int cmp = key.compareTo(T.key);
        if (cmp > 0) {
            return find(T.right, key);
        } else if (cmp < 0) {
            return find(T.left, key);
        } else {
            return T;
        }
    }

    private Node put(Node T, K key, V val) {
        if (T == null) {
            return new Node(key, val);
        }
        int cmp = key.compareTo(T.key);
        if (cmp > 0) {
            T.right = put(T.right, key, val);
        } else if (cmp < 0) {
            T.left = put(T.left, key, val);
        } else {
            T.val = val;
        }
        return T;
    }
}
