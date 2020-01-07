package org.slaq.slaqworx.panoptes.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Spliterator;

import org.junit.jupiter.api.Test;

/**
 * {@code StreamUtilTest} tests the functionality of {@code StreamUtil}.
 *
 * @author jeremy
 */
public class StreamUtilTest {
    /**
     * Tests that {@code partition()} behaves as expected.
     */
    @Test
    public void testPartition() {
        List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<Spliterator<Integer>> partitionedList = StreamUtil.partition(list.stream(), 2);

        // partitioning is based on size estimates, so we can't be too picky about how many
        // partitions are returned, but it's not unreasonable to expect at least 3
        assertTrue(partitionedList.size() >= 3, "expected a greater number of partitions");

        HashSet<Integer> encounteredElements = new HashSet<>();
        // whatever the partitioning, we should expect all the original elements to be there
        partitionedList.forEach(p -> p.forEachRemaining(i -> encounteredElements.add(i)));
        assertEquals(list.size(), encounteredElements.size(),
                "should have same number of encountered elements as original");

        // test some pathological inputs

        partitionedList = StreamUtil.partition(null, 1);
        assertNotNull(partitionedList, "partition() should not return null");
        assertTrue(partitionedList.isEmpty(), "null input should produce empty output");

        partitionedList = StreamUtil.partition(list.stream(), 0);
        assertEquals(list.size(), partitionedList.size(),
                "expected size <= 1 should result in full partitioning");
    }
}
