import java.util.*;

public class SlidingWindow<E> extends AbstractCollection<E> {

    private final E[] window;
    private int insertIndex = 0;

    public SlidingWindow(final E[] items) {
        window = items;
    }

    @Override
    public boolean add(final E item) {
        window[insertIndex++] = item;
        if (insertIndex >= window.length) {
            insertIndex = 0;
        }
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
        return window.length;
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
