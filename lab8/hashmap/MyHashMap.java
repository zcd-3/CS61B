package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    private int size;
    private int len;
    private final double loadFactor;


    /** Constructors */
    public MyHashMap() {
        len = 0;
        size = 16;
        loadFactor = 0.75;
        buckets = createTable(size);
    }

    public MyHashMap(int initialSize) {
        len = 0;
        size = initialSize;
        loadFactor = 0.75;
        buckets = createTable(size);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        len = 0;
        size = initialSize;
        loadFactor = maxLoad;
        buckets = createTable(size);
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new ArrayList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }

    @Override
    public void clear() {
        len = 0;
        size = 16;
        buckets = createTable(size);
    }

    @Override
    public boolean containsKey(K key) {
        return find(key) != null;
    }

    @Override
    public V get(K key) {
        Node n = find(key);
        if (n == null) {
            return null;
        } else {
            return n.value;
        }
    }

    @Override
    public int size() { return len; }

    @Override
    public void put(K key, V value) {
        if (loadFactor < (double) (len + 1) / size) { resize(); }
        Collection<Node> bucket = getBucket(key);
        if (bucket == null) {
            buckets[getIndex(key)] = createBucket();
        }
        Node n = find(key);
        if (n == null) {
            buckets[getIndex(key)].add(createNode(key, value));
            len++;
        } else {
            n.value = value;
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        for (Collection<Node> bucket : buckets) {
            if (bucket == null) { continue; }
            for (Node n : bucket) {
                keys.add(n.key);
            }
        }
        return keys;
    }

    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }

    @Override
    public V remove(K key) {
        Collection<Node> bucket = getBucket(key);
        if (bucket == null) {
            return null;
        }
        Node n = find(key);
        if (n == null) {
            return null;
        } else {
            bucket.remove(n);
            len--;
            return n.value;
        }
    }

    @Override
    public V remove(K key, V value) {
        if (get(key).equals(value)) {
            return remove(key);
        } else {
            return null;
        }
    }

    private int getIndex(K key) {
        return Math.floorMod(key.hashCode(), size);
    }

    private Collection<Node> getBucket(K key) {
        return buckets[getIndex(key)];
    }

    private Node find(K key) {
        Collection<Node> bucket = getBucket(key);
        if (bucket != null) {
            for (Node n : bucket) {
                if (key.equals(n.key)) {
                    return n;
                }
            }
        }
        return null;
    }

    private void resize() {
        int newSize = size * 2;
        Collection<Node>[] newBuckets = createTable(newSize);
        for (Collection<Node> bucket : buckets) {
            if (bucket == null) { continue; }
            for (Node n : bucket) {
                int newIndex = Math.floorMod(n.key.hashCode(), newSize);
                if (newBuckets[newIndex] == null) {
                    newBuckets[newIndex] = createBucket();
                }
                newBuckets[newIndex].add(n);
            }
        }
        buckets = newBuckets;
        size = newSize;
     }
}
