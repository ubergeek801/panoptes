package org.slaq.slaqworx.panoptes.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Stream;

/**
 * {@code StreamUtil} provides utilities for manipulating {@code Stream}s.
 *
 * @author jeremy
 */
public class StreamUtil {
    /**
     * Splits the given {@code Stream} into partitions that are (estimated to be) no larger than the
     * specified maximum size.
     *
     * @param <T>
     *            the type of elements in the {@code Stream}
     * @param stream
     *            the {@code Stream} to be partitioned
     * @param maxPartitionSize
     *            the maximum desired size of an individual partition
     * @return a {@code List} of {@code Spliterator}s which comprise the individual partitions
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

    /**
     * Creates a new {@code StreamUtil}. Restricted to enforce class utility semantics.
     */
    private StreamUtil() {
        // nothing to do
    }
}
