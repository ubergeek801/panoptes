package org.slaq.slaqworx.panoptes.trade;

import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.concurrent.Callable;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.util.ApplicationContextAware;

/**
 * A {@link Callable} which facilitates clustered {@link Trade} evaluation by serializing the
 * evaluation parameters for execution on a remote cluster node.
 *
 * @author jeremy
 */
public class TradeEvaluationRequest
    implements Callable<TradeEvaluationResult>, ApplicationContextAware, ProtobufSerializable {
  private final TradeKey tradeKey;
  private final EvaluationContext evaluationContext;

  private ApplicationContext applicationContext;

  /**
   * Creates a new {@link TradeEvaluationRequest} with the given parameters.
   *
   * @param tradeKey the {@link TradeKey} identifying the {@link Trade} to be evaluated
   * @param evaluationContext the {@link EvaluationContext} under which to evaluate
   */
  public TradeEvaluationRequest(TradeKey tradeKey, EvaluationContext evaluationContext) {
    this.tradeKey = tradeKey;
    this.evaluationContext = evaluationContext;
  }

  @Override
  public TradeEvaluationResult call() throws Exception {
    // note that this code executes on the server side; thus it needs to bootstrap the resources
    // it needs (namely the AssetCache and a local TradeEvaluator)

    AssetCache assetCache = applicationContext.getBean(AssetCache.class);

    Trade trade = assetCache.getTrade(tradeKey);

    TradeEvaluator evaluator =
        applicationContext.getBean(TradeEvaluator.class, Qualifiers.byName("local"));
    return evaluator.evaluate(trade, evaluationContext).get();
  }

  /**
   * Obtains the {@link TradeKey} identifying the {@link Trade} to be evaluated.
   *
   * @return a {@link TradeKey}
   */
  public TradeKey getTradeKey() {
    return tradeKey;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }
}
