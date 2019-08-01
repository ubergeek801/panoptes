package org.slaq.slaqworx.panoptes.rule;

import java.io.Serializable;
import java.util.ArrayList;
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
public abstract class Rule implements Keyed<RuleKey>, Serializable {
    private static final long serialVersionUID = 1L;

    private final RuleKey key;
    private final String description;
    private final EvaluationGroupClassifier groupClassifier;
    private final ArrayList<GroupAggregator<?>> groupAggregators = new ArrayList<>();

    /**
     * Creates a new Rule with the given key and description.
     *
     * @param key
     *            the unique key to assign to the Rule, or null to generate one
     * @param description
     *            the description of the Rule
     */
    protected Rule(RuleKey key, String description) {
        this(key, description, null);
    }

    /**
     * Creates a new Rule with the given key, description and evaluation group classifier.
     *
     * @param key
     *            the unique key to assign to the Rule, or null to generate one
     * @param description
     *            the description of the Rule
     * @param groupClassifier
     *            the (possibly null) EvaluationGroupClassifier to use, which may also implement
     *            GroupAggregator
     */
    protected Rule(RuleKey key, String description, EvaluationGroupClassifier groupClassifier) {
        this.key = (key == null ? new RuleKey(null) : key);
        this.description = description;
        if (groupClassifier == null) {
            this.groupClassifier = EvaluationGroupClassifier.defaultClassifier();
        } else {
            this.groupClassifier = groupClassifier;
            if (groupClassifier instanceof GroupAggregator) {
                groupAggregators.add((GroupAggregator<?>)groupClassifier);
            }
        }
    }

    /**
     * Creates a new Rule with a generated key and the given description.
     *
     * @param description
     *            the description of the Rule
     */
    protected Rule(String description) {
        this(null, description);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Rule)) {
            return false;
        }
        Rule other = (Rule)obj;
        return key.equals(other.key);
    }

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
            PositionSupplier benchmarkPositions, EvaluationContext evaluationContext) {
        try {
            return eval(portfolioPositions, benchmarkPositions, evaluationContext);
        } catch (Exception e) {
            return new EvaluationResult(e);
        }
    }

    /**
     * Obtains the description of this Rule.
     *
     * @return the Rule description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Obtains this Rule's GroupAggregators (if any) as a Stream.
     *
     * @return a (possibly empty) Stream of GroupAggregators
     */
    public Stream<GroupAggregator<?>> getGroupAggregators() {
        return groupAggregators.stream();
    }

    /**
     * Obtain's this Rule's (possibly null) EvaluationGroupClassifier.
     *
     * @return the Rule's EvaluationGroupClassifier
     */
    public EvaluationGroupClassifier getGroupClassifier() {
        return groupClassifier;
    }

    /**
     * Obtains this Rule's unique ID.
     *
     * @return the Rule ID
     */
    @Override
    public RuleKey getKey() {
        return key;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    /**
     * Evaluates the Rule on the given portfolio Positions, optionally relative to a given
     * benchmark. The public evaluate() methods ultimately delegate to this one.
     *
     * @param portfolioPositions
     *            the portfolio Positions on which to evaluate the Rule
     * @param benchmarkPositions
     *            the (possibly null) benchmark Positions to evaluate relative to
     * @param evaluationContext
     *            the EvaluationContext under which to evaluate
     * @return the result of the Rule evaluation
     */
    protected abstract EvaluationResult eval(PositionSupplier portfolioPositions,
            PositionSupplier benchmarkPositions, EvaluationContext evaluationContext);
}
