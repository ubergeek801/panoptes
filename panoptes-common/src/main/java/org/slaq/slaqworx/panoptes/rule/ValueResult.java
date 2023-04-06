package org.slaq.slaqworx.panoptes.rule;

import java.util.Arrays;
import java.util.Objects;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.trade.Trade;

/**
 * Encapsulates the results of a single {@link Rule} evaluation, typically grouped by {@link
 * EvaluationGroup} and aggregated into an {@link EvaluationResult}. Currently this class contains
 * aspects of Boolean- and value-based evaluations, which possibly should be separated.
 *
 * <p>For benchmark-relative rules, a {@link ValueResult} is determined for both the base/target
 * portfolio and the benchmark portfolio individually, to be combined eventually by a {@code
 * org.slaq.slaqworx.panoptes.evaluator.BenchmarkComparator}.
 *
 * @author jeremy
 */
public class ValueResult {
  // results with differences within this margin of error are treated as equal
  private static final double EPSILON = 0.000_001;
  private final boolean isPassed;
  private final Threshold threshold;
  private final Double value;
  private final Double benchmarkValue;
  private final Throwable exception;

  /**
   * Creates a new Boolean-based {@link ValueResult} indicating whether the evaluation passed.
   *
   * @param isPassed {@code true} if the evaluation passed, {@code false} otherwise
   */
  public ValueResult(boolean isPassed) {
    this.isPassed = isPassed;
    threshold = null;
    value = null;
    benchmarkValue = null;
    exception = null;
  }

  /**
   * Creates a new value-based {@link ValueResult} indicating the threshold status for evaluation
   * and the actual result value.
   *
   * @param threshold a {@link Threshold} value indicating whether the value is within the
   *     evaluation threshold
   * @param value the actual evaluation result value
   */
  public ValueResult(Threshold threshold, double value) {
    this(threshold, value, null);
  }

  /**
   * Creates a new value-based {@link ValueResult} indicating the threshold status for evaluation
   * and the actual result value.
   *
   * @param threshold a {@link Threshold} value indicating whether the value is within the
   *     evaluation threshold
   * @param value the actual evaluation result value
   * @param benchmarkValue the (possibly {@code null}) benchmark evaluation result value
   */
  public ValueResult(Threshold threshold, double value, Double benchmarkValue) {
    isPassed = (threshold == Threshold.WITHIN);
    this.threshold = threshold;
    this.value = value;
    this.benchmarkValue = benchmarkValue;
    exception = null;
  }

  /**
   * Creates a new Boolean-based {@link ValueResult} indicating that the evaluation failed due to
   * some exception.
   *
   * @param exception the exception causing the failure
   */
  public ValueResult(Throwable exception) {
    isPassed = false;
    threshold = null;
    value = null;
    benchmarkValue = null;
    this.exception = exception;
  }

  /**
   * Compares this {@link ValueResult} (which is interpreted as the result of some proposed change
   * such as a {@link Trade}) to the given {@link ValueResult} (which is interpreted as the value
   * prior to the proposed change).
   *
   * @param originalResult the {@link ValueResult} to compare to
   * @return an {@link Impact} describing the impact of the change on the evaluations
   */
  public Impact compare(ValueResult originalResult) {
    if (originalResult == null) {
      // groupings may appear in the proposed state that did not appear in the original state;
      // in this case consider a pass to be neutral and a fail to be negative
      return (isPassed ? Impact.NEUTRAL : Impact.NEGATIVE);
    }

    if (value == null
        || originalResult.value == null
        || threshold == null
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
        return (originalResult.threshold == Threshold.WITHIN ? Impact.NEUTRAL : Impact.POSITIVE);
      case BELOW:
        switch (originalResult.threshold) {
          case WITHIN:
            // now outside the limit; must be negative impact
            return Impact.NEGATIVE;
          case BELOW:
            // impact is negative, neutral or positive depending on if value decreased, remained
            // or increased
            double diff = value - originalResult.value;
            if (Math.abs(diff) < EPSILON) {
              return Impact.NEUTRAL;
            }
            if (diff < 0) {
              return Impact.NEGATIVE;
            }
            return Impact.POSITIVE;
          case ABOVE:
          default:
            // going from one extreme to the other seems unusual, but is possible; simply treat
            // as a negative impact
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
            double diff = value - originalResult.value;
            if (Math.abs(diff) < EPSILON) {
              return Impact.NEUTRAL;
            }
            if (diff > 0) {
              return Impact.NEGATIVE;
            }
            return Impact.POSITIVE;
        }
    }
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
    ValueResult other = (ValueResult) obj;
    if (!exceptionEquals(exception, other.exception)) {
      return false;
    }
    if (isPassed != other.isPassed) {
      return false;
    }
    if (threshold != other.threshold) {
      return false;
    }
    if (value == null) {
      return other.value == null;
    } else {
      return value.equals(other.value);
    }
  }

