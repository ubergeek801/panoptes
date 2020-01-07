package org.slaq.slaqworx.panoptes.asset;

import java.util.EnumSet;
import java.util.stream.Stream;

/**
 * A {@code HierarchicalPositionSupplier} is a {@code PositionSupplier} which provides hierarchies
 * of {@code Position}s, such as by employing "look-through" to {@code Position}s of constituent
 * {@code Portfolio}s, or by providing visibility to a {@code Position}'s individual
 * {@code TaxLot}s.
 *
 * @author jeremy
 */
public interface HierarchicalPositionSupplier extends PositionSupplier {
    public enum PositionHierarchyOption {
        LOOKTHROUGH, TAXLOT
    }

    /**
     * Obtains this {@code PositionSupplier}'s {@code Position}s as a (new) {@code Stream}, applying
     * the given hierarchy options.
     *
     * @param positionHierarchyOptions
     *            the (possibly empty) hierarchy options to be applied
     * @return a {@code Stream} of {@code Position}s
     */
    public Stream<Position> getPositions(EnumSet<PositionHierarchyOption> positionHierarchyOptions);
}
