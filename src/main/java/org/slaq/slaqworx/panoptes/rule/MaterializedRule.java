package org.slaq.slaqworx.panoptes.rule;

import java.util.ArrayList;
import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.asset.PositionSupplier;

/**
 * A MaterializedRule is a framework for implementation of the Rule interface. A
 * MaterializedPosition may be durable (e.g. sourced from a database/cache) or ephemeral (e.g.
 * supplied by a simulation mechanism or even a unit test).
 *
 * @author jeremy
 */
public abstract class MaterializedRule implements Rule {
    private static final long serialVersionUID = 1L;

    private final RuleKey key;
    private final String description;
    private final EvaluationGroupClassifier groupClassifier;
    private final ArrayList<GroupAggregator<?>> groupAggregators = new ArrayList<>();

    /**
     * Creates a new AbstractRule with the given key and description.
     *
     * @param key
     *            the unique key to assign to the Rule, or null to generate one
     * @param description
     *            the description of the Rule
     */
    protected MaterializedRule(RuleKey key, String description) {
        this(key, description, null);
    }

    /**
     * Creates a new AbstractRule with the given key, description and evaluation group classifier.
     *
     * @param key
     *            the unique key to assign to the Rule, or null to generate one
     * @param description
     *            the description of the Rule
     * @param groupClassifier
     *            the (possibly null) EvaluationGroupClassifier to use, which may also implement
     *            GroupAggregator
     */
    protected MaterializedRule(RuleKey key, String description,
            EvaluationGroupClassifier groupClassifier) {
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
     * Creates a new AbstractRule with a generated key and the given description.
     *
     * @param description
     *            the description of the Rule
     */
    protected MaterializedRule(String description) {
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
        return key.equals(other.getKey());
    }

    @Override
    public EvaluationResult evaluate(PositionSupplier portfolioPositions,
            PositionSupplier benchmarkPositions, EvaluationContext evaluationContext) {
        try {
            return eval(portfolioPositions, benchmarkPositions, evaluationContext);
        } catch (Exception e) {
            return new EvaluationResult(e);
        }
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Stream<GroupAggregator<?>> getGroupAggregators() {
        return groupAggregators.stream();
    }

    @Override
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
