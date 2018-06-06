import java.lang.reflect.Array;
import java.util.*;

public class SlidingWindow<E> extends AbstractCollection<E> {

    private final E[] window;
    private final Class<? extends E> c;

    private boolean isFull;
    private int insertIndex;

    @SuppressWarnings("unchecked")
    public SlidingWindow(Class<? extends E> c, final int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Must specify a positive capacity");
        }
        this.c = c;
        window = (E[]) Array.newInstance(c, capacity);
        insertIndex = 0;
        isFull = false;
    }

    @Override
    public boolean add(final E item) {
        if (insertIndex >= window.length) {
            insertIndex = 0;
            isFull = true;
        }

        window[insertIndex++] = item;

        return true;
    }

    @Override
    public boolean addAll(final Collection<? extends E> items) {
        for (E item : items) {
            add(item);
        }
        return  true;
    }

    public E[] getDocuments() {
        return window;
    }

    @Override
    public Iterator<E> iterator() {
        return new WindowIterator();
    }

    @Override
    public int size() {
        return isFull ? window.length : insertIndex;
    }

    public boolean isFull() {
        return isFull;
    }

    private class WindowIterator implements Iterator<E> {

        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < size();
        }

        @Override
        public E next() {
            return window[index++];
        }
    }
}
