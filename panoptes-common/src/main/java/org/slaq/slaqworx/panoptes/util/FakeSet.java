package org.slaq.slaqworx.panoptes.util;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A {@link Set} implementation which allows a {@link Collection} to masquerade as a {@link Set}
 * (e.g. to avoid copying data in order to create a new {@link Set} from a {@link List}. It is the
 * user's responsibility to ensure either that the provided collection has the necessary uniqueness
 * characteristics, or that uniqueness is unimportant for the usage at hand.
 *
 * @param <E> the type of element contained by this set
 * @author jeremy
 */
public class FakeSet<E> extends AbstractCollection<E> implements Set<E> {
  private final Collection<E> collection;

  /**
   * Creates a new {@link FakeSet} that wraps the given {@link Collection}.
   *
   * @param collection the collection to wrap
   */
  public FakeSet(Collection<E> collection) {
    this.collection = collection;
  }

  @Override
  public Iterator<E> iterator() {
    return collection.iterator();
  }

  @Override
  public int size() {
    return collection.size();
  }
}