  /**
   * Obtains the benchmark value associated with this result.
   *
   * @return the benchmark value associated with this result (if value-based), or {@code null} if it
   *     does not exist (Boolean-based) or there was no benchmark comparison
   */
  public Double getBenchmarkValue() {
    return benchmarkValue;
  }

  /**
   * Obtains the {@link Exception} associated with this result.
   *
   * @return the {@link Exception} causing an evaluation failure, or {@code null} if none occurred
   */
  public Throwable getException() {
    return exception;
  }

  /**
   * Obtains the {@link Threshold} associated with this result.
   *
   * @return the {@link Threshold} applied by the {@link Rule} that generated this result
   */
  public Threshold getThreshold() {
    return threshold;
  }

  /**
   * Obtains the value associated with this result.
   *
   * @return the value associated with this result (if value-based), or {@code null} if it does not
   *     exist (Boolean-based)
   */
  public Double getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (isPassed ? 1231 : 1237);
    result = prime * result + ((threshold == null) ? 0 : threshold.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());

    return result;
  }

  /**
   * Indicates whether the {@link Rule} evaluation passed.
   *
   * @return {@code true} if the evaluation passed, {@code false} otherwise
   */
  public boolean isPassed() {
    return isPassed;
  }

  @Override
  public String toString() {
    return "RuleResult [isPassed="
        + isPassed
        + ", threshold="
        + threshold
        + ", value="
        + value
        + ", exception="
        + exception
        + "]";
  }

  /**
   * Determines whether the given exceptions are considered equivalent for our purposes, since
   * {@link Exception} does not implement {@code equals()}.
   *
   * @param exception1 the first exception to be compared
   * @param exception2 the second exception to be compared
   * @return {@code true} if the exceptions are considered to be equivalent, {@code false} otherwise
   */
  protected boolean exceptionEquals(Throwable exception1, Throwable exception2) {
    if (exception1 == null) {
      // nulls are considered equal; null is considered unequal to non-null
      return (exception2 == null);
    }

    if (exception1 == exception2) {
      return true;
    }

    if (!Objects.equals(exception1.getClass().getName(), exception2.getClass().getName())) {
      return false;
    }

    if (!Objects.equals(exception1.getMessage(), exception2.getMessage())) {
      return false;
    }

    return Arrays.equals(exception1.getStackTrace(), exception2.getStackTrace());
  }

  /**
   * Describes the impact of some change (such as a proposed {@link Trade}) on a {@link Rule}, as
   * evaluated before and after the proposed changes are considered.
   */
  public enum Impact {
    /** the change negatively impacts the {@link Rule} evaluation */
    NEGATIVE,
    /** the change has no actionable impact on the {@link Rule} evaluation */
    NEUTRAL,
    /** the change positively impacts the {@link Rule} evaluation */
    POSITIVE,
    /** the impact of the change cannot be determined */
    UNKNOWN
  }

  /**
   * Indicates whether a value-based result is below, within or above the evaluated {@link Rule}'s
   * threshold.
   *
   * <p>TODO there are probably better names for these
   */
  public enum Threshold {
    BELOW,
    WITHIN,
    ABOVE
  }
}
