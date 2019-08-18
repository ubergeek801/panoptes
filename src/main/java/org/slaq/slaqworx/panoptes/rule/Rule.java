package org.slaq.slaqworx.panoptes.rule;

import java.util.ArrayList;
import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A {@code Rule} is a testable assertion against a set of {@code Position}s (typically supplied by
 * a {@code Portfolio}). A collection of {@code Rule}s is typically used to assure compliance with
 * the investment guidelines of a customer account. A {@code Rule} may be durable (e.g. sourced from
 * a database/cache) or ephemeral (e.g. supplied by a simulation mechanism or even a unit test).
 * <p>
 * In general, the calculations and results of {@code Rule} evaluation can be grouped as specified
 * by an {@code EvaluationGroupClassifier}; for example, results may be grouped by {@code Security}
 * currency by providing an {@code EvaluationGroupClassifier} which maps each currency to a distinct
 * group. The default, if an {@code EvaluationGroupClassifier} is not specified, is to calculate for
 * an entire {@code Portfolio}. Note that a {@code Rule} only specifies how its results should be
 * grouped; the actual tabulation is performed by an evaluator such as {@code PortfolioEvaluator}.
 * <p>
 * Currently, a {@code Rule} may have at most one {@code EvaluationGroupClassifier}, which may also
 * act as a {@code GroupAggregator}.
 *
 * @author jeremy
 */
public abstract class Rule implements Keyed<RuleKey> {
    private final RuleKey key;
    private final String description;
    private final EvaluationGroupClassifier groupClassifier;
    private final ArrayList<GroupAggregator<?>> groupAggregators = new ArrayList<>();

    /**
     * Creates a new {@code Rule} with the given key and description.
     *
     * @param key
     *            the unique key to assign to the {@code Rule}, or {@code null} to generate one
     * @param description
     *            the description of the {@code Rule}
     */
    protected Rule(RuleKey key, String description) {
        this(key, description, null);
    }

    /**
     * Creates a new {@code Rule} with the given key, description and evaluation group classifier.
     *
     * @param key
     *            the unique key to assign to the {@code Rule}, or {@code null} to generate one
     * @param description
     *            the description of the {@code Rule}
     * @param groupClassifier
     *            the (possibly {@code null}) {@code EvaluationGroupClassifier} to use, which may
     *            also implement {@code GroupAggregator}
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
     * Creates a new {@code Rule} with a generated key and the given description.
     *
     * @param description
     *            the description of the {@code Rule}
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
        if (!(obj instanceof ConfigurableRule)) {
            return false;
        }
        ConfigurableRule other = (ConfigurableRule)obj;
        return key.equals(other.getKey());
    }

    /**
     * Evaluates the {@code Rule} on the given {@code Portfolio}, optionally relative to a given
     * benchmark, subject to the given evaluation context.
     *
     * @param portfolioPositions
     *            the {@code Portfolio} {@code Position}s on which to evaluate the {@code
     *            Rule}
     * @param benchmarkPositions
     *            the (possibly {@code null}) benchmark {@code Position}s to evaluate relative to
     * @param evaluationContext
     *            the {@code EvaluationContext} under which to evaluate
     * @return the result of the {@code Rule} evaluation
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
     * Obtains the description of this {@code Rule}.
     *
     * @return the {@code Rule} description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Obtains this {@code Rule}'s {@code GroupAggregator}s (if any) as a {@code Stream}.
     *
     * @return a (possibly empty) {@code Stream} of {@code GroupAggregator}s
     */
    public Stream<GroupAggregator<?>> getGroupAggregators() {
        return groupAggregators.stream();
    }

    /**
     * Obtain's this {@code Rule}'s (possibly {@code null}) {@code EvaluationGroupClassifier}.
     *
     * @return the {@code Rule}'s {@code EvaluationGroupClassifier}
     */
    public EvaluationGroupClassifier getGroupClassifier() {
        return groupClassifier;
    }

    @Override
    public RuleKey getKey() {
        return key;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    /**
     * Evaluates the {@code Rule} on the given {@code Portfolio} {@code Position}s, optionally
     * relative to a given benchmark. The public {@code evaluate()} methods ultimately delegate to
     * this one.
     *
     * @param portfolioPositions
     *            the {@code Portfolio} {@code Position}s on which to evaluate the {@code Rule}
     * @param benchmarkPositions
     *            the (possibly {@code null}) benchmark {@code Position}s to evaluate relative to
     * @param evaluationContext
     *            the {@code EvaluationContext} under which to evaluate
     * @return the result of the {@code Rule} evaluation
     */
    protected abstract EvaluationResult eval(PositionSupplier portfolioPositions,
            PositionSupplier benchmarkPositions, EvaluationContext evaluationContext);
}
