package org.slaq.slaqworx.panoptes.rule;

/**
 * EvaluationResult encapsulates the results of a Rule evaluation. Currently this class contains
 * aspects of Boolean- and value-based evaluations, which possibly should be separated.
 *
 * @author jeremy
 */
public class EvaluationResult {
    /**
     * Impact describes the impact of some change (such as a proposed Trade) on a Rule, as evaluated
     * before and after the proposed changes are considered.
     */
    public enum Impact {
        /**
         * the change negatively impacts the Rule evaluation
         */
        NEGATIVE,
        /**
         * the change has no actionable impact on the Rule evaluation
         */
        NEUTRAL,
        /**
         * the change positively impacts the Rule evaluation
         */
        POSITIVE,
        /**
         * the impact of the change cannot be determined
         */
        UNKNOWN
    }

    /**
     * Threshold indicates whether a value-based result is below, within or above the evaluated
     * Rule's threshold.
     * <p>
     * TODO there are probably better names for these
     */
    public static enum Threshold {
        BELOW, WITHIN, ABOVE
    }

    private final boolean isPassed;
    private final Threshold threshold;
    private final Double value;
    private final Throwable exception;

    /**
     * Creates a new Boolean-based EvaluationResult indicating whether the evaluation passed.
     *
     * @param isPassed
     *            true if the evaluation passed, false otherwise
     */
    public EvaluationResult(boolean isPassed) {
        this.isPassed = isPassed;
        threshold = null;
        value = null;
        exception = null;
    }

    /**
     * Creates a new value-based EvaluationResult indicating the threshold status for evaluation and
     * the actual result value.
     *
     * @param threshold
     *            a Threshold value indicating whether the value is within the evaluation threshold
     * @param value
     *            the actual evaluation result value
     */
    public EvaluationResult(Threshold threshold, double value) {
        isPassed = (threshold == Threshold.WITHIN);
        this.threshold = threshold;
        this.value = value;
        exception = null;
    }

    /**
     * Creates a new Boolean-based EvaluationResult indicating that the evaluation failed due to
     * some exception.
     *
     * @param exception
     *            the exception causing the failure
     */
    public EvaluationResult(Throwable exception) {
        isPassed = false;
        threshold = null;
        value = null;
        this.exception = exception;
    }

    /**
     * Compares this EvaluationResult (which is interpreted as the result of some proposed change
     * such as a Trade) to the given EvaluationResult (which is interpreted as the value prior to
     * the proposed change).
     *
     * @param originalResult
     *            the EvalautionResult to compare to
     * @return an Impact describing the impact of the change on the evaluations
     */
    public Impact compare(EvaluationResult originalResult) {
        if (originalResult == null) {
            // TODO decide whether this even makes sense
            return Impact.UNKNOWN;
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
     * Obtains the Exception associated with this result.
     *
     * @return the Exception causing an evaluation failure, or null if none occurred
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * Obtains the value associated with this result.
     *
     * @return the value associated with this result (if value-based), or null if it does not exist
     *         (Boolean-based)
     */
    public Double getValue() {
        return value;
    }

    /**
     * Indicates whether the rule evaluation passed.
     *
     * @return true if the evaluation passed, false otherwise
     */
    public boolean isPassed() {
        return isPassed;
    }
}
