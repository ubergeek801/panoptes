package org.slaq.slaqworx.panoptes.asset;

import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * The interface for a holding that has a calculable market value, such as a {@link Position} or an
 * entire {@link Portfolio}.
 *
 * @author jeremy
 */
public interface MarketValued {
  /**
   * Obtains the market value of this holding.
   *
   * @param evaluationContext
   *     the {@link EvaluationContext} in which to perform the evaluation
   *
   * @return the total market value of whatever comprises this holding
   */
  double getMarketValue(EvaluationContext evaluationContext);
}
