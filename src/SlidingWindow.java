import java.util.*;

public class SlidingWindow<E> extends AbstractCollection<E> {

    private final E[] window;
    private int insertIndex = 0;
    private boolean isFull = false;


    public SlidingWindow(final E[] items) {
        window = items;
    }

    @Override
    public boolean add(final E item) {
        window[insertIndex++] = item;
        if (insertIndex >= window.length) {
            insertIndex = 0;
            isFull = true;
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
        if (isFull) {
            return window.clone();
        } else {
            return Arrays.copyOf(window, insertIndex);
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new WindowIterator();
    }

    @Override
    public int size() {
        return isFull ? window.length : insertIndex;
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
