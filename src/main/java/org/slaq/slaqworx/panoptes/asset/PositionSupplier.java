package org.slaq.slaqworx.panoptes.asset;

import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.PositionEvaluationContext;

/**
 * A {@code PositionSupplier} supplies {@code Position}s. An implementor might be a customer
 * {@code Portfolio} or a "raw" set of {@code Position}s. A {@code PositionSupplier} may provide
 * access to a related {@code Portfolio} (which may be the supplier itself), but note that the
 * {@code PositionSupplier}'s members may not be the same as the related {@code Portfolio}'s (the
 * supplier may, for example, provide access to a filtered set).
 *
 * @author jeremy
 */
public interface PositionSupplier {
    /**
     * "Concatenates" the given {@code PositionSupplier}s into a single logical
     * {@code PositionSupplier}.
     *
     * @param suppliers
     *            the {@code PositionSupplier}s to be concatenated
     * @return a {@code PositionSupplier} representing the concatenation of the suppliers
     */
    public static PositionSupplier concat(PositionSupplier... suppliers) {
        return new CompoundPositionSupplier(suppliers);
    }

    /**
     * Obtains the key of this {@code PositionSupplier}'s related {@code Portfolio}, if any.
     *
     * @return the related {@code Portfolio}, or {@code null} if none is associated
     */
    public PortfolioKey getPortfolioKey();

    /**
     * Obtains this {@code PositionSupplier}'s {@code Position}s as a (new) {@code Stream}.
     *
     * @return a {@code Stream} of {@code Position}s
     */
    public Stream<Position> getPositions();

    /**
     * Given an {@code EvaluationContext}, obtains this {@code PositionSupplier}'s {@code Position}s
     * as a (new) {@code Stream} of {@code PositionEvaluationContext}s.
     *
     * @return a {@code Stream} of {@code PositionEvaluationContext}s
     */
    public default Stream<PositionEvaluationContext>
            getPositionsWithContext(EvaluationContext evaluationContext) {
        return getPositions().map(p -> new PositionEvaluationContext(p, evaluationContext));
    }

    /**
     * Obtains the sum of the {@code Position} amounts of this {@code PositionSupplier}.
     *
     * @return the sum of {@code Position} amounts
     */
    public double getTotalMarketValue();

    /**
     * Obtains the number of {@code Position}s in this {@code PositionSupplier}.
     *
     * @return the number of {@code Position}s
     */
    public int size();
}
