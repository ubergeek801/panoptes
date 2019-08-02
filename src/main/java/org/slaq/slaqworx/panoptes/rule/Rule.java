package org.slaq.slaqworx.panoptes.rule;

import java.io.Serializable;
import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A Rule is a testable assertion against a set of Positions (typically supplied by a Portfolio). A
 * collection of Rules is typically used to assure compliance with the investment guidelines of a
 * customer account.
 * <p>
 * In general, the calculations and results of Rule evaluation can be grouped as specified by an
 * EvaluationGroupClassifier; for example, results may be grouped by Security currency by providing
 * an EvaluationGroupClassifier which maps each currency to a distinct group. The default, if an
 * EvaluationGroupClassifier is not specified, is to calculate for an entire Portfolio. Note that a
 * Rule only specifies how its results should be grouped; the actual tabulation is performed by an
 * evaluator such as PortfolioEvaluator.
 * <p>
 * Currently, a Rule may have at most one EvaluationGroupClassifier, which may also act as a
 * GroupAggregator.
 *
 * @author jeremy
 */
public interface Rule extends Keyed<RuleKey>, Serializable {
    /**
     * Evaluates the Rule on the given Portfolio, optionally relative to a given benchmark, subject
     * to the given evaluation context.
     *
     * @param portfolioPositions
     *            the portfolio Positions on which to evaluate the Rule
     * @param benchmarkPositions
     *            the (possibly null) benchmark Positions to evaluate relative to
     * @param evaluationContext
     *            the EvaluationContext under which to evaluate
     * @return the result of the Rule evaluation
     */
    public EvaluationResult evaluate(PositionSupplier portfolioPositions,
            PositionSupplier benchmarkPositions, EvaluationContext evaluationContext);

    /**
     * Obtains the description of this Rule.
     *
     * @return the Rule description
     */
    public String getDescription();

    /**
     * Obtains this Rule's GroupAggregators (if any) as a Stream.
     *
     * @return a (possibly empty) Stream of GroupAggregators
     */
    public Stream<GroupAggregator<?>> getGroupAggregators();

    /**
     * Obtain's this Rule's (possibly null) EvaluationGroupClassifier.
     *
     * @return the Rule's EvaluationGroupClassifier
     */
    public EvaluationGroupClassifier getGroupClassifier();
}
