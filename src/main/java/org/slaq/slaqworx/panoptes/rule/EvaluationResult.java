package org.slaq.slaqworx.panoptes.rule;

/**
 * EvaluationResult encapsulates the results of a Rule evaluation.
 *
 * @author jeremy
 */
public class EvaluationResult {
    private final boolean isPassed;
    private final Throwable exception;

    public EvaluationResult(boolean isPassed) {
        this.isPassed = isPassed;
        exception = null;
    }

    public EvaluationResult(Throwable exception) {
        isPassed = false;
        this.exception = exception;
    }

    public Throwable getException() {
        return exception;
    }

    public boolean isPassed() {
        return isPassed;
    }
}
