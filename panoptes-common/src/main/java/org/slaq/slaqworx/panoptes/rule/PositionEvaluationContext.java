package org.slaq.slaqworx.panoptes.rule;

import java.util.function.Predicate;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;

/**
 * An {@link EvaluationContext} wrapper which can be thought of as the {@link EvaluationContext} for
 * a {@link Position}, as it is a composite of the two; it mostly exists for the convenience of
 * specifying {@link Position} filters as {@link Predicate}s. Unlike an {@link EvaluationContext}
 * which may be used for an entire {@link Portfolio} evaluation, a {@link PositionEvaluationContext}
 * is unique to the {@link Position} being evaluated and may contain additional state about the
 * evaluation.
 *
 * @author jeremy
 */
public class PositionEvaluationContext {
  private final Position position;
  private final EvaluationContext evaluationContext;

  private Throwable exception;

  /**
   * Creates a new {@link PositionEvaluationContext} comprising the given {@link Position} and
   * {@link EvaluationContext}.
   *
   * @param position
   *     the {@link Position} to be evaluated in the context
   * @param evaluationContext
   *     the context in which to evaluate the {@link Position}
   */
  public PositionEvaluationContext(Position position, EvaluationContext evaluationContext) {
    this.position = position;
    this.evaluationContext = evaluationContext;
  }

  /**
   * Obtains the {@link EvaluationContext} of this context.
   *
   * @return an {@link EvaluationContext}
   */
  public EvaluationContext getEvaluationContext() {
    return evaluationContext;
  }

  /**
   * Obtains the exception that occurred during evaluation, if any.
   *
   * @return a (possibly {@code null}) exception
   */
  public Throwable getException() {
    return exception;
  }

  /**
   * Indicates that an exception occurred during evaluation.
   *
   * @param exception
   *     the exception that occurred during evaluation
   */
  public void setException(Throwable exception) {
    this.exception = exception;
  }

  /**
   * Obtains the {@link Position} of this context.
   *
   * @return a {@link Position}
   */
  public Position getPosition() {
    return position;
  }
}
