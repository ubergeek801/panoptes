package org.slaq.slaqworx.panoptes.rule;

import java.util.UUID;

import org.slaq.slaqworx.panoptes.asset.Portfolio;

/**
 * A Rule is a testable assertion against a set of Positions (typically supplied by a Portfolio). A
 * collection of Rules is typically used to assure compliance with the investment guidelines of a
 * customer account.
 *
 * @author jeremy
 */
public abstract class Rule {
    private final String id;
    private final String description;
    private final Double lowerLimit;
    private final Double upperLimit;

    /**
     * Creates a new Rule with a generated ID and the given description.
     *
     * @param description the description of the Rule
     */
    public Rule(String description) {
        this(null, description, null, null);
    }

    /**
     * Creates a new Rule with a generated ID and the given description, lower and upper limit.
     * <p>
     * FIXME refactor this into a ConcentrationRule/ValueRule parent
     *
     * @param description the description of the Rule
     * @param lowerLimit  the lower limit of acceptable values
     * @param upperLimit  the upper limit of acceptable values
     */
    public Rule(String description, Double lowerLimit, Double upperLimit) {
        this(null, description, lowerLimit, upperLimit);
    }

    /**
     * Creates a new Rule with the given ID, description, lower and upper limit.
     * <p>
     * FIXME refactor this into a ConcentrationRule/ValueRule parent
     *
     * @param description the description of the Rule
     * @param lowerLimit  the lower limit of acceptable values
     * @param upperLimit  the upper limit of acceptable values
     */
    public Rule(String id, String description, Double lowerLimit, Double upperLimit) {
        this.id = (id == null ? UUID.randomUUID().toString() : id);
        this.description = description;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
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
        Rule other = (Rule) obj;
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
     * Evaluates the Rule on the given Portfolio, optionally relative to a given benchmark.
     *
     * @param portfolio the Portfolio on which to evaluate the Rule
     * @param benchmark the (possibly null) benchmark to evaluate relative to
     * @return true if the Rule passes, false if it fails
     */
    public boolean evaluate(Portfolio portfolio, Portfolio benchmark) {
        try {
            double value = eval(portfolio, benchmark);

            if (lowerLimit != null && (value != Double.NaN && value < lowerLimit)) {
                return false;
            }

            if (upperLimit != null && (value == Double.NaN || value > upperLimit)) {
                return false;
            }

            return true;
        } catch (Exception e) {
            // for now, any unexpected exception results in failure
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

    /**
     * Evaluates the Rule on the given Portfolio, optionally relative to a given benchmark. The
     * public evaluate() methods ultimately delegate to this one.
     *
     * @param portfolio the Portfolio on which to evaluate the Rule
     * @param benchmark the (possibly null) benchmark to evaluate relative to
     * @return the calculation result of evaluating the rule
     */
    protected abstract double eval(Portfolio portfolio, Portfolio benchmark);
}
