package org.slaq.slaqworx.panoptes.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Stream;

/**
 * Provides utilities for manipulating {@link Stream}s.
 *
 * @author jeremy
 */
public class StreamUtil {
  /** Creates a new {@link StreamUtil}. Restricted to enforce class utility semantics. */
  private StreamUtil() {
    // nothing to do
  }

  /**
   * Splits the given {@link Stream} into partitions that are (estimated to be) no larger than the
   * specified maximum size. While not enforced, this is likely to be useful only on {@code SIZED}
   * streams.
   *
   * @param <T> the type of elements in the {@link Stream}
   * @param stream the {@link Stream} to be partitioned
   * @param maxPartitionSize the maximum desired size of an individual partition
   * @return a {@link List} of {@link Spliterator}s which comprise the individual partitions
   */
  public static <T> List<Spliterator<T>> partition(Stream<T> stream, int maxPartitionSize) {
    maxPartitionSize = Math.max(1, maxPartitionSize);

    if (stream == null) {
      return Collections.emptyList();
    }

    ArrayList<Spliterator<T>> partitions = new ArrayList<>();

    Spliterator<T> initialPartition = stream.spliterator();
    partitions.add(initialPartition);

    // iterate through the (possibly expanding) list of partitions
    for (int i = 0; i < partitions.size(); i++) {
      Spliterator<T> partition = partitions.get(i);
      // if a partition is encountered that is larger than the desired size, split it until it
      // is sized appropriately, adding any new partitions to the end of the list
      while (partition.estimateSize() > maxPartitionSize) {
        Spliterator<T> newPartition = partition.trySplit();
        partitions.add(newPartition);
      }
    }

    return partitions;
  }
}
