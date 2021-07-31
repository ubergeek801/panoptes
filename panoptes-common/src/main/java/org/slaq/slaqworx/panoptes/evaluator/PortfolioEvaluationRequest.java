package org.slaq.slaqworx.panoptes.evaluator;

import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.trade.Transaction;
import org.slaq.slaqworx.panoptes.util.ApplicationContextAware;

/**
 * A {@link Callable} which facilitates clustered {@link Portfolio} evaluation by serializing the
 * evaluation parameters for execution on a remote cluster node.
 *
 * @author jeremy
 */
public class PortfolioEvaluationRequest
    implements Callable<Map<RuleKey, EvaluationResult>>, ApplicationContextAware,
    ProtobufSerializable {
  @Nonnull
  private final PortfolioKey portfolioKey;
  private final Transaction transaction;
  @Nonnull
  private final EvaluationContext evaluationContext;

  private ApplicationContext applicationContext;

  /**
   * Creates a new {@link PortfolioEvaluationRequest} with the given parameters.
   *
   * @param portfolioKey
   *     the {@link PortfolioKey} identifying the {@link Portfolio} to be evaluated
   * @param transaction
   *     the (possibly {@code null} {@link Transaction} to be evaluated with the {@link Portfolio}
   * @param evaluationContext
   *     the {@link EvaluationContext} under which to evaluate
   */
  public PortfolioEvaluationRequest(@Nonnull PortfolioKey portfolioKey, Transaction transaction,
      @Nonnull EvaluationContext evaluationContext) {
    this.portfolioKey = portfolioKey;
    this.transaction = transaction;
    this.evaluationContext = evaluationContext;
  }

  @Override
  public Map<RuleKey, EvaluationResult> call() {
    // note that this code executes on the server side; thus it needs to bootstrap the resources
    // it needs (namely the AssetCache and a local PortfolioEvaluator)

    PortfolioEvaluator evaluator =
        applicationContext.getBean(PortfolioEvaluator.class, Qualifiers.byName("local"));
    return evaluator.evaluate(portfolioKey, transaction, evaluationContext).join();
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
    PortfolioEvaluationRequest other = (PortfolioEvaluationRequest) obj;
    if (portfolioKey == null) {
      if (other.portfolioKey != null) {
        return false;
      }
    } else if (!portfolioKey.equals(other.portfolioKey)) {
      return false;
    }
    if (transaction == null) {
      return other.transaction == null;
    } else {
      return transaction.equals(other.transaction);
    }
  }

  /**
   * Obtains the {@link EvaluationContext} in effect for this evaluation request.
   *
   * @return a {@link EvaluationContext}
   */
  public EvaluationContext getEvaluationContext() {
    return evaluationContext;
  }

  /**
   * Obtains the {@link PortfolioKey} identifying the {@link Portfolio} to be evaluated.
   *
   * @return the evaluated {@link Portfolio}'s key
   */
  public PortfolioKey getPortfolioKey() {
    return portfolioKey;
  }

  /**
   * Obtains the {@link Transaction} to be evaluated with the requested {@link Portfolio}.
   *
   * @return the {@link Transaction} to be evaluated, or {@code null} if not applicable
   */
  public Transaction getTransaction() {
    return transaction;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((portfolioKey == null) ? 0 : portfolioKey.hashCode());
    result = prime * result + ((transaction == null) ? 0 : transaction.hashCode());

    return result;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }
}
