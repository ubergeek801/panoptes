package org.slaq.slaqworx.panoptes.asset;

import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * {@code MarketValued} is the interface for a holding that has a calculable market value, such as a
 * {@code Position} or an entire {@code Portfolio}.
 *
 * @author jeremy
 */
public interface MarketValued {
    /**
     * Obtains the market value of this holding.
     *
     * @param evaluationContext
     *            the {@code EvaluationContext} in which to perform the evaluation
     * @return the total market value of whatever comprises this holding
     */
    public double getMarketValue(EvaluationContext evaluationContext);
}
