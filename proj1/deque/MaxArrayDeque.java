package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> c) {
        super();
        comparator = c;
    }

    public T max() {
        return max(comparator);
    }

    public T max(Comparator<T> c) {
        if (isEmpty() || c == null) {
            return null;
        }
        T res = get(0);
        for (int i = 1; i < size(); i++) {
            T cur = get(i);
            if (c.compare(cur, res) > 0) {
                res = cur;
            }
        }
        return res;
    }
}
