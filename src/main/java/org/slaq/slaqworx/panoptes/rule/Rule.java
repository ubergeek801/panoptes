package org.slaq.slaqworx.panoptes.rule;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.PositionSupplier;

/**
 * A Rule is a testable assertion against a set of Positions (typically supplied by a Portfolio). A
 * collection of Rules is typically used to assure compliance with the investment guidelines of a
 * customer account.
 * <p>
 * In general, the calculations and results of Rule evaluation can be grouped as specified by an
 * EvaluationGroupClassifier; for example, results may be grouped by Security currency by providing
 * an EvaluationGroupClassifier which maps each currency to a distinct group. The default, if an
 * EvaluationGroupClassifier is not specified, is to calculate for an entire Portfolio. Note that a
 * Rule only specifies how its results should be grouped; the actual tabulation is performed by
 * RuleEvaluator.
 *
 * @author jeremy
 */
public abstract class Rule {
    private static final Logger LOG = LoggerFactory.getLogger(Rule.class);

    private final String id;
    private final String description;
    private final EvaluationGroupClassifier groupClassifier;

    /**
     * Creates a new Rule with a generated ID and the given description.
     *
     * @param description
     *            the description of the Rule
     */
    protected Rule(String description) {
        this(null, description);
    }

    /**
     * Creates a new Rule with the given ID and description.
     *
     * @param id
     *            the unique ID to assign to the Rule, or null to generate one
     * @param description
     *            the description of the Rule
     */
    protected Rule(String id, String description) {
        this(id, description, null);
    }

    /**
     * Creates a new Rule with the given ID, description and evaluation group.
     *
     * @param id
     *            the unique ID to assign to the Rule, or null to generate one
     * @param description
     *            the description of the Rule
     * @param EvaluationGroupClassifier
     *            the (possibly null) EvaluationGroupClassifier to use
     */
    protected Rule(String id, String description, EvaluationGroupClassifier groupClassifier) {
        this.id = (id == null ? UUID.randomUUID().toString() : id);
        this.description = description;
        this.groupClassifier =
                (groupClassifier == null ? EvaluationGroupClassifier.defaultClassifier()
                        : groupClassifier);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Rule other = (Rule)obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    /**
     * Evaluates the Rule on the given portfolio Positions, optionally relative to a given
     * benchmark. The public evaluate() methods ultimately delegate to this one.
     *
     * @param portfolioPositions
     *            the portfolio Positions on which to evaluate the Rule
     * @param benchmarkPositions
     *            the (possibly null) benchmark Positions to evaluate relative to
     * @return true if the Rule passes, false if it fails
     */
    protected abstract boolean eval(PositionSupplier portfolioPositions,
            PositionSupplier benchmarkPositions);

    /**
     * Evaluates the Rule on the given Portfolio, optionally relative to a given benchmark.
     *
     * @param portfolioPositions
     *            the portfolio Positions on which to evaluate the Rule
     * @param benchmarkPositions
     *            the (possibly null) benchmark Positions to evaluate relative to
     * @return true if the Rule passes, false if it fails
     */
    public boolean evaluate(PositionSupplier portfolioPositions,
            PositionSupplier benchmarkPositions) {
        try {
            return eval(portfolioPositions, benchmarkPositions);
        } catch (Exception e) {
            // for now, any unexpected exception results in failure
            LOG.error("evaluation failed for rule " + id + " (\"" + description + "\")", e);
            return false;
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
    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }
}
