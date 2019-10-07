package org.slaq.slaqworx.panoptes.rule;

import java.io.Serializable;

/**
 * {@code RuleResult} encapsulates the results of a {@code Rule} evaluation, typically grouped by
 * {@code EvaluationGroup} and aggregated into an {@code EvaluationResult}. Currently this class
 * contains aspects of Boolean- and value-based evaluations, which possibly should be separated.
 *
 * @author jeremy
 */
public class RuleResult implements Serializable {
    /**
     * {@code Impact} describes the impact of some change (such as a proposed {@code Trade}) on a
     * {@code Rule}, as evaluated before and after the proposed changes are considered.
     */
    public enum Impact {
        /**
         * the change negatively impacts the {@code Rule} evaluation
         */
        NEGATIVE,
        /**
         * the change has no actionable impact on the {@code Rule} evaluation
         */
        NEUTRAL,
        /**
         * the change positively impacts the {@code Rule} evaluation
         */
        POSITIVE,
        /**
         * the impact of the change cannot be determined
         */
        UNKNOWN
    }

    /**
     * {@code Threshold} indicates whether a value-based result is below, within or above the
     * evaluated {@code Rule}'s threshold.
     * <p>
     * TODO there are probably better names for these
     */
    public static enum Threshold {
        BELOW, WITHIN, ABOVE
    }

    private static final long serialVersionUID = 1L;

    private final boolean isPassed;
    private final Threshold threshold;
    private final Double value;

    private final Throwable exception;

    /**
     * Creates a new Boolean-based {@code RuleResult} indicating whether the evaluation passed.
     *
     * @param isPassed
     *            {@code true} if the evaluation passed, {@code false} otherwise
     */
    public RuleResult(boolean isPassed) {
        this.isPassed = isPassed;
        threshold = null;
        value = null;
        exception = null;
    }

    /**
     * Creates a new value-based {@code RuleResult} indicating the threshold status for evaluation
     * and the actual result value.
     *
     * @param threshold
     *            a {@code Threshold} value indicating whether the value is within the evaluation
     *            threshold
     * @param value
     *            the actual evaluation result value
     */
    public RuleResult(Threshold threshold, double value) {
        isPassed = (threshold == Threshold.WITHIN);
        this.threshold = threshold;
        this.value = value;
        exception = null;
    }

    /**
     * Creates a new Boolean-based {@code RuleResult} indicating that the evaluation failed due to
     * some exception.
     *
     * @param exception
     *            the exception causing the failure
     */
    public RuleResult(Throwable exception) {
        isPassed = false;
        threshold = null;
        value = null;
        this.exception = exception;
    }

    /**
     * Compares this {@code RuleResult} (which is interpreted as the result of some proposed change
     * such as a {@code Trade}) to the given {@code RuleResult} (which is interpreted as the value
     * prior to the proposed change).
     *
     * @param originalResult
     *            the {@code RuleResult} to compare to
     * @return an {@code Impact} describing the impact of the change on the evaluations
     */
    public Impact compare(RuleResult originalResult) {
        if (originalResult == null) {
            // groupings may appear in the proposed state that did not appear in the original state;
            // in this case consider a pass to be neutral and a fail to be negative
            return (isPassed ? Impact.NEUTRAL : Impact.NEGATIVE);
        }

        if (value == null || originalResult.value == null || threshold == null
                || originalResult.threshold == null) {
            // values are unavailable so only compare pass/fail
            if (isPassed) {
                return (originalResult.isPassed ? Impact.NEUTRAL : Impact.POSITIVE);
            }

            return (originalResult.isPassed ? Impact.NEGATIVE : Impact.NEUTRAL);
        }

        switch (threshold) {
        case WITHIN:
            // if we are now within the limit, then the result is either neutral (if originally
            // within the limit) or positive (if not)
            return (originalResult.threshold == Threshold.WITHIN ? Impact.NEUTRAL
                    : Impact.POSITIVE);
        case BELOW:
            switch (originalResult.threshold) {
            case WITHIN:
                // now outside the limit; must be negative impact
                return Impact.NEGATIVE;
            case BELOW:
                // impact is negative, neutral or positive depending on if value decreased, remained
                // or increased
                if (value < originalResult.value) {
                    return Impact.NEGATIVE;
                }
                if (value > originalResult.value) {
                    return Impact.POSITIVE;
                }
                return Impact.NEUTRAL;
            case ABOVE:
            default:
                // going from one extreme to the other seems unusual, but is possible; simply treat
                // is a negative impact
                return Impact.NEGATIVE;
            }
        case ABOVE:
        default:
            switch (originalResult.threshold) {
            case WITHIN:
                // now outside the limit; must be negative impact
                return Impact.NEGATIVE;
            case BELOW:
                // same unusual case as described above
                return Impact.NEGATIVE;
            case ABOVE:
            default:
                // impact is negative, neutral or positive depending on if value increased, remained
                // or decreased
                if (value > originalResult.value) {
                    return Impact.NEGATIVE;
                }
                if (value < originalResult.value) {
                    return Impact.POSITIVE;
                }
                return Impact.NEUTRAL;
            }
        }
    }

    /**
     * Obtains the {@code Exception} associated with this result.
     *
     * @return the {@code Exception} causing an evaluation failure, or {@code null} if none occurred
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * Obtains the value associated with this result.
     *
     * @return the value associated with this result (if value-based), or {@code null} if it does
     *         not exist (Boolean-based)
     */
    public Double getValue() {
        return value;
    }

    /**
     * Indicates whether the {@code Rule} evaluation passed.
     *
     * @return {@code true} if the evaluation passed, {@code false} otherwise
     */
    public boolean isPassed() {
        return isPassed;
    }
}
