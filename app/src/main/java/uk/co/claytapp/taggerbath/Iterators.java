package uk.co.claytapp.taggerbath;

import java.util.Iterator;

public class Iterators<T> implements Iterator<T> {

    private Iterator<T> current;
    private Iterator<Iterator<T>> cursor;

    public Iterators(Iterable<Iterator<T>> iterators) {
        if (iterators == null) {
            throw new IllegalArgumentException("iterators is null");
        }
        this.cursor = iterators.iterator();
    }

    private Iterator<T> findNext() {
        while (cursor.hasNext()) {
            current = cursor.next();
            if (current.hasNext()) return current;
        }
        return null;
    }

    @Override
    public boolean hasNext() {
        if (current == null || !current.hasNext()) {
            current = findNext();
        }
        return (current != null && current.hasNext());
    }

    @Override
    public T next() {
        return current.next();
    }

    @Override
    public void remove() {
        if (current != null) {
            current.remove();
        }
    }
}
