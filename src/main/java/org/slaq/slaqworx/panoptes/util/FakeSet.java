package org.slaq.slaqworx.panoptes.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * {@code FakeSet} allows a {@code Collection} to masquerade as a {@code Set}. It is the user's
 * responsibility to ensure either that the provided collection has the necessary uniqueness
 * characteristics or that uniqueness is unimportant for the usage at hand.
 *
 * @author jeremy
 * @param <E>
 *            the type of element contained by this set
 */
public class FakeSet<E> implements Set<E> {
    private final Collection<E> collection;

    /**
     * Creates a new {@code FakeSet} that wraps the given {@code Collection}.
     *
     * @param collection
     *            the collection to wrap
     */
    public FakeSet(Collection<E> collection) {
        this.collection = collection;
    }

    @Override
    public boolean add(E e) {
        return collection.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return collection.addAll(c);
    }

    @Override
    public void clear() {
        collection.clear();
    }

    @Override
    public boolean contains(Object o) {
        return collection.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return collection.containsAll(c);
    }

    @Override
    public boolean isEmpty() {
        return collection.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return collection.iterator();
    }

    @Override
    public boolean remove(Object o) {
        return collection.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return collection.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return collection.retainAll(c);
    }

    @Override
    public int size() {
        return collection.size();
    }

    @Override
    public Object[] toArray() {
        return collection.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return collection.toArray(a);
    }
}
